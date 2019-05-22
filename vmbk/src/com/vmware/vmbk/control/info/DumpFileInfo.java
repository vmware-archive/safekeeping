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

import com.vmware.jvix.jDiskLib.Block;

public class DumpFileInfo {

    public static String header(final boolean compress) {
	if (compress) {
	    return "    n  tot          offset       size    compress    time    n-speed   c-ratio    r-speed\tmd5\t\t\t\t\tfile";
	} else {
	    return "    n  tot          offset       size     time      speed\tmd5\t\t\t\t\tfile";
	}
    }
    public Block block;
    public boolean compress;
    public long endTime;
    public int index;
    public String md5;
    public String name;
    public long size;
    public long startTime;
    public long streamSize;

    public int totalChunk;

    public float getCompressionRatio() {
	return ((float) this.size) / ((float) this.streamSize);
    }

    public String getExplicitCompressionRatio() {
	return String.format("%4.1f:1", ((float) this.size) / ((float) this.streamSize));
    }

    public float getMbSec() {
	return getSizeInMb() / (getOverallTime() * 0.000000001f);
    }

    public long getOverallTime() {
	return this.endTime - this.startTime;
    }

    public double getOverallTimeInSeconds() {
	return (this.endTime - this.startTime) / 1000000000.0;
    }

    public float getSizeInGb() {
	return this.size / (1024.0f * 1024.0f * 1024.0f);
    }

    public float getSizeInKb() {
	return this.size / (1024.0f);
    }

    public float getSizeInMb() {
	return this.size / (1024.0f * 1024.0f);
    }

    public float getSpaceSaving() {
	return 1 - (((float) this.size) / ((float) this.streamSize));
    }

    public int getSpaceSavingPercentage() {
	return (int) ((1 - (((float) this.size) / ((float) this.streamSize))) * 100);
    }

    public float getStreamMbSec() {
	return getStreamSizeInMb() / (getOverallTime() * 0.000000001f);
    }

    public float getStreamSizeInGb() {
	return this.streamSize / (1024.0f * 1024.0f * 1024.0f);
    }

    public float getStreamSizeInKb() {
	return this.streamSize / (1024.0f);
    }

    public float getStreamSizeInMb() {
	return this.streamSize / (1024.0f * 1024.0f);
    }

    protected String printSize() {

	if (this.size < (1024 * 999)) {
	    return String.format("%7.2fKB", getSizeInKb());
	} else if (this.size < (1024 * 1024 * 999)) {
	    return String.format("%7.2fMB", getSizeInMb());
	} else {
	    return String.format("%7.2fGB", getSizeInGb());
	}
    }

    protected String printStreamSize() {
	if (this.streamSize < (1024 * 999)) {
	    return String.format("%7.2fKB", getStreamSizeInKb());
	} else if (this.size < (1024 * 1024 * 999)) {
	    return String.format("%7.2fMB", getStreamSizeInMb());
	} else {
	    return String.format("%7.2fGB", getStreamSizeInGb());
	}
    }

    public String separetorBar() {
	if (this.compress) {
	    return "_________________________________________________________________________________________";
	} else {
	    return "_________________________________________________________";
	}
    }

    @Override
    public String toString() {
	if (this.compress) {
	    return String.format("(%4d/%4d)   %12d  %9s   %9s  %6.2fs  %5.2fMB/s   %7s  %5.2fMB/s\t%s\t%s", this.index,
		    this.totalChunk, this.block.offset, printSize(), printStreamSize(), getOverallTimeInSeconds(),
		    getMbSec(), getExplicitCompressionRatio(), getStreamMbSec(), this.md5, this.name);
	} else {
	    return String.format("(%4d/%4d)   %12d  %9s\t%5.2fs\t%5.2fMB/s\t%s\t%s", this.index, this.totalChunk,
		    this.block.offset, printSize(), getOverallTimeInSeconds(), getMbSec(), this.md5, this.name);
	}
    }
}
