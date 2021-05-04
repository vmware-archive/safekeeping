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
package com.vmware.safekeeping.core.ext.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.results.CoreResultActionPower;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDefragmentAllDisks;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVmFeature;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVmList;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.PowerOperation;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.GuestOperationsFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPowerStateFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.ToolsUnavailableFaultMsg;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public abstract class AbstractVmCommand extends AbstractCommandWithOptions {

    protected Boolean cbt;
    protected boolean detail;
    protected boolean force;
    protected boolean list;
    protected boolean powerOff;
    protected boolean powerOn;

    protected boolean profile;

    protected boolean reboot;
    protected boolean defragmentAllDisks;
    protected boolean remove;
    protected String execute;
    protected String password;
    protected Boolean feature;

    protected List<CoreResultActionCbt> actionCbt(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionCbt> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionCbt resultAction = new CoreResultActionCbt();
            try {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        if (fco instanceof VirtualMachineManager) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                            resultAction.setPreviousFlagState(vmm.isChangedBlockTrackingEnabled());
                            if (!getOptions().isDryRun()) {
                                if (vmm.setChangeBlockTracking(this.cbt)) {
                                    resultAction.setFlagState(this.cbt);
                                } else {
                                    resultAction.failure();
                                }
                            }
                        }
                    } catch (final Exception e) {
                        resultAction.failure(e);
                    }
                }
                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected List<CoreResultActionDefragmentAllDisks> actionDefragmentAllDisks(
            final ConnectionManager connetionManager) throws CoreResultActionException {
        final List<CoreResultActionDefragmentAllDisks> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            try (final CoreResultActionDefragmentAllDisks resultAction = new CoreResultActionDefragmentAllDisks()) {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                        if (!getOptions().isDryRun()) {
                            vmm.actionDefragmentAllDisks();
                        }
                    } catch (final InvalidPowerStateFaultMsg | FileFaultFaultMsg | InvalidStateFaultMsg
                            | RuntimeFaultFaultMsg | TaskInProgressFaultMsg e) {
                        resultAction.failure(e);
                    }
                }
                result.add(resultAction);
            }
        }
        return result;
    }

    protected List<CoreResultActionPower> actionExecute(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> result = new ArrayList<>();

        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionPower resultAction = new CoreResultActionPower();
            try {
                resultAction.start();
                resultAction
                        .setRequestedPowerOperation((this.force) ? PowerOperation.reset : PowerOperation.rebootGuest);
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof VirtualMachineManager) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());

                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;

                        if (vmm.getConfig().isTemplate()) {
                            resultAction.skip("virtual machine is a template");
                        }
                        try {
                            resultAction.setPowerState(vmm.getPowerState());
                            if (vmm.getPowerState() == FcoPowerState.poweredOn) {
                                if (getOptions().isDryRun()) {
                                } else {

                                    if (!vmm.runCommand(this.execute, this.password, vmm.isGuestWindows())) {
                                        resultAction.failure();
                                    }

                                }

                            }
                        } catch (GuestOperationsFaultFaultMsg | InvalidStateFaultMsg | RuntimeFaultFaultMsg
                                | TaskInProgressFaultMsg | IOException | InvalidTokenException | InvalidPropertyFaultMsg
                                | InvalidCollectorVersionFaultMsg | FileFaultFaultMsg
                                | SafekeepingConnectionException e) {
                            resultAction.failure(e);
                        } catch (final InterruptedException e) {
                            resultAction.failure(e);
                            this.logger.log(Level.WARNING, "Interrupted!", e);
                            // Restore interrupted state...
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        resultAction.skip("virtual machine is not powered on");
                    }
                }

                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected List<CoreResultActionVmFeature> actionFeature(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionVmFeature> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionVmFeature resultAction = new CoreResultActionVmFeature();
            try {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        if (fco instanceof VirtualMachineManager) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                            if (getOptions().isDryRun()) {
                            } else {
                                if (vmm.setFeature(this.feature)) {
                                    resultAction.setFlagState(this.feature);
                                } else {
                                    resultAction.failure();
                                }
                            }
                        }
                    } catch (final Exception e) {
                        resultAction.failure(e);
                    }
                }
                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected List<CoreResultActionVmList> actionList(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
        final List<CoreResultActionVmList> result = new ArrayList<>();

        for (final IFirstClassObject fco : targetList) {
            try (final CoreResultActionVmList resultAction = new CoreResultActionVmList()) {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof VirtualMachineManager) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                        resultAction.setVmm(vmm);
                        resultAction.getIvdList().addAll(vmm.getImprovedVirtuaDisk());
                    }
                }
                resultAction.setFcoEntityInfo(fco.getFcoInfo());
                result.add(resultAction);
            }
        }
        return result;
    }

    protected List<CoreResultActionPower> actionPowerOff(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> result = new ArrayList<>();

        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionPower resultAction = new CoreResultActionPower();
            try {
                resultAction.start();
                resultAction
                        .setRequestedPowerOperation((this.force) ? PowerOperation.powerOff : PowerOperation.shutdown);
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof VirtualMachineManager) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                        if (vmm.getConfig().isTemplate()) {
                            resultAction.skip("virtual machine is a template");
                        }
                        try {
                            resultAction.setPowerState(vmm.getPowerState());
                            if (vmm.getPowerState() == FcoPowerState.poweredOn) {

                                if (!getOptions().isDryRun()) {
                                    if (this.force) {
                                        if (!vmm.powerOff()) {
                                            resultAction.failure();
                                        }
                                    } else {
                                        vmm.shutdownGuest();
                                    }
                                }
                            } else {
                                resultAction.skip("virtual machine is already powered off");
                            }
                        } catch (InvalidStateFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
                                | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                                | CoreResultActionException | ToolsUnavailableFaultMsg e) {
                            resultAction.failure(e);
                            Utility.logWarning(this.logger, e);
                        } catch (final InterruptedException e) {
                            resultAction.failure(e);
                            this.logger.log(Level.WARNING, "Interrupted!", e);
                            // Restore interrupted state...
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected List<CoreResultActionPower> actionPowerOn(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> result = new ArrayList<>();

        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionPower resultAction = new CoreResultActionPower();
            try {
                resultAction.start();
                resultAction.setRequestedPowerOperation(PowerOperation.powereOn);
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof VirtualMachineManager) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;

                        if (vmm.getConfig().isTemplate()) {
                            resultAction.skip("virtual machine is a template");

                        }

                        try {
                            resultAction.setPowerState(vmm.getPowerState());
                            if (vmm.getPowerState() != FcoPowerState.poweredOn) {
                                if (!getOptions().isDryRun()) {
                                    vmm.powerOn();
                                }
                            } else {
                                resultAction.skip("virtual machine is already powered on");
                            }
                        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | FileFaultFaultMsg
                                | InsufficientResourcesFaultFaultMsg | InvalidStateFaultMsg | TaskInProgressFaultMsg
                                | VmConfigFaultFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException e) {
                            resultAction.failure(e);
                            Utility.logWarning(this.logger, e);
                        } catch (final InterruptedException e) {
                            resultAction.failure(e);
                            this.logger.log(Level.WARNING, "Interrupted!", e);
                            // Restore interrupted state...
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected List<CoreResultActionPower> actionReboot(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionPower> result = new ArrayList<>();

        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionPower resultAction = new CoreResultActionPower();
            try {
                resultAction.start();
                resultAction
                        .setRequestedPowerOperation((this.force) ? PowerOperation.reset : PowerOperation.rebootGuest);
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof VirtualMachineManager) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());

                        final VirtualMachineManager vmm = (VirtualMachineManager) fco;

                        if (vmm.getConfig().isTemplate()) {
                            resultAction.skip("virtual machine is a template");
                        }
                        try {
                            resultAction.setPowerState(vmm.getPowerState());
                            if (vmm.getPowerState() == FcoPowerState.poweredOn) {
                                if (!getOptions().isDryRun()) {
                                    if (this.force) {
                                        if (!vmm.reset()) {
                                            resultAction.failure();
                                        }
                                    } else {
                                        if (!vmm.rebootGuest()) {
                                            resultAction.failure();
                                        }
                                    }

                                }
                            } else {
                                resultAction.skip("virtual machine is not powered on");
                            }
                        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
                                | InvalidStateFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException e) {
                            resultAction.failure(e);
                            Utility.logWarning(this.logger, e);
                        } catch (final InterruptedException e) {
                            resultAction.failure(e);
                            this.logger.log(Level.WARNING, "Interrupted!", e);
                            // Restore interrupted state...
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected List<CoreResultActionDestroy> actionRemove(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionDestroy> result = new ArrayList<>();

        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionDestroy resultAction = new CoreResultActionDestroy();
            try {
                resultAction.start();

                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        if (fco instanceof VirtualMachineManager) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final VirtualMachineManager vmm = (VirtualMachineManager) fco;

                            if (vmm.getConfig().isTemplate()) {
                                resultAction.skip("virtual machine is a template");
                            } else {
                                resultAction.setShutdownRequired(vmm.getPowerState() == FcoPowerState.poweredOn);
                                if (resultAction.isShutdownRequired()) {
                                    if (!getOptions().isDryRun()) {
                                        if (this.force) {
                                            if (!vmm.powerOff()) {
                                                resultAction.failure("virtual machine cannot be powered off");
                                                result.add(resultAction);
                                                continue;
                                            }
                                        } else {
                                            resultAction.skip("virtual machine has to be powered off");
                                            result.add(resultAction);
                                            continue;
                                        }

                                    }
                                }
                                if (!getOptions().isDryRun()) {
                                    if (!vmm.destroy()) {
                                        resultAction.failure();
                                    }
                                }
                            }
                        }
                    } catch (final Exception e) {
                        resultAction.failure(e);
                    }
                }
                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    @Override
    protected String getLogName() {
        return "VmCommand";
    }

    @Override
    public final void initialize() {
        this.options = new CoreBasicCommandOptions();
        this.list = false;
        this.powerOn = false;
        this.powerOff = false;
        this.reboot = false;
        this.profile = false;
        this.force = false;
        this.remove = false;
        this.cbt = null;
        getOptions().setAnyFcoOfType(FirstClassObjectFilterType.vm);
        this.detail = false;
        this.execute = null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("VM : ");
        if (this.powerOn) {
            sb.append("[powerOn]");
        }
        if (this.powerOff) {
            sb.append("[powerOff]");
        }
        if (this.reboot) {
            sb.append("[reset]");
        }

        if (this.list) {
            sb.append("[list]");
        }
        if (this.force) {
            sb.append("[force]");
        }
        if (this.remove) {
            sb.append("[remove]");
        }

        sb.append(String.format("[quiet %s]", PrettyBoolean.toString(getOptions().isQuiet())));
        sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(getOptions().isDryRun())));
        return sb.toString();
    }
}
