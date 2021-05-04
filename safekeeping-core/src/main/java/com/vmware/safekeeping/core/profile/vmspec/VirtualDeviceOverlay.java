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

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.exception.ProfileException;
import com.vmware.vim25.Description;
import com.vmware.vim25.ID;
import com.vmware.vim25.ParaVirtualSCSIController;
import com.vmware.vim25.SharesInfo;
import com.vmware.vim25.SharesLevel;
import com.vmware.vim25.StorageIOAllocationInfo;
import com.vmware.vim25.VirtualAHCIController;
import com.vmware.vim25.VirtualBusLogicController;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualController;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConnectInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualE1000E;
import com.vmware.vim25.VirtualEnsoniq1371;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardResourceAllocation;
import com.vmware.vim25.VirtualFloppy;
import com.vmware.vim25.VirtualHdAudioCard;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualLsiLogicSASController;
import com.vmware.vim25.VirtualMachineVideoCard;
import com.vmware.vim25.VirtualNVDIMM;
import com.vmware.vim25.VirtualNVDIMMController;
import com.vmware.vim25.VirtualNVMEController;
import com.vmware.vim25.VirtualPCIPassthrough;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualParallelPort;
import com.vmware.vim25.VirtualPrecisionClock;
import com.vmware.vim25.VirtualSATAController;
import com.vmware.vim25.VirtualSCSIController;
import com.vmware.vim25.VirtualSCSIPassthrough;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VirtualSerialPort;
import com.vmware.vim25.VirtualSoundBlaster16;
import com.vmware.vim25.VirtualSoundCard;
import com.vmware.vim25.VirtualSriovEthernetCard;
import com.vmware.vim25.VirtualTPM;
import com.vmware.vim25.VirtualUSBController;
import com.vmware.vim25.VirtualUSBXHCIController;
import com.vmware.vim25.VirtualVmxnet;
import com.vmware.vim25.VirtualVmxnet2;
import com.vmware.vim25.VirtualVmxnet3;
import com.vmware.vim25.VirtualVmxnet3Vrdma;

public class VirtualDeviceOverlay extends AbstractVirtualDeviceProperties {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(VirtualDeviceOverlay.class.getName());

    public enum DeviceType {
        VIRTUAL_CDROM, VIRTUAL_FLOPPY, VIRTUAL_AHCI_CONTROLLER, VIRTUAL_SATA_CONTROLLER, VIRTUAL_NVDIMM_CONTROLLER,
        VIRTUAL_NVME_CONTROLLER, VIRTUAL_USB_CONTROLLER, VIRTUAL_USBXHCI_CONTROLLER, PARAVIRTUAL_SCSI_CONTROLLER,
        VIRTUAL_BUSLOGIC_CONTROLLER, VIRTUAL_LSILOGIC_CONTROLLER, VIRTUAL_LSILOGICSAS_CONTROLLER,
        VIRTUAL_MACHINE_VIDEOCARD, VIRTUAL_NVDIMM, VIRTUAL_PARALLEL_PORT, VIRTUAL_PCI_PASSTHROUGH,
        VIRTUAL_PRECISION_CLOCK, VIRTUAL_SCSI_PASSTHROUGH, VIRTUAL_SERIALPORT, VIRTUAL_TPM, VIRTUAL_SOUNDBLASTER16,
        VIRTUAL_HD_AUDIOCARD, VIRTUAL_ENSONIQ1371, VIRTUAL_DISK, VIRTUAL_E1000, VIRTUAL_E1000E, VIRTUAL_PCNET32,
        VIRTUAL_SRIOV_ETHERNET_CARD, VIRTUAL_VMXNET, VIRTUAL_VMXNET2, VIRTUAL_VMXNET3, VIRTUAL_VMXNET3_VRDMA, OTHER
    }

    private int key;

    private Description deviceInfo;

    private VirtualDeviceBackingInfoOverlay backing;

    private VirtualDeviceConnectInfo connectable;

    private VirtualDevicePciBusSlotInfoOverlay slotInfo;

    private Integer controllerKey;

    private Integer unitNumber;

    private DeviceType deviceType;

