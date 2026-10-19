/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.manager;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.ratelimit.RateLimitDecision;
import org.olat.core.util.ratelimit.RequestRateLimiter;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModule;
import org.olat.login.LoginModule;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpService;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.McpTokenValidation;
import org.olat.mcp.tool.McpToolContext;
import org.olat.restapi.audit.ApiAuditLogService;

/**
 * The gate of /mcp. Order per request: module switch, Origin
 * check, bearer token, identity cache or token validation, grants, rate limit
 * (parallel requests of the identity, parallel requests of the node, requests
 * per minute), request attributes for the transport, audit after the
 * response. The bearer
 * is verified on every request, the cache only saves the identity, roles and
 * grants queries. An entry older than {@link #REVALIDATE_AFTER_MS} is
 * re-validated against the token row, so a revocation on another node takes
 * effect within one minute. A wrong bearer counts in the attack prevention
 * of {@link LoginModule} like a wrong password: per token with the limit of
 * the UI login, per client IP with {@link #IP_ATTEMPTS_FACTOR} times that
 * limit.
 * <p>
 * Two rules of the UI login apply to a valid token of a user who is neither
 * administrator nor system administrator (AuthHelper.doLogin):
 * <ul>
 * <li>The administrative "Block login" ({@link AuthHelper#isLoginBlocked()})
 *   refuses every request at once, the flag is read per request.</li>
 * <li>An assessment mode or an assessment inspection that locks the user
 *   refuses every request. The lock is computed with the token validation
 *   and kept in the cache entry, so it takes effect within
 *   {@link #REVALIDATE_AFTER_MS}.</li>
 * </ul>
 * Both answer 403 with a JSON message, like a token without an enabled
 * profile: the token is valid, the owner is only not allowed to work now.
 * A token without an enabled profile counts in the limit of the client IP
 * for requests without grants before the 403, and its entry is cached for
 * {@link #REVALIDATE_AFTER_MS} like an entry with grants.
 * A 401 would count as a failed attempt in the attack prevention and tell
 * the client to authenticate again, which does not help.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAuthFilter implements Filter {

	private static final Logger log = Tracing.createLoggerFor(McpAuthFilter.class);

	public static final String HEADER_RETRY_AFTER = "Retry-After";
	public static final String HEADER_RATELIMIT_LIMIT = "X-RateLimit-Limit";
	public static final String HEADER_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
	public static final String HEADER_RATELIMIT_RESET = "X-RateLimit-Reset";
	public static final String WWW_AUTHENTICATE = "Bearer realm=\"OpenOlat MCP\"";

	/** Re-validate the token row of a cached entry after this time */
	public static final long REVALIDATE_AFTER_MS = 60_000L;
	/** Write the last login of the identity at most once in this interval per token */
	public static final long LAST_LOGIN_INTERVAL_MS = 300_000L;
	/** A wrong bearer counts per client IP with this factor of login.AttackPreventionMaxattempts, many clients share one IP behind NAT */
	public static final int IP_ATTEMPTS_FACTOR = 10;

	private static final String LOGIN_KEY_TOKEN_PREFIX = "mcp:tok:";
	private static final String LOGIN_KEY_IP_PREFIX = "mcp:ip:";

	private static final String RATELIMIT_IDENTITY_PREFIX = "mcp:id:";
	private static final String RATELIMIT_IP_PREFIX = "mcp:ip:";
	/** Key of the in-flight counter of all MCP requests of this node */
	static final String RATELIMIT_NODE_KEY = "mcp:node";

	private McpModule mcpModule;
	private McpService mcpService;
	private McpTokenService tokenService;
	private McpIdentityCache identityCache;
	private RequestRateLimiter rateLimiter;
	private BaseSecurity securityManager;
	private SessionStatsManager statsManager;
	private McpAuditWriter auditWriter;
	private LoginModule loginModule;
	private AssessmentModule assessmentModule;
	private AssessmentModeManager assessmentModeManager;
	private AssessmentInspectionService inspectionService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
			throw new ServletException("Only accept HTTP requests");
		}

		McpModule module = getMcpModule();
		if(module == null || !module.isEnabled()) {
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Tracing.setHttpRequest(httpRequest);
		// the MCP servlet has its own mapping, OpenOLATServlet never counts these requests
		getStatsManager().incrementRequest();
		getStatsManager().incrementConcurrentCounter();
		httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_START_NANOS, Long.valueOf(System.nanoTime()));
		McpRequestTrace trace = new McpRequestTrace();
		httpRequest.setAttribute(McpRequestTrace.KEY, trace);

		McpIdentityEntry entry = null;
		String limiterKey = null;
		boolean slotAcquired = false;
		boolean nodeSlotAcquired = false;
		try {
			if(!isOriginAllowed(httpRequest)) {
				sendJsonError(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
				return;
			}

			String plaintext = bearerOf(httpRequest);
			String tokenHash = StringHelper.containsNonWhitespace(plaintext) ? getTokenService().hash(plaintext) : null;
			if(isLoginBlocked(httpRequest.getRemoteAddr(), tokenHash)) {
				// no cache lookup and no DB query for a blocked token or IP
				skipAudit(httpRequest);
				sendTooManyRequests(httpResponse, attackPreventionTimeoutSeconds());
				return;
			}
			entry = resolveEntry(httpRequest, plaintext, tokenHash);
			if(entry == null) {
				rejectUnauthenticated(httpRequest, httpResponse, module, tokenHash);
				return;
			}
			if(AuthHelper.isLoginBlocked() && !isAdministrator(entry.roles())) {
				// the administrative block of the login, read on every request
				sendJsonError(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Login is blocked by the administrator");
				return;
			}
			if(entry.grants().isEmpty()) {
				// the identity limit needs grants, the limit of the client IP applies instead
				if(!isIpRateLimited(httpRequest, httpResponse, module)) {
					sendJsonError(httpResponse, HttpServletResponse.SC_FORBIDDEN, "No MCP access configured for this account");
				}
				return;
			}
			if(entry.assessmentLocked()) {
				sendJsonError(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Assessment in progress, the account is locked");
				return;
			}

			// rate limit of the token owner
			if(module.isRateLimitEnabled()) {
				limiterKey = RATELIMIT_IDENTITY_PREFIX + entry.getIdentity().getKey();
				RequestRateLimiter limiter = getRateLimiter();
				if(!limiter.acquire(limiterKey, module.getRateLimitMaxParallel())) {
					if(!limiter.markParallelRejection(limiterKey)) {
						skipAudit(httpRequest);
					}
					limiterKey = null;
					sendTooManyRequests(httpResponse, 1);
					return;
				}
				slotAcquired = true;
				// cap of the whole node, keeps request threads and DB connections for the GUI users
				int maxParallelNode = module.getRateLimitMaxParallelNode();
				if(maxParallelNode > 0) {
					if(!limiter.acquire(RATELIMIT_NODE_KEY, maxParallelNode)) {
						if(!limiter.markParallelRejection(RATELIMIT_NODE_KEY)) {
							skipAudit(httpRequest);
						}
						sendTooManyRequests(httpResponse, 1);
						return;
					}
					nodeSlotAcquired = true;
				}
				int limit = entry.grants().requestsPerMinute() == null
						? module.getRateLimitRequestsPerMinute() : entry.grants().requestsPerMinute().intValue();
				RateLimitDecision decision = limiter.check(limiterKey, limit);
				httpResponse.setHeader(HEADER_RATELIMIT_LIMIT, Integer.toString(decision.limit()));
				httpResponse.setHeader(HEADER_RATELIMIT_REMAINING, Integer.toString(decision.remaining()));
				httpResponse.setHeader(HEADER_RATELIMIT_RESET, Long.toString(decision.resetEpochSeconds()));
				if(!decision.allowed()) {
					if(!decision.firstRejection()) {
						skipAudit(httpRequest);
					}
					sendTooManyRequests(httpResponse, decision.retryAfterSeconds());
					return;
				}
			}

			// the transport reads these attributes into its context
			Identity identity = entry.getIdentity();
			Locale locale = localeOf(identity);
			McpToolContext toolContext = new McpToolContext(identity, entry.roles(), entry.grants(), entry.token(), locale, httpRequest.getRemoteAddr());
			httpRequest.setAttribute(McpToolRegistry.CTX_TOOL_CONTEXT, toolContext);
			httpRequest.setAttribute(McpToolRegistry.CTX_USER_SESSION, entry.userSession());
			httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_AUTH_PROVIDER, McpTokenService.MCP_AUTH_PROVIDER);
			// the tool runs on this thread, the registry installs and resets the activity logger around it

			chain.doFilter(httpRequest, httpResponse);
		} catch (Exception e) {
			// an exception outside the registry, for example in the transport
			String reference = trace.getErrorReference();
			if(reference == null) {
				reference = UUID.randomUUID().toString();
				trace.setErrorReference(reference);
			}
			trace.setOutcome(McpRequestTrace.Outcome.serverError);
			log.error("MCP request failed, reference {}", reference, e);
			if(!httpResponse.isCommitted()) {
				sendJsonError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
			}
		} finally {
			if(slotAcquired && limiterKey != null) {
				getRateLimiter().release(limiterKey);
			}
			if(nodeSlotAcquired) {
				getRateLimiter().release(RATELIMIT_NODE_KEY);
			}
			I18nManager.remove18nInfoFromThread();
			DBFactory.getInstance().commitAndCloseSession();
			getAuditWriter().audit(httpRequest, httpResponse, trace, entry);
			Tracing.clearHttpRequest();
			getStatsManager().decrementConcurrentCounter();
		}
	}

	/**
	 * Cached entry or a fresh validation. A cached entry older than
	 * {@link #REVALIDATE_AFTER_MS} is checked against the token row and
	 * rebuilt, so revocations and role changes take effect within a minute.
	 * A cache hit updates the last request time of the entry in place; the
	 * rebuild keeps the first request time and takes the client IP and the
	 * client from the current request.
	 * <p>
	 * An MCP login counts as a normal login for the day, week and month
	 * counts of the administration and for the user lifecycle: the creation
	 * of an entry calls
	 * {@link BaseSecurity#setIdentityLastLogin(Identity)}, which also
	 * computes the planned inactivation and deletion dates again and commits.
	 * A re-validation writes it again only after
	 * {@link #LAST_LOGIN_INTERVAL_MS}, a request served from the cache never.
	 * So the last login is written at most once per 5 minutes and token.
	 * <p>
	 * A valid token whose owner has no enabled profile gets an entry with
	 * empty grants and without session. It is kept in the denied entries of
	 * the cache for {@link #REVALIDATE_AFTER_MS}, so the repeated requests of
	 * such a token cost no query. A profile change drops it on every node.
	 *
	 * @param request The request
	 * @param plaintext The bearer value, may be null
	 * @param tokenHash The hash of the bearer, null without bearer
	 * @return The entry or null if the token is not valid
	 */
	private McpIdentityEntry resolveEntry(HttpServletRequest request, String plaintext, String tokenHash) {
		if(tokenHash == null || !plaintext.startsWith(McpTokenService.TOKEN_PREFIX)) {
			return null;
		}
		McpTokenService service = getTokenService();
		McpIdentityCache cache = getIdentityCache();
		McpIdentityEntry entry = cache.get(tokenHash);
		if(entry != null) {
			if(entry.token() != null && entry.token().isExpired(new Date())) {
				cache.remove(tokenHash);
				return null;
			}
			long now = System.currentTimeMillis();
			if(now - entry.createdAt() < REVALIDATE_AFTER_MS) {
				// in place, a volatile write, no cache put (see McpSessionActivity)
				entry.activity().touch(now);
				return entry;
			}
		} else {
			McpIdentityEntry deniedEntry = cache.getDenied(tokenHash);
			if(deniedEntry != null && (deniedEntry.token() == null || !deniedEntry.token().isExpired(new Date()))
					&& System.currentTimeMillis() - deniedEntry.createdAt() < REVALIDATE_AFTER_MS) {
				return deniedEntry;
			}
		}

		McpTokenValidation validation = service.validateToken(plaintext);
		if(!validation.isValid()) {
			if(entry != null) {
				cache.remove(tokenHash);
			}
			return null;
		}
		Identity identity = validation.token().identity();
		Roles roles = getSecurityManager().getRoles(identity);
		McpGrants grants = getMcpService().resolveGrants(identity);
		if(grants.isEmpty()) {
			// no session for an identity without access, the filter answers 403
			if(entry != null) {
				cache.remove(tokenHash);
			}
			McpIdentityEntry deniedEntry = new McpIdentityEntry(null, roles, McpGrants.EMPTY, validation.token(), System.currentTimeMillis());
			cache.putDenied(tokenHash, deniedEntry);
			return deniedEntry;
		}
		if(entry != null) {
			cache.remove(tokenHash);
		}
		// a valid token clears its failed attempts, like a successful login clears the login name
		getLoginModule().clearFailedLoginAttempts(LOGIN_KEY_TOKEN_PREFIX + tokenHash);
		boolean assessmentLocked = isAssessmentLocked(identity, roles, new Date());
		McpSessionActivity previousActivity = entry == null ? null : entry.activity();
		McpIdentityEntry newEntry = cache.create(tokenHash, request, identity, roles, grants, validation.token(), assessmentLocked, previousActivity);
		// An MCP login counts as a normal login: the last login and the lifecycle dates are
		// written at the creation of the entry, then at a re-validation at most every 5 minutes
		long now = System.currentTimeMillis();
		if(newEntry.activity().isLastLoginDue(now, LAST_LOGIN_INTERVAL_MS)) {
			getSecurityManager().setIdentityLastLogin(identity);
			newEntry.activity().setLastLoginWritten(now);
		}
		return newEntry;
	}

	private static boolean isAdministrator(Roles roles) {
		return roles != null && (roles.isAdministrator() || roles.isSystemAdmin());
	}

	/**
	 * The lock rule of the UI login (AuthHelper.doLogin): an administrator or
	 * system administrator is never locked; any other user is locked while an
	 * assessment mode (lead time, assessment, follow-up, extra time) or an
	 * assessment inspection applies to him. The GUI shows the guard of the
	 * exam instead of the application in this case.
	 *
	 * @param identity The token owner
	 * @param roles The roles of the token owner
	 * @param now The date of the check
	 * @return true if the identity must not use the tools now
	 */
	boolean isAssessmentLocked(IdentityRef identity, Roles roles, Date now) {
		if(identity == null || isAdministrator(roles)) {
			return false;
		}
		AssessmentModule module = getAssessmentModule();
		if(module.isAssessmentModeEnabled() && !getAssessmentModeManager().getAssessmentModeFor(identity).isEmpty()) {
			return true;
		}
		return module.isAssessmentInspectionEnabled()
				&& !getInspectionService().getInspectionFor(identity, now).isEmpty();
	}

	/**
	 * Limits requests without a valid token per IP, counts the failure and
	 * answers 401 with the challenge. The message is uniform, the client
	 * learns nothing about why the token was refused.
	 */
	private void rejectUnauthenticated(HttpServletRequest request, HttpServletResponse response, McpModule module, String tokenHash)
	throws IOException {
		if(isIpRateLimited(request, response, module)) {
			return;
		}
		String ip = request.getRemoteAddr();
		if(tokenHash != null && registerFailedAttempt(ip, tokenHash)) {
			sendTooManyRequests(response, attackPreventionTimeoutSeconds());
			return;
		}
		response.setHeader("WWW-Authenticate", WWW_AUTHENTICATE);
		sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	}

	/**
	 * The limit of the requests per client IP without grants
	 * (mcp.ratelimit.anonymous.requests.per.minute): requests without a valid
	 * token, and requests of a valid token without enabled profile.
	 *
	 * @return true if the limit is exhausted and the 429 is sent
	 */
	private boolean isIpRateLimited(HttpServletRequest request, HttpServletResponse response, McpModule module)
	throws IOException {
		if(module.isRateLimitEnabled()) {
			RateLimitDecision decision = getRateLimiter().check(RATELIMIT_IP_PREFIX + request.getRemoteAddr(),
					module.getRateLimitAnonymousRequestsPerMinute());
			if(!decision.allowed()) {
				if(!decision.firstRejection()) {
					skipAudit(request);
				}
				sendTooManyRequests(response, decision.retryAfterSeconds());
				return true;
			}
		}
		return false;
	}

	/**
	 * The attack prevention of the login (login.attackPreventionEnabled,
	 * login.AttackPreventionMaxattempts, login.AttackPreventionTimeoutmin)
	 * applied to bearers. A wrong token is blocked after the maximum of
	 * attempts like a login name, the client IP after IP_ATTEMPTS_FACTOR
	 * times the maximum, because many clients can share one IP.
	 *
	 * @param ip The client IP
	 * @param tokenHash The hash of the bearer, null without bearer
	 * @return true if the token or the IP is blocked
	 */
	private boolean isLoginBlocked(String ip, String tokenHash) {
		LoginModule module = getLoginModule();
		return (tokenHash != null && module.isLoginBlocked(LOGIN_KEY_TOKEN_PREFIX + tokenHash))
				|| module.isLoginBlocked(LOGIN_KEY_IP_PREFIX + ip, IP_ATTEMPTS_FACTOR);
	}

	/**
	 * Counts a wrong bearer for the token and for the IP.
	 *
	 * @param ip The client IP
	 * @param tokenHash The hash of the bearer
	 * @return true if the token or the IP reached the limit with this attempt
	 */
	private boolean registerFailedAttempt(String ip, String tokenHash) {
		LoginModule module = getLoginModule();
		boolean tokenBlocked = module.registerFailedLoginAttempt(LOGIN_KEY_TOKEN_PREFIX + tokenHash, 1);
		boolean ipBlocked = module.registerFailedLoginAttempt(LOGIN_KEY_IP_PREFIX + ip, IP_ATTEMPTS_FACTOR);
		if(ipBlocked) {
			log.warn("Too many failed MCP token attempts from IP {}", ip);
		} else if(tokenBlocked) {
			log.info("Too many failed attempts with the same MCP token from IP {}", ip);
		}
		return tokenBlocked || ipBlocked;
	}

	private int attackPreventionTimeoutSeconds() {
		Integer minutes = getLoginModule().getAttackPreventionTimeoutMin();
		return (minutes == null || minutes.intValue() <= 0 ? 5 : minutes.intValue()) * 60;
	}

	/**
	 * The MCP specification requires the check against DNS rebinding. Clients
	 * like Claude Code send no Origin; a browser-based client sends its own.
	 *
	 * @param request The request
	 * @return true if no Origin header is present or it matches this instance
	 */
	static boolean isOriginAllowed(HttpServletRequest request) {
		String origin = request.getHeader("Origin");
		if(!StringHelper.containsNonWhitespace(origin) || "null".equalsIgnoreCase(origin)) {
			return !"null".equalsIgnoreCase(origin);
		}
		try {
			URI own = URI.create(Settings.getServerContextPathURI());
			URI other = URI.create(origin.trim());
			return own.getScheme() != null && own.getScheme().equalsIgnoreCase(other.getScheme())
					&& own.getHost() != null && own.getHost().equalsIgnoreCase(other.getHost())
					&& portOf(own) == portOf(other);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static int portOf(URI uri) {
		if(uri.getPort() > 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	static String bearerOf(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if(authorization == null) {
			return null;
		}
		String trimmed = authorization.trim();
		if(trimmed.length() > 7 && trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}
		return null;
	}

	private static Locale localeOf(Identity identity) {
		String language = identity.getUser() == null || identity.getUser().getPreferences() == null
				? null : identity.getUser().getPreferences().getLanguage();
		return I18nManager.getInstance().getLocaleOrDefault(language);
	}

	private static void skipAudit(HttpServletRequest request) {
		request.setAttribute(ApiAuditLogService.REQ_ATTR_DONE, Boolean.TRUE);
	}

	private static void sendTooManyRequests(HttpServletResponse response, int retryAfterSeconds) throws IOException {
		response.setHeader(HEADER_RETRY_AFTER, Integer.toString(retryAfterSeconds));
		sendJsonError(response, 429, "Too many requests, retry after " + retryAfterSeconds + " seconds");
	}

	private static void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
	}

	@Override
	public void destroy() {
		//
	}

	private McpModule getMcpModule() {
		if(mcpModule == null && CoreSpringFactory.containsBean("mcpModule")) {
			mcpModule = CoreSpringFactory.getImpl(McpModule.class);
		}
		return mcpModule;
	}

	private McpService getMcpService() {
		if(mcpService == null) {
			mcpService = CoreSpringFactory.getImpl(McpService.class);
		}
		return mcpService;
	}

	private McpTokenService getTokenService() {
		if(tokenService == null) {
			tokenService = CoreSpringFactory.getImpl(McpTokenService.class);
		}
		return tokenService;
	}

	private McpIdentityCache getIdentityCache() {
		if(identityCache == null) {
			identityCache = CoreSpringFactory.getImpl(McpIdentityCache.class);
		}
		return identityCache;
	}

	private RequestRateLimiter getRateLimiter() {
		if(rateLimiter == null) {
			rateLimiter = CoreSpringFactory.getImpl(RequestRateLimiter.class);
		}
		return rateLimiter;
	}

	private BaseSecurity getSecurityManager() {
		if(securityManager == null) {
			securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		}
		return securityManager;
	}

	private SessionStatsManager getStatsManager() {
		if(statsManager == null) {
			statsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
		}
		return statsManager;
	}

	private McpAuditWriter getAuditWriter() {
		if(auditWriter == null) {
			auditWriter = new McpAuditWriter(getMcpModule(), CoreSpringFactory.getImpl(ApiAuditLogService.class));
		}
		return auditWriter;
	}

	private LoginModule getLoginModule() {
		if(loginModule == null) {
			loginModule = CoreSpringFactory.getImpl(LoginModule.class);
		}
		return loginModule;
	}

	private AssessmentModule getAssessmentModule() {
		if(assessmentModule == null) {
			assessmentModule = CoreSpringFactory.getImpl(AssessmentModule.class);
		}
		return assessmentModule;
	}

	private AssessmentModeManager getAssessmentModeManager() {
		if(assessmentModeManager == null) {
			assessmentModeManager = CoreSpringFactory.getImpl(AssessmentModeManager.class);
		}
		return assessmentModeManager;
	}

	private AssessmentInspectionService getInspectionService() {
		if(inspectionService == null) {
			inspectionService = CoreSpringFactory.getImpl(AssessmentInspectionService.class);
		}
		return inspectionService;
	}
}
