/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
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
package com.vmware.safekeeping.core.soap;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.ConnectException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;

import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RenewTargetType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rsa.names._2009._12.product.riat.wsdl.STSService;
import com.rsa.names._2009._12.product.riat.wsdl.STSServicePortType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.soap.helpers.TokenExchangeRestHandler;
import com.vmware.safekeeping.core.soap.sso.HeaderHandlerResolver;
import com.vmware.safekeeping.core.soap.sso.SSOHeaderHandler;
import com.vmware.safekeeping.core.soap.sso.SamlTokenExtractionHandler;
import com.vmware.safekeeping.core.soap.sso.SsoUtils;
import com.vmware.safekeeping.core.soap.sso.TimeStampHandler;
import com.vmware.safekeeping.core.soap.sso.WsSecurityUserCertificateSignatureHandler;
import com.vmware.vapi.saml.DefaultTokenFactory;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.saml.exception.InvalidTokenException;

/**
 * Samples that need a connection open before they can do anything useful extend
 * ConnectedVimServiceBase so that the code in those samples can focus on
 * demonstrating the feature at hand. The logic of most samples will not be
 * changed by the use of the BasicConnection or the SsoConnection.
 * </p>
 *
 * @see Connection
 */
class CspProvider extends AbstractSingleSignOnImpl {

    private static final String ACCESS_TOKEN = "access_token";

    private static final String ID_TOKEN = "id_token";

    private final TokenExchangeRestHandler apiClient = new TokenExchangeRestHandler();
    private final String csp;

    private final URL cspUrl;

    private SamlToken samlBearerToken;
    private Element token;

    private final URL tokenExchangeUrl;

    public CspProvider(final SSLContext sslContext, final String csp, final String host, final int nfcHostPort,
            final long ticketLifeExpectancyInMilliSeconds)
            throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException {
        super(host, Utility.HTTPS_PORT, nfcHostPort, sslContext, ticketLifeExpectancyInMilliSeconds);

        this.csp = csp;
        this.cspUrl = new URL(String.format("https://%s/csp/gateway/am/api/auth/api-tokens/authorize", csp));
        this.tokenExchangeUrl = new URL(String.format("https://%s/csp/gateway/am/api/auth/api-tokens/authorize", host));

    }

    public CspProvider(final SSLContext sslContext, final String csp, final String host, final int nfcHostPort,
            SsoX509CertFile certFile) throws IOException, SafekeepingException {
        super(host, Utility.HTTPS_PORT, nfcHostPort, sslContext, certFile);
        this.csp = csp;
        this.cspUrl = new URL(String.format("https://%s/csp/gateway/am/api/auth/api-tokens/authorize", csp));
        this.tokenExchangeUrl = new URL(String.format("https://%s/csp/gateway/am/api/auth/api-tokens/authorize", host));

    }

    public CspProvider(final SSLContext sslContext, final String csp, final String host, final int nfcHostPort,
            SsoKeyStoreCertificate keyStoreCert) throws IOException {
        super(host, Utility.HTTPS_PORT, nfcHostPort, sslContext, keyStoreCert);
        this.cspUrl = (csp.startsWith("http")) ? new URL(csp)
                : new URL(String.format("https://%s/csp/gateway/am/api/auth/api-tokens/authorize", csp));
        this.csp = this.cspUrl.getHost();
        this.tokenExchangeUrl = (host.startsWith("http")) ? new URL(host)
                : new URL(String.format("https://%s/csp/gateway/am/api/auth/api-tokens/authorize", host));

    }

//    @Override
//    public boolean disconnect() {
//	if (isConnected()) {
//	    try {
//		if (this.renewThread != null) {
//		    this.renewThread.interrupt();
//		}
//
//		logger_.info("disconnected.");
//	    } finally {
//		setUserCert(null);
//		this.lookup.disconnect();
//
//	    }
//	}
//	return true;
//    }

