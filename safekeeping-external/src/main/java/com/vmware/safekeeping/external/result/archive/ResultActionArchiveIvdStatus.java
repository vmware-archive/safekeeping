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
package com.vmware.safekeeping.external.result.archive;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveIvdStatus;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationDiskInfo;
import com.vmware.safekeeping.external.type.GenerationDiskInfo;

public class ResultActionArchiveIvdStatus extends ResultActionArchiveStatus {
    public static void convert(final CoreResultActionArchiveIvdStatus src, final ResultActionArchiveIvdStatus dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultActionArchiveStatus.convert(src, dst);
        for (final CoreGenerationDiskInfo cgdi : src.getGenerationDiskInfo().values()) {
            dst.getGenerationDiskInfo().add(new GenerationDiskInfo(cgdi));
        }
    }

    private List<GenerationDiskInfo> generationDiskInfo;

    public ResultActionArchiveIvdStatus() {
        setGenerationDiskInfo(new LinkedList<>());
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionArchiveIvdStatus.convert((CoreResultActionArchiveIvdStatus) src, this);
    }

    /**
     * @return the generationDiskInfoList
     */
    public List<GenerationDiskInfo> getGenerationDiskInfo() {
        return this.generationDiskInfo;
    }

    /**
     * @param generationDiskInfoList the generationDiskInfoList to set
     */
    public void setGenerationDiskInfo(final List<GenerationDiskInfo> generationDiskInfoList) {
        this.generationDiskInfo = generationDiskInfoList;
    }

}
