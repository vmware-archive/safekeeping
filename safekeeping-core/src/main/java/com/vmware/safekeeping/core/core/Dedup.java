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

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.core.control.info.ExBlockInfo;

public class Dedup {
	private List<DedupItem> dedupList;

	private boolean compress;

	private boolean cipher;

	private String md5;

	private String sha1;
	private long size;
	private long streamSize;

	public Dedup() {
		this.dedupList = new ArrayList<>();
	}

	public Dedup(final String uuid, final ExBlockInfo block) {
		this.dedupList = new ArrayList<>();
		this.dedupList.add(new DedupItem(uuid, block));

		this.cipher = block.isCipher();
		this.compress = block.isCompress();
		this.md5 = block.getMd5();
		this.sha1 = block.getSha1();
		this.streamSize = block.getStreamSize();
		this.size = block.getSize();

	}

	public List<DedupItem> getDedupList() {
		return this.dedupList;
	}

	public String getMd5() {
		return this.md5;
	}

	public String getSha1() {
		return this.sha1;
	}

	public long getSize() {
		return this.size;
	}

	public long getStreamSize() {
		return this.streamSize;
	}

	public boolean isCipher() {
		return this.cipher;
	}

	public boolean isCompress() {
		return this.compress;
	}

	public void setCipher(final boolean cipher) {
		this.cipher = cipher;
	}

	public void setCompress(final boolean compress) {
		this.compress = compress;
	}

	public void setDedupList(final List<DedupItem> dedupList) {
		this.dedupList = dedupList;
	}

	public void setMd5(final String md5) {
		this.md5 = md5;
	}

	public void setSha1(final String sha1) {
		this.sha1 = sha1;
	}

	public void setSize(final long size) {
		this.size = size;
	}

	public void setStreamSize(final long streamSize) {
		this.streamSize = streamSize;
	}

}