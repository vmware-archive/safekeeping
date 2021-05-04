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

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.interactive.AbstractVirtualBackupInteractive;
import com.vmware.safekeeping.core.command.interactive.AbstractVirtualBackupVappInteractive;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskVirtualBackup;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.logger.MessagesTemplate;

public class VirtualBackupInteractive extends AbstractVirtualBackupInteractive {

    private static final String LINE_SEPARATOR = "##################################################################################################################################################################################################################";

    /**
     * @param rab
     */
    public VirtualBackupInteractive(final AbstractCoreResultActionVirtualBackupForEntityWithDisks rab,
            final AbstractVirtualBackupVappInteractive parent) {
        super(rab, parent);
    }

    @Override
    public void endFinalizeProfile() {
        super.endFinalizeProfile();
        if (getRaFcoVirtualBackup() != null) {
            IoFunction.println(MessagesTemplate.getGenerationInfo(getRaFcoVirtualBackup()));
        }

    }

    @Override
    public void finish() {
        super.finish();
        if (getParent() != null) {
            IoFunction.printf("Virtual Backup of %s %s", getRaFcoVirtualBackup().getFcoToString(),
                    getRaFcoVirtualBackup().getState().toString());
            IoFunction.println();
            if (StringUtils.isNotEmpty(getRaFcoVirtualBackup().getReason())) {
                IoFunction.printf(" - Reason: %s", getRaFcoVirtualBackup().getReason());
                IoFunction.println();
            }
        }
    }

    @Override
    public VirtualBackupDiskInteractive newDiskInteractiveInstance(
            final CoreResultActionDiskVirtualBackup resultAction) {
        return new VirtualBackupDiskInteractive(resultAction, this);

    }

    @Override
    public void start() {
        super.start();
        if (getParent() != null) {
            IoFunction.println();
            IoFunction.println(VirtualBackupInteractive.LINE_SEPARATOR);
            IoFunction.printf("Virtual Backup of %s start.", getRaFcoVirtualBackup().getFcoToString());

        } else {
            IoFunction.printf("Virtual Backup of %s start.", getRaFcoVirtualBackup().getFcoToString());
        }

        IoFunction.println();

    }

    @Override
    public void startGenerationComputation() {
        super.startGenerationComputation();
        switch (getRaFcoVirtualBackup().getEntityType()) {
        case ImprovedVirtualDisk:
        case VirtualMachine:
            IoFunction.println(MessagesTemplate.getLocationString(getRaFcoVirtualBackup()));
            break;
        case VirtualApp:
        case K8sNamespace:
        default:
            break;
        }
    }

}
