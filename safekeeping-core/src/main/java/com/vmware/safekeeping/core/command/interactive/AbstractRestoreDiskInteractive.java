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

import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.type.enums.phase.RestoreDiskPhases;

public abstract class AbstractRestoreDiskInteractive implements InteractiveDisk {
    private static final float TOTAL_DUMP_PERCENTAGE = 100;
    private final List<TotalBlocksInfo> totalDumpsInfo;

    private final CoreResultActionDiskRestore raDiskRestore;

    private final AbstractRestoreInteractive parent;
    private float progressPercentagePerBlock;

    /**
     * Constructor
     *
     * @param rar
     */
    protected AbstractRestoreDiskInteractive(final CoreResultActionDiskRestore radr,
            final AbstractRestoreInteractive parent) {

        this.raDiskRestore = radr;
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
        setPhase(RestoreDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
    }

    /**
    *
    */
    public void endCloseVmdk() {
        setPhase(RestoreDiskPhases.END);
    }

    public void endDiskRestore() {
        setPhase(RestoreDiskPhases.END);
    }

    @Override
    public void endDumpsTotalCalculation(final TotalBlocksInfo totalDumpInfo) {
        setPhase(RestoreDiskPhases.END_DUMPS_TOTAL_CALCULATION);
        getTotalDumpsInfo().add(totalDumpInfo);
    }

    @Override
    public void endDumpThreads(final OperationState state) {
        setPhase(RestoreDiskPhases.END_DUMP_THREDS);
    }

    /**
    *
    */
    public void endOpenVmdk() {
        setPhase(RestoreDiskPhases.END_OPEN_VMDK);
    }

    /**
     * @return the activeRestoreOnDisk
     */
    public CoreResultActionDiskRestore getRaDiskRestore() {
        return this.raDiskRestore;
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
        return this.raDiskRestore.progressIncrease(increment);

    }

    private void setPhase(final RestoreDiskPhases phase) {
        if (this.raDiskRestore != null) {
            this.raDiskRestore.setPhase(phase);
        }
    }

    /**
     *
     */
    public void startCalculateNumberOfGenerationDiskRestore() {
        setPhase(RestoreDiskPhases.START_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
    }

    /**
    *
    */
    public void startCloseVmdk() {
        setPhase(RestoreDiskPhases.START_CLOSE_VMDK);
    }

    public void startDiskRestore() {
        setPhase(RestoreDiskPhases.START);
    }

    /**
    *
    */
    @Override
    public void startDumpsTotalCalculation() {
        setPhase(RestoreDiskPhases.START_DUMPS_TOTAL_CALCULATION);
    }

    @Override
    public void startDumpThreads() {
        setPhase(RestoreDiskPhases.START_DUMP_THREDS);
        final int numberOfBlocks = this.raDiskRestore.getNumberOfBlocks();
        this.progressPercentagePerBlock = TOTAL_DUMP_PERCENTAGE / numberOfBlocks;
    }

    /**
    *
    */
    public void startOpenVmdk() {
        setPhase(RestoreDiskPhases.START_OPEN_VMDK);
    }

}
