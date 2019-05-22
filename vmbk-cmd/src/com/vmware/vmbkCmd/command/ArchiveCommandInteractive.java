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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.vmbk.command.ArchiveCommand;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.FirstClassObjectType;
import com.vmware.vmbkCmd.ParsingException;
import com.vmware.vmbkCmd.VmbkCommandLine;
import com.vmware.vmbkCmd.VmbkParser;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;;

public class ArchiveCommandInteractive extends ArchiveCommand implements CommandInteractive {

    private static final String OPTION_LIST = "list";
    private static final String OPTION_CHECK = "check";

    private static final String OPTION_STATUS = "status";

    private static final String OPTION_SHOW = "show";

    private static final String OPTION_COMMIT = "commit";

    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_DETAIL = "detail";

    private static final String OPTION_QUIET = "quiet";

    private static final String OPTION_ALL = "all";
    private static final String OPTION_GENERATION = "generation";
    private static final String OPTION_DRYRUN = "dryrun";
    private static final String OPTION_PROFILE = "profile";
    private static final String OPTION_MTIME = "mtime";

    static String helpGlobalSummary() {
	final ArchiveCommandInteractive info = new ArchiveCommandInteractive();
	return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private final String commandDescription = "Archive management.";

    private boolean help;

    public ArchiveCommandInteractive() {
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
	this.parser = VmbkParser.newVmbkParser(this);// getCommandName(), this.commandDescription,
						     // helpEntities(),helpExample());
	final OptionSpecBuilder optionStatus = this.parser.accepts(OPTION_STATUS, "Show status of the archives.");

	final OptionSpecBuilder optionShow = this.parser.accepts(OPTION_SHOW,
		"show on stdout the content of the selected archive file content ");
	final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST,
		"List entity with various filters. (virtual machine, Improved Virtual Disks, vApp).");

	final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE,
		"Delete a specific generation archive or a profile.");

	final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
	final OptionSpecBuilder optionCheck = this.parser.accepts(OPTION_CHECK, "Validate the archives.");
	final OptionSpecBuilder optionCommit = this.parser.accepts(OPTION_COMMIT, "Force database data to commit.");

	this.parser.mainOptions(optionList, optionCheck, optionShow, optionRemove, optionStatus, optionCommit,
		optionHelp);

	optionHelp.forHelp();
	this.parser
		.accepts(OPTION_GENERATION,
			"Generations <id> to check " + "all - All generation " + "last  - Last generation (default) "
				+ "succeded - Only succeded ones " + " failed - Only failed ones")
		.availableUnless(optionHelp, optionCommit).withRequiredArg().describedAs("id");

