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
package com.vmware.safekeeping.cxf.test.common;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;

import com.vmware.safekeeping.common.PrettyBoolean;
import com.vmware.safekeeping.common.Utility;
import com.vmware.sapi.BackupMode;
import com.vmware.sapi.ExtensionManagerOperation;
import com.vmware.sapi.FcoTarget;

public class ConsoleWrapper {
    public static final ConsoleWrapper console = new ConsoleWrapper();
    private BufferedReader br;
    private PrintStream ps;
    private final Console sysConsole;
    private final boolean noConsole;
    private final Scanner scan;

    public ConsoleWrapper() {
        this.sysConsole = System.console();
        this.noConsole = (this.sysConsole == null);
        if (this.noConsole) {

            this.br = new BufferedReader(new InputStreamReader(System.in));
            this.ps = System.out;
        }
        this.scan = new Scanner(System.in);
    }

    public void format(final String format, final Object... objects) {
        if (this.noConsole) {
            this.ps.format(format, objects);
        } else {
            this.sysConsole.format(format, objects);
        }
    }

    public Writer getWriter() {
        if (this.noConsole) {
            return new OutputStreamWriter(this.ps);
        } else {
            return this.sysConsole.writer();
        }

    }

    public boolean isNoConsole() {
        return this.noConsole;
    }

    public void print(final Character c) {
        printf(c.toString());

    }

    public void print(final String string) {
        printf(string);

    }

    public void printf(final String format, final Object... objects) {
        if (this.noConsole) {
            this.ps.printf(format, objects);
        } else {
            this.sysConsole.printf(format, objects);
        }

    }

    public void println() {
        printf("%n");
    }

    public void println(final String out) {
        printf(out + "%n");
    }

    public BackupMode readBackupMode(final String message, String defaultValue) {
        if (defaultValue == null) {
            defaultValue = BackupMode.UNKNOW.toString();
        }
        final String backupMode = readString(message, true, defaultValue, new String[] { BackupMode.FULL.toString(),
                BackupMode.INCREMENTAL.toString(), BackupMode.UNKNOW.toString() });
        return BackupMode.fromValue(backupMode.toUpperCase(Utility.LOCALE));
    }

    public Boolean readBoolean(final String message, final Boolean defaultValue) {
        final boolean acceptEmpty = defaultValue != null;
        if (defaultValue != null) {
            ConsoleWrapper.console.printf("%s [Boolean {True|False}  Default:%s] :", message, defaultValue.toString());
        } else {
            ConsoleWrapper.console.printf("%s [Boolean {True|False}] :", message);
        }

        String readString = "default";
        boolean stayInLoop = true;
        while (stayInLoop) {

            if (this.scan.hasNextLine()) {
                readString = this.scan.nextLine();
                if (PrettyBoolean.isBoolean(readString) || readString.isEmpty()) {
                    if (readString.isEmpty()) {
                        if (acceptEmpty) {
                            stayInLoop = false;
                        }
                    } else {
                        stayInLoop = false;
                    }
                } else {
                    ConsoleWrapper.console.printf("Invalid input - Boolean only accepted");
                }
            } else {
                readString = null;
                stayInLoop = false;
            }
        }
        if (StringUtils.isEmpty(readString) && acceptEmpty) {
            return defaultValue;
        } else {
            return PrettyBoolean.parseBoolean(readString);
        }
    }

    public List<FcoTarget> readFcoString(final String message) throws FormatMismatch {
        return readFcoString(message, false);
    }

    public List<FcoTarget> readFcoString(final String message, final boolean acceptEmpty) throws FormatMismatch {
        final String str = readString(message, acceptEmpty);
        if (StringUtils.isNotBlank(str)) {
            return TestUtility.getFcoTargets(str);
        } else {
            return Collections.emptyList();
        }

    }

    public List<Integer> readGenerations(final String message, final Integer defaultValue) {
        final List<Integer> generations = new LinkedList<>();
        if (defaultValue != null) {
            ConsoleWrapper.console.printf("%s [Integer,Integer,...,Integer    Default:[%d]] :", message, defaultValue);
        } else {
            ConsoleWrapper.console.printf("%s [Integer,Integer,...,Integer   Default:[%d]] :", message, defaultValue);
        }
        final String str = readString(null, defaultValue != null);
        if (StringUtils.isNotBlank(str)) {
            final String[] genStrArray = StringUtils.split(str, ',');
            for (final String st : genStrArray) {
                final int d = Integer.parseInt(st);
                generations.add(d);
            }
        } else {
            if (defaultValue != null) {
                generations.add(defaultValue);
            }
        }
        return generations;
    }

