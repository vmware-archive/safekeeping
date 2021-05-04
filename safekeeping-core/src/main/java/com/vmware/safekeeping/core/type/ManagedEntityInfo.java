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
package com.vmware.safekeeping.core.type;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.vim25.ManagedObjectReference;

public class ManagedEntityInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6990824204473425157L;

    public static String composeEntityInfoName(final List<ManagedEntityInfo> entityInfoList) {
        final StringBuilder retString = new StringBuilder();
        if (!entityInfoList.isEmpty()) {
            for (int index = 1; index < (entityInfoList.size() - 1); index++) {
                retString.append(entityInfoList.get(index).getName());
                retString.append('/');
            }
            retString.append(entityInfoList.get(entityInfoList.size() - 1).getName());
        }
        return retString.toString();
    }

    private EntityType entityType;
    private String morefValue;

    private String name;

    private String serverUuid;

    public ManagedEntityInfo() {

    }

    public ManagedEntityInfo(final IFirstClassObject fo) {
        this.name = fo.getName();
        this.morefValue = fo.getMoref().getValue();
        this.entityType = fo.getEntityType();
        this.serverUuid = fo.getServerUuid();
    }

    public ManagedEntityInfo(final ManagedEntityInfo fo) {
        this.name = fo.getName();
        this.morefValue = fo.getMoref().getValue();
        this.entityType = fo.getEntityType();
        this.serverUuid = fo.getServerUuid();
    }

    public ManagedEntityInfo(final String name, final EntityType managedEntityType, final String morefValue,
            final String serverUuid) {
        this.morefValue = morefValue;
        this.name = (name == null) ? "" : name;
        this.entityType = managedEntityType;
        this.serverUuid = serverUuid;
    }

    public ManagedEntityInfo(final String name, final ManagedObjectReference moref, final String serverUuid) {
        this.name = (name == null) ? "" : name;
        this.morefValue = moref.getValue();
        this.entityType = EntityType.valueOf(moref.getType());
        this.serverUuid = serverUuid;
    }

    @Override
    public boolean equals(final Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /*
         * Check if o is an instance of Complex or not "null instanceof [type]" also
         * returns false
         */
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            // we can compare data members
            final ManagedEntityInfo c = (ManagedEntityInfo) obj;
            return (this.entityType == c.entityType) && this.morefValue.equals(c.morefValue) && this.name.equals(c.name)
                    && this.serverUuid.equals(c.serverUuid);
        }

        return false;
    }

    /**
     * @return the managedEntityType
     */
    public EntityType getEntityType() {
        return this.entityType;
    }

    @JsonIgnore
    public ManagedObjectReference getMoref() {
        final ManagedObjectReference moref = new ManagedObjectReference();
        moref.setType(this.entityType.toString(false));
        moref.setValue(this.morefValue);
        return moref;
    }

    /**
     * @return the morefValue
     */
    public String getMorefValue() {
        return this.morefValue;
    }

    public String getName() {
        return this.name;
    }

    public String getServerUuid() {
        return this.serverUuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.morefValue, this.entityType, this.serverUuid);
    }

    /**
     * @param managedEntityType the managedEntityType to set
     */
    public void setEntityType(final EntityType managedEntityType) {
        this.entityType = managedEntityType;
    }

    /**
     * @param morefValue the morefValue to set
     */
    public void setMorefValue(final String morefValue) {
        this.morefValue = morefValue;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param serverUuid the serverUuid to set
     */
    public void setServerUuid(final String serverUuid) {
        this.serverUuid = serverUuid;
    }

    @Override
    public String toString() {
        return String.format("%s:%s moref:%s", this.entityType.toString(true), this.name, this.morefValue);
    }

}
