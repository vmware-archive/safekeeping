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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import com.vmware.safekeeping.core.control.info.ExBlockInfo;

public class BlockLocker {

    private static ConcurrentHashMap<String, Semaphore> dataOnWriting;
    static {
        dataOnWriting = new ConcurrentHashMap<>();
    }

    public static ConcurrentMap<String, Semaphore> getDataOnWriting() {
        return dataOnWriting;
    }

    public static boolean isBlockLocked(final ExBlockInfo blockInfo) {
        return dataOnWriting.containsKey(blockInfo.getJsonKey());

    }

    /**
     * Lock a block to avoid multiple threads rite the same data
     *
     * @param blockInfo
     * @return
     * @throws InterruptedException
     */
    public static Semaphore lockBlock(final ExBlockInfo blockInfo) throws InterruptedException {
        final String key = blockInfo.getJsonKey();
        final Semaphore semaphore = dataOnWriting.computeIfAbsent(key, k -> new Semaphore(1));
        semaphore.acquire();
        return semaphore;
    }

    public static boolean releaseBlock(final ExBlockInfo blockInfo) {
        final String key = blockInfo.getJsonKey();
        final Semaphore semaphore = dataOnWriting.get(key);
        if (semaphore == null) {
            return false;
        } else if (semaphore.hasQueuedThreads()) {
            semaphore.release();
            return true;
        } else {
            dataOnWriting.remove(key);
            semaphore.release();
            return false;
        }
    }

    protected BlockLocker() {
    }
}
