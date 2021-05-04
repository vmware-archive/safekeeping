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
package com.vmware.safekeeping.core.profile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VirtualMachineConfigSpec;

public class GenerationProfileSpec {
	private int genId;
	private int prevGenId;
	private final IFirstClassObject fco;
	private final Calendar calendar;
	private final CoreBackupOptions backupOptions;
	private String cbt;
	private final List<VmdkInfo> vmdkInfoList;

	VirtualMachineConfigSpec vmSpec;

	/**
	 * @param cal
	 * @param ivd
	 */
	public GenerationProfileSpec(final IFirstClassObject fco, final CoreBackupOptions coreBackupOptions,
			final Calendar cal) {
		this.fco = fco;
		this.backupOptions = coreBackupOptions;
		this.vmdkInfoList = new LinkedList<>();
		this.calendar = cal;
	}

	public CoreBackupOptions getBackupOptions() {
		return this.backupOptions;
	}

	public Calendar getCalendar() {
		return this.calendar;
	}

	public String getCbt() {
		return this.cbt;
	}

	public IFirstClassObject getFco() {
		return this.fco;
	}

	public FirstClassObjectType getFcoType() {
		return this.fco.getType();
	}

	public int getGenId() {
		return this.genId;
	}

	public String getManagedEntityInfoFullPath(final ManagedEntityInfo rpInfo)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
		return ManagedEntityInfo.composeEntityInfoName(getManagedEntityInfoPath(rpInfo));
	}

	public List<ManagedEntityInfo> getManagedEntityInfoPath(final ManagedEntityInfo rpInfo)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
		List<ManagedEntityInfo> result = null;
		switch (this.fco.getType()) {
		case ivd:
		case k8s:
			result = new ArrayList<>();
			break;
		case vapp:
			result = ((VirtualAppManager) this.fco).getManagedEntityInfoPath(rpInfo);
			break;
		case vm:
			result = ((VirtualMachineManager) this.fco).getManagedEntityInfoPath(rpInfo);
			break;
		}
		return result;
	}

	public ManagedEntityInfo getManageEntityInfo() {
		return this.fco.getManageEntityInfo();
	}

	/**
	 * @return
	 */
	public Integer getMaxBlockSize() {
		return this.backupOptions.getMaxBlockSize();
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

	public ManagedEntityInfo getResourcePoolInfo()
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
		switch (this.fco.getType()) {
		case vapp:
			return ((VirtualAppManager) this.fco).getResourcePoolInfo();
		case vm:
			return ((VirtualMachineManager) this.fco).getResourcePoolInfo();
		default:
			return null;
		}
	}

	public List<VmdkInfo> getVmdkInfoList() {
		return this.vmdkInfoList;
	}

	public ManagedEntityInfo getVmFolderInfo()
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
		switch (this.fco.getType()) {
		case vapp:
			return ((VirtualAppManager) this.fco).getVmFolderInfo();
		case vm:
			return ((VirtualMachineManager) this.fco).getVmFolderInfo();
		default:
			return null;
		}
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
