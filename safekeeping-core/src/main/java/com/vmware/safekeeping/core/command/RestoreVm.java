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
package com.vmware.safekeeping.core.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.JVixException;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmProfileId;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGenerateRestoreManagedInfo;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.command.results.CoreResultActionPower;
import com.vmware.safekeeping.core.command.results.CoreResultActionReconfigureVm;
import com.vmware.safekeeping.core.command.results.CoreResultActionRestoreMetadata;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.control.info.CoreRestoreVmdkManagedInfo;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoNetwork;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.SearchManagementEntity;
import com.vmware.safekeeping.core.type.StorageDirectoryInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.PowerOperation;
import com.vmware.safekeeping.core.type.enums.SearchManagementEntityInfoType;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;
import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.CannotCreateFileFaultMsg;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DVPortgroupConfigInfo;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.FileNotFoundFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidDatastorePathFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.OpaqueNetworkSummary;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualE1000E;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualEthernetCardOpaqueNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSriovEthernetCard;
import com.vmware.vim25.VirtualVmxnet;
import com.vmware.vim25.VmConfigFaultFaultMsg;

class RestoreVm extends AbstractRestoreFcoWithDisk {

    RestoreVm(final VimConnection vimConnection, final CoreRestoreOptions options, final Logger log) {
        super(vimConnection, options, log);

    }

