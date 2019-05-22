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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

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
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.vmware.jvix.jDiskLib.Block;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.VmbkVersion;
import com.vmware.vmbk.control.info.DumpFileInfo;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.util.Utility;

public class AwsS3Target extends ATarget implements ITarget {

    private static final String BACKET_NAME = "backetName";
    private static final String DEFAULT_REGION_NAME = "us-west-2";
    private static final String REGION_NAME = "region";

    private static final String S3_ACCESS_KEY = "accessKey";
    private static final String S3_GROUP = "amazonS3";

    private static final String S3_SECRET_KEY = "secretKey";

    private InflaterInputStream[] inflaterInputStreams;
    private S3ObjectInputStream[] objectInputStream;

    private ByteArrayInOutStream[] outputDumps;
    private ReadableByteChannel[] readableChannel;
    private String region;
    private AmazonS3 s3;
    private String rootFolder;

    private String backetName;

    public AwsS3Target() {
	super();
	this.logger = Logger.getLogger(AwsS3Target.class.getName());
	this.logger.entering(getClass().getName(), "S3Target");
	this.configGroup = S3_GROUP;
	this.backetName = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup, BACKET_NAME);
	if ((this.backetName == null) || this.backetName.isEmpty()) {
	    this.backetName = VmbkVersion.getProductName() + VmbkVersion.getMajor();
	}

	if (this.backetName.contains("/")) {
	    final int slashIndex = this.backetName.indexOf('/');
	    this.rootFolder = this.backetName.substring(slashIndex + 1);
	    this.backetName = this.backetName.substring(0, slashIndex);
	}

