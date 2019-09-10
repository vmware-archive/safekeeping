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
package com.vmware.vmbk.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.command.BackupCommand;
import com.vmware.vmbk.command.RestoreCommand;
import com.vmware.vmbk.command.RestoreOptions;
import com.vmware.vmbk.control.target.AwsS3Target;
import com.vmware.vmbk.control.target.FileTarget;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.control.target.NfsTarget;
import com.vmware.vmbk.type.BackupMode;
import com.vmware.vmbk.type.Manipulator.VddkConfManipulator;
import com.vmware.vmbk.util.Utility;

public final class GlobalConfiguration {
    static {
	initializeGroups();
    }
    public static final String _G_PSC_PROVIDER = "pscProvider";
    public static final String _G_CSP_PROVIDER = "cspProvider";
    private static final String _GLOBAL_PROFILE_FILE_NAME = "global.json";
    private static final String _FCO_PROFILE_FILE_NAME = "profile.json";
    private static final String _CONFIG_PROPERTIES = "config.properties";
    public static final String _GENERATION_PROFILE_FILENAME = "generation.json";

    private static final String ACCEPT_UNTRUSTED_CERTIFICATE = "acceptUntrustedCertificate";

    private static final String AUTHENTICATION_PROVIDER = "authenticationProvider";
    private static final String CERTIFICATE_DIRECTORY = "cert";
    private static final String CONFIG_DIRECTORY = "conf";

    private static final String CSP_HOST = "cspHost";
    private static final String CSP_REFRESH_TOKEN = "refreshToken";
    private static final String CSP_VAPI_SERVER = "vapiServer";
    private static final Boolean DEFAULT_ACCEPT_UNTRUSTED_CERTIFICATE = true;
    private static final String DEFAULT_AUTHENTICATION_PROVIDER = _G_PSC_PROVIDER;
    private static final Boolean DEFAULT_ENABLE_COMPRESSION = true;
    private static final boolean DEFAULT_FORCE_SNAPSHOT_BEFORE_RESTORE = false;
    private static final String DEFAULT_INTERACTIVE_PROMPT = "vmbk# ";
    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";
    private static final String DEFAULT_KEY_STORE_TYPE = "jks";
    private static final String DEFAULT_KEYSTORE = "keystore.jks";

    private static final Integer DEFAULT_MAX_BLOCK_SIZE_MB = 250;
    private static final Integer DEFAULT_MAX_GET_THREADS_POOL = 5;

    private static final Integer DEFAULT_MAX_POST_THREADS_POOL = 5;
    private static final Integer DEFAULT_NFC_HOST_PORT = 902;

    private static final String DEFAULT_REPOSITORY = "amazonS3";

    private static final long DEFAULT_TICKET_LIFE_EXPECTANCY_IN_MILLISECONDS = 60 * 10 * 60 * 1000;
    private static final String DEFAULT_TRANSPORT_FULL_MODE = "hotadd";
    private static final String DEFAULT_TRANSPORT_INC_MODE = "nbdssl";
    private static final String DEFAULT_TRANSPORT_MODE_RESTORE = "hotadd";
    private static final Boolean DEFAULT_USE_BASE64_PASSWD = true;
    private static final String DEFAULT_VDDK_CONFIG = "vddk.conf";

    private static final String DEFAULT_VM_FOLDER_FILTER = "";

    private static final String DEFAULT_VM_RESOURCE_POOL_FILTER = "";
    private static final String DEFAULT_VMC_TRANSPORT_MODE = "hotadd";

    private static final String DEFAULT_VMC_VM_FOLDER_FILTER = "SDDC-Datacenter/vm/Workloads";

    private static final String DEFAULT_VMC_VM_RESOURCE_POOL_FILTER = "SDDC-Datacenter/host/Cluster-1/Resources/Compute-ResourcePool";
    private static final String ENABLE_COMPRESSION = "enableCompression";
    private static final String FORCE_SNAPSHOT_BEFORE_RESTORE = "forceSnaphotBeforeRestore";

    private static final String G_CMDLINE = "cmdline";

    private static final String G_FILTER = "filter";
    private static final String G_GLOBAL = "global";
    private static final String G_KS8 = "ks8";

    private static final String INTERACTIVE_PROMPT = "prompt";
    private static final String KEEP_GENERATIONS = "keep_generations";

    private static final String KEY_STORE_PASSWORD = "keyStorePassword";
    private static final String KEY_STORE_TYPE = "keyStoreType";
    private static final String KEY_AUTOLOGIN = "autologin";

    private static final String KEYSTORE = "keystore";

    private static final String MAX_BLOCK_SIZE_MB = "maxBlockSizeMb";

    private static final String MAX_GET_THREADS_POOL = "maxGetThreadsPool";

    private static final String MAX_POST_THREADS_POOL = "maxPostThreadsPool";

    private static final String NFCHOSTPORT = "nfchostport";

    private static final String PASSWORD = "password";

    private static final String PSC_SERVER = "pscServer";
    private static final String ARACHNI_SERVER = "arachniServer";
    private static final String ARACHNI_SERVER_PORT = "arachniServerPort";

    private static final String REPOSITORY = "repository";

    private static final String TASK_DIRECTORY = "task";

