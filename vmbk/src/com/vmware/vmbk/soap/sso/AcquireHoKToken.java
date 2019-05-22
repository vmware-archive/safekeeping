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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RenewTargetType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RenewingType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.w3c.dom.Element;

import com.rsa.names._2009._12.product.riat.wsdl.STSService;
import com.rsa.names._2009._12.product.riat.wsdl.STSServicePortType;

public class AcquireHoKToken {

    /**
     * @param args {@link String} array containing the following values in the below
     *             order:
     *             <ul>
     *             <li>SSO server url</li>
     *             <li>username</li>
     *             <li>password</li>
     *             </ul>
     * @return {@link Element} representing the Token issued
     * @throws DatatypeConfigurationException
     */
    public static Element getBearerTokenByUserCredential(final String ssoUrl, final String username,
	    final String password) throws DatatypeConfigurationException {

	/* Instantiating the STSService */
	final STSService stsService = new STSService();

	/*
	 * Instantiating the HeaderHandlerResolver. This is required to provide the
	 * capability of modifying the SOAP headers and the SOAP message in general for
	 * various requests via the different handlers. For different kinds of requests
	 * to SSO server one needs to follow the WS-Trust guidelines to provide the
	 * required SOAP message structure.
	 */
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

	/*
	 * For this specific case we need the following header elements wrapped in the
	 * security tag.
	 *
	 * 1. Timestamp containing the request's creation and expiry time
	 *
	 * 2. UsernameToken containing the username/password
	 */

	/* Adding the Timestamp via TimeStampHandler */
	headerResolver.addHandler(new TimeStampHandler());

	/* Adding the UsernameToken via UserCredentialHandler */
	final UserCredentialHandler ucHandler = new UserCredentialHandler(username, password);
	final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
	headerResolver.addHandler(ucHandler);
	headerResolver.addHandler(sbHandler);

	/*
	 * Set the handlerResolver for the STSService to the HeaderHandlerResolver
	 * created above
	 */
	stsService.setHandlerResolver(headerResolver);

	/*
	 * Retrieve the STSServicePort from the STSServicePortType object Note: All the
	 * required handlerResolvers need to be set in the STSServicePortType object
	 * before you retrieve the STSService instance
	 */
	final STSServicePortType stsPort = stsService.getSTSServicePort();

	/*
	 * Construct the SOAP body for the request. RequestSecurityTokenType is the
	 * parameter type that is passed to the "acquire" method. However, based on what
	 * kind of token (bearer or holder-of-key type) and by what means (aka
	 * username/password, certificate, or existing token) we want to acquire the
	 * token, different elements need to be populated
	 */
	final RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

	/*
	 * For this request we need at least the following element in the
	 * RequestSecurityTokenType set
	 *
	 * 1. Lifetime - represented by LifetimeType which specifies the lifetime for
	 * the token to be issued
	 *
	 * 2. Tokentype - "urn:oasis:names:tc:SAML:2.0:assertion", which is the class
	 * that models the requested token
	 *
	 * 3. RequestType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue", as
	 * we want to get a token issued
	 *
	 * 4. KeyType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer",
	 * representing the kind of key the token will have. There are two options
	 * namely bearer and holder-of-key
	 *
	 * 5. SignatureAlgorithm - "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
	 * representing the algorithm used for generating signature
	 *
	 * 6. Renewing - represented by the RenewingType which specifies whether the
	 * token is renewable or not
	 */
	final LifetimeType lifetime = new LifetimeType();

	final DatatypeFactory dtFactory = DatatypeFactory.newInstance();
	final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	final XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(cal);
	final AttributedDateTime created = new AttributedDateTime();
	created.setValue(xmlCalendar.toXMLFormat());

	final AttributedDateTime expires = new AttributedDateTime();
	xmlCalendar.add(dtFactory.newDuration(30 * 60 * 1000));
	expires.setValue(xmlCalendar.toXMLFormat());

	lifetime.setCreated(created);
	lifetime.setExpires(expires);

	tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
	tokenType.setLifetime(lifetime);
	tokenType.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer");
	tokenType.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
	tokenType.setDelegatable(true);

	final RenewingType renewing = new RenewingType();
	renewing.setAllow(Boolean.FALSE);
	renewing.setOK(Boolean.FALSE); // WS-Trust Profile: MUST be set to false
	tokenType.setRenewing(renewing);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

	/*
	 * Invoke the "issue" method on the STSService object to acquire the token from
	 * SSO Server
	 */
	stsPort.issue(tokenType);

	// SamlTokenExtractionHandler will now contain the raw SAML token for
	// further consumption
	return sbHandler.getToken();
    }

