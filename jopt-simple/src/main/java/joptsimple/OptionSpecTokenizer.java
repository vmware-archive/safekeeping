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

import static joptsimple.ParserRules.RESERVED_FOR_EXTENSIONS;
import static joptsimple.ParserRules.ensureLegalOption;

import java.util.NoSuchElementException;

/**
 * Tokenizes a short option specification string.
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
class OptionSpecTokenizer {
	private static final char POSIXLY_CORRECT_MARKER = '+';
	private static final char HELP_MARKER = '*';

	private String specification;
	private int index;

	OptionSpecTokenizer(String specification) {
		if (specification == null) {
			throw new NullPointerException("null option specification");
		}

		this.specification = specification;
	}

	private void adjustForPosixlyCorrect(OptionParser parser) {
		if (POSIXLY_CORRECT_MARKER == this.specification.charAt(0)) {
			parser.posixlyCorrect(true);
			this.specification = this.specification.substring(1);
		}
	}

	void configure(OptionParser parser) {
		adjustForPosixlyCorrect(parser);

		while (hasMore()) {
			parser.recognize(next());
		}
	}

	private AbstractOptionSpec<?> handleArgumentAcceptingOption(String candidate) {
		this.index++;

		if (hasMore() && (this.specification.charAt(this.index) == ':')) {
			this.index++;
			return new OptionalArgumentOptionSpec<String>(candidate);
		}

		return new RequiredArgumentOptionSpec<String>(candidate);
	}

	private AbstractOptionSpec<?> handleReservedForExtensionsToken() {
		if (!hasMore()) {
			return new NoArgumentOptionSpec(RESERVED_FOR_EXTENSIONS);
		}

		if (this.specification.charAt(this.index) == ';') {
			++this.index;
			return new AlternativeLongOptionSpec();
		}

		return null;
	}

	boolean hasMore() {
		return this.index < this.specification.length();
	}

	AbstractOptionSpec<?> next() {
		if (!hasMore()) {
			throw new NoSuchElementException();
		}

		final String optionCandidate = String.valueOf(this.specification.charAt(this.index));
		this.index++;

		AbstractOptionSpec<?> spec;
		if (RESERVED_FOR_EXTENSIONS.equals(optionCandidate)) {
			spec = handleReservedForExtensionsToken();

			if (spec != null) {
				return spec;
			}
		}

		ensureLegalOption(optionCandidate);

		if (hasMore()) {
			boolean forHelp = false;
			if (this.specification.charAt(this.index) == HELP_MARKER) {
				forHelp = true;
				++this.index;
			}
			spec = hasMore() && (this.specification.charAt(this.index) == ':')
					? handleArgumentAcceptingOption(optionCandidate)
					: new NoArgumentOptionSpec(optionCandidate);
			if (forHelp) {
				spec.forHelp();
			}
		} else {
			spec = new NoArgumentOptionSpec(optionCandidate);
		}

		return spec;
	}
}
