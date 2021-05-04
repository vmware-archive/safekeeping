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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.SecurityHeaderType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SsoUtils {

	private static final ObjectFactory wsseObjFactory = new ObjectFactory();

	public static String getNodeProperty(final Node node, final String propertyName) {
		return node.getAttributes().getNamedItem(propertyName).getNodeValue();
	}

	static Node getSecurityElement(final SOAPHeader header) {
		final NodeList targetElement = header.getElementsByTagNameNS(Constants.WSS_NS, Constants.SECURITY_ELEMENT_NAME);
		if ((targetElement == null) || (targetElement.getLength() == 0)) {
			final JAXBElement<SecurityHeaderType> value = wsseObjFactory
					.createSecurity(wsseObjFactory.createSecurityHeaderType());
			final Node headerNode = marshallJaxbElement(value).getDocumentElement();
			return header.appendChild(header.getOwnerDocument().importNode(headerNode, true));
		} else if (targetElement.getLength() > 1) {
			throw new RuntimeException(Constants.ERR_INSERTING_SECURITY_HEADER);
		}
		return targetElement.item(0);
	}

	static SOAPHeader getSOAPHeader(final SOAPMessageContext smc) throws SOAPException {
		return smc.getMessage().getSOAPPart().getEnvelope().getHeader() == null
				? smc.getMessage().getSOAPPart().getEnvelope().addHeader()
				: smc.getMessage().getSOAPPart().getEnvelope().getHeader();
	}

	public static boolean isHoKToken(final Node token) {
		if (isSamlToken(token)) {
			final NodeList elements = ((Element) token).getElementsByTagNameNS(
					Constants.URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION, Constants.SUBJECT_CONFIRMATION);
			if (elements.getLength() != 1) {
				throw new IllegalArgumentException(Constants.ERR_NOT_A_SAML_TOKEN);
			}
			final Node value = elements.item(0).getAttributes().getNamedItem(Constants.METHOD);
			return Constants.URN_OASIS_NAMES_TC_SAML_2_0_CM_HOLDER_OF_KEY.equalsIgnoreCase(value.getNodeValue());
		}
		throw new RuntimeException("The Node does not represnt a SAML token");
	}

	static boolean isOutgoingMessage(final SOAPMessageContext smc) {
		final Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		return outboundProperty.booleanValue();
	}

	static boolean isSamlToken(final Node token) {
		boolean isValid = false;
		isValid = (Constants.URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION.equalsIgnoreCase(token.getNamespaceURI()))
				&& ("assertion".equalsIgnoreCase(token.getLocalName()));
		return isValid;
	}

	public static final <T> Document marshallJaxbElement(final JAXBElement<T> jaxbElement) {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document result = null;
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Constants.WS_1_3_TRUST_JAXB_PACKAGE + ":"
					+ Constants.WSSE_JAXB_PACKAGE + ":" + Constants.WSSU_JAXB_PACKAGE);
			result = dbf.newDocumentBuilder().newDocument();
			jaxbContext.createMarshaller().marshal(jaxbElement, result);
		} catch (final JAXBException jaxbException) {
			jaxbException.printStackTrace();
			throw new RuntimeException(Constants.MARSHALL_EXCEPTION_ERR_MSG, jaxbException);
		} catch (final ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new RuntimeException(Constants.MARSHALL_EXCEPTION_ERR_MSG, pce);
		}

		return result;
	}

// TODO Remove unused code found by UCDetector
//     public static void printMessage(final SOAPMessageContext smc) {
// 	try {
// 	    System.out.println("*********Message Start********");
// 	    System.out.println("This is a "
// 		    + (((Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue() ? "Outbound request"
// 			    : "Inbound response"));
//
// 	    smc.getMessage().writeTo(System.out);
// 	    System.out.println("*********Message End**********");
// 	} catch (final SOAPException e) {
// 	    e.printStackTrace();
// 	    throw new RuntimeException(e);
// 	} catch (final IOException e) {
// 	    e.printStackTrace();
// 	    throw new RuntimeException(e);
// 	}
//     }

	public static String printToken(final String info, final Element token) {

		if (isSamlToken(token)) {

			final StringBuilder ret = new StringBuilder(info);
			ret.append(" - Token details:");
			ret.append("[AssertionId ");
			ret.append(SsoUtils.getNodeProperty(token, "ID"));
			ret.append("][Token type ");
			ret.append((isHoKToken(token) ? "Holder-Of-Key" : "Bearer"));
			ret.append("][Issued On ");
			ret.append(SsoUtils.getNodeProperty(token, "IssueInstant"));
			ret.append("]");
			return ret.toString();
		} else {
			return "Invalid token";
		}
	}

}
