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
package com.vmware.safekeeping.cmd.command;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.ext.command.K8sCommand;
import com.vmware.safekeeping.core.soap.ConnectionManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class K8sCommandInteractive extends K8sCommand implements ICommandInteractive {

    private static final String COMMAND_DESCRIPTION = "Manage Kubernetes.";

    private static final String OPTION_LIST = "list";
    private static final String OPTION_HELP = "help";

    public static final String K8S = "k8s";

    static String helpGlobalSummary() {
        final K8sCommandInteractive info = new K8sCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private final List<String> targetList;
    private VmbkParser parser;

    private boolean help;

    public K8sCommandInteractive() {
        super();
        setHelp(false);
        this.targetList = new LinkedList<>();
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
            final ConnectionManager connetionManager = vmbk.getConnetionManager();
            if (((connetionManager == null) || !connetionManager.isConnected())) {
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION));
            }
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);

        final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "Kubernetes list.");

        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        optionHelp.forHelp();

        this.parser.mainOptions(optionList, optionHelp);

        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return K8S;
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("K1", stringsCompleter(K8S));

        comp.put("K11", stringsCompleter(OPTION_LIST));

        comp.put("V18", stringsCompleter(OPTION_HELP));

        return "|K1 K11*|K1 V18?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
                + "vm		Virtual Machine			X	X	X\n" + "tag		vCenter Tag				X	 \n";

        return ret;
    }

    @Override
    public String helpExample() {
        return K8S + " " + OPTION_LIST + "\n" + "\tShow any Kubernets domain\n\n";
    }

    @Override
    public String helpSummary() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {

        final OptionSet options = parseArguments(this.parser, arguments, getOptions());
        setHelp(options.has(OPTION_HELP));
        try {
            if (options.has(OPTION_LIST)) {
                this.list = true;
                if (options.nonOptionArguments().isEmpty()) {
                    getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vm | FirstClassObjectFilterType.all);
                }
            }

        } catch (final Exception e) {
            IoFunction.showInfo(this.logger, e.getMessage());
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
