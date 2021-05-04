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

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.options.VirtualMachineQuisceSpec;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.QueryBlocksOption;

public class BackupOptions extends VirtualBackupOptions {

    public static void convert(final BackupOptions src, final CoreBackupOptions dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        BackupRestoreCommonOptions.convert(src, dst, FirstClassObjectFilterType.any);
        if (src.compression != null) {
            dst.setCompression(src.compression);
        }
        if (src.cipher != null) {
            dst.setCipher(src.cipher);
        }

        if (src.getMaxBlockSize() != null) {
            dst.setMaxBlockSize(src.maxBlockSize);
        }
        dst.setRequestedBackupMode((src.requestedBackupMode == null) ? BackupMode.UNKNOW : src.requestedBackupMode);
        if (src.getQueryBlocksOption() != null) {
            dst.setQueryBlocksOption(src.queryBlocksOption);
        }
        if (src.quisceSpec != null) {
            dst.setQuisceSpec(new VirtualMachineQuisceSpec(src.quisceSpec));
        }
        if (StringUtils.isNotEmpty(src.requestedTransportMode)) {
            dst.setRequestedTransportModes(src.requestedTransportMode);
        }
    }

    private String requestedTransportMode;

    private Boolean compression;

    private Boolean cipher;

    private BackupMode requestedBackupMode;
    private Integer maxBlockSize;
    private QueryBlocksOption queryBlocksOption;
    private VirtualMachineQuisceSpec quisceSpec;

    /**
     * @return the maxBlockSize
     */
    public Integer getMaxBlockSize() {
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

    /**
     * @return the requestedBackupMode
     */
    public BackupMode getRequestedBackupMode() {
        return this.requestedBackupMode;
    }

    public String getRequestedTransportMode() {
        return this.requestedTransportMode;
    }

    /**
     * @return the cipher
     */
    public Boolean isCipher() {
        return this.cipher;
    }

    /**
     * @return the compression
     */
    public Boolean isCompression() {
        return this.compression;
    }

    /**
     *
     * /**
     *
     * @param cipher the cipher to set
     */
    public void setCipher(final Boolean cipher) {
        this.cipher = cipher;
    }

    /**
     * @param compression the compression to set
     */
    public void setCompression(final Boolean compression) {
        this.compression = compression;
    }

    /**
     * @param maxBlockSize the maxBlockSize to set
     */
    public void setMaxBlockSize(final Integer maxBlockSize) {
        this.maxBlockSize = maxBlockSize;
    }

    /**
     *
     * /**
     *
     * @param queryBlocksOption the queryBlocksOption to set
     */
    public void setQueryBlocksOption(final QueryBlocksOption queryBlocksOption) {
        this.queryBlocksOption = queryBlocksOption;
    }

    public void setQuisceSpec(final VirtualMachineQuisceSpec quisceSpec) {
        this.quisceSpec = quisceSpec;
    }

    /**
     * @param requestedBackupMode the requestedBackupMode to set
     */
    public void setRequestedBackupMode(final BackupMode requestedBackupMode) {
        this.requestedBackupMode = requestedBackupMode;
    }

    public void setRequestedTransportMode(final String requestedTransportMode) {
        this.requestedTransportMode = requestedTransportMode;
    }

}
