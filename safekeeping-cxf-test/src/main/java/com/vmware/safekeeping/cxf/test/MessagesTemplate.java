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
package com.vmware.safekeeping.cxf.test;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.PrettyNumber;
import com.vmware.safekeeping.common.PrettyNumber.MetricPrefix;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.cxf.test.common.ConsoleWrapper;
import com.vmware.sapi.AbstractBasicCommandOptions;
import com.vmware.sapi.AbstractResultActionBackupForEntityWithDisks;
import com.vmware.sapi.AbstractResultActionBackupRestore;
import com.vmware.sapi.AbstractResultActionDiskVirtualOperation;
import com.vmware.sapi.AbstractResultActionVirtualBackupForEntityWithDisks;
import com.vmware.sapi.AbstractResultDiskBackupRestore;
import com.vmware.sapi.BackupMode;
import com.vmware.sapi.FcoTarget;
import com.vmware.sapi.GenerationInfo;
import com.vmware.sapi.GuestInfoFlags;
import com.vmware.sapi.IvdLocation;
import com.vmware.sapi.RestoreIvdManagedInfo;
import com.vmware.sapi.RestoreVappManagedInfo;
import com.vmware.sapi.RestoreVmManagedInfo;
import com.vmware.sapi.ResultActionDiskBackup;
import com.vmware.sapi.ResultActionVmBackup;
import com.vmware.sapi.VappLocation;
import com.vmware.sapi.VmLocation;

/**
 * @author mdaneri
 *
 */
public final class MessagesTemplate {

    /*
     * VixDiskLib Open Flags
     */
    public static final int OPEN_UNBUFFERED = (1 << 0);
    public static final int OPEN_SINGLE_LINK = (1 << 1);
    public static final int OPEN_READ_ONLY = (1 << 2);
    public static final int OPEN_COMPRESSION_ZLIB = (1 << 4);
    public static final int OPEN_COMPRESSION_FASTLZ = (1 << 5);
    public static final int OPEN_COMPRESSION_SKIPZ = (1 << 6);

    private static final String COMPRESS_HEADER = "    n  tot    c e     offset        last           size    compress     time      n-speed   c-ratio      r-speed\tmd5";

    private static final String LINE_SEPARATOR = "##################################################################################################################################################################################################################";
    private static final String STANDARD_HEADER = "    n  tot          offset       size     time      speed\t\tmd5 ";

    public static String compressHeader() {
        return COMPRESS_HEADER;
    }

    public static String diskDumpHeaderInfo(final AbstractResultActionDiskVirtualOperation radb) {
        return String.format(
                "[tot blocks %d][redundant %d][blocks %d][size %s][Handle %d][Flags %s][source %s][threads %d][blockSize %s]",
                radb.getTotalBlocks(), radb.getNumberOfReplacedBlock(), radb.getNumberOfBlocks(),
                PrettyNumber.toString(radb.getTotalDumpSize(), MetricPrefix.AUTO, 2), radb.getDiskHandle(),
                printFlags(radb.getFlags()), radb.getTargetName(), radb.getNumberOfThreads(),
                PrettyNumber.toString(radb.getMaxBlockSizeInBytes(), MetricPrefix.MEGA, 2));
    }

    public static String diskDumpHeaderInfo(final ResultActionDiskBackup radb) {
        final String queryMode = radb.getBackupMode() == BackupMode.FULL
                ? ("[" + radb.getQueryBlocksOption().toString().toLowerCase(Utility.LOCALE) + "]")
                : "";
        return String.format("Dump: [blocks %d]%s[Handle %d][Flags %s][target %s][threads %d][blockSize %s]",
                radb.getNumberOfBlocks(), queryMode, radb.getDiskHandle(), printFlags(radb.getFlags()),
                radb.getTargetName(), radb.getNumberOfThreads(),
                PrettyNumber.toString(radb.getMaxBlockSize(), MetricPrefix.MEGA));

    }

    public static String diskHeaderInfo(final AbstractResultDiskBackupRestore radr) {
        try {
            final String transport = radr.getUsedTransportModes()
                    .concat((StringUtils.isEmpty(radr.getRequestedTransportModes())) ? ""
                            : " (ask ".concat(radr.getRequestedTransportModes().concat(")")));

            return String.format("Disk(%2d/%2d): [size %s][diskId %d][uuid %s][trnsprt %s]%s", radr.getDiskId() + 1,
                    radr.getTotalNumberOfDisks(),
                    PrettyNumber.toString(radr.getCapacityInBytes(), MetricPrefix.AUTO, 2), radr.getDiskId(),
                    radr.getUuid(), transport,
                    (radr.getVDiskId() != null) ? ("[IVD ID " + radr.getVDiskId() + " ]") : "");
        } catch (final Exception e) {
            return "";
        }
    }

