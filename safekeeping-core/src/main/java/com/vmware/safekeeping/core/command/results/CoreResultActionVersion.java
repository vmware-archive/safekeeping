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
package com.vmware.safekeeping.core.command.results;

import com.vmware.safekeeping.common.BiosUuid;
import com.vmware.safekeeping.core.control.SafekeepingVersion;

public class CoreResultActionVersion extends AbstractCoreResultActionImpl {

	/**
     * 
     */
    private static final long serialVersionUID = 7410554372019797169L;
    private SafekeepingVersion version;
	private BiosUuid serverInfo;
	private String javaRuntime;

	public String getJavaRuntime() {
		return this.javaRuntime;
	}

	/**
	 * @return the serverInfo
	 */
	public BiosUuid getServerInfo() {
		return this.serverInfo;
	}

	/**
	 * @return the version
	 */
	public SafekeepingVersion getVersion() {
		return this.version;
	}

	public void setJavaRuntime(final String javaVersion) {
		this.javaRuntime = javaVersion;
	}

	/**
	 * @param serverInfo the serverInfo to set
	 */
	public void setServerInfo(final BiosUuid serverInfo) {
		this.serverInfo = serverInfo;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(final SafekeepingVersion version) {
		this.version = version;
	}

}