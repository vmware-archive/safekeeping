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
package com.vmware.safekeeping.core.soap.helpers;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import com.vmware.vsphereautomation.lookup.LookupServiceContent;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationAttribute;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationEndpoint;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationEndpointType;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationFilter;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationInfo;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationServiceType;
import com.vmware.vsphereautomation.lookup.LsPortType;
import com.vmware.vsphereautomation.lookup.LsService;
import com.vmware.vsphereautomation.lookup.ManagedObjectReference;
import com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg;

public class LookupServiceHelper {
	private class MultipleManagementNodeException extends Exception {

		private static final long serialVersionUID = -6179103331243513328L;
		Map<String, String> nodes;

		public MultipleManagementNodeException(final Map<String, String> nodes) {
			this.nodes = nodes;
		}

		@Override
		public String getMessage() {
			final String separator = System.getProperty("line.separator");
			String message = "Multiple Management Node Found on server";
			for (final String name : this.nodes.keySet()) {
				message += String.format(separator + "Node name: %s uuid: %s", name, this.nodes.get(name));
			}
			return message;
		}
	}

	private final LookupServiceContent lookupServiceContent;

	private final String lookupServiceUrl;
	private final LsPortType lsPort;

	private final ManagedObjectReference serviceInstanceRef;

	private final ManagedObjectReference serviceRegistration;

	public LookupServiceHelper(final URL url) throws RuntimeFaultFaultMsg {

		this.lookupServiceUrl = url.toString();

		this.serviceInstanceRef = new ManagedObjectReference();
		this.serviceInstanceRef.setType("LookupServiceInstance");
		this.serviceInstanceRef.setValue("ServiceInstance");

		final LsService lookupService = new LsService();
		this.lsPort = lookupService.getLsPort();
		((BindingProvider) this.lsPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				this.lookupServiceUrl);

		this.lookupServiceContent = this.lsPort.retrieveServiceContent(this.serviceInstanceRef);
		this.serviceRegistration = this.lookupServiceContent.getServiceRegistration();

	}

	private Map<String, String> findMgmtNodes() throws RuntimeFaultFaultMsg {
		final LookupServiceRegistrationServiceType filterServiceType = new LookupServiceRegistrationServiceType();
		filterServiceType.setProduct("com.vmware.cis");
		filterServiceType.setType("vcenterserver");

		final LookupServiceRegistrationEndpointType filterEndpointType = new LookupServiceRegistrationEndpointType();
		filterEndpointType.setProtocol("vmomi");
		filterEndpointType.setType("com.vmware.vim");

		final LookupServiceRegistrationFilter filterCriteria = new LookupServiceRegistrationFilter();
		filterCriteria.setServiceType(filterServiceType);
		filterCriteria.setEndpointType(filterEndpointType);

		final Map<String, String> retVal = new HashMap<>();
		final List<LookupServiceRegistrationInfo> results = this.lsPort.list(this.serviceRegistration, filterCriteria);
		for (final LookupServiceRegistrationInfo service : results) {
			for (final LookupServiceRegistrationAttribute serviceAttr : service.getServiceAttributes()) {
				if ("com.vmware.vim.vcenter.instanceName".equals(serviceAttr.getKey())) {
					retVal.put(serviceAttr.getValue(), service.getNodeId());
				}
			}
		}
		return retVal;
	}

	public String findSsoServer() throws RuntimeFaultFaultMsg {
		final Map<String, String> result = getServiceEndpointUrl("com.vmware.cis", "cs.identity",
				"com.vmware.cis.cs.identity.sso", "wsTrust", null);
		return result.values().toArray(new String[0])[0];
	}

	public String findSsoUrl() throws RuntimeFaultFaultMsg {
		final Map<String, String> result = getServiceEndpointUrl("com.vmware.cis", "cs.identity",
				"com.vmware.cis.cs.identity.sso", "wsTrust", null);
		return result.values().toArray(new String[0])[0];
	}

	public Map<String, String> findSsoUrls() throws RuntimeFaultFaultMsg {
		return getServiceEndpointUrl("com.vmware.cis", "cs.identity", "com.vmware.cis.cs.identity.sso", "wsTrust",
				null);
	}

	public String findVapiUrl(final String nodeId) throws RuntimeFaultFaultMsg {
		final Map<String, String> result = getServiceEndpointUrl("com.vmware.cis", "cs.vapi",
				"com.vmware.vapi.endpoint", "vapi.json.https.public", nodeId);
		return result.get(nodeId);
	}

	public Map<String, String> findVapiUrls() throws RuntimeFaultFaultMsg {
		return getServiceEndpointUrl("com.vmware.cis", "cs.vapi", "com.vmware.vapi.endpoint", "vapi.json.https.public",
				null);
	}

	public String findVimPbmUrl(final String nodeId) throws RuntimeFaultFaultMsg {
		final Map<String, String> result = getServiceEndpointUrl("com.vmware.vim.sms", "sms", "com.vmware.vim.pbm",
				"https", nodeId);
		return result.get(nodeId);
	}

	public Map<String, String> findVimPbmUrls() throws RuntimeFaultFaultMsg {
		return getServiceEndpointUrl("com.vmware.vim.sms", "sms", "com.vmware.vim.pbm", "https", null);
	}

