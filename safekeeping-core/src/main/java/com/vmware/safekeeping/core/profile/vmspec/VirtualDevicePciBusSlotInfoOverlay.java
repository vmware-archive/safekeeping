package com.vmware.safekeeping.core.profile.vmspec;

import com.vmware.vim25.VirtualDeviceBusSlotInfo;
import com.vmware.vim25.VirtualDevicePciBusSlotInfo;
import com.vmware.vim25.VirtualUSBControllerPciBusSlotInfo;

public class VirtualDevicePciBusSlotInfoOverlay {
	public enum VirtualDevicePciBusSlotInfoType {
		VIRTUAL_USB_CONTROLLER_PCIBUS_SLOT_INFO, VIRTUAL_DEVICE_PCI_BUS_SLOT_INFO
	}

	private Integer pciSlotNumber;
	private Integer ehciPciSlotNumber;
	private VirtualDevicePciBusSlotInfoType busSlotType;

	public VirtualDevicePciBusSlotInfoOverlay() {

	}

	public VirtualDevicePciBusSlotInfoOverlay(final VirtualDeviceBusSlotInfo slotInfo) {

		if (slotInfo instanceof VirtualDevicePciBusSlotInfo) {
			this.pciSlotNumber = ((VirtualDevicePciBusSlotInfo) slotInfo).getPciSlotNumber();
			this.busSlotType = VirtualDevicePciBusSlotInfoType.VIRTUAL_DEVICE_PCI_BUS_SLOT_INFO;
		} else if (slotInfo instanceof VirtualUSBControllerPciBusSlotInfo) {
			this.pciSlotNumber = ((VirtualUSBControllerPciBusSlotInfo) slotInfo).getPciSlotNumber();
			this.ehciPciSlotNumber = ((VirtualUSBControllerPciBusSlotInfo) slotInfo).getEhciPciSlotNumber();
			this.busSlotType = VirtualDevicePciBusSlotInfoType.VIRTUAL_USB_CONTROLLER_PCIBUS_SLOT_INFO;
		}

	}

	public VirtualDevicePciBusSlotInfoOverlay(final VirtualDevicePciBusSlotInfo source) {
		this.pciSlotNumber = source.getPciSlotNumber();
		setEhciPciSlotNumber(null);
	}

	public VirtualDevicePciBusSlotInfoOverlay(final VirtualUSBControllerPciBusSlotInfo source) {
		this.pciSlotNumber = source.getPciSlotNumber();
		setEhciPciSlotNumber(source.getEhciPciSlotNumber());
	}

	public VirtualDevicePciBusSlotInfoType getBusSlotType() {
		return this.busSlotType;
	}

	public Integer getEhciPciSlotNumber() {
		return this.ehciPciSlotNumber;
	}

	public Integer getPciSlotNumber() {
		return this.pciSlotNumber;
	}

	public void setBusSlotType(final VirtualDevicePciBusSlotInfoType busSlotType) {
		this.busSlotType = busSlotType;
	}

	public void setEhciPciSlotNumber(final Integer ehciPciSlotNumber) {
		this.ehciPciSlotNumber = ehciPciSlotNumber;
	}

	public void setPciSlotNumber(final Integer pciSlotNumber) {
		this.pciSlotNumber = pciSlotNumber;
	}

	public VirtualDeviceBusSlotInfo toVirtualDeviceBusSlotInfo() {
		switch (this.busSlotType) {
		case VIRTUAL_DEVICE_PCI_BUS_SLOT_INFO:
			final VirtualDevicePciBusSlotInfo virtualDevicePciBusSlotInfo = new VirtualDevicePciBusSlotInfo();
			virtualDevicePciBusSlotInfo.setPciSlotNumber(this.pciSlotNumber);
			return virtualDevicePciBusSlotInfo;
		case VIRTUAL_USB_CONTROLLER_PCIBUS_SLOT_INFO:
			final VirtualUSBControllerPciBusSlotInfo virtualUSBControllerPciBusSlotInfo = new VirtualUSBControllerPciBusSlotInfo();
			virtualUSBControllerPciBusSlotInfo.setPciSlotNumber(this.pciSlotNumber);
			virtualUSBControllerPciBusSlotInfo.setEhciPciSlotNumber(this.ehciPciSlotNumber);
			return virtualUSBControllerPciBusSlotInfo;
		default:
			return new VirtualDeviceBusSlotInfo();

		}

	}
}
