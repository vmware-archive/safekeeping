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

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.ArachneConnection;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.K8sIvdComponent;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VslmTagEntry;

public class K8s implements IFirstClassObject {

    /**
     * 
     */
    private static final long serialVersionUID = -29930773667860517L;

    private static final Logger logger = Logger.getLogger(K8s.class.getName());

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

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#destroy()
     */
    @Override
    public boolean destroy() throws RuntimeFaultFaultMsg, VimFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, TaskInProgressFaultMsg {
        // TODO Auto-generated method stub
        return false;
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
        final int bytePerSector = jDiskLibConst.SECTOR_SIZE;

        final long capacityInBytes = 0;
        // this.configInfo.getCapacityInMB() * 1024 * 1024;
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
    public ManagedFcoEntityInfo getParentFco() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InterruptedException, com.vmware.vslm.RuntimeFaultFaultMsg {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getPowerState()
     */
    @Override
    public FcoPowerState getPowerState() {
        // TODO Auto-generated method stub
        return FcoPowerState.suspended;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.FirstClassObject#isChangeTrackingEnabled()
     */

    @Override
    public String getServerUuid() {
        return "";
    }

    public List<K8sIvdComponent> getSnapshotInfo(final String snapShotUuid)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return this.arachneManager.getSnapshotInfo(this.name, snapShotUuid);
    }

    @Override
    public FirstClassObjectType getType() {
        return FirstClassObjectType.k8s;
    }

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
        final boolean result = false;
//	if (this.configInfo != null) {
//	    result = this.configInfo.isChangedBlockTrackingEnabled();
//	}
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
        return result;
    }

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final GenerationProfile profile, final Integer diskId,
            final BackupMode backupMode) {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public VimConnection getVimConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEncrypted() {
        // TODO Auto-generated method stub
        return false;
    }

}