	this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp, optionCommit);
	this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
	this.parser.accepts(OPTION_PROFILE, "Remove the First Class Object profile. Used with Remove")
		.availableIf(optionRemove);
	this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
		.availableUnless(optionHelp, optionCommit).withOptionalArg().ofType(FirstClassObjectType.class)
		.describedAs("vm|ivd|vapp");

	this.parser
		.accepts(OPTION_MTIME,
			"Filter by creation time +means older than dd:hh:mm  - means newer than dd:hh:mm (Default)")
		.availableIf(optionList);
	this.parser.accepts(OPTION_QUIET, "No confirmation asked.").availableUnless(optionHelp, optionList,
		optionCommit);
	return this;

    }

    @Override
    public String getCommandName() {
	return VmbkCommandLine.ARCHIVE;
    }

    @Override
    public String getPrologo() {
	return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
	comp.put("A1", StringsCompleter(VmbkCommandLine.ARCHIVE));
	comp.put("A11", StringsCompleter(OPTION_LIST, OPTION_MTIME));
	comp.put("A12", StringsCompleter(OPTION_STATUS, OPTION_ALL, OPTION_DETAIL));
	comp.put("A13", StringsCompleter(OPTION_CHECK, OPTION_DRYRUN, OPTION_ALL, OPTION_DETAIL, OPTION_GENERATION));
	comp.put("A14", StringsCompleter(OPTION_REMOVE, OPTION_DRYRUN, OPTION_ALL, OPTION_GENERATION, OPTION_PROFILE,
		OPTION_QUIET));
	comp.put("A15", StringsCompleter(OPTION_COMMIT));
	comp.put("A16", StringsCompleter(OPTION_SHOW, OPTION_GENERATION));
	comp.put("A99", StringsCompleter(OPTION_HELP));
	return "|A1 A11*|A1 A12*|A1 A13*|A1 A14*|A1 A15?|A1 A16*|A1 A99?";
    }

    @Override
    public String helpEntities() {
	final String ret = "\tEnyityType	Entity Description		uuid	name	moref\n"
		+ "\tvm		Virtual Machine			X	X	X\n"
		+ "\tivd		Improved Virtual Disks	X	X	 \n"
		+ "\tvapp		Virtual Appliance		X		 \n";
	return ret;
    }

    @Override
    public String helpExample() {
	return "archive -status -all\n\tShow the status of all archive\n\n"
		+ "archive -status vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330 -details\n\tShow the archive status with details of 3 different Vm.  1st by name. 2nd by Moref. 3rd by UUID\n\n"
		+ "archive -check -all\n\tValidate any archived object\n\n"
		+ "archive -remove vm:testVM -generation 2,4\n\tRemove TestVM generation 2 and 4 from the archive\n\n"
		+ "archive -remove vm:testVM -profile\n\tRemove TestVM Profile from the archive\n\n";
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
	if (options.has(OPTION_LIST)) {
	    this.list = true;
	    if (options.nonOptionArguments().isEmpty()) {
		this.anyFcoOfType = (FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
	    }
	}
	if (options.has(OPTION_CHECK)) {
	    this.check = true;
	}
	if (options.has(OPTION_SHOW)) {
	    final String showStr = options.valueOf(OPTION_SHOW).toString();
	    switch (showStr.toLowerCase()) {
	    case "globalprofile":
		this.show = ArchiveObjects.GlobalProfile;
		break;
	    case "fcoprofile":
		this.show = ArchiveObjects.FcoProfile;
		break;
	    case "generationprofile":
		this.show = ArchiveObjects.GenerationProfile;
		break;
	    case "vmxfile":
		this.show = ArchiveObjects.VmxFile;
		break;
	    case "reportfile":
		this.show = ArchiveObjects.ReportFile;
		break;
	    case "md5file":
		this.show = ArchiveObjects.Md5File;
		break;
	    default:
		this.show = ArchiveObjects.none;
		break;
	    }

	}
	if (options.has(OPTION_STATUS)) {

	    this.status = true;
	}

	if (options.has(OPTION_COMMIT)) {

	    this.commit = true;
	}
	if (options.has(OPTION_REMOVE)) {

	    this.remove = true;
	}

	if (options.has(OPTION_DETAIL)) {
	    this.setDetail(true);
	}

	if (options.has(OPTION_QUIET)) {
	    this.setQuiet(true);
	}

	if (options.has(OPTION_ALL)) {
	    this.anyFcoOfType = FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
		    FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
	}
	String checkGenerationIdStr = null;
	if (options.has(OPTION_GENERATION)) {
	    checkGenerationIdStr = options.valueOf(OPTION_GENERATION).toString();
	    switch (checkGenerationIdStr.toLowerCase()) {
	    case "all":
		this.generationId.add(-1);
		break;
	    case "last":
		this.generationId.add(-4);
		break;
	    case "succeded":
		this.generationId.add(-2);
		break;
	    case "failed":
		this.generationId.add(-3);
		break;
	    default:
		final String[] gens = checkGenerationIdStr.split(",");
		if (gens.length > 0) {
		    this.generationId = new LinkedList<>();
		    for (final String idStr : gens) {
			if (StringUtils.isNotEmpty(idStr)) {
			    if (StringUtils.isNumeric(idStr)) {
				this.generationId.add(Integer.parseInt(idStr));
			    } else {
				throw new ParsingException(OPTION_GENERATION, checkGenerationIdStr);
			    }
			}
		    }
		} else {
		    throw new ParsingException(OPTION_GENERATION, checkGenerationIdStr);
		}
	    }
	}
	if (options.has(OPTION_DRYRUN)) {
	    this.setDryRun(true);
	}

	if (options.has(OPTION_PROFILE)) {
	    this.profile = true;
	}

	if (options.has(OPTION_MTIME)) {
	    if (options.valueOf(OPTION_MTIME).toString().length() > 0) {
		this.isMtime = true;
		this.mTimeString = options.valueOf(OPTION_MTIME).toString();

		this.signMtime = (this.mTimeString.startsWith("-")) ? '-' : '+';
		if (this.signMtime != null) {
		    this.mTimeString = this.mTimeString.substring(1);
		} else {
		    this.signMtime = '-';
		}
		final SimpleDateFormat format = new SimpleDateFormat("dd:hh:mm");
		Date date;
		try {
		    date = format.parse(this.mTimeString);
		} catch (final ParseException e) {
		    throw new ParsingException(OPTION_MTIME, e.toString());
		}
		this.mtime = date.getTime();
	    }

	}
    }

    public void setHelp(final boolean help) {
	this.help = help;
    }

}
