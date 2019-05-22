/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.soap.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vmbk.soap.IVimConnection;
import com.vmware.vmbk.soap.helpers.builders.ObjectSpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.PropertyFilterSpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.PropertySpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.SelectionSpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.TraversalSpecBuilder;
import com.vmware.vmbk.type.EntityType;

public class MorefUtil extends BaseHelper {

    public static Boolean compare(final ManagedObjectReference a, final ManagedObjectReference b) {
	return (a.getType().compareTo(b.getType()) == 0) && (a.getValue().compareTo(b.getValue()) == 0);
    }

    public static ManagedObjectReference create(final EntityType entityType, final String morefStr) {
	if ((entityType == null) || (morefStr == null)) {
	    return null;
	}

	final ManagedObjectReference mor = new ManagedObjectReference();
	mor.setType(entityType.toString());
	mor.setValue(morefStr);

	return mor;
    }

    private static String populate(final RetrieveResult rslts, final Map<String, ManagedObjectReference> tgtMoref) {
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

    private ServiceContent serviceContent;

    private VimPortType vimPort;

    public MorefUtil(final IVimConnection connection) {
	super(connection);
    }

    private RetrieveResult containerViewByType(final ManagedObjectReference container, final String morefType,
	    final RetrieveOptions retrieveOptions) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
	return this.containerViewByType(container, morefType, retrieveOptions, "name");
    }

    private RetrieveResult containerViewByType(final ManagedObjectReference container, final String morefType,
	    final RetrieveOptions retrieveOptions, final String... morefProperties)
	    throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
	init();

	final PropertyFilterSpec[] propertyFilterSpecs = propertyFilterSpecs(container, morefType, morefProperties);

	return containerViewByType(container, morefType, morefProperties, retrieveOptions, propertyFilterSpecs);
    }

    private RetrieveResult containerViewByType(final ManagedObjectReference container, final String morefType,
	    final String[] morefProperties, final RetrieveOptions retrieveOptions,
	    final PropertyFilterSpec... propertyFilterSpecs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
	init();
	return this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(),
		Arrays.asList(propertyFilterSpecs), retrieveOptions);
    }

    public String entityName(final ManagedObjectReference entityMor)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	return entityProps(entityMor, "name").toString();
    }

