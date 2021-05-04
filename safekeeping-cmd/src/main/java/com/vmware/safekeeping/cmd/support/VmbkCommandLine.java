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
package com.vmware.safekeeping.cmd.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vmware.safekeeping.cmd.command.ArchiveCommandInteractive;
import com.vmware.safekeeping.cmd.command.BackupCommandInteractive;
import com.vmware.safekeeping.cmd.command.ConfigCommandInteractive;
import com.vmware.safekeeping.cmd.command.ConnectCommandInteractive;
import com.vmware.safekeeping.cmd.command.DisconnectCommandInteractive;
import com.vmware.safekeeping.cmd.command.ExtensionCommandInteractive;
import com.vmware.safekeeping.cmd.command.FcoCommandInteractive;
import com.vmware.safekeeping.cmd.command.HelpCommandInteractive;
import com.vmware.safekeeping.cmd.command.ICommandInteractive;
import com.vmware.safekeeping.cmd.command.IvdCommandInteractive;
import com.vmware.safekeeping.cmd.command.K8sCommandInteractive;
import com.vmware.safekeeping.cmd.command.QuitCommandInteractive;
import com.vmware.safekeeping.cmd.command.RestoreCommandInteractive;
import com.vmware.safekeeping.cmd.command.SnapCommandInteractive;
import com.vmware.safekeeping.cmd.command.TagCommandInteractive;
import com.vmware.safekeeping.cmd.command.TestCommandInteractive;
import com.vmware.safekeeping.cmd.command.VappCommandInteractive;
import com.vmware.safekeeping.cmd.command.VersionCommandInteractive;
import com.vmware.safekeeping.cmd.command.VirtualBackupCommandInteractive;
import com.vmware.safekeeping.cmd.command.VmCommandInteractive;

public class VmbkCommandLine {

    public static final String REM = "rem ";
    public static final String CALL = "call ";
    public static final String ECHO = "echo ";
    private String command;

    private List<String> targetList;
    private String[] arguments;

    private final Map<String, ICommandInteractive> commandFunctions;

    public VmbkCommandLine() {
        this.targetList = null;
        this.arguments = null;
        this.commandFunctions = new LinkedHashMap<>();
        addCommand(new ConnectCommandInteractive().configureParser());
        addCommand(new BackupCommandInteractive().configureParser());
        addCommand(new VirtualBackupCommandInteractive().configureParser());
        addCommand(new RestoreCommandInteractive().configureParser());
        addCommand(new IvdCommandInteractive().configureParser());
        addCommand(new SnapCommandInteractive().configureParser());
        addCommand(new ArchiveCommandInteractive().configureParser());
        addCommand(new TagCommandInteractive().configureParser());
        addCommand(new VmCommandInteractive().configureParser());
        addCommand(new VappCommandInteractive().configureParser());
        addCommand(new K8sCommandInteractive().configureParser());
        addCommand(new FcoCommandInteractive().configureParser());
        addCommand(new TestCommandInteractive().configureParser());
        addCommand(new DisconnectCommandInteractive().configureParser());
        addCommand(new ExtensionCommandInteractive().configureParser());
        addCommand(new VersionCommandInteractive().configureParser());
        addCommand(new ConfigCommandInteractive().configureParser());
        addCommand(new QuitCommandInteractive().configureParser());
        addCommand(new HelpCommandInteractive(this).configureParser());

    }

    private ICommandInteractive addCommand(final Entry<String, ICommandInteractive> entry) {
        return this.commandFunctions.put(entry.getKey(), entry.getValue());
    }

    public ICommandInteractive dispatch() throws ParsingException {
        ICommandInteractive result = null;
        if (this.commandFunctions.containsKey(this.command)) {
            result = this.commandFunctions.get(this.command);
            result.initialize();

            if (this.arguments == null) {
                this.arguments = new String[0];
            }
            result.parse(this.arguments);
        }
        return result;
    }

    public void forceConfiguration() {
        this.command = ConfigCommandInteractive.CONFIGURE;
    }

    public String getCommand() {
        return this.command;
    }

    public Map<String, ICommandInteractive> getCommandFunctions() {
        return this.commandFunctions;
    }

    public List<String> getTargets() {
        return this.targetList;
    }

    public void parse(final List<String> args) {
        this.targetList = null;
        if (!args.isEmpty()) {
            this.command = args.remove(0);
            args.removeAll(Collections.singleton(""));
            this.arguments = args.toArray(new String[args.size()]);
        }
    }

    void parse(final String[] args) {

        this.targetList = null;
        if (args.length > 0) {
            this.arguments = new String[args.length - 1];
            System.arraycopy(args, 1, this.arguments, 0, this.arguments.length);
            this.command = args[0];
        }
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public void setTargets(final List<String> targetList) {
        this.targetList = targetList;
    }

}
