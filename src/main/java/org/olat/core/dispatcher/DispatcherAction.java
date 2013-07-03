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
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.mapper.GlobalMapperRegistry;
import org.olat.core.dispatcher.mapper.MapperDispatcher;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.testutils.codepoints.server.Codepoint;
/**
 * Initial Date: 28.11.2003
 * 
 * @author Felix Jost
 */
public class DispatcherAction implements Dispatcher {
	
	private static OLog log = Tracing.createLoggerFor(DispatcherAction.class);
	// important: add trailing slashes for PATH_XXX
	private static String PATH_DEFAULT;
	/** Identifies requests for the mappingregistry */
	public static final String PATH_MAPPED = "/m/";
	/** Identifies requests for the global mappingregistry */
	public static final String PATH_GLOBAL_MAPPED = "/g/";
	/* default encoding */
	private static final String UTF8_ENCODING = "utf-8";

	
	/**
	 * incrementing an int is not atomar which needs to be synchronized
	 * AtomicInt provides the same without the need for syncing.
	 */
	private static AtomicInteger concurrentCounter = new AtomicInteger(0);

	private GlobalMapperRegistry gmr;
	/**
	 * set by spring
	 */
	private Map<String, Dispatcher> dispatchers;

	// brasato::remove!!
	// ............used by BusinessGroupMainRunController
	// ............used by DropboxScoringViewController
	// used by olat.basesecurity.AuthHelper.doLogin
	// used by olat.repository.RepoJumpInHandlerFactory.builRepositoryDispatchURI
	public static final String PATH_AUTHENTICATED = "/auth/";
	private static final String DOUBLE_SLASH = "//";

	// brasato::remove!!
	// olat.login.DMZController
	// olat.shibboleth13.ShibbolethDispatcher
	public static String getPathDefault(){
		return "/dmz/";
	}

	
	/**
	 * The main dispatcher.
	 */
	public DispatcherAction() {
		// Initialize global mapper for dynamically generated, globally named mappers
		gmr = GlobalMapperRegistry.getInstance();
	}
	

	/**
	 * Main method, called by OLATServlet.
	 * 
	 * @param request
	 * @param response
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String notusedhere) {
		Codepoint.codepoint(DispatcherAction.class, "execute-start");
		
		try {
			concurrentCounter.incrementAndGet();
			String pathInfo = request.getPathInfo();
			
			if (pathInfo == null) {
				// seems to depend on the servlet container if null or "/" is returned
				pathInfo = "/";
			}

			int sl = pathInfo.indexOf('/', 1);
			String sub;
			if (sl > 1) {
				//e.g. something like /dmz/ or /auth/
				sub = pathInfo.substring(0, sl + 1);
			} else {
				//e.g. something like /maintenance.html
				sub = pathInfo;
			}
			/*
			 * GLOBAL MAPPED and MAPPED PATHS
			 */
			if (pathInfo.startsWith(PATH_MAPPED)) {
				
 				// OLAT-5368: an intermediate commit is necessary here to close open transactions which could otherwise
 				// run into the 2 min timeout and cause errors. The code does a return after serving the file anyway
 				// and would do a commit right there as well - so this doesn't break the transaction semantics.
 				DBFactory.getInstance(false).intermediateCommit();
 				
 				// Session specific file mappers. When registered as cacheable
				// mappers the browser might cache the delivered ressources
				// using the last modified date.
				// mapped paths (per usersession) -> /m/...
				new MapperDispatcher().execute(request, response, subtractContextPath(request, pathInfo));
				return;
			} else if (pathInfo.startsWith(PATH_GLOBAL_MAPPED)) {
				// Dynamic files that can be cached by browsers based on last modified
				// date, but are dynamically created by the application
				gmr.execute(request, response, subtractContextPath(request, pathInfo));
				return;
			} else {
				
				// Dispatch to defaultconfig.xml and extconfig.xml configured paths
				// and also to named mappers -> /n/name...
				Dispatcher d = dispatchers.get(sub);
				if (d != null) {
					d.execute(request, response, request.getContextPath() + sub);// e.g. /olat/ + sub
					return;
				} else {
					// check if we have a root dispatcher that takes all
					// remaining requests
					d = dispatchers.get("/");
					if (d != null) {
						// the root context dispatcher takes everything else
						d.execute(request, response, request.getContextPath() + "/");
						return;
					}
				}
			}
			
			// no dispatcher found that matches - send a 404
			sendNotFound(sub, response);				
			return;

		} catch (Throwable e) {
			log.error("Exception in DispatcherAction", e);
			handleError();
			sendBadRequest(request.getPathInfo(), response);
		} finally {
		 try{
			concurrentCounter.decrementAndGet();

			DBFactory.getInstance(false).commitAndCloseSession();
			
			// reset user request for tracing - do this after db closing to provide logging to db closing
			Codepoint.codepoint(DispatcherAction.class, "execute-end");
		 } catch(Error er) {
				log.error("Uncaught Error in DispatcherAction.execute.finally.", er);
				throw er;
		 } catch(RuntimeException re) {
				log.error("Uncaught RuntimeException in DispatcherAction.execute.finally.", re);
				throw re;
		 } catch(Throwable th) {
				log.error("Uncaught Throwable in DispatcherAction.execute.finally.", th);
		 }
		}

	}

	/**
	 * As decoding a url is an expensive operation we try to minimize it here
	 * as most mapped pathes contain only ascii chars
	 * @param request
	 * @param pathInfo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String subtractContextPath(HttpServletRequest request, String pathInfo) throws UnsupportedEncodingException {
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
		redirectTo(response, WebappHelper.getServletContextPath() + PATH_DEFAULT);
	}
	
	public static final void redirectToMobile(HttpServletResponse response) {
		redirectTo(response, WebappHelper.getServletContextPath() + WebappHelper.getMobileContext());
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
			response.sendError(HttpServletResponse.SC_NOT_FOUND, url);
		} catch (IOException e) {
			log.error("Send 404 failed: url=" + url, e);
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
			response.sendError(HttpServletResponse.SC_FORBIDDEN, url);
		} catch (IOException e) {
			log.error("Send 403 failed: url=" + url, e);
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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, (url == null ? "n/a" : url));
		} catch (IOException e) {
			log.error("Send 400 failed: url=" + url, e);
		}
	}

	/**
	 * @return the number of requests currently handled
	 */
	public static int getConcurrentCounter() {
		return concurrentCounter.intValue();
	}

	public static void handleError() {
		if (log.isDebug()) log.debug("handleError : do rollback");
		DBFactory.getInstance().rollbackAndCloseSession();
	}


	/**
	 * @return
	 */
	/*public static String getPathDefault() {
		return PATH_DEFAULT;
	}*/
	

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

	/**
	 * used by spring
	 * @param defaultDispatcherName The defaultDispatcherName to set.
	 */
	public void setDefaultDispatcherName(String defaultDispatcherName) {
		PATH_DEFAULT = defaultDispatcherName;
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
	
}
