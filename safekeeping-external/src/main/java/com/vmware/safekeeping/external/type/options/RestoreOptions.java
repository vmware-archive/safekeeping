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
package com.vmware.safekeeping.external.type.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.options.CoreRestoreVmdkOption;
import com.vmware.safekeeping.core.type.SearchManagementEntity;

public class RestoreOptions extends BackupRestoreCommonOptions {
    public static void convert(final RestoreOptions src, final CoreRestoreOptions dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        BackupRestoreCommonOptions.convert(src, dst, FirstClassObjectFilterType.any);
        dst.setDatacenter(src.datacenter);
        dst.setDatastore(src.datastore);
        dst.setFolder(src.folder);
        if (src.generationId != null) {
            dst.setGenerationId(src.generationId);
        }
        dst.setHost(src.host);
        dst.setNoVmdk(src.isNoVmdk());
        dst.setName(src.name);
        if (StringUtils.isNotEmpty(src.prefix)) {
            dst.setPrefix(src.prefix);
        }
        if (StringUtils.isNotEmpty(src.postfix)) {
            dst.setPostfix(src.postfix);
        }
        dst.setPowerOn(src.powerOn);
        dst.setRecover(src.recover);
        dst.setResourcePool(src.resourcePool);
        dst.setResPoolFilter(src.resPoolFilter);
        dst.setVmFolderFilter(src.vmFolderFilter);
        if (src.networks != null) {
            dst.getNetworks().addAll(src.networks);
        }
        if (src.disks != null) {
            for (RestoreVmdkOption disk : src.disks) {
                CoreRestoreVmdkOption coreDisk = new CoreRestoreVmdkOption();
                RestoreVmdkOption.convert(disk, coreDisk);
                dst.getDisks().add(coreDisk);
            }
        }
        dst.setStorageProfile(src.storageProfile);
        dst.setOverwrite(src.overwrite);
        dst.setImportVmxFile(src.importVmxFile);
        if (src.allowDuplicatedVmNamesInsideVapp != null) {
            dst.setAllowDuplicatedVmNamesInsideVapp(src.allowDuplicatedVmNamesInsideVapp);
        }
        if (StringUtils.isNotEmpty(src.requestedTransportMode)) {
            dst.setRequestedTransportModes(src.requestedTransportMode);
        }

    }

    private String requestedTransportMode;

    private SearchManagementEntity datacenter;

    private SearchManagementEntity datastore;

    private SearchManagementEntity folder;

    private Integer generationId;

    private SearchManagementEntity host;

    private boolean isNoVmdk;

    private String name;

    private String prefix;

    private String postfix;

    private boolean powerOn;

    private boolean recover;

    private SearchManagementEntity resourcePool;

    private String resPoolFilter;

    private String vmFolderFilter;

    private List<SearchManagementEntity> networks;
    private List<RestoreVmdkOption> disks;

    private SearchManagementEntity storageProfile;
    private boolean overwrite;

    private boolean importVmxFile;

    private Boolean allowDuplicatedVmNamesInsideVapp;

    public RestoreOptions() {
        this.networks = new ArrayList<>();
        disks = new ArrayList<>();
        this.prefix = StringUtils.EMPTY;
        this.postfix = StringUtils.EMPTY;
    }

    /**
     * @return the datacenter
     */
    public SearchManagementEntity getDatacenter() {
        return this.datacenter;
    }

    /**
     * @return the datastore
     */
    public SearchManagementEntity getDatastore() {
        return this.datastore;
    }

    /**
     * @return the folder
     */
    public SearchManagementEntity getFolder() {
        return this.folder;
    }

