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
package com.vmware.vmbk.control;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GenerationProfileSpec;
import com.vmware.vmbk.type.AdapterType;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbk.type.PrettyNumber.MetricPrefix;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualControllerManager;
import com.vmware.vmbk.type.VirtualDiskManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.VmdkInfo;
import com.vmware.vmbk.util.Utility;

public class FcoArchiveManager {
    public enum ArchiveManagerMode {
	readOnly, temporary, write
    }

    private static final Logger logger = Logger.getLogger(FcoArchiveManager.class.getName());

// TODO Remove unused code found by UCDetector
//     static boolean isEmptyArchive(final ConfigGlobal cfgGlobal, final ManagedEntityInfo vmInfo) {
// 	return true;
//     }

// TODO Remove unused code found by UCDetector
//     static boolean isExistSucceededGeneration(final ConfigGlobal cfgGlobal, final ManagedEntityInfo vmInfo) {
//
// 	return true;
//     }

    private GenerationProfile currGen;

    private final EntityType entityType;

    private FcoProfile profFco;

    private final ITarget repositoryTarget;

    public FcoArchiveManager(final FirstClassObject fco, final ITarget target, final ArchiveManagerMode mode)
	    throws Exception {

	this.entityType = fco.getEntityType();

	this.currGen = null;
	this.profFco = null;
	this.repositoryTarget = target;
	if (target.getFcoTarget() == null) {
	    target.setFcoTarget(fco);
	    IoFunction.print(
		    "*********************************************************\nWhy FCO is not definedit's empty\n*********************************************************\n");
	    throw new Exception("Why it's empty");
	}
	VirtualMachineManager vmm;
	ImprovedVirtuaDisk ivd;
	VirtualAppManager vApp;
	switch (this.entityType) {
	case VirtualApp:
	    vApp = (VirtualAppManager) fco;
	    switch (mode) {

	    case readOnly:
		this.profFco = initializeProfile(vApp);
		break;
	    case temporary:
		this.profFco = new FcoProfile();
		this.profFco.initializeMetadata(vApp.getFcoInfo());
		this.repositoryTarget.createProfileVmFolder(this.profFco);
		break;
	    case write:
		this.profFco = initializeProfile(vApp);
		postProfileFco();
		break;

	    }
	    break;
	case VirtualMachine:
	    vmm = (VirtualMachineManager) fco;
	    switch (mode) {

	    case readOnly:
		this.profFco = initializeProfile(vmm);
		break;
	    case temporary:
		this.profFco = new FcoProfile();
		this.profFco.initializeMetadata(vmm.getFcoInfo());

		break;
	    case write:
		this.profFco = initializeProfile(vmm);
		postProfileFco();
		break;

	    }
	    break;
	case ImprovedVirtualDisk:
	    ivd = (ImprovedVirtuaDisk) fco;

	    switch (mode) {

	    case readOnly:
		this.profFco = initializeProfile(ivd);
		break;
	    case temporary:
		this.profFco = new FcoProfile();
		this.profFco.initializeMetadata(ivd.getFcoInfo());
		// this.profFco.initializeMetadata(ivd.getConfigInfo(), ivd.getUuid(),
		// ivd.getVcenterInstanceUuid());
		this.repositoryTarget.createProfileVmFolder(this.profFco);
		break;
	    case write:
		this.profFco = initializeProfile(ivd);
		if (this.profFco == null) {
		    throw new Exception("ivd:" + ivd.toString() + " corrupted");
		}
		postProfileFco();
		break;

	    }
	    break;
	default:
	    throw new Exception("Type :" + this.entityType.toString() + " not supprorted");

	}

    }

