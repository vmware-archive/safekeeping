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

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.jvix.VddkVersion;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractVersionCommand;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionVddkVersions;
import com.vmware.safekeeping.core.command.results.CoreResultActionVersion;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VersionCommandInteractive extends AbstractVersionCommand implements ICommandInteractive {
    private static final String COMMAND_DESCRIPTION = "Return Vmbk and Vddk Version.";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_LIST_VDDK = "list";

    public static final String VERSION = "version";

    static String helpGlobalSummary() {
        final VersionCommandInteractive info = new VersionCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private boolean help;

    private VmbkParser parser;

    public VersionCommandInteractive() {
        initialize();
    }

    @Override
    public OperationStateList action(final Vmbk vmbk) throws CoreResultActionException {
        OperationStateList result = null;
        if (isHelp()) {
            try {
                this.parser.printHelpOn(System.out);
                result = new OperationStateList(OperationState.SUCCESS);
            } catch (final IOException e) {
                Utility.logWarning(logger, e);
                result = new OperationStateList(OperationState.FAILED);
            }
        } else if (this.vddkList) {
            result = actionGetVddkVersionsInteractive();
        } else {
            result = actionGetVersionInteractive();
        }
        return result;
    }

    private OperationStateList actionGetVddkVersionsInteractive() throws CoreResultActionException {

        final OperationStateList result = new OperationStateList();
        final CoreResultActionVddkVersions resultAction = actionGetVddkVersions();

        switch (resultAction.getState()) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(this.logger, "Failed to exec");
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "Skip - Reason: %s", resultAction.getReason());
            break;
        case SUCCESS:
            IoFunction.showInfo(this.logger, "VDDK Versions available:");
            for (final VddkVersion vddk : resultAction.getVersions()) {
                if (vddk.getVersion().equals(resultAction.getActiveVersion().getVersion())) {
                    IoFunction.showInfo(this.logger, vddk.getExtendedVersion() + " (*)");
                } else {
                    IoFunction.showInfo(this.logger, vddk.getExtendedVersion());
                }
            }
            break;
        default:
            break;
        }
        return result;
    }

    private OperationStateList actionGetVersionInteractive() throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionVersion resultAction = actionGetVersion();

        switch (resultAction.getState()) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(this.logger, "Failed to exec");
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "Skip - Reason: %s", resultAction.getReason());
            break;
        case SUCCESS:
            IoFunction.showInfo(this.logger, resultAction.getVersion().getExtendedVersion());
            IoFunction.showInfo(this.logger, resultAction.getServerInfo().getExtendedVersion());
            IoFunction.showInfo(this.logger, resultAction.getJavaRuntime());

            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        this.parser.accepts(OPTION_LIST_VDDK, "List VDDK versions available.");
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        optionHelp.forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return VERSION;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
        comp.put("Ver1", stringsCompleter(VERSION));
        comp.put("Ver11", stringsCompleter(OPTION_HELP));
        return "|Ver1 Ver11?";
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbkCmd.command.CommandCmd#helpEntities()
     */
    @Override
    public String helpEntities() {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbkCmd.command.CommandCmd#helpExample()
     */
    @Override
    public String helpExample() {

        return null;
    }

    @Override
    public String helpSummary() {
        return COMMAND_DESCRIPTION;
    }

//    @Override
//    public void initialize() {
//	this.setHelp(false);
//    }

    @Override
    public void initialize() {
        this.options = new CoreBasicCommandOptions();

    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = this.parser.parse(arguments);
        this.vddkList = (options.has(OPTION_LIST_VDDK));
        setHelp(options.has(OPTION_HELP));
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
