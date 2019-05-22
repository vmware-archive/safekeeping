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

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.KeyProviderId;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.soap.VimConnection;

public class VirtualDiskManager implements Comparable<VirtualDiskManager> {
    private final long capacity;

    private VirtualControllerManager controller;

    private final String cryptoKeyId;

    private final String cryptoKeyProviderId;

    private final String datastore;

    private final boolean digestEnabled;

    private final int diskId;

    private final String ioFilter;

    private final int key;
//    private final String pbmProfileId;
    private final LinkedList<String> pbmProfileName;
    private final GenerationProfile profGen;

    // private final boolean thinProvisioned;

    private final int unitNumber;
    private final boolean writeThrough;

    private BaseConfigInfoDiskFileBackingInfoProvisioningType provisionType;

    public VirtualDiskManager(final GenerationProfile prof, final int diskId, final String datastore) {
	this.diskId = diskId;
	this.profGen = prof;
	this.unitNumber = prof.getUnitNumber(this.diskId);
	this.datastore = datastore;
	this.controller = null;
	this.key = this.profGen.getDiskDeviceKey(this.diskId);
	this.capacity = this.profGen.getCapacity(this.diskId);
//	this.thinProvisioned = this.profGen.isThinProvisioned(diskId);
	this.provisionType = this.profGen.getDiskProvisionType(diskId);
	this.digestEnabled = this.profGen.isDigestEnabled(diskId);
	this.writeThrough = this.profGen.isWriteThrough(diskId);
	this.cryptoKeyId = this.profGen.getCryptoKeyId(diskId);
	this.cryptoKeyProviderId = this.profGen.getCryptoKeyProviderId(diskId);
	this.ioFilter = this.profGen.getIoFilter(diskId);
	// this.pbmProfileId = this.profGen.getPbmProfileId(diskId);
	this.pbmProfileName = this.profGen.getDiskPbmProfileName(diskId);
    }

    @Override
    public int compareTo(final VirtualDiskManager rht) {
	final int cmpCtrl = this.controller.compareTo(rht.controller);
	if (cmpCtrl == 0) {
	    return this.unitNumber - rht.unitNumber;
	} else {
	    return cmpCtrl;
	}
    }

    VirtualDeviceConfigSpec create(final VirtualMachineManager vmm) {

	if (this.controller == null) {
	    return null;
	}

	final VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();

	diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
	diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);

	final VirtualDisk vd = new VirtualDisk();

	vd.setCapacityInKB(PrettyNumber.toKiloByte(this.capacity));

	diskSpec.setDevice(vd);
	if (this.pbmProfileName.size() > 0) {
	    final LinkedList<VirtualMachineDefinedProfileSpec> newPbmProfiles = ((VimConnection) vmm.getConnection())
		    .getPbmConnection().getDefinedProfileSpec(this.pbmProfileName);
	    if (newPbmProfiles != null) {
		diskSpec.getProfile().addAll(newPbmProfiles);
	    }
	}

	vd.setKey(this.key);
	vd.setUnitNumber(this.unitNumber);
	vd.setControllerKey(this.controller.getCkey());
	if (StringUtils.isNotEmpty(this.ioFilter)) {
	    vd.getIofilter().add(this.ioFilter);
	}
	final VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
	final String fileName = "[" + this.datastore + "]";
	diskfileBacking.setFileName(fileName);
	diskfileBacking.setDiskMode("persistent");

	diskfileBacking
		.setThinProvisioned(this.provisionType == BaseConfigInfoDiskFileBackingInfoProvisioningType.THIN);
	diskfileBacking.setDigestEnabled(this.digestEnabled);
	diskfileBacking.setWriteThrough(this.writeThrough);

	if (!(this.cryptoKeyId.isEmpty() && this.cryptoKeyProviderId.isEmpty())) {
	    final CryptoKeyId crypto = new CryptoKeyId();
	    crypto.setKeyId(this.cryptoKeyId);
	    final KeyProviderId keyProviderId = new KeyProviderId();
	    keyProviderId.setId(this.cryptoKeyProviderId);
	    crypto.setProviderId(keyProviderId);
	    diskfileBacking.setKeyId(crypto);
	}
	vd.setBacking(diskfileBacking);
	return diskSpec;
    }

    public BaseConfigInfoDiskFileBackingInfoProvisioningType getProvisionType() {
	return this.provisionType;
    }

    protected void setController(final VirtualControllerManager controller) {
	this.controller = controller;
    }

    public void setProvisionType(final BaseConfigInfoDiskFileBackingInfoProvisioningType provisionType) {
	this.provisionType = provisionType;
    }

    @Override
    public String toString() {
	return String.format("VirtualDiskManager: " + "key: %d, unitNumber: %d, capacity: %s, datastore: %s\n",
		this.key, this.unitNumber, PrettyNumber.toString(this.capacity, PrettyNumber.MetricPrefix.giga),
		this.datastore);
    }

}
