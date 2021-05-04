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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.IOUtils;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreFileTargetOptions;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public class FileTarget extends AbstractTarget {

	public static final String ROOT_FOLDER = "root";
	public static final String TARGET_TYPE_NAME = "fileStorage";

//    public FileTarget() {
//	this(new CoreFileOptions());
//	getOptions().setRootFolder(CoreGlobalSettings.getTargetCustomValueAsString(S3_GROUP, ROOT_FOLDER));
//
//	getOptions().setDefaultTarget(CoreGlobalSettings.getTargetRepository().equals(S3_GROUP));
//	getOptions().setName("Default_" + S3_GROUP);
//	getOptions().setActive(CoreGlobalSettings.getTargetCustomValueAsBool(S3_GROUP, ACTIVE_KEY));
//    }

	public FileTarget(final CoreFileTargetOptions options) {
		super(options);
		this.logger = Logger.getLogger(FileTarget.class.getName());
		this.options = options;
		this.targetType = TARGET_TYPE_NAME;
	}

	@Override
	public void close() {
		// No need
	}

	@Override
	public LinkedHashMap<String, String> defaultConfigurations() {
		final LinkedHashMap<String, String> result = super.defaultConfigurations();
		result.put(ROOT_FOLDER, "");
		return result;
	}

	@Override
	public boolean doesObjectExist(final String key) {
		final File keyFile = new File(getFullPath(key));
		return keyFile.exists();
	}

	@Override
	public byte[] getGlobalProfileToByteArray() throws IOException {

		final String contentName = getFullPath(CoreGlobalSettings.getGlobalProfileFileName());
		if (this.logger.isLoggable(Level.INFO)) {
			final String msg = String.format("Get %s from %s", contentName, getTargetType());
			this.logger.info(msg);
		}
		byte[] result = null;
		if (doesObjectExist(contentName)) {
			result = IOUtils.readBinaryFile(contentName);
		}
		return result;
	}

	@Override
	public CoreFileTargetOptions getOptions() {
		return (CoreFileTargetOptions) this.options;
	}

	@Override
	public String getSeparator() {
		return File.separator;
	}

	@Override
	public String getUri(final String path) {
		return String.format("file://%s/%s", getOptions().getRootFolder(), path);
	}

	@Override
	public boolean isProfAllVmExist() {
		final String profAllFcoPath = getFullPath(CoreGlobalSettings.getGlobalProfileFileName());

		final File profile = new File(profAllFcoPath);
		return profile.exists();

	}

	@Override
	public LinkedHashMap<String, String> manualConfiguration() {
		final LinkedHashMap<String, String> result = super.manualConfiguration();
		result.put(ROOT_FOLDER, "Root Folder [%s]");

		return result;
	}

	@Override
	public ITargetOperation newTargetOperation(final ManagedFcoEntityInfo entityInfo, final Logger logger) {
		return new FileTargetOperations(this, entityInfo, logger);
	}

	@Override
	public boolean open() {
		boolean result = false;

		final File root = new File(getOptions().getRootFolder());
		result = root.exists();

		return result;
	}

	@Override
	protected boolean post(final String path, final ByteArrayInOutStream digestOutput, final String contentType) {
		boolean result = false;
		try {
			final File fileOut = new File(getFullPath(path));
			IOUtils.inputStreamToFile(digestOutput.getByteArrayInputStream(), fileOut);

			result = true;
		} catch (final IOException e) {
			Utility.logWarning(this.logger, e);
		}
		return result;
	}

}
