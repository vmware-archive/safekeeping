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
package com.vmware.safekeeping.core.command.results.support;

import java.util.Calendar;
import java.util.Date;

import com.vmware.safekeeping.core.type.enums.EntityType;

public class CoreGenerationStatusInfo {

	private final int genId;

	final private EntityType entityType;
	final private boolean generationSucceeded;
	final private Date timeStamp;

	CoreGenerationStatusInfo(final int genId, final boolean generationSucceeded, final EntityType entityType,
			final Long timeStampMs) {
		this.genId = genId;
		this.entityType = entityType;
		this.generationSucceeded = generationSucceeded;
		final Calendar cal = Calendar.getInstance();
		final Long ms = timeStampMs;// Long.parseLong(timeStampMs);
		cal.setTimeInMillis(ms);
		this.timeStamp = cal.getTime();
	}

	/**
	 * @return the entityType
	 */
	public EntityType getEntityType() {
		return this.entityType;
	}

	/**
	 * @return the genId
	 */
	public int getGenId() {
		return this.genId;
	}

	/**
	 * @return the timeStampMs
	 */
	public Date getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * @return the timeStampAsString
	 */
//    public String getTimeStampAsString() {
//	return this.timeStampAsString;
//    }

	/**
	 * @return the generationSucceeded
	 */
	public boolean isGenerationSucceeded() {
		return this.generationSucceeded;
	}

}