    /**
     * @param ssoUrl     SSO server url
     * @param token      An existing Holder-Of-Key token
     * @param privateKey {@link PrivateKey} of the user or solution
     * @param userCert   {@link X509Certificate} certificate of the user or solution
     *
     * @return A new Holder-Of-Key token
     *
     * @throws DatatypeConfigurationException
     */
    public static Element getHoKTokenByHoKToken(final String ssoUrl, final Element token, final PrivateKey privateKey,
	    final X509Certificate userCert) throws DatatypeConfigurationException {

	/* Instantiating the STSService */
	final STSService stsService = new STSService();

	/*
	 * Instantiating the HeaderHandlerResolver. This is required to provide the
	 * capability of modifying the SOAP headers and the SOAP message in general for
	 * various requests via the different handlers. For different kinds of requests
	 * to SSO server one needs to follow the WS-Trust guidelines to provide the
	 * required SOAP message structure.
	 */
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

	/*
	 * For this specific case we need the following header elements wrapped in the
	 * security tag.
	 *
	 * 1. Timestamp containing the request's creation and expiry time
	 *
	 * 2. Holder-Of-Key token to be used for issuing the new token
	 *
	 * Once the above headers are added we need to sign the SOAP message using the
	 * combination of private key, certificate of the user or solution and the
	 * Holder-Of-Key token by adding a Signature element to the security header
	 */

	/* Adding the Timestamp via TimeStampHandler */
	headerResolver.addHandler(new TimeStampHandler());

	/* Adding the HoK SAML token in the header via SamlTokenHandler */
	final SamlTokenHandler stHandler = new SamlTokenHandler(token);
	headerResolver.addHandler(stHandler);
	final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
	headerResolver.addHandler(sbHandler);
	/*
	 * Adding the handler for signing the message via
	 * WsSecuritySignatureAssertionHandler
	 */
	final SSOHeaderHandler ssoHandler = new WsSecuritySignatureAssertionHandler(privateKey, userCert,
		SsoUtils.getNodeProperty(token, "ID"));
	headerResolver.addHandler(ssoHandler);

	/*
	 * Set the handlerResolver for the STSService to the HeaderHandlerResolver
	 * created above
	 */
	stsService.setHandlerResolver(headerResolver);

	/*
	 * Retrieve the STSServicePort from the STSServicePortType object Note: All the
	 * required handlerResolvers need to be set in the STSServicePortType object
	 * before you retrieve the STSService instance
	 */
	final STSServicePortType stsPort = stsService.getSTSServicePort();

	/*
	 * Construct the SOAP body for the request. RequestSecurityTokenType is the
	 * parameter type that is passed to the "acquire" method. However, based on what
	 * kind of token (bearer or holder-of-key type) and by what means (aka
	 * username/password, certificate, or existing token) we want to acquire the
	 * token, different elements need to be populated
	 */
	final RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

	/*
	 * For this request we need at least the following element in the
	 * RequestSecurityTokenType set
	 *
	 * 1. Lifetime - represented by LifetimeType which specifies the lifetime for
	 * the token to be issued
	 *
	 * 2. Tokentype - "urn:oasis:names:tc:SAML:2.0:assertion", which is the class
	 * that models the requested token
	 *
	 * 3. RequestType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue", as
	 * we want to get a token issued
	 *
	 * 4. KeyType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey",
	 * representing the holder-of-key kind of key the token will have. There are two
	 * options namely bearer and holder-of-key
	 *
	 * 5. SignatureAlgorithm - "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
	 * representing the algorithm used for generating signature
	 *
	 * 6. Renewing - represented by the RenewingType which specifies whether the
	 * token is renewable or not
	 */
	final LifetimeType lifetime = new LifetimeType();
	final DatatypeFactory dtFactory = DatatypeFactory.newInstance();
	final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	final XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(cal);
	final AttributedDateTime created = new AttributedDateTime();
	created.setValue(xmlCalendar.toXMLFormat());

	final AttributedDateTime expires = new AttributedDateTime();
	xmlCalendar.add(dtFactory.newDuration(30 * 60 * 1000));
	expires.setValue(xmlCalendar.toXMLFormat());

	lifetime.setCreated(created);
	lifetime.setExpires(expires);
	tokenType.setLifetime(lifetime);

	tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
	tokenType.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey");
	tokenType.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");

	final RenewingType renewing = new RenewingType();
	renewing.setAllow(Boolean.FALSE);
	renewing.setOK(Boolean.FALSE); // WS-Trust Profile: MUST be set to false
	tokenType.setRenewing(renewing);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

	/*
	 * Invoke the "issue" method on the STSService object to acquire the token from
	 * SSO Server
	 */
	stsPort.issue(tokenType);

	// SamlTokenExtractionHandler will now contain the raw SAML token for
	// further consumption
	return sbHandler.getToken();
    }

