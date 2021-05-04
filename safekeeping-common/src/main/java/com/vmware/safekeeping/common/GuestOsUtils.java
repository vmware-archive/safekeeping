package com.vmware.safekeeping.common;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

public class GuestOsUtils {

	public static String getAppData() throws URISyntaxException {
		String appData;
		if (GuestOsUtils.isWindows() || !GuestOsUtils.isAJar()) {
			appData = GuestOsUtils.getOsPropertyLocalAppData() + File.separatorChar + "safekeeping";
		} else {
			appData = GuestOsUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			appData = StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(appData, "/"), "/");
		}
		return appData;
	}

	public static String getArchDataModel() {
		return System.getProperty("sun.arch.data.model");
	}

	public static String getOS() {
		if (isWindows()) {
			return "win";
		} else if (isMac()) {
			return "osx";
		} else if (isUnix()) {
			return "uni";
		} else if (isSolaris()) {
			return "sol";
		} else {
			return "err";
		}
	}

	public static String getOsArch() {
		return System.getProperty("os.arch");
	}

	public static String getOsName() {
		return System.getProperty("os.name").toLowerCase();
	}

	public static String getOsPropertyLocalAppData() {
		if (isWindows()) {
			return System.getenv("LOCALAPPDATA");
		}
		if (isUnix()) {
			return "/opt";
		}
		return null;
	}

	public static String getTempDir() {
		if (isWindows()) {
			return System.getenv("TEMP") + File.separatorChar + "safekeeping";
		} else if (isUnix()) {
			return "/tmp/safekeeping";
		} else {
			return null;
		}
	}

	public static boolean is32bitJvm() {
		return getArchDataModel().compareTo("32") == 0;
	}

	public static boolean is64bitJvm() {
		return getArchDataModel().compareTo("64") == 0;
	}

	public static boolean isAJar() {
		final String protocol = GuestOsUtils.class.getResource("").getProtocol();
		return Objects.equals(protocol, "jar");
	}

	static boolean isMac() {
		return (getOsName().indexOf("mac") >= 0);
	}

	public static boolean isOS32bit() {
		return getOsArch().compareTo("i386") == 0;
	}

	public static boolean isOS64bit() {
		return (getOsArch().compareTo("amd64") == 0) || (getOsArch().compareTo("x86_64") == 0);
	}

	public static boolean isSolaris() {
		return (getOsName().indexOf("sunos") >= 0);
	}

	public static boolean isUnix() {
		return ((getOsName().indexOf("nix") >= 0) || (getOsName().indexOf("nux") >= 0)
				|| (getOsName().indexOf("aix") >= 0));
	}

	public static boolean isWindows() {
		return (getOsName().indexOf("win") >= 0);
	}

	private GuestOsUtils() {
		throw new IllegalStateException("Utility class");
	}

}
