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
package com.vmware.safekeeping.core.soap.sso;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.AttributedString;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.PasswordString;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.UsernameTokenType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.vmware.safekeeping.common.Utility;

public class UserCredentialHandler extends SSOHeaderHandler {

	private static final Logger logger = Logger.getLogger(UserCredentialHandler.class.getName());

	private final String passwd;
	private final String userName;

	public UserCredentialHandler(final String username, final String password) {
		this.userName = username;
		this.passwd = password;
	}

	private final JAXBElement<UsernameTokenType> createUsernameToken() {
		final org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory objFactory = new org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory();

		final UsernameTokenType userNameToken = objFactory.createUsernameTokenType();
		final AttributedString user = objFactory.createAttributedString();
		user.setValue(this.userName.toString());
		userNameToken.setUsername(user);

		if (this.passwd != null) {

			final PasswordString pass = objFactory.createPasswordString();
			pass.setValue(this.passwd);

			userNameToken.setPassword(pass);
		}
		return objFactory.createUsernameToken(userNameToken);
	}

	@Override
	public boolean handleMessage(final SOAPMessageContext smc) {
		if (SsoUtils.isOutgoingMessage(smc)) {
			try {
				final Node securityNode = SsoUtils.getSecurityElement(SsoUtils.getSOAPHeader(smc));
				final Node usernameNode = SsoUtils.marshallJaxbElement(createUsernameToken()).getDocumentElement();
				securityNode.appendChild(securityNode.getOwnerDocument().importNode(usernameNode, true));
			} catch (final DOMException e) {
				Utility.logWarning(logger, e);
				throw new RuntimeException(e);
			} catch (final SOAPException e) {
				Utility.logWarning(logger, e);
				throw new RuntimeException(e);
			}
		}

		return true;

	}
}