// TODO Remove unused code found by UCDetector
//     public Map<ManagedObjectReference, Map<String, Object>> entityProps(final List<ManagedObjectReference> entityMors,
// 	    final String[] props) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
// 	init();
//
// 	final Map<ManagedObjectReference, Map<String, Object>> retVal = new HashMap<>();
//
// 	final PropertyFilterSpecBuilder propertyFilterSpec = new PropertyFilterSpecBuilder();
// 	final Map<String, String> typesCovered = new HashMap<>();
//
// 	for (final ManagedObjectReference mor : entityMors) {
// 	    if (!typesCovered.containsKey(mor.getType())) {
//
// 		propertyFilterSpec
// 			.propSet(new PropertySpecBuilder().all(Boolean.FALSE).type(mor.getType()).pathSet(props));
// 		typesCovered.put(mor.getType(), "");
// 	    }
//
// 	    propertyFilterSpec.objectSet(new ObjectSpecBuilder().obj(mor));
// 	}
// 	final List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<>();
// 	propertyFilterSpecs.add(propertyFilterSpec);
//
// 	RetrieveResult rslts = this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(),
// 		propertyFilterSpecs, new RetrieveOptions());
//
// 	final List<ObjectContent> listobjcontent = new ArrayList<>();
// 	String token = populate(rslts, listobjcontent);
// 	while ((token != null) && !token.isEmpty()) {
// 	    rslts = this.vimPort.continueRetrievePropertiesEx(this.serviceContent.getPropertyCollector(), token);
//
// 	    token = populate(rslts, listobjcontent);
// 	}
//
// 	for (final ObjectContent oc : listobjcontent) {
// 	    final List<DynamicProperty> dps = oc.getPropSet();
// 	    final Map<String, Object> propMap = new HashMap<>();
// 	    if (dps != null) {
// 		for (final DynamicProperty dp : dps) {
// 		    propMap.put(dp.getName(), dp.getVal());
// 		}
// 	    }
// 	    retVal.put(oc.getObj(), propMap);
// 	}
// 	return retVal;
//     }

    public Object entityProps(final ManagedObjectReference entityMor, final String props)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	init();

	Object retVal = null;

	final PropertyFilterSpec[] propertyFilterSpecs = { new PropertyFilterSpecBuilder().propSet(

		new PropertySpecBuilder().all(Boolean.FALSE).type(entityMor.getType()).pathSet(props)).objectSet(

			new ObjectSpecBuilder().obj(entityMor)) };

	final List<ObjectContent> oCont = this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(),
		Arrays.asList(propertyFilterSpecs), new RetrieveOptions()).getObjects();

	if (oCont != null) {
	    for (final ObjectContent oc : oCont) {
		final List<DynamicProperty> dps = oc.getPropSet();
		for (final DynamicProperty dp : dps) {
		    retVal = dp.getVal();
		}
	    }
	}
	return retVal;
    }

    public Map<String, Object> entityProps(final ManagedObjectReference entityMor, final String[] props)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

	init();

	final HashMap<String, Object> retVal = new HashMap<>();

	final PropertyFilterSpec[] propertyFilterSpecs = { new PropertyFilterSpecBuilder().propSet(

		new PropertySpecBuilder().all(Boolean.FALSE).type(entityMor.getType()).pathSet(props)).objectSet(

			new ObjectSpecBuilder().obj(entityMor)) };

	final List<ObjectContent> oCont = this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(),
		Arrays.asList(propertyFilterSpecs), new RetrieveOptions()).getObjects();

	if (oCont != null) {
	    for (final ObjectContent oc : oCont) {
		final List<DynamicProperty> dps = oc.getPropSet();
		for (final DynamicProperty dp : dps) {
		    retVal.put(dp.getName(), dp.getVal());
		}
	    }
	}
	return retVal;
    }

    private ManagedObjectReference fcoByName(final String name, final ManagedObjectReference propCollectorRef,
	    final EntityType etype) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

	init();
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
	RetrieveResult retrieve = this.vimPort.retrievePropertiesEx(propCollectorRef, listpfs, options);
	final List<ObjectContent> listobcont = new ArrayList<>();
	listobcont.addAll(retrieve.getObjects());
	while (retrieve.getToken() != null) {
	    retrieve = this.vimPort.continueRetrievePropertiesEx(propCollectorRef, retrieve.getToken());
	    listobcont.addAll(retrieve.getObjects());
	}
	if (listobcont != null) {
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
	}
	return retVal;
    }

    public Map<String, Vector<Object>> getAllVAppList(ManagedObjectReference rootFolder)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	final HashMap<String, Vector<Object>> retVal = new HashMap<>();

	init();
	final ManagedObjectReference propCollectorRef = this.serviceContent.getPropertyCollector();

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
	final List<ObjectContent> listobcont = this.vimPort.retrievePropertiesEx(propCollectorRef, listpfs, options)
		.getObjects();

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
			case "vAppConfig":
			    a.add(dp.getVal());
			    break;
			}
		    }

		    retVal.put(vmname, a);
		}
	    }
	}
	return retVal;
    }

    public Map<String, Vector<Object>> getAllVmList(ManagedObjectReference rootFolder)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	final HashMap<String, Vector<Object>> retVal = new HashMap<>();

	init();
	final ManagedObjectReference propCollectorRef = this.serviceContent.getPropertyCollector();

	if (rootFolder == null) {
	    rootFolder = this.serviceContent.getRootFolder();
	}
	final TraversalSpec tSpec = getVMTraversalSpec();

	final PropertySpec propertySpec = new PropertySpecBuilder().all(Boolean.FALSE).pathSet("name").pathSet("config")
		.type("VirtualMachine");

	final ObjectSpec objectSpec = new ObjectSpecBuilder().obj(rootFolder).skip(Boolean.TRUE).selectSet(tSpec);

	final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder().propSet(propertySpec)
		.objectSet(objectSpec);

	final List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
	listpfs.add(propertyFilterSpec);

	final RetrieveOptions options = new RetrieveOptions();
	final List<ObjectContent> listobcont = this.vimPort.retrievePropertiesEx(propCollectorRef, listpfs, options)
		.getObjects();

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
			    a.add(dp.getVal());
			    break;
			}
		    }

		    retVal.put(vmname, a);
		}
	    }
	}
	return retVal;
    }

    public TraversalSpec getVMTraversalSpec() {

	final TraversalSpec vAppToVM = new TraversalSpecBuilder().name("vAppToVM").type("VirtualApp").path("vm");

	final TraversalSpec vAppToVApp = new TraversalSpecBuilder().name("vAppToVApp").type("VirtualApp")
		.path("resourcePool").selectSet(

			new SelectionSpecBuilder().name("vAppToVApp"), new SelectionSpecBuilder().name("vAppToVM"));

	final SelectionSpec visitFolders = new SelectionSpecBuilder().name("VisitFolders");

	final TraversalSpec dataCenterToVMFolder = new TraversalSpecBuilder().name("DataCenterToVMFolder")
		.type("Datacenter").path("vmFolder").skip(false).selectSet(visitFolders);

	return new TraversalSpecBuilder().name("VisitFolders").type("Folder").path("childEntity").skip(false)
		.selectSet(visitFolders, dataCenterToVMFolder, vAppToVM, vAppToVApp);
    }

    public Map<String, ManagedObjectReference> inContainerByType(final ManagedObjectReference container,
	    final String morefType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	return inContainerByType(container, morefType, new RetrieveOptions());
    }

