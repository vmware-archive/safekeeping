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

package joptsimple.internal;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static joptsimple.internal.Strings.LINE_SEPARATOR;
import static joptsimple.internal.Strings.repeat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
public class Rows {
	private final int overallWidth;
	private final int columnSeparatorWidth;
	private final List<Row> rows = new ArrayList<>();

	private int widthOfWidestOption;
	private int widthOfWidestDescription;

	public Rows(int overallWidth, int columnSeparatorWidth) {
		this.overallWidth = overallWidth;
		this.columnSeparatorWidth = columnSeparatorWidth;
	}

	private void add(Row row) {
		this.rows.add(row);
		this.widthOfWidestOption = max(this.widthOfWidestOption, row.option.length());
		this.widthOfWidestDescription = max(this.widthOfWidestDescription, row.description.length());
	}

	public void add(String option, String description) {
		add(new Row(option, description));
	}

	private int descriptionWidth() {
		return min(this.overallWidth - optionWidth() - this.columnSeparatorWidth, this.widthOfWidestDescription);
	}

	public void fitToWidth() {
		final Columns columns = new Columns(optionWidth(), descriptionWidth());

		final List<Row> fitted = new ArrayList<>();
		for (final Row each : this.rows) {
			fitted.addAll(columns.fit(each));
		}

		reset();

		for (final Row each : fitted) {
			add(each);
		}
	}

	private int optionWidth() {
		return min((this.overallWidth - this.columnSeparatorWidth) / 2, this.widthOfWidestOption);
	}

	private StringBuilder pad(StringBuilder buffer, String s, int length) {
		buffer.append(s).append(repeat(' ', length - s.length()));
		return buffer;
	}

	public String render() {
		final StringBuilder buffer = new StringBuilder();

		for (final Row each : this.rows) {
			pad(buffer, each.option, optionWidth()).append(repeat(' ', this.columnSeparatorWidth));
			pad(buffer, each.description, descriptionWidth()).append(LINE_SEPARATOR);
		}

		return buffer.toString();
	}

	public void reset() {
		this.rows.clear();
		this.widthOfWidestOption = 0;
		this.widthOfWidestDescription = 0;
	}
}
