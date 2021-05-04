/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.safekeeping.core.type.enums;

public enum EntityType {
    None, ComputeResource, ClusterComputeResource, ContainerView, Datacenter, Datastore, VirtualizationManager,
    VirtualMachineSnapshot, VirtualMachine, HostSystem, Folder, VirtualApp, ResourcePool, ImprovedVirtualDisk,
    K8sNamespace, Network, OpaqueNetwork, DistributedVirtualPortgroup;

    public static EntityType toEntityType(final String type) {
        switch (type) {
        case "ivd":
            return EntityType.ImprovedVirtualDisk;
        case "vm":
            return EntityType.VirtualMachine;
        case "vapp":
            return EntityType.VirtualApp;
        case "k8s":
            return EntityType.K8sNamespace;
        default:
            return EntityType.valueOf(type);
        }
    }

    public String toString(final boolean toReport) {
        if (toReport) {
            switch (this) {
            case VirtualApp:
                return "vapp";
            case VirtualMachine:
                return "vm";
            case ImprovedVirtualDisk:
                return "ivd";
            case K8sNamespace:
                return "k8s";
            default:
                return toString();
            }
        } else {
            return toString();
        }
    }

}
