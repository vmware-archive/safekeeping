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

import java.util.logging.Logger;

import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.util.Utility;

public class SnapshotManager {

    private static final Logger logger = Logger.getLogger(SnapshotManager.class.getName());
    public final static int VSS_BT_COPY = 5;
    public final static int VSS_BT_DIFFERENTIAL = 3;
    final static int VSS_BT_FULL = 1;
    public final static int VSS_BT_INCREMENTAL = 2;
    public final static int VSS_BT_LOG = 4;
    public final static int VSS_BT_OTHER = 6;
    public final static int VSS_BT_UNDEFINED = 0;

    private VirtualMachineConfigManager configMgr;
    private final VimConnection conn;
    private final ManagedObjectReference currentSnapshotMor;
    private String name;
    private VirtualMachineSnapshotTree snapshotTree;
    private final VirtualMachineManager vmm;
    private final VirtualMachineSnapshotInfo vmSnapshotInfo;

    SnapshotManager(final VimConnection conn, final VirtualMachineManager vm,
	    final VirtualMachineSnapshotInfo vmSnapshotInfo) {

	assert vmSnapshotInfo != null;
	this.conn = conn;
	this.vmm = vm;
	this.currentSnapshotMor = vmSnapshotInfo.getCurrentSnapshot();
	this.vmSnapshotInfo = vmSnapshotInfo;
	if (this.currentSnapshotMor != null) {

	    this.snapshotTree = vm.searchSnapshotTreeWithMoref(vmSnapshotInfo.getRootSnapshotList(),
		    this.currentSnapshotMor);
	    this.name = this.snapshotTree.getName();

	    this.configMgr = new VirtualMachineConfigManager(this.conn, this.vmm.getMoref(),
		    getVirtualMachineConfigInfoFromSnapshot(this.currentSnapshotMor));
	}
	this.vmm.setSnapshotManager(this);
    }

    public VirtualMachineConfigManager getConfig() {
	return this.configMgr;
    }

    public ManagedObjectReference getMoref() {
	return this.currentSnapshotMor;
    }

    public String getName() {

	return this.name;
    }

    public ManagedEntityInfo getSnapInfo() {
	return new ManagedEntityInfo(getName(), getMoref(), this.conn.getServerIntanceUuid());
    }

    public VirtualMachineManager getVirtualMachine() {
	return this.vmm;
    }

    private VirtualMachineConfigInfo getVirtualMachineConfigInfoFromSnapshot(final ManagedObjectReference snapMor) {
	VirtualMachineConfigInfo configInfo = null;
	try {
	    configInfo = (VirtualMachineConfigInfo) this.conn.morefHelper.entityProps(snapMor, "config");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}

	return configInfo;

    }

    public VirtualMachineSnapshotInfo getVirtualMachineSnapshotInfo() {
	return this.vmSnapshotInfo;
    }

    public ManagedEntityInfo getVmInfo() {
	return this.vmm.getFcoInfo();
    }

}
