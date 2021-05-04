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
package com.vmware.safekeeping.core.control;

import java.io.OutputStream;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.type.enums.EntityType;

public class IoFunction {
	private static IoFunctionInterface ioFunction;

	public static boolean confirmOperation(final String format, final Object... args) {
		return ioFunction.confirmOperation(format, args);
	}

// TODO Remove unused code found by UCDetector
//     public static IoFunctionInterface getIoFunction() {
// 	return ioFunction;
//     }

	public static void print(final char c) {
		ioFunction.print(c);

	}

	public static void print(final float i) {
		ioFunction.print(i);

	}

	/**
	 * @param l
	 */
	public static void print(final Integer i) {
		ioFunction.print(i);

	}

	public static void print(final String string) {
		ioFunction.print(string);
	}

	public static OutputStream printf(final String format, final Object... args) {
		return ioFunction.printf(format, args);
	}

	public static void println() {
		ioFunction.println();
	}

	public static void println(final String string) {
		ioFunction.println(string);

	}

	public static void printTotal(final EntityType entity, final StatisticResult total) {
		IoFunction.printTotal("%s:%d  Success:%d fails:%d skip:%d", entity.toString(true),
				total.getTotalByEntity(entity), total.getSuccess(), total.getFailure(), total.getSkip());
	}

	public static void printTotal(final EntityType[] entities, final StatisticResult total) {
		int found = 0;
		for (final EntityType element : entities) {
			if (total.containsEntity(element)) {
				IoFunction.println();
				IoFunction.printf("Entity %s:%d  Success:%d fails:%d skip:%d", element.toString(true),
						total.getTotalByEntity(element), total.getSuccess(element), total.getFailure(element),
						total.getSkip(element));
				++found;
			}
		}
		if (found > 1) {
			IoFunction.printTotal("Success:%d fails:%d skip:%d", total.getSuccess(), total.getFailure(),
					total.getSkip());
		} else {
			ioFunction.println();
		}
	}

	private static void printTotal(final String format, final Object... args) {
		ioFunction.printTotal(format, args);
		ioFunction.println();
	}

	public static void setFunction(final IoFunctionInterface ioFunction) {
		IoFunction.ioFunction = ioFunction;
	}

	public static String showInfo(final Logger logger, final String format, final Object... args) {
		if (format != null) {
			final String msg = String.format(format, args);
			logger.info(msg);
			ioFunction.showInfo(msg);
			return msg.concat("\n");
		}
		return "";
	}

	public static String showNoValidTargerMessage(final Logger logger) {
		return showInfo(logger, "No Valid target specified.");
	}

	public static String showWarning(final Logger logger, final Exception e) {
		return showWarning(logger, e.getMessage());
	}

	public static String showWarning(final Logger logger, final String format, final Object... args) {
		if (format != null) {
			final String msg = String.format(format, args);
			logger.warning(msg);
			ioFunction.showWarning(msg);
			return msg.concat("\n");
		}
		return "";
	}
}
