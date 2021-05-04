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
package com.vmware.safekeeping.core.control;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.safekeeping.common.DateUtility;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.profile.FcoGenerationsCatalog;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.profile.dataclass.Generation;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.safekeeping.core.type.fco.managers.VirtualControllerManager;
import com.vmware.safekeeping.core.type.fco.managers.VirtualDiskManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class FcoArchiveManager {
    public enum ArchiveManagerMode {
        READ, TEMPORARY, WRITE
    }

    private static final Logger logger = Logger.getLogger(FcoArchiveManager.class.getName());

    private FcoGenerationsCatalog generationsCatalog;

    private final ITargetOperation repositoryTarget;
    private GlobalFcoProfileCatalog globalFcoProfilesCatalog;

    public FcoArchiveManager(final ManagedFcoEntityInfo entity, final ITargetOperation targetOperations,
            final ArchiveManagerMode mode) throws ArchiveException, IOException {
        this.generationsCatalog = null;
        this.repositoryTarget = targetOperations;

        switch (mode) {
        case READ:
        case WRITE:

            if (this.repositoryTarget.isProfileVmExist(entity)) {
                boolean changed = false;
                this.generationsCatalog = new FcoGenerationsCatalog(this, entity);
                final ManagedFcoEntityInfo entityFromProfile = this.generationsCatalog.getFcoEntity();
                if (!entity.getUuid().equalsIgnoreCase(entityFromProfile.getUuid())) {
                    throw new ArchiveException("Profile uuid != fco uuid  %s != %s.", entityFromProfile.getServerUuid(),
                            entity.getUuid());
                }
                if (!StringUtils.equals(entityFromProfile.getName(), entity.getName())) {
                    entityFromProfile.setName(entity.getName());
                    changed = true;
                }

                if (!StringUtils.equals(entityFromProfile.getServerUuid(), entity.getServerUuid())) {
                    entityFromProfile.setServerUuid(entity.getServerUuid());
                    changed = true;
                }
                if ((mode == ArchiveManagerMode.WRITE) && changed) {
                    postGenerationsCatalog();
                }

            } else {
                this.generationsCatalog = new FcoGenerationsCatalog(entity);
                this.repositoryTarget.createProfileFolder(this.generationsCatalog);
                postGenerationsCatalog();
            }
            break;
        case TEMPORARY:
            this.generationsCatalog = new FcoGenerationsCatalog(entity);
            this.repositoryTarget.createProfileFolder(this.generationsCatalog);
            break;
        }

    }

    private boolean canExecIncrBackup(final DiskProfile currGen, final DiskProfile prevGen) {
        boolean result = false;
        final long capacity = currGen.getCapacity();
        final long prevCapacity = prevGen.getCapacity();
        if ((capacity < 0) || (capacity != prevCapacity)) {
            logger.info("capacity is invalid or different.");
        } else {
            /*
             * Check that changed block information of both previous and current generation
             * are available
             */
            final String currChangeId = currGen.getChangeId();
            if ((currChangeId == null) || "*".equals(currChangeId)) {
                logger.info("currChangeId is null or \"*\"");

            } else {
                final String prevChangeId = prevGen.getChangeId();
                logger.log(Level.INFO, () -> "prevChangeId: " + prevChangeId);
                if ((prevChangeId == null) || "*".equals(prevChangeId)) {
                    logger.info("prevChangeId is null or \"*\"");

                } else {
                    result = true;
                }
            }
        }
        return result;
    }

    public BackupMode determineBackupMode(final GenerationProfile currGen, final BackupMode requestMode) {
        return determineBackupMode(currGen, 0, requestMode);
    }

    public BackupMode determineBackupMode(final GenerationProfile profile, final int diskId,
            final BackupMode requestMode) {
        BackupMode mode = BackupMode.UNKNOW;
        if (!profile.isChangeTrackingEnabled()) {
            logger.log(Level.WARNING, () -> "Change Tracking is disabled. Backup Mode set to Full");
            mode = BackupMode.FULL;
        } else if ((requestMode == BackupMode.FULL)) {
            mode = BackupMode.FULL;
        } else if (profile.getPreviousGeneration() != null) {
            final String uuid = profile.getDiskUuid(diskId);
            final DiskProfile diskProfilePrevGeneration = profile.getPreviousGeneration().getDisks().get(diskId);
            if (diskProfilePrevGeneration == null) {
                logger.warning("No Previous succesfull generations are available.");
                mode = BackupMode.FULL;
            } else {
                logger.log(Level.INFO, () -> "Generation disk Id: " + diskId);
                if (diskId >= 0) {
                    if (diskProfilePrevGeneration.getDiskId() != profile.getDiskIdWithUuid(uuid)) {
                        final String msg = String.format(
                                "Previous Generation disk Id:%d  is different from Current Generation disk Id:%d",
                                diskProfilePrevGeneration.getDiskId(), diskId);
                        logger.warning(msg);
                    } else {
                        mode = (canExecIncrBackup(profile.getDisks().get(diskId), diskProfilePrevGeneration))
                                ? BackupMode.INCREMENTAL
                                : BackupMode.FULL;
                    }
                }
            }
        } else {
            mode = BackupMode.FULL;
        }

        return mode;
    }

    public boolean finalizeBackup(final GenerationProfile currGen, final OperationState operationState) {
        currGen.setSucceeded(operationState == OperationState.SUCCESS);
        this.generationsCatalog.setGenerationInfo(currGen, currGen.getBackupMode());
        return currGen.isSucceeded();
    }

    /**
     * Create a list of disk with associated controller to be re-create
     *
     * @param profile
     * @param datastoreName
     * @return
     */
    public List<VirtualControllerManager> generateVirtualControllerManagerList(final GenerationProfile profile,
            final String datastoreName) {

        final LinkedHashMap<Integer, VirtualControllerManager> vcmMap = new LinkedHashMap<>();

        for (int diskId = 0; diskId < profile.getNumberOfDisks(); diskId++) {
            final AdapterType type = profile.getAdapterType(diskId);
            assert type != AdapterType.UNKNOWN;
            final Integer ckey = profile.getControllerDeviceKey(diskId);
            final int busNumber = profile.getBusNumber(ckey);
            final int unitNumber = profile.getUnitNumber(diskId);

            if (profile.getDiskMode(diskId) == VirtualDiskModeType.independent_persistent) {
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Controller:%d Disk [%d:%d] is an Indipendant or Persistent disk ",
                            ckey, busNumber, unitNumber);
                    logger.info(msg);
                }
            } else if (profile.isImprovedVirtualDisk(diskId)) {
                if (logger.isLoggable(Level.INFO)) {
                    String msg = String.format("Controller:%d Disk [%d:%d] is an Improved Virtual Disk uuid:%s ", ckey,
                            busNumber, unitNumber, profile.getImprovedVirtualDiskId(diskId));
                    logger.info(msg);
                    msg = String.format(
                            "To restore this IVD use: restore ivd:%s (take note of the uuid) and execute ivd -attach -device %d:%d ivd:<newid>",
                            profile.getImprovedVirtualDiskId(diskId), ckey, unitNumber);
                    logger.info(msg);
                }
            } else {

                final VirtualDiskManager vdm = new VirtualDiskManager(profile, diskId, datastoreName);

                if (!vcmMap.containsKey(ckey)) {
                    final VirtualControllerManager vcm = new VirtualControllerManager(type, ckey, busNumber);
                    vcmMap.put(ckey, vcm);
                }
                vcmMap.get(ckey).add(vdm);
            }
        }

        final List<VirtualControllerManager> result = new LinkedList<>();
        result.addAll(vcmMap.values());
        return result;
    }

    public Generation getDependingGenerationId(final int genId) {
        Generation result = null;
        if (this.generationsCatalog != null) {
            final Generation nextGen = this.generationsCatalog.getNextSucceededGenerationId(genId);
            if (nextGen != null) {
                final int dependingGeneration = this.generationsCatalog.getDependingGenerationId(nextGen.getGenId());
                if (dependingGeneration == genId) {
                    result = nextGen;
                }
            }
        }
        return result;

    }

    List<Integer> getDependingGenerationList(final GenerationProfile profGen) {
        final List<Integer> result = this.generationsCatalog.getDependingGenerationList(profGen.getGenerationId());
        return result;
    }

    public int getDependingOnGenerationId(final int id) {
        if (this.generationsCatalog == null) {
            return -2;
        }
        return this.generationsCatalog.getDependingGenerationId(id);
    }

    public EntityType getEntityType() {
        return this.generationsCatalog.getFcoEntity().getEntityType();
    }

    public List<Integer> getFailedGenerationList() {
        return this.generationsCatalog.getFailedGenerationList();
    }

    public List<Integer> getGenerationIdList() {
        if (this.generationsCatalog == null) {
            return new LinkedList<>();
        }
        return this.generationsCatalog.getGenerationIdList();

    }

    public FcoGenerationsCatalog getGenerationsCatalog() {
        return this.generationsCatalog;
    }

    public GlobalFcoProfileCatalog getGlobalFcoProfilesCatalog() throws IOException {
        if (this.globalFcoProfilesCatalog == null) {
            this.globalFcoProfilesCatalog = new GlobalFcoProfileCatalog(this.repositoryTarget.getParent());
        }

        return this.globalFcoProfilesCatalog;
    }

    public String getInstanceUuid() {
        if (this.generationsCatalog == null) {
            return null;
        }
        return this.generationsCatalog.getFcoEntity().getUuid();
    }

    public int getLatestGenerationId() {
        if (this.generationsCatalog == null) {
            return -1;
        }
        return this.generationsCatalog.getLatestGenerationId();
    }

    public int getLatestSucceededGenerationId() {
        if (this.generationsCatalog == null) {
            return -1;
        }
        return this.generationsCatalog.getLatestSucceededGenerationId();
    }

    /**
     * Return the last Succeeded Generation from all generation contained in the
     * list
     *
     * @param generationList
     * @return
     */
    public int getLatestSucceededGenerationId(final List<Integer> generationList) {
        if (this.generationsCatalog == null) {
            return -1;
        }
        return this.generationsCatalog.getLatestSucceededGenerationId(Collections.max(generationList));
    }

    public String getMoref() {
        if (this.generationsCatalog == null) {
            return null;
        }
        return this.generationsCatalog.getFcoEntity().getMorefValue();
    }

    public String getName() {
        if (this.generationsCatalog == null) {
            return null;
        }
        return this.generationsCatalog.getFcoEntity().getName();
    }

    public int getNumOfGeneration() {
        return this.generationsCatalog.getNumOfGeneration();
    }

    public String getPrevChangeId(final GenerationProfile currGen, final int diskId) {

        final GenerationProfile prevGen = getPrevGeneration(currGen);
        if (prevGen == null) {
            return null;
        }

        final int prevDiskId = getPrevDiskId(currGen, diskId);
        if (prevDiskId < 0) {
            return null;
        }

        final String prevChangeId = prevGen.getDiskChangeId(prevDiskId);

        if ((prevChangeId != null) && (!"*".equals(prevChangeId))) {
            return prevChangeId;
        } else {
            return null;
        }
    }

    private int getPrevDiskId(final GenerationProfile currGen, final int diskId) {
        final GenerationProfile prevGen = getPrevGeneration(currGen);
        if (prevGen == null) {
            return -1;
        }
        final String uuid = prevGen.getDiskUuid(diskId);
        final int prevDiskId = prevGen.getDiskIdWithUuid(uuid);
        if (prevDiskId < 0) {
            return -1;
        }

        return prevDiskId;
    }

    private GenerationProfile getPrevGeneration(final GenerationProfile currGen) {
        final int currGenId = currGen.getGenerationId();
        return getPrevGeneration(currGenId);
    }

    private GenerationProfile getPrevGeneration(final int genId) {
        GenerationProfile result = null;
        if (genId >= 0) {
            final int prevGenId = this.generationsCatalog.getPrevSucceededGenerationId(genId);
            logger.log(Level.FINE, () -> "prevGenId: " + prevGenId);
            if ((prevGenId >= 0) && (genId != prevGenId)) {
                try {
                    result = loadProfileGeneration(prevGenId);
                } catch (final Exception e) {
                    Utility.logWarning(logger, e);
                }
            }
        }
        return result;
    }

    public ITargetOperation getRepositoryTarget() {
        return this.repositoryTarget;
    }

    public ITarget getTarget() {
        return this.repositoryTarget.getParent();
    }

    public long getTimestampMsOfLatestGeneration() {
        long result = -1L;
        if (this.generationsCatalog != null) {
            final Integer genId = this.generationsCatalog.getLatestGenerationId();
            if (genId < 0) {
                result = -1L;
            } else {
                final Generation lastGen = this.generationsCatalog.getLatestGeneration();
                if (lastGen != null) {
                    result = lastGen.getTimestamp().getTime();
                }
            }
        }
        return result;
    }

    public long getTimestampMsOfLatestSucceededGenerationId() {
        if (this.generationsCatalog == null) {
            return -1L;
        }
        final Integer genId = this.generationsCatalog.getLatestSucceededGenerationId();

        if (genId < 0) {
            return -1L;
        } else {
            return this.generationsCatalog.getLatestSuccededGeneration().getTimestamp().getTime();
        }
    }

    public String getTimestampOfLatestGeneration() {
        if (this.generationsCatalog == null) {
            return null;
        }
        final Integer genId = this.generationsCatalog.getLatestGenerationId();

        if ((genId < 0) || (this.generationsCatalog.getLatestGeneration() == null)) {
            return null;
        } else {

            return DateUtility.toGMTString(this.generationsCatalog.getLatestGeneration().getTimestamp());
        }
    }

    public String getTimestampOfLatestSucceededGenerationId() {
        if (this.generationsCatalog == null) {
            return null;
        }
        final Integer genId = this.generationsCatalog.getLatestSucceededGenerationId();

        if (genId < 0) {
            return null;
        } else {
            return DateUtility.toGMTString(this.generationsCatalog.getLatestSuccededGeneration().getTimestamp());
        }
    }

    public GenerationProfile loadLatestSucceededGeneration() throws IOException {
        final int genId = this.generationsCatalog.getLatestSucceededGenerationId();
        return loadProfileGeneration(genId);
    }

    public GenerationProfile loadProfileGeneration(final Integer requestedGenId) throws IOException {
        GenerationProfile result = null;
        final Integer genId = (requestedGenId < 0) ? this.generationsCatalog.getLatestSucceededGenerationId()
                : requestedGenId;
        if (genId < 0) {
            logger.log(Level.WARNING, () -> String.format("No generation has genId <0  %d.", genId));
        } else if (this.generationsCatalog.getGenerations().containsKey(genId)) {
            if (!this.generationsCatalog.isGenerationSucceeded(genId)) {
                logger.log(Level.WARNING, () -> String.format("status of the generation  %d is not succeeded.", genId));

            }
            result = new GenerationProfile(this, this.generationsCatalog.getFcoEntity(), genId);
        } else {
            final String msg = String.format("GenerationId %d doesn't exist", genId);
            logger.warning(msg);
        }
        return result;
    }

    /**
     * Post Profile FCO to target
     *
     * @return True if succeed
     */
    public boolean postGenerationsCatalog() {
        boolean result = true;
        try {
            result = this.repositoryTarget.postGenerationsCatalog(this.generationsCatalog.getFcoEntity(),
                    this.generationsCatalog.toByteArrayInOutputStream());
        } catch (final IOException | NoSuchAlgorithmException e) {
            Utility.logWarning(logger, e);
            result = false;
        }
        return result;
    }

    /**
     * Prepare a new archive generation for a FCD and set as current profile
     *
     * @param ivd
     * @param calendar
     * @param backupIvdInfo
     * @param cbt           if set force the disk cbt value
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws com.vmware.pbm.RuntimeFaultFaultMsg
     * @throws InvalidArgumentFaultMsg
     * @throws PbmFaultFaultMsg
     * @throws ProfileException
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws VimObjectNotExistException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws Exception
     * @Deprecated (since 2.0, not good,to do)
     */
    @Deprecated()
    public GenerationProfile prepareNewGeneration(final GenerationProfileSpec spec)
            throws IOException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, PbmFaultFaultMsg,
            com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg, ProfileException,
            com.vmware.vslm.RuntimeFaultFaultMsg, VimObjectNotExistException {
        final FcoGenerationsCatalog profFco = this.generationsCatalog;
        /*
         * Create new generation id.
         */
        spec.setPrevGenId(profFco.getLatestSucceededGenerationId());
        final BackupMode backupMode = (spec.getBackupOptions().getRequestedBackupMode() == BackupMode.FULL)
                ? BackupMode.FULL
                : BackupMode.UNKNOW;
        spec.setGenId(profFco.createNewGenerationId(spec.getCalendar().getTimeInMillis(), backupMode));

        /*
         * Make new ProfileGeneration and initialize it.
         */
        final GenerationProfile profGen = new GenerationProfile(this);
        profGen.initializeGeneration(spec);
        return profGen;
    }

}
