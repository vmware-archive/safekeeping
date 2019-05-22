/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.command;

import java.util.LinkedList;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;

public class VappCommand extends CommandWithOptions {

    protected Boolean cbt;
    protected boolean detail;
    protected boolean force;
    protected boolean list;
    protected boolean powerOff;
    protected boolean powerOn;
    protected boolean remove;

    protected VappCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	if (this.cbt != null) {
	    action_Cbt(connetionManager);
	} else if (this.powerOn) {
	    action_PowerOn(connetionManager);
	} else if (this.powerOff) {
	    action_PowerOff(connetionManager);
	} else if (this.remove) {
	    action_Remove(connetionManager);
	} else if (this.list) {
	    action_List(connetionManager);
	}
	logger.exiting(getClass().getName(), "action");
    }

    private void action_Cbt(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Cbt", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vAppm = (VirtualAppManager) fco;
			++countVm;
			IoFunction.showInfo(logger, "%s cbt:%s ", vAppm.toString(),
				vAppm.isChangeTrackingEnabled() ? "enable" : "disable");
			if (!this.isDryRun()) {
			    if (vAppm.setChangeBlockTracking(this.cbt)) {
				IoFunction.showInfo(logger, "\tcbt set to %s succeeded.",
					this.cbt ? "enable" : "disable");
				++success;
			    } else {
				IoFunction.showWarning(logger, "\tcbt set to %s  failed.",
					this.cbt ? "enable" : "disable");
				++failures;
			    }
			}
			IoFunction.println();
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm, success,
		    failures, skip);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}
	logger.exiting(getClass().getName(), "action_Cbt");
    }

    private void action_List(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_List", connetionManager);
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    IoFunction.showInfo(logger, VirtualMachineManager.headerToString() + "%-5s    %-11s  %s", " cbt  ", "state",
		    "vm(s)");

	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vAppm = (VirtualAppManager) fco;
			final StringBuilder vmListStr = new StringBuilder();
			IoFunction.showInfo(logger, "%s%-5b    %-11s  %d", vAppm.toString(),
				vAppm.isChangeTrackingEnabled(),

				vAppm.getPowerState().toString().toLowerCase().replace('_', ' '),
				vAppm.getVmList().size());
			if (this.detail) {
			    for (final VirtualMachineManager vm : vAppm.getVmList()) {
				vmListStr.append("\t\t");
				vmListStr.append(vm.toString());
				vmListStr.append('\n');

			    }
			    IoFunction.showInfo(logger, "\t\tAssociated Virtual Machine(s)\n%s", vmListStr.toString());
			}
		    }
		}
	    }
	} else {
	    IoFunction.showInfo(logger, "No Virtual Machines");
	}
	logger.exiting(getClass().getName(), "action_List");
    }

    private void action_PowerOff(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_PowerOff", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vAppm = (VirtualAppManager) fco;
			++countVm;

			if (!this.isDryRun()) {
			    if (!this.isQuiet()) {
				if (!IoFunction.confirmOperation("Shutdown %s ", vAppm.toString())) {
				    ++skip;
				    continue;
				}
			    }
			    if (vAppm.powerOff(this.force)) {
				IoFunction.showInfo(logger, "%s: virtual machine powered Off", vAppm.getName());
				++success;
			    } else {
				IoFunction.showWarning(logger, "%s: virtual machine cannot be powered Off",
					vAppm.getName());
				++failures;
			    }
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vapp:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm,
		    success, failures, skip);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}
	logger.exiting(getClass().getName(), "action_PowerOff");
    }

    private void action_PowerOn(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_PowerOn", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vAppm = (VirtualAppManager) fco;
			++countVm;

			if (!this.isDryRun()) {
			    if (vAppm.powerOn()) {
				IoFunction.showInfo(logger, "PowerOn Vm %s succeeded.", vAppm.getName());
				++success;
			    } else {
				IoFunction.showInfo(logger, "PowerOn Vm %s failed.", vAppm.getName());
				++failures;
			    }
			}

		    }
		}
	    }
	    IoFunction.printTotal("%d vapp:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm,
		    success, failures, skip);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}

	logger.exiting(getClass().getName(), "action_PowerOn");
    }

    private void action_Remove(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Remove", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualAppManager) {
			final VirtualAppManager vAppm = (VirtualAppManager) fco;
			++countVm;

			if (vAppm.getPowerState() != VirtualMachinePowerState.POWERED_OFF) {
			    IoFunction.showWarning(logger, "%s: virtual machine is not powered off", vAppm.getName());
			    if (!this.isDryRun()) {
				if (this.force) {
				    if (vAppm.powerOff(this.force)) {
					IoFunction.showInfo(logger, "%s: virtualApp powered off", vAppm.getName());
				    } else {
					IoFunction.showWarning(logger, "%s: virtualApp cannot be powerd off",
						vAppm.getName());
					IoFunction.showWarning(logger, "%s: virtualApp cannot be deleted",
						vAppm.getName());
					++failures;
					continue;
				    }
				}
			    }
			}
			if (!this.isDryRun()) {
			    if (!this.isQuiet()) {
				if (!IoFunction.confirmOperation("Delete vm: %s ", vAppm.toString())) {
				    ++skip;
				    continue;
				}
			    }
			    if (vAppm.destroy()) {
				IoFunction.showInfo(logger, "%s: virtualApp deleted", vAppm.getName());
				++success;
			    } else {
				IoFunction.showWarning(logger, "%s: virtualApp cannot be deleted", vAppm.getName());
				++failures;
			    }
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vapp:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm,
		    success, failures, skip);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}
	logger.exiting(getClass().getName(), "action_Remove");
    }

    @Override
    public void initialize() {
	super.initialize();
	this.list = false;
	this.powerOn = false;
	this.powerOff = false;
	this.list = false;
	this.force = false;
	this.remove = false;
	this.cbt = null;
	this.anyFcoOfType = FirstClassObjectFilterType.vapp;
	this.detail = false;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("VM : ");
	if (this.powerOn) {
	    sb.append("[powerOn]");
	}
	if (this.powerOff) {
	    sb.append("[powerOff]");
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

	sb.append(String.format("[quiet %s]", PrettyBoolean.toString(this.isQuiet())));
	sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(this.isDryRun())));
	return sb.toString();
    }
}
