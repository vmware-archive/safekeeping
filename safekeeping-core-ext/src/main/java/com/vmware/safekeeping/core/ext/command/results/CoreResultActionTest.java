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
package com.vmware.safekeeping.core.ext.command.results;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionWithSubOperations;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.util.TestDiskInfo;

public class CoreResultActionTest extends AbstractCoreResultActionWithSubOperations implements ICoreResultAction {

	/**
     * 
     */
    private static final long serialVersionUID = -2609960252664821321L;
    private String dumpDDBs;
	private TestDiskInfo info;
	private StringBuilder dumpDataContent;

	/**
	 * @return the dumpDataContent
	 */
	public StringBuilder getDumpDataContent() {
		return this.dumpDataContent;
	}

	/**
	 * @return the dumpDDBs
	 */
	public String getDumpDDBs() {
		return this.dumpDDBs;
	}

	/**
	 * @return the info
	 */
	public TestDiskInfo getInfo() {
		return this.info;
	}

	/**
	 * @param dumpDataContent the dumpDataContent to set
	 */
	public void setDumpDataContent(final StringBuilder dumpDataContent) {
		this.dumpDataContent = dumpDataContent;
	}

	/**
	 * @param dumpDDBs the dumpDDBs to set
	 */
	public void setDumpDDBs(final String dumpDDBs) {
		this.dumpDDBs = dumpDDBs;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(final TestDiskInfo info) {
		this.info = info;
	}
}