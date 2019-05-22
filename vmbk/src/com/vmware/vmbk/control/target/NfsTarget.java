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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

import org.apache.commons.lang.StringUtils;

import com.emc.ecs.nfsclient.nfs.NfsWriteRequest;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.nfs.io.NfsFileInputStream;
import com.emc.ecs.nfsclient.nfs.io.NfsFileOutputStream;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.portmap.Portmapper;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import com.vmware.jvix.jDiskLib.Block;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.info.DumpFileInfo;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.util.Utility;

public class NfsTarget extends ATarget implements ITarget {
    private static final String DEFAULT_NFS_ABSOLUTE_PATH = "localhost:/vol";
    private static final Integer DEFAULT_NFS_GID = 0;
    private static final Integer DEFAULT_NFS_MAXIMUM_RETRY = 5;
    private static final Integer DEFAULT_NFS_TIMEOUT = 20;
    private static final Integer DEFAULT_NFS_UID = 0;
    private static final Boolean DEFAULT_NFS_USE_PRIVILEGED_PORT = false;
    private static final Boolean DEFAULT_NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME = false;
    private static final String NFS_ABSOLUTE_PATH_NAME = "absolutePath";
    private static final String NFS_GID_NAME = "gid";
    private static final String NFS_MAXIMUM_RETRY_NAME = "maximumRetry";
    private static final String NFS_STORAGE_GROUP = "nfsStorage";
    private static final String NFS_TIMEOUT_NAME = "timeOut";
    private static final String NFS_UID_NAME = "uid";
    private static final String NFS_USE_PRIVILEGED_PORT_NAME = "usePrivilegedPort";
    private static final String NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME = "usePrivilegedPortMapperPort";

//    static {
//	logger = Logger.getLogger(NfsTarget.class.getName());
//    }

    private final String absolutePath;
    private final Integer gid;
    private Set<Integer> gids;
    private InflaterInputStream[] inflaterInputStreams;
    private NfsFileInputStream[] inputDump = null;

    private final Integer maximumRetries;

    private MessageDigest md5;
    private Nfs3 nfs3;
    private ByteArrayInOutStream[] outputDumps;
    private ReadableByteChannel[] readableChannel;
    private int timeOut = -1;
    private final Integer uid;
    private Boolean usePrivilegedPort;
    private Boolean usePrivilegedPortMapperPort;

