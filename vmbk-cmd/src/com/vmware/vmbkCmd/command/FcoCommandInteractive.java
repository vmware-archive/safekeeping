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

import java.net.MalformedURLException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.vmbk.command.FcoCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.PrettyBoolean.PrettyBooleanValues;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class FcoCommandInteractive extends FcoCommand implements CommandInteractive {
    private static String commandDescription = "Import and Copy First Class Objects.";

    private static final String OPTION_VIM = "vim";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_NOVMDK = "novmdk";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_NAME = "name";

    private static final String OPTION_HOST = "host";
    private static final String OPTION_DATACENTER = "datacenter";
    private static final String OPTION_DATASTORE = "datastore";
    private static final String OPTION_FOLDER = "folder";

    private static final String OPTION_RESPOOL = "respool";

    private static final String OPTION_VM_NETWORKS = "network";

    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_URL = "url";
    private static final String OPTION_IMPORT = "import";
    private static final String OPTION_CLONE = "clone";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";

    static String helpGlobalSummary() {
	final FcoCommandInteractive info = new FcoCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public FcoCommandInteractive() {
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

	final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
		"Enable disable Change Block Tracking");
	optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
	final OptionSpecBuilder optionImport = this.parser.accepts(OPTION_IMPORT, "Import an OVF.");
	final OptionSpecBuilder optionClone = this.parser.accepts(OPTION_CLONE, "Clone a First Class Object");
	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the First Class Object.");
	this.parser.mainOptions(optionHelp, optionImport, optionClone, optionRemove, optionCbt);
	final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME, "Name for the imported <EnyityType>")
		.requiredIf(optionImport, optionClone);

	optionName.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionHost = this.parser
		.accepts(OPTION_HOST, "VMware ESX host vim object name to use as import target.")
		.availableIf(optionImport);
	optionHost.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionDatacenter = this.parser
		.accepts(OPTION_DATACENTER, "Datacenter - vim object name to use as import target")
		.availableIf(optionImport);
	optionDatacenter.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionDatastore = this.parser
		.accepts(OPTION_DATASTORE, "Datastore - vim object name to use as import target.")
		.availableIf(optionImport, optionClone);
	optionDatastore.withRequiredArg().describedAs("name");

	final OptionSpecBuilder optionUrl = this.parser.accepts(OPTION_URL, " Source url.").availableIf(optionImport);
	optionUrl.withRequiredArg().describedAs("url");
	this.parser.accepts(OPTION_FORCE, "Force the operation.").availableIf(optionRemove);
	/*
	 * Option folder
	 */
	final OptionSpecBuilder optionFolder = this.parser
		.accepts(OPTION_FOLDER,
			"Virtual Machine Folder vim object name to use as import target. <name> vmfolder name"
				+ " <path> vmfolder path  <Datacenter>/vm/<parent>/../<name>")
		.availableIf(optionImport);
	optionFolder.withRequiredArg().describedAs("name|path");
	/*
	 * Option respool
	 */
	final OptionSpecBuilder optionRespool = this.parser.accepts(OPTION_RESPOOL,
		"Resource Pool vim object name to use as import target. <name> resource pool name "
			+ " <path> resource path SDDC-Datacenter/host/<cluster>/Resources/Compute-ResourcePool/..<name>")
		.availableIf(optionImport);
	optionRespool.withRequiredArg().describedAs("name|path");
	/*
	 * Option network
	 */
	final OptionSpecBuilder optionNetwork = this.parser
		.accepts(OPTION_VM_NETWORKS,
			"Reconfigure VM newtwork to the new specified backend starting from vm eth0 to eth9.")
		.availableIf(optionImport);
	optionNetwork.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");

	this.parser.mainOptions(optionHelp);

	optionHelp.forHelp();

	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)")
		.availableIf(optionImport, optionClone, optionRemove);
	optionVim.withRequiredArg().describedAs("vcenter");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableIf(optionImport, optionClone, optionRemove,
		optionCbt);

	this.parser.accepts(OPTION_POWERON, "PowerOn virtual machine after import").availableIf(optionImport,
		optionClone);
	this.parser.accepts(OPTION_NOVMDK, "Exclude VMDK contents.").availableIf(optionImport);
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.FCO;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
	comp.put("IM1", StringsCompleter(VmbkCommandLine.FCO));

	comp.put("IM11", StringsCompleter(OPTION_DRYRUN, OPTION_IMPORT, OPTION_DATASTORE, OPTION_NOVMDK, OPTION_FOLDER,
		OPTION_RESPOOL, OPTION_VM_NETWORKS, OPTION_HOST, OPTION_DATACENTER, OPTION_VIM, OPTION_URL));

	comp.put("IM12", StringsCompleter(OPTION_REMOVE, OPTION_FORCE));
	comp.put("IM13", StringsCompleter(OPTION_CLONE, OPTION_NAME));
	comp.put("IM99", StringsCompleter(OPTION_HELP));
	return "|IM1 IM11*|IM1 IM12*|IM1 IM13*|IM1 IM99?";

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
	return "vapp " + OPTION_CLONE + " vapp:testVapp " + OPTION_NAME + " test " + OPTION_VIM
		+ " 9a583042-cb9d-5673-cbab-56a02a91805d\n"
		+ "\tClone the virtualApp to a new vApp named test on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"

		+ "fco " + OPTION_REMOVE + " " + OPTION_FORCE + " vapp:testVapp vm:vm-123\n"
		+ "\tDelete the vapp:testVapp and the vm (moref vm-123) , forcing a poweroff if On\n\n";

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
	if (options.has(OPTION_IMPORT)) {
	    setVappImport(true);
	}
	if (options.has(OPTION_REMOVE)) {
	    setRemove(true);
	}
	if (options.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
	    setCbt(true);
	}
	if (options.has(OPTION_NAME)) {
	    this.setVappName(options.valueOf(OPTION_NAME).toString());
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
	if (options.has(OPTION_URL)) {
	    try {
		this.setUrlPath(options.valueOf(OPTION_URL).toString());
	    } catch (final MalformedURLException e) {
		throw new ParsingException(OPTION_URL, e.getLocalizedMessage());
	    }
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

	if (options.has(OPTION_NOVMDK)) {
	    this.setNoVmdk(true);
	}
	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}
	if (options.has(OPTION_POWERON)) {
	    this.setPowerOn(true);
	}

	if (options.has(OPTION_VIM)) {
	    this.vim = options.valueOf(OPTION_VIM).toString();
	}
	if (options.has(OPTION_CLONE)) {
	    this.setClone(true);
	}

    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
