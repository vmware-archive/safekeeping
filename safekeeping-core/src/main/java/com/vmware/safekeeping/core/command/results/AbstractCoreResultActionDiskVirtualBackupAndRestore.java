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
package com.vmware.safekeeping.core.command.results;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;

public abstract class AbstractCoreResultActionDiskVirtualBackupAndRestore extends AbstractCoreResultDiskBackupRestore {

    /**
     * 
     */
    private static final long serialVersionUID = 2165144008574912251L;
    private final List<CoreResultActionGetGenerationProfile> diskRestoreGenerationsProfile;
    private long totalDumpSize;
    private int numberOfReplacedBlock;

    private int totalBlocks;
    private int numberOfRelocatedBlocks;
    private final Map<Integer, GenerationProfile> profiles;
    private final Map<Integer, Integer> blocksPerGeneration;

    protected AbstractCoreResultActionDiskVirtualBackupAndRestore(final GenerationProfile profile,
            final AbstractCoreResultActionVirtualBackupForEntityWithDisks parent) {
        this(0, profile, parent);
    }

    protected AbstractCoreResultActionDiskVirtualBackupAndRestore(final GenerationProfile profile,
            final AbstractCoreResultActionRestoreForEntityWithDisks parent) {
        this(0, profile, parent);
    }

    protected AbstractCoreResultActionDiskVirtualBackupAndRestore(final int diskId, final GenerationProfile profile,
            final AbstractCoreResultActionVirtualBackupForEntityWithDisks parent) {
        super(diskId, profile, parent);
        parent.addResultActionOnDisk(this);
        this.diskRestoreGenerationsProfile = Collections
                .synchronizedList(new LinkedList<CoreResultActionGetGenerationProfile>());
        this.blocksPerGeneration = new TreeMap<>();
        this.profiles = new TreeMap<>();
    }

    protected AbstractCoreResultActionDiskVirtualBackupAndRestore(final int diskId, final GenerationProfile profile,
            final AbstractCoreResultActionRestoreForEntityWithDisks parent) {
        super(diskId, profile, parent);
        parent.addResultActionOnDisk(this);
        this.diskRestoreGenerationsProfile = Collections
                .synchronizedList(new LinkedList<CoreResultActionGetGenerationProfile>());
        this.blocksPerGeneration = new TreeMap<>();
        this.profiles = new TreeMap<>();
    }

    /**
     * @return the blocksPerGeneration
     */
    public Map<Integer, Integer> getBlocksPerGeneration() {
        return this.blocksPerGeneration;
    }

    public List<CoreResultActionGetGenerationProfile> getDiskRestoreGenerationsProfile() {
        return this.diskRestoreGenerationsProfile;
    }

    /**
     * @return the generationList
     */
    public Set<Integer> getGenerationList() {
        return this.profiles.keySet();
    }

    public int getNumberOfGenerations() {
        return this.profiles.size();
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

    @Override
    public AbstractCoreResultActionBackupRestore getParent() {
        return (AbstractCoreResultActionBackupRestore) this.parent;
    }

    @Override
    public IFirstClassObject getFirstClassObjectParent() {
        return getParent().getFirstClassObject();
    }

    /**
     * @return the profiles
     */
    public Map<Integer, GenerationProfile> getProfiles() {
        return this.profiles;
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

    @Override
    public int getTotalNumberOfDisks() {
        return getProfile().getNumberOfDisks();
    }

    /**
     * @param generationId
     */
    public void increaseNumberOfBlocksForGeneration(final int generationId) {
        this.blocksPerGeneration.put(generationId, (this.blocksPerGeneration.get(generationId) + 1));

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

}
