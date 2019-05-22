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

import java.util.logging.Logger;

import com.vmware.pbm.PbmAboutInfo;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.util.Utility;

class KeepAlive implements Runnable {
    private static final Long DEFAULT_INTERVAL = 300000l;

    private static final Logger logger_ = Logger.getLogger(KeepAlive.class.getName());

    static Thread keepAlive(final VimConnection vim) {
	return keepAlive(vim, DEFAULT_INTERVAL);
    }

    private static Thread keepAlive(final VimConnection vim, final Long interval) {
	final Thread thread = new Thread(new KeepAlive(vim, interval), "keepAlive_" + vim.getServerIntanceUuid());
	return thread;
    }

    private final Long interval;

    private Boolean running;

    private final VimConnection vim;

// TODO Remove unused code found by UCDetector
//     public KeepAlive(final BasicVimConnection vim) {
// 	this(vim, DEFAULT_INTERVAL);
//     }

    private KeepAlive(final VimConnection vim, final Long interval) {
	this.vim = vim;
	this.interval = interval;
	this.running = Boolean.TRUE;
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
	    this.vim.keepAlive();

	    final PbmAboutInfo pbmAbout = this.vim.getPbmConnection().getAboutInfo();
	    logger_.info(String.format("keep alive %s : Name %s  Version %s", pbmAbout.getInstanceUuid(),
		    pbmAbout.getName(), pbmAbout.getVersion()));
	    this.vim.getVapiService().keepAlive();
	} catch (final RuntimeFaultFaultMsg e) {
	    IoFunction.showWarning(logger_, Utility.toString(e));
	} catch (final Exception e) {
	    IoFunction.showWarning(logger_, Utility.toString(e));
	    stop();
	}

    }

    @Override
    public void run() {
	synchronized (this.running) {
	    this.running = true;
	}
	try {
	    while (isRunning()) {

		keepAlive();
		Thread.sleep(this.interval);
	    }
	} catch (final Throwable t) {
	    stop();
	}
    }

    private void stop() {
	synchronized (this.running) {
	    logger_.info("keep alive stopped");
	}
	this.running = false;
    }
}
