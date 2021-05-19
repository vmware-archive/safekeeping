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
package com.vmware.safekeeping.core.soap.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTimeConstants;
import org.w3c.dom.Element;

import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.exception.VimApplicationQuiesceFault;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.soap.IVimConnection;
import com.vmware.safekeeping.core.soap.helpers.builders.ObjectSpecBuilder;
import com.vmware.safekeeping.core.soap.helpers.builders.PropertyFilterSpecBuilder;
import com.vmware.safekeeping.core.soap.helpers.builders.PropertySpecBuilder;
import com.vmware.safekeeping.core.soap.helpers.builders.SelectionSpecBuilder;
import com.vmware.safekeeping.core.soap.helpers.builders.TraversalSpecBuilder;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vim25.ApplicationQuiesceFault;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.ObjectUpdate;
import com.vmware.vim25.ObjectUpdateKind;
import com.vmware.vim25.PropertyChange;
import com.vmware.vim25.PropertyChangeOp;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertyFilterUpdate;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.UpdateSet;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.WaitOptions;

public class MorefUtil {
    public static class HelperException extends RuntimeException {

        private static final long serialVersionUID = -3540376449643628875L;

        HelperException(final Throwable cause) {
            super(cause);
        }
    }

    static class PropertyCollector {
        private final ManagedObjectReference mor;
        private final VimPortType vimPort;

