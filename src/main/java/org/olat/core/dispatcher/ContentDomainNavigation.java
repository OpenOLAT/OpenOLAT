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
package org.olat.core.dispatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPBuilder;
import org.olat.core.commons.services.csp.CSPBuilder.Directive;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;

/**
 * Handles browser navigations to the content domain which are not a delivery
 * of content (OO-10045). The content domain never shows a login page or a
 * business path:
 * <ul>
 * <li>a top-level GET navigation is redirected (302) to the same path on the
 * main domain,</li>
 * <li>a navigation in a frame (and a POST navigation) gets a small,
 * session-free intermediate page which hands the path over to the main
 * window (<code>contentnav.js</code>).</li>
 * </ul>
 * Delivery paths, the component URLs of the content delivery windows (SCORM)
 * and all requests which are not navigations (images, scripts, fetch...) are
 * not changed.
 * <p>
 * The class must never create an HTTP session.
 *
 * Initial date: 10 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ContentDomainNavigation {

	private static final Logger log = Tracing.createLoggerFor(ContentDomainNavigation.class);

	public static final int MAX_PATH_LENGTH = 2048;

	private static final String PATH_URL = "/url/";

	/**
	 * Paths delivered by the content host without change.
	 */
	private static final List<String> DELIVERY_PATHS = List.of(DispatcherModule.PATH_MAPPED,
			DispatcherModule.PATH_GLOBAL_MAPPED, "/library/", "/edusharing/", "/robots.txt");

	/**
	 * Query parameters removed from the redirect target (lower case).
	 */
	private static final Set<String> REMOVED_PARAMETERS = Set.of("jsessionid", "oow", "o_winrndo",
			"token", "x-olat-token", "_csrf");

	public enum Navigation {
		/** Not a navigation: images, scripts, fetch, XHR, non-browser clients */
		NONE,
		/** Top-level navigation */
		DOCUMENT,
		/** Navigation in an iframe, frame, embed or object */
		FRAME,
		/** Browser without Sec-Fetch headers which accepts HTML */
		UNKNOWN
	}

	private ContentDomainNavigation() {
		//
	}

	/**
	 * Redirect or answer navigations to the content domain which are not
	 * a delivery.
	 *
	 * @param request The HTTP request
	 * @param response The HTTP response
	 * @return true if the request was answered and must not be processed further
	 */
	public static boolean handle(HttpServletRequest request, HttpServletResponse response) {
		if(!Settings.isContentDomainNameEnabled()) {
			return false;
		}
		if(isContentHost(request)) {
			return handleContentHost(request, response);
		}
		return handleFramedOnMainDomain(request, response);
	}

	/**
	 * Phase 2: a link to the main domain in a frame of the content domain without
	 * our JavaScript (PDF, SCORM, CP...). The OpenOlat page would be blocked in the
	 * frame (X-Frame-Options) and would launch a second window. The intermediate
	 * page calls gotopath() of the OpenOlat window instead.
	 *
	 * @param request The HTTP request to the main domain
	 * @param response The HTTP response
	 * @return true if the request was answered with the intermediate page
	 */
	private static boolean handleFramedOnMainDomain(HttpServletRequest request, HttpServletResponse response) {
		if(getNavigation(request) != Navigation.FRAME || !isRefererContentOrigin(request)) {
			return false;
		}

		String path = getPath(request);
		if(!isBusinessPath(path) || isDispatchUri(path)) {
			return false;
		}

		String safePath = sanitize(path, request.getQueryString());
		String target = Settings.getServerContextPathURI() + (safePath == null ? "/" : safePath);
		sendIntermediatePage(response, target, safePath);
		return true;
	}

	/**
	 * Browsers send the origin of the content page as referrer (default policy
	 * strict-origin-when-cross-origin) or the full URL.
	 *
	 * @param request The HTTP request
	 * @return true if the referrer is a page of the content domain
	 */
	public static boolean isRefererContentOrigin(HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		if(referer == null) {
			return false;
		}
		String contentOrigin = Settings.createContentServerURI();
		return referer.equalsIgnoreCase(contentOrigin)
				|| referer.regionMatches(true, 0, contentOrigin + "/", 0, contentOrigin.length() + 1);
	}

	private static boolean handleContentHost(HttpServletRequest request, HttpServletResponse response) {
		Navigation navigation = getNavigation(request);
		if(navigation == Navigation.NONE) {
			return false;
		}

		String path = getPath(request);
		String method = request.getMethod();
		boolean documentGet = navigation == Navigation.DOCUMENT && ("GET".equals(method) || "HEAD".equals(method));
		if(isDeliveryPath(path)
				// the content delivery window (SCORM) lives in a frame, only a bookmark of it is redirected
				|| (!documentGet && isDispatchUri(path))
				// WebDAV clients send no Sec-Fetch headers
				|| (navigation == Navigation.UNKNOWN && path.startsWith(DispatcherModule.WEBDAV_PATH))) {
			return false;
		}

		String safePath = sanitize(path, request.getQueryString());
		if(documentGet) {
			// A component URL makes no sense in a new window on the main domain
			String targetPath = safePath == null || isDispatchUri(safePath) ? "/" : safePath;
			redirect(response, Settings.getServerContextPathURI() + targetPath);
		} else {
			String target = Settings.getServerContextPathURI() + (safePath == null ? "/" : safePath);
			String businessPath = safePath != null && isBusinessPath(safePath) ? safePath : null;
			sendIntermediatePage(response, target, businessPath);
		}
		return true;
	}

	/**
	 * @param request The HTTP request
	 * @return true if the request is a top-level GET navigation to the content domain
	 */
	public static boolean isDocumentGetOnContentHost(HttpServletRequest request) {
		return isContentHost(request)
				&& "GET".equals(request.getMethod())
				&& getNavigation(request) == Navigation.DOCUMENT;
	}

	/**
	 * Redirect to the start page of the main domain.
	 *
	 * @param response The HTTP response
	 */
	public static void redirectToStartPage(HttpServletResponse response) {
		redirect(response, Settings.getServerContextPathURI() + "/");
	}

	public static boolean isContentHost(HttpServletRequest request) {
		return Settings.isContentDomainNameEnabled()
				&& Settings.getContentDomainName().equalsIgnoreCase(request.getServerName());
	}

	public static Navigation getNavigation(HttpServletRequest request) {
		String mode = request.getHeader("Sec-Fetch-Mode");
		if(mode == null) {
			String accept = request.getHeader("Accept");
			return accept != null && accept.contains("text/html") ? Navigation.UNKNOWN : Navigation.NONE;
		}
		if(!"navigate".equalsIgnoreCase(mode)) {
			return Navigation.NONE;
		}

		String dest = request.getHeader("Sec-Fetch-Dest");
		if(dest == null) {
			return Navigation.NONE;
		}
		return switch(dest.toLowerCase(Locale.ROOT)) {
			case "document" -> Navigation.DOCUMENT;
			case "iframe", "frame", "embed", "object" -> Navigation.FRAME;
			default -> Navigation.NONE;
		};
	}

	/**
	 * @param request The HTTP request
	 * @return The request URI without the context path, still encoded, never null
	 */
	public static String getPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if(uri == null || uri.isEmpty()) {
			return "/";
		}
		String contextPath = WebappHelper.getServletContextPath();
		if(StringHelper.containsNonWhitespace(contextPath) && uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		return uri.startsWith("/") ? uri : "/" + uri;
	}

	public static boolean isDeliveryPath(String path) {
		for(String deliveryPath:DELIVERY_PATHS) {
			if(path.startsWith(deliveryPath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The content delivery windows (SCORM) are full OpenOlat windows on the content
	 * domain with the URI prefix /auth/. Their component URLs start with a segment
	 * with the window id, the timestamp and the component id separated by colons,
	 * business paths never have a colon in their first segment.
	 *
	 * @param path The encoded path
	 * @return true if the path is the URL of a component of an OpenOlat window
	 */
	public static boolean isDispatchUri(String path) {
		if(!path.startsWith(DispatcherModule.PATH_AUTHENTICATED)) {
			return false;
		}
		String rest = path.substring(DispatcherModule.PATH_AUTHENTICATED.length());
		int slash = rest.indexOf('/');
		String firstSegment = slash >= 0 ? rest.substring(0, slash) : rest;
		return firstSegment.indexOf(':') >= 0 || firstSegment.toLowerCase(Locale.ROOT).contains("%3a");
	}

	public static boolean isBusinessPath(String path) {
		return path.startsWith(DispatcherModule.PATH_AUTHENTICATED) || path.startsWith(PATH_URL);
	}

	/**
	 * Remove the session id, tokens and window parameters from the path and the query.
	 *
	 * @param path The encoded path, without context path
	 * @param query The query string (can be null)
	 * @return The sanitized path with the query, or null if the path is not acceptable
	 */
	public static String sanitize(String path, String query) {
		if(path == null || !path.startsWith("/") || hasControlCharacters(path) || hasControlCharacters(query)) {
			return null;
		}

		String safePath = removeMatrixSessionId(path);
		StringBuilder sb = new StringBuilder(safePath.length() + 64).append(safePath);
		if(StringHelper.containsNonWhitespace(query)) {
			boolean first = true;
			for(String parameter:query.split("&")) {
				if(parameter.isEmpty() || isRemovedParameter(parameter)) {
					continue;
				}
				sb.append(first ? '?' : '&').append(parameter);
				first = false;
			}
		}

		String sanitized = sb.toString();
		return sanitized.length() > MAX_PATH_LENGTH ? null : sanitized;
	}

	private static String removeMatrixSessionId(String path) {
		String lowerPath = path.toLowerCase(Locale.ROOT);
		int index = lowerPath.indexOf(";jsessionid=");
		while(index >= 0) {
			int end = path.indexOf('/', index);
			path = end >= 0 ? path.substring(0, index) + path.substring(end) : path.substring(0, index);
			lowerPath = path.toLowerCase(Locale.ROOT);
			index = lowerPath.indexOf(";jsessionid=");
		}
		return path;
	}

	private static boolean isRemovedParameter(String parameter) {
		int eq = parameter.indexOf('=');
		String name = eq >= 0 ? parameter.substring(0, eq) : parameter;
		return REMOVED_PARAMETERS.contains(name.toLowerCase(Locale.ROOT));
	}

	/**
	 * Check raw and percent-encoded control characters (C0 and DEL).
	 */
	private static boolean hasControlCharacters(String value) {
		if(value == null) {
			return false;
		}

		int length = value.length();
		for(int i=0; i<length; i++) {
			char ch = value.charAt(i);
			if(ch < 0x20 || ch == 0x7F) {
				return true;
			}
			if(ch == '%' && i + 2 < length) {
				char h1 = value.charAt(i + 1);
				char h2 = Character.toLowerCase(value.charAt(i + 2));
				if(((h1 == '0' || h1 == '1') && Character.digit(h2, 16) >= 0)
						|| (h1 == '7' && h2 == 'f')) {
					return true;
				}
			}
		}
		return false;
	}

	private static void redirect(HttpServletResponse response, String target) {
		try {
			response.sendRedirect(target);
			ServletUtil.setCacheHeaders(response, ServletUtil.CACHE_NO_CACHE);
		} catch (IOException e) {
			log.error("Redirect from content domain failed: {}", target, e);
		}
	}

	private static void sendIntermediatePage(HttpServletResponse response, String target, String businessPath) {
		if(response.isCommitted()) {
			return;
		}

		// Remove the headers of HeadersFilter, X-Frame-Options and its CSP would block the page in the frame
		response.reset();

		String serverOrigin = Settings.createServerURI();
		String contentOrigin = Settings.createContentServerURI();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		CSPModule cspModule = CoreSpringFactory.getImpl(CSPModule.class);
		response.setHeader("Content-Security-Policy", "default-src 'none'; script-src 'self'; style-src 'self'; "
				+ getFrameAncestors(cspModule, serverOrigin, contentOrigin));
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.setHeader("X-Robots-Tag", "noindex");
		if(cspModule != null && cspModule.isStrictTransportSecurityEnabled()) {
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
		}

		Locale locale = I18nModule.getDefaultLocale();
		String linkText;
		try {
			Translator translator = Util.createPackageTranslator(ContentDomainNavigation.class, locale);
			linkText = translator.translate("content.navigation.link");
		} catch (Exception e) {
			log.warn("Cannot translate content navigation link", e);
			linkText = "Open the page";
		}

		String scriptUri = StaticMediaDispatcher.getStaticURI("js/openolat/contentnav.js");
		String page = renderIntermediatePage(locale, linkText, target, businessPath, serverOrigin, scriptUri);
		try {
			PrintWriter writer = response.getWriter();
			writer.write(page);
			writer.flush();
		} catch (IOException e) {
			log.error("Cannot send content navigation page", e);
		}
	}

	/**
	 * The frame-ancestors directive is checked against all ancestors. If OpenOlat
	 * is embedded in another platform (LTI, portal), the configured ancestors and
	 * the ones of the directive providers must be allowed too, the same as for
	 * the pages of OpenOlat.
	 *
	 * @param cspModule The CSP module (can be null)
	 * @param serverOrigin The origin of the main domain
	 * @param contentOrigin The origin of the content domain
	 * @return The frame-ancestors directive
	 */
	public static String getFrameAncestors(CSPModule cspModule, String serverOrigin, String contentOrigin) {
		if(cspModule == null) {
			return "frame-ancestors " + serverOrigin + " " + contentOrigin + ";";
		}
		Directive frameAncestors = new CSPBuilder(cspModule).frameAncestors()
				.useDefault()
				.useConfiguration();
		frameAncestors.addAdditionalDirectives(serverOrigin);
		frameAncestors.addAdditionalDirectives(contentOrigin);
		return frameAncestors.toString();
	}

	/**
	 * Build the intermediate page. All values are HTML-escaped.
	 *
	 * @param locale The locale of the page
	 * @param linkText The text of the fallback link
	 * @param target The URL on the main domain
	 * @param businessPath The business path to hand over to the main window (can be null)
	 * @param serverOrigin The origin of the main domain
	 * @param scriptUri The URI of contentnav.js on the same host
	 * @return The HTML page
	 */
	public static String renderIntermediatePage(Locale locale, String linkText, String target, String businessPath,
			String serverOrigin, String scriptUri) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("<!DOCTYPE html>\n<html lang=\"").append(StringHelper.escapeHtml(locale.toLanguageTag())).append("\">\n")
		  .append("<head>\n<meta charset=\"utf-8\">\n")
		  .append("<meta name=\"robots\" content=\"noindex\">\n")
		  .append("<title>OpenOlat</title>\n")
		  .append("<script src=\"").append(StringHelper.escapeHtml(scriptUri)).append("\" defer></script>\n")
		  .append("</head>\n<body>\n<p><a id=\"o_contentnav\" href=\"").append(StringHelper.escapeHtml(target))
		  .append("\" target=\"_top\" data-server=\"").append(StringHelper.escapeHtml(serverOrigin)).append("\"");
		if(businessPath != null) {
			sb.append(" data-path=\"").append(StringHelper.escapeHtml(businessPath)).append("\"");
		}
		sb.append(">").append(StringHelper.escapeHtml(linkText)).append("</a></p>\n</body>\n</html>\n");
		return sb.toString();
	}
}
