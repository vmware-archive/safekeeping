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

import com.vmware.vim25.ResourceAllocationInfo;
import com.vmware.vim25.SharesInfo;
import com.vmware.vim25.SharesLevel;

public class FcoGenerationResourceAllocation {
	/**
	 * In a resource pool with an expandable reservation, the reservation on a
	 * resource pool can grow beyond the specified value, if the parent resource
	 * pool has unreserved resources. A non-expandable reservation is called a fixed
	 * reservation. This property is invalid for virtual machines.
	 */
	private Boolean expandableReservation;
	/**
	 * The utilization of a virtual machine/resource pool will not exceed this
	 * limit, even if there are available resources. This is typically used to
	 * ensure a consistent performance of virtual machines / resource pools
	 * independent of available resources. If set to -1, then there is no fixed
	 * limit on resource usage (only bounded by available resources and shares).
	 * Units are MB for memory, MHz for CPU.
	 *
	 */
	private Long limit;

	/**
	 * The maximum allowed overhead memory. For a powered on virtual machine, the
	 * overhead memory reservation cannot be larger than its overheadLimit. This
	 * property is only applicable to powered on virtual machines and is not
	 * persisted across reboots. This property is not applicable for resource pools.
	 * If set to -1, then there is no limit on reservation. Units are MB. Note: For
	 * vCenter Server use only. Not available for other clients at this time. The
	 * server will throw an exception if you attempt to set this property.
	 *
	 */
	private Long overheadLimit;

	/**
	 * Amount of resource that is guaranteed available to the virtual machine or
	 * resource pool. Reserved resources are not wasted if they are not used. If the
	 * utilization is less than the reservation, the resources can be utilized by
	 * other running virtual machines. Units are MB for memory, MHz for CPU. shares*
	 */
	private Long reservation;

	/**
	 * The number of shares allocated. Used to determine resource allocation in case
	 * of resource contention. This value is only set if level is set to custom. If
	 * level is not set to custom, this value is ignored. Therefore, only shares
	 * with custom values can be compared. There is no unit for this value. It is a
	 * relative measure based on the settings for other resource pools.
	 */
	private int shares;

	/**
	 * The allocation level. The level is a simplified view of shares. Levels map to
	 * a pre-determined set of numeric values for shares. If the shares value does
	 * not map to a predefined size, then the level is set as custom.
	 */
	private SharesLevel level;

	public FcoGenerationResourceAllocation() {
	}

	/**
	 * Clone constructor
	 *
	 * @param src
	 */
	public FcoGenerationResourceAllocation(final FcoGenerationResourceAllocation src) {
		this.expandableReservation = src.expandableReservation;
		this.limit = src.limit;
		this.overheadLimit = src.overheadLimit;
		this.reservation = src.reservation;
		this.shares = src.shares;
		this.level = src.level;
	}

	/**
	 * @param cpuAllocation
	 */
	public FcoGenerationResourceAllocation(final ResourceAllocationInfo allocation) {
		this.expandableReservation = allocation.isExpandableReservation();
		this.limit = allocation.getLimit();
		this.overheadLimit = allocation.getOverheadLimit();
		this.reservation = allocation.getReservation();
		this.shares = allocation.getShares().getShares();
		this.level = allocation.getShares().getLevel();
	}

	/**
	 * @return the level
	 */
	public SharesLevel getLevel() {
		return this.level;
	}

	/**
	 * @return the limit
	 */
	public Long getLimit() {
		return this.limit;
	}

	/**
	 * @return the overheadLimit
	 */
	public Long getOverheadLimit() {
		return this.overheadLimit;
	}

	/**
	 * @return the reservation
	 */
	public Long getReservation() {
		return this.reservation;
	}

	/**
	 * @return the shares
	 */
	public int getShares() {
		return this.shares;
	}

	/**
	 * @return the expandableReservation
	 */
	public Boolean isExpandableReservation() {
		return this.expandableReservation;
	}

	/**
	 * @param expandableReservation the expandableReservation to set
	 */
	public void setExpandableReservation(final Boolean expandableReservation) {
		this.expandableReservation = expandableReservation;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(final SharesLevel level) {
		this.level = level;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(final Long limit) {
		this.limit = limit;
	}

	/**
	 * @param overheadLimit the overheadLimit to set
	 */
	public void setOverheadLimit(final Long overheadLimit) {
		this.overheadLimit = overheadLimit;
	}

	/**
	 * @param reservation the reservation to set
	 */
	public void setReservation(final Long reservation) {
		this.reservation = reservation;
	}

	/**
	 * @param shares the shares to set
	 */
	public void setShares(final int shares) {
		this.shares = shares;
	}

	/**
	 *
	 * @return
	 */
	public ResourceAllocationInfo toResourceAllocationInfo() {
		final ResourceAllocationInfo res = new ResourceAllocationInfo();
		res.setLimit(this.limit);
		res.setExpandableReservation(this.expandableReservation);
		final SharesInfo s = new SharesInfo();
		s.setLevel(this.level);
		s.setShares(this.shares);
		res.setShares(s);
		res.setOverheadLimit(this.overheadLimit);
		return res;
	}

}