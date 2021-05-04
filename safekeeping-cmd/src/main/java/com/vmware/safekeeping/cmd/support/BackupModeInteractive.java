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

public enum BackupModeInteractive {
	FULL, INCREMENTAL, UNKNOW, Full, Incremental, f, i, F, I, incremental, full, incr, Incr;

	public static BackupModeInteractive parse(final Object mode) {
		return parse(mode.toString());
	}

	public static BackupModeInteractive parse(final String modeStr) {

		if (modeStr.equalsIgnoreCase("full") || modeStr.equalsIgnoreCase("f")) {
			return FULL;

		} else if (modeStr.equalsIgnoreCase("incr") || modeStr.equalsIgnoreCase("inc")
				|| modeStr.equalsIgnoreCase("incremental") || modeStr.equalsIgnoreCase("i")) {
			return INCREMENTAL;
		} else {
			return UNKNOW;
		}
	}

	@Override
	public String toString() {

		String ret = "unknown";
		switch (this) {
		case FULL:
		case full:
		case Full:
		case f:
		case F:
			ret = "full";
			break;
		case INCREMENTAL:
		case incremental:
		case Incremental:
		case i:
		case I:
		case incr:
		case Incr:
			ret = "incr";
			break;
		case UNKNOW:
			ret = "unknow";
			break;
		default:
			break;
		}

		return ret;
	}

}