	this.region = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup, REGION_NAME);
	if ((this.region == null) || this.region.isEmpty()) {
	    this.region = DEFAULT_REGION_NAME;
	}
	this.logger.exiting(getClass().getName(), "S3Target");
    }

    @Override
    public boolean closeGetDump(final int index, final int bufferIndex) {
	this.logger.entering(getClass().getName(), "closePostDump");
	boolean result = true;
	try {
	    this.objectInputStream[bufferIndex].close();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    result = false;
	}
	try {
	    this.readableChannel[bufferIndex].close();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    result = false;
	}
	this.dumpFilesInfo[index].endTime = System.nanoTime();
	this.logger.exiting(getClass().getName(), "closeGetDump", result);
	return result;
    }

    @Override
    public boolean closePostDump(final int index, final int bufferIndex) {
	this.logger.entering(getClass().getName(), "closePostDump");
	boolean result = false;
	try {
	    final String fileName = getFullPath(getGenerationProfile().getDumpContentPath(this.diskId, index));
	    if (StringUtils.isEmpty(fileName)) {
		IoFunction.showWarning(this.logger, "DumpContentPath Disk:%d Index:%d bufferIndex:%s is null or empty",
			this.diskId, index, bufferIndex);
		return false;
	    }
	    final InputStream streamContent = (this.compress) ? this.outputDumps[bufferIndex].getDeflateInputStream()
		    : this.outputDumps[bufferIndex].getInputStream();
	    final byte[] md5Digest = (this.compress) ? this.outputDumps[bufferIndex].md5DeflateDigest()
		    : this.outputDumps[bufferIndex].md5Digest();

	    final ObjectMetadata metadata = new ObjectMetadata();

	    final String md5 = new String(Base64.encodeBase64(md5Digest), "UTF-8");
	    metadata.setContentMD5(md5);
	    final int streamSize = streamContent.available();
	    metadata.setContentLength(streamSize);
	    metadata.setContentType(MIME_BINARY_OCTECT_STREAM);

	    final PutObjectRequest putObjectRequest = new PutObjectRequest(getBacketname(), fileName, streamContent,
		    metadata);

	    final PutObjectResult ret = this.s3.putObject(putObjectRequest);
	    if (ret != null) {
		final String md5DigestSt = Utility.printByteArray(md5Digest);

		this.getMd5DiskList().put(fileName, md5DigestSt);
		this.dumpFilesInfo[index].index = index + 1;
		this.dumpFilesInfo[index].name = fileName;
		this.dumpFilesInfo[index].size = this.outputDumps[bufferIndex].size();
		this.dumpFilesInfo[index].streamSize = streamSize;
		this.dumpFilesInfo[index].totalChunk = this.dumpFilesInfo.length;
		this.dumpFilesInfo[index].md5 = md5DigestSt;
		this.dumpFilesInfo[index].compress = this.compress;
		result = true;
	    } else {
		result = false;
	    }
	} catch (

	final Exception e) {
	    this.logger.warning(Utility.toString(e));
	    result = false;

	} finally {
	    if (!result) {
		IoFunction.showWarning(this.logger, "Block %d Buffer Index %d FAILED - check logs for more details",
			index, bufferIndex);
	    }
	    final long postEndTime = System.nanoTime();
	    this.dumpFilesInfo[index].endTime = postEndTime;
	}
	this.logger.exiting(getClass().getName(), "closePostDump", result);
	return result;
    }

    private Bucket createAmazonS3Bucket(final String bucketName) {
	this.logger.entering(getClass().getName(), "createAmazonS3Bucket");
	Bucket result = null;
	try {

	    result = this.s3.createBucket(bucketName);

	} catch (final AmazonClientException e) {
	    this.logger.warning(Utility.toString(e));
	}
	this.logger.exiting(getClass().getName(), "createAmazonS3Bucket", result);
	return result;
    }

    @Override
    public boolean createGenerationFolder() {
	this.logger.entering(getClass().getName(), "createGenerationFolder");
	this.logger.info("S3 Create Generation Folder - no action required");
	final boolean result = true;
	this.logger.exiting(getClass().getName(), "createGenerationFolder", result);
	return result;
    }

    @Override
    public boolean createProfileVmFolder(final FcoProfile fcoProfile) {
	this.logger.entering(getClass().getName(), "createProfileVmFolder", fcoProfile);
	this.logger.info("S3 ProfileVm Folder  - no action required");
	final boolean result = true;
	this.logger.exiting(getClass().getName(), "createProfileVmFolder", result);
	return result;
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
	this.logger.entering(getClass().getName(), "defaultConfigurations");
	final LinkedHashMap<String, String> result = super.defaultConfigurations();
	result.put(REGION_NAME, DEFAULT_REGION_NAME);
	this.logger.exiting(getClass().getName(), "defaultConfigurations", result);
	return result;
    }

    @Override
    public boolean deleteFolder(final String folderName) {
	this.logger.entering(getClass().getName(), "deleteFolder", folderName);
	boolean result = false;
	final String absolutePath = getFullPath(folderName);
	try {
	    final List<S3ObjectSummary> fileList = this.s3.listObjects(getBacketname(), absolutePath)
		    .getObjectSummaries();
	    for (final S3ObjectSummary file : fileList) {
		this.s3.deleteObject(getBacketname(), file.getKey());
	    }
	    this.s3.deleteObject(getBacketname(), absolutePath);
	    result = true;
	} catch (final AmazonClientException e) {
	    this.logger.warning(Utility.toString(e));
	}
	this.logger.exiting(getClass().getName(), "deleteFolder", result);
	return result;
    }

    @Override
    protected void disposeBuffers() {
	this.logger.entering(getClass().getName(), "disposeBuffers");
	this.objectInputStream = null;
	this.readableChannel = null;
	this.inflaterInputStreams = null;
	this.outputDumps = null;
	this.logger.exiting(getClass().getName(), "disposeBuffers");
    }

    private boolean doesBucketExist(final String bucketName) {
	this.logger.entering(getClass().getName(), "doesBucketExist");
	boolean result = false;
	try {
	    result = this.s3.doesBucketExistV2(bucketName);
	} catch (final AmazonClientException e) {
	    this.logger.warning(Utility.toString(e));
	}
	this.logger.exiting(getClass().getName(), "doesBucketExist", result);
	return result;
    }

    @Override
    public void finalize() {
	this.logger.entering(getClass().getName(), "finalize");
	this.logger.info("S3 finalize");
	this.s3 = null;
	this.logger.exiting(getClass().getName(), "finalize");
    }

    @Override
    protected byte[] get(final String fullPath) {
	this.logger.entering(getClass().getName(), "get", fullPath);
	final String fileName = getFullPath(fullPath);
	S3ObjectInputStream objectInputStream = null;
	byte[] result = null;

	if (this.s3.doesObjectExist(getBacketname(), fileName)) {
	    final S3Object s3Object = this.s3.getObject(new GetObjectRequest(getBacketname(), fileName));

	    try {
		objectInputStream = s3Object.getObjectContent();
		result = IOUtils.toByteArray(objectInputStream);
	    } catch (final IOException e) {
		this.logger.warning(Utility.toString(e));

	    } finally {
		try {
		    objectInputStream.close();
		} catch (final IOException e) {
		    this.logger.warning(Utility.toString(e));
		}
	    }
	}

	this.logger.exiting(getClass().getName(), "get", result);
	return result;
    }

    private String getBacketname() {
	return this.backetName;
    }

    @Override
    public boolean getDump(final int index, final int bufferIndex, final ByteBuffer buffer) {
	try {
	    this.readableChannel[bufferIndex].read(buffer);
	    this.dumpFilesInfo[index].size += buffer.position();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
	return true;
    }

    @Override
    public String getFullPath(final String path) {
	if (StringUtils.isEmpty(getRootFolder())) {
	    return path;
	} else if (path.startsWith(getRootFolder())) {
	    return path;
	} else {
	    return getRootFolder().concat("/").concat(path);
	}
    }

    @Override
    public byte[] getObjectMd5(final String fullPath) {
	final ObjectMetadata s3ObjectMetadata = this.s3.getObjectMetadata(getBacketname(), getFullPath(fullPath));
	final String md5 = s3ObjectMetadata.getETag();
	try {
	    return md5.getBytes("UTF-8");
	} catch (final UnsupportedEncodingException e) {
	    this.logger.warning(Utility.toString(e));
	    return null;
	}
    }

    private String getRegion() {
	return this.region;
    }

    private String getRootFolder() {
	return this.rootFolder;
    }

    private String getS3AccessKey() {
	final String accessKey = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup, S3_ACCESS_KEY);
	if (accessKey == "") {
	    return null;
	}
	return accessKey;
    }

    public String getS3SecretKey() {
	final String secretKey = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup, S3_SECRET_KEY);
	if (GlobalConfiguration.useBase64Passwd()) {
	    return new String(Base64.decodeBase64(secretKey));
	}
	return secretKey;
    }

    @Override
    public String getUri(final String path) {
	return String.format("s3://%s/%s", this.backetName, path);
    }

    @Override
    public boolean initialize() {
	return login();
    }

    @Override
    public boolean initializeGetBuffersArray(final int size) {
	initializeBuffersArray(size);
	final int maxThread = GlobalConfiguration.getMaxGetThreadsPool();
	this.objectInputStream = new S3ObjectInputStream[maxThread];
	this.readableChannel = new ReadableByteChannel[maxThread];
	this.inflaterInputStreams = new InflaterInputStream[maxThread];
	return true;
    }

    @Override
    public boolean initializePostBuffersArray(final int size) {
	initializeBuffersArray(size);
	final int maxThread = GlobalConfiguration.getMaxPostThreadsPool();
	this.outputDumps = new ByteArrayInOutStream[maxThread];
	for (int i = 0; i < maxThread; ++i) {
	    this.outputDumps[i] = new ByteArrayInOutStream(GlobalConfiguration.getMaxBlockSize() * 2);
	}
	return true;
    }

    @Override
    public boolean isBlockTracksOutExist(final int generationId, final int diskId) {
	final String filepath = getFullPath(getGenerationProfile().getDigestContentPath(generationId, this.diskId));
	return this.s3.doesObjectExist(getBacketname(), filepath);

    }

    @Override
    public boolean isDumpOutExist(final int generationId, final int diskId) {
	final String filepath = getFullPath(getGenerationProfile().getDumpContentPath(generationId, this.diskId, 1));
	return this.s3.doesObjectExist(getBacketname(), filepath);
    }

    @Override
    public boolean isMd5FileExist(final int generationId) {
	final String filename = getFullPath(getGenerationProfile().getMd5ContentPath(generationId));
	return this.s3.doesObjectExist(getBacketname(), filename);
    }

    @Override
    public boolean isProfAllVmExist() {
	final String profAllFcoPath = getFullPath(GlobalConfiguration.getGlobalProfileFileName());
	return this.s3.doesObjectExist(getBacketname(), profAllFcoPath);
    }

    @Override
    public boolean isProfileVmExist() {
	final String profFcoPath = getFullPath(GlobalConfiguration.getDefaultProfileVmPath(this.fco.getUuid()));
	return this.s3.doesObjectExist(getBacketname(), profFcoPath);
    }

    private boolean login() {
	this.logger.entering(getClass().getName(), "login");
	boolean result = false;
	BasicAWSCredentials credentials;
	try {
	    credentials = new BasicAWSCredentials(getS3AccessKey(), getS3SecretKey());
	    LoggerUtils.logInfo(this.logger, "Login to S3 backet %s Region %s", getBacketname(), getRegion());
	    this.s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
		    .withRegion(getRegion()).build();
	    result = (doesBucketExist(getBacketname()));
	    if (!result) {
		result = createAmazonS3Bucket(getBacketname()) != null;
	    }
	} catch (final Exception e) {
	    this.logger.warning(Utility.toString(e));

	    result = false;
	}
	this.logger.exiting(getClass().getName(), "login", result);
	return result;
    }

    @Override
    public LinkedHashMap<String, String> manualConfiguration() {
	this.logger.entering(getClass().getName(), "manualConfiguration");
	final LinkedHashMap<String, String> result = super.manualConfiguration();
	result.put(REGION_NAME, "S3 Region [%s]");
	result.put(BACKET_NAME, "S3 Backet Name/folder [%s]");
	result.put(S3_ACCESS_KEY, "S3 Access Key [%s]");
	result.put(S3_SECRET_KEY, "S3 Secret Key <hidden>");
	this.logger.exiting(getClass().getName(), "manualConfiguration", result);
	return result;
    }

    @Override
    public boolean openGetDump(final int index, final Block block, final int bufferIndex, final int size) {
	this.logger.entering(getClass().getName(), "openGetDump", new Object[] { index, block, bufferIndex, size });
	final int genId = this.profGen.getGenerationId();
	final String fileName = getFullPath(getGenerationProfile().getDumpContentPath(genId, this.diskId, index));
	this.logger.fine(String.format("Getting Dump Generation %d to %s", genId, fileName));
	final DumpFileInfo dumpFileInfo = new DumpFileInfo();
	this.dumpFilesInfo[index] = dumpFileInfo;
	this.dumpFilesInfo[index].startTime = System.nanoTime();
	this.dumpFilesInfo[index].name = fileName;
	this.dumpFilesInfo[index].totalChunk = this.dumpFilesInfo.length;
	this.dumpFilesInfo[index].index = index + 1;
	this.dumpFilesInfo[index].block = block;
	this.dumpFilesInfo[index].compress = this.compress;

	final S3Object s3Object = this.s3.getObject(new GetObjectRequest(getBacketname(), fileName));
	this.dumpFilesInfo[index].md5 = s3Object.getObjectMetadata().getETag();

	this.objectInputStream[bufferIndex] = s3Object.getObjectContent();
	if (this.compress) {
	    try {
		final byte[] buf = IOUtils.toByteArray(this.objectInputStream[bufferIndex]);
		final ByteArrayInputStream temp = new ByteArrayInputStream(buf);
		this.inflaterInputStreams[bufferIndex] = new InflaterInputStream(temp);
	    } catch (final IOException e) {
		this.logger.warning(Utility.toString(e));
		return false;
	    }
	    this.dumpFilesInfo[index].streamSize = (int) s3Object.getObjectMetadata().getContentLength();
	    this.readableChannel[bufferIndex] = Channels.newChannel(this.inflaterInputStreams[bufferIndex]);
	} else {
	    this.readableChannel[bufferIndex] = Channels.newChannel(this.objectInputStream[bufferIndex]);
	}
	final boolean result = true;
	this.logger.exiting(getClass().getName(), "openGetDump", result);
	return result;
    }

    @Override
    public boolean openPostDump(final int index, final Block block, final int bufferIndex, final int size) {
	this.logger.entering(getClass().getName(), "openPostDump", new Object[] { index, block, bufferIndex, size });
	final long startTime = System.nanoTime();
	final DumpFileInfo dumpFileInfo = new DumpFileInfo();
	this.dumpFilesInfo[index] = dumpFileInfo;
	this.dumpFilesInfo[index].startTime = startTime;
	this.dumpFilesInfo[index].block = block;
	this.outputDumps[bufferIndex].reset();
	final boolean result = true;
	this.logger.exiting(getClass().getName(), "openPostDump", result);
	return result;
    }

    @Override
    protected boolean post(final String path, final ByteArrayInOutStream digestOutput, final String contentType) {
	this.logger.entering(getClass().getName(), "post", new Object[] { path, digestOutput, contentType });
	boolean result = false;
	byte[] md5Digest = null;
	try {
	    final String relativePath = getFullPath(path);
	    md5Digest = digestOutput.md5Digest();

	    final ObjectMetadata metadata = new ObjectMetadata();
	    metadata.setContentLength(digestOutput.size());
	    metadata.setContentType(contentType);
	    final String md5 = new String(Base64.encodeBase64(md5Digest), "UTF-8");
	    metadata.setContentMD5(md5);

	    final InputStream content = digestOutput.getInputStream();

	    final PutObjectRequest putObjectRequest = new PutObjectRequest(getBacketname(), relativePath, content,
		    metadata);

	    final PutObjectResult ret = this.s3.putObject(putObjectRequest);
	    if (ret != null) {
		if ((getGenerationProfile() != null) && path.startsWith(getGenerationProfile().getDumpPath())) {
		    this.getMd5DiskList().put(relativePath, Utility.printByteArray(md5Digest));
		}
		result = true;
	    }
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	}

	this.logger.exiting(getClass().getName(), "post", result);
	return result;
    }

    @Override
    public boolean postDump(final int index, final int bufferIndex, final ByteBuffer buffer) {
	try {
	    this.outputDumps[bufferIndex].write(buffer);
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}

	return true;
    }
}
