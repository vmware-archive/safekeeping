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
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VslmTagEntry;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.soap.ArachneConnection;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.soap.IConnection;

public class K8s implements FirstClassObject {

    private static final Logger logger = Logger.getLogger(K8s.class.getName());

    public static String headerToString() {
	return new K8s().headertoString();
    }

    private final String uuid;
    private final String name;

    private ConnectionManager connetionManager;
    private ArachneConnection arachneManager;

    private K8s() {
	this.uuid = null;
	this.name = null;
    }

    public K8s(final ConnectionManager connetionManager, final ManagedFcoEntityInfo fcoInfo) {
	this.uuid = fcoInfo.getUuid();// this.ivdID.getId();

	this.connetionManager = connetionManager;
	this.arachneManager = this.connetionManager.getArachneConnection();
	this.name = fcoInfo.getName();
    }

    public K8s(final ConnectionManager connetionManager, final String name) {
	this.uuid = null;
	this.connetionManager = connetionManager;
	this.arachneManager = this.connetionManager.getArachneConnection();
	this.name = name;
    }

    public K8s(final ConnectionManager connetionManager, final String uuid, final VStorageObject vStorageObject,
	    final ManagedEntityInfo dsInfo) {
	this.uuid = uuid;
	this.connetionManager = connetionManager;
	this.arachneManager = this.connetionManager.getArachneConnection();
	this.name = null;
    }

    public String createSnapshot() {
	return this.arachneManager.createSnapshot(this.name);
    }

    @Override
    public IConnection getConnection() {
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getDatacenterInfo()
     */
    @Override
    public ManagedEntityInfo getDatacenterInfo() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public EntityType getEntityType() {
	return EntityType.K8sNamespace;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getFcoInfo()
     */
    @Override
    public ManagedFcoEntityInfo getFcoInfo() {
	// return new ManagedFcoEntityInfo(getName(), null, getUuid(), null);
	return null;
    }

    @Override
    public ArrayList<Block> getFullDiskAreas(final int diskId) {
	final int bytePerSector = 512;

	final long capacityInBytes = 0;// this.configInfo.getCapacityInMB() * 1024 * 1024;
	final ArrayList<Block> vixBlocks = new ArrayList<>();
	final Block block = new Block();
	block.length = capacityInBytes / bytePerSector;
	block.offset = 0;
	vixBlocks.add(block);
	return vixBlocks;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getMoref()
     */
    @Override
    public ManagedObjectReference getMoref() {

	return null;
    }

    @Override
    public String getName() {
	return this.name;
    }

    @Override
    public String getServerUuid() {
	return "";
    }

    public List<K8sIvdComponent> getSnapshotInfo(final String snapShotUuid) {
	return this.arachneManager.getSnapshotInfo(this.name, snapShotUuid);
    }

    @Override
    public FirstClassObjectType getType() {
	return FirstClassObjectType.k8s;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.FirstClassObject#isChangeTrackingEnabled()
     */

    @Override
    public String getUuid() {
	return this.uuid;
    }

    @Override
    public String headertoString() {
	return String.format("%-8s%-36s\t%-30s", "entity", "uuid", "name");
    }

    @Override
    public boolean isChangedBlockTrackingEnabled() {
	logger.entering(getClass().getName(), "isChangedBlockTrackingEnabled");
	final boolean result = false;
//	if (this.configInfo != null) {
//	    result = this.configInfo.isChangedBlockTrackingEnabled();
//	}
	logger.exiting(getClass().getName(), "isChangedBlockTrackingEnabled", result);
	return result;
    }

    @Override
    public boolean isVmDatastoreNfs() {
//	try {
//	    final DatastoreSummary summary = (DatastoreSummary) getConnection().morefHelper
//		    .entityProps(this.dsInfo.getMoref(), "summary");
//
//	    if (summary.getType().equalsIgnoreCase("nfs")) {
//		return true;
//	    }
//	} catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
//	    logger.warning(Utility.toString(e));
//
//	}
	return false;
    }

    public List<VslmTagEntry> listTags() {
	logger.entering(getClass().getName(), "listTags");
	final List<VslmTagEntry> result = null;
//	if (this.configInfo != null) {
//	    try {
//		result = getConnection().getVimPort().listTagsAttachedToVStorageObject(getConnection().getVsoManager(),
//			getId());
//
//	    } catch (final Exception e) {
//		logger.warning(Utility.toString(e));
//	    }
//	}
	logger.exiting(getClass().getName(), "listTags", result);
	return result;
    }

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final FcoArchiveManager vmArcMgr, final int diskId,
	    final BackupMode mode) {
	return getFullDiskAreas(diskId);

    }

    @Override
    public boolean setChangeBlockTracking(final boolean enable) {
//	try {
//
//	    final ArrayList<String> controlFlagList = new ArrayList<>();
//	    controlFlagList.add(VslmVStorageObjectControlFlag.ENABLE_CHANGED_BLOCK_TRACKING.toString());
//	    if (enable) {
//		getConnection().getVimPort().setVStorageObjectControlFlags(getConnection().getVsoManager(), getId(),
//			getDatastore().getMoref(), controlFlagList);
//	    } else {
//		getConnection().getVimPort().clearVStorageObjectControlFlags(getConnection().getVsoManager(), getId(),
//			getDatastore().getMoref(), controlFlagList);
//	    }
//	    return true;
//
//	} catch (final Exception e) {
//	    logger.warning(Utility.toString(e));
//	}
	return false;
    }

}
