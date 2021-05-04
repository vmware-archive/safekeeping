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

import java.util.Arrays;

import com.vmware.vim25.VmConfigInfo;
import com.vmware.vim25.VmConfigSpec;

public class SerializableVmConfigInfo {

	private String[] eula;

	private boolean installBootRequired;

	private int installBootStopDelay;

	private SerializableVAppIPAssignmentInfo ipAssignment;

	private String[] ovfEnvironmentTransport;

	private SerializableVAppOvfSectionInfo[] ovfSection;

	private SerializableVAppProductInfo[] product;

	private SerializableVAppPropertyInfo[] property;

	public SerializableVmConfigInfo() {
//	this.installBootRequired = false;
//	this.installBootStopDelay = 0;
//	this.ipAssignment = new SerializableVAppIPAssignmentInfo();
//	this.ovfEnvironmentTransport = new String[0];
//	this.product = new SerializableVAppProductInfo[0];
//	this.eula = new String[0];
//	this.property = new SerializableVAppPropertyInfo[0];
//	this.ovfSection = new SerializableVAppOvfSectionInfo[0];
	}

	/**
	 * @param vAppConfig
	 */
	public SerializableVmConfigInfo(final SerializableVmConfigInfo vAppConfig) {
		this.eula = vAppConfig.eula;
		this.installBootStopDelay = vAppConfig.installBootStopDelay;
		this.installBootRequired = vAppConfig.installBootRequired;
		this.ovfEnvironmentTransport = vAppConfig.ovfEnvironmentTransport;
		this.ovfSection = vAppConfig.ovfSection;

		this.product = vAppConfig.product;
		this.property = vAppConfig.property;
		this.ipAssignment = vAppConfig.ipAssignment;
	}

	public SerializableVmConfigInfo(final VmConfigInfo vmConfigInfo) {
		this.eula = vmConfigInfo.getEula().toArray(new String[0]);
		this.installBootStopDelay = vmConfigInfo.getInstallBootStopDelay();
		this.installBootRequired = vmConfigInfo.isInstallBootRequired();
		this.ovfEnvironmentTransport = vmConfigInfo.getOvfEnvironmentTransport().toArray(new String[0]);

		this.ovfSection = new SerializableVAppOvfSectionInfo[vmConfigInfo.getOvfSection().size()];
		for (int i = 0; i < vmConfigInfo.getOvfSection().size(); i++) {
			this.ovfSection[i] = new SerializableVAppOvfSectionInfo(vmConfigInfo.getOvfSection().get(i));
		}

		this.product = new SerializableVAppProductInfo[vmConfigInfo.getProduct().size()];
		for (int i = 0; i < vmConfigInfo.getProduct().size(); i++) {
			this.product[i] = new SerializableVAppProductInfo(vmConfigInfo.getProduct().get(i));
		}

		this.property = new SerializableVAppPropertyInfo[vmConfigInfo.getProperty().size()];
		for (int i = 0; i < vmConfigInfo.getProperty().size(); i++) {
			this.property[i] = new SerializableVAppPropertyInfo(vmConfigInfo.getProperty().get(i));
		}

		this.ipAssignment = new SerializableVAppIPAssignmentInfo(vmConfigInfo.getIpAssignment());
	}

	/**
	 * @return the eula
	 */
	public String[] getEula() {
		return this.eula;
	}

	/**
	 * @return the installBootStopDelay
	 */
	public int getInstallBootStopDelay() {
		return this.installBootStopDelay;
	}

	/**
	 * @return the ipAssignment
	 */
	public SerializableVAppIPAssignmentInfo getIpAssignment() {
		return this.ipAssignment;
	}

	/**
	 * @return the ovfEnvironmentTransport
	 */
	public String[] getOvfEnvironmentTransport() {
		return this.ovfEnvironmentTransport;
	}

	/**
	 * @return the ovfSection
	 */
	public SerializableVAppOvfSectionInfo[] getOvfSection() {
		return this.ovfSection;
	}

	/**
	 * @return the product
	 */
	public SerializableVAppProductInfo[] getProduct() {
		return this.product;
	}

	/**
	 * @return the property
	 */
	public SerializableVAppPropertyInfo[] getProperty() {
		return this.property;
	}

	/**
	 * @return the installBootRequired
	 */
	public boolean isInstallBootRequired() {
		return this.installBootRequired;
	}

	/**
	 * @param eula the eula to set
	 */
	public void setEula(final String[] eula) {
		this.eula = eula;
	}

	/**
	 * @param installBootRequired the installBootRequired to set
	 */
	public void setInstallBootRequired(final boolean installBootRequired) {
		this.installBootRequired = installBootRequired;
	}

	/**
	 * @param installBootStopDelay the installBootStopDelay to set
	 */
	public void setInstallBootStopDelay(final int installBootStopDelay) {
		this.installBootStopDelay = installBootStopDelay;
	}

	/**
	 * @param ipAssignment the ipAssignment to set
	 */
	public void setIpAssignment(final SerializableVAppIPAssignmentInfo ipAssignment) {
		this.ipAssignment = ipAssignment;
	}

	/**
	 * @param ovfEnvironmentTransport the ovfEnvironmentTransport to set
	 */
	public void setOvfEnvironmentTransport(final String[] ovfEnvironmentTransport) {
		this.ovfEnvironmentTransport = ovfEnvironmentTransport;
	}

	/**
	 * @param ovfSection the ovfSection to set
	 */
	public void setOvfSection(final SerializableVAppOvfSectionInfo[] ovfSection) {
		this.ovfSection = ovfSection;
	}

	/**
	 * @param product the product to set
	 */
	public void setProduct(final SerializableVAppProductInfo[] product) {
		this.product = product;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(final SerializableVAppPropertyInfo[] property) {
		this.property = property;
	}

	public VmConfigSpec toVmConfigInfo() {
		final VmConfigSpec vmConfigInfo = new VmConfigSpec();
		vmConfigInfo.getEula().addAll(Arrays.asList(this.eula));
		vmConfigInfo.setInstallBootStopDelay(this.installBootStopDelay);
		vmConfigInfo.setInstallBootRequired(this.installBootRequired);
		vmConfigInfo.getOvfEnvironmentTransport().addAll(Arrays.asList(this.ovfEnvironmentTransport));
		for (final SerializableVAppOvfSectionInfo element : this.ovfSection) {
			vmConfigInfo.getOvfSection().add(element.toVAppOvfSectionSpec());
		}
		for (final SerializableVAppProductInfo element : this.product) {
			vmConfigInfo.getProduct().add(element.toVAppProductSpec());
		}
		for (final SerializableVAppPropertyInfo element : this.property) {
			vmConfigInfo.getProperty().add(element.toVAppPropertySpec());
		}

		vmConfigInfo.setIpAssignment(this.ipAssignment.toVAppIPAssignmentInfo());
		return vmConfigInfo;
	}

}
