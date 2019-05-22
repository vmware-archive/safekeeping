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
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.OperationResult;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.info.GrandTotalDumpFileInfo;
import com.vmware.vmbk.control.info.TotalDumpFileInfo;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.soap.VimConnection;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

public class RestoreCommand extends CommandSupportForSnapshot {

    private String datacenterName;
    private String datastoreName;
    private String folderName;
    private int generationId;
    private String hostName;
    private boolean isNoVmdk;
    private String newName;
    private boolean powerOn;

    private boolean recover;
    private String resourcePoolName;
    private String resPoolFilter;

    private ArrayList<TotalDumpFileInfo> totalDumpsInfo;
    private String transportMode;
    private String vmFolderFilter;

    private String[] vmNetworksName;

    public RestoreCommand() {
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
	int countVapp = 0;

	final StringBuilder result = new StringBuilder("type\tsuccess\tfails\tskips\t");
	result.append(VirtualMachineManager.headerToString());
	try {
	    IoFunction.showInfo(logger, this.toString());
	    final GlobalProfile profAllFco = vmbk.getRepositoryTarget().initializeProfileAllFco();
	    final LinkedList<ManagedFcoEntityInfo> entities = getTargetfromRepository(profAllFco);
	    if (entities.size() == 0) {
		IoFunction.showWarning(logger, "No valid targets specified");
	    } else {
		for (final ManagedFcoEntityInfo entityInfo : entities) {
		    FirstClassObject fco = null;
		    OperationResult opResult = new OperationResult();
		    if (Vmbk.isAbortTriggered()) {
			IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
			opResult.skip();
		    } else {
			FcoArchiveManager vmArcMgr = null;
			final String vim = StringUtils.isEmpty(this.vim) ? entityInfo.getServerUuid() : this.vim;
			VimConnection vimConnection = vmbk.getConnetionManager().getVimConnection(vim);
			if (vimConnection == null) {

			    IoFunction.showWarning(logger,
				    "No vCenter with UUID %s exist. Switch to default vcenter %s",
				    entityInfo.getServerUuid(),
				    connetionManager.getDefualtVcenter().getServerIntanceUuid());
			    vimConnection = connetionManager.getDefualtVcenter();
			}
			try {
			    RestoreOptions options = null;
			    switch (entityInfo.getEntityType()) {
			    case VirtualMachine:
				++countVm;
				fco = new VirtualMachineManager(vimConnection, entityInfo);
				vmbk.getRepositoryTarget().setFcoTarget(fco);
				vmArcMgr = new FcoArchiveManager(fco, vmbk.getRepositoryTarget(),
					ArchiveManagerMode.readOnly);
				options = RestoreOptions.NewRestoreOptions(this);
				final RestoreVm restoreVm = new RestoreVm(vimConnection, options);
				opResult = restoreVm.restore(vmArcMgr);
				this.totalDumpsInfo.addAll(options.getTotalDumpsInfo());
				// opResult = restoreVm(vimConnection, vmArcMgr);
				break;
			    case ImprovedVirtualDisk:
				fco = new ImprovedVirtuaDisk(vimConnection, entityInfo);
				vmbk.getRepositoryTarget().setFcoTarget(fco);
				vmArcMgr = new FcoArchiveManager(fco, vmbk.getRepositoryTarget(),
					ArchiveManagerMode.readOnly);
				options = RestoreOptions.NewRestoreOptions(this);
				final RestoreIvd restoreIvd = new RestoreIvd(vimConnection, options);
				opResult = restoreIvd.restore(vmArcMgr);
				this.totalDumpsInfo.addAll(options.getTotalDumpsInfo());
				// opResult = restoreIvd(vimConnection, vmArcMgr);
				++countIvd;
				break;
			    case VirtualApp:
				fco = new VirtualAppManager(vimConnection, entityInfo);
				vmbk.getRepositoryTarget().setFcoTarget(fco);
				vmArcMgr = new FcoArchiveManager(fco, vmbk.getRepositoryTarget(),
					ArchiveManagerMode.readOnly);
				options = RestoreOptions.NewRestoreOptions(this);
				final RestoreVApp restoreVApp = new RestoreVApp(vimConnection, options);
				opResult = restoreVApp.restore(vmArcMgr);
				this.totalDumpsInfo.addAll(options.getTotalDumpsInfo());
				// opResult = restoreVApp(vimConnection, vmArcMgr);

				++countVapp;
				break;
			    default:
				continue;

			    }

			} catch (final Exception e) {
			    if (fco != null) {
				IoFunction.showWarning(logger, "restore of %s failed.", fco.getUuid());
			    }
			    logger.warning(Utility.toString(e));

			} finally {
			    if (opResult.getFco() != null) {
				result.append(IoFunction.showInfo(logger, "Restore %s\n", opResult.toString()));
			    } else {
				result.append(IoFunction.showWarning(logger, "Restore %s\n", opResult.toString()));
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
		    }
		}
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

    public void add(final TotalDumpFileInfo e) {
	this.totalDumpsInfo.add(e);
    }

    public String getDatacenterName() {
	if (StringUtils.isNotEmpty(this.vmFolderFilter)) {
	    final String datacenterFromFilter = this.vmFolderFilter.substring(0, this.vmFolderFilter.indexOf("/"));
	    if (StringUtils.isEmpty(this.datacenterName)) {
		return datacenterFromFilter;
	    } else {
		if (this.datacenterName.equals(datacenterFromFilter)) {
		    return this.datacenterName;
		} else {
		    IoFunction.showWarning(logger,
			    "Requested Datacenter name %s is not matching with vmFolder path. vmFolderFilter Datacenter name %s will be used",
			    this.datacenterName, datacenterFromFilter);
		    return datacenterFromFilter;
		}
	    }
	}
	return this.datacenterName;
    }

    public String getDatastoreName() {
	return this.datastoreName;
    }

    public String getFolderName() {
	if (StringUtils.isNotEmpty(this.vmFolderFilter)) {
	    if (StringUtils.isEmpty(this.folderName)) {
		return this.vmFolderFilter;
	    } else {
		if (!this.folderName.contains(this.vmFolderFilter)) {
		    IoFunction.showWarning(logger,
			    "Requested Folder %s is not a subfolder of vmFolderFilter(%s). vmFilter will be used instead ",
			    this.folderName, this.vmFolderFilter);
		    return this.vmFolderFilter;
		}
	    }
	}
	return this.folderName;
    }

    public int getGenerationId() {
	return this.generationId;
    }

    public String getHostName() {
	return this.hostName;
    }

    public String getNewName() {
	return this.newName;
    }

    public String getResourcePoolName() {
	if (StringUtils.isNotEmpty(this.resPoolFilter)) {
	    if (StringUtils.isEmpty(this.resourcePoolName)) {
		return this.resPoolFilter;
	    } else {
		if (this.resourcePoolName.contains(this.resPoolFilter)) {
		    return this.resourcePoolName;
		} else {
		    IoFunction.showWarning(logger,
			    "Requested ResourcePool %s is not a subfolder of rpFilter(%s). rpFilter will be used instead ",
			    this.resourcePoolName, this.resPoolFilter);
		    return this.resPoolFilter;
		}
	    }
	}
	return this.resourcePoolName;
    }

    public String getResPoolFilter() {
	return this.resPoolFilter;
    }

    private GrandTotalDumpFileInfo getTotalDumpInfo(final int success, final int fails, final int skips,
	    final int countVm, final int countIvd, final int countVapp) {
	return new GrandTotalDumpFileInfo(success, fails, skips, countVm, countIvd, countVapp, this.totalDumpsInfo);
    }

    public ArrayList<TotalDumpFileInfo> getTotalDumpsInfo() {
	return this.totalDumpsInfo;
    }

    public String getTransportMode() {
	return this.transportMode;
    }

    public String getVmFolderFilter() {
	return this.vmFolderFilter;
    }

    public String[] getVmNetworksName() {
	return this.vmNetworksName;
    }

    @Override
    public void initialize() {
	super.initialize();
	this.newName = null;
	this.generationId = -1;
	this.hostName = null;
	this.datastoreName = null;
	this.folderName = null;
	this.resourcePoolName = null;
	this.datacenterName = null;
	this.isNoVmdk = false;
	this.vmFolderFilter = null;
	this.resPoolFilter = null;
	this.isNoVmdk = false;
	this.powerOn = false;
	this.transportMode = null;
	this.recover = false;
	this.totalDumpsInfo = new ArrayList<>();
	this.vmNetworksName = new String[GlobalConfiguration.getMaxNumberOfVirtaulMachineNetworkCard()];
	for (int i = 0; i < this.vmNetworksName.length; i++) {
	    this.vmNetworksName[i] = "";
	}
    }

    public boolean isNoVmdk() {
	return this.isNoVmdk;
    }

    public boolean isPowerOn() {
	return this.powerOn;
    }

    public boolean isRecover() {
	return this.recover;
    }

    protected final void setDatacenterName(final String datacenterName) {
	this.datacenterName = datacenterName;
    }

    protected final void setDatastoreName(final String datastoreName) {
	this.datastoreName = datastoreName;
    }

    protected final void setFolderName(final String folderName) {
	this.folderName = folderName;
    }

    public void setGenerationId(final int generationId) {
	this.generationId = generationId;
    }

    protected final void setHostName(final String hostName) {
	this.hostName = hostName;
    }

    public void setNewName(final String newName) {
	this.newName = newName;
    }

    public void setNoVmdk(final boolean isNoVmdk) {
	this.isNoVmdk = isNoVmdk;
    }

    public void setPowerOn(final boolean powerOn) {
	this.powerOn = powerOn;
    }

    protected final void setRecover(final boolean b) {
	this.recover = b;
    }

    protected final void setResourcePoolName(final String resourcePoolName) {
	this.resourcePoolName = resourcePoolName;
    }

    public void setTransportMode(final String transportMode) {
	this.transportMode = transportMode;
    }

    protected final void setVmNetworksName(final String st) {
	final String[] newvmNetworksName = st.split(",");
	for (int i = 0; i < newvmNetworksName.length; i++) {
	    this.vmNetworksName[i] = newvmNetworksName[i];
	}
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("RestoreInfo: ");

	if (this.recover) {
	    sb.append("[Recovery]");
	}
	if (this.newName != null) {
	    sb.append(String.format("[newName %s]", this.newName));
	}

	sb.append(String.format("[generation %s]",
		(this.generationId == -1) ? "last" : PrettyNumber.toString(this.generationId)));
	if (getResourcePoolName() != null) {
	    sb.append(String.format("[Resource Pool %s]", getResourcePoolName()));
	}
	if (getHostName() != null) {
	    sb.append(String.format("[host %s]", getHostName()));
	}
	if (getDatastoreName() != null) {
	    sb.append(String.format("[datastore %s]", getDatastoreName()));
	}
	if (getDatacenterName() != null) {
	    sb.append(String.format("[datacenter %s]", getDatacenterName()));
	}
	if (getFolderName() != null) {
	    sb.append(String.format("[folder %s]", getFolderName()));
	}

	sb.append(String.format("[isNoVmdk %s]", PrettyBoolean.toString(this.isNoVmdk)));
	sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(this.isDryRun())));
	sb.append(String.format("[transportMode %s]",
		(StringUtils.isEmpty(this.transportMode)) ? "default" : this.transportMode));

	return sb.toString();
    }

}
