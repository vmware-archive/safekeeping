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
package com.vmware.vmbk.profile;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.pbm.PbmProfile;
import com.vmware.pbm.PbmProfileId;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfo;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.KeyProviderId;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ResourceAllocationInfo;
import com.vmware.vim25.SharesInfo;
import com.vmware.vim25.SharesLevel;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.AdapterType;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.VmdkInfo;
import com.vmware.vmbk.util.Utility;

public class GenerationProfile extends Profile {

    private static final String ADAPTER_PROTOCOL_DISK_KEY = "adapterProtocol";
    private static final String ADAPTERTYPE_CONTROLLER_KEY = "adapterType";
    private static final String BACKUPMODE_KEY = "backupMode";
    private static final String BUSNUMBER_CONTROLLER_KEY = "busNumber";
    private static final String CAPACITY_DISK_KEY = "capacity";
    private static final String CHANGE_ID_DISK_KEY = "changeId";
    private static final String CHANGE_TRACKING_KEY = "changeTracking";
    private static final String COMPRESSION_DISK_KEY = "compression";
    private static final String CONTROLLER_GROUP_KEY = "controller";
    private static final String CONTROLLERKEY_KEY = "controllerKey";
    private static final String CRYPTOKEY_ID_KEY = "cryptoKeyId";

    private static final String CRYPTOKEY_PROVIDERID_KEY = "cryptoKeyProviderId";
    private static final String CRYPTOKEYID_DISK_KEY = "cryptoKeyId";
    private static final String CRYPTOKEYPROVIDERID_DISK_KEY = "cryptoKeyProviderId";
    private static final String DATACENTER_NAME_KEY = "datacenterName";
    private static final String DATASTORE_NAME_KEY = "datastoreName";
    private static final String DEVICEKEY_KEY = "devicekeyKey";
    private static final String DIGESTENABLED_DISK_KEY = "digestEnabled";
    private static final String DISK_GROUP_KEY = "disk";
    private static final String DISKMODE_DISK_KEY = "diskMode";
    private static final String DUMP_BEGIN_TIMESTAMP_MS_DISK_KEY = "dumpBeginTimestampMs";
    private static final String DUMP_END_TIMESTAMP_MS_DISK_KEY = "dumpEndTimestampMs";
    private static final String ENCRYPTION_BUNDLE_KEY = "encryptionBundle";
    private static final String ENTITY_TYPE_KEY = "entity";
    private static final String FILENAME_BTR_DISK_KEY = "filenameBlocksTrack";
    private static final String FILENAME_DUMP_DISK_KEY = "filenameDump";
    private static final String FIRMWARE_KEY = "firmware";

    private static final String FOLDER_PATH_KEY = "folderPath";
    private static final String GENERATION_GROUP_KEY = "generation";
    private static final String GENERATION_ID_GEN_KEY = "generationId";

    private static final String INDEX_GROUP_KEY = "index";
    private static final String INSTANCE_UUID_KEY = "instanceUuid";
    private static final String IOFILTER_KEY = "ioFilter";
    private static final String IS_CHANGED_DISK_KEY = "isChanged";
    private static final String IS_DELETED_PREVIOUS_DUMP_DISK_KEY = "isDeletedPreviousDump";
    private static final String IVD_DISK_KEY = "ivdDisk";

    private static final String IVD_IDKEY = "ivdID";

    private static final String MD5_FILE_NAME = "md5sum.txt";
    private static final String MOREF_GEN_KEY = "moref";

    private static final String MOREF_NETWORK_KEY = "moref";

    private static final String NAME_GEN_KEY = "name";
    private static final String NAME_NETWORK_KEY = "name";
    private static final String NETWORK_GROUP_KEY = "network";
    private static final String NUM_VMDKDUMP_FAILED_GEN_KEY = "numVmdkdumpFailed";
    private static final String NUM_VMDKDUMP_SUCCEEDED_GEN_KEY = "numVmdkdumpSucceeded";

    private static final String NUMBER_OF_DISKS_GEN_KEY = "numberOfDisks";
    private static final String NUMBER_OF_NETWORKS_GEN_KEY = "numberOfNetworks";
    private static final String NVRAM_FILENAME_GEN_KEY = "nvramFilename";
    private static final String PBM_PROFILE_ID_KEY = "pbmProfileId";
    private static final String PBM_PROFILE_NAME_KEY = "pbmProfileName";

    private static final String PREV_GENERATION_ID_GEN_KEY = "prevGenerationId";

    private static final String PROFILE_VERSION_KEY = "profile.Version";

    private static final String RDIFF_TIMESTAMP_MS_DISK_KEY = "rdiffTimestamp_ms";

    private static final String REMOTE_PATH_DISK_KEY = "remotePath";

    private static final String REPORT_DISK_KEY = "report";

    private static final String REPORT_FILE_EXTENSION = ".report";

    private static final String RESOURCE_POOL_PATH_KEY = "resourcePoolPath";

    private static final String STATUS_DISK_KEY = "succeeded";

    private static final String STATUS_GEN_KEY = "succeeded";

    private static final String TIMESTAMP_GEN_KEY = "timestamp";

    private static final String TIMESTAMP_MS_GEN_KEY = "timestampMs";

    private static final String TYPE_NETWORK_KEY = "type";

    private static final String UNITNUMBER_DISK_KEY = "unitNumber";

    private static final String UUID_DISK_KEY = "uuid";

    private static final String VAPP_CONFIG_KEY = "vappConfig";
    private static final String VMX_DISK_ENTRY_DISK_KEY = "vmxDiskEntry";
    private static final String VMX_FILENAME_GEN_KEY = "vmxFilename";
    private static final String WRITETHROUGH_DISK_KEY = "writeThrough";
    private static final String CPU_EXPANDABLE_RESERVATION_KEY = "cpuExpandableReservation";
    private static final String CPU_LIMIT_KEY = "cpuLimit";
    private static final String CPU_OVERHEAD_LIMIT_KEY = "cpuOverheadLimit";
    private static final String CPU_RESERVATION_KEY = "cpuReservation";
    private static final String CPU_SHARES_LEVEL_KEY = "cpuSharesLevel";
    private static final String CPU_SHARES_KEY = "cpuShares";
    private static final String MEM_EXPANDABLE_RESERVATION_KEY = "memExpandableReservation";
    private static final String MEM_LIMIT_KEY = "memLimit";
    private static final String MEM_OVERHEAD_LIMIT_KEY = "memOverheadLimit";
    private static final String MEM_RESERVATION_KEY = "memReservation";
    private static final String MEM_SHARES_LEVEL_KEY = "memSharesLevel";
    private static final String MEM_SHARES_KEY = "memShares";
    private static final String CHILDREN_NUMBER_KEY = "childrenNumber";
    private static final String CHILD_KEY = "child";
    private static final String CHILD_GROUP_KEY = "child";

