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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import com.linkedin.migz.MiGzOutputStream;
import com.vmware.safekeeping.common.ExtendedByteArrayOutputStream;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.InteractiveDisk;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.EncryptResult;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.util.AESEncryptionManager;

abstract class AbstractBlockThread {

    private static final int DUMP_BUFFER_SIZE = 20000;
    protected final Logger logger;
    protected final Buffers buffers;
    private final String[] report;
    private final InteractiveDisk interactive;
    protected final ITargetOperation target;
    protected final ExBlockInfo blockInfo;
    protected int tentative;
    protected final int maxBlockOperationRetries;

    AbstractBlockThread(final ExBlockInfo blockInfo, final Buffers buffers, final InteractiveDisk interactive,
            final Logger logger) {
        this.logger = logger;
        this.buffers = buffers;
        this.target = buffers.getTarget();
        this.report = null;
        this.interactive = interactive;
        this.blockInfo = blockInfo;
        this.tentative = 1;
        this.maxBlockOperationRetries = CoreGlobalSettings.getMaxBlockOperationRetries();
    }

    /**
     * @param logger
     * @param buffers
     */
    AbstractBlockThread(final ExBlockInfo blockInfo, final Buffers buffers, final String[] report,
            final InteractiveDisk interactive, final Logger logger) {
        this.logger = logger;
        this.buffers = buffers;
        this.target = buffers.getTarget();
        this.report = report;
        this.interactive = interactive;
        this.blockInfo = blockInfo;
        this.tentative = 1;
        this.maxBlockOperationRetries = CoreGlobalSettings.getMaxBlockOperationRetries();
    }

    protected boolean calculateSha1(final ExBlockInfo blockInfo, final TargetBuffer targetBuffer) {
        targetBuffer.shaUpdate(targetBuffer.getInputBuffer(), blockInfo.getStreamOffset(), blockInfo.getSizeInBytes());
        final byte[] digest = targetBuffer.shaDigest();
        blockInfo.setSha1(DatatypeConverter.printHexBinary(digest));
        return true;
    }

    protected boolean checkResult(final boolean result) throws InterruptedException {
        try {
            if (!result && this.buffers.isRunning()) {
                final String msg = String.format("Block n:%d Tentative %d/%d failed", this.blockInfo.getIndex(),
                        this.tentative, this.maxBlockOperationRetries);
                this.logger.warning(msg);
                // Check if this is not the last tentative
                if (this.tentative < (this.maxBlockOperationRetries)) {
                    Thread.sleep(CoreGlobalSettings.getWaitingTimeAfterBlockThreadFailureInMilliSeconds());
                } else {
                    final String msg1 = String.format("Block %d Max number of retries", this.blockInfo.getIndex());
                    this.logger.warning(msg1);
                    final String errMsg = String.format("%s Error(%s) - Check logs for more information", msg1,
                            this.blockInfo.getReason());
                    this.blockInfo.failed(getEntity(), errMsg);
                    return true;
                }
            } else {
                return true;
            }
            return false;
        } finally {
            ++tentative;
        }
    }

    /**
     *
     * @param blockInfo
     * @param targetBuffer
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeySpecException
     * @throws InterruptedException
     */
    protected void processDump(final ExBlockInfo blockInfo, final TargetBuffer targetBuffer)
            throws IOException, BadPaddingException, IllegalBlockSizeException, InterruptedException {
        byte[] buffer = targetBuffer.getInputBuffer();
        int count = blockInfo.getSizeInBytes();
        boolean released = false;
        if (blockInfo.isCompress()) {

            final int blockSize = MiGzOutputStream.DEFAULT_BLOCK_SIZE;
            final ExtendedByteArrayOutputStream b = new ExtendedByteArrayOutputStream(
                    targetBuffer.getBufferCompressData());
            try (final MiGzOutputStream mzos = new MiGzOutputStream(b, 5, blockSize)) {
                mzos.setCompressionLevel(Deflater.BEST_SPEED);
                int readCount = DUMP_BUFFER_SIZE;
                int base = 0;
                while (true) {
                    if ((base + readCount) > count) {
                        readCount = count - base;
                    }
                    mzos.write(buffer, base, readCount);
                    base += readCount;
                    if (base == count) {
                        break;
                    }
                }
            }
            count = b.size();
            buffer = targetBuffer.getBufferCompressData();
            // release input buffer
            targetBuffer.releaseInputStream();
            released = true;
        }
        if (blockInfo.isCipher()) {
            final EncryptResult er = AESEncryptionManager.encryptData(buffer, 0, count, targetBuffer.getBufferCipher());
            count = er.getLength();
            // Set the block offset for encryption
            blockInfo.setCipherOffset(er.getOffset());
            buffer = targetBuffer.getBufferCipher();
            if (!released) {
                targetBuffer.releaseInputStream();
                released = true;
            }
        }

        targetBuffer.md5Update(buffer, count);
        blockInfo.setMd5Digest(targetBuffer.md5Digest());
        blockInfo.setStreamSize(count);
        if (blockInfo.isCipher() || blockInfo.isCompress()) {
            targetBuffer.fillInputStreamWithLock(buffer, count);
        } else {
            targetBuffer.fillInputStream(buffer, count);
        }
    }

    protected void reportResult(final ExBlockInfo blockInfo, final boolean result) {
        final String msg = MessagesTemplate.dumpInfo(getEntity(), blockInfo);
        if (result) {
            this.logger.info(msg);
            this.interactive.dumpSuccess(blockInfo);
        } else {
            this.logger.warning(msg);
            this.interactive.dumpFailure(blockInfo);
            this.buffers.stop();
        }
        if (this.report != null) {
            this.report[blockInfo.getIndex()] = msg;

        }
    }

    protected abstract ManagedFcoEntityInfo getEntity();

    protected Integer waitForBuffer(final ExBlockInfo blockInfo) throws InterruptedException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - start"); //$NON-NLS-1$
        }
        Integer bufferIndex = null;
        boolean bufferAvailable = false;
        while (!bufferAvailable) {
            if (this.buffers.isRunning()) {
                for (bufferIndex = 0; bufferIndex < this.buffers.getLength(); ++bufferIndex) {
                    if (this.buffers.getBuffer(bufferIndex).getAvailable().compareAndSet(true, false)) {
                        bufferAvailable = true;
                        blockInfo.setStartTime(System.nanoTime());
                        break;
                    }
                }
                if (!bufferAvailable) {
                    Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
                }
            } else {
                bufferIndex = null;
                break;
            }
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return bufferIndex;
    }
}
