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

import com.vmware.safekeeping.cmd.report.VirtualBackupInteractive;
import com.vmware.safekeeping.cmd.report.VirtualBackupVappInteractive;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.VirtualBackupCommand;
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VirtualBackupCommandInteractive extends AbstractCommandWithOptions implements ICommandInteractive {

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_ALL = "all";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_THREADS = "threads";

    private static final String COMMAND_DESCRIPTION = "Virtual Backup any specified Entities (virtual machine, Improved Virtual Disks, vApp) creating a brand new full backup generation";

    public static final String VIRTUALBACKUP = "vbackup";

    static String helpGlobalSummary() {
        final VirtualBackupCommandInteractive info = new VirtualBackupCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    private final List<String> targetList;

    public VirtualBackupCommandInteractive() {
        super();
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
            if ((connetionManager != null) && connetionManager.isConnected()) {
                result = doAction(connetionManager);
            } else {
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION));
            }
        }
        return result;
    }

    public List<AbstractCoreResultActionVirtualBackup> actionVirtualBackupInteractive(
            final ConnectionManager connetionManager) throws CoreResultActionException {
        final List<AbstractCoreResultActionVirtualBackup> result = new LinkedList<>();
        final List<IFirstClassObject> backupList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
        for (final IFirstClassObject fco : backupList) {
            if (fco instanceof VirtualAppManager) {

                final CoreResultActionVappVirtualBackup rab = new CoreResultActionVappVirtualBackup(fco, getOptions());
                rab.setInteractive(new VirtualBackupVappInteractive(rab));
                result.add(rab);
            } else if (fco instanceof VirtualMachineManager) {
                final CoreResultActionVmVirtualBackup rab = new CoreResultActionVmVirtualBackup(fco, getOptions());
                rab.setInteractive(new VirtualBackupInteractive(rab, null));
                result.add(rab);
            } else if (fco instanceof ImprovedVirtualDisk) {
                final CoreResultActionIvdVirtualBackup rab = new CoreResultActionIvdVirtualBackup(fco, getOptions());
                rab.setInteractive(new VirtualBackupInteractive(rab, null));
                result.add(rab);
            } else {
                throw new CoreResultActionException(fco);
            }

        }
        actionVirtualBackup(connetionManager.getRepositoryTarget(), result);
        return result;
    }

    public void actionVirtualBackup(final ITarget iTarget,
            final Iterable<AbstractCoreResultActionVirtualBackup> resultList) throws CoreResultActionException {
        try {
            for (final AbstractCoreResultActionVirtualBackup rab : resultList) {
                if (!rab.isAbortedOrFailed()) {
                    final VirtualBackupCommand backupFcoCommand = new VirtualBackupCommand(rab);
                    backupFcoCommand.actionVirtualBackup(iTarget, rab);
                }
            }
        } catch (final Exception e) {
            Utility.logWarning(this.logger, e);
        }
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");

        final OptionSpecBuilder optionThreads = this.parser
                .accepts(OPTION_THREADS, "Number of Threads[" + getOptions().getNumberOfThreads() + "].")
                .availableUnless(optionHelp);
        optionThreads.withRequiredArg().ofType(Integer.class).describedAs("number");

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

    private OperationStateList doAction(final ConnectionManager connetionManager) throws CoreResultActionException {
        final List<AbstractCoreResultActionVirtualBackup> resultActions = actionVirtualBackupInteractive(
                connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            IoFunction.println("succeded\tfailed\tskipped\tEntity");
            opLoop: for (final AbstractCoreResultActionVirtualBackup resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.printf(" \t \tx\t \t\t%s (%s)\n", resultAction.getFcoToString(),
                            resultAction.getReason());

                    break;
                case SKIPPED:
                    IoFunction.printf(" \t \t \tx\t\t%s (%s)\n", resultAction.getFcoToString(),
                            resultAction.getReason());

                    break;
                case SUCCESS:
                    IoFunction.printf("x\t \t \t\t%s\n", resultAction.getFcoToString());
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk,
                    EntityType.VirtualApp, EntityType.K8sNamespace }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

    @Override
    public String getCommandName() {
        return VIRTUALBACKUP;
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    /**
     * @return the options
     */
    @Override
    public CoreVirtualBackupOptions getOptions() {
        return (CoreVirtualBackupOptions) this.options;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("B1", stringsCompleter(getCommandName()));
        comp.put("B11", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_VIM));
        comp.put("B12", stringsCompleter(OPTION_HELP));
        return "|B1 B11*|B1 B12?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        return "EnyityType  Entity Description      uuid    name    moref\n"
                + "vm        Virtual Machine            X       X       X\n"
                + "ivd       Improved Virtual Disks     X       X\n"
                + "vapp      Virtual Appliance          X       X       X\n"
                + "tag       vCenter Tag                        X\n";
    }

    @Override
    public String helpExample() {
        return " " + VIRTUALBACKUP + " " + OPTION_ALL + "\n\tBackup any kind of object.\n\n " + VIRTUALBACKUP
                + " vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330 \n\tStart a backup consolidation of 3 different Vm:1st by name,2nd by Moref,3rd by UUID.\n\n"
                + " backup " + OPTION_ALL + " ivd " + OPTION_VIM
                + " 9a583042-cb9d-5673-cbab-56a02a91805d\n\tCreate a virtual full backup of any Improved Virtual Disks managed by vcenter 9a583042-cb9d-5673-cbab-56a02a91805d.\n\n";
    }

    @Override
    public String helpSummary() {
        return VirtualBackupCommandInteractive.COMMAND_DESCRIPTION;
    }

    @Override
    public final void initialize() {
        setOptions(new CoreVirtualBackupOptions());
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = parseArguments(this.parser, arguments, getOptions());
        setHelp(options.has(OPTION_HELP));

        if (options.has(OPTION_THREADS)) {
            getOptions().setNumberOfThreads(Integer.parseInt(options.valueOf(OPTION_THREADS).toString()));
        }

        if (options.has(OPTION_ALL)) {
            getOptions().setAnyFcoOfType(FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
                    FirstClassObjectFilterType.any | FirstClassObjectFilterType.all));
        }
        if (options.has(OPTION_VIM)) {
            getOptions().setVim(options.valueOf(OPTION_VIM).toString());
        }

        getOptions().setDryRun(options.has(OPTION_DRYRUN));
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
