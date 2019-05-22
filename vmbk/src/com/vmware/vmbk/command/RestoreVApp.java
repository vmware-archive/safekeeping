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
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.OperationResult;
import com.vmware.vmbk.control.info.RestoreManagedInfo;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

public class RestoreVApp {
    protected static Logger logger = Logger.getLogger("Command");
    RestoreOptions options;
    VimConnection vimConnection;

    public RestoreVApp(final VimConnection vimConnection, final RestoreOptions options) {
	this.options = options;
	this.vimConnection = vimConnection;
    }

    private RestoreManagedInfo getRestoreManagedInfo(final FcoArchiveManager vmArcMgr)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	logger.entering(getClass().getName(), "getRestoreManagedInfo");
	final RestoreManagedInfo managedInfo = new RestoreManagedInfo(
		GlobalConfiguration.getMaxNumberOfVirtaulMachineNetworkCard());
	final GenerationProfile profGen = vmArcMgr.getTargetGeneration();

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
		    managedInfo.setFolderInfo(new ManagedEntityInfo(vmFolder.substring(vmFolder.lastIndexOf('/') + 1),
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
		    IoFunction.showWarning(logger, "ResourcePool(%s) doesn't exist rpFilter(%s) will be used instead",
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
	if (profGen.getvAppConfigPath() != null) {
	    SerializableVAppConfigInfo vAppConfig = null;
	    final byte[] buffer = vmArcMgr.getRepositoryTarget().getvAppConfigToByteArray();
	    if (buffer != null) {
		try (final ByteArrayInputStream fos = new ByteArrayInputStream(buffer);
			final ObjectInputStream oos = new ObjectInputStream(fos)) {
		    vAppConfig = (SerializableVAppConfigInfo) oos.readObject();
		} catch (final IOException e) {
		    logger.warning(Utility.toString(e));
		} catch (final ClassNotFoundException e) {
		    logger.warning(Utility.toString(e));
		}
		managedInfo.setVAppConfig(vAppConfig);
	    } else {
		return null;
	    }
//Resource pool information
	    final ResourceConfigSpec configSpec = new ResourceConfigSpec();
	    configSpec.setCpuAllocation(profGen.getCpuAllocation());
	    configSpec.setMemoryAllocation(profGen.getMemAllocation());
	    managedInfo.setResourceConfigSpec(configSpec);
	}
	if ((this.options.getFolderName() != null) && (managedInfo.getFolderInfo() == null)) {
	    if (this.options.getFolderName().contains("/")) {
		final String folderPath = this.options.getFolderName();
		final ManagedObjectReference folder = this.vimConnection.findByInventoryPath(folderPath);
		if (folder != null) {
		    managedInfo
			    .setFolderInfo(new ManagedEntityInfo(folderPath.substring(folderPath.lastIndexOf('/') + 1),
				    folder, this.vimConnection.getServerIntanceUuid()));
		}
	    } else if (managedInfo.getDcInfo() == null) {
		managedInfo.setFolderInfo(
			this.vimConnection.getManagedEntityInfo(EntityType.Folder, this.options.getFolderName()));
	    } else {
		managedInfo.setFolderInfo(this.vimConnection.getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
			EntityType.Folder, this.options.getFolderName()));
	    }
	    if (managedInfo.getFolderInfo() == null) {
		IoFunction.showWarning(logger, "Folder %s doesn't exist", this.options.getFolderName());
		return null;
	    }
	}
	if (StringUtils.isEmpty(this.options.getDatacenterName())) {
	    String originalDatacenterName = profGen.getOriginalFolderPath().substring(0,
		    profGen.getOriginalFolderPath().indexOf('/'));
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
			    managedInfo.setRpInfo(new ManagedEntityInfo(rpName.substring(rpName.lastIndexOf('/') + 1),
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
		    IoFunction.showWarning(logger, "ResourcePool %s doesn't exist", this.options.getResourcePoolName());
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

	logger.exiting(getClass().getName(), "getRestoreManagedInfo", managedInfo);
	return managedInfo;
    }

    public OperationResult restore(final FcoArchiveManager vmArcMgr) throws Exception {
	logger.entering(getClass().getName(), "restoreVApp", new Object[] { vmArcMgr });
	final OperationResult result = new OperationResult(this.options.getGenerationId());
	VirtualAppManager vapp = null;
	final ITarget target = vmArcMgr.getRepositoryTarget();
	// final boolean successRestoreVM = false;

	IoFunction.showInfo(logger, "restoreVApp() start.");
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
	    if (this.vimConnection.isVAppByNameExist(vmName)) {
		IoFunction.showWarning(logger, "VirtualApp name:%s already exist", vmName);
		result.skip();
	    } else {
		IoFunction.showInfo(logger, "Restore source: \"%s\" (%s) uuid:%s.", profGen.getName(),
			profGen.getMoref(), profGen.getInstanceUuid());
		IoFunction.showInfo(logger, "Restore destination: \"%s\".", vmName);
		final RestoreManagedInfo managedInfo = getRestoreManagedInfo(vmArcMgr);
		if ((managedInfo == null)) {
		    result.fails();
		} else if (this.options.isDryRun()) {
		    IoFunction.showInfo(logger, "Restore ends cause dryrun.");
		    result.dryruns();
		} else {

		    vapp = restoreVAppMetadata(vmArcMgr, managedInfo);
		    result.setFco(vapp);

		    final GlobalProfile profAllFco = target.initializeProfileAllFco();
		    // this.options.setRecover(true);
		    try {
			final LinkedHashMap<String, Integer> vappChildren = profGen.getVappChildrenUuid();
			for (final String uuid : vappChildren.keySet()) {
			    if (profAllFco.isExistVirtualMachineWithUuid(uuid)) {
				final ManagedFcoEntityInfo entityInfo = profAllFco
					.getFirstClassObjectEntityByUuid(uuid);
				final VirtualMachineManager fco = new VirtualMachineManager(this.vimConnection,
					entityInfo);
				target.setFcoTarget(fco);
				final FcoArchiveManager localArcMgr = new FcoArchiveManager(fco, target,
					ArchiveManagerMode.readOnly);

				final RestoreOptions vmOptions = RestoreOptions.NewRestoreOptions(this.options, vapp);
				if (!this.options.isRecover()) {
				    vmOptions.setRecover(false);
				    vmOptions.setNewName(vapp.getName() + "-" + fco.getName());
				    vmOptions.setGenerationId(vappChildren.get(uuid));
				}
				final RestoreVm restoreVm = new RestoreVm(this.vimConnection, vmOptions);
				final OperationResult subOperation = restoreVm.restore(localArcMgr);
				result.getSubOperations().add(subOperation);
				if (managedInfo.getVAppConfig().replaceVm(entityInfo.getMoref(),
					subOperation.getFco().getMoref())) {
				    IoFunction.showInfo(logger, "Replaced vm moref. old:%s to new:%s",
					    entityInfo.getMorefValue(), subOperation.getFco().getMoref().getValue());
				} else {
				    IoFunction.showWarning(logger, "Failed to replace  vm moref. old:%s to new:%s",
					    entityInfo.getMorefValue(), subOperation.getFco().getMoref().getValue());
				}
//				for (MyVAppEntityConfigInfo i:vapp.getvAppConfig().getEntityConfig()) {
//				    if (i.isKey(entityInfo.getMorefValue())){
//					i.setKey( subOperation.getFco().getMoref().getValue());
//				    }
//				}
				this.options.getTotalDumpsInfo().addAll(vmOptions.getTotalDumpsInfo());
				if (subOperation.isFails()) {
				    break;
				    // result.getSubOperations().add(restoreVm(this.vimConnection, localArcMgr));
				}
			    } else {
				result.fails();
				break;
			    }
			}
		    } catch (final Exception e) {
			logger.warning(Utility.toString(e));
		    } finally {
			if (result.isFails()) {
			    if (vapp != null) {
				IoFunction.showWarning(logger, "Restored failed.Deleting the Vapp and folder. ");
				vapp.destroy();
			    }
			} else {
			    if (vapp.updateVAppConfig(managedInfo.getVAppConfig())) {
				IoFunction.showInfo(logger, "vApp configuration updated");
			    } else {
				IoFunction.showWarning(logger, "vApp configuration updated failed");
			    }
			}
		    }
		}
	    }
	}
	logger.exiting(getClass().getName(), "restoreVApp", result);
	return result;
    }

    private VirtualAppManager restoreVAppMetadata(final FcoArchiveManager vmArcMgr,
	    final RestoreManagedInfo managedInfo) throws Exception {
	VirtualAppManager vmm = null;
	final GenerationProfile profGen = vmArcMgr.getTargetGeneration();
	vmm = new VirtualAppManager(this.vimConnection, managedInfo.getName());
	final ITarget target = vmArcMgr.getRepositoryTarget();

	target.setFcoTarget(vmm);
	target.setProfGen(profGen);
	final ManagedObjectReference moref = this.vimConnection.createVApp(managedInfo.getName(),
		managedInfo.getRpInfo(), managedInfo.getFolderInfo(), managedInfo.getResourceConfigSpec(),
		new VAppConfigSpec());

	if (moref != null) {
	    if (vmm.update(moref)) {

		return vmm;
	    }
	}
	return vmm;
    }
}
