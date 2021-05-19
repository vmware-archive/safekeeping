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
package com.vmware.safekeeping.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public final class Utility {
    public static class ExecResult {
        private String output;

        private String error;
        private boolean success;

        /**
         *
         */
        public ExecResult() {
            this.output = "";
            this.error = "";
            this.success = false;
        }

        public String getError() {
            return this.error;
        }

        public String getOutput() {
            return this.output;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public void setError(final String error) {
            this.error = error;
        }

        public void setOutput(final String output) {
            this.output = output;
        }

        public void setSuccess(final boolean success) {
            this.success = success;
        }
    }

    private static final Logger logger = Logger.getLogger(Utility.class.getName());

    public static final long ONE_SECOND_IN_MILLIS = 1000L;

    public static final long FIVE_SECONDS_IN_MILLIS = 5L * ONE_SECOND_IN_MILLIS;
    public static final long TEN_SECONDS_IN_MILLIS = 10L * ONE_SECOND_IN_MILLIS;

    public static final long THIRTY_SECONDS_IN_MILLIS = 30L * ONE_SECOND_IN_MILLIS;

    public static final long ONE_MINUTE_IN_MILLIS = 60L * ONE_SECOND_IN_MILLIS;
    public static final long EIGHT_MINUTES_IN_MILLIS = 8L * ONE_MINUTE_IN_MILLIS;
    public static final long TEN_MINUTES_IN_MILLIS = 10L * ONE_MINUTE_IN_MILLIS;
    public static final long THIRTY_MINUTE_IN_MILLIS = 30L * ONE_MINUTE_IN_MILLIS;
    public static final int HTTPS_PORT = 443;

    public static final int HTTP_PORT = 80;
    public static final long ONE_HUNDRED_MS = 100;

    public static final int FIVE_SECONDS = 5;

    public static final int TEN_SECONDS = 10;
    public static final int ONE_SECOND = 1;
    public static final int ONE_KBYTE = 1024;

    public static final int FOUR_KBYTES = 4 * ONE_KBYTE;
    public static final int ONE_MBYTES = ONE_KBYTE * ONE_KBYTE;
    public static final int ONE_GBYTES = ONE_MBYTES * ONE_KBYTE;
    public static final long HALF_SECOND_IN_MILLIS = ONE_SECOND_IN_MILLIS / 2;
    /**
     * Used for indicate a percentage of 100%
     */
    public static final float ONE_HUNDRED_PER_CENT = 100F;
    /**
     * Used for indicate a percentage almost at 100%
     */
    public static final float ALMOST_ONE_HUNDRED_PER_CENT = 99.9F;
    /**
     * Used for indicate a percentage of 10%
     */
    public static final float TEN_PER_CENT = 10.0F;

    /**
     * Used for indicate a percentage of 50%
     */
    public static final float FIFTY_PER_CENT = 50.0F;

    /**
     * A Locale object represents a specific geographical, political,or cultural
     * region. An operation that requires a Locale to perform its task is called
     * locale-sensitive and uses the Locale to tailor information for the user. For
     * example, displaying a number is a locale-sensitive operation the number
     * should be formatted according to the customs and conventions of the user's
     * native country,region, or culture.
     */
    public static final Locale LOCALE = Locale.ENGLISH;
    public static final Integer ONE_HUNDRED_PER_CENT_AS_INT = 100;

    public static final long FIVE_HUNDRED_MILLIS = 500;

    public static final Integer ONE_MINUTE_IN_SECONDS = 60;

    public static final float ONE_PER_CENT = 1.0F;

    public static String checkTransportMode(final String transportMode) {
        boolean san = false;
        boolean nbd = false;
        boolean nbdssl = false;
        boolean hotadd = false;
        final String formatDuplicatedMessage = "\"%s\" - Position %d - Transport mode %s duplicated ";
        final String[] modes = transportMode.toLowerCase(Locale.ENGLISH).split(":");
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

    public static void copyFromResource(final Class<? extends Object> obj, final File confDirectory,
            final String resourceName) throws IOException {
        int count = 0;

        if (resourceName.endsWith(".tar")) {
            try (final InputStream in = obj.getResourceAsStream(resourceName)) {
                count = in.available();
                com.vmware.safekeeping.common.IOUtils.unTarFile(in, confDirectory);
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Copy resource %s to %s (%d bytes)", resourceName,
                            confDirectory.getAbsolutePath() + resourceName, count);
                    logger.info(msg);
                }
            }
        } else {
            try (final InputStream in = obj.getResourceAsStream(resourceName)) {
                int available = in.available();
                final File targetFile = new File(confDirectory.getAbsolutePath() + resourceName);
                int pos = 0;
                try (OutputStream outStream = new FileOutputStream(targetFile)) {
                    while (available > 0) {
                        final byte[] buffer = new byte[available];

                        int readSize = in.read(buffer, pos, available);
                        count += readSize;
                        outStream.write(buffer, pos, readSize);
                        pos += readSize;
                        available = in.available();
                    }
                }
                if (logger.isLoggable(Level.INFO)) {
                    final String msg = String.format("Copy resource %s to %s (%d bytes)", resourceName,
                            targetFile.toString(), count);
                    logger.info(msg);
                }
            }
        }
    }

    public static void deleteDirectoryRecursive(final File pathFile, final boolean root) throws IOException {
        if (pathFile.isDirectory()) {
            final File[] files = pathFile.listFiles();

            for (final File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursive(file, true);
                } else {
                    Files.delete(file.toPath());
                }
            }
            if (root) {
                Files.delete(pathFile.toPath());
            }
        }

    }

    public static String doubleQuote(final String st) {
        return String.format("\"%s\"", st);
    }

    public static ExecResult execCmd(final String[] command) {
        final ExecResult result = new ExecResult();
        try {

            final Process process = Runtime.getRuntime().exec(command);
            try (final BufferedReader sNumReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                result.output = inputReader.lines().collect(Collectors.joining("\n"));

                final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                result.error = errorReader.lines().collect(Collectors.joining("\n"));
            }
            result.success = true;
        } catch (final IOException e) {
            logWarning(logger, e);
            result.success = false;

        }
        return result;
    }

    public static String generateSnapshotName() {
        return generateSnapshotName(Calendar.getInstance());
    }

    public static String generateSnapshotName(final Calendar cal) {
        String snapName;
        snapName = String.format("VMBK_%d-%02d-%02d_%02d:%02d:%02d", cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        return snapName;
    }

    public static byte[] getDigest(final InputStream is, final MessageDigest md, final int byteArraySize)
            throws IOException {
        final StringBuilder hexString = new StringBuilder();
        md.reset();
        final byte[] bytes = new byte[byteArraySize];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            md.update(bytes, 0, numBytes);
        }
        final byte[] hash = md.digest();
        for (final byte element : hash) {
            if ((0xff & element) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & element)));
            } else {
                hexString.append(String.format("%02X", element));
            }
        }
        return hexString.toString().getBytes();
    }

    public static String getJavaRuntimeInfo() {
        final String vmName = System.getProperty("java.vm.name");
        final String runtimeVersion = System.getProperty("java.runtime.version");
        final String vmVersion = System.getProperty("java.version");
        final String versionDate = System.getProperty("java.version.date");
        final String vmInfo = System.getProperty("java.vm.info");
        final String javaVendor = System.getProperty("java.vendor");

        return String.format("%s %s (build %s %s, %s - vendor:%s)", vmName, runtimeVersion, vmVersion,
                (versionDate != null) ? versionDate : "", vmInfo, javaVendor);
    }

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            final int dot = version.indexOf('.');
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    /**
     * @param logger
     * @param e
     */
    public static void logWarning(final Logger logger, final Throwable e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        final String msg = sw.toString();
        logger.warning(msg);
    }

    public static String printByteArray(final byte[] bytes) {
        final StringBuilder st = new StringBuilder();
        for (final byte i : bytes) {
            st.append(String.format("%02X", i & 0xFF));
        }
        return st.toString();
    }

    public static void printJavaProperties(final PrintStream console) {
        final Properties properties = System.getProperties();
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            console.print("Key: ");
            console.print(entry.getKey());
            console.print("\t\t\t\t\tValue: ");
            console.print(entry.getValue());
            console.print("\n");
        }
    }

    public static Object readSerializedObject(final String fileName) throws IOException, ClassNotFoundException {
        final FileInputStream fos = new FileInputStream(fileName);
        try (final ObjectInputStream oos = new ObjectInputStream(fos)) {
            return oos.readObject();
        }

    }

    public static String removeQuote(String st) {
        if (StringUtils.isEmpty(st)) {
            return "";
        }
        if ((st.length() >= 2) && (st.charAt(0) == '"') && (st.charAt(st.length() - 1) == '"')) {
            st = st.substring(1, st.length() - 1);
        }
        return st;
    }

    /**
     * Set Java library path
     *
     * @param path
     */
    public static void setNativeLibraryPath(final String path) {
        // Set Native Library path
        final String oldLibraryPath = System.getProperty("java.library.path");
        final String libraryPath = ((oldLibraryPath != null) && !oldLibraryPath.isEmpty())
                ? oldLibraryPath.concat(File.pathSeparator).concat(path)
                : path;
        System.setProperty("java.library.path", libraryPath);
    }

    static String toBigEndianUuid(final String uuid) {
        final String b1 = String.copyValueOf(new char[] { uuid.charAt(6), uuid.charAt(7), uuid.charAt(4),
                uuid.charAt(5), uuid.charAt(2), uuid.charAt(3), uuid.charAt(0), uuid.charAt(1), uuid.charAt(8) });

        final String b2 = String.copyValueOf(
                new char[] { uuid.charAt(11), uuid.charAt(12), uuid.charAt(9), uuid.charAt(10), uuid.charAt(13) });
        final String b3 = String
                .copyValueOf(new char[] { uuid.charAt(16), uuid.charAt(17), uuid.charAt(14), uuid.charAt(15) });

        return b1.concat(b2.concat(b3.concat(uuid.substring(18))));
    }

    public static String toHexString(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (final int b : bytes) {
            sb.append(String.format("%02x ", b & 0xff));
        }
        return sb.toString();
    }

    public static boolean writeSerializedObject(final Object obj, final String fileName) throws IOException {
        final FileOutputStream fos = new FileOutputStream(fileName);
        try (final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(obj);
        }
        return true;
    }

    private Utility() {
        throw new IllegalStateException("Utility class");
    }

}
