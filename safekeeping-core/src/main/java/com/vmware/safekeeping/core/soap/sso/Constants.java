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

/**
 * Class to capture all the constants used throughout the SDK
 *
 * @author Ecosystem Engineering
 */
public final class Constants {
// TODO Remove unused code found by UCDetector
//     public static final String AFTER_ADDING_THE_TIME_STAMP_NODE_IN_THE_HEADER = "After adding the TimeStamp node in the header";
// TODO Remove unused code found by UCDetector
//     public static final String AFTER_ADDING_THE_USERNAME_NODE_IN_THE_HEADER = "After adding the username node in the header";
	public static final String CREATING_SIGNATURE_ERR_MSG = "Error while creating SOAP request signature";
// TODO Remove unused code found by UCDetector
//     public static final String DBG_AFTER_ADDING_SAML_HEADER = "SAML token added to the header";
	public static final String DIGITAL_SIGNATURE_NAMESPACE_PREFIX = "ds";

	public static final String ENCODING_TYPE_BASE64 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";
	public static final String ERR_CREATING_USE_KEY_ELEMENT = "Error creating UseKey element";
	static final String ERR_INSERTING_SECURITY_HEADER = "Error inserting Security header into the SOAP message. Too many Security found.";
	static final String ERR_NOT_A_SAML_TOKEN = "Token provided is not a SAML token";
	public static final String ERROR_CREATING_BINARY_SECURITY_TOKEN = "Error creating BinarySecurityToken";
	static final String EXCEPTION_LOADING_THE_PRIVATE_KEY_CERTIFICATES_FROM_FILES = "Exception loading the private key / certificates from files";
// TODO Remove unused code found by UCDetector
//     static final String EXCEPTION_READING_LOADING_THE_USER_CERTIFICATES_FROM_KEYSTORE = "Exception reading loading the user certificates from keystore";
	static final String MARSHALL_EXCEPTION_ERR_MSG = "Error marshalling JAXB document";
	static final String METHOD = "Method";
// TODO Remove unused code found by UCDetector
//     public static final String PARSING_XML_ERROR_MSG = "Error while parsing the SOAP request (signature creation)";
	static final int REQUEST_VALIDITY_IN_MINUTES = 10;
	public static final String RSA_WITH_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";

	public static final String SAML_KEY_ID_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID";
	public static final String SAML_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";
	public static final String SECURITY_ELEMENT = "Security";

	static final String SECURITY_ELEMENT_NAME = "Security";
// TODO Remove unused code found by UCDetector
//     public static final String SIGNATURE_ELEMENT_NAME = "Signature";
	static final String SUBJECT_CONFIRMATION = "SubjectConfirmation";
	static final String URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION = "urn:oasis:names:tc:SAML:2.0:assertion";
	static final String URN_OASIS_NAMES_TC_SAML_2_0_CM_HOLDER_OF_KEY = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
	static final String WS_1_3_TRUST_JAXB_PACKAGE = "org.oasis_open.docs.ws_sx.ws_trust._200512";

	static final String WSS_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	static final String WSSE_JAXB_PACKAGE = "org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0";
	public static final String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static final String WSSE11_NAMESPACE = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";
	public static final String WSSE11_PREFIX = "wsse11";
	public static final String WSSE11_TOKEN_TYPE_ATTR_NAME = "TokenType";
	static final String WSSU_JAXB_PACKAGE = "org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0";
	public static final String WSU_ID_LOCAL_NAME = "Id";
	public static final String WSU_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	public static final String WSU_PREFIX = "wsu";
	public static final String WSU_TIMESTAMP_LOCAL_NAME = "Timestamp";
	public static final String X509_CERTIFICATE_TYPE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";
	static final String XML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
}