    private static final String SSO_TICKET_LIFE_EXPECTANCY_IN_SECONDS = "ssoTicketLifeExpectancy";

    private static final String TRANSPORT_FULL_MODE = "transportFullMode";

    private static final String TRANSPORT_INC_MODE = "transportIncMode";
    private static final String TRANSPORT_MODE_RESTORE = "transportRestore";

    private static final String USE_BASE64_PASSWD = "useBase64Passwd";

    private static final String USERNAME = "username";

    private static final String VDDK_CONFIG = "vddkConfig";

    private static final String VDDK_LIB_PATH = "vddkLibPath";

    private static final String VM_FOLDER_FILTER = "vmFolderFilter";

    private static final String VM_RESOURCE_POOL_FILTER = "resourcePoolFilter";

    private static final String X509_CERT_FILENAME = "sdk.crt";

    private static final String X509_CERT_PRIVATE_KEY_FILENAME = "sdk.key";

    private static final int DEFAULT_KEEP_GENERATIONS = 5;

    private static final Logger logger = Logger.getLogger(GlobalConfiguration.class.getName());
    private static final Boolean DEFAULT_CONNECT_VSLM = true;
    private static final String CONNECT_VSLM = "connectToVslm";

    private static int MaxNumberOfVirtaulMachineNetworkCard = 10;

// TODO Remove unused code found by UCDetector
//     public static String getBinPath() {
//
// 	return getInstallPath() + File.separatorChar + BIN_DIRECTORY;
//     }

    static private GlobalConfiguration config;

    private static String cmdLineGroup;

    private static String cspProviderGroup;

    private static String k8sGroup;

// TODO Remove unused code found by UCDetector
//     public static String getDefaultKeyStoreFile() {
//
// 	return getCertificatePath() + File.separatorChar + DEFAULT_KEYSTORE;
//
//     }

    private static boolean emptyConfig;

    private static String filterGroup;

    private static String globalGroup;

// TODO Remove unused code found by UCDetector
//     Properties properties;

    private static String pscProvider;

    private static ITarget[] repositories;

    private static File configPropertyFile;

    private static PropertyMap iniConfiguration = new PropertyMap();

    public static Boolean acceptUntrustedCertificate() {
	logger.entering(GlobalConfiguration.class.getName(), "acceptUntrustedCertificate");
	final Boolean result = iniConfiguration.getBooleanProperty(globalGroup, ACCEPT_UNTRUSTED_CERTIFICATE,
		DEFAULT_ACCEPT_UNTRUSTED_CERTIFICATE);
	logger.exiting(GlobalConfiguration.class.getName(), "acceptUntrustedCertificate", result);
	return result;
    }

    private static void configureDefault(final boolean vmcMode, final LinkedHashMap<String, String> confDefault) {
	if (vmcMode) {
	    System.out.println("VMware Cloud on AWS detected");
	    System.out.println("Importing default cloud settings");
	    confDefault.put(TRANSPORT_FULL_MODE, DEFAULT_VMC_TRANSPORT_MODE);
	    confDefault.put(TRANSPORT_INC_MODE, DEFAULT_VMC_TRANSPORT_MODE);
	    confDefault.put(VM_FOLDER_FILTER, DEFAULT_VMC_VM_FOLDER_FILTER);
	    if (StringUtils.isEmpty(iniConfiguration.getStringProperty(filterGroup, VM_FOLDER_FILTER))) {
		iniConfiguration.setStringProperty(filterGroup, VM_FOLDER_FILTER, DEFAULT_VMC_VM_FOLDER_FILTER);
	    }
	    if (StringUtils.isEmpty(iniConfiguration.getStringProperty(filterGroup, VM_RESOURCE_POOL_FILTER))) {
		iniConfiguration.setStringProperty(filterGroup, VM_RESOURCE_POOL_FILTER,
			DEFAULT_VMC_VM_RESOURCE_POOL_FILTER);
	    }
	    iniConfiguration.setStringProperty(globalGroup, TRANSPORT_FULL_MODE, DEFAULT_VMC_TRANSPORT_MODE);
	    iniConfiguration.setStringProperty(globalGroup, TRANSPORT_INC_MODE, DEFAULT_VMC_TRANSPORT_MODE);
	    iniConfiguration.setStringProperty(globalGroup, TRANSPORT_MODE_RESTORE, DEFAULT_VMC_TRANSPORT_MODE);

	} else {
	    confDefault.put(TRANSPORT_FULL_MODE, DEFAULT_TRANSPORT_FULL_MODE);
	    confDefault.put(TRANSPORT_INC_MODE, DEFAULT_TRANSPORT_INC_MODE);
	    confDefault.put(VDDK_CONFIG, DEFAULT_VDDK_CONFIG);
	    confDefault.put(VM_FOLDER_FILTER, DEFAULT_VM_FOLDER_FILTER);
	    if (StringUtils.isEmpty(iniConfiguration.getStringProperty(globalGroup, VDDK_CONFIG))) {
		iniConfiguration.setStringProperty(globalGroup, VDDK_CONFIG, DEFAULT_VDDK_CONFIG);
	    }
	    if (StringUtils.isEmpty(iniConfiguration.getStringProperty(globalGroup, TRANSPORT_FULL_MODE))) {
		iniConfiguration.setStringProperty(globalGroup, TRANSPORT_FULL_MODE, DEFAULT_TRANSPORT_FULL_MODE);
	    }
	    if (StringUtils.isEmpty(iniConfiguration.getStringProperty(globalGroup, TRANSPORT_INC_MODE))) {
		iniConfiguration.setStringProperty(globalGroup, TRANSPORT_INC_MODE, DEFAULT_TRANSPORT_INC_MODE);
	    }
	    if (StringUtils.isEmpty(iniConfiguration.getStringProperty(globalGroup, TRANSPORT_MODE_RESTORE))) {
		iniConfiguration.setStringProperty(globalGroup, TRANSPORT_MODE_RESTORE, DEFAULT_TRANSPORT_MODE_RESTORE);
	    }
	}

	if (StringUtils.isEmpty(iniConfiguration.getStringProperty(globalGroup, NFCHOSTPORT))) {
	    iniConfiguration.setIntegerProperty(globalGroup, NFCHOSTPORT, DEFAULT_NFC_HOST_PORT);
	}
	if (iniConfiguration.getBooleanProperty(globalGroup, USE_BASE64_PASSWD) == null) {
	    iniConfiguration.setBooleanProperty(globalGroup, USE_BASE64_PASSWD, DEFAULT_USE_BASE64_PASSWD);
	}
	if (StringUtils.isEmpty(iniConfiguration.getStringProperty(cmdLineGroup, INTERACTIVE_PROMPT))) {
	    iniConfiguration.setStringProperty(cmdLineGroup, INTERACTIVE_PROMPT, DEFAULT_INTERACTIVE_PROMPT);
	}

    }

