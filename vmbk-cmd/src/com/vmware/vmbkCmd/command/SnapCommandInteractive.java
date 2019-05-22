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

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.vmbk.command.SnapCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class SnapCommandInteractive extends SnapCommand implements CommandInteractive {
    private static String commandDescription = "Manage Snapshot of any specified Entities (virtual machine, Improved Virtual Disks, vApp).";
    private static final String OPTION_ALL = "all";

    private static final String OPTION_VIM = "vim";

    private static final String OPTION_QUIET = "quiet";

    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_LIST = "list";

    private static final String OPTION_DETAIL = "detail";

    private static final String OPTION_FORCE = "force";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_CREATE = "create";
    private static final String OPTION_REVERT = "revert";
    private static final String OPTION_NAME = "name";
    private static final String OPTION_DELETEALL = "deleteall";

    static String helpGlobalSummary() {
	final SnapCommandInteractive info = new SnapCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public SnapCommandInteractive() {
	super();
	this.setHelp(false);
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	if (isHelp()) {
	    this.parser.printHelpOn(System.out);
	} else {
	    super.action(vmbk);
	}
    }

    @Override
    public CommandInteractive configureParser() {
	this.parser = VmbkParser.newVmbkParser(this);
	final OptionSpecBuilder optionCreate = this.parser.accepts(OPTION_CREATE, "Create a new snapshot.");
	final OptionSpecBuilder optionRevert = this.parser.accepts(OPTION_REVERT, "Revert to a snapshot.");
	optionRevert.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "List Snapshot hierarchy.");

	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Delete the Virtual machine.");
	optionRemove.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");
	final OptionSpecBuilder optionDeleteAll = this.parser.accepts(OPTION_DELETEALL,
		"Delete any snapshot associated to the entity/entities.");

	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
	optionHelp.forHelp();

	this.parser.mainOptions(optionCreate, optionRevert, optionList, optionRemove, optionDeleteAll, optionHelp);
	/*
	 * Option vim
	 */
	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
	optionVim.withRequiredArg().describedAs("vcenter");
	/*
	 * Option dryrun
	 */
	this.parser.accepts(OPTION_DRYRUN, "Do not snap really.").availableUnless(optionList, optionHelp);
	this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
	this.parser.accepts(OPTION_FORCE, "Force the  Snapshot operation.").availableIf(optionRevert, optionDeleteAll,
		optionRemove);
	this.parser.accepts(OPTION_ALL, "Operation apply to any Virtual Machines.").availableUnless(optionHelp);
	this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(optionHelp, optionList);
	final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME, "Used by " + OPTION_CREATE)
		.availableIf(optionCreate);
	optionName.withRequiredArg().describedAs("name");
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.SNAP;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

	comp.put("N1", StringsCompleter(VmbkCommandLine.SNAP));
	comp.put("N11", StringsCompleter(OPTION_ALL, OPTION_LIST, OPTION_VIM));
	comp.put("N12", StringsCompleter(OPTION_DRYRUN, OPTION_REMOVE, OPTION_QUIET, OPTION_VIM));
	comp.put("N13", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_QUIET, OPTION_DELETEALL, OPTION_VIM));
	comp.put("N14", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_CREATE, OPTION_VIM));
	comp.put("N15", StringsCompleter(OPTION_DRYRUN, OPTION_REVERT, OPTION_VIM));
	comp.put("N16", StringsCompleter(OPTION_HELP));
	return "|N1 N11*|N1 N12*|N1 N13*|N1 N14*|N1 N15*|N1 N16?";
    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vm		Virtual Machine			X	X	X\n"
		+ "ivd		Improved Virtual Disks	X	X	 \n"
		+ "vapp		Virtual Appliance		X		 \n"
		+ "tag		vCenter Tag				X	 \n";

	return ret;
    }

    @Override
    public String helpExample() {
	return " snap " + OPTION_LIST + " " + OPTION_ALL + " " + OPTION_VIM
		+ " 9a583042-cb9d-5673-cbab-56a02a91805d\n\tShow any snapshot of any available object on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
		+ " snap " + OPTION_CREATE
		+ " vm:testVM vm:vm-2313 ivd:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n\tCreate a snapshot  3 different objects.  1st a VM selected by name. 2nd a VM selected by Moref. 3rd an IVD selected by UUID\n\n"
		+ " snap " + OPTION_DELETEALL + " " + OPTION_ALL + " vm \n\tDelete any snapshot from any VM \n\n";
    }

    @Override
    public String helpSummary() {
	return commandDescription;
    }

    @Override
    public boolean isHelp() {
	return this.help;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(final String[] arguments) throws ParsingException {
	final OptionSet options = parseArguments(this.parser, arguments, this.targetList);
	this.setHelp(options.has(OPTION_HELP));
	if (options.has(OPTION_LIST)) {
	    this.list = true;
	    if (options.nonOptionArguments().isEmpty()) {
		this.anyFcoOfType = (FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
	    }
	}

	if (options.has(OPTION_CREATE)) {
	    this.create = true;
	}
	if (options.has(OPTION_REVERT)) {
	    this.revert = true;
	}
	if (options.has(OPTION_REMOVE)) {
	    this.delete = (List<String>) options.valuesOf(OPTION_REMOVE);
	}
	if (options.has(OPTION_DELETEALL)) {

	    this.deleteAll = true;
	}

	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}
	if (options.has(OPTION_NAME)) {
	    this.name = options.valueOf(OPTION_NAME).toString();
	}

	if (options.has(OPTION_ALL)) {
	    this.anyFcoOfType = FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
		    FirstClassObjectFilterType.any) | FirstClassObjectFilterType.all;

	}
	if (options.has(OPTION_VIM)) {
	    this.vim = options.valueOf(OPTION_VIM).toString();
	}
	if (options.has(OPTION_QUIET)) {
	    this.setQuiet(true);
	}

    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
