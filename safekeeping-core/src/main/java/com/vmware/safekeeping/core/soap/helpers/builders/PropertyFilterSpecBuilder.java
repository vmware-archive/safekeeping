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
package com.vmware.safekeeping.core.soap.helpers.builders;

import java.util.ArrayList;
import java.util.Arrays;

import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;

public class PropertyFilterSpecBuilder extends PropertyFilterSpec {
	private void init() {
		if (this.propSet == null) {
			this.propSet = new ArrayList<>();
		}
		if (this.objectSet == null) {
			this.objectSet = new ArrayList<>();
		}
	}

	public PropertyFilterSpecBuilder objectSet(final ObjectSpec... objectSpecs) {
		init();
		this.objectSet.addAll(Arrays.asList(objectSpecs));
		return this;
	}

	public PropertyFilterSpecBuilder propSet(final PropertySpec... propertySpecs) {
		init();
		this.propSet.addAll(Arrays.asList(propertySpecs));
		return this;
	}

// TODO Remove unused code found by UCDetector
//     public PropertyFilterSpecBuilder reportMissingObjectsInResults(final Boolean value) {
// 	this.setReportMissingObjectsInResults(value);
// 	return this;
//     }
}
