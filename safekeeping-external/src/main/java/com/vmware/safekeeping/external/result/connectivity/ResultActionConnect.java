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

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectVcenter;
import com.vmware.safekeeping.external.command.support.Task;
import com.vmware.safekeeping.external.result.ResultAction;

public class ResultActionConnect extends ResultAction {

    /**
     * @param actionConnect
     * @return
     */
    public static void convert(final CoreResultActionConnect src, final ResultActionConnect dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);
        try {
            dst.setConnected(src.isConnected());

            if (src.getSubActionConnectSso() != null) {
                final ResultActionConnectSso racs = new ResultActionConnectSso();
                ResultActionConnectSso.convert(src.getSubActionConnectSso(), racs);
                dst.setSubActionConnectSso(racs);
            }
            if (src.getSubActionConnectVCenters() != null) {
                for (final CoreResultActionConnectVcenter _racvc : src.getSubActionConnectVCenters()) {
                    dst.getSubTasksActionConnectVCenters().add(new Task(_racvc));

                }
            }
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private ResultActionConnectSso subActionConnectSso;

    private List<Task> subTasksActionConnectVCenters;

    private String ssoEndPointUrl;
    private boolean connected;

    /**
     *
     */
    public ResultActionConnect() {
        this.subTasksActionConnectVCenters = new LinkedList<>();
    }

    public void convert(final CoreResultActionConnect src) {
        ResultActionConnect.convert(src, this);

    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionConnect.convert((CoreResultActionConnect) src, this);
    }

    /**
     * @return the ssoEndPointUrl
     */
    public String getSsoEndPointUrl() {
        return this.ssoEndPointUrl;
    }

    /**
     * @return the subActionConnectSso
     */
    public ResultActionConnectSso getSubActionConnectSso() {
        return this.subActionConnectSso;
    }

    /**
     * @return the subTasksActionConnectVCenters
     */
    public List<Task> getSubTasksActionConnectVCenters() {
        return this.subTasksActionConnectVCenters;
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(final boolean connected) {
        this.connected = connected;
    }

    /**
     * @param ssoEndPointUrl the ssoEndPointUrl to set
     */
    public void setSsoEndPointUrl(final String ssoEndPointUrl) {
        this.ssoEndPointUrl = ssoEndPointUrl;
    }

    /**
     * @param subActionConnectSso the subActionConnectSso to set
     */
    public void setSubActionConnectSso(final ResultActionConnectSso subActionConnectSso) {
        this.subActionConnectSso = subActionConnectSso;
    }

    /**
     * @param subTasksActionConnectVCenters the subTasksActionConnectVCenters to set
     */
    public void setSubTasksActionConnectVCenters(final List<Task> subTasksActionConnectVCenters) {
        this.subTasksActionConnectVCenters = subTasksActionConnectVCenters;
    }

}
