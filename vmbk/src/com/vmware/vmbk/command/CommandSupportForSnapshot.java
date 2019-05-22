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
package com.vmware.vmbk.command;

import java.util.Calendar;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.SnapshotManager;
import com.vmware.vmbk.type.VirtualMachineManager;

abstract class CommandSupportForSnapshot extends CommandWithOptions {

    protected VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtuaDisk ivd,
	    final Calendar cal) {
	final String snapName = generateSnapshotName(cal);
	return createSnapshot(ivd, snapName);
    }

    protected VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtuaDisk ivd,
	    final String snapName) {
	if (ivd.createSnapshot(snapName)) {
	    IoFunction.showInfo(logger, "Snapshot name %s created succesfully.", snapName);

	} else {
	    IoFunction.showInfo(logger, "Snapshot creation task failed.");
	    return null;
	}
	return ivd.getCurrentSnapshot();
    }

    protected SnapshotManager createSnapshot(final VirtualMachineManager vmm, final Calendar cal) {
	final String snapName = generateSnapshotName(cal);
	return createSnapshot(vmm, snapName);
    }

    protected SnapshotManager createSnapshot(final VirtualMachineManager vmm, final String snapName) {

	final ManagedObjectReference snapMoref = vmm.createSnapshot(snapName);
	if (snapMoref == null) {
	    IoFunction.showInfo(logger, "Snapshot creation task failed.");
	    return null;
	} else {
	    IoFunction.showInfo(logger, "Snapshot name %s (%s) created succesfully.", snapName, snapMoref.getValue());
	}

	final SnapshotManager snap = vmm.getCurrentSnapshot();
	if (snap == null) {
	    return null;
	}
	if (snapName.equals(snap.getName())) {
	    return snap;
	} else {
	    return null;
	}

    }

    protected String generateSnapshotName(final Calendar cal) {
	String snapName = null;
	snapName = String.format("VMBK_%d-%02d-%02d_%02d:%02d:%02d", cal.get(Calendar.YEAR),
		cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
		cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	return snapName;
    }

}
