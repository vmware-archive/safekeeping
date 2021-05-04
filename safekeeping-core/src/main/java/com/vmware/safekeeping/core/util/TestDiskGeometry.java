/**
 *
 */
package com.vmware.safekeeping.core.util;

/**
 * Disk Geometry
 */
public class TestDiskGeometry {
	private int cylinders;
	private int heads;
	private int sectors;

	/**
	 * @return the cylinders
	 */
	public int getCylinders() {
		return this.cylinders;
	}

	/**
	 * @return the heads
	 */
	public int getHeads() {
		return this.heads;
	}

	/**
	 * @return the sectors
	 */
	public int getSectors() {
		return this.sectors;
	}

	/**
	 * @param cylinders the cylinders to set
	 */
	public void setCylinders(final int cylinders) {
		this.cylinders = cylinders;
	}

	/**
	 * @param heads the heads to set
	 */
	public void setHeads(final int heads) {
		this.heads = heads;
	}

	/**
	 * @param sectors the sectors to set
	 */
	public void setSectors(final int sectors) {
		this.sectors = sectors;
	}
}
