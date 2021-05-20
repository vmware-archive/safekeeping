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

import java.util.IllegalFormatException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.JVixException;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmNonExistentHubsFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGenerateRestoreManagedInfo;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateIvd;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk.AttachedVirtualMachine;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vslm.FileFaultFaultMsg;
import com.vmware.vslm.InvalidDatastoreFaultMsg;
import com.vmware.vslm.InvalidStateFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.TaskInProgressFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

class RestoreIvd extends AbstractRestoreFcoWithDisk {
    RestoreIvd(final VimConnection vimConnection, final CoreRestoreOptions options, final Logger logger) {
        super(vimConnection, options, logger);
    }

    /**
     * Delete a failed VM
     *
     * @param cravr
     * @return
     * @throws CoreResultActionException
     */
    private boolean destroy(final CoreResultActionIvdRestore cravr) throws CoreResultActionException {
        /**
         * Start Section DestroyVm
         */
        cravr.getInteractive().startDestroy();
        final CoreResultActionDestroy rar = new CoreResultActionDestroy(cravr);
        rar.start();
        boolean result = false;
        this.logger.warning("Restored failed delete the VM and folder. ");
        try {
            cravr.getFirstClassObject().destroy();

            result = true;

        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            Thread.currentThread().interrupt();
            rar.failure(e);

        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                | com.vmware.vslm.RuntimeFaultFaultMsg | TaskInProgressFaultMsg | VslmFaultFaultMsg | VslmTaskException
                | com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg
                | com.vmware.vim25.InvalidStateFaultMsg | com.vmware.vim25.NotFoundFaultMsg
                | com.vmware.vim25.TaskInProgressFaultMsg e) {
            Utility.logWarning(this.logger, e);
            rar.failure(e);
        } finally {
            rar.done();
            /**
             * End Section DestroyVm
             */
            cravr.getInteractive().endDestroy();
        }
        return result;
    }

    private boolean disksRestore(final CoreResultActionIvdRestore rar, final Jvddk jvddk)
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
            final Future<CoreResultActionDiskRestore> future = submit(rar.getResultActionOnDisk(), jvddk, this.logger);

            // A) Await all runnable to be done (blocking)

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
            result = true;

        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            rar.failure(e);
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

    private boolean manageExistingFco(final CoreResultActionIvdRestore ravr) throws CoreResultActionException {
        boolean result = false;
        String msg;
        final String name = ravr.getManagedInfo().getName();
        if (getVimConnection().isIvdByNameExist(name)) {
            final CoreResultActionDestroy rar = new CoreResultActionDestroy(ravr);
            try {
                rar.start();
                final List<ImprovedVirtualDisk> ivds = getVimConnection().getFind().findIvdByName(name);
                if (ivds.size() > 1) {
                    msg = String.format("ImprovedVirtualDisk with name:%s already exist in %d copies", name,
                            ivds.size());
                    this.logger.warning(msg);
                    rar.skip(msg);

                } else {
                    final ImprovedVirtualDisk ivd = ivds.get(0);
                    rar.setFcoEntityInfo(ivd.getFcoInfo());
                    if (getOptions().isOverwrite()) {
                        if (ivd.isAttached()) {
                            if (getOptions().isForce()) {
                                final AttachedVirtualMachine atvm = ivd.getAttachedVirtualMachine();
                                atvm.getVmm().detachDisk(ivd);

                                ivd.destroy();
                            } else {
                                rar.failure("virtual machine is powered On");
                            }
                        } else {
                            ivd.destroy();
                        }

                    } else {
                        msg = String.format("Improved virtual Disk name:%s already exist", name);
                        this.logger.warning(msg);
                        rar.skip(msg);
                        ravr.skip(msg);
                    }
                }
            } catch (final com.vmware.vslm.RuntimeFaultFaultMsg | InvalidPropertyFaultMsg | RuntimeFaultFaultMsg
                    | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                    | TaskInProgressFaultMsg | VslmFaultFaultMsg | VslmTaskException
                    | com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg
                    | com.vmware.vim25.InvalidStateFaultMsg | com.vmware.vim25.NotFoundFaultMsg
                    | com.vmware.vim25.TaskInProgressFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                    | VmConfigFaultFaultMsg e) {
                msg = String.format(
                        "Improved Virtual Disk name:%s cannot be removed - Check server logs for more information ",
                        name);
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
                msg = String.format("Improved Virtual Disk name:%s doesn't exist", name);
                this.logger.fine(msg);
            }
            result = true;
        }
        return result;
    }

