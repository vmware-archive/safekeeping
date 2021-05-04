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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * Utilities for IO operations.
 */
public final class IOUtils {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(IOUtils.class.getName());

	private static final int BUFFER_SIZE = 1024 * 4;

	/**
	 * Copies all bytes from the given input stream to the given output stream.
	 * Caller is responsible for closing the streams.
	 *
	 * @throws IOException if there is any IO exception during read or write.
	 */
	public static long copy(final InputStream in, final OutputStream out) throws IOException {
		return copy(in, out, Long.MAX_VALUE);
	}

	/**
	 * Copies all bytes from the given input stream to the given output stream.
	 * Caller is responsible for closing the streams.
	 *
	 * @throws IOException if there is any IO exception during read or write or the
	 *                     read limit is exceeded.
	 */
	public static long copy(final InputStream in, final OutputStream out, final long readLimit) throws IOException {
		final byte[] buf = new byte[BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while ((n = in.read(buf)) > -1) {
			out.write(buf, 0, n);
			count += n;
			if (count >= readLimit) {
				throw new IOException("Read limit exceeded: " + readLimit);
			}
		}
		return count;
	}

	public static void copyFile(final File source, final File dest) throws IOException {
		Files.copy(source.toPath(), dest.toPath());
	}

	public static void copyFile(final Path source, final Path dest) throws IOException {
		Files.copy(source, dest);
	}

	public static void copyFile(final String source, final String dest) throws IOException {
		Files.copy(Paths.get(source), Paths.get(dest));
	}

	/**
	 * Reads and returns the rest of the given input stream as a byte array. Caller
	 * is responsible for closing the given input stream.
	 */
	public static int copyToByteArray(final InputStream is, final byte[] buffer) throws IOException {
		int size = 0;
		try (final ExtendedByteArrayOutputStream output = new ExtendedByteArrayOutputStream(buffer)) {
			final byte[] b = new byte[BUFFER_SIZE];
			int n = 0;
			while ((n = is.read(b)) != -1) {
				output.write(b, 0, n);
				size += n;
			}
		}
		return size;
	}

	/**
	 * Read all remaining data in the stream.
	 *
	 * @param in InputStream to read.
	 */
	public static void drainInputStream(final InputStream in) {
		try {
			while (in.read() != -1) {
				// Do nothing.
			}
		} catch (final IOException ignored) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "drainInputStream", ignored);
			}
			// Stream may be self closed by HTTP client so we ignore any failures.
		}
	}

	public static byte[] getFileChecksum(final MessageDigest digest, final String file) throws IOException {

		// Get file input stream for reading the file content
		try (FileInputStream fis = new FileInputStream(file)) {

			// Create byte array to read data in chunks
			final byte[] byteArray = new byte[1024];
			int bytesCount = 0;

			// Read file data and update in message digest
			while ((bytesCount = fis.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}
		}

		// Get the hash's bytes
		final byte[] bytes = digest.digest();
		return bytes;
	}

	public static long inputStreamToFile(final InputStream in, final File fileOut) throws IOException {
		try (final OutputStream out = new FileOutputStream(fileOut)) {
			final byte[] buf = new byte[BUFFER_SIZE];
			int len;
			long size = 0;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				size += len;
			}
			return size;
			// in.close();
		}
	}

	public static long inputStreamToFile(final InputStream in, final String fileOut) throws IOException {
		return inputStreamToFile(in, new File(fileOut));
	}

	public static byte[] readBinaryFile(final String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(fileName));
	}

	public static String readTextFile(final String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileName)));
	}

	/**
	 * Reads and returns the rest of the given input stream as a byte array. Caller
	 * is responsible for closing the given input stream.
	 */
	public static byte[] toByteArray(final InputStream is) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			final byte[] b = new byte[BUFFER_SIZE];
			int n = 0;
			while ((n = is.read(b)) != -1) {
				output.write(b, 0, n);
			}
			return output.toByteArray();
		} catch (final Exception e) {
			Utility.logWarning(logger, e);
		}
		return new byte[0];
	}

	/**
	 * Reads and returns the rest of the given input stream as a string. Caller is
	 * responsible for closing the given input stream.
	 */
	public static String toString(final InputStream is) throws IOException {
		return new String(toByteArray(is), StandardCharsets.UTF_8);
	}

	private static void unpackEntries(final TarArchiveInputStream tis, final TarArchiveEntry entry,
			final File outputDir) throws IOException {
		final File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getCanonicalPath().startsWith(outputDir.getCanonicalPath())) {
			throw new IOException("expanding " + entry.getName() + " would create entry outside of " + outputDir);
		}

		if (entry.isDirectory()) {
			final File subDir = new File(outputDir, entry.getName());
			if (!subDir.mkdirs() && !subDir.isDirectory()) {
				throw new IOException("Mkdirs failed to create tar internal dir " + outputDir);
			}

			for (final TarArchiveEntry e : entry.getDirectoryEntries()) {
				unpackEntries(tis, e, subDir);
			}

			return;
		}

		if (entry.isSymbolicLink()) {
// Create symbolic link relative to tar parent dir
			Files.createSymbolicLink(FileSystems.getDefault().getPath(outputDir.getPath(), entry.getName()),
					FileSystems.getDefault().getPath(entry.getLinkName()));
			return;
		}

		if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
			throw new IOException("Mkdirs failed to create tar internal dir " + outputDir);
		}

		if (entry.isLink()) {
			final File src = new File(outputDir, entry.getLinkName());
			Files.createLink(src.toPath(), outputFile.toPath());
			return;
		}

		int count;
		final byte[] data = new byte[2048];
		try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {

			while ((count = tis.read(data)) != -1) {
				outputStream.write(data, 0, count);
			}

			outputStream.flush();
		}
	}

	/**
	 *
	 * @param tarFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void unTarFile(final InputStream fis, final File destFile) throws IOException {

		try (final TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {
			TarArchiveEntry tarEntry = null;

			// tarIn is a TarArchiveInputStream
			while ((tarEntry = tis.getNextTarEntry()) != null) {

				unpackEntries(tis, tarEntry, destFile);

			}
		}
	}

	public static void writeBinaryFile(final String filePath, final byte[] data) throws IOException {
		Files.write(Paths.get(filePath), data);

	}

	public static void writeTextFile(final String filePath, final String data) throws IOException {
		Files.write(Paths.get(filePath), data.getBytes());
	}

	private IOUtils() {
		throw new IllegalStateException("Utility class");
	}

}
