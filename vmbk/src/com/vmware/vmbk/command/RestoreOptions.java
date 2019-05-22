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

import com.vmware.vmbk.control.info.TotalDumpFileInfo;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FirstClassObject;

/**
 * @author mdaneri
 *
 */
public class RestoreOptions {
    static public RestoreOptions NewRestoreOptions(final RestoreCommand r) {
	final RestoreOptions result = new RestoreOptions();
	result.datacenterName = r.getDatacenterName();
	result.datastoreName = r.getDatastoreName();
	result.folderName = r.getFolderName();
	result.generationId = r.getGenerationId();
	result.hostName = r.getHostName();
	result.isNoVmdk = r.isNoVmdk();
	result.newName = r.getNewName();
	result.powerOn = r.isPowerOn();
	result.recover = r.isRecover();
	result.resourcePoolName = r.getResourcePoolName();
	result.resPoolFilter = r.getResPoolFilter();
	result.totalDumpsInfo = r.getTotalDumpsInfo();
	result.transportMode = r.getTransportMode();
	result.vmFolderFilter = r.getVmFolderFilter();

	result.vmNetworksName = r.getVmNetworksName();
	result.isDryRun = r.isDryRun();
	return result;
    }

    /**
     * @param restoreVApp
     * @return
     */
    public static RestoreOptions NewRestoreOptions(final RestoreOptions r, final FirstClassObject parent) {
	final RestoreOptions result = new RestoreOptions();
	result.datacenterName = r.getDatacenterName();
	result.datastoreName = r.getDatastoreName();
	result.folderName = r.getFolderName();
	result.generationId = r.getGenerationId();
	result.hostName = r.getHostName();
	result.isNoVmdk = r.isNoVmdk();
	result.newName = r.getNewName();
	result.powerOn = r.isPowerOn();
	result.recover = r.isRecover();
	result.resourcePoolName = r.getResourcePoolName();
	result.resPoolFilter = r.getResPoolFilter();
	result.totalDumpsInfo = r.getTotalDumpsInfo();
	result.transportMode = r.getTransportMode();
	result.vmFolderFilter = r.getVmFolderFilter();

	result.vmNetworksName = r.getVmNetworksName();
	result.isDryRun = r.isDryRun();
	result.parent = parent;
	return result;
    }

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

    private boolean isDryRun;

    private FirstClassObject parent;

    public void add(final TotalDumpFileInfo e) {
	this.totalDumpsInfo.add(e);
    }

    public String getDatacenterName() {
	return this.datacenterName;
    }

    public String getDatastoreName() {
	return this.datastoreName;
    }

    public String getFolderName() {
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

    public FirstClassObject getParent() {
	return this.parent;
    }

    public String getResourcePoolName() {
	return this.resourcePoolName;
    }

    public String getResPoolFilter() {
	return this.resPoolFilter;
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

    public boolean isDryRun() {
	return this.isDryRun;
    }

    public boolean isNoVmdk() {
	return this.isNoVmdk;
    }

    public boolean isParentAVApp() {
	if (this.parent == null) {
	    return false;
	}
	return this.parent.getEntityType() == EntityType.VirtualApp;
    }

    public boolean isPowerOn() {
	return this.powerOn;
    }

    public boolean isRecover() {
	return this.recover;
    }

    public void setDatacenterName(final String datacenterName) {
	this.datacenterName = datacenterName;
    }

    public void setDatastoreName(final String datastoreName) {
	this.datastoreName = datastoreName;
    }

    public void setDryRun(final boolean isDryRun) {
	this.isDryRun = isDryRun;
    }

    public void setFolderName(final String folderName) {
	this.folderName = folderName;
    }

    public void setGenerationId(final int generationId) {
	this.generationId = generationId;
    }

    public void setHostName(final String hostName) {
	this.hostName = hostName;
    }

    public void setNewName(final String newName) {
	this.newName = newName;
    }

    public void setNoVmdk(final boolean isNoVmdk) {
	this.isNoVmdk = isNoVmdk;
    }

    public void setParent(final FirstClassObject parent) {
	this.parent = parent;
    }

    public void setPowerOn(final boolean powerOn) {
	this.powerOn = powerOn;
    }

    public void setRecover(final boolean recover) {
	this.recover = recover;
    }

    public void setResourcePoolName(final String resourcePoolName) {
	this.resourcePoolName = resourcePoolName;
    }

    public void setResPoolFilter(final String resPoolFilter) {
	this.resPoolFilter = resPoolFilter;
    }

    public void setTotalDumpsInfo(final ArrayList<TotalDumpFileInfo> totalDumpsInfo) {
	this.totalDumpsInfo = totalDumpsInfo;
    }

    public void setTransportMode(final String transportMode) {
	this.transportMode = transportMode;
    }

    public void setVmFolderFilter(final String vmFolderFilter) {
	this.vmFolderFilter = vmFolderFilter;
    }

    public void setVmNetworksName(final String[] vmNetworksName) {
	this.vmNetworksName = vmNetworksName;
    }
}
