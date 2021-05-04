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
package com.vmware.safekeeping.core.soap.managers;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.vmware.safekeeping.common.BiosUuid;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.ExtensionManagerOptions;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.enums.ExtensionManagerOperation;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.internal.util.StringUtils;
import com.vmware.vim25.Description;
import com.vmware.vim25.EventEventSeverity;
import com.vmware.vim25.ExtExtendedProductInfo;
import com.vmware.vim25.Extension;
import com.vmware.vim25.ExtensionEventTypeInfo;
import com.vmware.vim25.ExtensionFaultTypeInfo;
import com.vmware.vim25.ExtensionHealthInfo;
import com.vmware.vim25.ExtensionResourceInfo;
import com.vmware.vim25.ExtensionServerInfo;
import com.vmware.vim25.ExtensionTaskTypeInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.KeyValue;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;

public class VimExtensionManager {
    public enum TaskOperationType {
        BACKUP, RESTORE, VIRTUALBACKUP

    }

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(VimExtensionManager.class.getName());

    private static final String ENGLISH_LOCALIZATION = "en";
    private static final String SPANISH_LOCALIZATION = "es";

    private static final String VAPP_KEY = ".vapp";
    private static final String VM_KEY = ".vm";
    private static final String IVD_KEY = ".ivd";
    private static final String TASK_KEY = "Task";
    private static final String EVENT_KEY = "Event";
    private static final String FAULT_EVENT_KEY = "FaultEvent";
    private static final String FAULT_KEY = "Fault";

    public static final String EXTENSION_KEY = "com.vmware.safekeeping";
    public static final String BACKUP_KEY = "Backup";
    public static final String RESTORE_KEY = "Restore";
    public static final String VIRTUALBACKUP_KEY = "Virtual Backup";

    public static final String BACKUP_VM_TASK = EXTENSION_KEY + VM_KEY + BACKUP_KEY + TASK_KEY;
    public static final String VIRTUALBACKUP_VM_TASK = EXTENSION_KEY + VM_KEY + VIRTUALBACKUP_KEY + TASK_KEY;
    public static final String RESTORE_VM_TASK = EXTENSION_KEY + VM_KEY + RESTORE_KEY + TASK_KEY;
    public static final String BACKUP_VAPP_TASK = EXTENSION_KEY + VAPP_KEY + BACKUP_KEY + TASK_KEY;
    public static final String VIRTUALBACKUP_VAPP_TASK = EXTENSION_KEY + VAPP_KEY + VIRTUALBACKUP_KEY + TASK_KEY;
    public static final String RESTORE_VAPP_TASK = EXTENSION_KEY + VAPP_KEY + RESTORE_KEY + TASK_KEY;
    public static final String BACKUP_IVD_TASK = EXTENSION_KEY + IVD_KEY + BACKUP_KEY + TASK_KEY;
    public static final String VIRTUALBACKUP_IVD_TASK = EXTENSION_KEY + IVD_KEY + VIRTUALBACKUP_KEY + TASK_KEY;
    public static final String RESTORE_IVD_TASK = EXTENSION_KEY + IVD_KEY + RESTORE_KEY + TASK_KEY;

    public static final String BACKUP_VM_EVENT = EXTENSION_KEY + VM_KEY + BACKUP_KEY + EVENT_KEY;
    public static final String VIRTUALBACKUP_VM_EVENT = EXTENSION_KEY + VM_KEY + VIRTUALBACKUP_KEY + EVENT_KEY;
    public static final String RESTORE_VM_EVENT = EXTENSION_KEY + VM_KEY + RESTORE_KEY + EVENT_KEY;
    public static final String BACKUP_VAPP_EVENT = EXTENSION_KEY + VAPP_KEY + BACKUP_KEY + EVENT_KEY;
    public static final String VIRTUALBACKUP_VAPP_EVENT = EXTENSION_KEY + VAPP_KEY + VIRTUALBACKUP_KEY + EVENT_KEY;
    public static final String RESTORE_VAPP_EVENT = EXTENSION_KEY + VAPP_KEY + RESTORE_KEY + EVENT_KEY;
    public static final String BACKUP_IVD_EVENT = EXTENSION_KEY + IVD_KEY + BACKUP_KEY + EVENT_KEY;
    public static final String VIRTUALBACKUP_IVD_EVENT = EXTENSION_KEY + IVD_KEY + VIRTUALBACKUP_KEY + EVENT_KEY;
    public static final String RESTORE_IVD_EVENT = EXTENSION_KEY + IVD_KEY + RESTORE_KEY + EVENT_KEY;

