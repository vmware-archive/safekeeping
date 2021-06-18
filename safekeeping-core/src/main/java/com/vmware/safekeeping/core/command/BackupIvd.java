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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.support.SnapshotInfo;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vslm.VslmFaultFaultMsg;

public class BackupIvd extends AbstractBackupFcoWithDisks {

    public BackupIvd(final VimConnection vimConnection, final CoreBackupOptions options, final Logger logger) {
        super(vimConnection, options, logger);
    }

    public CoreResultActionIvdBackup backup(final ImprovedVirtualDisk ivd, final FcoArchiveManager fcoArcMgr,
            final CoreResultActionIvdBackup rab) throws CoreResultActionException {
        backup(ivd, fcoArcMgr, null, rab);
        return rab;
    }

    /**
     * entry point for an IVD backup
     *
     * @param ivd
     * @param fcoArcMgr
     * @param transportModes Transport mode to use during the backup
     * @param snap           if null a new snapshot is created
     * @return
     * @throws CoreResultActionException
     */
    public CoreResultActionIvdBackup backup(final ImprovedVirtualDisk ivd, final FcoArchiveManager fcoArcMgr,
            final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap, final CoreResultActionIvdBackup rab)
            throws CoreResultActionException {

        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupIvd() start.");
        }
        if (!getOptions().isDryRun()) {
            try {
                if (infoCollection(rab) && createSnapshot(rab, snap) && generationComputation(rab, fcoArcMgr)) {
                    final CoreResultActionDiskBackup radb = new CoreResultActionDiskBackup(rab.getProfile(), rab);
                    checkCbtHealth(radb);

                    final Jvddk jvddk = new Jvddk(this.logger, ivd, rab.getCreateSnapshotAction().getSnapId());
                    if (checkPrivileges(rab) && discoveryBackupMode(rab) && startVddkAccess(rab, jvddk)
                            && disksBackup(rab, jvddk) && endVddkAccess(rab, jvddk) && finalizeProfile(rab)) {
                        final String msg = rab.getFcoToString() + " Backup Success - start cleaning";
                        if (this.logger.isLoggable(Level.INFO)) {
                            this.logger.info(msg);
                        }
                    }
                } else {
                    final CoreResultActionDiskBackup radb = new CoreResultActionDiskBackup(rab.getProfile(), rab);
                    radb.aborted();
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
            }
        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupIvd() end.");
        }
        return rab;

    }

