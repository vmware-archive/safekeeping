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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import com.vmware.jvix.jDiskLib;
import com.vmware.jvix.jDiskLib.ConnectParams;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vim25.AboutInfo;
import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.ArrayOfDatastoreHostMount;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.FileNotFoundFaultMsg;
import com.vmware.vim25.ID;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidDatastorePathFaultMsg;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.ResourceConfigSpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VimService;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Jvddk;
import com.vmware.vmbk.control.Jvddk.CleanUp;
import com.vmware.vmbk.control.info.RestoreManagedInfo;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.soap.helpers.VapiAuthenticationHelper;
import com.vmware.vmbk.soap.helpers.WaitForValues;
import com.vmware.vmbk.soap.helpers.builders.ObjectSpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.PropertySpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.SelectionSpecBuilder;
import com.vmware.vmbk.soap.helpers.builders.TraversalSpecBuilder;
import com.vmware.vmbk.soap.sso.HeaderCookieExtractionHandler;
import com.vmware.vmbk.soap.sso.HeaderHandlerResolver;
import com.vmware.vmbk.soap.sso.SamlTokenHandler;
import com.vmware.vmbk.soap.sso.SsoUtils;
import com.vmware.vmbk.soap.sso.TimeStampHandler;
import com.vmware.vmbk.soap.sso.WsSecuritySignatureAssertionHandler;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;
import com.vmware.vslm.VslmServiceInstanceContent;

public class VimConnection extends AConnection implements IConnection {

    static {
	logger = Logger.getLogger(VimConnection.class.getName());
    }

    private HeaderCookieExtractionHandler cookieExtractor;

    private String cookieVal;
    private HandlerResolver defaultHandler;
    private String instanceUuid;
    private final Jvddk jvddk;
    private Thread keepAlive;

    private final String lookupServiceUuidReference;
    public com.vmware.vmbk.soap.helpers.MorefUtil morefHelper;
    private String privateKeyFileName;
    private final Sso pscConnection;
    private final VapiAuthenticationHelper vapiService;
    private AboutInfo aboutInfo;

    final com.vmware.vmbk.soap.helpers.WaitForValues waitForValues;

    private String x509CertFileName;
    private final VslmConnection vslmConnection;

    private final PbmConnection pbmConnection;

    VimConnection(final Sso pscConnection, final String lookupServiceUuidReference, final String url)
	    throws MalformedURLException {
	this.waitForValues = new WaitForValues(this);
	this.morefHelper = new MorefUtil(this);

	setUrl(url);
	this.lookupServiceUuidReference = lookupServiceUuidReference;
	this.pscConnection = pscConnection;
	final HttpConfiguration httpConfig = buildHttpConfiguration();
	this.vapiService = new VapiAuthenticationHelper(getHost(), httpConfig, this.pscConnection.getUserCert());

	this.jvddk = new Jvddk(this);
	this.vslmConnection = new VslmConnection(this);
	this.pbmConnection = new PbmConnection(this);
    }

    @SuppressWarnings("rawtypes")
    private void _connect(final Element token) throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg,
	    com.vmware.pbm.RuntimeFaultFaultMsg, MalformedURLException, InvalidPropertyFaultMsg,
	    com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg {

	this.cookieVal = loginUsingSAMLToken(token, getURL().toString());

	this.vimService = new VimService();

	this.vimPort = this.vimService.getVimPort();
	this.defaultHandler = this.vimService.getHandlerResolver();
	final Map<String, Object> ctxt = ((BindingProvider) this.vimPort).getRequestContext();
	ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getUrl().toString());
	ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
	@SuppressWarnings("unchecked")
	Map<String, List<String>> headers = (Map<String, List<String>>) ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
	if (headers == null) {
	    headers = new HashMap<>();
	}
	headers.put("Cookie", Arrays.asList(this.cookieVal));
	ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
	this.serviceContent = this.vimPort.retrieveServiceContent(getServiceInstanceReference());
	this.aboutInfo = this.serviceContent.getAbout();
	this.instanceUuid = this.aboutInfo.getInstanceUuid();
	this.userSession = getCurrentSession();

