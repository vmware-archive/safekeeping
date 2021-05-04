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
package com.vmware.safekeeping.cmd.report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.DateUtility;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.interactive.AbstractStatusInteractive;
import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchiveStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveIvdStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVappStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVmStatus;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationDiskInfo;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationDisksInfoList;
import com.vmware.safekeeping.core.command.results.support.CoreGenerationVirtualMachinesInfoList;
import com.vmware.safekeeping.core.command.results.support.StatusDiskInfo;
import com.vmware.safekeeping.core.command.results.support.StatusVirtualMachineDiskInfo;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.AdapterProtocolType;

/**
 * @author mdaneri
 *
 */
public class StatusInteractive extends AbstractStatusInteractive {
    protected static Logger logger = Logger.getLogger("StatusInteractive");

    /**
     * @param archiveStatus
     */
    public StatusInteractive(final AbstractCoreResultActionArchiveStatus archiveStatus) {
        super(archiveStatus);
    }

    @Override
    public void endRetrieveGenerationInfo(final Integer genId) {
        super.endRetrieveGenerationInfo(genId);
        if (genId == getArchiveStatus().getLatestSucceededGenerationId()) {
            IoFunction.print("     (*)");
        } else {
            IoFunction.print(" \t");
        }
        switch (getArchiveStatus().getEntityType()) {
        case VirtualMachine:
            IoFunction.print(getGenerationStatusString((CoreResultActionArchiveVmStatus) getArchiveStatus(), genId));
            break;
        case VirtualApp:
            IoFunction.print(getGenerationStatusString((CoreResultActionArchiveVappStatus) getArchiveStatus(), genId));
            break;
        case ImprovedVirtualDisk:
            IoFunction.print(getGenerationStatusString((CoreResultActionArchiveIvdStatus) getArchiveStatus(), genId));
            break;
        default:
            break;
        }
        IoFunction.println();
    }

    @Override
    public void endRetrieveGenerations() {
        super.endRetrieveGenerations();
        if (getArchiveStatus().getNumOfSuccceededGeneration() >= 0) {
            IoFunction.printf("Generation(s):%d/%d\n", getArchiveStatus().getNumOfSuccceededGeneration(),
                    getArchiveStatus().getNumOfGeneration());
        } else {
            IoFunction.print("No succedded generation");
        }
        if (getArchiveStatus().isAvailable()) {
            switch (getArchiveStatus().getEntityType()) {
            case VirtualMachine:
                IoFunction.printf("   uuid:%s\tmoRef:%s", getArchiveStatus().getFcoEntityInfo().getUuid(),
                        getArchiveStatus().getFcoEntityInfo().getMorefValue());
                break;
            case VirtualApp:
                IoFunction.printf("   uuid:-\t\t\t\tmoRef:%s", getArchiveStatus().getFcoEntityInfo().getMorefValue());
                break;
            case ImprovedVirtualDisk:
                IoFunction.printf("   uuid:%s\tmoRef:-", getArchiveStatus().getFcoEntityInfo().getUuid());
                break;
            default:
                break;
            }
            IoFunction.println();
        }
        if (getArchiveStatus().isEmpty()) {
            IoFunction.println();
            IoFunction.println("\tArchive is empty. ");
        }
        IoFunction.print("\tGen\tDate\t\t\t");
        switch (getArchiveStatus().getEntityType()) {
        case VirtualMachine:
            for (int i = 0; i < 3; ++i) {
                IoFunction.print("\tn device  size(GB)  mode  Exec Time");
            }
            IoFunction.print("\t...");
            break;
        case VirtualApp:
            for (int i = 0; i < 3; ++i) {
                IoFunction.print("\tn:Virtual Machine\t\t\t\t\t\t\t");
            }
            IoFunction.print("\t...");
            break;
        case ImprovedVirtualDisk:
            IoFunction.print("\tsize(GB)  mode  Exec Time");
            break;
        default:
            break;
        }

        IoFunction.println();
    }

    @Override
    public void finish() {
        super.finish();
        IoFunction.println("\n(*) Latest generation");
    }

