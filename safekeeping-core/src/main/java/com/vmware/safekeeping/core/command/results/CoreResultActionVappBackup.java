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

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.AtomicEnum;
import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.core.command.interactive.AbstractBackupVappInteractive;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.phase.BackupVappPhases;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;

public class CoreResultActionVappBackup extends AbstractCoreResultActionBackup
        implements ICoreResultActionVappBackupSupport {

    /**
     * 
     */
    private static final long serialVersionUID = 9098537142979990793L;
    private final AtomicEnum<BackupVappPhases> phase;
    private volatile int numberOfChildVm;
    protected final ConcurrentDoublyLinkedList<CoreResultActionVmBackup> resultActionOnsChildVm;
    private final ConcurrentDoublyLinkedList<ManagedFcoEntityInfo> fcoChildren;

    public CoreResultActionVappBackup(final IFirstClassObject fco, final CoreBackupOptions options) {
        super(fco, options);
        this.resultActionOnsChildVm = new ConcurrentDoublyLinkedList<>();
        this.fcoChildren = new ConcurrentDoublyLinkedList<>();
        this.phase = new AtomicEnum<>(BackupVappPhases.NONE);
    }

    /**
     * @return the fcoChildren
     */
    public ConcurrentDoublyLinkedList<ManagedFcoEntityInfo> getFcoChildren() {
        return this.fcoChildren;
    }

    @Override
    public VirtualAppManager getFirstClassObject() {
        return (VirtualAppManager) super.getFirstClassObject();
    }

    @Override
    public AbstractBackupVappInteractive getInteractive() {
        return (AbstractBackupVappInteractive) this.interactive;
    }

    /**
     * @return the numberOfChildVm
     */
    public int getNumberOfChildVm() {
        return this.numberOfChildVm;
    }

    @Override
    public CoreBackupOptions getOptions() {
        return (CoreBackupOptions) this.options;
    }

    public BackupVappPhases getPhase() {
        return this.phase.get();
    }

    /**
     * @return the resultActionOnsChildVm
     */

    @Override
    public ConcurrentDoublyLinkedList<CoreResultActionVmBackup> getResultActionOnsChildVm() {
        return this.resultActionOnsChildVm;
    }

    public CoreResultActionVmBackup getResultActionOnsChildVm(final Integer key) {
        return this.resultActionOnsChildVm.toArrayList().get(key);
    }

    @Override
    public OperationState getState() {
        OperationState result = super.getState();
        if ((result == OperationState.SUCCESS)) {
            for (final CoreResultActionVmBackup rab : this.getResultActionOnsChildVm()) {

                if ((result == OperationState.SUCCESS)) {
                    OperationState vmResult = OperationState.SUCCESS;
                    String vmResultReason = StringUtils.EMPTY;
                    switch (rab.getState()) {
                    case ABORTED:
                        vmResult = OperationState.ABORTED;
                        break;
                    case FAILED:
                        if (vmResult == OperationState.ABORTED) {
                            vmResult = OperationState.ABORTED;
                        } else {
                            vmResult = OperationState.FAILED;
                            vmResultReason = rab.getReason();
                        }

                        break;
                    case SKIPPED:
                        switch (vmResult) {
                        case ABORTED:
                            vmResult = OperationState.ABORTED;
                            break;
                        case FAILED:
                            vmResult = OperationState.FAILED;
                            vmResultReason = rab.getReason();
                            break;
                        case STARTED:
                            vmResult = OperationState.SKIPPED;
                            break;
                        case SKIPPED:
                            vmResult = OperationState.SKIPPED;
                            vmResultReason = rab.getReason();
                            break;
                        case SUCCESS:
                            vmResult = OperationState.SUCCESS;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SUCCESS:
                        vmResult = OperationState.SUCCESS;
                        break;
                    default:
                        break;
                    }
                    result = vmResult;
                    setReason(vmResultReason);
                }
            }
        }
        if (isDone() && (getEndDate() == null)) {
            setEndTime();
        }
        return result;

    }

    public void setInteractive(final AbstractBackupVappInteractive interactive) {
        this.interactive = interactive;
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
    public void setPhase(final BackupVappPhases phase) {
        this.phase.set(phase);
    }

}
