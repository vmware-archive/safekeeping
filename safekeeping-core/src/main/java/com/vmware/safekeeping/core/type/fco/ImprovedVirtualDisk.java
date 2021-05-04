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
package com.vmware.safekeeping.core.type.fco;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmProfileId;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.VslmConnection;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.FcoTag;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.location.CoreIvdLocation;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfo;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DiskChangeExtent;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.ID;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vim25.VStorageObjectSnapshotDetails;
import com.vmware.vim25.VStorageObjectSnapshotInfo;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VslmTagEntry;
import com.vmware.vim25.VslmVStorageObjectControlFlag;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;
import com.vmware.vslm.VslmSyncFaultFaultMsg;

public class ImprovedVirtualDisk implements IFirstClassObject {

    /**
     * 
     */
    private static final long serialVersionUID = -5954991656974369929L;

    public static class AttachedVirtualMachine {
        private VirtualMachineManager vmm;
        private int diskKey;

        public int getDiskKey() {
            return this.diskKey;
        }

        /**
         * @return
         */
        public ManagedFcoEntityInfo getFcoInfo() {
            if (this.vmm != null) {
                return this.vmm.getFcoInfo();
            } else {
                return null;
            }
        }

        public VirtualMachineManager getVmm() {
            return this.vmm;
        }

        public void setDiskKey(final int diskKey) {
            this.diskKey = diskKey;
        }

        public void setVmm(final VirtualMachineManager vmm) {
            this.vmm = vmm;
        }

    }

    private static final Logger logger = Logger.getLogger(ImprovedVirtualDisk.class.getName());

    public static ManagedObjectReference getMoref(final ID id) {
        return MorefUtil.newManagedObjectReference(EntityType.ImprovedVirtualDisk, "fcd:" + id.getId());
    }

    public static String sHeaderToString() {
        return String.format("%-8s%-36s\t%-30s", "entity", "uuid", "name");
    }

    private VStorageObjectConfigInfo configInfo;

    private ManagedEntityInfo dataCenterInfo;

    private ManagedEntityInfo dsInfo;

    private ID ivdID;

    private ManagedObjectReference moRef;

    private String serverUuid;
    private String uuid;
    private VimConnection vimConnection;

    private ManagedObjectReference vsoManager;

    private VslmConnection vslmConnection;

    public ImprovedVirtualDisk(final ConnectionManager connetionManager, final ManagedFcoEntityInfo fcoInfo) {
        this(connetionManager.getVimConnection(fcoInfo.getServerUuid()), fcoInfo);

    }

    @Override
    public boolean isEncrypted() {
        boolean result = false;
        if (getConfigInfo().getBacking() instanceof BaseConfigInfoDiskFileBackingInfo) {
            result = ((BaseConfigInfoDiskFileBackingInfo) getConfigInfo().getBacking()).getKeyId() != null;
        }
        return result;
    }

    /**
     * @param vmInfo
     */
    public ImprovedVirtualDisk(final ManagedFcoEntityInfo fcoInfo) {
        this.ivdID = new ID();
        this.ivdID.setId(fcoInfo.getUuid());
        this.uuid = this.ivdID.getId();
        this.serverUuid = fcoInfo.getServerUuid();
        this.configInfo = new VStorageObjectConfigInfo();
        this.configInfo.setName(fcoInfo.getName());

        this.moRef = fcoInfo.getMoref();
    }

    public ImprovedVirtualDisk(final VimConnection connection, final ID ivdID, final VStorageObject vStorageObject,
            final ManagedEntityInfo dsInfo) {
        this.ivdID = ivdID;
        this.uuid = ivdID.getId();
        this.serverUuid = connection.getServerIntanceUuid();
        this.moRef = getMoref(ivdID);
        if (vStorageObject != null) {
            this.configInfo = vStorageObject.getConfig();
        } else {
            this.configInfo = null;
        }
        this.vimConnection = connection;
        this.vslmConnection = connection.getVslmConnection();
        this.vsoManager = this.vslmConnection.getVsoManager();
        this.dsInfo = dsInfo;
    }