    private static final String PROVISIONING_TYPE_KEY = "provisioningType";

    private String generation;

//    private String generateControllerIndexGroup() {
//	return this.iniConfiguration.createSectionName(INDEX_GROUP_KEY, CONTROLLER_GROUP_KEY);
//    }

    public GenerationProfile() {
	super();
    }

    public GenerationProfile(final byte[] byteArray) throws IOException {
	super(byteArray);
    }

    private String generateChildGroup(final int num) {
	return this.valuesMap.createSectionName(CHILD_GROUP_KEY, num);
    }

    private String generateControllerGroup(final int ckey) {
	return this.valuesMap.createSectionName(CONTROLLER_GROUP_KEY, ckey);
    }

//    private String generateNetworkIndexGroup() {
//	return this.iniConfiguration.createSectionName(INDEX_GROUP_KEY, NETWORK_GROUP_KEY);
//    }

    private String generateDiskGroup(final int diskId) {
	return this.valuesMap.createSectionName(DISK_GROUP_KEY, diskId);
    }

    private String generateDiskIndexGroup() {
	return this.valuesMap.createSectionName(INDEX_GROUP_KEY, DISK_GROUP_KEY);
    }

    private String generateNetworkGroup(final int networkId) {
	return this.valuesMap.createSectionName(NETWORK_GROUP_KEY, networkId);
    }

    public AdapterType getAdapterType(final int diskId) {
	final String typeStr = this.valuesMap.getStringProperty(generateDiskGroup(diskId), ADAPTERTYPE_CONTROLLER_KEY);
	if (typeStr == null) {
	    return AdapterType.UNKNOWN;
	} else {
	    return AdapterType.parse(typeStr);
	}
    }

    public BackupMode getBackupMode() {
	final BackupMode backupMode = BackupMode
		.parse(this.valuesMap.getStringProperty(this.generation, BACKUPMODE_KEY));
	logger.fine(String.format("Get Value backupMode:%s\n", backupMode.toString()));
	return backupMode;
    }

    private String getBlocksTrackOutFileName(final int diskId) {
	return getDiskGroupValAsAutoString(diskId, FILENAME_BTR_DISK_KEY);
    }

    public int getBusNumber(final int ckey) {
	return this.valuesMap.getIntegerProperty(generateControllerGroup(ckey), BUSNUMBER_CONTROLLER_KEY);
    }

    public long getCapacity(final int diskId) {
	return this.valuesMap.getLongProperty(generateDiskGroup(diskId), CAPACITY_DISK_KEY);
    }

    public String getChangeId(final int diskId) {
	return getDiskGroupValAsAutoString(diskId, CHANGE_ID_DISK_KEY);
    }

    public int getControllerDeviceKey(final int diskId) {
	return this.valuesMap.getIntegerProperty(generateDiskGroup(diskId), CONTROLLERKEY_KEY);
    }

    public ResourceAllocationInfo getCpuAllocation() {
	final ResourceAllocationInfo cpuAllocation = new ResourceAllocationInfo();
	cpuAllocation.setExpandableReservation(
		this.valuesMap.getBooleanProperty(this.generation, CPU_EXPANDABLE_RESERVATION_KEY));
	cpuAllocation.setLimit(this.valuesMap.getLongProperty(this.generation, CPU_LIMIT_KEY));// ,
											       // cpuAllocation.getLimit());
	cpuAllocation.setOverheadLimit(this.valuesMap.getLongProperty(this.generation, CPU_OVERHEAD_LIMIT_KEY));
	cpuAllocation.setReservation(this.valuesMap.getLongProperty(this.generation, CPU_RESERVATION_KEY));
	final SharesInfo shareInfo = new SharesInfo();

	shareInfo
		.setLevel(SharesLevel.valueOf(this.valuesMap.getStringProperty(this.generation, CPU_SHARES_LEVEL_KEY)));
	shareInfo.setShares(this.valuesMap.getIntegerProperty(this.generation, CPU_SHARES_KEY));
	cpuAllocation.setShares(shareInfo);
	return cpuAllocation;
    }

    public String getCryptoKeyId(final int diskId) {
	return this.valuesMap.getStringProperty(generateDiskGroup(diskId), CRYPTOKEYID_DISK_KEY);
    }

    public String getCryptoKeyProviderId(final int diskId) {
	return this.valuesMap.getStringProperty(generateDiskGroup(diskId), CRYPTOKEYPROVIDERID_DISK_KEY);
    }

    public String getDigestContentPath(final int diskId) {
	final String st = String.format("%s/%d/%s", getInstanceUuid(), getGenerationId(),
		getBlocksTrackOutFileName(diskId));
	return st;
    }

    public String getDigestContentPath(final int generationId, final int diskId) {
	final String st = String.format("%s/%s", getDumpPath(generationId), getBlocksTrackOutFileName(diskId));
	return st;
    }

    public BackupMode getDiskBackupMode(final int diskId) {
	final BackupMode backupMode = BackupMode
		.parse(this.valuesMap.getStringProperty(generateDiskGroup(diskId), BACKUPMODE_KEY));
	logger.fine(String.format("Get Value disk (%d) BackupMode:%s\n", diskId, backupMode.toString()));
	return backupMode;
    }

    public int getDiskDeviceKey(final int diskId) {
	return this.valuesMap.getIntegerProperty(generateDiskGroup(diskId), DEVICEKEY_KEY);
    }

    private String getDiskGroupValAsAutoString(final int diskId, final String key) {
	final String val = this.valuesMap.getStringProperty(generateDiskGroup(diskId), key);

	if (val != null) {
	    return val;

	} else {
	    return null;
	}
    }

    private boolean getDiskGroupValAsBoolean(final int diskId, final String key) {
	final Boolean result = this.valuesMap.getBooleanProperty(generateDiskGroup(diskId), key);
	return result;

    }

    private String getDiskGroupValAsQString(final int diskId, final String key) {
	final String val = this.valuesMap.getStringProperty(generateDiskGroup(diskId), key);

	if (val != null) {
	    return val;

	} else {
	    return null;
	}
    }

    public List<Integer> getDiskIdList() {
	final List<Integer> ret = new LinkedList<>();

	final String diskIndexGroup = generateDiskIndexGroup();
	final HashMap<String, String> entryList = this.valuesMap.getProperties(diskIndexGroup);
	for (final String key : entryList.keySet()) {
	    final String uuid = key;
	    final String diskIdStr = entryList.get(key);

	    final int diskId = Integer.parseInt(diskIdStr);
	    assert diskId >= 0;
	    assert getDiskIdWithUuid(uuid) == diskId;

	    ret.add(new Integer(diskId));
	}

	Collections.sort(ret);
	return ret;
    }

    public int getDiskIdWithUuid(final String uuid) {
	final String diskIndexGroup = generateDiskIndexGroup();
	final Integer iStr = this.valuesMap.getIntegerProperty(diskIndexGroup, uuid);
	if ((iStr == null)) {
	    return -1;
	}
	return iStr;
    }

