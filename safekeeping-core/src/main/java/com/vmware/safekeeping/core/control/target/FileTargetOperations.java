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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.IOUtils;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.core.Dedup;
import com.vmware.safekeeping.core.profile.FcoGenerationsCatalog;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

class FileTargetOperations extends AbstractTargetOperationImpl {

    FileTargetOperations(final FileTarget parent, final ManagedFcoEntityInfo entityInfo, final Logger logger) {
        super(parent, entityInfo);
        this.logger = logger;
    }

    @Override
    public boolean closePostDump(final ExBlockInfo block, final TargetBuffer targetBuffer) {
        boolean result = true;
        try {
            final String uuid = getEntityInfo().getUuid();
            final Dedup dedup = new Dedup(uuid, block);
            final String entities = new ObjectMapper().writeValueAsString(dedup);
            new File(block.getKey()).mkdirs();
            IOUtils.writeTextFile(block.getJsonKey(), entities);
            IOUtils.inputStreamToFile(targetBuffer.getInputStream(), block.getDataKey());

            block.setDuplicated(false);

            getMd5DiskList().put(block.getKey(), block.getMd5());

        } catch (final IOException e) {
            result = false;
            Utility.logWarning(this.logger, e);
            block.setReason(getEntityInfo(), e);

        } finally {
            final long postEndTime = System.nanoTime();
            block.setEndTime(postEndTime);
        }
        return result;
    }

    @Override
    public boolean copyObject(final String sourceKey, final String destinationKey) {
        boolean result = true;
        try {
            IOUtils.copyFile(getFullPath(sourceKey), getFullPath(destinationKey));
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            result = false;
        }
        return result;
    }

    @Override
    public boolean createGenerationFolder(final GenerationProfile profile) {
        boolean result = true;
        final Path path = Paths.get(getFullPath(profile.getGenerationPath()));

        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            result = false;
        }
        return result;
    }

    @Override
    public boolean createProfileFolder(final FcoGenerationsCatalog fcoProfile) {
        boolean result = true;
        final Path path = Paths.get(getFullPath(fcoProfile.getUuid()));

        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            result = false;
        }
        return result;
    }

    @Override
    public boolean dedupDump(final ExBlockInfo block) {
        boolean result = true;
        try {
            final String entities = IOUtils.readTextFile(block.getJsonKey());

            final String newEntities = manageDedupEntities(block, entities);
            IOUtils.writeTextFile(block.getJsonKey(), newEntities);

            block.setDuplicated(true);

            getMd5DiskList().put(block.getKey(), block.getMd5());

        } catch (final IOException e) {
            result = false;
            Utility.logWarning(this.logger, e);
            block.setReason(getEntityInfo(), e);

        } finally {
            final long postEndTime = System.nanoTime();
            block.setEndTime(postEndTime);
        }
        return result;
    }

    @Override
    public boolean deleteFolder(final String folderName) {
        boolean result = true;
        try {
            Utility.deleteDirectoryRecursive(new File(folderName), true);
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            result = false;
        }
        return result;
    }

    @Override
    public void deleteObject(final String key) throws IOException {

        Files.deleteIfExists(new File(key).toPath());
    }

    @Override
    public boolean doesKeyExist(final ExBlockInfo block) {
        final File dataKeyFile = new File(block.getDataKey());
        final File jsonKey = new File(block.getJsonKey());
        return jsonKey.exists() && dataKeyFile.exists();
    }

    @Override
    public byte[] getObject(final String key) throws IOException {
        return IOUtils.readBinaryFile(getFullPath(key));
    }

    @Override
    public String getObjectAsString(final String key) throws IOException {
        return IOUtils.readTextFile(getFullPath(key));
    }

    @Override
    public byte[] getObjectMd5(final String path) throws IOException {

        try {
            return IOUtils.getFileChecksum(MessageDigest.getInstance("MD5"), getFullPath(path));
        } catch (NoSuchAlgorithmException | IOException e) {
            Utility.logWarning(this.logger, e);
            return new byte[0];
        }
    }

    @Override
    public FileTarget getParent() {
        return (FileTarget) this.parent;
    }

    @Override
    public ITargetOperation newTargetOperation(final ManagedFcoEntityInfo entityInfo, final Logger logger)
            throws NoSuchAlgorithmException {
        return new FileTargetOperations(getParent(), entityInfo, logger);
    }

    @Override
    public boolean openGetDump(final ExBlockInfo blockInfo, final TargetBuffer targetBuffer) {
        boolean result = true;

        if (this.logger.isLoggable(Level.FINE)) {
            final String msg = String.format("Getting Dump Generation %d from %s", blockInfo.getGenerationId(),
                    blockInfo.getKey());
            this.logger.fine(msg);
        }
        try {
            final FileInputStream is = new FileInputStream(blockInfo.getDataKey());
            final int count = IOUtils.copyToByteArray(is, targetBuffer.getInputBuffer());
            blockInfo.setStreamSize(count);
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            result = false;
            blockInfo.setReason(getEntityInfo(), e);
        }

        return result;
    }

    @Override
    protected boolean post(final GenerationProfile profile, final String path, final ByteArrayInOutStream digestOutput,
            final String contentType) throws IOException {
        boolean result = false;
        byte[] md5Digest = null;
        final String relativePath = getFullPath(path);
        md5Digest = digestOutput.md5Digest();
        final InputStream content = digestOutput.getByteArrayInputStream();
        if (IOUtils.inputStreamToFile(content, relativePath) > 0) {
            if ((profile != null) && path.startsWith(profile.getGenerationPath())) {
                getMd5DiskList().put(path, DatatypeConverter.printHexBinary(md5Digest));
            }
            result = true;
        }
        return result;
    }

    @Override
    public void putObject(final String key, final String content) throws IOException {
        IOUtils.writeTextFile(key, content);

    }

}
