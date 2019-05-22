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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Jvddk;
import com.vmware.vmbk.control.OperationResult;
import com.vmware.vmbk.control.OperationResult.Result;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.VmbkVersion;
import com.vmware.vmbk.control.info.GrandTotalDumpFileInfo;
import com.vmware.vmbk.control.info.TotalDumpFileInfo;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GenerationProfileSpec;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.vmbk.profile.ovf.SerializableVmConfigInfo;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.K8s;
import com.vmware.vmbk.type.K8sIvdComponent;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.SnapshotManager;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.VmdkInfo;
import com.vmware.vmbk.type.Manipulator.VmxManipulator;
import com.vmware.vmbk.util.Utility;

public class BackupCommand extends CommandSupportForSnapshot {
    private boolean compression;
    protected boolean isForce;
    protected boolean isNoVmdk;
    protected BackupMode mode;
    private ArrayList<TotalDumpFileInfo> totalDumpsInfo;
    protected String transportMode;

    protected BackupCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	int success = 0;
	int fails = 0;
	int skips = 0;
	int countVm = 0;
	int countIvd = 0;
	final int countVapp = 0;
	final StringBuilder result = new StringBuilder("type\tsuccess\tfails\tskips\t");
	result.append(VirtualMachineManager.headerToString());
	final GlobalProfile profAllFco = vmbk.getRepositoryTarget().initializeProfileAllFco();
	try {
	    IoFunction.showInfo(logger, this.toString());

	    final LinkedList<FirstClassObject> backupList = getFcoTarget(connetionManager, this.anyFcoOfType);
	    if ((backupList.size()) > 0) {
		for (final FirstClassObject fco : backupList) {
		    OperationResult opResult = new OperationResult(fco);
		    if (Vmbk.isAbortTriggered()) {
			IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
			opResult.skip();
		    } else {
			if (fco instanceof VirtualMachineManager) {
			    final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			    ++countVm;
			    opResult = action_BackupVm(vmbk, profAllFco, vmm);
			} else if (fco instanceof ImprovedVirtuaDisk) {
			    final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			    ++countIvd;
			    opResult = action_BackupIvd(vmbk, profAllFco, ivd);
			} else if (fco instanceof K8s) {
			    final K8s k8s = (K8s) fco;
			    // ++countIvd;
			    opResult = action_BackupK8s(vmbk, profAllFco, k8s);
			} else if (fco instanceof VirtualAppManager) {
			    final VirtualAppManager vApp = (VirtualAppManager) fco;
			    // ++countIvd;
			    opResult = action_BackupVApp(vmbk, profAllFco, vApp);
			}
		    }

		    result.append('\n');
		    result.append(fco.getType().toString());
		    switch (opResult.getResult()) {
		    case aborted:
		    case fails:
			++fails;
			result.append("\t \tx\t \t");
			break;
		    case dryruns:
		    case skips:
			++skips;
			result.append("\t \t \tx\t");
			break;
		    case success:
			result.append("\tx\t \t \t");
			++success;
			break;
		    }
		    result.append(fco.toString());
		}

	    } else {
		IoFunction.showWarning(logger, "No valid targets");

	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	} finally {
	    if ((success + fails) > 1) {
		final GrandTotalDumpFileInfo grandTotal = this.getTotalDumpInfo(success, fails, skips, countVm,
			countIvd, countVapp);
		IoFunction.println(grandTotal.separetorBar());
		IoFunction.showInfo(logger, result.toString());
		IoFunction.showInfo(logger, grandTotal.toString());
	    }
	}
	logger.exiting(getClass().getName(), "action");
    }

    private OperationResult action_BackupIvd(final Vmbk vmbk, final GlobalProfile profAllFco,
	    final ImprovedVirtuaDisk ivd) throws Exception {
	logger.entering(getClass().getName(), "action_BackupIvd", new Object[] { vmbk, ivd });

	OperationResult result = new OperationResult(ivd);

	profAllFco.addIvdEntry(ivd, Calendar.getInstance());

	vmbk.getRepositoryTarget().setFcoTarget(ivd);
	try {
	    final FcoArchiveManager vmArcMgr = new FcoArchiveManager(ivd, vmbk.getRepositoryTarget(),
		    ArchiveManagerMode.write);

	    result = backupIvd(ivd, vmArcMgr, GlobalConfiguration.getTransportMode(this));
	} catch (final Exception e) {
	    IoFunction.showWarning(logger, "backup of %s failed.", ivd.toString());
	    result.fails();
	    logger.warning(Utility.toString(e));
	} finally {
	    vmbk.getRepositoryTarget().postProfAllFco(profAllFco.toByteArrayInOutputStream());
	    IoFunction.showInfo(logger, "Backup %s", result.toString());
	    if (result.isFails()) {
		IoFunction.showInfo(logger, "Check logs for more details");
	    }
	    IoFunction.showInfo(logger, result.getTotalTimeString());
	}

	logger.exiting(getClass().getName(), "action_BackupIvd", result);
	return result;
    }

