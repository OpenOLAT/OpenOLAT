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
* Copyright (c) frentix GmbH<br>
* http://www.frentix.com<br>
* <p>
*/ 

package org.olat.core.commons.contextHelp;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.commons.chiefcontrollers.BaseChiefControllerCreator;
import org.olat.core.commons.fullWebApp.BaseFullWebappPopupBrowserWindow;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3> The context help dispatcher displays context help files
 * to a separate popup window. 
 * <p>
 * The dispatcher can be used with an authenticated session or with a
 * non-authenticated session.
 * 
 * <p>
 * Initial Date: 30.10.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class ContextHelpDispatcher extends LogDelegator implements Dispatcher {
	private static final String CONTEXTHELPCHIEFCONTROLLER = "CONTEXTHELPCHIEFCONTROLLER";
	private static String PATH_CHELP;
	
	public ContextHelpDispatcher(String contextHelpMapperPath) {
		PATH_CHELP = contextHelpMapperPath;
	}
	
	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String uriPrefix) {
		UserRequest ureq = null;
		
		try {
			ureq = new UserRequest(uriPrefix, request, response);
			if (!ContextHelpModule.isContextHelpEnabled()) {
				// disabled context help - redirect immediately
				DispatcherAction.sendNotFound(ureq.getNonParsedUri(), response);
				return;
			}

			ChiefController cc = (ChiefController) Windows.getWindows(ureq.getUserSession()).getAttribute(CONTEXTHELPCHIEFCONTROLLER);	
			// reuse existing chief controller for this user
			if (cc != null) {				
				Window currentWindow = cc.getWindow();
				// Check if this is a start URL or a framework URL
				if (ureq.isValidDispatchURI()) {
					// A standard framework request, dispatch by component
					currentWindow.dispatchRequest(ureq, false);
					return;
				} else {					
					// If path contains complete URL, dispose and start from scratch
					Windows.getWindows(ureq).deregisterWindow(currentWindow);
					cc.dispose();					
				}
			}
			
			// Creator code to create 
			// 1) the chief controller
			// 2) the layout controller
			// 3) the context help main controller
			ControllerCreator cHelpPopupWindowControllerCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					ControllerCreator cHelpMainControllerCreator = new ControllerCreator() {
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							// create the context help controller and wrapp it using the layout controller
							ContextHelpMainController helpCtr =  new ContextHelpMainController(lureq, lwControl);
							LayoutMain3ColsController layoutCtr =  new LayoutMain3ColsController(lureq, lwControl, null, null, helpCtr.getInitialComponent(), null);
							layoutCtr.addDisposableChildController(helpCtr);
							return layoutCtr;
						}
					};
					ContextHelpLayoutControllerCreator cHelpPopupLayoutCreator = new ContextHelpLayoutControllerCreator(cHelpMainControllerCreator);
					return new BaseFullWebappPopupBrowserWindow(lureq, lwControl, cHelpPopupLayoutCreator.getFullWebappParts());
				}
			};

			BaseChiefControllerCreator bbc = new BaseChiefControllerCreator();
			bbc.setContentControllerCreator(cHelpPopupWindowControllerCreator);			
			cc = bbc.createChiefController(ureq);		
			// add to user session for cleanup on user logout
			Windows.getWindows(ureq.getUserSession()).setAttribute(CONTEXTHELPCHIEFCONTROLLER, cc);			
			Window currentWindow = cc.getWindow();
			currentWindow.setUriPrefix(WebappHelper.getServletContextPath() + PATH_CHELP);
			Windows.getWindows(ureq).registerWindow(currentWindow);
			// finally dispatch the initial request
			currentWindow.dispatchRequest(ureq, true);
			
		} catch (Throwable th) {
			try {
				ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
				// the controller's window must be failsafe also
				msgcc.getWindow().dispatchRequest(ureq, true);
				// do not dispatch (render only), since this is a new Window created as
				// a result of another window's click.
			} catch (Throwable t) {
				logError("Sorry, can't handle this context help request....", t);
			}
		}
	
		}

	/**
	 * Create an URL for the given locale, bundle and page that can be dispatched
	 * by this dispatcher
	 * 
	 * @param locale The desired locale
	 * @param bundleName The bundle name, e.g. "org.olat.core"
	 * @param page The page, e.g. "my-file.html" 
	 * @return
	 */
	public static String createContextHelpURI(Locale locale, String bundleName, String page) {
		return WebappHelper.getServletContextPath() + PATH_CHELP + locale.toString() + "/" + bundleName + "/" + page;
	}
	
}