    private boolean canExecIncrBackup(final GenerationProfile prevGen, final String uuid) {

	final int diskId = this.currGen.getDiskIdWithUuid(uuid);
	logger.info("diskId: " + diskId);
	if (diskId < 0) {
	    return false;
	}
	final int prevDiskId = prevGen.getDiskIdWithUuid(uuid);
	if (prevDiskId < 0) {
	    logger.warning("prevDiskId < 0");
	    return false;
	}
	if (this.currGen.isImprovedVirtualDisk(diskId)
		&& (this.currGen.getEntityType() != EntityType.ImprovedVirtualDisk)) {

	} else if (prevGen.isVmdkdumpSucceeded(prevDiskId) == false) {
	    logger.warning("Previous generation did not succeed.");
	    return false;
	}

	final long capacity = this.currGen.getCapacity(diskId);
	final long prevCapacity = prevGen.getCapacity(prevDiskId);
	if ((capacity < 0) || (capacity != prevCapacity)) {
	    logger.info("capacity is invalid or different.");
	    return false;
	}

	if ((this.repositoryTarget.isMd5FileExist(prevGen.getGenerationId()) == false)) {
	    logger.info("No MD5 file available ");
	    return false;
	}
	/*
	 * Check that changed block information of both previous and current generation
	 * are available
	 */
	final String currChangeId = this.currGen.getChangeId(diskId);
	if ((currChangeId == null) || currChangeId.equals("*")) {
	    logger.info("currChangeId is null or \"*\"");
	    return false;
	}
	final String prevChangeId = prevGen.getChangeId(prevDiskId);
	logger.info("prevChangeId: " + prevChangeId);
	if ((prevChangeId == null) || prevChangeId.equals("*")) {
	    logger.info("prevChangeId is null or \"*\"");
	    return false;
	}

	return true;
    }

    public BackupMode determineBackupMode(final BackupMode requestMode) {
	logger.entering(getClass().getName(), "determineBackupMode", requestMode);
	final BackupMode result = determineBackupMode(0, requestMode);
	logger.exiting(getClass().getName(), "determineBackupMode", result);
	return result;
    }

    public BackupMode determineBackupMode(final int diskId, final BackupMode requestMode) {
	logger.entering(getClass().getName(), "determineBackupMode", new Object[] { diskId, requestMode });
	BackupMode mode = BackupMode.unknow;
	if (!this.currGen.isChangeTrackingEnabled()) {
	    IoFunction.showInfo(logger, "Change Tracking is disabled. Backup Mode set to Full");
	    return BackupMode.full;
	}
	final String uuid = this.currGen.getUuid(diskId);
	final GenerationProfile prevGen = this.getPrevGeneration();
	if (prevGen == null) {
	    logger.warning("prevGen is null.");
	    return BackupMode.full;
	}
	boolean isInc = false;
	isInc = canExecIncrBackup(prevGen, uuid);

	if (requestMode != BackupMode.unknow) {
	    if ((isInc && (requestMode == BackupMode.incr)) || (requestMode == BackupMode.full)) {
		mode = requestMode;
	    }
	}

	if (mode == BackupMode.unknow) {
	    if (isInc) {
		mode = BackupMode.incr;
	    } else {
		mode = BackupMode.full;
	    }
	}
	assert mode != BackupMode.unknow;
	LoggerUtils.logInfo(logger, "Available modes: full%s. Used mode: %s.", (isInc ? ", incr" : ""),
		mode.toString());

	if (prevGen.getChangeId(diskId) != null) {
	    this.currGen.setIsChanged(diskId, true);
	}
	logger.exiting(getClass().getName(), "determineBackupMode", mode);
	return mode;
    }

    public boolean finalizeBackup(final boolean isSucceeded) throws Exception {
	this.currGen.setIsSucceeded(isSucceeded);
	this.profFco.setGenerationInfo(this.currGen, this.currGen.getBackupMode());
	return isSucceeded;
    }

