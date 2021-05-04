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
package com.vmware.safekeeping.core.soap.sso.wssecurity;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.oasis_open.docs.ws_sx.ws_trust._200512.UseKeyType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.BinarySecurityTokenType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ReferenceType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.SecurityTokenReferenceType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vmware.safekeeping.core.soap.sso.Constants;
import com.vmware.safekeeping.core.soap.sso.SsoUtils;

public class WsSecuritySignatureCertificate extends WsSecuritySignatureImpl {

	public WsSecuritySignatureCertificate(PrivateKey privateKey, X509Certificate userCert) {
		super(privateKey, userCert);
	}

	@Override
	protected String addUseKeySignatureId(SOAPMessage message) {
		String sigId = "_" + UUID.randomUUID().toString();
		try {
			message.getSOAPBody().appendChild(message.getSOAPPart().importNode(createUseKeyElement(sigId), true));
		} catch (DOMException e) {
			System.out.println(Constants.ERR_CREATING_USE_KEY_ELEMENT);
			e.printStackTrace();
			throw new RuntimeException(Constants.ERR_CREATING_USE_KEY_ELEMENT, e);
		} catch (SOAPException e) {
			System.out.println(Constants.ERR_CREATING_USE_KEY_ELEMENT);
			e.printStackTrace();
			throw new RuntimeException(Constants.ERR_CREATING_USE_KEY_ELEMENT, e);
		}
		return sigId;
	}

	private Node createBinarySecurityToken(String uuid) {
		org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory secExtFactory = new org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory();
		BinarySecurityTokenType bst = secExtFactory.createBinarySecurityTokenType();
		try {

			bst.setValue(DatatypeConverter.printBase64Binary(getUserCert().getEncoded()));
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(Constants.ERROR_CREATING_BINARY_SECURITY_TOKEN, e);
		}

		bst.setValueType(Constants.X509_CERTIFICATE_TYPE);
		bst.setEncodingType(Constants.ENCODING_TYPE_BASE64);
		bst.setId(uuid);
		return SsoUtils.marshallJaxbElement(secExtFactory.createBinarySecurityToken(bst)).getFirstChild();
	}

	@Override
	protected Node createKeyInfoContent(SOAPMessage message) {

		String bstId = "_" + UUID.randomUUID().toString();

		NodeList secNodeList = message.getSOAPPart().getElementsByTagNameNS(Constants.WSSE_NAMESPACE,
				Constants.SECURITY_ELEMENT);
		if (secNodeList.getLength() != 1) {
			throw new RuntimeException("No/too many security elements found");
		}
		secNodeList.item(0).appendChild(message.getSOAPPart().importNode(createBinarySecurityToken(bstId), true));

		return createSecurityTokenReference(bstId);
	}

	private Node createSecurityTokenReference(String refId) {
		org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory secExtFactory = new org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory();
		SecurityTokenReferenceType stRef = secExtFactory.createSecurityTokenReferenceType();
		ReferenceType ref = secExtFactory.createReferenceType();
		ref.setURI("#" + refId);
		ref.setValueType(Constants.X509_CERTIFICATE_TYPE);
		stRef.getAny().add(secExtFactory.createReference(ref));
		return SsoUtils.marshallJaxbElement(secExtFactory.createSecurityTokenReference(stRef)).getFirstChild();
	}

	private Node createUseKeyElement(String sigId) {
		org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory wstFactory = new org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory();
		UseKeyType useKey = wstFactory.createUseKeyType();
		useKey.setSig(sigId);
		return SsoUtils.marshallJaxbElement(wstFactory.createUseKey(useKey)).getFirstChild();
	}

}
