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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Jvddk;
import com.vmware.vmbk.control.OperationResult;
import com.vmware.vmbk.control.VmbkVersion;
import com.vmware.vmbk.control.info.RestoreManagedInfo;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.profile.ovf.SerializableVmConfigInfo;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.SnapshotManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.VmdkInfo;
import com.vmware.vmbk.util.Utility;

public class RestoreVm {
    protected static Logger logger = Logger.getLogger("Command");
    RestoreOptions options;
    VimConnection vimConnection;

    public RestoreVm(final VimConnection vimConnection, final RestoreOptions options) {
	this.options = options;
	this.vimConnection = vimConnection;
    }

    protected VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtuaDisk ivd,
	    final Calendar cal) {
	final String snapName = generateSnapshotName(cal);
	return createSnapshot(ivd, snapName);
    }

    protected VStorageObjectSnapshotInfoVStorageObjectSnapshot createSnapshot(final ImprovedVirtuaDisk ivd,
	    final String snapName) {
	if (ivd.createSnapshot(snapName)) {
	    IoFunction.showInfo(logger, "Snapshot name %s created succesfully.", snapName);

	} else {
	    IoFunction.showInfo(logger, "Snapshot creation task failed.");
	    return null;
	}
	return ivd.getCurrentSnapshot();
    }

    protected SnapshotManager createSnapshot(final VirtualMachineManager vmm, final Calendar cal) {
	final String snapName = generateSnapshotName(cal);
	return createSnapshot(vmm, snapName);
    }

    protected SnapshotManager createSnapshot(final VirtualMachineManager vmm, final String snapName) {

	final ManagedObjectReference snapMoref = vmm.createSnapshot(snapName);
	if (snapMoref == null) {
	    IoFunction.showInfo(logger, "Snapshot creation task failed.");
	    return null;
	} else {
	    IoFunction.showInfo(logger, "Snapshot name %s (%s) created succesfully.", snapName, snapMoref.getValue());
	}

	final SnapshotManager snap = vmm.getCurrentSnapshot();
	if (snap == null) {
	    return null;
	}
	if (snapName.equals(snap.getName())) {
	    return snap;
	} else {
	    return null;
	}

    }

    protected String generateSnapshotName(final Calendar cal) {
	String snapName = null;
	snapName = String.format("VMBK_%d-%02d-%02d_%02d:%02d:%02d", cal.get(Calendar.YEAR),
		cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
		cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	return snapName;
    }

    private RestoreManagedInfo getRestoreManagedInfo(final FcoArchiveManager vmArcMgr)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	logger.entering(getClass().getName(), "getRestoreManagedInfo");
	final RestoreManagedInfo managedInfo = new RestoreManagedInfo(
		GlobalConfiguration.getMaxNumberOfVirtaulMachineNetworkCard());
	final GenerationProfile profGen = vmArcMgr.getTargetGeneration();

	managedInfo.networkMapping = this.options.getVmNetworksName();

	final int numberOfNetworksOnProfile = profGen.getNumberOfNetworks();
	for (int networkId = 0; (networkId < GlobalConfiguration.getMaxNumberOfVirtaulMachineNetworkCard())
		&& (networkId < numberOfNetworksOnProfile); networkId++) {
	    if (StringUtils.isEmpty(managedInfo.networkMapping[networkId])) {
		managedInfo.networkMapping[networkId] = profGen.getNetworkName(networkId);
	    }
	}
//	if (this.options.isParentAVApp()) {
//
//	    managedInfo.setRpInfo(this.options.getParent().getFcoInfo());
//	    managedInfo.setFolderInfo(null);
//	    if (StringUtils.isEmpty(this.options.getDatastoreName())) {
//		final String originalDatastore = profGen.getOriginalDatastore();
//		if ((originalDatastore != null) & !originalDatastore.isEmpty()) {
//
//		    managedInfo.setDsInfo(
//			    this.vimConnection.getManagedEntityInfo(EntityType.Datastore, originalDatastore));
//
//		    if (managedInfo.getDsInfo() == null) {
//			IoFunction.showWarning(logger, "Original datastore %s doesn't exist", originalDatastore);
//		    }
//		}
//	    }
//	    // managedInfo.setDcInfo(this.vimConnection.getDatacenterByMoref(this.options.getParent().getMoref()));
//	} else
	{

	    if (this.options.isRecover()) {
		managedInfo.setName(profGen.getName());
		managedInfo.setRecovery(true);
		logger.info(String.format("Use default name for new VM: %s.", managedInfo.getName()));
	    } else if (this.options.getNewName() == null) {
		IoFunction.showWarning(logger, "Could not decide the name of new VM.");
		return null;
	    } else {
		managedInfo.setName(this.options.getNewName());
	    }

	    if (StringUtils.isEmpty(this.options.getFolderName())) {
		String vmFolder = profGen.getOriginalFolderPath();
		if (StringUtils.isNotEmpty(vmFolder)) {
		    if (StringUtils.isNotEmpty(this.options.getVmFolderFilter())
			    && !vmFolder.contains(this.options.getVmFolderFilter())) {
			IoFunction.showWarning(logger,
				"Original  vmFolder(%s) cannot be used because is not subfolder of vmFilter(%s). vmFilter will be used instead ",
				vmFolder, this.options.getVmFolderFilter());
			vmFolder = this.options.getVmFolderFilter();
		    }

		    ManagedObjectReference folder = this.vimConnection.findByInventoryPath(vmFolder);

		    if ((folder == null) && StringUtils.isNotEmpty(this.options.getVmFolderFilter())
			    && (vmFolder != this.options.getVmFolderFilter())) {
			IoFunction.showWarning(logger, "vmFolder(%s) doesn't exist vmFilter(%s) will be used instead",
				vmFolder, this.options.getVmFolderFilter());
			vmFolder = this.options.getVmFolderFilter();
			folder = this.vimConnection.findByInventoryPath(vmFolder);
		    }
		    if (folder != null) {
			managedInfo
				.setFolderInfo(new ManagedEntityInfo(vmFolder.substring(vmFolder.lastIndexOf('/') + 1),
					folder, this.vimConnection.getServerIntanceUuid()));
		    }
		}
	    }
	    if (StringUtils.isEmpty(this.options.getResourcePoolName())) {
		String originalResourcePoolName = profGen.getOriginalResourcePoolPath();
		if (StringUtils.isNotEmpty(originalResourcePoolName)) {
		    if (StringUtils.isNotEmpty(this.options.getResPoolFilter())
			    && !originalResourcePoolName.contains(this.options.getResPoolFilter())) {
			IoFunction.showWarning(logger,
				"Original ResourcePool(%s) cannot be used because is not subfolder of rpFilter(%s). rpFilter will be used instead ",
				originalResourcePoolName, this.options.getResPoolFilter());
			originalResourcePoolName = this.options.getResPoolFilter();
		    }
		    ManagedObjectReference rp = this.vimConnection.findByInventoryPath(originalResourcePoolName);
		    if (rp == null) {
			IoFunction.showWarning(logger,
				"ResourcePool(%s) doesn't exist rpFilter(%s) will be used instead",
				originalResourcePoolName, this.options.getResPoolFilter());
			originalResourcePoolName = this.options.getResPoolFilter();
			rp = this.vimConnection.findByInventoryPath(originalResourcePoolName);
		    }
		    if (rp != null) {
			managedInfo.setRpInfo(new ManagedEntityInfo(
				originalResourcePoolName.substring(originalResourcePoolName.lastIndexOf('/') + 1), rp,
				this.vimConnection.getServerIntanceUuid()));
		    }
		} else {
		    originalResourcePoolName = GlobalConfiguration.getRpFilter();
		    final ManagedObjectReference rp = this.vimConnection.findByInventoryPath(originalResourcePoolName);
		    if (rp != null) {
			managedInfo.setRpInfo(new ManagedEntityInfo(
				originalResourcePoolName.substring(originalResourcePoolName.lastIndexOf('/') + 1), rp,
				this.vimConnection.getServerIntanceUuid()));
		    }
		}
	    }
	    if (StringUtils.isEmpty(this.options.getDatacenterName())) {

		String originalDatacenterName = profGen.getOriginalFolderPath();
		if (StringUtils.isNotEmpty(originalDatacenterName)) {
		    originalDatacenterName = originalDatacenterName.substring(0,
			    profGen.getOriginalFolderPath().indexOf('/'));
		}
		if (StringUtils.isEmpty(originalDatacenterName)) {
		    if (StringUtils.isNotEmpty(this.options.getVmFolderFilter())) {
			originalDatacenterName = this.options.getVmFolderFilter().substring(0,
				originalDatacenterName.indexOf('/', 1));
		    }
		} else {
		    if (StringUtils.isNotEmpty(this.options.getVmFolderFilter())) {
			if (!this.options.getVmFolderFilter().startsWith(originalDatacenterName)) {
			    originalDatacenterName = this.options.getVmFolderFilter().substring(0,
				    this.options.getVmFolderFilter().indexOf('/'));
			    IoFunction.showWarning(logger,
				    "Original Datacenter %s doesn't match with vmFilter(%s). %s will be used instead",
				    originalDatacenterName, GlobalConfiguration.getVmFilter(), originalDatacenterName);

			}
		    }
		    final ManagedObjectReference dc = this.vimConnection.findByInventoryPath(originalDatacenterName);
		    if (dc != null) {
			managedInfo.setDcInfo(new ManagedEntityInfo(originalDatacenterName, dc,
				this.vimConnection.getServerIntanceUuid()));
		    }
		}
	    }
	    if (StringUtils.isEmpty(this.options.getDatastoreName())) {
		final String originalDatastore = profGen.getOriginalDatastore();
		if ((originalDatastore != null) & !originalDatastore.isEmpty()) {

		    managedInfo.setDsInfo(
			    this.vimConnection.getManagedEntityInfo(EntityType.Datastore, originalDatastore));

		    if (managedInfo.getDsInfo() == null) {
			IoFunction.showWarning(logger, "Original datastore %s doesn't exist", originalDatastore);
		    }
		}
	    }

	    if ((this.options.getDatacenterName() != null) && (managedInfo.getDcInfo() == null))

	    {
		managedInfo.setDcInfo(this.vimConnection.getManagedEntityInfo(EntityType.Datacenter,
			this.options.getDatacenterName()));
		if (managedInfo.getDcInfo() == null) {
		    IoFunction.showWarning(logger, "Datacenter %s doesn't exist", this.options.getDatacenterName());
		    return null;
		}
	    }

	    if ((this.options.getFolderName() != null) && (managedInfo.getFolderInfo() == null)) {
		if (this.options.getFolderName().contains("/")) {
		    final String folderPath = this.options.getFolderName();
		    final ManagedObjectReference folder = this.vimConnection.findByInventoryPath(folderPath);
		    if (folder != null) {
			managedInfo.setFolderInfo(
				new ManagedEntityInfo(folderPath.substring(folderPath.lastIndexOf('/') + 1), folder,
					this.vimConnection.getServerIntanceUuid()));
		    }
		} else if (managedInfo.getDcInfo() == null) {
		    managedInfo.setFolderInfo(
			    this.vimConnection.getManagedEntityInfo(EntityType.Folder, this.options.getFolderName()));
		} else {
		    managedInfo.setFolderInfo(this.vimConnection.getManagedEntityInfo(
			    managedInfo.getDcInfo().getMoref(), EntityType.Folder, this.options.getFolderName()));
		}
		if (managedInfo.getFolderInfo() == null) {
		    IoFunction.showWarning(logger, "Folder %s doesn't exist", this.options.getFolderName());
		    return null;
		}
	    }
	    if ((this.options.getHostName() != null) && (managedInfo.getDsInfo() == null)) {
		if (managedInfo.getDcInfo() == null) {
		    managedInfo.setHostInfo(
			    this.vimConnection.getManagedEntityInfo(EntityType.HostSystem, this.options.getHostName()));
		} else {
		    managedInfo.setHostInfo(this.vimConnection.getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
			    EntityType.HostSystem, this.options.getHostName()));
		}
		if (managedInfo.getHostInfo() == null) {
		    IoFunction.showWarning(logger, "ESX host %s doesn't exist", this.options.getHostName());
		    return null;
		}
	    }

	    if ((this.options.getDatastoreName() != null) && (managedInfo.getDsInfo() == null)) {
		if (managedInfo.getDcInfo() == null) {
		    if (managedInfo.getHostInfo() == null) {
			managedInfo.setDsInfo(this.vimConnection.getManagedEntityInfo(EntityType.Datastore,
				this.options.getDatastoreName()));
		    } else {
			managedInfo
				.setDsInfo(new ManagedEntityInfo(this.options.getDatastoreName(),
					this.vimConnection.getDatastoreByHost(managedInfo.getHostInfo().getMoref(),
						this.options.getDatastoreName()),
					this.vimConnection.getServerIntanceUuid()));
		    }
		} else {
		    managedInfo.setDsInfo(this.vimConnection.getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
			    EntityType.Datastore, this.options.getDatastoreName()));
		}
		if (managedInfo.getDsInfo() == null) {
		    IoFunction.showWarning(logger, "Datastore %s doesn't exist", this.options.getDatastoreName());
		    return null;
		}
	    } else {
		if (managedInfo.getHostInfo() != null) {
		    final ManagedObjectReference entityMor = this.vimConnection
			    .getDatastoreByHost(managedInfo.getHostInfo().getMoref());
		    managedInfo.setDsInfo(new ManagedEntityInfo(this.vimConnection.morefHelper.entityName(entityMor),
			    entityMor, this.vimConnection.getServerIntanceUuid()));
		}
	    }
	    if ((managedInfo.getDcInfo() == null) && (managedInfo.getDsInfo() != null)) {
		managedInfo.setDcInfo(this.vimConnection.getDatacenterByMoref(managedInfo.getDsInfo().getMoref()));
	    }

	    if ((managedInfo.getHostInfo() == null) && (managedInfo.getDsInfo() != null)) {
		for (final DatastoreHostMount hostMount : this.vimConnection
			.getHostsByDatastore(managedInfo.getDsInfo().getMoref())) {
		    if (hostMount.getMountInfo().isAccessible() && hostMount.getMountInfo().isMounted()) {
			managedInfo
				.setHostInfo(new ManagedEntityInfo(this.vimConnection.getHostName(hostMount.getKey()),
					hostMount.getKey(), this.vimConnection.getServerIntanceUuid()));
			break;
		    }
		}

	    }
	    if (managedInfo.getRpInfo() == null) {
		if (this.options.getResourcePoolName() != null) {
		    if (managedInfo.getDcInfo() == null) {
			if (managedInfo.getHostInfo() == null) {
			    managedInfo.setRpInfo(this.vimConnection.getManagedEntityInfo(EntityType.ResourcePool,
				    this.options.getResourcePoolName()));
			} else {
			    final ManagedObjectReference rpMor = this.vimConnection
				    .getResourcePoolByHost(managedInfo.getHostInfo().getMoref());
			    managedInfo.setRpInfo(new ManagedEntityInfo(
				    this.vimConnection.morefHelper.entityProps(rpMor, "name").toString(), rpMor,
				    this.vimConnection.getServerIntanceUuid()));
			}
		    } else {
			final String rpName = this.options.getResourcePoolName();
			if (rpName.contains("/")) {
			    final ManagedObjectReference rpMor = this.vimConnection.findByInventoryPath(rpName);
			    if (rpMor != null) {
				managedInfo
					.setRpInfo(new ManagedEntityInfo(rpName.substring(rpName.lastIndexOf('/') + 1),
						rpMor, this.vimConnection.getServerIntanceUuid()));
			    }

			} else if (managedInfo.getDcInfo() == null) {
			    managedInfo.setFolderInfo(
				    this.vimConnection.getManagedEntityInfo(EntityType.ResourcePool, rpName));
			} else {
			    managedInfo.setFolderInfo(this.vimConnection.getManagedEntityInfo(
				    managedInfo.getDcInfo().getMoref(), EntityType.ResourcePool, rpName));
			}

		    }
		    if (managedInfo.getRpInfo() == null) {
			IoFunction.showWarning(logger, "ResourcePool %s doesn't exist",
				this.options.getResourcePoolName());
			return null;
		    }
		} else {
		    final ManagedObjectReference rpMor = this.vimConnection
			    .getResourcePoolByHost(managedInfo.getHostInfo().getMoref());
		    managedInfo.setRpInfo(
			    new ManagedEntityInfo(this.vimConnection.morefHelper.entityProps(rpMor, "name").toString(),
				    rpMor, this.vimConnection.getServerIntanceUuid()));
		}
	    }
	    if (managedInfo.getFolderInfo() == null) {

		ManagedObjectReference vmFolder;
		try {
		    vmFolder = (ManagedObjectReference) this.vimConnection.morefHelper
			    .entityProps(managedInfo.getDcInfo().getMoref(), "vmFolder");
		    managedInfo.setFolderInfo(new ManagedEntityInfo(
			    this.vimConnection.morefHelper.entityProps(vmFolder, "name").toString(), vmFolder,
			    this.vimConnection.getServerIntanceUuid()));
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));

		}
	    }

	    managedInfo.getSpbmProfile()
		    .addAll(this.vimConnection.getPbmConnection().getDefinedProfileSpec(profGen.getPbmProfileName()));

	    if (profGen.getvAppConfigPath() != null) {
		SerializableVmConfigInfo vAppConfig = null;
		final byte[] buffer = vmArcMgr.getRepositoryTarget().getvAppConfigToByteArray();
		if (buffer != null) {
		    try (final ByteArrayInputStream fos = new ByteArrayInputStream(buffer);
			    final ObjectInputStream oos = new ObjectInputStream(fos)) {
			vAppConfig = (SerializableVmConfigInfo) oos.readObject();
		    } catch (final IOException e) {
			logger.warning(Utility.toString(e));
		    } catch (final ClassNotFoundException e) {
			logger.warning(Utility.toString(e));
		    }
		    managedInfo.setVAppConfig(vAppConfig);
		} else {
		    return null;
		}
	    }

	    final String uuid = profGen.getInstanceUuid();
	    final ManagedObjectReference vm = this.vimConnection.getVimPort().findByUuid(
		    this.vimConnection.getSearchIndex(), managedInfo.getDcInfo().getMoref(), uuid, true, true);
	    if ((vm != null) && (this.options.getNewName() == null)) {
		IoFunction.showWarning(logger, "Virtual Machine  name:%s uuid:%s already exist", profGen.getName(),
			uuid);
		return null;

	    }
	}
