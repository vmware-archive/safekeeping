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

import java.util.HashMap;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.safekeeping.core.soap.helpers.MorefUtil;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.vapi.internal.util.StringUtils;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.KeyProviderId;
import com.vmware.vim25.VirtualCdromAtapiBackingInfo;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualCdromPassthroughBackingInfo;
import com.vmware.vim25.VirtualCdromRemoteAtapiBackingInfo;
import com.vmware.vim25.VirtualCdromRemotePassthroughBackingInfo;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceFileBackingInfo;
import com.vmware.vim25.VirtualDevicePipeBackingInfo;
import com.vmware.vim25.VirtualDeviceRemoteDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceURIBackingInfo;
import com.vmware.vim25.VirtualDiskFlatVer1BackingInfo;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskLocalPMemBackingInfo;
import com.vmware.vim25.VirtualDiskRawDiskMappingVer1BackingInfo;
import com.vmware.vim25.VirtualDiskRawDiskVer2BackingInfo;
import com.vmware.vim25.VirtualDiskSeSparseBackingInfo;
import com.vmware.vim25.VirtualDiskSparseVer1BackingInfo;
import com.vmware.vim25.VirtualDiskSparseVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualEthernetCardLegacyNetworkBackingInfo;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualEthernetCardOpaqueNetworkBackingInfo;
import com.vmware.vim25.VirtualFloppyDeviceBackingInfo;
import com.vmware.vim25.VirtualFloppyImageBackingInfo;
import com.vmware.vim25.VirtualFloppyRemoteDeviceBackingInfo;
import com.vmware.vim25.VirtualNVDIMMBackingInfo;
import com.vmware.vim25.VirtualPCIPassthroughDeviceBackingInfo;
import com.vmware.vim25.VirtualPCIPassthroughPluginBackingInfo;
import com.vmware.vim25.VirtualParallelPortDeviceBackingInfo;
import com.vmware.vim25.VirtualParallelPortFileBackingInfo;
import com.vmware.vim25.VirtualPointingDeviceDeviceBackingInfo;
import com.vmware.vim25.VirtualPrecisionClockSystemClockBackingInfo;
import com.vmware.vim25.VirtualSCSIPassthroughDeviceBackingInfo;
import com.vmware.vim25.VirtualSerialPortDeviceBackingInfo;
import com.vmware.vim25.VirtualSerialPortFileBackingInfo;
import com.vmware.vim25.VirtualSerialPortPipeBackingInfo;
import com.vmware.vim25.VirtualSerialPortThinPrintBackingInfo;
import com.vmware.vim25.VirtualSerialPortURIBackingInfo;
import com.vmware.vim25.VirtualSoundCardDeviceBackingInfo;
import com.vmware.vim25.VirtualSriovEthernetCardSriovBackingInfo;
import com.vmware.vim25.VirtualUSBRemoteClientBackingInfo;
import com.vmware.vim25.VirtualUSBRemoteHostBackingInfo;
import com.vmware.vim25.VirtualUSBUSBBackingInfo;

public class VirtualDeviceBackingInfoOverlay extends AbstractVirtualDeviceProperties {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(VirtualDeviceBackingInfoOverlay.class.getName());

    public enum BackingInfoType {
        VIRTUAL_DEVICE_DEVICE_BACKINGINFO, VIRTUAL_DEVICE_FILE_BACKINGINFO, VIRTUAL_DEVICE_PIPE_BACKINGINFO,
        VIRTUAL_DEVICE_REMOTE_DEVICE_BACKINGINFO, VIRTUAL_DEVICE_URI_BACKINGINFO,
        VIRTUAL_ETHERNET_CARD_DISTRIBUTED_VIRTUAL_PORT_BACKINGINFO, VIRTUAL_ETHERNET_CARD_OPAQUE_NETWORK_BACKINGINFO,
        VIRTUAL_PCI_PASSTHROUGH_PLUGIN_BACKINGINFO, VIRTUAL_PRECISIONCLOCKSYSTEMCLOCK_BACKINGINFO,
        VIRTUAL_SERIALPORTTHINPRINT_BACKINGINFO, VIRTUAL_SRIOV_ETHERNET_CARD_SRIOV_BACKINGINFO,
        VIRTUAL_CDROM_ATAPI_BACKINGINFO, VIRTUAL_CDROM_PASSTHROUGH_BACKINGINFO, VIRTUAL_DISK_RAW_DISK_VER2_BACKINGINFO,
        VIRTUAL_ETHERNET_CARD_LEGACY_NETWORK_BACKINGINFO, VIRTUAL_ETHERNET_CARD_NETWORK_BACKINGINFO,
        VIRTUAL_FLOPPY_DEVICE_BACKINGINFO, VIRTUAL_PARALLEL_PORT_DEVICE_BACKINGINFO,
        VIRTUAL_PCIPASSTHROUGH_DEVICE_BACKINGINFO, VIRTUAL_PCI_PASSTHROUGH_DYNAMIC_BACKINGINFO,
        VIRTUAL_POINTING_DEVICE_DEVICE_BACKINGINFO, VIRTUAL_SCSI_PASSTHROUGH_DEVICE_BACKINGINFO,
        VIRTUAL_SERIALPORT_DEVICE_BACKINGINFO, VIRTUAL_SOUNDCARD_DEVICE_BACKINGINFO, VIRTUAL_USB_REMOTEHOST_BACKINGINFO,
        VIRTUAL_USB_USB_BACKINGINFO, VIRTUAL_SERIAL_PORT_URI_BACKINGINFO, VIRTUAL_CDROM_REMOTEA_TAPI_BACKINGINFO,
        VIRTUAL_CDROM_REMOTE_PASSTHROUGH_BACKINGINFO, VIRTUAL_FLOPPY_REMOTE_DEVICE_BACKINGINFO,
        VIRTUAL_USB_REMOTECLIENT_BACKINGINFO, VIRTUAL_SERIAL_PORT_PIPE_BACKINGINFO, VIRTUAL_CDROM_ISO_BACKINGINFO,
        VIRTUAL_DISK_FLAT_VER1_BACKINGINFO, VIRTUAL_DISK_FLAT_VER2_BACKINGINFO, VIRTUAL_DISK_LOCAL_PMEM_BACKINGINFO,
        VIRTUAL_DISK_RAWDISK_MAPPING_VER1_BACKINGINFO, VIRTUAL_DISK_SESPARSE_BACKINGINFO,
        VIRTUAL_DISK_SPARSE_VER1_BACKINGINFO, VIRTUAL_DISK_SPARSE_VER2_BACKINGINFO, VIRTUAL_FLOPPY_IMAGE_BACKINGINFO,
        VIRTUAL_NVDIMM_BACKINGINFO, VIRTUAL_PARALLEL_PORT_FILE_BACKINGINFO, VIRTUAL_SERIAL_PORT_FILE_BACKINGINFO
    }

