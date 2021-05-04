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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.settings.CmdGlobalSettings;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractConnectCommand;
import com.vmware.safekeeping.core.command.options.CoreCspConnectOptions;
import com.vmware.safekeeping.core.command.options.CorePscConnectOptions;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectVcenter;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.soap.ConnectionManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.RegexMatcher;

public class ConnectCommandInteractive extends AbstractConnectCommand implements ICommandInteractive {

    private static final String OPTION_HELP = "help";
    private static final String OPTION_PSC = "psc";
    private static final String OPTION_USER = "username";
    private static final String OPTION_PASSWORD = "password";
    private static final String OPTION_BASE64 = "base64";
    private static final String OPTION_CSP = "csp";
    private static final String OPTION_PORT = "port";

    private static final String OPTION_EXTENSION = "extension";

    public static final String CONNECT = "connect";

    private static final String COMMAND_DESCRIPTION = "Connect to vSphere or VMware Cloud";

    static String helpGlobalSummary() {
        final ConnectCommandInteractive info = new ConnectCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;

    private String password;

    public ConnectCommandInteractive() {
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
                Utility.logWarning(logger, e);
                result = new OperationStateList(OperationState.FAILED);
            }
        } else {
            final ConnectionManager connetionManager = new ConnectionManager(getOptions(),
                    CmdGlobalSettings.getRepositoryTarget());
            vmbk.setConnetionManager(connetionManager);
            result = actionConnectInteractive(connetionManager);
        }
        return result;

    }

    private OperationStateList actionConnectInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        IoFunction.showInfo(logger, "Connecting...");
        final CoreResultActionConnectSso resultActionSso = connetionManager.connectSso(this.password,
                getOptions().isBase64());

        switch (resultActionSso.getState()) {
        case ABORTED:
            IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(logger, resultActionSso.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(logger, " skip - Reason: %s", resultActionSso.getReason());
            break;
        case SUCCESS:
            IoFunction.showInfo(logger, "Connected to Platform Service Controller: %s",
                    resultActionSso.getSsoEndPointUrl());

            final CoreResultActionConnect resultAction = actionConnect(connetionManager);

            switch (resultAction.getState()) {
            case ABORTED:
                IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
                break;

            case FAILED:
                IoFunction.showWarning(logger, resultActionSso.getReason());
                break;
            case SKIPPED:
                IoFunction.showInfo(logger, " skip - Reason: %s", resultAction.getReason());
                break;
            case SUCCESS:
                final ConcurrentDoublyLinkedList<CoreResultActionConnectVcenter> vims = resultAction
                        .getSubActionConnectVCenters();
                IoFunction.showInfo(logger, "LookupService reports %d VimService Instance%s", vims.size(),
                        vims.size() > 1 ? "s" : "");
                int index = 0;
                for (final CoreResultActionConnectVcenter vim : vims) {
                    ++index;
                    IoFunction.showInfo(logger, "vCenter %d:", index);
                    IoFunction.showInfo(logger, "\tVimService uuid: %s url: %s", vim.getInstanceUuid(), vim.getUrl());
                    IoFunction.showInfo(logger, "\t\t%s - Api Version: %s - Platform: %s", vim.getName(), vim.getApi(),
                            vim.getCloudPlatform().toString());
                    IoFunction.showInfo(logger, "\tStorage Profile Service Ver.%s url: %s", vim.getPbmVersion(),
                            vim.getPbmUrl());
                    IoFunction.showInfo(logger, "\t%s url: %s", vim.getVslmName(), vim.getVslmUrl());
                    IoFunction.showInfo(logger, "\tVapi Service url: %s", vim.getVapiUrl());
                }
                break;
            default:
                break;
            }
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
        final OptionSpecBuilder optionPsc = this.parser.accepts(OPTION_PSC, "Platform Service Controller URL");
        optionPsc.withRequiredArg().ofType(String.class).describedAs("hostname or FQDN");
        final OptionSpecBuilder optionPort = this.parser.accepts(OPTION_PORT, "Platform Service Controller port number")
                .availableIf(OPTION_PSC);
        optionPort.withRequiredArg().ofType(Integer.class).describedAs("port");
        final OptionSpecBuilder optionCsp = this.parser.accepts(OPTION_CSP, "Cloud Service Platform URL");
        optionCsp.withRequiredArg().ofType(String.class).describedAs("hostname or FQDN");

//	final OptionSpecBuilder optionVapi = this.parser.accepts(OPTION_VAPI, "VAPI Server URL")
//		.availableIf(OPTION_CSP);
//	optionVapi.withRequiredArg().ofType(String.class).describedAs("hostname or FQDN");
//
//	final OptionSpecBuilder optionToken = this.parser.accepts(OPTION_TOKEN, "CSP User token")
//		.availableIf(OPTION_CSP);
//	optionToken.withRequiredArg().ofType(String.class).describedAs("token");
        final OptionSpecBuilder optionPassword = this.parser
                .accepts(OPTION_PASSWORD, "Password as clear text or base64").availableIf(OPTION_PSC);
        optionPassword.withRequiredArg().ofType(String.class).describedAs("password");
        final OptionSpecBuilder optionUsername = this.parser.accepts(OPTION_USER, "Username used for the connection")
                .availableIf(OPTION_PSC);
        optionUsername.withRequiredArg().ofType(String.class).describedAs("name");

        final OptionSpecBuilder optionBase64 = this.parser.accepts(OPTION_BASE64, "Password in Base64");
        optionBase64.availableIf(OPTION_PASSWORD);
        final OptionSpecBuilder optionExtension = this.parser.accepts(OPTION_EXTENSION, "Manage vCenter extension");

        optionExtension.withRequiredArg()
                .withValuesConvertedBy(
                        RegexMatcher.regex("register|remove|update|forceupdate", Pattern.CASE_INSENSITIVE))
                .describedAs("register|remove|update|forceUpdate");

        this.parser.mainOptions(optionHelp, optionPsc, optionCsp, optionExtension);
        optionHelp.forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return CONNECT;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("CO1", stringsCompleter(CONNECT));
        comp.put("CO11", stringsCompleter(OPTION_PSC, OPTION_USER, OPTION_PASSWORD, OPTION_BASE64));
        comp.put("CO12", stringsCompleter(OPTION_CSP, OPTION_USER, OPTION_PASSWORD, OPTION_BASE64));

        comp.put("CO99", stringsCompleter(OPTION_HELP));
        return "|CO1 CO11*|CO1 CO12*|CO1 CO99?";
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
        return ConnectCommandInteractive.COMMAND_DESCRIPTION;
    }

    @Override
    public void initialize() {
        help = false;
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
        } else {
            setHelp(false);
            if (optionSet.has(OPTION_CSP)) {
                this.options = new CoreCspConnectOptions();
                getOptions().setAuthServer(optionSet.valueOf(OPTION_CSP).toString());
            } else if (optionSet.has(OPTION_PSC)) {
                this.options = new CorePscConnectOptions(CmdGlobalSettings.getSsoServer(),
                        CmdGlobalSettings.getUsername());
                getOptions().setAuthServer(optionSet.valueOf(OPTION_PSC).toString());
//            } else if (optionSet.has(OPTION_EXTENSION)) {
//                getOptions().setExtension(
//                        new ExtensionOptions(ExtensionManagerOperation.parse(optionSet.valueOf(OPTION_EXTENSION))));
            } else {
                this.options = new CorePscConnectOptions(CmdGlobalSettings.getSsoServer(),
                        CmdGlobalSettings.getUsername());
            }
            if (optionSet.has(OPTION_PASSWORD)) {
                this.password = optionSet.valueOf(OPTION_PASSWORD).toString();
            } else {
                this.password = CmdGlobalSettings.getPassword();
            }
            if (optionSet.has(OPTION_USER)) {
                ((CorePscConnectOptions) getOptions()).setUser(optionSet.valueOf(OPTION_USER).toString());
            }

            if (optionSet.has(OPTION_PORT) && !getOptions().isAuthServerAnURL()) {
                ((CorePscConnectOptions) getOptions())
                        .setPort(Integer.parseInt(optionSet.valueOf(OPTION_PORT).toString()));
            }

//	if (options.has(OPTION_TOKEN)) {
//	    this.getOptions().setRefreshToken(options.valueOf(OPTION_TOKEN).toString());
//	}
//	if (options.has(OPTION_VAPI)) {
//	    this.getOptions().setVapiServer(options.valueOf(OPTION_VAPI).toString());
//	}

            if (optionSet.has(OPTION_BASE64)) {
                getOptions().setBase64(true);
            }
            // automatically connect target
            getOptions().setConnectDefaultTarget(true);
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
