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

package ch.unizh.dispatcher;

import java.io.IOException;

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
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.URIHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;

/**
 * special dispatcher with some tweaks to redirect users to a certain page when they use the old olat uri
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 * @author guido
 */
public class UzhAuthenticatedDispatcher implements Dispatcher {
	private static final String AUTHDISPATCHER_ENTRYURL = "AuthDispatcher:entryUrl";
	private static final String AUTHCHIEFCONTROLLER = "AUTHCHIEFCONTROLLER";
	private static final String QUESTIONMARK = "?";
	private static final String GUEST = "guest";
	private static final String TRUE = "true";
	private static final String LANG = "lang";
	private static final String D_TABS = "DTabs";
	/** forces secure http connection to access olat if set to true **/
	private boolean forceSecureAccessOnly = false;
	private String olatLegacyURL = "http://www.olat.unizh.ch/";
	
	/**
	 * constructor ars setted via spring config, search for UzhAuthenticatedDispatcher in xml files
	 * @param forceSecureAccessOnly
	 * @param olatLegacyURL
	 */
	public UzhAuthenticatedDispatcher(boolean forceSecureAccessOnly, String olatLegacyURL) {
		this.forceSecureAccessOnly = forceSecureAccessOnly;
		if ( olatLegacyURL != null ) this.olatLegacyURL = olatLegacyURL;
	}

	/**
	 * Main method called by DispatcherAction. This processes all requests for
	 * authenticated users.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		UserSession usess = UserSession.getUserSession(request);
		UserRequest ureq = null;
		try {
			//upon creation URL is checked for 
			ureq = new UserRequest(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(Tracing.isDebugEnabled(UzhAuthenticatedDispatcher.class)){
				Tracing.logDebug("Bad Request "+request.getPathInfo(), this.getClass());
			}
			DispatcherAction.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		
		boolean auth = usess.isAuthenticated();
		
		if (!auth) {
			//check for legacy unizh url's and redirect
			if (checkForRedirect(request, response) ) return;
			
			
			if (!ureq.isValidDispatchURI()) {
				// might be a direct jump request -> remember it if not logged in yet
				String reqUri = request.getRequestURI();
				String query = request.getQueryString();
				String allGet = reqUri + QUESTIONMARK + query;
				usess.putEntryInNonClearedStore(AUTHDISPATCHER_ENTRYURL, allGet);
			}
			String guestAccess = ureq.getParameter(GUEST);
			if (guestAccess == null) {
				DispatcherAction.redirectToDefaultDispatcher(response);
				return;
			} else if (guestAccess.equals(TRUE)) {
				// try to log in as anonymous
				int loginStatus = AuthHelper.doAnonymousLogin(ureq,I18nManager.getInstance().getLocaleOrDefault(ureq.getParameter(LANG)) );
				if ( loginStatus != AuthHelper.LOGIN_OK ) {
					if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
						DispatcherAction.redirectToServiceNotAvailable(response);
					}
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
		//check for legacy unizh url's and redirect
			if (checkForRedirect(request, response)) return ;
			
			usess.getSessionInfo().setLastClickTime();
			String origUrl = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_ENTRYURL);
			if (origUrl != null) {
				// we had a direct jump request
				// to avoid a endless redirect, remove the guest parameter if any
				// this can happen if a guest has cookies disabled
				String url = new URIHelper(origUrl).removeParameter(GUEST).toString();
				DispatcherAction.redirectTo(response, url);
				return;
			}
			// 1. check for direct launch urls
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
				// handler found e.g. for repo/go
				OLATResourceable ores = jh.getOLATResourceable();
				String title = jh.getTitle();

				// get main window and dynamic tabs
				// brasato:: ChiefController cc = Windows.getWindows(usess).getMainOlatChiefController();
				ChiefController cc = (ChiefController) Windows.getWindows(usess).getAttribute(AUTHCHIEFCONTROLLER);

				if (cc == null) throw new AssertException("logged in, but no window/Chiefcontroller 'olatmain' found!");
				WindowControl wControl = cc.getWindowControl();

				// add to tabs
				DTabs dts = (DTabs)wControl.getWindowBackOffice().getWindow().getAttribute(D_TABS);
				synchronized (dts) { //o_clusterok per user session
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist -> create and add
						dt = dts.createDTab(ores, title);
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
					} else {
						dts.activate(ureq, dt, jh.extractActiveViewId(ureq)); // activate
																																	// controller
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
				window.dispatchRequest(ureq);
			}
		} catch (Throwable th) {
			Tracing.logDebug("handleError in AuthenticatedDispatcher", getClass());
			DispatcherAction.handleError();
			ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
			// the controller's window must be failsafe also
			msgcc.getWindow().dispatchRequest(ureq, true);
			// do not dispatch (render only), since this is a new Window created as
			// a result of another window's click.
		} 
	}

	private boolean checkForRedirect(HttpServletRequest request, HttpServletResponse response) {
		if(!request.getServerName().equals(Settings.getServerconfig("server_fqdn"))) {
			StringBuilder sb = new StringBuilder(olatLegacyURL);
			if (request.getQueryString() != null) sb.append("?").append(request.getQueryString());
			
			try {
				Tracing.logDebug("redirecting to proper domain: "+sb.toString(), this.getClass());
				response.sendRedirect(sb.toString());
				return true;
			} catch (IOException e) {
				Tracing.logError("could not redirect to url: "+sb.toString(), this.getClass());
				return false;
			}
		}
		return false;
	}

}
