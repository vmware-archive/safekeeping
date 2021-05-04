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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.profile.vmspec.VirtualMachineConfigSpecOverlay;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.enums.AdapterProtocolType;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.KeyProviderId;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class FcoGenerationProfile {

    private static final String VAPP_CONFIG_KEY_NAME = "vappConfig.json";

    private static final String MD5_FILE_NAME = "md5sum.txt";

    private static final String VMX_EXTENSION = ".vmx";

    private static final String NVRAM_EXTENSION = ".nvram";

    private boolean changeTracking;

    private Date timestamp;

    private BackupMode backupMode;

    private ManagedEntityInfo datacenterInfo;

    private int numberOfDisks;

    private int numVmdkdumpSucceeded;

    private int numVmdkdumpFailed;

    private long maxBlockSize;

    private String resourcePoolPath; // VES-ES-WDC/host/Compute/Resources/Max/RP_BackupTest,

    private ManagedEntityInfo resourcePoolInfo;

    private String folderPath; // VES-ES-WDC/vm/max,

    private ManagedEntityInfo folderInfo;

    private ManagedEntityInfo datastoreInfo;

    private String vmxFilename;

    private String nvramFilename;

    private String md5Filename;

    private String vappConfigFilename;

    private String cryptoKeyId;

    private String cryptoKeyProviderId;
    private String encryptionBundle;

    private boolean succeeded;

    private ManagedFcoEntityInfo fcoEntity;
    private ManagedFcoEntityInfo fcoParent;
    private int generationId;

    private final List<DiskProfile> disks;

    private final HashMap<Integer, DiskController> controllers;

    private final List<FcoChildrenGenerationProfile> fcoChildren;

    private FcoGenerationResourceAllocation cpuAllocation;

    private FcoGenerationResourceAllocation memAllocation;

    private final Map<String, FcoNetwork> networks;

    private FcoPbmProfile pbmProfile;

    private Integer previousGenerationId;

    private VirtualMachineConfigSpecOverlay configSpec;

    private Integer numberOfVnics;

    public FcoGenerationProfile() {
        this.disks = new ArrayList<>();
        this.controllers = new HashMap<>();
        this.fcoChildren = new ArrayList<>();
        this.networks = new HashMap<>();
    }

    public FcoGenerationProfile(final FcoGenerationProfile src) {
        this();
        this.changeTracking = src.changeTracking;

        this.timestamp = src.timestamp;

        this.backupMode = src.backupMode;

        this.datacenterInfo = new ManagedEntityInfo(src.datacenterInfo);

        this.numberOfDisks = src.numberOfDisks;

        this.numVmdkdumpSucceeded = src.numVmdkdumpSucceeded;

        this.numVmdkdumpFailed = src.numVmdkdumpFailed;

        this.maxBlockSize = src.maxBlockSize;

        this.resourcePoolPath = src.resourcePoolPath; // VES-ES-WDC/host/Compute/Resources/Max/RP_BackupTest,

        this.resourcePoolInfo = new ManagedEntityInfo(src.resourcePoolInfo);

        this.folderPath = src.folderPath; // VES-ES-WDC/vm/max,

        this.folderInfo = new ManagedEntityInfo(src.folderInfo);

        this.datastoreInfo = new ManagedEntityInfo(src.datastoreInfo);

        this.vmxFilename = src.vmxFilename;

        this.nvramFilename = src.nvramFilename;

        this.md5Filename = src.md5Filename;

        this.vappConfigFilename = src.vappConfigFilename;

        this.cryptoKeyId = src.cryptoKeyId;

        this.cryptoKeyProviderId = src.cryptoKeyProviderId;
        this.encryptionBundle = src.encryptionBundle;

        this.succeeded = src.succeeded;

        this.fcoEntity = new ManagedFcoEntityInfo(src.fcoEntity);
        this.previousGenerationId = src.previousGenerationId;

        this.numberOfVnics = src.numberOfVnics;
        this.generationId = src.generationId;
        this.pbmProfile = new FcoPbmProfile(src.pbmProfile);
        for (final DiskProfile disk : src.disks) {
            this.disks.add(new DiskProfile(disk));
        }
        if (src.cpuAllocation != null) {
            this.cpuAllocation = new FcoGenerationResourceAllocation(src.cpuAllocation);
        }
        if (src.memAllocation != null) {
            this.memAllocation = new FcoGenerationResourceAllocation(src.memAllocation);
        }

        for (final Entry<Integer, DiskController> entry : src.controllers.entrySet()) {
            this.controllers.put(entry.getKey(), new DiskController(entry.getValue()));
        }

        for (final FcoChildrenGenerationProfile child : src.fcoChildren) {
            this.fcoChildren.add(new FcoChildrenGenerationProfile(child));
        }
        for (final Entry<String, FcoNetwork> entry : src.networks.entrySet()) {
            this.networks.put(entry.getKey(), new FcoNetwork(entry.getValue()));
        }
//not a full copy
        this.configSpec = src.configSpec;

    }

    /**
     * @return the backupMode
     */
    public BackupMode getBackupMode() {
        return this.backupMode;
    }

    public VirtualMachineConfigSpecOverlay getConfigSpec() {
        return this.configSpec;
    }

    /**
     * @return
     */
    public Map<Integer, DiskController> getControllers() {
        return this.controllers;
    }

    /**
     * @return the cpuAllocation
     */
    public FcoGenerationResourceAllocation getCpuAllocation() {
        return this.cpuAllocation;
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
     * @return the datacenterInfo
     */
    public ManagedEntityInfo getDatacenterInfo() {
        return this.datacenterInfo;
    }

    /**
     * @return the datastore
     */
    public ManagedEntityInfo getDatastoreInfo() {
        return this.datastoreInfo;
    }

    /**
     * @return the disks
     */
    public List<DiskProfile> getDisks() {
        return this.disks;
    }

    /**
     * @return the encryptionBundle
     */
    public String getEncryptionBundle() {
        return this.encryptionBundle;
    }

    /**
     * @return the fcoChildren
     */
    public List<FcoChildrenGenerationProfile> getFcoChildren() {
        return this.fcoChildren;
    }

    /**
     * @return the fcoEntity
     */
    public ManagedFcoEntityInfo getFcoEntity() {
        return this.fcoEntity;
    }

    public ManagedFcoEntityInfo getFcoParent() {
        return this.fcoParent;
    }

    /**
     * @return the folderInfo
     */
    public ManagedEntityInfo getFolderInfo() {
        return this.folderInfo;
    }

    /**
     * @return the folderPath
     */
    public String getFolderPath() {
        return this.folderPath;
    }

    /**
     * @return the generationId
     */
    public int getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the maxBlockSize
     */
    public long getMaxBlockSize() {
        return this.maxBlockSize;
    }

    /**
     * @return the md5Filename
     */
    public String getMd5Filename() {
        return this.md5Filename;
    }

    /**
     * @return the memAllocation
     */
    public FcoGenerationResourceAllocation getMemAllocation() {
        return this.memAllocation;
    }

    /**
     * @return the networks
     */
    public Map<String, FcoNetwork> getNetworks() {
        return this.networks;
    }

    /**
     * @return the numberOfDisks
     */
    public int getNumberOfDisks() {
        return this.numberOfDisks;
    }

    public Integer getNumberOfVnics() {
        return this.numberOfVnics;
    }

    /**
     * @return the numVmdkdumpFailed
     */
    public int getNumVmdkdumpFailed() {
        return this.numVmdkdumpFailed;
    }

    /**
     * @return the numVmdkdumpSucceeded
     */
    public int getNumVmdkdumpSucceeded() {
        return this.numVmdkdumpSucceeded;
    }

    /**
     * @return the nvramFilename
     */
    public String getNvramFilename() {
        return this.nvramFilename;
    }

    public FcoPbmProfile getPbmProfile() {
        return this.pbmProfile;
    }

    /**
     * @return
     */
    public Integer getPreviousGenerationId() {
        return this.previousGenerationId;
    }

    /**
     * @return the resourcePoolInfo
     */
    public ManagedEntityInfo getResourcePoolInfo() {
        return this.resourcePoolInfo;
    }

    /**
     * @return the resourcePoolPath
     */
    public String getResourcePoolPath() {
        return this.resourcePoolPath;
    }

    /**
     * @return the time-stamp
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    public Map<String, Integer> getVappChildrenUuid() {
        final Map<String, Integer> result = new LinkedHashMap<>();
        for (final FcoChildrenGenerationProfile child : this.fcoChildren) {
            result.put(child.getFcoEntity().getUuid(), child.getGenerationId());

        }
        return result;
    }

    /**
     * @return the vappConfigFilename
     */
    public String getVappConfigKeyName() {
        return this.vappConfigFilename;
    }

    /**
     * @return the vmxFilename
     */
    public String getVmxFilename() {
        return this.vmxFilename;
    }

    /**
     * @return
     */
    public int incrementNumOfFailedVmdkdump() {
        return ++this.numVmdkdumpFailed;
    }

    /**
     * @return
     */
    public int incrementNumOfSucceededVmdkdump() {
        return ++this.numVmdkdumpSucceeded;
    }

    public void initializeDiskGroup(final GenerationProfileSpec spec) {

        for (final VmdkInfo vmdkInfo : spec.getVmdkInfoList()) {
            this.disks.add(new DiskProfile(spec, vmdkInfo));
            if (!this.controllers.containsKey(vmdkInfo.getControllerKey())) {
                final DiskController ctrl = new DiskController();
                ctrl.setAdapterType(vmdkInfo.getAdapterType());
                ctrl.setBusNumber(vmdkInfo.getBusNumber());
                ctrl.setControllerKey(vmdkInfo.getControllerKey());
                ctrl.setAdapterProtocol(AdapterProtocolType.getProtocolType(vmdkInfo.getAdapterType()));
                this.controllers.put(vmdkInfo.getControllerKey(), ctrl);
            }
        }
    }

    public void initializeGenerationGroup(final GenerationProfileSpec spec) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, InterruptedException, PbmFaultFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg,
            InvalidArgumentFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VimObjectNotExistException {

        this.fcoEntity = spec.getFco().getFcoInfo();
        this.fcoParent = spec.getFco().getParentFco();
        this.generationId = spec.getGenId();
        this.changeTracking = spec.getFco().isChangedBlockTrackingEnabled();
        this.timestamp = spec.getCalendar().getTime();
        this.backupMode = BackupMode.UNKNOW;
        this.datacenterInfo = spec.getFco().getDatacenterInfo();
        this.numberOfDisks = 1;
        this.numVmdkdumpSucceeded = 0;
        this.numVmdkdumpSucceeded = 0;
        this.maxBlockSize = spec.getMaxBlockSize();
        this.md5Filename = MD5_FILE_NAME;
        this.previousGenerationId = spec.getPrevGenId();
        switch (spec.getFcoType()) {
        case ivd:
            this.datastoreInfo = ((ImprovedVirtualDisk) spec.getFco()).getDatastoreInfo();
            break;
        case k8s:
            break;
        case vapp:
            this.resourcePoolInfo = spec.getResourcePoolInfo();
            this.resourcePoolPath = spec.getManagedEntityInfoFullPath(this.resourcePoolInfo);
            this.folderInfo = spec.getVmFolderInfo();
            this.folderPath = spec.getManagedEntityInfoFullPath(this.folderInfo);
            initializeGenerationGroup((VirtualAppManager) spec.getFco());
            break;
        case vm:
            this.resourcePoolInfo = spec.getResourcePoolInfo();
            this.resourcePoolPath = spec.getManagedEntityInfoFullPath(this.resourcePoolInfo);
            if (this.resourcePoolInfo.getEntityType() != EntityType.VirtualApp) {
                this.folderInfo = spec.getVmFolderInfo();
                this.folderPath = spec.getManagedEntityInfoFullPath(this.folderInfo);
            }
            this.numberOfDisks = spec.getNumberOfDisks();
            initializeGenerationGroup((VirtualMachineManager) spec.getFco());
            break;
        default:
            break;
        }

    }

    private void initializeGenerationGroup(final VirtualAppManager vApp) {
        if (vApp.isvAppConfigAvailable()) {
            this.vappConfigFilename = VAPP_CONFIG_KEY_NAME;
        }
        this.cpuAllocation = new FcoGenerationResourceAllocation(vApp.getConfig().getCpuAllocation());
        this.memAllocation = new FcoGenerationResourceAllocation(vApp.getConfig().getMemoryAllocation());

        int index = 0;
        for (final VirtualMachineManager fco : vApp.getVmList()) {
            final FcoChildrenGenerationProfile e = new FcoChildrenGenerationProfile();
            e.setFcoEntity(fco.getFcoInfo());
            e.setGenerationId(-1);
            e.setIndex(index);
            this.fcoChildren.add(e);
            ++index;
        }
    }

    private void initializeGenerationGroup(final VirtualMachineManager vmm)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, PbmFaultFaultMsg,
            com.vmware.pbm.RuntimeFaultFaultMsg, InvalidArgumentFaultMsg, VimObjectNotExistException {

        this.datastoreInfo = vmm.getDatastoreInfo();
        this.vmxFilename = vmm.getName() + VMX_EXTENSION;
        this.nvramFilename = vmm.getName() + NVRAM_EXTENSION;
        if (vmm.isvAppConfigAvailable()) {
            this.vappConfigFilename = VAPP_CONFIG_KEY_NAME;
        }

        final CryptoKeyId lCryptoKeyId = vmm.getConfig().getVirtualMachineConfigInfo().getKeyId();
        if (lCryptoKeyId != null) {
            final KeyProviderId providerId = lCryptoKeyId.getProviderId();
            this.cryptoKeyProviderId = lCryptoKeyId.getKeyId();
            this.cryptoKeyId = providerId.getId();
        }
        this.encryptionBundle = vmm.getEncryptionBundle();

        final List<PbmProfileId> pbmProfilesId = vmm.getAssociatedProfile();
        if (!pbmProfilesId.isEmpty()) {

            final List<PbmProfile> vimPbmProfiles = vmm.getVimConnection().getPbmConnection()
                    .pbmRetrieveContent(pbmProfilesId);
            if (!vimPbmProfiles.isEmpty()) {
                this.pbmProfile = new FcoPbmProfile(vimPbmProfiles.get(0));
            }
        }

    }

    /**
     * @param spec
     */
    public void initializeIvdDiskGroup(final GenerationProfileSpec spec) {
        this.disks.add(new DiskProfile(spec));

    }

    /**
     * @return the changeTracking
     */
    public boolean isChangeTracking() {
        return this.changeTracking;
    }

    /**
     * @return the succeeded
     */
    public boolean isSucceeded() {
        return this.succeeded;
    }

    public void reconfigureGeneration(final int generationId) {

        this.generationId = generationId;
        this.backupMode = BackupMode.FULL;
        this.previousGenerationId = -1;
        for (final DiskProfile disk : this.disks) {
            disk.getDumps().clear();
            disk.setBackupMode(BackupMode.FULL);

        }

    }

    /**
     * @param backupMode the backupMode to set
     */
    public void setBackupMode(final BackupMode backupMode) {
        this.backupMode = backupMode;
    }

    /**
     * @param changeTracking the changeTracking to set
     */
    public void setChangeTracking(final boolean changeTracking) {
        this.changeTracking = changeTracking;
    }

    public void setConfigSpec(final VirtualMachineConfigSpecOverlay configSpec) {
        this.configSpec = configSpec;
    }

    /**
     * @param cpuAllocation the cpuAllocation to set
     */
    public void setCpuAllocation(final FcoGenerationResourceAllocation cpuAllocation) {
        this.cpuAllocation = cpuAllocation;
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
     * @param datacenterInfo the datacenterInfo to set
     */
    public void setDatacenterInfo(final ManagedEntityInfo datacenterInfo) {
        this.datacenterInfo = datacenterInfo;
    }

    /**
     * @param datastore the datastore to set
     */
    public void setDatastore(final ManagedEntityInfo datastore) {
        this.datastoreInfo = datastore;
    }

    /**
     * @param encryptionBundle the encryptionBundle to set
     */
    public void setEncryptionBundle(final String encryptionBundle) {
        this.encryptionBundle = encryptionBundle;
    }

    /**
     * @param fcoEntity the fcoEntity to set
     */
    public void setFcoEntity(final ManagedFcoEntityInfo fcoEntity) {
        this.fcoEntity = fcoEntity;
    }

    public void setFcoParent(final ManagedFcoEntityInfo parent) {
        this.fcoParent = parent;
    }

    /**
     * @param folderInfo the folderInfo to set
     */
    public void setFolderInfo(final ManagedEntityInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    /**
     * @param folderPath the folderPath to set
     */
    public void setFolderPath(final String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * @param generationId the generationId to set
     */
    public void setGenerationId(final int generationId) {
        this.generationId = generationId;
    }

    /**
     * @param maxBlockSize the maxBlockSize to set
     */
    public void setMaxBlockSize(final long maxBlockSize) {
        this.maxBlockSize = maxBlockSize;
    }

    /**
     * @param md5Filename the md5Filename to set
     */
    public void setMd5Filename(final String md5Filename) {
        this.md5Filename = md5Filename;
    }

    /**
     * @param memAllocation the memAllocation to set
     */
    public void setMemAllocation(final FcoGenerationResourceAllocation memAllocation) {
        this.memAllocation = memAllocation;
    }

    /**
     * @param numberOfDisks the numberOfDisks to set
     */
    public void setNumberOfDisks(final int numberOfDisks) {
        this.numberOfDisks = numberOfDisks;
    }

    public void setNumberOfVnics(final int numberOfVnics) {
        this.numberOfVnics = numberOfVnics;
    }

    /**
     * @param numVmdkdumpFailed the numVmdkdumpFailed to set
     */
    public void setNumVmdkdumpFailed(final int numVmdkdumpFailed) {
        this.numVmdkdumpFailed = numVmdkdumpFailed;
    }

    /**
     * @param numVmdkdumpSucceeded the numVmdkdumpSucceeded to set
     */
    public void setNumVmdkdumpSucceeded(final int numVmdkdumpSucceeded) {
        this.numVmdkdumpSucceeded = numVmdkdumpSucceeded;
    }

    /**
     * @param nvramFilename the nvramFilename to set
     */
    public void setNvramFilename(final String nvramFilename) {
        this.nvramFilename = nvramFilename;
    }

    public void setPbmProfile(final FcoPbmProfile pbmProfile) {
        this.pbmProfile = pbmProfile;
    }

    /**
     * @param previousGenerationId the previousGenerationId to set
     */
    public void setPreviousGenerationId(final Integer previousGenerationId) {
        this.previousGenerationId = previousGenerationId;
    }

    /**
     * @param resourcePoolInfo the resourcePoolInfo to set
     */
    public void setResourcePoolInfo(final ManagedEntityInfo resourcePoolInfo) {
        this.resourcePoolInfo = resourcePoolInfo;
    }

    /**
     * @param resourcePoolPath the resourcePoolPath to set
     */
    public void setResourcePoolPath(final String resourcePoolPath) {
        this.resourcePoolPath = resourcePoolPath;
    }

    /**
     * @param succeeded the succeeded to set
     */
    public void setSucceeded(final boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param vappConfigFilename the vappConfigFilename to set
     */
    public void setVappConfigKeyName(final String vappConfigFilename) {
        this.vappConfigFilename = vappConfigFilename;
    }

    /**
     * @param vmxFilename the vmxFilename to set
     */
    public void setVmxFilename(final String vmxFilename) {
        this.vmxFilename = vmxFilename;
    }

}
