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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.TagModel;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmNonExistentHubsFaultMsg;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.PrettyBoolean.BooleanType;
import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateIvd;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionAttachIvd;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionAttachTag;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDetachIvd;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionDisableRelocation;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionExpandIvd;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdKeepAflterDeleteVm;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdList;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdPromote;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdReconcile;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionIvdTagList;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionRename;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.FcoTag;
import com.vmware.safekeeping.core.type.FcoTarget;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk.AttachedVirtualMachine;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.fco.managers.VirtualControllerManager;
import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfo;
import com.vmware.vim25.DeviceUnsupportedForVmVersionFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidControllerFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MissingControllerFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vim25.VslmTagEntry;
import com.vmware.vslm.VslmFaultFaultMsg;
import com.vmware.vslm.VslmSyncFaultFaultMsg;

public abstract class AbstractIvdCommand extends AbstractCommandWithOptions {
    protected enum TagOperationOptions {
        LIST, ATTACH, DETACH
    }

    static class UnitController {
        private Integer controllerKey;
        private Integer unitNumber;

        public Integer getControllerKey() {
            return this.controllerKey;
        }

        public Integer getUnitNumber() {
            return this.unitNumber;
        }

        public void setControllerKey(final Integer controllerKey) {
            this.controllerKey = controllerKey;
        }

        public void setUnitNumber(final Integer unitNumber) {
            this.unitNumber = unitNumber;
        }
    }

    protected TagOperationOptions tag;

    protected boolean attach;
    protected Boolean cbt;
    protected boolean create;
    protected String datastoreName;
    protected boolean detach;
    protected boolean detail;
    protected String device;
    protected Boolean disableRelocation;
    protected FileBackingInfoProvisioningType diskType;
    protected boolean extend;
    protected Boolean keepAfterDeleteVm;
    protected boolean list;
    protected String name;
    protected String promote;
    protected boolean reconcile;
    protected boolean remove;
    protected boolean rename;
    protected Long size;
    protected String sbpmProfileNames;

    protected CoreResultActionAttachIvd actionAttach(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionAttachIvd resultAction = new CoreResultActionAttachIvd()) {
            resultAction.start();
            VirtualMachineManager vmm = null;
            ImprovedVirtualDisk ivd = null;

            final List<IFirstClassObject> vmList = getFcoTarget(connetionManager,
                    FirstClassObjectFilterType.vm | FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.noTag);

            if (vmList.size() != 2) {
                resultAction.failure("No Improved Virtual Disk or/and Virtual Machine specified");
            } else {
                for (final IFirstClassObject fco : vmList) {
                    if (fco instanceof ImprovedVirtualDisk) {
                        ivd = (ImprovedVirtualDisk) fco;
                    } else if (fco instanceof VirtualMachineManager) {
                        vmm = (VirtualMachineManager) fco;
                    } else {
                        throw new CoreResultActionException(fco);
                    }
                }
                if (ivd == null) {
                    resultAction.failure("No Improved Virtual Disk specified");
                } else if (vmm == null) {
                    resultAction.failure("No target Virtual Machine specified");
                } else {
                    resultAction.setFcoEntityInfo(ivd.getFcoInfo());
                    resultAction.setVirtualMachine(vmm.getFcoInfo());
                    resultAction.setDatastore(ivd.getDatastoreInfo());
                    try {
                        if (StringUtils.isEmpty(this.device)) {
                            final List<VirtualControllerManager> listCtrl = vmm.getConfig().getControllersInfo();
                            if (!listCtrl.isEmpty()) {
                                resultAction.setControllerKey(listCtrl.get(0).getCkey());
                            }
                        } else {
                            final UnitController cu = deviceToUnitController(this.device);
                            resultAction.setControllerKey(cu.getControllerKey());
                            resultAction.setUnitNumber(cu.getUnitNumber());
                        }
                        if (!vmm.attachDisk(ivd, resultAction.getControllerKey(), resultAction.getUnitNumber())) {
                            resultAction.failure();
                        }
                    } catch (final DeviceUnsupportedForVmVersionFaultMsg | FileFaultFaultMsg | InvalidControllerFaultMsg
                            | InvalidDatastoreFaultMsg | InvalidStateFaultMsg | MissingControllerFaultMsg
                            | NotFoundFaultMsg | RuntimeFaultFaultMsg | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg
                            | VimObjectNotExistException | InvalidCollectorVersionFaultMsg | VimTaskException
                            | SafekeepingException e) {
                        Utility.logWarning(this.logger, e);
                        resultAction.failure(e);
                    } catch (final InterruptedException e) {
                        Utility.logWarning(this.logger, e);
                        resultAction.failure(e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return resultAction;
        }
    }

    protected List<CoreResultActionAttachTag> actionAttachTag(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionAttachTag> result = new ArrayList<>();
        final Map<String, FcoTarget> tagsTarget = getOptions().getTargetFcoList();
        final List<TagModel> tags = connetionManager.listTags(getOptions().getVim(), tagsTarget.keySet());

        final List<IFirstClassObject> ivdList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : ivdList) {
            try (final CoreResultActionAttachTag resultAction = new CoreResultActionAttachTag()) {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof ImprovedVirtualDisk) {
                        try {
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            for (final TagModel tagM : tags) {
                                final FcoTag fcoTag = new FcoTag();
                                fcoTag.setCategory(connetionManager
                                        .getTagCategory(getOptions().getVim(), tagM.getCategoryId()).getName());
                                fcoTag.setTag(tagM.getName());
                                fcoTag.setCategoryId(tagM.getCategoryId());
                                fcoTag.setTagId(tagM.getId());

                                if (!getOptions().isDryRun()) {
                                    if (ivd.attachTag(fcoTag)) {
                                        resultAction.getTags().add(fcoTag);
                                    } else {
                                        resultAction.failure();
                                        resultAction.setReason("Ivd %s failed to attach Tag %s:%s .", ivd.getName(),
                                                fcoTag.getCategory(), fcoTag.getTag());
                                        break;
                                    }
                                }
                            }
                        } catch (final NotFoundFaultMsg | RuntimeFaultFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                            resultAction.failure(e);
                        } finally {
                            resultAction.done();
                        }
                        result.add(resultAction);
                    }
                }
            }
        }
        return result;
    }

