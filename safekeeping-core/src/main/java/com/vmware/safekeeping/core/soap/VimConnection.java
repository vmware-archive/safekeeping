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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import com.vmware.jvix.CleanUpResults;
import com.vmware.jvix.jDiskLib.ConnectParams;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.pbm.PbmAboutInfo;
import com.vmware.pbm.PbmServiceInstanceContent;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.core.SJvddk;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.soap.helpers.VapiAuthenticationHelper;
import com.vmware.safekeeping.core.soap.managers.VimExtensionManager;
import com.vmware.safekeeping.core.soap.managers.VimPrivilegeChecker;
import com.vmware.safekeeping.core.soap.sso.HeaderCookieExtractionHandler;
import com.vmware.safekeeping.core.soap.sso.HeaderHandlerResolver;
import com.vmware.safekeeping.core.soap.sso.SamlTokenHandler;
import com.vmware.safekeeping.core.soap.sso.SsoUtils;
import com.vmware.safekeeping.core.soap.sso.TimeStampHandler;
import com.vmware.safekeeping.core.soap.sso.WsSecuritySignatureAssertionHandler;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.SearchManagementEntity;
import com.vmware.safekeeping.core.type.StorageDirectoryInfo;
import com.vmware.safekeeping.core.type.VmbkThreadFactory;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.SearchManagementEntityInfoType;
import com.vmware.safekeeping.core.type.enums.VMwareCloudPlatforms;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vim25.AboutInfo;
import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.ArrayOfDatastoreHostMount;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.DiskChangeInfo;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.FileNotFoundFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidDatastorePathFaultMsg;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
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
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.TimedoutFaultMsg;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VimService;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class VimConnection extends AbstractConnection {

    private static final int MAX_NUMBER_OF_PROPERTY_COLLECTOR = 5;

    static {
        AbstractConnection.logger = Logger.getLogger(VimConnection.class.getName());
    }
    private HeaderCookieExtractionHandler cookieExtractor;
    private String cookieVal;

    private HandlerResolver defaultHandler;

    private String instanceUuid;
    private final String lookupServiceUuidReference;

    private String privateKeyFileName;

    private final ISingleSignOn ssoConnection;
    private final VapiAuthenticationHelper vapiService;

    private AboutInfo aboutInfo;

    private String x509CertFileName;

    private ScheduledExecutorService scheduler;

    private boolean connectToVslm;

    private ManagedObjectReference taskManager;
    private ManagedObjectReference eventManager;

    // sub-classes
    private VimPrivilegeChecker privilegeChecker;
    private VimExtensionManager extensionManager;
    private MorefUtil vimHelper;
    private VimFindFco find;

    private final VslmConnection vslmConnection;
    private final PbmConnection pbmConnection;

    VimConnection(final ISingleSignOn ssoConnection, final String lookupServiceUuidReference, final String url,
            VMwareCloudPlatforms platform, final boolean connectToVslm)
            throws MalformedURLException, com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg {
        super(ssoConnection.getNfcHostPort(), platform);
        setUrl(url);
        this.lookupServiceUuidReference = lookupServiceUuidReference;
        this.ssoConnection = ssoConnection;
        this.connectToVslm = connectToVslm;

        final URL pbmUrl = new URL(
                ssoConnection.getLookup().getLookupServiceHelper().findVimPbmUrl(lookupServiceUuidReference));
        final URL vapiUrl = new URL(
                ssoConnection.getLookup().getLookupServiceHelper().findVapiUrl(lookupServiceUuidReference));
        final URL vslmUrl = new URL(
                ssoConnection.getLookup().getLookupServiceHelper().findVimVslmUrl(lookupServiceUuidReference));

        final HttpConfiguration httpConfig = buildHttpConfiguration();
        this.vapiService = new VapiAuthenticationHelper(vapiUrl, httpConfig, this.ssoConnection.getUserCert());
        this.vslmConnection = new VslmConnection(this, vslmUrl);
        this.pbmConnection = new PbmConnection(this, pbmUrl);

        ssoConnection.getVimConnections().add(this);
    }

    private boolean cleanup() {
        boolean success = false;
        final ConnectParams vddkConnection = newConnectParamsInstance();
        vddkConnection.setVmxSpec("moref=vm-682246");
        vddkConnection.setDatastoreMoRef("");
        vddkConnection.setSsId("");
        boolean exit = false;
        for (int i = 0; i < 3; i++) {
            final CleanUpResults cleanupResult = SJvddk.cleanup(vddkConnection);
            if (cleanupResult.getVddkCallResult() == jDiskLibConst.VIX_OK) {
                if (cleanupResult.getNumRemaining() == 0) {
                    success = true;
                    exit = true;
                }
            } else {
                exit = true;
            }
            if (exit) {
                break;
            }

        }
        return success;
    }

    private void clearHandlerResolver(final String vcServerUrl, final String cookie) {

        this.vimService.setHandlerResolver(this.defaultHandler);
        this.vimPort = this.vimService.getVimPort();

        final Map<String, Object> ctxt = ((BindingProvider) this.vimPort).getRequestContext();
        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vcServerUrl);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Cookie", Arrays.asList(cookie));
        ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, headers);

    }

    public ConnectParams configureVddkAccess(final ImprovedVirtualDisk ivd) {
        return configureVddkAccess(ivd, null);
    }

    public ConnectParams configureVddkAccess(final ImprovedVirtualDisk ivd, final String ssId) {
        final ConnectParams connectParams = newConnectParamsInstance();

        connectParams.setSpecType(ConnectParams.VIXDISKLIB_SPEC_VSTORAGE_OBJECT);
        connectParams.setId(ivd.getUuid());
        connectParams.setDatastoreMoRef(ivd.getDatastoreInfo().getMorefValue());
        connectParams.setSsId(ssId);
        return connectParams;
    }

    public ConnectParams configureVddkAccess(final VirtualMachineManager vmm) {

        final ConnectParams connectParams = newConnectParamsInstance();
        connectParams.setVmxSpec(String.format("moref=%s", vmm.getMorefValue()));
        connectParams.setSpecType(ConnectParams.VIXDISKLIB_SPEC_VMX);
        return connectParams;
    }

    @Override
    public IConnection connect(final Element token) throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg,
            InvalidLoginFaultMsg, InvalidPropertyFaultMsg, CertificateEncodingException, NoSuchAlgorithmException,
            IOException, DatatypeConfigurationException, NotFoundFaultMsg {
        if (!isConnected()) {
            socket();
            connectByToken(token);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private void connectByToken(final Element token)
            throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg, InvalidPropertyFaultMsg {

        this.cookieVal = loginUsingSAMLToken(token, getURL().toString());

        this.vimService = new VimService();

        this.vimPort = this.vimService.getVimPort();
        this.defaultHandler = this.vimService.getHandlerResolver();
        final Map<String, Object> ctxt = ((BindingProvider) this.vimPort).getRequestContext();
        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getUrl());
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

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

        this.headers = (Map<String, Object>) ((BindingProvider) this.vimPort).getResponseContext()
                .get(MessageContext.HTTP_RESPONSE_HEADERS);
        this.vimHelper = new MorefUtil(this, VimConnection.MAX_NUMBER_OF_PROPERTY_COLLECTOR);
        this.privilegeChecker = new VimPrivilegeChecker(this);
        this.find = new VimFindFco(this);
        this.extensionManager = new VimExtensionManager(this);
        this.taskManager = this.serviceContent.getTaskManager();
        this.eventManager = this.serviceContent.getEventManager();
    }

    /**
     * @return
     * @throws MalformedURLException
     * @throws ConnectException
     *
     */
    PbmServiceInstanceContent connectPbm() throws ConnectException {
        return getPbmConnection().connectPbm();

    }

    StubConfiguration connectVapi(final SamlToken samlToken) {
        return this.vapiService.loginBySamlBearerToken(samlToken);
    }

    /**
     * @return
     * @throws MalformedURLException
     * @throws ConnectException
     *
     */
    VslmConnection connectVslm() throws ConnectException {
        return getVslmConnection().connectVslm();

    }

    public ManagedObjectReference createChildVm(final VirtualMachineConfigSpec vmConfigSpec,
            final ManagedEntityInfo resourcePool, final ManagedEntityInfo host)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, RuntimeFaultFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException,
            InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, OutOfBoundsFaultMsg {

        ManagedObjectReference moRef = null;

        final ManagedObjectReference taskMor = getVimPort().createChildVMTask(resourcePool.getMoref(), vmConfigSpec,
                host.getMoref());
        if (waitForTask(taskMor)) {
            moRef = (ManagedObjectReference) taskResult(taskMor);
        }

        return moRef;
    }

    /**
     * @param vim
     * @param name
     * @param resSpec
     * @param configSpec
     * @return
     * @throws VmConfigFaultFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     * @throws DuplicateNameFaultMsg
     */
    public ManagedObjectReference createVApp(final String name, final ManagedEntityInfo rpInfo,
            final ManagedEntityInfo folderInfo, final ResourceConfigSpec resSpec, final VAppConfigSpec configSpec)
            throws DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, VmConfigFaultFaultMsg {
        ManagedObjectReference result = null;

        if (resSpec.getCpuAllocation().getReservation() == null) {
            resSpec.getCpuAllocation().setReservation((long) 0);
        }
        if (resSpec.getMemoryAllocation().getReservation() == null) {
            resSpec.getMemoryAllocation().setReservation((long) 0);
        }
        result = getVimPort().createVApp(rpInfo.getMoref(), name, resSpec, configSpec, folderInfo.getMoref());

        return result;
    }

    /**
     * Create a Virtual Machine
     *
     * @param vmConfigSpec
     * @param folder
     * @param resourcePool
     * @param host
     * @return
     * @throws FileFaultFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     * @throws VimTaskException
     * @throws InterruptedException
     * @throws DuplicateNameFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws AlreadyExistsFaultMsg
     * @throws OutOfBoundsFaultMsg
     */
    public ManagedObjectReference createVm(final VirtualMachineConfigSpec vmConfigSpec, final ManagedEntityInfo folder,
            final ManagedEntityInfo resourcePool, final ManagedEntityInfo host)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException,
            InterruptedException, DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg,
            AlreadyExistsFaultMsg, OutOfBoundsFaultMsg {

        ManagedObjectReference moRef = null;

        final ManagedObjectReference taskMor = getVimPort().createVMTask(folder.getMoref(), vmConfigSpec,
                resourcePool.getMoref(), host.getMoref());
        if (waitForTask(taskMor)) {
            moRef = (ManagedObjectReference) taskResult(taskMor);
        }

        return moRef;
    }

    public boolean deleteDirectory(final StorageDirectoryInfo directory) throws FileFaultFaultMsg, FileNotFoundFaultMsg,
            InvalidDatastoreFaultMsg, InvalidDatastorePathFaultMsg, RuntimeFaultFaultMsg {
        if ((directory != null) && StringUtils.isNotBlank(directory.getDirectoryUuid())
                && StringUtils.isNotBlank(directory.getDatastoreUuid())) {
            getVimPort().deleteDirectory(getServiceContent().getDatastoreNamespaceManager(),
                    directory.getDcInfo().getMoref(),
                    directory.getDatastoreUuid() + "/" + directory.getDirectoryUuid());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public IConnection disconnect() throws RuntimeFaultFaultMsg {
        if (isConnected()) {
            try {

                AbstractConnection.logger.info("disconnecting...");
                cleanup();
                if (this.scheduler != null) {
                    this.scheduler.shutdown();
                }
                this.vapiService.logout();
                getVslmConnection().disconnect();

                if (this.vimPort != null) {
                    this.vimPort.logout(this.serviceContent.getSessionManager());
                }
                AbstractConnection.logger.info("disconnected.");

            } finally {
                this.userSession = null;
                this.serviceContent = null;
                this.vimPort = null;
                this.vimService = null;
            }
        }

        return this;
    }

    public AboutInfo getAboutInfo() {
        return this.aboutInfo;
    }

    /**
     * Return a list of available network for a spcefic host
     *
     * @param moref HostSystem Moref
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     */
    public Map<String, ManagedObjectReference> getAvailableHostNetworks(final ManagedObjectReference moref)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {

        final HashMap<String, ManagedObjectReference> networksAvailable = new HashMap<>();
        final ArrayOfManagedObjectReference networks = (ArrayOfManagedObjectReference) getVimHelper().entityProps(moref,
                "network");

        for (final ManagedObjectReference ds : networks.getManagedObjectReference()) {
            try {
                final String st = getVimHelper().entityProps(ds, "name").toString();
                networksAvailable.put(st, ds);
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                Utility.logWarning(AbstractConnection.logger, e);
            }
        }
        return networksAvailable;

    }

    @Override
    public String getCookie() {
        return this.cookieVal;
    }

    @Override
    public String getCookieValue() {
        String[] tokens = this.cookieVal.split(";");
        tokens = tokens[0].split("=");
        return StringUtils.strip(tokens[1], "\"");
    }

    /**
     * Get current User session
     *
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    private UserSession getCurrentSession() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
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
        return (UserSession) retVal.get("currentSession");
    }

    public ManagedEntityInfo getDatacenterByMoref(final ManagedObjectReference moref)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return this.vimHelper.getDatacenterByMoref(moref);
    }

    public ManagedObjectReference getDatastoreByHost(final ManagedObjectReference hostMor)
            throws InterruptedException, VimOperationException {
        return getDatastoreByHost(hostMor, "");
    }

    public ManagedObjectReference getDatastoreByHost(final ManagedObjectReference hostMor,
            final ManagedObjectReference ds) throws InterruptedException, VimOperationException {
        List<ManagedObjectReference> dsList = null;
        try {
            dsList = ((ArrayOfManagedObjectReference) getVimHelper().entityProps(hostMor, "datastore"))
                    .getManagedObjectReference();
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(AbstractConnection.logger, e);
        }

        if ((dsList == null) || dsList.isEmpty()) {
            throw new VimOperationException("No Datastores accesible from host %s ", hostMor.getValue());
        }
        ManagedObjectReference dsMor = null;
        for (final ManagedObjectReference _ds : dsList) {
            dsMor = MorefUtil.compare(_ds, ds) ? ds : null;
        }

        if (dsMor == null) {
            throw new VimOperationException("Datastores accesible from host %s", hostMor.getValue());
        }
        return dsMor;
    }

    public ManagedObjectReference getDatastoreByHost(final ManagedObjectReference hostMor, final String datastore)
            throws InterruptedException, VimOperationException {
        List<ManagedObjectReference> dsList = null;
        try {
            dsList = ((ArrayOfManagedObjectReference) getVimHelper().entityProps(hostMor, "datastore"))
                    .getManagedObjectReference();
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(AbstractConnection.logger, e);
        }

        if ((dsList == null) || dsList.isEmpty()) {
            throw new VimOperationException("No Datastores accesible from host %s", hostMor.getValue());
        }
        ManagedObjectReference dsMor = null;
        if (StringUtils.isEmpty(datastore)) {
            dsMor = dsList.get(0);
        } else {
            for (final ManagedObjectReference ds : dsList) {
                try {
                    if (datastore.equalsIgnoreCase(
                            (String) getVimHelper().entityProps(ds, new String[] { "name" }).get("name"))) {
                        dsMor = ds;
                        break;
                    }
                } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                    Utility.logWarning(AbstractConnection.logger, e);
                }
            }
        }
        if (dsMor == null) {
            if (datastore != null) {
                throw new VimOperationException(
                        "No Datastore by name " + datastore + " is accessible from host " + hostMor.getValue());
            }
            throw new VimOperationException("No Datastores accesible from host " + hostMor.getValue());
        }
        return dsMor;
    }

    public ManagedObjectReference getDatastoreByHost(final String host, final String datastore)
            throws InterruptedException, VimOperationException {

        return getDatastoreByHost(MorefUtil.newManagedObjectReference(EntityType.HostSystem, host), datastore);
    }

    /**
     * retrieve a datastore entity by name
     *
     * @param name
     * @return
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public ManagedEntityInfo getDatastoreByName(final String name)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        ManagedEntityInfo result = null;
        if (StringUtils.isNotEmpty(name)) {
            final Map<String, ManagedObjectReference> datastoreList = getDatastoreList();
            if (datastoreList.containsKey(name)) {
                result = new ManagedEntityInfo(name, datastoreList.get(name), this.instanceUuid);

            }
        }
        return result;
    }

    public Map<String, ManagedObjectReference> getDatastoreList()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return getVimHelper().inFolderByType(getRootFolder(), EntityType.Datastore, new RetrieveOptions());
    }

    public ManagedEntityInfo getDefaultResurcePool() {
        ManagedObjectReference rpMoref = null;
        final String defaultRP = CoreGlobalSettings.getRpFilter();
        if ((defaultRP != null) && !defaultRP.isEmpty()) {
            try {
                rpMoref = this.vimPort.findByInventoryPath(this.serviceContent.getSearchIndex(), defaultRP);
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(AbstractConnection.logger, e);
                return null;
            }
            return new ManagedEntityInfo(defaultRP.substring(defaultRP.lastIndexOf('/') + 1), rpMoref,
                    getServerIntanceUuid());
        }
        return null;
    }

    String getDiskPathForVc(final ManagedEntityInfo dcInfo, final String fileNameOfDisk) {

        String vmdk = StringUtils.substringAfterLast(fileNameOfDisk, "/");
        String s = StringUtils.substringBeforeLast(fileNameOfDisk, "/");

        String ds = StringUtils.substringBefore(s, "]").substring(1);
        String vmFolder = StringUtils.substringAfter(s, "] ");

        /*
         * diskPath format as recognized by VC:
         * https://<VCIP>/folder/<PathToVmdkInsideDatastore
         * >?dcPath=<DataCenterName>&dsName=<DatastoreName>
         *
         * Ex: diskpath = https://10.160.232.230/folder/TestVm_REKZ/TestVm_REKZ.vmdk
         * ?dcPath=vcqaDC&dsName=sharedVmfs-0
         */

        final String result = String.format("https://%s/folder/%s/%s?dcPath=%s&dsName=%s", getHost(), vmFolder, vmdk,
                dcInfo.getName(), ds);
        if (AbstractConnection.logger.isLoggable(Level.FINE)) {
            AbstractConnection.logger.fine("DiskPath:" + result);
        }
        return result;
    }

    public ManagedObjectReference getEventManager() {
        return this.eventManager;
    }

    public VimExtensionManager getExtensionManager() {
        return this.extensionManager;
    }

    public Map<String, ManagedObjectReference> getHostList()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return getVimHelper().inFolderByType(getRootFolder(), EntityType.HostSystem, new RetrieveOptions());

    }

    public String getHostName(final ManagedObjectReference hostMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return (String) getVimHelper().entityProps(hostMor, "name");

    }

    public List<DatastoreHostMount> getHostsByDatastore(final ManagedObjectReference datastore)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final ArrayOfDatastoreHostMount datastoreHostsMount = (ArrayOfDatastoreHostMount) getVimHelper()
                .entityProps(datastore, "host");
        return datastoreHostsMount.getDatastoreHostMount();
    }

    public String getLookupServiceUuidReference() {
        return this.lookupServiceUuidReference;
    }

    /**
     * @param datacenter
     * @param datacenterName
     * @return
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VimObjectNotExistException
     */
    public ManagedEntityInfo getManagedEntityInfo(final EntityType entityType, final SearchManagementEntity search)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {
        if (search.getSearchType() == SearchManagementEntityInfoType.MOREF) {
            final ManagedObjectReference mor = MorefUtil.newManagedObjectReference(entityType, search.getSearchValue());
            return getManagedEntityInfo(mor);
        } else {
            return getManagedEntityInfo(getRootFolder(), entityType, search.getSearchValue());
        }
    }

    /**
     * Create a ManagedEntityInfo using the name
     *
     * @param entity
     * @param name
     * @return
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VimObjectNotExistException
     */
    public ManagedEntityInfo getManagedEntityInfo(final EntityType entity, final String name)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {
        return getManagedEntityInfo(getRootFolder(), entity, name);
    }

    /**
     * Create a ManagedEntityInfo from a moref
     *
     * @param root
     * @param entity
     * @param key
     * @return
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws VimObjectNotExistException
     */
    public ManagedEntityInfo getManagedEntityInfo(final ManagedObjectReference entityMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimObjectNotExistException {
        String name = null;
        name = getVimHelper().entityName(entityMor);
        if (name == null) {
            throw new VimObjectNotExistException(entityMor);
        }
        return new ManagedEntityInfo(name, entityMor, getServerIntanceUuid());
    }

    /**
     *
     * @param root
     * @param entityType
     * @param search
     * @return
     * @throws InterruptedException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws VimObjectNotExistException
     */
    public ManagedEntityInfo getManagedEntityInfo(final ManagedObjectReference root, final EntityType entityType,
            final SearchManagementEntity search)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {
        if (search.getSearchType() == SearchManagementEntityInfoType.MOREF) {
            final ManagedObjectReference mor = MorefUtil.newManagedObjectReference(entityType, search.getSearchValue());
            return getManagedEntityInfo(mor);
        } else {
            return getManagedEntityInfo(root, entityType, search.getSearchValue());
        }
    }

    public ManagedEntityInfo getManagedEntityInfo(final ManagedObjectReference root, final EntityType entity,
            String name)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {
        ManagedEntityInfo result = new ManagedEntityInfo();
        if (StringUtils.isNotEmpty(name) && name.contains("/")) {
            final ManagedObjectReference rpMor = getFind().findByInventoryPath(name);
            if (rpMor != null) {
                result = new ManagedEntityInfo(name.substring(name.lastIndexOf('/') + 1), rpMor,
                        getServerIntanceUuid());
            }

        } else {
            ManagedObjectReference moref = null;
            final Map<String, ManagedObjectReference> entityList = getManagedEntityInfoList(root, entity);
            if (entityList != null) {
                if (name == null) {
                    final Optional<Entry<String, ManagedObjectReference>> oEntry = entityList.entrySet().stream()
                            .findFirst();
                    if (oEntry.isPresent()) {
                        name = oEntry.get().getKey();
                        moref = oEntry.get().getValue();
                    }

                } else {
                    moref = entityList.get(name);
                }
                if (moref != null) {
                    result = new ManagedEntityInfo(name, moref, getServerIntanceUuid());
                } else {
                    throw new VimObjectNotExistException(name, entity);
                }
            }
        }
        return result;
    }

    private Map<String, ManagedObjectReference> getManagedEntityInfoList(final ManagedObjectReference root,
            final EntityType entity) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        return getVimHelper().inFolderByType(root, entity.toString(), new RetrieveOptions());

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
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        ManagedObjectReference rpMor = null;
        Map<String, Object> hostProp = null;
        hostProp = getVimHelper().entityProps(hostMor, new String[] { "parent" });
        rpMor = (ManagedObjectReference) getVimHelper().entityProps((ManagedObjectReference) hostProp.get("parent"),
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

    @Override
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

        this.vimPort.loginByToken(this.serviceContent.getSessionManager(), "en");
        final String cookie = this.cookieExtractor.getCookie();

        clearHandlerResolver(vcServerUrl, cookie);
        return cookie;
    }

    public ISingleSignOn getSSoConnection() {
        return this.ssoConnection;
    }

    public ManagedObjectReference getTaskManager() {
        return this.taskManager;
    }

    @Override
    public String getUsername() {
        return this.ssoConnection.getUsername();
    }

    public VapiAuthenticationHelper getVapiService() {
        return this.vapiService;
    }

    public URL getVapiUrl() {
        return this.vapiService.getVapiURL();
    }

    /**
     * Get Vapp by name
     *
     * @param name
     * @return
     * @throws InterruptedException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public ManagedObjectReference getVAppByName(final String name) {
        try {
            return getVimHelper().vAppByName(name);
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(AbstractConnection.logger, e);
        } catch (final InterruptedException e) {
            AbstractConnection.logger.warning(e.getMessage());
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return null;
    }

    protected ManagedObjectReference getVAppByUuid(final String name) {
        try {
            return getVimHelper().vAppByUuid(name);
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(AbstractConnection.logger, e);
        } catch (final InterruptedException e) {
            AbstractConnection.logger.warning(e.getMessage());
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public MorefUtil getVimHelper() {
        return this.vimHelper;
    }

    public VirtualMachineConfigInfo getVirtualMachineConfigInfo(final ManagedObjectReference vmMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        VirtualMachineConfigInfo configInfo = null;
        configInfo = (VirtualMachineConfigInfo) getVimHelper().entityProps(vmMor, "config");
        return configInfo;

    }

    public ManagedObjectReference getVmByName(final String name) {
        ManagedObjectReference vmRef = null;
        try {
            vmRef = getVimHelper().vmByName(name);
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(AbstractConnection.logger, e);
        } catch (final InterruptedException e) {
            AbstractConnection.logger.warning(e.getMessage());
            // Restore interrupted state...
            Thread.currentThread().interrupt();
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
     * @return
     * @throws InvalidCollectorVersionFaultMsg
     * @throws OutOfBoundsFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     * @throws DuplicateNameFaultMsg
     * @throws IOException
     * @throws VmConfigFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws ConcurrentAccessFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws TimedoutFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws InterruptedException
     * @throws SafekeepingConnectionException
     * @throws SafekeepingException
     * @throws SafekeepingUnsupportedObjectException
     */
    ManagedFcoEntityInfo importVApp(final URL urlPath, final String vappName, final String host, final String datastore,
            final String resourcePool, final String vmFolder) throws KeyManagementException, NoSuchAlgorithmException,
            RuntimeFaultFaultMsg, TimedoutFaultMsg, InvalidPropertyFaultMsg, ConcurrentAccessFaultMsg,
            FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, TaskInProgressFaultMsg,
            VmConfigFaultFaultMsg, IOException, DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg,
            InvalidNameFaultMsg, OutOfBoundsFaultMsg, InvalidCollectorVersionFaultMsg, InterruptedException,
            SafekeepingUnsupportedObjectException, SafekeepingException, SafekeepingConnectionException {
        final OvfImport ovf = new OvfImport(this);
        return ovf.importVApp(urlPath, vappName, host, datastore, resourcePool, vmFolder);

    }

    public boolean isConnectToVslm() {
        return this.connectToVslm;
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean isIvdByNameExist(final String name) {
        return getVslmConnection().isIvdByNameExist(name);
    }

    public boolean isVAppByNameExist(final String name) {
        final ManagedObjectReference vmMoref = getVAppByName(name);
        return vmMoref != null;
    }

    public boolean isVmByNameExist(final String name) {
        final ManagedObjectReference vmMoref = getVmByName(name);
        return vmMoref != null;
    }

    private void keepAlive() throws RuntimeFaultFaultMsg {
        final String msg = String.format("keep alive %s : Current Time %s", getServerIntanceUuid(),
                getVimPort().currentTime(getServiceInstanceReference()));
        AbstractConnection.logger.info(msg);
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
            final String msg = String.format("failed to connect to %s : token %s  not valid", vcServerUrl,
                    token.toString());
            AbstractConnection.logger.warning(msg);
            return null;
        }
    }

    private ConnectParams newConnectParamsInstance() {
        final ConnectParams connectParams = new ConnectParams();
        if (AbstractConnection.logger.isLoggable(Level.FINE)) {
            AbstractConnection.logger.fine("VDDK connection by session ID " + getCookieValue());
        }
        connectParams.setCredType(ConnectParams.VIXDISKLIB_CRED_SESSIONID);
        connectParams.setServerName(getHost());
        connectParams.setPort(getPort());
        connectParams.setNfcHostPort(getNfcHostPort());
        connectParams.setUsername(getUsername());
        connectParams.setPassword("");
        connectParams.setKey(" ");
        connectParams.setCookie(getCookieValue());
        connectParams.setThumbPrint(getThumbPrint());

        return connectParams;
    }

    /**
     * @param virtualMachineManager
     * @param key
     * @param startPosition
     * @param prevChanges
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws FileFaultFaultMsg
     */
    public DiskChangeInfo queryChangedDiskAreas(final VirtualMachineManager vmm, final int key,
            final long startPosition, final String prevChanges)
            throws FileFaultFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        return getVimPort().queryChangedDiskAreas(vmm.getMoref(), vmm.getSnapshotManager().getMoref(), key,
                startPosition, prevChanges);
    }

    public ManagedObjectReference registerVirtualMachine(final CoreRestoreManagedInfo managedInfo,
            final StorageDirectoryInfo directoryInfo) throws AlreadyExistsFaultMsg, DuplicateNameFaultMsg,
            FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidNameFaultMsg,
            InvalidStateFaultMsg, NotFoundFaultMsg, OutOfBoundsFaultMsg, RuntimeFaultFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InterruptedException, InvalidCollectorVersionFaultMsg, VimTaskException {
        final String vmxPath = String.format("[%s] %s/%s.vmx", directoryInfo.getDsInfo().getName(),
                directoryInfo.getDirectoryUuid(), managedInfo.getName());
        return registerVirtualMachine(vmxPath, managedInfo.getName(), managedInfo.getHostInfo(),
                managedInfo.getFolderInfo(), managedInfo.getResourcePollInfo());
    }

    private ManagedObjectReference registerVirtualMachine(final String vmxPath, final String vmName,
            final ManagedEntityInfo hostInfo, final ManagedEntityInfo folderInfo, final ManagedEntityInfo rpInfo)
            throws AlreadyExistsFaultMsg, DuplicateNameFaultMsg, FileFaultFaultMsg, InsufficientResourcesFaultFaultMsg,
            InvalidDatastoreFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg, NotFoundFaultMsg, OutOfBoundsFaultMsg,
            RuntimeFaultFaultMsg, VmConfigFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException,
            InvalidCollectorVersionFaultMsg, VimTaskException {
        ManagedObjectReference registeredVMRef = null;
        String msg = String.format("Registering Virtual Machine %s Name:%s", vmxPath, vmName);
        AbstractConnection.logger.info(msg);

        final ManagedObjectReference taskmor = this.vimPort.registerVMTask(folderInfo.getMoref(), vmxPath, vmName,
                false, rpInfo.getMoref(), hostInfo.getMoref());

        if (waitForTask(taskmor)) {
            registeredVMRef = (ManagedObjectReference) getVimHelper()
                    .entityProps(taskmor, new String[] { "info.result" }).get("info.result");
            msg = String.format("VM %s registered Moref:%s", vmName, registeredVMRef.getValue());
            AbstractConnection.logger.info(msg);

        } else {
            msg = String.format("Failed to register Virtual Machine %s Name:%s", vmxPath, vmName);
            AbstractConnection.logger.warning(msg);
        }
        return registeredVMRef;
    }

    public void setConnectToVslm(final boolean connectToVslm) {
        this.connectToVslm = connectToVslm;
    }

    public void setPrivateKeyFileName(final String privateKeyFileName) {
        this.privateKeyFileName = privateKeyFileName;
    }

    public void setX509CertFileName(final String x509CertFileName) {
        this.x509CertFileName = x509CertFileName;
    }

    void socket() throws NoSuchAlgorithmException, IOException, CertificateEncodingException {

        final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        final SSLSocketFactory factory = this.ssoConnection.getSslContext().getSocketFactory();
        try (final SSLSocket socket = (SSLSocket) factory.createSocket(getURL().getHost(), getURL().getPort())) {
            socket.startHandshake();
            final SSLSession session = socket.getSession();
            final Certificate[] servercerts = session.getPeerCertificates();
            for (final Certificate servercert : servercerts) {
                if (AbstractConnection.logger.isLoggable(Level.FINE)) {
                    AbstractConnection.logger.fine("-----BEGIN CERTIFICATE-----");
                    AbstractConnection.logger.fine(Base64.encodeBase64String(servercert.getEncoded()));
                    AbstractConnection.logger.fine("-----END CERTIFICATE-----");
                }
                if (this.thumbPrint == null) {
                    sha1.update(servercert.getEncoded());
                    String sha1Digest = Utility.toHexString(sha1.digest()).replace(" ", ":");
                    sha1Digest = sha1Digest.substring(0, sha1Digest.length() - 1);
                    this.thumbPrint = sha1Digest;
                    if (AbstractConnection.logger.isLoggable(Level.INFO)) {
                        AbstractConnection.logger.info("Thumb: " + this.thumbPrint);
                    }
                }
            }
        }
    }

    void startKeepAlive() {
        final Runnable keepAliveRunnuble = () -> {
            try {
                keepAlive();
                final PbmAboutInfo pbmAbout = getPbmConnection().getAboutInfo();
                if (AbstractConnection.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("keep alive %s : Name %s  Version %s", pbmAbout.getInstanceUuid(),
                            pbmAbout.getName(), pbmAbout.getVersion());
                    AbstractConnection.logger.info(msg);
                }
                getVapiService().keepAlive();
            } catch (final RuntimeFaultFaultMsg e) {
                Utility.logWarning(AbstractConnection.logger, e);
            }
        };
        final long delay = CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds() / 2;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new VmbkThreadFactory("KeepAlive", true));
        this.scheduler.scheduleAtFixedRate(keepAliveRunnuble, delay, delay, TimeUnit.MILLISECONDS);
    }

    public Object taskResult(final ManagedObjectReference morTask)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return this.vimHelper.taskResult(morTask);
    }

    HeaderHandlerResolver updateHeaderHandlerResolver(final Element token) {

        final HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
        handlerResolver.addHandler(new TimeStampHandler());
        handlerResolver.addHandler(new SamlTokenHandler(token));
        handlerResolver.addHandler(this.cookieExtractor);
        handlerResolver.addHandler(new WsSecuritySignatureAssertionHandler(this.ssoConnection.getUserCert(),

                SsoUtils.getNodeProperty(token, "ID")));
        this.vimService.setHandlerResolver(handlerResolver);
        resetTokenUpdateSessionEventTime();
        return handlerResolver;
    }

    /**
     *
     */
    HeaderHandlerResolver updatePbmHeaderHandlerResolver() {
        return this.pbmConnection.updatePbmHeaderHandlerResolver();

    }

    StubConfiguration updateVapi(final SamlToken samlToken) {
        return this.vapiService.updateSamlBearerToken(samlToken);
    }

    HeaderHandlerResolver updateVslmHeaderHandlerResolver() {
        return this.vslmConnection.updateVslmHeaderHandlerResolver();
    }

    public boolean waitForTask(final ManagedObjectReference task) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException {
        final TaskInfoState waitResult = this.vimHelper.taskWait(task, CoreGlobalSettings.getTaskMaxWaitSeconds());
        return (waitResult == TaskInfoState.SUCCESS);
    }

    public boolean waitForTask(final ManagedObjectReference task, final Integer maxWaitSeconds)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException,
            InterruptedException {
        final TaskInfoState waitResult = this.vimHelper.taskWait(task, maxWaitSeconds);
        return (waitResult == TaskInfoState.SUCCESS);
    }

    public boolean waitForTask(final ManagedObjectReference task, final RunningReport runRep)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, VimTaskException,
            InvalidCollectorVersionFaultMsg {
        final TaskInfoState waitResult = (runRep != null)
                ? this.vimHelper.taskWait(task, runRep, CoreGlobalSettings.getTaskMaxWaitSeconds())
                : this.vimHelper.taskWait(task, CoreGlobalSettings.getTaskMaxWaitSeconds());
        return (waitResult == TaskInfoState.SUCCESS);
    }

    public boolean waitForTask(final ManagedObjectReference task, final RunningReport runRep,
            final Integer maxWaitSeconds) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException,
            VimTaskException, InvalidCollectorVersionFaultMsg {
        final TaskInfoState waitResult = (runRep != null) ? this.vimHelper.taskWait(task, runRep, maxWaitSeconds)
                : this.vimHelper.taskWait(task, maxWaitSeconds);
        return (waitResult == TaskInfoState.SUCCESS);
    }

    public VimPrivilegeChecker getPrivilegeChecker() {
        return privilegeChecker;
    }

    public VimFindFco getFind() {
        return find;
    }

    /**
     * Check if vCenter has a KMS configured
     * 
     * @return true if encryption is enabled
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InterruptedException
     */
    public boolean isEncryptionEnabled() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        Boolean p = (Boolean) getVimHelper().entityProps(getServiceContent().getCryptoManager(), "enabled");
        return Boolean.TRUE.equals(p);

    }

}
