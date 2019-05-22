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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.VStorageObjectSnapshotInfo;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.SnapshotManager;
import com.vmware.vmbk.type.VirtualMachineManager;

public class SnapCommand extends CommandSupportForSnapshot {

    protected boolean create;
    protected List<String> delete;
    protected boolean deleteAll;
    protected String name;
    protected boolean list;
    protected boolean revert;

    protected SnapCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (!connetionManager.isConnected()) {
	    connetionManager.connect();
	}
	if (this.create) {
	    action_Create(connetionManager);
	} else if ((this.delete != null) || this.deleteAll) {
	    action_Delete(connetionManager);
	} else if (this.list) {
	    action_List(connetionManager);
	} else if (this.revert) {
	    action_List(connetionManager);
	}
	logger.exiting(getClass().getName(), "action");
    }

    private void action_Create(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Create", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	int countIvd = 0;

	final LinkedList<FirstClassObject> snapVmList = getFcoTarget(connetionManager, this.anyFcoOfType);

	if (snapVmList.size() > 0) {

	    for (final FirstClassObject fco : snapVmList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;

			++countVm;

			IoFunction.showInfo(logger, vmm.headertoString());
			if (vmm.getConfig().isTemplate()) {
			    IoFunction.showInfo(logger, "%s\t%s\t%s skipped. VM is a template.", vmm.getUuid(),
				    vmm.getMorefValue(), vmm.getName());
			    ++skip;
			} else {

			    IoFunction.showInfo(logger, vmm.toString());
			    final SnapshotManager snap;
			    if (StringUtils.isEmpty(this.name)) {
				final Calendar cal = Calendar.getInstance();
				IoFunction.showInfo(logger, "Starting creation of snapshot %s",
					generateSnapshotName(cal));
				snap = createSnapshot(vmm, cal);

			    } else {
				IoFunction.showInfo(logger, "Starting creation of snapshot %s", this.name);
				snap = createSnapshot(vmm, this.name);
			    }

			    if (snap == null) {

				++failures;
			    } else {
				++success;
			    }
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			++countIvd;

			IoFunction.showInfo(logger, "\n%-36s\t%s", "uuid", "name");
			IoFunction.showInfo(logger, "%s\t%s ", ivd.getUuid(), ivd.getName());
			/*
			 * Create snapshot
			 */
			final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap;
			if (StringUtils.isEmpty(this.name)) {
			    final Calendar cal = Calendar.getInstance();
			    IoFunction.showInfo(logger, "Starting creation of snapshot %s", generateSnapshotName(cal));
			    snap = createSnapshot(ivd, cal);

			} else {
			    IoFunction.showInfo(logger, "Starting creation of snapshot %s", this.name);
			    snap = createSnapshot(ivd, this.name);
			}

			if (snap == null) {
			    ++failures;
			} else {
			    ++success;
			}

		    }
		}
	    }
	    IoFunction.println();
	    IoFunction.printTotal("%d vm:%d ivd:%d Success:%d fails:%d skip:%d", success + failures + skip, countVm,
		    countIvd, success, failures, skip);
	    IoFunction.println();
	} else {
	    IoFunction.showWarning(logger, "No valid targets");

	}
	logger.exiting(getClass().getName(), "action_Create");
    }

    private void action_Delete(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_Delete", connetionManager);
	int success = 0;
	int failures = 0;
	int skip = 0;
	int countVm = 0;
	int countIvd = 0;

	final LinkedList<FirstClassObject> snapVmList = getFcoTarget(connetionManager, this.anyFcoOfType);

	if (snapVmList.size() > 0) {

	    for (final FirstClassObject fco : snapVmList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		    ++skip;
		} else {
		    if (fco instanceof VirtualMachineManager) {
			++countVm;
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;
			if (vmm.getSnapInfo() != null) {
			    IoFunction.println();
			    IoFunction.showInfo(logger, vmm.headertoString());
			    IoFunction.showInfo(logger, vmm.toString());
			    IoFunction.println(" Snapshot:");
			    final ArrayList<VirtualMachineSnapshotTree> snapList = SnapshotsList(
				    vmm.getSnapInfo().getRootSnapshotList());
			    for (final VirtualMachineSnapshotTree snTree : snapList) {
				IoFunction.showInfo(logger, "\t%s\t%d\t%s\t%s\t%s", snTree.getName(), snTree.getId(),
					snTree.getState(), snTree.getCreateTime().toString(), snTree.getDescription());
			    }
			    if (!this.isQuiet()) {
				if (!IoFunction
					.confirmOperation("delete snapshots: " + StringUtils.join(this.delete, ","))) {
				    ++skip;
				    continue;
				}
			    }
			    if (this.deleteAll) {
				for (final VirtualMachineSnapshotTree snTree : vmm.getSnapInfo()
					.getRootSnapshotList()) {
				    LoggerUtils.logInfo(logger, "%s\t%d\t%s\t%s\t%s", snTree.getName(), snTree.getId(),
					    snTree.getState(), snTree.getCreateTime().toString(),
					    snTree.getDescription());

				    if (vmm.getConfig().isTemplate()) {
					IoFunction.showInfo(logger, "\t%s\t%d\t%s\t%s\t%s skipped. VM is a template.",
						snTree.getName(), snTree.getId(), snTree.getState(),
						snTree.getCreateTime().toString(), snTree.getDescription());
					++skip;

				    } else if (vmm.deleteSnapshot(snTree.getSnapshot(), true, true)) {
					IoFunction.showInfo(logger, "\t%s\t%d\t%s\t%s\t%s  delete succeed.",
						snTree.getName(), snTree.getId(), snTree.getState(),
						snTree.getCreateTime().toString(), snTree.getDescription());
					++success;
				    } else {
					IoFunction.showWarning(logger, "\t%s\t%d\t%s\t%s\t%s  delete failed.",
						snTree.getName(), snTree.getId(), snTree.getState(),
						snTree.getCreateTime().toString(), snTree.getDescription());
					++failures;
				    }
				}
			    } else {

				for (final String sn : this.delete) {
				    for (final VirtualMachineSnapshotTree snTree : snapList) {
					boolean compare = false;
					if (StringUtils.isNumeric(sn)) {
					    compare = Integer.valueOf(sn) == snTree.getId();
					}
					if (!compare) {
					    compare = sn.equalsIgnoreCase(snTree.getName());
					}
					if (compare) {
					    ++countVm;
					    if (vmm.getConfig().isTemplate()) {
						IoFunction.showInfo(logger,
							"\t%s\t%d\t%s\t%s\t%s skipped. VM is a template.",
							snTree.getName(), snTree.getId(), snTree.getState(),
							snTree.getCreateTime().toString(), snTree.getDescription());
						++skip;

					    } else if (vmm.deleteSnapshot(snTree.getSnapshot(), false, true)) {
						IoFunction.showInfo(logger, "\t%s\t%d\t%s\t%s\t%s  delete succeed.",
							snTree.getName(), snTree.getId(), snTree.getState(),
							snTree.getCreateTime().toString(), snTree.getDescription());
						++success;
					    } else {
						IoFunction.showWarning(logger, "\t%s\t%d\t%s\t%s\t%s  delete failed.",
							snTree.getName(), snTree.getId(), snTree.getState(),
							snTree.getCreateTime().toString(), snTree.getDescription());
						++failures;
					    }
					    break;
					}
				    }
				}
			    }
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			++countIvd;

			final VStorageObjectSnapshotInfo currentSnap = ivd.getSnapshots();
			if ((currentSnap.getSnapshots() != null) && (currentSnap.getSnapshots().size() > 0)) {
			    IoFunction.showInfo(logger, " %-36s\t%s", "uuid", "name");
			    IoFunction.showInfo(logger, " %s\t%s ", ivd.getUuid(), ivd.getName());
			    IoFunction.println(" Snapshot:");
			    for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snTree : currentSnap
				    .getSnapshots()) {
				IoFunction.showInfo(logger, "Snapshot: %s\t%s\t%s delete succeed. ",
					snTree.getId().getId(), snTree.getCreateTime().toString(),
					snTree.getDescription());
			    }
			    if (!this.isQuiet()) {

				if (!IoFunction.confirmOperation("Delete snapshots")) {
				    ++skip;
				    continue;
				}
			    }

			    for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snTree : currentSnap
				    .getSnapshots()) {
				LoggerUtils.logInfo(logger, "\t%s\t%s\t%s ", snTree.getId().getId(),
					snTree.getCreateTime().toString(), snTree.getDescription());
				if (this.deleteAll) {
				    ++countIvd;
				    if (ivd.deleteSnapshot(snTree)) {
					IoFunction.showInfo(logger, "\t%s\t%s\t%s delete succeed. ",
						snTree.getId().getId(), snTree.getCreateTime().toString(),
						snTree.getDescription());

					++success;
				    } else {
					IoFunction.showInfo(logger, "\t%s\t%s\t%s delete failed. ",
						snTree.getId().getId(), snTree.getCreateTime().toString(),
						snTree.getDescription());
					++failures;
				    }
				} else {
				    for (final String sn : this.delete) {
					boolean compare = false;
					if (CommandWithOptions.UUIDPATTERN.matcher(sn).matches()) {
					    compare = sn.equalsIgnoreCase(snTree.getId().getId());
					}

					if (!compare) {
					    compare = sn.equalsIgnoreCase(snTree.getDescription());
					}
					if (compare) {
					    ++countIvd;
					    if (ivd.deleteSnapshot(snTree)) {
						IoFunction.showInfo(logger, "Snapshot: %s\t%s\t%s delete succeed. ",
							snTree.getId().getId(), snTree.getCreateTime().toString(),
							snTree.getDescription());

						++success;
					    } else {
						IoFunction.showInfo(logger, "Snapshot: %s\t%s\t%s delete failed. ",
							snTree.getId().getId(), snTree.getCreateTime().toString(),
							snTree.getDescription());
						++failures;
					    }

					}
				    }
				}
			    }
			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d ivd:%d Success:%d fails:%d skip:%d", success + failures + skip, countVm,
		    countIvd, success, failures, skip);
	} else {
	    IoFunction.showWarning(logger, "No valid targets");
	}
	logger.exiting(getClass().getName(), "action_Delete");
    }

    private void action_List(final ConnectionManager connetionManager) {
	logger.entering(getClass().getName(), "action_List", connetionManager);

	int countVm = 0;
	int countIvd = 0;
	final LinkedList<FirstClassObject> snapVmList = getFcoTarget(connetionManager, this.anyFcoOfType);

	if (snapVmList.size() > 0) {

	    for (final FirstClassObject fco : snapVmList) {
		if (Vmbk.isAbortTriggered()) {
		    IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		} else {
		    if (fco instanceof VirtualMachineManager) {
			final VirtualMachineManager vmm = (VirtualMachineManager) fco;

			if (vmm.getSnapInfo() != null) {
			    ++countVm;
			    IoFunction.println();
			    IoFunction.showInfo(logger, vmm.headertoString());
			    IoFunction.showInfo(logger, vmm.toString());
			    IoFunction.println(" Snapshot:");
			    listSnapshots(vmm.getSnapInfo().getRootSnapshotList());
			}
		    } else if (fco instanceof ImprovedVirtuaDisk) {
			final ImprovedVirtuaDisk ivd = (ImprovedVirtuaDisk) fco;
			++countIvd;

			final VStorageObjectSnapshotInfo currentSnap = ivd.getSnapshots();
			if (currentSnap != null) {
			    if ((currentSnap.getSnapshots() != null) && (currentSnap.getSnapshots().size() > 0)) {

				IoFunction.println();
				IoFunction.showInfo(logger, ivd.headertoString());
				IoFunction.showInfo(logger, ivd.toString());

				IoFunction.println(" Snapshot:");
				for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snTree : currentSnap
					.getSnapshots()) {
				    IoFunction.showInfo(logger, "\t%s\t%s\t%s ", snTree.getId().getId(),
					    snTree.getCreateTime().toString(), snTree.getDescription());
				}
			    }
			} else {
			    IoFunction.println();
			    IoFunction.showWarning(logger, ivd.headertoString());
			    IoFunction.showWarning(logger, ivd.toString());
			    IoFunction.showWarning(logger, "Cannot retrieve snapshots");

			}
		    }
		}
	    }
	    IoFunction.printTotal("%d vm:%d ivd:%d", countVm + countIvd, countVm, countIvd);
	} else {
	    IoFunction.showInfo(logger, "No target specified.");
	}
	logger.exiting(getClass().getName(), "action_List");
    }

    @Override
    public void initialize() {
	super.initialize();
	this.list = false;
	this.create = false;
	this.delete = null;
	this.deleteAll = false;
	this.revert = false;
	this.name = null;
    }

    private void listSnapshots(final List<VirtualMachineSnapshotTree> snTreeList) {
	logger.entering(getClass().getName(), "listSnapshots", snTreeList);
	for (final VirtualMachineSnapshotTree snTree : snTreeList) {
	    IoFunction.showInfo(logger, "\t%s\t%d\t\t%s\t%s\t%s", snTree.getName(), snTree.getId(), snTree.getState(),
		    snTree.getCreateTime().toString(), snTree.getDescription());
	    if (snTree.getChildSnapshotList() != null) {
		listSnapshots(snTree.getChildSnapshotList());

	    }
	}
	logger.exiting(getClass().getName(), "listSnapshots");
    }

    private ArrayList<VirtualMachineSnapshotTree> SnapshotsList(final List<VirtualMachineSnapshotTree> snTreeList) {
	logger.entering(getClass().getName(), "SnapshotsList", snTreeList);
	final ArrayList<VirtualMachineSnapshotTree> result = new ArrayList<>();

	for (final VirtualMachineSnapshotTree snTree : snTreeList) {
	    result.add(snTree);
	    if (snTree.getChildSnapshotList() != null) {
		result.addAll(SnapshotsList(snTree.getChildSnapshotList()));
	    }
	}
	logger.exiting(getClass().getName(), "SnapshotsList", result);
	return result;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("snap: ");
	sb.append(String.format("[list %s]", PrettyBoolean.toString(this.list)));

	return sb.toString();
    }

}