    public static final String BACKUP_VM_FAULT_EVENT = EXTENSION_KEY + VM_KEY + BACKUP_KEY + FAULT_EVENT_KEY;
    public static final String VIRTUALBACKUP_VM_FAULT_EVENT = EXTENSION_KEY + VM_KEY + VIRTUALBACKUP_KEY
            + FAULT_EVENT_KEY;
    public static final String RESTORE_VM_FAULT_EVENT = EXTENSION_KEY + VM_KEY + RESTORE_KEY + FAULT_EVENT_KEY;
    public static final String BACKUP_VAPP_FAULT_EVENT = EXTENSION_KEY + VAPP_KEY + BACKUP_KEY + FAULT_EVENT_KEY;
    public static final String VIRTUALBACKUP_VAPP_FAULT_EVENT = EXTENSION_KEY + VAPP_KEY + VIRTUALBACKUP_KEY
            + FAULT_EVENT_KEY;
    public static final String RESTORE_VAPP_FAULT_EVENT = EXTENSION_KEY + VAPP_KEY + RESTORE_KEY + FAULT_EVENT_KEY;
    public static final String BACKUP_IVD_FAULT_EVENT = EXTENSION_KEY + IVD_KEY + BACKUP_KEY + FAULT_EVENT_KEY;
    public static final String VIRTUALBACKUP_IVD_FAULT_EVENT = EXTENSION_KEY + IVD_KEY + VIRTUALBACKUP_KEY
            + FAULT_EVENT_KEY;
    public static final String RESTORE_IVD_FAULT_EVENT = EXTENSION_KEY + IVD_KEY + RESTORE_KEY + FAULT_EVENT_KEY;

    public static final String BACKUP_VM_FAULT = EXTENSION_KEY + VM_KEY + BACKUP_KEY + FAULT_KEY;
    public static final String VIRTUALBACKUP_VM_FAULT = EXTENSION_KEY + VM_KEY + VIRTUALBACKUP_KEY + FAULT_KEY;
    public static final String RESTORE_VM_FAULT = EXTENSION_KEY + VM_KEY + RESTORE_KEY + FAULT_KEY;
    public static final String BACKUP_VAPP_FAULT = EXTENSION_KEY + VAPP_KEY + BACKUP_KEY + FAULT_KEY;
    public static final String VIRTUALBACKUP_VAPP_FAULT = EXTENSION_KEY + VAPP_KEY + VIRTUALBACKUP_KEY + FAULT_KEY;
    public static final String RESTORE_VAPP_FAULT = EXTENSION_KEY + VAPP_KEY + RESTORE_KEY + FAULT_KEY;
    public static final String BACKUP_IVD_FAULT = EXTENSION_KEY + IVD_KEY + BACKUP_KEY + FAULT_KEY;
    public static final String VIRTUALBACKUP_IVD_FAULT = EXTENSION_KEY + IVD_KEY + VIRTUALBACKUP_KEY + FAULT_KEY;
    public static final String RESTORE_IVD_FAULT = EXTENSION_KEY + IVD_KEY + RESTORE_KEY + FAULT_KEY;

    private static final String PERMISSION_EXTENSION_REGISTER = "Extension.Register";
    private static final String PERMISSION_EXTENSION_UPDATE = "Extension.Update";
    private static final String PERMISSION_EXTENSION_UNREGISTER = "Extension.Unregister";
    // Create a task
    private static final String PERMISSION_TASK_CREATE = "Task.Create";
    // Update task
//    private static final String PERMISSION_TASK_UPDATE = "Task.Update";

    // Log event
    private static final String PERMISSION_GLOBAL_EVENT = "Global.LogEvent";
    private Extension extension;
    private final ManagedObjectReference em;

    private final VimConnection parent;
    private Map<String, Boolean> privileges;

