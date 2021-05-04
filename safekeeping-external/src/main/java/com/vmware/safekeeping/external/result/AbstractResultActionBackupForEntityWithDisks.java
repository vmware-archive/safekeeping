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

import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.type.enums.phase.BackupPhases;

public abstract class AbstractResultActionBackupForEntityWithDisks extends ResultActionBackup {
    private static final Logger logger = Logger.getLogger(AbstractResultActionBackupForEntityWithDisks.class.getName());

    public static void convert(final AbstractCoreResultActionBackupForEntityWithDisks src,
            final AbstractResultActionBackupForEntityWithDisks dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        try {
            ResultActionBackup.convert(src, dst);
            dst.setPhase(src.getPhase());

            dst.setCompressed(src.isCompressed());
            dst.setCipher(src.isCipher());
            dst.setNumberOfDisk(src.getNumberOfDisk());
        } catch (final Exception e) {
            Utility.logWarning(AbstractResultActionBackupForEntityWithDisks.logger, e);
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private int numberOfDisk;
    private BackupPhases phase;

    private boolean compressed;

    private boolean cipher;

    protected AbstractResultActionBackupForEntityWithDisks() {
    }

    /**
     * @return the numberOfDisk
     */
    public int getNumberOfDisk() {
        return this.numberOfDisk;
    }

    /**
     * @return the phase
     */
    @Override
    public BackupPhases getPhase() {
        return this.phase;
    }

    /**
     * @return the cipher
     */
    public boolean isCipher() {
        return this.cipher;
    }

//

    /**
     * @return the compressed
     */
    public boolean isCompressed() {
        return this.compressed;
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
     * @param numberOfDisk the numberOfDisk to set
     */
    public void setNumberOfDisk(final int numberOfDisk) {
        this.numberOfDisk = numberOfDisk;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(final BackupPhases phase) {
        this.phase = phase;
    }

}
