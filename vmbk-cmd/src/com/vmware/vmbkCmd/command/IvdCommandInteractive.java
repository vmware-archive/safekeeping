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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.vmbk.command.IvdCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.type.DiskFileProvisioningType;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyBoolean.PrettyBooleanValues;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class IvdCommandInteractive extends IvdCommand implements CommandInteractive {
    private static final String commandDescription = "Manage Improved Virtual Disks Virtual Disk";
    private static final String OPTION_VIM = "vim";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_LIST = "list";

    private static final String OPTION_PROMOTE = "promote";

    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_ATTACH_IVD = "attach";

    private static final String OPTION_DISK_DEVICE = "device";

    private static final String OPTION_DETACH_IVD = "detach";
    private static final String OPTION_RECONCILE = "reconcile";
    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";
    private static final String OPTION_KEEP_AFTER_DELETE_VM = "keepafterdeletevm";
    private static final String OPTION_DISABLE_RELOCATION = "disablerelocation";
    private static final String OPTION_CREATE_IVD = "create";
    private static final String OPTION_SIZE = "size";
    private static final String OPTION_DISK_TYPE = "type";
    private static final String OPTION_NAME = "name";

    private static final String OPTION_EXTEND = "extend";
    private static final String OPTION_PROFILE = "sbpmprofile";
    private static final String OPTION_RENAME = "rename";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_TAG = "tag";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_DETAIL = "detail";
    private static final String OPTION_DATASTORE = "datastore";

    static String helpGlobalSummary() {
	final IvdCommandInteractive info = new IvdCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public IvdCommandInteractive() {
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

	final OptionSpecBuilder optionPromote = this.parser.accepts(OPTION_PROMOTE,
		"Promote vm disk to Improved Virtual Disks.");

	final OptionSpecBuilder optionAttachIvd = this.parser.accepts(OPTION_ATTACH_IVD,
		"Attach Improved Virtual Disks  to Virtual Machine.");

	final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "List Improved Virtual Disks.");

	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the Improved Virtual Disks.");

	final OptionSpecBuilder optionReconcile = this.parser.accepts(OPTION_RECONCILE,
		"Reconcile Improved Virtual Disks database");
	final OptionSpecBuilder optionRename = this.parser.accepts(OPTION_RENAME, "Rename the Improved Virtual Disk.");
	final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
		"Enable disable Change Block Tracking");
	final OptionSpecBuilder optionDetachIvd = this.parser.accepts(OPTION_DETACH_IVD,
		"Detach Improved Virtual Disks from a Virtual Machine.");
	final OptionSpecBuilder optionKeepAfterDelete = this.parser.accepts(OPTION_KEEP_AFTER_DELETE_VM,
		"Enable/disable keepAfterDeleteVm property");
	final OptionSpecBuilder optionTag = this.parser.accepts(OPTION_TAG,
		"Attach, Detach and List Improved Virtual Disk Tags");
	optionTag.withRequiredArg().describedAs("attach|detach|list").ofType(TagOperationOptions.class);

	final OptionSpecBuilder optionExtend = this.parser.accepts(OPTION_EXTEND,
		"Extend the disk size. Use " + OPTION_SIZE + " for disk size");

	final OptionSpecBuilder optionDisableRelocation = this.parser.accepts(OPTION_DISABLE_RELOCATION,
		"Enable/disable keepAfterDeleteVm property");
	final OptionSpecBuilder optionCreateIvd = this.parser.accepts(OPTION_CREATE_IVD,
		"Create a new Improved Virtual Disks.");
	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");

	this.parser.mainOptions(optionReconcile, optionList, optionHelp, optionPromote, optionAttachIvd,
		optionDetachIvd, optionRemove, optionCbt, optionDisableRelocation, optionKeepAfterDelete,
		optionCreateIvd, optionRename, optionTag, optionExtend);

	optionHelp.forHelp();
	optionKeepAfterDelete.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("yes|no");
	optionDisableRelocation.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("yes|no");
	optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
	final OptionSpecBuilder optionSize = this.parser
		.accepts(OPTION_SIZE, "IVD size. Used by " + OPTION_CREATE_IVD + " and " + OPTION_EXTEND)
		.requiredIf(optionCreateIvd, optionExtend);
	optionSize.withRequiredArg().describedAs("size(k|M|G|T|P)")
		.withValuesConvertedBy(joptsimple.util.RegexMatcher.regex(PrettyNumber.regexStr));

	final OptionSpecBuilder optionDatastore = this.parser.accepts(OPTION_DATASTORE, "Datastore name.")
		.requiredIf(optionCreateIvd);
	optionDatastore.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionName = this.parser
		.accepts(OPTION_NAME, "Used by " + OPTION_CREATE_IVD + " and " + OPTION_RENAME)
		.availableIf(optionCreateIvd, optionRename);
	optionName.withRequiredArg().describedAs("name");
	final OptionSpecBuilder optionDiskType = this.parser.accepts(OPTION_DISK_TYPE,
		"Backend type  EAGER_ZEROED_THICK(t|thick) LAZY_ZEROED_THICK(z|zero), THIN (h|thin). Used by "
			+ OPTION_CREATE_IVD)
		.availableIf(optionCreateIvd);
	optionDiskType.withRequiredArg()
		.withValuesConvertedBy(joptsimple.util.RegexMatcher
			.regex("^(thick|t|zero|z|thin|h|EAGER_ZEROED_THICK|LAZY_ZEROED_THICK|THIN)"))
		.describedAs("t|z|h");// .ofType(DiskFileProvisioningType.class).
	final OptionSpecBuilder optionVim = this.parser
		.accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);

	optionVim.withRequiredArg().describedAs("vcenter");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp);

	final OptionSpecBuilder optionDiskDevice = this.parser
		.accepts(OPTION_DISK_DEVICE, "Specify the disk controller. Optional for " + OPTION_ATTACH_IVD
			+ "ctrlkey Device Key of the controller the disk will connect to." + "unit: unit disk number.")
		.requiredIf(optionAttachIvd);
	optionDiskDevice.withOptionalArg().withValuesConvertedBy(joptsimple.util.RegexMatcher.regex("^[0-9:]+$"))
		.describedAs("ctrlkey:unit|ctrlkey|:unit");
	this.parser.accepts(OPTION_PROFILE, "Set Improved Virtual Disks Storage Profile.").availableIf(optionCreateIvd);
	this.parser.accepts(OPTION_ALL, "Operation apply to any Improved Virtual Disks.").availableUnless(optionHelp,
		optionReconcile, optionRename, optionCreateIvd);
	this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
	this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableIf(optionDetachIvd,
		optionDisableRelocation, optionKeepAfterDelete, optionRemove, optionReconcile, optionRename);
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.IVD;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

	comp.put("I1", StringsCompleter(VmbkCommandLine.IVD));

	comp.put("I11", StringsCompleter(OPTION_DRYRUN, OPTION_REMOVE, OPTION_VIM));
	comp.put("I12", StringsCompleter(OPTION_LIST, OPTION_VIM, OPTION_DETAIL));
	comp.put("I13", StringsCompleter(OPTION_RECONCILE, OPTION_VIM));
	comp.put("I14", StringsCompleter(OPTION_DRYRUN, OPTION_DETACH_IVD, OPTION_VIM, OPTION_QUIET));
	comp.put("I15", StringsCompleter(OPTION_DRYRUN, OPTION_ATTACH_IVD, OPTION_DISK_DEVICE, OPTION_VIM));
	comp.put("I16", StringsCompleter(OPTION_DRYRUN, OPTION_ENABLE_CHANGED_BLOCK_TRACKING, OPTION_VIM));
	comp.put("I17", StringsCompleter(OPTION_DRYRUN, OPTION_DISABLE_RELOCATION, OPTION_VIM));
	comp.put("I18", StringsCompleter(OPTION_DRYRUN, OPTION_KEEP_AFTER_DELETE_VM, OPTION_VIM));
	comp.put("I19", StringsCompleter(OPTION_DRYRUN, OPTION_PROMOTE, OPTION_VIM, OPTION_QUIET));
	comp.put("I20", StringsCompleter(OPTION_DRYRUN, OPTION_CREATE_IVD, OPTION_SIZE, OPTION_DATASTORE, OPTION_NAME,
		OPTION_VIM, OPTION_QUIET));
	comp.put("I21", StringsCompleter(OPTION_DRYRUN, OPTION_TAG, OPTION_VIM, OPTION_QUIET));
	comp.put("I24", StringsCompleter(OPTION_DRYRUN, OPTION_EXTEND, OPTION_VIM, OPTION_QUIET));
	comp.put("I24", StringsCompleter(OPTION_DRYRUN, OPTION_RENAME, OPTION_NAME, OPTION_VIM, OPTION_QUIET));

	comp.put("I99", StringsCompleter(OPTION_HELP));
	return "|I1 I11*|I1 I12*|I1 I13*|I1 I14*|I1 I15*|I1 I16*|I1 I17*|I1 I18*|I1 I19*|I1 I20*|I1 I21*|I1 I24*|I1 I99?";
    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vm		Virtual Machine			X	X	X\n"
		+ "ivd		Improved Virtual Disks		X	X	 \n"
		+ "tag		vCenter Tag				X	 \n";

	return ret;
    }

    @Override

    public String helpExample() {
	return "ivd " + "-" + OPTION_LIST + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
		+ "\tShow any Improved Virtual Disks available on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
		+ "ivd " + "-" + OPTION_REMOVE + " ivd:testDisk\n" + "\tDelete the Improved Virtual Disks testDisk\n\n"
		+ "ivd " + "-" + OPTION_ATTACH_IVD + " 1000:2 ivd:testDisk vm:Windows2016Test\n"
		+ "\tAttach the Improved Virtual Disks testDisk to VM Windows2016Test as 2nd disk controller id 1000\n\n"
		+ "ivd " + "-" + OPTION_CREATE_IVD + " " + "-" + OPTION_NAME + " myIvdDisk " + "-" + OPTION_SIZE
		+ " 20G " + "-" + OPTION_DATASTORE + " vsanDatastore " + "-" + OPTION_DISK_TYPE + " zero \n"
		+ "\tCreate an Improved Virtual Disks name myIvdDisk type EAGER_ZEROED_THICK of size 20GB on datastore vsanDatastore\n\n";
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
	if (options.has(OPTION_ALL)) {
	    this.anyFcoOfType = FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
		    FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all);

	}
	if (options.has(OPTION_RECONCILE)) {
	    this.reconcile = true;
	}
	if (options.has(OPTION_LIST)) {

	    this.list = true;
	    if (options.nonOptionArguments().isEmpty()) {
		this.anyFcoOfType = FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all;
	    }
	}

	if (options.has(OPTION_PROMOTE)) {
	    this.promote = options.valueOf(OPTION_PROMOTE).toString();
	}
	if (options.has(OPTION_ATTACH_IVD)) {

	    this.attach = true;
	}
	if (options.has(OPTION_DETACH_IVD)) {
	    this.detach = true;
	}
	if (options.has(OPTION_REMOVE)) {
	    this.remove = true;
	}
	if (options.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
	    this.cbt = PrettyBoolean.parseBoolean(options.valueOf(OPTION_ENABLE_CHANGED_BLOCK_TRACKING));
	}
	if (options.has(OPTION_DISABLE_RELOCATION)) {
	    this.disableRelocation = PrettyBoolean.parseBoolean(options.valueOf(OPTION_DISABLE_RELOCATION));
	}
	if (options.has(OPTION_KEEP_AFTER_DELETE_VM)) {
	    this.keepAfterDeleteVm = PrettyBoolean.parseBoolean(options.valueOf(OPTION_KEEP_AFTER_DELETE_VM));
	}

	if (options.has(OPTION_CREATE_IVD)) {
	    this.create = true;
	}

	if (options.has(OPTION_TAG)) {
	    this.tag = (TagOperationOptions) options.valueOf(OPTION_TAG);
	    this.anyFcoOfType |= FirstClassObjectFilterType.noTag;
	    if (this.tag == TagOperationOptions.list) {
		if (options.nonOptionArguments().isEmpty()) {
		    this.anyFcoOfType = FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all;
		}
	    } else {
		this.anyFcoOfType |= FirstClassObjectFilterType.noTag;

	    }

	}

	if (options.has(OPTION_EXTEND)) {
	    this.extend = true;

	}
	if (options.has(OPTION_PROFILE)) {
	    final String[] profileList = options.valueOf(OPTION_PROFILE).toString().split(",");
	    this.sbpmProfileNames.addAll(Arrays.asList(profileList));

	}
	if (options.has(OPTION_RENAME)) {
	    this.rename = true;

	}

	if (options.has(OPTION_NAME)) {
	    this.name = options.valueOf(OPTION_NAME).toString();
	}
	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}
	if (options.has(OPTION_QUIET)) {
	    this.setQuiet(true);
	}
	if (options.has(OPTION_VIM)) {
	    this.vim = options.valueOf(OPTION_VIM).toString();
	}

	if (options.has(OPTION_DATASTORE)) {
	    this.datastoreName = options.valueOf(OPTION_DATASTORE).toString();
	}
	if (options.has(OPTION_DISK_DEVICE)) {
	    this.device = options.valueOf(OPTION_DISK_DEVICE).toString();
	}
	if (options.has(OPTION_SIZE)) {
	    this.size = PrettyNumber.toLong(options.valueOf(OPTION_SIZE));

	}
	if (options.has(OPTION_DISK_TYPE)) {
	    this.diskType = DiskFileProvisioningType.parse(options.valueOf(OPTION_DISK_TYPE));

	}
	if (options.has(OPTION_DETAIL)) {
	    this.detail = true;

	}
    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
