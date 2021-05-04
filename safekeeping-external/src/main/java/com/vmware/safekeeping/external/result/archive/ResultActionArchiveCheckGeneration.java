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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGeneration;
import com.vmware.safekeeping.core.type.GeneretionDependenciesInfo;

public class ResultActionArchiveCheckGeneration extends AbstractResultActionArchive {
    public static void convert(final CoreResultActionArchiveCheckGeneration src,
            final ResultActionArchiveCheckGeneration dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionArchive.convert(src, dst);
        dst.getDependents().addAll(src.getDependents());
        dst.setDependenciesInfo(src.getDependenciesInfo());
        dst.getMd5fileCheck().addAll(src.getMd5fileCheck());
        dst.setTimestampMs(src.getTimestampMs());
        dst.setGenId(src.getGenId());
        dst.setNumOfFiles(src.getNumOfFiles());

    }

    private List<Boolean> md5fileCheck;
    private List<GeneretionDependenciesInfo> dependents;
    private long timestampMs;
    private int genId;
    private int numOfFiles;
    private GeneretionDependenciesInfo dependenciesInfo;

    public ResultActionArchiveCheckGeneration() {
        this.dependents = new LinkedList<>();
        this.md5fileCheck = new ArrayList<>();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionArchiveCheckGeneration.convert((CoreResultActionArchiveCheckGeneration) src, this);
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
     * @return the md5fileCheck
     */
    public List<Boolean> getMd5fileCheck() {
        return this.md5fileCheck;
    }

    /**
     * @return the numOfFiles
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
     * @param genId the genId to set
     */
    public void setGenId(final int genId) {
        this.genId = genId;
    }

    /**
     * @param md5fileCheck the md5fileCheck to set
     */
    public void setMd5fileCheck(final List<Boolean> md5fileCheck) {
        this.md5fileCheck = md5fileCheck;
    }

    /**
     * @param numOfFiles the numOfFiles to set
     */
    public void setNumOfFiles(final int numOfFiles) {
        this.numOfFiles = numOfFiles;
    }

    /**
     * @param timestampMs the timestampMs to set
     */
    public void setTimestampMs(final long timestampMs) {
        this.timestampMs = timestampMs;
    }

}
