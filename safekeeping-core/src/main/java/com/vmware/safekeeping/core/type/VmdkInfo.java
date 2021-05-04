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
package com.vmware.safekeeping.core.type;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VirtualDeviceBackingInfoType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.ID;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualDeviceFileBackingInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskSeSparseBackingInfo;

public class VmdkInfo {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(VmdkInfo.class.getName());

    private final AdapterType adapterType;

    private final int busNumber;

    private final long capacityInBytes;

    private final String changeId;

    private String contentId;

    private final int controllerKey;

    private final CryptoKeyId cryptoKeyId;

    private final int deviceKey;

    private final Boolean digestEnabled;

    private final VirtualDiskModeType diskMode;
    private final String diskObjectId;
    private final ID vDiskId;

    private final List<String> iofilter;

    private final String remoteDiskPath;

    private final Boolean thinProvisioned;

    private final int unitNumber;

    private final String uuid;

    private final Boolean writeThrough;

    private final FileBackingInfoProvisioningType provisionType;
    private final Integer diskId;
    private VirtualDeviceBackingInfoType virtualDeviceBackingInfo;

    private ManagedObjectReference datastore;

    public VmdkInfo(final Integer diskId, final AdapterType type, final int busNumber,
            final VirtualDeviceFileBackingInfo vdbi, final VirtualDisk diskDev) {

        if (vdbi instanceof VirtualDiskFlatVer2BackingInfo) {
            final VirtualDiskFlatVer2BackingInfo bi = (VirtualDiskFlatVer2BackingInfo) vdbi;
            this.virtualDeviceBackingInfo = VirtualDeviceBackingInfoType.VirtualDiskFlatVer2BackingInfo;
            this.changeId = bi.getChangeId();
            this.uuid = bi.getUuid();
            this.diskMode = VirtualDiskModeType.parse(bi.getDiskMode());
            this.cryptoKeyId = bi.getKeyId();
            this.thinProvisioned = bi.isThinProvisioned();
            this.provisionType = (Boolean.TRUE.equals(this.thinProvisioned)) ? FileBackingInfoProvisioningType.THIN
                    : FileBackingInfoProvisioningType.LAZY_ZEROED_THICK;
            this.digestEnabled = bi.isDigestEnabled();
            this.writeThrough = bi.isWriteThrough();
            this.contentId = bi.getBackingObjectId();
        } else if (vdbi instanceof VirtualDiskSeSparseBackingInfo) {
            final VirtualDiskSeSparseBackingInfo bi = (VirtualDiskSeSparseBackingInfo) vdbi;
            this.virtualDeviceBackingInfo = VirtualDeviceBackingInfoType.VirtualDiskSeSparseBackingInfo;
            this.changeId = bi.getChangeId();
            this.uuid = bi.getUuid();
            this.diskMode = VirtualDiskModeType.parse(bi.getDiskMode());
            this.cryptoKeyId = bi.getKeyId();
            this.digestEnabled = bi.isDigestEnabled();
            this.writeThrough = bi.isWriteThrough();
            this.contentId = bi.getBackingObjectId();
            this.thinProvisioned = false;
            this.provisionType = FileBackingInfoProvisioningType.LAZY_ZEROED_THICK;
        } else {

            logger.warning("Unsupported " + vdbi.getClass().getName() + " VirtualDeviceBackingInfo"); //$NON-NLS-1$
            this.virtualDeviceBackingInfo = null;
            this.changeId = null;
            this.uuid = null;
            this.diskMode = null;
            this.cryptoKeyId = null;
            this.digestEnabled = null;
            this.writeThrough = null;
            this.contentId = null;
            this.thinProvisioned = false;
            this.provisionType = FileBackingInfoProvisioningType.THIN;
        }

        this.remoteDiskPath = vdbi.getFileName();
        this.setDatastore(vdbi.getDatastore());
        this.deviceKey = diskDev.getKey();
        this.controllerKey = diskDev.getControllerKey();
        this.adapterType = type;
        this.busNumber = busNumber;
        this.unitNumber = diskDev.getUnitNumber();
        this.iofilter = diskDev.getIofilter();
        this.diskObjectId = diskDev.getDiskObjectId();
        this.capacityInBytes = diskDev.getCapacityInBytes();
        this.vDiskId = diskDev.getVDiskId();
        this.diskId = diskId;
    }

    public AdapterType getAdapterType() {
        return this.adapterType;
    }

    public int getBusNumber() {
        return this.busNumber;
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

    /**
     * @return the contentId
     */
    public String getContentId() {
        return this.contentId;
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

    /**
     * @return the diskId
     */
    public Integer getDiskId() {
        return this.diskId;
    }

    public VirtualDiskModeType getDiskMode() {
        return this.diskMode;
    }

    public String getDiskObjectId() {
        return this.diskObjectId;
    }

    public List<String> getIofilter() {
        return this.iofilter;
    }

    public FileBackingInfoProvisioningType getProvisioningType() {
        return this.provisionType;
    }

    public String getRemoteDiskPath() {
        return this.remoteDiskPath;
    }

    public int getUnitNumber() {
        return this.unitNumber;
    }

    public String getUuid() {
        return this.uuid;
    }

    public ID getVDiskId() {
        return this.vDiskId;
    }

    /**
     * @return the virtualDeviceBackingInfo
     */
    public VirtualDeviceBackingInfoType getVirtualDeviceBackingInfo() {
        return this.virtualDeviceBackingInfo;
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

    /**
     * @param virtualDeviceBackingInfo the virtualDeviceBackingInfo to set
     */
    public void setVirtualDeviceBackingInfo(final VirtualDeviceBackingInfoType virtualDeviceBackingInfo) {
        this.virtualDeviceBackingInfo = virtualDeviceBackingInfo;
    }

    @Override
    public String toString() {
        return toString("");
    }

    private String toString(final String suffix) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - start"); //$NON-NLS-1$
        }

        String returnString = String.format(
                "VmdkInfo: name:%s, uuid:%s, changeId:%s, " + "key:%d, ckey:%d, capacityInByte:%d type:%s, "
                        + "busNumber: %d, unitNumber: %d, diskMode: %s" + "%s, cryptoKey: %s, thinProvisioned: %b,"
                        + "digestEnabled: %b, writeThrough: %b, diskObjectId: %s, contentId: %s",
                this.remoteDiskPath, this.uuid, this.changeId, this.deviceKey, this.controllerKey, this.capacityInBytes,
                this.adapterType.toString(), this.busNumber, this.unitNumber, this.diskMode, suffix,
                ((this.cryptoKeyId != null) ? this.cryptoKeyId.getKeyId() : ""), this.thinProvisioned,
                this.digestEnabled, this.writeThrough, this.diskObjectId, this.contentId

        );
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public ManagedObjectReference getDatastore() {
        return datastore;
    }

    public void setDatastore(ManagedObjectReference datastore) {
        this.datastore = datastore;
    }

}
