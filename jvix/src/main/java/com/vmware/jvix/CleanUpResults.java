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
package com.vmware.jvix;

public class CleanUpResults {
	private int numCleaned;
	private int numRemaining;
	private long vddkCallResult;

	/**
	 * @return the numCleaned
	 */
	public int getNumCleaned() {
		return this.numCleaned;
	}

	/**
	 * @return the numRemaining
	 */
	public int getNumRemaining() {
		return this.numRemaining;
	}

	/**
	 * @return the vddkCallResult
	 */
	public long getVddkCallResult() {
		return this.vddkCallResult;
	}

	/**
	 * @param numCleaned the numCleaned to set
	 */
	public void setNumCleaned(final int numCleaned) {
		this.numCleaned = numCleaned;
	}

	/**
	 * @param numRemaining the numRemaining to set
	 */
	public void setNumRemaining(final int numRemaining) {
		this.numRemaining = numRemaining;
	}

	/**
	 * @param vddkCallResult the vddkCallResult to set
	 */
	public void setVddkCallResult(final long vddkCallResult) {
		this.vddkCallResult = vddkCallResult;
	}
}
