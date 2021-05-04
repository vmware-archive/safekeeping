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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.jDiskLib.Connection;
import com.vmware.jvix.jDiskLib.DiskHandle;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.DiskController;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;

public abstract class AbstractCoreResultDiskBackupRestore extends AbstractCoreResultActionImpl {
    /**
     * 
     */
    private static final long serialVersionUID = -1496315575944706238L;

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AbstractCoreResultDiskBackupRestore.class.getName());

    private volatile Integer numberOfBlocks;

    private Integer diskId;

    private String uuid;

    private int controllerKey;

    private int busNumber;

    private int unitNumber;

    private DiskHandle diskHandle;

    private String targetName;

    private String vDiskId;

    private String usedTransportModes;
    private int flags;

    private int numberOfThreads;

    private String name;

    private long capacityInBytes;

    private AdapterType adapterType;

    private int deviceKey;

    private VirtualDiskModeType virtualDiskMode;

    private Boolean digestEnabled;

    private FileBackingInfoProvisioningType provisioningType;
    private int maxBlockSize;
    private Map<Integer, ExBlockInfo> dumpMap;
    private final GenerationProfile profile;

    private final DiskProfile diskProfile;

    /**
     * @param diskId2
     * @param targetName2
     * @param vmdkInfo
     */
    AbstractCoreResultDiskBackupRestore(final int diskId, final GenerationProfile profile,
            final AbstractCoreResultActionBackupRestore parent) {
        super(parent);
        assert (profile != null);
        this.profile = profile;
        this.diskProfile = this.profile.getDisks().get(diskId);
        setFcoEntityInfo(profile.getFcoEntity());

        this.targetName = profile.getTargetOperation().getTargetName();
        this.dumpMap = new ConcurrentHashMap<>();

        this.diskId = diskId;
        setUnitNumber(this.diskProfile.getUnitNumber());
        if (getEntityType() == EntityType.VirtualMachine) {
            final DiskController controller = profile.getController(this.diskProfile.getControllerKey());
            setControllerKey(this.diskProfile.getControllerKey());
            setBusNumber(controller.getBusNumber());
            this.adapterType = controller.getAdapterType();
        }
        setUuid(this.diskProfile.getUuid());

        setName(this.diskProfile.getRemoteDiskPath());
        setCapacityInBytes(this.diskProfile.getCapacity());

        setvDiskId(this.diskProfile.getIvdId());

        this.deviceKey = this.diskProfile.getDeviceKey();
        this.virtualDiskMode = this.diskProfile.getDiskMode();

        this.provisioningType = this.diskProfile.getProvisioningType();
        this.digestEnabled = this.diskProfile.isDigestEnabled();

    }

    /**
     * Return the First class object parent of VM disk that is going to be backup or
     * restore
     * 
     * @return The first class object (VM, VAPP, IVD)
     */
    public abstract IFirstClassObject getFirstClassObjectParent();

    /**
     * @param index
     * @param dumpFileInfo
     */
    public void addDumpInfo(final int index, final ExBlockInfo dumpFileInfo) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int, DumpBlockInfo - start"); //$NON-NLS-1$
        }

        this.dumpMap.put(index, dumpFileInfo);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int, DumpBlockInfo - end"); //$NON-NLS-1$
        }
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
     * @return the capacityInBytes
     */
    public long getCapacityInBytes() {
        return this.capacityInBytes;
    }

    /**
     * @return the connectionHandle
     */
    public Connection getConnectionHandle() {
        return getParent().getConnectionHandle();
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
    public DiskHandle getDiskHandle() {
        return this.diskHandle;
    }

    /**
     * @return the diskId
     */
    public Integer getDiskId() {
        return this.diskId;
    }

    public DiskProfile getDiskProfile() {
        return this.diskProfile;
    }

    public Map<Integer, ExBlockInfo> getDumpMap() {
        return this.dumpMap;
    }

    /**
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

    public int getMaxBlockSizeInBytes() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final int returnint = this.maxBlockSize * jDiskLibConst.SECTOR_SIZE;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnint;
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

    @Override
    public AbstractCoreResultActionBackupRestore getParent() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final AbstractCoreResultActionBackupRestore returnCoreAbstractResultActionBackupRestore = (AbstractCoreResultActionBackupRestore) this.parent;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnCoreAbstractResultActionBackupRestore;
    }

    /**
     * @return the profile
     */
    public GenerationProfile getProfile() {
        return this.profile;
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
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final String returnString = getParent().getOptions().getRequestedTransportModes();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnString;

    }

    /**
     * @return the targetName
     */
    public String getTargetName() {
        return this.targetName;
    }

    /**
     * // * @return the totalNumberOfDisks //
     */
    public abstract int getTotalNumberOfDisks();

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
    public Boolean isDigestEnabled() {
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
     * @param capacityInBytes the capacityInBytes to set
     */
    public void setCapacityInBytes(final long capacityInBytes) {
        this.capacityInBytes = capacityInBytes;
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
     * @param diskHandle the handle to set
     */
    public void setDiskHandle(final DiskHandle diskHandle) {
        this.diskHandle = diskHandle;
    }

    /**
     * @param diskId the diskId to set
     */
    public void setDiskId(final Integer diskId) {
        this.diskId = diskId;
    }

    /**
     * @param dumpMap the dumpMap to set
     */
    public void setDumpMap(final Map<Integer, ExBlockInfo> dumpMap) {
        this.dumpMap = dumpMap;
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
     * @param targetName the targetName to set
     */
    public void setTargetName(final String targetName) {
        this.targetName = targetName;
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
    private void setvDiskId(final String vDiskId) {
        this.vDiskId = vDiskId;
    }

    /**
     * @param diskMode the diskMode to set
     */
    public void setVirtualDiskMode(final VirtualDiskModeType diskMode) {
        this.virtualDiskMode = diskMode;
    }

}
