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
package com.vmware.safekeeping.core.type;

import com.vmware.safekeeping.core.type.enums.EntityType;

public class OperationPhase {
	private Integer activeChild;

	private int numberOfChildren;

	private int numberOfDisks;

	/**
	 * Percentage progress increment for a child VM
	 */
	private float progressPercentagePerSingleChild;

	/**
	 * Percentage progress increment on root Action for a disk
	 */
	private float progressPercentageOnChildPerSingleDumpOperation;

	/**
	 * Percentage progress increment on root Action for single dump operation
	 */
	private float progressPercentageOnRootPerDiskDumpOperation;

	private float progressPercentageOnDiskPerSingleDumpOperation;

	private final EntityType managedEntityType;

	/**
	 * @param entityType
	 */
	public OperationPhase(final EntityType entityType) {
		this.managedEntityType = entityType;
		reset();
	}

	/**
	 * @return the activeChild
	 */
	public Integer getActiveChild() {
		return this.activeChild;
	}

	/**
	 * @return the entityType
	 */
	public EntityType getManagedEntityType() {
		return this.managedEntityType;
	}

	/**
	 * @return the numberOfChildren
	 */
	public int getNumberOfChildren() {
		return this.numberOfChildren;
	}

	/**
	 * @return the numberOfDisks
	 */
	public int getNumberOfDisks() {
		return this.numberOfDisks;
	}

	/**
	 * @return the progressPercentageOnChildPerSingleDumpOperation
	 */
	public float getProgressPercentageOnChildPerSingleDumpOperation() {
		return this.progressPercentageOnChildPerSingleDumpOperation;
	}

	/**
	 * @return the progressPercentageOnDiskPerSingleDumpOperation
	 */
	public float getProgressPercentageOnDiskPerSingleDumpOperation() {
		return this.progressPercentageOnDiskPerSingleDumpOperation;
	}

	/**
	 * @return the progressPercentageOnRootPerDiskDumpOperation
	 */
	public float getProgressPercentageOnRootPerDiskDumpOperation() {
		return this.progressPercentageOnRootPerDiskDumpOperation;
	}

	/**
	 * @return the progressPercentagePerSingleChild
	 */
	public float getProgressPercentagePerSingleChild() {
		return this.progressPercentagePerSingleChild;
	}

	public void reset() {
		this.activeChild = null;
		this.numberOfChildren = 0;
		this.numberOfDisks = 0;

	}

	/**
	 * @param activeChild the activeChild to set
	 */
	public void setActiveChild(final Integer activeChild) {
		this.activeChild = activeChild;
	}

	/**
	 * @param numberOfChildren the numberOfChildren to set
	 */
	public void setNumberOfChildren(final int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}

	/**
	 * @param numberOfDisks the numberOfDisks to set
	 */
	public void setNumberOfDisks(final int numberOfDisks) {
		this.numberOfDisks = numberOfDisks;
	}

	/**
	 * @param progressPercentageOnChildPerSingleDumpOperation the
	 *                                                        progressPercentageOnChildPerSingleDumpOperation
	 *                                                        to set //
	 */
	public void setProgressPercentageOnChildPerSingleDumpOperation(
			final float progressPercentageOnChildPerSingleDumpOperation) {
		this.progressPercentageOnChildPerSingleDumpOperation = progressPercentageOnChildPerSingleDumpOperation;
	}

	/**
	 * @param progressPercentageOnDiskPerSingleDumpOperation the
	 *                                                       progressPercentageOnDiskPerSingleDumpOperation
	 *                                                       to set
	 */
	public void setProgressPercentageOnDiskPerSingleDumpOperation(
			final float progressPercentageOnDiskPerSingleDumpOperation) {
		this.progressPercentageOnDiskPerSingleDumpOperation = progressPercentageOnDiskPerSingleDumpOperation;
	}

	/**
	 * @param progressPercentageOnRootPerDiskDumpOperation the
	 *                                                     progressPercentageOnRootPerDiskDumpOperation
	 *                                                     to set
	 */
	public void setProgressPercentageOnRootPerDiskDumpOperation(
			final float progressPercentageOnRootPerDiskDumpOperation) {
		this.progressPercentageOnRootPerDiskDumpOperation = progressPercentageOnRootPerDiskDumpOperation;
	}

	/**
	 * @param progressPercentagePerSingleChild the progressPercentagePerSingleChild
	 *                                         to set
	 */
	public void setProgressPercentagePerSingleChild(final float progressPercentagePerSingleChild) {
		this.progressPercentagePerSingleChild = progressPercentagePerSingleChild;
	}

}
