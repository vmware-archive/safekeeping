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
package com.vmware.safekeeping.core.command.results.miscellanea;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionWithSubOperations;

public class CoreResultActionCloneConfigurationFiles extends CoreResultActionCloneVappConfigurationFile {

    /**
     * 
     */
    private static final long serialVersionUID = -1721390632691656838L;
    private String srcNvramContentPath;
    private String srcVmxContentPath;

    private String dstNvramContentPath;
    private String dstVmxContentPath;

    public CoreResultActionCloneConfigurationFiles(final AbstractCoreResultActionWithSubOperations parent) {
        super(parent);
    }

    public String getDstNvramContentPath() {
        return this.dstNvramContentPath;
    }

    public String getDstVmxContentPath() {
        return this.dstVmxContentPath;
    }

    public String getSrcNvramContentPath() {
        return this.srcNvramContentPath;
    }

    public String getSrcVmxContentPath() {
        return this.srcVmxContentPath;
    }

    public void setDstNvramContentPath(final String dstNvramContentPath) {
        this.dstNvramContentPath = dstNvramContentPath;
    }

    public void setDstVmxContentPath(final String dstVmxContentPath) {
        this.dstVmxContentPath = dstVmxContentPath;
    }

    public void setSrcNvramContentPath(final String srcNvramContentPath) {
        this.srcNvramContentPath = srcNvramContentPath;
    }

    public void setSrcVmxContentPath(final String srcVmxContentPath) {
        this.srcVmxContentPath = srcVmxContentPath;
    }

}