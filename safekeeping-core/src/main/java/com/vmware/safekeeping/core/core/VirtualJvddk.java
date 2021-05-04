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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.core.command.interactive.AbstractVirtualBackupDiskInteractive;
import com.vmware.safekeeping.core.command.options.CoreBackupRestoreCommonOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionGetGenerationProfile;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.BasicBlockInfo;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public class VirtualJvddk implements IJvddkBasic {

	final Logger logger;

	private VimConnection basicVimConnection;

	public VirtualJvddk(final Logger logger) {
		this.logger = logger;
	}

	//
	public VirtualJvddk(final Logger logger, final ImprovedVirtualDisk ivd) throws JVixException {
		this(ivd.getVimConnection(), logger);

	}

	public VirtualJvddk(final Logger logger, final VirtualMachineManager vmm) throws JVixException {
		this(vmm.getVimConnection(), logger);

	}

	private VirtualJvddk(final VimConnection vimConnection, final Logger logger) throws JVixException {
		this.logger = logger;
		this.basicVimConnection = vimConnection;

	}

	/**
	 *
	 * @param vixBlocks
	 * @param radc
	 * @param target
	 * @param diskInteractive
	 * @return
	 * @throws CoreResultActionException
	 */
	private String doConsolidate(final List<BasicBlockInfo> vixBlocks, final CoreResultActionDiskVirtualBackup radc,
			final ITargetOperation target, final AbstractVirtualBackupDiskInteractive interactive)
			throws CoreResultActionException {
		if (this.logger.isLoggable(Level.CONFIG)) {
			this.logger.config(
					"List<RestoreBlock>, CoreResultActionDiskConsolidate, TargetOperation, AbstractRestoreInteractive - start"); //$NON-NLS-1$
		}
		final GenerationProfile profile = radc.getProfile();
		Buffers buffers = null;
		final StringBuilder finalReport = new StringBuilder();

		String msg = null;
		try {
			if (!vixBlocks.isEmpty()) {

				/*
				 * Initialize buffers
				 */
				final int threadPool = radc.getNumberOfThreads();
				final int maxBlockSizeInBytes = radc.getMaxBlockSizeInBytes();
				buffers = new Buffers(target, threadPool, maxBlockSizeInBytes, radc.getFcoEntityInfo());

				/*
				 * end buffer initializations
				 */

				final List<IRestoreThread> futureThreads = new ArrayList<>(vixBlocks.size());

				/**
				 * Start Section DumpThreads
				 */
				interactive.startDumpThreads();

				final String[] report = new String[vixBlocks.size()];
				for (final BasicBlockInfo blockIn : vixBlocks) {

					final ExBlockInfo exBlockInfo = new ExBlockInfo(blockIn, vixBlocks.size(), target.getDisksPath());
					exBlockInfo.setGenerationId(radc.getProfile().getGenerationId());
					final VirtualBackupThread callableThread = new VirtualBackupThread(exBlockInfo, buffers, radc, report,
							interactive, this.logger);
					futureThreads.add(callableThread);
				}
				msg = MessagesTemplate.diskHeaderInfo(radc);
				this.logger.info(msg);
				finalReport.append(msg);
				finalReport.append('\n');
				msg = MessagesTemplate.diskDumpHeaderInfo(radc);
				this.logger.info(msg);
				finalReport.append(msg);
				finalReport.append('\n');
				msg = MessagesTemplate.header(true);
				this.logger.info(msg);
				finalReport.append(msg);
				finalReport.append('\n');
				buffers.start();
				TotalBlocksInfo totalDumpInfo;
				try {
					totalDumpInfo = restoreAndConsolidateThreads(radc, interactive, futureThreads);
					// wait for subtask to finish
					buffers.waitSubTasks();
				} finally {
					buffers.stop();
				}
				for (final IRestoreThread s : futureThreads) {
					profile.addDumpInfo(radc.getDiskId(), s.getBlockInfo());
				}
				if (totalDumpInfo != null) {
					finalReport.append(String.join("\n", report));
					finalReport.append('\n');
					finalReport.append(totalDumpInfo.separetorBar());
					finalReport.append('\n');
					msg = totalDumpInfo.toString();
					finalReport.append(msg);
					this.logger.info(msg);
					profile.setDiskTotalDumpSize(radc.getDiskId(), totalDumpInfo.getStreamSize());
					profile.setDiskTotalUncompressedDumpSize(radc.getDiskId(), totalDumpInfo.getSize());
				} else {
					this.logger.warning("Threads failed");
				}
			} else {
				msg = "No blocks to restore";
				this.logger.info(msg);
				radc.skip(msg);
			}
		} catch (final NoSuchAlgorithmException e) {
			this.logger.severe(
					"List<RestoreBlock>, CoreResultActionDiskConsolidate, TargetOperation, AbstractRestoreInteractive - exception: " //$NON-NLS-1$
							+ e);
			radc.failure(e);
		} catch (final InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
		if (radc.isRunning()) {
			msg = String.format("Dump disk:%d success", radc.getDiskId());
			this.logger.info(msg);
			finalReport.append(msg);
		} else {
			msg = String.format("Dump disk:%d failed: %s", radc.getDiskId(), radc.getReason());
			this.logger.warning(msg);
			finalReport.append(msg);
		}
		return finalReport.toString();
	}

	/**
	 * Start restore
	 *
	 * @param vmArcMgr
	 * @param profGen
	 * @param restoreInfo
	 * @param diskId
	 * @param radc
	 * @param diskInteractive
	 * @return
	 * @throws CoreResultActionException
	 */
	public CoreResultActionDiskVirtualBackup doVirtualBackupJava(final FcoArchiveManager vmArcMgr,
			final CoreBackupRestoreCommonOptions restoreInfo, final CoreResultActionDiskVirtualBackup radc,
			final AbstractVirtualBackupDiskInteractive diskInteractive) throws CoreResultActionException {
		if (this.logger.isLoggable(Level.CONFIG)) {
			this.logger.config(
					"FcoArchiveManager, CoreRestoreOptions, CoreResultActionDiskConsolidate, AbstractRestoreInteractive - start"); //$NON-NLS-1$
		}

		String msg = null;

		/**
		 * Start Section CalculateNumberOfGenerationDiskRestore
		 */
		diskInteractive.startCalculateNumberOfGenerationDiskRestore();

		radc.setNumberOfThreads(restoreInfo.getNumberOfThreads());
		radc.setTargetName(vmArcMgr.getRepositoryTarget().getTargetName());
		final CoreResultActionGetGenerationProfile raggp = getGenerationProfiles(vmArcMgr, radc.getSourceProfile(),
				radc);
		if (raggp.isSuccessful()) {
			final List<BasicBlockInfo> vixRestoreBlock = new ConsolidateBlocks(radc).compute();
			if (!vixRestoreBlock.isEmpty()) {
				diskInteractive.endCalculateNumberOfGenerationDiskRestore();
				if (this.logger.isLoggable(Level.INFO)) {
					msg = MessagesTemplate.getDiskGenerationsInfo(radc);
					this.logger.info(msg);
				}
				/**
				 * End Section CalculateNumberOfGenerationDiskRestore
				 */
				doConsolidate(vixRestoreBlock, radc, vmArcMgr.getRepositoryTarget(), diskInteractive);
			}
		} else {
			radc.failure(raggp.getReason());
		}

		if (this.logger.isLoggable(Level.CONFIG)) {
			this.logger.config(
					"FcoArchiveManager, CoreRestoreOptions, CoreResultActionDiskConsolidate, AbstractRestoreInteractive - end"); //$NON-NLS-1$
		}
		return radc;
	}

	public VimConnection getBasicVimConnection() {
		return this.basicVimConnection;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

}
