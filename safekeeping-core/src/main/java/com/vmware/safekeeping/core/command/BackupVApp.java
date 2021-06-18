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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionPostVappOvfMetadata;
import com.vmware.safekeeping.core.command.results.CoreResultActionPrepeareGeneration;
import com.vmware.safekeeping.core.command.results.CoreResultActionRetrieveChildren;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfoIdGeneration;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class BackupVApp extends AbstractBackupFco implements IBackupEntry, IVappOperationsSupport {

    public BackupVApp(final VimConnection vimConnection, final CoreBackupOptions options, final Logger logger) {
        super(vimConnection, options, logger);
    }

    public CoreResultActionVappBackup backup(final FcoArchiveManager fcoArcMgr,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionVappBackup rab)
            throws CoreResultActionException {
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupVapp() start.");
        }
        if (!getOptions().isDryRun()) {
            try {
                vAppInfoCollection(rab);
                if (generationComputation(rab, fcoArcMgr, globalFcoCatalog) && postOvfMetadata(rab)) {

                    childrenBackup(rab, globalFcoCatalog);
                    if (finalizeProfile(rab)) {

                        this.logger.info("Backup Success");
                    }

                }
            } catch (final InterruptedException e) {
                rab.failure(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();

            } catch (final NoSuchAlgorithmException | IOException e) {
                rab.failure(e);
                Utility.logWarning(this.logger, e);
            } catch (final Exception e) {
                logger.warning("----------------- Unexpected Error ---------------------");
                Utility.logWarning(this.logger, e);
                rab.failure();
            }
        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupVapp() end.");
        }
        return rab;

    }

    private boolean childrenBackup(final CoreResultActionVappBackup resultAction,
            final GlobalFcoProfileCatalog globalFcoCatalog) throws CoreResultActionException {
        boolean result = true;
        try {
            /**
             * Start Section ChildrenBackup
             */
            resultAction.getInteractive().startChildrenBackup();
            final ITarget target = globalFcoCatalog.getTarget();
            final List<Future<CoreResultActionVmBackup>> futures = new ArrayList<>();
            int index = 0;
            for (final CoreResultActionVmBackup rabChild : resultAction.getResultActionOnsChildVm()) {
                final Future<CoreResultActionVmBackup> f = submit(rabChild, globalFcoCatalog,
                        target.newTargetOperation(rabChild.getFcoEntityInfo(), this.logger));
                futures.add(f);
            }

            BackupMode backupMode = BackupMode.UNKNOW;
            // A) Await all runnable to be done (blocking)
            for (final Future<CoreResultActionVmBackup> future : futures) {
                // get will block until the future is done
                final CoreResultActionVmBackup rabChild = future.get();
                if (rabChild != null) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine(rabChild.toString());
                    }

                    if (rabChild.isSuccessful()) {
                        resultAction.getProfile().setChildSuccessfullGenerationId(index,
                                rabChild.getGenerationInfo().getGenerationId());

                        switch (backupMode) {
                        case FULL:
                            switch (rabChild.getGenerationInfo().getBackupMode()) {
                            case FULL:
                            case UNKNOW:
                                backupMode = BackupMode.FULL;
                                break;
                            case INCREMENTAL:
                                backupMode = BackupMode.MIXED;
                                break;
                            case MIXED:

                            default:
                                backupMode = BackupMode.MIXED;
                                break;
                            }
                            break;
                        case INCREMENTAL:
                            switch (rabChild.getGenerationInfo().getBackupMode()) {
                            case FULL:
                                backupMode = BackupMode.MIXED;
                                break;
                            case INCREMENTAL:
                            case UNKNOW:
                                backupMode = BackupMode.INCREMENTAL;
                                break;
                            case MIXED:
                            default:
                                backupMode = BackupMode.MIXED;
                                break;
                            }
                            break;
                        case MIXED:
                        case UNKNOW:
                        default:
                            backupMode = BackupMode.MIXED;
                            break;
                        }

                        resultAction.getGenerationInfo().setBackupMode(backupMode);
                        resultAction.getProfile().setBackupMode(backupMode);
                        resultAction.setBackupMode(backupMode);

                    }

                    Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);

                    ++index;
                    result &= rabChild.isSuccessfulOrSkipped();
                } else {
                    result = false;
                    final String msg = "Internal error - check server logs";
                    resultAction.failure(msg);
                }
            }

        } catch (final InterruptedException e) {
            resultAction.failure(e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            resultAction.getInteractive().endChildrenBackup();
            /**
             * End Section ChildrenBackup
             */
        }
        return result;
    }

    private boolean generationComputation(final CoreResultActionVappBackup rab, final FcoArchiveManager fcoArcMgr,
            final GlobalFcoProfileCatalog globalFcoCatalog) throws CoreResultActionException {
        boolean result = false;
        /**
         * Start Section GenerationComputation
         */
        rab.getInteractive().startGenerationComputation();
        final VirtualAppManager vApp = rab.getFirstClassObject();
        final GenerationProfileSpec spec = new GenerationProfileSpec(vApp, getOptions(), rab.getStartDate());
        final GenerationProfile profile = new GenerationProfile(fcoArcMgr);
        final CoreResultActionPrepeareGeneration resultPrepareNewProfile = prepareNewGeneration(spec, profile, rab);

        if (resultPrepareNewProfile.isSuccessful()) {
            try {
                final GenerationInfo generationInfo = new GenerationInfo();
                generationInfo.setGenerationId(profile.getGenerationId());
                generationInfo.setPreviousGenerationId(profile.getPreviousGenerationId());
                generationInfo.setTargetUri(fcoArcMgr.getRepositoryTarget().getUri(profile.getGenerationPath()));
                generationInfo.setBackupMode(BackupMode.UNKNOW);
                rab.setGenerationInfo(generationInfo);
                rab.setProfile(profile);
                if (this.logger.isLoggable(Level.INFO)) {
                    this.logger.info(rab.getGenerationInfo().toString());
                }
                globalFcoCatalog.createNewProfile(rab.getFcoEntityInfo(), rab.getStartDate());

                final ITargetOperation target = fcoArcMgr.getRepositoryTarget();
                rab.setTargetName(target.getTargetName());
                target.createGenerationFolder(profile);
                target.postGenerationProfile(profile);
                fcoArcMgr.postGenerationsCatalog();
                result = true;
            } catch (NoSuchAlgorithmException | IOException e) {
                rab.failure(e);
                Utility.logWarning(this.logger, e);
                result = false;
            }
        }
        rab.getInteractive().endGenerationComputation();
        /**
         * End Section GenerationComputation
         */
        return result;
    }

    @Override
    public CoreBackupOptions getOptions() {
        return (CoreBackupOptions) this.options;
    }

    private boolean postOvfMetadata(final CoreResultActionVappBackup rab) throws CoreResultActionException {
        boolean result = true;
        /**
         * Start Section PostOvfMetadata
         */
        rab.getInteractive().startPostOvfMetadata();
        final CoreResultActionPostVappOvfMetadata resultAction = new CoreResultActionPostVappOvfMetadata(rab);
        resultAction.start();
        try {
            final VirtualAppManager vApp = rab.getFirstClassObject();
            final GenerationProfile profile = rab.getProfile();
            if (vApp.isvAppConfigAvailable()) {
                final SerializableVAppConfigInfo vAppConfig = vApp.getVAppConfig();
                try (final ByteArrayInOutStream fos = new ByteArrayInOutStream()) {
                    fos.write(new ObjectMapper().writeValueAsBytes(vAppConfig));
                    if (!profile.getTargetOperation().postvAppConfig(profile, fos)) {
                        resultAction.failure();
                    }
                } catch (NoSuchAlgorithmException | IOException e) {
                    resultAction.failure(e);
                    Utility.logWarning(this.logger, e);
                    result = false;
                }

            }
        } finally {
            resultAction.done();
            rab.getInteractive().endPostOvfMetadata();
            /**
             * End Section PostOvfMetadata
             */
        }
        return result;
    }

    private Future<CoreResultActionVmBackup> submit(final CoreResultActionVmBackup rabChild,
            final GlobalFcoProfileCatalog globalFcoCatalog, final ITargetOperation newTargetOperation) {
        return ThreadsManager.executor(ThreadType.FCO).submit(() -> {
            CoreResultActionVmBackup result = null;
            try {
                result = actionBackupVmEntry(newTargetOperation, globalFcoCatalog, rabChild, this.logger);
            } catch (final CoreResultActionException e) {
                Utility.logWarning(this.logger, e);

            }
            return result;
        });
    }

    /**
     * Vapp Info collection section
     *
     * @param rab
     * @return
     * @throws InterruptedException
     */
    private boolean vAppInfoCollection(final CoreResultActionVappBackup rab) throws InterruptedException {
        final CoreResultActionRetrieveChildren resultAction = new CoreResultActionRetrieveChildren(rab);
        boolean cbt = true;
        /**
         * Start Section VappInfoCollection
         */
        rab.getInteractive().startVappInfoCollection();
        try {

            final VirtualAppManager vApp = rab.getFirstClassObject();
            int index = 0;
            rab.setNumberOfChildVm(vApp.getVmList().size());

            for (final VirtualMachineManager vmm : vApp.getVmList()) {
                ++index;
                final CoreResultActionVmBackup raChildVm = new CoreResultActionVmBackup(vmm, getOptions());
                resultAction.getChildren().put(vmm.getUuid(),
                        new ManagedFcoEntityInfoIdGeneration(vmm.getFcoInfo(), null));
                raChildVm.setInteractive(rab.getInteractive().newBackupVmInteractiveInstance(raChildVm));
                rab.getResultActionOnsChildVm().add(raChildVm);
                raChildVm.setParent(rab);
                rab.getFcoChildren().add(vmm.getFcoInfo());
                raChildVm.setIndex(index);
                try {
                    cbt &= vmm.isChangedBlockTrackingEnabled();
                } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                    raChildVm.failure(e);
                    resultAction.failure(e);
                    Utility.logWarning(this.logger, e);
                } catch (final InterruptedException e) {
                    raChildVm.failure(e);
                    resultAction.failure(e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }

            }
        } finally {
            rab.setCbtEnabled(cbt);
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
            resultAction.done();
            rab.getInteractive().endVappInfoCollection();
            /**
             * End Section VappInfoCollection
             */
        }
        return resultAction.isSuccessful();
    }

}
