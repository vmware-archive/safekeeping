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
package com.vmware.safekeeping.cmd.report;

import java.util.logging.Logger;

import com.vmware.safekeeping.core.command.interactive.AbstractRemoveGenerationsInteractive;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.safekeeping.core.control.IoFunction;

/**
 * @author mdaneri
 *
 */
public class RemoveGenerationsInteractive extends AbstractRemoveGenerationsInteractive {
	protected static Logger logger = Logger.getLogger("RemoveGenerationsReport");

	/**
	 * @param rab
	 */
	public RemoveGenerationsInteractive(final CoreResultActionArchiveRemoveGenerationWithDependencies raargwd) {
		super(raargwd);
	}

	@Override
	public void endAccessArchive() {
		super.endAccessArchive();
	}

	@Override
	public void endDeleteGeneration() {
		if (getGeneration2Remove().isGenerationDataRemoved()) {
			IoFunction.print("[O]");
		} else {
			IoFunction.print("[X]");
		}
		super.endDeleteGeneration();
	}

	@Override
	public void endDeleteGenerations() {
		super.endDeleteGenerations();
		IoFunction.println();
	}

	@Override
	public void endRetrieveDependingGenerations() {
		super.endRetrieveDependingGenerations();
	}

	@Override
	public void endRetrieveRequestedGenerations() {
		super.endRetrieveRequestedGenerations();
	}

	@Override
	public void endUpdateFcoProfilesCatalog() {
		super.endUpdateFcoProfilesCatalog();
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void startAccessArchive() {
		super.startAccessArchive();
		IoFunction.showInfo(logger, "%s", getGenerationWithDependencies().getFcoToString());
	}

	@Override
	public void startDeleteGeneration(final CoreResultActionArchiveRemoveGeneration craarg) {
		super.startDeleteGeneration(craarg);
		IoFunction.printf(" %2d(%s) ", craarg.getGenId(),
				getGeneration2Remove().getDependenciesInfo().getMode().toString());

	}

	@Override
	public void startDeleteGenerations() {
		super.startDeleteGenerations();
	}

	@Override
	public void startRetrieveDependingGenerations() {
		super.startRetrieveDependingGenerations();
	}

	@Override
	public void startRetrieveRequestedGenerations() {
		super.startRetrieveRequestedGenerations();
	}

	@Override
	public void startUpdateFcoProfilesCatalog() {
		super.startUpdateFcoProfilesCatalog();
	}
}
