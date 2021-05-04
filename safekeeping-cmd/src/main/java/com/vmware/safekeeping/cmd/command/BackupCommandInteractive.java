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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.report.BackupInteractive;
import com.vmware.safekeeping.cmd.report.BackupVappInteractive;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.PrettyBoolean.PrettyBooleanValues;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.BackupCommand;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.enums.QueryBlocksOption;
import com.vmware.safekeeping.core.type.enums.VddkTransportMode;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.RegexMatcher;

public class BackupCommandInteractive extends AbstractCommandWithOptions implements ICommandInteractive {
    private static final String OPTION_NOVMDK = "novmdk";
    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_COMPRESSION = "compression";
    private static final String OPTION_CIPHER = "cipher";

    private static final String OPTION_BACKUP_MODE_FULL = "full";

    private static final String OPTION_FORCE = "force";

    private static final String OPTION_TRANSPORT = "transport";

    private static final String OPTION_ALL = "all";
    private static final String OPTION_VIM = "vim";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_THREADS = "threads";
    private static final String OPTION_BLOCK_SIZE = "block";
    private static final String OPTION_QUERY_BLOCKS_MODE = "query";

    private static final String COMMAND_DESCRIPTION = "Backup any specified Entities (virtual machine, Improved Virtual Disks, vApp)";
    private static final String OPTION_NO_VSS = "novss";

    public static final String BACKUP = "backup";