    private void checkOptions(final CoreRestoreManagedInfo managedInfo, final GenerationProfile profile)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException,
            InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
        if (!getOptions().isParentAVApp()) {
            if (getOptions().getFolder() == null) {
                String vmFolder = profile.getFolderPath();
                if (StringUtils.isNotEmpty(vmFolder)) {
                    if (StringUtils.isNotEmpty(getOptions().getVmFolderFilter())
                            && !vmFolder.contains(getOptions().getVmFolderFilter())) {
                        final String msg = String.format(
                                "Original  vmFolder(%s) cannot be used because is not subfolder of vmFilter(%s). vmFilter will be used instead ",
                                vmFolder, getOptions().getVmFolderFilter());
                        this.logger.warning(msg);
                        vmFolder = getOptions().getVmFolderFilter();
                    }

                    ManagedObjectReference folder = getVimConnection().getFind().findByInventoryPath(vmFolder);

                    if (folder == null) {
                        final String msg = String.format("vmFolder(%s) doesn't exist vmFilter(%s) will be used instead",
                                vmFolder, getOptions().getVmFolderFilter());
                        this.logger.warning(msg);

                        vmFolder = getOptions().getVmFolderFilter();
                        if (StringUtils.isNotEmpty(vmFolder)) {
                            folder = getVimConnection().getFind().findByInventoryPath(vmFolder);
                        }
                    }
                    if (folder != null) {
                        managedInfo
                                .setFolderInfo(new ManagedEntityInfo(vmFolder.substring(vmFolder.lastIndexOf('/') + 1),
                                        folder, getVimConnection().getServerIntanceUuid()));
                    }
                }
            }
            if (getOptions().getResourcePool() == null) {
                String originalResourcePoolName = profile.getResourcePoolPath();
                if (StringUtils.isNotEmpty(originalResourcePoolName)) {
                    if (StringUtils.isNotEmpty(getOptions().getResPoolFilter())
                            && !originalResourcePoolName.contains(getOptions().getResPoolFilter())) {
                        originalResourcePoolName = getOptions().getResPoolFilter();
                        final String msg = String.format(
                                "Original ResourcePool(%s) cannot be used because is not subfolder of rpFilter(%s). rpFilter will be used instead ",
                                originalResourcePoolName, getOptions().getResPoolFilter());
                        this.logger.warning(msg);
                    }
                    ManagedObjectReference rp = getVimConnection().getFind()
                            .findByInventoryPath(originalResourcePoolName);
                    if (rp == null) {
                        final String msg = String.format(
                                "ResourcePool(%s) doesn't exist rpFilter(%s) will be used instead",
                                originalResourcePoolName, getOptions().getResPoolFilter());
                        this.logger.warning(msg);
                        originalResourcePoolName = getOptions().getResPoolFilter();
                        if (StringUtils.isNotEmpty(originalResourcePoolName)) {
                            rp = getVimConnection().getFind().findByInventoryPath(originalResourcePoolName);
                        }
                    }
                    if (rp != null) {
                        managedInfo.setResourcePollInfo(new ManagedEntityInfo(
                                originalResourcePoolName.substring(originalResourcePoolName.lastIndexOf('/') + 1), rp,
                                getVimConnection().getServerIntanceUuid()));
                    }
                } else {
                    originalResourcePoolName = CoreGlobalSettings.getRpFilter();
                    final ManagedObjectReference rp = getVimConnection().getFind()
                            .findByInventoryPath(originalResourcePoolName);
                    if (rp != null) {
                        managedInfo.setResourcePollInfo(new ManagedEntityInfo(
                                originalResourcePoolName.substring(originalResourcePoolName.lastIndexOf('/') + 1), rp,
                                getVimConnection().getServerIntanceUuid()));
                    }
                }
            }
            if (getOptions().getDatacenter() == null) {

                String originalDatacenterName = profile.getDatacenterInfo().getName();
                if (StringUtils.isNotEmpty(getOptions().getVmFolderFilter())
                        && (!getOptions().getVmFolderFilter().startsWith(originalDatacenterName))) {
                    originalDatacenterName = getOptions().getVmFolderFilter().substring(0,
                            getOptions().getVmFolderFilter().indexOf('/'));
                    final String msg = String.format(
                            "Original Datacenter %s doesn't match wih vmFilter(%s). %s will be used instead",
                            originalDatacenterName, CoreGlobalSettings.getVmFilter(), originalDatacenterName);
                    this.logger.warning(msg);
                }

                final ManagedObjectReference dc = getVimConnection().getFind()
                        .findByInventoryPath(originalDatacenterName);
                if (dc != null) {
                    managedInfo.setDcInfo(new ManagedEntityInfo(originalDatacenterName, dc,
                            getVimConnection().getServerIntanceUuid()));
                }

            }
        }
        if (getOptions().getDatastore() == null) {
            final String originalDatastore = profile.getDatastoreInfo().getName();
            if ((originalDatastore != null) && !originalDatastore.isEmpty()) {

                managedInfo.setDatastoreInfo(
                        getVimConnection().getManagedEntityInfo(EntityType.Datastore, originalDatastore));

                if (managedInfo.getDatastoreInfo() == null) {
                    final String msg = String.format("Original datastore %s doesn't exist", originalDatastore);
                    this.logger.warning(msg);
                }
            }
        }
        if (getOptions().getStorageProfile() == null) {
            managedInfo.setSpbmProfile(
                    getVimConnection().getPbmConnection().getDefinedProfileSpec(profile.getPbmProfileName()));
        }

    }

    private boolean createSnapshot(final CoreResultActionVmRestore ravr) throws CoreResultActionException {
        boolean snapEnable = CoreGlobalSettings.isForceSnapBeforeRestore();
        if ((getOptions().getRequestedTransportModes() != null)
                && getOptions().getRequestedTransportModes().contains("san")) {
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.info("SAN transport mode selected. Enable restore with snapshot ");
            }
            snapEnable = true;
        }
        final VirtualMachineManager vmm = ravr.getFirstClassObject();
        final CoreResultActionCreateSnap resultAction = new CoreResultActionCreateSnap(ravr);
        ravr.setCreateSnapshotAction(resultAction);
        if (snapEnable) {
            /**
             * Start Section CreateSnapshot
             */
            ravr.getInteractive().startCreateSnapshot();
            try {
                resultAction.start();
                final String snapName = Utility.generateSnapshotName(Calendar.getInstance());
                vmm.createSnapshot(snapName);
                final SnapshotManager snap = vmm.getCurrentSnapshot();
                if (snap == null) {
                    resultAction.failure();
                } else {
                    resultAction.setSnapshotManager(snap);
                    resultAction.setSnapMoref(snap.getMoref().getValue());
                    resultAction.setSnapName(snap.getName());
                    resultAction.setSnapDescription(StringUtils.EMPTY);
                    resultAction.success();
                    /**
                     * End Section CreateSnapshot
                     */
                    ravr.getInteractive().endCreateSnapshot();

                }
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                resultAction.failure(e);
                Thread.currentThread().interrupt();
            } catch (FileFaultFaultMsg | InvalidNameFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg
                    | SnapshotFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg
                    | InvalidCollectorVersionFaultMsg | VimTaskException e) {
                Utility.logWarning(this.logger, e);
                resultAction.failure(e);
            } finally {
                resultAction.done();
            }
        } else {
            if (this.logger.isLoggable(Level.INFO)) {
                final String msg = "Snaphot not required";
                this.logger.info(msg);
                resultAction.skip(msg);
            }
        }
        return resultAction.isSuccessfulOrSkipped();
    }

    /**
     * Delete a failed VM
     *
     * @param ravr
     * @return
     * @throws CoreResultActionException
     */
    private boolean destroy(final CoreResultActionVmRestore ravr) throws CoreResultActionException {
        /**
         * Start Section DestroyVm
         */
        ravr.getInteractive().startDestroy();
        final CoreResultActionDestroy rar = new CoreResultActionDestroy(ravr);
        rar.start();
        boolean result = false;
        this.logger.warning("Restored failed delete the VM and folder. ");
        try {
            ravr.getFirstClassObject().destroy();
            result = true;

        } catch (final InterruptedException e) {
            rar.failure(e);
            this.logger.log(Level.WARNING, "Interrupted ", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();

        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                | VimFaultFaultMsg e) {
            Utility.logWarning(this.logger, e);
            rar.failure(e);
        } finally {
            rar.done();
            /**
             * End Section DestroyVm
             */
            ravr.getInteractive().endDestroy();
        }
        return result;
    }

    /**
     * Map the requested disk backend to the destination backend
     *
     * @param managedInfo
     * @param profile
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InterruptedException
     * @throws VimObjectNotExistException
     * @throws com.vmware.pbm.RuntimeFaultFaultMsg
     * @throws InvalidArgumentFaultMsg
     * @throws SafekeepingUnsupportedObjectException
     */
    private boolean disksMapping(final CoreRestoreManagedInfo managedInfo, final GenerationProfile profile)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException,
            InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, SafekeepingUnsupportedObjectException {
        boolean result = false;

        for (int i = 0; i < profile.getNumberOfDisks(); i++) {

            ManagedEntityInfo vmdkDatastore = null;
            VirtualMachineDefinedProfileSpec spbmProfile = null;
            if (i < getOptions().getDisks().size()) {
                final SearchManagementEntity item = getOptions().getDisks().get(i).getDatastore();
                if (item != null) {
                    vmdkDatastore = getVimConnection().getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
                            EntityType.Datastore, item);
                    if (vmdkDatastore == null) {
                        throw new VimObjectNotExistException(item.getSearchValue(), EntityType.Datastore);
                    }
                } else {
                    vmdkDatastore = managedInfo.getDatastoreInfo();
                }

                final SearchManagementEntity itemProfile = getOptions().getDisks().get(i).getSpbmProfile();
                if (itemProfile != null) {
                    switch (itemProfile.getSearchType()) {
                    case NAME:
                        final String name = itemProfile.getSearchValue();
                        if (getVimConnection().getPbmConnection().doesPpmProfileExist(name)) {

                            spbmProfile = getVimConnection().getPbmConnection().getDefinedProfileSpec(name);
                        } else {
                            throw new VimObjectNotExistException("Profile %s doesn't exist ", name);
                        }
                        break;
                    case ID:
                        final PbmProfileId id = new PbmProfileId();
                        id.setUniqueId(itemProfile.getSearchValue());
                        if (getVimConnection().getPbmConnection().doesPpmProfileExist(id)) {
                            spbmProfile = getVimConnection().getPbmConnection().getDefinedProfileSpec(id);
                        } else {
                            throw new VimObjectNotExistException("Profile %s doesn't exist ", id.getUniqueId());
                        }
                        break;
                    case MOREF:
                        throw new SafekeepingUnsupportedObjectException("invalid search");

                    }
                } else {
                    spbmProfile = managedInfo.getSpbmProfile();
                }
            } else {
                if (getOptions().isRecover()) {
                    vmdkDatastore = profile.getDisks().get(i).getDatastoreInfo();
                    final DiskProfile diskProfile = profile.getDisks().get(i);

                    if ((diskProfile != null) && (diskProfile.getPbmProfile() != null)) {
                        spbmProfile = getVimConnection().getPbmConnection()
                                .getDefinedProfileSpec(diskProfile.getPbmProfile().getPbmProfileName());
                    } else {
                        throw new VimObjectNotExistException("Disk Profile error");
                    }
                } else {
                    vmdkDatastore = managedInfo.getDatastoreInfo();
                    spbmProfile = managedInfo.getSpbmProfile();
                }

            }
            final CoreRestoreVmdkManagedInfo vmdkManagedInfo;
            if (managedInfo.getVmdksManagedInfo().containsKey(i)) {
                vmdkManagedInfo = managedInfo.getVmdksManagedInfo().get(i);
            } else {
                vmdkManagedInfo = new CoreRestoreVmdkManagedInfo(i);
                managedInfo.getVmdksManagedInfo().put(i, vmdkManagedInfo);
            }
            vmdkManagedInfo.setDatastore(vmdkDatastore);
            vmdkManagedInfo.setSpbmProfile(spbmProfile);
            result = true;
        }
        return result;
    }

    private boolean disksRestore(final CoreResultActionVmRestore rar, final Jvddk jvddk)
            throws CoreResultActionException {
        boolean result = false;
        try {
            final GenerationProfile profile = rar.getProfile();
            if (this.logger.isLoggable(Level.INFO)) {
                final String msg = String.format("Restore %d disks.", profile.getNumberOfDisks());
                this.logger.info(msg);
            }
            /**
             * Start Section DisksRestore
             */
            rar.getInteractive().startDisksRestore();

            final List<Future<CoreResultActionDiskRestore>> futures = new ArrayList<>();
            for (final CoreResultActionDiskRestore radr : rar.getResultActionsOnDisk()) {

                final Future<CoreResultActionDiskRestore> f = submit(radr, jvddk, this.logger);
                futures.add(f);
            }
            // A) Await all runnable to be done (blocking)
            for (final Future<CoreResultActionDiskRestore> future : futures) {
                if (future != null) {
                    // get will block until the future is done
                    final CoreResultActionDiskRestore obj = future.get();
                    if ((obj != null) && this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine(obj.toString());
                    }
                } else {
                    final String msg = "Internal error - check server logs";
                    rar.failure(msg);
                }

            }
            result = true;

        } catch (final InterruptedException e) {
            rar.failure(e);
            this.logger.log(Level.WARNING, "Interrupted ", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            rar.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            rar.getInteractive().endDisksRestore();
            /**
             * End Section DiskRestore
             */
        }
        return result;
    }

    /**
     * Deal with any existing VM in conflict with the new restore VM
     *
     * @param ravr
     * @return
     * @throws CoreResultActionException
     */
    private boolean manageExistingFco(final CoreResultActionVmRestore ravr) throws CoreResultActionException {
        boolean result = false;
        String msg;
        final String name = ravr.getManagedInfo().getName();
        if (getOptions().isParentAVApp() && getOptions().isAllowDuplicatedVmNamesInsideVapp()) {
            result = true;
            if (this.logger.isLoggable(Level.INFO)) {
                msg = String.format("Virtual Machine name:%s is part of a Virtual Appliance", name);
                this.logger.info(msg);
            }
        } else {

            final ManagedObjectReference mor = getVimConnection().getVmByName(name);
            if (mor != null) {
                final CoreResultActionDestroy rar = new CoreResultActionDestroy(ravr);
                try {
                    rar.start();
                    final VirtualMachineManager vmm = new VirtualMachineManager(getVimConnection(), mor);
                    rar.setFcoEntityInfo(vmm.getFcoInfo());
                    if (getOptions().isOverwrite()) {

                        rar.setShutdownRequired(vmm.getPowerState() == FcoPowerState.poweredOn);
                        if (rar.isShutdownRequired()) {
                            if (getOptions().isForce()) {
                                vmm.powerOff();
                                vmm.destroy();
                            } else {
                                rar.failure("virtual machine is powered On");
                            }
                        } else {
                            vmm.destroy();
                        }
                    } else {
                        msg = String.format("Virtual Machine name:%s already exist", name);
                        this.logger.warning(msg);
                        rar.skip(msg);
                        ravr.skip(msg);
                    }
                } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg
                        | VimTaskException | VimFaultFaultMsg | InvalidStateFaultMsg | TaskInProgressFaultMsg e) {
                    msg = String.format("Virtual Machine name:%s cannot be removed ", name);
                    this.logger.log(Level.WARNING, msg, e);
                    rar.failure(msg);
                } catch (final InterruptedException e) {
                    this.logger.log(Level.WARNING, "Interrupted!", e);
                    rar.failure(e);
                    Thread.currentThread().interrupt();
                } finally {
                    rar.done();
                }
                result = rar.isSuccessful();
            } else {
                if (this.logger.isLoggable(Level.FINE)) {
                    msg = String.format("Virtual Machine name:%s doesn't exist", name);
                    this.logger.fine(msg);
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * Map the requested networks to the destination networks
     *
     * @param managedInfo
     * @param profile
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws VimObjectNotExistException
     */
    private void networksMapping(final CoreRestoreManagedInfo managedInfo, final GenerationProfile profile)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimObjectNotExistException {
        final Map<String, ManagedObjectReference> networksAvailable = getVimConnection()
                .getAvailableHostNetworks(managedInfo.getHostInfo().getMoref());
        for (int i = 0; i < profile.getNumberOfVnics(); i++) {
            if ((i < getOptions().getNetworks().size()) && (getOptions().getNetworks().get(i) != null)) {
                final SearchManagementEntity item = getOptions().getNetworks().get(i);
                ManagedObjectReference newNetwork = null;
                if (item.getSearchType() == SearchManagementEntityInfoType.NAME) {
                    newNetwork = networksAvailable.get(item.getSearchValue());
                    if (newNetwork == null) {
                        throw new VimObjectNotExistException(item.getSearchValue(), EntityType.Network);
                    }
                    managedInfo.getNetworkMapping()[i] = new ManagedEntityInfo(item.getSearchValue(), newNetwork,
                            getVimConnection().getServerIntanceUuid());
                } else {
                    String name = null;
                    try {
                        newNetwork = MorefUtil.newManagedObjectReference(EntityType.DistributedVirtualPortgroup,
                                item.getSearchValue());
                        name = getVimConnection().getVimHelper().entityName(newNetwork);
                    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                        Utility.logWarning(this.logger, e);
                    }
                    if (StringUtils.isEmpty(name)) {
                        try {
                            newNetwork = MorefUtil.newManagedObjectReference(EntityType.OpaqueNetwork,
                                    item.getSearchValue());
                            name = getVimConnection().getVimHelper().entityName(newNetwork);
                        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                        }
                    }
                    if (StringUtils.isEmpty(name)) {
                        try {
                            newNetwork = MorefUtil.newManagedObjectReference(EntityType.Network, item.getSearchValue());
                            name = getVimConnection().getVimHelper().entityName(newNetwork);
                        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                        }
                    }
                    if (StringUtils.isEmpty(name)) {
                        throw new VimObjectNotExistException(item.getSearchValue(), EntityType.Network);
                    }
                    managedInfo.getNetworkMapping()[i] = new ManagedEntityInfo(name, newNetwork,
                            getVimConnection().getServerIntanceUuid());
                }

            } else {
                for (final FcoNetwork net : profile.getNetworks().values()) {
                    if (net.getVmNics().contains(i)) {
                        final ManagedObjectReference newNetwork = networksAvailable.get(net.getName());
                        if (newNetwork == null) {
                            throw new VimObjectNotExistException(net.getName(), EntityType.Network);
                        }
                        managedInfo.getNetworkMapping()[i] = new ManagedEntityInfo(net.getName(), newNetwork,
                                getVimConnection().getServerIntanceUuid());
                        break;
                    }
                }
            }

        }
    }

    private boolean powerOn(final CoreResultActionVmRestore rar) throws CoreResultActionException {
        final VirtualMachineManager vmm = rar.getFirstClassObject();
        String msg = null;
        boolean result = false;
        if (rar.isRunning() && getOptions().isPowerOn()) {
            /**
             * Start Section PowerOn
             */
            rar.getInteractive().startVmPowerOn();
            final CoreResultActionPower resultAction = new CoreResultActionPower(rar);
            try {
                Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
                resultAction.start();
                resultAction.setRequestedPowerOperation(PowerOperation.powereOn);
                resultAction.setPowerState(vmm.getPowerState());
                if (vmm.powerOn(rar.getManagedInfo().getHostInfo()) && this.logger.isLoggable(Level.INFO)) {
                    msg = "PowerOn Vm " + vmm.getName() + " succeeded.";
                    this.logger.info(msg);
                }
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                Thread.currentThread().interrupt();
                resultAction.failure(e);
            } catch (FileFaultFaultMsg | InsufficientResourcesFaultFaultMsg | InvalidStateFaultMsg
                    | RuntimeFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg
                    | InvalidCollectorVersionFaultMsg | VimTaskException e) {
                Utility.logWarning(this.logger, e);
                resultAction.failure(e);
            } finally {
                resultAction.done();
                result = resultAction.isSuccessful();
            }
            rar.getInteractive().endVmPowerOn();
            /**
             * End Section PowerOn
             */

        } else {
            result = true;
        }
        return result;

    }

    private boolean reconfig(final GenerationProfile profile, final CoreResultActionVmRestore rar)
            throws CoreResultActionException {
        final VirtualMachineManager vmm = rar.getFirstClassObject();
        final CoreRestoreManagedInfo managedInfo = rar.getManagedInfo();
        final CoreResultActionReconfigureVm resultAction = new CoreResultActionReconfigureVm(rar);
        /**
         * Start Section Reconfiguration
         */
        rar.getInteractive().startReconfiguration();
        resultAction.start();
        final FcoArchiveManager vmArcMgr = profile.getFcoArchiveManager();
        try {
            if (vmm.reconfigureVm(vmArcMgr, profile, managedInfo)) {
                vmm.reload();
                final List<String> diskNameList = vmm.getConfig().getAllDiskNameList();
                for (final DiskProfile disk : profile.getDisks()) {
                    disk.setRemoteDiskPath(diskNameList.get(disk.getDiskId()));
                    new CoreResultActionDiskRestore(disk.getDiskId(), profile, rar);
                }
                rar.getInteractive().endReconfiguration();
                /**
                 * End Section Reconfiguration
                 */
            }
        } catch (final ConcurrentAccessFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg
                | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidNameFaultMsg
                | InvalidStateFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg
                | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            resultAction.failure(e);

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
        }
        return resultAction.isSuccessful();
    }

    private boolean reconfigDisk(final VirtualDeviceConfigSpec virtualDeviceSpec,
            final CoreRestoreVmdkManagedInfo managedVmdkInfo) {
        boolean result = false;
        if (virtualDeviceSpec.getDevice() instanceof VirtualDisk) {

            final VirtualDisk vdisk = (VirtualDisk) virtualDeviceSpec.getDevice();

            // workaround API issue
            vdisk.setCapacityInKB(PrettyNumber.toKiloByte(vdisk.getCapacityInBytes()));

            if (vdisk.getBacking() instanceof VirtualDiskFlatVer2BackingInfo) {
                final VirtualDiskFlatVer2BackingInfo backing = (VirtualDiskFlatVer2BackingInfo) vdisk.getBacking();
                backing.setFileName("[" + managedVmdkInfo.getDatastore().getName() + "]");
                backing.setDatastore(managedVmdkInfo.getDatastore().getMoref());
            }
            if (managedVmdkInfo.getSpbmProfile() != null) {
                virtualDeviceSpec.getProfile().add(managedVmdkInfo.getSpbmProfile());
            }
            result = true;
        }
        return result;
    }

    /**
     * Reconfigure the network backing
     *
     * @param device            Device representing the virtual network
     * @param networksAvailable Map containing Name and Moref of any available
     *                          networks
     * @param managedInfo
     * @param vmNicNumber
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     */
    private boolean reconfigNetwork(final VirtualDevice device, final CoreRestoreManagedInfo managedInfo,
            final int vmNicNumber) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        String msg;
        boolean result = false;
        if ((device instanceof VirtualE1000) || (device instanceof VirtualE1000E) || (device instanceof VirtualPCNet32)
                || (device instanceof VirtualVmxnet) || (device instanceof VirtualEthernetCard)) {
            // ********* IMPORTANT ****************
            // * External ID has to be reset for Opaque Network and CVDS ports
            // * No effects on VDS and standard Switch
            ((VirtualEthernetCard) device).setExternalId(null);
            // ******************************************
            if (managedInfo.getNetworkMapping().length <= vmNicNumber) {
                device.setBacking(null);
                if (device.getConnectable() != null) {
                    device.getConnectable().setConnected(false);
                }
                msg = String.format("No Network is available. Set vmnic%d backing port to Null", vmNicNumber);
                this.logger.warning(msg);
            } else {
                final ManagedEntityInfo newNetworkInfo = managedInfo.getNetworkMapping()[vmNicNumber];
                switch (newNetworkInfo.getEntityType()) {
                case Network:
                    final VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
                    nicBacking.setDeviceName(newNetworkInfo.getName());
                    device.setBacking(nicBacking);
                    break;
                case OpaqueNetwork:
                    final OpaqueNetworkSummary opaqueNetworkSummary = (OpaqueNetworkSummary) getVimConnection()
                            .getVimHelper().entityProps(newNetworkInfo, "summary");
                    final VirtualEthernetCardOpaqueNetworkBackingInfo opaqueBack = new VirtualEthernetCardOpaqueNetworkBackingInfo();
                    opaqueBack.setOpaqueNetworkId(opaqueNetworkSummary.getOpaqueNetworkId());
                    opaqueBack.setOpaqueNetworkType(opaqueNetworkSummary.getOpaqueNetworkType());
                    device.setBacking(opaqueBack);
                    break;
                case DistributedVirtualPortgroup:
                    final DVPortgroupConfigInfo dvPortgroupConfigInfo = (DVPortgroupConfigInfo) getVimConnection()
                            .getVimHelper().entityProps(newNetworkInfo, "config");
                    final String uuid = (String) getVimConnection().getVimHelper()
                            .entityProps(dvPortgroupConfigInfo.getDistributedVirtualSwitch(), "uuid");
                    final VirtualEthernetCardDistributedVirtualPortBackingInfo dvpBack = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
                    final DistributedVirtualSwitchPortConnection dvsPortConnection = new DistributedVirtualSwitchPortConnection();
                    dvsPortConnection.setPortgroupKey(dvPortgroupConfigInfo.getKey());
                    dvsPortConnection.setSwitchUuid(uuid);
                    dvpBack.setPort(dvsPortConnection);
                    device.setBacking(dvpBack);
                    break;
                default:
                    break;
                }

            }
            result = true;
        } else if (device instanceof VirtualSriovEthernetCard) {
            if (managedInfo.getNetworkMapping().length > vmNicNumber) {
                msg = String.format("vmnic%d is backing an SRIOV port cannot be remapped to %s", vmNicNumber,
                        managedInfo.getNetworkMapping()[vmNicNumber]);
                this.logger.warning(msg);
            }
            result = true;
        } else {
            if (this.logger.isLoggable(Level.FINE)) {
                msg = String.format("Unmanaged or not relevant device type:%s", device.getDeviceInfo());
                this.logger.fine(msg);
            }
        }
        return result;
    }

    /**
     * Remove snapshot
     *
     * @param rar CoreResultActionVmRestore object
     * @return true if succeed
     * @throws CoreResultActionException
     */
    private boolean removeSnapshot(final CoreResultActionVmRestore rar) throws CoreResultActionException {
        boolean result = false;
        if ((rar.getCreateSnapshotAction() != null) && rar.getCreateSnapshotAction().isSuccessful()) {
            /**
             * Start Section RemoveSnapshot
             */
            rar.getInteractive().startRemoveSnapshot();

            final VirtualMachineManager vmm = rar.getFirstClassObject();
            final CoreResultActionDeleteSnap resultAction = new CoreResultActionDeleteSnap(
                    rar.getCreateSnapshotAction());
            rar.setDeleteSnapshotAction(resultAction);
            rar.getSubOperations().add(resultAction);
            try {
                resultAction.start();
                final ManagedObjectReference snapMor = resultAction.getSnapshotManager().getMoref();
                String msg;
                if (vmm.revertToSnapshot(snapMor)) {
                    if (this.logger.isLoggable(Level.INFO)) {
                        msg = String.format("Revert snapshot %s succeeded.", resultAction.getSnapName());
                        this.logger.info(msg);
                    }
                } else {
                    msg = String.format("Revert snapshot %s failed.", resultAction.getSnapName());
                    this.logger.warning(msg);
                    resultAction.failure(msg);
                }

                if (vmm.deleteSnapshot(snapMor, false, true)) {
                    if (this.logger.isLoggable(Level.INFO)) {
                        msg = String.format("Delete snapshot %s succeeded.", resultAction.getSnapName());
                        this.logger.info(msg);
                    }
                } else {
                    msg = String.format("Delete snapshot %s failed.", resultAction.getSnapName());
                    this.logger.warning(msg);
                    resultAction.failure(msg);
                }
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                resultAction.failure(e);
                Thread.currentThread().interrupt();
            } catch (final FileFaultFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
                    | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
                    | InsufficientResourcesFaultFaultMsg | VimTaskException e) {
                Utility.logWarning(this.logger, e);
                resultAction.failure(e);
            } finally {
                resultAction.done();
                result = resultAction.isSuccessful();

                rar.getInteractive().endRemoveSnapshot();
                /**
                 * End Section RemoveSnapshot
                 */
            }
        } else {
            result = true;
        }
        return result;

    }

    /**
     * Main restore function
     *
     * @param vmArcMgr
     * @param rar
     * @return
     * @throws CoreResultActionException
     */
    public CoreResultActionVmRestore restore(final FcoArchiveManager vmArcMgr, final CoreResultActionVmRestore rar)
            throws CoreResultActionException {
        /**
         * Start Section RetrieveProfile
         */
        rar.getInteractive().startRetrieveProfile();
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("restoreVm() start.");
        }
        final CoreResultActionLoadProfile resultActionLoadProfile = actionLoadProfile(vmArcMgr, rar);
        if (resultActionLoadProfile.isSuccessful()) {
            final GenerationProfile profile = resultActionLoadProfile.getProfile();
            rar.setProfile(profile);
            rar.getInteractive().endRetrieveProfile();
            /**
             * End Section RetrieveProfile
             */
            try {
                if (restoreManagedInfo(profile, rar) && checkPrivileges(rar) && manageExistingFco(rar)
                        && !getOptions().isDryRun() && (restoreMetadata(rar))) {
                    if (getOptions().isNoVmdk()) {
                        if (this.logger.isLoggable(Level.INFO)) {
                            this.logger.info("Disks are not restored because specified --novmdk option.");
                        }
                    } else {
                        if (rar.isRunning()) {
                            final Jvddk jvddk = new Jvddk(this.logger, rar.getFirstClassObject());
                            if (createSnapshot(rar) && startVddkAccess(rar, jvddk) && disksRestore(rar, jvddk)
                                    && endVddkAccess(rar, jvddk)) {
                                final String st = rar.getFcoToString() + " Restore Success - start cleaning";
                                if (this.logger.isLoggable(Level.INFO)) {
                                    this.logger.info(st);
                                }
                            }
                        }
                    }
                }
            } catch (final JVixException | VimPermissionException e) {
                Utility.logWarning(this.logger, e);
                rar.failure(e);
            } catch (final Exception e) {
                logger.warning("----------------- Unexpected Error ---------------------");
                Utility.logWarning(this.logger, e);
                rar.failure();
            } finally {
                if (rar.isAbortedOrFailed() && rar.isOnErrorDestroyFco()) {
                    destroy(rar);
                } else {
                    if (removeSnapshot(rar)) {
                        powerOn(rar);
                    }
                }
            }

        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("restoreVm() end.");
        }
        return rar;

    }

    private boolean restoreByCreate(final CoreResultActionVmRestore rar) throws CoreResultActionException {

        final GenerationProfile profile = rar.getProfile();
        final CoreRestoreManagedInfo managedInfo = rar.getManagedInfo();
        final CoreResultActionRestoreMetadata resultAction = new CoreResultActionRestoreMetadata(rar);
        resultAction.start();
        final ITargetOperation target = profile.getTargetOperation();
        final String nvramContentPath = profile.getNvramContentPath();

        try {
            if (!target.doesObjectExist(nvramContentPath)) {
                resultAction.failure(nvramContentPath + " content doesn't exist");
            } else {
                final VirtualMachineConfigSpec vmConfigSpec = profile.getConfigSpec()
                        .toVirtualMachineConfigSpec(managedInfo.isRecovery());

                if (!managedInfo.isRecovery()) {
                    vmConfigSpec.setName(managedInfo.getName());
                    vmConfigSpec.getFiles().setVmPathName("[" + managedInfo.getDatastoreInfo().getName() + "]");

                    int networkIndex = 0;
                    int diskIndex = 0;
                    for (final VirtualDeviceConfigSpec virtualDeviceSpec : vmConfigSpec.getDeviceChange()) {
                        final VirtualDevice device = virtualDeviceSpec.getDevice();

                        if (device instanceof VirtualDisk) {
                            if (reconfigDisk(virtualDeviceSpec, managedInfo.getVmdksManagedInfo().get(diskIndex))) {
                                ++diskIndex;
                            }
                        } else if ((device instanceof VirtualE1000) || (device instanceof VirtualE1000E)
                                || (device instanceof VirtualPCNet32) || (device instanceof VirtualVmxnet)
                                || (device instanceof VirtualEthernetCard)) {
                            if (reconfigNetwork(device, managedInfo, networkIndex)) {
                                ++networkIndex;
                            }
                        } else {
                            if (this.logger.isLoggable(Level.FINE)) {
                                this.logger.fine("Skipping device " + device.getDeviceInfo());
                            }
                        }
                    }
                    if (managedInfo.getSpbmProfile() != null) {
                        vmConfigSpec.getVmProfile().add(managedInfo.getSpbmProfile());
                    }

                    if (managedInfo.getVAppConfig() != null) {
                        vmConfigSpec.setVAppConfig(managedInfo.getVAppConfig().toVmConfigInfo());
                    }
                }
                ManagedObjectReference moref = null;
                if (getOptions().isParentAVApp()) {
                    moref = getVimConnection().createChildVm(vmConfigSpec, managedInfo.getResourcePollInfo(),
                            managedInfo.getHostInfo());
                } else {
                    moref = getVimConnection().createVm(vmConfigSpec, managedInfo.getFolderInfo(),
                            managedInfo.getResourcePollInfo(), managedInfo.getHostInfo());
                }
                final VirtualMachineManager vmm = new VirtualMachineManager(getVimConnection(), moref);

                resultAction.setFcoTarget(vmm);
                rar.setLocations(vmm.getLocations());
                rar.setFcoEntityInfo(vmm.getFcoInfo());
                rar.setFirstClassObject(vmm);
                rar.setNumberOfDisk(profile.getNumberOfDisks());
                vmm.importNvram(profile.getTargetOperation().getObject(nvramContentPath));

                final List<String> diskNameList = vmm.getConfig().getAllDiskNameList();
                for (final DiskProfile disk : profile.getDisks()) {
                    disk.setRemoteDiskPath(diskNameList.get(disk.getDiskId()));

                    new CoreResultActionDiskRestore(disk.getDiskId(), profile, rar);
                }
            }
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            resultAction.failure(e);
            Thread.currentThread().interrupt();
        } catch (final AlreadyExistsFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg
                | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidNameFaultMsg
                | InvalidStateFaultMsg | OutOfBoundsFaultMsg | RuntimeFaultFaultMsg | VmConfigFaultFaultMsg
                | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException | ProfileException
                | VimObjectNotExistException | IOException e) {
            resultAction.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            resultAction.done();
        }
        return resultAction.isSuccessful();
    }

    private boolean restoreByImport(final CoreResultActionVmRestore rar) throws CoreResultActionException {
        boolean result;
        final CoreResultActionRestoreMetadata resultAction = new CoreResultActionRestoreMetadata(rar);
        resultAction.start();
        final GenerationProfile profile = rar.getProfile();
        final CoreRestoreManagedInfo managedInfo = rar.getManagedInfo();
        StorageDirectoryInfo storageVmHomeDirectory = null;
        final ITargetOperation target = profile.getTargetOperation();
        final String nvramContentPath = profile.getNvramContentPath();
        final String vmxContentPath = profile.getVmxContentPath();
        if (!target.doesObjectExist(vmxContentPath)) {
            resultAction.failure(vmxContentPath + " content doesn't exist");
        } else if (!target.doesObjectExist(nvramContentPath)) {
            resultAction.failure(nvramContentPath + " content doesn't exist");
        } else {
            try {
                final VirtualMachineManager vmm = new VirtualMachineManager(getVimConnection(), managedInfo.getName(),
                        managedInfo.getDcInfo());
                resultAction.setFcoTarget(vmm);
                storageVmHomeDirectory = vmm.createVMHomeDirectory(managedInfo);
                if (storageVmHomeDirectory != null) {

                    vmm.importVmx(managedInfo, profile, storageVmHomeDirectory, target.getObject(vmxContentPath));
                    final ManagedObjectReference vmMor = getVimConnection().registerVirtualMachine(managedInfo,
                            storageVmHomeDirectory);
                    vmm.importNvram(managedInfo, storageVmHomeDirectory, target.getObject(nvramContentPath));
                    if (vmm.update(vmMor)) {
                        rar.setLocations(vmm.getLocations());

                        rar.setFcoEntityInfo(vmm.getFcoInfo());
                        rar.setFirstClassObject(vmm);
                        rar.setNumberOfDisk(profile.getNumberOfDisks());
                    }

                } else {
                    resultAction.failure("Failed to create home directory");
                }
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                resultAction.failure(e);
                Thread.currentThread().interrupt();
            } catch (final CoreResultActionException | AlreadyExistsFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg
                    | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidNameFaultMsg
                    | InvalidStateFaultMsg | NotFoundFaultMsg | OutOfBoundsFaultMsg | RuntimeFaultFaultMsg
                    | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg | CannotCreateFileFaultMsg
                    | VimObjectNotExistException | InvalidCollectorVersionFaultMsg | VimTaskException | IOException e) {
                resultAction.failure(e);
                Utility.logWarning(this.logger, e);
                rar.setOnErrorDestroyDirectory(true);
            } finally {
                resultAction.done();
            }
        }
        result = resultAction.isSuccessful();
        if (resultAction.isFails() && rar.isOnErrorDestroyDirectory()) {
            try {
                getVimConnection().deleteDirectory(storageVmHomeDirectory);
                if (this.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("VM directory:%s deleted", storageVmHomeDirectory);
                    this.logger.info(msg);
                }
            } catch (FileFaultFaultMsg | FileNotFoundFaultMsg | InvalidDatastoreFaultMsg | InvalidDatastorePathFaultMsg
                    | RuntimeFaultFaultMsg e) {
                Utility.logWarning(this.logger, e);
            }

        } else {
            result = reconfig(profile, rar);
        }
        return result;
    }

    protected boolean restoreManagedInfo(final GenerationProfile profile, final CoreResultActionVmRestore rar)
            throws CoreResultActionException {
        final int i = 0;
        /**
         * Start Section RestoreManagedInfo
         */
        rar.getInteractive().startRestoreManagedInfo();

        final CoreResultActionGenerateRestoreManagedInfo ragrm = new CoreResultActionGenerateRestoreManagedInfo(rar);
        ragrm.start();
        final CoreRestoreManagedInfo managedInfo = new CoreRestoreManagedInfo(profile.getNumberOfVnics());
        try {
            ragrm.setManagedInfo(managedInfo);
            managedInfo.setRecovery(getOptions().isRecover());
            managedInfo.setName(getRestoreFcoName(profile.getFcoEntity(), rar.getRestoreFcoIndex()));
            checkOptions(managedInfo, profile);

            if ((getOptions().getDatacenter() != null) && (managedInfo.getDcInfo() == null)) {
                managedInfo.setDcInfo(
                        getVimConnection().getManagedEntityInfo(EntityType.Datacenter, getOptions().getDatacenter()));
                if (managedInfo.getDcInfo() == null) {
                    final String msg = String.format("Original Datacenter %s doesn't exist",
                            getOptions().getDatacenter());
                    this.logger.warning(msg);
                    ragrm.failure(msg);
                }
            }
            if (ragrm.isRunning()) {
                if ((getOptions().getFolder() != null) && (managedInfo.getFolderInfo() == null)) {
                    if (managedInfo.getDcInfo() == null) {
                        managedInfo.setFolderInfo(
                                getVimConnection().getManagedEntityInfo(EntityType.Folder, getOptions().getFolder()));
                    } else {
                        managedInfo.setFolderInfo(getVimConnection().getManagedEntityInfo(
                                managedInfo.getDcInfo().getMoref(), EntityType.Folder, getOptions().getFolder()));
                    }
                    if (managedInfo.getFolderInfo() == null) {
                        final String msg = String.format("Original Folder %s doesn't exist", getOptions().getFolder());
                        this.logger.warning(msg);
                        ragrm.failure(msg);
                    }
                }
                if (ragrm.isRunning()) {
                    if ((getOptions().getHost() != null) && (managedInfo.getDatastoreInfo() == null)) {
                        if (managedInfo.getDcInfo() == null) {
                            managedInfo.setHostInfo(getVimConnection().getManagedEntityInfo(EntityType.HostSystem,
                                    getOptions().getHost()));
                        } else {
                            managedInfo.setHostInfo(getVimConnection().getManagedEntityInfo(
                                    managedInfo.getDcInfo().getMoref(), EntityType.HostSystem, getOptions().getHost()));
                        }
                        if (managedInfo.getHostInfo() == null) {
                            final String msg = String.format("Original ESX host %s doesn't exist",
                                    getOptions().getHost());
                            this.logger.warning(msg);
                            ragrm.failure(msg);
                        }
                    }

                    if (ragrm.isRunning()) {
                        if ((getOptions().getDatastore() != null) && (managedInfo.getDatastoreInfo() == null)) {
                            if (managedInfo.getDcInfo() == null) {
                                managedInfo.setDatastoreInfo(getVimConnection()
                                        .getManagedEntityInfo(EntityType.Datastore, getOptions().getDatastore()));

                            } else {
                                managedInfo.setDatastoreInfo(
                                        getVimConnection().getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
                                                EntityType.Datastore, getOptions().getDatastore()));
                            }
                            if (managedInfo.getDatastoreInfo() == null) {
                                final String msg = String.format("Original Datastore %s doesn't exist",
                                        getOptions().getDatastore());
                                this.logger.warning(msg);
                                ragrm.failure(msg);

                            }
                        } else {
                            if (managedInfo.getHostInfo() != null) {
                                final ManagedObjectReference entityMor = getVimConnection()
                                        .getDatastoreByHost(managedInfo.getHostInfo().getMoref());
                                managedInfo.setDatastoreInfo(
                                        new ManagedEntityInfo(getVimConnection().getVimHelper().entityName(entityMor),
                                                entityMor, getVimConnection().getServerIntanceUuid()));
                            }
                        }

                        if (ragrm.isRunning()) {
                            if ((managedInfo.getDcInfo() == null) && (managedInfo.getDatastoreInfo() != null)) {
                                managedInfo.setDcInfo(getVimConnection()
                                        .getDatacenterByMoref(managedInfo.getDatastoreInfo().getMoref()));
                            }

                            if ((managedInfo.getHostInfo() == null) && (managedInfo.getDatastoreInfo() != null)) {
                                for (final DatastoreHostMount hostMount : getVimConnection()
                                        .getHostsByDatastore(managedInfo.getDatastoreInfo().getMoref())) {
                                    if (hostMount.getMountInfo().isAccessible()
                                            && hostMount.getMountInfo().isMounted()) {
                                        managedInfo.setHostInfo(new ManagedEntityInfo(
                                                getVimConnection().getHostName(hostMount.getKey()), hostMount.getKey(),
                                                getVimConnection().getServerIntanceUuid()));
                                        break;
                                    }
                                }

                            }
                            if (managedInfo.getResourcePollInfo() == null) {
                                setResourcePoolAndFolder(managedInfo, ragrm);
                            }
                            if (ragrm.isRunning()) {
                                if (managedInfo.getFolderInfo() == null) {

                                    final ManagedObjectReference vmFolder = (ManagedObjectReference) getVimConnection()
                                            .getVimHelper().entityProps(managedInfo.getDcInfo(), "vmFolder");
                                    managedInfo.setFolderInfo(new ManagedEntityInfo(
                                            getVimConnection().getVimHelper().entityProps(vmFolder, "name").toString(),
                                            vmFolder, getVimConnection().getServerIntanceUuid()));
                                }
                                setSpbmProfile(managedInfo);
                                managedInfo.setVAppConfig(restoreVmOvfConfig(profile, ragrm));
                            }
                        }
                    }
                }
            }
            if (ragrm.isRunning()) {
                networksMapping(managedInfo, profile);
                disksMapping(managedInfo, profile);
            }
            if (this.logger.isLoggable(Level.INFO)) {
                String msg = String.format("Restore source: %s  ", profile.getFcoEntity().toString());
                this.logger.info(msg);
                msg = String.format("Restore destination: \"%s\".", ragrm.getName());
                this.logger.info(msg);
            }
        } catch (final IllegalFormatException e) {
            ragrm.failure("Illegal Name format: " + e.getMessage());
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            ragrm.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | VimObjectNotExistException
                | com.vmware.pbm.RuntimeFaultFaultMsg | InvalidArgumentFaultMsg | VimOperationException
                | SafekeepingUnsupportedObjectException e) {
            Utility.logWarning(this.logger, e);
            ragrm.failure(e);
        } catch (final Exception e) {
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("Unexpected Exception type");
            }
            Utility.logWarning(this.logger, e);
            ragrm.failure(e);
        } finally {
            // assign the result to vmrestore
            rar.setManagedInfo(managedInfo);
            ragrm.done();
            rar.getInteractive().endRestoreManagedInfo();
            /**
             * End Section RestoreManagedInfo
             */
        }
        return ragrm.isSuccessful();

    }

    private boolean restoreMetadata(final CoreResultActionVmRestore rar) throws CoreResultActionException {
        boolean result = false;
        /**
         * Start Section RestoreMetadata
         */
        rar.getInteractive().startRestoreMetadata();
        if (rar.getOptions().isImportVmxFile()) {
            result = restoreByImport(rar);

        } else {
            result = restoreByCreate(rar);
        }

        rar.getInteractive().endRestoreMetadata();
        /**
         * End Section RestoreMetadata
         */
        if (result) {
            rar.setOnErrorDestroyFco(true);
        }
        return result;
    }

    private void setResourcePoolAndFolder(final CoreRestoreManagedInfo managedInfo,
            final CoreResultActionGenerateRestoreManagedInfo result)
            throws CoreResultActionException, VimObjectNotExistException {
        try {
            if (getOptions().getResourcePool() != null) {
                if (managedInfo.getDcInfo() == null) {
                    if (managedInfo.getHostInfo() == null) {
                        managedInfo.setResourcePollInfo(getVimConnection().getManagedEntityInfo(EntityType.ResourcePool,
                                getOptions().getResourcePool()));
                    } else {
                        final ManagedObjectReference rpMor = getVimConnection()
                                .getResourcePoolByHost(managedInfo.getHostInfo().getMoref());
                        managedInfo.setResourcePollInfo(new ManagedEntityInfo(
                                getVimConnection().getVimHelper().entityProps(rpMor, "name").toString(), rpMor,
                                getVimConnection().getServerIntanceUuid()));
                    }
                } else {
                    if (managedInfo.getDcInfo() == null) {
                        managedInfo.setResourcePollInfo(getVimConnection().getManagedEntityInfo(EntityType.ResourcePool,
                                getOptions().getResourcePool()));
                    } else {
                        managedInfo.setResourcePollInfo(
                                getVimConnection().getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
                                        EntityType.ResourcePool, getOptions().getResourcePool()));
                    }

                }
                if (managedInfo.getResourcePollInfo() == null) {
                    final String msg = String.format("ResourcePool %s doesn't exist", getOptions().getResourcePool());
                    this.logger.warning(msg);
                    result.failure(msg);
                }
            } else {
                final ManagedObjectReference rpMor = getVimConnection()
                        .getResourcePoolByHost(managedInfo.getHostInfo().getMoref());
                managedInfo.setResourcePollInfo(
                        new ManagedEntityInfo(getVimConnection().getVimHelper().entityProps(rpMor, "name").toString(),
                                rpMor, getVimConnection().getServerIntanceUuid()));
            }
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            result.failure(e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            result.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

}
