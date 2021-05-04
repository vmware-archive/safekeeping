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
package com.vmware.safekeeping.external.type;

import java.util.Date;

import com.vmware.safekeeping.core.command.results.support.CoreGenerationStatusInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;

public abstract class GenerationStatusInfo {

	private int genId;

	private EntityType entityType;

	private boolean generationSucceeded;

	private Date timeStamp;

	public GenerationStatusInfo() {
	}

	/**
	 * @param gdi
	 */
	public GenerationStatusInfo(final CoreGenerationStatusInfo gdi) {
		this.genId = gdi.getGenId();
		this.entityType = gdi.getEntityType();
		this.generationSucceeded = gdi.isGenerationSucceeded();
		this.timeStamp = gdi.getTimeStamp();
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
	 * @return the generationSucceeded
	 */
	public boolean isGenerationSucceeded() {
		return this.generationSucceeded;
	}

	/**
	 * @param entityType the entityType to set
	 */
	public void setEntityType(final EntityType entityType) {
		this.entityType = entityType;
	}

	/**
	 * @param generationSucceeded the generationSucceeded to set
	 */
	public void setGenerationSucceeded(final boolean generationSucceeded) {
		this.generationSucceeded = generationSucceeded;
	}

	/**
	 * @param genId the genId to set
	 */
	public void setGenId(final int genId) {
		this.genId = genId;
	}

	/**
	 * @return the timeStampAsString
	 */
//    public String getTimeStampAsString() {
//	return this.timeStampAsString;
//    }

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(final Date timeStamp) {
		this.timeStamp = timeStamp;
	}

}