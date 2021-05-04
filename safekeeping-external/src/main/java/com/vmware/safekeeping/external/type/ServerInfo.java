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
package com.vmware.safekeeping.external.type;

/**
 * @author mdaneri
 *
 */
public class ServerInfo {

	private String serverIp;
	private String bigEndianBiosUuid;
	private String serverBiosUuid;
	private String hostname;
	private String serverOs;
	private String extendedVersion;

	/**
	 * @return the bigEndianBiosUuid
	 */
	public String getBigEndianBiosUuid() {
		return this.bigEndianBiosUuid;
	}

	/**
	 * @return the extendedVersion
	 */
	public String getExtendedVersion() {
		return this.extendedVersion;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return this.hostname;
	}

	/**
	 * @return the serverBiosUuid
	 */
	public String getServerBiosUuid() {
		return this.serverBiosUuid;
	}

	/**
	 * @return the serverIp
	 */
	public String getServerIp() {
		return this.serverIp;
	}

	/**
	 * @return the serverOs
	 */
	public String getServerOs() {
		return this.serverOs;
	}

	/**
	 * @param bigEndianBiosUuid the bigEndianBiosUuid to set
	 */
	public void setBigEndianBiosUuid(final String bigEndianBiosUuid) {
		this.bigEndianBiosUuid = bigEndianBiosUuid;
	}

	/**
	 * @param extendedVersion the extendedVersion to set
	 */
	public void setExtendedVersion(final String extendedVersion) {
		this.extendedVersion = extendedVersion;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @param serverBiosUuid the serverBiosUuid to set
	 */
	public void setServerBiosUuid(final String serverBiosUuid) {
		this.serverBiosUuid = serverBiosUuid;
	}

	/**
	 * @param serverIp the serverIp to set
	 */
	public void setServerIp(final String serverIp) {
		this.serverIp = serverIp;
	}

	/**
	 * @param serverOs the serverOs to set
	 */
	public void setServerOs(final String serverOs) {
		this.serverOs = serverOs;
	}
}
