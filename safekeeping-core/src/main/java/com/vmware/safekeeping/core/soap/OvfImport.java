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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.HttpNfcLeaseExtenderRunningReport;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.exception.SafekeepingUnsupportedObjectException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.OvfCreateImportSpecParams;
import com.vmware.vim25.OvfCreateImportSpecResult;
import com.vmware.vim25.OvfFileItem;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TimedoutFaultMsg;
import com.vmware.vim25.VAppConfigInfo;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class OvfImport {

    private static final Logger logger = Logger.getLogger(OvfImport.class.getName());

    private final VimConnection conn;
    private boolean vmdkFlag;

    private long totalBytes;

    private long totalByteWritten;
    private HttpNfcLeaseInfo httpNfcLeaseInfo;
    private HttpNfcLeaseExtenderRunningReport leaseExtender;
    private String cookieValue = "";

    public OvfImport(final VimConnection conn) {
        this.conn = conn;
    }

    OvfCreateImportSpecParams createImportSpecParams(final ManagedObjectReference host, final String newVmName) {
        final OvfCreateImportSpecParams importSpecParams = new OvfCreateImportSpecParams();
        importSpecParams.setHostSystem(host);
        importSpecParams.setLocale("");
        importSpecParams.setEntityName(newVmName);
        importSpecParams.setDeploymentOption("");
        return importSpecParams;
    }

    HttpURLConnection getHTTPConnection(final URL url) throws IOException {

        final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        this.cookieValue = this.conn.getCookie();
        final StringTokenizer tokenizer = new StringTokenizer(this.cookieValue, ";");
        this.cookieValue = tokenizer.nextToken();
        final String pathData = "$" + tokenizer.nextToken();
        final String cookie = "$Version=\"1\"; " + this.cookieValue + "; " + pathData;

        // set the cookie in the new request header
        final Map<String, List<String>> map = new HashMap<>();
        map.put("Cookie", Collections.singletonList(cookie));

        ((BindingProvider) this.conn.getVimPort()).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, map);

        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(true);
        httpConnection.setAllowUserInteraction(true);
        httpConnection.setRequestProperty("Cookie", cookie);
        httpConnection.connect();
        return httpConnection;
    }

    public HttpNfcLeaseExtenderRunningReport getLeaseExtender() {
        return this.leaseExtender;
    }

    String getOvfDescriptorFromUrl(final URL ovfDescriptorUrl)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        final StringBuffer strContent = new StringBuffer("");
        int x;
        final HttpURLConnection connn = getHTTPConnection(ovfDescriptorUrl);
        final InputStream fis = connn.getInputStream();
        while ((x = fis.read()) != -1) {
            strContent.append((char) x);
        }
        return strContent + "";
    }

    /**
     * @return
     */
    public int getPercentage() {
        int result = (int) ((this.totalByteWritten * 100) / (this.totalBytes));
        if (result > 99) {
            result = 99;
        }
        return result;
    }

    public long getTotalBytes() {
        return this.totalBytes;
    }

    void getVMDKFile(final boolean put, final URL readFileLocation, final URL url, final long diskCapacity)
            throws IOException, KeyManagementException, NoSuchAlgorithmException, SafekeepingConnectionException {
        HttpsURLConnection writeConnection = null;

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Destination host URL: " + url.toString());
        }
        writeConnection = (HttpsURLConnection) url.openConnection();
        if (writeConnection == null) {
            throw new SafekeepingConnectionException("Connection URL:%s return an error", url.toString());
        }
        try {
            // Maintain session
            this.cookieValue = this.conn.getCookie();
            final StringTokenizer tokenizer = new StringTokenizer(this.cookieValue, ";");
            this.cookieValue = tokenizer.nextToken();
            final String path = "$" + tokenizer.nextToken();
            final String cookie = "$Version=\"1\"; " + this.cookieValue + "; " + path;

            // set the cookie in the new request header
            final Map<String, List<String>> map = new HashMap<>();
            map.put("Cookie", Collections.singletonList(cookie));
            ((BindingProvider) this.conn.getVimPort()).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                    map);

            final int maxBufferSize = 64 * 1024;
            writeConnection.setDoInput(true);
            writeConnection.setDoOutput(true);
            writeConnection.setUseCaches(false);
            writeConnection.setChunkedStreamingMode(maxBufferSize);
            if (put) {
                writeConnection.setRequestMethod("PUT");
            } else {
                writeConnection.setRequestMethod("POST");
            }
            writeConnection.setRequestProperty("Cookie", cookie);
            writeConnection.setRequestProperty("Connection", "Keep-Alive");
            writeConnection.setRequestProperty("Content-Type", "application/x-vnd.vmware-streamVmdk");
            writeConnection.setRequestProperty("Content-Length", String.valueOf(diskCapacity));

            try (BufferedOutputStream writeBufferedOutputStream = new BufferedOutputStream(
                    writeConnection.getOutputStream())) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Local file path: " + readFileLocation);
                }
                final HttpURLConnection readConnection = getHTTPConnection(readFileLocation);
                final InputStream readInputStream = readConnection.getInputStream();
                try (final BufferedInputStream readBufferedInputStream = new BufferedInputStream(readInputStream)) {

                    final int bufferSize = maxBufferSize;

                    final byte[] buffer = new byte[bufferSize];
                    int bytesRead;
                    while ((bytesRead = readBufferedInputStream.read(buffer)) != -1) {
                        this.totalByteWritten += bytesRead;
                        writeBufferedOutputStream.write(buffer, 0, bytesRead);

                        writeBufferedOutputStream.flush();
                    }
                    if (logger.isLoggable(Level.INFO)) {
                        final String msg = "Total bytes written: " + this.totalByteWritten + "/" + diskCapacity;

                        logger.info(msg);
                    }
                    try (final DataInputStream dis = new DataInputStream(writeConnection.getInputStream())) {
                        // Nothing to do
                    }
                }
            }
        } finally {
            writeConnection.disconnect();
        }
    }

    public int httpNfcLeaseProgress(final int progressPercent) {
        try {
            this.conn.getVimPort().httpNfcLeaseProgress(this.httpNfcLeaseInfo.getLease(), progressPercent);
        } catch (RuntimeFaultFaultMsg | TimedoutFaultMsg e) {
            Utility.logWarning(logger, e);
        }
        return progressPercent;
    }

    public ManagedFcoEntityInfo importVApp(final URL urlPath, final String vappName, final String host,
            final String datastore, final String resourcePool, final String vmFolder)
            throws RuntimeFaultFaultMsg, TimedoutFaultMsg, InvalidPropertyFaultMsg, ConcurrentAccessFaultMsg,
            FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidStateFaultMsg, TaskInProgressFaultMsg,
            VmConfigFaultFaultMsg, KeyManagementException, NoSuchAlgorithmException, IOException, DuplicateNameFaultMsg,
            InsufficientResourcesFaultFaultMsg, InvalidNameFaultMsg, OutOfBoundsFaultMsg,
            InvalidCollectorVersionFaultMsg, InterruptedException, SafekeepingUnsupportedObjectException,
            SafekeepingException, SafekeepingConnectionException {

        ManagedObjectReference rpMor = null;
        ManagedEntityInfo hostMor = null;
        Map<String, ManagedObjectReference> results = null;

        results = this.conn.getVimHelper().inFolderByType(this.conn.getRootFolder(), "HostSystem",
                new RetrieveOptions());
        if (StringUtils.isNotEmpty(resourcePool)) {
            rpMor = this.conn.getFind().findByInventoryPath(resourcePool);
        }

        if (StringUtils.isNotEmpty(host)) {
            hostMor = new ManagedEntityInfo(host, results.get(host), this.conn.getServerIntanceUuid());
        }

        if ((hostMor == null) && (rpMor != null)) {
            final ManagedObjectReference computeResource = (ManagedObjectReference) this.conn.getVimHelper()
                    .entityProps(rpMor, "owner");
            final ArrayOfManagedObjectReference hosts = (ArrayOfManagedObjectReference) this.conn.getVimHelper()
                    .entityProps(computeResource, "host");
            if (!hosts.getManagedObjectReference().isEmpty()) {
                final ManagedObjectReference mor = hosts.getManagedObjectReference().get(0);
                hostMor = new ManagedEntityInfo(this.conn.getVimHelper().entityName(mor), mor,
                        this.conn.getServerIntanceUuid());

            }
        }
        if (hostMor == null) {
            throw new SafekeepingException("Host System %s Not Found.", host);
        }

        final Map<String, Object> hostProps = this.conn.getVimHelper().entityProps(hostMor,
                new String[] { "datastore", "parent" });
        final List<ManagedObjectReference> dsList = ((ArrayOfManagedObjectReference) hostProps.get("datastore"))
                .getManagedObjectReference();
        if (dsList.isEmpty()) {
            throw new SafekeepingException("No Datastores accesible from host %s.", host);
        }
        ManagedObjectReference dsMor = null;
        if (datastore == null) {
            dsMor = dsList.get(0);
        } else {
            for (final ManagedObjectReference ds : dsList) {
                if (datastore.equalsIgnoreCase(
                        (String) this.conn.getVimHelper().entityProps(ds, new String[] { "name" }).get("name"))) {
                    dsMor = ds;
                    break;
                }
            }
        }
        if (dsMor == null) {
            if (datastore != null) {
                throw new SafekeepingException("No Datastore by name %s is accessible from host %s", datastore, host);
            }
            throw new SafekeepingException("No Datastores accesible from host %s.", host);
        }
        if (rpMor == null) {
            rpMor = this.conn.getResourcePoolByHost(hostMor.getMoref());
        }

        ManagedObjectReference vmFolderMor = null;
        ManagedEntityInfo dcMor = null;
        dcMor = this.conn.getDatacenterByMoref(dsMor);
        if (StringUtils.isNotEmpty(vmFolder)) {
            vmFolderMor = this.conn.getFind().findByInventoryPath(vmFolder);
        }
        if (vmFolder == null) {
            vmFolderMor = (ManagedObjectReference) this.conn.getVimHelper()
                    .entityProps(dcMor, new String[] { "vmFolder" }).get("vmFolder");
        }
        final String ovfDescriptor = getOvfDescriptorFromUrl(urlPath);
        if ((ovfDescriptor == null) || ovfDescriptor.isEmpty()) {
            throw new SafekeepingException("No OVF descriptor");
        }

        final OvfCreateImportSpecParams importSpecParams = createImportSpecParams(hostMor.getMoref(), vappName);

        final OvfCreateImportSpecResult ovfImportResult = this.conn.getVimPort().createImportSpec(
                this.conn.getServiceContent().getOvfManager(), ovfDescriptor, rpMor, dsMor, importSpecParams);
        if ((ovfImportResult.getError() == null) || ovfImportResult.getError().isEmpty()) {
            final List<OvfFileItem> fileItemArr = ovfImportResult.getFileItem();
            if (fileItemArr != null) {
                for (final OvfFileItem fi : fileItemArr) {
                    printOvfFileItem(fi);
                    if (fi.getSize() != -1) {
                        this.totalBytes += fi.getSize();
                    } else {
                        this.totalBytes += Integer.MAX_VALUE;
                    }
                }
            }
            final ManagedObjectReference httpNfcLease = this.conn.getVimPort().importVApp(rpMor,
                    ovfImportResult.getImportSpec(), vmFolderMor, hostMor.getMoref());
            final Object[] result = this.conn.getVimHelper().wait(httpNfcLease, new String[] { "state" },
                    new String[] { "state" },
                    new Object[][] { new Object[] { HttpNfcLeaseState.READY, HttpNfcLeaseState.ERROR } },
                    CoreGlobalSettings.getTaskMaxWaitSeconds());
            if (result[0] == HttpNfcLeaseState.READY) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("HttpNfcLeaseState: " + result[0]);
                }
                this.httpNfcLeaseInfo = (HttpNfcLeaseInfo) this.conn.getVimHelper()
                        .entityProps(httpNfcLease, new String[] { "info" }).get("info");
                printHttpNfcLeaseInfo(this.httpNfcLeaseInfo);
                this.vmdkFlag = false;
                final Thread t = new Thread(this.leaseExtender);
                t.start();
                final List<HttpNfcLeaseDeviceUrl> deviceUrlArr = this.httpNfcLeaseInfo.getDeviceUrl();
                for (final HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
                    final String deviceKey = deviceUrl.getImportKey();
                    for (final OvfFileItem ovfFileItem : fileItemArr) {
                        if (deviceKey.equals(ovfFileItem.getDeviceId())) {
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info("Import key: " + deviceKey);
                                logger.info("OvfFileItem device id: " + ovfFileItem.getDeviceId());
                                logger.info("HTTP Post file: " + ovfFileItem.getPath());
                            }
                            String absoluteFile = urlPath.toString().substring(0, urlPath.toString().lastIndexOf('/'));
                            absoluteFile = absoluteFile + "/" + ovfFileItem.getPath();
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info("Absolute path: " + absoluteFile);
                            }
                            final URL absoluteFileURL = new URL(absoluteFile);
                            final URL writeFileLocationURL = new URL(
                                    deviceUrl.getUrl().replace("*", hostMor.getName()));
                            getVMDKFile(ovfFileItem.isCreate(), absoluteFileURL, writeFileLocationURL,
                                    ovfFileItem.getSize());
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info("Completed uploading the VMDK file");
                            }
                        }
                    }
                }
                this.vmdkFlag = true;
                t.interrupt();

                this.conn.getVimPort().httpNfcLeaseProgress(httpNfcLease, 100);
                this.conn.getVimPort().httpNfcLeaseComplete(httpNfcLease);
            } else {
                final StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("HttpNfcLeaseState not ready\n");
                for (final Object o : result) {
                    errorMsg.append("HttpNfcLeaseState: ");
                    errorMsg.append(o);
                    errorMsg.append('\n');
                }
                throw new SafekeepingException(errorMsg.toString());
            }
        } else {

            final StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Cannot import the vAPP because of following:\n");
            for (final LocalizedMethodFault fault : ovfImportResult.getError()) {
                errorMsg.append(fault.getLocalizedMessage());
                errorMsg.append('\n');
            }
            throw new SafekeepingException(errorMsg.toString());
        }
        if (this.httpNfcLeaseInfo != null) {
            final ManagedObjectReference moref = this.httpNfcLeaseInfo.getEntity();
            String uuid = null;
            switch (moref.getType()) {
            case "VirtualApp":
                uuid = ((VAppConfigInfo) this.conn.getVimHelper().entityProps(moref, "vAppConfig")).getInstanceUuid();
                break;
            case "VirtualMachine":
                uuid = this.conn.getVirtualMachineConfigInfo(moref).getUuid();
                break;
            default:
                throw new SafekeepingUnsupportedObjectException(moref);
            }
            return new ManagedFcoEntityInfo(vappName, this.httpNfcLeaseInfo.getEntity(), uuid,
                    this.conn.getServerIntanceUuid());
        } else {
            return null;
        }
    }

    public boolean isVmdkFlag() {
        return this.vmdkFlag;
    }

    void printHttpNfcLeaseInfo(final HttpNfcLeaseInfo info) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("########################################################");
            logger.info("HttpNfcLeaseInfo");
            final List<HttpNfcLeaseDeviceUrl> deviceUrlArr = info.getDeviceUrl();
            for (final HttpNfcLeaseDeviceUrl durl : deviceUrlArr) {
                logger.info("Device URL Import Key: " + durl.getImportKey());
                logger.info("Device URL Key: " + durl.getKey());
                logger.info("Device URL : " + durl.getUrl());
                logger.info("Updated device URL: " + durl.getUrl().replace("*", "10.20.140.58"));
            }
            logger.info("Lease Timeout: " + info.getLeaseTimeout());
            logger.info("Total Disk capacity: " + info.getTotalDiskCapacityInKB());
            logger.info("########################################################");
        }
    }

    void printOvfFileItem(final OvfFileItem fi) {
        logger.info("##########################################################");
        logger.info("OvfFileItem");
        logger.info("chunkSize: " + fi.getChunkSize());
        logger.info("create: " + fi.isCreate());
        logger.info("deviceId: " + fi.getDeviceId());
        logger.info("path: " + fi.getPath());
        logger.info("size: " + fi.getSize());
        logger.info("##########################################################");
    }

    public void setLeaseExtender(final HttpNfcLeaseExtenderRunningReport leaseExtender) {
        this.leaseExtender = leaseExtender;
    }

    public void setTotalBytes(final long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setVmdkFlag(final boolean vmdkFlag) {
        this.vmdkFlag = vmdkFlag;
    }
}
