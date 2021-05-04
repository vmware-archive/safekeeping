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
package com.vmware.safekeeping.core.ext.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.ICommandSupportForSnapshot;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.command.results.support.SnapshotInfo;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionListSnap;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObjectSnapshotInfo;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VirtualMachineSnapshotTree;

public abstract class AbstractSnapshotCommand extends AbstractCommandWithOptions implements ICommandSupportForSnapshot {

	protected boolean create;
	protected List<String> delete;
	protected boolean deleteAll;
	protected String name;
	protected boolean list;
	protected boolean revert;

	private final RunningReport snapReport;

	protected AbstractSnapshotCommand(final RunningReport snapReport) {
		this.snapReport = snapReport;
	}

	protected List<CoreResultActionCreateSnap> actionCreate(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionCreateSnap> result = new ArrayList<>();

		final List<IFirstClassObject> snapVmList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : snapVmList) {
			final CoreResultActionCreateSnap resultAction = new CoreResultActionCreateSnap();
			try {
				resultAction.start();
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted("Aborted on user request");

					result.clear();
					result.add(resultAction);
					break;
				} else {
					try {
						resultAction.setFcoEntityInfo(fco.getFcoInfo());
						if (fco instanceof VirtualMachineManager) {
							final VirtualMachineManager vmm = (VirtualMachineManager) fco;

							if (vmm.getConfig().isTemplate()) {
								resultAction.skip("virtual machine is a template");
							} else {
								final SnapshotManager snap;
								if (StringUtils.isEmpty(this.name)) {
									final Calendar cal = Calendar.getInstance();
									snap = createSnapshot(vmm, cal, this.snapReport);
								} else {
									snap = createSnapshot(vmm, this.name, this.snapReport);
								}
								if (snap == null) {
									resultAction.failure();
								} else {
									resultAction.setSnapMoref(snap.getMoref().getValue());
									resultAction.setSnapName(snap.getName());
								}

							}
						} else if (fco instanceof ImprovedVirtualDisk) {
							final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;

							/*
							 * Create snapshot
							 */
							final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap;
							if (StringUtils.isEmpty(this.name)) {
								final Calendar cal = Calendar.getInstance();
								snap = createSnapshot(ivd, cal, this.snapReport);
							} else {
								snap = createSnapshot(ivd, this.name, this.snapReport);
							}

							if (snap == null) {
								resultAction.failure();
							} else {
								resultAction.setSnapId(snap.getId().getId());
								resultAction.setSnapName(snap.getDescription());

							}

						}
					} catch (final Exception e) {
						resultAction.failure();
						resultAction.setReason(e.getMessage());
					}
				}
				result.add(resultAction);
			} finally {
				resultAction.done();
			}
		}
		return result;
	}

	protected List<CoreResultActionDeleteSnap> actionDelete(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionDeleteSnap> result = new ArrayList<>();
		final List<IFirstClassObject> snapVmList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
		for (final IFirstClassObject fco : snapVmList) {
			final CoreResultActionDeleteSnap resultAction = new CoreResultActionDeleteSnap();
			try {
				resultAction.start();
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted();
					result.clear();
					result.add(resultAction);
					break;
				} else {
					resultAction.setFcoEntityInfo(fco.getFcoInfo());
					try {
						if (fco instanceof VirtualMachineManager) {
							final VirtualMachineManager vmm = (VirtualMachineManager) fco;
							if (vmm.getConfig().isTemplate()) {
								resultAction.skip("virtual machine is a template");
							} else if (vmm.getSnapInfo() == null) {
								resultAction.skip("virtual machine has no snapshots");
							} else {
								final ArrayList<VirtualMachineSnapshotTree> snapList = SnapshotsList(
										vmm.getSnapInfo().getRootSnapshotList());
								if (!getOptions().isDryRun()) {
									if (this.deleteAll) {
										for (final VirtualMachineSnapshotTree snTree : snapList) {
											final SnapshotInfo snapInfo = new SnapshotInfo();
											snapInfo.setCreateTime(snTree.getCreateTime());
											snapInfo.setDescription(snTree.getDescription());
											snapInfo.setId(snTree.getId().toString());
											snapInfo.setName(snTree.getName());
											switch (snTree.getState()) {
											case POWERED_OFF:
												snapInfo.setState(FcoPowerState.poweredOff);
												break;
											case POWERED_ON:
												snapInfo.setState(FcoPowerState.poweredOn);
												break;
											case SUSPENDED:
												snapInfo.setState(FcoPowerState.suspended);
												break;
											}
											resultAction.getSnapList().add(snapInfo);
										}
										if (!vmm.deleteSnapshot(snapList.get(0).getSnapshot(), true, true,
												this.snapReport)) {
											resultAction.failure();
										}
										for (final SnapshotInfo snapInfo : resultAction.getSnapList()) {
											snapInfo.setResult(resultAction.getState());
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
													final SnapshotInfo snapInfo = new SnapshotInfo();
													snapInfo.setCreateTime(snTree.getCreateTime());
													snapInfo.setDescription(snTree.getDescription());
													snapInfo.setId(snTree.getId().toString());
													snapInfo.setName(snTree.getName());
													switch (snTree.getState()) {
													case POWERED_OFF:
														snapInfo.setState(FcoPowerState.poweredOff);
														break;
													case POWERED_ON:
														snapInfo.setState(FcoPowerState.poweredOn);
														break;
													case SUSPENDED:
														snapInfo.setState(FcoPowerState.suspended);
														break;
													}
													resultAction.getSnapList().add(snapInfo);

													if (vmm.deleteSnapshot(snTree.getSnapshot(), false, true,
															this.snapReport)) {
														snapInfo.setResult(OperationState.SUCCESS);

													} else {
														snapInfo.setResult(OperationState.FAILED);
														resultAction.failure();
													}
												}
											}
										}
									}
								}
							}
						} else if (fco instanceof ImprovedVirtualDisk) {
							final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
							final VStorageObjectSnapshotInfo currentSnap = ivd.getSnapshots();
							if ((currentSnap.getSnapshots() == null) || (currentSnap.getSnapshots().size() == 0)) {
								resultAction.skip("Improved Virtual Disk has no snapshots");
								continue;
							} else {
								if (getOptions().isDryRun()) {
									// resultAction.dryruns();
								} else {
									if (this.deleteAll) {
										for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snTree : currentSnap
												.getSnapshots()) {
											final SnapshotInfo snapInfo = new SnapshotInfo();
											snapInfo.setCreateTime(snapInfo.getCreateTime());
											snapInfo.setDescription(snTree.getDescription());
											snapInfo.setId(snTree.getId().toString());
											if (ivd.deleteSnapshot(snTree, this.snapReport)) {
												snapInfo.setResult(OperationState.SUCCESS);
											} else {
												resultAction.failure();
												snapInfo.setResult(OperationState.FAILED);
											}
											resultAction.getSnapList().add(snapInfo);
										}
									} else {
										for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snTree : currentSnap
												.getSnapshots()) {
											for (final String sn : this.delete) {
												boolean compare = false;
												if (AbstractCommandWithOptions.UUIDPATTERN.matcher(sn).matches()) {
													compare = sn.equalsIgnoreCase(snTree.getId().getId());
												}

												if (!compare) {
													compare = sn.equalsIgnoreCase(snTree.getDescription());
												}
												if (compare) {
													final SnapshotInfo snapInfo = new SnapshotInfo();
													snapInfo.setCreateTime(snapInfo.getCreateTime());
													snapInfo.setDescription(snTree.getDescription());
													snapInfo.setId(snTree.getId().toString());
													if (ivd.deleteSnapshot(snTree, this.snapReport)) {
														snapInfo.setResult(OperationState.SUCCESS);
													} else {
														resultAction.failure();
														snapInfo.setResult(OperationState.FAILED);
													}
													resultAction.getSnapList().add(snapInfo);
												}
											}
										}
									}
								}
							}
						}

					} catch (final Exception e) {
						Utility.logWarning(this.logger, e);
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

	protected List<CoreResultActionListSnap> actionList(final ConnectionManager connetionManager)
			throws CoreResultActionException {
		final List<CoreResultActionListSnap> result = new ArrayList<>();
		final List<IFirstClassObject> snapVmList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

		for (final IFirstClassObject fco : snapVmList) {
			try (final CoreResultActionListSnap resultAction = new CoreResultActionListSnap()) {
				resultAction.start();
				if (Vmbk.isAbortTriggered()) {
					resultAction.aborted();
					result.clear();
					result.add(resultAction);
					break;
				} else {
					resultAction.setFcoEntityInfo(fco.getFcoInfo());

					try {
						if (fco instanceof VirtualAppManager) {
							final VirtualAppManager vapp = (VirtualAppManager) fco;
							for (final VirtualMachineManager vmm : vapp.getVmList()) {
								resultAction.getSnapList().addAll(getVmSnapShots(vmm));
							}
						} else if (fco instanceof VirtualMachineManager) {
							resultAction.getSnapList().addAll(getVmSnapShots((VirtualMachineManager) fco));
//			    final VirtualMachineManager vmm = (VirtualMachineManager) fco;
//			    if (vmm.getSnapInfo() != null) {
//				final ArrayList<VirtualMachineSnapshotTree> snapList = SnapshotsList(
//					vmm.getSnapInfo().getRootSnapshotList());
//				for (final VirtualMachineSnapshotTree snTree : snapList) {
//				    final SnapshotInfo snapInfo = new SnapshotInfo();
//				    snapInfo.setCreateTime(snTree.getCreateTime());
//				    snapInfo.setDescription(snTree.getDescription());
//				    snapInfo.setId(snTree.getId().toString());
//				    snapInfo.setName(snTree.getName());
//				    switch (snTree.getState()) {
//				    case POWERED_OFF:
//					snapInfo.setState(FcoPowerState.poweredOff);
//					break;
//				    case POWERED_ON:
//					snapInfo.setState(FcoPowerState.poweredOn);
//					break;
//				    case SUSPENDED:
//					snapInfo.setState(FcoPowerState.suspended);
//					break;
//				    }
//				    snapInfo.setResult(OperationState.SUCCESS);
//				    resultAction.getSnapList().add(snapInfo);
//				}
//			    }
						} else if (fco instanceof ImprovedVirtualDisk) {
							final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
							final VStorageObjectSnapshotInfo currentSnap = ivd.getSnapshots();
							if (currentSnap != null) {
								if ((currentSnap.getSnapshots() != null) && (currentSnap.getSnapshots().size() > 0)) {
									for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snTree : currentSnap
											.getSnapshots()) {
										final SnapshotInfo snapInfo = new SnapshotInfo();
										snapInfo.setCreateTime(snTree.getCreateTime());
										snapInfo.setDescription(snTree.getDescription());
										snapInfo.setId(snTree.getId().getId());
										snapInfo.setName("");

										snapInfo.setResult(OperationState.SUCCESS);
										resultAction.getSnapList().add(snapInfo);
									}
								}
							}
						}
					} catch (final Exception e) {
						Utility.logWarning(this.logger, e);
						resultAction.failure(e);

					}
				}
				result.add(resultAction);
			}
		}
		return result;
	}

	@Override
	protected String getLogName() {
		return "SnapshotCommand";
	}

	private List<SnapshotInfo> getVmSnapShots(final VirtualMachineManager vmm)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
		final List<SnapshotInfo> result = new LinkedList<>();
		if (vmm.getSnapInfo() != null) {
			final ArrayList<VirtualMachineSnapshotTree> snapList = SnapshotsList(
					vmm.getSnapInfo().getRootSnapshotList());
			for (final VirtualMachineSnapshotTree snTree : snapList) {
				final SnapshotInfo snapInfo = new SnapshotInfo();
				snapInfo.setFcoEntity(vmm.getFcoInfo());
				snapInfo.setCreateTime(snTree.getCreateTime());
				snapInfo.setDescription(snTree.getDescription());
				snapInfo.setId(snTree.getId().toString());
				snapInfo.setName(snTree.getName());
				switch (snTree.getState()) {
				case POWERED_OFF:
					snapInfo.setState(FcoPowerState.poweredOff);
					break;
				case POWERED_ON:
					snapInfo.setState(FcoPowerState.poweredOn);
					break;
				case SUSPENDED:
					snapInfo.setState(FcoPowerState.suspended);
					break;
				}
				snapInfo.setResult(OperationState.SUCCESS);
				result.add(snapInfo);
			}
		}
		return result;
	}

	@Override
	public final void initialize() {
		this.options = new CoreBasicCommandOptions();
		this.list = false;
		this.create = false;
		this.delete = null;
		this.deleteAll = false;
		this.revert = false;
		this.name = null;
	}

	private ArrayList<VirtualMachineSnapshotTree> SnapshotsList(final List<VirtualMachineSnapshotTree> snTreeList) {
		final ArrayList<VirtualMachineSnapshotTree> result = new ArrayList<>();

		for (final VirtualMachineSnapshotTree snTree : snTreeList) {
			result.add(snTree);
			if (snTree.getChildSnapshotList() != null) {
				result.addAll(SnapshotsList(snTree.getChildSnapshotList()));
			}
		}
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
