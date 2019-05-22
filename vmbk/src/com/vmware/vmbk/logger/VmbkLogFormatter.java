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
package com.vmware.vmbk.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public final class VmbkLogFormatter extends Formatter {
    @Override
    public synchronized String format(final LogRecord rec) {
	StringBuffer msg = new StringBuffer(1024);

	String header = String.format("%tD %<tT.%<tL [%s, %s, %s, %s] ", rec.getMillis(), rec.getLevel().toString(),
		rec.getThreadID(), rec.getLoggerName(), rec.getSourceMethodName());
	msg.append(header);
	msg.append(formatMessage(rec));
	msg.append('\n');

	Throwable throwable = rec.getThrown();
	if (throwable != null) {
	    msg.append(throwable.toString());
	    msg.append('\n');
	    for (StackTraceElement trace : throwable.getStackTrace()) {
		msg.append('\t');
		msg.append(trace.toString());
		msg.append('\n');
	    }
	}
	return msg.toString();
    }
}
