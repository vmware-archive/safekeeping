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
package com.vmware.safekeeping.core.profile.vmspec;

import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;

public class VirtualDeviceConfigSpecOverlay {

    private VirtualDeviceOverlay device;

    public VirtualDeviceConfigSpecOverlay() {
    }

    public VirtualDeviceConfigSpecOverlay(final VirtualDevice vd) throws ProfileException {
        try {
            this.device = new VirtualDeviceOverlay(vd);

        } catch (final Exception e) {
            throw new ProfileException(e);
        }
    }

    public VirtualDeviceConfigSpecOverlay(final VirtualDeviceConfigSpec deviceSpec) throws SafekeepingException {
        this(deviceSpec.getDevice());

    }

    public VirtualDeviceConfigSpecOverlay(final VirtualDeviceConfigSpecOverlay src) {
        this.device = new VirtualDeviceOverlay(src.device);
    }

    public VirtualDeviceOverlay getDevice() {
        return this.device;
    }

    public void setDevice(final VirtualDeviceOverlay device) {
        this.device = device;
    }

    public VirtualDeviceConfigSpec toVirtualDeviceConfigSpec() throws ProfileException {
        final VirtualDeviceConfigSpec deviceSpec = new VirtualDeviceConfigSpec();

        deviceSpec.setDevice(this.device.toVirtualDevice());
        deviceSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
        if (this.device.getBacking() != null) {
            deviceSpec.setFileOperation(this.device.getBacking().getFileOperation());
        }
        return deviceSpec;
    }

}
