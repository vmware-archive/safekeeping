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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmNonExistentHubsFaultMsg;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.common.VersionManipulator;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.soap.sso.HeaderHandlerResolver;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.ID;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RetrieveVStorageObjSpec;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vim25.VStorageObjectSnapshotDetails;
import com.vmware.vim25.VStorageObjectSnapshotInfo;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VirtualMachineProfileSpec;
import com.vmware.vim25.VslmCloneSpec;
import com.vmware.vim25.VslmCreateSpec;
import com.vmware.vim25.VslmCreateSpecDiskFileBackingSpec;
import com.vmware.vim25.VslmInfrastructureObjectPolicy;
import com.vmware.vim25.VslmTagEntry;
import com.vmware.vslm.AlreadyExistsFaultMsg;
import com.vmware.vslm.FileFaultFaultMsg;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.InvalidDatastoreFaultMsg;
import com.vmware.vslm.InvalidStateFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.RuntimeFaultFaultMsg;
import com.vmware.vslm.VslmAboutInfo;
import com.vmware.vslm.VslmFaultFaultMsg;
import com.vmware.vslm.VslmPortType;
import com.vmware.vslm.VslmService;
import com.vmware.vslm.VslmServiceInstanceContent;
import com.vmware.vslm.VslmSyncFaultFaultMsg;
import com.vmware.vslm.VslmTaskInfo;
import com.vmware.vslm.VslmVsoVStorageObjectAssociations;
import com.vmware.vslm.VslmVsoVStorageObjectAssociationsVmDiskAssociation;
import com.vmware.vslm.VslmVsoVStorageObjectQueryResult;
import com.vmware.vslm.VslmVsoVStorageObjectQuerySpec;
import com.vmware.vslm.VslmVsoVStorageObjectQuerySpecQueryFieldEnum;
import com.vmware.vslm.VslmVsoVStorageObjectQuerySpecQueryOperatorEnum;
import com.vmware.vslm.VslmVsoVStorageObjectResult;

public class VslmConnection extends AbstractHttpConf {
    private static final String VSLMSERVICEINSTANCETYPE = "VslmServiceInstance";
    private static final String VSLMSERVICEINSTANCEVALUE = "ServiceInstance";
    /**
     * Max Query results
     */
    private static final int MAX_RESULT = 100;
    private static Logger logger = Logger.getLogger(VslmConnection.class.getName());

    private static final int MIN_API_VERSION = 60700;
    private boolean useVslmLogout;

    private String instanceUuid;
    private final VimConnection vimConnection;

    private VslmAboutInfo aboutInfo;

    private VslmService vslmService;
    private VslmPortType vslmPort;
    private VslmServiceInstanceContent serviceContent;
    private ManagedObjectReference svcInstRef;
    private ManagedObjectReference vslmVsoManager;
    private ManagedObjectReference vimVsoManager;

    private final URL url;

    private boolean useVslm;

    VslmConnection(final VimConnection vimConnection, final URL url) {
        this.vimConnection = vimConnection;
        this.url = url;
        this.serviceContent = null;
        setUseVslmLogout(false);
    }

    /**
     * @param id
     * @param category
     * @param tag
     * @throws VslmFaultFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     */
    public void attachTagToVStorageObject(final ID id, final String category, final String tag)
            throws NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.NotFoundFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg {
        if (isVslm()) {
            getvslmPort().vslmAttachTagToVStorageObject(getVsoManager(), id, category, tag);
        } else {
            this.vimConnection.getVimPort().attachTagToVStorageObject(getVsoManager(), id, category, tag);
        }
    }

    public boolean clone(final ImprovedVirtualDisk ivd, final String cloneName, ManagedEntityInfo datastoreInfo,
            final RunningReport cloneReport) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InvalidCollectorVersionFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.NotFoundFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg, NotFoundFaultMsg,
            VslmFaultFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        boolean result = false;
        ManagedObjectReference taskMor = null;
        if (datastoreInfo == null) {
            datastoreInfo = ivd.getDatastoreInfo();
        }

        final VslmCloneSpec cloneSpec = new VslmCloneSpec();
        final VslmCreateSpecDiskFileBackingSpec specDiskFileBackingSpec = new VslmCreateSpecDiskFileBackingSpec();
        specDiskFileBackingSpec.setDatastore(datastoreInfo.getMoref());
        specDiskFileBackingSpec.setProvisioningType(ivd.getBackingProvisionType());
        cloneSpec.setBackingSpec(specDiskFileBackingSpec);
        cloneSpec.setName(cloneName);

