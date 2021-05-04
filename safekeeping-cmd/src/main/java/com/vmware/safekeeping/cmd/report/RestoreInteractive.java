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

import com.vmware.safekeeping.core.command.interactive.AbstractRestoreDiskInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreInteractive;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestoreForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.info.RestoreTotalDumpFileInfo;
import com.vmware.safekeeping.core.logger.MessagesTemplate;

public class RestoreInteractive extends AbstractRestoreInteractive {

	private static final String LINE_SEPARATOR = "##################################################################################################################################################################################################################";

	/**
	 * @param rar
	 * @param restoreVappInteractive
	 */
	public RestoreInteractive(final AbstractCoreResultActionRestoreForEntityWithDisks rar,
			final RestoreVappInteractive parent) {
		super(rar, parent);
	}

	@Override
	public void endRestoreMetadata() {
		super.endRestoreMetadata();
		IoFunction.println(MessagesTemplate.getRestoreManagedInfo(getRaFcoRestore()));
		IoFunction.println(MessagesTemplate.getLocationString(getRaFcoRestore()));

	}

	/**
	 *
	 */
	@Override
	public void endRetrieveProfile() {
		super.endRetrieveProfile();
		CoreResultActionLoadProfile ralp = null;
		for (final AbstractCoreResultActionImpl op : getRaFcoRestore().getSubOperations()) {
			if (op instanceof CoreResultActionLoadProfile) {
				ralp = (CoreResultActionLoadProfile) op;
			}
		}
		if ((ralp != null) && (ralp.getProfile() != null)) {
			IoFunction.printf("Restore source: \"%s\" (%s) uuid:%s.", ralp.getProfile().getFcoEntity().getName(),
					ralp.getProfile().getFcoEntity().getMorefValue(), ralp.getProfile().getFcoEntity().getUuid());
			IoFunction.println();
		}
	}

	/**
	*
	*/
	@Override
	public void finish() {
		super.finish();
		if (getParent() != null) {
			if (!isTotalDumpsInfoEmpty()) {
				final RestoreTotalDumpFileInfo restoreTotalDumpFileInfo = new RestoreTotalDumpFileInfo(
						getTotalDumpsInfo());
				IoFunction.println(restoreTotalDumpFileInfo.separetorBar());
				IoFunction.println("Child Restore Summary:");
				IoFunction.println(restoreTotalDumpFileInfo.toString());
			}
		} else if (!isTotalDumpsInfoEmpty()) {
			final RestoreTotalDumpFileInfo restoreTotalDumpFileInfo = new RestoreTotalDumpFileInfo(getTotalDumpsInfo());
			IoFunction.println(restoreTotalDumpFileInfo.separetorBar());
			IoFunction.println("Restore Summary:");
			IoFunction.println(restoreTotalDumpFileInfo.toString());
		} else {
		}

	}

	@Override
	public AbstractRestoreDiskInteractive newDiskInteractiveInstance(final CoreResultActionDiskRestore resultAction) {
		return new RestoreDiskInteractive(resultAction, this);
	}

	/**
	 *
	 */
	@Override
	public void start() {
		super.start();
		if (getParent() != null) {
			IoFunction.println();
			IoFunction.println(RestoreInteractive.LINE_SEPARATOR);
			IoFunction.printf("Start restore child %d: %s", getRaFcoRestore().getIndex(),
					getRaFcoRestore().getFcoToString());
			IoFunction.println();
		}
	}

	/**
	 *
	 */
	@Override
	public void startReconfiguration() {
		super.startReconfiguration();
		final CoreRestoreOptions options = getRaFcoRestore().getOptions();
		if (options != null) {
			if (options.isParentAVApp()) {
				IoFunction.println("Moving Virtual Machine inside the vApp.");
			}
			if (options.isNoVmdk()) {
				IoFunction.println("No disks will be restored. --novmdk option selected.");
			}
			if ((options.getRequestedTransportModes() != null)
					&& options.getRequestedTransportModes().contains("san")) {
				IoFunction.println("SAN transport mode selected. Enable restore with snapshot ");
			}
		} else {
			IoFunction.println(getRaFcoRestore().getFcoToString() + " no Options available.");
		}

	}

}
