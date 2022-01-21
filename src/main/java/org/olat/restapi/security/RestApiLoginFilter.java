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
package org.olat.restapi.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.restapi.RestModule;

/**
 *
 * Description:<br>
 * Filter which protects the REST Api.
 *
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestApiLoginFilter implements Filter {

	private static final Logger log = Tracing.createLoggerFor(RestApiLoginFilter.class);

	private static final String BASIC_AUTH_REALM = "OLAT Rest API";
	public static final String SYSTEM_MARKER = UUID.randomUUID().toString();

	private static List<String> openUrls;
	private static List<String> alwaysEnabledUrls;
	private static List<String> ipProtectedUrls;
	private static String LOGIN_URL;

	/**
	 * The survive time of the session used by token based authentication. For every request
	 * is a new session created.
	 */
	private static final int TOKEN_BASED_SESSION_TIMEOUT = 120;

	@Override
	public void init(FilterConfig filterConfig) {
		//
	}

	@Override
	public void destroy() {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	throws ServletException {

		if(request instanceof HttpServletRequest) {
			try {
				HttpServletRequest httpRequest = (HttpServletRequest)request;
				HttpServletResponse httpResponse = (HttpServletResponse)response;

				String requestURI = getRequestURI(httpRequest);
				RestModule restModule = (RestModule)CoreSpringFactory.getBean("restModule");
				if(restModule == null || !restModule.isEnabled() && !isRequestURIAlwaysEnabled(requestURI)) {
					httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				// initialize tracing with request, this allows debugging information as IP, User-Agent.
				Tracing.setHttpRequest(httpRequest);
				I18nManager.attachI18nInfoToThread(httpRequest);
				ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(httpRequest);

				UserSession uress = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(httpRequest);
				if("/restapi/api-docs/".equals(httpRequest.getRequestURI())) {
					sendSwaggerUI(httpResponse);
				} else if(uress != null && uress.isAuthenticated()) {
					//use the available session
					followSession(httpRequest, httpResponse, chain);
				} else {
					if(isRequestURIInLoginSpace(requestURI)) {
						followForAuthentication(requestURI, uress, httpRequest, httpResponse, chain);
					} else if(isRequestURIInOpenSpace(requestURI)) {
						followWithoutAuthentication(httpRequest, httpResponse, chain);
					} else if( isRequestURIInIPProtectedSpace(requestURI, httpRequest, restModule)) {
						upgradeIpAuthentication(httpRequest, httpResponse);
						followWithoutAuthentication(httpRequest, httpResponse, chain);
					} else if (isRequestTokenValid(httpRequest)) {
						String token = httpRequest.getHeader(RestSecurityHelper.SEC_TOKEN);

						followToken(token, httpRequest, httpResponse, chain);
					} else if (isBasicAuthenticated(httpRequest, httpResponse, requestURI)) {
						followBasicAuthenticated(request, response, chain);
					} else  {
						httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"" + BASIC_AUTH_REALM + "\"");
						httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					}
				}
			} catch (Exception e) {
				log.error("", e);
				try {
					HttpServletResponse httpResponse = (HttpServletResponse)response;
					httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (Exception ex) {
					log.error("", ex);
				}
			} finally {
				ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
				I18nManager.remove18nInfoFromThread();
				Tracing.clearHttpRequest();
				DBFactory.getInstance().commitAndCloseSession();
			}
		} else {
			throw new ServletException("Only accept HTTP Request");
		}
	}
	
	/**
	 * Send a customized version of the Swagger UI with the URL of
	 * the Open API JSON description of the REST API.
	 * 
	 * @param response The HTTP servlet response
	 */
	private void sendSwaggerUI(HttpServletResponse response) {
		try(InputStream in = RestApiLoginFilter.class.getResourceAsStream("_content/swagger_index.html");
				OutputStream out=response.getOutputStream()) {
			
			String index = IOUtils.toString(in, StandardCharsets.UTF_8);
			String openApiUrl = Settings.createServerURI() + RestSecurityHelper.SUB_CONTEXT + "/openapi.json";
			index = index.replace("${openolat.openapi.url}", openApiUrl);
			byte[] indexBytes = index.getBytes(StandardCharsets.UTF_8);

			response.setContentType("text/html;charset=utf-8");
			response.setContentLengthLong(indexBytes.length);
			out.write(indexBytes);
		} catch(Exception e) {
			log.error("", e);
		}
	}

	private boolean isBasicAuthenticated(HttpServletRequest request, HttpServletResponse response, String requestURI) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				// We only handle HTTP Basic authentication
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					String userPass = StringHelper.decodeBase64(credentials);
					// The decoded string is in the form "userID:password".
					int p = userPass.indexOf(':');
					if (p != -1) {
						String username = userPass.substring(0, p);
						String password = userPass.substring(p + 1);

						OLATAuthManager olatAuthenticationSpi = CoreSpringFactory.getImpl(OLATAuthManager.class);
						Identity identity = olatAuthenticationSpi.authenticate(null, username, password, new AuthenticationStatus());
						if(identity == null) {
							return false;
						}

						UserRequest ureq = null;
						try{
							//upon creation URL is checked for
							ureq = new UserRequestImpl(requestURI, request, response);
						} catch(NumberFormatException nfe) {
							return false;
						}
						request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);

						int loginStatus = AuthHelper.doHeadlessLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq, true);
						if (loginStatus == AuthHelper.LOGIN_OK) {
							CoreSpringFactory.getImpl(BaseSecurity.class).setIdentityLastLogin(identity);
							//Forge a new security token
							RestSecurityBean securityBean = CoreSpringFactory.getImpl(RestSecurityBean.class);
							String token = securityBean.generateToken(identity, request.getSession());
							response.setHeader(RestSecurityHelper.SEC_TOKEN, token);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private void followBasicAuthenticated(ServletRequest request, ServletResponse response, FilterChain chain)
	throws ServletException, IOException {
		chain.doFilter(request, response);
	}

	private boolean isRequestTokenValid(HttpServletRequest request) {
		String token = request.getHeader(RestSecurityHelper.SEC_TOKEN);
		RestSecurityBean securityBean =  CoreSpringFactory.getImpl(RestSecurityBean.class);
		return securityBean.isTokenRegistrated(token, request.getSession(true));
	}

	private boolean isRequestURIInLoginSpace(String requestURI) {
		String loginUrl = getLoginUrl();
		if(loginUrl != null && requestURI.startsWith(loginUrl)) {
			return true;
		}
		return false;
	}

	private boolean isRequestURIInOpenSpace(String requestURI) {
		List<String> uris = getOpenURIs();
		if(uris == null) return false;
		for(String openURI : uris) {
			if(requestURI.startsWith(openURI)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRequestURIInIPProtectedSpace(String requestURI, HttpServletRequest httpRequest, RestModule restModule) {
		List<String> uris = getIPProtectedURIs();
		if(uris == null) return false;
		for(String openURI : uris) {
			if(requestURI.startsWith(openURI)) {
				String remoteAddr = httpRequest.getRemoteAddr();
				if(StringHelper.containsNonWhitespace(remoteAddr)) {
					return restModule.getIpsWithSystemAccess().contains(remoteAddr);
				}
			}
		}
		return false;
	}

	private boolean isRequestURIAlwaysEnabled(String requestURI) {
		List<String> uris = getAlwaysEnabledURIs();
		if(uris == null) return false;
		for(String openURI : uris) {
			if(requestURI.startsWith(openURI)) {
				return true;
			}
		}
		return false;
	}

	private void followForAuthentication(String requestURI, UserSession uress, HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws IOException, ServletException {
		//create a session for login without security check
		if(uress == null) {
			CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		}
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for
			ureq = new UserRequestImpl(requestURI, request, response);
		} catch(NumberFormatException nfe) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
		chain.doFilter(request, response);
	}

	private void followWithoutAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws IOException, ServletException {
		UserSession uress = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(request);
		if(uress != null && uress.isAuthenticated()) {
			//is authenticated by session cookie, follow its current session
			followSession(request, response, chain);
			return;
		}

		String token = request.getHeader(RestSecurityHelper.SEC_TOKEN);
		RestSecurityBean securityBean = (RestSecurityBean)CoreSpringFactory.getBean(RestSecurityBean.class);
		if(StringHelper.containsNonWhitespace(token) && securityBean.isTokenRegistrated(token, request.getSession(true))) {
			//is authenticated by token, follow its current token
			followToken(token, request, response, chain);
			return;
		}
		
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for
			String requestURI = getRequestURI(request);
			ureq = new UserRequestImpl(requestURI, request, response);
		} catch(NumberFormatException nfe) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);

		//no authentication, but no authentication needed, go further
		chain.doFilter(request, response);
	}

	private void upgradeIpAuthentication(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		UserSessionManager sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		UserSession usess = sessionManager.getUserSessionIfAlreadySet(request);
		if(usess == null) {
			usess = sessionManager.getUserSession(request, request.getSession(true));
		}
		if(usess.getIdentity() == null) {
			usess.setRoles(Roles.userRoles());

			String remoteAddr = request.getRemoteAddr();
			SessionInfo sinfo = new SessionInfo(Long.valueOf(-1), request.getSession());
			sinfo.setFirstname("REST");
			sinfo.setLastname(remoteAddr);
			sinfo.setFromIP(remoteAddr);
			sinfo.setAuthProvider("IP");
			sinfo.setUserAgent(request.getHeader("User-Agent"));
			sinfo.setSecure(request.isSecure());
			sinfo.setREST(true);
			sinfo.setWebModeFromUreq(null);
			// set session info for this session
			usess.setSessionInfo(sinfo);
		}

		UserRequest ureq = null;
		try{
			//upon creation URL is checked for
			String requestURI = getRequestURI(request);
			ureq = new UserRequestImpl(requestURI, request, response);
			ureq.getUserSession().putEntryInNonClearedStore(SYSTEM_MARKER, Boolean.TRUE);
		} catch(NumberFormatException nfe) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
	}

	private void followToken(String token, HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws IOException, ServletException {
		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(TOKEN_BASED_SESSION_TIMEOUT);
		UserSession uress = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request, session);
		if(uress != null) {
			UserRequest ureq = null;
			try{
				//upon creation URL is checked for
				String requestURI = getRequestURI(request);
				ureq = new UserRequestImpl(requestURI, request, response);
			} catch(Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
			RestSecurityBean securityBean = (RestSecurityBean)CoreSpringFactory.getBean(RestSecurityBean.class);
			Identity identity = securityBean.getIdentity(token);
			int loginStatus = AuthHelper.doHeadlessLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq, true);
			if(loginStatus == AuthHelper.LOGIN_OK) {
				String renewedToken = securityBean.renewToken(token);
				if(renewedToken != null) {
					response.setHeader(RestSecurityHelper.SEC_TOKEN, renewedToken);
					synchronized(uress) {
						chain.doFilter(request, response);
					}
				} else response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	private void followSession(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws IOException, ServletException {
		UserSession uress = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(request);
		if(uress != null && uress.isAuthenticated()) {
			UserRequest ureq = null;
			try{
				//upon creation URL is checked for
				String requestURI = getRequestURI(request);
				ureq = new UserRequestImpl(requestURI, request, response);
			} catch(NumberFormatException nfe) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
			synchronized(uress) {
				try {
					chain.doFilter(request, response);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private boolean isWebappHelperInitiated() {
		if(Settings.isJUnitTest()) {
			return true;
		}
		return WebappHelper.getServletContextPath() != null;
	}
	
	private String getRequestURI(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		if(StringHelper.containsNonWhitespace(requestURI)) {
			try {
				requestURI = URLDecoder.decode(requestURI, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("", e);
			}
		}
		return requestURI;
	}

	private String getLoginUrl() {
		if(LOGIN_URL == null && isWebappHelperInitiated()) {
			String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
			LOGIN_URL = context + "/auth";
		}
		return LOGIN_URL;
	}

	private List<String> getAlwaysEnabledURIs() {
		if(alwaysEnabledUrls == null && isWebappHelperInitiated() ) {
			String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
			List<String > urls = new ArrayList<>();
			urls.add(context + "/i18n");
			urls.add(context + "/api");
			urls.add(context + "/ping");
			urls.add(context + "/openmeetings");
			urls.add(context + "/system");
			urls.add(context + "/onlyoffice");
			urls.add(context + "/office365");
			alwaysEnabledUrls = urls;
		}
		return alwaysEnabledUrls;
	}

	private List<String> getOpenURIs() {
		if(openUrls == null && isWebappHelperInitiated()) {
			String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
			List<String > urls = new ArrayList<>();
			urls.add(context + "/i18n");
			urls.add(context + "/api");
			urls.add(context + "/ping");
			urls.add(context + "/application.wadl");
			urls.add(context + "/application.html");
			urls.add(context + "/wadl");
			urls.add(context + "/registration");
			urls.add(context + "/openmeetings");
			urls.add(context + "/onlyoffice");
			urls.add(context + "/office365");
			openUrls = urls;
		}
		return openUrls;
	}

	private List<String> getIPProtectedURIs() {
		if(ipProtectedUrls == null && isWebappHelperInitiated()) {
			String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
			List<String > urls  = new ArrayList<>();
			urls.add(context + "/system");
			ipProtectedUrls = urls;
		}
		return ipProtectedUrls;
	}
}
