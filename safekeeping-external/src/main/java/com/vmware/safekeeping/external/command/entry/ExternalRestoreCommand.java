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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.RestoreCommand;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.AbstractRunnableCommand;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.external.command.interactive.RestoreNoInteractive;
import com.vmware.safekeeping.external.command.interactive.RestoreVappNoInteractive;
import com.vmware.safekeeping.external.command.support.Tasks;
import com.vmware.safekeeping.external.exception.InvalidTask;
import com.vmware.safekeeping.external.result.ResultActionIvdRestore;
import com.vmware.safekeeping.external.result.ResultActionRestore;
import com.vmware.safekeeping.external.result.ResultActionVappRestore;
import com.vmware.safekeeping.external.result.ResultActionVmRestore;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.RestoreOptions;

public class ExternalRestoreCommand extends AbstractCommandWithOptions {
	class RunnableRestore extends AbstractRunnableCommand {

		private final ITarget itarget;
		private final RestoreCommand restoreCmd;

		public RunnableRestore(final ITarget target, final AbstractCoreResultActionRestore ra) {
			super(ra);
			this.itarget = target;
			setName("restore_" + ra.getFcoToString());
			this.restoreCmd = new RestoreCommand(ra);
		}

		@Override
		public AbstractCoreResultActionRestore getResultAction() {
			return (AbstractCoreResultActionRestore) this.ra;
		}

		@Override
		public void run() {
			try {
				this.restoreCmd.actionRestore(this.itarget, getResultAction());
			} catch (final CoreResultActionException e) {
				Utility.logWarning(ExternalRestoreCommand.this.logger, e);
			} finally {
				if (ExternalRestoreCommand.this.logger.isLoggable(Level.INFO)) {
					ExternalRestoreCommand.this.logger.info("ResultActionConnectThread end");
				}
				getResultAction().done();
			}
		}
	}

	public ExternalRestoreCommand(final RestoreOptions options) {
		final CoreRestoreOptions loptions = new CoreRestoreOptions();
		RestoreOptions.convert(options, loptions);
		setOptions(loptions);
	}

	public List<ResultActionRestore> action(final ConnectionManager connectionManager) throws InvalidTask {
		final List<ResultActionRestore> result = new ArrayList<>();
		final ITarget target = connectionManager.getRepositoryTarget();
		if (connectionManager.isConnected()) {
			if (target == null) {
				throw new InvalidTask(Tasks.NO_REPOSITORY_TARGET_ERROR_MESSAGE);
			} else if (target.isEnable()) {
				GlobalFcoProfileCatalog fcoProfileCatalog;
				try {
					fcoProfileCatalog = new GlobalFcoProfileCatalog(target);
				} catch (final IOException e) {
					Utility.logWarning(this.logger, e);
					throw new InvalidTask(Tasks.GLOBAL_PROFILE_ERROR);
				}
				final List<IFirstClassObject> fcoList = getTargetFcoFromRepository(connectionManager,
						fcoProfileCatalog);
				if (fcoList.isEmpty()) {
					throw new InvalidTask(Tasks.NO_VALID_FCO_TARGETS);
				} else {
					int index = 0;
					for (final IFirstClassObject fco : fcoList) {
						AbstractCoreResultActionRestore rar = null;
						if (fco instanceof VirtualAppManager) {
							rar = new CoreResultActionVappRestore(fco, getOptions());
							rar.setInteractive(new RestoreVappNoInteractive((CoreResultActionVappRestore) rar));
						} else if (fco instanceof VirtualMachineManager) {
							rar = new CoreResultActionVmRestore(fco, getOptions());
							rar.setInteractive(new RestoreNoInteractive((CoreResultActionVmRestore) rar, null));
						} else if (fco instanceof ImprovedVirtualDisk) {
							rar = new CoreResultActionIvdRestore(fco, getOptions());
							rar.setInteractive(new RestoreNoInteractive((CoreResultActionIvdRestore) rar, null));
						} else {
							throw new InvalidTask(Tasks.UNSUPPORTED_ENTITY_TYPE + " " + fco.getEntityType().toString());
						}
						++index;
						rar.setRestoreFcoIndex(index);
						final RunnableRestore runnable = new RunnableRestore(connectionManager.getRepositoryTarget(),
								rar);
						runnable.run();
						if (fco instanceof VirtualAppManager) {
							final ResultActionVappRestore rs = new ResultActionVappRestore();
							rs.convert(rar);
							result.add(rs);
						} else if (fco instanceof VirtualMachineManager) {
							final ResultActionVmRestore rs = new ResultActionVmRestore();
							rs.convert(rar);
							result.add(rs);
						} else if (fco instanceof ImprovedVirtualDisk) {
							final ResultActionIvdRestore rs = new ResultActionIvdRestore();
							rs.convert(rar);
							result.add(rs);
						} else {
							throw new InvalidTask(Tasks.UNSUPPORTED_ENTITY_TYPE + " " + fco.getEntityType().toString());
						}
					}
				}
			} else {
				throw new InvalidTask(Tasks.REPOSITORY_NOT_ACTIVE);
			}
		} else {
			throw new InvalidTask(Tasks.NO_VCENTER_CONNECTION);
		}
		return result;
	}

	public Tasks actionAsync(final ConnectionManager connectionManager) {
		final Tasks result = new Tasks();
		final ITarget target = connectionManager.getRepositoryTarget();

		if (connectionManager.isConnected()) {
			if (target == null) {
				result.noRepositoryTargetFailure();
			} else if (target.isEnable()) {
				try {
					final GlobalFcoProfileCatalog fcoProfileCatalog = new GlobalFcoProfileCatalog(target);
					final List<IFirstClassObject> fcoList = getTargetFcoFromRepository(connectionManager,
							fcoProfileCatalog);
					if (fcoList.isEmpty()) {
						result.skipNoValidFcoTargets();
					} else {
						int index = 0;
						for (final IFirstClassObject fco : fcoList) {
							AbstractCoreResultActionRestore rar = null;

							if (fco instanceof VirtualAppManager) {
								rar = new CoreResultActionVappRestore(fco, getOptions());
								rar.setInteractive(new RestoreVappNoInteractive((CoreResultActionVappRestore) rar));
							} else if (fco instanceof VirtualMachineManager) {
								rar = new CoreResultActionVmRestore(fco, getOptions());
								rar.setInteractive(new RestoreNoInteractive((CoreResultActionVmRestore) rar, null));
							} else if (fco instanceof ImprovedVirtualDisk) {
								rar = new CoreResultActionIvdRestore(fco, getOptions());
								rar.setInteractive(new RestoreNoInteractive((CoreResultActionIvdRestore) rar, null));
							} else {
								rar = null;
								result.unsupportedTypeFailure(fco.getEntityType());
							}
							if (rar != null) {
								++index;
								rar.setRestoreFcoIndex(index);
								final RunnableRestore runnable = new RunnableRestore(
										connectionManager.getRepositoryTarget(), rar);
								result.addResultThread(new ResultThread(rar, runnable.getId()));
								ThreadsManager.executor(ThreadType.FCO).execute(runnable);
								result.setState(OperationState.SUCCESS);
							}
						}
					}
				} catch (final IOException e) {
					Utility.logWarning(this.logger, e);
					result.globalProfileFailure();
				}
			} else {
				result.repositoryNotActiveFailure();
			}
		} else {
			result.noVcenterConnectionFailure();
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
	public CoreRestoreOptions getOptions() {
		return (CoreRestoreOptions) this.options;
	}

	@Override
	protected void initialize() {
		// default implementation ignored
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(final CoreRestoreOptions options) {
		this.options = options;
	}

}
