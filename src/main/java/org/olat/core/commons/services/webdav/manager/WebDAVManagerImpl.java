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

package org.olat.core.commons.services.webdav.manager;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.webdav.WebDAVManager;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.commons.services.webdav.servlets.WebResourceRoot;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  16.04.2003
 *
 * @author Mike Stock
 * @author guido
 * 
 * Comment:  
 * 
 */
@Service("webDAVManager")
public class WebDAVManagerImpl implements WebDAVManager, InitializingBean {
	private static final Logger log = Tracing.createLoggerFor(WebDAVManagerImpl.class);
	private static boolean enabled = true;
	
	public static final String BASIC_AUTH_REALM = "OLAT WebDAV Access";
	private CoordinatorManager coordinatorManager;

	private CacheWrapper<CacheKey,UserSession> timedSessionCache;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private WebDAVAuthManager webDAVAuthManager;
	@Autowired
	private WebDAVModule webdavModule;

	@Autowired
	public WebDAVManagerImpl(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		timedSessionCache = coordinatorManager.getCoordinator().getCacher().getCache(WebDAVManager.class.getSimpleName(), "webdav");
	}
	
	@Override
	public WebResourceRoot getWebDAVRoot(HttpServletRequest req) {
		UserSession usess = getUserSession(req);
		if (usess == null || usess.getIdentity() == null) {
			return createEmptyRoot(usess);
		}

		usess.getSessionInfo().setLastClickTime();
		VFSResourceRoot fdc = (VFSResourceRoot)usess.getEntry("_DIRCTX");
		if (fdc != null) {
			return fdc;
		}
		
		IdentityEnvironment identityEnv = usess.getIdentityEnvironment();
		VFSContainer webdavContainer = getMountableRoot(identityEnv);
		
		//create the / folder
		VirtualContainer rootContainer = new VirtualContainer("");
		rootContainer.addItem(webdavContainer);
		rootContainer.setLocalSecurityCallback(new ReadOnlyCallback());

		fdc = new VFSResourceRoot(identityEnv.getIdentity(), rootContainer);
		usess.putEntry("_DIRCTX", fdc);
		return fdc;
	}
	
	/**
	 * Returns a mountable root containing all entries which will be exposed to the webdav mount.
	 * @return
	 */
	private VFSContainer getMountableRoot(IdentityEnvironment identityEnv) {
		MergeSource vfsRoot = new MergeSource(null, "webdav");
		for (Map.Entry<String, WebDAVProvider> entry : webdavModule.getWebDAVProviders().entrySet()) {
			WebDAVProvider provider = entry.getValue();
			if(provider.hasAccess(identityEnv)) {
				vfsRoot.addContainer(new WebDAVProviderNamedContainer(identityEnv, provider));
			}
		}
		return vfsRoot;
	}
	
	private WebResourceRoot createEmptyRoot(UserSession usess) {
		//create the / folder
		VirtualContainer rootContainer = new VirtualContainer("");
		rootContainer.setLocalSecurityCallback(new ReadOnlyCallback());
		return new VFSResourceRoot(usess.getIdentity(), rootContainer);
	}

	@Override
	public boolean handleAuthentication(HttpServletRequest req, HttpServletResponse resp) {
		//manger not started
		if(timedSessionCache == null) {
			return false;
		}
		
		UserSession usess = sessionManager.getUserSession(req);
		if(usess != null && usess.isAuthenticated()) {
			req.setAttribute(REQUEST_USERSESSION_KEY, usess);
			return true;
		} 

		usess = doAuthentication(req, resp);
		if (usess == null) {
			return false;
		}

		// register usersession in REQUEST, not session !!
		// see SecureWebDAVServlet.setAuthor() and checkQuota()
		req.setAttribute(REQUEST_USERSESSION_KEY, usess);
		return true;
	}

	@Override
	public UserSession getUserSession(HttpServletRequest req) {
		return (UserSession)req.getAttribute(REQUEST_USERSESSION_KEY);
	}
	
	private UserSession doAuthentication(HttpServletRequest request, HttpServletResponse response) {
		// Get the Authorization header, if one was supplied
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			// fetch user session from a previous authentication
			
			String cacheKey = null;
			UserSession usess = null;
			String remoteAddr = request.getRemoteAddr();
			
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				
				log.debug("Do authentication: {} for session {}", basic, getHttpSessionId(request));

				// We only handle HTTP Basic authentication
				if (basic.equalsIgnoreCase("Basic")) {
					cacheKey = authHeader;
					usess = timedSessionCache.get(new CacheKey(remoteAddr, authHeader));
					if (usess == null || !usess.isAuthenticated()) {
						String credentials = st.nextToken();
						usess = handleBasicAuthentication(credentials, request);
					}
				} else if (basic.equalsIgnoreCase("Digest")) {
					DigestAuthentication digestAuth = DigestAuthentication.parse(authHeader);
					cacheKey = digestAuth.getUsername();
					usess = timedSessionCache.get(new CacheKey(remoteAddr, digestAuth.getUsername()));

					log.debug("Digest authentication: {}", getHttpSessionId(request));
					if (usess == null || !usess.isAuthenticated()) {
						usess = handleDigestAuthentication(digestAuth, request);
					}
				} else if (basic.equalsIgnoreCase("Bearer")) {
					String sessionId = getHttpSessionId(request);
					if(sessionId != null) {
						usess = timedSessionCache.get(new CacheKey(remoteAddr, sessionId));
						if (usess != null && usess.isAuthenticated()) {
							return usess;
						}
					}
				}
			}
	
