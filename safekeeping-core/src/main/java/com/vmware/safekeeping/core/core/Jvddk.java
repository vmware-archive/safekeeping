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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.JDiskLibFactory;
import com.vmware.jvix.JVixException;
import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLib.ConnectParams;
import com.vmware.jvix.jDiskLib.Connection;
import com.vmware.jvix.jDiskLib.DiskHandle;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractBackupDiskInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreDiskInteractive;
import com.vmware.safekeeping.core.command.options.CoreBackupRestoreCommonOptions;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultDiskBackupRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGetGenerationProfile;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.JddkException;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.BasicBlockInfo;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.managers.PrivilegesList;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.VmbkThreadFactory;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.QueryBlocksOption;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.internal.util.StringUtils;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.InvalidDatastoreFaultMsg;
import com.vmware.vslm.InvalidStateFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public class Jvddk extends SJvddk implements IJvddkBasic {

    private final Logger logger;

    private final VimConnection basicVimConnection;

    private String identity;

    private ConnectParams connectParams;

    private IFirstClassObject fco;

    private Boolean privilageEnableDisableMethod;

    public Jvddk(final Logger logger, final ImprovedVirtualDisk ivd) throws JVixException {
        this(ivd.getVimConnection(), logger);
        this.connectParams = this.basicVimConnection.configureVddkAccess(ivd);
        this.fco = ivd;
        this.privilageEnableDisableMethod = null;
    }

    public Jvddk(final Logger logger, final ImprovedVirtualDisk ivd, final String ssId) throws JVixException {
        this(ivd.getVimConnection(), logger);
        this.connectParams = this.basicVimConnection.configureVddkAccess(ivd, ssId);
        this.fco = ivd;
        this.privilageEnableDisableMethod = null;
    }

    public Jvddk(final Logger logger, final VirtualMachineManager vmm) throws JVixException {
        this(vmm.getVimConnection(), logger);
        this.connectParams = this.basicVimConnection.configureVddkAccess(vmm);
        this.fco = vmm;
        this.privilageEnableDisableMethod = null;
    }

    private Jvddk(final VimConnection vimConnection, final Logger logger) throws JVixException {
        this.logger = logger;
        this.basicVimConnection = vimConnection;
        this.privilageEnableDisableMethod = null;
        if (!SJvddk.isInitialized()) {
            throw new JVixException("Library not Initialized correctly");
        }
    }

    /**
     * Check enable/disable method privileges
     *
     * @return
     * @throws JVixException
     */
    private boolean checkEnableDisablePrivileges() throws JVixException {
        switch (this.fco.getEntityType()) {
        case VirtualMachine:
            if (this.privilageEnableDisableMethod != null) {
                return this.privilageEnableDisableMethod;
            }
            final Map<String, Boolean> methodAuhorization = new HashMap<>();
            try {
                methodAuhorization.putAll(this.basicVimConnection.getPrivilegeChecker().hasUserPrivilegesOnEntity(
                        this.fco.getVimConnection().getRootFolder(), PrivilegesList.PRIVILEGE_ENABLE_METHOD,
                        PrivilegesList.PRIVILEGE_DISABLE_METHOD));
            } catch (final RuntimeFaultFaultMsg e) {
                throw new JVixException(e);
            }
            this.privilageEnableDisableMethod = Boolean.TRUE
                    .equals(methodAuhorization.get(PrivilegesList.PRIVILEGE_ENABLE_METHOD))
                    && Boolean.TRUE.equals(methodAuhorization.get(PrivilegesList.PRIVILEGE_DISABLE_METHOD));
            return this.privilageEnableDisableMethod;
        case ImprovedVirtualDisk:
        case VirtualApp:
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("No Enable-Disable options available for not VirtualMachine entity");
            }
            return true;
        default:
            throw new JVixException("Unsupported Type");

        }

    }

    private void closeVmdk(final AbstractCoreResultDiskBackupRestore radb) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("DiskHandle, CoreAbstractResultActionImpl - start"); //$NON-NLS-1$
        }
        final DiskHandle diskHandle = radb.getDiskHandle();
        if (diskHandle != null) {
            if (diskHandle.isValid()) {
                final DiskOpenCloseHandle diskClose = new DiskOpenCloseHandle(diskHandle);
                SJvddk.queue.add(diskClose);
                while (!diskClose.getExecuted().get()) {
                    try {
                        Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
                    } catch (final InterruptedException e) {
                        this.logger.severe("DiskHandle, CoreAbstractResultActionImpl - exception: " + e); //$NON-NLS-1$
                        Thread.currentThread().interrupt();
                    }
                }
                if (diskClose.getVddkCallResult() != jDiskLibConst.VIX_OK) {
                    final String msg = SJvddk.dli.getErrorText(diskClose.getVddkCallResult(), null);
                    radb.failure(msg);
                    this.logger.warning(msg);
                }
            } else {
                final String msg = String.format("DiskHandle %s is not valid", diskHandle.getHandle());
                this.logger.warning(msg);
            }
        } else {
            final String msg = "DiskHandle is NULL";
            this.logger.warning(msg);
        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("DiskHandle, CoreAbstractResultActionImpl - end"); //$NON-NLS-1$
        }
    }

    private Connection connect(final boolean readOnly, final String snapshotRef, final String requestedTransportModes)
            throws JVixException {

        final Connection connHandle = new Connection();

        long vddkCallResult;
        if (this.connectParams.getSpecType() == ConnectParams.VIXDISKLIB_SPEC_VSTORAGE_OBJECT) {

            vddkCallResult = SJvddk.dli.connectEx(this.connectParams, readOnly, null, requestedTransportModes,
                    connHandle);
        } else {
            this.logger.log(Level.INFO, () -> String.format("connectEx() VM: %s   snapshotRef: %s transportModes: %s",
                    this.connectParams.getVmxSpec(), snapshotRef, requestedTransportModes));
            vddkCallResult = SJvddk.dli.connectEx(this.connectParams, readOnly, snapshotRef, requestedTransportModes,
                    connHandle);
        }
        if (vddkCallResult != jDiskLibConst.VIX_OK) {
            throw new JVixException(vddkCallResult, SJvddk.dli.getErrorText(vddkCallResult, null));
        }

        return connHandle;
    }

    public Connection connectReadOnly(final String transportModes) throws JVixException {
        return connect(true, null, transportModes);
    }

    public Connection connectReadOnly(final String snapshotRef, final String transportModes) throws JVixException {
        return connect(true, snapshotRef, transportModes);
    }

    public Connection connectReadWrite(final String transportModes) throws JVixException {
        return connect(false, null, transportModes);
    }

    public Connection connectReadWrite(final String snapshotRef, final String transportModes) throws JVixException {
        return connect(false, snapshotRef, transportModes);
    }

    /**
     * Disconnect from FCO
     *
     * @param connHandle
     *
     * @return
     */
    public long disconnect(final Connection connHandle) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("Connection - start"); //$NON-NLS-1$
        }

        final long vddkCallResult = SJvddk.dli.disconnect(connHandle);
        if (vddkCallResult != jDiskLibConst.VIX_OK) {

            final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
            this.logger.warning(msg);
        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("Connection - end"); //$NON-NLS-1$
        }
        return vddkCallResult;

    }

    /**
     *
     * @param connectionHandle
     * @param fco
     * @param profile
     * @param blockListQueryChangedDiskAreas
     * @param target
     * @param radb
     * @param interactive
     * @throws CoreResultActionException
     */
    public void doDumpJava(final IFirstClassObject fco, final GenerationProfile profile,
            final List<Block> blockListQueryChangedDiskAreas, final ITargetOperation target,
            final CoreResultActionDiskBackup radb, final AbstractBackupDiskInteractive interactive)
            throws CoreResultActionException {

        String msg = null;

        radb.setFlags(SJvddk.dli.getFlags(radb.getRequestedTransportModes(), true));

        /**
         * Start Section OpenVmdkrestore vm:max_possa -name max_possa-revolution-1
         * -thread 1 -power-on
         */
        interactive.startOpenVmdk();
        try {

            if (openVmdk(fco.getEntityType(), radb)) {

                /**
                 * end Section OpenVmdk
                 */
                interactive.endOpenVmdk();
                /**
                 * Start Section QueryBlocks
                 */
                interactive.startQueryBlocks();
                radb.setJVmdkInfo(getVmdkInfo(radb.getDiskHandle()));
                if (radb.getJVmdkInfo() != null) {

                    profile.setDiskMetadata(radb.getDiskId(), radb.getJVmdkInfo().getMetadata());
                    msg = radb.getJVmdkInfo().toString();
                    this.logger.info(msg);
                    final List<Block> blockList = queryBlock(radb, blockListQueryChangedDiskAreas);
                    if (blockList.isEmpty()) {
                        manageEmptyBlocksList(radb);
                        interactive.endDumpThreads(radb.getState());
                        /**
                         * End Section DumpThreads
                         */
                    } else {
                        /**
                         * End Section QueryBlocks
                         */
                        interactive.endQueryBlocks();
                        final String finalReport = dump(radb, interactive, blockList, target);
                        try (ByteArrayInOutStream reportStream = new ByteArrayInOutStream(finalReport)) {
                            target.postReport(profile, radb.getDiskId(), reportStream);
                        }
                    }
                }

            }
        } catch (final IOException | NoSuchAlgorithmException | FileFaultFaultMsg | NotFoundFaultMsg
                | RuntimeFaultFaultMsg | com.vmware.vslm.FileFaultFaultMsg | InvalidArgumentFaultMsg
                | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | JddkException
                | com.vmware.vim25.InvalidDatastoreFaultMsg | com.vmware.vim25.InvalidStateFaultMsg
                | java.lang.IllegalArgumentException | javax.xml.ws.WebServiceException | JVixException e) {
            Utility.logWarning(this.logger, e);
            radb.failure(e);

        } catch (final InterruptedException e) {
            radb.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            radb.failure(e);
        } finally {
            /**
             * Start Section CloseVmdk
             */
            interactive.startCloseVmdk();
            closeVmdk(radb);

            interactive.endCloseVmdk();
            /**
             * end Section CloseVmdk
             */
        }

    }

    /**
     *
     * @param vixBlocks
     * @param radr
     * @param target
     * @param interactive
     * @return
     * @throws CoreResultActionException
     */
    private CoreResultActionDiskRestore doRestore(final List<BasicBlockInfo> vixBlocks,
            final CoreResultActionDiskRestore radr, final ITargetOperation target,
            final AbstractRestoreDiskInteractive interactive) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "List<RestoreBlock>, CoreResultActionDiskRestore, TargetOperation, AbstractRestoreInteractive - start"); //$NON-NLS-1$
        }

        Buffers buffers = null;
        try {
            if (!vixBlocks.isEmpty()) {

                /*
                 * Initialize buffers
                 */
                final int threadPool = radr.getNumberOfThreads();
                final int maxBlockSizeInBytes = radr.getMaxBlockSizeInBytes();
                buffers = new Buffers(target, threadPool, maxBlockSizeInBytes, radr.getFcoEntityInfo());

                /*
                 * end buffer initializations
                 */

                final List<IRestoreThread> futureThreads = new ArrayList<>(vixBlocks.size());

                /**
                 * Start Section DumpThreads
                 */
                interactive.startDumpThreads();
                final String msg = MessagesTemplate.diskHeaderInfo(radr);
                this.logger.info(msg);

                for (final BasicBlockInfo block : vixBlocks) {
                    final ExBlockInfo dumpFilesInfo = new ExBlockInfo(block, vixBlocks.size(), target.getDisksPath());

                    final IRestoreThread callableThread = new RestoreThread(dumpFilesInfo, buffers, radr, interactive,
                            this.logger);
                    futureThreads.add(callableThread);
                }
                buffers.start();
                try {
                    restoreAndConsolidateThreads(radr, interactive, futureThreads);
                } finally {
                    buffers.stop();
                }
            } else {
                final String msg = "No blocks to restore";
                this.logger.info(msg);
                radr.skip(msg);
            }
        } catch (final NoSuchAlgorithmException e) {
            this.logger.severe(
                    "List<RestoreBlock>, CoreResultActionDiskRestore, TargetOperation, AbstractRestoreInteractive - exception: " //$NON-NLS-1$
                            + e);
            radr.failure(e);
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "List<RestoreBlock>, CoreResultActionDiskRestore, TargetOperation, AbstractRestoreInteractive - end"); //$NON-NLS-1$
        }
        return radr;
    }

    /**
     * Start restore
     *
     * @param vmArcMgr
     * @param profGen
     * @param restoreInfo
     * @param diskId
     * @param radr
     * @param interactive
     * @return
     * @throws CoreResultActionException
     */
    public CoreResultActionDiskRestore doRestoreJava(final FcoArchiveManager vmArcMgr,
            final CoreBackupRestoreCommonOptions restoreInfo, final CoreResultActionDiskRestore radr,
            final AbstractRestoreDiskInteractive interactive) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "FcoArchiveManager, CoreRestoreOptions, CoreResultActionDiskRestore, AbstractRestoreInteractive - start"); //$NON-NLS-1$
        }

        String msg = null;
        radr.setFlags(SJvddk.dli.getFlags(radr.getRequestedTransportModes(), false));
        try {
            /**
             * start Section OpenVmdk
             */
            interactive.startOpenVmdk();
            if (openVmdk(radr.getEntityType(), radr)) {
                /**
                 * end Section OpenVmdk
                 */
                interactive.endOpenVmdk();
                /**
                 * Start Section CalculateNumberOfGenerationDiskRestore
                 */
                interactive.startCalculateNumberOfGenerationDiskRestore();
                if (this.logger.isLoggable(Level.INFO)) {
                    msg = MessagesTemplate.diskHeaderInfo(radr);
                    this.logger.info(msg);
                }
                radr.setNumberOfThreads(restoreInfo.getNumberOfThreads());
                radr.setTargetName(vmArcMgr.getRepositoryTarget().getTargetName());
                // TODO implement something better
                if (((CoreRestoreOptions) radr.getParent().getOptions()).isRecover()) {
                    setDiskMetadata(radr);
                }
                final CoreResultActionGetGenerationProfile raggp = getGenerationProfiles(vmArcMgr, radr.getProfile(),
                        radr);
                if (raggp.isSuccessful()) {
                    final List<BasicBlockInfo> vixRestoreBlock = new ConsolidateBlocks(radr).compute();
                    if (!vixRestoreBlock.isEmpty()) {
                        interactive.endCalculateNumberOfGenerationDiskRestore();
                        if (this.logger.isLoggable(Level.INFO)) {
                            msg = MessagesTemplate.getDiskGenerationsInfo(radr);
                            this.logger.info(msg);
                        }
                        /**
                         * End Section CalculateNumberOfGenerationDiskRestore
                         */
                        doRestore(vixRestoreBlock, radr, vmArcMgr.getRepositoryTarget(), interactive);
                    }
                } else {
                    radr.failure(raggp.getReason());
                }
            }
        } catch (final JddkException e) {
            radr.failure(e);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            radr.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            if (radr.getDiskHandle().isValid()) {
                /**
                 * Start Section CloseVmdk
                 */
                interactive.startCloseVmdk();
                closeVmdk(radr);

                interactive.endCloseVmdk();
                /**
                 * end Section CloseVmdk
                 */
            }
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "FcoArchiveManager, CoreRestoreOptions, CoreResultActionDiskRestore, AbstractRestoreInteractive - end"); //$NON-NLS-1$
        }
        return radr;
    }

    private String dump(final CoreResultActionDiskBackup radb, final AbstractBackupDiskInteractive interactive,
            final List<Block> blockList, final ITargetOperation target) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "CoreResultActionDiskBackup, AbstractBackupInteractive, List<Block>, DiskHandle, TargetOperation - start"); //$NON-NLS-1$
        }
        String msg;
        final List<BasicBlockInfo> vixBlocks = new ArrayList<>();
        final GenerationProfile profile = radb.getProfile();
        final int maxBlockSizeInBytes = (int) profile.getMaxBlockSize();
        final int maxSectorsXBlock = maxBlockSizeInBytes / jDiskLibConst.SECTOR_SIZE;
        final StringBuilder finalReport = new StringBuilder();

        try {
            /*
             * Initialize buffers
             */
            final int threadPool = radb.getNumberOfThreads();
            final Buffers buffers = new Buffers(target, threadPool, maxBlockSizeInBytes, radb.getFcoEntityInfo());

            /*
             * end buffer initializations
             */

            /**
             * Start Section Normalize Blocks
             */
            interactive.startNormalizeVmdkBlocks();
            msg = String.format("Normalize the block list to the Max block size of %d Sectors (%s)", maxSectorsXBlock,
                    PrettyNumber.toString(profile.getMaxBlockSize(), MetricPrefix.MEGA));
            this.logger.info(msg);
            final int newEntry = normalizeBlocks(radb, blockList, vixBlocks, maxSectorsXBlock);
            msg = String.format("Original blocks:%d New added blocks:%d Total blocks:%d ", blockList.size(), newEntry,
                    vixBlocks.size());
            this.logger.info(msg);

            radb.setNumberOfBlocks(vixBlocks.size());

            interactive.endNormalizeVmdkBlocks();
            /**
             * End Section Normalize Blocks
             */
            final List<DumpThread> futureThreads = new ArrayList<>(vixBlocks.size());
            /**
             * set semaphore for 1 single VDDK read operation at the time
             */
            final String[] report = new String[vixBlocks.size()];

            for (final BasicBlockInfo block : vixBlocks) {
                final ExBlockInfo dumpFilesInfo = new ExBlockInfo(block, vixBlocks.size(), target.getDisksPath());
                final DumpThread r = new DumpThread(dumpFilesInfo, buffers, radb, report, interactive, this.logger);

                futureThreads.add(r);
            }
            msg = MessagesTemplate.diskHeaderInfo(radb);
            finalReport.append(msg);
            this.logger.info(msg);
            finalReport.append('\n');
            msg = MessagesTemplate.diskDumpHeaderInfo(radb);
            this.logger.info(msg);
            finalReport.append(msg);
            finalReport.append('\n');
            msg = MessagesTemplate.header(radb.isCompressed());
            this.logger.info(msg);
            finalReport.append(msg);
            finalReport.append('\n');
            /**
             * Start Section DumpThreads
             */
            interactive.startDumpThreads();
            buffers.start();
            TotalBlocksInfo totalDumpInfo;
            try {
                totalDumpInfo = dumpThreads(radb, futureThreads);
                // wait for subtask to finish
                buffers.waitSubTasks();
            } finally {
                interactive.endDumpThreads(radb.getState());
                /**
                 * End Section DumpThreads
                 */
                buffers.stop();
            }
            for (final DumpThread s : futureThreads) {
                profile.addDumpInfo(radb.getDiskId(), s.getBlockInfo());
            }
            /**
             * Start Section DumpsTotalCalculation
             */
            interactive.startDumpsTotalCalculation();
            finalReport.append(String.join("\n", report));
            finalReport.append('\n');
            finalReport.append(totalDumpInfo.separetorBar());
            finalReport.append('\n');
            msg = totalDumpInfo.toString();
            finalReport.append(msg);
            this.logger.info(msg);
            /**
             * End Section DumpsTotalCalculation
             */
            interactive.endDumpsTotalCalculation(totalDumpInfo);
            profile.setDiskTotalDumpSize(radb.getDiskId(), totalDumpInfo.getStreamSize());
            profile.setDiskTotalUncompressedDumpSize(radb.getDiskId(), totalDumpInfo.getSize());
        } catch (final NoSuchAlgorithmException e) {
            this.logger.severe(
                    "CoreResultActionDiskBackup, AbstractBackupInteractive, List<Block>, DiskHandle, TargetOperation - exception: " //$NON-NLS-1$
                            + e);

            msg = String.format("Dump disk:%d fails - see log for more details", radb.getDiskId());
            this.logger.warning(msg);
            finalReport.append(msg);

            finalReport.append('\n');
            radb.failure(e);
        } catch (final InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        if (radb.isRunning()) {
            msg = String.format("Dump disk:%d success", radb.getDiskId());
            this.logger.info(msg);
            finalReport.append(msg);
        } else {
            msg = String.format("Dump disk:%d failed: %s", radb.getDiskId(), radb.getReason());
            this.logger.warning(msg);
            finalReport.append(msg);
        }
        final String returnString = finalReport.toString();
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "CoreResultActionDiskBackup, AbstractBackupInteractive, List<Block>, DiskHandle, TargetOperation - end"); //$NON-NLS-1$
        }
        return returnString;

    }

    /**
     * Execute the dump threads
     *
     * @param radb
     * @param buffers
     * @param futureThreads
     * @return
     * @throws CoreResultActionException
     */
    private TotalBlocksInfo dumpThreads(final CoreResultActionDiskBackup radb, final List<DumpThread> futureThreads)
            throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionDiskBackup, List<AbstractDumpThread> - start"); //$NON-NLS-1$
        }
        TotalBlocksInfo returnTotalDumpFileInfo = null;
        final long startTime = System.nanoTime();
        final int threadPool = (int) Math.ceil(radb.getNumberOfThreads() * 1.3);
        final VmbkThreadFactory threadFactory = new VmbkThreadFactory(
                String.format("dump-%s-disk:%d", radb.getFcoEntityInfo().getName(), radb.getDiskId()), false, 0);

        final ExecutorService es = Executors.newFixedThreadPool(threadPool, threadFactory);
        try {

            final List<Future<Boolean>> answers = es.invokeAll(futureThreads);

            final long endTime = System.nanoTime();
            int index = 0;
            for (final Future<Boolean> answer : answers) {
                if (answer.get() == null) {
                    final String msg = String.format("Dump disk:%d block:%d - Aborted by user", radb.getDiskId(),
                            index);
                    radb.aborted(msg);
                    this.logger.warning(msg);
                } else if (Boolean.FALSE.equals(answer.get())) {
                    final String msg = String.format("Dump disk:%d block:%d - fails - see log for more details",
                            radb.getDiskId(), index);
                    radb.failure(msg);
                    this.logger.warning(msg);
                } else {
                    if (this.logger.isLoggable(Level.FINE)) {
                        final String msg = String.format("Dump disk:%d block:%d - success", radb.getDiskId(), index);
                        this.logger.fine(msg);
                    }
                }
                if (radb.isAbortedOrFailed()) {
                    break;
                }
                ++index;
            }

            returnTotalDumpFileInfo = new TotalBlocksInfo(radb.getEntityType(), radb.getDiskId(),
                    radb.getDumpMap().values(), startTime, endTime);

        } catch (final InterruptedException e) {
            this.logger.severe("CoreResultActionDiskBackup, List<AbstractDumpThread> - exception: " + e); //$NON-NLS-1$
            radb.failure(e);
            Thread.currentThread().interrupt();

        } catch (final ExecutionException e) {
            Utility.logWarning(this.logger, e);
            radb.failure(e);
        } finally {
            if (es != null) {
                es.shutdown();
            }
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionDiskBackup, List<AbstractDumpThread> - end"); //$NON-NLS-1$
        }
        return returnTotalDumpFileInfo;
    }

    public long endAccess() throws JVixException {
        long vddkCallResult = jDiskLibConst.VIX_OK;
        if (StringUtils.isNotBlank(identity)) {
            vddkCallResult = endAccess(this.identity);
            this.identity = null;
        }
        return vddkCallResult;
    }

    public long endAccess(final String idt) throws JVixException {
        if (checkEnableDisablePrivileges() && CoreGlobalSettings.isEnableForAccessOn()) {
            final long vddkCallResult = SJvddk.dli.endAccess(this.connectParams, idt);
            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
                this.logger.warning(msg);
            }
            return vddkCallResult;
        } else {
            return jDiskLibConst.VIX_OK;
        }
    }

    public VimConnection getBasicVimConnection() {
        return this.basicVimConnection;
    }

    private List<Block> getBlockList(final CoreResultActionDiskBackup radb)
            throws FileFaultFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, com.vmware.vslm.FileFaultFaultMsg,
            InvalidArgumentFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.InvalidStateFaultMsg, JVixException {
        final List<Block> blockList = new ArrayList<>();
        switch (radb.getQueryBlocksOption()) {
        case ALLOCATED:
            blockList.addAll(queryAllocatedBlock(radb.getDiskHandle(), radb.getJVmdkInfo()));
            break;
        case CHANGED_AREAS:
            blockList.addAll(radb.getFirstClassObject().queryChangedDiskAreas(radb.getProfile(), radb.getDiskId(),
                    radb.getBackupMode()));
            break;
        case FULL:
        default:
            blockList.addAll(radb.getFirstClassObject().getFullDiskAreas(radb.getDiskId()));
            break;

        }
        return blockList;
    }

    public String getErrorText(final long vddkCallResult) {
        return SJvddk.dli.getErrorText(vddkCallResult, null);
    }

    private LinkedList<Block> getIncrementalSectorsRange(final List<Block> changedRangesList,
            final List<Block> allocatedRangesList) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("List<Block>, List<Block> - start"); //$NON-NLS-1$
        }

        // changed ranges should be ordered or in priority queue
        final LinkedList<Block> changedRanges = new LinkedList<>(changedRangesList);
        final LinkedList<Block> allocatedRanges = new LinkedList<>(allocatedRangesList);
        final LinkedList<Block> backupRanges = new LinkedList<>();
        while (!allocatedRanges.isEmpty() && !changedRanges.isEmpty()) {
            // get the first range
            final Block changedRange = changedRanges.peekFirst();
            final Block allocatedRange = allocatedRanges.peekFirst();
            final Block backupRange = getOverlap(changedRange, allocatedRange);
            if (backupRange != null) {
                backupRanges.push(backupRange);
            }
            // pop the least one
            if (changedRange.getLastBlock() < allocatedRange.getLastBlock()) {
                changedRanges.removeFirst();
            } else {
                allocatedRanges.removeFirst();
            }
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("List<Block>, List<Block> - end"); //$NON-NLS-1$
        }
        return backupRanges;
    }

    public ConnectParams getjVixConnectParams() {
        return this.connectParams;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    private Block getOverlap(final Block changedRange, final Block allocatedRanges) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("Block, Block - start"); //$NON-NLS-1$
        }
        Block returnBlock = null;
        if (changedRange.getBegin() < allocatedRanges.getBegin()) {
            if (changedRange.getLastBlock() <= allocatedRanges.getBegin()) {
                // no overlap

            } else {
                returnBlock = new Block(allocatedRanges.getBegin(),
                        Math.min(changedRange.getLastBlock(), allocatedRanges.getLastBlock()));

            }
        } else {
            if (changedRange.getBegin() < allocatedRanges.getLastBlock()) {
                returnBlock = new Block(Math.max(changedRange.getBegin(), allocatedRanges.getBegin()),
                        Math.min(changedRange.getLastBlock(), allocatedRanges.getLastBlock()));

            }
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("Block, Block - end"); //$NON-NLS-1$
        }
        return returnBlock;
    }

    private void manageEmptyBlocksList(final CoreResultActionDiskBackup radb) throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionDiskBackup - start"); //$NON-NLS-1$
        }

        String msg;
        final GenerationProfile profile = radb.getProfile();
        radb.setNumberOfBlocks(0);

        switch (radb.getBackupMode()) {
        case FULL:
            msg = String.format("Disk is empty. Sectors %d (%s). Skipping diskId %d",
                    profile.getMaxNumberOfSectorsPerBlock(),
                    PrettyNumber.toString(profile.getMaxBlockSize(), MetricPrefix.MEGA), radb.getDiskId());
            break;
        case INCREMENTAL:
            msg = String.format("No blocks change. %d Sectors (%s). Skipping diskId %d",
                    profile.getMaxNumberOfSectorsPerBlock(),
                    PrettyNumber.toString(profile.getMaxBlockSize(), MetricPrefix.MEGA), radb.getDiskId());
            break;
        default:
            msg = "backup mode cannot be undefined";
            break;
        }
        this.logger.info(msg);
        radb.skip(msg);

        profile.setDiskTotalDumpSize(radb.getDiskId(), 0);
        profile.setDiskTotalUncompressedDumpSize(radb.getDiskId(), 0);

        if (radb.isSkipped()) {
            msg = MessagesTemplate.diskHeaderInfo(radb);
            this.logger.info(msg);
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionDiskBackup - end"); //$NON-NLS-1$
        }
    }

    private int normalizeBlocks(final CoreResultActionDiskBackup radb, final List<Block> src,
            final List<BasicBlockInfo> dst, final int maxBlockSize) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionDiskBackup, List<Block>, List<RestoreBlock>, int - start"); //$NON-NLS-1$
        }
        final TreeMap<Long, Block> d = new TreeMap<>();
        for (final Block originalBlock : src) {
            if (originalBlock.length > maxBlockSize) {
                long len = originalBlock.length;
                long offset = originalBlock.offset;

                while (len > 0) {
                    final Block b = new Block();
                    b.length = Math.min(len, maxBlockSize);
                    b.offset = offset;
                    d.put(offset, b);
                    len -= maxBlockSize;
                    offset = b.getLastBlock() + 1;
                }

            } else {
                d.put(originalBlock.offset, originalBlock);
            }

        }
        final int extension = d.size() - src.size();
        int index = 0;
        for (final Entry<Long, Block> entry : d.entrySet()) {
            dst.add(new BasicBlockInfo(entry.getValue(), radb, index));
            ++index;
        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("CoreResultActionDiskBackup, List<Block>, List<RestoreBlock>, int - end"); //$NON-NLS-1$
        }
        return extension;

    }

    private boolean openVmdk(final EntityType entityType, final AbstractCoreResultDiskBackupRestore radb)
            throws JddkException, InterruptedException, CoreResultActionException {

        DiskOpenCloseHandle diskOpen = null;

        switch (entityType) {
        case VirtualMachine:
            final String remoteDiskPath = radb.getProfile().getRemoteDiskPath(radb.getDiskId());
            diskOpen = new DiskOpenCloseHandle(radb.getConnectionHandle(), radb.getFlags(), remoteDiskPath);
            break;
        case ImprovedVirtualDisk:
            diskOpen = new DiskOpenCloseHandle(radb.getConnectionHandle(), radb.getFlags());
            break;
        default:
            throw new JddkException(0, "Unsupported First Class Object");
        }

        SJvddk.queue.add(diskOpen);
        while (!diskOpen.getExecuted().get()) {
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
        }
        boolean result = false;
        if (diskOpen.getVddkCallResult() == jDiskLibConst.VIX_OK) {
            if (!diskOpen.getDiskHandle().isValid()) {
                final String msg = "Invalid DiskHandle - please check logs for more information";
                this.logger.warning(msg);
                radb.failure(msg);
            } else {
                radb.setDiskHandle(diskOpen.getDiskHandle());
                final String transport = getTransportMode(diskOpen.getDiskHandle());
                if (StringUtils.isNotBlank(transport)) {
                    radb.setUsedTransportModes(transport);
                    result = true;
                } else {
                    final String msg = "Invalid Transport Mode - please check logs for more information";
                    this.logger.warning(msg);
                    radb.failure(msg);
                }
            }
        } else {
            final String msg = SJvddk.dli.getErrorText(diskOpen.getVddkCallResult(), null);
            this.logger.warning(msg);
            radb.failure(msg);
        }

        return result;
    }

    public long prepareForAccess(final String identity) throws JVixException {

        if (checkEnableDisablePrivileges()
//                Boolean.TRUE.equals(this.methodAuhorization.get(PrivilegesList.PRIVILEGE_ENABLE_METHOD))
                && CoreGlobalSettings.isEnableForAccessOn()) {
            this.identity = identity;
            final long vddkCallResult = SJvddk.dli.prepareForAccess(this.connectParams, identity);
            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
                this.logger.warning(msg);
            }
            return vddkCallResult;
        } else {
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.info("User is not entitle to the Enable Method");
            }
            return jDiskLibConst.VIX_OK;
        }
    }

    private ArrayList<Block> queryAllocatedBlock(final DiskHandle diskHandle, final JVmdkInfo vmdkInfo)
            throws JVixException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("DiskHandle, JVmdkInfo - start"); //$NON-NLS-1$
        }

        final ArrayList<Block> vixBlocks = new ArrayList<>();
        long offset = 0;
        final long chunkSize = jDiskLibConst.MIN_CHUNK_SIZE;
        final long capacity = vmdkInfo.getCapacityInSectors();
        long numChunk = capacity / chunkSize;
        while (numChunk > 0) {
            final ArrayList<Block> blockList = new ArrayList<>();
            long numChunkToQuery;

            if (numChunk > jDiskLibConst.MAX_CHUNK_NUMBER) {
                numChunkToQuery = jDiskLibConst.MAX_CHUNK_NUMBER;
            } else {
                numChunkToQuery = numChunk;
            }

            final long vddkCallResult = SJvddk.dli.queryAllocatedBlocks(diskHandle, offset, numChunkToQuery * chunkSize,
                    chunkSize, blockList);

            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
                this.logger.warning(msg);
                throw new JVixException(vddkCallResult, msg);
            }

            vixBlocks.addAll(blockList);

            numChunk -= numChunkToQuery;
            offset += numChunkToQuery * chunkSize;

        }
        /*
         * Just add unaligned part even though it may not be allocated.
         */
        final long unalignedPart = capacity % chunkSize;
        if (unalignedPart > 0) {
            final Block block = new Block();
            block.offset = offset;
            block.length = unalignedPart;
            vixBlocks.add(block);
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("DiskHandle, JVmdkInfo - end"); //$NON-NLS-1$
        }
        return vixBlocks;
    }

    /**
     * Query the disk with the right algorithm
     *
     * @param radb
     * @param blockListQueryChangedDiskAreas
     * @param vmdkInfo
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws FileFaultFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws com.vmware.vslm.NotFoundFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidArgumentFaultMsg
     * @throws com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws com.vmware.vim25.InvalidStateFaultMsg
     * @throws JVixException
     */
    private List<Block> queryBlock(final CoreResultActionDiskBackup radb,
            final List<Block> blockListQueryChangedDiskAreas)
            throws FileFaultFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, com.vmware.vslm.FileFaultFaultMsg,
            InvalidArgumentFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, com.vmware.vslm.NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
            com.vmware.vim25.InvalidStateFaultMsg, JVixException {
        if (radb.getBackupMode() == BackupMode.INCREMENTAL) {

            if ((blockListQueryChangedDiskAreas != null)) {
                final List<Block> blockList = new ArrayList<>();
                if (isNoNfcSession() || "san".equalsIgnoreCase(radb.getUsedTransportModes())
                        || (!blockListQueryChangedDiskAreas.isEmpty())
                        || !CoreGlobalSettings.useQueryAllocatedBlocksForIncremental()) {
                    blockList.addAll(blockListQueryChangedDiskAreas);
                } else {
                    final List<Block> blockListQueryAllocatedBlock = queryAllocatedBlock(radb.getDiskHandle(),
                            radb.getJVmdkInfo());
                    blockList.addAll(
                            getIncrementalSectorsRange(blockListQueryChangedDiskAreas, blockListQueryAllocatedBlock));
                }
                return blockList;
            } else {
                radb.setQueryBlocksOption(QueryBlocksOption.CHANGED_AREAS);
            }
        } else {
            QueryBlocksOption queryBlockType = radb.getQueryBlocksOption();
            switch (queryBlockType) {
            case FULL:
                queryBlockType = QueryBlocksOption.FULL;
                radb.setBackupMode(BackupMode.FULL);
                break;
            case ALLOCATED:
                if ((JDiskLibFactory.getVddkVersion().checkVersion("6.7") >= 0) && (!isNoNfcSession())) {
                    queryBlockType = QueryBlocksOption.ALLOCATED;

                } else if (radb.isChangedBlockTrackingEnabled()) {
                    queryBlockType = QueryBlocksOption.CHANGED_AREAS;
                } else {
                    queryBlockType = QueryBlocksOption.FULL;
                }
                break;
            case CHANGED_AREAS:
                if (radb.isChangedBlockTrackingEnabled()) {
                    queryBlockType = QueryBlocksOption.CHANGED_AREAS;
                } else {
                    queryBlockType = QueryBlocksOption.FULL;
                }
                break;
            case UNKNOWS:
                queryBlockType = QueryBlocksOption.FULL;
                break;
            default:
                break;
            }
            radb.setQueryBlocksOption(queryBlockType);

        }

        return getBlockList(radb);

    }

    private void setDiskMetadata(final CoreResultActionDiskRestore radr) {
        final DiskHandle diskHandle = radr.getDiskHandle();
        final Map<String, String> metadata = radr.getProfile().getDiskMetadata(radr.getDiskId());

        for (final Entry<String, String> entry : metadata.entrySet()) {
            final long vddkCallResult = SJvddk.dli.writeMetadata(diskHandle, entry.getKey(), entry.getValue());
            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                final String msg = SJvddk.dli.getErrorText(vddkCallResult, null);
                this.logger.warning(msg);
                break;
            }
        }
    }

}
