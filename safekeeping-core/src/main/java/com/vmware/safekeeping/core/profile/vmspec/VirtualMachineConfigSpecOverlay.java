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
package com.vmware.safekeeping.core.profile.vmspec;

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.profile.vmspec.VirtualDeviceOverlay.DeviceType;
import com.vmware.vim25.CryptoSpec;
import com.vmware.vim25.FaultToleranceConfigInfo;
import com.vmware.vim25.LatencySensitivity;
import com.vmware.vim25.ManagedByInfo;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.ReplicationConfigSpec;
import com.vmware.vim25.ResourceAllocationInfo;
import com.vmware.vim25.ScheduledHardwareUpgradeInfo;
import com.vmware.vim25.ToolsConfigInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualMachineAffinityInfo;
import com.vmware.vim25.VirtualMachineBootOptions;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineConsolePreferences;
import com.vmware.vim25.VirtualMachineCpuIdInfoSpec;
import com.vmware.vim25.VirtualMachineDefaultPowerOpInfo;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineFlagInfo;
import com.vmware.vim25.VirtualMachineNetworkShaperInfo;
import com.vmware.vim25.VirtualMachineProfileSpec;
import com.vmware.vim25.VmConfigSpec;

public class VirtualMachineConfigSpecOverlay {
    private String alternateGuestName;

    private String annotation;

    private VirtualMachineBootOptions bootOptions;

    private Boolean changeTrackingEnabled;

    private String changeVersion;

    private VirtualMachineConsolePreferences consolePreferences;

    private VirtualMachineAffinityInfo cpuAffinity;

    private ResourceAllocationInfo cpuAllocation;

    private List<VirtualMachineCpuIdInfoSpec> cpuFeatureMask;

    private Boolean cpuHotAddEnabled;

    private Boolean cpuHotRemoveEnabled;

    private CryptoSpec crypto;

    private List<VirtualDeviceConfigSpecOverlay> deviceChange;

    private List<OptionValue> extraConfig;

    private VirtualMachineFileInfo files;

    private String firmware;

    private VirtualMachineFlagInfo flags;

    private FaultToleranceConfigInfo ftInfo;

    private Boolean guestAutoLockEnabled;

    private String guestId;

    private String instanceUuid;

    private LatencySensitivity latencySensitivity;

    private String locationId;

    private ManagedByInfo managedBy;

    private Integer maxMksConnections;

    private VirtualMachineAffinityInfo memoryAffinity;

    private ResourceAllocationInfo memoryAllocation;

    private Boolean memoryHotAddEnabled;

    private Long memoryMB;

    private Boolean memoryReservationLockedToMax;

    private Boolean messageBusTunnelEnabled;

    private String migrateEncryption;

    private String name;

    private Boolean nestedHVEnabled;

    private VirtualMachineNetworkShaperInfo networkShaper;

    private Short npivDesiredNodeWwns;

    private Short npivDesiredPortWwns;

    private List<Long> npivNodeWorldWideName;

    private Boolean npivOnNonRdmDisks;

    private List<Long> npivPortWorldWideName;

    private Boolean npivTemporaryDisabled;

    private String npivWorldWideNameOp;

    private String npivWorldWideNameType;

    private Integer numCoresPerSocket;

    private Integer numCPUs;

    private VirtualMachineDefaultPowerOpInfo powerOpInfo;

    private ReplicationConfigSpec repConfig;

    private ScheduledHardwareUpgradeInfo scheduledHardwareUpgradeInfo;

    private String swapPlacement;

    private ToolsConfigInfo tools;

    private String uuid;

    private VmConfigSpec vAppConfig;

    private Boolean vAppConfigRemoved;

    private Boolean vAssertsEnabled;

    private String version;

    private Boolean virtualICH7MPresent;

    private Boolean virtualSMCPresent;

    private List<VirtualMachineProfileSpec> vmProfile;

    private Boolean vpmcEnabled;

    public VirtualMachineConfigSpecOverlay() {
        this.npivPortWorldWideName = new ArrayList<>();
        this.npivNodeWorldWideName = new ArrayList<>();
        this.extraConfig = new ArrayList<>();
        this.deviceChange = new ArrayList<>();
    }

