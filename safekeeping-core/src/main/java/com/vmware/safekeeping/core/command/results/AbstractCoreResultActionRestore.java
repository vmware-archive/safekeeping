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

import com.vmware.safekeeping.core.command.interactive.IRestoreInteractive;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractCoreResultActionRestore extends AbstractCoreResultActionBackupRestore {
	/**
     * 
     */
    private static final long serialVersionUID = -8470823177783617934L;

    private Integer restoreFcoIndex;

	private final ManagedFcoEntityInfo originalFcoInfo;

	protected IRestoreInteractive interactive;

	private boolean onErrorDestroyFco;

	private boolean onErrorDestroyDirectory;

	AbstractCoreResultActionRestore(final IFirstClassObject fco, final CoreRestoreOptions options) {
		super(fco, options);
		this.originalFcoInfo = fco.getFcoInfo();
	}

	public IRestoreInteractive getInteractive() {
		return this.interactive;
	}

	/**
	 * @return
	 */
	public abstract CoreRestoreManagedInfo getManagedInfo();

	@Override
	public CoreRestoreOptions getOptions() {
		return (CoreRestoreOptions) this.options;
	}

	public ManagedFcoEntityInfo getOriginalFcoInfo() {
		return this.originalFcoInfo;
	}

	/**
	 * @return the restoreFcoIndex
	 */
	public Integer getRestoreFcoIndex() {
		return this.restoreFcoIndex;
	}

	public boolean isOnErrorDestroyDirectory() {
		return this.onErrorDestroyDirectory;
	}

	public boolean isOnErrorDestroyFco() {
		return this.onErrorDestroyFco;
	}

	public void setInteractive(final IRestoreInteractive interactive) {
		this.interactive = interactive;
	}

	public void setOnErrorDestroyDirectory(final boolean onErrorDestroyDirectory) {
		this.onErrorDestroyDirectory = onErrorDestroyDirectory;
	}

	public void setOnErrorDestroyFco(final boolean onErrorDestroyFco) {
		this.onErrorDestroyFco = onErrorDestroyFco;
	}

	/**
	 * @param restoreFcoIndex the restoreFcoIndex to set
	 */
	public void setRestoreFcoIndex(final Integer restoreFcoIndex) {
		this.restoreFcoIndex = restoreFcoIndex;
	}
}
