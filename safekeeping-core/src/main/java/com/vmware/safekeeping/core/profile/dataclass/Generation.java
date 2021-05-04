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

import java.io.Serializable;
import java.util.Date;

import com.vmware.safekeeping.core.type.enums.BackupMode;

public class Generation implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -636163108805818183L;
    private BackupMode backupMode;
    private boolean succeeded;
    private Date timestamp;
    private Integer dependingOnGenerationId;
    private Integer genId;

    /**
     * @return the backupMode
     */
    public BackupMode getBackupMode() {
        return this.backupMode;
    }

    /**
     * @return the dependingOnGenerationId
     */
    public Integer getDependingOnGenerationId() {
        return this.dependingOnGenerationId;
    }

    /**
     * @return the genId
     */
    public Integer getGenId() {
        return this.genId;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the succeeded
     */
    public boolean isSucceeded() {
        return this.succeeded;
    }

    /**
     * @param backupMode the backupMode to set
     */
    public void setBackupMode(final BackupMode backupMode) {
        this.backupMode = backupMode;
    }

    /**
     * @param dependingOnGenerationId the dependingOnGenerationId to set
     */
    public void setDependingOnGenerationId(final Integer dependingOnGenerationId) {
        this.dependingOnGenerationId = dependingOnGenerationId;
    }

    /**
     * @param genId the genId to set
     */
    public void setGenId(final Integer genId) {
        this.genId = genId;
    }

    /**
     * @param succeeded the succeeded to set
     */
    public void setSucceeded(final boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

}
