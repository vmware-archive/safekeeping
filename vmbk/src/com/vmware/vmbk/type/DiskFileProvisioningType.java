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

public enum DiskFileProvisioningType {
    EAGER_ZEROED_THICK, LAZY_ZEROED_THICK, THIN;

    public static DiskFileProvisioningType parse(final Object mode) {
	return parse(mode.toString());
    }

    public static DiskFileProvisioningType parse(final String modeStr) {
	if (modeStr.equals("thick") || modeStr.equals("t") || modeStr.equals("EAGER_ZEROED_THICK")) {
	    return EAGER_ZEROED_THICK;
	} else if (modeStr.equals("zero") || modeStr.equals("zerothick") || modeStr.equals("z")
		|| modeStr.equals("LAZY_ZEROED_THICK")) {
	    return LAZY_ZEROED_THICK;
	} else if (modeStr.equals("thin") || modeStr.equals("h") || modeStr.equals("THIN")) {
	    return THIN;
	} else {
	    return null;
	}
    }

    @Override
    public String toString() {
	String ret = null;
	switch (this) {
	case EAGER_ZEROED_THICK:
	    ret = "eagerZeroedThick";
	    break;
	case LAZY_ZEROED_THICK:
	    ret = "lazyZeroedThick";
	    break;
	case THIN:
	    ret = "thin";
	    break;
	default:
	    break;
	}
	return ret;
    }
}
