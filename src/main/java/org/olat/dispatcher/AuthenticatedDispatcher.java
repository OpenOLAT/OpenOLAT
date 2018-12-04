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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.impl.InvalidRequestParameterException;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;
import org.olat.login.LoginModule;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class AuthenticatedDispatcher implements Dispatcher {
	private static final OLog log = Tracing.createLoggerFor(AuthenticatedDispatcher.class);
	
	protected static final String AUTHDISPATCHER_BUSINESSPATH = "AuthDispatcher:businessPath";
	
	protected static final String QUESTIONMARK = "?";
	protected static final String GUEST = "guest";
	protected static final String INVITATION = "invitation";
	protected static final String TRUE = "true";
	/** forces secure http connection to access olat if set to true **/
	private boolean forceSecureAccessOnly = false;
	private UserBasedLogLevelManager userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();
	
	public AuthenticatedDispatcher(boolean forceSecureAccessOnly) {
		this.forceSecureAccessOnly = forceSecureAccessOnly;
	}

	/**
	 * Main method called by OpenOLATServlet. This processess all requests for
	 * authenticated users.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);

		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		UserRequest ureq = null;
		try{
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
		}
		
		boolean auth = usess.isAuthenticated();
		if (!auth) {
			String guestAccess = ureq.getParameter(GUEST);
			if (guestAccess == null || !CoreSpringFactory.getImpl(LoginModule.class).isGuestLoginEnabled()) {
				String businessPath = extractBusinessPath( ureq, request, uriPrefix);
				if(businessPath != null) {
					usess.putEntryInNonClearedStore(AUTHDISPATCHER_BUSINESSPATH, businessPath);
				}
				redirectToDefaultDispatcher(request, response);
				return;
			} else if (guestAccess.equals(TRUE)) {
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
				int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
				if ( loginStatus != AuthHelper.LOGIN_OK) {
					if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
						DispatcherModule.redirectToServiceNotAvailable(response);
					}
					redirectToDefaultDispatcher(request, response); // error, redirect to login screen
					return;
				}
				// else now logged in as anonymous user, continue
			}
		}

		// authenticated!
		try {
			
			//kill session if not secured via SSL
			if (forceSecureAccessOnly && !request.isSecure()) {
				SessionInfo sessionInfo = usess.getSessionInfo();
				if (sessionInfo!=null) {
					HttpSession session = sessionInfo.getSession();
					if (session!=null) {
						try{
							session.invalidate();
						} catch(IllegalStateException ise) {
							// thrown when session already invalidated. fine. ignore.
						}
					}
				}
				redirectToDefaultDispatcher(request, response);
				return;
			}
			
			SessionInfo sessionInfo = usess.getSessionInfo();
			if (sessionInfo == null) {
				redirectToDefaultDispatcher(request,response);
				return;
			}
			
			if (userBasedLogLevelManager != null) {
				userBasedLogLevelManager.activateUsernameBasedLogLevel(sessionInfo.getLogin());
			}
			sessionInfo.setLastClickTime();
			
			String businessPath = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_BUSINESSPATH);
			if (businessPath != null) {
				processBusinessPath(businessPath, ureq, usess);
			} else if (ureq.isValidDispatchURI()) {
				// valid uri for dispatching (has timestamp, componentid and windowid)
				processValidDispatchURI(ureq, usess, request, response);
			} else {
				businessPath = extractBusinessPath(ureq, request, uriPrefix);
				if(businessPath == null) {
					processBusinessPath("", ureq, usess);
				} else {
					processBusinessPath(businessPath, ureq, usess);
				}
			}
		} catch (InvalidRequestParameterException e) {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e1) {
				log.error("An exception occured while handling the invalid request parameter exception...", e1);
			}
		} catch (Throwable th) {
			// Do not log as Warn or Error here, log as ERROR in MsgFactory => ExceptionWindowController throws an OLATRuntimeException 
			log.debug("handleError in AuthenticatedDispatcher throwable=" + th);
			DispatcherModule.handleError();
			ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
			// the controller's window must be failsafe also
			msgcc.getWindow().dispatchRequest(ureq, true);
			// do not dispatch (render only), since this is a new Window created as
			// a result of another window's click.
		} finally {
			if (userBasedLogLevelManager != null) {
				userBasedLogLevelManager.deactivateUsernameBasedLogLevel();
			}
		}
	}
	
	private String extractBusinessPath(UserRequest ureq, HttpServletRequest request, String uriPrefix) {
		final String origUri = request.getRequestURI();
		String restPart = origUri.substring(uriPrefix.length());
		try {
			restPart = URLDecoder.decode(restPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		if(restPart.startsWith("repo/go")) {
			return convertJumpInURL(ureq);
		}

		String[] split = restPart.split("/");
		if (split.length > 0 && split.length % 2 == 0) {
			return BusinessControlFactory.getInstance().formatFromSplittedURI(split);
		}
		return null;
	}
	
	/**
	 * http://localhost:8080/olat/auth/repo/go?rid=819242&amp;par=77013818723561
	 * @param requestPart
	 * @param ureq
	 * @return
	 */
	private String convertJumpInURL(UserRequest ureq) {
		String repoId = ureq.getParameter("rid");
		String businessPath = "[RepositoryEntry:" + repoId + "]";
		String par = ureq.getParameter("par");
		if(StringHelper.containsNonWhitespace(par) && StringHelper.isLong(par)) {
			try {
				Long parLong = Long.parseLong(par);
				businessPath += "[Part:" + parLong + "]";
			} catch(NumberFormatException e) {
				//it can happen
			}
		}
		return businessPath;				
	}
	
	private void processValidDispatchURI(UserRequest ureq, UserSession usess, HttpServletRequest request, HttpServletResponse response) {
		Windows ws = Windows.getWindows(ureq);
		Window window = ws.getWindow(ureq);
		if (window == null) {
			//probably a 
			if(usess.isSavedSession() && !usess.getHistoryStack().isEmpty()) {
				redirectToDefaultDispatcher(request, response);
			} else {
				DispatcherModule.sendNotFound(request.getRequestURI(), response);
			}
		} else {
			window.dispatchRequest(ureq);
		}
	}
	
	private void redirectToDefaultDispatcher(HttpServletRequest request, HttpServletResponse response) {
		if(ServletUtil.acceptJson(request)) {
			try {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			} catch (IOException e) {
				log.error("", e);
			}
		} else {
			DispatcherModule.redirectToDefaultDispatcher(response);
		}
	}
	
	private void processBusinessPath(String businessPath, UserRequest ureq, UserSession usess) {
		ChiefController chiefController = Windows.getWindows(usess).getChiefController();
		
		if(chiefController == null) {
			if(usess.isAuthenticated()) {
				AuthHelper.createAuthHome(ureq).getWindow();
				chiefController = Windows.getWindows(usess).getChiefController();
			} else {
				redirectToDefaultDispatcher(ureq.getHttpReq(), ureq.getHttpResp());
				return;
			}
		}

		WindowBackOffice windowBackOffice = chiefController.getWindow().getWindowBackOffice();
		if(chiefController.isLoginInterceptionInProgress()) {
			Window w = windowBackOffice.getWindow();
			w.dispatchRequest(ureq, true); // renderOnly
		} else {
			String wSettings = (String) usess.removeEntryFromNonClearedStore(WINDOW_SETTINGS);
			if(wSettings != null) {
				WindowSettings settings = WindowSettings.parse(wSettings);
				windowBackOffice.setWindowSettings(settings);
			}
			
			try {
				BusinessControl bc = null;
				String historyPointId = ureq.getHttpReq().getParameter("historyPointId");
				if(StringHelper.containsNonWhitespace(historyPointId)) {
					HistoryPoint point = ureq.getUserSession().getHistoryPoint(historyPointId);
					bc = BusinessControlFactory.getInstance().createFromContextEntries(point.getEntries());
				}
				if(bc == null) {
					bc = BusinessControlFactory.getInstance().createFromString(businessPath);
				}
	
				WindowControl wControl = windowBackOffice.getChiefController().getWindowControl();
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
				NewControllerFactory.getInstance().launch(ureq, bwControl);	
				// render the window
				Window w = windowBackOffice.getWindow();
				log.debug("Dispatch auth request by window " + w.getInstanceId());
				w.dispatchRequest(ureq, true); // renderOnly
				chiefController.resetReload();
			} catch (Exception e) {
				// try to render something
				try {
					Window w = windowBackOffice.getWindow();
					w.dispatchRequest(ureq, true); // renderOnly
				} catch (Exception e1) {
					redirectToDefaultDispatcher(ureq.getHttpReq(), ureq.getHttpResp());
				}
				log.error("", e);
			}
		}
	}
}