        PropertyCollector(final VimPortType vimPort, final ServiceContent serviceContent) throws RuntimeFaultFaultMsg {
            this.mor = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());
            this.vimPort = vimPort;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                this.vimPort.destroyPropertyCollector(this.mor);
            } catch (final RuntimeFaultFaultMsg e) {

            } finally {
                super.finalize();
            }
        }

        ManagedObjectReference getPropertyCollector() {
            return this.mor;
        }
    }

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MorefUtil.class.getName());

    private static final String SPEC_VISIT_FOLDER = "VisitFolders";

    public static boolean compare(final ManagedObjectReference a, final ManagedObjectReference b) {
        return (a.getType().compareTo(b.getType()) == 0) && (a.getValue().compareTo(b.getValue()) == 0);
    }

    public static ManagedObjectReference newManagedObjectReference(final EntityType entityType, final String morefStr) {
        return newManagedObjectReference(entityType.toString(), morefStr);
    }

    public static ManagedObjectReference newManagedObjectReference(final String entityType, final String morefStr) {
        if ((entityType == null) || (morefStr == null)) {
            return null;
        }

        final ManagedObjectReference mor = new ManagedObjectReference();
        mor.setType(entityType);
        mor.setValue(morefStr);
        return mor;
    }

    private final ServiceContent serviceContent;

    private final VimPortType vimPort;
    private final IVimConnection connection;

    private final ArrayBlockingQueue<PropertyCollector> priorityBlockingQueue;

    public MorefUtil(final IVimConnection connection, final int maxThreads) {
        this.priorityBlockingQueue = new ArrayBlockingQueue<>(maxThreads);
        this.connection = connection;
        if (connection != null) {
            this.vimPort = connection.getVimPort();
            this.serviceContent = this.connection.getServiceContent();
        } else {
            this.vimPort = null;
            this.serviceContent = null;
        }

        try {
            for (int i = 0; i < maxThreads; ++i) {
                this.priorityBlockingQueue.put(new PropertyCollector(this.vimPort, this.serviceContent));
            }
        } catch (final RuntimeFaultFaultMsg t) {
            throw new HelperException(t);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

    }

    private List<SelectionSpec> buildTraversalSpecForDatastoreToDatacenter() {

        final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name(SPEC_VISIT_FOLDER);
        final TraversalSpec visitFolders = new TraversalSpecBuilder().name(SPEC_VISIT_FOLDER).path("parent")
                .type("Folder").skip(false).selectSet(sspecvfolders);
        final TraversalSpec datastoreToFolder = new TraversalSpecBuilder().name("DatastoreToFolder").path("parent")
                .type(EntityType.Datastore.toString()).skip(false).selectSet(sspecvfolders);
        final List<SelectionSpec> speclist = new ArrayList<>();
        speclist.add(datastoreToFolder);
        speclist.add(visitFolders);
        return speclist;
    }

    /**
     * @return
     */
    private List<SelectionSpec> buildTraversalSpecForHostToDatacenter() {
        final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name(SPEC_VISIT_FOLDER);
        final TraversalSpec visitFolders = new TraversalSpecBuilder().name(SPEC_VISIT_FOLDER).path("parent")
                .type("hostFolder").skip(false).selectSet(sspecvfolders);
        final TraversalSpec hostSystemToFolder = new TraversalSpecBuilder().name("HostSystemToFolder").path("parent")
                .type(EntityType.HostSystem.toString()).skip(true).selectSet(sspecvfolders);
        final TraversalSpec folderTraversalSpec = new TraversalSpecBuilder().name("FolderTraversalSpec")
                .path("childEntity").type(EntityType.Folder.toString()).skip(true).selectSet(sspecvfolders);

        final List<SelectionSpec> speclist = new ArrayList<>();
        speclist.add(hostSystemToFolder);
        speclist.add(visitFolders);
        speclist.add(folderTraversalSpec);
        return speclist;
    }

    private List<SelectionSpec> buildTraversalSpecForVappToDatacenter() {

        final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name(SPEC_VISIT_FOLDER);
        final TraversalSpec visitFolders = new TraversalSpecBuilder().name(SPEC_VISIT_FOLDER).path("parent")
                .type("Folder").skip(false).selectSet(sspecvfolders);
        final SelectionSpec sspecvApp = new SelectionSpecBuilder().name("vAppToVApp");

        final SelectionSpec sspecvAppToFolder = new SelectionSpecBuilder().name("vAppToFolder");
        final TraversalSpec vAppToFolder = new TraversalSpecBuilder().type("VirtualApp").path("parentFolder")
                .skip(false).name("vAppToFolder").selectSet(sspecvfolders);
        final TraversalSpec vAppToVApp = new TraversalSpecBuilder().type("VirtualApp").path("parentVApp").skip(false)
                .name("vAppToVApp").selectSet(sspecvApp, sspecvAppToFolder);

        final List<SelectionSpec> speclist = new ArrayList<>();
        speclist.add(vAppToFolder);
        speclist.add(vAppToVApp);
        speclist.add(visitFolders);
        return speclist;
    }

    private List<SelectionSpec> buildTraversalSpecForVmToDatacenter() {

        final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name(SPEC_VISIT_FOLDER);
        final TraversalSpec visitFolders = new TraversalSpecBuilder().name(SPEC_VISIT_FOLDER).path("parent")
                .type("Folder").skip(false).selectSet(sspecvfolders);

        final SelectionSpec sspecvApp = new SelectionSpecBuilder().name("vAppToVApp");

        final SelectionSpec sspecvAppToFolder = new SelectionSpecBuilder().name("vAppToFolder");
        final TraversalSpec vAppToFolder = new TraversalSpecBuilder().type("VirtualApp").path("parentFolder")
                .skip(false).name("vAppToFolder").selectSet(sspecvfolders);
        final TraversalSpec vAppToVApp = new TraversalSpecBuilder().type("VirtualApp").path("parentVApp").skip(false)
                .name("vAppToVApp").selectSet(sspecvApp, sspecvAppToFolder);
        final TraversalSpec vmTovApp = new TraversalSpecBuilder().type("VirtualMachine").path("parentVApp").skip(false)
                .name("vmTovApp").selectSet(vAppToVApp, vAppToFolder);

        final TraversalSpec vmToFolder = new TraversalSpecBuilder().type("VirtualMachine").path("parent").skip(false)
                .name("vmToFolder").selectSet(sspecvfolders);

        final List<SelectionSpec> speclist = new ArrayList<>();
        speclist.add(vmToFolder);
        speclist.add(vmTovApp);
        speclist.add(visitFolders);
        return speclist;
    }

    public void close() {
//	  Consumer<? super PropertyCollector> action=		this.vimPort.destroyPropertyCollector(this.mor);
//
//	priorityBlockingQueue.forEach(action);
    }

    private RetrieveResult containerViewByType(final ManagedObjectReference container, final String morefType,
            final RetrieveOptions retrieveOptions)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return this.containerViewByType(container, morefType, retrieveOptions, "name");
    }

    private RetrieveResult containerViewByType(final ManagedObjectReference container, final String morefType,
            final RetrieveOptions retrieveOptions, final String... morefProperties)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final PropertyFilterSpec[] propertyFilterSpecs = propertyFilterSpecs(container, morefType, morefProperties);

        return containerViewByType(morefProperties, retrieveOptions, propertyFilterSpecs);
    }

    private RetrieveResult containerViewByType(final String[] morefProperties, final RetrieveOptions retrieveOptions,
            final PropertyFilterSpec... propertyFilterSpecs)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {

        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            return this.vimPort.retrievePropertiesEx(propertyCollector.getPropertyCollector(),
                    Arrays.asList(propertyFilterSpecs), retrieveOptions);
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    public ManagedObjectReference createManagedObjectReference(final EntityType entityType, final String morefStr) {
        return newManagedObjectReference(entityType, morefStr);
    }

    public String entityName(final ManagedObjectReference entityMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return entityProps(entityMor, "name").toString();
    }

    public Object entityProps(final ManagedEntityInfo entity, final String props)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return entityProps(entity.getMoref(), props);
    }

    public Map<String, Object> entityProps(final ManagedEntityInfo entity, final String[] props)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return entityProps(entity.getMoref(), props);
    }

    public Object entityProps(final ManagedObjectReference entityMor, final String props)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            Object retVal = null;

            final PropertyFilterSpec[] propertyFilterSpecs = { new PropertyFilterSpecBuilder().propSet(

                    new PropertySpecBuilder().all(Boolean.FALSE).type(entityMor.getType()).pathSet(props)).objectSet(

                            new ObjectSpecBuilder().obj(entityMor)) };
            final List<ObjectContent> oCont = this.vimPort
                    .retrievePropertiesEx(propertyCollector.getPropertyCollector(), Arrays.asList(propertyFilterSpecs),
                            new RetrieveOptions())
                    .getObjects();

            if (oCont != null) {
                for (final ObjectContent oc : oCont) {
                    final List<DynamicProperty> dps = oc.getPropSet();
                    for (final DynamicProperty dp : dps) {
                        retVal = dp.getVal();
                    }
                }
            }
            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    public Map<String, Object> entityProps(final ManagedObjectReference entityMor, final String[] props)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            final HashMap<String, Object> retVal = new HashMap<>();

            final PropertyFilterSpec[] propertyFilterSpecs = { new PropertyFilterSpecBuilder().propSet(

                    new PropertySpecBuilder().all(Boolean.FALSE).type(entityMor.getType()).pathSet(props)).objectSet(

                            new ObjectSpecBuilder().obj(entityMor)) };

            final RetrieveResult retrieveResult = this.vimPort.retrievePropertiesEx(
                    propertyCollector.getPropertyCollector(), Arrays.asList(propertyFilterSpecs),
                    new RetrieveOptions());

            retrieveObjectContent(propertyCollector, retrieveResult, retVal);
