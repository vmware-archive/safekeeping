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
package com.vmware.safekeeping.core.command.results.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vmware.safekeeping.core.type.enums.EntityType;

public class StatisticResult {

    private final Map<EntityType, StatisticResultByFco> statisticByFco;

    public StatisticResult() {
        this.statisticByFco = new LinkedHashMap<>();
    }

    /**
     * @param entityType
     * @return
     */
    public boolean containsEntity(final EntityType entityType) {
        return this.statisticByFco.containsKey(entityType);
    }

    /**
     *
     * @param entityType
     * @param result
     * @return
     */
    public int countResult(final EntityType entityType, final OperationState result) {
        this.statisticByFco.putIfAbsent(entityType, new StatisticResultByFco());
        return this.statisticByFco.get(entityType).countResult(result);
    }

    public int getAborted() {
        int count = 0;
        for (final Entry<EntityType, StatisticResultByFco> entry : this.statisticByFco.entrySet()) {
            count += entry.getValue().getAborted();
        }
        return count;
    }

    public int getFailure() {
        int count = 0;
        for (final Entry<EntityType, StatisticResultByFco> entry : this.statisticByFco.entrySet()) {
            count += entry.getValue().getFailure();
        }
        return count;
    }

    public int getFailure(final EntityType entityType) {
        return this.statisticByFco.get(entityType).getFailure();
    }

    public int getSkip() {
        int count = 0;
        for (final Entry<EntityType, StatisticResultByFco> entry : this.statisticByFco.entrySet()) {
            count += entry.getValue().getSkip();
        }
        return count;
    }

    public int getSkip(final EntityType entityType) {
        return this.statisticByFco.get(entityType).getSkip();
    }

    public int getSuccess() {
        int count = 0;
        for (final Entry<EntityType, StatisticResultByFco> entry : this.statisticByFco.entrySet()) {
            count += entry.getValue().getSuccess();
        }
        return count;
    }

    public int getSuccess(final EntityType entityType) {
        return this.statisticByFco.get(entityType).getSuccess();
    }

    /**
     * @return
     */
    public int getTotalByEntity(final EntityType entityType) {
        return this.statisticByFco.get(entityType).getTotal();
    }

}