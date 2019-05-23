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
package com.vmware.vmbk.profile.ovf;

import java.io.Serializable;
import java.util.Arrays;

import com.vmware.vim25.VmConfigInfo;
import com.vmware.vim25.VmConfigSpec;

public class SerializableVmConfigInfo implements Serializable {

    private static final long serialVersionUID = -9120653759105913359L;
    protected final String[] eula;
    protected final boolean installBootRequired;
    protected final int installBootStopDelay;
    protected final SerializableVAppIPAssignmentInfo ipAssignment;
    protected final String[] ovfEnvironmentTransport;
    protected final SerializableVAppOvfSectionInfo[] OvfSection;
    protected final SerializableVAppProductInfo[] product;
    protected final SerializableVAppPropertyInfo[] property;

    /**
     * @param vAppConfig
     */
    public SerializableVmConfigInfo(final SerializableVmConfigInfo vAppConfig) {
	this.eula = vAppConfig.eula;
	this.installBootStopDelay = vAppConfig.installBootStopDelay;
	this.installBootRequired = vAppConfig.installBootRequired;
	this.ovfEnvironmentTransport = vAppConfig.ovfEnvironmentTransport;

	this.OvfSection = vAppConfig.OvfSection;

	this.product = vAppConfig.product;

	this.property = vAppConfig.property;

	this.ipAssignment = vAppConfig.ipAssignment;
    }

    public SerializableVmConfigInfo(final VmConfigInfo vmConfigInfo) {
	this.eula = vmConfigInfo.getEula().toArray(new String[0]);
	this.installBootStopDelay = vmConfigInfo.getInstallBootStopDelay();
	this.installBootRequired = vmConfigInfo.isInstallBootRequired();
	this.ovfEnvironmentTransport = vmConfigInfo.getOvfEnvironmentTransport().toArray(new String[0]);

	this.OvfSection = new SerializableVAppOvfSectionInfo[vmConfigInfo.getOvfSection().size()];
	for (int i = 0; i < vmConfigInfo.getOvfSection().size(); i++) {
	    this.OvfSection[i] = new SerializableVAppOvfSectionInfo(vmConfigInfo.getOvfSection().get(i));
	}

	this.product = new SerializableVAppProductInfo[vmConfigInfo.getProduct().size()];
	for (int i = 0; i < vmConfigInfo.getProduct().size(); i++) {
	    this.product[i] = new SerializableVAppProductInfo(vmConfigInfo.getProduct().get(i));
	}

	this.property = new SerializableVAppPropertyInfo[vmConfigInfo.getProperty().size()];
	for (int i = 0; i < vmConfigInfo.getProperty().size(); i++) {
	    this.property[i] = new SerializableVAppPropertyInfo(vmConfigInfo.getProperty().get(i));
	}

	this.ipAssignment = new SerializableVAppIPAssignmentInfo(vmConfigInfo.getIpAssignment());
    }

    public VmConfigSpec toVmConfigInfo() {
	final VmConfigSpec vmConfigInfo = new VmConfigSpec();
	vmConfigInfo.getEula().addAll(Arrays.asList(this.eula));
	vmConfigInfo.setInstallBootStopDelay(this.installBootStopDelay);
	vmConfigInfo.setInstallBootRequired(this.installBootRequired);
	vmConfigInfo.getOvfEnvironmentTransport().addAll(Arrays.asList(this.ovfEnvironmentTransport));
	for (final SerializableVAppOvfSectionInfo element : this.OvfSection) {
	    vmConfigInfo.getOvfSection().add(element.toVAppOvfSectionSpec());
	}
	for (final SerializableVAppProductInfo element : this.product) {
	    vmConfigInfo.getProduct().add(element.toVAppProductSpec());
	}
	for (final SerializableVAppPropertyInfo element : this.property) {
	    vmConfigInfo.getProperty().add(element.toVAppPropertySpec());
	}

	vmConfigInfo.setIpAssignment(this.ipAssignment.toVAppIPAssignmentInfo());
	return vmConfigInfo;
    }

}
