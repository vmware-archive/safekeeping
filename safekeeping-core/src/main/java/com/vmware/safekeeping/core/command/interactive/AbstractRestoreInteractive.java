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

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestoreForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.soap.VimTask;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.enums.phase.RestorePhases;

public abstract class AbstractRestoreInteractive implements IRestoreInteractive {
    /**
     * Total percentage progress increment to complete a full dump the remain part
     * is assigned to pre and post dump operations
     */
    private static final float ONE_HUNDRED_PER_CENT = 100F;
    private static final float REMAINING_PERCENTAGE = 84;
    private static final float NUMBER_OF_TRANSACTIONS_WITH_PERCENTAGE_INCREMENT = 20F;

    private static final float SINGLE_STEP_INCREASE = (ONE_HUNDRED_PER_CENT - REMAINING_PERCENTAGE)
            / NUMBER_OF_TRANSACTIONS_WITH_PERCENTAGE_INCREMENT;

    private final AbstractCoreResultActionRestoreForEntityWithDisks raFcoRestore;

    private final List<AbstractRestoreDiskInteractive> disksInteractive;

    private final AbstractRestoreVappInteractive parent;

    private float progressPercentagePerDisk;
    private final VimTask task;

    /**
     * Constructor
     *
     * @param rar
     */
    protected AbstractRestoreInteractive(final AbstractCoreResultActionRestoreForEntityWithDisks rar,
            final AbstractRestoreVappInteractive parent) {

        this.raFcoRestore = rar;
        this.disksInteractive = new ArrayList<>();
        this.parent = parent;
        this.task = new VimTask(rar.getFirstClassObject(), TaskOperationType.RESTORE);

        if (parent != null) {
            parent.getRestoreChildrenInteractive().add(this);
        }
    }

    @Override
    public float childIncrement(final float value) {
        return increment(value * this.progressPercentagePerDisk);
    }

    public void endCreateSnapshot() {
        setPhase(RestorePhases.END_CREATE_SNAPSHOT);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endDestroy() {
        setPhase(RestorePhases.END_DESTROY);
    }

    public void endDisksRestore() {
        setPhase(RestorePhases.END_DISKS_RESTORE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endReconfiguration() {
        setPhase(RestorePhases.END_RECONFIGURATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRemoveSnapshot() {
        setPhase(RestorePhases.END_REMOVE_SNAPSHOT);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRestoreManagedInfo() {
        setPhase(RestorePhases.END_RESTORE_MANAGED_INFO);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRestoreMetadata() {
        setPhase(RestorePhases.END_RESTORE_METADATA);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveProfile() {
        setPhase(RestorePhases.END_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endVddkAccess() {
        setPhase(RestorePhases.END_VDDK_ACCESS);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endVmPowerOn() {
        setPhase(RestorePhases.END_VM_POWERON);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    @Override
    public void finish() {
        if (getParent() != null) {
            getParent().getvAppTotalDumpInfoList().addAll(getTotalDumpsInfo());
        }
        task.done(raFcoRestore);
        setPhase(RestorePhases.END);
    }

    public List<AbstractRestoreDiskInteractive> getDisksInteractive() {
        return this.disksInteractive;
    }

    public AbstractRestoreVappInteractive getParent() {
        return this.parent;
    }

    /**
     * @return the rarVm
     */
    public AbstractCoreResultActionRestoreForEntityWithDisks getRaFcoRestore() {
        return this.raFcoRestore;
    }

    protected List<TotalBlocksInfo> getTotalDumpsInfo() {
        final List<TotalBlocksInfo> result = new ArrayList<>();
        for (final AbstractRestoreDiskInteractive d : this.disksInteractive) {
            result.addAll(d.getTotalDumpsInfo());
        }
        return result;
    }

    private float increment(final float value) {
        if (this.parent != null) {
            this.parent.childIncrement(value);
        }
        final float actualValue = this.raFcoRestore.progressIncrease(value);
        this.task.update(actualValue);
        return actualValue;
    }

    protected boolean isTotalDumpsInfoEmpty() {
        boolean result = true;
        for (final AbstractRestoreDiskInteractive d : this.disksInteractive) {
            result &= d.getTotalDumpsInfo().isEmpty();
        }
        return result;
    }

    public abstract AbstractRestoreDiskInteractive newDiskInteractiveInstance(
            final CoreResultActionDiskRestore resultAction);

    /**
     * Set the backup phase
     *
     * @param phase
     */
    private void setPhase(final RestorePhases phase) {
        this.raFcoRestore.setPhase(phase);
    }

    /**
     *
     */
    @Override
    public void start() {
        this.task.start();
        setPhase(RestorePhases.START);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void startCreateSnapshot() {
        setPhase(RestorePhases.START_CREATE_SNAPSHOT);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startDestroy() {
        setPhase(RestorePhases.START_DESTROY);
    }

    /**
    *
    */
    public void startDisksRestore() {
        final int numberOfDisks = this.raFcoRestore.getNumberOfDisk();
        this.progressPercentagePerDisk = (REMAINING_PERCENTAGE / numberOfDisks) / ONE_HUNDRED_PER_CENT;
        setPhase(RestorePhases.START_DISKS_RESTORE);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void startReconfiguration() {
        setPhase(RestorePhases.START_RECONFIGURATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startRemoveSnapshot() {
        setPhase(RestorePhases.START_REMOVE_SNAPSHOT);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
    *
    */
    public void startRestoreManagedInfo() {
        setPhase(RestorePhases.START_RESTORE_MANAGED_INFO);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void startRestoreMetadata() {
        setPhase(RestorePhases.START_RESTORE_METADATA);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveProfile() {
        setPhase(RestorePhases.START_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startVddkAccess() {
        setPhase(RestorePhases.START_VDDK_ACCESS);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void startVmPowerOn() {
        setPhase(RestorePhases.START_VM_POWERON);
        increment(SINGLE_STEP_INCREASE);
    }

}
