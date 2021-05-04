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

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.QueryBlocksOption;

public class CoreBackupOptions extends AbstractCoreBackupOptions {

    /**
     * 
     */
    private static final long serialVersionUID = 7708468562840069835L;

    private Boolean compression;

    private Boolean cipher;

    private int maxBlockSize;

    private QueryBlocksOption queryBlocksOption;

    private VirtualMachineQuisceSpec quisceSpec;

    /**
     *
     */
    public CoreBackupOptions() {
        this.compression = CoreGlobalSettings.isCompressionEnable();
        this.cipher = CoreGlobalSettings.isCipherEnable();
        this.maxBlockSize = (CoreGlobalSettings.getMaxBlockSize() * (Utility.ONE_MBYTES));
        this.queryBlocksOption = CoreGlobalSettings.useQueryAllocatedBlocks() ? QueryBlocksOption.ALLOCATED
                : QueryBlocksOption.CHANGED_AREAS;
        this.quisceSpec = new VirtualMachineQuisceSpec();
        this.requestedBackupMode = BackupMode.UNKNOW;
    }

    public CoreBackupOptions(final CoreVirtualBackupOptions options) {
        this();
        setNumberOfThreads(options.getNumberOfThreads());
        setAnyFcoOfType(options.getAnyFcoOfType());
        setQuiet(options.isQuiet());
        setDryRun(options.isDryRun());
        getTargetFcoList().putAll(options.getTargetFcoList());
        setVim(options.getVim());
        this.requestedBackupMode = BackupMode.FULL;
    }

    /**
     * @return the maxBlockSize
     */
    public int getMaxBlockSize() {
        return this.maxBlockSize;
    }

    /**
     * @return the queryBlocksOption
     */
    public QueryBlocksOption getQueryBlocksOption() {
        return this.queryBlocksOption;
    }

    public VirtualMachineQuisceSpec getQuisceSpec() {
        return this.quisceSpec;
    }

    public Boolean isCipher() {
        if (this.cipher == null) {
            this.cipher = CoreGlobalSettings.isCipherEnable();
        }
        return this.cipher;
    }

    public Boolean isCompression() {
        if (this.compression == null) {
            this.cipher = CoreGlobalSettings.isCompressionEnable();
        }
        return this.compression;
    }

    public void setCipher(final Boolean cipher) {
        this.cipher = cipher;
    }

    public void setCompression(final Boolean compression) {
        this.compression = compression;
    }

    /**
     * @param maxBlockSize the maxBlockSize to set
     */
    public void setMaxBlockSize(final int maxBlockSize) {
        this.maxBlockSize = maxBlockSize;
    }

    /**
     * @param queryBlocksOption the queryBlocksOption to set
     */
    public void setQueryBlocksOption(final QueryBlocksOption queryBlocksOption) {
        this.queryBlocksOption = queryBlocksOption;
    }

    public void setQuisceSpec(final VirtualMachineQuisceSpec quisceSpec) {
        this.quisceSpec = quisceSpec;
    }

}
