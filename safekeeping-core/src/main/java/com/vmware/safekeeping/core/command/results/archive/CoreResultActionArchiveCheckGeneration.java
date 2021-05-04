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
package com.vmware.safekeeping.core.command.results.archive;

import java.util.ArrayList;
import java.util.List;

import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.type.GeneretionDependenciesInfo;

public class CoreResultActionArchiveCheckGeneration extends AbstractCoreResultActionArchive {

    /**
     * 
     */
    private static final long serialVersionUID = 4101750728688009808L;

    private final ConcurrentDoublyLinkedList<GeneretionDependenciesInfo> dependents;

    private long timestampMs;
    private int genId;
    private GeneretionDependenciesInfo dependenciesInfo;
    private int numOfFiles;
    private final List<Boolean> md5fileCheck;

    /**
     *
     * @param resultAction
     */
    public CoreResultActionArchiveCheckGeneration(final ITarget target,
            final CoreResultActionArchiveCheckGenerationWithDependencies resultAction) {
        super(target);
        this.parent = resultAction;
        resultAction.getSubOperations().add(this);
        this.dependents = new ConcurrentDoublyLinkedList<>();
        setFcoEntityInfo(resultAction.getFcoEntityInfo());
        this.md5fileCheck = new ArrayList<>();
    }

    /**
     * @return the dependent
     */
    public GeneretionDependenciesInfo getDependenciesInfo() {
        return this.dependenciesInfo;
    }

    public ConcurrentDoublyLinkedList<GeneretionDependenciesInfo> getDependents() {
        return this.dependents;
    }

    /**
     * @return the genId
     */
    public int getGenId() {
        return this.genId;
    }

    /**
     * @return the md5fileCheck
     */
    public List<Boolean> getMd5fileCheck() {
        return this.md5fileCheck;
    }

    /**
     * @return the numFiles
     */
    public int getNumOfFiles() {
        return this.numOfFiles;
    }

    /**
     * @return the timestampMs
     */
    public long getTimestampMs() {
        return this.timestampMs;
    }

    /**
     * @param dependent the dependent to set
     */
    public void setDependenciesInfo(final GeneretionDependenciesInfo dependent) {
        this.dependenciesInfo = dependent;
    }

    /**
     * @param genId the genId to set
     */
    public void setGenId(final int genId) {
        this.genId = genId;
    }

    /**
     * @param numFiles the numFiles to set
     */
    public void setNumOfFiles(final int numFiles) {
        this.numOfFiles = numFiles;
    }

    /**
     * @param timestampMs the timestampMs to set
     */
    public void setTimestampMs(final long timestampMs) {
        this.timestampMs = timestampMs;
    }

}
