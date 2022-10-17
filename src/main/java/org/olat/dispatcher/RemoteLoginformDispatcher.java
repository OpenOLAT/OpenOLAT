/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.dispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.login.OLATAuthenticationController;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;

/**
 * <h3>Description:</h3>
 * Login dispatcher that takes a username and password provided by some HTTP
 * parameters to authenticat the user. In case of success the user will be
 * redirected to the home, in case of failure, the OLAT loginscreen appears and
 * an error message is triggered.
 * <p>
 * The external form must submit the data using the HTTP POST method. GET is not
 * supported to prevent logging of logfiles into apache logfiles or caching in
 * proxy servers.
 * <p>
 * The following parameters are used to transport the data:
 * <ul>
 * <li>username: username</li>
 * <li>password: pwd</li>
 * </ul>
 * <p>
 * The following example will open OLAT in a new window without browser toolbars:
 * <br>
 * <form 
 * 	method="post" 
 * 	action="http://office.frentix.com:8080/branch/remotelogin/"
 * 	onsubmit="var olat = window.open('','OLAT','location=no,menubar=no,resizable=yes,toolbar=no,statusbar=no,scrollbars=yes')" olat.focus();" 
 *  target="OLAT">
 *  <input type="text" name="username">
 *  <input type="password" name="pwd">
 *  <button>login</button>
 * </form>
 * <p>An optional parameter named redirect allow a redirect using a businesspath:
 *  <input type="hidden" name="redirect" value="/olat/url/RepositoryEntry/917504/CourseNode/81254724902921"/>
 * <p>
 * Initial Date: 05.08.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class RemoteLoginformDispatcher implements Dispatcher {
	
	private static final String METHOD_POST = "POST";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_CREDENTIAL = "pwd";
	private static final Logger log = Tracing.createLoggerFor(RemoteLoginformDispatcher.class);
	
	private BaseSecurity securityManager;
	
	/**
	 * [used by Spring]
	 * @param securityManager Base security
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}
	
	/**
	 * Tries to login the user with the parameters from the POST request and
	 * redirects to the home screen in case of success. In case of failure,
	 * redirects to the login screen.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		UserRequest ureq = null;

		try {
			String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
			ureq = new UserRequestImpl(uriPrefix, request, response);
				
			if (! request.getMethod().equals(METHOD_POST)) {
				log.warn("Wrong HTTP method, only POST allowed, but current method::{}", request.getMethod());
				DispatcherModule.redirectToDefaultDispatcher(response); 
				return;
			}
			String userName = ureq.getParameter(PARAM_USERNAME);
			if (! StringHelper.containsNonWhitespace(userName)) {
				log.warn("Missing username parameter, use '" + PARAM_USERNAME + "' to submit the login name");
				DispatcherModule.redirectToDefaultDispatcher(response); 
				return;
			}
			String pwd = ureq.getParameter(PARAM_CREDENTIAL);
			if ( ! StringHelper.containsNonWhitespace(pwd)) {
				log.warn("Missing password parameter, use '" + PARAM_CREDENTIAL + "' to submit the password");
				DispatcherModule.redirectToDefaultDispatcher(response); 
				return;					
			}
			
			// Authenticate user
			OLATAuthManager olatAuthenticationSpi = CoreSpringFactory.getImpl(OLATAuthManager.class);
			Identity identity = olatAuthenticationSpi.authenticate(null, userName, pwd, new AuthenticationStatus());
			if (identity == null) {
				log.info("Could not authenticate user '{}', wrong password or user name", userName);
				// redirect to OLAT loginscreen, add error parameter so that the loginform can mark itself as errorfull
				String loginUrl = WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + "?" + OLATAuthenticationController.PARAM_LOGINERROR + "=true";
				DispatcherModule.redirectTo(response, loginUrl); 
				return;									
			}
			
			UserSession usess = ureq.getUserSession();
			//re-init the activity logger to pass the user session and identity
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
			
			//sync over the UserSession Instance to prevent double logins
			synchronized (usess) {
				// Login user, set up everything
				int loginStatus = AuthHelper.doLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
				if (loginStatus == AuthHelper.LOGIN_OK) {
					// redirect to authenticated environment
					securityManager.setIdentityLastLogin(identity);
					
					final String origUri = request.getRequestURI();
					String restPart = origUri.substring(uriPrefix.length());
					if(request.getParameter("redirect") != null) {
						//redirect parameter like: /olat/url/RepositoryEntry/917504/CourseNode/81254724902921
						String redirect = request.getParameter("redirect");
						DispatcherModule.redirectSecureTo(response, redirect);
					} else if(StringHelper.containsNonWhitespace(restPart)) {
						//redirect like: http://www.frentix.com/olat/remotelogin/RepositoryEntry/917504/CourseNode/81254724902921
						try {
							restPart = URLDecoder.decode(restPart, "UTF8");
						} catch (UnsupportedEncodingException e) {
							log.error("Unsupported encoding", e);
						}
						
						String[] split = restPart.split("/");
						assert(split.length % 2 == 0);
						String businessPath = "";
						for (int i = 0; i < split.length; i=i+2) {
							String key = split[i];
							if(key != null && key.startsWith("path=")) {
								key = key.replace("~~", "/");
							}
							String value = split[i+1];
							businessPath += "[" + key + ":" + value +"]";
						}
						
						usess.putEntryInNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_BUSINESSPATH, businessPath);
						String url = getRedirectToURL(usess, ureq);
						DispatcherModule.redirectSecureTo(response, url);
					} else {
						//redirect
						ServletUtil.serveResource(request, response, ureq.getDispatchResult().getResultingMediaResource());
					}
				} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
						DispatcherModule.redirectToServiceNotAvailable(response);
				} else {
					// error, redirect to login screen
					DispatcherModule.redirectToDefaultDispatcher(response); 
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
				log.error("Sorry, can't handle this remote login request....", t);
			}
		}
	}
	
	private String getRedirectToURL(UserSession usess, UserRequest ureq) {
		Window w = Windows.getWindows(usess).getChiefController(ureq).getWindow();
		
		URLBuilder ubu = new URLBuilder("", w.getInstanceId(), w.getTimestamp(), usess.getCsrfToken());
		try(StringOutput sout = new StringOutput(30)) {
			ubu.buildURI(sout, null, null);
			return WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED + sout.toString();
		} catch(IOException e) {
			log.error("", e);
			return WebappHelper.getServletContextPath();
		}
	}
}
