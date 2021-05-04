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

import com.vmware.safekeeping.core.command.AbstractExtensionCommand;
import com.vmware.safekeeping.core.command.options.CoreExtensionOptions;
import com.vmware.safekeeping.core.command.options.ExtensionManagerOptions;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.external.result.connectivity.ResultActionExtension;
import com.vmware.safekeeping.external.type.options.ExtensionOptions;

public class ExternalExtensionCommand extends AbstractExtensionCommand {

    public ResultActionExtension action(final ConnectionManager connectionManager, ExtensionManagerOptions extensionOp)
            throws CoreResultActionException {
        ResultActionExtension result = new ResultActionExtension();
        ResultActionExtension.convert(actionExtension(connectionManager, extensionOp), result);
        return result;
    }

    public ExternalExtensionCommand(final ExtensionOptions options) {
        CoreExtensionOptions basicOptions = new CoreExtensionOptions();
        ExtensionOptions.convert(options, basicOptions);
        setOptions(basicOptions);
    }

    @Override
    protected void initialize() {
        // default implementation ignored
    }
}
