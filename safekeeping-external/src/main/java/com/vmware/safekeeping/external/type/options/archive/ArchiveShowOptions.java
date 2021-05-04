/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
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
package com.vmware.safekeeping.external.type.options.archive;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.core.command.AbstractArchiveCommand;
import com.vmware.safekeeping.core.command.options.CoreArchiveOptions;
import com.vmware.safekeeping.core.type.enums.ArchiveObjects;
import com.vmware.safekeeping.external.type.GenerationsFilter;
import com.vmware.safekeeping.external.type.options.AbstractBasicCommandOptions;

/**
 * @author mdaneri
 *
 */
public class ArchiveShowOptions extends AbstractArchiveOptions {

	public static void convert(final ArchiveShowOptions src, final CoreArchiveOptions dst) {
		if ((src == null) || (dst == null)) {
			return;
		}

		AbstractBasicCommandOptions.convert(src, dst, FirstClassObjectFilterType.any);
		dst.setShow(src.getArchiveObject());
		dst.setPrettyJason(src.isPrettyJason());
		switch (src.getFilter()) {
		case all:
			dst.getGenerationId().add(AbstractArchiveCommand.ALL_GENERATIONS);
			break;
		case failed:
			dst.getGenerationId().add(AbstractArchiveCommand.FAILED_GENERATIONS);
			break;
		case last:
			dst.getGenerationId().add(AbstractArchiveCommand.LAST_GENERATION);
			break;
		case list:
			if (src.getGenerationId() != null) {
				dst.getGenerationId().add(src.getGenerationId());
			}
			break;
		case succeded:
			dst.getGenerationId().add(AbstractArchiveCommand.SUCCEDED_GENERATIONS);
			break;
		default:
			break;

		}
	}

	private GenerationsFilter filter;

	private Integer generationId;

	private ArchiveObjects archiveObject;

	private boolean prettyJason;

	public ArchiveShowOptions() {
		this.archiveObject = ArchiveObjects.GLOBALPROFILE;
		this.filter = GenerationsFilter.list;
	}

	/**
	 * @return the archiveObject
	 */
	public ArchiveObjects getArchiveObject() {
		return this.archiveObject;
	}

	/**
	 * @return the filter
	 */
	public GenerationsFilter getFilter() {
		return this.filter;
	}

	/**
	 * @return the generationId
	 */
	public Integer getGenerationId() {
		return this.generationId;
	}

	/**
	 * @return the prettyJason
	 */
	public boolean isPrettyJason() {
		return this.prettyJason;
	}

	/**
	 * @param archiveObject the archiveObject to set
	 */
	public void setArchiveObject(final ArchiveObjects archiveObject) {
		this.archiveObject = archiveObject;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(final GenerationsFilter filter) {
		this.filter = filter;
	}

	/**
	 * @param generationId the generationId to set
	 */
	public void setGenerationId(final Integer generationId) {
		this.generationId = generationId;
	}

	/**
	 * @param prettyJason the prettyJason to set
	 */
	public void setPrettyJason(final boolean prettyJason) {
		this.prettyJason = prettyJason;
	}

}
