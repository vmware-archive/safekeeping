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
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionGenerateRestoreManagedInfo;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.command.results.CoreResultActionPower;
import com.vmware.safekeeping.core.command.results.CoreResultActionReplaceMorefInsideVapp;
import com.vmware.safekeeping.core.command.results.CoreResultActionRestoreMetadata;
import com.vmware.safekeeping.core.command.results.CoreResultActionRetrieveChildren;
import com.vmware.safekeeping.core.command.results.CoreResultActionUpdateVAppConfig;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfoIdGeneration;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.PowerOperation;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
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
import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VAppConfigFaultFaultMsg;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VmConfigFaultFaultMsg;

class RestoreVApp extends AbstractRestoreFco implements IRestoreEntry {
    RestoreVApp(final VimConnection vimConnection, final CoreRestoreOptions options, final Logger logger) {
        super(vimConnection, options, logger);
    }

    /**
     * Restore any vApp children VM
     *
     * @param rar
     * @param globalFcoCatalog
     * @throws CoreResultActionException
     */

    private boolean childrenRestore(final CoreResultActionVappRestore rar,
            final GlobalFcoProfileCatalog globalFcoCatalog) throws CoreResultActionException {
        boolean result = true;
        try {
            /**
             * Start Section ChildrenRestore
             */
            rar.getInteractive().startChildrenRestore();
            final ITarget target = globalFcoCatalog.getTarget();
            final List<Future<CoreResultActionVmRestore>> futures = new ArrayList<>();

            for (final CoreResultActionVmRestore rarChild : rar.getResultActionOnsChildVm()) {
                final Future<CoreResultActionVmRestore> f = submit(rarChild,
                        target.newTargetOperation(rarChild.getFcoEntityInfo(), this.logger));
                futures.add(f);
            }
            final StringBuilder reasons = new StringBuilder();
            // A) Await all runnable to be done (blocking)
            for (final Future<CoreResultActionVmRestore> future : futures) {
                // get will block until the future is done
                final CoreResultActionVmRestore rarChild = future.get();
                if (rarChild != null) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine(rarChild.toString());
                    }
                    if (!rarChild.isSuccessful()) {
                        reasons.append(reasons);
                        reasons.append("   ");
                        result = false;
                    }
                } else {
                    result = false;
                }
            }
            if (!result && !rar.isDone()) {
                rar.failure(reasons);
            }
        } catch (final InterruptedException e) {
            rar.failure(e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            rar.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            rar.getInteractive().endChildrenRestore();
            /**
             * End Section ChildrenRestore
             */
        }
        return result;
    }

    private boolean destroy(final CoreResultActionVappRestore cravr) throws CoreResultActionException {
        /**
         * Start Section DestroyVm
         */
        cravr.getInteractive().startDestroyVApp();
        boolean result = false;
        final CoreResultActionDestroy rar = new CoreResultActionDestroy(cravr);
        rar.start();
        this.logger.warning("Restored failed delete the vApp and folders. ");
        try {
            cravr.getFirstClassObject().destroy();
            result = true;

        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            Thread.currentThread().interrupt();
            rar.failure(e);

        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                | VimFaultFaultMsg e) {
            Utility.logWarning(this.logger, e);
            rar.failure(e);
        } finally {
            rar.done();
            /**
             * End Section DestroyVm
             */
            cravr.getInteractive().endDestroyVApp();
        }

        return result;

    }

    private boolean manageExistingFco(final CoreResultActionVappRestore ravr) throws CoreResultActionException {
        boolean result = false;
        String msg;
        final String name = ravr.getManagedInfo().getName();
        final ManagedObjectReference mor = getVimConnection().getVAppByName(name);
        if (mor != null) {
            final CoreResultActionDestroy rar = new CoreResultActionDestroy(ravr);
            try {
                rar.start();
                final VirtualAppManager vapp = new VirtualAppManager(getVimConnection(), mor);
                rar.setFcoEntityInfo(vapp.getFcoInfo());

                if (getOptions().isOverwrite()) {
                    rar.setShutdownRequired(vapp.getPowerState() == FcoPowerState.poweredOn);
                    if (rar.isShutdownRequired()) {
                        if (getOptions().isForce()) {
                            vapp.powerOff(getOptions().isForce());
                            vapp.destroy();
                        } else {
                            rar.failure("vApp is powered On");
                        }
                    } else {
                        vapp.destroy();
                    }
                } else {
                    msg = String.format("vApp name:%s already exist", name);
                    this.logger.warning(msg);
                    rar.skip(msg);
                    ravr.skip(msg);
                }
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                    | VimFaultFaultMsg | InvalidStateFaultMsg | TaskInProgressFaultMsg | VAppConfigFaultFaultMsg e) {
                msg = String.format("vApp name:%s cannot be removed ", name);
                this.logger.warning(msg);
                rar.failure(msg);
                Utility.logWarning(this.logger, e);
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
                msg = String.format("vApp name:%s doesn't exist", name);
                this.logger.info(msg);
            }
            result = true;
        }
        return result;
    }

    private boolean moveVmInParentVapp(final CoreResultActionVappRestore rar) {
        boolean result = true;
        if (getOptions().isImportVmxFile()) {
            /**
             * Start Section Relocation
             */
            rar.getInteractive().startRelocation();
            if (rar.isRunning()) {
                final List<ManagedObjectReference> vmListMoref = new ArrayList<>();
                for (final CoreResultActionVmRestore rarChild : rar.getResultActionOnsChildVm()) {

                    if (this.logger.isLoggable(Level.INFO)) {
                        this.logger.info("Moving Virtual Machine inside the vApp.");
                    }

                    vmListMoref.add(rarChild.getFirstClassObject().getMoref());
                }
                try {
                    getVimConnection().getVimPort().moveIntoResourcePool(rar.getFirstClassObject().getMoref(),
                            vmListMoref);

                } catch (DuplicateNameFaultMsg | InsufficientResourcesFaultFaultMsg | RuntimeFaultFaultMsg e) {
                    rar.failure(e);
                    result = false;
                } finally {
                    /**
                     * End Section Relocation
                     */
                    rar.getInteractive().endRelocation();
                }

            }
        }
        return result;
    }

    private boolean powerOn(final CoreResultActionVappRestore cravr) throws CoreResultActionException {
        final VirtualAppManager vApp = cravr.getFirstClassObject();
        String msg = null;
        boolean result = false;
        if (cravr.isRunning() && getOptions().isPowerOn()) {
            /**
             * Start Section VappPowerOn
             */
            cravr.getInteractive().startVappPowerOn();
            final CoreResultActionPower resultAction = new CoreResultActionPower(cravr);
            try {
                Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
                resultAction.start();
                resultAction.setRequestedPowerOperation(PowerOperation.powereOn);
                resultAction.setPowerState(vApp.getPowerState());
                if (vApp.powerOn()) {
                    msg = "PowerOn vApp " + vApp.getName() + " succeeded.";
                    this.logger.info(msg);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                resultAction.failure(e);
            } catch (FileFaultFaultMsg | InsufficientResourcesFaultFaultMsg | InvalidStateFaultMsg
                    | RuntimeFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg
                    | InvalidCollectorVersionFaultMsg | VimTaskException | VAppConfigFaultFaultMsg e) {
                resultAction.failure(e);
            } finally {
                resultAction.done();
                result = resultAction.isSuccessful();
            }
            /**
             * End Section VappPowerOn
             */
            cravr.getInteractive().endVappPowerOn();
        } else {
            result = true;
        }

        return result;

    }

    private boolean replaceVmIdentity(final CoreResultActionVappRestore rar) throws CoreResultActionException {
        boolean result = true;
        if (rar.isRunning()) {
            /**
             * Start Section ReplaceVMsIdentities
             */
            rar.getInteractive().startReplaceVMsIdentities();
            for (final CoreResultActionVmRestore rarChild : rar.getResultActionOnsChildVm()) {
                if (rarChild.isSuccessful()) {
                    final CoreResultActionReplaceMorefInsideVapp resultAction = new CoreResultActionReplaceMorefInsideVapp(
                            rar);
                    if (result) {
                        resultAction.start();
                        try {
                            if (rar.getManagedInfo().getVAppConfig().replaceVm(rarChild.getOriginalFcoInfo().getMoref(),
                                    rarChild.getFcoEntityInfo().getMoref())) {
                                final String msg = String.format("Replaced vm moref. old:%s to new:%s",
                                        rarChild.getFcoEntityInfo().getMorefValue(),
                                        rarChild.getFcoEntityInfo().getMoref());
                                this.logger.info(msg);
                                resultAction.success();
                            } else {
                                final String msg = String.format("Failed to replace  vm moref. old:%s to new:%s",
                                        rarChild.getFcoEntityInfo().getMorefValue(),
                                        rarChild.getFcoEntityInfo().getMoref());
                                this.logger.warning(msg);
                                resultAction.failure(msg);
                                result = false;
                            }
                        } finally {
                            resultAction.done();

                        }
                    } else {
                        resultAction.skip("replace vm moref failed");
                    }
                }
            }
        }
        /**
         * End Section ReplaceVMsIdentities
         */
        rar.getInteractive().endReplaceVMsIdentities();
        return result;
    }

    public CoreResultActionVappRestore restore(final FcoArchiveManager vmArcMgr, final CoreResultActionVappRestore rar)
            throws CoreResultActionException {
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("restoreVapp() start.");
        }
        try {
            /**
             * Start Section RetrieveProfile
             */
            rar.getInteractive().startRetrieveProfile();
            final ITargetOperation target = vmArcMgr.getRepositoryTarget();
            final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(target.getParent());
            this.logger.info("restoreVApp() start.");

            final CoreResultActionLoadProfile resultActionLoadProfile = actionLoadProfile(vmArcMgr, rar);

            if (resultActionLoadProfile.isSuccessful()) {

                final GenerationProfile profile = resultActionLoadProfile.getProfile();
                rar.getInteractive().endVappRetrieveProfile();
                /**
                 * End Section RetrieveProfile
                 */
                if (restoreManagedInfo(profile, rar) && checkPrivileges(rar) && manageExistingFco(rar)
                        && !getOptions().isDryRun() && restoreVappMetadata(rar)
                        && retreiveVappChildCollection(globalFcoCatalog, profile, rar)
                        && childrenRestore(rar, globalFcoCatalog) && moveVmInParentVapp(rar) && replaceVmIdentity(rar)
                        && updateVAppConfig(rar) && powerOn(rar)) {
                    final String msg = rar.getFcoToString() + " Restore Success - start cleaning";
                    if (this.logger.isLoggable(Level.INFO)) {
                        this.logger.info(msg);
                    }
                }
            }
        } catch (final IOException | VimPermissionException e) {
            rar.failure(e);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            rar.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            if (rar.isAbortedOrFailed() && rar.isOnErrorDestroyFco()) {
                destroy(rar);
            }
            rar.done();
        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("restoreVapp() end.");
        }
        return rar;
    }

    protected boolean restoreManagedInfo(final GenerationProfile profile, final CoreResultActionVappRestore rar)
            throws CoreResultActionException {

        String msg;
        /**
         * Start Section RestoreManagedInfo
         */
        rar.getInteractive().startRestoreManagedInfo();
        final CoreResultActionGenerateRestoreManagedInfo ragrm = new CoreResultActionGenerateRestoreManagedInfo(rar);
        ragrm.start();
        final CoreRestoreManagedInfo managedInfo = new CoreRestoreManagedInfo(
                CoreGlobalSettings.MAX_NUMBER_OF_VIRTUAL_MACHINE_NETWORK_CARDS);
        try {
            ragrm.setManagedInfo(managedInfo);

            managedInfo.setRecovery(getOptions().isRecover());
            managedInfo.setName(getRestoreFcoName(profile.getFcoEntity(), rar.getRestoreFcoIndex()));

            if (getOptions().getFolder() == null) {
                String vmFolder = profile.getFolderPath();
                if (StringUtils.isNotEmpty(vmFolder)) {
                    if (StringUtils.isNotEmpty(getOptions().getVmFolderFilter())
                            && !vmFolder.contains(getOptions().getVmFolderFilter())) {
                        msg = String.format(
                                "Original  vmFolder(%s) cannot be used because is not subfolder of vmFilter(%s). vmFilter will be used instead ",
                                vmFolder, getOptions().getVmFolderFilter());
                        this.logger.warning(msg);
                        vmFolder = getOptions().getVmFolderFilter();
                    }

                    ManagedObjectReference folder = getVimConnection().getFind().findByInventoryPath(vmFolder);

                    if ((folder == null) && StringUtils.isNotEmpty(getOptions().getVmFolderFilter())
                            && (!vmFolder.equals(getOptions().getVmFolderFilter()))) {
                        msg = String.format("vmFolder(%s) doesn't exist vmFilter(%s) will be used instead", vmFolder,
                                getOptions().getVmFolderFilter());
                        this.logger.warning(msg);
                        vmFolder = getOptions().getVmFolderFilter();
                        folder = getVimConnection().getFind().findByInventoryPath(vmFolder);
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
                if (originalResourcePoolName.endsWith("/" + profile.getName())) {
                    originalResourcePoolName = originalResourcePoolName.substring(0,
                            originalResourcePoolName.length() - profile.getName().length() - 1);
                }
                if (StringUtils.isNotEmpty(originalResourcePoolName)) {
                    if (StringUtils.isNotEmpty(getOptions().getResPoolFilter())
                            && !originalResourcePoolName.contains(getOptions().getResPoolFilter())) {
                        msg = String.format(
                                "Original ResourcePool(%s) cannot be used because is not subfolder of rpFilter(%s). rpFilter will be used instead ",
                                originalResourcePoolName, getOptions().getResPoolFilter());
                        this.logger.warning(msg);
                        originalResourcePoolName = getOptions().getResPoolFilter();
                    }
                    ManagedObjectReference rp = getVimConnection().getFind()
                            .findByInventoryPath(originalResourcePoolName);
                    if (rp == null) {
                        msg = String.format("ResourcePool(%s) doesn't exist rpFilter(%s) will be used instead",
                                originalResourcePoolName, getOptions().getResPoolFilter());
                        this.logger.warning(msg);
                        originalResourcePoolName = getOptions().getResPoolFilter();
                        rp = getVimConnection().getFind().findByInventoryPath(originalResourcePoolName);
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
            if (profile.isVappConfigAvailable()) {

                managedInfo.setVAppConfig(restoreVAppOvfConfig(profile, ragrm));
                // Resource pool information
                final ResourceConfigSpec configSpec = new ResourceConfigSpec();
                configSpec.setCpuAllocation(profile.getCpuAllocation().toResourceAllocationInfo());
                configSpec.setMemoryAllocation(profile.getMemAllocation().toResourceAllocationInfo());
                managedInfo.setResourceConfigSpec(configSpec);

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
                        msg = String.format("Folder %s doesn't exist", getOptions().getFolder());
                        this.logger.warning(msg);
                        ragrm.failure(msg);
                    }
                }
                if (ragrm.isRunning()) {
                    if (getOptions().getDatacenter() == null) {
                        String originalDatacenterName = profile.getFolderPath().substring(0,
                                profile.getFolderPath().indexOf('/'));
                        if (StringUtils.isEmpty(originalDatacenterName)) {
                            if (StringUtils.isNotEmpty(getOptions().getVmFolderFilter())) {
                                originalDatacenterName = getOptions().getVmFolderFilter().substring(0,
                                        originalDatacenterName.indexOf('/', 1));
                            }
                        } else {
                            if (StringUtils.isNotEmpty(getOptions().getVmFolderFilter())
                                    && !getOptions().getVmFolderFilter().startsWith(originalDatacenterName)) {
                                originalDatacenterName = getOptions().getVmFolderFilter().substring(0,
                                        getOptions().getVmFolderFilter().indexOf('/'));
                                msg = String.format(
                                        "Original Datacenter %s doesn't match with vmFilter(%s). %s will be used instead",
                                        originalDatacenterName, CoreGlobalSettings.getVmFilter(),
                                        originalDatacenterName);
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
                    if (managedInfo.getResourcePollInfo() == null) {
                        if (getOptions().getResourcePool() != null) {
                            if (managedInfo.getDcInfo() == null) {
                                if (managedInfo.getHostInfo() == null) {
                                    managedInfo.setResourcePollInfo(getVimConnection().getManagedEntityInfo(
                                            EntityType.ResourcePool, getOptions().getResourcePool()));
                                } else {
                                    final ManagedObjectReference rpMor = getVimConnection()
                                            .getResourcePoolByHost(managedInfo.getHostInfo().getMoref());
                                    managedInfo.setResourcePollInfo(new ManagedEntityInfo(
                                            getVimConnection().getVimHelper().entityProps(rpMor, "name").toString(),
                                            rpMor, getVimConnection().getServerIntanceUuid()));
                                }
                            } else {
                                if (managedInfo.getDcInfo() == null) {
                                    managedInfo.setFolderInfo(getVimConnection().getManagedEntityInfo(
                                            EntityType.ResourcePool, getOptions().getResourcePool()));
                                } else {
                                    managedInfo.setFolderInfo(
                                            getVimConnection().getManagedEntityInfo(managedInfo.getDcInfo().getMoref(),
                                                    EntityType.ResourcePool, getOptions().getResourcePool()));
                                }

                            }
                            if (managedInfo.getResourcePollInfo() == null) {

                                msg = String.format("ResourcePool %s doesn't exist", getOptions().getResourcePool());
                                this.logger.warning(msg);
                                ragrm.failure(msg);
                            }
                        } else {
                            final ManagedObjectReference rpMor = getVimConnection()
                                    .getResourcePoolByHost(managedInfo.getHostInfo().getMoref());
                            managedInfo.setResourcePollInfo(new ManagedEntityInfo(
                                    getVimConnection().getVimHelper().entityProps(rpMor, "name").toString(), rpMor,
                                    getVimConnection().getServerIntanceUuid()));
                        }
                    }
                }
            }
            if (this.logger.isLoggable(Level.INFO)) {
                msg = String.format("Restore source: %s  ", profile.getFcoEntity().toString());
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
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | VimObjectNotExistException e) {
            Utility.logWarning(this.logger, e);
            ragrm.failure(e);
        } finally {
            // assign the result to vapprestore
            rar.setManagedInfo(managedInfo);
            ragrm.done();
            rar.getInteractive().endRestoreManagedInfo();
            /**
             * End Section RestoreManagedInfo
             */
        }
        return ragrm.isSuccessful();
    }

    private boolean restoreVappMetadata(final CoreResultActionVappRestore rar) throws CoreResultActionException {
        final CoreRestoreManagedInfo managedInfo = rar.getManagedInfo();
        final CoreResultActionRestoreMetadata resultAction = new CoreResultActionRestoreMetadata(rar);
        /**
         * Start Section RestoreVappMetadata
         */
        rar.getInteractive().startRestoreVappMetadata();

        resultAction.start();
        VirtualAppManager vApp = null;
        try {

            final ManagedObjectReference moref = getVimConnection().createVApp(managedInfo.getName(),
                    managedInfo.getResourcePollInfo(), managedInfo.getFolderInfo(), managedInfo.getResourceConfigSpec(),
                    new VAppConfigSpec());
            if (moref != null) {
                vApp = new VirtualAppManager(getVimConnection(), moref);
                resultAction.setFcoTarget(vApp);
                rar.setOnErrorDestroyFco(true);
                rar.setFcoEntityInfo(vApp.getFcoInfo());
                rar.setLocations(vApp.getLocations());
                rar.setFirstClassObject(vApp);

            }
        } catch (final DuplicateNameFaultMsg | InsufficientResourcesFaultFaultMsg | InvalidNameFaultMsg
                | InvalidStateFaultMsg | RuntimeFaultFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            resultAction.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
            rar.getInteractive().endRestoreVappMetadata();
            /**
             * End Section RestoreVappMetadata
             */
        }

        return resultAction.isSuccessful();
    }

    private boolean retreiveVappChildCollection(final GlobalFcoProfileCatalog globalFcoCatalog,
            final GenerationProfile profile, final CoreResultActionVappRestore rar)
            throws CoreResultActionException, InterruptedException {
        final CoreResultActionRetrieveChildren resultAction = new CoreResultActionRetrieveChildren(rar);
        /**
         * Start Section RetreiveVappChildCollection
         */
        rar.getInteractive().startRetreiveVappChildCollection();

        try {
            resultAction.start();
            int index = 0;
            final Map<String, Integer> vappChildren = profile.getVappChildrenUuid();
            for (final Entry<String, Integer> entrySet : vappChildren.entrySet()) {
                ++index;
                final ManagedFcoEntityInfo entityInfo = globalFcoCatalog.getEntityByUuid(entrySet.getKey(),
                        EntityType.VirtualMachine);
                resultAction.getChildren().put(entrySet.getKey(),
                        new ManagedFcoEntityInfoIdGeneration(entityInfo, entrySet.getValue()));
                final CoreRestoreOptions vmOptions = new CoreRestoreOptions(getOptions(), rar.getFirstClassObject());
                if (!getOptions().isRecover()) {
                    vmOptions.setRecover(false);
                    if (getOptions().isAllowDuplicatedVmNamesInsideVapp()) {
                        vmOptions.setName(entityInfo.getName());
                    } else {
                        vmOptions.setName(rar.getFirstClassObject().getName() + "-" + entityInfo.getName());
                    }
                } else {
                    vmOptions.setName(entityInfo.getName());
                }
                vmOptions.setGenerationId(entrySet.getValue());
                final CoreResultActionVmRestore raChildVm = new CoreResultActionVmRestore(
                        new VirtualMachineManager(getVimConnection(), entityInfo), vmOptions);
                raChildVm.setInteractive(rar.getInteractive().newRestoreVmInteractiveInstance(raChildVm));
                rar.getResultActionOnsChildVm().add(raChildVm);
                raChildVm.setParent(rar);
                rar.getFcoChildren().add(entityInfo);
                raChildVm.setIndex(index);
            }
            rar.setNumberOfChildVm(rar.getResultActionOnsChildVm().size());

        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        } catch (final InterruptedException e) {
            resultAction.failure(e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
            resultAction.done();
            rar.getInteractive().endRetreiveVappChildCollection();
            /**
             * End Section RetreiveVappChildCollection
             */
        }
        return resultAction.isSuccessful();
    }

    private Future<CoreResultActionVmRestore> submit(final CoreResultActionVmRestore rarChild,
            final ITargetOperation newTargetOperation) {
        return ThreadsManager.executor(ThreadType.FCO).submit(() -> {
            CoreResultActionVmRestore result = null;
            try {
                result = actionRestoreVmEntry(newTargetOperation, rarChild, rarChild.getOptions(), this.logger);
            } catch (final CoreResultActionException e) {
                Utility.logWarning(this.logger, e);
            }
            return result;
        });
    }

    private boolean updateVAppConfig(final CoreResultActionVappRestore cravr) throws CoreResultActionException {
        boolean result = false;
        if (cravr.isRunning()) {

            /**
             * Start Section updatepdateVappConfig
             */
            cravr.getInteractive().startUpdateVappConfig();
            final CoreResultActionUpdateVAppConfig resultAction = new CoreResultActionUpdateVAppConfig(cravr);
            try {
                Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
                resultAction.start();
                resultAction.setVAppConfig(cravr.getManagedInfo().getVAppConfig());
                cravr.getFirstClassObject().updateVAppConfig(cravr.getManagedInfo().getVAppConfig());

                /**
                 * End Section updatepdateVappConfig
                 */
                cravr.getInteractive().endUpdateVappConfig();
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                Thread.currentThread().interrupt();
                resultAction.failure(e);
            } catch (FileFaultFaultMsg | InsufficientResourcesFaultFaultMsg | InvalidStateFaultMsg
                    | RuntimeFaultFaultMsg | ConcurrentAccessFaultMsg | InvalidNameFaultMsg | InvalidDatastoreFaultMsg
                    | DuplicateNameFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg e) {
                resultAction.failure(e);
            } finally {
                resultAction.done();
                result = resultAction.isSuccessful();
            }

        }

        return result;

    }

}
