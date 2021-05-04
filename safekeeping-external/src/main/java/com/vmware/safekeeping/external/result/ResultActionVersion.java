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
package com.vmware.safekeeping.external.result;

import java.util.regex.Pattern;

import com.vmware.safekeeping.core.command.results.CoreResultActionVersion;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.external.type.ServerInfo;
import com.vmware.safekeeping.external.type.VddkVersion;

public class ResultActionVersion extends ResultAction {
    private static final Pattern PATTERN_SPLIT_VERSION = Pattern.compile("[\\s.-]", Pattern.UNICODE_CHARACTER_CLASS);

    public static void convert(final CoreResultActionVersion src, final ResultActionVersion dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);
        try {
            dst.setExtendedVersion(src.getVersion().getExtendedVersion());
            dst.setVersion(src.getVersion().getVersion());
            dst.setIdentity(src.getVersion().getIdentity());
            dst.setMajor(src.getVersion().getMajor());
            dst.setMinor(src.getVersion().getMinor());
            dst.setPatchLevel(src.getVersion().getPatchLevev());
            dst.setProductName(src.getVersion().getProductName());

            final String[] vddkVerStringArray = PATTERN_SPLIT_VERSION.split(src.getVersion().getVddkVersion());
//            
//            
//            = src.getVersion().getVddkVersion().
//                    split("[\\s.-]");

            dst.getVddk().setMajor(Integer.parseUnsignedInt(vddkVerStringArray[0]));
            dst.getVddk().setMinor(Integer.parseUnsignedInt(vddkVerStringArray[1]));
            dst.getVddk().setPatchLevel(Integer.parseUnsignedInt(vddkVerStringArray[2]));
            dst.getVddk().setBuild(Integer.parseUnsignedInt(vddkVerStringArray[3]));
            dst.getVddk().setVersion(src.getVersion().getVddkVersion());
            dst.getVddk().setExtendedVersion(String.format("VDDK Version %s.%s.%s build %s", vddkVerStringArray[0],
                    vddkVerStringArray[1], vddkVerStringArray[2], vddkVerStringArray[3]));

            dst.getServerInfo().setExtendedVersion(src.getServerInfo().getExtendedVersion());
            dst.getServerInfo().setServerIp(src.getServerInfo().getiIpAddress());
            dst.getServerInfo().setBigEndianBiosUuid(src.getServerInfo().getBigEndianBiosUuid());
            dst.getServerInfo().setServerBiosUuid(src.getServerInfo().getServerBiosUuid());
            dst.getServerInfo().setHostname(src.getServerInfo().getHostname());
            dst.getServerInfo().setServerOs(src.getServerInfo().getServerOs());
            dst.setJavaRuntime(src.getJavaRuntime());
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private String extendedVersion;

    private String version;

    private String identity;

    private int major;

    private int minor;

    private int patchLevel;

    private String productName;

    private VddkVersion vddk;
    private ServerInfo serverInfo;
    private String javaRuntime;

    public ResultActionVersion() {
        this.vddk = new VddkVersion();
        this.serverInfo = new ServerInfo();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionVersion.convert((CoreResultActionVersion) src, this);
    }

    /**
     * @return the extendedVersion
     */
    public String getExtendedVersion() {
        return this.extendedVersion;
    }

    /**
     * @return the identity
     */
    public String getIdentity() {
        return this.identity;
    }

    public String getJavaRuntime() {
        return this.javaRuntime;
    }

    /**
     * @return the major
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * @return the minor
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * @return the patchLevel
     */
    public int getPatchLevel() {
        return this.patchLevel;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * @return the serverInfo
     */
    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    /**
     * @return the vddk
     */
    public VddkVersion getVddk() {
        return this.vddk;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param extendedVersion the extendedVersion to set
     */
    public void setExtendedVersion(final String extendedVersion) {
        this.extendedVersion = extendedVersion;
    }

    /**
     * @param identity the identity to set
     */
    public void setIdentity(final String identity) {
        this.identity = identity;
    }

    public void setJavaRuntime(final String javaRuntime) {
        this.javaRuntime = javaRuntime;
    }

    /**
     * @param major the major to set
     */
    public void setMajor(final int major) {
        this.major = major;
    }

    /**
     * @param minor the minor to set
     */
    public void setMinor(final int minor) {
        this.minor = minor;
    }

    /**
     * @param patchLevel the patchLevel to set
     */
    public void setPatchLevel(final int patchLevel) {
        this.patchLevel = patchLevel;
    }

    /**
     * @param productName the productName to set
     */
    public void setProductName(final String productName) {
        this.productName = productName;
    }

    /**
     * @param serverInfo the serverInfo to set
     */
    public void setServerInfo(final ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * @param vddk the vddk to set
     */
    public void setVddk(final VddkVersion vddk) {
        this.vddk = vddk;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }

}
