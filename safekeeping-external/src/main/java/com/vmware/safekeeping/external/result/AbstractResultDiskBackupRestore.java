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
package com.vmware.safekeeping.external.result;

import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultDiskBackupRestore;
import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractResultDiskBackupRestore extends ResultAction {
    private static final Logger logger = Logger.getLogger(AbstractResultDiskBackupRestore.class.getName());

    public static void convert(final AbstractCoreResultDiskBackupRestore src,
            final AbstractResultDiskBackupRestore dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        try {
            ResultAction.convert(src, dst);
            dst.setTotalNumberOfDisks(src.getTotalNumberOfDisks());
            dst.setDiskId(src.getDiskId());
            dst.setUuid(src.getUuid());
            dst.setControllerKey(src.getControllerKey());
            dst.setBusNumber(src.getBusNumber());
            dst.setUnitNumber(src.getUnitNumber());
            dst.setUsedTransportModes(src.getUsedTransportModes());
            dst.setFlags(src.getFlags());
            dst.setNumberOfThreads(src.getNumberOfThreads());
            dst.setCapacityInBytes(src.getCapacityInBytes());
            if (src.getDiskHandle() != null) {
                dst.setDiskHandle(src.getDiskHandle().getHandle());
            }
            dst.setvDiskId(src.getvDiskId());
            dst.setTargetName(src.getTargetName());
            dst.setAdapterType(src.getAdapterType());
            dst.setDeviceKey(src.getDeviceKey());
            dst.setVirtualDiskMode(src.getVirtualDiskMode());
            dst.setName(src.getName());
            dst.setProvisioningType(src.getProvisioningType());
            if (src.isDigestEnabled() != null) {
                dst.setDigestEnabled(src.isDigestEnabled());
            }
            dst.setUsedTransportModes(src.getUsedTransportModes());
            dst.setNumberOfBlocks(src.getNumberOfBlocks());
            dst.setMaxBlockSize(src.getMaxBlockSize());
            dst.setMaxBlockSizeInBytes(src.getMaxBlockSizeInBytes());
            dst.setRequestedTransportModes(src.getRequestedTransportModes());

        } catch (final Exception e) {
            Utility.logWarning(AbstractResultDiskBackupRestore.logger, e);
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private Integer numberOfBlocks;
    private int diskId;
    private String uuid;
    private int controllerKey;
    private int busNumber;
    private int unitNumber;
    private Long diskHandle;
    private String targetName;
    private String vDiskId;
    private String usedTransportModes;
    private String requestedTransportModes;

    private int flags;
    private int numberOfThreads;
    private String name;
    private long capacityInBytes;
    private AdapterType adapterType;
    private int deviceKey;
    private VirtualDiskModeType virtualDiskMode;
    private boolean digestEnabled;
    private int totalNumberOfDisks;
    private FileBackingInfoProvisioningType provisioningType;
    private int maxBlockSize;
    private int maxBlockSizeInBytes;

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
     * @return the capacityInByte
     */
    public long getCapacityInBytes() {
        return this.capacityInBytes;
    }

    /**
     * @return the controllerKey
     */
    public int getControllerKey() {
        return this.controllerKey;
    }

    /**
     * @return the deviceKey
     */
    public int getDeviceKey() {
        return this.deviceKey;
    }

    /**
     * @return the handle
     */
    public Long getDiskHandle() {
        return this.diskHandle;
    }

    /**
     * @return the diskId
     */
    public int getDiskId() {
        return this.diskId;
    }

    /**
     *
     * /**
     *
     * @return the flags
     */
    public int getFlags() {
        return this.flags;
    }

    /**
     * @return the maxBlockSize
     */
    public int getMaxBlockSize() {
        return this.maxBlockSize;
    }

    /**
     * @return the getMaxBlockSizeInBytes
     */
    public int getMaxBlockSizeInBytes() {
        return this.maxBlockSizeInBytes;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the numberOfDumps
     */
    public Integer getNumberOfBlocks() {
        return this.numberOfBlocks;
    }

    /**
     * @return the numberOfThreads
     */
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    /**
     * @return the provisioningType
     */
    public FileBackingInfoProvisioningType getProvisioningType() {
        return this.provisioningType;
    }

    /**
     * @return the requestedTransportModes
     */
    public String getRequestedTransportModes() {
        return this.requestedTransportModes;
    }

    /**
     * @return the targetName
     */
    public String getTargetName() {
        return this.targetName;
    }

    /**
     * @return the totalNumberOfDisks
     */
    public int getTotalNumberOfDisks() {
        return this.totalNumberOfDisks;
    }

    /**
     * @return the unitNumber
     */
    public int getUnitNumber() {
        return this.unitNumber;
    }

    /**
     * @return the usedTransportModes
     */
    public String getUsedTransportModes() {
        return this.usedTransportModes;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * @return the vDiskId
     */
    public String getvDiskId() {
        return this.vDiskId;
    }

    /**
     * @return the diskMode
     */
    public VirtualDiskModeType getVirtualDiskMode() {
        return this.virtualDiskMode;
    }

    /**
     * @return the digestEnabled
     */
    public boolean isDigestEnabled() {
        return this.digestEnabled;
    }

    /**
     * @param adapterType the adapterType to set
     */
    public void setAdapterType(final AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    /**
     * @param busNumber the busNumber to set
     */
    public void setBusNumber(final int busNumber) {
        this.busNumber = busNumber;
    }

    /**
     * @param capacityInByte the capacityInByte to set
     */
    public void setCapacityInBytes(final long capacityInByte) {
        this.capacityInBytes = capacityInByte;
    }

    /**
     * @param controllerKey the controllerKey to set
     */
    public void setControllerKey(final int controllerKey) {
        this.controllerKey = controllerKey;
    }

    /**
     * @param deviceKey the deviceKey to set
     */
    public void setDeviceKey(final int deviceKey) {
        this.deviceKey = deviceKey;
    }

    /**
     * @param digestEnabled the digestEnabled to set
     */
    public void setDigestEnabled(final Boolean digestEnabled) {
        this.digestEnabled = digestEnabled;
    }

    /**
     * @param handle the handle to set
     */
    public void setDiskHandle(final Long handle) {
        this.diskHandle = handle;
    }

    /**
     * @param diskId the diskId to set
     */
    public void setDiskId(final int diskId) {
        this.diskId = diskId;
    }

    /**
     * @param flags the flags to set
     */
    public void setFlags(final int flags) {
        this.flags = flags;
    }

    /**
     * @param maxBlockSize the maxBlockSize to set
     */
    public void setMaxBlockSize(final int maxBlockSize) {
        this.maxBlockSize = maxBlockSize;
    }

    /**
     * @param getMaxBlockSizeInBytes the getMaxBlockSizeInBytes to set
     */
    public void setMaxBlockSizeInBytes(final int getMaxBlockSizeInBytes) {
        this.maxBlockSizeInBytes = getMaxBlockSizeInBytes;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param numberOfDumps the numberOfDumps to set
     */
    public void setNumberOfBlocks(final Integer numberOfDumps) {
        this.numberOfBlocks = numberOfDumps;
    }

    /**
     * @param numberOfThreads the numberOfThreads to set
     */
    public void setNumberOfThreads(final int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * @param provisioningType the provisioningType to set
     */
    public void setProvisioningType(final FileBackingInfoProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    /**
     * @param requestedTransportModes the requestedTransportModes to set
     */
    public void setRequestedTransportModes(final String requestedTransportModes) {
        this.requestedTransportModes = requestedTransportModes;
    }

    /**
     * @param targetName the targetName to set
     */
    public void setTargetName(final String targetName) {
        this.targetName = targetName;
    }

    /**
     * @param totalNumberOfDisks the totalNumberOfDisks to set
     */
    public void setTotalNumberOfDisks(final int totalNumberOfDisks) {
        this.totalNumberOfDisks = totalNumberOfDisks;
    }

    /**
     * @param unitNumber the unitNumber to set
     */
    public void setUnitNumber(final int unitNumber) {
        this.unitNumber = unitNumber;
    }

    /**
     * @param usedTransportModes the usedTransportModes to set
     */
    public void setUsedTransportModes(final String usedTransportModes) {
        this.usedTransportModes = usedTransportModes;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * @param vDiskId the vDiskId to set
     */
    public void setvDiskId(final String vDiskId) {
        this.vDiskId = vDiskId;
    }

    /**
     * @param diskMode the diskMode to set
     */
    public void setVirtualDiskMode(final VirtualDiskModeType diskMode) {
        this.virtualDiskMode = diskMode;
    }

}
