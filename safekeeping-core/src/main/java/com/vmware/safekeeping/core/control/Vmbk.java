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
package com.vmware.safekeeping.core.control;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.NoSuchPaddingException;

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.common.BiosUuid;
import com.vmware.safekeeping.core.core.SJvddk;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.soap.sso.SecurityUtil;
import com.vmware.safekeeping.core.util.AESEncryptionManager;

public class Vmbk {

    private static final Logger logger = Logger.getLogger(Vmbk.class.getName());
    public static final String NO_CONNECTION = "No vCenter is connected";
    public static final String NO_TARGET_CONNECTION = "No target is configured and connected";

    private static final AtomicBoolean abort = new AtomicBoolean(false);
    public static final String OPERATION_ABORTED_BY_USER = "Operation Aborted by User";
    public static final String INVALID_COMMAND = "Invalid command";

    public static void abortAnyPendingOperation(final long millis) {
        logger.warning("Abort request triggered");
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        Vmbk.abort.set(true);
    }

    public static void cancelAbortRequest() {
        Vmbk.abort.set(false);
        logger.warning("Abort request removed");
    }

    public static boolean isAbortTriggered() {
        return abort.get();
    }

    private ConnectionManager connetionManager;

    private boolean configured;

    public void close() {
        if (this.connetionManager != null) {
            getConnetionManager().close();
        }
        SJvddk.exit();
        ThreadsManager.shutdown();

    }

    public Boolean configure(final String configPath) throws IOException {

        final File configFile = new File(configPath);
        if (configFile.exists()) {
            this.configured = CoreGlobalSettings.loadConfig(configFile);

        }
        return this.configured;
    }

    public ConnectionManager getConnetionManager() {
        return this.connetionManager;
    }

    public String getVersion() {
        return SafekeepingVersion.getInstance().getExtendedVersion() + "\n"
                + BiosUuid.getInstance().getExtendedVersion() + "\n";
    }

    public void initialize(final int numberOfConcurrentsFcoThreads, final int numberOfVddkThreads,
            final int numberOfConcurrentsArchiveThreads)
            throws JVixException, IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, SafekeepingException, URISyntaxException {
        System.setProperty("com.sun.org.apache.xml.internal.security.lineFeedOnly", "true");

        SafekeepingVersion.initialize(CoreGlobalSettings.getVddkVersion());
        ThreadsManager.initialize(numberOfConcurrentsFcoThreads, numberOfVddkThreads,
                numberOfConcurrentsArchiveThreads);

        final SecurityUtil aesCert = SecurityUtil.loadFromKeystore(CoreGlobalSettings.getKeystorePath(),
                CoreGlobalSettings.getKeyStorePassword(), "aes");
        AESEncryptionManager.initialize(new String(aesCert.getUserCert().getSignature()));

    }

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(final boolean configured) {
        this.configured = configured;
    }

    /**
     * @param connetionManager2
     * @return
     */
    public void setConnetionManager(final ConnectionManager connetionManager) {
        this.connetionManager = connetionManager;
    }

}
