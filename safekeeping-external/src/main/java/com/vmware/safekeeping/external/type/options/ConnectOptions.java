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

import javax.xml.bind.annotation.XmlSeeAlso;

import com.vmware.safekeeping.core.command.options.AbstractCoreBasicConnectOptions;

/**
 * @author mdaneri
 *
 */
@XmlSeeAlso({ CspConnectOptions.class, PscConnectOptions.class })
public class ConnectOptions {
    public static void convert(final ConnectOptions src, final AbstractCoreBasicConnectOptions dst) {
        // , ExtensionOptions eo) {
        if ((src == null) || (dst == null)) {
            return;
        }
        dst.setBase64(src.isBase64());
        dst.setAuthServer(src.getAuthServer());
//		if (src.extensionOperation != null) {
//			eo.setExtensionOperation(src.extensionOperation);
//			dst.setExtension(eo);
//		}
    }

    private boolean base64;

    private String authServer;
//	private ExtensionManagerOperation extensionOperation;

    /**
     *
     */
    public ConnectOptions() {
        // No operation required
    }

    /**
     * @return the authServer
     */
    public String getAuthServer() {
        return this.authServer;
    }

//	public ExtensionManagerOperation getExtensionOperation() {
//		return this.extensionOperation;
//	}

    /**
     * @return the base64
     */
    public boolean isBase64() {
        return this.base64;
    }

    /**
     * @param authServer the authServer to set
     */
    public void setAuthServer(final String authServer) {
        this.authServer = authServer;
    }

    /**
     * @param base64 the base64 to set
     */
    public void setBase64(final boolean base64) {
        this.base64 = base64;
    }

//	public void setExtensionOperation(ExtensionManagerOperation extensionOperation) {
//		this.extensionOperation = extensionOperation;
//	}

}
