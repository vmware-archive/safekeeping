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
package com.vmware.jvix;

import java.util.regex.Pattern;

public class VddkVersion {
    private int major;
    private int minor;
    private int patchLevel;
    private int build;
    private String version;
    private String extendedVersion;

    public VddkVersion(final int major, final int minor, final int patchLevel, final int build) {
        setMajor(major);
        setMinor(minor);
        setPatchLevel(patchLevel);
        setBuild(build);
        setVersion(String.format("%d.%d.%d.%d", major, minor, patchLevel, build));
        setExtendedVersion(String.format("VDDK Version %d.%d.%d build %d", major, minor, patchLevel, build));
    }

    private static final Pattern REGEX_SPITTED = Pattern.compile("[\\s.-]", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * @param vddkVersionStr
     * @throws Exception
     */
    public VddkVersion(final String version) throws JVixException {

        final String[] vddkVerStringArray = REGEX_SPITTED.split(version);
        if (!validate(vddkVerStringArray)) {
            throw new JVixException("Failure: VDDK version number uncorrect " + version);
        }
        setMajor(Integer.parseUnsignedInt(vddkVerStringArray[0]));
        setMinor(Integer.parseUnsignedInt(vddkVerStringArray[1]));
        setPatchLevel(Integer.parseUnsignedInt(vddkVerStringArray[2]));
        setBuild(Integer.parseUnsignedInt(vddkVerStringArray[3]));
        setVersion(version);
        setExtendedVersion(String.format("VDDK Version %s.%s.%s build %s", vddkVerStringArray[0], vddkVerStringArray[1],
                vddkVerStringArray[2], vddkVerStringArray[3]));
    }

    public VddkVersion(final String[] version) {
        setMajor(Integer.parseUnsignedInt(version[0]));
        setMinor(Integer.parseUnsignedInt(version[1]));
        setPatchLevel(Integer.parseUnsignedInt(version[2]));
        setBuild(Integer.parseUnsignedInt(version[3]));
        setVersion(String.format("%d.%d.%d.%d", this.major, this.minor, this.patchLevel, this.build));
        setExtendedVersion(
                String.format("VDDK Version %d.%d.%d build %d", this.major, this.minor, this.patchLevel, this.build));
    }

    public int checkVersion(final String version) {
        final String[] vddkVerStringArray = REGEX_SPITTED.split(version);

        final int lMajor = Integer.parseUnsignedInt(vddkVerStringArray[0]);
        if (lMajor > this.major) {
            return -1;
        }
        if (lMajor < this.major) {
            return 1;
        }
        if (vddkVerStringArray.length > 1) {
            final int lminor = Integer.parseUnsignedInt(vddkVerStringArray[1]);
            if (lminor > this.minor) {
                return -1;
            }
            if (lminor < this.minor) {
                return 1;
            }
            if (vddkVerStringArray.length > 2) {
                final int lpatch = Integer.parseUnsignedInt(vddkVerStringArray[2]);
                if (lpatch > this.patchLevel) {
                    return -1;
                }
                if (lpatch < this.patchLevel) {
                    return 1;
                }
                if (vddkVerStringArray.length > 3) {
                    final int lbuild = Integer.parseUnsignedInt(vddkVerStringArray[3]);
                    if (lbuild > this.build) {
                        return -1;
                    }
                    if (lbuild < this.build) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * @return the build
     */
    public int getBuild() {
        return this.build;
    }

    /**
     * @return the extendedVersion
     */
    public String getExtendedVersion() {
        return this.extendedVersion;
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
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * If these methods don't throw any NumberFormatException, then it means that
     * the parsing was successful and the String is numeric
     *
     * @param strNum
     * @return
     */
    private boolean isNumeric(final String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * @param build the build to set
     */
    public void setBuild(final int build) {
        this.build = build;
    }

    /**
     * @param extendedVersion the extendedVersion to set
     */
    public void setExtendedVersion(final String extendedVersion) {
        this.extendedVersion = extendedVersion;
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
     * @param version the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return this.extendedVersion;
    }

    public boolean validate(final String[] version) {

        boolean result = true;
        switch (version.length) {
        case 4:
            result &= isNumeric(version[3]);
            result &= isNumeric(version[2]);
            result &= isNumeric(version[1]);
            result &= isNumeric(version[0]);
            break;
        case 3:
            result &= isNumeric(version[2]);
            result &= isNumeric(version[1]);
            result &= isNumeric(version[0]);
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

}
