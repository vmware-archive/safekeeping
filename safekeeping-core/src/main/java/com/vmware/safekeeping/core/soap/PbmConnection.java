/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.safekeeping.core.soap;

import java.net.URL;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmAboutInfo;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmNonExistentHubsFaultMsg;
import com.vmware.pbm.PbmObjectType;
import com.vmware.pbm.PbmPlacementHub;
import com.vmware.pbm.PbmPortType;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmProfileResourceType;
import com.vmware.pbm.PbmProfileResourceTypeEnum;
import com.vmware.pbm.PbmServerObjectRef;
import com.vmware.pbm.PbmService;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.pbm.RuntimeFaultFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.soap.sso.HeaderHandlerResolver;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;

/**
 * @author mdaneri
 *
 */
public class PbmConnection {
    private static final String PBMSERVICEINSTANCETYPE = "PbmServiceInstance";
    private static final String PBMSERVICEINSTANCEVALUE = "ServiceInstance";
    private static Logger logger = Logger.getLogger(PbmConnection.class.getName());
    private PbmPortType pbmPort;
    private PbmService pbmService;
    private PbmServiceInstanceContent pbmServiceContent;
    private ManagedObjectReference pbmSvcInstRef;
    private PbmAboutInfo pbmAboutInfo;
    private String instanceUuid;
    private final URL url;
    private final VimConnection vimConnection;

    PbmConnection(final VimConnection vimConnection, final URL pbmUrl) {
        this.url = pbmUrl;
        this.vimConnection = vimConnection;
    }

    public PbmServiceInstanceContent connectPbm() throws ConnectException {
        this.pbmService = new PbmService();

        updatePbmHeaderHandlerResolver();
        this.pbmPort = this.pbmService.getPbmPort();
        final Map<String, Object> pbmCtxt = ((BindingProvider) this.pbmPort).getRequestContext();
        pbmCtxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        pbmCtxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getUrl().toString());
        try {
            this.pbmServiceContent = this.pbmPort.pbmRetrieveServiceContent(getPbmServiceInstanceReference());
        } catch (final RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
            throw new ConnectException(String.format("failed to connect to %s not valid", getUrl().toString()), e);
        }
        this.pbmAboutInfo = this.pbmServiceContent.getAboutInfo();
        this.instanceUuid = this.pbmAboutInfo.getInstanceUuid();

