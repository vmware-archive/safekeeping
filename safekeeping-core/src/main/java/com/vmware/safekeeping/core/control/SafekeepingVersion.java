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
package com.vmware.safekeeping.core.control;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.JDiskLibFactory;
import com.vmware.jvix.JVixException;
import com.vmware.jvix.VddkVersion;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

/**
 * Safekeeping Version.
 */
public final class SafekeepingVersion {
    private static final Logger logger = Logger.getLogger(SafekeepingVersion.class.getName());
    public static final String PRODUCT_NAME = "Safekeeping";
    public static final String PRODUCT_DESCRIPTION = "Safekeeping the open source VMware backup";
    private static final String PRODUCT_VERSION = "2.0.1";
    public static final String COMPANY = "VMware Inc.";
    public static final String COMPANY_URL = "http://www.vmware.com/";
    public static final String PRODUCT_URL = "https://github.com/vmware/safekeeping";

    private static SafekeepingVersion instance;

    private static String requestedVersion;

    private static final Pattern DOT_DASH_SEPARATED = Pattern.compile("[\\s.-]", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * @return
     */
    public static SafekeepingVersion getInstance() {

        return instance;
    }

    /**
     * @throws JVixException
     * @throws IOException
     * @throws SafekeepingException
     * @throws URISyntaxException
     * @throws Exception
     *
     */
    public static SafekeepingVersion initialize(final String version)
            throws JVixException, IOException, SafekeepingException, URISyntaxException {
        if ((instance == null) || (StringUtils.isNotBlank(version) && !requestedVersion.equals(version))) {
            requestedVersion = version;
            instance = new SafekeepingVersion();
            instance.verStringArray = PRODUCT_VERSION.split("\\.");
            if (instance.verStringArray.length != 3) {
                throw new SafekeepingException("Failure: " + PRODUCT_NAME + " version number uncorrect %s",
                        PRODUCT_VERSION);
            }
            instance.vddkVersionsList.addAll(JDiskLibFactory.getVddkVersionList());
            if (!instance.vddkVersionsList.isEmpty()) {
                if (StringUtils.isEmpty(version)) {
                    if (StringUtils.isNotBlank(CoreGlobalSettings.getVddkVersion())) {
                        instance.vddk = new VddkVersion(CoreGlobalSettings.getVddkVersion());
                    } else {
                        instance.vddk = JDiskLibFactory.getLastVddkVersion(instance.vddkVersionsList);
                    }
                } else {
                    instance.vddk = new VddkVersion(version);
                }
            } else {
                throw new SafekeepingException("No detectable VDDK versions");
            }
            if (logger.isLoggable(Level.INFO)) {
                final String msg = String.format("%s version %s detected", PRODUCT_NAME, PRODUCT_VERSION);
                logger.info(msg);
            }
        }
        return instance;
    }

    public static boolean validate(final String version) {
        return validate(DOT_DASH_SEPARATED.split(version));
    }

    private static boolean validate(final String[] version) {
        boolean result = true;
        switch (version.length) {
        case 4:
            result &= StringUtils.isNumeric(version[3]);
            result &= StringUtils.isNumeric(version[2]);
            result &= StringUtils.isNumeric(version[1]);
            result &= StringUtils.isNumeric(version[0]);
            break;

        case 3:
            result &= StringUtils.isNumeric(version[2]);
            result &= StringUtils.isNumeric(version[1]);
            result &= StringUtils.isNumeric(version[0]);
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

    private final List<VddkVersion> vddkVersionsList;

    private VddkVersion vddk;

    private String[] verStringArray;

    private SafekeepingVersion() {
        this.vddkVersionsList = new ArrayList<>();
    }

    public String getExtendedVersion() {
        return String.format("%s Version %s - VDDK Version %s", PRODUCT_NAME, getVersion(), getVddk().getVersion());
    }

    public String getIdentity() {
        return String.format("%s_Ver.%s", PRODUCT_NAME, PRODUCT_VERSION);
    }

    public String getJavaRuntime() {
        return Utility.getJavaRuntimeInfo();
    }

    public int getMajor() {
        return Integer.parseInt(this.verStringArray[0]);
    }

    public int getMinor() {
        return Integer.parseInt(this.verStringArray[1]);
    }

    public int getPatchLevev() {
        return Integer.parseInt(this.verStringArray[2]);
    }

    public String getProductName() {
        return PRODUCT_NAME;
    }

    /**
     * @return the vddk
     */
    public VddkVersion getVddk() {
        return this.vddk;
    }

    public String getVddkVersion() {
        return this.vddk.getVersion();
    }

    public List<VddkVersion> getVddkVersionsList() {
        return this.vddkVersionsList;
    }

    public String getVersion() {
        return PRODUCT_VERSION;
    }

    public boolean isInitialized() {
        return this.verStringArray != null;
    }
}
