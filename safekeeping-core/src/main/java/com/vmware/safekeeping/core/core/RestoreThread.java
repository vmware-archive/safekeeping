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
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.vmware.jvix.jDiskLib.DiskHandle;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreDiskInteractive;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;

class RestoreThread extends AbstractBlockThread implements IRestoreThread {

    private final DiskHandle diskHandle;
    private final CoreResultActionDiskRestore radr;
    private final Semaphore semaphore;

    RestoreThread(final ExBlockInfo blockInfo, final Buffers buffers, final CoreResultActionDiskRestore radr,
            final AbstractRestoreDiskInteractive interactive, final Logger logger) {
        super(blockInfo, buffers, interactive, logger);
        this.diskHandle = radr.getDiskHandle();
        this.radr = radr;
        this.semaphore = buffers.getSemaphore();
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
                this.blockInfo.failed(e.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                return false;
            } catch (final Exception e) {
                Utility.logWarning(this.logger, e);
                this.blockInfo.setReason("Server error - Check Logs");
                return false;
            }
        }
    }

    @Override
    public ExBlockInfo getBlockInfo() {
        return this.blockInfo;
    }

    private boolean run(final Integer bufferIndex) {
        boolean result = false;
        if (bufferIndex != null) {
            final TargetBuffer buffer = this.buffers.getBuffer(bufferIndex);
            try {
                result = (this.target.openGetDump(this.blockInfo, buffer)
                        && computeOpenGetDump(this.blockInfo, buffer, true) && vddkWrite(bufferIndex, this.tentative));
            } catch (final BadPaddingException | IllegalBlockSizeException | IOException e) {
                Utility.logWarning(this.logger, e);
                this.blockInfo.setReason(e.getMessage());
            } finally {
                result &= this.target.closeGetDump(this.blockInfo, bufferIndex);
                this.buffers.getBuffer(bufferIndex).getAvailable().set(true);
                this.blockInfo.setFailed(!result);
                this.radr.addDumpInfo(this.blockInfo.getIndex(), this.blockInfo);
                reportResult(this.blockInfo, result);
            }
        }
        return result;
    }

    private boolean vddkWrite(final int bufferIndex, final int tentative) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("int - start"); //$NON-NLS-1$
        }
        long dliResult = jDiskLibConst.VIX_OK;
        boolean result = false;
        try {
            if (this.buffers.isRunning()) {
                try {
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.finest(String.format("Index %d Sector %d - Semaphore ready to acquire",
                                this.blockInfo.getIndex(), this.blockInfo.getOffset()));// $NON-NLS-1$
                    }
                    this.semaphore.acquire();
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.finest(String.format("Index %d Sector %d - Semaphore acquired",
                                this.blockInfo.getIndex(), this.blockInfo.getOffset())); // $NON-NLS-1$
                    }
                    dliResult = SJvddk.dli.write(this.diskHandle, this.blockInfo.getOffset(),
                            this.blockInfo.getLength(), this.buffers.getBuffer(bufferIndex).getOutputBuffer());

                } finally {
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.finest(String.format("Index %d Sector %d - Semaphore ready to release",
                                this.blockInfo.getIndex(), this.blockInfo.getOffset()));// $NON-NLS-1$
                    }
                    this.semaphore.release();
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.finest(String.format("Index %d Sector %d - Semaphore released",
                                this.blockInfo.getIndex(), this.blockInfo.getOffset()));// $NON-NLS-1$
                    }
                }
                result = dliResult == jDiskLibConst.VIX_OK;
                if (result) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        final String msg = String.format(
                                "Index:%d Buffer:%d  size:%d  Write on handle %d start:%d nSectors:%d",
                                this.blockInfo.getIndex(), bufferIndex, this.blockInfo.getSizeInBytes(),
                                this.diskHandle.getHandle(), this.blockInfo.getOffset(), this.blockInfo.getLength());
                        this.logger.fine(msg);
                    }
                } else {

                    this.blockInfo.setReason(SJvddk.dli.getErrorText(dliResult, null));
                    final String msg = String.format("Index:%d Buffer:%d Tentative:%d  Error:%s",
                            this.blockInfo.getIndex(), bufferIndex, tentative, this.blockInfo.getReason());
                    this.logger.warning(msg);
                }

            }
        } catch (final Exception e) {
            this.logger.severe("int - exception: " + e); //$NON-NLS-1$

            this.blockInfo.setReason(e.getMessage());

        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("int - end"); //$NON-NLS-1$
        }
        return result;
    }
}
