/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.control;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLib;
import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLib.ConnectParams;
import com.vmware.jvix.jDiskLib.Connection;
import com.vmware.jvix.jDiskLib.DiskHandle;
import com.vmware.jvix.jDiskLib.Info;
import com.vmware.jvix.jDiskLibFactory;
import com.vmware.vmbk.command.BackupCommand;
import com.vmware.vmbk.command.RestoreCommand;
import com.vmware.vmbk.command.RestoreOptions;
import com.vmware.vmbk.control.info.DumpFileInfo;
import com.vmware.vmbk.control.info.TotalDumpFileInfo;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.Manipulator.VddkConfManipulator;
import com.vmware.vmbk.util.Utility;

public class Jvddk {

    public class CleanUp {
	private int numCleaned;
	public int numRemaining;
	public long result;
    }

    private class DumpThread implements Callable<Boolean> {
	private final Block block;
	private final DiskHandle diskHandle;
	private final int index;
	private final int numSectors;
	private boolean postResult;
	private long result;
	final private Semaphore semaphore;
	private final ITarget target;

	private DumpThread(final int index, final DiskHandle diskHandle, final ITarget target, final int numSectors,
		final Block block) {
	    this.diskHandle = diskHandle;
	    this.target = target;
	    this.numSectors = numSectors;
	    this.block = block;
	    this.index = index;
	    this.semaphore = new Semaphore(1);

	}

	@Override
	public Boolean call() {
	    ByteBuffer buffer = null;
	    final long lastBlockSector = (this.block.offset + this.block.length);
	    int bufferIndex = 0;
	    synchronized (Jvddk.this.buffersAvailable) {
		while (true) {
		    if (isRunning()) {

			if (Jvddk.this.buffersAvailable[bufferIndex]) {
			    Jvddk.this.buffersAvailable[bufferIndex] = false;
			    buffer = Jvddk.this.buffers[bufferIndex];
			    break;
			}
			if (++bufferIndex > Jvddk.this.buffersAvailable.length) {
			    bufferIndex = 0;
			}
			try {
			    Thread.sleep(1000);
			} catch (final InterruptedException e) {

			}
		    } else {
			if (Vmbk.isAbortTriggered()) {
			    return null;
			} else {
			    return true;
			}
		    }
		}
	    }

	    try {
		if (this.target.openPostDump(this.index, this.block, bufferIndex, buffer.capacity())) {
		    for (long startSector = this.block.offset; startSector < lastBlockSector; startSector += this.numSectors) {
			if (isRunning()) {

			    buffer.clear();
			    try {
				logger.finest(String.format("Index %d Sector %d - Semaphore ready to acquire",
					this.index, startSector));
				this.semaphore.acquire();
				logger.finest(String.format("Index %d Sector %d - Semaphore acquired", this.index,
					startSector));
				this.result = dli.Read(this.diskHandle, startSector, this.numSectors, buffer);
			    } finally {
				logger.finest(String.format("Index %d Sector %d - Semaphore ready to release",
					this.index, startSector));
				this.semaphore.release();
				logger.finest(String.format("Index %d Sector %d - Semaphore released", this.index,
					startSector));
			    }
			    if (this.result != jDiskLib.VIX_OK) {
				logger.warning(dli.GetErrorText(this.result, null));
				break;
			    }
			    this.postResult = this.target.postDump(this.index, bufferIndex, buffer);
			    if (!this.postResult) {
				stop();
				return false;
			    }
			}
		    }
		    if (this.postResult) {
			this.postResult = this.target.closePostDump(this.index, bufferIndex);
			if (!this.postResult) {
			    stop();
			    return false;
			}
		    }
		}

	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
		stop();
		return false;
	    } finally {

		Jvddk.this.buffersAvailable[bufferIndex] = true;

		final DumpFileInfo dumpFileInfo = this.target.getDumpFileInfo(this.index);
		if (this.postResult) {
		    Jvddk.this.report[this.index] = IoFunction.showInfo(logger, dumpFileInfo.toString());
		} else {
		    Jvddk.this.report[this.index] = IoFunction.showWarning(logger,
			    "(%4d/%4d) FAILED\t\tsize:%7.2fMB  time:%5.2fs \t%s", dumpFileInfo.index + 1,
			    dumpFileInfo.totalChunk, dumpFileInfo.getSizeInMb(), dumpFileInfo.getOverallTimeInSeconds(),
			    dumpFileInfo.name);
		}

	    }
	    return this.postResult;
	}
    }

    private enum QueryBlock {
	AllocatedBlocks, ChangedDiskAreas, FullBlocks
    }

    private class RestoreThread implements Callable<Boolean> {
	private final Block block;
	private final DiskHandle diskHandle;
	private boolean getResult;
	private final int index;
	private final int numSectors;
	private long result;
	final private Semaphore semaphore;
	private final ITarget target;

	private RestoreThread(final int index, final DiskHandle diskHandle, final ITarget target, final int numSectors,
		final Block block) {
	    this.diskHandle = diskHandle;
	    this.target = target;
	    this.numSectors = numSectors;
	    this.block = block;
	    this.index = index;
	    this.semaphore = new Semaphore(1);

	}

