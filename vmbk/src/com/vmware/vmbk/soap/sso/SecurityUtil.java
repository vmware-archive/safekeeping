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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class SecurityUtil {

    public static SecurityUtil loadFromFiles(final String privateKeyFileName, final String x509CertFileName) {
	try {
	    return new SecurityUtil().loadPrivateKey(privateKeyFileName).loadX509Cert(x509CertFileName);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(Constants.EXCEPTION_LOADING_THE_PRIVATE_KEY_CERTIFICATES_FROM_FILES, e);
	}
    }

// TODO Remove unused code found by UCDetector
//     public static SecurityUtil loadFromKeystore(final String keyStorePath, final String keyStorePassword,
// 	    final String userAlias) {
// 	try {
// 	    return new SecurityUtil().loadKeystore(keyStorePath, keyStorePassword, userAlias);
// 	} catch (final Exception e) {
// 	    e.printStackTrace();
// 	    throw new RuntimeException(Constants.EXCEPTION_READING_LOADING_THE_USER_CERTIFICATES_FROM_KEYSTORE, e);
// 	}
//     }

    private PrivateKey privateKey;

    private X509Certificate userCert;

    private SecurityUtil() {
    }

    public PrivateKey getPrivateKey() {
	return this.privateKey;
    }

    public X509Certificate getUserCert() {
	return this.userCert;
    }

    private SecurityUtil loadPrivateKey(final String privateKeyFileName)
	    throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

	final File keyFile = new File(privateKeyFileName);
	final byte[] encodedKey = new byte[(int) keyFile.length()];
	final FileInputStream keyInputStream = new FileInputStream(keyFile);
	keyInputStream.read(encodedKey);
	keyInputStream.close();
	final KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
	this.privateKey = rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
	return this;
    }

    private SecurityUtil loadX509Cert(final String x509CertFileName) throws IOException, CertificateException {

	final FileInputStream fis = new FileInputStream(x509CertFileName);
	final BufferedInputStream bis = new BufferedInputStream(fis);

	final CertificateFactory cf = CertificateFactory.getInstance("X.509");
	while (bis.available() > 0) {
	    this.userCert = (X509Certificate) cf.generateCertificate(bis);
	}
	return this;
    }

}