// TODO Remove unused code found by UCDetector
//     public Map<ManagedObjectReference, Map<String, Object>> inContainerByType(final ManagedObjectReference container,
// 	    final String morefType, final String[] strings) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
// 	return inContainerByType(container, morefType, strings, new RetrieveOptions());
//     }

    private Map<String, ManagedObjectReference> inContainerByType(final ManagedObjectReference folder,
	    final String morefType, final RetrieveOptions retrieveOptions)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

	init();
	final RetrieveResult rslts = containerViewByType(folder, morefType, retrieveOptions);
	return toMap(rslts);
    }

// TODO Remove unused code found by UCDetector
//     public Map<String, ManagedObjectReference> inFolderByType(final ManagedObjectReference folder,
// 	    final String morefType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
// 	return inFolderByType(folder, morefType, new RetrieveOptions());
//     }

    public Map<String, ManagedObjectReference> inFolderByType(final ManagedObjectReference folder,
	    final EntityType morefType, final RetrieveOptions retrieveOptions)
	    throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
	return inFolderByType(folder, morefType.toString(), retrieveOptions);
    }

    public Map<String, ManagedObjectReference> inFolderByType(final ManagedObjectReference folder,
	    final String morefType, final RetrieveOptions retrieveOptions)
	    throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
	final PropertyFilterSpec[] propertyFilterSpecs = propertyFilterSpecs(folder, morefType, "name");

	final ManagedObjectReference propertyCollector = this.serviceContent.getPropertyCollector();

	RetrieveResult results = this.vimPort.retrievePropertiesEx(propertyCollector,
		Arrays.asList(propertyFilterSpecs), retrieveOptions);

	final Map<String, ManagedObjectReference> tgtMoref = new HashMap<>();
	while ((results != null) && !results.getObjects().isEmpty()) {
	    resultsToTgtMorefMap(results, tgtMoref);
	    final String token = results.getToken();

	    results = (token != null) ? this.vimPort.continueRetrievePropertiesEx(propertyCollector, token) : null;
	}

	return tgtMoref;
    }

    private void init() {
	try {
	    this.vimPort = this.connection.getVimPort();
	    this.serviceContent = this.connection.getServiceContent();
	} catch (final Throwable cause) {
	    throw new HelperException(cause);
	}
    }

    private PropertyFilterSpec[] propertyFilterSpecs(final ManagedObjectReference container, final String morefType,
	    final String... morefProperties) throws RuntimeFaultFaultMsg {
	init();

	final ManagedObjectReference viewManager = this.serviceContent.getViewManager();
	final ManagedObjectReference containerView = this.vimPort.createContainerView(viewManager, container,
		Arrays.asList(morefType), true);

	return new PropertyFilterSpec[] { new PropertyFilterSpecBuilder()
		.propSet(new PropertySpecBuilder().all(Boolean.FALSE).type(morefType).pathSet(morefProperties))
		.objectSet(new ObjectSpecBuilder().obj(containerView).skip(Boolean.TRUE).selectSet(
			new TraversalSpecBuilder().name("view").path("view").skip(false).type("ContainerView"))) };
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

    private Map<String, ManagedObjectReference> toMap(RetrieveResult rslts)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	final Map<String, ManagedObjectReference> tgtMoref = new HashMap<>();
	String token = null;
	token = populate(rslts, tgtMoref);

	while ((token != null) && !token.isEmpty()) {

	    rslts = this.vimPort.continueRetrievePropertiesEx(this.serviceContent.getPropertyCollector(), token);

	    token = populate(rslts, tgtMoref);
	}

	return tgtMoref;
    }

    public ManagedObjectReference vAppByName(final String vmName, final ManagedObjectReference propCollectorRef)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	return fcoByName(vmName, propCollectorRef, EntityType.VirtualApp);
    }

    public ManagedObjectReference vmByName(final String vmName, final ManagedObjectReference propCollectorRef)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	return fcoByName(vmName, propCollectorRef, EntityType.VirtualMachine);
    }
}
