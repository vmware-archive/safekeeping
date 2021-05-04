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
package com.vmware.safekeeping.core.core;

public class ConsolidateStatistic {
	private int totalBlocks;

	private int index;

	private int numberReplacement;

	private int relocated;

	private int relocatedGreaterLength;

	private long totalSize;

	private int removedByOverlap;

	private int resizedByOverlap;

	private Long maxBlockLength;

	public ConsolidateStatistic() {
		this.totalBlocks = 0;
		this.index = 0;
		this.numberReplacement = 0;
		this.relocated = 0;
		this.relocatedGreaterLength = 0;
		this.totalSize = 0;
		this.removedByOverlap = 0;
		this.resizedByOverlap = 0;
		this.maxBlockLength = 0L;
	}

	public long decTotalSize(final long value) {
		this.totalSize -= value;
		return this.totalSize;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @return the maxBlockLength
	 */
	public Long getMaxBlockLength() {
		return this.maxBlockLength;
	}

	/**
	 * @return the numberReplacement
	 */
	public int getNumberReplacement() {
		return this.numberReplacement;
	}

	/**
	 * @return the relocated
	 */
	public int getRelocated() {
		return this.relocated;
	}

	/**
	 * @return the relocatedGreaterLength
	 */
	public int getRelocatedGreaterLength() {
		return this.relocatedGreaterLength;
	}

	/**
	 * @return the removedByOverlap
	 */
	public int getRemovedByOverlap() {
		return this.removedByOverlap;
	}

	/**
	 * @return the resizedByOverlap
	 */
	public int getResizedByOverlap() {
		return this.resizedByOverlap;
	}

	/**
	 * @return the totalBlocks
	 */
	public int getTotalBlocks() {
		return this.totalBlocks;
	}

	/**
	 * @return the totalSize
	 */
	public long getTotalSize() {
		return this.totalSize;
	}

	public int incIndex() {
		this.index++;
		return this.index;
	}

	public int incNumberReplacement() {
		this.numberReplacement++;
		return this.numberReplacement;
	}

	public int incResizedByOverlap() {
		this.resizedByOverlap++;
		return this.resizedByOverlap;

	}

	public int incTotalBlocks() {
		this.totalBlocks++;
		return this.totalBlocks;

	}

	public long incTotalSize(final long value) {
		this.totalSize += value;
		return this.totalSize;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(final int index) {
		this.index = index;
	}

	/**
	 * @param maxBlockLength the maxBlockLength to set
	 */
	public void setMaxBlockLength(final Long maxBlockLength) {
		this.maxBlockLength = maxBlockLength;
	}

	/**
	 * @param numberReplacement the numberReplacement to set
	 */
	public void setNumberReplacement(final int numberReplacement) {
		this.numberReplacement = numberReplacement;
	}

	/**
	 * @param relocated the relocated to set
	 */
	public void setRelocated(final int relocated) {
		this.relocated = relocated;
	}

	/**
	 * @param relocatedGreaterLength the relocatedGreaterLength to set
	 */
	public void setRelocatedGreaterLength(final int relocatedGreaterLength) {
		this.relocatedGreaterLength = relocatedGreaterLength;
	}

	/**
	 * @param removedByOverlap the removedByOverlap to set
	 */
	public void setRemovedByOverlap(final int removedByOverlap) {
		this.removedByOverlap = removedByOverlap;
	}

	/**
	 * @param resizedByOverlap the resizedByOverlap to set
	 */
	public void setResizedByOverlap(final int resizedByOverlap) {
		this.resizedByOverlap = resizedByOverlap;
	}

	/**
	 * @param totalBlocks the totalBlocks to set
	 */
	public void setTotalBlocks(final int totalBlocks) {
		this.totalBlocks = totalBlocks;
	}

	/**
	 * @param totalSize the totalSize to set
	 */
	public void setTotalSize(final long totalSize) {
		this.totalSize = totalSize;
	}

}
