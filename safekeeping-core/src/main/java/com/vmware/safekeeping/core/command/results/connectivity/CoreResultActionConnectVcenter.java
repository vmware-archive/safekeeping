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
package com.vmware.safekeeping.core.command.results.connectivity;

import org.apache.commons.lang.StringUtils;

public class CoreResultActionConnectVcenter extends AbstractCoreResultActionConnectDisconnectVcenter {

    /**
     *
     */
    private static final long serialVersionUID = -8340474635527749314L;
    private String resourcePoolFilter;
    private String vmFolderFilter;

    public CoreResultActionConnectVcenter(final CoreResultActionConnect rac) {
        super(rac);
        rac.getSubActionConnectVCenters().add(this);
    }

    public String getResourcePoolFilter() {
        return this.resourcePoolFilter;
    }

    public String getVmFolderFilter() {
        return this.vmFolderFilter;
    }

    public boolean isResourcePoolFilterSet() {
        return StringUtils.isNotBlank(this.resourcePoolFilter);
    }

    public boolean isVmFolderFilterSet() {
        return StringUtils.isNotBlank(this.vmFolderFilter);
    }

    public void setResourcePoolFilter(final String rpFilter) {
        this.resourcePoolFilter = rpFilter;
    }

    public void setVmFolderFilter(final String vmFilter) {
        this.vmFolderFilter = vmFilter;
    }
}
