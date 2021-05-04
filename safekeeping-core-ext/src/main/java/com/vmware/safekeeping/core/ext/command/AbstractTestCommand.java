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
package com.vmware.safekeeping.core.ext.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLib.ConnectParams;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.ICommandSupportForSnapshot;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.support.SnapshotInfo;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTest;
import com.vmware.safekeeping.core.ext.util.JvddkTest;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
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
import com.vmware.vim25.VirtualMachineGuestQuiesceSpec;
import com.vmware.vim25.VirtualMachineWindowsQuiesceSpec;
import com.vmware.vim25.VirtualMachineWindowsQuiesceSpecVssBackupContext;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public abstract class AbstractTestCommand extends AbstractCommandWithOptions implements ICommandSupportForSnapshot {

    protected AbstractTestCommand() {
    }

    protected List<CoreResultActionTest> actionFcoTest(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionTest> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        final JvddkTest test = new JvddkTest();
        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionTest resultAction = new CoreResultActionTest();
            resultAction.start();
            if (Vmbk.isAbortTriggered()) {
                resultAction.aborted("Aborted on user request");
                result.clear();
                result.add(resultAction);
                break;
            } else {
                try {
                    resultAction.setFcoEntityInfo(fco.getFcoInfo());

                    if (!getOptions().isDryRun() && fco instanceof VirtualMachineManager) {
                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                        final ConnectParams connectParams = vmm.getVimConnection().configureVddkAccess(vmm);

                        final List<VmdkInfo> disks = vmm.getConfig().getAllVmdkInfo();
                        final Calendar cal = Calendar.getInstance();

                        final String snapName = Utility.generateSnapshotName(cal);
                        final CoreResultActionCreateSnap resultActionCreateSnap = createVmSnap(vmm, snapName,
                                resultAction);
                        if (resultActionCreateSnap.isSuccessful()) {

                            for (final VmdkInfo disk : disks) {

                                if (test.testVmxAccess(connectParams, resultActionCreateSnap.getSnapMoref(),
                                        getOptions().getTransportMode(), disk.getRemoteDiskPath(), resultAction) != 0) {
                                    resultAction.failure();
                                    break;
                                }
                            }
                        }
                        if ((resultActionCreateSnap != null) && resultActionCreateSnap.isSuccessful()) {
                            deleteVmSnap(vmm, resultAction);
                        }

                    }

                } catch (final Exception e) {
                    Utility.logWarning(this.logger, e);
                    resultAction.failure(e);
                } finally {
                    resultAction.done();
                }
            }
            result.add(resultAction);

        }
        return result;
    }

    private CoreResultActionCreateSnap createVmSnap(final VirtualMachineManager vmm, final String snapName,
            final CoreResultActionTest rab) throws CoreResultActionException, VimOperationException {
        final CoreResultActionCreateSnap result = new CoreResultActionCreateSnap(rab);

        result.start();
        try {
            VirtualMachineGuestQuiesceSpec guestQuiesceSpec = null;
            if (vmm.isGuestWindows()) {
                final VirtualMachineWindowsQuiesceSpec guestWinQuiesceSpec = new VirtualMachineWindowsQuiesceSpec();
                guestWinQuiesceSpec.setVssPartialFileSupport(false);
                guestWinQuiesceSpec.setTimeout(CoreGlobalSettings.getWindowsVssTimeOut());
                guestWinQuiesceSpec.setVssBootableSystemState(true);
                guestWinQuiesceSpec
                        .setVssBackupContext(VirtualMachineWindowsQuiesceSpecVssBackupContext.CTX_BACKUP.toString());// "ctx_auto");VssBackupContext

                guestWinQuiesceSpec.setVssBackupType(SnapshotManager.VSS_BT_FULL);
                guestQuiesceSpec = guestWinQuiesceSpec;

            } else {
                guestQuiesceSpec = new VirtualMachineGuestQuiesceSpec();
                guestQuiesceSpec.setTimeout(CoreGlobalSettings.getQuisceTimeout());
            }

            final SnapshotManager snap = createSnapshot(vmm, snapName, guestQuiesceSpec);
            if (snap == null) {
                result.failure();
            } else {
                result.setSnapshotManager(snap);
                result.setSnapMoref(snap.getMoref().getValue());
                result.setSnapName(snap.getName());
                result.setSnapDescription(StringUtils.EMPTY);
            }
        } catch (final InterruptedException e) {
            result.failure(e);
            Utility.logWarning(this.logger, e);
            Thread.currentThread().interrupt();

        } catch (final InvalidStateFaultMsg | FileFaultFaultMsg | InvalidNameFaultMsg | RuntimeFaultFaultMsg
                | SnapshotFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg
                | InvalidCollectorVersionFaultMsg | VimTaskException e) {
            result.failure(e);
        } finally {
            result.done();
        }
        return result;

    }

    private CoreResultActionDeleteSnap deleteVmSnap(final VirtualMachineManager vmm, final CoreResultActionTest rab)
            throws CoreResultActionException {
        final CoreResultActionDeleteSnap result = new CoreResultActionDeleteSnap(rab);

        result.start();
        final SnapshotManager snap = vmm.getSnapshotManager();
        final ManagedObjectReference snapToDelete = snap.getMoref();

        final SnapshotInfo snapInfo = snap.getSnapshotInfo();
        result.getSnapList().add(snapInfo);
        try {

            if (vmm.deleteSnapshot(snapToDelete, false, true)) {
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Delete snapshot %s succeeded.", snap.getActiveSnapShotName());
                    this.logger.info(msg);
                }
            } else {
                final String msg = String.format("Delete snapshot %s failed.", snap.getActiveSnapShotName());
                this.logger.warning(msg);
                result.failure(msg);
            }
        } catch (final InterruptedException e) {
            result.failure(e);
            Utility.logWarning(this.logger, e);
            Thread.currentThread().interrupt();
        } catch (RuntimeFaultFaultMsg | TaskInProgressFaultMsg | InvalidPropertyFaultMsg | VimTaskException
                | InvalidCollectorVersionFaultMsg | CoreResultActionException e) {

            result.failure(e);
            Utility.logWarning(this.logger, e);

        } finally {
            result.done();
        }
        return result;

    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    @Override
    public TestOption getOptions() {
        return (TestOption) this.options;
    }

    @Override
    public final void initialize() {
        setOptions(new TestOption());
    }

}
