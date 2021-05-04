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

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.common.IBlockInfoProperties;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.BasicBlockInfo;
import com.vmware.safekeeping.core.profile.SimpleBlockInfo;

public class ExBlockInfo extends BasicBlockInfo implements IBlockInfoProperties {

    private long endTime;

    private long size;

    private long startTime;

    protected long streamSize;

    private Boolean failed;

    private String reason;

    private int totalBlocks;

    private final String keyPath;

    private boolean duplicated;

    protected ExBlockInfo() {
        this.keyPath = null;
    }

    /**
     * @param block2
     */
    public ExBlockInfo(final BasicBlockInfo block, final int totalBlocks, final String keyPath) {
        super(block);
        this.totalBlocks = totalBlocks;
        this.size = block.getLength() * jDiskLibConst.SECTOR_SIZE;
        this.keyPath = keyPath;
    }

    public ExBlockInfo(final SimpleBlockInfo value, final int totalBlocks, final String keyPath) {

        setMd5(value.getMd5());
        setCipherOffset(value.getCipherOffset());
        setOffset(value.getOffset());
        setLastBlock((value.getOffset() + value.getLength()) - 1);
        setIndex(value.getIndex());
        setSha1(value.getSha1());
        this.totalBlocks = totalBlocks;
        this.size = value.getLength() * jDiskLibConst.SECTOR_SIZE;
        this.keyPath = keyPath;
    }

    public void failed(final JsonProcessingException e) {
        failed(e.getMessage());

    }

    public void failed(final String reason) {
        this.failed = true;
        this.reason = reason;
    }

    public String getDataKey() {
        if (StringUtils.isEmpty(getSha1())) {
            return StringUtils.EMPTY;
        }
        return this.keyPath.concat(getSha1()).concat("/data");
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return this.endTime;
    }

    public String getJsonKey() {
        return getKey().concat("/json");

    }

    public String getKey() {
        if (StringUtils.isEmpty(getSha1())) {
            return StringUtils.EMPTY;
        }
        return this.keyPath.concat(getSha1());
    }

    public byte[] getMd5Digest() {
        return DatatypeConverter.parseHexBinary(this.md5);
    }

    public String getMd5EncodeBase64() {
        return new String(Base64.encodeBase64(getMd5Digest()), StandardCharsets.UTF_8);
    }

    public long getOverallTime() {
        return getEndTime() - getStartTime();
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return this.size;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return this.startTime;
    }

    public long getStreamSize() {
        return this.streamSize;
    }

    public int getStreamSizeAsInteger() {
        return (int) this.streamSize;
    }

    /**
     * @return the totalBlocks
     */
    public int getTotalBlocks() {
        return this.totalBlocks;
    }

    public boolean isDuplicated() {
        return this.duplicated;
    }

    /**
     * @return the failed
     */
    public Boolean isFailed() {
        return this.failed;
    }

    public String separetorBar() {
        return MessagesTemplate.separatorBar(isCompress());
    }

    public void setDuplicated(final boolean exist) {
        this.duplicated = exist;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

    /**
     * @param failed the failed to set
     */
    public void setFailed(final Boolean failed) {
        this.failed = failed;
    }

    public void setMd5Digest(final byte[] digest) {
        if (digest != null) {
            this.md5 = DatatypeConverter.printHexBinary(digest);
        }
    }

    public void setReason(final Exception reason) {
        this.reason = reason.getMessage();
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    /**
     * @param streamSize the streamSize to set
     */
    public void setStreamSize(final long streamSize) {
        this.streamSize = streamSize;
    }

    /**
     * @param totalBlocks the totalBlocks to set
     */
    public void setTotalBlocks(final int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public SimpleBlockInfo toSimpleBlockInfo() {
        final SimpleBlockInfo block = new SimpleBlockInfo(getIndex());
        block.setCipherOffset(getCipherOffset());
        block.setOffset(getOffset());
        block.setLength(getLength());
        block.setMd5(getMd5());
        block.setSha1(getSha1());
        return block;
    }

}