    public VirtualMachineConfigSpecOverlay(final VirtualMachineConfigInfo config) throws ProfileException {
        this();
        try {

            setName(config.getName());
            setVersion(config.getVersion());

            setGuestId(config.getGuestId());
            setAlternateGuestName(config.getAlternateGuestName());
            setAnnotation(config.getAnnotation());
            setTools(config.getTools());
            setFlags(config.getFlags());
            setPowerOpInfo(config.getDefaultPowerOps());
            setNumCPUs(config.getHardware().getNumCPU());
            setNumCoresPerSocket(config.getHardware().getNumCoresPerSocket());
            setMemoryMB((long) config.getHardware().getMemoryMB());
            setMemoryHotAddEnabled(config.isMemoryHotAddEnabled());
            setCpuHotAddEnabled(config.isCpuHotAddEnabled());
            setCpuHotRemoveEnabled(config.isCpuHotRemoveEnabled());
            setVirtualICH7MPresent(config.getHardware().isVirtualICH7MPresent());
            setVirtualSMCPresent(config.getHardware().isVirtualSMCPresent());
            setUuid(config.getUuid());
            setInstanceUuid(config.getInstanceUuid());
            setLocationId(config.getLocationId());
            setFiles(config.getFiles());
            setConsolePreferences(config.getConsolePreferences());

            if (config.getNpivNodeWorldWideName() != null) {
                this.npivNodeWorldWideName.addAll(config.getNpivNodeWorldWideName());
            }
            if (config.getNpivPortWorldWideName() != null) {
                this.npivPortWorldWideName.addAll(config.getNpivPortWorldWideName());
            }

            setNpivDesiredNodeWwns(config.getNpivDesiredNodeWwns());
            setNpivDesiredPortWwns(config.getNpivDesiredPortWwns());

            setCpuAllocation(config.getCpuAllocation());
            setMemoryAllocation(config.getMemoryAllocation());
            setLatencySensitivity(config.getLatencySensitivity());
            setCpuAffinity(config.getCpuAffinity());
            setMemoryAffinity(config.getMemoryAffinity());
            setNetworkShaper(config.getNetworkShaper());

            this.extraConfig.addAll(config.getExtraConfig());
            setBootOptions(config.getBootOptions());
            setChangeTrackingEnabled(config.isChangeTrackingEnabled());
            setFirmware(config.getFirmware());
            setMaxMksConnections(config.getMaxMksConnections());
            setGuestAutoLockEnabled(config.isGuestAutoLockEnabled());

            setMemoryReservationLockedToMax(config.isMemoryReservationLockedToMax());
            setNestedHVEnabled(config.isNestedHVEnabled());
            setVpmcEnabled(config.isVPMCEnabled());
            setMessageBusTunnelEnabled(config.isMessageBusTunnelEnabled());
            setMigrateEncryption(config.getMigrateEncryption());
            for (final VirtualDevice vd : config.getHardware().getDevice()) {

                final VirtualDeviceConfigSpecOverlay vdcSpec = new VirtualDeviceConfigSpecOverlay(vd);

                if ((vdcSpec.getDevice().getDeviceType() != null)
                        && (vdcSpec.getDevice().getDeviceType() != DeviceType.OTHER)) {
                    this.deviceChange.add(vdcSpec);
                }
            }
        } catch (final Exception e) {
            throw new ProfileException(e);
        }
    }

