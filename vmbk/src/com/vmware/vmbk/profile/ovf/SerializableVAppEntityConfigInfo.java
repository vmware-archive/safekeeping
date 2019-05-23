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
package com.vmware.vmbk.profile.ovf;

import java.io.Serializable;

import com.vmware.vim25.VAppEntityConfigInfo;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.type.EntityType;

public class SerializableVAppEntityConfigInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 911452871017267437L;
    private final Boolean destroyWithParent;
    private String key; // ManagedObjectReference value
    private final EntityType type;
    private final String startAction;
    private final Integer startDelay;
    private final Integer startOrder;
    private final String stopAction;
    private final Integer stopDelay;
    private final String tag;
    private final Boolean waitingForGuest;

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

    public boolean isKey(final String key) {
	return this.key.contentEquals(key);
    }

    public void setKey(final String newKey) {
	this.key = newKey;
    }

    public VAppEntityConfigInfo toVAppEntityConfigInfo() {
	final VAppEntityConfigInfo entityConfig = new VAppEntityConfigInfo();

	entityConfig.setDestroyWithParent(this.destroyWithParent);
	entityConfig.setKey(MorefUtil.create(this.type, this.key));
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
