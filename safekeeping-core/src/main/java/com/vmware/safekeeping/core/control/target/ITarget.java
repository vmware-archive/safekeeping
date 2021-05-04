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
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.options.AbstractCoreTargetRepository;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;

public interface ITarget {

	void close();

	Map<String, String> defaultConfigurations();

	boolean doesObjectExist(String key);

	default String getFullPath(final String path) {
		if (StringUtils.isEmpty(getRoot())) {
			return path;
		} else if (path.startsWith(getRoot())) {
			return path;
		} else {
			return getRoot().concat(getSeparator()).concat(path);
		}
	}

	byte[] getGlobalProfileToByteArray() throws IOException;

	String getName();

	AbstractCoreTargetRepository getOptions();

	default String getRoot() {
		return getOptions().getRoot();
	}

	String getSeparator();

	/**
	 * Type of target ex: S3 , File,etc
	 * 
	 * @return
	 */
	String getTargetType();

	String getUri(String path);

	boolean isEnable();

	boolean isProfAllVmExist();

	Map<String, String> manualConfiguration();

	ITargetOperation newTargetOperation(final ManagedFcoEntityInfo entityInfo, final Logger logger);

	boolean open() throws SafekeepingConnectionException;

	boolean updateFcoProfileCatalog(ByteArrayInOutStream byteArrayInOutStream);

}
