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

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;

/**
 * @author mdaneri
 *
 */
public class RestoreManagedInfo {

	static public void convert(final CoreRestoreManagedInfo src, final RestoreManagedInfo dst) {
		if ((src == null) || (dst == null)) {
			return;
		}
		dst.setDcInfo(src.getDcInfo());
		dst.setName(src.getName());
		dst.setRecovery(src.isRecovery());

	}

	private String name;

	private boolean recovery;

	private ManagedEntityInfo dcInfo;

	private final List<String> spbmProfile;

	public RestoreManagedInfo() {
		this.spbmProfile = new LinkedList<>();
	}

	/**
	 * @return the dcInfo
	 */
	public ManagedEntityInfo getDcInfo() {
		return this.dcInfo;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the spbmProfile
	 */
	public List<String> getSpbmProfile() {
		return this.spbmProfile;
	}

	/**
	 * @return the recovery
	 */
	public boolean isRecovery() {
		return this.recovery;
	}

	/**
	 * @param dcInfo the dcInfo to set
	 */
	public void setDcInfo(final ManagedEntityInfo dcInfo) {
		this.dcInfo = dcInfo;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param recovery the recovery to set
	 */
	public void setRecovery(final boolean recovery) {
		this.recovery = recovery;
	}

}
