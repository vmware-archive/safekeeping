package com.vmware.safekeeping.core.core;

import java.util.concurrent.atomic.AtomicBoolean;

import com.vmware.jvix.jDiskLib.Connection;
import com.vmware.jvix.jDiskLib.DiskHandle;
import com.vmware.safekeeping.common.Utility;

class DiskOpenCloseHandle {

	private final DiskHandle diskHandle;
	private final boolean open;
	private final Connection connHandle;

	private final int flags;
	private final String remoteDiskPath;
	private long vddkCallResult;
	private final AtomicBoolean executed;

	DiskOpenCloseHandle(final Connection connHandle, final int flags) {
		this(connHandle, flags, null);
	}

	DiskOpenCloseHandle(final Connection connHandle, final int flags, final String remoteDiskPath) {
		this.diskHandle = new DiskHandle();
		this.open = true;
		this.connHandle = connHandle;
		this.flags = flags;
		this.remoteDiskPath = Utility.removeQuote(remoteDiskPath);
		this.vddkCallResult = -1;
		this.executed = new AtomicBoolean(false);
	}

	/**
	 * Close the DiskHandle
	 *
	 * @param diskHandle
	 */
	DiskOpenCloseHandle(final DiskHandle diskHandle) {
		this.diskHandle = diskHandle;
		this.open = false;
		this.connHandle = null;
		this.flags = 0;
		this.remoteDiskPath = null;
		this.vddkCallResult = -1;
		this.executed = new AtomicBoolean(false);
	}

	/**
	 * @return the connHandle
	 */
	public Connection getConnHandle() {
		return this.connHandle;
	}

	/**
	 * @return the diskHandle
	 */
	public DiskHandle getDiskHandle() {
		return this.diskHandle;
	}

	/**
	 * @return the executed
	 */
	public AtomicBoolean getExecuted() {
		return this.executed;
	}

	/**
	 * @return the flags
	 */
	public int getFlags() {
		return this.flags;
	}

	/**
	 * @return the remoteDiskPath
	 */
	public String getRemoteDiskPath() {
		return this.remoteDiskPath;
	}

	/**
	 * @return the vddkCallResult
	 */
	public long getVddkCallResult() {
		return this.vddkCallResult;
	}

	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return this.open;
	}

	/**
	 * @param vddkCallResult the vddkCallResult to set
	 */
	public void setVddkCallResult(final long vddkCallResult) {
		this.vddkCallResult = vddkCallResult;
	}

}