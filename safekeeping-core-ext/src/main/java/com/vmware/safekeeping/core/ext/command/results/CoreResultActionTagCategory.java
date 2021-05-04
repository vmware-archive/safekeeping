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
package com.vmware.safekeeping.core.ext.command.results;

import java.util.HashSet;
import java.util.Set;

import com.vmware.safekeeping.core.type.enums.TagCardinalityOptions;

public class CoreResultActionTagCategory extends CoreAbstractResultActionAutoClose {
	/**
     * 
     */
    private static final long serialVersionUID = 4617724134469338257L;

    private String categoryName;

	private String description;
	private TagCardinalityOptions cardinality;
	private Set<String> categoriesUrn = new HashSet<>();
	private Set<String> associableTypes;
	private String id;

	public Set<String> getAssociableTypes() {
		return this.associableTypes;
	}

	public TagCardinalityOptions getCardinality() {
		return this.cardinality;
	}

	public Set<String> getCategoriesUrn() {
		return this.categoriesUrn;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public String getDescription() {
		return this.description;
	}

	public String getId() {
		return this.id;
	}

	/**
	 * @param associableTypes
	 */
	public void setAssociableTypes(final Set<String> associableTypes) {
		this.associableTypes = associableTypes;

	}

	public void setCardinality(final TagCardinalityOptions cardinality) {
		this.cardinality = cardinality;
	}

	public void setCategoriesUrn(final Set<String> categoryId) {
		this.categoriesUrn = categoryId;
	}

	/**
	 * @param assetCategory
	 */
	public void setCategoryName(final String categoryName) {
		this.categoryName = categoryName;

	}

	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @param id
	 */
	public void setId(final String id) {
		this.id = id;

	}
}
