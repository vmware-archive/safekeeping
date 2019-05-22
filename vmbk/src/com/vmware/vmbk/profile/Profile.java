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

import java.io.IOException;
import java.util.logging.Logger;

import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.type.ByteArrayInOutStream;

abstract class Profile {
    protected static final String META_GROUP = "meta";
    private static final Double _PROFILE_VERSION = 1.1;
    private static final String PROFILE_GROUP = "profile";
    private static final String PROFILE_VERSION_KEY = "version";
    protected static final Logger logger = Logger.getLogger(Profile.class.getName());

    protected String groupMeta = META_GROUP;
    protected final MultiValuesMap valuesMap;

    public Profile() {
	this.valuesMap = new MultiValuesMap();
	initializeGroups();
	setProfileVersion();
    }

    Profile(final byte[] byteArray) throws IOException {
	this.valuesMap = new MultiValuesMap();
	this.valuesMap.load(byteArray);
	final Double metaVersion = this.valuesMap.getDoubleProperty(PROFILE_GROUP, PROFILE_VERSION_KEY, 0.1);
	if (metaVersion.compareTo(_PROFILE_VERSION) != 0) {
	    IoFunction.showWarning(logger,
		    "Profile format version %.1f can be not compatible with this software version (%.1f)", metaVersion,
		    _PROFILE_VERSION);
	}
	initializeGroups();

    }

    abstract protected void initializeGroups();

    public void load(final byte[] byteArray) throws IOException {
	this.valuesMap.clear();
	this.valuesMap.load(byteArray);
    }

    protected void setProfileVersion() {
	this.valuesMap.setDoubleProperty(PROFILE_GROUP, PROFILE_VERSION_KEY, _PROFILE_VERSION);
    }

    public ByteArrayInOutStream toByteArrayInOutputStream() throws IOException {
	setProfileVersion();
	return this.valuesMap.toByteArrayInOutputStream();
    }
}
