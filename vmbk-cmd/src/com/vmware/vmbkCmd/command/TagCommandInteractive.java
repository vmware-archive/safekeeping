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

import com.vmware.vmbk.command.TagCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.FirstClassObjectType;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class TagCommandInteractive extends TagCommand implements CommandInteractive {

    private static String commandDescription = "Manage tagging.";
    private static final String OPTION_VIM = "vim";

    private static final String OPTION_QUIET = "quiet";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_TAG_ATTACH = "attach";

    private static final String OPTION_CATEGORY = "category";

    private static final String OPTION_TAG_CARDINALITY = "cardinality";

    private static final String OPTION_TAG_DETACH = "detach";

    // private static final String OPTION_TAG_LIST_CATEGORY = "listcategory";

    private static final String OPTION_TAG_LIST = "list";

    private static final String OPTION_REMOVE_TAG = "remove";

    private static final String OPTION_ALL = "all";

    private static final String OPTION_DESCRIPTION = "description";

    private static final String OPTION_TAG_ASSOCIABLE_TYPE = "entity";

    private static final String OPTION_CREATE_TAG = "create";

    private static final String OPTION_TAG_NAME = "tag";

    static String helpGlobalSummary() {
	final TagCommandInteractive info = new TagCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public TagCommandInteractive() {
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
	optionHelp.isForHelp();
	final OptionSpecBuilder optionTagList = this.parser.accepts(OPTION_TAG_LIST, "List available tags.");
	optionTagList.withOptionalArg().describedAs("tag|category").ofType(TagListOptions.class)
		.defaultsTo(TagListOptions.tag);

	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE_TAG,
		"Remove specified tag(s) or category.");

	final OptionSpecBuilder optionCreate = this.parser.accepts(OPTION_CREATE_TAG, "Create a new tag or category.");
	final OptionSpecBuilder optionAttachTag = this.parser.accepts(OPTION_TAG_ATTACH,
		"attach one or more tags to any specified entities.");
	final OptionSpecBuilder optionDetachTag = this.parser.accepts(OPTION_TAG_DETACH,
		"detach one or more tags to any specified entities.");

	this.parser.mainOptions(optionHelp, optionTagList, optionRemove, optionAttachTag, optionCreate,
		optionDetachTag);

	/*
	 * Option name
	 */
	final OptionSpecBuilder optionTagName = this.parser.accepts(OPTION_TAG_NAME, "Name associated to a new Tag")
		.availableIf(optionCreate);
	optionTagName.withRequiredArg().describedAs("tag");
	/*
	 * Option category
	 */
	final OptionSpecBuilder optionCategory = this.parser
		.accepts(OPTION_CATEGORY, "Category for a new tag. Mandatory option for " + OPTION_CREATE_TAG + " .")
		.availableIf(optionCreate, optionRemove);
	optionCategory.withRequiredArg().describedAs("category");
	/*
	 * Option description
	 */
	final OptionSpecBuilder optionDescription = this.parser
		.accepts(OPTION_DESCRIPTION, "Description for a new tag or category.").availableIf(optionCreate);
	optionDescription.withRequiredArg().describedAs("description");

	/*
	 * Option cardinality
	 */
	final OptionSpecBuilder optiontagCardinality = this.parser.accepts(OPTION_TAG_CARDINALITY,
		"Cardinailty of a new tag category.");
	optiontagCardinality.availableIf(optionCreate).withRequiredArg().ofType(TagCardinalityOptions.class)
		.describedAs("single|multiple");
	/*
	 * Option OPTION_TAG_ASSOCIABLE_TYPE
	 */
	final OptionSpecBuilder optiontagTagAssociableType = this.parser.accepts(OPTION_TAG_ASSOCIABLE_TYPE,
		"Entities where the new tag category can be applied."
			+ "(Cluster, Datacenter, Datastore, DatastoreCluster, DistributedPortGroup, DistributedSwitch, Folder, ResourcePool, VApp, VirtualPortGroup, VirtualMachine)");
	optiontagTagAssociableType.availableIf(optionCreate).withRequiredArg().ofType(EntityType.class)
		.withValuesSeparatedBy(",").describedAs("entity,..,entity");

	/*
	 * Option OPTION_DRYRUN
	 */
	this.parser.accepts(OPTION_DRYRUN, "Do nothing really.").availableUnless(optionHelp, optionTagList);
	/*
	 * Option OPTION_ALL
	 */
	this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
		.availableIf(optionAttachTag, optionDetachTag).withOptionalArg().ofType(FirstClassObjectType.class)
		.describedAs("vm|ivd|vapp");
	/*
	 * Option OPTION_QUIET
	 */
	this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableIf(optionAttachTag, optionDetachTag,
		optionRemove);

//	optionRemove.withRequiredArg().describedAs("tag|category").ofType(TagListOptions.class);
//	optionCreate.withRequiredArg().describedAs("tag|category").ofType(TagListOptions.class);
	return this;
    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.TAG;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

	comp.put("T1", StringsCompleter(VmbkCommandLine.TAG));

	comp.put("T11", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REMOVE_TAG, OPTION_VIM, OPTION_QUIET));
//	comp.put("T12", StringsCompleter(OPTION_DRYRUN, OPTION_CREATE_TAG_CATEGORY, OPTION_NAME, OPTION_DESCRIPTION,
//		OPTION_TAG_CARDINALITY, OPTION_TAG_ASSOCIABLE_TYPE, OPTION_VIM));
//	comp.put("T13", StringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_NAME, OPTION_REMOVE_TAG_CATEGORY,
//		OPTION_VIM, OPTION_QUIET));

	comp.put("T14", StringsCompleter(OPTION_TAG_LIST, OPTION_VIM));

	comp.put("T15", StringsCompleter(OPTION_DRYRUN, OPTION_CREATE_TAG, OPTION_TAG_NAME, OPTION_DESCRIPTION,
		OPTION_CATEGORY, OPTION_VIM));
	comp.put("T16", StringsCompleter(OPTION_DRYRUN, OPTION_TAG_ATTACH));
	comp.put("T17", StringsCompleter(OPTION_DRYRUN, OPTION_TAG_DETACH, OPTION_QUIET));
	comp.put("T19", StringsCompleter(OPTION_HELP));
//	comp.put("T18", StringsCompleter(OPTION_TAG_LIST_CATEGORY, OPTION_VIM));
	return "|T1 T11*|T1 T14*|T1 T15*|T1 T16*|T1 T17*|T1 T19?";
    }

    @Override
    public String helpEntities() {
	final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
		+ "vm		Virtual Machine			X	X	X\n"
		+ "ivd		Improved Virtual Disks		X	X	 \n"
		+ "vapp		Virtual Appliance		X		 \n";
	return ret;
    }

    @Override
    public String helpExample() {
	return getCommandName() + " -" + OPTION_TAG_LIST + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
		+ "\tShow any tag with asscociated entities available on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
		+ getCommandName() + " -" + OPTION_CREATE_TAG + " category -" + OPTION_TAG_NAME + " myTag -"
		+ OPTION_DESCRIPTION + " \"my personal VMs\" -" + OPTION_TAG_CARDINALITY + " single -"
		+ OPTION_TAG_ASSOCIABLE_TYPE + " VirtualMachine \n"
		+ "\tCreate a new tag called MyTag and make it assignable to any VM\n\n" + getCommandName() + " -"
		+ OPTION_REMOVE_TAG + " category -" + OPTION_TAG_NAME + " myOldCategory"
		+ "\n\tDelete the category with all associated tags\n\n" + getCommandName() + " -" + OPTION_TAG_ATTACH
		+ " tag:myTag vm:myVM vm:myserver" + "\n\tAttach tag 'MyTag' to VM 'myVm' and 'myServer' \n\n";
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
	if (options.has(OPTION_TAG_LIST)) {
	    this.list = (TagListOptions) options.valueOf(OPTION_TAG_LIST);
	}

	if (options.has(OPTION_CREATE_TAG)) {
	    this.create = true;
	}
	if (options.has(OPTION_REMOVE_TAG)) {
	    this.remove = true;
	}
	if (options.has(OPTION_TAG_ATTACH)) {
	    this.attach = true;
	}
	if (options.has(OPTION_TAG_DETACH)) {
	    this.detach = true;
	}

	if (options.has(OPTION_TAG_ASSOCIABLE_TYPE)) {
	    this.associableTypes = (List<EntityType>) options.valuesOf(OPTION_TAG_ASSOCIABLE_TYPE);
	}

	if (options.has(OPTION_TAG_CARDINALITY)) {
	    this.cardinality = (TagCardinalityOptions) options.valueOf(OPTION_TAG_CARDINALITY);
	}
	if (options.has(OPTION_TAG_NAME)) {
	    this.tagName = options.valueOf(OPTION_TAG_NAME).toString();
	}
	if (options.has(OPTION_ALL)) {
	    this.anyFcoOfType = FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
		    FirstClassObjectFilterType.tagElement | FirstClassObjectFilterType.all);
	}
	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}
	if (options.has(OPTION_DESCRIPTION)) {
	    this.description = options.valueOf(OPTION_DESCRIPTION).toString();

	}
	if (options.has(OPTION_CATEGORY)) {
	    this.assetCategory = options.valueOf(OPTION_CATEGORY).toString();
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
