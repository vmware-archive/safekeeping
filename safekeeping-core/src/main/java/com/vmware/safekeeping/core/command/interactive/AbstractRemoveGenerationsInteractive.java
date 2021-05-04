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

import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.safekeeping.core.type.enums.phase.RemoveGenerationsPhases;

public abstract class AbstractRemoveGenerationsInteractive {

    private static final float totalDumpPercentage = 88;

    private static final float SINGLE_STEP_INCREASE = 1F;

    private float percIncremenPerGeneration;
    private final CoreResultActionArchiveRemoveGenerationWithDependencies generationWithDependencies;

    private CoreResultActionArchiveRemoveGeneration generation2Remove;

    protected AbstractRemoveGenerationsInteractive(
            final CoreResultActionArchiveRemoveGenerationWithDependencies generationWithDependencies) {
        this.generationWithDependencies = generationWithDependencies;
        this.percIncremenPerGeneration = 0;
        this.generation2Remove = null;
    }

    public void endAccessArchive() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_ACCESS_ARCHIVE);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endDeleteData() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_DELETE_DATA);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endDeleteGeneration() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_DELETE_GENERATION);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
        this.generation2Remove = null;
    }

    public void endDeleteGenerations() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_DELETE_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRemoveProfileGeneration() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_REMOVE_PROFILE_GENERATION);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveDependingGenerations() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_RETRIEVE_DEPENDING_GENERATIONS);
        this.percIncremenPerGeneration = totalDumpPercentage
                / this.generationWithDependencies.getSubOperations().size();
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveRequestedGenerations() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_RETRIEVE_REQUESTED_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endUpdateFcoProfilesCatalog() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END_UPDATE_FCO_PROFILES_CATALOG);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void finish() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.END);
    }

    /**
     * @return the craag
     */
    public CoreResultActionArchiveRemoveGeneration getGeneration2Remove() {
        return this.generation2Remove;
    }

    /**
     * @return the raargwd
     */
    public CoreResultActionArchiveRemoveGenerationWithDependencies getGenerationWithDependencies() {
        return this.generationWithDependencies;
    }

    public void start() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START);
    }

    public void startAccessArchive() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_ACCESS_ARCHIVE);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startDeleteData() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_DELETE_DATA);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startDeleteGeneration(final CoreResultActionArchiveRemoveGeneration craarg) {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_DELETE_GENERATION);
        this.generationWithDependencies.progressIncrease(this.percIncremenPerGeneration);
        this.generation2Remove = craarg;
    }

    public void startDeleteGenerations() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_DELETE_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRemoveProfileGeneration() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_REMOVE_PROFILE_GENERATION);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveDependingGenerations() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_RETRIEVE_DEPENDING_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveRequestedGenerations() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_RETRIEVE_REQUESTED_GENERATIONS);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }

    /**
    *
    */
    public void startUpdateFcoProfilesCatalog() {
        this.generationWithDependencies.setPhase(RemoveGenerationsPhases.START_UPDATE_FCO_PROFILES_CATALOG);
        this.generationWithDependencies.progressIncrease(SINGLE_STEP_INCREASE);
    }
}
