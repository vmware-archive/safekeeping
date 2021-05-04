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
package com.vmware.safekeeping.core.command.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.SearchManagementEntity;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.SearchManagementEntityInfoType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class CoreRestoreOptions extends CoreBackupRestoreCommonOptions {

    /**
     * 
     */
    private static final long serialVersionUID = -8864011183264349318L;

    private SearchManagementEntity datacenter;

    private SearchManagementEntity datastore;

    private SearchManagementEntity folder;

    private SearchManagementEntity resourcePool;

    private SearchManagementEntity host;

    private int generationId;

    private String name;

    private String prefix;
    private String postfix;
    private boolean powerOn;

    private boolean recover;
    private String resPoolFilter;
    private String vmFolderFilter;
    private final List<SearchManagementEntity> networks;
    private final List<CoreRestoreVmdkOption> disks;
    private IFirstClassObject parent;
    private boolean overwrite;
    private boolean importVmxFile;
    private boolean allowDuplicatedVmNamesInsideVapp;

    private SearchManagementEntity storageProfile;

    public void setStorageProfile(SearchManagementEntity storageProfile) {
        this.storageProfile = storageProfile;
    }

    public CoreRestoreOptions() {
        this.networks = new ArrayList<>(CoreGlobalSettings.MAX_NUMBER_OF_VIRTUAL_MACHINE_NETWORK_CARDS);
        this.disks = new ArrayList<>();
        /**
         * Default last generation
         */
        this.prefix = StringUtils.EMPTY;
        this.postfix = StringUtils.EMPTY;
        this.generationId = -1;
        this.overwrite = false;
        this.allowDuplicatedVmNamesInsideVapp = true;
        this.importVmxFile = false;
    }

    public CoreRestoreOptions(final CoreRestoreOptions r, final VirtualAppManager parent)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        this.parent = parent;
        this.datacenter = new SearchManagementEntity(SearchManagementEntityInfoType.MOREF,
                parent.getDatacenterInfo().getMorefValue());
        this.datastore = r.getDatastore();
        this.folder = new SearchManagementEntity(SearchManagementEntityInfoType.MOREF,
                parent.getParentFolder().getValue());

        this.generationId = r.getGenerationId();
        this.host = r.getHost();
        this.name = r.getName();
        this.postfix = r.getPostfix();
        this.prefix = r.getPrefix();
        this.powerOn = false;
        this.recover = r.isRecover();
        this.importVmxFile = r.isImportVmxFile();
        if (this.importVmxFile) {
            this.resourcePool = new SearchManagementEntity(SearchManagementEntityInfoType.MOREF,
                    parent.getParent().getValue());
            this.allowDuplicatedVmNamesInsideVapp = false;
        } else {
            this.resourcePool = new SearchManagementEntity(SearchManagementEntityInfoType.MOREF,
                    parent.getMorefValue());

            this.allowDuplicatedVmNamesInsideVapp = r.allowDuplicatedVmNamesInsideVapp;
        }
        this.resPoolFilter = r.getResPoolFilter();
        this.vmFolderFilter = r.getVmFolderFilter();
        storageProfile = r.getStorageProfile();
        disks = r.disks;
        this.networks = r.getNetworks();
        setDryRun(r.isDryRun());
        setNumberOfThreads(r.getNumberOfThreads());
        setNoVmdk(r.isNoVmdk());
        setRequestedTransportModes(r.getRequestedTransportModes());

        setForce(r.isForce());

    }

    /**
     * @return the datacenterName
     */
    public SearchManagementEntity getDatacenter() {
        return this.datacenter;
    }

    /**
     * @return the datastoreName
     */
    public SearchManagementEntity getDatastore() {
        return this.datastore;
    }

    /**
     * @return the folderName
     */
    public SearchManagementEntity getFolder() {
        return this.folder;
    }

    public int getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the hostName
     */
    public SearchManagementEntity getHost() {
        return this.host;
    }

    public String getName() {
        return this.name;
    }

    public IFirstClassObject getParent() {
        return this.parent;
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

    /**
     * @return the resourcePoolName
     */
    public SearchManagementEntity getResourcePool() {
        return this.resourcePool;
    }

    public String getResPoolFilter() {
        return this.resPoolFilter;
    }

    public String getVmFolderFilter() {
        return this.vmFolderFilter;
    }

    public List<SearchManagementEntity> getNetworks() {
        return this.networks;
    }

    public boolean isAllowDuplicatedVmNamesInsideVapp() {
        return this.allowDuplicatedVmNamesInsideVapp;
    }

    public boolean isImportVmxFile() {
        return this.importVmxFile;
    }

    /**
     * @return the overwrite
     */
    public boolean isOverwrite() {
        return this.overwrite;
    }

    public boolean isParentAVApp() {
        return (this.parent != null) && (this.parent.getEntityType() == EntityType.VirtualApp);
    }

    public boolean isPowerOn() {
        return this.powerOn;
    }

    public boolean isRecover() {
        return this.recover;
    }

    public void setAllowDuplicatedVmNamesInsideVapp(final boolean allowDuplicatedVmNamesInsideVapp) {
        this.allowDuplicatedVmNamesInsideVapp = allowDuplicatedVmNamesInsideVapp;
    }

    /**
     * @param datacenterName the datacenterName to set
     */
    public void setDatacenter(final SearchManagementEntity datacenterName) {
        this.datacenter = datacenterName;
    }

    /**
     * @param datastoreName the datastoreName to set
     */
    public void setDatastore(final SearchManagementEntity datastoreName) {
        this.datastore = datastoreName;
    }

    /**
     * @param folderName the folderName to set
     */
    public void setFolder(final SearchManagementEntity folderName) {
        this.folder = folderName;
    }

    public void setGenerationId(final int generationId) {
        this.generationId = generationId;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHost(final SearchManagementEntity hostName) {
        this.host = hostName;
    }

    public void setImportVmxFile(final boolean importVmxFile) {
        this.importVmxFile = importVmxFile;
    }

    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setParent(final IFirstClassObject parent) {
        this.parent = parent;
    }

    /**
     * @param postfix the postfix to set
     */
    public void setPostfix(final String postfix) {
        this.postfix = postfix;
    }

    public void setPowerOn(final boolean powerOn) {
        this.powerOn = powerOn;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setRecover(final boolean recover) {
        this.recover = recover;
    }

    /**
     * @param resourcePoolName the resourcePoolName to set
     */
    public void setResourcePool(final SearchManagementEntity resourcePoolName) {
        this.resourcePool = resourcePoolName;
    }

    public void setResPoolFilter(final String resPoolFilter) {
        this.resPoolFilter = resPoolFilter;
    }

    public void setVmFolderFilter(final String vmFolderFilter) {
        this.vmFolderFilter = vmFolderFilter;
    }

    public SearchManagementEntity getStorageProfile() {
        return storageProfile;
    }

    public List<CoreRestoreVmdkOption> getDisks() {
        return disks;
    }

}
