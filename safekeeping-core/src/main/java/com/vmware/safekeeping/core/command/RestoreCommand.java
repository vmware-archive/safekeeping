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

import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.logger.VmbkLogFormatter;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;

public class RestoreCommand extends AbstractCommandWithOptions implements IRestoreEntry, ICommandSupportForSnapshot {

    private FileHandler filehandler;

    public RestoreCommand(final AbstractCoreResultActionRestore rar) {
        configureLog(rar);
        this.options = rar.getOptions();
    }

    public void actionRestore(final ITarget iTarget, final AbstractCoreResultActionRestore rar)
            throws CoreResultActionException {

        final IFirstClassObject fco = rar.getFirstClassObject();
        if (Vmbk.isAbortTriggered()) {
            rar.aborted();
        } else {
            try {
                rar.getInteractive().start();
                final ITargetOperation targetOperations = iTarget.newTargetOperation(rar.getFcoEntityInfo(),
                        this.logger);
                if (fco instanceof VirtualAppManager) {
                    actionRestoreVAppEntry(targetOperations, (CoreResultActionVappRestore) rar, this.logger);
                } else if (fco instanceof VirtualMachineManager) {
                    actionRestoreVmEntry(targetOperations, (CoreResultActionVmRestore) rar, this.logger);
                } else if (fco instanceof ImprovedVirtualDisk) {
                    actionRestoreIvdEntry(targetOperations, (CoreResultActionIvdRestore) rar, this.logger);
                } else {
                    throw new CoreResultActionException(fco);
                }

            } finally {
                rar.getInteractive().finish();
                if (this.filehandler != null) {
                    this.logger.removeHandler(this.filehandler);
                    this.filehandler.close();
                }
            }
        }
    }

    private void configureLog(final AbstractCoreResultActionRestore rab) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
            final String dirName = String.format("%s-%s_%s", rab.getEntityType().toString(true),
                    rab.getFcoEntityInfo().getName(), rab.getFcoEntityInfo().getUuid());

            final String directoryLogs = CoreGlobalSettings.getLogsPath().concat(File.separator).concat(dirName)
                    .concat(File.separator).concat("restore");
            final File logDir = new File(directoryLogs);
            if (!logDir.isDirectory()) {
                logDir.mkdirs();
            }
            final String logFile = String.format("%s%s%s-restore.log", directoryLogs, File.separator,
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
    public CoreRestoreOptions getOptions() {
        return (CoreRestoreOptions) this.options;
    }

    @Override
    public final void initialize() {
        setOptions(new CoreRestoreOptions());
    }

}