	@Override
	public Boolean call() {
	    ByteBuffer buffer = null;
	    final long lastBlockSector = (this.block.offset + this.block.length);
	    int bufferIndex = 0;
	    synchronized (Jvddk.this.buffersAvailable) {
		while (true) {
		    if (isRunning()) {

			if (Jvddk.this.buffersAvailable[bufferIndex]) {
			    Jvddk.this.buffersAvailable[bufferIndex] = false;
			    buffer = Jvddk.this.buffers[bufferIndex];
			    break;
			}
			if (++bufferIndex > Jvddk.this.buffersAvailable.length) {
			    bufferIndex = 0;
			}
			try {
			    Thread.sleep(1000);
			} catch (final InterruptedException e) {

			}
		    } else {
			if (Vmbk.isAbortTriggered()) {
			    return null;
			} else {
			    return true;
			}
		    }
		}
	    }
	    try {
		if (this.target.openGetDump(this.index, this.block, bufferIndex, buffer.capacity())) {
		    for (long startSector = this.block.offset; startSector < lastBlockSector; startSector += this.numSectors) {
			if (isRunning()) {

			    buffer.clear();
			    this.getResult = this.target.getDump(this.index, bufferIndex, buffer);
			    if (!this.getResult) {
				stop();
				return false;
			    }
			    try {
				this.semaphore.acquire();
				this.result = dli.Write(this.diskHandle, startSector, this.numSectors, buffer);
			    } finally {
				this.semaphore.release();
			    }
			    if (this.result != jDiskLib.VIX_OK) {
				logger.warning(dli.GetErrorText(this.result, null));
				break;
			    }
			}
		    }
		    if (this.getResult) {
			this.getResult = this.target.closeGetDump(this.index, bufferIndex);
			if (!this.getResult) {
			    stop();
			    return false;
			}
		    }
		}

	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    } finally {

		Jvddk.this.buffersAvailable[bufferIndex] = true;
		final DumpFileInfo dumpFileInfo = this.target.getDumpFileInfo(this.index);
		if (this.getResult) {
		    IoFunction.showInfo(logger, dumpFileInfo.toString());

		} else {
		    IoFunction.showWarning(logger, "(%4d/%4d) FAILED\t\tsize:%7.2fMB  time:%5.2fs \t%s",
			    dumpFileInfo.index, dumpFileInfo.totalChunk, dumpFileInfo.getSizeInMb(),
			    dumpFileInfo.getOverallTimeInSeconds(), dumpFileInfo.name);
		}

	    }
	    return this.getResult;
	}
    }

    private class VmdkInfo {
	public int adapterType;

	public long capacityInSectors;
	public HashMap<String, String> metadata;
	public long nBlocks;

	public int numLinks;

	VmdkInfo() {
	    this.adapterType = 0;
	    this.nBlocks = 0;
	    this.numLinks = 0;
	    this.metadata = new HashMap<>();
	}

	@Override
	public String toString() {
	    final StringBuilder str = new StringBuilder();
	    str.append("adapterType= ");
	    switch (this.adapterType) {
	    case 1:
		str.append("ADAPTER_IDE ");
		break;
	    case 2:
		str.append("ADAPTER_SCSI_BUSLOGIC ");
		break;
	    case 3:
		str.append("ADAPTER_SCSI_LSILOGIC ");
		break;
	    case 256:
		str.append("ADAPTER_UNKNOWN ");
		break;
	    default:
		str.append("MISSING DESCRIPTION ");
		break;
	    }
	    str.append(String.format("blocks= %d ", this.nBlocks));
	    str.append(String.format("Links= %d ", this.numLinks));
	    for (final String key : this.metadata.keySet()) {
		str.append(key);
		str.append(" = ");
		str.append(this.metadata.get(key));
		str.append(' ');
	    }
	    return str.toString();
	}
    }

    private static jDiskLib dli = null;

    private static final Logger logger = Logger.getLogger(Jvddk.class.getName());

    private static boolean cleanVddkTemp() {
	logger.entering(Jvddk.class.getName(), "cleanVddkTemp");
	boolean result = true;

	final String vmwareRoot = System.getProperty("java.io.tmpdir") + File.separator + "vmware-"
		+ System.getProperty("user.name");
	if (StringUtils.isNotEmpty(vmwareRoot)) {
	    LoggerUtils.logInfo(logger, "Deleting vddk temporary dir:%s", vmwareRoot);

	    final File vddkTempFolder = new File(vmwareRoot);
	    if (vddkTempFolder.exists()) {
		if (vddkTempFolder.isDirectory() == false) {
		    return false;
		}

		final File[] files = vddkTempFolder.listFiles();
		assert files != null;

		for (final File file : files) {
		    if (file.isDirectory()) {
			result &= Utility.deleteDirectoryRecursive(file);
		    }
		}
	    }
	}
	logger.exiting(Jvddk.class.getName(), "cleanVddkTemp", result);
	return result;
    }

