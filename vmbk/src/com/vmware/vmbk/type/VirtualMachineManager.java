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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmProfileId;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.CannotCreateFileFaultMsg;
import com.vmware.vim25.CustomizationFaultFaultMsg;
import com.vmware.vim25.DVPortgroupConfigInfo;
import com.vmware.vim25.DatastoreCapability;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DeviceUnsupportedForVmVersionFaultMsg;
import com.vmware.vim25.DiskChangeExtent;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.FileAlreadyExistsFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.GuestOsDescriptorFirmwareType;
import com.vmware.vim25.ID;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidControllerFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.MissingControllerFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.OpaqueNetworkSummary;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualE1000E;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualEthernetCardOpaqueNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFlagInfo;
import com.vmware.vim25.VirtualMachineGuestQuiesceSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.VirtualMachineWindowsQuiesceSpec;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSriovEthernetCard;
import com.vmware.vim25.VirtualVmxnet;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.info.RestoreManagedInfo;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.ovf.SerializableVmConfigInfo;
import com.vmware.vmbk.soap.IConnection;
import com.vmware.vmbk.soap.PutVMFilesException;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.type.Manipulator.VmxManipulator;
import com.vmware.vmbk.util.Utility;

public class VirtualMachineManager implements FirstClassObject {

    private static final Logger logger = Logger.getLogger(VirtualMachineManager.class.getName());

// TODO Remove unused code found by UCDetector
//     public static boolean deleteDirectory(final BasicVimConnection vimConnection, final ManagedEntityInfo dcInfo,
// 	    final ManagedEntityInfo dsInfo, final String directory) {
//
// 	boolean result = true;
// 	try {
// 	    final DatastoreSummary dsSummary = (DatastoreSummary) vimConnection.morefHelper
// 		    .entityProps(dsInfo.getMoref(), "summary");
// 	    final String namespaceUrl = dsSummary.getUrl() + directory;
//
// 	    String datastorePath = vimConnection.getVimPort().convertNamespacePathToUuidPath(
// 		    vimConnection.getServiceContent().getDatastoreNamespaceManager(), dcInfo.getMoref(), namespaceUrl);
// 	    System.out.println("datastorePath=" + datastorePath);
// 	    datastorePath = datastorePath.substring(5);
// 	    vimConnection.getVimPort().deleteDirectory(vimConnection.getServiceContent().getDatastoreNamespaceManager(),
// 		    dcInfo.getMoref(), datastorePath);
// 	} catch (InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg | FileFaultFaultMsg | FileNotFoundFaultMsg
// 		| InvalidDatastorePathFaultMsg e) {
// 	    logger.warning(Utility.toString(e));
// 	    result = false;
// 	} catch (final Exception e) {
// 	    logger.warning(Utility.toString(e));
// 	    result = false;
// 	}
// 	return result;
//     }

    public static String headerToString() {
	return new VirtualMachineManager().headertoString();
    }

    private VirtualMachineConfigManager configMgr;
    private String cookieValue = "";
    private ManagedEntityInfo dataCenterInfo;
    private String name;
    private SnapshotManager snapshotManager;
    private ManagedFcoEntityInfo temporaryVmInfo;

    private final VimConnection vimConnection;

    private ManagedObjectReference vmMoref;

    private final Map<Integer, ID> vStorageObjectAssociations;

    private VirtualMachineManager() {
	this.vimConnection = null;
	this.vmMoref = null;
	this.name = null;
	this.configMgr = null;
	this.dataCenterInfo = null;
	this.snapshotManager = null;
	this.vStorageObjectAssociations = null;
    }

    public VirtualMachineManager(final VimConnection vimConnection, final ManagedFcoEntityInfo vmInfo) {
	this.vimConnection = vimConnection;
	this.vmMoref = null;
	this.name = null;
	this.configMgr = null;
	this.dataCenterInfo = null;
	this.snapshotManager = null;
	this.temporaryVmInfo = vmInfo;
	this.vStorageObjectAssociations = new HashMap<>();
    }

    public VirtualMachineManager(final VimConnection vimConnection, final ManagedObjectReference vmMoref) {
	this.vimConnection = vimConnection;
	this.vmMoref = vmMoref;
	try {
	    this.name = vimConnection.morefHelper.entityName(vmMoref).toString();
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}

	this.configMgr = new VirtualMachineConfigManager(this.vimConnection, vmMoref);
	this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
	this.snapshotManager = null;
	this.vStorageObjectAssociations = new HashMap<>();
    }

    public VirtualMachineManager(final VimConnection vimConnection, final String name,
	    final ManagedEntityInfo dataCenterInfo) {
	this.vimConnection = vimConnection;
	this.vmMoref = null;
	this.name = name;
	this.configMgr = null;
	this.dataCenterInfo = dataCenterInfo;
	this.snapshotManager = null;
	this.vStorageObjectAssociations = new HashMap<>();
    }

    public VirtualMachineManager(final VimConnection vimConnection, final String name,
	    final ManagedObjectReference vm) {
	this.vimConnection = vimConnection;
	this.vmMoref = vm;
	this.name = name;
	this.configMgr = new VirtualMachineConfigManager(this.vimConnection, vm);
	this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
	this.snapshotManager = null;
	this.vStorageObjectAssociations = new HashMap<>();
    }

    public VirtualMachineManager(final VimConnection vimConnection, final String name, final ManagedObjectReference vm,
	    final VirtualMachineConfigInfo configInfo) {
	this.vimConnection = vimConnection;
	this.vmMoref = vm;
	this.name = name;
	this.configMgr = new VirtualMachineConfigManager(this.vimConnection, vm, configInfo);
	this.dataCenterInfo = null;
	this.snapshotManager = null;
	this.vStorageObjectAssociations = new HashMap<>();

    }

    private boolean addEmptyDisks(final VirtualMachineConfigSpec vmConfigSpec,
	    final List<VirtualControllerManager> ctrlmList) {

	final List<VirtualDeviceConfigSpec> specList = new LinkedList<>();

	for (final VirtualControllerManager ctrlm : ctrlmList) {
	    /*
	     * create new device of the controller and all disks managed by it.
	     */
	    specList.addAll(ctrlm.createAll(this));
	}
	vmConfigSpec.getDeviceChange().clear();
	vmConfigSpec.getDeviceChange().addAll(specList);

	return true;
    }

