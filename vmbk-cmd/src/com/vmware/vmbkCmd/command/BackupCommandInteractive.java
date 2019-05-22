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

import com.vmware.vmbk.command.BackupCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.FirstClassObjectType;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyBoolean.PrettyBooleanValues;
import com.vmware.vmbk.type.VddkTransportMode;
import com.vmware.vmbk.util.Utility;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class BackupCommandInteractive extends BackupCommand implements CommandInteractive {
    private static final String OPTION_NOVMDK = "novmdk";
    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_COMPRESSION = "compression";

    private static final String OPTION_MODE = "mode";

    private static final String OPTION_FORCE = "force";

    private static final String OPTION_TRANSPORT = "transport";

    private static final String OPTION_ALL = "all";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_HELP = "help";

    static String helpGlobalSummary() {
	final BackupCommandInteractive info = new BackupCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private final String commandDescription = "Backup any specified Entities (virtual machine, Improved Virtual Disks, vApp)";

    private boolean help;

    public BackupCommandInteractive() {
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
	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");

	final OptionSpecBuilder optionCompression = this.parser
		.accepts(OPTION_COMPRESSION, "Enable/disable compression.").availableUnless(optionHelp);
	optionCompression.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
	final OptionSpecBuilder optionTransport = this.parser
		.accepts(OPTION_TRANSPORT, "Prefered Transport Mode (nbd,nbdssl,san,hotadd).")
		.availableUnless(optionHelp);
	optionTransport.withRequiredArg().ofType(VddkTransportMode.class).describedAs("nbd|nbdssl|san|hotadd");

	final OptionSpecBuilder optionMode = this.parser.accepts(OPTION_MODE, "Specify the backup mode (incr,  full).")
		.availableUnless(optionHelp);

	optionMode.withRequiredArg().ofType(BackupMode.class).describedAs("incr|full");

	this.parser.mainOptions(optionHelp);

	optionHelp.forHelp();
	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
	optionVim.withRequiredArg().describedAs("vcenter");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionHelp);

	this.parser.accepts(OPTION_FORCE, "Force the  Virtual machine operation.").availableUnless(optionHelp);
	this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
		.availableUnless(optionHelp).withOptionalArg().ofType(FirstClassObjectType.class)
		.describedAs("vm|ivd|vapp");
	this.parser.accepts(OPTION_NOVMDK, "Exclude VMDK contents.").availableUnless(optionHelp);
	return this;

    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.BACKUP;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

	comp.put("B1", StringsCompleter(VmbkCommandLine.BACKUP));
	comp.put("B11", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_TRANSPORT, OPTION_MODE, OPTION_NOVMDK,
		OPTION_FORCE, OPTION_COMPRESSION, OPTION_VIM));
	comp.put("B12", StringsCompleter(OPTION_HELP));
	return "|B1 B11*|B1 B12?";
    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vm		Virtual Machine			X	X	X\n"
		+ "ivd		Improved Virtual Disks		X	X	 \n"
		+ "vapp		Virtual Appliance		X		 \n"
		+ "tag		vCenter Tag				X	 \n";
	return ret;
    }

    @Override
    public String helpExample() {
	return String.format(" backup %s\n\tBackup any kind of object.\n\n", OPTION_ALL) + String.format(
		" backup %s full  vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330 \n\tStart a backup in full mode of 3 different Vm:1st by name,2nd by Moref,3rd by UUID.\n\n",
		OPTION_MODE)
		+ String.format(
			" backup %s ivd %s 9a583042-cb9d-5673-cbab-56a02a91805d\n\tBackup any Improved Virtual Disks managed by vcenter 9a583042-cb9d-5673-cbab-56a02a91805d.\n\n",
			OPTION_ALL, OPTION_VIM);
    }

    @Override
    public String helpSummary() {
	return this.commandDescription;
    }

    @Override
    public boolean isHelp() {
	return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
	final OptionSet options = parseArguments(this.parser, arguments, this.targetList);
	this.setHelp(options.has(OPTION_HELP));
	if (options.has(OPTION_NOVMDK)) {
	    this.isNoVmdk = true;
	}
	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}
	if (options.has(OPTION_COMPRESSION)) {
	    this.setCompression(PrettyBoolean.parseBoolean(options.valueOf(OPTION_COMPRESSION)));
	} else {
	    this.setCompression(GlobalConfiguration.getEnableCompression());
	}

	if (options.has(OPTION_MODE)) {
	    this.mode = BackupMode.parse(options.valueOf(OPTION_MODE));

	}
	if (options.has(OPTION_FORCE)) {
	    this.isForce = true;
	}
	if (options.has(OPTION_TRANSPORT)) {
	    this.transportMode = options.valueOf(OPTION_TRANSPORT).toString();
	    final String transportCheck = Utility.checkTransportMode(this.getTransportMode());
	    if (StringUtils.isNotEmpty(transportCheck)) {
		throw new ParsingException(OPTION_TRANSPORT, transportCheck);
	    }
	}
	if (options.has(OPTION_ALL)) {
	    this.anyFcoOfType = FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
		    FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);

	}
	if (options.has(OPTION_VIM)) {
	    this.vim = options.valueOf(OPTION_VIM).toString();
	}

    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
