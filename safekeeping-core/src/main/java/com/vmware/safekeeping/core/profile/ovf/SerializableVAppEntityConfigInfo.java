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

import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vim25.VAppEntityConfigInfo;

public class SerializableVAppEntityConfigInfo {

	private Boolean destroyWithParent;

	private String key; // ManagedObjectReference value

	private EntityType type;

	private String startAction;

	private Integer startDelay;

	private Integer startOrder;

	private String stopAction;

	private Integer stopDelay;

	private String tag;

	private Boolean waitingForGuest;

	public SerializableVAppEntityConfigInfo() {
	}

	/**
	 *
	 * @param entityConfig
	 */
	public SerializableVAppEntityConfigInfo(final VAppEntityConfigInfo entityConfig) {
		this.destroyWithParent = entityConfig.isDestroyWithParent();
		this.key = entityConfig.getKey().getValue();
		this.type = EntityType.valueOf(entityConfig.getKey().getType());
		this.startAction = entityConfig.getStartAction();
		this.startDelay = entityConfig.getStartDelay();
		this.startOrder = entityConfig.getStartOrder();
		this.stopAction = entityConfig.getStopAction();
		this.stopDelay = entityConfig.getStopDelay();
		this.tag = entityConfig.getTag();
		this.waitingForGuest = entityConfig.isWaitingForGuest();
	}

	/**
	 * @return the destroyWithParent
	 */
	public Boolean getDestroyWithParent() {
		return this.destroyWithParent;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @return the startAction
	 */
	public String getStartAction() {
		return this.startAction;
	}

	/**
	 * @return the startDelay
	 */
	public Integer getStartDelay() {
		return this.startDelay;
	}

	/**
	 * @return the startOrder
	 */
	public Integer getStartOrder() {
		return this.startOrder;
	}

	/**
	 * @return the stopAction
	 */
	public String getStopAction() {
		return this.stopAction;
	}

	/**
	 * @return the stopDelay
	 */
	public Integer getStopDelay() {
		return this.stopDelay;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return this.tag;
	}

	/**
	 * @return the type
	 */
	public EntityType getType() {
		return this.type;
	}

	/**
	 * @return the waitingForGuest
	 */
	public Boolean getWaitingForGuest() {
		return this.waitingForGuest;
	}

	public boolean isKey(final String key) {
		return this.key.contentEquals(key);
	}

	/**
	 * @param destroyWithParent the destroyWithParent to set
	 */
	public void setDestroyWithParent(final Boolean destroyWithParent) {
		this.destroyWithParent = destroyWithParent;
	}

	public void setKey(final String newKey) {
		this.key = newKey;
	}

	/**
	 * @param startAction the startAction to set
	 */
	public void setStartAction(final String startAction) {
		this.startAction = startAction;
	}

	/**
	 * @param startDelay the startDelay to set
	 */
	public void setStartDelay(final Integer startDelay) {
		this.startDelay = startDelay;
	}

	/**
	 * @param startOrder the startOrder to set
	 */
	public void setStartOrder(final Integer startOrder) {
		this.startOrder = startOrder;
	}

	/**
	 * @param stopAction the stopAction to set
	 */
	public void setStopAction(final String stopAction) {
		this.stopAction = stopAction;
	}

	/**
	 * @param stopDelay the stopDelay to set
	 */
	public void setStopDelay(final Integer stopDelay) {
		this.stopDelay = stopDelay;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(final String tag) {
		this.tag = tag;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final EntityType type) {
		this.type = type;
	}

	/**
	 * @param waitingForGuest the waitingForGuest to set
	 */
	public void setWaitingForGuest(final Boolean waitingForGuest) {
		this.waitingForGuest = waitingForGuest;
	}

	public VAppEntityConfigInfo toVAppEntityConfigInfo() {
		final VAppEntityConfigInfo entityConfig = new VAppEntityConfigInfo();

		entityConfig.setDestroyWithParent(this.destroyWithParent);
		entityConfig.setKey(MorefUtil.newManagedObjectReference(this.type, this.key));
		entityConfig.setStartAction(this.startAction);
		entityConfig.setStartDelay(this.startDelay);
		entityConfig.setStartOrder(this.startOrder);
		entityConfig.setStopAction(this.stopAction);
		entityConfig.setStopDelay(this.stopDelay);
		entityConfig.setTag(this.tag);
		entityConfig.setWaitingForGuest(this.waitingForGuest);

		return entityConfig;
	}
}
