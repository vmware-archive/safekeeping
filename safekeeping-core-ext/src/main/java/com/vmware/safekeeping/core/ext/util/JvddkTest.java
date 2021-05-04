/**
 *
 */
package com.vmware.safekeeping.core.ext.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.jvix.JVixException;
import com.vmware.jvix.jDiskLib;
import com.vmware.jvix.jDiskLibConst;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.core.SJvddk;
import com.vmware.safekeeping.core.ext.command.results.CoreResultActionTest;
import com.vmware.safekeeping.core.util.TestDiskInfo;

/**
 * @author mdaneri
 *
 */
public class JvddkTest {
	private static final Logger logger = Logger.getLogger(JvddkTest.class.getName());
	private final jDiskLib dli;
	private final StringBuilder dumpDDBsResult;
	private final jDiskLib.Info info;

	final StringBuilder dumpDataContent;

	/**
	 *
	 */
	public JvddkTest() {
		this.info = new jDiskLib.Info();
		this.dli = SJvddk.getDli();
		this.dumpDDBsResult = new StringBuilder();
		this.dumpDataContent = new StringBuilder();
	}

	public void dumpData(final jDiskLib.DiskHandle diskHandle) throws JVixException {
		logger.info("Attempting disk read.");
		final byte[] buf = new byte[jDiskLibConst.SECTOR_SIZE];
		final long result = this.dli.read(diskHandle, 0, 1, buf);
		if (result != jDiskLibConst.VIX_OK) {
			throw new JVixException(this.dli.getErrorText(result, ""));
		}

		int i = 0;
		for (final byte b : buf) {
			if ((i++ % 16) == 0) {
				this.dumpDataContent.append('\n');
			}
			this.dumpDataContent.append(String.format("%02X ", b));
		}
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Data:");
			logger.info("\n" + this.dumpDataContent.toString() + "\n");
		}
	}

	public long dumpDDBs(final jDiskLib.DiskHandle diskHandle) {
		logger.info("Attempting GetMetadataKeys.");

		final String[] keys = this.dli.getMetadataKeys(diskHandle);

		long vixReturn = 0;
		for (final String key : keys) {
			final StringBuffer val = new StringBuffer();
			vixReturn = this.dli.readMetadata(diskHandle, key, val);
			if (vixReturn != 0) {
				final String msg = "Fail to get metadata with error " + vixReturn;
				logger.severe(msg);
				break;
			}
			final String msg = String.format("%-20s:      %s", key, val.toString());
			this.dumpDDBsResult.append(msg);
			if (logger.isLoggable(Level.INFO)) {
				logger.info(msg);
			}
		}
		return vixReturn;
	}

	public void dumpInfo(final jDiskLib.DiskHandle diskHandle) throws JVixException {
		logger.info("Attempting GetInfo.");

		final long result = this.dli.getInfo(diskHandle, this.info);
		if (result != jDiskLibConst.VIX_OK) {
			throw new JVixException(this.dli.getErrorText(result, ""));
		}
		if (logger.isLoggable(Level.INFO)) {
			String msg = String.format(" BiosGeo: cylinders: %d heads: %d sectors: %d", this.info.biosGeo.cylinders,
					this.info.biosGeo.heads, this.info.biosGeo.sectors);
			logger.info(msg);
			msg = String.format(" PhysGeo: cylinders: %d heads: %d sectors: %d", this.info.physGeo.cylinders,
					this.info.physGeo.heads, this.info.physGeo.sectors);
			logger.info(msg);
			logger.info("Capacity: " + this.info.capacityInSectors);
			logger.info("adapterType: " + this.info.adapterType);
			logger.info("numLinks: " + this.info.numLinks);
			logger.info("parentFileNameHint: " + this.info.parentFileNameHint);
		}
	}

	/**
	 * @return the dumpDDBsResult
	 */
	public StringBuilder getDumpDDBsResult() {
		return this.dumpDDBsResult;
	}

	/**
	 * @return the info
	 */
	public jDiskLib.Info getInfo() {
		return this.info;
	}

	public long testVmxAccess(final jDiskLib.ConnectParams conn, final String snapshotRef, final String transportMode,
			final String diskPath, final CoreResultActionTest resultAction) throws JVixException {
		conn.setSpecType(jDiskLib.ConnectParams.VIXDISKLIB_SPEC_VMX);
		final jDiskLib.Connection handle = new jDiskLib.Connection();
		long result = this.dli.prepareForAccess(conn, "jdisklibtest");
		if (result != jDiskLibConst.VIX_OK) {
			logger.warning("Attempting PrepareForAccess Failed:" + this.dli.getErrorText(result, ""));
		}
		try {
			logger.info("Attempting connect.");
			result = this.dli.connectEx(conn, true, snapshotRef, transportMode, handle);
			if (result != jDiskLibConst.VIX_OK) {
				throw new JVixException(this.dli.getErrorText(result, ""));
			}

			final jDiskLib.DiskHandle diskHandle = new jDiskLib.DiskHandle();

			logger.info("Attempting open.");
			result = this.dli.open(handle, Utility.removeQuote(diskPath), jDiskLibConst.OPEN_READ_ONLY, diskHandle);
			if (result != jDiskLibConst.VIX_OK) {
				logger.warning(this.dli.getErrorText(result, ""));
				throw new JVixException(this.dli.getErrorText(result, ""));
			}

			final String mode = this.dli.getTransportMode(diskHandle);
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Transport mode is: " + mode);
			}

			dumpDDBs(diskHandle);
			resultAction.setDumpDDBs(this.dumpDDBsResult.toString());
			dumpInfo(diskHandle);
			resultAction.setInfo(new TestDiskInfo(this.info));
			dumpData(diskHandle);
			resultAction.setDumpDataContent(this.dumpDataContent);

			logger.info("Attempting close.");
			result = this.dli.close(diskHandle);
			if (result != jDiskLibConst.VIX_OK) {
				throw new JVixException(this.dli.getErrorText(result, ""));
			}

			logger.info("Attempting remote disconnect.");
			result = this.dli.disconnect(handle);
			if (result != jDiskLibConst.VIX_OK) {
				throw new JVixException(this.dli.getErrorText(result, ""));
			}
		} finally {
			result = this.dli.endAccess(conn, "jdisklibtest");

		}
		if (result != jDiskLibConst.VIX_OK) {
			throw new JVixException(this.dli.getErrorText(result, ""));
		} else {
			logger.warning("EndAccess success");
		}
		return result;
	}

	public long testVmxAccess(final jDiskLib.ConnectParams conn, final String snapshotRef, final String transportMode,
			final String vmxSpec, final String diskPath, final CoreResultActionTest resultAction) throws JVixException {
		conn.setSpecType(jDiskLib.ConnectParams.VIXDISKLIB_SPEC_VMX);
		conn.setVmxSpec(vmxSpec);
		return testVmxAccess(conn, snapshotRef, transportMode, diskPath, resultAction);

	}

}
