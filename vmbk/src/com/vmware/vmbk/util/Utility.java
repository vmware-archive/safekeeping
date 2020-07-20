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
package com.vmware.vmbk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.vmware.vmbk.type.ManagedEntityInfo;

public class Utility {

    private static final Logger logger_ = Logger.getLogger(Utility.class.getName());

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static String checkTransportMode(final String transportMode) {
	boolean san = false;
	boolean nbd = false;
	boolean nbdssl = false;
	boolean hotadd = false;
	final String formatDuplicatedMessage = "\"%s\" - Position %d - Transport mode %s duplicated ";
	final String[] modes = transportMode.toLowerCase().split(":");
	int index = 1;
	for (final String mode : modes) {
	    switch (mode) {
	    case "san":
		if (san) {
		    return String.format(formatDuplicatedMessage, transportMode, index, "san");
		}
		san = true;
		break;
	    case "nbd":
		if (nbd) {
		    return String.format(formatDuplicatedMessage, transportMode, index, "nbd");
		}
		nbd = true;
		break;
	    case "nbdssl":
		if (nbdssl) {
		    return String.format(formatDuplicatedMessage, transportMode, index, "nbdssl");
		}
		nbdssl = true;
		break;
	    case "hotadd":
		if (hotadd) {
		    return String.format(formatDuplicatedMessage, transportMode, index, "hotadd");
		}
		hotadd = true;
		break;
	    default:
		return String.format("\"%s\" - Position %d - %s is not a valid transport mode", transportMode, index,
			mode);

	    }
	    ++index;
	}
	return null;

    }

    public static String composeEntityInfoName(final List<ManagedEntityInfo> entityInfoList) {
	final StringBuilder retString = new StringBuilder();
	if (entityInfoList.size() > 0) {
	    for (int index = 1; index < (entityInfoList.size() - 1); index++) {
		retString.append(entityInfoList.get(index).getName());
		retString.append('/');
	    }
	    retString.append(entityInfoList.get(entityInfoList.size() - 1).getName());
	}
	return retString.toString();
    }

// TODO Remove unused code found by UCDetector
//     public static List<String> dedup(final Iterable<String> list) {
// 	final TreeSet<String> set = new TreeSet<>();
//
// 	for (final String str : list) {
// 	    set.add(str);
// 	}
//
// 	return new LinkedList<>(set);
//     }

// TODO Remove unused code found by UCDetector
//     public static List<String> dedupKeepingOrder(final Iterable<String> list) {
// 	final LinkedList<String> ret = new LinkedList<>();
//
// 	for (final String str : list) {
// 	    if (ret.contains(str) == false) {
// 		ret.add(str);
// 	    }
// 	}
//
// 	return ret;
//     }

    public static boolean deleteDirectoryRecursive(final File pathFile) {
	if (pathFile.isDirectory() == false) {
	    return false;
	}

	final File[] files = pathFile.listFiles();
	assert files != null;
	boolean ret = false;

	for (final File file : files) {
	    if (file.isDirectory()) {
		ret = deleteDirectoryRecursive(file);
		if (ret == false) {
		    return false;
		}
	    } else {
		ret = file.delete();
		if (ret == false) {
		    logger_.info(String.format("deleted file %s %s.", file.getName(), (ret ? "succeeded" : "failed")));
		}
		if (ret == false) {
		    return false;
		}
	    }
	}
	ret = pathFile.delete();
	if (ret == false) {
	    logger_.info(String.format("deleted dir %s %s.", pathFile.getName(), (ret ? "succeeded" : "failed")));
	}
	return ret;
    }

// TODO Remove unused code found by UCDetector
//     public static String doubleQuote(final String st) {
// 	return String.format("\"%s\"", st);
//     }

    public static void execCmd(final String[] command) {
	try {
	    final Process process = Runtime.getRuntime().exec(command);
	    try (final BufferedReader sNumReader = new BufferedReader(
		    new InputStreamReader(process.getInputStream()))) {
		final BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		lineReader.lines().forEach(System.out::println);

		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		errorReader.lines().forEach(System.out::println);
	    }
	} catch (final Exception ex) {
	    System.err.println(ex.getMessage());
	}
    }

    public static byte[] getDigest(final InputStream is, final MessageDigest md, final int byteArraySize)
	    throws NoSuchAlgorithmException, IOException {
	final StringBuffer hexString = new StringBuffer();
	md.reset();
	final byte[] bytes = new byte[byteArraySize];
	int numBytes;
	while ((numBytes = is.read(bytes)) != -1) {
	    md.update(bytes, 0, numBytes);
	}
	final byte[] hash = md.digest();
	for (int i = 0; i < hash.length; i++) {
	    if ((0xff & hash[i]) < 0x10) {
		hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
	    } else {
		hexString.append(Integer.toHexString(0xFF & hash[i]));
	    }
	}
	return hexString.toString().getBytes();
    }