    /**
     * @param ssoUrl        SSO server url
     * @param solPrivateKey {@link PrivateKey} of the solution
     * @param solCert       {@link X509Certificate} certificate of the solution
     *
     * @return A new Holder-Of-Key token
     *
     * @throws DatatypeConfigurationException
     */
    public static Element getHoKTokenBySolutionCertificate(final String ssoUrl, final PrivateKey solPrivateKey,
	    final X509Certificate solCert) throws DatatypeConfigurationException {
	/* Instantiating the STSService */
	final STSService stsService = new STSService();

	/*
	 * Instantiating the HeaderHandlerResolver. This is required to provide the
	 * capability of modifying the SOAP headers and the SOAP message in general for
	 * various requests via the different handlers. For different kinds of requests
	 * to SSO server one needs to follow the WS-Trust guidelines to provide the
	 * required SOAP message structure.
	 */
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

	/*
	 * For this specific case we need the following header elements wrapped in the
	 * security tag.
	 *
	 * 1. Timestamp containing the request's creation and expiry time
	 *
	 * 2. UsernameToken containing the username/password
	 *
	 * Once the above headers are added we need to sign the SOAP message using the
	 * combination of private key, certificate of the user or solution by adding a
	 * Signature element to the security header
	 */

	/* Adding the Timestamp via TimeStampHandler */
	headerResolver.addHandler(new TimeStampHandler());

	/*
	 * Adding the handler for signing the message via
	 * WsSecurityUserCertificateSignatureHandler
	 */
	final SSOHeaderHandler ssoHandler = new WsSecurityUserCertificateSignatureHandler(solPrivateKey, solCert);
	headerResolver.addHandler(ssoHandler);
	final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
	headerResolver.addHandler(sbHandler);

	/*
	 * Set the handlerResolver for the STSService to the HeaderHandlerResolver
	 * created above
	 */
	stsService.setHandlerResolver(headerResolver);

	/*
	 * Retrieve the STSServicePort from the STSServicePortType object Note: All the
	 * required handlerResolvers need to be set in the STSServicePortType object
	 * before you retrieve the STSService instance
	 */
	final STSServicePortType stsPort = stsService.getSTSServicePort();

	/*
	 * Construct the SOAP body for the request. RequestSecurityTokenType is the
	 * parameter type that is passed to the "acquire" method. However, based on what
	 * kind of token (bearer or holder-of-key type) and by what means (aka
	 * username/password, certificate, or existing token) we want to acquire the
	 * token, different elements need to be populated
	 */
	final RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

	/*
	 * For this request we need at least the following element in the
	 * RequestSecurityTokenType set
	 *
	 * 1. Lifetime - represented by LifetimeType which specifies the lifetime for
	 * the token to be issued
	 *
	 * 2. Tokentype - "urn:oasis:names:tc:SAML:2.0:assertion", which is the class
	 * that models the requested token
	 *
	 * 3. RequestType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue", as
	 * we want to get a token issued
	 *
	 * 4. KeyType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey",
	 * representing the holder-of-key kind of key the token will have. There are two
	 * options namely bearer and holder-of-key
	 *
	 * 5. SignatureAlgorithm - "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
	 * representing the algorithm used for generating signature
	 *
	 * 6. Renewing - represented by the RenewingType which specifies whether the
	 * token is renewable or not
	 */

	final LifetimeType lifetime = new LifetimeType();
	final DatatypeFactory dtFactory = DatatypeFactory.newInstance();
	final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	final XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(cal);
	final AttributedDateTime created = new AttributedDateTime();
	created.setValue(xmlCalendar.toXMLFormat());

	final AttributedDateTime expires = new AttributedDateTime();
	xmlCalendar.add(dtFactory.newDuration(30 * 60 * 1000));
	expires.setValue(xmlCalendar.toXMLFormat());

	lifetime.setCreated(created);
	lifetime.setExpires(expires);

	tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
	tokenType.setLifetime(lifetime);
	tokenType.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey");
	tokenType.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");

	final RenewingType renewing = new RenewingType();
	renewing.setAllow(Boolean.TRUE);
	renewing.setOK(Boolean.FALSE); // WS-Trust Profile: MUST be set to false
	tokenType.setRenewing(renewing);
	// Setting the token to be delegatable
	tokenType.setDelegatable(true);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

	/*
	 * Invoke the "issue" method on the STSService object to acquire the token from
	 * SSO Server
	 */
	stsPort.issue(tokenType);

	// SamlTokenExtractionHandler will now contain the raw SAML token for
	// further consumption
	return sbHandler.getToken();
    }

