/**
 *
 */
package com.vmware.vmbk.util;

/**
 * @author mdaneri
 *
 */
public class VersionManipulator {

    private final String[] verStringArray;

    public VersionManipulator(final String version) {
	this.verStringArray = version.split("\\.");
    }

    public int getBuildNumber() {
	if (this.verStringArray.length < 3) {
	    return 0;
	}
	return Integer.parseInt(this.verStringArray[3]);
    }

    public int getMajor() {

	if (this.verStringArray.length < 1) {
	    return 0;
	}
	return Integer.parseInt(this.verStringArray[0]);
    }

    public int getMinor() {
	if (this.verStringArray.length < 2) {
	    return 0;
	}
	return Integer.parseInt(this.verStringArray[1]);
    }

    public int getPatchLevev() {
	if (this.verStringArray.length < 3) {
	    return 0;
	}
	return Integer.parseInt(this.verStringArray[2]);
    }

    public int toInteger() {
	return (getMajor() * 10000) + (getMinor() * 100) + (getPatchLevev() * 1);
    }
    // 60000+700+2
}
