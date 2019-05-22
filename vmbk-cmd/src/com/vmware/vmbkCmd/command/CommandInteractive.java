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
package com.vmware.vmbkCmd.command;

import java.util.List;
import java.util.Map;

import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.vmbk.command.Command;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;

public interface CommandInteractive extends Command {

//    static public void createParser(final Class<?> c) {
//	CommandInteractive p = null;
//	try {
//	    p = (CommandInteractive) c.newInstance();
//	} catch (InstantiationException | IllegalAccessException e) {
//
//	    e.printStackTrace();
//	}
//	if (p != null) {
//	    p.configureParser();
//	}
//    }

    public CommandInteractive configureParser();

    public String getCommandName();

    /**
     * @return
     */
    public String getPrologo();

    public String getRegexCompleter(final Map<String, StringsCompleter> comp);

    public String helpEntities();

    public String helpExample();

    public String helpSummary();

    public boolean isHelp();

    public void parse(final String[] arguments) throws ParsingException;

    default public OptionSet parseArguments(final VmbkParser parser, final String[] arguments,
	    final List<String> targetList) {
	final OptionSet options = parser.parse(arguments);
	targetList.clear();
	options.nonOptionArguments().forEach((xx) -> targetList.add(String.valueOf(xx)));
	return options;
    }

    /**
     *
     * @param args
     * @return
     */
    default public org.jline.reader.impl.completer.StringsCompleter StringsCompleter(final String... args) {
	final String[] mArgs = new String[args.length];
	if (args.length == 1) {
	    mArgs[0] = args[0];
	} else {
	    for (int i = 0; i < args.length; i++) {
		mArgs[i] = "-" + args[i];
	    }
	}
	return new org.jline.reader.impl.completer.StringsCompleter(mArgs);
    }
}
