/**
 *
 */
package com.vmware.vmbk.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmAboutInfo;
import com.vmware.pbm.PbmCapabilityMetadata;
import com.vmware.pbm.PbmCapabilityMetadataPerCategory;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmPortType;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.PbmProfileResourceType;
import com.vmware.pbm.PbmProfileResourceTypeEnum;
import com.vmware.pbm.PbmServerObjectRef;
import com.vmware.pbm.PbmService;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.pbm.RuntimeFaultFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.soap.sso.HeaderHandlerResolver;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

/**
 * @author mdaneri
 *
 */
public class PbmConnection {
    private static final String PBM_PATH = "/pbm";
    private static final String PBMSERVICEINSTANCETYPE = "PbmServiceInstance";
    private static final String PBMSERVICEINSTANCEVALUE = "ServiceInstance";
    static Logger logger = Logger.getLogger(PbmConnection.class.getName());
    private PbmPortType pbmPort;
    private PbmService pbmService;
    private PbmServiceInstanceContent pbmServiceContent;
    private ManagedObjectReference pbmSvcInstRef;
    private PbmAboutInfo pbmAboutInfo;
    private String instanceUuid;
    private final URL url;
    private final VimConnection vimConnection;

    PbmConnection(final VimConnection vimConnection) throws MalformedURLException {
	this.url = new URL("https", vimConnection.getHost(), PBM_PATH);

	this.vimConnection = vimConnection;

    }

    public PbmServiceInstanceContent connectPbm() throws MalformedURLException, ConnectException {
	this.pbmService = new PbmService();

	updatePbmHeaderHandlerResolver();
	this.pbmPort = this.pbmService.getPbmPort();
	final Map<String, Object> pbmCtxt = ((BindingProvider) this.pbmPort).getRequestContext();
	pbmCtxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
	pbmCtxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getUrl().toString());
	try {
	    this.pbmServiceContent = this.pbmPort.pbmRetrieveServiceContent(getPbmServiceInstanceReference());
	} catch (final RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    throw new ConnectException(String.format("failed to connect to %s not valid", getUrl().toString()), e);
	}
	this.pbmAboutInfo = this.pbmServiceContent.getAboutInfo();
	this.instanceUuid = this.pbmAboutInfo.getInstanceUuid();

