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
package com.vmware.safekeeping.external.command.support;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vmware.safekeeping.core.type.AbstractRunnableCommand;
import com.vmware.safekeeping.core.type.VmbkThreadFactory;

/**
 * @author mdaneri
 *
 */
public class ThreadReaperPools {
	static public ConcurrentHashMap<ExecutorService, List<AbstractRunnableCommand>> executorList;
	static ScheduledExecutorService scheduler;
	static Runnable th;

	static private void cleanThreads() {
		final Calendar calendar = Calendar.getInstance();
		if (ThreadReaperPools.executorList.size() > 0) {
			calendar.add(Calendar.MINUTE, -5);
		} else {
			calendar.add(Calendar.MINUTE, -10);
		}
		final long checkTime = calendar.getTime().getTime();
		final List<ExecutorService> remEsList = new LinkedList<>();
		for (final ExecutorService es : ThreadReaperPools.executorList.keySet()) {
			boolean allDone = true;
			for (final AbstractRunnableCommand lres : ThreadReaperPools.executorList.get(es)) {
				allDone &= lres.getResultAction().isDone()
						&& (lres.getResultAction().getEndDate().getTime().getTime() < checkTime);
			}
			if (allDone) {
				es.shutdown();
				remEsList.add(es);
			}
		}
		for (final ExecutorService es : remEsList) {
			ThreadReaperPools.executorList.remove(es);
		}
	}

	static public void initialize() {
		if (ThreadReaperPools.executorList == null) {
			ThreadReaperPools.executorList = new ConcurrentHashMap<>();
			ThreadReaperPools.th = ThreadReaperPools::cleanThreads;

			ThreadReaperPools.scheduler = Executors
					.newSingleThreadScheduledExecutor(new VmbkThreadFactory("threads-reaper", true));
			ThreadReaperPools.scheduler.scheduleAtFixedRate(ThreadReaperPools.th, 2, 5, TimeUnit.MINUTES);
		}
	}

	static public ExecutorService newExecutors(final List<AbstractRunnableCommand> threads, final int nThreads,
			final VmbkThreadFactory vmbkThreadFactory) {
		final ExecutorService es = Executors.newFixedThreadPool(nThreads, vmbkThreadFactory);
		ThreadReaperPools.executorList.put(es, threads);
		return es;
	}

	static public void shutdown() {
		ThreadReaperPools.scheduler.shutdown();
	}
}
