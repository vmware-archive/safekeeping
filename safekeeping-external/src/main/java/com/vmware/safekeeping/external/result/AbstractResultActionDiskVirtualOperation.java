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
package com.vmware.safekeeping.external.result;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionDiskVirtualBackupAndRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGetGenerationProfile;
import com.vmware.safekeeping.external.command.support.Task;

public abstract class AbstractResultActionDiskVirtualOperation extends AbstractResultDiskBackupRestore {

    public static void convert(final AbstractCoreResultActionDiskVirtualBackupAndRestore src,
            final AbstractResultActionDiskVirtualOperation dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        try {
            AbstractResultDiskBackupRestore.convert(src, dst);
            dst.setTotalNumberOfDisks(src.getTotalNumberOfDisks());
            dst.setTotalDumpSize(src.getTotalDumpSize());
            dst.setNumberOfReplacedBlock(src.getNumberOfReplacedBlock());

            dst.setTotalBlocks(src.getTotalBlocks());
            dst.setNumberOfRelocatedBlocks(src.getNumberOfRelocatedBlocks());
            dst.setGenerationList(src.getProfiles().keySet());
            dst.setNumberOfGenerations(src.getNumberOfGenerations());
            for (final CoreResultActionGetGenerationProfile _drgp : src.getDiskRestoreGenerationsProfile()) {
                dst.getDiskRestoreGenerationsProfileTasks().add(new Task(_drgp));
            }
        } catch (final Exception e) {
            src.failure(e);
            ResultAction.convert(src, dst);
        }
    }

    private long totalDumpSize;

    private int numberOfReplacedBlock;

    private int totalBlocks;

    private int numberOfRelocatedBlocks;

    private Set<Integer> generationList;

    private int numberOfGenerations;

    private List<Task> diskRestoreGenerationsProfileTasks;
    private int totalNumberOfDisks;

    protected AbstractResultActionDiskVirtualOperation() {
        this.diskRestoreGenerationsProfileTasks = new LinkedList<>();
    }

    /**
     * @return the diskRestoreGenerationsProfileTasks
     */
    public List<Task> getDiskRestoreGenerationsProfileTasks() {
        return this.diskRestoreGenerationsProfileTasks;
    }

    /**
     * @return the generationList
     */
    public Set<Integer> getGenerationList() {
        return this.generationList;
    }

    /**
     * @return the numberOfGenerations
     */
    public int getNumberOfGenerations() {
        return this.numberOfGenerations;
    }

    /**
     * @return the numberOfRelocatedBlocks
     */
    public int getNumberOfRelocatedBlocks() {
        return this.numberOfRelocatedBlocks;
    }

    /**
     * @return the numberOfReplacedBlock
     */
    public int getNumberOfReplacedBlock() {
        return this.numberOfReplacedBlock;
    }

    /**
     * @return the totalBlocks
     */
    public int getTotalBlocks() {
        return this.totalBlocks;
    }

    /**
     * @return the totalDumpSize
     */
    public long getTotalDumpSize() {
        return this.totalDumpSize;
    }

    /**
     * @return the totalNumberOfDisks
     */
    @Override
    public int getTotalNumberOfDisks() {
        return this.totalNumberOfDisks;
    }

    /**
     * @param diskRestoreGenerationsProfileTasks the
     *                                           diskRestoreGenerationsProfileTasks
     *                                           to set
     */
    public void setDiskRestoreGenerationsProfileTasks(final List<Task> diskRestoreGenerationsProfileTasks) {
        this.diskRestoreGenerationsProfileTasks = diskRestoreGenerationsProfileTasks;
    }

    /**
     * @param set the generationList to set
     */
    public void setGenerationList(final Set<Integer> set) {
        this.generationList = set;
    }

    /**
     * @param numberOfGenerations the numberOfGenerations to set
     */
    public void setNumberOfGenerations(final int numberOfGenerations) {
        this.numberOfGenerations = numberOfGenerations;
    }

    /**
     * @param numberOfRelocatedBlocks the numberOfRelocatedBlocks to set
     */
    public void setNumberOfRelocatedBlocks(final int numberOfRelocatedBlocks) {
        this.numberOfRelocatedBlocks = numberOfRelocatedBlocks;
    }

    /**
     * @param numberOfReplacedBlock the numberOfReplacedBlock to set
     */
    public void setNumberOfReplacedBlock(final int numberOfReplacedBlock) {
        this.numberOfReplacedBlock = numberOfReplacedBlock;
    }

    /**
     * @param totalBlocks the totalBlocks to set
     */
    public void setTotalBlocks(final int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    /**
     * @param totalDumpSize the totalDumpSize to set
     */
    public void setTotalDumpSize(final long totalDumpSize) {
        this.totalDumpSize = totalDumpSize;
    }

    /**
     * @param totalNumberOfDisks the totalNumberOfDisks to set
     */
    @Override
    public void setTotalNumberOfDisks(final int totalNumberOfDisks) {
        this.totalNumberOfDisks = totalNumberOfDisks;
    }
}
