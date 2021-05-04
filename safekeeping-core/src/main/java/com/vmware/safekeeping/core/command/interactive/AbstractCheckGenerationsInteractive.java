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

import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.core.type.enums.phase.CheckGenerationsPhases;

public abstract class AbstractCheckGenerationsInteractive {

    private static final float totalDumpPercentage = 88;

    private static final float SINGLE_STEP_INCREASE = 0.5F;
    private float percIncremenPerGeneration;
    private final CoreResultActionArchiveCheckGenerationWithDependencies generationWithDependencies;

    private CoreResultActionArchiveCheckGeneration generation2Check;

    protected AbstractCheckGenerationsInteractive(
            final CoreResultActionArchiveCheckGenerationWithDependencies generationWithDependencies) {
        this.generationWithDependencies = generationWithDependencies;
        this.percIncremenPerGeneration = 0;
        this.generation2Check = null;
    }

    public void endAccessArchive() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_ACCESS_ARCHIVE);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endCheckFile(final int index) {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_CHECK_FILE);
        this.generation2Check.progressIncrease(this.percIncremenPerGeneration);
    }

    /**
     *
     */
    public void endCheckFiles() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_CHECK_FILES);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endCheckGeneration() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_CHECK_GENERATION);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
        this.generation2Check = null;
    }

    public void endCheckGenerations() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_CHECK_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endLoadProfileGeneration() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_LOAD_GENERATION_PROFILE);
        this.generation2Check.progressIncrease(SINGLE_STEP_INCREASE + SINGLE_STEP_INCREASE);
        this.percIncremenPerGeneration = totalDumpPercentage / this.generation2Check.getNumOfFiles();
    }

    public void endRetrieveDependingGenerations() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_RETRIEVE_DEPENDING_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveRequestedGenerations() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_RETRIEVE_REQUESTED_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endUpdateFcoProfilesCatalog() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END_UPDATE_FCO_PROFILES_CATALOG);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void finish() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.END);
    }

    /**
     * @return the craag
     */
    public CoreResultActionArchiveCheckGeneration getGeneration2Check() {
        return this.generation2Check;
    }

    /**
     * @return the raargwd
     */
    public CoreResultActionArchiveCheckGenerationWithDependencies getGenerationWithDependencies() {
        return this.generationWithDependencies;
    }

    public void start() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START);
    }

    public void startAccessArchive() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_ACCESS_ARCHIVE);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startCheckFile(final int index) {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_CHECK_FILE);

    }

    public void startCheckFiles() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_CHECK_FILES);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startCheckGeneration(final CoreResultActionArchiveCheckGeneration craarg) {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_CHECK_GENERATION);
        this.generationWithDependencies.progressIncrease(this.percIncremenPerGeneration);
        this.generation2Check = craarg;
    }

    public void startCheckGenerations() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_CHECK_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startLoadProfileGeneration() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_LOAD_GENERATION_PROFILE);
        this.generation2Check.progressIncrease(1);
    }

    public void startRetrieveDependingGenerations() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_RETRIEVE_DEPENDING_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveRequestedGenerations() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_RETRIEVE_REQUESTED_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    /**
    *
    */
    public void startUpdateFcoProfilesCatalog() {
        this.generationWithDependencies.setPhase(CheckGenerationsPhases.START_UPDATE_FCO_PROFILES_CATALOG);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }
}
