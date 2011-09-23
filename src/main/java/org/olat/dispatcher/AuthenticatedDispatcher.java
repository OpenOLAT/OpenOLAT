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

package org.olat.dispatcher;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.dispatcher.jumpin.JumpInManager;
import org.olat.core.dispatcher.jumpin.JumpInReceptionist;
import org.olat.core.dispatcher.jumpin.JumpInResult;
import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.URIHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;
import org.olat.login.LoginModule;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class AuthenticatedDispatcher implements Dispatcher {
	protected static final String AUTHDISPATCHER_ENTRYURL = "AuthDispatcher:entryUrl";
	protected static final String AUTHDISPATCHER_BUSINESSPATH = "AuthDispatcher:businessPath";
	
	private static final String AUTHCHIEFCONTROLLER = "AUTHCHIEFCONTROLLER";
	protected static final String QUESTIONMARK = "?";
	protected static final String GUEST = "guest";
	protected static final String INVITATION = "invitation";
	protected static final String TRUE = "true";
	private static final String LANG = "lang";
	private static final String D_TABS = "DTabs";
	/** forces secure http connection to access olat if set to true **/
	private boolean forceSecureAccessOnly = false;
	private UserBasedLogLevelManager userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();
	
	public AuthenticatedDispatcher(boolean forceSecureAccessOnly) {
		this.forceSecureAccessOnly = forceSecureAccessOnly;
	}

	/**
	 * Main method called by DispatcherAction. This processess all requests for
	 * authenticated users.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		long startExecute = 0;
		if ( Tracing.isDebugEnabled(this.getClass()) ) {
			startExecute = System.currentTimeMillis();
		}
		UserSession usess = UserSession.getUserSession(request);
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequest(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(Tracing.isDebugEnabled(AuthenticatedDispatcher.class)){
				Tracing.logDebug("Bad Request "+request.getPathInfo(), this.getClass());
			}
			DispatcherAction.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		//GUIInterna.setLoadPerformanceMode(ureq);		
		//GUIInterna.setUserSession (usess);
		
		boolean auth = usess.isAuthenticated();

		if (!auth) {
			if (!ureq.isValidDispatchURI()) {
				// might be a direct jump request -> remember it if not logged in yet
				String reqUri = request.getRequestURI();
				String query = request.getQueryString();
				String allGet = reqUri + QUESTIONMARK + query;
				usess.putEntryInNonClearedStore(AUTHDISPATCHER_ENTRYURL, allGet);
			}
			String guestAccess = ureq.getParameter(GUEST);
			if (guestAccess == null || !LoginModule.isGuestLoginLinksEnabled()) {
				DispatcherAction.redirectToDefaultDispatcher(response);
				return;
			} else if (guestAccess.equals(TRUE)) {
				// try to log in as anonymous
				// use the language from the lang paramter if available, otherwhise use the system default locale
				String guestLang = ureq.getParameter("lang");
				Locale guestLoc;
				if (guestLang == null) {
					guestLoc = I18nModule.getDefaultLocale();
				} else {
					guestLoc = I18nManager.getInstance().getLocaleOrDefault(guestLang);
				}
				int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
				if ( loginStatus != AuthHelper.LOGIN_OK) {
					if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
						DispatcherAction.redirectToServiceNotAvailable(response);
					}
					DispatcherAction.redirectToDefaultDispatcher(response); // error, redirect to login screen
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
				DispatcherAction.redirectToDefaultDispatcher(response);
				return;
			}
			
			SessionInfo sessionInfo = usess.getSessionInfo();
			if (sessionInfo==null) {
				DispatcherAction.redirectToDefaultDispatcher(response);
				return;
			}
			
			if (userBasedLogLevelManager!=null) userBasedLogLevelManager.activateUsernameBasedLogLevel(sessionInfo.getLogin());
			
			sessionInfo.setLastClickTime();
			String origUrl = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_ENTRYURL);
			if (origUrl != null) {
				// we had a direct jump request
				// to avoid a endless redirect, remove the guest parameter if any
				// this can happen if a guest has cookies disabled
				String url = new URIHelper(origUrl).removeParameter(GUEST).toString();
				DispatcherAction.redirectTo(response, url);
				return;
			}
			String businessPath = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_BUSINESSPATH);
			if (businessPath != null) {
				BusinessControl bc = BusinessControlFactory.getInstance().createFromString(businessPath);
				ChiefController cc = (ChiefController) Windows.getWindows(usess).getAttribute("AUTHCHIEFCONTROLLER");

				WindowControl wControl = cc.getWindowControl();
			  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
			  NewControllerFactory.getInstance().launch(ureq, bwControl);	
				// render the window
				Window w = cc.getWindow();
				w.dispatchRequest(ureq, true); // renderOnly
				return;
			}
			
			// 1. check for direct launch urls, see org.olat.core.dispatcher.jumpin.JumpinConfig
			if (!ureq.isValidDispatchURI()) {
				JumpInReceptionist jh = JumpInManager.getInstance().getJumpInReceptionist(ureq);
				if (jh == null) {
          // found no JumpInManager => try with new 5.1 JumpIn-Resource URL
					String uri = ureq.getNonParsedUri();
					if (uri.startsWith(JumpInManager.CONST_EXTLINK)) {
						String resourceUrl = ureq.getParameter(JumpInManager.CONST_RESOURCE_URL);
						if (resourceUrl != null) {
							// attach the launcher data
							BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
							try {
								// get main window and dynamic tabs
								// brasato:: ChiefController cc = Windows.getWindows(usess).getWindowManager().getMainChiefController();
								ChiefController cc = (ChiefController) Windows.getWindows(usess).getAttribute(AUTHCHIEFCONTROLLER);

								// brasato:: todo: cc = Windows.getWindows(usess).getRegisteredJumpChiefController();
								WindowControl wControl = cc.getWindowControl();
							  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
							  NewControllerFactory.getInstance().launch(ureq, bwControl);	
								// render the window
								Window w = cc.getWindow();
								w.dispatchRequest(ureq, true); // renderOnly
							  return;
							} catch (Exception ex) {
								// sendNotFound					
							}
						}
					}
					DispatcherAction.sendNotFound(request.getRequestURI(), response);
					return;
				}
				
				// handler found e.g. for repo/go or cata/go or alike
				OLATResourceable ores = jh.getOLATResourceable();			
				String title = jh.getTitle();

				// get main window and dynamic tabs
				// brasato:: ChiefController cc = Windows.getWindows(usess).getMainOlatChiefController();
				ChiefController cc = (ChiefController) Windows.getWindows(usess).getAttribute(AUTHCHIEFCONTROLLER);

				if (cc == null) throw new AssertException("logged in, but no window/Chiefcontroller 'olatmain' found!");
				WindowControl wControl = cc.getWindowControl();

				// add to tabs
				DTabs dts = (DTabs)wControl.getWindowBackOffice().getWindow().getAttribute(D_TABS);
				synchronized (dts) { //o_clusterok per:fj user session
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// no dynamic tab found, lets see if the ores implements the SiteInstance interface.
						boolean isSiteInstance;
						try {
							// try to load class from ores resource type name and try to cast it to SiteInstance
							Class site = ores.getClass().getClassLoader().loadClass(ores.getResourceableTypeName());
							site.asSubclass(SiteInstance.class);
							// ok, casting did not fail, must be a site then
							isSiteInstance = true;
						} catch (Exception e) {
							// casting failed, not a site
							isSiteInstance = false;
						}
						
						if (isSiteInstance) {
							// case A) is a site: create view identifyer for this jump in to the site
							JumpInResult jres = jh.createJumpInResult(ureq, cc.getWindowControl());
							dts.activateStatic(ureq, ores.getResourceableTypeName(), jres.getInitialViewIdentifier());							
						} else {

							// case B) no site and no opened tab 
							//
							//see OLAT-4511 
							//sometimes it is a refresh, for example to get a css loaded, in such
							//cases the missing tab must not be recreated, it was just closed. In these cases 
							//referer will be same as current location uri -> create and add the tab
							
							HttpServletRequest hsr = ureq.getHttpReq();
							if (hsr.getHeader("referer") == null || // direct jump in to a tab
									!hsr.getHeader("referer").endsWith("?"+hsr.getQueryString())) {
								//-> create and add the tab
								dt = dts.createDTab(ores, title);
							}

							if (dt == null) { // tabs are full
								//create dtabs already issues a warning message	
							} else {
								JumpInResult jres = jh.createJumpInResult(ureq, dt.getWindowControl());
								Controller resC = jres.getController();
								if (resC == null) { // the resource was not found or user is not
									// allowed to start the resource
									DispatcherAction.sendNotFound(request.getRequestURI(), response);
									return;
								}
								dt.setController(resC);
								dts.addDTab(dt);
								dts.activate(ureq, dt, null); // null: do not activate controller
							}							
						}
					} else {
						// case C) opened dyn tab found, activate the dyn tab
						dts.activate(ureq, dt, jh.extractActiveViewId(ureq)); 
					}
				}
				// render the window
				Window w = cc.getWindow();
				w.dispatchRequest(ureq, true); // renderOnly
			} else { // valid uri for dispatching (has timestamp, componentid and
				// windowid)
				Windows ws = Windows.getWindows(ureq);
				Window window = ws.getWindow(ureq);
				if (window == null) {
					// If no window, this is probably a stale link. send not
					// found
					// note: do not redirect to login since this wastes a new
					// window each time since we are in an authenticated session
					// -> a content packaging with wrong links e.g. /css/my.css
					// wastes all the windows
					DispatcherAction.sendNotFound(request.getRequestURI(), response);
					return;
				}
				long startDispatchRequest = 0;
				if (Tracing.isDebugEnabled(this.getClass())) {
					startDispatchRequest = System.currentTimeMillis();
				}
				window.dispatchRequest(ureq);
				if ( Tracing.isDebugEnabled(this.getClass()) ) {
					long durationDispatchRequest = System.currentTimeMillis() - startDispatchRequest;
					Tracing.logDebug("Perf-Test: window=" + window, this.getClass());
					Tracing.logDebug("Perf-Test: durationDispatchRequest=" + durationDispatchRequest,this.getClass());
				}
			}
		} catch (Throwable th) {
			// Do not log as Warn or Error here, log as ERROR in MsgFactory => ExceptionWindowController throws an OLATRuntimeException 
			Tracing.logDebug("handleError in AuthenticatedDispatcher throwable=" + th, getClass());
			DispatcherAction.handleError();
			ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
			// the controller's window must be failsafe also
			msgcc.getWindow().dispatchRequest(ureq, true);
			// do not dispatch (render only), since this is a new Window created as
			// a result of another window's click.
		} finally {
			if (userBasedLogLevelManager!=null) userBasedLogLevelManager.deactivateUsernameBasedLogLevel();
			if ( Tracing.isDebugEnabled(this.getClass()) ) {
				long durationExecute = System.currentTimeMillis() - startExecute;
				Tracing.logDebug("Perf-Test: durationExecute=" + durationExecute, this.getClass());
			}
			//XX:GUIInterna.setLoadPerformanceMode(null);
		}

	}

}
