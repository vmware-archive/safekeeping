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

import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vim25.PropertySpec;

public class PropertySpecBuilder extends PropertySpec {
// TODO Remove unused code found by UCDetector
//     public PropertySpecBuilder addToPathSet(final Collection<String> paths) {
// 	init();
// 	this.pathSet.addAll(paths);
// 	return this;
//     }

	public PropertySpecBuilder all(final Boolean all) {
		this.setAll(all);
		return this;
	}

	private void init() {
		if (this.pathSet == null) {
			this.pathSet = new ArrayList<>();
		}
	}

	public PropertySpecBuilder pathSet(final String... paths) {
		init();
		this.pathSet.addAll(Arrays.asList(paths));
		return this;
	}

	public PropertySpecBuilder type(final EntityType type) {
		this.setType(type.toString());
		return this;
	}

	public PropertySpecBuilder type(final String type) {
		this.setType(type);
		return this;
	}
}
