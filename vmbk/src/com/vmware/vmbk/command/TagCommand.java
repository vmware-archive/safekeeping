/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.CategoryModel.Cardinality;
import com.vmware.cis.tagging.CategoryTypes;
import com.vmware.cis.tagging.TagAssociationTypes.BatchResult;
import com.vmware.cis.tagging.TagModel;
import com.vmware.cis.tagging.TagTypes;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.info.vmTypeSearch;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

public class TagCommand extends CommandWithOptions {
    protected enum TagCardinalityOptions {
	single, multiple
    }

    protected enum TagListOptions {
	tag, category
    }

    protected String assetCategory;
    protected List<EntityType> associableTypes;
    protected boolean attach;
    protected boolean create;
    // protected boolean createTagCategory;
    protected String description;
    protected boolean detach;

    protected TagListOptions list;

    protected String tagName;

    protected boolean remove;
    // protected boolean removeCategory;
    protected TagCardinalityOptions cardinality;

    protected TagCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	if (this.list != null) {
	    if (this.list == TagListOptions.tag) {
		action_List(connetionManager);
	    } else {
		action_ListCategory(connetionManager);
	    }
	} else if (this.attach) {
	    action_Attach(connetionManager);
	} else if (this.detach) {
	    action_Detach(connetionManager);
	} else if (this.create) {
	    if (StringUtils.isEmpty(this.tagName)) {
		action_CreateTagCategory(connetionManager);
	    } else {
		action_CreateTag(connetionManager);
	    }
	} else if (this.remove) {
	    if (StringUtils.isNotEmpty(this.assetCategory)) {
		action_RemoveTagCategory(connetionManager);
	    } else {
		action_RemoveTag(connetionManager);
	    }
	}
	logger.exiting(getClass().getName(), "action");
    }

    private void action_Attach(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Attach", connetionManager);

	int countVm = 0;
	int countIvd = 0;

	try {

	    final LinkedHashMap<String, vmTypeSearch> tagsTarget = getTagTargets();
//	    if (tagsTarget.size() == 0) {
//		IoFunction.showWarning(logger, "No Tags specified");
//		return;
//	    }
	    final List<TagModel> tags = connetionManager.listTags(this.vim, tagsTarget.keySet());

	    if (tags.size() == 0) {
		IoFunction.showWarning(logger, "No valid Tag(s) to attach");
		return;
	    }
	    final ArrayList<String> tagIds = new ArrayList<>();
	    for (final TagModel t : tags) {
		tagIds.add(t.getId());
	    }
	    final LinkedList<FirstClassObject> fcoList = getFcoTarget(connetionManager,
		    FirstClassObjectFilterType.any | FirstClassObjectFilterType.noTag);

	    if ((fcoList.size()) > 0) {
		for (final FirstClassObject fco : fcoList) {
		    BatchResult batchResult = null;
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;

			final DynamicID vmDynamicId = new DynamicID(vmm.getMoref().getType(),
				vmm.getMoref().getValue());
			batchResult = ((VimConnection) vmm.getConnection()).getVapiService().getTagAssociation()
				.attachMultipleTagsToObject(vmDynamicId, tagIds);

			if (batchResult.getSuccess()) {

			    ++countVm;
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;

			final DynamicID vmDynamicId = new DynamicID("vmomi:fcd", ivd.getId().getId());

			batchResult = ((VimConnection) ivd.getConnection()).getVapiService().getTagAssociation()
				.attachMultipleTagsToObject(vmDynamicId, tagIds);
			if (batchResult.getSuccess()) {
			    ++countIvd;
			}

		    }
		    if ((batchResult != null) && batchResult.getSuccess()) {
			IoFunction.showInfo(logger, "Tag(s) attached to : %s", fco.toString());
		    } else {
			IoFunction.showWarning(logger, "Tag(s) failed to attach %s", fco.toString());
		    }
		}
		IoFunction.printTotal("%d vm:%d ivd:%d", countVm + countIvd, countVm, countIvd);

	    } else {
		IoFunction.showWarning(logger, "No valid targets");

	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "action_Attach");

    }

    private void action_CreateTag(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_CreateTag", connetionManager);
	final TagTypes.CreateSpec spec = new TagTypes.CreateSpec();
	spec.setName(this.tagName);
	spec.setDescription(this.description);

	final HashSet<String> categoriesName = new HashSet<>();
	categoriesName.add(this.assetCategory);
	final LinkedList<CategoryModel> tagCategory = connetionManager.listTagCategories(this.vim, categoriesName);
	if (tagCategory.size() == 0) {
	    IoFunction.showWarning(logger, "Category %s doesn't exist", categoriesName);
	} else {
	    spec.setCategoryId(tagCategory.get(0).getId());

	    connetionManager.createTag(this.vim, spec);

	}
	logger.exiting(getClass().getName(), "action_CreateTag");
    }

    private HashSet<String> action_CreateTagCategory(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_CreateTagCategory", connetionManager);
	final CategoryTypes.CreateSpec createSpec = new CategoryTypes.CreateSpec();
	createSpec.setName(this.assetCategory);
	createSpec.setDescription(this.description);
	createSpec.setCardinality(
		(this.cardinality == TagCardinalityOptions.single) ? Cardinality.SINGLE : Cardinality.MULTIPLE);
	createSpec.setAssociableTypes(new HashSet<String>());
	for (final EntityType entity : this.associableTypes) {
	    createSpec.getAssociableTypes().add("urn:vim25:".concat(entity.toString()));
	}

	final HashSet<String> result = connetionManager.createTagCategorySpec(this.vim, createSpec);
	logger.exiting(getClass().getName(), "action_CreateTagCategory", result);
	return result;
    }

    private void action_Detach(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Detach", connetionManager);

	int countVm = 0;
	int countIvd = 0;
	final int countVapp = 0;

	try {

	    final LinkedHashMap<String, vmTypeSearch> tagsTarget = getTagTargets();
	    if (tagsTarget.size() == 0) {
		IoFunction.showWarning(logger, "No Tags specified");
		return;
	    }
	    final List<TagModel> tags = connetionManager.listTags(this.vim, tagsTarget.keySet());

	    if (tags.size() == 0) {
		IoFunction.showWarning(logger, "No valid Tags");
		return;
	    }
	    final ArrayList<String> tagIds = new ArrayList<>();
	    for (final TagModel t : tags) {
		tagIds.add(t.getId());
	    }
	    final LinkedList<FirstClassObject> fcoList = getFcoTarget(connetionManager,
		    FirstClassObjectFilterType.any | FirstClassObjectFilterType.noTag);

	    if ((fcoList.size()) > 0) {
		for (final FirstClassObject fco : fcoList) {
		    BatchResult batchResult = null;
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;

			final DynamicID vmDynamicId = new DynamicID(vmm.getMoref().getType(),
				vmm.getMoref().getValue());
			batchResult = ((VimConnection) vmm.getConnection()).getVapiService().getTagAssociation()
				.detachMultipleTagsFromObject(vmDynamicId, tagIds);

			if (batchResult.getSuccess()) {

			    ++countVm;
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;

			final DynamicID vmDynamicId = new DynamicID("vmomi:fcd", ivd.getId().getId());
			batchResult = ((VimConnection) ivd.getConnection()).getVapiService().getTagAssociation()
				.detachMultipleTagsFromObject(vmDynamicId, tagIds);
			if (batchResult.getSuccess()) {
			    ++countIvd;
			}

		    }
		    if ((batchResult != null) && batchResult.getSuccess()) {
			IoFunction.showInfo(logger, "Tag(s) detached from : %s", fco.toString());
		    } else {
			IoFunction.showWarning(logger, "Tag(s) failed to detached from %s", fco.toString());
		    }
		}

		IoFunction.printTotal("%d vm:%d ivd:%d vapp:%d", countVm + countIvd, countVm, countIvd, countVapp);
	    } else {
		IoFunction.showWarning(logger, "No valid targets");

	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "action_Detach");

    }

    private void action_List(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_List", connetionManager);

	final LinkedHashMap<String, vmTypeSearch> ivdTarget = this.getTagTargets();
	final List<TagModel> tags = connetionManager.listTags(this.vim, ivdTarget.keySet());
	for (final TagModel tag : tags) {
	    final CategoryModel tagCategory = connetionManager.getTagCategory(this.vim, tag.getCategoryId());
	    IoFunction.showInfo(logger, "Name: %s\n\tCategory:%s\n\tid: %s\n\tDescription: %s", tag.getName(),
		    tagCategory.getName(), tag.getId(), tag.getDescription());
	    final List<DynamicID> attachedObject = connetionManager.listTagAttachedObjects(this.vim, tag.getId());
	    if (attachedObject != null) {

		if (attachedObject.size() > 0) {
		    IoFunction.showInfo(logger, "\tAttached objects: ");
		    int vm = 0;
		    int ivd = 0;
		    int vApp = 0;
		    int other = 0;
		    for (final DynamicID id : attachedObject) {
			if (StringUtils.equals(id.getType(), EntityType.VirtualMachine.toString())) {
			    final VirtualMachineManager vmm = new VirtualMachineManager(
				    connetionManager.getVimConnection(this.vim),
				    MorefUtil.create(EntityType.VirtualMachine, id.getId()));
			    IoFunction.showInfo(logger, "\t\t%s", vmm.toString());
			    ++vm;
			} else if (StringUtils.equals(id.getType(), EntityType.VirtualApp.toString())) {
			    ++vApp;
			} else if (StringUtils.equals(id.getType(), "Any")) {
			    ++ivd;

			} else {
			    IoFunction.showInfo(logger, "\t\t%s %s", id.getType(), id.getId());
			    ++other;
			}

		    }
		    IoFunction.showInfo(logger, "\tTotal %d objects - vm:%d ivd:%d vApp:%d other:%d",
			    attachedObject.size(), vm, ivd, vApp, other);
		} else {
		    IoFunction.showInfo(logger, "\tNo objects attached.");
		}
	    }
	}

	logger.exiting(getClass().getName(), "action_List");
    }

    private void action_ListCategory(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_ListCategory", connetionManager);

	final List<CategoryModel> categories = connetionManager.listTagCategories(this.vim, new HashSet<String>());
	for (final CategoryModel tag : categories) {
	    IoFunction.showInfo(logger,
		    "Name: %s\n\tTags per Object:%s\n\tid: %s\n\tDescription: %s\n\tAssociable Objects: %s\n\n",
		    tag.getName(), ((tag.getCardinality() == Cardinality.MULTIPLE) ? "Many" : "Single"), tag.getId(),
		    tag.getDescription(),
		    ((tag.getAssociableTypes().size() > 0) ? StringUtils.join(tag.getAssociableTypes().toArray(), " ")
			    : "Any Type"));
	}
	logger.exiting(getClass().getName(), "action_ListCategory");

    }

    private void action_RemoveTag(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_RemoveTag", connetionManager);
	boolean result = false;
	final LinkedHashMap<String, vmTypeSearch> tagsTarget = getTagTargets();
	if (tagsTarget.size() == 0) {
	    IoFunction.showWarning(logger, "No Tags specified");
	    return;
	}
	final LinkedList<TagModel> tags = connetionManager.listTags(this.vim, tagsTarget.keySet());

	final HashSet<String> tagId = new HashSet<>();
	for (final TagModel t : tags) {
	    tagId.add(t.getId());
	}
	if (tags.size() == 0) {
	    IoFunction.showWarning(logger, "tags %s doesn't exist", tagsTarget.keySet());
	} else {
	    if (!this.isQuiet()) {
		if (IoFunction.confirmOperation("Remove Tag(s): %s ", tagsTarget.keySet())) {
		    result = connetionManager.removeTag(this.vim, tagId);
		}
	    } else {
		result = connetionManager.removeTag(this.vim, tagId);
	    }
	    if (result) {
		IoFunction.showInfo(logger, "Tag(s) %s removed ", tagsTarget.keySet());
	    } else {
		IoFunction.showWarning(logger, "Failed to remove Tag(s) %s ", tagsTarget.keySet());
	    }
	}
	logger.exiting(getClass().getName(), "action_RemoveTag");
    }

    private boolean action_RemoveTagCategory(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_RemoveTagCategory", connetionManager);
	boolean result = true;
	if (this.isDryRun()) {
	    result = true;
	} else {
	    if (!this.isQuiet()) {
		if (!IoFunction.confirmOperation("Remove Tag Category %s", this.assetCategory)) {
		    result = false;
		}
	    }
	    if (result) {
		result = connetionManager.removeTagCategorySpec(this.vim, this.assetCategory);

		if (result) {
		    IoFunction.showInfo(logger, "Category %s removed ", this.assetCategory);
		} else {
		    IoFunction.showWarning(logger, "Failed to remove Category %s ", this.assetCategory);
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_RemoveTagCategory", result);
	return result;
    }

    @Override
    public void initialize() {
	super.initialize();
	this.create = false;
	this.description = "";
	this.assetCategory = null;
	this.list = null;
	this.remove = false;
	this.anyFcoOfType = FirstClassObjectFilterType.tagElement;
	this.cardinality = null;
	this.associableTypes = new ArrayList<>();
	this.attach = false;
	this.detach = false;
	this.tagName = null;
    }

}
