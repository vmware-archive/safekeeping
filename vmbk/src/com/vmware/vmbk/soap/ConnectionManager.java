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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.CategoryTypes.CreateSpec;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vim25.BaseConfigInfoDiskFileBackingInfoProvisioningType;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VStorageObjectAssociations;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Jvddk;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

public class ConnectionManager {

    private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());

    private VimConnection defualtVcenter;
    private javax.net.ssl.SSLContext sslContext;
    private Sso ssoConnection;

    private Map<String, VimConnection> vimConnections;
    private final ArachneConnection arachneConnection;

    public ConnectionManager() throws Exception {

	if (GlobalConfiguration.acceptUntrustedCertificate()) {
	    SSLException();
	}
	switch (GlobalConfiguration.getAuthenticationProvider()) {
	case GlobalConfiguration._G_PSC_PROVIDER:
	    this.ssoConnection = new PscProvider(this.sslContext, GlobalConfiguration.getSsoServer(),
		    GlobalConfiguration.getUsername(), GlobalConfiguration.getPassword(),
		    GlobalConfiguration.getX509CertFile(), GlobalConfiguration.getX509CertPrivateKeyFile(),
		    GlobalConfiguration.getTicketLifeExpectancyInMilliSeconds());
	    break;
	case GlobalConfiguration._G_CSP_PROVIDER:
	    this.ssoConnection = new CspProvider(this.sslContext, GlobalConfiguration.getCspHost(),
		    GlobalConfiguration.getVapiServer(), GlobalConfiguration.getRefreshToken(),
		    GlobalConfiguration.getX509CertFile(), GlobalConfiguration.getX509CertPrivateKeyFile(),
		    GlobalConfiguration.getTicketLifeExpectancyInMilliSeconds());
	    break;
	default:
	    throw new Exception("No valid provider specified");
	}

	this.vimConnections = new LinkedHashMap<>();
	Jvddk.initialize();
	if (StringUtils.isNotEmpty(GlobalConfiguration.getArachniServer())) {
	    this.arachneConnection = new ArachneConnection(this, GlobalConfiguration.getArachniServer(),
		    GlobalConfiguration.getArachniServerPort());
	} else {
	    this.arachneConnection = null;
	}
    }

    public void close() {
	disconnect();
	Jvddk.exit();
	this.vimConnections = null;
	this.ssoConnection = null;

    }

    public boolean connect() {
	boolean result = false;
	if (isConnected()) {
	    result = true;
	} else {
	    logger.info(String.format("connecting to %s...", this.ssoConnection.getEndPointUrl()));

	    try {
		IoFunction.showInfo(logger, "Connecting...");
		this.ssoConnection.connect();
		if (this.ssoConnection.isConnected()) {
		    IoFunction.showInfo(logger, "Connected to Platform Service Controller: %s",
			    this.ssoConnection.getEndPointUrl());

		    final Map<String, String> vimUrls = this.ssoConnection.findVimUrls();
		    IoFunction.showInfo(logger, "LookupService reports %d VimService Instance%s", vimUrls.size(),
			    vimUrls.size() > 1 ? "s" : "");
		    for (final String uuid : vimUrls.keySet()) {
			final VimConnection vim = new VimConnection(this.ssoConnection, uuid, vimUrls.get(uuid));
			vim.connect(this.ssoConnection.getToken());

			IoFunction.showInfo(logger, "Connected to VimService uuid: %s url: %s",
				vim.getServerIntanceUuid(), vim.getUrl());
			IoFunction.showInfo(logger, "\t%s - Api Version: %s", vim.getAboutInfo().getFullName(),
				vim.getAboutInfo().getApiVersion());
			vim.connectPbm();
			IoFunction.showInfo(logger, "Connected to %s", vim.getPbmConnection().toString());

			if (vim.connectVslm() != null) {
			    IoFunction.showInfo(logger, "Connected to %s", vim.getVslmConnection().toString());
			}

			vim.connectVapi(this.ssoConnection.getSamlBearerToken());
			IoFunction.showInfo(logger, "Connected to Vapi Service url: %s", vim.getVapiUrl());
			vim.startKeepAlive();
			if (this.defualtVcenter == null) {
			    this.defualtVcenter = vim;
			}
			this.vimConnections.put(vim.getServerIntanceUuid(), vim);

			this.ssoConnection.TokerRenewThread(this.vimConnections);
		    }
		}
	    } catch (final com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg | MalformedURLException
		    | ConnectException e) {
		IoFunction.showWarning(logger, Utility.toString(e));
		IoFunction.showWarning(logger, "No valid connection available. Exiting now.");
		result = false;

	    }
	    if (this.arachneConnection != null) {
		try {
		    this.arachneConnection.connect(this.ssoConnection.getToken());
		} catch (final ConnectException e) {
		    IoFunction.showWarning(logger, Utility.toString(e));
		    result = false;
		}
	    }
	    logger.info("connected");
	}
	logger.exiting(getClass().getName(), "connect", result);
	return result;

    }

    public String createTag(final String key, final com.vmware.cis.tagging.TagTypes.CreateSpec spec) {
	logger.entering(getClass().getName(), "createTag", key);
	String result = null;
	try {
	    final VimConnection vim = (key.isEmpty()) ? this.defualtVcenter : this.vimConnections.get(key);
	    result = vim.getVapiService().getTaggingService().create(spec);

	    IoFunction.showInfo(logger, "Tag created; Id: %s", result);
	} catch (final com.vmware.vapi.std.errors.AlreadyExists e) {
	    IoFunction.showWarning(logger, "Tag already exist");
	}
	logger.exiting(getClass().getName(), "createTag", result);
	return result;
    }

    public HashSet<String> createTagCategorySpec(final String key, final CreateSpec createSpec) {
	logger.entering(getClass().getName(), "createTagCategorySpec", key);
	final HashSet<String> result = new HashSet<>();

	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		result.add(vim.getVapiService().getCategoryService().create(createSpec));
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		result.add(this.vimConnections.get(key).getVapiService().getCategoryService().create(createSpec));
	    }
	}
	final String[] arrayString = result.toArray(new String[result.size()]);
	IoFunction.showInfo(logger, "Tag category created; Id: %s ", StringUtils.join(arrayString, ","));
	logger.exiting(getClass().getName(), "createTagCategorySpec", result);
	return result;

    }

    private boolean disconnect() {
	if (this.vimConnections != null) {
	    for (final VimConnection vc : this.vimConnections.values()) {
		vc.disconnect();
	    }
	}
	if (this.ssoConnection != null) {
	    this.ssoConnection.disconnect();
	}
	return true;
    }

    /**
     * @param vim
     * @param create
     * @return
     */
    public VirtualAppManager findVAppByMoref(final String key, final ManagedObjectReference moref) {
	logger.entering(getClass().getName(), "findVAppByMoref", new Object[] { key, moref });
	VirtualAppManager result = null;
	try {
	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVAppByMoref(moref);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVAppByMoref(moref);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVAppByMoref", result);
	return result;
    }

    /**
     * @param vim
     * @param key
     * @return
     */
    public VirtualAppManager findVAppByName(final String key, final String name) {
	logger.entering(getClass().getName(), "findVAppByName", new Object[] { key, name });
	VirtualAppManager result = null;
	try {
	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVAppByName(name);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVAppByName(name);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVAppByName", result);
	return result;
    }

    /**
     * @param vim
     * @param key
     * @return
     */
    public VirtualAppManager findVAppByUuid(final String key, final String uuid) {
	logger.entering(getClass().getName(), "findVAppByUuid", new Object[] { key, uuid });
	VirtualAppManager result = null;
	try {
	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVAppByUuid(uuid);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVAppByUuid(uuid);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVAppByUuid", result);
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
	logger.entering(getClass().getName(), "findVmByIp", new Object[] { key, ip });
	VirtualMachineManager result = null;
	try {

	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVmByIp(ip);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVmByIp(ip);
		}
	    }

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVmByIp", result);
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
	logger.entering(getClass().getName(), "findVmByMoref", new Object[] { key, moref });
	VirtualMachineManager result = null;
	try {
	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVmByMoref(moref);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVmByMoref(moref);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVmByMoref", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public String getCookie(final String uuid) {
// 	return this.vimConnections.get(uuid).getCookie();
//     }

// TODO Remove unused code found by UCDetector
//     public String getCookieValue(final String uuid) {
// 	return this.vimConnections.get(uuid).getCookieValue();
//     }

    /**
     * Finds a virtual machine by name
     *
     * @param key  The vCenter key. If empty search any vcenter
     * @param name The vm name to find.
     * @return The virtual machine entity that is found. If no managed entities are
     *         found, null is returned.
     */
    public VirtualMachineManager findVmByName(final String key, final String name) {
	logger.entering(getClass().getName(), "findVmByName", new Object[] { key, name });
	VirtualMachineManager result = null;
	try {
	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVmByName(name);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVmByName(name);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVmByName", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public com.vmware.vmbk.soap.helpers.MorefUtil getMOREFs(final String vimUUID) {
// 	return this.vimConnections.get(vimUUID).morefHelper;
//
//     }

// TODO Remove unused code found by UCDetector
//     public ManagedObjectReference getSearchIndex(final String uuid) {
// 	return this.vimConnections.get(uuid).serviceContent.getSearchIndex();
//     }

// TODO Remove unused code found by UCDetector
//     public ServiceContent getServiceContent(final String uuid) {
// 	return this.vimConnections.get(uuid).serviceContent;
//     }

// TODO Remove unused code found by UCDetector
//     public ManagedObjectReference getServiceInstanceReference(final String uuid) {
// 	return this.vimConnections.get(uuid).getServiceInstanceReference();
//     }

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
	logger.entering(getClass().getName(), "findVmByUuid", new Object[] { key, uuid });
	VirtualMachineManager result = null;
	try {

	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    result = vim.findVmByUuid(uuid, instanceUuid);
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    result = vim.findVmByUuid(uuid, instanceUuid);
		}
	    }

	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = null;
	}
	logger.exiting(getClass().getName(), "findVmByUuid", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public String getUrl(final String vimUUID) {
// 	return this.vimConnections.get(vimUUID).getUrl();
//     }

// TODO Remove unused code found by UCDetector
//     public URL getURL(final String vimUUID) {
// 	return this.vimConnections.get(vimUUID).getURL();
//     }

// TODO Remove unused code found by UCDetector
//     public UserSession getUserSession(final String uuid) {
// 	return this.vimConnections.get(uuid).getUserSession();
//     }

    public LinkedList<ImprovedVirtuaDisk> getAllIvdList() {
	final LinkedList<ImprovedVirtuaDisk> ivdList = new LinkedList<>();
	for (final VimConnection vim : this.vimConnections.values()) {
	    ivdList.addAll(vim.getVslmConnection().getIvdList());
	}
	return ivdList;
    }

    public LinkedList<VirtualAppManager> getAllVAppList(final String key, final String vmFilter) {
	final LinkedList<VirtualAppManager> vmList = new LinkedList<>();
	if (StringUtils.isEmpty(key)) {
	    for (final VimConnection vimConnection : this.vimConnections.values()) {
		try {
		    vmList.addAll(vimConnection.getAllVAppList(vmFilter));
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));
		}
	    }
	} else {
	    try {
		vmList.addAll(this.vimConnections.get(key).getAllVAppList(vmFilter));
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
	    }
	}
	return vmList;
    }

// TODO Remove unused code found by UCDetector
//     public VimPortType getVimPort(final String vimUUID) {
// 	return this.vimConnections.get(vimUUID).vimPort;
//     }
    public LinkedList<VirtualMachineManager> getAllVmList(final String key, final String vmFilter) {
	final LinkedList<VirtualMachineManager> vmList = new LinkedList<>();
	if (StringUtils.isEmpty(key)) {
	    for (final VimConnection vimConnection : this.vimConnections.values()) {
		try {
		    vmList.addAll(vimConnection.getAllVmList(vmFilter));
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		    logger.warning(Utility.toString(e));
		}
	    }
	} else {
	    try {
		vmList.addAll(this.vimConnections.get(key).getAllVmList(vmFilter));
	    } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
		logger.warning(Utility.toString(e));
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

    public ImprovedVirtuaDisk getIvdById(final String key, final String uuid) {
	logger.entering(getClass().getName(), "getIvdById", new Object[] { key, uuid });
	ImprovedVirtuaDisk result = null;
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		final ImprovedVirtuaDisk ivd = vim.getVslmConnection().getIvdById(uuid);
		if (ivd != null) {
		    result = ivd;
		    break;
		}
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		final ImprovedVirtuaDisk ivd = this.vimConnections.get(key).getVslmConnection().getIvdById(uuid);
		if (ivd != null) {
		    result = ivd;
		}
	    }
	}
	logger.exiting(getClass().getName(), "getIvdById", result);
	return result;
    }

    /**
     * @param key
     * @return
     */
    public List<ImprovedVirtuaDisk> getIvdByName(final String key, final String name) {
	logger.entering(getClass().getName(), "getIvdByName", new Object[] { key, name });
	final LinkedList<ImprovedVirtuaDisk> result = new LinkedList<>();
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		final ImprovedVirtuaDisk ivd = vim.getVslmConnection().getIvdByName(name);
		if (ivd != null) {
		    result.add(vim.getVslmConnection().getIvdByName(name));
		}

	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		final ImprovedVirtuaDisk ivd = this.vimConnections.get(key).getVslmConnection().getIvdByName(name);
		if (ivd != null) {
		    result.add(this.vimConnections.get(key).getVslmConnection().getIvdByName(name));
		}
	    }
	}
	logger.exiting(getClass().getName(), "getIvdByName", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public LinkedList<CategoryModel> listTagCategories(final String key, final String name) {
// 	logger.entering(getClass().getName(), "listTags", new Object[] { key, name });
// 	final HashSet<String> categoryId = new HashSet<>();
// 	categoryId.add(name);
// 	final LinkedList<CategoryModel> result = listTagCategories(key, categoryId);
// 	logger.exiting(getClass().getName(), "listTags", result);
// 	return result;
//     }

    public CategoryModel getTagCategory(final String key, final String categoryId) {
	logger.entering(getClass().getName(), "getTagCategory", new Object[] { key, categoryId });
	CategoryModel result = null;
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		try {
		    result = vim.getVapiService().getCategoryService().get(categoryId);
		    break;
		} catch (final com.vmware.vapi.std.errors.NotFound e) {
		    logger.fine(String.format("vim: %s - category %s not found", key, categoryId));
		}
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		result = this.vimConnections.get(key).getVapiService().getCategoryService().get(categoryId);
	    }
	}
	logger.exiting(getClass().getName(), "getTagCategory", result);
	return result;
    }

    public VimConnection getVimConnection(final String vimUUID) {
	if (StringUtils.isEmpty(vimUUID)) {
	    return this.defualtVcenter;
	}
	return this.vimConnections.get(vimUUID);
    }

    public Map<String, VimConnection> getVimConnections() {
	return this.vimConnections;
    }

    public boolean importVapp(final String key, final URL urlPath, final String vappName, final String host,
	    final String datastore, final String resourcePool, final String vmFolder) {
	logger.entering(getClass().getName(), "getIvdById",
		new Object[] { key, vappName, urlPath, host, datastore, resourcePool, vmFolder });
	final boolean result = false;

	final VimConnection vim = (key.isEmpty()) ? this.defualtVcenter : this.vimConnections.get(key);

	vim.importVApp(urlPath, vappName, host, datastore, resourcePool, vmFolder);
	logger.exiting(getClass().getName(), "getIvdById", result);
	return result;
    }

    public boolean isConnected() {
	return this.ssoConnection.isConnected();
    }

    public boolean ivd_create(final String key, final String fileNameOfDisk, final String datastoreName,
	    final BaseConfigInfoDiskFileBackingInfoProvisioningType backingType, final long size,
	    final LinkedList<String> sbpmProfileNames) {
	logger.entering(getClass().getName(), "ivd_create", new Object[] { key, fileNameOfDisk, backingType, size });
	boolean result = false;
	final List<VirtualMachineDefinedProfileSpec> ProfileSpecs = new LinkedList<>();

	final VimConnection vim = (key.isEmpty()) ? this.defualtVcenter : this.vimConnections.get(key);

	ProfileSpecs.addAll(vim.getPbmConnection().getDefinedProfileSpec(sbpmProfileNames));

	final ManagedEntityInfo datastoreEntityInfo = vim.getManagedEntityInfo(EntityType.Datastore, datastoreName);
	result = vim.getVslmConnection().ivd_create(fileNameOfDisk, datastoreEntityInfo, backingType, size,
		ProfileSpecs) != null;
	logger.exiting(getClass().getName(), "ivd_create", result);
	return result;
    }

    public boolean ivd_create(final String key, final String fileNameOfDisk, final String datastoreName,
	    final BaseConfigInfoDiskFileBackingInfoProvisioningType backingType, final long size,
	    final List<VirtualMachineDefinedProfileSpec> ProfileSpecs) {
	logger.entering(getClass().getName(), "ivd_create", new Object[] { key, fileNameOfDisk, backingType, size });
	boolean result = false;
	final VimConnection vim = (key.isEmpty()) ? this.defualtVcenter : this.vimConnections.get(key);
	final ManagedEntityInfo datastoreEntityInfo = vim.getManagedEntityInfo(EntityType.Datastore, datastoreName);
	result = vim.getVslmConnection().ivd_create(fileNameOfDisk, datastoreEntityInfo, backingType, size,
		ProfileSpecs) != null;
	logger.exiting(getClass().getName(), "ivd_create", result);
	return result;
    }

    public boolean ivd_ReconcileDatastore(final String key, final String datastoreName) {
	logger.entering(getClass().getName(), "ivd_ReconcileDatastore", new Object[] { key, datastoreName });
	boolean result = false;
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		result |= vim.getVslmConnection().ivd_ReconcileDatastore(datastoreName);
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		result = this.vimConnections.get(key).getVslmConnection().ivd_ReconcileDatastore(datastoreName);
	    }
	}
	logger.exiting(getClass().getName(), "ivd_ReconcileDatastore", result);
	return result;
    }

    public List<DynamicID> listTagAttachedObjects(final String key, final String id) {
	logger.entering(getClass().getName(), "listTagAttachedObjects", new Object[] { key, id });
	List<DynamicID> result = null;
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		try {
		    result = vim.getVapiService().getTagAssociation().listAttachedObjects(id);
		    break;
		} catch (final com.vmware.vapi.std.errors.NotFound e) {
		    logger.fine(String.format("vim: %s - category %s not found", key, id));
		} catch (final Exception e) {
		    logger.warning(e.getMessage());
		    IoFunction.showWarning(logger, "vim: %s - category %s Error", key, id);
		}
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		result = this.vimConnections.get(key).getVapiService().getTagAssociation().listAttachedObjects(id);
	    }
	}
	logger.exiting(getClass().getName(), "listTagAttachedObjects", result);
	return result;
    }

    public LinkedList<CategoryModel> listTagCategories(final String key, final Set<String> set) {
	logger.entering(getClass().getName(), "listTags", new Object[] { key, set });
	final LinkedList<CategoryModel> result = new LinkedList<>();
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		result.addAll(vim.getVapiService().tagCategoriesList(set));
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		result.addAll(this.vimConnections.get(key).getVapiService().tagCategoriesList(set));
	    }
	}
	logger.exiting(getClass().getName(), "listTags", result);
	return result;
    }

    public LinkedList<TagModel> listTags(final String key, final Set<String> set) {
	logger.entering(getClass().getName(), "listTags", new Object[] { key, set });
	final LinkedList<TagModel> result = new LinkedList<>();
	if (key.isEmpty()) {
	    for (final VimConnection vim : this.vimConnections.values()) {
		result.addAll(vim.getVapiService().tagsList(set));
	    }
	} else {
	    if (this.vimConnections.containsKey(key)) {
		result.addAll(this.vimConnections.get(key).getVapiService().tagsList(set));
	    }
	}
	logger.exiting(getClass().getName(), "listTags", result);
	return result;
    }

    public boolean removeTag(final String key, final HashSet<String> tagId) {
	logger.entering(getClass().getName(), "removeTag", key);
	boolean result = true;
	try {

	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    for (final String t : tagId) {
			vim.getVapiService().getTaggingService().delete(t);
			logger.info("Tag  " + t + " deleted");
		    }
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    for (final String t : tagId) {

			vim.getVapiService().getTaggingService().delete(t);

			logger.info("Tag  " + t + " deleted");
		    }
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "removeTag", result);
	return result;
    }

    public boolean removeTagCategorySpec(final String key, final String categoryName) {
	logger.entering(getClass().getName(), "removeTagCategorySpec", key);
	boolean result = true;
	try {
	    final HashSet<String> category = new HashSet<>();
	    category.add(categoryName);
	    if (key.isEmpty()) {
		for (final VimConnection vim : this.vimConnections.values()) {
		    final LinkedList<CategoryModel> categoryId = vim.getVapiService().tagCategoriesList(category);
		    vim.getVapiService().getCategoryService().delete(categoryId.get(0).getId());
		}
	    } else {
		if (this.vimConnections.containsKey(key)) {
		    final VimConnection vim = this.vimConnections.get(key);
		    final LinkedList<CategoryModel> categoryId = vim.getVapiService().tagCategoriesList(category);
		    vim.getVapiService().getCategoryService().delete(categoryId.get(0).getId());
		    IoFunction.showInfo(logger, "Tag category %s (id:%s)created", categoryName,
			    categoryId.get(0).getId());
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "removeTagCategorySpec", result);
	return result;
    }

    public LinkedList<VStorageObjectAssociations> retrieveVStorageObjectAssociations() {
	final LinkedList<VStorageObjectAssociations> ivdList = new LinkedList<>();
	for (final VimConnection vim : this.vimConnections.values()) {
	    ivdList.addAll(vim.getVslmConnection().retrieveVStorageObjectAssociations());
	}
	return ivdList;

    }

    private void SSLException() throws NoSuchAlgorithmException, KeyManagementException {

	final javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
	final javax.net.ssl.TrustManager tm = new com.vmware.vmbk.soap.helpers.TrustAllTrustManager();
	trustAllCerts[0] = tm;

	this.sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1.2");

	this.sslContext.init(null, trustAllCerts, null);

	javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(this.sslContext.getSocketFactory());

    }
}
