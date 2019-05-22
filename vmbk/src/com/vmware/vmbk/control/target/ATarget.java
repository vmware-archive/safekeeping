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
package com.vmware.vmbk.control.target;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.vmware.vmbk.control.info.DumpFileInfo;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.util.Utility;

abstract class ATarget implements ITarget {
    private static final String ACTIVE_KEY = "active";
    protected static String MIME_BINARY_OCTECT_STREAM = "binary/octet-stream";
    private static String MIME_TEXT_PLAIN_STREAM = "text/plain";

    protected Logger logger;
    protected boolean compress;
    protected String configGroup;
    protected int diskId;
    protected DumpFileInfo[] dumpFilesInfo;

    protected FirstClassObject fco;

    private TreeMap<String, String> md5DiskList;
    protected GenerationProfile profGen;
    final private HashMap<String, Object[]> store;

    ATarget() {

	this.diskId = -1;
	this.store = new HashMap<>();
    }

    @Override
    public void close() {
	this.logger.entering(getClass().getName(), "close");
	this.logger.fine("endDisk disk" + this.diskId);
	this.diskId = -1;
	this.dumpFilesInfo = null;
	disposeBuffers();
	this.logger.exiting(getClass().getName(), "close");
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
	this.logger.entering(getClass().getName(), "defaultConfigurations");
	final LinkedHashMap<String, String> result = new LinkedHashMap<>();
	result.put(ACTIVE_KEY, "false");
	this.logger.exiting(getClass().getName(), "defaultConfigurations", result);
	return result;
    }

    abstract public boolean deleteFolder(final String folderName);

    protected abstract void disposeBuffers();

    @Override
    public void finalize() {
	this.logger.info("Default finalize");
    }

    protected abstract byte[] get(final String path);

    @Override
    public byte[] getBlockTracks() {
	this.logger.entering(getClass().getName(), "getBlockTracks");
	final String contentName = this.profGen.getDigestContentPath(this.diskId);
	LoggerUtils.logInfo(this.logger, "Get btr:%s from %s", contentName, getTargetName());
	byte[] result = get(contentName);
	if (result == null) {
	    result = new byte[0];
	}
	this.logger.exiting(getClass().getName(), "getBlockTracks", result);
	return result;
    }

    @Override
    public int getDiskId() {
	return this.diskId;
    }

    @Override
    public DumpFileInfo[] getDumpFileInfo() {
	return this.dumpFilesInfo;
    }

    @Override
    public DumpFileInfo getDumpFileInfo(final int index) {
	return this.dumpFilesInfo[index];
    }

    @Override
    public byte[] getFcoProfileToByteArray() {
	this.logger.entering(getClass().getName(), "getProfileFco");
	final String contentName = GlobalConfiguration.getDefaultProfileVmPath(this.fco.getUuid());
	LoggerUtils.logInfo(this.logger, "Get profileVm:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getProfileFco", result);
	return result;
    }

    @Override
    public FirstClassObject getFcoTarget() {
	return this.fco;
    }

    @Override
    public GenerationProfile getGenerationProfile() {
	return this.profGen;
    }

