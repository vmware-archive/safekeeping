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
package com.vmware.safekeeping.core.command.results;

import com.vmware.safekeeping.core.command.interactive.IBackupInteractive;
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractCoreResultActionVirtualBackup extends AbstractCoreResultActionBackupRestore {
	/**
     * 
     */
    private static final long serialVersionUID = 8927922806789587433L;
    private boolean cbtEnabled;
	protected IBackupInteractive interactive;

	private final BackupMode backupMode;
	private GenerationProfile sourceProfile;

	/**
	 *
	 * @param fco
	 * @param options
	 */
	AbstractCoreResultActionVirtualBackup(final IFirstClassObject fco, final CoreVirtualBackupOptions options) {
		super(fco, options);
		this.cbtEnabled = false;
		this.backupMode = BackupMode.FULL;
	}

	/**
	 * @return the backupMode
	 */
	public BackupMode getBackupMode() {
		return this.backupMode;
	}

	public IBackupInteractive getInteractive() {
		return this.interactive;
	}

	@Override
	public CoreVirtualBackupOptions getOptions() {
		return (CoreVirtualBackupOptions) this.options;
	}

	public GenerationProfile getSourceProfile() {
		return this.sourceProfile;
	}

	/**
	 * @return the cbtEnabled
	 */
	public boolean isCbtEnabled() {
		return this.cbtEnabled;
	}

	/**
	 * @param cbtEnabled the cbtEnabled to set
	 */
	public void setCbtEnabled(final boolean cbtEnabled) {
		this.cbtEnabled = cbtEnabled;
	}

	public void setInteractive(final IBackupInteractive interactive) {
		this.interactive = interactive;
	}

	/**
	 * @param generationProfile the locations to set
	 */
	public abstract void setLocations(final GenerationProfile profile);

	public void setSourceProfile(final GenerationProfile sourceProfile) {
		this.sourceProfile = sourceProfile;
	}

}
