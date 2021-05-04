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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public interface IRestoreEntry {
    default CoreResultActionIvdRestore actionRestoreIvdEntry(final ITargetOperation target,
            final CoreResultActionIvdRestore rar, final CoreRestoreOptions options, final Logger logger)
            throws CoreResultActionException {
        final ImprovedVirtualDisk ivd = rar.getFirstClassObject();
        rar.start();
        try {
            final RestoreIvd restoreIvd = new RestoreIvd(ivd.getVimConnection(), options, logger);
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(rar.getFcoEntityInfo(), target,
                    ArchiveManagerMode.READ);
            restoreIvd.restore(vmArcMgr, rar);
        } catch (final IOException | ArchiveException e) {
            final String msg = String.format("Restore of %s failed.", ivd.toString());
            logger.warning(msg);
            rar.failure(e);
            Utility.logWarning(logger, e);
        } finally {
            rar.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, rar.getSpentTotalTimeUntilNowAsString());
            }
        }
        return rar;
    }

    default CoreResultActionIvdRestore actionRestoreIvdEntry(final ITargetOperation target,
            final CoreResultActionIvdRestore rar, final Logger logger) throws CoreResultActionException {
        return actionRestoreIvdEntry(target, rar, getOptions(), logger);
    }

    /**
     * Restore a Virtual Appliance
     *
     * @param target
     * @param rar
     * @return
     * @throws CoreResultActionException
     */
    default CoreResultActionVappRestore actionRestoreVAppEntry(final ITargetOperation target,
            final CoreResultActionVappRestore rar, final CoreRestoreOptions options, final Logger logger)
            throws CoreResultActionException {
        final VirtualAppManager vApp = rar.getFirstClassObject();
        rar.start();
        try {
            final RestoreVApp restoreVApp = new RestoreVApp(vApp.getVimConnection(), options, logger);
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(rar.getFcoEntityInfo(), target,
                    ArchiveManagerMode.READ);
            restoreVApp.restore(vmArcMgr, rar);

        } catch (final IOException | ArchiveException e) {
            final String msg = String.format("Restore of %s failed.", vApp.toString());
            logger.warning(msg);
            rar.failure(e);
            Utility.logWarning(logger, e);
        } finally {
            rar.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, rar.getSpentTotalTimeUntilNowAsString());
            }
        }
        return rar;
    }

    default CoreResultActionVappRestore actionRestoreVAppEntry(final ITargetOperation target,
            final CoreResultActionVappRestore rar, final Logger logger) throws CoreResultActionException {
        return actionRestoreVAppEntry(target, rar, getOptions(), logger);
    }

    default CoreResultActionVmRestore actionRestoreVmEntry(final ITargetOperation target,
            final CoreResultActionVmRestore rar, final CoreRestoreOptions options, final Logger logger)
            throws CoreResultActionException {
        final VirtualMachineManager vmm = rar.getFirstClassObject();
        rar.start();
        try {
            final RestoreVm restoreVm = new RestoreVm(vmm.getVimConnection(), options, logger);
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(rar.getFcoEntityInfo(), target,
                    ArchiveManagerMode.READ);
            restoreVm.restore(vmArcMgr, rar);
        } catch (final IOException | ArchiveException e) {
            final String msg = String.format("Restore of %s failed.", vmm.toString());
            logger.warning(msg);
            rar.failure(e);
            Utility.logWarning(logger, e);
        } finally {
            rar.done();
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, rar.getSpentTotalTimeUntilNowAsString());
            }
        }
        return rar;
    }

    default CoreResultActionVmRestore actionRestoreVmEntry(final ITargetOperation target,
            final CoreResultActionVmRestore rar, final Logger logger) throws CoreResultActionException {
        return actionRestoreVmEntry(target, rar, getOptions(), logger);
    }

    CoreRestoreOptions getOptions();

}
