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
package com.vmware.safekeeping.core.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

public class ByteArrayInOutStream extends ByteArrayOutputStream {

	private static Cipher aesDecrypt;

	private static Cipher aesEncrypt;

	/**
	 * @return the aesDecrypt
	 */
	public static Cipher getAesDecrypt() {
		return aesDecrypt;
	}

	/**
	 * @return the aesEncrypt
	 */
	public static Cipher getAesEncrypt() {
		return aesEncrypt;
	}

	private final MessageDigest md5;

	private byte[] tempWriteBuffer;

	public ByteArrayInOutStream() throws NoSuchAlgorithmException {
		super();
		this.md5 = MessageDigest.getInstance("MD5");
	}

	public ByteArrayInOutStream(final int size) throws NoSuchAlgorithmException {
		super(size);
		this.md5 = MessageDigest.getInstance("MD5");

	}

	public ByteArrayInOutStream(final String st) throws IOException, NoSuchAlgorithmException {
		super(st.length() * 2);
		write(st);
		this.md5 = MessageDigest.getInstance("MD5");
	}

	public ByteArrayInputStream getByteArrayInputStream() {
		return new ByteArrayInputStream(this.buf, 0, this.count);
	}

	public synchronized InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.buf, 0, this.count);
	}

	public synchronized byte[] md5Digest() {
		this.md5.reset();
		this.md5.update(this.buf, 0, this.count);
		return this.md5.digest();
	}

	@Override
	public synchronized void reset() {
		super.reset();
	}

//    /**
//     * Writes <code>len</code> bytes from the specified byte array starting at
//     * offset <code>off</code> to this byte array output stream. The operation reset
//     * deflate and encrypt buffer
//     *
//     * @param b   the data.
//     * @param off the start offset in the data.
//     * @param len the number of bytes to write.
//     */
//    @Override
//    public synchronized void write(final byte b[], final int off, final int len) {
//
//	super.write(b, off, len);
//    }

	public synchronized void write(final ByteBuffer src) {
		final int size = src.remaining();
		if (size != 0) {
			if (this.tempWriteBuffer == null) {
				this.tempWriteBuffer = new byte[src.capacity()];
			}
			if (this.tempWriteBuffer.length > size) {
				throw new BufferUnderflowException();
			}
			src.get(this.tempWriteBuffer);
			this.write(this.tempWriteBuffer, 0, size);
		}
	}

//    /**
//     * Writes the specified byte to this byte array output stream. The operation
//     * reset deflate and encrypt buffer
//     *
//     * @param b the byte to be written.
//     */
//    @Override
//    public synchronized void write(final int b) {
//	resetBuffers();
//	super.write(b);
//    }

	/**
	 * Writes <code>b.length</code> bytes from the specified byte array to this
	 * output stream. The general contract for <code>write(b)</code> is that it
	 * should have exactly the same effect as the call
	 * <code>write(b, 0, b.length)</code>. The operation reset deflate and encrypt
	 * buffer
	 *
	 * @param b the data.
	 * @exception IOException if an I/O error occurs.
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public synchronized void write(final String src) throws IOException {

		final byte[] b = src.getBytes();
		this.write(b);

	}

}
