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
package com.vmware.safekeeping.core.type.fco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.location.CoreVappLocation;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.ToolsUnavailableFaultMsg;
import com.vmware.vim25.VAppCloneSpec;
import com.vmware.vim25.VAppConfigFaultFaultMsg;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class VirtualAppManager implements IFirstClassObject, IManagedEntityInfoPath {
    /**
     * 
     */
    private static final long serialVersionUID = -5089852573147305846L;

    private static final Logger logger = Logger.getLogger(VirtualAppManager.class.getName());

    private VimConnection vimConnection;

    private ManagedObjectReference moref;
    private String name;

    private SerializableVAppConfigInfo vAppConfig;

    private final List<VirtualMachineManager> vmList = new ArrayList<>();

    private ResourceConfigSpec config;
    private ManagedFcoEntityInfo temporaryFcoInfo;

    private ManagedObjectReference parentFolder;

    private ManagedObjectReference parentVApp;

    private ManagedEntityInfo dataCenterInfo;

    private ManagedObjectReference parent;

    /**
     * @param connetionManager
     * @param fcoInfo
     */
    public VirtualAppManager(final ConnectionManager connetionManager, final ManagedFcoEntityInfo fcoInfo) {
        this.vimConnection = connetionManager.getVimConnection(fcoInfo.getServerUuid());
        this.temporaryFcoInfo = fcoInfo;
    }

    /**
     * @param vmInfo
     */
    public VirtualAppManager(final ManagedFcoEntityInfo fcoInfo) {
        this.temporaryFcoInfo = fcoInfo;
    }

    /**
     * @param vimConnection2
     * @param fcoInfo
     */
    public VirtualAppManager(final VimConnection vimConnection, final ManagedFcoEntityInfo fcoInfo) {
        this.vimConnection = vimConnection;
        this.temporaryFcoInfo = fcoInfo;
    }

    /**
     * @param basicVimConnection
     * @param moref
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public VirtualAppManager(final VimConnection vimConnection, final ManagedObjectReference moref) {
        this.vimConnection = vimConnection;
        this.moref = moref;
        try {
            final Map<String, Object> entities = vimConnection.getVimHelper().entityProps(moref,
                    new String[] { "name", "vAppConfig", "vm", "config", "parentFolder", "parentVApp", "parent" });
            this.name = entities.get("name").toString();
            this.vAppConfig = new SerializableVAppConfigInfo((VAppConfigInfo) entities.get("vAppConfig"));
            final ArrayOfManagedObjectReference managedObjectReferences = (ArrayOfManagedObjectReference) entities
                    .get("vm");
            this.config = (ResourceConfigSpec) entities.get("config");
            this.parentFolder = (ManagedObjectReference) entities.get("parentFolder");
            this.parentVApp = (ManagedObjectReference) entities.get("parentVApp");
            this.parent = (ManagedObjectReference) entities.get("parent");

            for (final ManagedObjectReference vmMor : managedObjectReferences.getManagedObjectReference()) {
                final VirtualMachineManager vm = new VirtualMachineManager(vimConnection, vmMor);
                this.vmList.add(vm);
            }
            this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.moref);
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();

        }
    }

    /**
     * @param conn
     * @param name2
     */
    public VirtualAppManager(final VimConnection vimConnection, final String name) {
        this.vimConnection = vimConnection;
        this.name = name;
    }

    public VirtualAppManager(final VimConnection vimConnection, final String name, final ManagedObjectReference moref,
            final VAppConfigInfo vAppConfig)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        this.vimConnection = vimConnection;
        this.moref = moref;
        this.temporaryFcoInfo = null;
        this.vAppConfig = new SerializableVAppConfigInfo(vAppConfig);
        this.name = name;
        final Map<String, Object> entities = vimConnection.getVimHelper().entityProps(moref,
                new String[] { "vm", "config", "parentFolder", "parentVApp", "parent" });

        final ArrayOfManagedObjectReference managedObjectReferences = (ArrayOfManagedObjectReference) entities
                .get("vm");
        this.config = (ResourceConfigSpec) entities.get("config");
        this.parentFolder = (ManagedObjectReference) entities.get("parentFolder");
        this.parentVApp = (ManagedObjectReference) entities.get("parentVApp");
        this.parent = (ManagedObjectReference) entities.get("parent");
        for (final ManagedObjectReference vmMor : managedObjectReferences.getManagedObjectReference()) {
            final VirtualMachineManager vm = new VirtualMachineManager(vimConnection, vmMor);
            this.vmList.add(vm);
        }
        this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.moref);
    }

    /**
     * Clone the VM
     *
     * @param cloneName
     * @param datastore
     * @return
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws MigrationFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     * @throws FileFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimTaskException
     * @throws VimObjectNotExistException
     * @throws Exception
     */
    public boolean clone(final String cloneName, final String datastore, final RunningReport cloneReport)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, FileFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, MigrationFaultFaultMsg,
            TaskInProgressFaultMsg, VmConfigFaultFaultMsg, VimTaskException, InvalidCollectorVersionFaultMsg,
            VimObjectNotExistException {
        boolean result = false;
        final VAppCloneSpec cloneSpec = new VAppCloneSpec();
        if (StringUtils.isNotEmpty(datastore)) {
            cloneSpec.setLocation(this.vimConnection.getDatastoreByName(datastore).getMoref());
        } else {
            if (!getVmList().isEmpty()) {
                cloneSpec.setLocation(getVmList().get(0).getDatastoreInfo().getMoref());
            }
        }
        cloneSpec.setVmFolder(this.parentFolder);
        if (cloneSpec.getLocation() != null) {
            final ManagedObjectReference resPoolParentMoref = (ManagedObjectReference) this.vimConnection.getVimHelper()
                    .entityProps(getResourcePoolInfo().getMoref(), "parent");

            final ManagedObjectReference cloneTask = this.vimConnection.getVimPort().cloneVAppTask(getMoref(),
                    cloneName, resPoolParentMoref, cloneSpec);
            if (this.vimConnection.waitForTask(cloneTask, cloneReport)) {
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Successfully cloned vApp [%s] to clone name [%s]", getName(),
                            cloneName);
                    logger.info(msg);
                }
                result = true;
            } else {
                final String msg = String.format("Failure Cloning vApp [%s] to clone name [%s] ", getName(), cloneName);
                logger.warning(msg);
            }

        }
        return result;
    }

    /**
     *
     * @return
     * @throws FileFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimFaultFaultMsg
     * @throws VimTaskException
     * @throws InterruptedException
     */
    @Override
    public boolean destroy() throws RuntimeFaultFaultMsg, VimFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().destroyTask(this.moref);

        return this.vimConnection.waitForTask(taskMor);
    }

    public ResourceConfigSpec getConfig() {
        return this.config;
    }

    @Override
    public ManagedEntityInfo getDatacenterInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        if (this.dataCenterInfo == null) {
            this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(getMoref());
        }
        return this.dataCenterInfo;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VirtualApp;
    }

    @Override
    public ManagedFcoEntityInfo getFcoInfo() {
        if (this.temporaryFcoInfo != null) {
            return this.temporaryFcoInfo;
        }
        return new ManagedFcoEntityInfo(getName(), getMoref(), getUuid(), this.vimConnection.getServerIntanceUuid());
    }

    @Override
    public List<Block> getFullDiskAreas(final int diskId) {
        return Collections.emptyList();
    }

    /**
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InterruptedException
     */
    public CoreVappLocation getLocations() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final CoreVappLocation result = new CoreVappLocation();
        result.setDatacenterInfo(getDatacenterInfo());
        result.setResourcePoolInfo(getResourcePoolInfo());
        result.setFolderInfo(getVmFolderInfo());

        result.setResourcePoolPath(getManagedEntityInfoPath(result.getResourcePoolInfo()));
        result.setVmFolderPath(getManagedEntityInfoPath(result.getFolderInfo()));

        result.setResourcePoolFullPath(ManagedEntityInfo.composeEntityInfoName(result.getResourcePoolPath()));
        result.setVmFolderFullPath(ManagedEntityInfo.composeEntityInfoName(result.getVmFolderPath()));
        result.setvAppMember(this.parentVApp != null);
        return result;

    }

    @Override
    public ManagedObjectReference getMoref() {
        if (this.moref == null) {
            return this.temporaryFcoInfo.getMoref();
        }
        return this.moref;
    }

    /**
     * @return
     */
    public String getMorefValue() {
        return this.moref.getValue();
    }

    @Override
    public String getName() {
        if (this.moref == null) {
            return this.temporaryFcoInfo.getName();
        }
        return this.name;
    }

    /**
     * @return the parent
     */
    public ManagedObjectReference getParent() {
        return this.parent;
    }

    @Override
    public ManagedFcoEntityInfo getParentFco()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        ManagedFcoEntityInfo result = null;
        if (this.parentVApp != null) {
            final Map<String, Object> res2 = this.vimConnection.getVimHelper().entityProps(this.parentVApp,
                    new String[] { "name", "vAppConfig" });
            final String parentName = res2.get("name").toString();
            final VAppConfigInfo confInfo = (VAppConfigInfo) res2.get("vAppConfig");
            result = new ManagedFcoEntityInfo(parentName, this.parentVApp, confInfo.getInstanceUuid(),
                    this.vimConnection.getServerIntanceUuid());
        }
        return result;

    }

    /**
     * @return the parentFolder
     */
    @Override
    public ManagedObjectReference getParentFolder() {
        return this.parentFolder;
    }

    public ManagedObjectReference getParentVApp() {
        return this.parentVApp;
    }

    @Override
    public FcoPowerState getPowerState() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        FcoPowerState result = FcoPowerState.poweredOff;
        for (final VirtualMachineManager vmm : this.vmList) {
            final VirtualMachineRuntimeInfo runtimeInfo = vmm.getRuntimeInfo();
            switch (runtimeInfo.getPowerState()) {
            case POWERED_OFF:
                break;
            case POWERED_ON:
                if ((result == FcoPowerState.poweredOff) || (result == FcoPowerState.suspended)) {
                    result = FcoPowerState.poweredOn;
                }
                break;
            case SUSPENDED:
                if (result != FcoPowerState.poweredOn) {
                    result = FcoPowerState.suspended;
                }
                break;
            default:
                break;

            }

        }
        return result;
    }

    @Override
    public ManagedEntityInfo getResourcePoolInfo() {
        return new ManagedEntityInfo(getName(), getMoref(), this.vimConnection.getServerIntanceUuid());
    }

    @Override
    public String getServerUuid() {
        return this.vimConnection.getServerIntanceUuid();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getType()
     */
    @Override
    public FirstClassObjectType getType() {
        return FirstClassObjectType.vapp;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#getUuid()
     */
    @Override
    public String getUuid() {
        if (this.moref == null) {
            return this.temporaryFcoInfo.getUuid();
        }
        return this.vAppConfig.getInstanceUuid();
    }

    public SerializableVAppConfigInfo getVAppConfig() {
        return this.vAppConfig;
    }

    public VAppConfigSpec getVAppConfigSpec() {
        return this.vAppConfig.toVAppConfigSpec();
    }

    public String getVcenterInstanceUuid() {
        return (this.moref == null) ? this.temporaryFcoInfo.getServerUuid() : this.vimConnection.getServerIntanceUuid();
    }

    @Override
    public VimConnection getVimConnection() {
        return this.vimConnection;
    }

    @Override
    public ManagedEntityInfo getVmFolderInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final ManagedObjectReference mor = getParentFolder();
        final String entityName = getVimConnection().getVimHelper().entityName(mor);
        return new ManagedEntityInfo(entityName, mor, getVimConnection().getServerIntanceUuid());
    }

    public List<VirtualMachineManager> getVmList() {
        return this.vmList;
    }

    @Override
    public String headertoString() {
        return String.format("%-8s%-36s\t%-8s\t%-30s", "entity", "uuid", "moref", "name");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#isChangedBlockTrackingEnabled()
     */
    @Override
    public boolean isChangedBlockTrackingEnabled()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        boolean result = true;
        for (final VirtualMachineManager vm : this.vmList) {
            result &= vm.isChangedBlockTrackingEnabled();
        }
        return result;
    }

    @Override
    public boolean isEncrypted() {
        boolean result = false;
        for (final VirtualMachineManager vm : this.vmList) {
            result |= vm.isEncrypted();
        }
        return result;
    }

    /**
     * @return
     */
    public boolean isvAppConfigAvailable() {
        return this.vAppConfig != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.type.FirstClassObject#isVmDatastoreNfs()
     */
    @Override
    public boolean isVmDatastoreNfs() {
        return false;
    }

    /**
     * Stops this vApp. The virtual machines (or child vApps) will be stopped in the
     * order specified in the vApp configuration, if force is false. If force is set
     * to true, all virtual machines are powered-off (in no specific order and
     * possibly in parallel) regardless of the vApp auto-start configuration.
     *
     * @param force
     * @return
     * @throws VAppConfigFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public boolean powerOff(final boolean force)
            throws InvalidStateFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VAppConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOffVAppTask(getMoref(), force);
        return this.vimConnection.waitForTask(taskMor);

    }

    /**
     * Starts this vApp. The virtual machines (or sub vApps) will be started in the
     * order specified in the vApp configuration. If the vApp is suspended (@see
     * vim.VirtualApp.Summary.suspended), all suspended virtual machines will be
     * powered-on based on the defined start-up order. While a vApp is starting, all
     * power operations performed on sub entities are disabled through the VIM API.
     * They will throw TaskInProgress. In case of a failure to power-on a virtual
     * machine, the exception from the virtual machine power on is returned, and the
     * power-on sequence will be terminated. In case of a failure, virtual machines
     * that are already started will remain powered-on.
     *
     * @return
     * @throws VmConfigFaultFaultMsg
     * @throws VAppConfigFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     * @throws FileFaultFaultMsg
     * @throws VimTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InterruptedException
     */
    public boolean powerOn() throws FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VAppConfigFaultFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {

        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOnVAppTask(getMoref());
        return this.vimConnection.waitForTask(taskMor);
    }

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final GenerationProfile profile, final Integer diskId,
            final BackupMode backupMode) {
        return new ArrayList<>();
    }

    public boolean rebootGuest() {
        boolean result = true;
        for (final VirtualMachineManager vmm : getVmList()) {
            try {
                this.vimConnection.getVimPort().rebootGuest(vmm.getMoref());
            } catch (final ToolsUnavailableFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg
                    | TaskInProgressFaultMsg e) {
                Utility.logWarning(logger, e);
                result = false;
            }
        }
        return result;
    }

    /**
     * @return
     */
    public boolean reset() {
        boolean result = true;
        try {
            final List<ManagedObjectReference> taskMorList = new ArrayList<>();
            for (final VirtualMachineManager vmm : getVmList()) {
                taskMorList.add(this.vimConnection.getVimPort().resetVMTask(vmm.getMoref()));
            }

            for (final ManagedObjectReference taskMor : taskMorList) {
                result &= this.vimConnection.waitForTask(taskMor);
            }

        } catch (final TaskInProgressFaultMsg | InvalidPropertyFaultMsg | RuntimeFaultFaultMsg
                | InvalidCollectorVersionFaultMsg | VimTaskException | InvalidStateFaultMsg e) {
            Utility.logWarning(logger, e);
            result = false;
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();

        }
        return result;
    }

    @Override
    public boolean setChangeBlockTracking(final boolean enable)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException, ConcurrentAccessFaultMsg,
            DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg {
        boolean result = true;
        for (final VirtualMachineManager vm : this.vmList) {
            result &= vm.setChangeBlockTracking(enable);
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("%-8s%36s\t%-8s\t%-30s ", getEntityType().toString(true), getUuid(), getMoref().getValue(),
                getShortedEntityName());
    }

    /**
     * @param moref
     * @return
     */
    public boolean update(final ManagedObjectReference moref) {
        boolean result = true;
        try {
            this.moref = moref;
            this.temporaryFcoInfo = null;
            this.vmList.clear();
            final Map<String, Object> entities = this.vimConnection.getVimHelper().entityProps(moref,
                    new String[] { "name", "vAppConfig", "vm", "config", "parentFolder", "parentVApp" });
            this.name = entities.get("name").toString();

            this.vAppConfig = new SerializableVAppConfigInfo((VAppConfigInfo) entities.get("vAppConfig"));

            final ArrayOfManagedObjectReference managedObjectReferences = (ArrayOfManagedObjectReference) entities
                    .get("vm");
            this.config = (ResourceConfigSpec) entities.get("config");
            this.parentFolder = (ManagedObjectReference) entities.get("parentFolder");
            this.parentVApp = (ManagedObjectReference) entities.get("parentVApp");
            for (final ManagedObjectReference vmMor : managedObjectReferences.getManagedObjectReference()) {
                final VirtualMachineManager vm = new VirtualMachineManager(this.vimConnection, vmMor);
                this.vmList.add(vm);
            }
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
            result = false;
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();

        }

        return result;
    }

    public boolean updateVAppConfig(final SerializableVAppConfigInfo myVAppConfigInfo)
            throws ConcurrentAccessFaultMsg, DuplicateNameFaultMsg, FileFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg {
        final boolean result = true;
        this.vimConnection.getVimPort().updateVAppConfig(getMoref(), myVAppConfigInfo.toVAppConfigSpec());

        return result;

    }
}
