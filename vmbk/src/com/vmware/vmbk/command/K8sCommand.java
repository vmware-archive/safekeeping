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
package com.vmware.vmbk.command;

import java.util.List;

import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.soap.ConnectionManager;

public class K8sCommand extends CommandWithOptions {

    protected boolean list;

    protected K8sCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	if (this.list) {
	    action_List(connetionManager);
	}
	logger.exiting(getClass().getName(), "action");
    }

    private void action_List(final ConnectionManager connectionManager) {
	logger.entering(getClass().getName(), "action_List", connectionManager);
	final List<String> namespaces = connectionManager.getArachneConnection().list();
	IoFunction.showInfo(logger, "Kubernetes Namespaces:");
	for (final String namespace : namespaces) {
	    IoFunction.showInfo(logger, "\t%s", namespace);
	}

	logger.exiting(getClass().getName(), "action_List");
    }

    @Override
    public void initialize() {
	logger.entering(getClass().getName(), "initialize");
	super.initialize();
	this.list = false;
	logger.exiting(getClass().getName(), "initialize");

    }

}
