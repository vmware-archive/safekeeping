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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyBoolean.BooleanType;

public class FcoProfile extends Profile {
    private static final String GEN_GROUP_BACKUP_MODE = "backupMode";

    private static final String GEN_GROUP_DEPEND_GENERATION_ID = "dependingOnGenerationId";
    private static final String GEN_GROUP_STATUS = "succeeded";
    private static final String GEN_GROUP_TIME_STAMP = "timestamp";
    private static final String GEN_GROUP_TIME_STAMP_MS = "timestampMs";
    private static final String INDEX_GROUP = "index";
    private static final String GENERATION_GROUP = "generation";

    private static final Logger logger = Logger.getLogger(FcoProfile.class.getName());

    private static final String META_GROUP_INSTANCE_UUID_KEY = "instanceUuid";
    private static final String META_GROUP_LATEST_KEY = "latest";
    private static final String META_GROUP_MOREF_KEY = "moref";
    private static final String META_GROUP_NAME_KEY = "name";

    private static final String META_GROUP_TYPE_KEY = "type";

    private static final String META_GROUP_VCENTER_INSTANCE_UUID_KEY = "vCenterInstanceUuid";

    private static final String TIMESTAMP_MS_GENERATION_GROUP = "timestampMs-generation";

    private String grpTsGen;

    public FcoProfile() {
	super();
    }

    public FcoProfile(final byte[] byteArray) throws IOException {
	super(byteArray);
    }

    public int createNewGenerationId(final String timestampMs) throws Exception {
	final int depGenId = getLatestSucceededGenerationId();

	final int currGenId = getLatestGenerationId();
	final int newGenId = (currGenId < -1) ? 0 : currGenId + 1;
	setLatestGenerationId(newGenId);
	setIsGenerationSucceeded(newGenId, false, BackupMode.unknow);
	setTimestampMs(newGenId, timestampMs);
	setDependingGenerationId(newGenId, depGenId);

	return newGenId;
    }

    public void delGenerationInfo(final int genId) {

	final String timestampMs = getTimestampMs(genId);
	if (timestampMs != null) {
	    this.valuesMap.removeProperty(this.grpTsGen, timestampMs);
	}

	final String genGroup = makeGenerationGroup(genId);
	this.valuesMap.removeSection(genGroup);

	if ((getLatestGenerationId() == genId) && (genId > -1)) {
	    for (int i = genId - 1; i > -1; i--) {
		if (isGenerationExist(i)) {
		    setLatestGenerationId(i);
		    return;
		}
	    }
	    setLatestGenerationId(-2);
	}
    }

// TODO Remove unused code found by UCDetector
//     public BackupMode getBackupMode(final int genId) {
// 	logger.entering(getClass().getName(), "getBackupMode");
// 	final String genGroup = makeGenerationGroup(genId);
// 	final BackupMode result = BackupMode
// 		.parse(this.iniConfiguration.getStringProperty(genGroup, GEN_GROUP_BACKUP_MODE));
// 	logger.fine(String.format("Get Value Generation %d backupMode:%s\n", genId, result.toString()));
// 	logger.exiting(getClass().getName(), "getBackupMode", result);
// 	return result;
//     }

    public int getDependingGenerationId(final int genId) {
	logger.entering(getClass().getName(), "getDependingGenerationId");
	final String genGroup = makeGenerationGroup(genId);
	final Integer result = this.valuesMap.getIntegerProperty(genGroup, GEN_GROUP_DEPEND_GENERATION_ID, -1);
	logger.exiting(getClass().getName(), "getDependingGenerationId", result);
	return result;
    }

    public LinkedList<Integer> getDependingGenerationList(final int genId) {
	logger.entering(getClass().getName(), "getDependingGenerationList", genId);
	final LinkedList<Integer> result = new LinkedList<>();
	final String genGroup = makeGenerationGroup(genId);
	final Integer dependsOn = this.valuesMap.getIntegerProperty(genGroup, GEN_GROUP_DEPEND_GENERATION_ID, -1);
	if (dependsOn >= 0) {
	    result.addAll(getDependingGenerationList(dependsOn));
	}
	result.add(genId);
	logger.exiting(getClass().getName(), "getDependingGenerationList", result);
	return result;
    }

