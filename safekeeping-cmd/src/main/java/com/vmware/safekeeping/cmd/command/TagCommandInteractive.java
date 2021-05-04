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
import com.vmware.safekeeping.core.ext.command.AbstractTagCommand;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionAttachTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDetachTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTagCategory;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTagList;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.FcoTag;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.enums.TagCardinalityOptions;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class TagCommandInteractive extends AbstractTagCommand implements ICommandInteractive {

    private static final String COMMAND_DESCRIPTION = "Manage tagging.";
    private static final String OPTION_VIM = "vim";

    private static final String OPTION_QUIET = "quiet";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_DRYRUN = "dryrun";

    private static final String OPTION_TAG_ATTACH = "attach";

    private static final String OPTION_CATEGORY = "category";

    private static final String OPTION_TAG_CARDINALITY = "cardinality";

    private static final String OPTION_TAG_DETACH = "detach";

    // private static final String OPTION_TAG_LIST_CATEGORY = "listcategory";

    private static final String OPTION_TAG_LIST = "list";

    private static final String OPTION_REMOVE_TAG = "remove";

    private static final String OPTION_ALL = "all";

    private static final String OPTION_DESCRIPTION = "description";

    private static final String OPTION_TAG_ASSOCIABLE_TYPE = "entity";

    private static final String OPTION_CREATE_TAG = "create";

    private static final String OPTION_TAG_NAME = "tag";

    public static final String TAG = "tags";

    static String helpGlobalSummary() {
        final TagCommandInteractive info = new TagCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;
    private final List<String> targetList;

    private boolean help;

    public TagCommandInteractive() {
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
                if (this.list != null) {
                    if (this.list == TagListOptions.TAG) {
                        result = actionListInteractive(connetionManager);
                    } else {
                        result = actionListCategoryInteractive(connetionManager);
                    }
                } else if (this.attach) {
                    result = actionAttachInteractive(connetionManager);
                } else if (this.detach) {
                    result = actionDetachInteractive(connetionManager);
                } else if (this.create) {
                    if (StringUtils.isEmpty(this.tagName)) {
                        result = actionCreateTagCategoryInteractive(connetionManager);
                    } else {
                        result = actionCreateTagInteractive(connetionManager);
                    }
                } else if (this.remove) {
                    if (StringUtils.isNotEmpty(this.assetCategory)) {
                        result = actionRemoveTagCategoryInteractive(connetionManager);
                    } else {
                        result = actionRemoveTagInteractive(connetionManager);
                    }
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
        final List<CoreResultActionAttachTag> resultActions = actionAttach(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionAttachTag resultAction : resultActions) {

                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showWarning(this.logger, "Attached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    IoFunction.showWarning(this.logger, "Failed - Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showInfo(this.logger, "Attached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    IoFunction.showInfo(this.logger, "Skipped - Reason: %s", resultAction.getReason());

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
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk },
                    total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionCreateTagInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final ResultActionCreateTag resultAction = actionCreateTag(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(this.logger, resultAction.getReason());
            break;
        case SKIPPED:

            break;
        case SUCCESS:
            IoFunction.showInfo(this.logger, "Tag created Name:%s Category:%s %s", resultAction.getTagName(),
                    resultAction.getAssetCategory(), resultAction.getTagId());
            break;
        default:
            break;

        }
        return result;
    }

    private OperationStateList actionCreateTagCategoryInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final ResultActionCreateTagCategory resultAction = actionCreateTagCategory(connetionManager);

        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;

        case FAILED:
            IoFunction.showWarning(this.logger, resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showWarning(this.logger, resultAction.getReason());
            break;
        case SUCCESS:
            final String[] arrayString = resultAction.getCategoriesUrn()
                    .toArray(new String[resultAction.getCategoriesUrn().size()]);
            IoFunction.showInfo(this.logger, "Tag category created; Name:%s Cardinality:%s - %s ",
                    resultAction.getCategoryName(), resultAction.getCardinality(), StringUtils.join(arrayString, ","));
            break;
        default:
            break;
        }
        return result;
    }

    private OperationStateList actionDetachInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionDetachTag> resultActions = actionDetach(connetionManager);
        final OperationStateList result = new OperationStateList();
        if (!resultActions.isEmpty()) {
            opLoop: for (final CoreResultActionDetachTag resultAction : resultActions) {

                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    IoFunction.showWarning(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showWarning(this.logger, "Detached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    IoFunction.showWarning(this.logger, "Failed - Reason: %s", resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showInfo(this.logger, "%s", resultAction.getFcoToString());
                    for (final FcoTag tag : resultAction.getTags()) {
                        IoFunction.showInfo(this.logger, "Detached  Tag %s:%s.", tag.getCategory(), tag.getTag());
                    }
                    IoFunction.showInfo(this.logger, "Skipped - Reason: %s", resultAction.getReason());

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
            IoFunction.printTotal(new EntityType[] { EntityType.VirtualMachine, EntityType.ImprovedVirtualDisk },
                    total);
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    protected OperationStateList actionListInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionTagList> results = actionList(connetionManager);
        if (!results.isEmpty()) {
            opLoop: for (final CoreResultActionTagList resultAction : results) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;
                case FAILED:
                    break;
                case SKIPPED:
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "Name: %s\n\tCategory:%s\n\tid: %s\n\tDescription: %s",
                            resultAction.getTagName(), resultAction.getCategoryName(), resultAction.getTagId(),
                            resultAction.getDescription());

                    if (!resultAction.getFcoList().isEmpty()) {
                        IoFunction.showInfo(this.logger, "\tAttached objects: ");
                        IoFunction.showInfo(this.logger, "\t\t%s", resultAction.getFcoList().toString());

                    }

                    break;
                default:
                    break;
                }
            }
        } else {
            IoFunction.showInfo(this.logger, "No Valid target specified.");
        }
        return result;

    }

    protected OperationStateList actionListCategoryInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<CoreResultActionTagCategory> results = actionListCategory(connetionManager);
        if (!results.isEmpty()) {
            opLoop: for (final CoreResultActionTagCategory resultAction : results) {
                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    break;
                case SKIPPED:
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger,
                            "Name: %s\n\tTags per Object:%s\n\tid: %s\n\tDescription: %s\n\tAssociable Objects: %s\n\n",
                            resultAction.getCategoryName(),
                            ((resultAction.getCardinality() == TagCardinalityOptions.multiple) ? "Many" : "Single"),
                            resultAction.getId(), resultAction.getDescription(),
                            ((!resultAction.getAssociableTypes().isEmpty())
                                    ? StringUtils.join(resultAction.getAssociableTypes().toArray(), " ")
                                    : "Any Type"));
                    break;
                default:
                    break;
                }
            }
        } else {
            IoFunction.showInfo(this.logger, "No Valid target specified.");
        }
        return result;

    }

    private OperationStateList actionRemoveTagInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final List<ResultActionRemoveTag> resultActions = actionRemoveTag(connetionManager);
        if (!resultActions.isEmpty()) {
            opLoop: for (final ResultActionRemoveTag resultAction : resultActions) {

                switch (result.add(resultAction)) {
                case ABORTED:
                    IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
                    break opLoop;

                case FAILED:
                    IoFunction.showWarning(this.logger, "Failed to remove Tag  Name: %s %s", resultAction.getTagName(),
                            resultAction.getTagId());
                    IoFunction.showWarning(this.logger, resultAction.getReason());
                    break;
                case SKIPPED:
                    IoFunction.showWarning(this.logger, "Remove  Tag  Name: %s %s - skipped", resultAction.getTagName(),
                            resultAction.getTagId());
                    IoFunction.showWarning(this.logger, resultAction.getReason());
                    break;
                case SUCCESS:
                    IoFunction.showInfo(this.logger, "Tag  %s %s removed ", resultAction.getTagName(),
                            resultAction.getTagId());
                    break;
                default:
                    break;
                }
            }
        } else {
            result.add(OperationState.FAILED, IoFunction.showNoValidTargerMessage(this.logger));
        }
        return result;
    }

    private OperationStateList actionRemoveTagCategoryInteractive(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final OperationStateList result = new OperationStateList();
        final ResultActionRemoveTagCategory resultAction = actionRemoveTagCategory(connetionManager);
        switch (result.add(resultAction)) {
        case ABORTED:
            IoFunction.showWarning(this.logger, Vmbk.OPERATION_ABORTED_BY_USER);
            break;
        case FAILED:
            IoFunction.showWarning(this.logger, "Failed to remove Category  Name: %s ", resultAction.getCategoryName());
            IoFunction.showWarning(this.logger, resultAction.getReason());
            break;
        case SKIPPED:
            IoFunction.showWarning(this.logger, "Remove Category  Name: %s - skipped", resultAction.getCategoryName());
            IoFunction.showWarning(this.logger, resultAction.getReason());
            break;
        case SUCCESS:
            IoFunction.showInfo(this.logger, "Category  %s   removed ", resultAction.getCategoryName());
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
        optionHelp.isForHelp();
        final OptionSpecBuilder optionTagList = this.parser.accepts(OPTION_TAG_LIST, "List available tags.");
        optionTagList.withOptionalArg().describedAs("tag|category").ofType(TagListOptions.class)
                .defaultsTo(TagListOptions.TAG);

        final OptionSpecBuilder optionRemove = this.parser.accepts(OPTION_REMOVE_TAG,
                "Remove specified tag(s) or category.");

        final OptionSpecBuilder optionCreate = this.parser.accepts(OPTION_CREATE_TAG, "Create a new tag or category.");
        final OptionSpecBuilder optionAttachTag = this.parser.accepts(OPTION_TAG_ATTACH,
                "attach one or more tags to any specified entities.");
        final OptionSpecBuilder optionDetachTag = this.parser.accepts(OPTION_TAG_DETACH,
                "detach one or more tags to any specified entities.");

        this.parser.mainOptions(optionHelp, optionTagList, optionRemove, optionAttachTag, optionCreate,
                optionDetachTag);

        /*
         * Option name
         */
        final OptionSpecBuilder optionTagName = this.parser.accepts(OPTION_TAG_NAME, "Name associated to a new Tag")
                .availableIf(optionCreate);
        optionTagName.withRequiredArg().describedAs("tag");
        /*
         * Option category
         */
        final OptionSpecBuilder optionCategory = this.parser
                .accepts(OPTION_CATEGORY, "Category for a new tag. Mandatory option for " + OPTION_CREATE_TAG + " .")
                .availableIf(optionCreate, optionRemove);
        optionCategory.withRequiredArg().describedAs("category");
        /*
         * Option description
         */
        final OptionSpecBuilder optionDescription = this.parser
                .accepts(OPTION_DESCRIPTION, "Description for a new tag or category.").availableIf(optionCreate);
        optionDescription.withRequiredArg().describedAs("description");

        /*
         * Option cardinality
         */
        final OptionSpecBuilder optiontagCardinality = this.parser.accepts(OPTION_TAG_CARDINALITY,
                "Cardinailty of a new tag category.");
        optiontagCardinality.availableIf(optionCreate).withRequiredArg().ofType(TagCardinalityOptions.class)
                .describedAs("single|multiple");
        /*
         * Option OPTION_TAG_ASSOCIABLE_TYPE
         */
        final OptionSpecBuilder optiontagTagAssociableType = this.parser.accepts(OPTION_TAG_ASSOCIABLE_TYPE,
                "Entities where the new tag category can be applied."
                        + "(Cluster, Datacenter, Datastore, DatastoreCluster, DistributedPortGroup, DistributedSwitch, Folder, ResourcePool, VApp, VirtualPortGroup, VirtualMachine)");
        optiontagTagAssociableType.availableIf(optionCreate).withRequiredArg().ofType(EntityType.class)
                .withValuesSeparatedBy(",").describedAs("entity,..,entity");

        /*
         * Option OPTION_DRYRUN
         */
        this.parser.accepts(OPTION_DRYRUN, "Do nothing really.").availableUnless(optionHelp, optionTagList);
        /*
         * Option OPTION_ALL
         */
        this.parser.accepts(OPTION_ALL, "Operation to any Virtual Machines, Improved Virtual Disks, vApps repository.")
                .availableIf(optionAttachTag, optionDetachTag).withOptionalArg().ofType(FirstClassObjectType.class)
                .describedAs("vm|ivd|vapp");
        /*
         * Option OPTION_QUIET
         */
        this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableIf(optionAttachTag, optionDetachTag,
                optionRemove);

        return new AbstractMap.SimpleEntry<>(getCommandName(), this);
    }

    @Override
    public String getCommandName() {
        return TAG;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("T1", stringsCompleter(TAG));

        comp.put("T11", stringsCompleter(OPTION_DRYRUN, OPTION_ALL, OPTION_REMOVE_TAG, OPTION_VIM, OPTION_QUIET));
        comp.put("T14", stringsCompleter(OPTION_TAG_LIST, OPTION_VIM));

        comp.put("T15", stringsCompleter(OPTION_DRYRUN, OPTION_CREATE_TAG, OPTION_TAG_NAME, OPTION_DESCRIPTION,
                OPTION_CATEGORY, OPTION_VIM));
        comp.put("T16", stringsCompleter(OPTION_DRYRUN, OPTION_TAG_ATTACH));
        comp.put("T17", stringsCompleter(OPTION_DRYRUN, OPTION_TAG_DETACH, OPTION_QUIET));
        comp.put("T19", stringsCompleter(OPTION_HELP));
        return "|T1 T11*|T1 T14*|T1 T15*|T1 T16*|T1 T17*|T1 T19?";
    }

    @Override
    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public String helpEntities() {
        final String ret = "EnyityType	Entity Description		uuid	name	moref\n"
                + "vm		Virtual Machine			X	X	X\n" + "ivd		Improved Virtual Disks		X	X	 \n"
                + "vapp		Virtual Appliance		X		 \n";
        return ret;
    }

    @Override
    public String helpExample() {
        return getCommandName() + " -" + OPTION_TAG_LIST + " -vim 9a583042-cb9d-5673-cbab-56a02a91805d\n"
                + "\tShow any tag with asscociated entities available on vCenter 9a583042-cb9d-5673-cbab-56a02a91805d\n\n"
                + getCommandName() + " -" + OPTION_CREATE_TAG + " -" + OPTION_CATEGORY + " myCat -" + OPTION_DESCRIPTION
                + " \"my personal VMs\" -" + OPTION_TAG_CARDINALITY + " single -" + OPTION_TAG_ASSOCIABLE_TYPE
                + " VirtualMachine \n" + "\tCreate a new Category called myCat and make it assignable to any VM\n\n"
                + getCommandName() + " -" + OPTION_REMOVE_TAG + " category -" + OPTION_CATEGORY + " myOldCategory"
                + "\n\tDelete the category with all associated tags\n\n" + getCommandName() + " -" + OPTION_TAG_ATTACH
                + " tag:myTag vm:myVM vm:myserver" + "\n\tAttach tag 'MyTag' to VM 'myVm' and 'myServer' \n\n";
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
        if (options.has(OPTION_TAG_LIST)) {
            this.list = (TagListOptions) options.valueOf(OPTION_TAG_LIST);
        }

        if (options.has(OPTION_CREATE_TAG)) {
            this.create = true;
        }
        if (options.has(OPTION_REMOVE_TAG)) {
            this.remove = true;
        }
        if (options.has(OPTION_TAG_ATTACH)) {
            this.attach = true;
        }
        if (options.has(OPTION_TAG_DETACH)) {
            this.detach = true;
        }

        if (options.has(OPTION_TAG_ASSOCIABLE_TYPE)) {
            this.associableTypes = (List<EntityType>) options.valuesOf(OPTION_TAG_ASSOCIABLE_TYPE);
        }

        if (options.has(OPTION_TAG_CARDINALITY)) {
            this.cardinality = (TagCardinalityOptions) options.valueOf(OPTION_TAG_CARDINALITY);
        }
        if (options.has(OPTION_TAG_NAME)) {
            this.tagName = options.valueOf(OPTION_TAG_NAME).toString();
        }
        if (options.has(OPTION_ALL)) {
            getOptions().setAnyFcoOfType(FirstClassObjectFilterType.parse(options.valueOf(OPTION_ALL),
                    FirstClassObjectFilterType.tagElement | FirstClassObjectFilterType.all));
        }
        if (options.has(OPTION_DRYRUN)) {
            getOptions().setDryRun(true);
        }
        if (options.has(OPTION_DESCRIPTION)) {
            this.description = options.valueOf(OPTION_DESCRIPTION).toString();

        }
        if (options.has(OPTION_CATEGORY)) {
            this.assetCategory = options.valueOf(OPTION_CATEGORY).toString();
        }

        if (options.has(OPTION_VIM)) {
            getOptions().setVim(options.valueOf(OPTION_VIM).toString());
        }
        if (options.has(OPTION_QUIET)) {
            getOptions().setQuiet(true);
        }

    }

    public void setHelp(final boolean help) {
        this.help = help;
    }
}
