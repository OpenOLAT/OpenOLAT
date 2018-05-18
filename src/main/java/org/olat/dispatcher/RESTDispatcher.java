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
package org.olat.dispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.user.delete.service.UserDeletionManager;
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
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.LoginModule;
import org.olat.restapi.security.RestSecurityBean;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * Description:<br>
 * Entry point for Resource URL's which are a replacement for the jumpIn / Go Repo style URL's. The assumption is, that the URL here
 * set up from a list of BusinessControls containing a (type/resource)name and an (type/resource)id of type long.</br>
 * e.g. [RepoyEntry:12323123][CourseNode:2341231456][message:123123][blablup:555555] which is mapped to</br>
 * /RepoyEntry/12323123/CourseNode/2341231456/message/123123/blablup/555555/</p>
 * This dispatcher does the reverse mapping and creation of a list of BusinessControls which can be used to activate/spawn the Controller.
 * The same mechanism is used for lucene search engine and the activation of search results.
 * <p>
 * This dispatcher supports also a simple single sign-on-mechanism (SS). If an URL contains the parameter X-OLAT-TOKEN, the
 * RestSecurityBean will be used to look up the associated user. You can use the REST API to create such a X-OLAT-TOKEN or 
 * replace the RestSecurityBean with your own implementation that creates the tokens. Please refere to the REST API documentation
 * on how to create the X-OLAT-TOKEN
 * <br />
 * Example: [RepoyEntry:12323123][CourseNode:2341231456][message:123123][blablup:555555]?X-OLAT-TOKEN=xyz
 * <P>
 * TODO:pb:2009-06-02: (1) Check for Authenticated Session, otherwise send over login page (2) UZHDisparcher has a security check for
 * use of SSL -> introduce also here or maybe bring the check into webapphelper.
 * <P>
 * Initial Date:  24.04.2009 <br>
 * @author patrickb
 */
public class RESTDispatcher implements Dispatcher {
	private static final OLog log = Tracing.createLoggerFor(RESTDispatcher.class);

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		//
		// create a ContextEntries String which can be used to create a BusinessControl -> move to 
		//
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		String[] split = restPart.split("/");
		if (split.length % 2 != 0) {
			// assert(split.length % 2 == 0);
			//The URL is not a valid business path
			DispatcherModule.sendBadRequest(origUri, response);
			log.warn("URL is not valid: "+restPart);
			return;
		}
		String businessPath = BusinessControlFactory.getInstance().formatFromSplittedURI(split);
		if(log.isDebug()) {
			log.debug("REQUEST URI: " + origUri);
			log.debug("REQUEST PREFIX " + restPart);
			log.debug("calc buspath " + businessPath);
		}
		
		//check if the businesspath is valid
		try {
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(businessPath);
			if(!bc.hasContextEntry()) {
				//The URL is not a valid business path
				DispatcherModule.sendBadRequest(origUri, response);
				return;
			}
		} catch (Exception e) {
			DispatcherModule.sendBadRequest(origUri, response);
			log.warn("Error with business path: " + origUri, e);
			return;
		}
		
