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
package com.vmware.vmbk.control;

import java.io.OutputStream;
import java.util.logging.Logger;

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

    public static void printTotal(final String format, final Object... args) {
	ioFunction.printTotal(format, args);
    }

    public static void setFunction(final IoFunctionInterface ioFunction) {
	IoFunction.ioFunction = ioFunction;
    }

    public static String showInfo(final Logger logger, final String format, final Object... args) {
	final String msg = String.format(format, args);
	logger.info(msg);
	ioFunction.showInfo(msg);
	return msg.concat("\n");
    }

    public static String showWarning(final Logger logger, final String format, final Object... args) {
	final String msg = String.format(format, args);
	logger.warning(msg);
	ioFunction.showWarning(msg);
	return msg.concat("\n");
    }
}
