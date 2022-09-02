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
package com.vmware.jvix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.GuestOsUtils;
import com.vmware.safekeeping.common.Utility;

public final class JDiskLibFactory {
	private static String libDir;
	private static final Logger logger = Logger.getLogger(JDiskLibFactory.class.getName());
	private static String vddkPluginsDirectory;
	private static VddkVersion vddkVersion;
	private static String appData;

	private static List<VddkVersion> vddkVersionList;

	private static boolean debug;

	private static final int MAJOR_VERSION_8 = 8;
	private static final int MAJOR_VERSION_7 = 7;
	private static final int MAJOR_VERSION_6 = 6;
	private static final int MINOR_VERSION_7 = 7;
	private static final int MINOR_VERSION_5 = 5;

	public static String getAppData() {
		return appData;
	}

	/**
	 * Create an instance of JDisk
	 *
	 * @param version
	 * @param overwrite
	 * @param deleteOnExit
	 * @return
	 * @throws JVixException
	 */
	public static JDisk getInstance(final VddkVersion version, final boolean overwrite, final boolean deleteOnExit)
			throws JVixException {
		try {

			if (version == null) {
				JDiskLibFactory.vddkVersion = getLastVddkVersion(getVddkVersionList());
			} else {
				JDiskLibFactory.vddkVersion = version;
			}
			JDiskLibFactory.libDir = null;

			JDiskLibFactory.loadFromJar(overwrite, deleteOnExit);
		} catch (final IOException | URISyntaxException e) {
			throw new JVixException(e);
		}
		NativeLibraryVersion nativeVersion;
		if (JDiskLibFactory.vddkVersion.getMajor() == MAJOR_VERSION_8) {
			nativeVersion = NativeLibraryVersion.VDDK80;}else 
		if (JDiskLibFactory.vddkVersion.getMajor() == MAJOR_VERSION_7) {
			nativeVersion = NativeLibraryVersion.VDDK70;
		} else if (JDiskLibFactory.vddkVersion.getMajor() == MAJOR_VERSION_6) {
			if (JDiskLibFactory.vddkVersion.getMinor() == MINOR_VERSION_7) {
				nativeVersion = NativeLibraryVersion.VDDK67;
			} else if (JDiskLibFactory.vddkVersion.getMinor() == MINOR_VERSION_5) {
				nativeVersion = NativeLibraryVersion.VDDK65;
			} else {
				throw new JVixException("Unsupported VDDK version");
			}
		} else {
			throw new JVixException("Unsupported VDDK version");
		}
		return new JDisk(nativeVersion);
	}

	/**
	 * Return the most recent version of VDDK
	 *
	 * @return
	 * @throws JVixException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static VddkVersion getLastVddkVersion(final Iterable<VddkVersion> vddkList) {
		int major = 0;
		int minor = 0;
		int patchLevel = 0;
		int build = 0;
		VddkVersion result = null;
		for (final VddkVersion vddk : vddkList) {
			if (major <= vddk.getMajor()) {
				if (major < vddk.getMajor()) {
					minor = 0;
					patchLevel = 0;
					build = 0;
				}
				major = vddk.getMajor();
				if (minor <= vddk.getMinor()) {
					if (minor < vddk.getMinor()) {
						patchLevel = 0;
						build = 0;
					}
					minor = vddk.getMinor();
					if (patchLevel <= vddk.getPatchLevel()) {
						if (patchLevel < vddk.getPatchLevel()) {
							build = 0;
						}
						patchLevel = vddk.getPatchLevel();
						if (build < vddk.getBuild()) {
							build = vddk.getBuild();
							result = vddk;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Get the Library Directory
	 *
	 * @return
	 */
	public static String getLibDir() {
		return JDiskLibFactory.libDir;
	}

	public static String getVddkPluginsDirectory() {
		return vddkPluginsDirectory;

	}

	/**
	 * @return the vddkVersion
	 */
	public static VddkVersion getVddkVersion() {
		return JDiskLibFactory.vddkVersion;
	}

	public static List<VddkVersion> getVddkVersionList() throws IOException, URISyntaxException, JVixException {
		if (vddkVersionList == null) {
			vddkVersionList = retrieveVddkVersionList();
		}
		return vddkVersionList;
	}

