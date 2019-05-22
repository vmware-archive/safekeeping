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

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vmware.vim25.ID;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GenerationProfileSpec;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.FirstClassObjectFilterType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.type.VmdkInfo;
import com.vmware.vmbk.util.Utility;

public class VmCommand extends CommandWithOptions {

    protected Boolean cbt;
    protected boolean detail;
    protected boolean force;
    protected boolean list;
    protected boolean powerOff;
    protected boolean powerOn;
    protected boolean profile;
    protected boolean reboot;
    protected boolean remove;

    protected VmCommand() {
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
	} else if (this.reboot) {
	    action_Reboot(connetionManager);
	} else if (this.remove) {
	    action_Remove(connetionManager);
	} else if (this.list) {
	    action_List(connetionManager);
	} else if (this.profile) {
	    action_Profile(connetionManager, vmbk.getRepositoryTarget());
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
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			++countVm;
			IoFunction.showInfo(logger, "%s cbt:%s ", vmm.toString(),
				vmm.getConfig().isChangeTrackingEnabled() ? "enable" : "disable");
			if (!this.isDryRun()) {
			    if (vmm.setChangeBlockTracking(this.cbt)) {
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
	final LinkedList<ImprovedVirtuaDisk> allIvd = (this.detail) ? connetionManager.getAllIvdList() : null;
	if (targetList.size() > 0) {
	    IoFunction.showInfo(logger,
		    VirtualMachineManager.headerToString() + "%-5s    %-5s   %-5s   %-5s    %-11s     %s       %s",
		    " cbt  ", "snapshot", "template", "encrypt", "state", "ivd", "datastore");

	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			final Map<Integer, ID> vStorage = vmm.getVStorageObjectAssociations();
			final boolean useIvd = ((vStorage != null) && (vStorage.size() > 0));
			final StringBuilder vmDiskStr = new StringBuilder();
			IoFunction.showInfo(logger, "%s%-5b    %-5b      %-5b      %-5b      %-11s     %-5b     %s",
				vmm.toString(), vmm.getConfig().isChangeTrackingEnabled(),
				vmm.getCurrentSnapshot() != null, vmm.getConfig().isTemplate(),
				vmm.isConfigurationEncrypted(),
				vmm.getRuntimeInfo().getPowerState().toString().toLowerCase().replace('_', ' '), useIvd,
				vmm.getDatastoreInfo().getName());
			if (useIvd && this.detail) {
			    for (final ImprovedVirtuaDisk ivd : allIvd) {
				for (final ID id : vStorage.values()) {
				    if (ivd.getId().getId().equals(id.getId())) {
					vmDiskStr.append(ivd.toString());
					vmDiskStr.append('\n');
				    }
				}
			    }
			    IoFunction.showInfo(logger, "\t\tAssociated Improved Virtual Disk\n\t\t%s",
				    vmDiskStr.toString());
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
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			++countVm;
			if (vmm.getConfig().isTemplate()) {
			    IoFunction.showInfo(logger, "virtual machine %s is a template - Skip", vmm.getName());
			    ++skip;
			    continue;
			}
			final VirtualMachineRuntimeInfo runtimeInfo = vmm.getRuntimeInfo();
			if (runtimeInfo.getPowerState() != VirtualMachinePowerState.POWERED_OFF) {

			    if (!this.isDryRun()) {
				if (this.force) {
				    if (vmm.powerOff()) {
					IoFunction.showInfo(logger, "%s: virtual machine powered Off", vmm.getName());
					++success;
				    } else {
					IoFunction.showWarning(logger, "%s: virtual machine cannot be powered Off",
						vmm.getName());
					++failures;
				    }
				} else {
				    if (!this.isQuiet()) {
					if (!IoFunction.confirmOperation("Shutdown %s ", vmm.toString())) {
					    ++skip;
					    continue;
					}
				    }
				    if (vmm.shutdownGuest()) {
					IoFunction.showInfo(logger, "%s: virtual machine shutdown in progress",
						vmm.getName());
					++success;
				    } else {
					IoFunction.showWarning(logger, "%s: virtual machine cannot be shutdown ",
						vmm.getName());
					++failures;
				    }
				}

			    }
			} else {
			    IoFunction.showWarning(logger, "%s: virtual machine is already powered off", vmm.getName());
			    ++skip;
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm, success,
		    failures, skip);
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
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			++countVm;
			if (vmm.getConfig().isTemplate()) {
			    IoFunction.showInfo(logger, "virtual machine %s is a template - Skip", vmm.getName());
			    ++skip;
			    continue;
			}
			final VirtualMachineRuntimeInfo runtimeInfo = vmm.getRuntimeInfo();
			if (runtimeInfo.getPowerState() != VirtualMachinePowerState.POWERED_ON) {
			    if (!this.isDryRun()) {
				if (vmm.powerOn(null)) {
				    IoFunction.showInfo(logger, "PowerOn Vm %s succeeded.", vmm.getName());
				    ++success;
				} else {
				    IoFunction.showInfo(logger, "PowerOn Vm %s failed.", vmm.getName());
				    ++failures;
				}
			    }
			} else {
			    IoFunction.showWarning(logger, "%s: virtual machine is already powered on", vmm.getName());
			    ++skip;
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm, success,
		    failures, skip);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}

	logger.exiting(getClass().getName(), "action_PowerOn");
    }

    private void action_Profile(final ConnectionManager connetionManager, final ITarget repositoryTarget) {
	logger.entering(getClass().getName(), "action_Profile", new Object[] { connetionManager, repositoryTarget });
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    for (final FirstClassObject fco : targetList) {
		if (fco instanceof VirtualMachineManager) {
		    final VirtualMachineManager vmm = (VirtualMachineManager) fco;

		    try {
			final FcoArchiveManager vmArcMgr = new FcoArchiveManager(vmm, repositoryTarget,
				ArchiveManagerMode.temporary);
			// final SnapshotManager snap = vmm.getSnapshotManager();
			final List<VmdkInfo> vmdkInfoList = vmm.getConfig()
				.getAllVmdkInfo(vmm.getVStorageObjectAssociations());
			final Calendar cal = Calendar.getInstance();
			final GenerationProfileSpec spec = new GenerationProfileSpec(vmm, null);
			spec.setCalendar(cal);
			spec.getVmdkInfoList().addAll(vmdkInfoList);
			// final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(vmm,
			// vmdkInfoList, cal, null);
			final GenerationProfile profGen = vmArcMgr.prepareNewGeneration(spec);
			final String out = new String(profGen.toByteArrayInOutputStream().toByteArray());
			IoFunction.showInfo(logger, out);
		    } catch (final Exception e) {
			logger.warning(Utility.toString(e));
		    }
		}
	    }
	} else {
	    IoFunction.showInfo(logger, "No Virtual Machines");
	}
	logger.exiting(getClass().getName(), "action_Profile");
    }

    private void action_Reboot(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Reboot", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	final int countVm = 0;
	final LinkedList<FirstClassObject> targetList = getFcoTarget(connetionManager, this.anyFcoOfType);
	if (targetList.size() > 0) {
	    for (final FirstClassObject fco : targetList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			if (vmm.getConfig().isTemplate()) {
			    IoFunction.showInfo(logger, "virtual machine %s is a template - Skip", vmm.getName());
			    ++skip;
			    continue;
			}
			final VirtualMachineRuntimeInfo runtimeInfo = vmm.getRuntimeInfo();
			if (runtimeInfo.getPowerState() == VirtualMachinePowerState.POWERED_ON) {
			    if (!this.isDryRun()) {
				if (this.force) {
				    if (vmm.reset()) {
					IoFunction.showInfo(logger, "%s: virtual machine Reset", vmm.getName());
					++success;
				    } else {
					IoFunction.showWarning(logger, "%s: virtual machine cannot be Reset",
						vmm.getName());
					++failures;
				    }
				} else {
				    if (!this.isQuiet()) {
					if (!IoFunction.confirmOperation("Reboot vm: %s ", vmm.toString())) {
					    ++skip;
					    continue;
					}
				    }
				    if (vmm.rebootGuest()) {
					IoFunction.showInfo(logger, "%s: virtual machine reboot initiated",
						vmm.getName());
					++success;
				    } else {
					IoFunction.showWarning(logger, "%s: virtual machine cannot be rebooted",
						vmm.getName());
					++failures;
				    }
				}
			    }
			} else {
			    IoFunction.showWarning(logger, "%s(%s): virtual machine is not powered on", vmm.getName(),
				    runtimeInfo.getPowerState().toString());
			    ++skip;
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm, success,
		    failures, skip);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}
	logger.exiting(getClass().getName(), "action_Reboot");
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
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			++countVm;
			final VirtualMachineRuntimeInfo runtimeInfo = vmm.getRuntimeInfo();
			if (runtimeInfo.getPowerState() != VirtualMachinePowerState.POWERED_OFF) {
			    IoFunction.showWarning(logger, "%s: virtual machine is not powered off", vmm.getName());
			    if (!this.isDryRun()) {
				if (this.force) {
				    if (vmm.powerOff()) {
					IoFunction.showInfo(logger, "%s: virtual machine powered off", vmm.getName());
				    } else {
					IoFunction.showWarning(logger, "%s: virtual machine cannot be powerd off",
						vmm.getName());
					IoFunction.showWarning(logger, "%s: virtual machine cannot be deleted",
						vmm.getName());
					++failures;
					continue;
				    }
				}
			    }
			}
			if (!this.isDryRun()) {
			    if (!this.isQuiet()) {
				if (!IoFunction.confirmOperation("Delete vm: %s ", vmm.toString())) {
				    ++skip;
				    continue;
				}
			    }
			    if (vmm.destroy()) {
				IoFunction.showInfo(logger, "%s: virtual machine deleted", vmm.getName());
				++success;
			    } else {
				IoFunction.showWarning(logger, "%s: virtual machine cannot be deleted", vmm.getName());
				++failures;
			    }
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d  Success:%d fails:%d skip:%d", success + failures + skip, countVm, success,
		    failures, skip);
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
	this.reboot = false;
	this.list = false;
	this.profile = false;
	this.force = false;
	this.remove = false;
	this.cbt = null;
	this.anyFcoOfType = FirstClassObjectFilterType.vm;
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

	sb.append(String.format("[quiet %s]", PrettyBoolean.toString(this.isQuiet())));
	sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(this.isDryRun())));
	return sb.toString();
    }
}
