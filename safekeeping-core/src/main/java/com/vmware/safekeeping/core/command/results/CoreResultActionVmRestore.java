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
package com.vmware.safekeeping.core.command.results;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.type.GuestInfoFlags;
import com.vmware.safekeeping.core.type.enums.PowerOperation;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public class CoreResultActionVmRestore extends AbstractCoreResultActionRestoreForEntityWithDisks {

	/**
     * 
     */
    private static final long serialVersionUID = 7014645079465806987L;
    private final ConcurrentDoublyLinkedList<CoreResultActionDiskRestore> resultActionsOnDisks;
	private CoreRestoreManagedInfo managedInfo;

	/**
	 * index of the restore operation within a single restore command used to
	 * automatically assign a vmname when the new vm name include %d inside the name
	 */
	private volatile int numberOfChildVm;
	private GuestInfoFlags guestFlags;
	private boolean template;
	private volatile int numberOfDisk;
	protected final ConcurrentDoublyLinkedList<CoreResultActionVmRestore> resultActionOnsChildVm;

	public CoreResultActionVmRestore(final IFirstClassObject fco, final CoreRestoreOptions options) {
		super(fco, options);
		this.resultActionsOnDisks = new ConcurrentDoublyLinkedList<>();
		this.resultActionOnsChildVm = new ConcurrentDoublyLinkedList<>();
	}

//    /**
//     * @param virtualMachineManager
//     * @param rar
//     */
//    public CoreResultActionVmRestore(final IFirstClassObject fco, final CoreRestoreOptions options,
//            final CoreResultActionVappRestore parent) {
//	this(fco, options);
//	setParent(parent);
//	parent.getResultActionOnsChildVm().add(this);
//    }

	/**
	 * @param vApp
	 * @param result
	 */
	public CoreResultActionVmRestore(final IFirstClassObject fco, final CoreRestoreOptions options,
			final List<CoreResultActionVmRestore> result) {
		this(fco, options);
		result.add(this);
	}

	protected CoreResultActionPower actionPowerOn() throws Exception {
		final CoreResultActionPower resultAction = new CoreResultActionPower(this);

		resultAction.start();
		resultAction.setRequestedPowerOperation(PowerOperation.powereOn);
		try {

			final VirtualMachineManager vmm = getFirstClassObject();
			resultAction.setPowerState(vmm.getPowerState());
			if (!vmm.powerOn(getManagedInfo().getHostInfo())) {
				resultAction.failure(String.format("PowerOn Vm %s failed.", vmm.getName()));
			}
		} catch (final Exception e) {
			resultAction.failure(e);
		} finally {
			resultAction.done();
		}
		return resultAction;

	}

	@Override
	public void addResultActionOnDisk(final AbstractCoreResultDiskBackupRestore ret) {
		this.resultActionsOnDisks.add((CoreResultActionDiskRestore) ret);
	}

	@Override
	public OperationState getDiskActionsResult() {
		for (final CoreResultActionDiskRestore rd : getResultActionsOnDisk()) {

			switch (rd.getState()) {
			case ABORTED:
				return OperationState.ABORTED;
			case FAILED:
				return OperationState.FAILED;
			case QUEUED:
				return OperationState.STARTED;
			case STARTED:
				return OperationState.STARTED;
			case SKIPPED:
			case SUCCESS:
			default:
				break;
			}
		}
		return OperationState.SUCCESS;

	}

	@Override
	public VirtualMachineManager getFirstClassObject() {
		return (VirtualMachineManager) super.getFirstClassObject();
	}

	/**
	 * @return the guestFlags
	 */
	public GuestInfoFlags getGuestFlags() {
		return this.guestFlags;
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

//    /**
//     *
//     * // * @return the restoreFcoIndex //
//     */
//    public Integer getRestoreFcoIndex() {
//	return this.restoreFcoIndex;
//    }

	/**
	 * @return the numberOfDisk
	 */
	@Override
	public int getNumberOfDisk() {
		return this.numberOfDisk;
	}

	public AbstractCoreResultActionDiskVirtualBackupAndRestore getResultActionOnDisk(final Integer key) {
		return this.resultActionsOnDisks.toArrayList().get(key);
	}

	public ConcurrentDoublyLinkedList<CoreResultActionVmRestore> getResultActionOnsChildVm() {
		return this.resultActionOnsChildVm;
	}

	public CoreResultActionVmRestore getResultActionOnsChildVm(final Integer key) {
		return this.resultActionOnsChildVm.toArrayList().get(key);
	}

	public ConcurrentDoublyLinkedList<CoreResultActionDiskRestore> getResultActionsOnDisk() {
		return this.resultActionsOnDisks;
	}

	@Override
	public OperationState getState() {
		OperationState result = super.getState();
		if ((result == OperationState.SUCCESS) || (result == OperationState.STARTED)) {
			OperationState diskResult = OperationState.SUCCESS;
			String diskResultReason = StringUtils.EMPTY;
			for (final AbstractCoreResultActionDiskVirtualBackupAndRestore radb : getResultActionsOnDisk()) {
				if ((result == OperationState.SUCCESS)) {
					switch (radb.getState()) {
					case ABORTED:
						diskResult = OperationState.ABORTED;
						break;
					case FAILED:
						if (diskResult == OperationState.ABORTED) {
							diskResult = OperationState.ABORTED;
						} else {
							diskResult = OperationState.FAILED;
							diskResultReason = radb.getReason();
						}
						break;
					case SKIPPED:
						switch (diskResult) {
						case ABORTED:
							diskResult = OperationState.ABORTED;
							break;
						case FAILED:
							diskResult = OperationState.FAILED;
							diskResultReason = radb.getReason();
							break;
						case STARTED:
							diskResult = OperationState.SKIPPED;
							break;
						case SKIPPED:
							diskResult = OperationState.SKIPPED;
							diskResultReason = radb.getReason();
							break;
						case SUCCESS:
							diskResult = OperationState.SUCCESS;
							break;
						default:
							break;
						}
						break;
					case SUCCESS:
						diskResult = OperationState.SUCCESS;
						break;
					default:
						break;
					}
					result = diskResult;
					setReason(diskResultReason);
				}
			}
		}
		if (isDone() && (getEndDate() == null)) {
			setEndTime();
		}
		return result;
	}

	/**
	 * @return the template
	 */
	public boolean isTemplate() {
		return this.template;
	}

	/**
	 * @param guestFlags the guestFlags to set
	 */
	public void setGuestFlags(final GuestInfoFlags guestFlags) {
		this.guestFlags = guestFlags;
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
	 * @param numberOfDisk the numberOfDisk to set
	 */
	@Override
	public void setNumberOfDisk(final int numberOfDisk) {
		this.numberOfDisk = numberOfDisk;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(final boolean template) {
		this.template = template;
	}

	/**
	 * @param logInfo
	 */
	@Override
	public void success() throws CoreResultActionException {
		for (final AbstractCoreResultActionDiskVirtualBackupAndRestore rd : getResultActionsOnDisk()) {
			if (rd.isFails()) {
				failure(rd.getReason());
				return;
			}
		}
		super.success();

	}

	@Override
	public String toString() {
		return getState().toString() + " " + getFcoToString();

	}
}