//	if (this.options.isParentAVApp()) {
//	    managedInfo.setRpInfo(this.options.getParent().getManageEntityInfo());
//	}
	logger.exiting(getClass().getName(), "getRestoreManagedInfo", managedInfo);
	return managedInfo;
    }

    public OperationResult restore(final FcoArchiveManager vmArcMgr) throws Exception {
	logger.entering(getClass().getName(), "restoreVm", new Object[] { vmArcMgr });
	final OperationResult result = new OperationResult(this.options.getGenerationId());

	VirtualMachineManager vmm = null;

	SnapshotManager snap = null;
	final ITarget target = vmArcMgr.getRepositoryTarget();
	boolean successRestoreVM = false;

	IoFunction.showInfo(logger, "restoreVm() start.");
	final GenerationProfile profGen = vmArcMgr.setTargetGeneration(this.options.getGenerationId());
	if (profGen == null) {
	    IoFunction.showWarning(logger, "The specified generation %d doesn't exist.",
		    this.options.getGenerationId());
	    result.skip();
	} else if (profGen.isSucceeded() == false) {
	    IoFunction.showWarning(logger, "The specified generation %d is marked FAILED.",
		    this.options.getGenerationId());
	    result.fails();
	} else {
	    assert profGen != null;
	    target.setProfGen(profGen);

	    String vmName = profGen.getName();
	    if (StringUtils.isNotEmpty(this.options.getNewName())) {
		if (this.options.getNewName().equals(vmName)) {
		    this.options.setRecover(true);
		} else {
		    vmName = this.options.getNewName();
		}
	    }
	    if (this.vimConnection.isVmByNameExist(vmName)) {
		IoFunction.showWarning(logger, "Virtual Machine name:%s already exist", vmName);
		result.skip();
	    } else {
		IoFunction.showInfo(logger, "Restore source: \"%s\" (%s) uuid:%s.", profGen.getName(),
			profGen.getMoref(), profGen.getInstanceUuid());
		IoFunction.showInfo(logger, "Restore destination: \"%s\".", vmName);

		final RestoreManagedInfo managedInfo = getRestoreManagedInfo(vmArcMgr);

		if ((managedInfo == null) || (managedInfo.getDsInfo() == null)) {
		    result.fails();
		} else if (this.options.isDryRun()) {
		    IoFunction.showInfo(logger, "Restore ends cause dryrun.");
		    result.dryruns();
		} else {
		    vmm = restoreVmMetadata(vmArcMgr, managedInfo);
		    result.setFco(vmm);
		    if (this.options.isParentAVApp()) {
			IoFunction.showInfo(logger, "Moving Virtual Machine inside the vApp.");
			final List<ManagedObjectReference> vmListMoref = new ArrayList<>();
			vmListMoref.add(vmm.getMoref());
			this.vimConnection.getVimPort().moveIntoResourcePool(this.options.getParent().getMoref(),
				vmListMoref);
		    }
		    if (vmm == null) {
			IoFunction.showWarning(logger, "Could not find newly created vm.");
			result.fails();
		    } else {
			if (vmm.reconfigureVm(vmArcMgr,profGen, managedInfo)) {

			    vmm.reload();
			    if (this.options.isNoVmdk()) {
				IoFunction.showInfo(logger,
					"Disks are not restored because specified --novmdk option.");
				IoFunction.showInfo(logger, "restoreVm() end.");
				result.success();
			    } else {
				boolean snapEnable = GlobalConfiguration.isForceSnapBeforeRestore();
				if ((this.options.getTransportMode() != null)
					&& this.options.getTransportMode().contains("san")) {
				    IoFunction.showInfo(logger,
					    "SAN transport mode selected. Enable restore with snapshot ");
				    snapEnable = true;
				}
				if (snapEnable) {
				    snap = createSnapshot(vmm, Calendar.getInstance());
				}
				if ((snap != null) || !snapEnable) {
				    final Jvddk jvddk = this.vimConnection.configureVddkAccess(vmm.getMoref());
				    final String snapMoref = (snapEnable) ? snap.getMoref().getValue() : null;
				    final List<VmdkInfo> vmdkInfoList = vmm.getConfig()
					    .getAllVmdkInfo(vmm.getVStorageObjectAssociations());
				    if (vmdkInfoList.size() > 0) {
					try {
					    jvddk.prepareForAccess(VmbkVersion.getIdentity());
					    if (jvddk.connect(false, snapMoref,
						    GlobalConfiguration.getTransportModeRestore(this.options))) {

						IoFunction.showInfo(logger, "Restore %d disks.", vmdkInfoList.size());

						for (final VmdkInfo vmdkInfo : vmdkInfoList) {
						    final int diskId = vmArcMgr.getTargetDiskId(vmdkInfo);
						    if (diskId < 0) {

							logger.warning("diskId invalid.\n" + vmdkInfo.toString());
							continue;
						    }

						    try {
							target.open(diskId, profGen.isDiskCompressed(diskId));

							if (jvddk.doRestoreJava(vmArcMgr, this.options, diskId,
								vmdkInfo.getName())) {

							    IoFunction.showInfo(logger, "Restoring vmdk %d succeeded.",
								    diskId);
							    successRestoreVM = true;
							} else {

							    IoFunction.showInfo(logger, "Restoring vmdk %d failed.\n",
								    diskId);
							    successRestoreVM = false;
							    break;
							}
						    } finally {
							target.close();
						    }
						}
					    }
					} finally {
					    jvddk.endAccess();
					    jvddk.disconnect();
					    jvddk.Cleanup();
					}
				    } else {
					successRestoreVM = true;
				    }
				    if (successRestoreVM) {

					if (snap != null) {
					    final ManagedObjectReference snapMor = snap.getMoref();
					    if (vmm.revertToSnapshot(snapMor)) {
						IoFunction.showInfo(logger, "Revert snapshot %s succeeded.",
							snap.getName());
					    } else {
						IoFunction.showInfo(logger, "Revert snapshot %s failed.",
							snap.getName());
						successRestoreVM = false;
					    }

					    if (vmm.deleteSnapshot(snapMor, false, true)) {
						IoFunction.showInfo(logger, "Delete snapshot %s succeeded.",
							snap.getName());
					    } else {
						IoFunction.showInfo(logger, "Delete snapshot %s failed.",
							snap.getName());
						successRestoreVM = false;
					    }
					}
				    }
				    if (successRestoreVM) {
					if (this.options.isPowerOn()) {
					    Thread.sleep(1000);
					    if (vmm.powerOn(managedInfo.getHostInfo())) {
						IoFunction.showInfo(logger, "PowerOn Vm %s succeeded.", vmm.getName());
					    } else {
						IoFunction.showInfo(logger, "PowerOn Vm %s failed.", vmm.getName());
						successRestoreVM = false;
					    }
					}
				    }
				    if (successRestoreVM) {
					IoFunction.showInfo(logger, "restoreVm() end.");
					result.success();
				    }
				}
			    }
			}

		    }
		}
	    }
	}
	if (result.isFails()) {
	    if (vmm != null) {
		IoFunction.showWarning(logger, "Restored failed delete the VM and folder. ");
		vmm.destroy();
	    }
	}
	logger.exiting(getClass().getName(), "restoreVm", result);
	return result;

    }

    private VirtualMachineManager restoreVmMetadata(final FcoArchiveManager vmArcMgr,
	    final RestoreManagedInfo managedInfo) throws Exception {
	VirtualMachineManager vmm = null;
	final GenerationProfile profGen = vmArcMgr.getTargetGeneration();
	vmm = new VirtualMachineManager(this.vimConnection, managedInfo.getName(), managedInfo.getDcInfo());

	if (vmm.createVMHomeDirectory(managedInfo)) {
	    final ITarget target = vmArcMgr.getRepositoryTarget();

	    target.setFcoTarget(vmm);
	    target.setProfGen(profGen);
	    if (vmm.importVmx(managedInfo, profGen, target.getVmxToByteArray())) {
		if (vmm.importNvram(managedInfo, target.getNvRamToByteArray())) {
		    final ManagedObjectReference vmMor = this.vimConnection.registerVirtualMachine(managedInfo);
		    if (vmMor != null) {
			if (vmm.update(vmMor)) {
			    return vmm;
			}
		    }
		}
	    }
	}
	if (this.vimConnection.deleteDirectory(managedInfo.getDcInfo(), managedInfo.getDsInfo(),
		managedInfo.getDirectoryName())) {
	    IoFunction.showInfo(logger, "VM directory:%s deleted", managedInfo.getDirectoryName());

	}
	return null;
    }

}
