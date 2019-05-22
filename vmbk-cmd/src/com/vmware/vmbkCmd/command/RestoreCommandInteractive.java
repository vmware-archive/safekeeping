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

import com.vmware.vmbk.command.RestoreCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.FirstClassObjectType;
import com.vmware.vmbk.type.VddkTransportMode;
import com.vmware.vmbk.util.Utility;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class RestoreCommandInteractive extends RestoreCommand implements CommandInteractive {
    private static String commandDescription = "Restore any specified Entities (virtual machine, Improved Virtual Disks, vApp).";
    private static final String OPTION_TRANSPORT = "transport";

    private static final String OPTION_ALL = "all";

    private static final String OPTION_VIM = "vim";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_NOVMDK = "novmdk";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_NAME = "name";

    private static final String OPTION_GENERATION = "generation";

    private static final String OPTION_HOST = "host";
    private static final String OPTION_DATACENTER = "datacenter";
    private static final String OPTION_DATASTORE = "datastore";
    private static final String OPTION_FOLDER = "folder";

    private static final String OPTION_RESPOOL = "respool";

    private static final String OPTION_VM_NETWORKS = "network";

    private static final String OPTION_POWERON = "poweron";

    static String helpGlobalSummary() {
	final RestoreCommandInteractive info = new RestoreCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public RestoreCommandInteractive() {
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
	final OptionSpecBuilder optionTransport = this.parser
		.accepts(OPTION_TRANSPORT, "Prefered Transport Mode (nbd,nbdssl,san,hotadd).")
		.availableUnless(optionHelp);
	optionTransport.withRequiredArg().ofType(VddkTransportMode.class).describedAs("nbd|nbdssl|san|hotadd");

	final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME, "New name for the restored <EnyityType>")
		.availableUnless(optionHelp);
	optionName.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionHost = this.parser
		.accepts(OPTION_HOST, "VMware ESX host vim object name to use as restore target.")
		.availableUnless(optionHelp);
	optionHost.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionDatacenter = this.parser
		.accepts(OPTION_DATACENTER, "Datacenter - vim object name to use as restore target")
		.availableUnless(optionHelp);
	optionDatacenter.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionDatastore = this.parser
		.accepts(OPTION_DATASTORE, "Datastore - vim object name to use as restore target.")
		.availableUnless(optionHelp);
	optionDatastore.withRequiredArg().describedAs("name");
	/*
	 * Option folder
	 */
	final OptionSpecBuilder optionFolder = this.parser
		.accepts(OPTION_FOLDER,
			"Virtual Machine Folder vim object name to use as restore target. <name> vmfolder name"
				+ " <path> vmfolder path  <Datacenter>/vm/<parent>/../<name>")
		.availableUnless(optionHelp);
	optionFolder.withRequiredArg().describedAs("name|path");
	/*
	 * Option respool
	 */
	final OptionSpecBuilder optionRespool = this.parser.accepts(OPTION_RESPOOL,
		"Resource Pool vim object name to use as restore target. <name> resource pool name "
			+ " <path> resource path SDDC-Datacenter/host/<cluster>/Resources/Compute-ResourcePool/..<name>")
		.availableUnless(optionHelp);
	optionRespool.withRequiredArg().describedAs("name|path");
	/*
	 * Option network
	 */
	final OptionSpecBuilder optionNetwork = this.parser
		.accepts(OPTION_VM_NETWORKS,
			"Reconfigure VM newtwork to the new specified backend starting from vm eth0 to eth9.")
		.availableUnless(optionHelp);
	optionNetwork.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");

	this.parser.mainOptions(optionHelp);

	optionHelp.forHelp();

	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
	optionVim.withRequiredArg().describedAs("vcenter");
	final OptionSpecBuilder optionGeneration = this.parser.accepts(OPTION_GENERATION, "Generation <id> to restore")
		.availableUnless(optionHelp);
	optionGeneration.withRequiredArg().ofType(Integer.class).describedAs("id");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionHelp);

	this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
		.availableUnless(optionHelp).withOptionalArg().ofType(FirstClassObjectType.class)
		.describedAs("vm|ivd|vapp");
	this.parser.accepts(OPTION_POWERON, "PowerOn virtual machine after restore").availableUnless(optionHelp);
	this.parser.accepts(OPTION_NOVMDK, "Exclude VMDK contents.").availableUnless(optionHelp);
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.RESTORE;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
	comp.put("R1", StringsCompleter(VmbkCommandLine.RESTORE));

	comp.put("R11",
		StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_TRANSPORT, OPTION_DATASTORE, OPTION_NOVMDK,
			OPTION_TRANSPORT, OPTION_FOLDER, OPTION_RESPOOL, OPTION_VM_NETWORKS, OPTION_HOST,
			OPTION_GENERATION, OPTION_DATACENTER, OPTION_VIM));
	comp.put("R12", StringsCompleter(OPTION_HELP));
	return "|R1 R11*|R1 R12?";

    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vm		Virtual Machine			X	X	X\n"
		+ "ivd		Improved Virtual Disks	X	X	 \n"
		+ "vapp		Virtual Appliance		X		 \n";
	return ret;
    }

    @Override
    public String helpExample() {
	return String.format(" restore %s\n\tRecover any object previously backup.\n\n", OPTION_ALL) + String.format(
		" restore %s 4 vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n\tStart a restore of the 4th generation profile of 3 Virtual Machines :1st by name,2nd by Moref,3rd by UUID.\n\n",
		OPTION_GENERATION)
		+ String.format(
			" restore %s vm %s 9a583042-cb9d-5673-cbab-56a02a91805d\n\tRestore any Virtual Machine managed by vcenter 9a583042-cb9d-5673-cbab-56a02a91805d.\n\n",
			OPTION_ALL, OPTION_VIM)
		+ String.format(
			" restore vm:testVM  %s \"newVM\" %s \"myDatacenter\" %s \"myDatastore\" %s \"myResPool\" \n\n\tRestore vm:testVM  to an new VM named NewVm using as Datacenter:myDatacenter, as Datastore:myDatastore and as Resource Pool:myResPool \n\n",
			OPTION_NAME, OPTION_DATACENTER, OPTION_DATASTORE, OPTION_RESPOOL)
		+ String.format(
			" restore vm:testVM  %s \"newVM\" %s \"myVmFolder\" %s \"mynetwork01,mynetwork02,,,mynetwork05\"\n\n\tRestore vm:testVM  to an new VM named NewVm using as Virtual Machine folder: myVmFolder and reconfigure the vm network backend to eth0:mynetwork01,eth1:mynetwork02 and eth4:mynetwork05 \n\n",
			OPTION_NAME, OPTION_FOLDER, OPTION_VM_NETWORKS);

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
	if (options.has(OPTION_NAME)) {
	    this.setNewName(options.valueOf(OPTION_NAME).toString());
	} else {
	    this.setRecover(true);
	}
	if (options.has(OPTION_GENERATION)) {
	    this.setGenerationId((Integer) options.valueOf(OPTION_GENERATION));
	}

	if (options.has(OPTION_HOST)) {
	    this.setHostName(options.valueOf(OPTION_HOST).toString());

	}
	if (options.has(OPTION_DATASTORE)) {
	    this.setDatastoreName(options.valueOf(OPTION_DATASTORE).toString());
	}
	if (options.has(OPTION_DATACENTER)) {
	    this.setDatacenterName(options.valueOf(OPTION_DATACENTER).toString());
	}
	if (options.has(OPTION_FOLDER)) {
	    this.setFolderName(options.valueOf(OPTION_FOLDER).toString());
	} else {
	    this.setFolderName(GlobalConfiguration.getVmFilter());
	}

	if (options.has(OPTION_VM_NETWORKS)) {
	    this.setVmNetworksName(options.valueOf(OPTION_VM_NETWORKS).toString());
	}

	if (options.has(OPTION_RESPOOL)) {
	    this.setResourcePoolName(options.valueOf(OPTION_RESPOOL).toString());
	} else {
	    this.setResourcePoolName(GlobalConfiguration.getRpFilter());
	}

	if (options.has(OPTION_TRANSPORT)) {
	    this.setTransportMode(options.valueOf(OPTION_TRANSPORT).toString());
	    final String transportCheck = Utility.checkTransportMode(this.getTransportMode());
	    if (StringUtils.isNotEmpty(transportCheck)) {
		throw new ParsingException(OPTION_TRANSPORT, transportCheck);
	    }
	}

	if (options.has(OPTION_NOVMDK)) {
	    this.setNoVmdk(true);
	}
	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}
	if (options.has(OPTION_POWERON)) {
	    this.setPowerOn(true);
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