		//
		// create the olat ureq and get an associated main window to spawn the "tab"
		//
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		if(usess != null) {
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
		}
		UserRequest ureq = null;
		try {
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebug()){
				log.debug("Bad Request "+request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		//XX:GUIInterna.setLoadPerformanceMode(ureq);		
		
		// Do auto-authenticate if url contains a X-OLAT-TOKEN Single-Sign-On REST-Token
		String xOlatToken = ureq.getParameter(RestSecurityHelper.SEC_TOKEN);
		if (xOlatToken != null) {
			// Lookup identity that is associated with this token
			RestSecurityBean securityBean = (RestSecurityBean)CoreSpringFactory.getBean(RestSecurityBean.class);
			Identity restIdentity = securityBean.getIdentity(xOlatToken);			
			// 
			if(log.isDebug()) {
				if (restIdentity == null)
					log.debug("Found SSO token " + RestSecurityHelper.SEC_TOKEN + " in url, but token is not bound to an identity");
				else
					log.debug("Found SSO token " + RestSecurityHelper.SEC_TOKEN + " in url which is bound to identity::" + restIdentity.getKey());
			}
			//
			if (restIdentity != null) {
				// Test if the current OLAT session does already belong to this user.
				// The session could be an old session from another user or it could
				// belong to this user but miss the window object because so far it was
				// a head-less REST session. REST sessions initially have a small
				// timeout, however OLAT does set the standard session timeout on each
				// UserSession.getSession() request. This means, the normal session
				// timeout is set in the redirect request that will happen immediately
				// after the REST dispatcher finishes. No need to change it here.
				if (!usess.isAuthenticated() || !restIdentity.equalsByPersistableKey(usess.getIdentity())) {
					// Re-authenticate user session for this user and start a fresh
					// standard OLAT session
					int loginStatus = AuthHelper.doLogin(restIdentity, RestSecurityHelper.SEC_TOKEN, ureq);			
					if (loginStatus == AuthHelper.LOGIN_OK) {
						//fxdiff: FXOLAT-268 update last login date and register active user
						UserDeletionManager.getInstance().setIdentityAsActiv(restIdentity);
					} else {
						//error, redirect to login screen
						DispatcherModule.redirectToDefaultDispatcher(response);
					}
				} else if (Windows.getWindows(usess).getChiefController() == null) {
					// Session is already available, but no main window (Head-less REST
					// session). Only create the base chief controller and the window
					Window currentWindow = AuthHelper.createAuthHome(ureq).getWindow();
					//the user is authenticated successfully with a security token, we can set the authenticated path
					currentWindow.setUriPrefix(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED);
					Windows ws = Windows.getWindows(ureq);
					ws.registerWindow(currentWindow);
					// no need to call setIdentityAsActive as this was already done by RestApiLoginFilter...
				}
			}
		}
		
		boolean auth = usess.isAuthenticated();
		if (auth) {
			if (Windows.getWindows(usess).getChiefController() == null) {
				// Session is already available, but no main window (Head-less REST
				// session). Only create the base chief controller and the window
				setBusinessPathInUserSession(usess, businessPath, ureq.getParameter(WINDOW_SETTINGS));

				AuthHelper.createAuthHome(ureq);
				String url = getRedirectToURL(usess) + ";jsessionid=" + usess.getSessionInfo().getSession().getId();
				DispatcherModule.redirectTo(response, url);
			} else {
				//redirect to the authenticated dispatcher which support REST url
				String url = WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED + encodedRestPart;
				DispatcherModule.redirectTo(response, url);
			}
		} else {
			//prepare for redirect
			LoginModule loginModule = CoreSpringFactory.getImpl(LoginModule.class);
			setBusinessPathInUserSession(usess, businessPath, ureq.getParameter(WINDOW_SETTINGS));
			String invitationAccess = ureq.getParameter(AuthenticatedDispatcher.INVITATION);
			if (invitationAccess != null && loginModule.isInvitationEnabled()) {
			// try to log in as anonymous
				// use the language from the lang paramter if available, otherwhise use the system default locale
				Locale guestLoc = getLang(ureq);
				int loginStatus = AuthHelper.doInvitationLogin(invitationAccess, ureq, guestLoc);
				if ( loginStatus == AuthHelper.LOGIN_OK) {
					Identity invite = usess.getIdentity();
					//fxdiff: FXOLAT-268 update last login date and register active user
					UserDeletionManager.getInstance().setIdentityAsActiv(invite);					
					//logged in as invited user, continue
					String url = getRedirectToURL(usess);
					DispatcherModule.redirectTo(response, url);
				} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
					DispatcherModule.redirectToServiceNotAvailable(response);
				} else {
					//error, redirect to login screen
					DispatcherModule.redirectToDefaultDispatcher(response); 
				}
			} else {
				String guestAccess = ureq.getParameter(AuthenticatedDispatcher.GUEST);
				if (guestAccess == null || !loginModule.isGuestLoginLinksEnabled()) {
					DispatcherModule.redirectToDefaultDispatcher(response);
					return;
				} else if (guestAccess.equals(AuthenticatedDispatcher.TRUE)) {
					// try to log in as anonymous
					// use the language from the lang paramter if available, otherwhise use the system default locale
					Locale guestLoc = getLang(ureq);
					int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
					if ( loginStatus == AuthHelper.LOGIN_OK) {
						//logged in as anonymous user, continue
						String url = getRedirectToURL(usess);
						DispatcherModule.redirectTo(response, url);
					} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
						DispatcherModule.redirectToServiceNotAvailable(response);
					} else {
						//error, redirect to login screen
						DispatcherModule.redirectToDefaultDispatcher(response); 
					}
				}
			}
		}
	}
	
	/**
	 * The method allows for a finite sets of business path to redirect to the DMZ
	 * @param usess
	 * @param businessPath
	 */
	//fxdiff FXOLAT-113: business path in DMZ
	private void setBusinessPathInUserSession(UserSession usess, String businessPath, String options) {
		if(StringHelper.containsNonWhitespace(businessPath) && usess != null) {
			if(businessPath.startsWith("[changepw:0]") || "[registration:0]".equals(businessPath) || "[guest:0]".equals(businessPath)
					|| "[browsercheck:0]".equals(businessPath) || "[accessibility:0]".equals(businessPath) || "[about:0]".equals(businessPath)) {
				usess.putEntryInNonClearedStore(DMZDispatcher.DMZDISPATCHER_BUSINESSPATH, businessPath);
			} else {
				usess.putEntryInNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_BUSINESSPATH, businessPath);
			}
		}
		if(options != null && usess != null) {
			usess.putEntryInNonClearedStore(WINDOW_SETTINGS, options);
		}
	}
	
	private Locale getLang(UserRequest ureq) {
	// try to log in as anonymous
		// use the language from the lang parameter if available, otherwise use the system default locale
		String guestLang = ureq.getParameter("language");
		if (guestLang == null) {
			// support for legacy lang parameter
			guestLang = ureq.getParameter("lang");
		}
		Locale guestLoc;
		if (guestLang == null) {
			guestLoc = I18nModule.getDefaultLocale();
		} else {
			guestLoc = I18nManager.getInstance().getLocaleOrDefault(guestLang);
		}
		return guestLoc;
	}
	
	private String getRedirectToURL(UserSession usess) {
		ChiefController cc = Windows.getWindows(usess).getChiefController();
		Window w = cc.getWindow();

		URLBuilder ubu = new URLBuilder(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED, w.getInstanceId(), String.valueOf(w.getTimestamp()));
		StringOutput sout = new StringOutput(30);
		ubu.buildURI(sout, null, null);
		
		return sout.toString();
	}
}