    private OperationResult action_BackupK8s(final Vmbk vmbk, final GlobalProfile profAllFco, final K8s k8s)
	    throws Exception {
	logger.entering(getClass().getName(), "action_BackupK8s", new Object[] { vmbk, k8s });

	final Calendar cal = Calendar.getInstance();
	profAllFco.addK8sEntry(k8s, cal);
	final ITarget target = vmbk.getRepositoryTarget();
	target.setFcoTarget(k8s);
	final FcoArchiveManager vmArcMgr = new FcoArchiveManager(k8s, vmbk.getRepositoryTarget(),
		ArchiveManagerMode.write);
	final GenerationProfileSpec spec = new GenerationProfileSpec(k8s, this);
	spec.setCalendar(cal);
	final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(spec);
	vmArcMgr.setTargetGeneration(profGen);

	target.setFcoTarget(k8s);
	target.setProfGen(profGen);
	target.initializemM5List();
	target.createGenerationFolder();
	target.postGenerationProfile();

	vmArcMgr.postProfileFco();
	final String uuid = k8s.createSnapshot();

	final List<K8sIvdComponent> k8sTargets = k8s.getSnapshotInfo(uuid);
	final OperationResult result = new OperationResult(k8s);
	try {
	    for (final K8sIvdComponent k8starget : k8sTargets) {
		OperationResult subResult = new OperationResult(k8starget.ivd);

		profAllFco.addIvdEntry(k8starget.ivd, cal);

		vmbk.getRepositoryTarget().setFcoTarget(k8starget.ivd);
		try {
		    final FcoArchiveManager vmArcMgrIvd = new FcoArchiveManager(k8starget.ivd,
			    vmbk.getRepositoryTarget(), ArchiveManagerMode.write);

		    subResult = backupIvd(k8starget.ivd, vmArcMgrIvd, GlobalConfiguration.getTransportMode(this),
			    k8starget.activeSnapshot);
		} catch (final Exception e) {
		    IoFunction.showWarning(logger, "backup of %s failed.", k8starget.ivd.toString());
		    subResult.fails();
		    logger.warning(Utility.toString(e));
		} finally {
		    IoFunction.showInfo(logger, "Backup %s %s\n", subResult.toString(), k8starget.ivd.toString());
		    vmbk.getRepositoryTarget().postProfAllFco(profAllFco.toByteArrayInOutputStream());

		}
		result.getSubOperations().add(subResult);
	    }
	} catch (final Exception e) {
	    IoFunction.showWarning(logger, "backup of %s failed.", k8s.toString());
	    result.fails();
	    logger.warning(Utility.toString(e));
	} finally {
	    vmbk.getRepositoryTarget().postProfAllFco(profAllFco.toByteArrayInOutputStream());
	    IoFunction.showInfo(logger, "Backup %s", result.toString());
	    if (result.isFails()) {
		IoFunction.showInfo(logger, "Check logs for more details");
	    }
	    IoFunction.showInfo(logger, result.getTotalTimeString());
	}

	logger.exiting(getClass().getName(), "action_BackupK8s", result);
	return result;

    }