        return this.pbmServiceContent;
    }

    public boolean doesPpmProfileExist(final PbmProfileId profId) throws RuntimeFaultFaultMsg {
        final PbmServiceInstanceContent spbmsc = getPbmServiceContent();
        final ManagedObjectReference profileMgr = spbmsc.getProfileManager();
        final List<PbmProfileId> profileIds = new ArrayList<>();

        if (profId != null) {
            profileIds.add(profId);
            List<PbmProfile> pbmProfiles;
            try {
                pbmProfiles = getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
                return (pbmProfiles != null) && !pbmProfiles.isEmpty();
            } catch (final InvalidArgumentFaultMsg e) {
                Utility.logWarning(logger, e);

            }
        }
        return false;
    }

    public boolean doesPpmProfileExist(final String name) throws InvalidArgumentFaultMsg, RuntimeFaultFaultMsg {
        final PbmServiceInstanceContent spbmsc = getPbmServiceContent();
        final ManagedObjectReference profileMgr = spbmsc.getProfileManager();
        final List<PbmProfileId> profileIds = getPbmPort().pbmQueryProfile(profileMgr, getStorageResourceType(), null);

        if (((profileIds != null) && !profileIds.isEmpty())) {

            final List<PbmProfile> pbmProfiles = getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
            for (final PbmProfile pbmProfile : pbmProfiles) {
                if (pbmProfile.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the default requirement profile ID for the given datastore.
     * 
     * @param ManagedEntityInfo datastore
     * @return VirtualMachineDefinedProfileSpec of the default profile
     * @throws InvalidArgumentFaultMsg    Thrown if entity is invalid (does not
     *                                    denote a datastore).
     * @throws PbmFaultFaultMsg           Internal service error.
     * @throws PbmNonExistentHubsFaultMsg Thrown if datastore is non existent.
     * @throws RuntimeFaultFaultMsg       Thrown if any type of runtime fault is
     *                                    thrown that is not covered by the other
     *                                    faults; for example, a communication
     *                                    error.
     */
    public VirtualMachineDefinedProfileSpec geeDefaultProfile(final ManagedEntityInfo entity)
            throws InvalidArgumentFaultMsg, PbmFaultFaultMsg, PbmNonExistentHubsFaultMsg, RuntimeFaultFaultMsg {
        final List<PbmPlacementHub> pHub = new ArrayList<>();
        final PbmPlacementHub dHub = new PbmPlacementHub();
        dHub.setHubId(entity.getMorefValue());
        dHub.setHubType(entity.getEntityType().toString());
        pHub.add(dHub);
        PbmProfileId profileId = getPbmPort().pbmQueryDefaultRequirementProfile(getPbmProfileManager(), dHub);
        if (profileId != null) {
            final VirtualMachineDefinedProfileSpec pbmProfile = new VirtualMachineDefinedProfileSpec();
            pbmProfile.setProfileId(profileId.getUniqueId());
            return pbmProfile;
        }
        return null;
    }

    public PbmAboutInfo getAboutInfo() {
        return this.pbmAboutInfo;
    }

    /**
     * Returns identifiers for profiles associated with an Improved Virtual Disk
     * 
     * @param ImprovedVirtualDisk ivd the Improved Virtual Disk
     * @return list of PbmProfileId associated with the Improved Virtual Disk
     * @throws PbmFaultFaultMsg     Internal service error.
     * @throws RuntimeFaultFaultMsg Thrown if any type of runtime fault is thrown
     *                              that is not covered by the other faults; for
     *                              example, a communication error.
     */
    public List<PbmProfileId> getAssociatedProfile(final ImprovedVirtualDisk ivd)
            throws PbmFaultFaultMsg, RuntimeFaultFaultMsg {
        final PbmServerObjectRef pbmVm = new PbmServerObjectRef();
        pbmVm.setKey(ivd.getUuid());
        pbmVm.setObjectType(PbmObjectType.VIRTUAL_DISK_UUID.value());
        pbmVm.setServerUuid(ivd.getVcenterInstanceUuid());
        return getPbmPort().pbmQueryAssociatedProfile(getPbmProfileManager(), pbmVm);
    }

    /**
     * Returns identifiers for profiles associated with an Virtual Machine
     * 
     * @param VirtualMachineManager vmm the Virtual Machine
     * @return list of PbmProfileId associated with the Virtual Machine
     * @throws PbmFaultFaultMsg     Internal service error.
     * @throws RuntimeFaultFaultMsg Thrown if any type of runtime fault is thrown
     *                              that is not covered by the other faults; for
     *                              example, a communication error.
     */
    public List<PbmProfileId> getAssociatedProfile(final VirtualMachineManager vmm)
            throws PbmFaultFaultMsg, RuntimeFaultFaultMsg {
        final PbmServerObjectRef pbmVm = new PbmServerObjectRef();
        pbmVm.setKey(vmm.getMorefValue());
        pbmVm.setObjectType(PbmObjectType.VIRTUAL_MACHINE_AND_DISKS.value());
        pbmVm.setServerUuid(vmm.getVcenterInstanceUuid());
        return getPbmPort().pbmQueryAssociatedProfile(getPbmProfileManager(), pbmVm);
    }

    /**
     * Returns identifiers for profiles associated with an Virtual Machine disk
     * 
     * @param VirtualMachineManager vmm the Virtual Machine
     * @param int                   key the Virtual Machine disk device key
     * @return list of PbmProfileId associated with the Virtual Machine disk
     * @throws PbmFaultFaultMsg     Internal service error.
     * @throws RuntimeFaultFaultMsg Thrown if any type of runtime fault is thrown
     *                              that is not covered by the other faults; for
     *                              example, a communication error.
     */
    public List<PbmProfileId> getAssociatedProfile(final VirtualMachineManager vmm, final int key)
            throws PbmFaultFaultMsg, RuntimeFaultFaultMsg {
        final PbmServerObjectRef pbmVm = new PbmServerObjectRef();
        pbmVm.setKey(String.format("%s:%d", vmm.getMorefValue(), key));
        pbmVm.setObjectType(PbmObjectType.VIRTUAL_DISK_ID.value());
        pbmVm.setServerUuid(vmm.getVcenterInstanceUuid());
        return getPbmPort().pbmQueryAssociatedProfile(getPbmProfileManager(), pbmVm);
    }

    public String getCookie() {
        return this.vimConnection.getCookie();
    }

    public String getCookieValue() {
        return this.vimConnection.getCookieValue();

    }

    public VirtualMachineDefinedProfileSpec getDefinedProfileSpec(final PbmProfileId profile)
            throws RuntimeFaultFaultMsg {
        if ((profile != null) && (doesPpmProfileExist(profile))) {
            final VirtualMachineDefinedProfileSpec pbmProfile = new VirtualMachineDefinedProfileSpec();
            pbmProfile.setProfileId(profile.getUniqueId());
            return pbmProfile;
        }
        return null;

    }

    public List<VirtualMachineDefinedProfileSpec> getDefinedProfileSpec(final Set<String> pbmProfileNames)
            throws InvalidArgumentFaultMsg, RuntimeFaultFaultMsg {

        final LinkedList<VirtualMachineDefinedProfileSpec> result = new LinkedList<>();

        if ((pbmProfileNames != null) && !pbmProfileNames.isEmpty()) {
            for (final String prof : pbmProfileNames) {
                if (doesPpmProfileExist(prof)) {
                    final PbmCapabilityProfile profile = getPbmProfile(prof);

                    final VirtualMachineDefinedProfileSpec pbmProfile = new VirtualMachineDefinedProfileSpec();
                    pbmProfile.setProfileId(profile.getProfileId().getUniqueId());
                    result.add(pbmProfile);
                } else {
                    result.add(null);
                }
            }
        }
        return result;

    }

    /**
     * Retrieve the Profile based on the name
     *
     * @param pbmProfileName
     * @return
     * @throws InvalidArgumentFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public VirtualMachineDefinedProfileSpec getDefinedProfileSpec(final String pbmProfileName)
            throws InvalidArgumentFaultMsg, RuntimeFaultFaultMsg {

        if ((pbmProfileName != null) && (doesPpmProfileExist(pbmProfileName))) {
            final PbmCapabilityProfile profile = getPbmProfile(pbmProfileName);
            final VirtualMachineDefinedProfileSpec pbmProfile = new VirtualMachineDefinedProfileSpec();
            pbmProfile.setProfileId(profile.getProfileId().getUniqueId());
            return pbmProfile;
        }
        return null;
    }

    public String getInstanceUuid() {
        return this.instanceUuid;
    }

    public PbmPortType getPbmPort() {
        return this.pbmPort;
    }

    public PbmCapabilityProfile getPbmProfile(final PbmProfileId profId) throws RuntimeFaultFaultMsg {
        final PbmServiceInstanceContent spbmsc = getPbmServiceContent();
        final ManagedObjectReference profileMgr = spbmsc.getProfileManager();
        final List<PbmProfileId> profileIds = new ArrayList<>();
        profileIds.add(profId);

        List<PbmProfile> pbmProfiles;
        try {
            pbmProfiles = getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
            if (!pbmProfiles.isEmpty()) {
                return (PbmCapabilityProfile) pbmProfiles.get(0);
            }
        } catch (final InvalidArgumentFaultMsg e) {
            Utility.logWarning(logger, e);

        }
        return null;

    }

    private PbmCapabilityProfile getPbmProfile(final String name) throws InvalidArgumentFaultMsg, RuntimeFaultFaultMsg {
        final PbmServiceInstanceContent spbmsc = getPbmServiceContent();
        final ManagedObjectReference profileMgr = spbmsc.getProfileManager();
        final List<PbmProfileId> profileIds = getPbmPort().pbmQueryProfile(profileMgr, getStorageResourceType(), null);

        if ((profileIds == null) || profileIds.isEmpty()) {
            throw new RuntimeFaultFaultMsg("No storage Profiles exist.", null);
        }
        final List<PbmProfile> pbmProfiles = getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
        for (final PbmProfile pbmProfile : pbmProfiles) {
            if (pbmProfile.getName().equals(name)) {
                return (PbmCapabilityProfile) pbmProfile;
            }
        }
        throw new RuntimeFaultFaultMsg("Profile with the given name does not exist", null);
    }

    public ManagedObjectReference getPbmProfileManager() {
        return getPbmServiceContent().getProfileManager();
    }

    public PbmService getPbmService() {
        return this.pbmService;
    }

    public PbmServiceInstanceContent getPbmServiceContent() {
        return this.pbmServiceContent;
    }

    public ManagedObjectReference getPbmServiceInstanceReference() {
        if (this.pbmSvcInstRef == null) {
            final ManagedObjectReference ref = new ManagedObjectReference();
            ref.setType(PBMSERVICEINSTANCETYPE);
            ref.setValue(PBMSERVICEINSTANCEVALUE);
            this.pbmSvcInstRef = ref;
        }
        return this.pbmSvcInstRef;
    }

    public PbmProfileResourceType getStorageResourceType() {
        final PbmProfileResourceType resourceType = new PbmProfileResourceType();
        resourceType.setResourceType(PbmProfileResourceTypeEnum.STORAGE.value());
        return resourceType;
    }

    public URL getUrl() {

        return this.url;
    }

    public List<PbmProfile> pbmRetrieveContent(final List<PbmProfileId> profileIds)
            throws InvalidArgumentFaultMsg, RuntimeFaultFaultMsg {
        return getPbmPort().pbmRetrieveContent(getPbmProfileManager(), profileIds);
    }

    @Override
    public String toString() {
        return String.format("Storage Profile Service Ver.%s url: %s", getAboutInfo().getVersion(), getUrl());
    }

    HeaderHandlerResolver updatePbmHeaderHandlerResolver() {
        if (this.pbmService != null) {
            final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();
            headerResolver.addHandler(new VcSessionHandler(getCookieValue()));
            this.pbmService.setHandlerResolver(headerResolver);
            return headerResolver;
        } else {
            return null;
        }
    }
}
