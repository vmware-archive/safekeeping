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
package com.vmware.safekeeping.core.command;

import java.util.Calendar;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VirtualMachineGuestQuiesceSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vslm.InvalidDatastoreFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public interface ICommandSupportForSnapshot {

    /**
     *
     * @param ivd
     * @param cal
     * @return
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws VslmTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     */
    default VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtualDisk ivd,
            final Calendar cal)
            throws InterruptedException, com.vmware.vslm.FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException {
        final String snapName = Utility.generateSnapshotName(cal);
        return createSnapshot(ivd, snapName);
    }

    /**
     *
     * @param ivd
     * @param cal
     * @param snapReport
     * @return
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws VslmTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     */
    default VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtualDisk ivd,
            final Calendar cal, final RunningReport snapReport)
            throws InterruptedException, com.vmware.vslm.FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException {
        final String snapName = Utility.generateSnapshotName(cal);
        return createSnapshot(ivd, snapName, snapReport);
    }

    default VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtualDisk ivd,
            final String snapName)
            throws InterruptedException, com.vmware.vslm.FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException {
        ivd.createSnapshot(snapName);
        return ivd.getCurrentSnapshot();
    }

    default VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtualDisk ivd,
            final String snapName, final RunningReport snapReport)
            throws InterruptedException, com.vmware.vslm.FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VslmTaskException, VimTaskException {
        ivd.createSnapshot(snapName, snapReport);
        return ivd.getCurrentSnapshot();
    }

    /**
     * @param vmm
     * @param cal
     * @param snapReport
     * @return
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws SnapshotFaultFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws FileFaultFaultMsg
     */
    default SnapshotManager createSnapshot(final VirtualMachineManager vmm, final Calendar cal,
            final RunningReport snapReport) throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        final String snapName = Utility.generateSnapshotName(cal);
        return createSnapshot(vmm, snapName, snapReport);
    }

    /**
     *
     * @param vmm
     * @param snapName
     * @param snapReport
     * @return
     * @throws FileFaultFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws SnapshotFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimTaskException
     * @throws InterruptedException
     */
    default SnapshotManager createSnapshot(final VirtualMachineManager vmm, final String snapName,
            final RunningReport snapReport) throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        final ManagedObjectReference snapMoref = vmm.createSnapshot(snapName, snapReport);

        final SnapshotManager snap = vmm.getCurrentSnapshot();
        if (snap == null) {
            return null;
        }
        if (MorefUtil.compare(snapMoref, snap.getMoref())) {
            return snap;
        } else {
            return null;
        }

    }

    /**
     *
     * @param vmm
     * @param snapName
     * @param guestQuisceSpec
     * @return
     * @throws FileFaultFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws SnapshotFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimTaskException
     * @throws InterruptedException
     * @throws VimOperationException
     */
    default SnapshotManager createSnapshot(final VirtualMachineManager vmm, final String snapName,
            final VirtualMachineGuestQuiesceSpec guestQuisceSpec)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException, VimOperationException {
        final ManagedObjectReference snapMoref = vmm.createSnapshot(snapName, guestQuisceSpec);

        final SnapshotManager snap = vmm.getCurrentSnapshot();
        if (snap == null || !MorefUtil.compare(snapMoref, snap.getMoref())) {
            throw new VimOperationException("Snapshot:%s creation failed - Check the server log for more information",
                    snapName);
        }
        return snap;
    }

}