	public static boolean isDebug() {
		return debug;
	}

	/**
	 * When packaged into JAR extracts DLLs, places these into
	 *
	 * @param overwrite
	 * @param deleteOnExit
	 * @throws IOException
	 * @throws JVixException
	 * @throws URISyntaxException
	 */
	private static void loadFromJar(final boolean overwrite, final boolean deleteOnExit)
			throws IOException, JVixException, URISyntaxException {

		String libname = "";
		String libDiskPath = String.format("/%s/%d/%d/", ((GuestOsUtils.is64bitJvm()) ? "x64" : "i386"),
				JDiskLibFactory.vddkVersion.getMajor(), JDiskLibFactory.vddkVersion.getMinor());
		if (GuestOsUtils.isWindows()) {
			libDiskPath += "windows/";
			libname = "jDiskLib.dll";
		} else if (GuestOsUtils.isUnix()) {
			libDiskPath += "linux/";
			libname = "libjDiskLib.so";
		} else {
			throw new JVixException("Unsupported platform");
		}
		final String vddkpath = String.format("%s%d/%d.tar", libDiskPath, JDiskLibFactory.vddkVersion.getPatchLevel(),
				JDiskLibFactory.vddkVersion.getBuild());// patch,
		JDiskLibFactory.logger.log(Level.INFO, () -> "VDDK Path:".concat(vddkpath));

		JDiskLibFactory.appData = GuestOsUtils.getAppData();

		if (GuestOsUtils.isUnix()) {
			JDiskLibFactory.vddkPluginsDirectory = JDiskLibFactory.appData.concat("/lib");
		} else if (GuestOsUtils.isWindows()) {
			JDiskLibFactory.vddkPluginsDirectory = JDiskLibFactory.appData;
		} else {
			throw new JVixException("Unsupported GuestOs");
		}
		final String vddkDestDirectoryName = JDiskLibFactory.appData.concat(File.separator)
				.concat(((GuestOsUtils.isWindows()) ? "bin" : "lib/lib64"));
		JDiskLibFactory.libDir = vddkDestDirectoryName;
		JDiskLibFactory.logger.log(Level.INFO,
				() -> "VDDK Path:".concat(vddkpath).concat("    libDir:".concat(JDiskLibFactory.libDir)));
		final File vddkDestDirectory = new File(vddkDestDirectoryName);
		final Path vddkFileVersionPath = Paths.get(vddkDestDirectoryName.concat(File.separator).concat("version.txt"));
		JDiskLibFactory.logger.log(Level.INFO, () -> "VDDK Destination:".concat(vddkDestDirectory.toString())
				.concat("    VDDK VersionFile Info:".concat(vddkFileVersionPath.toString())));
		final boolean anotherInstance = !JDiskLibFactory.lockInstance(JDiskLibFactory.appData + "/.lock");
		if (anotherInstance) {
			throw new JVixException("Another Instance exist.");
		}
		if (vddkDestDirectory.exists()) {
			boolean upgrade = false;
			if (vddkFileVersionPath.toFile().exists()) {
				final String existingVddkFileVersion = new String(Files.readAllBytes(vddkFileVersionPath));
				upgrade = !existingVddkFileVersion.equals(JDiskLibFactory.vddkVersion.getExtendedVersion());
			} else {
				upgrade = true;
			}

			if (upgrade && anotherInstance) {
				throw new IOException("Cannot upgrade VDDK because another instance is running");
			}
			if ((overwrite || upgrade) && !anotherInstance) {
				JDiskLibFactory.logger.info("Overwrite or upgrade of VDDK is required");
				Utility.deleteDirectoryRecursive(vddkDestDirectory, false);
				vddkDestDirectory.mkdirs();
				try (final InputStream in = jDiskLib.class.getResourceAsStream(vddkpath)) {
					if (in != null) {
						com.vmware.safekeeping.common.IOUtils.unTarFile(in, vddkDestDirectory);
					} else {
						throw new JVixException(
								"Vddk version:" + getVddkVersion().getExtendedVersion() + " not available.");
					}
				}
				// write version information
				Files.write(vddkFileVersionPath, JDiskLibFactory.vddkVersion.getExtendedVersion().getBytes());
			}
		} else {
			JDiskLibFactory.logger.info("VDDK directory doesn't exist");
			vddkDestDirectory.mkdirs();
			try (final InputStream in = jDiskLib.class.getResourceAsStream(vddkpath)) {
				if (in != null) {
					com.vmware.safekeeping.common.IOUtils.unTarFile(in, vddkDestDirectory);
				} else {
					throw new JVixException(
							"Vddk version:" + getVddkVersion().getExtendedVersion() + " not available.");
				}

			}
			// write version information
			Files.write(vddkFileVersionPath, JDiskLibFactory.vddkVersion.getExtendedVersion().getBytes());
		}

		if (deleteOnExit && !anotherInstance) {
			vddkDestDirectory.deleteOnExit();
		}
		final Path fileOut = FileSystems.getDefault().getPath(vddkDestDirectory.getAbsolutePath(), libname);
		System.load(fileOut.toString());
	}

