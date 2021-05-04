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
package com.vmware.safekeeping.core.type.fco.managers;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.enums.AdapterType;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.ParaVirtualSCSIController;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VirtualBusLogicController;
import com.vmware.vim25.VirtualController;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceFileBackingInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualLsiLogicSASController;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualSCSIController;

public class VirtualMachineConfigManager {

    private static final Logger logger = Logger.getLogger(VirtualMachineConfigManager.class.getName());

    private VirtualMachineConfigInfo config;

    private final VimConnection vimConn;
    private List<VmdkInfo> vmdkInfo;

    private final ManagedObjectReference mor;

    /**
     * Constructor
     *
     * @param conn
     * @param vmMor
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public VirtualMachineConfigManager(final VimConnection conn, final ManagedObjectReference vmMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        this.mor = vmMor;
        this.vimConn = conn;
        this.config = this.vimConn.getVirtualMachineConfigInfo(vmMor);
    }

    /**
     * Constructor
     *
     * @param conn
     * @param vmMor
     * @param config
     */
    public VirtualMachineConfigManager(final VimConnection conn, final ManagedObjectReference vmMor,
            final VirtualMachineConfigInfo config) {
        this.mor = vmMor;
        this.vimConn = conn;
        this.config = config;
    }

    /**
     * Return the number of virtual disk attached to the VM
     *
     * @return number of disks
     */
    public int countVirtualDisk() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        int result = 0;
        final List<VirtualDevice> devices = this.config.getHardware().getDevice();
        if (devices != null) {
            for (final VirtualDevice device : devices) {
                final VirtualDeviceBackingInfo vdbi = device.getBacking();
                if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
                    ++result;
                }
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Return the controller type
     *
     * @param ckey Controller key
     * @return the adapter type
     * @see AdapterType
     */
    private AdapterType getAdapterType(final int ckey) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int - start"); //$NON-NLS-1$
        }

        AdapterType result = AdapterType.UNKNOWN;
        final VirtualDevice vd = searchVirtualDeviceWithKey(ckey);
        if (vd != null) {
            result = AdapterType.getAdapterType(vd);
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int - end"); //$NON-NLS-1$
        }
        return result;
    }

    public List<String> getAllDiskNameList() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final LinkedList<String> result = new LinkedList<>();

        final List<VirtualDevice> devices = this.config.getHardware().getDevice();
        if (devices != null) {
            for (final VirtualDevice device : devices) {

                final VirtualDeviceBackingInfo vdbi = device.getBacking();
                if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
                    final String fn = ((VirtualDeviceFileBackingInfo) vdbi).getFileName();
                    result.add(fn);
                }
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Return any datastore with a VMDK associated with the VM
     * 
     * @return
     * @throws VimObjectNotExistException
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public List<ManagedEntityInfo> getVmdkDatastores()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimObjectNotExistException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final LinkedList<ManagedEntityInfo> result = new LinkedList<>();

        final List<VirtualDevice> devices = this.config.getHardware().getDevice();
        if (devices != null) {
            for (final VirtualDevice device : devices) {

                final VirtualDeviceBackingInfo vdbi = device.getBacking();
                if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
                    final ManagedObjectReference fn = ((VirtualDeviceFileBackingInfo) vdbi).getDatastore();
                    result.add(vimConn.getManagedEntityInfo(fn));
                }
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    public List<VmdkInfo> getAllVmdkInfo() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        List<VmdkInfo> result;
        if (this.vmdkInfo != null) {
            result = this.vmdkInfo;
        } else {
            result = new LinkedList<>();

            final List<VirtualDevice> devices = this.config.getHardware().getDevice();
            if (devices != null) {
                int diskId = 0;
                for (final VirtualDevice device : devices) {
                    final VirtualDeviceBackingInfo vdbi = device.getBacking();
                    if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
                        final VirtualDisk diskDev = (VirtualDisk) device;
                        final Integer ckey = diskDev.getControllerKey();
                        final AdapterType type = getAdapterType(ckey);
                        final int busNumber = getBusNumber(ckey);

                        final VmdkInfo a = new VmdkInfo(diskId, type, busNumber, (VirtualDeviceFileBackingInfo) vdbi,
                                diskDev);
                        result.add(a);
                        diskId++;
                    }
                }
            }
            this.vmdkInfo = result;
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;

    }

    private int getBusNumber(final int ckey) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int - start"); //$NON-NLS-1$
        }

        int result = -1;
        final VirtualDevice vd = searchVirtualDeviceWithKey(ckey);
        if (vd instanceof VirtualController) {
            result = ((VirtualController) vd).getBusNumber();
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     *
     * @return
     */
    public List<VirtualControllerManager> getControllersInfo() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final List<VirtualControllerManager> result = new LinkedList<>();

        final List<VirtualDevice> devices = this.config.getHardware().getDevice();
        if (devices != null) {
            for (final VirtualDevice device : devices) {
                if ((device instanceof ParaVirtualSCSIController) || (device instanceof VirtualLsiLogicSASController)
                        || (device instanceof VirtualLsiLogicController)
                        || (device instanceof VirtualBusLogicController)) {
                    final VirtualSCSIController controller = (VirtualSCSIController) device;
                    final VirtualControllerManager ctrl = new VirtualControllerManager(controller);
                    result.add(ctrl);
                }

            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    public String getExtraConfig(final String key) {
        for (final OptionValue value : this.config.getExtraConfig()) {
            if (value.getKey().equals(key)) {
                return value.getValue().toString();
            }
        }
        return null;
    }

    public String getFtMetadataDirectory() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.config != null) {
            final String returnString = this.config.getFiles().getFtMetadataDirectory();
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnString;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return null;
        }
    }

    public String getInstanceUuid() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final String returnString = this.config.getInstanceUuid();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public String getLogDirectory() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.config != null) {
            final String returnString = this.config.getFiles().getLogDirectory();
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnString;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return null;
        }
    }

    public String getSnapshotDirectory() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.config != null) {
            final String returnString = this.config.getFiles().getSnapshotDirectory();
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnString;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return null;
        }
    }

    public String getSuspendDirectory() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        String result = null;
        if (this.config != null) {
            result = this.config.getFiles().getSuspendDirectory();
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    public VirtualMachineConfigInfo getVirtualMachineConfigInfo() {
        return this.config;
    }

    public ManagedObjectReference getVmMor() {
        return this.mor;
    }

    /**
     * Gets the value of the vmPathName property.
     *
     * @return
     */
    public String getVmPathName() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if (this.config != null) {
            final String returnString = this.config.getFiles().getVmPathName();
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return returnString;
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("<no args> - end"); //$NON-NLS-1$
            }
            return null;
        }
    }

    public boolean isChangeTrackingEnabled() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        boolean result = false;
        if (this.config != null) {
            result = Boolean.TRUE.equals(this.config.isChangeTrackingEnabled());
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Gets the value of vm/config/template property.
     *
     */
    public boolean isTemplate() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final boolean returnboolean = this.config.isTemplate();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnboolean;
    }

    private VirtualDevice searchVirtualDeviceWithKey(final int deviceKey) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int - start"); //$NON-NLS-1$
        }

        VirtualDevice result = null;
        final List<VirtualDevice> devices = this.config.getHardware().getDevice();
        if (devices != null) {
            for (final VirtualDevice device : devices) {

                final int key = device.getKey();
                if (key == deviceKey) {
                    result = device;
                    break;
                }
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("int - end"); //$NON-NLS-1$
        }
        return result;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.config);

    }

    public void update() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        this.config = this.vimConn.getVirtualMachineConfigInfo(this.mor);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

}
