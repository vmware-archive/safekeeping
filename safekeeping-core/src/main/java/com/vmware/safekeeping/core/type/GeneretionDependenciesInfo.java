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

import com.vmware.safekeeping.core.profile.dataclass.Generation;
import com.vmware.safekeeping.core.type.enums.BackupMode;

public class GeneretionDependenciesInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6230594899489257506L;
    private boolean exist;
    private int genId;
    private BackupMode mode;
    private int dependOnGenerationId;
    private Generation dependingGenerationId;

    /**
     * @return the dependingGenerationId
     */
    public Generation getDependingGenerationId() {
        return this.dependingGenerationId;
    }

    public int getDependOnGenerationId() {
        return this.dependOnGenerationId;
    }

    public int getGenId() {
        return this.genId;
    }

    /**
     * @return the mode
     */
    public BackupMode getMode() {
        return this.mode;
    }

    public boolean hasDependency() {
        return this.dependingGenerationId != null;
    }

    public boolean isDependingOn() {
        return this.dependOnGenerationId > -1;
    }

    public boolean isExist() {
        return this.exist;
    }

    /**
     * @param generation the dependingGenerationId to set
     */
    public void setDependingGenerationId(final Generation generation) {
        this.dependingGenerationId = generation;
    }

    public void setDependOnGenerationId(final int dependId) {
        this.dependOnGenerationId = dependId;
    }

    public void setExist(final boolean exist) {
        this.exist = exist;
    }

    public void setGenId(final int genId) {
        this.genId = genId;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(final BackupMode mode) {
        this.mode = mode;
    }
}
