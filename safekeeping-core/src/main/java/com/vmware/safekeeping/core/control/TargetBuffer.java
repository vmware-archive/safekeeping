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
package com.vmware.safekeeping.core.control;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class TargetBuffer {
    /**
     * Logger for this class
     */
    private static final float BUFFER_SIZE_MULTIPLICATOR = 1.1F;

    private final byte[] bufferCompressData;
    private final byte[] bufferCipher;
    private final byte[] inputBuffer;
    private final byte[] outputBuffer;
    private final AtomicBoolean available;
    private final MessageDigest md5;
    private final MessageDigest sha;
    private final ManagedFcoEntityInfo entityInfo;
    private final Semaphore semaphore;
    private final int bufferSize;
    private ByteArrayInputStream inputStream;

    private final byte[] finalBuffer;

    public TargetBuffer(final int bufferSize, final ManagedFcoEntityInfo entityInfo, MessageDigestAlgoritmhs algorithm)
            throws NoSuchAlgorithmException {
        this.bufferSize = bufferSize;
        this.bufferCompressData = new byte[(int) (bufferSize * BUFFER_SIZE_MULTIPLICATOR)];
        this.bufferCipher = new byte[(int) (bufferSize * BUFFER_SIZE_MULTIPLICATOR)];
        this.inputBuffer = new byte[(int) (bufferSize * BUFFER_SIZE_MULTIPLICATOR)];
        this.outputBuffer = new byte[(int) (bufferSize * BUFFER_SIZE_MULTIPLICATOR)];
        this.finalBuffer = new byte[(int) (bufferSize * BUFFER_SIZE_MULTIPLICATOR)];
        this.md5 = MessageDigest.getInstance(MessageDigestAlgoritmhs.MD5.toString());
        this.sha = MessageDigest.getInstance(algorithm.toString());
        this.available = new AtomicBoolean(true);
        this.entityInfo = entityInfo;
        this.semaphore = new Semaphore(1);
    }

    public void fillInputStream(final byte[] buffer, final int count) {
        System.arraycopy(buffer, 0, this.finalBuffer, 0, count);
        this.inputStream = new ByteArrayInputStream(this.finalBuffer, 0, count);
    }

    public void fillInputStreamWithLock(final byte[] buffer, final int count) throws InterruptedException {
        this.semaphore.acquire();
        System.arraycopy(buffer, 0, this.finalBuffer, 0, count);
        this.inputStream = new ByteArrayInputStream(this.finalBuffer, 0, count);
    }

    public AtomicBoolean getAvailable() {
        return this.available;
    }

    public byte[] getBufferCipher() {
        return this.bufferCipher;
    }

    public byte[] getBufferCompressData() {
        return this.bufferCompressData;
    }

    public ManagedFcoEntityInfo getEntityInfo() {
        return this.entityInfo;
    }

    public byte[] getInputBuffer() {
        return this.inputBuffer;
    }

    public ByteArrayInputStream getInputStream() {
        return this.inputStream;
    }

    public long getInputStreamSize() {
        if (this.inputStream != null) {
            return this.inputStream.available();
        } else {
            return 0;
        }
    }

    public byte[] getOutputBuffer() {
        return this.outputBuffer;
    }

    /**
     * @return
     */
    public byte[] md5Digest() {
        return this.md5.digest();
    }

    public void md5Update(final byte[] buffer, final int count) {
        this.md5.reset();
        this.md5.update(buffer, 0, count);
    }

    public void releaseInputStream() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
            this.inputStream = null;
        }
        this.semaphore.release();

    }

    public byte[] shaDigest() {
        return this.sha.digest();

    }

    public void shaUpdate(final byte[] buffer, final int offset, final int count) {
        this.sha.reset();
        this.sha.update(buffer, offset, count);

    }

    public int getBufferSize() {
        return bufferSize;
    }
}
