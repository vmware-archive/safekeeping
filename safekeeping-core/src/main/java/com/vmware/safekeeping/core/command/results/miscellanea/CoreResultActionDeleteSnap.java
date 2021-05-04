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
import com.vmware.safekeeping.core.command.results.support.SnapshotInfo;
import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;

public class CoreResultActionDeleteSnap extends AbstractCoreResultActionImpl {
    /**
     * 
     */
    private static final long serialVersionUID = -3205049133098433701L;

    private final List<SnapshotInfo> snapList = new LinkedList<>();

    private String snapId;

    private String snapName;

    private String snapMoref;

    private String snapDescription;

    private SnapshotManager snapshotManager;

    public CoreResultActionDeleteSnap() {
    }

    /**
     * @param result
     */
    public CoreResultActionDeleteSnap(final AbstractCoreResultActionWithSubOperations parent) {

        parent.getSubOperations().add(this);
        parent.setFcoEntityInfo(parent.getFcoEntityInfo());
    }

    public CoreResultActionDeleteSnap(final CoreResultActionCreateSnap createSnapshotAction) {
        super(createSnapshotAction.getParent());
        this.snapId = createSnapshotAction.getSnapId();
        this.snapName = createSnapshotAction.getSnapName();
        this.snapMoref = createSnapshotAction.getSnapMoref();
        this.snapDescription = createSnapshotAction.getSnapDescription();
        this.snapshotManager = createSnapshotAction.getSnapshotManager();
    }

    public String getSnapDescription() {
        return this.snapDescription;
    }

    public String getSnapId() {
        return this.snapId;
    }

    public List<SnapshotInfo> getSnapList() {
        return this.snapList;

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