    /**
     * @return
     */
    public static boolean connectToVslm() {
	logger.entering(GlobalConfiguration.class.getName(), "connectToVslm");
	final Boolean result = iniConfiguration.getBooleanProperty(globalGroup, CONNECT_VSLM, DEFAULT_CONNECT_VSLM);
	logger.exiting(GlobalConfiguration.class.getName(), "connectToVslm", result);
	return result;
    }

    public static boolean DefaulConfigPropertiesFileExist() {
	final File configPropertyFile = new File(getDefaulConfigPropertiesFile());
	return configPropertyFile.exists();
    }

    private static boolean finalizeConfiguration(final boolean vmcMode) throws IOException {

	try (VddkConfManipulator vddk = new VddkConfManipulator(
		getConfigPath() + File.separatorChar + DEFAULT_VDDK_CONFIG)) {
	    vddk.setNoNfcSession(vmcMode);
	    vddk.enablePhoneHome(true);
	    vddk.save();
	}
	write();
	return true;
    }

    public static String getArachniServer() {
	logger.entering(GlobalConfiguration.class.getName(), "getArachniServer");
	final String result = iniConfiguration.getStringProperty(k8sGroup, ARACHNI_SERVER);
	logger.exiting(GlobalConfiguration.class.getName(), "getArachniServer", result);
	return result;
    }

    public static Integer getArachniServerPort() {
	logger.entering(GlobalConfiguration.class.getName(), "getArachniServerPort");
	final Integer result = iniConfiguration.getIntegerProperty(k8sGroup, ARACHNI_SERVER_PORT, 8080);
	logger.exiting(GlobalConfiguration.class.getName(), "getArachniServerPort", result);
	return result;
    }

    /*
     * Get value of [global] AUTHENTICATION_PROVIDER.
     *
     * @return platformServCtrl when the entry is not found.
     */
    public static String getAuthenticationProvider() {
	logger.entering(GlobalConfiguration.class.getName(), "getAuthenticationProvider");
	final String result = iniConfiguration.getStringProperty(globalGroup, AUTHENTICATION_PROVIDER,
		DEFAULT_AUTHENTICATION_PROVIDER);
	logger.exiting(GlobalConfiguration.class.getName(), "getAuthenticationProvider", result);
	return result;
    }

    private static String getCertificatePath() {

	return getConfigPath() + File.separatorChar + CERTIFICATE_DIRECTORY;
    }

    public static String getCertPath() {
	logger.entering(GlobalConfiguration.class.getName(), "getCertPath");
	final String certPath = getCertificatePath() + File.separatorChar + getKeystore();
	final String result = certPath.replace('\\', '/');
	logger.exiting(GlobalConfiguration.class.getName(), "getCertPath", result);
	return result;
    }

    public static String getConfigPath() {

	return getInstallPath() + File.separatorChar + CONFIG_DIRECTORY;
    }

    public static String getCspHost() {
	logger.entering(GlobalConfiguration.class.getName(), "getCspHost");
	final String result = iniConfiguration.getStringProperty(cspProviderGroup, CSP_HOST);
	logger.exiting(GlobalConfiguration.class.getName(), "getCspHost", result);
	return result;
    }

    public static String getDefaulConfigPropertiesFile() {
	return getConfigPath() + File.separatorChar + _CONFIG_PROPERTIES;
    }