    private String getDiskMode(final int diskId) {
	return this.valuesMap.getStringProperty(generateDiskGroup(diskId), DISKMODE_DISK_KEY);
    }

    public LinkedList<String> getDiskPbmProfileName(final int diskId) {
	return this.valuesMap.getStringPropertyArray(generateDiskGroup(diskId), PBM_PROFILE_NAME_KEY);
    }

    public BaseConfigInfoDiskFileBackingInfoProvisioningType getDiskProvisionType(final int diskId) {
	final String typeStr = this.valuesMap.getStringProperty(generateDiskGroup(diskId), PROVISIONING_TYPE_KEY);
	if (typeStr == null) {
	    return BaseConfigInfoDiskFileBackingInfoProvisioningType.LAZY_ZEROED_THICK;
	} else {
	    return BaseConfigInfoDiskFileBackingInfoProvisioningType.fromValue(typeStr);
	}
    }

    public String getDumpContentPath(final int diskId, final int index) {
	final String st = String.format("%s/%d/%s", getInstanceUuid(), getGenerationId(),
		getDumpOutFileName(diskId, index));
	return st;
    }

    public String getDumpContentPath(final int genearationId, final int diskId, final int index) {
	final String st = String.format("%s/%d/%s", getInstanceUuid(), genearationId,
		getDumpOutFileName(diskId, index));
	return st;
    }

    public long getDumpElapsedTimeMs(final int diskId) {
	final String diskGroup = generateDiskGroup(diskId);

	final Long beginMs = this.valuesMap.getLongProperty(diskGroup, DUMP_BEGIN_TIMESTAMP_MS_DISK_KEY);
	final Long endMs = this.valuesMap.getLongProperty(diskGroup, DUMP_END_TIMESTAMP_MS_DISK_KEY);
	if ((beginMs == null) || (endMs == null)) {
	    logger.warning("beginStr or endStr is is null.");
	    return -1;
	}

	return endMs - beginMs;
    }

    private String getDumpOutFileName(final int diskId, final int index) {
	String ret = getDiskGroupValAsAutoString(diskId, FILENAME_DUMP_DISK_KEY);
	ret = ret.replaceAll("\\.", String.format("-%06d.", index));
	return ret;
    }

    public String getDumpPath() {
	final String st = String.format("%s/%d", getInstanceUuid(), getGenerationId());
	return st;
    }

    public String getDumpPath(final int generationId) {
	final String st = String.format("%s/%d", getInstanceUuid(), generationId);
	return st;
    }

    public EntityType getEntityType() {
	return this.valuesMap.getEntityTypeProperty(this.generation, ENTITY_TYPE_KEY);
    }

    public int getGenerationId() {
	return this.valuesMap.getIntegerProperty(this.generation, GENERATION_ID_GEN_KEY);
    }

    public String getGenerationPath() {
	return String.format("%s/%d/", getInstanceUuid(), getGenerationId());
    }

    public String getGenerationProfileContentPath() {
	return String.format("%s/%d/%s", getInstanceUuid(), getGenerationId(),
		GlobalConfiguration._GENERATION_PROFILE_FILENAME);
    }

    public String getGenerationProfileContentPath(final int genId) {
	return String.format("%s/%d/%s", getInstanceUuid(), genId, GlobalConfiguration._GENERATION_PROFILE_FILENAME);
    }

    public String getImprovedVirtualDiskId(final int diskId) {
	return this.valuesMap.getStringProperty(generateDiskGroup(diskId), IVD_IDKEY);
    }

    public String getInstanceUuid() {
	final String result = this.valuesMap.getStringProperty(this.generation, INSTANCE_UUID_KEY);
	return result;
    }

    public String getIoFilter(final int diskId) {
	return this.valuesMap.getStringProperty(generateDiskGroup(diskId), IOFILTER_KEY);
    }

    /*
     * Network Section
     */

    public String getMd5ContentPath() {
	final String st = String.format("%s/%s", getDumpPath(), GenerationProfile.MD5_FILE_NAME);
	return st;
    }

    public String getMd5ContentPath(final int generationId) {
	final String st = String.format("%s/%s", getDumpPath(generationId), GenerationProfile.MD5_FILE_NAME);
	return st;
    }

    public ResourceAllocationInfo getMemAllocation() {
	final ResourceAllocationInfo memAllocation = new ResourceAllocationInfo();
	memAllocation.setExpandableReservation(
		this.valuesMap.getBooleanProperty(this.generation, MEM_EXPANDABLE_RESERVATION_KEY));
	memAllocation.setLimit(this.valuesMap.getLongProperty(this.generation, MEM_LIMIT_KEY));
	memAllocation.setOverheadLimit(this.valuesMap.getLongProperty(this.generation, MEM_OVERHEAD_LIMIT_KEY));
	memAllocation.setReservation(this.valuesMap.getLongProperty(this.generation, MEM_RESERVATION_KEY));
	final SharesInfo shareInfo = new SharesInfo();

	shareInfo
		.setLevel(SharesLevel.valueOf(this.valuesMap.getStringProperty(this.generation, MEM_SHARES_LEVEL_KEY)));
	shareInfo.setShares(this.valuesMap.getIntegerProperty(this.generation, MEM_SHARES_KEY));
	memAllocation.setShares(shareInfo);
	return memAllocation;
    }

    public String getMoref() {
	return this.valuesMap.getStringProperty(this.generation, MOREF_GEN_KEY);
    }

    public String getName() {
	return this.valuesMap.getStringProperty(this.generation, NAME_GEN_KEY);
    }

    public String getNetworkName(final int networkId) {
	return this.valuesMap.getStringProperty(generateNetworkGroup(networkId), NAME_NETWORK_KEY);
    }

    public int getNumberOfDisks() {
	return this.valuesMap.getIntegerProperty(this.generation, NUMBER_OF_DISKS_GEN_KEY);
    }

    public int getNumberOfNetworks() {
	return this.valuesMap.getIntegerProperty(this.generation, NUMBER_OF_NETWORKS_GEN_KEY);
    }

    private int getNumOfFailedVmdkdump() {
	final int ret = this.valuesMap.getIntegerProperty(this.generation, NUM_VMDKDUMP_FAILED_GEN_KEY);
	if (ret < 0) {
	    logger.warning("num_vmdkdump_failed is not set or not an integer.");
	}
	return ret;
    }

    private int getNumOfSucceededVmdkdump() {
	final int ret = this.valuesMap.getIntegerProperty(this.generation, NUM_VMDKDUMP_SUCCEEDED_GEN_KEY);
	if (ret < 0) {
	    logger.warning("num_vmdkdump_succeeded is not set or not an integer.");
	}
	return ret;
    }

