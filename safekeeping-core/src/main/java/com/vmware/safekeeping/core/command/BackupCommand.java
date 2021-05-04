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
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.logger.VmbkLogFormatter;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.type.K8sIvdComponent;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.K8s;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class BackupCommand extends AbstractCommandWithOptions implements IBackupEntry, ICommandSupportForSnapshot {

    private FileHandler filehandler;

    public BackupCommand(final AbstractCoreResultActionBackup rab) {
        this.options = rab.getOptions();
        configureLog(rab);
    }

    public void actionBackup(final ITarget iTarget, final AbstractCoreResultActionBackup rab)
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
                    actionBackupVAppEntry(targetOperations, globalFcoCatalog, (CoreResultActionVappBackup) rab,
                            this.logger);
                } else if (fco instanceof VirtualMachineManager) {
                    actionBackupVmEntry(targetOperations, globalFcoCatalog, (CoreResultActionVmBackup) rab,
                            this.logger);
                } else if (fco instanceof ImprovedVirtualDisk) {
                    actionBackupIvdEntry(targetOperations, globalFcoCatalog, (CoreResultActionIvdBackup) rab,
                            this.logger);
                } else if (fco instanceof K8s) {
                    actionBackupK8sEntry(targetOperations, globalFcoCatalog, (K8s) fco, (CoreResultActionVmBackup) rab);
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

    private CoreResultActionVmBackup actionBackupK8sEntry(final ITargetOperation target,
            final GlobalFcoProfileCatalog globalFcoCatalog, final K8s k8s, final CoreResultActionVmBackup resultAction)
            throws CoreResultActionException {
        resultAction.start();

        try {
            final Calendar cal = Calendar.getInstance();
            globalFcoCatalog.createNewProfile(resultAction.getFcoEntityInfo(), cal);
            final FcoArchiveManager vmArcMgr = new FcoArchiveManager(resultAction.getFcoEntityInfo(), target,
                    ArchiveManagerMode.WRITE);
            final GenerationProfileSpec spec = new GenerationProfileSpec(k8s, getOptions(), cal);
            final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(spec);
            target.createGenerationFolder(profGen);
            target.postGenerationProfile(profGen);

            vmArcMgr.postGenerationsCatalog();
            final String uuid = k8s.createSnapshot();

            final List<K8sIvdComponent> k8sTargets = k8s.getSnapshotInfo(uuid);

            for (final K8sIvdComponent k8starget : k8sTargets) {
                final CoreResultActionVmBackup subResult = new CoreResultActionVmBackup(k8starget.ivd, getOptions());
                globalFcoCatalog.createNewProfile(k8starget.ivd.getFcoInfo(), cal);
                resultAction.getSubOperations().add(subResult);
            }
        } catch (final RuntimeFaultFaultMsg | NoSuchAlgorithmException | IOException | ArchiveException
                | ProfileException | InvalidPropertyFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg
                | com.vmware.vslm.RuntimeFaultFaultMsg | com.vmware.pbm.InvalidArgumentFaultMsg | PbmFaultFaultMsg
                | VimObjectNotExistException e) {
            resultAction.failure(e);
            final String msg = String.format("backup of %s failed.", k8s.toString());
            this.logger.warning(msg);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            resultAction.failure(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.log(Level.INFO, resultAction.getSpentTotalTimeUntilNowAsString());
            }
        }

        return resultAction;

    }

    private void configureLog(final AbstractCoreResultActionBackup rab) {
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

    /**
     * @return the options
     */
    @Override
    public CoreBackupOptions getOptions() {
        return (CoreBackupOptions) this.options;
    }

    @Override
    public final void initialize() {
        setOptions(new CoreBackupOptions());
    }

}
