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

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.type.GuestInfoFlags;
import com.vmware.safekeeping.external.command.support.Task;

public class ResultActionVmBackup extends AbstractResultActionBackupForEntityWithDisks {
    public static void convert(final CoreResultActionVmBackup src, final ResultActionVmBackup dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        try {
            AbstractResultActionBackupForEntityWithDisks.convert(src, dst);

            dst.setGuestFlags(src.getGuestFlags());
            dst.setTemplate(src.isTemplate());

            for (final CoreResultActionDiskBackup _radb : src.getResultActionsOnDisk()) {
                dst.getResultActionsOnDisk().add(new Task(_radb));
            }
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private List<Task> resultActionsOnDisk;

    private boolean template;

    private GuestInfoFlags guestFlags;

    public ResultActionVmBackup() {
        setResultActionsOnDisk(new LinkedList<>());
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionVmBackup.convert((CoreResultActionVmBackup) src, this);
    }

    /**
     * @return the guestFlags
     */
    public GuestInfoFlags getGuestFlags() {
        return this.guestFlags;
    }

    /**
     *
     * @return the subTaskResultActionsOnDisks
     */
    public List<Task> getResultActionsOnDisk() {
        return this.resultActionsOnDisk;
    }

    /**
     *
     * /**
     *
     * @return the template
     */
    public boolean isTemplate() {
        return this.template;
    }

    /**
     * @param guestFlags the guestFlags to set
     */
    public void setGuestFlags(final GuestInfoFlags guestFlags) {
        this.guestFlags = guestFlags;
    }

    /**
     * @param subTaskResultActionsOnDisks the subTaskResultActionsOnDisks to set
     */
    public void setResultActionsOnDisk(final List<Task> subTaskResultActionsOnDisks) {
        this.resultActionsOnDisk = subTaskResultActionsOnDisks;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(final boolean template) {
        this.template = template;
    }

}