//	    if (oCont != null) {
//		for (final ObjectContent oc : oCont) {
//		    final List<DynamicProperty> dps = oc.getPropSet();
//		    for (final DynamicProperty dp : dps) {
//			retVal.put(dp.getName(), dp.getVal());
//		    }
//		}
//	    }
            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    private ManagedObjectReference fcoByName(final String name, final EntityType etype)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            ManagedObjectReference retVal = null;
            final ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
            final TraversalSpec tSpec = getVMTraversalSpec();

            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE).pathSet("name")
                    .type(etype.toString());

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(rootFolder).skip(Boolean.TRUE).selectSet(tSpec);

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder().propSet(propertySpec)
                    .objectSet(objectSpec);

            final List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
            listpfs.add(propertyFilterSpec);

            final RetrieveOptions options = new RetrieveOptions();
            RetrieveResult retrieve = this.vimPort.retrievePropertiesEx(propertyCollector.getPropertyCollector(),
                    listpfs, options);

            final List<ObjectContent> listobcont = new ArrayList<>(retrieve.getObjects());
            while (retrieve.getToken() != null) {
                retrieve = this.vimPort.continueRetrievePropertiesEx(propertyCollector.getPropertyCollector(),
                        retrieve.getToken());
                listobcont.addAll(retrieve.getObjects());
            }
            for (final ObjectContent oc : listobcont) {
                final ManagedObjectReference mr = oc.getObj();
                String vmnm = null;
                final List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (final DynamicProperty dp : dps) {
                        vmnm = (String) dp.getVal();
                    }
                }
                if ((vmnm != null) && vmnm.equals(name)) {
                    retVal = mr;
                    break;
                }
            }
            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }

    }

    public Map<String, Vector<Object>> getAllVAppList(ManagedObjectReference rootFolder)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final HashMap<String, Vector<Object>> retVal = new HashMap<>();
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            if (rootFolder == null) {
                rootFolder = this.serviceContent.getRootFolder();
            }
            final TraversalSpec tSpec = getVMTraversalSpec();

            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE).pathSet("name")
                    .pathSet("vAppConfig").type("VirtualApp");

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(rootFolder).skip(Boolean.TRUE).selectSet(tSpec);

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder().propSet(propertySpec)
                    .objectSet(objectSpec);

            final List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
            listpfs.add(propertyFilterSpec);

            final RetrieveOptions options = new RetrieveOptions();
            final RetrieveResult retrieveResult = this.vimPort
                    .retrievePropertiesEx(propertyCollector.getPropertyCollector(), listpfs, options);

            retrieveObjectContent(propertyCollector, retrieveResult, retVal);
