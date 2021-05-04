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
package com.vmware.safekeeping.core.command.results.connectivity;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.VMwareCloudPlatforms;

abstract class AbstractCoreResultActionConnectDisconnectVcenter extends AbstractCoreResultActionImpl {
    /**
     * 
     */
    private static final long serialVersionUID = -7426613852603896218L;
    private String instanceUuid;
    private String url;
    private String name;
    private String api;

    private String pbmUrl;

    private String pbmVersion;

    private String vapiUrl;

    private String vslmName;

    private String vslmUrl;

    private boolean vapiConnected;

    private boolean vslmConnected;

    private boolean useVslm;

    private boolean pbmConnected;
    private boolean vimConnected;
    private VMwareCloudPlatforms cloudPlatform;

    AbstractCoreResultActionConnectDisconnectVcenter() {
        setFcoEntityInfo(ManagedFcoEntityInfo.newNullManagedEntityInfo());
    }

    /**
     * @param rac
     */
    AbstractCoreResultActionConnectDisconnectVcenter(final ICoreResultAction parent) {
        super(parent);
        setFcoEntityInfo(ManagedFcoEntityInfo.newNullManagedEntityInfo());
    }

    public String getApi() {
        return this.api;
    }

    public String getInstanceUuid() {
        return this.instanceUuid;
    }

    public String getName() {
        return this.name;
    }

    public String getPbmUrl() {
        return this.pbmUrl;
    }

    public String getPbmVersion() {
        return this.pbmVersion;
    }

    public String getUrl() {
        return this.url;
    }

    public String getVapiUrl() {
        return this.vapiUrl;
    }

    public String getVslmName() {
        return this.vslmName;
    }

    public String getVslmUrl() {
        return this.vslmUrl;
    }

    public boolean isPbmConnected() {
        return this.pbmConnected;
    }

    public boolean isUseVslm() {
        return this.useVslm;
    }

    public boolean isVapiConnected() {
        return this.vapiConnected;
    }

    /**
     * @return the vimConnected
     */
    public boolean isVimConnected() {
        return this.vimConnected;
    }

    public boolean isVslmConnected() {
        return this.vslmConnected;
    }

    public void setApi(final String api) {
        this.api = api;
    }

    public void setInstanceUuid(final String instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPbmConnected(final boolean pbmConnected) {
        this.pbmConnected = pbmConnected;
    }

    public void setPbmUrl(final String pbmUrl) {
        this.pbmUrl = pbmUrl;
    }

    public void setPbmVersion(final String pbmVersion) {
        this.pbmVersion = pbmVersion;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setUseVslm(final boolean useVslm) {
        this.useVslm = useVslm;
    }

    public void setVapiConnected(final boolean vapiConnected) {
        this.vapiConnected = vapiConnected;
    }

    public void setVapiUrl(final String vapiUrl) {
        this.vapiUrl = vapiUrl;
    }

    /**
     * @param vimConnected the vimConnected to set
     */
    public void setVimConnected(final boolean vimConnected) {
        this.vimConnected = vimConnected;
    }

    public void setVslmConnected(final boolean vslmConnected) {
        this.vslmConnected = vslmConnected;
    }

    public void setVslmName(final String vslmName) {
        this.vslmName = vslmName;
    }

    public void setVslmUrl(final String vslmUrl) {
        this.vslmUrl = vslmUrl;
    }

    public VMwareCloudPlatforms getCloudPlatform() {
        return cloudPlatform;
    }

    public void setCloudPlatform(VMwareCloudPlatforms platform) {
        this.cloudPlatform = platform;
    }

}
