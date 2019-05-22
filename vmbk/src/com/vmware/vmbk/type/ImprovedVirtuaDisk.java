/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.type;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmProfileId;
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
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vim25.VStorageObjectSnapshotDetails;
import com.vmware.vim25.VStorageObjectSnapshotInfo;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VslmTagEntry;
import com.vmware.vim25.VslmVStorageObjectControlFlag;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.soap.IConnection;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.soap.VslmConnection;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.util.Utility;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public class ImprovedVirtuaDisk implements FirstClassObject {

    private static final Logger logger = Logger.getLogger(ImprovedVirtuaDisk.class.getName());

    public static String headerToString() {
	return new ImprovedVirtuaDisk().headertoString();
    }

    private VStorageObjectConfigInfo configInfo;
    private ManagedEntityInfo dataCenterInfo;

    private final ManagedEntityInfo dsInfo;

    private final ID ivdID;

    private final ManagedObjectReference moRef;

    private final String serverUuid;

    private final String uuid;

    private final VimConnection vimConnection;
    private ManagedObjectReference vsoManager;
    private VslmConnection vslmConnection;

    private ImprovedVirtuaDisk() {
	this.ivdID = null;
	this.uuid = null;
	this.serverUuid = null;
	this.configInfo = null;
	this.vimConnection = null;
	this.dsInfo = null;
	this.moRef = null;
    }

    public ImprovedVirtuaDisk(final VimConnection connection, final ID ivdID, final VStorageObject vStorageObject,
	    final ManagedEntityInfo dsInfo) {
	this.ivdID = ivdID;
	this.uuid = ivdID.getId();
	this.serverUuid = connection.getServerIntanceUuid();
	this.moRef = MorefUtil.create(EntityType.ImprovedVirtualDisk, this.uuid.substring(24));
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

    public ImprovedVirtuaDisk(final VimConnection connection, final ManagedFcoEntityInfo vmInfo) {
	this.ivdID = new ID();
	this.ivdID.setId(vmInfo.getUuid());
	this.uuid = this.ivdID.getId();
	this.serverUuid = vmInfo.getServerUuid();
	this.configInfo = new VStorageObjectConfigInfo();
	this.configInfo.setName(vmInfo.getName());

	this.vimConnection = connection;
	if (connection != null) {

	    this.vslmConnection = connection.getVslmConnection();
	    this.vsoManager = this.vslmConnection.getVsoManager();
	} else {
	    logger.warning("ImprovedVirtuaDisk " + vmInfo.toString() + "is invalid - skipped");
	}
	this.moRef = vmInfo.getMoref();
	this.dsInfo = null;
    }

    /**
     * @param vslmConnection
     * @param id
     * @param vStorageObject
     * @param dsInfo2
     */
    public ImprovedVirtuaDisk(final VslmConnection vslmConnection, final ID id, final VStorageObject vStorageObject,
	    final ManagedEntityInfo dsInfo) {
	this(vslmConnection.getVimConnection(), id, vStorageObject, dsInfo);
	this.vslmConnection = vslmConnection;
    }

    public boolean AttachTag(final FcoTag tag) {
	logger.entering(getClass().getName(), "AttachTag", tag);
	boolean result = true;
	if (this.configInfo != null) {
	    try {
		this.vimConnection.getVimPort().attachTagToVStorageObject(this.vsoManager, getId(), tag.category,
			tag.tag);
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
		result = false;
	    }
	}
	logger.exiting(getClass().getName(), "AttachTag", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public boolean ClearControlFlags(final List<String> tags) {
// 	logger.entering(getClass().getName(), "ClearControlTags", tags);
// 	final boolean result = false;
// 	if (this.configInfo != null) {
// 	    try {
// 		vimConnection.getVimPort().clearVStorageObjectControlFlags(vsomanager, getId(),
// 			getDatastore().getMoref(), tags);
// 	    } catch (final Exception e) {
// 		logger.warning(Utility.toString(e));
// 	    }
// 	}
// 	logger.exiting(getClass().getName(), "ClearControlTags", result);
// 	return result;
//     }

// TODO Remove unused code found by UCDetector
//     public boolean Clone(final VslmCloneSpec cloneSpec) {
// 	logger.entering(getClass().getName(), "Clone", cloneSpec);
// 	boolean result = false;
// 	if (this.configInfo != null) {
// 	    try {
// 		final ManagedObjectReference taskMor = vimConnection.getVimPort().cloneVStorageObjectTask(
// 			vsomanager, getId(), getDatastore().getMoref(), cloneSpec);
// 		if (vimConnection.waitForTask(taskMor)) {
// 		    result = true;
// 		} else {
// 		    result = false;
// 		}
// 	    } catch (final Exception e) {
// 		logger.warning(Utility.toString(e));
// 	    }
// 	}
// 	logger.exiting(getClass().getName(), "Clone", result);
// 	return result;
//     }

    public boolean clone(final String cloneName, final String datastoreName) {
	logger.entering(getClass().getName(), "cloneVM", new Object[] { cloneName, datastoreName });
	final boolean result = this.vslmConnection.clone(this, cloneName, datastoreName);
	logger.exiting(getClass().getName(), "Clone", result);
	return result;
    }

    public boolean createSnapshot(final String snapName) {
	logger.entering(getClass().getName(), "createSnapshot", snapName);
	boolean result = false;
	try {
	    result = this.vimConnection.getVslmConnection().createSnapshot(this, snapName);

	} catch (InvalidCollectorVersionFaultMsg | InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | FileFaultFaultMsg
		| InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg | com.vmware.vslm.FileFaultFaultMsg
		| com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.InvalidStateFaultMsg
		| com.vmware.vslm.NotFoundFaultMsg | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "createSnapshot", result);
	return result;

    }

    public boolean deleteSnapshot(final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap) {
	logger.entering(getClass().getName(), "deleteSnapshot", snap);
	boolean result = false;
	try {
	    result = this.vimConnection.getVslmConnection().deleteSnapshot(this, snap);
//	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().deleteSnapshotTask(this.vsoManager,
//		    getId(), this.dsInfo.getMoref(), snap.getId());
//	    result = this.vimConnection.waitForTask(taskMor);

	} catch (RuntimeFaultFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
		| NotFoundFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
		| com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
		| com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
		| com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "deleteSnapshot", result);
	return result;
    }

    public boolean destroy() {
	ManagedObjectReference taskMor;
	try {
	    taskMor = this.vimConnection.getVimPort().deleteVStorageObjectTask(this.vsoManager, getId(),
		    getDatastoreInfo().getMoref());

	    if (this.vimConnection.waitForTask(taskMor)) {
		return true;
	    } else {
		return false;
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	return false;
    }

    public boolean detachTag(final FcoTag tag) {
	logger.entering(getClass().getName(), "detachTag", tag);
	final boolean result = false;
	if (this.configInfo != null) {
	    try {
		this.vimConnection.getVimPort().detachTagFromVStorageObject(this.vsoManager, getId(), tag.category,
			tag.tag);
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	}
	logger.exiting(getClass().getName(), "detachTag", result);
	return result;
    }

    public boolean extendDisk(final long newCapacity) {
	logger.entering(getClass().getName(), "extendDisk", newCapacity);
	boolean result = false;
	if (this.configInfo != null) {
	    try {
		final long newCapacityInMb = PrettyNumber.toMegaByte(newCapacity);
		final ManagedObjectReference taskMor = this.vimConnection.getVimPort().extendDiskTask(this.vsoManager,
			getId(), getDatastoreInfo().getMoref(), newCapacityInMb);
		if (this.vimConnection.waitForTask(taskMor)) {
		    result = true;
		} else {
		    result = false;
		}
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	}
	logger.exiting(getClass().getName(), "extendDisk", result);
	return result;
    }

    public List<PbmProfileId> getAssociatedProfile() {
	logger.entering(getClass().getName(), "getAssociatedProfile");

	List<PbmProfileId> result = null;
	try {
	    result = this.vimConnection.getPbmConnection().getAssociatedProfile(this);
	} catch (PbmFaultFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "getAssociatedProfile", result);
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
     */
    public String getChangeId() {
	logger.entering(getClass().getName(), "getChangeId");
	final VStorageObjectSnapshotDetails detail = retrieveSnapshotDetails();
	String result = null;
	if (detail != null) {
	    result = detail.getChangedBlockTrackingId();
	}
	logger.exiting(getClass().getName(), "getChangeId", result);
	return result;
    }

    public VStorageObjectConfigInfo getConfigInfo() {
	return this.configInfo;
    }

    @Override
    public IConnection getConnection() {
	return this.vimConnection;
    }

    public VStorageObjectSnapshotInfoVStorageObjectSnapshot getCurrentSnapshot() {
	try {
	    final VStorageObjectSnapshotInfo snapInfo = this.vslmConnection.getIvdSnapInfo(this);
	    if ((snapInfo != null) && (snapInfo.getSnapshots().size() > 0)) {
		return snapInfo.getSnapshots().get(0);
	    }
	    return null;
	} catch (RuntimeFaultFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
		| NotFoundFaultMsg | com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
		| com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
		| com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}

	return null;

    }

    @Override
    public ManagedEntityInfo getDatacenterInfo() {
	logger.entering(getClass().getName(), "getDatacenterInfo");
	if (this.dataCenterInfo == null) {
	    this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.dsInfo.getMoref());
	}
	logger.exiting(getClass().getName(), "getDatacenterInfo", this.dataCenterInfo);
	return this.dataCenterInfo;
    }

    public ManagedEntityInfo getDatastoreInfo() {
	return this.dsInfo;

    }

    @Override
    public EntityType getEntityType() {
	return EntityType.ImprovedVirtualDisk;
    }

    // TODO Remove unused code found by UCDetector
//     public String toString(final boolean moref) {
// 	if (!moref) {
// 	    String name = null;
// 	    if (getName().length() < 30) {
// 		name = getName();
// 	    } else {
// 		name = getName().substring(0, 27) + "...";
// 	    }
// 	    return String.format("%-8s%36s\t%-8s\t%-30s ", getEntityType().toString(true), getUuid(), "", name);
// 	} else {
// 	    return toString();
// 	}
//     }
    @Override
    public ManagedFcoEntityInfo getFcoInfo() {

	return new ManagedFcoEntityInfo(getName(), getMoref(), getUuid(), this.vimConnection.getServerIntanceUuid());
    }

    @Override
    public ArrayList<Block> getFullDiskAreas(final int diskId) {
	final int bytePerSector = 512;

	final long capacityInBytes = this.configInfo.getCapacityInMB() * 1024 * 1024;
	final ArrayList<Block> vixBlocks = new ArrayList<>();
	final Block block = new Block();
	block.length = capacityInBytes / bytePerSector;
	block.offset = 0;
	vixBlocks.add(block);
	return vixBlocks;
    }

    public ID getId() {
	return this.ivdID;
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
	    logger.warning(Utility.toString(e));
	}
	return null;

    }

    @Override
    public FirstClassObjectType getType() {
	return FirstClassObjectType.ivd;
    }

    @Override
    public String getUuid() {
	return this.uuid;
    }

    public String getVcenterInstanceUuid() {
	return this.serverUuid;
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

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.FirstClassObject#isChangeTrackingEnabled()
     */

    @Override
    public String headertoString() {
	return String.format("%-8s%-36s\t%-30s", "entity", "uuid", "name");
    }

    @Override
    public boolean isChangedBlockTrackingEnabled() {
	logger.entering(getClass().getName(), "isChangedBlockTrackingEnabled");
	boolean result = false;
	if (this.configInfo != null) {
	    result = this.configInfo.isChangedBlockTrackingEnabled();
	}
	logger.exiting(getClass().getName(), "isChangedBlockTrackingEnabled", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public boolean revertToSnapshot(final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap) {
// 	logger.entering(getClass().getName(), "revertSnapshot", snap);
// 	boolean result = false;
// 	try {
// 	    final ManagedObjectReference taskMor = vimConnection.getVimPort().revertVStorageObjectTask(
// 		    vsomanager, getId(), this.dsInfo.getMoref(), snap.getId());
// 	    result = this.vimConnection.waitForTask(taskMor);
//
// 	} catch (RuntimeFaultFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
// 		| NotFoundFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg e) {
// 	    logger.warning(Utility.toString(e));
// 	}
// 	logger.exiting(getClass().getName(), "revertSnapshot", result);
// 	return result;
//     }

    @Override
    public boolean isVmDatastoreNfs() {
	try {
	    final DatastoreSummary summary = (DatastoreSummary) this.vimConnection.morefHelper
		    .entityProps(this.dsInfo.getMoref(), "summary");

	    if (summary.getType().equalsIgnoreCase("nfs")) {
		return true;
	    }
	} catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	return false;
    }

    public List<VslmTagEntry> listTags() {
	logger.entering(getClass().getName(), "listTags");
	List<VslmTagEntry> result = null;
	if (this.configInfo != null) {
	    try {
		result = this.vimConnection.getVimPort().listTagsAttachedToVStorageObject(this.vsoManager, getId());

	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	}
	logger.exiting(getClass().getName(), "listTags", result);
	return result;
    }

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final FcoArchiveManager vmArcMgr, final int diskId,
	    final BackupMode mode) {
	final int bytePerSector = 512;
	try {
	    final GenerationProfile profGen = vmArcMgr.getTargetGeneration();

	    final long capacityInBytes = this.configInfo.getCapacityInMB() * 1024 * 1024;
	    DiskChangeInfo diskChangeInfo = null;
	    final ArrayList<Block> vixBlocks = new ArrayList<>();

	    long startPosition = 0;
	    String prevChanges = "";
	    if (mode == BackupMode.full) {
		prevChanges = "*";
	    } else {
		prevChanges = vmArcMgr.getPrevChangeId(diskId);
		if (prevChanges == null) {
		    prevChanges = "*";
		}
	    }
	    do {
		diskChangeInfo = this.getVslmConnection().queryChangedDiskAreas(this, startPosition, prevChanges);

		for (final DiskChangeExtent changedArea : diskChangeInfo.getChangedArea()) {
		    final Block block = new Block();
		    block.length = changedArea.getLength() / bytePerSector;
		    block.offset = changedArea.getStart() / bytePerSector;
		    vixBlocks.add(block);
		}
		startPosition = diskChangeInfo.getStartOffset() + diskChangeInfo.getLength();
	    } while (startPosition < capacityInBytes);
	    profGen.setIsChanged(diskId, diskChangeInfo.getLength() != 0);
	    return vixBlocks;

	} catch (com.vmware.vslm.FileFaultFaultMsg | InvalidArgumentFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
		| com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
		| com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}

    }

    public boolean rename(final String name) {
	logger.entering(getClass().getName(), "rename", name);
	boolean result = true;
	if (this.configInfo != null) {
	    try {
		this.vimConnection.getVimPort().renameVStorageObject(this.vsoManager, getId(),
			getDatastoreInfo().getMoref(), name);
		update();
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
		result = false;
	    }
	}
	logger.exiting(getClass().getName(), "rename", result);

	return result;
    }

    /**
     *
     * @return
     */
    public VStorageObjectSnapshotDetails retrieveSnapshotDetails() {
	logger.entering(getClass().getName(), "retrieveSnapshotDetails");
	VStorageObjectSnapshotDetails result = null;
	try {
	    result = getVslmConnection().retrieveSnapshotDetails(this);
	} catch (com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
		| com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
		| com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "retrieveSnapshotDetails", result);
	return result;
    }

    public List<VStorageObjectAssociations> retrieveStorageAssociations() {
	logger.entering(getClass().getName(), "retrieveStorageAssociations");
	final List<VStorageObjectAssociations> result = getVslmConnection().retrieveVStorageObjectAssociations(this);
	logger.exiting(getClass().getName(), "retrieveStorageAssociations", result);
	return result;
    }

    @Override
    public boolean setChangeBlockTracking(final boolean on) {
	logger.entering(getClass().getName(), "setChangeBlockTracking", on);
	boolean result = false;
	try {
	    final ArrayList<String> controlFlagList = new ArrayList<>();
	    controlFlagList.add(VslmVStorageObjectControlFlag.ENABLE_CHANGED_BLOCK_TRACKING.value());
	    this.vslmConnection.setVStorageObjectControlFlags(this, on, controlFlagList);
	    result = true;

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "setChangeBlockTracking", result);
	return result;
    }

    public boolean setDisableRelocation(final boolean on) {
	logger.entering(getClass().getName(), "setDisableRelocation", on);
	boolean result = false;
	try {

	    final ArrayList<String> controlFlagList = new ArrayList<>();
	    controlFlagList.add(VslmVStorageObjectControlFlag.DISABLE_RELOCATION.value());
	    this.vslmConnection.setVStorageObjectControlFlags(this, on, controlFlagList);

	    result = true;

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "setDisableRelocation", result);
	return result;
    }

    public boolean setKeepAfterDeleteVm(final boolean on) {
	logger.entering(getClass().getName(), "setKeepAfterDeleteVm", on);
	boolean result = false;
	try {

	    final ArrayList<String> controlFlagList = new ArrayList<>();
	    controlFlagList.add(VslmVStorageObjectControlFlag.KEEP_AFTER_DELETE_VM.value());
	    this.vslmConnection.setVStorageObjectControlFlags(this, on, controlFlagList);

	    result = true;

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "setKeepAfterDeleteVm", result);
	return result;

    }

    @Override
    public String toString() {
	String name = null;
	if (this.configInfo != null) {
	    if (getName().length() < 30) {
		name = getName();
	    } else {
		name = getName().substring(0, 27) + "...";
	    }
	} else {
	    name = "CORRUPTED - Remove this Improved Virtual Disk";
	}
	return String.format("%-8s%36s\t%-30s ", getEntityType().toString(true), getUuid(), name);
    }

    private void update() {
	try {
	    final VStorageObject vStorageObject = this.vimConnection.getVimPort()
		    .retrieveVStorageObject(this.vsoManager, this.ivdID, this.dsInfo.getMoref());
	    this.configInfo = vStorageObject.getConfig();
	} catch (final Exception e) {
	    LoggerUtils.logInfo(logger, "Improved Virtual Disk %s (%s) has some issue to be retrieved", this.ivdID,
		    this.dsInfo.toString());

	}
    }
}