    public String getGenerationStatusString(final CoreResultActionArchiveIvdStatus resultAction, final Integer key) {
        final CoreGenerationDiskInfo genDisksInfo = resultAction.getGenerationDiskInfo().get(key);
        final StringBuilder sb = new StringBuilder();
        final int genId = genDisksInfo.getGenId();

        sb.append(String.format("%d \t%s\t", genId, DateUtility.toGMTString(genDisksInfo.getTimeStamp())));
        if (genDisksInfo.isGenerationSucceeded()) {
            try {
                final StatusDiskInfo diskInfo = genDisksInfo.getDisksInfo();
                final long millis = genDisksInfo.getDisksInfo().getDumpElapsedTimeMs();
                final String timeElapse = new SimpleDateFormat("mm:ss:SSS").format(new Date(millis));
                sb.append(String.format(Utility.LOCALE, "%-7.2f   %s  %s",
                        (((float) diskInfo.getCapacity()) / ((float) Utility.ONE_GBYTES)),
                        diskInfo.getBackupMode().toString(), timeElapse));
            } catch (final Exception e) {
                StatusInteractive.logger.warning(String.format("failed with generation %d.", genId));
                return String.format("%d ##########_ERROR_##########", genId);
            }
        } else {
            sb.append(" --------_FAILED_--------");
        }
        return sb.toString();
    }

    public String getGenerationStatusString(final CoreResultActionArchiveVappStatus resultAction, final Integer key) {
        final StringBuilder sb = new StringBuilder();
        final CoreGenerationVirtualMachinesInfoList genDisksInfo = resultAction.getGenerationVmInfoList().get(key);
        final int genId = genDisksInfo.getGenId();

        sb.append(String.format("%d \t%s\t", genId, DateUtility.toGMTString(genDisksInfo.getTimeStamp())));

        if (genDisksInfo.isGenerationSucceeded()) {
            try {
                int i = 0;
                for (final ManagedFcoEntityInfo entity : genDisksInfo.getVmsInfoList()) {
                    sb.append(String.format("%d:%s\t", ++i, entity.toString(true)));
                }
            } catch (final Exception e) {
                StatusInteractive.logger.warning(String.format("failed with generation %d.", genId));
                return String.format("%d ##########_ERROR_##########", genId);
            }
        } else {
            sb.append(" ----------_FAILED_----------");
        }
        return sb.toString();
    }

    public String getGenerationStatusString(final CoreResultActionArchiveVmStatus resultAction, final Integer key) {
        final CoreGenerationDisksInfoList genDisksInfo = resultAction.getGenerationDisksInfoList().get(key);
        final StringBuilder sb = new StringBuilder();
        final int genId = genDisksInfo.getGenId();

        sb.append(String.format("%d \t%s\t", genId, DateUtility.toGMTString(genDisksInfo.getTimeStamp())));

        if (genDisksInfo.isGenerationSucceeded()) {
            try {
                for (final StatusVirtualMachineDiskInfo diskInfo : genDisksInfo.getDisksInfoList()) {
                    final long millis = diskInfo.getDumpElapsedTimeMs();
                    final String timeElapse = new SimpleDateFormat("mm:ss:SSS").format(new Date(millis));
                    sb.append(String.format(Utility.LOCALE, "%d %s%s:%s %-7.2f   %s  %s\t", diskInfo.getDiskId(),
                            AdapterProtocolType.getProtocolType(diskInfo.getAdapterType()).toString(),
                            diskInfo.getBusNumber(), diskInfo.getUnitNumber(),
                            (((float) diskInfo.getCapacity()) / ((float) Utility.ONE_GBYTES)),
                            diskInfo.getBackupMode().toString(), timeElapse));
                }

            } catch (final Exception e) {
                StatusInteractive.logger.warning(String.format("failed with generation %d.", genId));
                return String.format("%d ##########_ERROR_##########", genId);
            }
        } else {
            sb.append(" ----------_FAILED_----------");
        }
        return sb.toString();
    }

    @Override
    public void start() {
        super.start();
        IoFunction.printf("%s\t", getArchiveStatus().getFcoEntityInfo().getName());
    }

//    @Override
//    public void startAccessArchive() {
//        super.startAccessArchive();
//    }
//
//    @Override
//    public void startRetrieveGenerationInfo() {
//        super.startRetrieveGenerationInfo();
//
//    }
//
//    @Override
//    public void startRetrieveGenerations() {
//        super.startRetrieveGenerations();
//
//    }
//    @Override
//    public void endAccessArchive() {
//        super.endAccessArchive();      
//    }
}