    private BackingInfoType backingInfoType;

    public VirtualDeviceBackingInfoOverlay() {
    }

    public VirtualDeviceBackingInfoOverlay(final VirtualDeviceBackingInfo backing) {
        this.properties = new HashMap<>();
        if (backing instanceof VirtualDeviceDeviceBackingInfo) {

            this.properties.put("deviceName", ((VirtualDeviceDeviceBackingInfo) backing).getDeviceName());
            this.properties.put("useAutoDetect", ((VirtualDeviceDeviceBackingInfo) backing).isUseAutoDetect());
            if (backing instanceof VirtualCdromAtapiBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_CDROM_ATAPI_BACKINGINFO;

            } else if (backing instanceof VirtualCdromPassthroughBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_CDROM_PASSTHROUGH_BACKINGINFO;
                this.properties.put("exclusive", ((VirtualCdromPassthroughBackingInfo) backing).isExclusive());

            } else if (backing instanceof VirtualDiskRawDiskVer2BackingInfo) {

                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_RAW_DISK_VER2_BACKINGINFO;
                this.properties.put("changeId", ((VirtualDiskRawDiskVer2BackingInfo) backing).getChangeId());
                this.properties.put("descriptorFileName",
                        ((VirtualDiskRawDiskVer2BackingInfo) backing).getDescriptorFileName());
                this.properties.put("sharing", ((VirtualDiskRawDiskVer2BackingInfo) backing).getSharing());
                this.properties.put("uuid", ((VirtualDiskRawDiskVer2BackingInfo) backing).getUuid());

            } else if (backing instanceof VirtualEthernetCardLegacyNetworkBackingInfo) {

                this.backingInfoType = BackingInfoType.VIRTUAL_ETHERNET_CARD_LEGACY_NETWORK_BACKINGINFO;
            } else if (backing instanceof VirtualEthernetCardNetworkBackingInfo) {

                this.backingInfoType = BackingInfoType.VIRTUAL_ETHERNET_CARD_NETWORK_BACKINGINFO;
                this.properties.put("inPassthroughMode",
                        ((VirtualEthernetCardNetworkBackingInfo) backing).isInPassthroughMode());
                this.properties.put("network",
                        ((VirtualEthernetCardNetworkBackingInfo) backing).getNetwork().getValue());

            } else if (backing instanceof VirtualFloppyDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_FLOPPY_DEVICE_BACKINGINFO;
            } else if (backing instanceof VirtualParallelPortDeviceBackingInfo) {

                this.backingInfoType = BackingInfoType.VIRTUAL_PARALLEL_PORT_DEVICE_BACKINGINFO;
            } else if (backing instanceof VirtualPCIPassthroughDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_PCIPASSTHROUGH_DEVICE_BACKINGINFO;
                this.properties.put("deviceId", ((VirtualPCIPassthroughDeviceBackingInfo) backing).getDeviceId());
                this.properties.put("id", ((VirtualPCIPassthroughDeviceBackingInfo) backing).getId());
                this.properties.put("systemId", ((VirtualPCIPassthroughDeviceBackingInfo) backing).getDeviceName());
                this.properties.put("vendorId", ((VirtualPCIPassthroughDeviceBackingInfo) backing).getVendorId());
            } else if (backing instanceof VirtualPointingDeviceDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_POINTING_DEVICE_DEVICE_BACKINGINFO;
                this.properties.put("hostPointingDevice",
                        ((VirtualPointingDeviceDeviceBackingInfo) backing).getHostPointingDevice());

            } else if (backing instanceof VirtualSCSIPassthroughDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_SCSI_PASSTHROUGH_DEVICE_BACKINGINFO;

            } else if (backing instanceof VirtualSerialPortDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_SERIALPORT_DEVICE_BACKINGINFO;

            } else if (backing instanceof VirtualSoundCardDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_SOUNDCARD_DEVICE_BACKINGINFO;
            } else if (backing instanceof VirtualUSBRemoteHostBackingInfo) {

                this.backingInfoType = BackingInfoType.VIRTUAL_USB_REMOTEHOST_BACKINGINFO;
                this.properties.put("hostname", ((VirtualUSBRemoteHostBackingInfo) backing).getHostname());
            } else if (backing instanceof VirtualUSBUSBBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_USB_USB_BACKINGINFO;
            } else {
                this.backingInfoType = BackingInfoType.VIRTUAL_DEVICE_DEVICE_BACKINGINFO;
            }

        } else if (backing instanceof VirtualDeviceFileBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_DEVICE_FILE_BACKINGINFO;
            this.properties.put("backingObjectId", ((VirtualDeviceFileBackingInfo) backing).getBackingObjectId());
            this.properties.put("datastore",
                    ((((VirtualDeviceFileBackingInfo) backing).getDatastore() != null)
                            ? ((VirtualDeviceFileBackingInfo) backing).getDatastore().getValue()
                            : null));
            this.properties.put("fileName", ((VirtualDeviceFileBackingInfo) backing).getFileName());
            if (backing instanceof VirtualCdromIsoBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_CDROM_ISO_BACKINGINFO;
            } else if (backing instanceof VirtualDiskFlatVer1BackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_FLAT_VER1_BACKINGINFO;
            } else if (backing instanceof VirtualDiskFlatVer2BackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_FLAT_VER2_BACKINGINFO;
                this.properties.put("changeId", ((VirtualDiskFlatVer2BackingInfo) backing).getChangeId());
                this.properties.put("contentId", ((VirtualDiskFlatVer2BackingInfo) backing).getContentId());
                this.properties.put("deltaDiskFormat", ((VirtualDiskFlatVer2BackingInfo) backing).getDeltaDiskFormat());
                this.properties.put("deltaDiskFormatVariant",
                        ((VirtualDiskFlatVer2BackingInfo) backing).getDeltaDiskFormatVariant());
                this.properties.put("deltaGrainSize", ((VirtualDiskFlatVer2BackingInfo) backing).getDeltaGrainSize());
                this.properties.put("digestEnabled", ((VirtualDiskFlatVer2BackingInfo) backing).isDigestEnabled());
                this.properties.put("diskMode", ((VirtualDiskFlatVer2BackingInfo) backing).getDiskMode());
                this.properties.put("eagerlyScrub", ((VirtualDiskFlatVer2BackingInfo) backing).isEagerlyScrub());

                if (((VirtualDiskFlatVer2BackingInfo) backing).getKeyId() != null) {
                    this.properties.put("keyId.keyId",
                            ((VirtualDiskFlatVer2BackingInfo) backing).getKeyId().getKeyId());
                    this.properties.put("keyId.providerId.id",
                            ((VirtualDiskFlatVer2BackingInfo) backing).getKeyId().getProviderId().getId());
                }
                this.properties.put("split", ((VirtualDiskFlatVer2BackingInfo) backing).isSplit());
                this.properties.put("thinProvisioned", ((VirtualDiskFlatVer2BackingInfo) backing).isThinProvisioned());
                this.properties.put("uuid", ((VirtualDiskFlatVer2BackingInfo) backing).getUuid());
                this.properties.put("writeThrough", ((VirtualDiskFlatVer2BackingInfo) backing).isWriteThrough());
            } else if (backing instanceof VirtualDiskLocalPMemBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_LOCAL_PMEM_BACKINGINFO;
                this.properties.put("contentId", ((VirtualDiskLocalPMemBackingInfo) backing).getContentId());
                this.properties.put("diskMode", ((VirtualDiskLocalPMemBackingInfo) backing).getDiskMode());
                this.properties.put("uuid", ((VirtualDiskLocalPMemBackingInfo) backing).getUuid());
                this.properties.put("volumeUUID", ((VirtualDiskLocalPMemBackingInfo) backing).getVolumeUUID());

            } else if (backing instanceof VirtualDiskRawDiskMappingVer1BackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_RAWDISK_MAPPING_VER1_BACKINGINFO;
            } else if (backing instanceof VirtualDiskSeSparseBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_SESPARSE_BACKINGINFO;
                this.properties.put("changeId", ((VirtualDiskSeSparseBackingInfo) backing).getChangeId());
                this.properties.put("contentId", ((VirtualDiskSeSparseBackingInfo) backing).getContentId());
                this.properties.put("deltaDiskFormat", ((VirtualDiskSeSparseBackingInfo) backing).getDeltaDiskFormat());

                this.properties.put("digestEnabled", ((VirtualDiskSeSparseBackingInfo) backing).isDigestEnabled());
                this.properties.put("diskMode", ((VirtualDiskSeSparseBackingInfo) backing).getDiskMode());
                this.properties.put("grainSize", ((VirtualDiskSeSparseBackingInfo) backing).getGrainSize());
                if (((VirtualDiskSeSparseBackingInfo) backing).getKeyId() != null) {
                    this.properties.put("keyId.keyId",
                            ((VirtualDiskSeSparseBackingInfo) backing).getKeyId().getKeyId());
                    this.properties.put("keyId.providerId.id",
                            ((VirtualDiskSeSparseBackingInfo) backing).getKeyId().getProviderId().getId());
                }
                this.properties.put("uuid", ((VirtualDiskSeSparseBackingInfo) backing).getUuid());
                this.properties.put("writeThrough", ((VirtualDiskSeSparseBackingInfo) backing).isWriteThrough());

            } else if (backing instanceof VirtualDiskSparseVer1BackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_SPARSE_VER1_BACKINGINFO;
            } else if (backing instanceof VirtualDiskSparseVer2BackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_DISK_SPARSE_VER2_BACKINGINFO;
            } else if (backing instanceof VirtualFloppyImageBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_FLOPPY_IMAGE_BACKINGINFO;
            } else if (backing instanceof VirtualNVDIMMBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_NVDIMM_BACKINGINFO;
                this.properties.put("changeId", ((VirtualNVDIMMBackingInfo) backing).getChangeId());
            } else if (backing instanceof VirtualParallelPortFileBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_PARALLEL_PORT_FILE_BACKINGINFO;
            } else if (backing instanceof VirtualSerialPortFileBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_SERIAL_PORT_FILE_BACKINGINFO;
            } else {
                String st = String.format("Unsupported Backing Type %s - skipped", backing.getClass().getTypeName());
                logger.warning(st);
            }

        } else if (backing instanceof VirtualDevicePipeBackingInfo) {

            this.properties.put("pipeName", ((VirtualDevicePipeBackingInfo) backing).getPipeName());
            if (backing instanceof VirtualSerialPortPipeBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_SERIAL_PORT_PIPE_BACKINGINFO;
                this.properties.put("endpoint", ((VirtualSerialPortPipeBackingInfo) backing).getEndpoint());
                this.properties.put("noRxLoss", ((VirtualSerialPortPipeBackingInfo) backing).isNoRxLoss());

            } else {
                this.backingInfoType = BackingInfoType.VIRTUAL_DEVICE_PIPE_BACKINGINFO;
            }

        } else if (backing instanceof VirtualDeviceRemoteDeviceBackingInfo) {
            this.properties.put("deviceName", ((VirtualDeviceRemoteDeviceBackingInfo) backing).getDeviceName());
            this.properties.put("useAutoDetect", ((VirtualDeviceRemoteDeviceBackingInfo) backing).isUseAutoDetect());
            if (backing instanceof VirtualCdromRemoteAtapiBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_CDROM_REMOTEA_TAPI_BACKINGINFO;
            } else if (backing instanceof VirtualCdromRemotePassthroughBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_CDROM_REMOTE_PASSTHROUGH_BACKINGINFO;
                this.properties.put("exclusive", ((VirtualCdromRemotePassthroughBackingInfo) backing).isExclusive());

            } else if (backing instanceof VirtualFloppyRemoteDeviceBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_FLOPPY_REMOTE_DEVICE_BACKINGINFO;
            } else if (backing instanceof VirtualUSBRemoteClientBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_USB_REMOTECLIENT_BACKINGINFO;
                this.properties.put("hostname", ((VirtualUSBRemoteClientBackingInfo) backing).getHostname());
            } else {
                String st = String.format("Unsupported Backing Type %s - skipped", backing.getClass().getTypeName());
                logger.warning(st);
            }
        } else if (backing instanceof VirtualDeviceURIBackingInfo) {
            this.properties.put("direction", ((VirtualDeviceURIBackingInfo) backing).getDirection());
            this.properties.put("proxyURI", ((VirtualDeviceURIBackingInfo) backing).getProxyURI());
            this.properties.put("serviceURI", ((VirtualDeviceURIBackingInfo) backing).getServiceURI());

            if (backing instanceof VirtualSerialPortURIBackingInfo) {
                this.backingInfoType = BackingInfoType.VIRTUAL_SERIAL_PORT_URI_BACKINGINFO;
            } else {
                this.backingInfoType = BackingInfoType.VIRTUAL_DEVICE_URI_BACKINGINFO;
            }
        } else if (backing instanceof VirtualEthernetCardDistributedVirtualPortBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_ETHERNET_CARD_DISTRIBUTED_VIRTUAL_PORT_BACKINGINFO;
            this.properties.put("port.connectionCookie",
                    ((VirtualEthernetCardDistributedVirtualPortBackingInfo) backing).getPort().getConnectionCookie());
            this.properties.put("port.portgroupKey",
                    ((VirtualEthernetCardDistributedVirtualPortBackingInfo) backing).getPort().getPortgroupKey());
            this.properties.put("port.portKey",
                    ((VirtualEthernetCardDistributedVirtualPortBackingInfo) backing).getPort().getPortKey());
            this.properties.put("port.switchUuid",
                    ((VirtualEthernetCardDistributedVirtualPortBackingInfo) backing).getPort().getSwitchUuid());

        } else if (backing instanceof VirtualEthernetCardOpaqueNetworkBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_ETHERNET_CARD_OPAQUE_NETWORK_BACKINGINFO;
            this.properties.put("opaqueNetworkId",
                    ((VirtualEthernetCardOpaqueNetworkBackingInfo) backing).getOpaqueNetworkId());
            this.properties.put("opaqueNetworkType",
                    ((VirtualEthernetCardOpaqueNetworkBackingInfo) backing).getOpaqueNetworkType());
        } else if (backing instanceof VirtualPCIPassthroughPluginBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_PCI_PASSTHROUGH_PLUGIN_BACKINGINFO;
        } else if (backing instanceof VirtualPrecisionClockSystemClockBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_PRECISIONCLOCKSYSTEMCLOCK_BACKINGINFO;
            this.properties.put("protocol", ((VirtualPrecisionClockSystemClockBackingInfo) backing).getProtocol());
        } else if (backing instanceof VirtualSerialPortThinPrintBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_SERIALPORTTHINPRINT_BACKINGINFO;
        } else if (backing instanceof VirtualSriovEthernetCardSriovBackingInfo) {
            this.backingInfoType = BackingInfoType.VIRTUAL_SRIOV_ETHERNET_CARD_SRIOV_BACKINGINFO;
            this.properties.put("physicalFunctionBacking.deviceId",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getPhysicalFunctionBacking().getDeviceId());
            this.properties.put("physicalFunctionBacking.id",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getPhysicalFunctionBacking().getId());
            this.properties.put("physicalFunctionBacking.systemId",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getPhysicalFunctionBacking().getSystemId());
            this.properties.put("physicalFunctionBacking.vendorId",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getPhysicalFunctionBacking().getVendorId());

            this.properties.put("physicalFunctionBacking.deviceName",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getPhysicalFunctionBacking().getDeviceName());
            this.properties.put("physicalFunctionBacking.useAutodetect",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getPhysicalFunctionBacking()
                            .isUseAutoDetect());

            this.properties.put("virtualFunctionBacking.deviceId",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionBacking().getDeviceId());
            this.properties.put("virtualFunctionBacking.id",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionBacking().getId());
            this.properties.put("virtualFunctionBacking.systemId",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionBacking().getSystemId());
            this.properties.put("virtualFunctionBacking.vendorId",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionBacking().getVendorId());
            this.properties.put("virtualFunctionBacking.deviceName",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionBacking().getDeviceName());
            this.properties.put("virtualFunctionBacking.useAutodetect",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionBacking().isUseAutoDetect());
            this.properties.put("virtualFunctionIndex",
                    ((VirtualSriovEthernetCardSriovBackingInfo) backing).getVirtualFunctionIndex());

        } else {
            String st = String.format("Unsupported Backing Type %s - skipped", backing.getClass().getTypeName());
            logger.warning(st);
        }

    }