    public static String getDiskGenerationsInfo(final AbstractResultActionDiskVirtualOperation radr) {
        final StringBuilder result = new StringBuilder();
        result.append(String.format("Generations(%d): [", radr.getNumberOfGenerations()));

        result.append(String.format("%d(full)", radr.getGenerationList().get(0)));
        for (int i = 1; i < radr.getNumberOfGenerations(); i++) {
            result.append(String.format(", %d(inc)", radr.getGenerationList().get(i)));
        }
        result.append("]");
        return result.toString();
    }

    public static String getGenerationInfo(final AbstractResultActionBackupForEntityWithDisks resultActionBackup) {
        String returnString = null;
        final String encrStr = (resultActionBackup.isCipher()) ? "[cipher on]" : StringUtils.EMPTY;
        final String compStr = (resultActionBackup.isCompressed()) ? "[compress on]" : StringUtils.EMPTY;
        final GenerationInfo gen = resultActionBackup.getGenerationInfo();
        if (gen == null) {
            returnString = "Generation: No Info";
        } else {
            final String dependStr = (gen.getBackupMode() == BackupMode.FULL) || (gen.getPreviousGenerationId() < 0)
                    ? "no dependence"
                    : ("depend on " + gen.getPreviousGenerationId().toString());
            returnString = String.format("Generation: [number %d][%s][mode %s]%s%s[Uri %s]", gen.getGenerationId(),
                    dependStr, gen.getBackupMode().toString(), compStr, encrStr, gen.getTargetUri());
        }
        return returnString;

    }

    public static String getGenerationInfo(final AbstractResultActionVirtualBackupForEntityWithDisks rac) {
        String returnString = null;
        final GenerationInfo gen = rac.getGenerationInfo();
        if (gen == null) {
            returnString = "Generation: No Info";
        } else {
            final String dependStr = (gen.getBackupMode() == BackupMode.FULL) || (gen.getPreviousGenerationId() < 0)
                    ? "no dependence"
                    : ("depend on " + gen.getPreviousGenerationId().toString());
            returnString = String.format("Generation: [number %d][%s][mode %s][Uri %s]", gen.getGenerationId(),
                    dependStr, gen.getBackupMode().toString(), gen.getTargetUri());
        }
        return returnString;

    }

