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
package com.vmware.vmbk.control;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.util.Utility;

public class Vmbk {

    private static final Logger logger = Logger.getLogger(Vmbk.class.getName());
    private static boolean abort;
    public static String OPERATION_ABORTED_BY_USER = "Operation Aborted by User";

    public synchronized static void abortAnyPendingOperation(final long millis) {
	logger.entering("Vmbk", "abortAnyPendingOperation", millis);
	logger.warning("Abort request triggered");
	Vmbk.abort = true;

	logger.exiting("Vmbk", "abortAnyPendingOperation");
    }

    public synchronized static void cancelAbortRequest() {
	logger.entering("Vmbk", "cancelAbortRequest");
	Vmbk.abort = false;
	logger.warning("Abort request removed");
	logger.exiting("Vmbk", "cancelAbortRequest");
    }

    public synchronized static boolean isAbortTriggered() {
	return abort;
    }

    private ConnectionManager connetionManager;

    private ITarget repositoryTarget;

    public Boolean configure(final String configPath) throws Exception {
	logger.entering(getClass().getName(), "configure", configPath);
	final Boolean configStatus = setupGlobalManager(configPath);
	logger.exiting(getClass().getName(), "configure", configStatus);
	return configStatus;
    }

    public void finalizeApp() {
	logger.entering(getClass().getName(), "finalizeApp");
	if (this.repositoryTarget != null) {
	    this.repositoryTarget.finalize();
	}
	if (this.connetionManager != null) {
	    getConnetionManager().close();
	}
	logger.exiting(getClass().getName(), "finalizeApp");
    }

    public ConnectionManager getConnetionManager() {
	return this.connetionManager;
    }

    public ITarget getRepositoryTarget() {
	return this.repositoryTarget;
    }

    private Boolean setupGlobalManager(final String configPath) throws Exception {
	logger.entering(getClass().getName(), "setupGlobalManager", configPath);
	Boolean result = true;
	final File configFile = new File(configPath);
	GlobalConfiguration.loadConfig(configFile);
	if (!GlobalConfiguration.isEmptyConfig()) {

	    if (this.connetionManager == null) {

		try {
		    this.connetionManager = new ConnectionManager();
		} catch (final MalformedURLException e) {
		    logger.warning(Utility.toString(e));
		    result = false;
		}
	    }
	    if (result) {

		this.repositoryTarget = GlobalConfiguration.getRepositoryTarget();
		if (this.repositoryTarget.isActive()) {
		    result = this.repositoryTarget.initialize();
		} else {
		    IoFunction.showWarning(logger,
			    "Default repository \"%s\" is not active. Please configure the repository.",
			    this.repositoryTarget.getTargetName());
		}
	    }
	} else {
	    result = false;
	}
	logger.exiting(getClass().getName(), "setupGlobalManager", result);
	return result;
    }

}
