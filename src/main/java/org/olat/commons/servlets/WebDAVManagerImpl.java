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

package org.olat.commons.servlets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.servlets.SecureWebdavServlet;
import org.olat.core.servlets.WebDAVManager;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.auth.WebDAVAuthManager;

import com.oreilly.servlet.Base64Decoder;

/**
 * Initial Date:  16.04.2003
 *
 * @author Mike Stock
 * @author guido
 * 
 * Comment:  
 * 
 */
public class WebDAVManagerImpl extends WebDAVManager {
	private static boolean enabled = true;
	
	private static final String BASIC_AUTH_REALM = "OLAT WebDAV Access";
	private CoordinatorManager coordinatorManager;

	private CacheWrapper timedSessionCache;
	private UserSessionManager sessionManager;

	/**
	 * [spring]
	 */
	private WebDAVManagerImpl(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
		INSTANCE = this;
	}

	/**
	 * [used by Spring]
	 * @param sessionManager
	 */
	public void setSessionManager(UserSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * @see org.olat.commons.servlets.WebDAVManager#handleAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected boolean handleAuthentication(HttpServletRequest req, HttpServletResponse resp) {
		UserSession usess = handleBasicAuthentication(req, resp);
		if (usess == null) return false;

		// register usersession in REQUEST, not session !!
		// see SecureWebDAVServlet.setAuthor() and checkQuota()
		req.setAttribute(SecureWebdavServlet.REQUEST_USERSESSION_KEY, usess);
		return true;
	}
	
	/**
	 * @see org.olat.commons.servlets.WebDAVManager#getUserSession(javax.servlet.http.HttpServletRequest)
	 */
	protected UserSession getUserSession(HttpServletRequest req) {
		return (UserSession)req.getAttribute(SecureWebdavServlet.REQUEST_USERSESSION_KEY);
	}
	
	private UserSession handleBasicAuthentication(HttpServletRequest request, HttpServletResponse response) {
		
		if (timedSessionCache == null) {
			synchronized (this) {
				timedSessionCache = coordinatorManager.getCoordinator().getCacher().getOrCreateCache(this.getClass(), "webdav");
			}
		}
		
		// Get the Authorization header, if one was supplied
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			// fetch user session from a previous authentication
			UserSession usess = (UserSession)timedSessionCache.get(authHeader);
			if (usess != null && usess.isAuthenticated()) {
				return usess;
			}
			
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				// We only handle HTTP Basic authentication
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					usess = handleBasicAuthentication(credentials, request);
					
				}
			}
			
			if(usess != null) {
				timedSessionCache.put(authHeader, usess);
			}
		}

		// If the user was not validated or the browser does not know about the realm yet, fail with a
		// 401 status code (UNAUTHORIZED) and
		// pass back a WWW-Authenticate header for
		// this servlet.
		//
		// Note that this is the normal situation the
		// first time you access the page. The client
		// web browser will prompt for userID and password
		// and cache them so that it doesn't have to
		// prompt you again.

		response.setHeader("WWW-Authenticate", "Basic realm=\"" + BASIC_AUTH_REALM + "\"");
		response.setStatus(401);
		return null;
	}
	
	private UserSession handleBasicAuthentication(String credentials, HttpServletRequest request) {
		// This example uses sun.misc.* classes.
		// You will need to provide your own
		// if you are not comfortable with that.
		String userPass = Base64Decoder.decode(credentials);

		// The decoded string is in the form
		// "userID:password".
		int p = userPass.indexOf(":");
		if (p != -1) {
			String userID = userPass.substring(0, p);
			String password = userPass.substring(p + 1);
			
			// Validate user ID and password
			// and set valid true if valid.
			// In this example, we simply check
			// that neither field is blank
			Identity identity = WebDAVAuthManager.authenticate(userID, password);
			if (identity != null) {
				UserSession usess = sessionManager.getUserSession(request);
				synchronized(usess) {
					//double check to prevent severals concurrent login
					if(usess.isAuthenticated()) {
						return usess;
					}
				
					sessionManager.signOffAndClear(usess);
					usess.setIdentity(identity);
					UserDeletionManager.getInstance().setIdentityAsActiv(identity);
					// set the roles (admin, author, guest)
					Roles roles = BaseSecurityManager.getInstance().getRoles(identity);
					usess.setRoles(roles);
					// set authprovider
					//usess.getIdentityEnvironment().setAuthProvider(OLATAuthenticationController.PROVIDER_OLAT);
				
					// set session info
					SessionInfo sinfo = new SessionInfo(identity.getKey(), identity.getName(), request.getSession());
					User usr = identity.getUser();
					sinfo.setFirstname(usr.getProperty(UserConstants.FIRSTNAME, null));
					sinfo.setLastname(usr.getProperty(UserConstants.LASTNAME, null));
					sinfo.setFromIP(request.getRemoteAddr());
					sinfo.setFromFQN(request.getRemoteAddr());
					try {
						InetAddress[] iaddr = InetAddress.getAllByName(request.getRemoteAddr());
						if (iaddr.length > 0) sinfo.setFromFQN(iaddr[0].getHostName());
					} catch (UnknownHostException e) {
						 // ok, already set IP as FQDN
					}
					sinfo.setAuthProvider(BaseSecurityModule.getDefaultAuthProviderIdentifier());
					sinfo.setUserAgent(request.getHeader("User-Agent"));
					sinfo.setSecure(request.isSecure());
					sinfo.setWebDAV(true);
					sinfo.setWebModeFromUreq(null);
					// set session info for this session
					usess.setSessionInfo(sinfo);
					//
					sessionManager.signOn(usess);
					return usess;
				}
			}
		}
		return null;
	}
	
	/**
	 * @see org.olat.core.servlets.WebDAVManager#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Spring setter method to enable/disable the webDAV module
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		WebDAVManagerImpl.enabled = enabled;
	}



}