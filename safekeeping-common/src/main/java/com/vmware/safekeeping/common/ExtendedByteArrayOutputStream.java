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
package com.vmware.safekeeping.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author mdaneri
 *
 */
public class ExtendedByteArrayOutputStream extends ByteArrayOutputStream {
	public ExtendedByteArrayOutputStream() {
		super();
	}

	public ExtendedByteArrayOutputStream(final byte[] b) {
		super(0);
		this.buf = b;
	}

	public ExtendedByteArrayOutputStream(final byte[] b, final int count) {
		super(0);
		this.buf = b;
		this.count = count;
	}

	/**
	 * @param length
	 */
	public ExtendedByteArrayOutputStream(final int length) {
		super(length);
	}

	/**
	 * Creates a new ByteArrayInputStream that uses the internal byte array buffer
	 * of this ByteArrayInOutStream instance as its buffer array. The initial value
	 * of pos is set to zero and the initial value of count is the number of bytes
	 * that can be read from the byte array. The buffer array is not copied. This
	 * instance of ByteArrayInOutStream can not be used anymore after calling this
	 * method.
	 *
	 * @return the ByteArrayInputStream instance
	 */
	public InputStream getInputStream() {
		// create new ByteArrayInputStream that respects the current count
		final ByteArrayInputStream in = new ByteArrayInputStream(this.buf, 0, this.count);

		// set the buffer of the ByteArrayOutputStream
		// to null so it can't be altered anymore
		this.buf = null;

		return in;
	}
}
