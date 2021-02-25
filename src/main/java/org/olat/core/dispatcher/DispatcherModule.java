/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.dispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Felix Jost
 */
public class DispatcherModule {
	
	private static final Logger log = Tracing.createLoggerFor(DispatcherModule.class);
	
	/** Identifies requests for the DMZ  */
	private static String pathDefault;
	/** Identifies requests for the mapper registry */
	public static final String PATH_MAPPED = "/m/";
	/** Identifies requests for the global mapper registry */
	public static final String PATH_GLOBAL_MAPPED = "/g/";
	/** Identifies requests for webdav */
	public static final String WEBDAV_PATH = "/webdav/";
	/** Identifies requests for auth */
	public static final String PATH_AUTHENTICATED = "/auth/";
	/** default encoding */
	private static final String UTF8_ENCODING = "utf-8";

	/**
	 * set by spring
	 */
	private Map<String, Dispatcher> dispatchers;

	private static final String DOUBLE_SLASH = "//";
	
	public static String getLegacyUriPrefix(HttpServletRequest request) {
		return request.getContextPath() + getFirstPath(request);
	}
	
	public static String getFirstPath(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) return "/";

		int sl = pathInfo.indexOf('/', 1);
		String sub;
		if (sl > 1) {
			//e.g. something like /dmz/ or /auth/
			sub = pathInfo.substring(0, sl + 1);
		} else {
			//e.g. something like /maintenance.html
			sub = pathInfo;
		}
		return sub;
	}

	/**
	 * As decoding a url is an expensive operation we try to minimize it here
	 * as most mapped pathes contain only ascii chars
	 * @param request
	 * @param pathInfo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String subtractContextPath(HttpServletRequest request) throws UnsupportedEncodingException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			// seems to depend on the servlet container if null or "/" is returned
			pathInfo = "/";
		}
		
		String requestUri = request.getRequestURI();
		//context path set - normal case
		if (WebappHelper.getServletContextPath().length() > 0) {
			String requestUriPart = requestUri.substring(WebappHelper.getServletContextPath().length());
			//remove context path and if length is same no need to decode
			if (requestUriPart.length() == pathInfo.length()) {
				return requestUriPart;
			}
		} else if (requestUri.length() == pathInfo.length()) {
			//no context path set and only ascii chars, no need to decode uri
			return requestUri;
		}
		// Fix messy URL's as it is done in pathInfo by the servlet container.
		if (requestUri.indexOf(DOUBLE_SLASH) != -1) requestUri = requestUri.replaceAll(DOUBLE_SLASH, "/");
		requestUri = URLDecoder.decode(requestUri, UTF8_ENCODING);
		if (WebappHelper.getServletContextPath().length() > 0) {
			return requestUri.substring(WebappHelper.getServletContextPath().length());
		}
		return requestUri;
	}

	/**
	 * Redirect to login page.
	 * 
	 * @param response
	 */
	public static final void redirectToDefaultDispatcher(HttpServletResponse response) {
		redirectTo(response, WebappHelper.getServletContextPath() + getPathDefault());
	}

	/**
	 * Generic redirect method.
	 * 
	 * @param response
	 * @param url
	 */
	public static final void redirectTo(HttpServletResponse response, String url) {
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			log.error("Redirect failed: url=" + url, e);
		}
	}

	/**
	 * Sends a HTTP 404 response.
	 * 
	 * @param url
	 * @param response
	 */
	public static final void sendNotFound(String url, HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			log.error("Send 404 failed: url=" + url, e);
		}
	}
	
	/**
	 * Send a 404 without log.
	 * 
	 * @param response The HTTP response
	 */
	public static final void sendNotFound(HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			//
		}
	}

	/**
	 * Sends a HTTP 403 response.
	 * 
	 * @param url
	 * @param response
	 */
	public static final void sendForbidden(String url, HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException e) {
			log.error("Send 403 failed: url=" + url, e);
		}
	}
	
	/**
	 * Send a 403 error without log.
	 * 
	 * @param response The HTTP response
	 */
	public static final void sendForbidden(HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException e) {
			//
		}
	}

	/**
	 * Sends a HTTP 400 response.
	 * 
	 * @param url
	 * @param response
	 */
	public static final void sendBadRequest(String url, HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (IOException e) {
			log.error("Send 400 failed: url=" + url, e);
		}
	}
	
	/**
	 * Set the status 204 No content to the specified response.
	 * 
	 * @param url The url (for log purpose)
	 * @param response The HTTP response
	 */
	public static final void setNotContent(String url, HttpServletResponse response) {
		try {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} catch (Exception e) {
			log.error("Set 204 failed: url={}", url, e);
		}
	}
	
	public static final void setServerError(HttpServletResponse response) {
		try {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Set 500 failed", e);
		}
	}
	
	public static final void sendServerError(HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			//
		}
	}
	
	/**
	 * Sent to standard 503 if not available
	 * @param response
	 */
	public static void redirectToServiceNotAvailable(HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} catch (IOException e) {
			log.error("Send 503 failed", e);
		}
	}

	public static void handleError() {
		if (log.isDebugEnabled()) log.debug("handleError : do rollback");
		DBFactory.getInstance().rollbackAndCloseSession();
	}
	
	public Map<String, Dispatcher> getDispatchers() {
		return dispatchers;
	}

	/**
	 * [key, value] pairs<br>
	 * <ul>
	 * <li>key is a String with the dispatcher path, e.g. /dmz/ or /auth/ or /webstat.html</li>
	 * <li>value is of type <code>Dispatcher</code></li>
	 * </ul>
	 * 
	 * called only by spring
	 * @param dispatchers The dispatchers to set.
	 */
	public void setDispatchers(Map<String, Dispatcher> dispatchers) {
		this.dispatchers = dispatchers;
	}

	public static String getPathDefault() {
		return pathDefault;
	}

	public void setDefaultPath(String path) {
		pathDefault = path;
	}
	
	
}
