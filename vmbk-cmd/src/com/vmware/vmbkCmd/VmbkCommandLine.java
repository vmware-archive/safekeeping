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
package com.vmware.vmbkCmd;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.vmware.vmbkCmd.command.ArchiveCommandInteractive;
import com.vmware.vmbkCmd.command.BackupCommandInteractive;
import com.vmware.vmbkCmd.command.CommandInteractive;
import com.vmware.vmbkCmd.command.ConfigCommandInteractive;
import com.vmware.vmbkCmd.command.FcoCommandInteractive;
import com.vmware.vmbkCmd.command.HelpCommandInteractive;
import com.vmware.vmbkCmd.command.IvdCommandInteractive;
import com.vmware.vmbkCmd.command.K8sCommandInteractive;
import com.vmware.vmbkCmd.command.QuitCommandInteractive;
import com.vmware.vmbkCmd.command.RestoreCommandInteractive;
import com.vmware.vmbkCmd.command.SnapCommandInteractive;
import com.vmware.vmbkCmd.command.TagCommandInteractive;
import com.vmware.vmbkCmd.command.VappCommandInteractive;
import com.vmware.vmbkCmd.command.VersionCommandInteractive;
import com.vmware.vmbkCmd.command.VmCommandInteractive;

public class VmbkCommandLine {

    private static final Logger logger = Logger.getLogger(VmbkCommandLine.class.getName());
    // private static final String OPTION_CONF = "-conf";

    public static final String ARCHIVE = "archive";
    public static final String BACKUP = "backup";
    public static final String CONFIGURE = "configure";
    public static final String HELP = "help";
    public static final String IVD = "ivd";
    public static final String QUIT = "quit";
    public static final String RESTORE = "restore";
    public static final String SNAP = "snapshot";
    public static final String TAG = "tags";
    public static final String VERSION = "version";
    public static final String VM = "vm";
    public static final String K8S = "k8s";
    public static final String VAPP = "vapp";
    public static final String FCO = "fco";

    private String command;

    private List<String> targetList;
    private String[] arguments;

    private final HashMap<String, CommandInteractive> commandFunctions;

    public VmbkCommandLine() {
	this.targetList = null;
	this.arguments = null;
	this.commandFunctions = new HashMap<>();
	this.commandFunctions.put(BACKUP, new BackupCommandInteractive().configureParser());
	this.commandFunctions.put(RESTORE, new RestoreCommandInteractive().configureParser());
	this.commandFunctions.put(IVD, new IvdCommandInteractive().configureParser());
	this.commandFunctions.put(SNAP, new SnapCommandInteractive().configureParser());
	this.commandFunctions.put(ARCHIVE, new ArchiveCommandInteractive().configureParser());
	this.commandFunctions.put(TAG, new TagCommandInteractive().configureParser());
	this.commandFunctions.put(VM, new VmCommandInteractive().configureParser());
	this.commandFunctions.put(VAPP, new VappCommandInteractive().configureParser());
	this.commandFunctions.put(K8S, new K8sCommandInteractive().configureParser());
	this.commandFunctions.put(FCO, new FcoCommandInteractive().configureParser());
	this.commandFunctions.put(HELP, new HelpCommandInteractive().configureParser());
	this.commandFunctions.put(VERSION, new VersionCommandInteractive().configureParser());
	this.commandFunctions.put(CONFIGURE, new ConfigCommandInteractive().configureParser());
	this.commandFunctions.put(QUIT, new QuitCommandInteractive().configureParser());
    }

    CommandInteractive dispatch() throws ParsingException {
	logger.entering(getClass().getName(), "dispatch");
	CommandInteractive result = null;
	if (this.commandFunctions.containsKey(this.command)) {
	    result = this.commandFunctions.get(this.command);
	    result.initialize();

	    if (this.arguments == null) {
		this.arguments = new String[0];
	    }
	    result.parse(this.arguments);
	}
	logger.exiting(getClass().getName(), "dispatch", result);
	return result;
    }

    void forceConfiguration() {
	this.command = CONFIGURE;
    }

    public String getCommand() {
	return this.command;
    }

    public List<String> getTargets() {
	return this.targetList;
    }

    void parse(final List<String> args) {

	this.targetList = null;
	if (args.size() > 0) {
	    this.command = args.get(0);
	    args.remove(0);
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