    public VirtualDeviceOverlay() {
    }

    public VirtualDeviceOverlay(final VirtualDevice vd) {
        this.key = vd.getKey();
        this.unitNumber = vd.getUnitNumber();
        this.controllerKey = vd.getControllerKey();
        this.deviceInfo = vd.getDeviceInfo();
        if (vd.getSlotInfo() != null) {
            this.slotInfo = new VirtualDevicePciBusSlotInfoOverlay(vd.getSlotInfo());
        }
        if (vd.getBacking() != null) {
            this.backing = new VirtualDeviceBackingInfoOverlay(vd.getBacking());
        }
        this.properties = new HashMap<>();
        this.connectable = vd.getConnectable();
        this.deviceType = DeviceType.OTHER;
        if (vd instanceof VirtualFloppy) {
            this.deviceType = DeviceType.VIRTUAL_FLOPPY;

        } else if (vd instanceof VirtualCdrom) {
            this.deviceType = DeviceType.VIRTUAL_CDROM;
        } else if (vd instanceof VirtualDisk) {
            this.deviceType = DeviceType.VIRTUAL_DISK;
            this.properties.put("capacityInBytes", ((VirtualDisk) vd).getCapacityInBytes());
            this.properties.put("iofilter", ((VirtualDisk) vd).getIofilter());

            this.properties.put("storageIOAllocation.limit", ((VirtualDisk) vd).getStorageIOAllocation().getLimit());

            this.properties.put("storageIOAllocation.reservation",
                    ((VirtualDisk) vd).getStorageIOAllocation().getReservation());

            this.properties.put("storageIOAllocation.shares.level",
                    ((VirtualDisk) vd).getStorageIOAllocation().getShares().getLevel());

            this.properties.put("storageIOAllocation.shares.share",
                    ((VirtualDisk) vd).getStorageIOAllocation().getShares().getShares());

            final ID id = ((VirtualDisk) vd).getVDiskId();

            this.properties.put("vDiskId", (id != null) ? id.getId() : null);

        } else if (vd instanceof VirtualEthernetCard) {
            this.properties.put("addressType", ((VirtualEthernetCard) vd).getAddressType());
            this.properties.put("externalId", ((VirtualEthernetCard) vd).getExternalId());
            this.properties.put("macAddress", ((VirtualEthernetCard) vd).getMacAddress());
            this.properties.put("resourceAllocation.limit",
                    ((VirtualEthernetCard) vd).getResourceAllocation().getLimit());
            this.properties.put("resourceAllocation.reservation",
                    ((VirtualEthernetCard) vd).getResourceAllocation().getReservation());
            this.properties.put("resourceAllocation.share.level",
                    ((VirtualEthernetCard) vd).getResourceAllocation().getShare().getLevel());
            this.properties.put("resourceAllocation.share.share",
                    ((VirtualEthernetCard) vd).getResourceAllocation().getShare().getShares());
            this.properties.put("uptCompatibilityEnabled", ((VirtualEthernetCard) vd).isUptCompatibilityEnabled());
            this.properties.put("wakeOnLanEnabled", ((VirtualEthernetCard) vd).isWakeOnLanEnabled());
            if (vd instanceof VirtualE1000) {
                this.deviceType = DeviceType.VIRTUAL_E1000;
            } else if (vd instanceof VirtualE1000E) {
                this.deviceType = DeviceType.VIRTUAL_E1000E;
            } else if (vd instanceof VirtualPCNet32) {
                this.deviceType = DeviceType.VIRTUAL_PCNET32;
            } else if (vd instanceof VirtualSriovEthernetCard) {
                this.deviceType = DeviceType.VIRTUAL_SRIOV_ETHERNET_CARD;
                this.properties.put("allowGuestOSMtuChange", ((VirtualSriovEthernetCard) vd).isAllowGuestOSMtuChange());
            } else if (vd instanceof VirtualVmxnet) {
                this.deviceType = DeviceType.VIRTUAL_VMXNET;
                if (vd instanceof VirtualVmxnet2) {
                    this.deviceType = DeviceType.VIRTUAL_VMXNET2;
                } else if (vd instanceof VirtualVmxnet3) {
                    if (vd instanceof VirtualVmxnet3Vrdma) {
                        this.deviceType = DeviceType.VIRTUAL_VMXNET3_VRDMA;
                        this.properties.put("deviceProtocol", ((VirtualVmxnet3Vrdma) vd).getDeviceProtocol());
                    } else {
                        this.deviceType = DeviceType.VIRTUAL_VMXNET3;
                    }
                } else {
                    String st = String.format("Unsupported device %s - skipped", vd.getClass().getTypeName());
                    logger.warning(st);
                }
            } else {
                String st = String.format("Unsupported device %s - skipped", vd.getClass().getTypeName());
                logger.warning(st);
            }
        } else

        /***********************
         * Controller excluded VirtualPCIController, VirtualPS2Controller,
         * VirtualSIOController ,VirtualIDEController,
         * VirtualMachineVMCIDevice,VirtualMachineVMIROM,VirtualPointingDevice,VirtualUSB,VirtualWDT
         ********/
        if (vd instanceof VirtualController) {
            this.properties.put("busNumber", ((VirtualController) vd).getBusNumber());
            this.properties.put("device", ((VirtualController) vd).getDevice());
            if (vd instanceof VirtualUSBController) {
                this.deviceType = DeviceType.VIRTUAL_USB_CONTROLLER;

                this.properties.put("autoConnectDevices", ((VirtualUSBController) vd).isEhciEnabled());
                this.properties.put("ehciEnabled", ((VirtualUSBController) vd).isAutoConnectDevices());

            } else if (vd instanceof VirtualUSBXHCIController) {
                this.deviceType = DeviceType.VIRTUAL_USBXHCI_CONTROLLER;
                this.properties.put("autoConnectDevices", ((VirtualUSBXHCIController) vd).isAutoConnectDevices());

            } else if (vd instanceof VirtualSCSIController) {
                this.properties.put("hotAddRemove", ((VirtualSCSIController) vd).isHotAddRemove());
                this.properties.put("scsiCtlrUnitNumber", ((VirtualSCSIController) vd).getScsiCtlrUnitNumber());
                this.properties.put("sharedBus", ((VirtualSCSIController) vd).getSharedBus());
                if (vd instanceof ParaVirtualSCSIController) {
                    this.deviceType = DeviceType.PARAVIRTUAL_SCSI_CONTROLLER;
                } else if (vd instanceof VirtualBusLogicController) {
                    this.deviceType = DeviceType.VIRTUAL_BUSLOGIC_CONTROLLER;
                } else if (vd instanceof VirtualLsiLogicController) {
                    this.deviceType = DeviceType.VIRTUAL_LSILOGIC_CONTROLLER;
                } else if (vd instanceof VirtualLsiLogicSASController) {
                    this.deviceType = DeviceType.VIRTUAL_LSILOGICSAS_CONTROLLER;
                } else {
                    String st = String.format("Unsupported device %s - skipped", vd.getClass().getTypeName());
                    logger.warning(st);
                }
            } else if (vd instanceof VirtualSATAController) {
                if (vd instanceof VirtualAHCIController) {
                    this.deviceType = DeviceType.VIRTUAL_AHCI_CONTROLLER;
                } else {
                    this.deviceType = DeviceType.VIRTUAL_SATA_CONTROLLER;
                }
            } else if (vd instanceof VirtualNVDIMMController) {
                this.deviceType = DeviceType.VIRTUAL_NVDIMM_CONTROLLER;
            } else if (vd instanceof VirtualNVMEController) {
                this.deviceType = DeviceType.VIRTUAL_NVME_CONTROLLER;
            } else {
                String st = String.format("Unsupported device %s - skipped", vd.getClass().getTypeName());
                logger.warning(st);
            }
        } else if (vd instanceof VirtualMachineVideoCard) {
            this.deviceType = DeviceType.VIRTUAL_MACHINE_VIDEOCARD;
            this.properties.put("videoRamSizeInKB", ((VirtualMachineVideoCard) vd).getVideoRamSizeInKB());
            this.properties.put("numDisplays", ((VirtualMachineVideoCard) vd).getNumDisplays());
            this.properties.put("useAutoDetect", ((VirtualMachineVideoCard) vd).isUseAutoDetect());
            this.properties.put("enable3DSupport", ((VirtualMachineVideoCard) vd).isEnable3DSupport());
            this.properties.put("use3DRenderer", ((VirtualMachineVideoCard) vd).getUse3DRenderer());
            this.properties.put("graphicsMemorySizeInKB", ((VirtualMachineVideoCard) vd).getGraphicsMemorySizeInKB());

        } else if (vd instanceof VirtualNVDIMM) {

            this.deviceType = DeviceType.VIRTUAL_NVDIMM;
            this.properties.put("capacityInMB", ((VirtualNVDIMM) vd).getCapacityInMB());

        } else if (vd instanceof VirtualParallelPort) {
            this.deviceType = DeviceType.VIRTUAL_PARALLEL_PORT;
        } else if (vd instanceof VirtualPCIPassthrough) {

            this.deviceType = DeviceType.VIRTUAL_PCI_PASSTHROUGH;
        } else if (vd instanceof VirtualPrecisionClock) {

            this.deviceType = DeviceType.VIRTUAL_PRECISION_CLOCK;
        } else if (vd instanceof VirtualSCSIPassthrough) {
            this.deviceType = DeviceType.VIRTUAL_SCSI_PASSTHROUGH;
        } else if (vd instanceof VirtualSerialPort) {
            this.deviceType = DeviceType.VIRTUAL_SERIALPORT;

            this.properties.put("yieldOnPoll", ((VirtualSerialPort) vd).isYieldOnPoll());

        } else if (vd instanceof VirtualTPM) {
            this.deviceType = DeviceType.VIRTUAL_TPM;
            this.properties.put("endorsementKeyCertificate", ((VirtualTPM) vd).getEndorsementKeyCertificate());
            this.properties.put("endorsementKeyCertificateSigningRequest",
                    ((VirtualTPM) vd).getEndorsementKeyCertificateSigningRequest());

        } else if (vd instanceof VirtualSoundCard) {
            if (vd instanceof VirtualSoundBlaster16) {
                this.deviceType = DeviceType.VIRTUAL_SATA_CONTROLLER;
            } else if (vd instanceof VirtualHdAudioCard) {
                this.deviceType = DeviceType.VIRTUAL_HD_AUDIOCARD;
            } else if (vd instanceof VirtualEnsoniq1371) {
                this.deviceType = DeviceType.VIRTUAL_ENSONIQ1371;
            } else {
                String st = String.format("Unsupported device %s - skipped", vd.getClass().getTypeName());
                logger.warning(st);
            }
        } else {
            this.deviceType = DeviceType.OTHER;
        }

    }

