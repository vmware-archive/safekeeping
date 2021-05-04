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
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractCheckGenerationsInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractRemoveGenerationsInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractRemoveProfileInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractStatusInteractive;
import com.vmware.safekeeping.core.command.options.CoreArchiveOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchiveStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItem;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItemsList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveIvdStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveProfile;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveShow;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVappStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVmStatus;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationDiskInfo;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationDisksInfoList;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationVirtualMachinesInfoList;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatusDiskInfo;
import com.vmware.safekeeping.core.command.results.support.StatusVirtualMachineDiskInfo;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.InfoData;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.BlockLocker;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.FcoGenerationsCatalog;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.profile.SimpleBlockInfo;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoGenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoGenerations;
import com.vmware.safekeeping.core.profile.dataclass.Generation;
import com.vmware.safekeeping.core.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.safekeeping.core.profile.ovf.SerializableVmConfigInfo;
import com.vmware.safekeeping.core.type.GeneretionDependenciesInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.ArchiveObjects;
import com.vmware.safekeeping.core.type.enums.EntityType;

public abstract class AbstractArchiveCommand extends AbstractCommandWithOptions {

    public static final int ALL_GENERATIONS = -1;

    public static final int LAST_GENERATION = -4;

    public static final int SUCCEDED_GENERATIONS = -2;

    public static final int FAILED_GENERATIONS = -3;

    protected CoreResultActionArchiveCheckGenerationWithDependencies actionCheckGenerations(final ITarget target,
            final CoreResultActionArchiveCheckGenerationWithDependencies resultAction,
            final AbstractCheckGenerationsInteractive interactive) throws CoreResultActionException {
        try {
            /**
             * start interactive
             */
            interactive.start();
            resultAction.start();
            final ManagedFcoEntityInfo entity = resultAction.getFcoEntityInfo();
            final ITargetOperation targetOperation = target.newTargetOperation(entity, this.logger);
            if (Vmbk.isAbortTriggered()) {
                resultAction.aborted();
            } else {
                FcoArchiveManager fcoArcMgr = null;
                List<Integer> generationId = null;
                try {
                    fcoArcMgr = new FcoArchiveManager(entity, targetOperation, ArchiveManagerMode.READ);
                    /**
                     * start Section RetrieveRequestedGenerations
                     */
                    interactive.startRetrieveRequestedGenerations();
                    generationId = retrieveGenerations(getOptions().getGenerationId(), fcoArcMgr, resultAction);
                    interactive.endRetrieveRequestedGenerations();
                    /**
                     * end Section RetrieveRequestedGenerations
                     */
                    if (resultAction.isRunning()) {
                        retrieveDependingGenerations(resultAction, fcoArcMgr, generationId, interactive);

                        /**
                         * start Section CheckGenerations
                         */
                        interactive.startCheckGenerations();

                        for (final AbstractCoreResultActionImpl r : resultAction.getSubOperations()
                                .toReverseArrayList()) {
                            final CoreResultActionArchiveCheckGeneration craarg = (CoreResultActionArchiveCheckGeneration) r;
                            /**
                             * start Section CheckGeneration
                             */
                            interactive.startCheckGeneration(craarg);
                            checkGeneration(fcoArcMgr, craarg, interactive);
                            interactive.endCheckGeneration();
                            /**
                             * end Section CheckGeneration
                             */
                        }
                        interactive.endCheckGenerations();
                        /**
                         * end Section CheckGenerations
                         */

                    }
                } catch (final Exception e) {
                    final String msg = String.format("checkGeneration of %s failed.", entity.getUuid());
                    this.logger.warning(msg);
                    Utility.logWarning(this.logger, e);
                }
            }
        } finally {
            resultAction.done();
        }
        return resultAction;
    }