    private OperationResult action_BackupVApp(final Vmbk vmbk, final GlobalProfile profAllFco,
	    final VirtualAppManager vApp) throws Exception {
	logger.entering(getClass().getName(), "action_BackupVApp", new Object[] { vmbk, vApp });
	final OperationResult result = new OperationResult(vApp);
	try {
	    final Calendar cal = Calendar.getInstance();
	    profAllFco.addVAppEntry(vApp, cal);
	    final ITarget target = vmbk.getRepositoryTarget();
	    target.setFcoTarget(vApp);
	    final FcoArchiveManager vmArcMgr = new FcoArchiveManager(vApp, vmbk.getRepositoryTarget(),
		    ArchiveManagerMode.write);
	    final GenerationProfileSpec spec = new GenerationProfileSpec(vApp, this);
	    spec.setCalendar(cal);
	    final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(spec);
	    vmArcMgr.setTargetGeneration(profGen);

	    target.setFcoTarget(vApp);
	    target.setProfGen(profGen);
	    target.initializemM5List();
	    target.createGenerationFolder();
	    target.postGenerationProfile();

	    vmArcMgr.postProfileFco();
	    if (vApp.isvAppConfigAvailable()) {
		final SerializableVAppConfigInfo vAppConfig = vApp.getVAppConfig();
		try (final ByteArrayInOutStream fos = new ByteArrayInOutStream();
			final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
		    oos.writeObject(vAppConfig);
		    if (!target.postvAppConfig(fos)) {
			result.fails();
		    }
		}
	    }

	    for (int index = 0; index < vApp.getVmList().size(); index++) {
		final OperationResult subResult = action_BackupVm(vmbk, profAllFco, vApp.getVmList().get(index));
		profGen.setChildSuccessfullGeneration(index, subResult.getGenerationId());
		result.getSubOperations().add(subResult);
	    }

	    target.setFcoTarget(vApp);
	    target.setProfGen(profGen);
	    vmArcMgr.getFcoProfile().setGenerationNotDependent(profGen.getGenerationId());
	    final boolean ret = vmArcMgr.finalizeBackup(result.getResult() == Result.success);
	    if (ret == false) {
		logger.warning("Backup finalization failed.");
	    }
	    target.postGenerationProfile();
	    if (target.postMd5()) {
		vmArcMgr.postProfileFco();
	    }
	} catch (final Exception e) {
	    IoFunction.showWarning(logger, "backup of %s failed.", vApp.toString());
	    result.fails();
	    logger.warning(Utility.toString(e));
	} finally {
	    vmbk.getRepositoryTarget().postProfAllFco(profAllFco.toByteArrayInOutputStream());
	    IoFunction.showInfo(logger, "Backup %s", result.toString());
	    if (result.isFails()) {
		IoFunction.showInfo(logger, "Check logs for more details");
	    }
	    IoFunction.showInfo(logger, result.getTotalTimeString());
	}
	logger.exiting(getClass().getName(), "action_BackupVApp", result);
	return result;

    }

    private OperationResult action_BackupVm(final Vmbk vmbk, final GlobalProfile profAllFco,
	    final VirtualMachineManager vmm) throws Exception {
	logger.entering(getClass().getName(), "action_BackupVm", new Object[] { vmbk, vmm });
	OperationResult result = new OperationResult(vmm);

	profAllFco.addVmEntry(vmm.getFcoInfo(), Calendar.getInstance(), vmm.getConfig().isTemplate());

	vmbk.getRepositoryTarget().setFcoTarget(vmm);
	try {
	    final FcoArchiveManager vmArcMgr = new FcoArchiveManager(vmm, vmbk.getRepositoryTarget(),
		    ArchiveManagerMode.write);

	    result = backupVm(vmm, vmArcMgr, profAllFco, GlobalConfiguration.getTransportMode(this));
	} catch (final Exception e) {
	    IoFunction.showWarning(logger, "backup of %s failed.", vmm.toString());
	    result.fails();
	    logger.warning(Utility.toString(e));
	} finally {
	    vmbk.getRepositoryTarget().postProfAllFco(profAllFco.toByteArrayInOutputStream());
	    IoFunction.showInfo(logger, "Backup %s", result.toString());
	    if (result.isFails()) {
		IoFunction.showInfo(logger, "Check logs for more details");
	    }
	    IoFunction.showInfo(logger, result.getTotalTimeString());
	}
	logger.exiting(getClass().getName(), "action_BackupVm", result);
	return result;
    }

    public void add(final TotalDumpFileInfo e) {

	this.totalDumpsInfo.add(e);
    }