	public String findVimUrl(final String nodeId) throws RuntimeFaultFaultMsg {
		final Map<String, String> result = getServiceEndpointUrl("com.vmware.cis", "vcenterserver", "com.vmware.vim",
				"vmomi", nodeId);
		return result.get(nodeId);
	}

	public Map<String, String> findVimUrls() throws RuntimeFaultFaultMsg {
		return getServiceEndpointUrl("com.vmware.cis", "vcenterserver", "com.vmware.vim", "vmomi", null);
	}

	public Map<String, String> findVimVslmUrl() throws RuntimeFaultFaultMsg {
		return getServiceEndpointUrl("com.vmware.vim.sms", "sms", "com.vmware.vim.vslm", "https", null);
	}

	public String findVimVslmUrl(final String nodeId) throws RuntimeFaultFaultMsg {
		final Map<String, String> result = getServiceEndpointUrl("com.vmware.vim.sms", "sms", "com.vmware.vim.vslm",
				"https", nodeId);
		return result.get(nodeId);
	}

	public List<LookupServiceRegistrationInfo> getAnyService() {
		List<LookupServiceRegistrationInfo> results = null;
		try {
			final LookupServiceRegistrationFilter filterCriteria = new LookupServiceRegistrationFilter();
			results = this.lsPort.list(this.serviceRegistration, filterCriteria);

//	    for (final LookupServiceRegistrationInfo a : results) {
//		for (final LookupServiceRegistrationEndpoint b : a.getServiceEndpoints()) {
//		    System.out.println(b.getUrl());
//		    if (b.getUrl().contains("pbm")) {
//			System.out.println(b.getEndpointType().getProtocol());
//			System.out.println(b.getEndpointType().getType());
//			System.out.println(a.getServiceType().getProduct());
//			System.out.println(a.getServiceType().getType());
//		    } else if (b.getUrl().contains("vslm")) {
//			System.out.println(b.getEndpointType().getProtocol());
//			System.out.println(b.getEndpointType().getType());
//			System.out.println(a.getServiceType().getProduct());
//			System.out.println(a.getServiceType().getType());

//			 productType, final String serviceType,
//			    final String endpointType, final String endpointProtocol
//		    }
//		}
//	    }
		} catch (final RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	public String getDefaultMgmtNode() throws RuntimeFaultFaultMsg, MultipleManagementNodeException {
		final Map<String, String> nodes = findMgmtNodes();
		if (nodes.size() == 1) {
			return nodes.values().toArray(new String[0])[0];
		} else if (nodes.size() > 1) {
			throw new MultipleManagementNodeException(nodes);
		}
		throw new RuntimeException("No Management Node found");
	}

	public LookupServiceContent getLookupServiceContent() {
		return this.lookupServiceContent;
	}

	public String getLookupServiceUrl() {
		return this.lookupServiceUrl;
	}

	public LsPortType getLsPort() {
		return this.lsPort;
	}

	public String getMgmtNodeId(final String instanceName) throws RuntimeFaultFaultMsg {
		final Map<String, String> nodes = findMgmtNodes();
		return nodes.get(instanceName);
	}

	public String getMgmtNodeInstanceName(final String nodeId) throws RuntimeFaultFaultMsg {
		final Map<String, String> nodes = findMgmtNodes();
		for (final String name : nodes.keySet()) {
			if (nodeId.equals(nodes.get(name))) {
				return name;
			}
		}
		return null;
	}

	private Map<String, String> getServiceEndpointUrl(final String productType, final String serviceType,
			final String endpointType, final String endpointProtocol, final String mgmtNodeId)
			throws RuntimeFaultFaultMsg {
		final LookupServiceRegistrationServiceType filterServiceType = new LookupServiceRegistrationServiceType();
		filterServiceType.setProduct(productType);
		filterServiceType.setType(serviceType);

		final LookupServiceRegistrationEndpointType filterEndpointType = new LookupServiceRegistrationEndpointType();
		filterEndpointType.setProtocol(endpointProtocol);
		filterEndpointType.setType(endpointType);

		final LookupServiceRegistrationFilter filterCriteria = new LookupServiceRegistrationFilter();
		filterCriteria.setServiceType(filterServiceType);
		filterCriteria.setEndpointType(filterEndpointType);
		if (mgmtNodeId != null) {
			filterCriteria.setNodeId(mgmtNodeId);
		}
		final Map<String, String> retVal = new HashMap<>();
		final List<LookupServiceRegistrationInfo> results = this.lsPort.list(this.serviceRegistration, filterCriteria);
		for (final LookupServiceRegistrationInfo lookupServiceRegistrationInfo : results) {
			final LookupServiceRegistrationEndpoint lookupServiceRegistrationEndpoint = lookupServiceRegistrationInfo
					.getServiceEndpoints().get(0);
			if (lookupServiceRegistrationEndpoint != null) {
				final String nodeId = lookupServiceRegistrationInfo.getNodeId();
				final String url = lookupServiceRegistrationEndpoint.getUrl();
				retVal.put(nodeId, url);
			}
		}
		return retVal;
	}

	public ManagedObjectReference getServiceInstanceRef() {
		return this.serviceInstanceRef;
	}

	public ManagedObjectReference getServiceRegistration() {
		return this.serviceRegistration;
	}
}
