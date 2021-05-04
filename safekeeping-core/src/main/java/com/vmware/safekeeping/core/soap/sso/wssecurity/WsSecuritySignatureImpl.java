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

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vmware.safekeeping.core.soap.sso.Constants;

abstract class WsSecuritySignatureImpl implements WsSecuritySignature {

	private final PrivateKey _privateKey;
	private final X509Certificate _userCert;
	private final XMLSignatureFactory xmlSigFactory = XMLSignatureFactory.getInstance();

	WsSecuritySignatureImpl(final PrivateKey privateKey, final X509Certificate userCert) {
		this._privateKey = privateKey;
		this._userCert = userCert;
	}

	protected abstract String addUseKeySignatureId(SOAPMessage message);

	protected abstract Node createKeyInfoContent(SOAPMessage message);

	private List<Reference> createSignatureReferences(final ArrayList<String> referenceIdList)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		final List<Reference> result = new ArrayList<>();

		for (final String refId : referenceIdList) {
			if (refId == null) {
				continue;
			}

			final Reference ref = this.xmlSigFactory.newReference("#" + refId,
					this.xmlSigFactory.newDigestMethod(DigestMethod.SHA512, null),
					Collections.singletonList(this.xmlSigFactory.newCanonicalizationMethod(
							CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null)),
					null, null);

			result.add(ref);
		}

		return Collections.unmodifiableList(result);
	}

	private String createSoapBodyUuid(final SOAPMessage message) throws SOAPException {
		final String bodyId = "_" + UUID.randomUUID().toString();
		message.getSOAPBody().addAttribute(
				new QName(Constants.WSU_NAMESPACE, Constants.WSU_ID_LOCAL_NAME, Constants.WSU_PREFIX), bodyId);
		return bodyId;
	}

	private String createTimestampUuid(final SOAPMessage message) throws SOAPException {
		final NodeList timestampList = message.getSOAPHeader().getOwnerDocument()
				.getElementsByTagNameNS(Constants.WSU_NAMESPACE, Constants.WSU_TIMESTAMP_LOCAL_NAME);

		assert timestampList.getLength() <= 1;

		if (timestampList.getLength() == 1) {
			assert timestampList.item(0).getNodeType() == Node.ELEMENT_NODE;

			final Element timestamp = (Element) timestampList.item(0);
			final String timestampId = "_" + UUID.randomUUID().toString();
			timestamp.setAttributeNS(Constants.WSU_NAMESPACE, timestamp.getPrefix() + ":" + Constants.WSU_ID_LOCAL_NAME,
					timestampId);
			return timestampId;
		}

		System.out.println("Timestamp element not found in the message");
		return null;
	}

	public PrivateKey getPrivateKey() {
		return this._privateKey;
	}

	public X509Certificate getUserCert() {
		return this._userCert;
	}

	@Override
	public SOAPMessage sign(final SOAPMessage message) throws SignatureException, SOAPException {

		try {
			final CanonicalizationMethod canonicalizationMethod = this.xmlSigFactory
					.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
			final SignatureMethod signatureMethod = this.xmlSigFactory.newSignatureMethod(Constants.RSA_WITH_SHA512,
					null);
			final ArrayList<String> refList = new ArrayList<>();
			refList.add(createSoapBodyUuid(message));
			refList.add(createTimestampUuid(message));
			final List<Reference> references = createSignatureReferences(refList);
			final SignedInfo signedInfo = this.xmlSigFactory.newSignedInfo(canonicalizationMethod, signatureMethod,
					references);

			final KeyInfoFactory kif = KeyInfoFactory.getInstance();
			final KeyInfo ki = kif
					.newKeyInfo(Collections.singletonList(new DOMStructure(createKeyInfoContent(message))));

			final XMLSignature signature = this.xmlSigFactory.newXMLSignature(signedInfo, ki, null,
					addUseKeySignatureId(message), null);

			final DOMSignContext dsc = new DOMSignContext(getPrivateKey(), message.getSOAPHeader().getFirstChild());
			dsc.putNamespacePrefix(XMLSignature.XMLNS, Constants.DIGITAL_SIGNATURE_NAMESPACE_PREFIX);

			signature.sign(dsc);

		} catch (final NoSuchAlgorithmException e) {
			System.out.println(Constants.CREATING_SIGNATURE_ERR_MSG);
			e.printStackTrace();
			throw new SignatureException(Constants.CREATING_SIGNATURE_ERR_MSG, e);
		} catch (final InvalidAlgorithmParameterException e) {
			System.out.println(Constants.CREATING_SIGNATURE_ERR_MSG);
			e.printStackTrace();
			throw new SignatureException(Constants.CREATING_SIGNATURE_ERR_MSG, e);
		} catch (final MarshalException e) {
			System.out.println(Constants.CREATING_SIGNATURE_ERR_MSG);
			e.printStackTrace();
			throw new SignatureException(Constants.CREATING_SIGNATURE_ERR_MSG, e);
		} catch (final XMLSignatureException e) {
			System.out.println(Constants.CREATING_SIGNATURE_ERR_MSG);
			e.printStackTrace();
			throw new SignatureException(Constants.CREATING_SIGNATURE_ERR_MSG, e);
		}

		return message;
	}
}
