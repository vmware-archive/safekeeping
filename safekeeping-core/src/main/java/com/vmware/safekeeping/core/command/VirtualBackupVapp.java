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

import com.amazonaws.SdkClientException;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.command.results.CoreResultActionPrepeareGeneration;
import com.vmware.safekeeping.core.command.results.CoreResultActionRetrieveChildren;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCloneVappConfigurationFile;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfoIdGeneration;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public class VirtualBackupVapp extends AbstractBackupFco implements IVirtualBackupEntry, IVappOperationsSupport {

    public VirtualBackupVapp(final VimConnection vimConnection, final CoreVirtualBackupOptions options,
            final Logger logger) {
        super(vimConnection, options, logger);
    }

    private boolean virtualBackupChildren(final CoreResultActionVappVirtualBackup resultAction,
            final GlobalFcoProfileCatalog globalFcoCatalog) throws CoreResultActionException {
        boolean result = false;
        try {
            final GenerationProfile profile = resultAction.getProfile();
            final ITarget target = profile.getTargetOperation().getParent();
            /**
             * Start Section DisksRestore
             */
            resultAction.getInteractive().startChildrenVirtualBackup();
            int index = 0;
            final List<Future<CoreResultActionVmVirtualBackup>> futures = new ArrayList<>();
            for (final CoreResultActionVmVirtualBackup rarChild : resultAction.getResultActionOnsChildVm()) {
                final Future<CoreResultActionVmVirtualBackup> f = submit(rarChild, globalFcoCatalog,
                        target.newTargetOperation(rarChild.getFcoEntityInfo(), this.logger));
                futures.add(f);
            }
            // A) Await all runnable to be done (blocking)
            for (final Future<CoreResultActionVmVirtualBackup> future : futures) {
                if (future != null) {
                    // get will block until the future is done
                    final CoreResultActionVmVirtualBackup racChild = future.get();
                    if (racChild != null) {
                        if (this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine(racChild.toString());
                        }
                        if (racChild.isSuccessful()) {
                            if (racChild.getGenerationInfo() != null) {
                                resultAction.getProfile().setChildSuccessfullGenerationId(index,
                                        racChild.getGenerationInfo().getGenerationId());
                            } else {
                                final String msg = "generation index is missing" + racChild.toString();
                                this.logger.warning(msg);
                            }
                            resultAction.getGenerationInfo().setBackupMode(BackupMode.FULL);
                            resultAction.getProfile().setBackupMode(BackupMode.FULL);

                        }
                        result &= racChild.isSuccessfulOrSkipped();
                    }
                    Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);

                    ++index;
                } else {
                    final String msg = "Internal error - check server logs";
                    resultAction.failure(msg);
                    result = false;
                }

            }
            result = true;

        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            resultAction.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            resultAction.getInteractive().endChildrenVirtualBackup();
            /**
             * End Section DiskRestore
             */
        }
        return result;
    }

    private boolean cloneConfigurationFiles(final CoreResultActionVappVirtualBackup rac)
            throws CoreResultActionException {

        final CoreResultActionCloneVappConfigurationFile resultAction = new CoreResultActionCloneVappConfigurationFile(
                rac);
        resultAction.start();
        try {
            final GenerationProfile dstProfile = rac.getProfile();
            final GenerationProfile srcProfile = rac.getSourceProfile();
            final ITargetOperation target = dstProfile.getTargetOperation();

            if (srcProfile.isVappConfigAvailable()) {
                resultAction.setSrcVappContentPath(srcProfile.getvAppConfigPath());
                resultAction.setDstVappContentPath(dstProfile.getvAppConfigPath());
                if (!target.doesObjectExist(resultAction.getSrcVappContentPath())) {
                    resultAction.failure(resultAction.getSrcVappContentPath() + " content doesn't exist");
                } else {
                    target.copyObject(resultAction.getSrcVappContentPath(), resultAction.getDstVappContentPath());
                }
            }
        } catch (final SdkClientException e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            resultAction.done();
        }
        return resultAction.isSuccessful();
    }

    public final CoreResultActionVappVirtualBackup virtualBackup(final FcoArchiveManager fcoArcMgr,
            final CoreResultActionVappVirtualBackup rac) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupVm() start.");
        }
        if (!getOptions().isDryRun()) {
            try {

                /**
                 * Start Section RetrieveProfile
                 */
                rac.getInteractive().startRetrieveProfile();
                if (this.logger.isLoggable(Level.INFO)) {
                    this.logger.info("restoreVm() start.");

                }
                final GlobalFcoProfileCatalog globalFcoCatalog = fcoArcMgr.getGlobalFcoProfilesCatalog();
                final CoreResultActionLoadProfile resultActionLoadProfile = actionLoadProfile(fcoArcMgr, rac);
                final CoreResultActionLoadProfile resultActionLoadSourceProfile = actionLoadProfile(fcoArcMgr, rac);
                if (resultActionLoadProfile.isSuccessful() && resultActionLoadSourceProfile.isSuccessful()) {
                    rac.setProfile(resultActionLoadProfile.getProfile());
                    rac.setSourceProfile(resultActionLoadSourceProfile.getProfile());
                    rac.setLocations(rac.getProfile());
                    rac.getInteractive().endRetrieveProfile();
                    /**
                     * End Section RetrieveProfile
                     */

                    if (generationComputation(rac, fcoArcMgr) && cloneConfigurationFiles(rac)
                            && retreiveVappChildCollection(globalFcoCatalog, rac)
                            && virtualBackupChildren(rac, globalFcoCatalog) && finalizeProfile(rac)) {
                        final String msg = rac.getFcoToString() + " Backup Success - start cleaning";
                        if (this.logger.isLoggable(Level.INFO)) {
                            this.logger.info(msg);
                        }
                    }

                }
            } catch (final IOException | NoSuchAlgorithmException e) {
                Utility.logWarning(this.logger, e);
                rac.failure(e);
            } catch (final InterruptedException e) {
                rac.failure(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("backupVm() end.");
        }
        return rac;
    }

    protected boolean generationComputation(final AbstractCoreResultActionVirtualBackupForEntityWithDisks rab,
            final FcoArchiveManager fcoArcMgr) throws CoreResultActionException {
        /**
         * Start Section GenerationComputation
         */
        rab.getInteractive().startGenerationComputation();
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info(MessagesTemplate.getLocationString(rab));
        }
        final GenerationProfile profile = rab.getProfile();
        final CoreResultActionPrepeareGeneration resultPrepareNewProfile = prepareNewGeneration(profile, rab);

        final GenerationInfo generationInfo = new GenerationInfo();
        generationInfo.setGenerationId(profile.getGenerationId());
        generationInfo.setPreviousGenerationId(profile.getPreviousGenerationId());
        generationInfo.setTargetUri(fcoArcMgr.getRepositoryTarget().getUri(profile.getGenerationPath()));
        generationInfo.setBackupMode(getOptions().getRequestedBackupMode());

        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info(MessagesTemplate.getGenerationInfo(rab));
        }
        rab.getInteractive().endGenerationComputation();
        /**
         * end Section GenerationComputation
         */

        return resultPrepareNewProfile.isSuccessful();
    }

    @Override
    public CoreVirtualBackupOptions getOptions() {
        return (CoreVirtualBackupOptions) this.options;
    }

    private boolean retreiveVappChildCollection(final GlobalFcoProfileCatalog globalFcoCatalog,
            final CoreResultActionVappVirtualBackup rac) throws CoreResultActionException, InterruptedException {
        final CoreResultActionRetrieveChildren resultAction = new CoreResultActionRetrieveChildren(rac);
        /**
         * Start Section RetreiveVappChildCollection
         */
        rac.getInteractive().startRetreiveVappChildCollection();

        try {
            resultAction.start();
            int index = 0;
            final Map<String, Integer> vappChildren = rac.getProfile().getVappChildrenUuid();

            rac.setNumberOfChildVm(vappChildren.size());
            for (final Entry<String, Integer> entrySet : vappChildren.entrySet()) {
                ++index;
                final ManagedFcoEntityInfo entityInfo = globalFcoCatalog.getEntityByUuid(entrySet.getKey(),
                        EntityType.VirtualMachine);
                resultAction.getChildren().put(entrySet.getKey(),
                        new ManagedFcoEntityInfoIdGeneration(entityInfo, entrySet.getValue()));
                final CoreVirtualBackupOptions vmOptions = new CoreVirtualBackupOptions();

                vmOptions.setGenerationId(entrySet.getValue());
                final CoreResultActionVmVirtualBackup raChildVm = new CoreResultActionVmVirtualBackup(
                        new VirtualMachineManager(getVimConnection(), entityInfo), vmOptions);
                raChildVm.setInteractive(rac.getInteractive().newVirtualBackupVmInteractiveInstance(raChildVm));
                rac.getResultActionOnsChildVm().add(raChildVm);
                raChildVm.setParent(rac);
                rac.getFcoChildren().add(entityInfo);
                raChildVm.setIndex(index);
            }
        } finally {
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
            resultAction.done();
            rac.getInteractive().endRetreiveVappChildCollection();
            /**
             * End Section RetreiveVappChildCollection
             */
        }
        return resultAction.isSuccessful();
    }

    private Future<CoreResultActionVmVirtualBackup> submit(final CoreResultActionVmVirtualBackup rarChild,
            final GlobalFcoProfileCatalog globalFcoCatalog, final ITargetOperation newTargetOperation) {
        return ThreadsManager.executor(ThreadType.FCO).submit(() -> {
            CoreResultActionVmVirtualBackup result = null;
            try {
                result = actionVirtualBackupVmEntry(newTargetOperation, globalFcoCatalog, rarChild, this.logger);
            } catch (final CoreResultActionException e) {
                Utility.logWarning(this.logger, e);
            }
            return result;
        });
    }

}
