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
package com.vmware.vmbk.type.Manipulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.vmware.vmbk.control.VmbkVersion;

public class VddkConfManipulator extends PropertiesFileManipulator {
    private final String vddkConfFile;

    public VddkConfManipulator(final String vddkConfFile) throws IOException {
	super(vddkConfFile);
	this.vddkConfFile = vddkConfFile;
    }

// TODO Remove unused code found by UCDetector
//     public VddkConfManipulator(final String vddkConfFile, final byte[] encoded) throws IOException {
// 	super(encoded);
// 	this.vddkConfFile = vddkConfFile;
//     }

    public boolean enablePhoneHome(final boolean enable) {
	final String enableHome = (enable) ? "1" : "0";
	this.fileContent_.put("vixDiskLib.phoneHome.EnablePhoneHome", enableHome);
	if (enable) {
	    this.fileContent_.put("vixDiskLib.phoneHome.ProductName", VmbkVersion.getProductName());
	    this.fileContent_.put("vixDiskLib.phoneHome.ProductVersion", VmbkVersion.getVersion());
	}
	return enable;
    }

    public boolean isNoNfcSession() {
	final String content = this.fileContent_.get("vixDiskLib.transport.hotadd.NoNfcSession");
	if (content == null) {
	    return false;
	}
	final int noNfcSession = Integer.parseInt(content);
	return noNfcSession == 1;
    }

    public String productName(final String productName) {
	return this.fileContent_.put("vixDiskLib.phoneHome.ProductName", productName);
    }

    public String productVersion(final String vmbkVersion) {
	return this.fileContent_.put("vixDiskLib.phoneHome.ProductVersion", vmbkVersion);
    }

    public boolean save() throws IOException {
	final StringBuilder newVDDKString = new StringBuilder();
	for (final String key : this.fileContent_.keySet()) {
	    if (key.trim().startsWith("#")) {
		newVDDKString.append(key);
		newVDDKString.append('\n');
	    } else if (key.trim().length() > 0) {
		newVDDKString.append(key);
		newVDDKString.append("=");
		newVDDKString.append(this.fileContent_.get(key));
		newVDDKString.append('\n');
	    } else {
		newVDDKString.append('\n');
	    }

	}

	final File vddkFile = new File(this.vddkConfFile);
	try (Writer fstream = new OutputStreamWriter(new FileOutputStream(vddkFile), StandardCharsets.UTF_8)) {
	    fstream.write(newVDDKString.toString());
	}
	return true;
    }

    public String setNoNfcSession(final boolean enable) {
	final String noNfcSession = (enable) ? "1" : "0";
	return this.fileContent_.put("vixDiskLib.transport.hotadd.NoNfcSession", noNfcSession);
    }

}
