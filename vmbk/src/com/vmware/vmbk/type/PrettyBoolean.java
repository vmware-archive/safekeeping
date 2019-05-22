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

public class PrettyBoolean {
    public enum BooleanEnableDisableType {
	enable, disable
    }

    public enum BooleanOnOffType {
	on, off
    }

    public enum BooleanType {
	standard, enableDisable, onOff, yesNo
    }

    public enum BooleanYesNoType {
	yes, no
    }

    public enum PrettyBooleanValues {
	enable, on, yes, no, off, disable
    }

// TODO Remove unused code found by UCDetector
//     public static int compare(final boolean x, final boolean y) {
// 	return (x == y) ? 0 : (x ? 1 : -1);
//     }

// TODO Remove unused code found by UCDetector
//     public static boolean isBool(final String val) {
// 	switch (val) {
// 	case "true":
// 	case "1":
// 	case "on":
// 	case "enable":
// 	    return true;
//
// 	case "false":
// 	case "0":
// 	case "off":
// 	case "disable":
// 	    return false;
// 	default:
// 	    return false;
// 	}
//
//     }

    public static boolean parseBoolean(final Object val) {
	if (val instanceof PrettyBooleanValues) {
	    return parseBoolean((PrettyBooleanValues) val);
	}
	return parseBoolean(val.toString());
    }

    private static boolean parseBoolean(final PrettyBooleanValues val) {
	switch (val) {
	case disable:
	case no:
	case off:
	    return false;
	case on:
	case enable:
	case yes:
	    return true;
	default:
	    return false;
	}
    }

    public static boolean parseBoolean(final String val) {
	switch (val.toLowerCase()) {
	case "true":
	case "1":
	case "on":
	case "enable":
	case "yes":
	    return true;

	case "false":
	case "0":
	case "off":
	case "disable":
	case "no":
	    return false;
	default:
	    return false;
	}
    }

    public static String toString(final Boolean b) {
	if (b == null) {
	    return "NA";
	}
	return b ? "true" : "false";
    }

    public static String toString(final Boolean b, final BooleanType bType) {
	if (b == null) {
	    return "NA";
	}

	if (b) {
	    switch (bType) {
	    case enableDisable:
		return "enable";

	    case onOff:
		return "on";
	    case standard:
		return "true";

	    case yesNo:
		return "yes";

	    }
	} else {
	    switch (bType) {
	    case enableDisable:
		return "disable";

	    case onOff:
		return "off";
	    case standard:
		return "false";

	    case yesNo:
		return "no";

	    }

	}
	return b.toString();
    }

}
