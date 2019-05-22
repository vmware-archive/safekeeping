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

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.VmbkVersion;
import com.vmware.vmbk.util.BiosUuid;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VersionCommandInteractive extends SimpleCommandInteractive implements CommandInteractive {
    private static String commandDescription = "Return Vmbk and Vddk Version.";
    private static final String OPTION_HELP = "help";

    static String helpGlobalSummary() {
	final VersionCommandInteractive info = new VersionCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    public static void showVersion() {
	VmbkVersion.showVersion();
	BiosUuid.getLocalHwSettings();
    }

    private VmbkParser parser;

    public VersionCommandInteractive() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	if (isHelp()) {
	    this.parser.printHelpOn(System.out);
	} else {
	    showVersion();
	}
    }

    @Override
    public CommandInteractive configureParser() {
	this.parser = VmbkParser.newVmbkParser(this);
	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
	optionHelp.forHelp();
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.VERSION;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
	comp.put("Ver1", StringsCompleter(VmbkCommandLine.VERSION));
	comp.put("Ver11", StringsCompleter(OPTION_HELP));
	return "|Ver1 Ver11?";
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbkCmd.command.CommandCmd#helpEntities()
     */
    @Override
    public String helpEntities() {

	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbkCmd.command.CommandCmd#helpExample()
     */
    @Override
    public String helpExample() {

	return null;
    }

    @Override
    public String helpSummary() {
	return commandDescription;
    }

    @Override
    public void initialize() {
	this.setHelp(false);
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
	final OptionSet options = this.parser.parse(arguments);
	this.setHelp(options.has(OPTION_HELP));
    }
}
