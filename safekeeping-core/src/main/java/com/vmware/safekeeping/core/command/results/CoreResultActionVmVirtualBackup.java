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
package com.vmware.safekeeping.core.command.results;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.options.CoreVirtualBackupOptions;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.GuestInfoFlags;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.core.type.location.CoreVmLocation;

public class CoreResultActionVmVirtualBackup extends AbstractCoreResultActionVirtualBackupForEntityWithDisks {
    /**
     * 
     */
    private static final long serialVersionUID = -1425421293237301621L;

    private final CopyOnWriteArrayList<CoreResultActionDiskVirtualBackup> resultActionsOnDisks;

    private GuestInfoFlags guestFlags;

    private boolean template;

    public CoreResultActionVmVirtualBackup(final IFirstClassObject fco, final CoreVirtualBackupOptions options) {
        super(fco, options);
        this.resultActionsOnDisks = new CopyOnWriteArrayList<>();
    }

    @Override
    public void addResultActionOnDisk(final AbstractCoreResultDiskBackupRestore ret) {
        this.resultActionsOnDisks.add((CoreResultActionDiskVirtualBackup) ret);
    }

    public void disksSuccess() throws CoreResultActionException {
        for (final CoreResultActionDiskVirtualBackup rd : getResultActionsOnDisk()) {
            if (rd.isFails()) {
                failure(rd.getReason());
                return;
            }
        }

    }

    @Override
    public OperationState getDiskActionsResult() {
        boolean queued = false;
        for (final CoreResultActionDiskVirtualBackup rd : getResultActionsOnDisk()) {

            switch (rd.getState()) {
            case ABORTED:
                return OperationState.ABORTED;
            case FAILED:
                return OperationState.FAILED;
            case QUEUED:
                queued = true;
                break;
            case STARTED:
                return OperationState.STARTED;
            case SKIPPED:
            case SUCCESS:
            default:
                break;
            }
        }
        if (queued) {
            return OperationState.QUEUED;
        } else {
            return OperationState.SUCCESS;
        }

    }

    @Override
    public VirtualMachineManager getFirstClassObject() {
        return (VirtualMachineManager) super.getFirstClassObject();
    }

    /**
     * @return the guestFlags
     */
    public GuestInfoFlags getGuestFlags() {
        return this.guestFlags;
    }

    /**
     * @return the resultActionsOnDisks
     */
    public List<CoreResultActionDiskVirtualBackup> getResultActionsOnDisk() {
        return this.resultActionsOnDisks;
    }

    @Override
    public OperationState getState() {
        OperationState result = super.getState();
        if ((result == OperationState.SUCCESS)) {
            OperationState diskResult = OperationState.SUCCESS;
            String diskResultReason = StringUtils.EMPTY;
            for (final CoreResultActionDiskVirtualBackup radb : getResultActionsOnDisk()) {
                if ((result == OperationState.SUCCESS)) {
                    switch (radb.getState()) {
                    case ABORTED:
                        diskResult = OperationState.ABORTED;
                        break;
                    case FAILED:
                        if (diskResult == OperationState.ABORTED) {
                            diskResult = OperationState.ABORTED;
                        } else {
                            diskResult = OperationState.FAILED;
                            diskResultReason = radb.getReason();
                        }
                        break;
                    case SKIPPED:
                        switch (diskResult) {
                        case ABORTED:
                            diskResult = OperationState.ABORTED;
                            break;
                        case FAILED:
                            diskResult = OperationState.FAILED;
                            diskResultReason = radb.getReason();
                            break;
                        case STARTED:
                            diskResult = OperationState.SKIPPED;
                            break;
                        case SKIPPED:
                            diskResult = OperationState.SKIPPED;
                            diskResultReason = radb.getReason();
                            break;
                        case SUCCESS:
                            diskResult = OperationState.SUCCESS;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SUCCESS:
                        diskResult = OperationState.SUCCESS;
                        break;
                    default:
                        break;
                    }
                    result = diskResult;
                    setReason(diskResultReason);
                }
            }
        }
        if (isDone() && (getEndDate() == null)) {
            setEndTime();
        }
        return result;

    }

    public boolean isTemplate() {
        return this.template;
    }

    /**
     * @param guestFlags the guestFlags to set
     */
    public void setGuestFlags(final GuestInfoFlags guestFlags) {
        this.guestFlags = guestFlags;
    }

    @Override
    public void setLocations(final GenerationProfile profile) {
        final CoreVmLocation vmLocation = new CoreVmLocation();
        vmLocation.setDatastoreInfo(profile.getDatastoreInfo());

        vmLocation.setDatacenterInfo(profile.getDatacenterInfo());
        vmLocation.setResourcePoolInfo(profile.getResourcePoolInfo());
        vmLocation.setResourcePoolFullPath(profile.getResourcePoolPath());

        if (profile.hasParent() && (profile.getFcoParent().getEntityType() == EntityType.VirtualApp)) {
            vmLocation.setvAppMember(true);
        } else {
            vmLocation.setVmFolderInfo(profile.getFolderInfo());
            vmLocation.setVmFolderFullPath(profile.getFolderPath());
        }

        final String vmxPath = profile.getConfigSpec().getFiles().getVmPathName();
        vmLocation.setVmxFullPath(vmxPath);
        vmLocation.setDatastorePath(StringUtils.substringBeforeLast(vmxPath, "/"));
        vmLocation.setVmxFileName(StringUtils.substringAfterLast(vmxPath, "/"));
        this.locations = vmLocation;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(final boolean template) {
        this.template = template;
    }

}
