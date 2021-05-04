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
package com.vmware.safekeeping.external.type.options;

import com.vmware.safekeeping.core.command.options.CoreBackupRestoreCommonOptions;

public class BackupRestoreCommonOptions extends AbstractBasicCommandOptions {

    public static void convert(final BackupRestoreCommonOptions src, final CoreBackupRestoreCommonOptions dst,
            final int defaultAnyFcoOfType) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractBasicCommandOptions.convert(src, dst, defaultAnyFcoOfType);

        dst.setForce(src.force);
        if (src.numberOfThreads != null) {
            dst.setNumberOfThreads(src.numberOfThreads);
        }
        dst.setNoVmdk(src.noVmdk);

    }

    private Integer numberOfThreads;

    private boolean noVmdk;
    private boolean force;

    /**
     * @return the numberOfThreads
     */
    public Integer getNumberOfThreads() {
        return this.numberOfThreads;
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
        return this.noVmdk;
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
    public void setNoVmdk(final boolean noVmdk) {
        this.noVmdk = noVmdk;
    }

    /**
     * @param numberOfThreads the numberOfThreads to set
     */
    public void setNumberOfThreads(final Integer numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

//    /**
//     * @param requestedTransportMode the requestedTransportMode to set
//     */
//    public void setRequestedTransportMode(final String requestedTransportMode) {
//	this.requestedTransportMode = requestedTransportMode;
//    }

}
