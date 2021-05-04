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

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.AuthenticationProviders;

public abstract class AbstractCoreBasicConnectOptions extends CoreBasicCommandOptions {
    /**
     * 
     */
    private static final long serialVersionUID = 8497563935009041843L;
    private boolean base64;
    private boolean connectDefaultTarget;
    private String authServer;
    private int nfcHostPort;
    private boolean connectVslm;

    protected AbstractCoreBasicConnectOptions() {
        this.base64 = false;
        this.authServer = null;
        this.connectVslm = CoreGlobalSettings.connectToVslm();
    }

    protected AbstractCoreBasicConnectOptions(final String authServer) {
        this.base64 = false;
        this.authServer = authServer;
        this.connectVslm = CoreGlobalSettings.connectToVslm();
    }

    public boolean connectDefaultTarget() {
        return this.connectDefaultTarget;
    }

    /**
     * @return the authServer
     */
    public String getAuthServer() {

        return this.authServer;
    }

    public int getNfcHostPort() {
        return this.nfcHostPort;
    }

    /**
     * @return the provider
     */
    public abstract AuthenticationProviders getProvider();

    public boolean isAuthServerAnURL() {
        return (this.authServer.startsWith("https://"));
    }

    /**
     * @return the base64
     */
    public boolean isBase64() {
        return this.base64;
    }

    public boolean isConnectVslm() {
        return this.connectVslm;
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

    public void setConnectDefaultTarget(final boolean connectDefaultTarget) {
        this.connectDefaultTarget = connectDefaultTarget;
    }

    public void setConnectVslm(final boolean connectVslm) {
        this.connectVslm = connectVslm;
    }

    public void setNfcHostPort(final int nfcHostPort) {
        this.nfcHostPort = nfcHostPort;
    }

}
