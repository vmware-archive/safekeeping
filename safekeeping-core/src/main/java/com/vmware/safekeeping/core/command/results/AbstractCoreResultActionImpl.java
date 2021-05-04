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

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.AtomicFloat;
import com.vmware.safekeeping.common.DateUtility;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.list.ResultActionsList;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;

public abstract class AbstractCoreResultActionImpl implements ICoreResultAction {
    /**
     * 
     */
    private static final long serialVersionUID = 2050759163505972453L;

    private static final Logger logger = Logger.getLogger(AbstractCoreResultActionImpl.class.getName());

    private static final String DEFAULT_FAILURE_REASON = "Check logs for more details";
    private static final String DEFAULT_SKIPS_REASON = "Skipped. Check logs for more details";
    private static final String DEFAULT_ABORT_REASON = "Aborted on user request";
    private final String resultActionId;
    protected OperationState state;
    private ManagedFcoEntityInfo fcoEntityInfo;
    private String reason;
    private long endTime;
    private long startTime;
    private GregorianCalendar startDate;
    private final GregorianCalendar creationDate;
    private GregorianCalendar endDate;
    private volatile boolean done;
    protected ICoreResultAction parent;

    private final AtomicFloat progressPercent;

    protected AbstractCoreResultActionImpl() {
        this.creationDate = new GregorianCalendar();
        this.done = false;
        this.endDate = null;
        this.startTime = 0;
        this.endTime = 0;
        this.startDate = null;
        this.state = OperationState.QUEUED;
        this.progressPercent = new AtomicFloat();
        this.resultActionId = ResultActionsList.newResultActionId(this);
    }

    /**
     * @param parent2
     */
    protected AbstractCoreResultActionImpl(final ICoreResultAction parent) {
        this();
        this.parent = parent;
        this.fcoEntityInfo = parent.getFcoEntityInfo();

    }

    protected AbstractCoreResultActionImpl(final OperationState startingState) {
        this.creationDate = new GregorianCalendar();
        this.done = false;
        this.endDate = null;
        this.startTime = 0;
        this.endTime = 0;
        this.startDate = null;
        this.state = OperationState.QUEUED;
        this.progressPercent = new AtomicFloat();
        this.resultActionId = ResultActionsList.newResultActionId(this);
        try {
            setState(startingState);
        } catch (final CoreResultActionException e) {
            logger.severe("OperationState - exception: " + e); //$NON-NLS-1$

            this.state = OperationState.FAILED;
        }
    }

    @Override
    public void aborted() throws CoreResultActionException {

        aborted(DEFAULT_ABORT_REASON);

    }

    @Override
    public void aborted(final String reason) throws CoreResultActionException {
        this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
        if (!isDone()) {
            this.state = OperationState.ABORTED;
            setReason(reason);
            setEndTime();
            this.done = true;
            if (this.parent != null) {
                this.parent.aborted(reason);
            }
        } else {
            final String msg = String.format("Action %s - Cannot be aborted", toString());
            logger.warning(msg);
            throw new CoreResultActionException(msg);
        }
    }

