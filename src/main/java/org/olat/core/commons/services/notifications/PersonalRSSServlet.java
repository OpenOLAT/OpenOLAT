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
*/

package org.olat.core.commons.services.notifications;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

/**
 * Description:<BR>
 * Servlet that produces a personalized RSS feed of the users notifications.
 * <P>
 * Initial Date: Jan 11, 2005 2004
 * 
 * @author Florian Gnägi
 */
public class PersonalRSSServlet extends HttpServlet {
	
	private static final long serialVersionUID = -674630331334472714L;
	private static final Logger log = Tracing.createLoggerFor(PersonalRSSServlet.class);
	public static final String DEFAULT_ENCODING = "UTF-8";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		Tracing.setHttpRequest(req);
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
			Tracing.clearHttpRequest();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos, "utf-8");
				OutputStream ros = response.getOutputStream()) {
			String pathInfo = request.getPathInfo();
			if ((pathInfo == null) || (pathInfo.equals(""))) {
				return; // error
			}
			SyndFeed feed = null;
			// pathInfo is like /personal/username/tokenid.rss
			if (pathInfo.indexOf(PersonalRSSUtil.RSS_PREFIX_PERSONAL) == 0) {
				feed = getPersonalFeed(pathInfo);
				if (feed == null) {
					DispatcherModule.sendNotFound(pathInfo, response);
					return;
				}
			} else {
				DispatcherModule.sendNotFound(pathInfo, response);
				return;
			}

			// OLAT-5400 and OLAT-5243 related: sending back the reply can take arbitrary long,
			// considering slow end-user connections for example - or a sudden death of the connection
			// on the client-side which remains unnoticed (network partitioning)
			DBFactory.getInstance().intermediateCommit();
			
			response.setBufferSize(response.getBufferSize());

			String encoding = feed.getEncoding();
			if (encoding == null) {
				encoding = DEFAULT_ENCODING;
				if (log.isDebugEnabled()) {
					log.debug("Feed encoding::{}", encoding);
				}
				log.warn("No encoding provided by feed::{} Using utf-8 as default.", feed.getClass().getCanonicalName());
			}
			response.setCharacterEncoding(encoding);
			response.setContentType("application/rss+xml");

			Date pubDate = feed.getPublishedDate();
			if (pubDate != null) {
				response.setDateHeader("Last-Modified", pubDate.getTime());
			}
			
			SyndFeedOutput output = new SyndFeedOutput();
			String feedString = output.outputString(feed);
			osw.write(feedString);
			osw.close();
			
			response.setContentLength(baos.size());
			ros.write(baos.toByteArray());
		} catch (FeedException e) {
			// throw olat exception for nice logging
			log.warn("Error when generating RSS stream for path::" + request.getPathInfo(), e);
			DispatcherModule.sendNotFound("none", response);
		} catch (Exception e) {
			log.warn("Unknown Exception in rssservlet", e);
			DispatcherModule.sendNotFound("none", response);
		} catch (Error e) {
			log.warn("Unknown Error in rssservlet", e);
			DispatcherModule.sendNotFound("none", response);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
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
		int startIdName = PersonalRSSUtil.RSS_PREFIX_PERSONAL.length();
		int startIdToken = pathInfo.indexOf("/", PersonalRSSUtil.RSS_PREFIX_PERSONAL.length());
		String idName = pathInfo.substring(startIdName, startIdToken);
		int startUselessUri = pathInfo.indexOf("/", startIdToken + 1);
		String idToken = pathInfo.substring(startIdToken + 1, startUselessUri);

		// ---- check integrity and user authentication ----
		if (idName == null || idName.equals("")) {
			return null;
		}
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.findIdentityByName(idName);
		if (identity == null) {
			// error - abort
			return null;
		}
		// check if this is a valid authentication
		Authentication auth = securityManager.findAuthentication(identity, PersonalRSSUtil.RSS_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
		if (auth == null) {
			// error, rss authentication not yet set. user must login first, then the
			// auth provider will be generated on the fly
			return null;
		}
		if (!auth.getCredential().equals(idToken)) {
			// error - wrong authentication
			return null;
		}

		// create rss feed for user notifications
		return new PersonalRSSFeed(identity);
	}
}