    public static void exit() {
	logger.entering(Jvddk.class.getName(), "exit");
	if (dli != null) {
	    dli.Exit();
	    dli = null;
	}
	try {
	    cleanVddkTemp();
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(Jvddk.class.getName(), "exit");
    }

    static public boolean initialize() {
	long result;
	try {
	    dli = jDiskLibFactory.Get();

	    logger.fine("VddkManager::initialize() begin.\n");
	    result = dli.InitEx(VmbkVersion.getVddkMajor(), VmbkVersion.getVddkMinor(),
		    new com.vmware.vmbk.logger.JVixLoggerImpl(), GlobalConfiguration.getVddkLibPath(),
		    GlobalConfiguration.getVddk_config());
	    LoggerUtils.logInfo(logger, "MAJOR_VERSION:%d MINOR_VERSION:%d PATCH:%d VddkLibPath:%s Vddk_config:%s",
		    VmbkVersion.getVddkMajor(), VmbkVersion.getVddkMinor(), VmbkVersion.getVddkPatchLevel(),
		    GlobalConfiguration.getVddkLibPath(), GlobalConfiguration.getVddk_config());
	    if (result != jDiskLib.VIX_OK) {
		logger.warning(dli.GetErrorText(result, null));
		logger.info("VddkManager Initialization failure.");
		return false;
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    logger.info("VddkManager Initialization failure.");
	    return false;
	}
	logger.info("VddkManager Initialized successful.");
	LoggerUtils.logInfo(logger, "Transport modes available: %s ", dli.ListTransportModes());
	logger.fine("VddkManager::initialize() end.\n");
	return true;
    }

    private final VimConnection basicVimConnection;

    private ByteBuffer buffers[];
    private boolean buffersAvailable[];
    private final int chunkSize = 128;
    private ConnectParams connectParams;

    private Connection connHandle;

    private String identity;

// TODO Remove unused code found by UCDetector
//     int numDiskHandles = 1;

    private String report[];

    private String requestedTransportModes;

    private Boolean running;

    private String usedTransportMode;

    private int totalNumberOfRestoreOperations;
    private int restoreOperationCounter;

    public Jvddk(final VimConnection basicVimConnection) {

	this.connectParams = null;
	this.connHandle = null;

	this.basicVimConnection = basicVimConnection;
	this.restoreOperationCounter = 0;
	this.totalNumberOfRestoreOperations = 0;
    }

    public CleanUp Cleanup() {
	logger.entering(getClass().getName(), "Cleanup");
	final CleanUp result = new CleanUp();
	if (this.connectParams == null) {
	    logger.fine("VddkManager::Cleanup() no connection available.\n");

	} else {

	    final int[] numCleaned = new int[2];
	    final int[] numRemaining = new int[2];
	    final long vddkCallResult = dli.Cleanup(this.connectParams, numCleaned, numRemaining);
	    result.numCleaned = numCleaned[0];
	    result.numRemaining = numRemaining[0];
	    result.result = vddkCallResult;
	    if (vddkCallResult != jDiskLib.VIX_OK) {
		logger.warning(dli.GetErrorText(vddkCallResult, null));
	    } else {
		IoFunction.showInfo(logger, "Cleanup result: Object Cleaned: %d , Object Remaining: %d",
			result.numCleaned, result.numRemaining);
	    }
	}
	logger.exiting(getClass().getName(), "Cleanup", result);
	return result;

    }

    private boolean closeVmdk(final DiskHandle diskHandle) {
	if (diskHandle == null) {
	    return false;
	}
	final long vddkCallResult = dli.Close(diskHandle);
	if (vddkCallResult != jDiskLib.VIX_OK) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	    return false;
	}
	return true;
    }

    public boolean connect(final boolean readOnly, final String transportModes) {
	logger.entering(getClass().getName(), "connect", new Object[] { readOnly, transportModes });
	final boolean result = connect(readOnly, null, transportModes);
	logger.exiting(getClass().getName(), "connect", result);
	return result;
    }

    public boolean connect(final boolean readOnly, final String snapshotRef, final String transportModes) {
	logger.entering(getClass().getName(), "connect", new Object[] { readOnly, snapshotRef, transportModes });
	boolean result = false;
	this.connHandle = jDiskLib.createConnectionHandle();
	this.requestedTransportModes = transportModes;
	logger.info(String.format("connectEx() VM: %s   snapshotRef: %s transportModes: %s", this.connectParams.vmxSpec,
		snapshotRef, transportModes));
	long vddkCallResult;
	if (this.connectParams.specType == jDiskLib.ConnectParams.VIXDISKLIB_SPEC_VSTORAGE_OBJECT) {
	    vddkCallResult = dli.ConnectEx(this.connectParams, readOnly, null, transportModes, this.connHandle);
	} else {
	    vddkCallResult = dli.ConnectEx(this.connectParams, readOnly, snapshotRef, transportModes, this.connHandle);
	}
	result = (vddkCallResult == jDiskLib.VIX_OK);
	if (!result) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	}
	logger.exiting(getClass().getName(), "connect", result);
	return result;
    }