    @Override
    public final void done() {
        try {
            if (!this.done) {
                if (isRunning()) {
                    success();
                } else if (isQueuing()) {
                    skip();
                } else {
                    this.done = true;
                }
            } else {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Action %s - Already done" + toString()); //$NON-NLS-1$
                }
            }
        } catch (final CoreResultActionException e) {
            Utility.logWarning(logger, e);
        } finally {
            this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
        }
    }

    @Override
    public void failure() throws CoreResultActionException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        failure(DEFAULT_FAILURE_REASON);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

    @Override
    public void failure(final String reason) throws CoreResultActionException {
        failure(reason, true);
    }

    @Override
    public void failure(final String reason, final boolean effectParent) throws CoreResultActionException {
        this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
        if (isRunningOrQueuing()) {
            this.state = OperationState.FAILED;
            setReason(reason);
            setEndTime();
            this.done = true;
            if ((this.parent != null) && (!parent.isDone()) && effectParent) {
                this.parent.failure(reason, effectParent);
            }
        } else {
            final String msg = String.format("Action %s - Change to %s requires running or queue state ", toString(),
                    OperationState.FAILED.toString());
            logger.warning(msg);
            throw new CoreResultActionException(msg);
        }
    }

    @Override
    public void failure(final StringBuilder reason) throws CoreResultActionException {
        failure(reason.toString(), true);
    }

    @Override
    public void failure(final Throwable e) {
        failure(e, true);
    }

    @Override
    public void failure(final Throwable e, final boolean effectParent) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Throwable - start"); //$NON-NLS-1$
        }
        try {
            failure(e.getMessage());
        } catch (final CoreResultActionException e1) {
            if (logger.isLoggable(Level.FINE)) {
                Utility.logWarning(logger, e1);
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Throwable - end"); //$NON-NLS-1$
        }
    }

    @Override
    public void failure(final Throwable e, final boolean effectParent, final boolean localized) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Throwable, boolean - start"); //$NON-NLS-1$
        }

        try {
            final String msg = localized ? e.getLocalizedMessage() : e.getMessage();
            failure(msg, effectParent);
        } catch (final CoreResultActionException e1) {
            if (logger.isLoggable(Level.FINE)) {
                Utility.logWarning(logger, e1);
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Throwable, boolean - end"); //$NON-NLS-1$
        }
    }

    /**
     * Forced Aborted
     *
     * @param reason
     */
    protected void forceAbort(final String reason) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - start"); //$NON-NLS-1$
        }

        this.state = OperationState.ABORTED;
        setReason(reason);
        setEndTime();
        this.done = true;
        this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
    }

    /**
     * Forced Failed
     *
     * @param reason
     */
    protected void forceFail(final String reason) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - start"); //$NON-NLS-1$
        }

        this.state = OperationState.FAILED;
        setReason(reason);
        setEndTime();
        this.done = true;
        this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
    }

    /**
     * Forced Skipped
     *
     * @param reason
     */
    protected void forceSkip(final String reason) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - start"); //$NON-NLS-1$
        }

        this.state = OperationState.SKIPPED;
        setReason(reason);
        setEndTime();
        this.done = true;
        this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
    }

    @Override
    public GregorianCalendar getCreationDate() {
        return this.creationDate;
    }

    /**
     * @return
     */
    @Override
    public GregorianCalendar getEndDate() {
        return this.endDate;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public EntityType getEntityType() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final EntityType returnEntityType = this.fcoEntityInfo.getEntityType();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnEntityType;
    }

    @Override
    public ManagedFcoEntityInfo getFcoEntityInfo() {
        return this.fcoEntityInfo;
    }

    /**
     * @return
     */
    @Override
    public String getFcoToString() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.fcoEntityInfo != null) {
            if (this.fcoEntityInfo.getEntityType() == EntityType.ImprovedVirtualDisk) {
                final String returnString = this.fcoEntityInfo.toString(false);
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("<no args> - end"); //$NON-NLS-1$
                }
                return returnString;
            } else {
                final String returnString = this.fcoEntityInfo.toString(true);
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("<no args> - end"); //$NON-NLS-1$
                }
                return returnString;
            }
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return StringUtils.EMPTY;
        }

    }

    public long getOverallTime() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final long returnlong = this.endTime - this.startTime;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnlong;
    }

    public double getOverallTimeInSeconds() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final double returndouble = (this.endTime - this.startTime) / 1000000000.0F;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returndouble;
    }

    @Override
    public ICoreResultAction getParent() {
        return this.parent;
    }

    /**
     * Percentage of progress in base 10000
     *
     * @return the progressPercent in base 10000
     *
     */
    @Override
    public int getProgress() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final int returnint = this.progressPercent.intValue();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnint;
    }

    @Override
    public String getReason() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (StringUtils.isEmpty(this.reason) && (this.state != OperationState.SUCCESS)) {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return "unknow";
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return this.reason;
    }

    /**
     * @return the resultActionId
     */

    @Override
    public String getResultActionId() {
        return this.resultActionId;
    }

    /**
     * @return
     */
    public String getSpentTotalTimeUntilNowAsString() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }
        String result = String.format(Utility.LOCALE, "Time Start:%s End:%s - Elapsed Time %.0f sec",
                DateUtility.convertGregorianToString(getStartDate()), DateUtility.convertGregorianCurrentTimeToString(),
                getOverallTimeInSeconds());
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    @Override
    public GregorianCalendar getStartDate() {
        return this.startDate;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public OperationState getState() {
        return this.state;
    }

    public String getTotalTimeString() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        String result = "";

        try {
            while ((this.startDate == null) || (this.endDate == null)) {
                Thread.sleep(Utility.ONE_HUNDRED_MS);
            }
            result = String.format(Utility.LOCALE, "Time Start:%s End:%s - Elapsed Time %.0f sec",
                    DateUtility.convertGregorianToString(getStartDate()),
                    DateUtility.convertGregorianToString(getEndDate()), getOverallTimeInSeconds());

        } catch (final Exception e) {
            Utility.logWarning(logger, e);
            result = e.getMessage();
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * @param result
     */
    public void importResult(final ICoreResultAction result) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultActionImpl - start"); //$NON-NLS-1$
        }

        this.state = result.getState();
        setReason(result.getReason());
        this.endDate = result.getEndDate();
        this.endTime = result.getEndTime();
        this.done = result.isDone();

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultActionImpl - end"); //$NON-NLS-1$
        }
    }

    public boolean isAbortedOrFailed() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.done) {
            final boolean returnboolean = (getState() == OperationState.FAILED)
                    || (getState() == OperationState.ABORTED);
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return false;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public boolean isFails() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.done) {
            final boolean returnboolean = getState() == OperationState.FAILED;
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
    }

    @Override
    public boolean isQueuing() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (!this.done) {
            final boolean returnboolean = getState() == OperationState.QUEUED;
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
    }

    @Override
    public boolean isRunning() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.done) {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
        final boolean returnboolean = getState() == OperationState.STARTED;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnboolean;
    }

    public boolean isRunningOrQueuing() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (!this.done) {
            final boolean returnboolean = (getState() == OperationState.STARTED)
                    || (getState() == OperationState.QUEUED);
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
    }

    @Override
    public boolean isSkipped() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.done) {
            final boolean returnboolean = getState() == OperationState.SKIPPED;
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
    }

    @Override
    public boolean isSuccessful() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.done) {
            final boolean returnboolean = getState() == OperationState.SUCCESS;
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
    }

    /**
     * @return
     */
    public boolean isSuccessfulOrSkipped() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.done) {
            final boolean returnboolean = (getState() == OperationState.SUCCESS)
                    || (getState() == OperationState.SKIPPED);
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnboolean;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return false;
        }
    }

    @Override
    public final float progressIncrease(final float increase) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("float - start"); //$NON-NLS-1$
        }

        if (this.progressPercent.addAndGet(increase) > (Utility.ONE_HUNDRED_PER_CENT)) {
            final String msg = String.format(Utility.LOCALE, "Over 100 %2.2f", this.progressPercent.floatValue());
            logger.warning(msg);
            this.progressPercent.set(Utility.ALMOST_ONE_HUNDRED_PER_CENT);
        }
        final float returnfloat = this.progressPercent.get();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("float - end"); //$NON-NLS-1$
        }
        return returnfloat;
    }

    @Override
    public void resetTimer() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        this.startTime = System.nanoTime();
        this.startDate = new GregorianCalendar();

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

    protected void setEndTime() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        this.endTime = System.nanoTime();
        this.endDate = new GregorianCalendar();

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

    @Override
    public void setFcoEntityInfo(final ManagedFcoEntityInfo fco) {
        this.fcoEntityInfo = fco;
    }

    public void setParent(final ICoreResultAction parent) {
        this.parent = parent;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public void setReason(final String format, final Object... arguments) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, Object - start"); //$NON-NLS-1$
        }

        this.reason = String.format(format, arguments);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, Object - end"); //$NON-NLS-1$
        }
    }

    public void setReason(final Throwable e) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Throwable - start"); //$NON-NLS-1$
        }

        this.reason = e.getLocalizedMessage();

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Throwable - end"); //$NON-NLS-1$
        }
    }

    private void setState(final OperationState state) throws CoreResultActionException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("OperationState - start"); //$NON-NLS-1$
        }

        switch (state) {
        case ABORTED:
            aborted();
            this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
            break;
        case FAILED:
            failure();
            this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
            break;
        case QUEUED:
            this.state = OperationState.QUEUED;
            break;
        case STARTED:
            start();
            break;
        case SKIPPED:
            skip();
            this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
            break;
        case SUCCESS:
            success();
            this.progressPercent.set(Utility.ONE_HUNDRED_PER_CENT);
            break;
        default:
            break;

        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("OperationState - end"); //$NON-NLS-1$
        }
    }

    @Override
    public void skip() throws CoreResultActionException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        skip(DEFAULT_SKIPS_REASON);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

    @Override
    public void skip(final String reason) throws CoreResultActionException {
        if (isRunningOrQueuing()) {
            setEndTime();
            setReason(reason);
            if (isQueuing()) {
                this.startTime = this.endTime;
                this.startDate = this.endDate;
            }
            this.state = OperationState.SKIPPED;
            this.done = true;
        } else {
            final String msg = String.format("Action %s - Change to %s requires running or queue state ", toString(),
                    OperationState.FAILED.toString());
            logger.warning(msg);
            throw new CoreResultActionException(msg);
        }
    }

    @Override
    public void start() throws CoreResultActionException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (isQueuing()) {
            this.startTime = System.nanoTime();
            this.startDate = new GregorianCalendar();
            this.state = OperationState.STARTED;
        } else {
            final String msg = String.format("Action %s - Change to %s requires running or queue state ", toString(),
                    OperationState.FAILED.toString());
            logger.warning(msg);
            throw new CoreResultActionException(msg);
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

    @Override
    public void success() throws CoreResultActionException {
        if (isRunning()) {
            this.state = OperationState.SUCCESS;
            setEndTime();
            this.done = true;
        } else {
            final String msg = String.format("Action %s - Change to %s requires running or queue state ", toString(),
                    OperationState.FAILED.toString());
            logger.warning(msg);
            throw new CoreResultActionException(msg);
        }
    }

    @Override
    public String toString() {

        return String.format("Done %b Status:%s  -Reason %s", isDone(), this.state.toString(), this.reason);

    }

}
