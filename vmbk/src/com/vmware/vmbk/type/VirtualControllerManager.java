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
package com.vmware.vmbk.type;

import java.util.LinkedList;
import java.util.List;

import com.vmware.vim25.VirtualDeviceConfigSpec;

public class VirtualControllerManager implements Comparable<VirtualControllerManager> {

    private final int busNumber;

    private final int ckey;

    private final List<VirtualDiskManager> diskList;

    private final AdapterType type;

    public VirtualControllerManager(final AdapterType type, final int ckey, final int busNumber) {
	this.type = type;
	this.ckey = ckey;
	this.busNumber = busNumber;
	this.diskList = new LinkedList<>();
    }

    public void add(final VirtualDiskManager diskm) {
	diskm.setController(this);
	this.diskList.add(diskm);
    }

    @Override
    public int compareTo(final VirtualControllerManager rht) {
	if ((this.type == rht.type) || ((this.type != AdapterType.IDE) && (rht.type != AdapterType.IDE))) {

	    return this.busNumber - rht.busNumber;
	} else {
	    if (this.type == AdapterType.IDE) {
		return -1;
	    } else {
		assert rht.type == AdapterType.IDE;
		return 1;
	    }
	}
    }

// TODO Remove unused code found by UCDetector
//     public VirtualDeviceConfigSpec create() {
// 	final VirtualDeviceConfigSpec controllerSpec = new VirtualDeviceConfigSpec();
// 	controllerSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
//
// 	VirtualController ctrl = null;
// 	boolean isScsi = true;
//
// 	switch (this.type) {
// 	case IDE:
// 	    isScsi = false;
// 	    ctrl = new VirtualIDEController();
// 	    break;
// 	case SATA:
// 	    isScsi = false;
// 	    ctrl = new VirtualAHCIController();
// 	    break;
// 	case BUSLOGIC:
// 	    isScsi = true;
// 	    ctrl = new VirtualBusLogicController();
// 	    break;
// 	case LSILOGIC:
// 	    isScsi = true;
// 	    ctrl = new VirtualLsiLogicController();
// 	    break;
// 	case LSILOGICSAS:
// 	    isScsi = true;
// 	    ctrl = new VirtualLsiLogicSASController();
// 	    break;
// 	case PVSCSI:
// 	    isScsi = true;
// 	    ctrl = new ParaVirtualSCSIController();
// 	    break;
//
// 	case NVME:
// 	    isScsi = false;
// 	    ctrl = new VirtualNVMEController();
// 	    break;
//
// 	default:
// 	    return null;
// 	}
//
// 	ctrl.setKey(this.ckey);
// 	ctrl.setBusNumber(this.busNumber);
// 	if (isScsi) {
// 	    assert ctrl instanceof VirtualSCSIController;
// 	    final VirtualSCSIController scsiCtrl = (VirtualSCSIController) ctrl;
// 	    scsiCtrl.setSharedBus(VirtualSCSISharing.NO_SHARING);
// 	}
// 	controllerSpec.setDevice(ctrl);
//
// 	return controllerSpec;
//     }

    List<VirtualDeviceConfigSpec> createAll(final VirtualMachineManager virtualMachineManager) {
	final List<VirtualDeviceConfigSpec> specList = new LinkedList<>();

	for (final VirtualDiskManager diskm : this.diskList) {
	    final VirtualDeviceConfigSpec diskSpec = diskm.create(virtualMachineManager);

	    assert diskSpec != null;
	    specList.add(diskSpec);
	}

	return specList;
    }

    protected Integer getCkey() {
	return new Integer(this.ckey);
    }

    public int getNumOfDisks() {
	assert this.diskList != null;
	return this.diskList.size();
    }

    public void print() {
	System.out.println(toString());
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();

	sb.append(String.format("-----VirtualControllerManager-----\n" + "type: %s, ckey: %d, busNumber: %d\n",
		this.type.toString(), this.ckey, this.busNumber));
	for (final VirtualDiskManager vdm : this.diskList) {
	    sb.append(vdm.toString());
	}
	sb.append("----------------------------------------\n");

	return sb.toString();
    }

}