    static String getLinuxSystem_UUID() {

	final String command = "dmidecode -s system-uuid";
	String uuid = null;
	try {
	    final Process SerNumProcess = Runtime.getRuntime().exec(command);
	    try (final BufferedReader sNumReader = new BufferedReader(
		    new InputStreamReader(SerNumProcess.getInputStream()))) {
		uuid = sNumReader.readLine().trim();
		SerNumProcess.waitFor();
	    }
	} catch (final Exception ex) {
	    System.err.println("Linux UUID Exp : " + ex.getMessage());
	    return null;

	}

	return uuid.toLowerCase();
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

    static String getWindowsSystem_UUID() {

	final String command[] = new String[] { "wmic", "csproduct", "get", "UUID" };
	String uuid = null;
	Scanner sc = null;
	try {
	    final Process SerNumProcess = Runtime.getRuntime().exec(command);
	    SerNumProcess.getOutputStream().close();
	    sc = new Scanner(SerNumProcess.getInputStream());
	    sc.next();
	    uuid = sc.next().toLowerCase();
	} catch (final Exception ex) {
	    System.err.println("Windows UUID Exp : " + ex.getMessage());

	    uuid = null;
	} finally {
	    sc.close();
	}
	return uuid;
    }

    public static boolean isMac() {
	return (OS.indexOf("mac") >= 0);
    }

    private static boolean isSolaris() {
	return (OS.indexOf("sunos") >= 0);
    }

    public static boolean isUnix() {
	return ((OS.indexOf("nix") >= 0) || (OS.indexOf("nux") >= 0) || (OS.indexOf("aix") > 0));
    }

    public static boolean isWindows() {
	return (OS.indexOf("win") >= 0);
    }

    public static String printByteArray(final byte[] bytes) {
	final StringBuilder st = new StringBuilder();
	for (final byte i : bytes) {
	    st.append(String.format("%02X", i & 0xFF));
	}
	return st.toString();
    }

// TODO Remove unused code found by UCDetector
//     protected static void printListM(final Iterable<ManagedObjectReference> list) {
// 	if (list == null) {
// 	    IoFunction.println("The specified list is null.");
// 	    return;
// 	}
// 	for (final ManagedObjectReference m : list) {
// 	    IoFunction.println(m.getValue());
// 	}
//     }

// TODO Remove unused code found by UCDetector
//     public static Object readSerializedObject(final String fileName) {
// 	try {
//
// 	    final FileInputStream fos = new FileInputStream(fileName);
// 	    final ObjectInputStream oos = new ObjectInputStream(fos);
// 	    final Object obj = oos.readObject();
// 	    oos.close();
// 	    return obj;
// 	} catch (final Exception e) {
// 	    logger_.warning(Utility.toString(e));
// 	    return null;
// 	}
//     }

    public static String removeQuote(String st) {
	if ((st.length() >= 2) && (st.charAt(0) == '"') && (st.charAt(st.length() - 1) == '"')) {
	    st = st.substring(1, st.length() - 1);
	}
	return st;
    }

    static String toBigEndianUuid(final String uuid) {
	final String b1 = String.copyValueOf(new char[] { uuid.charAt(6), uuid.charAt(7), uuid.charAt(4),
		uuid.charAt(5), uuid.charAt(2), uuid.charAt(3), uuid.charAt(0), uuid.charAt(1), uuid.charAt(8) });

	final String b2 = String.copyValueOf(
		new char[] { uuid.charAt(11), uuid.charAt(12), uuid.charAt(9), uuid.charAt(10), uuid.charAt(13) });
	final String b3 = String
		.copyValueOf(new char[] { uuid.charAt(16), uuid.charAt(17), uuid.charAt(14), uuid.charAt(15) });

	return b1.toString().concat(b2.toString().concat(b3.toString().concat(uuid.substring(18))));
    }

    public static String toHexString(final byte[] bytes) {
	final StringBuilder sb = new StringBuilder(bytes.length * 3);
	for (final int b : bytes) {
	    sb.append(String.format("%02x ", b & 0xff));
	}
	return sb.toString();
    }

    public static String toString(final Exception e) {
	final StringWriter sw = new StringWriter();
	final PrintWriter pw = new PrintWriter(sw);
	e.printStackTrace(pw);
	pw.flush();
	return sw.toString();
    }

// TODO Remove unused code found by UCDetector
//     public static List<String> toStringList(final List<Integer> src) {
// 	final List<String> dst = new LinkedList<>();
//
// 	for (final Integer i : src) {
// 	    if (i != null) {
// 		dst.add(i.toString());
// 	    }
// 	}
// 	return dst;
//     }

// TODO Remove unused code found by UCDetector
//     public static boolean writeSerializedObject(final Object obj, final String fileName) {
// 	try {
// 	    final FileOutputStream fos = new FileOutputStream(fileName);
// 	    final ObjectOutputStream oos = new ObjectOutputStream(fos);
// 	    oos.writeObject(obj);
// 	    oos.close();
// 	    return true;
// 	} catch (final Exception e) {
// 	    logger_.warning(Utility.toString(e));
// 	    return false;
// 	}
//     }
}