    public EntityType getEntityType() {
	logger.entering(getClass().getName(), "getEntityType");
	final String type = this.valuesMap.getStringProperty(this.groupMeta, META_GROUP_TYPE_KEY);
	EntityType result;
	if (type.equals("ivd")) {
	    result = EntityType.ImprovedVirtualDisk;
	} else if (type.equals("vm")) {
	    result = EntityType.VirtualMachine;
	} else if (type.equals("vapp")) {
	    result = EntityType.VirtualApp;
	} else {
	    result = EntityType.valueOf(type);
	}
	logger.exiting(getClass().getName(), "getEntityType", result);
	return result;
    }

    public List<Integer> getFailedGenerationList() {
	logger.entering(getClass().getName(), "getFailedGenerationList");
	final List<Integer> list = getGenerationIdList();
	final List<Integer> result = new LinkedList<>();

	final int latestId = getLatestGenerationId();
	if (latestId < 0) {
	    logger.warning("LatestId < 0.");

	} else {

	    for (final Integer genIdI : list) {
		assert genIdI != null;
		final int genId = genIdI.intValue();

		if (!isGenerationSucceeded(genId) && (genId != latestId)) {

		    result.add(genIdI);
		}
	    }
	}
	logger.exiting(getClass().getName(), "getFailedGenerationList", result);
	return result;
    }

    public List<Integer> getGenerationIdList() {
	final List<Integer> ret = new LinkedList<>();
	final Map<Long, Integer> map = getGenerationMap();

	for (final Integer genId : map.values()) {

	    if (genId == null) {
		continue;
	    }
	    ret.add(genId);
	}
	return ret;
    }

    private Map<Long, Integer> getGenerationMap() {
	return getGenerationMap(null);
    }

    private Map<Long, Integer> getGenerationMap(final Comparator<Long> cmp) {

	logger.entering(getClass().getName(), "getGenerationMap");

	final HashMap<String, String> entryList = this.valuesMap.getProperties(this.grpTsGen);

	Map<Long, Integer> result;
	if (cmp != null) {
	    result = new TreeMap<>(cmp);
	} else {
	    result = new TreeMap<>();
	}

	for (final String key : entryList.keySet()) {

	    final long ts = Long.parseLong(key);
	    final int id = Integer.parseInt(entryList.get(key));

	    result.put(new Long(ts), new Integer(id));
	}

	logger.exiting(getClass().getName(), "getGenerationMap", result);
	return result;
    }

    public String getInstanceUuid() {
	return this.valuesMap.getStringProperty(this.groupMeta, META_GROUP_INSTANCE_UUID_KEY);
    }

    public int getLatestGenerationId() {
	logger.entering(getClass().getName(), "getLatestGenerationId");
	final Integer result = this.valuesMap.getIntegerProperty(this.groupMeta, META_GROUP_LATEST_KEY, -2);
	logger.exiting(getClass().getName(), "getLatestGenerationId", result);
	return result;
    }

    public int getLatestSucceededGenerationId() {
	logger.entering(getClass().getName(), "getLatestSucceededGenerationId");

	int result = getLatestGenerationId();
	if (result < 0) {
	    result = -2;
	}
	if (!isGenerationSucceeded(result)) {
	    final int prevId = getPrevSucceededGenerationId(result);
	    if (prevId >= 0) {
		result = prevId;
	    } else {
		result = -2;
	    }
	}
	logger.exiting(getClass().getName(), "getLatestSucceededGenerationId", result);
	return result;
    }

    public String getMoref() {
	logger.entering(getClass().getName(), "getMoref");

	final String result = this.valuesMap.getStringProperty(this.groupMeta, META_GROUP_MOREF_KEY);
	logger.exiting(getClass().getName(), "getMoref", result);
	return result;
    }

