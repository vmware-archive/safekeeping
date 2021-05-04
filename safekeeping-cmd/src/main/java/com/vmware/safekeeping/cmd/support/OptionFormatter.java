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
package com.vmware.safekeeping.cmd.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import joptsimple.AbstractOptionSpec;
import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionSpec;

class OptionFormatter implements HelpFormatter {
	static OptionFormatter newOptionFormatter(final VmbkParser parser) {
		final OptionFormatter of = new OptionFormatter(parser);
		parser.formatHelpWith(of);
		return of;
	}

	private VmbkParser parser = null;

	private OptionFormatter(final VmbkParser parser) {
		this.parser = parser;
	}

	private String addArgument(final AbstractOptionSpec<?> t, final String str1) {
		final StringBuilder o = new StringBuilder();
		o.append(' ');
		o.append('[');
		if (!t.representsNonOptions()) {
			o.append("-");
		}
		o.append(str1);
		if (t.acceptsArguments()) {
			o.append(" ");
			if (t.requiresArgument()) {
				o.append("<");
			} else {
				o.append("[");
			}
			o.append(t.argumentDescription());
			if (t.requiresArgument()) {
				o.append(">");
			} else {
				o.append("]");
			}
		}
		o.append(']');
		return o.toString();

	}

	private boolean findArgumentMatch(final Map<List<String>, Set<OptionSpec<?>>> availableUnless,
			final LinkedList<AbstractOptionSpec<?>> argumentConstructor, final AbstractOptionSpec<?> t) {
		for (final AbstractOptionSpec<?> p : argumentConstructor) {
			if (availableUnless.containsKey(p.options())) {
				for (final OptionSpec<?> q : availableUnless.get(p.options())) {
					if (q.options().equals(t.options())) {
						return true;
					}
				}
			}
		}
		if (availableUnless.containsKey(t.options())) {
			for (final OptionSpec<?> q : availableUnless.get(t.options())) {
				for (final AbstractOptionSpec<?> p : argumentConstructor) {
					if (q.options().equals(p.options())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public String format(final Map<String, ? extends OptionDescriptor> options) {
		final StringBuilder sb = new StringBuilder();

		final LinkedList<LinkedList<AbstractOptionSpec<?>>> arguments = new LinkedList<>();
		if (this.parser != null) {
			sb.append("Command: ");
			sb.append(this.parser.getProgr());
			sb.append("  -  ");
			if (this.parser.getDescription() != null) {
				sb.append(this.parser.getDescription());
			}
			sb.append('\n');
			if (this.parser.getProlog() != null) {
				sb.append(this.parser.getProlog());
			}
			sb.append('\n');
			sb.append("Usage:\n");

			sb.append(usageFor(this.parser, arguments));
			sb.append('\n');
			sb.append("Arguments:\n");

			for (final OptionDescriptor each : options.values()) {

				sb.append(lineFor(each));
			}
			sb.append('\n');

			if (this.parser.getEpilog() != null) {
				sb.append(this.parser.getEpilog());
				sb.append('\n');
			}

			if (this.parser.getExamples() != null) {
				sb.append("Examples:\n\n");
				sb.append(this.parser.getExamples());
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	private String lineFor(final OptionDescriptor d) {
		final StringBuilder line = new StringBuilder();
		if (StringUtils.isNotEmpty(d.description())) {
			final StringBuilder o = new StringBuilder();
			o.append("  ");
			for (final String str : d.options()) {
				if (!d.representsNonOptions()) {
					o.append("-");
				}
				o.append(str);
				if (d.acceptsArguments()) {
					o.append(" ");
					if (d.requiresArgument()) {
						o.append("<");
					} else {
						o.append("[");
					}
					o.append(d.argumentDescription());
					if (d.requiresArgument()) {
						o.append(">");
					} else {
						o.append("]");
					}
				}
			}

			line.append(String.format("%-50s", o.toString()));
			boolean first = true;
			for (final String l : rewrap(d.description())) {
				if (first) {
					first = false;
				} else {
					line.append(System.lineSeparator());
					line.append(String.format("%-30s", ""));
				}
				if (!l.trim().isEmpty()) {
					line.append(l);
				}
			}

			line.append(System.lineSeparator());
		}
		return line.toString();
	}

	private Collection<String> rewrap(final String lines) {
		final Collection<String> result = new ArrayList<>();
		final String[] words = lines.split("[ \n\r]");
		String line = "";
		int cols = 0;
		for (final String w : words) {
			if ((cols + w.length()) > 50) {
				result.add(line);
				line = w + " ";
				cols = w.length();
			} else {
				cols += w.length();
				line += w + " ";
			}
		}
		result.add(line);
		return result;
	}

	private String usageFor(final VmbkParser parser,
			final LinkedList<LinkedList<AbstractOptionSpec<?>>> argumentsConstructor) {
		final StringBuilder o = new StringBuilder();
		boolean flatHelp = (parser.getMainOptions().size() == 0);

		ll: for (final String primaryKey : parser.recognizedOptions().keySet()) {
			final AbstractOptionSpec<?> subCommand = (AbstractOptionSpec<?>) parser.recognizedOptions().get(primaryKey);

			if (parser.getMainOptions().containsKey(subCommand.options()) || flatHelp) {

				o.append('\t');
				o.append(parser.getProgr());

				for (final String str : subCommand.options()) {
					if (!flatHelp) {
						o.append("  -");
						o.append(str);
						if (subCommand.acceptsArguments()) {
							o.append(" ");
							if (subCommand.requiresArgument()) {
								o.append("<");
							} else {
								o.append("[");
							}
							o.append(subCommand.argumentDescription());
							if (subCommand.requiresArgument()) {
								o.append(">");
							} else {
								o.append("]");
							}
						}
					}
					final LinkedList<AbstractOptionSpec<?>> argumentConstructor = new LinkedList<>();
					argumentConstructor.add(subCommand);

					final LinkedList<AbstractOptionSpec<?>> newOption = new LinkedList<>();
					for (final String key : parser.recognizedOptions().keySet()) {
						final AbstractOptionSpec<?> t = (AbstractOptionSpec<?>) parser.recognizedOptions().get(key);
						if (!parser.getMainOptions().containsKey(t.options())) {
							newOption.add(t);
						}

					}
					if ((parser.getMainOptions().size() == 1) && !flatHelp) {
						flatHelp = true;
					} else {
						flatHelp = false;
					}

					int index = 0;
					boolean removedInThisCycle = false;
					while (true) {
						final AbstractOptionSpec<?> t = newOption.get(index);
						boolean remove = false;
						for (final String str1 : t.options()) {
							if (parser.getRequiredIf().containsKey(t.options())) {
								if (findArgumentMatch(parser.getAvailableIf(), argumentConstructor, t)) {
									o.append(addArgument(t, str1));
									argumentConstructor.add(t);
									remove = true;

								}
							}
							if (parser.getAvailableIf().containsKey(t.options())) {
								if (findArgumentMatch(parser.getAvailableIf(), argumentConstructor, t)) {
									o.append(addArgument(t, str1));
									argumentConstructor.add(t);
									remove = true;
								}
							}
							if (parser.getRequiredUnless().containsKey(t.options())) {
								if (!findArgumentMatch(parser.getRequiredUnless(), argumentConstructor, t)) {
									o.append(addArgument(t, str1));
									argumentConstructor.add(t);
									remove = true;
								}
							}
							if (parser.getAvailableUnless().containsKey(t.options())) {

								if (!findArgumentMatch(parser.getAvailableUnless(), argumentConstructor, t)) {
									o.append(addArgument(t, str1));
									argumentConstructor.add(t);
									remove = true;
								}

							}

						}
						if (remove) {
							newOption.remove(index);
							removedInThisCycle = true;
						} else {
							++index;
						}
						if (newOption.size() == 0) {
							break;
						}
						if (index >= newOption.size()) {
							if (!removedInThisCycle) {
								break;
							}
							index = 0;
							removedInThisCycle = false;
						}

					}
					argumentsConstructor.add(argumentConstructor);

				}
				o.append(System.lineSeparator());
				if (flatHelp) {

					continue ll;
				}
			}

		}
		return o.toString();
	}
}
