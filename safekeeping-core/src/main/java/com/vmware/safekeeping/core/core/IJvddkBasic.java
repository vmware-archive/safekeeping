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
package com.vmware.safekeeping.core.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.jDiskLib.DiskHandle;
import com.vmware.jvix.jDiskLib.Info;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.InteractiveDisk;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionDiskVirtualBackupAndRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultDiskBackupRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGetGenerationProfile;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.BasicBlockInfo;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.SimpleBlockInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.manipulator.VddkConfManipulator;

public interface IJvddkBasic {
    default JVmdkInfo getVmdkInfo(final DiskHandle diskHandle) {
        final Info diskInfo = new Info();
        long vddkCallResult = SJvddk.dli.getInfo(diskHandle, diskInfo);
        if (vddkCallResult != jDiskLibConst.VIX_OK) {
            final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
            getLogger().warning(msg);
            return null;
        }
        final JVmdkInfo result = new JVmdkInfo();
        result.setAdapterType(diskInfo.adapterType);
        result.setnBlocks(diskInfo.capacityInSectors / jDiskLibConst.SECTOR_SIZE);
        result.setCapacityInSectors(diskInfo.capacityInSectors);
        result.setNumLinks(diskInfo.numLinks);

        final String[] keys = SJvddk.dli.getMetadataKeys(diskHandle);
        for (final String key : keys) {
            final StringBuffer value = new StringBuffer();
            vddkCallResult = SJvddk.dli.readMetadata(diskHandle, key, value);
            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
                getLogger().warning(msg);
                return null;
            }
            result.getMetadata().put(key, value.toString());
        }

        return result;
    }

    default String getTransportMode(final DiskHandle diskHandle) {
        String result = null;
        if (diskHandle.isValid()) {
            result = SJvddk.dli.getTransportMode(diskHandle);
        }
        return result;
    }

    default boolean isNoNfcSession() {
        if (getLogger().isLoggable(Level.CONFIG)) {
            getLogger().config("<no args> - start"); //$NON-NLS-1$
        }

        try (final VddkConfManipulator vddk = new VddkConfManipulator(CoreGlobalSettings.getVddkConfig())) {
            if (vddk.isNoNfcSession()) {
                if (getLogger().isLoggable(Level.CONFIG)) {
                    getLogger().config("<no args> - end"); //$NON-NLS-1$
                }
                return true;
            }
        } catch (final IOException e) {
            getLogger().severe("<no args> - exception: " + e); //$NON-NLS-1$

        }

        if (getLogger().isLoggable(Level.CONFIG)) {
            getLogger().config("<no args> - end"); //$NON-NLS-1$
        }
        return false;
    }

    default CoreResultActionGetGenerationProfile getGenerationProfiles(final FcoArchiveManager vmArcMgr,
            final GenerationProfile profGen, final AbstractCoreResultActionDiskVirtualBackupAndRestore radr)
            throws CoreResultActionException {
        if (getLogger().isLoggable(Level.CONFIG)) {
            getLogger().config("FcoArchiveManager, GenerationProfile, CoreResultActionDiskConsolidate - start"); //$NON-NLS-1$
        }

        final CoreResultActionGetGenerationProfile drg = new CoreResultActionGetGenerationProfile(profGen, radr);
        String msg;
        drg.start();
        try {
            if (profGen.getDiskBackupMode(radr.getDiskId()) != BackupMode.FULL) {
                msg = String.format("Disk id(%d) Generation %d Depends from %d - mode inc ", radr.getDiskId(),
                        profGen.getGenerationId(), profGen.getPreviousGenerationId());
                getLogger().info(msg);
                GenerationProfile previousGeneration;
                try {
                    previousGeneration = vmArcMgr.loadProfileGeneration(profGen.getPreviousGenerationId());
                    drg.setParent(getGenerationProfiles(vmArcMgr, previousGeneration, radr));
                    if (drg.getParent().isFails()) {
                        drg.failure(drg.getParent().getReason());
                    }
                } catch (final IOException e) {
                    drg.failure(String.format("Archive Error- Missing parent generation (id:%d)",
                            profGen.getPreviousGenerationId()));
                    Utility.logWarning(getLogger(), e);

                }

                drg.setBackupMode(BackupMode.INCREMENTAL);

            } else {
                msg = String.format("Disk id(%d) Generation %d - mode full", radr.getDiskId(),
                        profGen.getGenerationId());
                getLogger().info(msg);
                drg.setBackupMode(BackupMode.FULL);
            }
            radr.getProfiles().put(drg.getGenerationId(), profGen);
            radr.getBlocksPerGeneration().put(drg.getGenerationId(), 0);
            if (drg.isRunning()) {
                final Map<Integer, SimpleBlockInfo> dumps = profGen.getDisks().get(radr.getDiskId()).getDumps();
                for (final Entry<Integer, SimpleBlockInfo> entry : dumps.entrySet()) {
                    drg.getVixBlocks().add(new BasicBlockInfo(entry.getValue(), radr.getDiskId(), drg.getGenerationId(),
                            drg.isCompressed(), drg.isCiphered()));
                }
            }

        } finally {
            drg.done();
        }
        return drg;

    }

    Logger getLogger();

    default TotalBlocksInfo restoreAndConsolidateThreads(final AbstractCoreResultDiskBackupRestore radr,
            final InteractiveDisk interactive, final List<IRestoreThread> futureThreads)
            throws CoreResultActionException {
        TotalBlocksInfo totalDumpInfo = null;
        final int threadPool = radr.getNumberOfThreads();
        final ExecutorService es = Executors.newFixedThreadPool(threadPool);
        try {
            getLogger().log(Level.INFO, () -> MessagesTemplate.header(true));
            final long startTime = System.nanoTime();
            final List<Future<Boolean>> answers = es.invokeAll(futureThreads);
            final long endTime = System.nanoTime();
            int index = 0;
            for (final Future<Boolean> answer : answers) {
                if (answer.get() == null) {
                    final String msg = String.format("Operation disk:%d block:%d - Aborted by user", radr.getDiskId(),
                            index);

                    getLogger().warning(msg);
                    radr.aborted(msg);
                } else if (Boolean.FALSE.equals(answer.get())) {
                    final String msg = String.format("Operation disk:%d block:%d - fails - see log for more details",
                            radr.getDiskId(), index);
                    radr.failure(msg);
                    getLogger().warning(msg);
                    break;
                } else {
                    if (getLogger().isLoggable(Level.FINE)) {
                        final String msg = String.format("Operation disk:%d block:%d - success", radr.getDiskId(),
                                index);
                        getLogger().fine(msg);
                    }
                }
                ++index;

            }
            interactive.endDumpThreads(radr.getState());
            /**
             * End Section DumpThreads
             */
            /**
             * Start Section DumpsTotalCalculation
             */
            interactive.startDumpsTotalCalculation();
            totalDumpInfo = new TotalBlocksInfo(radr.getEntityType(), radr.getDiskId(), radr.getDumpMap().values(),
                    startTime, endTime);
            interactive.endDumpsTotalCalculation(totalDumpInfo);
            /**
             * Start Section EndTotalCalculation
             */

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            Utility.logWarning(getLogger(), e);
            radr.failure(e);
        } finally {
            if (es != null) {
                es.shutdown();
            }

        }

        return totalDumpInfo;
    }
}
