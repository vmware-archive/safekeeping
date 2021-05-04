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
package com.vmware.safekeeping.core.type.manipulator;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.vmware.safekeeping.core.profile.GenerationProfile;

public class VmxManipulator extends PropertiesFileManipulator {

	public VmxManipulator(final byte[] encoded) throws IOException {
		super(encoded);
	}

// TODO Remove unused code found by UCDetector
//     public VmxManipulator(final String vmxPath) throws IOException {
// 	super(vmxPath);
//     }

	public String getNvram() {
		return this.fileContent.get("nvram");
	}

	public void keepUuid() {
		this.fileContent.put("uuid.action", "\"keep\"");

	}

	public void prepareForRestore(final String newName) {
		this.fileContent.put("nvram", "\"" + newName + ".nvram\"");
		this.fileContent.put("displayName", "\"" + newName + "\"");
		this.fileContent.remove("uuid.bios");
		this.fileContent.remove("vc.uuid");
		this.fileContent.remove("migrate.hostLog");
		this.fileContent.remove("sched.swap.derivedName");
		this.fileContent.remove("uuid.location");

	}

	public void removeDisks(final GenerationProfile profGen) {
		final int num_disks = profGen.getNumberOfDisks();
		final LinkedHashMap<String, String> newVmxContent = new LinkedHashMap<>(this.fileContent);
		for (int i = 0; i < num_disks; i++) {
			final String controller = profGen.getVmxDiskEntry(i);
			for (final String key : newVmxContent.keySet()) {
				if (key.startsWith(controller)) {
					this.fileContent.remove(key);
				}

			}
		}
	}

}
