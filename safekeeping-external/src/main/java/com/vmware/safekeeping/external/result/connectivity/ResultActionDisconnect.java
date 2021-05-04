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
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectVcenter;
import com.vmware.safekeeping.external.command.support.Task;
import com.vmware.safekeeping.external.result.ResultAction;

/**
 * @author mdaneri
 *
 */
public class ResultActionDisconnect extends ResultAction {

    /**
     * @param actionConnect
     * @return
     */
    public static void convert(final CoreResultActionDisconnect src, final ResultActionDisconnect dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);
        try {
            dst.setConnected(src.isConnected());
            for (final CoreResultActionDisconnectVcenter _racvc : src.getSubActionDisconnectVCenters()) {
                dst.getSubTasksActionConnectVCenters().add(new Task(_racvc));
            }
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private List<Task> subTasksActionConnectVCenters;

    private boolean connected;

    /**
     *
     */
    public ResultActionDisconnect() {
        this.subTasksActionConnectVCenters = new LinkedList<>();
    }

    public void convert(final CoreResultActionDisconnect src) {
        ResultActionDisconnect.convert(src, this);
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionDisconnect.convert((CoreResultActionDisconnect) src, this);
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
     * @param subTasksActionConnectVCenters the subTasksActionConnectVCenters to set
     */
    public void setSubTasksActionConnectVCenters(final List<Task> subTasksActionConnectVCenters) {
        this.subTasksActionConnectVCenters = subTasksActionConnectVCenters;
    }

}
