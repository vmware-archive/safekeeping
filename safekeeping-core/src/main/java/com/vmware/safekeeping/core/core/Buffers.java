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

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vmware.safekeeping.core.control.MessageDigestAlgoritmhs;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class Buffers {
    private final int length;

    private final TargetBuffer[] buffer;
    private final Semaphore semaphore;
    private final ExecutorService executor;
    private final AtomicBoolean running;

    private final ITargetOperation target;

    /**
     * @param target
     * @param readOnly
     * @param maxBlockSizeInBytes
     * @param threadPool
     * @throws NoSuchAlgorithmException
     */
    public Buffers(final ITargetOperation target, final int length, final int maxBlockSizeInBytes,
            final ManagedFcoEntityInfo managedFcoEntityInfo) throws NoSuchAlgorithmException {
        this.length = length;
        this.target = target;
        MessageDigestAlgoritmhs digestAlgorithm = CoreGlobalSettings.getMessageDigestAlgorithm();
        /**
         * set semaphore for 1 single VDDK write operation at the time
         */
        this.semaphore = new Semaphore(1);
        this.buffer = new TargetBuffer[length];
        for (int i = 0; i < length; i++) {
            this.buffer[i] = new TargetBuffer(maxBlockSizeInBytes, managedFcoEntityInfo, digestAlgorithm);
        }
        this.executor = Executors.newCachedThreadPool();
        this.running = new AtomicBoolean(false);
    }

    public void executeSubTask(final Runnable runnable) {
        this.executor.execute(runnable);
    }

    public TargetBuffer getBuffer(final Integer bufferIndex) {
        return this.buffer[bufferIndex];
    }

    /**
     * @return the length
     */
    public int getLength() {
        return this.length;
    }

    public Semaphore getSemaphore() {
        return this.semaphore;
    }

    public ITargetOperation getTarget() {
        return this.target;
    }

    public boolean isRunning() {
        return this.running.get();
    }

    public void start() {
        this.running.set(true);

    }

    void stop() {
        this.running.set(false);
    }

    public void waitSubTasks() throws InterruptedException {
        // wait for subtask to finish
        this.executor.shutdown();
        this.executor.awaitTermination(1, TimeUnit.MINUTES);
    }
}
