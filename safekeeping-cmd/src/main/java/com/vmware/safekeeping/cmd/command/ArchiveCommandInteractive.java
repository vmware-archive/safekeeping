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

import static joptsimple.util.DateConverter.datePattern;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.safekeeping.cmd.report.CheckGenerationsInteractive;
import com.vmware.safekeeping.cmd.report.RemoveGenerationsInteractive;
import com.vmware.safekeeping.cmd.report.RemoveProfileInteractive;
import com.vmware.safekeeping.cmd.report.StatusInteractive;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractArchiveCommand;
import com.vmware.safekeeping.core.command.options.CoreArchiveOptions;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchiveStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationsList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItem;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItemsList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveIvdStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveProfile;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveProfilesList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemovedGenerationsList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveShow;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveShowList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveStatusList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVappStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVmStatus;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.info.InfoData;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.ArchiveObjects;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.RegexMatcher;

public class ArchiveCommandInteractive extends AbstractArchiveCommand implements ICommandInteractive {

    private static final String OPTION_LIST = "list";

    private static final String OPTION_CHECK = "check";

    private static final String OPTION_STATUS = "status";

    private static final String OPTION_SHOW = "show";

    private static final String OPTION_COMMIT = "commit";

    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_GENERATION = "generation";
    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_PROFILE = "profile";
    private static final String OPTION_OLDER_THAN = "olderThan";

    private static final String OPTION_NEWER_THAN = "newerThan";

    private static final String COMMAND_DESCRIPTION = "Archive management.";

    public static final String ARCHIVE = "archive";