    public String getName() {
	logger.entering(getClass().getName(), "getName");
	final String result = this.valuesMap.getStringProperty(this.groupMeta, META_GROUP_NAME_KEY);
	logger.exiting(getClass().getName(), "getName", result);
	return result;
    }

    public int getNextSucceededGenerationId(final int genId) {
	logger.entering(getClass().getName(), "getNextSucceededGenerationId", genId);
	final Map<Long, Integer> genMap = this.getGenerationMap();
	final ArrayList<Integer> genList = new ArrayList<>(genMap.values());
	int result = -2;
	final Integer genIdI = new Integer(genId);

	final int curIdx = genList.indexOf(genIdI);
	if (curIdx < 0) {
	    result = -2;
	} else {
	    for (int idx = curIdx + 1; idx < genList.size(); idx++) {
		final Integer tmpGenIdI = genList.get(idx);
		final int tmpGenId = tmpGenIdI.intValue();

		if (isGenerationSucceeded(tmpGenId)) {
		    result = tmpGenId;
		    break;
		}
	    }
	}

	logger.exiting(getClass().getName(), "getNextSucceededGenerationId", result);
	return result;
    }

    public int getNumOfGeneration() {
	final Map<Long, Integer> genMap = getGenerationMap();
	return genMap.size();
    }

    private int getNumOfSuccceededGeneration() {
	final Map<Long, Integer> genMap = getGenerationMap();
	final ArrayList<Integer> genList = new ArrayList<>(genMap.values());

	int count = 0;
	for (final Integer i : genList) {
	    assert i != null;

	    if (isGenerationSucceeded(i)) {
		count++;
	    }
	}
	return count;
    }

    public int getPrevSucceededGenerationId(final int genId) {
	final Map<Long, Integer> genMap = this.getGenerationMap();
	final ArrayList<Integer> genList = new ArrayList<>(genMap.values());

	final Integer genIdI = new Integer(genId);

	final int curIdx = genList.indexOf(genIdI);
	if (curIdx < 0) {
	    return -2;
	}

	for (int idx = curIdx - 1; idx >= 0; idx--) {
	    final Integer tmpGenIdI = genList.get(idx);
	    final int tmpGenId = tmpGenIdI.intValue();

	    if (isGenerationSucceeded(tmpGenId)) {
		return tmpGenId;
	    }
	}
	return -2;
    }

    public String getStatusString(final boolean isAvailable) {
	final StringBuffer sb = new StringBuffer();

	if (isAvailable) {
	    sb.append(String.format("%s\t", getName()));
	} else {
	    sb.append(String.format("[(%s)][%s][%s]", getInstanceUuid(), getMoref(), getName()));
	}

	final int genId = getLatestSucceededGenerationId();
	if (genId >= 0) {

	    final int numGen = getNumOfGeneration();
	    final int numSucceededGen = getNumOfSuccceededGeneration();
	    sb.append(String.format("Generation(s):%d/%d\n", numSucceededGen, numGen));
	}
	if (isAvailable) {
	    switch (getEntityType()) {
	    case VirtualMachine:
		sb.append(String.format("   uuid:%s\tmoRef:%s", getInstanceUuid(), getMoref()));
		break;
	    case VirtualApp:
		sb.append(String.format("   uuid:-\t\t\t\tmoRef:%s", getMoref()));
		break;
	    case ImprovedVirtualDisk:
		sb.append(String.format("   uuid:%s\tmoRef:-", getInstanceUuid()));
		break;
	    default:
		break;
	    }

	}
	return sb.toString();
    }

    public String getTimestampMs(final int genId) {
	logger.entering(getClass().getName(), "getTimestampMs", genId);
	final String genGroup = makeGenerationGroup(genId);
	final String result = this.valuesMap.getStringProperty(genGroup, GEN_GROUP_TIME_STAMP_MS);
	logger.exiting(getClass().getName(), "getTimestampMs", result);
	return result;
    }