    protected List<CoreResultActionCbt> actionCbt(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionCbt> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionCbt resultAction = new CoreResultActionCbt();
            try {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        if (fco instanceof ImprovedVirtualDisk) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            resultAction.setPreviousFlagState(ivd.isChangedBlockTrackingEnabled());
                            if (this.cbt.equals(resultAction.getPreviousFlagState())) {
                                resultAction.skip(
                                        "Cbt already set to " + PrettyBoolean.toString(this.cbt, BooleanType.onOff));
                                resultAction.setFlagState(this.cbt);

                            } else {
                                if (!getOptions().isDryRun()) {
                                    resultAction.setFlagState(this.cbt);
                                    if (!ivd.setChangeBlockTracking(this.cbt)) {
                                        resultAction.failure();
                                    }
                                }
                            }
                        }
                    } catch (final InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                            | RuntimeFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                            | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                            | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
                        Utility.logWarning(this.logger, e);
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

    protected CoreResultActionCreateIvd actionCreate(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final CoreResultActionCreateIvd resultAction = new CoreResultActionCreateIvd(OperationState.STARTED);
        try {
            final FileBackingInfoProvisioningType backingType = FileBackingInfoProvisioningType
                    .parse(this.diskType.toString());
            resultAction.setName(this.name);
            resultAction.setBackingType(backingType);
            resultAction.setSize(this.size);
            resultAction.setSbpmProfileName(this.sbpmProfileNames);
            final ManagedEntityInfo datastoreInfo = connetionManager.getManagedEntityInfo(getOptions().getVim(),
                    EntityType.Datastore, this.datastoreName);
            resultAction.setDatastoreInfo(datastoreInfo);
            if (StringUtils.isEmpty(this.name)) {
                resultAction.failure("New name is empty or invalid");
            } else {
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();

                } else {
                    if (!getOptions().isDryRun()) {
                        final VStorageObject vStore = connetionManager.createIvd(getOptions().getVim(), this.name,
                                datastoreInfo, backingType, this.size, this.sbpmProfileNames);

                        final ManagedFcoEntityInfo fcoInfo = new ManagedFcoEntityInfo(this.name,
                                ImprovedVirtualDisk.getMoref(vStore.getConfig().getId()),
                                vStore.getConfig().getId().getId(), datastoreInfo.getServerUuid());
                        resultAction.setFcoEntityInfo(fcoInfo);
                    }
                }
            }
        } catch (final com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | InvalidPropertyFaultMsg
                | InvalidCollectorVersionFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg
                | VslmTaskException | VimTaskException | InvalidArgumentFaultMsg | VimObjectNotExistException
                | PbmFaultFaultMsg | PbmNonExistentHubsFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
        } catch (final InterruptedException e) {
            Utility.logWarning(this.logger, e);
            resultAction.failure(e);
            Thread.currentThread().interrupt();
        } finally {
            resultAction.done();
        }
        return resultAction;

    }

