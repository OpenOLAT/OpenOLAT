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
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.commons.fullWebApp.MinimalBaseFullWebappController;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.impl.InvalidRequestParameterException;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.LoginModule;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class AuthenticatedDispatcher implements Dispatcher {
	private static final Logger log = Tracing.createLoggerFor(AuthenticatedDispatcher.class);
	
	public static final String AUTHDISPATCHER_BUSINESSPATH = "AuthDispatcher:businessPath";
	public static final String AUTHDISPATCHER_REDIRECT_URL = "AuthDispatcher:redirectUrl";
	
	protected static final String QUESTIONMARK = "?";
	protected static final String GUEST = "guest";
	protected static final String INVITATION = "invitation";
	protected static final String TRUE = "true";
	/** forces secure http connection to access olat if set to true **/
	private boolean forceSecureAccessOnly = false;
	
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
		final String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);

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
			log.debug("Bad Request {}", request.getPathInfo());
		}
		
		boolean auth = usess.isAuthenticated();
		Tracing.setUserSession(usess);
		if (!auth) {
			String pathInfo = request.getPathInfo();
			if(pathInfo != null && pathInfo.contains("close-window")) {
				DispatcherModule.setNotContent(pathInfo, response);
				return;
			}
			
			String guestAccess = ureq.getParameter(GUEST);
			if (guestAccess == null || !CoreSpringFactory.getImpl(LoginModule.class).isGuestLoginEnabled()) {
				String businessPath = extractBusinessPath(ureq, request, uriPrefix);
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
				invalidateSession(usess);
				redirectToDefaultDispatcher(request, response);
				return;
			}
			
			SessionInfo sessionInfo = usess.getSessionInfo();
			if (sessionInfo == null) {
				redirectToDefaultDispatcher(request,response);
				return;
			}
			
			sessionInfo.setLastClickTime();

			String redirectUrl = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_REDIRECT_URL);
			String businessPath = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_BUSINESSPATH);
			if (redirectUrl != null) {
				DispatcherModule.redirectTo(response, redirectUrl);
			} else if (businessPath != null) {
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
			log.debug("handleError in AuthenticatedDispatcher throwable", th);
			DispatcherModule.handleError();
			ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
			// the controller's window must be failsafe also
			msgcc.getWindow().dispatchRequest(ureq, true);
			// do not dispatch (render only), since this is a new Window created as
			// a result of another window's click.
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
		
		if(restPart == null) {
			return null;
		}
		int index = restPart.indexOf(";jsessionid=");
		if(index >= 0) {
			restPart = restPart.substring(0, index);
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
		Windows ws;
		try {
			ws = Windows.getWindows(ureq);
		} catch (IllegalStateException e) {
			log.error("", e);// session was invalidate, return to login screen
			redirectToDefaultDispatcher(request, response);
			return;
		}
		ws.disposeClosedWindows(ureq);
		Window window = ws.getWindow(ureq);
		if (window == null) {
			if(request.getPathInfo() != null && request.getPathInfo().contains("close-window")) {
				DispatcherModule.setNotContent(request.getPathInfo(), response);
			} else if(usess.isSavedSession() && !usess.getHistoryStack().isEmpty()) {
				redirectToDefaultDispatcher(request, response);
			} else {
				DispatcherModule.sendNotFound(request.getRequestURI(), response);
			}
		} else {
			window.dispatchRequest(ureq);
		}
	}
	
	private void invalidateSession(UserSession usess) {
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
		// Check first if URL was opened in an existing window. Send page that reads the
		// browser window name and redirects back with the attached parameter.
		String ooBrowserWinCheck = ureq.getHttpReq().getParameter("oow");
		String windowId = ureq.getWindowID();
		if (ooBrowserWinCheck == null && (windowId == null || "-1".equals(windowId))) {
			// not yet checked
			String newWindow = ureq.getParameter("new-window");
			String requestUri = ureq.getHttpReq().getRequestURI();
			if(StringHelper.containsNonWhitespace(businessPath)) {
				requestUri = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
			}
			try(StringOutput clientSideWindowCheck = new StringOutput()) {
				clientSideWindowCheck.append("<!DOCTYPE html>\n<html><head><title>Reload</title><script>")
					.append("window.location.replace('").append(requestUri).append("?");
				if(StringHelper.containsNonWhitespace(newWindow)) {
					clientSideWindowCheck.append("new-window=").append(newWindow).append("&");
				}
				clientSideWindowCheck
					.append("oow=' + window.name").append(");")
					.append("</script></head><body></body></html>");
				ServletUtil.serveStringResource(ureq.getHttpResp(), clientSideWindowCheck);
			} catch(IOException e) {
				log.error("", e);
			}
			return;
		}
		
		Windows windows = Windows.getWindows(usess);
		ChiefController chiefController = windows.getChiefController(ureq);
		if(chiefController == null && !usess.isAuthenticated()) {
			redirectToDefaultDispatcher(ureq.getHttpReq(), ureq.getHttpResp());
			return;
		}
		
		if(chiefController == null) {
			String newWindow = ureq.getParameter("new-window");
			if("minimal".equals(newWindow)) {
				PopupBrowserWindow pbw = new MinimalBaseFullWebappController(ureq);
				pbw.open(ureq);
				chiefController = (ChiefController)pbw;
			} else if("reduced".equals(newWindow)) {
				ControllerCreator alternativeWindowControllerCreator = (lureq, lwControl) -> {
					Controller dummyCtr = new BasicController(lureq, lwControl) {
						
						@Override
						protected void event(UserRequest llureq, Component source, Event event) {
							// no events
						}
					};
					return new LayoutMain3ColsController(lureq, lwControl, dummyCtr);
				};
				PopupBrowserWindow pbw = Windows.getWindows(ureq).getWindowManager().createNewPopupBrowserWindowFor(ureq, alternativeWindowControllerCreator);
				pbw.open(ureq);
				chiefController = (ChiefController)pbw;
			} else {
				if(usess.getRoles().isGuestOnly()) {
					chiefController = AuthHelper.createGuestHome(ureq);
				} else {
					chiefController = AuthHelper.createAuthHome(ureq);
				}
				LockResourceInfos lockInfos = windows.getLockResourceInfos();
				if(lockInfos != null) {
					((BaseFullWebappController)chiefController).hardLockResource(lockInfos);
				}
			}
			Window window = chiefController.getWindow();
			window.setUriPrefix(ureq.getUriPrefix());
			Windows.getWindows(usess).registerWindow(chiefController);
		}
		
		// If no client side window detected => open in new window, create new server side window
		if ((!StringHelper.containsNonWhitespace(ooBrowserWinCheck) || "-1".equals(ooBrowserWinCheck))
				&& (windowId == null || "-1".equals(windowId))) {
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(businessPath);
			if(!chiefController.delayLaunch(ureq, bc)) {
				WindowControl wControl = chiefController.getWindowControl();
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
				NewControllerFactory.getInstance().launch(ureq, bwControl);	
			}
			chiefController.getWindow().dispatchRequest(ureq, true); // renderOnly
			chiefController.resetReload();
			return;
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

				if(!chiefController.delayLaunch(ureq, bc)) {
					WindowControl wControl = windowBackOffice.getChiefController().getWindowControl();
					WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
					NewControllerFactory.getInstance().launch(ureq, bwControl);
				}
				// render the window
				Window w = windowBackOffice.getWindow();
				log.debug("Dispatch auth request by window {}", w.getInstanceId());
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