    /**
     * @param args       {@link String} array containing the following values in the
     *                   below order:
     *                   <ul>
     *                   <li>SSO server url</li>
     *                   <li>username</li>
     *                   <li>password</li>
     *                   </ul>
     * @param privateKey {@link PrivateKey} of the user
     * @param userCert   {@link X509Certificate} certificate of the user
     *
     * @return A Holder-Of-Key token
     *
     * @throws DatatypeConfigurationException
     */
    public static Element getHoKTokenByUserCredential(final String ssoUrl, final String username, final String password,
	    final PrivateKey privateKey, final X509Certificate userCert) throws DatatypeConfigurationException {
	/* Instantiating the STSService */
	final STSService stsService = new STSService();

	/*
	 * Instantiating the HeaderHandlerResolver. This is required to provide the
	 * capability of modifying the SOAP headers and the SOAP message in general for
	 * various requests via the different handlers. For different kinds of requests
	 * to SSO server one needs to follow the WS-Trust guidelines to provide the
	 * required SOAP message structure.
	 */
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

	/*
	 * For this specific case we need the following header elements wrapped in the
	 * security tag.
	 *
	 * 1. Timestamp containing the request's creation and expiry time
	 *
	 * 2. UsernameToken containing the username/password
	 *
	 * Once the above headers are added we need to sign the SOAP message using the
	 * combination of private key, certificate of the user or solution by adding a
	 * Signature element to the security header
	 */

	/* Adding the Timestamp via TimeStampHandler */
	headerResolver.addHandler(new TimeStampHandler());

	/* Adding the UsernameToken via UserCredentialHandler */
	final UserCredentialHandler ucHandler = new UserCredentialHandler(username, password);
	headerResolver.addHandler(ucHandler);

	/*
	 * Adding the handler for signing the message via
	 * WsSecurityUserCertificateSignatureHandler
	 */
	final SSOHeaderHandler ssoHandler = new WsSecurityUserCertificateSignatureHandler(privateKey, userCert);
	headerResolver.addHandler(ssoHandler);
	final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
	headerResolver.addHandler(sbHandler);

	/*
	 * Set the handlerResolver for the STSService to the HeaderHandlerResolver
	 * created above
	 */
	stsService.setHandlerResolver(headerResolver);

	/*
	 * Retrieve the STSServicePort from the STSServicePortType object Note: All the
	 * required handlerResolvers need to be set in the STSServicePortType object
	 * before you retrieve the STSService instance
	 */
	final STSServicePortType stsPort = stsService.getSTSServicePort();

	/*
	 * Construct the SOAP body for the request. RequestSecurityTokenType is the
	 * parameter type that is passed to the "acquire" method. However, based on what
	 * kind of token (bearer or holder-of-key type) and by what means (aka
	 * username/password, certificate, or existing token) we want to acquire the
	 * token, different elements need to be populated
	 */
	final RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

	/*
	 * For this request we need at least the following element in the
	 * RequestSecurityTokenType set
	 *
	 * 1. Lifetime - represented by LifetimeType which specifies the lifetime for
	 * the token to be issued
	 *
	 * 2. Tokentype - "urn:oasis:names:tc:SAML:2.0:assertion", which is the class
	 * that models the requested token
	 *
	 * 3. RequestType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue", as
	 * we want to get a token issued
	 *
	 * 4. KeyType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey",
	 * representing the holder-of-key kind of key the token will have. There are two
	 * options namely bearer and holder-of-key
	 *
	 * 5. SignatureAlgorithm - "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
	 * representing the algorithm used for generating signature
	 *
	 * 6. Renewing - represented by the RenewingType which specifies whether the
	 * token is renewable or not
	 */
	final LifetimeType lifetime = new LifetimeType();
	final DatatypeFactory dtFactory = DatatypeFactory.newInstance();
	final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	final XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(cal);
	final AttributedDateTime created = new AttributedDateTime();
	created.setValue(xmlCalendar.toXMLFormat());

	final AttributedDateTime expires = new AttributedDateTime();
	xmlCalendar.add(dtFactory.newDuration(30 * 60 * 1000));
	expires.setValue(xmlCalendar.toXMLFormat());

	lifetime.setCreated(created);
	lifetime.setExpires(expires);

	tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
	tokenType.setLifetime(lifetime);
	tokenType.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey");
	tokenType.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
	tokenType.setDelegatable(true);

	final RenewingType renewing = new RenewingType();
	renewing.setAllow(Boolean.TRUE);
	renewing.setOK(Boolean.FALSE); // WS-Trust Profile: MUST be set to false
	tokenType.setRenewing(renewing);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

	/*
	 * Invoke the "issue" method on the STSService object to acquire the token from
	 * SSO Server
	 */
	stsPort.issue(tokenType);

	// SamlTokenExtractionHandler will now contain the raw SAML token for
	// further consumption
	return sbHandler.getToken();
    }

