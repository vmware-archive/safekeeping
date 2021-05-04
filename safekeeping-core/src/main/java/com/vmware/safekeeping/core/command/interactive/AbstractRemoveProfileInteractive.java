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

import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveProfile;
import com.vmware.safekeeping.core.type.enums.phase.RemoveProfilePhases;

public abstract class AbstractRemoveProfileInteractive {

    private static final float SINGLE_STEP_INCREASE = 100F / 2F;
    private final CoreResultActionArchiveRemoveProfile archiveRemoveProfile;

    protected AbstractRemoveProfileInteractive(final CoreResultActionArchiveRemoveProfile archiveRemoveProfile) {
        this.archiveRemoveProfile = archiveRemoveProfile;
    }

    public void endAccessArchive() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END_ACCESS_ARCHIVE);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);

    }

    public void endRemoveFcoProfileContent() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END_REMOVE_FCO_PROFILE_CONTENT);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);

    }

    public void endRemoveFcoProfileMetadata() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END_REMOVE_FCO_PROFILE_METADATA);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRemoveGeneration(final Integer genId) {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END_REMOVE_GENERATION);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endRetrieveGenerations() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END_RETRIEVE_GENERATIONS);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void endUpdateFcoProfilesCatalog() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END_UPDATE_FCO_PROFILES_CATALOG);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void finish() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.END);
    }

    /**
     * @return the raargwd
     */
    public CoreResultActionArchiveRemoveProfile getArchiveRemoveProfile() {
        return this.archiveRemoveProfile;
    }

    public void start() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START);
    }

    public void startAccessArchive() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START_ACCESS_ARCHIVE);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);

    }

    public void startRemoveFcoProfileContent() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START_REMOVE_FCO_PROFILE_CONTENT);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRemoveFcoProfileMetadata() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START_REMOVE_FCO_PROFILE_METADATA);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRemoveGeneration(final Integer genId) {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START_REMOVE_GENERATION);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startRetrieveGenerations() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START_RETRIEVE_GENERATIONS);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }

    public void startUpdateFcoProfilesCatalog() {
        this.archiveRemoveProfile.setPhase(RemoveProfilePhases.START_UPDATE_FCO_PROFILES_CATALOG);
        this.archiveRemoveProfile.progressIncrease(SINGLE_STEP_INCREASE);
    }
}
