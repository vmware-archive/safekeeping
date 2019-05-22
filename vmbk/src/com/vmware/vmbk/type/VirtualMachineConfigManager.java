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
package com.vmware.vmbk.type;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vmware.vim25.ID;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ParaVirtualSCSIController;
import com.vmware.vim25.VirtualBusLogicController;
import com.vmware.vim25.VirtualController;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceFileBackingInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualLsiLogicSASController;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualNVMEController;
import com.vmware.vim25.VirtualSATAController;
import com.vmware.vmbk.soap.VimConnection;

public class VirtualMachineConfigManager {

    private final VirtualMachineConfigInfo config;

    private final VimConnection vimConn;

    private List<VmdkInfo> vmdkInfo;
    private final ManagedObjectReference mor;

    /**
     * Constructor
     *
     * @param conn
     * @param vmMor
     */
    VirtualMachineConfigManager(final VimConnection conn, final ManagedObjectReference vmMor) {
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
    VirtualMachineConfigManager(final VimConnection conn, final ManagedObjectReference vmMor,
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
    int countVirtualDisk() {
	int count = 0;
	final List<VirtualDevice> devices = this.config.getHardware().getDevice();
	if (devices != null) {
	    for (final VirtualDevice device : devices) {
		final VirtualDeviceBackingInfo vdbi = device.getBacking();
		if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
		    ++count;
		}
	    }
	}
	return count;
    }

    /**
     * Return the controller type
     *
     * @param ckey Controller key
     * @return the adapter type
     * @see AdapterType
     */
    private AdapterType getAdapterType(final int ckey) {
	final VirtualDevice vd = searchVirtualDeviceWithKey(ckey);
	if (vd == null) {
	    return AdapterType.UNKNOWN;
	}
	assert vd.getKey() == ckey;

	AdapterType ret = AdapterType.UNKNOWN;
	if (vd instanceof VirtualIDEController) {
	    ret = AdapterType.IDE;
	} else if (vd instanceof VirtualBusLogicController) {
	    ret = AdapterType.BUSLOGIC;
	} else if (vd instanceof VirtualLsiLogicController) {
	    ret = AdapterType.LSILOGIC;
	} else if (vd instanceof VirtualLsiLogicSASController) {
	    ret = AdapterType.LSILOGICSAS;
	} else if (vd instanceof ParaVirtualSCSIController) {
	    ret = AdapterType.PVSCSI;
	} else if (vd instanceof VirtualSATAController) {
	    ret = AdapterType.SATA;
	} else if (vd instanceof VirtualNVMEController) {
	    ret = AdapterType.NVME;
	}

	return ret;
    }

    public List<String> getAllDiskNameList() {
	final LinkedList<String> ret = new LinkedList<>();

	final List<VirtualDevice> devices = this.config.getHardware().getDevice();
	if (devices != null) {
	    for (final VirtualDevice device : devices) {

		final VirtualDeviceBackingInfo vdbi = device.getBacking();
		if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
		    final String fn = ((VirtualDeviceFileBackingInfo) vdbi).getFileName();
		    ret.add(fn);
		}
	    }
	}
	return ret;
    }

    public List<VmdkInfo> getAllVmdkInfo() {
	if (this.vmdkInfo != null) {
	    return this.vmdkInfo;
	} else {
	    return getAllVmdkInfo(null);
	}
    }

    public List<VmdkInfo> getAllVmdkInfo(final Map<Integer, ID> storageObjectAssociations) {
	if (this.vmdkInfo != null) {
	    return this.vmdkInfo;
	}
	this.vmdkInfo = new LinkedList<>();

	final List<VirtualDevice> devices = this.config.getHardware().getDevice();
	if (devices != null) {
	    for (final VirtualDevice device : devices) {
		final VirtualDeviceBackingInfo vdbi = device.getBacking();
		if ((device instanceof VirtualDisk) && (vdbi instanceof VirtualDeviceFileBackingInfo)) {
		    final VirtualDisk diskDev = (VirtualDisk) device;
		    final int key = diskDev.getKey();
		    final Integer ckeyI = diskDev.getControllerKey();
		    assert ckeyI != null;
		    final int ckey = ckeyI.intValue();
		    final AdapterType type = getAdapterType(ckey);
		    final int busNumber = getBusNumber(ckey);

		    ID improvedVirtualDiskId = null;
		    if (storageObjectAssociations != null) {
			for (final Integer stAssociation : storageObjectAssociations.keySet()) {
			    if (stAssociation == key) {
				improvedVirtualDiskId = storageObjectAssociations.get(key);
				break;
			    }
			}
		    }

		    final VmdkInfo a = new VmdkInfo(ckey, type, busNumber, improvedVirtualDiskId, vdbi, diskDev);

		    this.vmdkInfo.add(a);
		}
	    }
	}
	return this.vmdkInfo;
    }

    private int getBusNumber(final int ckey) {
	final VirtualDevice vd = searchVirtualDeviceWithKey(ckey);
	if (vd == null) {
	    return -1;
	}

	if (vd instanceof VirtualController) {
	    return ((VirtualController) vd).getBusNumber();
	} else {

	    return -1;
	}
    }

    public String getFtMetadataDirectory() {
	if (this.config != null) {
	    return this.config.getFiles().getFtMetadataDirectory();
	} else {
	    return null;
	}
    }

    public String getInstanceUuid() {
	return this.config.getInstanceUuid();
    }

    public String getLogDirectory() {
	if (this.config != null) {
	    return this.config.getFiles().getLogDirectory();
	} else {
	    return null;
	}
    }

    public String getSnapshotDirectory() {
	if (this.config != null) {
	    return this.config.getFiles().getSnapshotDirectory();
	} else {
	    return null;
	}
    }

    public String getSuspendDirectory() {
	if (this.config != null) {
	    return this.config.getFiles().getSuspendDirectory();
	} else {
	    return null;
	}
    }

    public VirtualMachineConfigInfo getVirtualMachineConfigInfo() {
	return this.config;
    }

    public ManagedObjectReference getVmMor() {
	return this.mor;
    }

    public String getVmPathName() {
	if (this.config != null) {
	    return this.config.getFiles().getVmPathName();
	} else {
	    return null;
	}
    }

    public Boolean isChangeTrackingEnabled() {
	if (this.config != null) {
	    return this.config.isChangeTrackingEnabled();
	}
	return false;
    }

    public boolean isTemplate() {
	return this.config.isTemplate();
    }

    private VirtualDevice searchVirtualDeviceWithKey(final int deviceKey) {
	final List<VirtualDevice> devices = this.config.getHardware().getDevice();
	if (devices != null) {
	    for (final VirtualDevice device : devices) {

		final int key = device.getKey();
		if (key == deviceKey) {
		    return device;
		}
	    }
	}
	return null;
    }

}
