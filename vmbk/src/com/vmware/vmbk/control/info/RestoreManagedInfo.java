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
package com.vmware.vmbk.control.info;

import java.util.LinkedList;
import java.util.List;

import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vmbk.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.vmbk.profile.ovf.SerializableVmConfigInfo;
import com.vmware.vmbk.type.ManagedEntityInfo;

public class RestoreManagedInfo {
    private ManagedEntityInfo dcInfo;
    private String directoryName;
    private ManagedEntityInfo dsInfo;
    private ManagedEntityInfo folderInfo;
    private ManagedEntityInfo hostInfo;
    private String name;
    public String[] networkMapping;
    private boolean recovery;
    private ManagedEntityInfo rpInfo;
    private final List<VirtualMachineDefinedProfileSpec> spbmProfile;
    private SerializableVAppConfigInfo vAppConfig;
    private ResourceConfigSpec resourceConfigSpec;
    // private MyVmConfigInfo vmConfigInfo;

    public RestoreManagedInfo(final int maxNetworks) {
	this.name = null;
	this.directoryName = null;
	this.recovery = false;
	this.hostInfo = null;
	this.dcInfo = null;
	this.dsInfo = null;
	this.folderInfo = null;
	this.rpInfo = null;
	this.spbmProfile = new LinkedList<>();
	this.vAppConfig = null;
	// this.vmConfigInfo = null;
	this.networkMapping = new String[maxNetworks];
	this.resourceConfigSpec = null;
    }

    public ManagedEntityInfo getDcInfo() {
	return this.dcInfo;
    }

    public String getDirectoryName() {
	return this.directoryName;
    }

    public ManagedEntityInfo getDsInfo() {
	return this.dsInfo;
    }

    public ManagedEntityInfo getFolderInfo() {
	return this.folderInfo;
    }

    public ManagedEntityInfo getHostInfo() {
	return this.hostInfo;
    }

    public String getName() {
	return this.name;
    }

    public ResourceConfigSpec getResourceConfigSpec() {
	return this.resourceConfigSpec;
    }

    public ManagedEntityInfo getRpInfo() {
	return this.rpInfo;
    }

    public List<VirtualMachineDefinedProfileSpec> getSpbmProfile() {
	return this.spbmProfile;
    }

    /**
     * Configuration of a vApp
     *
     * @return
     */
    public SerializableVAppConfigInfo getVAppConfig() {
	return this.vAppConfig;
    }

    public boolean isRecovery() {
	return this.recovery;
    }

    public void setDcInfo(final ManagedEntityInfo dcInfo) {
	this.dcInfo = dcInfo;
    }

    public void setDirectoryName(final String directoryName) {
	this.directoryName = directoryName;
    }

    public void setDsInfo(final ManagedEntityInfo dsInfo) {
	this.dsInfo = dsInfo;
    }

    public void setFolderInfo(final ManagedEntityInfo folderInfo) {
	this.folderInfo = folderInfo;
    }

    public void setHostInfo(final ManagedEntityInfo hostInfo) {
	this.hostInfo = hostInfo;
    }

    public void setName(final String vmName) {
	this.name = vmName;
    }

    public void setRecovery(final boolean recovery) {
	this.recovery = recovery;
    }

    public void setResourceConfigSpec(final ResourceConfigSpec resourceConfigSpec) {
	this.resourceConfigSpec = resourceConfigSpec;
    }

    public void setRpInfo(final ManagedEntityInfo rpInfo) {
	this.rpInfo = rpInfo;
    }

//    public void setSpbmProfile(final List<VirtualMachineDefinedProfileSpec> spbmProfile) {
//	this.spbmProfile = spbmProfile;
//    }

    public void setVAppConfig(final SerializableVAppConfigInfo vAppConfig) {
	this.vAppConfig = vAppConfig;
    }

    public void setVAppConfig(final SerializableVmConfigInfo vAppConfig) {
	this.vAppConfig = new SerializableVAppConfigInfo(vAppConfig);
    }

}
