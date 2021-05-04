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
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.soap.VimTask;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.enums.phase.RestoreVappPhases;

public abstract class AbstractRestoreVappInteractive implements IRestoreInteractive {
    /**
     * Total percentage progress increment to complete a full dump the remain part
     * is assigned to pre and post dump operations
     */
    private static final float REMAINING_PERCENTAGE = 84;
    private static final float SINGLE_STEP_INCREASE = 0.1F;

    private float progressPercentagePerChild;
    private final CoreResultActionVappRestore raFcoRestore;
    private AbstractBackupVappInteractive parent;
    private final List<AbstractRestoreInteractive> restoreChildrenInteractive;
    private final List<TotalBlocksInfo> vAppTotalDumpInfoList;
    private final VimTask task;

    /**
     * Constructor
     *
     * @param rar
     */
    protected AbstractRestoreVappInteractive(final CoreResultActionVappRestore rar) {
        this.raFcoRestore = rar;
        this.restoreChildrenInteractive = new ArrayList<>();
        this.vAppTotalDumpInfoList = new ArrayList<>();
        this.task = new VimTask(rar.getFirstClassObject(), TaskOperationType.RESTORE);

    }

    @Override
    public float childIncrement(final float value) {
        return increment(value * this.progressPercentagePerChild);
    }

    public void endChildrenRestore() {

        setPhase(RestoreVappPhases.END_CHILDREN_RESTORE);

    }

    public void endDestroyVApp() {
        setPhase(RestoreVappPhases.END_VAPP_DESTROY);
    }

    public void endRelocation() {
        setPhase(RestoreVappPhases.END_VMS_RELOCATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endReplaceVMsIdentities() {
        setPhase(RestoreVappPhases.END_REPLACE_VMS_IDENTITIES);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRestoreManagedInfo() {
        setPhase(RestoreVappPhases.END_RESTORE_MANAGED_INFO);
        increment(SINGLE_STEP_INCREASE);
    }

    public void endRestoreVappMetadata() {

        setPhase(RestoreVappPhases.END_RESTORE_METADATA);
        increment(SINGLE_STEP_INCREASE);

    }

    public void endRetreiveVappChildCollection() {

        setPhase(RestoreVappPhases.END_RETRIEVE_CHILD_COLLECTION);

        this.progressPercentagePerChild = (REMAINING_PERCENTAGE / this.raFcoRestore.getNumberOfChildVm())
                / Utility.ONE_HUNDRED_PER_CENT;

        increment(SINGLE_STEP_INCREASE);

    }

    public void endUpdateVappConfig() {

        setPhase(RestoreVappPhases.END_UPDATE_CONFIG);
        increment(SINGLE_STEP_INCREASE);

    }

    public void endVappPowerOn() {

        setPhase(RestoreVappPhases.END_POWERON);
        increment(SINGLE_STEP_INCREASE);

    }

    public void endVappRetrieveProfile() {

        setPhase(RestoreVappPhases.END_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);

    }

    /**
     *
     */
    @Override
    public void finish() {

        task.done(raFcoRestore);
        setPhase(RestoreVappPhases.END);

    }

    /**
     * @return the numberOfChildVm
     */
    public int getNumberOfChildVm() {
        return this.raFcoRestore.getNumberOfChildVm();
    }

    /**
     * @return the rarVapp
     */
    public CoreResultActionVappRestore getRaFcoRestore() {
        return this.raFcoRestore;
    }

    public List<AbstractRestoreInteractive> getRestoreChildrenInteractive() {
        return this.restoreChildrenInteractive;
    }

    public List<TotalBlocksInfo> getvAppTotalDumpInfoList() {
        return this.vAppTotalDumpInfoList;
    }

    float increment(final float value) {
        if (this.parent != null) {
            this.parent.childIncrement(value);
        }
        final float actualValue = this.raFcoRestore.progressIncrease(value);
        this.task.update(actualValue);
        return actualValue;
    }

    public abstract AbstractRestoreInteractive newRestoreVmInteractiveInstance(final CoreResultActionVmRestore rab);

    private void setPhase(final RestoreVappPhases phase) {
        this.raFcoRestore.setPhase(phase);
    }

    @Override
    public void start() {
        this.task.start();
        setPhase(RestoreVappPhases.START);
    }

    /**
     *
     */
    public void startChildrenRestore() {
        setPhase(RestoreVappPhases.START_CHILDREN_RESTORE);
    }

    public void startDestroyVApp() {
        setPhase(RestoreVappPhases.START_VAPP_DESTROY);
    }

    public void startRelocation() {
        setPhase(RestoreVappPhases.START_VMS_RELOCATION);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startReplaceVMsIdentities() {
        setPhase(RestoreVappPhases.START_REPLACE_VMS_IDENTITIES);
        increment(SINGLE_STEP_INCREASE);
    }

    public void startRestoreManagedInfo() {
        setPhase(RestoreVappPhases.START_RESTORE_MANAGED_INFO);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void startRestoreVappMetadata() {

        setPhase(RestoreVappPhases.START_RESTORE_METADATA);
        increment(SINGLE_STEP_INCREASE);

    }

    public void startRetreiveVappChildCollection() {
        setPhase(RestoreVappPhases.START_RETRIEVE_CHILD_COLLECTION);
        increment(SINGLE_STEP_INCREASE);
    }

    /**
    *
    */
    public void startRetrieveProfile() {
        setPhase(RestoreVappPhases.START_RETRIEVE_PROFILE);
        increment(SINGLE_STEP_INCREASE);

    }

    public void startUpdateVappConfig() {
        setPhase(RestoreVappPhases.START_UPDATE_CONFIG);
        increment(SINGLE_STEP_INCREASE);

    }

    public void startVappPowerOn() {
        setPhase(RestoreVappPhases.START_POWERON);
        increment(SINGLE_STEP_INCREASE);
    }

}