    public boolean attachDisk(final ImprovedVirtuaDisk ivd, final Integer controllerKey, final Integer unitNumber) {
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().attachDiskTask(getMoref(),
		    ivd.getId(), getDatastoreInfo().getMoref(), controllerKey, unitNumber);
	    if (this.vimConnection.waitForTask(taskMor)) {
		IoFunction.showInfo(logger, "Attach Improved Virtual Disks %s to vm:%s  Datastore:%s succeed",
			ivd.getId().getId(), getName(), getDatastoreInfo().getName());

	    } else {
		IoFunction.showWarning(logger, "Attach Improved Virtual Disks %s to vm:%s  Datastore:%s failed",
			ivd.getId().getId(), getName(), getDatastoreInfo().getName());

	    }
	    return true;
	} catch (final FileFaultFaultMsg | InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg
		| DeviceUnsupportedForVmVersionFaultMsg | InvalidControllerFaultMsg | InvalidStateFaultMsg
		| MissingControllerFaultMsg | NotFoundFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg
		| InvalidCollectorVersionFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
    }

    public boolean clone(final String cloneName) {
	logger.entering(getClass().getName(), "cloneVM", cloneName);
	boolean result = false;
	final VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
	final VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
	cloneSpec.setLocation(relocSpec);
	cloneSpec.setPowerOn(false);
	cloneSpec.setTemplate(false);
	try {
	    IoFunction.showInfo(logger, "Cloning Virtual Machine [%s] to clone name [%s] %n", getName(), cloneName);
	    final ManagedObjectReference cloneTask = this.vimConnection.getVimPort().cloneVMTask(getMoref(),
		    getVmFolder().getMoref(), cloneName, cloneSpec);
	    if (this.vimConnection.waitForTask(cloneTask)) {
		IoFunction.showInfo(logger, "Successfully cloned Virtual Machine [%s] to clone name [%s]", getName(),
			cloneName);
		result = true;
	    } else {
		IoFunction.showWarning(logger, "Failure Cloning Virtual Machine [%s] to clone name [%s] ", getName(),
			cloneName);
	    }
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | CustomizationFaultFaultMsg | FileFaultFaultMsg
		| InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
		| MigrationFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg
		| InvalidCollectorVersionFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "cloneVM", result);
	return result;
    }

    private boolean createDirectory(final ManagedEntityInfo dsInfo, final ManagedEntityInfo dcInfo,
	    final String newVmName) {
	logger.entering(getClass().getName(), "createDirectory", new Object[] { dsInfo, dcInfo, newVmName });

	DatastoreCapability datastoreInfo = null;
	boolean result = false;
	try {
	    datastoreInfo = (DatastoreCapability) this.vimConnection.morefHelper.entityProps(dsInfo.getMoref(),
		    "capability");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	if (datastoreInfo != null) {
	    if (datastoreInfo.isTopLevelDirectoryCreateSupported()) {
		try {
		    this.vimConnection.getVimPort().makeDirectory(
			    this.vimConnection.getServiceContent().getFileManager(),
			    String.format("[%s] %s", dsInfo.getName(), newVmName), dcInfo.getMoref(), true);
		} catch (FileFaultFaultMsg | InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));
		    result = false;
		}
		result = true;
	    } else {
		try {
		    result = this.vimConnection.getVimPort().createDirectory(
			    this.vimConnection.getServiceContent().getDatastoreNamespaceManager(), dsInfo.getMoref(),
			    newVmName, "") != null;
		} catch (CannotCreateFileFaultMsg | InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));
		    result = false;
		} catch (final FileAlreadyExistsFaultMsg e) {
		    logger.warning(Utility.toString(e));
		    logger.info("Folder " + dsInfo.getName() + " already exist");
		    result = false;
		} catch (final Exception e) {
		    logger.warning(Utility.toString(e));
		    result = false;
		}
	    }
	}
	if (result) {
	    IoFunction.showInfo(logger, "Create Folder [%s] %s succeed", dsInfo.getName(), newVmName);
	} else {
	    IoFunction.showWarning(logger, "Create Folder [%s] %s failed", dsInfo.getName(), newVmName);
	}
	logger.exiting(getClass().getName(), "createDirectory", result);
	return result;
    }

    public ManagedObjectReference createSnapshot(final String snapName) {
	ManagedObjectReference snapMor = null;
	try {
	    ManagedObjectReference taskMor = null;
	    if (isGuestWindows()) {
		final VirtualMachineWindowsQuiesceSpec guestWinQuiesceSpec = new VirtualMachineWindowsQuiesceSpec();
		guestWinQuiesceSpec.setVssPartialFileSupport(false);
		guestWinQuiesceSpec.setTimeout(10);
		guestWinQuiesceSpec.setVssBootableSystemState(true);
		guestWinQuiesceSpec.setVssBackupContext("ctx_auto");

		guestWinQuiesceSpec.setVssBackupType(SnapshotManager.VSS_BT_FULL);

		taskMor = this.vimConnection.getVimPort().createSnapshotExTask(getMoref(), snapName, null, false,
			guestWinQuiesceSpec);

	    } else {
		final VirtualMachineGuestQuiesceSpec guestQuiesceSpec = new VirtualMachineGuestQuiesceSpec();
		guestQuiesceSpec.setTimeout(10);

		taskMor = this.vimConnection.getVimPort().createSnapshotExTask(getMoref(), snapName, null, false,
			guestQuiesceSpec);

	    }

	    assert taskMor != null;
	    if (this.vimConnection.waitForTask(taskMor)) {
		final TaskInfo taskInfo = (TaskInfo) this.vimConnection.morefHelper.entityProps(taskMor, "info");
		snapMor = (ManagedObjectReference) taskInfo.getResult();

		return snapMor;

	    }

	    return null;

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));

	    return null;
	}
    }

    public boolean createVMHomeDirectory(final RestoreManagedInfo managedInfo) {
	logger.entering(getClass().getName(), "createVMHomeDirectory", new Object[] { managedInfo });
	managedInfo.setDirectoryName(managedInfo.getName());
	boolean result = false;
	int iteraction = 0;
	while (true) {
	    result = createDirectory(managedInfo.getDsInfo(), managedInfo.getDcInfo(), managedInfo.getDirectoryName());
	    if (result) {
		break;
	    } else {
		IoFunction.showWarning(logger, "Tentative %d: Datastore folder: %s already exist", iteraction,
			managedInfo.getDirectoryName());
		managedInfo.setDirectoryName(String.format("%s_%d", managedInfo.getName(), ++iteraction));
		if (iteraction > 9) {
		    IoFunction.showWarning(logger, "Max Iteraction reached. Failed to ccreate a directory");
		    break;
		}
	    }
	}
	logger.exiting(getClass().getName(), "createVMHomeDirectory", result);
	return result;
    }

    public boolean deleteSnapshot(final ManagedObjectReference snap, final boolean removeChildren,
	    final boolean consolidate) {
	logger.entering(getClass().getName(), "deleteSnapshot", new Object[] { snap, removeChildren, consolidate });
	boolean result = false;
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().removeSnapshotTask(snap,
		    removeChildren, consolidate);
	    assert taskMor != null;
	    if (this.vimConnection.waitForTask(taskMor)) {
		result = true;
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));

	}
	logger.exiting(getClass().getName(), "deleteSnapshot", result);
	return result;
    }

    public boolean destroy() {
	logger.entering(getClass().getName(), "destroy");
	boolean result = false;
	if (exist()) {
	    try {
		// logger.info("Delete VM: " + getName());
		final ManagedObjectReference taskMor = this.vimConnection.getVimPort().destroyTask(getMoref());
		assert taskMor != null;
		if (this.vimConnection.waitForTask(taskMor)) {
		    result = true;
		}
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
		result = false;
	    } finally {
		this.vmMoref = null;
		this.configMgr = null;
	    }
	}
	logger.exiting(getClass().getName(), "destroy", result);
	return result;
    }

