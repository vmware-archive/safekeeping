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

import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.enums.phase.VirtualBackupDiskPhases;

public class CoreResultActionDiskVirtualBackup extends AbstractCoreResultActionDiskVirtualBackupAndRestore {
    /**
     * 
     */
    private static final long serialVersionUID = -226609073520528047L;
    private GenerationProfile sourceProfile;
    private VirtualBackupDiskPhases phase;

    public CoreResultActionDiskVirtualBackup(final AbstractCoreResultActionVirtualBackupForEntityWithDisks parent) {
        super(parent.getProfile(), parent);
        this.phase = VirtualBackupDiskPhases.NONE;
        this.sourceProfile = parent.getSourceProfile();
        setUsedTransportModes("none");
    }

    public CoreResultActionDiskVirtualBackup(final int diskId,
            final AbstractCoreResultActionVirtualBackupForEntityWithDisks parent) {
        super(diskId, parent.getProfile(), parent);
        this.phase = VirtualBackupDiskPhases.NONE;
        this.sourceProfile = parent.getSourceProfile();
        setUsedTransportModes("none");
    }

    public VirtualBackupDiskPhases getPhase() {
        return this.phase;
    }

    public GenerationProfile getSourceProfile() {
        return this.sourceProfile;
    }

    public void setPhase(final VirtualBackupDiskPhases phase) {
        this.phase = phase;
    }

    public void setSourceProfile(final GenerationProfile sourceProfile) {
        this.sourceProfile = sourceProfile;
    }

}
