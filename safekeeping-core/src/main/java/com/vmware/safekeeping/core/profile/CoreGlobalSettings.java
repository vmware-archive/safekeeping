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
package com.vmware.safekeeping.core.profile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.GuestOsUtils;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.control.MessageDigestAlgoritmhs;
import com.vmware.safekeeping.core.logger.VmbkLogFormatter;
import com.vmware.safekeeping.core.type.enums.WindowsQuiesceSpecVssBackupContext;
import com.vmware.safekeeping.core.type.enums.WindowsQuiesceSpecVssBackupType;
import com.vmware.safekeeping.core.util.KeyStoreHelper;

/**
 * Contains Safekeeping settings
 *
 */
public class CoreGlobalSettings {

    private static final String GLOBAL_PROFILE_FILE_NAME = "global.json";
    private static final String FCO_PROFILE_FILE_NAME = "profile.json";
    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
    public static final String GENERATION_PROFILE_FILENAME = "generation.json";

    protected static final String ACCEPT_UNTRUSTED_CERTIFICATE = "acceptUntrustedCertificate";

    private static final String CONFIG_DIRECTORY = "conf";
    private static final String CSP_REFRESH_TOKEN = "refreshToken";
    protected static final Boolean DEFAULT_VALUE_ACCEPT_UNTRUSTED_CERTIFICATE = true;
    protected static final Boolean DEFAULT_VALUE_ENABLE_COMPRESSION = true;
    private static final boolean DEFAULT_VALUE_FORCE_SNAPSHOT_BEFORE_RESTORE = false;
    protected static final String DEFAULT_VALUE_KEY_STORE_PASSWORD = "changeit";
    protected static final String DEFAULT_VALUE_KEY_STORE_TYPE = "pkcs12";
    protected static final String DEFAULT_VALUE_KEYSTORE = "keystore.pkcs12";

    protected static final Integer DEFAULT_VALUE_MAX_BLOCK_SIZE_MB = 20;
    protected static final Integer DEFAULT_VALUE_MAX_GET_THREADS_POOL = 10;

    protected static final Integer DEFAULT_VALUE_MAX_POST_THREADS_POOL = 10;

    private static final long DEFAULT_VALUE_TICKET_LIFE_EXPECTANCY_IN_SECONDS = 500;
    protected static final String DEFAULT_VALUE_TRANSPORT_FULL_MODE = "hotadd";
    protected static final String DEFAULT_VALUE_TRANSPORT_INC_MODE = "nbdssl";
    protected static final String DEFAULT_VALUE_TRANSPORT_MODE_RESTORE = "hotadd";
    protected static final String DEFAULT_VALUE_VDDK_CONFIG = "vddk.conf";

    protected static final String DEFAULT_VALUE_VM_FOLDER_FILTER = "";

    protected static final String DEFAULT_VALUE_VM_RESOURCE_POOL_FILTER = "";
    protected static final String DEFAULT_VALUE_VMC_TRANSPORT_MODE = "hotadd";

    protected static final String DEFAULT_VALUE_VMC_VM_FOLDER_FILTER = "SDDC-Datacenter/vm/Workloads";

    protected static final String DEFAULT_VALUE_VMC_VM_RESOURCE_POOL_FILTER = "SDDC-Datacenter/host/Cluster-1/Resources/Compute-ResourcePool";
    protected static final String ENABLE_COMPRESSION = "enableCompression";
    private static final String FORCE_SNAPSHOT_BEFORE_RESTORE = "forceSnaphotBeforeRestore";

    private static final String G_FILTER = "filter";
    private static final String G_GLOBAL = "global";
    private static final String G_KS8 = "ks8";

    private static final String KEEP_GENERATIONS = "keep_generations";

    protected static final String KEY_STORE_PASSWORD = "keyStorePassword";
    protected static final String KEY_STORE_TYPE = "keyStoreType";

