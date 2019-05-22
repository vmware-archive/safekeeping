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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RenewTargetType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RenewingType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.w3c.dom.Element;

import com.rsa.names._2009._12.product.riat.wsdl.STSService;
import com.rsa.names._2009._12.product.riat.wsdl.STSServicePortType;
import com.vmware.vapi.saml.DefaultTokenFactory;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vmbk.soap.sso.HeaderHandlerResolver;
import com.vmware.vmbk.soap.sso.SSOHeaderHandler;
import com.vmware.vmbk.soap.sso.SamlTokenExtractionHandler;
import com.vmware.vmbk.soap.sso.SsoUtils;
import com.vmware.vmbk.soap.sso.TimeStampHandler;
import com.vmware.vmbk.soap.sso.UserCredentialHandler;
import com.vmware.vmbk.soap.sso.WsSecurityUserCertificateSignatureHandler;

/**
 * Samples that need a connection open before they can do anything useful extend
 * ConnectedVimServiceBase so that the code in those samples can focus on
 * demonstrating the feature at hand. The logic of most samples will not be
 * changed by the use of the BasicConnection or the SsoConnection.
 * </p>
 *
 * @see Connection
 */
class PscProvider extends SsoImpl {

    /**
     * Logger.
     */
    private static final Logger logger_ = Logger.getLogger(PscProvider.class.getName());

    private String password;
// private SecurityUtil userCert;
//    private LookupService lookup;
    private Thread renewThread;
    private SamlToken SamlBearerToken;
    private final URL ssoUrl;
    private final long ticketLifeExpectancyInMilliSeconds;
//    private final SSLContext sslContext;
    // private String privateKeyFileName;
//    private String x509CertFileName;
    private Element token;

    private String username;

    public PscProvider(final SSLContext sslContext, final String host, final String username, final String password,
	    final String x509CertFileName, final String privateKeyFileName,
	    final long ticketLifeExpectancyInMilliSeconds) throws MalformedURLException {
	super(host, sslContext, x509CertFileName, privateKeyFileName);

	this.ssoUrl = new URL(String.format("https://%s/sts/STSService", host));
	this.username = username;
	this.password = password;
	this.ticketLifeExpectancyInMilliSeconds = ticketLifeExpectancyInMilliSeconds;
    }

    @Override
    public Sso connect() throws ConnectException {
	if (!isConnected()) {
	    if (this.getUserCert() == null) {
		throw new ConnectException("User Certificate is missing");
	    }
	    try {
		setHoKTokenByUserCredential(this.ticketLifeExpectancyInMilliSeconds);
		// renewToken();
		this.lookup.connect();
	    } catch (final ConnectException e) {

		throw e;
	    }
	}
	return this;
    }

    @Override
    public boolean disconnect() {
	if (isConnected()) {
	    try {
		if (this.renewThread != null) {
		    this.renewThread.interrupt();
		}

		logger_.info("disconnected.");
	    } finally {
		setUserCert(null);
		this.lookup.disconnect();

	    }
	}
	return true;
    }

    /**
     * Will attempt to return the SSO URL you set from the command line, if you
     * forgot or didn't set one it will call getDefaultSsoUrl to attempt to
     * calculate what the URL should have been.
     *
     * @return the URL for the SSO services
     * @throws MalformedURLException
     */

    @Override
    public URL getEndPointUrl() {
	return this.ssoUrl;
    }

    @Override
    public SamlToken getSamlBearerToken() {
	return this.SamlBearerToken;
    }

    /**
     * @return the token
     */

    @Override
    public Element getToken() {
	return this.token;
    }

    /**
     * @return the userCert
     */

    @Override
    public boolean isConnected() {
	return this.token != null;
    }

    /**
     *
     * @return
     * @throws DatatypeConfigurationException
     * @throws InvalidTokenException
     */

    @Override
    public Element renewToken() throws DatatypeConfigurationException, InvalidTokenException {
	return renewToken(this.ticketLifeExpectancyInMilliSeconds);
    }

