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
package com.vmware.safekeeping.core.type;

import java.util.concurrent.ThreadFactory;

public class VmbkThreadFactory implements ThreadFactory {
	private final String name;
	private Integer index;
	private final boolean daemon;

	public VmbkThreadFactory(final String name) {
		this(name, false, null);
	}

	public VmbkThreadFactory(final String name, final boolean daemon) {
		this(name, daemon, null);
	}

	public VmbkThreadFactory(final String name, final boolean daemon, final Integer startingIndex) {
		this.name = name;
		this.index = startingIndex;
		this.daemon = daemon;
	}

	public VmbkThreadFactory(final String name, final int startingIndex) {
		this(name, false, startingIndex);
	}

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return this.index;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the daemon
	 */
	public boolean isDaemon() {
		return this.daemon;
	}

	@Override
	public Thread newThread(final Runnable r) {
		Thread thread = null;
		if (r instanceof AbstractRunnableCommand) {
			final AbstractRunnableCommand rc = (AbstractRunnableCommand) r;
			thread = new Thread(r, rc.getName());
		} else {
			thread = new Thread(r, (this.index == null) ? this.name : String.format("%s-%d", this.name, this.index++));
		}
		thread.setDaemon(this.daemon);
		return thread;
	}
}