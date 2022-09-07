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
package com.vmware.safekeeping.external.result;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupRestore;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.enums.phase.PhaseInterface;
import com.vmware.safekeeping.core.type.location.CoreIvdLocation;
import com.vmware.safekeeping.core.type.location.CoreVappLocation;
import com.vmware.safekeeping.core.type.location.CoreVmLocation;
import com.vmware.safekeeping.external.type.FcoLocation;
import com.vmware.safekeeping.external.type.IvdLocation;
import com.vmware.safekeeping.external.type.VappLocation;
import com.vmware.safekeeping.external.type.VmLocation;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractResultActionBackupRestore extends AbstractResultActionWithSubOperations {

    public static void convert(final AbstractCoreResultActionBackupRestore src,
            final AbstractResultActionBackupRestore dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        ResultAction.convert(src, dst);

        if (src.getLocations() != null) {
            switch (src.getEntityType()) {
            case K8sNamespace:
                break;
            case ImprovedVirtualDisk:
                final IvdLocation ivdLocation = new IvdLocation();
                IvdLocation.convertFrom((CoreIvdLocation) src.getLocations(), ivdLocation);
                dst.setLocations(ivdLocation);
                break;
            case VirtualApp:
                final VappLocation vappLocation = new VappLocation();
                VappLocation.convertFrom((CoreVappLocation) src.getLocations(), vappLocation);
                dst.setLocations(vappLocation);
                break;
            case VirtualMachine:
                final VmLocation vmLocation = new VmLocation();
                VmLocation.convertFrom((CoreVmLocation) src.getLocations(), vmLocation);
                dst.setLocations(vmLocation);
                break;
            default:
                break;
            }
        }
        dst.setGenerationInfo(src.getGenerationInfo());
        dst.generationId = src.getGenerationId();

        dst.setSuccess(src.isSuccessful());
        dst.setTargetName(src.getTargetName());
        dst.setIndex(src.getIndex());

    }

    private int generationId;
    private GenerationInfo generationInfo;

    private Integer index;

    private FcoLocation locations;

    private boolean success;

    private String targetName;

    public int getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the generationInfo
     */
    public GenerationInfo getGenerationInfo() {
        return this.generationInfo;
    }

    /**
     * @return the index
     */
    public Integer getIndex() {
        return this.index;
    }

    /**
     * @return the locations
     */
    public FcoLocation getLocations() {
        return this.locations;
    }

    public abstract PhaseInterface getPhase();

    /**
     * @return the targetName
     */
    public String getTargetName() {
        return this.targetName;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return this.success;
    }

    public void setGenerationId(final int generationId) {
        this.generationId = generationId;
    }

    /**
     * @param generationInfo the generationInfo to set
     */
    public void setGenerationInfo(final GenerationInfo generationInfo) {
        this.generationInfo = generationInfo;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(final Integer index) {
        this.index = index;
    }

    /**
     * @param locations the locations to set
     */
    public void setLocations(final FcoLocation locations) {
        this.locations = locations;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(final boolean success) {
        this.success = success;
    }

    /**
     * @param targetName the targetName to set
     */
    public void setTargetName(final String targetName) {
        this.targetName = targetName;
    }

}
