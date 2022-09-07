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
package com.vmware.safekeeping.external.type;

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.location.CoreVappLocation;

public class VappLocation extends FcoLocation {
	static public void convertFrom(final CoreVappLocation src, final VappLocation dst) {
		if ((src == null) || (dst == null)) {
			return;
		}
		FcoLocation.convertFrom(src, dst);
		dst.setResourcePoolFullPath(src.getResourcePoolFullPath());

		dst.setVmFolderFullPath(src.getVmFolderFullPath());

		dst.setvAppMember(src.isVAppMember());
		dst.setResourcePoolFullPath(src.getResourcePoolFullPath());

		dst.setResourcePoolPath(src.getResourcePoolPath());
		dst.setVmFolderPath(src.getVmFolderPath());
	}

	private List<ManagedEntityInfo> resourcePoolPath;

	private ManagedEntityInfo resourcePoolInfo;
	private ManagedEntityInfo folderInfo;
	private List<ManagedEntityInfo> vmFolderPath;
	private String resourcePoolFullPath;
	private String vmFolderFullPath;
	private boolean vAppMember;

	public ManagedEntityInfo getFolderInfo() {
		return this.folderInfo;
	}

	/**
	 * @return the resourcePoolFullPath
	 */
	public String getResourcePoolFullPath() {
		return this.resourcePoolFullPath;
	}

	public ManagedEntityInfo getResourcePoolInfo() {
		return this.resourcePoolInfo;
	}

	public List<ManagedEntityInfo> getResourcePoolPath() {
		return this.resourcePoolPath;
	}

	/**
	 * @return the vmFolderFullPath
	 */
	public String getVmFolderFullPath() {
		return this.vmFolderFullPath;
	}

	public List<ManagedEntityInfo> getVmFolderPath() {
		return this.vmFolderPath;
	}

	public boolean isvAppMember() {
		return this.vAppMember;
	}

	public void setFolderInfo(final ManagedEntityInfo vmFolderInfo) {
		this.folderInfo = vmFolderInfo;
	}

	/**
	 * @param resourcePoolFullPath the resourcePoolFullPath to set
	 */
	public void setResourcePoolFullPath(final String resourcePoolFullPath) {
		this.resourcePoolFullPath = resourcePoolFullPath;
	}

	public void setResourcePoolInfo(final ManagedEntityInfo resourcePoolInfo) {
		this.resourcePoolInfo = resourcePoolInfo;
	}

	/**
	 * @param resourcePoolPath
	 */
	public void setResourcePoolPath(final ArrayList<ManagedEntityInfo> resourcePoolPath) {
		this.resourcePoolPath = resourcePoolPath;

	}

	/**
	 * @param resourcePoolPath the resourcePoolPath to set
	 */
	public void setResourcePoolPath(final List<ManagedEntityInfo> resourcePoolPath) {
		this.resourcePoolPath = resourcePoolPath;
	}

	public void setvAppMember(final boolean isAMember) {
		this.vAppMember = isAMember;
	}

	/**
	 * @param vmFolderFullPath the vmFolderFullPath to set
	 */
	public void setVmFolderFullPath(final String vmFolderFullPath) {
		this.vmFolderFullPath = vmFolderFullPath;
	}

	/**
	 * @param vmFolderPath
	 */
	public void setVmFolderPath(final List<ManagedEntityInfo> vmFolderPath) {
		this.vmFolderPath = vmFolderPath;

	}
}