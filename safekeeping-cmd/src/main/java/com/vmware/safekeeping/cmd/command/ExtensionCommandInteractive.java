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

import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractExtensionCommand;
import com.vmware.safekeeping.core.command.options.CoreExtensionOptions;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionExtension;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.ExtensionManagerOperation;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class ExtensionCommandInteractive extends AbstractExtensionCommand implements ICommandInteractive {

    private static final String OPTION_HELP = "help";

    private static final String COMMAND_DESCRIPTION = "Manage vcenter extension";
    private static final String OPTION_REGISTER = "register";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_UPDATE = "update";

    private static final String OPTION_CHECK = "check";
    private static final String OPTION_FORCE = "force";

    public static final String EXTENSION = "extension";

    static String helpGlobalSummary() {
        final ExtensionCommandInteractive info = new ExtensionCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    public ExtensionCommandInteractive() {
        setHelp(false);
    }

    @Override
    public OperationStateList action(final Vmbk vmbk)
            throws CoreResultActionException, SafekeepingException, SafekeepingConnectionException {
        OperationStateList result = null;
        if (isHelp()) {
            try {
                this.parser.printHelpOn(System.out);
                result = new OperationStateList(OperationState.SUCCESS);
            } catch (final IOException e) {
                Utility.logWarning(this.logger, e);
                result = new OperationStateList(OperationState.FAILED);
            }
        } else {
            final ConnectionManager connetionManager = vmbk.getConnetionManager();
            if ((connetionManager != null) && connetionManager.isConnected()) {

                result = actionExtensionInteractive(connetionManager);
            }
        }
        return result;

    }

    private OperationStateList actionExtensionInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {

        final OperationStateList result = new OperationStateList();
        final CoreResultActionExtension resultAction = actionExtension(connetionManager, null);

        switch (resultAction.getState()) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(this.logger, "Failed  - Reason: %s", resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "Skip - Reason: %s", resultAction.getReason());
            break;
        case SUCCESS:
            if (resultAction.getExtension() == null) {
                IoFunction.showInfo(this.logger, "No Extension available");
            } else {
                IoFunction.showInfo(this.logger, "Extension ver:%s  - Health Information Available: %s",
                        resultAction.getExtension().getVersion(),
                        (resultAction.getExtension().getHealthInfo() != null) ? "Yes" : "No");
            }
            IoFunction.showInfo(this.logger, "Extension operation %s success", getOptions().getExtensionOperation());

            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");

        final OptionSpecBuilder optionRegister = this.parser.accepts(OPTION_REGISTER, "Register the vCenter extension");
        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the vCenter extension");
        final OptionSpecBuilder optionUpdate = this.parser.accepts(OPTION_UPDATE, "Update the vCenter extension");

        final OptionSpecBuilder optionCheck = this.parser.accepts(OPTION_CHECK, "Check the vCenter extension");

        this.parser.accepts(OPTION_FORCE, "Force the operation.");
        this.parser.mainOptions(optionHelp, optionRegister, optionRemove, optionUpdate, optionCheck);
        optionHelp.forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return EXTENSION;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("CO1", stringsCompleter(OPTION_REGISTER));
        comp.put("CO2", stringsCompleter(OPTION_UPDATE));
        comp.put("CO99", stringsCompleter(OPTION_HELP));
        return "CO1 CO99?";
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
        return ExtensionCommandInteractive.COMMAND_DESCRIPTION;
    }

    @Override
    public void initialize() {
        this.help = false;
        setOptions(new CoreExtensionOptions());
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet optionSet = this.parser.parse(arguments);
        if (optionSet.has(OPTION_HELP)) {
            setHelp(true);
        } else if (optionSet.has(OPTION_REGISTER)) {
            getOptions().setExtensionOperation(ExtensionManagerOperation.REGISTER);
        } else if (optionSet.has(OPTION_REMOVE)) {
            getOptions().setExtensionOperation(ExtensionManagerOperation.REMOVE);
        } else if (optionSet.has(OPTION_CHECK)) {
            getOptions().setExtensionOperation(ExtensionManagerOperation.CHECK);
        } else if (optionSet.has(OPTION_UPDATE)) {
            if (optionSet.has(OPTION_FORCE)) {
                getOptions().setExtensionOperation(ExtensionManagerOperation.FORCE_UPDATE);
            } else {
                getOptions().setExtensionOperation(ExtensionManagerOperation.UPDATE);
            }
        } else {
            throw new ParsingException("No Action specified");
        }
        getOptions().setForce(optionSet.has(OPTION_FORCE));
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
