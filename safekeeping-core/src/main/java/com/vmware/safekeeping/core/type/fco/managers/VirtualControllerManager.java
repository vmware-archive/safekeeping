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
package com.vmware.safekeeping.core.type.fco.managers;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.safekeeping.core.type.enums.VmbkVirtualSCSISharing;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualSCSIController;

public class VirtualControllerManager {// implements Comparable<VirtualControllerManager> {

	private int busNumber;

	private VmbkVirtualSCSISharing sharedBus;

	private Integer scsiCtlrUnitNumber;

	private final int ckey;
	private final List<VirtualDiskManager> diskList;
	private final AdapterType type;
	private String deviceInfo;

	public VirtualControllerManager(final AdapterType type, final int ckey, final int busNumber) {
		this.type = type;
		this.ckey = ckey;
		this.busNumber = busNumber;
		this.diskList = new LinkedList<>();
	}

	VirtualControllerManager(final VirtualSCSIController controller) {
		this.type = AdapterType.getAdapterType(controller);
		this.ckey = controller.getKey();
		this.busNumber = controller.getBusNumber();
		this.scsiCtlrUnitNumber = controller.getScsiCtlrUnitNumber();
		this.deviceInfo = controller.getDeviceInfo().getLabel();
		this.busNumber = controller.getBusNumber();
		this.diskList = new LinkedList<>();
	}

	public void add(final VirtualDiskManager diskm) {
		diskm.setController(this);
		this.diskList.add(diskm);
	}

//    @Override
//    public int compareTo(final VirtualControllerManager rht) {
//	if ((this.type == rht.type) || ((this.type != AdapterType.IDE) && (rht.type != AdapterType.IDE))) {
//
//	    return this.busNumber - rht.busNumber;
//	} else {
//	    if (this.type == AdapterType.IDE) {
//		return -1;
//	    } else {
//		return 1;
//	    }
//	}
//    }

	public List<VirtualDeviceConfigSpec> createAll(final VirtualMachineManager virtualMachineManager) {
		final List<VirtualDeviceConfigSpec> specList = new LinkedList<>();

		for (final VirtualDiskManager diskm : this.diskList) {
			final VirtualDeviceConfigSpec diskSpec = diskm.create(virtualMachineManager);

			assert diskSpec != null;
			specList.add(diskSpec);
		}

		return specList;
	}

//    @Override
//    public boolean equals(final Object obj) {
//	if (obj == null) {
//	    return false;
//	}
//
//	if (this.getClass() != obj.getClass()) {
//	    return false;
//	}
//	final VirtualControllerManager mc = (VirtualControllerManager) obj;
//	return compareTo(mc) == 0;
//    }

	/**
	 * Gets the value of the busNumber property.
	 *
	 */
	public int getBusNumber() {
		return this.busNumber;
	}

	public Integer getCkey() {
		return this.ckey;
	}

	public String getDeviceInfo() {
		return this.deviceInfo;
	}

	public int getNumOfDisks() {
		assert this.diskList != null;
		return this.diskList.size();
	}

	/**
	 * Gets the value of the scsiCtlrUnitNumber property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getScsiCtlrUnitNumber() {
		return this.scsiCtlrUnitNumber;
	}

	/**
	 * Gets the value of the sharedBus property.
	 *
	 * @return possible object is {@link VmbkVirtualSCSISharing }
	 *
	 */
	public VmbkVirtualSCSISharing getSharedBus() {
		return this.sharedBus;
	}

	/**
	 * Sets the value of the busNumber property.
	 *
	 */
	public void setBusNumber(final int value) {
		this.busNumber = value;
	}

	public void setDeviceInfo(final String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	/**
	 * Sets the value of the scsiCtlrUnitNumber property.
	 *
	 * @param value allowed object is {@link Integer }
	 *
	 */
	public void setScsiCtlrUnitNumber(final Integer value) {
		this.scsiCtlrUnitNumber = value;
	}

	/**
	 * Sets the value of the sharedBus property.
	 *
	 * @param value allowed object is {@link VmbkVirtualSCSISharing }
	 *
	 */
	public void setSharedBus(final VmbkVirtualSCSISharing value) {
		this.sharedBus = value;
	}

	public String toLog() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format(" VirtualControllerManager  type: %s, ckey: %d, busNumber: %d ", this.type.toString(),
				this.ckey, this.busNumber));
		for (final VirtualDiskManager vdm : this.diskList) {
			sb.append(" - ");
			sb.append(vdm.toString());
		}

		return sb.toString();
	}

	@Override

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("-----VirtualControllerManager----- \n");
		sb.append(
				String.format("type: %s, ckey: %d, busNumber: %d%n", this.type.toString(), this.ckey, this.busNumber));
		for (final VirtualDiskManager vdm : this.diskList) {
			sb.append(vdm.toString());
			sb.append('\n');
		}
		sb.append("----------------------------------------\n");

		return sb.toString();
	}

}