    private final UserSession userSession;
    private ExtensionManagerOptions extensionOp;
    private final VimPortType vimPort;

    public VimExtensionManager(final VimConnection parent) throws RuntimeFaultFaultMsg {
        super();
        this.parent = parent;
        this.vimPort = parent.getVimPort();
        this.em = parent.getServiceContent().getExtensionManager();
        this.extension = parent.getVimPort().findExtension(this.em, EXTENSION_KEY);

        this.userSession = parent.getUserSession();
    }

    public Extension action(final ExtensionManagerOperation extensionOperation, final boolean force,
            final ExtensionManagerOptions extensionOp) throws RuntimeFaultFaultMsg, DatatypeConfigurationException,
            NotFoundFaultMsg, VimOperationException, VimPermissionException {
        if (extensionOperation != null) {
            if (extensionOp != null) {
                this.extensionOp = extensionOp;
            }
            switch (extensionOperation) {
            case REGISTER:
                return register(force);

            case REMOVE:
                return remove(force);

            case UPDATE:
                return update(false);

            case FORCE_UPDATE:
                return update(true);
            case CHECK:
                return this.vimPort.findExtension(this.em, EXTENSION_KEY);
            default:
                break;
            }
        }
        return null;
    }

    private void addEventResourceInfo(final ExtensionResourceInfo eri, final EventEventSeverity severity,
            final String event, final String label) {
        eri.getData().add(getKeyValue(event + ".category", severity.value()));
        eri.getData().add(getKeyValue(event + ".formatOnComputeResource", ""));
        eri.getData().add(getKeyValue(event + ".formatOnDatacenter", ""));
        eri.getData().add(getKeyValue(event + ".formatOnHost", ""));
        eri.getData().add(getKeyValue(event + ".formatOnVm", ""));
        eri.getData().add(getKeyValue(event + ".fullFormat", label));

    }

