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
package com.vmware.safekeeping.core.soap.managers;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.vim25.EntityPrivilege;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PrivilegeAvailability;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.UserPrivilegeResult;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;

public class VimPrivilegeChecker {
    private final ManagedObjectReference auth;
    private final VimPortType vimPort;
    private final UserSession userSession;

    private final VimConnection parent;

    public VimPrivilegeChecker(final VimConnection parent) {

        this.parent = parent;
        this.auth = parent.getServiceContent().getAuthorizationManager();
        this.vimPort = parent.getVimPort();
        this.userSession = parent.getUserSession();
    }

    public Map<String, Boolean> checkPrivilegesOnEntities(final ManagedEntityInfo[] entities, final String[] privId)
            throws RuntimeFaultFaultMsg {
        final List<ManagedObjectReference> mos = new ArrayList<>();
        final Map<String, Boolean> methodAuhorization = new HashMap<>();
        for (final ManagedEntityInfo entity : entities) {
            mos.add(entity.getMoref());
        }
        final List<UserPrivilegeResult> privOnEntities = this.vimPort.fetchUserPrivilegeOnEntities(this.auth, mos,
                this.userSession.getUserName());
        for (final UserPrivilegeResult privOnEntity : privOnEntities) {
            ManagedEntityInfo lEntity = null;
            for (final ManagedEntityInfo entity : entities) {
                if (entity.getMorefValue().equals(privOnEntity.getEntity().getValue())) {
                    lEntity = entity;
                    break;
                }
            }
            if (lEntity != null) {
                for (final String priv : privId) {
                    methodAuhorization.put(
                            String.format("[%s{Name:%s,Type:%s}]", priv, lEntity.getName(), lEntity.getEntityType()),
                            privOnEntity.getPrivileges().contains(priv));
                }
            }
        }
        return methodAuhorization;

    }

    public Map<String, Boolean> checkPrivilegesOnEntity(final ManagedEntityInfo entity, final String[] privId)
            throws RuntimeFaultFaultMsg {
        return checkPrivilegesOnEntities(new ManagedEntityInfo[] { entity }, privId);
    }

    public List<UserPrivilegeResult> fetchUserPrivilegeOnEntities(final List<ManagedObjectReference> mos)
            throws RuntimeFaultFaultMsg {

        return this.vimPort.fetchUserPrivilegeOnEntities(this.auth, mos, this.userSession.getUserName());
    }

    public List<UserPrivilegeResult> fetchUserPrivilegeOnEntities(final ManagedEntityInfo... entities)
            throws RuntimeFaultFaultMsg {
        final List<ManagedObjectReference> mos = new ArrayList<>();
        for (final ManagedEntityInfo entity : entities) {
            mos.add(entity.getMoref());
        }
        return this.vimPort.fetchUserPrivilegeOnEntities(this.auth, mos, this.userSession.getUserName());
    }

    public List<String> fetchUserPrivilegeOnEntity(final ManagedEntityInfo entity) throws RuntimeFaultFaultMsg {
        final List<ManagedObjectReference> mos = new ArrayList<>();
        mos.add(entity.getMoref());

        final List<UserPrivilegeResult> res = this.vimPort.fetchUserPrivilegeOnEntities(this.auth, mos,
                this.userSession.getUserName());
        if (res.isEmpty()) {
            return Collections.emptyList();
        } else {
            return res.get(0).getPrivileges();
        }

    }

    public VimConnection getParent() {
        return this.parent;
    }

    /**
     * Check privilege for current user
     *
     * @param entity
     * @param priv
     * @return
     * @throws RuntimeFaultFaultMsg
     */
    public SimpleEntry<String, Boolean> hasUserPrivilegeOnEntity(final ManagedEntityInfo entity, final String priv)
            throws RuntimeFaultFaultMsg {
        final List<ManagedObjectReference> mos = new ArrayList<>();
        mos.add(entity.getMoref());
        final List<String> privId = new ArrayList<>();
        privId.add(priv);

        for (final EntityPrivilege pr : this.vimPort.hasUserPrivilegeOnEntities(this.auth, mos,
                this.userSession.getUserName(), privId)) {
            if (!pr.getPrivAvailability().isEmpty()) {
                final PrivilegeAvailability aval = pr.getPrivAvailability().get(0);
                return new SimpleEntry<>(String.format("[%s(%s)]", aval.getPrivId(), entity.getEntityType()),
                        aval.isIsGranted());
            }
        }
        return null;
    }

    public Map<String, Boolean> hasUserPrivilegesOnEntities(final List<ManagedObjectReference> mos,
            final List<String> privId) throws RuntimeFaultFaultMsg {

        final Map<String, Boolean> entitlePrivilages = new HashMap<>();
        for (final EntityPrivilege pr : this.vimPort.hasUserPrivilegeOnEntities(this.auth, mos,
                this.userSession.getUserName(), privId)) {
            final Iterator<ManagedObjectReference> mosIter = mos.iterator();
            for (final PrivilegeAvailability aval : pr.getPrivAvailability()) {

                entitlePrivilages.put(String.format("[%s(%s)]", aval.getPrivId(), mosIter.next().getType()),
                        aval.isIsGranted());
            }
        }
        return entitlePrivilages;
    }

    /**
     * Check privileges for current user
     *
     * @param entity     moref of the object
     * @param privileges to check
     * @return
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, Boolean> hasUserPrivilegesOnEntity(final ManagedEntityInfo entity, final String priv,
            final String... privileges) throws RuntimeFaultFaultMsg {
        return hasUserPrivilegesOnEntity(entity.getMoref(), priv, privileges);
    }

    public Map<String, Boolean> hasUserPrivilegesOnEntity(final ManagedObjectReference moref, final String priv,
            final String... privileges) throws RuntimeFaultFaultMsg {

        final List<ManagedObjectReference> mos = new ArrayList<>();
        mos.add(moref);
        final List<String> privId = new ArrayList<>();
        privId.add(priv);
        if (privileges != null) {
            privId.addAll(Arrays.asList(privileges));
        }
        final Map<String, Boolean> entitlePrivilages = new HashMap<>();
        for (final EntityPrivilege pr : this.vimPort.hasUserPrivilegeOnEntities(this.auth, mos,
                this.userSession.getUserName(), privId)) {
            for (final PrivilegeAvailability aval : pr.getPrivAvailability()) {
                entitlePrivilages.put(aval.getPrivId(), aval.isIsGranted());
            }
        }
        return entitlePrivilages;

    }

}
