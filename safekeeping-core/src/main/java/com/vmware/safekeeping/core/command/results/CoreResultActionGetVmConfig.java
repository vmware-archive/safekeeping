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
package com.vmware.safekeeping.core.command.results;

public class CoreResultActionGetVmConfig extends AbstractCoreResultActionImpl {

    /**
     * 
     */
    private static final long serialVersionUID = -1830972306331621445L;

    private Long size;

    private String vmPathName;
    private String vmNvRamPathName;
    private boolean vAppConfig;

    /**
     * @param result
     */
    public CoreResultActionGetVmConfig(final AbstractCoreResultActionBackupRestore parent) {
        super(parent);
        parent.getSubOperations().add(this);
        setFcoEntityInfo(parent.getFcoEntityInfo());
    }

    public Long getSize() {
        return this.size;
    }

    public String getVmPathName() {
        return this.vmPathName;
    }

    public String getVmNvRamPathName() {
        return this.vmNvRamPathName;
    }

    public boolean isvAppConfig() {
        return this.vAppConfig;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    /**
     * @param b
     */
    public void setvAppConfig(final boolean vAppConfig) {
        this.vAppConfig = vAppConfig;
    }

    /**
     * @param nvram
     */
    public void setVmNvRamPathName(final String vmNvRamPathName) {
        this.vmNvRamPathName = vmNvRamPathName;

    }

    public void setVmPathName(final String vmPathName) {
        this.vmPathName = vmPathName;
    }

}
