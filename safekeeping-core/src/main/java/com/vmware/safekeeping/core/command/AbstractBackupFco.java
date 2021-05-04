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
package com.vmware.safekeeping.core.command;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.JVixException;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.AbstractCoreBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.command.results.CoreResultActionPrepeareGeneration;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.logger.MessagesTemplate;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.FcoGenerationsCatalog;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.GenerationProfileSpec;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.managers.PrivilegesList;
import com.vmware.safekeeping.core.soap.managers.VimPrivilegeChecker;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public abstract class AbstractBackupFco {
    protected final Logger logger;
    protected final AbstractCoreBackupOptions options;

    private final VimConnection vimConnection;

    /**
     * @param vimConnection2
     * @param options2
     * @param interactive
     */
    AbstractBackupFco(final VimConnection vimConnection, final AbstractCoreBackupOptions options, final Logger logger) {
        this.options = options;
        this.vimConnection = vimConnection;
        this.logger = logger;
    }

    protected CoreResultActionLoadProfile actionLoadProfile(final FcoArchiveManager vmArcMgr,
            final AbstractCoreResultActionVirtualBackup rab) throws CoreResultActionException {
        final CoreResultActionLoadProfile result = new CoreResultActionLoadProfile(rab);

        try {
            result.start();
            result.setProfile(vmArcMgr.loadProfileGeneration(-1));
            if ((result.getProfile() == null) || !result.getProfile().isProfileValid()) {
                final String msg = String.format("GenerationId %d doesn't exist.", -1);
                result.failure(msg);
                this.logger.warning(msg);
            } else if (!result.getProfile().isSucceeded()) {
                final String msg = String.format("GenerationId %d is marked FAILED.",
                        result.getProfile().getGenerationId());
                result.failure(msg);
                this.logger.warning(msg);
            } else {
                if (this.logger.isLoggable(Level.FINE)) {
                    final String msg = String.format("GenerationId %d is good.", result.getProfile().getGenerationId());
                    this.logger.fine(msg);
                }
            }

        } catch (final IOException e) {
            result.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            result.done();
        }
        return result;
    }

    /**
     * Check privileges
     * 
     * @param rab
     * @return
     * @throws CoreResultActionException
     */
    protected boolean checkPrivileges(AbstractCoreResultActionBackup rab) throws VimPermissionException {
        List<String> failedCheck = new ArrayList<>();
        for (Entry<String, Boolean> entry : checkRequiredBackupPrivileges(rab).entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                failedCheck.add(entry.getKey());
            }
        }
        if (!failedCheck.isEmpty()) {
            throw new VimPermissionException(
                    "Account lack of the following privileges:" + StringUtils.join(failedCheck, ","));
        }
        List<String> failedOptionalCheck = new ArrayList<>();
        for (Entry<String, Boolean> entry : checkOptionalBackupPrivileges(rab).entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                failedOptionalCheck.add(entry.getKey());
            }
        }
        if (!failedOptionalCheck.isEmpty()) {
            try {
                if (!CoreGlobalSettings.skipEncryptionCheck() && vimConnection.isEncryptionEnabled()
                        && rab.getFirstClassObject().isEncrypted()) {
                    throw new VimPermissionException(
                            "Encryption is enabled and the account lack of the following privileges:"
                                    + StringUtils.join(failedOptionalCheck, ","));
                } else {
                    String st = "Account lack of the following privileges:"
                            + StringUtils.join(failedOptionalCheck, ",");
                    logger.warning(st);
                }
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                throw new VimPermissionException(e);
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
        return true;
    }

    /**
     * Check the optional privileges for a backup
     * 
     * @return
     * @throws JVixException
     */
    private Map<String, Boolean> checkOptionalBackupPrivileges(AbstractCoreResultActionBackup rab)
            throws VimPermissionException {
        Map<String, Boolean> methodAuhorization = new HashMap<>();
        try {
            final VimPrivilegeChecker privChecker = rab.getFirstClassObject().getVimConnection().getPrivilegeChecker();

            methodAuhorization.putAll(privChecker.hasUserPrivilegesOnEntity(
                    rab.getFirstClassObject().getManageEntityInfo(), PrivilegesList.PRIVILEGE_CRYPTOGRAPHER_ACCESS));

        } catch (final RuntimeFaultFaultMsg e) {
            throw new VimPermissionException(e);
        }
        return methodAuhorization;
    }

    /**
     * Check the required privileges for a backup
     * 
     * @return
     * @throws JVixException
     * @throws CoreResultActionException
     */
    private Map<String, Boolean> checkRequiredBackupPrivileges(AbstractCoreResultActionBackup rab)
            throws VimPermissionException {
        Map<String, Boolean> methodAuhorization = new HashMap<>();
        try {
            final VimPrivilegeChecker privChecker = rab.getFirstClassObject().getVimConnection().getPrivilegeChecker();
            IFirstClassObject fco = rab.getFirstClassObject();
            methodAuhorization.putAll(privChecker.hasUserPrivilegesOnEntity(fco.getVimConnection().getRootFolder(),
                    PrivilegesList.PRIVILEGE_STORAGEPROFILE_VIEW));
            if (fco.getEntityType() == EntityType.VirtualMachine) {
                final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                methodAuhorization.putAll(privChecker.hasUserPrivilegesOnEntity(fco.getManageEntityInfo(),
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_PROVISIONING_GETVMFILE,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_STATE_CREATESNAPSHOT,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_STATE_REMOVESNAPSHOT,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_CONFIG_DISKLEASE,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_CONFIG_CHANGE_TRACKING,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_ALLOW_DISK_ACCESS,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_ALLOW_FILE_ACCESS,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_PROVISIONING_MARK_AS_TEMPLATE,
                        PrivilegesList.PRIVILEGE_VIRTUALMACHINE_PROVISIONING_MARK_AS_VM));
                methodAuhorization.putAll(privChecker.hasUserPrivilegesOnEntity(vmm.getDatastoreInfo(),
                        PrivilegesList.PRIVILEGE_DATASTORE_BROWSE, PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT));
                for (final ManagedEntityInfo moref : vmm.getConfig().getVmdkDatastores()) {
                    SimpleEntry<String, Boolean> per = privChecker.hasUserPrivilegeOnEntity(moref,
                            PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT);

                    if (Boolean.FALSE.equals(per.getValue())) {
                        methodAuhorization.put(PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT, false);
                        break;
                    }
                }
                methodAuhorization.putIfAbsent(PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT, Boolean.TRUE);
            } else if (fco.getEntityType() == EntityType.ImprovedVirtualDisk) {
                final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                methodAuhorization.putAll(privChecker.hasUserPrivilegesOnEntity(ivd.getDatastoreInfo().getMoref(),
                        PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT));
            } else {
                // do nothing
            }
        } catch (final RuntimeFaultFaultMsg | InvalidPropertyFaultMsg | VimObjectNotExistException e) {
            throw new VimPermissionException(e);
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return methodAuhorization;
    }

    protected boolean generationComputation(final CoreResultActionVappVirtualBackup rab,
            final FcoArchiveManager fcoArcMgr) throws CoreResultActionException {
        /**
         * Start Section GenerationComputation
         */
        rab.getInteractive().startGenerationComputation();
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info(MessagesTemplate.getLocationString(rab));
        }
        final GenerationProfile profile = rab.getProfile();
        final CoreResultActionPrepeareGeneration resultPrepareNewProfile = prepareNewGeneration(profile, rab);

        final GenerationInfo generationInfo = new GenerationInfo();
        generationInfo.setGenerationId(profile.getGenerationId());
        generationInfo.setPreviousGenerationId(profile.getPreviousGenerationId());
        generationInfo.setTargetUri(fcoArcMgr.getRepositoryTarget().getUri(profile.getGenerationPath()));
        generationInfo.setBackupMode(getOptions().getRequestedBackupMode());
        rab.setGenerationInfo(generationInfo);
        if (this.logger.isLoggable(Level.INFO)) {
            this.logger.info(MessagesTemplate.getGenerationInfo(rab));
        }
        rab.getInteractive().endGenerationComputation();
        /**
         * end Section GenerationComputation
         */

        return resultPrepareNewProfile.isSuccessful();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public AbstractCoreBackupOptions getOptions() {
        return this.options;
    }

    public VimConnection getVimConnection() {
        return this.vimConnection;
    }

    protected CoreResultActionPrepeareGeneration prepareNewGeneration(final GenerationProfile profGen,
            final AbstractCoreResultActionBackupRestore parent) throws CoreResultActionException {
        final FcoArchiveManager fcoArchive = profGen.getFcoArchiveManager();
        final CoreResultActionPrepeareGeneration result = new CoreResultActionPrepeareGeneration(parent);
        try {
            result.start();

            final ITargetOperation target = fcoArchive.getRepositoryTarget();

            result.setFcoEntityInfo(profGen.getFcoEntity());
            final FcoGenerationsCatalog profFco = fcoArchive.getGenerationsCatalog();
            final Integer newGenId = profFco.createNewGenerationId(profGen.getTimestamp().getTime(), BackupMode.FULL);
            /*
             * Create new generation id.
             */
            profGen.reconfigureGeneration(newGenId);

            result.setGenerationId(profGen.getGenerationId());
            result.setGenerationPath(profGen.getGenerationPath());
            result.setPreviousGenerationId(profGen.getPreviousGenerationId());

            target.createGenerationFolder(profGen);
            target.postGenerationProfile(profGen);
            fcoArchive.postGenerationsCatalog();
        } finally {
            result.done();
        }
        return result;

    }

    /**
     * Prepare a new archive generation for a FCD and set as current profile
     *
     * @param fco
     * @param profGen
     * @param parent
     * @param cal
     * @param vmArcMgr
     * @return
     * @throws CoreResultActionException
     */
    protected CoreResultActionPrepeareGeneration prepareNewGeneration(final GenerationProfileSpec spec,
            final GenerationProfile profGen, final AbstractCoreResultActionBackup parent)
            throws CoreResultActionException {
        final FcoArchiveManager fcoArchive = profGen.getFcoArchiveManager();
        final CoreResultActionPrepeareGeneration result = new CoreResultActionPrepeareGeneration(parent);
        try {
            result.start();

            final ITargetOperation target = fcoArchive.getRepositoryTarget();

            result.setFcoEntityInfo(spec.getFco().getFcoInfo());
            final FcoGenerationsCatalog profFco = fcoArchive.getGenerationsCatalog();
            /*
             * Create new generation id.
             */
            spec.setPrevGenId(profFco.getLatestSucceededGenerationId());
            final BackupMode backupMode = (spec.getBackupOptions().getRequestedBackupMode() == BackupMode.FULL)
                    ? BackupMode.FULL
                    : BackupMode.UNKNOW;
            spec.setGenId(profFco.createNewGenerationId(spec.getCalendar().getTimeInMillis(), backupMode));

            profGen.initializeGeneration(spec);
            result.setGenerationId(profGen.getGenerationId());
            result.setGenerationPath(profGen.getGenerationPath());
            result.setPreviousGenerationId(profGen.getPreviousGenerationId());

            target.createGenerationFolder(profGen);
            target.postGenerationProfile(profGen);
            fcoArchive.postGenerationsCatalog();
        } catch (final IOException | InvalidPropertyFaultMsg | com.vmware.pbm.RuntimeFaultFaultMsg
                | com.vmware.pbm.InvalidArgumentFaultMsg | PbmFaultFaultMsg | ProfileException
                | VimObjectNotExistException | com.vmware.vslm.RuntimeFaultFaultMsg | RuntimeFaultFaultMsg e) {
            result.failure(e);
            Utility.logWarning(this.logger, e);
        } catch (final InterruptedException e) {
            result.failure(e);
            this.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            result.done();
        }
        return result;

    }

}
