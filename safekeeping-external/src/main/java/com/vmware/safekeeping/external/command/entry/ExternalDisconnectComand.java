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
package com.vmware.safekeeping.external.command.entry;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.DisconnectCommand;
import com.vmware.safekeeping.core.command.options.CoreArchiveOptions;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectSso;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.external.result.connectivity.ResultActionDisconnect;
import com.vmware.safekeeping.external.type.ResultThread;

public class ExternalDisconnectComand extends DisconnectCommand implements Runnable {
    private CoreResultActionDisconnect rac;

    private ConnectionManager connectionManager;

    public ResultActionDisconnect action(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.rac = new CoreResultActionDisconnect();

        run();
        final ResultActionDisconnect rs = new ResultActionDisconnect();
        rs.convert(this.rac);
        return rs;
    }

    public ResultThread actionAsync(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        ResultThread result = null;
        this.rac = new CoreResultActionDisconnect();
        final Thread thread = new Thread(this);
        result = new ResultThread(this.rac, thread.getId());

        thread.setName(ExternalDisconnectComand.class.getName());
        thread.start();

        return result;
    }

    @Override
    public void initialize() {
        setOptions(new CoreArchiveOptions());
    }

    /**
     * @param connectionManager
     * @return
     * @return
     * @throws CoreResultActionException
     */
    public boolean actionEndSsoSession(final ConnectionManager connectionManager,
            final CoreResultActionDisconnectSso rads) throws CoreResultActionException {
        this.connectionManager = connectionManager;

        connectionManager.disconnectSsoConnections(rads);

        return !rads.isConnected();
    }

    @Override
    public void run() {
        try {
            actionDisconnect(this.connectionManager, this.rac);
        } catch (final CoreResultActionException e) {
            Utility.logWarning(this.logger, e);
            this.rac.failure(e);
        } finally {
            this.rac.done();
        }
    }
}
