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

import org.apache.commons.lang.StringUtils;

public class PrettyNumber {

    public enum MetricPrefix {
	none, kilo, mega, giga, tera, peta
    }

    private final static String kilo = "k";

    private final static String mega = "M";
    private final static String giga = "G";
    private final static String tera = "T";
    private final static String peta = "P";

    private final static long noneWeight = 1L;
    private final static long kiloWeight = 1024L;
    private final static long megaWeight = 1024L * 1024L;
    private final static long gigaWeight = 1024L * 1024L * 1024L;
    private final static long teraWeight = 1024L * 1024L * 1024L * 1024L;
    private final static long petaWeight = 1024L * 1024L * 1024L * 1024L * 1024L;

    private final static String[] unitArray = new String[] { kilo, mega, giga, tera, peta };

    public final static String regexStr = "(^(?!\\d+$)[kMGTP0-9]+$|^(\\d+$))$";

    private static boolean isNumber(final String val) {
	String number = null;
	if (StringUtils.endsWithAny(val, unitArray)) {
	    number = StringUtils.left(val, val.length() - 1);
	} else {
	    number = val;
	}
	if ((val.charAt(0) == '-') || (val.charAt(0) == '1')) {
	    number = StringUtils.substring(number, 1);
	}
	return StringUtils.isNumeric(number);
    }

// TODO Remove unused code found by UCDetector
//     public static Long toGigaByte(final Long num) {
// 	if (num != null) {
// 	    return num / gigaWeight;
// 	}
// 	return null;
//     }

// TODO Remove unused code found by UCDetector
//     public static long toInteger(final Object s) {
// 	return toInteger(s.toString());
//     }

    public static int toInteger(final String s) {
	final Number num = toNumber(s);
	if (num instanceof Integer) {
	    return num.intValue();
	} else {
	    throw new NumberFormatException("For input string: \"" + s + "\"");
	}
    }

    static Long toKiloByte(final Long num) {
	if (num != null) {
	    return num / kiloWeight;
	}
	return null;
    }

    public static Long toLong(final Object s) {
	return toLong(s.toString());
    }

    public static Long toLong(final String s) {
	if (StringUtils.isEmpty(s)) {
	    return null;
	}
	final Number num = toNumber(s);
	if ((num instanceof Long) || (num instanceof Integer)) {
	    return num.longValue();
	} else {
	    throw new NumberFormatException("For input string: \"" + s + "\"");
	}
    }

    public static Long toMegaByte(final Long num) {
	if (num != null) {
	    return num / megaWeight;
	}
	return null;
    }

    private static Number toNumber(final String s) {
	String number = null;
	Integer valueInt = null;
	Long valueLong = null;
	Long unit = 1L;

	if (StringUtils.endsWithAny(s, unitArray)) {
	    number = StringUtils.left(s, s.length() - 1);
	    final String unitString = s.substring(s.length() - 1);
	    switch (unitString) {
	    case kilo:
		unit = kiloWeight;
		break;
	    case mega:
		unit = megaWeight;
		break;
	    case giga:
		unit = gigaWeight;
		break;
	    case tera:
		unit = teraWeight;
		break;
	    case peta:
		unit = petaWeight;
		break;
	    default:
		unit = noneWeight;
		break;

	    }
	} else {
	    number = s;
	}
	if (number.length() == 0) {
	    throw new NumberFormatException("Number " + s + " not in valid format - Missing value");
	}
	if (number.charAt(0) == '+') {
	    number = StringUtils.substring(number, 1);
	}
	if (isNumber(number)) {
	    valueLong = Long.parseLong(number);
	    valueLong *= unit;
	    if ((valueLong > Integer.MIN_VALUE) && (valueLong < Integer.MAX_VALUE)) {
		valueInt = valueLong.intValue();
	    }
	}
	if (valueInt != null) {
	    return valueInt;
	} else if (valueLong != null) {
	    return valueLong;
	} else {
	    throw new NumberFormatException("For input string: \"" + s + "\"");
	}
    }

// TODO Remove unused code found by UCDetector
//     public static Long toPetaByte(final Long num) {
// 	if (num != null) {
// 	    return num / petaWeight;
// 	}
// 	return null;
//     }

    public static String toString(final Number b) {
	if (b == null) {
	    return "";
	}
	return b.toString();
    }

    public static String toString(final Number num, final MetricPrefix unit) {
	Long result = null;
	switch (unit) {
	case none:
	    result = num.longValue() / noneWeight;
	    return result.toString();
	case kilo:
	    result = (num.longValue() / kiloWeight);
	    return result.toString().concat(kilo);
	case mega:
	    result = (num.longValue() / megaWeight);
	    return result.toString().concat(mega);
	case giga:
	    result = (num.longValue() / gigaWeight);
	    return result.toString().concat(giga);
	case tera:
	    result = (num.longValue() / teraWeight);
	    return result.toString().concat(tera);
	case peta:
	    result = (num.longValue() / petaWeight);
	    return result.toString().concat(peta);
	}
	return num.toString();

    }

// TODO Remove unused code found by UCDetector
//     public static Long toTeraByte(final Long num) {
// 	if (num != null) {
// 	    return num / teraWeight;
// 	}
// 	return null;
//     }

}
