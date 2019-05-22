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
package com.vmware.vmbk.soap;

import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.util.Utility;

class TokenRenewThread implements Runnable {

    private static final Logger logger = Logger.getLogger(TokenRenewThread.class.getName());

    static Thread tokenRenewThread(final Sso pscConnection, final Map<String, VimConnection> vimConnections,
	    final Long defaultInterval) {
	final Thread thread = new Thread(new TokenRenewThread(pscConnection, vimConnections, defaultInterval),
		"TokenRenew_" + pscConnection.getHost());
	return thread;
    }

    private final Long interval;
    private final Sso psc;

    private Boolean running;

    private final Map<String, VimConnection> vimConnections;

    private TokenRenewThread(final Sso pscConnection, final Map<String, VimConnection> vimConnections,
	    final Long interval) {
	this.psc = pscConnection;
	this.interval = interval;
	this.running = Boolean.TRUE;
	this.vimConnections = vimConnections;
    }

    public boolean isRunning() {
	final Boolean val;
	synchronized (this.running) {
	    val = this.running;
	}
	return val;
    }

    private void keepAlive() {
	try {
	    final Element token = this.psc.renewToken();
	    for (final VimConnection vim : this.vimConnections.values()) {
		vim.updateHeaderHandlerResolver(token);
		vim.updatePbmHeaderHandlerResolver();
		vim.updateVslmHeaderHandlerResolver();
		vim.updateVapi(this.psc.getSamlBearerToken());
	    }

	} catch (final Exception e) {
	    IoFunction.showWarning(logger, Utility.toString(e));

	}
    }

    @Override
    public void run() {
	synchronized (this.running) {
	    this.running = true;
	}
	try {
	    while (isRunning()) {

		Thread.sleep((this.interval * 3) / 4);
		keepAlive();

	    }
	} catch (final Throwable t) {
	    stop();
	}
    }

    private void stop() {
	synchronized (this.running) {
	    logger.info("TokenRenew stopped");
	}
	this.running = false;
    }
}
