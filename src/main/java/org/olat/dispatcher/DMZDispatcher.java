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

import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ChiefControllerCreator;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class DMZDispatcher implements Dispatcher {
	//fxdiff FXOLAT-113: business path in DMZ
	public static final String DMZDISPATCHER_BUSINESSPATH =  "DMZDispatcher:businessPath";
	
	/**
	 * set by spring to create the starting workflow for /dmz/
	 */
	private ChiefControllerCreator chiefControllerCreator;

	/**
	 * set by spring
	 */
	private Map<String, ChiefControllerCreator> dmzServicesByPath;

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
								Tracing.logInfo("Allowing request with valid bypass cookie, sessionId="+request.getRequestedSessionId(), DMZDispatcher.class);
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
				Tracing.logInfo("Rejecting request to DMZDispatcher (AuthHelper.isRejectDMZRequests() is true) to "+rejectUrl+", sessionId="+request.getRequestedSessionId(), DMZDispatcher.class);
				if (sessionCookie!=null) {
					String newSessionId = sessionCookie.getValue().substring(0, sessionCookie.getValue().length()-2);
					response.setHeader("Set-Cookie", "JSESSIONID="+newSessionId+"; Path="+request.getContextPath()+(request.isSecure()?"":"; Secure"));
				}
				DispatcherAction.redirectTo(response, rejectUrl);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Main method called by DispatcherAction. This processess all requests for
	 * users who are not authenticated.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		if (rejectRequest(request, response)) {
			return;
		}
		
		
		/*
		 * (here it was assumed that an url containing /m/ is that of a
		 * mapper of olat, which is anyway not a good assumption.
		 * 
		 * Removing this check can not create a security issue because a crafted
		 * request just needs to have referer null to bypass this check.
		 * 
		 * 
		 * 
		 
		String referer = request.getHeader("referer");
		if(referer != null && referer.indexOf(DispatcherAction.PATH_MAPPED) > -1) {
			//TODO:gs may no longer needed as bad rel links are catched in dispatcherAction
			//OLAT-3334
			//ignore /dmz/ requests issued from "content" delivered by 
			// /m/98129834/folder0/folder1/folder3/bla.hmtl
			// this can happen if for example a CP contains a relative link pointing back like
			// ../../../../../../presentation/cool.js where the "up navigation" exceeds the
			// the /folder0/folder1/folder3 path and even jumps over /m/98129834.
			//The DMZ is reached, the session invalidated and next click shows login screen.
			//
			//Because /g/ mapped content is considered to be save against such errors, there
			// is no check for PATH_GLOBAL_MAPPED. Typically /g/ mapped paths are 
			// application wide defined and not brought in by users. Hence it should 
			// be discovered during developing or testing.
			//
			String msg = "BAD LINK IN [["+referer+"]]";
			Tracing.logWarn(msg, DMZDispatcher.class);
			DispatcherAction.sendNotFound(msg, response);
			return;
		}
		
		*
		*
		*/

		UserRequest ureq = null;

		try {
			// upon creation URL is checked for

			ureq = new UserRequest(uriPrefix, request, response);
		} catch (NumberFormatException nfe) {
			// MODE could not be decoded
			// typically if robots with wrong urls hit the system
			// or user have bookmarks
			// or authors copy-pasted links to the content.
			// showing redscreens for non valid URL is wrong instead
			// a 404 message must be shown -> e.g. robots correct their links.
			if (Tracing.isDebugEnabled(DMZDispatcher.class)) {
				Tracing.logDebug("Bad Request " + request.getPathInfo(), this.getClass());
			}
			DispatcherAction.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		//set load performance mode depending on logged in user or global parameter
		//here in the DMZ only the global parameter plays a role.
		//XX:GUIInterna.setLoadPerformanceMode(ureq);
		
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
				
				UserSession usess = ureq.getUserSession();
				Windows ws = Windows.getWindows(usess);
				synchronized (ws) { //o_clusterOK by:fj per user session
					ChiefController occ;
					if(subPathccc != null){
						occ = subPathccc.createChiefController(ureq);
						Window window = occ.getWindow();
						window.setUriPrefix(uriPrefix);
						ws.registerWindow(window);
						
						window.dispatchRequest(ureq, true);
						return;
					}					
				}
			}//else a /olat/dmz/ request
			/*
			 * create content as it is defined in config.xml in he dmzbean
			 */

			/*
			 * solve this with a predispatcher action
			 */
			
//			// convenience method to jump immediatly to AAI (Shibboleth) home
//			// organisation for login without selecting home organisation manually
//			if (ShibbolethModule.isEnableShibbolethLogins()) {
//				String preSelIdp = request.getParameter("preselection");
//				String redirect = request.getParameter("redirect");
//				if (preSelIdp != null && redirect != null && redirect.equalsIgnoreCase("true")) {
//					preSelIdp = preSelIdp.toLowerCase();
//					Collection sites = IdPSite.getIdPSites(ShibbolethModule.getMetadata());
//					for (Iterator iter = sites.iterator(); iter.hasNext();) {
//						IdPSite site = (IdPSite) iter.next();
//						if (site.getName().toLowerCase().indexOf(preSelIdp) > -1) {
//							response.sendRedirect(AssertionConsumerService.buildRequest(request.getLocale(), site));
//							break;
//						}
//					}
//				}
//			}

			UserSession usess = ureq.getUserSession();
			Windows ws = Windows.getWindows(usess);
			synchronized (ws) { //o_clusterOK by:fj per user session

				Window window;
				boolean windowHere = ws.isExisting(ureq.getWindowID());
				boolean validDispatchUri = ureq.isValidDispatchURI();
				if (validDispatchUri && !windowHere) {
					// probably valid framework link from previous user && new Session(no window):
					// when a previous user logged off, and 30min later (when the httpsession is invalidated), the next user clicks e.g. on 
					// the log-in link in the -same- browser window ->
					// -> there is no window -> create a new one
					window = null;
					usess.signOffAndClear();
					usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
					I18nManager.updateLocaleInfoToThread(usess);//update locale infos
					
					// request new windows since it is a new usersession, the old one was purged
					ws = Windows.getWindows(usess);
				} else {
					if (validDispatchUri) {
						window = ws.getWindow(ureq);
					} else {
						// e.g. /dmz/ -> start screen, clear previous session data
						window = null; 
						usess.signOffAndClear();
						usess.setLocale(LocaleNegotiator.getPreferedLocale(ureq));
						I18nManager.updateLocaleInfoToThread(usess);//update locale infos
						// request new windows since it is a new usersession, the old one was purged
						ws = Windows.getWindows(usess);
					}
				}
				
				if (window == null) {
					// no window found, -> start a new WorkFlow/Controller and obtain the window
					// main controller which also implements the windowcontroller for pagestatus and modal dialogs
					ChiefController occ = chiefControllerCreator.createChiefController(ureq);
					
//				REVIEW:12-2007:CodeCleanup				
					// browser did not send a cookie && url has a window id with it (= we
					// are clicking a framework link or submitting a framework form)
					//if (sessionId == null && ureq.getWindowID() != null) {
						//Translator trans = new PackageTranslator("org.olat", ureq.getLocale()); // locale
						//occ.getWindowControl().setWarning(trans.translate("wayf.cookies"));
					//}

					window = occ.getWindow();
					window.setUriPrefix(uriPrefix);
					ws.registerWindow(window);
					//fxdiff FXOLAT-113: business path in DMZ
					String businessPath = (String) usess.removeEntryFromNonClearedStore(DMZDISPATCHER_BUSINESSPATH);
					if (businessPath != null) {
						List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
						DTabs dts = (DTabs) window.getAttribute("DTabs");
						dts.activate(ureq, null, null, ces);
					}
					
					window.dispatchRequest(ureq, true);
				
				} else {
					window.dispatchRequest(ureq);
				}
			}
		} catch (Throwable th) {
			try {
				ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
				// the controller's window must be failsafe also
				msgcc.getWindow().dispatchRequest(ureq, true);
				// do not dispatch (render only), since this is a new Window created as
				// a result of another window's click.
			} catch (Throwable t) {
				Tracing.logError("An exception occured while handling the exception...",t, DMZDispatcher.class);
				//XX:GUIInterna.setLoadPerformanceMode(null);
			}
			//XX:GUIInterna.setLoadPerformanceMode(null);
		} finally {
//		REVIEW:12-2007:CodeCleanup			
//			if (postDispatcher != null) {
//				postDispatcher.execute(request, response, uriPrefix);
//			}
		}
			
			
			

	}

	/**
	 * called by spring only
	 * 
	 * @param subdispatchers The subdispatchers to set.
	 */
	public void setDmzServicesByPath(Map<String, ChiefControllerCreator> dmzServicesByPath) {
		this.dmzServicesByPath = dmzServicesByPath;
	}

	/**
	 * @param chiefControllerCreator The chiefControllerCreator to set.
	 */
	public void setChiefControllerCreator(ChiefControllerCreator chiefControllerCreator) {
		this.chiefControllerCreator = chiefControllerCreator;
	}
	
	
}
