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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jline.builtins.Completers;
import org.jline.builtins.Completers.RegexCompleter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.logger.VmbkLog;
import com.vmware.vmbk.profile.GlobalConfiguration;
import com.vmware.vmbk.util.Utility;
import com.vmware.vmbkCmd.command.ArchiveCommandInteractive;
import com.vmware.vmbkCmd.command.BackupCommandInteractive;
import com.vmware.vmbkCmd.command.CommandInteractive;
import com.vmware.vmbkCmd.command.ConfigCommandInteractive;
import com.vmware.vmbkCmd.command.FcoCommandInteractive;
import com.vmware.vmbkCmd.command.HelpCommandInteractive;
import com.vmware.vmbkCmd.command.IvdCommandInteractive;
import com.vmware.vmbkCmd.command.QuitCommandInteractive;
import com.vmware.vmbkCmd.command.RestoreCommandInteractive;
import com.vmware.vmbkCmd.command.SnapCommandInteractive;
import com.vmware.vmbkCmd.command.TagCommandInteractive;
import com.vmware.vmbkCmd.command.VappCommandInteractive;
import com.vmware.vmbkCmd.command.VersionCommandInteractive;
import com.vmware.vmbkCmd.command.VmCommandInteractive;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class VmbkMain { // NO_UCD (unused code)

    private static final String HELP = "help";

    private static final String INTERACTIVE = "interactive";

    private static final Logger logger = Logger.getLogger(VmbkMain.class.getName());

    private static final String STDIN = "stdin";
    private static final String COMMAND_FILE = "file";

    private static final String VERSION = "version";

    private static final String CONFIG_FILE = "configFile";

    public static void main(final String[] args) throws NoSuchAlgorithmException, KeyManagementException {
	final VmbkMain vmbkMain = new VmbkMain();

	if (vmbkMain.initialize() == false) {
	    System.exit(1);
	}
	int exitStatus;
	try {
	    exitStatus = vmbkMain.run(args);
	} catch (final IOException e) {

	    e.printStackTrace();
	    exitStatus = 9;
	}
	System.exit(exitStatus);
    }

    private IoFunctionImpl ioManager;

    private Vmbk vmbk;

    private String configPropertyPath;

    private VmbkCommandLine cmdLine;

    private int batch() {
	logger.entering(getClass().getName(), "batch");
	int result = 0;
	String line = null;
	int lineNumber = 0;
	this.ioManager.setInteractive(false);
	try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
	    this.vmbk.getConnetionManager().connect();
	    System.out.println("Batch Mode ");
	    while ((line = br.readLine()) != null) {
		try {
		    ++lineNumber;
		    if (StringUtils.isNotEmpty(line)) {
			System.out.println("###################################################");
			System.out.printf("# Line   :\t%d \n", lineNumber);
			System.out.printf("# Command:\t%s \n", line);
			System.out.println("#");
			System.out.println("###################################################");
			System.out.println();
			commandFlow(line);
			System.out.println();
		    }
		} catch (final ParseException e) {
		    System.out.println("Parameter is not correct");
		} catch (final NoCommandException e) {

		} catch (final ParsingException e) {
		    System.out.println(e.getMessage());
		} catch (final Exception e) {
		    e.printStackTrace();
		}

	    }
	} catch (final IOException e) {
	    e.printStackTrace();
	    result = 1;
	} catch (final Exception e) {
	    e.printStackTrace();
	    result = e.getCause().hashCode();
	} finally {
	    System.out.println("\nClosing connection...");
	    this.vmbk.finalizeApp();
	    System.out.println("Bye. ");
	}
	logger.exiting(getClass().getName(), "batch", result);
	return result;

    }

    private void checkConfiguration() {
	logger.entering(getClass().getName(), "checkConfiguration");
	try {
	    String configPath = this.configPropertyPath;
	    if (StringUtils.isEmpty(configPath)) {
		configPath = GlobalConfiguration.getDefaulConfigPropertiesFile();
	    }
	    final Boolean configReturnStatus = this.vmbk.configure(configPath);
	    if (configReturnStatus == false) {
		System.err.println("There is a problem with the configuration. Manual reconfiguration enforced");

		this.cmdLine.forceConfiguration();
		final ConfigCommandInteractive info = (ConfigCommandInteractive) this.cmdLine.dispatch();
		if (GlobalConfiguration.manualConfiguration(info.quiet)) {
		    GlobalConfiguration.write();
		    final Boolean configured = this.vmbk.configure(configPath);
		    if ((configured == null) || !configured) {
			throw new Exception("Configuration issue");
		    }
		}
	    } else {
		if (configReturnStatus) {

		} else {
		    System.err.println("Initialization issue");
		    // System.exit(4);
		}
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	    System.err.println(e.getLocalizedMessage());
	    System.exit(4);
	}
	logger.exiting(getClass().getName(), "checkConfiguration");
    }

    private boolean commandFlow(final String line) throws Exception {
	logger.entering(getClass().getName(), "commandFlow", line);
	boolean result = false;
	if (StringUtils.isNotEmpty(line)) {
	    if (line.equalsIgnoreCase("quit")) {
		result = true;
	    } else if (line.startsWith("!")) {
		runShellCommand(line);
	    } else if (line.startsWith("&")) {

		runScript(line);
	    } else if (line.startsWith("#")) {
		logger.info("Comment: ".concat(line.substring(1)));
	    } else {
		final ArrayList<String> lineArgs = lineArgsBuilder(line);
		if (lineArgs.size() > 0) {
		    this.cmdLine.parse(lineArgs);
		    dispatch(this.vmbk, this.cmdLine);
		}
	    }
	}
	logger.exiting(getClass().getName(), "commandFlow", result);
	return result;
    }

    public OptionParser configureParser() {
	logger.entering(getClass().getName(), "configureParser");
	final OptionParser result = new OptionParser(true);

	result.accepts(VmbkMain.INTERACTIVE, "Run interactive");

	result.accepts(VmbkMain.VERSION, "Show Version.");
	final OptionSpecBuilder optionFile = result.accepts(COMMAND_FILE, "Run the script.");
	optionFile.withRequiredArg().ofType(File.class);
	result.accepts(VmbkMain.STDIN, "Input form stdin.");

	final OptionSpecBuilder optionHelp = result.accepts(VmbkMain.HELP, "Help");
	final OptionSpecBuilder optionConfigFile = result.accepts(VmbkMain.CONFIG_FILE, "Config.property file path");
	optionConfigFile.withRequiredArg().ofType(File.class);

	optionHelp.forHelp();
	logger.exiting(getClass().getName(), "configureParser", result);
	return result;
    }

    public void dispatch(final Vmbk vmbk, final VmbkCommandLine cmdLine) throws Exception {
	logger.entering(getClass().getName(), "dispatch", new Object[] { vmbk, cmdLine });
	final CommandInteractive info = cmdLine.dispatch();
	if (info == null) {
	    throw new NoCommandException(cmdLine.getCommand());
	}
	if (info.isHelp()) {
	    info.action(vmbk);
	    return;
	}

	info.action(vmbk);

	logger.exiting(getClass().getName(), "dispatch");
    }

    private RegexCompleter getRegexCompleter() {
	logger.entering(getClass().getName(), "getRegexCompleter");
	final Map<String, StringsCompleter> comp = new HashMap<>();
	final String regExString = new HelpCommandInteractive().getRegexCompleter(comp)
		+ new QuitCommandInteractive().getRegexCompleter(comp)
		+ new VersionCommandInteractive().getRegexCompleter(comp)
		+ new ConfigCommandInteractive().getRegexCompleter(comp)
		+ new ArchiveCommandInteractive().getRegexCompleter(comp)
		+ new RestoreCommandInteractive().getRegexCompleter(comp)
		+ new BackupCommandInteractive().getRegexCompleter(comp)
		+ new VmCommandInteractive().getRegexCompleter(comp)
		+ new SnapCommandInteractive().getRegexCompleter(comp)
		+ new IvdCommandInteractive().getRegexCompleter(comp)
		+ new TagCommandInteractive().getRegexCompleter(comp)
		+ new VappCommandInteractive().getRegexCompleter(comp)
		+ new FcoCommandInteractive().getRegexCompleter(comp);
	final RegexCompleter result = new Completers.RegexCompleter(regExString.substring(1), comp::get);
	logger.exiting(getClass().getName(), "getRegexCompleter", result);
	return result;
    }

    public boolean initialize() {
	logger.entering(getClass().getName(), "interactive");
	boolean result = false;
	try {
	    VmbkLog.loadLogSetting();
	    this.ioManager = new IoFunctionImpl();
	    IoFunction.setFunction(this.ioManager);
	    this.cmdLine = new VmbkCommandLine();
	    logger.info("\n" + "----------------------------------------\n");
	    VersionCommandInteractive.showVersion();
	    this.vmbk = new Vmbk();
	    result = true;
	} catch (final Exception e) {
	    e.printStackTrace();
	    System.err.println("Log setting initialiation failed.");

	}
	logger.exiting(getClass().getName(), "interactive", result);
	return result;
    }

    private int interactive() {
	logger.entering(getClass().getName(), "interactive");
	int result = 0;
	try (final Terminal terminal = TerminalBuilder.builder().system(true).signalHandler(VmbkSignalHandler.VBSH)
		.build()) {

	    final LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).completer(getRegexCompleter())
		    .build();

	    this.ioManager.setInteractive(true);

	    terminal.writer().println("Interactive Mode ");
	    terminal.writer().println("<help> for help  - <quit> to leave");
	    final String prompt = GlobalConfiguration.getInteractivePrompt() + " ";

	    while (true) {
		if (Vmbk.isAbortTriggered()) {
		    Vmbk.cancelAbortRequest();
		}
		String line = null;
		try {
		    line = lineReader.readLine(prompt);
		    if (commandFlow(line)) {
			break;
		    }
		} catch (final UserInterruptException e) {

		} catch (final EndOfFileException e) {
		} catch (final ParseException e) {
		    System.err.println(e.getMessage());
		} catch (final NoCommandException e) {
		    terminal.writer().println(e.getLocalizedMessage());
		} catch (final ParsingException e) {
		    terminal.writer().println(e.getLocalizedMessage());
		} catch (final joptsimple.OptionException e) {
		    terminal.writer().println(e.getLocalizedMessage());
		} catch (final Exception e) {
		    System.err.println(e.getMessage());
		}
	    }
	} catch (final IOException e) {
	    e.printStackTrace();
	    result = 1;
	} catch (final Exception e) {
	    e.printStackTrace();
	    result = e.getCause().hashCode();
	} finally {
	    System.out.println();
	    System.out.println("Closing connection...");
	    this.vmbk.finalizeApp();
	    System.out.println("Bye. ");
	}
	logger.exiting(getClass().getName(), "interactive", result);
	return result;

    }

    private ArrayList<String> lineArgsBuilder(final String line) {
	logger.entering(getClass().getName(), "lineArgsBuilder", line);
	final ArrayList<String> result = new ArrayList<>();
	final char charArray[] = line.trim().toCharArray();
	boolean openQuote = false;
	StringBuilder lineBuilder = new StringBuilder();
	for (int i = 0; i < charArray.length; i++) {
	    if (charArray[i] == '"') {
		openQuote = !openQuote;
		continue;
	    }
	    if (openQuote) {
		lineBuilder.append(charArray[i]);
	    } else {
		if (charArray[i] == ' ') {
		    result.add(lineBuilder.toString());
		    lineBuilder = new StringBuilder();
		} else {
		    lineBuilder.append(charArray[i]);
		}
	    }
	}
	result.add(lineBuilder.toString());
	logger.exiting(getClass().getName(), "lineArgsBuilder", result);
	return result;
    }

    public int run(final String[] args) throws IOException {
	logger.entering(getClass().getName(), "run", args);
	int result = 1;
	try {
	    final OptionParser parser = configureParser();
	    parser.allowsUnrecognizedOptions();

	    OptionSet options = null;

	    options = parser.parse(args);
	    if (options.has(CONFIG_FILE)) {
		this.configPropertyPath = options.valueOf(CONFIG_FILE).toString();
	    }
	    if (options.has(VmbkMain.HELP)) {
		HelpCommandInteractive.introHelp();

		parser.printHelpOn(System.out);
		System.out.println();
		System.out.println("#######################################");
		System.out.println();
		HelpCommandInteractive.showHelp();
		result = 0;

	    } else if (options.has(VmbkMain.VERSION)) {
		result = 0;

	    } else if (options.has(VmbkMain.INTERACTIVE) || (args.length == 0)) {
		checkConfiguration();
		result = interactive();
	    } else if (options.has(VmbkMain.STDIN)) {
		checkConfiguration();
		result = batch();
	    } else if (options.has(COMMAND_FILE)) {
		checkConfiguration();
		result = (runScript((File) options.valueOf(COMMAND_FILE)) == 0) ? 0 : 3;

	    } else {
		try {
		    checkConfiguration();
		    final List<String> arguments = new ArrayList<>();
		    options.nonOptionArguments().forEach((xx) -> arguments.add(String.valueOf(xx)));
		    this.cmdLine.parse(arguments);
		    if (StringUtils.isEmpty(this.cmdLine.getCommand())) {
			return interactive();
		    } else {
			dispatch(this.vmbk, this.cmdLine);
		    }
		} catch (final Exception e) {
		    File script = null;
		    if (StringUtils.isNotEmpty(this.cmdLine.getCommand())
			    && (script = new File(this.cmdLine.getCommand())).isFile()) {
			result = (runScript(script) == 0) ? 0 : 3;
		    } else {
			logger.warning(e.toString());
			System.err.printf("Command execution failed: %s.\n", e.getMessage());
			result = 3;
		    }
		}
	    }
	} finally {
	    this.vmbk.finalizeApp();
	}
	logger.exiting(getClass().getName(), "run", result);
	return result;
    }

    /**
     * Run a script in batch mode
     *
     * @param scriptFile file to execute
     * @return Number of line executed
     * @throws FileNotFoundException
     * @throws IOException
     */
    private int runScript(final File scriptFile) {
	logger.entering(getClass().getName(), "runScript", scriptFile);
	int result = 0;
	if (scriptFile.exists() && scriptFile.canRead()) {
	    boolean interactiveMode = false;
	    if (this.ioManager.isInteractive()) {
		interactiveMode = true;
		this.ioManager.setInteractive(false);
	    }
	    try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
		this.vmbk.getConnetionManager().connect();
		System.out.println("Batch Mode ");
		String inputline;
		try {
		    while ((inputline = br.readLine()) != null) {
			++result;
			if (StringUtils.isEmpty(inputline)) {
			    continue;
			}
			commandFlow(inputline);
		    }
		} catch (final EndOfFileException e) {
		} catch (final ParseException e) {
		    System.err.println(e.getMessage());
		} catch (final NoCommandException e) {
		    System.err.println(e.getMessage());
		} catch (final ParsingException e) {
		    System.err.println(e.getMessage());
		} catch (final Exception e) {
		    System.err.println(e.getMessage());
		}
	    } catch (final FileNotFoundException e) {
		System.err.printf("File %s not found.\n", scriptFile);
		result = 0;
	    } catch (final IOException e) {
		e.printStackTrace();
		System.err.printf("Command execution failed: %s.\n", e.getMessage());
		result = 0;
	    }

	    if (interactiveMode) {
		this.ioManager.setInteractive(true);
		System.out.println("Interactive Mode ");
	    }
	} else {
	    IoFunction.showWarning(logger, "File %s cannot be loaded", scriptFile.toPath().toString());
	    result = 0;
	}
	logger.exiting(getClass().getName(), "runScript", result);
	return result;
    }

    /**
     * Run a script in batch mode
     *
     * @param scriptFileName name of the file to execute
     * @return Number of line executed
     * @throws FileNotFoundException
     * @throws IOException
     */
    private int runScript(String scriptFileName) throws FileNotFoundException, IOException {
	logger.entering(getClass().getName(), "runScript", scriptFileName);
	scriptFileName = scriptFileName.substring(1).trim();
	final int result = runScript(new File(scriptFileName));
	logger.exiting(getClass().getName(), "runScript", result);
	return result;
    }

    /**
     * Run as shell command in windows or unix
     *
     * @param line Command to execute
     */
    private void runShellCommand(final String line) {
	logger.entering(getClass().getName(), "runShellCommand", line);
	final ArrayList<String> cmdShell = new ArrayList<>();

	switch (Utility.getOS()) {
	case "win":
	    cmdShell.add("cmd");
	    cmdShell.add("/c");
	    break;
	case "osx":
	case "uni":
	case "sol":
	    cmdShell.add("bash");
	    cmdShell.add("-c");
	    break;
	default:
	    return;
	}
	cmdShell.add(line.substring(1).trim());
	Utility.execCmd(cmdShell.toArray(new String[0]));
	logger.exiting(getClass().getName(), "runShellCommand");
    }

}
