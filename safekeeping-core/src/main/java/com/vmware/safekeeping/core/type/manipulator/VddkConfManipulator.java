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
package com.vmware.safekeeping.core.type.manipulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.vmware.safekeeping.core.control.SafekeepingVersion;

public class VddkConfManipulator extends PropertiesFileManipulator {
	private final String vddkConfFile;
	private boolean changed;

	public VddkConfManipulator(final String vddkConfFile) throws IOException {
		super(vddkConfFile);
		this.vddkConfFile = vddkConfFile;
	}

	public boolean enablePhoneHome(final boolean enable) {
		final String enableHome = (enable) ? "1" : "0";
		if (

		(!this.fileContent.containsKey("vixDiskLib.phoneHome.EnablePhoneHome")
				|| !(this.fileContent.get("vixDiskLib.phoneHome.EnablePhoneHome").equals(enableHome)))
				|| (!this.fileContent.containsKey("vixDiskLib.phoneHome.ProductName")
						|| !(this.fileContent.get("vixDiskLib.phoneHome.ProductName")
								.equals(SafekeepingVersion.getInstance().getProductName())))
				|| (!this.fileContent.containsKey("vixDiskLib.phoneHome.ProductVersion")
						|| !(this.fileContent.get("vixDiskLib.phoneHome.ProductVersion")
								.equals(SafekeepingVersion.getInstance().getVersion())))) {
			this.fileContent.put("vixDiskLib.phoneHome.EnablePhoneHome", enableHome);

			this.fileContent.put("vixDiskLib.phoneHome.ProductName",
					SafekeepingVersion.getInstance().getProductName());
			this.fileContent.put("vixDiskLib.phoneHome.ProductVersion", SafekeepingVersion.getInstance().getVersion());
			this.changed = true;
		}
		return enable;

	}

	public boolean getNoNfcSession() {
		if (this.fileContent.containsKey("vixDiskLib.transport.hotadd.NoNfcSession")) {
			return this.fileContent.get("vixDiskLib.transport.hotadd.NoNfcSession").equals("1");
		}
		return false;
	}

	public String getTmpDirectory() {
		if (this.fileContent.containsKey("tmpDirectory")) {
			return this.fileContent.get("tmpDirectory");
		} else {
			return null;
		}

	}

	public boolean isNoNfcSession() {
		final String content = this.fileContent.get("vixDiskLib.transport.hotadd.NoNfcSession");
		if (content == null) {
			return false;
		}
		final int noNfcSession = Integer.parseInt(content);
		return noNfcSession == 1;
	}

	public String productName(final String productName) {
		return this.fileContent.put("vixDiskLib.phoneHome.ProductName", productName);
	}

	public String productVersion(final String vmbkVersion) {
		return this.fileContent.put("vixDiskLib.phoneHome.ProductVersion", vmbkVersion);
	}

	public boolean save() throws IOException {
		if (this.changed) {
			final StringBuilder newVDDKString = new StringBuilder();
			for (final String key : this.fileContent.keySet()) {
				if (key.trim().startsWith("#")) {
					newVDDKString.append(key);
					newVDDKString.append('\n');
				} else if (key.trim().length() > 0) {
					newVDDKString.append(key);
					newVDDKString.append("=");
					newVDDKString.append(this.fileContent.get(key));
					newVDDKString.append('\n');
				} else {
					newVDDKString.append('\n');
				}

			}

			final File vddkFile = new File(this.vddkConfFile);
			try (Writer fstream = new OutputStreamWriter(new FileOutputStream(vddkFile), StandardCharsets.UTF_8)) {
				fstream.write(newVDDKString.toString());
			}

		}
		return this.changed;
	}

	public String setEnableSslFIPS(final boolean enable) {
		final String enableSslFIPS = (enable) ? "1" : "0";
		if (!this.fileContent.containsKey("vixDiskLib.ssl.enableSslFIPS")
				|| !(this.fileContent.get("vixDiskLib.ssl.enableSslFIPS").equals(enableSslFIPS))) {
			this.fileContent.put("vixDiskLib.ssl.enableSslFIPS", enableSslFIPS);
			this.changed = true;
		}
		return enableSslFIPS;
	}

	public boolean setNoNfcSession(final boolean enable) {
		final String noNfcSession = (enable) ? "1" : "0";
		if (!this.fileContent.containsKey("vixDiskLib.transport.hotadd.NoNfcSession")
				|| !(this.fileContent.get("vixDiskLib.transport.hotadd.NoNfcSession").equals(noNfcSession))) {
			this.fileContent.put("vixDiskLib.transport.hotadd.NoNfcSession", noNfcSession);
			this.changed = true;
		}
		return enable;
	}

	public String setTmpDirectory(final String tmp) {
		if (!this.fileContent.containsKey("tmpDirectory") || !(this.fileContent.get("tmpDirectory").equals(tmp))) {
			this.fileContent.put("tmpDirectory", tmp);

			this.changed = true;
		}
		return tmp;
	}

}
