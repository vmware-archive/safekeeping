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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Representation of a group of detected command line options, their arguments,
 * and non-option arguments.
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
public class OptionSet {
	private static Map<String, List<?>> defaultValues(Map<String, AbstractOptionSpec<?>> recognizedSpecs) {
		final Map<String, List<?>> defaults = new HashMap<>();
		for (final Map.Entry<String, AbstractOptionSpec<?>> each : recognizedSpecs.entrySet()) {
			defaults.put(each.getKey(), each.getValue().defaultValues());
		}
		return defaults;
	}

	private final List<OptionSpec<?>> detectedSpecs;
	private final Map<String, AbstractOptionSpec<?>> detectedOptions;
	private final Map<AbstractOptionSpec<?>, List<String>> optionsToArguments;
	private final Map<String, AbstractOptionSpec<?>> recognizedSpecs;

	private final Map<String, List<?>> defaultValues;

	/*
	 * Package-private because clients don't create these.
	 */
	OptionSet(Map<String, AbstractOptionSpec<?>> recognizedSpecs) {
		this.detectedSpecs = new ArrayList<>();
		this.detectedOptions = new HashMap<>();
		this.optionsToArguments = new IdentityHashMap<>();
		this.defaultValues = defaultValues(recognizedSpecs);
		this.recognizedSpecs = recognizedSpecs;
	}

	void add(AbstractOptionSpec<?> spec) {
		addWithArgument(spec, null);
	}

	void addWithArgument(AbstractOptionSpec<?> spec, String argument) {
		this.detectedSpecs.add(spec);

		for (final String each : spec.options()) {
			this.detectedOptions.put(each, spec);
		}

		List<String> optionArguments = this.optionsToArguments.get(spec);

		if (optionArguments == null) {
			optionArguments = new ArrayList<>();
			this.optionsToArguments.put(spec, optionArguments);
		}

		if (argument != null) {
			optionArguments.add(argument);
		}
	}

	/**
	 * Gives all declared options as a map of string to {@linkplain OptionSpec}.
	 *
	 * @return the declared options as a map
	 */
	public Map<OptionSpec<?>, List<?>> asMap() {
		final Map<OptionSpec<?>, List<?>> map = new HashMap<>();

		for (final AbstractOptionSpec<?> spec : this.recognizedSpecs.values()) {
			if (!spec.representsNonOptions()) {
				map.put(spec, valuesOf(spec));
			}
		}

		return unmodifiableMap(map);
	}

	private <V> List<V> defaultValueFor(OptionSpec<V> option) {
		return defaultValuesFor(option.options().iterator().next());
	}

	@SuppressWarnings("unchecked")
	private <V> List<V> defaultValuesFor(String option) {
		if (this.defaultValues.containsKey(option)) {
			return unmodifiableList((List<V>) this.defaultValues.get(option));
		}

		return emptyList();
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if ((that == null) || !getClass().equals(that.getClass())) {
			return false;
		}

		final OptionSet other = (OptionSet) that;
		final Map<AbstractOptionSpec<?>, List<String>> thisOptionsToArguments = new HashMap<>(this.optionsToArguments);
		final Map<AbstractOptionSpec<?>, List<String>> otherOptionsToArguments = new HashMap<>(
				other.optionsToArguments);
		return this.detectedOptions.equals(other.detectedOptions)
				&& thisOptionsToArguments.equals(otherOptionsToArguments);
	}

	/**
	 * Tells whether the given option was detected.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent
	 * interface methods.
	 * </p>
	 *
	 * <p>
	 * Specifying a
	 * {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[])} default
	 * argument value} for an option does not cause this method to return
	 * {@code true} if the option was not detected on the command line.
	 * </p>
	 *
	 * @param option the option to search for
	 * @return {@code true} if the option was detected
	 * @see #has(String)
	 */
	public boolean has(OptionSpec<?> option) {
		return this.optionsToArguments.containsKey(option);
	}

	/**
	 * Tells whether the given option was detected.
	 *
	 * @param option the option to search for
	 * @return {@code true} if the option was detected
	 * @see #has(OptionSpec)
	 */
	public boolean has(String option) {
		return this.detectedOptions.containsKey(option);
	}

