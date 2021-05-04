/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met;
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS AS IS
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
package com.vmware.safekeeping.core.profile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.common.DateUtility;
import com.vmware.safekeeping.core.control.FcoArchiveManager;
import com.vmware.safekeeping.core.control.target.ITargetOperation;
import com.vmware.safekeeping.core.profile.dataclass.FcoGenerations;
import com.vmware.safekeeping.core.profile.dataclass.Generation;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vim25.VStorageObjectConfigInfo;

public class FcoGenerationsCatalog {

	private final FcoGenerations fcoGenerations;

	private FcoArchiveManager fcoArchiveManager;

	/**
	 * @param fcoArchiveManager
	 * @param entity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public FcoGenerationsCatalog(final FcoArchiveManager fcoArchiveManager, final ManagedFcoEntityInfo entity)
			throws IOException {
		this.fcoArchiveManager = fcoArchiveManager;
		final byte[] bytes = fcoArchiveManager.getRepositoryTarget().getFcoProfileToByteArray(entity);
		this.fcoGenerations = new ObjectMapper().readValue(bytes, FcoGenerations.class);
	}

	public FcoGenerationsCatalog(final ManagedFcoEntityInfo entity) {
		this.fcoGenerations = new FcoGenerations(entity);

	}

	/**
	 *
	 * @param configInfo
	 * @param uuid
	 * @param vCenterInstanceUuid
	 */
	public FcoGenerationsCatalog(final VStorageObjectConfigInfo configInfo, final String uuid,
			final String vCenterInstanceUuid) {
		final ManagedFcoEntityInfo entity = new ManagedFcoEntityInfo(configInfo.getName(),
				EntityType.ImprovedVirtualDisk, uuid.substring(24), uuid, vCenterInstanceUuid);
		this.fcoGenerations = new FcoGenerations(entity);

	}
//
//    public Integer createNewGenerationId() {
//	final Integer currGenId = getLatestGenerationId();
//	this.fcoGenerations.setLatest((currGenId < -1) ? 0 : currGenId + 1);
//	return this.fcoGenerations.getLatest();
//    }

	public Integer createNewGenerationId(final Long timestampMs, final BackupMode mode) {

		final Integer currGenId = getLatestGenerationId();
		this.fcoGenerations.setLatest((currGenId < -1) ? 0 : currGenId + 1);

		final Generation generation = new Generation();
		generation.setSucceeded(false);
		generation.setBackupMode(mode);

		generation.setGenId(this.fcoGenerations.getLatest());
		generation.setDependingOnGenerationId((mode != BackupMode.FULL) ? getLatestSucceededGenerationId() : -2);
		final Calendar cal = Calendar.getInstance();
		if (timestampMs != null) {
			cal.setTimeInMillis(timestampMs);
			generation.setTimestamp(cal.getTime());
		}
		this.fcoGenerations.addGeneration(generation);
		return this.fcoGenerations.getLatest();
	}

	/**
	 * @param id
	 * @return
	 */
	public Integer getDependingGenerationId(final Integer genId) {
		final Generation generation = this.fcoGenerations.getGenerations().get(genId);
		return generation.getDependingOnGenerationId();

	}

	/**
	 * @param generationId
	 * @return
	 */
	public List<Integer> getDependingGenerationList(final Integer genId) {
		final List<Integer> result = new LinkedList<>();
		final Generation generation = this.fcoGenerations.getGenerations().get(genId);
		final Integer dependsOn = generation.getDependingOnGenerationId();
		if (dependsOn >= 0) {
			result.addAll(getDependingGenerationList(dependsOn));
		}
		result.add(genId);
		return result;
	}

	/**
	 * @return
	 */
	public List<Integer> getFailedGenerationList() {
		final List<Integer> result = new LinkedList<>();
		for (final Generation generation : this.fcoGenerations.getGenerations().values()) {
			if (!generation.isSucceeded()) {
				result.add(generation.getGenId());
			}

		}
		return result;
	}

	/**
	 * @return the fcoArchiveManager
	 */
	public FcoArchiveManager getFcoArchiveManager() {
		return this.fcoArchiveManager;
	}

