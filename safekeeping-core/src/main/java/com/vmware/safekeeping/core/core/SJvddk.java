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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.jvix.CleanUpResults;
import com.vmware.jvix.JDisk;
import com.vmware.jvix.JDiskLibFactory;
import com.vmware.jvix.JVixException;
import com.vmware.jvix.jDiskLib.ConnectParams;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.common.GuestOsUtils;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.manipulator.VddkConfManipulator;

public class SJvddk {
    protected static BlockingQueue<DiskOpenCloseHandle> queue;

    protected static JDisk dli;

    private static final Logger logger = Logger.getLogger(SJvddk.class.getName());

    private static Thread openCloseVmdkThread;

    protected static final int CHUNK_SIZE = 128;

    public static CleanUpResults cleanup(final ConnectParams connectParams) {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("ConnectParams - start"); //$NON-NLS-1$
        }

        final CleanUpResults returnCleanUpResults = SJvddk.dli.cleanup(connectParams);
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("ConnectParams - end"); //$NON-NLS-1$
        }
        return returnCleanUpResults;
    }

    public static boolean cleanVddkTemp() {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - start"); //$NON-NLS-1$
        }
        String vmwareRoot = null;
        boolean result = true;
        try (final VddkConfManipulator vddkConf = new VddkConfManipulator(CoreGlobalSettings.getVddkConfig())) {
            vmwareRoot = vddkConf.getTmpDirectory();

            if (StringUtils.isNotEmpty(vmwareRoot)) {
                if (SJvddk.logger.isLoggable(Level.INFO)) {
                    SJvddk.logger.info("Deleting vddk temporary directory :" + vmwareRoot);
                }
                final File vddkTempFolder = new File(vmwareRoot);
                if (vddkTempFolder.exists() && vddkTempFolder.isDirectory()) {
                    Utility.deleteDirectoryRecursive(vddkTempFolder, false);
                }
            }
        } catch (final IOException e) {
            Utility.logWarning(SJvddk.logger, e);
            result = false;
        }
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return result;
    }

    public static void exit() {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        if ((SJvddk.openCloseVmdkThread != null) && SJvddk.openCloseVmdkThread.isAlive()) {
            SJvddk.openCloseVmdkThread.interrupt();
            SJvddk.logger.info("Shutting down VDDK threads");
        }
        if (SJvddk.dli != null) {
            SJvddk.logger.info("Closing VDDK Library");
            SJvddk.dli.exit();
            SJvddk.dli = null;
        }
        SJvddk.cleanVddkTemp();

        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - end"); //$NON-NLS-1$
        }

    }

    /**
     * @return the dli
     */
    public static JDisk getDli() {
        return SJvddk.dli;
    }

    public static boolean initialize(boolean vmcConfiguration) throws JVixException {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        long result;
        if (!SJvddk.isInitialized()) {

            try {

                final SafekeepingVersion ver = SafekeepingVersion.getInstance();
                if (ver.getVddk() == null) {
                    throw new JVixException("No VDDK version detected");
                }
                try (final VddkConfManipulator vddkConf = new VddkConfManipulator(CoreGlobalSettings.getVddkConfig())) {
                    // read noNfcSession value used to disable enableaccess
                    vddkConf.enablePhoneHome(true);
                    vddkConf.setTmpDirectory(GuestOsUtils.getTempDir());
                    vddkConf.setEnableSslFIPS(true);
                    vddkConf.setNoNfcSession(vmcConfiguration);
                    if (vddkConf.save()) {
                        SJvddk.logger.info("vddk.conf changed");
                    }
                    final File tmpDir = new File(vddkConf.getTmpDirectory());
                    if (!tmpDir.exists()) {
                        tmpDir.mkdirs();
                    }
                }

                SJvddk.dli = JDiskLibFactory.getInstance(ver.getVddk(), CoreGlobalSettings.isVddkOverwriteOnStart(),
                        CoreGlobalSettings.isVddkRemovedOnExit());

                SJvddk.logger.fine("VddkManager::initialize() begin.\n");
                result = SJvddk.dli.initEx(new com.vmware.safekeeping.core.logger.JVixLoggerImpl(),
                        CoreGlobalSettings.getVddkConfig());
                if (SJvddk.logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("%s VddkLibPath:%s VddkPluginsPath:%s Vddk_config:%s",
                            ver.getVddk().getExtendedVersion(), JDiskLibFactory.getLibDir(),
                            JDiskLibFactory.getVddkPluginsDirectory(), CoreGlobalSettings.getVddkConfig());
                    SJvddk.logger.info(msg);
                }
                if (result != jDiskLibConst.VIX_OK) {
                    final String msg = SJvddk.dli.getErrorText(result, null);
                    SJvddk.logger.warning(msg);

                    if (SJvddk.logger.isLoggable(Level.CONFIG)) {
                        SJvddk.logger.config("<no args> - end"); //$NON-NLS-1$
                    }
                    return false;
                }
            } catch (final JVixException | IOException e) {
                Utility.logWarning(SJvddk.logger, e);
                SJvddk.logger.info("VddkManager Initialization failure.");
                throw new JVixException(e);
            }
            SJvddk.logger.info("VddkManager Initialized successful.");
            if (SJvddk.logger.isLoggable(Level.INFO)) {
                SJvddk.logger.info("Transport modes available: " + SJvddk.dli.listTransportModes());
            }
            SJvddk.initializeOpenCloseVmdkThread();
        }

        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return true;
    }

    private static void initializeOpenCloseVmdkThread() {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final Runnable th = () -> {
            try {
                while (true) {
                    final DiskOpenCloseHandle handle = SJvddk.queue.take();
                    if (handle.isOpen()) {
                        handle.setVddkCallResult(SJvddk.dli.open(handle.getConnHandle(), handle.getRemoteDiskPath(),
                                handle.getFlags(), handle.getDiskHandle()));
                    } else {
                        handle.setVddkCallResult(SJvddk.dli.close(handle.getDiskHandle()));
                    }
                    handle.getExecuted().set(true);
                }

            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        SJvddk.queue = new LinkedBlockingQueue<>();
        SJvddk.openCloseVmdkThread = new Thread(th);
        SJvddk.openCloseVmdkThread.setName("OpenCloseVmdk");
        SJvddk.openCloseVmdkThread.start();

        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }

    public static boolean isInitialized() {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final boolean returnboolean = SJvddk.dli != null;
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnboolean;
    }

    public static String printFlags(final int flags) {
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("int - start"); //$NON-NLS-1$
        }

        final StringBuilder printFlags = new StringBuilder();
        printFlags.append(
                ((flags & jDiskLibConst.OPEN_UNBUFFERED) == jDiskLibConst.OPEN_UNBUFFERED) ? "OPEN_UNBUFFERED " : "");
        printFlags.append(
                ((flags & jDiskLibConst.OPEN_SINGLE_LINK) == jDiskLibConst.OPEN_SINGLE_LINK) ? "OPEN_SINGLE_LINK "
                        : "");
        printFlags.append(
                ((flags & jDiskLibConst.OPEN_READ_ONLY) == jDiskLibConst.OPEN_READ_ONLY) ? "OPEN_READ_ONLY " : "");
        printFlags.append(((flags & jDiskLibConst.OPEN_COMPRESSION_ZLIB) == jDiskLibConst.OPEN_COMPRESSION_ZLIB)
                ? "OPEN_COMPRESSION_ZLIB "
                : "");
        printFlags.append(((flags & jDiskLibConst.OPEN_COMPRESSION_FASTLZ) == jDiskLibConst.OPEN_COMPRESSION_FASTLZ)
                ? "OPEN_COMPRESSION_FASTLZ "
                : "");
        printFlags.append(((flags & jDiskLibConst.OPEN_COMPRESSION_SKIPZ) == jDiskLibConst.OPEN_COMPRESSION_SKIPZ)
                ? "OPEN_COMPRESSION_SKIPZ "
                : "");
        final String returnString = printFlags.toString();
        if (SJvddk.logger.isLoggable(Level.CONFIG)) {
            SJvddk.logger.config("int - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    protected SJvddk() {
    }

}
