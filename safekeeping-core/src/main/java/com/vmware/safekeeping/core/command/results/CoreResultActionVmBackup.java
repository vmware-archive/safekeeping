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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.type.GuestInfoFlags;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.location.CoreVmLocation;

public class CoreResultActionVmBackup extends AbstractCoreResultActionBackupForEntityWithDisks {

	/**
     * 
     */
    private static final long serialVersionUID = -1767391451666726076L;

    private final CopyOnWriteArrayList<CoreResultActionDiskBackup> resultActionsOnDisks;

	private GuestInfoFlags guestFlags;

	private boolean template;

	public CoreResultActionVmBackup(final IFirstClassObject fco, final CoreBackupOptions options) {
		super(fco, options);
		this.resultActionsOnDisks = new CopyOnWriteArrayList<>();
	}

	@Override
	public void addResultActionOnDisk(final AbstractCoreResultDiskBackupRestore ret) {
		this.resultActionsOnDisks.add((CoreResultActionDiskBackup) ret);
	}

	public void disksSuccess() throws CoreResultActionException {
		for (final CoreResultActionDiskBackup rd : getResultActionsOnDisk()) {
			if (rd.isFails()) {
				failure(rd.getReason());
				return;
			}
		}

	}

	@Override
	public OperationState getDiskActionsResult() {
		for (final CoreResultActionDiskBackup rd : getResultActionsOnDisk()) {

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
	public CoreVmLocation getLocations() {
		return (CoreVmLocation) this.locations;
	}

	/**
	 * @return the resultActionsOnDisks
	 */
	public List<CoreResultActionDiskBackup> getResultActionsOnDisk() {
		return this.resultActionsOnDisks;
	}

	@Override
	public OperationState getState() {
		OperationState result = super.getState();
		if ((result == OperationState.SUCCESS)) {
			OperationState diskResult = OperationState.SUCCESS;
			String diskResultReason = StringUtils.EMPTY;
			for (final CoreResultActionDiskBackup radb : getResultActionsOnDisk()) {
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
	 *
	 * @Override public CoreBackupOptions getOptions() { return (CoreBackupOptions)
	 *           this.options; }
	 *
	 *           public synchronized CoreResultActionDiskBackup
	 *           getResultActionOnDisk(final Integer key) { return
	 *           this.resultActionsOnDisks.get(key); }
	 *
	 *           public Map<Integer, CoreResultActionDiskBackup>
	 *           getResultActionsOnDisk() { return this.resultActionsOnDisks; }
	 *
	 *           /**
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

	/**
	 * @param template the template to set
	 */
	public void setTemplate(final boolean template) {
		this.template = template;
	}

}