    private boolean backupAttachedIvd(final VirtualMachineManager vmm, final ITarget target, final Calendar cal,
	    final Jvddk jvddk, final GenerationProfile profGen, final VmdkInfo vmdkInfo, final GlobalProfile profAllVm)
	    throws Exception {

	boolean ret = true;
	boolean targetDiskOpen = false;
	final BackupMode prevMode = this.getMode();
	try {
	    final int diskId = profGen.getDiskIdWithUuid(vmdkInfo.getUuid());

	    final ImprovedVirtuaDisk ivd = ((VimConnection) vmm.getConnection()).getVslmConnection()
		    .getIvdById(profGen.getImprovedVirtualDiskId(diskId));
	    IoFunction.showInfo(logger, "Controller:%d Disk [%d:%d]  %s is an Improved Virtual Disk",
		    vmdkInfo.getControllerKey(), vmdkInfo.getBusNumber(), vmdkInfo.getUnitNumber(),
		    ivd.toString().trim());
	    IoFunction.showInfo(logger, "Starting dump vmdk %s (Improved Virtual Disks disk)", vmdkInfo.getUuid());

	    profAllVm.addIvdEntry(ivd, cal);

	    target.saveStatus(vmm.getUuid());

	    target.setFcoTarget(ivd);
	    target.initializemM5List();
	    final FcoArchiveManager ivdArcMgr = new FcoArchiveManager(ivd, target, ArchiveManagerMode.write);
	    final GenerationProfileSpec spec = new GenerationProfileSpec(ivd, this);
	    spec.setCalendar(cal);
	    spec.setCbt(vmdkInfo.getChangeId());
	    // final GenerationProfile ivdProfGen = ivdArcMgr.prepareNewGeneration(ivd, cal,
	    // this, vmdkInfo.getChangeId());
	    final GenerationProfile ivdProfGen = ivdArcMgr.prepareNewGeneration(spec);
	    ivdArcMgr.setTargetGeneration(ivdProfGen);

	    target.setProfGen(ivdProfGen);

	    target.createGenerationFolder();
	    target.postGenerationProfile();

	    BackupMode tempMode = ivdArcMgr.determineBackupMode(this.getMode());

	    switch (tempMode) {
	    case full:
		tempMode = BackupMode.full;
		ivdArcMgr.getFcoProfile().setGenerationNotDependent(ivdProfGen.getGenerationId());
		break;
	    case incr:
		tempMode = BackupMode.incr;
		break;
	    case unknow:
		IoFunction.showWarning(logger, "Unable to detect the backup mode");

		return false;

	    }

	    this.setMode(tempMode);
	    target.setFcoTarget(vmm);
	    targetDiskOpen = target.open(diskId, this.isCompression());
	    ret = backupVmdk(ivdArcMgr, jvddk);
	    target.setFcoTarget(ivd);
	    IoFunction.showInfo(logger, "Dump vmdk %s %s.", vmdkInfo.getUuid(), (ret ? "succeeded" : "failed"));
	    ret = ivdArcMgr.finalizeBackup(ret);
	    if (ret == false) {
		logger.warning("Backup finalization failed.");
	    }
	    target.postGenerationProfile();
	    if (target.postMd5()) {
		ivdArcMgr.postProfileFco();
	    }

	} finally {
	    if (targetDiskOpen) {
		target.close();
		targetDiskOpen = false;
	    }

	    this.setMode(prevMode);
	    target.loadStatus(vmm.getUuid(), true);
	    target.setFcoTarget(vmm);
	    target.setProfGen(profGen);

	}
	return ret;
    }

    private OperationResult backupIvd(final ImprovedVirtuaDisk ivd, final FcoArchiveManager vmArcMgr,
	    final String transportModes) throws Exception {
	logger.entering(getClass().getName(), "backupIvd", new Object[] { ivd, vmArcMgr, transportModes });
	final OperationResult result = backupIvd(ivd, vmArcMgr, transportModes, null);
	logger.exiting(getClass().getName(), "backupIvd", result);
	return result;
    }

