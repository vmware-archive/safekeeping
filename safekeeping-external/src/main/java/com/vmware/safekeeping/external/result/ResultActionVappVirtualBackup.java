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

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.phase.VirtualBackupVappPhases;
import com.vmware.safekeeping.external.command.support.Task;

public class ResultActionVappVirtualBackup extends AbstractResultActionVirtualBackup {

    public static void convert(final CoreResultActionVappVirtualBackup src, final ResultActionVappVirtualBackup dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        try {
            AbstractResultActionVirtualBackup.convert(src, dst);
            dst.setPhase(src.getPhase());
            dst.setNumberOfChildVm(src.getNumberOfChildVm());
            dst.getFcoChildren().addAll(src.getFcoChildren());
            if (src.getResultActionOnsChildVm().isEmpty()) {
                dst.setNumberOfChildVm(src.getNumberOfChildVm());
            }
            for (final AbstractCoreResultActionBackupRestore _rabChild : src.getResultActionOnsChildVm()) {
                dst.getResultActionOnChildVms().add(new Task(_rabChild));
            }
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private List<Task> resultActionOnChildVms;
    private List<ManagedFcoEntityInfo> fcoChildren;
    private VirtualBackupVappPhases phase;

    private int numberOfChildVm;

    public ResultActionVappVirtualBackup() {
        this.resultActionOnChildVms = new LinkedList<>();
        this.fcoChildren = new LinkedList<>();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionVappVirtualBackup.convert((CoreResultActionVappVirtualBackup) src, this);
    }

    public List<ManagedFcoEntityInfo> getFcoChildren() {
        return this.fcoChildren;
    }

    /**
     * @return the numberOfChildVm
     */
    public int getNumberOfChildVm() {
        return this.numberOfChildVm;
    }

    /**
     * @return the phase
     */
    @Override
    public VirtualBackupVappPhases getPhase() {
        return this.phase;
    }

    /**
     * @return the resultActionOnChildVms
     */

    public List<Task> getResultActionOnChildVms() {
        return this.resultActionOnChildVms;
    }

    /**
     * @param fcoChildren the fcoChildren to set
     */

    public void setFcoChildren(final List<ManagedFcoEntityInfo> fcoChildren) {
        this.fcoChildren = fcoChildren;
    }

    /**
     * @param numberOfChildVm the numberOfChildVm to set
     */
    public void setNumberOfChildVm(final int numberOfChildVm) {
        this.numberOfChildVm = numberOfChildVm;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(final VirtualBackupVappPhases phase) {
        this.phase = phase;
    }

    /**
     * @param resultActionOnChildVms the resultActionOnChildVms to set
     */

    public void setResultActionOnChildVms(final List<Task> resultActionOnChildVms) {
        this.resultActionOnChildVms = resultActionOnChildVms;
    }

}
