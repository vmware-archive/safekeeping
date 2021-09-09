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
package com.vmware.safekeeping.cmd.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreAwsS3TargetOptions;
import com.vmware.safekeeping.core.command.options.CoreFileTargetOptions;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.control.target.AwsS3Target;
import com.vmware.safekeeping.core.control.target.FileTarget;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.manipulator.VddkConfManipulator;

public final class CmdGlobalSettings extends CoreGlobalSettings {
    private static final String G_PSC_PROVIDER = "pscProvider";
    private static final String G_CSP_PROVIDER = "cspProvider";
    private static final String G_CMDLINE = "cmdline";
    private static String pscProvider;

    private static String cmdLineGroup;
    private static String cspProviderGroup;
    private static final String INTERACTIVE_PROMPT = "prompt";

    private static final String DEFAULT_VALUE_INTERACTIVE_PROMPT = "# ";
    private static final String PSC_SERVER = "pscServer";
    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";
    private static final String SCRIPT_FOLDER = "scriptFolder";

    private static final String DEFAULT_VALUE_SCRIPT_FOLDER = "..\\sample_scripts";

    private static final Integer DEFAULT_VALUE_NFC_HOST_PORT = 902;
    private static final String NFCHOSTPORT = "nfchostport";

    private static final String REPOSITORY = "repository";

    private static final String DEFAULT_VALUE_REPOSITORY = "amazonS3";

    private static final String DEFAULT_VALUE_AUTHENTICATION_PROVIDER = G_PSC_PROVIDER;

    private static ITarget[] repositories;
    private static final String CSP_TOKEN_EXCHANGE_SERVER_KEY = "tokeExchangeServer";

    private static final String AUTHENTICATION_PROVIDER = "authenticationProvider";

    private static final String CSP_HOST = "cspHost";

    static {
        pscProvider = G_PSC_PROVIDER;
        cmdLineGroup = G_CMDLINE;
        cspProviderGroup = G_CSP_PROVIDER;
    }

    private static void configureDefault(final boolean vmcMode, final Map<String, String> confDefault) {
        if (vmcMode) {
            System.out.println("VMware Cloud on AWS detected");
            System.out.println("Importing default cloud settings");
            confDefault.put(TRANSPORT_FULL_MODE, DEFAULT_VALUE_VMC_TRANSPORT_MODE);
            confDefault.put(TRANSPORT_INC_MODE, DEFAULT_VALUE_VMC_TRANSPORT_MODE);
            configurationMap.setStringProperty(globalGroup, TRANSPORT_FULL_MODE, DEFAULT_VALUE_VMC_TRANSPORT_MODE);
            configurationMap.setStringProperty(globalGroup, TRANSPORT_INC_MODE, DEFAULT_VALUE_VMC_TRANSPORT_MODE);
            configurationMap.setStringProperty(globalGroup, TRANSPORT_MODE_RESTORE, DEFAULT_VALUE_VMC_TRANSPORT_MODE);

        } else {
            confDefault.put(TRANSPORT_FULL_MODE, DEFAULT_VALUE_TRANSPORT_FULL_MODE);
            confDefault.put(TRANSPORT_INC_MODE, DEFAULT_VALUE_TRANSPORT_INC_MODE);
            confDefault.put(VDDK_CONFIG, DEFAULT_VALUE_VDDK_CONFIG);
            if (StringUtils.isEmpty(configurationMap.getStringProperty(globalGroup, VDDK_CONFIG))) {
                configurationMap.setStringProperty(globalGroup, VDDK_CONFIG, DEFAULT_VALUE_VDDK_CONFIG);
            }
            if (StringUtils.isEmpty(configurationMap.getStringProperty(globalGroup, TRANSPORT_FULL_MODE))) {
                configurationMap.setStringProperty(globalGroup, TRANSPORT_FULL_MODE, DEFAULT_VALUE_TRANSPORT_FULL_MODE);
            }
            if (StringUtils.isEmpty(configurationMap.getStringProperty(globalGroup, TRANSPORT_INC_MODE))) {
                configurationMap.setStringProperty(globalGroup, TRANSPORT_INC_MODE, DEFAULT_VALUE_TRANSPORT_INC_MODE);
            }
            if (StringUtils.isEmpty(configurationMap.getStringProperty(globalGroup, TRANSPORT_MODE_RESTORE))) {
                configurationMap.setStringProperty(globalGroup, TRANSPORT_MODE_RESTORE,
                        DEFAULT_VALUE_TRANSPORT_MODE_RESTORE);
            }
        }

        confDefault.put(VM_FOLDER_FILTER, DEFAULT_VALUE_VM_FOLDER_FILTER);
        confDefault.put(VM_RESOURCE_POOL_FILTER, DEFAULT_VALUE_VM_RESOURCE_POOL_FILTER);
        if (StringUtils.isEmpty(configurationMap.getStringProperty(pscProvider, NFCHOSTPORT))) {
            configurationMap.setIntegerProperty(pscProvider, NFCHOSTPORT, DEFAULT_VALUE_NFC_HOST_PORT);
        }
        if (configurationMap.getBooleanProperty(globalGroup, USE_BASE64_PASSWD) == null) {
            configurationMap.setBooleanProperty(globalGroup, USE_BASE64_PASSWD, DEFAULT_VALUE_USE_BASE64_PASSWD);
        }
        if (StringUtils.isEmpty(configurationMap.getStringProperty(cmdLineGroup, INTERACTIVE_PROMPT))) {
            configurationMap.setStringProperty(cmdLineGroup, INTERACTIVE_PROMPT, DEFAULT_VALUE_INTERACTIVE_PROMPT);
        }

    }

