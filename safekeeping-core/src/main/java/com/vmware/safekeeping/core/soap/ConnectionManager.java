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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.CategoryTypes.CreateSpec;
import com.vmware.cis.tagging.TagModel;
import com.vmware.jvix.JVixException;
import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmFaultFaultMsg;
import com.vmware.pbm.PbmNonExistentHubsFaultMsg;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.AbstractCoreBasicConnectOptions;
import com.vmware.safekeeping.core.command.options.CoreCspConnectOptions;
import com.vmware.safekeeping.core.command.options.CorePscConnectOptions;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectVcenter;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectVcenter;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.core.SJvddk;
import com.vmware.safekeeping.core.exception.ArchiveException;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.exception.VimObjectNotExistException;
import com.vmware.safekeeping.core.exception.VimTaskException;
import com.vmware.safekeeping.core.exception.VslmTaskException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.helpers.SslUtil;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.core.type.enums.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.core.type.enums.VMwareCloudPlatforms;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.vapi.saml.exception.InvalidTokenException;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vapi.std.errors.NotFound;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.ID;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TimedoutFaultMsg;
import com.vmware.vim25.VStorageObject;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vslm.NotFoundFaultMsg;
import com.vmware.vslm.VslmFaultFaultMsg;

public class ConnectionManager {

    abstract static class AbstractConnectService extends Thread {
        protected boolean success;
        protected final VimConnection vim;

        /**
         * @param string
         */
        protected AbstractConnectService(final VimConnection vim, final String name) {
            super(name);
            this.vim = vim;
            this.success = false;
        }

        public boolean isSuccessful() {
            return this.success;
        }

        @Override
        public abstract void run();
    }

    private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
    private VimConnection defualtVcenter;
    private AbstractCoreBasicConnectOptions options;
    private ISingleSignOn ssoConnection;
    private ITarget repositoryTarget;
    private Map<String, VimConnection> vimConnections;

    private ArachneConnection arachneConnection;

    public ConnectionManager(final AbstractCoreBasicConnectOptions options) throws SafekeepingException {
        this.options = options;
        sslUtil = new SslUtil();
    }

    public ConnectionManager(final AbstractCoreBasicConnectOptions options, final ITarget repositoryTarget)
            throws SafekeepingException, SafekeepingConnectionException {
        this(options);
        if (options.connectDefaultTarget()) {
            this.repositoryTarget = repositoryTarget;
            if (this.repositoryTarget.isEnable()) {
                this.repositoryTarget.open();
            } else {
                throw new ArchiveException("Default repository \"%s\" is not active. Please configure the repository.",
                        this.repositoryTarget.getTargetType());
            }
        }
    }

    private boolean areVcentersConnected() {
        if (this.vimConnections == null) {
            return false;
        }
        boolean connected = this.vimConnections.size() > 0;

        for (final VimConnection vim : this.vimConnections.values()) {
            connected &= vim.isConnected();
        }
        return connected;
    }

    /**
     *
     */
    public void close() {
        try {
            disconnectVimConnections();
        } catch (final CoreResultActionException e) {
            Utility.logWarning(ConnectionManager.logger, e);
        }
        try {
            disconnectSsoConnections();
        } catch (final CoreResultActionException e) {
            Utility.logWarning(ConnectionManager.logger, e);
        }

    }

