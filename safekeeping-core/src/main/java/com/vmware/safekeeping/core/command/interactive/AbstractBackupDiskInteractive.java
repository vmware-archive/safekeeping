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

import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.phase.BackupDiskPhases;

public abstract class AbstractBackupDiskInteractive implements InteractiveDisk {

    /**
     *
     */
    private static final float TOTAL_DUMP_PERCENTAGE = 100;
    private final List<TotalBlocksInfo> totalDumpsInfo;

    private CoreResultActionDiskBackup raDiskBackup;
    private final AbstractBackupInteractive parent;

    private float progressPercentagePerBlock;

    @Override
    public ManagedEntityInfo getEntity() {
        return raDiskBackup.getFcoEntityInfo();
    }

    /**
     * @param rab
     */
    protected AbstractBackupDiskInteractive(final CoreResultActionDiskBackup radb,
            final AbstractBackupInteractive parent) {
        this.raDiskBackup = radb;
        this.totalDumpsInfo = new ArrayList<>();
        this.parent = parent;
        parent.getDisksInteractive().add(this);
    }

    @Override
    public void dumpFailure(final ExBlockInfo blockInfo) {
        increment(1);
    }

    @Override
    public void dumpSuccess(final ExBlockInfo block) {
        increment(1);

    }

    public void endCloseVmdk() {
        setPhase(BackupDiskPhases.END_CLOSE_VMDK);
    }

    public void endDiskBackup() {
        setPhase(BackupDiskPhases.END);
        this.raDiskBackup = null;
    }

    /**
     * @param totalDumpInfo
     */

    @Override
    public void endDumpsTotalCalculation(final TotalBlocksInfo totalDumpInfo) {
        setPhase(BackupDiskPhases.END_DUMPS_TOTAL_CALCULATION);
        getTotalDumpsInfo().add(totalDumpInfo);
    }

    /**
     *
     */

    @Override
    public void endDumpThreads(final OperationState state) {
        setPhase(BackupDiskPhases.END_DUMP_THREADS);
    }

    public void endNormalizeVmdkBlocks() {
        setPhase(BackupDiskPhases.END_NORMALIZE_VMDK_BLOCKS);
    }

    /**
     *
     */
    public void endOpenVmdk() {
        setPhase(BackupDiskPhases.END_OPEN_VMDK);

    }

    /**
     *
     */
    public void endQueryBlocks() {
        setPhase(BackupDiskPhases.END_QUERY_BLOCKS);

    }

    public AbstractBackupInteractive getParent() {
        return this.parent;
    }

    /**
     * @return the activeDisk
     */
    public CoreResultActionDiskBackup getRaDiskBackup() {
        return this.raDiskBackup;
    }

    /**
     * @return the totalDumpsInfo
     */

    @Override
    public List<TotalBlocksInfo> getTotalDumpsInfo() {
        return this.totalDumpsInfo;
    }

    private float increment(final float value) {
        final float increment = value * this.progressPercentagePerBlock;
        if (this.parent != null) {
            this.parent.childIncrement(increment);
        }
        return this.raDiskBackup.progressIncrease(increment);

    }

    private void setPhase(final BackupDiskPhases phase) {
        this.raDiskBackup.setPhase(phase);
    }

    /**
     *
     */
    public void startCloseVmdk() {
        setPhase(BackupDiskPhases.START_CLOSE_VMDK);
    }

    public void startDiskBackup() {

        setPhase(BackupDiskPhases.START);
    }

    /**
     *
     */

    @Override
    public void startDumpsTotalCalculation() {
        setPhase(BackupDiskPhases.START_DUMPS_TOTAL_CALCULATION);
    }

    @Override
    public void startDumpThreads() {
        setPhase(BackupDiskPhases.START_DUMP_THREADS);
        final float numberOfBlocks = this.raDiskBackup.getNumberOfBlocks();
        this.progressPercentagePerBlock = TOTAL_DUMP_PERCENTAGE / numberOfBlocks;
    }

    /**
    *
    */
    public void startNormalizeVmdkBlocks() {
        setPhase(BackupDiskPhases.START_NORMALIZE_VMDK_BLOCKS);
    }

    /**
     *
     */
    public void startOpenVmdk() {
        setPhase(BackupDiskPhases.START_OPEN_VMDK);
    }

    /**
     *
     */
    public void startQueryBlocks() {
        setPhase(BackupDiskPhases.START_QUERY_BLOCKS);
    }

}
