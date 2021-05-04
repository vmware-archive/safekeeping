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

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.core.type.enums.EntityType;

interface IvmbkMap {
    void clear();

    default Boolean getBooleanProperty(final String section, final String key) {
        return getBooleanProperty(section, key, null);
    }

    Boolean getBooleanProperty(final String section, final String key, final Boolean defaultValue);

    default Double getDoubleProperty(final String section, final String key) {
        return getDoubleProperty(section, key, null);
    }

    Double getDoubleProperty(String section, String key, Double defaultValue);

    default EntityType getEntityTypeProperty(final String section, final String key) {
        final String type = getStringProperty(section, key, null);
        return EntityType.toEntityType(type);
    }

    default Integer getIntegerProperty(final String section, final String key) {
        return getIntegerProperty(section, key, null);
    }

    Integer getIntegerProperty(final String section, final String key, final Integer defaultValue);

    default Long getLongProperty(final String section, final String key) {
        return getLongProperty(section, key, null);
    }

    Long getLongProperty(final String section, final String key, final Long defaultValue);

    default String getStringProperty(final String section, final String key) {
        return getStringProperty(section, key, null);
    }

    String getStringProperty(final String section, final String key, final String defaultValue);

    default LinkedList<String> getStringPropertyArray(final String section, final String key) {
        final LinkedList<String> result = new LinkedList<>();
        final String st = getStringProperty(section, key, null);
        if (StringUtils.isEmpty(st)) {
            result.add(st);
        } else {
            final String[] a = st.split(",");
            result.addAll(Arrays.asList(a));
        }
        return result;
    }

    String removeProperty(String section, String key);

    void setBooleanProperty(final String section, final String key, final Boolean value);

    void setDateProperty(String section, String key, Date value);

    void setDoubleProperty(String section, String key, Double value);

    void setEntityTypeProperty(final String section, final String key, EntityType value);

    void setIntegerProperty(final String section, final String key, final Integer value);

    void setLongProperty(String section, String key, Long value);

    void setLongProperty(String section, String key, Long value, MetricPrefix unit);

    void setStringProperty(final String section, final String key, final String value);

    String toJson() throws JsonProcessingException;

    String toPrettyPrintingJson() throws JsonProcessingException;
}