    protected static final String KEYSTORE = "keystore";

    protected static final String MAX_BLOCK_SIZE_MB = "maxBlockSizeMb";

    protected static final String MAX_GET_THREADS_POOL = "maxGetThreadsPool";

    protected static final String MAX_POST_THREADS_POOL = "maxPostThreadsPool";

    private static final String ARACHNI_SERVER = "arachniServer";
    private static final String ARACHNI_SERVER_PORT = "arachniServerPort";

    private static final String TASK_DIRECTORY = "task";

    private static final String SSO_TICKET_LIFE_EXPECTANCY_IN_SECONDS = "ssoTicketLifeExpectancyInSec";

    protected static final String TRANSPORT_FULL_MODE = "transportFullMode";

    protected static final String TRANSPORT_INC_MODE = "transportIncMode";
    protected static final String TRANSPORT_MODE_RESTORE = "transportRestore";

    protected static final String VDDK_CONFIG = "vddkConfig";

    private static final String VDDK_LIB_PATH = "vddkLibPath";

    protected static final String VM_FOLDER_FILTER = "vmFolderFilter";

    protected static final String VM_RESOURCE_POOL_FILTER = "resourcePoolFilter";

    private static final String X509_CERT_FILENAME = "sdk.crt";

    private static final String X509_CERT_PRIVATE_KEY_FILENAME = "sdk.key";

    private static final int DEFAULT_VALUE_KEEP_GENERATIONS = 5;

    private static final Logger logger = Logger.getLogger(CoreGlobalSettings.class.getName());
    private static final Boolean DEFAULT_VALUE_CONNECT_VSLM = true;
    private static final String CONNECT_VSLM = "connectToVslm";
    private static final String TASK_MAX_WAIT_SECONDS = "taskMaxWaitInSeconds";
    private static final int DEFAULT_VALUE_TASK_MAX_WAIT_SECONDS = 60;
    private static final String USE_VDDK_VERSION = "vddkVersion";
    private static final boolean DEFAULT_VALUE_USE_QUERY_ALLOCACATED_BLOCKS_FOR_INCREMENTAL = true;

    private static final String USE_QUERY_ALLOCACATED_BLOCKS_FOR_INCREMENTAL_KEY = "useQueryAllocatedBlocksForIncremental";
    private static final Boolean DEFAULT_VALUE_USE_QUERY_ALLOCACATED_BLOCKS = true;
    private static final String USE_QUERY_ALLOCACATED_BLOCKS_KEY = "useQueryAllocatedBlocks";

    private static final Boolean DEFAULT_AUTO_CONFIGURE_CBT = true;
    private static final String AUTO_CONFIGURE_CBT = "autoConfigureCbt";