    public static String getDefaultProfileVmPath(final String moref) {
	logger.entering(GlobalConfiguration.class.getName(), "getDefaultProfileVmPath", moref);
	final String result = String.format("%s/%s", moref, GlobalConfiguration._FCO_PROFILE_FILE_NAME);
	logger.exiting(GlobalConfiguration.class.getName(), "getDefaultProfileVmPath", result);
	return result;
    }

    public static boolean getEnableCompression() {
	logger.entering(GlobalConfiguration.class.getName(), "getEnableCompression");
	final Boolean result = iniConfiguration.getBooleanProperty(globalGroup, ENABLE_COMPRESSION,
		DEFAULT_ENABLE_COMPRESSION);
	logger.exiting(GlobalConfiguration.class.getName(), "getEnableCompression", result);
	return result;
    }

    public static String getGlobalProfileFileName() {
	return _GLOBAL_PROFILE_FILE_NAME;
    }

    public static String getInstallPath() {
	final File f = new File(new File(".").getAbsolutePath());
	final String installPath = f.getPath();
	logger.fine("Installation Directory:" + installPath + "\n");
	return installPath.substring(0, installPath.length() - 2);
    }

    public static String getInteractivePrompt() {
	logger.entering(GlobalConfiguration.class.getName(), "getInteractivePrompt");
	final String result = iniConfiguration.getStringProperty(cmdLineGroup, INTERACTIVE_PROMPT,
		DEFAULT_INTERACTIVE_PROMPT);
	logger.exiting(GlobalConfiguration.class.getName(), "getInteractivePrompt", result);
	return result;
    }

    public static int getKeepGenerations() {
	logger.entering(GlobalConfiguration.class.getName(), "getKeepGenerations");
	final Integer result = iniConfiguration.getIntegerProperty(globalGroup, KEEP_GENERATIONS,
		DEFAULT_KEEP_GENERATIONS);
	logger.exiting(GlobalConfiguration.class.getName(), "getKeepGenerations", result);
	return result;
    }

    public static String getKeyCertStorePassword() {
	return "";

    }

    public static String getKeystore() {
	logger.entering(GlobalConfiguration.class.getName(), "getKeystore");
	final String result = iniConfiguration.getStringProperty(pscProvider, KEYSTORE, DEFAULT_KEYSTORE);
	logger.exiting(GlobalConfiguration.class.getName(), "getKeystore", result);
	return result;
    }

    public static String getKeyStorePassword() {
	logger.entering(GlobalConfiguration.class.getName(), "getKeyStorePassword");
	final String result = iniConfiguration.getStringProperty(pscProvider, KEY_STORE_PASSWORD,
		DEFAULT_KEY_STORE_PASSWORD);
	logger.exiting(GlobalConfiguration.class.getName(), "getKeyStorePassword", result);
	return result;
    }

    public static String getKeyStoreType() {
	logger.entering(GlobalConfiguration.class.getName(), "getKeyStoreType");
	final String result = iniConfiguration.getStringProperty(pscProvider, KEY_STORE_TYPE, DEFAULT_KEY_STORE_TYPE);
	logger.exiting(GlobalConfiguration.class.getName(), "getKeyStoreType", result);
	return result;
    }

    public static int getMaxBlockSize() {
	logger.entering(GlobalConfiguration.class.getName(), "getMaxBlockSize");
	final Integer result = iniConfiguration.getIntegerProperty(globalGroup, MAX_BLOCK_SIZE_MB,
		DEFAULT_MAX_BLOCK_SIZE_MB);
	logger.exiting(GlobalConfiguration.class.getName(), "getMaxBlockSize", result);
	return result;
    }

    public static int getMaxGetThreadsPool() {
	logger.entering(GlobalConfiguration.class.getName(), "getMaxGetThreadsPool");
	final Integer result = iniConfiguration.getIntegerProperty(globalGroup, MAX_GET_THREADS_POOL,
		DEFAULT_MAX_GET_THREADS_POOL);
	logger.exiting(GlobalConfiguration.class.getName(), "getMaxGetThreadsPool", result);
	return result;

    }

    public static int getMaxNumberOfVirtaulMachineNetworkCard() {
	return MaxNumberOfVirtaulMachineNetworkCard;
    }

    public static int getMaxPostThreadsPool() {
	logger.entering(GlobalConfiguration.class.getName(), "getMaxPostThreadsPool");
	final Integer result = iniConfiguration.getIntegerProperty(globalGroup, MAX_POST_THREADS_POOL,
		DEFAULT_MAX_POST_THREADS_POOL);
	logger.exiting(GlobalConfiguration.class.getName(), "getMaxPostThreadsPool", result);
	return result;
    }

    public static int getNfcHostPort() {
	logger.entering(GlobalConfiguration.class.getName(), "getNfcHostPort");
	final Integer result = iniConfiguration.getIntegerProperty(globalGroup, NFCHOSTPORT, DEFAULT_NFC_HOST_PORT);
	logger.exiting(GlobalConfiguration.class.getName(), "getNfcHostPort", result);
	return result;
    }

    public static String getPassword() {
	logger.entering(GlobalConfiguration.class.getName(), "getPassword");
	String result = iniConfiguration.getStringProperty(pscProvider, PASSWORD, StringUtils.EMPTY);
	if (useBase64Passwd()) {
	    result = new String(Base64.decodeBase64(result));
	}
	logger.exiting(GlobalConfiguration.class.getName(), "getPassword", result);
	return result;

    }