    /**
     * entry point for an IVD backup
     *
     * @param ivd
     * @param vmArcMgr
     * @param transportModes Transport mode to use during the backup
     * @param snap           if null a new snapsho is created
     * @return
     * @throws Exception
     */
    private OperationResult backupIvd(final ImprovedVirtuaDisk ivd, final FcoArchiveManager vmArcMgr,
	    final String transportModes, VStorageObjectSnapshotInfoVStorageObjectSnapshot snap) throws Exception {
	logger.entering(getClass().getName(), "backupIvd", new Object[] { ivd, vmArcMgr, transportModes, snap });
	final OperationResult result = new OperationResult(ivd);
	boolean removeSnapshotOnClose = false;
	final Calendar cal = Calendar.getInstance();
	boolean isAllVmdkDumpSucceeded = false;
	final VimConnection conn = (VimConnection) ivd.getConnection();
	final ITarget target = vmArcMgr.getRepositoryTarget();
	logger.info("backupIvd() start.");
	boolean ret;
	IoFunction.showInfo(logger, "Backup Improved Virtual Disk \"%s\"   uuid:%s start.", ivd.getName(),
		ivd.getUuid());

	if (this.isDryRun()) {
	    IoFunction.showInfo(logger, "Backup ends cause dryrun.");
	    result.dryruns();
	    return result;
	}
	try {
	    if (!ivd.isChangedBlockTrackingEnabled()) {
		if (this.isForce) {
		    IoFunction.showWarning(logger, "Change Tracking Block (CBT) disabled. -force option detected.");
		} else {
		    IoFunction.showWarning(logger,
			    "Change Tracking Block (CBT) disabled. Improved Virtual Disk Skipped. Enable CBT using: ivd -cbt on ivd:%s or use the option -force",
			    ivd.getUuid());
		    result.skip();
		    return result;
		}
	    }

	    if (snap == null) {
		/*
		 * Create snapshot
		 */
		removeSnapshotOnClose = true;
		IoFunction.showInfo(logger, "Starting creation of snapshot %s", generateSnapshotName(cal));
		snap = createSnapshot(ivd, cal);
		if (snap == null) {
		    result.fails();
		    return result;
		}
	    }
	    /*
	     * Make generation profile.
	     */
	    logger.info(String.format("snapshot: %s ID:%s Backing Object %s", snap.getDescription(), snap.getId(),
		    snap.getBackingObjectId()));

	    final GenerationProfileSpec spec = new GenerationProfileSpec(ivd, this);
	    spec.setCalendar(cal);

	    final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(spec);
	    vmArcMgr.setTargetGeneration(profGen);

	    target.setFcoTarget(ivd);
	    target.setProfGen(profGen);
	    target.initializemM5List();

	    target.createGenerationFolder();
	    target.postGenerationProfile();
	    vmArcMgr.postProfileFco();
	    final Jvddk jvddk = conn.configureVddkAccess(ivd, snap.getId());
	    try {
		isAllVmdkDumpSucceeded = true;

		BackupMode backupMode = BackupMode.incr;

		final BackupMode tempMode = vmArcMgr.determineBackupMode(this.getMode());
		switch (tempMode) {
		case full:
		    backupMode = BackupMode.full;
		    vmArcMgr.getFcoProfile().setGenerationNotDependent(profGen.getGenerationId());
		    break;
		case incr:
		    backupMode = BackupMode.incr;
		    break;
		case unknow:
		    IoFunction.showWarning(logger, "Unable to detect the backup mode");
		    result.fails();
		    return result;
		}

		this.setMode(backupMode);
		final Integer prevGenId = profGen.getPreviousGenerationId();
		IoFunction
			.showInfo(logger, "Generation Info: [number %d][%s][Uri %s]", profGen.getGenerationId(),
				(backupMode == BackupMode.full) || (prevGenId < 0) ? "no dependence"
					: "depend on " + prevGenId.toString(),
				target.getUri(profGen.getGenerationPath()));
		jvddk.prepareForAccess(VmbkVersion.getIdentity());
		if (jvddk.connect(true, transportModes)) {

		    try {
			target.open(this.isCompression());

			isAllVmdkDumpSucceeded = backupVmdk(vmArcMgr, jvddk);

			IoFunction.showInfo(logger, "Dump vmdk %s %s.", ivd.getUuid(),
				(isAllVmdkDumpSucceeded ? "succeeded" : "failed"));

		    } finally {
			target.close();
		    }

		}
	    } finally {
		jvddk.endAccess();
		jvddk.disconnect();
		jvddk.Cleanup();
	    }

	    final boolean vmdkDumpSucceeded = isAllVmdkDumpSucceeded;
	    ret = vmArcMgr.finalizeBackup(vmdkDumpSucceeded);
	    if (ret == false) {
		logger.warning("Backup finalization failed.");
	    }
	    target.postGenerationProfile();
	    if (target.postMd5()) {
		vmArcMgr.postProfileFco();
	    }
	} finally {
	    if ((snap != null) && removeSnapshotOnClose) {
		if (ivd.deleteSnapshot(snap)) {
		    IoFunction.showInfo(logger, "Delete snapshot ID: %s Desc: %s succeeded.", snap.getId().getId(),
			    snap.getDescription());
		} else {
		    IoFunction.showInfo(logger, "Delete snapshot ID: %s Desc: %s failed.", snap.getId().getId(),
			    snap.getDescription());
		}
	    }

	}
	IoFunction.showInfo(logger, "backupIvd() end.");
	logger.exiting(getClass().getName(), "backupIvd", isAllVmdkDumpSucceeded && ret);
	if (isAllVmdkDumpSucceeded && ret) {
	    result.success();

	} else {
	    result.fails();
	}
	return result;
    }