    public VirtualDeviceDeviceBackingInfo addVirtualDeviceDeviceBackingInfo(
            final VirtualDeviceDeviceBackingInfo virtualDeviceDeviceBackingInfo) {
        virtualDeviceDeviceBackingInfo.setDeviceName(getPropertyAsString("deviceName"));
        virtualDeviceDeviceBackingInfo.setUseAutoDetect(getPropertyAsBoolean("useAutoDetect"));
        return virtualDeviceDeviceBackingInfo;
    }

    public VirtualDeviceFileBackingInfo addVirtualDeviceFileBackingInfo(
            final VirtualDeviceFileBackingInfo virtualDeviceFileBackingInfo) {
        virtualDeviceFileBackingInfo.setBackingObjectId(getPropertyAsString("backingObjectId"));
        final String morefValue = getPropertyAsString("datastore");
        if (StringUtils.isNotBlank(morefValue)) {
            virtualDeviceFileBackingInfo
                    .setDatastore(MorefUtil.newManagedObjectReference(EntityType.Datastore, morefValue));
        }
        virtualDeviceFileBackingInfo.setFileName(getPropertyAsString("fileName"));
        return virtualDeviceFileBackingInfo;
    }

    public VirtualDeviceRemoteDeviceBackingInfo addVirtualDeviceRemoteDeviceBackingInfo(
            final VirtualDeviceRemoteDeviceBackingInfo virtualDeviceRemoteDeviceBackingInfo) {
        virtualDeviceRemoteDeviceBackingInfo.setDeviceName(getPropertyAsString("deviceName"));
        virtualDeviceRemoteDeviceBackingInfo.setUseAutoDetect(getPropertyAsBoolean("useAutoDetect"));
        return virtualDeviceRemoteDeviceBackingInfo;
    }

