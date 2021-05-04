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

public final class PrivilegesList {
    private PrivilegesList() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PRIVILEGE_ENABLE_METHOD = "Global.EnableMethods";

    public static final String PRIVILEGE_DISABLE_METHOD = "Global.DisableMethods";
    // Allow virtual machine download
    public static final String PRIVILEGE_VIRTUALMACHINE_PROVISIONING_GETVMFILE = "VirtualMachine.Provisioning.GetVmFiles";
    // Create snapshot
    public static final String PRIVILEGE_VIRTUALMACHINE_STATE_CREATESNAPSHOT = "VirtualMachine.State.CreateSnapshot";
//  Remove snapshot
    public static final String PRIVILEGE_VIRTUALMACHINE_STATE_REMOVESNAPSHOT = "VirtualMachine.State.RemoveSnapshot";
    // Acquire disk lease
    public static final String PRIVILEGE_VIRTUALMACHINE_CONFIG_DISKLEASE = "VirtualMachine.Config.DiskLease";
    // Create new
    public static final String PRIVILEGE_VIRTUALMACHINE_INVENTORY_CREATE = "VirtualMachine.Inventory.Create";
// Remove
    public static final String PRIVILEGE_VIRTUALMACHINE_INVENTORY_DELETE = "VirtualMachine.Inventory.Delete";

    // Change Settings
    public static final String PRIVILEGE_VIRTUALMACHINE_CONFIG_SETTINGS = "VirtualMachine.Config.Settings";
    // Change resource
    public static final String PRIVILEGE_VIRTUALMACHINE_CONFIG_RESOURCE = "VirtualMachine.Config.Resource";

    // Assign virtual machine to resource pool
    public static final String PRIVILEGE_RESOURCE_ASSIGNVMTOPOOL = "Resource.AssignVMToPool";
    // Add new disk
    public static final String PRIVILEGE_VIRTUALMACHINE_CONFIG_ADD_NEW_DISK = "VirtualMachine.Config.AddNewDisk";

    // Toggle disk change tracking
    public static final String PRIVILEGE_VIRTUALMACHINE_CONFIG_CHANGE_TRACKING = "VirtualMachine.Config.ChangeTracking";

    // Advanced configuration
    public static final String PRIVILEGE_VIRTUALMACHINE_CONFIG_ADVANCEDCONFIG = "VirtualMachine.Config.AdvancedConfig";

    // Allocate space
    public static final String PRIVILEGE_DATASTORE_ALLOCATE_SPACE = "Datastore.AllocateSpace";

    // Browse datastore
    public static final String PRIVILEGE_DATASTORE_BROWSE = "Datastore.Browse";

    // Low level file operations
    public static final String PRIVILEGE_DATASTORE_FILEMANAGEMENT = "Datastore.FileManagement";

    // Assign network
    public static final String PRIVILEGE_NETWORK_ASSIGN = "Network.Assign";

    // Encrypt new
    public static final String PRIVILEGE_CRYPTOGRAPHER_ENCRYPT_NEW = "Cryptographer.EncryptNew";
    // Grants access to unencrypted or cleartext data of encrypted VMs
    public static final String PRIVILEGE_CRYPTOGRAPHER_ACCESS = "Cryptographer.Access";
    // Power on or resume a virtual machine
    public static final String PRIVILEGE_VIRTUALMACHINE_INTERACT_POWERON = "VirtualMachine.Interact.PowerOn";

    // Power off a virtual machine
    public static final String PRIVILEGE_VIRTUALMACHINE_INTERACT_POWEROFF = "VirtualMachine.Interact.PowerOff";

    // Allow read-only disk access
    public static final String PRIVILEGE_VIRTUALMACHINE_PROVISIONING_DISK_RANDOM_READ = "VirtualMachine.Provisioning.DiskRandomRead";

    // Profile-driven storage view
    public static final String PRIVILEGE_STORAGEPROFILE_VIEW = "StorageProfile.View";

    // Mark as template
    public static final String PRIVILEGE_VIRTUALMACHINE_PROVISIONING_MARK_AS_TEMPLATE = "VirtualMachine.Provisioning.MarkAsTemplate";

    // Mark as virtual machine
    public static final String PRIVILEGE_VIRTUALMACHINE_PROVISIONING_MARK_AS_VM = "VirtualMachine.Provisioning.MarkAsVM";
//Allow access to files through a separate NFC connection
    public static final String PRIVILEGE_VIRTUALMACHINE_ALLOW_FILE_ACCESS = "VirtualMachine.Provisioning.FileRandomAccess";
    // Allow random access to disk files through a separate NFC connection
    public static final String PRIVILEGE_VIRTUALMACHINE_ALLOW_DISK_ACCESS = "VirtualMachine.Provisioning.DiskRandomAccess";
    // Allow upload of virtual machine
    public static final String PRIVILEGE_VIRTUALMACHINE_PROVISIONING_PUTVMFILES = "VirtualMachine.Provisioning.PutVmFiles";
    // Create a VApp
    public static final String PRIVILEGE_VAPP_CREATE = "VApp.Create";

    // Power on or resume a virtual machine
    public static final String PRIVILEGE_VAPP_POWERON = "VApp.PowerOn";

    // Power on or resume a virtual machine
    public static final String PRIVILEGE_VAPP_POWEROFF = "VApp.PowerOff";

    // vApp resource configuration
    public static final String PRIVILEGE_VAPP_RESOURCE_CONFIG = "VApp.ResourceConfig";
    // Add a virtual machine to the vApp
    public static final String PRIVILEGE_VAPP_ASSIGN_VM = "VApp.AssignVM";
    // Assign resource pool to vApp
    public static final String PRIVILEGE_VAPP_ASSIGN_RESOURCEGROUP = "VApp.AssignResourcePool";
    // Assign vApp
    public static final String PRIVILEGE_VAPP_ASSIGN_VAPP = "VApp.AssignVApp";
    // Delete a vApp
    public static final String PRIVILEGE_VAPP_DELETE = "VApp.Delete";
    // vApp instance configuration
    public static final String PRIVILEGE_VAPP_INSTANCE_CONFIG = "VApp.InstanceConfig";
    // vApp resource configuration
    public static final String PRIVILEGE_VAPP_RESOURCECONFIG = "VApp.ResourceConfig";
    // vApp instance configuration
    public static final String PRIVILEGE_VAPP_INSTANCECONFIG = "VApp.InstanceConfig";
    // vApp application configuration
    public static final String PRIVILEGE_VAPP_APPLICATIONCONFIG = "VApp.ApplicationConfig";

    // Delete a vApp
    public static final String PRIVILEGE_VAPP_RENAME = "VApp.Rename";

}
