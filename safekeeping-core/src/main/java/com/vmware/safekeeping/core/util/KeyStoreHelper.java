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
package com.vmware.safekeeping.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.GuestOsUtils;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

public class KeyStoreHelper {
    private static KeyStoreHelper keyStore;

    private static final Logger logger = Logger.getLogger(KeyStoreHelper.class.getName());

    public static final String KEYSTORE_FILENAME = "keystore.pkcs12";

    /**
     * Execute the commands
     *
     * @param jetty
     */
    public static void executeKeytools(final String[] keyToolCmd) {

        try {

            String line;
            final Process p = Runtime.getRuntime().exec(keyToolCmd, null);
            final BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            final BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = bri.readLine()) != null) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(line);
                }
            }
            bri.close();
            while ((line = bre.readLine()) != null) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning(line);
                }
            }
            bre.close();
            p.waitFor();
        } catch (final IOException e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Generate keypair
     *
     * @param confDirectory
     */
    public static void generateKeyPair(final File confDirectory) {
        final String[] jetty = getKeyToolCmdLine("jetty", confDirectory, "changeit", "changeit");
        final String[] user = getKeyToolCmdLine("user", confDirectory, "changeit", "changeit");
        final String[] aes = getKeyToolCmdLine("aes", confDirectory, "changeit", "changeit");
        executeKeytools(jetty);
        executeKeytools(user);
        executeKeytools(aes);
    }

    public static boolean doesKeyStoreExist(File confDirectory) {
        return new File(confDirectory.getPath() + File.separatorChar + KeyStoreHelper.KEYSTORE_FILENAME).exists();
    }

    public static KeyStoreHelper getKeyStoreHelper() {
        return keyStore;
    }

    private static String[] getKeyToolCmdLine(final String alias, final File confDirectory, final String keypass,
            final String storepass) {
        String[] result;
        if (GuestOsUtils.isWindows()) {
            result = new String[] { String.format("\"%s\\bin\\keytool.exe\"", System.getProperty("java.home")),
                    "-genkeypair", "-alias", alias, "-keyalg", "RSA", "-keysize", "2048", "-dname",
                    "\"CN=127.0.0.1, OU=VMware Inc, O=Ecosystem Services, L=Palo Alto, ST=CA, C=US\"", "-keypass",
                    keypass, "-validity", CoreGlobalSettings.getDefaultSelfSignedValidity(), "-storetype", "PKCS12", "-keystore",
                    String.format("\"%s%c%s\"", confDirectory.getAbsolutePath(), File.separatorChar, KEYSTORE_FILENAME),
                    "-storepass", storepass, "-v" };
        } else {
            result = new String[] { String.format("%s/bin/keytool", System.getProperty("java.home")), "-genkeypair",
                    "-alias", alias, "-keyalg", "RSA", "-keysize", "2048", "-dname",
                    "CN=127.0.0.1, OU=VMware Inc, O=Ecosystem Services, L=Palo Alto, ST=CA, C=US", "-keypass", keypass,
                    "-validity", CoreGlobalSettings.getDefaultSelfSignedValidity(), "-storetype", "PKCS12", "-keystore",
                    String.format("%s%c%s", confDirectory.getAbsolutePath(), File.separatorChar, KEYSTORE_FILENAME),
                    "-storepass", storepass, "-v" };
        }
        return result;
    }

    public static KeyStoreHelper newKeyStore(final String keyStoreFile, final String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        keyStore = new KeyStoreHelper(keyStoreFile, password);
        return keyStore;
    }

    private final KeyStore sr;

    public KeyStoreHelper(final String keyStoreFile, final String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
// creating the object of KeyStore
// and getting instance
// By using getInstance() method
        this.sr = KeyStore.getInstance("JKS");

// keystore password is required to access keystore
        final char[] pass = password.toCharArray();

// creating and initializing object of InputStream
        final InputStream is = new FileInputStream(keyStoreFile);

// Initializing keystore object
        this.sr.load(is, pass);
    }

    public X509Certificate getCert(final String alias) throws KeyStoreException {
        // getting the certificate
        // using getCertificate() method
        return (X509Certificate) this.sr.getCertificate(alias);
    }

    public KeyStore getKeyStore() {
        return this.sr;
    }

}