    public VirtualMachineConfigSpecOverlay(final VirtualMachineConfigSpecOverlay src) {
        this();
        this.alternateGuestName = src.alternateGuestName;

        this.annotation = src.annotation;

        this.bootOptions = src.bootOptions;

        this.changeTrackingEnabled = src.changeTrackingEnabled;

        this.changeVersion = src.changeVersion;

        this.cpuHotAddEnabled = src.cpuHotAddEnabled;

        this.cpuHotRemoveEnabled = src.cpuHotRemoveEnabled;

        for (final VirtualDeviceConfigSpecOverlay dc : src.deviceChange) {
            this.deviceChange.add(new VirtualDeviceConfigSpecOverlay(dc));
        }

        this.firmware = src.firmware;

        this.guestAutoLockEnabled = src.guestAutoLockEnabled;

        this.guestId = src.guestId;

        this.instanceUuid = src.instanceUuid;

        this.locationId = src.locationId;

        this.memoryHotAddEnabled = src.memoryHotAddEnabled;

        this.memoryMB = src.memoryMB;

        this.memoryReservationLockedToMax = src.memoryReservationLockedToMax;

        this.messageBusTunnelEnabled = src.messageBusTunnelEnabled;

        this.migrateEncryption = src.migrateEncryption;

        this.name = src.name;

        this.nestedHVEnabled = src.nestedHVEnabled;

        this.npivDesiredNodeWwns = src.npivDesiredNodeWwns;

        this.npivDesiredPortWwns = src.npivDesiredPortWwns;

        this.npivNodeWorldWideName = new ArrayList<>(src.npivNodeWorldWideName);

        this.npivOnNonRdmDisks = src.npivOnNonRdmDisks;
        this.npivPortWorldWideName = new ArrayList<>(src.npivPortWorldWideName);

        this.npivTemporaryDisabled = src.npivTemporaryDisabled;

        this.npivWorldWideNameOp = src.npivWorldWideNameOp;

        this.npivWorldWideNameType = src.npivWorldWideNameType;

        this.numCoresPerSocket = src.numCoresPerSocket;

        this.numCPUs = src.numCPUs;

        this.swapPlacement = src.swapPlacement;

        this.uuid = src.uuid;

        this.maxMksConnections = src.maxMksConnections;

        this.vAppConfigRemoved = src.vAppConfigRemoved;

        this.vAssertsEnabled = src.vAssertsEnabled;

        this.version = src.version;

        this.virtualICH7MPresent = src.virtualICH7MPresent;

        this.virtualSMCPresent = src.virtualSMCPresent;

        this.vpmcEnabled = src.vpmcEnabled;
        // vim25 component

        this.consolePreferences = src.consolePreferences;
        this.tools = src.tools;
        this.powerOpInfo = src.powerOpInfo;
        this.repConfig = src.repConfig;
        this.scheduledHardwareUpgradeInfo = src.scheduledHardwareUpgradeInfo;
        this.flags = src.flags;
        this.ftInfo = src.ftInfo;
        this.extraConfig = src.extraConfig;
        this.files = src.files;
        this.vmProfile = new ArrayList<>(src.vmProfile);
        this.networkShaper = src.networkShaper;
        this.vAppConfig = src.vAppConfig;
        this.cpuAffinity = src.cpuAffinity;

        this.cpuAllocation = src.cpuAllocation;

        this.cpuFeatureMask = src.cpuFeatureMask;

        this.crypto = src.crypto;

        this.latencySensitivity = src.latencySensitivity;

        this.managedBy = src.managedBy;

        this.memoryAffinity = src.memoryAffinity;

        this.memoryAllocation = src.memoryAllocation;
    }

    public String getAlternateGuestName() {
        return this.alternateGuestName;
    }

    public String getAnnotation() {
        return this.annotation;
    }

    public VirtualMachineBootOptions getBootOptions() {
        return this.bootOptions;
    }

    public Boolean getChangeTrackingEnabled() {
        return this.changeTrackingEnabled;
    }

    public String getChangeVersion() {
        return this.changeVersion;
    }

    public VirtualMachineConsolePreferences getConsolePreferences() {
        return this.consolePreferences;
    }

    public VirtualMachineAffinityInfo getCpuAffinity() {
        return this.cpuAffinity;
    }

    public ResourceAllocationInfo getCpuAllocation() {
        return this.cpuAllocation;
    }

    public List<VirtualMachineCpuIdInfoSpec> getCpuFeatureMask() {
        return this.cpuFeatureMask;
    }

    public Boolean getCpuHotAddEnabled() {
        return this.cpuHotAddEnabled;
    }

    public Boolean getCpuHotRemoveEnabled() {
        return this.cpuHotRemoveEnabled;
    }

    public CryptoSpec getCrypto() {
        return this.crypto;
    }

    public List<VirtualDeviceConfigSpecOverlay> getDeviceChange() {
        return this.deviceChange;
    }

    public List<OptionValue> getExtraConfig() {
        return this.extraConfig;
    }