    /**
     * Number of target post operation retries before failing
     */
    private static final Integer DEFAULT_MAX_POST_DUMP_RETRIES = 5;
    private static final String MAX_POST_DUMP_RETRIES = "maxPostDumpRetries";
    private static final String MAX_VDDK_READ_RETRIES = "maxVddkReadRetries";
    private static final Integer DEFAULT_MAX_VDDK_READ_RETRIES = 5;
    private static final String OVERWRITE_VDDK_ON_START = "overwriteVddkOnStart";
    private static final Boolean DEFAULT_OVERWRITE_VDDK_ON_START = true;
    private static final String DELETE_VDDK_ON_EXIT = "deleteVddkOnExit";
    private static final Boolean DEFAULT_DELETE_VDDK_ON_EXIT = true;
    private static final String EXCLUDE_BACKUP_SERVER = "excludeBackupServer";
    private static final Boolean DEFAULT_EXCLUDE_BACKUP_SERVER = true;
    private static final String ENABLE_CIPHER = "enableCipher";
    private static final Boolean DEFAULT_VALUE_ENABLE_CIPHER = false;
    private static final String KEY_STORE_SSL_CERTIFICATE_ALIAS = "keyStoreSslCertificateAlias";
    private static final String DEFAULT_KEY_STORE_SSL_ALIAS = "jetty";
    private static final String DEFAULT_KEY_STORE_USER_CERTIFICATE_ALIAS = "user";
    private static final String KEY_STORE_USER_CERTIFICATE_ALIAS = "keyStoreUserCetificateAlias";
    private static final String KEY_STORE_SSL_CERTIFICATE_PASSWORD = "keyStoreSslCetificatePassword";
    public static final String REPOSITORY_DATA_PATH = "data/";
    private static final String VSS_BOOTABLE_SYSTEM_STATE = "vssBootableSystemState";
    private static final Boolean DEFAULT_VALUE_VSS_BOOTABLE_SYSTEM_STATE = true;
    private static final Boolean DEFAULT_VALUE_VSS_PARTIAL_FILE_SUPPORT = false;
    private static final String VSS_PARTIAL_FILE_SUPPORT = "vssPartialFileSupport";
    private static final String VSS_BACKUP_CONTEXT = "vssBackupContext";
    private static final String DEFAULT_VALUE_VSS_BACKUP_CONTEXT = "CTX_AUTO";
    private static final String USE_VSS_WINDOWS_SYSTEM = "useVssWindowsSystem";
    private static final Boolean DEFAULT_VALUE_USE_VSS_WINDOWS_SYSTEM = true;
    private static final String VSS_BACKUP_TYPE = "vssBackupType";
    private static final String DEFAULT_VALUE_VSS_BACKUP_TYPE = "VSS_BT_COPY";
    private static final Integer DEFAULT_VALUE_QUISCE_TIMEOUT = 5;
    private static final String QUISCE_TIMEOUT = "quisceTimeout";
    private static final String VSS_RETRY_ON_FAILURE = "retryOnFailure";
    private static final Boolean DEFAULT_VALUE_VSS_RETRY_ON_FAILURE = true;

    public static final int MAX_NUMBER_OF_VIRTUAL_MACHINE_NETWORK_CARDS = 10;
    private static final String ENABLE_FOR_ACCESS_ON = "enableForAccessOn";
    private static final Boolean DEFAULT_ENABLE_FOR_ACCESS_ON = true;
    private static final String DAEMON_PID = ".pid";

    private static String k8sGroup;

    protected static boolean emptyConfig;

    protected static String filterGroup;

    protected static String globalGroup;

    protected static File configPropertyFile;

    protected static PropertyMap configurationMap = new PropertyMap();

    private static String configPath;
    private static String logsPath;

    static {
        globalGroup = G_GLOBAL;
        filterGroup = G_FILTER;
        k8sGroup = G_KS8;
    }

    protected static final String USE_BASE64_PASSWD = "useBase64Passwd";

    protected static final Boolean DEFAULT_VALUE_USE_BASE64_PASSWD = true;
    private static final String WINDOWS_VSS_TIMEOUT = "WindowsVssTimeOut";
    private static final Integer DEFAULT_VALUE_VSS_TIMEOUT_SECONDS = 10;
    private static final String SKIP_ENCRYPTION_CHECK = "skipEncryptionCheck";
    private static final Boolean DEFAULT_VALUE_SKIP_ENCRYPTION_CHECK = false;
    private static final String MESSAGE_DIGEST_ALGORITHM = "digestAlgorithm";
    private static final MessageDigestAlgoritmhs DEFAULT_VALUE_MESSAGE_DIGEST_ALGORITHM = MessageDigestAlgoritmhs.SHA256;
    private static final String WAITING_TIME_AFTER_BLOCK_THREAD_FAILURE_IN_SECONDS = "waitingTimeAfterBlockThreadFailure";
    private static final Long DEFAULT_WAITING_TIME_AFTER_BLOCK_THREAD_FAILURE_IN_SECONDS = 3L;

