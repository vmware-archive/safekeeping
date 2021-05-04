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
package com.vmware.safekeeping.core.profile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.profile.dataclass.DiskController;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoChildrenGenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoGenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoGenerationResourceAllocation;
import com.vmware.safekeeping.core.profile.dataclass.FcoNetwork;
import com.vmware.safekeeping.core.profile.vmspec.VirtualDeviceBackingInfoOverlay;
import com.vmware.safekeeping.core.profile.vmspec.VirtualDeviceConfigSpecOverlay;
import com.vmware.safekeeping.core.profile.vmspec.VirtualMachineConfigSpecOverlay;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VirtualDiskModeType;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class GenerationProfile {

    private final FcoGenerationProfile profile;
    private FcoArchiveManager fcoArchiveManager;
    private FcoGenerationProfile previousGeneration;

    private ITargetOperation target;

    public GenerationProfile() {
        this.profile = new FcoGenerationProfile();
    }

    public GenerationProfile(final FcoArchiveManager fcoArchiveManager) {
        this.profile = new FcoGenerationProfile();
        this.fcoArchiveManager = fcoArchiveManager;
        this.target = fcoArchiveManager.getRepositoryTarget();
    }

    /**
     *
     * @param fcoArchiveManager
     * @param fcoEntity
     * @param genId
     * @throws IOException
     */
    public GenerationProfile(final FcoArchiveManager fcoArchiveManager, final ManagedFcoEntityInfo fcoEntity,
            final Integer genId) throws IOException {
        this.fcoArchiveManager = fcoArchiveManager;
        this.target = fcoArchiveManager.getRepositoryTarget();
        final byte[] bytes = this.target.getGenerationProfileToByteArray(fcoEntity, genId);
        if ((bytes != null) && (bytes.length > 0)) {
            final ObjectMapper objectMapper = new ObjectMapper();
            this.profile = objectMapper.readValue(bytes, FcoGenerationProfile.class);
            if ((this.profile.getPreviousGenerationId() != null) && (this.profile.getPreviousGenerationId() >= 0)) {
                final byte[] bytes2 = this.target.getGenerationProfileToByteArray(fcoEntity,
                        this.profile.getPreviousGenerationId());
                if ((bytes2 != null) && (bytes2.length > 0)) {
                    this.previousGeneration = objectMapper.readValue(bytes2, FcoGenerationProfile.class);
                }
            }
        } else {
            this.profile = null;
        }
    }

    public GenerationProfile(final GenerationProfile src) {
        this.profile = new FcoGenerationProfile(src.profile);
        this.fcoArchiveManager = src.fcoArchiveManager;
        this.target = src.fcoArchiveManager.getRepositoryTarget();
    }

    public void addDumpInfo(final Integer diskId, final ExBlockInfo exBlockInfo) {
        final DiskProfile diskProfile = this.profile.getDisks().get(diskId);

        diskProfile.getDumps().put(exBlockInfo.getIndex(), exBlockInfo.toSimpleBlockInfo());
    }

    public FcoGenerationProfile clearGenerationDependency() {
        final FcoGenerationProfile prevGen = this.previousGeneration;
        this.previousGeneration = null;
        this.profile.setPreviousGenerationId(-1);

        return prevGen;
    }

    /**
     * @param diskId
     * @return
     */
    public AdapterType getAdapterType(final int diskId) {
        final int ctrlKey = this.profile.getDisks().get(diskId).getControllerKey();
        return this.profile.getControllers().get(ctrlKey).getAdapterType();
    }

    /**
     * @return
     */
    public BackupMode getBackupMode() {
        return this.profile.getBackupMode();
    }

    /**
     * @param ckey
     * @return
     */
    public int getBusNumber(final Integer ckey) {
        return this.profile.getControllers().get(ckey).getBusNumber();
    }

    /**
     * @param diskId
     * @return
     */
    public long getCapacity(final int diskId) {
        return this.profile.getDisks().get(diskId).getCapacity();
    }

    public VirtualMachineConfigSpecOverlay getConfigSpec() {
        return this.profile.getConfigSpec();
    }

    /**
     * @param controllerKey
     * @return
     */
    public DiskController getController(final int controllerKey) {
        return this.profile.getControllers().get(controllerKey);

    }

    /**
     * @param diskId
     * @return
     */
    public Integer getControllerDeviceKey(final int diskId) {
        return this.profile.getDisks().get(diskId).getControllerKey();
    }

    /**
     * @return
     */
    public FcoGenerationResourceAllocation getCpuAllocation() {
        return this.profile.getCpuAllocation();
    }

    /**
     * @return
     */
    public ManagedEntityInfo getDatacenterInfo() {
        return this.profile.getDatacenterInfo();
    }

    /**
     * @return
     */
    public ManagedEntityInfo getDatastoreInfo() {
        return this.profile.getDatastoreInfo();
    }

    /**
     * @param diskId
     * @return
     */
    public BackupMode getDiskBackupMode(final int diskId) {
        return this.profile.getDisks().get(diskId).getBackupMode();
    }

    public String getDiskChangeId(final int diskId) {
        return this.profile.getDisks().get(diskId).getChangeId();
    }

    /**
     * @param uuid
     * @return
     */
    public int getDiskIdWithUuid(final String uuid) {
        for (final DiskProfile disk : this.profile.getDisks()) {
            if (disk.getUuid().equals(uuid)) {
                return disk.getDiskId();
            }
        }
        return -1;

    }

    public Map<String, String> getDiskMetadata(final int diskId) {
        return this.profile.getDisks().get(diskId).getDiskMetadata();
    }

    /**
     * @param diskId
     * @return
     */
    public VirtualDiskModeType getDiskMode(final int diskId) {
        return this.profile.getDisks().get(diskId).getDiskMode();
    }

    /**
     * @param diskId
     * @return
     */
    public FileBackingInfoProvisioningType getDiskProvisionType(final int diskId) {
        return this.profile.getDisks().get(diskId).getProvisioningType();
    }

    public List<DiskProfile> getDisks() {
        return this.profile.getDisks();
    }

    public String getDiskUuid(final int diskId) {
        return this.profile.getDisks().get(diskId).getUuid();
    }

    /**
     * @param diskId
     * @return
     */
    public long getDumpElapsedTime(final int diskId) {
        return this.profile.getDisks().get(diskId).getDumpElapsedTime();
    }

    /**
     * @return the fcoArchiveManager
     */

    public FcoArchiveManager getFcoArchiveManager() {
        return this.fcoArchiveManager;
    }

    public List<FcoChildrenGenerationProfile> getFcoChildren() {
        return this.profile.getFcoChildren();

    }

    /**
     * @return the fcoEntity
     */
    public ManagedFcoEntityInfo getFcoEntity() {
        return this.profile.getFcoEntity();
    }

    public ManagedFcoEntityInfo getFcoParent() {
        return this.profile.getFcoParent();
    }

    public ManagedEntityInfo getFolderInfo() {
        return this.profile.getFolderInfo();
    }

    /**
     * @return
     */
    public String getFolderPath() {
        return this.profile.getFolderPath();
    }

    /**
     * @return
     */
    public Integer getGenerationId() {
        return this.profile.getGenerationId();
    }

    public String getGenerationPath() {
        return String.format("%s/%d", getUuid(), getGenerationId());

    }

    public String getGenerationProfileContentPath() {
        return String.format("%s/%d/%s", getUuid(), getGenerationId(), CoreGlobalSettings.GENERATION_PROFILE_FILENAME);
    }

    public FcoGenerationsCatalog getGenerationsCatalog() {
        return this.fcoArchiveManager.getGenerationsCatalog();
    }

    /**
     * @param diskId
     * @return
     */
    public Object getImprovedVirtualDiskId(final int diskId) {
        return this.profile.getDisks().get(diskId).getIvdId();
    }

    /**
     * @return
     */
    public long getMaxBlockSize() {
        return this.profile.getMaxBlockSize();
    }

    public long getMaxNumberOfSectorsPerBlock() {
        return this.profile.getMaxBlockSize() / jDiskLibConst.SECTOR_SIZE;
    }

    /**
     * @return
     */
    public String getMd5ContentPath() {
        return String.format("%s/%s", getGenerationPath(), this.profile.getMd5Filename());

    }

    /**
     * @return
     */
    public FcoGenerationResourceAllocation getMemAllocation() {
        return this.profile.getCpuAllocation();
    }

    /**
     * @return
     */
    public String getName() {
        return this.profile.getFcoEntity().getName();
    }

    /**
     * @return
     */
    public Map<String, FcoNetwork> getNetworks() {
        return this.profile.getNetworks();
    }

    /**
     * @return
     */
    public int getNumberOfDisks() {
        return this.profile.getNumberOfDisks();
    }

    public int getNumberOfDumps() {
        int result = 0;
        for (final DiskProfile disk : this.profile.getDisks()) {
            result += disk.getDumps().size();
        }
        return result;
    }

    /**
     * @return
     */
    public int getNumberOfNetworks() {
        return this.profile.getNetworks().size();
    }

    public Integer getNumberOfVnics() {
        return this.profile.getNumberOfVnics();
    }

    public String getNvramContentPath() {
        if (StringUtils.isNotEmpty(this.profile.getNvramFilename())) {
            return String.format("%s/%d/%s", getUuid(), getGenerationId(), this.profile.getNvramFilename());
        }
        return null;
    }

    /**
     * @return
     */
    public String getPbmProfileName() {
        if (this.profile.getPbmProfile() != null) {
            return this.profile.getPbmProfile().getPbmProfileName();
        } else {
            return null;
        }
    }

    public FcoGenerationProfile getPreviousGeneration() {
        return this.previousGeneration;
    }

    /**
     * @return
     */
    public Integer getPreviousGenerationId() {
        return this.profile.getPreviousGenerationId();
    }

    /**
     * @param diskId
     * @return
     */
    public String getRemoteDiskPath(final Integer diskId) {
        return this.profile.getDisks().get(diskId).getRemoteDiskPath();
    }

    public String getReportContentPath(final int diskId) {
        return String.format("%s/%d%s", getGenerationPath(), diskId, this.profile.getDisks().get(diskId).getReport());

    }

    public ManagedEntityInfo getResourcePoolInfo() {
        return this.profile.getResourcePoolInfo();
    }

    /**
     * @return
     */
    public String getResourcePoolPath() {
        return this.profile.getResourcePoolPath();
    }

    /**
     * @return the target
     */
    public ITargetOperation getTargetOperation() {
        return this.fcoArchiveManager.getRepositoryTarget();
    }

    /**
     * @return
     */
    public Date getTimestamp() {
        return this.profile.getTimestamp();
    }

    /**
     * @param diskId
     * @return
     */
    public int getUnitNumber(final int diskId) {
        return this.profile.getDisks().get(diskId).getUnitNumber();
    }

    /**
     * @return
     */

    @JsonIgnore
    public String getUuid() {
        return this.profile.getFcoEntity().getUuid();
    }

    /**
     * @return
     */
    public Map<String, Integer> getVappChildrenUuid() {
        return this.profile.getVappChildrenUuid();
    }

    /**
     * @return
     */
    public String getvAppConfigPath() {
        if (StringUtils.isNotEmpty(this.profile.getVappConfigKeyName())) {
            return String.format("%s/%d/%s", getUuid(), getGenerationId(), this.profile.getVappConfigKeyName());
        }
        return null;
    }

    public String getVmxContentPath() {
        if (StringUtils.isNotEmpty(this.profile.getVmxFilename())) {
            return String.format("%s/%d/%s", getUuid(), getGenerationId(), this.profile.getVmxFilename());
        }
        return null;
    }

    /**
     * @param i
     * @return
     */
    public String getVmxDiskEntry(final int diskId) {
        return this.profile.getDisks().get(diskId).getVmxDiskEntry();
    }

    public boolean hasParent() {
        return this.profile.getFcoParent() != null;
    }

    private int incrementNumOfFailedVmdkdump() {
        return this.profile.incrementNumOfFailedVmdkdump();
    }

    private int incrementNumOfSucceededVmdkdump() {

        return this.profile.incrementNumOfSucceededVmdkdump();
    }

    public void initializeGeneration(final GenerationProfileSpec spec)
            throws IOException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, PbmFaultFaultMsg,
            com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg, ProfileException,
            com.vmware.vslm.RuntimeFaultFaultMsg, VimObjectNotExistException {

        this.profile.initializeGenerationGroup(spec);
        switch (spec.getFcoType()) {
        case ivd:
            this.profile.initializeIvdDiskGroup(spec);
            break;
        case k8s:
            break;
        case vapp:
            break;
        case vm:
            this.profile.initializeDiskGroup(spec);

            this.profile.setConfigSpec(new VirtualMachineConfigSpecOverlay(
                    ((VirtualMachineManager) spec.getFco()).getConfig().getVirtualMachineConfigInfo()));
            this.profile.getNetworks().putAll((((VirtualMachineManager) spec.getFco()).getVirtualMachineNetworks()));
            int vNicNumber = 0;
            for (final VirtualDeviceConfigSpecOverlay deviceChange : this.profile.getConfigSpec().getDeviceChange()) {
                switch (deviceChange.getDevice().getDeviceType()) {
                case VIRTUAL_E1000:
                case VIRTUAL_E1000E:
                case VIRTUAL_PCNET32:
                case VIRTUAL_SRIOV_ETHERNET_CARD:
                case VIRTUAL_VMXNET:
                case VIRTUAL_VMXNET2:
                case VIRTUAL_VMXNET3_VRDMA:
                case VIRTUAL_VMXNET3:
                    FcoNetwork profNetwork = null;
                    final VirtualDeviceBackingInfoOverlay backing = deviceChange.getDevice().getBacking();
                    switch (backing.getBackingInfoType()) {
                    case VIRTUAL_ETHERNET_CARD_DISTRIBUTED_VIRTUAL_PORT_BACKINGINFO:
                        profNetwork = this.profile.getNetworks().get(backing.getProperties().get("port.portgroupKey"));
                        break;
                    case VIRTUAL_ETHERNET_CARD_OPAQUE_NETWORK_BACKINGINFO:
                        profNetwork = this.profile.getNetworks().get(backing.getProperties().get("opaqueNetworkId"));
                        break;
                    default:
                        break;
                    }
                    if (profNetwork != null) {
                        profNetwork.getVmNics().add(vNicNumber);
                    }
                    vNicNumber++;
                    break;
                default:
                    break;

                }
            }

            this.profile.setNumberOfVnics(vNicNumber);
            break;
        default:
            break;

        }
        if (spec.getPrevGenId() >= 0) {
            final byte[] bytes2 = this.fcoArchiveManager.getRepositoryTarget().getGenerationProfileToByteArray(
                    this.profile.getFcoEntity(), this.profile.getPreviousGenerationId());
            if ((bytes2 != null) && (bytes2.length > 0)) {

                final ObjectMapper objectMapper = new ObjectMapper();
                this.previousGeneration = objectMapper.readValue(bytes2, FcoGenerationProfile.class);
            } else {
                this.previousGeneration = null;
            }
        }

    }

    /**
     * @return
     */
    public boolean isChangeTrackingEnabled() {
        return this.profile.isChangeTracking();
    }

    /**
     * @param diskId
     */
    public boolean isDiskChanged(final Integer diskId) {
        if (this.previousGeneration == null) {
            return true;
        }
        return !getDisks().get(diskId).getChangeId()
                .equals(this.previousGeneration.getDisks().get(diskId).getChangeId());

    }

    /**
     * @param diskId
     * @return
     */
    public boolean isImprovedVirtualDisk(final int diskId) {
        return this.profile.getDisks().get(diskId).isIvdDisk();
    }

    public boolean isProfileValid() {
        return this.profile != null;
    }

    /**
     * @return
     */
    public boolean isSucceeded() {
        return this.profile.isSucceeded();
    }

    public boolean isVappConfigAvailable() {
        return (StringUtils.isNotEmpty(this.profile.getVappConfigKeyName()));
    }

    /**
     * @param prevDiskId
     * @return
     */
    public boolean isVmdkdumpSucceeded(final int diskId) {
        return this.profile.getDisks().get(diskId).isSucceeded();
    }

    public void reconfigureGeneration(final Integer generationId) {
        this.profile.reconfigureGeneration(generationId);
    }

    /**
     * @param backupMode
     */
    public void setBackupMode(final BackupMode value) {
        this.profile.setBackupMode(value);

    }

    /**
     * VAPP children. Set the the success generation for this specific child
     *
     * @param indexchild index
     * @param generation sucessfull generation
     */
    public void setChildSuccessfullGenerationId(final int index, final int generationId) {
        this.profile.getFcoChildren().get(index).setGenerationId(generationId);
    }

    /**
     * @param diskId
     * @param backupMode
     */
    public void setDiskBackupMode(final int diskId, final BackupMode backupMode) {
        this.profile.getDisks().get(diskId).setBackupMode(backupMode);

    }

    public void setDiskMetadata(final int diskId, final Map<String, String> metadata) {
        this.profile.getDisks().get(diskId).setDiskMetadata(metadata);

    }

    /**
     * @param diskId
     * @param i
     */
    public void setDiskTotalDumpSize(final Integer diskId, final long i) {
        this.profile.getDisks().get(diskId).setTotalDumpSize(i);

    }

    /**
     * @param diskId
     * @param i
     */
    public void setDiskTotalUncompressedDumpSize(final Integer diskId, final long i) {
        this.profile.getDisks().get(diskId).setTotalUncompressedDumpSize(i);
    }

    /**
     * @param diskId
     * @param startTime
     */
    public void setDumpBeginTimestamp(final int diskId, final long time) {
        this.profile.getDisks().get(diskId).setDumpBeginTimestamp(new Date(time));

    }

    /**
     * @param diskId
     * @param endTime
     */
    public void setDumpEndTimestamp(final int diskId, final long time) {
        this.profile.getDisks().get(diskId).setDumpEndTimestamp(new Date(time));

    }

    /**
     * @param isSucceeded
     */
    public void setSucceeded(final boolean value) {
        this.profile.setSucceeded(value);
    }

    /**
     * @param diskId
     * @param b
     */
    public void setVmdkdumpResult(final int diskId, final boolean succeeded) {
        this.profile.getDisks().get(diskId).setSucceeded(succeeded);
        if (succeeded) {
            incrementNumOfSucceededVmdkdump();
        } else {
            incrementNumOfFailedVmdkdump();
        }
    }

    /**
     * Convert the class Generation to JSON
     *
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public ByteArrayInOutStream toByteArrayInOutputStream() throws NoSuchAlgorithmException, IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(this.profile);
        return new ByteArrayInOutStream(json);
    }

    public boolean isEncrypted() {
        return profile.getCryptoKeyId() != null;
    }

}
