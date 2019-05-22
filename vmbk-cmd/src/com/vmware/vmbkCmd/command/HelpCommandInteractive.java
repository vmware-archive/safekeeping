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
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class HelpCommandInteractive extends SimpleCommandInteractive implements CommandInteractive {
    private static final String commandDescription = "Command Line interactive help.";
    private static boolean all;
    private static final String OPTION_HELP = "help";
    private static final String OPTION_ALL = "all";

    public static void introHelp() {
	System.out.printf(
		"\n" + "Online Virtual Machine Backup Tool for VMware vSphere.\n" + "Version %s   VDDK Version %s\n"
			+ "Copyright (C) 2019 VMware Inc. All rights reserved.\n"
			+ "This software comes with ABSOLUTELY NO WARRANTY. This is free software,\n"
			+ "and you are welcome to modify and redistribute it under the BSD v2 license.\n" + "\n"
			+ "Usage: %s [prep.comand]|[command] [option(s)] [target(s)]\n" + "\n",
		VmbkVersion.getVersion(), VmbkVersion.getVddkVersion(), VmbkVersion.getProductName());
    }

// TODO Remove unused code found by UCDetector
//     public static void doSummaryHelp() {
//
// 	introHelp();
// 	showHelp();
//     }

    static public void showHelp() {
	if (all) {

	} else {
	    System.out.println(BackupCommandInteractive.helpGlobalSummary());
	    System.out.println(RestoreCommandInteractive.helpGlobalSummary());
	    System.out.println(SnapCommandInteractive.helpGlobalSummary());
	    System.out.println(VmCommandInteractive.helpGlobalSummary());
	    System.out.println(VappCommandInteractive.helpGlobalSummary());
	    System.out.println(IvdCommandInteractive.helpGlobalSummary());
	    System.out.println(ArchiveCommandInteractive.helpGlobalSummary());
	    System.out.println(TagCommandInteractive.helpGlobalSummary());
	    System.out.println(K8sCommandInteractive.helpGlobalSummary());
	    System.out.println(FcoCommandInteractive.helpGlobalSummary());

	    System.out.println("----");
	    System.out.println(ConfigCommandInteractive.helpGlobalSummary());
	    System.out.println(VersionCommandInteractive.helpGlobalSummary());
	    System.out.println(QuitCommandInteractive.helpGlobalSummary());
	    System.out.println("!\t\t\tExecute shell command");
	    System.out.println("&\t\t\tExecute external " + VmbkVersion.getProductName() + " commands");
	    System.out.println();
	    System.out.println("[command] " + OPTION_HELP + " for command help");
	}
    }

    private VmbkParser parser;

    public HelpCommandInteractive() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	if (isHelp()) {
	    this.parser.printHelpOn(System.out);
	} else {
	    showHelp();
	}
    }

    @Override
    public CommandInteractive configureParser() {
	this.parser = VmbkParser.newVmbkParser(this);
	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
	final OptionSpecBuilder optionAll = this.parser.accepts(OPTION_ALL, "Show help from any available commmands")
		.availableUnless(optionHelp);
	optionHelp.availableUnless(optionAll).forHelp();
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.HELP;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
	comp.put("H1", StringsCompleter(VmbkCommandLine.HELP));
	comp.put("H11", StringsCompleter(OPTION_HELP));
	return "|H1 H11?";
    }

    @Override
    public String helpEntities() {

	return null;
    }

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
	all = false;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
	final OptionSet options = this.parser.parse(arguments);
	this.setHelp(options.has(OPTION_HELP));

	if (options.has(OPTION_ALL)) {
	    HelpCommandInteractive.all = true;
	} else {
	    HelpCommandInteractive.all = false;
	}
    }
}
