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

package org.olat.commons.dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ChiefControllerCreator;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;

/**
 * 
 * @author Felix Jost
 */
public class SessionDispatcher implements Dispatcher {
	private ChiefControllerCreator chiefControllerCreator;
	
	private Dispatcher preDispatcher = null, postDispatcher = null;

	/**
	 * Main method called by DispatcherAction.
	 * This processess all requests for users who are not authenticated.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		if (preDispatcher != null) {
			preDispatcher.execute(request, response, uriPrefix);
		}
		
		UserRequest ureq = new UserRequest(uriPrefix, request, response);
		//String sessionId = request.getRequestedSessionId();
		try {
			UserSession usess = ureq.getUserSession();
			Windows ws = Windows.getWindows(usess);
			synchronized (ws) {  //o_clusterOK by:fj

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
					// request new windows since it is a new usersession, the old one was purged
					ws = Windows.getWindows(usess);
				} else {
					if (validDispatchUri) {
						window = ws.getWindow(ureq);
					} else {
						// e.g. /dmz/ -> start screen, clear previous session data
						window = null; 
						usess.signOffAndClear();
						// request new windows since it is a new usersession, the old one was purged
						ws = Windows.getWindows(usess);
					}
				}
				
				if (window == null) {
					// no window found, -> start a new WorkFlow/Controller and obtain the window
					// main controller which also implements the windowcontroller for pagestatus and modal dialogs
					ChiefController occ = chiefControllerCreator.createChiefController(ureq);
				
					// browser did not send a cookie && url has a window id with it (= we
					// are clicking a framework link or submitting a framework form)
					//if (sessionId == null && ureq.getWindowID() != null) {
						//Translator trans = new PackageTranslator("org.olat", ureq.getLocale()); // locale
						//occ.getWindowControl().setWarning(trans.translate("wayf.cookies"));
					//}

					window = occ.getWindow();
					window.setUriPrefix(uriPrefix);
					ws.registerWindow(window);
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
				Tracing.logError("An exception occured while handling the exception...",t, SessionDispatcher.class);
			}
		} finally {
			if (postDispatcher != null) {
				postDispatcher.execute(request, response, uriPrefix);
			}
		}
	}

	/**
	 * [used by spring]
	 * @param chiefControllerCreator The chiefControllerCreator to set.
	 */
	public void setChiefControllerCreator(ChiefControllerCreator chiefControllerCreator) {
		this.chiefControllerCreator = chiefControllerCreator;
	}

	/**
	 * [spring]
	 * @param postDispatcher
	 */
	public void setPostDispatcher(Dispatcher postDispatcher) {
		this.postDispatcher = postDispatcher;
	}

	/**
	 * [spring]
	 * @param preDispatcher
	 */
	public void setPreDispatcher(Dispatcher preDispatcher) {
		this.preDispatcher = preDispatcher;
	}
}
