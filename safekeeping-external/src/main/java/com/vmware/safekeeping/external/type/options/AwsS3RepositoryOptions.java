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
package com.vmware.safekeeping.external.type.options;

import com.vmware.safekeeping.core.command.options.CoreAwsS3TargetOptions;

public class AwsS3RepositoryOptions extends RepositoryOptions {

	public static void convert(final AwsS3RepositoryOptions src, final CoreAwsS3TargetOptions dst) {
		if ((src == null) || (dst == null)) {
			return;
		}
		RepositoryOptions.convert(src, dst);
		dst.setAccessKey(src.accessKey);
		dst.setSecretKey(src.secretKey);
		dst.setBacket(src.backet);
		dst.setRegion(src.region);
		dst.setBase64(src.base64);

	}

	public static void convert(final CoreAwsS3TargetOptions src, final AwsS3RepositoryOptions dst) {
		if ((src == null) || (dst == null)) {
			return;
		}
		RepositoryOptions.convert(src, dst);
		dst.setAccessKey(src.getAccessKey());
		dst.setSecretKey(src.getSecretKey());
		dst.setBacket(src.getBacket());
		dst.setRegion(src.getRegion());
		dst.setBase64(src.isBase64());

	}

	private String accessKey;

	private String secretKey;

	private String backet;

	private String region;
	private boolean base64;

	public AwsS3RepositoryOptions convert(CoreAwsS3TargetOptions src) {
		AwsS3RepositoryOptions.convert(src, this);
		return this;
	}

	public String getAccessKey() {
		return this.accessKey;
	}

	public String getBacket() {
		return this.backet;
	}

	public String getRegion() {
		return this.region;
	}

	public String getSecretKey() {
		return this.secretKey;
	}

	public boolean isBase64() {
		return this.base64;
	}

	public void setAccessKey(final String accessKey) {
		this.accessKey = accessKey;
	}

	public void setBacket(final String backet) {
		this.backet = backet;
	}

	public void setBase64(final boolean base64) {
		this.base64 = base64;
	}

	public void setRegion(final String region) {
		this.region = region;
	}

	public void setSecretKey(final String secretKey) {
		this.secretKey = secretKey;
	}
}
