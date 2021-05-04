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
package com.vmware.safekeeping.core.command.options;

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

public class CoreBackupRestoreCommonOptions extends CoreBasicCommandOptions {
    /**
     * 
     */
    private static final long serialVersionUID = -2237135692493256961L;

    private int numberOfThreads;

    private boolean isNoVmdk;

    private String requestedTransportModes;

    private boolean force;

    public CoreBackupRestoreCommonOptions() {
        this.numberOfThreads = CoreGlobalSettings.getMaxPostThreadsPool();
        this.requestedTransportModes = null;
        this.force = false;
        this.isNoVmdk = false;
    }

    /**
     * @return the numberOfThreads
     */
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    /**
     * @return the requestedTransportMode
     */
    public String getRequestedTransportModes() {
        return this.requestedTransportModes;
    }

    /**
     * @return the force
     */
    public boolean isForce() {
        return this.force;
    }

    /**
     * @return the isNoVmdk
     */
    public boolean isNoVmdk() {
        return this.isNoVmdk;
    }

    /**
     * @param force the force to set
     */
    public void setForce(final boolean force) {
        this.force = force;
    }

    /**
     * @param isNoVmdk the isNoVmdk to set
     */
    public void setNoVmdk(final boolean isNoVmdk) {
        this.isNoVmdk = isNoVmdk;
    }

    /**
     * @param numberOfThreads the numberOfThreads to set
     */
    public void setNumberOfThreads(final int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * @param requestedTransportMode the requestedTransportMode to set
     */
    public void setRequestedTransportModes(final String requestedTransportMode) {
        this.requestedTransportModes = requestedTransportMode;
    }
}
