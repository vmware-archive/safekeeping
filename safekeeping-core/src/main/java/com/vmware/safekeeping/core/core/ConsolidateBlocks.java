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
package com.vmware.safekeeping.core.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionDiskVirtualBackupAndRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionGetGenerationProfile;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.profile.BasicBlockInfo;

public class ConsolidateBlocks {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ConsolidateBlocks.class.getName());
    private final AbstractCoreResultActionDiskVirtualBackupAndRestore radr;
    private int checkTotalBlocks;
    private final TreeMap<Long, BasicBlockInfo> vixBlocks;
    private boolean sortByGenerations;
    private final ConsolidateStatistic statistic;

    public ConsolidateBlocks(final AbstractCoreResultActionDiskVirtualBackupAndRestore radr) {
        this.radr = radr;
        this.checkTotalBlocks = 0;
        this.vixBlocks = new TreeMap<>();
        this.statistic = new ConsolidateStatistic();
    }

    private void addBlock(final BasicBlockInfo newBlock) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock - start"); //$NON-NLS-1$
        }

        this.vixBlocks.put(newBlock.getOffset(), newBlock);
        if (logger.isLoggable(Level.FINE)) {
            final String msg = "New Entry " + newBlock.toString();
            logger.log(Level.FINE, msg);
        }
        this.statistic.incTotalSize(newBlock.getLength());

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock - end"); //$NON-NLS-1$
        }
    }

    private void blockSplit(final BasicBlockInfo block1, final BasicBlockInfo newBlock) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock, RestoreBlock - start"); //$NON-NLS-1$
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,
                    "block1.getOffset() < newBlock.getOffset() && block1.getLastBlock() > newBlock.getLastBlock()");
        }
        final BasicBlockInfo block3 = new BasicBlockInfo(block1);
        this.statistic.decTotalSize(block1.getLength());
        block1.setLastBlock(newBlock.getOffset() - 1);

        this.statistic.incTotalSize(block1.getLength());
        block3.setStartBlock(newBlock.getLastBlock() + 1);
