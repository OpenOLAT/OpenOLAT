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
package org.olat.commons.calendar.manager;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Checks the links of a calendar event. A link can come from an imported or a
 * synchronized iCalendar file (property X-OLAT-LINK) or from the REST API: the
 * URI and the icon class are untrusted.
 * <ul>
 * <li>An internal link is a URL of this server, a business path (e.g.
 * 		"[RepositoryEntry:1]") or a path of this server (e.g. "/url/..."). The
 * 		server opens it with a redirect.</li>
 * <li>An external link is a URI with "://" which is no URL of this server. It
 * 		must use http or https.</li>
 * <li>Other URIs are not allowed.</li>
 * <li>The icon class is a list of 1 to {@link #MAX_CSS_CLASSES} CSS
 * 		classes separated by spaces, each a letter or "_", then letters,
 * 		digits, "_" and "-", at most {@link #MAX_CSS_CLASS_LENGTH}
 * 		characters ({@link #isValidCssClasses(String)}).</li>
 * </ul>
 * Values of the link are logged shortened and with their control
 * characters escaped (no log injection).
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public final class EventLinkValidator {

	private static final Logger log = Tracing.createLoggerFor(EventLinkValidator.class);

	public static final int MAX_CSS_CLASS_LENGTH = 128;
	public static final int MAX_CSS_CLASSES = 8;

	/** One CSS class name, e.g. "o_icon_link" */
	private static final Pattern CSS_CLASS = Pattern.compile("[A-Za-z_][A-Za-z0-9_-]*");
	private static final Pattern SPACES = Pattern.compile(" +");

	/** The length of a value in the log, longer values are cut */
	static final int MAX_LOG_LENGTH = 80;

	private EventLinkValidator() {
		//
	}

	/**
	 * @param uri The URI of a link
	 * @param rootUri The URL of this server, e.g. "https://learn.example.com"
	 * @return true if the link is an external link (a URI with "://" which
	 * 		is no URL of this server)
	 */
	public static boolean isExternal(String uri, String rootUri) {
		if (uri == null) {
			return false;
		}
		if (isServerUrl(uri, rootUri)) {
			return false;
		}
		return uri.contains("://");
	}

	/**
	 * @param uri The URI of a link
	 * @param rootUri The URL of this server
	 * @return true if the link is an internal link: a URL of this server, a
	 * 		business path or a path of this server
	 */
	public static boolean isInternal(String uri, String rootUri) {
		if (uri == null) {
			return false;
		}
		return isServerUrl(uri, rootUri) || isBusinessPath(uri) || isServerPath(uri);
	}

	/**
	 * @param uri A URI
	 * @param rootUri The URL of this server, e.g. "https://learn.example.com"
	 * @return true if the URI is the URL of this server or starts with the
	 * 		URL of this server followed by "/", "?" or "#"
	 */
	static boolean isServerUrl(String uri, String rootUri) {
		if (uri == null || !StringHelper.containsNonWhitespace(rootUri) || !uri.startsWith(rootUri)) {
			return false;
		}
		if (uri.length() == rootUri.length()) {
			return true;
		}
		char next = uri.charAt(rootUri.length());
		return next == '/' || next == '?' || next == '#';
	}

	/**
	 * @param uri A URI
	 * @return true if the URI is a business path, e.g. "[RepositoryEntry:1]"
	 */
	static boolean isBusinessPath(String uri) {
		return uri != null && uri.startsWith("[") && !containsWhitespaceOrControl(uri);
	}

	/**
	 * @param uri A URI
	 * @return true if the URI is a path of this server: it starts with one
	 * 		"/" (not "//" and not "/\"), without whitespace and control characters
	 */
	static boolean isServerPath(String uri) {
		if (uri == null || uri.isEmpty() || uri.charAt(0) != '/' || containsWhitespaceOrControl(uri)) {
			return false;
		}
		return uri.length() == 1 || (uri.charAt(1) != '/' && uri.charAt(1) != '\\');
	}

	private static boolean containsWhitespaceOrControl(String uri) {
		for (int i = 0; i < uri.length(); i++) {
			char c = uri.charAt(i);
			if (c <= ' ' || c == 0x7f || Character.isWhitespace(c) || Character.isISOControl(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param uri A URI
	 * @return true if the URI starts with "http://" or "https://" (the case is
	 * 		ignored, nothing before the scheme)
	 */
	public static boolean isHttpUri(String uri) {
		if (uri == null) {
			return false;
		}
		String lowerUri = uri.toLowerCase(Locale.ROOT);
		return lowerUri.startsWith("http://") || lowerUri.startsWith("https://");
	}

	/**
	 * @param uri The URI of a link
	 * @param rootUri The URL of this server
	 * @return true if the URI is an internal link or an external http or
	 * 		https link
	 */
	public static boolean isAllowedUri(String uri, String rootUri) {
		if (!StringHelper.containsNonWhitespace(uri)) {
			return false;
		}
		return isInternal(uri, rootUri) || (isExternal(uri, rootUri) && isHttpUri(uri));
	}

	/**
	 * @param iconCssClass The icon class of a link
	 * @return The trimmed classes, or null if the value is blank or no valid
	 * 		list of CSS classes
	 */
	public static String validIconCssClass(String iconCssClass) {
		if (StringHelper.containsNonWhitespace(iconCssClass) && isValidCssClasses(iconCssClass)) {
			return iconCssClass.trim();
		}
		return null;
	}

	/**
	 * @param value A value
	 * @return true if the value is 1 to {@link #MAX_CSS_CLASSES} CSS class
	 * 		names separated by spaces. Leading and trailing white space is
	 * 		allowed and is trimmed by {@link #validIconCssClass(String)}.
	 */
	static boolean isValidCssClasses(String value) {
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
			if (cssClass.length() > MAX_CSS_CLASS_LENGTH || !CSS_CLASS.matcher(cssClass).matches()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param value A value of unknown origin
	 * @return The value for a log message: at most {@link #MAX_LOG_LENGTH}
	 * 		characters followed by "...", control characters, format
	 * 		characters and line separators as "\\uXXXX", quotes and
	 * 		backslashes escaped
	 */
	static String toLogValue(String value) {
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
	 * Checks a link of an untrusted source. An invalid icon class is dropped
	 * (empty string), the link keeps its URI.
	 *
	 * @param link The link
	 * @param rootUri The URL of this server
	 * @param source The source of the link for the log
	 * @return The link, or null if its URI is not allowed
	 */
	public static KalendarEventLink validLink(KalendarEventLink link, String rootUri, String source) {
		if (link == null) {
			return null;
		}
		if (!isAllowedUri(link.getURI(), rootUri)) {
			log.warn("Calendar event link with an invalid URI in {} ignored: \"{}\"",
					toLogValue(source), toLogValue(link.getURI()));
			return null;
		}
		String iconCssClass = link.getIconCssClass();
		if (StringHelper.containsNonWhitespace(iconCssClass)) {
			String validIconCssClass = validIconCssClass(iconCssClass);
			if (validIconCssClass == null) {
				log.warn("Calendar event link with an invalid icon class in {}, the icon is ignored: \"{}\"",
						toLogValue(source), toLogValue(iconCssClass));
				link.setIconCssClass("");
			} else {
				link.setIconCssClass(validIconCssClass);
			}
		}
		return link;
	}
}