	return this.pbmServiceContent;
    }

    public PbmAboutInfo getAboutInfo() {
	return this.pbmAboutInfo;
    }

    public List<PbmProfileId> getAssociatedProfile(final ImprovedVirtuaDisk ivd)
	    throws PbmFaultFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
	final PbmServerObjectRef pbmVm = new PbmServerObjectRef();
	pbmVm.setKey(ivd.getUuid());
	pbmVm.setObjectType(com.vmware.pbm.PbmObjectType.VIRTUAL_DISK_UUID.value());
	pbmVm.setServerUuid(ivd.getVcenterInstanceUuid());
	final List<PbmProfileId> entities = getPbmPort().pbmQueryAssociatedProfile(getPbmProfileManager(), pbmVm);
	return entities;
    }

    public List<PbmProfileId> getAssociatedProfile(final VirtualMachineManager vmm)
	    throws PbmFaultFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
	final PbmServerObjectRef pbmVm = new PbmServerObjectRef();
	pbmVm.setKey(vmm.getMorefValue());
	pbmVm.setObjectType(com.vmware.pbm.PbmObjectType.VIRTUAL_MACHINE.value());
	pbmVm.setServerUuid(vmm.getVcenterInstanceUuid());
	final List<PbmProfileId> entities = getPbmPort().pbmQueryAssociatedProfile(getPbmProfileManager(), pbmVm);
	return entities;
    }

    public List<PbmProfileId> getAssociatedProfile(final VirtualMachineManager vmm, final int key)
	    throws PbmFaultFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
	final PbmServerObjectRef pbmVm = new PbmServerObjectRef();
	pbmVm.setKey(String.format("%s:%d", vmm.getMorefValue(), key));
	pbmVm.setObjectType(com.vmware.pbm.PbmObjectType.VIRTUAL_DISK_ID.value());
	pbmVm.setServerUuid(vmm.getVcenterInstanceUuid());
	final List<PbmProfileId> entities = getPbmPort().pbmQueryAssociatedProfile(getPbmProfileManager(), pbmVm);
	return entities;
    }

    public PbmCapabilityMetadata getCapabilityMeta(final String capabilityName,
	    final List<PbmCapabilityMetadataPerCategory> schema) {
	for (final PbmCapabilityMetadataPerCategory cat : schema) {
	    for (final PbmCapabilityMetadata cap : cat.getCapabilityMetadata()) {
		if (cap.getId().getId().equals(capabilityName)) {
		    return cap;
		}
	    }
	}
	return null;
    }

    public String getCookie() {
	return this.vimConnection.getCookie();
    }

    public String getCookieValue() {
	return this.vimConnection.getCookieValue();

    }

    public LinkedList<VirtualMachineDefinedProfileSpec> getDefinedProfileSpec(final LinkedList<String> linkedList) {
	try {
	    final LinkedList<VirtualMachineDefinedProfileSpec> result = new LinkedList<>();

	    if ((linkedList != null) && !linkedList.isEmpty()) {
		for (final String prof : linkedList) {
		    final PbmCapabilityProfile profile = getPbmProfile(prof);

		    if (profile == null) {
			return null;
		    }
		    final VirtualMachineDefinedProfileSpec pbmProfile = new VirtualMachineDefinedProfileSpec();
		    pbmProfile.setProfileId(profile.getProfileId().getUniqueId());
		    result.add(pbmProfile);
		}
	    }
	    return result;
	} catch (RuntimeFaultFaultMsg | com.vmware.pbm.InvalidArgumentFaultMsg e) {
	    IoFunction.showWarning(logger, "Profile name:%s doesn't exist on this vcenter", linkedList);
	    logger.warning(Utility.toString(e));
	    return null;
	}
    }

    public String getInstanceUuid() {
	return this.instanceUuid;
    }

    public PbmPortType getPbmPort() {
	return this.pbmPort;
    }

    public PbmCapabilityProfile getPbmProfile(final String name)
	    throws InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, RuntimeFaultFaultMsg {
	final PbmServiceInstanceContent spbmsc = getPbmServiceContent();
	final ManagedObjectReference profileMgr = spbmsc.getProfileManager();
	final List<PbmProfileId> profileIds = getPbmPort().pbmQueryProfile(profileMgr, getStorageResourceType(), null);

	if ((profileIds == null) || profileIds.isEmpty()) {
	    throw new RuntimeFaultFaultMsg("No storage Profiles exist.", null);
	}
	final List<PbmProfile> pbmProfiles = getPbmPort().pbmRetrieveContent(profileMgr, profileIds);
	for (final PbmProfile pbmProfile : pbmProfiles) {
	    if (pbmProfile.getName().equals(name)) {
		final PbmCapabilityProfile profile = (PbmCapabilityProfile) pbmProfile;
		return profile;
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

    public PbmCapabilityMetadata getTagCategoryMeta(final String tagCategoryName,
	    final List<PbmCapabilityMetadataPerCategory> schema) {
	for (final PbmCapabilityMetadataPerCategory cat : schema) {
	    if (cat.getSubCategory().equals("tag")) {
		for (final PbmCapabilityMetadata cap : cat.getCapabilityMetadata()) {
		    if (cap.getId().getId().equals(tagCategoryName)) {
			return cap;
		    }
		}
	    }
	}
	return null;
    }

    public URL getUrl() {

	return this.url;
    }

    public List<PbmProfile> pbmRetrieveContent(final List<PbmProfileId> profileIds)
	    throws com.vmware.pbm.InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
	return getPbmPort().pbmRetrieveContent(getPbmProfileManager(), profileIds);
    }

    @Override
    public String toString() {
	return String.format("Storage Profile Service Ver.%s url: %s", getAboutInfo().getVersion(), getUrl());
    }

    public HeaderHandlerResolver updatePbmHeaderHandlerResolver() {
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
