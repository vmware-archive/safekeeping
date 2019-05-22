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

public enum EntityType {
    Alarm, AlarmManager, AuthorizationManager, CertificateManager, ClusterComputeResource, ClusterEVCManager,
    ClusterProfile, ClusterProfileManager, ComputeResource, ContainerView, CryptoManager, CryptoManagerKmip,
    CustomFieldsManager, CustomizationSpecManager, Datacenter, Datastore, DatastoreNamespaceManager, DiagnosticManager,
    DistributedVirtualPortgroup, DistributedVirtualSwitch, DistributedVirtualSwitchManager, EnvironmentBrowser,
    EventHistoryCollector, EventManager, ExtensibleManagedObject, ExtensionManager, FailoverClusterConfigurator,
    FailoverClusterManager, FileManager, Folder, GuestAliasManager, GuestAuthManager, GuestFileManager,
    GuestOperationsManager, GuestProcessManager, GuestWindowsRegistryManager, HealthUpdateManager, HistoryCollector,
    HostAccessManager, HostActiveDirectoryAuthentication, HostAuthenticationManager, HostAuthenticationStore,
    HostAutoStartManager, HostBootDeviceSystem, HostCacheConfigurationManager, HostCertificateManager,
    HostCpuSchedulerSystem, HostDatastoreBrowser, HostDatastoreSystem, HostDateTimeSystem, HostDiagnosticSystem,
    HostDirectoryStore, HostEsxAgentHostManager, HostFirewallSystem, HostFirmwareSystem, HostGraphicsManager,
    HostHealthStatusSystem, HostImageConfigManager, HostKernelModuleSystem, HostLocalAccountManager,
    HostLocalAuthentication, HostMemorySystem, HostNetworkSystem, HostPatchManager, HostPciPassthruSystem,
    HostPowerSystem, HostProfile, HostProfileManager, HostServiceSystem, HostSnmpSystem, HostSpecificationManager,
    HostStorageSystem, HostSystem, HostVFlashManager, HostVirtualNicManager, HostVMotionSystem, HostVsanInternalSystem,
    HostVsanSystem, HostVStorageObjectManager, HttpNfcLease, ImprovedVirtualDisk, InventoryView, IoFilterManager,
    IpPoolManager, IscsiManager, LicenseAssignmentManager, LicenseManager, ListView, LocalizationManager, ManagedEntity,
    ManagedObjectView, MessageBusProxy, Network, OpaqueNetwork, OptionManager, OverheadMemoryManager, OvfManager,
    PerformanceManager, Profile, ProfileComplianceManager, ProfileManager, PropertyCollector, PropertyFilter,
    ResourcePlanningManager, ResourcePool, ScheduledTask, ScheduledTaskManager, SearchIndex, ServiceInstance,
    ServiceManager, SessionManager, SimpleCommand, StoragePod, StorageResourceManager, Task, TaskHistoryCollector,
    TaskManager, UserDirectory, VcenterVStorageObjectManager, View, ViewManager, VirtualApp, VirtualDiskManager,
    VirtualizationManager, VirtualMachine, VirtualMachineCompatibilityChecker, VirtualMachineProvisioningChecker,
    VirtualMachineSnapshot, VmwareDistributedVirtualSwitch, VsanUpgradeSystem, VStorageObjectManagerBase, K8sNamespace;

    public static EntityType toEntityType(final String type) {
	if (type.equals("ivd")) {
	    return EntityType.ImprovedVirtualDisk;
	}
	if (type.equals("vm")) {
	    return EntityType.VirtualMachine;
	}
	if (type.equals("vapp")) {
	    return EntityType.VirtualApp;
	}
	if (type.equals("k8s")) {
	    return EntityType.K8sNamespace;
	}
	return EntityType.valueOf(type);
    }

    public String toString(final boolean toReport) {
	if (toReport) {
	    switch (this) {
	    case VirtualApp:
		return "vapp";
	    case VirtualMachine:
		return "vm";
	    case ImprovedVirtualDisk:
		return "ivd";
	    case K8sNamespace:
		return "k8s";
	    default:
		return toString();
	    }
	} else {
	    return toString();
	}
    }

}
