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
package com.vmware.vmbk.soap.sso.wssecurity;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.KeyIdentifierType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.SecurityTokenReferenceType;
import org.w3c.dom.Node;

import com.vmware.vmbk.soap.sso.Constants;
import com.vmware.vmbk.soap.sso.SsoUtils;

public class WsSecuritySignatureAssertion extends WsSecuritySignatureImpl {

    private final String _assertionId;

    public WsSecuritySignatureAssertion(PrivateKey privateKey, X509Certificate userCert, String assertionId) {
	super(privateKey, userCert);
	assert assertionId != null;
	_assertionId = assertionId;
    }

    @Override
    protected String addUseKeySignatureId(SOAPMessage message) {
	return null;
    }

    @Override
    protected Node createKeyInfoContent(SOAPMessage message) {
	return createSecurityTokenReference();
    }

    
    private Node createSecurityTokenReference() {
	ObjectFactory secExtFactory = new ObjectFactory();
	SecurityTokenReferenceType stRef = secExtFactory.createSecurityTokenReferenceType();
	KeyIdentifierType ki = secExtFactory.createKeyIdentifierType();
	ki.setValue(_assertionId);
	ki.setValueType(Constants.SAML_KEY_ID_TYPE);
	stRef.getAny().add(secExtFactory.createKeyIdentifier(ki));
	stRef.getOtherAttributes().put(
		new QName(Constants.WSSE11_NAMESPACE, Constants.WSSE11_TOKEN_TYPE_ATTR_NAME, Constants.WSSE11_PREFIX),
		Constants.SAML_TOKEN_TYPE);

	return SsoUtils.marshallJaxbElement(secExtFactory.createSecurityTokenReference(stRef)).getFirstChild();
    }

}
