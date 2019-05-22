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
package com.vmware.vmbk.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.control.IoFunction;

public class BiosUuid {
    private static String bigEndianBiosUuid;
    private static InetAddress ip;

    private static final Logger logger_ = Logger.getLogger(BiosUuid.class.getName());
    private static String serverBiosUuid;

    public static String getBigEndianBiosUuid() {
	return bigEndianBiosUuid;
    }

    private static String getiIpAddress() {
	return (ip != null) ? ip.getHostAddress() : "127.0.0.1";
    }

    static public void getLocalHwSettings() {
	switch (Utility.getOS()) {

	case "win":
	    serverBiosUuid = Utility.getWindowsSystem_UUID();
	    break;
	case "uni":
	    serverBiosUuid = Utility.getLinuxSystem_UUID();
	    break;
	case "osx":
	    break;
	case "sol":
	    break;
	}
	try {
	    ip = InetAddress.getLocalHost();

	} catch (final UnknownHostException e) {
	    e.printStackTrace();
	}

	final String hostname = (ip != null) ? ip.getHostName() : "localhost";
	IoFunction.showInfo(logger_, "Server: %s ip:%s uuid:%s", hostname, getiIpAddress(), serverBiosUuid);
	if (StringUtils.isNotEmpty(serverBiosUuid)) {
	    bigEndianBiosUuid = Utility.toBigEndianUuid(serverBiosUuid);
	}
    }

    public static String getServerBiosUuid() {
	return serverBiosUuid;
    }

}
