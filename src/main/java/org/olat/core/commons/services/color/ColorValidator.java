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
package org.olat.core.commons.services.color;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Checks the colour values that come from a configuration, a theme, the
 * database or an imported file before they are used as a CSS class. A value
 * that does not pass is dropped or rendered as no colour, never repaired.
 * <ul>
 * <li>A colour key ({@link #KEY}): lower case letters and digits, parts
 * 		separated by one "-", "_" or ".", e.g. "lightblue", "blue-light",
 * 		"theme2-dark", "light_blue", "light.blue". At most
 * 		{@link #MAX_KEY_LENGTH} characters.</li>
 * <li>A CSS class ({@link #CSS_CLASS}): one class name, a letter or "_",
 * 		then letters, digits, "_" and "-", e.g. "o_color_lightblue". At
 * 		most {@link #MAX_CSS_CLASS_LENGTH} characters.</li>
 * <li>A list of CSS classes: 1 to {@link #MAX_CSS_CLASSES} classes separated
 * 		by spaces, e.g. "o_swatch_blue o_swatch_light". No line break: the
 * 		list can be written into a JavaScript string.</li>
 * </ul>
 * Neither form can hold a quote, "&lt;", "&gt;", "=", ";", ":", "(", "/", "\"
 * or white space inside a name: a valid value can not leave an HTML
 * attribute, a JavaScript string or a CSS class selector.
 * <p>
 * An invalid value is logged once per source and value, with the value
 * shortened and its control characters escaped (no log injection).
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public final class ColorValidator {

	private static final Logger log = Tracing.createLoggerFor(ColorValidator.class);

	public static final int MAX_KEY_LENGTH = 64;
	public static final int MAX_CSS_CLASS_LENGTH = 128;
	public static final int MAX_CSS_CLASSES = 8;

	/** A colour key, e.g. "lightblue", "blue-light", "light_blue", "light.blue" */
	public static final Pattern KEY = Pattern.compile("[a-z][a-z0-9]*(?:[-_.][a-z0-9]+)*");
	/** One CSS class name, e.g. "o_color_lightblue" */
	public static final Pattern CSS_CLASS = Pattern.compile("[A-Za-z_][A-Za-z0-9_-]*");
	private static final Pattern SPACES = Pattern.compile(" +");

	/** The length of a value in the log, longer values are cut */
	static final int MAX_LOG_LENGTH = 80;
	/** The number of invalid values logged, to bound the memory of the log once */
	private static final int MAX_LOGGED = 1000;
	private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();

	private ColorValidator() {
		//
	}

	/**
	 * @param value A value
	 * @return true if the value is a colour key, see {@link #KEY}
	 */
	public static boolean isValidKey(String value) {
		return value != null && value.length() <= MAX_KEY_LENGTH && KEY.matcher(value).matches();
	}

	/**
	 * @param value A value
	 * @return true if the value is one CSS class name, see {@link #CSS_CLASS}
	 */
	public static boolean isValidCssClass(String value) {
		return value != null && value.length() <= MAX_CSS_CLASS_LENGTH && CSS_CLASS.matcher(value).matches();
	}

	/**
	 * @param value A value
	 * @return true if the value is a stored colour value: a colour key or one
	 * 		CSS class, e.g. "light.blue", "o_color_lightblue"
	 */
	public static boolean isValidColorValue(String value) {
		return isValidKey(value) || isValidCssClass(value);
	}

	/**
	 * @param value A value
	 * @return true if the value is 1 to {@link #MAX_CSS_CLASSES} CSS class
	 * 		names separated by spaces. Leading and trailing white space is
	 * 		allowed and is trimmed by {@link #validCssClasses(String, String)}.
	 */
	public static boolean isValidCssClasses(String value) {
		if (value == null || value.length() > MAX_CSS_CLASSES * (MAX_CSS_CLASS_LENGTH + 1)) {
			return false;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return false;
		}
		String[] classes = SPACES.split(trimmed);
		if (classes.length > MAX_CSS_CLASSES) {
			return false;
		}
		for (String cssClass : classes) {
			if (!isValidCssClass(cssClass)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param value A colour key
	 * @param source The source of the value for the log, e.g. "stored colour"
	 * @return The key, or null if the value is no valid key. An invalid value
	 * 		that is not blank is logged once.
	 */
	public static String validKey(String value, String source) {
		if (isValidKey(value)) {
			return value;
		}
		logInvalid(source, value);
		return null;
	}

	/**
	 * @param value A list of CSS classes
	 * @param source The source of the value for the log
	 * @return The trimmed classes, or null if the value is no valid list of
	 * 		CSS classes. An invalid value that is not blank is logged once.
	 */
	public static String validCssClasses(String value, String source) {
		if (isValidCssClasses(value)) {
			return value.trim();
		}
		logInvalid(source, value);
		return null;
	}

	/**
	 * Logs a warning for an invalid value, once per source and value. A null
	 * or blank value is no irregularity and is not logged.
	 *
	 * @param source The source of the value, e.g. "stored colour"
	 * @param value The invalid value
	 */
	public static void logInvalid(String source, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		String logValue = toLogValue(value);
		String logKey = source + "\u0000" + logValue;
		if (LOGGED.size() < MAX_LOGGED && LOGGED.add(logKey)) {
			log.warn("Invalid colour value in {}: \"{}\". The value is ignored.", toLogValue(source), logValue);
		}
	}

	/**
	 * @param value A value of unknown origin
	 * @return The value for a log message: at most {@link #MAX_LOG_LENGTH}
	 * 		characters followed by "...", control characters, format
	 * 		characters and line separators as "\\uXXXX", quotes and
	 * 		backslashes escaped
	 */
	public static String toLogValue(String value) {
		if (value == null) {
			return "null";
		}
		boolean cut = value.length() > MAX_LOG_LENGTH;
		String shortValue = cut ? value.substring(0, MAX_LOG_LENGTH) : value;
		StringBuilder sb = new StringBuilder(shortValue.length() + 8);
		for (int i = 0; i < shortValue.length(); i++) {
			char c = shortValue.charAt(i);
			int type = Character.getType(c);
			if (c == '"' || c == '\\') {
				sb.append('\\').append(c);
			} else if (Character.isISOControl(c) || type == Character.FORMAT || type == Character.LINE_SEPARATOR
					|| type == Character.PARAGRAPH_SEPARATOR || type == Character.SURROGATE
					|| type == Character.UNASSIGNED || type == Character.PRIVATE_USE) {
				sb.append(String.format("\\u%04x", Integer.valueOf(c)));
			} else {
				sb.append(c);
			}
		}
		if (cut) {
			sb.append("...");
		}
		return sb.toString();
	}

	/**
	 * For the tests: forgets the logged values.
	 */
	static void resetLogged() {
		LOGGED.clear();
	}
}
