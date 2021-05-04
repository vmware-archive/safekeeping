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
package com.vmware.safekeeping.external.result.connectivity;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.external.result.ResultAction;

public class ResultActionConnectSso extends ResultAction {

    public static void convert(final CoreResultActionConnectSso src, final ResultActionConnectSso dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);
        dst.setConnected(src.isConnected());
        if (src.getSsoEndPointUrl() != null) {
            dst.setSsoEndPointUrl(src.getSsoEndPointUrl().toString());
        }
        if (src.getToken() != null) {
            dst.setToken(src.getToken());
        }

    }

    private String ssoEndPointUrl;
    private boolean connected;
    private String token;

    public ResultActionConnectSso() {
        // default implementation ignored
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionConnectSso.convert((CoreResultActionConnectSso) src, this);
    }

    public String getSsoEndPointUrl() {
        return this.ssoEndPointUrl;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return this.token;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(final boolean connected) {
        this.connected = connected;
    }

    /**
     * @param endPointUrl
     */
    public void setSsoEndPointUrl(final String endPointUrl) {
        this.ssoEndPointUrl = endPointUrl;

    }

    /**
     * @param element the token to set
     */
    public void setToken(final String element) {
        this.token = element;
    }

}