    /**
     * @param ssoUrl SSO server url
     * @param token  An existing token to be verified
     *
     * @return true is valid, false otherwise
     *
     * @throws DatatypeConfigurationException
     */
    public static boolean isTokenValid(final String ssoUrl, final Element token) throws DatatypeConfigurationException {
	/* Instantiating the STSService */
	final STSService stsService = new STSService();

	/*
	 * Instantiating the HeaderHandlerResolver. This is required to provide the
	 * capability of modifying the SOAP headers and the SOAP message in general for
	 * various requests via the different handlers. For different kinds of requests
	 * to SSO server one needs to follow the WS-Trust guidelines to provide the
	 * required SOAP message structure.
	 */
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

	/*
	 * For this specific case we need the following header elements wrapped in the
	 * security tag.
	 *
	 * 1. Timestamp containing the request's creation and expiry time
	 */

	/* Adding the Timestamp via TimeStampHandler */
	headerResolver.addHandler(new TimeStampHandler());

	/*
	 * Set the handlerResolver for the STSService to the HeaderHandlerResolver
	 * created above
	 */
	stsService.setHandlerResolver(headerResolver);

	/*
	 * Retrieve the STSServicePort from the STSServicePortType object Note: All the
	 * required handlerResolvers need to be set in the STSServicePortType object
	 * before you retrieve the STSService instance
	 */
	final STSServicePortType stsPort = stsService.getSTSServicePort();

	/*
	 * Construct the SOAP body for the request. RequestSecurityTokenType is the
	 * parameter type that is passed to the "acquire" method. However, based on what
	 * kind of token (bearer or holder-of-key type) and by what means (aka
	 * username/password, certificate, or existing token) we want to acquire the
	 * token, different elements need to be populated
	 */
	final RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

	/*
	 * For this request we need at least the following element in the
	 * RequestSecurityTokenType set
	 *
	 * 1. Tokentype -
	 * "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTR/Status", which is the
	 * class that models token status
	 *
	 * 2. RequestType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate",
	 * as we want to get a token validated
	 *
	 * 3. ValidateTarget - represented by ValidateTargetType which contains the SAML
	 * token to be validated
	 */
	tokenType.setTokenType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTR/Status");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate");

	final ValidateTargetType value = new ValidateTargetType();

	value.setAny(token);
	tokenType.setValidateTarget(value);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

	/*
	 * Invoke the "validate" method on the STSService object to validate the token
	 * from SSO Server
	 */
	final RequestSecurityTokenResponseType statusResponse = stsPort.validate(tokenType);

	/* handle the response - extract the SAML token status */
	final StatusType rstResponse = statusResponse.getStatus();

