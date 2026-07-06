/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.certificate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small interpreter which turns a serial number format string into a serial
 * number. The format is compiled once with {@link #parse(String)} and can then
 * be rendered repeatedly with {@link #generate(long, LocalDate)}.
 * <p>
 * Supported tokens:
 * <ul>
 * <li><code>${counter}</code> - the running counter (mandatory)</li>
 * <li><code>${counter:N}</code> - the counter, left-padded with zeros to at least N digits</li>
 * <li><code>${year}</code> - the 4 digit year</li>
 * <li><code>${month}</code> - the 2 digit month (01-12)</li>
 * <li><code>${day}</code> - the 2 digit day of month (01-31)</li>
 * </ul>
 * <code>${year}</code>, <code>${month}</code> and <code>${day}</code> also
 * accept the <code>:N</code> zero-padding suffix. Everything outside of a token,
 * as well as any unrecognized <code>${...}</code> sequence, is copied verbatim.
 * <p>
 * Example: <code>SVTI-${year}-${counter:5}</code> with counter 1 in 2026 yields
 * <code>SVTI-2026-00001</code>.
 * <p>
 * 
 * Initial date: 3 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SerialNumberFormat {

	private static final Pattern TOKEN = Pattern
			.compile("\\$\\{\\s*(counter|year|month|day)\\s*(?::\\s*(\\d+))?\\s*\\}");

	private final String format;
	private final boolean hasCounter;
	private final List<Token> tokens;

	private SerialNumberFormat(String format, List<Token> tokens, boolean hasCounter) {
		this.format = format;
		this.tokens = tokens;
		this.hasCounter = hasCounter;
	}

	/**
	 * Compile a format string into a reusable interpreter.
	 *
	 * @param format the format string, may be null or empty
	 * @return the compiled interpreter, never null
	 */
	public static SerialNumberFormat parse(String format) {
		List<Token> tokens = new ArrayList<>();
		boolean hasCounter = false;
		if (format != null && !format.isEmpty()) {
			Matcher matcher = TOKEN.matcher(format);
			int pos = 0;
			while (matcher.find()) {
				if (matcher.start() > pos) {
					tokens.add(literal(format.substring(pos, matcher.start())));
				}
				int pad = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
				switch (matcher.group(1)) {
					case "counter" -> {
						tokens.add((sb, counter, date) -> sb.append(zeroPad(Long.toString(counter), pad)));
						hasCounter = true;
					}
					case "year" -> tokens.add((sb, counter, date) -> sb.append(zeroPad(Integer.toString(date.getYear()), pad)));
					case "month" -> tokens.add((sb, counter, date) -> sb.append(zeroPad(pad2(date.getMonthValue()), pad)));
					case "day" -> tokens.add((sb, counter, date) -> sb.append(zeroPad(pad2(date.getDayOfMonth()), pad)));
					default -> { /* the regex only matches the tokens above */ }
				}
				pos = matcher.end();
			}
			if (pos < format.length()) {
				tokens.add(literal(format.substring(pos)));
			}
		}
		return new SerialNumberFormat(format == null ? "" : format, tokens, hasCounter);
	}

	/**
	 * @return the original format string, never null
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return true if the format contains the mandatory <code>${counter}</code> token
	 */
	public boolean hasCounter() {
		return hasCounter;
	}

	/**
	 * Render the serial number for the given counter at the current date.
	 *
	 * @param counter the counter value
	 * @return the rendered serial number
	 */
	public String generate(long counter) {
		return generate(counter, LocalDate.now());
	}

	/**
	 * Render the serial number for the given counter and date.
	 *
	 * @param counter the counter value
	 * @param date the date used for the year/month/day tokens, defaults to today when null
	 * @return the rendered serial number
	 */
	public String generate(long counter, LocalDate date) {
		LocalDate refDate = date == null ? LocalDate.now() : date;
		StringBuilder sb = new StringBuilder(format.length() + 8);
		for (Token token : tokens) {
			token.render(sb, counter, refDate);
		}
		return sb.toString();
	}

	private static Token literal(String value) {
		return (sb, counter, date) -> sb.append(value);
	}

	private static String pad2(int value) {
		return value < 10 ? "0" + value : Integer.toString(value);
	}

	private static String zeroPad(String value, int minLength) {
		if (value.length() >= minLength) {
			return value;
		}
		StringBuilder sb = new StringBuilder(minLength);
		sb.append("0".repeat(minLength - value.length()));
		return sb.append(value).toString();
	}

	@FunctionalInterface
	private interface Token {
		void render(StringBuilder sb, long counter, LocalDate date);
	}
}
