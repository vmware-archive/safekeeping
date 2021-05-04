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

public class GuestInfoFlags {
	private Boolean diskUuidEnabled;

	private boolean template;

	private Boolean vbsEnabled;

	private boolean vAppConfigAvailable;

	private Boolean configurationEncrypted;

	private String guestFullName;
	private Integer numberOfVirtualDisk;
	private String vmPathName;
	private Boolean changeTrackingEnabled;

	/**
	 * @return the changeTrackingEnabled
	 */
	public Boolean getChangeTrackingEnabled() {
		return this.changeTrackingEnabled;
	}

	/**
	 * @return the configurationEncrypted
	 */
	public Boolean getConfigurationEncrypted() {
		return this.configurationEncrypted;
	}

	/**
	 * @return the diskUuidEnabled
	 */
	public Boolean getDiskUuidEnabled() {
		return this.diskUuidEnabled;
	}

	/**
	 * Gets the value of the guestFullName property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getGuestFullName() {
		return this.guestFullName;
	}

	/**
	 * Gets the value of the numberOfVirtualDisk property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getNumberOfVirtualDisk() {
		return this.numberOfVirtualDisk;
	}

	/**
	 * @return the vbsEnabled
	 */
	public Boolean getVbsEnabled() {
		return this.vbsEnabled;
	}

	/**
	 * Gets the value of the vmPathName property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getVmPathName() {
		return this.vmPathName;
	}

	/**
	 * Gets the value of the changeTrackingEnabled property.
	 *
	 * @return possible object is {@link Boolean }
	 *
	 */
	public Boolean isChangeTrackingEnabled() {
		return this.changeTrackingEnabled;
	}

	/**
	 * Gets the value of the configurationEncrypted property.
	 *
	 * @return possible object is {@link Boolean }
	 *
	 */
	public Boolean isConfigurationEncrypted() {
		return this.configurationEncrypted;
	}

	/**
	 * Gets the value of the diskUuidEnabled property.
	 *
	 * @return possible object is {@link Boolean }
	 *
	 */
	public Boolean isDiskUuidEnabled() {
		return this.diskUuidEnabled;
	}

	/**
	 * Gets the value of the template property.
	 *
	 */
	public boolean isTemplate() {
		return this.template;
	}

	/**
	 * Check the existence of the vAppConfig property.
	 *
	 * @return get true if vm has vAppConfig set
	 *
	 */
	public boolean isvAppConfigAvailable() {
		return this.vAppConfigAvailable;
	}

	/**
	 * Gets the value of the vbsEnabled property.
	 *
	 * @return possible object is {@link Boolean }
	 *
	 */
	public Boolean isVbsEnabled() {
		return this.vbsEnabled;
	}

	/**
	 * Sets the value of the changeTrackingEnabled property.
	 *
	 * @param value allowed object is {@link Boolean }
	 *
	 */
	public void setChangeTrackingEnabled(final Boolean value) {
		this.changeTrackingEnabled = value;
	}

	/**
	 * Sets the value of the configurationEncrypted property.
	 *
	 * @param value allowed object is {@link Boolean }
	 *
	 */
	public void setConfigurationEncrypted(final Boolean value) {
		this.configurationEncrypted = value;
	}

	/**
	 * Sets the value of the diskUuidEnabled property.
	 *
	 * @param value allowed object is {@link Boolean }
	 *
	 */
	public void setDiskUuidEnabled(final Boolean value) {
		this.diskUuidEnabled = value;
	}

	/**
	 * Sets the value of the guestFullName property.
	 *
	 * @param value allowed object is {@link String }
	 *
	 */
	public void setGuestFullName(final String value) {
		this.guestFullName = value;
	}

	/**
	 * Sets the value of the numberOfVirtualDisk property.
	 *
	 * @param value allowed object is {@link Integer }
	 *
	 */
	public void setNumberOfVirtualDisk(final Integer value) {
		this.numberOfVirtualDisk = value;
	}

	/**
	 * Sets the value of the template property.
	 *
	 */
	public void setTemplate(final boolean value) {
		this.template = value;
	}

	/**
	 * @param vAppConfigAvailable the vAppConfigAvailable to set
	 */
	public void setvAppConfigAvailable(final boolean vAppConfigAvailable) {
		this.vAppConfigAvailable = vAppConfigAvailable;
	}

	/**
	 * Sets the value of the vAppConfigAvailable property.
	 *
	 * @param value allowed object is {@link Boolean }
	 *
	 */
	public void setVAppConfigAvailable(final Boolean value) {
		this.vAppConfigAvailable = value;
	}

	/**
	 * Sets the value of the vbsEnabled property.
	 *
	 * @param value allowed object is {@link Boolean }
	 *
	 */
	public void setVbsEnabled(final Boolean value) {
		this.vbsEnabled = value;
	}

	/**
	 * Sets the value of the vmPathName property.
	 *
	 * @param value allowed object is {@link String }
	 *
	 */
	public void setVmPathName(final String value) {
		this.vmPathName = value;
	}

}