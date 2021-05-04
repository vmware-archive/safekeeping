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
package com.vmware.safekeeping.core.command.results.miscellanea;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionWithSubOperations;
import com.vmware.safekeeping.core.type.VmdkInfo;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;

public class CoreResultActionCreateSnap extends AbstractCoreResultActionImpl {
    /**
     * 
     */
    private static final long serialVersionUID = -1376248709991254999L;
    private String snapId;
    private String snapName;
    private String snapMoref;
    private String snapDescription;
    private SnapshotManager snapshotManager;
    private int tentative;
    private String snapBackingObjectId;
    private final List<VmdkInfo> vmdkInfoList;

    public CoreResultActionCreateSnap() {
        this.vmdkInfoList = new LinkedList<>();
    }

    /**
     * @param result
     */
    public CoreResultActionCreateSnap(final AbstractCoreResultActionWithSubOperations parent) {
        super(parent);
        setFcoEntityInfo(parent.getFcoEntityInfo());
        this.vmdkInfoList = new LinkedList<>();
        this.tentative = 0;
        parent.getSubOperations().add(this);
    }

    public String getSnapBackingObjectId() {
        return this.snapBackingObjectId;
    }

    public String getSnapDescription() {
        return this.snapDescription;
    }

    public String getSnapId() {
        return this.snapId;
    }

    public String getSnapMoref() {
        return this.snapMoref;
    }

    public String getSnapName() {
        return this.snapName;
    }

    public SnapshotManager getSnapshotManager() {
        return this.snapshotManager;
    }

    public int getTentative() {
        return this.tentative;
    }

    public List<VmdkInfo> getVmdkInfoList() {
        return this.vmdkInfoList;
    }

    public int newTentative() {
        return ++this.tentative;
    }

    public void setSnapBackingObjectId(final String backingObjectId) {
        this.snapBackingObjectId = backingObjectId;
    }

    public void setSnapDescription(final String snapDescription) {
        this.snapDescription = snapDescription;
    }

    public void setSnapId(final String snapId) {
        this.snapId = snapId;
    }

    public void setSnapMoref(final String snapMoref) {
        this.snapMoref = snapMoref;
    }

    public void setSnapName(final String snapName) {
        this.snapName = snapName;
    }

    public void setSnapshotManager(final SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

}
