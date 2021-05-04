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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.ICommand;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectRepository;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.external.result.connectivity.ResultActionDisconnectRepository;
import com.vmware.safekeeping.external.type.ResultThread;

public class ExternalDisconnectTargetCommand implements ICommand, Runnable {

	private CoreResultActionDisconnectRepository radt;
	private final Map<String, ITarget> targets;
	private final ConnectionManager connectionManager;
	private final Logger logger;
	private final String name;

	public ExternalDisconnectTargetCommand(final String name, final ConnectionManager connectionManager,
			final Map<String, ITarget> targets) {
		this.targets = targets;
		this.logger = Logger.getLogger("DisconnectTargetCommand");
		this.name = name;
		this.connectionManager = connectionManager;

	}

	public ResultActionDisconnectRepository disconnect() {
		this.radt = new CoreResultActionDisconnectRepository();
		run();
		final ResultActionDisconnectRepository rs = new ResultActionDisconnectRepository();
		rs.convert(this.radt);
		return rs;
	}

	public ResultThread disconnectAsync() {
		final Thread thread = new Thread(this);
		final ResultThread result = new ResultThread(this.radt, thread.getId());
		thread.setName(ExternalDisconnectTargetCommand.class.getName());
		thread.start();
		return result;
	}

	@Override
	public void run() {
		try {
			this.radt.start();
			if (this.targets.containsKey(this.name)) {
				this.targets.get(this.name).close();
				this.targets.remove(this.name);
				if (this.connectionManager.getRepositoryTarget().getName().equals(this.name)) {
					final String msg = String.format("Dicsonnecting %s it's the default repository.", this.name);
					if (this.logger.isLoggable(Level.INFO)) {
						this.logger.info(msg);
					}
					if (this.targets.isEmpty()) {
						this.connectionManager.setRepositoryTarget(null);
					} else {
						final List<Entry<String, ITarget>> list = new ArrayList<>(this.targets.entrySet());
						this.connectionManager.setRepositoryTarget(list.get(0).getValue());
					}
				}

			} else {
				final String msg = String.format("No target named %s exist", this.name);
				this.radt.failure(msg);
				this.logger.warning(msg);
			}
		} catch (final Exception e) {
			this.radt.failure(e);
			Utility.logWarning(this.logger, e);
		} finally {
			this.radt.done();
		}
	}
}
