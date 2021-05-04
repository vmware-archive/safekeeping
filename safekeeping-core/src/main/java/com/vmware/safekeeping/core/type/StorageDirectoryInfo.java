package com.vmware.safekeeping.core.type;

import org.apache.commons.lang.StringUtils;

public class StorageDirectoryInfo {
	private ManagedEntityInfo dcInfo;
	private ManagedEntityInfo dsInfo;
	private String datastoreUuid;
	private String directoryUuid;
	private String directoryName;

	public StorageDirectoryInfo(final ManagedEntityInfo dcInfo, final ManagedEntityInfo dsInfo,
			final String directory) {
		super();
		this.dcInfo = dcInfo;
		this.dsInfo = dsInfo;
		if (directory.startsWith("[")) {
			this.directoryUuid = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(directory, "] "), "/");
		} else {
			this.directoryName = directory;
		}
	}

	public String getDatastoreUuid() {
		return this.datastoreUuid;
	}

	public ManagedEntityInfo getDcInfo() {
		return this.dcInfo;
	}

	public String getDirectoryName() {
		return this.directoryName;
	}

	public String getDirectoryUuid() {
		return this.directoryUuid;
	}

	public ManagedEntityInfo getDsInfo() {
		return this.dsInfo;
	}

	public String getFileUri(final String fileName) {
		return String.format("[%s] %s/%s", getDsInfo().getName(), getDirectoryUuid(), fileName);

	}

	public void setDatastoreUuid(final String datastoreUuid) {
		this.datastoreUuid = datastoreUuid;
	}

	public void setDcInfo(final ManagedEntityInfo dcInfo) {
		this.dcInfo = dcInfo;
	}

	public void setDirectoryName(final String directoryName) {
		this.directoryName = directoryName;
	}

	public void setDirectoryUuid(final String directoryUuid) {
		this.directoryUuid = directoryUuid;
	}

	public void setDsInfo(final ManagedEntityInfo dsInfo) {
		this.dsInfo = dsInfo;
	}

}