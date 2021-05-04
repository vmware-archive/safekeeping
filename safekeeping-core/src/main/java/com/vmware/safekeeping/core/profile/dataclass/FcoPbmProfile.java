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
package com.vmware.safekeeping.core.profile.dataclass;

import com.vmware.pbm.PbmProfile;

public class FcoPbmProfile {
    private String pbmProfileId;

    private String pbmProfileName;

    private String pbmProfileDescription;

    public FcoPbmProfile() {
    }

    /**
     * Clone constructor
     *
     * @param src
     */
    public FcoPbmProfile(final FcoPbmProfile src) {
        this.pbmProfileId = src.pbmProfileId;
        this.pbmProfileName = src.pbmProfileName;
        this.pbmProfileDescription = src.pbmProfileDescription;
    }

    /**
     * @param pbmProfileId
     * @param pbmProfileName
     * @param pbmProfileDescription
     */
    public FcoPbmProfile(final String pbmProfileId, final String pbmProfileName, final String pbmProfileDescription) {
        this.pbmProfileId = pbmProfileId;
        this.pbmProfileName = pbmProfileName;
        this.pbmProfileDescription = pbmProfileDescription;
    }

    public FcoPbmProfile(PbmProfile pbmProfile) {
        this.pbmProfileId = pbmProfile.getProfileId().getUniqueId();
        this.pbmProfileName = pbmProfile.getName();
        this.pbmProfileDescription = pbmProfile.getDescription();
    }

    /**
     * @return the pbmProfileDescription
     */
    public String getPbmProfileDescription() {
        return this.pbmProfileDescription;
    }

    /**
     * @return the pbmProfileId
     */
    public String getPbmProfileId() {
        return this.pbmProfileId;
    }

    /**
     * @return the pbmProfileName
     */
    public String getPbmProfileName() {
        return this.pbmProfileName;
    }

    /**
     * @param pbmProfileDescription the pbmProfileDescription to set
     */
    public void setPbmProfileDescription(final String pbmProfileDescription) {
        this.pbmProfileDescription = pbmProfileDescription;
    }

    /**
     * @param pbmProfileId the pbmProfileId to set
     */
    public void setPbmProfileId(final String pbmProfileId) {
        this.pbmProfileId = pbmProfileId;
    }

    /**
     * @param pbmProfileName the pbmProfileName to set
     */
    public void setPbmProfileName(final String pbmProfileName) {
        this.pbmProfileName = pbmProfileName;
    }
}
