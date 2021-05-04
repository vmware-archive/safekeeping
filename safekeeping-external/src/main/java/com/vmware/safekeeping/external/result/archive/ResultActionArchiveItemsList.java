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
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItemsList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItem;

public class ResultActionArchiveItemsList extends AbstractResultActionArchive {
    public static void convert(final CoreResultActionArchiveItemsList src, final ResultActionArchiveItemsList dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        AbstractResultActionArchive.convert(src, dst);
        for (final CoreResultActionArchiveItem item : src.getItems()) {
            final ResultActionArchiveItem newItem = new ResultActionArchiveItem();
            ResultActionArchiveItem.convert(item, newItem);
            dst.getItems().add(newItem);
        }

    }

    private List<ResultActionArchiveItem> items;

    public ResultActionArchiveItemsList() {
        this.items = new LinkedList<>();
    }

    @Override
    public void convert(ICoreResultAction src) {
        ResultActionArchiveItemsList.convert((CoreResultActionArchiveItemsList) src, this);
    }

    /**
     * @return the items
     */
    public List<ResultActionArchiveItem> getItems() {
        return this.items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(final List<ResultActionArchiveItem> items) {
        this.items = items;
    }

}