    public VirtualDeviceURIBackingInfo addVirtualDeviceURIBackingInfo(
            final VirtualDeviceURIBackingInfo virtualDeviceURIBackingInfo) {
        virtualDeviceURIBackingInfo.setDirection(getPropertyAsString("direction"));
        virtualDeviceURIBackingInfo.setProxyURI(getPropertyAsString("proxyURI"));
        virtualDeviceURIBackingInfo.setServiceURI(getPropertyAsString("serviceURI"));
        return virtualDeviceURIBackingInfo;
    }

    public BackingInfoType getBackingInfoType() {
        return this.backingInfoType;
    }

    VirtualDeviceConfigSpecFileOperation getFileOperation() {
        switch (this.backingInfoType) {
        case VIRTUAL_DISK_SESPARSE_BACKINGINFO:
        case VIRTUAL_DISK_SPARSE_VER1_BACKINGINFO:
        case VIRTUAL_DISK_SPARSE_VER2_BACKINGINFO:
        case VIRTUAL_DISK_FLAT_VER1_BACKINGINFO:
        case VIRTUAL_DISK_FLAT_VER2_BACKINGINFO:
            return VirtualDeviceConfigSpecFileOperation.CREATE;

        case VIRTUAL_ETHERNET_CARD_OPAQUE_NETWORK_BACKINGINFO:
        case VIRTUAL_ETHERNET_CARD_DISTRIBUTED_VIRTUAL_PORT_BACKINGINFO:
        case VIRTUAL_ETHERNET_CARD_NETWORK_BACKINGINFO:
        case VIRTUAL_CDROM_ATAPI_BACKINGINFO:
        case VIRTUAL_CDROM_ISO_BACKINGINFO:
        case VIRTUAL_CDROM_PASSTHROUGH_BACKINGINFO:
        case VIRTUAL_CDROM_REMOTEA_TAPI_BACKINGINFO:
        case VIRTUAL_CDROM_REMOTE_PASSTHROUGH_BACKINGINFO:
        case VIRTUAL_DEVICE_DEVICE_BACKINGINFO:
        case VIRTUAL_DEVICE_FILE_BACKINGINFO:
        case VIRTUAL_DEVICE_PIPE_BACKINGINFO:
        case VIRTUAL_DEVICE_REMOTE_DEVICE_BACKINGINFO:
        case VIRTUAL_DEVICE_URI_BACKINGINFO:
        case VIRTUAL_DISK_LOCAL_PMEM_BACKINGINFO:
        case VIRTUAL_DISK_RAWDISK_MAPPING_VER1_BACKINGINFO:
        case VIRTUAL_DISK_RAW_DISK_VER2_BACKINGINFO:

        case VIRTUAL_ETHERNET_CARD_LEGACY_NETWORK_BACKINGINFO:
        case VIRTUAL_FLOPPY_DEVICE_BACKINGINFO:
        case VIRTUAL_FLOPPY_IMAGE_BACKINGINFO:
        case VIRTUAL_FLOPPY_REMOTE_DEVICE_BACKINGINFO:
        case VIRTUAL_NVDIMM_BACKINGINFO:
        case VIRTUAL_PARALLEL_PORT_DEVICE_BACKINGINFO:
        case VIRTUAL_PARALLEL_PORT_FILE_BACKINGINFO:
        case VIRTUAL_PCIPASSTHROUGH_DEVICE_BACKINGINFO:
        case VIRTUAL_PCI_PASSTHROUGH_DYNAMIC_BACKINGINFO:
        case VIRTUAL_PCI_PASSTHROUGH_PLUGIN_BACKINGINFO:
        case VIRTUAL_POINTING_DEVICE_DEVICE_BACKINGINFO:
        case VIRTUAL_PRECISIONCLOCKSYSTEMCLOCK_BACKINGINFO:
        case VIRTUAL_SCSI_PASSTHROUGH_DEVICE_BACKINGINFO:
        case VIRTUAL_SERIALPORTTHINPRINT_BACKINGINFO:
        case VIRTUAL_SERIALPORT_DEVICE_BACKINGINFO:
        case VIRTUAL_SERIAL_PORT_FILE_BACKINGINFO:
        case VIRTUAL_SERIAL_PORT_PIPE_BACKINGINFO:
        case VIRTUAL_SERIAL_PORT_URI_BACKINGINFO:
        case VIRTUAL_SOUNDCARD_DEVICE_BACKINGINFO:
        case VIRTUAL_SRIOV_ETHERNET_CARD_SRIOV_BACKINGINFO:
        case VIRTUAL_USB_REMOTECLIENT_BACKINGINFO:
        case VIRTUAL_USB_REMOTEHOST_BACKINGINFO:
        case VIRTUAL_USB_USB_BACKINGINFO:
        default:
            return null;

        }
    }

