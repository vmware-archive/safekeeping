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
package com.vmware.safekeeping.core.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.type.VmbkThreadFactory;

public final class ThreadsManager {
	public enum ThreadType {
		VDDK, FCO, ARCHIVE
	}

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(ThreadsManager.class.getName());

	private static ExecutorService vddkExecutorService;
	private static ExecutorService fcoOperationsExecutorService;
	private static ExecutorService archiveOperationsExecutorService;

	public static ExecutorService executor(final ThreadType threadType) {
		switch (threadType) {
		case ARCHIVE:
			return archiveOperationsExecutorService;
		case VDDK:
			return vddkExecutorService;
		case FCO:
		default:
			return fcoOperationsExecutorService;
		}
	}

	public static ExecutorService getArchiveOperationsExecutorService() {
		return archiveOperationsExecutorService;
	}

	public static ExecutorService getFcoOperationsExecutorService() {
		return fcoOperationsExecutorService;
	}

	public static ExecutorService getVddkExecutorService() {
		return vddkExecutorService;
	}

	public static void initialize(final int numberOfConcurrentsFcoThreads, final int numberOfVddkThreads,
			final int numberOfConcurrentsArchiveThreads) {
		vddkExecutorService = Executors.newFixedThreadPool(numberOfVddkThreads,
				new VmbkThreadFactory("vddk-operation", 1));
		fcoOperationsExecutorService = Executors.newFixedThreadPool(numberOfConcurrentsFcoThreads,
				new VmbkThreadFactory("fco-operation", 1));
		archiveOperationsExecutorService = Executors.newFixedThreadPool(numberOfConcurrentsArchiveThreads,
				new VmbkThreadFactory("archive-operation", 1));
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Threads Manager initiated");
		}
	}

	public static void shutdown() {
		// shutdown VDDK ExecutorService
		vddkExecutorService.shutdown();
		fcoOperationsExecutorService.shutdown();
		archiveOperationsExecutorService.shutdown();
	}

	private ThreadsManager() {

	}

}
