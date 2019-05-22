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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;

public class FcoCommand extends CommandWithOptions {

    private String datacenterName;
    private String datastoreName;
    private String folderName;
    private String hostName;
    private boolean isNoVmdk;
    private String name;
    private boolean powerOn;

    private String resourcePoolName;
    private String resPoolFilter;

    private String vmFolderFilter;

    private String[] vmNetworksName;
    private URL urlPath;
    private boolean vappImport;
    private boolean clone;
    private boolean remove;
    private Boolean cbt;

    public FcoCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	if (this.vappImport) {
	    action_VappImport(connetionManager);
	} else if (this.clone) {
	    action_Clone(connetionManager);
	} else if (this.remove) {
	    action_Remove(connetionManager);
	} else if (this.cbt) {
	    action_Cbt(connetionManager);
	}
	logger.exiting(getClass().getName(), "action");

    }

    private void action_Cbt(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Cbt", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	int countIvd = 0;
	int countVapp = 0;

	final LinkedList<FirstClassObject> fcoList = getFcoTarget(connetionManager, this.anyFcoOfType);

	if (fcoList.size() > 0) {

	    for (final FirstClassObject fco : fcoList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualMachineManager) {
			++countVm;
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			if (vmm.setChangeBlockTracking(this.cbt)) {
			    ++success;
			} else {
			    ++failures;
			}
		    } else if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vApp = (VirtualAppManager) fco;
			++countVapp;
			if (vApp.setChangeBlockTracking(this.cbt)) {
			    ++success;
			} else {
			    ++failures;
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			++countIvd;
			if (ivd.setChangeBlockTracking(this.cbt)) {
			    ++success;
			} else {
			    ++failures;
			}

		    }
		}
	    }
	    IoFunction.println();
	    IoFunction.printTotal("%d vm:%d ivd:%d vApp:%d Success:%d fails:%d skip:%d", success + failures + skip,
		    countVm, countIvd, countVapp, success, failures, skip);
	    IoFunction.println();
	} else {
	    IoFunction.showWarning(logger, "No valid targets");

	}
	logger.exiting(getClass().getName(), "action_Cbt");
    }

    private void action_Clone(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Clone", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	int countIvd = 0;
	int countVapp = 0;

	final LinkedList<FirstClassObject> fcoList = getFcoTarget(connetionManager, this.anyFcoOfType);

	if (fcoList.size() > 0) {

	    for (final FirstClassObject fco : fcoList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualMachineManager) {
			++countVm;
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			if (vmm.clone(this.name)) {
			    ++success;
			} else {
			    ++failures;
			}
		    } else if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vApp = (VirtualAppManager) fco;
			++countVapp;
			if (vApp.clone(this.name, this.datastoreName)) {
			    ++success;
			} else {
			    ++failures;
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			++countIvd;
			if (ivd.clone(this.name, this.datastoreName)) {
			    ++success;
			} else {
			    ++failures;
			}

		    }
		}
	    }
	    IoFunction.println();
	    IoFunction.printTotal("%d vm:%d ivd:%d vApp:%d Success:%d fails:%d skip:%d", success + failures + skip,
		    countVm, countIvd, countVapp, success, failures, skip);
	    IoFunction.println();
	} else {
	    IoFunction.showWarning(logger, "No valid targets");

	}
	logger.exiting(getClass().getName(), "action_Clone");
    }

    private void action_Remove(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Destroy", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	int countIvd = 0;
	int countVapp = 0;

	final LinkedList<FirstClassObject> fcoList = getFcoTarget(connetionManager, this.anyFcoOfType);

	if (fcoList.size() > 0) {

	    for (final FirstClassObject fco : fcoList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualMachineManager) {
			++countVm;
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			if (vmm.destroy()) {
			    ++success;
			} else {
			    ++failures;
			}
		    } else if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vApp = (VirtualAppManager) fco;
			++countVapp;
			if (vApp.destroy()) {
			    ++success;
			} else {
			    ++failures;
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			++countIvd;
			if (ivd.destroy()) {
			    ++success;
			} else {
			    ++failures;
			}

		    }
		}
	    }
	    IoFunction.println();
	    IoFunction.printTotal("%d vm:%d ivd:%d vApp:%d Success:%d fails:%d skip:%d", success + failures + skip,
		    countVm, countIvd, countVapp, success, failures, skip);
	    IoFunction.println();
	} else {
	    IoFunction.showWarning(logger, "No valid targets");

	}
	logger.exiting(getClass().getName(), "action_Destroy");
    }

    private void action_VappImport(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_VappImport", connetionManager);
	IoFunction.showInfo(logger, toString());
	connetionManager.importVapp(this.vim, this.urlPath, this.name, this.hostName, this.datastoreName,
		this.resourcePoolName, this.folderName);
	logger.exiting(getClass().getName(), "action_VappImport");
    }

    public Boolean getCbt() {
	return this.cbt;
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

    public String getHostName() {
	return this.hostName;
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

    public URL getUrlPath() {
	return this.urlPath;
    }

    public String getVappName() {
	return this.name;
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
	this.urlPath = null;
	this.name = null;
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
	this.vappImport = false;
	this.remove = false;
	this.cbt = false;
	this.vmNetworksName = new String[GlobalConfiguration.getMaxNumberOfVirtaulMachineNetworkCard()];
	for (int i = 0; i < this.vmNetworksName.length; i++) {
	    this.vmNetworksName[i] = "";
	}
    }

    public boolean isClone() {
	return this.clone;
    }

    public boolean isNoVmdk() {
	return this.isNoVmdk;
    }

    public boolean isPowerOn() {
	return this.powerOn;
    }

    public boolean isRemove() {
	return this.remove;
    }

    public boolean isVappImport() {
	return this.vappImport;
    }

    public void setCbt(final Boolean cbt) {
	this.cbt = cbt;
    }

    public void setClone(final boolean clone) {
	this.clone = clone;
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

    protected final void setHostName(final String hostName) {
	this.hostName = hostName;
    }

    public void setNoVmdk(final boolean isNoVmdk) {
	this.isNoVmdk = isNoVmdk;
    }

    public void setPowerOn(final boolean powerOn) {
	this.powerOn = powerOn;
    }

    public void setRemove(final boolean remove) {
	this.remove = remove;
    }

    public final void setResourcePoolName(final String resourcePoolName) {
	this.resourcePoolName = resourcePoolName;
    }

    /**
     * @param string
     * @throws MalformedURLException
     */
    public void setUrlPath(final String url) throws MalformedURLException {
	this.urlPath = new URL(url);

    }

    public void setUrlPath(final URL importUrl) {
	this.urlPath = importUrl;
    }

    public void setVappImport(final boolean vappImport) {
	this.vappImport = vappImport;
    }

    public void setVappName(final String vappName) {
	this.name = vappName;
    }

    public final void setVmNetworksName(final String st) {
	final String[] newvmNetworksName = st.split(",");
	for (int i = 0; i < newvmNetworksName.length; i++) {
	    this.vmNetworksName[i] = newvmNetworksName[i];
	}
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("ovfInfo: ");

	if (StringUtils.isNotEmpty(this.name)) {
	    sb.append(String.format("[vappName %s]", this.name));
	}
	if (getUrlPath() != null) {
	    sb.append(String.format("[Url %s]", getUrlPath().toString()));
	}
	if (StringUtils.isNotEmpty(getHostName())) {
	    sb.append(String.format("[host %s]", getHostName()));
	}
	if (StringUtils.isNotEmpty(getResourcePoolName())) {
	    sb.append(String.format("[Resource Pool %s]", getResourcePoolName()));
	}

	if (StringUtils.isNotEmpty(getDatastoreName())) {
	    sb.append(String.format("[datastore %s]", getDatastoreName()));
	}
	if (StringUtils.isNotEmpty(getDatacenterName())) {
	    sb.append(String.format("[datacenter %s]", getDatacenterName()));
	}
	if (StringUtils.isNotEmpty(getFolderName())) {
	    sb.append(String.format("[folder %s]", getFolderName()));
	}

	sb.append(String.format("[isNoVmdk %s]", PrettyBoolean.toString(this.isNoVmdk)));
	sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(this.isDryRun())));

	return sb.toString();
    }
}
