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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.jDiskLib.Connection;
import com.vmware.safekeeping.core.command.options.CoreBackupRestoreCommonOptions;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCreateSnap;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDeleteSnap;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.location.AbstractCoreFcoLocation;

public abstract class AbstractCoreResultActionBackupRestore extends AbstractCoreResultActionWithSubOperations {
    /**
     * 
     */
    private static final long serialVersionUID = 5764004955070939476L;

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AbstractCoreResultActionBackupRestore.class.getName());

    private String targetName;

    private GenerationInfo generationInfo;

    private IFirstClassObject firstClassObject;

    private CoreResultActionCreateSnap createSnapshotAction;

    private CoreResultActionDeleteSnap deleteSnapshotAction;
    private Integer index;
    private GenerationProfile profile;
    protected AbstractCoreFcoLocation locations;
    private Connection connectionHandle;
    protected final CoreBackupRestoreCommonOptions options;

    AbstractCoreResultActionBackupRestore(final CoreBackupRestoreCommonOptions options) {
        this.index = null;
        this.options = options;
    }

    AbstractCoreResultActionBackupRestore(final IFirstClassObject fco, final CoreBackupRestoreCommonOptions options) {
        this.firstClassObject = fco;
        setFcoEntityInfo(fco.getFcoInfo());
        this.index = null;
        this.options = options;
    }

    public Connection getConnectionHandle() {
        return this.connectionHandle;
    }

    public CoreResultActionCreateSnap getCreateSnapshotAction() {
        return this.createSnapshotAction;
    }

    public CoreResultActionDeleteSnap getDeleteSnapshotAction() {
        return this.deleteSnapshotAction;
    }

    public IFirstClassObject getFirstClassObject() {
        return this.firstClassObject;
    }

    public int getGenerationId() {
        if (this.generationInfo == null) {
            return -2;
        }
        return this.generationInfo.getGenerationId();
    }

    public GenerationInfo getGenerationInfo() {
        return this.generationInfo;
    }

    /**
     * @return the index
     */
    public Integer getIndex() {
        return this.index;
    }

    /**
     * @return the locations
     */
    public AbstractCoreFcoLocation getLocations() {
        return this.locations;
    }

    /**
     * @return the options
     */
    public CoreBackupRestoreCommonOptions getOptions() {
        return this.options;
    }

    public GenerationProfile getProfile() {
        return this.profile;
    }

    @Override
    public OperationState getState() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final OperationState result = super.getState();
        for (final AbstractCoreResultActionImpl op : getSubOperations()) {
            if (op.isDone()) {
                switch (result) {
                case ABORTED:
                    forceAbort(op.getReason());
                    break;
                case FAILED:
                    switch (op.getState()) {
                    case ABORTED:
                        forceAbort(op.getReason());
                        break;
                    case FAILED:
                    case QUEUED:
                    case SKIPPED:
                    case STARTED:
                    case SUCCESS:
                        break;
                    }
                    break;
                case SKIPPED:
                    switch (op.getState()) {
                    case ABORTED:
                        forceAbort(op.getReason());
                        break;
                    case FAILED:
                        forceFail(op.getReason());
                        break;
                    case QUEUED:
                    case SKIPPED:
                    case STARTED:
                    case SUCCESS:
                        break;
                    }
                    break;
                case STARTED:
                case SUCCESS:
                    switch (op.getState()) {
                    case ABORTED:
                        forceAbort(op.getReason());
                        break;
                    case FAILED:
                        forceFail(op.getReason());
                        break;
                    case QUEUED:
                    case SKIPPED:
                    case STARTED:
                    case SUCCESS:
                    default:
                        break;
                    }

                    break;
                default:
                    break;
                }
            }
        }

        if (isDone() && (getEndDate() == null)) {
            setEndTime();
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;

    }

    /**
     * @return
     */
    public String getTargetName() {
        return this.targetName;
    }

    public void setConnectionHandle(final Connection connectionHandle) {
        this.connectionHandle = connectionHandle;
    }

    public void setCreateSnapshotAction(final CoreResultActionCreateSnap createSnapshotAction) {
        this.createSnapshotAction = createSnapshotAction;
    }

    public void setDeleteSnapshotAction(final CoreResultActionDeleteSnap deleteSnapshotAction) {
        this.deleteSnapshotAction = deleteSnapshotAction;
    }

    public void setFirstClassObject(final IFirstClassObject firstClassObject) {
        this.firstClassObject = firstClassObject;
    }

    public void setGenerationInfo(final GenerationInfo generationInfo) {
        this.generationInfo = generationInfo;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(final Integer index) {
        this.index = index;
    }

    public void setLocations(final AbstractCoreFcoLocation locations) {
        this.locations = locations;

    }

    public void setProfile(final GenerationProfile profile) {
        this.profile = profile;
    }

    public void setTargetName(final String targetName) {
        this.targetName = targetName;
    }

    @Override
    public String toString() {
        return getState().toString() + " " + getFcoToString();
    }

    /**
     * @return
     */

}
