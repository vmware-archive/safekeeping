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
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.Vmbk;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class QuitCommandInteractive extends AbstractSimpleCommandInteractive {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(QuitCommandInteractive.class.getName());

    private static final String COMMAND_DESCRIPTION = "Quit Interactive mode.";
    private static final String OPTION_HELP = "help";

    public static final String QUIT = "quit";

    static String helpGlobalSummary() {
        final QuitCommandInteractive info = new QuitCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    public QuitCommandInteractive() {
        initialize();
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
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        optionHelp.forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return QUIT;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
        comp.put("U1", stringsCompleter(QUIT));
        comp.put("U11", stringsCompleter(OPTION_HELP));
        return "|U1 U11?";
    }

    @Override
    public String helpEntities() {
        return "";
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
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = this.parser.parse(arguments);
        setHelp(options.has(OPTION_HELP));

    }

}
