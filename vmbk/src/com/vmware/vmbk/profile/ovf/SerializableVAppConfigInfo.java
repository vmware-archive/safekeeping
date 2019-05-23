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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VAppEntityConfigInfo;

public class SerializableVAppConfigInfo extends SerializableVmConfigInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7387303158382246258L;
    final protected String annotation;

    private final String instanceUuid;
    protected final SerializableManagedByInfo managedBy;
    private final List<SerializableVAppEntityConfigInfo> entityConfig = new ArrayList<>();

    /**
     * @param vAppConfig
     */
    public SerializableVAppConfigInfo(final SerializableVmConfigInfo vAppConfig) {
	super(vAppConfig);
	this.annotation = null;
	this.instanceUuid = null;
	this.managedBy = null;
    }

    public SerializableVAppConfigInfo(final VAppConfigInfo vAppConfigInfo) {
	super(vAppConfigInfo);
	this.annotation = vAppConfigInfo.getAnnotation();
	this.instanceUuid = vAppConfigInfo.getInstanceUuid();
	this.managedBy = (vAppConfigInfo.getManagedBy() != null)
		? new SerializableManagedByInfo(vAppConfigInfo.getManagedBy())
		: null;

	for (final VAppEntityConfigInfo entityCnfg : vAppConfigInfo.getEntityConfig()) {
	    this.entityConfig.add(new SerializableVAppEntityConfigInfo(entityCnfg));
	}
    }

    public List<SerializableVAppEntityConfigInfo> getEntityConfig() {
	return this.entityConfig;
    }

    public String getInstanceUuid() {
	return this.instanceUuid;
    }

    public boolean replaceVm(final ManagedObjectReference orig, final ManagedObjectReference moref) {
	for (final SerializableVAppEntityConfigInfo i : getEntityConfig()) {
	    if (i.isKey(orig.getValue())) {
		i.setKey(moref.getValue());
		return true;
	    }
	}
	return false;
    }

    public VAppConfigSpec toVAppConfigSpec() {

	final VAppConfigSpec vAppConfigInfo = new VAppConfigSpec();

	vAppConfigInfo.getEula().addAll(Arrays.asList(this.eula));
	vAppConfigInfo.setInstallBootStopDelay(this.installBootStopDelay);
	vAppConfigInfo.setInstallBootRequired(this.installBootRequired);
	vAppConfigInfo.getOvfEnvironmentTransport().addAll(Arrays.asList(this.ovfEnvironmentTransport));
	for (final SerializableVAppOvfSectionInfo element : this.OvfSection) {
	    vAppConfigInfo.getOvfSection().add(element.toVAppOvfSectionSpec());
	}
	for (final SerializableVAppProductInfo element : this.product) {
	    vAppConfigInfo.getProduct().add(element.toVAppProductSpec());
	}
	for (final SerializableVAppPropertyInfo element : this.property) {
	    vAppConfigInfo.getProperty().add(element.toVAppPropertySpec());
	}

	vAppConfigInfo.setIpAssignment(this.ipAssignment.toVAppIPAssignmentInfo());
	vAppConfigInfo.setAnnotation(this.annotation);
	for (final SerializableVAppEntityConfigInfo entityCnfg : this.entityConfig) {
	    vAppConfigInfo.getEntityConfig().add(entityCnfg.toVAppEntityConfigInfo());
	}
	return vAppConfigInfo;

    }

}
