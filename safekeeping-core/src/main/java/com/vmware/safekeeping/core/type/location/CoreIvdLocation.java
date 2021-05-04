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
package com.vmware.safekeeping.core.type.location;

import com.vmware.safekeeping.core.type.ManagedEntityInfo;

public class CoreIvdLocation extends AbstractCoreFcoLocation {

    /**
     * 
     */
    private static final long serialVersionUID = -4349388377308858026L;

    private String vmdkFileName;

    private String datastorePath;

    private ManagedEntityInfo datastoreInfo;
    private String vmdkFullPath;

    public CoreIvdLocation() {
        super();
    }

    /**
     * @return the datastoreInfo
     */

    public ManagedEntityInfo getDatastoreInfo() {
        return this.datastoreInfo;
    }

    /**
     * @return the datastorePath
     */
    public String getDatastorePath() {
        return this.datastorePath;
    }

    /**
     * @return the vmdkFileName
     */
    public String getVmdkFileName() {
        return this.vmdkFileName;
    }

    /**
     * @return the vmdkFullPath
     */
    public String getVmdkFullPath() {
        return this.vmdkFullPath;
    }

    /**
     * @param datastoreInfo the datastoreInfo to set
     */

    public void setDatastoreInfo(final ManagedEntityInfo datastoreInfo) {
        this.datastoreInfo = datastoreInfo;
    }

    /**
     * @param datastorePath the datastorePath to set
     */
    public void setDatastorePath(final String datastorePath) {
        this.datastorePath = datastorePath;
    }

    /**
     * @param vmdkFileName the vmdkFileName to set
     */
    public void setVmdkFileName(final String vmdkFileName) {
        this.vmdkFileName = vmdkFileName;
    }

    /**
     * @param vmdkFullPath the vmdkFullPath to set
     */
    public void setVmdkFullPath(final String vmdkFullPath) {
        this.vmdkFullPath = vmdkFullPath;
    }

}