    public static String getHeaderGuestInfoString(final ResultActionVmBackup resultActionBackup) {
        final GuestInfoFlags flags = resultActionBackup.getGuestFlags();
        final StringBuilder sum = new StringBuilder("GuestInfo:  ");

        if (flags.isTemplate()) {
            sum.append("[templateVM true]");
        }
        if (Boolean.TRUE.equals(flags.isVbsEnabled())) {

            sum.append("[vbs true]");
        }
        if (flags.isVAppConfigAvailable()) {
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
        return sum.toString();

    }

    /**
     * @return
     * @throws InterruptedException
     */
    public static String getLocationString(final AbstractResultActionBackupRestore resultActionBackup) {
        String result = StringUtils.EMPTY;
        if (resultActionBackup.getLocations() != null) {

            switch (resultActionBackup.getFcoEntityInfo().getEntityType()) {
            case K_8_S_NAMESPACE:
                break;
            case IMPROVED_VIRTUAL_DISK:
                final IvdLocation ivdLocation = (IvdLocation) resultActionBackup.getLocations();
                result = String.format("Location:   [Datastore %s][diskPath %s][vmdk %s]",
                        ivdLocation.getDatastoreInfo().getName(), ivdLocation.getDatastorePath(),
                        ivdLocation.getVmdkFileName());
                break;
            case VIRTUAL_APP:
                final VappLocation vAppLocation = (VappLocation) resultActionBackup.getLocations();
                final String vAppFolder = (vAppLocation.isVAppMember())
                        ? ("vApp " + vAppLocation.getResourcePoolInfo().getName())
                        : ("vmFolder " + vAppLocation.getVmFolderFullPath());
                result = String.format("Location:   [ResourcePool %s] [ %s]", vAppLocation.getResourcePoolFullPath(),
                        vAppFolder);
                break;
            case VIRTUAL_MACHINE:
                final VmLocation location = (VmLocation) resultActionBackup.getLocations();
                final String vmFolder = (location.isVAppMember()) ? ("vApp " + location.getResourcePoolInfo().getName())
                        : ("vmFolder " + location.getVmFolderFullPath());
                result = String.format("Location:   [ResourcePool %s][Datastore %s]%n            [%s ][vmPath %s]",
                        location.getResourcePoolFullPath(), location.getDatastoreInfo().getName(), vmFolder,
                        location.getDatastorePath());
                break;
            default:
                break;
            }
        }
        return result;
    }

    public static String getRestoreManagedInfo(final RestoreIvdManagedInfo rmi) {

        final StringBuilder sum = new StringBuilder("Restore Info :  ");
        if (rmi.isRecovery()) {
            sum.append("[Recovery]");
        }
        if (rmi.getName() != null) {
            sum.append("[name ");
            sum.append(rmi.getName());
            sum.append(" ]");
        }
        if (rmi.getDcInfo() != null) {
            sum.append("[dc ");
            sum.append(rmi.getDcInfo().getName());
            sum.append(" ]");
        }

        return sum.toString();
    }

    public static String getRestoreManagedInfo(final RestoreVappManagedInfo rmi) {
        final StringBuilder sum = new StringBuilder("Restore Info :  ");
        if (rmi.isRecovery()) {
            sum.append("[Recovery]");
        }
        if (rmi.getName() != null) {
            sum.append("[name ");
            sum.append(rmi.getName());
            sum.append(" ]");
        }
        if (rmi.getDcInfo() != null) {
            sum.append("[dc ");
            sum.append(rmi.getDcInfo().getName());
            sum.append(" ]");
        }

        if (rmi.getFolderInfo() != null) {
            sum.append("[folder ");
            sum.append(rmi.getFolderInfo().getName());
            sum.append(" ]");
        }

        if (rmi.getResourcePoolInfo() != null) {
            sum.append("[ResPool ");
            sum.append(rmi.getResourcePoolInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getVAppConfig() != null) {
            sum.append("[vApp config]");
        }

        return sum.toString();
    }

    public static String getRestoreManagedInfo(final RestoreVmManagedInfo rmi) {
        final StringBuilder sum = new StringBuilder("Restore Info :  ");
        if (rmi.isRecovery()) {
            sum.append("[Recovery]");
        }
        if (rmi.getName() != null) {
            sum.append("[name ");
            sum.append(rmi.getName());
            sum.append(" ]");
        }
        if (rmi.getDcInfo() != null) {
            sum.append("[dc ");
            sum.append(rmi.getDcInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getDsInfo() != null) {
            sum.append("[ds ");
            sum.append(rmi.getDsInfo().getName());
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
        if (rmi.getResourcePoolInfo() != null) {
            sum.append("[ResPool ");
            sum.append(rmi.getResourcePoolInfo().getName());
            sum.append(" ]");
        }
        if (rmi.getVAppConfig() != null) {
            sum.append("[vApp config]");
        }

        return sum.toString();
    }

    public static String printFlags(final int flags) {
        final StringBuilder printFlags = new StringBuilder();
        printFlags.append(((flags & OPEN_UNBUFFERED) == OPEN_UNBUFFERED) ? "OPEN_UNBUFFERED " : "");
        printFlags.append(((flags & OPEN_SINGLE_LINK) == OPEN_SINGLE_LINK) ? "OPEN_SINGLE_LINK " : "");
        printFlags.append(((flags & OPEN_READ_ONLY) == OPEN_READ_ONLY) ? "OPEN_READ_ONLY " : "");
        printFlags.append(((flags & OPEN_COMPRESSION_ZLIB) == OPEN_COMPRESSION_ZLIB) ? "OPEN_COMPRESSION_ZLIB " : "");
        printFlags.append(
                ((flags & OPEN_COMPRESSION_FASTLZ) == OPEN_COMPRESSION_FASTLZ) ? "OPEN_COMPRESSION_FASTLZ " : "");
        printFlags
                .append(((flags & OPEN_COMPRESSION_SKIPZ) == OPEN_COMPRESSION_SKIPZ) ? "OPEN_COMPRESSION_SKIPZ " : "");
        return printFlags.toString();
    }

    public static void printTestHeader(final String testName, final AbstractBasicCommandOptions options) {
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.printf(
                "************************************************************************\t\t%s - ",
                testName.toUpperCase(Utility.LOCALE));
        for (final FcoTarget fco : options.getTargetList()) {
            ConsoleWrapper.console.printf(" %s:%s ", fco.getKeyType().toString(), fco.getKey());
        }
        ConsoleWrapper.console.println("\t\t************************************");
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println();
    }

    public static String separetorBar(final boolean compress) {
        if (compress) {
            return "________________________________________________________________________________________________________________";
        } else {
            return "_________________________________________________________";
        }
    }

    public static String standardHeader() {
        return STANDARD_HEADER;
    }

    private MessagesTemplate() {
        throw new IllegalStateException("Utility class");
    }

}
