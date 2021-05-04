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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.TagModel;
import com.vmware.safekeeping.common.BiosUuid;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GlobalFcoProfileCatalog;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.FcoTarget;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.K8s;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vim25.ID;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;

public abstract class AbstractCommandWithOptions implements ICommand {
    protected static final Pattern IP4PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    protected static final Pattern UUIDPATTERN = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    protected Logger logger;

    protected CoreBasicCommandOptions options;

    protected AbstractCommandWithOptions() {

        this.logger = Logger.getLogger(getLogName());
        initialize();
    }

    private List<IFirstClassObject> getAnyImprovedVirtualDisk(final ConnectionManager connetionManager)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {

        final List<IFirstClassObject> result = new LinkedList<>();
        final List<ImprovedVirtualDisk> ivdList = connetionManager.getAllIvdList();
        if (ivdList.isEmpty()) {
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.warning("No Improved Virtual Disks are available");
            }
        } else {
            int i = 0;
            for (final ImprovedVirtualDisk ivd : ivdList) {
                ++i;
                if (this.logger.isLoggable(Level.INFO)) {
                    this.logger.info(String.format("%d: %s %s %s ", i, ivd.getUuid(), ivd.getName(),
                            ivd.getDatastoreInfo().toString()));
                }
                result.add(ivd);
            }
        }
        return result;
    }

    private List<VirtualAppManager> getAnyVapp(final ConnectionManager connetionManager) {
        final String vmFilter = CoreGlobalSettings.getVmFilter();
        return connetionManager.getAllVAppList(this.options.getVim(), vmFilter);

    }

    private List<VirtualMachineManager> getAnyVirtualMachine(final ConnectionManager connetionManager) {
        final String vmFilter = CoreGlobalSettings.getVmFilter();
        final List<VirtualMachineManager> result = new LinkedList<>();
        final List<VirtualMachineManager> vmmList = connetionManager.getAllVmList(this.options.getVim(), vmFilter);

        if (vmmList.isEmpty()) {
            final String msg = "No virtual machine available with filter " + vmFilter;
            this.logger.warning(msg);

        } else {
            Map<String, VStorageObjectAssociations> vStorageObjectAssociations = null;
            try {
                vStorageObjectAssociations = retrieveVStorageObjectAssociations(connetionManager);
            } catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
                Utility.logWarning(this.logger, e);
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            for (final VirtualMachineManager vmm : vmmList) {
                if ((vStorageObjectAssociations != null) && !vStorageObjectAssociations.isEmpty()) {
                    searchIvdAssociation(vStorageObjectAssociations, vmm);
                }
                /*
                 * Check if the VM is this backup server if true skip this server
                 */
                if (CoreGlobalSettings.excludeBackupServer()
                        && (vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getInstance().getServerBiosUuid())
                                || vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getInstance().getBigEndianBiosUuid()))) {
                    final String msg = String.format("Backup server uuid:%s removed from the backup list",
                            vmm.getBiosUuid());
                    this.logger.info(msg);
                } else if (vmm.getManagedBy() != null) {
                    final String msg = String.format("Virtual Machine managed by %s - skipped",
                            vmm.getManagedBy().getExtensionKey());
                    this.logger.info(msg);
                } else {
                    result.add(vmm);
                }
            }
        }
        return result;
    }

    protected List<IFirstClassObject> getFcoTarget(final ConnectionManager connetionManager, final int filter) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager, int - start"); //$NON-NLS-1$
        }

        final List<IFirstClassObject> result = new LinkedList<>();
        boolean tagEvaluated = false;
        try {
            if ((filter & FirstClassObjectFilterType.all) > 0) {
                if ((filter & FirstClassObjectFilterType.noTag) == 0) {
                    result.addAll(getTagTarget(connetionManager, filter));
                }
                if ((filter & FirstClassObjectFilterType.vm) > 0) {
                    result.addAll(getAnyVirtualMachine(connetionManager));
                }
                if ((filter & FirstClassObjectFilterType.ivd) > 0) {
                    result.addAll(getAnyImprovedVirtualDisk(connetionManager));
                }
                if ((filter & FirstClassObjectFilterType.vapp) > 0) {
                    result.addAll(getAnyVapp(connetionManager));
                }
            }

            if ((filter & FirstClassObjectFilterType.vm) > 0) {
                if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
                    result.addAll(getTagTarget(connetionManager, filter));
                    tagEvaluated = true;
                }
                result.addAll(getVmTarget(connetionManager));
            }

            if ((filter & FirstClassObjectFilterType.ivd) > 0) {
                if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
                    result.addAll(getTagTarget(connetionManager, filter));
                    tagEvaluated = true;
                }
                result.addAll(getIvdTarget(connetionManager));
            }
            if ((filter & FirstClassObjectFilterType.vapp) > 0) {
                if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
                    result.addAll(getTagTarget(connetionManager, filter));
                    tagEvaluated = true;
                }
                result.addAll(getvAppTarget(connetionManager));
            }
            if ((filter & FirstClassObjectFilterType.k8s) > 0) {
                if (!tagEvaluated && ((filter & FirstClassObjectFilterType.noTag) == 0)) {
                    result.addAll(getTagTarget(connetionManager, filter));

                }
                result.addAll(getK8sTarget(connetionManager));
            }
        } catch (final InterruptedException e) {
            this.logger.log(Level.WARNING, "Interrupted!", e);
            Thread.currentThread().interrupt();

        } catch (InvalidPropertyFaultMsg | SafekeepingException | RuntimeFaultFaultMsg e) {
            Utility.logWarning(this.logger, e);
        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ConnectionManager, int - end"); //$NON-NLS-1$
        }
        return result;
    }

    private LinkedList<IFirstClassObject> getIvdTarget(final ConnectionManager connetionManager) {
        final Map<String, FcoTarget> ivdTarget = getOptions().getTargetFcoList();
        final LinkedList<IFirstClassObject> result = new LinkedList<>();

        if (ivdTarget.isEmpty()) {
            this.logger.warning("No Improved Virtual Disk specified ");
        } else {

            for (final Entry<String, FcoTarget> entry : ivdTarget.entrySet()) {
                String vim = null;
                if (StringUtils.isEmpty(this.options.getVim())
                        || this.options.getVim().equals(entry.getValue().getVcenterUuid())) {
                    vim = entry.getValue().getVcenterUuid();
                } else {
                    if (this.logger.isLoggable(Level.INFO)) {
                        final String st = String.format("vcenter mismatch between option (%s) and target (%s)",
                                this.options.getVim(), entry.getValue().getVcenterUuid());
                        this.logger.info(st);
                    }
                    continue;
                }
                switch (entry.getValue().getKeyType()) {
                case IVD_NAME:
                    final List<ImprovedVirtualDisk> ivdList = new LinkedList<>(
                            connetionManager.findIvdByName(vim, entry.getKey()));
                    result.addAll(ivdList);
                    break;
                case IVD_UUID:
                    final ImprovedVirtualDisk ivd = connetionManager.findIvdById(vim, entry.getKey());
                    if (ivd != null) {
                        result.add(ivd);
                    }
                    break;
                default:
                    continue;
                }

            }
        }
        return result;
    }

    private LinkedList<IFirstClassObject> getK8sTarget(final ConnectionManager connetionManager) {
        final Map<String, FcoTarget> k8sTarget = getOptions().getTargetFcoList();
        final LinkedList<IFirstClassObject> result = new LinkedList<>();

        if (k8sTarget.isEmpty()) {
            this.logger.warning("No Kubernetics domain specified");

        } else {
            for (final Entry<String, FcoTarget> entry : k8sTarget.entrySet()) {

                switch (entry.getValue().getKeyType()) {
                case K8S_NAME:
                    final K8s k8 = new K8s(connetionManager, entry.getKey());
                    result.add(k8);
                    break;
                case K8S_UUID:
                    break;
                default:
                    continue;
                }

            }
        }
        return result;

    }

    protected abstract String getLogName();

    public CoreBasicCommandOptions getOptions() {
        return this.options;
    }

    private LinkedList<IFirstClassObject> getTagTarget(final ConnectionManager connetionManager, final int filter)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, SafekeepingException {
        final LinkedList<IFirstClassObject> result = new LinkedList<>();
        final Map<String, FcoTarget> tagTarget = getOptions().getTargetFcoList();

        if (tagTarget.size() == 0) {
            if ((filter & FirstClassObjectFilterType.tag) > 0) {
                this.logger.fine("No tag specified");
            }

        } else {
            final List<TagModel> tags = connetionManager.listTags(this.options.getVim(), tagTarget.keySet());
            for (final TagModel tag : tags) {
                final List<DynamicID> attachedObject = connetionManager.listTagAttachedObjects(this.options.getVim(),
                        tag.getId());
                for (final DynamicID id : attachedObject) {
                    if (((filter & FirstClassObjectFilterType.vm) > 0)
                            && StringUtils.equals(id.getType(), EntityType.VirtualMachine.toString())) {
                        final VirtualMachineManager vmm = new VirtualMachineManager(
                                connetionManager.getVimConnection(this.options.getVim()),
                                MorefUtil.newManagedObjectReference(EntityType.VirtualMachine, id.getId()));
                        result.add(vmm);
                    } else if (((filter & FirstClassObjectFilterType.vapp) > 0)
                            && StringUtils.equals(id.getType(), EntityType.VirtualApp.toString())) {
                        final VirtualAppManager vapp = new VirtualAppManager(
                                connetionManager.getVimConnection(this.options.getVim()),
                                MorefUtil.newManagedObjectReference(EntityType.VirtualApp, id.getId()));
                        result.add(vapp);
                    } else if (((filter & FirstClassObjectFilterType.ivd) > 0)
                            && StringUtils.equals(id.getType(), EntityType.ImprovedVirtualDisk.toString())) {
                        // TODO insert IVD tag support
                    } else {
                        throw new SafekeepingException("Invalid filter");
                    }

                }
            }
        }
        return result;
    }

    protected List<ManagedFcoEntityInfo> getTargetFcoEntitiesFromRepository(
            final GlobalFcoProfileCatalog globalFcoCatalog) {
        final LinkedList<ManagedFcoEntityInfo> result = new LinkedList<>();
        if ((this.options.getAnyFcoOfType() & FirstClassObjectFilterType.all) > 0) {
            if ((this.options.getAnyFcoOfType() & FirstClassObjectFilterType.vm) > 0) {

                result.addAll(globalFcoCatalog.getAllEntities(EntityType.VirtualMachine));
            }

            if ((this.options.getAnyFcoOfType() & FirstClassObjectFilterType.vapp) > 0) {

                result.addAll(globalFcoCatalog.getAllEntities(EntityType.VirtualApp));
            }
            if ((this.options.getAnyFcoOfType() & FirstClassObjectFilterType.ivd) > 0) {

                result.addAll(globalFcoCatalog.getAllEntities(EntityType.ImprovedVirtualDisk));
            }
        } else {

            final Map<String, FcoTarget> fcoTarget = getOptions().getTargetFcoList();

            if (!fcoTarget.isEmpty()) {
                ManagedFcoEntityInfo entity = null;
                for (final Entry<String, FcoTarget> entry : fcoTarget.entrySet()) {
                    switch (entry.getValue().getKeyType()) {
                    case VM_IP:
                        break;
                    case VM_MOREF:
                        entity = globalFcoCatalog.getEntityByMoref(entry.getKey(), EntityType.VirtualMachine);
                        if (entity == null) {
                            final String msg = String.format("No Virtual Machine with Moref %s exist on the repository",
                                    entry);
                            this.logger.warning(msg);
                        } else {
                            result.add(entity);
                        }
                        break;
                    case VM_NAME:
                        if ((entity = globalFcoCatalog.getEntityByName(entry.getKey(),
                                EntityType.VirtualMachine)) == null) {
                            final String msg = String.format("No Virtual Machine named %s exist on the repository",
                                    entry);
                            this.logger.warning(msg);
                        } else {
                            result.add(entity);
                        }
                        break;
                    case VAPP_MOREF:
                        if ((entity = globalFcoCatalog.getEntityByMoref(entry.getKey(),
                                EntityType.VirtualApp)) == null) {
                            final String msg = String.format("No VirtualApp with Moref %s exist on the repository",
                                    entry);
                            this.logger.warning(msg);
                        } else {
                            result.add(entity);
                        }
                        break;
                    case VAPP_NAME:
                        if ((entity = globalFcoCatalog.getEntityByName(entry.getKey(),
                                EntityType.VirtualApp)) == null) {
                            final String msg = String.format("No VirtualApp named %s exist on the repository", entry);
                            this.logger.warning(msg);
                        } else {
                            result.add(entity);
                        }
                        break;
                    case IVD_NAME:
                        if ((entity = globalFcoCatalog.getEntityByName(entry.getKey(),
                                EntityType.ImprovedVirtualDisk)) == null) {
                            final String msg = String
                                    .format("No Improved Virtual Disk named %s exist on the repository", entry);
                            this.logger.warning(msg);
                        } else {
                            result.add(entity);
                        }
                        break;
                    case VM_UUID:
                    case IVD_UUID:
                    case VAPP_UUID:
                        if ((entity = globalFcoCatalog.getEntityByUuid(entry.getKey())) == null) {
                            final String msg = String.format(
                                    "No Improved Virtual Disk  with SystemUuid %s exist on the repository", entry);
                            this.logger.warning(msg);
                        } else {
                            result.add(entity);
                        }
                        break;
                    default:
                        break;

                    }
                }
            } else {
                final String msg = "No valid target specified ";
                this.logger.warning(msg);
            }

        }

        return result;
    }

    protected List<IFirstClassObject> getTargetFcoFromRepository(final ConnectionManager connectionManager,
            final GlobalFcoProfileCatalog globalFcoCatalog) {
        final LinkedList<IFirstClassObject> result = new LinkedList<>();
        final List<ManagedFcoEntityInfo> entities = getTargetFcoEntitiesFromRepository(globalFcoCatalog);
        for (final ManagedFcoEntityInfo entityInfo : entities) {
            final String vim = StringUtils.isEmpty(getOptions().getVim()) ? entityInfo.getServerUuid()
                    : getOptions().getVim();
            VimConnection vimConnection = connectionManager.getVimConnection(vim);
            if (vimConnection == null) {
                final String msg = String.format("No vCenter with UUID %s exist. Switch to default vcenter %s",
                        entityInfo.getServerUuid(), connectionManager.getDefualtVcenter().getServerIntanceUuid());
                this.logger.warning(msg);
                vimConnection = connectionManager.getDefualtVcenter();
            }
            IFirstClassObject fco = null;
            switch (entityInfo.getEntityType()) {
            case VirtualMachine:
                fco = new VirtualMachineManager(vimConnection, entityInfo);
                break;
            case ImprovedVirtualDisk:
                fco = new ImprovedVirtualDisk(vimConnection, entityInfo);
                break;
            case VirtualApp:
                fco = new VirtualAppManager(vimConnection, entityInfo);
                break;
            default:
                break;
            }
            if (fco != null) {
                result.add(fco);
            }
        }

        return result;
    }

    private LinkedList<IFirstClassObject> getvAppTarget(final ConnectionManager connetionManager) {
        final Map<String, FcoTarget> vappTargets = getOptions().getTargetFcoList();
        final LinkedList<IFirstClassObject> result = new LinkedList<>();
        for (final Entry<String, FcoTarget> entry : vappTargets.entrySet()) {

            VirtualAppManager vApp;
            String vim = null;
            if (StringUtils.isEmpty(this.options.getVim())
                    || this.options.getVim().equals(entry.getValue().getVcenterUuid())) {
                vim = entry.getValue().getVcenterUuid();
            } else {
                if (this.logger.isLoggable(Level.INFO)) {
                    final String st = String.format("vcenter mismatch between option (%s) and target (%s)",
                            this.options.getVim(), entry.getValue().getVcenterUuid());
                    this.logger.info(st);
                }
                continue;
            }
            switch (entry.getValue().getKeyType()) {

            case VAPP_MOREF:
                vApp = connetionManager.findVAppByMoref(vim,
                        MorefUtil.newManagedObjectReference(EntityType.VirtualApp, entry.getKey()));
                break;
            case VAPP_NAME:
                vApp = connetionManager.findVAppByName(vim, entry.getKey());
                break;
            case VAPP_UUID:
                vApp = connetionManager.findVAppByUuid(vim, entry.getKey());
                break;
            default:
                continue;
            }
            if (vApp != null) {
                result.add(vApp);
            }
        }

        return result;
    }

    private LinkedList<IFirstClassObject> getVmTarget(final ConnectionManager connetionManager) {
        final LinkedList<IFirstClassObject> result = new LinkedList<>();
        final Map<String, FcoTarget> vmTargets = getOptions().getTargetFcoList();
        if (!vmTargets.isEmpty()) {

            Map<String, VStorageObjectAssociations> vStorageObjectAssociations = null;
            try {
                vStorageObjectAssociations = retrieveVStorageObjectAssociations(connetionManager);
            } catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
                Utility.logWarning(this.logger, e);
            } catch (final InterruptedException e) {
                this.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
            for (final Entry<String, FcoTarget> entry : vmTargets.entrySet()) {

                VirtualMachineManager vmm = null;
                String vim = null;
                if (StringUtils.isEmpty(this.options.getVim())
                        || this.options.getVim().equals(entry.getValue().getVcenterUuid())) {
                    vim = entry.getValue().getVcenterUuid();
                } else {
                    if (this.logger.isLoggable(Level.INFO)) {
                        final String st = String.format("vcenter mismatch between option (%s) and target (%s)",
                                this.options.getVim(), entry.getValue().getVcenterUuid());
                        this.logger.info(st);
                    }
                    continue;
                }
                switch (entry.getValue().getKeyType()) {
                case VM_IP:
                    vmm = connetionManager.findVmByIp(vim, entry.getKey());
                    break;
                case VM_MOREF:
                    vmm = connetionManager.findVmByMoref(vim,
                            MorefUtil.newManagedObjectReference(EntityType.VirtualMachine, entry.getKey()));
                    break;
                case VM_NAME:
                    vmm = connetionManager.findVmByName(vim, entry.getKey());
                    break;
                case VM_UUID:
                    vmm = connetionManager.findVmByUuid(vim, entry.getKey(), true);
                    break;
                default:
                    continue;
                }
                if (vmm != null) {
                    if (CoreGlobalSettings.excludeBackupServer()
                            && (vmm.getBiosUuid().equalsIgnoreCase(BiosUuid.getInstance().getServerBiosUuid()) || vmm
                                    .getBiosUuid().equalsIgnoreCase(BiosUuid.getInstance().getBigEndianBiosUuid()))) {
                        final String msg = String.format(
                                "Backup server uuid:%s removed from the list of available target", vmm.getBiosUuid());
                        this.logger.warning(msg);
                    } else {
                        result.add(vmm);
                        if (vStorageObjectAssociations != null) {
                            searchIvdAssociation(vStorageObjectAssociations, vmm);
                        }
                    }
                }

            }

        }
        return result;
    }

    protected abstract void initialize();

    private Map<String, VStorageObjectAssociations> retrieveVStorageObjectAssociations(
            final ConnectionManager connetionManager)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final Map<String, VStorageObjectAssociations> result = new HashMap<>();
        final List<VStorageObjectAssociations> p = connetionManager.retrieveVStorageObjectAssociations();
        for (final VStorageObjectAssociations a : p) {
            if (a.getVmDiskAssociations() == null) {
                continue;
            }
            for (final VStorageObjectAssociationsVmDiskAssociations associated : a.getVmDiskAssociations()) {
                result.put(associated.getVmId(), a);
            }
        }
        return result;
    }

    private List<ID> searchIvdAssociation(final Map<String, VStorageObjectAssociations> vStorageObjectAssociations,
            final VirtualMachineManager vmm) {
        final List<ID> result = new LinkedList<>();
        if (vStorageObjectAssociations.containsKey(vmm.getMorefValue())) {
            final ID id = vStorageObjectAssociations.get(vmm.getMorefValue()).getId();
            final List<VStorageObjectAssociationsVmDiskAssociations> vmDiskAssociation = vStorageObjectAssociations
                    .get(vmm.getMorefValue()).getVmDiskAssociations();
            for (final VStorageObjectAssociationsVmDiskAssociations disk : vmDiskAssociation) {
                if (disk.getVmId().equalsIgnoreCase(vmm.getMorefValue())) {
                    vmm.getVStorageObjectAssociations().put(disk.getDiskKey(), id);
                }
            }

            result.add(id);
        }
        return result;
    }

    public void setOptions(final CoreBasicCommandOptions options) {
        this.options = options;
    }

}