    /**
     *
     * @param durationInSeconds Duration in seconds the token should be kept valid
     *                          from the current time.
     * @return
     * @throws DatatypeConfigurationException
     * @throws InvalidTokenException
     */

    private Element renewToken(final long durationInMilliSeconds)
	    throws DatatypeConfigurationException, InvalidTokenException {
	final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();
	final STSService stsService = new STSService();

	final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
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
	final SSOHeaderHandler ssoHandler = new WsSecurityUserCertificateSignatureHandler(this.getUserCert());// this.userCert.getPrivateKey(),
	// this.userCert.getUserCert()
	// );
	headerResolver.addHandler(ssoHandler);
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
	xmlCalendar.add(dtFactory.newDuration(durationInMilliSeconds));
	expires.setValue(xmlCalendar.toXMLFormat());
	lifetime.setCreated(created);
	lifetime.setExpires(expires);
	tokenType.setLifetime(lifetime);

	tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
	tokenType.setRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Renew");

	final RenewTargetType renewTarget = new RenewTargetType();
	renewTarget.setAny(this.token);
	tokenType.setRenewTarget(renewTarget);

	/* Set the endpoint address for the request */
	final Map<String, Object> reqContext = ((BindingProvider) stsPort).getRequestContext();
	reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.ssoUrl.toString());

	/*
	 * Invoke the "renew" method on the STSService object to renew the token from
	 * SSO Server
	 */
	stsPort.renew(tokenType);
	this.token = sbHandler.getToken();
	logger_.info(SsoUtils.printToken("Renewed token", this.token));
	logger_.info(SsoUtils.printToken("New Token", this.token));
	this.SamlBearerToken = DefaultTokenFactory.createTokenFromDom(this.token);
	// SamlTokenExtractionHandler will now contain the raw SAML token for
	// further consumption
	return getToken();
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
     * @throws ConnectException
     *
     * @throws DatatypeConfigurationException
     */

    private Element setHoKTokenByUserCredential(final long durationInMilliSeconds) throws ConnectException {
	try {
	    final HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();
	    final STSService stsService = new STSService();

	    final SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
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
	    final UserCredentialHandler ucHandler = new UserCredentialHandler(this.username, this.password);
	    headerResolver.addHandler(ucHandler);

	    /*
	     * Adding the handler for signing the message via
	     * WsSecurityUserCertificateSignatureHandler
	     */
	    final SSOHeaderHandler ssoHandler = new WsSecurityUserCertificateSignatureHandler(getUserCert());// this.userCert.getPrivateKey(),
	    // this.userCert.getUserCert()
	    // );
	    headerResolver.addHandler(ssoHandler);
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
	    xmlCalendar.add(dtFactory.newDuration(durationInMilliSeconds));
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
	    reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.ssoUrl.toString());

	    /*
	     * Invoke the "issue" method on the STSService object to acquire the token from
	     * SSO Server
	     */
	    stsPort.issue(tokenType);
	    // SamlTokenExtractionHandler will now contain the raw SAML token for
	    // further consumption
	    this.token = sbHandler.getToken();
	    logger_.info(SsoUtils.printToken("New Token", this.token));
	    this.SamlBearerToken = DefaultTokenFactory.createTokenFromDom(this.token);
	    return sbHandler.getToken();
	}

	catch (final Exception e) {
	    // logger_.warning(Utility.toString(e));
	    throw new ConnectException(String.format("failed to connect to %s", getEndPointUrl().toString()), e);
	}
    }

    public void setPassword(final String password) {
	this.password = password;
    }

    public void setUsername(final String username) {
	this.username = username;
    }

    /**
     * Start the keepAlive thread
     *
     * @param vimConnections
     */

    @Override
    public void TokerRenewThread(final Map<String, VimConnection> vimConnections) {

	this.renewThread = TokenRenewThread.tokenRenewThread(this, vimConnections,
		this.ticketLifeExpectancyInMilliSeconds);
	this.renewThread.start();
    }

}
