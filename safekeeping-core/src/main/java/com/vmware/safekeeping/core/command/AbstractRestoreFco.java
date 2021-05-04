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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreRestoreOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGenerateRestoreManagedInfo;
import com.vmware.safekeeping.core.command.results.CoreResultActionLoadProfile;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.ovf.SerializableVAppConfigInfo;
import com.vmware.safekeeping.core.profile.ovf.SerializableVmConfigInfo;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.managers.PrivilegesList;
import com.vmware.safekeeping.core.soap.managers.VimPrivilegeChecker;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

abstract class AbstractRestoreFco {
    protected final Logger logger;
    private final CoreRestoreOptions options;
    private final VimConnection vimConnection;

    private final Map<String, Boolean> methodAuhorization;

    /**
     * Constructor
     * 
     * @param vimConnection
     * @param options
     * @param logger
     */
    AbstractRestoreFco(final VimConnection vimConnection, final CoreRestoreOptions options, final Logger logger) {
        this.options = options;
        this.vimConnection = vimConnection;
        this.logger = logger;
        this.methodAuhorization = new HashMap<>();
    }

    protected CoreResultActionLoadProfile actionLoadProfile(final FcoArchiveManager vmArcMgr,
            final AbstractCoreResultActionRestore rar) throws CoreResultActionException {
        final CoreResultActionLoadProfile result = new CoreResultActionLoadProfile(rar);

        try {
            result.start();
            GenerationProfile profile = vmArcMgr.loadProfileGeneration(this.options.getGenerationId());
            result.setProfile(profile);
            final GenerationInfo generationInfo = new GenerationInfo();
            generationInfo.setGenerationId(profile.getGenerationId());
            generationInfo.setPreviousGenerationId(profile.getPreviousGenerationId());
            generationInfo.setTargetUri(vmArcMgr.getRepositoryTarget().getUri(profile.getGenerationPath()));
            generationInfo.setBackupMode(profile.getBackupMode());
            result.setGenerationInfo(generationInfo);
            if ((result.getProfile() == null) || !result.getProfile().isProfileValid()) {
                final String msg = String.format("GenerationId %d doesn't exist.", this.options.getGenerationId());
                result.failure(msg);
                this.logger.warning(msg);
            } else {
                if (!result.getProfile().isSucceeded()) {
                    final String msg = String.format("GenerationId %d is marked FAILED.",
                            this.options.getGenerationId());
                    result.failure(msg);
                    this.logger.warning(msg);
                }
            }

        } catch (final IOException e) {
            result.failure(e);
            Utility.logWarning(this.logger, e);
        } finally {
            result.done();
            rar.setProfile(result.getProfile());
            rar.setGenerationInfo(result.getGenerationInfo());
        }
        return result;
    }