    public CoreResultActionIvdRestore restore(final FcoArchiveManager vmArcMgr, final CoreResultActionIvdRestore rar)
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
            rar.getInteractive().endRetrieveProfile();
            /**
             * End Section RetrieveProfile
             */
            try {
                if (restoreManagedInfo(profile, rar) && checkPrivileges(rar) && manageExistingFco(rar)
                        && !getOptions().isDryRun() && restoreMetadata(rar)) {

                    final ImprovedVirtualDisk ivd = rar.getFirstClassObject();
                    final Jvddk jvddk = new Jvddk(this.logger, ivd);
                    if (startVddkAccess(rar, jvddk) && disksRestore(rar, jvddk) && endVddkAccess(rar, jvddk)) {
                        final String msg = rar.getFcoToString() + " Restore Success - start cleaning";
                        if (this.logger.isLoggable(Level.INFO)) {
                            this.logger.info(msg);
                        }
                    }
                }
            } catch (final JVixException | VimPermissionException e) {
                Utility.logWarning(this.logger, e);
                rar.failure(e);
            } finally {
                if (rar.isAbortedOrFailed() && rar.isOnErrorDestroyFco()) {
                    destroy(rar);
                }
            }

        }
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info("restoreIvd() end.");
        }
        return rar;
    }

    private void checkOptions(final CoreRestoreManagedInfo managedInfo, final GenerationProfile profile)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException,
            InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {

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
        if (getOptions().getDatacenter() == null) {

            String originalDatacenterName = profile.getDatacenterInfo().getName();
            final ManagedObjectReference dc = getVimConnection().getFind().findByInventoryPath(originalDatacenterName);
            if (dc != null) {
                managedInfo.setDcInfo(
                        new ManagedEntityInfo(originalDatacenterName, dc, getVimConnection().getServerIntanceUuid()));
            }

        }
        if (getOptions().getStorageProfile() == null) {
            managedInfo.setSpbmProfile(
                    getVimConnection().getPbmConnection().getDefinedProfileSpec(profile.getPbmProfileName()));
        }

    }

    protected boolean restoreManagedInfo(final GenerationProfile profile, final CoreResultActionIvdRestore rar)
            throws CoreResultActionException {

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
            checkOptions(managedInfo, profile);
            if ((getOptions().getDatastore() != null) && (managedInfo.getDatastoreInfo() == null)) {
                if (managedInfo.getDcInfo() == null) {
                    managedInfo.setDatastoreInfo(
                            getVimConnection().getManagedEntityInfo(EntityType.Datastore, getOptions().getDatastore()));

                } else {
                    managedInfo.setDatastoreInfo(getVimConnection().getManagedEntityInfo(
                            managedInfo.getDcInfo().getMoref(), EntityType.Datastore, getOptions().getDatastore()));
                }
                if (managedInfo.getDatastoreInfo() == null) {
                    final String msg = String.format("Original Datastore %s doesn't exist",
                            getOptions().getDatastore());
                    this.logger.warning(msg);
                    ragrm.failure(msg);

                }
            }
            if ((managedInfo.getDcInfo() == null) && (managedInfo.getDatastoreInfo() != null)) {
                managedInfo
                        .setDcInfo(getVimConnection().getDatacenterByMoref(managedInfo.getDatastoreInfo().getMoref()));
            }
            setSpbmProfile(managedInfo);
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
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | SafekeepingUnsupportedObjectException
                | InvalidArgumentFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg | VimObjectNotExistException e) {
            Utility.logWarning(this.logger, e);
            ragrm.failure(e);
        } catch (Exception e) {
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

    private boolean restoreMetadata(final CoreResultActionIvdRestore rar) throws CoreResultActionException {
        final CoreRestoreManagedInfo managedInfo = rar.getManagedInfo();
        final GenerationProfile profile = rar.getProfile();
        final CoreResultActionCreateIvd resultAction = new CoreResultActionCreateIvd();
        rar.getSubOperations().add(resultAction);
        /**
         * Start Section RestoreMetadata
         */
        rar.getInteractive().startRestoreMetadata();

        resultAction.start();
        try {
            final FileBackingInfoProvisioningType backingType = profile.getDiskProvisionType(0);
            resultAction.setName(managedInfo.getName());
            resultAction.setBackingType(backingType);
            resultAction.setSize(profile.getCapacity(0));
            resultAction.setSbpmProfileName(profile.getPbmProfileName());
            resultAction.setDatastoreInfo(managedInfo.getDatastoreInfo());
            if (StringUtils.isEmpty(resultAction.getName())) {
                resultAction.failure("New name is empty or invalid");
            } else {

                final VStorageObject vStore = getVimConnection().getVslmConnection().createIvd(
                        resultAction.getDatastoreInfo(), resultAction.getName(), backingType, resultAction.getSize(),
                        managedInfo.getSpbmProfile());

                final ManagedFcoEntityInfo fcoInfo = new ManagedFcoEntityInfo(resultAction.getName(),
                        ImprovedVirtualDisk.getMoref(vStore.getConfig().getId()), vStore.getConfig().getId().getId(),
                        managedInfo.getDatastoreInfo().getServerUuid());
                resultAction.setFcoEntityInfo(fcoInfo);
                rar.setFcoEntityInfo(fcoInfo);
                rar.setFirstClassObject(new ImprovedVirtualDisk(getVimConnection(), resultAction.getFcoEntityInfo(),
                        resultAction.getDatastoreInfo()));
                rar.setOnErrorDestroyFco(true);
                // Create CoreResultActionDiskRestore
                new CoreResultActionDiskRestore(profile, rar);

            }

        } catch (final FileFaultFaultMsg | InvalidDatastoreFaultMsg | com.vmware.vslm.RuntimeFaultFaultMsg
                | VslmFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
                | com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg
                | PbmFaultFaultMsg | PbmNonExistentHubsFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg
                | InvalidArgumentFaultMsg | VslmTaskException | VimTaskException e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            rar.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
            rar.getInteractive().endRestoreMetadata();
            /**
             * End Section RestoreMetadata
             */
        }

        return resultAction.isSuccessful();

    }

}
