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

public class SimpleBlockInfo {
    protected String md5;
    protected String sha1;
    protected byte cipherOffset;

    protected long offset;

    private long length;

    protected int index;

    public SimpleBlockInfo() {
    }

    public SimpleBlockInfo(final int index) {
        this.index = index;
    }

    public SimpleBlockInfo(final SimpleBlockInfo sourceBlock) {
        this.index = sourceBlock.index;
        this.offset = sourceBlock.offset;
        this.cipherOffset = sourceBlock.cipherOffset;
        this.md5 = sourceBlock.md5;
        this.sha1 = sourceBlock.sha1;
    }

    public byte getCipherOffset() {
        return this.cipherOffset;
    }

    public int getIndex() {
        return this.index;
    }

    public long getLength() {
        return this.length;
    }

    public String getMd5() {
        return this.md5;
    }

    public long getOffset() {
        return this.offset;
    }

    public String getSha1() {
        return this.sha1;
    }

    public void setCipherOffset(final byte cipherOffset) {
        this.cipherOffset = cipherOffset;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public void setMd5(final String md5) {
        this.md5 = md5;
    }

    public void setOffset(final long offset) {
        this.offset = offset;
    }

    public void setSha1(final String sha1) {
        this.sha1 = sha1;
    }

}
