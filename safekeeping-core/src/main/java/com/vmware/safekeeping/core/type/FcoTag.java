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
package com.vmware.safekeeping.core.type;

import org.apache.commons.lang.StringUtils;

public class FcoTag {

	private String category;

	private String tag;
	private String tagId;
	private String categoryId;

	public FcoTag() {

	}

	public FcoTag(final String optionArg) {
		final String[] tempTag = optionArg.split(":");
		if (tempTag.length == 2) {
			this.setCategory(tempTag[0]);
		}
		this.setTag(tempTag[1]);

	}

	public String getCategory() {
		return this.category;
	}

	public String getCategoryId() {
		return this.categoryId;
	}

	public String getTag() {
		return this.tag;
	}

	public String getTagId() {
		return this.tagId;
	}

	public boolean isValid() {
		return StringUtils.isNotEmpty(this.getCategory()) && StringUtils.isNotEmpty(this.getTag());
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public void setCategoryId(final String categoryId) {
		this.categoryId = categoryId;
	}

	public void setTag(final String tag) {
		this.tag = tag;
	}

	public void setTagId(final String tagId) {
		this.tagId = tagId;
	}

}
