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
package com.vmware.safekeeping.cmd.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.jline.terminal.Terminal;

import com.vmware.safekeeping.core.control.IoFunctionInterface;

public class IoFunctionImpl implements IoFunctionInterface {
	private boolean interactive;
	private Terminal terminal;

	public IoFunctionImpl() {
		this.interactive = false;
		this.terminal = null;
	}

	@Override
	public boolean confirmOperation(final String format, final Object... args) {
		if (this.interactive) {
			final String msg = String.format(format + "\nConfirm the operation (y/n)? ", args);

			BufferedReader br = null;
			final Reader r = new InputStreamReader(System.in);
			br = new BufferedReader(r);
			if (this.terminal != null) {
				this.terminal.writer().print(msg);
			} else {
				System.out.print(msg);
			}
			String str = null;

			do {
				try {
					str = br.readLine();
				} catch (final IOException e) {
					return false;
				}
				if (!str.isEmpty()) {
					if ((str.compareToIgnoreCase("yes") == 0) || (str.compareToIgnoreCase("y") == 0)) {

						return true;
					}
				}
			} while (!str.equalsIgnoreCase("no") && !str.equalsIgnoreCase("n"));
			System.out.println();
			return false;
		}
		return true;
	}

	public Terminal getTerminal() {
		return this.terminal;
	}

	public boolean isInteractive() {
		return this.interactive;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vmware.vmbk.control.IoFunctionInterface#print(char)
	 */
	@Override
	public void print(final char c) {
		System.out.print(c);
	}

	@Override
	public void print(final float f) {
		System.out.print(f);
	}

	@Override
	public void print(final Integer i) {
		System.out.print(i);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vmware.vmbk.control.IoFunctionInterface#print(java.lang.String)
	 */
	@Override
	public void print(final String string) {
		System.out.print(string);
	}

	@Override
	public OutputStream printf(final String format, final Object... args) {
		return System.out.printf(format, args);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vmware.vmbk.control.IoFunctionInterface#println()
	 */
	@Override
	public void println() {
		System.out.println();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vmware.vmbk.control.IoFunctionInterface#println(java.lang.String)
	 */
	@Override
	public void println(final String string) {
		System.out.println(string);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vmware.vmbk.control.IoFunctionInterface#printTotal(java.lang.String,
	 * java.lang.Object[])
	 */
	@Override
	public void printTotal(final String format, final Object[] args) {
		System.out.println();
		System.out.print("Total:");
		System.out.printf(format, args);
		System.out.println();

	}

	public void setInteractive(final boolean interactive) {
		this.interactive = interactive;
	}

	public void setTerminal(final Terminal terminal) {
		this.terminal = terminal;
	}

	@Override
	public void showInfo(final String msg) {

		if (this.terminal != null) {
			this.terminal.writer().println(msg);
		} else {
			System.out.println(msg);
		}

	}

	@Override
	public void showWarning(final String msg) {

		if (this.terminal != null) {
			this.terminal.writer().println(msg);
		} else {
			System.err.println(msg);
		}

	}
}