    /**
     * @return the generationId
     */
    public Integer getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the host
     */
    public SearchManagementEntity getHost() {
        return this.host;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the networks
     */
    public List<SearchManagementEntity> getNetworks() {
        return this.networks;
    }

    /**
     * @return the postfix
     */
    public String getPostfix() {
        return this.postfix;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    public String getRequestedTransportMode() {
        return this.requestedTransportMode;
    }

    /**
     * @return the resourcePool
     */
    public SearchManagementEntity getResourcePool() {
        return this.resourcePool;
    }

    /**
     * @return the resPoolFilter
     */
    public String getResPoolFilter() {
        return this.resPoolFilter;
    }

    public SearchManagementEntity getStorageProfile() {
        return this.storageProfile;
    }

    /**
     * @return the vmFolderFilter
     */
    public String getVmFolderFilter() {
        return this.vmFolderFilter;
    }

    public Boolean isAllowDuplicatedVmNamesInsideVapp() {
        return this.allowDuplicatedVmNamesInsideVapp;
    }

    public boolean isImportVmxFile() {
        return this.importVmxFile;
    }

    /**
     * @return the isNoVmdk
     */
    @Override
    public boolean isNoVmdk() {
        return this.isNoVmdk;
    }

    /**
     * @return the overwrite
     */
    public boolean isOverwrite() {
        return this.overwrite;
    }

    /**
     * @return the powerOn
     */
    public boolean isPowerOn() {
        return this.powerOn;
    }

    /**
     * @return the recover
     */
    public boolean isRecover() {
        return this.recover;
    }

    public void setAllowDuplicatedVmNamesInsideVapp(final Boolean allowDuplicatedVmNamesInsideVapp) {
        this.allowDuplicatedVmNamesInsideVapp = allowDuplicatedVmNamesInsideVapp;
    }

    /**
     * @param datacenter the datacenter to set
     */
    public void setDatacenter(final SearchManagementEntity datacenter) {
        this.datacenter = datacenter;
    }

    /**
     * @param datastore the datastore to set
     */
    public void setDatastore(final SearchManagementEntity datastore) {
        this.datastore = datastore;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(final SearchManagementEntity folder) {
        this.folder = folder;
    }

    /**
     * @param generationId the generationId to set
     */
    public void setGenerationId(final Integer generationId) {
        this.generationId = generationId;
    }

    /**
     * @param host the host to set
     */
    public void setHost(final SearchManagementEntity host) {
        this.host = host;
    }

    public void setImportVmxFile(final boolean importVmxFile) {
        this.importVmxFile = importVmxFile;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param networks the networks to set
     */
    public void setNetworks(final List<SearchManagementEntity> networks) {
        this.networks = networks;
    }

    /**
     * @param isNoVmdk the isNoVmdk to set
     */
    @Override
    public void setNoVmdk(final boolean isNoVmdk) {
        this.isNoVmdk = isNoVmdk;
    }

    /**
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * @param postfix the postfix to set
     */
    public void setPostfix(final String postfix) {
        this.postfix = postfix;
    }

    /**
     * @param powerOn the powerOn to set
     */
    public void setPowerOn(final boolean powerOn) {
        this.powerOn = powerOn;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    /**
     * @param recover the recover to set
     */
    public void setRecover(final boolean recover) {
        this.recover = recover;
    }

    public void setRequestedTransportMode(final String requestedTransportMode) {
        this.requestedTransportMode = requestedTransportMode;
    }

    /**
     * @param resourcePool the resourcePool to set
     */
    public void setResourcePool(final SearchManagementEntity resourcePool) {
        this.resourcePool = resourcePool;
    }

    /**
     * @param resPoolFilter the resPoolFilter to set
     */
    public void setResPoolFilter(final String resPoolFilter) {
        this.resPoolFilter = resPoolFilter;
    }

    public void setStorageProfile(final SearchManagementEntity storageProfile) {
        this.storageProfile = storageProfile;
    }

    /**
     * @param vmFolderFilter the vmFolderFilter to set
     */
    public void setVmFolderFilter(final String vmFolderFilter) {
        this.vmFolderFilter = vmFolderFilter;
    }

    public List<RestoreVmdkOption> getDisks() {
        return disks;
    }

    public void setDisks(List<RestoreVmdkOption> disks) {
        this.disks = disks;
    }

}