//    public boolean destroy() {
//	logger.entering(getClass().getName(), "destroy");
//	boolean result = false;
//	try {
//	    final ManagedObjectReference task = this.vimConnection.getVimPort().destroyTask(this.vmMoref);
//
//	    if (this.vimConnection.waitForTask(task)) {
//		result = true;
//	    }
//	} catch (final Exception e) {
//
//	    logger.warning(Utility.toString(e));
//	} finally {
//		this.vmMoref = null;
//		this.configMgr = null;
//	    }
//	logger.exiting(getClass().getName(), "destroy", result);
//	return result;
//
//    }

    public boolean detachDisk(final ImprovedVirtuaDisk ivd) {
	logger.entering(getClass().getName(), "detachDisk", ivd);
	boolean result = false;
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().detachDiskTask(getMoref(),
		    ivd.getId());
	    if (this.vimConnection.waitForTask(taskMor)) {
		IoFunction.showInfo(logger, "Detach Improved Virtual Disks %s from vm:%s  Datastore:%s succeed",
			ivd.getId().getId(), getName(), getDatastoreInfo().getName());
		result = true;
	    } else {
		IoFunction.showWarning(logger, "Detach Improved Virtual Disks %s from vm:%s  Datastore:%s failed",
			ivd.getId().getId(), getName(), getDatastoreInfo().getName());
		result = false;
	    }
	} catch (final FileFaultFaultMsg | RuntimeFaultFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
		| VmConfigFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "detachDisk", result);
	return result;
    }

    private ByteArrayInOutStream download(final String esxFile, final String dataStoreName, final String dataCenter) {
	logger.entering(getClass().getName(), "download", new Object[] { esxFile, dataStoreName, dataCenter });
	String serviceUrl = this.vimConnection.getUrl();
	serviceUrl = serviceUrl.substring(0, serviceUrl.lastIndexOf("sdk") - 1);

	String httpUrl = serviceUrl + "/folder/" + esxFile + "?dcPath=" + dataCenter + "&dsName=" + dataStoreName;
	httpUrl = httpUrl.replaceAll("\\ ", "%20");

	LoggerUtils.logInfo(logger, "Url: %s  downloading VM File: %s", httpUrl, esxFile);

	final ByteArrayInOutStream result = getData(httpUrl);
	logger.exiting(getClass().getName(), "download", result);
	return result;

    }

    private boolean exist() {
	return this.vmMoref != null;
    }

    public ByteArrayInOutStream exportNvram(final GenerationProfile profGen, final String nVram) {
	logger.entering(getClass().getName(), "exportNvram", new Object[] { profGen, nVram });
	ByteArrayInOutStream result = null;
	final String vmPahtName = this.configMgr.getVmPathName();
	if (vmPahtName != null) {
	    final String configurationDir = vmPahtName.substring(vmPahtName.indexOf("]") + 2,
		    vmPahtName.lastIndexOf("/"));
	    final String dataStoreName = vmPahtName.substring(vmPahtName.indexOf("[") + 1, vmPahtName.lastIndexOf("]"));

	    final String cm = configurationDir.concat("/").concat(nVram.replace("\"", ""));
	    logger.fine(String.format("vmDirectory: %s datacenter: %s file: %s", vmPahtName,
		    getDatacenterInfo().getName(), cm));
	    IoFunction.showInfo(logger, "Exporting NVRAM File: %s", cm);
	    result = download(cm, dataStoreName, getDatacenterInfo().getName());
	}
	logger.exiting(getClass().getName(), "exportNvram", result);
	return result;

    }

    public ByteArrayInOutStream exportVmx(final GenerationProfile profGen) {
	logger.entering(getClass().getName(), "exportVmx", new Object[] { profGen });
	ByteArrayInOutStream result = null;
	final String vmPahtName = this.configMgr.getVmPathName();
	if (vmPahtName != null) {

	    final String dataStoreName = vmPahtName.substring(vmPahtName.indexOf("[") + 1, vmPahtName.lastIndexOf("]"));

	    final String vmxFileLocation = vmPahtName.substring(vmPahtName.indexOf("]") + 2);
	    IoFunction.showInfo(logger, "Retrieving VMX File: %s", vmxFileLocation);
	    result = download(vmxFileLocation, dataStoreName, getDatacenterInfo().getName());
	}
	logger.exiting(getClass().getName(), "exportVmx", result);
	return result;
    }

    private boolean forceUpdate() {
	logger.entering(getClass().getName(), "forceUpdate");
	boolean result = false;
	this.configMgr = new VirtualMachineConfigManager(this.vimConnection, this.vmMoref);
	if (this.configMgr != null) {
	    this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
	    if (this.dataCenterInfo != null) {
		this.temporaryVmInfo = null;
		result = true;
	    }
	}
	logger.exiting(getClass().getName(), "forceUpdate", result);
	return result;
    }

    public List<String> getAllSnapshotNameList() {
	final VirtualMachineSnapshotInfo snapInfo = getSnapInfo();
	if (snapInfo == null) {
	    return null;
	}

	final VirtualMachineSnapshotTree[] snapTree = (VirtualMachineSnapshotTree[]) snapInfo.getRootSnapshotList()
		.toArray();

	final List<String> ret = getAllSnapshotNameList(snapTree);

	return ret;
    }

    private List<String> getAllSnapshotNameList(final VirtualMachineSnapshotTree[] snapTree) {
	final List<String> ret = new LinkedList<>();

	for (int i = 0; (snapTree != null) && (i < snapTree.length); i++) {
	    ret.add(snapTree[i].getName());
	    final VirtualMachineSnapshotTree[] childTree = (VirtualMachineSnapshotTree[]) snapTree[i]
		    .getChildSnapshotList().toArray();
	    if (childTree != null) {
		ret.addAll(getAllSnapshotNameList(childTree));
	    }
	}
	return ret;
    }

    public List<PbmProfileId> getAssociatedProfile() {

	try {
	    return this.vimConnection.getPbmConnection().getAssociatedProfile(this);
	} catch (PbmFaultFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
    }

    public List<PbmProfileId> getAssociatedProfile(final int key) {

	try {
	    return this.vimConnection.getPbmConnection().getAssociatedProfile(this, key);
	} catch (PbmFaultFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
    }

    public HashMap<String, ManagedObjectReference> getAvailableHostNetworks() {
	try {
	    final HashMap<String, ManagedObjectReference> networksAvailable = new HashMap<>();
	    final ArrayOfManagedObjectReference networks = (ArrayOfManagedObjectReference) this.vimConnection.morefHelper
		    .entityProps(this.getRuntimeInfo().getHost(), "network");

	    for (final ManagedObjectReference ds : networks.getManagedObjectReference()) {
		try {
		    final String st = this.vimConnection.morefHelper.entityProps(ds, "name").toString();
		    networksAvailable.put(st, ds);
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));
		}
	    }
	    return networksAvailable;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
    }

    public String getBiosUuid() {
	if (this.vmMoref == null) {
	    return "";
	}
	return this.configMgr.getVirtualMachineConfigInfo().getUuid();
    }

    public VirtualMachineConfigManager getConfig() {
	return this.configMgr;
    }

    @Override
    public IConnection getConnection() {
	return this.vimConnection;
    }

    public SnapshotManager getCurrentSnapshot() {
	try {
	    final VirtualMachineSnapshotInfo snapInfo = getSnapInfo();
	    if (snapInfo != null) {
		final ManagedObjectReference vmSnap = snapInfo.getCurrentSnapshot();
		if (vmSnap != null) {
		    return new SnapshotManager(this.vimConnection, this, snapInfo);
		}
	    }
	    return null;

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
    }

    private ByteArrayInOutStream getData(final String urlString) {
	InputStream in = null;
	try {

	    logger.info(String.format("Downloading VM File %s ", urlString));
	    HttpURLConnection conn = null;
	    final URL urlSt = new URL(urlString);
	    conn = (HttpURLConnection) urlSt.openConnection();
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setAllowUserInteraction(true);

	    this.cookieValue = this.vimConnection.getCookie();
	    final StringTokenizer tokenizer = new StringTokenizer(this.cookieValue, ";");
	    this.cookieValue = tokenizer.nextToken();
	    final String pathData = "$" + tokenizer.nextToken();
	    final String cookie = "$Version=\"1\"; " + this.cookieValue + "; " + pathData;

	    final Map<String, List<String>> map = new HashMap<>();
	    map.put("Cookie", Collections.singletonList(cookie));
	    ((BindingProvider) this.vimConnection.getVimPort()).getRequestContext()
		    .put(MessageContext.HTTP_REQUEST_HEADERS, map);

	    conn.setRequestProperty("Cookie", cookie);
	    conn.setRequestProperty("Content-Type", "application/octet-stream");
	    conn.setRequestProperty("Expect", "100-continue");
	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("Content-Length", "1024");
	    in = conn.getInputStream();
	    final ByteArrayInOutStream o = new ByteArrayInOutStream();
	    final int bufLen = 9 * 1024;
	    final byte[] buf = new byte[bufLen];
	    byte[] tmp = null;
	    int len = 0;
	    @SuppressWarnings("unused")
	    int bytesRead = 0;
	    while ((len = in.read(buf, 0, bufLen)) != -1) {
		bytesRead += len;
		tmp = new byte[len];
		System.arraycopy(buf, 0, tmp, 0, len);
		o.write(buf, 0, len);

	    }
	    return o;
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));

	    return null;
	} finally {
	    try {
		if (in != null) {
		    in.close();
		}
	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
	    }
	}
    }

    @Override
    public ManagedEntityInfo getDatacenterInfo() {
	if (this.dataCenterInfo == null) {
	    this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(getMoref());
	}
	return this.dataCenterInfo;
    }

    public ManagedEntityInfo getDatastoreInfo() {
	if (this.configMgr != null) {
	    final String vmPahtName = this.configMgr.getVmPathName();
	    if (vmPahtName != null) {

		final String name = vmPahtName.substring(vmPahtName.indexOf("[") + 1, vmPahtName.lastIndexOf("]"));
		final ManagedEntityInfo datastoreEntityInfo = this.vimConnection
			.getManagedEntityInfo(EntityType.Datastore, name);
		return datastoreEntityInfo;
	    } else {
		return null;
	    }
	}

	return null;
    }

    public String getEncryptionBundle() {
	String encryptionbundle = "";
	if (getConfig() != null) {
	    final List<OptionValue> extraConfig = getExtraConfig();
	    for (final OptionValue option : extraConfig) {
		if (option.getKey().equalsIgnoreCase("encryption.bundle")) {
		    encryptionbundle = option.getValue().toString();
		    break;
		}
	    }
	}
	return encryptionbundle;
    }

    @Override
    public EntityType getEntityType() {
	return EntityType.VirtualMachine;
    }

    public List<OptionValue> getExtraConfig() {
	if (getConfig() != null) {
	    return getConfig().getVirtualMachineConfigInfo().getExtraConfig();
	}
	return null;
    }

    @Override
    public ManagedFcoEntityInfo getFcoInfo() {
	if (this.temporaryVmInfo != null) {
	    return this.temporaryVmInfo;
	}
	return new ManagedFcoEntityInfo(getName(), getMoref(), getUuid(), this.vimConnection.getServerIntanceUuid());
    }

    public GuestOsDescriptorFirmwareType getFirmware() {
	if (getConfig() != null) {
	    final String firmware = getConfig().getVirtualMachineConfigInfo().getFirmware();
	    return GuestOsDescriptorFirmwareType.fromValue(firmware);
	}
	return GuestOsDescriptorFirmwareType.BIOS;
    }

    @Override
    public ArrayList<Block> getFullDiskAreas(final int diskId) {
	final int bytePerSector = 512;
	final List<VmdkInfo> vmdkInfoList = this.configMgr.getAllVmdkInfo();
	final VmdkInfo vmdkInfo = vmdkInfoList.get(diskId);

	final long capacityInBytes = vmdkInfo.getCapacityInBytes();
	final ArrayList<Block> vixBlocks = new ArrayList<>();
	final Block block = new Block();
	block.offset = 0;
	block.length = capacityInBytes / bytePerSector;
	vixBlocks.add(block);
	return vixBlocks;
    }

    public String getGuestFullName() {
	if (getConfig() != null) {
	    final String os = getConfig().getVirtualMachineConfigInfo().getGuestFullName();
	    return os;
	}
	return "";
    }

    public String getGuestId() {
	if (getConfig() != null) {
	    final String os = getConfig().getVirtualMachineConfigInfo().getGuestId();
	    return os;
	}
	return "";
    }

    public List<VirtualDevice> getHardwareDevices() {
	try {
	    return getConfig().getVirtualMachineConfigInfo().getHardware().getDevice();
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
    }

    public boolean getHeaderGuestInfo() {
	logger.entering(getClass().getName(), "getHeaderGuestInfo");

	final VirtualMachineFlagInfo flags = getConfig().getVirtualMachineConfigInfo().getFlags();

	if (!flags.isDiskUuidEnabled()) {
	    LoggerUtils.logWarning(logger, "Disk UUIDs are not exposed to the guest.");
	}
	final StringBuilder sum = new StringBuilder("GuestInfo:  ");

	if (getConfig().isTemplate()) {
	    sum.append("[templateVM true]");
	}
	if (flags.isVbsEnabled()) {

	    sum.append("[vbs true]");
	}
	if (isvAppConfigAvailable()) {
	    sum.append("[vApp config]");
	}
	if (isConfigurationEncrypted()) {

	    sum.append("[encrypted config]");
	}
	sum.append("[guestOs ");
	sum.append(getGuestFullName());
	sum.append("]");

	final int numberDisk = getConfig().countVirtualDisk();
	sum.append("[virtualDisk ");
	sum.append(numberDisk);
	sum.append("]");
	IoFunction.showInfo(logger, sum.toString());
	return true;
    }

    @Override
    public ManagedObjectReference getMoref() {
	if (this.vmMoref == null) {
	    return this.temporaryVmInfo.getMoref();
	}
	return this.vmMoref;
    }

    public String getMorefValue() {

	return getMoref().getValue();
    }

    @Override
    public String getName() {
	if (this.vmMoref == null) {
	    return this.temporaryVmInfo.getName();
	}
	return this.name;
    }

    public ManagedEntityInfo getResourcePool() {
	ManagedObjectReference mor = null;
	String name;
	try {
	    mor = (ManagedObjectReference) this.vimConnection.morefHelper.entityProps(getMoref(), "resourcePool");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	try {
	    name = this.vimConnection.morefHelper.entityName(mor);
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	final ManagedEntityInfo entity = new ManagedEntityInfo(name, mor, this.vimConnection.getServerIntanceUuid());
	return entity;
    }

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

    public VirtualMachineRuntimeInfo getRuntimeInfo() {
	try {
	    return (VirtualMachineRuntimeInfo) this.vimConnection.morefHelper.entityProps(this.vmMoref, "runtime");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}

    }

    @Override
    public String getServerUuid() {
	if (this.vimConnection != null) {
	    return this.vimConnection.getServerIntanceUuid();
	}
	return null;
    }

    public VirtualMachineSnapshotInfo getSnapInfo() {
	VirtualMachineSnapshotInfo snapInfo = null;
	try {
	    snapInfo = (VirtualMachineSnapshotInfo) this.vimConnection.morefHelper.entityProps(getMoref(), "snapshot");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	return snapInfo;
    }

    public SnapshotManager getSnapshotManager() {
	return this.snapshotManager;
    }

    @Override
    public FirstClassObjectType getType() {
	return FirstClassObjectType.vm;
    }

    @Override
    public String getUuid() {
	if (this.vmMoref == null) {
	    return this.temporaryVmInfo.getUuid();
	}
	return this.configMgr.getInstanceUuid();
    }

    public SerializableVmConfigInfo getvAppConfig() {
	if (isvAppConfigAvailable()) {
	    final SerializableVmConfigInfo vmConfigInfo = new SerializableVmConfigInfo(
		    getConfig().getVirtualMachineConfigInfo().getVAppConfig());
	    return vmConfigInfo;

	}
	return null;
    }

    public String getVcenterInstanceUuid() {
	return (this.vmMoref == null) ? this.temporaryVmInfo.getServerUuid()
		: this.vimConnection.getServerIntanceUuid();
    }

    public HashMap<String, ManagedObjectReference> getVirtualMachineNetworks() {
	final HashMap<String, ManagedObjectReference> vmNetworks = new HashMap<>();
	try {

	    final ArrayOfManagedObjectReference networks = (ArrayOfManagedObjectReference) this.vimConnection.morefHelper
		    .entityProps(this.getMoref(), "network");
	    for (final ManagedObjectReference network : networks.getManagedObjectReference()) {
		final String name = this.vimConnection.morefHelper.entityProps(network, "name").toString();
		vmNetworks.put(name, network);
	    }

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	return vmNetworks;
    }

    public ManagedEntityInfo getVmFolder() {
	ManagedObjectReference mor = null;
	String name = "";

	try {
	    mor = (ManagedObjectReference) this.vimConnection.morefHelper.entityProps(getMoref(), "parent");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	if (mor == null) {
	    return null;
	}
	try {
	    name = this.vimConnection.morefHelper.entityName(mor);
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	return new ManagedEntityInfo(name, mor, this.vimConnection.getServerIntanceUuid());
    }

// TODO Remove unused code found by UCDetector
//     public boolean hasVirtualStorageDisks() {
// 	return (this.vStorageObjectAssociations != null) && (this.vStorageObjectAssociations.size() > 0);
//     }

    public List<ManagedEntityInfo> getVmFolderPath() {
	final ArrayList<ManagedEntityInfo> listManagedEntityInfo = new ArrayList<>();
	ManagedObjectReference mor = getMoref();

	String name = "";
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

	    final ManagedEntityInfo entity = new ManagedEntityInfo(name, mor,
		    this.vimConnection.getServerIntanceUuid());
	    listManagedEntityInfo.add(entity);
	}
	Collections.reverse(listManagedEntityInfo);
	return listManagedEntityInfo;
    }

    public Map<Integer, ID> getVStorageObjectAssociations() {
	return this.vStorageObjectAssociations;
    }

    @Override
    public String headertoString() {
	return String.format("%-8s%-36s\t%-8s\t%-30s", "entity", "uuid", "moref", "name");
    }

    public boolean importNvram(final RestoreManagedInfo managedInfo, final byte[] byteArray) {
	final String location = String.format("[%s] %s/%s.nvram", managedInfo.getDsInfo().getName(),
		managedInfo.getDirectoryName(), managedInfo.getName());
	IoFunction.showInfo(logger, "Uploading NVRAM File: %s", location);
	final boolean ret = upload(location, byteArray);

	return ret;

    }

    public boolean importVmx(final RestoreManagedInfo managedInfo, final GenerationProfile profGen,
	    final byte[] byteArray) {
	boolean ret = false;

	try (final VmxManipulator newVmx = new VmxManipulator(byteArray)) {
	    if (managedInfo.isRecovery()) {
		newVmx.keepUuid();
	    } else {
		newVmx.prepareForRestore(managedInfo.getName());
	    }
	    newVmx.removeDisks(profGen);

	    final String location = String.format("[%s] %s/%s.vmx", managedInfo.getDsInfo().getName(),
		    managedInfo.getDirectoryName(), managedInfo.getName());
	    IoFunction.showInfo(logger, "Uploading VMX File: %s", location);
	    ret = upload(location, newVmx.getBytes());
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));
	}
	return ret;

    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.FirstClassObject#isChangeTrackingEnabled()
     */
    @Override
    public boolean isChangedBlockTrackingEnabled() {
	logger.entering(getClass().getName(), "isChangedBlockTrackingEnabled");
	if (this.configMgr != null) {
	    forceUpdate();
	}
	logger.exiting(getClass().getName(), "isChangedBlockTrackingEnabled", this.configMgr.isChangeTrackingEnabled());
	return this.configMgr.isChangeTrackingEnabled();
    }

    public boolean isConfigurationEncrypted() {
	if (getConfig() != null) {
	    if (getConfig().getVirtualMachineConfigInfo().getKeyId() != null) {
		return true;
	    }
	}
	return false;
    }

    public boolean isGuestWindows() {
	boolean windows = false;
	if (getConfig() != null) {
	    final String os = getConfig().getVirtualMachineConfigInfo().getGuestId();
	    windows = os.contains("windows");
	}
	return windows;
    }

    public boolean isInstanceUuidAvailable() {
	if (this.vmMoref == null) {
	    return this.temporaryVmInfo.getUuid() != null;
	}
	final String instanceUuid = this.configMgr.getInstanceUuid();
	return ((instanceUuid != null) && !instanceUuid.isEmpty());
    }

    public boolean isvAppConfigAvailable() {
	return (getConfig() != null) && (getConfig().getVirtualMachineConfigInfo() != null)
		&& (getConfig().getVirtualMachineConfigInfo().getVAppConfig() != null);
    }

    public boolean isVcenterInstanceUuidAvailable() {

	final String instanceUuid = (this.vmMoref == null) ? this.temporaryVmInfo.getServerUuid()
		: this.vimConnection.getServerIntanceUuid();
	return ((instanceUuid != null) && !instanceUuid.isEmpty());
    }

    @Override
    public boolean isVmDatastoreNfs() {
	final ManagedEntityInfo datastore = getDatastoreInfo();
	try {
	    final DatastoreSummary summary = (DatastoreSummary) this.vimConnection.morefHelper
		    .entityProps(datastore.getMoref(), "summary");

	    if (summary.getType().equalsIgnoreCase("nfs")) {
		return true;
	    }
	} catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	return false;
    }

    public boolean markAsTemplate() {
	try {
	    this.vimConnection.getVimPort().markAsTemplate(getMoref());
	} catch (FileFaultFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg | VmConfigFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
	forceUpdate();
	return true;
    }

    public boolean markAsVirtualMachine(ManagedEntityInfo rp) {
	if (getConfig().isTemplate()) {
	    try {
		final VirtualMachineSummary summary = (VirtualMachineSummary) this.vimConnection.morefHelper
			.entityProps(getMoref(), "summary");
		final ManagedObjectReference hostMoRef = summary.getRuntime().getHost();

		if (rp == null) {
		    final ManagedObjectReference rpMor = this.vimConnection.getResourcePoolByHost(hostMoRef);
		    rp = new ManagedEntityInfo(this.vimConnection.morefHelper.entityProps(rpMor, "name").toString(),
			    rpMor, this.vimConnection.getServerIntanceUuid());
		}

		this.vimConnection.getVimPort().markAsVirtualMachine(getMoref(), rp.getMoref(), hostMoRef);
	    } catch (FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg
		    | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg e) {
		logger.warning(Utility.toString(e));
		return false;
	    }

	    forceUpdate();
	    return true;
	}
	return false;
    }

    public boolean powerOff() {
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOffVMTask(getMoref());
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

    public boolean powerOn(final ManagedEntityInfo hostInfo) {

	try {
	    final ManagedObjectReference hostMoref = (hostInfo != null) ? hostInfo.getMoref() : null;
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOnVMTask(getMoref(), hostMoref);
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

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final FcoArchiveManager vmArcMgr, final int diskId,
	    final BackupMode mode) {
	final int bytePerSector = 512;
	try {
	    final List<VmdkInfo> vmdkInfoList = this.configMgr.getAllVmdkInfo();
	    final GenerationProfile profGen = vmArcMgr.getTargetGeneration();

	    final VmdkInfo vmdkInfo = vmdkInfoList.get(diskId);
	    final long capacityInBytes = vmdkInfo.getCapacity();
	    // assert (diskId == profGen.getDiskIdWithUuid(vmdkInfo.getUuid())) ;
	    DiskChangeInfo diskChangeInfo = null;
	    final ArrayList<Block> vixBlocks = new ArrayList<>();

	    long startPosition = 0;
	    String prevChanges = "";
	    if (mode == BackupMode.full) {
		prevChanges = "*";
	    } else {
		prevChanges = vmArcMgr.getPrevChangeId(diskId);
		if (prevChanges == null) {
		    prevChanges = "*";
		}
	    }
	    do {
		diskChangeInfo = this.vimConnection.queryChangedDiskAreas(this, vmdkInfo.getKey(), startPosition,
			prevChanges);
		// this.vimConnection.getVimPort().queryChangedDiskAreas(getMoref(),
		// this.snapshotManager.getMoref(), vmdkInfo.getKey(), startPosition,
		// prevChanges);
		if (diskChangeInfo == null) {
		    break;
		}
		for (final DiskChangeExtent changedArea : diskChangeInfo.getChangedArea()) {
		    final Block block = new Block();
		    block.length = changedArea.getLength() / bytePerSector;
		    block.offset = changedArea.getStart() / bytePerSector;
		    vixBlocks.add(block);
		}
		startPosition = diskChangeInfo.getStartOffset() + diskChangeInfo.getLength();
	    } while (startPosition < capacityInBytes);
	    profGen.setIsChanged(diskId, diskChangeInfo.getLength() != 0);
	    return vixBlocks;
	    // }

	    // return null;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}

    }

    public boolean rebootGuest() {

	try {
	    this.vimConnection.getVimPort().rebootGuest(getMoref());
	    return true;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
    }

    private boolean reconfigureNetwork(final VirtualMachineConfigSpec vmConfigSpec, final String[] networkMapping) {
	logger.entering(getClass().getName(), "reconfigureNetwork", new Object[] { vmConfigSpec, networkMapping });
	boolean result = false;
	try {
	    final List<VirtualDevice> listvd = getHardwareDevices();
	    if (listvd != null) {
		final HashMap<String, ManagedObjectReference> networksAvailable = getAvailableHostNetworks();
		if (networksAvailable != null) {
		    int index = 0;
		    for (final VirtualDevice virtualDevice : listvd) {
			final VirtualDeviceConfigSpec element = new VirtualDeviceConfigSpec();
			if ((virtualDevice instanceof VirtualE1000) || (virtualDevice instanceof VirtualE1000E)
				|| (virtualDevice instanceof VirtualPCNet32) || (virtualDevice instanceof VirtualVmxnet)
				|| (virtualDevice instanceof VirtualEthernetCard)) {
			    if (StringUtils.isEmpty(networkMapping[index])) {

				virtualDevice.setBacking(null);
				virtualDevice.getConnectable().setConnected(false);
				IoFunction.showWarning(logger,
					"No Network is avaialble. Set vmnic%d backing port to Null", index);
			    } else {
				if (networksAvailable.containsKey(networkMapping[index])) {
				    final ManagedObjectReference newNetwork = networksAvailable
					    .get(networkMapping[index]);
				    switch (newNetwork.getType()) {
				    case "Network":
					final VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
					nicBacking.setDeviceName(networkMapping[index]);
					virtualDevice.setBacking(nicBacking);
					break;
				    case "OpaqueNetwork":
					final OpaqueNetworkSummary opaqueNetworkSummary = (OpaqueNetworkSummary) this.vimConnection.morefHelper
						.entityProps(newNetwork, "summary");
					final VirtualEthernetCardOpaqueNetworkBackingInfo opaqueBack = new VirtualEthernetCardOpaqueNetworkBackingInfo();
					opaqueBack.setOpaqueNetworkId(opaqueNetworkSummary.getOpaqueNetworkId());
					opaqueBack.setOpaqueNetworkType(opaqueNetworkSummary.getOpaqueNetworkType());
					virtualDevice.setBacking(opaqueBack);
					break;
				    case "DistributedVirtualPortgroup":
					final DVPortgroupConfigInfo dvPortgroupConfigInfo = (DVPortgroupConfigInfo) this.vimConnection.morefHelper
						.entityProps(newNetwork, "config");
					final String uuid = (String) this.vimConnection.morefHelper.entityProps(
						dvPortgroupConfigInfo.getDistributedVirtualSwitch(), "uuid");
					final VirtualEthernetCardDistributedVirtualPortBackingInfo dvpBack = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
					final DistributedVirtualSwitchPortConnection dvsPortConnection = new DistributedVirtualSwitchPortConnection();
					dvsPortConnection.setPortgroupKey(dvPortgroupConfigInfo.getKey());
					dvsPortConnection.setSwitchUuid(uuid);
					dvpBack.setPort(dvsPortConnection);
					virtualDevice.setBacking(dvpBack);
					break;
				    }

				    IoFunction.showInfo(logger, "Reconfigure vmnic%d backing port to %s", index,
					    networkMapping[index]);
				}
				element.setDevice(virtualDevice);
				element.setOperation(VirtualDeviceConfigSpecOperation.EDIT);
				vmConfigSpec.getDeviceChange().add(element);
			    }
			    ++index;
			} else if (virtualDevice instanceof VirtualSriovEthernetCard) {
			    ++index;
			    if (StringUtils.isNotEmpty(networkMapping[index])) {
				IoFunction.showWarning(logger,
					"vmnic%d is backing an SRIOV port cannot be remapped to %s", index,
					networkMapping[index]);
			    }
			}

		    }
		}
	    }
	    result = true;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "reconfigureNetwork", result);
	return result;
    }

    public boolean reconfigureVm(final FcoArchiveManager vmArcMgr, final RestoreManagedInfo managedInfo) {
	logger.entering(getClass().getName(), "reconfigureVm", new Object[] { vmArcMgr, managedInfo });
	boolean result = false;

	final List<VirtualControllerManager> vcmList = vmArcMgr
		.generateVirtualControllerManagerList(managedInfo.getDsInfo().getName());
	logger.info(vmArcMgr.toStringVirtualControllerManagerList(vcmList));
	final VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

	addEmptyDisks(vmConfigSpec, vcmList);
	reconfigureNetwork(vmConfigSpec, managedInfo.networkMapping);

	if (managedInfo.getSpbmProfile() != null) {
	    vmConfigSpec.getVmProfile().addAll(managedInfo.getSpbmProfile());
	}

	if (managedInfo.getVAppConfig() != null) {
	    vmConfigSpec.setVAppConfig(managedInfo.getVAppConfig().toVmConfigInfo());
	}

	if (reconfigureVm(vmConfigSpec)) {
	    logger.info("Reconfigure Virtual Machine success");
	    if (vcmList.size() > 0) {
		int numDisks = 0;
		for (final VirtualControllerManager vcm : vcmList) {
		    numDisks += vcm.getNumOfDisks();
		}
		IoFunction.showInfo(logger, "add empty %d disks succeeded.", numDisks);
	    }
	    result = true;
	} else {
	    logger.warning("Fails: addEmptyDisks()");
	    result = false;
	}
	logger.exiting(getClass().getName(), "reconfigureVm", result);
	return result;
    }

    private boolean reconfigureVm(final VirtualMachineConfigSpec vmConfigSpec) {
	logger.entering(getClass().getName(), "reconfigureVm", vmConfigSpec);
	boolean result = false;

	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().reconfigVMTask(getMoref(),
		    vmConfigSpec);
	    if (this.vimConnection.waitForTask(taskMor)) {
		logger.fine("Success: reconfigureVm ");
		result = true;
	    } else {
		logger.warning("Fails: reconfigureVm ");
		result = false;
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "reconfigureVm", result);
	return result;
    }

    public void reload() {

	this.configMgr = new VirtualMachineConfigManager(this.vimConnection, getMoref());
    }

    public boolean reset() {

	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().resetVMTask(getMoref());
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

// TODO Remove unused code found by UCDetector
//     public VirtualMachineSnapshotTree searchSnapshotTreeWithMoref(final ManagedObjectReference snapmor) {
// 	final VirtualMachineSnapshotInfo snapInfo = getSnapInfo();
// 	final List<VirtualMachineSnapshotTree> listvmst = snapInfo.getRootSnapshotList();
//
// 	return searchSnapshotTreeWithMoref(listvmst, snapmor);
//
//     }

    public boolean revertToSnapshot(final ManagedObjectReference snapMor) throws Exception {
	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().revertToSnapshotTask(snapMor, null,
		    true);

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

    VirtualMachineSnapshotTree searchSnapshotTreeWithMoref(final List<VirtualMachineSnapshotTree> snapTree,
	    final ManagedObjectReference snapmor) {
	VirtualMachineSnapshotTree snapNode = null;
	if (snapTree == null) {
	    return null;
	}
	for (final VirtualMachineSnapshotTree node : snapTree) {

	    logger.info("Snapshot Name : " + node.getName());

	    if (MorefUtil.compare(snapmor, node.getSnapshot())) {
		return node;
	    } else {
		final List<VirtualMachineSnapshotTree> childTree = node.getChildSnapshotList();
		snapNode = searchSnapshotTreeWithMoref(childTree, snapmor);
	    }
	}
	return snapNode;

    }

    @Override
    public boolean setChangeBlockTracking(final boolean enable) {

	if ((this.configMgr.isChangeTrackingEnabled() && enable)
		|| (!this.configMgr.isChangeTrackingEnabled() && !enable)) {
	    return true;
	}
	final VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
	vmConfigSpec.setChangeTrackingEnabled(enable);

	try {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().reconfigVMTask(getMoref(),
		    vmConfigSpec);
	    if (this.vimConnection.waitForTask(taskMor)) {
		logger.info("Success: Change Tracking to " + (enable ? "Enable" : "Disable"));

	    } else {
		logger.warning("Fails: Change Tracking to " + (enable ? "Enable" : "Disable"));
		return false;
	    }
	    final VirtualMachineRuntimeInfo runtimeInfo = getRuntimeInfo();
	    if (runtimeInfo.getPowerState() != VirtualMachinePowerState.POWERED_ON) {

		final ManagedObjectReference snap = createSnapshot(enable ? "Enable" : "Disable" + " CBT");
		return deleteSnapshot(snap, false, true);
	    }
	    return true;

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
    }

    public void setSnapshotManager(final SnapshotManager snapshotManager) {
	this.snapshotManager = snapshotManager;
    }

    public boolean shutdownGuest() {

	try {
	    this.vimConnection.getVimPort().shutdownGuest(getMoref());
	    return true;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    return false;
	}
    }

    @Override
    public String toString() {
	String name = null;
	if (getName().length() < 30) {
	    name = getName();
	} else {
	    name = getName().substring(0, 27) + "...";
	}
	return String.format("%-8s%36s\t%-8s\t%-30s ", getEntityType().toString(true), getUuid(), getMorefValue(),
		name);
    }

    public boolean update(final ManagedObjectReference moRef) {
	logger.entering(getClass().getName(), "update", moRef);
	boolean result = false;
	if ((moRef != null) || (this.vmMoref != null)) {
	    this.vmMoref = moRef;

	    this.configMgr = new VirtualMachineConfigManager(this.vimConnection, moRef);
	    if (this.configMgr != null) {
		this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
		if (this.dataCenterInfo != null) {
		    this.temporaryVmInfo = null;
		    result = true;
		}
	    }
	}
	logger.exiting(getClass().getName(), "update", result);
	return result;
    }

    private boolean upload(final String vmPahtName, final byte[] byteArray) {
	String msg;
	final String dataStoreName = vmPahtName.substring(vmPahtName.indexOf("[") + 1, vmPahtName.lastIndexOf("]"));
	final String vmxFileLocation = vmPahtName.substring(vmPahtName.indexOf("]") + 2);
	String httpUrl = this.vimConnection.getURL().getProtocol() + "://" + this.vimConnection.getURL().getHost()
		+ "/folder" + (vmxFileLocation.startsWith("/") ? "" : "/") + vmxFileLocation + "?dcPath="
		+ getDatacenterInfo().getName() + "&dsName=" + dataStoreName;
	httpUrl = httpUrl.replaceAll("\\ ", "%20");

	logger.info(String.format("Uploading VM File %s ", httpUrl));

	final URL fileURL;
	final HttpURLConnection conn;
	try {
	    fileURL = new URL(httpUrl);
	    conn = (HttpURLConnection) fileURL.openConnection();
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setAllowUserInteraction(true);
	} catch (final MalformedURLException e) {
	    throw new PutVMFilesException(e);
	} catch (final IOException e) {
	    throw new PutVMFilesException(e);
	}

	this.cookieValue = this.vimConnection.getCookie();

	final StringTokenizer tokenizer = new StringTokenizer(this.cookieValue, ";");
	this.cookieValue = tokenizer.nextToken();
	final String pathData = "$" + tokenizer.nextToken();
	final String cookie = "$Version=\"1\"; " + this.cookieValue + "; " + pathData;

	final Map<String, List<String>> map = new HashMap<>();
	map.put("Cookie", Collections.singletonList(cookie));
	((BindingProvider) this.vimConnection.getVimPort()).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
		map);

	conn.setRequestProperty("Cookie", cookie);
	conn.setRequestProperty("Content-Type", "application/octet-stream");
	try {
	    conn.setRequestMethod("PUT");
	} catch (final ProtocolException e) {
	    throw new PutVMFilesException(e);
	}
	conn.setRequestProperty("Content-Length", "1024");

	final long fileLen = byteArray.length;
	msg = String.format("File size is  %d bytes\n", fileLen);
	logger.info(msg);

	conn.setChunkedStreamingMode(0);

	OutputStream out = null;
	InputStream in = null;
	try {
	    out = conn.getOutputStream();
	    in = new BufferedInputStream(new ByteArrayInputStream(byteArray));
	    final int bufLen = 9 * 1024;
	    final byte[] buf = new byte[bufLen];
	    byte[] tmp = null;
	    int len = 0;

	    while ((len = in.read(buf, 0, bufLen)) != -1) {
		tmp = new byte[len];
		System.arraycopy(buf, 0, tmp, 0, len);
		out.write(tmp, 0, len);

	    }
	} catch (final FileNotFoundException e) {
	    throw new PutVMFilesException(e);
	} catch (final IOException e) {
	    throw new PutVMFilesException(e);
	} finally {
	    try {
		if (in != null) {
		    in.close();
		}
		if (out != null) {
		    out.close();
		}
		conn.getResponseCode();
	    } catch (final IOException e) {
		throw new PutVMFilesException(e);
	    }
	    conn.disconnect();

	}
	return true;

    }

}