    public String getTimestampStr(final int genId) {
	logger.entering(getClass().getName(), "getTimestampStr", genId);
	final String genGroup = makeGenerationGroup(genId);
	final String result = this.valuesMap.getStringProperty(genGroup, GEN_GROUP_TIME_STAMP);
	logger.exiting(getClass().getName(), "getTimestampStr", result);
	return result;
    }

    public String getVcenterInstanceUuid() {
	logger.entering(getClass().getName(), "getVcenterInstanceUuid");
	final String result = this.valuesMap.getStringProperty(this.groupMeta, META_GROUP_VCENTER_INSTANCE_UUID_KEY);
	logger.exiting(getClass().getName(), "getVcenterInstanceUuid", result);
	return result;
    }

    @Override
    protected void initializeGroups() {

	this.groupMeta = META_GROUP;
	this.grpTsGen = this.valuesMap.createSectionName(INDEX_GROUP, TIMESTAMP_MS_GENERATION_GROUP);

    }

    public void initializeMetadata(final ManagedFcoEntityInfo vmInfo) {
	logger.entering(getClass().getName(), "initializeMetadata", vmInfo);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_MOREF_KEY, vmInfo.getMorefValue());
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_NAME_KEY, vmInfo.getName());

	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_INSTANCE_UUID_KEY, vmInfo.getUuid());
	this.valuesMap.setIntegerProperty(this.groupMeta, META_GROUP_LATEST_KEY, -1);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_VCENTER_INSTANCE_UUID_KEY, vmInfo.getServerUuid());
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_TYPE_KEY, vmInfo.getEntityType().toString());
	logger.exiting(getClass().getName(), "initializeMetadata");

    }

    public void initializeMetadata(final VStorageObjectConfigInfo configInfo, final String uuid,
	    final String vCenterInstanceUuid) {
	logger.entering(getClass().getName(), "initializeMetadata",
		new Object[] { configInfo, uuid, vCenterInstanceUuid });
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_MOREF_KEY, uuid.substring(24));
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_NAME_KEY, configInfo.getName());

	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_INSTANCE_UUID_KEY, uuid);
	this.valuesMap.setIntegerProperty(this.groupMeta, META_GROUP_LATEST_KEY, -1);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_VCENTER_INSTANCE_UUID_KEY, vCenterInstanceUuid);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_TYPE_KEY,
		EntityType.ImprovedVirtualDisk.toString());
	logger.exiting(getClass().getName(), "initializeMetadata");

    }

    public boolean isGenerationExist(final int genId) {
	logger.entering(getClass().getName(), "isGenerationExist", genId);
	final String genGroup = makeGenerationGroup(genId);
	final boolean result = this.valuesMap.doesPropertyExist(genGroup, GEN_GROUP_STATUS);
	logger.exiting(getClass().getName(), "isGenerationExist", result);
	return result;

    }

    public boolean isGenerationSucceeded(final int genId) {
	logger.entering(getClass().getName(), "isGenerationSucceeded", genId);
	boolean result = false;
	if (genId >= 0) {
	    final String genGroup = makeGenerationGroup(genId);
	    result = this.valuesMap.getBooleanProperty(genGroup, GEN_GROUP_STATUS);
	}

	logger.exiting(getClass().getName(), "isGenerationSucceeded", result);
	return result;
    }

    private String makeGenerationGroup(final int genId) {
	if (genId < 0) {
	    logger.warning("generation id < 0.");
	    return null;
	}
	return this.valuesMap.createSectionName(GENERATION_GROUP, genId);
    }

    private void setDependingGenerationId(final int genId, final int depGenId) {
	logger.entering(getClass().getName(), "setDependingGenerationId", new Object[] { genId, depGenId });
	final String genGroup = makeGenerationGroup(genId);
	this.valuesMap.setIntegerProperty(genGroup, GEN_GROUP_DEPEND_GENERATION_ID, depGenId);
	logger.exiting(getClass().getName(), "setDependingGenerationId");
    }

    public void setGenerationInfo(final GenerationProfile profGen, final BackupMode mode) {

	logger.entering(getClass().getName(), "setGenerationInfo", new Object[] { profGen, mode });
	final int genId = profGen.getGenerationId();
	final String timestampMs = profGen.getTimestampMs();
	setIsGenerationSucceeded(genId, profGen.isSucceeded(), mode);
	setTimestampMs(genId, timestampMs);
	logger.exiting(getClass().getName(), "setGenerationInfo");
    }

    public void setGenerationNotDependent(final int genId) {
	logger.entering(getClass().getName(), "setGenerationNotDependent", genId);
	setDependingGenerationId(genId, -2);
	logger.exiting(getClass().getName(), "setGenerationNotDependent");
    }

    public void setInstanceUuid(final String uuid) {
	logger.entering(getClass().getName(), "setInstanceUuid", uuid);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_INSTANCE_UUID_KEY, uuid);
	logger.exiting(getClass().getName(), "setInstanceUuid");
    }

    private void setIsGenerationSucceeded(final int genId, final boolean isSucceeded, final BackupMode mode) {
	logger.entering(getClass().getName(), "setIsGenerationSucceeded", new Object[] { genId, isSucceeded, mode });
	final String genGroup = makeGenerationGroup(genId);
	this.valuesMap.setBooleanProperty(genGroup, GEN_GROUP_STATUS, isSucceeded);

	logger.fine(String.format("Set Value Generation  %d status :%s\n", genId,
		PrettyBoolean.toString(isSucceeded, BooleanType.yesNo)));
	this.valuesMap.setStringProperty(genGroup, GEN_GROUP_BACKUP_MODE, mode.toString());
	logger.fine(String.format("Set Value Generation %d backupMode:%s\n", genId, mode.toString()));
	logger.exiting(getClass().getName(), "setIsGenerationSucceeded");
    }

    private void setLatestGenerationId(final int newGenId) {
	logger.entering(getClass().getName(), "setLatestGenerationId", newGenId);
	this.valuesMap.setIntegerProperty(this.groupMeta, META_GROUP_LATEST_KEY, newGenId);
	logger.exiting(getClass().getName(), "setLatestGenerationId");
    }

    public void setName(final String name) {
	logger.entering(getClass().getName(), "setName", name);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_NAME_KEY, name);
	logger.exiting(getClass().getName(), "setName");
    }

    private void setTimestampMs(final int genId, final String timestampMs) {
	logger.entering(getClass().getName(), "setTimestampMs", new Object[] { genId, timestampMs });
	final String oldTimestampStr = getTimestampMs(genId);
	if (oldTimestampStr != null) {
	    logger.info("delete from [index \"" + GEN_GROUP_TIME_STAMP_MS + "\"] " + oldTimestampStr);
	    this.valuesMap.removeProperty(this.grpTsGen, oldTimestampStr);
	}

	final Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(Long.parseLong(timestampMs));

	final String genGroup = makeGenerationGroup(genId);
	this.valuesMap.setDateProperty(genGroup, GEN_GROUP_TIME_STAMP, cal.getTime());
	this.valuesMap.setStringProperty(genGroup, GEN_GROUP_TIME_STAMP_MS, timestampMs);
	this.valuesMap.setIntegerProperty(this.grpTsGen, timestampMs, genId);
	logger.exiting(getClass().getName(), "setTimestampMs");
    }

    public void setVcenterInstanceUuid(final String uuid) {
	logger.entering(getClass().getName(), "setVcenterInstanceUuid", uuid);
	this.valuesMap.setStringProperty(this.groupMeta, META_GROUP_VCENTER_INSTANCE_UUID_KEY, uuid);
	logger.exiting(getClass().getName(), "setVcenterInstanceUuid");
    }

}
