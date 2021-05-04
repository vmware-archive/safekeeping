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

import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.vmware.jvix.JVixException;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmNonExistentHubsFaultMsg;
import com.vmware.pbm.PbmProfileId;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractRestoreDiskInteractive;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestoreForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.enums.EntityType;

public abstract class AbstractRestoreFcoWithDisk extends AbstractRestoreFco {

    AbstractRestoreFcoWithDisk(final VimConnection vimConnection, final CoreRestoreOptions options,
            final Logger logger) {
        super(vimConnection, options, logger);
    }

    protected void setSpbmProfile(CoreRestoreManagedInfo managedInfo) throws VimObjectNotExistException,
            InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, SafekeepingUnsupportedObjectException {
        if (managedInfo.getSpbmProfile() == null) {
            if (getOptions().getStorageProfile() == null) {
                try {
                    managedInfo.setSpbmProfile(
                            getVimConnection().getPbmConnection().geeDefaultProfile(managedInfo.getDatastoreInfo()));
                } catch (PbmFaultFaultMsg | PbmNonExistentHubsFaultMsg e) {
                    Utility.logWarning(this.logger, e);
                    final String st = "No default profile for datastore:" + managedInfo.getDatastoreInfo().getName();
                    this.logger.warning(st);
                    managedInfo.setSpbmProfile(null);
                }
            } else {
                switch (getOptions().getStorageProfile().getSearchType()) {
                case NAME:
                    final String name = getOptions().getStorageProfile().getSearchValue();
                    if (getVimConnection().getPbmConnection().doesPpmProfileExist(name)) {

                        managedInfo.setSpbmProfile(getVimConnection().getPbmConnection().getDefinedProfileSpec(name));
                    } else {
                        throw new VimObjectNotExistException("Profile %s doesn't exist ", name);
                    }
                    break;
                case ID:
                    final PbmProfileId id = new PbmProfileId();
                    id.setUniqueId(getOptions().getStorageProfile().getSearchValue());
                    if (getVimConnection().getPbmConnection().doesPpmProfileExist(id)) {
                        managedInfo.setSpbmProfile(getVimConnection().getPbmConnection().getDefinedProfileSpec(id));
                    } else {
                        throw new VimObjectNotExistException("Profile %s doesn't exist ", id.getUniqueId());
                    }
                    break;

                case MOREF:
                    throw new SafekeepingUnsupportedObjectException("invalid search");

                }
            }
        }
    }

    protected boolean endVddkAccess(final AbstractCoreResultActionRestoreForEntityWithDisks rard, final Jvddk jvddk)
            throws CoreResultActionException, JVixException {

        long vddkCallResult;
        vddkCallResult = jvddk.disconnect(rard.getConnectionHandle());
        if (vddkCallResult != jDiskLibConst.VIX_OK) {
            rard.failure(jvddk.getErrorText(vddkCallResult));

        } else {

            vddkCallResult = jvddk.endAccess();
            if (vddkCallResult != jDiskLibConst.VIX_OK) {
                rard.failure(jvddk.getErrorText(vddkCallResult));

            }
        }
        rard.getInteractive().endVddkAccess();
        /**
         * End Section VddkAccess
         */

        return rard.isRunning();

    }

    private void restoreVmdk(final CoreResultActionDiskRestore radr, final Jvddk jvddk)
            throws CoreResultActionException {

        final GenerationProfile profile = radr.getProfile();
        radr.start();
        final AbstractRestoreDiskInteractive diskInteractive = ((AbstractCoreResultActionRestoreForEntityWithDisks) radr
                .getParent()).getInteractive().newDiskInteractiveInstance(radr);
        try {
            /**
             * Start Section DiskRestore
             */
            diskInteractive.startDiskRestore();

            jvddk.doRestoreJava(profile.getFcoArchiveManager(), getOptions(), radr, diskInteractive);
            if (radr.isAbortedOrFailed()) {
                final String msg = String.format("Restoring vmdk %d failed.", radr.getDiskId());
                this.logger.warning(msg);
            }

        } finally {
            radr.done();
            diskInteractive.endDiskRestore();
            /**
             * End Section DiskRestore
             */
        }
    }

    /**
     * Prepare for the VDDK access to the disk
     * 
     * @param rar
     * @param jvddk
     * @return true if succeed
     * @throws CoreResultActionException
     */
    protected boolean startVddkAccess(final AbstractCoreResultActionRestoreForEntityWithDisks rar, final Jvddk jvddk)
            throws CoreResultActionException {
        /**
         * Start Section VddkAccess
         */
        rar.getInteractive().startVddkAccess();
        try {
            if (rar.getEntityType() == EntityType.VirtualMachine) {
                final long vddkCallResult = jvddk.prepareForAccess(SafekeepingVersion.getInstance().getIdentity());
                if (vddkCallResult != jDiskLibConst.VIX_OK) {
                    rar.failure(jvddk.getErrorText(vddkCallResult));
                    return false;
                }
            }
            final String snapMorefValue = (rar.getCreateSnapshotAction() == null) ? null
                    : rar.getCreateSnapshotAction().getSnapMoref();
            rar.setConnectionHandle(jvddk.connectReadWrite(snapMorefValue, getOptions().getRequestedTransportModes()));
        } catch (final JVixException e) {
            Utility.logWarning(this.logger, e);
            rar.failure(e);
        }
        return rar.isRunning();
    }

    /**
     * Submit the restore thread to the Threads Executor
     * 
     * @param radr
     * @param jvddk
     * @param logger
     * @return
     */
    protected Future<CoreResultActionDiskRestore> submit(final CoreResultActionDiskRestore radr, final Jvddk jvddk,
            final Logger logger) {
        return ThreadsManager.executor(ThreadType.VDDK).submit(() -> {
            try {

                if (radr.isQueuing()) {
                    restoreVmdk(radr, jvddk);
                } else {
                    if (radr.isDone()) {
                        logger.warning("Disk operation on disk" + radr.getDiskId() + " over");
                    } else {
                        final String msg = "Disk operation state is inconsistent " + radr.getState().toString();
                        logger.warning(msg);
                        radr.failure(msg);
                    }
                }
            } catch (final CoreResultActionException e) {
                Utility.logWarning(logger, e);

            }
            return radr;

        });
    }

}
