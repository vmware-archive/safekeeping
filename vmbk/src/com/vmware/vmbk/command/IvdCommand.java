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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.TagModel;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfo;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vim25.VslmTagEntry;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.info.vmTypeSearch;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.type.DiskFileProvisioningType;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FcoTag;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyBoolean.BooleanType;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbk.type.PrettyNumber.MetricPrefix;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

public class IvdCommand extends CommandWithOptions {
    protected enum TagOperationOptions {
	list, attach, detach
    }

    protected TagOperationOptions tag;
    protected boolean attach;
    protected Boolean cbt;
    protected boolean create;
    protected String datastoreName;
    protected boolean detach;
    protected boolean detail;
    protected String device;
    protected Boolean disableRelocation;
    protected DiskFileProvisioningType diskType;
    protected boolean extend;
    protected Boolean keepAfterDeleteVm;
    protected boolean list;
    protected String name;
    protected String promote;
    protected boolean reconcile;
    protected boolean remove;
    protected boolean rename;
    protected Long size;
    protected final LinkedList<String> sbpmProfileNames;

    public IvdCommand() {
	initialize();
	this.sbpmProfileNames = new LinkedList<>();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {

	logger.entering(getClass().getName(), "action", vmbk);
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	if (this.reconcile) {
	    action_Reconcile(connetionManager);
	} else if (this.list) {
	    action_List(connetionManager);
	} else if (this.cbt != null) {
	    action_Cbt(connetionManager);
	} else if (this.keepAfterDeleteVm != null) {
	    action_KeepAfterDeleteVm(connetionManager);
	} else if (this.disableRelocation != null) {
	    action_DisableRelocation(connetionManager);
	} else if (this.remove) {
	    action_Remove(connetionManager);
	} else if (this.attach) {
	    action_Attach(connetionManager);
	} else if (this.detach) {
	    action_Detach(connetionManager);
	} else if (this.promote != null) {
	    action_Promote(connetionManager);
	} else if (this.create) {
	    action_Create(connetionManager);
	} else if (this.tag != null) {
	    switch (this.tag) {
	    case attach:
		action_AttachTag(connetionManager);
		break;
	    case detach:
		action_DetachTag(connetionManager);
		break;
	    case list:
		action_ListTags(connetionManager);
		break;
	    default:
		break;
	    }
	} else if (this.extend) {
	    action_Expand(connetionManager);
	} else if (this.rename) {
	    action_Rename(connetionManager);
	}
	logger.exiting(getClass().getName(), "action");
    }

    private boolean action_Attach(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Attach", connetionManager);
	boolean result = true;

	VirtualMachineManager vmm = null;
	ImprovedVirtuaDisk ivd = null;

	final LinkedList<FirstClassObject> vmList = getFcoTarget(connetionManager,
		FirstClassObjectFilterType.vm | FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.noTag);

	if (vmList.size() != 2) {
	    IoFunction.showWarning(logger, "No target specified");
	} else {
	    for (final FirstClassObject fco : vmList) {
		if (fco instanceof ImprovedVirtuaDisk) {
		    ivd = (ImprovedVirtuaDisk) fco;
		} else if (fco instanceof VirtualMachineManager) {
		    vmm = (VirtualMachineManager) fco;
		}
	    }
	    if (ivd == null) {
		IoFunction.showWarning(logger, "No Improved Virtual Disk specified");
	    } else if (vmm == null) {
		IoFunction.showWarning(logger, "No target Virtual Machine specified");
	    } else {
		IoFunction.showInfo(logger, "\n %-36s\t%s\t  %s", "uuid", "moref", "name");
		IoFunction.showInfo(logger, " %s\t%s\t%s", vmm.getUuid(), vmm.getMorefValue(), vmm.getName());
		final String[] ctrl_disk = (StringUtils.isEmpty(this.device)) ? new String[0] : this.device.split(":");
		if (this.isDryRun()) {
		    IoFunction.showInfo(logger, "DryRun");
		} else {
		    final Integer controllerKey = null;
		    Integer unitNumber = null;
		    switch (ctrl_disk.length) {
		    case 0:
			break;
		    case 2:
			if (StringUtils.isNumeric(ctrl_disk[1])) {
			    unitNumber = PrettyNumber.toInteger(ctrl_disk[1]);
			} else {
			    IoFunction.showWarning(logger, "Invalid id number %s", ctrl_disk[1]);
			    result = false;
			    break;
			}
		    case 1:
			if (StringUtils.isNumeric(ctrl_disk[1])) {
			    unitNumber = PrettyNumber.toInteger(ctrl_disk[1]);
			} else {
			    IoFunction.showWarning(logger, "Invalid controller number %s", ctrl_disk[0]);
			    result = false;
			}
			break;
		    default:
			IoFunction.showWarning(logger, "Invalid arguments number %s", this.attach);
			result = false;
			break;
		    }
		    if (result) {
			result = vmm.attachDisk(ivd, controllerKey, unitNumber);
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_Attach", result);
	return result;
    }

    private void action_AttachTag(final ConnectionManager connetionManager) {// , final FcoTag tag) {
	logger.entering(getClass().getName(), "action_AttachTag", new Object[] { connetionManager });

	final LinkedHashMap<String, vmTypeSearch> tagsTarget = getTagTargets();
	final List<TagModel> tags = connetionManager.listTags(this.vim, tagsTarget.keySet());
	if (tags.size() == 0) {
	    IoFunction.showWarning(logger, "No valid Tag(s) to attach");
	} else {
	    final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);
	    if (ivdList.size() == 0) {
		IoFunction.showWarning(logger, "No target specified");
	    } else {

		for (final FirstClassObject fco : ivdList) {
		    if (Vmbk.isAbortTriggered()) {
			IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    } else {
			if (fco instanceof ImprovedVirtuaDisk) {
			    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			    for (final TagModel tagM : tags) {
				final FcoTag tag = new FcoTag();
				tag.category = connetionManager.getTagCategory(this.vim, tagM.getCategoryId())
					.getName();
				tag.tag = tagM.getName();

				if (!this.isDryRun()) {
				    if (ivd.AttachTag(tag)) {
					IoFunction.showInfo(logger, "Ivd %s attached to Tag %s:%s succeeded.",
						ivd.getName(), tag.category, tag.tag);
				    } else {
					IoFunction.showWarning(logger, "Ivd %s failed to attach Tag %s:%s .",
						ivd.getName(), tag.category, tag.tag);
				    }
				}
			    }

			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_AttachTag");
    }

    private void action_Cbt(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Cbt", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else {
	    for (final FirstClassObject fco : ivdList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			if (!this.isDryRun()) {
			    if (ivd.setChangeBlockTracking(this.cbt)) {
				IoFunction.showInfo(logger, "Ivd %s cbt set to %s succeeded.", ivd.getName(),
					this.cbt ? "enable" : "disable");
			    } else {
				IoFunction.showWarning(logger, "Ivd %s cbt set to %s failed.", ivd.getName(),
					this.cbt ? "enable" : "disable");
			    }
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_Cbt");
    }

    private void action_Create(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Create", connetionManager);

	final BaseConfigInfoDiskFileBackingInfoProvisioningType backingType = BaseConfigInfoDiskFileBackingInfoProvisioningType
		.fromValue(this.diskType.toString());

	if (StringUtils.isNotBlank(this.name)) {
	    if (!this.isQuiet()) {
		if (!IoFunction.confirmOperation("Create ivd: %s  size: %d type: %s", this.name, this.size,
			this.diskType.toString())) {
		    return;
		}
	    }
	    if (this.isDryRun()) {
		return;
	    }

	    connetionManager.ivd_create(this.vim, this.name, this.datastoreName, backingType, this.size,
		    this.sbpmProfileNames);

	}
	logger.exiting(getClass().getName(), "action_Create");
    }

    private void action_Detach(final ConnectionManager connetionManager) {
	try {
	    logger.entering(getClass().getName(), "action_Detach", connetionManager);

	    VirtualMachineManager vmm = null;
	    ImprovedVirtuaDisk ivd = null;

	    final LinkedList<FirstClassObject> vmList = getFcoTarget(connetionManager,
		    FirstClassObjectFilterType.vm | FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.noTag);

	    if (vmList.size() != 2) {
		IoFunction.showWarning(logger, "No target specified");
	    } else {
		for (final FirstClassObject fco : vmList) {
		    if (fco instanceof ImprovedVirtuaDisk) {
			ivd = (ImprovedVirtuaDisk) fco;
		    } else if (fco instanceof VirtualMachineManager) {
			vmm = (VirtualMachineManager) fco;
		    }
		}
		if (ivd == null) {
		    IoFunction.showWarning(logger, "No Improved Virtual Disk specified");
		} else if (vmm == null) {
		    IoFunction.showWarning(logger, "No target Virtual Machine specified");
		} else {
		    IoFunction.showInfo(logger, "\n %-36s\t%s\t  %s", "uuid", "moref", "name");
		    IoFunction.showInfo(logger, " %s\t%s\t%s", vmm.getUuid(), vmm.getMorefValue(), vmm.getName());
		    if (!this.isQuiet()) {
			if (!IoFunction.confirmOperation("Detach ivd: %s ", ivd.toString())) {
			    return;
			}
		    }
		    if (this.isDryRun()) {
			IoFunction.showInfo(logger, "DryRun");
		    } else {
			vmm.detachDisk(ivd);
		    }
		}
	    }
	} finally {
	    logger.exiting(getClass().getName(), "action_Detach");
	}
    }

    private void action_DetachTag(final ConnectionManager connetionManager) {// , final FcoTag tag) {
	logger.entering(getClass().getName(), "action_DetachTag", new Object[] { connetionManager });
	final LinkedHashMap<String, vmTypeSearch> tagsTarget = getTagTargets();
	final List<TagModel> tags = connetionManager.listTags(this.vim, tagsTarget.keySet());
	if (tags.size() == 0) {
	    IoFunction.showWarning(logger, "No valid Tag(s) to attach");
	} else {
	    final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);

	    for (final FirstClassObject fco : ivdList) {
		if (fco instanceof ImprovedVirtuaDisk) {
		    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
		    for (final TagModel tagM : tags) {
			final FcoTag tag = new FcoTag();
			tag.category = connetionManager.getTagCategory(this.vim, tagM.getCategoryId()).getName();
			tag.tag = tagM.getName();

			if (!this.isDryRun()) {
			    if (ivd.detachTag(tag)) {
				IoFunction.showInfo(logger, "Ivd %s Tag %s:%s succeeded.", ivd.getName(), tag.category,
					tag.tag);
			    } else {
				IoFunction.showWarning(logger, "Ivd %s Tag %s:%s failed.", ivd.getName(), tag.category,
					tag.tag);
			    }
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_DetachTag");
    }

    private void action_DisableRelocation(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_DisableRelocation", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else {
	    for (final FirstClassObject fco : ivdList) {
		if (fco instanceof ImprovedVirtuaDisk) {
		    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;

		    if (!this.isDryRun()) {
			if (ivd.setDisableRelocation(this.disableRelocation)) {
			    IoFunction.showInfo(logger, "Ivd %s disableRelocation set to %s succeeded.", ivd.getName(),
				    this.cbt ? "enable" : "disable");
			} else {
			    IoFunction.showWarning(logger, "Ivd %s disableRelocation set to %s failed.", ivd.getName(),
				    this.cbt ? "enable" : "disable");
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_DisableRelocation");
    }

    private void action_Expand(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Expand", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, FirstClassObjectFilterType.ivd);
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else if (ivdList.size() > 1) {
	    IoFunction.showWarning(logger, "One single target allowed");
	} else {
	    for (final FirstClassObject fco : ivdList) {
		if (fco instanceof ImprovedVirtuaDisk) {
		    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
		    if (ivd.getConfigInfo().getCapacityInMB() < PrettyNumber.toMegaByte(this.size)) {
			if (!this.isDryRun()) {
			    if (ivd.extendDisk(this.size)) {
				IoFunction.showInfo(logger, "Ivd %s extended to  %s.", ivd.getName(),
					PrettyNumber.toString(this.size, MetricPrefix.giga));
			    } else {
				IoFunction.showWarning(logger, "Ivd %s cannot be extended to %s.", ivd.getName(),
					PrettyNumber.toString(this.size, MetricPrefix.giga));
			    }
			}
		    } else {
			IoFunction.showWarning(logger, "Ivd:%s actual size is larger than the new specified size %s.",
				ivd.getName(), PrettyNumber.toString(this.size, MetricPrefix.mega));
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_Expand");
    }

    private void action_KeepAfterDeleteVm(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_KeepAfterDeleteVm", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else {
	    for (final FirstClassObject fco : ivdList) {
		if (fco instanceof ImprovedVirtuaDisk) {
		    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
		    if (!this.isDryRun()) {
			if (ivd.setKeepAfterDeleteVm(this.keepAfterDeleteVm)) {
			    IoFunction.showInfo(logger, "Ivd %s keepAfterDeleteVm set to %s succeeded.", ivd.getName(),
				    this.cbt ? "enable" : "disable");
			} else {
			    IoFunction.showWarning(logger, "Ivd %s keepAfterDeleteVm set to %s failed.", ivd.getName(),
				    this.cbt ? "enable" : "disable");
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_KeepAfterDeleteVm");
    }

    private void action_List(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_List", connetionManager);

	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);

	IoFunction.showInfo(logger, "%s\t%9s   %9s\t%s    %s%24s\t%-85s", ImprovedVirtuaDisk.headerToString(), "size",
		"cbt", "snapshot", "attached", "datastore", "path");
	for (final FirstClassObject fco : ivdList) {
	    if (Vmbk.isAbortTriggered()) {
		IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
	    } else {
		if (fco instanceof ImprovedVirtuaDisk) {
		    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
//
//		    final RetrieveVStorageObjSpec retrieveVStorageObjSpec = new RetrieveVStorageObjSpec();
//		    retrieveVStorageObjSpec.setDatastore(ivd.getDatastore().getMoref());
//		    retrieveVStorageObjSpec.setId(ivd.getId());
//
		    final VStorageObjectConfigInfo config = ivd.getConfigInfo();
		    if (config == null) {
			IoFunction.showInfo(logger, "%s ", ivd.toString());
		    } else {
//			final ArrayList<RetrieveVStorageObjSpec> listRetrieve = new ArrayList<>();
//			listRetrieve.add(retrieveVStorageObjSpec);
			final List<VStorageObjectAssociations> listAssociation = ivd.getVslmConnection()
				.retrieveVStorageObjectAssociations(ivd);

//				    ivd.getVimPort().retrieveVStorageObjectAssociations(
//				    ((BasicVimConnection) ivd.getConnection()).getVsoManager(), listRetrieve);

			final StringBuilder vmDiskStr = new StringBuilder();
			if (this.detail) {
			    for (final VStorageObjectAssociations association : listAssociation) {
				for (final VStorageObjectAssociationsVmDiskAssociations vmDisk : association
					.getVmDiskAssociations()) {
				    final ManagedObjectReference vmMor = MorefUtil.create(EntityType.VirtualMachine,
					    vmDisk.getVmId());

				    final VirtualMachineManager vmm = new VirtualMachineManager(
					    ((VimConnection) ivd.getConnection()), vmMor);
				    if (vmDiskStr.length() > 0) {
					vmDiskStr.append("\n");
					vmDiskStr.append(StringUtils.repeat(" ", 66 + 10 + 12 + 40));

				    }
				    vmDiskStr.append(vmm.toString());
				    vmDiskStr.append("(ctrl key:");
				    vmDiskStr.append(vmDisk.getDiskKey());
				    vmDiskStr.append(')');
				    vmDiskStr.append('\n');

				}
			    }
			}
			final BaseConfigInfoDiskFileBackingInfo backing = (BaseConfigInfoDiskFileBackingInfo) config
				.getBacking();
			final String filePath = backing.getFilePath()
				.substring(backing.getFilePath().lastIndexOf(']') + 2);
			final String cbt = PrettyBoolean.toString(config.isChangedBlockTrackingEnabled(),
				BooleanType.standard);
			final boolean attached = listAssociation.size() > 0;
			IoFunction.showInfo(logger, "%s\t%10dMB   %6s\t%8s    %8s%24s\t%-85s", ivd.toString(),
				config.getCapacityInMB(), cbt,
				PrettyBoolean.toString(backing.getParent() != null, BooleanType.yesNo),
				PrettyBoolean.toString(attached, BooleanType.yesNo), ivd.getDatastoreInfo().getName(),
				filePath);
			if (vmDiskStr.length() > 0) {
			    IoFunction.showInfo(logger, "\t\tAssociated Virtual Machine\n\t\t%s", vmDiskStr.toString());
			}
		    }
		}
	    }
	}

	logger.exiting(getClass().getName(), "action_List");
    }

    private void action_ListTags(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_ListTags", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);
	for (final FirstClassObject fco : ivdList) {
	    try {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			final List<VslmTagEntry> lTags = ivd.listTags();
			if ((lTags != null) && (lTags.size() > 0)) {
			    IoFunction.printf("%s ", ivd.toString());
			    IoFunction.println();
			    IoFunction.printf("\t%-30s%-40s", "category", "name");
			    IoFunction.println();
			    for (final VslmTagEntry t : lTags) {
				IoFunction.printf("\t%-30s%-40s", t.getParentCategoryName(), t.getTagName());
				IoFunction.println();
			    }
			    IoFunction.println();
			}
		    }
		}
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	}
	logger.exiting(getClass().getName(), "action_ListTags");
    }

    private void action_Promote(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Promote", connetionManager);

	final LinkedList<FirstClassObject> vmList = getFcoTarget(connetionManager, FirstClassObjectFilterType.vm);
	if (vmList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else {
	    IoFunction.println("\nVirtual Machines:");
	    for (final FirstClassObject fco : vmList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;

			IoFunction.showInfo(logger, "\n %-36s\t%s\t  %s", "uuid", "moref", "name");
			IoFunction.showInfo(logger, " %s\t%s\t%s", vmm.getUuid(), vmm.getMorefValue(), vmm.getName());
			IoFunction.println(" vmdk:");
			final List<String> allDisk = vmm.getConfig().getAllDiskNameList();
			int i = 0;
			for (final String disk : allDisk) {
			    IoFunction.showInfo(logger, "\t%d\t%s ", i, disk);
			    ++i;
			}
			if (StringUtils.isNumeric(this.promote)) {
			    if (!this.isQuiet()) {
				if (!IoFunction.confirmOperation("Promote %s disk to ivd", vmm.toString())) {
				    continue;
				}
			    }
			    if (this.isDryRun()) {
				IoFunction.showInfo(logger, "DryRun");
			    } else {
				final int p = PrettyNumber.toInteger(this.promote);
				final String ivdName = StringUtils.isEmpty(this.name)
					? String.format("vmdk_%d_%s", p, vmm.getName())
					: this.name;
				((VimConnection) vmm.getConnection()).getVslmConnection()
					.ivd_promote(vmm.getDatacenterInfo(), allDisk.get(p), ivdName);
			    }
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_Promote");
    }

    private void action_Reconcile(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Reconcile", connetionManager);
	if (StringUtils.isEmpty(this.datastoreName)) {
	    IoFunction.showWarning(logger, "No datastore specified. Use -datastore ");
	} else {
	    connetionManager.ivd_ReconcileDatastore(this.vim, this.datastoreName);
	}
	logger.exiting(getClass().getName(), "action_Reconcile");
    }

    private void action_Remove(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Remove", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else {
	    for (final FirstClassObject fco : ivdList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;

			if (!this.isQuiet()) {
			    if (!IoFunction.confirmOperation("Destroy ivd: %s ", ivd.toString())) {
				continue;
			    }
			}
			if (this.isDryRun()) {
			    continue;
			}
			if (ivd.destroy()) {
			    IoFunction.showInfo(logger, "Destroy Improved Virtual Disks %s success", ivd.getName());

			} else {
			    IoFunction.showWarning(logger, "Destroy Improved Virtual Disks  %s fails", ivd.getName());
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_Remove");
    }

    private void action_Rename(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Rename", connetionManager);
	final LinkedList<FirstClassObject> ivdList = getFcoTarget(connetionManager, FirstClassObjectFilterType.ivd);
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No target specified");
	} else if (ivdList.size() > 1) {
	    IoFunction.showWarning(logger, "One single target allowed");
	} else {
	    for (final FirstClassObject fco : ivdList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;

			final String oldName = ivd.getName();

			if (!this.isDryRun()) {
			    if (ivd.rename(this.name)) {
				IoFunction.showInfo(logger, "Ivd %s rename to  %s.", oldName, ivd.getName());
			    } else {
				IoFunction.showWarning(logger, "Ivd %s rename to %s.", oldName, ivd.getName());
			    }
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "action_Rename");
    }

    public String getDatastoreName() {
	return this.datastoreName;
    }

    @Override
    public void initialize() {
	super.initialize();
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
	this.anyFcoOfType = FirstClassObjectFilterType.ivd;
	this.diskType = DiskFileProvisioningType.THIN;
	this.size = null;
	this.name = null;
	this.detail = false;
	this.device = null;
	this.detach = false;
	this.device = null;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("ivd: ");

	if (this.datastoreName != null) {
	    sb.append(String.format("[datastore %s]", (this.datastoreName.isEmpty()) ? "any" : this.datastoreName));
	}
	sb.append(String.format("[list %s]", PrettyBoolean.toString(this.list)));

	sb.append(String.format("[reconcile %s]", PrettyBoolean.toString(this.reconcile)));
	sb.append(String.format("[remove %s]", PrettyBoolean.toString(this.remove)));
	sb.append(String.format("[quiet %s]", PrettyBoolean.toString(this.isQuiet())));
	return sb.toString();
    }
}
