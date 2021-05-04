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
package com.vmware.safekeeping.core.profile.ovf;

import java.util.Arrays;

import com.vmware.vim25.VAppIPAssignmentInfo;

public class SerializableVAppIPAssignmentInfo {

	private String ipAllocationPolicy;

	private String ipProtocol;

	private String[] supportedAllocationScheme;

	private String[] supportedIpProtocol;

	public SerializableVAppIPAssignmentInfo() {
	}

	public SerializableVAppIPAssignmentInfo(final VAppIPAssignmentInfo vAppIPAssignmentInfo) {
		this.ipAllocationPolicy = vAppIPAssignmentInfo.getIpAllocationPolicy();
		this.ipProtocol = vAppIPAssignmentInfo.getIpProtocol();
		this.supportedAllocationScheme = vAppIPAssignmentInfo.getSupportedAllocationScheme().toArray(new String[0]);
		this.supportedIpProtocol = vAppIPAssignmentInfo.getSupportedIpProtocol().toArray(new String[0]);
	}

	/**
	 * @return the ipAllocationPolicy
	 */
	public String getIpAllocationPolicy() {
		return this.ipAllocationPolicy;
	}

	/**
	 * @return the ipProtocol
	 */
	public String getIpProtocol() {
		return this.ipProtocol;
	}

	/**
	 * @return the supportedAllocationScheme
	 */
	public String[] getSupportedAllocationScheme() {
		return this.supportedAllocationScheme;
	}

	/**
	 * @return the supportedIpProtocol
	 */
	public String[] getSupportedIpProtocol() {
		return this.supportedIpProtocol;
	}

	/**
	 * @param ipAllocationPolicy the ipAllocationPolicy to set
	 */
	public void setIpAllocationPolicy(final String ipAllocationPolicy) {
		this.ipAllocationPolicy = ipAllocationPolicy;
	}

	/**
	 * @param ipProtocol the ipProtocol to set
	 */
	public void setIpProtocol(final String ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	/**
	 * @param supportedAllocationScheme the supportedAllocationScheme to set
	 */
	public void setSupportedAllocationScheme(final String[] supportedAllocationScheme) {
		this.supportedAllocationScheme = supportedAllocationScheme;
	}

	/**
	 * @param supportedIpProtocol the supportedIpProtocol to set
	 */
	public void setSupportedIpProtocol(final String[] supportedIpProtocol) {
		this.supportedIpProtocol = supportedIpProtocol;
	}

	public VAppIPAssignmentInfo toVAppIPAssignmentInfo() {
		final VAppIPAssignmentInfo vAppIPAssignmentInfo = new VAppIPAssignmentInfo();
		vAppIPAssignmentInfo.setIpAllocationPolicy(this.ipAllocationPolicy);
		vAppIPAssignmentInfo.setIpProtocol(this.ipProtocol);
		vAppIPAssignmentInfo.getSupportedAllocationScheme().addAll(Arrays.asList(this.supportedAllocationScheme));
		vAppIPAssignmentInfo.getSupportedIpProtocol().addAll(Arrays.asList(this.supportedIpProtocol));
		return vAppIPAssignmentInfo;

	}
}