    /**
     * Clone constructor
     *
     * @param device
     */
    public VirtualDeviceOverlay(final VirtualDeviceOverlay src) {
        this.key = src.key;
        this.deviceType = src.deviceType;
        this.unitNumber = src.unitNumber;
        this.controllerKey = src.controllerKey;
        this.backing = src.backing;
        this.slotInfo = src.slotInfo;
        // vim25
        this.deviceInfo = src.deviceInfo;
        this.connectable = src.connectable;
    }

    @SuppressWarnings("unchecked")
    private VirtualController addVirtualControllerProperties(final VirtualController vd) throws ProfileException {
        addVirtualDeviceProperties(vd);
        vd.setBusNumber((int) this.properties.get("busNumber"));
        vd.getDevice().addAll((Collection<? extends Integer>) this.properties.get("device"));
        return vd;
    }

    private VirtualDevice addVirtualDeviceProperties(final VirtualDevice vd) throws ProfileException {
        vd.setKey(this.key);
        vd.setUnitNumber(this.unitNumber);
        vd.setControllerKey(this.controllerKey);
        vd.setDeviceInfo(this.deviceInfo);
        vd.setConnectable(this.connectable);
        if (this.slotInfo != null) {
            vd.setSlotInfo(this.slotInfo.toVirtualDeviceBusSlotInfo());
        }
        if (this.backing != null) {
            vd.setBacking(this.backing.toVirtualDeviceBackingInfo());
        }
        return vd;
    }