    public String getNvramContentPath() {

	final String nvramFilename = this.valuesMap.getStringProperty(this.generation, NVRAM_FILENAME_GEN_KEY);
	if (nvramFilename == null) {
	    return null;
	}
	return String.format("%s/%d/%s", getInstanceUuid(), getGenerationId(), nvramFilename);

    }

    public String getOriginalDatacenter() {
	logger.entering(getClass().getName(), "getOriginalDatacenter");
	final String op = this.valuesMap.getStringProperty(this.generation, DATACENTER_NAME_KEY);
	logger.exiting(getClass().getName(), "getOriginalDatacenter", op);
	return op;

    }

    public String getOriginalDatastore() {
	logger.entering(getClass().getName(), "getOriginalDatastore");
	final String op = this.valuesMap.getStringProperty(this.generation, DATASTORE_NAME_KEY);
	logger.exiting(getClass().getName(), "getOriginalDatastore", op);
	return op;
    }

    public String getOriginalFolderPath() {
	logger.entering(getClass().getName(), "getOriginalFolderPath");
	String op = this.valuesMap.getStringProperty(this.generation, FOLDER_PATH_KEY);
	if ((op != null) && op.startsWith("Datacenters")) {
	    op = op.substring(op.indexOf('/'));
	}
	logger.exiting(getClass().getName(), "getOriginalFolderPath", op);
	return op;
    }

    public String getOriginalResourcePoolPath() {
	logger.entering(getClass().getName(), "getOriginalResourcePoolPath");
	final String op = this.valuesMap.getStringProperty(this.generation, RESOURCE_POOL_PATH_KEY);

	logger.exiting(getClass().getName(), "getOriginalResourcePoolPath", op);
	return op;
    }

    public LinkedList<String> getPbmProfileId() {
	return this.valuesMap.getStringPropertyArray(this.generation, PBM_PROFILE_ID_KEY);
    }

    public LinkedList<String> getPbmProfileId(final int diskId) {
	return this.valuesMap.getStringPropertyArray(generateDiskGroup(diskId), PBM_PROFILE_ID_KEY);
    }

    public LinkedList<String> getPbmProfileName() {
	return this.valuesMap.getStringPropertyArray(this.generation, PBM_PROFILE_NAME_KEY);
    }

    public LinkedList<String> getPbmProfileName(final int diskId) {
	return this.valuesMap.getStringPropertyArray(generateDiskGroup(diskId), PBM_PROFILE_NAME_KEY);
    }

    public int getPreviousGenerationId() {
	final int previousGenerationId = this.valuesMap.getIntegerProperty(this.generation, PREV_GENERATION_ID_GEN_KEY);
	logger.fine(String.format("Value previousGenerationId:%d\n", previousGenerationId));
	return previousGenerationId;
    }

    public String getPreviousGenerationIdStr() {
	return this.valuesMap.getStringProperty(this.generation, PREV_GENERATION_ID_GEN_KEY);
    }

    public String getPreviousGenerationProfileContentPath() {
	return String.format("%s/%d/%s", getInstanceUuid(), getPreviousGenerationId(),
		GlobalConfiguration._GENERATION_PROFILE_FILENAME);
    }

    public String getRemoteDiskPath(final int diskId) {
	return getDiskGroupValAsQString(diskId, REMOTE_PATH_DISK_KEY);
    }

    public String getReportContentPath(final int diskId) {
	final String st = String.format("%s/%d%s", getDumpPath(), diskId, GenerationProfile.REPORT_FILE_EXTENSION);
	return st;
    }

    public String getReportFileName(final int diskId) {
	return getDiskGroupValAsAutoString(diskId, REPORT_DISK_KEY);
    }

    public String getTimestampMs() {
	return this.valuesMap.getStringProperty(this.generation, TIMESTAMP_MS_GEN_KEY);
    }

    public int getUnitNumber(final int diskId) {
	return this.valuesMap.getIntegerProperty(generateDiskGroup(diskId), UNITNUMBER_DISK_KEY);
    }

    public String getUuid(final int diskId) {
	return getDiskGroupValAsAutoString(diskId, UUID_DISK_KEY);
    }

    public LinkedHashMap<String, Integer> getVappChildrenUuid() {
	final LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
	final Integer size = this.valuesMap.getIntegerProperty(this.generation, CHILDREN_NUMBER_KEY, 0);
//
//	    this.valuesMap.setIntegerProperty(sectionName, GENERATION_ID_GEN_KEY, -1);
//	    this.valuesMap.setStringProperty(sectionName, INSTANCE_UUID_KEY, vApp.getVmList().get(i).getUuid());
//	    this.valuesMap.setStringProperty(sectionName, NAME_GEN_KEY, vApp.getVmList().get(i).getName());
//	    this.valuesMap.setEntityTypeProperty(sectionName, ENTITY_TYPE_KEY, vApp.getVmList().get(i).getEntityType());
//	    this.valuesMap.setStringProperty(sectionName, MOREF_GEN_KEY, vApp.getVmList().get(i).getMorefValue());
	for (Integer i = 0; i < size; i++) {
	    final String sectionName = generateChildGroup(i);
	    result.put(this.valuesMap.getStringProperty(sectionName, INSTANCE_UUID_KEY),
		    this.valuesMap.getIntegerProperty(sectionName, GENERATION_ID_GEN_KEY, -1));
	}
	return result;
    }

    public String getvAppConfigPath() {

	final String vAppConfig = this.valuesMap.getStringProperty(this.generation, VAPP_CONFIG_KEY);
	if (vAppConfig == null) {
	    return null;
	}
	return String.format("%s/%d/%s", getInstanceUuid(), getGenerationId(), vAppConfig);

    }

    public String getVmxContentPath() {
	final String vmxFilename = this.valuesMap.getStringProperty(this.generation, VMX_FILENAME_GEN_KEY);
	if (vmxFilename == null) {
	    return null;
	}
	return String.format("%s/%d/%s", getInstanceUuid(), getGenerationId(), vmxFilename);

    }

    public String getVmxDiskEntry(final int diskId) {
	final String diskGroup = generateDiskGroup(diskId);
	return this.valuesMap.getStringProperty(diskGroup, VMX_DISK_ENTRY_DISK_KEY);
    }

    private void incrementNumOfFailedVmdkdump() {
	int count = getNumOfFailedVmdkdump();
	count++;
	this.valuesMap.setIntegerProperty(this.generation, NUM_VMDKDUMP_FAILED_GEN_KEY, count);
    }

    private void incrementNumOfSucceededVmdkdump() {
	int count = getNumOfSucceededVmdkdump();
	count++;
	this.valuesMap.setIntegerProperty(this.generation, NUM_VMDKDUMP_SUCCEEDED_GEN_KEY, count);
    }

