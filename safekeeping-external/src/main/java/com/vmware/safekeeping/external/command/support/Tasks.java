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
package com.vmware.safekeeping.external.command.support;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.external.type.ResultThread;

/**
 * @author mdaneri
 *
 */
public class Tasks {
	public static final String INTERNAL_ERROR_MESSAGE = "Internal Error Check the server log";
	public static final String NO_REPOSITORY_TARGET_ERROR_MESSAGE = "No Repository Target available";
	public static final String NO_VALID_CATALOG_ENTITY = "No valid catalogs entity";
	public static final String REPOSITORY_NOT_ACTIVE = "Repository is not active";
	public static final String NO_VCENTER_CONNECTION = "No vCenter connection";
	public static final String NO_VALID_FCO_TARGETS = "No valid Fco targets";
	public static final String UNSUPPORTED_ENTITY_TYPE = "Unsupported entity Type";
	public static final String GLOBAL_PROFILE_ERROR = "Errors accessing the Global Profile file";
	private List<Task> taskList;
	private String reason;

	private OperationState state;

	public Tasks() {
		this.taskList = new LinkedList<>();
		this.state = OperationState.STARTED;
	}

	/**
	 * @param resultThread
	 * @return
	 */
	public Task addResultThread(final ResultThread resultThread) {
		final Task task = new Task(resultThread);
		this.taskList.add(task);
		return task;
	}

	/**
	 * @param e
	 */
	public void fails(final Exception e) {
		this.state = OperationState.FAILED;
		this.reason = e.getMessage();
	}

	public void fails(String reason) {
		this.state = OperationState.FAILED;
		this.reason = reason;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return this.reason;
	}

	/**
	 * @return the state
	 */
	public OperationState getState() {
		return this.state;
	}

	/**
	 * @return the taskList
	 */
	public List<Task> getTaskList() {
		return this.taskList;
	}

	public void globalProfileFailure() {
		this.state = OperationState.FAILED;
		this.reason = Tasks.GLOBAL_PROFILE_ERROR;
	}

	/**
	 *
	 */
	public void internalFailure() {
		this.state = OperationState.FAILED;
		this.reason = Tasks.INTERNAL_ERROR_MESSAGE;

	}

	public void noRepositoryTargetFailure() {
		this.state = OperationState.FAILED;
		this.reason = Tasks.NO_REPOSITORY_TARGET_ERROR_MESSAGE;
	}

	public void noVcenterConnectionFailure() {
		this.state = OperationState.FAILED;
		this.reason = Tasks.NO_VCENTER_CONNECTION;
	}

	public void repositoryNotActiveFailure() {
		this.state = OperationState.FAILED;
		this.reason = Tasks.REPOSITORY_NOT_ACTIVE;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(final String reason) {
		this.reason = reason;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(final OperationState state) {
		if ((state != OperationState.FAILED) && (state != OperationState.ABORTED)) {
			this.state = state;
		}
	}

	/**
	 * @param taskList the taskList to set
	 */
	public void setTaskList(final List<Task> taskList) {
		this.taskList = taskList;
	}

	public void skipNoCatalogEntity() {
		this.state = OperationState.SKIPPED;
		this.reason = Tasks.NO_VALID_CATALOG_ENTITY;
	}

	public void skipNoValidFcoTargets() {
		this.state = OperationState.SKIPPED;
		this.reason = Tasks.NO_VALID_FCO_TARGETS;
	}

	public void unsupportedTypeFailure(EntityType entityType) {
		this.state = OperationState.FAILED;
		this.reason = Tasks.UNSUPPORTED_ENTITY_TYPE + " " + entityType.toString();
	}

}
