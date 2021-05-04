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
package com.vmware.safekeeping.core.soap;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.xml.datatype.DatatypeConfigurationException;

import org.w3c.dom.Element;

import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.soap.sso.SecurityUtil;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg;

public interface ISingleSignOn {

	ISingleSignOn connect(String password, final boolean base64) throws SafekeepingConnectionException;

	boolean disconnect();

	Map<String, String> findVimUrls() throws RuntimeFaultFaultMsg;

	URL getEndPointUrl();

	String getHost();

	/**
	 * @return
	 */
	LookupService getLookup();

	int getNfcHostPort();

	/**
	 * // * @return //
	 */
//     String getPassword();

	SamlToken getSamlBearerToken() throws InvalidTokenException;

	SSLContext getSslContext();

	Element getToken();

	SecurityUtil getUserCert();

	/**
	 * @return
	 */
	String getUsername();

	/**
	 * @return
	 */
	List<VimConnection> getVimConnections();

	boolean isConnected();

	Element renewToken() throws DatatypeConfigurationException, InvalidTokenException;

	void TokerRenewThread();
}