    private void initializeDiskGroup(final GenerationProfileSpec spec) {

	logger.entering(getClass().getName(), "initializeDiskGroup", new Object[] { spec });
	int diskId = 0;
	final VirtualMachineManager vmm = (VirtualMachineManager) spec.getFco();
	for (final VmdkInfo vmdkInfo : spec.getVmdkInfoList()) {
	    final String iStr = Integer.toString(diskId);
	    final String diskGroup = generateDiskGroup(diskId);
	    this.valuesMap.setStringProperty(diskGroup, REMOTE_PATH_DISK_KEY, vmdkInfo.getName());
	    this.valuesMap.setStringProperty(diskGroup, UUID_DISK_KEY, vmdkInfo.getUuid());
	    this.valuesMap.setLongProperty(diskGroup, CAPACITY_DISK_KEY, vmdkInfo.getCapacity(),
		    PrettyNumber.MetricPrefix.kilo);
	    this.valuesMap.setBooleanProperty(diskGroup, IVD_DISK_KEY, (vmdkInfo.getImprovedVirtualDiskId() != null));
	    if (vmdkInfo.getImprovedVirtualDiskId() != null) {
		this.valuesMap.setStringProperty(diskGroup, IVD_IDKEY, vmdkInfo.getImprovedVirtualDiskId().getId());
	    }
	    if (vmdkInfo.getChangeId() != null) {
		this.valuesMap.setStringProperty(diskGroup, CHANGE_ID_DISK_KEY, vmdkInfo.getChangeId());
	    } else {
		this.valuesMap.setStringProperty(diskGroup, CHANGE_ID_DISK_KEY, "*");
	    }
	    if (spec.getBackupInfo() != null) {
		this.valuesMap.setBooleanProperty(diskGroup, COMPRESSION_DISK_KEY,
			spec.getBackupInfo().isCompression());
		this.valuesMap.setStringProperty(diskGroup, BACKUPMODE_KEY, "unknown");
		this.valuesMap.setLongProperty(diskGroup, RDIFF_TIMESTAMP_MS_DISK_KEY, -1);
		this.valuesMap.setStringProperty(diskGroup, FILENAME_DUMP_DISK_KEY, iStr + ".dump");
		this.valuesMap.setStringProperty(diskGroup, FILENAME_BTR_DISK_KEY, iStr + ".btr");

		this.valuesMap.setStringProperty(diskGroup, REPORT_DISK_KEY, iStr + REPORT_FILE_EXTENSION);

		this.valuesMap.setBooleanProperty(diskGroup, IS_DELETED_PREVIOUS_DUMP_DISK_KEY, false);
		this.valuesMap.setStringProperty(diskGroup, IS_CHANGED_DISK_KEY, "undefined");

		this.valuesMap.setLongProperty(diskGroup, DUMP_BEGIN_TIMESTAMP_MS_DISK_KEY, -1);
		this.valuesMap.setLongProperty(diskGroup, DUMP_END_TIMESTAMP_MS_DISK_KEY, -1);

		this.valuesMap.setBooleanProperty(diskGroup, STATUS_DISK_KEY, Boolean.FALSE);

	    }
	    this.valuesMap.setStringProperty(diskGroup, ADAPTERTYPE_CONTROLLER_KEY,
		    vmdkInfo.getAdapterType().toString());
	    this.valuesMap.setStringProperty(diskGroup, ADAPTER_PROTOCOL_DISK_KEY,
		    vmdkInfo.getAdapterType().toTypeString());
	    this.valuesMap.setStringProperty(diskGroup, VMX_DISK_ENTRY_DISK_KEY, String.format("%s%d:%d",
		    vmdkInfo.getAdapterType().toTypeString(), vmdkInfo.getBusNumber(), vmdkInfo.getUnitNumber()));

	    this.valuesMap.setIntegerProperty(diskGroup, CONTROLLERKEY_KEY, vmdkInfo.getControllerKey());
	    this.valuesMap.setIntegerProperty(diskGroup, DEVICEKEY_KEY, vmdkInfo.getKey());
	    this.valuesMap.setIntegerProperty(diskGroup, BUSNUMBER_CONTROLLER_KEY, vmdkInfo.getBusNumber());
	    this.valuesMap.setIntegerProperty(diskGroup, UNITNUMBER_DISK_KEY, vmdkInfo.getUnitNumber());

	    this.valuesMap.setStringProperty(diskGroup, DISKMODE_DISK_KEY, vmdkInfo.getDiskMode());
	    this.valuesMap.setStringProperty(diskGroup, PROVISIONING_TYPE_KEY, vmdkInfo.getProvisioningType().value());
	    this.valuesMap.setBooleanProperty(diskGroup, DIGESTENABLED_DISK_KEY, vmdkInfo.isDigestEnabled());
	    this.valuesMap.setBooleanProperty(diskGroup, WRITETHROUGH_DISK_KEY, vmdkInfo.isWriteThrough());
	    if (vmdkInfo.getCryptoKeyId() != null) {
		this.valuesMap.setStringProperty(diskGroup, CRYPTOKEYID_DISK_KEY, vmdkInfo.getCryptoKeyId().getKeyId());
		this.valuesMap.setStringProperty(diskGroup, CRYPTOKEYPROVIDERID_DISK_KEY,
			vmdkInfo.getCryptoKeyId().getProviderId().getId());
	    } else {
		this.valuesMap.setStringProperty(diskGroup, CRYPTOKEYID_DISK_KEY, "");
		this.valuesMap.setStringProperty(diskGroup, CRYPTOKEYPROVIDERID_DISK_KEY, "");
	    }

	    if (vmdkInfo.getIoFilter().size() > 0) {
		final StringBuilder ioFilters = new StringBuilder(vmdkInfo.getIoFilter().get(0).toString());
		for (int i = 1; i < vmdkInfo.getIoFilter().size(); i++) {
		    ioFilters.append(",");
		    ioFilters.append(vmdkInfo.getIoFilter().get(i));
		}
		this.valuesMap.setStringProperty(diskGroup, IOFILTER_KEY, ioFilters.toString());
	    }
	    try {
		final List<PbmProfileId> pbmProfilesId = vmm.getAssociatedProfile(vmdkInfo.getKey());

		final List<PbmProfile> pbmProfiles = ((VimConnection) vmm.getConnection()).getPbmConnection()
			.pbmRetrieveContent(pbmProfilesId);
		for (final PbmProfile pbmProf : pbmProfiles) {
		    this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_ID_KEY,
			    pbmProf.getProfileId().getUniqueId());
		    this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_NAME_KEY, pbmProf.getName());

		    logger.info(String.format("VM Profile id:%s  name:%s  description:%s",
			    pbmProf.getProfileId().getUniqueId(), pbmProf.getName(), pbmProf.getDescription()));
		}
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
		this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_ID_KEY, "");
		this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_NAME_KEY, "");

	    }

	    final String diskIndexGroup = generateDiskIndexGroup();
	    this.valuesMap.setStringProperty(diskIndexGroup, vmdkInfo.getUuid(), iStr);

	    final String controllerGroup = generateControllerGroup(vmdkInfo.getControllerKey());
	    this.valuesMap.setStringProperty(controllerGroup, ADAPTERTYPE_CONTROLLER_KEY,
		    vmdkInfo.getAdapterType().toString());
	    this.valuesMap.setStringProperty(controllerGroup, BUSNUMBER_CONTROLLER_KEY,
		    Integer.toString(vmdkInfo.getBusNumber()));
	    diskId++;
	}
	// final String controllerIndexGroup = generateControllerIndexGroup();
	// this.iniConfiguration.setIntegerProperty(controllerIndexGroup,
	// vmdkInfo.getUuid(), vmdkInfo.getControllerKey());
	logger.exiting(getClass().getName(), "initializeDiskGroup");
    }

    public void initializeGeneration(final GenerationProfileSpec spec) {

	logger.entering(getClass().getName(), "initializeGeneration", new Object[] { spec });
	logger.info("initializeGeneration() begin.");

	initializeGenerationGroup(spec);

	switch (spec.getFcoType()) {
	case ivd:
	    initializeIvdDiskGroup(spec);
	    break;
	case k8s:
	    break;
	case vapp:
	    break;
	case vm:
//	    int diskId = 0;
	    initializeDiskGroup(spec);
//	    for (final VmdkInfo vmdkInfo : spec.getVmdkInfoList()) {
//
//		initializeDiskGroup((VirtualMachineManager) spec.getFco(), diskId, vmdkInfo, spec.getBackupInfo());
//		diskId++;
//	    }
	    initializeNetworkGroup((VirtualMachineManager) spec.getFco());
	    break;
	default:
	    break;

	}

	logger.info("initializeGeneration() end.");
	logger.exiting(getClass().getName(), "initializeGeneration");
    }

    private void initializeGenerationGroup(final GenerationProfileSpec spec) {

	final Date timestamp = spec.getCalendar().getTime();
	final long timestampMs = spec.getCalendar().getTimeInMillis();

	this.valuesMap.setIntegerProperty(this.generation, GENERATION_ID_GEN_KEY, spec.getGenId());
	this.valuesMap.setBooleanProperty(this.generation, CHANGE_TRACKING_KEY,
		spec.getFco().isChangedBlockTrackingEnabled());
	this.valuesMap.setFcoTypeProperty(this.generation, ENTITY_TYPE_KEY, spec.getFcoType());
	this.valuesMap.setStringProperty(this.generation, NAME_GEN_KEY, spec.getFco().getName());
	this.valuesMap.setDateProperty(this.generation, TIMESTAMP_GEN_KEY, timestamp);
	this.valuesMap.setLongProperty(this.generation, TIMESTAMP_MS_GEN_KEY, timestampMs);
	this.valuesMap.setIntegerProperty(this.generation, PREV_GENERATION_ID_GEN_KEY, spec.getPrevGenId());
	this.valuesMap.setBackupModeProperty(this.generation, BACKUPMODE_KEY, BackupMode.unknow);
	this.valuesMap.setStringProperty(this.generation, INSTANCE_UUID_KEY, spec.getFco().getUuid());

	this.valuesMap.setStringProperty(this.generation, DATACENTER_NAME_KEY,
		spec.getFco().getDatacenterInfo().getName());

	this.valuesMap.setIntegerProperty(this.generation, NUMBER_OF_DISKS_GEN_KEY, 1);
	this.valuesMap.setIntegerProperty(this.generation, NUM_VMDKDUMP_SUCCEEDED_GEN_KEY, 0);
	this.valuesMap.setIntegerProperty(this.generation, NUM_VMDKDUMP_FAILED_GEN_KEY, 0);
	switch (spec.getFcoType()) {
	case ivd:
	    this.valuesMap.setStringProperty(this.generation, DATASTORE_NAME_KEY,
		    ((ImprovedVirtuaDisk) spec.getFco()).getDatastoreInfo().getName());
	    break;
	case k8s:
	    break;
	case vapp:
	    this.valuesMap.setStringProperty(this.generation, RESOURCE_POOL_PATH_KEY, spec.getResourcePoolFullPath());
	    this.valuesMap.setStringProperty(this.generation, FOLDER_PATH_KEY, spec.getVmFolderFullPath());
	    initializeGenerationGroup((VirtualAppManager) spec.getFco());
	    break;
	case vm:
	    this.valuesMap.setStringProperty(this.generation, RESOURCE_POOL_PATH_KEY, spec.getResourcePoolFullPath());
	    this.valuesMap.setStringProperty(this.generation, FOLDER_PATH_KEY, spec.getVmFolderFullPath());
	    this.valuesMap.setIntegerProperty(this.generation, NUMBER_OF_DISKS_GEN_KEY, spec.getNumberOfDisks());
	    initializeGenerationGroup((VirtualMachineManager) spec.getFco());
	    break;
	default:
	    break;

	}

    }

    private void initializeGenerationGroup(final VirtualAppManager vApp) {
	this.valuesMap.setStringProperty(this.generation, MOREF_GEN_KEY, vApp.getMorefValue());
	if (vApp.isvAppConfigAvailable()) {
	    this.valuesMap.setStringProperty(this.generation, VAPP_CONFIG_KEY, "vapp.config");
	}
	final ResourceAllocationInfo cpuAllocation = vApp.getConfig().getCpuAllocation();
	this.valuesMap.setBooleanProperty(this.generation, CPU_EXPANDABLE_RESERVATION_KEY,
		cpuAllocation.isExpandableReservation());
	this.valuesMap.setLongProperty(this.generation, CPU_LIMIT_KEY, cpuAllocation.getLimit());
	this.valuesMap.setLongProperty(this.generation, CPU_OVERHEAD_LIMIT_KEY, cpuAllocation.getOverheadLimit());
	this.valuesMap.setLongProperty(this.generation, CPU_RESERVATION_KEY, cpuAllocation.getReservation());
	this.valuesMap.setStringProperty(this.generation, CPU_SHARES_LEVEL_KEY,
		cpuAllocation.getShares().getLevel().toString());
	this.valuesMap.setIntegerProperty(this.generation, CPU_SHARES_KEY, cpuAllocation.getShares().getShares());

	final ResourceAllocationInfo memAllocation = vApp.getConfig().getMemoryAllocation();
	this.valuesMap.setBooleanProperty(this.generation, MEM_EXPANDABLE_RESERVATION_KEY,
		memAllocation.isExpandableReservation());
	this.valuesMap.setLongProperty(this.generation, MEM_LIMIT_KEY, memAllocation.getLimit());
	this.valuesMap.setLongProperty(this.generation, MEM_OVERHEAD_LIMIT_KEY, memAllocation.getOverheadLimit());
	this.valuesMap.setLongProperty(this.generation, MEM_RESERVATION_KEY, memAllocation.getReservation());
	this.valuesMap.setStringProperty(this.generation, MEM_SHARES_LEVEL_KEY,
		memAllocation.getShares().getLevel().toString());
	this.valuesMap.setIntegerProperty(this.generation, MEM_SHARES_KEY, memAllocation.getShares().getShares());

	final int size = vApp.getVmList().size();
	this.valuesMap.setIntegerProperty(this.generation, CHILDREN_NUMBER_KEY, vApp.getVmList().size());
	for (Integer i = 0; i < size; i++) {
	    final String sectionName = generateChildGroup(i);
	    this.valuesMap.setIntegerProperty(sectionName, GENERATION_ID_GEN_KEY, -1);
	    this.valuesMap.setStringProperty(sectionName, INSTANCE_UUID_KEY, vApp.getVmList().get(i).getUuid());
	    this.valuesMap.setStringProperty(sectionName, NAME_GEN_KEY, vApp.getVmList().get(i).getName());
	    this.valuesMap.setEntityTypeProperty(sectionName, ENTITY_TYPE_KEY, vApp.getVmList().get(i).getEntityType());
	    this.valuesMap.setStringProperty(sectionName, MOREF_GEN_KEY, vApp.getVmList().get(i).getMorefValue());
	}

    }

    private void initializeGenerationGroup(final VirtualMachineManager vmm) {
	this.valuesMap.setStringProperty(this.generation, DATASTORE_NAME_KEY, vmm.getDatastoreInfo().getName());
	this.valuesMap.setStringProperty(this.generation, VMX_FILENAME_GEN_KEY, "0.vmx");
	this.valuesMap.setStringProperty(this.generation, NVRAM_FILENAME_GEN_KEY, "0.nvram");

	this.valuesMap.setStringProperty(this.generation, MOREF_GEN_KEY, vmm.getMorefValue());
	this.valuesMap.setStringProperty(this.generation, FIRMWARE_KEY, vmm.getFirmware().toString());

	if (vmm.isvAppConfigAvailable()) {
	    this.valuesMap.setStringProperty(this.generation, VAPP_CONFIG_KEY, "vapp.config");
	}
	final CryptoKeyId cryptoKeyId = vmm.getConfig().getVirtualMachineConfigInfo().getKeyId();
	if (cryptoKeyId != null) {
	    final KeyProviderId providerId = cryptoKeyId.getProviderId();
	    final String id = providerId.getId();
	    final String keyId = cryptoKeyId.getKeyId();
	    this.valuesMap.setStringProperty(this.generation, CRYPTOKEY_ID_KEY, id);
	    this.valuesMap.setStringProperty(this.generation, CRYPTOKEY_PROVIDERID_KEY, keyId);
	} else {
	    this.valuesMap.setStringProperty(this.generation, CRYPTOKEY_ID_KEY, "");
	    this.valuesMap.setStringProperty(this.generation, CRYPTOKEY_PROVIDERID_KEY, "");
	}

	this.valuesMap.setStringProperty(this.generation, ENCRYPTION_BUNDLE_KEY, vmm.getEncryptionBundle());

	try {
	    final List<PbmProfileId> pbmProfilesId = vmm.getAssociatedProfile();
	    if (pbmProfilesId.size() > 0) {
		final List<PbmProfile> pbmProfiles = ((VimConnection) vmm.getConnection()).getPbmConnection()
			.pbmRetrieveContent(pbmProfilesId);
		for (final PbmProfile pbmProf : pbmProfiles) {
		    this.valuesMap.setStringProperty(this.generation, PBM_PROFILE_ID_KEY,
			    pbmProf.getProfileId().getUniqueId());
		    this.valuesMap.setStringProperty(this.generation, PBM_PROFILE_NAME_KEY, pbmProf.getName());

		    logger.info(String.format("VM Profile id:%s  name:%s  description:%s",
			    pbmProf.getProfileId().getUniqueId(), pbmProf.getName(), pbmProf.getDescription()));
		}
	    }
	} catch (final Exception e) {

	}
    }

    @Override
    protected void initializeGroups() {
	this.groupMeta = META_GROUP;
	this.generation = GENERATION_GROUP_KEY;

    }

    private void initializeIvdDiskGroup(final GenerationProfileSpec spec) {

	final int diskId = 0;
	final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) spec.getFco();
	final String iStr = Integer.toString(diskId);
	final String diskGroup = generateDiskGroup(diskId);
	final VStorageObjectConfigInfo vmdkInfo = ivd.getConfigInfo();
	this.valuesMap.setStringProperty(diskGroup, UUID_DISK_KEY, ivd.getUuid());
	this.valuesMap.setLongProperty(diskGroup, CAPACITY_DISK_KEY, vmdkInfo.getCapacityInMB() * 1024 * 1024,
		PrettyNumber.MetricPrefix.mega);

	final BaseConfigInfoDiskFileBackingInfo backing = (BaseConfigInfoDiskFileBackingInfo) vmdkInfo.getBacking();
	if ((backing != null) && (backing.getParent() != null)) {
	    this.valuesMap.setStringProperty(diskGroup, REMOTE_PATH_DISK_KEY, backing.getParent().getFilePath());
	}
	this.valuesMap.setStringProperty(diskGroup, BACKUPMODE_KEY, "unknown");

	this.valuesMap.setBooleanProperty(diskGroup, IVD_DISK_KEY, true);
	this.valuesMap.setStringProperty(diskGroup, IVD_IDKEY, ivd.getId().toString());

	this.valuesMap.setLongProperty(diskGroup, RDIFF_TIMESTAMP_MS_DISK_KEY, -1);
	this.valuesMap.setStringProperty(diskGroup, FILENAME_DUMP_DISK_KEY, iStr + ".dump");
	this.valuesMap.setStringProperty(diskGroup, FILENAME_BTR_DISK_KEY, iStr + ".btr");

	this.valuesMap.setStringProperty(diskGroup, REPORT_DISK_KEY, iStr + REPORT_FILE_EXTENSION);

	this.valuesMap.setBooleanProperty(diskGroup, IS_DELETED_PREVIOUS_DUMP_DISK_KEY, false);
	this.valuesMap.setStringProperty(diskGroup, IS_CHANGED_DISK_KEY, "undefined");
	this.valuesMap.setBooleanProperty(diskGroup, COMPRESSION_DISK_KEY, spec.getBackupInfo().isCompression());
