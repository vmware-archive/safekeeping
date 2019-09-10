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
package com.vmware.vmbk.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.ID;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RetrieveVStorageObjSpec;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VStorageObjectAssociationsVmDiskAssociations;
import com.vmware.vim25.VStorageObjectConfigInfo;
import com.vmware.vim25.VStorageObjectSnapshotDetails;
import com.vmware.vim25.VStorageObjectSnapshotInfo;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VslmCloneSpec;
import com.vmware.vim25.VslmCreateSpec;
import com.vmware.vim25.VslmCreateSpecDiskFileBackingSpec;
import com.vmware.vim25.VslmInfrastructureObjectPolicy;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Jvddk;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.soap.sso.HeaderHandlerResolver;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.PrettyNumber;
import com.vmware.vmbk.util.Utility;
import com.vmware.vmbk.util.VersionManipulator;
import com.vmware.vslm.AlreadyExistsFaultMsg;
import com.vmware.vslm.FileFaultFaultMsg;
import com.vmware.vslm.InvalidArgumentFaultMsg;
import com.vmware.vslm.InvalidDatastoreFaultMsg;
import com.vmware.vslm.InvalidStateFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.RuntimeFaultFaultMsg;
import com.vmware.vslm.VslmAboutInfo;
import com.vmware.vslm.VslmFaultFaultMsg;
import com.vmware.vslm.VslmPortType;
import com.vmware.vslm.VslmService;
import com.vmware.vslm.VslmServiceInstanceContent;
import com.vmware.vslm.VslmSyncFaultFaultMsg;
import com.vmware.vslm.VslmTaskInfo;
import com.vmware.vslm.VslmVsoVStorageObjectAssociations;
import com.vmware.vslm.VslmVsoVStorageObjectAssociationsVmDiskAssociation;
import com.vmware.vslm.VslmVsoVStorageObjectQueryResult;
import com.vmware.vslm.VslmVsoVStorageObjectQuerySpec;
import com.vmware.vslm.VslmVsoVStorageObjectQuerySpecQueryFieldEnum;
import com.vmware.vslm.VslmVsoVStorageObjectQuerySpecQueryOperatorEnum;

public class VslmConnection extends HttpConf {
    private static final String VSLMSERVICEINSTANCETYPE = "VslmServiceInstance";
    private static final String VSLMSERVICEINSTANCEVALUE = "ServiceInstance";
    private static final String VSLM_PATH = "/vslm/sdk";

    static Logger logger = Logger.getLogger(VslmConnection.class.getName());

    private String instanceUuid;
    private final Jvddk jvddk;

    private final VimConnection vimConnection;
    private VslmAboutInfo aboutInfo;
    private VslmService vslmService;

    private VslmPortType vslmPort;

    private VslmServiceInstanceContent serviceContent;
    private ManagedObjectReference svcInstRef;
    private ManagedObjectReference vsoManager;
    private final URL url;

    VslmConnection(final VimConnection vimConnection) throws MalformedURLException {
	logger.entering(getClass().getName(), "Constructor");
	this.url = new URL("https", vimConnection.getHost(), VSLM_PATH);
	this.jvddk = vimConnection.getJvddk();
	this.vimConnection = vimConnection;

	this.serviceContent = null;
	logger.exiting(getClass().getName(), "Constructor");
    }

