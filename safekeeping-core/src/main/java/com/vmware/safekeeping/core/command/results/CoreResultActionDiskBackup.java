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

import com.vmware.safekeeping.core.core.JVmdkInfo;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.QueryBlocksOption;
import com.vmware.safekeeping.core.type.enums.phase.BackupDiskPhases;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;

public class CoreResultActionDiskBackup extends AbstractCoreResultDiskBackupRestore {
    /**
     * 
     */
    private static final long serialVersionUID = -6645200389717343805L;

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(CoreResultActionDiskBackup.class.getName());

    private volatile Integer generationId;
    private String changeId;

    private BackupMode backupMode;
    private boolean noChanges;
    private boolean compressed;
    private boolean cbtHealth;
    private boolean cipher;
    private BackupDiskPhases phase;
    private QueryBlocksOption queryBlocksOption;
    private boolean changedBlockTrackingEnabled;
    private final IFirstClassObject firstClassObject;
    private JVmdkInfo jvmdkInfo;

    public CoreResultActionDiskBackup(final GenerationProfile profile, final CoreResultActionIvdBackup parent) {
        super(0, profile, parent);
        this.generationId = null;
        this.phase = BackupDiskPhases.NONE;
        parent.addResultActionOnDisk(this);
        setChangeId(profile.getDiskChangeId(0));
        setCompressed(parent.isCompressed());
        setCipher(parent.isCipher());
        setName(parent.getLocations().getVmdkFullPath());
        this.firstClassObject = parent.getFirstClassObject();
    }

    public CoreResultActionDiskBackup(final int diskId, final GenerationProfile profile,
            final CoreResultActionVmBackup parent) {
        super(diskId, profile, parent);
        this.generationId = null;
        this.phase = BackupDiskPhases.NONE;
        parent.addResultActionOnDisk(this);
        setChangeId(profile.getDiskChangeId(diskId));
        setCompressed(parent.isCompressed());
        setCipher(parent.isCipher());
        this.firstClassObject = parent.getFirstClassObject();
    }

    public BackupMode getBackupMode() {
        return this.backupMode;
    }

    public String getChangeId() {
        return this.changeId;
    }

    public IFirstClassObject getFirstClassObject() {
        return this.firstClassObject;
    }

    public Integer getGenerationId() {
        return this.generationId;
    }

    public JVmdkInfo getJVmdkInfo() {
        return this.jvmdkInfo;
    }

    @Override
    public AbstractCoreResultActionBackupForEntityWithDisks getParent() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final AbstractCoreResultActionBackupForEntityWithDisks returnCoreAbstractResultActionBackupForEntityWithDisks = (AbstractCoreResultActionBackupForEntityWithDisks) this.parent;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnCoreAbstractResultActionBackupForEntityWithDisks;
    }

    /**
     * @return the phase
     */
    public BackupDiskPhases getPhase() {
        return this.phase;
    }

    /**
     * @return the queryBlocksOption
     */
    public QueryBlocksOption getQueryBlocksOption() {
        return this.queryBlocksOption;
    }

    @Override
    public int getTotalNumberOfDisks() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final int returnint = getParent().getNumberOfDisk();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnint;
    }

    public boolean hasNoChanges() {
        return this.noChanges;
    }

    public boolean isCbtHealth() {
        return this.cbtHealth;
    }

    public boolean isChangedBlockTrackingEnabled() {
        return this.changedBlockTrackingEnabled;
    }

    /**
     * @return the cipher
     */
    public boolean isCipher() {
        return this.cipher;
    }

    public boolean isCompressed() {
        return this.compressed;
    }

    /**
     * @return the noChanges
     */
    public boolean isNoChanges() {
        return this.noChanges;
    }

    public void setBackupMode(final BackupMode backupMode) {
        this.backupMode = backupMode;
    }

    public void setCbtHealth(final boolean cbtHealth) {
        this.cbtHealth = cbtHealth;
    }

    public void setChangedBlockTrackingEnabled(final boolean changedBlockTrackingEnabled) {
        this.changedBlockTrackingEnabled = changedBlockTrackingEnabled;
    }

    public void setChangeId(final String changeId) {
        this.changeId = changeId;
    }

    /**
     * @param cipher the cipher to set
     */
    public void setCipher(final boolean cipher) {
        this.cipher = cipher;
    }

    public void setCompressed(final boolean compressed) {
        this.compressed = compressed;
    }

    /**
     * @param generationId the generationId to set
     */
    public void setGenerationId(final Integer generationId) {
        this.generationId = generationId;
    }

    public void setJVmdkInfo(final JVmdkInfo jvmdkInfo) {
        this.jvmdkInfo = jvmdkInfo;
    }

    /**
     * @param noChanges the noChanges to set
     */
    public void setNoChanges(final boolean noChanges) {
        this.noChanges = noChanges;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(final BackupDiskPhases phase) {
        this.phase = phase;
    }

    /**
     * @param queryBlocksOption the queryBlocksOption to set
     */
    public void setQueryBlocksOption(final QueryBlocksOption queryBlocksOption) {
        this.queryBlocksOption = queryBlocksOption;
    }

    @Override
    public IFirstClassObject getFirstClassObjectParent() {
        return getParent().getFirstClassObject();
    }
}