    /**
     *
     * @param vmcMode
     * @return
     * @throws IOException
     */
    private static boolean finalizeConfiguration(final boolean vmcMode) throws IOException {

        try (VddkConfManipulator vddk = new VddkConfManipulator(
                getConfigPath() + File.separatorChar + DEFAULT_VALUE_VDDK_CONFIG)) {
            vddk.setNoNfcSession(vmcMode);
            vddk.enablePhoneHome(true);
            vddk.save();
        }
        write();
        return true;
    }

    /*
     * Get value of [global] AUTHENTICATION_PROVIDER.
     *
     * @return platformServCtrl when the entry is not found.
     */
    public static String getAuthenticationProvider() {
        return configurationMap.getStringProperty(globalGroup, AUTHENTICATION_PROVIDER,
                DEFAULT_VALUE_AUTHENTICATION_PROVIDER);

    }

    public static String getCspHost() {
        return configurationMap.getStringProperty(cspProviderGroup, CSP_HOST);
    }

    public static String getInteractivePrompt() {
        return configurationMap.getStringProperty(cmdLineGroup, INTERACTIVE_PROMPT, DEFAULT_VALUE_INTERACTIVE_PROMPT);

    }

    public static int getNfcHostPort() {
        return configurationMap.getIntegerProperty(pscProvider, NFCHOSTPORT, DEFAULT_VALUE_NFC_HOST_PORT);
    }

    public static String getPassword() {
        String result = configurationMap.getStringProperty(pscProvider, PASSWORD, StringUtils.EMPTY);
        if (useBase64Passwd()) {
            result = new String(Base64.decodeBase64(result));
        }
        return result;

    }

    public static ITarget getRepositoryTarget() {
        int defaultRepositoryIndex = 0;
        int index = 0;
        for (final ITarget repository : repositories) {
            if (repository.getTargetType().equalsIgnoreCase(getTargetRepository())) {
                return repository;
            }
            if (repository.isEnable()) {
                defaultRepositoryIndex = index;
            }
            ++index;
        }
        return repositories[defaultRepositoryIndex];
    }

    public static String getScriptFolder() {
        return configurationMap.getStringProperty(cmdLineGroup, SCRIPT_FOLDER, DEFAULT_VALUE_SCRIPT_FOLDER);

    }

    public static String getSsoServer() {
        return configurationMap.getStringProperty(pscProvider, PSC_SERVER);
    }

    public static String getTargetRepository() {
        return configurationMap.getStringProperty(globalGroup, REPOSITORY);
    }

