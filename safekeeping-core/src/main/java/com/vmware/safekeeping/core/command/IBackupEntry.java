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
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public interface IBackupEntry {
    default CoreResultActionIvdBackup actionBackupIvdEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionIvdBackup resultAction,
            final Logger logger) throws CoreResultActionException {
        resultAction.start();
        final ImprovedVirtualDisk ivd = resultAction.getFirstClassObject();
        try {
            final BackupIvd backupIvd = new BackupIvd(ivd.getVimConnection(), getOptions(), logger);

            globalFcoCatalog.createNewProfile(resultAction.getFcoEntityInfo(), Calendar.getInstance());
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(resultAction.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            resultAction.setTargetName(target.getTargetName());
            resultAction.setLocations(ivd.getLocations());
            backupIvd.backup(ivd, vmArcMgr, resultAction);
        } catch (final IOException | ArchiveException | NoSuchAlgorithmException | InvalidPropertyFaultMsg
                | RuntimeFaultFaultMsg e) {
            logger.log(Level.WARNING, () -> String.format("backup of %s failed.", ivd.toString()));
            resultAction.failure(e);
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            resultAction.failure(e);
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, resultAction.getSpentTotalTimeUntilNowAsString());
            }
        }
        return resultAction;
    }

    default CoreResultActionVappBackup actionBackupVAppEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionVappBackup resultAction,
            final Logger logger) throws CoreResultActionException {
        resultAction.start();
        final VirtualAppManager vApp = resultAction.getFirstClassObject();
        try {
            final BackupVApp backupVApp = new BackupVApp(vApp.getVimConnection(), getOptions(), logger);
            globalFcoCatalog.createNewProfile(resultAction.getFcoEntityInfo(), resultAction.getStartDate());
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(resultAction.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            resultAction.setTargetName(target.getTargetName());
            resultAction.setLocations(vApp.getLocations());
            backupVApp.backup(vmArcMgr, globalFcoCatalog, resultAction);
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | ArchiveException | IOException
                | NoSuchAlgorithmException e) {
            resultAction.failure(e);
            final String msg = String.format("backup of %s failed.", vApp.toString());
            logger.warning(msg);
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            resultAction.failure(e);
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, resultAction.getSpentTotalTimeUntilNowAsString());
            }
        }
        return resultAction;
    }

    default CoreResultActionVmBackup actionBackupVmEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionVmBackup resultAction,
            final Logger logger) throws CoreResultActionException {
        resultAction.start();
        final VirtualMachineManager vmm = resultAction.getFirstClassObject();
        try {
            final BackupVM backupVm = new BackupVM(vmm.getVimConnection(), getOptions(), logger);
            globalFcoCatalog.createNewProfile(resultAction.getFcoEntityInfo(), Calendar.getInstance());
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(resultAction.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            resultAction.setGuestFlags(vmm.getHeaderGuestInfo());
            resultAction.setLocations(vmm.getLocations());
            resultAction.setBackupMode(getOptions().getRequestedBackupMode());
            resultAction.setTargetName(target.getTargetName());
            backupVm.backup(vmm, vmArcMgr, globalFcoCatalog, resultAction);
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | ArchiveException | IOException
                | VimObjectNotExistException | NoSuchAlgorithmException e) {
            resultAction.failure(e);
            final String msg = String.format("backup of %s failed.", vmm.toString());
            logger.warning(msg);
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, resultAction.getSpentTotalTimeUntilNowAsString());
            }
        }
        return resultAction;

    }

    CoreBackupOptions getOptions();
}