    public static boolean acceptUntrustedCertificate() {
        return configurationMap.getBooleanProperty(globalGroup, ACCEPT_UNTRUSTED_CERTIFICATE,
                DEFAULT_VALUE_ACCEPT_UNTRUSTED_CERTIFICATE);
    }

    /**
     * @return
     */
    public static boolean connectToVslm() {
        return configurationMap.getBooleanProperty(globalGroup, CONNECT_VSLM, DEFAULT_VALUE_CONNECT_VSLM);
    }

    public static void createConfigFolder(final Class<? extends Object> obj) throws IOException, URISyntaxException {
        final String appData = GuestOsUtils.getAppData();
        final File confDirectory = new File(appData + File.separatorChar + "conf");
        if (!confDirectory.isDirectory()) {
            confDirectory.mkdirs();
            Utility.copyFromResource(obj, confDirectory, "/config.properties");
            Utility.copyFromResource(CoreGlobalSettings.class, confDirectory, "/vddk.conf");
            Utility.copyFromResource(CoreGlobalSettings.class, confDirectory, "/cacerts");

            KeyStoreHelper.generateKeyPair(confDirectory);

        } else {
            if (!(new File(confDirectory + "/config.properties").exists())) {
                Utility.copyFromResource(obj, confDirectory, "/config.properties");
            }
            if (!(new File(confDirectory + "/vddk.conf").exists())) {
                Utility.copyFromResource(obj, confDirectory, "/vddk.conf");
            }
            if (!(new File(confDirectory + "/cacerts").exists())) {
                Utility.copyFromResource(obj, confDirectory, "/cacerts");
            }
            if (!KeyStoreHelper.doesKeyStoreExist(confDirectory)) {
                KeyStoreHelper.generateKeyPair(confDirectory);
            }
        }

        CoreGlobalSettings.setConfigPath(confDirectory.toString());

    }

    /**
     * Set Log folder
     *
     * @throws URISyntaxException
     */
    public static void createLogFolder() throws URISyntaxException {
        final String appData = GuestOsUtils.getAppData();
        final File logDirectory = new File(appData + File.separatorChar + "log");
        CoreGlobalSettings.setLogsPath(logDirectory.toString());
        if (!logDirectory.isDirectory()) {
            logDirectory.mkdirs();
        }
    }

    public static boolean defaulConfigPropertiesFileExist() {
        final File configPropertyFile = new File(getDefaulConfigPropertiesFile());
        return configPropertyFile.exists();
    }

    public static boolean excludeBackupServer() {
        return configurationMap.getBooleanProperty(globalGroup, EXCLUDE_BACKUP_SERVER, DEFAULT_EXCLUDE_BACKUP_SERVER);
    }

    public static String getArachniServer() {
        return configurationMap.getStringProperty(k8sGroup, ARACHNI_SERVER);
    }

    public static Integer getArachniServerPort() {
        return configurationMap.getIntegerProperty(k8sGroup, ARACHNI_SERVER_PORT, 8080);
    }

    private static String getCertificatePath() {

        return getConfigPath();
    }

    public static String getCertPath() {
        final String certPath = getCertificatePath() + File.separatorChar + getKeystore();
        return certPath.replace('\\', '/');
    }

    public static String getConfigPath() {
        if (StringUtils.isEmpty(configPath)) {
            return getInstallPath() + File.separatorChar + CONFIG_DIRECTORY;
        } else {
            return configPath;
        }
    }

    public static File getDaemonPidFile() {
        return new File(getDefaultDaemonPidFile());
    }

    public static String getDefaulConfigPropertiesFile() {
        return getConfigPath() + File.separatorChar + CONFIG_PROPERTIES_FILENAME;
    }

    public static String getDefaultDaemonPidFile() {
        return getConfigPath() + File.separatorChar + DAEMON_PID;
    }

    public static String getDefaultProfileVmPath(final String moref) {
        return String.format("%s/%s", moref, CoreGlobalSettings.FCO_PROFILE_FILE_NAME);
    }