    private boolean createSnapshot(final CoreResultActionIvdBackup rab,
            VStorageObjectSnapshotInfoVStorageObjectSnapshot snap) throws CoreResultActionException {
        final ImprovedVirtualDisk ivd = rab.getFirstClassObject();
        boolean result = false;
        /**
         * Start Section CreateSnapshot
         */
        rab.getInteractive().startCreateSnapshot();

        final CoreResultActionCreateSnap resultAction = new CoreResultActionCreateSnap(rab);
        rab.setCreateSnapshotAction(resultAction);
        if (snap == null) {
            /*
             * Create snapshot
             */
            resultAction.start();
            final String snapName = Utility.generateSnapshotName(rab.getStartDate());
            try {
                snap = createSnapshot(ivd, snapName);
                if (snap == null) {
                    resultAction.failure("Unable to retreive the snapshots list");
                } else {
                    resultAction.setSnapId(snap.getId().getId());
                    resultAction.setSnapName(snapName);
                    resultAction.setSnapDescription(snap.getDescription());
                    resultAction.setSnapBackingObjectId(snap.getBackingObjectId());
                    result = true;
                }
            } catch (final InterruptedException e) {
                resultAction.failure(e);
                Utility.logWarning(this.logger, e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } catch (com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                    | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                    | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | InvalidDatastoreFaultMsg
                    | NotFoundFaultMsg | VslmTaskException | FileFaultFaultMsg | InvalidStateFaultMsg
                    | RuntimeFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
                    | VimTaskException e) {
                resultAction.failure(e);
                Utility.logWarning(this.logger, e);
            } finally {
                resultAction.done();

            }

        } else {
            resultAction.setSnapId(snap.getId().getId());
            resultAction.setSnapName(snap.getDescription());
            resultAction.setSnapDescription(snap.getDescription());
            resultAction.setSnapBackingObjectId(snap.getBackingObjectId());
            final String msg = String.format("Snapshot  id:%s name:%s provided no additional one is required",
                    snap.getId().toString(), snap.getDescription());
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.info(msg);
            }
            resultAction.skip(msg);
            result = true;

        }
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("snapshot: %s ID:%s Backing Object %s", resultAction.getSnapDescription(),
                    resultAction.getSnapId(), resultAction.getSnapBackingObjectId());
            this.logger.info(msg);
            this.logger.info(MessagesTemplate.getLocationString(rab));
        }
        rab.getInteractive().endCreateSnapshot();
        /**
         * End Section CreateSnapshot
         */
        return result;
    }

    private boolean discoveryBackupMode(final CoreResultActionIvdBackup rab) throws CoreResultActionException {
        /**
         * Start Section DiscoverBackupMode
         */
        rab.getInteractive().startDiscoverBackupMode();

        final GenerationProfile profile = rab.getProfile();
        final BackupMode backupMode = getDiskBackupMode(rab.getResultActionOnDisk());

        rab.setBackupMode(backupMode);
        profile.setBackupMode(backupMode);
        rab.getGenerationInfo().setBackupMode(backupMode);
        rab.getInteractive().endDiscoverBackupMode();
        /**
         * End Section DiscoverBackupMode
         */
        return backupMode != BackupMode.UNKNOW;
    }

    private boolean disksBackup(final CoreResultActionIvdBackup rab, final Jvddk jvddk) {
        boolean result = false;
        try {
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
            /**
             * Start Section DisksBackup
             */
            rab.getInteractive().startDisksBackup();

            final CoreResultActionDiskBackup radb = rab.getResultActionOnDisk();
            final Future<CoreResultActionDiskBackup> future = submit(radb, jvddk, null, this.logger);

            // A) Await all runnable to be done (blocking)
            // get will block until the future is done
            final CoreResultActionDiskBackup obj = future.get();
            if ((obj != null) && this.logger.isLoggable(Level.FINE)) {
                this.logger.fine(obj.toString());
            }
            result = true;

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

    private boolean infoCollection(final CoreResultActionIvdBackup rab) throws CoreResultActionException {
        /**
         * Start Section InfoCollection
         */
        rab.getInteractive().startInfoCollection();
        final boolean result = checkCbt(rab);
        rab.getInteractive().endInfoCollection();
        /**
         * End Section InfoCollection
         */
        return result;
    }

    private boolean removeSnapshot(final CoreResultActionIvdBackup rab) throws CoreResultActionException {
        boolean result = false;
        final CoreResultActionDeleteSnap resultAction = new CoreResultActionDeleteSnap(rab);
        rab.setDeleteSnapshotAction(resultAction);
        final ImprovedVirtualDisk ivd = rab.getFirstClassObject();
        if ((rab.getCreateSnapshotAction() != null) && rab.getCreateSnapshotAction().isSuccessful()) {
            /**
             * Start Section RemoveSnapshot
             */
            rab.getInteractive().startRemoveSnapshot();
            try {
                resultAction.start();
                final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap = ivd.getCurrentSnapshot();
                final SnapshotInfo snapInfo = new SnapshotInfo();
                snapInfo.setCreateTime(snap.getCreateTime());
                snapInfo.setDescription(snap.getDescription());
                snapInfo.setId(snap.getId().toString());
                resultAction.getSnapList().add(snapInfo);

                ivd.deleteSnapshot(snap);
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Delete snapshot ID: %s Desc: %s succeeded.", snap.getId().getId(),
                            snap.getDescription());
                    this.logger.info(msg);
                }
                result = true;

            } catch (final InterruptedException e) {
                resultAction.failure(e);
                this.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } catch (FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                    | RuntimeFaultFaultMsg | com.vmware.vslm.FileFaultFaultMsg
                    | com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.InvalidStateFaultMsg
                    | com.vmware.vslm.NotFoundFaultMsg | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg
                    | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg | VslmTaskException
                    | VimTaskException e) {
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
            resultAction.skip("No snapshot removal required");
            result = true;
        }
        return result;
    }

}