    public static String getTokenExchangeServer() {
        return configurationMap.getStringProperty(cspProviderGroup, CSP_TOKEN_EXCHANGE_SERVER_KEY);

    }

    public static String getUsername() {
        return configurationMap.getStringProperty(pscProvider, USERNAME);

    }

    public static void initTargets() throws URISyntaxException {
        final CoreFileTargetOptions fileOption = new CoreFileTargetOptions();
        fileOption.setRoot(
                CoreGlobalSettings.getTargetCustomValueAsString(FileTarget.TARGET_TYPE_NAME, FileTarget.ROOT_FOLDER));
        if (StringUtils.isEmpty(fileOption.getRoot())) {
            fileOption.setRoot(FileTarget.getDefaultPathFileArchive());
        }
        fileOption.setActive(getTargetRepository().equals(FileTarget.TARGET_TYPE_NAME));
        fileOption.setName("Default_" + FileTarget.TARGET_TYPE_NAME);
        fileOption.setEnable(getTargetCustomValueAsBool(FileTarget.TARGET_TYPE_NAME, FileTarget.ACTIVE_KEY));
        final CoreAwsS3TargetOptions s3Option = new CoreAwsS3TargetOptions();
        s3Option.setRegion(getTargetCustomValueAsString(AwsS3Target.TARGET_TYPE_NAME, AwsS3Target.REGION_NAME));
        s3Option.setBacket(getTargetCustomValueAsString(AwsS3Target.TARGET_TYPE_NAME, AwsS3Target.BACKET_NAME));
        s3Option.setAccessKey(getTargetCustomValueAsString(AwsS3Target.TARGET_TYPE_NAME, AwsS3Target.S3_ACCESS_KEY));
        s3Option.setSecretKey(getTargetCustomValueAsString(AwsS3Target.TARGET_TYPE_NAME, AwsS3Target.S3_SECRET_KEY));
        s3Option.setBase64(useBase64Passwd());

        s3Option.setActive(getTargetRepository().equals(AwsS3Target.TARGET_TYPE_NAME));
        s3Option.setName("Default_" + AwsS3Target.TARGET_TYPE_NAME);
        s3Option.setEnable(
                CoreGlobalSettings.getTargetCustomValueAsBool(AwsS3Target.TARGET_TYPE_NAME, AwsS3Target.ACTIVE_KEY));

        repositories = new ITarget[] { new AwsS3Target(s3Option), new FileTarget(fileOption) };
    }

