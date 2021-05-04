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

import com.vmware.safekeeping.cmd.report.CloneReport;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.DateUtility;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.SnapshotInfo;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.ext.command.AbstractSnapshotCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionListSnap;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class SnapCommandInteractive extends AbstractSnapshotCommand implements ICommandInteractive {
    private static final String COMMAND_DESCRIPTION = "Manage Snapshot of any specified Entities (virtual machine, Improved Virtual Disks, vApp).";
    private static final String OPTION_ALL = "all";

    private static final String OPTION_VIM = "vim";

    private static final String OPTION_QUIET = "quiet";

    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_LIST = "list";

    private static final String OPTION_DETAIL = "detail";

    private static final String OPTION_FORCE = "force";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_CREATE = "create";
    private static final String OPTION_REVERT = "revert";
    private static final String OPTION_NAME = "name";
    private static final String OPTION_DELETEALL = "deleteall";

    public static final String SNAP = "snapshot";

    static String helpGlobalSummary() {
        SnapCommandInteractive info = null;

        info = new SnapCommandInteractive();

        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;
    private boolean detail;

    private final List<String> targetList;

    public SnapCommandInteractive() {
        super(new CloneReport());
        setHelp(false);
        this.detail = false;
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
                if (this.create) {
                    result = action_Create(connetionManager);
                } else if ((this.delete != null) || this.deleteAll) {
                    result = action_Delete(connetionManager);
                } else if (this.list) {
                    result = action_List(connetionManager);
                } else {
                    result = new OperationStateList(OperationState.FAILED,
                            IoFunction.showWarning(this.logger, Vmbk.INVALID_COMMAND));
                }
            } else {
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION));
            }
        }
        return result;
    }

    private OperationStateList action_Create(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionCreateSnap> resultActions = actionCreate(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionCreateSnap resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "%s failed to create a snapshot",
                            resultAction.getFcoToString());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    switch (resultAction.getEntityType()) {
                    case VirtualApp:
                        IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                        break;
                    case VirtualMachine:
                        IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                        IoFunction.showInfo(this.logger, "Snapshot %s Moref: %s created", resultAction.getSnapName(),
                                resultAction.getSnapMoref());
                        break;
                    case ImprovedVirtualDisk:
                        IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                        IoFunction.showInfo(this.logger, "Snapshot %s Id: %s created", resultAction.getSnapName(),
                                resultAction.getSnapId());
                        break;
                    default:
                        break;
                    }
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk,
                    EntityType.VirtualApp }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    protected OperationStateList action_Delete(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionDeleteSnap> resultActions = actionDelete(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDeleteSnap resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                    IoFunction.println(" Snapshot:");
                    for (final SnapshotInfo snTree : resultAction.getSnapList()) {
                        IoFunction.showInfo(this.logger, "\t%s\t%s\t%d\t%s\t%s\t%s",
                                (snTree.getResult() == OperationState.SUCCESS) ? "removed" : "", snTree.getName(),
                                snTree.getId(), snTree.getState(), snTree.getCreateTime().toString(),
                                snTree.getDescription());
                    }
                    IoFunction.showWarning(this.logger, "%s failed to Remove", resultAction.getFcoToString());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    switch (resultAction.getEntityType()) {
                    case VirtualApp:
                        IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                        break;
                    case VirtualMachine:
                    case ImprovedVirtualDisk:

                        // IoFunction.showInfo(logger, VirtualMachineManager.HeadertoString());
                        IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                        IoFunction.println(" Snapshot:");
                        for (final SnapshotInfo snTree : resultAction.getSnapList()) {
                            IoFunction.showInfo(this.logger, "\t%s\t%s\t%s\t%s\t%s\t%s",
                                    (snTree.getResult() == OperationState.SUCCESS) ? "removed" : "", snTree.getName(),
                                    snTree.getId(), snTree.getState(), snTree.getCreateTime().toString(),
                                    snTree.getDescription());
                        }
                        break;
                    default:
                        break;
                    }
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk,
                    EntityType.VirtualApp }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    protected OperationStateList action_List(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionListSnap> resultActions = actionList(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionListSnap resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    switch (resultAction.getEntityType()) {
                    case VirtualApp:

                    case VirtualMachine:
                    case ImprovedVirtualDisk:
                        if (!resultAction.getSnapList().isEmpty()) {
                            IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                            IoFunction.println(" Snapshot:");
                            for (final SnapshotInfo snTree : resultAction.getSnapList()) {
                                IoFunction.showInfo(this.logger, "\t%s\t%s\t%s\t%s\t%s", snTree.getName(),
                                        snTree.getId(), (snTree.getState() != null) ? snTree.getState().toString() : "",
                                        DateUtility.convertGregorianToString(snTree.getCreateTime()),
                                        snTree.getDescription());
                            }
                        } else {
                            if (this.detail || (resultActions.size() == 1)) {
                                IoFunction.showInfo(this.logger, resultAction.getFcoToString());
                                IoFunction.println(" No Snapshots");
                            }
                        }
                        break;
                    default:
                        break;
                    }
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk },
                    total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionCreate = this.parser.accepts(OPTION_CREATE, "Create a new snapshot.");
        final OptionSpecBuilder optionRevert = this.parser.accepts(OPTION_REVERT, "Revert to a snapshot.");
        optionRevert.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "List Snapshot hierarchy.");

        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Delete the Virtual machine.");
        optionRemove.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");
        final OptionSpecBuilder optionDeleteAll = this.parser.accepts(OPTION_DELETEALL,
                "Delete any snapshot associated to the entity/entities.");

        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        optionHelp.forHelp();

        this.parser.mainOptions(optionCreate, optionRevert, optionList, optionRemove, optionDeleteAll, optionHelp);
        /*
         * Option vim
         */
        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
        optionVim.withRequiredArg().describedAs("vcenter");
        /*
         * Option dryrun
         */
        this.parser.accepts(OPTION_DRYRUN, "Do not snap really.").availableUnless(optionList, optionHelp);
        this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
        this.parser.accepts(OPTION_FORCE, "Force the  Snapshot operation.").availableIf(optionRevert, optionDeleteAll,
                optionRemove);
        this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks .")
                .availableUnless(optionHelp).withOptionalArg().ofType(FirstClassObjectType.class)
                .describedAs("vm|ivd ");
        this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(optionHelp, optionList);
        final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME, "Used by " + OPTION_CREATE)
                .availableIf(optionCreate);
        optionName.withRequiredArg().describedAs("name");
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return SNAP;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("N1", stringsCompleter(SNAP));
        comp.put("N11", stringsCompleter(OPTION_ALL, OPTION_LIST, OPTION_VIM));
        comp.put("N12", stringsCompleter(OPTION_DRYRUN, OPTION_REMOVE, OPTION_QUIET, OPTION_VIM));
        comp.put("N13", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_QUIET, OPTION_DELETEALL, OPTION_VIM));
        comp.put("N14", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_CREATE, OPTION_VIM));
        comp.put("N15", stringsCompleter(OPTION_DRYRUN, OPTION_REVERT, OPTION_VIM));
        comp.put("N16", stringsCompleter(OPTION_HELP));
        return "|N1 N11*|N1 N12*|N1 N13*|N1 N14*|N1 N15*|N1 N16?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
                + "vm		Virtual Machine			X	X	X\n" + "ivd		Improved Virtual Disks	X	X	 \n"
                + "vapp		Virtual Appliance		X		 \n" + "tag		vCenter Tag				X	 \n";

        return ret;
    }

    @Override
    public String helpExample() {
        return String.format(" %s -%s -%s -%s 9a583042-cb9d-5673-cbab-56a02a91805d\n\t%s\n\n", SNAP, OPTION_LIST,
                OPTION_ALL, OPTION_VIM,
                "Show any snapshot of any available object on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d")
                + String.format(" %s -%s vm:testVM vm:vm-2313 ivd:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n\t%s\n\n", SNAP,
                        OPTION_CREATE,
                        "Create a snapshot  3 different objects.  1st a VM selected by name. 2nd a VM selected by Moref. 3rd an IVD selected by UUID")
                + String.format(" %s -%s -%s  vm \n\t%s\n\n", SNAP, OPTION_DELETEALL, OPTION_ALL,
                        " Delete any snapshot from any VM");
    }

    @Override
    public String helpSummary() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = parseArguments(this.parser, arguments, getOptions());
        setHelp(options.has(OPTION_HELP));
        if (options.has(OPTION_LIST)) {
            this.list = true;
            if (options.nonOptionArguments().isEmpty()) {
                getOptions().setAnyFcoOfType(FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
            }
        }

        if (options.has(OPTION_CREATE)) {
            this.create = true;
        }
        if (options.has(OPTION_REVERT)) {
            this.revert = true;
        }
        if (options.has(OPTION_REMOVE)) {
            this.delete = (List<String>) options.valuesOf(OPTION_REMOVE);
        }
        if (options.has(OPTION_DELETEALL)) {

            this.deleteAll = true;
        }

        if (options.has(OPTION_DRYRUN)) {
            getOptions().setDryRun(true);
        }
        if (options.has(OPTION_NAME)) {
            this.name = options.valueOf(OPTION_NAME).toString();
        }

        if (options.has(OPTION_ALL)) {
            getOptions().setAnyFcoOfType(
                    FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL), FirstClassObjectFilterType.any)
                            | FirstClassObjectFilterType.all);

        }
        if (options.has(OPTION_VIM)) {
            getOptions().setVim(options.valueOf(OPTION_VIM).toString());
        }
        if (options.has(OPTION_QUIET)) {
            getOptions().setQuiet(true);
        }
        if (options.has(OPTION_DETAIL)) {
            this.detail = true;
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