			if(usess != null && cacheKey != null) {
				timedSessionCache.putIfAbsent(new CacheKey(remoteAddr, cacheKey), usess);
				
				String sessionId = getHttpSessionId(request);
				if(sessionId != null) {
					timedSessionCache.putIfAbsent(new CacheKey(remoteAddr, sessionId), usess);
				}
				return usess;
			}
		} else {
			String sessionId = getHttpSessionId(request);
			if(sessionId != null) {
				String remoteAddr = request.getRemoteAddr();
				UserSession usess = timedSessionCache.get(new CacheKey(remoteAddr, sessionId));
				if (usess != null && usess.isAuthenticated()) {
					return usess;
				}
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

		if(proposeBasicAuthentication(request)) {
			log.debug("Add basic authentication: {} {}", getHttpSessionId(request), ServletUtil.getUserAgent(request));
			response.addHeader("WWW-Authenticate", "Basic realm=\"" + BASIC_AUTH_REALM + "\"");
		}
		if(webdavModule.isDigestAuthenticationEnabled()) {
			log.debug("Add digest authentication: {}", getHttpSessionId(request));
			String nonce = UUID.randomUUID().toString().replace("-", "");
			response.addHeader("WWW-Authenticate", "Digest realm=\"" + BASIC_AUTH_REALM + "\", qop=\"auth\", nonce=\"" + nonce + "\"");
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return null;
	}
	
	/**
	 * The method doesn't create a new HTTP Session.
	 * @param request The HTTP servlet request
	 * @return The session ID if the request has a HTTP session attached or null
	 */
	private String getHttpSessionId(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session == null ? null : session.getId();
	}
	
	private boolean proposeBasicAuthentication(HttpServletRequest request) {
		String userAgent = ServletUtil.getUserAgent(request);
		if(StringHelper.containsNonWhitespace(userAgent)) {
			String[] exclusionList = webdavModule.getBasicAuthenticationExclusionList();
			for(String exclusion:exclusionList) {
				if(userAgent.contains(exclusion) && webdavModule.isDigestAuthenticationEnabled()) {
					return false;
				}
			}
		}
		return request.isSecure() || Settings.isJUnitTest();
	}

	protected UserSession handleDigestAuthentication(DigestAuthentication digestAuth, HttpServletRequest request) {
		Identity identity = webDAVAuthManager.digestAuthentication(request.getMethod(), digestAuth);
		if(identity != null && securityManager.isIdentityLoginAllowed(identity)) {
			log.info("WebDAV Digest authentication of: {}", identity);
			return afterAuthorization(identity, request);
		}
		return null;
	}
	
	protected UserSession handleBasicAuthentication(String credentials, HttpServletRequest request) {
		String userPass = StringHelper.decodeBase64(credentials);

		// The decoded string is in the form
		// "userID:password".
		int p = userPass.indexOf(':');
		if (p != -1) {
			String userID = userPass.substring(0, p);
			String password = userPass.substring(p + 1);
			
			// Validate user ID and password
			// and set valid true if valid.
			// In this example, we simply check
			// that neither field is blank
			Identity identity = webDAVAuthManager.authenticate(userID, password);
			if (identity != null && securityManager.isIdentityLoginAllowed(identity)) {
				log.debug("WebDAV Basic authentication of: {}", identity);
				return afterAuthorization(identity, request);
			}
		}
		return null;
	}
	
	private UserSession afterAuthorization(Identity identity, HttpServletRequest request) {
		UserSession usess = sessionManager.getUserSession(request);
		synchronized(usess) {
			//double check to prevent severals concurrent login
			if(usess.isAuthenticated()) {
				return usess;
			}
		
			sessionManager.signOffAndClear(usess);
			usess.setIdentity(identity);
			securityManager.setIdentityLastLogin(identity);
			// set the roles (admin, author, guest)
			Roles roles = securityManager.getRoles(identity);
			usess.setRoles(roles);
			// set session info
			SessionInfo sinfo = new SessionInfo(identity.getKey(), request.getSession());
			User usr = identity.getUser();
			sinfo.setFirstname(usr.getProperty(UserConstants.FIRSTNAME, null));
			sinfo.setLastname(usr.getProperty(UserConstants.LASTNAME, null));
			
			String remoteAddr = request.getRemoteAddr();
			sinfo.setFromIP(remoteAddr);
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
	
	/**
	 * @see org.olat.core.commons.services.webdav.WebDAVManager#isTrue()
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