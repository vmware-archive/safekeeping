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
package com.vmware.safekeeping.core.type.fco;

import java.io.Serializable;
import java.util.List;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public interface IFirstClassObject extends Serializable {

    int MAX_NAME_LENGHT_SUBSTR = 30;

    String THREE_DOTS = "...";

    boolean destroy() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException, VimFaultFaultMsg, com.vmware.vslm.FileFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg,
            com.vmware.vslm.RuntimeFaultFaultMsg, com.vmware.vslm.TaskInProgressFaultMsg, VslmFaultFaultMsg,
            VslmTaskException, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            com.vmware.vim25.NotFoundFaultMsg, TaskInProgressFaultMsg;

    /**
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InterruptedException
     */
    ManagedEntityInfo getDatacenterInfo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException;

    EntityType getEntityType();

    ManagedFcoEntityInfo getFcoInfo();

    List<Block> getFullDiskAreas(final int diskId);

    /**
     * @return
     */

    default ManagedEntityInfo getManageEntityInfo() {
        return new ManagedEntityInfo(this);
    }

    ManagedObjectReference getMoref();

    String getName();

    ManagedFcoEntityInfo getParentFco() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException,
            com.vmware.vslm.RuntimeFaultFaultMsg;

    /**
     * @return
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     */
    FcoPowerState getPowerState() throws com.vmware.vslm.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, InterruptedException;

    String getServerUuid();

    default String getShortedEntityName() {
        final String name = ("undefined".equals(getName())) ? "CORRUPTED - Remove this Improved Virtual Disk"
                : getName();
        return (name.length() < MAX_NAME_LENGHT_SUBSTR) ? getName()
                : (getName().substring(0, MAX_NAME_LENGHT_SUBSTR - THREE_DOTS.length()).concat(THREE_DOTS));
    }

    FirstClassObjectType getType();

    String getUuid();

    VimConnection getVimConnection();

    String headertoString();

    boolean isChangedBlockTrackingEnabled() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException;

    /**
     * Check if the VM datastore is NFS
     *
     * @return true if the datastore is NFS
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InterruptedException
     * @throws VimObjectNotExistException
     */
    boolean isVmDatastoreNfs()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException;

    /**
     * Query VMDK changed disk area (CBT)
     *
     * @param profile
     * @param diskId
     * @param backupMode
     * @return List of modified blocks
     * @throws FileFaultFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws com.vmware.vslm.FileFaultFaultMsg
     * @throws InvalidArgumentFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws com.vmware.vslm.InvalidStateFaultMsg
     * @throws NotFoundFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidStateFaultMsg
     */
    List<Block> queryChangedDiskAreas(final GenerationProfile profile, Integer diskId, BackupMode backupMode)
            throws FileFaultFaultMsg, com.vmware.vim25.NotFoundFaultMsg, RuntimeFaultFaultMsg,
            com.vmware.vslm.FileFaultFaultMsg, InvalidArgumentFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg;

    boolean setChangeBlockTracking(final boolean enable) throws FileFaultFaultMsg, InvalidNameFaultMsg,
            InvalidStateFaultMsg, RuntimeFaultFaultMsg, SnapshotFaultFaultMsg, TaskInProgressFaultMsg,
            VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException,
            InterruptedException, ConcurrentAccessFaultMsg, DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg,
            InvalidDatastoreFaultMsg, com.vmware.vim25.NotFoundFaultMsg, com.vmware.vslm.InvalidDatastoreFaultMsg,
            com.vmware.vslm.InvalidStateFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg;

    @Override
    String toString();

    boolean isEncrypted();
}
