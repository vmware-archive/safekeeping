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

import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.VAppPropertyInfo;
import com.vmware.vim25.VAppPropertySpec;

class SerializableVAppPropertyInfo implements Serializable {

    private static final long serialVersionUID = -1343964514546521597L;
    private final String category;
    private final String classId;
    private final String defaultValue;
    private final String description;
    private final String id;
    private final String instanceId;
    private final int key;
    private final String label;
    private final String type;
    private final String typeReference;
    private final Boolean userConfigurable;
    private final String value;

    SerializableVAppPropertyInfo(final VAppPropertyInfo vAppPropertyInfo) {
	this.category = vAppPropertyInfo.getCategory();
	this.classId = vAppPropertyInfo.getClassId();
	this.defaultValue = vAppPropertyInfo.getDefaultValue();
	this.description = vAppPropertyInfo.getDescription();
	this.id = vAppPropertyInfo.getId();
	this.instanceId = vAppPropertyInfo.getInstanceId();
	this.key = vAppPropertyInfo.getKey();
	this.label = vAppPropertyInfo.getLabel();
	this.type = vAppPropertyInfo.getType();
	this.typeReference = vAppPropertyInfo.getTypeReference();
	this.value = vAppPropertyInfo.getValue();
	this.userConfigurable = vAppPropertyInfo.isUserConfigurable();
    }

    VAppPropertySpec toVAppPropertySpec() {
	final VAppPropertyInfo vAppPropertyInfo = new VAppPropertyInfo();
	vAppPropertyInfo.setCategory(this.category);
	vAppPropertyInfo.setClassId(this.classId);
	vAppPropertyInfo.setDefaultValue(this.defaultValue);
	vAppPropertyInfo.setDescription(this.description);
	vAppPropertyInfo.setId(this.id);
	vAppPropertyInfo.setInstanceId(this.instanceId);
	vAppPropertyInfo.setKey(this.key);
	vAppPropertyInfo.setLabel(this.label);
	vAppPropertyInfo.setType(this.type);
	vAppPropertyInfo.setTypeReference(this.typeReference);
	vAppPropertyInfo.setValue(this.value);
	vAppPropertyInfo.setUserConfigurable(this.userConfigurable);

	final VAppPropertySpec vAppPropertySpec = new VAppPropertySpec();
	vAppPropertySpec.setInfo(vAppPropertyInfo);
	vAppPropertySpec.setOperation(ArrayUpdateOperation.ADD);
	return vAppPropertySpec;
    }

}
