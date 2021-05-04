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
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.ext.command.AbstractTestCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTest;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.enums.VddkTransportMode;
import com.vmware.safekeeping.core.util.TestDiskInfo;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class TestCommandInteractive extends AbstractTestCommand implements ICommandInteractive {
    private static final String COMMAND_DESCRIPTION = "Import and Copy First Class Objects.";

    private static final String OPTION_VIM = "vim";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_TRANSPORT = "transport";

    private static final String OPTION_ALL = "all";

    public static final String TEST = "test";

    static String helpGlobalSummary() {
        final TestCommandInteractive info = new TestCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;

    private final List<String> targetList;

    public TestCommandInteractive() {
        setHelp(false);
        this.targetList = new LinkedList<>();
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
            if (connetionManager.isConnected()) {
                result = actionFcoTestInteractive(connetionManager);
            } else {
                IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION);
            }
        }
        return result;
    }

    private OperationStateList actionFcoTestInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        List<CoreResultActionTest> resultActions = actionFcoTest(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionTest resultAction : resultActions) {

                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showWarning(this.logger, " Failed to set CBT-  Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.println();
                    IoFunction.showInfo(this.logger, "Metadata:");
                    IoFunction.showInfo(this.logger, "%s", resultAction.getDumpDDBs());
                    IoFunction.println();
                    final TestDiskInfo info = resultAction.getInfo();
                    IoFunction.showInfo(this.logger, "BiosGeo - cylinders: %d heads: %d sectors: %d",
                            info.getBiosGeo().getCylinders(), info.getBiosGeo().getHeads(),
                            info.getBiosGeo().getSectors());

                    IoFunction.showInfo(this.logger, "PhysGeo - cylinders: %d heads: %d sectors: %d",
                            info.getPhysGeo().getCylinders(), info.getPhysGeo().getHeads(),
                            info.getPhysGeo().getSectors());

                    IoFunction.showInfo(this.logger, "Capacity           :  " + info.getCapacityInSectors());
                    IoFunction.showInfo(this.logger, "adapterType        :  " + info.getAdapterType());
                    IoFunction.showInfo(this.logger, "numLinks           :  " + info.getNumLinks());
                    IoFunction.showInfo(this.logger, "parentFileNameHint :  " + info.getParentFileNameHint());
                    IoFunction.println();
                    IoFunction.showInfo(this.logger, "Data:" + resultAction.getDumpDataContent().toString());

                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                    EntityType.ImprovedVirtualDisk }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");

        final OptionSpecBuilder optionTransport = this.parser
                .accepts(OPTION_TRANSPORT, "Prefered Transport Mode (nbd,nbdssl,san,hotadd).")
                .availableUnless(optionHelp);
        optionTransport.withRequiredArg().ofType(VddkTransportMode.class).describedAs("nbd|nbdssl|san|hotadd");

        this.parser.mainOptions(optionHelp);

        optionHelp.forHelp();
        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
        optionVim.withRequiredArg().describedAs("vcenter");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionHelp);

        this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
                .availableUnless(optionHelp).withOptionalArg().ofType(FirstClassObjectType.class)
                .describedAs("vm|ivd|vapp");
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return TEST;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
        comp.put("TST1", stringsCompleter(TEST));

        comp.put("TST11", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_TRANSPORT, OPTION_VIM));

        comp.put("TST99", stringsCompleter(OPTION_HELP));
        return "|TST1 TST11*|TST1 TST99?";

    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        return "EnyityType Entity Description      uuid    name    moref\n"
                + "vm         Virtual Machine         X       X       X\n"
                + "ivd        Improved Virtual Disks  X       X\n" + "vapp       Virtual Appliance       X\n";
    }

    @Override
    public String helpExample() {
        return "";

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
        this.parser.accepts(OPTION_HELP, "Help");
        if (options.has(OPTION_DRYRUN)) {
            getOptions().setDryRun(true);
        }
        if (options.has(OPTION_TRANSPORT)) {
            getOptions().setTransportMode(options.valueOf(OPTION_TRANSPORT).toString());
            final String transportCheck = Utility.checkTransportMode(getOptions().getTransportMode());
            if (StringUtils.isNotEmpty(transportCheck)) {
                throw new ParsingException(OPTION_TRANSPORT, transportCheck);
            }
        }
        if (options.has(OPTION_ALL)) {
            getOptions().setAnyFcoOfType(FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
                    FirstClassObjectFilterType.any | FirstClassObjectFilterType.all));
        }
        if (options.has(OPTION_VIM)) {
            getOptions().setVim(options.valueOf(OPTION_VIM).toString());
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }

}
