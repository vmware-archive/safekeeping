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
package com.vmware.safekeeping.external.type;

import com.vmware.safekeeping.core.control.info.ExBlockInfo;

public class BlockInfo {

    public static BlockInfo newInstance(final ExBlockInfo src) {
        final BlockInfo dst = new BlockInfo();
        dst.setGenerationId(src.getGenerationId());
        dst.setOffset(src.getOffset());
        dst.setLength(src.getLength());
        dst.setIndex(src.getIndex());
        dst.setCompress(src.isCompress());
        dst.setCipher(src.isCipher());
        dst.setDiskId(src.getDiskId());
        dst.setSize(src.getSize());
        dst.setEndTime(src.getEndTime());
        dst.setMd5(src.getMd5());
        dst.setKey(src.getKey());
        dst.setStartTime(src.getStartTime());
        dst.setStreamSize(src.getStreamSize());
        dst.setFailed(src.isFailed());
        dst.setReason(src.getReason());
        dst.setTotalBlocks(src.getTotalBlocks());
        dst.setModified(src.isModified());
        dst.lastBlock = src.getLastBlock();
        dst.sha1 = src.getSha1();
        dst.duplicated = src.isDuplicated();
        return dst;
    }

    private boolean duplicated;

    private String sha1;

    private long lastBlock;

    private long endTime;

    private String md5;

    private String key;

    private String keyPath;
    private long size;
    private long startTime;

    private long streamSize;

    private Boolean failed;

    private String reason;

    private int totalBlocks;

    private int generationId;

    private long offset;

    private long length;

    private int index;

    private boolean compress;

    private boolean cipher;

    private int diskId;

    private boolean modified;

    /**
     * @return the diskId
     */
    public int getDiskId() {
        return this.diskId;
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return this.endTime;
    }

    /**
     * @return the failed
     */
    public Boolean getFailed() {
        return this.failed;
    }

    /**
     * @return the generationId
     */
    public int getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @return the keyPath
     */
    public String getKeyPath() {
        return this.keyPath;
    }

    public long getLastBlock() {
        return this.lastBlock;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return this.length;
    }

    /**
     * @return the md5
     */
    public String getMd5() {
        return this.md5;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return this.offset;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return this.reason;
    }

    public String getSha1() {
        return this.sha1;
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

    /**
     * @return the streamSize
     */
    public long getStreamSize() {
        return this.streamSize;
    }

    /**
     * @return the totalBlocks
     */
    public int getTotalBlocks() {
        return this.totalBlocks;
    }

    /**
     * @return the cipher
     */
    public boolean isCipher() {
        return this.cipher;
    }

    /**
     * @return the compress
     */
    public boolean isCompress() {
        return this.compress;
    }

    public boolean isDuplicated() {
        return this.duplicated;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return this.modified;
    }

    /**
     * @param cipher the cipher to set
     */
    public void setCipher(final boolean cipher) {
        this.cipher = cipher;
    }

    /**
     * @param compress the compress to set
     */
    public void setCompress(final boolean compress) {
        this.compress = compress;
    }

    /**
     * @param diskId the diskId to set
     */
    public void setDiskId(final int diskId) {
        this.diskId = diskId;
    }

    public void setDuplicated(final boolean duplicated) {
        this.duplicated = duplicated;
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

    /**
     * @param generationId the generationId to set
     */
    public void setGenerationId(final int generationId) {
        this.generationId = generationId;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(final int index) {
        this.index = index;
    }

    /**
     * @param key the key to set
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @param keyPath the keyPath to set
     */
    public void setKeyPath(final String keyPath) {
        this.keyPath = keyPath;
    }

    public void setLastBlock(final long lastBlock) {
        this.lastBlock = lastBlock;
    }

    /**
     * @param length the length to set
     */
    public void setLength(final long length) {
        this.length = length;
    }

    /**
     * @param md5 the md5 to set
     */
    public void setMd5(final String md5) {
        this.md5 = md5;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(final boolean modified) {
        this.modified = modified;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(final long offset) {
        this.offset = offset;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(final String reason) {
        this.reason = reason;
    }

    public void setSha1(final String sha1) {
        this.sha1 = sha1;
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

}
