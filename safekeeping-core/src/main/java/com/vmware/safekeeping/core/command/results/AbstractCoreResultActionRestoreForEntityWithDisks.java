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

import com.vmware.safekeeping.common.AtomicEnum;
import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreInteractive;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.type.enums.phase.RestorePhases;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;

public abstract class AbstractCoreResultActionRestoreForEntityWithDisks extends AbstractCoreResultActionRestore
        implements IOperationOnEntityWithDisks {

    /**
     * 
     */
    private static final long serialVersionUID = -7240464383910289051L;
    private final AtomicEnum<RestorePhases> phase;
    private volatile int numberOfDisk;
    private final ConcurrentDoublyLinkedList<TotalBlocksInfo> totalDumpsInfo;

    /**
     * @param fco
     * @param options
     */
    protected AbstractCoreResultActionRestoreForEntityWithDisks(final IFirstClassObject fco,
            final CoreRestoreOptions options) {
        super(fco, options);
        this.totalDumpsInfo = new ConcurrentDoublyLinkedList<>();
        this.phase = new AtomicEnum<>(RestorePhases.NONE);
        this.numberOfDisk = 0;
    }

    public void addDumpInfo(final TotalBlocksInfo e) {
        this.totalDumpsInfo.add(e);
    }

    @Override
    public AbstractRestoreInteractive getInteractive() {
        return (AbstractRestoreInteractive) this.interactive;
    }

    /**
     * @return the numberOfDisk
     */
    @Override
    public int getNumberOfDisk() {
        return this.numberOfDisk;
    }

    public RestorePhases getPhase() {
        return this.phase.get();
    }

    public ConcurrentDoublyLinkedList<TotalBlocksInfo> getTotalDumpsInfo() {
        return this.totalDumpsInfo;
    }

    /**
     * @param numberOfDisk the numberOfDisk to set
     */
    @Override
    public void setNumberOfDisk(final int numberOfDisk) {
        this.numberOfDisk = numberOfDisk;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(final RestorePhases phase) {
        this.phase.set(phase);
    }

    @Override
    public String toString() {
        return getState().toString() + " " + getFcoToString();
    }
}