	this.headers = (Map) ((BindingProvider) this.vimPort).getResponseContext()
		.get(MessageContext.HTTP_RESPONSE_HEADERS);
	// this.vsoManager = this.serviceContent.getVStorageObjectManager();

    }

    private List<SelectionSpec> buildTraversalSpecForDatastoreToDatacenter() {

	final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name("VisitFolders");
	final TraversalSpec visitFolders = new TraversalSpecBuilder().name("VisitFolders").path("parent").type("Folder")
		.skip(false).selectSet(sspecvfolders);
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
	final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name("VisitFolders");
	final TraversalSpec visitFolders = new TraversalSpecBuilder().name("VisitFolders").path("parent")
		.type("hostFolder").skip(false).selectSet(sspecvfolders);
	final TraversalSpec HostSystemToFolder = new TraversalSpecBuilder().name("HostSystemToFolder").path("parent")
		.type(EntityType.HostSystem.toString()).skip(true).selectSet(sspecvfolders);
	final TraversalSpec FolderTraversalSpec = new TraversalSpecBuilder().name("FolderTraversalSpec")
		.path("childEntity").type(EntityType.Folder.toString()).skip(true).selectSet(sspecvfolders);

	final List<SelectionSpec> speclist = new ArrayList<>();
	speclist.add(HostSystemToFolder);
	speclist.add(visitFolders);
	speclist.add(FolderTraversalSpec);
	return speclist;
    }

    private List<SelectionSpec> buildTraversalSpecForVappToDatacenter() {

	final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name("VisitFolders");
	final TraversalSpec visitFolders = new TraversalSpecBuilder().name("VisitFolders").path("parent").type("Folder")
		.skip(false).selectSet(sspecvfolders);
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

	final SelectionSpec sspecvfolders = new SelectionSpecBuilder().name("VisitFolders");
	final TraversalSpec visitFolders = new TraversalSpecBuilder().name("VisitFolders").path("parent").type("Folder")
		.skip(false).selectSet(sspecvfolders);

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

    private boolean Cleanup() {
	boolean success = false;
	for (int i = 0; i < 3; i++) {
	    final CleanUp cleanupResult = this.jvddk.Cleanup();
	    if (cleanupResult.result == com.vmware.jvix.jDiskLib.VIX_OK) {
		if (cleanupResult.numRemaining == 0) {
		    success = true;
		    break;
		}
	    } else {
		break;
	    }

	}
	return success;
    }

    private void clearHandlerResolver(final String vcServerUrl, final String cookie) {

	this.vimService.setHandlerResolver(this.defaultHandler);
	this.vimPort = this.vimService.getVimPort();

	{
	    final Map<String, Object> ctxt = ((BindingProvider) this.vimPort).getRequestContext();
	    ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vcServerUrl);
	    ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

	    @SuppressWarnings("unchecked")
	    Map<String, List<String>> headers = (Map<String, List<String>>) ctxt
		    .get(MessageContext.HTTP_REQUEST_HEADERS);
	    if (headers == null) {
		headers = new HashMap<>();
	    }
	    headers.put("Cookie", Arrays.asList(cookie));
	    ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
	}
    }

    public Jvddk configureVddkAccess(final ImprovedVirtuaDisk ivd) {
	logger.entering(getClass().getName(), "configureVddkAccess", new Object[] { ivd });
	configureVddkAccess(ivd, null);
	logger.exiting(getClass().getName(), "configureVddkAccess", this.jvddk);
	return this.jvddk;
    }

    public Jvddk configureVddkAccess(final ImprovedVirtuaDisk ivd, final ID id) {
	logger.entering(getClass().getName(), "configureVddkAccess", new Object[] { ivd, id });
	final ConnectParams connectParams = new jDiskLib.ConnectParams();

	connectParams.specType = jDiskLib.ConnectParams.VIXDISKLIB_SPEC_VSTORAGE_OBJECT;
	connectParams.id = ivd.getUuid();
	connectParams.datastoreMoRef = ivd.getDatastoreInfo().getMorefValue();
	if (id != null) {
	    connectParams.ssId = id.getId();
	}
	connectParams.credType = jDiskLib.ConnectParams.VIXDISKLIB_CRED_SESSIONID;
	connectParams.serverName = getHost();
	connectParams.port = getPort();
	connectParams.nfcHostPort = GlobalConfiguration.getNfcHostPort();
	connectParams.username = GlobalConfiguration.getUsername();

	connectParams.password = "";
	connectParams.key = GlobalConfiguration.getPassword();
	connectParams.cookie = getCookieValue();

	connectParams.thumbPrint = getThumbPrint();

	this.jvddk.setConnectParams(connectParams);
	logger.exiting(getClass().getName(), "configureVddkAccess", this.jvddk);
	return this.jvddk;

    }

    public Jvddk configureVddkAccess(final ManagedObjectReference vmMor) {
	logger.entering(getClass().getName(), "configureVddkAccess", new Object[] { vmMor });

	final ConnectParams connectParams = new jDiskLib.ConnectParams();

	connectParams.vmxSpec = String.format("moref=%s", vmMor.getValue());
	connectParams.specType = jDiskLib.ConnectParams.VIXDISKLIB_SPEC_VMX;
	connectParams.credType = jDiskLib.ConnectParams.VIXDISKLIB_CRED_SESSIONID;
	connectParams.serverName = getHost();
	connectParams.port = getPort();
	connectParams.nfcHostPort = GlobalConfiguration.getNfcHostPort();
	connectParams.username = GlobalConfiguration.getUsername();

	connectParams.password = "";
	connectParams.key = GlobalConfiguration.getPassword();
	connectParams.cookie = getCookieValue();

	connectParams.thumbPrint = getThumbPrint();

	this.jvddk.setConnectParams(connectParams);
	logger.exiting(getClass().getName(), "configureVddkAccess", this.jvddk);
	return this.jvddk;
    }

    @Override
    public IConnection connect(final Element token) throws ConnectException {
	if (!isConnected()) {
	    try {
		socket();
		_connect(token);

	    } catch (final Exception e) {

		throw new ConnectException(String.format("failed to connect to %s not valid", getURL().toString()), e);
	    }
	}
	return this;
    }

    /**
     * @return
     * @throws MalformedURLException
     * @throws ConnectException
     *
     */
    public PbmServiceInstanceContent connectPbm() throws ConnectException, MalformedURLException {
	return getPbmConnection().connectPbm();

    }

    StubConfiguration connectVapi(final SamlToken samlToken) throws MalformedURLException {
	logger.entering(getClass().getName(), "connectVapi", samlToken);
	final StubConfiguration result = this.vapiService.loginBySamlBearerToken(samlToken);

	logger.exiting(getClass().getName(), "connectVapi", result);
	return result;
    }

    /**
     * @return
     * @throws MalformedURLException
     * @throws ConnectException
     *
     */
    public VslmServiceInstanceContent connectVslm() throws ConnectException, MalformedURLException {
	return getVslmConnection().connectVslm();

    }

    /**
     * @param vim
     * @param name
     * @param resSpec
     * @param configSpec
     * @return
     */
    public ManagedObjectReference createVApp(final String name, final ManagedEntityInfo rpInfo,
	    final ManagedEntityInfo folderInfo, final ResourceConfigSpec resSpec, final VAppConfigSpec configSpec) {
	ManagedObjectReference result = null;
	try {
	    result = getVimPort().createVApp(rpInfo.getMoref(), name, resSpec, configSpec, folderInfo.getMoref());
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	return result;
    }

    public boolean deleteDirectory(final ManagedEntityInfo dcInfo, final ManagedEntityInfo dsInfo,
	    final String directory) {

	boolean result = true;
	try {
	    final DatastoreSummary dsSummary = (DatastoreSummary) this.morefHelper.entityProps(dsInfo.getMoref(),
		    "summary");
	    final String namespaceUrl = dsSummary.getUrl() + directory;

	    String datastorePath = getVimPort().convertNamespacePathToUuidPath(
		    getServiceContent().getDatastoreNamespaceManager(), dcInfo.getMoref(), namespaceUrl);
	    System.out.println("datastorePath=" + datastorePath);
	    datastorePath = datastorePath.substring(5);
	    getVimPort().deleteDirectory(getServiceContent().getDatastoreNamespaceManager(), dcInfo.getMoref(),
		    datastorePath);
	} catch (InvalidDatastoreFaultMsg | RuntimeFaultFaultMsg | FileFaultFaultMsg | FileNotFoundFaultMsg
		| InvalidDatastorePathFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	return result;
    }

    @Override
    public IConnection disconnect() {
	if (isConnected()) {
	    try {
		if (this.keepAlive != null) {
		    this.keepAlive.interrupt();
		}

		Cleanup();
		logger.info("disconnecting...");
		this.vimPort.logout(this.serviceContent.getSessionManager());
		this.getVslmConnection().disconnect();
		this.vapiService.logout();
		logger.info("disconnected.");
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));

	    } finally {

		this.userSession = null;
		this.serviceContent = null;
		this.vimPort = null;
		this.vimService = null;
	    }
	}

	return this;
    }

    /**
     * @param vmFolder
     * @return
     */
    public ManagedObjectReference findByInventoryPath(final String path) {
	logger.entering(getClass().getName(), "findByInventoryPath", path);
	ManagedObjectReference result = null;
	try {
	    result = getVimPort().findByInventoryPath(getSearchIndex(), path);
	} catch (final RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	logger.exiting(getClass().getName(), "findByInventoryPath", result);
	return result;
    }

    /**
     * @param moref
     * @return
     */
    public VirtualAppManager findVAppByMoref(final ManagedObjectReference moref) {
	logger.entering(getClass().getName(), "findVAppByMoref", moref);
	VirtualAppManager result = null;
	try {
	    result = new VirtualAppManager(this, moref);
	} catch (final RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	logger.exiting(getClass().getName(), "findVAppByMoref", result);
	return result;
    }

    public VirtualAppManager findVAppByName(final String name) throws Exception {
	logger.entering(getClass().getName(), "findVmByName", name);
	VirtualAppManager result = null;
	final ManagedObjectReference mor = getVAppByName(name);
	if (mor != null) {
	    result = new VirtualAppManager(this, mor);
	}
	logger.exiting(getClass().getName(), "findVAppByName", result);
	return result;
    }

    /**
     * @param uuid
     * @return
     */
    public VirtualAppManager findVAppByUuid(final String uuid) {
	// TODO Auto-generated method stub
	return null;
    }

// TODO Remove unused code found by UCDetector
//     public ManagedObjectReference getFolder(final ManagedObjectReference dcMor, final String folder) throws Exception {
//
// 	final ManagedObjectReference folderMor = getvmFolderByDatacenter(dcMor, folder);
// 	if (folderMor == null) {
// 	    throw new Exception("folder is not found.");
// 	}
// 	return folderMor;
//     }

    /**
     * Finds a virtual machine or host by IP address, where the IP address is in
     * dot-decimal notation. For example, 10.17.12.12. The IP address for a virtual
     * machine is the one returned from VMware tools, ipAddress.
     *
     * @param ip The IP to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByIp(final String ip) {
	logger.entering(getClass().getName(), "findVmByIp", ip);
	VirtualMachineManager result = null;
	try {
	    final ManagedObjectReference mor = getVimPort().findByIp(getSearchIndex(), null, ip, true);
	    result = new VirtualMachineManager(this, mor);
	} catch (final RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	logger.exiting(getClass().getName(), "findVmByIp", result);
	return result;
    }

    /**
     * Finds a virtual machine by the moref.
     *
     * @param moref The moref to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByMoref(final ManagedObjectReference moref) {
	logger.entering(getClass().getName(), "findVmByMoref", moref);
	final VirtualMachineManager result = new VirtualMachineManager(this, moref);

	logger.exiting(getClass().getName(), "findVmByMoref", result);
	return result;
    }

    public VirtualMachineManager findVmByName(final String vmName) throws Exception {
	logger.entering(getClass().getName(), "findVmByName", vmName);
	VirtualMachineManager result = null;
	final ManagedObjectReference mor = getVmByName(vmName);
	if (mor != null) {
	    result = new VirtualMachineManager(this, vmName, mor);
	}
	logger.exiting(getClass().getName(), "findVmByName", result);
	return result;
    }

    /**
     * Finds a virtual machine by BIOS or instance UUID.
     *
     * @param uuid         The UUID to find.
     * @param instanceUuid If true, search for virtual machines whose instance UUID
     *                     matches the given uuid. Otherwise, search for virtual
     *                     machines whose BIOS UUID matches the given uuid.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByUuid(final String uuid, final boolean instanceUuid) {
	logger.entering(getClass().getName(), "findVmByUuid", uuid);
	VirtualMachineManager result = null;
	try {
	    final ManagedObjectReference mor = getVimPort().findByUuid(getSearchIndex(), null, uuid, true,
		    instanceUuid);
	    result = new VirtualMachineManager(this, mor);
	} catch (final RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	logger.exiting(getClass().getName(), "findVmByUuid", result);
	return result;
    }

    public AboutInfo getAboutInfo() {
	return this.aboutInfo;
    }

    LinkedList<VirtualAppManager> getAllVAppList(final String filter)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

	ManagedObjectReference folder = null;
	if ((filter != null) & !filter.isEmpty()) {
	    folder = this.vimPort.findByInventoryPath(getSearchIndex(), filter);
	}
	final Map<String, Vector<Object>> tmpList = this.morefHelper.getAllVAppList(folder);
	final LinkedList<VirtualAppManager> result = new LinkedList<>();
	int i = 0;
	for (final String me : tmpList.keySet()) {
	    try {
		final ManagedObjectReference mor = (ManagedObjectReference) tmpList.get(me).get(0);
		final VAppConfigInfo config = (VAppConfigInfo) tmpList.get(me).get(1);

		final VirtualAppManager vmm = new VirtualAppManager(this, me, mor, config);

		LoggerUtils.logInfo(logger, "%d: %s %s %s", i, vmm.getUuid(), vmm.getMorefValue(), vmm.getName());
		result.add(vmm);
		i++;
	    } catch (final Exception e) {
		LoggerUtils.logWarning(logger, "Virtual Machine information missing:  %s skipped", me);
	    }

	}
	return result;
    }

    LinkedList<VirtualMachineManager> getAllVmList(final String filter)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

	ManagedObjectReference folder = null;
	if ((filter != null) & !filter.isEmpty()) {
	    folder = this.vimPort.findByInventoryPath(getSearchIndex(), filter);
	}
	final Map<String, Vector<Object>> tmpList = this.morefHelper.getAllVmList(folder);
	final LinkedList<VirtualMachineManager> ret = new LinkedList<>();
	int i = 0;
	for (final String me : tmpList.keySet()) {
	    try {
		final ManagedObjectReference vmMor = (ManagedObjectReference) tmpList.get(me).get(0);
		final VirtualMachineConfigInfo config = (VirtualMachineConfigInfo) tmpList.get(me).get(1);

		final VirtualMachineManager vmm = new VirtualMachineManager(this, me, vmMor, config);

		LoggerUtils.logInfo(logger, "%d: %s %s %s", i, vmm.getUuid(), vmm.getMorefValue(), vmm.getName());
		ret.add(vmm);
		i++;
	    } catch (final Exception e) {
		LoggerUtils.logWarning(logger, "Virtual Machine information missing:  %s skipped", me);
	    }

	}
	return ret;
    }

    @Override
    public String getCookie() {
	return this.cookieVal;
    }

    @Override
    public String getCookieValue() {
	String[] tokens = this.cookieVal.split(";");
	tokens = tokens[0].split("=");
	final String extractedCookie = StringUtils.strip(tokens[1], "\"");
	return extractedCookie;

    }

    private UserSession getCurrentSession() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

	logger.entering(getClass().getName(), "getCurrentSession");
	final ManagedObjectReference entityMor = this.serviceContent.getSessionManager();

	final HashMap<String, Object> retVal = new HashMap<>();

	final PropertySpec propertySpec = new PropertySpec();
	propertySpec.setAll(Boolean.FALSE);
	propertySpec.setType(entityMor.getType());
	propertySpec.getPathSet().add("currentSession");

	final ObjectSpec objectSpec = new ObjectSpec();
	objectSpec.setObj(entityMor);

	final PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
	propertyFilterSpec.getPropSet().add(propertySpec);
	propertyFilterSpec.getObjectSet().add(objectSpec);

	final List<ObjectContent> oCont = this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(),
		Arrays.asList(propertyFilterSpec), new RetrieveOptions()).getObjects();

	if (oCont != null) {
	    for (final ObjectContent oc : oCont) {
		final List<DynamicProperty> dps = oc.getPropSet();
		for (final DynamicProperty dp : dps) {
		    retVal.put(dp.getName(), dp.getVal());
		}
	    }
	}
	final UserSession result = (UserSession) retVal.get("currentSession");
	logger.exiting(getClass().getName(), "getCurrentSession", result);
	return result;

    }

    public ManagedEntityInfo getDatacenterByHostsystem(final ManagedObjectReference hostMor) {
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

	List<ObjectContent> oCont = null;
	try {
	    oCont = this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(), propertyFilterSpecs,
		    new RetrieveOptions()).getObjects();

	    if (oCont != null) {
		for (final ObjectContent oc : oCont) {
		    dcMor = oc.getObj();
		    name = oc.getPropSet().get(0).getVal().toString();
		    break;
		}
	    }
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	final ManagedEntityInfo datacenterInfo = new ManagedEntityInfo(name, dcMor, getServerIntanceUuid());
	return datacenterInfo;
    }

    public ManagedEntityInfo getDatacenterByMoref(final ManagedObjectReference moref) {
	ManagedObjectReference dcMor = null;
	String name = null;
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

	List<ObjectContent> oCont = null;
	try {
	    oCont = this.vimPort.retrievePropertiesEx(this.serviceContent.getPropertyCollector(), propertyFilterSpecs,
		    new RetrieveOptions()).getObjects();

	    if (oCont != null) {
		for (final ObjectContent oc : oCont) {
		    dcMor = oc.getObj();
		    name = oc.getPropSet().get(0).getVal().toString();
		    break;
		}
	    }
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	final ManagedEntityInfo datacenterInfo = new ManagedEntityInfo(name, dcMor, getServerIntanceUuid());
	return datacenterInfo;
    }

    public ManagedObjectReference getDatastoreByHost(final ManagedObjectReference hostMor) {
	return getDatastoreByHost(hostMor, null);
    }

    public ManagedObjectReference getDatastoreByHost(final ManagedObjectReference hostMor, final String datastore) {
	ManagedObjectReference dsMor = null;

	List<ManagedObjectReference> dsList = null;
	try {
	    dsList = ((ArrayOfManagedObjectReference) this.morefHelper.entityProps(hostMor, "datastore"))
		    .getManagedObjectReference();
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e1) {
	    logger.warning(Utility.toString(e1));
	}

	if (dsList.isEmpty()) {
	    throw new RuntimeException("No Datastores accesible from host " + hostMor.getValue());
	}
	if (datastore == null) {
	    dsMor = dsList.get(0);
	} else {
	    for (final ManagedObjectReference ds : dsList) {
		try {
		    if (datastore.equalsIgnoreCase(
			    (String) this.morefHelper.entityProps(ds, new String[] { "name" }).get("name"))) {
			dsMor = ds;
			break;
		    }
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));
		}
	    }
	}
	if (dsMor == null) {
	    if (datastore != null) {
		throw new RuntimeException(
			"No Datastore by name " + datastore + " is accessible from host " + hostMor.getValue());
	    }
	    throw new RuntimeException("No Datastores accesible from host " + hostMor.getValue());
	}
	return dsMor;
    }
    // TODO Remove unused code found by UCDetector
//    private ManagedObjectReference getDatastoreByHost(final String host, final String datastore) {
//
//	return getDatastoreByHost(MorefUtil.create(EntityType.HostSystem, host), datastore);
//    }

// TODO Remove unused code found by UCDetector
//     public ManagedObjectReference getSnapshotReference(final ManagedObjectReference vmMor, final String snapName)
// 	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
// 	final VirtualMachineSnapshotInfo snapInfo = (VirtualMachineSnapshotInfo) this.morefHelper
// 		.entityProps(vmMor, new String[] { "snapshot" }).get("snapshot");
// 	ManagedObjectReference snapmor = null;
// 	if (snapInfo != null) {
// 	    final List<VirtualMachineSnapshotTree> listvmst = snapInfo.getRootSnapshotList();
// 	    snapmor = traverseSnapshotInTree(listvmst, snapName, false);
// 	    if (snapmor == null) {
// 		logger.warning("No Snapshot named : " + snapName);
// 	    }
// 	} else {
//
// 	    logger.warning("No Snapshots found ");
// 	}
// 	return snapmor;
//     }
    /**
     * retrieve a datastore entity by name
     * 
     * @param name
     * @return
     */
    public ManagedEntityInfo getDatastoreByName(final String name) {
	logger.entering(getClass().getName(), "getDatastoreByName", name);
	ManagedEntityInfo result = null;
	if (StringUtils.isNotEmpty(name)) {
	    final Map<String, ManagedObjectReference> datastoreList = getDatastoreList();
	    if (datastoreList.containsKey(name)) {
		result = new ManagedEntityInfo(name, datastoreList.get(name), this.instanceUuid);

	    }
	}
	logger.exiting(getClass().getName(), "getDatastoreByName", result);
	return result;
    }

    public Map<String, ManagedObjectReference> getDatastoreList() {
	Map<String, ManagedObjectReference> results = null;

	try {
	    results = this.morefHelper.inFolderByType(getRootFolder(), EntityType.Datastore, new RetrieveOptions());
	} catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}

	return results;
    }

    public ManagedEntityInfo getDefaultResurcePool() {
	ManagedObjectReference rpMoref = null;
	final String defaultRP = GlobalConfiguration.getRpFilter();
	if ((defaultRP != null) && !defaultRP.isEmpty()) {
	    try {
		rpMoref = this.vimPort.findByInventoryPath(this.serviceContent.getSearchIndex(), defaultRP);
	    } catch (final RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		return null;
	    }
	    return new ManagedEntityInfo(defaultRP.substring(defaultRP.lastIndexOf('/') + 1), rpMoref,
		    getServerIntanceUuid());
	}
	return null;
    }

    String getDiskPathForVc(final ManagedEntityInfo dcInfo, final String fileNameOfDisk) {
	logger.entering(getClass().getName(), "getDiskPathForVc", new Object[] { dcInfo, fileNameOfDisk });

	final String regex1 = "\\[(.*)\\]\\s(.*)/(.*\\.vmdk)";
	String ds = null;
	String vmFolder = null;
	String vmdk = null;
	if (Pattern.matches(regex1, fileNameOfDisk)) {

	    final Pattern pattern1 = Pattern.compile(regex1);
	    final Matcher m = pattern1.matcher(fileNameOfDisk);
	    if (m.find()) {
		ds = m.group(1);
		vmFolder = m.group(2);
		vmdk = m.group(3);
	    }
	}
	/*
	 * diskPath format as recognized by VC:
	 * https://<VCIP>/folder/<PathToVmdkInsideDatastore
	 * >?dcPath=<DataCenterName>&dsName=<DatastoreName>
	 *
	 * Ex: diskpath = https://10.160.232.230/folder/TestVm_REKZ/TestVm_REKZ.vmdk
	 * ?dcPath=vcqaDC&dsName=sharedVmfs-0
	 */
	final String result = "https://" + getHost() + "/" + "folder/" + vmFolder + "/" + vmdk + "?dcPath="
		+ dcInfo.getName() + "&dsName=" + ds;
	logger.exiting(getClass().getName(), "getDiskPathForVc", result);
	return result;
    }

    public Map<String, ManagedObjectReference> getHostList() {
	Map<String, ManagedObjectReference> results = null;

	try {
	    results = this.morefHelper.inFolderByType(getRootFolder(), EntityType.HostSystem, new RetrieveOptions());
	} catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}

	return results;
    }

