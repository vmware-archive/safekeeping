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
package com.vmware.safekeeping.core.profile.dataclass;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.pbm.RuntimeFaultFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.profile.SimpleBlockInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.enums.AdapterProtocolType;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VirtualDeviceBackingInfoType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vslm.FileFaultFaultMsg;
import com.vmware.vslm.InvalidDatastoreFaultMsg;
import com.vmware.vslm.InvalidStateFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public class DiskProfile {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(DiskProfile.class.getName());

    private static final String REPORT_FILE_EXTENSION = ".report";
    private String remoteDiskPath;

    private String uuid;

    private long capacity;

    private boolean ivdDisk;
    private String changeId;
    private boolean compression;
    private boolean cipher;
    private BackupMode backupMode;
    private String report;
    private Date dumpBeginTimestamp;
    private Date dumpEndTimestamp;
    private boolean succeeded;
    private String vmxDiskEntry;
    private int controllerKey;// 1000,
    private int unitNumber;
    private int deviceKey;// 2000//
    private VirtualDiskModeType diskMode; // persistent

    private FileBackingInfoProvisioningType provisioningType;// THIN

    private boolean digestEnabled;
    private boolean writeThrough;
    private String cryptoKeyId;
    private String cryptoKeyProviderId;
    private long totalDumpSize;
    private long totalUncompressedDumpSize;
    private int diskId;
    private String ivdId;
    private FcoPbmProfile pbmProfile;

    private String ioFilter;

    private String contentId;
    private VirtualDeviceBackingInfoType virtualDeviceBackingInfo;
    private Map<String, String> metadata;
    private final Map<Integer, SimpleBlockInfo> dumps;

    private ManagedEntityInfo datastoreInfo;

    /**
     *
     */
    public DiskProfile() {
        this.dumps = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public DiskProfile(final DiskProfile src) {
        this();
        this.remoteDiskPath = src.remoteDiskPath;

        this.uuid = src.uuid;

        this.capacity = src.capacity;

        this.ivdDisk = src.ivdDisk;
        this.changeId = src.changeId;
        this.compression = src.compression;
        this.cipher = src.cipher;
        this.backupMode = src.backupMode;
        this.report = src.report;
        this.dumpBeginTimestamp = src.dumpBeginTimestamp;
        this.dumpEndTimestamp = src.dumpEndTimestamp;
        this.succeeded = src.succeeded;
        this.vmxDiskEntry = src.vmxDiskEntry;
        this.controllerKey = src.controllerKey;
        this.unitNumber = src.unitNumber;
        this.deviceKey = src.deviceKey;
        this.diskMode = src.diskMode;
        this.provisioningType = src.provisioningType;
        this.digestEnabled = src.digestEnabled;
        this.writeThrough = src.writeThrough;
        this.cryptoKeyId = src.cryptoKeyId;
        this.cryptoKeyProviderId = src.cryptoKeyProviderId;
        this.totalDumpSize = src.totalDumpSize;
        this.totalUncompressedDumpSize = src.totalUncompressedDumpSize;
        this.diskId = src.diskId;
        this.ivdId = src.ivdId;
        datastoreInfo = src.datastoreInfo;
        this.ioFilter = src.ioFilter;
        this.contentId = src.contentId;
        this.virtualDeviceBackingInfo = src.virtualDeviceBackingInfo;
        this.pbmProfile = new FcoPbmProfile(src.pbmProfile);

        for (final Entry<String, String> entry : src.metadata.entrySet()) {
            this.metadata.put(entry.getKey(), entry.getValue());
        }

    }

    /**
     * @param spec
     */
    public DiskProfile(final GenerationProfileSpec spec) {
        this();
        this.diskId = 0;
        final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) spec.getFco();
        final String iStr = String.valueOf(this.diskId);
        final VStorageObjectConfigInfo vmdkInfo = ivd.getConfigInfo();
        this.uuid = ivd.getUuid();
        this.capacity = vmdkInfo.getCapacityInMB() * Utility.ONE_MBYTES;

        this.backupMode = BackupMode.UNKNOW;
        this.ivdDisk = true;
        this.ivdId = ivd.getId().getId();
        this.report = iStr + REPORT_FILE_EXTENSION;
        this.succeeded = false;
        this.compression = spec.getBackupOptions().isCompression();
        this.cipher = spec.getBackupOptions().isCipher();
        this.changeId = spec.getCbt();
        if (StringUtils.isEmpty(this.changeId)) {
            try {
                this.changeId = ivd.getChangeId();
            } catch (FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                    | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | com.vmware.vim25.FileFaultFaultMsg
                    | com.vmware.vim25.InvalidDatastoreFaultMsg | com.vmware.vim25.InvalidStateFaultMsg
                    | com.vmware.vim25.NotFoundFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
            if (StringUtils.isEmpty(this.changeId)) {
                this.changeId = "*";
            }
        }
        this.ioFilter = StringUtils.join(vmdkInfo.getIofilter(), ',');

        try {
            final BaseConfigInfoDiskFileBackingInfo backing = (BaseConfigInfoDiskFileBackingInfo) vmdkInfo.getBacking();
            this.remoteDiskPath = backing.getFilePath();
            this.virtualDeviceBackingInfo = VirtualDeviceBackingInfoType.BaseConfigInfoFileBackingInfo;
            this.provisioningType = FileBackingInfoProvisioningType.parse(backing.getProvisioningType());

            this.datastoreInfo = new ManagedEntityInfo(
                    ivd.getVimConnection().getVimHelper().entityName(backing.getDatastore()), backing.getDatastore(),
                    ivd.getVimConnection().getServerIntanceUuid());
            final List<PbmProfileId> pbmProfilesId = ivd.getAssociatedProfile();
            if (!pbmProfilesId.isEmpty()) {
                final List<PbmProfile> vimPbmProfiles = ivd.getVimConnection().getPbmConnection()
                        .pbmRetrieveContent(pbmProfilesId);
                this.pbmProfile = new FcoPbmProfile(vimPbmProfiles.get(0));
            } else {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning(ivd.toString() + " has no associated storage profile");
                }
            }

        } catch (final InvalidArgumentFaultMsg | RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                | com.vmware.vim25.RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    public DiskProfile(final GenerationProfileSpec spec, final VmdkInfo vmdkInfo) {
        this();
        final VirtualMachineManager vmm = (VirtualMachineManager) spec.getFco();
        this.diskId = vmdkInfo.getDiskId();
        this.unitNumber = vmdkInfo.getUnitNumber();
        this.remoteDiskPath = vmdkInfo.getRemoteDiskPath();
        this.succeeded = false;
        this.uuid = vmdkInfo.getUuid();
        this.capacity = vmdkInfo.getCapacityInBytes();
        this.contentId = vmdkInfo.getContentId();
        this.ivdDisk = (vmdkInfo.getVDiskId() != null);

        if (this.ivdDisk) {
            this.ivdId = vmdkInfo.getVDiskId().getId();
        }

        this.changeId = vmdkInfo.getChangeId();
        if (this.changeId == null) {
            this.changeId = "*";
        }
        this.virtualDeviceBackingInfo = vmdkInfo.getVirtualDeviceBackingInfo();
        if (spec.getBackupOptions() != null) {
            this.compression = spec.getBackupOptions().isCompression();
            this.cipher = spec.getBackupOptions().isCipher();
            this.backupMode = BackupMode.UNKNOW;
            this.report = vmdkInfo.getDiskId() + REPORT_FILE_EXTENSION;

        }
        this.vmxDiskEntry = String.format("%s%d:%d",
                AdapterProtocolType.getProtocolType(vmdkInfo.getAdapterType()).toString(), vmdkInfo.getBusNumber(),
                vmdkInfo.getUnitNumber());

        this.controllerKey = vmdkInfo.getControllerKey();
        this.deviceKey = vmdkInfo.getDeviceKey();
        this.diskMode = vmdkInfo.getDiskMode();

        this.provisioningType = FileBackingInfoProvisioningType.parse(vmdkInfo.getProvisioningType());
        this.digestEnabled = vmdkInfo.isDigestEnabled();
        this.writeThrough = vmdkInfo.isWriteThrough();
        if (vmdkInfo.getCryptoKeyId() != null) {
            this.cryptoKeyId = vmdkInfo.getCryptoKeyId().getKeyId();
            this.cryptoKeyProviderId = vmdkInfo.getCryptoKeyId().getProviderId().getId();
        }
        this.ioFilter = StringUtils.join(vmdkInfo.getIofilter(), ',');

        try {
            this.datastoreInfo = new ManagedEntityInfo(
                    vmm.getVimConnection().getVimHelper().entityName(vmdkInfo.getDatastore()), vmdkInfo.getDatastore(),
                    vmm.getVimConnection().getServerIntanceUuid());
            final List<PbmProfileId> pbmProfilesId = vmm.getAssociatedProfile(this.deviceKey);
            if (!pbmProfilesId.isEmpty()) {
                final List<PbmProfile> vimPbmProfiles = vmm.getVimConnection().getPbmConnection()
                        .pbmRetrieveContent(pbmProfilesId);
                if ((vimPbmProfiles != null) && !vimPbmProfiles.isEmpty()) {
                    this.pbmProfile = new FcoPbmProfile(vimPbmProfiles.get(0));
                }
            } else {
                final String msg = String.format("vm:%s has no associate storage profile", vmm.toString());
                logger.warning(msg);
            }
        } catch (final InvalidArgumentFaultMsg | RuntimeFaultFaultMsg | PbmFaultFaultMsg | InvalidPropertyFaultMsg
                | com.vmware.vim25.RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

    }

    /**
     * @return the backupMode
     */
    public BackupMode getBackupMode() {
        return this.backupMode;
    }

    /**
     * @return the capacity
     */
    public long getCapacity() {
        return this.capacity;
    }

    /**
     * @return the changeId
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

    /**
     * @return the controllerKey
     */
    public int getControllerKey() {
        return this.controllerKey;
    }

    /**
     * @return the cryptoKeyId
     */
    public String getCryptoKeyId() {
        return this.cryptoKeyId;
    }

    /**
     * @return the cryptoKeyProviderId
     */
    public String getCryptoKeyProviderId() {
        return this.cryptoKeyProviderId;
    }

    /**
     * @return the deviceKey
     */
    public int getDeviceKey() {
        return this.deviceKey;
    }

    /**
     * @return the diskId
     */
    public int getDiskId() {
        return this.diskId;
    }

    public Map<String, String> getDiskMetadata() {
        return this.metadata;
    }

    /**
     * @return the diskMode
     */
    public VirtualDiskModeType getDiskMode() {
        return this.diskMode;
    }

    /**
     * @return the dumpBeginTimestamp
     */
    public Date getDumpBeginTimestamp() {
        return this.dumpBeginTimestamp;
    }

    /**
     * @return
     */
    @JsonIgnore
    public long getDumpElapsedTime() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if ((this.dumpEndTimestamp == null) || (this.dumpBeginTimestamp == null)) {

            final long returnlong = -1;
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end - return value=" + returnlong); //$NON-NLS-1$
            }
            return returnlong;
        }
        final long endTime = this.dumpEndTimestamp.getTime();
        final long startTime = this.dumpBeginTimestamp.getTime();
        final long returnlong = endTime - startTime;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end - return value=" + returnlong); //$NON-NLS-1$
        }
        return returnlong;

    }

    /**
     * @return the dumpEndTimestamp
     */
    public Date getDumpEndTimestamp() {
        return this.dumpEndTimestamp;
    }

    public Map<Integer, SimpleBlockInfo> getDumps() {
        return this.dumps;
    }

    /**
     * @return the ioFilter
     */
    public String getIoFilter() {
        return this.ioFilter;
    }

    /**
     * @return
     */
    public String getIvdId() {
        return this.ivdId;
    }

    /**
     * @return the pbmProfiles
     */
    public FcoPbmProfile getPbmProfile() {
        return this.pbmProfile;
    }

    /**
     * @return the provisioningType
     */
    public FileBackingInfoProvisioningType getProvisioningType() {
        return this.provisioningType;
    }

    /**
     * @return the remotePath
     */
    public String getRemoteDiskPath() {
        return this.remoteDiskPath;
    }

    /**
     * @return the report
     */
    public String getReport() {
        return this.report;
    }

    /**
     * @return the totalDumpSize
     */
    public long getTotalDumpSize() {
        return this.totalDumpSize;
    }

    /**
     * @return the totalUncompressedDumpSize
     */
    public long getTotalUncompressedDumpSize() {
        return this.totalUncompressedDumpSize;
    }

    /**
     * @return the unitNumber
     */
    public int getUnitNumber() {
        return this.unitNumber;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * @return the virtualDeviceBackingInfo
     */
    public VirtualDeviceBackingInfoType getVirtualDeviceBackingInfo() {
        return this.virtualDeviceBackingInfo;
    }

    /**
     * @return the vmxDiskEntry
     */
    public String getVmxDiskEntry() {
        return this.vmxDiskEntry;
    }

    /**
     * @return the cipher
     */
    public boolean isCipher() {
        return this.cipher;
    }

    /**
     * @return the compression
     */
    public boolean isCompression() {
        return this.compression;
    }

    /**
     * @return the digestEnabled
     */
    public boolean isDigestEnabled() {
        return this.digestEnabled;
    }

    /**
     * @return the ivdDisk
     */
    public boolean isIvdDisk() {
        return this.ivdDisk;
    }

    /**
     * @return the succeeded
     */
    public boolean isSucceeded() {
        return this.succeeded;
    }

    /**
     * @return the writeThrough
     */
    public boolean isWriteThrough() {
        return this.writeThrough;
    }

    /**
     * @param backupMode the backupMode to set
     */
    public void setBackupMode(final BackupMode backupMode) {
        this.backupMode = backupMode;
    }

    /**
     * @param capacity the capacity to set
     */
    public void setCapacity(final long capacity) {
        this.capacity = capacity;
    }

    /**
     * @param changeId the changeId to set
     */
    public void setChangeId(final String changeId) {
        this.changeId = changeId;
    }

    /**
     * @param cipher the cipher to set
     */
    public void setCipher(final boolean cipher) {
        this.cipher = cipher;
    }

    /**
     * @param compression the compression to set
     */
    public void setCompression(final boolean compression) {
        this.compression = compression;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(final String contentId) {
        this.contentId = contentId;
    }

    /**
     * @param controllerKey the controllerKey to set
     */
    public void setControllerKey(final int controllerKey) {
        this.controllerKey = controllerKey;
    }

    /**
     * @param cryptoKeyId the cryptoKeyId to set
     */
    public void setCryptoKeyId(final String cryptoKeyId) {
        this.cryptoKeyId = cryptoKeyId;
    }

    /**
     * @param cryptoKeyProviderId the cryptoKeyProviderId to set
     */
    public void setCryptoKeyProviderId(final String cryptoKeyProviderId) {
        this.cryptoKeyProviderId = cryptoKeyProviderId;
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
    public void setDigestEnabled(final boolean digestEnabled) {
        this.digestEnabled = digestEnabled;
    }

    /**
     * @param diskId the diskId to set
     */
    public void setDiskId(final int diskId) {
        this.diskId = diskId;
    }

    public void setDiskMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;

    }

    /**
     * @param diskMode the diskMode to set
     */
    public void setDiskMode(final VirtualDiskModeType diskMode) {
        this.diskMode = diskMode;
    }

    /**
     * @param dumpBeginTimestamp the dumpBeginTimestamp to set
     */
    public void setDumpBeginTimestamp(final Date dumpBeginTimestamp) {
        this.dumpBeginTimestamp = dumpBeginTimestamp;
    }

    /**
     * @param dumpEndTimestamp the dumpEndTimestamp to set
     */
    public void setDumpEndTimestamp(final Date dumpEndTimestamp) {
        this.dumpEndTimestamp = dumpEndTimestamp;
    }

    /**
     * @param ioFilter the ioFilter to set
     */
    public void setIoFilter(final String ioFilter) {
        this.ioFilter = ioFilter;
    }

    /**
     * @param ivdDisk the ivdDisk to set
     */
    public void setIvdDisk(final boolean ivdDisk) {
        this.ivdDisk = ivdDisk;
    }

    /**
     * @param ivdId the ivdId to set
     */
    public void setIvdId(final String ivdId) {
        this.ivdId = ivdId;
    }

    public void setPbmProfile(final FcoPbmProfile pbmProfiles) {
        this.pbmProfile = pbmProfiles;
    }

    /**
     * @param provisioningType the provisioningType to set
     */
    public void setProvisioningType(final FileBackingInfoProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    /**
     * @param remotePath the remotePath to set
     */
    public void setRemoteDiskPath(final String remotePath) {
        this.remoteDiskPath = remotePath;
    }

    /**
     * @param report the report to set
     */
    public void setReport(final String report) {
        this.report = report;
    }

    /**
     * @param succeeded the succeeded to set
     */
    public void setSucceeded(final boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     * @param totalDumpSize the totalDumpSize to set
     */
    public void setTotalDumpSize(final long totalDumpSize) {
        this.totalDumpSize = totalDumpSize;
    }

    /**
     * @param totalUncompressedDumpSize the totalUncompressedDumpSize to set
     */
    public void setTotalUncompressedDumpSize(final long totalUncompressedDumpSize) {
        this.totalUncompressedDumpSize = totalUncompressedDumpSize;
    }

    /**
     * @param unitNumber the unitNumber to set
     */
    public void setUnitNumber(final int unitNumber) {
        this.unitNumber = unitNumber;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * @param virtualDeviceBackingInfo the virtualDeviceBackingInfo to set
     */
    public void setVirtualDeviceBackingInfo(final VirtualDeviceBackingInfoType virtualDeviceBackingInfo) {
        this.virtualDeviceBackingInfo = virtualDeviceBackingInfo;
    }

    /**
     * @param vmxDiskEntry the vmxDiskEntry to set
     */
    public void setVmxDiskEntry(final String vmxDiskEntry) {
        this.vmxDiskEntry = vmxDiskEntry;
    }

    /**
     * @param writeThrough the writeThrough to set
     */
    public void setWriteThrough(final boolean writeThrough) {
        this.writeThrough = writeThrough;
    }

    public ManagedEntityInfo getDatastoreInfo() {
        return datastoreInfo;
    }

    public void setDatastoreInfo(ManagedEntityInfo datastoreInfo) {
        this.datastoreInfo = datastoreInfo;
    }

}
