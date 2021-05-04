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

import com.vmware.safekeeping.core.command.results.CoreResultActionDiskVirtualBackup;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.type.enums.phase.VirtualBackupDiskPhases;

public abstract class AbstractVirtualBackupDiskInteractive implements InteractiveDisk {
    private static final float TOTAL_DUMP_PERCENTAGE = 100;
    private final List<TotalBlocksInfo> totalDumpsInfo;

    private final CoreResultActionDiskVirtualBackup raVirtualBackupDisk;

    private final AbstractVirtualBackupInteractive parent;
    private float progressPercentagePerBlock;

    /**
     * Constructor
     *
     * @param rar
     */
    protected AbstractVirtualBackupDiskInteractive(final CoreResultActionDiskVirtualBackup radc,
            final AbstractVirtualBackupInteractive parent) {
        this.raVirtualBackupDisk = radc;
        this.totalDumpsInfo = new ArrayList<>();
        this.parent = parent;
        parent.getDisksInteractive().add(this);
    }

    @Override
    public void dumpFailure(final ExBlockInfo blockInfo) {
        increment(1);
    }

    /**
     * @param dumpFileInfo
     */
    @Override
    public void dumpSuccess(final ExBlockInfo dumpFileInfo) {
        increment(1);
    }

    public void endCalculateNumberOfGenerationDiskRestore() {
        setPhase(VirtualBackupDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
    }

    public void endVirtualBackupDisk() {
        setPhase(VirtualBackupDiskPhases.END);
    }

    @Override
    public void endDumpsTotalCalculation(final TotalBlocksInfo totalDumpInfo) {
        setPhase(VirtualBackupDiskPhases.END_DUMPS_TOTAL_CALCULATION);
        getTotalDumpsInfo().add(totalDumpInfo);
    }

    @Override
    public void endDumpThreads(final OperationState state) {
        setPhase(VirtualBackupDiskPhases.END_DUMP_THREDS);
    }

    /**
     * @return the activeRestoreOnDisk
     */
    public CoreResultActionDiskVirtualBackup getRaDiskVirtualBackup() {
        return this.raVirtualBackupDisk;
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
        return this.raVirtualBackupDisk.progressIncrease(increment);
    }

    private void setPhase(final VirtualBackupDiskPhases phase) {
        if (this.raVirtualBackupDisk != null) {
            this.raVirtualBackupDisk.setPhase(phase);
        }
    }

    /**
     *
     */
    public void startCalculateNumberOfGenerationDiskRestore() {
        setPhase(VirtualBackupDiskPhases.START_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
    }

    public void startVirtualBackupDisk() {
        setPhase(VirtualBackupDiskPhases.START);
    }

    @Override
    public void startDumpsTotalCalculation() {
        setPhase(VirtualBackupDiskPhases.START_DUMPS_TOTAL_CALCULATION);
    }

//
    @Override
    public void startDumpThreads() {
        setPhase(VirtualBackupDiskPhases.START_DUMP_THREDS);
        final int numberOfBlocks = this.raVirtualBackupDisk.getNumberOfBlocks();
        this.progressPercentagePerBlock = TOTAL_DUMP_PERCENTAGE / numberOfBlocks;
    }

}
