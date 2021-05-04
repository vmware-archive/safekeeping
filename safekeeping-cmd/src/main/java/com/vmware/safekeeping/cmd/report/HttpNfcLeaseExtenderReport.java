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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.report.HttpNfcLeaseExtenderRunningReport;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.soap.OvfImport;

public class HttpNfcLeaseExtenderReport extends HttpNfcLeaseExtenderRunningReport {
	private static class ProgressPercentual {
		private int progress;
		private int isMultipleOfTen;
	}

	private static final Logger logger = Logger.getLogger(HttpNfcLeaseExtenderReport.class.getName());

	/**
	 * @param ovfImport
	 */
	public HttpNfcLeaseExtenderReport(final OvfImport ovfImport) {
		super(ovfImport);
		ovfImport.setLeaseExtender(this);
	}

	void logSoapFaultException(final SOAPFaultException sfe) {
		logger.warning("SOAP Fault -");
		if (sfe.getFault().hasDetail()) {
			logger.warning(sfe.getFault().getDetail().getFirstChild().getLocalName());
		}
		if (sfe.getFault().getFaultString() != null) {
			logger.warning("Message: " + sfe.getFault().getFaultString());
		}
	}

	private void progressCounter(final ProgressPercentual percent) {
		try {
			this.ovfImport.httpNfcLeaseProgress(this.progressPercent);
			for (int i = 0; i < (this.progressPercent - percent.progress); ++i, ++percent.isMultipleOfTen) {
				if (percent.isMultipleOfTen > 9) {
					IoFunction.print('O');
					percent.isMultipleOfTen = 0;
				} else {
					IoFunction.print('o');
				}
			}
			percent.progress = this.progressPercent;
			Thread.sleep(Utility.THIRTY_SECONDS_IN_MILLIS);
		} catch (final InterruptedException e) {
			if (this.progressPercent != 100) {
				for (int i = this.progressPercent; i < 99; i++) {
					IoFunction.print('o');
				}
				IoFunction.print('O');
			}
			IoFunction.println();
			Thread.currentThread().interrupt();

		} catch (final SOAPFaultException sfe) {
			logSoapFaultException(sfe);

		}
	}

	@Override
	public void run() {
		try {
			initializeReport();
			Thread.sleep(Utility.TEN_SECONDS_IN_MILLIS);
			final ProgressPercentual progress = new ProgressPercentual();
			IoFunction.println();
			IoFunction.print("0%|");
			IoFunction.print(StringUtils.repeat("---------", "|", 10));
			IoFunction.println("|100%");
			IoFunction.print("   ");
			while (!this.ovfImport.isVmdkFlag()) {
				if (this.ovfImport.getTotalBytes() != 0) {
					this.progressPercent = this.ovfImport.getPercentage();
				}
				progressCounter(progress);
			}
		} catch (final SOAPFaultException sfe) {
			logSoapFaultException(sfe);
		} catch (final InterruptedException e) {
			logger.log(Level.WARNING, "Interrupted!", e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
	}
}
