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
package com.vmware.safekeeping.core.command.results.miscellanea;

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.command.results.support.OperationState;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractCoreResultList {
    private static final Logger logger = Logger.getLogger(AbstractCoreResultList.class.getName());

    /**
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    private boolean done;

    private String reason;

    private final GregorianCalendar startDate;

    private GregorianCalendar endDate;

    private long endTime;

    private final long startTime;

    private OperationState state;

    protected AbstractCoreResultList() {
        this.done = false;
        this.endDate = null;
        this.endTime = 0;
        this.startTime = System.nanoTime();
        this.startDate = new GregorianCalendar();
        this.state = OperationState.STARTED;

    }

    public final void done() {

        if (!this.done) {
            this.done = true;
            if (this.state == OperationState.STARTED) {
                this.state = OperationState.SUCCESS;
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                final String msg = "Action ".concat(toString()).concat(" - Already done");
                logger.fine(msg);
            }
        }

    }

    public void failure(final String reason) {

        this.state = OperationState.FAILED;
        this.reason = reason;
        setEndTime();
        done();
    }

    public void failure(final Throwable e) {
        failure(e.getMessage());

    }

    public void failure(final Throwable e, final boolean localized) {
        if (localized) {
            failure(e.getLocalizedMessage());
        } else {
            failure(e.getMessage());
        }

    }

    /**
     * @return the endDate
     */
    public GregorianCalendar getEndDate() {
        return this.endDate;
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return this.endTime;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * @return the startDate
     */
    public GregorianCalendar getStartDate() {
        return this.startDate;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * @return the state
     */
    public OperationState getState() {
        return this.state;
    }

    /**
     * @return the done
     */
    public boolean isDone() {
        return this.done;
    }

    protected void setEndTime() {
        this.endTime = System.nanoTime();
        this.endDate = new GregorianCalendar();
    }

    public abstract int size();

}
