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
package com.vmware.vmbk.profile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.command.BackupCommand;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.FirstClassObjectType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.VmdkInfo;
import com.vmware.vmbk.util.Utility;

public class GenerationProfileSpec {
    private int genId;
    private int prevGenId;
    private final FirstClassObject fco;
    private Calendar calendar;
    private final BackupCommand backupInfo;
    private String cbt;
    final private List<VmdkInfo> vmdkInfoList;

    /**
     * @param ivd
     */
    public GenerationProfileSpec(final FirstClassObject fco, final BackupCommand backupInfo) {
	this.fco = fco;
	this.backupInfo = backupInfo;
	this.vmdkInfoList = new LinkedList<>();
    }

    public BackupCommand getBackupInfo() {
	return this.backupInfo;
    }

    public Calendar getCalendar() {
	return this.calendar;
    }

    public String getCbt() {
	return this.cbt;
    }

//    public ManagedEntityInfo getDatastore() {
//	return this.fco.getDatastoreInfo();
//    }

    public FirstClassObject getFco() {
	return this.fco;
    }

    public FirstClassObjectType getFcoType() {
	return this.fco.getType();
    }

    public int getGenId() {
	return this.genId;
    }

    /**
     * @return
     */
    public String getLocationString() {
	String result = StringUtils.EMPTY;
	if (this.backupInfo != null) {
	    switch (this.fco.getType()) {
	    case k8s:
		break;
	    case ivd:
		result = String.format("Location: [Datastore %s]",
			((ImprovedVirtuaDisk) this.fco).getDatastoreInfo().getName());
		break;
	    case vapp:
		result = String.format("Location: [ResourcePool %s] [vmFolder %s]", getResourcePoolFullPath(),
			getVmFolderFullPath());
		break;
	    case vm:
		result = String.format("Location: [ResourcePool %s][Datastore %s][vmFolder %s]",
			getResourcePoolFullPath(), ((VirtualMachineManager) this.fco).getDatastoreInfo().getName(),
			getVmFolderFullPath());
		break;
	    }
	}
	return result;
    }

    public ManagedEntityInfo getManageEntityInfo() {
	return this.fco.getManageEntityInfo();
    }

    /**
     * @return
     */
    public Integer getNumberOfDisks() {
	return this.vmdkInfoList.size();
    }

    public int getPrevGenId() {
	return this.prevGenId;
    }

    public String getResourcePoolFullPath() {
	return Utility.composeEntityInfoName(getResourcePoolPath());
    }

    public List<ManagedEntityInfo> getResourcePoolPath() {
	List<ManagedEntityInfo> result = null;
	switch (this.fco.getType()) {
	case ivd:
	case k8s:
	    result = new ArrayList<>();
	    break;
	case vapp:
	    result = ((VirtualAppManager) this.fco).getResourcePoolPath();
	    break;
	case vm:
	    result = ((VirtualMachineManager) this.fco).getResourcePoolPath();
	    break;
	}
	return result;
    }

    public List<VmdkInfo> getVmdkInfoList() {
	return this.vmdkInfoList;
    }

    public String getVmFolderFullPath() {
	return Utility.composeEntityInfoName(getVmFolderPath());
    }

    public List<ManagedEntityInfo> getVmFolderPath() {
	List<ManagedEntityInfo> result = null;
	switch (this.fco.getType()) {
	case ivd:
	case k8s:
	    result = new ArrayList<>();
	    break;
	case vapp:
	    result = ((VirtualAppManager) this.fco).getVmFolderPath();
	    break;
	case vm:
	    result = ((VirtualMachineManager) this.fco).getVmFolderPath();
	    break;
	}
	return result;
    }

    public void setCalendar(final Calendar calendar) {
	this.calendar = calendar;
    }

    public void setCbt(final String forcedCbt) {
	this.cbt = forcedCbt;
    }

    public void setGenId(final int genId) {
	this.genId = genId;
    }

    public void setPrevGenId(final int prevGenId) {
	this.prevGenId = prevGenId;
    }

}
