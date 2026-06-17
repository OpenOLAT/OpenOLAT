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
package org.olat.modules.selectus.dispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.servlets.RequestAbortedException;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Initial date: 11.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbstractRecruitingDispatcher implements Dispatcher {
	private static final Logger log = Tracing.createLoggerFor(AbstractRecruitingDispatcher.class);
	
	public static final String DISPATCHER_SOURCE = "rt-dispatcher";
	
	private final String name;
	
	public AbstractRecruitingDispatcher(String name) {
		this.name = name;
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		//
		// create a ContextEntries String which can be used to create a BusinessControl -> move to 
		//
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String restPart = origUri.substring(uriPrefix.length());
		try {
			restPart = URLDecoder.decode(restPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}

		// create the olat ureq and get an associated main window to spawn the "tab"
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		if(usess != null) {
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
			usess.putEntryInNonClearedStore(DISPATCHER_SOURCE, name);
		}
		UserRequest ureq = null;
		try {
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(RequestAbortedException | NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebugEnabled()){
				log.debug("Bad Request {}", request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		// try to log in as anonymous
		// use the language from the language parameter if available, otherwise use the system default locale
		Locale guestLoc = getLang(ureq);
		int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
		if ( loginStatus == AuthHelper.LOGIN_OK) {
			//logged in as anonymous user, continue
			String url = getRedirectToURL(usess, ureq);
			DispatcherModule.redirectTo(response, url);
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
			DispatcherModule.redirectToServiceNotAvailable(response);
		} else {
			//error, redirect to login screen
			DispatcherModule.redirectToDefaultDispatcher(response); 
		}
	}
	
	protected Locale getLang(UserRequest ureq) {
		String guestLang = ureq.getParameter("lang");
		Locale locale;
		if (guestLang == null) {
			locale = PositionLocaleNegotiator.getPreferedLocale(ureq);
		} else {
			locale = I18nManager.getInstance().getLocaleOrDefault(guestLang);
		}
		return locale;
	}
	
	private String getRedirectToURL(UserSession usess, UserRequest ureq) {
		ChiefController cc = Windows.getWindows(usess).getChiefController(ureq);
		Window w = cc.getWindow();

		URLBuilder ubu = new URLBuilder(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED, w.getInstanceId(), w.getTimestamp(), w.getCsrfToken());
		try(StringOutput sout = new StringOutput(30)) {
			ubu.buildURI(sout, null, null);
			return sout.toString();
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}
}
