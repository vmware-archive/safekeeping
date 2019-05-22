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
package com.vmware.vmbk.control.info;

import com.vmware.vmbk.type.FirstClassObjectType;

public class TotalDumpFileInfo extends DumpFileInfo {

    private final int diskId;
    private FirstClassObjectType firstClassObjectType;
    private final int numberObject;

    protected TotalDumpFileInfo() {
	this.diskId = 0;
	this.numberObject = 0;
    }

    public TotalDumpFileInfo(final FirstClassObjectType firstClassObjectType, final int diskId,
	    final DumpFileInfo[] dumpFileInfo, final long startTime, final long endTime) {
	this.firstClassObjectType = firstClassObjectType;

	this.diskId = diskId;
	this.startTime = startTime;
	this.endTime = endTime;
	this.index = 0;
	this.numberObject = dumpFileInfo.length;
	if (dumpFileInfo[0] != null) {
	    this.compress = dumpFileInfo[0].compress;
	}
	this.totalChunk = dumpFileInfo.length;

	this.name = "Total";
	for (final DumpFileInfo info : dumpFileInfo) {
	    if (info != null) {
		this.size += info.size;
		this.streamSize += info.streamSize;
	    }
	}
    }

    public FirstClassObjectType getFirstClassObjectType() {
	return this.firstClassObjectType;
    }

    @Override
    public String toString() {
	if (this.compress) {
	    return String.format("TOTAL                      %10s  %10s  %6.2fs  %5.2fMB/s   %7s  %5.2fMB/s",
		    printSize(), printStreamSize(), getOverallTimeInSeconds(), getMbSec(),
		    getExplicitCompressionRatio(), getStreamMbSec());
	} else {
	    return String.format("TOTAL                      %10s\t%5.2fs\t%5.2fMB/s", printSize(),
		    getOverallTimeInSeconds(), getMbSec());
	}
    }

    public int getDiskId() {
	return diskId;
    }

    public int getNumberObject() {
	return numberObject;
    }

}
