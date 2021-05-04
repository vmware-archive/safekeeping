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
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.phase.RestoreVappPhases;
import com.vmware.safekeeping.external.command.support.Task;
import com.vmware.safekeeping.external.type.RestoreVappManagedInfo;

public class ResultActionVappRestore extends ResultActionRestore {
    public static void convert(final CoreResultActionVappRestore src, final ResultActionVappRestore dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionBackupRestore.convert(src, dst);
        try {
            dst.setPhase(src.getPhase());
            dst.setNumberOfChildVm(src.getNumberOfChildVm());
            RestoreVappManagedInfo.convert(src.getManagedInfo(), dst.getManagedInfo());
            for (final AbstractCoreResultActionBackupRestore _rabChild : src.getResultActionOnsChildVm()) {
                dst.getResultActionOnChildVms().add(new Task(_rabChild));
            }
            dst.getFcoChildren().addAll(src.getFcoChildren());

        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private RestoreVappManagedInfo managedInfo;
    private RestoreVappPhases phase;
    private List<ManagedFcoEntityInfo> fcoChildren;

    private int numberOfChildVm;
    private List<Task> resultActionOnChildVms;

    public ResultActionVappRestore() {
        this.fcoChildren = new LinkedList<>();
        this.resultActionOnChildVms = new LinkedList<>();
        this.managedInfo = new RestoreVappManagedInfo();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionVappRestore.convert((CoreResultActionVappRestore) src, this);
    }

    /**
     * @return the fcoChildren
     */
    public List<ManagedFcoEntityInfo> getFcoChildren() {
        return this.fcoChildren;
    }

    /**
     * @return the managedInfo
     */
    public RestoreVappManagedInfo getManagedInfo() {
        return this.managedInfo;
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
    public RestoreVappPhases getPhase() {
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
     * @param managedInfo the managedInfo to set
     */
    public void setManagedInfo(final RestoreVappManagedInfo managedInfo) {
        this.managedInfo = managedInfo;
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
    public void setPhase(final RestoreVappPhases phase) {
        this.phase = phase;
    }

    /**
     * @param resultActionOnChildVms the resultActionOnChildVms to set
     */
    public void setResultActionOnChildVms(final List<Task> resultActionOnChildVms) {
        this.resultActionOnChildVms = resultActionOnChildVms;
    }

}
