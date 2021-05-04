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

public enum AdapterProtocolType {
    IDE("ide"), SCSI("scsi"), NVME("nvme"), SATA("sata"), UNKNOWN("Unknow");

    public static AdapterProtocolType getProtocolType(final AdapterType adapter) {
        switch (adapter) {
        case IDE:
            return IDE;
        case BUSLOGIC:
        case LSILOGIC:
        case LSILOGICSAS:
        case PVSCSI:
            return SCSI;
        case NVME:
            return NVME;
        case SATA:
            return SATA;
        default:
            return UNKNOWN;
        }
    }

    private final String protocol;

    AdapterProtocolType(final String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return this.protocol;
    }

    public static AdapterProtocolType parse(final Object mode) {
        return parse(mode.toString());
    }

    public static AdapterProtocolType parse(final String modeStr) {
        switch (modeStr.toLowerCase(Utility.LOCALE)) {
        case "ide":
            return IDE;
        case "scsi":
            return SCSI;
        case "nvme":
            return NVME;
        case "sata":
            return SATA;
        default:
            return UNKNOWN;

        }
    }
}
