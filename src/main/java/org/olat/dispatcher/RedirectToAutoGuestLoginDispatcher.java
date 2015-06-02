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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;

/**
 * Description:<br>
 * Dispatcher which redirects requests from / (root) to the default path 
 * defined in the <code>_spring/defaultconfig.xml</code> or the respective <code>_spring/extconfig.xml</code>
 * and appended by the AutoGuest-login provider
 * for example in a default installation this will be:<br>
 * http://www.yourthingy.org/olat/ -> http://www.yourthingy.org/olat/dmz/?lp=xyz
 * 
 * <P>
 * Initial Date:  08.07.2006 <br>
 * @author patrickb
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class RedirectToAutoGuestLoginDispatcher implements Dispatcher {
	
	private static final OLog log = Tracing.createLoggerFor(RedirectToAutoGuestLoginDispatcher.class);
		
	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
			ureq = new UserRequestImpl(uriPrefix, request, response);
		}catch(NumberFormatException nfe){
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebug()){
				log.debug("Bad Request "+request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		String lang = ureq.getParameter("language");
		if (lang == null) {
			// support for legacy lang parameter
			lang = ureq.getParameter("lang");
		}
		int loginStatus = AuthHelper.doAnonymousLogin(ureq,I18nManager.getInstance().getLocaleOrDefault(lang) );
		if ( loginStatus != AuthHelper.LOGIN_OK) {
			if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToServiceNotAvailable(response);
			}
			DispatcherModule.redirectToDefaultDispatcher(response); // error, redirect to login screen
			return;
		}
		
		ChiefController cc = Windows.getWindows(usess).getChiefController();
		if (cc == null) throw new AssertException("logged in, but no window/Chiefcontroller 'olatmain' found!");
		Window w = cc.getWindow();
		w.dispatchRequest(ureq, true); // renderOnly
	}

}