//	this.valuesMap.setBooleanProperty(diskGroup, THINPROVISIONED_DISK_KEY,
//		backing.getProvisioningType().equalsIgnoreCase("thin"));
	this.valuesMap.setStringProperty(diskGroup, PROVISIONING_TYPE_KEY, backing.getProvisioningType());
	this.valuesMap.setLongProperty(diskGroup, DUMP_BEGIN_TIMESTAMP_MS_DISK_KEY, -1);
	this.valuesMap.setLongProperty(diskGroup, DUMP_END_TIMESTAMP_MS_DISK_KEY, -1);
	String changeId = spec.getCbt();
	if (StringUtils.isEmpty(changeId)) {
	    changeId = ivd.getChangeId();
	    if (StringUtils.isEmpty(changeId)) {
		changeId = "*";
	    }
	}
	this.valuesMap.setStringProperty(diskGroup, CHANGE_ID_DISK_KEY, changeId);

	if (vmdkInfo.getIofilter().size() > 0) {
	    final StringBuilder ioFilters = new StringBuilder(vmdkInfo.getIofilter().get(0).toString());
	    for (int i = 1; i < vmdkInfo.getIofilter().size(); i++) {
		ioFilters.append(",");
		ioFilters.append(vmdkInfo.getIofilter().get(i));
	    }
	    this.valuesMap.setStringProperty(diskGroup, IOFILTER_KEY, ioFilters.toString());
	}

	try {
	    final List<PbmProfileId> pbmProfilesId = ivd.getAssociatedProfile();

	    final List<PbmProfile> pbmProfiles = ((VimConnection) ivd.getConnection()).getPbmConnection()
		    .pbmRetrieveContent(pbmProfilesId);
	    for (final PbmProfile pbmProf : pbmProfiles) {
		this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_ID_KEY, pbmProf.getProfileId().getUniqueId());
		this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_NAME_KEY, pbmProf.getName());

		logger.info(String.format("IVD Profile id:%s  name:%s  description:%s",
			pbmProf.getProfileId().getUniqueId(), pbmProf.getName(), pbmProf.getDescription()));
	    }

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_ID_KEY, "");
	    this.valuesMap.setStringProperty(diskGroup, PBM_PROFILE_NAME_KEY, "");
	}

	final String diskIndexGroup = generateDiskIndexGroup();
	this.valuesMap.setStringProperty(diskIndexGroup, ivd.getUuid(), iStr);

    }

    private void initializeNetworkGroup(final VirtualMachineManager vmm) {

	final HashMap<String, ManagedObjectReference> networksAvailable = vmm.getVirtualMachineNetworks();
	if (networksAvailable == null) {
	    return;
	}
	Integer networkId = 0;
	// final String networkIndex = generateNetworkIndexGroup();
	for (final String name : networksAvailable.keySet()) {
	    // this.iniConfiguration.setStringProperty(networkIndex, networkId.toString(),
	    // name);
	    final String networkGroup = generateNetworkGroup(networkId);
	    this.valuesMap.setStringProperty(networkGroup, NAME_NETWORK_KEY, name);
	    this.valuesMap.setStringProperty(networkGroup, TYPE_NETWORK_KEY, networksAvailable.get(name).getType());
	    this.valuesMap.setStringProperty(networkGroup, MOREF_NETWORK_KEY, networksAvailable.get(name).getValue());
	    this.valuesMap.setStringProperty(networkGroup, MOREF_NETWORK_KEY, networksAvailable.get(name).getValue());
	    ++networkId;
	}
	this.valuesMap.setIntegerProperty(this.generation, NUMBER_OF_NETWORKS_GEN_KEY, networkId);
    }

