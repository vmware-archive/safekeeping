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
package com.vmware.safekeeping.core.type.manipulator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

class PropertiesFileManipulator implements AutoCloseable {
    protected final LinkedHashMap<String, String> fileContent;
    private File tempFile;

    PropertiesFileManipulator(final byte[] encoded) {
        this.fileContent = new LinkedHashMap<>();
        final String vmxString = new String(encoded, StandardCharsets.UTF_8);
        final String[] lines = vmxString.split("\n");
        int emptySpace = 0;
        for (final String line : lines) {
            if (line.trim().startsWith("#")) {
                this.fileContent.put(line, " ");
            } else if (line.trim().length() == 0) {
                ++emptySpace;
                final String key = String.join("", Collections.nCopies(emptySpace, " "));

                this.fileContent.put(key, " ");

            } else {
                final int equalSignePosition = line.indexOf('=');
                if ((equalSignePosition == -1) || line.startsWith("#")) {
                    continue;
                }
                final String key = line.substring(0, equalSignePosition).trim();
                final String value = line.substring(equalSignePosition + 1).trim();
                this.fileContent.put(key, value);
            }
        }
    }

    PropertiesFileManipulator(final String fileName) throws IOException {
        this(Files.readAllBytes(Paths.get(fileName)));
    }

    @Override
    public void close() throws IOException {
        if (this.tempFile != null) {
            Files.deleteIfExists(this.tempFile.toPath());
        }

    }

    public byte[] getBytes() {
        return toString().getBytes();

    }

// TODO Remove unused code found by UCDetector
//     public File saveToTemp(final String filename) throws IOException {
// 	final StringBuilder newVmxString = new StringBuilder();
// 	for (final String key : this.fileContent_.keySet()) {
// 	    newVmxString.append(key);
// 	    newVmxString.append(" = ");
// 	    newVmxString.append(this.fileContent_.get(key));
//
// 	    newVmxString.append('\n');
//
// 	}
//
// 	Writer fstream = null;
// 	this.tempFile_ = File.createTempFile(filename, null);
// 	fstream = new OutputStreamWriter(new FileOutputStream(this.tempFile_), StandardCharsets.UTF_8);
// 	fstream.write(newVmxString.toString());
// 	fstream.close();
// 	return this.tempFile_;
//     }

    @Override
    public String toString() {
        final StringBuilder newContent = new StringBuilder();
        for (final Entry<String, String> entry : this.fileContent.entrySet()) {
            String key = entry.getKey();
            if (key.trim().startsWith("#")) {
                newContent.append(key);
                newContent.append('\n');
            } else if (key.trim().length() > 0) {
                newContent.append(key);
                newContent.append(" = ");
                newContent.append(this.fileContent.get(key));
                newContent.append('\n');
            } else {
                newContent.append('\n');
            }

        }
        return newContent.toString();
    }

}