    protected CoreResultActionArchiveItemsList actionList(final ITarget target,
            final CoreResultActionArchiveItemsList craal) throws CoreResultActionException {
        try {
            craal.start();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target);
            final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
            final float percIncrement = Utility.ONE_HUNDRED_PER_CENT / entities.size();
            for (final ManagedFcoEntityInfo entity : entities) {
                craal.progressIncrease(percIncrement);
                final CoreResultActionArchiveItem resultAction = new CoreResultActionArchiveItem(target);
                try {
                    resultAction.start();
                    resultAction.setFcoEntityInfo(entity);
                    if (Vmbk.isAbortTriggered()) {
                        resultAction.aborted();
                        craal.aborted();
                        break;
                    } else {
                        if (insertInfoData(resultAction, target)) {
                            craal.getItems().add(resultAction);
                        }
                    }
                } finally {
                    resultAction.done();
                }
            }
        } catch (final Exception e) {
            Utility.logWarning(this.logger, e);
            craal.failure(e);
        } finally {
            craal.done();
        }
        return craal;
    }

    public CoreResultActionArchiveRemoveGenerationWithDependencies actionRemoveGenerations(final ITarget target,
            final CoreResultActionArchiveRemoveGenerationWithDependencies resultAction,
            final AbstractRemoveGenerationsInteractive interactive) throws CoreResultActionException {
        try {
            resultAction.start();
            /**
             * start interactive
             */
            interactive.start();
            final ManagedFcoEntityInfo entity = resultAction.getFcoEntityInfo();
            final ITargetOperation targetOperation = target.newTargetOperation(entity, this.logger);
            if (Vmbk.isAbortTriggered()) {
                resultAction.aborted();
            } else {
                /**
                 * start Section AccessArchive
                 */
                interactive.startAccessArchive();
                List<Integer> generationId = null;
                FcoArchiveManager fcoArcMgr = null;
                try {
                    fcoArcMgr = new FcoArchiveManager(entity, targetOperation, ArchiveManagerMode.READ);
                    interactive.endAccessArchive();
                    /**
                     * end Section AccessArchive
                     */
                    /**
                     * start Section RetrieveRequestedGenerations
                     */
                    interactive.startRetrieveRequestedGenerations();
                    generationId = retrieveGenerations(getOptions().getGenerationId(), fcoArcMgr, resultAction);
                    interactive.endRetrieveRequestedGenerations();
                    /**
                     * end Section RetrieveRequestedGenerations
                     */
                    if (!generationId.isEmpty()) {
                        retrieveDependingGenerations(resultAction, fcoArcMgr, generationId, interactive);

                        /**
                         * start Section DeleteGenerations
                         */
                        interactive.startDeleteGenerations();
                        for (final AbstractCoreResultActionImpl r : resultAction.getSubOperations()
                                .toReverseArrayList()) {
                            final CoreResultActionArchiveRemoveGeneration craarg = (CoreResultActionArchiveRemoveGeneration) r;
                            deleteGeneration(fcoArcMgr, craarg, interactive);
                        }
                        interactive.endDeleteGenerations();
                        /**
                         * end Section DeleteGenerations
                         */
                        if (resultAction.getChildrenActionsResult() == OperationState.SUCCESS) {
                            /**
                             * start Section UpdateFcoProfilesCatalog
                             */
                            interactive.startUpdateFcoProfilesCatalog();
                            if (!fcoArcMgr.postGenerationsCatalog()) {
                                resultAction.failure("Update Profile failed");

                            } else {
                                interactive.endUpdateFcoProfilesCatalog();
                                /**
                                 * end Section UpdateFcoProfilesCatalog
                                 */
                            }
                        }
                    }
                } catch (final Exception e) {
                    Utility.logWarning(this.logger, e);
                    resultAction.failure(e);
                }
            }
            return resultAction;
        } finally {
            resultAction.done();
            interactive.finish();
            /**
             * end interactive
             */
        }
    }

    protected CoreResultActionArchiveRemoveProfile actionRemoveProfile(final ITarget target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionArchiveRemoveProfile aarp,
            final AbstractRemoveProfileInteractive interactive) throws CoreResultActionException {
        try {
            aarp.start();
            /**
             * start interactive
             */
            interactive.start();
            final ManagedFcoEntityInfo fcoInfo = aarp.getFcoEntityInfo();
            final ITargetOperation targetOperation = target.newTargetOperation(fcoInfo, this.logger);
            if (Vmbk.isAbortTriggered()) {
                aarp.aborted();
            } else {
                if (!getOptions().isDryRun()) {
                    try {

                        /**
                         * start Section AccessArchive
                         */
                        interactive.startAccessArchive();
                        final FcoArchiveManager fcoArcMgr = new FcoArchiveManager(fcoInfo, targetOperation,
                                ArchiveManagerMode.READ);
                        interactive.endAccessArchive();
                        /**
                         * end Section AccessArchive
                         */
                        /**
                         * start Section RetrieveRequestedGenerations
                         */
                        interactive.startRetrieveGenerations();
                        final List<Integer> generationsList = fcoArcMgr.getGenerationIdList();
                        interactive.endRetrieveGenerations();
                        /**
                         * end Section RetrieveRequestedGenerations
                         */

                        removeGeneration(fcoArcMgr, generationsList, interactive);

                        /**
                         * start Section RemoveFcoProfile
                         */
                        interactive.startRemoveFcoProfileContent();
                        if (targetOperation.removeFcoProfile(fcoInfo)) {
                            /**
                             * end Section RemoveFcoProfile
                             */
                            interactive.endRemoveFcoProfileContent();
                            /**
                             * start Section RemoveFcoProfile
                             */
                            interactive.startRemoveFcoProfileMetadata();
                            if (globalFcoCatalog.removeProfile(fcoInfo) != null) {
                                /**
                                 * end Section RemoveFcoProfile
                                 */
                                interactive.endRemoveFcoProfileMetadata();
                                /**
                                 * start Section UpdateFcoProfilesCatalog
                                 */
                                interactive.startUpdateFcoProfilesCatalog();
                                if (globalFcoCatalog.updateFcoProfileCatalog()) {
                                    if (this.logger.isLoggable(Level.INFO)) {
                                        final String msg = String.format("Profile: %s removed", fcoInfo.toString());
                                        this.logger.info(msg);
                                    }
                                    /**
                                     * end Section UpdateFcoProfilesCatalog
                                     */
                                    interactive.endUpdateFcoProfilesCatalog();
                                }
                            } else {
                                aarp.failure("Failed to remove from global catalog");
                            }
                        } else {
                            aarp.failure();
                        }
                    } catch (final IOException | NoSuchAlgorithmException | ArchiveException e) {
                        Utility.logWarning(this.logger, e);
                        aarp.failure(e);
                    }

                }
            }
        } finally {
            aarp.done();
            /**
             * end interactive
             */
            interactive.finish();
        }
        return aarp;
    }

    public CoreResultActionArchiveShow actionShow(final ITarget target, final CoreResultActionArchiveShow resultAction)
            throws CoreResultActionException {
        try {
            byte[] reportContent = null;
            resultAction.start();
            resultAction.setArchiveObject(getOptions().getShow());
            final ManagedFcoEntityInfo entity = resultAction.getFcoEntityInfo();
            final ITargetOperation targetOperation = target.newTargetOperation(entity, this.logger);
            if (Vmbk.isAbortTriggered()) {
                resultAction.aborted();
            } else {
                Integer generationId = null;
                FcoArchiveManager fcoArcMgr = null;
                try {
                    fcoArcMgr = new FcoArchiveManager(entity, targetOperation, ArchiveManagerMode.READ);
                    if (getOptions().getShow() == ArchiveObjects.GLOBALPROFILE) {
                        if (getOptions().isPrettyJason()) {
                            resultAction.setContent(
                                    toPrettyJason(target.getGlobalProfileToByteArray(), GlobalFcoProfileCatalog.class));
                        } else {
                            resultAction.setContent(new String(target.getGlobalProfileToByteArray()));
                        }
                    } else if (getOptions().getShow() == ArchiveObjects.FCOPROFILE) {
                        if (getOptions().isPrettyJason()) {
                            resultAction.setContent(toPrettyJason(targetOperation.getFcoProfileToByteArray(entity),
                                    FcoGenerations.class));
                        } else {
                            resultAction.setContent(new String(targetOperation.getFcoProfileToByteArray(entity)));
                        }

                    } else {
                        switch (getOptions().getGenerationId().size()) {
                        case 0:
                            generationId = LAST_GENERATION;
                            break;
                        case 1:
                            generationId = getOptions().getGenerationId().get(0);
                            break;
                        default:
                            resultAction.failure("Multiple generation not allowed");
                            break;
                        }
                        if (resultAction.isRunning()) {
                            Integer genId = null;
                            if ((genId = retrieveGenerations(generationId, fcoArcMgr, resultAction)) != null) {
                                final GenerationProfile profile = fcoArcMgr.loadProfileGeneration(genId);
                                if ((profile != null) && profile.isProfileValid()) {
                                    switch (getOptions().getShow()) {
                                    case GENERATIONPROFILE:
                                        if (getOptions().isPrettyJason()) {
                                            resultAction.setContent(toPrettyJason(
                                                    targetOperation.getGenerationProfileToByteArray(
                                                            profile.getFcoEntity(), genId),
                                                    FcoGenerationProfile.class));
                                        } else {
                                            resultAction.setContent(new String(targetOperation
                                                    .getGenerationProfileToByteArray(profile.getFcoEntity(), genId)));
                                        }

                                        break;
                                    case MD5FILE:
                                        if (targetOperation.doesObjectExist(profile.getMd5ContentPath())) {
                                            resultAction.setContent(
                                                    targetOperation.getObjectAsString(profile.getMd5ContentPath()));
                                        } else {
                                            resultAction.failure(profile.getMd5ContentPath() + " doesn't exist.");
                                        }
                                        break;
                                    case REPORTFILE:
                                        final StringBuilder reports = new StringBuilder();
                                        for (int diskId = 0; diskId < profile.getNumberOfDisks(); diskId++) {
                                            if (targetOperation.doesObjectExist(profile.getReportContentPath(diskId))) {
                                                reports.append(targetOperation
                                                        .getObjectAsString(profile.getReportContentPath(diskId)));
                                            } else {
                                                reports.append("Report for Disk:");
                                                reports.append(diskId);
                                                reports.append(" is missing.\n");

                                            }

                                        }
                                        resultAction.setContent(reports.toString());
                                        break;
                                    case VMXFILE:
                                        if (resultAction.getFcoEntityInfo()
                                                .getEntityType() == EntityType.VirtualMachine) {
                                            String st;
                                            if ((st = targetOperation
                                                    .getObjectAsString(profile.getVmxContentPath())) == null) {
                                                resultAction.failure(profile.getVmxContentPath() + " doesn't exist.");
                                            } else {
                                                resultAction.setContent(st);
                                            }
                                        } else {
                                            resultAction.failure("Unsupported Object");
                                        }
                                        break;
                                    case VAPPCONFIG:
                                        if ((reportContent = targetOperation
                                                .getObject(profile.getvAppConfigPath())) == null) {
                                            resultAction.failure("Doesn't exist");
                                        } else {
                                            if (targetOperation.doesObjectExist(profile.getvAppConfigPath())) {
                                                final ObjectMapper mapper = new ObjectMapper();
                                                switch (resultAction.getFcoEntityInfo().getEntityType()) {
                                                case VirtualApp:
                                                    final SerializableVAppConfigInfo vAppConfigInfo = new ObjectMapper()
                                                            .readValue(reportContent, SerializableVAppConfigInfo.class);

                                                    resultAction.setContent(mapper.writerWithDefaultPrettyPrinter()
                                                            .writeValueAsString(vAppConfigInfo));
                                                    break;
                                                case VirtualMachine:
                                                    final SerializableVmConfigInfo vmConfigInfo = new ObjectMapper()
                                                            .readValue(reportContent, SerializableVmConfigInfo.class);

                                                    resultAction.setContent(mapper.writerWithDefaultPrettyPrinter()
                                                            .writeValueAsString(vmConfigInfo));
                                                    break;
                                                default:
                                                    resultAction.failure("Unsupported Object");
                                                    break;

                                                }
                                            } else {
                                                resultAction.failure(profile.getvAppConfigPath() + " doesn't exist.");
                                            }
                                        }
                                        break;
                                    default:
                                        resultAction.failure("Unsupported Object");
                                        break;
                                    }

                                } else {
                                    resultAction.failure("Profile is empty");
                                }
                            }
                        }
                    }
                } catch (final IOException | ArchiveException e) {
                    Utility.logWarning(this.logger, e);
                    resultAction.failure(e);
                }
            }

            return resultAction;
        } finally {
            resultAction.done();
        }
    }

    protected AbstractCoreResultActionArchiveStatus actionStatus(final ITarget target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final AbstractCoreResultActionArchiveStatus resultAction,
            final AbstractStatusInteractive interactive) throws CoreResultActionException {
        try {

            resultAction.start();
            /**
             * start interactive
             */
            interactive.start();
            final ManagedFcoEntityInfo fcoEntity = resultAction.getFcoEntityInfo();
            final ITargetOperation targetOperation = target.newTargetOperation(fcoEntity, this.logger);
            if (Vmbk.isAbortTriggered()) {
                resultAction.aborted();

            } else {
                if (!getOptions().isDryRun()) {
                    /**
                     * start Section AccessArchive
                     */
                    interactive.startAccessArchive();
                    final FcoArchiveManager fcoManager = new FcoArchiveManager(resultAction.getFcoEntityInfo(),
                            targetOperation, ArchiveManagerMode.READ);

                    interactive.endAccessArchive();
                    /**
                     * end Section AccessArchive
                     */
                    /**
                     * start Section RetrieveGenerations
                     */
                    interactive.startRetrieveGenerations();
                    resultAction.setAvailable(
                            globalFcoCatalog.existProfileWithUuid(resultAction.getFcoEntityInfo().getUuid()));
                    // if no generation are specified by default select the last available
                    // generation

                    final LinkedList<Integer> generations = new LinkedList<>();
                    if (getOptions().getGenerationId().isEmpty()) {
                        generations.add(LAST_GENERATION);
                    } else {
                        generations.addAll(getOptions().getGenerationId());
                    }
                    resultAction.getGenerationList().addAll(retrieveGenerations(generations, fcoManager, resultAction));

                    resultAction.setNumOfGeneration(resultAction.getGenerationList().size());
                    resultAction.setLatestSucceededGenerationId(
                            fcoManager.getLatestSucceededGenerationId(resultAction.getGenerationList()));

                    resultAction.setNumOfSuccceededGeneration(fcoManager.getGenerationsCatalog()
                            .getNumOfSuccceededGeneration(resultAction.getGenerationList()));

                    /**
                     * end Section RetrieveGenerations
                     */
                    interactive.endRetrieveGenerations();

                    /**
                     * start Section RetrieveGenerationsInfo
                     */
                    interactive.startRetrieveGenerationsInfo();
                    for (final Integer genId : resultAction.getGenerationList()) {
                        /**
                         * start Section RetrieveGenerationInfo
                         */
                        interactive.startRetrieveGenerationInfo();
                        switch (fcoEntity.getEntityType()) {
                        case ImprovedVirtualDisk:
                            final CoreGenerationDiskInfo genDiskInfo = new CoreGenerationDiskInfo(genId,
                                    fcoManager.getGenerationsCatalog().isGenerationSucceeded(genId),
                                    fcoEntity.getEntityType(),
                                    fcoManager.getGenerationsCatalog().getTimestampMs(genId));
                            ((CoreResultActionArchiveIvdStatus) resultAction).getGenerationDiskInfo().put(genId,
                                    genDiskInfo);
                            if (genDiskInfo.isGenerationSucceeded()) {
                                final GenerationProfile profGen = fcoManager.loadProfileGeneration(genId);
                                if ((profGen != null) && profGen.isProfileValid()) {
                                    genDiskInfo.setDisksInfo(new StatusDiskInfo(profGen));
                                }
                            }
                            break;
                        case K8sNamespace:
                            break;
                        case VirtualApp:
                            final CoreGenerationVirtualMachinesInfoList genVmsInfo = new CoreGenerationVirtualMachinesInfoList(
                                    genId, fcoManager.getGenerationsCatalog().isGenerationSucceeded(genId),
                                    fcoEntity.getEntityType(),
                                    fcoManager.getGenerationsCatalog().getTimestampMs(genId));
                            ((CoreResultActionArchiveVappStatus) resultAction).getGenerationVmInfoList().put(genId,
                                    genVmsInfo);
                            if (fcoManager.getGenerationsCatalog().isGenerationSucceeded(genId)) {
                                final GenerationProfile profGen = fcoManager.loadProfileGeneration(genId);
                                if ((profGen != null) && profGen.isProfileValid()) {
                                    final Map<String, Integer> vappChildren = profGen.getVappChildrenUuid();
                                    for (final String uuid : vappChildren.keySet()) {
                                        genVmsInfo.getVmsInfoList()
                                                .add(globalFcoCatalog.getEntityByUuid(uuid, EntityType.VirtualMachine));
                                    }
                                }
                            }
                            break;
                        case VirtualMachine:
                            final CoreGenerationDisksInfoList genDisksInfo = new CoreGenerationDisksInfoList(genId,
                                    fcoManager.getGenerationsCatalog().isGenerationSucceeded(genId),
                                    fcoEntity.getEntityType(),
                                    fcoManager.getGenerationsCatalog().getTimestampMs(genId));
                            ((CoreResultActionArchiveVmStatus) resultAction).getGenerationDisksInfoList().put(genId,
                                    genDisksInfo);
                            if (genDisksInfo.isGenerationSucceeded()) {
                                final GenerationProfile profGen = fcoManager.loadProfileGeneration(genId);
                                if ((profGen != null) && profGen.isProfileValid()) {
                                    for (int diskId = 0; diskId < profGen.getNumberOfDisks(); diskId++) {
                                        genDisksInfo.getDisksInfoList()
                                                .add(new StatusVirtualMachineDiskInfo(diskId, profGen));
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                        }
                        /**
                         * end Section RetrieveGenerationInfo
                         */
                        interactive.endRetrieveGenerationInfo(genId);
                    }
                    /**
                     * end Section RetrieveGenerationsInfo
                     */
                    interactive.endRetrieveGenerationsInfo();
                }
            }
        } catch (final Exception e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        } finally {
            resultAction.done();
        }

        return resultAction;
    }

    /**
     * @param target
     * @param fcoArcMgr
     * @param craarg
     * @param interactive
     * @return
     * @throws CoreResultActionException
     */
    private CoreResultActionArchiveCheckGeneration checkGeneration(final FcoArchiveManager fcoArchMgr,
            final CoreResultActionArchiveCheckGeneration resultAction,
            final AbstractCheckGenerationsInteractive interactive) throws CoreResultActionException {
        final ITargetOperation target = fcoArchMgr.getRepositoryTarget();
        String msg;
        try {
            resultAction.start();
            final int genId = resultAction.getGenId();
            /**
             * start Section LoadProfileGeneration
             */
            interactive.startLoadProfileGeneration();
            final GenerationProfile profile = fcoArchMgr.loadProfileGeneration(genId);

            if ((profile != null) && profile.isProfileValid() && profile.isSucceeded()) {
                resultAction.setTimestampMs(profile.getTimestamp().getTime());
                boolean ret = true;
                resultAction.setNumOfFiles(profile.getNumberOfDumps());
                if (this.logger.isLoggable(Level.FINE)) {
                    msg = String.format("\tGeneration %d Blocks %d.", genId, profile.getNumberOfDumps());
                    this.logger.fine(msg);
                }
                /**
                 * end Section LoadProfileGeneration
                 */
                interactive.endLoadProfileGeneration();
                for (final DiskProfile disk : profile.getDisks()) {
                    final StringBuilder errorReason = new StringBuilder();
                    for (final Entry<Integer, SimpleBlockInfo> entry : disk.getDumps().entrySet()) {
                        /**
                         * start Section CheckFile
                         */
                        interactive.startCheckFile(entry.getKey());
                        final ExBlockInfo block = new ExBlockInfo(entry.getValue(), disk.getDumps().size(),
                                target.getDisksPath());
                        if (target.doesKeyExist(block)) {
                            final byte[] digest = target.getObjectMd5(block.getDataKey());
                            final String md5 = new String(digest);
                            resultAction.getMd5fileCheck().add(entry.getKey(), md5.equalsIgnoreCase(block.getMd5()));
                        } else {
                            resultAction.getMd5fileCheck().add(entry.getKey(), false);
                            msg = String.format("Generation %d disk %d block %s check failed: Doesn't exist", genId,
                                    disk.getDiskId(), block.getKey());
                            this.logger.warning(msg);
                            errorReason.append(msg);
                            errorReason.append('\n');

                            ret &= false;
                        }
                        interactive.endCheckFile(entry.getKey());
                        /**
                         * end Section CheckFile
                         */

                    }
                    if (this.logger.isLoggable(Level.INFO)) {
                        msg = String.format("Generation %d files checksum check completed %s.", genId,
                                (ret) ? "succesfully" : "with errors");
                        this.logger.info(msg);
                    }
                    if (ret) {
                        if (this.logger.isLoggable(Level.INFO)) {
                            msg = String.format("Generation %d files checksum check completed  succesfully", genId);
                            this.logger.info(msg);
                        }
                        resultAction.done();
                    } else {
                        resultAction.failure(errorReason.toString());
                        msg = String.format("Generation %d files checksum check completed  with Errors", genId);
                        this.logger.warning(msg);
                    }
                    /**
                     * end Section CheckFiles
                     */
                    interactive.endCheckFiles();

                }
            } else {
                msg = String.format("Generation %d is marked FAILED.", genId);
                resultAction.failure(msg);
                this.logger.warning(msg);
            }
        } catch (final Exception e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            resultAction.done();
        }
        return resultAction;

    }

    private CoreResultActionArchiveRemoveGeneration deleteGeneration(final FcoArchiveManager fcoArchMgr,
            final CoreResultActionArchiveRemoveGeneration craarg,
            final AbstractRemoveGenerationsInteractive interactive) throws CoreResultActionException {
        String msg;
        try {

            craarg.start();
            /**
             * start Section DeleteGeneration
             */
            interactive.startDeleteGeneration(craarg);
            final int genId = craarg.getGenId();
            final GenerationProfile profGen = fcoArchMgr.loadProfileGeneration(genId);
            if ((profGen != null) && profGen.isProfileValid()) {
                craarg.setTimestampMs(profGen.getTimestamp().getTime());
                interactive.startDeleteData();
                craarg.setGenerationDataRemoved(removeGeneration(profGen));
                if (craarg.isGenerationDataRemoved()) {
                    if (this.logger.isLoggable(Level.INFO)) {
                        msg = String.format("%s %s generation %d removed.", fcoArchMgr.getEntityType().toString(),
                                fcoArchMgr.getName(), genId);
                        this.logger.info(msg);
                    }
                } else {
                    msg = String.format("%s %s generation %d removing failed.", fcoArchMgr.getEntityType().toString(),
                            fcoArchMgr.getName(), genId);
                    this.logger.warning(msg);
                    craarg.failure(msg);
                }
                interactive.endDeleteData();

            } else {
                msg = String.format("%s %s generation %d  profile already removed.",
                        fcoArchMgr.getEntityType().toString(), fcoArchMgr.getName(), genId);
                this.logger.warning(msg);
                craarg.skip(msg);
            }
        } catch (final Exception e) {
            craarg.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            removeProfileGeneration(craarg, fcoArchMgr.getGenerationsCatalog(), interactive);
            craarg.done();
            interactive.endDeleteGeneration();
            /**
             * end Section DeleteGeneration
             */
        }
        return craarg;
    }

    /**
     * retrieve any generation that depends from the specified generation
     *
     * @param fcoArchMgr
     * @param genId      queried generation
     * @return List of generations
     */
    private List<GeneretionDependenciesInfo> getDependingGenerations(final FcoArchiveManager fcoArchMgr,
            final int genId) {
        final LinkedList<GeneretionDependenciesInfo> result = new LinkedList<>();
        final GeneretionDependenciesInfo elem = new GeneretionDependenciesInfo();
        elem.setGenId(genId);
        final FcoGenerationsCatalog fcoProfile = fcoArchMgr.getGenerationsCatalog();
        final Generation generation = fcoProfile.getGeneration(genId);
        elem.setExist(generation != null);
        if (elem.isExist()) {
            elem.setMode(generation.getBackupMode());
            elem.setDependOnGenerationId(generation.getDependingOnGenerationId());
            elem.setDependingGenerationId(fcoArchMgr.getDependingGenerationId(genId));
            if (elem.hasDependency()) {
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Generation %d depend on %d. Loading generation %d", genId,
                            elem.getDependingGenerationId().getGenId(), genId);
                    this.logger.info(msg);
                }
                result.addAll(getDependingGenerations(fcoArchMgr, elem.getDependingGenerationId().getGenId()));
            }
            result.add(elem);
        }
        return result;
    }

    /**
     * @return the options
     */
    @Override
    public CoreArchiveOptions getOptions() {
        return (CoreArchiveOptions) this.options;
    }

    /**
     * retrieve any generation that the queried generation depends
     *
     * @param fcoArchMgr
     * @param genId      queried generation
     * @return List of generations
     */
    private List<GeneretionDependenciesInfo> getParentGenerations(final FcoArchiveManager fcoArchMgr, final int genId) {
        final LinkedList<GeneretionDependenciesInfo> result = new LinkedList<>();
        final GeneretionDependenciesInfo elem = new GeneretionDependenciesInfo();
        elem.setGenId(genId);
        final FcoGenerationsCatalog fcoProfile = fcoArchMgr.getGenerationsCatalog();
        final Generation generation = fcoProfile.getGeneration(genId);
        elem.setExist(generation != null);
        if (elem.isExist()) {
            elem.setMode(generation.getBackupMode());
            elem.setDependOnGenerationId(generation.getDependingOnGenerationId());
            elem.setDependingGenerationId(fcoArchMgr.getDependingGenerationId(genId));
            if (elem.isDependingOn()) {
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Loading generation %d (dependent from %d)",
                            elem.getDependOnGenerationId(), genId);
                    this.logger.info(msg);
                }
                result.addAll(getParentGenerations(fcoArchMgr, elem.getDependOnGenerationId()));
            }
            result.add(elem);
        }
        return result;
    }

    private boolean insertInfoData(final CoreResultActionArchiveItem resultAction, final ITarget target) {
        try {
            FcoArchiveManager fcoArchMgr = null;
            final ManagedFcoEntityInfo entity = resultAction.getFcoEntityInfo();
            fcoArchMgr = new FcoArchiveManager(entity, target.newTargetOperation(entity, this.logger),
                    ArchiveManagerMode.READ);
            final InfoData data = new InfoData(fcoArchMgr);

            final long tsNow = Calendar.getInstance().getTimeInMillis();
            if (getOptions().getDateTimeFilter() != null) {
                if (isSatisfyTime(data.getTimestampMsOfLatestSucceededGenerationId(), tsNow)) {
                    resultAction.setInfo(data);
                }
            } else {
                resultAction.setInfo(data);
            }

        } catch (final IOException | ArchiveException e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        }
        return resultAction.getInfo() != null;
    }

    private boolean isSatisfyTime(final long tgtMs, final long nowMs) {
        boolean isPass = false;
        long baseMs;
        if (getOptions().getDateTimeFilter() != null) {
            baseMs = nowMs - Math.abs(getOptions().getDateTimeFilter());

            if (getOptions().getDateTimeFilter() >= 0) {
                if (baseMs <= tgtMs) {
                    isPass = true;
                }
            } else {
                if ((getOptions().getDateTimeFilter() < 0) && (baseMs >= tgtMs)) {
                    isPass = true;
                }
            }

        }
        return isPass;
    }

    private int removeGeneration(final FcoArchiveManager fcoArcMgr, final List<Integer> generationsList,
            final AbstractRemoveProfileInteractive interactive) {
        int removed = 0;
        for (final Integer genId : generationsList) {
            /**
             * start Section RemoveGeneration
             */
            interactive.startRemoveGeneration(genId);
            GenerationProfile profile;
            try {
                profile = fcoArcMgr.loadProfileGeneration(genId);

                if ((profile != null) && profile.isProfileValid() && removeGeneration(profile)) {
                    removed++;
                }
            } catch (final IOException e) {
                Utility.logWarning(this.logger, e);
            }
            /**
             * end Section RemoveGeneration
             */
            interactive.endRemoveGeneration(genId);
        }
        return removed;
    }

    public boolean removeGeneration(final GenerationProfile profile) {
        boolean result = true;

        final AtomicInteger removedKeys = new AtomicInteger(0);
        final AtomicInteger updateKeys = new AtomicInteger(0);
        final ITargetOperation targetOperation = profile.getTargetOperation();
        try {
            final List<Future<Boolean>> futures = new ArrayList<>();
            for (final DiskProfile disk : profile.getDisks()) {
                for (final Entry<Integer, SimpleBlockInfo> entry : disk.getDumps().entrySet()) {
                    final ExBlockInfo dumpFileInfo = new ExBlockInfo(entry.getValue(), disk.getDumps().size(),
                            targetOperation.getDisksPath());
                    final Future<Boolean> f = submit(dumpFileInfo, removedKeys, updateKeys, profile);
                    futures.add(f);
                }

            }
            // A) Await all runnable to be done (blocking)
            for (final Future<Boolean> future : futures) {
                // get will block until the future is done
                final Boolean itemResult = future.get();
                if (itemResult != null) {

                    result &= itemResult;
                } else {
                    result = false;
                }
            }

            if (this.logger.isLoggable(Level.INFO)) {
                final String msg = String.format("Total Removed:%d Updated:%d", removedKeys.get(), updateKeys.get());
                this.logger.info(msg);
            }
        } catch (final InterruptedException e) {

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {

            Utility.logWarning(this.logger, e);
        }

        result &= targetOperation.deleteFolder(profile.getGenerationPath());
        return result;
    }

    void removeProfileGeneration(final CoreResultActionArchiveRemoveGeneration resultAction,
            final FcoGenerationsCatalog fcoProfile, final AbstractRemoveGenerationsInteractive interactive)
            throws CoreResultActionException {
        String msg;
        interactive.startRemoveProfileGeneration();
        final int genId = resultAction.getGenId();
        final FcoArchiveManager fcoArchMgr = fcoProfile.getFcoArchiveManager();
        try {
            if (fcoProfile.isGenerationExist(genId)) {
                if (fcoProfile.removeGeneration(genId) != null) {
                    resultAction.setGenerationProfileMetadataRemoved(true);
                    if (this.logger.isLoggable(Level.INFO)) {
                        msg = String.format("%s %s generation %d profile entry deleted.",
                                fcoArchMgr.getEntityType().toString(), fcoArchMgr.getName(), genId);
                        this.logger.info(msg);
                    }
                } else {
                    msg = String.format("%s %s generation %d delete profile entry failed.",
                            fcoArchMgr.getEntityType().toString(), fcoArchMgr.getName(), genId);
                    this.logger.warning(msg);
                    resultAction.failure(msg);
                }
            }
        } finally {
            interactive.endRemoveProfileGeneration();
        }
    }

    private boolean retrieveDependingGenerations(
            final CoreResultActionArchiveCheckGenerationWithDependencies resultAction,
            final FcoArchiveManager fcoArcMgr, final List<Integer> generationId,
            final AbstractCheckGenerationsInteractive interactive) {
        final ITarget target = fcoArcMgr.getTarget();
        /**
         * start Section RetrieveDependingGenerations
         */
        interactive.startRetrieveDependingGenerations();
        final List<Integer> alreadyListed = new LinkedList<>();
        for (final Integer genId : generationId) {
            final List<GeneretionDependenciesInfo> depends = getParentGenerations(fcoArcMgr, genId);
            final ListIterator<GeneretionDependenciesInfo> listIterator = depends.listIterator(depends.size());
            final List<GeneretionDependenciesInfo> dependents = new LinkedList<>();
            while (listIterator.hasPrevious()) {
                final GeneretionDependenciesInfo dependent = listIterator.previous();
                if (!alreadyListed.contains(dependent.getGenId())) {
                    final CoreResultActionArchiveCheckGeneration craarg = new CoreResultActionArchiveCheckGeneration(
                            target, resultAction);
                    craarg.setGenId(dependent.getGenId());
                    craarg.setDependenciesInfo(dependent);
                    craarg.getDependents().addAll(dependents);
                    dependents.add(dependent);
                    alreadyListed.add(dependent.getGenId());
                }
            }
        }
        interactive.endRetrieveDependingGenerations();
        /**
         * end Section RetrieveDependingGenerations
         */
        return true;

    }

    private boolean retrieveDependingGenerations(
            final CoreResultActionArchiveRemoveGenerationWithDependencies resultAction,
            final FcoArchiveManager fcoArcMgr, final List<Integer> generationId,
            final AbstractRemoveGenerationsInteractive interactive) {
        final ITarget target = fcoArcMgr.getTarget();
        /**
         * start Section RetrieveDependingGenerations
         */
        interactive.startRetrieveDependingGenerations();
        for (final Integer genId : generationId) {
            final List<GeneretionDependenciesInfo> depends = getDependingGenerations(fcoArcMgr, genId);
            final ListIterator<GeneretionDependenciesInfo> listIterator = depends.listIterator(depends.size());
            final List<GeneretionDependenciesInfo> dependents = new LinkedList<>();
            while (listIterator.hasPrevious()) {
                final GeneretionDependenciesInfo dependent = listIterator.previous();

                final CoreResultActionArchiveRemoveGeneration craarg = new CoreResultActionArchiveRemoveGeneration(
                        target, resultAction);
                craarg.setGenId(dependent.getGenId());
                craarg.setDependenciesInfo(dependent);
                craarg.getDependents().addAll(dependents);
                dependents.add(dependent);
            }
        }
        interactive.endRetrieveDependingGenerations();
        /**
         * end Section RetrieveDependingGenerations
         */
        return true;
    }

    private Integer retrieveGenerations(final Integer requesterGenerationId, final FcoArchiveManager fcoArcMgr,
            final ICoreResultAction resultAction) throws CoreResultActionException {
        Integer generationId = null;
        if (requesterGenerationId == null) {
            if (fcoArcMgr.getLatestSucceededGenerationId() < 0) {
                final String msg = "No archive available.";
                this.logger.warning(msg);
                resultAction.failure(msg);
            }
            generationId = fcoArcMgr.getLatestSucceededGenerationId();
        } else {

            switch (requesterGenerationId) {
            case ALL_GENERATIONS:
            case FAILED_GENERATIONS:
                resultAction.failure("Multiple generation not allowed");
                break;
            case SUCCEDED_GENERATIONS:
                generationId = fcoArcMgr.getLatestSucceededGenerationId();

                if (generationId == -2) {
                    final String msg = "No suceeded last generation available";
                    this.logger.warning(msg);
                    resultAction.failure(msg);
                    generationId = null;
                }
                break;
            case LAST_GENERATION:
                generationId = fcoArcMgr.getLatestGenerationId();
                if (generationId == -2) {
                    final String msg = "No last generation available";
                    this.logger.warning(msg);
                    resultAction.failure(msg);
                    generationId = null;
                }
                break;
            default:
                generationId = requesterGenerationId;
                break;
            }

        }
        return generationId;
    }

    private List<Integer> retrieveGenerations(final List<Integer> requesterGenerationId,
            final FcoArchiveManager fcoArcMgr, final ICoreResultAction resultAction) throws CoreResultActionException {
        final List<Integer> generationId = new LinkedList<>(requesterGenerationId);
        String msg;
        if (generationId.isEmpty()) {
            if (fcoArcMgr.getLatestSucceededGenerationId() < 0) {
                msg = "No archive available.";
                resultAction.failure(msg);
                this.logger.warning(msg);
            }
            generationId.add(fcoArcMgr.getLatestSucceededGenerationId());
        } else {
            if (generationId.size() == 1) {

                switch (generationId.get(0)) {
                case ALL_GENERATIONS:
                    generationId.clear();
                    generationId.addAll(fcoArcMgr.getGenerationIdList());
                    if (generationId.isEmpty()) {
                        msg = "No archive available.";
                        resultAction.failure(msg);
                        this.logger.warning(msg);
                    }
                    break;
                case SUCCEDED_GENERATIONS:
                    generationId.clear();
                    final int latestSucceededGenerationId = fcoArcMgr.getLatestSucceededGenerationId();
                    generationId.add(latestSucceededGenerationId);
                    if ((generationId.isEmpty()) || (latestSucceededGenerationId < 0)) {
                        msg = "No suceeded last generation available";
                        resultAction.failure(msg);
                        this.logger.warning(msg);
                    }
                    break;
                case FAILED_GENERATIONS:
                    generationId.clear();
                    generationId.addAll(fcoArcMgr.getFailedGenerationList());
                    if (generationId.isEmpty()) {
                        msg = "No failed generations available";
                        resultAction.failure(msg);
                        this.logger.warning(msg);
                    }
                    break;
                case LAST_GENERATION:

                    generationId.clear();
                    generationId.add(fcoArcMgr.getLatestGenerationId());
                    if (generationId.isEmpty()) {
                        msg = "No last generation available";
                        resultAction.failure(msg);
                        this.logger.warning(msg);
                    }
                    break;
                default:
                    break;
                }

            }
        }

        Collections.sort(generationId);
        return generationId;
    }

    protected Future<Boolean> submit(final ExBlockInfo dumpFileInfo, final AtomicInteger removedKeys,
            final AtomicInteger updateKeys, final GenerationProfile profile) {
        return ThreadsManager.executor(ThreadType.ARCHIVE).submit(() -> {
            final ITargetOperation targetOperation = profile.getTargetOperation();
            final String json = dumpFileInfo.getJsonKey();
            if (targetOperation.doesKeyExist(dumpFileInfo) && !BlockLocker.isBlockLocked(dumpFileInfo)) {
                try {
                    BlockLocker.lockBlock(dumpFileInfo);
                    final String entities = targetOperation.getObjectAsString(json);
                    final String newEntities = targetOperation.removeDedupEntities(profile.getGenerationId(), entities);
                    if (StringUtils.isEmpty(newEntities)) {
                        final int removed = removedKeys.incrementAndGet();
                        final String data = dumpFileInfo.getDataKey();
                        if (this.logger.isLoggable(Level.FINE)) {
                            final String msg = String.format("Removing %s %s keys(%d) ", json, data, removed);
                            this.logger.fine(msg);
                        }
                        targetOperation.removeDump(dumpFileInfo);
                        if (this.logger.isLoggable(Level.INFO)) {
                            final String msg = String.format("Removed %s %s keys(%d) ", json, data, removed);
                            this.logger.info(msg);
                        }
                    } else {
                        final int updated = updateKeys.incrementAndGet();
                        if (this.logger.isLoggable(Level.FINE)) {
                            final String msg = String.format("Updating %s (%d):%s ", json, updated, newEntities);
                            this.logger.fine(msg);
                        }
                        targetOperation.putObject(json, newEntities);
                    }
                } catch (final JsonProcessingException e) {
                    Utility.logWarning(this.logger, e);
                    dumpFileInfo.failed(e);
                } finally {
                    BlockLocker.releaseBlock(dumpFileInfo);
                }
            } else {
                final String msg = String.format("Key %s doesn't exist", json);
                dumpFileInfo.failed(msg);
            }
            return dumpFileInfo.isFailed();
        });
    }

    /**
     * Convert to JSON String
     *
     * @param bytes
     * @param prettyJason
     * @return
     * @throws IOException
     */
    public String toJason(final byte[] bytes) throws IOException {
        return new String(bytes);
    }

    /**
     * Convert to JSON String
     *
     * @param bytes
     * @param prettyJason
     * @return
     * @throws IOException
     */
    public String toPrettyJason(final byte[] bytes, final Class<?> valueType) throws IOException {
        final Object obj = new ObjectMapper().readValue(bytes, valueType);
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

}
