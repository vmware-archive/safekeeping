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
package com.vmware.vmbk.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import com.vmware.vmbk.profile.GlobalConfiguration;

public class VmbkLog {

    public static void loadLogSetting() throws IOException {
//	final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	try (final InputStream in = new FileInputStream(
		GlobalConfiguration.getConfigPath() + File.separatorChar + "log.properties")) {
	    loadLogSetting(in);
	}
    }

    private static void loadLogSetting(final InputStream in) throws IOException {
	LogManager.getLogManager().readConfiguration(in);
	final String logDirName = LogManager.getLogManager().getProperty("java.util.logging.FileHandler.pattern");

	final File logDir = new File(GlobalConfiguration.getInstallPath() + File.separatorChar
		+ logDirName.substring(0, logDirName.indexOf("/")));
	if (!logDir.isDirectory()) {
	    logDir.mkdirs();
	}
    }

// TODO Remove unused code found by UCDetector
//     public static void loadLogSetting(final String filePath) throws FileNotFoundException, IOException {
// 	final InputStream in = new FileInputStream(filePath);
// 	loadLogSetting(in);
// 	in.close();
//     }

}
