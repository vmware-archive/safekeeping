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

import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class PrettyNumber {

	public enum MetricPrefix {
		NONE, KILO, MEGA, GIGA, TERA, PETA, AUTO
	}

	private static final String KILO = "K";

	private static final String MEGA = "M";
	private static final String GIGA = "G";
	private static final String TERA = "T";
	private static final String PETA = "P";

	private static final long NONEWEIGHT = 1L;
	private static final long KILOWEIGHT = 1024L;
	private static final long MEGAWEIGHT = 1024L * 1024L;
	private static final long GIGAWEIGHT = 1024L * 1024L * 1024L;
	private static final long TERAWEIGHT = 1024L * 1024L * 1024L * 1024L;
	private static final long PETAWEIGHT = 1024L * 1024L * 1024L * 1024L * 1024L;

	private static final String[] unitArray = new String[] { KILO, MEGA, GIGA, TERA, PETA };

	public static final Pattern pattern = compile("(^(?!\\d+$)[KMGTPB0-9]+$|^(\\d+$))$");

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

	public static Long toGigaByte(final Long num) {
		if (num != null) {
			return num / GIGAWEIGHT;
		}
		return null;
	}

	public static int toInteger(final String s) {
		final Number num = toNumber(s);
		if (num instanceof Integer) {
			return num.intValue();
		} else {
			throw new NumberFormatException("For input string: \"" + s + "\"");
		}
	}

	public static Long toKiloByte(final Long num) {
		if (num != null) {
			return num / KILOWEIGHT;
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
			return num / MEGAWEIGHT;
		}
		return null;
	}

	private static Number toNumber(final String s) {
		String number = null;
		Integer valueInt = null;
		Long valueLong = null;
		Long unit = 1L;
		final String numberWithUnit = StringUtils.endsWithIgnoreCase(s, "b") ? StringUtils.left(s, s.length() - 1) : s;

		if (StringUtils.endsWithAny(numberWithUnit, unitArray)) {
			number = StringUtils.left(numberWithUnit, numberWithUnit.length() - 1);
			final String unitString = numberWithUnit.substring(numberWithUnit.length() - 1);
			switch (unitString) {
			case KILO:
				unit = KILOWEIGHT;
				break;
			case MEGA:
				unit = MEGAWEIGHT;
				break;
			case GIGA:
				unit = GIGAWEIGHT;
				break;
			case TERA:
				unit = TERAWEIGHT;
				break;
			case PETA:
				unit = PETAWEIGHT;
				break;
			default:
				unit = NONEWEIGHT;
				break;

			}
		} else {
			number = numberWithUnit;
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

	public static Long toPetaByte(final Long num) {
		if (num != null) {
			return num / PETAWEIGHT;
		}
		return null;
	}

	public static String toString(final Number b) {
		if (b == null) {
			return "";
		}
		return b.toString();
	}

	public static String toString(final Number num, final MetricPrefix unit) {
		return toString(num, unit, 0);
	}

	public static String toString(final Number num, MetricPrefix unit, final Integer decimal) {
		Float result = null;
		if (unit == MetricPrefix.AUTO) {
			if (num.longValue() < PETAWEIGHT) {
				unit = MetricPrefix.TERA;
			}
			if (num.longValue() < TERAWEIGHT) {
				unit = MetricPrefix.GIGA;
			}
			if (num.longValue() < GIGAWEIGHT) {
				unit = MetricPrefix.MEGA;
			}
			if (num.longValue() < MEGAWEIGHT) {
				unit = MetricPrefix.KILO;
			}
			if (num.longValue() < KILOWEIGHT) {
				unit = MetricPrefix.NONE;
			}
		}

		final String format = "%." + decimal.toString() + "f%s";

		switch (unit) {
		case NONE:
			return String.format("%d%s", num.longValue(), "");
		case KILO:
			result = (num.floatValue() / KILOWEIGHT);
			return String.format(format, result, KILO);
		case MEGA:
			result = (num.floatValue() / MEGAWEIGHT);
			return String.format(format, result, MEGA);
		case GIGA:
			result = (num.floatValue() / GIGAWEIGHT);
			return String.format(format, result, GIGA);
		case TERA:
			result = (num.floatValue() / TERAWEIGHT);
			return String.format(format, result, TERA);
		case PETA:
		case AUTO:
			result = (num.floatValue() / PETAWEIGHT);
			return String.format(format, result, PETA);
		}
		return num.toString();

	}

	public static Long toTeraByte(final Long num) {
		if (num != null) {
			return num / TERAWEIGHT;
		}
		return null;
	}

}
