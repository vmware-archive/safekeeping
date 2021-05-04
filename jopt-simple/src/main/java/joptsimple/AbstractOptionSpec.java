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

import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static joptsimple.internal.Strings.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import joptsimple.internal.Reflection;
import joptsimple.internal.ReflectionException;

/**
 * @param <V> represents the type of the arguments this option accepts
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
public abstract class AbstractOptionSpec<V> implements OptionSpec<V>, OptionDescriptor {
	private final List<String> options = new ArrayList<>();
	private final String description;
	private boolean forHelp;

	AbstractOptionSpec(List<String> options, String description) {
		arrangeOptions(options);

		this.description = description;
	}

	AbstractOptionSpec(String option) {
		this(singletonList(option), EMPTY);
	}

	protected String argumentTypeIndicatorFrom(ValueConverter<V> converter) {
		if (converter == null) {
			return null;
		}

		final String pattern = converter.valuePattern();
		return pattern == null ? converter.valueType().getName() : pattern;
	}

	private void arrangeOptions(List<String> unarranged) {
		if (unarranged.size() == 1) {
			this.options.addAll(unarranged);
			return;
		}

		final List<String> shortOptions = new ArrayList<>();
		final List<String> longOptions = new ArrayList<>();

		for (final String each : unarranged) {
			if (each.length() == 1) {
				shortOptions.add(each);
			} else {
				longOptions.add(each);
			}
		}

		sort(shortOptions);
		sort(longOptions);

		this.options.addAll(shortOptions);
		this.options.addAll(longOptions);
	}

	protected abstract V convert(String argument);

	protected V convertWith(ValueConverter<V> converter, String argument) {
		try {
			return Reflection.convertWith(converter, argument);
		} catch (ReflectionException | ValueConversionException ex) {
			throw new OptionArgumentConversionException(this, argument, ex);
		}
	}

	@Override
	public String description() {
		return this.description;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof AbstractOptionSpec<?>)) {
			return false;
		}

		final AbstractOptionSpec<?> other = (AbstractOptionSpec<?>) that;
		return this.options.equals(other.options);
	}

	public final AbstractOptionSpec<V> forHelp() {
		this.forHelp = true;
		return this;
	}

	abstract void handleOption(OptionParser parser, ArgumentList arguments, OptionSet detectedOptions,
			String detectedArgument);

	@Override
	public int hashCode() {
		return this.options.hashCode();
	}

	@Override
	public final boolean isForHelp() {
		return this.forHelp;
	}

	@Override
	public final List<String> options() {
		return unmodifiableList(this.options);
	}

	@Override
	public boolean representsNonOptions() {
		return false;
	}

	@Override
	public String toString() {
		return this.options.toString();
	}

	@Override
	public final V value(OptionSet detectedOptions) {
		return detectedOptions.valueOf(this);
	}

	@Override
	public final Optional<V> valueOptional(OptionSet detectedOptions) {
		return Optional.ofNullable(value(detectedOptions));
	}

	@Override
	public final List<V> values(OptionSet detectedOptions) {
		return detectedOptions.valuesOf(this);
	}
}