//	block3.setInternalIndex((block3.getGenerationId() * 10000) + (this.statistic.incIndex()));
        this.statistic.incResizedByOverlap();
        addBlock(newBlock);
        if (this.vixBlocks.containsKey(block3.getOffset())) {
            logger.log(Level.WARNING, () -> "Something is wrong");
            check(block3);
        } else {
            this.vixBlocks.put(block3.getOffset(), block3);
            this.statistic.incTotalSize(block3.getLength());
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock, RestoreBlock - end"); //$NON-NLS-1$
        }
    }

    private void check(final BasicBlockInfo newBlock) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock - start"); //$NON-NLS-1$
        }

        Long relCeilingOffset = newBlock.getOffset();
        Long relFloorOffset = newBlock.getOffset();

        while ((relFloorOffset != null) || (relCeilingOffset != null)) {
            if (relCeilingOffset != null) {
                final Entry<Long, BasicBlockInfo> block1CeilingEntry = this.vixBlocks.ceilingEntry(relCeilingOffset);
                if (block1CeilingEntry == null) {
                    relCeilingOffset = null;
                } else {
                    final BasicBlockInfo block1 = block1CeilingEntry.getValue();
                    if (newBlock.equals(block1) || checkCeiling(newBlock, block1)) {
                        ++relCeilingOffset;
                    } else {
                        relCeilingOffset = null;
                    }
                }
            }
            if (relFloorOffset != null) {
                final Entry<Long, BasicBlockInfo> block1FloorEntry = this.vixBlocks.floorEntry(relFloorOffset);
                if (block1FloorEntry == null) {
                    relFloorOffset = null;
                } else {
                    final BasicBlockInfo block1 = block1FloorEntry.getValue();
                    if (newBlock.equals(block1) || checkFloor(newBlock, block1)) {
                        --relFloorOffset;
                    } else {
                        relFloorOffset = null;
                    }
                }
            }
        }

        /*
         * Block doens't exist
         */
        if (this.vixBlocks.containsKey(newBlock.getOffset())) {
            if (this.vixBlocks.get(newBlock.getOffset()).equals(newBlock)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Block already in the list: " + newBlock.toString());
                }
            } else {
                logger.log(Level.WARNING, () -> "Block who are you?: " + newBlock.toString());
            }
        } else {
            addBlock(newBlock);
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock - end"); //$NON-NLS-1$
        }
    }

    private boolean checkCeiling(final BasicBlockInfo newBlock, final BasicBlockInfo block1) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock, RestoreBlock - start"); //$NON-NLS-1$
        }
        boolean result = false;
        if (block1.getOffset() == newBlock.getOffset()) {
            logger.log(Level.INFO, () -> "Duplicated entry :" + newBlock.getOffset());
            if (block1.getLastBlock() > newBlock.getLastBlock()) {
                // block1.offset ==newBlock.offset. && block1.lastBlock >newBlock.lastBlock.
                // block1.offset=newBlock.lastBlock+1
                // Block1.lastBlock=block1.lastBlock
                // Block2.offset=newBlock.offset
                // Block2.lastBlock=newBlock.lastBlock
                this.vixBlocks.remove(block1.getOffset());
                this.statistic.decTotalSize(block1.getLength());
                block1.setStartBlock(newBlock.getLastBlock() + 1);
                addBlock(newBlock);
                if (this.vixBlocks.containsKey(block1.getOffset())) {
                    logger.log(Level.WARNING,
                            () -> String.format("Block1 offset already present:%s", block1.toString()));
                    check(block1);
                } else {
                    addBlock(block1);
                }
                result = true;
            } else if (block1.getLastBlock() <= newBlock.getLastBlock()) {
                // block1.offset ==newBlock.offset. && block1.lastBlock <=newBlock.lastBlock.
                // block1=null
                // Block2.offset=newBlock.offset
                // Block2.lastBlock=newBlock.lastBlock
                this.vixBlocks.remove(block1.getOffset());
                this.statistic.decTotalSize(block1.getLength());
                this.statistic.incNumberReplacement();
                addBlock(newBlock);

                result = true;
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Nothing to do :" + newBlock.getOffset());
                }
            }
        } else if ((block1.getOffset() > newBlock.getOffset()) && (block1.getLastBlock() <= newBlock.getLastBlock())) {
            // block1.offset > newBlock.offset. && block1.lastBlock <=newBlock.lastBlock.
            // block1=null
            // Block2.offset=newBlock.offset
            // Block2.lastBlock=newBlock.lastBlock
            this.vixBlocks.remove(block1.getOffset());
            this.statistic.decTotalSize(block1.getLength());
            this.statistic.incNumberReplacement();
            addBlock(newBlock);

            result = true;
        } else if ((block1.getOffset() > newBlock.getOffset()) && (block1.getLastBlock() >= newBlock.getLastBlock())
                && (block1.getOffset() <= newBlock.getLastBlock())) {
            // block1.offset > newBlock.offset. and
            // block1.lastBlock > newBlock.lastBlock and block1.offset<newBlock.lastBlock
            // block1.offset=newBlock.lastBlock+1
            // Block1.lastBlock=block1.lastBlock
            // Block2.offset=newBlock.offset
            // Block2.lastBlock=newBlock.lastBlock
            this.vixBlocks.remove(block1.getOffset());
            this.statistic.decTotalSize(block1.getLength());
            block1.setStartBlock(newBlock.getLastBlock() + 1);
            addBlock(newBlock);
            if (this.vixBlocks.containsKey(block1.getOffset())) {
                logger.log(Level.WARNING, () -> String.format("Block1 offset already present:%s", block1.toString()));
                check(block1);
            } else {
                addBlock(block1);
            }
            result = true;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Nothing to do :" + block1.getOffset());
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock, RestoreBlock - end"); //$NON-NLS-1$
        }
        return result;
    }

    private boolean checkFloor(final BasicBlockInfo newBlock, final BasicBlockInfo block1) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock, RestoreBlock - start"); //$NON-NLS-1$
        }
        boolean result = false;
        if (block1.getOffset() < newBlock.getOffset()) {
            if (((block1.getOffset() < newBlock.getLastBlock()) && (block1.getLastBlock() > newBlock.getLastBlock()))
                    || (block1.getLastBlock() > newBlock.getLastBlock())) {
                blockSplit(block1, newBlock);
                result = true;
            } else if ((block1.getLastBlock() < newBlock.getLastBlock())
                    && (block1.getLastBlock() > newBlock.getOffset())) {
                // block1.offset <newBlock.offset. and
                // block1.lastBlock <newBlock.lastBlock and block1.lastBlock>newBlock.offset
                // block1.offset=block1.offset
                // Block1.lastBlock=newBlock.offset-1
                // Block2.offset=newBlock.offset
                // Block2.lastBlock=newBlock.lastBlock
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,
                            () -> "block1.getOffset() < newBlock.getOffset() && (block1.getLastBlock() < newBlock.getLastBlock())"
                                    + " && (block1.getLastBlock() > newBlock.getOffset())");
                }
                this.statistic.decTotalSize(block1.getLength());
                block1.setLastBlock(newBlock.getOffset() - 1);
                this.statistic.incTotalSize(block1.getLength());
                addBlock(newBlock);

                result = true;
            } else if (block1.getLastBlock() == newBlock.getLastBlock()) {
                // block1.offset <newBlock.offset. && block1.lastBlock ==newBlock.lastBlock.
                // block1.offset=block1.offset
                // Block1.lastBlock=newBlock.offset-1
                // Block2.offset=newBlock.offset
                // Block2.lastBlock=newBlock.lastBlock
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,
                            () -> "block1.getOffset() < newBlock.getOffset() && block1.getLastBlock() == newBlock.getLastBlock()");
                }
                this.statistic.decTotalSize(block1.getLength());
                block1.setLastBlock(newBlock.getOffset() - 1);
                this.statistic.incTotalSize(block1.getLength());
                addBlock(newBlock);

                result = true;
            }
        } else if (block1.getOffset() == newBlock.getOffset()) {
            logger.log(Level.WARNING, () -> "block1.getOffset() == newBlock.getOffset()");
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                        "block1.getOffset() <newBlock.getOffset() && newBlock.getLastBlock()<block1.getOffset() )");
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("RestoreBlock, RestoreBlock - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Consolidate blocks from different generations
     *
     * @param target
     * @param radr
     * @param interactive
     * @return List of consolidated blocks
     * @throws IOException
     * @throws CoreResultActionException
     */
    public List<BasicBlockInfo> compute() throws CoreResultActionException {

        final ListIterator<CoreResultActionGetGenerationProfile> li = this.radr.getDiskRestoreGenerationsProfile()
                .listIterator(this.radr.getDiskRestoreGenerationsProfile().size());
        logger.log(Level.INFO, () -> String.format("Consolidate Generations: %s",
                StringUtils.join(this.radr.getGenerationList(), ",")));

        while (li.hasPrevious()) {
            final CoreResultActionGetGenerationProfile raggp = li.previous();
            this.radr.getBlocksPerGeneration().put(raggp.getGenerationId(), 0);
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO,
                        () -> String.format("Analize generationId:%d mode:%s compressed:%b ciphered:%b blocks:%d",
                                raggp.getGenerationId(), raggp.getBackupMode().toString(), raggp.isCompressed(),
                                raggp.isCiphered(), raggp.getVixBlocks().size()));
                if (raggp.getVixBlocks().isEmpty()) {

                    logger.log(Level.INFO, () -> String.format("GenerationId:%d  No blocks", raggp.getGenerationId()));
                } else {
                    logger.log(Level.INFO, () -> String.format("GenerationId:%d  %d blocks", raggp.getGenerationId(),
                            raggp.getVixBlocks().size()));
                }
            }
            this.checkTotalBlocks += raggp.getVixBlocks().size();
            for (final BasicBlockInfo newBlock : raggp.getVixBlocks()) {
                this.statistic.incTotalBlocks();
                check(newBlock);

            }
        }
        final List<BasicBlockInfo> result = getResult(this.vixBlocks);
        summarize(result);
        return result;
    }

    /**
     *
     * @param vixBlocks
     * @return
     * @throws CoreResultActionException
     */
    private List<BasicBlockInfo> getResult(final Map<Long, BasicBlockInfo> vixBlocks) throws CoreResultActionException {

        final List<BasicBlockInfo> result = new ArrayList<>();
        int index = 0;
        BasicBlockInfo prev = null;
        final TreeMap<Integer, List<BasicBlockInfo>> sortByGenerationsTreeMap = new TreeMap<>();
        for (final BasicBlockInfo value : vixBlocks.values()) {
            value.setIndex(index++);
            this.radr.increaseNumberOfBlocksForGeneration(value.getGenerationId());
            if ((prev != null) && (value.getOffset() <= prev.getLastBlock()) && logger.isLoggable(Level.WARNING)) {

                logger.log(Level.WARNING, String.format("Gen:%d  offset:%d  <= previous(%d) lastBlock:%d",
                        value.getGenerationId(), value.getOffset(), prev.getGenerationId(), prev.getLastBlock()));
                this.radr.failure("Blocks order not alligned check log for more details");
                break;
            }
            if (value.getLength() > this.statistic.getMaxBlockLength()) {
                this.statistic.setMaxBlockLength(value.getLength());
            }
            if (this.sortByGenerations) {
                if (sortByGenerationsTreeMap.containsKey(value.getGenerationId())) {
                    sortByGenerationsTreeMap.get(value.getGenerationId()).add(value);
                } else {
                    sortByGenerationsTreeMap.put(value.getGenerationId(), new LinkedList<>());
                    sortByGenerationsTreeMap.get(value.getGenerationId()).add(value);
                }
            } else {
                result.add(value);
            }
            prev = value;
        }
        if (this.radr.isRunning() && this.sortByGenerations) {
            result.addAll(sortBlocksByGenerations(sortByGenerationsTreeMap));
        }

        return result;
    }

    public ConsolidateStatistic getStatistic() {
        return this.statistic;
    }

    public boolean isSortByGenerations() {
        return this.sortByGenerations;
    }

    public void setSortByGenerations(final boolean sortByGenerations) {
        this.sortByGenerations = sortByGenerations;
    }

    private List<BasicBlockInfo> sortBlocksByGenerations(final TreeMap<Integer, List<BasicBlockInfo>> source) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("TreeMap<Integer,List<RestoreBlock>> - start"); //$NON-NLS-1$
        }

        final List<BasicBlockInfo> result = new ArrayList<>();
        int index = 0;
        for (final Entry<Integer, List<BasicBlockInfo>> entry : source.entrySet()) {
            for (final BasicBlockInfo value : entry.getValue()) {
                value.setIndex(index++);
                result.add(value);
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("TreeMap<Integer,List<RestoreBlock>> - end"); //$NON-NLS-1$
        }
        return result;
    }

    public ConsolidateBlocks sortByGeneration() {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        this.sortByGenerations = true;

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return this;
    }

    private void summarize(final List<BasicBlockInfo> result) throws CoreResultActionException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("List<RestoreBlock> - start"); //$NON-NLS-1$
        }

        if (this.checkTotalBlocks != ((result.size() + this.statistic.getNumberReplacement())
                - this.statistic.getResizedByOverlap())) {
            this.radr.failure(String.format("Blocks check number mismatch %d vs %d", this.checkTotalBlocks,
                    this.vixBlocks.size() + this.statistic.getNumberReplacement()));
        } else {
            if (logger.isLoggable(Level.INFO)) {
                final String msg = String.format(
                        "Total:%d  Final blocks:%d TotalSize:%d Max BlockLength %d Replacement:%d Relocated:%d relocatedGreaterLength:%d removedByOverlap:%d resizedByOverlap:%d",
                        this.statistic.getTotalBlocks(), result.size(), this.statistic.getTotalSize(),
                        this.statistic.getMaxBlockLength() * jDiskLibConst.SECTOR_SIZE,
                        this.statistic.getNumberReplacement(), this.statistic.getRelocated(),
                        this.statistic.getRelocatedGreaterLength(), this.statistic.getRemovedByOverlap(),
                        this.statistic.getResizedByOverlap());
                logger.info(msg);
            }
            this.radr.setNumberOfReplacedBlock(this.statistic.getNumberReplacement());
            this.radr.setNumberOfBlocks(result.size());
            this.radr.setTotalBlocks(this.statistic.getTotalBlocks());
            this.radr.setNumberOfRelocatedBlocks(this.statistic.getRelocated());
            this.radr.setTotalDumpSize(this.statistic.getTotalSize() * jDiskLibConst.SECTOR_SIZE);
            this.radr.setMaxBlockSize(this.statistic.getMaxBlockLength().intValue());
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("List<RestoreBlock> - end"); //$NON-NLS-1$
        }
    }

}
