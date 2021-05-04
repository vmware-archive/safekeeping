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
import java.net.MalformedURLException;
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
import com.vmware.safekeeping.common.PrettyBoolean.PrettyBooleanValues;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.ext.command.AbstractFcoCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionClone;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVappImport;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.EntityType;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class FcoCommandInteractive extends AbstractFcoCommand implements ICommandInteractive {
    private static String COMMAND_DESCRIPTION = "Import and Copy First Class Objects.";

    private static final String OPTION_VIM = "vim";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_NOVMDK = "novmdk";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_NAME = "name";

    private static final String OPTION_HOST = "host";
    private static final String OPTION_DATACENTER = "datacenter";
    private static final String OPTION_DATASTORE = "datastore";
    private static final String OPTION_FOLDER = "folder";

    private static final String OPTION_RESPOOL = "respool";

    private static final String OPTION_VM_NETWORKS = "network";

    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_URL = "url";
    private static final String OPTION_IMPORT = "import";
    private static final String OPTION_CLONE = "clone";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";

    public static final String FCO = "fco";

    static String helpGlobalSummary() {
        final FcoCommandInteractive info = new FcoCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;

    private final List<String> targetList;

    public FcoCommandInteractive() {
        super(new CloneReport());
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
                if (this.vappImport) {
                    result = actionVappImportInteractive(connetionManager);
                } else if (this.clone) {
                    result = actionCloneInteractive(connetionManager);
                } else if (this.remove) {
                    result = actionRemoveInteractive(connetionManager);
                } else if (Boolean.TRUE.equals(this.cbt)) {
                    result = actionCbtInteractive(connetionManager);
                } else {
                    result = new OperationStateList(OperationState.FAILED,
                            IoFunction.showWarning(this.logger, Vmbk.INVALID_COMMAND));
                }

            } else {
                IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION);
            }
        }
        return result;
    }

    private OperationStateList actionCbtInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        List<CoreResultActionCbt> resultActions = actionCbt(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionCbt resultAction : resultActions) {
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
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                    EntityType.ImprovedVirtualDisk }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionCloneInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionClone> resultActions = actionClone(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionClone resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    if (StringUtils.isEmpty(resultAction.getCloneDatastore())) {
                        IoFunction.showWarning(this.logger, "Failed to cloned to name [%s]",
                                resultAction.getCloneName());
                    } else {
                        IoFunction.showWarning(this.logger, "Failed to cloned to name [%s] datastore [%s]",
                                resultAction.getCloneName(), resultAction.getCloneDatastore());
                    }
                    IoFunction.showWarning(this.logger, " Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    if (StringUtils.isEmpty(resultAction.getCloneDatastore())) {
                        IoFunction.showInfo(this.logger, "Cloned to name [%s]", resultAction.getCloneName());
                    } else {
                        IoFunction.showInfo(this.logger, "Cloned to name [%s] datastore [%s]",
                                resultAction.getCloneName(), resultAction.getCloneDatastore());
                    }
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

    private OperationStateList actionRemoveInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionDestroy> resultActions = actionRemove(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDestroy resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showWarning(this.logger, "Remove FCO failed - Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s Removed", resultAction.getFcoToString());
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

    private OperationStateList actionVappImportInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionVappImport resultAction = actionVappImport(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);

            IoFunction.showInfo(this.logger, "Dryrun - %s Removed", resultAction.getFcoToString());
            break;
        case FAILED:

            IoFunction.showWarning(this.logger, "Import %s failed - Reason: %s", resultAction.getUrlPath(),
                    resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "Import %s  skipped - Reason: %s", resultAction.getUrlPath(),
                    resultAction.getReason());
            break;
        case SUCCESS:
            IoFunction.showInfo(this.logger, "%s Imported", resultAction.getFcoToString());
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

        final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
                "Enable disable Change Block Tracking");
        optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
        final OptionSpecBuilder optionImport = this.parser.accepts(OPTION_IMPORT, "Import an OVF.");
        final OptionSpecBuilder optionClone = this.parser.accepts(OPTION_CLONE, "Clone a First Class Object");
        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the First Class Object.");
        this.parser.mainOptions(optionHelp, optionImport, optionClone, optionRemove, optionCbt);
        final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME, "Name for the imported <EnyityType>")
                .requiredIf(OPTION_IMPORT, OPTION_CLONE);

        optionName.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionHost = this.parser
                .accepts(OPTION_HOST, "VMware ESX host vim object name to use as import target.")
                .availableIf(optionImport);
        optionHost.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionDatacenter = this.parser
                .accepts(OPTION_DATACENTER, "Datacenter - vim object name to use as import target")
                .availableIf(optionImport);
        optionDatacenter.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionDatastore = this.parser
                .accepts(OPTION_DATASTORE, "Datastore - vim object name to use as import target.")
                .availableIf(optionImport, optionClone);
        optionDatastore.withRequiredArg().describedAs("name");

        final OptionSpecBuilder optionUrl = this.parser.accepts(OPTION_URL, " Source url.").availableIf(optionImport);
        optionUrl.withRequiredArg().describedAs("url");
        this.parser.accepts(OPTION_FORCE, "Force the operation.").availableIf(optionRemove);
        /*
         * Option folder
         */
        final OptionSpecBuilder optionFolder = this.parser
                .accepts(OPTION_FOLDER,
                        "Virtual Machine Folder vim object name to use as import target. <name> vmfolder name"
                                + " <path> vmfolder path  <Datacenter>/vm/<parent>/../<name>")
                .availableIf(optionImport);
        optionFolder.withRequiredArg().describedAs("name|path");
        /*
         * Option respool
         */
        final OptionSpecBuilder optionRespool = this.parser.accepts(OPTION_RESPOOL,
                "Resource Pool vim object name to use as import target. <name> resource pool name "
                        + " <path> resource path SDDC-Datacenter/host/<cluster>/Resources/Compute-ResourcePool/..<name>")
                .availableIf(optionImport);
        optionRespool.withRequiredArg().describedAs("name|path");
        /*
         * Option network
         */
        final OptionSpecBuilder optionNetwork = this.parser
                .accepts(OPTION_VM_NETWORKS,
                        "Reconfigure VM newtwork to the new specified backend starting from vm eth0 to eth9.")
                .availableIf(optionImport);
        optionNetwork.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");

        this.parser.mainOptions(optionHelp);

        optionHelp.forHelp();

        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)")
                .availableIf(optionImport, optionClone, optionRemove);
        optionVim.withRequiredArg().describedAs("vcenter");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableIf(optionImport, optionClone, optionRemove,
                optionCbt);

        this.parser.accepts(OPTION_POWERON, "PowerOn virtual machine after import").availableIf(optionImport,
                optionClone);
        this.parser.accepts(OPTION_NOVMDK, "Exclude VMDK contents.").availableIf(optionImport);
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return FCO;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
        comp.put("IM1", stringsCompleter(FCO));

        comp.put("IM11", stringsCompleter(OPTION_DRYRUN, OPTION_IMPORT, OPTION_DATASTORE, OPTION_NOVMDK, OPTION_FOLDER,
                OPTION_RESPOOL, OPTION_VM_NETWORKS, OPTION_HOST, OPTION_DATACENTER, OPTION_VIM, OPTION_URL));

        comp.put("IM12", stringsCompleter(OPTION_REMOVE, OPTION_FORCE));
        comp.put("IM13", stringsCompleter(OPTION_CLONE, OPTION_NAME));
        comp.put("IM99", stringsCompleter(OPTION_HELP));
        return "|IM1 IM11*|IM1 IM12*|IM1 IM13*|IM1 IM99?";

    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        return "EnyityType  Entity Description      uuid    name    moref%n"
                + "vm        Virtual Machine            X       X        X%n"
                + "ivd       Improved Virtual Disks     X       X         %n"
                + "vapp      Virtual Appliance          X       X        X%n";

    }

    @Override
    public String helpExample() {
        return "fco " + OPTION_CLONE + " vapp:testVapp " + OPTION_NAME + " test " + OPTION_VIM
                + " 9a583042-cb9d-5673-cbab-56a02a91805d\n"
                + "\tClone the virtualApp to a new vApp named test on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"

                + "fco " + OPTION_REMOVE + " " + OPTION_FORCE + " vapp:testVapp vm:vm-123\n"
                + "\tDelete the vapp:testVapp and the vm (moref vm-123) , forcing a poweroff if On\n\n";

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
        if (options.has(OPTION_IMPORT)) {
            setVappImport(true);
        }
        if (options.has(OPTION_REMOVE)) {
            setRemove(true);
        }
        if (options.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
            setCbt(true);
        }
        if (options.has(OPTION_NAME)) {
            setVappName(options.valueOf(OPTION_NAME).toString());
        }
        if (options.has(OPTION_HOST)) {
            setHostName(options.valueOf(OPTION_HOST).toString());

        }
        if (options.has(OPTION_DATASTORE)) {
            setDatastoreName(options.valueOf(OPTION_DATASTORE).toString());
        }
        if (options.has(OPTION_DATACENTER)) {
            setDatacenterName(options.valueOf(OPTION_DATACENTER).toString());
        }
        if (options.has(OPTION_URL)) {
            try {
                this.setUrlPath(options.valueOf(OPTION_URL).toString());
            } catch (final MalformedURLException e) {
                throw new ParsingException(OPTION_URL, e.getLocalizedMessage());
            }
        }
        if (options.has(OPTION_FOLDER)) {
            setFolderName(options.valueOf(OPTION_FOLDER).toString());
        } else {
            setFolderName(CoreGlobalSettings.getVmFilter());
        }

        if (options.has(OPTION_VM_NETWORKS)) {
            setVmNetworksName(options.valueOf(OPTION_VM_NETWORKS).toString());
        }

        if (options.has(OPTION_RESPOOL)) {
            setResourcePoolName(options.valueOf(OPTION_RESPOOL).toString());
        } else {
            setResourcePoolName(CoreGlobalSettings.getRpFilter());
        }

        if (options.has(OPTION_NOVMDK)) {
            setNoVmdk(true);
        }
        if (options.has(OPTION_DRYRUN)) {
            getOptions().setDryRun(true);
        }
        if (options.has(OPTION_POWERON)) {
            setPowerOn(true);
        }

        if (options.has(OPTION_VIM)) {
            getOptions().setVim(options.valueOf(OPTION_VIM).toString());
        }
        if (options.has(OPTION_CLONE)) {
            setClone(true);
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }

}
