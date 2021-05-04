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

import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionExtension;
import com.vmware.safekeeping.core.type.enums.ExtensionManagerOperation;
import com.vmware.safekeeping.external.result.ResultAction;

public class ResultActionExtension extends ResultAction {

    public static void convert(final CoreResultActionExtension src, final ResultActionExtension dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);

        dst.operation = src.getOperation();
        dst.version = src.getExtension().getVersion();
        dst.healthInfo = src.getExtension().getHealthInfo() != null;
    }

    private String version;

    private boolean healthInfo;

    private ExtensionManagerOperation operation;

    public ExtensionManagerOperation getOperation() {
        return this.operation;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isHealthInfo() {
        return this.healthInfo;
    }

    public void setHealthInfo(final boolean healthInfo) {
        this.healthInfo = healthInfo;
    }

    public void setOperation(final ExtensionManagerOperation operation) {
        this.operation = operation;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

}
