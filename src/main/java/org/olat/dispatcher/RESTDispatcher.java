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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.LoginModule;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.restapi.security.RestSecurityBean;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * Initial Date:  24.04.2009 <br>
 * @author patrickb
 */
@Service("restdispatcher")
public class RESTDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(RESTDispatcher.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private RestSecurityBean restSecurityBean;
	@Autowired
	private UserSessionManager userSessionManager;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		//
		// create a ContextEntries String which can be used to create a BusinessControl -> move to 
		//
		final String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		final String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		String[] split = restPart.split("/");
		if (split.length % 2 != 0) {
			//The URL is not a valid business path
			DispatcherModule.sendBadRequest(origUri, response);
			log.warn("URL is not valid: {}", restPart);
			return;
		}
		String businessPath = BusinessControlFactory.getInstance().formatFromSplittedURI(split);
		log.debug("REQUEST URI: {} PREFIX: {} Business path: {}", origUri,  restPart,  businessPath);
		
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
			log.warn("Error with business path: {}", origUri, e);
			return;
		}
		
		//
		// create the olat ureq and get an associated main window to spawn the "tab"
		//
		UserSession usess = userSessionManager.getUserSession(request);
		if(usess != null) {
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
			Tracing.setUserSession(usess);
		} else {
			DispatcherModule.sendForbidden(request.getPathInfo(), response);
			return;
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
			if(log.isDebugEnabled()){
				log.debug("Bad Request {}", request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		// Do auto-authenticate if url contains a X-OLAT-TOKEN Single-Sign-On REST-Token
		String xOlatToken = ureq.getParameter(RestSecurityHelper.SEC_TOKEN);
		if (xOlatToken != null) {
			// Lookup identity that is associated with this token
			Identity restIdentity = restSecurityBean.getIdentity(xOlatToken);			
			Tracing.setIdentity(restIdentity);
			if (restIdentity == null) {
				log.debug("Found SSO token {} in url, but token is not bound to an identity", RestSecurityHelper.SEC_TOKEN);
			} else {
				log.debug("Found SSO token {} in url which is bound to identity:: {}", RestSecurityHelper.SEC_TOKEN, restIdentity.getKey());
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
						securityManager.setIdentityLastLogin(restIdentity);
					} else {
						//error, redirect to login screen
						DispatcherModule.redirectToDefaultDispatcher(response);
					}
				} else if (Windows.getWindows(usess).getChiefController(ureq) == null) {
					redirectAuthenticatedTo(usess, ureq, encodedRestPart);
					return;
				}
			}
		}
		
		boolean auth = usess.isAuthenticated();
		if (auth) {
			String invitationAccess = ureq.getParameter(AuthenticatedDispatcher.INVITATION);
			if (invitationAccess != null && invitationModule.isInvitationEnabled()) {
				Identity identity = usess.getIdentity();
				Invitation invitation = invitationService.findInvitation(invitationAccess);
				if(invitation != null && invitation.getStatus() == InvitationStatusEnum.active
						&& identity != null && identity.equals(invitation.getIdentity())) {
					invitationService.acceptInvitation(invitation, usess.getIdentity());
					dbInstance.commit();// Make sure membership is saved before redirect
				}
			}
			redirectAuthenticatedTo(usess, ureq, encodedRestPart);
		} else {
			//prepare for redirect
			setBusinessPathInUserSession(usess, businessPath, ureq.getParameter(WINDOW_SETTINGS));
			String invitationAccess = ureq.getParameter(AuthenticatedDispatcher.INVITATION);
			if (invitationAccess != null && invitationModule.isInvitationEnabled()) {
			// try to log in as anonymous
				// use the language from the lang parameter if available, otherwise use the system default locale
				Locale guestLoc = getLang(ureq);
				int loginStatus = AuthHelper.doInvitationLogin(invitationAccess, ureq, guestLoc);
				if (loginStatus == AuthHelper.LOGIN_OK) {
					Identity invite = usess.getIdentity();
					securityManager.setIdentityLastLogin(invite);					
					//logged in as invited user, continue
					ServletUtil.serveResource(request, response, ureq.getDispatchResult().getResultingMediaResource());
				} else if (loginStatus == AuthHelper.LOGIN_REGISTER) {
					redirectRegister(response);
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
				} else if (guestAccess.equals(AuthenticatedDispatcher.TRUE)) {
					// try to log in as anonymous
					// use the language from the lang parameter if available, otherwise use the system default locale
					Locale guestLoc = getLang(ureq);
					int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
					if ( loginStatus == AuthHelper.LOGIN_OK) {
						//logged in as anonymous user, continue
						ServletUtil.serveResource(request, response, ureq.getDispatchResult().getResultingMediaResource());
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
	
	private void redirectAuthenticatedTo(UserSession usess, UserRequest ureq, String encodedRestPart) {
		String url = WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED + encodedRestPart;
		if(usess != null && !ureq.getHttpReq().isRequestedSessionIdFromCookie()) {
			url += ";jsessionid=" + usess.getSessionInfo().getSession().getId();
		}
		DispatcherModule.redirectTo(ureq.getHttpResp(), url + "?invitation=3142d595-02ed-4085-a1e6-78f681136a66");
	}
	
	private void redirectRegister(HttpServletResponse response) {
		try {
			response.sendRedirect(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + "invitationregister/");
		} catch (IOException e) {
			log.error("Redirect failed: url={}{}", WebappHelper.getServletContextPath(), DispatcherModule.getPathDefault(),e);
		}
	}
	
	/**
	 * The method allows for a finite sets of business path to redirect to the DMZ
	 * @param usess
	 * @param businessPath
	 */
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
}
