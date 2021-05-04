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
package com.vmware.safekeeping.core.command.interactive;

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
import com.vmware.safekeeping.core.soap.VimTask;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.enums.phase.VirtualBackupVappPhases;

public abstract class AbstractVirtualBackupVappInteractive implements IBackupInteractive {
    /**
     * Total percentage progress increment to complete a full dump the remain part
     * is assigned to pre and post dump operations
     */

    private static final float SINGLE_STEP_INCREASE = 0.1F;
    private final CoreResultActionVappVirtualBackup raVirtualBackupFco;
    private final AbstractVirtualBackupVappInteractive parent;

    private final List<IBackupInteractive> childrenInteractive;

    private float progressPercentagePerDisk;
    private final VimTask task;

    protected AbstractVirtualBackupVappInteractive(final CoreResultActionVappVirtualBackup rab) {
        this(rab, null);
    }

    /**
     * @param rab
     */
    protected AbstractVirtualBackupVappInteractive(final CoreResultActionVappVirtualBackup rab,
            final AbstractVirtualBackupVappInteractive parent) {

        this.raVirtualBackupFco = rab;
        this.childrenInteractive = new ArrayList<>();
        this.parent = parent;
        this.task = new VimTask(rab.getFirstClassObject(), TaskOperationType.VIRTUALBACKUP);

        if (parent != null) {
            parent.getChildrenInteractive().add(this);
        }

    }

    @Override
    public float childIncrement(final float value) {
        return increment(value * this.progressPercentagePerDisk);
    }

    public void endChildrenVirtualBackup() {
        setPhase(VirtualBackupVappPhases.END_CHILDREN_CONSOLIDATE);
    }

    @Override
    public void endFinalizeProfile() {
        setPhase(VirtualBackupVappPhases.END_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    //
    public void endGenerationComputation() {
        setPhase(VirtualBackupVappPhases.END_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRetreiveVappChildCollection() {
        setPhase(VirtualBackupVappPhases.END_RETRIEVE_CHILD_COLLECTION);

    }

    public void endRetrieveProfile() {
        setPhase(VirtualBackupVappPhases.END_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    @Override
    public void finish() {
        task.done(raVirtualBackupFco);
        setPhase(VirtualBackupVappPhases.END);
    }

    public List<IBackupInteractive> getChildrenInteractive() {
        return this.childrenInteractive;
    }

    /**
     * @return the numberOfChildVm
     */
    public int getNumberOfChildVm() {
        return this.raVirtualBackupFco.getNumberOfChildVm();
    }

    public AbstractVirtualBackupVappInteractive getParent() {
        return this.parent;
    }

    /**
     * @return the rabVm
     */
    public CoreResultActionVappVirtualBackup getRaVirtualBackupFco() {
        return this.raVirtualBackupFco;
    }

    float increment(final float value) {
        if (this.parent != null) {
            this.parent.childIncrement(value);
        }
        final float actualValue = this.raVirtualBackupFco.progressIncrease(value);
        this.task.update(actualValue);
        return actualValue;
    }

    public abstract AbstractVirtualBackupInteractive newVirtualBackupVmInteractiveInstance(
            final CoreResultActionVmVirtualBackup rab);

    private void setPhase(final VirtualBackupVappPhases phase) {

        this.raVirtualBackupFco.setPhase(phase);
    }

    @Override
    public void start() {
        this.task.start();
        setPhase(VirtualBackupVappPhases.START);
    }

    public void startChildrenVirtualBackup() {
        setPhase(VirtualBackupVappPhases.START_CHILDREN_CONSOLIDATE);
    }

    @Override
    public void startFinalizeProfile() {
        setPhase(VirtualBackupVappPhases.START_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startGenerationComputation() {
        setPhase(VirtualBackupVappPhases.START_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startRetreiveVappChildCollection() {
        setPhase(VirtualBackupVappPhases.START_RETRIEVE_CHILD_COLLECTION);

    }

    public void startRetrieveProfile() {
        setPhase(VirtualBackupVappPhases.START_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }
}
