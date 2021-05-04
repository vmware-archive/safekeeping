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

import com.vmware.safekeeping.core.command.results.ICoreResultAction;

public class CoreResultActionVappImport extends CoreAbstractResultActionAutoClose implements ICoreResultAction {

	/**
     * 
     */
    private static final long serialVersionUID = -6045340961811014842L;

    private String vim;

	private String urlPath;
	private String name;
	private String hostName;
	private String datastoreName;
	private String resourcePoolName;
	private String folderName;

	/**
	 * @return the datastoreName
	 */
	public String getDatastoreName() {
		return this.datastoreName;
	}

	/**
	 * @return the folderName
	 */
	public String getFolderName() {
		return this.folderName;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return this.hostName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the resourcePoolName
	 */
	public String getResourcePoolName() {
		return this.resourcePoolName;
	}

	/**
	 * @return the urlPath
	 */
	public String getUrlPath() {
		return this.urlPath;
	}

	public String getVim() {
		return this.vim;
	}

	/**
	 * @param datastoreName the datastoreName to set
	 */
	public void setDatastoreName(final String datastoreName) {
		this.datastoreName = datastoreName;
	}

	/**
	 * @param folderName the folderName to set
	 */
	public void setFolderName(final String folderName) {
		this.folderName = folderName;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(final String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param resourcePoolName the resourcePoolName to set
	 */
	public void setResourcePoolName(final String resourcePoolName) {
		this.resourcePoolName = resourcePoolName;
	}

	/**
	 * @param urlPath the urlPath to set
	 */
	public void setUrlPath(final String urlPath) {
		this.urlPath = urlPath;
	}

	public void setVim(final String vim) {
		this.vim = vim;
	}

}