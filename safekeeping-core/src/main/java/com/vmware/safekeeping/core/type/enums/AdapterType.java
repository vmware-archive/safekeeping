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
import com.vmware.vim25.ParaVirtualSCSIController;
import com.vmware.vim25.VirtualBusLogicController;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualLsiLogicSASController;
import com.vmware.vim25.VirtualNVMEController;
import com.vmware.vim25.VirtualSATAController;

/**
 * VM Controller types available on ESX
 *
 * @author mdaneri
 *
 */
public enum AdapterType {
    /**
     * BusLogic controller
     */
    BUSLOGIC("buslogic"), IDE("ide"), LSILOGIC("lsilogic"), LSILOGICSAS("lsilogicsas"), NVME("nvme"),
    PVSCSI("paravirtual"), SATA("sata"), UNKNOWN("Unknow");

    /**
     *
     * @param vd
     * @return @see {@link AdapterType }
     */
    public static AdapterType getAdapterType(final VirtualDevice vd) {

        final AdapterType ret;
        if (vd instanceof VirtualIDEController) {
            ret = AdapterType.IDE;
        } else if (vd instanceof VirtualBusLogicController) {
            ret = AdapterType.BUSLOGIC;
        } else if (vd instanceof VirtualLsiLogicController) {
            ret = AdapterType.LSILOGIC;
        } else if (vd instanceof VirtualLsiLogicSASController) {
            ret = AdapterType.LSILOGICSAS;
        } else if (vd instanceof ParaVirtualSCSIController) {
            ret = AdapterType.PVSCSI;
        } else if (vd instanceof VirtualSATAController) {
            ret = AdapterType.SATA;
        } else if (vd instanceof VirtualNVMEController) {
            ret = AdapterType.NVME;
        } else {
            ret = AdapterType.UNKNOWN;
        }

        return ret;
    }

    public static AdapterType parse(final Object mode) {
        return parse(mode.toString());
    }

    public static AdapterType parse(final String adaptType) {
        switch (adaptType.toLowerCase(Utility.LOCALE)) {
        case "ide":
            return IDE;
        case "buslogic":
            return BUSLOGIC;
        case "lsilogic":
            return LSILOGIC;
        case "lsilogicsas":
            return LSILOGICSAS;
        case "paravirtual":
            return PVSCSI;
        case "nvme":
            return NVME;
        case "sata":
            return SATA;
        default:
            return UNKNOWN;
        }
    }

    private final String name;

    AdapterType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