//	    if (listobcont != null) {
//		for (final ObjectContent oc : listobcont) {
//		    final ManagedObjectReference mr = oc.getObj();
//		    String vmname = null;
//		    final List<DynamicProperty> dps = oc.getPropSet();
//		    final Vector<Object> a = new Vector<>();
//		    a.add(mr);
//		    if (dps != null) {
//			for (final DynamicProperty dp : dps) {
//			    switch (dp.getName()) {
//			    case "name":
//				vmname = (String) dp.getVal();
//				break;
//			    case "vAppConfig":
//				a.add(dp.getVal());
//				break;
//			    default:
//				break;
//			    }
//			}
//
//			retVal.put(vmname, a);
//		    }
//		}
//	    }
            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    public Map<String, Vector<Object>> getAllVmList(ManagedObjectReference rootFolder)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final HashMap<String, Vector<Object>> retVal = new HashMap<>();
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            if (rootFolder == null) {
                rootFolder = this.serviceContent.getRootFolder();
            }
            final TraversalSpec tSpec = getVMTraversalSpec();

            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE).pathSet("name")
                    .pathSet("config").type("VirtualMachine");

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(rootFolder).skip(Boolean.TRUE).selectSet(tSpec);

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder().propSet(propertySpec)
                    .objectSet(objectSpec);

            final List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
            listpfs.add(propertyFilterSpec);

            final RetrieveOptions options = new RetrieveOptions();
            final RetrieveResult retrieveResult = this.vimPort
                    .retrievePropertiesEx(propertyCollector.getPropertyCollector(), listpfs, options);
            if (retrieveResult != null) {
                retrieveObjectContent(propertyCollector, retrieveResult, retVal);
            }
            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    /**
     * Search Datacenter by host
     *
     * @param hostMor
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     */
    public ManagedEntityInfo getDatacenterByHostsystem(final ManagedObjectReference hostMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            ManagedObjectReference dcMor = null;
            String name = null;
            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE)
                    .type(EntityType.Datacenter.toString());
            propertySpec.getPathSet().add("name");

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(hostMor).skip(Boolean.TRUE);
            objectSpec.getSelectSet().addAll(buildTraversalSpecForHostToDatacenter());

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            final List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<>();
            propertyFilterSpecs.add(propertyFilterSpec);

            final List<ObjectContent> oCont = this.vimPort.retrievePropertiesEx(
                    propertyCollector.getPropertyCollector(), propertyFilterSpecs, new RetrieveOptions()).getObjects();

            if (oCont != null) {
                final ObjectContent oc = oCont.get(0);
                dcMor = oc.getObj();
                name = oc.getPropSet().get(0).getVal().toString();
            }
            return new ManagedEntityInfo(name, dcMor, this.connection.getServerIntanceUuid());
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    public ManagedEntityInfo getDatacenterByMoref(final ManagedObjectReference moref)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        ManagedObjectReference dcMor = null;
        String name = null;
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE).type("Datacenter");
            propertySpec.getPathSet().add("name");

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(moref).skip(Boolean.TRUE);

            switch (EntityType.toEntityType(moref.getType())) {
            case VirtualMachine:
                objectSpec.getSelectSet().addAll(buildTraversalSpecForVmToDatacenter());
                break;
            case Datastore:
                objectSpec.getSelectSet().addAll(buildTraversalSpecForDatastoreToDatacenter());
                break;
            case VirtualApp:
                objectSpec.getSelectSet().addAll(buildTraversalSpecForVappToDatacenter());
                break;
            default:
                return null;

            }

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            final List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<>();
            propertyFilterSpecs.add(propertyFilterSpec);

            List<ObjectContent> oCont;
            oCont = this.vimPort.retrievePropertiesEx(propertyCollector.getPropertyCollector(), propertyFilterSpecs,
                    new RetrieveOptions()).getObjects();

            if (oCont != null) {
                for (final ObjectContent oc : oCont) {
                    dcMor = oc.getObj();
                    name = oc.getPropSet().get(0).getVal().toString();

                }
            }

            return new ManagedEntityInfo(name, dcMor, this.connection.getServerIntanceUuid());
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    /**
     * Search host by datacenter
     *
     * @param datacenterMor
     * @return
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public ManagedEntityInfo getHostsystemByDatacenter(final ManagedObjectReference datacenterMor)
            throws InterruptedException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference dcMor = null;
        String name = null;
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE)
                    .type(EntityType.HostSystem.toString());
            propertySpec.getPathSet().add("name");

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(datacenterMor).skip(Boolean.TRUE);
            objectSpec.getSelectSet().addAll(buildTraversalSpecForHostToDatacenter());

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            final List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<>();
            propertyFilterSpecs.add(propertyFilterSpec);

            final List<ObjectContent> oCont = this.vimPort.retrievePropertiesEx(
                    propertyCollector.getPropertyCollector(), propertyFilterSpecs, new RetrieveOptions()).getObjects();

            if (oCont != null) {
                final ObjectContent oc = oCont.get(0);
                dcMor = oc.getObj();
                name = oc.getPropSet().get(0).getVal().toString();
            }

            return new ManagedEntityInfo(name, dcMor, this.connection.getServerIntanceUuid());
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    public TraversalSpec getVMTraversalSpec() {

        final TraversalSpec vAppToVM = new TraversalSpecBuilder().name("vAppToVM").type("VirtualApp").path("vm");

        final TraversalSpec vAppToVApp = new TraversalSpecBuilder().name("vAppToVApp").type("VirtualApp")
                .path("resourcePool").selectSet(

                        new SelectionSpecBuilder().name("vAppToVApp"), new SelectionSpecBuilder().name("vAppToVM"));

        final SelectionSpec visitFolders = new SelectionSpecBuilder().name(SPEC_VISIT_FOLDER);

        final TraversalSpec dataCenterToVMFolder = new TraversalSpecBuilder().name("DataCenterToVMFolder")
                .type("Datacenter").path("vmFolder").skip(false).selectSet(visitFolders);

        return new TraversalSpecBuilder().name(SPEC_VISIT_FOLDER).type("Folder").path("childEntity").skip(false)
                .selectSet(visitFolders, dataCenterToVMFolder, vAppToVM, vAppToVApp);
    }

    public Map<String, ManagedObjectReference> inContainerByType(final ManagedObjectReference container,
            final String morefType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return inContainerByType(container, morefType, new RetrieveOptions());
    }

    private Map<String, ManagedObjectReference> inContainerByType(final ManagedObjectReference folder,
            final String morefType, final RetrieveOptions retrieveOptions)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        final RetrieveResult rslts = containerViewByType(folder, morefType, retrieveOptions);
        return toMap(rslts);
    }

    public Map<String, ManagedObjectReference> inFolderByType(final ManagedObjectReference folder,
            final EntityType morefType, final RetrieveOptions retrieveOptions)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return inFolderByType(folder, morefType.toString(), retrieveOptions);
    }

    public Map<String, ManagedObjectReference> inFolderByType(final ManagedObjectReference folder,
            final String morefType, final RetrieveOptions retrieveOptions)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final PropertyFilterSpec[] propertyFilterSpecs = propertyFilterSpecs(folder, morefType, "name");
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            RetrieveResult results = this.vimPort.retrievePropertiesEx(propertyCollector.getPropertyCollector(),
                    Arrays.asList(propertyFilterSpecs), retrieveOptions);

            final Map<String, ManagedObjectReference> tgtMoref = new HashMap<>();
            while ((results != null) && !results.getObjects().isEmpty()) {
                resultsToTgtMorefMap(results, tgtMoref);
                final String token = results.getToken();

                results = (token != null)
                        ? this.vimPort.continueRetrievePropertiesEx(propertyCollector.getPropertyCollector(), token)
                        : null;
            }

            return tgtMoref;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    private String populate(final RetrieveResult rslts, final Map<String, ManagedObjectReference> tgtMoref) {
        String token = null;
        if (rslts != null) {
            token = rslts.getToken();
            for (final ObjectContent oc : rslts.getObjects()) {
                final ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                final List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (final DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
        return token;
    }

    private PropertyFilterSpec propertyFilterSpec(final ManagedObjectReference objmor, final String[] filterProps) {
        final PropertyFilterSpec spec = new PropertyFilterSpec();
        final ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(objmor);
        oSpec.setSkip(Boolean.FALSE);
        spec.getObjectSet().add(oSpec);

        final PropertySpec pSpec = new PropertySpec();
        pSpec.getPathSet().addAll(Arrays.asList(filterProps));
        pSpec.setType(objmor.getType());
        spec.getPropSet().add(pSpec);
        return spec;
    }

    private PropertyFilterSpec[] propertyFilterSpecs(final ManagedObjectReference container, final String morefType,
            final String... morefProperties) throws RuntimeFaultFaultMsg {

        final ManagedObjectReference viewManager = this.serviceContent.getViewManager();
        final ManagedObjectReference containerView = this.vimPort.createContainerView(viewManager, container,
                Arrays.asList(morefType), true);

        return new PropertyFilterSpec[] { new PropertyFilterSpecBuilder()
                .propSet(new PropertySpecBuilder().all(Boolean.FALSE).type(morefType).pathSet(morefProperties))
                .objectSet(new ObjectSpecBuilder().obj(containerView).skip(Boolean.TRUE).selectSet(
                        new TraversalSpecBuilder().name("view").path("view").skip(false).type("ContainerView"))) };
    }

    private void releasePropertyCollector(final PropertyCollector propertyCollector) throws InterruptedException {
        this.priorityBlockingQueue.put(propertyCollector);
    }

    private PropertyCollector reservePropertyCollector() throws InterruptedException {
        return this.priorityBlockingQueue.take();
    }

    private void resultsToTgtMorefMap(final RetrieveResult results,
            final Map<String, ManagedObjectReference> tgtMoref) {
        final List<ObjectContent> oCont = (results != null) ? results.getObjects() : null;

        if (oCont != null) {
            for (final ObjectContent oc : oCont) {
                final ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                final List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (final DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
    }

    private int retrieveObjectContent(final PropertyCollector propertyCollector, RetrieveResult retrieveResult,
            final HashMap<String, Vector<Object>> retVal) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        while (true) {
            final List<ObjectContent> listobcont = retrieveResult.getObjects();
            if (listobcont != null) {
                for (final ObjectContent oc : listobcont) {
                    final ManagedObjectReference mr = oc.getObj();
                    String vmname = null;
                    final List<DynamicProperty> dps = oc.getPropSet();
                    final Vector<Object> a = new Vector<>();
                    a.add(mr);
                    if (dps != null) {
                        for (final DynamicProperty dp : dps) {
                            switch (dp.getName()) {
                            case "name":
                                vmname = (String) dp.getVal();
                                break;
                            case "config":
                            case "vAppConfig":
                                a.add(dp.getVal());
                                break;
                            default:
                                break;
                            }
                        }

                        retVal.put(vmname, a);
                    }
                }
            }
            if ((retrieveResult.getToken() != null)) {
                retrieveResult = this.vimPort.continueRetrievePropertiesEx(propertyCollector.getPropertyCollector(),
                        retrieveResult.getToken());
            } else {
                break;
            }
        }
        return retVal.size();
    }

    private int retrieveObjectContent(final PropertyCollector propertyCollector, RetrieveResult retrieveResult,
            final Map<String, Object> retVal) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        while (true) {
            final List<ObjectContent> listobcont = retrieveResult.getObjects();
            if (listobcont != null) {
                for (final ObjectContent oc : listobcont) {
                    final List<DynamicProperty> dps = oc.getPropSet();
                    for (final DynamicProperty dp : dps) {
                        retVal.put(dp.getName(), dp.getVal());
                    }
                }

            }
            if ((retrieveResult.getToken() != null)) {
                retrieveResult = this.vimPort.continueRetrievePropertiesEx(propertyCollector.getPropertyCollector(),
                        retrieveResult.getToken());
            } else {
                break;
            }
        }
        return retVal.size();

    }

    public Object taskResult(final ManagedObjectReference morTask)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return entityProps(morTask, "info.result");

    }

    public TaskInfoState taskWait(final ManagedObjectReference morTask, final Integer maxWaitSeconds)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException,
            InterruptedException {

        final Object[] waitResult = wait(morTask, new String[] { "info.state", "info.error" }, new String[] { "state" },
                new Object[][] { new Object[] { TaskInfoState.SUCCESS, TaskInfoState.ERROR } }, maxWaitSeconds);
        if (waitResult[0] == TaskInfoState.SUCCESS) {
            // do nothing
        } else {
            if (waitResult[1] instanceof LocalizedMethodFault) {
                final LocalizedMethodFault local = (LocalizedMethodFault) waitResult[1];
                if (local.getFault() instanceof ApplicationQuiesceFault) {
                    throw new VimApplicationQuiesceFault(local);
                } else {
                    throw new VimTaskException(local);
                }
            }
        }

        return (TaskInfoState) waitResult[0];
    }

    public TaskInfoState taskWait(final ManagedObjectReference morTask, final RunningReport runRep,
            final Integer maxWaitSeconds)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimTaskException {
        TaskInfoState result = null;
        runRep.progressPercent = 0;
        boolean done = false;
        final Thread t = new Thread(runRep);
        try {
            t.start();
            while (!done) {
                final Map<String, Object> task = entityProps(morTask,
                        new String[] { "info.state", "info.error", "info.progress" });
                if (task.containsKey("info.progress")) {
                    runRep.progressPercent = (int) task.get("info.progress");
                }
                result = (TaskInfoState) task.get("info.state");
                switch (result) {
                case ERROR:
                    done = true;
                    if (task.containsKey("info.error")) {
                        final LocalizedMethodFault local = (LocalizedMethodFault) task.get("info.error");
                        throw new VimTaskException(local);
                    }
                    break;
                case QUEUED:
                case RUNNING:
                    Thread.sleep(5L * DateTimeConstants.MILLIS_PER_SECOND);
                    break;
                case SUCCESS:
                    done = true;
                    break;
                }
            }
        } catch (final InterruptedException e) {
            runRep.error = true;
            runRep.errorMessage = e.getLocalizedMessage();
            throw e;
        } finally {
            if (t.isAlive()) {
                t.interrupt();
            }
            Thread.sleep(DateTimeConstants.MILLIS_PER_SECOND);
        }
        return result;
    }

    private Map<String, ManagedObjectReference> toMap(RetrieveResult rslts)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            final Map<String, ManagedObjectReference> tgtMoref = new HashMap<>();
            String token;
            token = populate(rslts, tgtMoref);

            while ((token != null) && !token.isEmpty()) {

                rslts = this.vimPort.continueRetrievePropertiesEx(propertyCollector.getPropertyCollector(), token);

                token = populate(rslts, tgtMoref);
            }

            return tgtMoref;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }

    private void updateValues(final String[] props, final Object[] vals, final PropertyChange propchg) {
        for (int findi = 0; findi < props.length; findi++) {
            if (propchg.getName().lastIndexOf(props[findi]) >= 0) {
                if (propchg.getOp() == PropertyChangeOp.REMOVE) {
                    vals[findi] = "";
                } else {
                    vals[findi] = propchg.getVal();
                }
            }
        }
    }

    public ManagedObjectReference vAppByName(final String vmName)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return fcoByName(vmName, EntityType.VirtualApp);
    }

    public ManagedObjectReference vAppByUuid(final String uuid)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        ManagedObjectReference retVal = null;
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            final ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
            final TraversalSpec tSpec = getVMTraversalSpec();

            final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE)
                    .pathSet("vAppConfig.instanceUuid").type(EntityType.VirtualApp.toString());

            final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(rootFolder).skip(Boolean.TRUE).selectSet(tSpec);

            final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder().propSet(propertySpec)
                    .objectSet(objectSpec);

            final List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
            listpfs.add(propertyFilterSpec);

            final RetrieveOptions options = new RetrieveOptions();
            RetrieveResult retrieve = this.vimPort.retrievePropertiesEx(propertyCollector.getPropertyCollector(),
                    listpfs, options);
            final List<ObjectContent> listobcont = new ArrayList<>(retrieve.getObjects());
            while (retrieve.getToken() != null) {
                retrieve = this.vimPort.continueRetrievePropertiesEx(propertyCollector.getPropertyCollector(),
                        retrieve.getToken());
                listobcont.addAll(retrieve.getObjects());
            }
            for (final ObjectContent oc : listobcont) {
                final ManagedObjectReference mr = oc.getObj();
                String vmnm = null;
                final List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (final DynamicProperty dp : dps) {
                        vmnm = (String) dp.getVal();
                    }
                }
                if ((vmnm != null) && vmnm.equals(uuid)) {
                    retVal = mr;
                    break;
                }
            }

            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }

    }

    public ManagedObjectReference vmByName(final String vmName)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return fcoByName(vmName, EntityType.VirtualMachine);
    }

    public Object[] wait(final ManagedObjectReference objmor, final String[] filterProps, final String[] endWaitProps,
            final Object[][] expectedVals, final Integer maxWaitSeconds) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg, InterruptedException {
        final PropertyCollector propertyCollector = reservePropertyCollector();
        try {
            ManagedObjectReference filterSpecRef;

            String version = "";
            final Object[] endVals = new Object[endWaitProps.length];
            final Object[] filterVals = new Object[filterProps.length];
            String stateVal = null;

            final PropertyFilterSpec spec = propertyFilterSpec(objmor, filterProps);

            filterSpecRef = this.vimPort.createFilter(propertyCollector.getPropertyCollector(), spec, true);

            boolean reached = false;

            UpdateSet updateset = null;
            List<PropertyFilterUpdate> filtupary = null;
            List<ObjectUpdate> objupary = null;
            List<PropertyChange> propchgary = null;
            final WaitOptions waitOption = new WaitOptions();
            waitOption.setMaxWaitSeconds(maxWaitSeconds);
            while (!reached) {
                updateset = this.vimPort.waitForUpdatesEx(propertyCollector.getPropertyCollector(), version,
                        waitOption);
                if ((updateset == null) || (updateset.getFilterSet() == null)) {
                    continue;
                }
                version = updateset.getVersion();

                filtupary = updateset.getFilterSet();

                for (final PropertyFilterUpdate filtup : filtupary) {
                    objupary = filtup.getObjectSet();
                    for (final ObjectUpdate objup : objupary) {

                        if ((objup.getKind() == ObjectUpdateKind.MODIFY) || (objup.getKind() == ObjectUpdateKind.ENTER)
                                || (objup.getKind() == ObjectUpdateKind.LEAVE)) {
                            propchgary = objup.getChangeSet();
                            for (final PropertyChange propchg : propchgary) {

                                updateValues(endWaitProps, endVals, propchg);
                                updateValues(filterProps, filterVals, propchg);
                            }
                        }
                    }
                }
                Object expctdval = null;

                for (int chgi = 0; (chgi < endVals.length) && !reached; chgi++) {
                    for (int vali = 0; (vali < expectedVals[chgi].length) && !reached; vali++) {
                        expctdval = expectedVals[chgi][vali];
                        if (endVals[chgi] == null) {
                            // Do Nothing
                        } else if (endVals[chgi].toString().contains("val: null")) {
                            // Due to some issue in JAX-WS De-serialization getting the information from
                            // the nodes
                            final Element stateElement = (Element) endVals[chgi];
                            if ((stateElement != null) && (stateElement.getFirstChild() != null)) {
                                stateVal = stateElement.getFirstChild().getTextContent();
                                reached = expctdval.toString().equalsIgnoreCase(stateVal) || reached;
                            }
                        } else {
                            expctdval = expectedVals[chgi][vali];
                            reached = expctdval.equals(endVals[chgi]) || reached;
                            stateVal = "filtervals";
                        }
                    }
                }
            }
            Object[] retVal = null;
            this.vimPort.destroyPropertyFilter(filterSpecRef);

            if (stateVal != null) {
                if (stateVal.equalsIgnoreCase(HttpNfcLeaseState.READY.value())) {
                    retVal = new Object[] { HttpNfcLeaseState.READY };
                } else if (stateVal.equalsIgnoreCase(HttpNfcLeaseState.ERROR.value())) {
                    retVal = new Object[] { HttpNfcLeaseState.ERROR };
                } else if ("filtervals".equals(stateVal)) {
                    retVal = filterVals;
                } else if (stateVal.equals(TaskInfoState.SUCCESS.value())) {
                    retVal = new Object[] { TaskInfoState.SUCCESS };
                } else if (stateVal.equals(TaskInfoState.ERROR.value())) {
                    retVal = new Object[] { TaskInfoState.ERROR };
                } else {
                    retVal = new Object[] { TaskInfoState.ERROR };
                }
            } else {
                retVal = new Object[] { HttpNfcLeaseState.ERROR };
            }

            return retVal;
        } finally {
            releasePropertyCollector(propertyCollector);
        }
    }
}
