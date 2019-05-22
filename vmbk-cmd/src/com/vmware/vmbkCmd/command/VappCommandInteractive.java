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

import com.vmware.vmbk.command.VappCommand;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyBoolean.PrettyBooleanValues;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VappCommandInteractive extends VappCommand implements CommandInteractive {

    private static final String commandDescription = "Manage VirtualApp.";
    private static final String OPTION_POWEROFF = "poweroff";
    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";
    private static final String OPTION_DRYRUN = "dryrun";
    private static final String OPTION_LIST = "list";
    private static final String OPTION_DETAIL = "detail";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_HELP = "help";

    static String helpGlobalSummary() {
	final VappCommandInteractive info = new VappCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public VappCommandInteractive() {
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

	final OptionSpecBuilder optionPowerOff = this.parser.accepts(OPTION_POWEROFF, "Shut down the virtualApp(s).");

	final OptionSpecBuilder optionPowerOn = this.parser.accepts(OPTION_POWERON, "Power On the virtualApp(s).");

	final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "virtualApps list.");

	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the virtualApp.");

	final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
		"Enable disable Change Block Tracking");
	optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");

	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
	optionHelp.forHelp();

	this.parser.mainOptions(optionPowerOff, optionPowerOn, optionList, optionRemove, optionCbt, optionHelp);

	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
	optionVim.withRequiredArg().describedAs("vcenter");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp);
	this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
	this.parser.accepts(OPTION_FORCE, "Force the  virtualApp operation.").availableIf(optionPowerOff, optionRemove);
	this.parser.accepts(OPTION_ALL, "Operation apply to any virtualApps.").availableUnless(optionHelp);
	this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(optionHelp, optionList);
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.VAPP;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

	comp.put("VA1", StringsCompleter(VmbkCommandLine.VAPP));

	comp.put("VA11",
		StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REMOVE, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
	comp.put("VA12", StringsCompleter(OPTION_LIST, OPTION_DETAIL, OPTION_VIM));
	comp.put("VA13", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWERON, OPTION_VIM));
	comp.put("VA14",
		StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWEROFF, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));

	comp.put("VA16", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_ENABLE_CHANGED_BLOCK_TRACKING, OPTION_VIM,
		OPTION_QUIET));
	comp.put("VA18", StringsCompleter(OPTION_HELP));

	return "|VA1 VA11*|VA1 VA12*|VA1 VA13*|VA1 VA14*| VA1 VA16*| VA1 VA18?";
    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vapp		virtualApp			X	X	X\n"
		+ "tag		vCenter Tag				X	 \n";

	return ret;
    }

    @Override
    public String helpExample() {
	return "vapp " + OPTION_LIST + " " + OPTION_DETAIL + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
		+ "\tShow any virtualApp available with associated Virtual Machines on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
		+ "vapp " + OPTION_POWERON
		+ " vapp:testVM vapp:resgroup-v2313 vapp:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n"
		+ "\tPower On 3 virtualApp . 1st a VAPP selected by name. 2nd a VAPP selected by Moref. 3rd a VAPP selected by UUID\n\n"
		+ "vapp " + OPTION_REMOVE + " " + OPTION_FORCE + " vapp:testVapp\n"
		+ "\tDelete the vm vapp:testVapp and force a poweroff is the VAPP is On\n\n";
    }

    @Override
    public String helpSummary() {
	return commandDescription;
    }

    @Override
    public boolean isHelp() {
	return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
	final OptionSet options = parseArguments(this.parser, arguments, this.targetList);
	this.setHelp(options.has(OPTION_HELP));
	try {
	    if (options.has(OPTION_POWERON)) {
		this.powerOn = true;

	    } else if (options.has(OPTION_POWEROFF)) {
		this.powerOff = true;

	    } else if (options.has(OPTION_LIST)) {
		this.list = true;
		if (options.nonOptionArguments().isEmpty()) {
		    this.anyFcoOfType = (FirstClassObjectFilterType.vapp | FirstClassObjectFilterType.all);
		}
	    }
	    if (options.has(OPTION_REMOVE)) {
		this.remove = true;
	    }
	    if (options.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
		this.cbt = PrettyBoolean.parseBoolean(options.valueOf(OPTION_ENABLE_CHANGED_BLOCK_TRACKING));
	    }
	    if (options.has(OPTION_ALL)) {
		this.anyFcoOfType = FirstClassObjectFilterType.vapp | FirstClassObjectFilterType.all;
	    }
	    if (options.has(OPTION_DRYRUN)) {
		this.setDryRun(true);
	    }

	    if (options.has(OPTION_FORCE)) {
		this.force = true;
	    }
	    if (options.has(OPTION_DETAIL)) {
		this.detail = true;
	    }
	    if (options.has(OPTION_QUIET)) {
		this.setQuiet(true);
	    }
	    if (options.has(OPTION_VIM)) {
		this.vim = options.valueOf(OPTION_VIM).toString();
	    }

	} catch (final Exception e) {
	    IoFunction.showInfo(logger, e.getMessage());
	}

    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
