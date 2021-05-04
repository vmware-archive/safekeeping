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

import java.util.logging.Level;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractConnectCommand;
import com.vmware.safekeeping.core.command.options.AbstractCoreBasicConnectOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.options.CoreCspConnectOptions;
import com.vmware.safekeeping.core.command.options.CorePscConnectOptions;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnect;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.ConnectOptions;
import com.vmware.safekeeping.external.type.options.CspConnectOptions;
import com.vmware.safekeeping.external.type.options.PscConnectOptions;

public class ExternalConnectCommand extends AbstractConnectCommand implements Runnable {

    private CoreResultActionConnect rac;
    private ConnectionManager connectionManager;

    public ExternalConnectCommand() {
        setOptions(new CoreBasicCommandOptions());
    }

    public ExternalConnectCommand(final ConnectOptions options) throws SafekeepingException {
        AbstractCoreBasicConnectOptions basicOptions = null;
        if (options instanceof PscConnectOptions) {
            basicOptions = new CorePscConnectOptions();
            PscConnectOptions.convert((PscConnectOptions) options, (CorePscConnectOptions) basicOptions);
        } else if (options instanceof CspConnectOptions) {
            basicOptions = new CoreCspConnectOptions();
            CspConnectOptions.convert((CspConnectOptions) options, (CoreCspConnectOptions) basicOptions);
        } else {
            throw new SafekeepingException("Unsupported authentication type:" + options.getClass().toString());
        }

        setOptions(basicOptions);
    }

    public ResultActionConnect connect(final ConnectionManager connectionManager) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager connectionManager=" + connectionManager + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        this.connectionManager = connectionManager;
        this.rac = new CoreResultActionConnect();
        run();
        final ResultActionConnect rs = new ResultActionConnect();
        rs.convert(this.rac);

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager - end"); //$NON-NLS-1$
        }
        return rs;

    }

    public ResultThread connectAsync(final ConnectionManager connectionManager) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager connectionManager=" + connectionManager + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        this.connectionManager = connectionManager;
        ResultThread result = null;
        this.rac = new CoreResultActionConnect();
        final Thread thread = new Thread(this);

        result = new ResultThread(this.rac, thread.getId());

        thread.setName(ExternalConnectCommand.class.getName());
        thread.start();

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager - end"); //$NON-NLS-1$
        }
        return result;

    }

    /**
     * @param _racs
     * @param password
     * @return
     * @throws CoreResultActionException
     * @throws SafekeepingException
     */
    public boolean connectSso(final CoreResultActionConnectSso racs, final String password)
            throws CoreResultActionException, SafekeepingException {

        this.connectionManager = new ConnectionManager(getOptions());
        this.connectionManager.connectSso(racs, password, getOptions().isBase64());
        return racs.isConnected();
    }

    /**
     * @return the connectionManager
     */
    public ConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

    @Override
    protected void initialize() {
        // Nothing to do
    }

    @Override
    public void run() {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        try {
            actionConnect(this.connectionManager, this.rac);
        } catch (final CoreResultActionException e) {
            this.logger.severe("<no args> - exception: " + e); //$NON-NLS-1$

            Utility.logWarning(this.logger, e);
            this.rac.failure(e);
        } finally {
            this.rac.done();
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }
}
