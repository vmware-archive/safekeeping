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
package com.vmware.vmbk.soap.sso;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.AttributedString;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.PasswordString;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.UsernameTokenType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.vmware.vmbk.util.Utility;


public class UserCredentialHandler extends SSOHeaderHandler {
    
    private static final Logger logger_ = Logger.getLogger(UserCredentialHandler.class.getName());

    private final String passwd;
    private final String userName;

    
    public UserCredentialHandler(String username, String password) {
	this.userName = username;
	this.passwd = password;
    }

    
    private final JAXBElement<UsernameTokenType> createUsernameToken() {
	org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory objFactory = new org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory();

	UsernameTokenType userNameToken = objFactory.createUsernameTokenType();
	AttributedString user = objFactory.createAttributedString();
	user.setValue(userName.toString());
	userNameToken.setUsername(user);

	if (passwd != null) {
	    
	    
	    
	    PasswordString pass = objFactory.createPasswordString();
	    pass.setValue(passwd);

	    userNameToken.setPassword(pass);
	}
	return objFactory.createUsernameToken(userNameToken);
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
	if (SsoUtils.isOutgoingMessage(smc)) {
	    try {
		Node securityNode = SsoUtils.getSecurityElement(SsoUtils.getSOAPHeader(smc));
		Node usernameNode = SsoUtils.marshallJaxbElement(createUsernameToken()).getDocumentElement();
		securityNode.appendChild(securityNode.getOwnerDocument().importNode(usernameNode, true));
	    } catch (DOMException e) {
		logger_.warning(Utility.toString(e));
		throw new RuntimeException(e);
	    } catch (SOAPException e) {
		logger_.warning(Utility.toString(e));
		throw new RuntimeException(e);
	    }
	}
	

	return true;

    }
}
