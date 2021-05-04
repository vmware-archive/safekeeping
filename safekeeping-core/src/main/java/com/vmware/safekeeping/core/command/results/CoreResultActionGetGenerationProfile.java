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
package com.vmware.safekeeping.core.command.results;

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.core.profile.BasicBlockInfo;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.type.enums.BackupMode;

public class CoreResultActionGetGenerationProfile extends AbstractCoreResultActionImpl {
	/**
     * 
     */
    private static final long serialVersionUID = -5542138311539002701L;
    private final int generationId;
	private final int numberOfGenerations;
	private BackupMode backupMode;
	private final int parentGenerationId;

	private final int numberOfThreads;
	private final List<BasicBlockInfo> vixBlocks;
	private final GenerationProfile profile;
	private final long size;
	private final long compressedSize;
	private final Integer diskId;
	private final boolean compressed;
	private final String targetName;
	private final boolean ciphered;

	/**
	 * @param profGen
	 * @param result
	 */
	public CoreResultActionGetGenerationProfile(final GenerationProfile profile,
			final AbstractCoreResultActionDiskVirtualBackupAndRestore result) {
		setFcoEntityInfo(result.getFcoEntityInfo());
		this.diskId = result.getDiskId();
		this.generationId = profile.getGenerationId();
		this.parentGenerationId = profile.getPreviousGenerationId();
		this.numberOfGenerations = result.getNumberOfGenerations();
		this.backupMode = profile.getBackupMode();
		this.numberOfThreads = result.getNumberOfThreads();
		this.targetName = result.getTargetName();
		this.profile = profile;
		final DiskProfile disk = profile.getDisks().get(this.diskId);
		this.compressed = disk.isCompression();
		this.ciphered = disk.isCipher();
		this.size = disk.getTotalUncompressedDumpSize();
		this.compressedSize = disk.getTotalDumpSize();
		result.getDiskRestoreGenerationsProfile().add(this);
		this.vixBlocks = new ArrayList<>();
	}

	public BackupMode getBackupMode() {
		return this.backupMode;
	}

	/**
	 * @return the compressedSize
	 */
	public long getCompressedSize() {
		return this.compressedSize;
	}

	public Integer getDiskId() {
		return this.diskId;
	}

	public int getGenerationId() {
		return this.generationId;
	}

	public Integer getNumberOfDumps() {
		return this.vixBlocks.size();
	}

	public int getNumberOfGenerations() {
		return this.numberOfGenerations;
	}

	public int getNumberOfThreads() {
		return this.numberOfThreads;
	}

	public int getParentGenerationId() {
		return this.parentGenerationId;
	}

	public GenerationProfile getProfile() {
		return this.profile;
	}

	public long getSize() {
		return this.size;
	}

	/**
	 * @return the targetName
	 */
	public String getTargetName() {
		return this.targetName;
	}

	public List<BasicBlockInfo> getVixBlocks() {
		return this.vixBlocks;
	}

	/**
	 * @return the ciphered
	 */
	public boolean isCiphered() {
		return this.ciphered;
	}

	public boolean isCompressed() {
		return this.compressed;
	}

	/**
	 * @param backupMode the backupMode to set
	 */
	public void setBackupMode(final BackupMode backupMode) {
		this.backupMode = backupMode;
	}

}
