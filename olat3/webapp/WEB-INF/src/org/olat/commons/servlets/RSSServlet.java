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
 * <p>
 */

package org.olat.commons.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.rss.RSSUtil;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.notifications.PersonalRSSFeed;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Description:<BR>
 * Servlet that produces a personalized RSS feed of the users notifications.
 * <P>
 * Initial Date: Jan 11, 2005 2004
 * 
 * @author Florian Gnägi
 */
public class RSSServlet extends HttpServlet {

	public static final String DEFAULT_ENCODING = "UTF-8";

	private static int outputBufferSize = 2048;
	private static int inputBufferSize = 2048;
	// TODO:GW How shall we cache the feed?
	// private CacheWrapper timerCache;
	private static final OLog log = Tracing.createLoggerFor(RSSServlet.class);

	/**
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		log.info("init statics servlet");
		try {
			String bufSize = servletConfig.getInitParameter("input");
			inputBufferSize = Integer.parseInt(bufSize);
			bufSize = servletConfig.getInitParameter("output");
			outputBufferSize = Integer.parseInt(bufSize);
		} catch (Exception e) {
			log.warn("problem with config parameters for rss servlets:", e);
		}
		log.info("input buffer size: " + inputBufferSize);
		log.info("output buffer size: " + outputBufferSize);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		Tracing.setUreq(req);
		I18nManager.attachI18nInfoToThread(req);
		try {
			String method = req.getMethod();
			if (method.equals("GET")) {
				doGet(req, resp);
			} else {
				super.service(req, resp);
			}
		} catch (Exception e) {
			log.error("Exception while serving RSS feed::" + req.getPathInfo(), e);
		} finally {
			I18nManager.remove18nInfoFromThread();
			// consume the user request.
			Tracing.setUreq(null);
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		SyndFeed feed = null;
		Writer writer = null;

		try {
			String pathInfo = request.getPathInfo();
			if ((pathInfo == null) || (pathInfo.equals(""))) { return; // error
			}

			// pathInfo is like /personal/username/tokenid.rss
			if (pathInfo.indexOf(RSSUtil.RSS_PREFIX_PERSONAL) == 0) {
				feed = getPersonalFeed(pathInfo);
				if (feed == null) {
					DispatcherAction.sendNotFound(pathInfo, response);
					return;
				}
			} else {
				DispatcherAction.sendNotFound(pathInfo, response);
				return;
			}

			// OLAT-5400 and OLAT-5243 related: sending back the reply can take arbitrary long,
			// considering slow end-user connections for example - or a sudden death of the connection
			// on the client-side which remains unnoticed (network partitioning)
			DBFactory.getInstance().intermediateCommit();
			
			response.setBufferSize(outputBufferSize);

			String encoding = feed.getEncoding();
			if (encoding == null) {
				encoding = DEFAULT_ENCODING;
				if (log.isDebug()) {
					log.debug("Feed encoding::" + encoding);
				}
				log.warn("No encoding provided by feed::" + feed.getClass().getCanonicalName() + " Using utf-8 as default.");
			}
			response.setCharacterEncoding(encoding);
			response.setContentType("application/rss+xml");

			Date pubDate = feed.getPublishedDate();
			if (pubDate != null) {
				response.setDateHeader("Last-Modified", pubDate.getTime());
			}
			// TODO:GW Do we need this?
			// response.setContentLength(feed.get);

			writer = response.getWriter();
			SyndFeedOutput output = new SyndFeedOutput();
			output.output(feed, writer);

		} catch (FeedException e) {
			// throw olat exception for nice logging
			log.warn("Error when generating RSS stream for path::" + request.getPathInfo(), e);
			DispatcherAction.sendNotFound("none", response);
		} catch (Exception e) {
			log.warn("Unknown Exception in rssservlet", e);
			DispatcherAction.sendNotFound("none", response);
		} catch (Error e) {
			log.warn("Unknown Error in rssservlet", e);
			DispatcherAction.sendNotFound("none", response);
		} finally {
			try {
				if (writer != null) writer.close();
			} catch (IOException e) {
				// writer didn't close properly – ignore
			}
			
			DBFactory.getInstance(false).commitAndCloseSession();
		}
	}

	/**
	 * Creates a personal RSS document
	 * 
	 * @param pathInfo
	 * @return RssDocument
	 */
	private SyndFeed getPersonalFeed(String pathInfo) {
		// pathInfo is like /personal/username/tokenid/olat.rss
		int startIdName = RSSUtil.RSS_PREFIX_PERSONAL.length();
		int startIdToken = pathInfo.indexOf("/", RSSUtil.RSS_PREFIX_PERSONAL.length());
		String idName = pathInfo.substring(startIdName, startIdToken);
		int startUselessUri = pathInfo.indexOf("/", startIdToken + 1);
		String idToken = pathInfo.substring(startIdToken + 1, startUselessUri);

		// ---- check integrity and user authentication ----
		if (idName == null || idName.equals("")) { return null; }
		Identity identity = BaseSecurityManager.getInstance().findIdentityByName(idName);
		if (identity == null) {
			// error - abort
			return null;
		}
		// check if this is a valid authentication
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(identity, RSSUtil.RSS_AUTH_PROVIDER);
		if (auth == null) {
			// error, rss authentication not yet set. user must login first, then the
			// auth provider will be generated on the fly
			return null;
		}
		if (!auth.getCredential().equals(idToken)) {
			// error - wrong authentication
			return null;
		}

		// FIXME:GW Make some sensible verifications here (expire date etc.) and
		// return a cached file if it exists
		SyndFeed cachedFeed = null;
		if (cachedFeed != null) {
			// return the cache entry
			return cachedFeed;
		} else {
			// create rss feed for user notifications
			SyndFeed feed = new PersonalRSSFeed(identity, idToken);
			return feed;
		}
	}
}