    protected boolean checkPrivileges(AbstractCoreResultActionRestore rar) throws VimPermissionException {
        final List<String> failedCheck = new ArrayList<>();
        for (final Entry<String, Boolean> entry : checkRestorePrivileges(rar).entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                failedCheck.add(entry.getKey());
            }
        }
        if (!failedCheck.isEmpty()) {
            throw new VimPermissionException(
                    "User lack of the following privileges:" + StringUtils.join(failedCheck, ","));
        }
        List<String> failedOptionalCheck = new ArrayList<>();
        for (Entry<String, Boolean> entry : checkOptionalPrivileges(rar).entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                failedOptionalCheck.add(entry.getKey());
            }
        }
        if (!failedOptionalCheck.isEmpty()) {
            try {
                if (!CoreGlobalSettings.skipEncryptionCheck() && vimConnection.isEncryptionEnabled()
                        && rar.getProfile().isEncrypted()) {
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
     * @throws CoreResultActionException
     * @throws JVixException
     */
    private Map<String, Boolean> checkOptionalPrivileges(AbstractCoreResultActionRestore rar)
            throws VimPermissionException {
        try {
            final VimPrivilegeChecker privChecker = rar.getFirstClassObject().getVimConnection().getPrivilegeChecker();
            final CoreRestoreManagedInfo rmi = rar.getManagedInfo();
            if (rar.getEntityType() == EntityType.VirtualMachine) {
                this.methodAuhorization.putAll(privChecker.checkPrivilegesOnEntities(
                        new ManagedEntityInfo[] { rmi.getResourcePollInfo(), rmi.getFolderInfo() },
                        new String[] { PrivilegesList.PRIVILEGE_CRYPTOGRAPHER_ACCESS,
                                PrivilegesList.PRIVILEGE_CRYPTOGRAPHER_ENCRYPT_NEW }));
            }
        } catch (final RuntimeFaultFaultMsg e) {
            throw new VimPermissionException(e);
        }
        return this.methodAuhorization;
    }

    /**
     * Check the required privileges for a backup
     * 
     * @param rar
     * @return
     * @throws CoreResultActionException
     */
    private Map<String, Boolean> checkRestorePrivileges(AbstractCoreResultActionRestore rar)
            throws VimPermissionException {
        try {
            final VimPrivilegeChecker privChecker = rar.getFirstClassObject().getVimConnection().getPrivilegeChecker();
            final CoreRestoreManagedInfo rmi = rar.getManagedInfo();
            switch (rar.getEntityType()) {
            case VirtualMachine:
                this.methodAuhorization.putAll(privChecker.checkPrivilegesOnEntities(
                        new ManagedEntityInfo[] { rmi.getResourcePollInfo(), rmi.getFolderInfo() },
                        new String[] { PrivilegesList.PRIVILEGE_VIRTUALMACHINE_INVENTORY_CREATE,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_INVENTORY_DELETE,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_CONFIG_SETTINGS,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_CONFIG_RESOURCE,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_CONFIG_ADD_NEW_DISK,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_CONFIG_ADVANCEDCONFIG,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_INTERACT_POWERON,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_INTERACT_POWEROFF,
                                PrivilegesList.PRIVILEGE_RESOURCE_ASSIGNVMTOPOOL,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_ALLOW_DISK_ACCESS,
                                PrivilegesList.PRIVILEGE_VIRTUALMACHINE_ALLOW_FILE_ACCESS }));
                this.methodAuhorization.putAll(privChecker.checkPrivilegesOnEntity(rmi.getDatastoreInfo(),
                        new String[] { PrivilegesList.PRIVILEGE_DATASTORE_BROWSE,
                                PrivilegesList.PRIVILEGE_DATASTORE_ALLOCATE_SPACE,
                                PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT }));
                this.methodAuhorization.putAll(privChecker.checkPrivilegesOnEntities(rmi.getNetworkMapping(),
                        new String[] { PrivilegesList.PRIVILEGE_NETWORK_ASSIGN }));
                break;
            case VirtualApp:
                this.methodAuhorization.putAll(privChecker.checkPrivilegesOnEntity(rmi.getResourcePollInfo(),
                        new String[] { PrivilegesList.PRIVILEGE_VAPP_CREATE, PrivilegesList.PRIVILEGE_VAPP_POWERON,
                                PrivilegesList.PRIVILEGE_VAPP_POWEROFF, PrivilegesList.PRIVILEGE_VAPP_ASSIGN_VM,
                                PrivilegesList.PRIVILEGE_VAPP_ASSIGN_RESOURCEGROUP,
                                PrivilegesList.PRIVILEGE_VAPP_ASSIGN_VAPP,
                                PrivilegesList.PRIVILEGE_VAPP_INSTANCE_CONFIG, PrivilegesList.PRIVILEGE_VAPP_RENAME,
                                PrivilegesList.PRIVILEGE_VAPP_RESOURCECONFIG,
                                PrivilegesList.PRIVILEGE_VAPP_INSTANCECONFIG,
                                PrivilegesList.PRIVILEGE_VAPP_APPLICATIONCONFIG,
                                PrivilegesList.PRIVILEGE_VAPP_DELETE }));
                break;
            case ImprovedVirtualDisk:
                this.methodAuhorization.putAll(privChecker.checkPrivilegesOnEntity(rmi.getDatastoreInfo(),
                        new String[] { PrivilegesList.PRIVILEGE_DATASTORE_ALLOCATE_SPACE,
                                PrivilegesList.PRIVILEGE_DATASTORE_FILEMANAGEMENT }));
                break;
            default:
                throw new VimPermissionException("Unsupported Type");
            }

        } catch (final RuntimeFaultFaultMsg e) {
            throw new VimPermissionException(e);
        }
        return this.methodAuhorization;
    }

    public CoreRestoreOptions getOptions() {
        return this.options;
    }

    protected String getRestoreFcoName(final ManagedFcoEntityInfo fcoEntity, final Integer index) {
        String result = fcoEntity.getName();
        if (!getOptions().isRecover() && ((fcoEntity.getEntityType() != EntityType.VirtualMachine)
                || !getOptions().isAllowDuplicatedVmNamesInsideVapp() || !getOptions().isParentAVApp())) {
            if (StringUtils.isEmpty(getOptions().getName()) && StringUtils.isEmpty(getOptions().getPostfix())
                    && StringUtils.isEmpty(getOptions().getPrefix())) {
                final Calendar cal = Calendar.getInstance();
                result = String.format("%s_Restored_%d%02d%02d%02d%02d%02d", result, cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
            } else {
                final String tmpResult = String.format("%s%s%s", getOptions().getPrefix(),
                        StringUtils.isEmpty(getOptions().getName()) ? fcoEntity.getName() : getOptions().getName(),
                        getOptions().getPostfix());
                if ((index != null) && tmpResult.contains("%")) {
                    result = String.format(tmpResult, index);
                } else {
                    result = tmpResult;
                }
            }
        }
        return result;
    }

    public VimConnection getVimConnection() {
        return this.vimConnection;
    }

    protected SerializableVAppConfigInfo restoreVAppOvfConfig(final GenerationProfile profile,
            final CoreResultActionGenerateRestoreManagedInfo resultAction) throws CoreResultActionException {
        SerializableVAppConfigInfo vAppConfig = null;
        if (resultAction.getEntityType() == EntityType.VirtualApp) {
            if (profile.isVappConfigAvailable()) {
                final String vAppConfigPath = profile.getvAppConfigPath();
                final ITargetOperation target = profile.getTargetOperation();

                if (!target.doesObjectExist(vAppConfigPath)) {
                    resultAction.failure(vAppConfigPath + " content doesn't exist");
                } else {
                    try {
                        if (this.logger.isLoggable(Level.INFO)) {
                            final String msg = String.format("Get vAppConfig: %s from %s", vAppConfigPath,
                                    target.getTargetName());
                            this.logger.info(msg);
                        }
                        final byte[] buffer = target.getObject(vAppConfigPath);
                        vAppConfig = new ObjectMapper().readValue(buffer, SerializableVAppConfigInfo.class);

                    } catch (final IOException e) {
                        resultAction.failure(e);
                    }
                }
            }
        } else {
            throw new CoreResultActionException(resultAction.getFcoEntityInfo());
        }
        return vAppConfig;
    }

    protected SerializableVmConfigInfo restoreVmOvfConfig(final GenerationProfile profile,
            final CoreResultActionGenerateRestoreManagedInfo resultAction) throws CoreResultActionException {
        SerializableVmConfigInfo vAppConfig = null;
        if (resultAction.getEntityType() == EntityType.VirtualMachine) {
            if (profile.isVappConfigAvailable()) {
                final String vAppConfigPath = profile.getvAppConfigPath();
                final ITargetOperation target = profile.getTargetOperation();

                if (!target.doesObjectExist(vAppConfigPath)) {
                    resultAction.failure(vAppConfigPath + " content doesn't exist");
                } else {
                    try {
                        if (this.logger.isLoggable(Level.INFO)) {
                            final String msg = String.format("Get vAppConfig: %s from %s", vAppConfigPath,
                                    target.getTargetName());
                            this.logger.info(msg);
                        }
                        final byte[] buffer = target.getObject(vAppConfigPath);

                        vAppConfig = new ObjectMapper().readValue(buffer, SerializableVmConfigInfo.class);

                    } catch (final IOException e) {
                        resultAction.failure(e);
                    }
                }
            }
        } else {
            throw new CoreResultActionException(resultAction.getFcoEntityInfo());
        }
        return vAppConfig;
    }

}