    private VirtualEthernetCard addVirtualEthernetCard(final VirtualEthernetCard virtualEthernetCard)
            throws ProfileException {
        addVirtualDeviceProperties(virtualEthernetCard);
        virtualEthernetCard.setAddressType(getPropertyAsString("addressType"));
        virtualEthernetCard.setExternalId(getPropertyAsString("externalId"));
        virtualEthernetCard.setMacAddress(getPropertyAsString("macAddress"));
        final VirtualEthernetCardResourceAllocation resource = new VirtualEthernetCardResourceAllocation();
        resource.setLimit(getPropertyAsLong("resourceAllocation.limit"));
        resource.setReservation(getPropertyAsLong("resourceAllocation.reservation"));
        final SharesInfo shareInfo = new SharesInfo();

        shareInfo.setShares(getPropertyAsInteger("resourceAllocation.share.share"));
        switch (getPropertyAsString("resourceAllocation.share.level")) {
        case "HIGH":
            shareInfo.setLevel(SharesLevel.HIGH);
            break;
        case "LOW":
            shareInfo.setLevel(SharesLevel.LOW);
            break;
        case "CUSTOM":
            shareInfo.setLevel(SharesLevel.CUSTOM);
            break;
        case "NORMAL":
        default:
            shareInfo.setLevel(SharesLevel.NORMAL);
            break;
        }

        resource.setShare(shareInfo);

        virtualEthernetCard.setResourceAllocation(resource);
        virtualEthernetCard.setUptCompatibilityEnabled(getPropertyAsBoolean("uptCompatibilityEnabled"));

        virtualEthernetCard.setWakeOnLanEnabled(getPropertyAsBoolean("wakeOnLanEnabled"));

        return virtualEthernetCard;
    }

