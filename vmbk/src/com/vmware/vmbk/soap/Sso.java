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

import java.net.URL;
import java.rmi.ConnectException;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.xml.datatype.DatatypeConfigurationException;

import org.w3c.dom.Element;

import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vmbk.soap.sso.SecurityUtil;
import com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg;


public interface Sso {

    public Sso connect() throws ConnectException;

    public boolean disconnect();

    
    public Map<String, String> findVimUrls() throws RuntimeFaultFaultMsg;

    
    public URL getEndPointUrl();

    
    public String getHost();

    
    public SamlToken getSamlBearerToken();

    
    public SSLContext getSslContext();

    
    public Element getToken();

    
    public SecurityUtil getUserCert();

    public boolean isConnected();

    
    public Element renewToken() throws DatatypeConfigurationException, InvalidTokenException;

    
    public void TokerRenewThread(Map<String, VimConnection> vimConnections);

}
