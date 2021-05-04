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
package com.vmware.safekeeping.core.soap;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager.TaskOperationType;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.DatacenterEventArgument;
import com.vmware.vim25.DatastoreEventArgument;
import com.vmware.vim25.EventEx;
import com.vmware.vim25.ExtendedFault;
import com.vmware.vim25.HostEventArgument;
import com.vmware.vim25.InvalidEventFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.KeyAnyValue;
import com.vmware.vim25.KeyValue;
import com.vmware.vim25.LocalizableMessage;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VmEventArgument;

public class VimTask {

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(VimTask.class.getName());

    private VimPortType vimPort;
    private ManagedObjectReference taskManager;
    private ManagedObjectReference eventManager;

    private TaskInfo taskInfo;

    private boolean available;

    private int previousUpdate;

    private String eventTypeId;

    private IFirstClassObject fco;

    private String faulTypeId;

    private String taskTypeId;
    private int eventId;

    private String eventFaultTypeId;
    // Create a task
    private static final String PERMISSION_TASK_CREATE = "Task.Create";
    // Update task
    private static final String PERMISSION_TASK_UPDATE = "Task.Update";

    public VimTask(IFirstClassObject fco, TaskOperationType t) {
        this.fco = fco;
        VimExtensionManager extMgmr = fco.getVimConnection().getExtensionManager();
        this.available = extMgmr.hasExtension() && extMgmr.hasTaskPermissionsGlobally();
        if (available) {
            Map<String, Boolean> privileges;
            try {
                ManagedObjectReference moref = (fco.getEntityType() == EntityType.ImprovedVirtualDisk)
                        ? ((ImprovedVirtualDisk) fco).getDatastoreInfo().getMoref()
                        : fco.getMoref();

                privileges = fco.getVimConnection().getPrivilegeChecker().hasUserPrivilegesOnEntity(moref,
                        PERMISSION_TASK_CREATE, PERMISSION_TASK_UPDATE);

                this.available = privileges != null && Boolean.TRUE.equals(privileges.get(PERMISSION_TASK_CREATE))
                        && Boolean.TRUE.equals(privileges.get(PERMISSION_TASK_UPDATE));

            } catch (RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
        this.previousUpdate = 0;

        if (this.available) {
            String entityStr = null;
            switch (fco.getEntityType()) {

            case ImprovedVirtualDisk:
                entityStr = ".ivd";
                break;
            case K8sNamespace:
                entityStr = ".k8s";
                break;
            case VirtualApp:
                entityStr = ".vapp";
                break;
            case VirtualMachine:
                entityStr = ".vm";
                break;

            default:
                return;
            }

            String key = "";
            switch (t) {
            case BACKUP:
                key = VimExtensionManager.EXTENSION_KEY + entityStr + VimExtensionManager.BACKUP_KEY;
                this.eventId = 1;
                break;
            case VIRTUALBACKUP:
                key = VimExtensionManager.EXTENSION_KEY + entityStr + VimExtensionManager.VIRTUALBACKUP_KEY;
                this.eventId = 2;
                break;
            case RESTORE:
                key = VimExtensionManager.EXTENSION_KEY + entityStr + VimExtensionManager.RESTORE_KEY;
                this.eventId = 3;
                break;
            default:
                logger.warning("Unsupported operation");
                return;
            }
            this.taskTypeId = key + "Task";
            this.eventTypeId = key + "Event";
            this.eventFaultTypeId = key + "FaultEvent";
            this.faulTypeId = key + "Fault";
            this.vimPort = fco.getVimConnection().vimPort;
            this.taskManager = fco.getVimConnection().getTaskManager();
            this.eventManager = fco.getVimConnection().getEventManager();
            try {
                if (fco.getEntityType() == EntityType.ImprovedVirtualDisk) {
                    this.taskInfo = this.vimPort.createTask(this.taskManager,
                            ((ImprovedVirtualDisk) fco).getDatastoreInfo().getMoref(), this.taskTypeId,
                            fco.getVimConnection().getUserSession().getUserName(), true, null, null);
                } else {
                    this.taskInfo = this.vimPort.createTask(this.taskManager, fco.getMoref(), this.taskTypeId,
                            fco.getVimConnection().getUserSession().getUserName(), true, null, null);
                }
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
                this.available = false;
            }
        }
    }

    private String composeEntityInfoName(final List<ManagedEntityInfo> entityInfoList) {
        final StringBuilder retString = new StringBuilder();
        if (!entityInfoList.isEmpty()) {
            for (int index = 1; index < (entityInfoList.size() - 1); index++) {
                retString.append(entityInfoList.get(index).getName());
                retString.append('/');
            }
            retString.append(entityInfoList.get(entityInfoList.size() - 1).getName());
        }
        return retString.toString();
    }

    public void done(ICoreResultAction result) {
        if (this.available) {
            try {
                final EventEx event = new EventEx();
                if (result.isDone() && result.isSuccessful()) {

                    updateDescription("");
                    this.vimPort.setTaskState(this.taskInfo.getTask(), TaskInfoState.SUCCESS, null, null);
                    this.taskInfo.setProgress(Utility.ONE_HUNDRED_PER_CENT_AS_INT);
                    this.taskInfo.setState(TaskInfoState.SUCCESS);
                    event.setEventTypeId(this.eventTypeId);
                } else {
                    final ExtendedFault fault = new ExtendedFault();
                    final KeyValue e = new KeyValue();
                    e.setKey("result");
                    e.setValue(result.getReason());
                    fault.getData().add(e);
                    fault.setFaultTypeId(this.faulTypeId);
                    final LocalizedMethodFault faultCause = new LocalizedMethodFault();
                    faultCause.setFault(fault);
                    this.taskInfo.setError(faultCause);
                    this.vimPort.setTaskState(this.taskInfo.getTask(), TaskInfoState.ERROR, null, faultCause);
                    event.setEventTypeId(this.eventFaultTypeId);
                    event.setFault(faultCause);
                }

                final GregorianCalendar g = new GregorianCalendar();
                event.setCreatedTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(g));
                final DatacenterEventArgument de = new DatacenterEventArgument();
                de.setDatacenter(this.fco.getDatacenterInfo().getMoref());
                de.setName(composeEntityInfoName(getManagedEntityInfoPath(this.fco.getDatacenterInfo())));
                event.setDatacenter(de);
                event.setKey(this.eventId);
                if (this.fco.getEntityType() == EntityType.VirtualMachine) {
                    final DatastoreEventArgument ds = new DatastoreEventArgument();
                    final ManagedEntityInfo dsInfo = ((VirtualMachineManager) this.fco).getDatastoreInfo();
                    ds.setDatastore(dsInfo.getMoref());
                    ds.setName(composeEntityInfoName(getManagedEntityInfoPath(dsInfo)));
                    event.setDs(ds);

                    final HostEventArgument hs = new HostEventArgument();
                    final ManagedEntityInfo hostInfo = ((VirtualMachineManager) this.fco).getHostInfo();
                    hs.setHost(hostInfo.getMoref());
                    hs.setName(hostInfo.getName());
                    event.setHost(hs);

                    final VmEventArgument vs = new VmEventArgument();
                    vs.setVm(this.fco.getMoref());
                    vs.setName(this.fco.getName());
                    event.setVm(vs);
                } else if (this.fco.getEntityType() == EntityType.ImprovedVirtualDisk) {
                    final DatastoreEventArgument ds = new DatastoreEventArgument();
                    ds.setDatastore(((ImprovedVirtualDisk) this.fco).getDatastoreInfo().getMoref());
                    event.setDs(ds);
                } else {
                    logger.info("vApp has no datastore");
                }

                // if (fco.getEntityType() == EntityType.VirtualMachine) {
                // ComputeResourceEventArgument re = new ComputeResourceEventArgument();
                //
                // ManagedEntityInfo hostInfo = ((VirtualMachineManager)
                // fco).getDatastoreInfo();
                // re.setComputeResource(((VirtualMachineManager)
                // fco).get.getRuntimeInfo().getHost());
                // event.setComputeResource(re);
                // } else {
                // logger.info("vApp and IVD have no host");
                // }
                event.setChainId(this.taskInfo.getEventChainId());
                event.setUserName(this.fco.getVimConnection().getUserSession().getUserName());
                event.setSeverity("info");
                final KeyAnyValue e = new KeyAnyValue();
                e.setKey("total");
                e.setValue("2000");
                event.setFullFormattedMessage("Maybe works [data.total]");
                event.setObjectId(this.fco.getMoref().getValue());
                event.setObjectName(this.fco.getName());
                event.setObjectType(this.fco.getMoref().getType());
                event.getArguments().add(e);

                this.vimPort.postEvent(this.eventManager, event, this.taskInfo);
            } catch (InvalidStateFaultMsg | RuntimeFaultFaultMsg | InvalidEventFaultMsg | DatatypeConfigurationException
                    | VimObjectNotExistException | InvalidPropertyFaultMsg e) {
                Utility.logWarning(logger, e);
            } catch (final InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }

    }

    private List<ManagedEntityInfo> getManagedEntityInfoPath(final ManagedEntityInfo rpInfo)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        List<ManagedEntityInfo> result = null;
        switch (this.fco.getType()) {
        case ivd:
        case k8s:
            result = new ArrayList<>();
            break;
        case vapp:
            result = ((VirtualAppManager) this.fco).getManagedEntityInfoPath(rpInfo);
            break;
        case vm:
            result = ((VirtualMachineManager) this.fco).getManagedEntityInfoPath(rpInfo);
            break;
        }
        return result;
    }

    public TaskInfo getTaskInfo() {
        return this.taskInfo;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void start() {
        if (this.available) {
            try {
                this.vimPort.setTaskState(this.taskInfo.getTask(), TaskInfoState.RUNNING, null, null);
                this.taskInfo.setState(TaskInfoState.RUNNING);
            } catch (InvalidStateFaultMsg | RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
    }

    public void update(Float f) {
        update(f.intValue());
    }

    public void update(int i) {
        if (this.available && (i > (this.previousUpdate + 5))) {
            try {
                this.vimPort.updateProgress(this.taskInfo.getTask(), i);
                this.previousUpdate = i;
                this.taskInfo.setProgress(i);
            } catch (InvalidStateFaultMsg | RuntimeFaultFaultMsg | OutOfBoundsFaultMsg e) {
                Utility.logWarning(logger, e);
            }

        }
    }

    public void updateDescription(final String descriptionMsg) {
        if (available) {
            try {
                final LocalizableMessage locMsg = new LocalizableMessage();
                locMsg.setKey(this.taskInfo.getKey() + ".details");
                locMsg.setMessage(descriptionMsg);
                this.taskInfo.setDescription(locMsg);

                this.vimPort.setTaskDescription(this.taskInfo.getTask(), locMsg);
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
    }
}