    public boolean clone(final ImprovedVirtuaDisk ivd, final String cloneName, final String datastoreName) {
	logger.entering(getClass().getName(), "cloneVM", new Object[] { ivd, cloneName, datastoreName });
	boolean result = false;
	ManagedObjectReference taskMor = null;
	try {
	    ManagedEntityInfo meiDatastore = null;
	    if (StringUtils.isNotEmpty(datastoreName)) {
		meiDatastore = this.vimConnection.getDatastoreByName(datastoreName);
	    } else {
		meiDatastore = ivd.getDatastoreInfo();
	    }

	    final VslmCloneSpec cloneSpec = new VslmCloneSpec();
	    final VslmCreateSpecDiskFileBackingSpec specDiskFileBackingSpec = new VslmCreateSpecDiskFileBackingSpec();
	    specDiskFileBackingSpec.setDatastore(meiDatastore.getMoref());
	    specDiskFileBackingSpec.setProvisioningType(ivd.getBackingProvisionType());
	    cloneSpec.setBackingSpec(specDiskFileBackingSpec);
	    cloneSpec.setName(cloneName);

	    if (isVslm()) {
		taskMor = getvslmPort().vslmCloneVStorageObjectTask(this.getVsoManager(), ivd.getId(), cloneSpec);
		result = waitForTask(taskMor);
	    } else {

		taskMor = getVimPort().cloneVStorageObjectTask(this.getVsoManager(), ivd.getId(),
			meiDatastore.getMoref(), cloneSpec);
		if (getVimConnection().waitForTask(taskMor)) {
		    result = true;
		}
	    }

	} catch (final FileFaultFaultMsg | RuntimeFaultFaultMsg | VslmFaultFaultMsg | InvalidDatastoreFaultMsg
		| com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg
		| com.vmware.vim25.RuntimeFaultFaultMsg | InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg
		| com.vmware.vim25.NotFoundFaultMsg | NotFoundFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	if (result) {
	    IoFunction.showInfo(logger, "Successfully cloned   to clone name [%s]", ivd.toString(), cloneName);
	    result = true;
	} else {
	    IoFunction.showWarning(logger, "Failure Cloning  %s  to clone name [%s] ", ivd.toString(), cloneName);
	}
	logger.exiting(getClass().getName(), "cloneVM", result);
	return result;
    }

    public VslmServiceInstanceContent connectVslm() throws MalformedURLException, ConnectException {
	logger.entering(getClass().getName(), "connectVslm");
	final VersionManipulator apiVersion = new VersionManipulator(this.vimConnection.getAboutInfo().getApiVersion());
	// api version greater than 6.7.1
	if ((apiVersion.toInteger() > ((60000 + 700 + 1))) && GlobalConfiguration.connectToVslm()) {
	    this.vslmService = new VslmService();
	    updateVslmHeaderHandlerResolver();
	    this.vslmPort = this.vslmService.getVslmPort();

	    final Map<String, Object> pbmCtxt = ((BindingProvider) this.vslmPort).getRequestContext();
	    pbmCtxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
	    pbmCtxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getURL().toString());

	    try {
		this.serviceContent = this.vslmPort.retrieveContent(getServiceInstanceReference());
	    } catch (final RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		throw new ConnectException(String.format("failed to connect to %s not valid", getURL().toString()), e);
	    }
	    this.aboutInfo = this.serviceContent.getAboutInfo();
	    this.instanceUuid = this.aboutInfo.getInstanceUuid();

	    this.vsoManager = this.serviceContent.getVStorageObjectManager();
	} else {
	    this.vsoManager = this.vimConnection.getServiceContent().getVStorageObjectManager();
	}
	logger.exiting(getClass().getName(), "connectVslm", this.serviceContent);
	return this.serviceContent;
    }

    private List<VStorageObjectAssociations> convertVslmVsoVStorageObjectAssociations2VStorageObjectAssociations(
	    final List<VslmVsoVStorageObjectAssociations> sourceList) {
	logger.entering(getClass().getName(), "convertVslmVsoVStorageObjectAssociations2VStorageObjectAssociations",
		sourceList);
	final LinkedList<VStorageObjectAssociations> result = new LinkedList<>();

	for (final VslmVsoVStorageObjectAssociations source : sourceList) {
	    final VStorageObjectAssociations a = new VStorageObjectAssociations();
	    a.setId(source.getId());

	    for (final VslmVsoVStorageObjectAssociationsVmDiskAssociation diskAssociation : source
		    .getVmDiskAssociation()) {
		final VStorageObjectAssociationsVmDiskAssociations convertedDiskAssosiation = new VStorageObjectAssociationsVmDiskAssociations();
		convertedDiskAssosiation.setDiskKey(diskAssociation.getDiskKey());
		convertedDiskAssosiation.setVmId(diskAssociation.getVmId());
		a.getVmDiskAssociations().add(convertedDiskAssosiation);
	    }
	    result.add(a);
	}
	logger.exiting(getClass().getName(), "convertVslmVsoVStorageObjectAssociations2VStorageObjectAssociations",
		result);
	return result;
    }

    private VStorageObject CreateDiskTask(final VslmCreateSpec spec)
	    throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg,
	    InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
	    com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
	logger.entering(getClass().getName(), "CreateDiskTask", spec);
	VStorageObject result = null;

	if (isVslm()) {
	    final ManagedObjectReference taskMor = getvslmPort().vslmCreateDiskTask(this.getVsoManager(), spec);
	    if (waitForTask(taskMor)) {
		final VslmTaskInfo taskResult = this.vslmPort.vslmQueryInfo(taskMor);
		result = (VStorageObject) taskResult.getResult();
	    }
	} else {
	    final ManagedObjectReference taskMor = getVimConnection().getVimPort().createDiskTask(this.getVsoManager(),
		    spec);
	    if (getVimConnection().waitForTask(taskMor)) {
		result = (VStorageObject) getVimConnection().morefHelper.entityProps(taskMor, "info.result");
	    }
	}

