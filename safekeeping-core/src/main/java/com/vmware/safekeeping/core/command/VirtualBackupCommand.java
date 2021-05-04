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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.logger.VmbkLogFormatter;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public class VirtualBackupCommand extends AbstractCommandWithOptions
        implements IVirtualBackupEntry, ICommandSupportForSnapshot {

    private FileHandler filehandler;

    public VirtualBackupCommand(final AbstractCoreResultActionVirtualBackup rab) {

        this.options = rab.getOptions();
        configureLog(rab);
    }

    public void actionVirtualBackup(final ITarget iTarget, final AbstractCoreResultActionVirtualBackup rab)
            throws CoreResultActionException {
        final IFirstClassObject fco = rab.getFirstClassObject();
        if (Vmbk.isAbortTriggered()) {
            rab.aborted();
        } else {
            try {
                rab.getInteractive().start();
                final GlobalFcoProfileCatalog globalFcoCatalog = new GlobalFcoProfileCatalog(iTarget);
                final ITargetOperation targetOperations = iTarget.newTargetOperation(rab.getFcoEntityInfo(),
                        this.logger);
                if (fco instanceof VirtualAppManager) {
                    actionVirtualBackupVAppEntry(targetOperations, globalFcoCatalog,
                            (CoreResultActionVappVirtualBackup) rab, this.logger);
                } else if (fco instanceof VirtualMachineManager) {
                    actionVirtualBackupVmEntry(targetOperations, globalFcoCatalog,
                            (CoreResultActionVmVirtualBackup) rab, this.logger);

                } else if (fco instanceof ImprovedVirtualDisk) {
                    actionVirtualBackupIvdEntry(targetOperations, globalFcoCatalog,
                            (CoreResultActionIvdVirtualBackup) rab, this.logger);
                } else {
                    throw new CoreResultActionException(fco);
                }

            } catch (final IOException e) {
                rab.failure(e);
                Utility.logWarning(this.logger, e);
            } finally {
                rab.getInteractive().finish();
                if (this.filehandler != null) {
                    this.logger.removeHandler(this.filehandler);
                    this.filehandler.close();
                }

            }
        }
    }

    private void configureLog(final AbstractCoreResultActionVirtualBackup rab) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            final String dirName = String.format("%s-%s_%s", rab.getEntityType().toString(true),
                    rab.getFcoEntityInfo().getName(), rab.getFcoEntityInfo().getUuid());
            final String directoryLogs = CoreGlobalSettings.getLogsPath().concat(File.separator).concat(dirName)
                    .concat(File.separator).concat("backup");
            final File logDir = new File(directoryLogs);
            if (!logDir.isDirectory()) {
                logDir.mkdirs();
            }

            final String logFile = String.format("%s%s%s.log", directoryLogs, File.separator,
                    dateFormat.format(new Date()));

            this.filehandler = new FileHandler(logFile);

            this.filehandler.setFormatter(new VmbkLogFormatter());
            this.logger.addHandler(this.filehandler);
        } catch (SecurityException | IOException e) {
            rab.failure(e);
        }
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    @Override
    public CoreVirtualBackupOptions getOptions() {
        return (CoreVirtualBackupOptions) this.options;
    }

    @Override
    public final void initialize() {
        setOptions(new CoreVirtualBackupOptions());
    }

}