	/**
	 * Tells whether there are any arguments associated with the given option.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent
	 * interface methods.
	 * </p>
	 *
	 * <p>
	 * Specifying a
	 * {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default
	 * argument value} for an option does not cause this method to return
	 * {@code true} if the option was not detected on the command line, or if the
	 * option can take an optional argument but did not have one on the command
	 * line.
	 * </p>
	 *
	 * @param option the option to search for
	 * @return {@code true} if the option was detected and at least one argument was
	 *         detected for the option
	 * @throws NullPointerException if {@code option} is {@code null}
	 * @see #hasArgument(String)
	 */
	public boolean hasArgument(OptionSpec<?> option) {
		requireNonNull(option);

		final List<String> values = this.optionsToArguments.get(option);
		return (values != null) && !values.isEmpty();
	}

	/**
	 * Tells whether there are any arguments associated with the given option.
	 *
	 * @param option the option to search for
	 * @return {@code true} if the option was detected and at least one argument was
	 *         detected for the option
	 * @see #hasArgument(OptionSpec)
	 */
	public boolean hasArgument(String option) {
		final AbstractOptionSpec<?> spec = this.detectedOptions.get(option);
		return (spec != null) && hasArgument(spec);
	}

	@Override
	public int hashCode() {
		final Map<AbstractOptionSpec<?>, List<String>> thisOptionsToArguments = new HashMap<>(this.optionsToArguments);
		return this.detectedOptions.hashCode() ^ thisOptionsToArguments.hashCode();
	}

	/**
	 * Tells whether any options were detected.
	 *
	 * @return {@code true} if any options were detected
	 */
	public boolean hasOptions() {
		return ((this.detectedOptions.size() != 1) || !this.detectedOptions.values().iterator().next().representsNonOptions());
	}

	/**
	 * @return the detected non-option arguments
	 */
	public List<?> nonOptionArguments() {
		final AbstractOptionSpec<?> spec = this.detectedOptions.get(NonOptionArgumentSpec.NAME);
		return valuesOf(spec);
	}

	/**
	 * Gives the set of options that were detected, in the form of
	 * {@linkplain OptionSpec}s, in the order in which the options were found on the
	 * command line.
	 *
	 * @return the set of detected command line options
	 */
	public List<OptionSpec<?>> specs() {
		final List<OptionSpec<?>> specs = new ArrayList<>(this.detectedSpecs);
		specs.removeAll(singletonList(this.detectedOptions.get(NonOptionArgumentSpec.NAME)));

		return unmodifiableList(specs);
	}

	/**
	 * Gives the set of options and non-option arguments that were detected, in the
	 * form of {@linkplain OptionSpec}s, in the order in which they were found on
	 * the command line.
	 *
	 * @return the set of detected command line options and non-option args
	 */
	public List<OptionSpec<?>> specsWithNonOptions() {
		final List<OptionSpec<?>> specs = new ArrayList<>(this.detectedSpecs);
		specs.remove(this.detectedOptions.get(NonOptionArgumentSpec.NAME));

		return unmodifiableList(specs);
	}

	/**
	 * Gives the argument associated with the given option.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent
	 * interface methods.
	 * </p>
	 *
	 * @param <V>    represents the type of the arguments the given option accepts
	 * @param option the option to search for
	 * @return the argument of the given option; {@code null} if no argument is
	 *         present, or that option was not detected
	 * @throws OptionException      if more than one argument was detected for the
	 *                              option
	 * @throws NullPointerException if {@code option} is {@code null}
	 * @throws ClassCastException   if the arguments of this option are not of the
	 *                              expected type
	 */
	public <V> V valueOf(OptionSpec<V> option) {
		requireNonNull(option);

		final List<V> values = valuesOf(option);
		switch (values.size()) {
		case 0:
			return null;
		case 1:
			return values.get(0);
		default:
			throw new MultipleArgumentsForOptionException(option);
		}
	}

