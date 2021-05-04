package com.vmware.guest.appmonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.GuestOsUtils;

public final class VmGuestAppMonitor {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(VmGuestAppMonitor.class.getName());
	private static final int BUFFER_SIZE = 4096;

	private VmGuestAppMonitor() {
		// nothing here
	}

	public static VmGuestAppMonitor getInstance() throws VmGuestAppMonitorException {
		loadFromJar();
		return new VmGuestAppMonitor();
	}

	/**
	 * When packaged into JAR extracts DLLs, places these into
	 * 
	 * @throws VmGuestAppMonitorException
	 */
	private static void loadFromJar() throws VmGuestAppMonitorException {
		try {
			String libVmGuestAppMonitorNative = "";
			String libVmwareAppmonitor = "";
			if (GuestOsUtils.isWindows()) {
				libVmGuestAppMonitorNative = "VmGuestAppMonitorNative.dll";
				libVmwareAppmonitor = "vmware-appmonitor.dll";
			} else if (GuestOsUtils.isUnix()) {
				libVmGuestAppMonitorNative = "VmGuestAppMonitorNative.so";
				libVmwareAppmonitor = "vmware-appmonitor.so";
			} else {
				throw new VmGuestAppMonitorException("Unsupported platform");
			}

			String appData = GuestOsUtils.getAppData();

			final String destDirectoryName = appData.concat(File.separator)
					.concat(((GuestOsUtils.isWindows()) ? "bin" : "lib/lib64"));

			File fileOutLibVmGuestAppMonitorNative = new File(destDirectoryName, libVmGuestAppMonitorNative);
			if (!fileOutLibVmGuestAppMonitorNative.exists()) {
				writeResource(fileOutLibVmGuestAppMonitorNative);
			}
			File fileOutLibVmwareAppmonitor = new File(destDirectoryName, libVmwareAppmonitor);
			if (!fileOutLibVmwareAppmonitor.exists()) {
				writeResource(fileOutLibVmwareAppmonitor);
			}

			System.load(fileOutLibVmGuestAppMonitorNative.toString());
		} catch (IOException | URISyntaxException e) {
			throw new VmGuestAppMonitorException(e);
		}

	}

	private static void writeResource(File fileOut) throws IOException {
		try (final InputStream in = VmGuestAppMonitor.class.getResourceAsStream("/" + fileOut.getName())) {
			// always write to different location

			try (final OutputStream out = new FileOutputStream(fileOut)) {

				final byte[] buf = new byte[BUFFER_SIZE];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
		}
	}

	public enum AppStatus {
		GREEN, GRAY, RED
	}

	/**
	 * Return the current status recorded for the application.
	 * 
	 * @return the application status.
	 */
	public AppStatus getAppStatus() {
		switch (NativeAppMonitor.getAppStatus()) {
		case "green":
			return AppStatus.GREEN;
		case "gray":
			return AppStatus.GRAY;
		case "red":
			return AppStatus.RED;
		default:
			return AppStatus.GRAY;
		}
	}

	/**
	 * Post application state. The guest's application agent has requested an
	 * immediate reset. The guest can request this at any time.
	 * 
	 * @return true if successful
	 */
	public boolean postNeedReset() {
		int result = NativeAppMonitor.postAppState("needReset");
		return checkResult(result);
	}

	/**
	 * Post application state. The guest's application agent declared state to be
	 * normal and no action is required.
	 * 
	 * @return true if successful
	 */
	public boolean postOk() {
		int result = NativeAppMonitor.postAppState("OK");
		return checkResult(result);
	}

	public static final int VMGUESTAPPMONITORLIB_ERROR_SUCCESS = 0;
	/** No error. */
	public static final int VMGUESTAPPMONITORLIB_ERROR_OTHER = 1;
	/** Other error */
	public static final int VMGUESTAPPMONITORLIB_ERROR_NOT_RUNNING_IN_VM = 2;
	/** Not running in a VM */
	public static final int VMGUESTAPPMONITORLIB_ERROR_NOT_ENABLE = 3;
	/** Monitoring is not enabled */
	public static final int VMGUESTAPPMONITORLIB_ERROR_NOT_SUPPORTED = 4;

	/** < Monitoring is not supported */

	/**
	 * Enable application monitoring. After this call, the agent must call
	 * markActive() at least once every 30 seconds or the application will be viewed
	 * as having failed.
	 *
	 * @return true if monitoring has been enabled.
	 */
	public boolean enable() {
		int result = NativeAppMonitor.enable();
		return checkResult(result);
	}

	/**
	 * Marks the application as active. This function needs to be called at least
	 * once every 30 seconds while application monitoring is enabled or HA will
	 * determine that the application has failed.
	 * 
	 * @return true if successful
	 */
	public boolean markActive() {
		int result = NativeAppMonitor.markActive();
		return checkResult(result);

	}

	private boolean checkResult(int result) {
		switch (result) {

		case VMGUESTAPPMONITORLIB_ERROR_SUCCESS:
			return true;
		case VMGUESTAPPMONITORLIB_ERROR_NOT_ENABLE:
			logger.warning("Monitoring is not enabled");
			return false;
		case VMGUESTAPPMONITORLIB_ERROR_NOT_SUPPORTED:
			logger.warning("Monitoring is not supported");
			return false;
		case VMGUESTAPPMONITORLIB_ERROR_NOT_RUNNING_IN_VM:
			logger.warning("Not running in a VM");
			return false;
		case VMGUESTAPPMONITORLIB_ERROR_OTHER:
		default:
			logger.warning("Other error");
			return false;
		}
	}

	/**
	 * Disable application monitoring.
	 * 
	 * @return TRUE if monitoring has been disabled.
	 */
	public boolean disable() {
		int result = NativeAppMonitor.disable();
		return checkResult(result);
	}

	/**
	 * Return the current state of application monitoring.
	 * 
	 * @return true if monitoring is enabled.
	 */
	public boolean isEnabled() {
		int result = NativeAppMonitor.isEnabled();
		return result == 1;
	}
}