	logger.exiting(getClass().getName(), "CreateDiskTask", result);
	return result;

    }

    /**
     * @param snapName
     * @return
     * @throws VslmFaultFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws                                 com.vmware.vim25.FileFaultFaultMsg
     * @throws                                 com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws                                 com.vmware.vim25.InvalidStateFaultMsg
     * @throws                                 com.vmware.vim25.NotFoundFaultMsg
     * @throws                                 com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public boolean createSnapshot(final ImprovedVirtuaDisk ivd, final String snapName) throws FileFaultFaultMsg,
	    InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg,
	    com.vmware.vim25.FileFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg,
	    com.vmware.vim25.InvalidStateFaultMsg, com.vmware.vim25.NotFoundFaultMsg,
	    com.vmware.vim25.RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
	logger.entering(getClass().getName(), "createSnapshot", new Object[] { ivd, snapName });
	boolean result = false;
	if (isVslm()) {
	    final ManagedObjectReference taskMor = getvslmPort().vslmCreateSnapshotTask(this.vsoManager, ivd.getId(),
		    snapName);
	    result = waitForTask(taskMor);
	} else {
	    final ManagedObjectReference taskMor = getVimPort().vStorageObjectCreateSnapshotTask(this.vsoManager,
		    ivd.getId(), ivd.getDatastoreInfo().getMoref(), snapName);
	    result = this.vimConnection.waitForTask(taskMor);
	}
	logger.exiting(getClass().getName(), "createSnapshot", result);
	return result;
    }

    /**
     * @param improvedVirtuaDisk
     * @param snap
     * @return
     * @throws                                 com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws                                 com.vmware.vim25.NotFoundFaultMsg
     * @throws                                 com.vmware.vim25.InvalidStateFaultMsg
     * @throws                                 com.vmware.vim25.InvalidDatastoreFaultMsg
     * @throws                                 com.vmware.vim25.FileFaultFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public boolean deleteSnapshot(final ImprovedVirtuaDisk ivd,
	    final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap) throws com.vmware.vim25.FileFaultFaultMsg,
	    com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
	    com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, FileFaultFaultMsg,
	    InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg,
	    InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {

	logger.entering(getClass().getName(), "deleteSnapshot", new Object[] { ivd, snap });
	boolean result = false;
	if (isVslm()) {
	    final ManagedObjectReference taskMor = getvslmPort().vslmDeleteSnapshotTask(this.vsoManager, ivd.getId(),
		    snap.getId());
	    result = waitForTask(taskMor);
	} else {
	    final ManagedObjectReference taskMor = this.vimConnection.getVimPort().deleteSnapshotTask(this.vsoManager,
		    ivd.getId(), ivd.getDatastoreInfo().getMoref(), snap.getId());
	    result = this.vimConnection.waitForTask(taskMor);
	}

	logger.exiting(getClass().getName(), "deleteSnapshot", result);
	return result;

    }

    public void disconnect() {
	logger.entering(getClass().getName(), "disconnect");
	if (isConnected()) {
	    try {

		logger.fine("disconnecting VSLM...");
		this.vslmPort.vslmLogout(this.serviceContent.getSessionManager());

		logger.fine("disconnected.");
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));

	    } finally {

		this.serviceContent = null;
		this.vslmPort = null;
		this.vslmService = null;
	    }
	}
	logger.exiting(getClass().getName(), "disconnect");
    }

    public VslmAboutInfo getAboutInfo() {
	return this.aboutInfo;
    }

    public String getCookie() {
	return this.vimConnection.getCookie();
    }

    public String getCookieValue() {
	return this.vimConnection.getCookieValue();

    }

    public List<VslmInfrastructureObjectPolicy> getDiskAssociatedProfile(final ImprovedVirtuaDisk ivd)
	    throws InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg,
	    VslmFaultFaultMsg, com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
	    com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
	logger.entering(getClass().getName(), "getDiskAssociatedProfile", ivd);
	List<VslmInfrastructureObjectPolicy> result = null;
	if (isVslm()) {
	    result = this.vslmPort.vslmRetrieveVStorageInfrastructureObjectPolicy(this.vsoManager,
		    ivd.getDatastoreInfo().getMoref());
	} else {
	    result = this.getVimPort().retrieveVStorageInfrastructureObjectPolicy(this.vsoManager,
		    ivd.getDatastoreInfo().getMoref());
	}
	logger.exiting(getClass().getName(), "getDiskAssociatedProfile", result);
	return result;
    }

//    public LinkedList<ImprovedVirtuaDisk> getIvdList() throws NotFoundFaultMsg, RuntimeFaultFaultMsg {
//	logger.entering(getClass().getName(), "getIvdList");
//	final LinkedList<ImprovedVirtuaDisk> result = getIvdList(null);
//	logger.exiting(getClass().getName(), "getIvdList", result);
//	return result;
//    }

    public String getHost() {
	return this.url.getHost();
    }

    private LinkedList<ID> getIdList(final Map<String, ManagedObjectReference> datastoreMap)
	    throws NotFoundFaultMsg, RuntimeFaultFaultMsg {
	logger.entering(getClass().getName(), "getIdList", datastoreMap);
	final LinkedList<ID> result = new LinkedList<>();

	try {

		List<VslmVsoVStorageObjectQuerySpec> query = new LinkedList<>();
		final VslmVsoVStorageObjectQuerySpec q = new VslmVsoVStorageObjectQuerySpec();
		if ((datastoreMap != null) && (datastoreMap.size() > 0)) {
			q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.DATASTORE_MO_ID.value());
			q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.EQUALS.value());
			for (final String key : datastoreMap.keySet()) {
				final ManagedObjectReference dsMor = datastoreMap.get(key);
				q.getQueryValue().add(dsMor.getValue());
			}
	    }else {
			/* workaround for vSPhere 6.7U3 bug*/
		 	q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.CREATE_TIME.value());
			q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.GREATER_THAN.value());
			q.getQueryValue().add("0");
	    }
		query.add(q);
	    VslmVsoVStorageObjectQueryResult res = getvslmPort().vslmListVStorageObjectForSpec(this.getVsoManager(),
		    query, 100);

	    result.addAll(res.getId());
	    while (!res.isAllRecordsReturned()) {
		query = new LinkedList<>();
		final VslmVsoVStorageObjectQuerySpec retry = new VslmVsoVStorageObjectQuerySpec();
		retry.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.ID.value());
		retry.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.GREATER_THAN.value());
		retry.getQueryValue().add(res.getId().get(res.getId().size() - 1).getId());
		query.add(retry);
		if (q != null) {
		    query.add(q);
		}
		res = getvslmPort().vslmListVStorageObjectForSpec(this.getVsoManager(), query, 100);
		result.addAll(res.getId());
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "getIdList", result);
	return result;
    }

    public ImprovedVirtuaDisk getIvdById(final ID id) {
	logger.entering(getClass().getName(), "getIvdById", new Object[] { id });
	ImprovedVirtuaDisk result = null;

	try {
	    final VStorageObject vStorageObject = getvslmPort().vslmRetrieveVStorageObject(this.getVsoManager(), id);
	    final ManagedObjectReference dsMor = vStorageObject.getConfig().getBacking().getDatastore();
	    final String dsName = getVimConnection().morefHelper.entityName(dsMor);
	    final ManagedEntityInfo dsInfo = new ManagedEntityInfo(dsName, dsMor, getServerIntanceUuid());

	    try {
		result = new ImprovedVirtuaDisk(this, id, vStorageObject, dsInfo);

	    } catch (final Exception e) {
		LoggerUtils.logInfo(logger, "Improved Virtual Disk %s (%s) has some issue to be retrieved", id,
			dsMor.getValue());
		result = new ImprovedVirtuaDisk(this, id, null, dsInfo);

	    }

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "getIvdById", result);
	return result;
    }

    public ImprovedVirtuaDisk getIvdById(final ID id, final Map<String, ManagedObjectReference> datastoreMap) {
	logger.entering(getClass().getName(), "getIvdById", new Object[] { id, datastoreMap });
	ImprovedVirtuaDisk result = null;

	try {

	    for (final String key : datastoreMap.keySet()) {
		final ManagedObjectReference dsMor = datastoreMap.get(key);
		final ManagedEntityInfo dsInfo = new ManagedEntityInfo(key, dsMor, getServerIntanceUuid());
		try {
		    logger.fine(String.format("retrieving IVD on datastore %s (%s) ", key, dsMor.getValue()));
		    final VStorageObject vStorageObject = getVimPort().retrieveVStorageObject(this.getVsoManager(), id,
			    dsMor);
		    try {
			result = new ImprovedVirtuaDisk(this, id, vStorageObject, dsInfo);

		    } catch (final Exception e) {
			LoggerUtils.logInfo(logger, "Improved Virtual Disk %s (%s) has some issue to be retrieved", id,
				dsMor.getValue());
			result = new ImprovedVirtuaDisk(this, id, null, dsInfo);

		    }

		} catch (final Exception e) {
		    LoggerUtils.logInfo(logger, "Datastore %s (%s) doesn't support Improved Virtual Disk", key,
			    dsMor.getValue());
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "getIvdById", result);
	return result;
    }

    public ImprovedVirtuaDisk getIvdById(final String uuid) {
	ImprovedVirtuaDisk result = null;
	logger.entering(getClass().getName(), "getIvdById", uuid);
	if (isVslm()) {
	    final ID id = new ID();
	    id.setId(uuid);
	    result = getIvdById(id);
	} else {

	    final LinkedList<ImprovedVirtuaDisk> allIvd = getIvdList();
	    for (final ImprovedVirtuaDisk ivd : allIvd) {
		if (ivd.getUuid().equalsIgnoreCase(uuid)) {
		    result = ivd;
		    break;
		}
	    }

	}
	logger.exiting(getClass().getName(), "getIvdById", result);
	return result;
    }

    public ImprovedVirtuaDisk getIvdByName(final String name) {
	ImprovedVirtuaDisk result = null;
	logger.entering(getClass().getName(), "getIvdByName", name);
	if (isVslm()) {
	    try {

		final List<VslmVsoVStorageObjectQuerySpec> query = new LinkedList<>();
		final VslmVsoVStorageObjectQuerySpec q = new VslmVsoVStorageObjectQuerySpec();
		q.setQueryField(VslmVsoVStorageObjectQuerySpecQueryFieldEnum.NAME.value());
		q.setQueryOperator(VslmVsoVStorageObjectQuerySpecQueryOperatorEnum.EQUALS.value());
		q.getQueryValue().add(name);
		query.add(q);
		final VslmVsoVStorageObjectQueryResult res = getvslmPort()
			.vslmListVStorageObjectForSpec(this.getVsoManager(), query, 10);
		ID id = null;
		if (res.getId().size() > 0) {
		    id = res.getId().get(0);
		}
		result = getIvdById(id);

	    } catch (final RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
	    }
	} else {

	    final LinkedList<ImprovedVirtuaDisk> allIvd = getIvdList();
	    for (final ImprovedVirtuaDisk ivd : allIvd) {
		if (ivd.getName().equalsIgnoreCase(name)) {
		    result = ivd;
		    break;
		}
	    }

	}
	logger.exiting(getClass().getName(), "getIvdByName", result);
	return result;

    }

    public LinkedList<ImprovedVirtuaDisk> getIvdList() {
	logger.entering(getClass().getName(), "getIvdList");
	final Map<String, ManagedObjectReference> datastoreMap = (isVslm()) ? null
		: getVimConnection().getDatastoreList();
	final LinkedList<ImprovedVirtuaDisk> result = getIvdList(datastoreMap);
	logger.exiting(getClass().getName(), "getIvdList", result);
	return result;
    }

    private LinkedList<ImprovedVirtuaDisk> getIvdList(final Map<String, ManagedObjectReference> datastoreMap) {
	logger.entering(getClass().getName(), "getIvdList", datastoreMap);
	final LinkedList<ImprovedVirtuaDisk> result = new LinkedList<>();
	if (isVslm()) {
	    try {
		final LinkedList<ID> idList = getIdList(datastoreMap);
		for (final ID id : idList) {
		    result.add(getIvdById(id));
		}

	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	} else {
	    List<ID> ivdList = null;
	    for (final String key : datastoreMap.keySet()) {
		final ManagedObjectReference dsMor = datastoreMap.get(key);
		final ManagedEntityInfo dsInfo = new ManagedEntityInfo(key, dsMor, getServerIntanceUuid());
		try {
		    ivdList = null;
		    logger.fine(String.format("retrieving IVD on datastore %s (%s) ", key, dsMor.getValue()));
		    ivdList = getVimPort().listVStorageObject(this.getVsoManager(), dsMor);
		    for (final ID ivdID : ivdList) {
			try {
			    final VStorageObject vStorageObject = getVimPort()
				    .retrieveVStorageObject(this.getVsoManager(), ivdID, dsMor);
			    final ImprovedVirtuaDisk ivd = new ImprovedVirtuaDisk(this, ivdID, vStorageObject, dsInfo);
			    result.add(ivd);
			} catch (final Exception e) {
			    LoggerUtils.logInfo(logger, "Improved Virtual Disk %s (%s) has some issue to be retrieved",
				    ivdID, dsMor.getValue());
			    final ImprovedVirtuaDisk ivd = new ImprovedVirtuaDisk(this, ivdID, null, dsInfo);
			    result.add(ivd);
			}
		    }
		} catch (final Exception e) {
		    LoggerUtils.logInfo(logger, "Datastore %s (%s) doesn't support Improved Virtual Disk", key,
			    dsMor.getValue());
		}
	    }
	}
	logger.exiting(getClass().getName(), "getIvdList", result);
	return result;
    }

    public VStorageObjectSnapshotInfo getIvdSnapInfo(final ImprovedVirtuaDisk ivd)
	    throws FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg,
	    RuntimeFaultFaultMsg, VslmFaultFaultMsg, com.vmware.vim25.FileFaultFaultMsg,
	    com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
	    com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg {
	logger.entering(getClass().getName(), "getIvdSnapInfo", ivd);
	VStorageObjectSnapshotInfo result = null;
	if (isVslm()) {
	    result = getvslmPort().vslmRetrieveSnapshotInfo(this.getVsoManager(), ivd.getId());
	} else {
	    result = getVimPort().retrieveSnapshotInfo(this.getVsoManager(), ivd.getId(),
		    ivd.getDatastoreInfo().getMoref());
	}
	logger.exiting(getClass().getName(), "getIvdSnapInfo", result);
	return result;
    }

    public Jvddk getJvddk() {
	return this.jvddk;
    }

    public Integer getPort() {
	final int port = this.url.getPort();
	return port;
    }

    public String getServerIntanceUuid() {
	return this.instanceUuid;
    }

    public ManagedObjectReference getServiceInstanceReference() {
	if (this.svcInstRef == null) {
	    final ManagedObjectReference ref = new ManagedObjectReference();
	    ref.setType(VSLMSERVICEINSTANCETYPE);
	    ref.setValue(VSLMSERVICEINSTANCEVALUE);
	    this.svcInstRef = ref;
	}
	return this.svcInstRef;
    }

    public String getUrl() {
	return this.url.toString();
    }

    public URL getURL() {
	return this.url;
    }

    public VimConnection getVimConnection() {
	return this.vimConnection;
    }

    /**
     * @return
     */
    private VimPortType getVimPort() {
	return this.vimConnection.getVimPort();
    }

    /**
     * @return
     */
    private VslmPortType getvslmPort() {

	return this.vslmPort;
    }

    /**
     * @return
     */
    public ManagedObjectReference getVsoManager() {
	return this.vsoManager;
    }

    public boolean isConnected() {
	// TODO Auto-generated method stub
	return this.serviceContent != null;
    }

    public boolean isIvdByNameExist(final String name) {
	logger.entering(getClass().getName(), "isIvdByNameExist", name);
	final ImprovedVirtuaDisk ivd = getIvdByName(name);
	final boolean result = (ivd == null) ? false : true;
	logger.exiting(getClass().getName(), "isIvdByNameExist", result);
	return result;
    }

    private boolean isVslm() {
	return this.vslmPort != null;
    }

    public VStorageObject ivd_create(final ManagedEntityInfo datastore, final String name,
	    final BaseConfigInfoDiskFileBackingInfoProvisioningType backingType, final long size,
	    final List<VirtualMachineDefinedProfileSpec> list) {
	logger.entering(getClass().getName(), "ivd_create", new Object[] { datastore, name, backingType, size, list });

	final VStorageObject result = ivd_create(datastore, name, null, backingType, size, list);
	logger.exiting(getClass().getName(), "ivd_create", result);
	return result;
    }

    private VStorageObject ivd_create(final ManagedEntityInfo datastore, final String name, final String folder,
	    final BaseConfigInfoDiskFileBackingInfoProvisioningType backingType, final long size,
	    final List<VirtualMachineDefinedProfileSpec> spbmProfile) {
	logger.entering(getClass().getName(), "ivd_create",
		new Object[] { datastore, name, folder, backingType, size, spbmProfile });
	VStorageObject result = null;
	try {

	    if (datastore != null) {
		final Long sizeInMB = PrettyNumber.toMegaByte(size);
		final VslmCreateSpec spec = new VslmCreateSpec();
		final VslmCreateSpecDiskFileBackingSpec backingSpec = new VslmCreateSpecDiskFileBackingSpec();
		backingSpec.setDatastore(datastore.getMoref());
		backingSpec.setPath(folder);
		backingSpec.setProvisioningType(backingType.value());
		spec.setCapacityInMB(sizeInMB);
		spec.setName(name);
		spec.setKeepAfterDeleteVm(true);
		spec.setBackingSpec(backingSpec);
		spec.getProfile().addAll(spbmProfile);
		result = CreateDiskTask(spec);
		if (result != null) {
		    IoFunction.showInfo(logger,
			    "New Improved Virtual Disk Name:%s ID:%s Datastore:%s size:%dM Type:%s created", name,
			    result.getConfig().getId().getId(), datastore.getName(), sizeInMB, backingType.toString());
		} else {
		    IoFunction.showWarning(logger,
			    "Failed to create new Improved Virtual Disk Name:%s Datastore:%s size:%d Type:%s", name,
			    datastore.getName(), size, backingType.toString());
		}
	    }
	} catch (InvalidPropertyFaultMsg | InvalidCollectorVersionFaultMsg | FileFaultFaultMsg
		| InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg | VslmFaultFaultMsg
		| com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg
		| com.vmware.vim25.RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "ivd_create", result);
	return result;
    }

    VStorageObject ivd_create(final String fileNameOfDisk, final ManagedEntityInfo datastoreEntityInfo,
	    final BaseConfigInfoDiskFileBackingInfoProvisioningType backingType, final long size,
	    final List<VirtualMachineDefinedProfileSpec> storagePolicy) {
	logger.entering(getClass().getName(), "ivd_create",
		new Object[] { fileNameOfDisk, datastoreEntityInfo, backingType, size, storagePolicy });

	final String diskFolder = null;
	final String diskName = fileNameOfDisk;

	final VStorageObject result = ivd_create(datastoreEntityInfo, diskName, diskFolder, backingType, size,
		storagePolicy);

	logger.exiting(getClass().getName(), "ivd_create", result);
	return result;
    }

    public VStorageObject ivd_promote(final ManagedEntityInfo dcInfo, final String path, final String name) {
	logger.entering(getClass().getName(), "ivd_promote", new Object[] { dcInfo, path, name });
	VStorageObject result = null;
	try {
	    final String urlDisk = getVimConnection().getDiskPathForVc(dcInfo, path);
	    if (isVslm()) {
		result = getvslmPort().vslmRegisterDisk(this.getVsoManager(), urlDisk, name);
	    } else {
		result = getVimPort().registerDisk(this.getVsoManager(), urlDisk, name);
	    }
	} catch (final AlreadyExistsFaultMsg | FileFaultFaultMsg | RuntimeFaultFaultMsg | VslmFaultFaultMsg
		| VslmSyncFaultFaultMsg | InvalidDatastoreFaultMsg | com.vmware.vim25.AlreadyExistsFaultMsg
		| com.vmware.vim25.FileFaultFaultMsg | com.vmware.vim25.InvalidDatastoreFaultMsg
		| com.vmware.vim25.RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	if (result != null) {
	    IoFunction.showInfo(logger, "Promote disk:%s name:%s succeed", path, name);
	} else {
	    IoFunction.showWarning(logger, "Promote disk:%s name:%s Failed", path, name);
	}
	logger.exiting(getClass().getName(), "ivd_promote", result);
	return result;
    }

    boolean ivd_ReconcileDatastore(final String datastoreName) {
	logger.entering(getClass().getName(), "ivd_ReconcileDatastore", new Object[] { datastoreName });
	boolean result = false;
	try {
	    final ManagedObjectReference morDatastore = getVimConnection().morefHelper
		    .inContainerByType(this.vimConnection.getRootFolder(), "Datastore").get(datastoreName);
	    if (morDatastore != null) {
		IoFunction.showInfo(logger, "Starting reconciliation for datastore:%s", datastoreName);
		if (isVslm()) {
		    final ManagedObjectReference taskMor = getvslmPort()
			    .vslmReconcileDatastoreInventoryTask(this.getVsoManager(), morDatastore);
		    result = waitForTask(taskMor);

		} else {
		    final ManagedObjectReference taskMor = getVimPort()
			    .reconcileDatastoreInventoryTask(this.getVsoManager(), morDatastore);
		    result = getVimConnection().waitForTask(taskMor);

		}

		if (result) {
		    IoFunction.showInfo(logger, "Done  ");
		    result = true;
		} else {
		    IoFunction.showWarning(logger, "Failed");
		}
	    } else {
		IoFunction.showWarning(logger, "Datastore:%s doesn't exist", datastoreName);
	    }
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidDatastoreFaultMsg | NotFoundFaultMsg
		| InvalidCollectorVersionFaultMsg | com.vmware.vim25.RuntimeFaultFaultMsg | VslmFaultFaultMsg
		| com.vmware.vim25.InvalidDatastoreFaultMsg | com.vmware.vim25.NotFoundFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "ivd_ReconcileDatastore", result);
	return result;
    }

    void keepAlive() throws RuntimeFaultFaultMsg {
	logger.info(String.format("keep alive %s : Current Time %s", getServerIntanceUuid(), ""));
	// getvslmPort().currentTime(getServiceInstanceReference())));
    }

    public DiskChangeInfo queryChangedDiskAreas(final ImprovedVirtuaDisk ivd, final long startOffset,
	    final String changeId) throws FileFaultFaultMsg, InvalidArgumentFaultMsg, InvalidDatastoreFaultMsg,
	    InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg {
	logger.entering(getClass().getName(), "queryChangedDiskAreas", new Object[] { ivd, startOffset, changeId });
	final DiskChangeInfo result = this.vslmPort.vslmQueryChangedDiskAreas(this.getVsoManager(), ivd.getId(),
		ivd.getCurrentSnapshot().getId(), startOffset, changeId);
	logger.exiting(getClass().getName(), "queryChangedDiskAreas", result);
	return result;
    }

    public VStorageObjectSnapshotDetails retrieveSnapshotDetails(final ImprovedVirtuaDisk ivd) throws FileFaultFaultMsg,
	    InvalidDatastoreFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg {
	logger.entering(getClass().getName(), "retrieveSnapshotDetails", ivd);
	final VStorageObjectSnapshotInfoVStorageObjectSnapshot snapshot = ivd.getCurrentSnapshot();
	VStorageObjectSnapshotDetails result = null;
	if (snapshot != null) {
	    result = this.vslmPort.vslmRetrieveSnapshotDetails(this.getVsoManager(), ivd.getId(), snapshot.getId());
	}
	logger.exiting(getClass().getName(), "retrieveSnapshotDetails", result);
	return result;
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociations() {
	logger.entering(getClass().getName(), "retrieveVStorageObjectAssociations");
	Map<String, ManagedObjectReference> datastoreMap = null;
	if (isVslm()) {
	} else {
	    datastoreMap = getVimConnection().getDatastoreList();
	}
	final List<VStorageObjectAssociations> result = retrieveVStorageObjectAssociations(datastoreMap);
	logger.exiting(getClass().getName(), "retrieveVStorageObjectAssociations", result);
	return result;
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociations(final ImprovedVirtuaDisk ivd) {
	logger.entering(getClass().getName(), "retrieveVStorageObjectAssociations", ivd);
	List<VStorageObjectAssociations> result = new LinkedList<>();

	if (isVslm()) {
	    final LinkedList<ID> ivdList = new LinkedList<>();
	    ivdList.add(ivd.getId());
	    result = retrieveVStorageObjectAssociationsById(ivdList);
	} else {
	    final RetrieveVStorageObjSpec retrieveVStorageObjSpec = new RetrieveVStorageObjSpec();
	    retrieveVStorageObjSpec.setDatastore(ivd.getDatastoreInfo().getMoref());
	    retrieveVStorageObjSpec.setId(ivd.getId());

	    final VStorageObjectConfigInfo config = ivd.getConfigInfo();
	    if (config == null) {
		IoFunction.showInfo(logger, "%s ", ivd.toString());
	    } else {
		final ArrayList<RetrieveVStorageObjSpec> listRetrieve = new ArrayList<>();
		listRetrieve.add(retrieveVStorageObjSpec);
		result.addAll(retrieveVStorageObjectAssociationsByVStorageObjSpec(listRetrieve));
	    }
	}
	logger.exiting(getClass().getName(), "retrieveVStorageObjectAssociations", result);
	return result;
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociations(
	    final Map<String, ManagedObjectReference> datastoreMap) {
	logger.entering(getClass().getName(), "retrieveVStorageObjectAssociations", new Object[] { datastoreMap });
	List<VStorageObjectAssociations> result = null;
	List<ID> idList = null;
	if (isVslm()) {
	    try {
		idList = getIdList(datastoreMap);
		result = retrieveVStorageObjectAssociationsById(idList);
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	} else {

	    try {

		final LinkedList<RetrieveVStorageObjSpec> list = new LinkedList<>();
		for (final String key : datastoreMap.keySet()) {
		    final ManagedObjectReference dsMor = datastoreMap.get(key);
		    final int i = 0;
		    try {
			idList = null;
			logger.fine(String.format("retrieving IVD on datastore %s (%s) ", key, dsMor.getValue()));
			idList = getVimPort().listVStorageObject(this.getVsoManager(), dsMor);
			for (final ID ivdID : idList) {
			    final RetrieveVStorageObjSpec objSpec = new RetrieveVStorageObjSpec();
			    objSpec.setDatastore(dsMor);
			    objSpec.setId(ivdID);
			    list.add(objSpec);
			}
		    } catch (final Exception e) {
			LoggerUtils.logInfo(logger, "Datastore %s (%s) doesn't support Improved Virtual Disk", key,
				dsMor.getValue());
		    }
		}
		result = retrieveVStorageObjectAssociationsByVStorageObjSpec(list);

	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	}
	logger.exiting(getClass().getName(), "retrieveVStorageObjectAssociations", result);
	return result;
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociationsById(final List<ID> idList) {
	logger.entering(getClass().getName(), "retrieveVStorageObjectAssociationsById", new Object[] { idList });
	List<VStorageObjectAssociations> result = null;
	try {
	    result = convertVslmVsoVStorageObjectAssociations2VStorageObjectAssociations(
		    getvslmPort().vslmRetrieveVStorageObjectAssociations(this.getVsoManager(), idList));
	    // result =
	    // getvslmPort().vslmRetrieveVStorageObjectAssociations(this.getVsoManager(),
	    // idList);
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}

	logger.exiting(getClass().getName(), "retrieveVStorageObjectAssociationsById", result);
	return result;
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociationsByVStorageObjSpec(
	    final List<RetrieveVStorageObjSpec> listObjSpec) {
	logger.entering(getClass().getName(), "retrieveVStorageObjectAssociationsByVStorageObjSpec",
		new Object[] { listObjSpec });
	List<VStorageObjectAssociations> result = null;
	try {
	    result = getVimPort().retrieveVStorageObjectAssociations(this.getVsoManager(), listObjSpec);
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}

	logger.exiting(getClass().getName(), "retrieveVStorageObjectAssociationsByVStorageObjSpec", result);
	return result;
    }

    public void setVStorageObjectControlFlags(final ImprovedVirtuaDisk ivd, final boolean on,
	    final List<String> controlFlagList)
	    throws com.vmware.vim25.InvalidDatastoreFaultMsg, com.vmware.vim25.InvalidStateFaultMsg,
	    com.vmware.vim25.NotFoundFaultMsg, com.vmware.vim25.RuntimeFaultFaultMsg, InvalidDatastoreFaultMsg,
	    InvalidStateFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VslmFaultFaultMsg {
	logger.entering(getClass().getName(), "setVStorageObjectControlFlags");
	if (isVslm()) {
	    if (on) {
		getvslmPort().vslmSetVStorageObjectControlFlags(this.vsoManager, ivd.getId(), controlFlagList);
	    } else {
		getvslmPort().vslmClearVStorageObjectControlFlags(this.vsoManager, ivd.getId(), controlFlagList);
	    }
	} else {
	    if (on) {
		getVimPort().setVStorageObjectControlFlags(this.vsoManager, ivd.getId(),
			ivd.getDatastoreInfo().getMoref(), controlFlagList);
	    } else {
		getVimPort().clearVStorageObjectControlFlags(this.vsoManager, ivd.getId(),
			ivd.getDatastoreInfo().getMoref(), controlFlagList);
	    }
	}
	logger.exiting(getClass().getName(), "setVStorageObjectControlFlags");
    }

    @Override
    public String toString() {
	return String.format("%s url: %s", getAboutInfo().getFullName(), getUrl());
    }

    public HeaderHandlerResolver updateVslmHeaderHandlerResolver() {
	logger.entering(getClass().getName(), "updateVslmHeaderHandlerResolver");
	HeaderHandlerResolver result = null;
	if (this.vslmService != null) {
	    result = new HeaderHandlerResolver();
	    result.addHandler(new VcSessionHandler(getCookieValue()));
	    this.vslmService.setHandlerResolver(result);
	}
	logger.exiting(getClass().getName(), "updateVslmHeaderHandlerResolver", result);
	return result;
    }

    /**
     * 
     * @param task
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     */
    public boolean waitForTask(final ManagedObjectReference task)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
	logger.entering(getClass().getName(), "waitForTask", task);
	boolean result = false;
	boolean done = false;
	while (!done) {
	    final VslmTaskInfo taskResult = this.vslmPort.vslmQueryInfo(task);
	    switch (taskResult.getState()) {
	    case ERROR:
		done = true;
		result = false;
		break;
	    case QUEUED:
		break;
	    case RUNNING:
		break;
	    case SUCCESS:
		done = true;
		result = true;
		break;
	    }
	}
	logger.exiting(getClass().getName(), "waitForTask", result);
	return result;
    }

}
