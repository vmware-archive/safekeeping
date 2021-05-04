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

import java.util.NavigableMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;

public class FcoGenerations {
	private final NavigableMap<Integer, Generation> generations;

	private Integer latest;
	private Integer latestSucceded;

	private ManagedFcoEntityInfo fcoEntity;

	public FcoGenerations() {
		this.generations = new TreeMap<>();
		this.latest = -1;
		this.latestSucceded = -1;
	}

	/**
	 * @param entity
	 */
	public FcoGenerations(final ManagedFcoEntityInfo entity) {
		this();
		this.fcoEntity = entity;
	}

	/**
	 * Puts a generation into this map.
	 *
	 * @param generation the generation to add
	 * @return the generation previously mapped to this key, null if none
	 */
	public Generation addGeneration(final Generation generation) {
		return this.generations.put(generation.getGenId(), generation);

	}

	public Generation completeGeneration(final GenerationProfile profGen, final BackupMode mode) {
		final Integer genId = profGen.getGenerationId();
		final Generation generation = getGeneration(genId);
		if (generation != null) {
			generation.setSucceeded(profGen.isSucceeded());

			generation.setBackupMode(mode);
			generation.setTimestamp(profGen.getTimestamp());
			if (generation.isSucceeded()) {
				this.latestSucceded = genId;
			}
		}
		return generation;
	}

	/**
	 * @return the fcoEntity
	 */
	public ManagedFcoEntityInfo getFcoEntity() {
		return this.fcoEntity;
	}

	/**
	 * Gets the value mapped to the generation version specified.
	 *
	 * @param genId generation version
	 * @return the mapped generation, null if no match or genId<0
	 */
	public Generation getGeneration(final Integer genId) {
		Generation result = null;
		if ((genId >= 0) && this.generations.containsKey(genId)) {
			result = this.generations.get(genId);
		}
		return result;
	}

	/**
	 * @return the generations
	 */
	public NavigableMap<Integer, Generation> getGenerations() {
		return this.generations;
	}

	/**
	 * @return the latest
	 */
	public Integer getLatest() {
		return this.latest;
	}

	/**
	 * @return the latestSucceded
	 */
	public Integer getLatestSucceded() {
		return this.latestSucceded;
	}

	/**
	 * Get the FCO entity UUID
	 *
	 * @return
	 */
	@JsonIgnore
	public String getUuid() {
		return this.fcoEntity.getUuid();
	}

	/**
	 * Removes the specified generation from this map.
	 *
	 * @param genId the generation to remove
	 * @return the mapped mapped to the removed version, null if version is not in
	 *         map
	 */
	public Generation removeGeneration(final Integer genId) {
		return this.generations.remove(genId);
	}

	/**
	 * @param fcoEntity the fcoEntity to set
	 */
	public void setFcoEntity(final ManagedFcoEntityInfo fcoEntity) {
		this.fcoEntity = fcoEntity;
	}

	/**
	 * @param latest the latest to set
	 */
	public void setLatest(final Integer latest) {
		this.latest = latest;
	}

	/**
	 * @param latestSucceded the latestSucceded to set
	 */
	public void setLatestSucceded(final Integer latestSucceded) {
		this.latestSucceded = latestSucceded;
	}

}