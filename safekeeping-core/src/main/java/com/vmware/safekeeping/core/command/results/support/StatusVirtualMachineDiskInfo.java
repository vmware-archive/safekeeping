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
package com.vmware.safekeeping.core.command.results.support;

import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.enums.AdapterType;

public class StatusVirtualMachineDiskInfo extends StatusDiskInfo {

	private int controllerDeviceKey;
	private int diskId;
	private int busNumber;
	private int unitNumber;
	private AdapterType adapterType;

	public StatusVirtualMachineDiskInfo() {
	}

	/**
	 * Constructor for VM
	 *
	 * @param diskId
	 * @param profGen
	 */
	public StatusVirtualMachineDiskInfo(final Integer diskId, final GenerationProfile profGen) {
		super(profGen);
		this.diskId = diskId;
		this.controllerDeviceKey = profGen.getControllerDeviceKey(diskId);
		this.busNumber = profGen.getBusNumber(this.controllerDeviceKey);
		this.adapterType = profGen.getAdapterType(diskId);
		this.unitNumber = profGen.getUnitNumber(this.diskId);
	}

	/**
	 * @return the adapterType
	 */

	public AdapterType getAdapterType() {
		return this.adapterType;
	}

	/**
	 * @return the busNumber
	 */

	public int getBusNumber() {
		return this.busNumber;
	}

	/**
	 *
	 * /**
	 *
	 * @return the controllerDeviceKey
	 */

	public int getControllerDeviceKey() {
		return this.controllerDeviceKey;
	}

	/**
	 * @return the diskId
	 */
	@Override
	public int getDiskId() {
		return this.diskId;
	}

	/**
	 * @return the unitNumber
	 */
	public int getUnitNumber() {
		return this.unitNumber;
	}

	/**
	 * @param adapterType the adapterType to set
	 */

	public void setAdapterType(final AdapterType adapterType) {
		this.adapterType = adapterType;
	}

	/**
	 *
	 *
	 * /**
	 *
	 * @param busNumber the busNumber to set
	 */

	public void setBusNumber(final int busNumber) {
		this.busNumber = busNumber;
	}

	/**
	 *
	 *
	 * /**
	 *
	 * @param controllerDeviceKey the controllerDeviceKey to set
	 */

	public void setControllerDeviceKey(final int controllerDeviceKey) {
		this.controllerDeviceKey = controllerDeviceKey;
	}

	/**
	 * @param diskId the diskId to set
	 */
	public void setDiskId(final int diskId) {
		this.diskId = diskId;
	}

	/**
	 * @param unitNumber the unitNumber to set
	 */
	public void setUnitNumber(final int unitNumber) {
		this.unitNumber = unitNumber;
	}

}
