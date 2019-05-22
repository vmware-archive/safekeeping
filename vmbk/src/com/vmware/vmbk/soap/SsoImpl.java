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
package com.vmware.vmbk.soap;

import java.net.MalformedURLException;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.vmware.vmbk.soap.sso.SecurityUtil;
import com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg;

abstract class SsoImpl implements Sso {
    protected final String host;
    protected LookupService lookup;
    private String privateKeyFileName;
    private final SSLContext sslContext;
    private SecurityUtil userCert;
    private String x509CertFileName;

    SsoImpl(final String host, final SSLContext sslContext, final String x509CertFileName,
	    final String privateKeyFileName) throws MalformedURLException {
	this.lookup = new LookupService(host);
	this.host = host;
	this.privateKeyFileName = privateKeyFileName;
	this.x509CertFileName = x509CertFileName;
	this.userCert = SecurityUtil.loadFromFiles(this.privateKeyFileName, this.x509CertFileName);
	this.sslContext = sslContext;
    }

    @Override
    public Map<String, String> findVimUrls() throws RuntimeFaultFaultMsg {
	return this.lookup.findVimUrls();
    }

    @Override
    public String getHost() {
	return this.host;
    }

    public String getPrivateKeyFileName() {
	return this.privateKeyFileName;
    }

    @Override
    public SSLContext getSslContext() {
	return this.sslContext;
    }

    @Override
    public SecurityUtil getUserCert() {
	return this.userCert;
    }

    public String getX509CertFileName() {
	return this.x509CertFileName;
    }

    public void setPrivateKeyFileName(final String privateKeyFileName) {
	this.privateKeyFileName = privateKeyFileName;
    }

    public void setUserCert(final SecurityUtil userCert) {
	this.userCert = userCert;
    }

    public void setX509CertFileName(final String x509CertFileName) {
	this.x509CertFileName = x509CertFileName;
    }
}
