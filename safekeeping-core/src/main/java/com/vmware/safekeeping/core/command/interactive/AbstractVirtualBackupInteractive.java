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

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskVirtualBackup;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.soap.VimTask;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.enums.phase.VirtualBackupPhases;

public abstract class AbstractVirtualBackupInteractive implements IBackupInteractive {
    /**
     * Total percentage progress increment to complete a full dump the remain part
     * is assigned to pre and post dump operations
     */
    private static final float ONE_HUNDRED_PER_CENT = 100F;
    private static final float REMAINING_PERCENTAGE = 88;
    private static final float NUMBER_OF_TRANSACTIONS_WITH_PERCENTAGE_INCREMENT = 8F;

    private static final float SINGLE_STEP_INCREASE = (ONE_HUNDRED_PER_CENT - REMAINING_PERCENTAGE)
            / NUMBER_OF_TRANSACTIONS_WITH_PERCENTAGE_INCREMENT;

    private final AbstractCoreResultActionVirtualBackupForEntityWithDisks raFcoVirtualBackup;
    private final AbstractVirtualBackupVappInteractive parent;

    private final List<AbstractVirtualBackupDiskInteractive> disksInteractive;

    private float progressPercentagePerDisk;
    private final VimTask task;

    protected AbstractVirtualBackupInteractive(final AbstractCoreResultActionVirtualBackupForEntityWithDisks rab) {
        this(rab, null);
    }

    /**
     * @param rab
     */
    protected AbstractVirtualBackupInteractive(final AbstractCoreResultActionVirtualBackupForEntityWithDisks rac,
            final AbstractVirtualBackupVappInteractive parent) {

        this.raFcoVirtualBackup = rac;
        this.disksInteractive = new ArrayList<>();
        this.parent = parent;
        this.task = new VimTask(rac.getFirstClassObject(), TaskOperationType.VIRTUALBACKUP);

        if (parent != null) {
            parent.getChildrenInteractive().add(this);
        }

    }

    @Override
    public float childIncrement(final float value) {
        return increment(value * this.progressPercentagePerDisk);
    }

    public void endDisksVirtualBackup() {
        setPhase(VirtualBackupPhases.END_DISKS_CONSOLIDATE);
        increment(SINGLE_STEP_INCREASE);
    }

    @Override
    public void endFinalizeProfile() {
        setPhase(VirtualBackupPhases.END_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    //
    public void endGenerationComputation() {
        setPhase(VirtualBackupPhases.END_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveProfile() {
        setPhase(VirtualBackupPhases.END_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    @Override
    public void finish() {
        task.done(raFcoVirtualBackup);
        setPhase(VirtualBackupPhases.END);
    }

    public List<AbstractVirtualBackupDiskInteractive> getDisksInteractive() {
        return this.disksInteractive;
    }

    public AbstractVirtualBackupVappInteractive getParent() {
        return this.parent;
    }

    /**
     * @return the rabVm
     */
    public AbstractCoreResultActionVirtualBackupForEntityWithDisks getRaFcoVirtualBackup() {
        return this.raFcoVirtualBackup;
    }

    protected List<TotalBlocksInfo> getTotalDumpsInfo() {
        final List<TotalBlocksInfo> result = new ArrayList<>();
        for (final AbstractVirtualBackupDiskInteractive d : this.disksInteractive) {
            result.addAll(d.getTotalDumpsInfo());
        }
        return result;
    }

    float increment(final float value) {
        if (this.parent != null) {
            this.parent.childIncrement(value);
        }
        final float actualValue = this.raFcoVirtualBackup.progressIncrease(value);
        this.task.update(actualValue);
        return actualValue;

    }

    protected boolean isTotalDumpsInfoEmpty() {
        boolean result = true;
        for (final AbstractVirtualBackupDiskInteractive d : this.disksInteractive) {
            result &= d.getTotalDumpsInfo().isEmpty();
        }
        return result;
    }

    public abstract AbstractVirtualBackupDiskInteractive newDiskInteractiveInstance(
            final CoreResultActionDiskVirtualBackup resultAction);

    private void setPhase(final VirtualBackupPhases phase) {
        this.raFcoVirtualBackup.setPhase(phase);
    }

    @Override
    public void start() {
        this.task.start();
        setPhase(VirtualBackupPhases.START);
    }

    public void startDisksVirtualBackup() {
        final int numberOfDisks = this.raFcoVirtualBackup.getNumberOfDisk();
        this.progressPercentagePerDisk = (REMAINING_PERCENTAGE / numberOfDisks) / ONE_HUNDRED_PER_CENT;
        setPhase(VirtualBackupPhases.START_DISKS_CONSOLIDATE);
        increment(SINGLE_STEP_INCREASE);
    }

    @Override
    public void startFinalizeProfile() {
        setPhase(VirtualBackupPhases.START_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startGenerationComputation() {
        setPhase(VirtualBackupPhases.START_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveProfile() {
        setPhase(VirtualBackupPhases.START_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }
}
