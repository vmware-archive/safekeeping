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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.info.DumpFileInfo;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.util.Utility;

public class FileTarget extends ATarget implements ITarget {
    private static final String ARCHIVE_DIRECTORY = "archive_directory";
    private static final String FILE_STORAGE_GROUP = "fileStorage";

//    static {
//	logger = Logger.getLogger(FileTarget.class.getName());
//    }
    private String archiveDirectory;

    private InflaterInputStream[] inflaterInputStreams;
    private FileInputStream[] inputDump = null;
    private MessageDigest md5;
    private ByteArrayInOutStream[] outputDumps;
    private ReadableByteChannel[] readableChannel;

    public FileTarget() {
	super();
	this.logger = Logger.getLogger(FileTarget.class.getName());
	this.configGroup = FILE_STORAGE_GROUP;

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
	    final File dumpFile = new File(fullFileName);
	    try (FileOutputStream fileOut = new FileOutputStream(dumpFile, false)) {
		if (this.compress) {
		    this.outputDumps[bufferIndex].deflateTo(fileOut);
		    this.dumpFilesInfo[index].streamSize = this.outputDumps[bufferIndex].getDeflateSize();

		} else {
		    this.outputDumps[bufferIndex].writeTo(fileOut);
		}
	    }
	    final byte[] md5Digest = (this.compress) ? this.outputDumps[bufferIndex].md5DeflateDigest()
		    : this.outputDumps[bufferIndex].md5Digest();

	    final long postEndTime = System.nanoTime();
	    final String md5DigestSt = Utility.printByteArray(md5Digest);
	    this.getMd5DiskList().put(fileName, md5DigestSt);

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
	if ((new File(directory)).mkdir() == false) {
	    this.logger.warning(String.format("mkdir %s failed.", directory));
	    return false;
	}
	return true;
    }

    @Override
    public boolean createProfileVmFolder(final FcoProfile fcoProfile) {
	final String directory = getFullPath(fcoProfile.getInstanceUuid());
	LoggerUtils.logInfo(this.logger, "Creating folder %s", directory);
	if ((new File(directory)).mkdir() == false) {
	    this.logger.warning(String.format("mkdir %s failed.", directory));
	    return false;
	}
	return true;
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
	final LinkedHashMap<String, String> confDefault = super.defaultConfigurations();
	final String defaul_archiveDirectory = new File("").getAbsoluteFile().getAbsolutePath() + File.separatorChar
		+ "archive";
	confDefault.put(ARCHIVE_DIRECTORY, defaul_archiveDirectory);
	return confDefault;
    }

    private boolean deleteFolder(final File file) {
	boolean ret = true;
	final File[] contents = file.listFiles();
	if (contents != null) {
	    for (final File f : contents) {
		if (!Files.isSymbolicLink(f.toPath())) {
		    ret &= deleteFolder(f);
		}
	    }
	}
	ret &= file.delete();
	return ret;
    }

    @Override
    public boolean deleteFolder(final String folderName) {
	return deleteFolder(new File(folderName));
    }

    @Override
    protected void disposeBuffers() {
	this.readableChannel = null;
	this.inflaterInputStreams = null;
	this.outputDumps = null;
	this.inputDump = null;
    }

    @Override
    protected byte[] get(final String path) {
	byte[] ret = null;
	try {
	    final Path filePath = new File(getFullPath(path)).toPath();
	    ret = Files.readAllBytes(filePath);
	} catch (final IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return null;
	}
	return ret;
    }

    public String getArchiveDirectory() {
	if (this.archiveDirectory == null) {
	    this.archiveDirectory = GlobalConfiguration.getTargetCustomValueAsString(this.configGroup,
		    ARCHIVE_DIRECTORY);
	    if ((this.archiveDirectory == null) || this.archiveDirectory.isEmpty()) {
		this.archiveDirectory = new File("").getAbsoluteFile().getAbsolutePath() + File.separatorChar
			+ "archive";
	    }
	    final File ArchiveFile = new File(this.archiveDirectory);
	    if (!ArchiveFile.isDirectory()) {
		ArchiveFile.mkdirs();
	    }
	}
	return this.archiveDirectory;
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
	return getArchiveDirectory() + File.separatorChar + path.replace("%20", " ");
    }

    @Override
    public byte[] getObjectMd5(final String fullPath) {
	try {

	    final String md5File = getFullPath(fullPath);
	    if (this.md5 == null) {

		this.md5 = MessageDigest.getInstance("MD5");
	    }
	    final byte[] md5Res = Utility.getDigest(new FileInputStream(md5File), this.md5, 2048);

	    return md5Res;
	} catch (final NoSuchAlgorithmException | IOException e) {
	    this.logger.warning(Utility.toString(e));
	    return null;
	}
    }

    @Override
    public String getUri(final String path) {
	return String.format("file://%s/%s", this.archiveDirectory, path);
    }

    @Override
    public boolean initializeGetBuffersArray(final int size) {
	initializeBuffersArray(size);
	final int maxThread = GlobalConfiguration.getMaxGetThreadsPool();
	this.readableChannel = new ReadableByteChannel[maxThread];
	this.inputDump = new FileInputStream[maxThread];
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
	return (new File(filepath)).isFile();
    }

    @Override
    public boolean isDumpOutExist(final int generationId, final int diskId) {
	final String filepath = getFullPath(getGenerationProfile().getDumpContentPath(generationId, this.diskId, 1));
	return (new File(filepath)).isFile();
    }

    @Override
    public boolean isMd5FileExist(final int generationId) {
	final String filepath = getFullPath(getGenerationProfile().getMd5ContentPath(generationId));
	return (new File(filepath)).isFile();
    }

    @Override
    public boolean isProfAllVmExist() {
	final String profAllVmPath = getFullPath(GlobalConfiguration.getGlobalProfileFileName());
	return (new File(profAllVmPath)).isFile();
    }

    @Override
    public boolean isProfileVmExist() {

	final String profVmPath = getFullPath(GlobalConfiguration.getDefaultProfileVmPath(this.fco.getUuid()));
	return (new File(profVmPath)).isFile();
    }

    @Override
    public LinkedHashMap<String, String> manualConfiguration() {
	final LinkedHashMap<String, String> confFiletargetParams = super.manualConfiguration();
	confFiletargetParams.put(ARCHIVE_DIRECTORY, "File Archive directory [%s]");
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
	    final File dumpFile = new File(fullFileName);
	    this.inputDump[bufferIndex] = new FileInputStream(dumpFile);

	    if (this.compress) {
		this.inflaterInputStreams[bufferIndex] = new InflaterInputStream(this.inputDump[bufferIndex]);
		dumpFileInfo.streamSize = dumpFile.length();
		this.readableChannel[bufferIndex] = Channels.newChannel(this.inflaterInputStreams[bufferIndex]);
	    } else {
		this.readableChannel[bufferIndex] = this.inputDump[bufferIndex].getChannel();

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

	    FileOutputStream out = null;
	    final String fullPath = getFullPath(path);
	    try {
		out = new FileOutputStream(new File(fullPath));
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
