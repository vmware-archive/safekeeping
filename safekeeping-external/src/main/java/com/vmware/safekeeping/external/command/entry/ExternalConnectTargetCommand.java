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

import java.util.Map;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.ICommand;
import com.vmware.safekeeping.core.command.options.CoreAwsS3TargetOptions;
import com.vmware.safekeeping.core.command.options.CoreFileTargetOptions;
import com.vmware.safekeeping.core.command.results.connectivity.AbstractCoreResultActionConnectRepository;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectAwsS3Repository;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectFileRepository;
import com.vmware.safekeeping.core.control.target.AwsS3Target;
import com.vmware.safekeeping.core.control.target.FileTarget;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.external.exception.InvalidTask;
import com.vmware.safekeeping.external.result.connectivity.AbstractResultActionConnectRepository;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnectAwsS3Repository;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnectFileRepository;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.AwsS3RepositoryOptions;
import com.vmware.safekeeping.external.type.options.FileRepositoryOptions;
import com.vmware.safekeeping.external.type.options.RepositoryOptions;

public class ExternalConnectTargetCommand implements ICommand, Runnable {

	private AbstractCoreResultActionConnectRepository ract;
	private final Map<String, ITarget> targets;
	private final ConnectionManager connectionManager;
	private final Logger logger;
	private final RepositoryOptions options;

	public ExternalConnectTargetCommand(final RepositoryOptions options, final ConnectionManager connectionManager,
			final Map<String, ITarget> targets) {
		this.targets = targets;
		this.logger = Logger.getLogger("ConnectTargetCommand");
		this.options = options;
		this.connectionManager = connectionManager;

	}

	public AbstractResultActionConnectRepository connect() throws InvalidTask {
		convertOption();
		run();
		if (this.ract instanceof CoreResultActionConnectAwsS3Repository) {
			final ResultActionConnectAwsS3Repository rs = new ResultActionConnectAwsS3Repository();
			rs.convert(this.ract);
			return rs;
		} else if (this.ract instanceof CoreResultActionConnectFileRepository) {
			final ResultActionConnectFileRepository rs = new ResultActionConnectFileRepository();
			rs.convert((CoreResultActionConnectFileRepository) this.ract);
			return rs;
		} else {
			throw new InvalidTask("Unsupported type Target type");
		}

	}

	public ResultThread connectAsync() throws InvalidTask {
		convertOption();
		final Thread thread = new Thread(this);
		final ResultThread result = new ResultThread(this.ract, thread.getId());
		thread.setName(ExternalConnectTargetCommand.class.getName());
		thread.start();

		return result;

	}

	private void convertOption() throws InvalidTask {
		if (this.options instanceof AwsS3RepositoryOptions) {
			final CoreAwsS3TargetOptions core = new CoreAwsS3TargetOptions();
			AwsS3RepositoryOptions.convert((AwsS3RepositoryOptions) this.options, core);
			this.ract = new CoreResultActionConnectAwsS3Repository();
			this.ract.setOptions(core);
			this.ract.setTarget(new AwsS3Target(core));

		} else if (this.options instanceof FileRepositoryOptions) {
			final CoreFileTargetOptions core = new CoreFileTargetOptions();
			FileRepositoryOptions.convert((FileRepositoryOptions) this.options, core);
			this.ract = new CoreResultActionConnectFileRepository();
			this.ract.setOptions(core);
			this.ract.setTarget(new FileTarget(core));

		} else {
			throw new InvalidTask("Unsupported type Target type");
		}
	}

	@Override
	public void run() {
		try {
			this.ract.start();
			this.ract.setConnected(this.ract.getTarget().open());
			if (this.ract.isConnected()) {
				this.targets.put(this.ract.getOptions().getName(), this.ract.getTarget());
				if ((this.ract.getOptions().isActive() || (this.connectionManager.getRepositoryTarget() == null))
						&& this.ract.isConnected()) {
					this.connectionManager.setRepositoryTarget(this.ract.getTarget());
				}
			} else {
				this.ract.failure("Connection failed");
			}

		} catch (final CoreResultActionException | SafekeepingConnectionException e) {
			this.ract.failure(e);
			Utility.logWarning(this.logger, e);
		} finally {
			this.ract.done();
		}
	}

}
