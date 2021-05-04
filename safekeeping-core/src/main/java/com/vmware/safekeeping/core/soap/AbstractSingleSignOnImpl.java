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
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.xml.datatype.DatatypeConfigurationException;

import org.w3c.dom.Element;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.sso.SecurityUtil;
import com.vmware.safekeeping.core.type.VmbkThreadFactory;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg;

abstract class AbstractSingleSignOnImpl implements ISingleSignOn {
    protected static final Logger logger = Logger.getLogger(AbstractSingleSignOnImpl.class.getName());
    protected final String host;
    protected LookupService lookup;
    private final SSLContext sslContext;
    private SecurityUtil userCert;

    private final List<VimConnection> vimConnection;

    private int nfcHostPort;

    protected final long ticketLifeExpectancyInMilliSeconds;
    /**
     * Start the keepAlive thread
     *
     * @param vimConnections
     */
    private ScheduledExecutorService scheduler;
    private final int port;

    AbstractSingleSignOnImpl(final String host, final int port, final int nfcHostPort, final SSLContext sslContext,
            final long ticketLifeExpectancyInMilliSeconds)
            throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException {
        this.lookup = new LookupService(host);
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.vimConnection = new LinkedList<>();
        this.ticketLifeExpectancyInMilliSeconds = ticketLifeExpectancyInMilliSeconds;
        this.userCert = SecurityUtil.loadFromResources();
        this.nfcHostPort = nfcHostPort;
    }

    AbstractSingleSignOnImpl(final String host, final int port, final int nfcHostPort, final SSLContext sslContext,
            SsoX509CertFile certFile) throws IOException, SafekeepingException {
        this.lookup = new LookupService(host);
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.vimConnection = new LinkedList<>();
        this.ticketLifeExpectancyInMilliSeconds = certFile.getTicketLifeExpectancyInMilliSeconds();

        this.userCert = SecurityUtil.loadFromFiles(certFile.getX509CertPrivateKeyFile(), certFile.getX509CertFile());
        this.nfcHostPort = nfcHostPort;
    }

    AbstractSingleSignOnImpl(final String host, final int port, final int nfcHostPort, final SSLContext sslContext,
            SsoKeyStoreCertificate keyStoreCert) throws IOException {
        this.lookup = new LookupService(host);
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.vimConnection = new LinkedList<>();
        this.ticketLifeExpectancyInMilliSeconds = keyStoreCert.getTicketLifeExpectancyInMilliSeconds();
        this.userCert = SecurityUtil.loadFromKeystore(keyStoreCert.getKeystorePath(),
                keyStoreCert.getKeyStorePassword(), keyStoreCert.getKeyStoreUserCertificateAlias());
        this.nfcHostPort = nfcHostPort;
    }

    @Override
    public boolean disconnect() {
        if (isConnected()) {
            try {
                this.lookup.disconnect();
                if (this.scheduler != null) {
                    this.scheduler.shutdown();
                }

                logger.info("disconnected.");
            } finally {
                setUserCert(null);
            }
        }
        return true;
    }

    @Override
    public Map<String, String> findVimUrls() throws RuntimeFaultFaultMsg {
        return this.lookup.findVimUrls();
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public LookupService getLookup() {
        return this.lookup;
    }

    @Override
    public int getNfcHostPort() {
        return this.nfcHostPort;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return this.port;
    }

    @Override
    public SSLContext getSslContext() {
        return this.sslContext;
    }

    @Override
    public SecurityUtil getUserCert() {
        return this.userCert;
    }

    @Override
    public List<VimConnection> getVimConnections() {
        return this.vimConnection;
    }

    @Override
    public Element renewToken() throws DatatypeConfigurationException, InvalidTokenException {
        return renewToken(this.ticketLifeExpectancyInMilliSeconds);
    }

    protected abstract Element renewToken(final long durationInMilliSeconds)
            throws DatatypeConfigurationException, InvalidTokenException;

    public void setNfcHostPort(final int nfcHostPort) {
        this.nfcHostPort = nfcHostPort;
    }

    public void setUserCert(final SecurityUtil userCert) {
        this.userCert = userCert;
    }

    @Override
    public void TokerRenewThread() {
        final Runnable renewThread = () -> {
            try {
                final Element token = renewToken();
                for (final VimConnection vim : getVimConnections()) {
                    if (vim.isConnected()) {
                        vim.updateHeaderHandlerResolver(token);
                        vim.updatePbmHeaderHandlerResolver();
                        vim.updateVslmHeaderHandlerResolver();
                        vim.updateVapi(getSamlBearerToken());
                    }
                }

            } catch (final Exception e) {
                Utility.logWarning(logger, e);

            }
        };
        final long delay = CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds() / 2;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new VmbkThreadFactory("RenewToken", true));
        this.scheduler.scheduleAtFixedRate(renewThread, delay, delay, TimeUnit.MILLISECONDS);

    }
}
