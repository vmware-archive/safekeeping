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
package com.vmware.safekeeping.core.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JVmdkInfo {
	private int adapterType;

	private long capacityInSectors;

	private final Map<String, String> metadata;

	private long nBlocks;

	private int numLinks;

	JVmdkInfo() {
		this.adapterType = 0;
		this.nBlocks = 0;
		this.numLinks = 0;
		this.metadata = new HashMap<>();
	}

	public int getAdapterType() {
		return this.adapterType;
	}

	public long getCapacityInSectors() {
		return this.capacityInSectors;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public long getnBlocks() {
		return this.nBlocks;
	}

	public int getNumLinks() {
		return this.numLinks;
	}

	public void setAdapterType(final int adapterType) {
		this.adapterType = adapterType;
	}

	public void setCapacityInSectors(final long capacityInSectors) {
		this.capacityInSectors = capacityInSectors;
	}

	public void setnBlocks(final long nBlocks) {
		this.nBlocks = nBlocks;
	}

	public void setNumLinks(final int numLinks) {
		this.numLinks = numLinks;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append("adapterType= ");
		switch (this.adapterType) {
		case 1:
			str.append("ADAPTER_IDE ");
			break;
		case 2:
			str.append("ADAPTER_SCSI_BUSLOGIC ");
			break;
		case 3:
			str.append("ADAPTER_SCSI_LSILOGIC ");
			break;
		case 256:
			str.append("ADAPTER_UNKNOWN ");
			break;
		default:
			str.append("MISSING DESCRIPTION ");
			break;
		}
		str.append(String.format("blocks= %d ", this.nBlocks));
		str.append(String.format("Links= %d ", this.numLinks));
		for (final Entry<String, String> entry : this.metadata.entrySet()) {
			str.append(entry.getKey());
			str.append(" = ");
			str.append(entry.getValue());
			str.append(' ');
		}
		return str.toString();
	}
}