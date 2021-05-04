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

import com.vmware.safekeeping.core.command.interactive.AbstractRemoveProfileInteractive;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveProfile;
import com.vmware.safekeeping.core.control.IoFunction;

public class RemoveProfileInteractive extends AbstractRemoveProfileInteractive {
	protected static Logger logger = Logger.getLogger("RemoveProfileInteractive");

	/**
	 * @param archiveRemoveProfile
	 */
	public RemoveProfileInteractive(final CoreResultActionArchiveRemoveProfile archiveRemoveProfile) {
		super(archiveRemoveProfile);
	}

	@Override
	public void endRemoveFcoProfileContent() {
		super.endRemoveFcoProfileContent();
		IoFunction.print("\t-> Archive content removed.");
		IoFunction.println();
	}

	@Override
	public void endRemoveFcoProfileMetadata() {
		super.endRemoveFcoProfileMetadata();
		IoFunction.println("\t-> Profile metadata removed.");
	}

	@Override
	public void endUpdateFcoProfilesCatalog() {
		super.endUpdateFcoProfilesCatalog();
		IoFunction.println("\t-> Fco Profile Catalog updated.");
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void start() {
		super.start();
		IoFunction.showInfo(logger, "Removing profile: %s", getArchiveRemoveProfile().getFcoToString());

	}

	@Override
	public void startRemoveFcoProfileContent() {
		super.startRemoveFcoProfileContent();
	}

	@Override
	public void startRemoveFcoProfileMetadata() {
		super.startRemoveFcoProfileMetadata();
	}

	@Override
	public void startUpdateFcoProfilesCatalog() {
		super.startUpdateFcoProfilesCatalog();
	}

}
