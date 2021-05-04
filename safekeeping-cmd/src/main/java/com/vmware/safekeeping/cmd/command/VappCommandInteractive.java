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
import com.vmware.safekeeping.core.ext.command.VappCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVappList;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VappCommandInteractive extends VappCommand implements ICommandInteractive {
    /**
     * Logger for this class
     */

    private static final String COMMAND_DESCRIPTION = "Manage VirtualApp.";
    private static final String OPTION_POWEROFF = "poweroff";
    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_REMOVE = "remove";
    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";
    private static final String OPTION_DRYRUN = "dryrun";
    private static final String OPTION_LIST = "list";
    private static final String OPTION_DETAIL = "detail";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_REBOOT = "reboot";

    public static final String VAPP = "vapp";

    static String helpGlobalSummary() {
        final VappCommandInteractive info = new VappCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;

    private final List<String> targetList;

    public VappCommandInteractive() {
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
        final List<CoreResultActionCbt> resultActions = actionCbt(connetionManager);
        final OperationStateList result = new OperationStateList();
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
                            (Boolean.TRUE.equals(this.cbt)) ? "enable" : "disable",
                            (resultAction.getState() == OperationState.SUCCESS) ? ""
                                    : resultAction.getState().toString());
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualApp, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

    private OperationStateList actionListInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager - start"); //$NON-NLS-1$
        }

        final List<CoreResultActionVappList> resultActions = actionList(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            IoFunction.showInfo(this.logger, VirtualMachineManager.sHeaderToString() + "%-5s    %-11s  %s", " cbt  ",
                    "state", "vm(s)");
            opLoop: for (final CoreResultActionVappList resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    break;
                case SKIPPED:
                    break;
                case SUCCESS:
                    final VirtualAppManager vAppm = resultAction.getvApp();
                    final StringBuilder vmListStr = new StringBuilder();
                    try {
                        IoFunction.showInfo(this.logger, "%s%-5b    %-11s  %d", vAppm.toString(),
                                vAppm.isChangedBlockTrackingEnabled(),
                                vAppm.getPowerState().toString().toLowerCase(Utility.LOCALE), vAppm.getVmList().size());
                    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                        this.logger.warning("ConnectionManager - exception ignored - exception: " + e); //$NON-NLS-1$

                    } catch (final InterruptedException e) {
                        this.logger.log(Level.WARNING, "Interrupted!", e);
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
                    }
                    if (this.detail) {
                        for (final VirtualMachineManager vm : vAppm.getVmList()) {
                            vmListStr.append("\t\t");
                            vmListStr.append(vm.toString());
                            vmListStr.append('\n');
                        }
                        IoFunction.showInfo(this.logger, "\t\tAssociated Virtual Machine(s)\n%s", vmListStr.toString());
                    }
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualApp, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     *
     * @param connetionManager
     * @return
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
            IoFunction.printTotal(EntityType.VirtualApp, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionPowerOnInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> resultActions = actionPowerOn(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionPower resultAction : resultActions) {
                switch (result.add(resultAction)) {
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
            IoFunction.printTotal(EntityType.VirtualApp, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

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
                    break opLoop;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.VirtualApp, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionRemoveInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionDestroy> resultActions = actionRemove(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDestroy resultAction : resultActions) {
                switch (result.add(resultAction)) {
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
            IoFunction.printTotal(EntityType.VirtualApp, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);

        final OptionSpecBuilder optionPowerOff = this.parser.accepts(OPTION_POWEROFF, "Shut down the virtualApp(s).");

        final OptionSpecBuilder optionPowerOn = this.parser.accepts(OPTION_POWERON, "Power On the virtualApp(s).");
        final OptionSpecBuilder optionReboot = this.parser.accepts(OPTION_REBOOT, "Try a  reboot of the vApp(s)");
        final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "virtualApps list.");

        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the virtualApp.");

        final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
                "Enable disable Change Block Tracking");
        optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");

        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        optionHelp.forHelp();

        this.parser.mainOptions(optionPowerOff, optionPowerOn, optionReboot, optionList, optionRemove, optionCbt,
                optionHelp);

        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
        optionVim.withRequiredArg().describedAs("vcenter");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp);
        this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
        this.parser.accepts(OPTION_FORCE, "Force the  virtualApp operation.").availableIf(optionPowerOff, optionRemove,
                optionReboot);
        this.parser.accepts(OPTION_ALL, "Operation apply to any virtualApps.").availableUnless(optionHelp);
        this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(optionHelp, optionList);
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return VAPP;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("VA1", stringsCompleter(VAPP));

        comp.put("VA11",
                stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REMOVE, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));
        comp.put("VA12", stringsCompleter(OPTION_LIST, OPTION_DETAIL, OPTION_VIM));
        comp.put("VA13", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWERON, OPTION_VIM));
        comp.put("VA14",
                stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_POWEROFF, OPTION_FORCE, OPTION_VIM, OPTION_QUIET));

        comp.put("VA16", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_ENABLE_CHANGED_BLOCK_TRACKING, OPTION_VIM,
                OPTION_QUIET));
        comp.put("VA18", stringsCompleter(OPTION_HELP));

        return "|VA1 VA11*|VA1 VA12*|VA1 VA13*|VA1 VA14*| VA1 VA16*| VA1 VA18?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        return "EnyityType  Entity Description      uuid    name    moref\n"
                + "vapp        Virtual Machine         X       X       X\n" + "tag         vCenter Tag             X\n";
    }

    @Override
    public String helpExample() {
        return "vapp " + OPTION_LIST + " " + OPTION_DETAIL + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
                + "\tShow any virtualApp available with associated Virtual Machines on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
                + "vapp " + OPTION_POWERON
                + " vapp:testVM vapp:resgroup-v2313 vapp:f9ad3050-d5a6-d107-d0d8-d305c4bc2330\n"
                + "\tPower On 3 virtualApp . 1st a VAPP selected by name. 2nd a VAPP selected by Moref. 3rd a VAPP selected by UUID\n\n"
                + "vapp " + OPTION_REMOVE + " " + OPTION_FORCE + " vapp:testVapp\n"
                + "\tDelete the vm vapp:testVapp and force a poweroff is the VAPP is On\n\n";
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

            } else if (optionSet.has(OPTION_LIST)) {
                this.list = true;
                if (optionSet.nonOptionArguments().isEmpty()) {
                    getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vapp | FirstClassObjectFilterType.all);
                }
            } else if (optionSet.has(OPTION_REMOVE)) {
                this.remove = true;
            } else if (optionSet.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
                this.cbt = PrettyBoolean.parseBoolean(optionSet.valueOf(OPTION_ENABLE_CHANGED_BLOCK_TRACKING));
            } else {
                throw new ParsingException("No Action specified");
            }
            if (optionSet.has(OPTION_ALL)) {
                getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vapp | FirstClassObjectFilterType.all);
            }
            if (optionSet.has(OPTION_DRYRUN)) {
                getOptions().setDryRun(true);
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
