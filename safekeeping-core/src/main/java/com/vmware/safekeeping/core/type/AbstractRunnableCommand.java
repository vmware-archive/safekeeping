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
package com.vmware.safekeeping.core.type;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractRunnableCommand implements Runnable {

    /* For generating thread ID */
    private static long threadSeqNumber;

    private static synchronized long nextCommandID() {
        return ++threadSeqNumber;
    }

    private String name;

    protected ICoreResultAction ra;

    /*
     * Thread ID
     */
    private long tid;

    protected AbstractRunnableCommand(final ICoreResultAction ra) {
        this.ra = ra;
        this.tid = nextCommandID();
    }

    /**
     * Returns the identifier of this Thread. The thread ID is a positive
     * <tt>long</tt> number generated when this thread was created. The thread ID is
     * unique and remains unchanged during its lifetime. When a thread is
     * terminated, this thread ID may be reused.
     *
     * @return this thread's ID.
     * @since 1.5
     */
    public long getId() {
        return this.tid;
    }

    public String getName() {
        return this.name;
    }

    public ICoreResultAction getResultAction() {
        return this.ra;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
