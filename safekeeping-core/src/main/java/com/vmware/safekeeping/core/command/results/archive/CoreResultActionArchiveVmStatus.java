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
package com.vmware.safekeeping.core.command.results.archive;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vmware.safekeeping.core.command.results.support.CoreGenerationDisksInfoList;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class CoreResultActionArchiveVmStatus extends AbstractCoreResultActionArchiveStatus {

    /**
     * 
     */
    private static final long serialVersionUID = 9031749679483367683L;
    private final Map<Integer, CoreGenerationDisksInfoList> generationDisksInfoList;

    /**
     * @param target
     * @param fcoEntity
     * @param b
     */
    public CoreResultActionArchiveVmStatus(final ITarget target, final ManagedFcoEntityInfo fcoEntity) {
        super(target);
        this.generationDisksInfoList = new LinkedHashMap<>();
        setFcoEntityInfo(fcoEntity);
    }

    /**
     * @return the generationDisksInfoList
     */
    public Map<Integer, CoreGenerationDisksInfoList> getGenerationDisksInfoList() {
        return this.generationDisksInfoList;
    }

}