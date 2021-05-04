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
package com.vmware.safekeeping.core.profile.ovf;

import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.VAppProductInfo;
import com.vmware.vim25.VAppProductSpec;

public class SerializableVAppProductInfo {

	private String appUrl;

	private String classId;

	private String fullVersion;

	private String instanceId;

	private int key;

	private String name;

	private String productUrl;

	private String vendor;

	private String vendorUrl;

	private String version;

	public SerializableVAppProductInfo() {
	}

	SerializableVAppProductInfo(final VAppProductInfo vAppProductInfo) {
		this.appUrl = vAppProductInfo.getAppUrl();
		this.classId = vAppProductInfo.getClassId();
		this.fullVersion = vAppProductInfo.getFullVersion();
		this.instanceId = vAppProductInfo.getInstanceId();
		this.key = vAppProductInfo.getKey();
		this.name = vAppProductInfo.getName();
		this.productUrl = vAppProductInfo.getProductUrl();
		this.vendor = vAppProductInfo.getVendor();
		this.vendorUrl = vAppProductInfo.getVendorUrl();
		this.version = vAppProductInfo.getVersion();
	}

	/**
	 * @return the appUrl
	 */
	public String getAppUrl() {
		return this.appUrl;
	}

	/**
	 * @return the classId
	 */
	public String getClassId() {
		return this.classId;
	}

	/**
	 * @return the fullVersion
	 */
	public String getFullVersion() {
		return this.fullVersion;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return this.instanceId;
	}

	/**
	 * @return the key
	 */
	public int getKey() {
		return this.key;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the productUrl
	 */
	public String getProductUrl() {
		return this.productUrl;
	}

	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return this.vendor;
	}

	/**
	 * @return the vendorUrl
	 */
	public String getVendorUrl() {
		return this.vendorUrl;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * @param appUrl the appUrl to set
	 */
	public void setAppUrl(final String appUrl) {
		this.appUrl = appUrl;
	}

	/**
	 * @param classId the classId to set
	 */
	public void setClassId(final String classId) {
		this.classId = classId;
	}

	/**
	 * @param fullVersion the fullVersion to set
	 */
	public void setFullVersion(final String fullVersion) {
		this.fullVersion = fullVersion;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(final String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(final int key) {
		this.key = key;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param productUrl the productUrl to set
	 */
	public void setProductUrl(final String productUrl) {
		this.productUrl = productUrl;
	}

	/**
	 * @param vendor the vendor to set
	 */
	public void setVendor(final String vendor) {
		this.vendor = vendor;
	}

	/**
	 * @param vendorUrl the vendorUrl to set
	 */
	public void setVendorUrl(final String vendorUrl) {
		this.vendorUrl = vendorUrl;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	public VAppProductSpec toVAppProductSpec() {
		final VAppProductInfo vAppProductInfo = new VAppProductInfo();
		vAppProductInfo.setAppUrl(this.appUrl);
		vAppProductInfo.setClassId(this.classId);
		vAppProductInfo.setInstanceId(this.instanceId);
		vAppProductInfo.setFullVersion(this.fullVersion);
		vAppProductInfo.setKey(this.key);
		vAppProductInfo.setName(this.name);
		vAppProductInfo.setProductUrl(this.productUrl);
		vAppProductInfo.setVendor(this.vendor);
		vAppProductInfo.setVendorUrl(this.vendorUrl);
		vAppProductInfo.setVersion(this.version);

		final VAppProductSpec vAppProductSpec = new VAppProductSpec();
		vAppProductSpec.setInfo(vAppProductInfo);
		vAppProductSpec.setOperation(ArrayUpdateOperation.ADD);
		return vAppProductSpec;
	}

}
