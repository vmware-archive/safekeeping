/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
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
package com.vmware.safekeeping.cmd.command;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkCommandLine;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.control.Vmbk;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class HelpCommandInteractive extends AbstractSimpleCommandInteractive {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(HelpCommandInteractive.class.getName());

    private static final String COMMAND_DESCRIPTION = "Command Line interactive help.";
    private static boolean all;
    private static final String OPTION_HELP = "help";
    private static final String OPTION_ALL = "all";

    private static VmbkCommandLine cmdLine;

    public static final String HELP = "help";

    public static void introHelp() {
        final SafekeepingVersion ver = SafekeepingVersion.getInstance();
        System.out.printf(
                "\n" + "Online Virtual Machine Backup Tool for VMware vSphere.\n" + "Version %s   VDDK Version %s\n"
                        + "Copyright (C) 2019 VMware Inc. All rights reserved.\n"
                        + "This software comes with ABSOLUTELY NO WARRANTY. This is free software,\n"
                        + "and you are welcome to modify and redistribute it under the BSD v2 license.\n" + "\n"
                        + "Usage: %s [prep.comand]|[command] [option(s)] [target(s)]\n" + "\n",
                ver.getVersion(), ver.getVddkVersion(), ver.getProductName());
    }

    public static void showHelp() {
        if (!all) {

            for (final ICommandInteractive s : cmdLine.getCommandFunctions().values()) {
                System.out.println(String.format("%-20s\t%s", s.getCommandName(), s.helpSummary()));
            }
            System.out.println("----");
            System.out.println("!\t\t\tExecute shell command");
            System.out.println(
                    "&\t\t\tExecute external " + SafekeepingVersion.getInstance().getProductName() + " commands");
            System.out.println();
            System.out.println("[command] " + OPTION_HELP + " for command help");
        }
    }

    private VmbkParser parser;

    public HelpCommandInteractive() {
        initialize();
    }

    /**
     * @param vmbkCommandLine
     */
    public HelpCommandInteractive(final VmbkCommandLine cmdLine) {
        initialize();
        HelpCommandInteractive.cmdLine = cmdLine;
    }

    @Override
    public OperationStateList action(final Vmbk vmbk) {
        OperationStateList result = null;
        if (isHelp()) {
            try {
                this.parser.printHelpOn(System.out);
                result = new OperationStateList(OperationState.SUCCESS);
            } catch (final IOException e) {
                Utility.logWarning(logger, e);
                result = new OperationStateList(OperationState.FAILED);
            }
        } else {
            showHelp();
        }
        result = new OperationStateList(OperationState.SUCCESS);
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        final OptionSpecBuilder optionAll = this.parser.accepts(OPTION_ALL, "Show help for any available commands")
                .availableUnless(optionHelp);
        optionHelp.availableUnless(optionAll).forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return HELP;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
        comp.put("H1", stringsCompleter(HELP));
        comp.put("H11", stringsCompleter(OPTION_HELP));
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
        return COMMAND_DESCRIPTION;
    }

    @Override
    public void initialize() {

        setHelp(false);
        all = false;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = this.parser.parse(arguments);
        setHelp(options.has(OPTION_HELP));

        if (options.has(OPTION_ALL)) {
            HelpCommandInteractive.all = true;
        } else {
            HelpCommandInteractive.all = false;
        }
    }
}
