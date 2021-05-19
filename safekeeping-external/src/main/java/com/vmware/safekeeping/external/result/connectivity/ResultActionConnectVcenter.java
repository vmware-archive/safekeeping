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
package com.vmware.safekeeping.external.result.connectivity;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectVcenter;
import com.vmware.safekeeping.core.type.enums.VMwareCloudPlatforms;
import com.vmware.safekeeping.external.result.ResultAction;

public class ResultActionConnectVcenter extends ResultAction {
    public String getResourcePoolFilter() {
        return resourcePoolFilter;
    }

    public void setResourcePoolFilter(String resourcePoolFilter) {
        this.resourcePoolFilter = resourcePoolFilter;
    }

    public String getVmFolderFilter() {
        return vmFolderFilter;
    }

    public void setVmFolderFilter(String vmFolderFilter) {
        this.vmFolderFilter = vmFolderFilter;
    }

    /**
     * @param dst
     * @return
     */
    public static void convert(final CoreResultActionConnectVcenter src, final ResultActionConnectVcenter dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);
        dst.setInstanceUuid(src.getInstanceUuid());
        dst.setUrl(src.getUrl());
        dst.setName(src.getName());
        dst.setApi(src.getApi());
        dst.setPbmUrl(src.getPbmUrl());
        dst.setPbmVersion(src.getPbmVersion());
        dst.setVapiUrl(src.getVapiUrl());
        dst.setVslmName(src.getVslmName());
        dst.setVslmUrl(src.getVslmUrl());
        dst.setVapiConnected(src.isVapiConnected());
        dst.setVslmConnected(src.isVslmConnected());
        dst.setPbmConnected(src.isPbmConnected());
        dst.vmFolderFilter = src.getVmFolderFilter();
        dst.resourcePoolFilter = src.getResourcePoolFilter();
        dst.cloudPlatform = src.getCloudPlatform();
    }

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

    private boolean pbmConnected;
    private VMwareCloudPlatforms cloudPlatform;
    private String resourcePoolFilter;
    private String vmFolderFilter;

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionConnectVcenter.convert((CoreResultActionConnectVcenter) src, this);
    }

    /**
     * @return the api
     */
    public String getApi() {
        return this.api;
    }

    /**
     * @return the instanceUuid
     */
    public String getInstanceUuid() {
        return this.instanceUuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the pbmUrl
     */
    public String getPbmUrl() {
        return this.pbmUrl;
    }

    /**
     * @return the pbmVersion
     */
    public String getPbmVersion() {
        return this.pbmVersion;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return the vapiUrl
     */
    public String getVapiUrl() {
        return this.vapiUrl;
    }

    /**
     * @return the vslmName
     */
    public String getVslmName() {
        return this.vslmName;
    }

    /**
     * @return the vslmUrl
     */
    public String getVslmUrl() {
        return this.vslmUrl;
    }

    /**
     * @return the pbmConnected
     */
    public boolean isPbmConnected() {
        return this.pbmConnected;
    }

    /**
     * @return the vapiConnected
     */
    public boolean isVapiConnected() {
        return this.vapiConnected;
    }

    /**
     * @return the vslmConnected
     */
    public boolean isVslmConnected() {
        return this.vslmConnected;
    }

    /**
     * @param api the api to set
     */
    public void setApi(final String api) {
        this.api = api;
    }

    /**
     * @param instanceUuid the instanceUuid to set
     */
    public void setInstanceUuid(final String instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param pbmConnected the pbmConnected to set
     */
    public void setPbmConnected(final boolean pbmConnected) {
        this.pbmConnected = pbmConnected;
    }

    /**
     * @param pbmUrl the pbmUrl to set
     */
    public void setPbmUrl(final String pbmUrl) {
        this.pbmUrl = pbmUrl;
    }

    /**
     * @param pbmVersion the pbmVersion to set
     */
    public void setPbmVersion(final String pbmVersion) {
        this.pbmVersion = pbmVersion;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * @param vapiConnected the vapiConnected to set
     */
    public void setVapiConnected(final boolean vapiConnected) {
        this.vapiConnected = vapiConnected;
    }

    /**
     * @param vapiUrl the vapiUrl to set
     */
    public void setVapiUrl(final String vapiUrl) {
        this.vapiUrl = vapiUrl;
    }

    /**
     * @param vslmConnected the vslmConnected to set
     */
    public void setVslmConnected(final boolean vslmConnected) {
        this.vslmConnected = vslmConnected;
    }

    /**
     * @param vslmName the vslmName to set
     */
    public void setVslmName(final String vslmName) {
        this.vslmName = vslmName;
    }

    /**
     * @param vslmUrl the vslmUrl to set
     */
    public void setVslmUrl(final String vslmUrl) {
        this.vslmUrl = vslmUrl;
    }

    public VMwareCloudPlatforms getCloudPlatform() {
        return cloudPlatform;
    }

    public void setCloudPlatform(VMwareCloudPlatforms cloudPlatform) {
        this.cloudPlatform = cloudPlatform;
    }
}
