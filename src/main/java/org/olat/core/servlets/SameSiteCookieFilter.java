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
package org.olat.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Implementation of an HTTP servlet {@link Filter} which conditionally adds the
 * SameSite attribute to cookies.
 *
 * <p>Affected cookies are configured and placed into a Map of cookie name to
 * same-site attribute value.</p>
 *
 * Initial date: 16 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SameSiteCookieFilter implements Filter {

	private final Logger log = Tracing.createLoggerFor(SameSiteCookieFilter.class);

	/** The name of the same-site cookie attribute. */
	private static final String SAMESITE_ATTRIBITE_NAME = "SameSite";
	
	private static final String SET_COOKIE = "Set-Cookie";

	public SameSiteCookieFilter() {
		//
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		//
	}

	@Override
	public void destroy() {
		//
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest)) {
			throw new ServletException("Request is not an instance of HttpServletRequest");
		}

		if (!(response instanceof HttpServletResponse)) {
			throw new ServletException("Response is not an instance of HttpServletResponse");
		}

		chain.doFilter(request, new SameSiteResponseProxy((HttpServletResponse) response));
	}

	/**
	 * An implementation of the {@link HttpServletResponse} which adds the same-site
	 * flag to {@literal Set-Cookie} headers for the set of configured cookies.
	 */
	private class SameSiteResponseProxy extends HttpServletResponseWrapper {

		private final HttpServletResponse response;

		/**
		 * Constructor.
		 *
		 * @param resp the response to delegate to
		 */
		public SameSiteResponseProxy(HttpServletResponse resp) {
			super(resp);
			response = resp;
		}

		@Override
		public void sendError(final int sc) throws IOException {
			appendSameSite();
			super.sendError(sc);
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			appendSameSite();
			return super.getWriter();
		}

		@Override
		public void sendError(final int sc, final String msg) throws IOException {
			appendSameSite();
			super.sendError(sc, msg);
		}

		@Override
		public void sendRedirect(final String location) throws IOException {
			appendSameSite();
			super.sendRedirect(location);
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			appendSameSite();
			return super.getOutputStream();
		}

		/**
		 * Add the SameSite attribute to those cookies configured in the
		 * {@code sameSiteCookies} map iff they do not already contain the same-site
		 * flag. All other cookies are copied over to the response without modification.
		 */
		private void appendSameSite() {

			final Collection<String> cookieheaders = response.getHeaders(SET_COOKIE);

			boolean firstHeader = true;
			for (final String cookieHeader : cookieheaders) {

				if (!StringHelper.containsNonWhitespace(cookieHeader)) {
					continue;
				}

				List<HttpCookie> parsedCookies = null;
				try {
					// this parser only parses name and value, we only need the name.
					parsedCookies = HttpCookie.parse(cookieHeader);
				} catch (final IllegalArgumentException e) {
					// should not get here
					log.trace("Cookie header [{}] violates the cookie specification and will be ignored", cookieHeader);
				}

				if (parsedCookies == null || parsedCookies.size() != 1) {
					// should be one cookie
					continue;
				}

				appendSameSiteAttribute(cookieHeader, firstHeader);
				firstHeader = false;
			}
		}

		/**
		 * Append the SameSite cookie attribute with the specified samesite-value to the
		 * {@code cookieHeader} iff it does not already have one set.
		 *
		 * @param cookieHeader  the cookie header value.
		 * @param sameSiteValue the SameSite attribute value e.g. None, Lax, or Strict.
		 * @param first         is this the first Set-Cookie header.
		 */
		private void appendSameSiteAttribute(String cookieHeader, boolean first) {
			String sameSiteSetCookieValue = cookieHeader;
			// only add if does not already exist, else leave
			if (!cookieHeader.contains(SAMESITE_ATTRIBITE_NAME)) {
				sameSiteSetCookieValue = String.format("%s; %s", cookieHeader, SAMESITE_ATTRIBITE_NAME + "=Strict");
			}

			if (first) {
				response.setHeader(SET_COOKIE, sameSiteSetCookieValue);
			} else {
				response.addHeader(SET_COOKIE, sameSiteSetCookieValue);
			}
		}
	}
}