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
package com.vmware.safekeeping.cmd.report;

import java.util.logging.Logger;

import com.vmware.safekeeping.core.command.interactive.AbstractBackupDiskInteractive;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.logger.MessagesTemplate;

public class BackupDiskInteractive extends AbstractBackupDiskInteractive {

	protected static Logger logger = Logger.getLogger("BackupReport");

	/**
	 * @param rab
	 */
	public BackupDiskInteractive(final CoreResultActionDiskBackup radb, final BackupInteractive backupInteractive) {
		super(radb, backupInteractive);
	}

	@Override
	public void dumpFailure(final ExBlockInfo dumpFileInfo) {
		super.dumpFailure(dumpFileInfo);
		IoFunction.println(MessagesTemplate.dumpInfo(dumpFileInfo));
	}

	/**
	 * @param dumpFileInfo
	 */
	@Override
	public void dumpSuccess(final ExBlockInfo dumpFileInfo) {
		super.dumpSuccess(dumpFileInfo);
		IoFunction.println(MessagesTemplate.dumpInfo(dumpFileInfo));
	}

	/**
	 * @param totalDumpInfo
	 */
	@Override
	public void endDumpsTotalCalculation(final TotalBlocksInfo totalDumpInfo) {
		super.endDumpsTotalCalculation(totalDumpInfo);
		IoFunction.println(totalDumpInfo.toString());
	}

	@Override
	public void endDumpThreads(final OperationState state) {
		super.endDumpThreads(state);
		IoFunction.println(MessagesTemplate.separatorBar(true));
		switch (state) {
		case ABORTED:
		case FAILED:
		case SKIPPED:
			IoFunction.println(MessagesTemplate.diskHeaderInfo(getRaDiskBackup()));
			IoFunction.println(getRaDiskBackup().getReason());
			IoFunction.println(MessagesTemplate.separatorBar(true));
			break;

		case QUEUED:
		case STARTED:
			break;
		case SUCCESS:
		default:
			break;

		}
	}

	@Override
	public void startDumpThreads() {
		super.startDumpThreads();
		IoFunction.println(MessagesTemplate.diskHeaderInfo(getRaDiskBackup()));
		IoFunction.println(MessagesTemplate.diskDumpHeaderInfo(getRaDiskBackup()));
		IoFunction.println(MessagesTemplate.header(true));

	}

}
