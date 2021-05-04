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
import java.util.logging.Logger;

import com.vmware.safekeeping.core.command.results.ICoreResultActionVappBackupSupport;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.profile.GenerationProfile;

public interface IVappOperationsSupport {
	default boolean finalizeProfile(final ICoreResultActionVappBackupSupport rab)
			throws NoSuchAlgorithmException, IOException {
		final GenerationProfile profile = rab.getProfile();
		final ITargetOperation target = profile.getTargetOperation();
		final FcoArchiveManager fcoArcMgr = profile.getFcoArchiveManager();
		boolean result = false;
		/**
		 * Start Section FinalizeProfile
		 */
		rab.getInteractive().startFinalizeProfile();
		fcoArcMgr.getGenerationsCatalog().setGenerationNotDependent(profile.getGenerationId());
		result = fcoArcMgr.finalizeBackup(profile, rab.getChildVmActionsResult());
		if (!result) {
			getLogger().warning("Backup finalization failed.");
		} else {
			target.postGenerationProfile(profile);
			if (target.postMd5(profile)) {
				fcoArcMgr.postGenerationsCatalog();
				result = true;
			}

		}
		rab.getInteractive().endFinalizeProfile();
		/**
		 * End Section FinalizeProfile
		 */
		return result;
	}

	Logger getLogger();

}
