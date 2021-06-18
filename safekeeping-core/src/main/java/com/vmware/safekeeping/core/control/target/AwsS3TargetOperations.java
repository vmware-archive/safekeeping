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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.AmazonClientException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.control.TargetBuffer;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.core.Dedup;
import com.vmware.safekeeping.core.profile.FcoGenerationsCatalog;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

class AwsS3TargetOperations extends AbstractTargetOperationImpl {

    private final AmazonS3 s3;

    private final String backetName;

    AwsS3TargetOperations(final AwsS3Target parent, final ManagedFcoEntityInfo entityInfo, final Logger logger) {
        super(parent, entityInfo);
        this.backetName = parent.getOptions().getBacket();
        this.s3 = parent.getS3();
        this.logger = logger;
    }

    @Override
    public boolean closePostDump(final ExBlockInfo block, final TargetBuffer targetBuffer) {
        boolean result = true;
        try {
            final String uuid = getEntityInfo().getUuid();
            final Dedup dedup = new Dedup(uuid, block);
            final String entities = new ObjectMapper().writeValueAsString(dedup);
            final ObjectMetadata s3ObjectMetadata = new ObjectMetadata();
            s3ObjectMetadata.setContentMD5(block.getMd5EncodeBase64());
            s3ObjectMetadata.setContentLength(block.getStreamSize());
            s3ObjectMetadata.setContentType(MIME_BINARY_OCTECT_STREAM);

            this.s3.putObject(getBacketname(), block.getJsonKey(), entities);
            final PutObjectRequest putObjectRequest = new PutObjectRequest(getBacketname(), block.getDataKey(),
                    targetBuffer.getInputStream(), s3ObjectMetadata);
            final PutObjectResult putObjectResult = this.s3.putObject(putObjectRequest);
            result = putObjectResult != null;
            block.setDuplicated(false);
            if (result) {
                getMd5DiskList().put(block.getKey(), block.getMd5());
            }
        } catch (final JsonProcessingException | SdkClientException e) {
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
        final CopyObjectResult result = this.s3.copyObject(getBacketname(), getFullPath(sourceKey), getBacketname(),
                getFullPath(destinationKey));
        return result != null;
    }

    @Override
    public boolean createGenerationFolder(final GenerationProfile profile) {
        this.logger.info("S3 Create Generation Folder - no action required");
        return true;
    }

    @Override
    public boolean createProfileFolder(final FcoGenerationsCatalog fcoProfile) {
        this.logger.info("S3 ProfileVm Folder  - no action required");
        return true;
    }

    @Override
    public boolean dedupDump(final ExBlockInfo block) {
        boolean result = true;
        try {
            String entities;

            entities = this.s3.getObjectAsString(getBacketname(), block.getJsonKey());
            final String newEntities = manageDedupEntities(block, entities);
            this.s3.putObject(getBacketname(), block.getJsonKey(), newEntities);

            block.setDuplicated(true);

            getMd5DiskList().put(block.getKey(), block.getMd5());

        } catch (final JsonProcessingException e) {
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
        boolean result = false;
        final String absolutePath = getFullPath(folderName);
        try {
            while (true) {
                final List<S3ObjectSummary> fileList = this.s3.listObjects(getBacketname(), absolutePath)
                        .getObjectSummaries();
                if (fileList.isEmpty()) {
                    break;
                }
                for (final S3ObjectSummary file : fileList) {
                    this.s3.deleteObject(getBacketname(), file.getKey());
                }
            }
            this.s3.deleteObject(getBacketname(), absolutePath);
            result = true;
        } catch (final AmazonClientException e) {
            Utility.logWarning(this.logger, e);
        }
        return result;
    }

    @Override
    public void deleteObject(final String key) throws IOException {
        this.s3.deleteObject(getBacketname(), key);

    }

    @Override
    public boolean doesKeyExist(final ExBlockInfo block) {
        return this.s3.doesObjectExist(getBacketname(), block.getDataKey())
                && this.s3.doesObjectExist(getBacketname(), block.getJsonKey());
    }

    private String getBacketname() {
        return this.backetName;
    }

    @Override
    public byte[] getObject(final String key) throws IOException {
        byte[] result = null;
        final S3Object s3Object = this.s3.getObject(new GetObjectRequest(getBacketname(), getFullPath(key)));

        try (S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {
            result = IOUtils.toByteArray(objectInputStream);
        }
        return result;
    }

    @Override
    public String getObjectAsString(final String key) throws IOException {
        return this.s3.getObjectAsString(getBacketname(), getFullPath(key));
    }

    @Override
    public byte[] getObjectMd5(final String fullPath) throws IOException {
        final ObjectMetadata s3ObjectMetadata = this.s3.getObjectMetadata(getBacketname(), getFullPath(fullPath));
        final String md5 = s3ObjectMetadata.getETag();
        return md5.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public AwsS3Target getParent() {
        return (AwsS3Target) this.parent;
    }

    public String getRootFolder() {
        return this.parent.getOptions().getRoot();
    }

    @Override
    public ITargetOperation newTargetOperation(final ManagedFcoEntityInfo entityInfo, final Logger logger)
            throws NoSuchAlgorithmException {
        return new AwsS3TargetOperations(getParent(), entityInfo, logger);
    }

    @Override
    public boolean openGetDump(final ExBlockInfo blockInfo, final TargetBuffer targetBuffer) {
        boolean result = false;

        if (this.logger.isLoggable(Level.FINE)) {
            final String msg = String.format("Getting Dump Generation %d from %s", blockInfo.getGenerationId(),
                    blockInfo.getKey());
            this.logger.fine(msg);
        }
        final S3Object s3Object = this.s3.getObject(new GetObjectRequest(getBacketname(), blockInfo.getDataKey()));
        if (blockInfo.getMd5().equalsIgnoreCase(s3Object.getObjectMetadata().getETag())) {
            blockInfo.setStreamSize(s3Object.getObjectMetadata().getContentLength());

            final byte[] buffer = targetBuffer.getInputBuffer();
            int count = 0;
            try (final S3ObjectInputStream s3InputStream = s3Object.getObjectContent()) {
                int n = 0;
                while ((n = s3InputStream.read(buffer, count, Utility.SIXTEEN_KBYTES)) > -1) {
                    count += n;
                }
                result = (count == blockInfo.getStreamSize());

            } catch (final Exception e) {
                Utility.logWarning(this.logger, e);
                result = false;
                blockInfo.setReason(getEntityInfo(), e);
            }
        } else {
            result = false;
            final String msg = String.format("md5 mismatch expected:%s found:%s", blockInfo.getMd5(),
                    s3Object.getObjectMetadata().getETag().toUpperCase(Utility.LOCALE));
            blockInfo.setReason(getEntityInfo(), msg);
            this.logger.warning(msg);
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

        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(digestOutput.size());
        metadata.setContentType(contentType);
        final String md5 = new String(Base64.encodeBase64(md5Digest), StandardCharsets.UTF_8);
        metadata.setContentMD5(md5);
        final InputStream content = digestOutput.getByteArrayInputStream();
        final PutObjectRequest putObjectRequest = new PutObjectRequest(getBacketname(), relativePath, content,
                metadata);
        final PutObjectResult ret = this.s3.putObject(putObjectRequest);
        if (ret != null) {
            if ((profile != null) && path.startsWith(profile.getGenerationPath())) {
                getMd5DiskList().put(path, DatatypeConverter.printHexBinary(md5Digest));
            }
            result = true;
        }
        return result;
    }

    @Override
    public void putObject(final String key, final String content) throws IOException {
        this.s3.putObject(getBacketname(), key, content);

    }

}
