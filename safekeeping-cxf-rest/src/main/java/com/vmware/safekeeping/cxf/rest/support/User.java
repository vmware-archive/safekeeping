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
package com.vmware.safekeeping.cxf.rest.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.soap.ConnectionManager;

public class User {
	private final ConnectionManager connection;

	private long lastOperation;
	private final Map<String, ITarget> targets;

	private String token;

	/**
	 * @param token 
	 * @param connectionManager
	 */
	public User(final ConnectionManager connection, String token) {
		this.targets = new ConcurrentHashMap<>();
		this.connection = connection;
		this.setToken(token);
	}

	/**
	 * Close any user connection
	 */
	public void close() {
		getConnection().close();

	}

	public boolean doesTargetListInclude(final String name) {
		return this.targets.containsKey(name);
	}

	/**
	 * @return the connection
	 */
	public ConnectionManager getConnection() {
		return this.connection;
	}

	/**
	 * @return the lastOperation
	 */
	public long getLastOperation() {
		return this.lastOperation;
	}

	public ITarget getTargetByName(final String name) {
		if (StringUtils.isEmpty(name)) {
			return this.targets.entrySet().iterator().next().getValue();
		} else {
			return this.targets.get(name);
		}
	}

	/**
	 * @return the targets
	 */
	public Map<String, ITarget> getTargets() {
		return this.targets;
	}

	/**
	 * @param lastOperation the lastOperation to set
	 */
	public void setLastOperation(final long lastOperation) {
		this.lastOperation = lastOperation;
	}

	public String getToken() {
	    return token;
	}

	public void setToken(String token) {
	    this.token = token;
	}
}