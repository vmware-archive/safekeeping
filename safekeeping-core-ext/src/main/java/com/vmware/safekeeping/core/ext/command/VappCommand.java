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
package com.vmware.safekeeping.core.ext.command;

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionPower;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVappList;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.PowerOperation;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VAppConfigFaultFaultMsg;

public abstract class VappCommand extends AbstractCommandWithOptions {

	protected Boolean cbt;
	protected boolean list;
	protected boolean powerOff;
	protected boolean powerOn;
	protected boolean remove;
	protected boolean reboot;

	protected boolean detail;
	protected boolean force;

	protected List<CoreResultActionCbt> actionCbt(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionCbt> result = new ArrayList<>();
		final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : targetList) {
			final CoreResultActionCbt resultAction = new CoreResultActionCbt();
			try {
				resultAction.start();

				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted();
					result.clear();
					result.add(resultAction);
					break;
				} else {
					try {
						if (fco instanceof VirtualAppManager) {
							final VirtualAppManager vAppm = (VirtualAppManager) fco;
							resultAction.setFcoEntityInfo(fco.getFcoInfo());

							resultAction.setPreviousFlagState(vAppm.isChangedBlockTrackingEnabled());
							if (!getOptions().isDryRun()) {
								if (vAppm.setChangeBlockTracking(this.cbt)) {
									resultAction.setFlagState(this.cbt);
								} else {
									resultAction.failure();
								}
							}
						}
					} catch (final Exception e) {
						Utility.logWarning(this.logger, e);
						resultAction.failure(e);
					}
				}
				result.add(resultAction);
			} finally {
				resultAction.done();
			}
		}
		return result;
	}

	protected List<CoreResultActionVappList> actionList(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
		final List<CoreResultActionVappList> result = new ArrayList<>();

		for (final IFirstClassObject fco : targetList) {
			try (final CoreResultActionVappList resultAction = new CoreResultActionVappList()) {
				resultAction.start();
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted();
					result.clear();
					result.add(resultAction);
					break;
				} else {
					if (fco instanceof VirtualAppManager) {
						resultAction.setFcoEntityInfo(fco.getFcoInfo());
						final VirtualAppManager vAppm = (VirtualAppManager) fco;
						resultAction.setvApp(vAppm);

					}
				}
				resultAction.setFcoEntityInfo(fco.getFcoInfo());
				result.add(resultAction);
			}
		}
		return result;
	}

	protected List<CoreResultActionPower> actionPowerOff(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionPower> result = new ArrayList<>();

		final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : targetList) {
			final CoreResultActionPower resultAction = new CoreResultActionPower();
			try {
				resultAction.start();
				resultAction
						.setRequestedPowerOperation((this.force) ? PowerOperation.powerOff : PowerOperation.shutdown);
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted();
					result.clear();
					result.add(resultAction);
					break;
				} else {
					if (fco instanceof VirtualAppManager) {
						resultAction.setFcoEntityInfo(fco.getFcoInfo());
						final VirtualAppManager vAppm = (VirtualAppManager) fco;
						try {
							resultAction.setPowerState(vAppm.getPowerState());
							if (vAppm.getPowerState() == FcoPowerState.poweredOn) {
								if (getOptions().isDryRun()) {
								} else {
									vAppm.powerOff(this.force);
								}
							} else {
								resultAction.skip(String.format("vApp (%s) cannot powerOff", vAppm.getPowerState()));
							}
						} catch (InvalidStateFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
								| VAppConfigFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
								| VimTaskException e) {
							resultAction.failure(e);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
							resultAction.failure(e);
						}
					}
				}
				result.add(resultAction);
			} finally {
				resultAction.done();
			}
		}
		return result;
	}

	protected List<CoreResultActionPower> actionPowerOn(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionPower> result = new ArrayList<>();

		final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : targetList) {
			final CoreResultActionPower resultAction = new CoreResultActionPower();
			try {
				resultAction.start();
				resultAction.setRequestedPowerOperation(PowerOperation.powereOn);
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted();
					result.clear();
					result.add(resultAction);
					break;
				} else {
					if (fco instanceof VirtualAppManager) {
						final VirtualAppManager vAppm = (VirtualAppManager) fco;
						resultAction.setFcoEntityInfo(fco.getFcoInfo());
						if (!getOptions().isDryRun()) {
							try {
								if (!vAppm.powerOn()) {
									resultAction.failure();
								}
							} catch (final Exception e) {
								resultAction.failure(e);
							}
						}
					}
				}
				result.add(resultAction);
			} finally {
				resultAction.done();
			}
		}
		return result;
	}

	protected List<CoreResultActionPower> actionReboot(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionPower> result = new ArrayList<>();

		final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : targetList) {
			final CoreResultActionPower resultAction = new CoreResultActionPower();
			try {
				resultAction.start();
				resultAction
						.setRequestedPowerOperation((this.force) ? PowerOperation.reset : PowerOperation.rebootGuest);
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted("Aborted on user request");

					result.clear();
					result.add(resultAction);
					break;
				} else {
					if (fco instanceof VirtualAppManager) {
						resultAction.setFcoEntityInfo(fco.getFcoInfo());

						final VirtualAppManager vmm = (VirtualAppManager) fco;
						try {
							resultAction.setPowerState(vmm.getPowerState());
							if (vmm.getPowerState() == FcoPowerState.poweredOn) {
								if (!getOptions().isDryRun()) {
									if (this.force) {
										if (!vmm.reset()) {
											resultAction.failure();
										}
									} else {
										if (!vmm.rebootGuest()) {
											resultAction.failure();
										}
									}

								}
							} else {
								resultAction.skip("vApp is not powered on");
							}
						} catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg | CoreResultActionException e) {
							resultAction.failure(e);
							Utility.logWarning(this.logger, e);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
							resultAction.failure(e);
						}
					}
				}
				result.add(resultAction);
			} finally {
				resultAction.done();
			}
		}
		return result;
	}

	protected List<CoreResultActionDestroy> actionRemove(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionDestroy> result = new ArrayList<>();

		final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : targetList) {
			final CoreResultActionDestroy resultAction = new CoreResultActionDestroy();
			try {
				resultAction.start();
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted("Aborted on user request");
					result.clear();
					result.add(resultAction);
					break;
				} else {
					try {
						if (fco instanceof VirtualAppManager) {
							final VirtualAppManager vAppm = (VirtualAppManager) fco;
							resultAction.setFcoEntityInfo(fco.getFcoInfo());
							resultAction.setShutdownRequired(vAppm.getPowerState() == FcoPowerState.poweredOn);
							if (resultAction.isShutdownRequired()) {
								if (!getOptions().isDryRun()) {
									if (this.force) {
										if (!vAppm.powerOff(this.force)) {
											resultAction.failure("vApp cannot be powered off");
											result.add(resultAction);
											continue;
										}
									} else {
										resultAction.skip("vApp has to be powered off");
										result.add(resultAction);
										continue;
									}
								}
							}

							if (getOptions().isDryRun()) {
							} else {
								if (!vAppm.destroy()) {
									resultAction.failure();
								}
							}
						}
					} catch (final Exception e) {
						Utility.logWarning(this.logger, e);
						resultAction.failure(e);
					}
				}
				result.add(resultAction);
			} finally {
				resultAction.done();
			}
		}
		return result;
	}

	@Override
	protected String getLogName() {
		return this.getClass().getName();
	}

	@Override
	public final void initialize() {
		this.options = new CoreBasicCommandOptions();
		this.list = false;
		this.reboot = false;
		this.powerOn = false;
		this.powerOff = false;
		this.force = false;
		this.remove = false;
		this.cbt = null;
		getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vapp);
		this.detail = false;
	}

}
