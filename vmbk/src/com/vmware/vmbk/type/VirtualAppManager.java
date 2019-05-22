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
package com.vmware.vmbk.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VAppCloneSpec;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.vmbk.soap.IConnection;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.util.Utility;

public class VirtualAppManager implements FirstClassObject {
    private static final Logger logger = Logger.getLogger(VirtualAppManager.class.getName());

    private final VimConnection vimConnection;

    private ManagedObjectReference moref;
    private String name;

    private SerializableVAppConfigInfo vAppConfig;

    private ArrayList<VirtualMachineManager> vmList;

    private ResourceConfigSpec config;
    private ManagedFcoEntityInfo temporaryVmInfo;

    private ManagedObjectReference parentFolder;

    private ManagedObjectReference parentVApp;

    private ManagedEntityInfo dataCenterInfo;

    /**
     * @param vimConnection2
     * @param fcoInfo
     */
    public VirtualAppManager(final VimConnection vimConnection, final ManagedFcoEntityInfo fcoInfo) {
	this.vimConnection = vimConnection;
	this.moref = null;
	this.name = null;
	this.temporaryVmInfo = fcoInfo;
	this.parentVApp = null;
	this.parentFolder = null;
	this.config = null;
	this.vAppConfig = null;
	this.dataCenterInfo = null;
	this.vmList = new ArrayList<>();
    }

    /**
     * @param basicVimConnection
     * @param moref
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public VirtualAppManager(final VimConnection vimConnection, final ManagedObjectReference moref)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	this.vimConnection = vimConnection;
	this.moref = moref;
	this.temporaryVmInfo = null;
	this.vmList = new ArrayList<>();
	final Map<String, Object> entities = vimConnection.morefHelper.entityProps(moref,
		new String[] { "name", "vAppConfig", "vm", "config", "parentFolder", "parentVApp" });
	this.name = entities.get("name").toString();
	this.vAppConfig = new SerializableVAppConfigInfo((VAppConfigInfo) entities.get("vAppConfig"));
	final ArrayOfManagedObjectReference ManagedObjectReferences = (ArrayOfManagedObjectReference) entities
		.get("vm");
	this.config = (ResourceConfigSpec) entities.get("config");
	this.parentFolder = (ManagedObjectReference) entities.get("parentFolder");
	this.parentVApp = (ManagedObjectReference) entities.get("parentVApp");
	for (final ManagedObjectReference vmMor : ManagedObjectReferences.getManagedObjectReference()) {
	    final VirtualMachineManager vm = new VirtualMachineManager(vimConnection, vmMor);
	    this.vmList.add(vm);
	}
	this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.moref);

    }

    /**
     * @param conn
     * @param name2
     */
    public VirtualAppManager(final VimConnection vimConnection, final String name) {
	this.vimConnection = vimConnection;
	this.moref = null;
	this.temporaryVmInfo = null;
	this.vAppConfig = null;
	this.name = name;
	this.vmList = new ArrayList<>();
	this.config = null;
	this.parentFolder = null;
	this.parentVApp = null;
	this.dataCenterInfo = null;
    }

    public VirtualAppManager(final VimConnection vimConnection, final String name, final ManagedObjectReference moref,
	    final VAppConfigInfo vAppConfig) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	this.vimConnection = vimConnection;
	this.moref = moref;
	this.temporaryVmInfo = null;
	this.vAppConfig = new SerializableVAppConfigInfo(vAppConfig);
	this.name = name;
	this.vmList = new ArrayList<>();
	final Map<String, Object> entities = vimConnection.morefHelper.entityProps(moref,
		new String[] { "vm", "config", "parentFolder", "parentVApp" });

