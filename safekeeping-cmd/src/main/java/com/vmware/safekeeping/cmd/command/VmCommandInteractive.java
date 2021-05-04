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
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.PrettyBoolean.PrettyBooleanValues;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.CoreResultActionPower;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.ext.command.AbstractVmCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDefragmentAllDisks;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVmFeature;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVmList;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VmCommandInteractive extends AbstractVmCommand implements ICommandInteractive {

    private static final String COMMAND_DESCRIPTION = "Manage Virtual Machines.";
    private static final String OPTION_POWEROFF = "poweroff";
    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_PROFILE = "profile";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_REBOOT = "reboot";
    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";
    private static final String OPTION_DRYRUN = "dryrun";
    private static final String OPTION_LIST = "list";
    private static final String OPTION_DETAIL = "detail";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_EXECUTE = "execute";
    private static final String OPTION_PASSWORD = "password";
    private static final String OPTION_FEATURE = "feature";
    private static final String OPTION_DEFRAGMENT_ALL_DISKS = "defrag";

    public static final String VM = "vm";

    static String helpGlobalSummary() {
        final VmCommandInteractive info = new VmCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;

    private final List<String> targetList;

    public VmCommandInteractive() {
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
                if (this.cbt != null) {
                    result = actionCbtInteractive(connetionManager);
                } else if (this.powerOff) {
                    result = actionPowerOffInteractive(connetionManager);
                } else if (this.powerOn) {
                    result = actionPowerOnInteractive(connetionManager);
                } else if (this.reboot) {
                    result = actionRebootInteractive(connetionManager);
                } else if (this.remove) {
                    result = actionRemoveInteractive(connetionManager);
                } else if (this.list) {
                    result = actionListInteractive(connetionManager);
                } else if (this.execute != null) {
                    result = actionExecuteInteractive(connetionManager);
                } else if (this.feature != null) {
                    result = actionFeatureInteractive(connetionManager);
                } else if (this.defragmentAllDisks) {
                    result = actionDefragmentAllDisksInteractive(connetionManager);
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

    /**
     * Change CBT for VMs
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionCbtInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionCbt> resultActions = actionCbt(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionCbt resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                case SKIPPED:
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tCBT before:%s  now:%s  %s.",
                            resultAction.getPreviousFlagState() ? "enable" : "disable",
                            resultAction.hasFlagState() ? "enable" : "disable",
                            (resultAction.getState() == OperationState.SUCCESS) ? ""
                                    : resultAction.getState().toString());
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

    /**
     * Change CBT for VMs
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionDefragmentAllDisksInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionDefragmentAllDisks> resultActions = actionDefragmentAllDisks(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDefragmentAllDisks resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tDefragmenting All disks %s  - reason: %s",
                            resultAction.getState().toString(), resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tDefragmenting All disks started. ");
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

    /**
     * Execute a process inside a vm
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionExecuteInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> resultActions = actionExecute(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionPower resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "%s failed to exec", resultAction.getFcoToString());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s executed", resultAction.getFcoToString());
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }

        return result;
    }

    private OperationStateList actionFeatureInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionVmFeature> resultActions = actionFeature(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionVmFeature resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                case SKIPPED:
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tFeature now:%s  %s.",
                            resultAction.hasFlagState() ? "enable" : "disable",
                            (resultAction.getState() == OperationState.SUCCESS) ? ""
                                    : resultAction.getState().toString());
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

    private OperationStateList actionListInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionVmList> resultActions = actionList(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            IoFunction.showInfo(this.logger,
                    VirtualMachineManager.sHeaderToString() + "%-5s    %-5s   %-5s   %-5s    %-11s     %s       %s",
                    " cbt  ", "snapshot", "template", "encrypt", "state", "ivd", "datastore");
            opLoop: for (final CoreResultActionVmList resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                case SKIPPED:
                    IoFunction.printf("%s: %s", resultAction.getState(), resultAction.getReason());
                    break;
                case SUCCESS:
                    final VirtualMachineManager vmm = resultAction.getVmm();
                    final StringBuilder vmDiskStr = new StringBuilder();
                    try {
                        IoFunction.showInfo(this.logger,
                                "%s%-5b    %-5b      %-5b      %-5b      %-11s     %-5b     %s", vmm.toString(),
                                vmm.getConfig().isChangeTrackingEnabled(), vmm.getCurrentSnapshot() != null,
                                vmm.getConfig().isTemplate(), vmm.isEncrypted(), vmm.getPowerState().toString(),
                                vmm.useImprovedVirtuaDisk(), vmm.getDatastoreInfo().getName());
                    } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | VimObjectNotExistException e) {
                        Utility.logWarning(this.logger, e);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Interrupted!", e);
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
                    }
                    if (vmm.useImprovedVirtuaDisk() && this.detail) {
                        for (final ImprovedVirtualDisk ivd : resultAction.getIvdList()) {
                            vmDiskStr.append(ivd.toString());
                            vmDiskStr.append('\n');
                        }
                        IoFunction.showInfo(this.logger, "\t\tAssociated Improved Virtual Disk\n\t\t%s",
                                vmDiskStr.toString());
                    }
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    /**
     * Power off a VMs
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionPowerOffInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> resultActions = actionPowerOff(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionPower resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    if (this.force) {
                        IoFunction.showWarning(this.logger, "%s failed to powerOff", resultAction.getFcoToString());
                    } else {
                        IoFunction.showWarning(this.logger, "%s cannot be shutdown", resultAction.getFcoToString());
                    }
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    if (this.force) {
                        IoFunction.showInfo(this.logger, "%s powerOff", resultAction.getFcoToString());
                    } else {
                        IoFunction.showInfo(this.logger, "%s shutdown in progress", resultAction.getFcoToString());
                    }
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    /**
     * Power On a VMs
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionPowerOnInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> resultActions = actionPowerOn(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionPower resultAction : resultActions) {

                switch (resultAction.getState()) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "%s failed to powerOn", resultAction.getFcoToString());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s powerOn", resultAction.getFcoToString());
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    /**
     * Reboot a VMs
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionRebootInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> resultActions = actionReboot(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionPower resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    if (this.force) {
                        IoFunction.showWarning(this.logger, "%s failed to Restart", resultAction.getFcoToString());
                    } else {
                        IoFunction.showWarning(this.logger, "%s cannot Reboot the Guest",
                                resultAction.getFcoToString());
                    }
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip  - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    if (this.force) {
                        IoFunction.showInfo(this.logger, "%s Restart", resultAction.getFcoToString());
                    } else {
                        IoFunction.showInfo(this.logger, "%s Reboot Guest in progress", resultAction.getFcoToString());
                    }
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    /**
     * Remove a VMs
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    private OperationStateList actionRemoveInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionDestroy> resultActions = actionRemove(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDestroy resultAction : resultActions) {

                switch (resultAction.getState()) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:

                    IoFunction.showWarning(this.logger, "%s %s failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.isShutdownRequired() ? "Shutdown and removed" : "Removed",
                            resultAction.getReason());

                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:

                    IoFunction.showInfo(this.logger, "%s %s", resultAction.getFcoToString(),
                            resultAction.isShutdownRequired() ? "Shutdown and removed" : "Removed");

                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualMachine, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);

        final OptionSpecBuilder optionPowerOff = this.parser.accepts(OPTION_POWEROFF,
                "Shut down the Virtual machine(s).");

        final OptionSpecBuilder optionPowerOn = this.parser.accepts(OPTION_POWERON, "Power On the Virtual machine(s).");

        final OptionSpecBuilder optionReboot = this.parser.accepts(OPTION_REBOOT,
                "Try a  reboot of the Virtual machine(s)");
        final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "Virtual Machines list.");

        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the Virtual machine.");

        final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
                "Enable disable Change Block Tracking");

        optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
        final OptionSpecBuilder optionFeature = this.parser.accepts(OPTION_FEATURE,
                "Enable disable UI Storage VMotion");
        optionFeature.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
        final OptionSpecBuilder optionExecute = this.parser.accepts(OPTION_EXECUTE, "Run guest command");
        optionExecute.withRequiredArg().describedAs("command");
        final OptionSpecBuilder optionPassword = this.parser
                .accepts(OPTION_PASSWORD, "vCenter password to access a VM.").requiredIf(optionExecute);
        optionPassword.withRequiredArg().describedAs("password");
        final OptionSpecBuilder optionDefragmentAllDisks = this.parser.accepts(OPTION_DEFRAGMENT_ALL_DISKS,
                "Defragment all disks");
        final OptionSpecBuilder optionProfile = this.parser.accepts(OPTION_PROFILE, "Show Virtual Machine(s) profile.");
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        optionHelp.forHelp();

        this.parser.mainOptions(optionPowerOff, optionPowerOn, optionReboot, optionList, optionRemove, optionCbt,
                optionExecute, optionProfile, optionHelp, optionFeature, optionDefragmentAllDisks);

        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
        optionVim.withRequiredArg().describedAs("vcenter");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp);
        this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
        this.parser.accepts(OPTION_FORCE, "Force the  Virtual machine operation.").availableIf(optionPowerOff,
                optionReboot, optionRemove);
        this.parser.accepts(OPTION_ALL, "Operation apply to any Virtual Machines.").availableUnless(optionHelp);
        this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(optionHelp, optionList);
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return VM;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("V1", stringsCompleter(VM));

        comp.put("V11",
                stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REMOVE, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
        comp.put("V12", stringsCompleter(OPTION_LIST, OPTION_DETAIL, OPTION_VIM));
        comp.put("V13", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWERON, OPTION_VIM));
        comp.put("V14",
                stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWEROFF, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
        comp.put("V15",
                stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REBOOT, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
        comp.put("V16", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_ENABLE_CHANGED_BLOCK_TRACKING, OPTION_VIM,
                OPTION_QUIET));
        comp.put("V17", stringsCompleter(OPTION_ALL, OPTION_PROFILE, OPTION_VIM));
        comp.put("V18", stringsCompleter(OPTION_HELP));

        return "|V1 V11*|V1 V12*|V1 V13*|V1 V14*|V1 V15*|V1 V16*|V1 V17*|V1 V18?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        return "EnyityType  Entity Description      uuid    name    moref\n"
                + "vm          Virtual Machine         X       X       X\n" + "tag         vCenter Tag             X\n";
    }

    @Override
    public String helpExample() {
        return "vm " + OPTION_LIST + " " + OPTION_DETAIL + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
                + "\tShow any Virtual Machine available with associated Improved Virtual Disk on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
                + "vm " + OPTION_POWERON + " vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n"
                + "\tPower On 3 Virtual Machines. 1st a VM selected by name. 2nd a VM selected by Moref. 3rd a VM selected by UUID\n\n"
                + "vm " + OPTION_REMOVE + " " + OPTION_FORCE + " vm:testVM\n"
                + "\tRemove the vm testVM and force a poweroff is the VM is On\n\n";
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
        final OptionSet optionSet = parseArguments(this.parser, arguments, getOptions());

        try {
            if (optionSet.has(OPTION_HELP)) {
                setHelp(true);
            } else if (optionSet.has(OPTION_POWERON)) {
                this.powerOn = true;

            } else if (optionSet.has(OPTION_POWEROFF)) {
                this.powerOff = true;

            } else if (optionSet.has(OPTION_REBOOT)) {
                this.reboot = true;

            } else if (optionSet.has(OPTION_LIST)) {
                this.list = true;
                if (optionSet.nonOptionArguments().isEmpty()) {
                    getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vm | FirstClassObjectFilterType.all);
                }
            } else if (optionSet.has(OPTION_REMOVE)) {
                this.remove = true;
            } else if (optionSet.has(OPTION_DEFRAGMENT_ALL_DISKS)) {
                this.defragmentAllDisks = true;
            } else if (optionSet.has(OPTION_EXECUTE)) {
                this.execute = optionSet.valueOf(OPTION_EXECUTE).toString();
            } else if (optionSet.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
                this.cbt = PrettyBoolean.parseBoolean(optionSet.valueOf(OPTION_ENABLE_CHANGED_BLOCK_TRACKING));
            } else if (optionSet.has(OPTION_FEATURE)) {
                this.feature = PrettyBoolean.parseBoolean(optionSet.valueOf(OPTION_FEATURE));
            } else {
                throw new ParsingException("No Action specified");
            }
            if (optionSet.has(OPTION_ALL)) {
                getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vm | FirstClassObjectFilterType.all);
            }
            if (optionSet.has(OPTION_DRYRUN)) {
                getOptions().setDryRun(true);
            }
            if (optionSet.has(OPTION_PROFILE)) {
                this.profile = true;
            }
            if (optionSet.has(OPTION_FORCE)) {
                this.force = true;
            }
            if (optionSet.has(OPTION_DETAIL)) {
                this.detail = true;
            }
            if (optionSet.has(OPTION_QUIET)) {
                getOptions().setQuiet(true);
            }
            if (optionSet.has(OPTION_PASSWORD)) {
                this.password = optionSet.valueOf(OPTION_PASSWORD).toString();
            }
            if (optionSet.has(OPTION_VIM)) {
                getOptions().setVim(optionSet.valueOf(OPTION_VIM).toString());
            }

        } catch (final Exception e) {
            IoFunction.showInfo(this.logger, e.getMessage());
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