    protected CoreResultActionDetachIvd actionDetach(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionDetachIvd result = new CoreResultActionDetachIvd()) {
            VirtualMachineManager vmm = null;
            ImprovedVirtualDisk ivd = null;
            result.start();
            final List<IFirstClassObject> vmList = getFcoTarget(connetionManager,
                    FirstClassObjectFilterType.vm | FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.noTag);

            if (vmList.isEmpty()) {
                result.failure("No Improved Virtual Disk or/and Virtual Machine specified");
            } else {
                try {
                    for (final IFirstClassObject fco : vmList) {
                        if (fco instanceof ImprovedVirtualDisk) {
                            ivd = (ImprovedVirtualDisk) fco;
                        } else if (fco instanceof VirtualMachineManager) {
                            vmm = (VirtualMachineManager) fco;
                        } else {
                            throw new CoreResultActionException(fco);
                        }
                    }
                    if ((vmm == null) && (ivd != null)) {
                        final List<VStorageObjectAssociations> listAssociation = ivd.getVslmConnection()
                                .retrieveVStorageObjectAssociations(ivd);
                        if (!listAssociation.isEmpty()) {
                            final VStorageObjectAssociations association = listAssociation.get(0);

                            if (!association.getVmDiskAssociations().isEmpty()) {
                                for (final VStorageObjectAssociationsVmDiskAssociations vmDisk : association
                                        .getVmDiskAssociations()) {
                                    final ManagedObjectReference vmMor = MorefUtil
                                            .newManagedObjectReference(EntityType.VirtualMachine, vmDisk.getVmId());
                                    vmm = new VirtualMachineManager(ivd.getVimConnection(), vmMor);
                                    break;
                                }
                            }
                        }
                    }
                    if (ivd == null) {
                        result.failure("No Improved Virtual Disk specified");
                    } else if (vmm == null) {
                        result.failure("No Virtual Machine specified");
                    } else {
                        result.setFcoEntityInfo(ivd.getFcoInfo());
                        result.setVirtualMachine(vmm.getFcoInfo());
                        result.setDatastore(ivd.getDatastoreInfo());
                        if (!getOptions().isDryRun()) {
                            vmm.detachDisk(ivd);
                        }
                    }
                } catch (final FileFaultFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg | RuntimeFaultFaultMsg
                        | VmConfigFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
                        | com.vmware.vslm.RuntimeFaultFaultMsg | VimTaskException e) {
                    Utility.logWarning(this.logger, e);
                    result.failure(e);
                } catch (final InterruptedException e) {
                    Utility.logWarning(this.logger, e);
                    result.failure(e);
                    Thread.currentThread().interrupt();
                }
            }
            return result;
        }
    }

    protected List<CoreResultActionAttachTag> actionDetachTag(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionAttachTag> result = new ArrayList<>();
        final Map<String, FcoTarget> tagsTarget = getOptions().getTargetFcoList();
        final List<TagModel> tags = connetionManager.listTags(getOptions().getVim(), tagsTarget.keySet());

        final List<IFirstClassObject> ivdList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : ivdList) {
            try (final CoreResultActionAttachTag resultAction = new CoreResultActionAttachTag()) {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof ImprovedVirtualDisk) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        try {
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            for (final TagModel tagM : tags) {
                                final FcoTag lTag = new FcoTag();
                                lTag.setCategory(connetionManager
                                        .getTagCategory(getOptions().getVim(), tagM.getCategoryId()).getName());
                                lTag.setTag(tagM.getName());
                                lTag.setCategoryId(tagM.getCategoryId());
                                lTag.setTagId(tagM.getId());

                                if (!getOptions().isDryRun()) {
                                    resultAction.getTags().add(lTag);
                                    if (!ivd.detachTag(lTag)) {
                                        resultAction.failure();
                                        resultAction.setReason("Ivd %s failed to attach Tag %s:%s .", ivd.getName(),
                                                lTag.getCategory(), lTag.getTag());
                                        break;
                                    }
                                }
                            }
                        } catch (final NotFoundFaultMsg | RuntimeFaultFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                            resultAction.failure();
                            resultAction.setReason(e.getMessage());
                        }
                        result.add(resultAction);
                    }
                }
            }
        }
        return result;
    }

    protected List<CoreResultActionDisableRelocation> actionDisableRelocation(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionDisableRelocation> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionDisableRelocation resultAction = new CoreResultActionDisableRelocation();
            try {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        if (fco instanceof ImprovedVirtualDisk) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            resultAction.setPreviousFlagState(ivd.getConfigInfo().isRelocationDisabled());
                            if (this.disableRelocation.equals(resultAction.getPreviousFlagState())) {

                                resultAction.setFlagState(this.disableRelocation);
                                resultAction.skip("DisableRelocation already set to "
                                        + PrettyBoolean.toString(this.disableRelocation, BooleanType.onOff));
                            } else if (!getOptions().isDryRun()) {
                                resultAction.setFlagState(this.disableRelocation);
                                if (!ivd.setDisableRelocation(this.disableRelocation)) {
                                    resultAction.failure();
                                }
                            } else {
                                throw new CoreResultActionException(fco);
                            }
                        }
                    } catch (final InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                            | RuntimeFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                            | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                            | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
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

    protected CoreResultActionExpandIvd actionExpand(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionExpandIvd result = new CoreResultActionExpandIvd()) {
            result.start();
            final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
            result.setSize(this.size);
            if (this.size == null) {
                result.failure("New size is missing or invalid");
            } else if (targetList.size() == 1) {
                for (final IFirstClassObject fco : targetList) {

                    if (Vmbk.isAbortTriggered()) {
                        result.aborted("Aborted on user request");
                        break;
                    } else {
                        try {
                            if (fco instanceof ImprovedVirtualDisk) {
                                final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                                result.setFcoEntityInfo(fco.getFcoInfo());
                                if (ivd.getConfigInfo().getCapacityInMB() < PrettyNumber.toMegaByte(this.size)) {

                                    if (!getOptions().isDryRun() && (!ivd.extendDisk(this.size))) {
                                        result.failure();

                                    }
                                } else {
                                    result.failure(String.format(
                                            "Ivd:%s actual size %sMB is larger than the new specified size %s.",
                                            fco.toString(), ivd.getConfigInfo().getCapacityInMB(),
                                            PrettyNumber.toString(this.size, MetricPrefix.MEGA, 2)));
                                }
                            }

                        } catch (final FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
                                | NotFoundFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
                                | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
                                | com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                                | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                                | com.vmware.vslm.RuntimeFaultFaultMsg | com.vmware.vslm.TaskInProgressFaultMsg
                                | VslmFaultFaultMsg | VslmTaskException | VimTaskException e) {
                            Utility.logWarning(this.logger, e);
                            result.failure(e);
                        } catch (final InterruptedException e) {
                            Utility.logWarning(this.logger, e);
                            result.failure(e);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } else if (targetList.isEmpty()) {
                result.failure("No Improved Virtual Disk  specified");
            } else if (targetList.size() > 1) {
                result.failure("Number of targets is restricted to one");
            } else {
                if (this.logger.isLoggable(Level.FINE)) {
                    final String msg = "One target";
                    this.logger.fine(msg);
                }
            }
            return result;
        }
    }

    protected List<CoreResultActionIvdKeepAflterDeleteVm> actionKeepAfterDeleteVm(
            final ConnectionManager connetionManager) throws CoreResultActionException {
        final List<CoreResultActionIvdKeepAflterDeleteVm> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            final CoreResultActionIvdKeepAflterDeleteVm resultAction = new CoreResultActionIvdKeepAflterDeleteVm();
            try {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        if (fco instanceof ImprovedVirtualDisk) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            resultAction.setPreviousFlagState(ivd.getConfigInfo().isKeepAfterDeleteVm());
                            if (Boolean.compare(resultAction.getPreviousFlagState(), this.keepAfterDeleteVm) == 0) {
                                resultAction.setFlagState(this.keepAfterDeleteVm);
                                resultAction.skip("KeepAfterDeleteVm already set to "
                                        + PrettyBoolean.toString(this.keepAfterDeleteVm, BooleanType.onOff));
                            } else {
                                if (!getOptions().isDryRun()) {
                                    resultAction.setFlagState(this.keepAfterDeleteVm);
                                    if (!ivd.setKeepAfterDeleteVm(this.keepAfterDeleteVm)) {
                                        resultAction.failure();
                                    }
                                }
                            }
                        }
                    } catch (final InvalidDatastoreFaultMsg | InvalidStateFaultMsg | NotFoundFaultMsg
                            | RuntimeFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                            | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                            | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
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

    protected List<CoreResultActionIvdList> actionList(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
        final List<CoreResultActionIvdList> result = new ArrayList<>();
        for (final IFirstClassObject fco : targetList) {
            try (final CoreResultActionIvdList resultAction = new CoreResultActionIvdList(OperationState.STARTED)) {
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    if (fco instanceof ImprovedVirtualDisk) {
                        final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        final VStorageObjectConfigInfo config = ivd.getConfigInfo();
                        if (config == null) {
                            resultAction.skip("Cannot retrieve IVD configuration");
                        } else {

                            final BaseConfigInfoDiskFileBackingInfo backing = (BaseConfigInfoDiskFileBackingInfo) config
                                    .getBacking();
                            final String filePath = backing.getFilePath()
                                    .substring(backing.getFilePath().lastIndexOf(']') + 2);
                            final String lCbt = PrettyBoolean.toString(config.isChangedBlockTrackingEnabled(),
                                    BooleanType.standard);

                            resultAction.setCapacityInMB(config.getCapacityInMB());
                            resultAction.setCbt(lCbt);
                            resultAction.setHasSnapshot(backing.getParent() != null);
                            resultAction.setIsAttached(false);
                            resultAction.setDatastore(ivd.getDatastoreInfo());
                            resultAction.setFilePath(filePath);
                            try {
                                resultAction.setIsAttached(ivd.isAttached());
                                if (resultAction.isAttached() && this.detail) {
                                    final AttachedVirtualMachine avm = ivd.getAttachedVirtualMachine();

                                    resultAction.setAttachedVm(avm.getFcoInfo());
                                    resultAction.setVmControllerKey(avm.getDiskKey());
                                }
                            } catch (final com.vmware.vslm.RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                                    | RuntimeFaultFaultMsg e) {
                                Utility.logWarning(this.logger, e);
                                resultAction.failure(e.getMessage());
                            } catch (final InterruptedException e) {
                                Utility.logWarning(this.logger, e);
                                resultAction.failure(e);
                                Thread.currentThread().interrupt();
                            }
                        }

                    }
                }

                result.add(resultAction);
            }
        }
        return result;
    }

    protected List<CoreResultActionIvdTagList> actionListTags(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionIvdTagList> result = new ArrayList<>();
        final List<IFirstClassObject> ivdList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
        for (final IFirstClassObject fco : ivdList) {
            try (final CoreResultActionIvdTagList resultAction = new CoreResultActionIvdTagList()) {
                resultAction.start();
                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted("Aborted on user request");

                    result.clear();
                    result.add(resultAction);
                    break;
                } else {

                    if (fco instanceof ImprovedVirtualDisk) {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        try {
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            final List<VslmTagEntry> lTags = ivd.listTags();
                            if ((lTags != null)) {
                                for (final VslmTagEntry vslmTag : lTags) {
                                    final FcoTag ftag = new FcoTag();
                                    ftag.setTag(vslmTag.getTagName());
                                    ftag.setCategory(vslmTag.getParentCategoryName());
                                    resultAction.getTags().add(ftag);
                                }
                            }
                        } catch (final NotFoundFaultMsg | RuntimeFaultFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                            resultAction.failure();
                            resultAction.setReason(e.getMessage());
                        }
                    }
                    result.add(resultAction);
                }
            }
        }
        return result;
    }

    protected CoreResultActionIvdPromote actionPromote(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionIvdPromote result = new CoreResultActionIvdPromote()) {
            result.start();
            final List<IFirstClassObject> targetList = getFcoTarget(connetionManager,
                    FirstClassObjectFilterType.vm | FirstClassObjectFilterType.noTag);

            if (!StringUtils.isNumeric(this.promote) && StringUtils.isNotEmpty(this.promote)) {
                result.failure("Invalid DiskId");
            } else if (targetList.size() == 1) {
                for (final IFirstClassObject fco : targetList) {

                    if (Vmbk.isAbortTriggered()) {
                        result.aborted("Aborted on user request");
                        result.setReason("Aborted on user request");
                        break;
                    } else {
                        try {
                            if (fco instanceof VirtualMachineManager) {
                                final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                                result.setFcoEntityInfo(fco.getFcoInfo());
                                if (!getOptions().isDryRun()) {
                                    final List<String> allDisk = vmm.getConfig().getAllDiskNameList();

                                    if (StringUtils.isEmpty(this.promote)) {
                                        for (int index = 0; index < allDisk.size(); index++) {
                                            final String ivdName = StringUtils.isEmpty(this.name)
                                                    ? String.format("vmdk_%d_%s", index, vmm.getName())
                                                    : String.format("%s_%d", this.name, index);
                                            result.setName(ivdName);
                                            final VStorageObject vStore = vmm.getVimConnection().getVslmConnection()
                                                    .promoteIvd(vmm.getDatacenterInfo(), allDisk.get(index), ivdName);
                                            if (vStore != null) {
                                                final ManagedFcoEntityInfo fcoInfo = new ManagedFcoEntityInfo(ivdName,
                                                        ImprovedVirtualDisk.getMoref(vStore.getConfig().getId()),
                                                        vStore.getConfig().getId().getId(), vmm.getServerUuid());
                                                result.getImprovedVirtualDiskList().put(index, fcoInfo);
                                            } else {
                                                result.failure();
                                                break;
                                            }
                                        }
                                    } else {
                                        final int index = PrettyNumber.toInteger(this.promote);
                                        final String ivdName = StringUtils.isEmpty(this.name)
                                                ? String.format("vmdk_%d_%s", index, vmm.getName())
                                                : this.name;
                                        result.setName(ivdName);
                                        final VStorageObject vStore = vmm.getVimConnection().getVslmConnection()
                                                .promoteIvd(vmm.getDatacenterInfo(), allDisk.get(index), ivdName);
                                        if (vStore != null) {
                                            final ManagedFcoEntityInfo fcoInfo = new ManagedFcoEntityInfo(ivdName,
                                                    ImprovedVirtualDisk.getMoref(vStore.getConfig().getId()),
                                                    vStore.getConfig().getId().getId(), vmm.getServerUuid());

                                            result.getImprovedVirtualDiskList().put(index, fcoInfo);
                                        } else {
                                            result.failure();
                                        }
                                    }
                                }
                            }

                        } catch (final AlreadyExistsFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg
                                | RuntimeFaultFaultMsg | com.vmware.vslm.AlreadyExistsFaultMsg
                                | com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | VslmSyncFaultFaultMsg
                                | InvalidPropertyFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                            result.failure(e);
                        } catch (final InterruptedException e) {
                            Utility.logWarning(this.logger, e);
                            result.failure(e);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } else if (targetList.isEmpty()) {
                result.failure("No Virtual Machine specified");
            } else {
                result.failure("Number of targets is restricted to one");
            }
            return result;
        }
    }

    protected CoreResultActionIvdReconcile actionReconcile(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionIvdReconcile result = new CoreResultActionIvdReconcile()) {
            result.start();

            if (StringUtils.isEmpty(this.datastoreName)) {
                result.failure("No datastore specified.");
            } else {
                try {
                    final ManagedEntityInfo datastoreInfo = connetionManager.getManagedEntityInfo(getOptions().getVim(),
                            EntityType.Datastore, this.datastoreName);
                    if (datastoreInfo != null) {
                        result.setDatastore(datastoreInfo);
                        if (Vmbk.isAbortTriggered()) {
                            result.aborted("Aborted on user request");
                            result.setReason("Aborted on user request");
                        } else {
                            if (!getOptions().isDryRun()
                                    && !connetionManager.reconcileDatastore(getOptions().getVim(), datastoreInfo)) {
                                result.failure();
                            }
                        }
                    } else {
                        result.failure("Not Existing datastore " + this.datastoreName);
                    }
                } catch (final com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                        | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | InvalidDatastoreFaultMsg
                        | NotFoundFaultMsg | RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                        | InvalidCollectorVersionFaultMsg | VimTaskException | VslmTaskException
                        | VimObjectNotExistException e) {
                    Utility.logWarning(this.logger, e);
                    result.failure(e);
                } catch (final InterruptedException e) {
                    this.logger.log(Level.WARNING, "Interrupted!", e);
                    result.failure(e);
                    Thread.currentThread().interrupt();
                }
            }
            return result;
        }
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
                        if (fco instanceof ImprovedVirtualDisk) {
                            resultAction.setFcoEntityInfo(fco.getFcoInfo());
                            final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                            if (!getOptions().isDryRun() && !ivd.destroy()) {
                                resultAction.failure();
                            }
                        }
                    } catch (final com.vmware.vslm.FileFaultFaultMsg | com.vmware.vslm.InvalidDatastoreFaultMsg
                            | com.vmware.vslm.InvalidStateFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                            | com.vmware.vslm.RuntimeFaultFaultMsg | com.vmware.vslm.TaskInProgressFaultMsg
                            | VslmFaultFaultMsg | VslmTaskException | FileFaultFaultMsg | InvalidDatastoreFaultMsg
                            | InvalidStateFaultMsg | NotFoundFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
                            | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException e) {
                        Utility.logWarning(this.logger, e);
                        resultAction.failure(e);
                    } catch (final InterruptedException e) {
                        this.logger.log(Level.WARNING, "Interrupted!", e);
                        resultAction.failure(e);
                        Thread.currentThread().interrupt();
                    }
                }
                result.add(resultAction);
            } finally {
                resultAction.done();
            }
        }
        return result;
    }

    protected CoreResultActionRename actionRename(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionRename result = new CoreResultActionRename()) {
            result.start();
            final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());
            result.setName(this.name);
            if (StringUtils.isEmpty(this.name)) {
                result.failure("New name is empty or invalid");
            } else if (targetList.size() == 1) {
                for (final IFirstClassObject fco : targetList) {

                    if (Vmbk.isAbortTriggered()) {
                        result.aborted("Aborted on user request");
                        break;
                    } else {
                        try {
                            if (fco instanceof ImprovedVirtualDisk) {
                                final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                                result.setFcoEntityInfo(fco.getFcoInfo());
                                if (!getOptions().isDryRun() && !ivd.rename(this.name)) {
                                    result.failure();
                                }
                            }

                        } catch (final FileFaultFaultMsg | InvalidDatastoreFaultMsg | NotFoundFaultMsg
                                | RuntimeFaultFaultMsg | com.vmware.vslm.FileFaultFaultMsg
                                | com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                                | com.vmware.vslm.RuntimeFaultFaultMsg | VslmFaultFaultMsg | VslmSyncFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                            result.failure(e);
                        }
                    }
                }
            } else if (targetList.isEmpty()) {
                result.failure("No Improved Virtual Disk  specified");
            } else {
                result.failure("Number of targets is restricted to one");
            }
            return result;
        }
    }

    private UnitController deviceToUnitController(final String device) throws SafekeepingException {
        final String[] ctrlDisk = (StringUtils.isEmpty(device)) ? new String[0] : device.split(":");
        final UnitController result = new UnitController();
        switch (ctrlDisk.length) {
        case 0:
            break;
        case 2:
            if (StringUtils.isNumeric(ctrlDisk[1])) {
                result.setUnitNumber(PrettyNumber.toInteger(ctrlDisk[1]));
            } else {
                throw new SafekeepingException("Invalid id number " + ctrlDisk[1]);
            }
            if (StringUtils.isNumeric(ctrlDisk[0])) {
                result.setControllerKey(PrettyNumber.toInteger(ctrlDisk[0]));
            } else {
                throw new SafekeepingException("Invalid controller number " + ctrlDisk[0]);
            }
            break;
        case 1:
            if (StringUtils.isNumeric(ctrlDisk[0])) {
                result.setControllerKey(PrettyNumber.toInteger(ctrlDisk[0]));
            } else {
                throw new SafekeepingException("Invalid controller number " + ctrlDisk[0]);
            }
            break;
        default:
            throw new SafekeepingException("Invalid arguments number " + device);
        }
        return result;
    }

    public String getDatastoreName() {
        return this.datastoreName;
    }

    @Override
    protected String getLogName() {
        return "IvdCommand";
    }

//  

}
