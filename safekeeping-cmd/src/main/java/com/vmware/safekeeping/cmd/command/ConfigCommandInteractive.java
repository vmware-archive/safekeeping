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
package com.vmware.safekeeping.cmd.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.cmd.settings.CmdGlobalSettings;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.cmd.support.VmbkParser;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.core.SJvddk;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class ConfigCommandInteractive extends AbstractSimpleCommandInteractive {

    private static final String OPTION_HELP = "help";
    private static final String OPTION_QUIET = "quiet";
    private static final String OPTION_SAVE = "save";
    private static final String OPTION_RELOAD = "reload";
    private static final String OPTION_VDDK_VERSION = "vddk";
    private static final Logger logger = Logger.getLogger(ConfigCommandInteractive.class.getName());

    public static final String CONFIGURE = "configure";

    private static final String COMMAND_DESCRIPTION = "Configure " + SafekeepingVersion.PRODUCT_NAME
            + " interactively.";

    static String helpGlobalSummary() {
        final ConfigCommandInteractive info = new ConfigCommandInteractive();
        return String.format("%-20s\t%s", info.getCommandName(), info.helpSummary());
    }

    private VmbkParser parser;

    private boolean help;
    private boolean quiet;
    private boolean save;
    private boolean reload;
    private String vddkVersion;

    public ConfigCommandInteractive() {
        initialize();
    }

    @Override
    public OperationStateList action(final Vmbk vmbk) {
        OperationStateList result = null;
        if (isHelp()) {
            try {
                this.parser.printHelpOn(System.out);
                result = new OperationStateList(OperationState.SUCCESS);
            } catch (final IOException e) {
                Utility.logWarning(logger, e);
                result = new OperationStateList(OperationState.FAILED);
            }
        } else {
            try {
                if (StringUtils.isNotEmpty(this.vddkVersion)) {
                    if (SafekeepingVersion.validate(this.vddkVersion)) {
                        if (!SJvddk.isInitialized()) {
                            if (this.save) {
                                CoreGlobalSettings.setVddkVersionPermanently(this.vddkVersion);
                            } else {
                                CoreGlobalSettings.setVddkVersion(this.vddkVersion);
                            }
                            SafekeepingVersion.initialize(CoreGlobalSettings.getVddkVersion());
                            IoFunction.showInfo(logger, "Vddk version changed to %s", this.vddkVersion);
                            result = new OperationStateList(OperationState.SUCCESS);
                        } else {
                            if (this.save && SJvddk.isInitialized()) {
                                CoreGlobalSettings.setVddkVersionPermanently(this.vddkVersion);
                                IoFunction.showInfo(logger, "Vddk version changed to %s", this.vddkVersion);
                                IoFunction.showWarning(logger, "Restart is required");
                            } else {
                                result = new OperationStateList(OperationState.SKIPPED, IoFunction.showWarning(logger,
                                        "save option is required when VDDK is already initialized"));
                            }
                        }

                    } else {
                        result = new OperationStateList(OperationState.FAILED,
                                IoFunction.showWarning(logger, " %s is not a valid version", this.vddkVersion));
                    }
                } else if (this.reload) {
                    CmdGlobalSettings.reloadConfig();
                    final ITarget repositoryTarget = CmdGlobalSettings.getRepositoryTarget();
                    if (repositoryTarget.isEnable()) {
                        repositoryTarget.open();
                        result = new OperationStateList(OperationState.SUCCESS);
                    } else {
                        result = new OperationStateList(OperationState.FAILED,
                                String.format(
                                        "Default repository \"%s\" is not active. Please configure the repository.",
                                        repositoryTarget.getTargetType()));
                    }

                } else {
                    if (CmdGlobalSettings.manualConfiguration(this.quiet)) {
                        result = new OperationStateList(OperationState.SUCCESS);
                    } else {
                        result = new OperationStateList(OperationState.FAILED);
                    }
                }
            } catch (final IOException | JVixException | URISyntaxException | SafekeepingException
                    | SafekeepingConnectionException e) {
                Utility.logWarning(logger, e);
                result = new OperationStateList(OperationState.FAILED);
            }
        }
        return result;
    }

    @Override
    public Entry<String, ICommandInteractive> configureParser() {
        this.parser = VmbkParser.newVmbkParser(this);
        final OptionSpecBuilder optionHelp = this.parser.accepts(OPTION_HELP, "Help");
        final OptionSpecBuilder optionVddkVersion = this.parser.accepts(OPTION_VDDK_VERSION, "Set Vddk Version");
        optionVddkVersion.withRequiredArg().ofType(String.class).describedAs("version");

        this.parser.accepts(OPTION_SAVE, "Save changes").availableIf(OPTION_VDDK_VERSION);
        final OptionSpecBuilder optionReload = this.parser.accepts(OPTION_RELOAD, "Reload the configuration file");
        this.parser.accepts(OPTION_QUIET, "No confirmation is asked.").availableUnless(OPTION_HELP,
                OPTION_VDDK_VERSION);
        this.parser.mainOptions(optionHelp, optionVddkVersion, optionReload);
        optionHelp.forHelp();
        return new AbstractMap.SimpleEntry<>(getCommandName(), this);

    }

    @Override
    public String getCommandName() {
        return CONFIGURE;
    }

    @Override
    public String getPrologo() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRegexCompleter(final Map<String, StringsCompleter> comp) {

        comp.put("Q1", stringsCompleter(CONFIGURE));
        comp.put("Q11", stringsCompleter(OPTION_QUIET));
        comp.put("Q12", stringsCompleter(OPTION_VDDK_VERSION, OPTION_SAVE));
        comp.put("Q99", stringsCompleter(OPTION_HELP));
        return "|Q1 Q11?|Q1 Q12*|Q1 Q99?";
    }

    @Override
    public String helpEntities() {
        return "";
    }

    @Override
    public String helpExample() {

        return null;
    }

    @Override
    public String helpSummary() {
        return ConfigCommandInteractive.COMMAND_DESCRIPTION;
    }

    @Override
    public void initialize() {

        setHelp(false);
        this.quiet = false;
        this.save = false;
        this.vddkVersion = null;
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    /**
     * @return the quiet
     */
    public boolean isQuiet() {
        return this.quiet;
    }

    @Override
    public void parse(final String[] arguments) throws ParsingException {
        final OptionSet options = this.parser.parse(arguments);

        setHelp(options.has(OPTION_HELP));
        if (options.has(OPTION_VDDK_VERSION)) {
            this.vddkVersion = options.valueOf(OPTION_VDDK_VERSION).toString();
        }

        if (options.has(OPTION_SAVE)) {
            this.save = true;
        }
        if (options.has(OPTION_QUIET)) {
            this.quiet = true;
        }
        if (options.has(OPTION_RELOAD)) {
            this.reload = true;
        }
    }

    @Override
    public void setHelp(final boolean help) {
        this.help = help;
    }

    /**
     * @param quiet the quiet to set
     */
    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }
}
