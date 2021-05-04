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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.core.Dedup;
import com.vmware.safekeeping.core.core.DedupItem;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

abstract class AbstractTargetOperationImpl implements ITargetOperation {

    private static final String ACTIVE_KEY = "active";
    protected static final String MIME_BINARY_OCTECT_STREAM = "binary/octet-stream";
    protected static final String MIME_TEXT_PLAIN_STREAM = "text/plain";
    protected Logger logger;
    protected final ITarget parent;
    private Map<String, String> md5DiskList;

    private final HashMap<String, Object[]> store;
    private final ManagedFcoEntityInfo entityInfo;

    AbstractTargetOperationImpl(final ITarget parent, final ManagedFcoEntityInfo entityInfo) {
        this.store = new HashMap<>();
        this.parent = parent;
        this.md5DiskList = Collections.synchronizedMap(new LinkedHashMap<String, String>());
        this.entityInfo = entityInfo;
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put(ACTIVE_KEY, "false");
        return result;
    }

    @Override
    public boolean doesObjectExist(final String key) {
        return this.parent.doesObjectExist(key);
    }

    @Override
    public String getDisksPath() {
        return this.parent.getFullPath(CoreGlobalSettings.REPOSITORY_DATA_PATH);
    }

    @Override
    public ManagedFcoEntityInfo getEntityInfo() {
        return this.entityInfo;
    }

    @Override
    public byte[] getFcoProfileToByteArray(final ManagedFcoEntityInfo fco) throws IOException {
        final String contentName = CoreGlobalSettings.getDefaultProfileVmPath(fco.getUuid());
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Get profileVm:%s from %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return getObject(contentName);
    }

    @Override
    public String getFullPath(final String path) {
        return this.parent.getFullPath(path);
    }

