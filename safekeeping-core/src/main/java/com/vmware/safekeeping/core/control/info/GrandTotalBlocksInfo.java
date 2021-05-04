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

import java.util.List;

import com.vmware.safekeeping.common.Utility;

public class GrandTotalBlocksInfo extends TotalBlocksInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 642108732669023679L;
    private final int fails;
    private final int numberIvd;
    private final int numberObject;
    private final int numberVapp;
    private final int numberVm;
    private final int skips;
    private final int success;

    public GrandTotalBlocksInfo(final int success, final int fails, final int skips, final int numberVm,
            final int numberIvd, final int numberVapp, final List<TotalBlocksInfo> totalDumpsInfo) {
        this.numberIvd = numberIvd;
        this.numberVapp = numberVapp;
        this.numberVm = numberVm;
        this.success = success;
        this.skips = skips;
        this.fails = fails;
        this.numberObject = totalDumpsInfo.size();
        for (final TotalBlocksInfo info : totalDumpsInfo) {
            if ((info.getStartTime() < getStartTime()) || (getStartTime() == 0)) {
                setStartTime(info.getStartTime());
            }
            if ((info.getEndTime() > getEndTime()) || (getEndTime() == 0)) {
                setEndTime(info.getEndTime());
            }
            setSize(getSize() + info.getSize());
            this.streamSize += info.streamSize;
            setCompress(isCompress() || info.isCompress());
            setCipher(isCipher() || info.isCipher());

        }

    }

    @Override
    public String separetorBar() {
        return ("__________________________________________________________________________________________________________________________");
    }

    @Override
    public String toString() {
        return String.format(Utility.LOCALE, "Total\n_______________________________%n" + "%d\t%d\t%d\t%d%n",
                this.success + this.fails + this.skips, this.success, this.fails, this.skips)
                + "SUMMARY:\nvm\tivd\tvapp\tdisks\t\tsize\tcompress   time    n-speed   c-ratio    r-speed%n"
                + String.format(Utility.LOCALE, "%d\t%d\t%d\t%d\t  %10s  %10s%6.2fs  %5.2fMB/s   %7s  %5.2fMB/s%n",
                        this.numberVm, this.numberIvd, this.numberVapp, this.numberObject, printSize(),
                        printStreamSize(), getOverallTimeInSeconds(), getMbSec(), getExplicitCompressionRatio(),
                        getStreamMbSec());

    }
}
