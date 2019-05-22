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
package com.vmware.vmbk.soap.helpers;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class SslUtil {

// TODO Remove unused code found by UCDetector
//     public static KeyStore createTrustStoreForServer(final String url) {
// 	URI uri;
// 	try {
// 	    uri = new URI(url);
// 	} catch (final URISyntaxException ex) {
// 	    throw new IllegalArgumentException(ex);
// 	}
// 	final String host = uri.getHost();
// 	int port = uri.getPort();
// 	if (port == -1) {
// 	    port = 443;
// 	}
// 	return createTrustStoreForServer(host, port);
//     }

// TODO Remove unused code found by UCDetector
//     public static X509Certificate loadCertificate(final String filePath) throws IOException, CertificateException {
// 	final ByteArrayInputStream bis = new ByteArrayInputStream(Files.readAllBytes(Paths.get(filePath)));
// 	final CertificateFactory cf = CertificateFactory.getInstance("X.509");
// 	return (X509Certificate) cf.generateCertificate(bis);
//     }

// TODO Remove unused code found by UCDetector
//     public static KeyStore loadTrustStore(final String filePath, final String password) throws Exception {
// 	final KeyStore trustStore = KeyStore.getInstance("JKS");
// 	final InputStream truststoreStream = new FileInputStream(filePath);
// 	try {
// 	    trustStore.load(truststoreStream, password.toCharArray());
// 	    return trustStore;
// 	} finally {
// 	    truststoreStream.close();
// 	}
//     }

    public static void trustAllHttpsCertificates() {
	try {

	    final javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
	    final javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
	    trustAllCerts[0] = tm;

	    final javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");

	    final javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
	    /*
	     * Initialize the contexts; the session context takes the trust manager.
	     */
	    sslsc.setSessionTimeout(0);
	    sc.init(null, trustAllCerts, null);

	    /*
	     * Use the default socket factory to create the socket for the secure connection
	     */
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	    /*
	     * Declare a host name verifier that will automatically enable the connection.
	     * The host name verifier is invoked during the SSL handshake.
	     */
	    final HostnameVerifier hv = (urlHostName, session) -> true;

	    HttpsURLConnection.setDefaultHostnameVerifier(hv);

	} catch (final KeyManagementException e) {
	    throw new RuntimeException(e);
	} catch (final NoSuchAlgorithmException e) {
	    throw new RuntimeException(e);
	} catch (final IllegalArgumentException e) {
	    throw new RuntimeException(e);
	}
    }
}
