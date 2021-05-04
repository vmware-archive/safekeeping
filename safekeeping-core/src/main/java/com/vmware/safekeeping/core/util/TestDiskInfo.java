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
package com.vmware.safekeeping.core.util;

import com.vmware.jvix.jDiskLib;

/*
    * VixDiskLib's DiskInfo structure
    */
public class TestDiskInfo {
    private TestDiskGeometry biosGeo;

    private TestDiskGeometry physGeo;

    private long capacityInSectors;

    private int adapterType;

    private int numLinks;

    private String parentFileNameHint;

    private long logicalSectorSize;

    private long physicalSectorSize;

    public TestDiskInfo() {
        this.biosGeo = new TestDiskGeometry();
        this.physGeo = new TestDiskGeometry();
    }

    public TestDiskInfo(final jDiskLib.Info info) {
        this.biosGeo = new TestDiskGeometry();
        this.physGeo = new TestDiskGeometry();
        this.capacityInSectors = info.capacityInSectors;
        this.adapterType = info.adapterType;
        this.numLinks = info.numLinks;
        this.parentFileNameHint = info.parentFileNameHint;
        this.logicalSectorSize = info.logicalSectorSize;
        this.physicalSectorSize = info.physicalSectorSize;
        this.biosGeo.setCylinders(info.biosGeo.cylinders);
        this.biosGeo.setHeads(info.biosGeo.heads);
        this.biosGeo.setSectors(info.biosGeo.sectors);
        this.physGeo.setCylinders(info.physGeo.cylinders);
        this.physGeo.setHeads(info.physGeo.heads);
        this.physGeo.setSectors(info.physGeo.sectors);

    }

    /**
     * @return the adapterType
     */
    public int getAdapterType() {
        return this.adapterType;
    }

    /**
     * @return the biosGeo
     */
    public TestDiskGeometry getBiosGeo() {
        return this.biosGeo;
    }

    /**
     * @return the capacityInSectors
     */
    public long getCapacityInSectors() {
        return this.capacityInSectors;
    }

    /**
     * @return the logicalSectorSize
     */
    public long getLogicalSectorSize() {
        return this.logicalSectorSize;
    }

    /**
     * @return the numLinks
     */
    public int getNumLinks() {
        return this.numLinks;
    }

    /**
     * @return the parentFileNameHint
     */
    public String getParentFileNameHint() {
        return this.parentFileNameHint;
    }

    /**
     * @return the physGeo
     */
    public TestDiskGeometry getPhysGeo() {
        return this.physGeo;
    }

    /**
     * @return the physicalSectorSize
     */
    public long getPhysicalSectorSize() {
        return this.physicalSectorSize;
    }

    /**
     * @param adapterType the adapterType to set
     */
    public void setAdapterType(final int adapterType) {
        this.adapterType = adapterType;
    }

    /**
     * @param biosGeo the biosGeo to set
     */
    public void setBiosGeo(final TestDiskGeometry biosGeo) {
        this.biosGeo = biosGeo;
    }

    /**
     * @param capacityInSectors the capacityInSectors to set
     */
    public void setCapacityInSectors(final long capacityInSectors) {
        this.capacityInSectors = capacityInSectors;
    }

    /**
     * @param logicalSectorSize the logicalSectorSize to set
     */
    public void setLogicalSectorSize(final long logicalSectorSize) {
        this.logicalSectorSize = logicalSectorSize;
    }

    /**
     * @param numLinks the numLinks to set
     */
    public void setNumLinks(final int numLinks) {
        this.numLinks = numLinks;
    }

    /**
     * @param parentFileNameHint the parentFileNameHint to set
     */
    public void setParentFileNameHint(final String parentFileNameHint) {
        this.parentFileNameHint = parentFileNameHint;
    }

    /**
     * @param physGeo the physGeo to set
     */
    public void setPhysGeo(final TestDiskGeometry physGeo) {
        this.physGeo = physGeo;
    }

    /**
     * @param physicalSectorSize the physicalSectorSize to set
     */
    public void setPhysicalSectorSize(final long physicalSectorSize) {
        this.physicalSectorSize = physicalSectorSize;
    }
}
