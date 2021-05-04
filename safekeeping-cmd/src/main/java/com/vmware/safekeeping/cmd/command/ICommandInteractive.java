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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.jline.reader.impl.completer.StringsCompleter;

import com.vmware.jvix.JVixException;
import com.vmware.safekeeping.cmd.support.ParsingException;
import com.vmware.safekeeping.core.command.ICommand;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.info.FcoTypeSearch;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingConnectionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.type.FcoTarget;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public interface ICommandInteractive extends ICommand {
    Pattern IP4PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    Pattern UUIDPATTERN = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    OperationStateList action(Vmbk vmbk)
            throws JVixException, CoreResultActionException, SafekeepingException, SafekeepingConnectionException;

    Entry<String, ICommandInteractive> configureParser();

    default Map<? extends String, FcoTarget> getAnySpecifiedTargets(final Iterable<String> targetList) {
        final Map<String, FcoTarget> result = getVmTargets(targetList);
        result.putAll(getIvdTargets(targetList));
        result.putAll(getVappTargets(targetList));
        result.putAll(getK8sTargets(targetList));
        result.putAll(getTagTargets(targetList));
        return result;
    }

    String getCommandName();

    default Map<String, FcoTarget> getIvdTargets(final Iterable<String> targetList) {
        final LinkedHashMap<String, FcoTarget> result = new LinkedHashMap<>();
        for (String target : targetList) {
            if (target.startsWith("ivd:")) {
                target = target.substring("ivd:".length());

                FcoTypeSearch typeVm = FcoTypeSearch.IVD_NAME;
                if (UUIDPATTERN.matcher(target).matches()) {
                    typeVm = FcoTypeSearch.IVD_UUID;
                }

                FcoTarget fcoT = new FcoTarget(target, typeVm);
                result.put(target, fcoT);
            }

        }
        return result;
    }

    default Map<String, FcoTarget> getK8sTargets(final Iterable<String> targetList) {
        final LinkedHashMap<String, FcoTarget> result = new LinkedHashMap<>();
        for (String target : targetList) {
            if (target.startsWith("k8s:")) {
                target = target.substring("k8s:".length());
                FcoTypeSearch typeVm = FcoTypeSearch.K8S_NAME;
                if (UUIDPATTERN.matcher(target).matches()) {
                    typeVm = FcoTypeSearch.K8S_UUID;
                }
                FcoTarget fcoT = new FcoTarget(target, typeVm);
                result.put(target, fcoT);
            }

        }
        return result;
    }

    /**
     * @return
     */
    String getPrologo();

    String getRegexCompleter(final Map<String, StringsCompleter> comp);

    default Map<String, FcoTarget> getTagTargets(final Iterable<String> targetList) {
        final LinkedHashMap<String, FcoTarget> result = new LinkedHashMap<>();
        for (String target : targetList) {
            if (target.startsWith("tag:")) {
                target = target.substring("tag:".length());
                FcoTarget fcoT = new FcoTarget(target, FcoTypeSearch.TAG);
                result.put(target, fcoT);
            }

        }
        return result;
    }

    /**
     * @return the targetList
     */
    default List<String> getTargetList() {
        return Collections.emptyList();
    }

    default Map<String, FcoTarget> getVappTargets(final Iterable<String> targetList) {
        final LinkedHashMap<String, FcoTarget> result = new LinkedHashMap<>();
        for (String target : targetList) {
            if (target.startsWith("vapp:")) {
                target = target.substring("vapp:".length());
                final FcoTypeSearch typeVm;
                if (target.startsWith("resgroup-")) {
                    typeVm = FcoTypeSearch.VAPP_MOREF;
                } else if (UUIDPATTERN.matcher(target).matches()) {
                    typeVm = FcoTypeSearch.VAPP_UUID;
                } else {
                    typeVm = FcoTypeSearch.VAPP_NAME;
                }
                FcoTarget fcoT = new FcoTarget(target, typeVm);
                result.put(target, fcoT);
            } else {
                if (target.startsWith("resgroup-")) {
                    FcoTarget fcoT = new FcoTarget(target, FcoTypeSearch.VAPP_MOREF);
                    result.put(target, fcoT);
                }
            }

        }
        return result;
    }

    default Map<String, FcoTarget> getVmTargets(final Iterable<String> targetList) {
        final LinkedHashMap<String, FcoTarget> result = new LinkedHashMap<>();
        for (String target : targetList) {
            if (target.startsWith("vm:")) {
                target = target.substring("vm:".length());
                final FcoTypeSearch typeVm;
                if (UUIDPATTERN.matcher(target).matches()) {
                    typeVm = FcoTypeSearch.VM_UUID;

                } else if (IP4PATTERN.matcher(target).matches()) {
                    typeVm = FcoTypeSearch.VM_IP;

                } else if (target.startsWith("vm-")) {
                    typeVm = FcoTypeSearch.VM_MOREF;
                } else {
                    typeVm = FcoTypeSearch.VM_NAME;
                }
                FcoTarget fcoT = new FcoTarget(target, typeVm);
                result.put(target, fcoT);
            } else {
                if (target.startsWith("vm-")) {
                    FcoTarget fcoT = new FcoTarget(target, FcoTypeSearch.VM_MOREF);
                    result.put(target, fcoT);

                }
            }
        }
        return result;
    }

    String helpEntities();

    String helpExample();

    String helpSummary();

    void initialize();

    boolean isHelp();

    void parse(final String[] arguments) throws ParsingException;

    default OptionSet parseArguments(final OptionParser parser, final String[] arguments,
            final CoreBasicCommandOptions commandOptions) {
        final OptionSet options = parser.parse(arguments);
        getTargetList().clear();
        options.nonOptionArguments().forEach(xx -> getTargetList().add(String.valueOf(xx)));

        commandOptions.getTargetFcoList().putAll(getAnySpecifiedTargets(getTargetList()));
        return options;
    }

    /**
     *
     * @param args
     * @return
     */
    default StringsCompleter stringsCompleter(final String... args) {
        final String[] mArgs = new String[args.length];
        if (args.length == 1) {
            mArgs[0] = args[0];
        } else {
            for (int i = 0; i < args.length; i++) {
                mArgs[i] = "-" + args[i];
            }
        }
        return new StringsCompleter(mArgs);
    }

}
