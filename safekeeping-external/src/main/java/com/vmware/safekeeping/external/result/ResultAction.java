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

import java.util.Date;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.external.command.support.Task;

public class ResultAction implements Result {

    public static void convert(final ICoreResultAction src, final ResultAction dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        dst.setTask(new Task(src));
        dst.setState(src.getState());
        dst.setFcoEntityInfo(src.getFcoEntityInfo());
        dst.setReason(src.getReason());
        dst.setDone(src.isDone());

        dst.setEndTime(src.getEndTime());
        dst.setStartTime(src.getStartTime());
        if (src.getStartDate() != null) {
            dst.setStartDate(src.getStartDate().getTime());
        }
        if (src.getCreationDate() != null) {
            dst.setCreationDate(src.getCreationDate().getTime());
        }
        if (src.getEndDate() != null) {
            dst.setEndDate(src.getEndDate().getTime());
        }
        dst.setProgress(src.getProgress());

        if (src.getParent() != null) {
            dst.setParent(new Task(src.getParent()));
        }
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultAction.convert(src, this);
    }

    private Task task;
    private OperationState state;
    private ManagedFcoEntityInfo fcoEntityInfo;
    private String reason;
    private long endTime;
    private long startTime;
    private Date startDate;
    private Date creationDate;
    private Date endDate;
    private boolean done;
    private Task parent;
    private int progress;

    public ResultAction() {
        // nothing to do
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return this.endTime;
    }

    /**
     * @return the fcoEntityInfo
     */
    public ManagedFcoEntityInfo getFcoEntityInfo() {
        return this.fcoEntityInfo;
    }

    /**
     * @return the parent
     */
    public Task getParent() {
        return this.parent;
    }

    /**
     * @return the progress
     */
    public int getProgress() {
        return this.progress;
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
    public Date getStartDate() {
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
     * @return the id
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * @return the done
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @param done the done to set
     */
    public void setDone(final boolean done) {
        this.done = done;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

    /**
     * @param fcoEntityInfo the fcoEntityInfo to set
     */
    public void setFcoEntityInfo(final ManagedFcoEntityInfo fcoEntityInfo) {
        this.fcoEntityInfo = fcoEntityInfo;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(final Task parent) {
        this.parent = parent;
    }

    /**
     * @param progress the progress to set
     */
    public void setProgress(final int progress) {
        this.progress = progress;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    /**
     * @param state the state to set
     */
    public void setState(final OperationState state) {
        this.state = state;
    }

    /**
     * @param id the id to set
     */
    public void setTask(final Task id) {
        this.task = id;
    }
}