    void addEvents(final Extension extension) {
        extension.getEventList().add(getExtensionEventTypeInfo(BACKUP_VM_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(VIRTUALBACKUP_VM_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(RESTORE_VM_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(BACKUP_VAPP_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(VIRTUALBACKUP_VAPP_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(RESTORE_VAPP_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(BACKUP_IVD_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(VIRTUALBACKUP_IVD_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(RESTORE_IVD_EVENT));

        extension.getEventList().add(getExtensionEventTypeInfo(BACKUP_VM_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(VIRTUALBACKUP_VM_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(RESTORE_VM_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(BACKUP_VAPP_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(VIRTUALBACKUP_VAPP_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(RESTORE_VAPP_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(BACKUP_IVD_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(VIRTUALBACKUP_IVD_FAULT_EVENT));
        extension.getEventList().add(getExtensionEventTypeInfo(RESTORE_IVD_FAULT_EVENT));

//resources

        final ExtensionResourceInfo eri = new ExtensionResourceInfo();
        eri.setLocale(ENGLISH_LOCALIZATION);
        eri.setModule(EVENT_KEY);
        addEventResourceInfo(eri, EventEventSeverity.INFO, BACKUP_VM_EVENT,
                "Backup Virtual Machine Total data:[data.total]");
        addEventResourceInfo(eri, EventEventSeverity.INFO, BACKUP_VAPP_EVENT, "Backup vApp Total data:[data.total]");
        addEventResourceInfo(eri, EventEventSeverity.INFO, BACKUP_IVD_EVENT,
                "Backup Improved Virtual Disk Total data:[data.total]");
        addEventResourceInfo(eri, EventEventSeverity.INFO, RESTORE_VM_EVENT, "Restore Virtual Machine");
        addEventResourceInfo(eri, EventEventSeverity.INFO, RESTORE_VAPP_EVENT, "Restore vApp");
        addEventResourceInfo(eri, EventEventSeverity.INFO, RESTORE_IVD_EVENT, "Restore Improved Virtual Disk");
        addEventResourceInfo(eri, EventEventSeverity.INFO, VIRTUALBACKUP_VM_EVENT,
                "Virtual Backup Virtual Machine Backup");
        addEventResourceInfo(eri, EventEventSeverity.INFO, VIRTUALBACKUP_VAPP_EVENT, "Virtual Backup vApp Backup");
        addEventResourceInfo(eri, EventEventSeverity.INFO, VIRTUALBACKUP_IVD_EVENT,
                "Virtual Backup Improved Virtual Disk Backup");

        addEventResourceInfo(eri, EventEventSeverity.ERROR, BACKUP_VM_FAULT_EVENT,
                "Backup Virtual Machine Total data:[data.total]");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, BACKUP_VAPP_FAULT_EVENT,
                "Backup vApp Total data:[data.total]");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, BACKUP_IVD_FAULT_EVENT,
                "Backup Improved Virtual Disk Total data:[data.total]");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, RESTORE_VM_FAULT_EVENT, "Restore Virtual Machine");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, RESTORE_VAPP_FAULT_EVENT, "Restore vApp");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, RESTORE_IVD_FAULT_EVENT, "Restore Improved Virtual Disk");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, VIRTUALBACKUP_VM_FAULT_EVENT,
                "Virtual Backup Virtual Machine Backup");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, VIRTUALBACKUP_VAPP_FAULT_EVENT,
                "Virtual Backup vApp Backup");
        addEventResourceInfo(eri, EventEventSeverity.ERROR, VIRTUALBACKUP_IVD_FAULT_EVENT,
                "Virtual Backup Improved Virtual Disk Backup");

        extension.getResourceList().add(eri);
        final ExtensionResourceInfo eriEs = new ExtensionResourceInfo();
        eriEs.setLocale(SPANISH_LOCALIZATION);
        eriEs.setModule(EVENT_KEY);
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, BACKUP_VM_EVENT, "Máquina virtual copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, BACKUP_VAPP_EVENT, "vApp copia de respaldo ");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, BACKUP_IVD_EVENT,
                "Improved Virtual Disk copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, RESTORE_VM_EVENT,
                "Máquina virtual restaurar datos guardados");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, RESTORE_VAPP_EVENT, "vApp restaurar datos guardados");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, RESTORE_IVD_EVENT,
                "Improved Virtual Disk restaurar datos guardados");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, VIRTUALBACKUP_VM_EVENT,
                "Máquina virtual consolidar copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, VIRTUALBACKUP_VAPP_EVENT,
                "vApp consolidar copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.INFO, VIRTUALBACKUP_IVD_EVENT,
                "Improved Virtual Disk consolidar copia de respaldo");

        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, BACKUP_VM_FAULT_EVENT,
                "Máquina virtual copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, BACKUP_VAPP_FAULT_EVENT, "vApp copia de respaldo ");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, BACKUP_IVD_FAULT_EVENT,
                "Improved Virtual Disk copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, RESTORE_VM_FAULT_EVENT,
                "Máquina virtual restaurar datos guardados");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, RESTORE_VAPP_FAULT_EVENT,
                "vApp restaurar datos guardados");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, RESTORE_IVD_FAULT_EVENT,
                "Improved Virtual Disk restaurar datos guardados");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, VIRTUALBACKUP_VM_FAULT_EVENT,
                "Máquina virtual consolidar copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, VIRTUALBACKUP_VAPP_FAULT_EVENT,
                "vApp consolidar copia de respaldo");
        addEventResourceInfo(eriEs, EventEventSeverity.ERROR, VIRTUALBACKUP_IVD_FAULT_EVENT,
                "Improved Virtual Disk consolidar copia de respaldo");

        extension.getResourceList().add(eriEs);

    }

    void addExtensionInfo(final Extension extension) {
        final ExtensionResourceInfo eri = new ExtensionResourceInfo();
        eri.setLocale(ENGLISH_LOCALIZATION);
        eri.setModule("extension");
        addExtensionResourceInfo(eri, SafekeepingVersion.PRODUCT_NAME, SafekeepingVersion.PRODUCT_DESCRIPTION);
        extension.getResourceList().add(eri);
        // Spanish
        final ExtensionResourceInfo eriEs = new ExtensionResourceInfo();
        eriEs.setLocale(SPANISH_LOCALIZATION);
        eriEs.setModule("extension");
        addExtensionResourceInfo(eriEs, SafekeepingVersion.PRODUCT_NAME,
                "Resguardo de la copia de seguridad de VMware de código abierto");
        extension.getResourceList().add(eriEs);

    }

