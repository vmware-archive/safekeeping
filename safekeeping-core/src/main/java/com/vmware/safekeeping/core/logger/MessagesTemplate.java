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
package com.vmware.safekeeping.core.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionDiskVirtualBackupAndRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestoreForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultDiskBackupRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.control.info.CoreRestoreManagedInfo;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.core.SJvddk;
import com.vmware.safekeeping.core.profile.GenerationProfile;
import com.vmware.safekeeping.core.type.GenerationInfo;
import com.vmware.safekeeping.core.type.GuestInfoFlags;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.location.CoreIvdLocation;
import com.vmware.safekeeping.core.type.location.CoreVappLocation;
import com.vmware.safekeeping.core.type.location.CoreVmLocation;

public final class MessagesTemplate {

    private static Logger logger = Logger.getLogger("MessagesTemplate");

    public static String diskDumpHeaderInfo(final AbstractCoreResultActionDiskVirtualBackupAndRestore radb) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionDiskRestore radb=" + radb + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final String diskHandle = radb.getDiskHandle() != null
                ? String.format("[Handle %d]", radb.getDiskHandle().getHandle())
                : "";
        final String flag = (radb.getFlags() >= 0) ? ("[Flags " + SJvddk.printFlags(radb.getFlags()) + " ]") : "";

