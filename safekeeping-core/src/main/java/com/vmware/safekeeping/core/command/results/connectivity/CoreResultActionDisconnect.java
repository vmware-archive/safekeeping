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
package com.vmware.safekeeping.core.command.results.connectivity;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;

public class CoreResultActionDisconnect extends AbstractCoreResultActionConnectDisconnect {

	/**
	 * 
	 */
	private static final long serialVersionUID = 703199940834135783L;

	private final List<CoreResultActionDisconnectVcenter> subActionDisconnectVCenters;

	private CoreResultActionDisconnectSso subActionDisconnectSso;

	/**
	 *
	 */
	public CoreResultActionDisconnect() {
		this.subActionDisconnectVCenters = new LinkedList<>();
		setConnected(true);
	}

	public CoreResultActionDisconnect(CoreResultActionDisconnectSso subActionDisconnectSso) {
		this();
		this.subActionDisconnectSso=subActionDisconnectSso;
	}

	/**
	 * @return the subActionConnectSso
	 */
	public CoreResultActionDisconnectSso getSubActionDisconnectSso() {
		return this.subActionDisconnectSso;
	}

	public List<CoreResultActionDisconnectVcenter> getSubActionDisconnectVCenters() {
		return this.subActionDisconnectVCenters;
	}

	/**
	 * @param subActionConnectSso the subActionConnectSso to set
	 */
	public void setSubActionDisconnectSso(final CoreResultActionDisconnectSso subActionConnectSso) {
		this.subActionDisconnectSso = subActionConnectSso;
	}

}