    private VirtualSCSIController addVirtualSCSIControllerProperties(final VirtualSCSIController vd)
            throws ProfileException {
        addVirtualControllerProperties(vd);
        vd.setHotAddRemove(getPropertyAsBoolean("hotAddRemove"));
        vd.setScsiCtlrUnitNumber(getPropertyAsInteger("scsiCtlrUnitNumber"));
        switch (getPropertyAsString("sharedBus")) {
        case "PHYSICAL_SHARING":
            vd.setSharedBus(VirtualSCSISharing.PHYSICAL_SHARING);
            break;
        case "VIRTUAL_SHARING":
            vd.setSharedBus(VirtualSCSISharing.VIRTUAL_SHARING);
            break;
        case "NO_SHARING":
        default:
            vd.setSharedBus(VirtualSCSISharing.NO_SHARING);
            break;

        }
        return vd;
    }

    public VirtualDeviceBackingInfoOverlay getBacking() {
        return this.backing;
    }

    public VirtualDeviceConnectInfo getConnectable() {
        return this.connectable;
    }

    public Integer getControllerKey() {
        return this.controllerKey;
    }

    public Description getDeviceInfo() {
        return this.deviceInfo;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public int getKey() {
        return this.key;
    }

    public VirtualDevicePciBusSlotInfoOverlay getSlotInfo() {
        return this.slotInfo;
    }

    public Integer getUnitNumber() {
        return this.unitNumber;
    }

    public void setBacking(final VirtualDeviceBackingInfoOverlay backing) {
        this.backing = backing;
    }

    public void setConnectable(final VirtualDeviceConnectInfo connectable) {
        this.connectable = connectable;
    }

    public void setControllerKey(final Integer controllerKey) {
        this.controllerKey = controllerKey;
    }

    public void setDeviceInfo(final Description deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setDeviceType(final DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public void setKey(final int key) {
        this.key = key;
    }

    public void setSlotInfo(final VirtualDevicePciBusSlotInfoOverlay slotInfo) {
        this.slotInfo = slotInfo;
    }

    public void setUnitNumber(final Integer unitNumber) {
        this.unitNumber = unitNumber;
    }

    @SuppressWarnings("unchecked")
    public VirtualDevice toVirtualDevice() throws ProfileException {
        switch (this.deviceType) {
        case VIRTUAL_DISK:
            final VirtualDisk virtualDisk = (VirtualDisk) addVirtualDeviceProperties(new VirtualDisk());
            virtualDisk.setCapacityInBytes(getPropertyAsLong("capacityInBytes"));
            final String vId = getPropertyAsString("vDiskId");
            if (StringUtils.isNotBlank(vId)) {
                final ID id = new ID();
                id.setId(vId);
                virtualDisk.setVDiskId(id);
            }
            virtualDisk.getIofilter().addAll((Collection<? extends String>) this.properties.get("iofilter"));

            final StorageIOAllocationInfo storageIOAllocationInfo = new StorageIOAllocationInfo();
            storageIOAllocationInfo.setLimit(getPropertyAsLong("storageIOAllocation.limit"));
            storageIOAllocationInfo.setReservation(getPropertyAsInteger("storageIOAllocation.reservation"));
            final SharesInfo shareInfo = new SharesInfo();
            switch (getPropertyAsString("storageIOAllocation.shares.level")) {
            case "HIGH":
                shareInfo.setLevel(SharesLevel.HIGH);
                break;
            case "LOW":
                shareInfo.setLevel(SharesLevel.LOW);
                break;
            case "CUSTOM":
                shareInfo.setLevel(SharesLevel.CUSTOM);
                break;
            case "NORMAL":
            default:
                shareInfo.setLevel(SharesLevel.NORMAL);
                break;
            }
            shareInfo.setShares(getPropertyAsInteger("storageIOAllocation.shares.share"));
            storageIOAllocationInfo.setShares(shareInfo);
            virtualDisk.setStorageIOAllocation(storageIOAllocationInfo);
            return virtualDisk;
        case PARAVIRTUAL_SCSI_CONTROLLER:
            return addVirtualSCSIControllerProperties(new ParaVirtualSCSIController());
        case VIRTUAL_AHCI_CONTROLLER:
            return addVirtualDeviceProperties(new VirtualAHCIController());
        case VIRTUAL_BUSLOGIC_CONTROLLER:
            return addVirtualSCSIControllerProperties(new VirtualBusLogicController());
        case VIRTUAL_LSILOGICSAS_CONTROLLER:
            return addVirtualSCSIControllerProperties(new VirtualLsiLogicSASController());
        case VIRTUAL_LSILOGIC_CONTROLLER:
            return addVirtualSCSIControllerProperties(new VirtualLsiLogicController());
        case VIRTUAL_USBXHCI_CONTROLLER:
            final VirtualUSBXHCIController virtualUSBXHCIController = (VirtualUSBXHCIController) addVirtualControllerProperties(
                    new VirtualUSBXHCIController());

            virtualUSBXHCIController.setAutoConnectDevices(getPropertyAsBoolean("autoConnectDevices"));
            return virtualUSBXHCIController;

        case VIRTUAL_USB_CONTROLLER:
            final VirtualUSBController virtualUSBController = (VirtualUSBController) addVirtualControllerProperties(
                    new VirtualUSBController());
            virtualUSBController.setAutoConnectDevices(getPropertyAsBoolean("autoConnectDevices"));
            virtualUSBController.setEhciEnabled(getPropertyAsBoolean("ehciEnabled"));
            return virtualUSBController;
        case VIRTUAL_NVDIMM_CONTROLLER:
            return addVirtualControllerProperties(new VirtualNVDIMMController());
        case VIRTUAL_NVME_CONTROLLER:
            return addVirtualControllerProperties(new VirtualNVMEController());
        case VIRTUAL_SATA_CONTROLLER:
            return addVirtualControllerProperties(new VirtualSATAController());
        case VIRTUAL_CDROM:

            return addVirtualDeviceProperties(new VirtualCdrom());

        case VIRTUAL_FLOPPY:
            return addVirtualDeviceProperties(new VirtualFloppy());
        case VIRTUAL_HD_AUDIOCARD:
            return addVirtualDeviceProperties(new VirtualHdAudioCard());
        case VIRTUAL_SOUNDBLASTER16:
            return addVirtualDeviceProperties(new VirtualSoundBlaster16());
        case VIRTUAL_ENSONIQ1371:
            return addVirtualDeviceProperties(new VirtualEnsoniq1371());

        case VIRTUAL_MACHINE_VIDEOCARD:
            final VirtualMachineVideoCard virtualMachineVideoCard = (VirtualMachineVideoCard) addVirtualDeviceProperties(
                    new VirtualMachineVideoCard());
            virtualMachineVideoCard.setVideoRamSizeInKB(getPropertyAsLong("videoRamSizeInKB"));
            virtualMachineVideoCard.setNumDisplays(getPropertyAsInteger("numDisplays"));
            virtualMachineVideoCard.setUseAutoDetect(getPropertyAsBoolean("useAutoDetect"));
            virtualMachineVideoCard.setEnable3DSupport(getPropertyAsBoolean("enable3DSupport"));
            virtualMachineVideoCard.setUse3DRenderer(getPropertyAsString("use3DRenderer"));
            virtualMachineVideoCard.setGraphicsMemorySizeInKB(getPropertyAsLong("graphicsMemorySizeInKB"));

            return virtualMachineVideoCard;
        case VIRTUAL_NVDIMM:
            final VirtualNVDIMM virtualNVDIMM = (VirtualNVDIMM) addVirtualDeviceProperties(new VirtualNVDIMM());
            virtualNVDIMM.setCapacityInMB((long) this.properties.get("capacityInMB"));
            return virtualNVDIMM;
        case VIRTUAL_PARALLEL_PORT:
            return addVirtualDeviceProperties(new VirtualParallelPort());
        case VIRTUAL_PCI_PASSTHROUGH:
            return addVirtualDeviceProperties(new VirtualPCIPassthrough());
        case VIRTUAL_PRECISION_CLOCK:
            return addVirtualDeviceProperties(new VirtualPrecisionClock());

        case VIRTUAL_SCSI_PASSTHROUGH:
            return addVirtualDeviceProperties(new VirtualSCSIPassthrough());
        case VIRTUAL_SERIALPORT:
            final VirtualSerialPort virtualSerialPort = (VirtualSerialPort) addVirtualDeviceProperties(
                    new VirtualSerialPort());
            virtualSerialPort.setYieldOnPoll(getPropertyAsBoolean("yieldOnPoll"));
            return virtualSerialPort;

        case VIRTUAL_TPM:
            final VirtualTPM virtualTPM = (VirtualTPM) addVirtualDeviceProperties(new VirtualTPM());
            virtualTPM.getEndorsementKeyCertificateSigningRequest()
                    .addAll(getPropertyAsListBytes("endorsementKeyCertificateSigningRequest"));
            return virtualTPM;
        case VIRTUAL_E1000:
            return addVirtualEthernetCard(new VirtualE1000());
        case VIRTUAL_E1000E:
            return addVirtualEthernetCard(new VirtualE1000E());
        case VIRTUAL_PCNET32:
            return addVirtualEthernetCard(new VirtualPCNet32());
        case VIRTUAL_SRIOV_ETHERNET_CARD:
            final VirtualSriovEthernetCard virtualSriovEthernetCard = (VirtualSriovEthernetCard) addVirtualEthernetCard(
                    new VirtualSriovEthernetCard());
            virtualSriovEthernetCard.setAllowGuestOSMtuChange(getPropertyAsBoolean("allowGuestOSMtuChange"));
            return virtualSriovEthernetCard;
        case VIRTUAL_VMXNET:
            return addVirtualEthernetCard(new VirtualVmxnet());
        case VIRTUAL_VMXNET2:
            return addVirtualEthernetCard(new VirtualVmxnet2());
        case VIRTUAL_VMXNET3_VRDMA:
            final VirtualVmxnet3Vrdma virtualVmxnet3Vrdma = (VirtualVmxnet3Vrdma) addVirtualEthernetCard(
                    new VirtualVmxnet3Vrdma());
            virtualVmxnet3Vrdma.setDeviceProtocol(getPropertyAsString("deviceProtocol"));
            return virtualVmxnet3Vrdma;
        case VIRTUAL_VMXNET3:
            return addVirtualEthernetCard(new VirtualVmxnet3());

        default:
            throw new ProfileException("Unsupported Virtual Device Type %s", this.deviceType);
        }

    }

}