        final String returnString = String.format(
                "[tot blocks %d][redundant %d][blocks %d][size %s]%s%s[source %s][threads %d][blockSize %s]",
                radb.getTotalBlocks(), radb.getNumberOfReplacedBlock(), radb.getNumberOfBlocks(),
                PrettyNumber.toString(radb.getTotalDumpSize(), MetricPrefix.AUTO, 2), diskHandle, flag,
                radb.getTargetName(), radb.getNumberOfThreads(),
                PrettyNumber.toString(radb.getMaxBlockSizeInBytes(), MetricPrefix.MEGA, 2));
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionDiskRestore - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String diskDumpHeaderInfo(final CoreResultActionDiskBackup radb) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionDiskBackup radb=" + radb + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final String queryMode = (radb.getBackupMode() == BackupMode.FULL) ? ("[" + radb.getQueryBlocksOption() + "]")
                : "";
        final String returnString = String.format(Utility.LOCALE,
                "Dump: [blocks %d]%s[Handle %d][Flags %s][target %s][threads %d][blockSize %s]",
                radb.getNumberOfBlocks(), queryMode, radb.getDiskHandle().getHandle(),
                SJvddk.printFlags(radb.getFlags()), radb.getTargetName(), radb.getNumberOfThreads(),
                PrettyNumber.toString(radb.getMaxBlockSize(), MetricPrefix.MEGA));
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionDiskBackup - end"); //$NON-NLS-1$
        }
        return returnString;

    }

    public static String diskHeaderInfo(final AbstractCoreResultDiskBackupRestore radr) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultDiskBackupRestore radr=" + radr + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        final String transport;
        if (StringUtils.isNotBlank(radr.getUsedTransportModes())) {
            transport = "[trnsprt "
                    + radr.getUsedTransportModes().concat((StringUtils.isEmpty(radr.getRequestedTransportModes())) ? ""
                            : " (ask ".concat(radr.getRequestedTransportModes().concat(")")))
                    + " ]";
        } else {
            transport = "";
        }
        int numDisk = 1;
        if (radr.getParent() instanceof AbstractCoreResultActionBackupForEntityWithDisks) {
            numDisk = ((AbstractCoreResultActionBackupForEntityWithDisks) radr.getParent()).getNumberOfDisk();
        } else {
            if (radr.getParent() instanceof AbstractCoreResultActionRestoreForEntityWithDisks) {
                numDisk = ((AbstractCoreResultActionRestoreForEntityWithDisks) radr.getParent()).getNumberOfDisk();
            }
        }
        final String returnString = String.format(Utility.LOCALE, "Disk(%2d/%2d): [size %s][diskId %d][uuid %s]%s%s%s]",
                radr.getDiskId() + 1, numDisk, PrettyNumber.toString(radr.getCapacityInBytes(), MetricPrefix.AUTO, 2),
                radr.getDiskId(), radr.getUuid(), transport,
                (radr.getvDiskId() != null) ? ("[IVD ID " + radr.getvDiskId() + " ]") : "",
                radr.getName().replace("] ", "/"));

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultDiskBackupRestore - end"); //$NON-NLS-1$
        }
        return returnString;

    }

    public static String dumpInfo(ManagedEntityInfo entity, final ExBlockInfo dump) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("DumpBlockInfo dump=" + dump + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        final String returnString;
        if (Boolean.TRUE.equals(dump.isFailed())) {
            final String reason = (StringUtils.isNotEmpty(dump.getReason())) ? dump.getReason()
                    : "unknow - check the log";
            returnString = String.format(Utility.LOCALE, "(%4d/%4d)  FAILED\t\t%7.2fMB\t\t%5.2fs%n\tReason:(%s) %s ",
                    dump.getIndex() + 1, dump.getTotalBlocks(), dump.getSizeInMb(), dump.getOverallTimeInSeconds(),
                    entity.toString(), reason);

        } else {
            final String nominalSpeed;
            final String realSpeed;
            final char openParenthesis;
            final char closeParenthesis;
            if (dump.isDuplicated()) {
                nominalSpeed = "          -";
                realSpeed = "          -";
                openParenthesis = '[';
                closeParenthesis = ']';
            } else {
                nominalSpeed = (dump.isCompress()) ? String.format(Utility.LOCALE, "%7.2fMB/s", dump.getMbSec())
                        : "          -";
                realSpeed = String.format(Utility.LOCALE, "%7.2fMB/s", dump.getStreamMbSec());
                openParenthesis = '(';
                closeParenthesis = ')';
            }
            final char compress = (dump.isCompress()) ? 'x' : ' ';
            final char cipher = (dump.isCipher()) ? 'x' : ' ';
            final String compressStreamSize = (dump.isCompress()) ? dump.printStreamSize() : "       -";
            final char modified = (dump.isModified()) ? '*' : ' ';
            returnString = String.format(Utility.LOCALE,
                    "%c%4d/%4d%c %c %c %c %12d  %12d  %9s   %9s  %6.2fs  %s   %7s  %s\t%s\t%s", openParenthesis,
                    dump.getIndex() + 1, dump.getTotalBlocks(), closeParenthesis, modified, compress, cipher,
                    dump.getOffset(), dump.getLastBlock(), dump.printSize(), compressStreamSize,
                    dump.getOverallTimeInSeconds(), nominalSpeed, dump.getExplicitCompressionRatio(), realSpeed,
                    dump.getSha1(), dump.getMd5());

        }
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("DumpBlockInfo - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String getDiskGenerationsInfo(final AbstractCoreResultActionDiskVirtualBackupAndRestore radr) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionDiskRestore radr=" + radr + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final StringBuilder result = new StringBuilder();
        result.append(String.format(Utility.LOCALE, "Generations(%d): [", radr.getNumberOfGenerations()));
        for (final GenerationProfile profile : radr.getProfiles().values()) {
            if (profile.getBackupMode() == BackupMode.INCREMENTAL) {
                result.append(", ");
            }
            result.append(String.format("%d(%s)", profile.getGenerationId(), profile.getBackupMode().toString()));
        }
        result.append("]");
        final String returnString = result.toString();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionDiskRestore - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String getGenerationInfo(final AbstractCoreResultActionBackupRestore resultActionBackup) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config(
                    "CoreAbstractResultActionBackupRestore resultActionBackup=" + resultActionBackup + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String returnString = null;
        if (resultActionBackup instanceof CoreResultActionVmBackup) {
            returnString = getGenerationInfo((CoreResultActionVmBackup) resultActionBackup);
        } else if (resultActionBackup instanceof CoreResultActionVappBackup) {
            returnString = getGenerationInfo((CoreResultActionVappBackup) resultActionBackup);
        } else if (resultActionBackup instanceof CoreResultActionIvdBackup) {
            returnString = getGenerationInfo((CoreResultActionIvdBackup) resultActionBackup);
        } else {
            returnString = "";
        }
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultActionBackupRestore - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String getGenerationInfo(final CoreResultActionIvdBackup resultActionBackup) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionIvdBackup resultActionBackup=" + resultActionBackup + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final String encrStr = (resultActionBackup.isCipher()) ? "[cipher on]" : StringUtils.EMPTY;

        final String compStr = (resultActionBackup.isCompressed()) ? "[compress on]" : StringUtils.EMPTY;
        final GenerationInfo gen = resultActionBackup.getGenerationInfo();
        final String dependStr = (gen.getBackupMode() == BackupMode.FULL) || (gen.getPreviousGenerationId() < 0)
                ? "no dependence"
                : ("depend on " + gen.getPreviousGenerationId().toString());
        final String returnString = String.format(Utility.LOCALE, "Generation: [number %d][%s][mode %s]%s%s[Uri %s]",
                gen.getGenerationId(), dependStr, gen.getBackupMode().toString(), compStr, encrStr, gen.getTargetUri());
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionIvdBackup - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String getGenerationInfo(final CoreResultActionVappBackup resultActionBackup) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionVappBackup resultActionBackup=" + resultActionBackup + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final GenerationInfo gen = resultActionBackup.getGenerationInfo();
        final String returnString = String.format(Utility.LOCALE, "Generation: [number %d][Uri %s]",
                gen.getGenerationId(), gen.getTargetUri());
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionVappBackup - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String getGenerationInfo(final CoreResultActionVmBackup resultActionBackup) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionVmBackup resultActionBackup=" + resultActionBackup + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final String encrStr = (resultActionBackup.isCipher()) ? "[cipher on]" : StringUtils.EMPTY;
        final String compStr = (resultActionBackup.isCompressed()) ? "[compress on]" : StringUtils.EMPTY;
        final GenerationInfo gen = resultActionBackup.getGenerationInfo();
        final String dependStr = (gen.getBackupMode() == BackupMode.FULL) || (gen.getPreviousGenerationId() < 0)
                ? "no dependence"
                : ("depend on " + gen.getPreviousGenerationId().toString());
        final String returnString = String.format(Utility.LOCALE, "Generation: [number %d][%s][mode %s]%s%s[Uri %s]",
                gen.getGenerationId(), dependStr, gen.getBackupMode().toString(), compStr, encrStr, gen.getTargetUri());
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionVmBackup - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String getHeaderGuestInfoString(final CoreResultActionVmBackup resultActionBackup) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionVmBackup resultActionBackup=" + resultActionBackup + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final GuestInfoFlags flags = resultActionBackup.getGuestFlags();
        if (Boolean.FALSE.equals(flags.isDiskUuidEnabled())) {
            logger.warning("Disk UUIDs are not exposed to the guest.");
        }
        final StringBuilder sum = new StringBuilder("GuestInfo:  ");

        if (flags.isTemplate()) {
            sum.append("[templateVM true]");
        }
        if (Boolean.TRUE.equals(flags.isVbsEnabled())) {

            sum.append("[vbs true]");
        }
        if (flags.isvAppConfigAvailable()) {
            sum.append("[vApp config]");
        }
        if (Boolean.TRUE.equals(flags.isConfigurationEncrypted())) {

            sum.append("[encrypted config]");
        }
        sum.append("[guestOs ");
        sum.append(flags.getGuestFullName());
        sum.append("]");

        sum.append("[virtualDisk ");
        sum.append(flags.getNumberOfVirtualDisk());
        sum.append("]");
        final String returnString = sum.toString();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreResultActionVmBackup - end"); //$NON-NLS-1$
        }
        return returnString;

    }

    /**
     * @return
     * @throws InterruptedException
     */
    public static String getLocationString(final AbstractCoreResultActionBackupRestore resultActionBackup) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config(
                    "CoreAbstractResultActionBackupRestore resultActionBackup=" + resultActionBackup + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        String result = StringUtils.EMPTY;
        if (resultActionBackup.getLocations() != null) {

            switch (resultActionBackup.getEntityType()) {
            case K8sNamespace:
                break;
            case ImprovedVirtualDisk:
                final CoreIvdLocation ivdLocation = (CoreIvdLocation) resultActionBackup.getLocations();
                result = String.format(Utility.LOCALE, "Location:   [Datastore %s][diskPath %s][vmdk %s]",
                        ivdLocation.getDatastoreInfo().getName(), ivdLocation.getDatastorePath(),
                        ivdLocation.getVmdkFileName());
                break;
            case VirtualApp:
                final CoreVappLocation vAppLocation = (CoreVappLocation) resultActionBackup.getLocations();
                final String vAppFolder = (vAppLocation.isVAppMember())
                        ? ("vApp " + vAppLocation.getResourcePoolInfo().getName())
                        : ("vmFolder " + vAppLocation.getVmFolderFullPath());
                result = String.format(Utility.LOCALE, "Location:   [ResourcePool %s] [ %s]",
                        vAppLocation.getResourcePoolFullPath(), vAppFolder);
                break;
            case VirtualMachine:
                final CoreVmLocation location = (CoreVmLocation) resultActionBackup.getLocations();
                final String vmFolder = (location.isvAppMember()) ? ("vApp " + location.getResourcePoolInfo().getName())
                        : ("vmFolder " + location.getVmFolderFullPath());
                result = String.format(Utility.LOCALE,
                        "Location:   [ResourcePool %s][Datastore %s]%n            [%s ][vmPath %s]",
                        location.getResourcePoolFullPath(), location.getDatastoreInfo().getName(), vmFolder,
                        location.getDatastorePath());
                break;
            default:
                break;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultActionBackupRestore - end"); //$NON-NLS-1$
        }
        return result;
    }

    public static String getRestoreManagedInfo(final AbstractCoreResultActionRestore rar) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultActionRestore rar=" + rar + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final CoreRestoreManagedInfo rmi = rar.getManagedInfo();

        final StringBuilder sum = new StringBuilder("Restore Info :  ");
        if (rmi.isRecovery()) {
            sum.append("[Recovery]");
        } else {
            if (rmi.getName() != null) {
                sum.append("[name ");
                sum.append(rmi.getName());
                sum.append(" ]");
            }
        }
        if (rmi.getDcInfo() != null) {
            sum.append("[dc ");
            sum.append(rmi.getDcInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getDatastoreInfo() != null) {
            sum.append("[ds ");
            sum.append(rmi.getDatastoreInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getFolderInfo() != null) {
            sum.append("[folder ");
            sum.append(rmi.getFolderInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getHostInfo() != null) {
            sum.append("[host ");
            sum.append(rmi.getHostInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getResourcePollInfo() != null) {
            sum.append("[ResPool ");
            sum.append(rmi.getResourcePollInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getVAppConfig() != null) {
            sum.append("[vApp config]");
        }

        final String returnString = sum.toString();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("CoreAbstractResultActionRestore - end"); //$NON-NLS-1$
        }
        return returnString;
    }

    public static String header(final boolean compress) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("boolean compress=" + compress + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (compress) {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("boolean - end"); //$NON-NLS-1$
            }
            return "    n  tot    c e     offset        last           size    compress     time      n-speed   c-ratio      r-speed\tsha1                                                              \tmd5";
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("boolean - end"); //$NON-NLS-1$
            }
            return "    n  tot          offset       size     time      speed\t\tsha1                                                              \tmd5 ";
        }
    }

    public static String separatorBar(final boolean compress) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("boolean compress=" + compress + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (compress) {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("boolean - end"); //$NON-NLS-1$
            }
            return "________________________________________________________________________________________________________________";
        } else {
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("boolean - end"); //$NON-NLS-1$
            }
            return "_________________________________________________________";
        }
    }

    private MessagesTemplate() {
        throw new IllegalStateException("Utility class");
    }
}
