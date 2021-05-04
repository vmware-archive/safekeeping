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
package com.vmware.safekeeping.external.result;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;

public abstract class ResultActionRestore extends AbstractResultActionBackupRestore {
    public static void convert(final AbstractCoreResultActionRestore src, final ResultActionRestore dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionBackupRestore.convert(src, dst);
        dst.setRestoreFcoIndex(src.getRestoreFcoIndex());

    }

    private Integer restoreFcoIndex;

    /**
     * @return the restoreFcoIndex
     */
    public Integer getRestoreFcoIndex() {
        return this.restoreFcoIndex;
    }

    /**
     * @param restoreFcoIndex the restoreFcoIndex to set
     */
    public void setRestoreFcoIndex(final Integer restoreFcoIndex) {
        this.restoreFcoIndex = restoreFcoIndex;
    }
}
