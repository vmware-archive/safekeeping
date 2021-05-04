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
package com.vmware.safekeeping.common;

public interface IBlockInfoProperties {

    float ONE_KB = 1024.0F;
    float NANO_TO_SEC_MULTIPLIER = 0.000000001F;

    float ONE_HUNDRED_PER_CENT = 100F;

    default float getCompressionRatio() {
        return ((float) getSize()) / ((float) getStreamSize());
    }

    boolean isCompress();

    boolean isDuplicated();

    default String getExplicitCompressionRatio() {
        if (isCompress() && !isDuplicated()) {
            return String.format(Utility.LOCALE, "%4.1f:1", ((float) getSize()) / ((float) getStreamSize()));
        } else {
            return "      -";
        }
    }

    default float getMbSec() {
        return getSizeInMb() / (getOverallTime() * NANO_TO_SEC_MULTIPLIER);
    }

    long getOverallTime();

    default double getOverallTimeInSeconds() {
        return getOverallTime() * NANO_TO_SEC_MULTIPLIER;
    }

    long getSize();

    default float getSizeInGb() {
        return getSize() / (ONE_KB * ONE_KB * ONE_KB);
    }

    default float getSizeInKb() {
        return getSize() / (ONE_KB);
    }

    default float getSizeInMb() {
        return getSize() / (ONE_KB * ONE_KB);
    }

    default float getSpaceSaving() {
        return 1 - (((float) getSize()) / ((float) getStreamSize()));
    }

    default int getSpaceSavingPercentage() {
        return (int) ((1 - (((float) getSize()) / ((float) getStreamSize()))) * ONE_HUNDRED_PER_CENT);
    }

    default float getStreamMbSec() {
        return getStreamSizeInMb() / (getOverallTime() * NANO_TO_SEC_MULTIPLIER);
    }

    default float getStreamByteSec() {
        return this.getStreamSize() / (getOverallTime() * NANO_TO_SEC_MULTIPLIER);
    }

    long getStreamSize();

    default float getStreamSizeInGb() {
        return getStreamSize() / (ONE_KB * ONE_KB * ONE_KB);
    }

    default float getStreamSizeInKb() {
        return getStreamSize() / (ONE_KB);
    }

    default float getStreamSizeInMb() {
        return getStreamSize() / (ONE_KB * ONE_KB);
    }

    default String printSize() {

        if (getSize() < (ONE_KB * ONE_KB)) {
            return String.format(Utility.LOCALE, "%7.2fKB", getSizeInKb());
        } else if (getSize() < (ONE_KB * ONE_KB * ONE_KB)) {
            return String.format(Utility.LOCALE, "%7.2fMB", getSizeInMb());
        } else {
            return String.format(Utility.LOCALE, "%7.2fGB", getSizeInGb());
        }
    }

    default String printStreamSize() {
        if (getStreamSize() < (ONE_KB * ONE_KB)) {
            return String.format(Utility.LOCALE, "%7.2fKB", getStreamSizeInKb());
        } else if (getSize() < (ONE_KB * ONE_KB * ONE_KB)) {
            return String.format(Utility.LOCALE, "%7.2fMB", getStreamSizeInMb());
        } else {
            return String.format(Utility.LOCALE, "%7.2fGB", getStreamSizeInGb());
        }
    }

    default String printSummary() {
        if (isCompress()) {
            return String.format(Utility.LOCALE,
                    "Summary                                      %10s  %10s  %6.2fs  %5.2fMB/s   %7s  %5.2fMB/s",
                    printSize(), printStreamSize(), getOverallTimeInSeconds(), getMbSec(),
                    getExplicitCompressionRatio(), getStreamMbSec());
        } else {
            return String.format(Utility.LOCALE,
                    "Summary                                      %10s              %6.2fs  %5.2fMB/s", printSize(),
                    getOverallTimeInSeconds(), getMbSec());
        }
    }
}
