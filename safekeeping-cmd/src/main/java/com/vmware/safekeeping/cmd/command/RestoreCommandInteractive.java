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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.report.RestoreInteractive;
import com.vmware.safekeeping.cmd.report.RestoreVappInteractive;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.RestoreCommand;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.options.CoreRestoreVmdkOption;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.list.CoreResultActionRestoreList;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.SearchManagementEntity;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.enums.SearchManagementEntityInfoType;
import com.vmware.safekeeping.core.type.enums.VddkTransportMode;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class RestoreCommandInteractive extends AbstractCommandWithOptions implements ICommandInteractive {
    private static final String COMMAND_DESCRIPTION = "Restore any specified Entities (virtual machine, Improved Virtual Disks, vApp).";
    private static final String OPTION_TRANSPORT = "transport";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_NOVMDK = "novmdk";
    private static final String OPTION_DRYRUN = "dryrun";
    private static final String OPTION_NAME = "name";
    private static final String OPTION_GENERATION = "generation";
    private static final String OPTION_HOST = "host";
    private static final String OPTION_DATACENTER = "datacenter";
    private static final String OPTION_DATASTORE = "datastore";
    private static final String OPTION_FOLDER = "folder";
    private static final String OPTION_RESPOOL = "respool";
    private static final String OPTION_VM_NETWORKS = "network";
    private static final String OPTION_POWERON = "poweron";
    private static final String OPTION_THREADS = "threads";
    private static final String OPTION_OVERWRITE = "overwrite";
    private static final String OPTION_FORCE = "force";
    private static final String OPTION_IMPORT_VMX = "import";
    private static final String OPTION_RECOVERY = "recovery";

    public static final String RESTORE = "restore";
    private static final String OPTION_VMDK_DATASTORES = "vmdkdatastore";
    private static final String OPTION_VMDK_PROFILE = "vmdkprofile";

    static String helpGlobalSummary() {
        final RestoreCommandInteractive info = new RestoreCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private final List<String> targetList;

    private boolean help;

    public RestoreCommandInteractive() {
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
                result = actionRestoreInteractive(connetionManager);
            } else {
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION));
            }
        }
        return result;
    }

    private OperationStateList actionRestoreInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionRestoreList resultsList = actionRestore(connetionManager);
        if (resultsList.getState() == OperationState.SUCCESS) {
            if (resultsList.size() > 0) {
                IoFunction.println("succeded\tfailed\tskipped\tEntity");
                opLoop: for (final AbstractCoreResultActionRestore resultAction : resultsList.getResults()) {
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
                        IoFunction.printf("x\t \t \t\t%s%n", resultAction.getFcoToString());
                        break;
                    default:
                        break;

                    }
                }
                final StatisticResult total = ICoreResultAction.getResultStatistic(resultsList.getResults());
                IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk,
                        EntityType.VirtualApp, EntityType.K8sNamespace }, total);
            } else {
                result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
            }
        } else {
            result.add(OperationState.FAILED, IoFunction.showWarning(this.logger, resultsList.getReason()));
        }

        return result;
    }

    public CoreResultActionRestoreList actionRestore(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final CoreResultActionRestoreList result = new CoreResultActionRestoreList();
        try {
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(
                    connetionManager.getRepositoryTarget());
            final List<IFirstClassObject> restoreList = getTargetFcoFromRepository(connetionManager, globalFcoCatalog);
            for (final IFirstClassObject fco : restoreList) {
                if (fco instanceof VirtualAppManager) {
                    final CoreResultActionVappRestore rab = new CoreResultActionVappRestore(fco, getOptions());
                    rab.setInteractive(new RestoreVappInteractive(rab));
                    result.add(rab);
                } else if (fco instanceof VirtualMachineManager) {
                    final CoreResultActionVmRestore rab = new CoreResultActionVmRestore(fco, getOptions());
                    rab.setInteractive(new RestoreInteractive(rab, null));
                    result.add(rab);
                } else if (fco instanceof ImprovedVirtualDisk) {
                    final CoreResultActionIvdRestore rab = new CoreResultActionIvdRestore(fco, getOptions());
                    rab.setInteractive(new RestoreInteractive(rab, null));
                    result.add(rab);
                } else {
                    throw new CoreResultActionException(fco);
                }
            }
            actionRestore(connetionManager.getRepositoryTarget(), (result.getResults()));
        } catch (final IOException e) {
            Utility.logWarning(this.logger, e);
            result.failure(e);
        } finally {
            result.done();
        }
        return result;
    }

    public void actionRestore(final ITarget iTarget, final List<AbstractCoreResultActionRestore> resultList)
            throws CoreResultActionException {
        for (final AbstractCoreResultActionRestore rar : resultList) {
            if (!rar.isAbortedOrFailed()) {
                final RestoreCommand restoreFcoCommand = new RestoreCommand(rar);
                restoreFcoCommand.actionRestore(iTarget, rar);
            }
        }
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        final OptionSpecBuilder optionTransport = this.parser
                .accepts(OPTION_TRANSPORT, "Prefered Transport Mode (nbd,nbdssl,san,hotadd).")
                .availableUnless(optionHelp);
        optionTransport.withRequiredArg().ofType(VddkTransportMode.class).describedAs("nbd|nbdssl|san|hotadd");

        final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME, "New name for the restored <EnyityType>")
                .availableUnless(optionHelp);
        optionName.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionHost = this.parser
                .accepts(OPTION_HOST, "VMware ESX host vim object name to use as restore target.")
                .availableUnless(optionHelp);
        optionHost.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionDatacenter = this.parser
                .accepts(OPTION_DATACENTER, "Datacenter - vim object name to use as restore target")
                .availableUnless(optionHelp);
        optionDatacenter.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionDatastore = this.parser
                .accepts(OPTION_DATASTORE, "Datastore - vim object name to use as restore target.")
                .availableUnless(optionHelp);
        optionDatastore.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionThreads = this.parser
                .accepts(OPTION_THREADS, "Number of Threads[" + getOptions().getNumberOfThreads() + "].")
                .availableUnless(optionHelp);
        optionThreads.withRequiredArg().ofType(Integer.class).describedAs("number");

        /*
         * Option folder
         */
        final OptionSpecBuilder optionFolder = this.parser
                .accepts(OPTION_FOLDER,
                        "Virtual Machine Folder vim object name to use as restore target. <name> vmfolder name"
                                + " <path> vmfolder path  <Datacenter>/vm/<parent>/../<name>")
                .availableUnless(optionHelp);
        optionFolder.withRequiredArg().describedAs("name|path");
        /*
         * Option respool
         */
        final OptionSpecBuilder optionRespool = this.parser.accepts(OPTION_RESPOOL,
                "Resource Pool vim object name to use as restore target. <name> resource pool name "
                        + " <path> resource path SDDC-Datacenter/host/<cluster>/Resources/Compute-ResourcePool/..<name>")
                .availableUnless(optionHelp);
        optionRespool.withRequiredArg().describedAs("name|path");
        /*
         * Option network
         */
        final OptionSpecBuilder optionNetwork = this.parser
                .accepts(OPTION_VM_NETWORKS,
                        "Reconfigure VM newtwork to the new specified backend starting from vm eth0 to eth9. ")
                .availableUnless(optionHelp);
        optionNetwork.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");
        /*
         * Option VMDK datastores
         */
        final OptionSpecBuilder optionVmdkDatastore = this.parser.accepts(OPTION_VMDK_DATASTORES,
                "Reconfigure VM VMDK backend datastore to the new specified backend starting from vm disk0 to disk(n).")
                .availableUnless(optionHelp);
        optionVmdkDatastore.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");
        /*
         * Option VMDK profiles
         */
        final OptionSpecBuilder optionVmdkProfile = this.parser.accepts(OPTION_VMDK_PROFILE,
                "Reconfigure VM VMDK storage profile to the new specified backend starting from vm disk0 to disk(n).")
                .availableUnless(optionHelp);
        optionVmdkProfile.withRequiredArg().withValuesSeparatedBy(",").describedAs("name,..,name");

        this.parser.mainOptions(optionHelp);

        optionHelp.forHelp();

        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
        optionVim.withRequiredArg().describedAs("vcenter");
        final OptionSpecBuilder optionGeneration = this.parser.accepts(OPTION_GENERATION, "Generation <id> to restore")
                .availableUnless(optionHelp);
        optionGeneration.withRequiredArg().ofType(Integer.class).describedAs("id");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionHelp);

        this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
                .availableUnless(optionHelp).withOptionalArg().ofType(FirstClassObjectType.class)
                .describedAs("vm|ivd|vapp");
        this.parser.accepts(OPTION_POWERON, "PowerOn virtual machine after restore").availableUnless(optionHelp);
        this.parser.accepts(OPTION_NOVMDK, "Exclude VMDK contents.").availableUnless(optionHelp);
        this.parser.accepts(OPTION_OVERWRITE, "Overwrite an existing FCO").availableUnless(optionHelp);
        this.parser.accepts(OPTION_FORCE, "Force the Overwrite.").availableIf(OPTION_OVERWRITE);
        this.parser.accepts(OPTION_IMPORT_VMX, "Restore VM inporting VMX file").availableUnless(optionHelp);
        this.parser.accepts(OPTION_RECOVERY, "Recover the VM with the original property").availableUnless(optionHelp);

        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return RESTORE;
    }

    @Override
    protected String getLogName() {
        return "RestoreCommand";
    }

    /**
     * @return the options
     */
    @Override
    public CoreRestoreOptions getOptions() {
        return (CoreRestoreOptions) this.options;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {
        comp.put("R1", stringsCompleter(RESTORE));

        comp.put("R11",
                stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_TRANSPORT, OPTION_DATASTORE, OPTION_NOVMDK,
                        OPTION_TRANSPORT, OPTION_FOLDER, OPTION_RESPOOL, OPTION_VM_NETWORKS, OPTION_HOST,
                        OPTION_GENERATION, OPTION_DATACENTER, OPTION_VIM, OPTION_IMPORT_VMX));
        comp.put("R12", stringsCompleter(OPTION_HELP));
        return "|R1 R11*|R1 R12?";

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
        return String.format(" restore %s%n\tRecover any object previously backup.%n%n", OPTION_ALL) + String.format(
                " restore %s 4 vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330%n\tStart a restore of the 4th generation profile of 3 Virtual Machines :1st by name,2nd by Moref,3rd by UUID.%n%n",
                OPTION_GENERATION)
                + String.format(
                        " restore %s vm %s 9a583042-cb9d-5673-cbab-56a02a91805d%n\tRestore any Virtual Machine managed by vcenter 9a583042-cb9d-5673-cbab-56a02a91805d.%n%n",
                        OPTION_ALL, OPTION_VIM)
                + String.format(
                        " restore vm:testVM  %s \"newVM\" %s \"myDatacenter\" %s \"myDatastore\" %s \"myResPool\" %n%n\tRestore vm:testVM  to an new VM named NewVm using as Datacenter:myDatacenter, as Datastore:myDatastore and as Resource Pool:myResPool %n%n",
                        OPTION_NAME, OPTION_DATACENTER, OPTION_DATASTORE, OPTION_RESPOOL)
                + String.format(
                        " restore vm:testVM  %s \"newVM\" %s \"myVmFolder\" %s \"mynetwork01,mynetwork02,,,mynetwork05\"%n%n\tRestore vm:testVM  to an new VM named NewVm using as Virtual Machine folder: myVmFolder and reconfigure the vm network backend to eth0:mynetwork01,eth1:mynetwork02 and eth4:mynetwork05 %n%n",
                        OPTION_NAME, OPTION_FOLDER, OPTION_VM_NETWORKS);

    }

    @Override
    public String helpSummary() {
        return COMMAND_DESCRIPTION;
    }

    @Override
    public final void initialize() {
        help = false;
        setOptions(new CoreRestoreOptions());
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {

        final OptionSet options = parseArguments(this.parser, arguments, getOptions());
        setHelp(options.has(OPTION_HELP));
        if (options.has(OPTION_NAME)) {
            getOptions().setName(options.valueOf(OPTION_NAME).toString());
        }
        if (options.has(OPTION_THREADS)) {
            getOptions().setNumberOfThreads(Integer.parseInt(options.valueOf(OPTION_THREADS).toString()));
        }
        if (options.has(OPTION_GENERATION)) {
            getOptions().setGenerationId((Integer) options.valueOf(OPTION_GENERATION));
        }

        if (options.has(OPTION_HOST)) {
            getOptions().setHost(new SearchManagementEntity(SearchManagementEntityInfoType.NAME,
                    options.valueOf(OPTION_HOST).toString()));

        }
        if (options.has(OPTION_DATASTORE)) {
            getOptions().setDatastore(new SearchManagementEntity(SearchManagementEntityInfoType.NAME,
                    options.valueOf(OPTION_DATASTORE).toString()));
        }
        if (options.has(OPTION_DATACENTER)) {
            getOptions().setDatacenter(new SearchManagementEntity(SearchManagementEntityInfoType.NAME,
                    options.valueOf(OPTION_DATACENTER).toString()));
        }
        if (options.has(OPTION_FOLDER)) {
            getOptions().setFolder(new SearchManagementEntity(SearchManagementEntityInfoType.NAME,
                    options.valueOf(OPTION_FOLDER).toString()));
        }

        if (options.has(OPTION_VM_NETWORKS)) {
            List<SearchManagementEntity> remappedNetwork = new ArrayList<>();
            final String[] newVmNetworksName = options.valueOf(OPTION_VM_NETWORKS).toString().split(",");
            for (String name : newVmNetworksName) {
                if (StringUtils.isEmpty(name)) {
                    remappedNetwork.add(null);
                } else {
                    remappedNetwork.add(new SearchManagementEntity(SearchManagementEntityInfoType.NAME, name));
                }
            }
            getOptions().getNetworks().addAll(remappedNetwork);
        }
        if (options.has(OPTION_VMDK_DATASTORES)) {
            final String[] newVmDatastoresName = options.valueOf(OPTION_VMDK_DATASTORES).toString().split(",");
            int i = 0;
            for (String name : newVmDatastoresName) {
                CoreRestoreVmdkOption disk;
                if (getOptions().getDisks().size() > i) {
                    disk = getOptions().getDisks().get(i);
                } else {
                    disk = new CoreRestoreVmdkOption(i);
                    getOptions().getDisks().add(disk);
                }
                if (StringUtils.isEmpty(name)) {
                    disk.setDatastore(null);
                } else {
                    disk.setDatastore(new SearchManagementEntity(SearchManagementEntityInfoType.NAME, name));
                }
                ++i;
            }
        }

        if (options.has(OPTION_VMDK_PROFILE)) {
            final String[] newVmSpbmProfile = options.valueOf(OPTION_VMDK_PROFILE).toString().split(",");
            int i = 0;
            for (String name : newVmSpbmProfile) {
                CoreRestoreVmdkOption disk;
                if (getOptions().getDisks().size() > i) {
                    disk = getOptions().getDisks().get(i);
                } else {
                    disk = new CoreRestoreVmdkOption(i);
                    getOptions().getDisks().add(disk);
                }
                if (StringUtils.isEmpty(name)) {
                    disk.setSpbmProfile(null);
                } else {
                    disk.setSpbmProfile(new SearchManagementEntity(SearchManagementEntityInfoType.NAME, name));
                }
                ++i;
            }
        }

        if (options.has(OPTION_RESPOOL)) {
            getOptions().setResourcePool(new SearchManagementEntity(SearchManagementEntityInfoType.NAME,
                    options.valueOf(OPTION_RESPOOL).toString()));
        }

        if (options.has(OPTION_TRANSPORT)) {
            getOptions().setRequestedTransportModes(options.valueOf(OPTION_TRANSPORT).toString());
            final String transportCheck = Utility.checkTransportMode(getOptions().getRequestedTransportModes());
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
        getOptions().setOverwrite(options.has(OPTION_OVERWRITE));
        getOptions().setRecover(options.has(OPTION_RECOVERY));
        getOptions().setImportVmxFile(options.has(OPTION_IMPORT_VMX));
        getOptions().setNoVmdk(options.has(OPTION_NOVMDK));
        getOptions().setDryRun(options.has(OPTION_DRYRUN));
        getOptions().setPowerOn(options.has(OPTION_POWERON));
        getOptions().setForce(options.has(OPTION_FORCE));
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
