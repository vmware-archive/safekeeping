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
package com.vmware.safekeeping.external.command.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.BackupCommand;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.AbstractRunnableCommand;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.external.command.interactive.BackupNoInteractive;
import com.vmware.safekeeping.external.command.interactive.BackupVappNoInteractive;
import com.vmware.safekeeping.external.command.support.Tasks;
import com.vmware.safekeeping.external.exception.InvalidTask;
import com.vmware.safekeeping.external.result.ResultActionBackup;
import com.vmware.safekeeping.external.result.ResultActionIvdBackup;
import com.vmware.safekeeping.external.result.ResultActionVappBackup;
import com.vmware.safekeeping.external.result.ResultActionVmBackup;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.BackupOptions;

public class ExternalBackupCommand extends AbstractCommandWithOptions {
    static class RunnableBackup extends AbstractRunnableCommand {
        private final ITarget itarget;
        private final BackupCommand backupCmd;
        private final Logger logger;

        public RunnableBackup(final ITarget target, final AbstractCoreResultActionBackup ra, final Logger logger) {
            super(ra);
            this.itarget = target;
            setName("backup_" + ra.getFcoToString());
            this.backupCmd = new BackupCommand(ra);
            this.logger = logger;
        }

        @Override
        public AbstractCoreResultActionBackup getResultAction() {
            return (AbstractCoreResultActionBackup) this.ra;
        }

        @Override
        public void run() {
            try {
                this.backupCmd.actionBackup(this.itarget, getResultAction());

            } catch (final CoreResultActionException e) {
                Utility.logWarning(this.logger, e);
            } finally {
                if (this.logger.isLoggable(Level.INFO)) {
                    this.logger.info("ResultActionConnectThread end");
                }
                getResultAction().done();
            }
        }
    }

    public ExternalBackupCommand(final BackupOptions options) {
        final CoreBackupOptions loptions = new CoreBackupOptions();
        BackupOptions.convert(options, loptions);
        setOptions(loptions);
    }

    public List<ResultActionBackup> action(final ConnectionManager connectionManager) throws InvalidTask {
        final List<ResultActionBackup> result = new ArrayList<>();
        final ITarget target = connectionManager.getRepositoryTarget();
        if (connectionManager.isConnected()) {
            final List<IFirstClassObject> fcoList = getFcoTarget(connectionManager, getOptions().getAnyFcoOfType());
            if (fcoList.isEmpty()) {
                throw new InvalidTask(Tasks.NO_VALID_FCO_TARGETS);
            } else if (target == null) {
                throw new InvalidTask(Tasks.NO_REPOSITORY_TARGET_ERROR_MESSAGE);
            } else if (target.isEnable()) {
                for (final IFirstClassObject fco : fcoList) {
                    AbstractCoreResultActionBackup rab = null;
                    if (fco instanceof VirtualAppManager) {
                        rab = new CoreResultActionVappBackup(fco, getOptions());
                        rab.setInteractive(new BackupVappNoInteractive((CoreResultActionVappBackup) rab));
                    } else if (fco instanceof VirtualMachineManager) {
                        rab = new CoreResultActionVmBackup(fco, getOptions());
                        rab.setInteractive(new BackupNoInteractive((CoreResultActionVmBackup) rab));
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        rab = new CoreResultActionIvdBackup(fco, getOptions());
                        rab.setInteractive(new BackupNoInteractive((CoreResultActionIvdBackup) rab));
                    } else {
                        throw new InvalidTask("Unsupported type" + fco.getEntityType().toString());
                    }
                    final RunnableBackup runnable = new RunnableBackup(connectionManager.getRepositoryTarget(), rab,
                            this.logger);
                    runnable.run();
                    if (fco instanceof VirtualAppManager) {
                        final ResultActionVappBackup rs = new ResultActionVappBackup();
                        rs.convert(rab);
                        result.add(rs);
                    } else if (fco instanceof VirtualMachineManager) {
                        final ResultActionVmBackup rs = new ResultActionVmBackup();
                        rs.convert(rab);
                        result.add(rs);
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        final ResultActionIvdBackup rs = new ResultActionIvdBackup();
                        rs.convert(rab);
                        result.add(rs);
                    } else {
                        throw new InvalidTask(Tasks.UNSUPPORTED_ENTITY_TYPE + " " + fco.getEntityType().toString());
                    }

                }
            } else {
                throw new InvalidTask(Tasks.REPOSITORY_NOT_ACTIVE);
            }
        } else {
            throw new InvalidTask(Tasks.NO_VCENTER_CONNECTION);
        }
        return result;
    }

    public Tasks actionAsync(final ConnectionManager connectionManager) {
        final Tasks result = new Tasks();
        final ITarget target = connectionManager.getRepositoryTarget();
        if (connectionManager.isConnected()) {
            final List<IFirstClassObject> fcoList = getFcoTarget(connectionManager, getOptions().getAnyFcoOfType());
            if (fcoList.isEmpty()) {
                result.skipNoValidFcoTargets();
            } else if (target == null) {
                result.noRepositoryTargetFailure();
            } else if (target.isEnable()) {
                for (final IFirstClassObject fco : fcoList) {
                    AbstractCoreResultActionBackup rab = null;
                    if (fco instanceof VirtualAppManager) {
                        rab = new CoreResultActionVappBackup(fco, getOptions());
                        rab.setInteractive(new BackupVappNoInteractive((CoreResultActionVappBackup) rab));
                    } else if (fco instanceof VirtualMachineManager) {
                        rab = new CoreResultActionVmBackup(fco, getOptions());
                        rab.setInteractive(new BackupNoInteractive((CoreResultActionVmBackup) rab));
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        rab = new CoreResultActionIvdBackup(fco, getOptions());
                        rab.setInteractive(new BackupNoInteractive((CoreResultActionIvdBackup) rab));
                    } else {
                        rab = null;
                        result.unsupportedTypeFailure(fco.getEntityType());
                    }
                    if (rab != null) {
                        final RunnableBackup runnable = new RunnableBackup(connectionManager.getRepositoryTarget(), rab,
                                this.logger);
                        result.addResultThread(new ResultThread(rab, runnable.getId()));
                        ThreadsManager.executor(ThreadType.FCO).execute(runnable);
                        result.setState(OperationState.SUCCESS);
                    }
                }
            } else {
                result.repositoryNotActiveFailure();
            }
        } else {
            result.noVcenterConnectionFailure();
        }
        return result;
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
    protected void initialize() {
        // default implementation ignored
    }

    /**
     * @param options the options to set
     */
    public void setOptions(final CoreBackupOptions options) {
        this.options = options;
    }

}
