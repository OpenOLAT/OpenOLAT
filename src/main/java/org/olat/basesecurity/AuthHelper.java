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

package org.olat.basesecurity;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.fullWebApp.BaseFullWebappControllerParts;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.OlatLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.login.AuthBFWCParts;
import org.olat.login.GuestBFWCParts;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: <br>
 *
 * @author Felix Jost
 */
public class AuthHelper {

	public static final String ATTRIBUTE_AUTHPROVIDER = "authprovider";
	public static final String ATTRIBUTE_LANGUAGE = "language";
	public static final String ATTRIBUTE_IS_REST = "isrest";
	public static final String ATTRIBUTE_IS_WEBDAV = "iswebdav";
	public static final String ATTRIBUTE_INVITATION = "invitation";
	/**
	 * <code>LOGOUT_PAGE</code>
	 */
	public static final int LOGIN_OK = 0;
	public static final int LOGIN_FAILED = 1;
	public static final int LOGIN_DENIED = 2;
	public static final int LOGIN_NOTAVAILABLE = 3;
	public static final int LOGIN_INACTIVE = 4;
	public static final int LOGIN_REGISTER = 5;
	public static final int LOGIN_NEED_LOGIN = 6;
	

	private static final int MAX_SESSION_NO_LIMIT = 0;


	/** whether or not requests to dmz (except those coming via 'switch-to-node' cluster feature) are
	 * rejected hence resulting the browser to go to another node.
	 * Note: this is not configurable currently as it's more of a runtime choice to change this to true
	 */
	private static boolean rejectDMZRequests = false;

	private static boolean loginBlocked = false;
	private static int maxSessions = MAX_SESSION_NO_LIMIT;

	private static final Logger log = Tracing.createLoggerFor(AuthHelper.class);

	/**
	 * Used by DMZDispatcher to do regular logins and by ShibbolethDispatcher
	 * which is somewhat special because logins are handled asynchronuous ->
	 * therefore a dedicated dispatcher is needed which also has to have access to
	 * the doLogin() method.
	 *
	 * @param identity
	 * @param authProvider
	 * @param ureq
	 * @return True if success, false otherwise.
	 */
	public static int doLogin(Identity identity, String authProvider, UserRequest ureq) {
		int initializeStatus = initializeLogin(identity, authProvider, ureq, false);
		if (initializeStatus != LOGIN_OK) {
			return initializeStatus; // login not successful
		}

		// do logging
		ThreadLocalUserActivityLogger.log(OlatLoggingAction.OLAT_LOGIN, AuthHelper.class, LoggingResourceable.wrap(identity));

		// successful login, reregister window
		ChiefController occ;
		if(ureq.getUserSession().getRoles().isGuestOnly()){
			occ = createGuestHome(ureq);
		}else{
			occ = createAuthHome(ureq);
		}

		Window currentWindow = occ.getWindow();
		currentWindow.setUriPrefix(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED);
		Windows.getWindows(ureq).registerWindow(occ);
		ureq.overrideWindowComponentID(currentWindow.getDispatchID());

		RedirectMediaResource redirect;
		String redirectTo = (String)ureq.getUserSession().getEntry("redirect-bc");
		if(StringHelper.containsNonWhitespace(redirectTo)) {
			String url = WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED + redirectTo + "?oow=" + currentWindow.getDispatchID();
			redirect = new RedirectMediaResource(url);
		} else {
			// redirect to AuthenticatedDispatcher
			// IMPORTANT: windowID has changed due to re-registering current window -> do not use ureq.getWindowID() to build new URLBuilder.
			URLBuilder ubu = new URLBuilder(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED,
					currentWindow.getInstanceId(), "1", currentWindow.getCsrfToken());
			try(StringOutput sout = new StringOutput(30)) {
				ubu.buildURI(sout, null, null);
				redirect = new RedirectMediaResource(sout.toString());
			} catch(IOException e) {
				log.error("", e);
				redirect = null;
			}
		}
		ureq.getDispatchResult().setResultingMediaResource(redirect);
		return LOGIN_OK;
	}

