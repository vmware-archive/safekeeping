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

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.location.CoreIvdLocation;

public class CoreResultActionIvdVirtualBackup extends AbstractCoreResultActionVirtualBackupForEntityWithDisks {
	/**
     * 
     */
    private static final long serialVersionUID = -7662243904068190263L;
    private CoreResultActionDiskVirtualBackup resultActionOnDisk;

	public CoreResultActionIvdVirtualBackup(final IFirstClassObject fco, final CoreVirtualBackupOptions options) {
		super(fco, options);
	}

	@Override
	public void addResultActionOnDisk(final AbstractCoreResultDiskBackupRestore ret) {
		this.resultActionOnDisk = (CoreResultActionDiskVirtualBackup) ret;
	}

	@Override
	public OperationState getDiskActionsResult() {
		return this.resultActionOnDisk.getState();
	}

	@Override
	public ImprovedVirtualDisk getFirstClassObject() {
		return (ImprovedVirtualDisk) super.getFirstClassObject();
	}

	@Override
	public int getNumberOfDisk() {
		return 1;
	}

	public CoreResultActionDiskVirtualBackup getResultActionOnDisk() {
		return this.resultActionOnDisk;
	}

	@Override
	public OperationState getState() {
		OperationState result = super.getState();
		if ((result == OperationState.SUCCESS)) {
			OperationState diskResult = OperationState.SUCCESS;
			String diskResultReason = StringUtils.EMPTY;
			if ((this.resultActionOnDisk != null) && (result == OperationState.SUCCESS)) {
				switch (this.resultActionOnDisk.getState()) {
				case ABORTED:
					diskResult = OperationState.ABORTED;
					break;
				case FAILED:
					if (diskResult == OperationState.ABORTED) {
						diskResult = OperationState.ABORTED;
					} else {
						diskResult = OperationState.FAILED;
						diskResultReason = this.resultActionOnDisk.getReason();
					}
					break;
				case SKIPPED:
					switch (diskResult) {
					case ABORTED:
						diskResult = OperationState.ABORTED;
						break;
					case FAILED:
						diskResult = OperationState.FAILED;
						diskResultReason = this.resultActionOnDisk.getReason();
						break;
					case STARTED:
						diskResult = OperationState.SKIPPED;
						break;
					case SKIPPED:
						diskResult = OperationState.SKIPPED;
						diskResultReason = this.resultActionOnDisk.getReason();
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
		if (isDone() && (getEndDate() == null)) {
			setEndTime();
		}
		return result;
	}

	/**
	 * @param generationProfile the locations to set
	 */
	@Override
	public void setLocations(final GenerationProfile profile) {

		final CoreIvdLocation ivdLocation = new CoreIvdLocation();
		ivdLocation.setDatastoreInfo(profile.getDatastoreInfo());
		ivdLocation.setDatacenterInfo(profile.getDatacenterInfo());

		final String filePath = profile.getDisks().get(0).getRemoteDiskPath();
		ivdLocation.setDatastorePath(StringUtils.substringBeforeLast(filePath, "/"));
		ivdLocation.setVmdkFullPath(filePath);
		ivdLocation.setVmdkFileName(StringUtils.substringAfterLast(filePath, "/"));

		this.locations = ivdLocation;
	}

}
