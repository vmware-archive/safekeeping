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

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;

public class CoreResultActionIvdList extends CoreAbstractResultActionAutoClose implements ICoreResultAction {

	/**
     * 
     */
    private static final long serialVersionUID = -5075814026019277853L;
    private final List<ImprovedVirtualDisk> ivdList = new ArrayList<>();
	private long capacityInMB;
	private String cbt;
	private boolean snapshot;
	private ManagedEntityInfo datastoreInfo;
	private boolean attached;
	private String filePath;
	private int diskKey;
	private ManagedFcoEntityInfo attachedVm;

	public CoreResultActionIvdList() {
	}

	public CoreResultActionIvdList(final OperationState state) {
		super(state);
	}

	public ManagedFcoEntityInfo getAttachedVm() {
		return this.attachedVm;
	}

	public long getCapacityInMB() {
		return this.capacityInMB;
	}

	public String getCbt() {
		return this.cbt;
	}

	public ManagedEntityInfo getDatastoreInfo() {
		return this.datastoreInfo;
	}

	public int getDiskKey() {
		return this.diskKey;
	}

	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * @return the ivdList
	 */
	public List<ImprovedVirtualDisk> getIvdList() {
		return this.ivdList;
	}

	public boolean hasSnapshot() {
		return this.snapshot;
	}

	public boolean isAttached() {
		return this.attached;
	}

	/**
	 * @param fcoInfo
	 */
	public void setAttachedVm(final ManagedFcoEntityInfo attachedVm) {
		this.attachedVm = attachedVm;

	}

	/**
	 * @param capacityInMB
	 */
	public void setCapacityInMB(final long capacityInMB) {
		this.capacityInMB = capacityInMB;

	}

	/**
	 * @param cbt
	 */
	public void setCbt(final String cbt) {
		this.cbt = cbt;

	}

	/**
	 * @param datastoreInfo
	 */
	public void setDatastore(final ManagedEntityInfo datastoreInfo) {
		this.datastoreInfo = datastoreInfo;

	}

	public void setDatastoreInfo(final ManagedEntityInfo datastoreInfo) {
		this.datastoreInfo = datastoreInfo;
	}

	public void setDiskKey(final int diskKey) {
		this.diskKey = diskKey;
	}

	/**
	 * @param filePath
	 */
	public void setFilePath(final String filePath) {
		this.filePath = filePath;

	}

	/**
	 * @param b
	 */
	public void setHasSnapshot(final boolean snapshot) {
		this.snapshot = snapshot;

	}

	/**
	 * @param attached
	 */
	public void setIsAttached(final boolean attached) {
		this.attached = attached;

	}

	/**
	 * @param diskKey
	 */
	public void setVmControllerKey(final int diskKey) {
		this.diskKey = diskKey;

	}
}
