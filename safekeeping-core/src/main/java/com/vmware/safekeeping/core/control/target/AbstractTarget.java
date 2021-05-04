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
package com.vmware.safekeeping.core.control.target;

import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.command.options.AbstractCoreTargetRepository;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;

abstract class AbstractTarget implements ITarget {
    //
    public static final String ACTIVE_KEY = "active";
    protected static final String MIME_BINARY_OCTECT_STREAM = "binary/octet-stream";
    protected static final String MIME_TEXT_PLAIN_STREAM = "text/plain";
    protected Logger logger;
    protected String targetType;
    protected AbstractCoreTargetRepository options;
    private Boolean enable;

    protected AbstractTarget(final AbstractCoreTargetRepository options) {
        this.options = options;
        this.enable = options.isEnable();
    }

    @Override
    public LinkedHashMap<String, String> defaultConfigurations() {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put(ACTIVE_KEY, "false");

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - end - return value=" + result); //$NON-NLS-1$
        }
        return result;
    }

    @Override
    public String getName() {
        return this.options.getName();
    }

    @Override
    public String getTargetType() {
        return this.targetType;
    }

    @Override
    public boolean isEnable() {
        return this.enable;
    }

    @Override
    public LinkedHashMap<String, String> manualConfiguration() {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put(ACTIVE_KEY, "Enable the Plugin [%s]");

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - end - return value=" + result); //$NON-NLS-1$
        }
        return result;
    }

    protected abstract boolean post(final String path, final ByteArrayInOutStream digestOutput,
            final String contentType);

    @Override
    public boolean updateFcoProfileCatalog(final ByteArrayInOutStream byteArrayStream) {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("ByteArrayInOutStream byteArrayStream=" + byteArrayStream + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final String contentName = CoreGlobalSettings.getGlobalProfileFileName();
        if (this.logger.isLoggable(Level.INFO)) {
            final String msg = String.format("Post vmbk.profile:%s to %s", contentName, getTargetType());
            this.logger.info(msg);
        }
        final boolean result = post(contentName, byteArrayStream, MIME_TEXT_PLAIN_STREAM);

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(
                    "ByteArrayInOutStream byteArrayStream=" + byteArrayStream + " - end - return value=" + result); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

}