	/**
	 * @return the fcoEntity
	 */
	public ManagedFcoEntityInfo getFcoEntity() {
		return this.fcoGenerations.getFcoEntity();
	}

	/**
	 * Gets the value mapped to the generation version specified.
	 *
	 * @param genId generation version
	 * @return the mapped generation, null if no match or genId<0
	 */
	public Generation getGeneration(final Integer genId) {
		return this.fcoGenerations.getGeneration(genId);
	}

	/**
	 * @return
	 */
	public List<Integer> getGenerationIdList() {
		return new ArrayList<>(this.fcoGenerations.getGenerations().keySet());
	}

	/**
	 * @return the generations
	 */
	public NavigableMap<Integer, Generation> getGenerations() {
		return this.fcoGenerations.getGenerations();
	}

	/**
	 * @return the latest
	 */
	public Integer getLatest() {
		return this.fcoGenerations.getLatest();
	}

	/**
	 * Gets the value mapped to the latest generation available.
	 *
	 * @return the mapped generation, null if no match or genId<0
	 */
	public Generation getLatestGeneration() {
		return this.fcoGenerations.getGeneration(getLatest());
	}

	public Integer getLatestGenerationId() {
		return this.fcoGenerations.getLatest();
	}

	/**
	 * Gets the value mapped to the latest successful generation available.
	 *
	 * @return the mapped generation, null if no match or genId<0
	 */
	public Generation getLatestSuccededGeneration() {
		return this.fcoGenerations.getGeneration(getLatestSucceededGenerationId());
	}

	/**
	 * return the last Succeeded Generation starting from the last generation
	 *
	 * @return
	 */
	public Integer getLatestSucceededGenerationId() {
		return this.fcoGenerations.getLatestSucceded();
	}

	/**
	 * return the last Succeeded Generation starting from genId
	 *
	 * @param genId
	 * @return
	 */
	public Integer getLatestSucceededGenerationId(final Integer genId) {
		Integer result = genId;
		if (result < 0) {
			result = -2;
		}
		if (!isGenerationSucceeded(result)) {
			final Integer prevId = getPrevSucceededGenerationId(result);
			if (prevId >= 0) {
				result = prevId;
			} else {
				result = -2;
			}
		}
		return result;
	}

	/**
	 * Return the next succeeded generation
	 *
	 * @param genId
	 * @return generation ID or -2 if none available
	 */
	public Generation getNextSucceededGenerationId(final Integer genId) {
		Generation result = null;
		final Entry<Integer, Generation> next = this.fcoGenerations.getGenerations().higherEntry(genId);
		if ((next != null) && next.getValue().isSucceeded()) {
			result = next.getValue();
		}

		return result;
	}

	/**
	 * @return
	 */
	public Integer getNumOfGeneration() {
		return this.fcoGenerations.getGenerations().size();
	}

