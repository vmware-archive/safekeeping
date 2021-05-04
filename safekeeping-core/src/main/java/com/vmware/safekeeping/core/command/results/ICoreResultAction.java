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
package com.vmware.safekeeping.core.command.results;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.GregorianCalendar;
import java.util.List;

import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;

public interface ICoreResultAction extends Serializable {

    static StatisticResult getResultStatistic(final AbstractCollection<? extends ICoreResultAction> resultActions) {
        final StatisticResult statistic = new StatisticResult();
        for (final ICoreResultAction action : resultActions) {

            statistic.countResult(action.getEntityType(), action.getState());

        }

        return statistic;
    }

    static StatisticResult getResultStatistic(final List<? extends ICoreResultAction> resultActions) {
        final StatisticResult statistic = new StatisticResult();
        for (final ICoreResultAction action : resultActions) {

            statistic.countResult(action.getEntityType(), action.getState());

        }

        return statistic;
    }

    /**
     * @throws CoreResultActionException
     */
    void aborted() throws CoreResultActionException;

    void aborted(String reason) throws CoreResultActionException;

    /**
     *
     */
    void done();

    /**
     * @throws CoreResultActionException
     *
     */
    void failure() throws CoreResultActionException;

    void failure(String reason) throws CoreResultActionException;

    void failure(String reason, boolean effectParent) throws CoreResultActionException;

    void failure(StringBuilder reason) throws CoreResultActionException;

    /**
     * @param reason
     * @throws CoreResultActionException
     */
    void failure(Throwable reason) throws CoreResultActionException;

    void failure(Throwable e, boolean effectParent);

    /**
     * @param e
     * @param locai
     */
    void failure(Throwable e, boolean effectParent, boolean localized);

    /**
     * @return
     */
    GregorianCalendar getCreationDate();

    /**
     * @return
     */
    GregorianCalendar getEndDate();

    /**
     * @return
     */
    long getEndTime();

    EntityType getEntityType();

    ManagedFcoEntityInfo getFcoEntityInfo();

    String getFcoToString();

    /**
     * @return
     */
    ICoreResultAction getParent();

    /**
     * @return the progressPercent
     */
    int getProgress();

    /**
     * @return
     */
    String getReason();

    /**
     * Return the unique ResultAction ID
     *
     * @return
     */
    String getResultActionId();

    /**
     * @return
     */
    GregorianCalendar getStartDate();

    /**
     * @return
     */
    long getStartTime();

    OperationState getState();

    /**
     * @return
     */
    boolean isDone();

    boolean isFails();

    /**
     * @return
     */
    boolean isQueuing();

    boolean isRunning();

    boolean isSkipped();

    boolean isSuccessful();

    float progressIncrease(final float increase);

    /**
     *
     */
    void resetTimer();

    void setFcoEntityInfo(final ManagedFcoEntityInfo fco);

    void skip() throws CoreResultActionException;

    /**
     * @param reason
     * @throws CoreResultActionException
     */
    void skip(String reason) throws CoreResultActionException;

    /**
     * @throws CoreResultActionException
     */
    void start() throws CoreResultActionException;

    void success() throws CoreResultActionException;
}
