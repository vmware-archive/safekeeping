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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.cmd.command.ICommandInteractive;

import joptsimple.AbstractOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class VmbkParser extends OptionParser {
	/**
	 * @param archiveCommandInteractive
	 *
	 * @return
	 */
	public static VmbkParser newVmbkParser(final ICommandInteractive command) {
		final VmbkParser parser = new VmbkParser(command);
		OptionFormatter.newOptionFormatter(parser);
		return parser;
	}

	private ICommandInteractive command;

	private final Map<List<String>, AbstractOptionSpec<?>> mainOptions;

	private VmbkParser(final boolean allowAbbreviations) {
		super(allowAbbreviations);

		this.mainOptions = new HashMap<>();

	}

	/**
	 * @param archiveCommandInteractive
	 */
	public VmbkParser(final ICommandInteractive command) {
		this(true);
		this.command = command;
	}

	public Map<List<String>, Set<OptionSpec<?>>> getAvailableIf() {
		return this.availableIf;
	}

	public Map<List<String>, Set<OptionSpec<?>>> getAvailableUnless() {
		return this.availableUnless;
	}

	public String getDescription() {
		if (this.command != null) {
			return this.command.helpSummary();
		}
		return StringUtils.EMPTY;
	}

	public String getEpilog() {
		if (this.command != null) {
			return this.command.helpEntities();
		}
		return StringUtils.EMPTY;
	}

	public String getExamples() {
		if (this.command != null) {
			return this.command.helpExample();
		}
		return StringUtils.EMPTY;
	}

	public Map<List<String>, AbstractOptionSpec<?>> getMainOptions() {
		return this.mainOptions;
	}

	public String getProgr() {
		if (this.command != null) {
			return this.command.getCommandName();
		}
		return StringUtils.EMPTY;
	}

	public String getProlog() {
		if (this.command != null) {
			return this.command.getPrologo();
		}
		return StringUtils.EMPTY;
	}

	public Map<List<String>, Set<OptionSpec<?>>> getRequiredIf() {
		return this.requiredIf;
	}

	public Map<List<String>, Set<OptionSpec<?>>> getRequiredUnless() {
		return this.requiredUnless;
	}

	public void mainOptions(final OptionSpecBuilder... specs) {
		mutuallyExclusive(specs);
		for (final OptionSpecBuilder spec : specs) {
			this.mainOptions.put(spec.options(), spec);
		}
	}

}
