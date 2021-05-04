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
package com.vmware.safekeeping.core.command.options;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.core.type.FcoTarget;

public class CoreBasicCommandOptions implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3783392521744911447L;

    public static Map<String, FcoTarget> toTargetFcoList(final Iterable<FcoTarget> targetList) {
        final Map<String, FcoTarget> result = new LinkedHashMap<>();
        for (final FcoTarget target : targetList) {
            result.put(target.getKey(), target);
        }
        return result;
    }

    private Integer anyFcoOfType;

    private boolean quiet;

    private boolean isDryRun;

    private final Map<String, FcoTarget> targetFcoList;
    private String vim;

    public CoreBasicCommandOptions() {
        this.anyFcoOfType = FirstClassObjectFilterType.any;

        this.targetFcoList = new LinkedHashMap<>();
        this.vim = "";
        this.quiet = false;
        this.isDryRun = false;
    }

    /**
     * @return the anyFcoOfType
     */
    public Integer getAnyFcoOfType() {
        return this.anyFcoOfType;
    }

    /**
     * @return the targetFcoList
     */
    @XmlTransient
    public Map<String, FcoTarget> getTargetFcoList() {
        return this.targetFcoList;
    }

    /**
     * @return the vim
     */
    public String getVim() {
        return this.vim;
    }

    /**
     * @return the isDryRun
     */
    public boolean isDryRun() {
        return this.isDryRun;
    }

    /**
     * @return the quiet
     */
    public boolean isQuiet() {
        return this.quiet;
    }

    /**
     * @param anyFcoOfType the anyFcoOfType to set
     */
    public void setAnyFcoOfType(final Integer anyFcoOfType) {
        this.anyFcoOfType = anyFcoOfType;
    }

    /**
     * @param isDryRun the isDryRun to set
     */
    public void setDryRun(final boolean isDryRun) {
        this.isDryRun = isDryRun;
    }

    /**
     * @param quiet the quiet to set
     */
    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * @param vim the vim to set
     */
    public void setVim(final String vim) {
        this.vim = vim;
    }

}
