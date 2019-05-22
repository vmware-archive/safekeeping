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

import java.util.List;

import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.ID;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceFileBackingInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskSeSparseBackingInfo;

public class VmdkInfo {

    private final AdapterType adapterType;

    private final int busNumber;

    private final long capacityInBytes;

    private final long capacityInKB;

    private final String changeId;

    private String contentId;

    private final int controllerKey;

    private final CryptoKeyId cryptoKeyId;

    private final int deviceKey;

    private final Boolean digestEnabled;

    private final String diskMode;
    private final String diskObjectId;
    private final ID improvedVirtualDiskId;

    private final List<String> ioFilter;

    private final String name;

    private final Boolean thinProvisioned;

    private final int unitNumber;

    private final String uuid;

    private final Boolean writeThrough;

    private BaseConfigInfoDiskFileBackingInfoProvisioningType provisionType;

    VmdkInfo(final int ckey, final AdapterType type, final int busNumber, final ID improvedVirtualDiskId,
	    final VirtualDeviceBackingInfo vdbi, final VirtualDisk diskDev) {

	if (vdbi instanceof VirtualDiskFlatVer2BackingInfo) {
	    final VirtualDiskFlatVer2BackingInfo bi = (VirtualDiskFlatVer2BackingInfo) vdbi;
	    this.changeId = bi.getChangeId();
	    this.uuid = bi.getUuid();
	    this.diskMode = bi.getDiskMode();
	    this.cryptoKeyId = bi.getKeyId();

	    this.thinProvisioned = bi.isThinProvisioned();
	    this.provisionType = (this.thinProvisioned) ? BaseConfigInfoDiskFileBackingInfoProvisioningType.THIN
		    : BaseConfigInfoDiskFileBackingInfoProvisioningType.LAZY_ZEROED_THICK;
	    this.digestEnabled = bi.isDigestEnabled();
	    this.writeThrough = bi.isWriteThrough();
	    this.contentId = bi.getBackingObjectId();
	} else if (vdbi instanceof VirtualDiskSeSparseBackingInfo) {
	    final VirtualDiskSeSparseBackingInfo bi = (VirtualDiskSeSparseBackingInfo) vdbi;
	    this.changeId = bi.getChangeId();
	    this.uuid = bi.getUuid();
	    this.diskMode = bi.getDiskMode();
	    this.cryptoKeyId = bi.getKeyId();
	    this.digestEnabled = bi.isDigestEnabled();
	    this.writeThrough = bi.isWriteThrough();
	    this.contentId = bi.getBackingObjectId();
	    this.thinProvisioned = false;
	    this.provisionType = BaseConfigInfoDiskFileBackingInfoProvisioningType.LAZY_ZEROED_THICK;
	} else {
	    this.changeId = null;
	    this.uuid = null;
	    this.diskMode = null;
	    this.cryptoKeyId = null;
	    this.thinProvisioned = null;
	    this.digestEnabled = null;
	    this.writeThrough = null;
	    this.provisionType = null;
	    this.contentId = null;
	}

	this.name = ((VirtualDeviceFileBackingInfo) vdbi).getFileName();

	this.deviceKey = diskDev.getKey();
	this.controllerKey = ckey;
	this.capacityInKB = diskDev.getCapacityInKB();
	this.adapterType = type;
	this.busNumber = busNumber;
	this.unitNumber = diskDev.getUnitNumber();
	this.ioFilter = diskDev.getIofilter();
	this.diskObjectId = diskDev.getDiskObjectId();
	this.capacityInBytes = diskDev.getCapacityInBytes();
	this.improvedVirtualDiskId = improvedVirtualDiskId;
    }

    public AdapterType getAdapterType() {
	return this.adapterType;
    }

    public int getBusNumber() {
	return this.busNumber;
    }

    public long getCapacity() {
	return this.capacityInKB * 1024;
    }

    public long getCapacityInBytes() {
	return this.capacityInBytes;
    }

    /**
     * Changing Block
     *
     * @return
     */
    public String getChangeId() {
	return this.changeId;
    }

    public int getControllerKey() {
	return this.controllerKey;
    }

    public CryptoKeyId getCryptoKeyId() {
	return this.cryptoKeyId;
    }

    public int getDeviceKey() {
	return this.deviceKey;
    }

    public String getDiskMode() {
	return this.diskMode;
    }

    public String getDiskObjectId() {
	return this.diskObjectId;
    }

    public ID getImprovedVirtualDiskId() {
	return this.improvedVirtualDiskId;
    }

    public List<String> getIoFilter() {
	return this.ioFilter;
    }

    public int getKey() {
	return this.deviceKey;
    }

    public String getName() {
	return this.name;
    }

    /**
     * @return
     */
    public BaseConfigInfoDiskFileBackingInfoProvisioningType getProvisioningType() {
	// TODO Auto-generated method stub
	return this.provisionType;
    }

    public int getUnitNumber() {
	return this.unitNumber;
    }

    public String getUuid() {
	return this.uuid;
    }

    public Boolean isDigestEnabled() {
	return this.digestEnabled;
    }

    public Boolean isThinProvisioned() {
	return this.thinProvisioned;
    }

    public Boolean isWriteThrough() {
	return this.writeThrough;
    }

    @Override
    public String toString() {
	return toString("");
    }

    private String toString(final String suffix) {
	return String.format(
		"VmdkInfo: name:%s, uuid:%s, changeId:%s, "
			+ "key:%d, ckey:%d, capacityInKB:%d,capacityInByte:%d type:%s, "
			+ "busNumber: %d, unitNumber: %d, diskMode: %s" + "%s, cryptoKey: %s, thinProvisioned: %b,"
			+ "digestEnabled: %b, writeThrough: %b, diskObjectId: %s, contentId: %s",
		this.name, this.uuid, this.changeId, this.deviceKey, this.controllerKey, this.capacityInKB,
		this.capacityInBytes, this.adapterType.toString(), this.busNumber, this.unitNumber, this.diskMode,
		suffix, ((this.cryptoKeyId != null) ? this.cryptoKeyId.getKeyId() : ""), this.thinProvisioned,
		this.digestEnabled, this.writeThrough, this.diskObjectId, this.contentId

	);
    }

}
