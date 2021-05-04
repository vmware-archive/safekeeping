/**
 *
 */
package com.vmware.safekeeping.external.type;

public class VddkVersion {
	private int major;
	private int minor;
	private int patchLevel;
	private int build;
	private String version;
	private String extendedVersion;

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
}
