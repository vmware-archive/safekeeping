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
package com.vmware.vmbk.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

public class ByteArrayInOutStream extends ByteArrayOutputStream {
    private byte[] deflateBuffer;

    private int deflateCount = 0;
    private boolean deflateReady;
    private MessageDigest md5;
    private byte[] md5TempBuffer;

    private byte[] tempWriteBuffer;

    Deflater deflater;

    public ByteArrayInOutStream() {
	super();

	try {
	    this.md5 = MessageDigest.getInstance("MD5");
	    this.md5TempBuffer = new byte[32];
	    this.deflateReady = false;
	} catch (final NoSuchAlgorithmException e) {

	    e.printStackTrace();
	}
    }

    // TODO Remove unused code found by UCDetector
    public ByteArrayInOutStream(final byte[] b) throws IOException {
	super(b.length);
	write(b);
	try {
	    this.md5 = MessageDigest.getInstance("MD5");
	    this.md5TempBuffer = new byte[b.length];
	    this.deflateReady = false;
	} catch (final NoSuchAlgorithmException e) {

	    e.printStackTrace();
	}
    }

    public ByteArrayInOutStream(final int size) {
	super(size);

	try {
	    this.md5 = MessageDigest.getInstance("MD5");
	    this.md5TempBuffer = new byte[size];
	    this.deflateReady = false;
	} catch (final NoSuchAlgorithmException e) {

	    e.printStackTrace();
	}
    }

    public ByteArrayInOutStream(final String st) throws IOException {
	super(st.length() * 2);
	write(st);
	try {
	    this.md5 = MessageDigest.getInstance("MD5");
	    this.md5TempBuffer = new byte[st.length() * 2];
	    this.deflateReady = false;
	} catch (final NoSuchAlgorithmException e) {

	    e.printStackTrace();
	}
    }

    private void deflate() throws IOException {
	if ((this.deflateBuffer == null) || (this.deflateBuffer.length < this.buf.length)) {
	    this.deflateBuffer = new byte[this.buf.length];
	}
	if ((this.md5TempBuffer == null) || (this.md5TempBuffer.length < this.buf.length)) {
	    this.md5TempBuffer = new byte[this.buf.length];
	}
	this.deflateReady = false;
	if (this.deflater == null) {
	    this.deflater = new Deflater();
	    // final Deflater deflater = new Deflater();
	}

	this.deflateCount = 0;
	int deflateSize = 0;

	this.deflater.setInput(this.buf, 0, this.count);
	this.deflater.finish();
	final byte[] buffer = new byte[1024];
	while (!this.deflater.finished()) {
	    deflateSize = this.deflater.deflate(buffer);
	    System.arraycopy(buffer, 0, this.deflateBuffer, this.deflateCount, deflateSize);
	    this.deflateCount += deflateSize;
	}

	System.arraycopy(this.deflateBuffer, 0, this.md5TempBuffer, 0, this.deflateCount);

	this.md5.update(this.md5TempBuffer, 0, this.deflateCount);
	this.deflater.reset();
//	deflater.end();
	this.deflateReady = true;
    }

    public synchronized void deflateTo(final OutputStream out) throws IOException {
	if (!this.deflateReady) {
	    deflate();
	}
	out.write(this.deflateBuffer, 0, this.deflateCount);
    }

    public byte[] getBuffer() {
	return this.buf;
    }

    public ByteArrayInputStream getDeflateInputStream() throws IOException {
	if (!this.deflateReady) {
	    deflate();
	}
	final ByteArrayInputStream in = new ByteArrayInputStream(this.deflateBuffer, 0, this.deflateCount);
	return in;
    }

    public long getDeflateSize() {
	return this.deflateCount;
    }

    public ByteArrayInputStream getInputStream() {

	final ByteArrayInputStream in = new ByteArrayInputStream(this.buf, 0, this.count);

	return in;
    }

    public byte[] md5DeflateDigest() throws IOException {
	if (!this.deflateReady) {
	    deflate();
	}
	return this.md5.digest();

    }

    public byte[] md5Digest() throws IOException {
	return this.md5.digest(toByteArray());

    }

    public byte[] md5DigestBase64() throws IOException {

	return Base64.encodeBase64(md5Digest());
    }

    @Override
    public synchronized void reset() {
	super.reset();
	this.deflateReady = false;
	this.deflateCount = 0;
    }

    public void write(final ByteBuffer src) throws IOException {
	if (this.tempWriteBuffer == null) {
	    this.tempWriteBuffer = new byte[src.capacity()];
	}

	final int size = src.remaining();
	src.get(this.tempWriteBuffer);
	this.write(this.tempWriteBuffer, 0, size);
	return;
    }

    public void write(final String src) throws IOException {
	final byte[] b = src.getBytes();
	this.write(b);

    }

}
