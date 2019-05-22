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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.TagModel;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vim25.ID;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.info.vmTypeSearch;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.K8s;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.BiosUuid;
import com.vmware.vmbk.util.Utility;

abstract class CommandWithOptions implements Command {
    private static final Pattern IP4PATTERN = Pattern
	    .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    protected static Logger logger = Logger.getLogger("Command");

    static final Pattern UUIDPATTERN = Pattern
	    .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    protected Integer anyFcoOfType;

    private boolean quiet;
    private boolean isDryRun;

    protected List<String> targetList;

    protected String vim;

    protected CommandWithOptions() {
	initialize();
    }

    private LinkedList<FirstClassObject> getAnyImprovedVirtualDisk(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getAnyImprovedVirtualDisk", new Object[] { connetionManager });
	final LinkedList<FirstClassObject> result = new LinkedList<>();
	final LinkedList<ImprovedVirtuaDisk> ivdList = connetionManager.getAllIvdList();
	if (ivdList.size() == 0) {
	    IoFunction.showWarning(logger, "No Improved Virtual Disks are available");
	} else {
	    int i = 0;
	    for (final ImprovedVirtuaDisk ivd : ivdList) {
		logger.info(String.format("%d: %s %s %s ", ++i, ivd.getUuid(), ivd.getName(),
			ivd.getDatastoreInfo().toString()));
		result.add(ivd);
	    }
	}
	logger.exiting(getClass().getName(), "getAnyImprovedVirtualDisk", result);
	return result;
    }

    public LinkedHashMap<String, vmTypeSearch> getAnySpecifiedTargets() {
	logger.entering(getClass().getName(), "getAnySpecifiedTargets");
	final LinkedHashMap<String, vmTypeSearch> result = getVmTargets();
	result.putAll(getIvdTargets());
	result.putAll(getVappTargets());
	logger.exiting(getClass().getName(), "getAnySpecifiedTargets", result);
	return result;
    }

    private LinkedList<VirtualAppManager> getAnyVapp(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getAnyVapp", connetionManager);
	final String vmFilter = GlobalConfiguration.getVmFilter();
	final LinkedList<VirtualAppManager> result = connetionManager.getAllVAppList(this.vim, vmFilter);
	logger.exiting(getClass().getName(), "getAnyVapp", result);
	return result;
    }

