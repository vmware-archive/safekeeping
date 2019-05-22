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
/**
 *
 */
package com.vmware.vmbk.soap;

import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration.SslConfiguration;
import com.vmware.vmbk.soap.helpers.SslUtil;

/**
 * @author mdaneri
 *
 */
abstract class HttpConf {
    private static HttpConfiguration httpConfig = null;

    public HttpConf() {

    }

    protected HttpConfiguration buildHttpConfiguration() {
	if (httpConfig == null) {
	    httpConfig = new HttpConfiguration.Builder().setSslConfiguration(buildSslConfiguration()).getConfig();
	}
	return httpConfig;
    }

    private SslConfiguration buildSslConfiguration() {
	SslConfiguration sslConfig;

	/*
	 * Below method enables all VIM API connections to the server without validating
	 * the server certificates.
	 *
	 * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
	 * Circumventing SSL trust is unsafe and should not be used in production
	 * software.
	 */
	SslUtil.trustAllHttpsCertificates();

	/*
	 * Below code enables all vAPI connections to the server without validating the
	 * server certificates..
	 *
	 * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
	 * Circumventing SSL trust is unsafe and should not be used in production
	 * software.
	 */
	sslConfig = new SslConfiguration.Builder().disableCertificateValidation().disableHostnameVerification()
		.getConfig();

	return sslConfig;
    }
}
