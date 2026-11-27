/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.edusharing.manager;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.olat.core.util.StringHelper;
import org.owasp.html.AttributePolicy;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Cleans the HTML of the edu-sharing rendering service before OpenOlat inserts
 * it into its own page (rich text in display mode SANITIZED_RICH_TEXT, everything without content domain).
 *
 * Initial date: 27.11.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingHtmlSanitizer {
	
	public static final String IFRAME_SANDBOX = "allow-scripts allow-same-origin allow-popups allow-forms allow-presentation";
	
	private static final Pattern NAME = Pattern.compile("[a-zA-Z0-9_\\-:. ]+");
	private static final Pattern NUMBER_OR_PERCENT = Pattern.compile("\\d+(px|%)?");
	private static final Pattern IFRAME_ALLOW = Pattern.compile("[a-z\\-; *]+");
	private static final Pattern EDUSHARING_CLASS = Pattern.compile("(edusharing_|edu_|mds|license|node_|video|option_v_)[\\w\\-]*|material-icons|author|embedded_video");
	private static final Pattern VIEWPORT_UNIT = Pattern.compile("\\d\\s*(vw|vh|vmin|vmax)", Pattern.CASE_INSENSITIVE);
	private static final Pattern NEGATIVE_VALUE = Pattern.compile("(^|[\\s:(,])-\\s*[\\d.]");
	private static final AttributePolicy STYLE_POLICY = (_, _, value) -> {
		String style = Arrays.stream(value.split(";"))
				.filter(declaration -> !VIEWPORT_UNIT.matcher(declaration).find() && !NEGATIVE_VALUE.matcher(declaration).find())
				.collect(Collectors.joining(";"));
		return style.isBlank()? null: style;
	};
	private static final AttributePolicy CLASS_POLICY = (_, _, value) -> {
		String classes = Arrays.stream(value.split("\\s+"))
				.filter(c -> EDUSHARING_CLASS.matcher(c).matches())
				.collect(Collectors.joining(" "));
		return classes.isEmpty()? null: classes;
	};
	private static final CssSchema CSS = CssSchema.withProperties(List.of("width", "max-width", "min-width", "height",
			"max-height", "min-height", "display", "margin", "margin-top", "margin-bottom", "margin-left", "margin-right",
			"padding", "padding-top", "padding-bottom", "padding-left", "padding-right", "text-align", "vertical-align",
			"font-weight", "font-style", "float", "border", "border-radius"));
			
	private final Set<String> hosts;
	private final PolicyFactory policy;
	
	public EdusharingHtmlSanitizer(String edusharingUrl, Collection<String> allowedHosts) {
		hosts = allowedHosts.stream().map(h -> h.toLowerCase(Locale.ROOT)).collect(Collectors.toCollection(HashSet::new));
		String edusharingHost = getHost(edusharingUrl);
		if (StringHelper.containsNonWhitespace(edusharingHost)) {
			hosts.add(edusharingHost);
		}
		
		AttributePolicy hostPolicy = (_, _, value) -> isAllowedHost(value)? value: null;
		policy = new HtmlPolicyBuilder()
				.allowElements("div", "span", "p", "br", "hr", "a", "img", "h1", "h2", "h3", "h4", "h5", "h6",
						"ul", "ol", "li", "i", "b", "em", "strong", "small", "sub", "sup", "figure", "figcaption",
						"table", "thead", "tbody", "tr", "th", "td", "video", "audio", "source", "track", "button")
				.allowWithoutAttributes("span", "div")
				.allowStyling(CSS)
				.allowAttributes("style").matching(STYLE_POLICY).globally()
				.allowUrlProtocols("https", "http")
				.allowAttributes("class").matching(CLASS_POLICY).globally()
				.allowAttributes("title", "role", "lang").globally()
				.allowAttributes("aria-label", "aria-hidden", "aria-expanded", "aria-haspopup", "aria-checked").globally()
				.allowAttributes("tabindex").matching(Pattern.compile("-?\\d")).globally()
				.allowAttributes("data-url").matching(hostPolicy).onElements("li")
				.allowAttributes("type").matching(Pattern.compile("button")).onElements("button")
				.allowAttributes("href").onElements("a")
				.allowAttributes("target").matching(Pattern.compile("_blank")).onElements("a")
				.requireRelsOnLinks("noopener", "noreferrer")
				.allowAttributes("alt").onElements("img")
				.allowAttributes("width", "height").matching(NUMBER_OR_PERCENT).onElements("img", "video", "iframe")
				.allowAttributes("src").matching(hostPolicy).onElements("img", "video", "audio", "source", "track", "iframe")
				.allowAttributes("poster").matching(hostPolicy).onElements("video")
				.allowAttributes("controls", "preload", "controlslist", "playsinline", "muted").onElements("video", "audio")
				.allowAttributes("type").matching(NAME).onElements("source")
				.allowAttributes("kind", "srclang", "label").matching(NAME).onElements("track")
				.allowAttributes("frameborder", "scrolling", "allowfullscreen").onElements("iframe")
				.allowAttributes("allow").matching(IFRAME_ALLOW).onElements("iframe")
				.allowElements((name, attrs) -> {
					if (!hasAttribute(attrs, "src")) {
						return null;
					}
					attrs.add("sandbox");
					attrs.add(IFRAME_SANDBOX);
					return name;
				}, "iframe")
				.toFactory();
	}
	
	public String sanitize(String html) {
		if (!StringHelper.containsNonWhitespace(html)) {
			return "";
		}
		return policy.sanitize(html);
	}
	
	private static boolean hasAttribute(List<String> attrs, String name) {
		for (int i = 0; i < attrs.size(); i += 2) {
			if (name.equals(attrs.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	boolean isAllowedHost(String url) {
		String host = getHost(url);
		return StringHelper.containsNonWhitespace(host) && hosts.contains(host);
	}
	
	private static String getHost(String url) {
		if (!StringHelper.containsNonWhitespace(url)) {
			return null;
		}
		try {
			String absolute = url.trim().startsWith("//")? "https:" + url.trim(): url.trim();
			URI uri = new URI(absolute);
			String scheme = uri.getScheme();
			if (uri.getHost() == null || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
				return null;
			}
			return uri.getHost().toLowerCase(Locale.ROOT);
		} catch (Exception e) {
			return null;
		}
	}
	
}