    private LinkedList<VirtualMachineManager> getAnyVirtualMachine(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getVmTarget", new Object[] { connetionManager });
	final String vmFilter = GlobalConfiguration.getVmFilter();
	final LinkedList<VirtualMachineManager> result = new LinkedList<>();
	final LinkedList<VirtualMachineManager> vmmList = connetionManager.getAllVmList(this.vim, vmFilter);

	if (vmmList.size() == 0) {
	    LoggerUtils.logInfo(logger, "No virtual machine available with filter %s", vmFilter);

	} else {
	    final HashMap<String, VStorageObjectAssociations> vStorageObjectAssociations = retrieveVStorageObjectAssociations(
		    connetionManager);
	    for (final VirtualMachineManager vmm : vmmList) {

		searchIvdAssociation(vStorageObjectAssociations, vmm);
		/*
		 * Check if the VM is this backup server if true skip this server
		 */
		if (vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getServerBiosUuid())
			|| vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getBigEndianBiosUuid())) {
		    IoFunction.showInfo(logger, "Backup server uuid:%s removed from the backup list",
			    vmm.getBiosUuid());
		    continue;
		}
		result.add(vmm);
	    }
	}
	logger.exiting(getClass().getName(), "getVmTarget", result);
	return result;
    }

    LinkedList<FirstClassObject> getFcoTarget(final ConnectionManager connetionManager, final int filter) {
	logger.entering(getClass().getName(), "getFcoTarget", new Object[] { connetionManager });
	final LinkedList<FirstClassObject> result = new LinkedList<>();
	boolean tagEvaluated = false;
	if ((filter & FirstClassObjectFilterType.all) > 0) {
	    if ((filter & FirstClassObjectFilterType.noTag) == 0) {
		result.addAll(getTagTarget(connetionManager, filter));
	    }
	    if ((filter & FirstClassObjectFilterType.vm) > 0) {
		result.addAll(getAnyVirtualMachine(connetionManager));
	    }
	    if ((filter & FirstClassObjectFilterType.ivd) > 0) {
		result.addAll(getAnyImprovedVirtualDisk(connetionManager));
	    }
	    if ((filter & FirstClassObjectFilterType.vapp) > 0) {
		result.addAll(getAnyVapp(connetionManager));
	    }
	}

	if ((filter & FirstClassObjectFilterType.vm) > 0) {
	    if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
		result.addAll(getTagTarget(connetionManager, filter));
		tagEvaluated = true;
	    }
	    result.addAll(getVmTarget(connetionManager));
	}

	if ((filter & FirstClassObjectFilterType.ivd) > 0) {
	    if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
		result.addAll(getTagTarget(connetionManager, filter));
		tagEvaluated = true;
	    }
	    result.addAll(getIvdTarget(connetionManager));
	}
	if ((filter & FirstClassObjectFilterType.vapp) > 0) {
	    if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
		result.addAll(getTagTarget(connetionManager, filter));
		tagEvaluated = true;
	    }
	    result.addAll(getvAppTarget(connetionManager));
	}
	if ((filter & FirstClassObjectFilterType.k8s) > 0) {
	    if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
		result.addAll(getTagTarget(connetionManager, filter));
		tagEvaluated = true;
	    }
	    result.addAll(getK8sTarget(connetionManager));
	}
	logger.exiting(getClass().getName(), "getFcoTarget", result);
	return result;
    }

    private LinkedList<FirstClassObject> getIvdTarget(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getIvdTarget", connetionManager);
	final LinkedHashMap<String, vmTypeSearch> ivdTarget = this.getIvdTargets();
	final LinkedList<FirstClassObject> result = new LinkedList<>();

	if ((ivdTarget.size() == 0)) {
	    LoggerUtils.logInfo(logger, "No Improved Virtual Disk specified ");

	} else {

	    for (final String key : ivdTarget.keySet()) {
		try {

		    switch (ivdTarget.get(key)) {
		    case ivd_name:
			List<ImprovedVirtuaDisk> ivdList = null;
			ivdList = connetionManager.getIvdByName(this.vim, key);
			if (ivdList != null) {
			    result.addAll(ivdList);
			}
			break;
		    case ivd_uuid:
			final ImprovedVirtuaDisk ivd = connetionManager.getIvdById(this.vim, key);
			if (ivd != null) {
			    result.add(ivd);
			}

			break;
		    default:
			continue;
		    }

//	    final LinkedList<ImprovedVirtuaDisk> ivdList = connetionManager.getAllIvdList();
//
//	    if (ivdList.size() == 0) {
//		IoFunction.showWarning(logger, "No Improved Virtual Disks are available");
//
//	    } else {
//		for (final String key : ivdTarget.keySet()) {
//		    for (final ImprovedVirtuaDisk ivd : ivdList) {
//			try {
//			    switch (ivdTarget.get(key)) {
//			    case ivd_name:
//				if (ivd.getName().equalsIgnoreCase(key)) {
//
//				    result.add(ivd);
//				}
//				break;
//			    case ivd_uuid:
//				if (ivd.getUuid().equalsIgnoreCase(key)) {
//
//				    result.add(ivd);
//				}
//				break;
//			    default:
//				continue;
//			    }
//
//			} catch (final Exception e) {
//			    logger.warning(Utility.toString(e));
//			}
//		    }
//		}
		} catch (final Exception e) {
		    logger.warning(Utility.toString(e));

		}
	    }
	}
	logger.exiting(getClass().getName(), "getIvdTarget", result);
	return result;
    }

    @Override
    public LinkedHashMap<String, vmTypeSearch> getIvdTargets() {
	logger.entering(getClass().getName(), "getIvdTargets");
	final LinkedHashMap<String, vmTypeSearch> result = new LinkedHashMap<>();
	for (String target : this.targetList) {
	    if (target.startsWith("ivd:")) {
		target = target.substring("ivd:".length());
		vmTypeSearch typeVm = vmTypeSearch.ivd_name;
		if (UUIDPATTERN.matcher(target).matches()) {
		    typeVm = vmTypeSearch.ivd_uuid;
		}
		result.put(target, typeVm);
	    }

	}
	logger.exiting(getClass().getName(), "getIvdTargets", result);
	return result;
    }

    private LinkedList<FirstClassObject> getK8sTarget(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getk8sTarget", connetionManager);
	final LinkedHashMap<String, vmTypeSearch> k8sTarget = this.getK8sTargets();
	final LinkedList<FirstClassObject> result = new LinkedList<>();

	if ((k8sTarget.size() == 0)) {
	    LoggerUtils.logInfo(logger, "No Kubernetics domain specified");

	} else {
	    // final LinkedList<K8s> ivdList = new LinkedList<>();

//	    if (ivdList.size() == 0) {
//		IoFunction.showWarning(logger, "No Kubernetics domain specified");
//
//	    } else {
	    for (final String key : k8sTarget.keySet()) {

		try {
		    switch (k8sTarget.get(key)) {
		    case k8s_name:
			// if (ivd.getName().equalsIgnoreCase(key)) {
			final K8s k8 = new K8s(connetionManager, key);
			// )
			result.add(k8);
			// }
			break;
		    case k8s_uuid:
//				if (ivd.getUuid().equalsIgnoreCase(key)) {
//
//				    result.add(ivd);
//				}
			break;
		    default:
			continue;
		    }

		} catch (final Exception e) {
		    logger.warning(Utility.toString(e));
		}

//		}
	    }
	}
	logger.exiting(getClass().getName(), "getk8sTarget", result);
	return result;

    }

    @Override
    public LinkedHashMap<String, vmTypeSearch> getK8sTargets() {
	logger.entering(getClass().getName(), "getK8sTargets");
	final LinkedHashMap<String, vmTypeSearch> result = new LinkedHashMap<>();
	for (String target : this.targetList) {
	    if (target.startsWith("k8s:")) {
		target = target.substring("k8s:".length());
		vmTypeSearch typeVm = vmTypeSearch.k8s_name;
		if (UUIDPATTERN.matcher(target).matches()) {
		    typeVm = vmTypeSearch.k8s_uuid;
		}

		result.put(target, typeVm);
	    }

	}
	logger.exiting(getClass().getName(), "getK8sTargets", result);
	return result;
    }

    private LinkedList<FirstClassObject> getTagTarget(final ConnectionManager connetionManager, final int filter) {
	logger.entering(getClass().getName(), "getTagTarget", new Object[] { connetionManager, filter });
	final LinkedList<FirstClassObject> result = new LinkedList<>();
	final LinkedHashMap<String, vmTypeSearch> tagTarget = this.getTagTargets();

	if (tagTarget.size() == 0) {
	    if ((filter & FirstClassObjectFilterType.tag) > 0) {
		logger.fine("No tag specified");
	    }

	} else {
	    final List<TagModel> tags = connetionManager.listTags(this.vim, tagTarget.keySet());
	    for (final TagModel tag : tags) {
		final List<DynamicID> attachedObject = connetionManager.listTagAttachedObjects(this.vim, tag.getId());
		for (final DynamicID id : attachedObject) {
		    if (((filter & FirstClassObjectFilterType.vm) > 0)
			    && StringUtils.equals(id.getType(), EntityType.VirtualMachine.toString())) {
			final VirtualMachineManager vmm = new VirtualMachineManager(
				connetionManager.getVimConnection(this.vim),
				MorefUtil.create(EntityType.VirtualMachine, id.getId()));
			result.add(vmm);
		    } else if (((filter & FirstClassObjectFilterType.vapp) > 0)
			    && StringUtils.equals(id.getType(), EntityType.VirtualApp.toString())) {
		    } else if (((filter & FirstClassObjectFilterType.ivd) > 0)
			    && StringUtils.equals(id.getType(), EntityType.ImprovedVirtualDisk.toString())) {
		    }

		}
	    }
	}
	logger.exiting(getClass().getName(), "getTagTarget", result);
	return result;
    }

    public LinkedHashMap<String, vmTypeSearch> getTagTargets() {
	logger.entering(getClass().getName(), "getTagTargets");
	final LinkedHashMap<String, vmTypeSearch> result = new LinkedHashMap<>();
	for (String target : this.targetList) {
	    if (target.startsWith("tag:")) {
		target = target.substring("tag:".length());
		final vmTypeSearch typeVm = vmTypeSearch.tag;
		result.put(target, typeVm);
	    }

	}
	logger.exiting(getClass().getName(), "getTagTargets", result);
	return result;
    }

    LinkedList<ManagedFcoEntityInfo> getTargetfromRepository(final GlobalProfile profileAllFco) {
	logger.entering(getClass().getName(), "getTargetfromRepository", profileAllFco);
	final LinkedList<ManagedFcoEntityInfo> result = new LinkedList<>();
	if ((this.anyFcoOfType & FirstClassObjectFilterType.all) > 0) {
	    if ((this.anyFcoOfType & FirstClassObjectFilterType.vm) > 0) {

		result.addAll(profileAllFco.getAllVmEntities());
	    }

	    if ((this.anyFcoOfType & FirstClassObjectFilterType.vapp) > 0) {

		result.addAll(profileAllFco.getAllVAppEntities());
	    }
	    if ((this.anyFcoOfType & FirstClassObjectFilterType.ivd) > 0) {

		result.addAll(profileAllFco.getAllIvdEntities());
	    }
	} else {

	    final LinkedHashMap<String, vmTypeSearch> fcoTarget = getAnySpecifiedTargets();

	    if ((fcoTarget.size() > 0)) {
		ManagedFcoEntityInfo entity = null;
		for (final String key : fcoTarget.keySet()) {
		    switch (fcoTarget.get(key)) {
		    case vm_ip:
			break;
		    case vm_moref:
			entity = profileAllFco.getVirtualMachineEntityByMoref(key);
			if (entity == null) {
			    if (profileAllFco.isExistVirtualMachineWithMoref(key)) {
				profileAllFco.delGroupVmEntry(key);
			    }
			    IoFunction.showWarning(logger, "No Virtual Machine with Moref %s exist on the repository",
				    key);
			} else {
			    result.add(entity);
			}
			break;
		    case vm_name:
			entity = profileAllFco.getVirtualMachineEntityByName(key);
			if (entity == null) {
			    if (profileAllFco.isExistVirtualMachineWithName(key)) {
				profileAllFco.delGroupVmEntry(key);
			    }
			    IoFunction.showWarning(logger, "No Virtual Machine named %s exist on the repository", key);
			} else {
			    result.add(entity);
			}
			break;
		    case vapp_moref:
			entity = profileAllFco.getVirtualAppEntityByMoref(key);
			if (entity == null) {
			    if (profileAllFco.isExistVirtualMachineWithMoref(key)) {
				profileAllFco.delGroupVmEntry(key);
			    }
			    IoFunction.showWarning(logger, "No VirtualApp with Moref %s exist on the repository", key);
			} else {
			    result.add(entity);
			}
			break;
		    case vapp_name:
			entity = profileAllFco.getVirtualAppEntityByName(key);
			if (entity == null) {
			    if (profileAllFco.isExistVirtualMachineWithName(key)) {
				profileAllFco.delGroupVmEntry(key);
			    }
			    IoFunction.showWarning(logger, "No VirtualApp named %s exist on the repository", key);
			} else {
			    result.add(entity);
			}
			break;
		    case ivd_name:
			entity = profileAllFco.getImprovedVirtualDiskEntityByName(key);
			if (entity == null) {
			    if (profileAllFco.isExistImprovedVirtualDiskWithName(key)) {
				profileAllFco.delGroupIvdEntry(key);
			    }
			    IoFunction.showWarning(logger, "No Improved Virtual Disk named %s exist on the repository",
				    key);
			} else {
			    result.add(entity);
			}
			break;
		    case vm_uuid:
		    case ivd_uuid:
		    case vapp_uuid:
			entity = profileAllFco.getFirstClassObjectEntityByUuid(key);
			if (entity == null) {
			    if (profileAllFco.isExistVirtualMachineWithUuid(key)
				    || profileAllFco.isExistImprovedVirtualDiskWithUuid(key)) {
				profileAllFco.delGroupIvdEntry(key);
				profileAllFco.delGroupVmEntry(key);
			    }
			    IoFunction.showWarning(logger, "No FCO with SystemUuid %s exist on the repository", key);
			} else {
			    result.add(entity);
			}
			break;
		    default:
			break;

		    }
		}
	    } else {
		IoFunction.showWarning(logger, "No virtual machine specified ");
	    }

	}

	logger.exiting(getClass().getName(), "getTargetfromRepository", result);
	return result;
    }

    @Override
    public List<String> getTargets() {
	return this.targetList;
    }

    private LinkedList<FirstClassObject> getvAppTarget(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getvAppTarget", connetionManager);
	final int i = 0;
	final LinkedHashMap<String, vmTypeSearch> vappTargets = this.getVappTargets();
	final LinkedList<FirstClassObject> result = new LinkedList<>();
	for (final String key : vappTargets.keySet()) {

	    VirtualAppManager vApp;

	    switch (vappTargets.get(key)) {

	    case vapp_moref:
		vApp = connetionManager.findVAppByMoref(this.vim, MorefUtil.create(EntityType.VirtualApp, key));
		break;
	    case vapp_name:
		vApp = connetionManager.findVAppByName(this.vim, key);
		break;
	    case vapp_uuid:
		vApp = connetionManager.findVAppByUuid(this.vim, key);
		break;
	    default:
		continue;
	    }
	    if (vApp != null) {
		result.add(vApp);
	    }
	}

	logger.exiting(getClass().getName(), "getvAppTarget", result);
	return result;
    }

    @Override
    public LinkedHashMap<String, vmTypeSearch> getVappTargets() {
	logger.entering(getClass().getName(), "getVappTargets");
	final LinkedHashMap<String, vmTypeSearch> result = new LinkedHashMap<>();
	for (String target : this.targetList) {
	    if (target.startsWith("vapp:")) {
		target = target.substring("vapp:".length());
		vmTypeSearch typeVm = vmTypeSearch.vapp_name;
		if (target.startsWith("resgroup-")) {
		    typeVm = vmTypeSearch.vapp_moref;
		} else if (UUIDPATTERN.matcher(target).matches()) {
		    typeVm = vmTypeSearch.vapp_uuid;
		}
		result.put(target, typeVm);
	    }

	}
	logger.exiting(getClass().getName(), "getVappTargets", result);
	return result;
    }

    private LinkedList<FirstClassObject> getVmTarget(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "getVmTarget", new Object[] { connetionManager });
	final LinkedList<FirstClassObject> result = new LinkedList<>();
	if (getVmTargets().size() > 0) {

	    final HashMap<String, VStorageObjectAssociations> vStorageObjectAssociations = retrieveVStorageObjectAssociations(
		    connetionManager);
	    for (final String key : getVmTargets().keySet()) {

		try {
		    VirtualMachineManager vmm = null;
		    switch (getVmTargets().get(key)) {
		    case vm_ip:
			vmm = connetionManager.findVmByIp(this.vim, key);
			break;
		    case vm_moref:
			vmm = connetionManager.findVmByMoref(this.vim,
				MorefUtil.create(EntityType.VirtualMachine, key));
			break;
		    case vm_name:
			vmm = connetionManager.findVmByName(this.vim, key);
			break;
		    case vm_uuid:
			vmm = connetionManager.findVmByUuid(this.vim, key, true);
			break;
		    default:
			continue;
		    }
		    if (vmm != null) {
			if (vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getServerBiosUuid())
				|| vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getBigEndianBiosUuid())) {
			    IoFunction.showWarning(logger,
				    "Backup server uuid:%s removed from the list of available target",
				    vmm.getBiosUuid());

			} else {
			    result.add(vmm);
			    searchIvdAssociation(vStorageObjectAssociations, vmm);
			}
		    }
		} catch (final Exception e) {
		    logger.warning(Utility.toString(e));
		}
		// }
	    }

	}
	logger.exiting(getClass().getName(), "getVmTarget", result);
	return result;
    }

    @Override
    public LinkedHashMap<String, vmTypeSearch> getVmTargets() {
	logger.entering(getClass().getName(), "getVmTargets");
	final LinkedHashMap<String, vmTypeSearch> result = new LinkedHashMap<>();
	for (String target : this.targetList) {
	    if (target.startsWith("vm:")) {
		target = target.substring("vm:".length());
		vmTypeSearch typeVm = vmTypeSearch.vm_name;
		if (UUIDPATTERN.matcher(target).matches()) {
		    typeVm = vmTypeSearch.vm_uuid;

		} else if (IP4PATTERN.matcher(target).matches()) {
		    typeVm = vmTypeSearch.vm_ip;

		} else if (target.startsWith("vm-")) {
		    typeVm = vmTypeSearch.vm_moref;
		}
		result.put(target, typeVm);
	    } else if (target.startsWith("vm-")) {
		result.put(target, vmTypeSearch.vm_moref);
	    }
	}
	logger.exiting(getClass().getName(), "getVmTargets", result);
	return result;
    }

    @Override
    public void initialize() {
	this.anyFcoOfType = FirstClassObjectFilterType.any;
	this.targetList = new LinkedList<>();
	this.vim = "";
	this.quiet = false;
	this.isDryRun = false;
    }

    protected boolean isDryRun() {
	return this.isDryRun;
    }

    protected boolean isQuiet() {
	return this.quiet;
    }

    private HashMap<String, VStorageObjectAssociations> retrieveVStorageObjectAssociations(
	    final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "retrieveVStorageObjectAssociations", connetionManager);
	final HashMap<String, VStorageObjectAssociations> result = new HashMap<>();
	final LinkedList<VStorageObjectAssociations> p = connetionManager.retrieveVStorageObjectAssociations();
	for (final VStorageObjectAssociations a : p) {
	    if (a.getVmDiskAssociations() == null) {
		continue;
	    }
	    for (final VStorageObjectAssociationsVmDiskAssociations associated : a.getVmDiskAssociations()) {
		result.put(associated.getVmId(), a);
	    }
	}
	logger.exiting(getClass().getName(), "retrieveVStorageObjectAssociations", result);
	return result;
    }

    private List<ID> searchIvdAssociation(final HashMap<String, VStorageObjectAssociations> vStorageObjectAssociations,
	    final VirtualMachineManager vmm) {
	logger.entering(getClass().getName(), "searchIvdAssociation", new Object[] { vStorageObjectAssociations, vmm });

	final List<ID> result = new LinkedList<>();
	if (vStorageObjectAssociations.containsKey(vmm.getMorefValue())) {
	    final ID id = vStorageObjectAssociations.get(vmm.getMorefValue()).getId();
	    final List<VStorageObjectAssociationsVmDiskAssociations> vmDiskAssociation = vStorageObjectAssociations
		    .get(vmm.getMorefValue()).getVmDiskAssociations();
	    for (final VStorageObjectAssociationsVmDiskAssociations disk : vmDiskAssociation) {
		if (disk.getVmId().equalsIgnoreCase(vmm.getMorefValue())) {
		    vmm.getVStorageObjectAssociations().put(disk.getDiskKey(), id);
		}
	    }

	    result.add(id);
	}
	logger.exiting(getClass().getName(), "searchIvdAssociation", result);
	return result;
    }

    protected void setDryRun(final boolean isDryRun) {
	this.isDryRun = isDryRun;
    }

    protected void setQuiet(final boolean quiet) {
	this.quiet = quiet;
    }

}
