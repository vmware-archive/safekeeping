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
package com.vmware.safekeeping.core.soap.sso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.exception.SafekeepingException;

/**
 * Use this utility class to load the private key and corresponding certificate
 * chain from either a java keystore or individual files.
 * <p>
 * <b>Note: </b>This utility class is simply provided here for convenience sake.
 * Users are free to use any other mechanism of loading the private key and
 * certificate in java and use it.
 * </p>
 *
 **/
public final class SecurityUtil {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());

    /**
     * Load the private keys, and the certificate from individual files. This method
     * comes handy when trying to work as a solution user for e.g. vCenter server.
     * The open source "openssl" tool can be leveraged for converting your private
     * key into the PKCS8 format by using the following command:
     *
     * <pre>
     * <b>
     * openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key_file -nocrypt &gt; pkcs8_key
     * </b>
     * </pre>
     *
     * @param privateKeyFileName Path to the file storing the private key in PKCS8
     *                           format ONLY
     * @param x509CertFileName   Path to the file storing the certificate in X509
     *                           format ONLY
     * @return
     * @throws SafekeepingException
     */
    public static SecurityUtil loadFromFiles(final String privateKeyFileName, final String x509CertFileName)
            throws SafekeepingException {
        try {
            return new SecurityUtil().loadPrivateKey(privateKeyFileName).loadX509Cert(x509CertFileName);
        } catch (final IOException | CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.warning(Constants.EXCEPTION_LOADING_THE_PRIVATE_KEY_CERTIFICATES_FROM_FILES);
            throw new SafekeepingException(e);
        }
    }

    /**
     * Loads the keys from the keystore.
     * <p>
     * Users can generate their own pair of private key and certificate using the
     * keytool utility shipped with the jdk. Sample usage of the keytool to generate
     * a pair would be as follows:
     *
     * <pre>
     * <b>
     *  &gt; keytool -genkey -keyalg RSA -alias sample -keystore sampleKeystore.jks -storepass sample
     *  What is your first and last name?
     *    [Unknown]:  *.vmware.com
     *  What is the name of your organizational unit?
     *    [Unknown]:  Ecosystem Engineering
     *  What is the name of your organization?
     *    [Unknown]:  VMware, Inc.
     *  What is the name of your City or Locality?
     *    [Unknown]:  Palo Alto
     *  What is the name of your State or Province?
     *    [Unknown]:  California
     *  What is the two-letter country code for this unit?
     *    [Unknown]:  US
     *  Is CN=*.vmware.com, OU=Ecosystem Engineering, O="VMware, Inc.", L=Palo Alto, ST=
     *  California, C=US correct?
     *    [no]:  yes
     *
     *  Enter key password for &lt;sample&gt;
     *          (RETURN if same as keystore password):
     * </b>
     * </pre>
     *
     * <p>
     *
     * @param keyStorePath     path to the keystore
     * @param keyStorePassword keystore password
     * @param userAlias        alias that was used at the time of key generation
     * @return
     */
    public static SecurityUtil loadFromKeystore(final String keyStorePath, final String keyStorePassword,
            final String userAlias) {
        SecurityUtil result = null;
        try {
            result = new SecurityUtil().loadKeystore(keyStorePath, keyStorePassword, userAlias);
        } catch (final NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateException
                | IOException e) {
            Utility.logWarning(logger, e);
        }
        return result;
    }

    /**
     * Load the pre-generated private keys, and the certificate from individual
     * files.
     *
     * @return the pre-generated key/cert pair
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    public static SecurityUtil loadFromResources()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
        final SecurityUtil securityUtil = new SecurityUtil();
        try (final InputStream in = SecurityUtil.class.getResourceAsStream("/sdk.crt")) {

            securityUtil.loadX509Cert(in);
        }
        try (final InputStream in = SecurityUtil.class.getResourceAsStream("/sdk.key")) {
            final byte[] encodedKey = new byte[in.available()];
            securityUtil.loadPrivateKey(in, encodedKey);
        }

        return securityUtil;

    }

    private PrivateKey privateKey;

    private X509Certificate userCert;

    /**
     * private constructor
     */
    private SecurityUtil() {
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public X509Certificate getUserCert() {
        return this.userCert;
    }

    private KeyStore keyStore;

    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     *
     * keytool -genkey -keyalg RSA -alias [userAlias] -keystore [keyStorePath]
     * -storepass [keyStorePassword]
     *
     * @param keyStorePath
     * @param keyStorePassword
     * @param userAlias
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     */
    private SecurityUtil loadKeystore(final String keyStorePath, final String keyStorePassword, final String userAlias)
            throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, CertificateException,
            IOException {
        final File file = new File(keyStorePath);
        try (final InputStream in = new FileInputStream(file)) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final char[] passphrase = keyStorePassword.toCharArray();
            keyStore.load(in, passphrase);

            if (keyStore.isKeyEntry(userAlias)) {
                // get the private key
                final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userAlias,
                        new KeyStore.PasswordProtection(passphrase));
                this.privateKey = pkEntry.getPrivateKey();
                if (pkEntry.getCertificate() instanceof X509Certificate) {
                    this.userCert = (X509Certificate) pkEntry.getCertificate();
                }
            }
        }
        return this;
    }

    private SecurityUtil loadPrivateKey(final InputStream in, final byte[] encodedKey)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        in.read(encodedKey, 0, encodedKey.length);
        final KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        return this;
    }

    private SecurityUtil loadPrivateKey(final String privateKeyFileName)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Load the private key (in PKCS#8 DER encoding).
        final File keyFile = new File(privateKeyFileName);
        final byte[] encodedKey = new byte[(int) keyFile.length()];
        try (final FileInputStream keyInputStream = new FileInputStream(keyFile)) {
            return loadPrivateKey(keyInputStream, encodedKey);
        }
    }

    private SecurityUtil loadX509Cert(final InputStream fis) throws IOException, CertificateException {
        try (final BufferedInputStream bis = new BufferedInputStream(fis)) {

            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (bis.available() > 0) {
                this.userCert = (X509Certificate) cf.generateCertificate(bis);
            }
        }
        return this;
    }

    private SecurityUtil loadX509Cert(final String x509CertFileName) throws IOException, CertificateException {
        try (final FileInputStream fis = new FileInputStream(x509CertFileName)) {
            return loadX509Cert(fis);
        }
    }

}
