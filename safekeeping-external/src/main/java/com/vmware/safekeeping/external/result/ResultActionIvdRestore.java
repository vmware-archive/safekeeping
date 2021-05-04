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
package com.vmware.safekeeping.external.result;

import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.external.command.support.Task;
import com.vmware.safekeeping.external.type.RestoreIvdManagedInfo;

public class ResultActionIvdRestore extends AbstractResultActionRestoreForEntityWithDisks {
    public static void convert(final CoreResultActionIvdRestore src, final ResultActionIvdRestore dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionRestoreForEntityWithDisks.convert(src, dst);
        RestoreIvdManagedInfo.convert(src.getManagedInfo(), dst.getManagedInfo());
        try {

            if (src.getResultActionOnDisk() != null) {
                dst.setResultActionOnDisk(new Task(src.getResultActionOnDisk()));
            }
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private Task resultActionOnDisk;
    private RestoreIvdManagedInfo managedInfo;

    public ResultActionIvdRestore() {
        this.managedInfo = new RestoreIvdManagedInfo();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionIvdRestore.convert((CoreResultActionIvdRestore) src, this);
    }

    /**
     * @return the managedInfo
     */
    public RestoreIvdManagedInfo getManagedInfo() {
        return this.managedInfo;
    }

    /**
     * @return the resultActionOnDisk
     */
    public Task getResultActionOnDisk() {
        return this.resultActionOnDisk;
    }

    /**
     * @param managedInfo the managedInfo to set
     */
    public void setManagedInfo(final RestoreIvdManagedInfo managedInfo) {
        this.managedInfo = managedInfo;
    }

    /**
     * @param resultActionOnDisk the resultActionOnDisk to set
     */
    public void setResultActionOnDisk(final Task resultActionOnDisk) {
        this.resultActionOnDisk = resultActionOnDisk;
    }

}