// TODO Remove unused code found by UCDetector
//     ManagedObjectReference getvmFolderByDatastore(final ManagedObjectReference dsMor, final String folder)
// 	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
//
// 	final ManagedEntityInfo datacenterInfo = getDatacenterByDatastore(dsMor);
// 	return getvmFolderByDatacenter(datacenterInfo.getMoref(), folder);
//     }

    public String getHostName(final ManagedObjectReference hostMor) {
	String name = null;
	try {
	    name = (String) this.morefHelper.entityProps(hostMor, "name");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}

	return name;
    }

    public List<DatastoreHostMount> getHostsByDatastore(final ManagedObjectReference datastore)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	final ArrayOfDatastoreHostMount datastoreHostsMount = (ArrayOfDatastoreHostMount) this.morefHelper
		.entityProps(datastore, "host");
	return datastoreHostsMount.getDatastoreHostMount();
    }

    public Jvddk getJvddk() {
	return this.jvddk;
    }

    public String getLookupServiceUuidReference() {
	return this.lookupServiceUuidReference;
    }

    public ManagedEntityInfo getManagedEntityInfo(final EntityType entity, final String name) {
	return getManagedEntityInfo(getRootFolder(), entity, name);
    }

    public ManagedEntityInfo getManagedEntityInfo(final ManagedObjectReference root, final EntityType entity,
	    String name) {
	ManagedObjectReference moref = null;
	final Map<String, ManagedObjectReference> entityList = getManagedEntityInfoList(root, entity);
	if (entityList == null) {
	    return null;
	}
	if (name == null) {
	    name = entityList.keySet().stream().findFirst().get();
	    moref = entityList.values().stream().findFirst().get();
	} else {
	    moref = entityList.get(name);
	}
	if (moref == null) {
	    return null;
	}
	return new ManagedEntityInfo(name, moref, getServerIntanceUuid());

    }

    private Map<String, ManagedObjectReference> getManagedEntityInfoList(final ManagedObjectReference root,
	    final EntityType entity) {
	Map<String, ManagedObjectReference> results = null;
	try {
	    results = this.morefHelper.inFolderByType(root, entity.toString(), new RetrieveOptions());
	} catch (RuntimeFaultFaultMsg | InvalidPropertyFaultMsg e) {
	    logger.warning(Utility.toString(e));
	}
	return results;
    }

    /**
     * @return
     */
    public PbmConnection getPbmConnection() {
	return this.pbmConnection;
    }

    public String getPrivateKeyFileName() {
	return this.privateKeyFileName;
    }

    public ManagedObjectReference getResourcePoolByHost(final ManagedObjectReference hostMor)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
	ManagedObjectReference rpMor = null;
	Map<String, Object> hostProp = null;
	hostProp = this.morefHelper.entityProps(hostMor, new String[] { "parent" });
	rpMor = (ManagedObjectReference) this.morefHelper.entityProps((ManagedObjectReference) hostProp.get("parent"),
		"resourcePool");
	return rpMor;
    }

    /**
     * @return
     */
    public ManagedObjectReference getRootFolder() {

	return this.serviceContent.getRootFolder();
    }

    public ManagedObjectReference getSearchIndex() {
	return this.serviceContent.getSearchIndex();
    }

    public String getServerIntanceUuid() {
	return this.instanceUuid;
    }

    private String getSessionCookieUsingHokToken(final Element token, final String vcServerUrl)
	    throws InvalidLocaleFaultMsg, InvalidLoginFaultMsg, RuntimeFaultFaultMsg {

	this.cookieExtractor = new HeaderCookieExtractionHandler();
	updateHeaderHandlerResolver(token);
	this.vimPort = this.vimService.getVimPort();

	final Map<String, Object> ctxt = ((BindingProvider) this.vimPort).getRequestContext();
	ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vcServerUrl);
	ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

	this.vimPort.loginByToken(this.serviceContent.getSessionManager(), null);
	final String cookie = this.cookieExtractor.getCookie();

	clearHandlerResolver(vcServerUrl, cookie);
	return cookie;
    }

    public VapiAuthenticationHelper getVapiService() {
	return this.vapiService;
    }

    public URL getVapiUrl() throws MalformedURLException {
	return this.vapiService.getVapiURL();
    }

    private ManagedObjectReference getVAppByName(final String name) {
	ManagedObjectReference vmRef;
	try {
	    vmRef = this.morefHelper.vAppByName(name, this.serviceContent.getPropertyCollector());
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	return vmRef;
    }

    public VirtualMachineConfigInfo getVirtualMachineConfigInfo(final ManagedObjectReference vmMor) {
	VirtualMachineConfigInfo configInfo = null;
	try {
	    configInfo = (VirtualMachineConfigInfo) this.morefHelper.entityProps(vmMor, "config");
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}

	return configInfo;

    }

    private ManagedObjectReference getVmByName(final String name) {
	ManagedObjectReference vmRef;
	try {
	    vmRef = this.morefHelper.vmByName(name, this.serviceContent.getPropertyCollector());
	} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));
	    return null;
	}
	return vmRef;
    }

    /**
     * @return
     */
    public VslmConnection getVslmConnection() {
	return this.vslmConnection;
    }

    public String getX509CertFileName() {
	return this.x509CertFileName;
    }

    /**
     * @param string
     * @param string2
     * @param ovfImport
     * @param string3
     */
    public void importVApp(final URL urlPath, final String vappName, final String host, final String datastore,
	    final String resourcePool, final String vmFolder) {
	final OvfImport ovf = new OvfImport(this);
	ovf.importVApp(urlPath, vappName, host, datastore, resourcePool, vmFolder);

    }

    /**
     *
     * @param name
     * @return
     */
    public boolean isIvdByNameExist(final String name) {
	logger.entering(getClass().getName(), "isIvdByNameExist", name);
	final ImprovedVirtuaDisk ivd = getVslmConnection().getIvdByName(name);
	final boolean ret = (ivd == null) ? false : true;
	logger.exiting(getClass().getName(), "isIvdByNameExist", ret);
	return ret;
    }

    public boolean isVAppByNameExist(final String name) {
	logger.entering(getClass().getName(), "isVAppByNameExist", name);
	final ManagedObjectReference vmMoref = getVAppByName(name);
	final boolean ret = (vmMoref == null) ? false : true;
	logger.exiting(getClass().getName(), "isVAppByNameExist", ret);
	return ret;
    }

    public boolean isVmByNameExist(final String name) {
	logger.entering(getClass().getName(), "isVmByNameExist", name);
	final ManagedObjectReference vmMoref = getVmByName(name);
	final boolean ret = (vmMoref == null) ? false : true;
	logger.exiting(getClass().getName(), "isVmByNameExist", ret);
	return ret;
    }

    void keepAlive() throws RuntimeFaultFaultMsg {
	logger.info(String.format("keep alive %s : Current Time %s", getServerIntanceUuid(),
		getVimPort().currentTime(getServiceInstanceReference())));
    }

    private String loginUsingSAMLToken(final Element token, final String vcServerUrl)
	    throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg {

	this.vimService = new VimService();

	this.vimPort = this.vimService.getVimPort();

	final Map<String, Object> ctxt = ((BindingProvider) this.vimPort).getRequestContext();
	ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vcServerUrl);
	ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, false);

	this.serviceContent = this.vimPort.retrieveServiceContent(getServiceInstanceReference());

	if (SsoUtils.isHoKToken(token)) {
	    return getSessionCookieUsingHokToken(token, vcServerUrl);
	} else {
	    LoggerUtils.logWarning(logger, "failed to connect to %s : token %s  not valid", vcServerUrl,
		    token.toString());
	    return null;
	}
    }

    /**
     * @param virtualMachineManager
     * @param key
     * @param startPosition
     * @param prevChanges
     * @return
     */
    public DiskChangeInfo queryChangedDiskAreas(final VirtualMachineManager vmm, final int key,
	    final long startPosition, final String prevChanges) {
	logger.entering(getClass().getName(), "queryChangedDiskAreas",
		new Object[] { vmm, key, startPosition, prevChanges });
	DiskChangeInfo result = null;
	try {
	    result = getVimPort().queryChangedDiskAreas(vmm.getMoref(), vmm.getSnapshotManager().getMoref(), key,
		    startPosition, prevChanges);
	} catch (FileFaultFaultMsg | NotFoundFaultMsg | RuntimeFaultFaultMsg e) {
	    logger.warning(Utility.toString(e));

	}
	logger.exiting(getClass().getName(), "queryChangedDiskAreas");
	return result;
    }

    public ManagedObjectReference registerVirtualMachine(final RestoreManagedInfo managedInfo) {
	final String vmxPath = String.format("[%s] %s/%s.vmx", managedInfo.getDsInfo().getName(),
		managedInfo.getDirectoryName(), managedInfo.getName());
	return registerVirtualMachine(vmxPath, managedInfo.getName(), managedInfo.getHostInfo(),
		managedInfo.getFolderInfo(), managedInfo.getRpInfo());
    }

    private ManagedObjectReference registerVirtualMachine(final String vmxPath, final String vmName,
	    final ManagedEntityInfo hostInfo, final ManagedEntityInfo folderInfo, final ManagedEntityInfo rpInfo) {
	ManagedObjectReference registeredVMRef = null;
	IoFunction.showInfo(logger, "Registering Virtual Machine %s Name:%s", vmxPath, vmName);

	if (hostInfo == null) {
	    IoFunction.showWarning(logger, "Host is undefined");
	}
	if (folderInfo == null) {
	    IoFunction.showWarning(logger, "VmFolder is undefined");
	}
	if (rpInfo == null) {
	    IoFunction.showWarning(logger, "ResourcePool  is undefined");
	}

	if ((hostInfo != null) && (folderInfo != null) && (rpInfo != null)) {
	    try {
		final ManagedObjectReference taskmor = this.vimPort.registerVMTask(folderInfo.getMoref(), vmxPath,
			vmName, false, rpInfo.getMoref(), hostInfo.getMoref());

		if (waitForTask(taskmor)) {
		    // System.out.print("*");

		    registeredVMRef = (ManagedObjectReference) this.morefHelper
			    .entityProps(taskmor, new String[] { "info.result" }).get("info.result");

		    IoFunction.showInfo(logger, "VM %s registered Moref:%s", vmName, registeredVMRef.getValue());

		} else {
		    IoFunction.showWarning(logger, "Failed to register Virtual Machine %s Name:%s", vmxPath, vmName);

		}
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg
		    | AlreadyExistsFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg
		    | InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidNameFaultMsg
		    | InvalidStateFaultMsg | NotFoundFaultMsg | OutOfBoundsFaultMsg | VmConfigFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
		return null;
	    }
	}

	return registeredVMRef;
    }

    public void setPrivateKeyFileName(final String privateKeyFileName) {
	this.privateKeyFileName = privateKeyFileName;
    }

    public void setX509CertFileName(final String x509CertFileName) {
	this.x509CertFileName = x509CertFileName;
    }

    void socket() throws NoSuchAlgorithmException, UnknownHostException, IOException, CertificateEncodingException {

	final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
	final SSLSocketFactory factory = this.pscConnection.getSslContext().getSocketFactory();
	final SSLSocket socket = (SSLSocket) factory.createSocket(getURL().getHost(), getURL().getPort());
	socket.startHandshake();
	final SSLSession session = socket.getSession();
	final java.security.cert.Certificate[] servercerts = session.getPeerCertificates();
	for (final Certificate servercert : servercerts) {
	    logger.fine("-----BEGIN CERTIFICATE-----\n");
	    logger.fine(Base64.encodeBase64String(servercert.getEncoded()));
	    logger.fine("\n-----END CERTIFICATE-----\n");
	    if (this.thumbPrint == null) {
		sha1.update(servercert.getEncoded());
		String sha1Digest = Utility.toHexString(sha1.digest()).replaceAll(" ", ":");
		sha1Digest = sha1Digest.substring(0, sha1Digest.length() - 1);
		this.thumbPrint = sha1Digest;
		logger.info("Thumb: " + this.thumbPrint);
	    }
	}
	socket.close();
    }

    void startKeepAlive() {
	this.keepAlive = KeepAlive.keepAlive(this);
	this.keepAlive.start();
    }

    HeaderHandlerResolver updateHeaderHandlerResolver(final Element token) {

	final HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
	handlerResolver.addHandler(new TimeStampHandler());
	handlerResolver.addHandler(new SamlTokenHandler(token));
	handlerResolver.addHandler(this.cookieExtractor);
	handlerResolver.addHandler(new WsSecuritySignatureAssertionHandler(this.pscConnection.getUserCert(),

		SsoUtils.getNodeProperty(token, "ID")));
	this.vimService.setHandlerResolver(handlerResolver);
	resetTokenUpdateSessionEventTime();
	return handlerResolver;
    }

    /**
     *
     */
    public HeaderHandlerResolver updatePbmHeaderHandlerResolver() {
	return this.pbmConnection.updatePbmHeaderHandlerResolver();

    }

    StubConfiguration updateVapi(final SamlToken samlToken) {
	logger.entering(getClass().getName(), "updateVapi", samlToken);

	final StubConfiguration result = this.vapiService.updateSamlBearerToken(samlToken);
	logger.exiting(getClass().getName(), "updateVapi", result);
	return result;

    }

    public HeaderHandlerResolver updateVslmHeaderHandlerResolver() {
	return this.vslmConnection.updateVslmHeaderHandlerResolver();
    }

    public boolean waitForTask(final ManagedObjectReference task)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
	logger.entering(getClass().getName(), "waitForTask", task);
	boolean result = false;

	final Object[] waitResult = this.waitForValues.wait(task, new String[] { "info.state", "info.error" },
		new String[] { "state" },
		new Object[][] { new Object[] { TaskInfoState.SUCCESS, TaskInfoState.ERROR } });

	if (waitResult[0].equals(TaskInfoState.SUCCESS)) {
	    result = true;
	}
	if (waitResult[1] instanceof LocalizedMethodFault) {
	    IoFunction.showWarning(logger, ((LocalizedMethodFault) waitResult[1]).getLocalizedMessage());
	    result = false;
	}

	logger.exiting(getClass().getName(), "waitForTask");
	return result;
    }

}