	private static boolean lockInstance(final String lockFile) {
		try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							Files.delete(file.toPath());
						} catch (final IOException e) {
							logger.log(Level.SEVERE, "Unable to remove lock file: ".concat(lockFile), e);
						}
					}
				});
				return true;
			} else {
				randomAccessFile.close();
			}
		} catch (final IOException e) {
			logger.log(Level.SEVERE, "Unable to create and/or lock file: ".concat(lockFile), e);
		}
		return false;
	}

	private static List<VddkVersion> retrieveVddkVersionList() throws IOException, URISyntaxException, JVixException {

		if (logger.isLoggable(Level.INFO)) {
			logger.info("List of available VDDK libraries");
		}

		String filter;
		if (GuestOsUtils.isWindows()) {
			filter = "windows";
		} else if (GuestOsUtils.isUnix()) {
			filter = "linux";
		} else {
			throw new JVixException("Unsupported OS");
		}

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			cl = JDiskLibFactory.class.getClassLoader();
		}
		if (cl == null) {
			throw new JVixException("Error to access Jar Resources");
		}
		final String resourceEntry = "x64";
		final URL res = cl.getResource(resourceEntry);
		if (res == null) {
			throw new JVixException("Resource folder " + resourceEntry + " doesn't exist");
		}
		final URI uri = res.toURI();
		Path myPath;
		FileSystem fileSystem = null;
		final List<VddkVersion> result = new ArrayList<>();
		try {
			if ("jar".equals(uri.getScheme())) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
				myPath = fileSystem.getPath(resourceEntry);
			} else {
				myPath = Paths.get(uri);
			}
			try (final Stream<Path> walk = Files.walk(myPath, 5)) {
				for (final Iterator<Path> it = walk.iterator(); it.hasNext();) {
					final String entry = it.next().toString();
					if (entry.endsWith(".tar") && entry.contains(filter)) {
						String startSubString;
						char separatorChar;
						if (entry.contains("/x64/")) {
							// support for Java 8 JAR
							startSubString = "/x64/";
							separatorChar = '/';
						} else if (entry.contains("x64/")) {
							// support for Java 11 JAR
							startSubString = "x64/";
							separatorChar = '/';
						} else if (entry.contains("\\x64\\")) {
							startSubString = "\\x64\\";
							separatorChar = '\\';
						} else {
							throw new JVixException("Unrecognizable path: " + entry);
						}

						result.add(new VddkVersion(
								StringUtils.split(StringUtils.substringBetween(entry, startSubString, ".tar")
										.replace(separatorChar + filter, ""), separatorChar)));
						if (logger.isLoggable(Level.INFO)) {
							logger.info(entry);
						}
					}
				}
			}
		} finally {
			if (fileSystem != null) {
				fileSystem.close();
			}
		}
		return result;
	}

	public static void setAppData(final String appData) {
		JDiskLibFactory.appData = appData;
	}

	public static void setDebug(final boolean debug) {
		JDiskLibFactory.debug = debug;
	}

	private JDiskLibFactory() {
		throw new IllegalStateException("Utility class");
	}

}
