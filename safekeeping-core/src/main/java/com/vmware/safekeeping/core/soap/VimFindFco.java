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
package com.vmware.safekeeping.core.soap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.ID;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualMachineConfigInfo;

public class VimFindFco {

    private VimConnection parent;
    private VimPortType vimPort;
    private ManagedObjectReference searchIndex;

    public VimFindFco(VimConnection parent) {

        this.parent = parent;
        vimPort = parent.getVimPort();
        searchIndex = parent.serviceContent.getSearchIndex();

    }

    /**
     * @return
     */
    public VslmConnection getVslmConnection() {
        return parent.getVslmConnection();
    }

    /**
     * @param vmFolder
     * @return
     */
    public ManagedObjectReference findByInventoryPath(final String path) {
        ManagedObjectReference result = null;
        if (StringUtils.isNotBlank(path)) {
            try {
                result = vimPort.findByInventoryPath(searchIndex, path);
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(AbstractConnection.logger, e);
            }
        }
        return result;
    }

    /**
     * @param moref
     * @return
     */
    VirtualAppManager findVAppByMoref(final ManagedObjectReference moref) {
        VirtualAppManager result = null;
        result = new VirtualAppManager(parent, moref);
        return result;
    }

    public VirtualAppManager findVAppByName(final String name) {
        VirtualAppManager result = null;
        final ManagedObjectReference mor = parent.getVAppByName(name);
        if (mor != null) {
            result = new VirtualAppManager(parent, mor);
        }
        return result;
    }

    /**
     * @param uuid
     * @return
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    VirtualAppManager findVAppByUuid(final String uuid) {
        VirtualAppManager result = null;
        final ManagedObjectReference mor = parent.getVAppByUuid(uuid);
        if (mor != null) {
            result = new VirtualAppManager(parent, mor);
        }
        return result;
    }

    /**
     * Finds a virtual machine or host by IP address, where the IP address is in
     * dot-decimal notation. For example, 10.17.12.12. The IP address for a virtual
     * machine is the one returned from VMware tools, ipAddress.
     *
     * @param ip The IP to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     */
    VirtualMachineManager findVmByIp(final String ip)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        VirtualMachineManager result = null;

        final ManagedObjectReference mor = vimPort.findByIp(searchIndex, null, ip, true);
        result = new VirtualMachineManager(parent, mor);

        return result;
    }

    /**
     * Finds a virtual machine by the moref.
     *
     * @param moref The moref to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    VirtualMachineManager findVmByMoref(final ManagedObjectReference moref)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return new VirtualMachineManager(parent, moref);
    }

    public VirtualMachineManager findVmByName(final String name)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        VirtualMachineManager result = null;
        final ManagedObjectReference mor = parent.getVmByName(name);
        if (mor != null) {
            result = new VirtualMachineManager(parent, name, mor);
        }
        return result;
    }

    /**
     * Finds a virtual machine by BIOS or instance UUID.
     *
     * @param uuid         The UUID to find.
     * @param instanceUuid If true, search for virtual machines whose instance UUID
     *                     matches the given uuid. Otherwise, search for virtual
     *                     machines whose BIOS UUID matches the given uuid.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     */
    public VirtualMachineManager findVmByUuid(final String uuid, final boolean instanceUuid)
            throws InvalidPropertyFaultMsg, InterruptedException {
        VirtualMachineManager result = null;
        try {
            final ManagedObjectReference mor = vimPort.findByUuid(searchIndex, null, uuid, true, instanceUuid);
            result = new VirtualMachineManager(parent, mor);
        } catch (final RuntimeFaultFaultMsg e) {
            Utility.logWarning(AbstractConnection.logger, e);

        }
        return result;
    }

    public List<VirtualAppManager> findAnyVapp(final String filter)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        ManagedObjectReference folder = null;
        if ((filter != null) && !filter.isEmpty()) {
            folder = vimPort.findByInventoryPath(searchIndex, filter);
        }
        final Map<String, Vector<Object>> tmpList = parent.getVimHelper().getAllVAppList(folder);
        final List<VirtualAppManager> result = new LinkedList<>();
        int i = 0;
        for (final Entry<String, Vector<Object>> entry : tmpList.entrySet()) {
            final ManagedObjectReference mor = (ManagedObjectReference) entry.getValue().get(0);
            final VAppConfigInfo config = (VAppConfigInfo) entry.getValue().get(1);

            final VirtualAppManager vApp = new VirtualAppManager(parent, entry.getKey(), mor, config);
            if (AbstractConnection.logger.isLoggable(Level.INFO)) {
                final String msg = String.format("%d: %s %s %s", i, vApp.getUuid(), vApp.getMorefValue(),
                        vApp.getName());
                AbstractConnection.logger.info(msg);
            }
            result.add(vApp);
            i++;
        }
        return result;
    }

    public List<VirtualMachineManager> findAnyVm(final String filter)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        ManagedObjectReference folder = null;
        if ((filter != null) && !filter.isEmpty()) {
            folder = vimPort.findByInventoryPath(searchIndex, filter);
        }
        final Map<String, Vector<Object>> tmpList = parent.getVimHelper().getAllVmList(folder);
        final List<VirtualMachineManager> ret = new LinkedList<>();
        int i = 0;
        for (final Entry<String, Vector<Object>> entry : tmpList.entrySet()) {
            final ManagedObjectReference mor = (ManagedObjectReference) entry.getValue().get(0);
            final VirtualMachineConfigInfo config = (VirtualMachineConfigInfo) entry.getValue().get(1);

            final VirtualMachineManager vmm = new VirtualMachineManager(parent, entry.getKey(), mor, config);
            if (AbstractConnection.logger.isLoggable(Level.INFO)) {
                final String msg = String.format("%d: %s %s %s", i, vmm.getUuid(), vmm.getMorefValue(), vmm.getName());
                AbstractConnection.logger.info(msg);
            }
            ret.add(vmm);
            i++;

        }
        return ret;
    }

    public ImprovedVirtualDisk findIvdById(final ID id) {
        return getVslmConnection().getIvdById(id);
    }

    public ImprovedVirtualDisk findIvdById(final String id) {
        return getVslmConnection().getIvdById(id);
    }

    public List<ImprovedVirtualDisk> findIvdByName(final String name) {
        return getVslmConnection().getIvdByName(name);
    }

    public List<ImprovedVirtualDisk> findAnyIvd()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return getVslmConnection().getIvdList();
    }

}
