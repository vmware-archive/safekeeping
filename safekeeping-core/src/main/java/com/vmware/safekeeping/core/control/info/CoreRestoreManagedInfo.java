/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
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
package com.vmware.safekeeping.core.control.info;

import java.util.HashMap;
import java.util.Map;

import com.vmware.safekeeping.core.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.safekeeping.core.profile.ovf.SerializableVmConfigInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;

public class CoreRestoreManagedInfo {

    private ManagedEntityInfo dcInfo;
    private ManagedEntityInfo datastoreInfo;
    private ManagedEntityInfo folderInfo;
    private ManagedEntityInfo hostInfo;
    private String name;
    private final ManagedEntityInfo[] networkMapping;
    private boolean recovery;
    private ManagedEntityInfo resourcePollInfo;
    private VirtualMachineDefinedProfileSpec spbmProfile;
    private final Map<Integer, CoreRestoreVmdkManagedInfo> vmdksManagedInfo;

    private SerializableVAppConfigInfo vAppConfig;
    private ResourceConfigSpec resourceConfigSpec;

    public CoreRestoreManagedInfo(final int maxNetworks) {
        this.name = null;
        this.recovery = false;
        this.hostInfo = null;
        this.dcInfo = null;
        this.datastoreInfo = null;
        this.folderInfo = null;
        this.resourcePollInfo = null;
        this.vmdksManagedInfo = new HashMap<>();
        this.vAppConfig = null;
        this.networkMapping = new ManagedEntityInfo[maxNetworks];
        this.resourceConfigSpec = null;
    }

    public ManagedEntityInfo getDatastoreInfo() {
        return this.datastoreInfo;
    }

    public ManagedEntityInfo getDcInfo() {
        return this.dcInfo;
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

    public ManagedEntityInfo[] getNetworkMapping() {
        return this.networkMapping;
    }

    public ResourceConfigSpec getResourceConfigSpec() {
        return this.resourceConfigSpec;
    }

    public ManagedEntityInfo getResourcePollInfo() {
        return this.resourcePollInfo;
    }

    public VirtualMachineDefinedProfileSpec getSpbmProfile() {
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

    public void setDatastoreInfo(final ManagedEntityInfo dsInfo) {
        this.datastoreInfo = dsInfo;
    }

    public void setDcInfo(final ManagedEntityInfo dcInfo) {
        this.dcInfo = dcInfo;
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

    public void setResourcePollInfo(final ManagedEntityInfo rpInfo) {
        this.resourcePollInfo = rpInfo;
    }

    public void setSpbmProfile(final VirtualMachineDefinedProfileSpec spbmProfile) {
        this.spbmProfile = spbmProfile;
    }

    public void setVAppConfig(final SerializableVAppConfigInfo vAppConfig) {
        this.vAppConfig = vAppConfig;
    }

    public void setVAppConfig(final SerializableVmConfigInfo vAppConfig) {
        if (vAppConfig != null) {
            this.vAppConfig = new SerializableVAppConfigInfo(vAppConfig);
        }
    }

    public Map<Integer, CoreRestoreVmdkManagedInfo> getVmdksManagedInfo() {
        return vmdksManagedInfo;
    }

}
