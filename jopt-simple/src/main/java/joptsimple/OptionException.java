/*
 The MIT License

 Copyright (c) 2004-2016 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package joptsimple;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static joptsimple.internal.Messages.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Thrown when a problem occurs during option parsing.
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
public abstract class OptionException extends RuntimeException {
	private static final long serialVersionUID = -1L;

	static OptionException unrecognizedOption(String option) {
		return new UnrecognizedOptionException(option);
	}

	private final List<String> options = new ArrayList<>();

	protected OptionException(Collection<? extends OptionSpec<?>> options) {
		this.options.addAll(specsToStrings(options));
	}

	protected OptionException(Collection<? extends OptionSpec<?>> options, Throwable cause) {
		super(cause);
		this.options.addAll(specsToStrings(options));
	}

	protected OptionException(List<String> options) {
		this.options.addAll(options);
	}

	private String formattedMessage(Locale locale) {
		return message(locale, "joptsimple.ExceptionMessages", getClass(), "message", messageArguments());
	}

	@Override
	public final String getLocalizedMessage() {
		return localizedMessage(Locale.getDefault());
	}

	@Override
	public final String getMessage() {
		return localizedMessage(Locale.ENGLISH);
	}

	final String localizedMessage(Locale locale) {
		return formattedMessage(locale);
	}

	abstract Object[] messageArguments();

	protected final String multipleOptionString() {
		final StringBuilder buffer = new StringBuilder("[");

		final Set<String> asSet = new LinkedHashSet<>(this.options);
		for (final Iterator<String> iter = asSet.iterator(); iter.hasNext();) {
			buffer.append(singleOptionString(iter.next()));
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}

		buffer.append(']');

		return buffer.toString();
	}

	/**
	 * Gives the option being considered when the exception was created.
	 *
	 * @return the option being considered when the exception was created
	 */
	public List<String> options() {
		return unmodifiableList(this.options);
	}

	protected final String singleOptionString() {
		return singleOptionString(this.options.get(0));
	}

	protected final String singleOptionString(String option) {
		return option;
	}

	private List<String> specsToStrings(Collection<? extends OptionSpec<?>> options) {
		final List<String> strings = new ArrayList<>();
		for (final OptionSpec<?> each : options) {
			strings.add(specToString(each));
		}
		return strings;
	}

	private String specToString(OptionSpec<?> option) {
		return option.options().stream().collect(joining("/"));
	}
}