    private OperationResult backupVm(final VirtualMachineManager vmm, final FcoArchiveManager vmArcMgr,
	    final GlobalProfile profAllVm, final String transportModes) throws Exception {
	logger.entering(getClass().getName(), "backupVm", new Object[] { vmm, vmArcMgr });
	final OperationResult result = new OperationResult(vmm);
	BackupMode backupMode = getMode();// BackupMode.incr;
	boolean revertToTemplate = false;
	boolean isAllVmdkDumpSucceeded = false;
	final VimConnection conn = (VimConnection) vmm.getConnection();
	final ITarget target = vmArcMgr.getRepositoryTarget();
	logger.info("backupVm() start.");
	boolean ret = true;

	IoFunction.showInfo(logger, "Backup \"%s\" (%s) uuid:%s start.", vmm.getName(), vmm.getMorefValue(),
		vmm.getUuid());
	if (this.isDryRun()) {
	    IoFunction.showInfo(logger, "Backup ends cause dryrun.");
	    result.dryruns();
	    return result;
	}
	SnapshotManager snap = null;
	try {
	    if (!vmm.getHeaderGuestInfo()) {
		result.fails();
		return result;
	    }

	    if (!vmm.getConfig().isChangeTrackingEnabled()) {
		if (this.isForce) {
		    IoFunction.showWarning(logger, "Change Tracking Block (CBT) disabled. -force option detected.");
		} else {
		    IoFunction.showWarning(logger,
			    "Change Tracking Block (CBT) disabled. Virtual Machine Skipped. Enable CBT using: vm -cbt on vm:%s or use the option -force",
			    vmm.getMorefValue());
		    result.skip();
		    return result;
		}
	    }

	    if (vmm.getConfig().isTemplate()) {
		revertToTemplate = vmm.markAsVirtualMachine(conn.getDefaultResurcePool());
	    }
	    /*
	     * Create snapshot
	     */
	    final Calendar cal = Calendar.getInstance();
	    IoFunction.showInfo(logger, "Starting creation of snapshot %s", generateSnapshotName(cal));
	    snap = createSnapshot(vmm, cal);
	    if (snap == null) {
		result.fails();
		return result;
	    }
	    /*
	     * Make generation profile.
	     */
	    logger.info(String.format("snapshot: %s Moref: %s", snap.getName(), snap.getMoref().getValue()));
	    final List<VmdkInfo> vmdkInfoList = snap.getConfig().getAllVmdkInfo(vmm.getVStorageObjectAssociations());

	    final ManagedEntityInfo snapInfo = snap.getSnapInfo();
	    final GenerationProfileSpec spec = new GenerationProfileSpec(vmm, this);
	    spec.setCalendar(cal);
	    spec.getVmdkInfoList().addAll(vmdkInfoList);

	    // final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(vmm,
	    // vmdkInfoList, cal, this);
	    final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(spec);
	    vmArcMgr.setTargetGeneration(profGen);

	    target.setFcoTarget(vmm);
	    target.setProfGen(profGen);
	    target.initializemM5List();

	    target.createGenerationFolder();
	    target.postGenerationProfile();
	    vmArcMgr.postProfileFco();
	    Jvddk jvddk = null;

	    if (getVmMetadata(vmm, target)) {
		isAllVmdkDumpSucceeded = true;
		if (vmdkInfoList.size() > 0) {
		    try {
			vmdkLoop: {
			    for (final VmdkInfo vmdkInfo : vmdkInfoList) {
				final int diskId = profGen.getDiskIdWithUuid(vmdkInfo.getUuid());

				if (profGen.isIndipendentPersistentVirtualDisk(diskId)) {
				    // || profGen.isImprovedVirtualDisk(diskId)) {
				    continue;
				}
				final BackupMode tempMode = vmArcMgr.determineBackupMode(diskId, this.getMode());
				switch (tempMode) {
				case full:
				    backupMode = BackupMode.full;
				    vmArcMgr.getFcoProfile().setGenerationNotDependent(profGen.getGenerationId());
				    break vmdkLoop;
				case incr:
				    backupMode = BackupMode.incr;
				    break;
				case unknow:
				    IoFunction.showWarning(logger, "Unable to detect the backup mode");
				    result.fails();
				    return result;
				}
			    }
			}
			this.setMode(backupMode);
			final Integer prevGenId = profGen.getPreviousGenerationId();
			result.setGenerationId(profGen.getGenerationId());
			IoFunction.showInfo(logger, "Generation Info: [number %d][%s][Uri %s]",
				profGen.getGenerationId(),
				(backupMode == BackupMode.full) || (prevGenId < 0) ? "no dependence"
					: "depend on " + prevGenId.toString(),
				target.getUri(profGen.getGenerationPath()));
			jvddk = conn.configureVddkAccess(vmm.getMoref());
			jvddk.prepareForAccess(VmbkVersion.getIdentity());
			if (jvddk.connect(true, snapInfo.getMorefValue(), transportModes)) {
			    /*
			     * Dump each vmdk file.
			     */
			    for (final VmdkInfo vmdkInfo : vmdkInfoList) {
				boolean targetDiskOpen = false;
				final int diskId = profGen.getDiskIdWithUuid(vmdkInfo.getUuid());
				if (diskId < 0) {

				    logger.warning("diskId invalid.\n" + vmdkInfo.toString());
				    isAllVmdkDumpSucceeded = false;
				    continue;
				}

				if (this.isNoVmdk()) {
				    profGen.setVmdkdumpResult(diskId, true);
				} else {
				    if (profGen.isIndipendentPersistentVirtualDisk(diskId)) {
					IoFunction.showInfo(logger,
						"Dump vmdk %s skipped (Indipendent or Persistent Virtual Disks disk)",
						vmdkInfo.getUuid());
				    } else

				    if (profGen.isImprovedVirtualDisk(diskId)) {
					ret = backupAttachedIvd(vmm, target, cal, jvddk, profGen, vmdkInfo, profAllVm);

				    } else {
					try {
					    targetDiskOpen = target.open(diskId, this.isCompression());
					    ret = backupVmdk(vmArcMgr, jvddk);
					    IoFunction.showInfo(logger, "Dump vmdk %s %s.", vmdkInfo.getUuid(),
						    (ret ? "succeeded" : "failed"));
					} finally {
					    if (targetDiskOpen) {
						target.close();
						targetDiskOpen = false;
					    }
					}

					isAllVmdkDumpSucceeded &= ret;
				    }

				}
			    }
			}
		    } finally {
			jvddk.endAccess();
			jvddk.disconnect();
			jvddk.Cleanup();
		    }
		}
	    }
	    final boolean vmdkDumpSucceeded = isAllVmdkDumpSucceeded;
	    ret = vmArcMgr.finalizeBackup(vmdkDumpSucceeded);
	    if (ret == false) {
		logger.warning("Backup finalization failed.");
	    }
	    target.postGenerationProfile();
	    if (target.postMd5()) {
		vmArcMgr.postProfileFco();
	    }
	} finally {

	    if (snap != null) {
		final ManagedObjectReference snapToDelete = snap.getMoref();
		if (vmm.deleteSnapshot(snapToDelete, false, true)) {
		    IoFunction.showInfo(logger, "Delete snapshot %s succeeded.", snap.getName());
		} else {
		    IoFunction.showInfo(logger, "Delete snapshot %s failed.", snap.getName());
		}
	    }
	    if (revertToTemplate) {
		vmm.markAsTemplate();
	    }
	}
	IoFunction.showInfo(logger, "backupVm() end.");
	logger.exiting(getClass().getName(), "backupVm", isAllVmdkDumpSucceeded && ret);
	if (isAllVmdkDumpSucceeded && ret) {
	    result.success();

	} else {
	    result.fails();
	}
	return result;
    }

