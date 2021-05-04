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
package com.vmware.safekeeping.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

public class BiosUuid {
	private static BiosUuid instance;
	static final String UUID_ZERO = "00000000-0000-0000-0000-000000000000";

	/**
	 * @return
	 */
	public static BiosUuid getInstance() {
		if (instance == null) {
			return initialize();
		}
		return instance;
	}

	static String getLinuxSystemUUID() throws IOException, InterruptedException {

		final String command = "dmidecode -s system-uuid";
		String uuid = null;

		final Process serNumProcess = Runtime.getRuntime().exec(command);
		try (final BufferedReader sNumReader = new BufferedReader(
				new InputStreamReader(serNumProcess.getInputStream()))) {
			uuid = sNumReader.readLine().trim();
			serNumProcess.waitFor();
		}

		return uuid.toLowerCase();
	}

	static String getWindowsSystemUUID() throws IOException {

		final String[] command = new String[] { "wmic", "csproduct", "get", "UUID" };
		String uuid = null;

		final Process serNumProcess = Runtime.getRuntime().exec(command);
		serNumProcess.getOutputStream().close();
		try (Scanner sc = new Scanner(serNumProcess.getInputStream())) {
			sc.next();
			uuid = sc.next().toLowerCase();
		}

		return uuid;
	}

	private static BiosUuid initialize() {
		instance = new BiosUuid();
		instance.serverOs = GuestOsUtils.getOS();
		try {
			switch (instance.serverOs) {
			case "win":
				instance.serverBiosUuid = BiosUuid.getWindowsSystemUUID();
				break;
			case "uni":
				instance.serverBiosUuid = BiosUuid.getLinuxSystemUUID();
				break;
			case "osx":
			case "sol":
			default:
				break;
			}
		} catch (final IOException e) {
			instance.serverBiosUuid = UUID_ZERO;
		} catch (final InterruptedException e) {
			instance.serverBiosUuid = UUID_ZERO;
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}

		if (StringUtils.isNotEmpty(instance.serverBiosUuid)) {
			instance.bigEndianBiosUuid = Utility.toBigEndianUuid(instance.serverBiosUuid);
		}
		try {
			instance.ip = InetAddress.getLocalHost();

		} catch (final UnknownHostException e) {
			e.printStackTrace();
		}
		instance.hostname = (instance.ip != null) ? instance.ip.getHostName() : "localhost";
		return instance;

	}

	private String bigEndianBiosUuid;
	private String serverOs;
	private InetAddress ip;

	private String hostname;

	private String serverBiosUuid;

	public String getBigEndianBiosUuid() {
		return this.bigEndianBiosUuid;
	}

	public String getExtendedVersion() {
		return String.format("Server: %s ip:%s uuid:%s", this.hostname, getiIpAddress(), this.serverBiosUuid);
	}

	public String getHostname() {
		return this.hostname;
	}

	public String getiIpAddress() {
		return (this.ip != null) ? this.ip.getHostAddress() : "127.0.0.1";
	}

	/**
	 * @return the IpAddress
	 */
	public InetAddress getIpAddress() {
		return this.ip;
	}

	public String getServerBiosUuid() {
		return this.serverBiosUuid;
	}

	/**
	 * @return the serverOs
	 */
	public String getServerOs() {
		return this.serverOs;
	}

	/**
	 * @param serverOs the serverOs to set
	 */
	public void setServerOs(final String serverOs) {
		this.serverOs = serverOs;
	}
}