    @Override
    public byte[] getGenerationProfileToByteArray(final ManagedFcoEntityInfo fcoEntity, final int genId)
            throws IOException {
        final String instanceFolder = fcoEntity.getUuid();
        final String contentName = String.format("%s/%d/%s", instanceFolder, genId,
                CoreGlobalSettings.GENERATION_PROFILE_FILENAME);
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Get instanceFolder:%sgeneration:%d generationProfile:%s from %s",
                    instanceFolder, genId, contentName, getTargetName());
            this.logger.info(msg);
        }
        return getObject(contentName);
    }

    @Override
    public Map<String, String> getMd5DiskList() {
        return this.md5DiskList;
    }

    private ByteArrayInOutStream getMd5FileContent() throws NoSuchAlgorithmException {
        final StringBuilder stBuilder = new StringBuilder();
        ByteArrayInOutStream result = null;
        if (this.md5DiskList != null) {
            for (final Entry<String, String> entry : this.md5DiskList.entrySet()) {
                stBuilder.append(String.format("%s\t*%s%n", entry.getValue(), entry.getKey()));
            }
            result = new ByteArrayInOutStream();
            try {
                result.write(stBuilder.toString());
            } catch (final IOException e) {
                Utility.logWarning(this.logger, e);
                result = null;
            }
        }
        return result;
    }

    @Override
    public String getTargetName() {
        return this.parent.getTargetType();
    }

    @Override
    public String getUri(final String path) {
        return this.parent.getUri(path);
    }

    @Override
    public boolean isMd5FileExist(final GenerationProfile profile) {
        return doesObjectExist(profile.getMd5ContentPath());
    }

    @Override
    public boolean isProfileVmExist(final ManagedFcoEntityInfo fco) {
        return doesObjectExist(CoreGlobalSettings.getDefaultProfileVmPath(fco.getUuid()));
    }

    @Override
    public boolean isVAppConfigExist(final GenerationProfile profile) {
        return doesObjectExist(profile.getvAppConfigPath());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadStatus(final String key, final boolean clean) {
        final Object[] obj = this.store.get(key);
        this.md5DiskList = (TreeMap<String, String>) obj[1];
        if (clean) {
            this.store.remove(key);
        }

    }

    protected String manageDedupEntities(final ExBlockInfo block, final String entities)
            throws JsonProcessingException {
        final String uuid = getEntityInfo().getUuid();
        final Dedup dedup = new ObjectMapper().readValue(entities, Dedup.class);
        final Integer generationId = block.getGenerationId();
        if ((block.getMd5() == null) || !block.getMd5().equalsIgnoreCase(dedup.getMd5())) {
            block.setMd5(dedup.getMd5());
        }
        // XOR operation
        if (((block.isCipher() && !dedup.isCipher()) || (!block.isCipher() && dedup.isCipher()))) {
            block.setCipher(dedup.isCipher());
        }
        // XOR operation
        if (((block.isCompress() && !dedup.isCompress()) || (!block.isCompress() && dedup.isCompress()))) {
            block.setCompress(dedup.isCompress());
        }
        if (block.getSize() != dedup.getSize()) {
            block.setSize(dedup.getSize());
        }
        if (block.getStreamSize() != dedup.getStreamSize()) {
            block.setStreamSize(dedup.getStreamSize());
        }
        boolean found = false;
        for (final DedupItem d : dedup.getDedupList()) {
            if (d.getUuid().contains(uuid)) {
                d.getGenerations().add(generationId);
                found = true;
                break;
            }
        }

        if (!found) {
            dedup.getDedupList().add(new DedupItem(uuid, block));
        }

        return new ObjectMapper().writeValueAsString(dedup);

    }

    protected abstract boolean post(GenerationProfile profile, final String path,
            final ByteArrayInOutStream digestOutput, final String contentType) throws IOException;

    private final boolean post(final String path, final ByteArrayInOutStream digestOutput, final String contentType)
            throws IOException {
        return post(null, path, digestOutput, contentType);
    }

    @Override
    public boolean postGenerationProfile(final GenerationProfile profile) {
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post generationProfile:%s to %s",
                    profile.getGenerationProfileContentPath(), getTargetName());
            this.logger.info(msg);
        }
        boolean result = false;
        ByteArrayInOutStream byteArrayStream;
        try {
            byteArrayStream = profile.toByteArrayInOutputStream();
            result = post(profile, profile.getGenerationProfileContentPath(), byteArrayStream, MIME_TEXT_PLAIN_STREAM);
        } catch (final IOException | NoSuchAlgorithmException e) {
            Utility.logWarning(this.logger, e);
        }
        return result;

    }

    @Override
    public boolean postGenerationsCatalog(final ManagedFcoEntityInfo fco, final ByteArrayInOutStream byteArrayStream)
            throws IOException {
        final String contentName = CoreGlobalSettings.getDefaultProfileVmPath(fco.getUuid());
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post postGenerationsCatalog:%s to %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return post(contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
    }

    @Override
    public boolean postMd5(final GenerationProfile profile) throws NoSuchAlgorithmException, IOException {
        final String contentName = profile.getMd5ContentPath();
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post md5:%s to %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return post(profile, contentName, getMd5FileContent(), MIME_TEXT_PLAIN_STREAM);
    }

    @Override
    public boolean postNvRam(final GenerationProfile profile, final ByteArrayInOutStream byteArrayStream)
            throws IOException {
        final String contentName = profile.getNvramContentPath();
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post nvram:%s to %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return post(profile, contentName, byteArrayStream, MIME_BINARY_OCTECT_STREAM);
    }

    @Override
    public boolean postReport(final GenerationProfile profile, final int diskId,
            final ByteArrayInOutStream byteArrayStream) throws IOException {
        final String contentName = profile.getReportContentPath(diskId);
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post report:%s to %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return post(profile, contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
    }

    @Override
    public boolean postvAppConfig(final GenerationProfile profile, final ByteArrayInOutStream byteArrayStream)
            throws IOException {
        final String contentName = profile.getvAppConfigPath();
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post vAppConfig:%s to %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return post(profile, contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
    }

    @Override
    public boolean postVmx(final GenerationProfile profile, final ByteArrayInOutStream byteArrayStream)
            throws IOException {
        final String contentName = profile.getVmxContentPath();
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post vmx:%s to %s", contentName, getTargetName());
            this.logger.info(msg);
        }
        return post(profile, contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);
    }

    @Override
    public boolean removeFcoProfile(final ManagedFcoEntityInfo fcoInfo) {
        return deleteFolder(fcoInfo.getUuid());
    }

    @Override
    public void saveStatus(final String key) {
        final TreeMap<String, String> prevMd5DiskList = new TreeMap<>();
        prevMd5DiskList.putAll(this.md5DiskList);

        this.store.put(key, new Object[] { prevMd5DiskList });
    }

}
