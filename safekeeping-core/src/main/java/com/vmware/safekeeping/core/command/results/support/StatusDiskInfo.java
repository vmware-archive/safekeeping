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
package com.vmware.safekeeping.core.command.results.support;

import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.enums.BackupMode;

public class StatusDiskInfo {

	private final int diskId = 0;

	private long dumpElapsedTimeMs;

	private long capacity;

	private BackupMode backupMode;

	public StatusDiskInfo() {
	}

	/**
	 * Constructor
	 *
	 * @param diskId
	 * @param profGen
	 */
	public StatusDiskInfo(final GenerationProfile profGen) {
		this.dumpElapsedTimeMs = profGen.getDumpElapsedTime(this.diskId);
		this.capacity = profGen.getCapacity(this.diskId);
		this.backupMode = profGen.getDiskBackupMode(this.diskId);
	}

	/**
	 * @return the backupMode
	 */
	public BackupMode getBackupMode() {
		return this.backupMode;
	}

	/**
	 * @return the capacity
	 */
	public long getCapacity() {
		return this.capacity;
	}

	/**
	 * @return the diskId
	 */
	public int getDiskId() {
		return this.diskId;
	}

	/**
	 * @return the dumpElapsedTimeMs
	 */
	public long getDumpElapsedTimeMs() {
		return this.dumpElapsedTimeMs;
	}

	/**
	 * @param backupMode the backupMode to set
	 */
	public void setBackupMode(final BackupMode backupMode) {
		this.backupMode = backupMode;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(final long capacity) {
		this.capacity = capacity;
	}

	/**
	 * @param dumpElapsedTimeMs the dumpElapsedTimeMs to set
	 */
	public void setDumpElapsedTimeMs(final long dumpElapsedTimeMs) {
		this.dumpElapsedTimeMs = dumpElapsedTimeMs;
	}

}
