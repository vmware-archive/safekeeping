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
package com.vmware.safekeeping.core.profile.dataclass;

import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class FcoChildrenGenerationProfile {
	private ManagedFcoEntityInfo fcoEntity;

	private Integer generationId;

	private int index;

	public FcoChildrenGenerationProfile() {
	}

	/**
	 * Clone constructor
	 *
	 * @param src
	 */
	public FcoChildrenGenerationProfile(final FcoChildrenGenerationProfile src) {
		this.fcoEntity = new ManagedFcoEntityInfo(src.fcoEntity);
		this.generationId = src.generationId;
		this.index = src.index;
	}

	/**
	 * @return the fcoEntity
	 */
	public ManagedFcoEntityInfo getFcoEntity() {
		return this.fcoEntity;
	}

	/**
	 * @return the generationId
	 */
	public Integer getGenerationId() {
		return this.generationId;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @param fcoEntity the fcoEntity to set
	 */
	public void setFcoEntity(final ManagedFcoEntityInfo fcoEntity) {
		this.fcoEntity = fcoEntity;
	}

	/**
	 * @param generationId the generationId to set
	 */
	public void setGenerationId(final Integer generationId) {
		this.generationId = generationId;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(final int index) {
		this.index = index;
	}
}