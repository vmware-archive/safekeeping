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

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.soap.VimTask;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.enums.phase.BackupVappPhases;

public abstract class AbstractBackupVappInteractive implements IBackupInteractive {
    /**
     * Total percentage progress increment to complete a full dump the remain part
     * is assigned to pre and post dump operations
     */

    private static final float REMAINING_PERCENTAGE = 88;

    private static final float SINGLE_STEP_INCREASE = 0.1F;
    private final CoreResultActionVappBackup raFcoBackup;

    private final List<IBackupInteractive> childrenInteractive;

    private float progressPercentagePerChild;
    private final AbstractBackupVappInteractive parent;
    private final VimTask task;

    protected AbstractBackupVappInteractive(final CoreResultActionVappBackup rab) {
        this(rab, null);
    }

    /**
     * @param rab
     */
    protected AbstractBackupVappInteractive(final CoreResultActionVappBackup rab,
            final AbstractBackupVappInteractive parent) {

        this.raFcoBackup = rab;
        this.childrenInteractive = new ArrayList<>();
        this.parent = parent;
        this.task = new VimTask(rab.getFirstClassObject(), TaskOperationType.BACKUP);

        if (parent != null) {
            parent.getChildrenInteractive().add(this);
        }

    }

    @Override
    public float childIncrement(final float value) {
        return increment(value * this.progressPercentagePerChild);
    }

    public void endChildrenBackup() {
        setPhase(BackupVappPhases.END_CHILDREN_BACKUP);
    }

    @Override
    public void endFinalizeProfile() {
        setPhase(BackupVappPhases.END_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endGenerationComputation() {
        setPhase(BackupVappPhases.END_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endPostOvfMetadata() {
        setPhase(BackupVappPhases.END_POST_OVF_METADATA);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endVappInfoCollection() {
        setPhase(BackupVappPhases.END_INFO_COLLECTION);
        this.progressPercentagePerChild = (REMAINING_PERCENTAGE / this.raFcoBackup.getNumberOfChildVm())
                / Utility.ONE_HUNDRED_PER_CENT;
        increment(SINGLE_STEP_INCREASE);
    }

    @Override
    public void finish() {
        task.done(raFcoBackup);
        setPhase(BackupVappPhases.END);

    }

    public List<IBackupInteractive> getChildrenInteractive() {
        return this.childrenInteractive;
    }

    /**
     * @return the numberOfChildVm
     */
    public int getNumberOfChildVm() {
        return this.raFcoBackup.getNumberOfChildVm();
    }

    public AbstractBackupVappInteractive getParent() {
        return this.parent;
    }

    /**
     * @return the rabVapp
     */
    public CoreResultActionVappBackup getRaFcoBackup() {
        return this.raFcoBackup;
    }

    float increment(final float value) {
        if (this.parent != null) {
            this.parent.childIncrement(value);
        }
        final float actualValue = this.raFcoBackup.progressIncrease(value);
        this.task.update(actualValue);
        return actualValue;
    }

    public abstract AbstractBackupInteractive newBackupVmInteractiveInstance(final CoreResultActionVmBackup rab);

    private void setPhase(final BackupVappPhases phase) {
        this.raFcoBackup.setPhase(phase);
    }

    @Override
    public void start() {
        this.task.start();
        setPhase(BackupVappPhases.START);
    }

    public void startChildrenBackup() {
        setPhase(BackupVappPhases.START_CHILDREN_BACKUP);
    }

    @Override
    public void startFinalizeProfile() {
        setPhase(BackupVappPhases.START_FINALIZE_PROFILE);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startGenerationComputation() {
        setPhase(BackupVappPhases.START_GENERATION_COMPUTATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startPostOvfMetadata() {

        setPhase(BackupVappPhases.START_POST_OVF_METADATA);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startVappInfoCollection() {
        setPhase(BackupVappPhases.START_INFO_COLLECTION);
        increment(SINGLE_STEP_INCREASE);
    }

}
