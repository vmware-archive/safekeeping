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
package com.vmware.safekeeping.core.command.options;

import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.vapi.internal.util.StringUtils;

public class CoreAwsS3TargetOptions extends AbstractCoreTargetRepository {
	public static final String DEFAULT_REGION_NAME = "us-west-2";

	private String accessKey;

	private String secretKey;

	private String backet;

	private String region;

	private boolean base64;

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
		if ((StringUtils.isEmpty(backet))) {
			if (SafekeepingVersion.getInstance() != null) {
				this.backet = SafekeepingVersion.getInstance().getProductName()
						+ SafekeepingVersion.getInstance().getMajor();
			} else {
				this.backet = null;
				setEnable(false);
			}
			setRoot("");
		} else if (backet.contains("/")) {
			final int slashIndex = backet.indexOf('/');
			setRoot(backet.substring(slashIndex + 1));
			this.backet = backet.substring(0, slashIndex);
		} else {
			setRoot("");
			this.backet = backet;
		}
	}

	public void setBase64(final boolean base64) {
		this.base64 = base64;
	}

	public void setRegion(final String region) {
		if (StringUtils.isEmpty(region)) {
			this.region = DEFAULT_REGION_NAME;
		} else {
			this.region = region;
		}
	}

	public void setSecretKey(final String secretKey) {
		this.secretKey = secretKey;
	}

}