    static String helpGlobalSummary() {
        final BackupCommandInteractive info = new BackupCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;

    private final List<String> targetList;

    public BackupCommandInteractive() {
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
                result = actionBackupInteractive(connetionManager);
            } else {
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(this.logger, Vmbk.NO_CONNECTION));
            }
        }
        return result;
    }

    private OperationStateList actionBackupInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<AbstractCoreResultActionBackup> resultActions = actionBackup(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            IoFunction.println("succeded\tfailed\tskipped\tEntity");
            opLoop: for (final AbstractCoreResultActionBackup resultAction : resultActions) {
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

    public List<AbstractCoreResultActionBackup> actionBackup(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<AbstractCoreResultActionBackup> result = new LinkedList<>();
        final List<IFirstClassObject> backupList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
        for (final IFirstClassObject fco : backupList) {
            if (fco instanceof VirtualAppManager) {

                final CoreResultActionVappBackup rab = new CoreResultActionVappBackup(fco, getOptions());
                rab.setInteractive(new BackupVappInteractive(rab));
                result.add(rab);
            } else if (fco instanceof VirtualMachineManager) {
                final CoreResultActionVmBackup rab = new CoreResultActionVmBackup(fco, getOptions());
                rab.setInteractive(new BackupInteractive(rab, null));
                result.add(rab);
            } else if (fco instanceof ImprovedVirtualDisk) {
                final CoreResultActionIvdBackup rab = new CoreResultActionIvdBackup(fco, getOptions());
                rab.setInteractive(new BackupInteractive(rab, null));
                result.add(rab);
            } else {
                // unsupported type
            }
        }
        actionBackup(connetionManager.getRepositoryTarget(), result);
        return result;
    }

    public void actionBackup(final ITarget iTarget, final List<AbstractCoreResultActionBackup> resultList)
            throws CoreResultActionException {
        try {
            for (final AbstractCoreResultActionBackup rab : resultList) {
                if (!rab.isAbortedOrFailed()) {
                    final BackupCommand backupFcoCommand = new BackupCommand(rab);
                    backupFcoCommand.actionBackup(iTarget, rab);
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

        final OptionSpecBuilder optionCompression = this.parser
                .accepts(OPTION_COMPRESSION, "Enable/disable compression.").availableUnless(optionHelp);

        optionCompression.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
        final OptionSpecBuilder optionCipher = this.parser.accepts(OPTION_CIPHER, "Enable/disable encryption.")
                .availableUnless(optionHelp);
        optionCipher.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
        final OptionSpecBuilder optionTransport = this.parser
                .accepts(OPTION_TRANSPORT, "Prefered Transport Mode (nbd,nbdssl,san,hotadd).")
                .availableUnless(optionHelp);
        optionTransport.withRequiredArg().ofType(VddkTransportMode.class).describedAs("nbd|nbdssl|san|hotadd");

        final OptionSpecBuilder optionQueryBlocksMode = this.parser.accepts(OPTION_QUERY_BLOCKS_MODE,
                "Suggested method to collect any changed block in a full backup.  ( (A)llocatedBlocks,(C)hangedDiskAreas,(F)ullBlocks )")
                .availableUnless(optionHelp);
        optionQueryBlocksMode.withRequiredArg()
                .withValuesConvertedBy(
                        RegexMatcher.regex("AllocatedBlocks|ChangedDiskAreas|FullBlocks|a|c|f|allocated|changed|full",
                                Pattern.CASE_INSENSITIVE))
                .describedAs("a|c|f");

        this.parser
                .accepts(OPTION_BACKUP_MODE_FULL,
                        "Specify if the backup mode is full. Otherwise is automatically detected")
                .availableUnless(optionHelp);

        final OptionSpecBuilder optionThreads = this.parser
                .accepts(OPTION_THREADS, "Number of Threads[" + getOptions().getNumberOfThreads() + "].")
                .availableUnless(optionHelp);
        optionThreads.withRequiredArg().ofType(Integer.class).describedAs("number");
        final OptionSpecBuilder optionBlocks = this.parser
                .accepts(OPTION_BLOCK_SIZE, "Max Block size[" + getOptions().getMaxBlockSize() + "].")
                .availableUnless(optionHelp);
        optionBlocks.withRequiredArg().describedAs("size(k|M|G|T|P)")
                .withValuesConvertedBy(RegexMatcher.regex(PrettyNumber.pattern));

        this.parser.mainOptions(optionHelp);

        optionHelp.forHelp();
        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);
        optionVim.withRequiredArg().describedAs("vcenter");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionHelp);

        this.parser.accepts(OPTION_FORCE, "Force the  Virtual machine operation.").availableUnless(optionHelp);
        this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
                .availableUnless(optionHelp).withOptionalArg().ofType(FirstClassObjectType.class)
                .describedAs("vm|ivd|vapp");
        this.parser.accepts(OPTION_NOVMDK, "Exclude VMDK contents.").availableUnless(optionHelp);
        this.parser.accepts(OPTION_NO_VSS, "Doesn't use Windows VSS to snapshot Windows.").availableUnless(optionHelp);

        return new AbstractMap.SimpleEntry<>(getCommandName(), this);

    }

    @Override
    public String getCommandName() {
        return BACKUP;
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    /**
     * @return the options
     */
    @Override
    public CoreBackupOptions getOptions() {
        return (CoreBackupOptions) this.options;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("B1", stringsCompleter(BACKUP));
        comp.put("B11", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_TRANSPORT, OPTION_BACKUP_MODE_FULL,
                OPTION_NOVMDK, OPTION_FORCE, OPTION_COMPRESSION, OPTION_VIM));
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
        return String.format(" backup %s\n\tBackup any kind of object.\n\n", OPTION_ALL) + String.format(
                " backup -%s   vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330 \n\tStart a backup in full mode of 3 different Vm:1st by name,2nd by Moref,3rd by UUID.\n\n",
                OPTION_BACKUP_MODE_FULL)
                + String.format(
                        " backup %s ivd %s 9a583042-cb9d-5673-cbab-56a02a91805d\n\tBackup any Improved Virtual Disks managed by vcenter 9a583042-cb9d-5673-cbab-56a02a91805d.\n\n",
                        OPTION_ALL, OPTION_VIM);
    }

    @Override
    public String helpSummary() {
        return BackupCommandInteractive.COMMAND_DESCRIPTION;
    }

    @Override
    public final void initialize() {
        help = false;
        setOptions(new CoreBackupOptions());
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet optionSet = parseArguments(this.parser, arguments, getOptions());
        if (optionSet.has(OPTION_HELP)) {
            setHelp(true);
        } else {
            if (optionSet.has(OPTION_THREADS)) {
                getOptions().setNumberOfThreads(Integer.parseInt(optionSet.valueOf(OPTION_THREADS).toString()));
            }
            if (optionSet.has(OPTION_COMPRESSION)) {
                getOptions().setCompression(PrettyBoolean.parseBoolean(optionSet.valueOf(OPTION_COMPRESSION)));
            } else {
                getOptions().setCompression(CoreGlobalSettings.isCompressionEnable());
            }

            if (optionSet.has(OPTION_CIPHER)) {
                getOptions().setCipher(PrettyBoolean.parseBoolean(optionSet.valueOf(OPTION_CIPHER)));
            } else {
                getOptions().setCipher(CoreGlobalSettings.isCipherEnable());
            }
            if (optionSet.has(OPTION_BACKUP_MODE_FULL)) {
                getOptions().setRequestedBackupMode(BackupMode.FULL);
            }

            if (optionSet.has(OPTION_TRANSPORT)) {
                getOptions().setRequestedTransportModes(optionSet.valueOf(OPTION_TRANSPORT).toString());
                final String transportCheck = Utility.checkTransportMode(getOptions().getRequestedTransportModes());
                if (StringUtils.isNotEmpty(transportCheck)) {
                    throw new ParsingException(OPTION_TRANSPORT, transportCheck);
                }
            }
            if (optionSet.has(OPTION_QUERY_BLOCKS_MODE)) {
                getOptions().setQueryBlocksOption(QueryBlocksOption.parse(optionSet.valueOf(OPTION_QUERY_BLOCKS_MODE)));

            }

            if (optionSet.has(OPTION_ALL)) {
                getOptions().setAnyFcoOfType(FirstClassObjectFilterType.parse(optionSet.valueOf(OPTION_ALL),
                        FirstClassObjectFilterType.any | FirstClassObjectFilterType.all));
            }
            if (optionSet.has(OPTION_VIM)) {
                getOptions().setVim(optionSet.valueOf(OPTION_VIM).toString());
            }
            if (optionSet.has(OPTION_NO_VSS)) {
                getOptions().getQuisceSpec().setUseWindowsVss(false);
            }
            if (optionSet.has(OPTION_BLOCK_SIZE)) {
                int blockSize = PrettyNumber.toInteger(optionSet.valueOf(OPTION_BLOCK_SIZE).toString());
                if (blockSize < Utility.ONE_MBYTES) {
                    blockSize *= Utility.ONE_MBYTES;
                }
                getOptions().setMaxBlockSize(blockSize);
            }
            getOptions().setNoVmdk(optionSet.has(OPTION_NOVMDK));
            getOptions().setDryRun(optionSet.has(OPTION_DRYRUN));
            getOptions().setForce(optionSet.has(OPTION_FORCE));
        }
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