    public Integer readInt(final String message, final Integer defaultValue, final IntRange range) {
        final boolean acceptEmpty = defaultValue != null;
        if (defaultValue != null) {
            if (range != null) {
                ConsoleWrapper.console.printf("%s [Integer {%d..%d}  Default:%s] :", message, range.getMinimumInteger(),
                        range.getMaximumInteger(), defaultValue.toString());
            } else {
                ConsoleWrapper.console.printf("%s [Integer  Default:%s] :", message, defaultValue.toString());
            }
        } else {
            if (range != null) {
                ConsoleWrapper.console.printf("%s [Integer {%d..%d}] :", message, range.getMinimumInteger(),
                        range.getMaximumInteger());
            } else {
                ConsoleWrapper.console.printf("%s [Integer] :", message);
            }
        }
        String readString = "default";
        boolean stayInLoop = true;
        while (stayInLoop) {

            if (this.scan.hasNextLine()) {
                readString = this.scan.nextLine();
                if (StringUtils.isNumeric(readString)) {
                    if (readString.isEmpty()) {
                        if (acceptEmpty) {
                            stayInLoop = false;
                        }
                    } else {
                        stayInLoop = false;
                    }
                } else {
                    ConsoleWrapper.console.printf("Invalid input - Number only accepted");
                }
            } else {
                readString = null;
                stayInLoop = false;
            }
        }
        if (StringUtils.isEmpty(readString) && acceptEmpty) {
            return defaultValue;
        } else {
            return Integer.valueOf(readString);
        }
    }

    public String readLine(final String out) {
        if (this.noConsole) {
            this.ps.format(out);
            try {
                return this.br.readLine();
            } catch (final IOException e) {
                return null;
            }
        } else {
            return this.sysConsole.readLine(out);
        }

    }

    public String readString(final String message) {
        return readString(message, true, null, null);
    }

    public String readString(final String message, final boolean acceptEmpty) {
        return readString(message, acceptEmpty, null, null);
    }

    public String readString(final String message, final boolean acceptEmpty, final String defaultValue,
            final String[] range) {
        String readString = "default";
        if (StringUtils.isNotEmpty(message)) {
            if (defaultValue != null) {
                if (range != null) {
                    ConsoleWrapper.console.printf("%s [String {%s}  Default:%s] :", message,
                            StringUtils.join(range, "|"), defaultValue);
                } else {
                    ConsoleWrapper.console.printf("%s [String  Default:%s] :", message, defaultValue);
                }
            } else {
                if (range != null) {
                    ConsoleWrapper.console.printf("%s [String {%s}] :", message, message);
                } else {
                    ConsoleWrapper.console.printf("%s [String] :", message);
                }
            }
        }

        boolean stayInLoop = true;
        while (stayInLoop) {

            if (this.scan.hasNextLine()) {
                readString = this.scan.nextLine();
                if (readString.isEmpty()) {
                    if (acceptEmpty) {
                        stayInLoop = false;
                    }
                } else {
                    stayInLoop = false;
                }
            } else {
                readString = null;
                stayInLoop = false;
            }
        }
        if (StringUtils.isEmpty(readString) && acceptEmpty) {
            return defaultValue;
        } else {

            return readString;
        }
    }

    public String readTransportMode(final String message, String defaultValue) throws FormatMismatch {
        if (defaultValue == null) {
            defaultValue = "default";
        }
        final String transportMode = readString(message, true, defaultValue,
                // StringUtils.isNotBlank(defaultValue), defaultValue,
                new String[] { "hotadd", "hotadd:nbdssl", "hotadd:nbd", "hotadd:nbdssl:nbd", "nbdssl", "nbdssl:hotadd",
                        "nbdssl:nbd", "nbdssl:hotadd:nbd", "nbdssl:nbd:hotadd", "nbd", "nbd:hotadd",
                        "nbd:hotadd:nbdssl", "nbd:nbdsl:hotadd", "nbd:nbdssl" });
        // check TransportMode
        if (!transportMode.isEmpty()) {
            final String[] tr = transportMode.split(":");
            for (final String t : tr) {
                switch (t) {
                case "nbd":
                case "nbdssl":
                case "hotadd":
                case "default":
                    break;
                default:
                    throw new FormatMismatch("transportMode invalid :" + transportMode);
                }
            }
        }
        if (transportMode.equals("default")) {
            return "";
        } else {
            return transportMode;
        }
    }

    public ExtensionManagerOperation readExtensionOperation(final String message,
            ExtensionManagerOperation defaultValue) {
        if (defaultValue == null) {
            defaultValue = ExtensionManagerOperation.CHECK;
        }
        final String operation = readString(message, true, defaultValue.toString(),
                new String[] { ExtensionManagerOperation.REGISTER.toString(),
                        ExtensionManagerOperation.REMOVE.toString(), ExtensionManagerOperation.UPDATE.toString(),
                        ExtensionManagerOperation.CHECK.toString(), ExtensionManagerOperation.NONE.toString() });
        // check TransportMode
        if (!operation.isEmpty()) {
            switch (operation.toLowerCase(Utility.LOCALE)) {
            case "register":
                return ExtensionManagerOperation.REGISTER;
            case "remove":
                return ExtensionManagerOperation.REMOVE;
            case "forceupdate":
                return ExtensionManagerOperation.FORCE_UPDATE;
            case "update":
                return ExtensionManagerOperation.UPDATE;
            case "check":
                return ExtensionManagerOperation.CHECK;
            default:
                return ExtensionManagerOperation.NONE;
            }
        }

        return ExtensionManagerOperation.CHECK;

    }

}
