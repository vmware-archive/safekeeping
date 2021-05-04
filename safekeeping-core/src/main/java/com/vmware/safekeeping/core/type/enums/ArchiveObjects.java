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

public enum ArchiveObjects {
    GLOBALPROFILE, FCOPROFILE, GENERATIONPROFILE, VMXFILE, REPORTFILE, MD5FILE, VAPPCONFIG, NONE;

    public static ArchiveObjects parse(final Object mode) {
        return parse(mode.toString());
    }

    public static ArchiveObjects parse(final String modeStr) {

        switch (modeStr.toLowerCase(Utility.LOCALE)) {
        case "globalprofile":
        case "global":
            return GLOBALPROFILE;
        case "fcoprofile":
        case "fco":
            return FCOPROFILE;

        case "generationprofile":
        case "generation":
            return GENERATIONPROFILE;

        case "vmxfile":
        case "vmx":
            return VMXFILE;

        case "reportfile":
        case "report":
            return REPORTFILE;

        case "md5file":
        case "md5":
            return MD5FILE;

        case "vappconfig":
        case "vapp":
            return VAPPCONFIG;

        default:
            return NONE;

        }

    }
}
