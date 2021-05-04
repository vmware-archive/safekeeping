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

import com.vmware.safekeeping.core.command.interactive.AbstractCheckGenerationsInteractive;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.type.enums.BackupMode;

/**
 * @author mdaneri
 *
 */
public class CheckGenerationsInteractive extends AbstractCheckGenerationsInteractive {
	protected static Logger logger = Logger.getLogger("CheckGenerationsInteractive");

	/**
	 * @param rab
	 */
	public CheckGenerationsInteractive(final CoreResultActionArchiveCheckGenerationWithDependencies raargwd) {
		super(raargwd);
	}

	@Override
	public void endCheckFile(final int index) {
		super.endCheckFile(index);
		if (Boolean.TRUE.equals(getGeneration2Check().getMd5fileCheck().get(index))) {
			final int j1 = index + 1;
			final int numFiles = getGeneration2Check().getNumOfFiles();
			final int divider = 10;
			if ((numFiles - j1) >= (numFiles % divider)) {
				if (((j1 % divider) == 0)) {
					IoFunction.print('O');
				} else {
					IoFunction.print('o');
				}
			}
		} else {
			IoFunction.print('X');
		}
	}

	@Override
	public void endCheckGeneration() {
		IoFunction.printf("\t     -> Blocks %d Result: %s", getGeneration2Check().getNumOfFiles(),
				(getGeneration2Check().isSuccessful() ? "ok" : "currupted"));
		IoFunction.println();
		super.endCheckGeneration();
	}

	@Override
	public CoreResultActionArchiveCheckGenerationWithDependencies getGenerationWithDependencies() {
		return super.getGenerationWithDependencies();
	}

	@Override
	public void start() {
		super.start();
		IoFunction.printf("Check %s\n", getGenerationWithDependencies().getFcoToString());
	}

	@Override
	public void startAccessArchive() {
		super.startAccessArchive();
		IoFunction.print(getGenerationWithDependencies().getFcoToString());
	}

	@Override
	public void startCheckFiles() {
		super.startCheckFiles();
		IoFunction.print(String.format("Generation %03d Blocks %-4d:", getGeneration2Check().getGenId(),
				getGeneration2Check().getNumOfFiles()));
	}

	@Override
	public void startCheckGeneration(final CoreResultActionArchiveCheckGeneration craarg) {
		super.startCheckGeneration(craarg);
		final BackupMode mode = getGeneration2Check().getDependenciesInfo().getMode();
		if (mode == BackupMode.INCREMENTAL) {
			IoFunction.printf("     %2d(%s)\t", craarg.getGenId(), mode.toString());
		} else {
			IoFunction.printf(" %2d(%s)    \t", craarg.getGenId(), mode.toString());
		}

	}

	@Override
	public void startCheckGenerations() {
		super.startCheckGenerations();
		IoFunction.printf("%d generation(s) to check\n", getGenerationWithDependencies().getSubOperations().size());
	}

}
