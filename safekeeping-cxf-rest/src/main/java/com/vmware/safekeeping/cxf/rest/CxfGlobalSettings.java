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
package com.vmware.safekeeping.cxf.rest;

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.vapi.internal.util.StringUtils;

public class CxfGlobalSettings extends CoreGlobalSettings {
	private static final String WEB_SERVICE = "webService";
	private static final String HTTP_PORT = "httpPort";
	private static final String HTTPS_PORT = "httpsPort";
	private static final String CONTEXT = "context";
	private static final String HTTP_ENABLE = "enableHttp";
	private static final String HTTPS_ENABLE = "enableHttps";
	private static final String BIND = "bind";
	private static final String DEFAULT_CONTEXT = "sapi";
	private static final Integer DEFAULT_HTTPS_PORT = 7243;
	private static final Integer DEFAULT_HTTP_PORT = 8080;
	private static final Boolean DEFAULT_HTTP_ENABLE = false;
	private static final Boolean DEFAULT_HTTPS_ENABLE = true;
	public static final String DEFAULT_BINDING = "0.0.0.0";
	private static final String GUEST_MONITOR_ENABLE = "enableGuestMonitor";
	private static final Boolean DEFAULT_GUEST_MONITOR_ENABLE = false;
	private static final String STATUS_PAGE_ENABLE = "enableStatusPage";
	private static final Boolean DEFAULT_STATUS_PAGE_ENABLE = true;
	private static String webService;

	static {
		webService = WEB_SERVICE;
	}

	public static String getBind() {
		String st = configurationMap.getStringProperty(webService, BIND, DEFAULT_BINDING);
		if (StringUtils.isEmpty(st)) {
			return DEFAULT_BINDING;
		}
		return st;
	}

	public static String getContext() {
		return configurationMap.getStringProperty(webService, CONTEXT, DEFAULT_CONTEXT);
	}

	public static Integer getHttpPort() {
		return configurationMap.getIntegerProperty(webService, HTTP_PORT, DEFAULT_HTTP_PORT);
	}

	public static Integer getHttpsPort() {
		return configurationMap.getIntegerProperty(webService, HTTPS_PORT, DEFAULT_HTTPS_PORT);
	}

	public static boolean isHttpEnabled() {
		return configurationMap.getBooleanProperty(webService, HTTP_ENABLE, DEFAULT_HTTP_ENABLE);
	}

	public static boolean isHttpsEnabled() {
		return configurationMap.getBooleanProperty(webService, HTTPS_ENABLE, DEFAULT_HTTPS_ENABLE);
	}

	public static boolean isGuestMonitorEnabled() {
		return configurationMap.getBooleanProperty(webService, GUEST_MONITOR_ENABLE, DEFAULT_GUEST_MONITOR_ENABLE);
	}

	public static boolean isStatusPageEnabled() {
		return configurationMap.getBooleanProperty(webService, STATUS_PAGE_ENABLE, DEFAULT_STATUS_PAGE_ENABLE);
	}
}
