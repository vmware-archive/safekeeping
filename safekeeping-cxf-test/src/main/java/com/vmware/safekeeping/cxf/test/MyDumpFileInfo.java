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

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.IBlockInfoProperties;
import com.vmware.safekeeping.common.Utility;
import com.vmware.sapi.BlockInfo;

public class MyDumpFileInfo implements IBlockInfoProperties {

    private BlockInfo dump;

    MyDumpFileInfo() {
    }

    public MyDumpFileInfo(final BlockInfo dump) {
        this.dump = dump;
    }

    @Override
    public long getOverallTime() {
        return this.dump.getEndTime() - this.dump.getStartTime();
    }

    @Override
    public long getSize() {
        return this.dump.getSize();
    }

    @Override
    public long getStreamSize() {
        return this.dump.getStreamSize();
    }

    public boolean isModified() {
        return this.dump.isModified();
    }

    public boolean isCompress() {

        return this.dump.isCompress();
    }

    public boolean isDuplicated() {

        return this.dump.isDuplicated();
    }

    @Override
    public String toString() {
        final String returnString;
        if (Boolean.TRUE.equals(this.dump.isFailed())) {
            final String reason = (StringUtils.isNotEmpty(this.dump.getReason())) ? this.dump.getReason()
                    : "unknow - check the log";
            returnString = String.format(Utility.LOCALE, "(%4d/%4d)  FAILED\t\t%7.2fMB\t\t%5.2fs%n\tReason: %s ",
                    this.dump.getIndex() + 1, this.dump.getTotalBlocks(), getSizeInMb(), getOverallTimeInSeconds(),
                    reason);

        } else {
            final String nominalSpeed;
            final String realSpeed;
            final char openParenthesis;
            final char closeParenthesis;
            if (this.dump.isDuplicated()) {
                nominalSpeed = "          -";
                realSpeed = "          -";
                openParenthesis = '[';
                closeParenthesis = ']';
            } else {
                nominalSpeed = (this.dump.isCompress()) ? String.format(Utility.LOCALE, "%7.2fMB/s", getMbSec())
                        : "            -";
                realSpeed = String.format(Utility.LOCALE, "%7.2fMB/s", getStreamMbSec());
                openParenthesis = '(';
                closeParenthesis = ')';
            }
            final char compress = (this.dump.isCompress()) ? 'x' : ' ';
            final char cipher = (this.dump.isCipher()) ? 'x' : ' ';
            final String compressStreamSize = (this.dump.isCompress()) ? printStreamSize() : "       -";
            final char modified = (this.dump.isModified()) ? '*' : ' ';
            returnString = String.format(Utility.LOCALE,
                    "%c%4d/%4d%c %c %c %c %12d  %12d  %9s   %9s  %6.2fs  %s   %7s  %s\t%s\t%s", openParenthesis,
                    this.dump.getIndex() + 1, this.dump.getTotalBlocks(), closeParenthesis, modified, compress, cipher,
                    this.dump.getOffset(), this.dump.getLastBlock(), printSize(), compressStreamSize,
                    getOverallTimeInSeconds(), nominalSpeed, getExplicitCompressionRatio(), realSpeed,
                    this.dump.getSha1(), this.dump.getMd5());

        }
        return returnString;
    }

}
