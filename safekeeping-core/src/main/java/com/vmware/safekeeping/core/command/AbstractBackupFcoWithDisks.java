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
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.JVixException;
import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractBackupDiskInteractive;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionPrepeareGeneration;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.BackupException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public abstract class AbstractBackupFcoWithDisks extends AbstractBackupFco implements ICommandSupportForSnapshot {

    protected AbstractBackupFcoWithDisks(final VimConnection vimConnection, final CoreBackupOptions options,
            final Logger logger) {
        super(vimConnection, options, logger);
    }

    protected AbstractBackupFcoWithDisks(final VimConnection vimConnection, final CoreVirtualBackupOptions options,
            final Logger logger) {
        super(vimConnection, options, logger);
    }

    private CoreResultActionDiskBackup backupAttachedIvd(final Jvddk jvddk, final GlobalFcoProfileCatalog profAllVm,
            final CoreResultActionDiskBackup resultAction) throws CoreResultActionException {
        final BackupMode prevMode = getOptions().getRequestedBackupMode();
        ArrayList<Block> changedDiskAreas = null;

        final GenerationProfile profile = resultAction.getProfile();
        ITargetOperation target = null;
        VirtualMachineManager vmm = null;
        try {
            target = profile.getTargetOperation().newTargetOperation(resultAction.getFcoEntityInfo(), this.logger);
            profile.getTargetOperation();
            vmm = (VirtualMachineManager) resultAction.getFirstClassObject();
            final int diskId = profile.getDiskIdWithUuid(resultAction.getUuid());

            final ImprovedVirtualDisk ivd = vmm.getVimConnection().getFind().findIvdById(resultAction.getvDiskId());
            resultAction.setFcoEntityInfo(ivd.getFcoInfo());
            if (resultAction.getDiskId() == null) {
                resultAction.setDiskId(diskId);
            }
            resultAction.setUuid(ivd.getUuid());
            if (this.logger.isLoggable(Level.INFO)) {
                String msg = String.format("Ivd uuid:%s vm disk uuid:%s", ivd.getUuid(), resultAction.getUuid());
                this.logger.info(msg);
                msg = String.format("Controller:%d Disk [%d:%d]  %s is an Improved Virtual Disk",
                        resultAction.getControllerKey(), resultAction.getBusNumber(), resultAction.getUnitNumber(),
                        ivd.toString().trim());
                this.logger.info(msg);
                msg = String.format("Starting dump vmdk %s (Improved Virtual Disks disk)", resultAction.getUuid());
                this.logger.info(msg);

            }
            profAllVm.createNewProfile(resultAction.getFcoEntityInfo(), resultAction.getStartDate());

            target.saveStatus(vmm.getUuid());

            final FcoArchiveManager ivdArcMgr = new FcoArchiveManager(resultAction.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            final GenerationProfileSpec spec = new GenerationProfileSpec(ivd, (CoreBackupOptions) getOptions(),
                    resultAction.getStartDate());

            spec.setCbt(resultAction.getChangeId());

            final GenerationProfile ivdProfGen = ivdArcMgr.prepareNewGeneration(spec);

            target.createGenerationFolder(ivdProfGen);
            target.postGenerationProfile(ivdProfGen);
            boolean cbtHealth = true;
            BackupMode backupMode = ivdArcMgr.determineBackupMode(ivdProfGen, getOptions().getRequestedBackupMode());

            switch (backupMode) {
            case FULL:

                ivdArcMgr.getGenerationsCatalog().setGenerationNotDependent(ivdProfGen.getGenerationId());
                break;
            case INCREMENTAL:
                changedDiskAreas = ivd.queryChangedDiskAreas(ivdProfGen, 0, backupMode);
                if (changedDiskAreas == null) {
                    cbtHealth = false;
                    this.logger.log(Level.WARNING, () -> "Problem with the CBT - switch to full backup");
                    backupMode = BackupMode.FULL;
                    ivdArcMgr.getGenerationsCatalog().setGenerationNotDependent(ivdProfGen.getGenerationId());
                    break;

                }
                break;
            default:
                throw new BackupException("Unable to detect the backup mode");

            }
            resultAction.setCbtHealth(cbtHealth);
            resultAction.setBackupMode(backupMode);

            backupVmdk(vmm, ivdProfGen, changedDiskAreas, jvddk, resultAction);
            final String msg = String.format("Dump vmdk %s %s.", resultAction.getUuid(),
                    (resultAction.isSuccessful() ? "succeeded" : "failed"));
            this.logger.info(msg);
            ivdArcMgr.finalizeBackup(ivdProfGen, resultAction.getState());
            if (resultAction.isFails()) {
                this.logger.log(Level.WARNING, () -> "Backup finalization failed.");
            }
            target.postGenerationProfile(ivdProfGen);
            if (target.postMd5(ivdProfGen)) {
                ivdArcMgr.postGenerationsCatalog();
            }
        } catch (final BackupException | NoSuchAlgorithmException | IOException | ArchiveException
                | InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidArgumentFaultMsg
                | com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.RuntimeFaultFaultMsg
                | com.vmware.vslm.NotFoundFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg | ProfileException
                | com.vmware.pbm.InvalidArgumentFaultMsg | NotFoundFaultMsg | FileFaultFaultMsg | PbmFaultFaultMsg
                | VimObjectNotExistException | VslmFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            Thread.currentThread().interrupt();
            resultAction.failure(e);

        } finally {
            resultAction.setBackupMode(prevMode);
            target.loadStatus(vmm.getUuid(), true);
        }

        return resultAction;

    }

    protected void backupVmdk(final IFirstClassObject fco, final GenerationProfile profGen,
            final ArrayList<Block> changedDiskAreas, final Jvddk jvddk, final CoreResultActionDiskBackup radb)
            throws CoreResultActionException {
        radb.start();
        final AbstractBackupDiskInteractive diskInteractive = radb.getParent().getInteractive()
                .newDiskInteractiveInstance(radb);
        final ITargetOperation target = profGen.getTargetOperation();

        final int diskId = radb.getDiskId();
        try {
            final AbstractCoreResultActionBackupForEntityWithDisks rab = radb.getParent();
            final CoreBackupOptions option = rab.getOptions();
            profGen.setDiskBackupMode(diskId, rab.getBackupMode());
            radb.setBackupMode(rab.getBackupMode());
            radb.setGenerationId(profGen.getGenerationId());
            radb.setNumberOfThreads(option.getNumberOfThreads());
            radb.setMaxBlockSize(option.getMaxBlockSize());
            radb.setQueryBlocksOption(option.getQueryBlocksOption());
            profGen.setDumpBeginTimestamp(diskId, radb.getStartTime());
            radb.setChangedBlockTrackingEnabled(radb.getParent().isCbtEnabled());
            /**
             * Start Section DiskBackup
             */
            diskInteractive.startDiskBackup();
            jvddk.doDumpJava(fco, profGen, changedDiskAreas, target, radb, diskInteractive);
        } finally {
            radb.done();
            profGen.setDumpEndTimestamp(diskId, radb.getEndTime());
            profGen.setVmdkdumpResult(diskId, radb.isSuccessfulOrSkipped());
            diskInteractive.endDiskBackup();
            /**
             * End Section DiskBackup
             */
        }
    }

    protected boolean checkCbt(final AbstractCoreResultActionBackupForEntityWithDisks rab)
            throws CoreResultActionException {
        boolean result = false;
        final IFirstClassObject fco = rab.getFirstClassObject();

        try {
            rab.setCbtEnabled(fco.isChangedBlockTrackingEnabled());
            if (!rab.isCbtEnabled()) {

                if (CoreGlobalSettings.isAutoConfigureCbtOn()) {
                    result = enableCbt(rab);
                } else {
                    if (getOptions().isForce()) {
                        this.logger.log(Level.WARNING,
                                () -> "Change Tracking Block (CBT) disabled. force option detected.");
                        result = true;
                    } else {
                        rab.skip("Change Tracking Block (CBT) disabled. Virtual Machine Skipped. Enable CBT");
                    }
                }
            } else {
                if (logger.isLoggable(Level.INFO)) {
                    String msg = rab.getFcoToString() + " change Tracking Block (CBT) enabled.";
                    logger.info(msg);
                }
                result = true;
            }
        } catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
            rab.failure(e);
        } catch (final InterruptedException e) {
            rab.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

        return result;
    }

    /**
     * Check if the CBT is healthy and not corrupted
     *
     * @param radb
     * @return
     */
    protected boolean checkCbtHealth(final CoreResultActionDiskBackup radb) {
        final boolean isCbtHealth = true;
        // TODO implement CBT Health
        radb.setCbtHealth(isCbtHealth);
        return isCbtHealth;
    }

    private boolean enableCbt(final AbstractCoreResultActionBackupForEntityWithDisks rab)
            throws CoreResultActionException {
        final boolean cbtOn = true;
        final IFirstClassObject fco = rab.getFirstClassObject();
        final CoreResultActionCbt resultAction = new CoreResultActionCbt();
        resultAction.setParent(rab);
        rab.getSubOperations().add(resultAction);
        resultAction.start();
        try {
            resultAction.setFcoEntityInfo(fco.getFcoInfo());
            resultAction.setPreviousFlagState(fco.isChangedBlockTrackingEnabled());

            if (fco.setChangeBlockTracking(cbtOn)) {
                rab.setCbtEnabled(cbtOn);
                resultAction.setFlagState(cbtOn);
                if (this.logger.isLoggable(Level.INFO)) {
                    this.logger.info(fco.getFcoInfo().toString() + " CBT enabled");
                }
            }
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | FileFaultFaultMsg | InvalidNameFaultMsg
                | InvalidStateFaultMsg | SnapshotFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg
                | InvalidCollectorVersionFaultMsg | VimTaskException | ConcurrentAccessFaultMsg | DuplicateNameFaultMsg
                | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | NotFoundFaultMsg
                | com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.InvalidStateFaultMsg
                | com.vmware.vslm.NotFoundFaultMsg | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            resultAction.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
        }

        return resultAction.isSuccessful();
    }

    protected boolean endVddkAccess(final AbstractCoreResultActionBackupForEntityWithDisks rab, final Jvddk jvddk)
            throws CoreResultActionException, JVixException {

        long vddkCallResult;
        vddkCallResult = jvddk.disconnect(rab.getConnectionHandle());
        if (vddkCallResult != jDiskLibConst.VIX_OK) {
            rab.failure(jvddk.getErrorText(vddkCallResult));

        } else {

            vddkCallResult = jvddk.endAccess();
            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                rab.failure(jvddk.getErrorText(vddkCallResult));

            }
        }
        rab.getInteractive().endVddkAccess();
        /**
         * End Section VddkAccess
         */

        return rab.isRunning();

    }

    protected boolean finalizeProfile(final AbstractCoreResultActionBackupForEntityWithDisks rab)
            throws NoSuchAlgorithmException, IOException {
        boolean result = false;
        /**
         * Start Section FinalizeProfile
         */
        rab.getInteractive().startFinalizeProfile();
        final GenerationProfile profile = rab.getProfile();
        final ITargetOperation target = profile.getTargetOperation();
        final FcoArchiveManager fcoArcMgr = profile.getFcoArchiveManager();
        result = fcoArcMgr.finalizeBackup(profile, rab.getDiskActionsResult());
        if (rab.isFails()) {
            this.logger.warning("Backup finalization failed.");
        }

        result &= target.postGenerationProfile(profile);
        if (target.postMd5(profile)) {
            fcoArcMgr.postGenerationsCatalog();
        }
        rab.getInteractive().endFinalizeProfile();
        /**
         * End Section FinalizeProfile
         */
        return result;
    }

    protected boolean generationComputation(final AbstractCoreResultActionBackupForEntityWithDisks rab,
            final FcoArchiveManager fcoArcMgr) throws CoreResultActionException, InterruptedException {
        /**
         * Start Section GenerationComputation
         */
        rab.getInteractive().startGenerationComputation();
        if (this.logger.isLoggable(Level.INFO)) {
            if (rab instanceof CoreResultActionVmBackup) {
                this.logger.info(MessagesTemplate.getHeaderGuestInfoString((CoreResultActionVmBackup) rab));
            }
            this.logger.info(MessagesTemplate.getLocationString(rab));
        }
        final GenerationProfileSpec spec = new GenerationProfileSpec(rab.getFirstClassObject(),
                (CoreBackupOptions) getOptions(), rab.getStartDate());
        spec.getVmdkInfoList().addAll(rab.getCreateSnapshotAction().getVmdkInfoList());
        final GenerationProfile profile = new GenerationProfile(fcoArcMgr);
        rab.setProfile(profile);
        final CoreResultActionPrepeareGeneration resultPrepareNewProfile = prepareNewGeneration(spec, profile, rab);

        final GenerationInfo generationInfo = new GenerationInfo();
        generationInfo.setGenerationId(profile.getGenerationId());
        generationInfo.setPreviousGenerationId(profile.getPreviousGenerationId());
        generationInfo.setTargetUri(fcoArcMgr.getRepositoryTarget().getUri(profile.getGenerationPath()));
        generationInfo.setBackupMode(getOptions().getRequestedBackupMode());
        rab.setGenerationInfo(generationInfo);

        Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info(MessagesTemplate.getGenerationInfo(rab));
        }
        rab.getInteractive().endGenerationComputation();
        /**
         * end Section GenerationComputation
         */

        return resultPrepareNewProfile.isSuccessful();
    }

    protected BackupMode getDiskBackupMode(final CoreResultActionDiskBackup radb) throws CoreResultActionException {
        final Integer diskId = radb.getDiskId();
        BackupMode backupMode = BackupMode.UNKNOW;
        final GenerationProfile profile = radb.getProfile();
        String msg;
        if (getOptions().isNoVmdk()) {
            profile.setVmdkdumpResult(diskId, true);
            msg = String.format("Dump vmdk %s skipped noVmdk option", radb.getUuid());
            this.logger.warning(msg);
            radb.skip(msg);
        } else if (profile.getDisks().get(diskId).getDiskMode() == VirtualDiskModeType.independent_persistent) {
            msg = String.format("Dump vmdk %s skipped (Indipendent or Persistent Virtual Disks disk)", radb.getUuid());
            this.logger.warning(msg);
            radb.skip(msg);
        } else if ((profile.getFcoArchiveManager().determineBackupMode(profile, diskId,
                getOptions().getRequestedBackupMode()) == BackupMode.FULL) || (backupMode == BackupMode.FULL)) {
            backupMode = BackupMode.FULL;
            radb.setBackupMode(backupMode);
            profile.getFcoArchiveManager().getGenerationsCatalog().setGenerationNotDependent(profile.getGenerationId());
        } else {
            backupMode = BackupMode.INCREMENTAL;
            radb.setBackupMode(backupMode);
            radb.setGenerationId(radb.getParent().getGenerationInfo().getGenerationId());
        }

        return backupMode;
    }

    /**
     * Prepare for the VDDK access to the disk
     * 
     * @param rab
     * @param jvddk
     * @return true if succeed
     * @throws CoreResultActionException
     */
    protected boolean startVddkAccess(final AbstractCoreResultActionBackupForEntityWithDisks rab, final Jvddk jvddk)
            throws CoreResultActionException {
        /**
         * Start Section VddkAccess
         */
        rab.getInteractive().startVddkAccess();
        try {
            if (rab.getEntityType() == EntityType.VirtualMachine) {
                final long vddkCallResult = jvddk.prepareForAccess(SafekeepingVersion.getInstance().getIdentity());
                if (vddkCallResult != jDiskLibConst.VIX_OK) {
                    rab.failure(jvddk.getErrorText(vddkCallResult));
                    return false;
                }
            }
            final String snapMorefValue = (rab.getCreateSnapshotAction() == null) ? null
                    : rab.getCreateSnapshotAction().getSnapMoref();
            rab.setConnectionHandle(jvddk.connectReadOnly(snapMorefValue, getOptions().getRequestedTransportModes()));

        } catch (final JVixException e) {
            Utility.logWarning(this.logger, e);
            rab.failure(e);
        }

        return rab.isRunning();
    }

    /**
     * Submit the backup thread to the Threads Executor
     * 
     * @param radb
     * @param jvddk
     * @param fcoProfileCatalog
     * @param logger
     * @return
     */
    protected Future<CoreResultActionDiskBackup> submit(final CoreResultActionDiskBackup radb, final Jvddk jvddk,
            final GlobalFcoProfileCatalog fcoProfileCatalog, final Logger logger) {
        return ThreadsManager.executor(ThreadType.VDDK).submit(() -> {
            final GenerationProfile profile = radb.getProfile();
            try {
                final IFirstClassObject fco = radb.getFirstClassObject();
                if (radb.isQueuing()) {
                    if ((fco instanceof VirtualMachineManager) && (radb.getvDiskId() != null)
                            && (fcoProfileCatalog != null)) {
                        backupAttachedIvd(jvddk, fcoProfileCatalog, radb);
                    } else {
                        backupVmdk(fco, profile, null, jvddk, radb);
                    }
                } else if (radb.isDone()) {
                    logger.warning("Disk operation on disk" + radb.getDiskId() + " over");
                } else {
                    final String msg = "Disk operation state is inconsistent " + radb.getState().toString();
                    logger.warning(msg);
                    radb.failure(msg);
                }

            } catch (final CoreResultActionException e) {
                Utility.logWarning(logger, e);

            }
            return radb;

        });
    }

}
