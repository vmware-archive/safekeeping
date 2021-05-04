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
package com.vmware.safekeeping.core.command.options;

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.enums.WindowsQuiesceSpecVssBackupContext;
import com.vmware.safekeeping.core.type.enums.WindowsQuiesceSpecVssBackupType;
import com.vmware.vim25.VirtualMachineGuestQuiesceSpec;
import com.vmware.vim25.VirtualMachineWindowsQuiesceSpec;

public class VirtualMachineQuisceSpec {
    private Boolean vssPartialFileSupport;

    private Integer timeout;

    private Boolean vssBootableSystemState;

    private WindowsQuiesceSpecVssBackupContext vssBackupContext;

    private WindowsQuiesceSpecVssBackupType vssBackupType;

    private boolean useWindowsVss;

    private Boolean vssRetryOnFail;

    public VirtualMachineQuisceSpec() {
        this.vssPartialFileSupport = CoreGlobalSettings.getDefaultVssPartialFileSupport();
        this.vssBootableSystemState = CoreGlobalSettings.getDefaultVssBootableSystemState();
        this.vssBackupContext = CoreGlobalSettings.getDefaultVssBackupContext();
        this.vssBackupType = CoreGlobalSettings.getDefaultVssBackupType();
        this.useWindowsVss = CoreGlobalSettings.getDefaultUSeWindowsVss();
        this.timeout = this.useWindowsVss ? CoreGlobalSettings.getWindowsVssTimeOut()
                : CoreGlobalSettings.getQuisceTimeout();
        this.vssRetryOnFail = CoreGlobalSettings.getDefaultVssRetryOnFailure();
    }

    public VirtualMachineQuisceSpec(final VirtualMachineQuisceSpec src) {

        this.vssPartialFileSupport = src.vssPartialFileSupport == null
                ? CoreGlobalSettings.getDefaultVssPartialFileSupport()
                : src.vssPartialFileSupport;
        this.vssBootableSystemState = src.vssBootableSystemState == null
                ? CoreGlobalSettings.getDefaultVssBootableSystemState()
                : src.vssBootableSystemState;
        this.vssBackupContext = src.vssBackupContext == null ? CoreGlobalSettings.getDefaultVssBackupContext()
                : src.vssBackupContext;
        this.vssBackupType = src.vssBackupType == null ? CoreGlobalSettings.getDefaultVssBackupType()
                : src.vssBackupType;
        this.useWindowsVss = src.useWindowsVss;
        if (this.useWindowsVss) {
            this.timeout = (src.timeout == null) ? CoreGlobalSettings.getWindowsVssTimeOut() : src.timeout;
        } else {
            this.timeout = (src.timeout == null) ? CoreGlobalSettings.getQuisceTimeout() : src.timeout;
        }

        this.vssRetryOnFail = src.vssRetryOnFail == null ? CoreGlobalSettings.getDefaultVssRetryOnFailure()
                : src.vssRetryOnFail;
    }

    public Integer getTimeout() {
        return this.timeout;
    }

    public WindowsQuiesceSpecVssBackupContext getVssBackupContext() {
        return this.vssBackupContext;
    }

    public WindowsQuiesceSpecVssBackupType getVssBackupType() {
        return this.vssBackupType;
    }

    public Boolean getVssBootableSystemState() {
        return this.vssBootableSystemState;
    }

    public Boolean getVssPartialFileSupport() {
        return this.vssPartialFileSupport;
    }

    public boolean isUseWindowsVss() {
        return this.useWindowsVss;
    }

    public Boolean isVssRetryOnFail() {
        return this.vssRetryOnFail;
    }

    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    public void setUseWindowsVss(final boolean useWindowsVss) {
        this.useWindowsVss = useWindowsVss;
    }

    public void setVssBackupContext(final WindowsQuiesceSpecVssBackupContext vssBackupContext) {
        this.vssBackupContext = vssBackupContext;
    }

    public void setVssBackupType(final WindowsQuiesceSpecVssBackupType vssBackupType) {
        this.vssBackupType = vssBackupType;
    }

    public void setVssBootableSystemState(final Boolean vssBootableSystemState) {
        this.vssBootableSystemState = vssBootableSystemState;
    }

    public void setVssPartialFileSupport(final Boolean vssPartialFileSupport) {
        this.vssPartialFileSupport = vssPartialFileSupport;
    }

    public void setVssRetryOnFail(final Boolean vssRetryOnFail) {
        this.vssRetryOnFail = vssRetryOnFail;
    }

//    public VirtualMachineGuestQuiesceSpec toVirtualMachineGuestQuiesceSpec(final boolean windowsGuest) {
//
//        if (windowsGuest) {
//            final VirtualMachineWindowsQuiesceSpec guestWinQuiesceSpec = new VirtualMachineWindowsQuiesceSpec();
//
//            guestWinQuiesceSpec.setVssPartialFileSupport(this.vssPartialFileSupport);// https://docs.microsoft.com/en-us/windows/win32/vss/working-with-partial-files
//            guestWinQuiesceSpec.setTimeout(this.timeout);
//            guestWinQuiesceSpec.setVssBootableSystemState(this.vssBootableSystemState); // https://docs.microsoft.com/en-us/windows/win32/vss/vss-backup-state
//            guestWinQuiesceSpec.setVssBackupContext(this.vssBackupContext.toString());// "ctx_auto");VssBackupContext
//
//            guestWinQuiesceSpec.setVssBackupType(this.vssBackupType.toInteger());
//            return guestWinQuiesceSpec;
//
//        } else {
//            final VirtualMachineGuestQuiesceSpec guestQuiesceSpec = new VirtualMachineGuestQuiesceSpec();
//            guestQuiesceSpec.setTimeout(this.timeout);
//            return guestQuiesceSpec;
//        }
//    }
    public VirtualMachineGuestQuiesceSpec toVirtualMachineGuestQuiesceSpec() {
        final VirtualMachineGuestQuiesceSpec guestQuiesceSpec = new VirtualMachineGuestQuiesceSpec();
        guestQuiesceSpec.setTimeout(this.timeout);
        return guestQuiesceSpec;
    }

    public VirtualMachineWindowsQuiesceSpec toVirtualMachineWindowsQuiesceSpec() {
        final VirtualMachineWindowsQuiesceSpec guestWinQuiesceSpec = new VirtualMachineWindowsQuiesceSpec();

        guestWinQuiesceSpec.setVssPartialFileSupport(this.vssPartialFileSupport);// https://docs.microsoft.com/en-us/windows/win32/vss/working-with-partial-files
        guestWinQuiesceSpec.setTimeout(this.timeout);
        guestWinQuiesceSpec.setVssBootableSystemState(this.vssBootableSystemState); // https://docs.microsoft.com/en-us/windows/win32/vss/vss-backup-state
        guestWinQuiesceSpec.setVssBackupContext(this.vssBackupContext.toString());// "ctx_auto");VssBackupContext

        guestWinQuiesceSpec.setVssBackupType(this.vssBackupType.toInteger());
        return guestWinQuiesceSpec;

    }

}
