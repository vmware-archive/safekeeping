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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionGetVmConfig;
import com.vmware.safekeeping.core.command.results.CoreResultActionRevertToTemplate;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.support.SnapshotInfo;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimApplicationQuiesceFault;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.profile.ovf.SerializableVmConfigInfo;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.managers.PrivilegesList;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;
import com.vmware.safekeeping.core.type.manipulator.VmxManipulator;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VirtualMachineGuestQuiesceSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class BackupVM extends AbstractBackupFcoWithDisks {

    public BackupVM(final VimConnection vimConnection, final CoreBackupOptions options, final Logger logger) {
        super(vimConnection, options, logger);
    }

    public final CoreResultActionVmBackup backup(final VirtualMachineManager vmm, final FcoArchiveManager fcoArcMgr,
            final GlobalFcoProfileCatalog profAllVm, final CoreResultActionVmBackup rab)
            throws CoreResultActionException {
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupVm() start.");
        }
        if (!getOptions().isDryRun()) {
            try {
                if (infoCollection(rab) && createSnapshot(rab) && generationComputation(rab, fcoArcMgr)) {
                    final GenerationProfile profile = rab.getProfile();
                    for (int diskId = 0; diskId < profile.getNumberOfDisks(); diskId++) {
                        final CoreResultActionDiskBackup radb = new CoreResultActionDiskBackup(diskId, profile, rab);
                        checkCbtHealth(radb);
                    }
                    final Jvddk jvddk = new Jvddk(this.logger, vmm);
                    if (checkPrivileges(rab) && retrieveVmConfigFiles(rab)
                            && !rab.getCreateSnapshotAction().getVmdkInfoList().isEmpty() && discoveryBackupMode(rab)
                            && startVddkAccess(rab, jvddk) && disksBackup(rab, profAllVm, jvddk)
                            && endVddkAccess(rab, jvddk) && finalizeProfile(rab)) {
                        final String msg = rab.getFcoToString() + " Backup Success - start cleaning";
                        if (this.logger.isLoggable(Level.INFO)) {
                            this.logger.info(msg);
                        }
                    }

                }
            } catch (final JVixException | NoSuchAlgorithmException | VimPermissionException | IOException e) {
                Utility.logWarning(this.logger, e);
                rab.failure(e);
            } catch (final InterruptedException e) {
                rab.failure(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                logger.warning("----------------- Unexpected Error ---------------------");
                Utility.logWarning(this.logger, e);
                rab.failure();

            } finally {
                removeSnapshot(rab);
                revertToTemplate(rab);
            }
        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupVm() end.");
        }
        return rab;
    }

    private boolean createSnapshot(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        /**
         * Start Section CreateSnapshot
         */
        rab.getInteractive().startCreateSnapshot();
        boolean result = false;
        final CoreResultActionCreateSnap resultAction = new CoreResultActionCreateSnap(rab);
        rab.setCreateSnapshotAction(resultAction);
        VirtualMachineGuestQuiesceSpec guestQuiesceSpec = null;
        final String snapName = Utility.generateSnapshotName(rab.getStartDate());
        resultAction.setSnapName(snapName);
        final VirtualMachineManager vmm = rab.getFirstClassObject();
        resultAction.start();
        guestQuiesceSpec = vmm.isGuestWindows() ? getOptions().getQuisceSpec().toVirtualMachineWindowsQuiesceSpec()
                : getOptions().getQuisceSpec().toVirtualMachineGuestQuiesceSpec();
        try {
            boolean retryOnFail = getOptions().getQuisceSpec().isVssRetryOnFail();
            while (resultAction.isRunning()) {
                try {
                    if (doSnapshot(vmm, resultAction, guestQuiesceSpec)) {
                        result = true;
                        break;
                    }
                } catch (final InterruptedException e) {
                    resultAction.failure(e);
                    Utility.logWarning(this.logger, e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                } catch (VimApplicationQuiesceFault e) {
                    if (retryOnFail) {
                        retryOnFail = false;
                        guestQuiesceSpec = null;
                        Utility.logWarning(this.logger, e);
                    } else {
                        resultAction.failure(e);
                    }
                } catch (InvalidNameFaultMsg | SnapshotFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg
                        | FileFaultFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                        | InvalidCollectorVersionFaultMsg | VimTaskException e) {
                    Utility.logWarning(this.logger, e);
                } catch (final Exception e) {
                    Utility.logWarning(this.logger, e);
                    resultAction.failure(e);
                }
            }
        } finally {
            resultAction.done();
            rab.setNumberOfDisk(resultAction.getVmdkInfoList().size());
            /**
             * End Section CreateSnapshot
             */
            rab.getInteractive().endCreateSnapshot();
        }
        return result;
    }

    private boolean discoveryBackupMode(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        /**
         * Start Section DiscoverBackupMode
         */
        rab.getInteractive().startDiscoverBackupMode();

        final GenerationProfile profile = rab.getProfile();
        BackupMode backupMode = BackupMode.UNKNOW;
        for (final CoreResultActionDiskBackup radb : rab.getResultActionsOnDisk()) {

            final BackupMode lbackupMode = getDiskBackupMode(radb);
            if (backupMode == BackupMode.UNKNOW) {
                backupMode = lbackupMode;
            } else {
                if (backupMode != lbackupMode) {
                    backupMode = BackupMode.MIXED;
                    break;
                }
            }
        }

        rab.setBackupMode(backupMode);
        profile.setBackupMode(backupMode);
        rab.getGenerationInfo().setBackupMode(backupMode);
        rab.getInteractive().endDiscoverBackupMode();
        /**
         * End Section DiscoverBackupMode
         */
        return backupMode != BackupMode.UNKNOW;
    }

    private boolean disksBackup(final CoreResultActionVmBackup rab, final GlobalFcoProfileCatalog fcoProfileCatalog,
            final Jvddk jvddk) {
        boolean result = true;
        try {
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
            /**
             * Start Section DisksBackup
             */
            rab.getInteractive().startDisksBackup();
            final List<Future<CoreResultActionDiskBackup>> futures = new ArrayList<>();
            for (final CoreResultActionDiskBackup radb : rab.getResultActionsOnDisk()) {

                final Future<CoreResultActionDiskBackup> f = submit(radb, jvddk, fcoProfileCatalog, this.logger);
                futures.add(f);
            }
            // A) Await all runnable to be done (blocking)
            for (final Future<CoreResultActionDiskBackup> future : futures) {
                // get will block until the future is done
                final CoreResultActionDiskBackup radb = future.get();
                if (radb != null) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine(radb.toString());
                    }
                    result &= radb.isSuccessfulOrSkipped();
                } else {
                    result = false;
                }
            }

        } catch (final InterruptedException e) {
            rab.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            rab.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            rab.getInteractive().endDisksBackup();
            /**
             * End Section DisksBackup
             */
        }
        return result;
    }

    private boolean doSnapshot(final VirtualMachineManager vmm, final CoreResultActionCreateSnap resultAction,
            final VirtualMachineGuestQuiesceSpec guestQuiesceSpec)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException, VimOperationException {
        resultAction.newTentative();

        final SnapshotManager snap = createSnapshot(vmm, resultAction.getSnapName(), guestQuiesceSpec);
        resultAction.setSnapshotManager(snap);
        resultAction.setSnapMoref(snap.getMoref().getValue());
        resultAction.setSnapDescription(StringUtils.EMPTY);
        resultAction.getVmdkInfoList().addAll(snap.getConfig().getAllVmdkInfo());
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("snapshot: %s Moref: %s", snap.getName(), snap.getMoref().getValue());
            this.logger.info(msg);
        }
        return true;
    }

    @Override
    public CoreBackupOptions getOptions() {
        return (CoreBackupOptions) this.options;
    }

    /**
     *
     * @param rab
     * @return
     * @throws CoreResultActionException
     */
    private boolean retrieveVmConfigFiles(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionVmBackup - start"); //$NON-NLS-1$
        }
        final CoreResultActionGetVmConfig resultAction = new CoreResultActionGetVmConfig(rab);
        final GenerationProfile profile = rab.getProfile();
        final ITargetOperation target = profile.getTargetOperation();
        final VirtualMachineManager vmm = rab.getFirstClassObject();
        try {
            resultAction.start();
            resultAction.setFcoEntityInfo(vmm.getFcoInfo());
            resultAction.setVmPathName(vmm.getConfig().getVmPathName());
            resultAction.setvAppConfig(vmm.isvAppConfigAvailable());
            final ByteArrayInOutStream vmxByteArrayStreamContent = vmm.exportVmx();
            if (vmxByteArrayStreamContent != null) {
                try (VmxManipulator vmxContent = new VmxManipulator(vmxByteArrayStreamContent.toByteArray())) {
                    resultAction.setVmNvRamPathName(vmxContent.getNvram());
                    final ByteArrayInOutStream nvRamByteArrayStreamContent = vmm.exportNvram(vmxContent.getNvram());
                    if (nvRamByteArrayStreamContent == null) {
                        resultAction.failure(
                                String.format("NVRAM export failed. %s not available.", vmxContent.getNvram()));
                    } else {
                        SerializableVmConfigInfo vAppConfig = null;
                        if (vmm.isvAppConfigAvailable()) {
                            vAppConfig = vmm.getvAppConfig();
                        }
                        if (target.postVmx(profile, vmxByteArrayStreamContent)
                                && target.postNvRam(profile, nvRamByteArrayStreamContent) && (vAppConfig != null)) {
                            try (final ByteArrayInOutStream fos = new ByteArrayInOutStream()) {
                                fos.write(new ObjectMapper().writeValueAsBytes(vAppConfig));
                                if (target.postvAppConfig(profile, fos)) {
                                    resultAction.success();
                                } else {
                                    resultAction.failure("vApp config export failed.  ");
                                }
                            }
                        }
                    }
                }

            } else {
                resultAction.failure("Virtual Machine Configuration export failed.  ");
            }

        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | IOException | NoSuchAlgorithmException e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        } catch (final InterruptedException e) {
            resultAction.failure(e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "VirtualMachineManager, GenerationProfile, TargetOperation, CoreResultActionVmBackup - end"); //$NON-NLS-1$
        }
        return resultAction.isSuccessful();
    }

    private boolean infoCollection(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        /**
         * Start Section InfoCollection
         */
        rab.getInteractive().startInfoCollection();

        final boolean result = unmarkAsTemplate(rab) && checkCbt(rab);

        rab.getInteractive().endInfoCollection();
        /**
         * End Section InfoCollection
         */
        return result;
    }

    private boolean removeSnapshot(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        boolean result = false;
        final VirtualMachineManager vmm = rab.getFirstClassObject();
        final CoreResultActionDeleteSnap resultAction = new CoreResultActionDeleteSnap(rab);
        rab.setDeleteSnapshotAction(resultAction);
        if ((rab.getCreateSnapshotAction() != null) && rab.getCreateSnapshotAction().isSuccessful()) {
            /**
             * Start Section RemoveSnapshot
             */
            rab.getInteractive().startRemoveSnapshot();

            try {
                resultAction.start();
                final SnapshotManager snap = vmm.getSnapshotManager();
                final ManagedObjectReference snapToDelete = snap.getMoref();

                final SnapshotInfo snapInfo = snap.getSnapshotInfo();
                resultAction.getSnapList().add(snapInfo);
                vmm.deleteSnapshot(snapToDelete, false, true);
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Delete snapshot %s succeeded.", snap.getActiveSnapShotName());
                    this.logger.info(msg);
                }
                result = true;

            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                resultAction.failure(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | VimTaskException | InvalidCollectorVersionFaultMsg
                    | TaskInProgressFaultMsg e) {
                resultAction.failure(e);
                Utility.logWarning(this.logger, e);
            } finally {
                resultAction.done();
                rab.getInteractive().endRemoveSnapshot();
                /**
                 * End Section RemoveSnapshot
                 */
            }
        } else {
            result = true;
            resultAction.skip("No snapshot removal required");
        }
        return result;

    }

    private boolean revertToTemplate(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        boolean result = false;
        final VirtualMachineManager vmm = rab.getFirstClassObject();
        if (rab.isTemplate()) {
            final CoreResultActionRevertToTemplate resultAction = new CoreResultActionRevertToTemplate(rab);
            resultAction.start();
            /**
             * Start Section RevertToTemplate
             */
            rab.getInteractive().startRevertToTemplate();
            try {
                vmm.markAsTemplate();
                result = true;
            } catch (FileFaultFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                    | VmConfigFaultFaultMsg e) {
                resultAction.failure(e);
                rab.failure(e);
            } catch (final InterruptedException e) {
                resultAction.failure(e);
                rab.failure(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                resultAction.done();
            }
            rab.getInteractive().endRevertToTemplate();
            /**
             * End Section RevertToTemplate
             */

        } else {
            result = true;
        }

        return result;
    }

    private boolean unmarkAsTemplate(final CoreResultActionVmBackup rab) throws CoreResultActionException {
        boolean result = false;
        final VirtualMachineManager vmm = rab.getFirstClassObject();

        if (vmm.getConfig().isTemplate()) {

            final CoreResultActionRevertToTemplate resultAction = new CoreResultActionRevertToTemplate(rab);
            resultAction.start();
            try {
                rab.setTemplate(true);
                Map<String, Boolean> privileges = vmm.getVimConnection().getPrivilegeChecker()
                        .hasUserPrivilegesOnEntity(vmm.getMoref(),
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_PROVISIONING_MARK_AS_TEMPLATE,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_PROVISIONING_MARK_AS_VM);

                List<String> failedCheck = new ArrayList<>();
                for (Entry<String, Boolean> entry : privileges.entrySet()) {
                    if (Boolean.FALSE.equals(entry.getValue())) {
                        failedCheck.add(entry.getKey());
                    }
                }
                if (failedCheck.isEmpty()) {

                    /**
                     * Start Section RevertToTemplate
                     */
                    rab.getInteractive().startRevertToTemplate();
                    vmm.markAsVirtualMachine(vmm.getVimConnection().getDefaultResurcePool());
                    result = true;
                } else {
                    String msg = "User lack of the following privileges:" + StringUtils.join(failedCheck, ",");
                    resultAction.failure(msg);
                    rab.failure(msg);
                    logger.warning(msg);
                }
            } catch (FileFaultFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                    | InvalidDatastoreFaultMsg | VmConfigFaultFaultMsg e) {
                resultAction.failure(e);
                rab.failure(e);
            } catch (final InterruptedException e) {
                resultAction.failure(e);
                rab.failure(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                resultAction.done();

                rab.getInteractive().endRevertToTemplate();
                /**
                 * End Section RevertToTemplate
                 */
            }

        } else {
            result = true;
            rab.setTemplate(false);
        }

        return result;
    }

}