	/*
	 * There are only two possible values for the status code
	 * "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/valid" for valid
	 * token "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/invalid" for
	 * invalid token
	 */
	final String tokenStatus = rstResponse.getCode();
	System.out.println("Token Status is determined to be " + tokenStatus);
	return tokenStatus.equalsIgnoreCase("http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/valid");
    }

    /**
     * @param ssoUrl            SSO server url
     * @param token             An existing Holder-Of-Key token
     * @param privateKey        {@link PrivateKey} of the user or solution
     * @param cert              {@link X509Certificate} certificate of the user or
     *                          solution
     *
     * @param durationInSeconds Duration in seconds the token should be kept valid
     *                          from the current time.
     *
     * @return A renewed Holder-Of-Key token
     *
     * @throws DatatypeConfigurationException
     */
    public static Element renewToken(final String ssoUrl, final Element token, final PrivateKey privateKey,
	    final X509Certificate cert, final long durationInSeconds) throws DatatypeConfigurationException {
	/* Instantiating the STSService */
	final STSService stsService = new STSService();

	/*
	 * Instantiating the HeaderHandlerResolver. This is required to provide the
	 * capability of modifying the SOAP headers and the SOAP message in general for
	 * various requests via the different handlers. For different kinds of requests
	 * to SSO server one needs to follow the WS-Trust guidelines to provide the
	 * required SOAP message structure.
	 */
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

	/*
	 * For this specific case we need the following header elements wrapped in the
	 * security tag.
	 *
	 * 1. Timestamp containing the request's creation and expiry time
	 *
	 * Once the above headers are added we need to sign the SOAP message using the
	 * combination of private key, certificate of the user or solution by adding a
	 * Signature element to the security header
	 */

	/* Adding the Timestamp via TimeStampHandler */
	headerResolver.addHandler(new TimeStampHandler());

	/*
	 * Adding the handler for signing the message via
	 * WsSecurityUserCertificateSignatureHandler
	 */
	final SSOHeaderHandler ssoHandler = new WsSecurityUserCertificateSignatureHandler(privateKey, cert);
	headerResolver.addHandler(ssoHandler);
	final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
	headerResolver.addHandler(sbHandler);
	/*
	 * Set the handlerResolver for the STSService to the HeaderHandlerResolver
	 * created above
	 */
	stsService.setHandlerResolver(headerResolver);

	/*
	 * Retrieve the STSServicePort from the STSServicePortType object Note: All the
	 * required handlerResolvers need to be set in the STSServicePortType object
	 * before you retrieve the STSService instance
	 */
	final STSServicePortType stsPort = stsService.getSTSServicePort();

	/*
	 * Construct the SOAP body for the request. RequestSecurityTokenType is the
	 * parameter type that is passed to the "acquire" method. However, based on what
	 * kind of token (bearer or holder-of-key type) and by what means (aka
	 * username/password, certificate, or existing token) we want to acquire the
	 * token, different elements need to be populated
	 */
	final RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

	/*
	 * For this request we need at least the following element in the
	 * RequestSecurityTokenType set
	 *
	 * 1. Lifetime - represented by LifetimeType which specifies the lifetime for
	 * the token to be issued. In this case this will represent the extended
	 * validity period for the token after renewal
	 *
	 * 2. Tokentype - "urn:oasis:names:tc:SAML:2.0:assertion", which is the class
	 * that models the requested token
	 *
	 * 3. RequestType - "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Renew", as
	 * we want to get a token renewed
	 *
	 * 4. RenewTarget - represented by RenewTargetType which contains the
	 * Holder-Of-Key SAML token to be renewed
	 */

	final LifetimeType lifetime = new LifetimeType();

	final DatatypeFactory dtFactory = DatatypeFactory.newInstance();
	final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	final XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(cal);
	final AttributedDateTime created = new AttributedDateTime();
	created.setValue(xmlCalendar.toXMLFormat());
	final AttributedDateTime expires = new AttributedDateTime();
	xmlCalendar.add(dtFactory.newDuration(durationInSeconds * 1000));
	expires.setValue(xmlCalendar.toXMLFormat());
	lifetime.setCreated(created);
	lifetime.setExpires(expires);
	tokenType.setLifetime(lifetime);

	tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Renew");

	final RenewTargetType renewTarget = new RenewTargetType();
	renewTarget.setAny(token);
	tokenType.setRenewTarget(renewTarget);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

	/*
	 * Invoke the "renew" method on the STSService object to renew the token from
	 * SSO Server
	 */
	stsPort.renew(tokenType);

	// SamlTokenExtractionHandler will now contain the raw SAML token for
	// further consumption
	return sbHandler.getToken();
    }

}
