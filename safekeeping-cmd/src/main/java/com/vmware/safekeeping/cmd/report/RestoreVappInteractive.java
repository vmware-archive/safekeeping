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

import com.vmware.safekeeping.core.command.interactive.AbstractRestoreInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreVappInteractive;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.info.RestoreTotalDumpFileInfo;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class RestoreVappInteractive extends AbstractRestoreVappInteractive {

	/**
	 * @param rar
	 */
	public RestoreVappInteractive(final CoreResultActionVappRestore rar) {
		super(rar);

	}

	@Override
	public void endRestoreVappMetadata() {
		super.endRestoreVappMetadata();
		IoFunction.println(MessagesTemplate.getRestoreManagedInfo(getRaFcoRestore()));
		IoFunction.println(MessagesTemplate.getLocationString(getRaFcoRestore()));

	}

	@Override
	public void endRetreiveVappChildCollection() {
		super.endRetreiveVappChildCollection();
		IoFunction.printf("Contains %d VMs:", getNumberOfChildVm());
		int index = 0;
		IoFunction.println();
		for (final ManagedFcoEntityInfo child : getRaFcoRestore().getFcoChildren()) {
			++index;
			IoFunction.printf("\t%d - %s", index, child.toString());
			IoFunction.println();
		}
		IoFunction.println();
	}

	@Override
	public void endVappRetrieveProfile() {
		super.endVappRetrieveProfile();
		CoreResultActionLoadProfile ralp = null;
		for (final AbstractCoreResultActionImpl op : getRaFcoRestore().getSubOperations()) {

			if (op instanceof CoreResultActionLoadProfile) {
				ralp = (CoreResultActionLoadProfile) op;

			}
		}
		if ((ralp != null) && (ralp.getProfile() != null)) {
			IoFunction.printf("Restore source:  %s :", ralp.getProfile().getFcoEntity().toString());
			IoFunction.println();
		}

	}

	/**
	*
	*/
	@Override
	public void finish() {
		super.finish();
		if (!getvAppTotalDumpInfoList().isEmpty()) {
			final RestoreTotalDumpFileInfo restoreTotalDumpFileInfo = new RestoreTotalDumpFileInfo(
					getvAppTotalDumpInfoList());
			IoFunction.println(restoreTotalDumpFileInfo.separetorBar());
			IoFunction.println("Restore Summary:");
			IoFunction.println(restoreTotalDumpFileInfo.toString());
		}
	}

	@Override
	public AbstractRestoreInteractive newRestoreVmInteractiveInstance(final CoreResultActionVmRestore rab) {
		return new RestoreInteractive(rab, this);
	}

}
