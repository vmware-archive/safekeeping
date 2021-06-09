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
package com.vmware.safekeeping.core.core;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractVirtualBackupDiskInteractive;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskVirtualBackup;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

class VirtualBackupThread extends AbstractBlockThread implements IRestoreThread {

    private final CoreResultActionDiskVirtualBackup radr;

    VirtualBackupThread(final ExBlockInfo blockInfo, final Buffers buffers,
            final CoreResultActionDiskVirtualBackup radr, final String[] report,
            final AbstractVirtualBackupDiskInteractive interactive, final Logger logger) {
        super(blockInfo, buffers, report, interactive, logger);
        this.radr = radr;
    }

    @Override
    public Boolean call() {
        for (;;) {
            try {
                final Integer bufferIndex = waitForBuffer(this.blockInfo);
                final boolean result = run(bufferIndex);
                if (checkResult(result)) {
                    return result;
                }
            } catch (final InterruptedException e) {
                this.logger.severe("<no args> - exception: " + e); //$NON-NLS-1$
                this.blockInfo.failed(getEntity(), e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                return false;
            } catch (final Exception e) {
                Utility.logWarning(this.logger, e);
                this.blockInfo.setReason(getEntity(), "Server error - Check Logs");
                return false;
            }
        }
    }

    private boolean cloneDump(final ExBlockInfo blockInfoOut, final TargetBuffer buffer) {
        boolean result = false;
        try {
            BlockLocker.lockBlock(blockInfoOut);
            result = this.target.dedupDump(blockInfoOut);
        } catch (final InterruptedException e) {
            blockInfoOut.setReason(getEntity(), e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            BlockLocker.releaseBlock(blockInfoOut);
            blockInfoOut.setFailed(!result);
            this.radr.addDumpInfo(blockInfoOut.getIndex(), blockInfoOut);
            reportResult(blockInfoOut, result);

            buffer.getAvailable().set(true);
        }
        return result;
    }

    @Override
    public ExBlockInfo getBlockInfo() {
        return this.blockInfo;
    }

    private boolean postDump(final ExBlockInfo blockInfo, final TargetBuffer buffer)
            throws BadPaddingException, IllegalBlockSizeException {
        boolean result = false;
        try {
            this.target.openPostDump(blockInfo);
            processDump(blockInfo, buffer);
            try {
                BlockLocker.lockBlock(blockInfo);
                result = this.target.closePostDump(blockInfo, buffer);
            } finally {
                BlockLocker.releaseBlock(blockInfo);

            }
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            blockInfo.setReason(getEntity(), e);
        } catch (final InterruptedException e) {
            blockInfo.setReason(getEntity(), e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private boolean run(final Integer bufferIndex) {
        boolean result = false;
        if (bufferIndex != null) {
            final TargetBuffer buffer = this.buffers.getBuffer(bufferIndex);

            if (this.blockInfo.isModified()) {
                try {
                    result = (this.target.openGetDump(this.blockInfo, buffer)
                            && computeOpenGetDump(this.blockInfo, buffer, false)
                            && this.target.closeGetDump(this.blockInfo, bufferIndex)
                            && calculateSha1(this.blockInfo, buffer));
                    if (result) {
                        if (this.target.doesKeyExist(this.blockInfo) || BlockLocker.isBlockLocked(this.blockInfo)) {
                            result = cloneDump(this.blockInfo, buffer);
                        } else {
                            result = postDump(this.blockInfo, buffer);
                        }
                    }
                } catch (IllegalBlockSizeException | IOException | BadPaddingException e) {
                    this.blockInfo.setReason(getEntity(), e);
                    Utility.logWarning(this.logger, e);
                } finally {
                    this.blockInfo.setFailed(!result);
                    buffer.getAvailable().set(true);
                    this.radr.addDumpInfo(this.blockInfo.getIndex(), this.blockInfo);
                    reportResult(this.blockInfo, result);
                }
            } else {
                result = cloneDump(this.blockInfo, buffer);
            }

        }
        return result;
    }

    @Override
    protected ManagedFcoEntityInfo getEntity() {
        return radr.getFcoEntityInfo();
    }
}
