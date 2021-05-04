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

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.VirtualBackupCommand;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
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
import com.vmware.safekeeping.external.command.interactive.VirtualBackupNoInteractive;
import com.vmware.safekeeping.external.command.interactive.VirtualBackupVappNoInteractive;
import com.vmware.safekeeping.external.command.support.Tasks;
import com.vmware.safekeeping.external.exception.InvalidTask;
import com.vmware.safekeeping.external.result.ResultActionIvdVirtualBackup;
import com.vmware.safekeeping.external.result.ResultActionVappVirtualBackup;
import com.vmware.safekeeping.external.result.AbstractResultActionVirtualBackup;
import com.vmware.safekeeping.external.result.ResultActionVmVirtualBackup;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.VirtualBackupOptions;

public class ExternalVirtualBackupCommand extends AbstractCommandWithOptions {
    class RunnableVirtualBackup extends AbstractRunnableCommand {
        private final ITarget itarget;
        private final VirtualBackupCommand vBackupCmd;

        public RunnableVirtualBackup(final ITarget target, final AbstractCoreResultActionVirtualBackup ra) {
            super(ra);
            this.itarget = target;
            setName("backup_" + ra.getFcoToString());
            this.vBackupCmd = new VirtualBackupCommand(ra);

        }

        @Override
        public AbstractCoreResultActionVirtualBackup getResultAction() {
            return (AbstractCoreResultActionVirtualBackup) this.ra;
        }

        @Override
        public void run() {
            try {
                this.vBackupCmd.actionVirtualBackup(this.itarget, getResultAction());

            } catch (final CoreResultActionException e) {
                Utility.logWarning(ExternalVirtualBackupCommand.this.logger, e);
            } finally {
                if (ExternalVirtualBackupCommand.this.logger.isLoggable(Level.INFO)) {
                    ExternalVirtualBackupCommand.this.logger.info("ResultActionVirtualBackup end");
                }
                getResultAction().done();
            }
        }
    }

    public ExternalVirtualBackupCommand(final VirtualBackupOptions options) {
        final CoreVirtualBackupOptions loptions = new CoreVirtualBackupOptions();
        VirtualBackupOptions.convert(options, loptions);
        setOptions(loptions);
    }

    public List<AbstractResultActionVirtualBackup> action(final ConnectionManager connectionManager) throws InvalidTask {
        final List<AbstractResultActionVirtualBackup> result = new ArrayList<>();
        final ITarget target = connectionManager.getRepositoryTarget();
        if (connectionManager.isConnected()) {
            final List<IFirstClassObject> fcoList = getFcoTarget(connectionManager, getOptions().getAnyFcoOfType());
            if (fcoList.isEmpty()) {
                throw new InvalidTask(Tasks.NO_VALID_FCO_TARGETS);
            } else if (target == null) {
                throw new InvalidTask(Tasks.NO_REPOSITORY_TARGET_ERROR_MESSAGE);
            } else if (target.isEnable()) {
                for (final IFirstClassObject fco : fcoList) {
                    AbstractCoreResultActionVirtualBackup rab = null;
                    if (fco instanceof VirtualAppManager) {
                        rab = new CoreResultActionVappVirtualBackup(fco, getOptions());
                        rab.setInteractive(new VirtualBackupVappNoInteractive((CoreResultActionVappVirtualBackup) rab));
                    } else if (fco instanceof VirtualMachineManager) {
                        rab = new CoreResultActionVmVirtualBackup(fco, getOptions());
                        rab.setInteractive(new VirtualBackupNoInteractive((CoreResultActionVmVirtualBackup) rab));
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        rab = new CoreResultActionIvdVirtualBackup(fco, getOptions());
                        rab.setInteractive(new VirtualBackupNoInteractive((CoreResultActionIvdVirtualBackup) rab));
                    } else {
                        throw new InvalidTask("Unsupported type" + fco.getEntityType().toString());
                    }
                    final RunnableVirtualBackup runnable = new RunnableVirtualBackup(
                            connectionManager.getRepositoryTarget(), rab);
                    runnable.run();
                    if (fco instanceof VirtualAppManager) {
                        final ResultActionVappVirtualBackup rs = new ResultActionVappVirtualBackup();
                        rs.convert(rab);
                        result.add(rs);
                    } else if (fco instanceof VirtualMachineManager) {
                        final ResultActionVmVirtualBackup rs = new ResultActionVmVirtualBackup();
                        rs.convert((CoreResultActionVmVirtualBackup) rab);
                        result.add(rs);
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        final ResultActionIvdVirtualBackup rs = new ResultActionIvdVirtualBackup();
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
                    AbstractCoreResultActionVirtualBackup rab = null;
                    if (fco instanceof VirtualAppManager) {
                        rab = new CoreResultActionVappVirtualBackup(fco, getOptions());
                        rab.setInteractive(new VirtualBackupVappNoInteractive((CoreResultActionVappVirtualBackup) rab));
                    } else if (fco instanceof VirtualMachineManager) {
                        rab = new CoreResultActionVmVirtualBackup(fco, getOptions());
                        rab.setInteractive(new VirtualBackupNoInteractive((CoreResultActionVmVirtualBackup) rab));
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        rab = new CoreResultActionIvdVirtualBackup(fco, getOptions());
                        rab.setInteractive(new VirtualBackupNoInteractive((CoreResultActionIvdVirtualBackup) rab));
                    } else {
                        rab = null;
                        result.unsupportedTypeFailure(fco.getEntityType());
                    }
                    if (rab != null) {
                        final RunnableVirtualBackup runnable = new RunnableVirtualBackup(
                                connectionManager.getRepositoryTarget(), rab);
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
    public CoreVirtualBackupOptions getOptions() {
        return (CoreVirtualBackupOptions) this.options;
    }

    @Override
    protected void initialize() {
        // TODO Auto-generated method stub

    }

    /**
     * @param options the options to set
     */
    public void setOptions(final CoreBackupOptions options) {
        this.options = options;
    }

}