    public ImprovedVirtualDisk(final VimConnection connection, final ManagedFcoEntityInfo info) {
        this(info);
        this.vimConnection = connection;
        if (connection != null) {
            this.vslmConnection = connection.getVslmConnection();
            this.vsoManager = this.vslmConnection.getVsoManager();
        } else {
            final String msg = "ImprovedVirtuaDisk " + info.toString() + "is invalid - skipped";
            logger.warning(msg);
        }

    }

    public ImprovedVirtualDisk(final VimConnection connection, final ManagedFcoEntityInfo info,
            final ManagedEntityInfo dsInfo) {
        this(connection, info);
        this.dsInfo = dsInfo;
    }

    /**
     * @param vslmConnection
     * @param id
     * @param vStorageObject
     * @param dsInfo2
     */
    public ImprovedVirtualDisk(final VslmConnection vslmConnection, final ID id, final VStorageObject vStorageObject,
            final ManagedEntityInfo dsInfo) {
        this(vslmConnection.getVimConnection(), id, vStorageObject, dsInfo);
        this.vslmConnection = vslmConnection;
    }

    public boolean attachTag(final FcoTag tag) throws NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        final boolean result = true;
        if (this.configInfo != null) {

            getVslmConnection().attachTagToVStorageObject(getId(), tag.getCategory(), tag.getTag());
        }
        return result;
    }

    /**
     *
     * @param cloneName
     * @param cloneReport
     * @param datastoreName
     * @return
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws VslmTaskException
     * @throws VimTaskException
     * @throws InterruptedException
     */
    public boolean clone(final String cloneName, final ManagedEntityInfo datastoreInfo, final RunningReport cloneReport)
            throws InvalidPropertyFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg,
            FileFaultFaultMsg, InvalidDatastoreFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, VslmFaultFaultMsg, VslmTaskException, VimTaskException,
            InterruptedException {
        return this.vslmConnection.clone(this, cloneName, datastoreInfo, cloneReport);

    }

    public boolean createSnapshot(final String snapName)
            throws com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        return createSnapshot(snapName, null);
    }

    public boolean createSnapshot(final String snapName, final RunningReport snapReport)
            throws com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        return this.vimConnection.getVslmConnection().createSnapshot(this, snapName, snapReport);
    }

    public boolean deleteSnapshot(final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        return this.vimConnection.getVslmConnection().deleteSnapshot(this, snap);
    }

    public boolean deleteSnapshot(final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap,
            final RunningReport snapReport)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException, InterruptedException {
        return this.vimConnection.getVslmConnection().deleteSnapshot(this, snap, snapReport);
    }

    /**
     * Destroy this Ivd
     *
     * @return true if succeed
     * @throws com.vmware.vslm.TaskInProgressFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws com.vmware.vslm.TaskInProgressFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws VslmTaskException
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.TaskInProgressFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws FileFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimTaskException
     * @throws InterruptedException
     */
    @Override
    public boolean destroy() throws com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, com.vmware.vslm.TaskInProgressFaultMsg, VslmFaultFaultMsg,
            VslmTaskException, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException {
        return this.vimConnection.getVslmConnection().destroy(this);
    }

    public boolean detachTag(final FcoTag tag) throws NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        final boolean result = true;
        if (this.configInfo != null) {

            getVslmConnection().detachTagFromVStorageObject(getId(), tag.getCategory(), tag.getTag());
        }
        return result;
    }

    /**
     * Extend IVD
     *
     * @param newCapacity
     * @return
     * @throws FileFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.TaskInProgressFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws VslmTaskException
     * @throws VimTaskException
     * @throws InterruptedException
     */
    public boolean extendDisk(final long newCapacity)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
            com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, com.vmware.vslm.TaskInProgressFaultMsg, VslmFaultFaultMsg,
            VslmTaskException, VimTaskException, InterruptedException {
        boolean result = false;
        if (this.configInfo != null) {
            final long newCapacityInMb = PrettyNumber.toMegaByte(newCapacity);

            result = this.vimConnection.getVslmConnection().extendDisk(this, newCapacityInMb);

        }
        return result;
    }

    public List<PbmProfileId> getAssociatedProfile() {

        List<PbmProfileId> result = null;
        try {
            result = this.vimConnection.getPbmConnection().getAssociatedProfile(this);
        } catch (PbmFaultFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
        }
        return result;
    }

    public AttachedVirtualMachine getAttachedVirtualMachine() throws com.vmware.vslm.RuntimeFaultFaultMsg,
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final AttachedVirtualMachine result = new AttachedVirtualMachine();

        final List<VStorageObjectAssociations> listAssociation = retrieveStorageAssociations();
        if (!listAssociation.isEmpty()) {
            final VStorageObjectAssociations association = listAssociation.get(0);
            final boolean attached = !association.getVmDiskAssociations().isEmpty();
            if (attached) {
                final VStorageObjectAssociationsVmDiskAssociations vmDisk = association.getVmDiskAssociations().get(0);
                final ManagedObjectReference vmMor = MorefUtil.newManagedObjectReference(EntityType.VirtualMachine,
                        vmDisk.getVmId());
                result.setVmm(new VirtualMachineManager(getVimConnection(), vmMor));
                result.setDiskKey(vmDisk.getDiskKey());
            }
        }

        return result;
    }

    /**
     * @return
     */
    public String getBackingProvisionType() {
        String result = BaseConfigInfoDiskFileBackingInfoProvisioningType.THIN.value();
        if (getConfigInfo().getBacking() instanceof BaseConfigInfoDiskFileBackingInfo) {
            result = ((BaseConfigInfoDiskFileBackingInfo) getConfigInfo().getBacking()).getProvisioningType();
        }
        return result;
    }

    /**
     * @return
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     */
    public String getChangeId() throws com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        final VStorageObjectSnapshotDetails detail = retrieveSnapshotDetails();
        String result = null;
        if (detail != null) {
            result = detail.getChangedBlockTrackingId();
        }
        return result;
    }

    public VStorageObjectConfigInfo getConfigInfo() {
        return this.configInfo;
    }

    public VStorageObjectSnapshotInfoVStorageObjectSnapshot getCurrentSnapshot()
            throws com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {

        final VStorageObjectSnapshotInfo snapInfo = this.vslmConnection.getIvdSnapInfo(this);
        if ((snapInfo != null) && (!snapInfo.getSnapshots().isEmpty())) {
            return snapInfo.getSnapshots().get(0);
        }
        return null;

    }

    @Override
    public ManagedEntityInfo getDatacenterInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        if (this.dataCenterInfo == null) {
            this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.dsInfo.getMoref());
        }
        return this.dataCenterInfo;
    }

    public ManagedEntityInfo getDatastoreInfo() {
        return this.dsInfo;

    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ImprovedVirtualDisk;
    }

    @Override
    public ManagedFcoEntityInfo getFcoInfo() {
        return new ManagedFcoEntityInfo(getName(), getMoref(), getUuid(), getServerUuid());
    }

    @Override
    public ArrayList<Block> getFullDiskAreas(final int diskId) {
        final long capacityInBytes = this.configInfo.getCapacityInMB() * 1024 * 1024;
        final ArrayList<Block> vixBlocks = new ArrayList<>();
        final Block block = new Block();
        block.length = capacityInBytes / jDiskLibConst.SECTOR_SIZE;
        block.offset = 0;
        vixBlocks.add(block);
        return vixBlocks;
    }

    public ID getId() {
        return this.ivdID;
    }

    public CoreIvdLocation getLocations() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final CoreIvdLocation result = new CoreIvdLocation();
        result.setDatastoreInfo(getDatastoreInfo());
        result.setDatacenterInfo(getDatacenterInfo());
        if (this.configInfo.getBacking() instanceof BaseConfigInfoDiskFileBackingInfo) {
            final BaseConfigInfoDiskFileBackingInfo backing = (BaseConfigInfoDiskFileBackingInfo) this.configInfo
                    .getBacking();
            final String filePath = backing.getFilePath();
            result.setDatastorePath(StringUtils.substringBeforeLast(filePath, "/"));
            result.setVmdkFullPath(filePath);
            result.setVmdkFileName(StringUtils.substringAfterLast(filePath, "/"));
        }
        return result;

    }

    @Override
    public ManagedObjectReference getMoref() {
        return this.moRef;
    }

    @Override
    public String getName() {
        if (this.configInfo == null) {
            return "undefined";
        }
        return this.configInfo.getName();
    }

    @Override
    public ManagedFcoEntityInfo getParentFco() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InterruptedException, com.vmware.vslm.RuntimeFaultFaultMsg {
        final AttachedVirtualMachine atVM = getAttachedVirtualMachine();
        if (atVM.vmm != null) {
            return atVM.vmm.getFcoInfo();
        }

        return null;
    }

    /**
     * Get the power state of the attached VM if any otherwise return
     * FcoPowerState.PowerOff
     *
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     *
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     */
    @Override
    public FcoPowerState getPowerState() throws com.vmware.vslm.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, InterruptedException {
        FcoPowerState result = FcoPowerState.poweredOff;
        final AttachedVirtualMachine avm = getAttachedVirtualMachine();
        if ((avm != null) && (avm.getVmm() != null)) {
            result = avm.getVmm().getPowerState();
        }
        return result;
    }

    @Override
    public String getServerUuid() {
        return this.serverUuid;
    }

    public VStorageObjectSnapshotInfo getSnapshots() {
        try {
            return this.vslmConnection.getIvdSnapInfo(this);
        } catch (final FileFaultFaultMsg | com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | InvalidDatastoreFaultMsg
                | InvalidStateFaultMsg | NotFoundFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);

        }

        return null;

    }

    @Override
    public FirstClassObjectType getType() {
        return FirstClassObjectType.ivd;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.FirstClassObject#isChangeTrackingEnabled()
     */

    @Override
    public String getUuid() {
        if (StringUtils.isEmpty(this.uuid)) {
            return this.vimConnection.getServerIntanceUuid();
        }
        return this.uuid;
    }

    public String getVcenterInstanceUuid() {
        return this.serverUuid;
    }

    public VimConnection getVimConnection() {
        return this.vimConnection;
    }

    /**
     * @return
     */
    public VimPortType getVimPort() {
        return this.vimConnection.getVimPort();
    }

    /**
     * @return
     */
    public VslmConnection getVslmConnection() {
        return this.vslmConnection;
    }

    @Override
    public String headertoString() {
        return sHeaderToString();
    }

    public boolean isAttached() throws com.vmware.vslm.RuntimeFaultFaultMsg {
        boolean result = false;
        final List<VStorageObjectAssociations> associated = retrieveStorageAssociations();
        for (final VStorageObjectAssociations asso : associated) {
            result = !asso.getVmDiskAssociations().isEmpty();
        }
        return result;
    }

    @Override
    public boolean isChangedBlockTrackingEnabled() {
        boolean result = false;
        if (this.configInfo != null) {
            result = this.configInfo.isChangedBlockTrackingEnabled();
        }
        return result;
    }

    @Override
    public boolean isVmDatastoreNfs() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final DatastoreSummary summary = (DatastoreSummary) this.vimConnection.getVimHelper().entityProps(this.dsInfo,
                "summary");
        return "nfs".equalsIgnoreCase(summary.getType());
    }

    public List<VslmTagEntry> listTags() throws NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg {
        return getVslmConnection().listTagsAttachedToVStorageObject(getId());
    }

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final GenerationProfile profile, final Integer diskId,
            final BackupMode backupMode) throws com.vmware.vslm.FileFaultFaultMsg, InvalidArgumentFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.InvalidStateFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {

        final ArrayList<Block> vixBlocks = new ArrayList<>();

        if (profile.isDiskChanged(diskId)) {
            String changeId = "";
            final DiskProfile diskProfile = profile.getDisks().get(diskId);
            if ((profile.getPreviousGeneration() == null) || (backupMode == BackupMode.FULL)) {
                changeId = "*";
            } else {
                final DiskProfile prevGenDiskProfile = profile.getPreviousGeneration().getDisks().get(diskId);
                changeId = prevGenDiskProfile.getChangeId();
            }
            DiskChangeInfo diskChangeInfo = null;
            long startPosition = 0;
            do {
                diskChangeInfo = getVslmConnection().queryChangedDiskAreas(this, startPosition, changeId);
                if (diskChangeInfo == null) {
                    break;
                }
                for (final DiskChangeExtent changedArea : diskChangeInfo.getChangedArea()) {
                    final Block block = new Block();
                    block.length = changedArea.getLength() / jDiskLibConst.SECTOR_SIZE;
                    block.offset = changedArea.getStart() / jDiskLibConst.SECTOR_SIZE;
                    vixBlocks.add(block);
                }
                startPosition = diskChangeInfo.getStartOffset() + diskChangeInfo.getLength();
            } while (startPosition < diskProfile.getCapacity());
        }
        return vixBlocks;

    }

    public boolean rename(final String name) throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, com.vmware.vslm.FileFaultFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            VslmSyncFaultFaultMsg {
        boolean result = false;
        if (this.configInfo != null) {
            this.vimConnection.getVslmConnection().renameIvd(this, name);
            update();
            result = true;
        }
        return result;
    }

    /**
     *
     * @return
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     */
    private VStorageObjectSnapshotDetails retrieveSnapshotDetails() throws com.vmware.vslm.FileFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.InvalidStateFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        return getVslmConnection().retrieveSnapshotDetails(this);

    }

    private List<VStorageObjectAssociations> retrieveStorageAssociations() throws com.vmware.vslm.RuntimeFaultFaultMsg {
        return getVslmConnection().retrieveVStorageObjectAssociations(this);
    }

    @Override
    public boolean setChangeBlockTracking(final boolean on)
            throws InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.InvalidStateFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg {

        final ArrayList<String> controlFlagList = new ArrayList<>();
        controlFlagList.add(VslmVStorageObjectControlFlag.ENABLE_CHANGED_BLOCK_TRACKING.value());
        this.vslmConnection.setVStorageObjectControlFlags(this, on, controlFlagList);

        return true;
    }

    public boolean setDisableRelocation(final boolean on)
            throws InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.InvalidStateFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg {

        final ArrayList<String> controlFlagList = new ArrayList<>();
        controlFlagList.add(VslmVStorageObjectControlFlag.DISABLE_RELOCATION.value());
        this.vslmConnection.setVStorageObjectControlFlags(this, on, controlFlagList);

        return true;
    }

    public boolean setKeepAfterDeleteVm(final boolean on)
            throws InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.InvalidStateFaultMsg,
            com.vmware.vslm.NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg {

        final ArrayList<String> controlFlagList = new ArrayList<>();
        controlFlagList.add(VslmVStorageObjectControlFlag.KEEP_AFTER_DELETE_VM.value());
        this.vslmConnection.setVStorageObjectControlFlags(this, on, controlFlagList);
        return true;

    }

    @Override
    public String toString() {
        return String.format("%-8s%36s\t%-30s ", getEntityType().toString(true), getUuid(), getShortedEntityName());
    }

    private void update() throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        final VStorageObject vStorageObject = this.vimConnection.getVimPort().retrieveVStorageObject(this.vsoManager,
                this.ivdID, this.dsInfo.getMoref());
        this.configInfo = vStorageObject.getConfig();

    }
}
