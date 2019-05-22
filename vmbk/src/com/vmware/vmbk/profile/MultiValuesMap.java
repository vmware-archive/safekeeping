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
package com.vmware.vmbk.profile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.FirstClassObjectType;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbk.type.PrettyNumber.MetricPrefix;

class MultiValuesMap implements vmbkMap {

    HashMap<String, HashMap<String, String>> map;

    public MultiValuesMap() {
	this.map = new HashMap<>();
    }

    @Override
    public void clear() {
	this.map.clear();

    }

    String createSectionName(final String a, final int b) {
	return String.format("%s_%d", a, b);

    }

    String createSectionName(final String a, final String b) {
	return String.format("%s_%s", a, b);
    }

    boolean doesPropertyExist(final String section, final String key) {
	if (this.map.containsKey(section)) {
	    return this.map.get(section).containsKey(key);
	}
	return false;

    }

    @Override
    public Boolean getBooleanProperty(final String section, final String key, final Boolean defaultValue) {
	if (this.map.containsKey(section)) {
	    final HashMap<String, String> coll = this.map.get(section);
	    if (coll.containsKey(key)) {
		final String val = coll.get(key);
		return PrettyBoolean.parseBoolean(val);
	    }
	}
	return defaultValue;
    }

    @Override
    public Double getDoubleProperty(final String section, final String key, final Double defaultValue) {
	if (this.map.containsKey(section)) {
	    final HashMap<String, String> coll = this.map.get(section);
	    if (coll.containsKey(key)) {
		final String val = coll.get(key);
		return Double.parseDouble(val);
	    }
	}
	return defaultValue;
    }

    @Override
    public Integer getIntegerProperty(final String section, final String key, final Integer defaultValue) {
	if (this.map.containsKey(section)) {
	    final HashMap<String, String> coll = this.map.get(section);
	    if (coll.containsKey(key)) {
		final String val = coll.get(key);
		return PrettyNumber.toInteger(val);
	    }
	}
	return defaultValue;
    }

    @Override
    public Long getLongProperty(final String section, final String key, final Long defaultValue) {
	if (this.map.containsKey(section)) {
	    final HashMap<String, String> coll = this.map.get(section);
	    if (coll.containsKey(key)) {
		final String val = coll.get(key);
		return PrettyNumber.toLong(val);
	    }
	}
	return defaultValue;
    }

    HashMap<String, String> getProperties(final String section) {

	final HashMap<String, String> result = new LinkedHashMap<>();
	if (this.map.containsKey(section)) {
	    result.putAll(this.map.get(section));
	}
	return result;
    }

    LinkedList<String> getPropertyNames(final String section) {
	final LinkedList<String> result = new LinkedList<>();
	if (this.map.containsKey(section)) {
	    result.addAll(this.map.get(section).keySet());
	}
	return result;
    }

    @Override
    public String getStringProperty(final String section, final String key, final String defaultValue) {
	if (this.map.containsKey(section)) {
	    final HashMap<String, String> coll = this.map.get(section);
	    if (coll.containsKey(key)) {
		final String val = coll.get(key);
		return val;
	    }
	}
	return defaultValue;
    }

    public void load(final byte[] byteArray) throws IOException {
	this.map = new ObjectMapper().readValue(byteArray,
		new TypeReference<HashMap<String, HashMap<String, String>>>() {
		});
    }

    @Override
    public String removeProperty(final String section, final String key) {
	if (this.map.containsKey(section)) {
	    if (this.map.get(section).containsKey(key)) {
		return this.map.get(section).remove(key);
	    }
	}
	return null;
    }

    HashMap<String, String> removeSection(final String section) {
	return this.map.remove(section);
    }

    /**
     * @param generation
     * @param backupmodeKey
     * @param unknow
     */
    public void setBackupModeProperty(final String section, final String key, final BackupMode value) {
	setProperty(section, key, value.toString());

    }

    @Override
    public void setBooleanProperty(final String section, final String key, final Boolean value) {
	setProperty(section, key, PrettyBoolean.toString(value));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setDateProperty(final String section, final String key, final Date value) {
	setProperty(section, key, value.toGMTString());
    }

    @Override
    public void setDoubleProperty(final String section, final String key, final Double value) {
	setProperty(section, key, Double.toString(value));
    }

    @Override
    public void setEntityTypeProperty(final String section, final String key, final EntityType value) {
	setProperty(section, key, value.toString(true));

    }

    public void setFcoTypeProperty(final String section, final String key,
	    final FirstClassObjectType firstClassObjectType) {
	setProperty(section, key, firstClassObjectType.toString());

    }

    @Override
    public void setIntegerProperty(final String section, final String key, final Integer value) {
	setProperty(section, key, PrettyNumber.toString(value));
    }

    @Override
    public void setLongProperty(final String section, final String key, final Integer value) {
	setProperty(section, key, PrettyNumber.toString(value));
    }

    @Override
    public void setLongProperty(final String section, final String key, final Long value) {
	setProperty(section, key, PrettyNumber.toString(value));
    }

    @Override
    public void setLongProperty(final String section, final String key, final Long value, final MetricPrefix unit) {
	setProperty(section, key, PrettyNumber.toString(value, unit));
    }

    private void setProperty(final String section, final String key, final String value) {
	if (!this.map.containsKey(section)) {
	    this.map.put(section, new LinkedHashMap<String, String>());
	}
	this.map.get(section).put(key, value);
    }

    @Override
    public void setStringProperty(final String section, final String key, final String value) {
	setProperty(section, key, value);
    }

    ByteArrayInOutStream toByteArrayInOutputStream() throws IOException {
	final ByteArrayInOutStream result = new ByteArrayInOutStream(toJson());
	return result;
    }

    @Override
    public String toJson() throws JsonProcessingException {
	final String result = new ObjectMapper().writeValueAsString(this.map);
	return result;
    }
}
