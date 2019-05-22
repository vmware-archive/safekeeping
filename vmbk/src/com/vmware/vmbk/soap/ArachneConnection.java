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
/**
 *
 */
package com.vmware.vmbk.soap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.vim25.VStorageObjectSnapshotInfoVStorageObjectSnapshot;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.K8sIvdComponent;
import com.vmware.vmbk.util.Utility;

/**
 * @author mdaneri
 *
 */
public class ArachneConnection extends HttpConf implements IConnection {
    private static final Logger logger = Logger.getLogger(ArachneConnection.class.getName());
    private URL url;
    private final ConnectionManager connManager;
    private CloseableHttpClient httpClient;
    // private final String url = "http://10.160.137.172:8080";

    ArachneConnection(final ConnectionManager connManager, final String server, final int port)
	    throws MalformedURLException {

	this.url = new URL(String.format("http://%s:%d/api", server, port));
	this.connManager = connManager;
	// final HttpConfiguration httpConfig = buildHttpConfiguration();

    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#connect(org.w3c.dom.Element)
     */
    @Override
    public IConnection connect(final Element token) throws ConnectException {
	final int timeout = 100;
	final RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
		.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
	this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	return this;
    }

    public String createSnapshot(final String name) {

	final String lurl = String.format("%s/arachne/k8s/k8s:%s?snapshot", this.url, name);
	final HttpGet getRequest = new HttpGet(lurl);

	getRequest.addHeader("accept", "application/json");
	try {
	    final CloseableHttpResponse response = this.httpClient.execute(getRequest);
	    try {

		if (response.getStatusLine().getStatusCode() != 200) {
		    throw new RuntimeException(
			    "Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}
		final BufferedReader br = new BufferedReader(
			new InputStreamReader((response.getEntity().getContent())));

		final StringBuilder output = new StringBuilder();
		String in;
		while ((in = br.readLine()) != null) {
		    output.append(in);
		}
		logger.info("Arachne output:" + output.toString());
		final JsonNode jsonNode = new ObjectMapper().readTree(output.toString());

		return jsonNode.get("snapshotID").asText();
	    } catch (final ClientProtocolException e) {
		logger.warning(Utility.toString(e));

	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
	    } finally {
		response.close();
	    }
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));
	}
	return "";
    }

    @Override
    public IConnection disconnect() {
	this.httpClient = null;
	return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getCookie()
     */
    @Override
    public String getCookie() {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getCookieValue()
     */
    @Override
    public String getCookieValue() {
	// TODO Auto-generated method stub
	return null;
    }

    private byte[] getFile(final String url) throws IOException {
	byte[] result = null;
	final HttpGet httpget = new HttpGet(url);
	final CloseableHttpClient httpClient = HttpClients.createDefault();
	final HttpResponse response = httpClient.execute(httpget);
	final HttpEntity entity = response.getEntity();
	if (entity != null) {
	    // final long len = entity.getContentLength();
	    try (final BufferedInputStream is = new BufferedInputStream(entity.getContent())) {
		try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
		    int nRead;
		    final byte[] data = new byte[1024];
		    while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		    }
		    buffer.flush();
		    result = buffer.toByteArray();
		}
	    }
	}
	return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getHeaders()
     */
    @Override
    public Map getHeaders() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getHost() {
	return this.url.getHost();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getPassword()
     */
    @Override
    public String getPassword() {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getPort()
     */
    @Override
    public Integer getPort() {
	// TODO Auto-generated method stub
	return null;
    }

    public List<K8sIvdComponent> getSnapshotInfo(final String name, final String snapShotUuid) {
	final ArrayList<K8sIvdComponent> result = new ArrayList<>();
	// http://10.160.137.172:8080/api/arachne/k8s/k8s:nginx-example:fade8aea-65d0-45dd-95dc-5e01ff6a0aba
	final String lurl = String.format("%s/arachne/k8s/k8s:%s:%s", this.url, name, snapShotUuid);

	final CloseableHttpClient httpClient = HttpClients.createDefault();
	final HttpGet getRequest = new HttpGet(lurl);

	getRequest.addHeader("accept", "application/json");
	try {
	    final CloseableHttpResponse response = httpClient.execute(getRequest);
	    try {

		if (response.getStatusLine().getStatusCode() != 200) {
		    throw new RuntimeException(
			    "Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}
		final BufferedReader br = new BufferedReader(
			new InputStreamReader((response.getEntity().getContent())));

		final StringBuilder output = new StringBuilder();
		String in;
		while ((in = br.readLine()) != null) {
		    output.append(in);
		}
		logger.info("Arachne output:" + output.toString());
		final JsonNode jsonNode = new ObjectMapper().readTree(output.toString());

		final ArrayList<String> ivdList = new ArrayList<>();
		final JsonNode components = jsonNode.withArray("components");
		if (components.isArray()) {
		    final Iterator<JsonNode> iterator = jsonNode.withArray("components").elements();
		    while (iterator.hasNext()) {
			ivdList.add(iterator.next().toString());
		    }

		}
		final LinkedList<ImprovedVirtuaDisk> ivdFcoList = this.connManager.getAllIvdList();
		for (final String ivdUuid : ivdList) {
		    final String[] uuids = StringUtils.strip(ivdUuid, "\"").split(":");
		    if (uuids.length == 3) {
			for (final ImprovedVirtuaDisk ivdFco : ivdFcoList) {
			    if (ivdFco.getUuid().equals(uuids[1])) {
				final List<VStorageObjectSnapshotInfoVStorageObjectSnapshot> ivdSnaps = ivdFco
					.getSnapshots().getSnapshots();
				for (final VStorageObjectSnapshotInfoVStorageObjectSnapshot snap : ivdSnaps) {
				    if (snap.getId().getId().equals(uuids[2])) {
					final K8sIvdComponent ivdComp = new K8sIvdComponent();
					ivdComp.ivd = ivdFco;
					ivdComp.activeSnapshot = snap;
					result.add(ivdComp);
					break;
				    }
				}
			    }
			}
		    }

		}

	    } catch (final ClientProtocolException e) {
		logger.warning(Utility.toString(e));

	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
	    } finally {
		response.close();
	    }
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));
	}
	return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getUrl()
     */
    @Override
    public String getUrl() {

	return this.url.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getURL()
     */
    @Override
    public URL getURL() {
	// TODO Auto-generated method stub
	return this.url;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#getUsername()
     */
    @Override
    public String getUsername() {
	// TODO Auto-generated method stub
	return null;
    }

    public byte[] getZip(final String name, final String snapShotUuid) {
	byte[] result = null;
	final String lurl = String.format("%s/s3/k8s/k8s:%s:%s.zip", this.url, name, snapShotUuid);
	try {
	    result = getFile(lurl);
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	}
	// http://10.160.137.172:8080/api/s3/k8s/k8s:nginx-example:fade8aea-65d0-45dd-95dc-5e01ff6a0aba.zip
	return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#isConnected()
     */
    @Override
    public boolean isConnected() {
	return this.httpClient != null;
    }

    public List<String> list() {
	final ArrayList<String> result = new ArrayList<>();
	final String lurl = String.format("%s/arachne/k8s", this.url);
	final HttpGet getRequest = new HttpGet(lurl);

	getRequest.addHeader("accept", "application/json");
	try {
	    final CloseableHttpResponse response = this.httpClient.execute(getRequest);
	    try {

		if (response.getStatusLine().getStatusCode() != 200) {
		    throw new RuntimeException(
			    "Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}
		final BufferedReader br = new BufferedReader(
			new InputStreamReader((response.getEntity().getContent())));

		final StringBuilder output = new StringBuilder();
		String in;
		while ((in = br.readLine()) != null) {
		    output.append(in);
		}
		logger.info("Arachne output:" + output.toString());
		final JsonNode jsonNode = new ObjectMapper().readTree(output.toString());

		if (jsonNode.isArray()) {
		    final Iterator<JsonNode> iterator = jsonNode.elements();
		    while (iterator.hasNext()) {
			final String st = (StringUtils.strip(iterator.next().toString(), "\"").split(":"))[1];
			result.add(st);
		    }

		}

//    		map = new ObjectMapper().readValue(output, new TypeReference<HashMap<String, String>>() {
//    		});
		//

		// return jsonNode.get("snapshotID").asText();
	    } catch (final ClientProtocolException e) {
		logger.warning(Utility.toString(e));

	    } catch (final IOException e) {
		logger.warning(Utility.toString(e));
	    } finally {
		response.close();
	    }
	} catch (final IOException e) {
	    logger.warning(Utility.toString(e));
	}
	return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(final String password) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.soap.IConnection#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(final String url) throws MalformedURLException {
	this.url = new URL(url);

    } /*
       * (non-Javadoc)
       *
       * @see com.vmware.vmbk.soap.IConnection#setUsername(java.lang.String)
       */

    @Override
    public void setUsername(final String username) {
	// TODO Auto-generated method stub

    }
}
