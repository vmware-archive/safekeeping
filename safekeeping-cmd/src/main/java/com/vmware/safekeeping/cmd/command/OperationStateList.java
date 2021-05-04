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
package com.vmware.safekeeping.cmd.command;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.support.OperationState;

public class OperationStateList {
	private final List<OperationState> list;
	private String reason;
	private boolean quit = false;

	public OperationStateList() {
		this.list = new LinkedList<>();
	}

	public OperationStateList(final AbstractCoreResultActionImpl operationState) {
		this.list = new LinkedList<>();
		this.list.add(operationState.getState());
		this.reason = operationState.getReason();
	}

	/**
	 * @param done
	 */
	public OperationStateList(final OperationState state) {
		this.list = new LinkedList<>();
		this.list.add(state);
		this.reason = null;
	}

	/**
	 * @param failed
	 * @param showWarning
	 */
	public OperationStateList(final OperationState state, final String reason) {
		this.list = new LinkedList<>();
		this.list.add(state);
		this.reason = reason;
	}

	public OperationState add(final AbstractCoreResultActionImpl operationState) {
		this.list.add(operationState.getState());
		this.reason = operationState.getReason();
		return operationState.getState();
	}

	public OperationState add(final OperationState state, final String reason) {
		this.list.add(state);
		this.reason = reason;
		return state;
	}

	/**
	 * @param commandFlow
	 * @return
	 */
	public boolean addAll(final OperationStateList operationStateList) {
		if (StringUtils.isNotEmpty(operationStateList.getReason())) {
			this.reason = operationStateList.getReason();
		}
		if (operationStateList.isQuitRequested()) {
			this.quit = true;
		}
		return this.list.addAll(operationStateList.list);
	}

	public List<OperationState> getList() {
		return this.list;
	}

	public String getReason() {
		return this.reason;
	}

	public boolean isQuitRequested() {
		return this.quit;
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper sequence).
	 *
	 * @return a list iterator over the elements in this list (in proper sequence)
	 */
	public ListIterator<OperationState> listIterator() {
		return this.list.listIterator();
	}

	public void quitRequested() {
		this.quit = true;
	}

}