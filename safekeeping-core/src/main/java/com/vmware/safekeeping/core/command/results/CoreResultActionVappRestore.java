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
package com.vmware.safekeeping.core.command.results;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.AtomicEnum;
import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreVappInteractive;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.control.info.TotalBlocksInfo;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.PowerOperation;
import com.vmware.safekeeping.core.type.enums.phase.RestoreVappPhases;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;

public class CoreResultActionVappRestore extends AbstractCoreResultActionRestore {

	/**
     * 
     */
    private static final long serialVersionUID = -3981434870740580241L;
    private CoreRestoreManagedInfo managedInfo;
	private final AtomicEnum<RestoreVappPhases> phase;
	/**
	 * index of the restore operation within a single restore command used to
	 * automatically assign a vmname when the new vm name include %d inside the name
	 */
	private volatile int numberOfChildVm;

	protected final ConcurrentDoublyLinkedList<CoreResultActionVmRestore> resultActionOnsChildVm;

	private final ConcurrentDoublyLinkedList<ManagedFcoEntityInfo> fcoChildren;

	public CoreResultActionVappRestore(final IFirstClassObject fco, final CoreRestoreOptions options) {
		super(fco, options);
		this.phase = new AtomicEnum<>(RestoreVappPhases.NONE);
		this.resultActionOnsChildVm = new ConcurrentDoublyLinkedList<>();
		this.fcoChildren = new ConcurrentDoublyLinkedList<>();
	}

	/**
	 * @param vApp
	 * @param result
	 */
	public CoreResultActionVappRestore(final IFirstClassObject fco, final CoreRestoreOptions options,
			final List<CoreResultActionVappRestore> result) {
		this(fco, options);
		result.add(this);
	}

	protected CoreResultActionPower actionPowerOn() throws CoreResultActionException {
		final CoreResultActionPower result = new CoreResultActionPower(this);
		result.start();
		result.setRequestedPowerOperation(PowerOperation.powereOn);
		try {

			final VirtualAppManager vApp = getFirstClassObject();
			result.setPowerState(vApp.getPowerState());
			if (!vApp.powerOn()) {
				result.failure(String.format("PowerOn Vm %s failed.", vApp.getName()));
			}
		} catch (final Exception e) {
			result.failure(e);
		} finally {
			result.done();
		}

		return result;

	}

	/**
	 * @return the fcoChildren
	 */
	public ConcurrentDoublyLinkedList<ManagedFcoEntityInfo> getFcoChildren() {
		return this.fcoChildren;
	}

	@Override
	public VirtualAppManager getFirstClassObject() {
		return (VirtualAppManager) super.getFirstClassObject();
	}

	@Override
	public AbstractRestoreVappInteractive getInteractive() {
		return (AbstractRestoreVappInteractive) this.interactive;
	}

	@Override
	public CoreRestoreManagedInfo getManagedInfo() {
		return this.managedInfo;
	}

	/**
	 * @return the numberOfChildVm
	 */
	public int getNumberOfChildVm() {
		return this.numberOfChildVm;
	}

	/**
	 * @return the options
	 */
	@Override
	public CoreRestoreOptions getOptions() {
		return (CoreRestoreOptions) this.options;
	}

	/**
	 * @return the phase
	 */
	public RestoreVappPhases getPhase() {
		return this.phase.get();
	}

	public ConcurrentDoublyLinkedList<CoreResultActionVmRestore> getResultActionOnsChildVm() {
		return this.resultActionOnsChildVm;
	}

	public CoreResultActionVmRestore getResultActionOnsChildVm(final Integer key) {
		return this.resultActionOnsChildVm.toArrayList().get(key);
	}

	@Override
	public OperationState getState() {
		OperationState result = super.getState();
		if ((result == OperationState.SUCCESS)) {
			for (final CoreResultActionVmRestore rab : this.getResultActionOnsChildVm()) {

				if ((result == OperationState.SUCCESS)) {
					OperationState vmResult = OperationState.SUCCESS;
					String vmResultReason = StringUtils.EMPTY;
					switch (rab.getState()) {
					case ABORTED:
						vmResult = OperationState.ABORTED;
						break;
					case FAILED:
						if (vmResult == OperationState.ABORTED) {
							vmResult = OperationState.ABORTED;
						} else {
							vmResult = OperationState.FAILED;
							vmResultReason = rab.getReason();
						}

						break;
					case SKIPPED:
						switch (vmResult) {
						case ABORTED:
							vmResult = OperationState.ABORTED;
							break;
						case FAILED:
							vmResult = OperationState.FAILED;
							vmResultReason = rab.getReason();
							break;
						case STARTED:
							vmResult = OperationState.SKIPPED;
							break;
						case SKIPPED:
							vmResult = OperationState.SKIPPED;
							vmResultReason = rab.getReason();
							break;
						case SUCCESS:
							vmResult = OperationState.SUCCESS;
							break;
						default:
							break;
						}
						break;
					case SUCCESS:
						vmResult = OperationState.SUCCESS;
						break;
					default:
						break;
					}
					result = vmResult;
					setReason(vmResultReason);
				}
			}
		}
		if (isDone() && (getEndDate() == null)) {
			setEndTime();
		}
		return result;

	}

	/**
	 * @return
	 */
	public Collection<TotalBlocksInfo> getTotalDumpsInfo() {
		final ConcurrentDoublyLinkedList<TotalBlocksInfo> result = new ConcurrentDoublyLinkedList<>();
		for (final CoreResultActionVmRestore childVm : this.resultActionOnsChildVm) {
			result.addAll(childVm.getTotalDumpsInfo());
		}
		return result;
	}

	public void setInteractive(final AbstractRestoreVappInteractive interactive) {
		this.interactive = interactive;
	}

	public void setManagedInfo(final CoreRestoreManagedInfo managedInfo) {
		this.managedInfo = managedInfo;
	}

	/**
	 * @param numberOfChildVm the numberOfChildVm to set
	 */
	public void setNumberOfChildVm(final int numberOfChildVm) {
		this.numberOfChildVm = numberOfChildVm;
	}

	/**
	 * @param phase2
	 */
	public void setPhase(final RestoreVappPhases phase) {
		this.phase.set(phase);

	}

}
