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

import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.vmware.safekeeping.core.type.EncryptResult;

/**
 * Encryption / Decryption service using the AES algorithm example for
 * nullbeans.com
 */
public final class AESEncryptionManager {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AESEncryptionManager.class.getName());

    private static Cipher aesDecrypt;

    private static Cipher aesEncrypt;

    /**
     *
     * @param encryptedData
     * @param inputOffset
     * @param inputLen
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static int decryptData(final byte[] encryptedData, final int inputOffset, final int inputLen,
            final byte[] bufferCipher, final byte cipherOffset) throws IllegalBlockSizeException, BadPaddingException {

// Decrypt the data
        final byte[] decryptedData = aesDecrypt.doFinal(encryptedData, inputOffset, inputLen);
        final int len = decryptedData.length - cipherOffset;
        System.arraycopy(decryptedData, 0, bufferCipher, 0, len);
        return len;
    }

    /**
     * This method will encrypt the given data
     *
     * @param data         : the data that will be encrypted
     * @param inputOffset
     * @param inputLen
     * @param bufferCipher
     * @return Encrypted data in a byte array
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeySpecException
     */
    public static EncryptResult encryptData(final byte[] data, final int inputOffset, final int inputLen,
            final byte[] bufferCipher) throws BadPaddingException, IllegalBlockSizeException {
        int len = 0;
        byte elem = 0;
        final byte rest = (byte) (inputLen % 16);
        if (rest != 0) {
            elem = (byte) (16 - rest);
            len = inputLen + elem;
        } else {
            len = inputLen;
        }

        // Encrypt the data
        final byte[] encryptedData = aesEncrypt.doFinal(data, inputOffset, len);
        System.arraycopy(encryptedData, 0, bufferCipher, 0, encryptedData.length);
        return new EncryptResult(encryptedData.length, elem);

    }

    /**
     * Function to generate a 128 bit key from the given password and iv
     *
     * @param password
     * @param iv
     * @return Secret key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static SecretKey generateSecretKey(final String password, final byte[] iv)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeySpec spec = new PBEKeySpec(password.toCharArray(), iv, 65536, 128); // AES-128
        final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    public static CipherInputStream getCipherInputStream(final InputStream stream) {
        // get the rest of encrypted data
        return new CipherInputStream(stream, aesDecrypt);
    }

    public static void initialize(final String key)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException {
        // Prepare the nonce

        // Noonce should be 12 bytes
        final byte[] iv = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
//	secureRandom.nextBytes(iv);

        // Prepare your key/password

        final SecretKey secretKey = generateSecretKey(key, iv);
//	final String algorithm = "RawBytes";
//	final SecretKeySpec secretKey = new SecretKeySpec(iv, algorithm);
        // "AES/ECB/PKCS5Padding");// "AES/CBC/PKCS5Padding");
        aesEncrypt = Cipher.getInstance("AES/ECB/NoPadding");
//	final GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        // Encryption mode on!
        aesEncrypt.init(Cipher.ENCRYPT_MODE, secretKey);
        // Prepare your key/password

        aesDecrypt = Cipher.getInstance("AES/ECB/NoPadding");

        // Encryption mode on!
        aesDecrypt.init(Cipher.DECRYPT_MODE, secretKey);

//	ByteArrayInOutStream.setAesDecrypt(aesDecrypt);
//	ByteArrayInOutStream.setAesEncrypt(aesEncrypt);

        if (logger.isLoggable(Level.INFO)) {
            logger.info("AES Encryption Manager initiated");
        }

    }

    private AESEncryptionManager() {
        throw new IllegalStateException("Utility class");
    }

//    private void initCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
//  	this.keygen = KeyGenerator.getInstance("AES");
//  	this.k = this.keygen.generateKey();
//  	this.aesDecrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
//  	this.aesDecrypt.init(Cipher.DECRYPT_MODE, this.k);
//  	this.aesEncrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
//  	this.aesEncrypt.init(Cipher.ENCRYPT_MODE, this.k);
//      }
}