// TODO Remove unused code found by UCDetector
//     public Boolean isChanged(final int diskId) {
// 	final Boolean result = this.iniConfiguration.getBooleanProperty(generateDiskGroup(diskId), IS_CHANGED_DISK_KEY);
//
// 	return result;
//     }

    public boolean isChangeTrackingEnabled() {
	return this.valuesMap.getBooleanProperty(this.generation, CHANGE_TRACKING_KEY);
    }

    public boolean isDigestEnabled(final int diskId) {
	return this.valuesMap.getBooleanProperty(generateDiskGroup(diskId), DIGESTENABLED_DISK_KEY);

    }

    public boolean isDiskCompressed(final int diskId) {
	return this.valuesMap.getBooleanProperty(generateDiskGroup(diskId), COMPRESSION_DISK_KEY);

    }

    public boolean isImprovedVirtualDisk(final int diskId) {
	return getImprovedVirtualDiskId(diskId) != null;
    }

    public boolean isIndipendentPersistentVirtualDisk(final int diskId) {
	return "independent_persistent".equals(getDiskMode(diskId));
    }

    public boolean isSucceeded() {
	final Boolean result = this.valuesMap.getBooleanProperty(this.generation, STATUS_GEN_KEY);
	return result;
    }

    public boolean isVmdkdumpSucceeded(final int diskId) {
	final boolean result = getDiskGroupValAsBoolean(diskId, STATUS_DISK_KEY);
	return result;

    }

    public boolean isWriteThrough(final int diskId) {
	return this.valuesMap.getBooleanProperty(generateDiskGroup(diskId), WRITETHROUGH_DISK_KEY);
    }

    /**
     * VAPP children. Set the the success generation for this specific child
     *
     * @param indexchild index
     * @param generation sucessfull generation
     */
    public void setChildSuccessfullGeneration(final int index, final int generation) {
	this.valuesMap.setIntegerProperty(generateChildGroup(index), GENERATION_ID_GEN_KEY, generation);
    }

    public void setDiskBackupMode(final int diskId, final BackupMode mode) {
	logger.fine(String.format("Set Value disk (%d) BackupMode:%s\n", diskId, mode.toString()));
	this.valuesMap.setStringProperty(generateDiskGroup(diskId), BACKUPMODE_KEY, mode.toString());
	if (getBackupMode() != mode) {
	    this.valuesMap.setStringProperty(this.generation, BACKUPMODE_KEY, mode.toString());
	}
    }

    public void setDumpBeginTimestamp(final int diskId) {
	final String diskGroup = generateDiskGroup(diskId);
	final long timestampMs = Calendar.getInstance().getTimeInMillis();
	this.valuesMap.setLongProperty(diskGroup, DUMP_BEGIN_TIMESTAMP_MS_DISK_KEY, timestampMs);
    }

    public void setDumpEndTimestamp(final int diskId) {
	final String diskGroup = generateDiskGroup(diskId);
	final long timestampMs = Calendar.getInstance().getTimeInMillis();
	this.valuesMap.setLongProperty(diskGroup, DUMP_END_TIMESTAMP_MS_DISK_KEY, timestampMs);
    }

    public void setIsChanged(final int diskId, final boolean isChanged) {
	this.valuesMap.setBooleanProperty(generateDiskGroup(diskId), IS_CHANGED_DISK_KEY, isChanged);
    }

    public void setIsSucceeded(final boolean isSucceeded) {
	this.valuesMap.setBooleanProperty(this.generation, STATUS_GEN_KEY, isSucceeded);
    }

    public void setVmdkdumpResult(final int diskId, final boolean isSucceeded) {
	this.valuesMap.setBooleanProperty(generateDiskGroup(diskId), STATUS_DISK_KEY, isSucceeded);
	if (isSucceeded) {
	    incrementNumOfSucceededVmdkdump();
	} else {
	    incrementNumOfFailedVmdkdump();
	}
    }

}