	/**
	 *
	 * @param identity
	 * @param authProvider
	 * @param ureq
	 * @param Is login via REST API?
	 * @return
	 */
	public static int doHeadlessLogin(Identity identity, String authProvider, UserRequest ureq, boolean rest) {
		int initializeStatus = initializeLogin(identity, authProvider, ureq, rest);
		if (initializeStatus != LOGIN_OK) {
			return initializeStatus; // login not successful
		}
		// Set session info to reflect the REST headless login
		UserSession usess = ureq.getUserSession();
		usess.getSessionInfo().setREST(true);
		usess.getIdentityEnvironment().getAttributes().put(ATTRIBUTE_IS_REST, "true");
		//
		ThreadLocalUserActivityLogger.log(OlatLoggingAction.OLAT_LOGIN, AuthHelper.class, LoggingResourceable.wrap(identity));
		return LOGIN_OK;
	}

	/**
	 * Create a base chief controller for the current anonymous user request
	 * and initialize the first screen after login. Note, the user request
	 * must be authenticated, but as an anonymous user and not a known user.
	 *
	 * @param ureq The authenticated user request.
	 * @return The chief controller
	 */
	private static ChiefController createGuestHome(UserRequest ureq) {
		if (!ureq.getUserSession().isAuthenticated()) throw new AssertException("not authenticated!");

		BaseFullWebappControllerParts guestSitesAndNav = new GuestBFWCParts();
		ChiefController cc = new BaseFullWebappController(ureq, guestSitesAndNav);
		Windows.getWindows(ureq.getUserSession()).registerWindow(cc);
		return cc;
	}

	/**
	 * Create a base chief controller for the current authenticated user request
	 * and initialize the first screen after login.
	 *
	 * @param ureq The authenticated user request.
	 * @return The chief controller
	 */
	public static ChiefController createAuthHome(UserRequest ureq) {
		if (!ureq.getUserSession().isAuthenticated()) throw new AssertException("not authenticated!");

		BaseFullWebappControllerParts authSitesAndNav = new AuthBFWCParts();
		return new BaseFullWebappController(ureq, authSitesAndNav);
	}