    public void setBackingInfoType(final BackingInfoType backingInfoType) {
        this.backingInfoType = backingInfoType;
    }

    public VirtualDeviceBackingInfo toVirtualDeviceBackingInfo() throws ProfileException {
        switch (this.backingInfoType) {
        case VIRTUAL_DEVICE_DEVICE_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualDeviceDeviceBackingInfo());
        case VIRTUAL_CDROM_ATAPI_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualCdromAtapiBackingInfo());

        case VIRTUAL_CDROM_PASSTHROUGH_BACKINGINFO:
            final VirtualCdromPassthroughBackingInfo virtualCdromPassthroughBackingInfo = (VirtualCdromPassthroughBackingInfo) addVirtualDeviceDeviceBackingInfo(
                    new VirtualCdromPassthroughBackingInfo());
            virtualCdromPassthroughBackingInfo.setExclusive(getPropertyAsBoolean("exclusive"));
            return virtualCdromPassthroughBackingInfo;

        case VIRTUAL_DISK_RAW_DISK_VER2_BACKINGINFO:
            final VirtualDiskRawDiskVer2BackingInfo virtualDiskRawDiskVer2BackingInfo = (VirtualDiskRawDiskVer2BackingInfo) addVirtualDeviceDeviceBackingInfo(
                    new VirtualDiskRawDiskVer2BackingInfo());
            virtualDiskRawDiskVer2BackingInfo.setChangeId(getPropertyAsString("changeId"));
            virtualDiskRawDiskVer2BackingInfo.setDescriptorFileName(getPropertyAsString("descriptorFileName"));
            virtualDiskRawDiskVer2BackingInfo.setSharing(getPropertyAsString("sharing"));
            virtualDiskRawDiskVer2BackingInfo.setUuid(getPropertyAsString("uuid"));