    private boolean backupVmdk(final FcoArchiveManager vmArcMgr, final Jvddk jvddk) {
	final ITarget target = vmArcMgr.getRepositoryTarget();
	boolean ret = false;
	final GenerationProfile profGen = vmArcMgr.getTargetGeneration();
	final int diskId = target.getDiskId();

	profGen.setDiskBackupMode(diskId, this.getMode());

	profGen.setDumpBeginTimestamp(diskId);

	IoFunction.showInfo(logger, "BackupInfo: [mode %s][transportMode %s]", this.getMode(),
		jvddk.getRequestedTransportModes());
	ret = jvddk.doDumpJava(this, vmArcMgr, target);

	profGen.setDumpEndTimestamp(diskId);
	profGen.setVmdkdumpResult(diskId, ret);

	return ret;
    }

    public BackupMode getMode() {
	return this.mode;
    }

    private GrandTotalDumpFileInfo getTotalDumpInfo(final int success, final int fails, final int skips,
	    final int countVm, final int countIvd, final int countVapp) {
	return new GrandTotalDumpFileInfo(success, fails, skips, countVm, countIvd, countVapp, this.totalDumpsInfo);
    }

    public String getTransportMode() {
	return this.transportMode;
    }

    private boolean getVmMetadata(final VirtualMachineManager vmm, final ITarget target) throws Exception {
	boolean result = true;
	final GenerationProfile profGen = target.getGenerationProfile();
	final ByteArrayInOutStream vmxByteArrayStreamContent = vmm.exportVmx(profGen);
	ByteArrayInOutStream nvRamByteArrayStreamContent = null;
	if (vmxByteArrayStreamContent != null) {
	    try (VmxManipulator vmxContent = new VmxManipulator(vmxByteArrayStreamContent.toByteArray())) {
		nvRamByteArrayStreamContent = vmm.exportNvram(profGen, vmxContent.getNvram());
		if (nvRamByteArrayStreamContent == null) {
		    IoFunction.showWarning(logger, "NVRAM export failed. %s not available.", vmxContent.getNvram());
		    result = false;
		}

	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
		result = false;
	    }
	} else {
	    IoFunction.showWarning(logger, "Virtual Machine VMX export failed.  ");
	    result = false;
	}
	if (result) {
	    if (target.postVmx(vmxByteArrayStreamContent)) {
		if (target.postNvRam(nvRamByteArrayStreamContent)) {
		    if (vmm.isvAppConfigAvailable()) {
			final SerializableVmConfigInfo vAppConfig = vmm.getvAppConfig();
			try (final ByteArrayInOutStream fos = new ByteArrayInOutStream();
				final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			    oos.writeObject(vAppConfig);

			    if (!target.postvAppConfig(fos)) {
				result = false;
			    }
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "backupVm", result);
	return result;
    }

    @Override
    public void initialize() {
	super.initialize();
	this.isNoVmdk = false;
	this.isForce = false;
	this.compression = false;
	this.mode = BackupMode.unknow;
	this.totalDumpsInfo = new ArrayList<>();
	this.transportMode = null;
    }

    public boolean isBackupMode(final BackupMode backupMode) {
	return this.mode == backupMode;
    }

    public boolean isCompression() {
	return this.compression;
    }

    public boolean isNoVmdk() {
	return this.isNoVmdk;
    }

    /**
     * @param enableCompression
     */
    public void setCompression(final boolean compression) {
	this.compression = compression;

    }

    public void setMode(final BackupMode mode) {
	this.mode = mode;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("BackupInfo: ");

	sb.append(String.format("[isNoVmdk %s]", PrettyBoolean.toString(this.isNoVmdk)));
	sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(isDryRun())));
	sb.append(String.format("[compression %s]", PrettyBoolean.toString(this.compression)));
	sb.append(String.format("[transportMode %s]", (this.transportMode == null) ? "default" : this.transportMode));
	sb.append(String.format("[mode %s]", (this.mode == BackupMode.unknow) ? "auto" : this.mode.toString()));
	if (this.isForce) {
	    sb.append("[force]");
	}

	return sb.toString();
    }

}