    public VirtualMachineFileInfo getFiles() {
        return this.files;
    }

    public String getFirmware() {
        return this.firmware;
    }

    public VirtualMachineFlagInfo getFlags() {
        return this.flags;
    }

    public FaultToleranceConfigInfo getFtInfo() {
        return this.ftInfo;
    }

    public Boolean getGuestAutoLockEnabled() {
        return this.guestAutoLockEnabled;
    }

    public String getGuestId() {
        return this.guestId;
    }

    public String getInstanceUuid() {
        return this.instanceUuid;
    }

    public LatencySensitivity getLatencySensitivity() {
        return this.latencySensitivity;
    }

    public String getLocationId() {
        return this.locationId;
    }

    public ManagedByInfo getManagedBy() {
        return this.managedBy;
    }

    public Integer getMaxMksConnections() {
        return this.maxMksConnections;
    }

    public VirtualMachineAffinityInfo getMemoryAffinity() {
        return this.memoryAffinity;
    }

    public ResourceAllocationInfo getMemoryAllocation() {
        return this.memoryAllocation;
    }

    public Boolean getMemoryHotAddEnabled() {
        return this.memoryHotAddEnabled;
    }

    public Long getMemoryMB() {
        return this.memoryMB;
    }

    public Boolean getMemoryReservationLockedToMax() {
        return this.memoryReservationLockedToMax;
    }

    public Boolean getMessageBusTunnelEnabled() {
        return this.messageBusTunnelEnabled;
    }

    public String getMigrateEncryption() {
        return this.migrateEncryption;
    }

    public String getName() {
        return this.name;
    }

    public Boolean getNestedHVEnabled() {
        return this.nestedHVEnabled;
    }

    public VirtualMachineNetworkShaperInfo getNetworkShaper() {
        return this.networkShaper;
    }

    public Short getNpivDesiredNodeWwns() {
        return this.npivDesiredNodeWwns;
    }

    public Short getNpivDesiredPortWwns() {
        return this.npivDesiredPortWwns;
    }

    public List<Long> getNpivNodeWorldWideName() {
        return this.npivNodeWorldWideName;
    }

    public Boolean getNpivOnNonRdmDisks() {
        return this.npivOnNonRdmDisks;
    }

    public List<Long> getNpivPortWorldWideName() {
        return this.npivPortWorldWideName;
    }

    public Boolean getNpivTemporaryDisabled() {
        return this.npivTemporaryDisabled;
    }

    public String getNpivWorldWideNameOp() {
        return this.npivWorldWideNameOp;
    }

    public String getNpivWorldWideNameType() {
        return this.npivWorldWideNameType;
    }

    public Integer getNumCoresPerSocket() {
        return this.numCoresPerSocket;
    }

    public Integer getNumCPUs() {
        return this.numCPUs;
    }

    public VirtualMachineDefaultPowerOpInfo getPowerOpInfo() {
        return this.powerOpInfo;
    }

    public ReplicationConfigSpec getRepConfig() {
        return this.repConfig;
    }

    public ScheduledHardwareUpgradeInfo getScheduledHardwareUpgradeInfo() {
        return this.scheduledHardwareUpgradeInfo;
    }

    public String getSwapPlacement() {
        return this.swapPlacement;
    }

    public ToolsConfigInfo getTools() {
        return this.tools;
    }

    public String getUuid() {
        return this.uuid;
    }

    public VmConfigSpec getvAppConfig() {
        return this.vAppConfig;
    }

    public Boolean getvAppConfigRemoved() {
        return this.vAppConfigRemoved;
    }

    public Boolean getvAssertsEnabled() {
        return this.vAssertsEnabled;
    }

    public String getVersion() {
        return this.version;
    }

    public Boolean getVirtualICH7MPresent() {
        return this.virtualICH7MPresent;
    }

    public Boolean getVirtualSMCPresent() {
        return this.virtualSMCPresent;
    }

    public List<VirtualMachineProfileSpec> getVmProfile() {
        return this.vmProfile;
    }

    public Boolean getVpmcEnabled() {
        return this.vpmcEnabled;
    }

    public Boolean isChangeTrackingEnabled() {
        return this.changeTrackingEnabled;
    }

