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
package com.vmware.safekeeping.cxf.rest.support;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.VmbkThreadFactory; 

/**
 * @author mdaneri
 *
 */
public final class ThreadUsersReaperPools {
	public final class ThreadUsersReaperPoolsLogFormatter extends Formatter {
		@Override
		public synchronized String format(final LogRecord rec) {
			final StringBuffer msg = new StringBuffer(1024);

			final String header = String.format("%tD %<tT.%<tL [%s, %s, %s] ", rec.getMillis(),
					rec.getLevel().toString(), rec.getThreadID(), rec.getSourceMethodName());
			msg.append(header);
			msg.append(formatMessage(rec));
			msg.append('\n');

			final Throwable throwable = rec.getThrown();
			if (throwable != null) {
				msg.append(throwable.toString());
				msg.append('\n');
				for (final StackTraceElement trace : throwable.getStackTrace()) {
					msg.append('\t');
					msg.append(trace.toString());
					msg.append('\n');
				}
			}
			return msg.toString();
		}
	}

	private static final int MAX_HOURS = 5;
	static ThreadUsersReaperPools threadReaperPools;
	static final String THREAD_NAME = "threads-users-reaper";
	static final long initialDelay = 2;

	static final long period = 2;

	static public void initialize(final ConcurrentHashMap<String, User> _usersList)
			throws SecurityException, IOException {
		threadReaperPools = new ThreadUsersReaperPools(_usersList);
		threadReaperPools.initScheduler();
	}

	static public void shutdown() {
		threadReaperPools._shutdown();
	}

	private final ConcurrentHashMap<String, User> usersList;

	private ScheduledExecutorService scheduler;

	private Runnable th;
	private final Logger logger;

	private FileHandler filehandler = null;

	private ThreadUsersReaperPools(final ConcurrentHashMap<String, User> _usersList)
			throws SecurityException, IOException {
		this.logger = Logger.getLogger(ThreadUsersReaperPools.class.getName());

		this.usersList = _usersList;
		configureLog();

	}

	private void _shutdown() {
		this.scheduler.shutdown();
		if (this.filehandler != null) {
			this.logger.removeHandler(this.filehandler);
			this.filehandler.close();
		}
	}

	private void configureLog() throws SecurityException, IOException {
		final String logFile = String.format("%s%s%s.log", CoreGlobalSettings.getLogsPath(), File.separator,
				THREAD_NAME);
		this.filehandler = new FileHandler(logFile);

		this.filehandler.setFormatter(new ThreadUsersReaperPoolsLogFormatter());
		this.logger.addHandler(this.filehandler);
		this.logger.setUseParentHandlers(false);

	}

	private void initScheduler() {
		threadReaperPools.th = () -> {
			final Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -MAX_HOURS);
			final long checkTime = calendar.getTimeInMillis();
			if (this.logger.isLoggable(Level.INFO)) {
				this.logger.info("CleanThreads - checkTime: " + checkTime);
			}
			final List<String> removeList = new LinkedList<>();
			for (final String key : this.usersList.keySet()) {

				final long lastOperation = this.usersList.get(key).getLastOperation();
				if (this.logger.isLoggable(Level.INFO)) {
					final String msg = String.format("Check session:%s LastOperation:%d chekTime:%d Timeout:%b", key,
							lastOperation, checkTime, (lastOperation < checkTime));
					this.logger.info(msg);
				}
				if (lastOperation < checkTime) {
					removeList.add(key);
				}
			}
			for (final String key : removeList) {
				if (this.logger.isLoggable(Level.INFO)) {
					this.logger.info("Remove User:" + key);
				}
				this.usersList.get(key).close();

				this.usersList.remove(key);
			}
			this.logger.info("CleanThreads - done");
		};

		threadReaperPools.scheduler = Executors
				.newSingleThreadScheduledExecutor(new VmbkThreadFactory(THREAD_NAME, true));
		this.scheduler.scheduleAtFixedRate(this.th, initialDelay, period, TimeUnit.MINUTES);
		if (this.logger.isLoggable(Level.INFO)) {
			final String msg = String.format("Configured Scheduler: initialDelay:%d period:%d", initialDelay, period);
			this.logger.info(msg);
		}
	}
}
