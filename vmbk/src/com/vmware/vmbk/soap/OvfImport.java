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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OvfCreateImportSpecParams;
import com.vmware.vim25.OvfCreateImportSpecResult;
import com.vmware.vim25.OvfFileItem;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VimPortType;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.type.ManagedEntityInfo;
import com.vmware.vmbk.util.Utility;

/**
 * @author mdaneri
 *
 */
public class OvfImport {
    class HttpNfcLeaseExtender implements Runnable {
	private ManagedObjectReference httpNfcLease = null;
	private VimPortType vimPort = null;
	private int progressPercent = 0;

	public HttpNfcLeaseExtender(final ManagedObjectReference mor, final VimPortType vimport) {
	    this.httpNfcLease = mor;
	    this.vimPort = vimport;
	}

	@Override
	public void run() {
	    try {
		Thread.sleep(10000);
		int previousProgressPercent = 0;
		int isTen = 0;
		IoFunction.println();
		IoFunction.print("0%|");
		IoFunction.print(StringUtils.repeat("---------", "|", 10));
		IoFunction.println("|100%");
		IoFunction.print("   ");
		while (!OvfImport.this.vmdkFlag) {

		    if (OvfImport.this.TOTAL_BYTES != 0) {
			this.progressPercent = (int) ((OvfImport.this.TOTAL_BYTES_WRITTEN * 100)
				/ (OvfImport.this.TOTAL_BYTES));
		    }
		    try {
			this.vimPort.httpNfcLeaseProgress(this.httpNfcLease, this.progressPercent);
			for (int i = 0; i < (this.progressPercent - previousProgressPercent); i++) {
			    if (++isTen > 9) {
				IoFunction.print('O');
				isTen = 0;
			    } else {
				IoFunction.print('o');
			    }
			}
			previousProgressPercent = this.progressPercent;
			Thread.sleep(30000);
		    } catch (final InterruptedException e) {
			if (this.progressPercent != 100) {
			    for (int i = this.progressPercent; i < 99; i++) {
				IoFunction.print('o');
			    }
			    IoFunction.print('O');
			}
			IoFunction.println();
			IoFunction.showInfo(logger, "Done");
			break;
		    } catch (final SOAPFaultException sfe) {
			printSoapFaultException(sfe);
			break;
		    }
		}
	    } catch (final SOAPFaultException sfe) {
		printSoapFaultException(sfe);
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
    }

    private static final Logger logger = Logger.getLogger(OvfImport.class.getName());

    private final VimConnection conn;
    boolean vmdkFlag = false;

    volatile long TOTAL_BYTES = 0;

    volatile long TOTAL_BYTES_WRITTEN = 0;

    HttpNfcLeaseExtender leaseExtender;
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

    HttpURLConnection getHTTPConnection(final URL url)
	    throws NoSuchAlgorithmException, KeyManagementException, IOException {

	final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
	// Maintain session
//	@SuppressWarnings("unchecked")
	// final List<String> cookies = (List<String>) this.headers.get("Set-cookie");

	this.cookieValue = this.conn.getCookie();// cookies.get(0);
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

    void getVMDKFile(final boolean put, final URL readFileLocation, final URL url, final long diskCapacity) {
	HttpsURLConnection writeConnection = null;
	BufferedOutputStream writeBufferedOutputStream = null;

	int bytesRead, bufferSize;
	byte[] buffer;
	final int maxBufferSize = 64 * 1024;

	try {
	    logger.info("Destination host URL: " + url.toString());
//	    final URL url = new URL(writeFileLocation);
	    writeConnection = (HttpsURLConnection) url.openConnection();

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

	    writeBufferedOutputStream = new BufferedOutputStream(writeConnection.getOutputStream());
	    logger.info("Local file path: " + readFileLocation);
	    final HttpURLConnection readConnection = getHTTPConnection(readFileLocation);
	    final InputStream readInputStream = readConnection.getInputStream();
	    final BufferedInputStream readBufferedInputStream = new BufferedInputStream(readInputStream);

	    bufferSize = maxBufferSize;
	    buffer = new byte[bufferSize];
	    while ((bytesRead = readBufferedInputStream.read(buffer)) != -1) {
		this.TOTAL_BYTES_WRITTEN += bytesRead;
		writeBufferedOutputStream.write(buffer, 0, bytesRead);

		writeBufferedOutputStream.flush();
	    }
	    logger.info("Total bytes written: " + this.TOTAL_BYTES_WRITTEN + "/" + diskCapacity);
	    try {
		final DataInputStream dis = new DataInputStream(writeConnection.getInputStream());
		dis.close();
	    } catch (final SocketTimeoutException stex) {
		IoFunction.showWarning(logger, "From (ServerResponse): " + stex);
	    } catch (final IOException ioex) {
		IoFunction.showWarning(logger, "From (ServerResponse): " + ioex);
	    }
	    // IoFunction.showInfo(logger, "Writing vmdk to the output stream done");
	    readBufferedInputStream.close();
	} catch (final IOException | KeyManagementException | NoSuchAlgorithmException ex) {

	} finally {
	    try {
		writeBufferedOutputStream.flush();
		writeBufferedOutputStream.close();
		writeConnection.disconnect();
	    } catch (final SOAPFaultException sfe) {
		printSoapFaultException(sfe);
	    } catch (final Exception e) {
		logger.warning(Utility.toString(e));
	    }
	}
    }

    public void importVApp(final URL urlPath, final String vappName, final String host, final String datastore,
	    final String resourcePool, final String vmFolder) {

	try {
	    ManagedObjectReference dsMor = null;
	    ManagedObjectReference rpMor = null;
	    ManagedObjectReference vmFolderMor = null;
	    ManagedEntityInfo hostMor = null;
	    ManagedEntityInfo dcMor = null;
	    Map<String, ManagedObjectReference> results = null;
	    try {
		results = this.conn.morefHelper.inFolderByType(this.conn.getRootFolder(), "HostSystem",
			new RetrieveOptions());
	    } catch (final RuntimeFaultFaultMsg e) {
		e.printStackTrace();
	    } catch (final InvalidPropertyFaultMsg e) {
		e.printStackTrace();
	    }

	    if (StringUtils.isNotEmpty(resourcePool)) {
		rpMor = this.conn.findByInventoryPath(resourcePool);
	    }

	    if (StringUtils.isNotEmpty(host)) {
		hostMor = new ManagedEntityInfo(host, results.get(host), this.conn.getServerIntanceUuid());
	    }

	    if ((hostMor == null) && (rpMor != null)) {
		final ManagedObjectReference computeResource = (ManagedObjectReference) this.conn.morefHelper
			.entityProps(rpMor, "owner");
		final ArrayOfManagedObjectReference hosts = (ArrayOfManagedObjectReference) this.conn.morefHelper
			.entityProps(computeResource, "host");
		if (hosts.getManagedObjectReference().size() > 0) {
		    final ManagedObjectReference mor = hosts.getManagedObjectReference().get(0);
		    hostMor = new ManagedEntityInfo(this.conn.morefHelper.entityName(mor), mor,
			    this.conn.getServerIntanceUuid());

		}
	    }
	    if (hostMor == null) {
		throw new RuntimeException("Host System " + host + " Not Found.");
	    }

	    final Map<String, Object> hostProps = this.conn.morefHelper.entityProps(hostMor.getMoref(),
		    new String[] { "datastore", "parent" });
	    final List<ManagedObjectReference> dsList = ((ArrayOfManagedObjectReference) hostProps.get("datastore"))
		    .getManagedObjectReference();
	    if (dsList.isEmpty()) {
		throw new RuntimeException("No Datastores accesible from host " + host);
	    }
	    if (datastore == null) {
		dsMor = dsList.get(0);
	    } else {
		for (final ManagedObjectReference ds : dsList) {
		    if (datastore.equalsIgnoreCase(
			    (String) this.conn.morefHelper.entityProps(ds, new String[] { "name" }).get("name"))) {
			dsMor = ds;
			break;
		    }
		}
	    }
	    if (dsMor == null) {
		if (datastore != null) {
		    throw new RuntimeException(
			    "No Datastore by name " + datastore + " is accessible from host " + host);
		}
		throw new RuntimeException("No Datastores accesible from host " + host);
	    }
//
//	    if (StringUtils.isNotEmpty(resourcePool)) {
//		rpMor = this.conn.findByInventoryPath(resourcePool);
//	    }
	    if (rpMor == null) {
		rpMor = this.conn.getResourcePoolByHost(hostMor.getMoref());
	    }

	    dcMor = this.conn.getDatacenterByMoref(dsMor);
	    if (StringUtils.isNotEmpty(vmFolder)) {
		vmFolderMor = this.conn.findByInventoryPath(vmFolder);
	    }
	    if (vmFolder == null) {
		vmFolderMor = (ManagedObjectReference) this.conn.morefHelper
			.entityProps(dcMor.getMoref(), new String[] { "vmFolder" }).get("vmFolder");
	    }
	    final OvfCreateImportSpecParams importSpecParams = createImportSpecParams(hostMor.getMoref(), vappName);

	    final String ovfDescriptor = getOvfDescriptorFromUrl(urlPath);
	    if ((ovfDescriptor == null) || ovfDescriptor.isEmpty()) {
		return;
	    }

	    final OvfCreateImportSpecResult ovfImportResult = this.conn.getVimPort().createImportSpec(
		    this.conn.getServiceContent().getOvfManager(), ovfDescriptor, rpMor, dsMor, importSpecParams);
	    if ((ovfImportResult.getError() == null) || ovfImportResult.getError().isEmpty()) {
		final List<OvfFileItem> fileItemArr = ovfImportResult.getFileItem();
		if (fileItemArr != null) {
		    for (final OvfFileItem fi : fileItemArr) {
			printOvfFileItem(fi);
			this.TOTAL_BYTES += fi.getSize();
		    }
		}
		IoFunction.showInfo(logger, "Total bytes to import:" + this.TOTAL_BYTES);
		if (ovfImportResult != null) {
		    final ManagedObjectReference httpNfcLease = this.conn.getVimPort().importVApp(rpMor,
			    ovfImportResult.getImportSpec(), vmFolderMor, hostMor.getMoref());
		    final Object[] result = this.conn.waitForValues.wait(httpNfcLease, new String[] { "state" },
			    new String[] { "state" },
			    new Object[][] { new Object[] { HttpNfcLeaseState.READY, HttpNfcLeaseState.ERROR } });
		    if (result[0].equals(HttpNfcLeaseState.READY)) {
			logger.info("HttpNfcLeaseState: " + result[0]);
			final HttpNfcLeaseInfo httpNfcLeaseInfo = (HttpNfcLeaseInfo) this.conn.morefHelper
				.entityProps(httpNfcLease, new String[] { "info" }).get("info");
			printHttpNfcLeaseInfo(httpNfcLeaseInfo);
			this.leaseExtender = new HttpNfcLeaseExtender(httpNfcLease, this.conn.getVimPort());
			this.vmdkFlag = false;
			final Thread t = new Thread(this.leaseExtender);
			t.start();
			final List<HttpNfcLeaseDeviceUrl> deviceUrlArr = httpNfcLeaseInfo.getDeviceUrl();
			for (final HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
			    final String deviceKey = deviceUrl.getImportKey();
			    for (final OvfFileItem ovfFileItem : fileItemArr) {
				if (deviceKey.equals(ovfFileItem.getDeviceId())) {
				    logger.info("Import key: " + deviceKey);
				    logger.info("OvfFileItem device id: " + ovfFileItem.getDeviceId());
				    logger.info("HTTP Post file: " + ovfFileItem.getPath());

				    String absoluteFile = urlPath.toString().substring(0,
					    urlPath.toString().lastIndexOf("/"));
				    absoluteFile = absoluteFile + "/" + ovfFileItem.getPath();
				    logger.info("Absolute path: " + absoluteFile);
				    final URL absoluteFileURL = new URL(absoluteFile);
				    final URL writeFileLocationURL = new URL(
					    deviceUrl.getUrl().replace("*", hostMor.getName()));
				    getVMDKFile(ovfFileItem.isCreate(), absoluteFileURL, writeFileLocationURL,
					    ovfFileItem.getSize());
				    logger.info("Completed uploading the VMDK file");
				}
			    }
			}
			this.vmdkFlag = true;
			t.interrupt();
			this.conn.getVimPort().httpNfcLeaseProgress(httpNfcLease, 100);
			this.conn.getVimPort().httpNfcLeaseComplete(httpNfcLease);
		    } else {
			IoFunction.showWarning(logger, "HttpNfcLeaseState not ready");
			for (final Object o : result) {
			    IoFunction.showWarning(logger, "HttpNfcLeaseState: " + o);
			}
		    }
		}
	    } else {
		IoFunction.showWarning(logger, "Cannot import the vAPP because of following:");
		for (final LocalizedMethodFault fault : ovfImportResult.getError()) {
		    IoFunction.showWarning(logger, fault.getLocalizedMessage());
		}
	    }
	} catch (final SOAPFaultException sfe) {
	    printSoapFaultException(sfe);
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
    }

    void printHttpNfcLeaseInfo(final HttpNfcLeaseInfo info) {

	logger.info("########################################################");
	logger.info("HttpNfcLeaseInfo");
	// logger.info("cookie: " + info.getCookie());
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

    void printSoapFaultException(final SOAPFaultException sfe) {
	logger.warning("SOAP Fault -");
	if (sfe.getFault().hasDetail()) {
	    logger.warning(sfe.getFault().getDetail().getFirstChild().getLocalName());
	}
	if (sfe.getFault().getFaultString() != null) {
	    logger.warning("Message: " + sfe.getFault().getFaultString());
	}
    }

}