            return virtualDiskRawDiskVer2BackingInfo;

        case VIRTUAL_ETHERNET_CARD_LEGACY_NETWORK_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualEthernetCardLegacyNetworkBackingInfo());

        case VIRTUAL_ETHERNET_CARD_NETWORK_BACKINGINFO:
            final VirtualEthernetCardNetworkBackingInfo virtualEthernetCardNetworkBackingInfo = (VirtualEthernetCardNetworkBackingInfo) addVirtualDeviceDeviceBackingInfo(
                    new VirtualEthernetCardNetworkBackingInfo());
            virtualEthernetCardNetworkBackingInfo.setInPassthroughMode(getPropertyAsBoolean("inPassthroughMode"));
            final String morefValue = getPropertyAsString("network");
            virtualEthernetCardNetworkBackingInfo
                    .setNetwork(MorefUtil.newManagedObjectReference(EntityType.Network, morefValue));
            return virtualEthernetCardNetworkBackingInfo;
        case VIRTUAL_FLOPPY_DEVICE_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualFloppyDeviceBackingInfo());
        case VIRTUAL_PARALLEL_PORT_DEVICE_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualParallelPortDeviceBackingInfo());
        case VIRTUAL_PCIPASSTHROUGH_DEVICE_BACKINGINFO:
            final VirtualPCIPassthroughDeviceBackingInfo virtualPCIPassthroughDeviceBackingInfo = (VirtualPCIPassthroughDeviceBackingInfo) addVirtualDeviceDeviceBackingInfo(
                    new VirtualPCIPassthroughDeviceBackingInfo());
            virtualPCIPassthroughDeviceBackingInfo.setDeviceId(getPropertyAsString("deviceId"));
            virtualPCIPassthroughDeviceBackingInfo.setId(getPropertyAsString("id"));
            virtualPCIPassthroughDeviceBackingInfo.setSystemId(getPropertyAsString("systemId"));
            virtualPCIPassthroughDeviceBackingInfo.setVendorId(getPropertyAsShort("vendorId"));
            return virtualPCIPassthroughDeviceBackingInfo;
        case VIRTUAL_POINTING_DEVICE_DEVICE_BACKINGINFO:
            final VirtualPointingDeviceDeviceBackingInfo virtualPointingDeviceDeviceBackingInfo = (VirtualPointingDeviceDeviceBackingInfo) addVirtualDeviceDeviceBackingInfo(
                    new VirtualPointingDeviceDeviceBackingInfo());
            virtualPointingDeviceDeviceBackingInfo.setHostPointingDevice(getPropertyAsString("hostPointingDevice"));
            return virtualPointingDeviceDeviceBackingInfo;
        case VIRTUAL_SCSI_PASSTHROUGH_DEVICE_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualSCSIPassthroughDeviceBackingInfo());
        case VIRTUAL_SERIALPORT_DEVICE_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualSerialPortDeviceBackingInfo());
        case VIRTUAL_SOUNDCARD_DEVICE_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualSoundCardDeviceBackingInfo());
        case VIRTUAL_USB_REMOTEHOST_BACKINGINFO:
            final VirtualUSBRemoteHostBackingInfo virtualUSBRemoteHostBackingInfo = (VirtualUSBRemoteHostBackingInfo) addVirtualDeviceDeviceBackingInfo(
                    new VirtualUSBRemoteHostBackingInfo());
            virtualUSBRemoteHostBackingInfo.setHostname(getPropertyAsString("hostname"));
            return virtualUSBRemoteHostBackingInfo;
        case VIRTUAL_USB_USB_BACKINGINFO:
            return addVirtualDeviceDeviceBackingInfo(new VirtualUSBUSBBackingInfo());
        // End of VIRTUAL_DEVICE_DEVICE_BACKINGINFO

        case VIRTUAL_DEVICE_FILE_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualDeviceFileBackingInfo());
        case VIRTUAL_CDROM_ISO_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualCdromIsoBackingInfo());
        case VIRTUAL_DISK_FLAT_VER1_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualDiskFlatVer1BackingInfo());

        case VIRTUAL_DISK_FLAT_VER2_BACKINGINFO:
            final VirtualDiskFlatVer2BackingInfo virtualDiskFlatVer2BackingInfo = (VirtualDiskFlatVer2BackingInfo) addVirtualDeviceFileBackingInfo(
                    new VirtualDiskFlatVer2BackingInfo());
            virtualDiskFlatVer2BackingInfo.setChangeId(getPropertyAsString("changeId"));
            virtualDiskFlatVer2BackingInfo.setContentId(getPropertyAsString("contentId"));
            virtualDiskFlatVer2BackingInfo.setDeltaDiskFormat(getPropertyAsString("deltaDiskFormat"));
            virtualDiskFlatVer2BackingInfo.setDeltaDiskFormatVariant(getPropertyAsString("deltaDiskFormatVariant"));
            virtualDiskFlatVer2BackingInfo.setDeltaGrainSize(getPropertyAsInteger("deltaGrainSize"));
            virtualDiskFlatVer2BackingInfo.setDigestEnabled(getPropertyAsBoolean("digestEnabled"));
            virtualDiskFlatVer2BackingInfo.setDiskMode(getPropertyAsString("diskMode"));
            virtualDiskFlatVer2BackingInfo.setEagerlyScrub(getPropertyAsBoolean("eagerlyScrub"));
            if (this.properties.containsKey("keyId.keyId")) {
                final CryptoKeyId keyId = new CryptoKeyId();
                keyId.setKeyId(getPropertyAsString("keyId.keyId"));
                keyId.setProviderId(new KeyProviderId());
                keyId.getProviderId().setId(getPropertyAsString("keyId.providerId.id"));
                virtualDiskFlatVer2BackingInfo.setKeyId(keyId);
            }
            virtualDiskFlatVer2BackingInfo.setSplit(getPropertyAsBoolean("split"));
            virtualDiskFlatVer2BackingInfo.setThinProvisioned(getPropertyAsBoolean("thinProvisioned"));
            virtualDiskFlatVer2BackingInfo.setUuid(getPropertyAsString("uuid"));
            virtualDiskFlatVer2BackingInfo.setWriteThrough(getPropertyAsBoolean("writeThrough"));
            return virtualDiskFlatVer2BackingInfo;
        case VIRTUAL_DISK_LOCAL_PMEM_BACKINGINFO:
            final VirtualDiskLocalPMemBackingInfo virtualDiskLocalPMemBackingInfo = (VirtualDiskLocalPMemBackingInfo) addVirtualDeviceFileBackingInfo(
                    new VirtualDiskLocalPMemBackingInfo());
            virtualDiskLocalPMemBackingInfo.setDiskMode(getPropertyAsString("diskMode"));
            virtualDiskLocalPMemBackingInfo.setContentId(getPropertyAsString("contentId"));
            virtualDiskLocalPMemBackingInfo.setVolumeUUID(getPropertyAsString("volumeUUID"));
            virtualDiskLocalPMemBackingInfo.setUuid(getPropertyAsString("uuid"));
            return virtualDiskLocalPMemBackingInfo;
        case VIRTUAL_DISK_RAWDISK_MAPPING_VER1_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualDiskRawDiskMappingVer1BackingInfo());
        case VIRTUAL_DISK_SESPARSE_BACKINGINFO:
            final VirtualDiskSeSparseBackingInfo virtualDiskSeSparseBackingInfo = (VirtualDiskSeSparseBackingInfo) addVirtualDeviceFileBackingInfo(
                    new VirtualDiskSeSparseBackingInfo());
            virtualDiskSeSparseBackingInfo.setChangeId(getPropertyAsString("changeId"));
            virtualDiskSeSparseBackingInfo.setContentId(getPropertyAsString("contentId"));
            virtualDiskSeSparseBackingInfo.setDeltaDiskFormat(getPropertyAsString("deltaDiskFormat"));

            virtualDiskSeSparseBackingInfo.setDigestEnabled(getPropertyAsBoolean("digestEnabled"));
            virtualDiskSeSparseBackingInfo.setDiskMode(getPropertyAsString("diskMode"));
            virtualDiskSeSparseBackingInfo.setGrainSize(getPropertyAsInteger("grainSize"));
            if (this.properties.containsKey("keyId.keyId")) {
                final CryptoKeyId keyId = new CryptoKeyId();
                keyId.setKeyId(getPropertyAsString("keyId.keyId"));
                keyId.setProviderId(new KeyProviderId());
                keyId.getProviderId().setId(getPropertyAsString("keyId.providerId.id"));
                virtualDiskSeSparseBackingInfo.setKeyId(keyId);
            }
            virtualDiskSeSparseBackingInfo.setUuid(getPropertyAsString("uuid"));
            virtualDiskSeSparseBackingInfo.setWriteThrough(getPropertyAsBoolean("writeThrough"));
            return virtualDiskSeSparseBackingInfo;
        case VIRTUAL_DISK_SPARSE_VER1_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualDiskSparseVer1BackingInfo());
        case VIRTUAL_DISK_SPARSE_VER2_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualDiskSparseVer2BackingInfo());
        case VIRTUAL_FLOPPY_IMAGE_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualFloppyImageBackingInfo());
        case VIRTUAL_NVDIMM_BACKINGINFO:
            final VirtualNVDIMMBackingInfo virtualNVDIMMBackingInfo = (VirtualNVDIMMBackingInfo) addVirtualDeviceFileBackingInfo(
                    new VirtualNVDIMMBackingInfo());
            virtualNVDIMMBackingInfo.setChangeId(getPropertyAsString("changeId"));
            return virtualNVDIMMBackingInfo;
        case VIRTUAL_PARALLEL_PORT_FILE_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualParallelPortFileBackingInfo());
        case VIRTUAL_SERIAL_PORT_FILE_BACKINGINFO:
            return addVirtualDeviceFileBackingInfo(new VirtualSerialPortFileBackingInfo());

        case VIRTUAL_DEVICE_PIPE_BACKINGINFO:
            final VirtualDevicePipeBackingInfo virtualDevicePipeBackingInfo = new VirtualDevicePipeBackingInfo();
            virtualDevicePipeBackingInfo.setPipeName(getPropertyAsString("pipeName"));
            return virtualDevicePipeBackingInfo;
        case VIRTUAL_DEVICE_REMOTE_DEVICE_BACKINGINFO:
            return addVirtualDeviceRemoteDeviceBackingInfo(new VirtualDeviceRemoteDeviceBackingInfo());

        case VIRTUAL_CDROM_REMOTEA_TAPI_BACKINGINFO:
            return addVirtualDeviceRemoteDeviceBackingInfo(new VirtualCdromRemoteAtapiBackingInfo());
        case VIRTUAL_CDROM_REMOTE_PASSTHROUGH_BACKINGINFO:
            final VirtualCdromRemotePassthroughBackingInfo virtualCdromRemotePassthroughBackingInfo = (VirtualCdromRemotePassthroughBackingInfo) addVirtualDeviceRemoteDeviceBackingInfo(
                    new VirtualCdromRemotePassthroughBackingInfo());
            virtualCdromRemotePassthroughBackingInfo.setExclusive(getPropertyAsBoolean("exclusive"));
            return virtualCdromRemotePassthroughBackingInfo;
        case VIRTUAL_FLOPPY_REMOTE_DEVICE_BACKINGINFO:
            return addVirtualDeviceRemoteDeviceBackingInfo(new VirtualFloppyRemoteDeviceBackingInfo());
        case VIRTUAL_USB_REMOTECLIENT_BACKINGINFO:
            final VirtualUSBRemoteClientBackingInfo virtualUSBRemoteClientBackingInfo = (VirtualUSBRemoteClientBackingInfo) addVirtualDeviceRemoteDeviceBackingInfo(
                    new VirtualUSBRemoteClientBackingInfo());
            virtualUSBRemoteClientBackingInfo.setHostname(getPropertyAsString("hostname"));
            return virtualUSBRemoteClientBackingInfo;
        case VIRTUAL_SERIAL_PORT_URI_BACKINGINFO:
            return addVirtualDeviceURIBackingInfo(new VirtualSerialPortURIBackingInfo());

        case VIRTUAL_DEVICE_URI_BACKINGINFO:
            return addVirtualDeviceURIBackingInfo(new VirtualDeviceURIBackingInfo());

        case VIRTUAL_ETHERNET_CARD_DISTRIBUTED_VIRTUAL_PORT_BACKINGINFO:
            final VirtualEthernetCardDistributedVirtualPortBackingInfo virtualEthernetCardDistributedVirtualPortBackingInfo = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
            final DistributedVirtualSwitchPortConnection port = new DistributedVirtualSwitchPortConnection();
            port.setConnectionCookie(getPropertyAsInteger("port.connectionCookie"));
            port.setPortgroupKey(getPropertyAsString("port.portgroupKey"));
            port.setPortKey(getPropertyAsString("port.portKey"));
            port.setSwitchUuid(getPropertyAsString("port.switchUuid"));
            virtualEthernetCardDistributedVirtualPortBackingInfo.setPort(port);
            return virtualEthernetCardDistributedVirtualPortBackingInfo;
        case VIRTUAL_ETHERNET_CARD_OPAQUE_NETWORK_BACKINGINFO:
            final VirtualEthernetCardOpaqueNetworkBackingInfo virtualEthernetCardOpaqueNetworkBackingInfo = new VirtualEthernetCardOpaqueNetworkBackingInfo();
            virtualEthernetCardOpaqueNetworkBackingInfo.setOpaqueNetworkId(getPropertyAsString("opaqueNetworkId"));
            virtualEthernetCardOpaqueNetworkBackingInfo.setOpaqueNetworkType(getPropertyAsString("opaqueNetworkType"));
            return virtualEthernetCardOpaqueNetworkBackingInfo;
        case VIRTUAL_PCI_PASSTHROUGH_PLUGIN_BACKINGINFO:
            return new VirtualPCIPassthroughPluginBackingInfo();
        case VIRTUAL_PRECISIONCLOCKSYSTEMCLOCK_BACKINGINFO:
            final VirtualPrecisionClockSystemClockBackingInfo virtualPrecisionClockSystemClockBackingInfo = new VirtualPrecisionClockSystemClockBackingInfo();
            virtualPrecisionClockSystemClockBackingInfo.setProtocol(getPropertyAsString("protocol"));
            return virtualPrecisionClockSystemClockBackingInfo;

        case VIRTUAL_SERIALPORTTHINPRINT_BACKINGINFO:
            return new VirtualSerialPortThinPrintBackingInfo();
        case VIRTUAL_SRIOV_ETHERNET_CARD_SRIOV_BACKINGINFO:
            final VirtualSriovEthernetCardSriovBackingInfo virtualSriovEthernetCardSriovBackingInfo = new VirtualSriovEthernetCardSriovBackingInfo();
            virtualSriovEthernetCardSriovBackingInfo.setPhysicalFunctionBacking(
                    getPropertyAsVirtualPCIPassthroughDeviceBackingInfo("physicalFunctionBacking"));
            virtualSriovEthernetCardSriovBackingInfo.setVirtualFunctionBacking(
                    getPropertyAsVirtualPCIPassthroughDeviceBackingInfo("virtualFunctionBacking"));
            virtualSriovEthernetCardSriovBackingInfo
                    .setVirtualFunctionIndex(getPropertyAsInteger("virtualFunctionIndex"));
            return virtualSriovEthernetCardSriovBackingInfo;

        default:
            throw new ProfileException("Unsupported Backing Info Type %s", this.backingInfoType);

        }
    }

}
