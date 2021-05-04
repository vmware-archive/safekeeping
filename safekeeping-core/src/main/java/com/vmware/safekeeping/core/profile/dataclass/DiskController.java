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
package com.vmware.safekeeping.core.profile.dataclass;

import com.vmware.safekeeping.core.type.enums.AdapterProtocolType;
import com.vmware.safekeeping.core.type.enums.AdapterType;

public class DiskController {
	private AdapterType adapterType;
	private int busNumber;
	private int controllerKey;// 1000,
	private AdapterProtocolType adapterProtocol;

	public DiskController() {
	}

	/**
	 * Clone constructor
	 *
	 * @param src
	 */
	public DiskController(final DiskController src) {
		this.adapterType = src.adapterType;
		this.busNumber = src.busNumber;
		this.controllerKey = src.controllerKey;
		this.adapterProtocol = src.adapterProtocol;
	}

	/**
	 * @return the adapterProtocol
	 */
	public AdapterProtocolType getAdapterProtocol() {
		return this.adapterProtocol;
	}

	/**
	 * @return the adapterType
	 */
	public AdapterType getAdapterType() {
		return this.adapterType;
	}

	/**
	 * @return the busNumber
	 */
	public int getBusNumber() {
		return this.busNumber;
	}

	/**
	 * @return the controllerKey
	 */
	public int getControllerKey() {
		return this.controllerKey;
	}

	/**
	 * @param adapterProtocol the adapterProtocol to set
	 */
	public void setAdapterProtocol(final AdapterProtocolType adapterProtocol) {
		this.adapterProtocol = adapterProtocol;
	}

	/**
	 * @param adapterType the adapterType to set
	 */
	public void setAdapterType(final AdapterType adapterType) {
		this.adapterType = adapterType;
	}

	/**
	 * @param busNumber the busNumber to set
	 */
	public void setBusNumber(final int busNumber) {
		this.busNumber = busNumber;
	}

	/**
	 * @param controllerKey the controllerKey to set
	 */
	public void setControllerKey(final int controllerKey) {
		this.controllerKey = controllerKey;
	}

}