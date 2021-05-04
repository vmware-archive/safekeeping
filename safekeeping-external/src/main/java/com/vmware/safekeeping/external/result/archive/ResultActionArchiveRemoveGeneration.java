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
package com.vmware.safekeeping.external.result.archive;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGeneration;
import com.vmware.safekeeping.core.type.GeneretionDependenciesInfo;

public class ResultActionArchiveRemoveGeneration extends AbstractResultActionArchive {
    public static void convert(final CoreResultActionArchiveRemoveGeneration src,
            final ResultActionArchiveRemoveGeneration dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionArchive.convert(src, dst);
        dst.getDependents().addAll(src.getDependents());
        dst.setDependenciesInfo(src.getDependenciesInfo());
        dst.setGenerationDataRemoved(src.isGenerationDataRemoved());
        dst.setGenerationProfileMetadataRemoved(src.isGenerationProfileMetadataRemoved());
        dst.setTimestampMs(src.getTimestampMs());
        dst.setGenId(src.getGenId());

    }

    private List<GeneretionDependenciesInfo> dependents;

    private boolean generationDataRemoved;
    private boolean generationProfileMetadataRemoved;
    private long timestampMs;
    private int genId;
    private GeneretionDependenciesInfo dependenciesInfo;

    public ResultActionArchiveRemoveGeneration() {
        this.dependents = new LinkedList<>();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionArchiveRemoveGeneration.convert((CoreResultActionArchiveRemoveGeneration) src, this);
    }

    /**
     * @return the dependenciesInfo
     */
    public GeneretionDependenciesInfo getDependenciesInfo() {
        return this.dependenciesInfo;
    }

    /**
     * @return the dependents
     */
    public List<GeneretionDependenciesInfo> getDependents() {
        return this.dependents;
    }

    /**
     * @return the genId
     */
    public int getGenId() {
        return this.genId;
    }

    /**
     * @return the timestampMs
     */
    public long getTimestampMs() {
        return this.timestampMs;
    }

    /**
     * @return the generationDataRemoved
     */
    public boolean isGenerationDataRemoved() {
        return this.generationDataRemoved;
    }

    /**
     * @return the generationProfileMetadataRemoved
     */
    public boolean isGenerationProfileMetadataRemoved() {
        return this.generationProfileMetadataRemoved;
    }

    /**
     * @param dependenciesInfo the dependenciesInfo to set
     */
    public void setDependenciesInfo(final GeneretionDependenciesInfo dependenciesInfo) {
        this.dependenciesInfo = dependenciesInfo;
    }

    /**
     * @param dependents the dependents to set
     */
    public void setDependents(final List<GeneretionDependenciesInfo> dependents) {
        this.dependents = dependents;
    }

    /**
     * @param generationDataRemoved the generationDataRemoved to set
     */
    public void setGenerationDataRemoved(final boolean generationDataRemoved) {
        this.generationDataRemoved = generationDataRemoved;
    }

    /**
     * @param generationProfileMetadataRemoved the generationProfileMetadataRemoved
     *                                         to set
     */
    public void setGenerationProfileMetadataRemoved(final boolean generationProfileMetadataRemoved) {
        this.generationProfileMetadataRemoved = generationProfileMetadataRemoved;
    }

    /**
     * @param genId the genId to set
     */
    public void setGenId(final int genId) {
        this.genId = genId;
    }

    /**
     * @param timestampMs the timestampMs to set
     */
    public void setTimestampMs(final long timestampMs) {
        this.timestampMs = timestampMs;
    }

}