    public static String getRefreshToken() {
	logger.entering(GlobalConfiguration.class.getName(), "getRefreshToken");
	final String result = iniConfiguration.getStringProperty(globalGroup, CSP_REFRESH_TOKEN);
	logger.exiting(GlobalConfiguration.class.getName(), "getRefreshToken", result);
	return result;
    }

    public static ITarget getRepositoryTarget() {
	for (final ITarget repository : repositories) {
	    if (repository.getTargetName().equalsIgnoreCase(getTargetRepository())) {
		return repository;
	    }
	}
	return repositories[0];
    }

    public static String getRpFilter() {
	logger.entering(GlobalConfiguration.class.getName(), "getRpFilter");
	final String result = iniConfiguration.getStringProperty(filterGroup, VM_RESOURCE_POOL_FILTER,
		DEFAULT_VM_RESOURCE_POOL_FILTER);
	logger.exiting(GlobalConfiguration.class.getName(), "getRpFilter", result);
	return result;

    }

    public static String getSsoServer() {
	logger.entering(GlobalConfiguration.class.getName(), "getSsoServer");
	final String result = iniConfiguration.getStringProperty(pscProvider, PSC_SERVER);
	logger.exiting(GlobalConfiguration.class.getName(), "getSsoServer", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public Integer getTargetCustomValueAsInt(final String pstrSection, final String key) {
// 	logger.entering(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool", new Object[] { pstrSection, key });
// 	final Integer result = iniConfiguration.getIntegerProperty(pstrSection, key);
// 	logger.exiting(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool", result);
// 	return result;
//     }

    public static boolean getTargetCustomValueAsBool(final String pstrSection, final String key) {
	logger.entering(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool",
		new Object[] { pstrSection, key });
	Boolean result = iniConfiguration.getBooleanProperty(pstrSection, key);
	if ((result == null)) {
	    result = false;
	}
	logger.exiting(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool", result);
	return result;
    }

    public static Boolean getTargetCustomValueAsBool(final String pstrSection, final String key,
	    final boolean defaultValue) {
	logger.entering(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool",
		new Object[] { pstrSection, key, defaultValue });
	final Boolean result = iniConfiguration.getBooleanProperty(pstrSection, key, defaultValue);

	logger.exiting(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool", result);
	return result;
    }

    public static Integer getTargetCustomValueAsInt(final String pstrSection, final String key,
	    final Integer defaultValue) {
	logger.entering(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool",
		new Object[] { pstrSection, key, defaultValue });
	final Integer result = iniConfiguration.getIntegerProperty(pstrSection, key, defaultValue);
	logger.exiting(GlobalConfiguration.class.getName(), "getTargetCustomValueAsBool", result);
	return result;
    }

    public static String getTargetCustomValueAsString(final String pstrSection, final String key) {
	logger.entering(GlobalConfiguration.class.getName(), "getTargetCustomValue", new Object[] { pstrSection, key });
	String result = iniConfiguration.getStringProperty(pstrSection, key);
	if (StringUtils.isEmpty(result)) {
	    result = null;
	}
	logger.exiting(GlobalConfiguration.class.getName(), "getTargetCustomValue", result);
	return result;
    }

    public static String getTargetCustomValueAsString(final String pstrSection, final String key,
	    final String defaultValue) {
	logger.entering(GlobalConfiguration.class.getName(), "getTargetCustomValueAsString",
		new Object[] { pstrSection, key, defaultValue });
	final String result = iniConfiguration.getStringProperty(pstrSection, key, defaultValue);

	logger.exiting(GlobalConfiguration.class.getName(), "getTargetCustomValueAsString", result);
	return result;
    }

    public static String getTargetRepository() {
	logger.entering(GlobalConfiguration.class.getName(), "getTargetRepository");
	final String result = iniConfiguration.getStringProperty(globalGroup, REPOSITORY);
	logger.exiting(GlobalConfiguration.class.getName(), "getTargetRepository", result);
	return result;
    }

    public static String getTaskDirectory() {
	return getInstallPath() + File.separatorChar + TASK_DIRECTORY;
    }

    public static long getTicketLifeExpectancyInMilliSeconds() {
	logger.entering(GlobalConfiguration.class.getName(), "getTicketLifeExpectancyInMilliSeconds");
	Long result = iniConfiguration.getLongProperty(pscProvider, SSO_TICKET_LIFE_EXPECTANCY_IN_SECONDS,
		DEFAULT_TICKET_LIFE_EXPECTANCY_IN_MILLISECONDS);
	result *= 1000;
	logger.exiting(GlobalConfiguration.class.getName(), "getTicketLifeExpectancyInMilliSeconds", result);
	return result;
    }

    public static String getTransportMode(final BackupCommand backupInfo) {
	logger.entering(GlobalConfiguration.class.getName(), "getTransportMode", backupInfo);
	final BackupMode mode = (backupInfo.getMode() == BackupMode.unknow) ? BackupMode.full : backupInfo.getMode();
	String result = backupInfo.getTransportMode();
	if (StringUtils.isEmpty(result)) {
	    result = iniConfiguration.getStringProperty(globalGroup,
		    (mode == BackupMode.full) ? TRANSPORT_FULL_MODE : TRANSPORT_INC_MODE);

	}
	logger.exiting(GlobalConfiguration.class.getName(), "getTransportMode", result);
	return result;
    }

    public static String getTransportModeRestore(final RestoreCommand restoreInfo) {
	logger.entering(GlobalConfiguration.class.getName(), "getTransportModeRestore", restoreInfo);
	String result = restoreInfo.getTransportMode();
	if (StringUtils.isEmpty(result)) {
	    result = iniConfiguration.getStringProperty(globalGroup, TRANSPORT_MODE_RESTORE,
		    DEFAULT_TRANSPORT_MODE_RESTORE);
	}
	logger.exiting(GlobalConfiguration.class.getName(), "getTransportModeRestore", result);
	return result;
    }

    public static String getTransportModeRestore(final RestoreOptions restoreInfo) {
	logger.entering(GlobalConfiguration.class.getName(), "getTransportModeRestore", restoreInfo);
	String result = restoreInfo.getTransportMode();
	if (StringUtils.isEmpty(result)) {
	    result = iniConfiguration.getStringProperty(globalGroup, TRANSPORT_MODE_RESTORE,
		    DEFAULT_TRANSPORT_MODE_RESTORE);
	}
	logger.exiting(GlobalConfiguration.class.getName(), "getTransportModeRestore", result);
	return result;
    }

    public static String getTrustStorePath() {
	final String certPath = getCertificatePath() + File.separatorChar + "cacerts";

	return certPath.replace('\\', '/');
    }

    public static String getUsername() {
	logger.entering(GlobalConfiguration.class.getName(), "getUsername");
	final String result = iniConfiguration.getStringProperty(pscProvider, USERNAME);
	logger.exiting(GlobalConfiguration.class.getName(), "getUsername", result);
	return result;
    }

    public static String getVapiServer() {
	logger.entering(GlobalConfiguration.class.getName(), "getVapiServer");
	final String result = iniConfiguration.getStringProperty(cspProviderGroup, CSP_VAPI_SERVER);
	logger.exiting(GlobalConfiguration.class.getName(), "getVapiServer", result);
	return result;
    }

    public static String getVddk_config() {
	logger.entering(GlobalConfiguration.class.getName(), "getVddk_config");
	final String result = getConfigPath() + File.separatorChar
		+ iniConfiguration.getStringProperty(globalGroup, VDDK_CONFIG, DEFAULT_VDDK_CONFIG);
	logger.exiting(GlobalConfiguration.class.getName(), "getVddk_config", result);
	return result;
    }

    public static String getVddkLibPath() {
	logger.entering(GlobalConfiguration.class.getName(), "getVddkLibPath");

	final String result = iniConfiguration.getStringProperty(globalGroup, VDDK_LIB_PATH,
		getInstallPath() + File.separatorChar + ((Utility.isWindows()) ? "bin" : "lib"));

	logger.exiting(GlobalConfiguration.class.getName(), "getVddkLibPath", result);
	return result;
    }

    public static String getVmFilter() {
	logger.entering(GlobalConfiguration.class.getName(), "getVmFilter");
	final String result = iniConfiguration.getStringProperty(filterGroup, VM_FOLDER_FILTER,
		DEFAULT_VM_FOLDER_FILTER);
	logger.exiting(GlobalConfiguration.class.getName(), "getVmFilter", result);
	return result;
    }

    public static String getX509CertFile() {

	return getCertificatePath() + File.separatorChar + X509_CERT_FILENAME;

    }

    public static String getX509CertPrivateKeyFile() {
	return getCertificatePath() + File.separatorChar + X509_CERT_PRIVATE_KEY_FILENAME;
    }

    private static void initializeGroups() {

	globalGroup = G_GLOBAL;
	pscProvider = _G_PSC_PROVIDER;
	cmdLineGroup = G_CMDLINE;
	filterGroup = G_FILTER;

	cspProviderGroup = _G_CSP_PROVIDER;
	k8sGroup = G_KS8;
    }

    public static void initTargets() {
	repositories = new ITarget[] { new AwsS3Target(), new FileTarget(), new NfsTarget() };
    }

    public static boolean isEmptyConfig() {
	return emptyConfig;
    }

    public static boolean isForceSnapBeforeRestore() {
	logger.entering(GlobalConfiguration.class.getName(), "isForceSnapBeforeRestore");
	final Boolean result = iniConfiguration.getBooleanProperty(globalGroup, FORCE_SNAPSHOT_BEFORE_RESTORE,
		DEFAULT_FORCE_SNAPSHOT_BEFORE_RESTORE);
	logger.exiting(GlobalConfiguration.class.getName(), "isForceSnapBeforeRestore", result);
	return result;
    }

    public static boolean loadConfig(final File configPropertyFile) throws Exception {
	GlobalConfiguration.configPropertyFile = configPropertyFile;

	if (configPropertyFile.exists()) {
	    iniConfiguration.clear();
	    iniConfiguration.loadPropertyFile(configPropertyFile);
	    emptyConfig = false;
	} else {
	    emptyConfig = true;
	}
	initTargets();
	return true;
    }

    public static boolean manualConfiguration(final boolean quiet) {
	BufferedReader br = null;
	final Reader r = new InputStreamReader(System.in);
	br = new BufferedReader(r);
	String str = null;
	boolean vmcMode = false;
	final LinkedHashMap<String, String> confDefault = new LinkedHashMap<>();
	final LinkedHashMap<String, String> confGlobalParams = new LinkedHashMap<>();
	final LinkedHashMap<String, String> confVcenterParams = new LinkedHashMap<>();
	final LinkedHashMap<String, String> confFilterParams = new LinkedHashMap<>();
	final LinkedHashMap<String, String> confComLineParams = new LinkedHashMap<>();
	final LinkedHashMap<String, LinkedHashMap<String, String>> confParams = new LinkedHashMap<>();

	confDefault.put(TRANSPORT_FULL_MODE, DEFAULT_TRANSPORT_FULL_MODE);
	confDefault.put(TRANSPORT_INC_MODE, DEFAULT_TRANSPORT_INC_MODE);

	confDefault.put(MAX_POST_THREADS_POOL, DEFAULT_MAX_POST_THREADS_POOL.toString());
	confDefault.put(MAX_GET_THREADS_POOL, DEFAULT_MAX_GET_THREADS_POOL.toString());
	confDefault.put(MAX_BLOCK_SIZE_MB, DEFAULT_MAX_BLOCK_SIZE_MB.toString());
	confDefault.put(KEY_STORE_TYPE, DEFAULT_KEY_STORE_TYPE);
	confDefault.put(KEY_STORE_PASSWORD, DEFAULT_KEY_STORE_PASSWORD);
	confDefault.put(KEYSTORE, DEFAULT_KEYSTORE);
	confDefault.put(ACCEPT_UNTRUSTED_CERTIFICATE, DEFAULT_ACCEPT_UNTRUSTED_CERTIFICATE.toString());
	confDefault.put(ENABLE_COMPRESSION, DEFAULT_ENABLE_COMPRESSION.toString());
	confDefault.put(REPOSITORY, DEFAULT_REPOSITORY);
	confDefault.put(NFCHOSTPORT, DEFAULT_NFC_HOST_PORT.toString());
	confDefault.put(USE_BASE64_PASSWD, DEFAULT_USE_BASE64_PASSWD.toString());
	for (final ITarget repository : repositories) {
	    confDefault.putAll(repository.defaultConfigurations());
	}
	String pscServer = "";
	if (quiet) {
	    try {
		pscServer = iniConfiguration.getStringProperty(pscProvider, PSC_SERVER);
		vmcMode = pscServer.contains(".vmwarevmc.com") || pscServer.contains(".vmc.vmware.com");
		configureDefault(vmcMode, confDefault);
		return finalizeConfiguration(vmcMode);
	    } catch (final IOException e) {
		System.err.println(e.getLocalizedMessage());
		System.exit(5);
	    }
	} else {
	    while (true) {
		try {
		    confParams.clear();
		    confVcenterParams.clear();
		    confVcenterParams.put(PSC_SERVER, "VMware Platform Service Controller FQDN or IP [%s]");

		    confParams.put(pscProvider, confVcenterParams);
		    do {
			readConfParams(br, confParams, confDefault);
			pscServer = iniConfiguration.getStringProperty(pscProvider, PSC_SERVER);
		    } while ((pscServer == null) || pscServer.isEmpty());
		    vmcMode = pscServer.contains(".vmwarevmc.com") || pscServer.contains(".vmc.vmware.com");
		    configureDefault(vmcMode, confDefault);

		} catch (final IOException e) {
		    System.err.println(e.getLocalizedMessage());
		    System.exit(5);
		}

		try {
		    confVcenterParams.clear();
		    confGlobalParams.clear();
		    confFilterParams.clear();
		    confComLineParams.clear();

		    final StringBuilder repString = new StringBuilder();
		    repString.append('(');
		    for (final ITarget repository : repositories) {
			confParams.put(repository.getGroup(), repository.manualConfiguration());
			repString.append(repository.getTargetName());
			repString.append(' ');

		    }
		    repString.append(')');
		    confGlobalParams.put(REPOSITORY,
			    "Default Target Repository class " + repString.toString() + " [%s]");
		    confGlobalParams.put(ACCEPT_UNTRUSTED_CERTIFICATE, "Accept Untrusted Certificate [%s]");
		    confGlobalParams.put(TRANSPORT_FULL_MODE, "VDDK Transport Mode for Full backups [%s]");
		    confGlobalParams.put(TRANSPORT_INC_MODE, "VDDK Transport Mode for Incremental backups [%s]");
		    confGlobalParams.put(TRANSPORT_MODE_RESTORE, "VDDK Transport Mode for Restore [%s]");
		    confGlobalParams.put(MAX_POST_THREADS_POOL, "\nMax number of concurrent post threads [%s]");
		    confGlobalParams.put(MAX_GET_THREADS_POOL, "\nMax number of concurrent get threads [%s]");
		    confGlobalParams.put(MAX_BLOCK_SIZE_MB,
			    "\nMax atomic block size in MB (increase the value required more RAM) [%s]");
		    confGlobalParams.put(ENABLE_COMPRESSION, "\nEnable compression [%s]");
		    confVcenterParams.put(USERNAME, "\t Login account [%s]");
		    confVcenterParams.put(PASSWORD, "\t Password <hidden>");
		    confFilterParams.put(VM_FOLDER_FILTER, "\nVirtual Machine Folder Filter [%s]");
		    confFilterParams.put(VM_RESOURCE_POOL_FILTER, "\nVirtual Machine Resource pool [%s]");

		    confParams.put(globalGroup, confGlobalParams);
		    confParams.put(pscProvider, confVcenterParams);
		    confParams.put(filterGroup, confFilterParams);

		    readConfParams(br, confParams, confDefault);
		    printParams(confParams);
		    System.out.print("\n\nDo you conferm? (Yes/No)");
		    do {
			str = br.readLine();
			if (!str.isEmpty()) {
			    if (str.compareToIgnoreCase("yes") == 0) {
				return finalizeConfiguration(vmcMode);
			    }
			}
		    } while (!str.equalsIgnoreCase("no") && !str.equalsIgnoreCase("yes"));
		} catch (final Exception e) {
		    System.err.println(e.getLocalizedMessage());
		    System.exit(5);
		}
		System.out.println();
	    }
	}
	return true;
    }

    private static void printParams(final LinkedHashMap<String, LinkedHashMap<String, String>> confParams) {
	System.out.println(new String(new char[50]).replace("\0", "\r\n"));
	for (final String group : confParams.keySet()) {
	    System.out.println();
	    System.out.println(group.toUpperCase());
	    for (final String key : confParams.get(group).keySet()) {
		final String val = iniConfiguration.getStringProperty(group, key);
		System.out.println(String.format(confParams.get(group).get(key), val));
	    }
	}
    }

    private static void readConfParams(final BufferedReader br,
	    final LinkedHashMap<String, LinkedHashMap<String, String>> confParams,
	    final LinkedHashMap<String, String> confDefault) throws IOException {

	String str = "";
	final java.io.Console cnsl = System.console();
	for (final String group : confParams.keySet()) {
	    System.out.println();
	    System.out.println(group.toUpperCase());
	    for (final String key : confParams.get(group).keySet()) {
		String val = iniConfiguration.getStringProperty(group, key);
		if (val == null) {
		    if (confDefault.containsKey(key)) {
			val = confDefault.get(key);
		    } else {
			val = "";
		    }
		}

		if (confParams.get(group).get(key).contains("%s")) {
		    System.out.print(String.format(confParams.get(group).get(key), val));
		    str = br.readLine();
		} else {
		    if (cnsl == null) {
			System.out.print(String.format(confParams.get(group).get(key), val));
			str = br.readLine();

			str = new String(Base64.encodeBase64(str.getBytes()));
		    } else {
			char[] passwd1 = null;
			char[] passwd12 = null;
			while (true) {
			    System.out.print(String.format(confParams.get(group).get(key), val));
			    passwd1 = cnsl.readPassword();

			    if (passwd1 == null) {
				str = "";
				break;
			    }
			    passwd12 = cnsl.readPassword("\tRepeat password:     ");
			    if (java.util.Arrays.equals(passwd1, passwd12)) {

				str = new String(Base64.encodeBase64(new String(passwd1).getBytes()));
				break;
			    }
			}
		    }

		}
		if (!str.isEmpty()) {
		    iniConfiguration.setStringProperty(group, key, str);
		} else {
		    iniConfiguration.setStringProperty(group, key, val);
		}
	    }

	}
    }

    public static void testConnectionTo(final String aURL) throws Exception {
	logger.entering(GlobalConfiguration.class.getName(), "testConnectionTo");
	final URL destinationURL = new URL(aURL);
	final HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
	conn.connect();
	final java.security.cert.Certificate[] certs = conn.getServerCertificates();
	System.out.println("nb = " + certs.length);

	for (final java.security.cert.Certificate cert : certs) {
	    System.out.println("");
	    System.out.println("");
	    System.out.println("");
	    System.out.println("################################################################");
	    System.out.println("");
	    System.out.println("");
	    System.out.println("");
	    System.out.println("Certificate is: " + cert);
	    if (cert instanceof X509Certificate) {
		((X509Certificate) cert).checkValidity();
		System.out.println("Certificate is active for current date");
	    } else {
		System.err.println("Unknown certificate type: " + cert);
	    }
	}
	logger.exiting(GlobalConfiguration.class.getName(), "testConnectionTo");
    }

    public static boolean useBase64Passwd() {
	logger.entering(GlobalConfiguration.class.getName(), "useBase64Passwd");
	final Boolean result = iniConfiguration.getBooleanProperty(globalGroup, USE_BASE64_PASSWD,
		DEFAULT_USE_BASE64_PASSWD);
	logger.exiting(GlobalConfiguration.class.getName(), "useBase64Passwd", result);
	return result;
    }

    public static void write() throws IOException {
	iniConfiguration.savePropertyFile(configPropertyFile);
    }

    private GlobalConfiguration() {
	// iniConfiguration = new PropertyMap();
    }

}