        if (isVslm()) {
            taskMor = getvslmPort().vslmCloneVStorageObjectTask(getVsoManager(), ivd.getId(), cloneSpec);
            result = waitForTask(taskMor, cloneReport);
        } else {

            taskMor = getVimPort().cloneVStorageObjectTask(getVsoManager(), ivd.getId(), datastoreInfo.getMoref(),
                    cloneSpec);
            if (getVimConnection().waitForTask(taskMor, cloneReport)) {
                result = true;
            }
        }
        return result;
    }

    VslmConnection connectVslm() throws ConnectException {
        final VersionManipulator apiVersion = new VersionManipulator(this.vimConnection.getAboutInfo().getApiVersion());
        // api version greater than 6.7.1
        if ((apiVersion.toInteger() >= (MIN_API_VERSION)) && this.vimConnection.isConnectToVslm()
                && (this.url != null)) {
            this.vslmService = new VslmService();
            updateVslmHeaderHandlerResolver();
            this.vslmPort = this.vslmService.getVslmPort();

            final Map<String, Object> pbmCtxt = ((BindingProvider) this.vslmPort).getRequestContext();
            pbmCtxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
            pbmCtxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getURL().toString());

            try {
                this.serviceContent = this.vslmPort.retrieveContent(getServiceInstanceReference());
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
                throw new ConnectException(String.format("failed to connect to %s not valid", getURL().toString()), e);
            }
            this.aboutInfo = this.serviceContent.getAboutInfo();
            this.instanceUuid = this.aboutInfo.getInstanceUuid();

            this.vslmVsoManager = this.serviceContent.getVStorageObjectManager();
            this.useVslm = true;
        } else {
            this.useVslm = false;
        }
        this.vimVsoManager = this.vimConnection.getServiceContent().getVStorageObjectManager();

        return this;
    }

    private List<VStorageObjectAssociations> convertVslmVsoVStorageObjectAssociations2VStorageObjectAssociations(
            final List<VslmVsoVStorageObjectAssociations> sourceList) {
        final LinkedList<VStorageObjectAssociations> result = new LinkedList<>();

        for (final VslmVsoVStorageObjectAssociations source : sourceList) {
            final VStorageObjectAssociations a = new VStorageObjectAssociations();
            a.setId(source.getId());

            for (final VslmVsoVStorageObjectAssociationsVmDiskAssociation diskAssociation : source
                    .getVmDiskAssociation()) {
                final VStorageObjectAssociationsVmDiskAssociations convertedDiskAssosiation = new VStorageObjectAssociationsVmDiskAssociations();
                convertedDiskAssosiation.setDiskKey(diskAssociation.getDiskKey());
                convertedDiskAssosiation.setVmId(diskAssociation.getVmId());
                a.getVmDiskAssociations().add(convertedDiskAssosiation);
            }
            result.add(a);
        }
        return result;
    }

    private VStorageObject createDiskTask(final VslmCreateSpec spec) throws FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            RuntimeFaultFaultMsg, VslmFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
            com.vmware.vim25.FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        VStorageObject result = null;

        if (isVslm()) {
            final ManagedObjectReference taskMor = getvslmPort().vslmCreateDiskTask(getVsoManager(), spec);
            if (waitForTask(taskMor)) {
                final VslmTaskInfo taskResult = this.vslmPort.vslmQueryInfo(taskMor);
                result = (VStorageObject) taskResult.getResult();
            }
        } else {
            final ManagedObjectReference taskMor = getVimConnection().getVimPort().createDiskTask(getVsoManager(),
                    spec);
            if (getVimConnection().waitForTask(taskMor)) {
                result = (VStorageObject) getVimConnection().getVimHelper().entityProps(taskMor, "info.result");
            }
        }

        return result;

    }

    public VStorageObject createIvd(final ManagedEntityInfo datastore, final String name,
            final FileBackingInfoProvisioningType backingType, final long size,
            final VirtualMachineDefinedProfileSpec spbmProfile)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, VslmTaskException,
            VimTaskException, InterruptedException, com.vmware.pbm.InvalidArgumentFaultMsg, PbmFaultFaultMsg,
            PbmNonExistentHubsFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {

        return createIvd(datastore, name, null, backingType, size, spbmProfile);
    }

    public VStorageObject createIvd(final ManagedEntityInfo datastore, final String name, final String folder,
            final FileBackingInfoProvisioningType backingType, final long size,
            final VirtualMachineDefinedProfileSpec spbmProfile)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, VslmTaskException,
            VimTaskException, InterruptedException, com.vmware.pbm.InvalidArgumentFaultMsg, PbmFaultFaultMsg,
            PbmNonExistentHubsFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
        VStorageObject result = null;

        if (datastore != null) {
            final Long sizeInMB = PrettyNumber.toMegaByte(size);
            final VslmCreateSpec spec = new VslmCreateSpec();
            final VslmCreateSpecDiskFileBackingSpec backingSpec = new VslmCreateSpecDiskFileBackingSpec();
            backingSpec.setDatastore(datastore.getMoref());
            backingSpec.setPath(folder);
            backingSpec.setProvisioningType(backingType.value());
            spec.setCapacityInMB(sizeInMB);
            spec.setName(name);
            spec.setKeepAfterDeleteVm(true);
            spec.setBackingSpec(backingSpec);
            VirtualMachineProfileSpec profile = (spbmProfile == null)
                    ? vimConnection.getPbmConnection().geeDefaultProfile(datastore)
                    : spbmProfile;

            spec.getProfile().add(profile);
            result = createDiskTask(spec);
        }

        return result;
    }

    /**
     * @param snapName
     * @return
     * @throws VslmFaultFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws com.vmware.vim25.FileFaultFaultMsg
     * @throws com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws com.vmware.vim25.InvalidStateFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws VslmTaskException
     * @throws VimTaskException
     * @throws InterruptedException
     */
    public boolean createSnapshot(final ImprovedVirtualDisk ivd, final String snapName,
            final RunningReport runningReport) throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        boolean result = false;
        if (isVslm()) {
            final ManagedObjectReference taskMor = getvslmPort().vslmCreateSnapshotTask(getVsoManager(), ivd.getId(),
                    snapName);
            result = waitForTask(taskMor, runningReport);
        } else {
            final ManagedObjectReference taskMor = getVimPort().vStorageObjectCreateSnapshotTask(getVsoManager(),
                    ivd.getId(), ivd.getDatastoreInfo().getMoref(), snapName);
            result = this.vimConnection.waitForTask(taskMor, runningReport);
        }

        if (result) {
            if (logger.isLoggable(Level.INFO)) {
                final String msg = String.format("Snapshot name %s created succesfully.", snapName);
                logger.info(msg);
            }
        } else {
            final String msg = String.format("Snapshot creation name %s failed.", snapName);
            logger.warning(msg);
        }
        return result;
    }

    public boolean deleteSnapshot(final ImprovedVirtualDisk ivd,
            final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap)
            throws com.vmware.vim25.FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.InvalidStateFaultMsg, com.vmware.vim25.NotFoundFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {

        return deleteSnapshot(ivd, snap, null);
    }

    /**
     * @param improvedVirtuaDisk
     * @param snap
     * @return
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.InvalidStateFaultMsg
     * @throws com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws com.vmware.vim25.FileFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws VslmTaskException
     * @throws VimTaskException
     * @throws InterruptedException
     */
    public boolean deleteSnapshot(final ImprovedVirtualDisk ivd,
            final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap, final RunningReport runningReport)
            throws com.vmware.vim25.FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.InvalidStateFaultMsg, com.vmware.vim25.NotFoundFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {

        boolean result = false;
        if (isVslm()) {
            final ManagedObjectReference taskMor = getvslmPort().vslmDeleteSnapshotTask(getVsoManager(), ivd.getId(),
                    snap.getId());
            result = waitForTask(taskMor, runningReport);
        } else {
            final ManagedObjectReference taskMor = this.vimConnection.getVimPort().deleteSnapshotTask(getVsoManager(),
                    ivd.getId(), ivd.getDatastoreInfo().getMoref(), snap.getId());
            result = this.vimConnection.waitForTask(taskMor, runningReport);
        }
        if (logger.isLoggable(Level.INFO)) {
            final String msg = String.format("IVD:%s Snapshot %s:%s removed", ivd.toString(), snap.getId(),
                    snap.getDescription());
            logger.info(msg);
        }
        return result;

    }

    public boolean destroy(final ImprovedVirtualDisk ivd) throws FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, com.vmware.vslm.TaskInProgressFaultMsg,
            VslmFaultFaultMsg, VslmTaskException, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, TaskInProgressFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        boolean result = false;
        if (isVslm()) {
            final ManagedObjectReference taskMor = getvslmPort().vslmDeleteVStorageObjectTask(getVsoManager(),
                    ivd.getId());
            result = waitForTask(taskMor);
        } else {
            final ManagedObjectReference taskMor = this.vimConnection.getVimPort()
                    .deleteVStorageObjectTask(getVsoManager(), ivd.getId(), ivd.getDatastoreInfo().getMoref());
            result = this.vimConnection.waitForTask(taskMor);
        }
        if (logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Ivd:%s removed", ivd.toString());
            logger.info(msg);
        }
        return result;
    }

    /**
     * Detach a tag
     *
     * @param id
     * @param category
     * @param tag
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VslmFaultFaultMsg
     */
    public void detachTagFromVStorageObject(final ID id, final String category, final String tag)
            throws com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        if (isVslm()) {
            getvslmPort().vslmDetachTagFromVStorageObject(getVsoManager(), id, category, tag);
        } else {
            this.vimConnection.getVimPort().detachTagFromVStorageObject(getVsoManager(), id, category, tag);
        }
    }

    void disconnect() {
        try {
            if (isConnected() && this.useVslmLogout) {
                try {
                    logger.fine("disconnecting VSLM...");
                    this.vslmPort.vslmLogout(this.serviceContent.getSessionManager());

                    logger.fine("disconnected.");
                } catch (final RuntimeFaultFaultMsg e) {
                    Utility.logWarning(logger, e);
                }
            }
        } finally {
            this.serviceContent = null;
            this.vslmPort = null;
            this.vslmService = null;
        }
    }

    /**
     * Extend disk
     *
     * @param ivd             disk to extend
     * @param newCapacityInMb New disk size in MB
     * @return
     * @throws com.vmware.vim25.FileFaultFaultMsg
     * @throws com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws com.vmware.vim25.InvalidStateFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws FileFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.TaskInProgressFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws VslmTaskException
     * @throws VimTaskException
     * @throws InterruptedException
     */
    public boolean extendDisk(final ImprovedVirtualDisk ivd, final long newCapacityInMb)
            throws com.vmware.vim25.FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.InvalidStateFaultMsg, com.vmware.vim25.NotFoundFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            NotFoundFaultMsg, RuntimeFaultFaultMsg, com.vmware.vslm.TaskInProgressFaultMsg, VslmFaultFaultMsg,
            VslmTaskException, VimTaskException, InterruptedException {
        boolean result = false;

        ManagedObjectReference taskMor;
        if (isVslm()) {
            taskMor = getvslmPort().vslmExtendDiskTask(getVsoManager(), ivd.getId(), newCapacityInMb);
            result = waitForTask(taskMor);
        } else {
            taskMor = getVimPort().extendDiskTask(getVsoManager(), ivd.getId(), ivd.getDatastoreInfo().getMoref(),
                    newCapacityInMb);
            result = this.vimConnection.waitForTask(taskMor);
        }
        if (logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Ivd:%s extended to %dMB", ivd.toString(), newCapacityInMb);
            logger.info(msg);
        }
        return result;
    }

    public VslmAboutInfo getAboutInfo() {
        return this.aboutInfo;
    }

    public String getCookie() {
        return this.vimConnection.getCookie();
    }

    public String getCookieValue() {
        return this.vimConnection.getCookieValue();

    }

    public List<VslmInfrastructureObjectPolicy> getDiskAssociatedProfile(final ImprovedVirtualDisk ivd)
            throws InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
        List<VslmInfrastructureObjectPolicy> result = null;
        if (isVslm()) {
            result = this.vslmPort.vslmRetrieveVStorageInfrastructureObjectPolicy(getVsoManager(),
                    ivd.getDatastoreInfo().getMoref());
        } else {
            result = getVimPort().retrieveVStorageInfrastructureObjectPolicy(getVsoManager(),
                    ivd.getDatastoreInfo().getMoref());
        }
        return result;
    }

    public String getHost() {
        return this.url.getHost();
    }

    private LinkedList<ID> getIdList(final Map<String, ManagedObjectReference> datastoreMap)
            throws RuntimeFaultFaultMsg {
        final LinkedList<ID> result = new LinkedList<>();

        List<VslmVsoVStorageObjectQuerySpec> query = new LinkedList<>();
        VslmVsoVStorageObjectQuerySpec q = null;
        if ((datastoreMap != null) && (datastoreMap.size() > 0)) {
            q = new VslmVsoVStorageObjectQuerySpec();
            q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.DATASTORE_MO_ID.value());
            q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.EQUALS.value());
            for (final ManagedObjectReference moref : datastoreMap.values()) {
                q.getQueryValue().add(moref.getValue());
            }
            query.add(q);
        } else {
            /* workaround for vSPhere 6.7U3 */
            q = new VslmVsoVStorageObjectQuerySpec();
            q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.CREATE_TIME.value());
            q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.GREATER_THAN.value());

            q.getQueryValue().add("0");
            query.add(q);
        }
        VslmVsoVStorageObjectQueryResult res = getvslmPort().vslmListVStorageObjectForSpec(getVsoManager(), query,
                MAX_RESULT);

        result.addAll(res.getId());
        while (!res.isAllRecordsReturned()) {
            query = new LinkedList<>();
            final VslmVsoVStorageObjectQuerySpec retry = new VslmVsoVStorageObjectQuerySpec();
            retry.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.ID.value());
            retry.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.GREATER_THAN.value());
            retry.getQueryValue().add(res.getId().get(res.getId().size() - 1).getId());
            query.add(retry);
            query.add(q);
            res = getvslmPort().vslmListVStorageObjectForSpec(getVsoManager(), query, MAX_RESULT);
            result.addAll(res.getId());
        }

        return result;
    }

    ImprovedVirtualDisk getIvdById(final ID id) {
        try {
            final VStorageObject vStorageObject = getvslmPort().vslmRetrieveVStorageObject(getVsoManager(), id);
            final ManagedObjectReference dsMor = vStorageObject.getConfig().getBacking().getDatastore();
            final String dsName = getVimConnection().getVimHelper().entityName(dsMor);
            final ManagedEntityInfo dsInfo = new ManagedEntityInfo(dsName, dsMor, getServerIntanceUuid());

            return new ImprovedVirtualDisk(this, id, vStorageObject, dsInfo);

        } catch (final InvalidPropertyFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg | FileFaultFaultMsg
                | InvalidDatastoreFaultMsg | NotFoundFaultMsg | RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

        return null;
    }

    ImprovedVirtualDisk getIvdById(final ID id, final Map<String, ManagedObjectReference> datastoreMap) {
        ImprovedVirtualDisk result = null;
        for (final Entry<String, ManagedObjectReference> entry : datastoreMap.entrySet()) {
            final String key = entry.getKey();
            final ManagedObjectReference dsMor = entry.getValue();
            final ManagedEntityInfo dsInfo = new ManagedEntityInfo(key, dsMor, getServerIntanceUuid());
            try {
                if (logger.isLoggable(Level.FINE)) {
                    final String msg = String.format("retrieving IVD on datastore %s (%s) ", key, dsMor.getValue());
                    logger.fine(msg);
                }
                final VStorageObject vStorageObject = getVimPort().retrieveVStorageObject(getVsoManager(), id, dsMor);

                result = new ImprovedVirtualDisk(this, id, vStorageObject, dsInfo);

            } catch (com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.NotFoundFaultMsg
                    | com.vmware.vim25.RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            } catch (final com.vmware.vim25.InvalidDatastoreFaultMsg e) {
                final String msg = String.format("Datastore %s (%s) doesn't support Improved Virtual Disk", key,
                        dsMor.getValue());
                logger.warning(msg);
                Utility.logWarning(logger, e);
            }
        }
        return result;
    }

    ImprovedVirtualDisk getIvdById(final String uuid) {
        ImprovedVirtualDisk result = null;
        try {
            if (isVslm()) {
                final ID id = new ID();
                id.setId(uuid);
                result = getIvdById(id);
            } else {

                final List<ImprovedVirtualDisk> allIvd = getIvdList();
                for (final ImprovedVirtualDisk ivd : allIvd) {
                    if (ivd.getUuid().equalsIgnoreCase(uuid)) {
                        result = ivd;
                        break;
                    }
                }

            }
        } catch (final com.vmware.vim25.RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;
    }

    List<ImprovedVirtualDisk> getIvdByName(final String name) {
        final List<ImprovedVirtualDisk> result = new LinkedList<>();
        try {
            if (isVslm()) {
                final List<VslmVsoVStorageObjectQuerySpec> query = new LinkedList<>();
                final VslmVsoVStorageObjectQuerySpec q = new VslmVsoVStorageObjectQuerySpec();
                q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.NAME.value());
                q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.EQUALS.value());
                q.getQueryValue().add(name);
                query.add(q);
                final VslmVsoVStorageObjectQueryResult res = getvslmPort()
                        .vslmListVStorageObjectForSpec(getVsoManager(), query, 10);

                for (final VslmVsoVStorageObjectResult queryItem : res.getQueryResults()) {
                    final ImprovedVirtualDisk ivd = getIvdById(queryItem.getId());
                    if (ivd != null) {
                        result.add(ivd);
                    }
                }
            } else {
                final List<ImprovedVirtualDisk> allIvd = getIvdList();
                for (final ImprovedVirtualDisk ivd : allIvd) {
                    if (ivd.getName().equalsIgnoreCase(name)) {
                        result.add(ivd);
                    }
                }
            }
        } catch (final RuntimeFaultFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;

    }

    List<ImprovedVirtualDisk> getIvdList()
            throws com.vmware.vim25.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final Map<String, ManagedObjectReference> datastoreMap = (isVslm()) ? null
                : getVimConnection().getDatastoreList();
        return getIvdList(datastoreMap);
    }

    private LinkedList<ImprovedVirtualDisk> getIvdList(final Map<String, ManagedObjectReference> datastoreMap) {
        final LinkedList<ImprovedVirtualDisk> result = new LinkedList<>();
        if (isVslm()) {
            try {
                final LinkedList<ID> idList = getIdList(datastoreMap);
                for (final ID id : idList) {
                    result.add(getIvdById(id));
                }

            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        } else {
            List<ID> ivdList = null;
            for (final Entry<String, ManagedObjectReference> entry : datastoreMap.entrySet()) {
                final ManagedEntityInfo dsInfo = new ManagedEntityInfo(entry.getKey(), entry.getValue(),
                        getServerIntanceUuid());
                try {
                    ivdList = null;
                    if (logger.isLoggable(Level.FINE)) {
                        final String msg = String.format("retrieving IVD on datastore %s (%s) ", entry.getKey(),
                                entry.getValue().getValue());
                        logger.fine(msg);
                    }
                    ivdList = getVimPort().listVStorageObject(getVsoManager(), entry.getValue());
                } catch (final com.vmware.vim25.InvalidDatastoreFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg e) {
                    final String msg = String.format("Datastore %s (%s) doesn't support Improved Virtual Disk", entry,
                            entry.getValue().getValue());
                    logger.warning(msg);
                    Utility.logWarning(logger, e);
                }
                if (ivdList != null) {
                    for (final ID ivdID : ivdList) {
                        try {
                            final VStorageObject vStorageObject = getVimPort().retrieveVStorageObject(getVsoManager(),
                                    ivdID, entry.getValue());
                            final ImprovedVirtualDisk ivd = new ImprovedVirtualDisk(this, ivdID, vStorageObject, dsInfo);
                            result.add(ivd);
                        } catch (final com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg
                                | com.vmware.vim25.NotFoundFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg e) {
                            final String msg = String.format(
                                    "Improved Virtual Disk %s (%s) has some issue to be retrieved", ivdID,
                                    entry.getValue().getValue());
                            logger.warning(msg);
                            Utility.logWarning(logger, e);
                            final ImprovedVirtualDisk ivd = new ImprovedVirtualDisk(this, ivdID, null, dsInfo);
                            result.add(ivd);
                        }
                    }
                }

            }
        }
        return result;
    }

    public VStorageObjectSnapshotInfo getIvdSnapInfo(final ImprovedVirtualDisk ivd)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
        VStorageObjectSnapshotInfo result = null;
        if (isVslm()) {
            result = getvslmPort().vslmRetrieveSnapshotInfo(getVsoManager(), ivd.getId());
        } else {
            result = getVimPort().retrieveSnapshotInfo(getVsoManager(), ivd.getId(), ivd.getDatastoreInfo().getMoref());
        }
        return result;
    }

    public Integer getPort() {
        return this.url.getPort();
    }

    public String getServerIntanceUuid() {
        return this.instanceUuid;
    }

    public ManagedObjectReference getServiceInstanceReference() {
        if (this.svcInstRef == null) {
            final ManagedObjectReference ref = new ManagedObjectReference();
            ref.setType(VSLMSERVICEINSTANCETYPE);
            ref.setValue(VSLMSERVICEINSTANCEVALUE);
            this.svcInstRef = ref;
        }
        return this.svcInstRef;
    }

    public URL getURL() {
        return this.url;
    }

    public VimConnection getVimConnection() {
        return this.vimConnection;
    }

    /**
     * @return
     */
    private VimPortType getVimPort() {
        return this.vimConnection.getVimPort();
    }

    /**
     * @return
     */
    public VslmPortType getvslmPort() {

        return this.vslmPort;
    }

    /**
     * @return
     */
    public ManagedObjectReference getVsoManager() {
        if (this.useVslm) {
            return this.vslmVsoManager;
        } else {
            return this.vimVsoManager;
        }
    }

    public boolean isConnected() {
        return this.serviceContent != null;
    }

    public boolean isIvdByNameExist(final String name) {
        if (isVslm()) {
            try {
                final List<VslmVsoVStorageObjectQuerySpec> query = new LinkedList<>();
                final VslmVsoVStorageObjectQuerySpec q = new VslmVsoVStorageObjectQuerySpec();
                q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.NAME.value());
                q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.EQUALS.value());
                q.getQueryValue().add(name);
                query.add(q);
                final VslmVsoVStorageObjectQueryResult res = getvslmPort()
                        .vslmListVStorageObjectForSpec(getVsoManager(), query, 10);
                return !res.getId().isEmpty();
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
                return false;
            }
        } else {
            final List<ImprovedVirtualDisk> ivdList = getIvdByName(name);
            return ivdList.isEmpty();
        }
    }

    public boolean isUseVslm() {
        return this.useVslm;
    }

    public boolean isUseVslmLogout() {
        return this.useVslmLogout;
    }

    private boolean isVslm() {
        return this.vslmPort != null;
    }

    void keepAlive() {
        if (logger.isLoggable(Level.INFO)) {
            final String msg = String.format("keep alive %s : Current Time %s", getServerIntanceUuid(), "");
            logger.info(msg);
        }
    }

    /**
     *
     * @param id
     * @return
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VslmFaultFaultMsg
     */
    public List<VslmTagEntry> listTagsAttachedToVStorageObject(final ID id) throws com.vmware.vim25.NotFoundFaultMsg,
            com.vmware.vim25.RuntimeFaultFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        if (isVslm()) {
            return getvslmPort().vslmListTagsAttachedToVStorageObject(getVsoManager(), id);
        } else {
            return this.vimConnection.getVimPort().listTagsAttachedToVStorageObject(getVsoManager(), id);
        }
    }

    /**
     *
     * @param category
     * @param tag
     * @return
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VslmFaultFaultMsg
     */
    public List<ID> listVStorageObjectsAttachedToTag(final String category, final String tag)
            throws com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        if (isVslm()) {
            return getvslmPort().vslmListVStorageObjectsAttachedToTag(getVsoManager(), category, tag);
        } else {
            return this.vimConnection.getVimPort().listVStorageObjectsAttachedToTag(getVsoManager(), category, tag);
        }
    }

    public VStorageObject promoteIvd(final ManagedEntityInfo dcInfo, final String path, final String name)
            throws com.vmware.vim25.AlreadyExistsFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, AlreadyExistsFaultMsg,
            FileFaultFaultMsg, InvalidDatastoreFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            VslmSyncFaultFaultMsg {
        VStorageObject result = null;

        final String urlDisk = getVimConnection().getDiskPathForVc(dcInfo, path);
        if (isVslm()) {
            result = getvslmPort().vslmRegisterDisk(getVsoManager(), urlDisk, name);
        } else {
            result = getVimPort().registerDisk(getVsoManager(), urlDisk, name);
        }
        return result;
    }

    public DiskChangeInfo queryChangedDiskAreas(final ImprovedVirtualDisk ivd, final long startOffset,
            final String changeId)
            throws FileFaultFaultMsg, InvalidArgumentFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
        return this.vslmPort.vslmQueryChangedDiskAreas(getVsoManager(), ivd.getId(), ivd.getCurrentSnapshot().getId(),
                startOffset, changeId);
    }

    boolean reconcileDatastore(final ManagedEntityInfo datastoreInfo) throws InvalidDatastoreFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException, VslmTaskException {
        boolean result = false;

        final ManagedObjectReference morDatastore = datastoreInfo.getMoref();
        if (morDatastore != null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Starting reconciliation for datastore:" + datastoreInfo.toString());
            }
            if (isVslm()) {
                final ManagedObjectReference taskMor = getvslmPort()
                        .vslmReconcileDatastoreInventoryTask(getVsoManager(), morDatastore);
                result = waitForTask(taskMor);

            } else {
                final ManagedObjectReference taskMor = getVimPort().reconcileDatastoreInventoryTask(getVsoManager(),
                        morDatastore);
                result = getVimConnection().waitForTask(taskMor);

            }
        }

        return result;
    }

    public boolean renameIvd(final ImprovedVirtualDisk ivd, final String name) throws FileFaultFaultMsg,
            InvalidDatastoreFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg, VslmSyncFaultFaultMsg,
            com.vmware.vim25.FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
        if (isVslm()) {
            getvslmPort().vslmRenameVStorageObject(getVsoManager(), ivd.getId(), name);
        } else {
            getVimPort().renameVStorageObject(getVsoManager(), ivd.getId(), ivd.getDatastoreInfo().getMoref(), name);

        }
        return true;
    }

    public VStorageObjectSnapshotDetails retrieveSnapshotDetails(final ImprovedVirtualDisk ivd)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
            com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
        final VStorageObjectSnapshotInfoVStorageObjectSnapshot snapshot = ivd.getCurrentSnapshot();
        if (snapshot != null) {
            return this.vslmPort.vslmRetrieveSnapshotDetails(getVsoManager(), ivd.getId(), snapshot.getId());
        }
        return null;
    }

    List<VStorageObjectAssociations> retrieveVStorageObjectAssociations()
            throws com.vmware.vim25.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final Map<String, ManagedObjectReference> datastoreMap = new HashMap<>();
        if (!isVslm()) {
            datastoreMap.putAll(getVimConnection().getDatastoreList());
        }
        return retrieveVStorageObjectAssociations(datastoreMap);
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociations(final ImprovedVirtualDisk ivd)
            throws RuntimeFaultFaultMsg {
        List<VStorageObjectAssociations> result = new LinkedList<>();

        if (isVslm()) {
            final LinkedList<ID> ivdList = new LinkedList<>();
            ivdList.add(ivd.getId());
            result = retrieveVStorageObjectAssociationsById(ivdList);
        } else {
            final RetrieveVStorageObjSpec retrieveVStorageObjSpec = new RetrieveVStorageObjSpec();
            retrieveVStorageObjSpec.setDatastore(ivd.getDatastoreInfo().getMoref());
            retrieveVStorageObjSpec.setId(ivd.getId());

            final VStorageObjectConfigInfo config = ivd.getConfigInfo();
            if (config != null) {
                final ArrayList<RetrieveVStorageObjSpec> listRetrieve = new ArrayList<>();
                listRetrieve.add(retrieveVStorageObjSpec);
                result.addAll(retrieveVStorageObjectAssociationsByVStorageObjSpec(listRetrieve));
            }
        }
        return result;
    }

    private List<VStorageObjectAssociations> retrieveVStorageObjectAssociations(
            final Map<String, ManagedObjectReference> datastoreMap) {
        final List<VStorageObjectAssociations> result = new LinkedList<>();
        List<ID> idList = null;
        if (isVslm()) {
            try {
                idList = getIdList(datastoreMap);
                result.addAll(retrieveVStorageObjectAssociationsById(idList));
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        } else {

            try {

                final LinkedList<RetrieveVStorageObjSpec> list = new LinkedList<>();
                for (final Entry<String, ManagedObjectReference> entry : datastoreMap.entrySet()) {
                    final ManagedObjectReference dsMor = entry.getValue();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(String.format("retrieving IVD on datastore %s (%s) ", entry.getKey(),
                                dsMor.getValue()));
                    }
                    idList = getVimPort().listVStorageObject(getVsoManager(), dsMor);
                    for (final ID ivdID : idList) {
                        final RetrieveVStorageObjSpec objSpec = new RetrieveVStorageObjSpec();
                        objSpec.setDatastore(dsMor);
                        objSpec.setId(ivdID);
                        list.add(objSpec);
                    }
                }
                result.addAll(retrieveVStorageObjectAssociationsByVStorageObjSpec(list));
            } catch (final com.vmware.vim25.InvalidDatastoreFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
        return result;
    }

    private List<VStorageObjectAssociations> retrieveVStorageObjectAssociationsById(final List<ID> idList)
            throws RuntimeFaultFaultMsg {
        final List<VStorageObjectAssociations> result = new LinkedList<>();
        if (!idList.isEmpty()) {
            final List<VslmVsoVStorageObjectAssociations> idl = getvslmPort()
                    .vslmRetrieveVStorageObjectAssociations(getVsoManager(), idList);
            result.addAll(convertVslmVsoVStorageObjectAssociations2VStorageObjectAssociations(idl));
        }
        return result;
    }

    private List<VStorageObjectAssociations> retrieveVStorageObjectAssociationsByVStorageObjSpec(
            final List<RetrieveVStorageObjSpec> listObjSpec) {
        List<VStorageObjectAssociations> result = null;
        try {
            result = getVimPort().retrieveVStorageObjectAssociations(getVsoManager(), listObjSpec);
        } catch (final com.vmware.vim25.RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
        }

        return result;
    }

    public void setUseVslmLogout(final boolean useVslmLogout) {
        this.useVslmLogout = useVslmLogout;
    }

    public void setVStorageObjectControlFlags(final ImprovedVirtualDisk ivd, final boolean on,
            final List<String> controlFlagList)
            throws com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        if (isVslm()) {
            if (on) {
                getvslmPort().vslmSetVStorageObjectControlFlags(getVsoManager(), ivd.getId(), controlFlagList);
            } else {
                getvslmPort().vslmClearVStorageObjectControlFlags(getVsoManager(), ivd.getId(), controlFlagList);
            }
        } else {
            if (on) {
                getVimPort().setVStorageObjectControlFlags(getVsoManager(), ivd.getId(),
                        ivd.getDatastoreInfo().getMoref(), controlFlagList);
            } else {
                getVimPort().clearVStorageObjectControlFlags(getVsoManager(), ivd.getId(),
                        ivd.getDatastoreInfo().getMoref(), controlFlagList);
            }
        }
    }

    @Override
    public String toString() {
        if (isVslm()) {
            return String.format("%s url: %s", getAboutInfo().getFullName(), getURL().toString());
        } else {
            return "No VSLM connection";
        }
    }

    public HeaderHandlerResolver updateVslmHeaderHandlerResolver() {
        HeaderHandlerResolver result = null;
        if (this.vslmService != null) {
            result = new HeaderHandlerResolver();
            result.addHandler(new VcSessionHandler(getCookieValue()));
            this.vslmService.setHandlerResolver(result);
        }
        return result;
    }

    /**
     *
     * @param task
     * @return
     * @throws VslmTaskException
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws Exception
     */
    public boolean waitForTask(final ManagedObjectReference task)
            throws VslmTaskException, RuntimeFaultFaultMsg, InterruptedException {
        boolean result = false;
        boolean done = false;
        while (!done) {
            final VslmTaskInfo taskResult = this.vslmPort.vslmQueryInfo(task);
            taskResult.getProgress();
            switch (taskResult.getState()) {
            case ERROR:
                throw new VslmTaskException(taskResult);

            case QUEUED:
                break;
            case RUNNING:
                break;
            case SUCCESS:
                done = true;
                result = true;
                break;
            }
            Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);
        }
        return result;
    }

    public boolean waitForTask(final ManagedObjectReference task, final RunningReport runRep)
            throws VslmTaskException, RuntimeFaultFaultMsg, InterruptedException {
        boolean result = false;
        if (runRep != null) {

            boolean done = false;
            runRep.progressPercent = 0;
            final Thread t = new Thread(runRep);
            t.start();
            while (!done) {
                final VslmTaskInfo taskResult = this.vslmPort.vslmQueryInfo(task);
                if (taskResult.getProgress() != null) {
                    runRep.progressPercent = taskResult.getProgress();
                }
                switch (taskResult.getState()) {
                case ERROR:
                    throw new VslmTaskException(taskResult);
                case QUEUED:
                    break;
                case RUNNING:
                    break;
                case SUCCESS:
                    done = true;
                    result = true;
                    break;
                }
                Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);
            }
            if (t.isAlive()) {
                t.interrupt();
            }
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
        } else {
            result = waitForTask(task);
        }
        return result;
    }
}
