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

import com.vmware.vmbk.command.VmCommand;
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

public class VmCommandInteractive extends VmCommand implements CommandInteractive {

    private static final String commandDescription = "Manage Virtual Machines.";
    private static final String OPTION_POWEROFF = "poweroff";
    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_PROFILE = "profile";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_REBOOT = "reboot";
    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";
    private static final String OPTION_DRYRUN = "dryrun";
    private static final String OPTION_LIST = "list";
    private static final String OPTION_DETAIL = "detail";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_HELP = "help";

    static String helpGlobalSummary() {
	final VmCommandInteractive info = new VmCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public VmCommandInteractive() {
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

	final OptionSpecBuilder optionPowerOff = this.parser.accepts(OPTION_POWEROFF,
		"Shut down the Virtual machine(s).");

	final OptionSpecBuilder optionPowerOn = this.parser.accepts(OPTION_POWERON, "Power On the Virtual machine(s).");

	final OptionSpecBuilder optionReboot = this.parser.accepts(OPTION_REBOOT,
		"Try a  reboot of the Virtual machine(s)");
	final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "Virtual Machines list.");

	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the Virtual machine.");

	final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
		"Enable disable Change Block Tracking");

	optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");

	final OptionSpecBuilder optionProfile = this.parser.accepts(OPTION_PROFILE, "Show Virtual Machine(s) profile.");
	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
	optionHelp.forHelp();

	this.parser.mainOptions(optionPowerOff, optionPowerOn, optionReboot, optionList, optionRemove, optionCbt,
		optionProfile, optionHelp);
	// , optionImport);

	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
	optionVim.withRequiredArg().describedAs("vcenter");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp);
	this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
	this.parser.accepts(OPTION_FORCE, "Force the  Virtual machine operation.").availableIf(optionPowerOff,
		optionReboot, optionRemove);
	this.parser.accepts(OPTION_ALL, "Operation apply to any Virtual Machines.").availableUnless(optionHelp);
	this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(optionHelp, optionList);
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.VM;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

	comp.put("V1", StringsCompleter(VmbkCommandLine.VM));

	comp.put("V11",
		StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REMOVE, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
	comp.put("V12", StringsCompleter(OPTION_LIST, OPTION_DETAIL, OPTION_VIM));
	comp.put("V13", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWERON, OPTION_VIM));
	comp.put("V14",
		StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWEROFF, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
	comp.put("V15",
		StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REBOOT, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
	comp.put("V16", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_ENABLE_CHANGED_BLOCK_TRACKING, OPTION_VIM,
		OPTION_QUIET));
	comp.put("V17", StringsCompleter(OPTION_ALL, OPTION_PROFILE, OPTION_VIM));
	comp.put("V18", StringsCompleter(OPTION_HELP));

	return "|V1 V11*|V1 V12*|V1 V13*|V1 V14*|V1 V15*|V1 V16*|V1 V17*|V1 V18?";
    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vm		Virtual Machine			X	X	X\n"
		+ "tag		vCenter Tag				X	 \n";

	return ret;
    }

    @Override
    public String helpExample() {
	return "vm " + OPTION_LIST + " " + OPTION_DETAIL + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
		+ "\tShow any Virtual Machine available with associated Improved Virtual Disk on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
		+ "vm " + OPTION_POWERON + " vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n"
		+ "\tPower On 3 Virtual Machines. 1st a VM selected by name. 2nd a VM selected by Moref. 3rd a VM selected by UUID\n\n"
		+ "vm " + OPTION_REMOVE + " " + OPTION_FORCE + " vm:testVM\n"
		+ "\tRemove the vm testVM and force a poweroff is the VM is On\n\n";
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

	    } else if (options.has(OPTION_REBOOT)) {
		this.reboot = true;

	    } else if (options.has(OPTION_LIST)) {
		this.list = true;
		if (options.nonOptionArguments().isEmpty()) {
		    this.anyFcoOfType = (FirstClassObjectFilterType.vm | FirstClassObjectFilterType.all);
		}
	    }
	    if (options.has(OPTION_REMOVE)) {
		this.remove = true;
	    }
	    if (options.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
		this.cbt = PrettyBoolean.parseBoolean(options.valueOf(OPTION_ENABLE_CHANGED_BLOCK_TRACKING));
	    }
	    if (options.has(OPTION_ALL)) {
		this.anyFcoOfType = FirstClassObjectFilterType.vm | FirstClassObjectFilterType.all;
	    }
	    if (options.has(OPTION_DRYRUN)) {
		this.setDryRun(true);
	    }
	    if (options.has(OPTION_PROFILE)) {
		this.profile = true;
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
//	    if (options.has(OPTION_OVF_IMPORT)) {
//		this.ovfImport = options.valueOf(OPTION_OVF_IMPORT).toString();
//	    }

	} catch (final Exception e) {
	    IoFunction.showInfo(logger, e.getMessage());
	}

    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
