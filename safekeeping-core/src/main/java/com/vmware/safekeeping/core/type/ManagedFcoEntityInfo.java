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

import java.util.Objects;

import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vim25.ManagedObjectReference;

public class ManagedFcoEntityInfo extends ManagedEntityInfo {
    /**
     * 
     */
    private static final long serialVersionUID = -4196558415257974772L;
    public static final String UUID_ZERO = "00000000-00000000-00000000-00000000";

    /**
     * Create an empty Null ManagedFcoEntityInfo
     *
     * @return
     */
    public static ManagedFcoEntityInfo newNullManagedEntityInfo() {
        final ManagedFcoEntityInfo entity = new ManagedFcoEntityInfo();
        entity.setUuid(UUID_ZERO);
        entity.setName("");
        entity.setServerUuid(UUID_ZERO);
        entity.setMorefValue("none-0");
        entity.setEntityType(EntityType.None);
        return entity;
    }

    private String uuid;

    public ManagedFcoEntityInfo() {

    }

    public ManagedFcoEntityInfo(final ManagedFcoEntityInfo src) {
        super(src);
        this.uuid = src.uuid;
    }

    public ManagedFcoEntityInfo(final String name, final EntityType managedEntityType, final String morefValue,
            final String uuid, final String serverUuid) {
        super(name, managedEntityType, morefValue, serverUuid);
        this.uuid = uuid;
    }

    public ManagedFcoEntityInfo(final String name, final ManagedObjectReference moref, final String uuid,
            final String serverUuid) {
        super(name, moref, serverUuid);
        this.uuid = uuid;
    }

    @Override
    public boolean equals(final Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        if (o != null && this.getClass() == o.getClass()) {
            // typecast o to Complex so that we can compare data members
            return this.uuid.equals(((ManagedFcoEntityInfo) o).uuid) && super.equals(o);
        }
        return false;
    }

    public String getUuid() {
        return this.uuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, super.hashCode());
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return String.format("%s:%s uuid:%s moref:%s", getEntityType().toString(true), getName(), this.uuid,
                getMorefValue());
    }

    public String toString(final boolean moref) {
        if (moref) {
            return toString();
        }

        String lname;
        if (getName().length() < 30) {
            lname = getName();
        } else {
            lname = getName().substring(0, 27) + "...";
        }

        return String.format("%-8s%36s\t%-30s ", getEntityType().toString(true), getUuid(), lname);
    }
}
