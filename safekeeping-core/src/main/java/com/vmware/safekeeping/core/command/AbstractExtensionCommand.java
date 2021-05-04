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
package com.vmware.safekeeping.core.command;

import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreExtensionOptions;
import com.vmware.safekeeping.core.command.options.ExtensionManagerOptions;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionExtension;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.VimOperationException;
import com.vmware.safekeeping.core.exception.VimPermissionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public abstract class AbstractExtensionCommand extends AbstractCommandWithOptions {

    protected CoreResultActionExtension actionExtension(final ConnectionManager connetionManager,
            ExtensionManagerOptions extensionOp) throws CoreResultActionException {
        final CoreResultActionExtension result = new CoreResultActionExtension();
        try {
            result.start();
            result.setOperation(getOptions().getExtensionOperation());
            for (Entry<String, VimConnection> entry : connetionManager.getVimConnections().entrySet()) {
                try {
                    result.setExtension(entry.getValue().getExtensionManager()
                            .action(getOptions().getExtensionOperation(), getOptions().isForce(), extensionOp));
                } catch (RuntimeFaultFaultMsg | DatatypeConfigurationException | NotFoundFaultMsg
                        | VimOperationException | VimPermissionException e) {
                    Utility.logWarning(logger, e);
                    result.failure(e);
                    break;
                }
            }
        } finally {
            result.done();
        }
        return result;
    }

    @Override
    protected String getLogName() {
        return this.getClass().getName();
    }

    /**
     * @return the options
     */
    @Override
    public CoreExtensionOptions getOptions() {
        return (CoreExtensionOptions) this.options;
    }

}
