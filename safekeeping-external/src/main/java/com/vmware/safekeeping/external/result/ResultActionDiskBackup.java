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

import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.QueryBlocksOption;
import com.vmware.safekeeping.core.type.enums.phase.BackupDiskPhases;

public class ResultActionDiskBackup extends AbstractResultDiskBackupRestore {
    public static void convert(final CoreResultActionDiskBackup src, final ResultActionDiskBackup dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        try {
            AbstractResultDiskBackupRestore.convert(src, dst);
            dst.setGenerationId(src.getGenerationId());
            dst.setChangeId(src.getChangeId());
            dst.setTotalNumberOfDisks(src.getTotalNumberOfDisks());
            dst.setBackupMode(src.getBackupMode());
            dst.setNoChanges(src.hasNoChanges());
            dst.setCompressed(src.isCompressed());
            dst.setCbtHealth(src.isCbtHealth());
            dst.setCipher(src.isCipher());
            dst.setPhase(src.getPhase());
            dst.setProvisioningType(src.getProvisioningType());
            dst.setQueryBlocksOption(src.getQueryBlocksOption());
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private Integer generationId;

    private String changeId;

    private BackupMode backupMode;

    private boolean noChanges;

    private boolean compressed;

    private boolean cbtHealth;

    private boolean cipher;

    private BackupDiskPhases phase;

    private QueryBlocksOption queryBlocksOption;

    private int totalNumberOfDisks;

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionDiskBackup.convert((CoreResultActionDiskBackup) src, this);
    }

    /**
     * @return the backupMode
     */
    public BackupMode getBackupMode() {
        return this.backupMode;
    }

    /**
     * @return the changeId
     */
    public String getChangeId() {
        return this.changeId;
    }

    /**
     * @return the generationId
     */
    public Integer getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the phase
     */
    public BackupDiskPhases getPhase() {
        return this.phase;
    }

    /**
     * @return the queryBlocksOption
     */
    public QueryBlocksOption getQueryBlocksOption() {
        return this.queryBlocksOption;
    }

    /**
     * @return the totalNumberOfDisks
     */
    @Override
    public int getTotalNumberOfDisks() {
        return this.totalNumberOfDisks;
    }

    /**
     * @return the cbtHealth
     */
    public boolean isCbtHealth() {
        return this.cbtHealth;
    }

    /**
     * @return the cipher
     */
    public boolean isCipher() {
        return this.cipher;
    }

    /**
     * @return the compressed
     */
    public boolean isCompressed() {
        return this.compressed;
    }

    /**
     * @return the noChanges
     */
    public boolean isNoChanges() {
        return this.noChanges;
    }

    /**
     * @param backupMode the backupMode to set
     */
    public void setBackupMode(final BackupMode backupMode) {
        this.backupMode = backupMode;
    }

    /**
     * @param cbtHealth the cbtHealth to set
     */
    public void setCbtHealth(final boolean cbtHealth) {
        this.cbtHealth = cbtHealth;
    }

    /**
     * @param changeId the changeId to set
     */
    public void setChangeId(final String changeId) {
        this.changeId = changeId;
    }

    /**
     * @param cipher the cipher to set
     */
    public void setCipher(final boolean cipher) {
        this.cipher = cipher;
    }

    /**
     * @param compressed the compressed to set
     */
    public void setCompressed(final boolean compressed) {
        this.compressed = compressed;
    }

    /**
     * @param generationId the generationId to set
     */
    public void setGenerationId(final Integer generationId) {
        this.generationId = generationId;
    }

    /**
     * @param noChanges the noChanges to set
     */
    public void setNoChanges(final boolean noChanges) {
        this.noChanges = noChanges;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(final BackupDiskPhases phase) {
        this.phase = phase;
    }

    /**
     * @param queryBlocksOption the queryBlocksOption to set
     */
    public void setQueryBlocksOption(final QueryBlocksOption queryBlocksOption) {
        this.queryBlocksOption = queryBlocksOption;
    }

    /**
     * @param totalNumberOfDisks the totalNumberOfDisks to set
     */
    @Override
    public void setTotalNumberOfDisks(final int totalNumberOfDisks) {
        this.totalNumberOfDisks = totalNumberOfDisks;
    }
}
