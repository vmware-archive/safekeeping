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
package com.vmware.safekeeping.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.cmd.command.ConfigCommandInteractive;
import com.vmware.safekeeping.cmd.command.HelpCommandInteractive;
import com.vmware.safekeeping.cmd.command.ICommandInteractive;
import com.vmware.safekeeping.cmd.command.OperationStateList;
import com.vmware.safekeeping.cmd.settings.CmdGlobalSettings;
import com.vmware.safekeeping.cmd.support.IoFunctionImpl;
import com.vmware.safekeeping.cmd.support.NoCommandException;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkCommandLine;
import com.vmware.safekeeping.cmd.support.VmbkSignalHandler;
import com.vmware.safekeeping.common.ConsoleWrapper;
import com.vmware.safekeeping.common.GuestOsUtils;
import com.vmware.safekeeping.common.JavaWarning;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.common.Utility.ExecResult;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.IoFunctionInterface;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class App {

    static class ScriptStateListSupport {
        private final OperationStateList opStateList;
        private final String command;
        private final int lineNumber;

        /**
         * script with a predefine state
         *
         * @param lineNumber
         * @param command
         * @param state
         * @param reason
         */
        public ScriptStateListSupport(final int lineNumber, final String command, final OperationState state,
                final String reason) {
            this.lineNumber = lineNumber;
            this.command = command;
            this.opStateList = new OperationStateList(state, reason);
        }

        public ScriptStateListSupport(final int lineNumber, final String command,
                final OperationStateList opStateList) {
            this.lineNumber = lineNumber;
            this.command = command;
            this.opStateList = opStateList;
        }

        public String getCommand() {
            return this.command;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        public OperationStateList getOpStateList() {
            return this.opStateList;
        }

    }

    private static final String HELP = "help";

    private static final String INTERACTIVE = "interactive";

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String STDIN = "stdin";

    private static final String COMMAND_FILE = "file";

    private static final String VERSION = "version";

    private static final String CONFIG_FILE = "configFile";

    private static final String QUIT = "quit";

    private static final int DEFAULT_NUMBER_OF_VDDK_THREADS = 1;

    private static final int DEFAULT_NUMBER_OF_CONCURRENTS_FCO_THREADS = 1;

    private static final int DEFAULT_NUMBER_OF_CONCURRENTS_ARCHIVE_THREADS = 1;
    private static final int EXIT_STATUS_ERROR = 1;

    public static void main(final String[] args) {

        int exitStatus = 0;
        final App vmbkMain = new App();
        try {
            JavaWarning.disableWarning();
            CoreGlobalSettings.createLogFolder();
            CoreGlobalSettings.loadLogSetting();
            CoreGlobalSettings.createConfigFolder(App.class);

            exitStatus = vmbkMain.run(args);
        } catch (final IOException | URISyntaxException | NoSuchFieldException | ClassNotFoundException
                | IllegalAccessException e) {
            Utility.logWarning(logger, e);
            exitStatus = EXIT_STATUS_ERROR;
        }
        System.exit(exitStatus);

    }

    private IoFunctionImpl ioManager;

    private Vmbk vmbk;

    private String configPropertyPath;

    private VmbkCommandLine cmdLine;

    private int batch() {
        int result = 0;
        String line = null;
        int lineNumber = 0;
        this.ioManager.setInteractive(false);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            this.vmbk.getConnetionManager().connectVimConnetions();
            ConsoleWrapper.console.println("Batch Mode ");
            while ((line = br.readLine()) != null) {
                try {
                    ++lineNumber;
                    if (StringUtils.isNotEmpty(line)) {
                        ConsoleWrapper.console.println("###################################################");
                        ConsoleWrapper.console.printf("# Line   :\t%d", lineNumber);
                        ConsoleWrapper.console.println();
                        ConsoleWrapper.console.printf("# Command:\t%s ", line);
                        ConsoleWrapper.console.println();
                        ConsoleWrapper.console.println("#");
                        ConsoleWrapper.console.println("###################################################");
                        ConsoleWrapper.console.println();
                        commandFlow(line);
                        ConsoleWrapper.console.println();
                    }
                } catch (final NoCommandException e) {

                } catch (final ParsingException e) {
                    ConsoleWrapper.console.println(e.getMessage());
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
            ConsoleWrapper.console.println("\nClosing connection...");
            this.vmbk.close();
            ConsoleWrapper.console.println("Bye. ");
        }
        return result;

    }

    private OperationStateList commandFlow(final String line) throws NoCommandException, ParsingException,
            JVixException, CoreResultActionException, SafekeepingException, SafekeepingConnectionException {
        OperationStateList result = null;
        final LinkedList<ScriptStateListSupport> scriptStateList = new LinkedList<>();
        final String trimLine = line.trim();
        if (StringUtils.isNotEmpty(trimLine)) {
            if (trimLine.equalsIgnoreCase(QUIT)) {
                result = new OperationStateList(OperationState.SUCCESS);
                result.quitRequested();
            } else if (trimLine.startsWith("!")) {
                runShellCommand(trimLine);
                result = new OperationStateList(OperationState.SUCCESS);
            } else if (trimLine.startsWith(VmbkCommandLine.ECHO)) {
                IoFunction.println(trimLine.substring(VmbkCommandLine.ECHO.length()));
                result = new OperationStateList(OperationState.SUCCESS);
            } else if (trimLine.startsWith("&")) {
                result = runScript(trimLine.substring("&".length()), scriptStateList);
            } else if (trimLine.startsWith(VmbkCommandLine.CALL)) {
                result = runScript(trimLine.substring(VmbkCommandLine.CALL.length()), scriptStateList);
            } else if (trimLine.startsWith("#")) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Comment: ".concat(trimLine.substring("#".length())));
                }
                result = new OperationStateList(OperationState.SUCCESS);
            } else if (trimLine.startsWith(VmbkCommandLine.REM)) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Comment: ".concat(trimLine.substring(VmbkCommandLine.REM.length())));
                }
                result = new OperationStateList(OperationState.SUCCESS);
            } else {
                final ArrayList<String> lineArgs = lineArgsBuilder(trimLine);
                if (!lineArgs.isEmpty()) {
                    this.cmdLine.parse(lineArgs);
                    result = dispatch(this.vmbk, this.cmdLine);
                }
            }
        } else {
            result = new OperationStateList(OperationState.SUCCESS);
        }
        return result;
    }

    public OptionParser configureParser() {
        final OptionParser result = new OptionParser(true);

        result.accepts(App.INTERACTIVE, "Run interactive");

        result.accepts(App.VERSION, "Show Version.");
        final OptionSpecBuilder optionFile = result.accepts(COMMAND_FILE, "Run the script.");
        optionFile.withRequiredArg().ofType(File.class);
        result.accepts(App.STDIN, "Input form stdin.");

        final OptionSpecBuilder optionHelp = result.accepts(App.HELP, "Help");
        final OptionSpecBuilder optionConfigFile = result.accepts(App.CONFIG_FILE, "Config.property file path");
        optionConfigFile.withRequiredArg().ofType(File.class);

        optionHelp.forHelp();
        return result;
    }

    public OperationStateList dispatch(final Vmbk vmbk, final VmbkCommandLine cmdLine)
            throws NoCommandException, ParsingException, JVixException, CoreResultActionException, SafekeepingException,
            SafekeepingConnectionException {
        final ICommandInteractive info = cmdLine.dispatch();
        if (info == null) {
            throw new NoCommandException(cmdLine.getCommand());
        }
        return info.action(vmbk);
    }

    private RegexCompleter getRegexCompleter() {
        final Map<String, StringsCompleter> comp = new HashMap<>();
        StringBuilder regExString = new StringBuilder();
        for (final ICommandInteractive s : this.cmdLine.getCommandFunctions().values()) {
            regExString.append(s.getRegexCompleter(comp));
        }
        return new Completers.RegexCompleter(regExString.substring(1), comp::get);
    }

    public Vmbk initialize(final File configFile, final IoFunctionInterface ioManager) {
        final Vmbk newVmbkInstance = new Vmbk();
        try {
            if (configFile == null) {
                this.configPropertyPath = CoreGlobalSettings.getDefaulConfigPropertiesFile();
            } else {
                this.configPropertyPath = configFile.getPath();
            }
            IoFunction.setFunction(ioManager);
            this.cmdLine = new VmbkCommandLine();
            final Boolean configReturnStatus = newVmbkInstance.configure(this.configPropertyPath);
            CmdGlobalSettings.initTargets();
            if (Boolean.FALSE.equals(configReturnStatus)) {
                ConsoleWrapper.console
                        .println("There is a problem with the configuration. Manual reconfiguration enforced");

            } else {
                newVmbkInstance.initialize(DEFAULT_NUMBER_OF_CONCURRENTS_FCO_THREADS, DEFAULT_NUMBER_OF_VDDK_THREADS,
                        DEFAULT_NUMBER_OF_CONCURRENTS_ARCHIVE_THREADS);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            ConsoleWrapper.console.println("Log setting initialiation failed.");

        }
        return newVmbkInstance;
    }

    private int interactive() throws IOException {
        final int result = 0;
        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.builder().system(true).signalHandler(VmbkSignalHandler.VBSH).build();

            final LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).completer(getRegexCompleter())
                    .build();

            this.ioManager.setInteractive(true);

            terminal.writer().println("Interactive Mode ");
            terminal.writer().println("<help> for help  - <quit> to leave");
            if (StringUtils.isEmpty(CmdGlobalSettings.getSsoServer())) {
                terminal.writer().println();
                terminal.writer().println("Safekeeping-cmd is not configured");
                terminal.writer().println("Use <configure> to setup");

            }
            interactiveCommandFlow(terminal, lineReader);

        } finally {
            if (terminal != null) {
                terminal.writer().println();
                terminal.writer().println("Closing connection...");
            }
            this.vmbk.close();
            if (terminal != null) {
                terminal.writer().println("Bye. ");
                terminal.close();
            }
        }
        return result;

    }

    private void interactiveCommandFlow(final Terminal terminal, final LineReader lineReader) {
        final String prompt = CmdGlobalSettings.getInteractivePrompt() + " ";
        while (true) {
            if (Vmbk.isAbortTriggered()) {
                Vmbk.cancelAbortRequest();
            }
            String line = null;
            try {
                line = lineReader.readLine(prompt);
                final OperationStateList cflow = commandFlow(line);
                if ((cflow != null) && cflow.isQuitRequested()) {
                    break;
                }
            } catch (final UserInterruptException e) {
                logger.log(Level.WARNING, "Interrupted!", e);

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } catch (final ParsingException | NoCommandException | EndOfFileException | NullPointerException
                    | JVixException | CoreResultActionException | SafekeepingException | SafekeepingConnectionException
                    | joptsimple.OptionException e) {
                Utility.logWarning(logger, e);
                terminal.writer().println(e.getMessage());
            }
        }
    }

    private ArrayList<String> lineArgsBuilder(final String line) {
        final ArrayList<String> result = new ArrayList<>();
        final char[] charArray = line.trim().toCharArray();
        boolean openQuote = false;
        StringBuilder lineBuilder = new StringBuilder();
        for (final char element : charArray) {
            if (element == '"') {
                openQuote = !openQuote;
                continue;
            }
            if (openQuote || (element != ' ')) {
                lineBuilder.append(element);
            } else {
                result.add(lineBuilder.toString());
                lineBuilder = new StringBuilder();
            }
        }
        result.add(lineBuilder.toString());
        return result;
    }

    public int run(final String[] args) throws IOException {
        int result = 1;
        File configFile = null;
        final LinkedList<ScriptStateListSupport> scriptStateList = new LinkedList<>();
        try {
            final OptionParser parser = configureParser();
            parser.allowsUnrecognizedOptions();

            OptionSet options = null;

            options = parser.parse(args);

            if (options.has(CONFIG_FILE)) {
                configFile = (File) options.valueOf(CONFIG_FILE);
            }
            this.ioManager = new IoFunctionImpl();
            this.vmbk = initialize(configFile, this.ioManager);

            if (this.vmbk != null) {
                if (this.vmbk.isConfigured()) {
                    if (options.has(App.HELP)) {
                        HelpCommandInteractive.introHelp();

                        parser.printHelpOn(System.out);
                        ConsoleWrapper.console.println();
                        ConsoleWrapper.console.println("#######################################");
                        ConsoleWrapper.console.println();
                        HelpCommandInteractive.showHelp();
                        result = 0;
                    } else if (options.has(App.VERSION)) {
                        result = 0;
                    } else if (options.has(App.INTERACTIVE) || (args.length == 0)) {
                        result = interactive();
                    } else if (options.has(App.STDIN)) {
                        result = batch();
                    } else if (options.has(COMMAND_FILE)) {
                        runScript((File) options.valueOf(COMMAND_FILE), scriptStateList);
                        result = 0;
                    } else {
                        try {
                            final List<String> arguments = new ArrayList<>();
                            options.nonOptionArguments().forEach(xx -> arguments.add(String.valueOf(xx)));
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
                                result = (runScript(script, scriptStateList) != null) ? 0 : 3;
                            } else {
                                logger.warning(e.toString());
                                ConsoleWrapper.console.printf("Command execution failed: %s.%n", e.getMessage());
                                result = 3;
                            }
                        }
                    }
                } else {

                    this.cmdLine.forceConfiguration();
                    final ConfigCommandInteractive info = (ConfigCommandInteractive) this.cmdLine.dispatch();
                    if (CmdGlobalSettings.manualConfiguration(info.isQuiet())) {
                        CoreGlobalSettings.write();
                        final Boolean configured = this.vmbk.configure(this.configPropertyPath);
                        if ((configured == null) || !configured) {
                            throw new SafekeepingException("Configuration issue");
                        }
                        CmdGlobalSettings.initTargets();

                    }
                }
            }

        } catch (final Exception e) {
            ConsoleWrapper.console.println(e.getMessage());
        } finally {
            if (this.vmbk != null) {
                this.vmbk.close();
            }
        }
        return result;
    }

    private int runScriptLines(BufferedReader br, List<ScriptStateListSupport> scriptStateList) throws IOException {
        String inputline = "";
        int lineNumber = 0;
        try {
            this.vmbk.getConnetionManager().connectVimConnetions();

            ConsoleWrapper.console.println("Batch Mode ");

            while ((inputline = br.readLine()) != null) {

                ++lineNumber;
                if (StringUtils.isNotEmpty(inputline)) {
                    ConsoleWrapper.console.println("###################################################");
                    ConsoleWrapper.console.printf("# Line   :\t%d", lineNumber);
                    ConsoleWrapper.console.println();
                    ConsoleWrapper.console.printf("# Command:\t%s", inputline);
                    ConsoleWrapper.console.println();
                    ConsoleWrapper.console.println("#");
                    ConsoleWrapper.console.println("###################################################");
                    ConsoleWrapper.console.println();
                    scriptStateList.add(new ScriptStateListSupport(lineNumber, inputline, commandFlow(inputline)));
                    ConsoleWrapper.console.println();
                }
            }
        } catch (final EndOfFileException e) {
            if (logger.isLoggable(Level.FINE)) {
                IoFunction.showWarning(logger, e);
            }
        } catch (final NoCommandException | CoreResultActionException | JVixException | SafekeepingException
                | SafekeepingConnectionException | ParsingException e) {
            scriptStateList.add(new ScriptStateListSupport(lineNumber, inputline, OperationState.FAILED,
                    IoFunction.showWarning(logger, e)));
        }
        return lineNumber;
    }

    /**
     * Run a script in batch mode
     *
     * @param scriptFile file to execute
     * @return Number of line executed
     * @throws FileNotFoundException
     * @throws IOException
     */
    private OperationStateList runScript(final File scriptFile,
            final LinkedList<ScriptStateListSupport> scriptStateList) {
        OperationStateList result = null;
        scriptStateList.clear();
        if (scriptFile.exists() && scriptFile.canRead()) {
            boolean interactiveMode = false;
            if (this.ioManager.isInteractive()) {
                interactiveMode = true;
                this.ioManager.setInteractive(false);
            }
            try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
                runScriptLines(br, scriptStateList);
            } catch (final FileNotFoundException e) {
                IoFunction.showWarning(logger, e);
                ConsoleWrapper.console.printf("File %s not found.%n", scriptFile);

            } catch (final IOException e) {
                IoFunction.showWarning(logger, e);
                result = new OperationStateList(OperationState.FAILED,
                        IoFunction.showWarning(logger, "Command execution failed: %s.\n", e.getMessage()));

            }

            if (interactiveMode) {
                this.ioManager.setInteractive(true);
                for (final ScriptStateListSupport r : scriptStateList) {
                    for (final OperationState s : r.opStateList.getList()) {
                        if (s != OperationState.SUCCESS) {
                            ConsoleWrapper.console.printf("(%d) %s : [%s] %s", r.lineNumber, r.command, s.toString(),
                                    r.opStateList.getReason());
                        }
                    }
                }

                ConsoleWrapper.console.println("Interactive Mode ");
            }
            result = new OperationStateList(OperationState.SUCCESS);
        } else {
            result = new OperationStateList(OperationState.FAILED,
                    IoFunction.showWarning(logger, "File %s cannot be loaded", scriptFile.toPath().toString()));
        }
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
    private OperationStateList runScript(final String scriptFileName,
            final LinkedList<ScriptStateListSupport> scriptStateList) {
        String fileName = scriptFileName.trim();
        if (!StringUtils.startsWithAny(fileName, new String[] { "/", "." })) {
            fileName = CmdGlobalSettings.getScriptFolder() + File.separator + fileName;
        }
        if (!StringUtils.endsWith(fileName, ".vmbk")) {
            fileName = fileName + ".vmbk";
        }
        return runScript(new File(fileName), scriptStateList);
    }

    /**
     * Run as shell command in windows or unix
     *
     * @param line Command to execute
     */
    private OperationStateList runShellCommand(final String line) {
        final ArrayList<String> cmdShell = new ArrayList<>();

        switch (GuestOsUtils.getOS()) {
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

        }
        OperationStateList result = null;
        if (!cmdShell.isEmpty()) {
            cmdShell.add(line.substring(1).trim());
            final ExecResult resultCmd = Utility.execCmd(cmdShell.toArray(new String[0]));
            if (logger.isLoggable(Level.INFO)) {
                logger.info(resultCmd.getOutput());
            }
            if (resultCmd.isSuccess()) {
                result = new OperationStateList(OperationState.SUCCESS);
            } else {
                result = new OperationStateList(OperationState.FAILED, resultCmd.getError());
            }
        } else {
            result = new OperationStateList(OperationState.FAILED,
                    IoFunction.showWarning(logger, "Unsupported OS:%s", GuestOsUtils.getOS()));
        }
        return result;
    }

}