    public Boolean isCpuHotAddEnabled() {
        return this.cpuHotAddEnabled;
    }

    public Boolean isCpuHotRemoveEnabled() {
        return this.cpuHotRemoveEnabled;
    }

    public Boolean isGuestAutoLockEnabled() {
        return this.guestAutoLockEnabled;
    }

    public Boolean isMemoryHotAddEnabled() {
        return this.memoryHotAddEnabled;
    }

    public Boolean isMemoryReservationLockedToMax() {
        return this.memoryReservationLockedToMax;
    }

    public Boolean isMessageBusTunnelEnabled() {
        return this.messageBusTunnelEnabled;
    }

    public Boolean isNestedHVEnabled() {
        return this.nestedHVEnabled;
    }

    public Boolean isNpivOnNonRdmDisks() {
        return this.npivOnNonRdmDisks;
    }

    public Boolean isNpivTemporaryDisabled() {
        return this.npivTemporaryDisabled;
    }

    public Boolean isvAppConfigRemoved() {
        return this.vAppConfigRemoved;
    }

    public Boolean isvAssertsEnabled() {
        return this.vAssertsEnabled;
    }

    public Boolean isVirtualICH7MPresent() {
        return this.virtualICH7MPresent;
    }

    public Boolean isVirtualSMCPresent() {
        return this.virtualSMCPresent;
    }

    public Boolean isVpmcEnabled() {
        return this.vpmcEnabled;
    }

    public void setAlternateGuestName(final String alternateGuestName) {
        this.alternateGuestName = alternateGuestName;
    }

    public void setAnnotation(final String annotation) {
        this.annotation = annotation;
    }

    public void setBootOptions(final VirtualMachineBootOptions bootOptions) {
        this.bootOptions = bootOptions;
    }

    public void setChangeTrackingEnabled(final Boolean changeTrackingEnabled) {
        this.changeTrackingEnabled = changeTrackingEnabled;
    }

    public void setChangeVersion(final String changeVersion) {
        this.changeVersion = changeVersion;
    }

    public void setConsolePreferences(final VirtualMachineConsolePreferences consolePreferences) {
        this.consolePreferences = consolePreferences;
    }

    public void setCpuAffinity(final VirtualMachineAffinityInfo cpuAffinity) {
        this.cpuAffinity = cpuAffinity;
    }

    public void setCpuAllocation(final ResourceAllocationInfo cpuAllocation) {
        this.cpuAllocation = cpuAllocation;
    }

    public void setCpuFeatureMask(final List<VirtualMachineCpuIdInfoSpec> cpuFeatureMask) {
        this.cpuFeatureMask = cpuFeatureMask;
    }

    public void setCpuHotAddEnabled(final Boolean cpuHotAddEnabled) {
        this.cpuHotAddEnabled = cpuHotAddEnabled;
    }

    public void setCpuHotRemoveEnabled(final Boolean cpuHotRemoveEnabled) {
        this.cpuHotRemoveEnabled = cpuHotRemoveEnabled;
    }

    public void setCrypto(final CryptoSpec crypto) {
        this.crypto = crypto;
    }

    public void setDeviceChange(final List<VirtualDeviceConfigSpecOverlay> deviceChange) {
        this.deviceChange = deviceChange;
    }

    public void setExtraConfig(final List<OptionValue> extraConfig) {
        this.extraConfig = extraConfig;
    }

    public void setFiles(final VirtualMachineFileInfo files) {
        this.files = files;
    }

    public void setFirmware(final String firmware) {
        this.firmware = firmware;
    }

    public void setFlags(final VirtualMachineFlagInfo flags) {
        this.flags = flags;
    }

    public void setFtInfo(final FaultToleranceConfigInfo ftInfo) {
        this.ftInfo = ftInfo;
    }

    public void setGuestAutoLockEnabled(final Boolean guestAutoLockEnabled) {
        this.guestAutoLockEnabled = guestAutoLockEnabled;
    }

    public void setGuestId(final String guestId) {
        this.guestId = guestId;
    }

