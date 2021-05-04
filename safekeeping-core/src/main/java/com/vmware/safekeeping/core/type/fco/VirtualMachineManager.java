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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import com.vmware.jvix.JVixException;
import com.vmware.jvix.jDiskLib.Block;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmProfileId;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.core.Jvddk;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.profile.dataclass.DiskProfile;
import com.vmware.safekeeping.core.profile.dataclass.FcoNetwork;
import com.vmware.safekeeping.core.profile.ovf.SerializableVmConfigInfo;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.PscProvider;
import com.vmware.safekeeping.core.soap.PscProvider.KeyType;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.GuestInfoFlags;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.StorageDirectoryInfo;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FcoPowerState;
import com.vmware.safekeeping.core.type.enums.FirstClassObjectType;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;
import com.vmware.safekeeping.core.type.fco.managers.VirtualControllerManager;
import com.vmware.safekeeping.core.type.fco.managers.VirtualMachineConfigManager;
import com.vmware.safekeeping.core.type.location.CoreVmLocation;
import com.vmware.safekeeping.core.type.manipulator.VmxManipulator;
import com.vmware.vapi.saml.DefaultTokenFactory;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.CannotCreateFileFaultMsg;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.CustomizationFaultFaultMsg;
import com.vmware.vim25.DVPortgroupConfigInfo;
import com.vmware.vim25.DatastoreCapability;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DeviceUnsupportedForVmVersionFaultMsg;
import com.vmware.vim25.DiskChangeExtent;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileAlreadyExistsFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.GuestOperationsFaultFaultMsg;
import com.vmware.vim25.GuestOsDescriptorFirmwareType;
import com.vmware.vim25.GuestProcessInfo;
import com.vmware.vim25.GuestProgramSpec;
import com.vmware.vim25.ID;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidControllerFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPowerStateFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedByInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.MigrationFaultFaultMsg;
import com.vmware.vim25.MissingControllerFaultMsg;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.OpaqueNetworkSummary;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SAMLTokenAuthentication;
import com.vmware.vim25.SnapshotFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.ToolsUnavailableFaultMsg;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VimFaultFaultMsg;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualE1000E;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualEthernetCardOpaqueNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFlagInfo;
import com.vmware.vim25.VirtualMachineGuestQuiesceSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.VirtualMachineWindowsQuiesceSpec;
import com.vmware.vim25.VirtualMachineWindowsQuiesceSpecVssBackupContext;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSriovEthernetCard;
import com.vmware.vim25.VirtualVmxnet;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class VirtualMachineManager implements IFirstClassObject, IManagedEntityInfoPath {

    /**
     * 
     */
    private static final long serialVersionUID = 6826865282824824363L;

    private static final Logger logger = Logger.getLogger(VirtualMachineManager.class.getName());

    private static final int NUMBER_MAX_TENTATIVECREATE_DIRECTORY = 9;

    public static String sHeaderToString() {
        return String.format("%-8s%-36s\t%-8s\t%-30s", "entity", "uuid", "moref", "name");
    }

    private VirtualMachineConfigManager configMgr;
    private ManagedEntityInfo dataCenterInfo;
    private String name;

    private SnapshotManager snapshotManager;

    private ManagedFcoEntityInfo temporaryFcoInfo;

    private VimConnection vimConnection;

    private ManagedObjectReference vmMoref;

    private final Map<Integer, ID> vStorageObjectAssociations = new HashMap<>();

    /**
     * @param connetionManager
     * @param fcoInfo
     */
    public VirtualMachineManager(final ConnectionManager connetionManager, final ManagedFcoEntityInfo fcoInfo) {
        this.vimConnection = connetionManager.getVimConnection(fcoInfo.getServerUuid());
        this.temporaryFcoInfo = fcoInfo;
    }

    /**
     * @param vmInfo
     */
    public VirtualMachineManager(final ManagedFcoEntityInfo fcoInfo) {
        this.temporaryFcoInfo = fcoInfo;
    }

    public VirtualMachineManager(final VimConnection vimConnection, final ManagedFcoEntityInfo fcoInfo) {
        this.vimConnection = vimConnection;
        this.temporaryFcoInfo = fcoInfo;
    }

    public VirtualMachineManager(final VimConnection vimConnection, final ManagedObjectReference vmMoref)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        this.vimConnection = vimConnection;
        this.vmMoref = vmMoref;
        this.name = (String) vimConnection.getVimHelper().entityProps(vmMoref, "name");

        this.configMgr = new VirtualMachineConfigManager(this.vimConnection, vmMoref);
        this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
    }

    public VirtualMachineManager(final VimConnection vimConnection, final String name,
            final ManagedEntityInfo dataCenterInfo) {
        this.vimConnection = vimConnection;
        this.name = name;
        this.dataCenterInfo = dataCenterInfo;
    }

    public VirtualMachineManager(final VimConnection vimConnection, final String name, final ManagedObjectReference vm)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        this.vimConnection = vimConnection;
        this.vmMoref = vm;
        this.name = name;
        this.configMgr = new VirtualMachineConfigManager(this.vimConnection, vm);
        this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
    }

    public VirtualMachineManager(final VimConnection vimConnection, final String name,
            final ManagedObjectReference moref, final VirtualMachineConfigInfo configInfo) {
        this.vimConnection = vimConnection;
        this.vmMoref = moref;
        this.name = name;
        this.configMgr = new VirtualMachineConfigManager(this.vimConnection, moref, configInfo);

    }

    public void actionDefragmentAllDisks() throws FileFaultFaultMsg, InvalidPowerStateFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg {
        this.vimConnection.getVimPort().defragmentAllDisks(getMoref());
    }

    private boolean addEmptyDisks(final VirtualMachineConfigSpec vmConfigSpec,
            final List<VirtualControllerManager> ctrlmList) {

        final List<VirtualDeviceConfigSpec> specList = new LinkedList<>();

        for (final VirtualControllerManager ctrlm : ctrlmList) {
            /*
             * create new device of the controller and all disks managed by it.
             */
            specList.addAll(ctrlm.createAll(this));
        }
        vmConfigSpec.getDeviceChange().clear();
        vmConfigSpec.getDeviceChange().addAll(specList);

        return true;
    }

    public boolean attachDisk(final ImprovedVirtualDisk ivd, final Integer controllerKey, final Integer unitNumber)
            throws DeviceUnsupportedForVmVersionFaultMsg, FileFaultFaultMsg, InvalidControllerFaultMsg,
            InvalidDatastoreFaultMsg, InvalidStateFaultMsg, MissingControllerFaultMsg, NotFoundFaultMsg,
            RuntimeFaultFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException, VimObjectNotExistException {
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().attachDiskTask(getMoref(), ivd.getId(),
                getDatastoreInfo().getMoref(), controllerKey, unitNumber);
        return this.vimConnection.waitForTask(taskMor);
    }

    public boolean clone(final String cloneName, final RunningReport cloneReport) throws CustomizationFaultFaultMsg,
            FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg,
            MigrationFaultFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InterruptedException, VimTaskException, InvalidCollectorVersionFaultMsg {
        boolean result = false;
        final VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        final VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
        cloneSpec.setLocation(relocSpec);
        cloneSpec.setPowerOn(false);
        cloneSpec.setTemplate(false);

        final ManagedObjectReference cloneTask = this.vimConnection.getVimPort().cloneVMTask(getMoref(),
                getVmFolderInfo().getMoref(), cloneName, cloneSpec);
        if (this.vimConnection.waitForTask(cloneTask, cloneReport)) {
            result = true;
        }

        return result;
    }

    private StorageDirectoryInfo createDirectory(final ManagedEntityInfo dsInfo, final ManagedEntityInfo dcInfo,
            final String directoryName)
            throws CannotCreateFileFaultMsg, FileAlreadyExistsFaultMsg, InvalidDatastoreFaultMsg, RuntimeFaultFaultMsg,
            FileFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        String msg;

        final StorageDirectoryInfo result = new StorageDirectoryInfo(dcInfo, dsInfo, directoryName);
        final Map<String, Object> datastoreInfo = this.vimConnection.getVimHelper().entityProps(dsInfo,
                new String[] { "capability", "info" });

        if (datastoreInfo != null) {
            final DatastoreCapability capability = (DatastoreCapability) datastoreInfo.get("capability");

            final DatastoreInfo info = (DatastoreInfo) datastoreInfo.get("info");

            result.setDatastoreUuid(info.getContainerId());
            if (Boolean.TRUE.equals(capability.isTopLevelDirectoryCreateSupported())) {

                this.vimConnection.getVimPort().makeDirectory(this.vimConnection.getServiceContent().getFileManager(),
                        result.getDirectoryName(), dcInfo.getMoref(), true);
                if (logger.isLoggable(Level.INFO)) {
                    msg = String.format("Folder [%s] %s created succesfully", dsInfo.getName(), result);
                    logger.info(msg);
                }
            } else {
                final String tmp = this.vimConnection.getVimPort().createDirectory(
                        this.vimConnection.getServiceContent().getDatastoreNamespaceManager(), dsInfo.getMoref(),
                        result.getDirectoryName(), "", null);
                result.setDirectoryUuid(StringUtils.substringAfterLast(tmp, "/"));
                if (logger.isLoggable(Level.INFO)) {
                    msg = String.format("Folder [%s] %s  (%s) created succesfully", dsInfo.getName(), tmp, result);
                    logger.info(msg);
                }
            }
        }

        return result;
    }

    public ManagedObjectReference createSnapshot(final String snapName)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        if (isGuestWindows()) {
            final VirtualMachineWindowsQuiesceSpec guestWinQuiesceSpec = new VirtualMachineWindowsQuiesceSpec();
            guestWinQuiesceSpec.setVssPartialFileSupport(false);
            guestWinQuiesceSpec.setTimeout(CoreGlobalSettings.getWindowsVssTimeOut());
            guestWinQuiesceSpec.setVssBootableSystemState(true);
            guestWinQuiesceSpec
                    .setVssBackupContext(VirtualMachineWindowsQuiesceSpecVssBackupContext.CTX_AUTO.toString());// "ctx_auto");VssBackupContext

            guestWinQuiesceSpec.setVssBackupType(SnapshotManager.VSS_BT_FULL);

            return createSnapshot(snapName, guestWinQuiesceSpec);

        } else {
            final VirtualMachineGuestQuiesceSpec guestQuiesceSpec = new VirtualMachineGuestQuiesceSpec();
            guestQuiesceSpec.setTimeout(Utility.TEN_SECONDS);

            return createSnapshot(snapName, guestQuiesceSpec);

        }
    }

    public ManagedObjectReference createSnapshot(final String snapName, final RunningReport snapReport)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        ManagedObjectReference result = null;
        ManagedObjectReference snapMor = null;
        ManagedObjectReference taskMor = null;
        // Quisce timeout
        final int timeout = 10;
        if (isGuestWindows()) {
            final VirtualMachineWindowsQuiesceSpec guestWinQuiesceSpec = new VirtualMachineWindowsQuiesceSpec();
            guestWinQuiesceSpec.setVssPartialFileSupport(false);
            guestWinQuiesceSpec.setTimeout(timeout);
            guestWinQuiesceSpec.setVssBootableSystemState(true);
            guestWinQuiesceSpec
                    .setVssBackupContext(VirtualMachineWindowsQuiesceSpecVssBackupContext.CTX_AUTO.toString());// "ctx_auto");VssBackupContext

            guestWinQuiesceSpec.setVssBackupType(SnapshotManager.VSS_BT_FULL);

            taskMor = this.vimConnection.getVimPort().createSnapshotExTask(getMoref(), snapName, null, false,
                    guestWinQuiesceSpec);

        } else {
            final VirtualMachineGuestQuiesceSpec guestQuiesceSpec = new VirtualMachineGuestQuiesceSpec();
            guestQuiesceSpec.setTimeout(timeout);

            taskMor = this.vimConnection.getVimPort().createSnapshotExTask(getMoref(), snapName, null, false,
                    guestQuiesceSpec);

        }
        if (this.vimConnection.waitForTask(taskMor, snapReport)) {
            final TaskInfo taskInfo = (TaskInfo) this.vimConnection.getVimHelper().entityProps(taskMor, "info");
            snapMor = (ManagedObjectReference) taskInfo.getResult();
            result = snapMor;
        }
        return result;
    }

    public ManagedObjectReference createSnapshot(final String snapName,
            final VirtualMachineGuestQuiesceSpec guestQuiesceSpec)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        ManagedObjectReference result = null;
        ManagedObjectReference snapMor = null;
        ManagedObjectReference taskMor;
        taskMor = this.vimConnection.getVimPort().createSnapshotExTask(getMoref(), snapName, null, false,
                guestQuiesceSpec);
        if (this.vimConnection.waitForTask(taskMor)) {
            final TaskInfo taskInfo = (TaskInfo) this.vimConnection.getVimHelper().entityProps(taskMor, "info");
            snapMor = (ManagedObjectReference) taskInfo.getResult();
            result = snapMor;
        }
        return result;
    }

    public StorageDirectoryInfo createVMHomeDirectory(final CoreRestoreManagedInfo managedInfo)
            throws CannotCreateFileFaultMsg, InvalidDatastoreFaultMsg, RuntimeFaultFaultMsg, FileFaultFaultMsg,
            InvalidPropertyFaultMsg, InterruptedException {
        String directoryName = managedInfo.getName();
        String msg;
        StorageDirectoryInfo result = null;
        boolean success = false;
        for (int iteraction = 0; iteraction < NUMBER_MAX_TENTATIVECREATE_DIRECTORY; iteraction++) {
            try {
                result = createDirectory(managedInfo.getDatastoreInfo(), managedInfo.getDcInfo(), directoryName);
                success = true;
                break;
            } catch (final FileAlreadyExistsFaultMsg e) {
                msg = String.format("Tentative %d: Datastore folder: %s already exist", iteraction, directoryName);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.WARNING, msg, e);
                } else {
                    logger.warning(msg);
                }
                directoryName = String.format("%s_%d", managedInfo.getName(), iteraction);
            }
        }

        if (!success) {
            msg = "Max Iteraction reached. Failed to  create a directory";
            logger.warning(msg);

        }
        return result;
    }

    /**
     * Delete a VM snapshot
     *
     * @param snap
     * @param removeChildren
     * @param consolidate
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws TaskInProgressFaultMsg
     */
    public boolean deleteSnapshot(final ManagedObjectReference snap, final boolean removeChildren,
            final boolean consolidate) throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg,
            InterruptedException, VimTaskException, InvalidCollectorVersionFaultMsg {
        return deleteSnapshot(snap, removeChildren, consolidate, null);
    }

    /**
     * Delete a VM snapshot
     *
     * @param snap
     * @param removeChildren
     * @param consolidate
     * @param snapReport
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws TaskInProgressFaultMsg
     */
    public boolean deleteSnapshot(final ManagedObjectReference snap, final boolean removeChildren,
            final boolean consolidate, final RunningReport snapReport)
            throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg, InterruptedException,
            VimTaskException, InvalidCollectorVersionFaultMsg {
        boolean result = false;

        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().removeSnapshotTask(snap, removeChildren,
                consolidate);
        assert taskMor != null;
        if (this.vimConnection.waitForTask(taskMor, snapReport)) {
            result = true;
        }
        return result;
    }

    /**
     *
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws VimFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimTaskException
     * @throws InterruptedException
     */
    @Override
    public boolean destroy() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException, VimFaultFaultMsg {
        boolean result;
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().destroyTask(getMoref());

        result = this.vimConnection.waitForTask(taskMor);

        return result;
    }

    public boolean detachDisk(final ImprovedVirtualDisk ivd) throws FileFaultFaultMsg, InvalidStateFaultMsg,
            NotFoundFaultMsg, RuntimeFaultFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().detachDiskTask(getMoref(), ivd.getId());
        return this.vimConnection.waitForTask(taskMor);
    }

    private ByteArrayInOutStream download(final String esxFile, final String dataStoreName, final String dataCenter) {
        String serviceUrl = this.vimConnection.getUrl();
        serviceUrl = serviceUrl.substring(0, serviceUrl.lastIndexOf("sdk") - 1);

        final String httpUrl = serviceUrl + "/folder/" + esxFile + "?dcPath=" + dataCenter + "&dsName=" + dataStoreName;
        if (logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Url: %s  downloading VM File: %s", httpUrl, esxFile);
            logger.info(msg);
        }
        return getData(httpUrl);
    }

    public ByteArrayInOutStream exportNvram(final String nVram)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        ByteArrayInOutStream result = null;
        final String vmPahtName = this.configMgr.getVmPathName();
        if (vmPahtName != null) {
            final String configurationDir = vmPahtName.substring(vmPahtName.indexOf(']') + 2,
                    vmPahtName.lastIndexOf('/'));
            final String dataStoreName = vmPahtName.substring(vmPahtName.indexOf('[') + 1, vmPahtName.lastIndexOf(']'));

            final String cm = configurationDir.concat("/").concat(nVram.replace("\"", ""));

            if (logger.isLoggable(Level.FINER)) {
                final String msg = String.format("vmDirectory: %s datacenter: %s file: %s", vmPahtName,
                        getDatacenterInfo().getName(), cm);
                logger.fine(msg);
            }
            result = download(cm, dataStoreName, getDatacenterInfo().getName());
        }
        return result;

    }

    public ByteArrayInOutStream exportVmx() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        ByteArrayInOutStream result = null;
        final String vmPahtName = this.configMgr.getVmPathName();
        if (vmPahtName != null) {

            final String dataStoreName = vmPahtName.substring(vmPahtName.indexOf('[') + 1, vmPahtName.lastIndexOf(']'));

            final String vmxFileLocation = vmPahtName.substring(vmPahtName.indexOf(']') + 2);
            result = download(vmxFileLocation, dataStoreName, getDatacenterInfo().getName());
        }
        return result;
    }

    private boolean forceUpdate() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        boolean result = false;
        this.configMgr = new VirtualMachineConfigManager(this.vimConnection, this.vmMoref);

        this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
        if (this.dataCenterInfo != null) {
            this.temporaryFcoInfo = null;
            result = true;
        }
        return result;
    }

    public List<String> getAllSnapshotNameList()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        List<String> result = null;

        final VirtualMachineSnapshotInfo snapInfo = getSnapInfo();
        if (snapInfo != null) {
            final VirtualMachineSnapshotTree[] snapTree = snapInfo.getRootSnapshotList()
                    .toArray(new VirtualMachineSnapshotTree[0]);

            result = getAllSnapshotNameList(snapTree);
        }

        return result;
    }

    private List<String> getAllSnapshotNameList(final VirtualMachineSnapshotTree[] snapTree) {
        final List<String> result = new LinkedList<>();

        for (int i = 0; (snapTree != null) && (i < snapTree.length); i++) {
            result.add(snapTree[i].getName());
            final VirtualMachineSnapshotTree[] childTree = snapTree[i].getChildSnapshotList()
                    .toArray(new VirtualMachineSnapshotTree[0]);
            if (childTree != null) {
                result.addAll(getAllSnapshotNameList(childTree));
            }
        }
        return result;
    }

    public List<PbmProfileId> getAssociatedProfile() throws PbmFaultFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {

        return this.vimConnection.getPbmConnection().getAssociatedProfile(this);

    }

    public List<PbmProfileId> getAssociatedProfile(final int key)
            throws PbmFaultFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {

        return this.vimConnection.getPbmConnection().getAssociatedProfile(this, key);

    }

    public Map<String, ManagedObjectReference> getAvailableHostNetworks()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        final HashMap<String, ManagedObjectReference> networksAvailable = new HashMap<>();
        final ArrayOfManagedObjectReference networks = (ArrayOfManagedObjectReference) this.vimConnection.getVimHelper()
                .entityProps(getRuntimeInfo().getHost(), "network");

        for (final ManagedObjectReference ds : networks.getManagedObjectReference()) {
            try {
                final String st = this.vimConnection.getVimHelper().entityProps(ds, "name").toString();
                networksAvailable.put(st, ds);
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                Utility.logWarning(logger, e);
            }
        }
        return networksAvailable;

    }

    public String getBiosUuid() {
        if (this.vmMoref == null) {
            return "";
        }
        return this.configMgr.getVirtualMachineConfigInfo().getUuid();
    }

    public VirtualMachineConfigManager getConfig() {
        return this.configMgr;
    }

    public SnapshotManager getCurrentSnapshot()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final VirtualMachineSnapshotInfo snapInfo = getSnapInfo();
        if (snapInfo != null) {
            final ManagedObjectReference vmSnap = snapInfo.getCurrentSnapshot();
            if (vmSnap != null) {
                return new SnapshotManager(this.vimConnection, this, snapInfo);
            }
        }
        return null;

    }

    /**
     * Download from URL
     *
     * @param urlString
     * @return
     */
    private ByteArrayInOutStream getData(final String urlString) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                final String msg = String.format("Downloading VM File %s ", urlString);
                logger.info(msg);
            }
            HttpURLConnection conn;
            final URL url = new URL(StringUtils.replace(urlString, " ", "%20"));
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setAllowUserInteraction(true);

            String cookieValue = this.vimConnection.getCookie();
            final StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
            cookieValue = tokenizer.nextToken();
            final String pathData = "$" + tokenizer.nextToken();
            final String cookie = "$Version=\"1\"; " + cookieValue + "; " + pathData;

            final Map<String, List<String>> map = new HashMap<>();
            map.put("Cookie", Collections.singletonList(cookie));
            ((BindingProvider) this.vimConnection.getVimPort()).getRequestContext()
                    .put(MessageContext.HTTP_REQUEST_HEADERS, map);

            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Expect", "100-continue");
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Length", "1024");
            try (InputStream in = conn.getInputStream();
                    final ByteArrayInOutStream byteOut = new ByteArrayInOutStream()) {
                final int bufLen = 9 * 1024;
                final byte[] buf = new byte[bufLen];
                byte[] tmp = null;
                int len = 0;
                int bytesRead = 0;
                while ((len = in.read(buf, 0, bufLen)) != -1) {
                    bytesRead += len;
                    tmp = new byte[len];
                    System.arraycopy(buf, 0, tmp, 0, len);
                    byteOut.write(buf, 0, len);

                }

                if (logger.isLoggable(Level.FINE)) {
                    final String msg = String.format("URL: %s Downloaded %d bytes", urlString, bytesRead);
                    logger.fine(msg);
                }
                return byteOut;
            }
        } catch (final IOException | NoSuchAlgorithmException e) {
            Utility.logWarning(logger, e);
            return null;
        }
    }

    @Override
    public ManagedEntityInfo getDatacenterInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        if (this.dataCenterInfo == null) {
            this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(getMoref());
        }
        return this.dataCenterInfo;
    }

    public ManagedEntityInfo getDatastoreInfo()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {
        if (this.configMgr != null) {
            final String vmPahtName = this.configMgr.getVmPathName();
            if (vmPahtName != null) {

                final String dataStoreName = vmPahtName.substring(vmPahtName.indexOf('[') + 1,
                        vmPahtName.lastIndexOf(']'));
                return this.vimConnection.getManagedEntityInfo(EntityType.Datastore, dataStoreName);
            }
        }

        return null;
    }

    public String getEncryptionBundle() {
        String encryptionbundle = "";
        if (getConfig() != null) {
            final List<OptionValue> extraConfig = getExtraConfig();
            for (final OptionValue option : extraConfig) {
                if ("encryption.bundle".equalsIgnoreCase(option.getKey())) {
                    encryptionbundle = option.getValue().toString();
                    break;
                }
            }
        }
        return encryptionbundle;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VirtualMachine;
    }

    public List<OptionValue> getExtraConfig() {
        if (getConfig() != null) {
            return getConfig().getVirtualMachineConfigInfo().getExtraConfig();
        }
        return Collections.emptyList();
    }

    @Override
    public ManagedFcoEntityInfo getFcoInfo() {
        if (this.temporaryFcoInfo != null) {
            return this.temporaryFcoInfo;
        }
        return new ManagedFcoEntityInfo(getName(), getMoref(), getUuid(), this.vimConnection.getServerIntanceUuid());
    }

    public GuestOsDescriptorFirmwareType getFirmware() {
        if (getConfig() != null) {
            final String firmware = getConfig().getVirtualMachineConfigInfo().getFirmware();
            return GuestOsDescriptorFirmwareType.fromValue(firmware);
        }
        return GuestOsDescriptorFirmwareType.BIOS;
    }

    @Override
    public ArrayList<Block> getFullDiskAreas(final int diskId) {
        final int bytePerSector = jDiskLibConst.SECTOR_SIZE;
        final List<VmdkInfo> vmdkInfoList = this.configMgr.getAllVmdkInfo();
        final VmdkInfo vmdkInfo = vmdkInfoList.get(diskId);

        final long capacityInBytes = vmdkInfo.getCapacityInBytes();
        final ArrayList<Block> vixBlocks = new ArrayList<>();
        final Block block = new Block();
        block.offset = 0;
        block.length = capacityInBytes / bytePerSector;
        vixBlocks.add(block);
        return vixBlocks;
    }

    public String getGuestFullName() {
        if (getConfig() != null) {
            return getConfig().getVirtualMachineConfigInfo().getGuestFullName();

        }
        return "";
    }

    public String getGuestId() {
        if (getConfig() != null) {
            return getConfig().getVirtualMachineConfigInfo().getGuestId();

        }
        return "";
    }

    public GuestInfoFlags getHeaderGuestInfo() {
        final GuestInfoFlags result = new GuestInfoFlags();
        final VirtualMachineFlagInfo flags = getConfig().getVirtualMachineConfigInfo().getFlags();

        if (Boolean.FALSE.equals(flags.isDiskUuidEnabled())) {
            logger.log(Level.WARNING, () -> "Disk UUIDs are not exposed to the guest.");
        }
        result.setDiskUuidEnabled(flags.isDiskUuidEnabled());
        result.setTemplate(getConfig().isTemplate());
        result.setVbsEnabled(flags.isVbsEnabled());
        result.setVAppConfigAvailable(isvAppConfigAvailable());
        result.setConfigurationEncrypted(isEncrypted());
        result.setGuestFullName(getGuestFullName());
        result.setNumberOfVirtualDisk(getConfig().countVirtualDisk());
        result.setVmPathName(getConfig().getVmPathName());
        result.setChangeTrackingEnabled(getConfig().isChangeTrackingEnabled());
        return result;
    }

    public ManagedObjectReference getHost() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return getRuntimeInfo().getHost();
    }

    public ManagedEntityInfo getHostInfo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final ManagedObjectReference hostMoref = getRuntimeInfo().getHost();
        final String hostname = this.vimConnection.getHostName(hostMoref);
        return new ManagedEntityInfo(hostname, hostMoref, getVcenterInstanceUuid());
    }

    public List<ImprovedVirtualDisk> getImprovedVirtuaDisk() {
        final LinkedList<ImprovedVirtualDisk> result = new LinkedList<>();
        if (useImprovedVirtuaDisk()) {
            for (final ID id : getVStorageObjectAssociations().values()) {
                final ImprovedVirtualDisk ivd = getVimConnection().getFind().findIvdById(id);
                if (ivd != null) {
                    result.add(ivd);
                }
            }
        }
        return result;
    }

    /**
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InterruptedException
     * @throws VimObjectNotExistException
     *
     */
    public CoreVmLocation getLocations()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimObjectNotExistException {
        final CoreVmLocation result = new CoreVmLocation();
        result.setDatastoreInfo(getDatastoreInfo());

        result.setDatacenterInfo(getDatacenterInfo());
        result.setResourcePoolInfo(getResourcePoolInfo());
        result.setResourcePoolPath(getManagedEntityInfoPath(result.getResourcePoolInfo()));
        result.setResourcePoolFullPath(ManagedEntityInfo.composeEntityInfoName(result.getResourcePoolPath()));
        if (result.getResourcePoolInfo().getEntityType() == EntityType.VirtualApp) {
            result.setvAppMember(true);
        } else {
            result.setVmFolderInfo(getVmFolderInfo());
            result.setVmFolderPath(getManagedEntityInfoPath(result.getVmFolderInfo()));
            result.setVmFolderFullPath(ManagedEntityInfo.composeEntityInfoName(result.getVmFolderPath()));
        }
        final String vmxPath = this.configMgr.getVmPathName();
        result.setVmxFullPath(vmxPath);
        result.setDatastorePath(StringUtils.substringBeforeLast(vmxPath, "/"));
        result.setVmxFileName(StringUtils.substringAfterLast(vmxPath, "/"));
        return result;

    }

    public ManagedByInfo getManagedBy() {
        if ((this.configMgr != null) && (getConfig().getVirtualMachineConfigInfo() != null)) {
            return this.configMgr.getVirtualMachineConfigInfo().getManagedBy();
        }
        return null;
    }

    @Override
    public ManagedObjectReference getMoref() {
        if ((this.vmMoref == null) && (this.temporaryFcoInfo != null)) {
            return this.temporaryFcoInfo.getMoref();

        }
        return this.vmMoref;
    }

    public String getMorefValue() {

        return getMoref().getValue();
    }

    @Override
    public String getName() {
        if ((this.vmMoref == null) && (this.temporaryFcoInfo != null)) {
            return this.temporaryFcoInfo.getName();
        }
        return this.name;
    }

    @Override
    public ManagedFcoEntityInfo getParentFco()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        ManagedFcoEntityInfo result = null;
        final ManagedObjectReference parMoref = (ManagedObjectReference) this.vimConnection.getVimHelper()
                .entityProps(this.vmMoref, "parentVApp");
        if (parMoref != null) {
            final Map<String, Object> res2 = this.vimConnection.getVimHelper().entityProps(parMoref,
                    new String[] { "name", "vAppConfig" });
            final String parentName = res2.get("name").toString();
            final VAppConfigInfo confInfo = (VAppConfigInfo) res2.get("vAppConfig");
            result = new ManagedFcoEntityInfo(parentName, parMoref, confInfo.getInstanceUuid(),
                    this.vimConnection.getServerIntanceUuid());
        }
        return result;
    }

    @Override
    public ManagedObjectReference getParentFolder()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        return (ManagedObjectReference) this.vimConnection.getVimHelper().entityProps(getMoref(), "parent");
    }

    @Override
    public FcoPowerState getPowerState() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        switch (getRuntimeInfo().getPowerState()) {
        case POWERED_OFF:
            return FcoPowerState.poweredOff;
        case POWERED_ON:
            return FcoPowerState.poweredOn;
        case SUSPENDED:
            return FcoPowerState.suspended;
        }
        return FcoPowerState.poweredOff;
    }

    @Override
    public ManagedEntityInfo getResourcePoolInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        final ManagedObjectReference mor = (ManagedObjectReference) getVimConnection().getVimHelper()
                .entityProps(getMoref(), "resourcePool");

        final String entityName = getVimConnection().getVimHelper().entityName(mor);

        return new ManagedEntityInfo(entityName, mor, getVimConnection().getServerIntanceUuid());

    }

    public VirtualMachineRuntimeInfo getRuntimeInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        return (VirtualMachineRuntimeInfo) this.vimConnection.getVimHelper().entityProps(this.vmMoref, "runtime");

    }

    @Override
    public String getServerUuid() {
        if (this.vimConnection != null) {
            return this.vimConnection.getServerIntanceUuid();
        }
        return null;
    }

    public VirtualMachineSnapshotInfo getSnapInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        VirtualMachineSnapshotInfo snapInfo;

        snapInfo = (VirtualMachineSnapshotInfo) this.vimConnection.getVimHelper().entityProps(getMoref(), "snapshot");

        return snapInfo;
    }

    public SnapshotManager getSnapshotManager() {
        return this.snapshotManager;
    }

    public StorageDirectoryInfo getStorageDirectoryInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimObjectNotExistException {
        return new StorageDirectoryInfo(getDatacenterInfo(), getDatastoreInfo(), getConfig().getVmPathName());
    }

    @Override
    public FirstClassObjectType getType() {
        return FirstClassObjectType.vm;
    }

    @Override
    public String getUuid() {
        if ((this.vmMoref == null) && (this.temporaryFcoInfo != null)) {
            return this.temporaryFcoInfo.getUuid();
        }
        return this.configMgr.getInstanceUuid();
    }

    public SerializableVmConfigInfo getvAppConfig() {
        if (isvAppConfigAvailable()) {
            return new SerializableVmConfigInfo(getConfig().getVirtualMachineConfigInfo().getVAppConfig());
        }
        return null;
    }

    public String getVcenterInstanceUuid() {
        return (this.vmMoref == null) ? this.temporaryFcoInfo.getServerUuid()
                : this.vimConnection.getServerIntanceUuid();
    }

    @Override
    public VimConnection getVimConnection() {
        return this.vimConnection;
    }

    public Map<String, FcoNetwork> getVirtualMachineNetworks() {
        final Map<String, FcoNetwork> vmNetworks = new HashMap<>();
        try {

            final ArrayOfManagedObjectReference networks = (ArrayOfManagedObjectReference) this.vimConnection
                    .getVimHelper().entityProps(getMoref(), "network");
            int index = 0;

            for (final ManagedObjectReference network : networks.getManagedObjectReference()) {
                if ("OpaqueNetwork".equals(network.getType())) {
                    final OpaqueNetworkSummary summary = (OpaqueNetworkSummary) this.vimConnection.getVimHelper()
                            .entityProps(network, "summary");
                    final FcoNetwork fcoNetwork = new FcoNetwork();
                    fcoNetwork.setName(summary.getName());
                    fcoNetwork.setMoref(network.getValue());
                    fcoNetwork.setType(network.getType());
                    fcoNetwork.setKey(summary.getOpaqueNetworkId());
                    fcoNetwork.setIndex(index);
                    vmNetworks.put(summary.getOpaqueNetworkId(), fcoNetwork);
                } else if ("DistributedVirtualPortgroup".equals(network.getType())) {
                    final Map<String, Object> res = this.vimConnection.getVimHelper().entityProps(network,
                            new String[] { "name", "key" });

                    final FcoNetwork fcoNetwork = new FcoNetwork();
                    final String key = (String) res.get("key");
                    fcoNetwork.setName((String) res.get("name"));
                    fcoNetwork.setMoref(network.getValue());
                    fcoNetwork.setType(network.getType());
                    fcoNetwork.setKey(key);
                    fcoNetwork.setIndex(index);
                    vmNetworks.put(key, fcoNetwork);
                    ++index;
                } else {
                    final FcoNetwork fcoNetwork = new FcoNetwork();
                    fcoNetwork.setName(this.vimConnection.getVimHelper().entityName(network));
                    fcoNetwork.setMoref(network.getValue());
                    fcoNetwork.setType(network.getType());
                    fcoNetwork.setIndex(index);
                    vmNetworks.put(this.name, fcoNetwork);
                    ++index;
                }
            }

        } catch (final RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
            Utility.logWarning(logger, e);
        } catch (final InterruptedException e) {

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return vmNetworks;
    }

    @Override
    public ManagedEntityInfo getVmFolderInfo()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final ManagedObjectReference mor = (ManagedObjectReference) this.vimConnection.getVimHelper()
                .entityProps(getMoref(), "parent");
        if (mor != null) {
            final String folderName = this.vimConnection.getVimHelper().entityName(mor);

            return new ManagedEntityInfo(folderName, mor, this.vimConnection.getServerIntanceUuid());
        } else {
            return null;
        }
    }

    public Map<Integer, ID> getVStorageObjectAssociations() {
        return this.vStorageObjectAssociations;
    }

    @Override
    public String headertoString() {
        return sHeaderToString();
    }

    public boolean importNvram(final byte[] byteArray) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InterruptedException, IOException, VimObjectNotExistException {
        final StorageDirectoryInfo storageDirectoryInfo = getStorageDirectoryInfo();
        final String location = String.format("%s/%s", storageDirectoryInfo.getDirectoryUuid(),
                getConfig().getExtraConfig("nvram"));
        return upload(location, storageDirectoryInfo, byteArray);
    }

    public boolean importNvram(final CoreRestoreManagedInfo managedInfo,
            final StorageDirectoryInfo storageVmHomeDirectory, final byte[] byteArray) throws IOException {
        final String location = String.format("%s/%s.nvram", storageVmHomeDirectory.getDirectoryUuid(),
                managedInfo.getName());

        logger.log(Level.INFO, () -> "Uploading NVRAM File: " + location);
        return upload(location, storageVmHomeDirectory, byteArray);

    }

    public boolean importVmx(final CoreRestoreManagedInfo managedInfo, final GenerationProfile profGen,
            final StorageDirectoryInfo storageVmHomeDirectory, final byte[] byteArray) throws IOException {
        boolean result = false;

        try (final VmxManipulator newVmx = new VmxManipulator(byteArray)) {
            if (managedInfo.isRecovery()) {
                newVmx.keepUuid();
            } else {
                newVmx.prepareForRestore(managedInfo.getName());
            }
            newVmx.removeDisks(profGen);

            final String location = String.format("%s/%s.vmx", storageVmHomeDirectory.getDirectoryUuid(),
                    managedInfo.getName());
            logger.log(Level.INFO, () -> "Uploading VMX File:" + location);
            result = upload(location, storageVmHomeDirectory, newVmx.getBytes());
        }
        return result;
    }

    @Override
    public boolean isChangedBlockTrackingEnabled()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        if (this.configMgr != null) {
            forceUpdate();
            return this.configMgr.isChangeTrackingEnabled();
        }
        return false;
    }

    /**
     * Check if the VM is encrypted
     *
     * @return true if encrypted
     */
    @Override
    public boolean isEncrypted() {
        return ((getConfig() != null) && (getConfig().getVirtualMachineConfigInfo().getKeyId() != null));
    }

    public boolean isGuestWindows() {
        boolean result = false;
        if (getConfig() != null) {
            final String os = getConfig().getVirtualMachineConfigInfo().getGuestId().toLowerCase(Utility.LOCALE);
            result = os.contains("windows");
        }
        return result;
    }

    public boolean isInstanceUuidAvailable() {
        if (this.vmMoref == null) {
            return this.temporaryFcoInfo.getUuid() != null;
        }
        final String instanceUuid = this.configMgr.getInstanceUuid();
        return ((instanceUuid != null) && !instanceUuid.isEmpty());
    }

    /**
     * Check the existence of the vAppConfig property.
     *
     * @return get true if vm has vAppConfig set
     *
     */
    public boolean isvAppConfigAvailable() {
        return (getConfig() != null) && (getConfig().getVirtualMachineConfigInfo() != null)
                && (getConfig().getVirtualMachineConfigInfo().getVAppConfig() != null);
    }

    public boolean isVcenterInstanceUuidAvailable() {

        final String instanceUuid = (this.vmMoref == null) ? this.temporaryFcoInfo.getServerUuid()
                : this.vimConnection.getServerIntanceUuid();
        return ((instanceUuid != null) && !instanceUuid.isEmpty());
    }

    @Override
    public boolean isVmDatastoreNfs()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {
        final ManagedEntityInfo datastore = getDatastoreInfo();

        final DatastoreSummary summary = (DatastoreSummary) this.vimConnection.getVimHelper()
                .entityProps(datastore.getMoref(), "summary");

        return "nfs".equalsIgnoreCase(summary.getType());
    }

    public boolean markAsTemplate() throws FileFaultFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        this.vimConnection.getVimPort().markAsTemplate(getMoref());
        forceUpdate();
        return true;
    }

    public boolean markAsVirtualMachine(ManagedEntityInfo rp)
            throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        if (getConfig().isTemplate()) {

            final VirtualMachineSummary summary = (VirtualMachineSummary) this.vimConnection.getVimHelper()
                    .entityProps(getMoref(), "summary");
            final ManagedObjectReference hostMoRef = summary.getRuntime().getHost();

            if (rp == null) {
                final ManagedObjectReference rpMor = this.vimConnection.getResourcePoolByHost(hostMoRef);
                rp = new ManagedEntityInfo(this.vimConnection.getVimHelper().entityProps(rpMor, "name").toString(),
                        rpMor, this.vimConnection.getServerIntanceUuid());
            }

            this.vimConnection.getVimPort().markAsVirtualMachine(getMoref(), rp.getMoref(), hostMoRef);

            forceUpdate();
            return true;
        }
        return false;
    }

    public boolean powerOff() throws InvalidStateFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        boolean result = false;
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOffVMTask(getMoref());
        assert taskMor != null;
        if (this.vimConnection.waitForTask(taskMor)) {
            result = true;
        }

        return result;
    }

    public boolean powerOn() throws FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        return powerOn(null);
    }

    public boolean powerOn(final ManagedEntityInfo hostInfo)
            throws FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException {
        boolean result = false;

        final ManagedObjectReference hostMoref = (hostInfo != null) ? hostInfo.getMoref() : null;
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().powerOnVMTask(getMoref(), hostMoref);
        if (this.vimConnection.waitForTask(taskMor)) {
            result = true;
        }
        return result;
    }

    @Override
    public ArrayList<Block> queryChangedDiskAreas(final GenerationProfile profile, final Integer diskId,
            final BackupMode backupMode) throws FileFaultFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        final ArrayList<Block> vixBlocks = new ArrayList<>();

        if (profile.isDiskChanged(diskId)) {
            String changeId = "";
            final DiskProfile diskProfile = profile.getDisks().get(diskId);

            if ((profile.getPreviousGeneration() == null) || (backupMode == BackupMode.FULL)) {
                changeId = "*";
            } else {
                final DiskProfile prevGenDiskProfile = profile.getPreviousGeneration().getDisks().get(diskId);
                changeId = prevGenDiskProfile.getChangeId();
                if (logger.isLoggable(Level.FINE)) {
                    final String msg = String.format("ChangeId (gen %d) %s - Prev Generation (gen %d) %s ",
                            profile.getGenerationId(), diskProfile.getChangeId(),
                            profile.getPreviousGeneration().getGenerationId(), prevGenDiskProfile.getChangeId());
                    logger.fine(msg);
                }
            }
            DiskChangeInfo diskChangeInfo = null;
            long startPosition = 0;
            do {
                diskChangeInfo = this.vimConnection.queryChangedDiskAreas(this, diskProfile.getDeviceKey(),
                        startPosition, changeId);
                if (diskChangeInfo == null) {
                    break;
                }
                for (final DiskChangeExtent changedArea : diskChangeInfo.getChangedArea()) {
                    final Block block = new Block();
                    block.length = changedArea.getLength() / jDiskLibConst.SECTOR_SIZE;
                    block.offset = changedArea.getStart() / jDiskLibConst.SECTOR_SIZE;
                    vixBlocks.add(block);
                }
                startPosition = diskChangeInfo.getStartOffset() + diskChangeInfo.getLength();
            } while (startPosition < diskProfile.getCapacity());
        } else {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("No disk Changes - changeId:" + profile.getDisks().get(diskId).getChangeId());
            }
        }
        return vixBlocks;

    }

    public boolean rebootGuest() {

        try {
            this.vimConnection.getVimPort().rebootGuest(getMoref());
            return true;
        } catch (final InvalidStateFaultMsg | RuntimeFaultFaultMsg | TaskInProgressFaultMsg
                | ToolsUnavailableFaultMsg e) {
            Utility.logWarning(logger, e);
            return false;
        }
    }

    private boolean reconfigureNetwork(final VirtualMachineConfigSpec vmConfigSpec,
            final ManagedEntityInfo[] networkMapping) {
        boolean result = false;
        String msg;
        try {
            final List<VirtualDevice> listvd = getConfig().getVirtualMachineConfigInfo().getHardware().getDevice();
            if (!listvd.isEmpty()) {
                int index = 0;
                for (final VirtualDevice virtualDevice : listvd) {
                    final VirtualDeviceConfigSpec element = new VirtualDeviceConfigSpec();
                    if ((virtualDevice instanceof VirtualE1000) || (virtualDevice instanceof VirtualE1000E)
                            || (virtualDevice instanceof VirtualPCNet32) || (virtualDevice instanceof VirtualVmxnet)
                            || (virtualDevice instanceof VirtualEthernetCard)) {
                        if (networkMapping[index] == null) {

                            virtualDevice.setBacking(null);
                            virtualDevice.getConnectable().setConnected(false);
                            msg = String.format("No Network is avaialble. Set vmnic%d backing port to Null", index);
                            logger.warning(msg);
                        } else {
                            final ManagedEntityInfo newNetwork = networkMapping[index];
                            // ********* IMPORTANT ****************
                            // * External ID has to be reset for Opaque Network and CVDS ports
                            // * No effects on VDS and standard Switch
                            ((VirtualEthernetCard) virtualDevice).setExternalId(null);
                            // ******************************************
                            switch (newNetwork.getEntityType()) {
                            case Network:
                                final VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
                                nicBacking.setDeviceName(networkMapping[index].getName());
                                virtualDevice.setBacking(nicBacking);
                                break;
                            case OpaqueNetwork:
                                final OpaqueNetworkSummary opaqueNetworkSummary = (OpaqueNetworkSummary) this.vimConnection
                                        .getVimHelper().entityProps(newNetwork, "summary");
                                final VirtualEthernetCardOpaqueNetworkBackingInfo opaqueBack = new VirtualEthernetCardOpaqueNetworkBackingInfo();
                                opaqueBack.setOpaqueNetworkId(opaqueNetworkSummary.getOpaqueNetworkId());
                                opaqueBack.setOpaqueNetworkType(opaqueNetworkSummary.getOpaqueNetworkType());
                                virtualDevice.setBacking(opaqueBack);
                                break;
                            case DistributedVirtualPortgroup:
                                final DVPortgroupConfigInfo dvPortgroupConfigInfo = (DVPortgroupConfigInfo) this.vimConnection
                                        .getVimHelper().entityProps(newNetwork, "config");
                                final String uuid = (String) this.vimConnection.getVimHelper()
                                        .entityProps(dvPortgroupConfigInfo.getDistributedVirtualSwitch(), "uuid");
                                final VirtualEthernetCardDistributedVirtualPortBackingInfo dvpBack = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
                                final DistributedVirtualSwitchPortConnection dvsPortConnection = new DistributedVirtualSwitchPortConnection();
                                dvsPortConnection.setPortgroupKey(dvPortgroupConfigInfo.getKey());
                                dvsPortConnection.setSwitchUuid(uuid);
                                dvpBack.setPort(dvsPortConnection);
                                virtualDevice.setBacking(dvpBack);
                                break;
                            default:
                                break;
                            }
                            msg = String.format("Reconfigure vmnic%d backing port to %s", index, networkMapping[index]);
                            logger.warning(msg);

                            element.setDevice(virtualDevice);
                            element.setOperation(VirtualDeviceConfigSpecOperation.EDIT);
                            vmConfigSpec.getDeviceChange().add(element);

                            ++index;
                        }
                    } else {
                        if (virtualDevice instanceof VirtualSriovEthernetCard) {
                            ++index;
                            if (networkMapping[index] != null) {
                                msg = String.format("vmnic%d is backing an SRIOV port cannot be remapped to %s", index,
                                        networkMapping[index]);
                                logger.warning(msg);
                            }
                        }
                    }

                }

            }
            result = true;
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(logger, e);
            result = false;
        } catch (final InterruptedException e) {
            Utility.logWarning(logger, e);
            result = false;
            Thread.currentThread().interrupt();

        }
        return result;
    }

    public boolean reconfigureVm(final FcoArchiveManager vmArcMgr, final GenerationProfile profile,
            final CoreRestoreManagedInfo managedInfo) throws ConcurrentAccessFaultMsg, DuplicateNameFaultMsg,
            FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidNameFaultMsg,
            InvalidStateFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        boolean result = false;
        final List<VirtualControllerManager> vcmList = vmArcMgr.generateVirtualControllerManagerList(profile,
                managedInfo.getDatastoreInfo().getName());
        if (logger.isLoggable(Level.INFO)) {
            for (final VirtualControllerManager vcm : vcmList) {
                logger.info(vcm.toLog());
            }
        }
        final VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

        addEmptyDisks(vmConfigSpec, vcmList);
        reconfigureNetwork(vmConfigSpec, managedInfo.getNetworkMapping());

        if (managedInfo.getSpbmProfile() != null) {
            vmConfigSpec.getVmProfile().add(managedInfo.getSpbmProfile());
        }

        if (managedInfo.getVAppConfig() != null) {
            vmConfigSpec.setVAppConfig(managedInfo.getVAppConfig().toVmConfigInfo());
        }

        if (reconfigureVm(vmConfigSpec)) {
            logger.info("Reconfigure Virtual Machine success");
            logger.info("Update Config Manager");
            if (!vcmList.isEmpty()) {
                int numDisks = 0;
                for (final VirtualControllerManager vcm : vcmList) {
                    numDisks += vcm.getNumOfDisks();
                }
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("add empty %d disks succeeded.", numDisks);
                    logger.info(msg);
                }
            }
            result = true;
        } else {
            logger.warning("Fails: addEmptyDisks()");
            result = false;
        }
        return result;
    }

    private boolean reconfigureVm(final VirtualMachineConfigSpec vmConfigSpec)
            throws ConcurrentAccessFaultMsg, DuplicateNameFaultMsg, FileFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        boolean result = false;
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().reconfigVMTask(getMoref(), vmConfigSpec);
        if (this.vimConnection.waitForTask(taskMor)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Success: reconfigureVm ");
            }
            result = true;
        } else {
            logger.warning("Fails: reconfigureVm ");
            result = false;
        }

        return result;
    }

    public void reload() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        this.configMgr = new VirtualMachineConfigManager(this.vimConnection, getMoref());
    }

    public boolean reset() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException, InvalidStateFaultMsg, TaskInProgressFaultMsg {

        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().resetVMTask(getMoref());
        return this.vimConnection.waitForTask(taskMor);
    }

    public boolean revertToSnapshot(final ManagedObjectReference snapMor)
            throws FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg,
            VimTaskException, InterruptedException {
        final ManagedObjectReference taskMor = this.vimConnection.getVimPort().revertToSnapshotTask(snapMor, null,
                true);
        return this.vimConnection.waitForTask(taskMor);

    }

    /**
     * @param string
     * @param interactive
     * @return
     * @throws TaskInProgressFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws GuestOperationsFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws FileFaultFaultMsg
     * @throws InterruptedException
     * @throws IOException
     * @throws InvalidTokenException
     * @throws SafekeepingConnectionException
     */
    public boolean runCommand(final String guestProgramPath, final String password, final boolean interactive)
            throws GuestOperationsFaultFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, FileFaultFaultMsg, InterruptedException,
            IOException, InvalidTokenException, SafekeepingConnectionException {

        String[] opts;
        String[] opt;
        if (interactive) {
            opts = new String[] { "guest.interactiveGuestOperationsReady" };
            opt = new String[] { "guest.interactiveGuestOperationsReady" };
        } else {
            opts = new String[] { "guest.guestOperationsReady" };
            opt = new String[] { "guest.guestOperationsReady" };
        }
        getVimConnection().getVimHelper().wait(getMoref(), opts, opt, new Object[][] { new Object[] { true } },
                Utility.ONE_MINUTE_IN_SECONDS);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Guest Operations are ready for the VM");
        }
        final ManagedObjectReference guestOpManger = this.vimConnection.getServiceContent().getGuestOperationsManager();
        final Map<String, Object> guestOpMgr = this.vimConnection.getVimHelper().entityProps(guestOpManger,
                new String[] { "processManager", "fileManager", "authManager", "aliasManager" });
        final ManagedObjectReference fileManagerRef = (ManagedObjectReference) guestOpMgr.get("fileManager");
        final ManagedObjectReference processManagerRef = (ManagedObjectReference) guestOpMgr.get("processManager");
        final ManagedObjectReference authManagerRef = (ManagedObjectReference) guestOpMgr.get("authManager");
        final ManagedObjectReference aliasManagerRef = (ManagedObjectReference) guestOpMgr.get("aliasManager");
        if (logger.isLoggable(Level.FINE)) {
            final String msg = String.format("fileManagerRef:%s aliasManagerRef:%s", fileManagerRef.getValue(),
                    aliasManagerRef.getValue());
            logger.info(msg);
        }
        final SAMLTokenAuthentication auth = new SAMLTokenAuthentication();
        auth.setInteractiveSession(interactive);

        final Element element = ((PscProvider) getVimConnection().getSSoConnection())
                .acquireTokenByUserCredential(KeyType.BEARER, password, false, 600_000, false);

        final String sXml = DefaultTokenFactory.createTokenFromDom(element).toXml();

        auth.setToken(sXml);

        Files.write(Paths.get("C:\\Users\\mdaneri\\Downloads\\java.txt"), sXml.getBytes(),
                StandardOpenOption.CREATE_NEW);

        getVimConnection().getVimPort().validateCredentialsInGuest(authManagerRef, getMoref(), auth);

        final GuestProgramSpec spec = new GuestProgramSpec();
        spec.setProgramPath(guestProgramPath);
        spec.setArguments("");
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Starting the specified program inside the guest");
        }
        final long pid = getVimConnection().getVimPort().startProgramInGuest(processManagerRef, getMoref(), auth, spec);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Process ID of the program started is: " + pid);
        }

        final List<Long> pidsList = new ArrayList<>();
        pidsList.add(pid);
        List<GuestProcessInfo> procInfo = null;
        do {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Waiting for the process to finish running.");
            }
            procInfo = getVimConnection().getVimPort().listProcessesInGuest(processManagerRef, getMoref(), auth,
                    pidsList);
            Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);
        } while (procInfo.get(0).getEndTime() == null);
        return true;

    }

    public VirtualMachineSnapshotTree searchSnapshotTreeWithMoref(final List<VirtualMachineSnapshotTree> snapTree,
            final ManagedObjectReference snapmor) {
        VirtualMachineSnapshotTree snapNode = null;
        if (snapTree != null) {
            for (final VirtualMachineSnapshotTree node : snapTree) {

                logger.info("Snapshot Name : " + node.getName());

                if (MorefUtil.compare(snapmor, node.getSnapshot())) {
                    return node;
                } else {
                    final List<VirtualMachineSnapshotTree> childTree = node.getChildSnapshotList();
                    snapNode = searchSnapshotTreeWithMoref(childTree, snapmor);
                }
            }
        }
        return snapNode;

    }

    @Override
    public boolean setChangeBlockTracking(final boolean enable)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            SnapshotFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException, ConcurrentAccessFaultMsg,
            DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg {
        boolean result = false;
        if ((this.configMgr.isChangeTrackingEnabled() && enable)
                || (!this.configMgr.isChangeTrackingEnabled() && !enable)) {
            result = true;
        } else {
            final VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
            vmConfigSpec.setChangeTrackingEnabled(enable);

            final ManagedObjectReference taskMor = this.vimConnection.getVimPort().reconfigVMTask(getMoref(),
                    vmConfigSpec);
            result = this.vimConnection.waitForTask(taskMor);
            if (result) {
                final VirtualMachineRuntimeInfo runtimeInfo = getRuntimeInfo();
                if (runtimeInfo.getPowerState() == VirtualMachinePowerState.POWERED_ON) {

                    ManagedObjectReference taskSnapMor = this.vimConnection.getVimPort().createSnapshotExTask(
                            getMoref(), Utility.generateSnapshotName(), (enable ? "Enable" : "Disable") + " CBT", false,
                            null);
                    if (this.vimConnection.waitForTask(taskSnapMor)) {
                        final TaskInfo taskInfo = (TaskInfo) this.vimConnection.getVimHelper().entityProps(taskSnapMor,
                                "info");
                        ManagedObjectReference snap = (ManagedObjectReference) taskInfo.getResult();
                        if (snap != null) {
                            result = deleteSnapshot(snap, false, true);
                        }
                    }

                }
            }
        }
        return result;
    }

    /**
     * @param feature
     * @return
     * @throws JVixException
     * @throws Exception
     */
    public boolean setFeature(final Boolean feature) throws JVixException {
        long result;
        final Jvddk vddk = new Jvddk(logger, this);
        if (Boolean.TRUE.equals(feature)) {
            result = vddk.prepareForAccess("none");
        } else {
            result = vddk.endAccess("none");
        }

        return result == jDiskLibConst.VIX_OK;
    }

    public void setSnapshotManager(final SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    public boolean shutdownGuest()
            throws InvalidStateFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, ToolsUnavailableFaultMsg {

        this.vimConnection.getVimPort().shutdownGuest(getMoref());
        return true;

    }

    @Override
    public String toString() {
        return String.format("%-8s%36s\t%-8s\t%-30s ", getEntityType().toString(true), getUuid(), getMorefValue(),
                getShortedEntityName());
    }

    public boolean update(final ManagedObjectReference moRef)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        boolean result = false;
        this.vmMoref = moRef;
        this.configMgr = new VirtualMachineConfigManager(this.vimConnection, moRef);
        this.dataCenterInfo = this.vimConnection.getDatacenterByMoref(this.vmMoref);
        if (this.dataCenterInfo != null) {
            this.temporaryFcoInfo = null;
            result = true;
        }

        return result;
    }

    private boolean upload(final String vmPahtName, final StorageDirectoryInfo storageVmHomeDirectory,
            final byte[] byteArray) throws IOException {
        String msg;
        final String httpUrl = this.vimConnection.getURL().getProtocol() + "://" + this.vimConnection.getURL().getHost()
                + "/folder/" + vmPahtName + "?dcPath=" + storageVmHomeDirectory.getDcInfo().getName() + "&dsName="
                + storageVmHomeDirectory.getDsInfo().getName();
        StringUtils.replace(httpUrl, "\\ ", "%20");

        final URL fileURL;
        final HttpURLConnection conn;
        fileURL = new URL(httpUrl);
        conn = (HttpURLConnection) fileURL.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setAllowUserInteraction(true);

        String cookieValue = this.vimConnection.getCookie();

        final StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
        cookieValue = tokenizer.nextToken();
        final String pathData = "$" + tokenizer.nextToken();
        final String cookie = "$Version=\"1\"; " + cookieValue + "; " + pathData;

        final Map<String, List<String>> map = new HashMap<>();
        map.put("Cookie", Collections.singletonList(cookie));
        ((BindingProvider) this.vimConnection.getVimPort()).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                map);

        conn.setRequestProperty("Cookie", cookie);
        conn.setRequestProperty("Content-Type", "application/octet-stream");

        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Length", "1024");

        final long fileLen = byteArray.length;
        msg = String.format("Uploading VM File %s  %d bytes", httpUrl, fileLen);
        logger.info(msg);

        conn.setChunkedStreamingMode(0);

        try (InputStream in = new BufferedInputStream(new ByteArrayInputStream(byteArray));
                OutputStream out = conn.getOutputStream()) {
            final int bufLen = 9 * 1024;
            final byte[] buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;

            while ((len = in.read(buf, 0, bufLen)) != -1) {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len);
                out.write(tmp, 0, len);

            }
        } finally {
            try {
                conn.getResponseCode();
            } finally {
                conn.disconnect();
            }
        }
        return true;

    }

    public boolean useImprovedVirtuaDisk() {
        return this.vStorageObjectAssociations.size() > 0;
    }

}
