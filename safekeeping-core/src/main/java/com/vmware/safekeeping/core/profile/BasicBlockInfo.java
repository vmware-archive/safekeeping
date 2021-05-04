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
package com.vmware.safekeeping.core.profile;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;

public class BasicBlockInfo extends SimpleBlockInfo {

    private int generationId;

    private int fileIndex;
    private long lastBlock;

    private boolean compress;

    private boolean cipher;

    private int diskId;
    private final long originalOffset;
    private final long originalLastBlock;

    public BasicBlockInfo() {
        this.originalOffset = 0;
        this.originalLastBlock = 0;

    }

    /**
     * Clone constructor
     *
     * @param sourceBlock
     */
    public BasicBlockInfo(final BasicBlockInfo sourceBlock) {
        super(sourceBlock);
        this.generationId = sourceBlock.generationId;
        this.fileIndex = sourceBlock.fileIndex;

        this.lastBlock = sourceBlock.lastBlock;
        this.cipher = sourceBlock.cipher;
        this.compress = sourceBlock.compress;
        this.diskId = sourceBlock.diskId;

        this.originalOffset = sourceBlock.originalOffset;
        this.originalLastBlock = sourceBlock.originalLastBlock;

    }

    public BasicBlockInfo(final BasicBlockInfo sourceBlock, final int diskId, final int generationId, final int index,
            final int fileIndex, final boolean compress, final boolean cipher) {
        super(sourceBlock);
        setGenerationId(generationId);
        setIndex(index);
        this.fileIndex = fileIndex;
        setCipher(cipher);
        this.compress = compress;
        setDiskId(diskId);

        this.lastBlock = sourceBlock.lastBlock;
        this.originalOffset = this.offset;
        this.originalLastBlock = this.lastBlock;

    }

    public BasicBlockInfo(final Block sourceBlock) {
        this.offset = sourceBlock.offset;
        this.lastBlock = sourceBlock.getLastBlock();
        this.originalOffset = this.offset;
        this.originalLastBlock = this.lastBlock;
        this.cipherOffset = 0;
    }

    public BasicBlockInfo(final Block sourceBlock, final CoreResultActionDiskBackup radb, final int index) {

        this.offset = sourceBlock.offset;
        this.lastBlock = sourceBlock.getLastBlock();
        this.originalOffset = this.offset;
        this.originalLastBlock = this.lastBlock;
        this.cipherOffset = 0;
        this.generationId = radb.getGenerationId();
        this.index = index;
        this.fileIndex = index;
        this.cipher = radb.isCipher();
        this.compress = radb.isCompressed();
        this.diskId = radb.getDiskId();
    }

    public BasicBlockInfo(final long offset, final long length, final byte cipherOffset) {
        this.offset = offset;
        this.lastBlock = (this.offset + length) - 1;
        this.originalOffset = this.offset;
        this.originalLastBlock = this.lastBlock;
        setCipherOffset(cipherOffset);

    }

    public BasicBlockInfo(final SimpleBlockInfo block, final int diskId, final int generationId, final boolean compress,
            final boolean cipher) {
        super(block);
        this.lastBlock = (this.offset + block.getLength()) - 1;
        this.originalOffset = this.offset;
        this.originalLastBlock = this.lastBlock;
        this.generationId = generationId;
        this.fileIndex = block.getIndex();
        this.cipher = cipher;
        this.compress = compress;
        this.diskId = diskId;

    }

    @Override
    public byte getCipherOffset() {
        return this.cipherOffset;
    }

    /**
     * @return the diskId
     */
    public int getDiskId() {
        return this.diskId;
    }

    public int getFileIndex() {
        return this.fileIndex;
    }

    /**
     * @return the generationId
     */
    public int getGenerationId() {
        return this.generationId;
    }

    public long getLastBlock() {
        return this.lastBlock;
    }

    @Override
    public long getLength() {
        return (this.lastBlock - this.offset) + 1;
    }

    public long getOriginalLastBlock() {
        return this.originalLastBlock;
    }

    public long getOriginalLenght() {
        return (this.originalLastBlock - this.originalOffset) + 1;
    }

    public long getOriginalOffset() {
        return this.originalOffset;
    }

    public int getSizeInBytes() {

        return (int) (getLength() * jDiskLibConst.SECTOR_SIZE);
    }

    public int getStreamLength() {
        return (int) getLength() * jDiskLibConst.SECTOR_SIZE;
    }

    public int getStreamOffset() {
        return (int) (this.offset - this.originalOffset) * jDiskLibConst.SECTOR_SIZE;
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

    /**
     * @return
     */
    public boolean isModified() {
        return (this.originalOffset != this.offset) || (this.originalLastBlock != this.lastBlock);
    }

    public void setCipher(final boolean cipher) {
        this.cipher = cipher;
    }

    public void setCompress(final boolean compress) {
        this.compress = compress;
    }

    public void setDiskId(final int diskId) {
        this.diskId = diskId;
    }

    public void setFileIndex(final int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public void setGenerationId(final int generationId) {
        this.generationId = generationId;
    }

    public void setLastBlock(final long lastBlock) {
        this.lastBlock = lastBlock;

    }

    @Override
    public void setLength(final long length) {
        this.lastBlock = (this.offset + length) - 1;
    }

    public void setStartBlock(final long newOffset) {
        this.offset = newOffset;

    }

    @Override
    public String toString() {
        if (isModified()) {
            return String.format("index:%d generation:%d fileIndex:%d offset:%d(%d) last:%d(%d) lenght:[%d-(%d)==%d]) ",
                    getIndex(), getGenerationId(), this.fileIndex, this.offset, this.originalOffset, getLastBlock(),
                    getOriginalLastBlock(), getLength(), getOriginalLenght(), getOriginalLenght() - getLength());
        }
        return String.format("index:%d generation:%d fileIndex:%d offset:%d last:%d lenght:%d", getIndex(),
                getGenerationId(), this.fileIndex, this.offset, getLastBlock(), getLength());
    }
}
