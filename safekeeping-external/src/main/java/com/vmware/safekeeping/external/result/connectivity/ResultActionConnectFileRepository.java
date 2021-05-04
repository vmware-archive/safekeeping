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
package com.vmware.safekeeping.external.result.connectivity;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectFileRepository;
import com.vmware.safekeeping.external.type.options.FileRepositoryOptions;
import com.vmware.safekeeping.external.type.options.RepositoryOptions;

public class ResultActionConnectFileRepository extends AbstractResultActionConnectRepository {

    /**
     * @param actionConnect
     * @return
     */
    public static void convert(final CoreResultActionConnectFileRepository src,
            final ResultActionConnectFileRepository dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionConnectRepository.convert(src, dst);
        RepositoryOptions.convert(src.getOptions(), dst.getOptions());

    }

    protected FileRepositoryOptions options;

    public void convert(CoreResultActionConnectFileRepository src) {
        ResultActionConnectFileRepository.convert(src, this);
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionConnectFileRepository.convert((CoreResultActionConnectFileRepository) src, this);
    }

    public FileRepositoryOptions getOptions() {
        return this.options;
    }

    public void setOptions(final FileRepositoryOptions options) {
        this.options = options;
    }

}
