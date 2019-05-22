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
package com.vmware.vmbk.control.info;

import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.type.EntityType;

public class InfoData {
    static public String getTableHeader() {
	return "type\t\t moRef \tuuid\t\t\t\t\tLG\tLG Date\t\t\t\tLSG\tLSG Date\t\t\tName";
    }

    static public String getTableKeys() {
	return "LG  - Last Generation     -     LSG - Last Succeded Generation";
    }

    private EntityType entityType;
    private boolean full;
    private int latestGenerationId;
    private int latestSucceededGenerationId;
    private String moref;
    private String name;
    private long timestampMsOfLatestGenerationId;
    private long timestampMsOfLatestSucceededGenerationId;
    private String timestampOfLatestGenerationId;

    private String timestampOfLatestSucceededGenerationId;

    private String uuid;

    public InfoData() {
    }

    public InfoData(final FcoArchiveManager vmArcMgr) {
	this.moref = vmArcMgr.getMoref();
	this.entityType = vmArcMgr.getEntityType();
	this.uuid = vmArcMgr.getInstanceUuid();
	this.name = vmArcMgr.getName();
	this.timestampMsOfLatestGenerationId = vmArcMgr.getTimestampMsOfLatestGeneration();

	this.timestampMsOfLatestSucceededGenerationId = vmArcMgr.getTimestampMsOfLatestSucceededGenerationId();
	this.timestampOfLatestGenerationId = vmArcMgr.getTimestampOfLatestGeneration();
	if (this.timestampOfLatestGenerationId == null) {
	    this.timestampOfLatestGenerationId = "na";
	}
	this.timestampOfLatestSucceededGenerationId = vmArcMgr.getTimestampOfLatestSucceededGenerationId();
	if (this.timestampOfLatestSucceededGenerationId == null) {
	    this.timestampOfLatestSucceededGenerationId = "na";
	}
	this.latestSucceededGenerationId = vmArcMgr.getLatestSucceededGenerationId();

	this.latestGenerationId = vmArcMgr.getLatestGenerationId();
    }

    public EntityType getEntityType() {
	return this.entityType;
    }

    public int getLatestGenerationId() {
	return this.latestGenerationId;
    }

    public int getLatestSucceededGenerationId() {
	return this.latestSucceededGenerationId;
    }

    public String getMoref() {
	return this.moref;
    }

    public String getName() {
	return this.name;
    }

    public long getTimestampMsOfLatestGenerationId() {
	return this.timestampMsOfLatestGenerationId;
    }

    public long getTimestampMsOfLatestSucceededGenerationId() {
	return this.timestampMsOfLatestSucceededGenerationId;
    }

    public String getTimestampOfLatestGenerationId() {
	return this.timestampOfLatestGenerationId;
    }

    public String getTimestampOfLatestSucceededGenerationId() {
	return this.timestampOfLatestSucceededGenerationId;
    }

    public String getUuid() {
	return this.uuid;
    }

    public boolean isFull() {
	return this.full;
    }

    public void setEntityType(final EntityType entityType) {
	this.entityType = entityType;
    }

    public void setFull(final boolean full) {
	this.full = full;
    }

    public void setLatestGenerationId(final int latestGenerationId) {
	this.latestGenerationId = latestGenerationId;
    }

    public void setLatestSucceededGenerationId(final int latestSucceededGenerationId) {
	this.latestSucceededGenerationId = latestSucceededGenerationId;
    }

    public void setMoref(final String moref) {
	this.moref = moref;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public void setTimestampMsOfLatestGenerationId(final long timestampMsOfLatestGenerationId) {
	this.timestampMsOfLatestGenerationId = timestampMsOfLatestGenerationId;
    }

    public void setTimestampMsOfLatestSucceededGenerationId(final long timestampMsOfLatestSucceededGenerationId) {
	this.timestampMsOfLatestSucceededGenerationId = timestampMsOfLatestSucceededGenerationId;
    }

    public void setTimestampOfLatestGenerationId(final String timestampOfLatestGenerationId) {
	this.timestampOfLatestGenerationId = timestampOfLatestGenerationId;
    }

    public void setTimestampOfLatestSucceededGenerationId(final String timestampOfLatestSucceededGenerationId) {
	this.timestampOfLatestSucceededGenerationId = timestampOfLatestSucceededGenerationId;
    }

    public void setUuid(final String uuid) {
	this.uuid = uuid;
    }

// TODO Remove unused code found by UCDetector
//     public String toCsv() {
// 	String latestGenerationIdStr = ((this.latestGenerationId > -1) ? String.valueOf(this.latestGenerationId) : "-");
// 	String latestSucceededGenerationIdStr = ((this.latestSucceededGenerationId > -1)
// 		? String.valueOf(this.latestSucceededGenerationId)
// 		: "-");
// 	return String.format("%s,%s,%s,%s,%s,%s,%s,%s", this.entityType.toString(true),
// 		(this.entityType == EntityType.ImprovedVirtualDisk) ? "-" : this.moref, this.uuid,
// 		latestSucceededGenerationIdStr, this.timestampOfLatestSucceededGenerationId, latestGenerationIdStr,
// 		this.timestampOfLatestGenerationId, this.name);
//     }

    @Override
    public String toString() {
	final String latestGenerationIdStr = ((this.latestGenerationId > -1) ? String.valueOf(this.latestGenerationId)
		: "-");
	final String latestSucceededGenerationIdStr = ((this.latestSucceededGenerationId > -1)
		? String.valueOf(this.latestSucceededGenerationId)
		: "-");
	final String morefSt = ((this.entityType == EntityType.ImprovedVirtualDisk)
		|| (this.entityType == EntityType.K8sNamespace)) ? "-" : this.moref;
	return String.format("%s\t%14s\t%s\t%s\t%-24s\t%s\t%s\t\t%s", this.entityType.toString(true), morefSt,
		this.uuid, latestSucceededGenerationIdStr, this.timestampOfLatestSucceededGenerationId,
		latestGenerationIdStr, this.timestampOfLatestGenerationId, this.name);
    }

}
