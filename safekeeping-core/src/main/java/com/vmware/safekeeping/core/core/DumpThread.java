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
import com.vmware.safekeeping.core.command.interactive.AbstractBackupDiskInteractive;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;

class DumpThread extends AbstractBlockThread implements IDumpThread {

    private final DiskHandle diskHandle;
    private final CoreResultActionDiskBackup radb;
    private final Semaphore semaphore;

    DumpThread(final ExBlockInfo blockInfo, final Buffers buffers, final CoreResultActionDiskBackup radb,
            final String[] report, final AbstractBackupDiskInteractive interactive, final Logger logger) {
        super(blockInfo, buffers, report, interactive, logger);
        this.diskHandle = radb.getDiskHandle();
        this.radb = radb;
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

    private boolean cloneDump(final ExBlockInfo blockInfoOut, final TargetBuffer buffer) {
        final Runnable runnable = () -> {
            boolean result1 = false;
            try {
                BlockLocker.lockBlock(blockInfoOut);
                result1 = this.target.dedupDump(blockInfoOut);
            } catch (final InterruptedException e) {
                blockInfoOut.setReason(e);
                this.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                BlockLocker.releaseBlock(blockInfoOut);
                blockInfoOut.setFailed(!result1);
                this.radb.addDumpInfo(blockInfoOut.getIndex(), blockInfoOut);
                reportResult(blockInfoOut, result1);
            }
        };
        buffer.getAvailable().set(true);
        this.buffers.executeSubTask(runnable);
        return true;
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
            blockInfo.setReason(e);
        } catch (final InterruptedException e) {
            blockInfo.setReason(e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            blockInfo.setReason(e);
            Utility.logWarning(this.logger, e);
        } finally {
            blockInfo.setFailed(!result);
            this.radb.addDumpInfo(blockInfo.getIndex(), blockInfo);
            reportResult(blockInfo, result);
            buffer.getAvailable().set(true);
        }
        return result;
    }

    private boolean run(final Integer bufferIndex) {
        boolean result = false;
        if (bufferIndex != null) {
            try {
                final TargetBuffer buffer = this.buffers.getBuffer(bufferIndex);

                if (vddkRead(bufferIndex)) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        final String msg = String.format(
                                "Index:%d Buffer:%d  size:%d  Write on handle %d start:%d nSectors:%d",
                                this.blockInfo.getIndex(), bufferIndex, this.blockInfo.getSizeInBytes(),
                                this.diskHandle.getHandle(), this.blockInfo.getOffset(), this.blockInfo.getLength());
                        this.logger.fine(msg);
                    }
                    calculateSha1(this.blockInfo, buffer);
                    if (this.target.doesKeyExist(this.blockInfo) || BlockLocker.isBlockLocked(this.blockInfo)) {
                        result = cloneDump(this.blockInfo, buffer);
                    } else {
                        result = postDump(this.blockInfo, buffer);
                    }
                }
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                result = false;
                this.blockInfo.setReason(e);
                Utility.logWarning(this.logger, e);
            } catch (final Exception e) {
                result = false;
                this.blockInfo.setReason("Server error - Check Logs");
                Utility.logWarning(this.logger, e);
            }
        }
        return result;
    }

    private boolean vddkRead(final int bufferIndex) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("int, int - start"); //$NON-NLS-1$
        }

        boolean result = false;
        if (this.buffers.isRunning()) {
            long dliResult = jDiskLibConst.VIX_OK;
            try {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.finest(String.format("Index %d Sector %d - Semaphore ready to acquire",
                            this.blockInfo.getIndex(), this.blockInfo.getOffset())); // $NON-NLS-1$
                }
                this.semaphore.acquire();
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.finest(String.format("Index %d Sector %d - Semaphore acquired",
                            this.blockInfo.getIndex(), this.blockInfo.getOffset())); // $NON-NLS-1$
                }
                dliResult = SJvddk.dli.read(this.diskHandle, this.blockInfo.getOffset(), this.blockInfo.getLength(),
                        this.buffers.getBuffer(bufferIndex).getInputBuffer());
            } catch (final InterruptedException e) {
                this.blockInfo.setReason(e.getMessage());
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.finest(String.format("Index %d Sector %d - Semaphore ready to release",
                            this.blockInfo.getIndex(), this.blockInfo.getOffset()));
                }
                this.semaphore.release();
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.finest(String.format("Index %d Sector %d - Semaphore released",
                            this.blockInfo.getIndex(), this.blockInfo.getOffset()));
                }
            }
            if (dliResult == jDiskLibConst.VIX_OK) {
                result = true;
            } else {
                this.blockInfo.setReason(SJvddk.dli.getErrorText(dliResult, null));
                final String msg = String.format("Index:%d Buffer:%d  Error:%s", this.blockInfo.getIndex(), bufferIndex,
                        this.blockInfo.getReason());
                this.logger.warning(msg);
            }

        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("int, int - end"); //$NON-NLS-1$
        }
        return result;
    }
}