    public static Integer getQuisceTimeout() {
        return configurationMap.getIntegerProperty(globalGroup, QUISCE_TIMEOUT, DEFAULT_VALUE_QUISCE_TIMEOUT);
    }

    public static Boolean getDefaultUSeWindowsVss() {
        return configurationMap.getBooleanProperty(globalGroup, USE_VSS_WINDOWS_SYSTEM,
                DEFAULT_VALUE_USE_VSS_WINDOWS_SYSTEM);
    }

    public static WindowsQuiesceSpecVssBackupContext getDefaultVssBackupContext() {
        return WindowsQuiesceSpecVssBackupContext.parse(
                configurationMap.getStringProperty(globalGroup, VSS_BACKUP_CONTEXT, DEFAULT_VALUE_VSS_BACKUP_CONTEXT));
    }

    public static WindowsQuiesceSpecVssBackupType getDefaultVssBackupType() {
        return WindowsQuiesceSpecVssBackupType
                .parse(configurationMap.getStringProperty(globalGroup, VSS_BACKUP_TYPE, DEFAULT_VALUE_VSS_BACKUP_TYPE));
    }

    public static Boolean getDefaultVssBootableSystemState() {
        return configurationMap.getBooleanProperty(globalGroup, VSS_BOOTABLE_SYSTEM_STATE,
                DEFAULT_VALUE_VSS_BOOTABLE_SYSTEM_STATE);
    }

    public static Boolean getDefaultVssPartialFileSupport() {
        return configurationMap.getBooleanProperty(globalGroup, VSS_PARTIAL_FILE_SUPPORT,
                DEFAULT_VALUE_VSS_PARTIAL_FILE_SUPPORT);
    }

    public static Boolean getDefaultVssRetryOnFailure() {
        return configurationMap.getBooleanProperty(globalGroup, VSS_RETRY_ON_FAILURE,
                DEFAULT_VALUE_VSS_RETRY_ON_FAILURE);
    }

    public static String getGlobalProfileFileName() {
        return GLOBAL_PROFILE_FILE_NAME;
    }

