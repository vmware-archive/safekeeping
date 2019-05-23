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
package com.vmware.vmbk.profile.ovf;

import java.io.Serializable;

import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.VAppProductInfo;
import com.vmware.vim25.VAppProductSpec;

class SerializableVAppProductInfo implements Serializable {

    private static final long serialVersionUID = 7486817592998843245L;
    private final String appUrl;
    private final String classId;
    private final String fullVersion;
    private final String instanceId;
    private final int key;
    private final String name;
    private final String productUrl;
    private final String vendor;
    private final String vendorUrl;
    private final String version;

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

    VAppProductSpec toVAppProductSpec() {
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
