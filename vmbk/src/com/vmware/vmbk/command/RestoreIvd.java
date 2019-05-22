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

import java.util.logging.Logger;

import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Jvddk;
import com.vmware.vmbk.control.OperationResult;
import com.vmware.vmbk.control.VmbkVersion;
import com.vmware.vmbk.control.info.RestoreManagedInfo;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;

public class RestoreIvd {
    protected static Logger logger = Logger.getLogger("Command");
    RestoreOptions options;
    VimConnection vimConnection;

    public RestoreIvd(final VimConnection vimConnection, final RestoreOptions options) {
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

	final String originalDatastore = profGen.getOriginalDatastore();
	if ((originalDatastore != null) & !originalDatastore.isEmpty()) {

	    managedInfo.setDsInfo(this.vimConnection.getManagedEntityInfo(EntityType.Datastore, originalDatastore));

	    if (managedInfo.getDsInfo() == null) {
		IoFunction.showWarning(logger, "Original datastore %s doesn't exist", originalDatastore);
	    }
	}
	final String originalDatacenter = profGen.getOriginalDatacenter();
	if ((originalDatastore != null) & !originalDatastore.isEmpty()) {

	    managedInfo.setDcInfo(this.vimConnection.getManagedEntityInfo(EntityType.Datacenter, originalDatacenter));

	    if (managedInfo.getDcInfo() == null) {
		IoFunction.showWarning(logger, "Original datacenter %s doesn't exist", originalDatacenter);
	    }
	}
	if ((this.options.getDatacenterName() != null) && (managedInfo.getDcInfo() == null)) {
	    managedInfo.setDcInfo(
		    this.vimConnection.getManagedEntityInfo(EntityType.Datacenter, this.options.getDatacenterName()));
	    if (managedInfo.getDcInfo() == null) {
		IoFunction.showWarning(logger, "Datacenter %s doesn't exist", this.options.getDatacenterName());
		return null;
	    }
	}
	if ((this.options.getDatastoreName() != null) && (managedInfo.getDsInfo() == null)) {
	    if (managedInfo.getDcInfo() == null) {
		managedInfo.setDsInfo(
			this.vimConnection.getManagedEntityInfo(EntityType.Datastore, this.options.getDatastoreName()));
	    } else {
		managedInfo.setDsInfo(this.vimConnection.getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
			EntityType.Datastore, this.options.getDatastoreName()));
	    }
	    if (managedInfo.getDsInfo() == null) {
		IoFunction.showWarning(logger, "Datastore %s doesn't exist", this.options.getDatastoreName());
		return null;
	    }
	}
	managedInfo.getSpbmProfile()
		.addAll(this.vimConnection.getPbmConnection().getDefinedProfileSpec(profGen.getPbmProfileName(0)));

	logger.exiting(getClass().getName(), "getRestoreManagedInfo", managedInfo);
	return managedInfo;
    }

    public OperationResult restore(final FcoArchiveManager vmArcMgr) throws Exception {
	logger.entering(getClass().getName(), "restoreIvd", new Object[] { vmArcMgr });
	final OperationResult result = new OperationResult(this.options.getGenerationId());
	ImprovedVirtuaDisk ivd = null;

	final ITarget target = vmArcMgr.getRepositoryTarget();
	boolean successRestoreVM = false;

	IoFunction.showInfo(logger, "restoreIvd() start.");
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

	    target.setProfGen(profGen);

	    String ivdName = profGen.getName();
	    if ((this.options.getNewName() != null) && !this.options.getNewName().isEmpty()) {
		if (this.options.getNewName().equals(ivdName)) {
		    this.options.setRecover(true);
		} else {
		    ivdName = this.options.getNewName();
		}
	    }
	    if (this.vimConnection.isIvdByNameExist(ivdName)) {
		IoFunction.showWarning(logger, "ImprovedVirtualDisk with name:%s already exist", ivdName);

		result.skip();
	    } else {
		IoFunction.showInfo(logger, "Restore source: \"%s\" uuid:%s.", profGen.getName(),
			profGen.getInstanceUuid());
		IoFunction.showInfo(logger, "Restore destination: \"%s\".", ivdName);

		final RestoreManagedInfo managedInfo = getRestoreManagedInfo(vmArcMgr);

		if ((managedInfo == null) || (managedInfo.getDsInfo() == null)) {

		    result.fails();
		} else if (this.options.isDryRun()) {
		    IoFunction.showInfo(logger, "Restore ends cause dryrun.");

		    result.dryruns();
		} else {
		    final long size = profGen.getCapacity(0);
		    // size = (size / 1024) / 1024;
		    final VStorageObject vstorage = this.vimConnection.getVslmConnection().ivd_create(
			    managedInfo.getDsInfo(), this.options.getNewName(), profGen.getDiskProvisionType(0), size,
			    managedInfo.getSpbmProfile());

		    if (vstorage == null) {
			IoFunction.showWarning(logger, "Could not find newly created IVD.");

			result.fails();
		    } else {
			ivd = new ImprovedVirtuaDisk(this.vimConnection, vstorage.getConfig().getId(), vstorage,
				managedInfo.getDsInfo());
			result.setFco(ivd);
			final Jvddk jvddk = this.vimConnection.configureVddkAccess(ivd);
			try {
			    jvddk.prepareForAccess(VmbkVersion.getIdentity());
			    if (jvddk.connect(false, GlobalConfiguration.getTransportModeRestore(this.options))) {

				try {
				    target.open(0, profGen.isDiskCompressed(0));

				    if (jvddk.doRestoreJava(vmArcMgr, this.options, 0, this.options.getNewName())) {

					IoFunction.showInfo(logger, "Restoring vmdk %d succeeded.", 0);
					successRestoreVM = true;
				    } else {

					IoFunction.showInfo(logger, "Restoring vmdk %d failed.\n", 0);
					successRestoreVM = false;

				    }
				} finally {
				    target.close();
				}

			    }
			} finally {
			    jvddk.endAccess();
			    jvddk.disconnect();
			    jvddk.Cleanup();
			}
			if (successRestoreVM) {

			    IoFunction.showInfo(logger, "restoreIvd() end.");
			    result.success();
			}
		    }
		}
	    }
	}

	if (result.isFails()) {
	    if (ivd != null) {
		IoFunction.showWarning(logger, "Restored failed delete the IVD and folder. ");
		ivd.destroy();
	    }
	}
	logger.exiting(getClass().getName(), "restoreIvd", result);
	return result;

    }

}
