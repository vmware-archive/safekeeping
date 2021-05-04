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
package com.vmware.safekeeping.core.type;

import java.io.Serializable;

import com.vmware.safekeeping.core.type.enums.BackupMode;

public class GenerationInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6433473924681523763L;
    private int generationId;
    private BackupMode backupMode;
    private Integer prevGenId;
    private String uri;

    public BackupMode getBackupMode() {
        return this.backupMode;
    }

    public int getGenerationId() {
        return this.generationId;
    }

    public Integer getPreviousGenerationId() {
        return this.prevGenId;

    }

    public String getTargetUri() {
        return this.uri;
    }

    public void setBackupMode(final BackupMode backupMode) {
        this.backupMode = backupMode;
    }

    /**
     * @param generationId
     */
    public void setGenerationId(final int generationId) {
        this.generationId = generationId;
    }

    public void setPreviousGenerationId(final Integer prevGenId) {
        this.prevGenId = prevGenId;
    }

    public void setTargetUri(final String uri) {
        this.uri = uri;
    }

}
