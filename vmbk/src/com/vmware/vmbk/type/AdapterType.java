/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.type;

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

    public static AdapterType parse(final String adaptType) {

	switch (adaptType.toLowerCase()) {
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

    private AdapterType(final String name) {
	this.name = name;
    }

    @Override
    public String toString() {
	return this.name;
    }

    public String toTypeString() {
	switch (this) {
	case IDE:
	    return "ide";
	case BUSLOGIC:
	case LSILOGIC:
	case LSILOGICSAS:
	case PVSCSI:
	    return "scsi";
	case NVME:
	    return "nvme";
	case SATA:
	    return "sata";
	default:
	    return "unknown";
	}
    }
}