    public static String getInstallPath() {
        final File f = new File(new File(new File(new File(".").getAbsolutePath()).getParent()).getParent());
        final String installPath = f.getPath();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Installation Directory:" + installPath);
        }
        return installPath;
    }

    public static int getKeepGenerations() {
        return configurationMap.getIntegerProperty(globalGroup, KEEP_GENERATIONS, DEFAULT_VALUE_KEEP_GENERATIONS);
    }

    private static String getKeystore() {
        return configurationMap.getStringProperty(globalGroup, KEYSTORE, DEFAULT_VALUE_KEYSTORE);
    }

    public static String getKeyStorePassword() {
        String result = configurationMap.getStringProperty(globalGroup, KEY_STORE_PASSWORD,
                DEFAULT_VALUE_KEY_STORE_PASSWORD);
        if (!result.equals(DEFAULT_VALUE_KEY_STORE_PASSWORD) && useBase64Passwd()) {
            result = new String(Base64.decodeBase64(result));
        }
        return result;
    }

    public static String getKeystorePath() {
        return getCertificatePath() + File.separatorChar + getKeystore();
    }

    public static String getKeystoreCaCertsPath() {
        return getCertificatePath() + File.separatorChar + "cacerts";
    }

    public static String getKeyStoreCaCertsPassword() {

        return "changeit";
    }

    public static String getKeyStoreSslAlias() {
        return configurationMap.getStringProperty(globalGroup, KEY_STORE_SSL_CERTIFICATE_ALIAS,
                DEFAULT_KEY_STORE_SSL_ALIAS);
    }

    /**
     * @return
     */
    public static String getKeyStoreSslCertificatePassword() {
        String result = configurationMap.getStringProperty(globalGroup, KEY_STORE_SSL_CERTIFICATE_PASSWORD,
                getKeyStorePassword());
        if (!result.equals(getKeyStorePassword()) && useBase64Passwd()) {
            result = new String(Base64.decodeBase64(result));
        }
        return result;
    }

    public static String getKeyStoreType() {
        return configurationMap.getStringProperty(globalGroup, KEY_STORE_TYPE, DEFAULT_VALUE_KEY_STORE_TYPE);
    }

    public static String getKeyStoreUserCertificateAlias() {
        return configurationMap.getStringProperty(globalGroup, KEY_STORE_USER_CERTIFICATE_ALIAS,
                DEFAULT_KEY_STORE_USER_CERTIFICATE_ALIAS);
    }

    public static String getLogsPath() {
        if (StringUtils.isEmpty(logsPath)) {
            return getInstallPath() + File.separatorChar + CONFIG_DIRECTORY;
        } else {
            return logsPath;
        }
    }

    /**
     * @return
     */
    public static int getMaxBlockOperationRetries() {
        return configurationMap.getIntegerProperty(globalGroup, MAX_VDDK_READ_RETRIES, DEFAULT_MAX_VDDK_READ_RETRIES);
    }

    public static int getMaxBlockSize() {
        return configurationMap.getIntegerProperty(globalGroup, MAX_BLOCK_SIZE_MB, DEFAULT_VALUE_MAX_BLOCK_SIZE_MB);
    }

    public static int getMaxGetThreadsPool() {
        return configurationMap.getIntegerProperty(globalGroup, MAX_GET_THREADS_POOL,
                DEFAULT_VALUE_MAX_GET_THREADS_POOL);
    }

    /**
     * @return
     */
    public static int getMaxPostDumpRetries() {
        return configurationMap.getIntegerProperty(globalGroup, MAX_POST_DUMP_RETRIES, DEFAULT_MAX_POST_DUMP_RETRIES);
    }

    public static int getMaxPostThreadsPool() {
        return configurationMap.getIntegerProperty(globalGroup, MAX_POST_THREADS_POOL,
                DEFAULT_VALUE_MAX_POST_THREADS_POOL);
    }

    public static MessageDigestAlgoritmhs getMessageDigestAlgorithm() {
        final String st = configurationMap.getStringProperty(globalGroup, MESSAGE_DIGEST_ALGORITHM,
                DEFAULT_VALUE_MESSAGE_DIGEST_ALGORITHM.toString());

        MessageDigestAlgoritmhs res = MessageDigestAlgoritmhs.parse(st);
        if (res == null) {
            res = MessageDigestAlgoritmhs.SHA1;
        }
        return res;
    }

    public static String getRefreshToken() {
        return configurationMap.getStringProperty(globalGroup, CSP_REFRESH_TOKEN);
    }

    public static String getRpFilter() {
        return configurationMap.getStringProperty(filterGroup, VM_RESOURCE_POOL_FILTER,
                DEFAULT_VALUE_VM_RESOURCE_POOL_FILTER);
    }

    public static Boolean getTargetCustomValueAsBool(final String pstrSection, final String key) {

        Boolean result = configurationMap.getBooleanProperty(pstrSection, key);
        if ((result == null)) {
            result = false;
        }
        return result;
    }

    public static Boolean getTargetCustomValueAsBool(final String pstrSection, final String key,
            final boolean defaultValue) {
        return configurationMap.getBooleanProperty(pstrSection, key, defaultValue);
    }

    public static Integer getTargetCustomValueAsInt(final String pstrSection, final String key,
            final Integer defaultValue) {
        return configurationMap.getIntegerProperty(pstrSection, key, defaultValue);
    }

    public static String getTargetCustomValueAsString(final String pstrSection, final String key) {
        String result = configurationMap.getStringProperty(pstrSection, key);
        if (StringUtils.isEmpty(result)) {
            result = null;
        }
        return result;
    }

    public static String getTargetCustomValueAsString(final String pstrSection, final String key,
            final String defaultValue) {
        return configurationMap.getStringProperty(pstrSection, key, defaultValue);
    }

    public static String getTaskDirectory() {
        return getInstallPath() + File.separatorChar + TASK_DIRECTORY;
    }

    /**
     * @return
     */
    public static Integer getTaskMaxWaitSeconds() {
        return configurationMap.getIntegerProperty(globalGroup, TASK_MAX_WAIT_SECONDS,
                DEFAULT_VALUE_TASK_MAX_WAIT_SECONDS);
    }

    public static long getTicketLifeExpectancyInMilliSeconds() {
        Long result = configurationMap.getLongProperty(globalGroup, SSO_TICKET_LIFE_EXPECTANCY_IN_SECONDS,
                DEFAULT_VALUE_TICKET_LIFE_EXPECTANCY_IN_SECONDS);
        result *= Utility.ONE_SECOND_IN_MILLIS;
        return result;
    }

    public static long getWaitingTimeAfterBlockThreadFailureInMilliSeconds() {
        Long result = configurationMap.getLongProperty(globalGroup, WAITING_TIME_AFTER_BLOCK_THREAD_FAILURE_IN_SECONDS,
                DEFAULT_WAITING_TIME_AFTER_BLOCK_THREAD_FAILURE_IN_SECONDS);
        result *= Utility.ONE_SECOND_IN_MILLIS;
        return result;
    }

    public static String getVddkConfig() {
        return getConfigPath() + File.separatorChar
                + configurationMap.getStringProperty(globalGroup, VDDK_CONFIG, DEFAULT_VALUE_VDDK_CONFIG);

    }

    public static String getVddkLibPath() {
        return configurationMap.getStringProperty(globalGroup, VDDK_LIB_PATH,
                getInstallPath() + File.separatorChar + ((GuestOsUtils.isWindows()) ? "bin" : "lib"));
    }

    /**
     * @return
     */
    public static String getVddkVersion() {
        return configurationMap.getStringProperty(globalGroup, USE_VDDK_VERSION);
    }

    public static String getVmFilter() {
        return configurationMap.getStringProperty(filterGroup, VM_FOLDER_FILTER, DEFAULT_VALUE_VM_FOLDER_FILTER);
    }

    public static Integer getWindowsVssTimeOut() {
        return configurationMap.getIntegerProperty(globalGroup, WINDOWS_VSS_TIMEOUT, DEFAULT_VALUE_VSS_TIMEOUT_SECONDS);
    }

    public static String getX509CertFile() {

        return getCertificatePath() + File.separatorChar + X509_CERT_FILENAME;

    }

    public static String getX509CertPrivateKeyFile() {
        return getCertificatePath() + File.separatorChar + X509_CERT_PRIVATE_KEY_FILENAME;
    }

    public static boolean isAutoConfigureCbtOn() {
        return configurationMap.getBooleanProperty(globalGroup, AUTO_CONFIGURE_CBT, DEFAULT_AUTO_CONFIGURE_CBT);
    }

    public static boolean isCipherEnable() {
        return configurationMap.getBooleanProperty(globalGroup, ENABLE_CIPHER, DEFAULT_VALUE_ENABLE_CIPHER);
    }

    public static boolean isCompressionEnable() {
        return configurationMap.getBooleanProperty(globalGroup, ENABLE_COMPRESSION, DEFAULT_VALUE_ENABLE_COMPRESSION);
    }

    public static boolean isEmptyConfig() {
        return emptyConfig;
    }

    public static boolean isEnableForAccessOn() {
        return configurationMap.getBooleanProperty(globalGroup, ENABLE_FOR_ACCESS_ON, DEFAULT_ENABLE_FOR_ACCESS_ON);

    }

    public static boolean isForceSnapBeforeRestore() {
        return configurationMap.getBooleanProperty(globalGroup, FORCE_SNAPSHOT_BEFORE_RESTORE,
                DEFAULT_VALUE_FORCE_SNAPSHOT_BEFORE_RESTORE);
    }

    /**
     * @return
     */
    public static boolean isVddkOverwriteOnStart() {
        return configurationMap.getBooleanProperty(globalGroup, OVERWRITE_VDDK_ON_START,
                DEFAULT_OVERWRITE_VDDK_ON_START);
    }

    /**
     * @return
     */
    public static boolean isVddkRemovedOnExit() {
        return configurationMap.getBooleanProperty(globalGroup, DELETE_VDDK_ON_EXIT, DEFAULT_DELETE_VDDK_ON_EXIT);
    }

    public static boolean loadConfig(final File configPropertyFile) throws IOException {
        CoreGlobalSettings.configPropertyFile = configPropertyFile;

        if (configPropertyFile.exists()) {
            configurationMap.clear();
            configurationMap.loadPropertyFile(configPropertyFile);
            emptyConfig = false;
        } else {
            emptyConfig = true;
        }
        return true;
    }

    public static void loadLogSetting() throws IOException {

        LogManager.getLogManager().reset();
        final Logger globalLogger = Logger.getLogger("");
        final Handler[] handlers = globalLogger.getHandlers();
        for (final Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }
        final String logFile = String.format("%s%s%s", getLogsPath(), File.separator, "safekeeping.log");
        final File logDir = new File(getLogsPath());
        if (!logDir.isDirectory()) {
            logDir.mkdirs();
        }
        final FileHandler filehandler = new FileHandler(logFile, 1024 * 1024, 10);
        filehandler.setFormatter(new VmbkLogFormatter());
        globalLogger.addHandler(filehandler);
    }

    /**
     * @param configPath the configPath to set
     */
    public static void setConfigPath(final String configPath) {
        CoreGlobalSettings.configPath = configPath;
    }

    public static void setLogsPath(final String logsPath) {
        CoreGlobalSettings.logsPath = logsPath;
    }

    /**
     * Set the default VDDK version to use
     *
     * @param version
     * @throws IOException
     */
    public static void setVddkVersion(final String version) {
        configurationMap.setStringProperty(globalGroup, USE_VDDK_VERSION, version);
    }

    /**
     * Set the default VDDK version to use and save the new configuration file
     *
     * @param version
     * @throws IOException
     */
    public static void setVddkVersionPermanently(final String version) throws IOException {
        configurationMap.setStringProperty(globalGroup, USE_VDDK_VERSION, version);
        configurationMap.savePropertyFile(configPropertyFile);
    }

    public static boolean skipEncryptionCheck() {
        return configurationMap.getBooleanProperty(globalGroup, SKIP_ENCRYPTION_CHECK,
                DEFAULT_VALUE_SKIP_ENCRYPTION_CHECK);
    }

    public static boolean useBase64Passwd() {
        return configurationMap.getBooleanProperty(globalGroup, USE_BASE64_PASSWD, DEFAULT_VALUE_USE_BASE64_PASSWD);
    }

    public static boolean useQueryAllocatedBlocks() {
        return configurationMap.getBooleanProperty(globalGroup, USE_QUERY_ALLOCACATED_BLOCKS_KEY,
                DEFAULT_VALUE_USE_QUERY_ALLOCACATED_BLOCKS);
    }

    public static boolean useQueryAllocatedBlocksForIncremental() {
        return configurationMap.getBooleanProperty(globalGroup, USE_QUERY_ALLOCACATED_BLOCKS_FOR_INCREMENTAL_KEY,
                DEFAULT_VALUE_USE_QUERY_ALLOCACATED_BLOCKS_FOR_INCREMENTAL);

    }

    public static void write() throws IOException {
        configurationMap.savePropertyFile(configPropertyFile);
    }

    protected CoreGlobalSettings() {
    }

}
