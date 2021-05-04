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
package com.vmware.safekeeping.core.control.info;

import java.io.Serializable;
import java.util.Collection;

import com.vmware.safekeeping.core.type.enums.EntityType;

public class TotalBlocksInfo extends ExBlockInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5015827933333515267L;
    private final int diskId;
    private EntityType firstClassObjectType;
    private final int numberObject;

    protected TotalBlocksInfo() {
        this.diskId = 0;
        this.numberObject = 0;
    }

    public TotalBlocksInfo(final EntityType firstClassObjectType, final int diskId,
            final Collection<ExBlockInfo> dumpFileInfo, final long startTime, final long endTime) {
        this.firstClassObjectType = firstClassObjectType;
        this.diskId = diskId;
        setStartTime(startTime);
        setEndTime(endTime);
        this.numberObject = dumpFileInfo.size();

        setTotalBlocks(dumpFileInfo.size());

        for (final ExBlockInfo info : dumpFileInfo) {
            if (info != null) {
                setSize(getSize() + info.getSize());
                this.streamSize += info.streamSize;
                setCompress(isCompress() || info.isCompress());
                setCipher(isCipher() || info.isCipher());
            }
        }
    }

    /**
     * @param entityType
     * @param diskId2
     * @param values
     */
    public TotalBlocksInfo(final EntityType firstClassObjectType, final Integer diskId,
            final Collection<ExBlockInfo> values) {
        this.firstClassObjectType = firstClassObjectType;
        this.diskId = diskId;
        this.numberObject = values.size();

        setTotalBlocks(values.size());

        int index = 0;
        for (final ExBlockInfo info : values) {
            if (getStartTime() == 0) {
                setStartTime(info.getStartTime());
                setCompress(isCompress() || info.isCompress());
                setCipher(isCipher() || info.isCipher());
            }
            if (info != null) {
                setSize(getSize() + info.getSize());
                this.streamSize += info.streamSize;
            }
            ++index;
            if ((index == getTotalBlocks()) && (info != null)) {
                setEndTime(info.getEndTime());
            }

        }

    }

    @Override
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
    public String toString() {
        return printSummary();
    }
}
