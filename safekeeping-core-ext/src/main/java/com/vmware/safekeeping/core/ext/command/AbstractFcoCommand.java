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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionCbt;
import com.vmware.safekeeping.core.command.results.miscellanea.CoreResultActionDestroy;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionClone;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionVappImport;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.CustomizationFaultFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TimedoutFaultMsg;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vslm.RuntimeFaultFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public abstract class AbstractFcoCommand extends AbstractCommandWithOptions {

    private String datacenterName;
    private String datastoreName;
    private String folderName;
    private String hostName;
    private boolean isNoVmdk;
    private String name;
    private boolean powerOn;

    private String resourcePoolName;
    private String resPoolFilter;

    private String vmFolderFilter;

    private String[] vmNetworksName;
    private URL urlPath;
    protected boolean vappImport;
    protected boolean clone;
    protected boolean remove;
    protected Boolean cbt;

    private final RunningReport cloneReport;

    protected AbstractFcoCommand(final RunningReport cloneReport) {
        this.cloneReport = cloneReport;
        initialize();
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
                    resultAction.aborted("Aborted on user request");
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    try {
                        resultAction.setFcoEntityInfo(fco.getFcoInfo());
                        resultAction.setFlagState(this.cbt);
                        resultAction.setPreviousFlagState(fco.isChangedBlockTrackingEnabled());
                        if (!getOptions().isDryRun() && !fco.setChangeBlockTracking(this.cbt)) {
                            resultAction.failure();

                        }
                    } catch (final InvalidPropertyFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg | FileFaultFaultMsg
                            | InvalidNameFaultMsg | InvalidStateFaultMsg | SnapshotFaultFaultMsg
                            | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | InvalidCollectorVersionFaultMsg
                            | VimTaskException | ConcurrentAccessFaultMsg | DuplicateNameFaultMsg
                            | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | NotFoundFaultMsg
                            | com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.InvalidStateFaultMsg
                            | com.vmware.vslm.NotFoundFaultMsg | RuntimeFaultFaultMsg | VslmFaultFaultMsg e) {
                        Utility.logWarning(this.logger, e);
                        resultAction.failure(e);
                    } catch (final InterruptedException e) {
                        resultAction.failure(e);
                        this.logger.log(Level.WARNING, "Interrupted!", e);
                        // Restore interrupted state...
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

    protected List<CoreResultActionClone> actionClone(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        final List<CoreResultActionClone> result = new ArrayList<>();
        final List<IFirstClassObject> targetList = getFcoTarget(connetionManager, getOptions().getAnyFcoOfType());

        for (final IFirstClassObject fco : targetList) {
            try (final CoreResultActionClone resultAction = new CoreResultActionClone()) {
                resultAction.start();

                if (Vmbk.isAbortTriggered()) {
                    resultAction.aborted();
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    resultAction.setFcoEntityInfo(fco.getFcoInfo());
                    resultAction.setCloneDatastore(this.datastoreName);
                    resultAction.setCloneName(this.name);
                    if (!getOptions().isDryRun()) {
                        try {
                            if (fco instanceof VirtualMachineManager) {
                                final VirtualMachineManager vmm = (VirtualMachineManager) fco;
                                if (!vmm.clone(this.name, this.cloneReport)) {
                                    resultAction.failure();
                                }

                            } else if (fco instanceof VirtualAppManager) {
                                final VirtualAppManager vApp = (VirtualAppManager) fco;
                                if (!vApp.clone(this.name, this.datastoreName, this.cloneReport)) {
                                    resultAction.failure();
                                }
                            } else if (fco instanceof ImprovedVirtualDisk) {
                                final ImprovedVirtualDisk ivd = (ImprovedVirtualDisk) fco;
                                final ManagedEntityInfo datastoreInfo = (this.datastoreName == null)
                                        ? ivd.getDatastoreInfo()
                                        : ivd.getVimConnection().getManagedEntityInfo(EntityType.Datastore,
                                                this.datastoreName);
                                if (!ivd.clone(this.name, datastoreInfo, this.cloneReport)) {
                                    resultAction.failure();
                                }
                            } else {
                                final String msg = String.format("Unsupported type %s", fco.getClass().toString());
                                this.logger.warning(msg);
                                resultAction.failure(msg);
                            }

                        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg
                                | FileFaultFaultMsg | InvalidDatastoreFaultMsg | NotFoundFaultMsg
                                | com.vmware.vim25.RuntimeFaultFaultMsg | com.vmware.vslm.FileFaultFaultMsg
                                | com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.NotFoundFaultMsg
                                | VslmFaultFaultMsg | VslmTaskException | VimTaskException
                                | InsufficientResourcesFaultFaultMsg | InvalidStateFaultMsg | MigrationFaultFaultMsg
                                | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | VimObjectNotExistException
                                | CustomizationFaultFaultMsg e) {
                            Utility.logWarning(this.logger, e);
                            resultAction.failure(e);
                        } catch (final InterruptedException e) {
                            resultAction.failure(e);
                            this.logger.log(Level.WARNING, "Interrupted!", e);
                            // Restore interrupted state...
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                result.add(resultAction);
            }
        }
        return result;
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
                    resultAction.aborted();
                    result.clear();
                    result.add(resultAction);
                    break;
                } else {
                    resultAction.setFcoEntityInfo(fco.getFcoInfo());
                    try {
                        resultAction.setShutdownRequired(fco.getPowerState() == FcoPowerState.poweredOn);
                        if (!getOptions().isDryRun() && !fco.destroy()) {
                            resultAction.failure();
                        }
                    } catch (final RuntimeFaultFaultMsg | InvalidPropertyFaultMsg
                            | com.vmware.vim25.RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg | VimTaskException
                            | VimFaultFaultMsg | com.vmware.vslm.FileFaultFaultMsg
                            | com.vmware.vslm.InvalidDatastoreFaultMsg | com.vmware.vslm.InvalidStateFaultMsg
                            | com.vmware.vslm.NotFoundFaultMsg | com.vmware.vslm.TaskInProgressFaultMsg
                            | VslmFaultFaultMsg | VslmTaskException | FileFaultFaultMsg | InvalidDatastoreFaultMsg
                            | InvalidStateFaultMsg | NotFoundFaultMsg | TaskInProgressFaultMsg e) {
                        Utility.logWarning(this.logger, e);
                        resultAction.failure(e);
                    } catch (final InterruptedException e) {
                        this.logger.log(Level.WARNING, "Interrupted!", e);
                        resultAction.failure(e);
                        // Restore interrupted state...
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

    protected CoreResultActionVappImport actionVappImport(final ConnectionManager connetionManager)
            throws CoreResultActionException {
        try (final CoreResultActionVappImport result = new CoreResultActionVappImport()) {
            result.start();
            result.setDatastoreName(this.datastoreName);
            result.setFolderName(this.folderName);
            result.setHostName(this.hostName);
            result.setName(this.name);
            result.setUrlPath(this.urlPath.toString());
            result.setResourcePoolName(this.resourcePoolName);
            result.setVim(getOptions().getVim());
            if (Vmbk.isAbortTriggered()) {
                result.aborted("Aborted on user request");

            } else {
                try {
                    result.setFcoEntityInfo(connetionManager.importVapp(getOptions().getVim(), this.urlPath, this.name,
                            this.hostName, this.datastoreName, this.resourcePoolName, this.folderName));
                } catch (final IOException | KeyManagementException | NoSuchAlgorithmException
                        | com.vmware.vim25.RuntimeFaultFaultMsg | TimedoutFaultMsg | InvalidPropertyFaultMsg
                        | ConcurrentAccessFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
                        | TaskInProgressFaultMsg | VmConfigFaultFaultMsg | DuplicateNameFaultMsg
                        | InsufficientResourcesFaultFaultMsg | SafekeepingUnsupportedObjectException
                        | SafekeepingConnectionException | SafekeepingException | InvalidNameFaultMsg
                        | OutOfBoundsFaultMsg | InvalidCollectorVersionFaultMsg e) {
                    Utility.logWarning(this.logger, e);
                    result.failure(e);
                } catch (final InterruptedException e) {
                    result.failure(e);
                    this.logger.log(Level.WARNING, "Interrupted!", e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
            return result;
        }
    }

    public Boolean getCbt() {
        return this.cbt;
    }

    public String getDatacenterName() {
        if (StringUtils.isNotEmpty(this.vmFolderFilter)) {
            final String datacenterFromFilter = this.vmFolderFilter.substring(0, this.vmFolderFilter.indexOf('/'));
            if (StringUtils.isEmpty(this.datacenterName)) {
                return datacenterFromFilter;
            } else {
                if (this.datacenterName.equals(datacenterFromFilter)) {
                    return this.datacenterName;
                } else {
                    final String msg = String.format(
                            "Requested Datacenter name %s is not matching with vmFolder path. vmFolderFilter Datacenter name %s will be used",
                            this.datacenterName, datacenterFromFilter);
                    this.logger.warning(msg);
                    return datacenterFromFilter;
                }
            }
        }
        return this.datacenterName;
    }

    public String getDatastoreName() {
        return this.datastoreName;
    }

    public String getFolderName() {
        if (StringUtils.isNotEmpty(this.vmFolderFilter)) {
            if (StringUtils.isEmpty(this.folderName)) {
                return this.vmFolderFilter;
            } else {
                if (!this.folderName.contains(this.vmFolderFilter)) {
                    final String msg = String.format(
                            "Requested Folder %s is not a subfolder of vmFolderFilter(%s). vmFilter will be used instead ",
                            this.folderName, this.vmFolderFilter);
                    this.logger.warning(msg);
                    return this.vmFolderFilter;
                }
            }
        }
        return this.folderName;
    }

    public String getHostName() {
        return this.hostName;
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    public String getResourcePoolName() {
        if (StringUtils.isNotEmpty(this.resPoolFilter)) {
            if (StringUtils.isEmpty(this.resourcePoolName)) {
                return this.resPoolFilter;
            } else {
                if (this.resourcePoolName.contains(this.resPoolFilter)) {
                    return this.resourcePoolName;
                } else {
                    final String msg = String.format(
                            "Requested ResourcePool %s is not a subfolder of rpFilter(%s). rpFilter will be used instead ",
                            this.resourcePoolName, this.resPoolFilter);
                    this.logger.warning(msg);
                    return this.resPoolFilter;
                }
            }
        }
        return this.resourcePoolName;
    }

    public String getResPoolFilter() {
        return this.resPoolFilter;
    }

    public URL getUrlPath() {
        return this.urlPath;
    }

    public String getVappName() {
        return this.name;
    }

    public String getVmFolderFilter() {
        return this.vmFolderFilter;
    }

    public String[] getVmNetworksName() {
        return this.vmNetworksName;
    }

    @Override
    public final void initialize() {
        this.options = new CoreBasicCommandOptions();
        this.urlPath = null;
        this.name = null;
        this.hostName = null;
        this.datastoreName = null;
        this.folderName = null;
        this.resourcePoolName = null;
        this.datacenterName = null;
        this.isNoVmdk = false;
        this.vmFolderFilter = null;
        this.resPoolFilter = null;
        this.powerOn = false;
        this.vappImport = false;
        this.remove = false;
        this.cbt = false;
        this.clone = false;
        this.vmNetworksName = new String[CoreGlobalSettings.MAX_NUMBER_OF_VIRTUAL_MACHINE_NETWORK_CARDS];
        for (int i = 0; i < this.vmNetworksName.length; i++) {
            this.vmNetworksName[i] = "";
        }
    }

    public boolean isClone() {
        return this.clone;
    }

    public boolean isNoVmdk() {
        return this.isNoVmdk;
    }

    public boolean isPowerOn() {
        return this.powerOn;
    }

    public boolean isRemove() {
        return this.remove;
    }

    public boolean isVappImport() {
        return this.vappImport;
    }

    public void setCbt(final Boolean cbt) {
        this.cbt = cbt;
    }

    public void setClone(final boolean clone) {
        this.clone = clone;
    }

    protected final void setDatacenterName(final String datacenterName) {
        this.datacenterName = datacenterName;
    }

    protected final void setDatastoreName(final String datastoreName) {
        this.datastoreName = datastoreName;
    }

    protected final void setFolderName(final String folderName) {
        this.folderName = folderName;
    }

    protected final void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setNoVmdk(final boolean isNoVmdk) {
        this.isNoVmdk = isNoVmdk;
    }

    public void setPowerOn(final boolean powerOn) {
        this.powerOn = powerOn;
    }

    public void setRemove(final boolean remove) {
        this.remove = remove;
    }

    public final void setResourcePoolName(final String resourcePoolName) {
        this.resourcePoolName = resourcePoolName;
    }

    /**
     * @param string
     * @throws MalformedURLException
     */
    public void setUrlPath(final String url) throws MalformedURLException {
        this.urlPath = new URL(url);

    }

    public void setUrlPath(final URL importUrl) {
        this.urlPath = importUrl;
    }

    public void setVappImport(final boolean vappImport) {
        this.vappImport = vappImport;
    }

    public void setVappName(final String vappName) {
        this.name = vappName;
    }

    public final void setVmNetworksName(final String st) {
        final String[] newvmNetworksName = st.split(",");
        this.vmNetworksName = Arrays.copyOf(newvmNetworksName, newvmNetworksName.length);

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ovfInfo: ");

        if (StringUtils.isNotEmpty(this.name)) {
            sb.append(String.format("[vappName %s]", this.name));
        }
        if (getUrlPath() != null) {
            sb.append(String.format("[Url %s]", getUrlPath().toString()));
        }
        if (StringUtils.isNotEmpty(getHostName())) {
            sb.append(String.format("[host %s]", getHostName()));
        }
        if (StringUtils.isNotEmpty(getResourcePoolName())) {
            sb.append(String.format("[Resource Pool %s]", getResourcePoolName()));
        }

        if (StringUtils.isNotEmpty(getDatastoreName())) {
            sb.append(String.format("[datastore %s]", getDatastoreName()));
        }
        if (StringUtils.isNotEmpty(getDatacenterName())) {
            sb.append(String.format("[datacenter %s]", getDatacenterName()));
        }
        if (StringUtils.isNotEmpty(getFolderName())) {
            sb.append(String.format("[folder %s]", getFolderName()));
        }

        sb.append(String.format("[isNoVmdk %s]", PrettyBoolean.toString(this.isNoVmdk)));
        sb.append(String.format("[isDryRun %s]", PrettyBoolean.toString(getOptions().isDryRun())));

        return sb.toString();
    }

}
