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

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.soap.VimTask;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.enums.phase.BackupPhases;

public abstract class AbstractBackupInteractive implements IBackupInteractive {

    /**
     * Total percentage progress increment to complete a full dump the remain part
     * is assigned to pre and post dump operations
     */
    private static final float ONE_HUNDRED_PER_CENT = 100F;
    private static final float REMAINING_PERCENTAGE = 84F;
    private static final float NUMBER_OF_TRANSACTIONS_WITH_PERCENTAGE_INCREMENT = 25F;
    private static final float SINGLE_STEP_INCREASE = (ONE_HUNDRED_PER_CENT - REMAINING_PERCENTAGE)
            / NUMBER_OF_TRANSACTIONS_WITH_PERCENTAGE_INCREMENT;
    private static final float LONG_OPERATION_STEPS_INCREASE = 4 * SINGLE_STEP_INCREASE;

    private final AbstractCoreResultActionBackupForEntityWithDisks raFcoBackup;
    private final AbstractBackupVappInteractive parent;

    private final List<AbstractBackupDiskInteractive> disksInteractive;

    private float progressPercentagePerDisk;
    private final VimTask task;

    protected AbstractBackupInteractive(final AbstractCoreResultActionBackupForEntityWithDisks rab) {
        this(rab, null);
    }

    /**
     * @param rab
     */
    protected AbstractBackupInteractive(final AbstractCoreResultActionBackupForEntityWithDisks rab,
            final AbstractBackupVappInteractive parent) {

        this.raFcoBackup = rab;
        this.disksInteractive = new ArrayList<>();
        this.parent = parent;
        this.task = new VimTask(rab.getFirstClassObject(), TaskOperationType.BACKUP);
        if (parent != null) {
            parent.getChildrenInteractive().add(this);
        }
    }

    @Override
    public float childIncrement(final float value) {
        return increment(value * this.progressPercentagePerDisk);
    }

    public void endCreateSnapshot() {
        setPhase(BackupPhases.END_CREATE_SNAPSHOT);
        increment(LONG_OPERATION_STEPS_INCREASE);
    }

    public void endDiscoverBackupMode() {
        setPhase(BackupPhases.END_DISCOVER_BACKUP_MODE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endDisksBackup() {
        setPhase(BackupPhases.END_DISKS_BACKUP);
        increment(SINGLE_STEP_INCREASE);
        task.updateDescription("Disks backup done");
    }

    @Override
    public void endFinalizeProfile() {
        setPhase(BackupPhases.END_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endGenerationComputation() {
        setPhase(BackupPhases.END_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endInfoCollection() {
        setPhase(BackupPhases.END_INFO_COLLECTION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRemoveSnapshot() {
        setPhase(BackupPhases.END_REMOVE_SNAPSHOT);
        increment(LONG_OPERATION_STEPS_INCREASE);
    }

    public void endRevertToTemplate() {
        setPhase(BackupPhases.END_REVERT_TO_TEMPLATE);
    }

    public void endVddkAccess() {
        setPhase(BackupPhases.END_VDDK_ACCESS);
        increment(SINGLE_STEP_INCREASE);
    }

    @Override
    public void finish() {
        task.done(raFcoBackup);
        setPhase(BackupPhases.END);
    }

    public List<AbstractBackupDiskInteractive> getDisksInteractive() {
        return this.disksInteractive;
    }

    public AbstractBackupVappInteractive getParent() {
        return this.parent;
    }

    /**
     * @return the rabVm
     */
    public AbstractCoreResultActionBackupForEntityWithDisks getRaFcoBackup() {
        return this.raFcoBackup;
    }

    protected List<TotalBlocksInfo> getTotalDumpsInfo() {
        final List<TotalBlocksInfo> result = new ArrayList<>();
        for (final AbstractBackupDiskInteractive d : this.disksInteractive) {
            result.addAll(d.getTotalDumpsInfo());
        }
        return result;
    }

    float increment(final float value) {
        if (this.parent != null) {
            this.parent.childIncrement(value);
        }

        final float actualValue = this.raFcoBackup.progressIncrease(value);
        this.task.update(actualValue);
        return actualValue;
    }

    protected boolean isTotalDumpsInfoEmpty() {
        boolean result = true;
        for (final AbstractBackupDiskInteractive d : this.disksInteractive) {
            result &= d.getTotalDumpsInfo().isEmpty();
        }
        return result;
    }

    public abstract AbstractBackupDiskInteractive newDiskInteractiveInstance(
            final CoreResultActionDiskBackup resultAction);

    private void setPhase(final BackupPhases phase) {

        this.raFcoBackup.setPhase(phase);
    }

    @Override
    public void start() {
        this.task.start();
        setPhase(BackupPhases.START);
        increment(SINGLE_STEP_INCREASE);

    }

    public void startCreateSnapshot() {
        setPhase(BackupPhases.START_CREATE_SNAPSHOT);
        increment(SINGLE_STEP_INCREASE);
        task.updateDescription("Creating snapshot");
    }

    public void startDiscoverBackupMode() {
        setPhase(BackupPhases.START_DISCOVER_BACKUP_MODE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startDisksBackup() {
        final int numberOfDisks = this.raFcoBackup.getNumberOfDisk();
        this.progressPercentagePerDisk = (REMAINING_PERCENTAGE / numberOfDisks) / ONE_HUNDRED_PER_CENT;
        setPhase(BackupPhases.START_DISKS_BACKUP);
        increment(SINGLE_STEP_INCREASE);
        task.updateDescription("Starting disks backup");
    }

    @Override
    public void startFinalizeProfile() {
        setPhase(BackupPhases.START_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startGenerationComputation() {
        setPhase(BackupPhases.START_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startInfoCollection() {
        setPhase(BackupPhases.START_INFO_COLLECTION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startRemoveSnapshot() {
        setPhase(BackupPhases.START_REMOVE_SNAPSHOT);
        increment(SINGLE_STEP_INCREASE);
        task.updateDescription("Removing snapshot");
    }

    public void startRevertToTemplate() {
        setPhase(BackupPhases.START_REVERT_TO_TEMPLATE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startVddkAccess() {
        setPhase(BackupPhases.START_VDDK_ACCESS);
        increment(SINGLE_STEP_INCREASE);
    }
}
