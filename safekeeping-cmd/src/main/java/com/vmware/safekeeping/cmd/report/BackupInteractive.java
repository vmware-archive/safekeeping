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

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.interactive.AbstractBackupInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractBackupVappInteractive;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.type.enums.EntityType;

public class BackupInteractive extends AbstractBackupInteractive {

	private static final String LINE_SEPARATOR = "##################################################################################################################################################################################################################";

	/**
	 * @param rab
	 */
	public BackupInteractive(final AbstractCoreResultActionBackupForEntityWithDisks rab,
			final AbstractBackupVappInteractive parent) {
		super(rab, parent);
	}

	@Override
	public void endDiscoverBackupMode() {
		super.endDiscoverBackupMode();
		if (getRaFcoBackup() != null) {
			IoFunction.println(MessagesTemplate.getGenerationInfo(getRaFcoBackup()));
		}

	}

	@Override
	public void endInfoCollection() {
		super.endInfoCollection();

		if (!getRaFcoBackup().isCbtEnabled()) {
			IoFunction.printf(
					"Change Tracking Block (CBT) disabled. Virtual Machine Skipped. Enable CBT using: vm -cbt on vm:%s or use the option -force",
					getRaFcoBackup().getFcoEntityInfo().getUuid());
			IoFunction.println();
		}

		if ((getRaFcoBackup() != null) && (getRaFcoBackup().getEntityType() == EntityType.VirtualMachine)
				&& ((CoreResultActionVmBackup) getRaFcoBackup()).isTemplate()) {
			IoFunction.printf("Convert template to Virtual Machine");
		}

	}

	@Override
	public void finish() {
		super.finish();
		if (getParent() != null) {
			IoFunction.printf("Backup of %s %s", getRaFcoBackup().getFcoToString(),
					getRaFcoBackup().getState().toString());
			IoFunction.println();
			if (StringUtils.isNotEmpty(getRaFcoBackup().getReason())) {
				IoFunction.printf(" - Reason: %s", getRaFcoBackup().getReason());
				IoFunction.println();
			}
		}
	}

	@Override
	public BackupDiskInteractive newDiskInteractiveInstance(final CoreResultActionDiskBackup resultAction) {
		return new BackupDiskInteractive(resultAction, this);

	}

	@Override
	public void start() {
		super.start();
		if (getParent() != null) {
			IoFunction.println();
			IoFunction.println(BackupInteractive.LINE_SEPARATOR);
			IoFunction.printf("Backup of %s start.", getRaFcoBackup().getFcoToString());

		} else {
			IoFunction.printf("Backup of %s start.", getRaFcoBackup().getFcoToString());
		}

		IoFunction.println();

	}

	@Override
	public void startGenerationComputation() {
		super.startGenerationComputation();
		switch (getRaFcoBackup().getEntityType()) {
		case ImprovedVirtualDisk:
			IoFunction.println(MessagesTemplate.getLocationString(getRaFcoBackup()));
			break;
		case VirtualMachine:
			IoFunction.println(MessagesTemplate.getHeaderGuestInfoString((CoreResultActionVmBackup) getRaFcoBackup()));
			IoFunction.println(MessagesTemplate.getLocationString(getRaFcoBackup()));
			break;
		case VirtualApp:
		case K8sNamespace:
		default:
			break;
		}
	}

}