    @Override
    public byte[] getGenerationProfileToByteArray() {
	this.logger.entering(getClass().getName(), "getGenerationProfile");
	final String contentName = getGenerationProfile().getGenerationProfileContentPath();
	LoggerUtils.logInfo(this.logger, "Get active generation generationProfile:%s from %s", contentName,
		getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getGenerationProfile", result);
	return result;

    }

    @Override
    public byte[] getGenerationProfileToByteArray(final FcoProfile fcoProfile, final int genId) {
	this.logger.entering(getClass().getName(), "getGenerationProfile", new Object[] { fcoProfile, genId });
	final String instanceFolder = fcoProfile.getInstanceUuid();
	final String contentName = String.format("%s/%d/%s", instanceFolder, genId,
		GlobalConfiguration._GENERATION_PROFILE_FILENAME);
	LoggerUtils.logInfo(this.logger, "Get instanceFolder:%sgeneration:%d generationProfile:%s from %s",
		instanceFolder, genId, contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getGenerationProfile", result);
	return result;
    }

    @Override
    public byte[] getGenerationProfileToByteArray(final int genId) {
	this.logger.entering(getClass().getName(), "getGenerationProfile", genId);
	final String contentName = getGenerationProfile().getGenerationProfileContentPath(genId);
	LoggerUtils.logInfo(this.logger, "Get generation:%d generationProfile:%s from %s", genId, contentName,
		getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getGenerationProfile", result);
	return result;
    }

    @Override
    public byte[] getGlobalProfileToByteArray() {
	this.logger.entering(getClass().getName(), "getProfAllFco");
	final String contentName = GlobalConfiguration.getGlobalProfileFileName();
	LoggerUtils.logInfo(this.logger, "Get vmbk.profile:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getProfAllFco", result);
	return result;
    }

    @Override
    public String getGroup() {
	return this.configGroup;
    }

    @Override
    public byte[] getMd5() {
	this.logger.entering(getClass().getName(), "getMd5");
	final String contentName = getGenerationProfile().getMd5ContentPath();
	LoggerUtils.logInfo(this.logger, "Get md5:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getMd5", result);
	return result;
    }

    @Override
    public TreeMap<String, String> getMd5DiskList() {
	return this.md5DiskList;
    }

    private ByteArrayInOutStream getMd5FileContent() {
	this.logger.entering(getClass().getName(), "getMd5FileContent");
	final StringBuilder stBuilder = new StringBuilder();
	ByteArrayInOutStream result = null;
	if (this.md5DiskList != null) {
	    for (final String key : this.md5DiskList.keySet()) {
		stBuilder.append(String.format("%s\t*%s\n", this.md5DiskList.get(key).toString(), key));
	    }
	    result = new ByteArrayInOutStream();
	    try {
		result.write(stBuilder.toString());
	    } catch (final IOException e) {
		this.logger.warning(Utility.toString(e));
		result = null;
	    }
	}
	this.logger.exiting(getClass().getName(), "getMd5FileContent", result);
	return result;
    }

    @Override
    public byte[] getNvRamToByteArray() {
	this.logger.entering(getClass().getName(), "getNvRam");
	final String contentName = getGenerationProfile().getNvramContentPath();
	LoggerUtils.logInfo(this.logger, "Get nvram:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getNvRam", result);
	return result;
    }

    @Override
    public byte[] getPreviousGenerationProfileToByteArray() {
	this.logger.entering(getClass().getName(), "getPreviousGenerationProfile");
	final String contentName = getGenerationProfile().getPreviousGenerationProfileContentPath();
	LoggerUtils.logInfo(this.logger, "Get previous generationProfile:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getPreviousGenerationProfile", result);
	return result;
    }

    @Override
    public String getTargetName() {
	return this.configGroup;
    }

    @Override
    public byte[] getvAppConfigToByteArray() {
	this.logger.entering(getClass().getName(), "getvAppConfig");
	final String contentName = getGenerationProfile().getvAppConfigPath();
	LoggerUtils.logInfo(this.logger, "Get vAppConfig:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getvAppConfig", result);
	return result;
    }

    @Override
    public byte[] getVmxToByteArray() {
	this.logger.entering(getClass().getName(), "getVmx");
	final String contentName = getGenerationProfile().getVmxContentPath();
	LoggerUtils.logInfo(this.logger, "Get vmx:%s from %s", contentName, getTargetName());
	final byte[] result = get(contentName);
	this.logger.exiting(getClass().getName(), "getVmx", result);
	return result;
    }

    @Override
    public boolean initialize() {
	return initializemM5List();

    }

    boolean initializeBuffersArray(final int size) {
	this.dumpFilesInfo = new DumpFileInfo[size];
	return (this.dumpFilesInfo != null);
    }

    @Override
    public boolean initializemM5List() {
	this.md5DiskList = new TreeMap<>();
	return this.md5DiskList != null;
    }

    @Override
    public GlobalProfile initializeProfileAllFco() {
	this.logger.entering(getClass().getName(), "initializeProfileAllVm");
	GlobalProfile result = null;
	try {
	    if (isProfAllVmExist()) {
		final byte[] globProfile = getGlobalProfileToByteArray();
		result = new GlobalProfile(globProfile);
	    } else {

		result = new GlobalProfile();
	    }
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	}
	this.logger.exiting(getClass().getName(), "initializeProfileAllVm", result);
	return result;
    }

    @Override
    public boolean isActive() {
	this.logger.entering(getClass().getName(), "isActive");
	final boolean result = GlobalConfiguration.getTargetCustomValueAsBool(this.configGroup, ACTIVE_KEY);
	this.logger.exiting(getClass().getName(), "isActive", result);
	return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadStatus(final String key, final boolean clean) {
	this.logger.entering(getClass().getName(), "loadStatus", new Object[] { key, clean });
	final Object[] obj = this.store.get(key);
	this.profGen = (GenerationProfile) obj[0];
	this.fco = (FirstClassObject) obj[1];
	this.md5DiskList = (TreeMap<String, String>) obj[2];
	if (clean) {
	    this.store.remove(key);
	}
	this.logger.exiting(getClass().getName(), "loadStatus");

    }

    @Override
    public LinkedHashMap<String, String> manualConfiguration() {
	this.logger.entering(getClass().getName(), "manualConfiguration");
	final LinkedHashMap<String, String> result = new LinkedHashMap<>();
	result.put(ACTIVE_KEY, "Activate the Plugin [%s]");
	this.logger.exiting(getClass().getName(), "manualConfiguration", result);
	return result;
    }

    @Override
    public boolean open(final boolean compress) {
	this.logger.entering(getClass().getName(), "open", new Object[] { this.diskId, compress });
	final boolean result = open(0, compress);
	this.logger.exiting(getClass().getName(), "open", result);
	return result;
    }

    @Override
    public boolean open(final int diskId, final boolean compress) {
	this.logger.entering(getClass().getName(), "open", new Object[] { diskId, compress });
	boolean result = false;
	if (this.diskId != -1) {
	    this.logger.warning(String.format("diskId %d not close", this.diskId));

	} else {
	    this.diskId = diskId;
	    this.compress = compress;
	    result = true;
	}
	this.logger.exiting(getClass().getName(), "open", result);
	return result;
    }

    protected abstract boolean post(final String path, final ByteArrayInOutStream digestOutput,
	    final String contentType);

    @Override
    public boolean postBlockTracks(final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postBlockTracks", byteArrayStream);
	final String contentName = getGenerationProfile().getDigestContentPath(this.diskId);
	LoggerUtils.logInfo(this.logger, "Post btr:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_BINARY_OCTECT_STREAM);
	this.logger.exiting(getClass().getName(), "postBlockTracks", result);
	return result;
    }

    @Override
    public boolean postGenerationProfile() {
	this.logger.entering(getClass().getName(), "postGenerationProfile");
	LoggerUtils.logInfo(this.logger, "Post generationProfile:%s to %s",
		getGenerationProfile().getGenerationProfileContentPath(), getTargetName());
	boolean result = false;
	ByteArrayInOutStream byteArrayStream;
	try {
	    byteArrayStream = getGenerationProfile().toByteArrayInOutputStream();
	    result = post(getGenerationProfile().getGenerationProfileContentPath(), byteArrayStream,
		    MIME_TEXT_PLAIN_STREAM);
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	}
	this.logger.exiting(getClass().getName(), "postGenerationProfile", result);
	return result;

    }

    @Override
    public boolean postMd5() {
	this.logger.entering(getClass().getName(), "postMd5");
	final String contentName = getGenerationProfile().getMd5ContentPath();
	LoggerUtils.logInfo(this.logger, "Post md5:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, getMd5FileContent(), MIME_TEXT_PLAIN_STREAM);
	this.logger.exiting(getClass().getName(), "postMd5", result);
	return result;
    }

    @Override
    public boolean postNvRam(final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postNvRam", byteArrayStream);
	final String contentName = getGenerationProfile().getNvramContentPath();
	LoggerUtils.logInfo(this.logger, "Post nvram:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_BINARY_OCTECT_STREAM);
	this.logger.exiting(getClass().getName(), "postNvRam", result);
	return result;
    }

    @Override
    public boolean postProfAllFco(final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postProfAllFco", byteArrayStream);
	final String contentName = GlobalConfiguration.getGlobalProfileFileName();
	LoggerUtils.logInfo(this.logger, "Post vmbk.profile:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
	this.logger.exiting(getClass().getName(), "postProfAllFco", result);
	return result;
    }

    @Override
    public boolean postProfileFco(final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postProfileFco", byteArrayStream);
	final String contentName = GlobalConfiguration.getDefaultProfileVmPath(this.fco.getUuid());
	LoggerUtils.logInfo(this.logger, "Post profileVm:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
	this.logger.exiting(getClass().getName(), "postProfileFco", result);
	return result;

    }

    @Override
    public boolean postReport(final int diskId, final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postReport", byteArrayStream);
	final String contentName = getGenerationProfile().getReportContentPath(diskId);
	LoggerUtils.logInfo(this.logger, "Post report:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
	this.logger.exiting(getClass().getName(), "postReport", result);
	return result;
    }

    @Override
    public boolean postvAppConfig(final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postvAppConfig", byteArrayStream);
	final String contentName = getGenerationProfile().getvAppConfigPath();
	LoggerUtils.logInfo(this.logger, "Post vAppConfig:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_BINARY_OCTECT_STREAM);
	this.logger.exiting(getClass().getName(), "postvAppConfig", result);
	return result;
    }

    @Override
    public boolean postVmx(final ByteArrayInOutStream byteArrayStream) {
	this.logger.entering(getClass().getName(), "postVmx", byteArrayStream);
	final String contentName = getGenerationProfile().getVmxContentPath();
	LoggerUtils.logInfo(this.logger, "Post vmx:%s to %s", contentName, getTargetName());
	final boolean result = post(contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
	this.logger.exiting(getClass().getName(), "postVmx", result);
	return result;
    }

    @Override
    public boolean removeFcoProfile(final ManagedFcoEntityInfo fcoInfo) {
	this.logger.entering(getClass().getName(), "removeFcoProfile", fcoInfo);
	final boolean result = deleteFolder(fcoInfo.getUuid());
	this.logger.exiting(getClass().getName(), "removeFcoProfile", result);
	return result;
    }

    @Override
    public boolean removeGeneration(final int generationId) {
	this.logger.entering(getClass().getName(), "removeGeneration", generationId);
	final boolean result = deleteFolder(getGenerationProfile().getDumpPath(generationId));
	this.logger.exiting(getClass().getName(), "removeGeneration", result);
	return result;
    }

    @Override
    public void saveStatus(final String key) {
	this.logger.entering(getClass().getName(), "saveStatus", key);
	final TreeMap<String, String> prevMd5DiskList = new TreeMap<>();
	prevMd5DiskList.putAll(this.md5DiskList);

	this.store.put(key, new Object[] { this.profGen, this.fco, prevMd5DiskList });
	this.logger.exiting(getClass().getName(), "saveStatus");
    }

    @Override
    public void setFcoTarget(final FirstClassObject vmm) {
	this.fco = vmm;
    }

    @Override
    public void setProfGen(final GenerationProfile profGen) {
	this.profGen = profGen;
    }

}