    public NfsTarget() {
	super();
	this.logger = Logger.getLogger(NfsTarget.class.getName());
	this.configGroup = NFS_STORAGE_GROUP;
	this.timeOut = GlobalConfiguration.getTargetCustomValueAsInt(this.configGroup, NFS_TIMEOUT_NAME,
		DEFAULT_NFS_TIMEOUT);
	this.maximumRetries = GlobalConfiguration.getTargetCustomValueAsInt(this.configGroup, NFS_MAXIMUM_RETRY_NAME,
		DEFAULT_NFS_MAXIMUM_RETRY);
	this.uid = GlobalConfiguration.getTargetCustomValueAsInt(this.configGroup, NFS_UID_NAME, DEFAULT_NFS_UID);
	this.gid = GlobalConfiguration.getTargetCustomValueAsInt(this.configGroup, NFS_GID_NAME, DEFAULT_NFS_GID);

	this.absolutePath = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup, NFS_ABSOLUTE_PATH_NAME,
		DEFAULT_NFS_ABSOLUTE_PATH);

    }

    @Override
    public boolean closeGetDump(final int index, final int bufferIndex) {

	try {
	    if (this.readableChannel[bufferIndex] != null) {
		this.readableChannel[bufferIndex].close();
	    }
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
	try {
	    if (this.inputDump[bufferIndex] != null) {
		this.inputDump[bufferIndex].close();
	    }
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
	final long postEndTime = System.nanoTime();
	this.dumpFilesInfo[index].endTime = postEndTime;
	return true;
    }

    @Override
    public boolean closePostDump(final int index, final int bufferIndex) {
	boolean dumpResult = false;
	try {

	    final String fileName = getGenerationProfile().getDumpContentPath(this.diskId, index);
	    final String fullFileName = getFullPath(fileName);

	    final byte[] md5Digest = (this.compress) ? this.outputDumps[bufferIndex].md5DeflateDigest()
		    : this.outputDumps[bufferIndex].md5Digest();

	    final long postEndTime = System.nanoTime();
	    final String md5DigestSt = Utility.printByteArray(md5Digest);
	    this.getMd5DiskList().put(fileName, md5DigestSt);

	    final Nfs3File nfsPostDump = new Nfs3File(this.nfs3, fullFileName);

	    try (NfsFileOutputStream fileOut = new NfsFileOutputStream(nfsPostDump, NfsWriteRequest.FILE_SYNC)) {
		if (this.compress) {
		    this.outputDumps[bufferIndex].deflateTo(fileOut);
		    this.dumpFilesInfo[index].streamSize = this.outputDumps[bufferIndex].getDeflateSize();

		} else {
		    synchronized (this) {
			this.outputDumps[bufferIndex].writeTo(fileOut);
		    }
		}
	    }
	    this.dumpFilesInfo[index].index = index + 1;
	    this.dumpFilesInfo[index].name = fileName;
	    this.dumpFilesInfo[index].size = this.outputDumps[bufferIndex].size();
	    this.dumpFilesInfo[index].endTime = postEndTime;
	    this.dumpFilesInfo[index].totalChunk = this.dumpFilesInfo.length;
	    this.dumpFilesInfo[index].md5 = md5DigestSt;
	    this.dumpFilesInfo[index].compress = this.compress;
	    dumpResult = true;

	} catch (final Exception e) {
	    this.logger.warning(Utility.toString(e));
	    dumpResult = false;
	} finally {
	    if (!dumpResult) {
		IoFunction.showWarning(this.logger, "Block %d Buffer Index %d FAILED - check logs for more details",
			index, bufferIndex);
	    }
	}
	return dumpResult;
    }

    @Override
    public boolean createGenerationFolder() {
	final String directory = getFullPath(getGenerationProfile().getGenerationPath());
	LoggerUtils.logInfo(this.logger, "Creating folder %s", directory);

	try {
	    final Nfs3File nfsDirectory = new Nfs3File(this.nfs3, directory);
	    nfsDirectory.mkdir();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}

	return true;
    }

    @Override
    public boolean createProfileVmFolder(final FcoProfile fcoProfile) {
	final String directory = getFullPath(fcoProfile.getInstanceUuid());
	LoggerUtils.logInfo(this.logger, "Creating folder %s", directory);
	try {
	    final Nfs3File nfsDirectory = new Nfs3File(this.nfs3, directory);
	    nfsDirectory.mkdir();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
	return true;
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
	final LinkedHashMap<String, String> confDefault = super.defaultConfigurations();
	confDefault.put(NFS_MAXIMUM_RETRY_NAME, DEFAULT_NFS_MAXIMUM_RETRY.toString());
	confDefault.put(NFS_UID_NAME, DEFAULT_NFS_UID.toString());
	confDefault.put(NFS_GID_NAME, DEFAULT_NFS_GID.toString());
	confDefault.put(NFS_ABSOLUTE_PATH_NAME, DEFAULT_NFS_ABSOLUTE_PATH);
	confDefault.put(NFS_USE_PRIVILEGED_PORT_NAME, DEFAULT_NFS_USE_PRIVILEGED_PORT.toString());
	confDefault.put(NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME,
		DEFAULT_NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME.toString());
	confDefault.put(NFS_TIMEOUT_NAME, DEFAULT_NFS_TIMEOUT.toString());
	return confDefault;
    }

    private boolean deleteFolder(final Nfs3File file) {
	boolean ret = true;
	try {
	    final List<Nfs3File> contents = file.listFiles();
	    if (contents != null) {
		for (final Nfs3File f : contents) {
		    if (f.followLinks() != null) {
			ret &= deleteFolder(f);
		    }
		}
	    }
	    file.remove();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
	return ret;
    }

    @Override
    public boolean deleteFolder(final String folderName) {
	try {
	    return deleteFolder(new Nfs3File(this.nfs3, folderName));
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
    }

    @Override
    protected void disposeBuffers() {
	this.readableChannel = null;
	this.inflaterInputStreams = null;
	this.outputDumps = null;
	this.inputDump = null;
    }

    private boolean fileExist(final String filepath) {
	try {
	    final String fullPath = getFullPath(filepath);
	    final Nfs3File lFile = new Nfs3File(this.nfs3, fullPath);
	    return lFile.exists();
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
    }

    @Override
    public void finalize() {
	this.logger.info("Closing NFS");
	this.nfs3 = null;
    }

    @Override
    protected byte[] get(final String path) {
	byte[] ret = null;
	try {
	    final Nfs3File nfsGet = new Nfs3File(this.nfs3, getFullPath(path));
	    try (NfsFileInputStream inputStream = new NfsFileInputStream(nfsGet)) {
		ret = new byte[inputStream.available()];
		inputStream.read(ret);
	    }
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return null;
	}
	return ret;
    }

    private String getAbsolutePath() {

	return this.absolutePath;
    }

    @Override
    public boolean getDump(final int index, final int bufferIndex, final ByteBuffer buffer) {
	try {
	    this.dumpFilesInfo[index].size += this.readableChannel[bufferIndex].read(buffer);

	    return true;
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
    }

    @Override
    public String getFullPath(final String path) {
	return "/" + path.replace("%20", " ");
    }

    private int getGid() {
	return this.gid;
    }

    private int getMaximumRetries() {

	return this.maximumRetries;
    }

    @Override
    public byte[] getObjectMd5(final String fullPath) {
	try {
	    final String md5Filepath = getFullPath(fullPath);
	    if (this.md5 == null) {
		this.md5 = MessageDigest.getInstance("MD5");
	    }
	    final Nfs3File md5File = new Nfs3File(this.nfs3, md5Filepath);
	    final byte[] md5Res = Utility.getDigest(new NfsFileInputStream(md5File), this.md5, 2048 * 10);
	    return md5Res;
	} catch (final NoSuchAlgorithmException | IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return null;
	}
    }

    private int getTimeOut() {
	return this.timeOut;
    }

    private int getUid() {
	return this.uid;
    }

    @Override
    public String getUri(final String path) {
	return String.format("nfs://%s/%s", this.absolutePath, path);
    }

    @Override
    public boolean initialize() {
	Nfs3.NFS_TIMEOUT = getTimeOut();
	this.gids = null;
	final CredentialUnix credential = new CredentialUnix(getUid(), getGid(), this.gids);
	Portmapper.setUsePrivilegedPort(isUsePrivilegedPortMapperPort());
	try {
	    this.nfs3 = new Nfs3(getAbsolutePath(), credential, getMaximumRetries(), isUsePrivilegedPort());
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    IoFunction.showWarning(this.logger, e.getMessage());
	    return false;
	}
	return true;
    }

    @Override
    public boolean initializeGetBuffersArray(final int size) {
	initializeBuffersArray(size);
	final int maxThread = GlobalConfiguration.getMaxGetThreadsPool();
	this.readableChannel = new ReadableByteChannel[maxThread];
	this.inputDump = new NfsFileInputStream[maxThread];
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
	return fileExist(getGenerationProfile().getDigestContentPath(generationId, this.diskId));
    }

    @Override
    public boolean isDumpOutExist(final int generationId, final int diskId) {
	return fileExist(getGenerationProfile().getDumpContentPath(generationId, this.diskId, 1));
    }

    @Override
    public boolean isMd5FileExist(final int generationId) {
	return fileExist(getGenerationProfile().getMd5ContentPath(generationId));
    }

    @Override
    public boolean isProfAllVmExist() {
	return fileExist(GlobalConfiguration.getGlobalProfileFileName());
    }

    @Override
    public boolean isProfileVmExist() {
	return fileExist(GlobalConfiguration.getDefaultProfileVmPath(this.fco.getUuid()));
    }

    private boolean isUsePrivilegedPort() {
	if (this.usePrivilegedPort == null) {
	    this.usePrivilegedPort = GlobalConfiguration.getTargetCustomValueAsBool(this.configGroup,
		    NFS_USE_PRIVILEGED_PORT_NAME, DEFAULT_NFS_USE_PRIVILEGED_PORT);
	}
	return this.usePrivilegedPort;
    }

    private boolean isUsePrivilegedPortMapperPort() {
	if (this.usePrivilegedPortMapperPort == null) {
	    final String privPortStr = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup,
		    NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME);
	    if (StringUtils.isEmpty(privPortStr)) {
		this.usePrivilegedPortMapperPort = DEFAULT_NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME;
	    } else {
		this.usePrivilegedPortMapperPort = PrettyBoolean.parseBoolean(privPortStr);
	    }
	}
	return this.usePrivilegedPortMapperPort;
    }

    @Override
    public LinkedHashMap<String, String> manualConfiguration() {
	final LinkedHashMap<String, String> confFiletargetParams = super.manualConfiguration();
	confFiletargetParams.put(NFS_ABSOLUTE_PATH_NAME, "NFS Absolute Path (server:/Export) [%s]");
	confFiletargetParams.put(NFS_UID_NAME, "User UID [%s]");
	confFiletargetParams.put(NFS_GID_NAME, "User GID [%s]");
	confFiletargetParams.put(NFS_MAXIMUM_RETRY_NAME, "Max number of retry [%s]");
	confFiletargetParams.put(NFS_USE_PRIVILEGED_PORT_NAME, "Use port <1024 [%s]");
	confFiletargetParams.put(NFS_USE_PRIVILEGED_PORTMAPPER_PORT_NAME, "Use portmap port <1024 [%s]");
	confFiletargetParams.put(NFS_TIMEOUT_NAME, "NFS Timeout [%s]");

	return confFiletargetParams;
    }

    @Override
    public boolean openGetDump(final int index, final Block block, final int bufferIndex, final int size) {
	final int genId = this.profGen.getGenerationId();
	final String fileName = getGenerationProfile().getDumpContentPath(this.diskId, index);
	final String fullFileName = getFullPath(fileName);
	this.logger.fine(String.format("Getting Dump Generation %d to %s", genId, fileName));
	try {
	    final DumpFileInfo dumpFileInfo = new DumpFileInfo();

	    final Nfs3File dumpFile = new Nfs3File(this.nfs3, fullFileName);
	    this.inputDump[bufferIndex] = new NfsFileInputStream(dumpFile);

	    if (this.compress) {
		this.inflaterInputStreams[bufferIndex] = new InflaterInputStream(this.inputDump[bufferIndex]);
		dumpFileInfo.streamSize = dumpFile.length();
		this.readableChannel[bufferIndex] = Channels.newChannel(this.inflaterInputStreams[bufferIndex]);
	    } else {
		this.readableChannel[bufferIndex] = Channels.newChannel(this.inputDump[bufferIndex]);

	    }

	    this.dumpFilesInfo[index] = dumpFileInfo;
	    this.dumpFilesInfo[index].startTime = System.nanoTime();
	    this.dumpFilesInfo[index].name = fileName;
	    this.dumpFilesInfo[index].totalChunk = this.dumpFilesInfo.length;
	    this.dumpFilesInfo[index].index = index + 1;
	    this.dumpFilesInfo[index].block = block;
	    this.dumpFilesInfo[index].compress = this.compress;

	    this.dumpFilesInfo[index].md5 = "";
	    return true;
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return false;
	}
    }

    @Override
    public boolean openPostDump(final int index, final Block block, final int bufferIndex, final int size) {
	final long startTime = System.nanoTime();
	final DumpFileInfo dumpFileInfo = new DumpFileInfo();
	this.dumpFilesInfo[index] = dumpFileInfo;
	this.dumpFilesInfo[index].startTime = startTime;
	this.dumpFilesInfo[index].block = block;
	this.outputDumps[bufferIndex].reset();
	return true;
    }

    @Override
    protected boolean post(final String path, final ByteArrayInOutStream byteArrayStream, final String contentType) {

	byte[] md5Digest = null;
	try {
	    md5Digest = byteArrayStream.md5Digest();

	    NfsFileOutputStream out = null;
	    final String fullPath = getFullPath(path);
	    final Nfs3File nfsPostFile = new Nfs3File(this.nfs3, fullPath);
	    try {

		out = new NfsFileOutputStream(nfsPostFile);

		byteArrayStream.writeTo(out);
	    } catch (final FileNotFoundException e) {
		this.logger.warning(Utility.toString(e));
		return false;
	    }

	    catch (final IOException e) {
		this.logger.warning(Utility.toString(e));
		return false;
	    } finally {
		try {
		    if (out != null) {
			out.close();
		    }
		} catch (final IOException e) {
		    this.logger.warning(Utility.toString(e));
		    return false;
		}
	    }
	    if ((getGenerationProfile() != null) && fullPath.startsWith(getGenerationProfile().getDumpPath())) {
		this.getMd5DiskList().put(fullPath, Utility.printByteArray(md5Digest));
	    }
	    return true;
	} catch (final IOException e) {
	    return false;
	}

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
