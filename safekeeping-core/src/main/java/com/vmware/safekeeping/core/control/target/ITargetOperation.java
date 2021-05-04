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
package com.vmware.safekeeping.core.control.target;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.core.Dedup;
import com.vmware.safekeeping.core.core.DedupItem;
import com.vmware.safekeeping.core.profile.FcoGenerationsCatalog;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.vapi.internal.util.StringUtils;

public interface ITargetOperation {

	default boolean closeGetDump(final ExBlockInfo dumpFilesInfo, final int bufferIndex) {
		dumpFilesInfo.setEndTime(System.nanoTime());
		return true;
	}

	boolean closePostDump(ExBlockInfo dumpFileInfo, TargetBuffer targetBuffer);

	boolean copyObject(String sourceKey, String destinationKey);

	boolean createGenerationFolder(final GenerationProfile profile);

	boolean createProfileFolder(FcoGenerationsCatalog fcoProfile);

	boolean dedupDump(ExBlockInfo dumpFileInfo);

	Map<String, String> defaultConfigurations();

	boolean deleteFolder(String folderName);

	void deleteObject(String key) throws IOException;

	boolean doesKeyExist(ExBlockInfo dumpFileInfo);

	boolean doesObjectExist(String json);

	String getDisksPath();

	ManagedFcoEntityInfo getEntityInfo();

	byte[] getFcoProfileToByteArray(ManagedFcoEntityInfo fco) throws IOException;

	String getFullPath(final String path);

	byte[] getGenerationProfileToByteArray(ManagedFcoEntityInfo fcoEntity, int genId) throws IOException;

	Map<String, String> getMd5DiskList();

	byte[] getObject(final String key) throws IOException;

	String getObjectAsString(String key) throws IOException;

	byte[] getObjectMd5(final String key) throws IOException;

	ITarget getParent();

	String getTargetName();

	String getUri(String path);

	boolean isMd5FileExist(final GenerationProfile profile);

	boolean isProfileVmExist(ManagedFcoEntityInfo fco);

	boolean isVAppConfigExist(final GenerationProfile profile);

	void loadStatus(String key, boolean clean);

	ITargetOperation newTargetOperation(ManagedFcoEntityInfo entityInfo, Logger logger) throws NoSuchAlgorithmException;

	boolean openGetDump(ExBlockInfo dumpFileInfo, TargetBuffer targetBuffer);

	default void openPostDump(final ExBlockInfo dumpFileInfo) {
		dumpFileInfo.setStartTime(System.nanoTime());

	}

	boolean postGenerationProfile(final GenerationProfile profile);

	boolean postGenerationsCatalog(ManagedFcoEntityInfo fco, ByteArrayInOutStream byteArray) throws IOException;

	boolean postMd5(final GenerationProfile profile) throws NoSuchAlgorithmException, IOException;

	boolean postNvRam(final GenerationProfile profile, final ByteArrayInOutStream byteArrayStream) throws IOException;

	boolean postReport(final GenerationProfile profile, int diskId, final ByteArrayInOutStream byteArrayStream)
			throws IOException;

	boolean postvAppConfig(final GenerationProfile profile, ByteArrayInOutStream byteArrayStream) throws IOException;

	boolean postVmx(final GenerationProfile profile, final ByteArrayInOutStream byteArrayStream) throws IOException;

	void putObject(String key, String content) throws IOException;

	default String removeDedupEntities(final Integer generationId, final String entities)
			throws JsonProcessingException {
		final String uuid = getEntityInfo().getUuid();
		final Dedup dedup = new ObjectMapper().readValue(entities, Dedup.class);
		DedupItem removeItem = null;
		for (final DedupItem d : dedup.getDedupList()) {
			if (d.getUuid().contains(uuid)) {
				d.getGenerations().remove(generationId);
				if (d.getGenerations().isEmpty()) {
					removeItem = d;
				}
				break;
			}
		}
		if (removeItem != null) {
			dedup.getDedupList().remove(removeItem);
		}
		if (dedup.getDedupList().isEmpty()) {
			return StringUtils.EMPTY;
		} else {
			return new ObjectMapper().writeValueAsString(dedup);
		}

	}

	default void removeDump(final ExBlockInfo dumpFileInfo) throws IOException {
		deleteObject(dumpFileInfo.getJsonKey());
		deleteObject(dumpFileInfo.getDataKey());
	}

	boolean removeFcoProfile(ManagedFcoEntityInfo fcoInfo);

	void saveStatus(String key);
}