	/**
	 * Gives the argument associated with the given option. If the option was given
	 * an argument type, the argument will take on that type; otherwise, it will be
	 * a {@link String}.
	 *
	 * <p>
	 * Specifying a
	 * {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default
	 * argument value} for an option will cause this method to return that default
	 * value even if the option was not detected on the command line, or if the
	 * option can take an optional argument but did not have one on the command
	 * line.
	 * </p>
	 *
	 * @param option the option to search for
	 * @return the argument of the given option; {@code null} if no argument is
	 *         present, or that option was not detected
	 * @throws NullPointerException if {@code option} is {@code null}
	 * @throws OptionException      if more than one argument was detected for the
	 *                              option
	 */
	public Object valueOf(String option) {
		requireNonNull(option);

		final AbstractOptionSpec<?> spec = this.detectedOptions.get(option);
		if (spec == null) {
			final List<?> defaults = defaultValuesFor(option);
			return defaults.isEmpty() ? null : defaults.get(0);
		}

		return valueOf(spec);
	}

	/**
	 * Gives the argument associated with the given option as an {@link Optional}.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent
	 * interface methods.
	 * </p>
	 *
	 * @param <V>    represents the type of the arguments the given option accepts
	 * @param option the option to search for
	 * @return the argument of the given option as an {@code Optional}
	 * @throws OptionException      if more than one argument was detected for the
	 *                              option
	 * @throws NullPointerException if {@code option} is {@code null}
	 * @throws ClassCastException   if the arguments of this option are not of the
	 *                              expected type
	 */
	public <V> Optional<V> valueOfOptional(OptionSpec<V> option) {
		return Optional.ofNullable(valueOf(option));
	}

	/**
	 * Gives the argument associated with the given option, as an {@link Optional}.
	 * If the option was given an argument type, the argument will take on that
	 * type; otherwise, it will be a {@link String}.
	 *
	 * <p>
	 * Specifying a
	 * {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default
	 * argument value} for an option will cause this method to return that default
	 * value even if the option was not detected on the command line, or if the
	 * option can take an optional argument but did not have one on the command
	 * line.
	 * </p>
	 *
	 * @param option the option to search for
	 * @return the argument of the given option as an {@code Optional}
	 * @throws NullPointerException if {@code option} is {@code null}
	 * @throws OptionException      if more than one argument was detected for the
	 *                              option
	 */
	public Optional<Object> valueOfOptional(String option) {
		return Optional.ofNullable(valueOf(option));
	}

	/**
	 * <p>
	 * Gives any arguments associated with the given option. If the option was given
	 * an argument type, the arguments will take on that type; otherwise, they will
	 * be {@link String}s.
	 * </p>
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent
	 * interface methods.
	 * </p>
	 *
	 * @param <V>    represents the type of the arguments the given option accepts
	 * @param option the option to search for
	 * @return the arguments associated with the option; an empty list if no such
	 *         arguments are present, or if the option was not detected
	 * @throws NullPointerException if {@code option} is {@code null}
	 * @throws OptionException      if there is a problem converting the option's
	 *                              arguments to the desired type; for example, if
	 *                              the type does not implement a correct conversion
	 *                              constructor or method
	 */
	public <V> List<V> valuesOf(OptionSpec<V> option) {
		requireNonNull(option);

		final List<String> values = this.optionsToArguments.get(option);
		if ((values == null) || values.isEmpty()) {
			return defaultValueFor(option);
		}

		final AbstractOptionSpec<V> spec = (AbstractOptionSpec<V>) option;
		final List<V> convertedValues = new ArrayList<>();
		for (final String each : values) {
			convertedValues.add(spec.convert(each));
		}

		return unmodifiableList(convertedValues);
	}

	/**
	 * <p>
	 * Gives any arguments associated with the given option. If the option was given
	 * an argument type, the arguments will take on that type; otherwise, they will
	 * be {@link String}s.
	 * </p>
	 *
	 * @param option the option to search for
	 * @return the arguments associated with the option, as a list of objects of the
	 *         type given to the arguments; an empty list if no such arguments are
	 *         present, or if the option was not detected
	 * @throws NullPointerException if {@code option} is {@code null}
	 */
	public List<?> valuesOf(String option) {
		requireNonNull(option);

		final AbstractOptionSpec<?> spec = this.detectedOptions.get(option);
		return spec == null ? defaultValuesFor(option) : valuesOf(spec);
	}
}
