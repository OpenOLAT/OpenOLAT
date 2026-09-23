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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.servlets.RequestAbortedException;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.ratelimit.RateLimitDecision;
import org.olat.core.util.ratelimit.RequestRateLimiter;
import org.olat.core.util.session.UserSessionManager;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.restapi.RestModule;
import org.olat.restapi.RestModule.ApiAccess;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.CachedBodyHttpServletRequest;

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
	
	public static final String HEADER_RETRY_AFTER = "Retry-After";
	public static final String HEADER_RATELIMIT_LIMIT = "X-RateLimit-Limit";
	public static final String HEADER_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
	public static final String HEADER_RATELIMIT_RESET = "X-RateLimit-Reset";

	private static List<String> openUrls;
	private static List<String> alwaysEnabledUrls;
	private static List<String> ipProtectedUrls;
	private static List<String> rateLimitExemptUrls;
	private static String LOGIN_URL;
	
	private RestModule restModule;
	private RequestRateLimiter requestRateLimiter;

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
	
	private RestModule getRestModule() {
		if(restModule == null) {
			restModule = CoreSpringFactory.getImpl(RestModule.class);
		}
		return restModule;
	}
	
	private RequestRateLimiter getLimiter() {
		if(requestRateLimiter == null) {
			requestRateLimiter = CoreSpringFactory.getImpl(RequestRateLimiter.class);
		}
		return requestRateLimiter;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	throws ServletException {

		if(request instanceof HttpServletRequest originalRequest && response instanceof HttpServletResponse httpResponse) {
			RestModule restModule = getRestModule();
			SessionStatsManager statsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
			ApiAuditLogService auditLogService = CoreSpringFactory.getImpl(ApiAuditLogService.class);

			// keep a copy of the body of a write call for the audit log
			HttpServletRequest httpRequest = originalRequest;
			if(restModule != null && restModule.isAuditLogEnabled() && restModule.isAuditLogBody()
					&& CachedBodyHttpServletRequest.shouldWrap(originalRequest.getMethod(), originalRequest.getContentType())) {
				httpRequest = new CachedBodyHttpServletRequest(originalRequest, restModule.getAuditLogBodyMaxSize());
				// the JAX-RS filter sees a proxy of the request, not the wrapper itself
				httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_CACHED_REQUEST, httpRequest);
			}
			httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_START_NANOS, Long.valueOf(System.nanoTime()));
			httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_AUTH_PROVIDER, ApiAuditLogService.AUTH_NONE);
			statsManager.incrementRequest();
			statsManager.incrementConcurrentCounter();

			try {
				String requestURI = getRequestURI(httpRequest);
				if(restModule == null || !restModule.isEnabled() && !isRequestURIAlwaysEnabled(requestURI)) {
					httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				// initialize tracing with request, this allows debugging information as IP, User-Agent.
				Tracing.setHttpRequest(httpRequest);
				Tracing.setRequest(httpRequest.getMethod(), requestURI);
				I18nManager.attachI18nInfoToThread(httpRequest);
				ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(httpRequest);

				UserSession uress = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(httpRequest);
				if(uress != null && uress.isContentDelivery()) {
					if(uress.isAuthenticated() && this.isCourseDBIndex(httpRequest)) {
						followSession(httpRequest, httpResponse, chain);
					} else {
						httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
					}
				} else if(isApiDocIndex(httpRequest)) {
					sendSwaggerUI(httpResponse);
				} else if(uress != null && uress.isAuthenticated()) {
					if(restModule.getApiAccess() == ApiAccess.all
							|| (restModule.getApiAccess() == ApiAccess.apikey  && RestModule.RESTAPI_AUTH.equals(uress.getSessionInfo().getAuthProvider()))) {
						followSession(httpRequest, httpResponse, chain);
					} else if(isRequestURIInOpenSpace(requestURI)) {
						followWithoutAuthentication(httpRequest, httpResponse, chain);
					} else {
						sendUnauthorized(httpResponse);
					}
				} else {
					if(isRequestURIInLoginSpace(requestURI)) {
						followForAuthentication(requestURI, uress, httpRequest, httpResponse, chain);
					} else if(isRequestURIInOpenSpace(requestURI)) {
						followWithoutAuthentication(httpRequest, httpResponse, chain);
					} else if(isRequestURIInIPProtectedSpace(requestURI, httpRequest, restModule)) {
						upgradeIpAuthentication(httpRequest, httpResponse);
						followWithoutAuthentication(httpRequest, httpResponse, chain);
					} else if (isRequestTokenValid(httpRequest)) {
						String token = httpRequest.getHeader(RestSecurityHelper.SEC_TOKEN);

						followToken(token, httpRequest, httpResponse, chain);
					} else if (isBasicAuthenticated(httpRequest, httpResponse, requestURI)) {
						followBasicAuthenticated(httpRequest, httpResponse, chain);
					} else  {
						sendUnauthorized(httpResponse);
					}
				}
			} catch (Exception e) {
				log.error("", e);
				try {
					httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (Exception ex) {
					log.error("", ex);
				}
			} finally {
				ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
				I18nManager.remove18nInfoFromThread();
				DBFactory.getInstance().commitAndCloseSession();
				auditAfterRequest(httpRequest, httpResponse, auditLogService);
				Tracing.clearHttpRequest();
				statsManager.decrementConcurrentCounter();
			}
		} else {
			throw new ServletException("Only accept HTTP Request");
		}
	}
	
	/**
	 * Writes the row of a request which did not reach a web service, or which the
	 * JAX-RS filter could not write itself. Runs after the commit of the request,
	 * the service opens a transaction of its own.
	 * 
	 * @param request The request
	 * @param response The response with the final status
	 * @param auditLogService The service
	 */
	private void auditAfterRequest(HttpServletRequest request, HttpServletResponse response, ApiAuditLogService auditLogService) {
		try {
			Object pending = request.getAttribute(ApiAuditLogService.REQ_ATTR_PENDING);
			if(pending instanceof ApiAuditEntry entry) {
				// the change was rolled back, the row of the JAX-RS filter was lost
				auditLogService.logInNewTransaction(entry);
			} else if(request.getAttribute(ApiAuditLogService.REQ_ATTR_DONE) == null) {
				// denied, rejected or failed before a web service was reached
				auditLogService.logInNewTransaction(
						ApiAuditEntry.valueOf(request, response.getStatus(), ApiAuditChannel.rest));
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void sendUnauthorized(HttpServletResponse httpResponse) {
		httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"" + BASIC_AUTH_REALM + "\"");
		httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}
	
	/**
	 * The only way to the filter chain. Takes a slot for a parallel request,
	 * counts the request in the window of the subject and follows the chain.
	 * A request above a limit is rejected at once with the status 429, it never
	 * waits. The check runs outside of the lock of the user session.<br>
	 * The warn line of the access log and the row of the audit log are written
	 * by the audit of the request, as for every request rejected by this filter.
	 * 
	 * @param request The request
	 * @param response The response
	 * @param call The call of the filter chain
	 */
	private void limitAndFollow(HttpServletRequest request, HttpServletResponse response, ChainCall call)
	throws IOException, ServletException {
		RestModule restModule = getRestModule();
		if(!restModule.isRateLimitEnabled() || isRateLimitExempt(getRequestURI(request), request, restModule)) {
			call.proceed();
			return;
		}
		
		RateLimitSubject subject = getRateLimitSubject(request, restModule);
		RequestRateLimiter limiter = getLimiter();
		if(!limiter.acquire(subject.key(), restModule.getRateLimitMaxParallel())) {
			log.debug("Rate limit of parallel requests reached: {} {}", subject.key(), request.getRequestURI());
			sendTooManyRequests(response, 1);
			return;
		}
		
		try {
			RateLimitDecision decision = limiter.check(subject.key(), subject.limitPerMinute());
			response.setHeader(HEADER_RATELIMIT_LIMIT, Integer.toString(decision.limit()));
			response.setHeader(HEADER_RATELIMIT_REMAINING, Integer.toString(decision.remaining()));
			response.setHeader(HEADER_RATELIMIT_RESET, Long.toString(decision.resetEpochSeconds()));
			if(decision.allowed()) {
				call.proceed();
			} else {
				log.debug("Rate limit of requests per minute reached: {} {}", subject.key(), request.getRequestURI());
				sendTooManyRequests(response, decision.retryAfterSeconds());
			}
		} finally {
			limiter.release(subject.key());
		}
	}
	
	/**
	 * An authenticated user is limited by its identity, all other
	 * requests by the IP of the client with the limit for anonymous requests.
	 * 
	 * @param request The request
	 * @param restModule The module with the limits
	 * @return The subject
	 */
	private RateLimitSubject getRateLimitSubject(HttpServletRequest request, RestModule restModule) {
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(request);
		if(usess != null && usess.isAuthenticated() && usess.getIdentity() != null) {
			return new RateLimitSubject("rest:id:" + usess.getIdentity().getKey(), restModule.getRateLimitRequestsPerMinute());
		}
		return new RateLimitSubject("rest:ip:" + request.getRemoteAddr(), restModule.getRateLimitAnonymousRequestsPerMinute());
	}
	
	/**
	 * /ping and /i18n are never limited, /system only if the IP of the
	 * client has access to the system informations.
	 */
	private boolean isRateLimitExempt(String requestURI, HttpServletRequest request, RestModule restModule) {
		List<String> uris = getRateLimitExemptURIs();
		if(uris != null) {
			for(String uri:uris) {
				if(requestURI.startsWith(uri)) {
					return true;
				}
			}
		}
		return isRequestURIInIPProtectedSpace(requestURI, request, restModule);
	}
	
	/**
	 * Send the status 429 with the header Retry-After and a JSON body. The
	 * message is not translated and contains no user input.
	 * 
	 * @param response The response
	 * @param retryAfterSeconds The seconds to wait
	 */
	private void sendTooManyRequests(HttpServletResponse response, int retryAfterSeconds) throws IOException {
		response.setStatus(Status.TOO_MANY_REQUESTS.getStatusCode());
		response.setHeader(HEADER_RETRY_AFTER, Integer.toString(retryAfterSeconds));
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write("{\"code\":429,\"message\":\"Too many requests, retry after "
				+ retryAfterSeconds + " seconds\"}");
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
			String openApiUrl = Settings.getServerContextPathURI() + RestSecurityHelper.SUB_CONTEXT + "/openapi.json";
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
						int loginStatus = doAuthentication(request, response, requestURI, username, password);
						return loginStatus == AuthHelper.LOGIN_OK;
					}
				}
			}
		}
		return false;
	}
	
	private int doAuthentication(HttpServletRequest request, HttpServletResponse response, String requestURI, String username, String pwd) {
		// Block login after 5x failed
		final LoginModule loginModule = CoreSpringFactory.getImpl(LoginModule.class);
		if(loginModule.isLoginBlocked(username)) {
			request.setAttribute(ApiAuditLogService.REQ_ATTR_LOGIN_ATTEMPT, username);
			return AuthHelper.LOGIN_DENIED;
		}
				
		final RestModule restModule = CoreSpringFactory.getImpl(RestModule.class);
		final BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		final RestSecurityBean securityBean = CoreSpringFactory.getImpl(RestSecurityBean.class);
		final AuthenticationDAO authentication = CoreSpringFactory.getImpl(AuthenticationDAO.class);
		
		int loginStatus = -1;
		Identity identity = null;
		String auditAuthProvider = ApiAuditLogService.AUTH_NONE;
		Authentication clientAuthentication = authentication.getAuthentication(username, RestModule.RESTAPI_AUTH, BaseSecurity.DEFAULT_ISSUER);
		if(clientAuthentication == null) {
			if(restModule.getApiAccess() == ApiAccess.all) {
				OLATAuthManager olatAuthenticationSpi = CoreSpringFactory.getImpl(OLATAuthManager.class);
				identity = olatAuthenticationSpi.authenticate(null, username, pwd, new AuthenticationStatus());
				loginStatus = doHeadlessLogin(request, response, requestURI, identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());
				auditAuthProvider = ApiAuditLogService.AUTH_PASSWORD;
			} else {
				loginStatus = AuthHelper.LOGIN_DENIED;
			}
		} else if(securityManager.checkCredentials(clientAuthentication, pwd)) {
			identity = clientAuthentication.getIdentity();
			loginStatus = doHeadlessLogin(request, response, requestURI, identity, RestModule.RESTAPI_AUTH);
			auditAuthProvider = ApiAuditLogService.AUTH_API_KEY;
		}
		
		if (loginStatus == AuthHelper.LOGIN_OK && identity != null) {
			securityManager.setIdentityLastLogin(identity);
			//Forge a new security token
			String token = securityBean.generateToken(identity, request.getSession());
			response.setHeader(RestSecurityHelper.SEC_TOKEN, token);
			auditAuthentication(request, auditAuthProvider);
			Tracing.setIdentity(identity);
		} else {
			loginModule.registerFailedLoginAttempt(username);
			request.setAttribute(ApiAuditLogService.REQ_ATTR_LOGIN_ATTEMPT, username);
		}
		
		return loginStatus;	
	}
	
	/**
	 * Remembers how the request was authenticated, for the audit log and the access log.
	 * 
	 * @param request The request
	 * @param authProvider One of the AUTH constants of the audit log service
	 */
	private void auditAuthentication(HttpServletRequest request, String authProvider) {
		request.setAttribute(ApiAuditLogService.REQ_ATTR_AUTH_PROVIDER, authProvider);
		Tracing.setAuthProvider(authProvider);
	}
	
	private int doHeadlessLogin(HttpServletRequest request, HttpServletResponse response, String requestURI, Identity identity, String provider) {
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for
			ureq = new UserRequestImpl(requestURI, request, response);
		} catch(RequestAbortedException | NumberFormatException nfe) {
			return -1;
		}
		request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
		
		return AuthHelper.doHeadlessLogin(identity, provider, ureq, true);
	}

	private void followBasicAuthenticated(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws ServletException, IOException {
		limitAndFollow(request, response, () -> chain.doFilter(request, response));
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
		limitAndFollow(request, response, () -> chain.doFilter(request, response));
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
		RestSecurityBean securityBean = CoreSpringFactory.getImpl(RestSecurityBean.class);
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
		limitAndFollow(request, response, () -> chain.doFilter(request, response));
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
			auditAuthentication(request, ApiAuditLogService.AUTH_IP);
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
			RestSecurityBean securityBean = CoreSpringFactory.getImpl(RestSecurityBean.class);
			Identity identity = securityBean.getIdentity(token);
			int loginStatus = AuthHelper.doHeadlessLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq, true);
			if(loginStatus == AuthHelper.LOGIN_OK) {
				auditAuthentication(request, ApiAuditLogService.AUTH_TOKEN);
				Tracing.setUserSession(uress);
				String renewedToken = securityBean.renewToken(token);
				if(renewedToken != null) {
					response.setHeader(RestSecurityHelper.SEC_TOKEN, renewedToken);
					limitAndFollow(request, response, () -> {
						synchronized(uress) {
							chain.doFilter(request, response);
						}
					});
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
			if(ApiAuditLogService.AUTH_NONE.equals(request.getAttribute(ApiAuditLogService.REQ_ATTR_AUTH_PROVIDER))) {
				// authenticated by the session cookie, the other branches set their own provider
				auditAuthentication(request, ApiAuditLogService.AUTH_SESSION);
			}
			Tracing.setUserSession(uress);
			limitAndFollow(request, response, () -> {
				synchronized(uress) {
					try {
						chain.doFilter(request, response);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
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
	
	private boolean isApiDocIndex(HttpServletRequest request) {
		String index = WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT + "/api-docs/";
		return index.equalsIgnoreCase(request.getRequestURI());
	}
	
	private boolean isCourseDBIndex(HttpServletRequest request) {
		try {
			String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
			String index = context + "/repo/courses/";
			String requestUri = request.getRequestURI();
			if(requestUri.startsWith(index)) {
				String path = requestUri.substring(index.length(), requestUri.length());
				String[] subPathArray = path.split("/");
				if(subPathArray.length > 2 && StringHelper.isLong(subPathArray[0]) && "db".equals(subPathArray[1])) {
					return true;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
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
			urls.add(context + "/drawio");
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
			urls.add(context + "/drawio");
			urls.add(context + "/onlyoffice");
			urls.add(context + "/office365");
			openUrls = urls;
		}
		return openUrls;
	}

	private List<String> getRateLimitExemptURIs() {
		if(rateLimitExemptUrls == null && isWebappHelperInitiated()) {
			String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
			List<String> urls = new ArrayList<>();
			urls.add(context + "/ping");
			urls.add(context + "/i18n");
			rateLimitExemptUrls = urls;
		}
		return rateLimitExemptUrls;
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
	
	/**
	 * The call of the filter chain. Two callers wrap it in a synchronized
	 * block, the limit is checked outside of it.
	 */
	@FunctionalInterface
	private interface ChainCall {
		void proceed() throws IOException, ServletException;
	}
	
	private record RateLimitSubject(String key, int limitPerMinute) {
		//
	}
}