     /**
     * Create a list of disk with associated controller to be re-create
     * 
     * @param profile
     * @param datastoreName
     * @return
     */
    public List<VirtualControllerManager> generateVirtualControllerManagerList(final GenerationProfile profile,
	    final String datastoreName) {
	logger.entering(getClass().getName(), "generateVirtualControllerManagerList",
		new Object[] { profile, datastoreName });

	final LinkedHashMap<Integer, VirtualControllerManager> vcmMap = new LinkedHashMap<>();

	final List<Integer> diskIdList = profile.getDiskIdList();
	for (final Integer diskIdI : diskIdList) {

	    final int diskId = diskIdI.intValue();
	    final AdapterType type = profile.getAdapterType(diskId);
	    assert type != AdapterType.UNKNOWN;
	    final Integer ckey = profile.getControllerDeviceKey(diskId);
	    final int busNumber = profile.getBusNumber(ckey);
	    final int unitNumber = profile.getUnitNumber(diskId);

	    if (profile.isIndipendentPersistentVirtualDisk(diskId)) {
		LoggerUtils.logInfo(logger, "Controller:%d Disk [%d:%d] is an Indipendant or Persistent disk ", ckey,
			busNumber, unitNumber);
		continue;
	    }
	    if (profile.isImprovedVirtualDisk(diskId)) {
		LoggerUtils.logInfo(logger, "Controller:%d Disk [%d:%d] is an Improved Virtual Disk uuid:%s ", ckey,
			busNumber, unitNumber, profile.getImprovedVirtualDiskId(diskId));
		LoggerUtils.logInfo(logger,
			"To restore this IVD use:\n restore ivd:%s (take note of the uuid) and execute ivd -attach -device %d:%d ivd:<newid>",
			profile.getImprovedVirtualDiskId(diskId), ckey, unitNumber);
		continue;
	    }

	    final VirtualDiskManager vdm = new VirtualDiskManager(profile, diskId, datastoreName);

	    if (!vcmMap.containsKey(ckey)) {
		final VirtualControllerManager vcm = new VirtualControllerManager(type, ckey, busNumber);
		vcmMap.put(ckey, vcm);
	    }

	    vcmMap.get(ckey).add(vdm);
	}

	final List<VirtualControllerManager> result = new LinkedList<>();
	result.addAll(vcmMap.values());

	logger.exiting(getClass().getName(), "generateVirtualControllerManagerList", result);
	return result;
    }

    public int getDependingGenerationId(final int id) {
	if (this.profFco == null) {
	    return -2;
	}
	final int genId = this.profFco.getDependingGenerationId(id);

	return genId;
    }