	final ArrayOfManagedObjectReference ManagedObjectReferences = (ArrayOfManagedObjectReference) entities
		.get("vm");
	this.config = (ResourceConfigSpec) entities.get("config");
	this.parentFolder = (ManagedObjectReference) entities.get("parentFolder");
	this.parentVApp = (ManagedObjectReference) entities.get("parentVApp");
	for (final ManagedObjectReference vmMor : ManagedObjectReferences.getManagedObjectReference()) {
	    final VirtualMachineManager vm = new VirtualMachineManager(vimConnection, vmMor);
	    this.vmList.add(vm);
	}
	this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.moref);
    }

    /**
     * Clone the VM
     *
     * @param cloneName
     * @param datastore
     * @return
     */
    public boolean clone(final String cloneName, final String datastore) {
	logger.entering(getClass().getName(), "clone", cloneName);
	boolean result = false;
	final VAppCloneSpec cloneSpec = new VAppCloneSpec();
	if (StringUtils.isNotEmpty(datastore)) {
	    cloneSpec.setLocation(this.vimConnection.getDatastoreByName(datastore).getMoref());
	} else {
	    if (getVmList().size() > 0) {
		cloneSpec.setLocation(getVmList().get(0).getDatastoreInfo().getMoref());
	    }
	}
	if (cloneSpec.getLocation() != null) {
	    try {
		IoFunction.showInfo(logger, "Cloning vApp [%s] to clone name [%s] %n", getName(), cloneName);
		final ManagedObjectReference cloneTask = this.vimConnection.getVimPort().cloneVAppTask(getMoref(),
			cloneName, getResourcePool().getMoref(), cloneSpec);
		if (this.vimConnection.waitForTask(cloneTask)) {
		    IoFunction.showInfo(logger, "Successfully cloned vApp [%s] to clone name [%s]", getName(),
			    cloneName);
		    result = true;
		} else {
		    IoFunction.showWarning(logger, "Failure Cloning vApp [%s] to clone name [%s] ", getName(),
			    cloneName);
		}
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | FileFaultFaultMsg
		    | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
		    | MigrationFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg
		    | InvalidCollectorVersionFaultMsg e) {
		logger.warning(Utility.toString(e));
	    }
	} else {
	    IoFunction.showWarning(logger, "Failure Cloning Datastore [%s] is invalid ", datastore);
	    result = false;
	}
	logger.exiting(getClass().getName(), "clone", result);
	return result;
    }

    /**
     * @return
     */
    public boolean destroy() {
	logger.entering(getClass().getName(), "destroy");
	boolean result = false;
	try {
	    final ManagedObjectReference task = this.vimConnection.getVimPort().destroyTask(this.moref);

	    if (this.vimConnection.waitForTask(task)) {
		result = true;
	    }
	} catch (final Exception e) {

	    logger.warning(Utility.toString(e));

	}
	logger.exiting(getClass().getName(), "destroy", result);
	return result;
    }

    public ResourceConfigSpec getConfig() {
	return this.config;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getConnection()
     */
    @Override
    public IConnection getConnection() {
	return this.vimConnection;
    }

    @Override
    public ManagedEntityInfo getDatacenterInfo() {
	logger.entering(getClass().getName(), "getDatacenterInfo");
	if (this.dataCenterInfo == null) {
	    this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.getMoref());
	}
	logger.exiting(getClass().getName(), "getDatacenterInfo", this.dataCenterInfo);
	return this.dataCenterInfo;
    }

    @Override
    public EntityType getEntityType() {
	return EntityType.VirtualApp;
    }

    @Override
    public ManagedFcoEntityInfo getFcoInfo() {
	if (this.temporaryVmInfo != null) {
	    return this.temporaryVmInfo;
	}
	return new ManagedFcoEntityInfo(getName(), getMoref(), getUuid(), this.vimConnection.getServerIntanceUuid());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getFullDiskAreas(int)
     */
    @Override
    public ArrayList<Block> getFullDiskAreas(final int diskId) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ManagedObjectReference getMoref() {
	if (this.moref == null) {
	    return this.temporaryVmInfo.getMoref();
	}
	return this.moref;
    }

    /**
     * @return
     */
    public String getMorefValue() {
	return this.moref.getValue();
    }

    @Override
    public String getName() {
	if (this.moref == null) {
	    return this.temporaryVmInfo.getName();
	}
	return this.name;
    }

    /**
     * @return
     */
    public VirtualMachinePowerState getPowerState() {
	VirtualMachinePowerState result = VirtualMachinePowerState.POWERED_OFF;

	for (final VirtualMachineManager vmm : this.vmList) {
	    final VirtualMachineRuntimeInfo runtimeInfo = vmm.getRuntimeInfo();
	    switch (runtimeInfo.getPowerState()) {
	    case POWERED_OFF:

		break;
	    case POWERED_ON:
		if ((result == VirtualMachinePowerState.POWERED_OFF)
			|| (result == VirtualMachinePowerState.SUSPENDED)) {
		    result = VirtualMachinePowerState.POWERED_ON;
		}
		break;
	    case SUSPENDED:
		if (result != VirtualMachinePowerState.POWERED_ON) {
		    result = VirtualMachinePowerState.SUSPENDED;
		}
		break;
	    default:
		break;

	    }

	}
	return result;
    }

    public ManagedEntityInfo getResourcePool() {
	// ArrayOfManagedObjectReference mor = null;
	// final ManagedObjectReference mor = getMoref();
	// final String name = null;
//	try {
//	    mor = (ArrayOfManagedObjectReference) this.vimConnection.morefHelper.entityProps(getMoref(),
//		    "resourcePool");
//	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
//	    logger.warning(Utility.toString(e));
//	    return null;
//	}
//	try {
//	    name = this.vimConnection.morefHelper.entityName(mor);
//	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
//	    logger.warning(Utility.toString(e));
//	    return null;
//	}
	final ManagedEntityInfo entity = new ManagedEntityInfo(getName(), getMoref(),
		this.vimConnection.getServerIntanceUuid());
	return entity;
    }

    /**
     * @return
     */
    public ArrayList<ManagedEntityInfo> getResourcePoolPath() {
	ManagedEntityInfo entity = getResourcePool();
	final ArrayList<ManagedEntityInfo> listManagedEntityInfo = new ArrayList<>();
	ManagedObjectReference mor = entity.getMoref();
	listManagedEntityInfo.add(entity);
	String name;
	while (true) {
	    try {
		mor = (ManagedObjectReference) this.vimConnection.morefHelper.entityProps(mor, "parent");
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		return null;
	    }
	    if (mor == null) {
		break;
	    }
	    try {
		name = this.vimConnection.morefHelper.entityName(mor);
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		return null;
	    }
	    entity = new ManagedEntityInfo(name, mor, this.vimConnection.getServerIntanceUuid());
	    listManagedEntityInfo.add(entity);
	}
	Collections.reverse(listManagedEntityInfo);
	return listManagedEntityInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getServerUuid()
     */
    @Override
    public String getServerUuid() {
	return this.vimConnection.getServerIntanceUuid();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getType()
     */
    @Override
    public FirstClassObjectType getType() {
	return FirstClassObjectType.vapp;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getUuid()
     */
    @Override
    public String getUuid() {
	if (this.moref == null) {
	    return this.temporaryVmInfo.getUuid();
	}
	return this.vAppConfig.getInstanceUuid();
    }

//    public VAppConfigInfo getvAppConfig() {
//	return this.vAppConfig;
//    }

    public SerializableVAppConfigInfo getVAppConfig() {
	return this.vAppConfig;
    }

    public VAppConfigSpec getVAppConfigSpec() {
	return this.vAppConfig.toVAppConfigSpec();
    }

    public String getVcenterInstanceUuid() {
	return (this.moref == null) ? this.temporaryVmInfo.getServerUuid() : this.vimConnection.getServerIntanceUuid();
    }

    /**
     * @return
     */
    public List<ManagedEntityInfo> getVmFolderPath() {
	final ArrayList<ManagedEntityInfo> listManagedEntityInfo = new ArrayList<>();
	ManagedObjectReference mor = this.parentFolder;

	String name = "";
	while (true) {
	    if (mor == null) {
		break;
	    }
	    try {
		name = this.vimConnection.morefHelper.entityName(mor);
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		return null;
	    }

	    final ManagedEntityInfo entity = new ManagedEntityInfo(name, mor,
		    this.vimConnection.getServerIntanceUuid());
	    listManagedEntityInfo.add(entity);
	    try {
		mor = (ManagedObjectReference) this.vimConnection.morefHelper.entityProps(mor, "parent");
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		return null;
	    }
	}
	Collections.reverse(listManagedEntityInfo);
	return listManagedEntityInfo;
    }

    public ArrayList<VirtualMachineManager> getVmList() {
	return this.vmList;
    }

    @Override
    public String headertoString() {
	return String.format("%-8s%-36s\t%-8s\t%-30s", "entity", "uuid", "moref", "name");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#isChangedBlockTrackingEnabled()
     */
    @Override
    public boolean isChangedBlockTrackingEnabled() {
	boolean result = true;
	for (final VirtualMachineManager vm : this.vmList) {
	    result &= vm.isChangedBlockTrackingEnabled();
	}
	return result;
    }

    /**
     * @return
     */
    public boolean isChangeTrackingEnabled() {
	boolean result = this.vmList.size() > 0;
	for (final VirtualMachineManager vm : this.vmList) {
	    result &= vm.isChangedBlockTrackingEnabled();
	}
	return result;
    }

    /**
     * @return
     */
    public boolean isvAppConfigAvailable() {
	return this.vAppConfig != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#isVmDatastoreNfs()
     */
    @Override
    public boolean isVmDatastoreNfs() {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * Stops this vApp. The virtual machines (or child vApps) will be stopped in the
     * order specified in the vApp configuration, if force is false. If force is set
     * to true, all virtual machines are powered-off (in no specific order and
     * possibly in parallel) regardless of the vApp auto-start configuration.
     *
     * @param force
     * @return
     */
    public boolean powerOff(final boolean force) {
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOffVAppTask(getMoref(), force);
	    assert taskMor != null;
	    if (this.vimConnection.waitForTask(taskMor)) {
		return true;
	    } else {
		return false;
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
    }

    /**
     * Starts this vApp. The virtual machines (or sub vApps) will be started in the
     * order specified in the vApp configuration. If the vApp is suspended (@see
     * vim.VirtualApp.Summary.suspended), all suspended virtual machines will be
     * powered-on based on the defined start-up order. While a vApp is starting, all
     * power operations performed on sub entities are disabled through the VIM API.
     * They will throw TaskInProgress. In case of a failure to power-on a virtual
     * machine, the exception from the virtual machine power on is returned, and the
     * power-on sequence will be terminated. In case of a failure, virtual machines
     * that are already started will remain powered-on.
     *
     * @return
     */
    public boolean powerOn() {
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOnVAppTask(getMoref());
	    assert taskMor != null;
	    if (this.vimConnection.waitForTask(taskMor)) {
		return true;
	    } else {
		return false;
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vmware.vmbk.type.FirstClassObject#queryChangedDiskAreas(com.vmware.vmbk.
     * control.FcoArchiveManager, int, com.vmware.vmbk.type.BackupMode)
     */
    @Override
    public ArrayList<Block> queryChangedDiskAreas(final FcoArchiveManager vmArcMgr, final int diskId,
	    final BackupMode mode) {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#setChangeBlockTracking(boolean)
     */
    @Override
    public boolean setChangeBlockTracking(final boolean enable) {
	boolean result = true;
	for (final VirtualMachineManager vm : this.vmList) {
	    result &= vm.setChangeBlockTracking(enable);
	}
	return result;
    }

    @Override
    public String toString() {
	String name = null;
	if (getName().length() < 30) {
	    name = getName();
	} else {
	    name = getName().substring(0, 27) + "...";
	}
	return String.format("%-8s%36s\t%-8s\t%-30s ", getEntityType().toString(true), getUuid(), getMoref().getValue(),
		name);
    }

    /**
     * @param moref
     * @return
     */
    public boolean update(final ManagedObjectReference moref) {
	boolean result = true;
	try {
	    this.moref = moref;
	    this.temporaryVmInfo = null;
	    this.vmList = new ArrayList<>();
	    final Map<String, Object> entities = this.vimConnection.morefHelper.entityProps(moref,
		    new String[] { "name", "vAppConfig", "vm", "config", "parentFolder", "parentVApp" });
	    this.name = entities.get("name").toString();

	    this.vAppConfig = new SerializableVAppConfigInfo((VAppConfigInfo) entities.get("vAppConfig"));

	    final ArrayOfManagedObjectReference ManagedObjectReferences = (ArrayOfManagedObjectReference) entities
		    .get("vm");
	    this.config = (ResourceConfigSpec) entities.get("config");
	    this.parentFolder = (ManagedObjectReference) entities.get("parentFolder");
	    this.parentVApp = (ManagedObjectReference) entities.get("parentVApp");
	    for (final ManagedObjectReference vmMor : ManagedObjectReferences.getManagedObjectReference()) {
		final VirtualMachineManager vm = new VirtualMachineManager(this.vimConnection, vmMor);
		this.vmList.add(vm);
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}

	return result;
    }

    public boolean updateVAppConfig(final SerializableVAppConfigInfo myVAppConfigInfo) {
	boolean result = true;
	try {
	    this.vimConnection.getVimPort().updateVAppConfig(getMoref(), myVAppConfigInfo.toVAppConfigSpec());
	} catch (ConcurrentAccessFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg
		| InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidNameFaultMsg
		| InvalidStateFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	return result;
    }
}
