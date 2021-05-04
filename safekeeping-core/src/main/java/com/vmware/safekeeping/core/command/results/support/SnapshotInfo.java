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
package com.vmware.safekeeping.core.command.results.support;

import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;

public class SnapshotInfo {
	private String name;
	private String id;
	private FcoPowerState state;
	private GregorianCalendar createTime;
	private String description;
	private OperationState result;
	private ManagedFcoEntityInfo fcoEntity;

	public GregorianCalendar getCreateTime() {
		return this.createTime;
	}

	public String getDescription() {
		return this.description;
	}

	public ManagedFcoEntityInfo getFcoEntity() {
		return this.fcoEntity;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public OperationState getResult() {
		return this.result;
	}

	public FcoPowerState getState() {
		return this.state;
	}

	public void setCreateTime(final GregorianCalendar gregorianCalendar) {
		this.createTime = gregorianCalendar;
	}

	/**
	 * @param createTime2
	 */
	public void setCreateTime(final XMLGregorianCalendar xmlGregorianCalendar) {
		this.createTime = xmlGregorianCalendar.toGregorianCalendar();
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setFcoEntity(final ManagedFcoEntityInfo fcoEntity) {
		this.fcoEntity = fcoEntity;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param failure
	 */
	public void setResult(final OperationState result) {
		this.result = result;
	}

	public void setState(final FcoPowerState state) {
		this.state = state;
	}

}