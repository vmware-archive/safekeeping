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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.DisconnectCommand;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectVcenter;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class DisconnectCommandInteractive extends DisconnectCommand implements ICommandInteractive {

    private static final String OPTION_HELP = "help";
    private static final String OPTION_PSC = "psc";
    private static final String OPTION_CSP = "csp";

    public static final String DISCONNECT = "disconnect";

    static String helpGlobalSummary() {
        final DisconnectCommandInteractive info = new DisconnectCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private final String COMMAND_DESCRIPTION = "Disconnect from vSphere or VMware Cloud";

    private boolean help;
    protected boolean psc;

    protected boolean csp;

    public DisconnectCommandInteractive() {
        initialize();
        logger = Logger.getLogger(DisconnectCommandInteractive.class.getName());
    }

    @Override
    public void initialize() {
        help = false;
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
        } else {
            final ConnectionManager connetionManager = vmbk.getConnetionManager();
            if ((connetionManager != null) && connetionManager.isConnected()) {
                result = actionDisconnectInteractive(connetionManager);
            } else {
                IoFunction.showWarning(logger, Vmbk.NO_CONNECTION);
            }
        }
        return result;
    }

    private OperationStateList actionDisconnectInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        IoFunction.showInfo(logger, "Disconnecting...");

        final CoreResultActionDisconnect resultAction = actionDisconnect(connetionManager);

        switch (resultAction.getState()) {
        case ABORTED:
            IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(logger, "%s failed to exec", resultAction.getFcoToString());
            break;
        case SKIPPED:
            IoFunction.showInfo(logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                    resultAction.getReason());
            break;
        case SUCCESS:

            final List<CoreResultActionDisconnectVcenter> radv = resultAction.getSubActionDisconnectVCenters();
            IoFunction.showInfo(logger, "LookupService reports %d VimService Instance%s", radv.size(),
                    radv.size() > 1 ? "s" : "");
            int index = 0;
            for (final CoreResultActionDisconnectVcenter vim : radv) {
                ++index;
                IoFunction.showInfo(logger, "vCenter %d:", index);
                IoFunction.showInfo(logger, "\tVimService uuid: %s url: %s - Disconnected", vim.getInstanceUuid(),
                        vim.getUrl());
                IoFunction.showInfo(logger, "\tStorage Profile Service Ver.%s url: %s - Disconnected",
                        vim.getPbmVersion(), vim.getPbmUrl());
                IoFunction.showInfo(logger, "\t%s url: %s - Disconnected", vim.getVslmName(), vim.getVslmUrl());
                IoFunction.showInfo(logger, "\tVapi Service url: %s - Disconnected", vim.getVapiUrl());
            }
            IoFunction.showInfo(logger, "LookupService disconnected");
            IoFunction.showInfo(logger, "PSC %s disconnected",
                    resultAction.getSubActionDisconnectSso().getSsoEndPointUrl());

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
        final OptionSpecBuilder optionPsc = this.parser.accepts(OPTION_PSC, "Platform Service Controller");

        final OptionSpecBuilder optionCsp = this.parser.accepts(OPTION_CSP, "Cloud Service Platform");

        this.parser.mainOptions(optionHelp, optionPsc, optionCsp);
        optionHelp.forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);

    }

    @Override
    public String getCommandName() {
        return DISCONNECT;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("CO1", stringsCompleter(DISCONNECT));
        comp.put("CO11", stringsCompleter(OPTION_PSC));
        comp.put("CO12", stringsCompleter(OPTION_CSP));

        comp.put("CO99", stringsCompleter(OPTION_HELP));
        return "|CO1 CO11|CO1 CO12|CO1 CO99?";
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
        return this.COMMAND_DESCRIPTION;
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = this.parser.parse(arguments);

        setHelp(options.has(OPTION_HELP));

        this.csp = options.has(OPTION_CSP);

        this.psc = options.has(OPTION_PSC);
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
