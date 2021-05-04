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
package com.vmware.safekeeping.core.type.enums;

import com.vmware.safekeeping.common.Utility;

public enum BackupMode {
    FULL, INCREMENTAL, MIXED, UNKNOW;

    public static BackupMode parse(final Object mode) {
        return parse(mode.toString());
    }

    public static BackupMode parse(final String modeStr) {
        switch (modeStr.toLowerCase(Utility.LOCALE)) {
        case "full":
        case "f":
            return FULL;
        case "incr":
        case "inc":
        case "incremental":
        case "i":
            return INCREMENTAL;
        default:
            return UNKNOW;
        }
    }

    @Override
    public String toString() {

        String ret = "unknown";
        switch (this) {
        case FULL:
            ret = "full";
            break;
        case INCREMENTAL:
            ret = "incr";
            break;
        case UNKNOW:
            ret = "unknow";
            break;
        case MIXED:
            ret = "mixed";
            break;
        default:
            break;
        }
        return ret;
    }

}