    @Override
    public ISingleSignOn connect(final String refreshToken, final boolean base64) throws SafekeepingConnectionException {
        if (!isConnected()) {
            if (getUserCert() == null) {
                throw new SafekeepingConnectionException("User Certificate is missing");
            }
            try {

                final Map<String, String> tokenMap = getAccessTokenByApiRefreshToken(refreshToken, base64);
                final String accessToken = tokenMap.get(ACCESS_TOKEN);
                final String id_token = tokenMap.get("id_token");

                final Element ltoken = getSamlTokenByApiAccessToken(accessToken, id_token, this.host);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(SsoUtils.printToken("New Token", ltoken));
                }
                this.samlBearerToken = DefaultTokenFactory.createTokenFromDom(ltoken);

                SsoUtils.printToken("New Token", ltoken);
                // setHoKTokenByUserCredential(this.ticketLifeExpectancyInMilliSeconds);
                // renewToken();
                this.lookup.connect();
            } catch (final IOException | InvalidTokenException | ParserConfigurationException | SAXException e) {
                Utility.logWarning(logger, e);
                throw new SafekeepingConnectionException(e);
            }
        }
        return this;
    }

    /***
     * Method to to get accesstoken by using refreshtoken and cspURL
     *
     * @param refreshToken refresh Token of the authorized VMware Cloud User
     * @param cspURL       VMware Cloud URL (eg:https://console.cloud.vmware.com)
     * @param base64
     * @return
     * @throws SafekeepingConnectionException
     * @throws IOException
     * @throws Exception
     */
    private Map<String, String> getAccessTokenByApiRefreshToken(final String refreshToken, final boolean base64)
            throws IOException, SafekeepingConnectionException {

        // Form the REST Header
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
        final String body = "refresh_token="
                + ((base64) ? new String(Base64.getDecoder().decode(refreshToken)) : refreshToken);

        URLEncoder.encode(body, "UTF-8");
        // POST the REST CALL and get the API Response
        final Map<Integer, String> apiResponse = this.apiClient.httpPost(this.cspUrl, headers, body);
        final Iterator<Integer> itr = apiResponse.keySet().iterator();
        final Integer key = itr.next();
        final String local_response = apiResponse.get(key).replaceFirst("\\{", "").replace("\\}", "");
        final StringTokenizer tokenizer = new StringTokenizer(local_response, ",");
        final Map<String, String> map = new HashMap<>();
        while (tokenizer.hasMoreElements()) {
            final String element = (String) tokenizer.nextElement();
            final String[] res = element.split(":");
            final String jsonkey = res[0].replaceAll("\"", "");
            final String value = res[1].replaceAll("\"", "");
            if (jsonkey.equalsIgnoreCase(ACCESS_TOKEN)) {
                map.put(jsonkey, value);
            }
            if (jsonkey.equalsIgnoreCase(ID_TOKEN)) {
                map.put(jsonkey, value);
            }
        }
        return map;
    }

    /**
     * @return the csp
     */
    public String getCsp() {
        return this.csp;
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
        return this.tokenExchangeUrl;
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

    @Override
    public SamlToken getSamlBearerToken() {
        return this.samlBearerToken;
    }

    /**
     * Method to to get SAML token in the form of DOM element by accepting the
     * following parameteres, This method process json string which comes by doing
     * http post call
     *
     * @param access_token    Access Token (Obtained from VMware Cloud Authorize API
     *                        response)
     * @param id_token        ID Token (Obtained from VMware Cloud Authorize API
     *                        response)
     * @param vcenterHostname vcenter host name
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SafekeepingConnectionException
     * @throws SAXException
     * @throws InvalidTokenException
     */
    private Element getSamlTokenByApiAccessToken(final String access_token, final String id_token,
            final String vcenterHostname) throws ParserConfigurationException, IOException,
            SafekeepingConnectionException, SAXException, InvalidTokenException {
        // Form the REST URL
        // String vcHostnameUri = vcenterHostname + SamlConfig.VCENTER_REST.getURL();
        // URL url = new URL(vcHostnameUri);
        // Form the REST Header
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        headers.put("Authorization", "Bearer " + access_token);

        // POST the REST CALL and get the API Response
        final String payload = "{ \"spec\": { \"subject_token\":\"" + access_token + "\","
                + "\"subject_token_type\":\"urn:ietf:params:oauth:token-type:access_token\", \""
                + "requested_token_type\": \"urn:ietf:params:oauth:token-type:saml2\","
                + "\"grant_type\": \"urn:ietf:params:oauth:grant-type:token-exchange\","
                + "\"actor_token_type\":\"urn:ietf:params:oauth:token-type:id_token\",\"actor_token\":\"" + id_token
                + "\"} }";

        URLEncoder.encode(payload, "UTF-8");

        final Map<Integer, String> apiResponse = this.apiClient.httpPost(this.tokenExchangeUrl, headers, payload);
        final Iterator<Integer> itr = apiResponse.keySet().iterator();
        final Integer key = itr.next();
        final String local_response = apiResponse.get(key).replaceFirst("\\{", "").replace("\\}", "");
        final StringTokenizer tokenizer = new StringTokenizer(local_response, ",");
        while (tokenizer.hasMoreElements()) {
            final String element = (String) tokenizer.nextElement();
            final String[] res = element.split(":");
            final String accessTokenElements = res[1].replaceFirst("\\{", "").replace("\\}", "");
            final String replacedAcessToken = accessTokenElements.replaceAll("\"", "");
            if (replacedAcessToken.equalsIgnoreCase(ACCESS_TOKEN)) {
                final String samlToken = res[2];
                final String replacedSamlToken = samlToken.replaceAll("\"", "");
                final byte[] decodedBytes = DatatypeConverter.parseBase64Binary(replacedSamlToken);
                final String decodedSamlToken = new String(decodedBytes, StandardCharsets.UTF_8);
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder;
                builder = factory.newDocumentBuilder();
                final Document doc = builder.parse(new InputSource(new StringReader(decodedSamlToken)));
                this.token = doc.getDocumentElement();
                this.samlBearerToken = DefaultTokenFactory.createTokenFromDom(this.token);
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = SsoUtils.printToken("New Token", this.token);
                    logger.info(msg);
                }
                return this.token;
            }
        }
        return null;
    }

    /**
     * @return the token
     */

    @Override
    public Element getToken() {
        return this.token;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return this.token != null;
    }

    /**
     *
     * @param durationInSeconds Duration in seconds the token should be kept valid
     *                          from the current time.
     * @return
     * @throws DatatypeConfigurationException
     * @throws InvalidTokenException
     */

    @Override
    protected Element renewToken(final long durationInMilliSeconds)
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
        reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.cspUrl.toString());

        /*
         * Invoke the "renew" method on the STSService object to renew the token from
         * SSO Server
         */
        stsPort.renew(tokenType);
        this.token = sbHandler.getToken();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(SsoUtils.printToken("Renewed token", this.token));
            logger.info(SsoUtils.printToken("New Token", this.token));
        }
        this.samlBearerToken = DefaultTokenFactory.createTokenFromDom(this.token);
        // SamlTokenExtractionHandler will now contain the raw SAML token for
        // further consumption
        return getToken();
    }

}
