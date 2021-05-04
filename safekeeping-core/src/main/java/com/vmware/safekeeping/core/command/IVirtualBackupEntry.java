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
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public interface IVirtualBackupEntry {
    default CoreResultActionIvdVirtualBackup actionVirtualBackupIvdEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionIvdVirtualBackup rac,
            final Logger logger) throws CoreResultActionException {
        rac.start();
        final ImprovedVirtualDisk ivd = rac.getFirstClassObject();
        try {
            final VirtualBackupIvd vBackupFco = new VirtualBackupIvd(ivd.getVimConnection(), getOptions(), logger);

            globalFcoCatalog.createNewProfile(rac.getFcoEntityInfo(), Calendar.getInstance());
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(rac.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            rac.setTargetName(target.getTargetName());
            vBackupFco.virtualBackup(vmArcMgr, rac);
        } catch (final IOException | ArchiveException | NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, () -> String.format("backup of %s failed.", ivd.toString()));
            rac.failure(e);
            Utility.logWarning(logger, e);

        } finally {
            rac.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, rac.getSpentTotalTimeUntilNowAsString());
            }
        }
        return rac;
    }

    default CoreResultActionVappVirtualBackup actionVirtualBackupVAppEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionVappVirtualBackup rac,
            final Logger logger) throws CoreResultActionException {
        rac.start();
        final VirtualAppManager vApp = rac.getFirstClassObject();
        try {
            final VirtualBackupVapp vBackupFco = new VirtualBackupVapp(vApp.getVimConnection(), getOptions(), logger);
            globalFcoCatalog.createNewProfile(rac.getFcoEntityInfo(), rac.getStartDate());
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(rac.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            rac.setTargetName(target.getTargetName());
            vBackupFco.virtualBackup(vmArcMgr, rac);
        } catch (ArchiveException | IOException | NoSuchAlgorithmException e) {
            rac.failure(e);
            final String msg = String.format("backup of %s failed.", vApp.toString());
            logger.warning(msg);
            Utility.logWarning(logger, e);

        } finally {
            rac.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, rac.getSpentTotalTimeUntilNowAsString());
            }
        }
        return rac;
    }

    default CoreResultActionVmVirtualBackup actionVirtualBackupVmEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final CoreResultActionVmVirtualBackup rac,
            final Logger logger) throws CoreResultActionException {
        rac.start();

        final VirtualMachineManager vmm = rac.getFirstClassObject();
        try {
            final VirtualBackupVm vBackupFco = new VirtualBackupVm(vmm.getVimConnection(), getOptions(), logger);
            globalFcoCatalog.createNewProfile(rac.getFcoEntityInfo(), Calendar.getInstance());
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(rac.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            rac.setTargetName(target.getTargetName());
            vBackupFco.virtualBackup(vmArcMgr, rac);
        } catch (final ArchiveException | IOException | NoSuchAlgorithmException e) {
            rac.failure(e);
            final String msg = String.format("backup of %s failed.", vmm.toString());
            logger.warning(msg);
            Utility.logWarning(logger, e);
        } finally {
            rac.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, rac.getSpentTotalTimeUntilNowAsString());
            }
        }
        return rac;

    }

    CoreVirtualBackupOptions getOptions();
}