    public boolean disconnect() {
	logger.entering(getClass().getName(), "disconnect");

	final long vddkCallResult = dli.Disconnect(this.connHandle);
	if (vddkCallResult != jDiskLib.VIX_OK) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	}
	this.connHandle = null;
	final boolean result = vddkCallResult == jDiskLib.VIX_OK;
	logger.exiting(getClass().getName(), "disconnect", result);
	return result;
    }

    public boolean doDumpJava(final BackupCommand backupInfo, final FcoArchiveManager vmArcMgr, final ITarget target) {
	logger.entering(getClass().getName(), "doDumpJava", new Object[] { vmArcMgr, backupInfo, target });

	System.gc();
	boolean result = true;
	final GenerationProfile profGen = vmArcMgr.getTargetGeneration();
	final StringBuilder finalReport = new StringBuilder();
	target.setProfGen(profGen);

	final int threadPool = GlobalConfiguration.getMaxPostThreadsPool();

	final FirstClassObject vmm = target.getFcoTarget();
	int flags = 0;
	flags |= jDiskLib.OPEN_READ_ONLY;
	if (this.requestedTransportModes.equalsIgnoreCase("nbdssl")) {
	    flags |= jDiskLib.OPEN_COMPRESSION_SKIPZ;
	}

	DiskHandle diskHandle = null;
	try {
	    if (vmm instanceof VirtualMachineManager) {
		diskHandle = openVmdk(profGen.getRemoteDiskPath(target.getDiskId()), flags);
	    } else {
		diskHandle = openVmdk(flags);
	    }

	    if (!diskHandle.IsValid()) {
		IoFunction.showWarning(logger, "Invalid DiskHandle - please check logs for more information");
		result = false;
	    } else if (this.usedTransportMode == null) {
		IoFunction.showWarning(logger, "Invalid Transport Mode - please check logs for more information");
		result = false;
	    } else {
		finalReport.append(IoFunction.showInfo(logger,
			"Disk: [diskId %d][Handle %d][Flags %s][TMode Requested %s][Tmode Used %s]", target.getDiskId(),
			diskHandle.getHandle(), printFlags(flags), this.requestedTransportModes,
			this.usedTransportMode));

		final ArrayList<Block> vixBlocks = new ArrayList<>();
		final VmdkInfo vmdkInfo = getVmdkInfo(diskHandle);
		logger.info(vmdkInfo.toString());
		if (vmdkInfo != null) {
		    final int maxBlockSize = (GlobalConfiguration.getMaxBlockSize() * (1024 * 1024))
			    / jDiskLib.SECTOR_SIZE;
		    ArrayList<Block> blockList;
		    if (backupInfo.isBackupMode(BackupMode.incr)) {
			blockList = vmm.queryChangedDiskAreas(vmArcMgr, target.getDiskId(), backupInfo.getMode());
		    } else {
			QueryBlock queryBlockType = QueryBlock.AllocatedBlocks;
			if ((VmbkVersion.getVddkMajor() < 6) && (VmbkVersion.getVddkMinor() < 7)) {
			    queryBlockType = QueryBlock.ChangedDiskAreas;
			} else if (isNoNfcSession() && !target.getFcoTarget().isChangedBlockTrackingEnabled()) {
			    queryBlockType = QueryBlock.FullBlocks;
			} else if (isNoNfcSession()) {
			    queryBlockType = QueryBlock.ChangedDiskAreas;
			} else if (vmm.isVmDatastoreNfs()) {
			    queryBlockType = QueryBlock.ChangedDiskAreas;
			} else if (this.usedTransportMode.equalsIgnoreCase("san")) {
			    queryBlockType = QueryBlock.ChangedDiskAreas;
			}
			switch (queryBlockType) {
			case AllocatedBlocks:
			    blockList = queryAllocatedBlock(diskHandle, vmdkInfo);
			    break;
			case ChangedDiskAreas:
			    blockList = vmm.queryChangedDiskAreas(vmArcMgr, target.getDiskId(), backupInfo.getMode());
			    break;
			case FullBlocks:
			default:
			    blockList = vmm.getFullDiskAreas(target.getDiskId());
			    break;

			}

		    }
		    if (blockList == null) {
			IoFunction.showWarning(logger, "Unable to retrieve the block list.Skipping disk %d..",
				target.getDiskId());

			return false;
		    }
		    if (blockList.size() == 0) {
			switch (backupInfo.getMode()) {
			case full:
			    IoFunction.showInfo(logger, "Disk is empty n.sectors %d (%dMB).Skipping disk %d..",
				    maxBlockSize, GlobalConfiguration.getMaxBlockSize(), target.getDiskId());
			    break;
			case incr:
			    IoFunction.showInfo(logger,
				    "No blocks has been changed in %d Sectors (%dMB).Skipping disk %d..", maxBlockSize,
				    GlobalConfiguration.getMaxBlockSize(), target.getDiskId());
			    break;
			case unknow:
			    assert backupInfo.getMode() == BackupMode.unknow : "backup mode cannot be undefined";
			    break;

			}
			result = true;
		    } else {
			LoggerUtils.logInfo(logger,
				"Normalize the block list to the Max block size of %d Sectors (%dMB)", maxBlockSize,
				GlobalConfiguration.getMaxBlockSize());
			final int newEntry = normalizeBlocks(blockList, vixBlocks, maxBlockSize);
			LoggerUtils.logInfo(logger, "Original blocks:%d New added blocks:%d Total blocks:%d ",
				blockList.size(), newEntry, vixBlocks.size());
			ExecutorService es = null;
			try (final ByteArrayInOutStream digestOutput = new ByteArrayInOutStream();
				DataOutputStream outputDigest = new DataOutputStream(digestOutput)) {
			    final int numSectors = this.chunkSize;
			    int index = 0;
			    es = Executors.newFixedThreadPool(threadPool);
			    final List<DumpThread> todo = new ArrayList<>(vixBlocks.size());
			    this.buffers = new ByteBuffer[threadPool];
			    this.buffersAvailable = new boolean[threadPool];
			    for (int i = 0; i < threadPool; i++) {
				this.buffers[i] = ByteBuffer.allocateDirect(numSectors * jDiskLib.SECTOR_SIZE);
				this.buffersAvailable[i] = true;
			    }
			    target.initializePostBuffersArray(vixBlocks.size());
			    long size = 0;
			    for (final Block block : vixBlocks) {
				try {
				    outputDigest.writeLong(block.offset);
				    outputDigest.writeLong(block.length);
				    size += block.length;

				} catch (final IOException e) {
				    logger.warning(Utility.toString(e));
				    IoFunction.showWarning(logger, "Dump disk:%d fails - see log for more details",
					    target.getDiskId());
				    return false;
				}
				final DumpThread r = new DumpThread(index, diskHandle, target, numSectors, block);
				todo.add(r);
				++index;
			    }

			    this.report = new String[index];
			    finalReport.append(
				    IoFunction.showInfo(logger, "Dump: [blocks %d][size %.2fMB][target %s][threads %d]",
					    vixBlocks.size(), (size * jDiskLib.SECTOR_SIZE) / (1024.0f * 1024.0f),
					    target.getTargetName(), threadPool));

			    finalReport.append(
				    IoFunction.showInfo(logger, DumpFileInfo.header(backupInfo.isCompression())));
			    final long startTime = System.nanoTime();
			    this.running = true;
			    final List<Future<Boolean>> answers = es.invokeAll(todo);
			    final long endTime = System.nanoTime();
			    for (final Future<Boolean> answer : answers) {
				try {
				    if (answer.get() == null) {
					IoFunction.showWarning(logger, "Dump disk:%d - Aborted by user",
						target.getDiskId());
					return false;
				    } else if (!answer.get()) {
					IoFunction.showWarning(logger, "Dump disk:%d fails - see log for more details",
						target.getDiskId());
					return false;
				    }
				} catch (final ExecutionException e) {
				    logger.warning(Utility.toString(e));
				    IoFunction.showWarning(logger, "Dump disk:%d fails - see log for more details",
					    target.getDiskId());
				    return false;
				}
			    }
			    if (result) {
				final TotalDumpFileInfo totalDumpInfo = new TotalDumpFileInfo(
					target.getFcoTarget().getType(), target.getDiskId(), target.getDumpFileInfo(),
					startTime, endTime);
				finalReport.append(String.join("", this.report));
				finalReport.append(IoFunction.showInfo(logger, totalDumpInfo.separetorBar()));
				finalReport.append(IoFunction.showInfo(logger, totalDumpInfo.toString()));
				backupInfo.add(totalDumpInfo);
				if (!this.running) {
				    IoFunction.showWarning(logger, "One or more threads failed");
				    finalReport.append(IoFunction.showWarning(logger,
					    "Dump disk:%d fails - see log for more details", target.getDiskId()));
				    return false;
				}
				this.running = false;
				if (!target.postBlockTracks(digestOutput)) {
				    return false;
				}
			    }
			} catch (final InterruptedException | IOException e) {
			    logger.warning(Utility.toString(e));
			    finalReport.append(IoFunction.showWarning(logger,
				    "Dump disk:%d fails - see log for more details", target.getDiskId()));

			    return false;
			} finally {

			    try {
				target.postReport(target.getDiskId(), new ByteArrayInOutStream(finalReport.toString()));
			    } catch (final IOException e) {
				logger.warning(Utility.toString(e));
			    }
			    if (es != null) {
				es.shutdown();
			    }
			}
			finalReport.append(IoFunction.showInfo(logger, "Dump disk:%d success", target.getDiskId()));

		    }
		}
	    }
	} finally {
	    if (diskHandle.IsValid()) {
		closeVmdk(diskHandle);
	    }
	    System.gc();
	}
	logger.exiting(getClass().getName(), "doDumpJava", result);
	return result;
    }

    public boolean doRestoreJava(final FcoArchiveManager vmArcMgr, final RestoreCommand restoreInfo, final int diskId,
	    final String remoteVmdkPath) {
	logger.entering(getClass().getName(), "doRestoreJava",
		new Object[] { vmArcMgr, restoreInfo, diskId, remoteVmdkPath });
	System.gc();
	boolean result = true;
	DiskHandle diskHandle = null;
	int flags = 0;

	if (this.requestedTransportModes.equalsIgnoreCase("nbdssl")) {
	    flags |= jDiskLib.OPEN_COMPRESSION_SKIPZ;
	}
	switch (vmArcMgr.getEntityType()) {
	case VirtualMachine:
	    diskHandle = openVmdk(remoteVmdkPath, flags);

	    break;
	case ImprovedVirtualDisk:
	    diskHandle = openVmdk(flags);
	    break;

	case VirtualApp:
	    break;
	default:
	    IoFunction.showWarning(logger, "Unsupported code path.Type %s is not a valid type to restore",
		    vmArcMgr.getEntityType().toString());
	    return false;
	}

	if ((diskHandle == null) || !diskHandle.IsValid()) {
	    IoFunction.showWarning(logger, "Invalid DiskHandle - please check logs for more information");
	    return false;
	}
	if (this.usedTransportMode == null) {
	    IoFunction.showWarning(logger, "Invalid Transport Mode - please check logs for more information");
	    return false;
	}
	try {
	    IoFunction.showInfo(logger, "Disk: [diskId %d][Handle %d][Flags %s][TMode Requested %s][Tmode Used %s]",
		    diskId, diskHandle.getHandle(), printFlags(flags), this.requestedTransportModes,
		    this.usedTransportMode);
	    final LinkedList<Integer> generations = vmArcMgr.getDependingGenerationList();
	    this.totalNumberOfRestoreOperations = generations.size();
	    this.restoreOperationCounter = 0;
	    result &= doRestoreJava(restoreInfo, diskHandle, vmArcMgr, vmArcMgr.getTargetGeneration(), diskId,
		    remoteVmdkPath);
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));

	} finally {
	    result &= closeVmdk(diskHandle);
	    System.gc();
	}
	logger.exiting(getClass().getName(), "doRestoreJava", result);
	return result;
    }

    public boolean doRestoreJava(final FcoArchiveManager vmArcMgr, final RestoreOptions restoreInfo, final int diskId,
	    final String remoteVmdkPath) {
	logger.entering(getClass().getName(), "doRestoreJava",
		new Object[] { vmArcMgr, restoreInfo, diskId, remoteVmdkPath });
	boolean result = true;
	DiskHandle diskHandle = null;
	int flags = 0;

	if (this.requestedTransportModes.equalsIgnoreCase("nbdssl")) {
	    flags |= jDiskLib.OPEN_COMPRESSION_SKIPZ;
	}
	switch (vmArcMgr.getEntityType()) {
	case VirtualMachine:
	    diskHandle = openVmdk(remoteVmdkPath, flags);

	    break;
	case ImprovedVirtualDisk:
	    diskHandle = openVmdk(flags);
	    break;

	case VirtualApp:
	    break;
	default:
	    IoFunction.showWarning(logger, "Unsupported code path.Type %s is not a valid type to restore",
		    vmArcMgr.getEntityType().toString());
	    return false;
	}

	if ((diskHandle == null) || !diskHandle.IsValid()) {
	    IoFunction.showWarning(logger, "Invalid DiskHandle - please check logs for more information");
	    return false;
	}
	if (this.usedTransportMode == null) {
	    IoFunction.showWarning(logger, "Invalid Transport Mode - please check logs for more information");
	    return false;
	}
	try {
	    IoFunction.showInfo(logger, "Disk: [diskId %d][Handle %d][Flags %s][TMode Requested %s][Tmode Used %s]",
		    diskId, diskHandle.getHandle(), printFlags(flags), this.requestedTransportModes,
		    this.usedTransportMode);
	    final LinkedList<Integer> generations = vmArcMgr.getDependingGenerationList();
	    this.totalNumberOfRestoreOperations = generations.size();
	    this.restoreOperationCounter = 0;
	    result &= doRestoreJava(restoreInfo, diskHandle, vmArcMgr, vmArcMgr.getTargetGeneration(), diskId,
		    remoteVmdkPath);
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));

	} finally {
	    result &= closeVmdk(diskHandle);

	}
	logger.exiting(getClass().getName(), "doRestoreJava", result);
	return result;
    }

    private boolean doRestoreJava(final RestoreCommand restoreInfo, final DiskHandle diskHandle,
	    final FcoArchiveManager vmArcMgr, final GenerationProfile profGen, final int diskId,
	    final String remoteVmdkPath) throws IOException {
	boolean result = true;
	final ArrayList<Block> vixBlocks = new ArrayList<>();
	final ITarget target = vmArcMgr.getRepositoryTarget();
	if (diskHandle == null) {
	    return false;
	}

	if (profGen.getDiskBackupMode(diskId) != BackupMode.full) {
	    final GenerationProfile previousGeneration = vmArcMgr
		    .loadProfileGeneration(profGen.getPreviousGenerationId());
	    if (previousGeneration == null) {
		result = false;
	    } else {
		result = doRestoreJava(restoreInfo, diskHandle, vmArcMgr, previousGeneration, diskId, remoteVmdkPath);
		if (!result) {
		    IoFunction.showWarning(logger, "Restore of generation %d failed",
			    previousGeneration.getGenerationId());
		}
	    }
	}
	if (result) {
	    target.setProfGen(profGen);
	    vixBlocks.addAll(readDigest(new ByteArrayInputStream(vmArcMgr.getRepositoryTarget().getBlockTracks())));
	    IoFunction.showInfo(logger, "Restore (  %d/  %d )", ++this.restoreOperationCounter,
		    this.totalNumberOfRestoreOperations);
	    IoFunction.showInfo(logger,
		    "#########################################################################################################");

	    if (vixBlocks.size() > 0) {
		final int numSectors = this.chunkSize;
		int index = 0;
		final int threadPool = GlobalConfiguration.getMaxGetThreadsPool();
		final ExecutorService es = Executors.newFixedThreadPool(threadPool);
		final List<RestoreThread> todo = new ArrayList<>(vixBlocks.size());
		this.buffers = new ByteBuffer[threadPool];
		this.buffersAvailable = new boolean[threadPool];
		for (int i = 0; i < threadPool; i++) {
		    this.buffers[i] = ByteBuffer.allocateDirect(numSectors * jDiskLib.SECTOR_SIZE);
		    this.buffersAvailable[i] = true;
		}
		target.initializeGetBuffersArray(vixBlocks.size());
		long size = 0;
		for (final Block block : vixBlocks) {
		    final RestoreThread callableThread = new RestoreThread(index, diskHandle, target, numSectors,
			    block);
		    todo.add(callableThread);
		    size += block.length;
		    ++index;
		}
		IoFunction.showInfo(logger, "[generation %d ][mode %s][Blocks %d][size %.2fMB][source %s][threads %d]",
			profGen.getGenerationId(), profGen.getBackupMode().toString(), vixBlocks.size(),
			(size * jDiskLib.SECTOR_SIZE) / (1024.0f * 1024.0f), target.getTargetName(), threadPool);
		try {
		    IoFunction.showInfo(logger, DumpFileInfo.header(profGen.isDiskCompressed(diskId)));
		    final long startTime = System.nanoTime();
		    this.running = true;
		    final List<Future<Boolean>> answers = es.invokeAll(todo);
		    final long endTime = System.nanoTime();

		    for (final Future<Boolean> answer : answers) {
			try {
			    if (answer.get() == null) {
				IoFunction.showWarning(logger, "Restore disk:%d - Aborted by user", target.getDiskId());
				return false;
			    }
			    if (!answer.get()) {
				IoFunction.showWarning(logger, "Restore disk:%d fails - see log for more details",
					target.getDiskId());
				return false;
			    }
			} catch (final ExecutionException e) {
			    logger.warning(Utility.toString(e));
			    IoFunction.showWarning(logger, "Restore disk:%d fails - see log for more details",
				    target.getDiskId());
			    return false;
			}

		    }
		    final TotalDumpFileInfo totalDumpInfo = new TotalDumpFileInfo(target.getFcoTarget().getType(),
			    target.getDiskId(), target.getDumpFileInfo(), startTime, endTime);
		    IoFunction.showInfo(logger, totalDumpInfo.separetorBar());
		    IoFunction.showInfo(logger, totalDumpInfo.toString());
		    restoreInfo.add(totalDumpInfo);

		} catch (final InterruptedException e) {
		    logger.warning(Utility.toString(e));
		} finally {
		    if (es != null) {
			es.shutdown();
		    }
		}
	    } else {

		IoFunction.showInfo(logger, "Restore: [generation %d][mode %s][no blocks] -  Skip",
			profGen.getGenerationId(), profGen.getBackupMode().toString());
	    }
	}
	return result;

    }

    private boolean doRestoreJava(final RestoreOptions restoreInfo, final DiskHandle diskHandle,
	    final FcoArchiveManager vmArcMgr, final GenerationProfile profGen, final int diskId,
	    final String remoteVmdkPath) throws IOException {
	boolean result = true;
	final ArrayList<Block> vixBlocks = new ArrayList<>();
	final ITarget target = vmArcMgr.getRepositoryTarget();
	if (diskHandle == null) {
	    return false;
	}

	if (profGen.getDiskBackupMode(diskId) != BackupMode.full) {
	    final GenerationProfile previousGeneration = vmArcMgr
		    .loadProfileGeneration(profGen.getPreviousGenerationId());
	    if (previousGeneration == null) {
		result = false;
	    } else {
		result = doRestoreJava(restoreInfo, diskHandle, vmArcMgr, previousGeneration, diskId, remoteVmdkPath);
		if (!result) {
		    IoFunction.showWarning(logger, "Restore of generation %d failed",
			    previousGeneration.getGenerationId());
		}
	    }
	}
	if (result) {
	    target.setProfGen(profGen);
	    vixBlocks.addAll(readDigest(new ByteArrayInputStream(vmArcMgr.getRepositoryTarget().getBlockTracks())));
	    IoFunction.showInfo(logger, "Restore (  %d/  %d )", ++this.restoreOperationCounter,
		    this.totalNumberOfRestoreOperations);
	    IoFunction.showInfo(logger,
		    "#########################################################################################################");

	    if (vixBlocks.size() > 0) {
		final int numSectors = this.chunkSize;
		int index = 0;
		final int threadPool = GlobalConfiguration.getMaxGetThreadsPool();
		final ExecutorService es = Executors.newFixedThreadPool(threadPool);
		final List<RestoreThread> todo = new ArrayList<>(vixBlocks.size());
		this.buffers = new ByteBuffer[threadPool];
		this.buffersAvailable = new boolean[threadPool];
		for (int i = 0; i < threadPool; i++) {
		    this.buffers[i] = ByteBuffer.allocateDirect(numSectors * jDiskLib.SECTOR_SIZE);
		    this.buffersAvailable[i] = true;
		}
		target.initializeGetBuffersArray(vixBlocks.size());
		long size = 0;
		for (final Block block : vixBlocks) {
		    final RestoreThread callableThread = new RestoreThread(index, diskHandle, target, numSectors,
			    block);
		    todo.add(callableThread);
		    size += block.length;
		    ++index;
		}
		IoFunction.showInfo(logger, "[generation %d ][mode %s][Blocks %d][size %.2fMB][source %s][threads %d]",
			profGen.getGenerationId(), profGen.getBackupMode().toString(), vixBlocks.size(),
			(size * jDiskLib.SECTOR_SIZE) / (1024.0f * 1024.0f), target.getTargetName(), threadPool);
		try {
		    IoFunction.showInfo(logger, DumpFileInfo.header(profGen.isDiskCompressed(diskId)));
		    final long startTime = System.nanoTime();
		    this.running = true;
		    final List<Future<Boolean>> answers = es.invokeAll(todo);
		    final long endTime = System.nanoTime();

		    for (final Future<Boolean> answer : answers) {
			try {
			    if (answer.get() == null) {
				IoFunction.showWarning(logger, "Restore disk:%d - Aborted by user", target.getDiskId());
				return false;
			    }
			    if (!answer.get()) {
				IoFunction.showWarning(logger, "Restore disk:%d fails - see log for more details",
					target.getDiskId());
				return false;
			    }
			} catch (final ExecutionException e) {
			    logger.warning(Utility.toString(e));
			    IoFunction.showWarning(logger, "Restore disk:%d fails - see log for more details",
				    target.getDiskId());
			    return false;
			}

		    }
		    final TotalDumpFileInfo totalDumpInfo = new TotalDumpFileInfo(target.getFcoTarget().getType(),
			    target.getDiskId(), target.getDumpFileInfo(), startTime, endTime);
		    IoFunction.showInfo(logger, totalDumpInfo.separetorBar());
		    IoFunction.showInfo(logger, totalDumpInfo.toString());
		    restoreInfo.add(totalDumpInfo);

		} catch (final InterruptedException e) {
		    logger.warning(Utility.toString(e));
		} finally {
		    if (es != null) {
			es.shutdown();
		    }
		}
	    } else {

		IoFunction.showInfo(logger, "Restore: [generation %d][mode %s][no blocks] -  Skip",
			profGen.getGenerationId(), profGen.getBackupMode().toString());
	    }
	}
	return result;

    }

    public boolean endAccess() {
	logger.entering(getClass().getName(), "endAccess");

	final long vddkCallResult = dli.EndAccess(this.connectParams, this.identity);
	if (vddkCallResult != jDiskLib.VIX_OK) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	}
	this.identity = null;

	final boolean result = vddkCallResult == jDiskLib.VIX_OK;
	logger.exiting(getClass().getName(), "prepareForAccess", result);
	return result;
    }

    public VimConnection getBasicVimConnection() {
	return this.basicVimConnection;
    }

    public String getRequestedTransportModes() {
	return this.requestedTransportModes;
    }

    private VmdkInfo getVmdkInfo(final DiskHandle diskHandle) {
	final Info diskInfo = new Info();
	final long vddkCallResult = dli.GetInfo(diskHandle, diskInfo);
	if (vddkCallResult != jDiskLib.VIX_OK) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	    return null;
	}
	final VmdkInfo vmdkInfo = new VmdkInfo();
	vmdkInfo.adapterType = diskInfo.adapterType;
	vmdkInfo.nBlocks = diskInfo.capacityInSectors / jDiskLib.SECTOR_SIZE;
	vmdkInfo.capacityInSectors = diskInfo.capacityInSectors;
	vmdkInfo.numLinks = diskInfo.numLinks;

	final String keys[] = dli.GetMetadataKeys(diskHandle);
	int i;
	for (i = 0; i < keys.length; i++) {
	    String value;

	    value = dli.ReadMetadata(diskHandle, keys[i]);
	    vmdkInfo.metadata.put(keys[i], value);
	}
	return vmdkInfo;
    }

    private boolean isNoNfcSession() {
	try (final VddkConfManipulator vddk = new VddkConfManipulator(GlobalConfiguration.getVddk_config())) {
	    if ((vddk != null) && vddk.isNoNfcSession()) {
		return true;
	    }
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));
	}
	return false;
    }

    public synchronized boolean isRunning() {
	final Boolean val;
	val = this.running && !Vmbk.isAbortTriggered();
	return val;
    }

    public boolean isStopped() {
	final Boolean val;
	synchronized (this.running) {
	    val = this.running;
	}
	return !val;
    }

    private int normalizeBlocks(final ArrayList<Block> src, final ArrayList<Block> dst, final int maxBlockSize) {
	int extension = 0;
	for (final Block originalBlock : src) {
	    if (originalBlock.length > maxBlockSize) {
		Block prevBlock = originalBlock;
		Block newBlock = null;
		do {
		    ++extension;
		    newBlock = new Block();
		    newBlock.offset = prevBlock.offset + maxBlockSize;
		    newBlock.length = prevBlock.length - maxBlockSize;
		    prevBlock.length = maxBlockSize;
		    dst.add(prevBlock);
		    prevBlock = newBlock;
		} while (newBlock.length > maxBlockSize);
		dst.add(newBlock);
	    } else {
		dst.add(originalBlock);
	    }
	}
	return extension;

    }

    private DiskHandle openVmdk(final int flags) {
	final DiskHandle diskHandle = new DiskHandle();
	logger.info("Open()  IVDisk : ");

	final long vddkCallResult = dli.Open(this.connHandle, null, flags, diskHandle);

	if (vddkCallResult != jDiskLib.VIX_OK) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	    return null;
	}
	this.usedTransportMode = dli.GetTransportMode(diskHandle);

	return diskHandle;
    }

    private DiskHandle openVmdk(final String remoteDiskPath, final int flags) {
	final DiskHandle diskHandle = new DiskHandle();
	logger.info("Open()  Disk : " + remoteDiskPath);
	final long vddkCallResult = dli.Open(this.connHandle, Utility.removeQuote(remoteDiskPath), flags, diskHandle);
	if (vddkCallResult != jDiskLib.VIX_OK) {
	    logger.warning(dli.GetErrorText(vddkCallResult, null));
	    return null;
	}
	this.usedTransportMode = dli.GetTransportMode(diskHandle);

	return diskHandle;
    }

    public long prepareForAccess(final String identity) {
	logger.entering(getClass().getName(), "prepareForAccess", identity);
	this.identity = identity;
	long vddkCallResult = jDiskLib.VIX_OK;
	for (int i = 0; i < 10; i++) {
	    vddkCallResult = dli.PrepareForAccess(this.connectParams, identity);
	    if (vddkCallResult == jDiskLib.VIX_OK) {
		break;
	    } else {
		logger.warning(dli.GetErrorText(vddkCallResult, null));
		if (vddkCallResult == jDiskLib.VIX_E_OPERATION_DISABLED) {
		    break;
		}
		if (vddkCallResult == jDiskLib.VIX_E_HOST_USER_PERMISSIONS) {
		    break;
		}
		try {
		    Thread.sleep(10);
		} catch (final InterruptedException e) {
		    logger.warning(Utility.toString(e));
		}
	    }
	}
	logger.exiting(getClass().getName(), "prepareForAccess", vddkCallResult);
	return vddkCallResult;
    }

    private String printFlags(final int flags) {
	final StringBuilder printFlags = new StringBuilder();
	printFlags.append(((flags & jDiskLib.OPEN_UNBUFFERED) == jDiskLib.OPEN_UNBUFFERED) ? "OPEN_UNBUFFERED " : "");
	printFlags
		.append(((flags & jDiskLib.OPEN_SINGLE_LINK) == jDiskLib.OPEN_SINGLE_LINK) ? "OPEN_SINGLE_LINK " : "");
	printFlags.append(((flags & jDiskLib.OPEN_READ_ONLY) == jDiskLib.OPEN_READ_ONLY) ? "OPEN_READ_ONLY " : "");
	printFlags.append(
		((flags & jDiskLib.OPEN_COMPRESSION_ZLIB) == jDiskLib.OPEN_COMPRESSION_ZLIB) ? "OPEN_COMPRESSION_ZLIB "
			: "");
	printFlags.append(((flags & jDiskLib.OPEN_COMPRESSION_FASTLZ) == jDiskLib.OPEN_COMPRESSION_FASTLZ)
		? "OPEN_COMPRESSION_FASTLZ "
		: "");
	printFlags.append(((flags & jDiskLib.OPEN_COMPRESSION_SKIPZ) == jDiskLib.OPEN_COMPRESSION_SKIPZ)
		? "OPEN_COMPRESSION_SKIPZ "
		: "");
	return printFlags.toString();
    }

    private ArrayList<Block> queryAllocatedBlock(final DiskHandle diskHandle, final VmdkInfo vmdkInfo) {
	final ArrayList<Block> vixBlocks = new ArrayList<>();
	long offset = 0;

	final long capacity = vmdkInfo.capacityInSectors;
	long numChunk = capacity / this.chunkSize;

	while (numChunk > 0) {
	    final ArrayList<Block> blockList = new ArrayList<>();
	    long numChunkToQuery;

	    if (numChunk > jDiskLib.MAX_CHUNK_NUMBER) {
		numChunkToQuery = jDiskLib.MAX_CHUNK_NUMBER;
	    } else {
		numChunkToQuery = numChunk;
	    }

	    final long vddkCallResult = dli.QueryAllocatedBlocks(diskHandle, offset, numChunkToQuery * this.chunkSize,
		    this.chunkSize, blockList);

	    if (vddkCallResult != jDiskLib.VIX_OK) {
		logger.warning(dli.GetErrorText(vddkCallResult, null));

	    }

	    vixBlocks.addAll(blockList);

	    numChunk -= numChunkToQuery;
	    offset += numChunkToQuery * this.chunkSize;
	}

	/*
	 * Just add unaligned part even though it may not be allocated.
	 */
	final long unalignedPart = capacity % this.chunkSize;
	if (unalignedPart > 0) {
	    final Block block = new Block();
	    block.offset = offset;
	    block.length = unalignedPart;
	    vixBlocks.add(block);
	}
	return vixBlocks;
    }

    private ArrayList<Block> readDigest(final ByteArrayInputStream digestOutPath) throws FileNotFoundException {
	final ArrayList<Block> vixBlocks = new ArrayList<>();

	DataInputStream inputDigest = null;
	inputDigest = new DataInputStream(digestOutPath);

	try {
	    while (true) {
		final Block block = new Block();
		block.offset = inputDigest.readLong();
		block.length = inputDigest.readLong();
		vixBlocks.add(block);

	    }
	} catch (final EOFException e1) {
	    logger.fine(String.format("Added %d Blocks ", vixBlocks.size()));

	} catch (final IOException e1) {
	    logger.warning(Utility.toString(e1));
	} finally {
	    try {
		if (inputDigest != null) {
		    inputDigest.close();
		}
	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
	    }
	}

	return vixBlocks;
    }

    public jDiskLib.ConnectParams setConnectParams(final jDiskLib.ConnectParams connectParams) {
	logger.entering(getClass().getName(), "setConnectParams", new Object[] { connectParams });
	this.connectParams = connectParams;
	logger.exiting(getClass().getName(), "setConnectParams", this.connectParams);
	return this.connectParams;
    }

    private synchronized void stop() {
	logger.info("keep alive stopped");
	this.running = false;
    }

}