    static String helpGlobalSummary() {
        final ArchiveCommandInteractive info = new ArchiveCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());

    }

    private VmbkParser parser;

    private final List<String> targetList;

    private boolean help;

    public ArchiveCommandInteractive() {
        setHelp(false);
        this.targetList = new LinkedList<>();
    }

    @Override
    public void initialize() {
        help = false;
        setOptions(new CoreArchiveOptions());
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
                if (getOptions().isList()) {
                    result = actionListInteractive(connetionManager);
                } else if (getOptions().isCheck()) {
                    result = actionCheckInteractive(connetionManager);
                } else if (getOptions().isRemove()) {
                    if (getOptions().isProfile()) {
                        result = actionRemoveProfileInteractive(connetionManager);
                    } else {
                        result = actionRemoveGenerationsInteractive(connetionManager);
                    }
                } else if (getOptions().isStatus()) {
                    result = actionStatusInteractive(connetionManager);
                } else if (getOptions().getShow() != ArchiveObjects.NONE) {
                    result = actionShowInteractive(connetionManager);
                } else {
                    result = new OperationStateList(OperationState.FAILED,
                            IoFunction.showWarning(this.logger, Vmbk.INVALID_COMMAND));
                }
            } else {
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(this.logger, Vmbk.NO_TARGET_CONNECTION));
            }
        }
        return result;
    }

    private OperationStateList actionCheckInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {

        final OperationStateList result = new OperationStateList();
        final CoreResultActionArchiveCheckGenerationsList resultActionList = actionCheckGenerationsInteractive(
                connetionManager.getRepositoryTarget(),
                new CoreResultActionArchiveCheckGenerationsList(connetionManager.getRepositoryTarget()));
        if (!resultActionList.getItems().isEmpty()) {
            opLoop: for (final CoreResultActionArchiveCheckGenerationWithDependencies resultAction : resultActionList
                    .getItems()) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    break;
                case SKIPPED:
                    break;
                case SUCCESS:
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActionList.getItems());
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                    EntityType.ImprovedVirtualDisk }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionListInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionArchiveItemsList resultActionList = actionList(connetionManager.getRepositoryTarget(),
                new CoreResultActionArchiveItemsList(connetionManager.getRepositoryTarget()));
        if (resultActionList.isSuccessfulOrSkipped()) {
            if (!resultActionList.getItems().isEmpty()) {
                IoFunction.println(InfoData.TABLE_KEYS);
                IoFunction.println();
                IoFunction.println(InfoData.TABLE_HEADER);

                opLoop: for (final CoreResultActionArchiveItem resultAction : resultActionList.getItems()) {

                    switch (result.add(resultAction)) {
                    case ABORTED:
                        IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                        break opLoop;
                    case FAILED:
                        break;
                    case SKIPPED:
                        break;
                    case SUCCESS:
                        IoFunction.println(resultAction.getInfo().toString());
                        break;
                    default:
                        break;
                    }
                }
                final StatisticResult total = ICoreResultAction.getResultStatistic(resultActionList.getItems());
                IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk,
                        EntityType.VirtualApp, EntityType.K8sNamespace }, total);
            } else {
                result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
            }
        } else {
            result.add(resultActionList);
        }
        return result;
    }

    private OperationStateList actionRemoveGenerationsInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionArchiveRemovedGenerationsList resultActionRemoveGenerationsList = actionRemoveGenerationsInteractive(
                connetionManager.getRepositoryTarget(),
                new CoreResultActionArchiveRemovedGenerationsList(connetionManager.getRepositoryTarget()));
        if (resultActionRemoveGenerationsList.isSuccessfulOrSkipped()) {
            if (!resultActionRemoveGenerationsList.getItems().isEmpty()) {
                opLoop: for (final CoreResultActionArchiveRemoveGenerationWithDependencies resultAction : resultActionRemoveGenerationsList
                        .getItems()) {
                    switch (result.add(resultAction)) {
                    case ABORTED:
                        IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                        break opLoop;
                    case FAILED:
                        break;
                    case SKIPPED:
                        break;
                    case SUCCESS:
                        break;
                    default:
                        break;
                    }
                }
                final StatisticResult total = ICoreResultAction
                        .getResultStatistic(resultActionRemoveGenerationsList.getItems());
                IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                        EntityType.ImprovedVirtualDisk }, total);
            } else {
                result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
            }
        } else {
            result.add(resultActionRemoveGenerationsList);
        }
        return result;
    }

    private OperationStateList actionRemoveProfileInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionArchiveRemoveProfilesList resultActionRemoveProfile = actionRemoveProfileInteractive(
                connetionManager.getRepositoryTarget(),
                new CoreResultActionArchiveRemoveProfilesList(connetionManager.getRepositoryTarget()));
        if (resultActionRemoveProfile.isSuccessfulOrSkipped()) {
            if (!resultActionRemoveProfile.getItems().isEmpty()) {
                opLoop: for (final CoreResultActionArchiveRemoveProfile resultAction : resultActionRemoveProfile
                        .getItems()) {
                    switch (result.add(resultAction)) {
                    case ABORTED:
                        IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                        break opLoop;
                    case FAILED:
                    case SKIPPED:
                    case SUCCESS:
                        break;
                    default:
                        break;
                    }
                }
                final StatisticResult total = ICoreResultAction
                        .getResultStatistic(resultActionRemoveProfile.getItems());
                IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                        EntityType.ImprovedVirtualDisk }, total);
            } else {
                result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
            }
        } else {
            result.add(resultActionRemoveProfile);
        }
        return result;
    }

    private OperationStateList actionShowInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionArchiveShowList resultActionList = actionShowInteractive(
                connetionManager.getRepositoryTarget(),
                new CoreResultActionArchiveShowList(connetionManager.getRepositoryTarget()));
        if (!resultActionList.getItems().isEmpty()) {
            opLoop: for (final CoreResultActionArchiveShow resultAction : resultActionList.getItems()) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    break;
                case SKIPPED:
                    break;
                case SUCCESS:
                    IoFunction.println(resultAction.getArchiveObject().toString());
                    IoFunction.println(resultAction.getContent());
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActionList.getItems());
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                    EntityType.ImprovedVirtualDisk }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionStatusInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionArchiveStatusList resultActionList = actionStatusInteractive(
                connetionManager.getRepositoryTarget(),
                new CoreResultActionArchiveStatusList(connetionManager.getRepositoryTarget()));
        if (!resultActionList.getItems().isEmpty()) {
            opLoop: for (final AbstractCoreResultActionArchiveStatus resultAction : resultActionList.getItems()) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    break;
                case SKIPPED:
                    break;
                case SUCCESS:
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActionList.getItems());
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.VirtualApp,
                    EntityType.ImprovedVirtualDisk }, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    public CoreResultActionArchiveCheckGenerationsList actionCheckGenerationsInteractive(final ITarget target,
            final CoreResultActionArchiveCheckGenerationsList craarl) throws CoreResultActionException {
        try {
            craarl.start();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target);
            final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
            final float percIncrementPerEntity = 100.0f / entities.size();
            for (final ManagedFcoEntityInfo vmInfo : entities) {
                if (Vmbk.isAbortTriggered()) {
                    craarl.aborted();
                    break;
                }
                final CoreResultActionArchiveCheckGenerationWithDependencies resultAction = new CoreResultActionArchiveCheckGenerationWithDependencies(
                        vmInfo, target);
                craarl.getItems().add(resultAction);
                actionCheckGenerations(target, resultAction, new CheckGenerationsInteractive(resultAction));

                craarl.progressIncrease(percIncrementPerEntity);
            }
        } catch (final Exception e) {
            craarl.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            craarl.done();
        }
        return craarl;
    }

    public CoreResultActionArchiveRemovedGenerationsList actionRemoveGenerationsInteractive(final ITarget target,
            final CoreResultActionArchiveRemovedGenerationsList craarl) throws CoreResultActionException {
        try {
            craarl.start();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target);
            final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
            final float percIncrementPerEntity = 100.0f / entities.size();
            for (final ManagedFcoEntityInfo vmInfo : entities) {
                if (Vmbk.isAbortTriggered()) {
                    craarl.aborted();
                    break;
                }
                final CoreResultActionArchiveRemoveGenerationWithDependencies resultAction = new CoreResultActionArchiveRemoveGenerationWithDependencies(
                        vmInfo, target);
                craarl.getItems().add(resultAction);
                actionRemoveGenerations(target, resultAction, new RemoveGenerationsInteractive(resultAction));

                craarl.progressIncrease(percIncrementPerEntity);
            }
        } catch (final Exception e) {
            craarl.failure(e);
            Utility.logWarning(this.logger, e);

        } finally {
            craarl.done();
        }
        return craarl;
    }

    protected CoreResultActionArchiveRemoveProfilesList actionRemoveProfileInteractive(final ITarget target,
            final CoreResultActionArchiveRemoveProfilesList craarl) throws CoreResultActionException {
        try {
            craarl.start();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target);
            final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
            for (final ManagedFcoEntityInfo entity : entities) {
                if (Vmbk.isAbortTriggered()) {
                    craarl.aborted();
                    break;
                }
                final CoreResultActionArchiveRemoveProfile resultAction = new CoreResultActionArchiveRemoveProfile(
                        entity, target);
                craarl.getItems().add(resultAction);
                actionRemoveProfile(target, globalFcoCatalog, resultAction, new RemoveProfileInteractive(resultAction));
            }
        } catch (final Exception e) {
            craarl.failure(e);
            Utility.logWarning(this.logger, e);

        } finally {
            craarl.done();
        }
        return craarl;
    }

    public CoreResultActionArchiveShowList actionShowInteractive(final ITarget target,
            final CoreResultActionArchiveShowList craarl) throws CoreResultActionException {
        try {
            craarl.start();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target);
            final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
            if (getOptions().getShow() == ArchiveObjects.GLOBALPROFILE) {
                final CoreResultActionArchiveShow resultAction = new CoreResultActionArchiveShow(
                        ManagedFcoEntityInfo.newNullManagedEntityInfo(), target);
                craarl.getItems().add(resultAction);
                actionShow(target, resultAction);

            } else {
                final float percIncrementPerEntity = 100.0F / entities.size();
                for (final ManagedFcoEntityInfo vmInfo : entities) {
                    if (Vmbk.isAbortTriggered()) {
                        craarl.aborted();
                        break;
                    }
                    final CoreResultActionArchiveShow resultAction = new CoreResultActionArchiveShow(vmInfo, target);
                    craarl.getItems().add(resultAction);
                    actionShow(target, resultAction);
                    craarl.progressIncrease(percIncrementPerEntity);
                }
            }
        } catch (final Exception e) {
            craarl.failure(e);
            Utility.logWarning(this.logger, e);

        } finally {
            craarl.done();
        }
        return craarl;
    }

    protected CoreResultActionArchiveStatusList actionStatusInteractive(final ITarget target,
            final CoreResultActionArchiveStatusList result) throws CoreResultActionException {
        try {
            result.start();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target);
            final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
            for (final ManagedFcoEntityInfo fcoEntity : entities) {
                if (Vmbk.isAbortTriggered()) {
                    result.aborted();
                    break;
                } else {

                    AbstractCoreResultActionArchiveStatus resultAction = null;
                    try {
                        switch (fcoEntity.getEntityType()) {
                        case VirtualMachine:
                            resultAction = new CoreResultActionArchiveVmStatus(target, fcoEntity);
                            break;
                        case ImprovedVirtualDisk:
                            resultAction = new CoreResultActionArchiveIvdStatus(target, fcoEntity);
                            break;
                        case VirtualApp:
                            resultAction = new CoreResultActionArchiveVappStatus(target, fcoEntity);
                            break;
                        default:
                            break;
                        }
                        actionStatus(target, globalFcoCatalog, resultAction, new StatusInteractive(resultAction));
                    } catch (final Exception e) {
                        Utility.logWarning(this.logger, e);
                        if (resultAction != null) {
                            resultAction.failure(e);
                        }
                    } finally {
                        result.getItems().add(resultAction);
                        if (resultAction != null) {
                            resultAction.done();
                        }
                    }
                }
            }
        } catch (final Exception e) {
            result.failure(e);
            Utility.logWarning(this.logger, e);

        } finally {
            result.done();
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionStatus = this.parser.accepts(OPTION_STATUS, "Show status of the archives.");

        final OptionSpecBuilder optionShow = this.parser.accepts(OPTION_SHOW,
                "show on stdout the content of the selected archive file content ");
        final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST,
                "List entity with various filters. (virtual machine, Improved Virtual Disks, vApp).");

        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE,
                "Delete a specific generation archive or a profile.");

        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        final OptionSpecBuilder optionCheck = this.parser.accepts(OPTION_CHECK, "Validate the archives.");
        final OptionSpecBuilder optionCommit = this.parser.accepts(OPTION_COMMIT, "Force database data to commit.");

        this.parser.mainOptions(optionList, optionCheck, optionShow, optionRemove, optionStatus, optionCommit,
                optionHelp);
        optionShow.withRequiredArg().withValuesConvertedBy(RegexMatcher.regex(
                "GlobalProfile|FcoProfile|GenerationProfile|VmxFile|ReportFile|Md5File|VappConfig|global|fco|generation|vmx|report|md5|vapp",
                Pattern.CASE_INSENSITIVE))
                .describedAs("GlobalProfile|FcoProfile|GenerationProfile|VmxFile|ReportFile|Md5File|VappConfig");

        optionHelp.forHelp();
        this.parser
                .accepts(OPTION_GENERATION,
                        "Generations <id> to check " + "all - All generation " + "last  - Last generation (default) "
                                + "succeded - Only succeded ones " + " failed - Only failed ones")
                .availableUnless(optionHelp, optionCommit).withRequiredArg().describedAs("id");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp, optionCommit);
        this.parser.accepts(OPTION_PROFILE, "Remove the First Class Object profile. Used with Remove")
                .availableIf(optionRemove);
        this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
                .availableUnless(optionHelp, optionCommit).withOptionalArg().ofType(FirstClassObjectType.class)
                .describedAs("vm|ivd|vapp");

        this.parser.accepts(OPTION_OLDER_THAN, "Filter by creation time means older than dd:hh:mm ")
                .availableIf(OPTION_LIST).withRequiredArg().describedAs("dd:hh:mm")
                .withValuesConvertedBy(datePattern("dd:hh:mm"));
        this.parser.accepts(OPTION_NEWER_THAN, "Filter by creation time means newer than dd:hh:mm ")
                .availableIf(OPTION_LIST).withRequiredArg().describedAs("dd:hh:mm")
                .withValuesConvertedBy(datePattern("dd:hh:mm"));
        this.parser.accepts(OPTION_QUIET, "No confirmation asked.").availableUnless(optionHelp, optionList,
                optionCommit);
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);

    }

    @Override
    public String getCommandName() {
        return ARCHIVE;
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
        comp.put("A1", stringsCompleter(ARCHIVE));
        comp.put("A11", stringsCompleter(OPTION_LIST, OPTION_OLDER_THAN));
        comp.put("A21", stringsCompleter(OPTION_LIST, OPTION_NEWER_THAN));
        comp.put("A12", stringsCompleter(OPTION_STATUS, OPTION_ALL, OPTION_GENERATION));
        comp.put("A13", stringsCompleter(OPTION_CHECK, OPTION_DRYRUN, OPTION_ALL, OPTION_GENERATION));
        comp.put("A14", stringsCompleter(OPTION_REMOVE, OPTION_DRYRUN, OPTION_ALL, OPTION_GENERATION, OPTION_PROFILE,
                OPTION_QUIET));
        comp.put("A15", stringsCompleter(OPTION_COMMIT));
        comp.put("A16", stringsCompleter(OPTION_SHOW, OPTION_GENERATION));
        comp.put("A99", stringsCompleter(OPTION_HELP));
        return "|A1 A11*|A1 A12*|A1 A13*|A1 A14*|A1 A15?|A1 A16*|A1 A21*|A1 A99?";
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
        return "archive -status -all\n\tShow the status of all archive\n\n"
                + "archive -status vm:testVM vm:vm-2313 vm:f9ad3050-d5a6-d107-d0d8-d305c4bc2330 -details\n\tShow the archive status with details of 3 different Vm.  1st by name. 2nd by Moref. 3rd by UUID\n\n"
                + "archive -check -all\n\tValidate any archived object\n\n"
                + "archive -remove vm:testVM -generation 2,4\n\tRemove TestVM generation 2 and 4 from the archive\n\n"
                + "archive -remove vm:testVM -profile\n\tRemove TestVM Profile from the archive\n\n";
    }

    @Override
    public String helpSummary() {
        return ArchiveCommandInteractive.COMMAND_DESCRIPTION;
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
        } else if (optionSet.has(OPTION_LIST)) {
            getOptions().setList(true);
            if (optionSet.nonOptionArguments().isEmpty()) {
                getOptions().setAnyFcoOfType((FirstClassObjectFilterType.any | FirstClassObjectFilterType.all));
            }
        } else if (optionSet.has(OPTION_CHECK)) {
            getOptions().setCheck(true);
        } else if (optionSet.has(OPTION_SHOW)) {
            getOptions().setPrettyJason(true);
            getOptions().setShow(ArchiveObjects.parse(optionSet.valueOf(OPTION_SHOW)));
        } else if (optionSet.has(OPTION_REMOVE)) {
            getOptions().setRemove(optionSet.has(OPTION_REMOVE));
        } else {
            throw new ParsingException("No Action specified");
        }
        if (optionSet.has(OPTION_QUIET)) {
            getOptions().setQuiet(true);
        }
        if (optionSet.has(OPTION_ALL)) {
            getOptions().setAnyFcoOfType(FirstClassObjectFilterType.parse(optionSet.valueOf(OPTION_ALL),
                    FirstClassObjectFilterType.any | FirstClassObjectFilterType.all));
        }
        getOptions().setStatus(optionSet.has(OPTION_STATUS));

        String checkGenerationIdStr = null;
        if (optionSet.has(OPTION_GENERATION)) {
            checkGenerationIdStr = optionSet.valueOf(OPTION_GENERATION).toString();
            switch (checkGenerationIdStr.toLowerCase(Utility.LOCALE)) {
            case "all":
                getOptions().getGenerationId().add(ALL_GENERATIONS);
                break;
            case "last":
                getOptions().getGenerationId().add(LAST_GENERATION);
                break;
            case "succeded":
                getOptions().getGenerationId().add(SUCCEDED_GENERATIONS);
                break;
            case "failed":
                getOptions().getGenerationId().add(FAILED_GENERATIONS);
                break;
            default:
                final String[] gens = checkGenerationIdStr.split(",");
                if (gens.length > 0) {
                    for (final String idStr : gens) {
                        if (StringUtils.isNotEmpty(idStr)) {
                            if (StringUtils.isNumeric(idStr)) {
                                getOptions().getGenerationId().add(Integer.parseInt(idStr));
                            } else {
                                throw new ParsingException(OPTION_GENERATION, checkGenerationIdStr);
                            }
                        }
                    }
                } else {
                    throw new ParsingException(OPTION_GENERATION, checkGenerationIdStr);
                }
            }
        }
        if (optionSet.has(OPTION_DRYRUN)) {
            getOptions().setDryRun(true);
        }
        getOptions().setProfile(optionSet.has(OPTION_PROFILE));
        if (optionSet.has(OPTION_OLDER_THAN)) {
            final Date date = (Date) optionSet.valueOf(OPTION_OLDER_THAN);
            getOptions().setDateTimeFilter(date.getTime());
        }
        if (optionSet.has(OPTION_NEWER_THAN)) {
            final Date date = (Date) optionSet.valueOf(OPTION_NEWER_THAN);
            getOptions().setDateTimeFilter(-date.getTime());
        }
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }

}
