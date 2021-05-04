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
package com.vmware.safekeeping.core.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.core.type.enums.EntityType;

public class PropertyMap implements IvmbkMap {
    private final TreeMap<String, String> map;

    PropertyMap() {
        this.map = new TreeMap<>();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Boolean getBooleanProperty(final String section, final String key, final Boolean defaultValue) {
        final String finalkey = section + "." + key;
        if (this.map.containsKey(finalkey)) {
            final String val = this.map.get(finalkey);
            return PrettyBoolean.parseBoolean(val);
        }
        return defaultValue;

    }

    @Override
    public Double getDoubleProperty(final String section, final String key, final Double defaultValue) {
        final String finalkey = section + "." + key;
        if (this.map.containsKey(finalkey)) {
            final String val = this.map.get(finalkey);
            return Double.parseDouble(val);
        }
        return defaultValue;

    }

    @Override
    public Integer getIntegerProperty(final String section, final String key, final Integer defaultValue) {
        final String finalkey = section + "." + key;
        if (this.map.containsKey(finalkey)) {
            final String val = this.map.get(finalkey);
            return PrettyNumber.toInteger(val);
        }
        return defaultValue;
    }

    @Override
    public Long getLongProperty(final String section, final String key, final Long defaultValue) {
        final String finalkey = section + "." + key;
        if (this.map.containsKey(finalkey)) {
            final String val = this.map.get(finalkey);
            return PrettyNumber.toLong(val);
        }
        return defaultValue;
    }

    @Override
    public String getStringProperty(final String section, final String key, final String defaultValue) {
        final String finalkey = section + "." + key;
        if (this.map.containsKey(finalkey)) {
            final String val = this.map.get(finalkey);
            return StringUtils.trimToNull(val);
        }
        return defaultValue;
    }

    public void loadPropertyFile(final File configPropertyFile) throws IOException {
        final Properties properties = new Properties();
        try (FileInputStream propFile = new FileInputStream(configPropertyFile)) {
            properties.load(propFile);
            for (final String key : properties.stringPropertyNames()) {
                final String value = properties.getProperty(key);
                this.map.put(key, value);
            }
        }
    }

    @Override
    public String removeProperty(final String section, final String key) {
        final String finalkey = section + "." + key;
        return this.map.remove(finalkey);
    }

    void savePropertyFile(final File configPropertyFile) throws IOException {
        final Properties properties = new Properties() {

            private static final long serialVersionUID = 4112578634029874840L;

            @Override
            public synchronized Enumeration<Object> keys() {
                final Comparator<Object> byCaseInsensitiveString = Comparator.comparing(Object::toString,
                        String.CASE_INSENSITIVE_ORDER);

                final Supplier<TreeSet<Object>> supplier = () -> new TreeSet<>(byCaseInsensitiveString);

                final TreeSet<Object> sortedSet = super.keySet().stream().collect(Collectors.toCollection(supplier));

                return Collections.enumeration(sortedSet);
            }
        };
        properties.putAll(this.map);
        final String comment = "VMBK version 2";
        try (FileOutputStream propFile = new FileOutputStream(configPropertyFile)) {

            properties.store(propFile, comment);
        }

    }

    @Override
    public void setBooleanProperty(final String section, final String key, final Boolean value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, PrettyBoolean.toString(value));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setDateProperty(final String section, final String key, final Date value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, value.toGMTString());
    }

    @Override
    public void setDoubleProperty(final String section, final String key, final Double value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, Double.toString(value));
    }

    @Override
    public void setEntityTypeProperty(final String section, final String key, final EntityType value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, value.toString(true));

    }

    @Override
    public void setIntegerProperty(final String section, final String key, final Integer value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, PrettyNumber.toString(value));
    }

    @Override
    public void setLongProperty(final String section, final String key, final Long value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, PrettyNumber.toString(value));
    }

    @Override
    public void setLongProperty(final String section, final String key, final Long value, final MetricPrefix unit) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, PrettyNumber.toString(value, unit));
    }

    @Override
    public void setStringProperty(final String section, final String key, final String value) {
        final String finalkey = section + "." + key;
        this.map.put(finalkey, value);
    }

    @Override
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.map);
    }

    @Override
    public String toPrettyPrintingJson() throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.map);
    }
}