    public void setInstanceUuid(final String instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    public void setLatencySensitivity(final LatencySensitivity latencySensitivity) {
        this.latencySensitivity = latencySensitivity;
    }

    public void setLocationId(final String locationId) {
        this.locationId = locationId;
    }

    public void setManagedBy(final ManagedByInfo managedBy) {
        this.managedBy = managedBy;
    }

    public void setMaxMksConnections(final Integer maxMksConnections) {
        this.maxMksConnections = maxMksConnections;
    }

    public void setMemoryAffinity(final VirtualMachineAffinityInfo memoryAffinity) {
        this.memoryAffinity = memoryAffinity;
    }

    public void setMemoryAllocation(final ResourceAllocationInfo memoryAllocation) {
        this.memoryAllocation = memoryAllocation;
    }

    public void setMemoryHotAddEnabled(final Boolean memoryHotAddEnabled) {
        this.memoryHotAddEnabled = memoryHotAddEnabled;
    }

    public void setMemoryMB(final Long memoryMB) {
        this.memoryMB = memoryMB;
    }

    public void setMemoryReservationLockedToMax(final Boolean memoryReservationLockedToMax) {
        this.memoryReservationLockedToMax = memoryReservationLockedToMax;
    }

    public void setMessageBusTunnelEnabled(final Boolean messageBusTunnelEnabled) {
        this.messageBusTunnelEnabled = messageBusTunnelEnabled;
    }

    public void setMigrateEncryption(final String migrateEncryption) {
        this.migrateEncryption = migrateEncryption;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setNestedHVEnabled(final Boolean nestedHVEnabled) {
        this.nestedHVEnabled = nestedHVEnabled;
    }

    public void setNetworkShaper(final VirtualMachineNetworkShaperInfo networkShaper) {
        this.networkShaper = networkShaper;
    }

    public void setNpivDesiredNodeWwns(final Short npivDesiredNodeWwns) {
        this.npivDesiredNodeWwns = npivDesiredNodeWwns;
    }

    public void setNpivDesiredPortWwns(final Short npivDesiredPortWwns) {
        this.npivDesiredPortWwns = npivDesiredPortWwns;
    }

    public void setNpivNodeWorldWideName(final List<Long> npivNodeWorldWideName) {
        this.npivNodeWorldWideName = npivNodeWorldWideName;
    }

    public void setNpivOnNonRdmDisks(final Boolean npivOnNonRdmDisks) {
        this.npivOnNonRdmDisks = npivOnNonRdmDisks;
    }

    public void setNpivPortWorldWideName(final List<Long> npivPortWorldWideName) {
        this.npivPortWorldWideName = npivPortWorldWideName;
    }

    public void setNpivTemporaryDisabled(final Boolean npivTemporaryDisabled) {
        this.npivTemporaryDisabled = npivTemporaryDisabled;
    }

    public void setNpivWorldWideNameOp(final String npivWorldWideNameOp) {
        this.npivWorldWideNameOp = npivWorldWideNameOp;
    }

    public void setNpivWorldWideNameType(final String npivWorldWideNameType) {
        this.npivWorldWideNameType = npivWorldWideNameType;
    }

    public void setNumCoresPerSocket(final Integer numCoresPerSocket) {
        this.numCoresPerSocket = numCoresPerSocket;
    }

    public void setNumCPUs(final Integer numCPUs) {
        this.numCPUs = numCPUs;
    }

    public void setPowerOpInfo(final VirtualMachineDefaultPowerOpInfo powerOpInfo) {
        this.powerOpInfo = powerOpInfo;
    }

    public void setRepConfig(final ReplicationConfigSpec repConfig) {
        this.repConfig = repConfig;
    }

    public void setScheduledHardwareUpgradeInfo(final ScheduledHardwareUpgradeInfo scheduledHardwareUpgradeInfo) {
        this.scheduledHardwareUpgradeInfo = scheduledHardwareUpgradeInfo;
    }

    public void setSwapPlacement(final String swapPlacement) {
        this.swapPlacement = swapPlacement;
    }

    public void setTools(final ToolsConfigInfo tools) {
        this.tools = tools;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setvAppConfig(final VmConfigSpec vAppConfig) {
        this.vAppConfig = vAppConfig;
    }

    public void setvAppConfigRemoved(final Boolean vAppConfigRemoved) {
        this.vAppConfigRemoved = vAppConfigRemoved;
    }

    public void setvAssertsEnabled(final Boolean vAssertsEnabled) {
        this.vAssertsEnabled = vAssertsEnabled;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setVirtualICH7MPresent(final Boolean virtualICH7MPresent) {
        this.virtualICH7MPresent = virtualICH7MPresent;
    }

    public void setVirtualSMCPresent(final Boolean virtualSMCPresent) {
        this.virtualSMCPresent = virtualSMCPresent;
    }

    public void setVmProfile(final List<VirtualMachineProfileSpec> vmProfile) {
        this.vmProfile = vmProfile;
    }

    public void setVpmcEnabled(final Boolean vpmcEnabled) {
        this.vpmcEnabled = vpmcEnabled;
    }

    public VirtualMachineConfigSpec toVirtualMachineConfigSpec(final boolean recovery) throws ProfileException {

        final VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        try {
            configSpec.setName(this.name);
            configSpec.setVersion(this.version);
            configSpec.setBootOptions(this.bootOptions);
            configSpec.setGuestId(this.guestId);
            configSpec.setAlternateGuestName(this.alternateGuestName);
            configSpec.setAnnotation(this.annotation);
            configSpec.setTools(this.tools);
            configSpec.setFlags(this.flags);
            configSpec.setPowerOpInfo(this.powerOpInfo);
            configSpec.setNumCPUs(this.numCPUs);
            configSpec.setNumCoresPerSocket(this.numCoresPerSocket);
            configSpec.setMemoryMB(this.memoryMB);
            configSpec.setMemoryHotAddEnabled(this.memoryHotAddEnabled);
            configSpec.setCpuHotAddEnabled(this.cpuHotAddEnabled);
            configSpec.setCpuHotRemoveEnabled(this.cpuHotRemoveEnabled);
            configSpec.setVirtualICH7MPresent(this.virtualICH7MPresent);
            configSpec.setVirtualSMCPresent(this.virtualSMCPresent);
            configSpec.setConsolePreferences(this.consolePreferences);
            if (recovery) {
                configSpec.setUuid(this.uuid);
                configSpec.setInstanceUuid(this.instanceUuid);
                configSpec.setLocationId(this.locationId);
                configSpec.setFiles(this.files);

                configSpec.getNpivNodeWorldWideName().addAll(this.npivNodeWorldWideName);
                configSpec.getNpivPortWorldWideName().addAll(this.npivPortWorldWideName);
                configSpec.setNpivDesiredNodeWwns(getNpivDesiredNodeWwns());
                configSpec.setNpivDesiredPortWwns(getNpivDesiredPortWwns());
            } else {
                configSpec.setFiles(new VirtualMachineFileInfo());
            }
            configSpec.setCpuAllocation(this.cpuAllocation);
            configSpec.setMemoryAllocation(this.memoryAllocation);
            configSpec.setLatencySensitivity(this.latencySensitivity);
            configSpec.setCpuAffinity(this.cpuAffinity);
            configSpec.setMemoryAffinity(this.memoryAffinity);
            configSpec.setNetworkShaper(this.networkShaper);

            configSpec.getExtraConfig().addAll(this.extraConfig);
            configSpec.setChangeTrackingEnabled(this.changeTrackingEnabled);
            configSpec.setFirmware(this.firmware);
            configSpec.setMaxMksConnections(this.maxMksConnections);
            configSpec.setGuestAutoLockEnabled(this.guestAutoLockEnabled);

            configSpec.setMemoryReservationLockedToMax(this.memoryReservationLockedToMax);
            configSpec.setNestedHVEnabled(this.nestedHVEnabled);
            configSpec.setVPMCEnabled(this.vpmcEnabled);
            configSpec.setMessageBusTunnelEnabled(this.messageBusTunnelEnabled);
            configSpec.setMigrateEncryption(this.migrateEncryption);

            for (final VirtualDeviceConfigSpecOverlay vdConfigSpec : this.deviceChange) {
                final VirtualDeviceConfigSpec vdcSpec = vdConfigSpec.toVirtualDeviceConfigSpec();
                configSpec.getDeviceChange().add(vdcSpec);
            }
        } catch (final Exception e) {
            throw new ProfileException(e);
        }
        return configSpec;
    }

}