    public static boolean manualConfiguration(final boolean quiet)
            throws JVixException, IOException, SafekeepingException, URISyntaxException {
        BufferedReader br = null;
        SafekeepingVersion.initialize(null);
        final Reader r = new InputStreamReader(System.in);
        br = new BufferedReader(r);
        String str = null;
        boolean vmcMode = false;
        final Map<String, String> confDefault = new LinkedHashMap<>();
        final Map<String, String> confGlobalParams = new LinkedHashMap<>();
        final Map<String, String> confVcenterParams = new LinkedHashMap<>();
        final Map<String, String> confFilterParams = new LinkedHashMap<>();
        final Map<String, String> confComLineParams = new LinkedHashMap<>();
        final Map<String, Map<String, String>> confParams = new LinkedHashMap<>();

        confDefault.put(TRANSPORT_FULL_MODE, DEFAULT_VALUE_TRANSPORT_FULL_MODE);
        confDefault.put(TRANSPORT_INC_MODE, DEFAULT_VALUE_TRANSPORT_INC_MODE);

        confDefault.put(MAX_POST_THREADS_POOL, DEFAULT_VALUE_MAX_POST_THREADS_POOL.toString());
        confDefault.put(MAX_GET_THREADS_POOL, DEFAULT_VALUE_MAX_GET_THREADS_POOL.toString());
        confDefault.put(MAX_BLOCK_SIZE_MB, DEFAULT_VALUE_MAX_BLOCK_SIZE_MB.toString());
        confDefault.put(KEY_STORE_TYPE, DEFAULT_VALUE_KEY_STORE_TYPE);
        confDefault.put(KEY_STORE_PASSWORD, DEFAULT_VALUE_KEY_STORE_PASSWORD);
        confDefault.put(KEYSTORE, DEFAULT_VALUE_KEYSTORE);
        confDefault.put(ACCEPT_UNTRUSTED_CERTIFICATE, DEFAULT_VALUE_ACCEPT_UNTRUSTED_CERTIFICATE.toString());
        confDefault.put(ENABLE_COMPRESSION, DEFAULT_VALUE_ENABLE_COMPRESSION.toString());
        confDefault.put(REPOSITORY, DEFAULT_VALUE_REPOSITORY);
        confDefault.put(NFCHOSTPORT, DEFAULT_VALUE_NFC_HOST_PORT.toString());
        confDefault.put(USE_BASE64_PASSWD, DEFAULT_VALUE_USE_BASE64_PASSWD.toString());
        for (final ITarget repository : repositories) {
            confDefault.putAll(repository.defaultConfigurations());
        }
        String pscServer = "";
        if (quiet) {
            try {
                pscServer = configurationMap.getStringProperty(pscProvider, PSC_SERVER);
                vmcMode = pscServer.contains(".vmwarevmc.com") || pscServer.contains(".vmc.vmware.com");
                configureDefault(vmcMode, confDefault);
                return finalizeConfiguration(vmcMode);
            } catch (final IOException e) {
                System.err.println(e.getLocalizedMessage());
                return false;
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
                        pscServer = configurationMap.getStringProperty(pscProvider, PSC_SERVER);
                    } while ((pscServer == null) || pscServer.isEmpty());
                    vmcMode = pscServer.contains(".vmwarevmc.com") || pscServer.contains(".vmc.vmware.com");
                    configureDefault(vmcMode, confDefault);

                } catch (final IOException e) {
                    System.err.println(e.getLocalizedMessage());
                    return false;
                }

                try {
                    confVcenterParams.clear();
                    confGlobalParams.clear();
                    confFilterParams.clear();
                    confComLineParams.clear();

                    final StringBuilder repString = new StringBuilder();
                    repString.append('(');
                    for (final ITarget repository : repositories) {
                        confParams.put(repository.getTargetType(), repository.manualConfiguration());
                        repString.append(repository.getTargetType());
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
                        if (StringUtils.isNotBlank(str) && PrettyBoolean.parseBoolean(str)) {
                            return finalizeConfiguration(vmcMode);
                        }

                    } while (!PrettyBoolean.isBoolean(str));
                } catch (final Exception e) {
                    System.err.println(e.getLocalizedMessage());
                    return false;
                }
                System.out.println();
            }
        }
    }

    private static void printParams(final Map<String, Map<String, String>> confParams) {
        System.out.println(new String(new char[50]).replace("\0", "\r\n"));
        for (final String group : confParams.keySet()) {
            System.out.println();
            System.out.println(group.toUpperCase(Utility.LOCALE));
            for (final String key : confParams.get(group).keySet()) {
                final String val = configurationMap.getStringProperty(group, key);
                System.out.println(String.format(confParams.get(group).get(key), val));
            }
        }
    }

    private static void readConfParams(final BufferedReader br, final Map<String, Map<String, String>> confParams,
            final Map<String, String> confDefault) throws IOException {

        String str = "";
        final java.io.Console cnsl = System.console();
        for (final String group : confParams.keySet()) {
            System.out.println();
            System.out.println(group.toUpperCase(Utility.LOCALE));
            for (final String key : confParams.get(group).keySet()) {
                String val = configurationMap.getStringProperty(group, key);
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
                    configurationMap.setStringProperty(group, key, str);
                } else {
                    configurationMap.setStringProperty(group, key, val);
                }
            }

        }
    }

    public static boolean reloadConfig() throws IOException, URISyntaxException {

        if (configPropertyFile.exists()) {
            configurationMap.clear();
            configurationMap.loadPropertyFile(configPropertyFile);
            emptyConfig = false;
        } else {
            emptyConfig = true;
        }
        initTargets();
        return true;
    }

    public static void testConnectionTo(final String aURL) throws Exception {
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
    }
}
