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

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

public class ConsoleWrapper {
	public static final ConsoleWrapper console = new ConsoleWrapper();
	private BufferedReader br;
	private PrintStream ps;
	private final Console sysConsole;
	private final boolean noConsole;

	public ConsoleWrapper() {
		this.sysConsole = System.console();
		this.noConsole = (this.sysConsole == null);
		if (this.noConsole) {

			this.br = new BufferedReader(new InputStreamReader(System.in));
			this.ps = System.out;
		}

	}

	public void format(final String format, final Object... objects) {
		if (this.noConsole) {
			this.ps.format(format, objects);
		} else {
			this.sysConsole.format(format, objects);
		}
	}

	public Writer getWriter() {
		if (this.noConsole) {
			return new OutputStreamWriter(this.ps);
		} else {
			return this.sysConsole.writer();
		}

	}

	public boolean isNoConsole() {
		return this.noConsole;
	}

	public void printf(final String format, final Object... objects) {
		if (this.noConsole) {
			this.ps.printf(format, objects);
		} else {
			this.sysConsole.printf(format, objects);
		}

	}

	public void println() {
		printf("%n");
	}

	public void println(final String out) {
		printf(out + "%n");
	}

	public String readLine(final String out) {
		if (this.noConsole) {
			this.ps.format(out);
			try {
				return this.br.readLine();
			} catch (final IOException e) {
				return null;
			}
		} else {
			return this.sysConsole.readLine(out);
		}

	}
}