    public void connectSso(final CoreResultActionConnectSso racs, final String password, final boolean base64)
            throws CoreResultActionException {
        racs.start();
        try {
            initializeSso();
            ConnectionManager.logger.log(Level.INFO,
                    () -> String.format("connecting to %s...", this.ssoConnection.getEndPointUrl()));
            racs.setSsoEndPointUrl(this.ssoConnection.getEndPointUrl());

            this.ssoConnection.connect(password, base64);
            if (this.ssoConnection.isConnected()) {
                if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Connected to Platform Service Controller: %s",
                            this.ssoConnection.getEndPointUrl());
                    ConnectionManager.logger.info(msg);
                }

                racs.setConnected(true);
                racs.setToken(this.ssoConnection.getToken().getAttribute("ID"));
                racs.setSsoEndPointUrl(this.ssoConnection.getEndPointUrl());
            }
        } catch (final IOException | SafekeepingConnectionException | NoSuchAlgorithmException | InvalidKeySpecException
                | CertificateException | SafekeepingException e) {
            Utility.logWarning(ConnectionManager.logger, e);
            racs.failure(e);
        } catch (WebServiceException e) {
            Utility.logWarning(ConnectionManager.logger, e);
            racs.failure("WebService connection error: probably a certification issue");
        }
    }

    public CoreResultActionConnectSso connectSso(final String password, final boolean base64)
            throws CoreResultActionException {
        final CoreResultActionConnectSso result = new CoreResultActionConnectSso();
        try {
            connectSso(result, password, base64);
            return result;
        } finally {
            result.done();
        }
    }

    public CoreResultActionConnect connectVimConnetions() throws CoreResultActionException {
        final CoreResultActionConnect result = new CoreResultActionConnect();
        try {
            connectVimConnetions(result);
            return result;
        } finally {
            result.done();
        }
    }

    private static final Pattern COMMA_SEPARATED = Pattern.compile(",\\s*", Pattern.UNICODE_CHARACTER_CLASS);

    private String getCommonName(X509Certificate[] x509Certificates) {
        if (x509Certificates.length > 0) {
            X509Certificate c = x509Certificates[0];
            if (logger.isLoggable(Level.FINE)) {
                String st = String.format("Subject DN principal name: %s", c.getSubjectDN().getName());
                logger.fine(st);
            }
            for (String each : COMMA_SEPARATED.split(c.getSubjectDN().getName())) {
                if (each.startsWith("CN=")) {
                    String result = each.substring(3);
                    if (logger.isLoggable(Level.FINE)) {
                        String st = String.format("Common Name: %s", result);
                        logger.fine(st);
                    }

                    return result;
                }
            }

            throw new IllegalStateException("Missed CN in Subject DN: " + c.getSubjectDN());
        }
        throw new IllegalStateException("No Subject DN available");

    }

    public void connectVimConnetions(final CoreResultActionConnect rac) throws CoreResultActionException {
        if (isConnected()) {
            rac.skip("Already connected");
        } else {
            rac.start();
            boolean vmcConfiguration = false;
            try {
                if (this.ssoConnection.isConnected()) {
                    initialize();
                    rac.setConnected(true);
                    if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                        final String msg = String.format("Connected to Platform Service Controller: %s",
                                this.ssoConnection.getEndPointUrl());
                        ConnectionManager.logger.info(msg);
                    }
                    final Map<String, String> vimUrls = this.ssoConnection.findVimUrls();
                    if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                        final String msg = String.format("LookupService reports %d VimService Instance%s",
                                vimUrls.size(), vimUrls.size() > 1 ? "s" : "");
                        ConnectionManager.logger.info(msg);
                    }
                    Map<String, VMwareCloudPlatforms> platforms = new HashMap<>();
                    for (final Entry<String, String> entry : vimUrls.entrySet()) {

                        final URL destinationURL = new URL(entry.getValue());
                        final HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
                        conn.connect();
                        String cn = getCommonName((X509Certificate[]) conn.getServerCertificates());
                        conn.disconnect();
                        if (cn.endsWith("fractal.vmwarevmc.com")) {
                            platforms.put(entry.getKey(), VMwareCloudPlatforms.VMC_ON_DELL_EMC);
                        } else if (cn.endsWith(".vmwarevmc.com")) {
                            platforms.put(entry.getKey(), VMwareCloudPlatforms.VMC_ON_AWS);
                        } else if (cn.endsWith(".vmwarevmcgov.com")) {
                            platforms.put(entry.getKey(), VMwareCloudPlatforms.VMC_GOVCLOUD);
                        } else {
                            // NO VMC
                            platforms.put(entry.getKey(), VMwareCloudPlatforms.ON_PREM);
                        }
                        vmcConfiguration |= platforms.get(entry.getKey()) != VMwareCloudPlatforms.ON_PREM;
                        rac.progressIncrease(Utility.ONE_PER_CENT);
                    }
                    SJvddk.initialize(vmcConfiguration);

                    rac.progressIncrease(Utility.TEN_PER_CENT - vimUrls.size());
                    final int step = 80 / vimUrls.size();
                    for (final Entry<String, String> entry : vimUrls.entrySet()) {
                        vimConnect(entry, platforms.get(entry.getKey()), step, rac);
                    }
                } else {
                    rac.failure("Failed to connect SSO");
                }
            } catch (final com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg | IOException | JVixException e) {
                Utility.logWarning(ConnectionManager.logger, e);
                rac.failure(e);
            }

            for (final CoreResultActionConnectVcenter racv : rac.getSubActionConnectVCenters()) {
                if (racv.isAbortedOrFailed()) {
                    rac.failure(rac.getReason() + ", " + racv.getReason());
                }
            }
            if (rac.isFails()) {
                disconnectVimConnections();
                rac.setConnected(false);
            } else {
                this.ssoConnection.TokerRenewThread();
                if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                    ConnectionManager.logger.info("connected");
                }
            }

        }
    }

    public VStorageObject createIvd(final String key, final String fileNameOfDisk,
            final ManagedEntityInfo datastoreEntityInfo, final FileBackingInfoProvisioningType backingType,
            final long size, final String sbpmProfileNames) throws com.vmware.vslm.FileFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            RuntimeFaultFaultMsg, VslmTaskException, VimTaskException, InterruptedException, InvalidArgumentFaultMsg,
            com.vmware.pbm.RuntimeFaultFaultMsg, PbmFaultFaultMsg, PbmNonExistentHubsFaultMsg {
        VStorageObject result = null;
        final VimConnection vim = (StringUtils.isEmpty(key))
                ? this.vimConnections.get(datastoreEntityInfo.getServerUuid())
                : this.vimConnections.get(key);
        result = vim.getVslmConnection().createIvd(datastoreEntityInfo, fileNameOfDisk, backingType, size,
                vim.getPbmConnection().getDefinedProfileSpec(sbpmProfileNames));
        return result;
    }

    public VStorageObject createIvd(final String key, final String fileNameOfDisk, final String datastoreName,
            final FileBackingInfoProvisioningType backingType, final long size,
            final VirtualMachineDefinedProfileSpec profileSpecs) throws com.vmware.vslm.FileFaultFaultMsg,
            com.vmware.vslm.InvalidDatastoreFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg, VslmFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            RuntimeFaultFaultMsg, VslmTaskException, VimTaskException, InterruptedException, VimObjectNotExistException,
            InvalidArgumentFaultMsg, PbmFaultFaultMsg, PbmNonExistentHubsFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg {
        VStorageObject result = null;
        final VimConnection vim = (StringUtils.isEmpty(key)) ? this.defualtVcenter : this.vimConnections.get(key);
        final ManagedEntityInfo datastoreEntityInfo = vim.getManagedEntityInfo(EntityType.Datastore, datastoreName);
        result = vim.getVslmConnection().createIvd(datastoreEntityInfo, fileNameOfDisk, backingType, size,
                profileSpecs);
        return result;
    }

    public String createTag(final String key, final com.vmware.cis.tagging.TagTypes.CreateSpec spec) {
        String result = null;

        final VimConnection vim = (StringUtils.isEmpty(key)) ? this.defualtVcenter : this.vimConnections.get(key);
        result = vim.getVapiService().getTaggingService().create(spec);
        return result;
    }

    /**
     *
     * @param key
     * @param createSpec
     * @return
     * @throws Exception
     */
    public Set<String> createTagCategorySpec(final String key, final CreateSpec createSpec) {
        final HashSet<String> result = new HashSet<>();

        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result.add(vim.getVapiService().getCategoryService().create(createSpec));
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                result.add(this.vimConnections.get(key).getVapiService().getCategoryService().create(createSpec));
            }
        }
        return result;

    }

    private CoreResultActionDisconnectSso disconnectSsoConnections() throws CoreResultActionException {
        final CoreResultActionDisconnectSso result = new CoreResultActionDisconnectSso();
        try {
            disconnectSsoConnections(result);
            return result;
        } finally {
            result.done();
        }
    }

    public void disconnectSsoConnections(final CoreResultActionDisconnectSso result) throws CoreResultActionException {
        if (!isSsoConnected()) {
            result.skip("Not SSOsession");
        } else {
            result.start();
            result.setSsoEndPointUrl(this.ssoConnection.getEndPointUrl());
            result.setConnected(!this.ssoConnection.disconnect());
            this.ssoConnection = null;
        }
    }

    private void disconnectVimConnection(final CoreResultActionDisconnect result, final VimConnection vim,
            final int step) throws CoreResultActionException {
        final CoreResultActionDisconnectVcenter racv = new CoreResultActionDisconnectVcenter(result);
        racv.start();
        try {
            racv.setUrl(vim.getUrl());
            racv.setInstanceUuid(vim.getServerIntanceUuid());
            racv.setApi(vim.getAboutInfo().getApiVersion());
            racv.setName(vim.getAboutInfo().getFullName());

            racv.setVapiUrl(vim.getVapiUrl().toString());

            racv.setPbmVersion(vim.getPbmConnection().getAboutInfo().getVersion());
            racv.setPbmUrl(vim.getPbmConnection().getUrl().toString());
            if (vim.isConnectToVslm()) {
                racv.setVslmName(vim.getVslmConnection().getAboutInfo().getFullName());
                racv.setVslmUrl(vim.getVslmConnection().getURL().toString());
            }
            vim.disconnect();
            result.progressIncrease(step);
            racv.setPbmConnected(false);
            racv.setVapiConnected(false);
            racv.setVslmConnected(false);
            racv.setVimConnected(false);
        } catch (final RuntimeFaultFaultMsg e) {
            Utility.logWarning(ConnectionManager.logger, e);
            racv.failure(e);
        } finally {
            racv.done();
        }
    }

    public CoreResultActionDisconnect disconnectVimConnections() throws CoreResultActionException {
        final CoreResultActionDisconnect result = new CoreResultActionDisconnect();
        try {
            disconnectVimConnections(result);
            return result;
        } finally {
            result.done();
        }
    }

    public void disconnectVimConnections(final CoreResultActionDisconnect result) throws CoreResultActionException {
        if (!areVcentersConnected()) {
            result.skip("Not system connected");
            result.setConnected(false);
        } else {
            result.start();
            try {
                result.progressIncrease(Utility.TEN_PER_CENT);
                final int step = 80 / this.vimConnections.size();
                for (final VimConnection vim : this.vimConnections.values()) {
                    disconnectVimConnection(result, vim, step);
                    for (final CoreResultActionDisconnectVcenter radv : result.getSubActionDisconnectVCenters()) {
                        result.setConnected(result.isConnected() || radv.isVimConnected());
                    }
                    this.vimConnections = null;
                }

            } catch (final Exception e) {
                Utility.logWarning(ConnectionManager.logger, e);
                result.failure(e);
            }
        }
    }

    public ImprovedVirtualDisk findIvdById(final String key, final ID id) {
        ImprovedVirtualDisk result = null;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                final ImprovedVirtualDisk ivd = vim.getVslmConnection().getIvdById(id);
                if (ivd != null) {
                    result = ivd;
                    break;
                }
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                final ImprovedVirtualDisk ivd = this.vimConnections.get(key).getVslmConnection().getIvdById(id);
                if (ivd != null) {
                    result = ivd;
                }
            }
        }
        return result;
    }

    public ImprovedVirtualDisk findIvdById(final String key, final String uuid) {
        ImprovedVirtualDisk result = null;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                final ImprovedVirtualDisk ivd = vim.getVslmConnection().getIvdById(uuid);
                if (ivd != null) {
                    result = ivd;
                    break;
                }
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                final ImprovedVirtualDisk ivd = this.vimConnections.get(key).getVslmConnection().getIvdById(uuid);
                if (ivd != null) {
                    result = ivd;
                }
            }
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    public List<ImprovedVirtualDisk> findIvdByName(final String key, final String name) {
        final LinkedList<ImprovedVirtualDisk> result = new LinkedList<>();
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result.addAll(vim.getVslmConnection().getIvdByName(name));
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                result.addAll(this.vimConnections.get(key).getVslmConnection().getIvdByName(name));
            }
        }
        return result;
    }

    /**
     * @param vim
     * @param create
     * @return
     */
    public VirtualAppManager findVAppByMoref(final String key, final ManagedObjectReference moref) {
        VirtualAppManager result = null;
        try {
            if (StringUtils.isEmpty(key)) {
                for (final VimConnection vim : this.vimConnections.values()) {
                    result = vim.getFind().findVAppByMoref(moref);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                if (this.vimConnections.containsKey(key)) {
                    final VimConnection vim = this.vimConnections.get(key);
                    result = vim.getFind().findVAppByMoref(moref);
                }
            }
        } catch (final Exception e) {
            Utility.logWarning(ConnectionManager.logger, e);
            result = null;
        }
        return result;
    }

    /**
     * @param vim
     * @param key
     * @return
     */
    public VirtualAppManager findVAppByName(final String key, final String name) {
        VirtualAppManager result = null;
        try {
            if (StringUtils.isEmpty(key)) {
                for (final VimConnection vim : this.vimConnections.values()) {
                    result = vim.getFind().findVAppByName(name);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                if (this.vimConnections.containsKey(key)) {
                    final VimConnection vim = this.vimConnections.get(key);
                    result = vim.getFind().findVAppByName(name);
                }
            }
        } catch (final Exception e) {
            Utility.logWarning(ConnectionManager.logger, e);
            result = null;
        }
        return result;
    }

    /**
     * 
     * @param key
     * @param uuid
     * @return
     */
    public VirtualAppManager findVAppByUuid(final String key, final String uuid) {
        VirtualAppManager result = null;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result = vim.getFind().findVAppByUuid(uuid);
                if (result != null) {
                    break;
                }
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                final VimConnection vim = this.vimConnections.get(key);
                result = vim.getFind().findVAppByUuid(uuid);
            }
        }

        return result;
    }

    /**
     * Finds a virtual machine by IP address, where the IP address is in dot-decimal
     * notation. For example, 10.17.12.12. The IP address for a virtual machine is
     * the one returned from VMware tools, ipAddress.
     *
     * @param key The vCenter key. If empty search any vcenter
     * @param ip  The IP to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByIp(final String key, final String ip) {
        VirtualMachineManager result = null;
        try {

            if (StringUtils.isEmpty(key)) {
                for (final VimConnection vim : this.vimConnections.values()) {
                    result = vim.getFind().findVmByIp(ip);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                if (this.vimConnections.containsKey(key)) {
                    final VimConnection vim = this.vimConnections.get(key);
                    result = vim.getFind().findVmByIp(ip);
                }
            }
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(ConnectionManager.logger, e);
            result = null;
        } catch (final InterruptedException e) {
            ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * Finds a virtual machine by the moref.
     *
     * @param key   The vCenter key. If empty search any vcenter
     * @param moref The moref to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByMoref(final String key, final ManagedObjectReference moref) {
        VirtualMachineManager result = null;
        try {
            if (StringUtils.isEmpty(key)) {
                for (final VimConnection vim : this.vimConnections.values()) {
                    result = vim.getFind().findVmByMoref(moref);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                if (this.vimConnections.containsKey(key)) {
                    final VimConnection vim = this.vimConnections.get(key);
                    result = vim.getFind().findVmByMoref(moref);
                }
            }
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(ConnectionManager.logger, e);
            result = null;
        } catch (final InterruptedException e) {
            ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * Finds a virtual machine by name
     *
     * @param key  The vCenter key. If empty search any vcenter
     * @param name The vm name to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByName(final String key, final String name) {
        VirtualMachineManager result = null;
        try {
            if (StringUtils.isEmpty(key)) {
                for (final VimConnection vim : this.vimConnections.values()) {
                    result = vim.getFind().findVmByName(name);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                if (this.vimConnections.containsKey(key)) {
                    final VimConnection vim = this.vimConnections.get(key);
                    result = vim.getFind().findVmByName(name);
                }
            }
        } catch (final InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
            Utility.logWarning(ConnectionManager.logger, e);
            result = null;
        } catch (final InterruptedException e) {
            ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * Finds a virtual machine by BIOS or instance UUID.
     *
     * @param key          The vCenter key. If empty search any vcenter
     * @param uuid         The UUID to find.
     * @param instanceUuid If true, search for virtual machines whose instance UUID
     *                     matches the given uuid. Otherwise, search for virtual
     *                     machines whose BIOS UUID matches the given uuid.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByUuid(final String key, final String uuid, final boolean instanceUuid) {
        VirtualMachineManager result = null;
        try {

            if (StringUtils.isEmpty(key)) {
                for (final VimConnection vim : this.vimConnections.values()) {
                    result = vim.getFind().findVmByUuid(uuid, instanceUuid);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                if (this.vimConnections.containsKey(key)) {
                    final VimConnection vim = this.vimConnections.get(key);
                    result = vim.getFind().findVmByUuid(uuid, instanceUuid);
                }
            }

        } catch (final InvalidPropertyFaultMsg e) {
            Utility.logWarning(ConnectionManager.logger, e);
            result = null;
        } catch (final InterruptedException e) {
            ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return result;
    }

    public List<ImprovedVirtualDisk> getAllIvdList()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final LinkedList<ImprovedVirtualDisk> ivdList = new LinkedList<>();
        for (final VimConnection vim : this.vimConnections.values()) {
            ivdList.addAll(vim.getVslmConnection().getIvdList());
        }
        return ivdList;
    }

    public List<VirtualAppManager> getAllVAppList(final String key, final String vmFilter) {
        final LinkedList<VirtualAppManager> vmList = new LinkedList<>();
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vimConnection : this.vimConnections.values()) {
                try {
                    vmList.addAll(vimConnection.getFind().findAnyVapp(vmFilter));
                } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                    Utility.logWarning(ConnectionManager.logger, e);
                } catch (final InterruptedException e) {
                    ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            try {
                vmList.addAll(this.vimConnections.get(key).getFind().findAnyVapp(vmFilter));
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                Utility.logWarning(ConnectionManager.logger, e);
            } catch (final InterruptedException e) {
                ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
        return vmList;
    }

    public List<VirtualMachineManager> getAllVmList(final String key, final String vmFilter) {
        final List<VirtualMachineManager> vmList = new LinkedList<>();
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vimConnection : this.vimConnections.values()) {
                try {
                    vmList.addAll(vimConnection.getFind().findAnyVm(vmFilter));
                } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                    Utility.logWarning(ConnectionManager.logger, e);
                } catch (final InterruptedException e) {
                    ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            try {
                vmList.addAll(this.vimConnections.get(key).getFind().findAnyVm(vmFilter));
            } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
                Utility.logWarning(ConnectionManager.logger, e);
            } catch (final InterruptedException e) {
                ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
        return vmList;
    }

    public ArachneConnection getArachneConnection() {
        return this.arachneConnection;
    }

    public VimConnection getDefualtVcenter() {
        return this.defualtVcenter;
    }

    public ManagedEntityInfo getManagedEntityInfo(final String key, final EntityType entityType, final String name)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException, VimObjectNotExistException {

        ManagedEntityInfo result = null;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result = vim.getManagedEntityInfo(entityType, name);
                if (result != null) {
                    break;
                }
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                result = this.vimConnections.get(key).getManagedEntityInfo(entityType, name);
            }
        }
        return result;
    }

    /**
     * @return the options
     */
    public AbstractCoreBasicConnectOptions getOptions() {
        return this.options;
    }

    public ITarget getRepositoryTarget() {
        return this.repositoryTarget;
    }

    public ManagedObjectReference getSearchIndex(final String uuid) {
        return this.vimConnections.get(uuid).serviceContent.getSearchIndex();
    }

    public ServiceContent getServiceContent(final String uuid) {
        return this.vimConnections.get(uuid).serviceContent;
    }

    public ManagedObjectReference getServiceInstanceReference(final String uuid) {
        return this.vimConnections.get(uuid).getServiceInstanceReference();
    }

    public CategoryModel getTagCategory(final String key, final String categoryId) {
        CategoryModel result = null;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                try {
                    result = vim.getVapiService().getCategoryService().get(categoryId);
                    break;
                } catch (final NotFound e) {
                    final String msg = String.format("vim: %s - category %s not found", key, categoryId);
                    ConnectionManager.logger.log(Level.FINE, msg, e);
                }
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                try {
                    result = this.vimConnections.get(key).getVapiService().getCategoryService().get(categoryId);
                } catch (final NotFound e) {
                    final String msg = String.format("vim: %s - category %s not found", key, categoryId);
                    ConnectionManager.logger.log(Level.FINE, msg, e);
                }
            }
        }
        return result;
    }

    /**
     * Return the vimConnection by uuid
     *
     * @param vimUUID
     * @return
     */
    public VimConnection getVimConnection(final String vimUUID) {
        VimConnection result = null;
        if (this.vimConnections != null) {
            if (StringUtils.isEmpty(vimUUID) || !this.vimConnections.containsKey(vimUUID)) {
                return this.defualtVcenter;
            }
            result = this.vimConnections.get(vimUUID);
        }
        return result;
    }

    public Map<String, VimConnection> getVimConnections() {
        return this.vimConnections;
    }

    public ManagedFcoEntityInfo importVapp(final String key, final URL urlPath, final String vappName,
            final String host, final String datastore, final String resourcePool, final String vmFolder)
            throws KeyManagementException, NoSuchAlgorithmException, RuntimeFaultFaultMsg, TimedoutFaultMsg,
            InvalidPropertyFaultMsg, ConcurrentAccessFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidStateFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, IOException, DuplicateNameFaultMsg,
            InsufficientResourcesFaultFaultMsg, InvalidNameFaultMsg, OutOfBoundsFaultMsg,
            InvalidCollectorVersionFaultMsg, InterruptedException, SafekeepingUnsupportedObjectException,
            SafekeepingException, SafekeepingConnectionException {
        ManagedFcoEntityInfo result = null;

        final VimConnection vim = (StringUtils.isEmpty(key)) ? this.defualtVcenter : this.vimConnections.get(key);

        result = vim.importVApp(urlPath, vappName, host, datastore, resourcePool, vmFolder);
        return result;
    }

    private void initialize() {

        this.vimConnections = new LinkedHashMap<>();
        this.arachneConnection = null;
    }

    private final SslUtil sslUtil;

    private void initializeSso() throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException,
            IOException, SafekeepingException {
//keystore first choice
        SSLContext sslContext = sslUtil.getSslContext();

        if ((new File(CoreGlobalSettings.getKeystorePath()).exists())) {
            SsoKeyStoreCertificate keyStoreCert = new SsoKeyStoreCertificate(CoreGlobalSettings.getKeystorePath(),
                    CoreGlobalSettings.getKeyStorePassword(), CoreGlobalSettings.getKeyStoreUserCertificateAlias(),
                    CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds());
            if (this.options instanceof CorePscConnectOptions) {
                final CorePscConnectOptions pscOptions = (CorePscConnectOptions) this.options;
                this.ssoConnection = new PscProvider(sslContext, pscOptions.getAuthServer(), pscOptions.getPort(),
                        pscOptions.getNfcHostPort(), pscOptions.getUser(), keyStoreCert);
            } else if (this.options instanceof CoreCspConnectOptions) {
                final CoreCspConnectOptions cspOptions = (CoreCspConnectOptions) this.options;
                this.ssoConnection = new CspProvider(sslContext, cspOptions.getAuthServer(),
                        cspOptions.getTokenExchangeServer(), cspOptions.getNfcHostPort(), keyStoreCert);
            } else {
                throw new SafekeepingException("Unsupported connection type: %s", this.options.getClass().toString());
            }
        } else if ((StringUtils.isEmpty(CoreGlobalSettings.getX509CertFile())
                || StringUtils.isEmpty(CoreGlobalSettings.getX509CertPrivateKeyFile()))
                || (!(new File(CoreGlobalSettings.getX509CertFile()).exists())
                        || !(new File(CoreGlobalSettings.getX509CertPrivateKeyFile()).exists()))) {
            if (this.options instanceof CorePscConnectOptions) {
                final CorePscConnectOptions pscOptions = (CorePscConnectOptions) this.options;
                this.ssoConnection = new PscProvider(sslContext, pscOptions.getAuthServer(), pscOptions.getPort(),
                        pscOptions.getNfcHostPort(), pscOptions.getUser(),
                        CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds());
            } else if (this.options instanceof CoreCspConnectOptions) {
                final CoreCspConnectOptions cspOptions = (CoreCspConnectOptions) this.options;
                this.ssoConnection = new CspProvider(sslContext, cspOptions.getAuthServer(),
                        cspOptions.getTokenExchangeServer(), cspOptions.getNfcHostPort(),
                        CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds());
            } else {
                throw new SafekeepingException("Unsupported connection type: %s", this.options.getClass().toString());
            }
        } else {
            SsoX509CertFile certFile = new SsoX509CertFile(CoreGlobalSettings.getX509CertFile(),
                    CoreGlobalSettings.getX509CertPrivateKeyFile(),
                    CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds());
            if (this.options instanceof CorePscConnectOptions) {
                final CorePscConnectOptions pscOptions = (CorePscConnectOptions) this.options;
                this.ssoConnection = new PscProvider(sslContext, pscOptions.getAuthServer(), pscOptions.getPort(),
                        pscOptions.getNfcHostPort(), pscOptions.getUser(), certFile);
            } else if (this.options instanceof CoreCspConnectOptions) {
                final CoreCspConnectOptions cspOptions = (CoreCspConnectOptions) this.options;
                this.ssoConnection = new CspProvider(sslContext, cspOptions.getAuthServer(),
                        cspOptions.getTokenExchangeServer(), cspOptions.getNfcHostPort(), certFile);
            } else {
                throw new SafekeepingException("Unsupported connection type: %s", this.options.getClass().toString());
            }
        }

    }

    public boolean isConnected() {
        if ((this.ssoConnection != null) && this.ssoConnection.isConnected()) {
            return areVcentersConnected();
        }

        return false;
    }

    public boolean isSsoConnected() {
        if (this.ssoConnection == null) {
            return false;
        }
        return this.ssoConnection.isConnected();
    }

    public List<DynamicID> listTagAttachedObjects(final String key, final String id) {
        List<DynamicID> result = null;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                try {
                    result = vim.getVapiService().getTagAssociation().listAttachedObjects(id);
                    break;
                } catch (final NotFound e) {
                    ConnectionManager.logger.warning(String.format("vim: %s - category %s not found", key, id));
                    Utility.logWarning(ConnectionManager.logger, e);
                } catch (final Exception e) {
                    Utility.logWarning(ConnectionManager.logger, e);
                }
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                try {
                    result = this.vimConnections.get(key).getVapiService().getTagAssociation().listAttachedObjects(id);
                } catch (final NotFound e) {
                    ConnectionManager.logger.warning(String.format("vim: %s - category %s not found", key, id));
                    Utility.logWarning(ConnectionManager.logger, e);
                } catch (final Exception e) {
                    Utility.logWarning(ConnectionManager.logger, e);
                }
            }
        }
        return result;
    }

    public List<CategoryModel> listTagCategories(final String key, final Set<String> set) {
        final LinkedList<CategoryModel> result = new LinkedList<>();
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result.addAll(vim.getVapiService().tagCategoriesList(set));
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                result.addAll(this.vimConnections.get(key).getVapiService().tagCategoriesList(set));
            }
        }
        return result;
    }

    public List<TagModel> listTags(final String key, final Set<String> set) {
        final LinkedList<TagModel> result = new LinkedList<>();
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result.addAll(vim.getVapiService().tagsList(set));
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                result.addAll(this.vimConnections.get(key).getVapiService().tagsList(set));
            }
        }
        return result;
    }

    /**
     * Reconcile Datastore
     *
     * @param key
     * @param datastoreInfo
     * @return
     * @throws VslmTaskException
     * @throws InterruptedException
     * @throws VimTaskException
     * @throws InvalidCollectorVersionFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws com.vmware.vim25.NotFoundFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws VslmFaultFaultMsg
     * @throws com.vmware.vslm.RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     * @throws com.vmware.vslm.InvalidDatastoreFaultMsg
     * @throws Exception
     */
    public boolean reconcileDatastore(final String key, final ManagedEntityInfo datastoreInfo)
            throws com.vmware.vslm.InvalidDatastoreFaultMsg, NotFoundFaultMsg, com.vmware.vslm.RuntimeFaultFaultMsg,
            VslmFaultFaultMsg, InvalidDatastoreFaultMsg, com.vmware.vim25.NotFoundFaultMsg, RuntimeFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, VimTaskException, InterruptedException,
            VslmTaskException {
        boolean result = false;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                result |= vim.getVslmConnection().reconcileDatastore(datastoreInfo);
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                result = this.vimConnections.get(key).getVslmConnection().reconcileDatastore(datastoreInfo);
            }
        }
        return result;
    }

    public boolean removeTag(final String key, final String tagId) throws NotFound {
        boolean result = false;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                vim.getVapiService().getTaggingService().delete(tagId);
                if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                    ConnectionManager.logger.info("Tag  " + tagId + " deleted");
                }
                result = true;

            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                final VimConnection vim = this.vimConnections.get(key);
                vim.getVapiService().getTaggingService().delete(tagId);
                if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                    ConnectionManager.logger.info("Tag  " + tagId + " deleted");
                }
                result = true;
            }
        }
        return result;
    }

    public boolean removeTagCategorySpec(final String key, final String categoryName) {
        final boolean result = true;
        if (StringUtils.isEmpty(key)) {
            for (final VimConnection vim : this.vimConnections.values()) {
                final CategoryModel categoryId = vim.getVapiService().getTagCategoryByName(categoryName);
                vim.getVapiService().getCategoryService().delete(categoryId.getId());
            }
        } else {
            if (this.vimConnections.containsKey(key)) {
                final VimConnection vim = this.vimConnections.get(key);
                final CategoryModel categoryId = vim.getVapiService().getTagCategoryByName(categoryName);
                vim.getVapiService().getCategoryService().delete(categoryId.getId());
            }
        }
        return result;
    }

    public List<VStorageObjectAssociations> retrieveVStorageObjectAssociations()
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InterruptedException {
        final LinkedList<VStorageObjectAssociations> ivdList = new LinkedList<>();
        for (final VimConnection vim : this.vimConnections.values()) {
            ivdList.addAll(vim.getVslmConnection().retrieveVStorageObjectAssociations());
        }
        return ivdList;

    }

    /**
     * @param options the options to set
     */
    public void setOptions(final CorePscConnectOptions options) {
        this.options = options;
    }

    public void setRepositoryTarget(final ITarget repositoryTarget) {
        this.repositoryTarget = repositoryTarget;
    }

    private void vimConnect(final Entry<String, String> entry, VMwareCloudPlatforms cloudPlatform, final int step,
            final CoreResultActionConnect rac) throws CoreResultActionException {
        final CoreResultActionConnectVcenter racv = new CoreResultActionConnectVcenter(rac);
        VimConnection vim = null;

        racv.start();
        racv.setUrl(entry.getValue());
        racv.setInstanceUuid(entry.getKey());
        racv.setCloudPlatform(cloudPlatform);
        try {
            vim = new VimConnection(this.ssoConnection, entry.getKey(), entry.getValue(), cloudPlatform,
                    getOptions().isConnectVslm());
            vim.connect(this.ssoConnection.getToken());
            rac.progressIncrease(step / 2F);
            racv.progressIncrease(Utility.FIFTY_PER_CENT);
            racv.setApi(vim.getAboutInfo().getApiVersion());
            racv.setName(vim.getAboutInfo().getFullName());

            racv.setVapiUrl(vim.getVapiUrl().toString());
            if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                String msg = String.format("Connected to VimService uuid: %s url: %s", vim.getServerIntanceUuid(),
                        vim.getUrl());
                ConnectionManager.logger.info(msg);
                msg = String.format("\t%s - Api Version: %s - Platform: %s", vim.getAboutInfo().getFullName(),
                        vim.getAboutInfo().getApiVersion(), cloudPlatform.toString());
                ConnectionManager.logger.info(msg);
            }

            final AbstractConnectService connectPbm = new AbstractConnectService(vim, "connectPbm") {
                @Override
                public void run() {
                    try {
                        this.vim.connectPbm();
                        if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                            final String msg = String.format("Connected to %s", this.vim.getPbmConnection().toString());
                            ConnectionManager.logger.info(msg);
                        }
                        this.success = true;
                        rac.progressIncrease(step / 6F);
                        racv.progressIncrease(10);
                    } catch (final IOException e) {
                        Utility.logWarning(ConnectionManager.logger, e);
                        this.success = false;
                    }
                }
            };

            final AbstractConnectService connectVslm = new AbstractConnectService(vim, "connectVslm") {
                @Override
                public void run() {
                    try {
                        if (this.vim.connectVslm() != null) {
                            if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                                final String msg = String.format("Connected to %s",
                                        this.vim.getVslmConnection().toString());
                                ConnectionManager.logger.info(msg);
                            }
                            this.success = true;
                            rac.progressIncrease(step / 6F);
                            racv.progressIncrease(10);
                        }
                    } catch (final IOException e) {
                        Utility.logWarning(ConnectionManager.logger, e);
                        this.success = false;
                    }
                }
            };

            final AbstractConnectService connectVapi = new AbstractConnectService(vim, "connectVapi") {
                @Override
                public void run() {
                    try {
                        this.vim.connectVapi(ConnectionManager.this.ssoConnection.getSamlBearerToken());
                        if (ConnectionManager.logger.isLoggable(Level.INFO)) {
                            final String msg = String.format("Connected to Vapi Service url: %s",
                                    this.vim.getVapiUrl());
                            ConnectionManager.logger.info(msg);
                        }
                        this.success = true;
                        rac.progressIncrease(step / 6F);
                        racv.progressIncrease(10);
                    } catch (final InvalidTokenException e) {
                        Utility.logWarning(ConnectionManager.logger, e);
                        this.success = false;
                    }
                }
            };
            racv.setVmFolderFilter(CoreGlobalSettings.getVmFilter());
            racv.setResourcePoolFilter(CoreGlobalSettings.getRpFilter());
            connectVapi.start();
            connectVslm.start();
            connectPbm.start();

            connectPbm.join();
            connectVapi.join();
            connectVslm.join();
            racv.setPbmConnected(connectPbm.isSuccessful());
            racv.setVapiConnected(connectVapi.isSuccessful());
            racv.setVslmConnected(connectVslm.isSuccessful());
            if (connectPbm.isSuccessful()) {
                racv.setPbmVersion(vim.getPbmConnection().getAboutInfo().getVersion());
                racv.setPbmUrl(vim.getPbmConnection().getUrl().toString());
            }

            if (connectVslm.isSuccessful()) {
                racv.setUseVslm(vim.getVslmConnection().isUseVslm());
                if (vim.getVslmConnection().isUseVslm()) {
                    racv.setVslmName(vim.getVslmConnection().getAboutInfo().getFullName());
                    racv.setVslmUrl(vim.getVslmConnection().getURL().toString());
                }
            }

            if (connectPbm.isSuccessful() && connectVslm.isSuccessful() && connectVapi.isSuccessful()) {

                vim.startKeepAlive();
                if (this.defualtVcenter == null) {
                    this.defualtVcenter = vim;
                }
                this.vimConnections.put(vim.getServerIntanceUuid(), vim);

            } else {
                final String msg = String.format("Connection failure:%s %s %s",
                        (!connectPbm.isSuccessful()) ? "Storage Profile connection error." : "",
                        (!connectVslm.isSuccessful()) ? "VSLM connection error." : "",
                        (!connectVapi.isSuccessful()) ? "VAPI connection error." : "");
                ConnectionManager.logger.warning(msg);
                racv.failure(msg);
                vim.disconnect();
            }

        } catch (final IOException | com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg
                | CertificateEncodingException | NoSuchAlgorithmException | RuntimeFaultFaultMsg | InvalidLocaleFaultMsg
                | InvalidLoginFaultMsg | DatatypeConfigurationException | com.vmware.vim25.NotFoundFaultMsg
                | InvalidPropertyFaultMsg e) {
            racv.failure(e);
            Utility.logWarning(ConnectionManager.logger, e);
            if (vim != null) {
                try {
                    vim.disconnect();
                } catch (final RuntimeFaultFaultMsg e1) {
                    Utility.logWarning(ConnectionManager.logger, e1);
                }
            }
        } catch (final InterruptedException e) {
            racv.failure(e);
            ConnectionManager.logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            racv.done();
        }
    }

}
