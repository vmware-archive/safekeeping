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

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vmware.safekeeping.common.Utility;

public class SamlTokenExtractionHandler extends SSOHeaderHandler {

	private static final Logger logger = Logger.getLogger(SamlTokenExtractionHandler.class.getName());

	private Node token;

	public Element getToken() {
		return (Element) this.token;
	}

	@Override
	public boolean handleMessage(final SOAPMessageContext smc) {
		if (!SsoUtils.isOutgoingMessage(smc)) {
			try {

				final SOAPBody responseBody = smc.getMessage().getSOAPBody();
				final Node firstChild = responseBody.getFirstChild();
				if ((firstChild != null)
						&& "RequestSecurityTokenResponseCollection".equalsIgnoreCase(firstChild.getLocalName())) {
					if ((firstChild.getFirstChild() != null) && "RequestSecurityTokenResponse"
							.equalsIgnoreCase(firstChild.getFirstChild().getLocalName())) {
						final Node rstrNode = firstChild.getFirstChild();
						if ((rstrNode.getFirstChild() != null)
								&& "RequestedSecurityToken".equalsIgnoreCase(rstrNode.getFirstChild().getLocalName())) {
							final Node rstNode = rstrNode.getFirstChild();
							if ((rstNode.getFirstChild() != null)
									&& "Assertion".equalsIgnoreCase(rstNode.getFirstChild().getLocalName())) {
								this.token = rstNode.getFirstChild();
							}
						}
					}
				} else {
					if ((firstChild != null)
							&& "RequestSecurityTokenResponse".equalsIgnoreCase(firstChild.getLocalName())) {
						if ((firstChild.getFirstChild() != null) && "RequestedSecurityToken"
								.equalsIgnoreCase(firstChild.getFirstChild().getLocalName())) {
							final Node rstNode = firstChild.getFirstChild();
							if ((rstNode.getFirstChild() != null)
									&& "Assertion".equalsIgnoreCase(rstNode.getFirstChild().getLocalName())) {
								this.token = rstNode.getFirstChild();
							}
						}
					}
				}
			} catch (final SOAPException e) {
				Utility.logWarning(logger, e);

			}
		}
		return true;
	}
}
