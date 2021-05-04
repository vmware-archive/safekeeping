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
package com.vmware.safekeeping.external.command.entry;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.options.CoreArchiveOptions;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.type.AbstractRunnableCommand;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.external.command.ArchiveCheckGenerationsCommand;
import com.vmware.safekeeping.external.command.support.Tasks;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.archive.ArchiveCheckGenerationsOptions;

public class ExternalArchiveCommandCheckGenerations extends AbstractCommandWithOptions {
	class RunnableArchiveRemoveGenerations extends AbstractRunnableCommand {
		private final ITarget itarget;
		private final ArchiveCheckGenerationsCommand archiveCmd;

		private final Logger logger;

		public RunnableArchiveRemoveGenerations(final ITarget target,
				final CoreResultActionArchiveCheckGenerationWithDependencies ra, final Logger logger) {
			super(ra);
			this.itarget = target;
			setName("removeGenerations_" + ra.getFcoToString());
			this.archiveCmd = new ArchiveCheckGenerationsCommand();
			this.archiveCmd.setOptions(getOptions());
			this.logger = logger;
		}

		@Override
		public CoreResultActionArchiveCheckGenerationWithDependencies getResultAction() {
			return (CoreResultActionArchiveCheckGenerationWithDependencies) this.ra;
		}

		@Override
		public void run() {
			try {
				this.archiveCmd.action(this.itarget, getResultAction());
			} catch (final CoreResultActionException e) {
				Utility.logWarning(this.logger, e);
			} finally {
				if (this.logger.isLoggable(Level.INFO)) {
					this.logger.info("ArchiveCommandCheckGenerations end");
				}
				getResultAction().done();
			}
		}
	}

	public ExternalArchiveCommandCheckGenerations(final ArchiveCheckGenerationsOptions options) {
		final CoreArchiveOptions loptions = new CoreArchiveOptions();
		ArchiveCheckGenerationsOptions.convert(options, loptions);
		setOptions(loptions);
	}

	public Tasks action(final ITarget target) {
		final Tasks result = new Tasks();
		if (target == null) {
			result.noRepositoryTargetFailure();
		} else if (target.isEnable()) {
			try {
				final GlobalFcoProfileCatalog profAllFco = new GlobalFcoProfileCatalog(target);
				final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(profAllFco);
				if (!entities.isEmpty()) {
					for (final ManagedFcoEntityInfo fco : entities) {
						final CoreResultActionArchiveCheckGenerationWithDependencies resultAction = new CoreResultActionArchiveCheckGenerationWithDependencies(
								fco, target);
						final RunnableArchiveRemoveGenerations runnable = new RunnableArchiveRemoveGenerations(target,
								resultAction, this.logger);
						result.addResultThread(new ResultThread(resultAction, runnable.getId()));
						ThreadsManager.executor(ThreadType.ARCHIVE).execute(runnable);
						result.setState(OperationState.SUCCESS);
					}

				} else {
					result.setState(OperationState.SKIPPED);
					result.setReason("No valid catalog entity");
				}
			} catch (final IOException e) {
				result.fails(e);
			}
		} else {
			result.setState(OperationState.FAILED);
			result.setReason("Repository is not active");
		}
		return result;
	}

	@Override
	protected String getLogName() {
		return this.getClass().getName();
	}

	/**
	 * @return the options
	 */
	@Override
	public CoreArchiveOptions getOptions() {
		return (CoreArchiveOptions) this.options;
	}

	@Override
	protected void initialize() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(final CoreArchiveOptions options) {
		this.options = options;
	}

}
