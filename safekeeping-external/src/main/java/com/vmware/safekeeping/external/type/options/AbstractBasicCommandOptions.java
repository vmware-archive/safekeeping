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
package com.vmware.safekeeping.external.type.options;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.type.FcoTarget;

public abstract class AbstractBasicCommandOptions {

    public static void convert(final AbstractBasicCommandOptions src, final CoreBasicCommandOptions dst,
            final int defaultAnyFcoOfType) {
        if ((src == null) || (dst == null)) {
            return;
        }
        if (src.getTargetList().isEmpty()) {
            if (src.anyFcoOfType == null) {
                dst.setAnyFcoOfType(FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
            } else {
                dst.setAnyFcoOfType(src.anyFcoOfType);
            }
        } else {
            dst.setAnyFcoOfType(defaultAnyFcoOfType);
        }
        dst.setDryRun(src.dryRun);

        dst.setVim((src.vim == null) ? StringUtils.EMPTY : src.vim);
        if (src.targetList != null) {
            for (final FcoTarget target : src.targetList) {
                dst.getTargetFcoList().put(target.getKey(), target);
            }
        }
        AbstractBasicCommandOptions.targetList2TargetFcoList(src.targetList, dst.getTargetFcoList());
    }

    private static int targetList2TargetFcoList(final List<FcoTarget> src, final Map<String, FcoTarget> dst) {
        int result = 0;
        for (final FcoTarget target : src) {
            dst.put(target.getKey(), target);
            ++result;
        }
        return result;
    }

    private Integer anyFcoOfType;
    private boolean dryRun;
    private List<FcoTarget> targetList;
    private String vim;

    protected AbstractBasicCommandOptions() {
        this.targetList = new LinkedList<>();
    }

    /**
     * @return the anyFcoOfType
     */
    public Integer getAnyFcoOfType() {
        return this.anyFcoOfType;
    }

    /**
     * @return the targetList
     */
    public List<FcoTarget> getTargetList() {
        return this.targetList;
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
        return this.dryRun;
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
    public void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * @param targetList the targetList to set
     */
    public void setTargetList(final List<FcoTarget> targetList) {
        this.targetList = targetList;
    }

    /**
     * @param vim the vim to set
     */
    public void setVim(final String vim) {
        this.vim = vim;
    }
}
