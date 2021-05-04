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
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.PrettyBoolean.BooleanType;
import com.vmware.safekeeping.common.PrettyBoolean.PrettyBooleanValues;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateIvd;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.StatisticResult;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.ext.command.AbstractIvdCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionAttachIvd;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionAttachTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDetachIvd;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDisableRelocation;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionExpandIvd;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdKeepAflterDeleteVm;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdList;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdPromote;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdReconcile;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdTagList;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionRename;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.FcoTag;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class IvdCommandInteractive extends AbstractIvdCommand implements ICommandInteractive {
    private static final String COMMAND_DESCRIPTION = "Manage Improved Virtual Disks Virtual Disk";
    private static final String OPTION_VIM = "vim";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_LIST = "list";

    private static final String OPTION_PROMOTE = "promote";

    private static final String OPTION_REMOVE = "remove";

    private static final String OPTION_ATTACH_IVD = "attach";

    private static final String OPTION_DISK_DEVICE = "device";

    private static final String OPTION_DETACH_IVD = "detach";
    private static final String OPTION_RECONCILE = "reconcile";
    private static final String OPTION_ENABLE_CHANGED_BLOCK_TRACKING = "cbt";
    private static final String OPTION_KEEP_AFTER_DELETE_VM = "keepafterdeletevm";
    private static final String OPTION_DISABLE_RELOCATION = "disablerelocation";
    private static final String OPTION_CREATE_IVD = "create";
    private static final String OPTION_SIZE = "size";
    private static final String OPTION_DISK_TYPE = "type";
    private static final String OPTION_NAME = "name";

    private static final String OPTION_EXTEND = "extend";
    private static final String OPTION_PROFILE = "sbpmprofile";
    private static final String OPTION_RENAME = "rename";
    private static final String OPTION_ALL = "all";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_TAG = "tag";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_DETAIL = "detail";
    private static final String OPTION_DATASTORE = "datastore";

    public static final String IVD = "ivd";

    @Override
    public final void initialize() {
        help = false;
        this.options = new CoreBasicCommandOptions();
        this.reconcile = false;
        this.datastoreName = null;
        this.list = false;
        this.promote = null;
        this.attach = false;
        this.detach = false;
        this.remove = false;
        this.cbt = null;
        this.keepAfterDeleteVm = null;
        this.disableRelocation = null;
        this.create = false;
        this.tag = null;
        this.extend = false;
        this.rename = false;

        this.diskType = FileBackingInfoProvisioningType.THIN;
        this.size = null;
        this.name = null;
        this.detail = false;
        this.device = null;
        this.sbpmProfileNames = null;
        getOptions().setAnyFcoOfType(FirstClassObjectFilterType.ivd);
    }

    static String helpGlobalSummary() {
        final IvdCommandInteractive info = new IvdCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private boolean help;

    private final List<String> targetList;

    public IvdCommandInteractive() {
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
                if (this.reconcile) {
                    result = actionReconcileInteractive(connetionManager);
                } else if (this.list) {
                    result = actionListInteractive(connetionManager);
                } else if (this.cbt != null) {
                    result = actionCbtInteractive(connetionManager);
                } else if (this.keepAfterDeleteVm != null) {
                    result = actionKeepAfterDeleteVmInteractive(connetionManager);
                } else if (this.disableRelocation != null) {
                    result = actionDisableRelocationInteractive(connetionManager);
                } else if (this.remove) {
                    result = actionRemoveInteractive(connetionManager);
                } else if (this.attach) {
                    result = actionAttachInteractive(connetionManager);
                } else if (this.detach) {
                    result = actionDetachInteractive(connetionManager);
                } else if (this.promote != null) {
                    result = actionPromoteInteractive(connetionManager);
                } else if (this.create) {
                    result = actionCreateInteractive(connetionManager);
                } else if (this.tag != null) {
                    switch (this.tag) {
                    case ATTACH:
                        result = actionAttachTagInteractive(connetionManager);
                        break;
                    case DETACH:
                        result = actionDetachTagInteractive(connetionManager);
                        break;
                    case LIST:
                        result = actionListTagsInteractive(connetionManager);
                        break;
                    default:
                        break;
                    }
                } else if (this.extend) {
                    result = actionExpandInteractive(connetionManager);
                } else if (this.rename) {
                    result = actionRenameInteractive(connetionManager);
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

    private OperationStateList actionAttachInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionAttachIvd resultAction = actionAttach(connetionManager);

        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            if (resultAction.getVirtualMachine() != null) {
                IoFunction.showInfo(this.logger, " %s", resultAction.getVirtualMachine().toString());
            }
            IoFunction.showWarning(this.logger, "Attach failed - Reason: %s", resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "Skipped - Reason: %s", resultAction.getReason());
            break;

        case SUCCESS:
            IoFunction.showInfo(this.logger, "\tAttach %s  %s to succeed", resultAction.getFcoToString(),
                    resultAction.getVirtualMachine().toString());
            break;
        default:
            break;
        }

        return result;
    }

    private OperationStateList actionAttachTagInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionAttachTag> resultActions = actionAttachTag(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            for (final CoreResultActionAttachTag resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showWarning(this.logger, "Attached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    IoFunction.showInfo(this.logger, "Failed - Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
                    break;

                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showInfo(this.logger, "Attached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

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
                    IoFunction.showWarning(this.logger, "%s -failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skipped - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;

                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tCBT before:%s  now:%s  success.",
                            resultAction.getPreviousFlagState() ? "enable" : "disable",
                            resultAction.hasFlagState() ? "enable" : "disable");
                    break;
                default:
                    break;
                }

            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionCreateInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionCreateIvd resultAction = actionCreate(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            IoFunction.showWarning(this.logger,
                    "Failed to create Improved Virtual Disk: %s Datastore:%s size:%dM Type:%s created",
                    resultAction.getFcoToString(), resultAction.getDatastoreInfo().getName(), resultAction.getSize(),
                    resultAction.getBackingType().toString());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
            break;

        case SUCCESS:
            IoFunction.showInfo(this.logger, "New Improved Virtual Disk: %s Datastore:%s size:%dM Type:%s created",
                    resultAction.getFcoToString(), resultAction.getDatastoreInfo().getName(), resultAction.getSize(),
                    resultAction.getBackingType().toString());
            break;
        default:
            break;
        }

        return result;
    }

    private OperationStateList actionDetachInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionDetachIvd resultAction = actionDetach(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            if (resultAction.getVirtualMachine() != null) {
                IoFunction.showInfo(this.logger, " %s", resultAction.getVirtualMachine().toString());
            }
            IoFunction.showWarning(this.logger, "\tDetach failed - Reason: %s", resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
            break;

        case SUCCESS:
            IoFunction.showInfo(this.logger, " %s", resultAction.getVirtualMachine().toString());
            IoFunction.showInfo(this.logger, "\tDetach %s   Datastore:%s from %s succeed",
                    resultAction.getFcoToString(), resultAction.getDatastore().getName(),
                    resultAction.getVirtualMachine().toString());

            break;
        default:
            break;
        }
        return result;
    }

    private OperationStateList actionDetachTagInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionAttachTag> resultActions = actionDetachTag(connetionManager);
        if (!resultActions.isEmpty()) {
            for (final CoreResultActionAttachTag resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showWarning(this.logger, "Detached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    IoFunction.showInfo(this.logger, "Failed - Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
                    break;

                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showInfo(this.logger, "Detached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionDisableRelocationInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionDisableRelocation> resultActions = actionDisableRelocation(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDisableRelocation resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s -failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skipped - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;

                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tDisableRelocation Flag before:%s  now:%s  success.",
                            resultAction.getPreviousFlagState() ? "enable" : "disable",
                            resultAction.hasFlagState() ? "enable" : "disable");
                    break;
                default:
                    break;
                }

            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));

        }
        return result;
    }

    private OperationStateList actionExpandInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionExpandIvd resultAction = actionExpand(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            IoFunction.showWarning(this.logger, "Ivd %s cannot be extended to %s  - Reason: %s",
                    resultAction.getFcoToString(), PrettyNumber.toString(this.size, MetricPrefix.GIGA),
                    resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
            break;

        case SUCCESS:
            IoFunction.showInfo(this.logger, "Ivd %s extended to  %s.", resultAction.getFcoToString(),
                    PrettyNumber.toString(resultAction.getSize(), MetricPrefix.GIGA));
            break;
        default:
            break;
        }

        return result;
    }

    private OperationStateList actionKeepAfterDeleteVmInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionIvdKeepAflterDeleteVm> resultActions = actionKeepAfterDeleteVm(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionIvdKeepAflterDeleteVm resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s - failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:

                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    IoFunction.showInfo(this.logger, "\tKeepAfterDeleteVm Flag before:%s  now:%s  %s.",
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
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;

    }

    private OperationStateList actionListInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionIvdList> resultActions = actionList(connetionManager);
        if (!resultActions.isEmpty()) {
            IoFunction.showInfo(this.logger, "%s\t%9s   %9s\t%s    %s%24s\t%-85s",
                    ImprovedVirtualDisk.sHeaderToString(), "size", "cbt", "snapshot", "attached", "datastore", "path");
            opLoop: for (final CoreResultActionIvdList resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s - failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "%s\t%10dMB   %6s\t%8s    %8s%24s\t%-85s",
                            resultAction.getFcoToString(), resultAction.getCapacityInMB(), resultAction.getCbt(),
                            PrettyBoolean.toString(resultAction.hasSnapshot(), BooleanType.yesNo),
                            PrettyBoolean.toString(resultAction.isAttached(), BooleanType.yesNo),
                            resultAction.getDatastoreInfo().getName(), resultAction.getFilePath());
                    if (resultAction.isAttached() && this.detail) {
                        final StringBuilder vmDiskStr = new StringBuilder();
                        vmDiskStr.append(resultAction.getAttachedVm().toString());
                        vmDiskStr.append("(ctrl key:");
                        vmDiskStr.append(resultAction.getDiskKey());
                        vmDiskStr.append(')');
                        vmDiskStr.append('\n');
                        IoFunction.showInfo(this.logger, "\t\tAssociated Virtual Machine\n\t\t%s",
                                vmDiskStr.toString());
                    }
                    break;
                default:
                    break;
                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    protected OperationStateList actionListTagsInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionIvdTagList> resultActions = actionListTags(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionIvdTagList resultAction : resultActions) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "%s failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s - skip - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.printf("%s ", resultAction.getFcoToString());
                    IoFunction.println();
                    IoFunction.printf("\t%-30s%-40s", "category", "name");
                    IoFunction.println();
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.printf("\t%-30s%-40s", tag.getCategory(), tag.getTag());
                        IoFunction.println();
                    }
                    IoFunction.println();
                    break;
                default:
                    break;

                }
            }
            final StatisticResult total = ICoreResultAction.getResultStatistic(resultActions);
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionPromoteInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final CoreResultActionIvdPromote resultAction = actionPromote(connetionManager);
        final OperationStateList result = new OperationStateList();
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            IoFunction.showWarning(this.logger, "Failed %s - Reason: %s", resultAction.getFcoToString(),
                    resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "skipped %s - Reason: %s", resultAction.getFcoToString(),
                    resultAction.getReason());
            break;

        case SUCCESS:

            IoFunction.showInfo(this.logger, VirtualMachineManager.sHeaderToString());
            IoFunction.showInfo(this.logger, resultAction.getFcoToString());
            IoFunction.println(" vmdk:");

            for (final Integer key : resultAction.getImprovedVirtualDiskList().keySet()) {
                IoFunction.showInfo(this.logger, "\t%d\t%s ", key,
                        resultAction.getImprovedVirtualDiskList().get(key).toString());
            }
            break;
        default:
            break;
        }

        return result;

    }

    private OperationStateList actionReconcileInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final CoreResultActionIvdReconcile resultAction = actionReconcile(connetionManager);
        final OperationStateList result = new OperationStateList();
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            if (resultAction.getDatastore() != null) {
                IoFunction.showWarning(this.logger, "Reconcile datastore %s failed - Reason: %s",
                        resultAction.getDatastore().toString(), resultAction.getReason());
            } else {
                IoFunction.showWarning(this.logger, "Reconcile failed - Reason: %s", resultAction.getReason());
            }

            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
            break;

        case SUCCESS:
            IoFunction.showInfo(this.logger, "Datastore %s reconciled  ", resultAction.getDatastore().toString());
            break;
        default:
            break;
        }

        return result;
    }

    private OperationStateList actionRemoveInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionDestroy> resultActions = actionRemove(connetionManager);

        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDestroy resultAction : resultActions) {
                switch (resultAction.getState()) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "%s failed - Reason: %s", resultAction.getFcoToString(),
                            resultAction.getReason());
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
            IoFunction.printTotal(EntityType.ImprovedVirtualDisk, total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionRenameInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final CoreResultActionRename resultAction = actionRename(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            IoFunction.showWarning(this.logger, "%s renaming to %s failed - Reason: %s", resultAction.getFcoToString(),
                    resultAction.getName(), resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showInfo(this.logger, "skipped - Reason: %s", resultAction.getReason());
            break;

        case SUCCESS:
            IoFunction.showInfo(this.logger, " %s renamed to %s", resultAction.getFcoToString(),
                    resultAction.getName());
            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);

        final OptionSpecBuilder optionPromote = this.parser.accepts(OPTION_PROMOTE,
                "Promote vm disk to Improved Virtual Disks.");
        optionPromote.withOptionalArg().describedAs("diskId|all");
        final OptionSpecBuilder optionAttachIvd = this.parser.accepts(OPTION_ATTACH_IVD,
                "Attach Improved Virtual Disks  to Virtual Machine.");

        final OptionSpecBuilder optionList = this.parser.accepts(OPTION_LIST, "List Improved Virtual Disks.");

        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE, "Remove the Improved Virtual Disks.");

        final OptionSpecBuilder optionReconcile = this.parser.accepts(OPTION_RECONCILE,
                "Reconcile Improved Virtual Disks database");
        final OptionSpecBuilder optionRename = this.parser.accepts(OPTION_RENAME, "Rename the Improved Virtual Disk.");
        final OptionSpecBuilder optionCbt = this.parser.accepts(OPTION_ENABLE_CHANGED_BLOCK_TRACKING,
                "Enable disable Change Block Tracking");
        final OptionSpecBuilder optionDetachIvd = this.parser.accepts(OPTION_DETACH_IVD,
                "Detach Improved Virtual Disks from a Virtual Machine.");
        final OptionSpecBuilder optionKeepAfterDelete = this.parser.accepts(OPTION_KEEP_AFTER_DELETE_VM,
                "Enable/disable keepAfterDeleteVm property");
        final OptionSpecBuilder optionTag = this.parser.accepts(OPTION_TAG,
                "Attach, Detach and List Improved Virtual Disk Tags");
        optionTag.withRequiredArg().describedAs("attach|detach|list").ofType(TagOperationOptions.class);

        final OptionSpecBuilder optionExtend = this.parser.accepts(OPTION_EXTEND,
                "Extend the disk size. Use " + OPTION_SIZE + " for disk size");

        final OptionSpecBuilder optionDisableRelocation = this.parser.accepts(OPTION_DISABLE_RELOCATION,
                "Enable/disable keepAfterDeleteVm property");
        final OptionSpecBuilder optionCreateIvd = this.parser.accepts(OPTION_CREATE_IVD,
                "Create a new Improved Virtual Disks.");
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");

        this.parser.mainOptions(optionReconcile, optionList, optionHelp, optionPromote, optionAttachIvd,
                optionDetachIvd, optionRemove, optionCbt, optionDisableRelocation, optionKeepAfterDelete,
                optionCreateIvd, optionRename, optionTag, optionExtend);

        optionHelp.forHelp();
        optionKeepAfterDelete.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("yes|no");
        optionDisableRelocation.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("yes|no");
        optionCbt.withRequiredArg().ofType(PrettyBooleanValues.class).describedAs("on|off");
        final OptionSpecBuilder optionSize = this.parser
                .accepts(OPTION_SIZE, "IVD size. Used by " + OPTION_CREATE_IVD + " and " + OPTION_EXTEND)
                .requiredIf(OPTION_CREATE_IVD, OPTION_EXTEND);
        optionSize.withRequiredArg().describedAs("size(k|M|G|T|P)")
                .withValuesConvertedBy(joptsimple.util.RegexMatcher.regex(PrettyNumber.pattern));

        final OptionSpecBuilder optionDatastore = this.parser.accepts(OPTION_DATASTORE, "Datastore name.")
                .requiredIf(OPTION_CREATE_IVD, OPTION_RECONCILE);
        optionDatastore.withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionName = this.parser.accepts(OPTION_NAME,
                "Used by " + OPTION_CREATE_IVD + ", " + OPTION_PROMOTE + " and " + OPTION_RENAME);
        optionName.availableIf(OPTION_CREATE_IVD, OPTION_RENAME, OPTION_PROMOTE).withRequiredArg().describedAs("name");
        final OptionSpecBuilder optionDiskType = this.parser.accepts(OPTION_DISK_TYPE,
                "Backend type  EAGER_ZEROED_THICK(t|thick) LAZY_ZEROED_THICK(z|zero), THIN (h|thin). Used by "
                        + OPTION_CREATE_IVD)
                .availableIf(optionCreateIvd);
        optionDiskType.withRequiredArg()
                .withValuesConvertedBy(joptsimple.util.RegexMatcher
                        .regex("^(thick|t|zero|z|thin|h|EAGER_ZEROED_THICK|LAZY_ZEROED_THICK|THIN)"))
                .describedAs("t|z|h");// .ofType(DiskFileProvisioningType.class).
        final OptionSpecBuilder optionVim = this.parser
                .accepts(OPTION_VIM, "Target a specific vim service  <vim> (uuid,url)").availableUnless(optionHelp);

        optionVim.withRequiredArg().describedAs("vcenter");

        this.parser.accepts(OPTION_DRYRUN, "Do not do anything.").availableUnless(optionList, optionHelp);

        final OptionSpecBuilder optionDiskDevice = this.parser
                .accepts(OPTION_DISK_DEVICE, "Specify the disk controller. Optional for " + OPTION_ATTACH_IVD
                        + "ctrlkey Device Key of the controller the disk will connect to." + "unit: unit disk number.")
                .availableIf(OPTION_ATTACH_IVD);

        optionDiskDevice.withOptionalArg().withValuesConvertedBy(joptsimple.util.RegexMatcher.regex("^[0-9:]+$"))
                .describedAs("ctrlkey:unit|ctrlkey|:unit");
        this.parser.accepts(OPTION_PROFILE, "Set Improved Virtual Disks Storage Profile.").availableIf(optionCreateIvd);
        this.parser.accepts(OPTION_ALL, "Operation apply to any Improved Virtual Disks.").availableUnless(optionHelp,
                optionReconcile, optionRename, optionCreateIvd);
        this.parser.accepts(OPTION_DETAIL, "Show details. Used with " + OPTION_LIST + "").availableIf(optionList);
        this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableIf(optionDetachIvd,
                optionDisableRelocation, optionKeepAfterDelete, optionRemove, optionReconcile, optionRename);
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return IVD;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("I1", stringsCompleter(IVD));

        comp.put("I11", stringsCompleter(OPTION_DRYRUN, OPTION_REMOVE, OPTION_VIM));
        comp.put("I12", stringsCompleter(OPTION_LIST, OPTION_VIM, OPTION_DETAIL));
        comp.put("I13", stringsCompleter(OPTION_RECONCILE, OPTION_VIM));
        comp.put("I14", stringsCompleter(OPTION_DRYRUN, OPTION_DETACH_IVD, OPTION_VIM, OPTION_QUIET));
        comp.put("I15", stringsCompleter(OPTION_DRYRUN, OPTION_ATTACH_IVD, OPTION_DISK_DEVICE, OPTION_VIM));
        comp.put("I16", stringsCompleter(OPTION_DRYRUN, OPTION_ENABLE_CHANGED_BLOCK_TRACKING, OPTION_VIM));
        comp.put("I17", stringsCompleter(OPTION_DRYRUN, OPTION_DISABLE_RELOCATION, OPTION_VIM));
        comp.put("I18", stringsCompleter(OPTION_DRYRUN, OPTION_KEEP_AFTER_DELETE_VM, OPTION_VIM));
        comp.put("I19", stringsCompleter(OPTION_DRYRUN, OPTION_PROMOTE, OPTION_VIM, OPTION_QUIET));
        comp.put("I20", stringsCompleter(OPTION_DRYRUN, OPTION_CREATE_IVD, OPTION_SIZE, OPTION_DATASTORE, OPTION_NAME,
                OPTION_VIM, OPTION_QUIET));
        comp.put("I21", stringsCompleter(OPTION_DRYRUN, OPTION_TAG, OPTION_VIM, OPTION_QUIET));
        comp.put("I24", stringsCompleter(OPTION_DRYRUN, OPTION_EXTEND, OPTION_VIM, OPTION_QUIET));
        comp.put("I25", stringsCompleter(OPTION_DRYRUN, OPTION_RENAME, OPTION_NAME, OPTION_VIM, OPTION_QUIET));

        comp.put("I99", stringsCompleter(OPTION_HELP));
        return "|I1 I11*|I1 I12*|I1 I13*|I1 I14*|I1 I15*|I1 I16*|I1 I17*|I1 I18*|I1 I19*|I1 I20*|I1 I21*|I1 I24*|I1 I25*|I1 I99?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        return "EnyityType  Entity Description      uuid    name    moref\n"
                + "ivd         Improved Virtual Disk   X       X       X\n" + "tag         vCenter Tag             X\n";
    }

    @Override

    public String helpExample() {
        return "ivd " + "-" + OPTION_LIST + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
                + "\tShow any Improved Virtual Disks available on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
                + "ivd " + "-" + OPTION_REMOVE + " ivd:testDisk\n" + "\tDelete the Improved Virtual Disks testDisk\n\n"
                + "ivd " + "-" + OPTION_ATTACH_IVD + " 1000:2 ivd:testDisk vm:Windows2016Test\n"
                + "\tAttach the Improved Virtual Disks testDisk to VM Windows2016Test as 2nd disk controller id 1000\n\n"
                + "ivd " + "-" + OPTION_CREATE_IVD + " " + "-" + OPTION_NAME + " myIvdDisk " + "-" + OPTION_SIZE
                + " 20G " + "-" + OPTION_DATASTORE + " vsanDatastore " + "-" + OPTION_DISK_TYPE + " zero \n"
                + "\tCreate an Improved Virtual Disks name myIvdDisk type EAGER_ZEROED_THICK of size 20GB on datastore vsanDatastore\n\n";
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
        if (options.has(OPTION_HELP)) {
            setHelp(true);
        } else if (options.has(OPTION_RECONCILE)) {
            this.reconcile = true;
        } else if (options.has(OPTION_LIST)) {

            this.list = true;
            if (options.nonOptionArguments().isEmpty()) {
                getOptions().setAnyFcoOfType(FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all);
            }
        } else if (options.has(OPTION_PROMOTE)) {
            this.promote = options.valueOf(OPTION_PROMOTE).toString();
            if ("all".equalsIgnoreCase(this.promote)) {
                this.promote = StringUtils.EMPTY;
            }
        } else if (options.has(OPTION_ATTACH_IVD)) {

            this.attach = true;
        } else if (options.has(OPTION_DETACH_IVD)) {
            this.detach = true;
        } else if (options.has(OPTION_REMOVE)) {
            this.remove = true;
        } else if (options.has(OPTION_ENABLE_CHANGED_BLOCK_TRACKING)) {
            this.cbt = PrettyBoolean.parseBoolean(options.valueOf(OPTION_ENABLE_CHANGED_BLOCK_TRACKING));
        } else if (options.has(OPTION_DISABLE_RELOCATION)) {
            this.disableRelocation = PrettyBoolean.parseBoolean(options.valueOf(OPTION_DISABLE_RELOCATION));
        } else if (options.has(OPTION_KEEP_AFTER_DELETE_VM)) {
            this.keepAfterDeleteVm = PrettyBoolean.parseBoolean(options.valueOf(OPTION_KEEP_AFTER_DELETE_VM));
        } else if (options.has(OPTION_CREATE_IVD)) {
            this.create = true;
        } else if (options.has(OPTION_TAG)) {
            this.tag = (TagOperationOptions) options.valueOf(OPTION_TAG);
            getOptions().setAnyFcoOfType(getOptions().getAnyFcoOfType() | FirstClassObjectFilterType.noTag);
            if (this.tag == TagOperationOptions.LIST) {
                if (options.nonOptionArguments().isEmpty()) {
                    getOptions().setAnyFcoOfType(FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all);
                }
            } else {
                getOptions().setAnyFcoOfType(getOptions().getAnyFcoOfType() | FirstClassObjectFilterType.noTag);
            }

        } else if (options.has(OPTION_EXTEND)) {
            this.extend = true;
        } else if (options.has(OPTION_RENAME)) {
            this.rename = true;
        } else {
            throw new ParsingException("No Action specified");
        }
        if (options.has(OPTION_PROFILE)) {
            sbpmProfileNames = options.valueOf(OPTION_PROFILE).toString();
        }

        if (options.has(OPTION_NAME)) {
            this.name = options.valueOf(OPTION_NAME).toString();
        }
        if (options.has(OPTION_DRYRUN)) {
            getOptions().setDryRun(true);
        }
        if (options.has(OPTION_QUIET)) {
            getOptions().setQuiet(true);
        }
        if (options.has(OPTION_VIM)) {
            getOptions().setVim(options.valueOf(OPTION_VIM).toString());
        }
        if (options.has(OPTION_ALL)) {
            getOptions().setAnyFcoOfType(FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
                    FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all));

        }

        if (options.has(OPTION_DATASTORE)) {
            this.datastoreName = options.valueOf(OPTION_DATASTORE).toString();
        }
        if (options.has(OPTION_DISK_DEVICE)) {
            this.device = options.valueOf(OPTION_DISK_DEVICE).toString();
        }
        if (options.has(OPTION_SIZE)) {
            this.size = PrettyNumber.toLong(options.valueOf(OPTION_SIZE));

        }
        if (options.has(OPTION_DISK_TYPE)) {
            this.diskType = FileBackingInfoProvisioningType.parse(options.valueOf(OPTION_DISK_TYPE));

        }
        if (options.has(OPTION_DETAIL)) {
            this.detail = true;

        }
    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