    public LinkedList<Integer> getDependingGenerationList() {
	logger.entering(getClass().getName(), "getDependingGenerationList");
	final LinkedList<Integer> result = this.profFco
		.getDependingGenerationList(getTargetGeneration().getGenerationId());
	logger.exiting(getClass().getName(), "getDependingGenerationList", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public LinkedList<Integer> getDependingGenerationList(final GenerationProfile profGen) {
// 	logger.entering(getClass().getName(), "getDependingGenerationList", profGen);
// 	final LinkedList<Integer> result = this.profFco.getDependingGenerationList(profGen.getGenerationId());
// 	logger.exiting(getClass().getName(), "getDependingGenerationList", result);
// 	return result;
//     }

    public int getDependingOnGenerationId(final int genId) {
	logger.entering(getClass().getName(), "getDependingOnGenerationId", genId);
	int result = -2;
	if (this.profFco != null) {
	    final int nextGen = this.profFco.getNextSucceededGenerationId(genId);
	    if (nextGen > 0) {
		final int dependingGeneration = this.profFco.getDependingGenerationId(nextGen);
		if (dependingGeneration == genId) {
		    result = nextGen;
		}
	    }
	}
	logger.exiting(getClass().getName(), "getDependingOnGenerationId", result);
	return result;

    }

    public EntityType getEntityType() {
	return this.entityType;
    }

    public List<Integer> getFailedGenerationList() {
	return this.profFco.getFailedGenerationList();
    }

    public FcoProfile getFcoProfile() {
	return this.profFco;
    }

    public List<Integer> getGenerationIdList() {
	if (this.profFco == null) {
	    return new LinkedList<>();
	}
	return this.profFco.getGenerationIdList();

    }

// TODO Remove unused code found by UCDetector
//     public ArrayList<Integer> getGenerationListDependingOnGeneration(int genId) {
// 	logger.entering(getClass().getName(), "getGenerationListDependingOnGeneration", genId);
// 	if (this.profFco == null) {
// 	    return null;
// 	}
// 	final ArrayList<Integer> result = new ArrayList<>();
// 	result.add(genId);
// 	while (true) {
// 	    final int i = this.profFco.getNextSucceededGenerationId(genId);
// 	    if (i == -2) {
// 		break;
// 	    }
// 	    final int depGeneration = this.profFco.getDependingGenerationId(i);
// 	    if (depGeneration == genId) {
// 		result.add(i);
// 		genId = i;
// 	    } else {
// 		break;
// 	    }
// 	}
// 	logger.exiting(getClass().getName(), "getGenerationListDependingOnGeneration", result);
// 	return result;
//
//     }

    public String getGenerationStatusString(final int genId) {
	final StringBuffer sb = new StringBuffer();
	sb.append(String.format("%d \t%s\t", genId, this.profFco.getTimestampStr(genId)));

	if (this.profFco.isGenerationSucceeded(genId)) {
	    try {
		final GenerationProfile profGen = loadProfileGeneration(genId);

		final List<Integer> diskIdList = profGen.getDiskIdList();

		for (final Integer diskId : diskIdList) {
		    final long millis = profGen.getDumpElapsedTimeMs(diskId);
		    final String timeElapse = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis)
				    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		    switch (profGen.getEntityType()) {

		    case VirtualMachine:
			sb.append(String.format("%d %s%s:%s %-7.2f   %s  %s", diskId,
				profGen.getAdapterType(diskId).toTypeString(),
				profGen.getBusNumber(profGen.getControllerDeviceKey(diskId)),
				profGen.getUnitNumber(diskId),
				(float) (profGen.getCapacity(diskId) / (1024 * 1024 * 1024)),
				profGen.getDiskBackupMode(diskId).toString(), timeElapse));
			break;
		    case ImprovedVirtualDisk:
			sb.append(String.format("%d         %-7.2f   %s  %s", diskId,

				(float) (profGen.getCapacity(diskId) / (1024 * 1024 * 1024)),
				profGen.getDiskBackupMode(diskId).toString(), timeElapse));
			break;
		    default:
			break;

		    }

		}

	    } catch (final Exception e) {
		logger.warning(String.format("failed with generation %d.", genId));
		return String.format("%d ##########_ERROR_##########");
	    }
	} else {
	    sb.append(" ----------_FAILED_----------");
	}
	return sb.toString();
    }

    public String getInstanceUuid() {
	return this.profFco.getInstanceUuid();
    }

    public int getLatestGenerationId() {
	return this.profFco.getLatestGenerationId();
    }

    public int getLatestSucceededGenerationId() {
	return this.profFco.getLatestSucceededGenerationId();
    }

    public String getMoref() {
	if (this.profFco == null) {
	    return null;
	}
	return this.profFco.getMoref();
    }

    public String getName() {
	if (this.profFco == null) {
	    return null;
	}
	return this.profFco.getName();
    }

    public int getNumOfGeneration() {
	return this.profFco.getNumOfGeneration();
    }

    public String getOriginalFolderPath() {
	if (this.currGen == null) {
	    return null;
	}
	return this.currGen.getOriginalFolderPath();
    }

    public String getOriginalResourcePoolPath() {
	if (this.currGen == null) {
	    return null;
	}
	return this.currGen.getOriginalResourcePoolPath();
    }

    public String getPrevChangeId(final int diskId) {

	final GenerationProfile prevGen = getPrevGeneration();
	if (prevGen == null) {
	    return null;
	}

	final int prevDiskId = getPrevDiskId(diskId);
	if (prevDiskId < 0) {
	    return null;
	}

	final String prevChangeId = prevGen.getChangeId(prevDiskId);

	if ((prevChangeId != null) && (prevChangeId.equals("*") == false)) {
	    return prevChangeId;
	} else {
	    return null;
	}
    }

    private int getPrevDiskId(final int diskId) {
	final GenerationProfile currGen = this.currGen;

	final String uuid = currGen.getUuid(diskId);

	final GenerationProfile prevGen = getPrevGeneration();
	if (prevGen == null) {
	    return -1;
	}

	final int prevDiskId = prevGen.getDiskIdWithUuid(uuid);
	if (prevDiskId < 0) {
	    return -1;
	}

	return prevDiskId;
    }

    private GenerationProfile getPrevGeneration() {
	final int currGenId = this.currGen.getGenerationId();
	return getPrevGeneration(currGenId);
    }

    private GenerationProfile getPrevGeneration(final int genId) {
	if (genId < 0) {
	    return null;
	}
	final int prevGenId = this.profFco.getPrevSucceededGenerationId(genId);
	logger.fine("prevGenId: " + prevGenId);
	if (prevGenId < 0) {
	    return null;
	}
	assert this.profFco.isGenerationSucceeded(prevGenId);

	GenerationProfile ret = null;
	try {
	    ret = loadProfileGeneration(prevGenId);
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	return ret;
    }

    public ITarget getRepositoryTarget() {
	return this.repositoryTarget;
    }

    public int getTargetDiskId(final VmdkInfo vmdkInfo) {
	assert this.currGen != null;
	final GenerationProfile profGen = this.currGen;

	final List<Integer> diskIdList = profGen.getDiskIdList();
	for (final Integer diskIdI : diskIdList) {
	    final int diskId = diskIdI.intValue();

	    /*
	     * Comparing key is enough, however, The following parameters should be the
	     * same.
	     */
	    final int ckey = profGen.getControllerDeviceKey(diskId);
	    if ((vmdkInfo.getKey() == profGen.getDiskDeviceKey(diskId))
		    && ((vmdkInfo.getCapacity()) == profGen.getCapacity(diskId))
		    && (vmdkInfo.getUnitNumber() == profGen.getUnitNumber(diskId))
		    && (vmdkInfo.getControllerKey() == ckey) && (vmdkInfo.getBusNumber() == profGen.getBusNumber(ckey))
		    && (vmdkInfo.getAdapterType() == profGen.getAdapterType(diskId))) {

		return diskId;
	    }
	}

	return -1;
    }

    public GenerationProfile getTargetGeneration() {
	return this.currGen;
    }

    public long getTimestampMsOfLatestGeneration() {
	if (this.profFco == null) {
	    return -1L;
	}
	final int genId = this.profFco.getLatestGenerationId();

	if (genId < 0) {
	    return -1L;
	} else {
	    final String val = this.profFco.getTimestampMs(genId);
	    if (StringUtils.isEmpty(val)) {
		return -1L;
	    }
	    return Long.valueOf(val).longValue();
	}
    }

    public long getTimestampMsOfLatestSucceededGenerationId() {
	if (this.profFco == null) {
	    return -1L;
	}
	final int genId = this.profFco.getLatestSucceededGenerationId();

	if (genId < 0) {
	    return -1L;
	} else {
	    return Long.valueOf(this.profFco.getTimestampMs(genId)).longValue();
	}
    }

    public String getTimestampOfLatestGeneration() {
	if (this.profFco == null) {
	    return null;
	}
	final int genId = this.profFco.getLatestGenerationId();

	if (genId < 0) {
	    return null;
	} else {
	    return this.profFco.getTimestampStr(genId);
	}
    }

    public String getTimestampOfLatestSucceededGenerationId() {
	if (this.profFco == null) {
	    return null;
	}
	final int genId = this.profFco.getLatestSucceededGenerationId();

	if (genId < 0) {
	    return null;
	} else {
	    return this.profFco.getTimestampStr(genId);
	}
    }

    private FcoProfile initializeProfile(final ImprovedVirtuaDisk ivd) throws Exception {

	assert ivd != null;

	FcoProfile profVm = null;

	if (this.repositoryTarget.isProfileVmExist()) {
	    profVm = new FcoProfile(this.repositoryTarget.getFcoProfileToByteArray());

	    if (!ivd.getUuid().equalsIgnoreCase(profVm.getInstanceUuid())) {
		IoFunction.showWarning(logger, "Profile uuid != ivd uuid  %s != %s .\n", profVm.getInstanceUuid(),
			ivd.getUuid());
		throw new Exception();
	    }
	    if ((profVm.getName() == null) || profVm.getName().isEmpty()) {
		profVm.setName(ivd.getName());
	    }
	    profVm.setVcenterInstanceUuid(ivd.getVcenterInstanceUuid());

	} else {
	    if (ivd.getConfigInfo() != null) {
		profVm = new FcoProfile();
		profVm.initializeMetadata(ivd.getConfigInfo(), ivd.getUuid(), ivd.getVcenterInstanceUuid());
		this.repositoryTarget.createProfileVmFolder(profVm);
	    } else {
		IoFunction.showWarning(logger, "%s is corrupted", ivd.toString());
	    }
	}

	return profVm;
    }

    private FcoProfile initializeProfile(final VirtualAppManager vmm) throws Exception {

	FcoProfile profFco;
	if (this.repositoryTarget.isProfileVmExist()) {

	    profFco = new FcoProfile(this.repositoryTarget.getFcoProfileToByteArray());

	    if (!vmm.getUuid().equalsIgnoreCase(profFco.getInstanceUuid())) {
		IoFunction.showWarning(logger, "Profile uuid != ivd uuid  %s != %s .\n", profFco.getInstanceUuid(),
			vmm.getUuid());
		throw new Exception();
	    }

	    if ((profFco.getInstanceUuid() == null) || profFco.getInstanceUuid().isEmpty()) {

		// if (vmm.isInstanceUuidAvailable()) {
		profFco.setInstanceUuid(vmm.getUuid());
		// }
	    }
	    if ((profFco.getName() == null) || profFco.getName().isEmpty()) {

		profFco.setName(vmm.getName());
	    }

	    // if (vmm.isVcenterInstanceUuidAvailable()) {
	    profFco.setVcenterInstanceUuid(vmm.getVcenterInstanceUuid());
	    // }

	} else {

	    profFco = new FcoProfile();
	    profFco.initializeMetadata(vmm.getFcoInfo());
	    this.repositoryTarget.createProfileVmFolder(profFco);
	}

	return profFco;
    }

    private FcoProfile initializeProfile(final VirtualMachineManager vmm) throws Exception {

	FcoProfile profFco;
	if (this.repositoryTarget.isProfileVmExist()) {

	    profFco = new FcoProfile(this.repositoryTarget.getFcoProfileToByteArray());

	    if (!vmm.getUuid().equalsIgnoreCase(profFco.getInstanceUuid())) {
		IoFunction.showWarning(logger, "Profile uuid != ivd uuid  %s != %s .\n", profFco.getInstanceUuid(),
			vmm.getUuid());
		throw new Exception();
	    }

	    if ((profFco.getInstanceUuid() == null) || profFco.getInstanceUuid().isEmpty()) {

		if (vmm.isInstanceUuidAvailable()) {
		    profFco.setInstanceUuid(vmm.getUuid());
		}
	    }
	    if ((profFco.getName() == null) || profFco.getName().isEmpty()) {

		profFco.setName(vmm.getName());
	    }

	    if (vmm.isVcenterInstanceUuidAvailable()) {
		profFco.setVcenterInstanceUuid(vmm.getVcenterInstanceUuid());
	    }

	} else {

	    profFco = new FcoProfile();
	    profFco.initializeMetadata(vmm.getFcoInfo());
	    this.repositoryTarget.createProfileVmFolder(profFco);
	}

	return profFco;
    }

    public GenerationProfile loadProfileGeneration(int genId) {

	final FcoProfile profVm = this.profFco;
	GenerationProfile profGen = null;
	if (genId < 0) {
	    genId = profVm.getLatestSucceededGenerationId();
	    if (genId < 0) {
		logger.warning(String.format("No generation has genId <0  %d.", genId));

		return null;
	    }
	}
	if (profVm.isGenerationSucceeded(genId) == false) {
	    logger.warning(String.format("status of the generation  %d is not succeeded.", genId));
	    return null;
	}
	final byte[] byteArrayStream = this.repositoryTarget.getGenerationProfileToByteArray(this.profFco, genId);
	if (byteArrayStream != null) {
	    try {
		profGen = new GenerationProfile(byteArrayStream);
	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
	    }
	}
	return profGen;
    }

    public boolean postProfileFco() {

	assert this.profFco != null;
	try {
	    return this.repositoryTarget.postProfileFco(this.profFco.toByteArrayInOutputStream());
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}

    }

    /**
     * Prepare a new archive generation for a FCD
     *
     * @param ivd
     * @param calendar
     * @param backupIvdInfo
     * @param cbt           if set force the disk cbt value
     * @return
     * @throws Exception
     */
    public GenerationProfile prepareNewGeneration(final GenerationProfileSpec spec) throws Exception {
	final FcoProfile profFco = this.profFco;
	/*
	 * Create new generation id.
	 */
	spec.setPrevGenId(profFco.getLatestSucceededGenerationId());
	final String timestampMs = PrettyNumber.toString(spec.getCalendar().getTimeInMillis(), MetricPrefix.none);
	spec.setGenId(profFco.createNewGenerationId(timestampMs));

	IoFunction.showInfo(logger, spec.getLocationString());
	/*
	 * Make new ProfileGeneration and initialize it.
	 */
	final GenerationProfile profGen = new GenerationProfile();
	profGen.initializeGeneration(spec);

	return profGen;
    }

    public GenerationProfile setTargetGeneration(final GenerationProfile profGen) {

	if (profGen != null) {
	    this.currGen = profGen;
	}
	return this.currGen;
    }

    public GenerationProfile setTargetGeneration(final int genId) {
	this.currGen = loadProfileGeneration(genId);
	return this.currGen;
    }

    public String toStringVirtualControllerManagerList(final List<VirtualControllerManager> vcmList) {
	final StringBuffer sb = new StringBuffer();
	for (final VirtualControllerManager vcm : vcmList) {
	    sb.append(vcm.toString());
	}
	return sb.toString();
    }

}
