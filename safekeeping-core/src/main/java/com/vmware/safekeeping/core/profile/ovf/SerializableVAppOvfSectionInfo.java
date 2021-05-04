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
import com.vmware.vim25.VAppOvfSectionInfo;
import com.vmware.vim25.VAppOvfSectionSpec;

public class SerializableVAppOvfSectionInfo {

	private Boolean atEnvelopeLevel;

	private String contents;

	private Integer key;

	private String namespace;

	private String type;

	public SerializableVAppOvfSectionInfo() {
	}

	public SerializableVAppOvfSectionInfo(final VAppOvfSectionInfo vAppOvfSectionInfo) {
		this.contents = vAppOvfSectionInfo.getContents();
		this.key = vAppOvfSectionInfo.getKey();
		this.namespace = vAppOvfSectionInfo.getNamespace();
		this.type = vAppOvfSectionInfo.getType();
		this.atEnvelopeLevel = vAppOvfSectionInfo.isAtEnvelopeLevel();
	}

	/**
	 * @return the atEnvelopeLevel
	 */
	public Boolean getAtEnvelopeLevel() {
		return this.atEnvelopeLevel;
	}

	/**
	 * @return the contents
	 */
	public String getContents() {
		return this.contents;
	}

	/**
	 * @return the key
	 */
	public Integer getKey() {
		return this.key;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @param atEnvelopeLevel the atEnvelopeLevel to set
	 */
	public void setAtEnvelopeLevel(final Boolean atEnvelopeLevel) {
		this.atEnvelopeLevel = atEnvelopeLevel;
	}

	/**
	 * @param contents the contents to set
	 */
	public void setContents(final String contents) {
		this.contents = contents;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(final Integer key) {
		this.key = key;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	public VAppOvfSectionSpec toVAppOvfSectionSpec() {
		final VAppOvfSectionInfo vAppOvfSectionInfo = new VAppOvfSectionInfo();
		vAppOvfSectionInfo.setContents(this.contents);
		vAppOvfSectionInfo.setKey(this.key);
		vAppOvfSectionInfo.setNamespace(this.namespace);
		vAppOvfSectionInfo.setType(this.type);
		vAppOvfSectionInfo.setAtEnvelopeLevel(this.atEnvelopeLevel);

		final VAppOvfSectionSpec vAppOvfSectionSpec = new VAppOvfSectionSpec();
		vAppOvfSectionSpec.setInfo(vAppOvfSectionInfo);
		vAppOvfSectionSpec.setOperation(ArrayUpdateOperation.ADD);
		return vAppOvfSectionSpec;
	}
}
