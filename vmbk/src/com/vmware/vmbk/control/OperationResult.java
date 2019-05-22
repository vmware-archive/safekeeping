/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.control;

import java.util.Date;
import java.util.LinkedList;

import com.vmware.vmbk.type.FirstClassObject;

public class OperationResult {
    public enum Result {
	aborted, dryruns, fails, skips, success
    }

    /**
     * @return
     */
    public static OperationResult fails(final FirstClassObject fco, final int generationId) {
	final OperationResult result = new OperationResult(fco, generationId);
	result.fails();
	return result;
    }

    private FirstClassObject fco;
    private Result operation;
    private Integer generationId;
    private long endTime;
    private final long startTime;
    private final Date startDate;
    private Date endDate;
    private final LinkedList<OperationResult> subOperations;

    public OperationResult() {
	this.startTime = System.nanoTime();
	this.startDate = new Date();
	this.fco = null;
	this.generationId = null;
	this.operation = Result.fails;
	this.subOperations = new LinkedList<>();
    }

    public OperationResult(final FirstClassObject fco) {
	this.startTime = System.nanoTime();
	this.startDate = new Date();
	this.operation = Result.fails;
	this.fco = fco;
	this.generationId = null;
	this.subOperations = new LinkedList<>();
    }

    public OperationResult(final FirstClassObject fco, final int generationId) {
	this.startTime = System.nanoTime();
	this.startDate = new Date();
	this.operation = Result.fails;
	this.fco = fco;
	this.generationId = generationId;
	this.subOperations = new LinkedList<>();
    }

    public OperationResult(final int generationId) {
	this.startTime = System.nanoTime();
	this.startDate = new Date();
	this.fco = null;
	this.generationId = generationId;
	this.operation = Result.fails;
	this.subOperations = new LinkedList<>();
    }

    public void dryruns() {
	this.operation = Result.dryruns;
	this.endTime = System.nanoTime();
	this.endDate = new Date();
    }

    public void fails() {
	this.operation = Result.fails;
	this.endTime = System.nanoTime();
	this.endDate = new Date();
    }

    public long getEndTime() {
	return this.endTime;
    }

    public FirstClassObject getFco() {
	return this.fco;
    }

    public int getGenerationId() {
	return this.generationId;
    }

    public long getOverallTime() {
	return this.endTime - this.startTime;
    }

    public double getOverallTimeInSeconds() {
	return (this.endTime - this.startTime) / 1000000000.0;
    }

    public Result getResult() {
	if (this.subOperations.size() == 0) {
	    return this.operation;
	} else {
	    Result result = Result.success;
	    for (final OperationResult op : this.subOperations) {

		switch (result) {
		case aborted:
		    result = Result.aborted;
		    break;
		case dryruns:
		    result = Result.dryruns;
		    break;
		case fails:
		    if (op.getResult() == Result.aborted) {
			result = Result.aborted;
		    } else {
			result = Result.fails;
		    }
		    break;
		case skips:
		    if (op.getResult() == Result.aborted) {
			result = Result.aborted;
		    } else if (op.getResult() == Result.fails) {
			result = Result.fails;
		    } else {
			result = Result.skips;
		    }
		    break;

		case success:
		    if (op.getResult() == Result.success) {
			result = Result.success;
		    }
		    break;
		default:
		    break;

		}

	    }
	    if (this.endDate == null) {
		this.endTime = System.nanoTime();
		this.endDate = new Date();
	    }
	    return result;
	}
    }

    public long getStartTime() {
	return this.startTime;
    }

    public LinkedList<OperationResult> getSubOperations() {
	return this.subOperations;
    }

    /**
     * @return
     */
    public String getTotalTimeString() {
	String result = "";

	try {
	    result = String.format("Time Start:%tF %<tT End:%tF %<tT - Elapsed Time %.0f sec\n", this.startDate,
		    this.endDate, getOverallTimeInSeconds());
	} catch (final Exception e) {
	    result = e.getMessage();
	}
	return result;
    }

    public boolean isFails() {
	return getResult() == Result.fails;
    }

    public void setFco(final FirstClassObject fco) {
	this.fco = fco;
    }

    public void setGenerationId(final int generationId) {
	this.generationId = generationId;
    }

    public void skip() {
	this.operation = Result.skips;
	this.endTime = System.nanoTime();
	this.endDate = new Date();
    }

    public void success() {
	this.operation = Result.success;
	this.endTime = System.nanoTime();
	this.endDate = new Date();
    }

    @Override
    public String toString() {
	if (this.fco != null) {
	    return this.getResult().toString() + " " + this.fco.toString();
	} else {
	    return this.getResult().toString();
	}

    }

}
