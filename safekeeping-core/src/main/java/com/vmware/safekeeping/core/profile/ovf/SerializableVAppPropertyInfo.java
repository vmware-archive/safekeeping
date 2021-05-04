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
package com.vmware.safekeeping.core.profile.ovf;

import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.VAppPropertyInfo;
import com.vmware.vim25.VAppPropertySpec;

public class SerializableVAppPropertyInfo {

	private String category;

	private String classId;

	private String defaultValue;

	private String description;

	private String id;

	private String instanceId;

	private int key;

	private String label;

	private String type;

	private String typeReference;

	private Boolean userConfigurable;

	private String value;

	public SerializableVAppPropertyInfo() {
	}

	public SerializableVAppPropertyInfo(final VAppPropertyInfo vAppPropertyInfo) {
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

	/**
	 * @return the category
	 */
	public String getCategory() {
		return this.category;
	}

	/**
	 * @return the classId
	 */
	public String getClassId() {
		return this.classId;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return this.instanceId;
	}

	/**
	 * @return the key
	 */
	public int getKey() {
		return this.key;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @return the typeReference
	 */
	public String getTypeReference() {
		return this.typeReference;
	}

	/**
	 * @return the userConfigurable
	 */
	public Boolean getUserConfigurable() {
		return this.userConfigurable;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(final String category) {
		this.category = category;
	}

	/**
	 * @param classId the classId to set
	 */
	public void setClassId(final String classId) {
		this.classId = classId;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(final String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(final int key) {
		this.key = key;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * @param typeReference the typeReference to set
	 */
	public void setTypeReference(final String typeReference) {
		this.typeReference = typeReference;
	}

	/**
	 * @param userConfigurable the userConfigurable to set
	 */
	public void setUserConfigurable(final Boolean userConfigurable) {
		this.userConfigurable = userConfigurable;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
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
