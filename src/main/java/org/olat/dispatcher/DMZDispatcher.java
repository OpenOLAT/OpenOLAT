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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.impl.InvalidRequestParameterException;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ChiefControllerCreator;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthResource;
import org.olat.login.oauth.OAuthSPI;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class DMZDispatcher implements Dispatcher {
	private static final Logger log = Tracing.createLoggerFor(DMZDispatcher.class);
	
	public static final String DMZDISPATCHER_BUSINESSPATH =  "DMZDispatcher:businessPath";
	
	private final boolean maintenance;
	
	/**
	 * set by spring to create the starting workflow for /dmz/
	 */
	private ChiefControllerCreator chiefControllerCreator;

	/**
	 * set by spring
	 */
	private final Map<String, ChiefControllerCreator> dmzServicesByPath = new HashMap<>();
	
	public DMZDispatcher(boolean maintenance) {
		this.maintenance = maintenance;
	}

	/**
	 * OLAT-5165: check whether we are currently rejecting all dmz requests and if
	 * the current request is not from an admin who did 'switch to node'.
	 * <p>
	 * @param request the incoming request
	 * @param response the response object
	 * @return whether or not to reject this request. upon true, the calling execute() method
	 * will stop any further action and simply return
	 */
	private boolean rejectRequest(HttpServletRequest request, HttpServletResponse response) {
		if (AuthHelper.isRejectDMZRequests()) {
			boolean validBypass = false;
			Cookie[] cookies = request.getCookies();
			Cookie sessionCookie = null;
			if (cookies!=null) {
				for(int i=0; i<cookies.length; i++) {
					Cookie cookie = cookies[i];
					if ("bypassdmzreject".equals(cookie.getName())) {
						// there is a bypassdmzreject cookie set - let's check the time
						try{
							long bypasscreationtime = Long.parseLong(cookie.getValue());
							if (System.currentTimeMillis()-bypasscreationtime<5*60*1000) {
								log.info("Allowing request with valid bypass cookie");
								validBypass = true;
							}
						} catch(NumberFormatException e) {
							// ignore
						}
					} else if ("JSESSIONID".equals(cookie.getName())) {
						sessionCookie = cookie;
					}
				}
			}
			if (!validBypass) {
				final String rejectUrl = request.getRequestURI();
				log.info("Rejecting request to DMZDispatcher (AuthHelper.isRejectDMZRequests() is true) to {}", rejectUrl);
				if (sessionCookie!=null) {
					String newSessionId = sessionCookie.getValue().substring(0, sessionCookie.getValue().length()-2);
					response.setHeader("Set-Cookie", "JSESSIONID="+newSessionId+"; Path="+request.getContextPath()+(request.isSecure()?"":"; Secure"));
				}
				DispatcherModule.redirectTo(response, rejectUrl);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Main method called by OpenOLATServlet. This processess all requests for
	 * users who are not authenticated.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		if (rejectRequest(request, response)) {
			return;
		}

		UserRequest ureq = null;
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		try {
			// upon creation URL is checked for
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch (NumberFormatException nfe) {
			// MODE could not be decoded
			// typically if robots with wrong urls hit the system
			// or user have bookmarks
			// or authors copy-pasted links to the content.
			// showing redscreens for non valid URL is wrong instead
			// a 404 message must be shown -> e.g. robots correct their links.
			if (log.isDebugEnabled()) {
				log.debug("Bad Request {}", request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		try {
			// find out about which subdispatcher is meant
			// e.g. got here because of /dmz/...
			// maybe something like /dmz/registration/
			//
			// add the context path to align with uriPrefix e.g. /olat/dmz/
			String pathInfo = request.getContextPath() + request.getPathInfo();
			ChiefControllerCreator subPathccc = null;
			boolean dmzOnly = pathInfo.equals(uriPrefix);// if /olat/dmz/
			if (!dmzOnly) {
				int sl = pathInfo.indexOf('/', uriPrefix.length());
				String sub;
				if (sl > 1) {
					// e.g. something like /registration/ or /pwchange/
					sub = pathInfo.substring(uriPrefix.length() - 1, sl + 1);
				} else {
					// e.g. something like /info.html from (/dmz/info.html)
					sub = pathInfo;
				}
				// chief controller creator for sub path, e.g. 
				subPathccc = dmzServicesByPath.get(sub);
				if(subPathccc != null) {
					UserSession usess = ureq.getUserSession();
					Windows ws = Windows.getWindows(usess);
					synchronized (ws) { //o_clusterOK by:fj per user session
						ChiefController occ = subPathccc.createChiefController(ureq);
						Window window = occ.getWindow();
						window.setUriPrefix(uriPrefix);
						ws.registerWindow(occ);
						window.dispatchRequest(ureq, true);
						return;
					}					
				}
			}//else a /olat/dmz/ request

			UserSession usess = ureq.getUserSession();
			Windows ws = Windows.getWindows(usess);
			//sync over the UserSession Instance as the Windows can be recreated in the synchronize block
			//and make it useless under heavily load or 2 concurrent requests
			synchronized (usess) { //o_clusterOK by:fj per user session

				Window window;
				boolean windowHere = ws.isExisting(uriPrefix, ureq.getWindowID());
				boolean validDispatchUri = ureq.isValidDispatchURI();
				if (validDispatchUri && !windowHere) {
					// probably valid framework link from previous user && new Session(no window):
					// when a previous user logged off, and 30min later (when the httpsession is invalidated), the next user clicks e.g. on 
					// the log-in link in the -same- browser window ->
					// -> there is no window -> create a new one
					if(ignoreMissingWindow(request)) {
						DispatcherModule.setNotContent(request.getPathInfo(), response);
						return;
					}
					
					window = null;
					CoreSpringFactory.getImpl(UserSessionManager.class).signOffAndClear(usess);
					usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
					I18nManager.updateLocaleInfoToThread(usess);//update locale infos
					
					// request new windows since it is a new usersession, the old one was purged
					ws = Windows.getWindows(usess);
				} else if (validDispatchUri) {
					window = ws.getWindow(ureq);
				} else if (dmzOnly) {
					// e.g. /dmz/ -> start screen, clear previous session data
					window = null; 
					CoreSpringFactory.getImpl(UserSessionManager.class).signOffAndClear(usess);
					usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
					I18nManager.updateLocaleInfoToThread(usess);//update locale infos
					
					OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
					if(canRedirectConfigurableOAuth(request, response, oauthModule)) {
						return;
					} else if(canRedirectOAuth(request, oauthModule)) {
						OAuthSPI oauthSpi = oauthModule.getRootProvider();
						HttpSession session = request.getSession();
						OAuthResource.redirect(oauthSpi, response, session);
						return;
					} 
					
					// request new windows since it is a new usersession, the old one was purged
					ws = Windows.getWindows(usess);
				} else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				
				if (window == null) {
					// no window found, -> start a new WorkFlow/Controller and obtain the window
					// main controller which also implements the windowcontroller for pagestatus and modal dialogs
					Object wSettings = usess.getEntry(WINDOW_SETTINGS);
					ChiefController occ = chiefControllerCreator.createChiefController(ureq);
					
					window = occ.getWindow();
					window.getWindowBackOffice().getWindowManager().setAjaxWanted(ureq);
					window.setUriPrefix(uriPrefix);
					ws.registerWindow(occ);
					
					String businessPath = (String) usess.removeEntryFromNonClearedStore(DMZDISPATCHER_BUSINESSPATH);
					if (businessPath != null) {
						List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
						window.getDTabs().activate(ureq, null, ces);
					}
					//apply the settings forward
					usess.putEntryInNonClearedStore(WINDOW_SETTINGS, wSettings);
				}
				window.dispatchRequest(ureq);
			}
		} catch (InvalidRequestParameterException e) {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e1) {
				log.error("An exception occured while handling the invalid request parameter exception...", e1);
			}
		} catch (Throwable th) {
			try {
				ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
				// the controller's window must be failsafe also
				msgcc.getWindow().dispatchRequest(ureq, true);
				// do not dispatch (render only), since this is a new Window created as
				// a result of another window's click.
			} catch (Throwable t) {
				log.error("An exception occured while handling the exception...", t);
			}
		}
	}
	
	private boolean ignoreMissingWindow(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if(pathInfo.contains("cid:close-window")) {
			return true;
		}
		log.info("DMZ log off triggered by {}", pathInfo);
		return false;
	}
	
	private boolean canRedirectOAuth(HttpServletRequest request, OAuthLoginModule oauthModule) {
		boolean canRedirect;
		if(maintenance) {
			canRedirect = false;
		} else if(StringHelper.containsNonWhitespace(request.getParameter("logout"))) {
			canRedirect = false;
		} else if(oauthModule.getRootProvider() != null) {
			canRedirect = true;
		} else {
			canRedirect = false;
		}
		return canRedirect;
	}
	
	private boolean canRedirectConfigurableOAuth(HttpServletRequest request, HttpServletResponse response, OAuthLoginModule oauthModule) {
		String provider = request.getParameter("provider");
		if(StringHelper.containsNonWhitespace(provider)) {
			OAuthSPI spi = oauthModule.getProvider(provider);
			if(spi != null) {
				HttpSession session = request.getSession();
				OAuthResource.redirect(spi, response, session);
				return true;
			}
		}
		return false;
	}

	/**
	 * called by spring only
	 * 
	 * @param subdispatchers The subdispatchers to set.
	 */
	public void setDmzServicesByPath(Map<String, ChiefControllerCreator> servicesByPath) {
		if(servicesByPath != null) {
			dmzServicesByPath.putAll(servicesByPath);
		}
	}

	/**
	 * @param chiefControllerCreator The chiefControllerCreator to set.
	 */
	public void setChiefControllerCreator(ChiefControllerCreator chiefControllerCreator) {
		this.chiefControllerCreator = chiefControllerCreator;
	}
	
	
}