	/**
	 * Logs in as anonymous user using the given language key. If the current
	 * installation does not support this language, the systems default language
	 * is used instead
	 *
	 * @param ureq The user request
	 * @param lang The language of the anonymous user or null if system default should be used
	 * @return true if login was successful, false otherwise
	 */
	public static int doAnonymousLogin(UserRequest ureq, Locale locale) {
		Collection<String> supportedLanguages = CoreSpringFactory.getImpl(I18nModule.class).getEnabledLanguageKeys();
		if ( locale == null || ! supportedLanguages.contains(locale.toString()) ) {
			locale = I18nModule.getDefaultLocale();
		}
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity guestIdent = securityManager.getAndUpdateAnonymousUserForLanguage(locale);
		Roles guestRoles = securityManager.getRoles(guestIdent);
		if(guestRoles.isGuestOnly()) {
			return doLogin(guestIdent, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
		}
		log.error("Guest account has user permissions: {}", guestIdent);
		return LOGIN_DENIED;
	}
	


	public static int doInvitationLogin(String invitationToken, UserRequest ureq, Locale locale) {
		InvitationService invitationService = CoreSpringFactory.getImpl(InvitationService.class);
		boolean hasPolicies = invitationService.hasInvitations(invitationToken);
		if(!hasPolicies) {
			return LOGIN_DENIED;
		}

		UserManager um = UserManager.getInstance();
		GroupDAO groupDao = CoreSpringFactory.getImpl(GroupDAO.class);
		Invitation invitation = invitationService.findInvitation(invitationToken);
		if(invitation == null || invitation.getStatus() != InvitationStatusEnum.active) {
			return LOGIN_DENIED;
		}
		UserSession usess = ureq.getUserSession();
		usess.putEntryInNonClearedStore(ATTRIBUTE_INVITATION, invitation);

		//check if identity exists
		Identity identity = invitation.getIdentity();
		if(identity == null) {
			identity = um.findUniqueIdentityByEmail(invitation.getMail());
		}
		if(identity != null) {
			OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
			if(organisationService.hasRole(identity, OrganisationRoles.user)) {
				//already a normal olat user, cannot be invited
				return LOGIN_DENIED;
			} else {
				if(!groupDao.hasRole(invitation.getBaseGroup(), identity, GroupRoles.invitee.name())) {
					groupDao.addMembershipTwoWay(invitation.getBaseGroup(), identity, GroupRoles.invitee.name());
					DBFactory.getInstance().commit();
				}
				if(invitation.isRegistration()) {
					BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
					Authentication authentication = securityManager.findAuthentication(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);
					if(authentication == null) {
						return LOGIN_REGISTER;	
					}
					// Make sure the membership are set
					invitationService.acceptInvitation(invitation, identity);
					return LOGIN_NEED_LOGIN;
				}

				int result = doLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
				if(ureq.getUserSession().getRoles().isInvitee()) {
					return result;
				}
				return LOGIN_DENIED;
			}
		}

		Collection<String> supportedLanguages = CoreSpringFactory.getImpl(I18nModule.class).getEnabledLanguageKeys();
		if ( locale == null || ! supportedLanguages.contains(locale.toString()) ) {
			locale = I18nModule.getDefaultLocale();
		}

		//invitation ok -> create a temporary user
		Identity invitee = invitationService.createIdentityFrom(invitation, locale);
		return doLogin(invitee, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
	}

	/**
	 * ONLY for authentication provider OLAT Authenticate Identity and do the
	 * necessary work. Returns true if successful, false otherwise.
	 *
	 * @param identity
	 * @param authProvider
	 * @param ureq
	 * @return boolean
	 */
	private static int initializeLogin(Identity identity, String authProvider, UserRequest ureq, boolean rest) {
		// continue only if user has login permission.
		if (identity == null) return LOGIN_FAILED;
		//test if a user may not logon, since he/she is in the PERMISSION_LOGON
		if (!BaseSecurityManager.getInstance().isIdentityLoginAllowed(identity, authProvider)) {
			if(identity != null && Identity.STATUS_INACTIVE.equals(identity.getStatus())) {
				log.info(Tracing.M_AUDIT, "was denied login because inactive: {}", identity);
				return LOGIN_INACTIVE;
			}
			log.info(Tracing.M_AUDIT, "was denied login");
			return LOGIN_DENIED;
		}
		UserSessionManager sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		// if the user sending the cookie did not log out and we are logging in
		// again, then we need to make sure everything is cleaned up. we cleanup in all cases.
		UserSession usess = ureq.getUserSession();
		// prepare for a new user: clear all the instance vars of the userSession
		// note: does not invalidate the session, since it is reused
		sessionManager.signOffAndClear(usess);
		// init the UserSession for the new User
		// we can set the identity and finish the log in process
		usess.setIdentity(identity);
		Tracing.setIdentity(identity);
		setRolesFor(identity, usess);

		// check if loginDenied or maxSession (only for non-admin)
		if ( (loginBlocked && !usess.getRoles().isAdministrator() && !usess.getRoles().isSystemAdmin())
				|| ( ((maxSessions != MAX_SESSION_NO_LIMIT) && (sessionManager.getUserSessionsCnt() >= maxSessions))
						&& !usess.getRoles().isAdministrator() && !usess.getRoles().isSystemAdmin())) {
			log.info(Tracing.M_AUDIT, "Login was blocked for identity={}, loginBlocked={} NbrOfSessions={}", usess.getIdentity().getKey(), loginBlocked, sessionManager.getUserSessionsCnt());
			sessionManager.signOffAndClear(usess);
			return LOGIN_NOTAVAILABLE;
		}

		//need to block the all things for assessment?
		if(usess.getRoles() != null && (usess.getRoles().isAdministrator() || usess.getRoles().isSystemAdmin())) {
			usess.setAssessmentModes(Collections.<TransientAssessmentMode>emptyList());
		} else {
			AssessmentModule assessmentModule = CoreSpringFactory.getImpl(AssessmentModule.class);
			if(assessmentModule.isAssessmentModeEnabled()) {
				AssessmentModeManager assessmentManager = CoreSpringFactory.getImpl(AssessmentModeManager.class);
				List<AssessmentMode> modes = assessmentManager.getAssessmentModeFor(identity);
				if(modes.isEmpty()) {
					usess.setAssessmentModes(Collections.<TransientAssessmentMode>emptyList());
				} else {
					usess.setAssessmentModes(TransientAssessmentMode.create(modes));
				}
			}
		}

		//set the language
		usess.setLocale( I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage()) );
		// calculate session info and attach it to the user session
		setSessionInfoFor(identity, usess, authProvider, ureq, rest);
		//confirm signedOn
		sessionManager.signOn(usess);
		// set users web delivery mode
		Windows.getWindows(ureq).getWindowManager().setAjaxWanted(ureq);
		// update web delivery mode in session info
		usess.getSessionInfo().setWebModeFromUreq(ureq);
		return LOGIN_OK;
	}

	/**
	 * This is a convenience method to log out. IMPORTANT: This method initiates a
	 * redirect and RETURN. Make sure you return the call hierarchy gracefully.
	 * Most of all, don't touch HttpServletRequest or the Session after you call
	 * this method.
	 *
	 * @param ureq
	 */
	public static void doLogout(UserRequest ureq) {
		if(ureq == null) return;

		boolean wasGuest = false;
		UserSession usess = ureq.getUserSession();
		if(usess != null && usess.getRoles() != null) {
			wasGuest = usess.getRoles().isGuestOnly();
		}

		String lang = CoreSpringFactory.getImpl(I18nModule.class).getLocaleKey(ureq.getLocale());
		HttpSession session = ureq.getHttpReq().getSession(false);
		// next line fires a valueunbound event to UserSession, which does some
		// stuff on logout
		if (session != null) {
			try{
				session.invalidate();
				deleteShibsessionCookie(ureq);
			} catch(IllegalStateException ise) {
				// thrown when session already invalidated. fine. ignore.
			}
		}

		// redirect to logout page in dmz realm, set info that DMZ is shown because of logout
		// if it was a guest user, do not set logout=true. The parameter must be evaluated
		// by the implementation of the AuthenticationProvider.
		String setWarning = wasGuest ? "" : "&logout=true";
		ureq.getDispatchResult().setResultingMediaResource(
				new RedirectMediaResource(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + "?lang=" + lang + setWarning));
	}

	private static void deleteShibsessionCookie(UserRequest ureq) {
    //	try to delete the "shibsession" cookie for this ureq, if any found
		Cookie[] cookies = ureq.getHttpReq().getCookies();
		Cookie cookie = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().indexOf("shibsession")!=-1) { //contains "shibsession"
					cookie = cookies[i];
					break;
				}
			}
		}
		if(cookie!=null) {
			//A zero value causes the cookie to be deleted.
			cookie.setMaxAge(0);
			cookie.setPath("/");
			ureq.getHttpResp().addCookie(cookie);
			if(log.isDebugEnabled()) {
				log.info("AuthHelper - shibsession cookie deleted");
			}
		}
	}

	/**
	 * Build session info
	 * @param identity
	 * @param authProvider
	 * @param ureq
	 */
	private static void setSessionInfoFor(Identity identity, UserSession usess, String authProvider, UserRequest ureq, boolean rest) {
		HttpSession session = ureq.getHttpReq().getSession();
		SessionInfo sinfo = new SessionInfo(identity.getKey(), session);
		sinfo.setFirstname(identity.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()));
		sinfo.setLastname(identity.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()));
		sinfo.setFromIP(ureq.getHttpReq().getRemoteAddr());
		sinfo.setAuthProvider(authProvider);
		sinfo.setUserAgent(ureq.getHttpReq().getHeader("User-Agent"));
		sinfo.setSecure(ureq.getHttpReq().isSecure());
		sinfo.setLastClickTime();
		sinfo.setREST(rest);
		// set session info for this session
		usess.setSessionInfo(sinfo);
		// For Usertracking, let the User object know about some desired/specified infos from the sessioninfo
		Map<String,String> sessionInfoForUsertracking = new HashMap<>();
		sessionInfoForUsertracking.put(ATTRIBUTE_LANGUAGE, usess.getLocale().toString());
		sessionInfoForUsertracking.put(ATTRIBUTE_AUTHPROVIDER, authProvider);
		sessionInfoForUsertracking.put(ATTRIBUTE_IS_WEBDAV, String.valueOf(sinfo.isWebDAV()));
		sessionInfoForUsertracking.put(ATTRIBUTE_IS_REST, String.valueOf(sinfo.isREST()));
		usess.getIdentityEnvironment().setAttributes(sessionInfoForUsertracking);

	}

	/**
	 * Set the roles (admin, author, guest)
	 * @param identity
	 * @param usess
	 */
	private static void setRolesFor(Identity identity, UserSession usess) {
		Roles roles = BaseSecurityManager.getInstance().getRoles(identity);
		usess.setRoles(roles);
	}

	public static void setLoginBlocked(boolean newLoginBlocked) {
		loginBlocked = newLoginBlocked;
	}

	public static boolean isLoginBlocked() {
		return loginBlocked;
	}

	public static void setRejectDMZRequests(boolean newRejectDMZRequests) {
		rejectDMZRequests = newRejectDMZRequests;
	}

	public static boolean isRejectDMZRequests() {
		return rejectDMZRequests;
	}

	public static void setMaxSessions(int maxSession) {
		maxSessions  = maxSession;
	}

	public static int getMaxSessions() {
		return maxSessions;
	}
}