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
package com.vmware.safekeeping.external.type;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;

/**
 * @author mdaneri
 *
 */
public class RestoreVmManagedInfo extends RestoreManagedInfo {

    public static void convert(final CoreRestoreManagedInfo src, final RestoreVmManagedInfo dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        RestoreManagedInfo.convert(src, dst);
        dst.setDsInfo(src.getDatastoreInfo());
        dst.setHostInfo(src.getHostInfo());

        dst.setResourcePoolInfo(src.getResourcePollInfo());
        dst.setFolderInfo(src.getFolderInfo());

        dst.getNetworkMapping().addAll(Arrays.asList(src.getNetworkMapping()));

        dst.setvAppConfig(src.getVAppConfig());

    }

    private ManagedEntityInfo dsInfo;

    private ManagedEntityInfo hostInfo;

    private List<ManagedEntityInfo> networkMapping;

    private SerializableVAppConfigInfo vAppConfig;

    private ManagedEntityInfo folderInfo;

    private ManagedEntityInfo resourcePoolInfo;

    public RestoreVmManagedInfo() {
        this.networkMapping = new LinkedList<>();
    }

    /**
     * @return the dsInfo
     */
    public ManagedEntityInfo getDsInfo() {
        return this.dsInfo;
    }

    /**
     * @return the folderInfo
     */
    public ManagedEntityInfo getFolderInfo() {
        return this.folderInfo;
    }

    /**
     * @return the hostInfo
     */
    public ManagedEntityInfo getHostInfo() {
        return this.hostInfo;
    }

    /**
     * @return the networkMapping
     */
    public List<ManagedEntityInfo> getNetworkMapping() {
        return this.networkMapping;
    }

    /**
     * @return the resourcePoolInfo
     */
    public ManagedEntityInfo getResourcePoolInfo() {
        return this.resourcePoolInfo;
    }

    /**
     * @return the vAppConfig
     */
    public SerializableVAppConfigInfo getvAppConfig() {
        return this.vAppConfig;
    }

    /**
     * @param dsInfo the dsInfo to set
     */
    public void setDsInfo(final ManagedEntityInfo dsInfo) {
        this.dsInfo = dsInfo;
    }

    /**
     * @param folderInfo the folderInfo to set
     */
    public void setFolderInfo(final ManagedEntityInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    /**
     * @param hostInfo the hostInfo to set
     */
    public void setHostInfo(final ManagedEntityInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * @param networkMapping the networkMapping to set
     */
    public void setNetworkMapping(final List<ManagedEntityInfo> networkMapping) {
        this.networkMapping = networkMapping;
    }

    /**
     * @param resourcePoolInfo the resourcePoolInfo to set
     */
    public void setResourcePoolInfo(final ManagedEntityInfo resourcePoolInfo) {
        this.resourcePoolInfo = resourcePoolInfo;
    }

    /**
     * @param vAppConfig the vAppConfig to set
     */
    public void setvAppConfig(final SerializableVAppConfigInfo vAppConfig) {
        this.vAppConfig = vAppConfig;
    }
}