	public Integer getNumOfSuccceededGeneration() {
		Integer result = 0;
		for (final Generation generation : this.fcoGenerations.getGenerations().values()) {
			if (generation.isSucceeded()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * @param generationList
	 * @return
	 */
	public Integer getNumOfSuccceededGeneration(final List<Integer> generationList) {
		Integer result = 0;
		for (final Integer key : generationList) {
			if (this.fcoGenerations.getGenerations().get(key).isSucceeded()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Return the previous succeeded generation
	 *
	 * @param genId
	 * @return generation ID or -2 if none available
	 */
	public Integer getPrevSucceededGenerationId(final Integer genId) {
		Integer result = -2;
		final List<Integer> keyList = new ArrayList<>(this.fcoGenerations.getGenerations().keySet());

		final Integer idx = keyList.indexOf(genId) - 1;
		for (Integer i = idx; i >= 0; i--) {
			if (this.fcoGenerations.getGenerations().get(keyList.get(i)).isSucceeded()) {
				result = i;
				break;
			}
		}
		return result;
	}

	public List<Integer> getSuccededGenerationList() {
		final List<Integer> result = new LinkedList<>();
		for (final Generation generation : this.fcoGenerations.getGenerations().values()) {
			if (generation.isSucceeded()) {
				result.add(generation.getGenId());
			}

		}
		return result;
	}

	/**
	 * @return the target
	 */
	public ITargetOperation getTarget() {
		return this.fcoArchiveManager.getRepositoryTarget();
	}

	public Date getTimestamp(final Integer genId) {
		Date result = null;
		if ((genId >= 0) && this.fcoGenerations.getGenerations().containsKey(genId)) {
			result = this.fcoGenerations.getGenerations().get(genId).getTimestamp();
		}
		return result;
	}

	/**
	 * @param genId
	 * @return
	 */
	public Long getTimestampMs(final Integer genId) {
		Long result = null;
		if ((genId >= 0) && this.fcoGenerations.getGenerations().containsKey(genId)) {
			result = this.fcoGenerations.getGenerations().get(genId).getTimestamp().getTime();
		}
		return result;
	}

	public String getTimestampStr(final Integer genId) {
		String result = null;
		if ((genId >= 0) && this.fcoGenerations.getGenerations().containsKey(genId)) {
			result = DateUtility.toGMTString(this.fcoGenerations.getGenerations().get(genId).getTimestamp());
		}
		return result;
	}

	/**
	 * Get the fco entity UUID
	 *
	 * @return
	 */
	public String getUuid() {
		return this.fcoGenerations.getUuid();
	}

	/**
	 * @param genId
	 * @return
	 */
	public boolean isGenerationExist(final Integer genId) {
		return ((genId >= 0) && this.fcoGenerations.getGenerations().containsKey(genId));
	}

	public boolean isGenerationSucceeded(final Integer genId) {
		boolean result = false;
		if ((genId >= 0) && this.fcoGenerations.getGenerations().containsKey(genId)) {
			result = this.fcoGenerations.getGenerations().get(genId).isSucceeded();
		}
		return result;
	}

	/**
	 * @return post on target the class Generation in JSON format
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public boolean post() throws NoSuchAlgorithmException, IOException {
		final String json = new ObjectMapper().writeValueAsString(this.fcoGenerations);
		return getTarget().postGenerationsCatalog(this.fcoGenerations.getFcoEntity(), new ByteArrayInOutStream(json));
	}

	/**
	 * @param genId
	 * @return
	 */
	public Generation removeGeneration(final Integer genId) {
		if (this.fcoGenerations.getLatestSucceded().intValue() == genId.intValue()) {
			this.fcoGenerations.setLatestSucceded(getPrevSucceededGenerationId(genId));
		}
		final Generation result = this.fcoGenerations.removeGeneration(genId);
		if (this.fcoGenerations.getLatest().intValue() == genId.intValue()) {
			this.fcoGenerations.setLatest(this.fcoGenerations.getGenerations().lastKey());
		}
		if (this.fcoGenerations.getLatest().intValue() > this.fcoGenerations.getLatestSucceded().intValue()) {
			this.fcoGenerations.setLatestSucceded(getPrevSucceededGenerationId(this.fcoGenerations.getLatest()));
		}
		return result;
	}

	private void setDependingGenerationId(final Integer genId, final Integer dependingOnGenerationId) {
		final Generation generation = this.fcoGenerations.getGenerations().get(genId);
		generation.setDependingOnGenerationId(dependingOnGenerationId);
	}

	/**
	 * @param fcoEntity the fcoEntity to set
	 */
	public void setFcoEntity(final ManagedFcoEntityInfo fcoEntity) {
		this.fcoGenerations.setFcoEntity(fcoEntity);
	}

	public void setGenerationInfo(final GenerationProfile profGen, final BackupMode mode) {
		this.fcoGenerations.completeGeneration(profGen, mode);
	}

	/**
	 * @param generationId
	 */
	public void setGenerationNotDependent(final Integer genId) {
		setDependingGenerationId(genId, -2);
	}

	/**
	 * Convert the class Generation to JSON
	 *
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public ByteArrayInOutStream toByteArrayInOutputStream() throws NoSuchAlgorithmException, IOException {
		final String json = new ObjectMapper().writeValueAsString(this.fcoGenerations);
		return new ByteArrayInOutStream(json);
	}

}
