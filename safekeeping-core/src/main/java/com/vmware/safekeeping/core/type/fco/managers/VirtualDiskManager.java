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
package com.vmware.safekeeping.core.type.fco.managers;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.RuntimeFaultFaultMsg;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VirtualDeviceBackingInfoType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.KeyProviderId;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskSeSparseBackingInfo;

public class VirtualDiskManager {
    private static final Logger logger = Logger.getLogger(VirtualDiskManager.class.getName());
    private final long capacity;
    private VirtualControllerManager controller;

    private final String cryptoKeyId;

    private final String cryptoKeyProviderId;

    private final String datastore;

    private final boolean digestEnabled;

    private final int diskId;

    private final String ioFilter;

    private final int key;
    private final String pbmProfileName;

    private final int unitNumber;
    private final boolean writeThrough;

    private FileBackingInfoProvisioningType provisionType;
    private final VirtualDiskModeType diskMode;
    private final String uuid;
    private final VirtualDeviceBackingInfoType virtualDeviceBackingInfo;

    public VirtualDiskManager(final GenerationProfile prof, final int diskId, final String datastore) {
        this.diskId = diskId;
        this.datastore = datastore;
        this.controller = null;
        final DiskProfile disk = prof.getDisks().get(diskId);
        this.unitNumber = disk.getUnitNumber();
        this.key = disk.getDeviceKey();
        this.capacity = disk.getCapacity();

        this.provisionType = disk.getProvisioningType();
        this.digestEnabled = disk.isDigestEnabled();
        this.writeThrough = disk.isWriteThrough();
        this.cryptoKeyId = disk.getCryptoKeyId();
        this.cryptoKeyProviderId = disk.getCryptoKeyProviderId();
        this.ioFilter = disk.getIoFilter();
        this.diskMode = disk.getDiskMode();
        this.pbmProfileName = disk.getPbmProfile().getPbmProfileName();
        this.uuid = disk.getUuid();
        this.virtualDeviceBackingInfo = disk.getVirtualDeviceBackingInfo();
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
        if (!this.pbmProfileName.isEmpty()) {
            try {
                diskSpec.getProfile()
                        .add(vmm.getVimConnection().getPbmConnection().getDefinedProfileSpec(this.pbmProfileName));
            } catch (InvalidArgumentFaultMsg | RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
        vd.setKey(this.key);
        vd.setUnitNumber(this.unitNumber);
        vd.setControllerKey(this.controller.getCkey());
        if (StringUtils.isNotEmpty(this.ioFilter)) {
            vd.getIofilter().add(this.ioFilter);
        }

        switch (this.virtualDeviceBackingInfo) {

        case VirtualDiskSeSparseBackingInfo:
            final VirtualDiskSeSparseBackingInfo diskSeSparseBacking = new VirtualDiskSeSparseBackingInfo();
            diskSeSparseBacking.setFileName("[" + this.datastore + "]");
            diskSeSparseBacking.setDiskMode(this.diskMode.toString());
            diskSeSparseBacking.setUuid(this.uuid);
            diskSeSparseBacking.setDigestEnabled(this.digestEnabled);
            diskSeSparseBacking.setWriteThrough(this.writeThrough);
            if (StringUtils.isNotEmpty(this.cryptoKeyId) && StringUtils.isNotEmpty(this.cryptoKeyProviderId)) {
                final CryptoKeyId crypto = new CryptoKeyId();
                crypto.setKeyId(this.cryptoKeyId);
                final KeyProviderId keyProviderId = new KeyProviderId();
                keyProviderId.setId(this.cryptoKeyProviderId);
                crypto.setProviderId(keyProviderId);
                diskSeSparseBacking.setKeyId(crypto);
            }
            vd.setBacking(diskSeSparseBacking);
            break;

        case VirtualDiskFlatVer2BackingInfo:
            final VirtualDiskFlatVer2BackingInfo diskFlatBacking = new VirtualDiskFlatVer2BackingInfo();
            diskFlatBacking.setFileName("[" + this.datastore + "]");
            diskFlatBacking.setDiskMode(this.diskMode.toString());
            diskFlatBacking.setUuid(this.uuid);
            diskFlatBacking.setThinProvisioned(this.provisionType == FileBackingInfoProvisioningType.THIN);
            diskFlatBacking.setDigestEnabled(this.digestEnabled);
            diskFlatBacking.setWriteThrough(this.writeThrough);
            if (StringUtils.isNotEmpty(this.cryptoKeyId) && StringUtils.isNotEmpty(this.cryptoKeyProviderId)) {
                final CryptoKeyId crypto = new CryptoKeyId();
                crypto.setKeyId(this.cryptoKeyId);
                final KeyProviderId keyProviderId = new KeyProviderId();
                keyProviderId.setId(this.cryptoKeyProviderId);
                crypto.setProviderId(keyProviderId);
                diskFlatBacking.setKeyId(crypto);
            }
            vd.setBacking(diskFlatBacking);
            break;
        case BaseConfigInfoFileBackingInfo:

            break;
        case VirtualDiskRawDiskMappingVer1BackingInfo:
        case VirtualDiskSparseVer1BackingInfo:
        case VirtualDiskFlatVer1BackingInfo:
        case VirtualDiskSparseVer2BackingInfo:

        default:
            break;

        }

        return diskSpec;
    }

    /**
     * @return the diskId
     */
    public int getDiskId() {
        return this.diskId;
    }

    public FileBackingInfoProvisioningType getProvisionType() {
        return this.provisionType;
    }

    protected void setController(final VirtualControllerManager controller) {
        this.controller = controller;
    }

    public void setProvisionType(final FileBackingInfoProvisioningType provisionType) {
        this.provisionType = provisionType;
    }

    @Override
    public String toString() {
        return String.format("VirtualDiskManager: " + "key: %d, unitNumber: %d, capacity: %s, datastore: %s", this.key,
                this.unitNumber, PrettyNumber.toString(this.capacity, PrettyNumber.MetricPrefix.GIGA, 2),
                this.datastore);
    }

}
