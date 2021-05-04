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
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreAwsS3TargetOptions;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class AwsS3Target extends AbstractTarget {

    public static final String BACKET_NAME = "backetName";
    public static final String REGION_NAME = "region";

    public static final String S3_ACCESS_KEY = "accessKey";
    public static final String TARGET_TYPE_NAME = "amazonS3";

    public static final String S3_SECRET_KEY = "secretKey";
    private AmazonS3 s3;

    public AwsS3Target(final CoreAwsS3TargetOptions options) {
        super(options);
        this.logger = Logger.getLogger(AwsS3Target.class.getName());
        this.options = options;
        this.targetType = TARGET_TYPE_NAME;

    }

    @Override
    public void close() {
        this.s3.shutdown();
    }

    private Bucket createAmazonS3Bucket(final String bucketName) {
        Bucket result = null;
        try {

            result = this.s3.createBucket(bucketName);

        } catch (final AmazonClientException e) {
            Utility.logWarning(this.logger, e);
        }
        return result;
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
        final LinkedHashMap<String, String> result = super.defaultConfigurations();
        result.put(REGION_NAME, CoreAwsS3TargetOptions.DEFAULT_REGION_NAME);
        return result;
    }

    private boolean doesBucketExist(final String bucketName) {
        boolean result = false;

        result = this.s3.doesBucketExistV2(bucketName);

        return result;
    }

    @Override
    public boolean doesObjectExist(final String key) {
        return this.s3.doesObjectExist(getBacketName(), getFullPath(key));
    }

    public String getBacketName() {
        return getOptions().getBacket();
    }

    @Override
    public byte[] getGlobalProfileToByteArray() throws IOException {

        final String contentName = CoreGlobalSettings.getGlobalProfileFileName();
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Get %s from %s", contentName, getTargetType());
            this.logger.info(msg);
        }
        byte[] result = null;
        if (doesObjectExist(contentName)) {
            final S3Object s3Object = this.s3
                    .getObject(new GetObjectRequest(getBacketName(), getFullPath(contentName)));

            try (S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {
                result = IOUtils.toByteArray(objectInputStream);
            }
        }
        return result;
    }

    @Override
    public CoreAwsS3TargetOptions getOptions() {
        return (CoreAwsS3TargetOptions) this.options;
    }

    /**
     * @return the s3
     */
    public AmazonS3 getS3() {
        return this.s3;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public String getUri(final String path) {
        return String.format("s3://%s/%s", getOptions().getBacket(), path);
    }

    @Override
    public boolean isProfAllVmExist() {
        final String profAllFcoPath = getFullPath(CoreGlobalSettings.getGlobalProfileFileName());
        return this.s3.doesObjectExist(getOptions().getBacket(), profAllFcoPath);
    }

    @Override
    public LinkedHashMap<String, String> manualConfiguration() {
        final LinkedHashMap<String, String> result = super.manualConfiguration();
        result.put(REGION_NAME, "S3 Region [%s]");
        result.put(BACKET_NAME, "S3 Backet Name/folder [%s]");
        result.put(S3_ACCESS_KEY, "S3 Access Key [%s]");
        result.put(S3_SECRET_KEY, "S3 Secret Key <hidden>");
        return result;
    }

    @Override
    public ITargetOperation newTargetOperation(final ManagedFcoEntityInfo entityInfo, final Logger logger) {
        return new AwsS3TargetOperations(this, entityInfo, logger);
    }

    @Override
    public boolean open() throws SafekeepingConnectionException {
        boolean result = false;
        BasicAWSCredentials credentials;
        try {
            credentials = new BasicAWSCredentials(getOptions().getAccessKey(),
                    (getOptions().isBase64()) ? (new String(Base64.decodeBase64(getOptions().getSecretKey())))
                            : getOptions().getSecretKey());
            if (this.logger.isLoggable(Level.INFO)) {
                final String msg = String.format("Login to S3 backet %s Region %s", getOptions().getBacket(),
                        getOptions().getRegion());
                this.logger.info(msg);
            }
            this.s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(getOptions().getRegion()).build();

            result = (doesBucketExist(getOptions().getBacket()));

            if (result) {
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Backet %s Region %s already exist", getOptions().getBacket(),
                            getOptions().getRegion());
                    this.logger.info(msg);
                }
            } else {
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Backet %s Region %s doesn't exist", getOptions().getBacket(),
                            getOptions().getRegion());
                    this.logger.info(msg);
                }
                result = createAmazonS3Bucket(getOptions().getBacket()) != null;
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Backet %s Region %s created", getOptions().getBacket(),
                            getOptions().getRegion());
                    this.logger.info(msg);
                }
            }
        } catch (final Exception e) {
            Utility.logWarning(this.logger, e);
            throw new SafekeepingConnectionException(e);

        }
        return result;
    }

    @Override
    protected boolean post(final String path, final ByteArrayInOutStream digestOutput, final String contentType) {
        boolean result = false;
        byte[] md5Digest = null;
        try {
            final String relativePath = getFullPath(path);
            md5Digest = digestOutput.md5Digest();

            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(digestOutput.size());
            metadata.setContentType(contentType);
            final String md5 = new String(Base64.encodeBase64(md5Digest), StandardCharsets.UTF_8);
            metadata.setContentMD5(md5);
            final InputStream content = digestOutput.getByteArrayInputStream();
            final PutObjectRequest putObjectRequest = new PutObjectRequest(getOptions().getBacket(), relativePath,
                    content, metadata);
            final PutObjectResult ret = this.s3.putObject(putObjectRequest);
            if (ret != null) {
                result = true;
            }
        } catch (final AmazonClientException e) {
            Utility.logWarning(this.logger, e);
        }
        return result;
    }

}
