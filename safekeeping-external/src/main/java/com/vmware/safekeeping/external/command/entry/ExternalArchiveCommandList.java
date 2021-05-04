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
package com.vmware.safekeeping.external.command.entry;

import java.text.ParseException;
import java.util.logging.Level;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractArchiveCommand;
import com.vmware.safekeeping.core.command.options.CoreArchiveOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItemsList;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.external.command.support.Tasks;
import com.vmware.safekeeping.external.exception.InternalCoreResult;
import com.vmware.safekeeping.external.type.ResultThread;
import com.vmware.safekeeping.external.type.options.archive.ArchiveListOptions;

public class ExternalArchiveCommandList extends AbstractArchiveCommand implements Runnable {

    private CoreResultActionArchiveItemsList rac;

    private ITarget target;

    public ExternalArchiveCommandList() {
        setOptions(new CoreBasicCommandOptions());
    }

    public ExternalArchiveCommandList(final ArchiveListOptions options) throws ParseException {
        final CoreArchiveOptions loptions = new CoreArchiveOptions();
        ArchiveListOptions.convert(options, loptions);
        setOptions(loptions);
    }

    public ResultThread action(final ITarget target) throws InternalCoreResult {
        this.target = target;
        ResultThread result = null;
        this.rac = new CoreResultActionArchiveItemsList(target);
        final Thread thread = new Thread(this);
        result = new ResultThread(this.rac, thread.getId());
        thread.setName(ExternalArchiveCommandList.class.getName());
        if (target == null) {
            try {
                this.rac.failure(Tasks.NO_REPOSITORY_TARGET_ERROR_MESSAGE);
            } catch (final CoreResultActionException e) {
                Utility.logWarning(this.logger, e);
                throw new InternalCoreResult();
            }
        } else {
            thread.start();
        }
        return result;

    }

    @Override
    public void initialize() {
        setOptions(new CoreArchiveOptions());
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    /**
     * @return the target
     */
    public ITarget getTarget() {
        return this.target;
    }

    @Override
    public void run() {
        try {
            actionList(this.target, this.rac);
        } catch (final CoreResultActionException e) {
            Utility.logWarning(this.logger, e);
        } finally {
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.info("ResultActionConnectThread end");
            }
        }
    }
}