    private void addExtensionResourceInfo(final ExtensionResourceInfo eri, final String label, final String summary) {
        eri.getData().add(getKeyValue(EXTENSION_KEY + ".label", label));
        eri.getData().add(getKeyValue(EXTENSION_KEY + ".summary", summary));
        eri.getData().add(getKeyValue(EXTENSION_KEY + ".server.management.label", label));
        eri.getData().add(getKeyValue(EXTENSION_KEY + ".server.management.summary", summary));

    }

    private void addFaultResourceInfo(final ExtensionResourceInfo eri, final String task, final String label) {
        eri.getData().add(getKeyValue(task + ".summary", label));
    }

    void addFaults(final Extension extension) {
        extension.getFaultList().add(getExtensionFaultTypeInfo(BACKUP_VM_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(VIRTUALBACKUP_VM_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(RESTORE_VM_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(BACKUP_VAPP_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(VIRTUALBACKUP_VAPP_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(RESTORE_VAPP_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(BACKUP_IVD_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(VIRTUALBACKUP_IVD_FAULT));
        extension.getFaultList().add(getExtensionFaultTypeInfo(RESTORE_IVD_FAULT));

//resources

        final ExtensionResourceInfo eri = new ExtensionResourceInfo();
        eri.setLocale(ENGLISH_LOCALIZATION);
        eri.setModule(FAULT_KEY);
        addFaultResourceInfo(eri, BACKUP_VM_FAULT, "Backup Virtual Machine failed: [data.result]");
        addFaultResourceInfo(eri, BACKUP_VAPP_FAULT, "Backup vApp failed: [data.result]");
        addFaultResourceInfo(eri, BACKUP_IVD_FAULT, "Backup Improved Virtual Disk failed: [data.result]");
        addFaultResourceInfo(eri, RESTORE_VM_FAULT, "Restore Virtual Machine failed: [data.result]");
        addFaultResourceInfo(eri, RESTORE_VAPP_FAULT, "Restore vApp failed: [data.result]");
        addFaultResourceInfo(eri, RESTORE_IVD_FAULT, "Restore Improved Virtual Disk failed: [data.result]");
        addFaultResourceInfo(eri, VIRTUALBACKUP_VM_FAULT,
                "Virtual Backup Virtual Machine Backup failed: [data.result]");
        addFaultResourceInfo(eri, VIRTUALBACKUP_VAPP_FAULT, "Virtual Backup vApp Backup failed: [data.result]");
        addFaultResourceInfo(eri, VIRTUALBACKUP_IVD_FAULT,
                "Virtual Backup Improved Virtual Disk Backup failed: [data.result]");

        extension.getResourceList().add(eri);
        final ExtensionResourceInfo eriEs = new ExtensionResourceInfo();
        eriEs.setLocale(SPANISH_LOCALIZATION);
        eriEs.setModule(FAULT_KEY);
        addFaultResourceInfo(eriEs, BACKUP_VM_FAULT, "Máquina virtual copia de respaldo fallido: [data.result]");
        addFaultResourceInfo(eriEs, BACKUP_VAPP_FAULT, "vApp copia de respaldo  fallido: [data.result]");
        addFaultResourceInfo(eriEs, BACKUP_IVD_FAULT, "Improved Virtual Disk copia de respaldo fallido: [data.result]");
        addFaultResourceInfo(eriEs, RESTORE_VM_FAULT,
                "Máquina virtual restaurar datos guardados fallido: [data.result]");
        addFaultResourceInfo(eriEs, RESTORE_VAPP_FAULT, "vApp restaurar datos guardados fallido: [data.result]");
        addFaultResourceInfo(eriEs, RESTORE_IVD_FAULT,
                "Improved Virtual Disk restaurar datos guardados fallido: [data.result]");
        addFaultResourceInfo(eriEs, VIRTUALBACKUP_VM_FAULT,
                "Máquina virtual consolidar copia de respaldo fallido: [data.result]");
        addFaultResourceInfo(eriEs, VIRTUALBACKUP_VAPP_FAULT,
                "vApp consolidar copia de respaldo fallido: [data.result]");
        addFaultResourceInfo(eriEs, VIRTUALBACKUP_IVD_FAULT,
                "Improved Virtual Disk consolidar copia de respaldo fallido: [data.result]");

        extension.getResourceList().add(eriEs);

    }

    private void addTaskResourceInfo(final ExtensionResourceInfo eri, final String task, final String label) {
        eri.getData().add(getKeyValue(task + ".label", label));
        eri.getData().add(getKeyValue(task + ".summary", label));
    }

    void addTasks(final Extension extension) {
        extension.getTaskList().add(getExtensionTaskTypeInfo(BACKUP_VM_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(VIRTUALBACKUP_VM_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(RESTORE_VM_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(BACKUP_VAPP_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(VIRTUALBACKUP_VAPP_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(RESTORE_VAPP_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(BACKUP_IVD_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(VIRTUALBACKUP_IVD_TASK));
        extension.getTaskList().add(getExtensionTaskTypeInfo(RESTORE_IVD_TASK));

//resources

        final ExtensionResourceInfo eri = new ExtensionResourceInfo();
        eri.setLocale(ENGLISH_LOCALIZATION);
        eri.setModule(TASK_KEY);
        addTaskResourceInfo(eri, BACKUP_VM_TASK, "Backup Virtual Machine");
        addTaskResourceInfo(eri, BACKUP_VAPP_TASK, "Backup vApp");
        addTaskResourceInfo(eri, BACKUP_IVD_TASK, "Backup Improved Virtual Disk");
        addTaskResourceInfo(eri, RESTORE_VM_TASK, "Restore Virtual Machine");
        addTaskResourceInfo(eri, RESTORE_VAPP_TASK, "Restore vApp");
        addTaskResourceInfo(eri, RESTORE_IVD_TASK, "Restore Improved Virtual Disk");
        addTaskResourceInfo(eri, VIRTUALBACKUP_VM_TASK, "Virtual Backup Virtual Machine Backup");
        addTaskResourceInfo(eri, VIRTUALBACKUP_VAPP_TASK, "Virtual Backup vApp Backup");
        addTaskResourceInfo(eri, VIRTUALBACKUP_IVD_TASK, "Virtual Backup Improved Virtual Disk Backup");

        extension.getResourceList().add(eri);
        final ExtensionResourceInfo eriEs = new ExtensionResourceInfo();
        eriEs.setLocale(SPANISH_LOCALIZATION);
        eriEs.setModule(TASK_KEY);
        addTaskResourceInfo(eriEs, BACKUP_VM_TASK, "Máquina virtual copia de respaldo");
        addTaskResourceInfo(eriEs, BACKUP_VAPP_TASK, "vApp copia de respaldo ");
        addTaskResourceInfo(eriEs, BACKUP_IVD_TASK, "Improved Virtual Disk copia de respaldo");
        addTaskResourceInfo(eriEs, RESTORE_VM_TASK, "Máquina virtual restaurar datos guardados");
        addTaskResourceInfo(eriEs, RESTORE_VAPP_TASK, "vApp restaurar datos guardados");
        addTaskResourceInfo(eriEs, RESTORE_IVD_TASK, "Improved Virtual Disk restaurar datos guardados");
        addTaskResourceInfo(eriEs, VIRTUALBACKUP_VM_TASK, "Máquina virtual consolidar copia de respaldo");
        addTaskResourceInfo(eriEs, VIRTUALBACKUP_VAPP_TASK, "vApp consolidar copia de respaldo");
        addTaskResourceInfo(eriEs, VIRTUALBACKUP_IVD_TASK, "Improved Virtual Disk consolidar copia de respaldo");
        extension.getResourceList().add(eriEs);

    }

    /**
     * Create the Extension object
     *
     * @return
     * @throws DatatypeConfigurationException
     */
    private Extension createExtension() throws DatatypeConfigurationException {
        final Extension ext = new Extension();

        ext.setDescription(getProductDescription());
        ext.setKey(EXTENSION_KEY);
        ext.setVersion(SafekeepingVersion.getInstance().getVersion());
        ext.setLastHeartbeatTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        ext.setCompany(SafekeepingVersion.COMPANY);
        if (this.extensionOp != null) {
            if (StringUtils.isNotBlank(this.extensionOp.getHealthInfoUrl())) {
                final ExtensionHealthInfo healthInfo = new ExtensionHealthInfo();
                healthInfo.setUrl(this.extensionOp.getHealthInfoUrl());
                ext.setHealthInfo(healthInfo);
            }
            if (StringUtils.isNotBlank(this.extensionOp.getServerInfoUrl())) {
                ext.getServer().add(getExtensionServerInfo(this.extensionOp.getServerInfoUrl(),
                        this.extensionOp.getServerThumbprint()));

                ext.setExtendedProductInfo(getFullExtExtendedProductInfo());
                ext.setShownInSolutionManager(true);
            }
        } else {
            ext.setExtendedProductInfo(getSimpleExtExtendedProductInfo());
            ext.setShownInSolutionManager(false);
        }
        addExtensionInfo(ext);
        addTasks(ext);
        addEvents(ext);
        addFaults(ext);
        return ext;
    }

    private ExtensionEventTypeInfo getExtensionEventTypeInfo(final String eventId) {
        final ExtensionEventTypeInfo extensionEventTypeInfo = new ExtensionEventTypeInfo();
        extensionEventTypeInfo.setEventID(eventId);
        return extensionEventTypeInfo;
    }

    private ExtensionFaultTypeInfo getExtensionFaultTypeInfo(final String faultId) {
        final ExtensionFaultTypeInfo extensionFaultTypeInfo = new ExtensionFaultTypeInfo();
        extensionFaultTypeInfo.setFaultID(faultId);
        return extensionFaultTypeInfo;
    }

    private ExtensionServerInfo getExtensionServerInfo(final String url, final String serverThumbprint) {
        final ExtensionServerInfo serverInfo = new ExtensionServerInfo();
        serverInfo.getAdminEmail().add("noreply@vmware.com");
        serverInfo.setCompany("VMware, Inc.");
        serverInfo.setDescription(getProductDescription());
        serverInfo.setType("SOAP");
        serverInfo.setUrl(url);
        if (StringUtils.isNotBlank(serverThumbprint)) {
            serverInfo.setServerThumbprint(serverThumbprint);
        }
        return serverInfo;
    }

    private ExtensionTaskTypeInfo getExtensionTaskTypeInfo(final String taskId) {
        final ExtensionTaskTypeInfo extensionTaskTypeInfo = new ExtensionTaskTypeInfo();
        extensionTaskTypeInfo.setTaskID(taskId);
        return extensionTaskTypeInfo;
    }

    /**
     *
     * @return
     */
    private ExtExtendedProductInfo getFullExtExtendedProductInfo() {
        final ExtExtendedProductInfo extProductInfo = getSimpleExtExtendedProductInfo();
        final String uuid = BiosUuid.getInstance().getBigEndianBiosUuid();
        VirtualMachineManager vm = null;
        try {
            vm = this.parent.getFind().findVmByUuid(uuid, false);
        } catch (final InvalidPropertyFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        if (vm != null) {
            extProductInfo.setSelf(vm.getMoref());
        }
        return extProductInfo;
    }

    private KeyValue getKeyValue(final String key, final String value) {
        final KeyValue kv = new KeyValue();
        kv.setKey(key);
        kv.setValue(value);
        return kv;
    }

    public Map<String, Boolean> getPrivileges() {
        if (this.privileges == null) {
            try {
                this.privileges = this.parent.getPrivilegeChecker().hasUserPrivilegesOnEntity(this.em,
                        PERMISSION_EXTENSION_REGISTER, PERMISSION_EXTENSION_UPDATE, PERMISSION_EXTENSION_UNREGISTER,
                        PERMISSION_TASK_CREATE, // PERMISSION_TASK_UPDATE,
                        PERMISSION_GLOBAL_EVENT);
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
        return this.privileges;
    }

    private Description getProductDescription() {
        final Description desc = new Description();
        desc.setLabel(SafekeepingVersion.PRODUCT_NAME);
        desc.setSummary(SafekeepingVersion.PRODUCT_DESCRIPTION);
        return desc;
    }

    private ExtExtendedProductInfo getSimpleExtExtendedProductInfo() {
        final ExtExtendedProductInfo extProductInfo = new ExtExtendedProductInfo();
        extProductInfo.setCompanyUrl(SafekeepingVersion.COMPANY_URL);
        extProductInfo.setProductUrl(SafekeepingVersion.PRODUCT_URL);
        return extProductInfo;
    }

    public boolean hasExtension() {
        return this.extension != null;
    }

    public boolean hasTaskPermissionsGlobally() {
        return (getPrivileges() != null) && Boolean.TRUE.equals(getPrivileges().get(PERMISSION_TASK_CREATE))
//                && Boolean.TRUE.equals(this.getPrivileges().get(PERMISSION_TASK_UPDATE))
                && Boolean.TRUE.equals(getPrivileges().get(PERMISSION_GLOBAL_EVENT));
    }

    public Extension register(final boolean force)
            throws RuntimeFaultFaultMsg, DatatypeConfigurationException, VimOperationException, VimPermissionException {
        if (Boolean.TRUE.equals(getPrivileges().get(PERMISSION_EXTENSION_REGISTER)) || force) {
            this.extension = this.vimPort.findExtension(this.em, EXTENSION_KEY);
            if (this.extension == null) {
                this.vimPort.registerExtension(this.em, createExtension());
                this.extension = this.vimPort.findExtension(this.em, EXTENSION_KEY);
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Extension %s ver:%s created", EXTENSION_KEY,
                            this.extension.getVersion());
                    logger.info(msg);
                }
            } else {
                final String msg = String.format("Extension %s ver:%s already exist", EXTENSION_KEY,
                        this.extension.getVersion());
                logger.warning(msg);
                throw new VimOperationException(msg);
            }
        } else {
            final String msg = String.format("User %s not entitle with the privileges %s",
                    this.userSession.getUserName(), PERMISSION_EXTENSION_REGISTER);
            logger.warning(msg);
            throw new VimPermissionException(msg);
        }
        return this.extension;
    }

    public Extension remove(final boolean force)
            throws RuntimeFaultFaultMsg, NotFoundFaultMsg, VimOperationException, VimPermissionException {
        if (Boolean.TRUE.equals(getPrivileges().get(PERMISSION_EXTENSION_UNREGISTER)) || force) {
            this.extension = this.vimPort.findExtension(this.em, EXTENSION_KEY);
            if (this.extension != null) {
                this.vimPort.unregisterExtension(this.em, EXTENSION_KEY);
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Extension %s removed", EXTENSION_KEY);
                    logger.info(msg);
                }
                return this.extension;
            } else {
                final String msg = String.format("Extension %s  doesn't exist", EXTENSION_KEY);
                logger.warning(msg);
                throw new VimOperationException(msg);
            }
        } else {
            final String msg = String.format("User %s not entitle with the privileges %s",
                    this.userSession.getUserName(), PERMISSION_EXTENSION_UNREGISTER);
            logger.warning(msg);
            throw new VimPermissionException(msg);
        }
    }

    public Extension update(final boolean force) throws RuntimeFaultFaultMsg, DatatypeConfigurationException,
            NotFoundFaultMsg, VimPermissionException, VimOperationException {
        if (Boolean.TRUE.equals(getPrivileges().get(PERMISSION_EXTENSION_UPDATE)) || force) {
            this.extension = this.vimPort.findExtension(this.em, EXTENSION_KEY);
            if ((this.extension != null) && (force
                    || !this.extension.getVersion().contentEquals(SafekeepingVersion.getInstance().getVersion()))) {
                final String prevVersion = this.extension.getVersion();
                this.vimPort.updateExtension(this.em, createExtension());
                this.extension = this.vimPort.findExtension(this.em, EXTENSION_KEY);
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Extension %s updated from ver:%s to ver:%s", EXTENSION_KEY,
                            prevVersion, this.extension.getVersion());
                    logger.info(msg);
                }
            } else {
                final String msg = String.format("Extension %s doesn't exist", EXTENSION_KEY);
                logger.warning(msg);
                throw new VimOperationException(msg);
            }
        } else {
            final String msg = String.format("User %s not entitle with the privileges %s",
                    this.userSession.getUserName(), PERMISSION_EXTENSION_UPDATE);
            logger.warning(msg);
            throw new VimPermissionException(msg);
        }
        return this.extension;
    }
}
