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

import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchiveStatus;
import com.vmware.safekeeping.core.type.enums.phase.StatusProfilePhases;

public abstract class AbstractStatusInteractive {

    private static final float SINGLE_STEP_INCREASE = 100F / 8F;
    private final AbstractCoreResultActionArchiveStatus archiveStatus;

    protected AbstractStatusInteractive(final AbstractCoreResultActionArchiveStatus archiveStatus) {
        this.archiveStatus = archiveStatus;
    }

    public void endAccessArchive() {
        this.archiveStatus.setPhase(StatusProfilePhases.END_ACCESS_ARCHIVE);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveGenerationInfo(final Integer genId) {
        this.archiveStatus.setPhase(StatusProfilePhases.END_RETRIEVE_GENERATION_INFO);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveGenerations() {
        this.archiveStatus.setPhase(StatusProfilePhases.END_RETRIEVE_GENERATIONS);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void endRetrieveGenerationsInfo() {
        this.archiveStatus.setPhase(StatusProfilePhases.END_RETRIEVE_GENERATIONS_INFO);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);

    }

    public void finish() {
        this.archiveStatus.setPhase(StatusProfilePhases.END);
    }

    /**
     * @return the archiveStatus
     */
    public AbstractCoreResultActionArchiveStatus getArchiveStatus() {
        return this.archiveStatus;
    }

    public void start() {
        this.archiveStatus.setPhase(StatusProfilePhases.START);
    }

    public void startAccessArchive() {
        this.archiveStatus.setPhase(StatusProfilePhases.START_ACCESS_ARCHIVE);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveGenerationInfo() {
        this.archiveStatus.setPhase(StatusProfilePhases.START_RETRIEVE_GENERATION_INFO);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveGenerations() {
        this.archiveStatus.setPhase(StatusProfilePhases.START_RETRIEVE_GENERATIONS);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);
    }

    /**
     *
     */
    public void startRetrieveGenerationsInfo() {
        this.archiveStatus.setPhase(StatusProfilePhases.START_RETRIEVE_GENERATIONS_INFO);
        this.archiveStatus.progressIncrease(SINGLE_STEP_INCREASE);

    }

}
