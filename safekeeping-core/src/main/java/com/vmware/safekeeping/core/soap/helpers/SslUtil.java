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
package com.vmware.safekeeping.core.soap.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

public final class SslUtil {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(SslUtil.class.getName());

    private boolean acceptUntrustedCertificate;

    private final SSLContext sslContext;

    public SSLContext getSslContext() {
        return sslContext;
    }

    public SslUtil() throws SafekeepingException {
        try {
            acceptUntrustedCertificate = CoreGlobalSettings.acceptUntrustedCertificate();
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

            try (InputStream in = new FileInputStream(CoreGlobalSettings.getKeystoreCaCertsPath())) {
                keystore.load(in, CoreGlobalSettings.getKeyStoreCaCertsPassword().toCharArray());
            }
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, CoreGlobalSettings.getKeyStoreCaCertsPassword().toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);
            // Get hold of the default trust manager
            X509TrustManager x509Tm = null;
            for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    x509Tm = (X509TrustManager) tm;
                    break;
                }
            }
            // Wrap it in your own class.
            final X509TrustManager finalTm = x509Tm;
            sslContext = SSLContext.getInstance("TLSv1.2");
            final javax.net.ssl.SSLSessionContext sslsc = sslContext.getServerSessionContext();
            /*
             * Initialize the contexts; the session context takes the trust manager.
             */
            sslsc.setSessionTimeout(0);

            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    if (!acceptUntrustedCertificate) {
                        finalTm.checkServerTrusted(chain, authType);
                    }
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    if (!acceptUntrustedCertificate) {
                        finalTm.checkClientTrusted(chain, authType);
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    if (acceptUntrustedCertificate) {
                        return new X509Certificate[0];
                    } else {
                        return finalTm.getAcceptedIssuers();
                    }
                }

            } }, new SecureRandom());

            /*
             * Use the default socket factory to create the socket for the secure connection
             */
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException
                | UnrecoverableKeyException | CertificateException e) {
            Utility.logWarning(SslUtil.logger, e);
            throw new SafekeepingException(e);
        }
    }
}
