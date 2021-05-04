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
package com.vmware.safekeeping.core.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.linkedin.migz.MiGzInputStream;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.util.AESEncryptionManager;

interface IRestoreThread extends Callable<Boolean> {
	int GZIP_READ_BUFFER_SIZE = 4096;

	default boolean computeOpenGetDump(final ExBlockInfo blockInfo, final TargetBuffer targetBuffer,
			final boolean destinationOutputBuffer) throws IOException, IllegalBlockSizeException, BadPaddingException {
		byte[] buffer = targetBuffer.getInputBuffer();
		int bufferSize = blockInfo.getStreamSizeAsInteger();
		if (blockInfo.isCipher()) {
			bufferSize = AESEncryptionManager.decryptData(buffer, 0, bufferSize, targetBuffer.getBufferCipher(),
					blockInfo.getCipherOffset());
			buffer = targetBuffer.getBufferCipher();
		}

		if (blockInfo.isCompress()) {
			final ByteArrayInputStream b = new ByteArrayInputStream(buffer, 0, bufferSize);
			final byte[] buffer2 = targetBuffer.getBufferCompressData();
			try (MiGzInputStream mgzip = new MiGzInputStream(b)) {
				int count = 0;
				int n = 0;
				while ((n = mgzip.read(buffer2, count, count + GZIP_READ_BUFFER_SIZE)) > -1) {
					count += n;
				}
			}
			buffer = targetBuffer.getBufferCompressData();
		}
		if (destinationOutputBuffer) {
			System.arraycopy(buffer, blockInfo.getStreamOffset(), targetBuffer.getOutputBuffer(), 0,
					blockInfo.getStreamLength());
		} else {
			System.arraycopy(buffer, blockInfo.getStreamOffset(), targetBuffer.getInputBuffer(), 0,
					blockInfo.getStreamLength());
		}

		return true;
	}

	ExBlockInfo getBlockInfo();

}
