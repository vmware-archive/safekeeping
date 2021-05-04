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
package com.vmware.safekeeping.cxf.test;

import java.util.Collection;

import com.vmware.safekeeping.common.IBlockInfoProperties;
import com.vmware.sapi.BlockInfo;
import com.vmware.sapi.EntityType;

public class TotalDumpFileInfo implements IBlockInfoProperties {

    private final int diskId;
    private EntityType firstClassObjectType;
    protected int numberObject;
    private long startTime;
    private long endTime;
    private boolean compress;
    private int totalChunk;
    private long size;

    private long streamSize;

    protected TotalDumpFileInfo() {
        this.diskId = 0;
        this.numberObject = 0;
    }

    public TotalDumpFileInfo(final EntityType firstClassObjectType, final int diskId, final BlockInfo[] dumpFileInfo,
            final long startTime, final long endTime) {
        this.firstClassObjectType = firstClassObjectType;

        this.diskId = diskId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberObject = dumpFileInfo.length;
        if (dumpFileInfo[0] != null) {
            this.compress = dumpFileInfo[0].isCompress();
        }
        this.totalChunk = dumpFileInfo.length;

        for (final BlockInfo info : dumpFileInfo) {
            if (info != null) {
                this.size += info.getSize();
                this.streamSize += info.getStreamSize();
            }
        }
    }

    /**
     * @param entityType
     * @param diskId2
     * @param values
     */
    public TotalDumpFileInfo(final EntityType firstClassObjectType, final Integer diskId,
            final Collection<BlockInfo> values) {
        this.firstClassObjectType = firstClassObjectType;

        this.diskId = diskId;
        this.numberObject = values.size();

        this.totalChunk = values.size();

        int index = 0;
        for (final BlockInfo info : values) {
            if (this.startTime == 0) {
                this.startTime = info.getStartTime();
                this.compress = info.isCompress();
            }
            if (info != null) {
                this.size += info.getSize();
                this.streamSize += info.getStreamSize();
            }
            ++index;
            if ((index == this.totalChunk) && (info != null)) {
                this.endTime = info.getEndTime();
            }

        }

    }

    public int getDiskId() {
        return this.diskId;
    }

    public EntityType getFirstClassObjectType() {
        return this.firstClassObjectType;
    }

    public int getNumberObject() {
        return this.numberObject;
    }

    @Override
    public long getOverallTime() {
        return this.endTime - this.startTime;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public long getStreamSize() {
        return this.streamSize;
    }

    @Override
    public boolean isCompress() {
        return this.compress;
    }

    @Override
    public boolean isDuplicated() {
        return false;
    }

    @Override
    public String toString() {
        return printSummary();
    }

}
