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
package com.vmware.safekeeping.core.ext.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.CategoryModel.Cardinality;
import com.vmware.cis.tagging.CategoryTypes;
import com.vmware.cis.tagging.TagAssociationTypes.BatchResult;
import com.vmware.cis.tagging.TagModel;
import com.vmware.cis.tagging.TagTypes;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionAttachTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDetachTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTagCategory;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTagList;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.FcoTag;
import com.vmware.safekeeping.core.type.FcoTarget;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.TagCardinalityOptions;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public abstract class AbstractTagCommand extends AbstractCommandWithOptions {

    public static class ResultActionCreateTag extends CoreResultActionTag {
        /**
         *
         */
        private static final long serialVersionUID = 7515889582340570553L;
        private String assetCategory;

        public String getAssetCategory() {
            return this.assetCategory;
        }

        public void setAssetCategory(final String assetCategory) {
            this.assetCategory = assetCategory;
        }
    }

    public static class ResultActionCreateTagCategory extends CoreResultActionTagCategory {

        /**
         *
         */
        private static final long serialVersionUID = 4975919774112958216L;

    }

    public static class ResultActionRemoveTag extends CoreResultActionTag {

        /**
         *
         */
        private static final long serialVersionUID = -3124224370134624936L;

    }

    public static class ResultActionRemoveTagCategory extends CoreResultActionTagCategory {

        /**
         *
         */
        private static final long serialVersionUID = -4881595929507941137L;
    }

    public enum TagListOptions {
        TAG, CATEGORY
    }

    protected String assetCategory;
    protected List<EntityType> associableTypes;
    protected boolean attach;
    protected boolean create;
    protected String description;
    protected boolean detach;

    protected TagListOptions list;

    protected String tagName;
    protected boolean remove;

    protected TagCardinalityOptions cardinality;

    /**
     * Attach a tag from a FCO
     *
     * @param connetionManager
     * @return
     * @throws CoreResultActionException
     */
    protected List<CoreResultActionAttachTag> actionAttach(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionAttachTag> result = new ArrayList<>();

        final Map<String, FcoTarget> tagsTarget = getOptions().getTargetFcoList();
        if (tagsTarget.size() > 0) {
            final List<TagModel> tags = connetionManager.listTags(getOptions().getVim(), tagsTarget.keySet());
            if ((tags != null) && (!tags.isEmpty())) {
                final Map<String, FcoTag> tagIds = new HashMap<>();
                for (final TagModel t : tags) {
                    final FcoTag ltag = new FcoTag();
                    ltag.setTagId(t.getId());
                    ltag.setTag(t.getName());
                    ltag.setCategoryId(t.getCategoryId());
                    tagIds.put(t.getId(), ltag);
                }
                final List<IFirstClassObject> fcoList = getFcoTarget(connetionManager,
                        FirstClassObjectFilterType.any | FirstClassObjectFilterType.noTag);
                if ((fcoList != null) && ((fcoList.size()) > 0)) {
                    for (final IFirstClassObject fco : fcoList) {
                        try (final CoreResultActionAttachTag resultAction = new CoreResultActionAttachTag()) {
                            resultAction.start();
                            if (Vmbk.isAbortTriggered()) {
                                resultAction.aborted();
                                result.clear();
                                result.add(resultAction);
                                break;
                            } else {
                                resultAction.setFcoEntityInfo(fco.getFcoInfo());
                                resultAction.getTags().addAll(tagIds.values());
                                BatchResult batchResult = null;
                                if (fco instanceof VirtualMachineManager) {
                                    final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                                    final DynamicID vmDynamicId = new DynamicID(vmm.getMoref().getType(),
                                            vmm.getMoref().getValue());
                                    batchResult = vmm.getVimConnection().getVapiService().getTagAssociation()
                                            .attachMultipleTagsToObject(vmDynamicId, new ArrayList<>(tagIds.keySet()));
                                } else if (fco instanceof ImprovedVirtualDisk) {
                                    final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                                    final DynamicID vmDynamicId = new DynamicID("vmomi:fcd", ivd.getId().getId());
                                    batchResult = ivd.getVimConnection().getVapiService().getTagAssociation()
                                            .attachMultipleTagsToObject(vmDynamicId, new ArrayList<>(tagIds.keySet()));
                                } else if (fco instanceof VirtualAppManager) {
                                    final VirtualAppManager vmm = (VirtualAppManager) fco;
                                    final DynamicID vmDynamicId = new DynamicID(vmm.getMoref().getType(),
                                            vmm.getMoref().getValue());
                                    batchResult = fco.getVimConnection().getVapiService().getTagAssociation()
                                            .attachMultipleTagsToObject(vmDynamicId, new ArrayList<>(tagIds.keySet()));
                                }
                                if ((batchResult != null) && !batchResult.getSuccess()) {
                                    resultAction.setReason(StringUtils.join(batchResult.getErrorMessages(), ' '));
                                    resultAction.failure();
                                }

                                result.add(resultAction);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    protected ResultActionCreateTag actionCreateTag(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final ResultActionCreateTag resultAction = new ResultActionCreateTag()) {
            resultAction.start();
            resultAction.setTagName(this.tagName);
            resultAction.setDescription(this.description);
            resultAction.setAssetCategory(this.assetCategory);
            final TagTypes.CreateSpec spec = new TagTypes.CreateSpec();
            spec.setName(this.tagName);
            spec.setDescription(this.description);

            final HashSet<String> categoriesName = new HashSet<>();
            categoriesName.add(this.assetCategory);

            final List<CategoryModel> tagCategory = connetionManager.listTagCategories(getOptions().getVim(),
                    categoriesName);
            if (!tagCategory.isEmpty()) {
                resultAction.setReason("Category %s doesn't exist", categoriesName);
                resultAction.failure();
            } else {
                if (!getOptions().isDryRun()) {
                    spec.setCategoryId(tagCategory.get(0).getId());
                    try {
                        resultAction.setTagId(connetionManager.createTag(getOptions().getVim(), spec));
                    } catch (final com.vmware.vapi.std.errors.AlreadyExists e) {
                        resultAction.failure("Tag already exist");
                    } catch (final Exception e) {
                        resultAction.failure(e);
                    }

                }
            }
            return resultAction;
        }
    }

    protected ResultActionCreateTagCategory actionCreateTagCategory(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final ResultActionCreateTagCategory resultAction = new ResultActionCreateTagCategory()) {
            resultAction.start();
            resultAction.setCategoryName(this.assetCategory);
            resultAction.setDescription(this.description);
            resultAction.setCardinality(this.cardinality);
            final CategoryTypes.CreateSpec createSpec = new CategoryTypes.CreateSpec();
            createSpec.setName(this.assetCategory);
            createSpec.setDescription(this.description);
            createSpec.setCardinality(
                    (this.cardinality == TagCardinalityOptions.single) ? Cardinality.SINGLE : Cardinality.MULTIPLE);
            final HashSet<String> assocTypes = new HashSet<>();
            for (final EntityType entity : this.associableTypes) {
                assocTypes.add("urn:vim25:".concat(entity.toString()));
            }
            createSpec.setAssociableTypes(assocTypes);
            resultAction.setAssociableTypes(assocTypes);
            if (!getOptions().isDryRun()) {
                try {
                    resultAction.setCategoriesUrn(
                            connetionManager.createTagCategorySpec(getOptions().getVim(), createSpec));
                } catch (final com.vmware.vapi.std.errors.AlreadyExists e) {
                    resultAction.failure("Tag already exist");
                    Utility.logWarning(this.logger, e);
                }
            }
            return resultAction;
        }
    }

    /**
     * Detach a tag from a FCO
     *
     * @param connetionManager
     * @return
     */
    protected List<CoreResultActionDetachTag> actionDetach(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionDetachTag> result = new ArrayList<>();

        final Map<String, FcoTarget> tagsTarget = getOptions().getTargetFcoList();
        if (tagsTarget.size() > 0) {
            final List<TagModel> tags = connetionManager.listTags(getOptions().getVim(), tagsTarget.keySet());
            if ((tags != null) && (!tags.isEmpty())) {
                final Map<String, FcoTag> tagIds = new HashMap<>();
                for (final TagModel t : tags) {
                    final FcoTag ltag = new FcoTag();
                    ltag.setTagId(t.getId());
                    ltag.setTag(t.getName());
                    ltag.setCategoryId(t.getCategoryId());
                    tagIds.put(t.getId(), ltag);
                }
                final List<IFirstClassObject> fcoList = getFcoTarget(connetionManager,
                        FirstClassObjectFilterType.any | FirstClassObjectFilterType.noTag);
                if ((fcoList != null) && ((fcoList.size()) > 0)) {
                    for (final IFirstClassObject fco : fcoList) {
                        try (final CoreResultActionDetachTag resultAction = new CoreResultActionDetachTag()) {
                            resultAction.start();
                            if (Vmbk.isAbortTriggered()) {
                                resultAction.aborted("Aborted on user request");

                                result.clear();
                                result.add(resultAction);
                                break;
                            } else {
                                resultAction.setFcoEntityInfo(fco.getFcoInfo());
                                resultAction.getTags().addAll(tagIds.values());
                                BatchResult batchResult = null;
                                try {
                                    if (fco instanceof VirtualMachineManager) {
                                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;

                                        final DynamicID vmDynamicId = new DynamicID(vmm.getMoref().getType(),
                                                vmm.getMoref().getValue());
                                        batchResult = vmm.getVimConnection().getVapiService().getTagAssociation()
                                                .detachMultipleTagsFromObject(vmDynamicId,
                                                        new ArrayList<>(tagIds.keySet()));
                                    } else if (fco instanceof ImprovedVirtualDisk) {
                                        final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;

                                        final DynamicID vmDynamicId = new DynamicID("vmomi:fcd", ivd.getId().getId());
                                        batchResult = ivd.getVimConnection().getVapiService().getTagAssociation()
                                                .detachMultipleTagsFromObject(vmDynamicId,
                                                        new ArrayList<>(tagIds.keySet()));
                                    } else if (fco instanceof VirtualAppManager) {
                                        final VirtualAppManager vmm = (VirtualAppManager) fco;
                                        final DynamicID vmDynamicId = new DynamicID(vmm.getMoref().getType(),
                                                vmm.getMoref().getValue());
                                        batchResult = fco.getVimConnection().getVapiService().getTagAssociation()
                                                .detachMultipleTagsFromObject(vmDynamicId,
                                                        new ArrayList<>(tagIds.keySet()));
                                    }
                                    if ((batchResult != null) && !batchResult.getSuccess()) {
                                        resultAction.failure(StringUtils.join(batchResult.getErrorMessages(), ' '));
                                    }
                                } catch (final Exception e) {
                                    resultAction.failure(e);
                                }
                                result.add(resultAction);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    protected List<CoreResultActionTagList> actionList(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionTagList> result = new ArrayList<>();
        final Map<String, FcoTarget> ivdTarget = getOptions().getTargetFcoList();
        final List<TagModel> tags = connetionManager.listTags(getOptions().getVim(), ivdTarget.keySet());
        for (final TagModel tag : tags) {
            try (final CoreResultActionTagList resultAction = new CoreResultActionTagList()) {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    final CategoryModel tagCategory = connetionManager.getTagCategory(getOptions().getVim(),
                            tag.getCategoryId());

                    resultAction.setTagName(tag.getName());
                    resultAction.setDescription(tag.getDescription());
                    resultAction.setTagId(tag.getId());
                    resultAction.setCategoryId(tag.getCategoryId());
                    resultAction.setCategoryName(tagCategory.getName());

                    final List<DynamicID> attachedObject = connetionManager
                            .listTagAttachedObjects(getOptions().getVim(), tag.getId());
                    if (attachedObject != null) {

                        for (final DynamicID id : attachedObject) {
                            IFirstClassObject fco = null;
                            if (StringUtils.equals(id.getType(), EntityType.VirtualMachine.toString())) {
                                try {
                                    fco = new VirtualMachineManager(
                                            connetionManager.getVimConnection(getOptions().getVim()),
                                            MorefUtil.newManagedObjectReference(EntityType.VirtualMachine, id.getId()));
                                } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                                    Utility.logWarning(this.logger, e);
                                } catch (final InterruptedException e) {
                                    this.logger.log(Level.WARNING, "Interrupted!", e);
                                    // Restore interrupted state...
                                    Thread.currentThread().interrupt();
                                }

                            } else if (StringUtils.equals(id.getType(), EntityType.VirtualApp.toString())) {
                                fco = new VirtualAppManager(connetionManager.getVimConnection(getOptions().getVim()),
                                        MorefUtil.newManagedObjectReference(EntityType.VirtualApp, id.getId()));

                            } else if (StringUtils.equals(id.getType(), "Any")) {
                                fco = connetionManager.getVimConnection(getOptions().getVim()).getFind()
                                        .findIvdById(id.getId());

                            } else {
                                if (this.logger.isLoggable(Level.INFO)) {
                                    this.logger.info(String.format("\t\t%s %s", id.getType(), id.getId()));
                                }

                            }
                            if (fco != null) {
                                resultAction.getFcoList().add(fco);
                            }
                        }

                    }
                    result.add(resultAction);
                }
            }
        }
        return result;
    }

    protected List<CoreResultActionTagCategory> actionListCategory(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionTagCategory> result = new ArrayList<>();

        final List<CategoryModel> cat = connetionManager.listTagCategories(getOptions().getVim(), new HashSet<>());
        for (final CategoryModel model : cat) {
            try (final CoreResultActionTagCategory resultAction = new CoreResultActionTagCategory()) {
                resultAction.start();
                resultAction.setCategoryName(model.getName());
                resultAction.setDescription(model.getDescription());
                resultAction
                        .setCardinality((model.getCardinality() == Cardinality.SINGLE) ? TagCardinalityOptions.single
                                : TagCardinalityOptions.multiple);
                resultAction.setAssociableTypes(model.getAssociableTypes());
                resultAction.setId(model.getId());
                result.add(resultAction);
            }
        }
        return result;
    }

    protected List<ResultActionRemoveTag> actionRemoveTag(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<ResultActionRemoveTag> result = new ArrayList<>();
        final Map<String, FcoTarget> tagsTarget = getOptions().getTargetFcoList();
        final List<TagModel> tags = connetionManager.listTags(getOptions().getVim(), tagsTarget.keySet());

        for (final TagModel tagId : tags) {

            try (final ResultActionRemoveTag resultAction = new ResultActionRemoveTag()) {
                resultAction.start();
                resultAction.setTagName(tagId.getName());
                resultAction.setDescription(tagId.getDescription());
                resultAction.setTagId(tagId.getId());
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (!getOptions().isDryRun()) {
                        try {
                            if (!connetionManager.removeTag(getOptions().getVim(), tagId.getId())) {
                                resultAction.failure();
                            }

                        } catch (final Exception e) {
                            resultAction.failure(e);
                        }
                    }
                    result.add(resultAction);
                }
            }
        }
        return result;
    }

    protected ResultActionRemoveTagCategory actionRemoveTagCategory(final ConnectionManager connetionManager)
            throws CoreResultActionException {

        try (final ResultActionRemoveTagCategory resultAction = new ResultActionRemoveTagCategory()) {
            resultAction.start();
            resultAction.setCategoryName(this.assetCategory);
            if (Vmbk.isAbortTriggered()) {
                resultAction.aborted("Aborted on user request");

            } else {
                if (!getOptions().isDryRun()) {
                    try {
                        if ((resultAction.getState() != OperationState.SKIPPED)
                                && !connetionManager.removeTagCategorySpec(getOptions().getVim(), this.assetCategory)) {
                            resultAction.failure();
                        }

                    } catch (final Exception e) {
                        resultAction.failure(e);
                    }
                }
            }
            return resultAction;
        }
    }

    @Override
    protected String getLogName() {
        return "TagCommand";
    }

    @Override
    public final void initialize() {
        this.options = new CoreBasicCommandOptions();
        this.create = false;
        this.description = "";
        this.assetCategory = null;
        this.list = null;
        this.remove = false;
        getOptions().setAnyFcoOfType(FirstClassObjectFilterType.tagElement);
        this.cardinality = null;
        this.associableTypes = new ArrayList<>();
        this.attach = false;
        this.detach = false;
        this.tagName = null;
    }

}
