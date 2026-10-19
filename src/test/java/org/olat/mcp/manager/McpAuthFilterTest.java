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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.assertj.core.api.Assertions;
import org.hibernate.proxy.HibernateProxy;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModule;
import org.olat.login.LoginModule;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.core.WhoAmIToolProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The pipeline of the filter against the real token service, profiles,
 * identity cache, rate limiter and audit log.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAuthFilterTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private McpIdentityCache identityCache;
	@Autowired
	private ApiAuditLogService auditLogService;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	private boolean enabledBefore;
	private boolean auditEnabledBefore;
	private boolean rateLimitEnabledBefore;
	private int maxParallelNodeBefore;

	@Before
	public void enableModule() {
		enabledBefore = mcpModule.isEnabled();
		auditEnabledBefore = mcpModule.isAuditLogEnabled();
		rateLimitEnabledBefore = mcpModule.isRateLimitEnabled();
		maxParallelNodeBefore = mcpModule.getRateLimitMaxParallelNode();
		mcpModule.setEnabled(true);
		mcpModule.setAuditLogEnabled(true);
		mcpModule.setRateLimitEnabled(true);
	}

	@After
	public void restoreModule() {
		mcpModule.setEnabled(enabledBefore);
		mcpModule.setAuditLogEnabled(auditEnabledBefore);
		mcpModule.setRateLimitEnabled(rateLimitEnabledBefore);
		mcpModule.setRateLimitMaxParallelNode(maxParallelNodeBefore);
	}

	private static MockHttpServletRequest request(String bearer) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/olat/mcp");
		request.setContextPath("/olat");
		request.setServletPath("/mcp");
		request.setRemoteAddr("10.0.0." + (int)(Math.random() * 250 + 1));
		request.addHeader("Accept", "application/json, text/event-stream");
		request.addHeader("Content-Type", "application/json");
		if(bearer != null) {
			request.addHeader("Authorization", "Bearer " + bearer);
		}
		request.setContent("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}".getBytes());
		return request;
	}

	/**
	 * Puts an entry with a chosen creation time into the identity cache, to
	 * simulate an entry older than the re-validation time.
	 */
	private void putEntry(String tokenHash, McpIdentityEntry entry) {
		coordinatorManager.getCoordinator().getCacher()
			.<String,McpIdentityEntry>getCache(McpIdentityCache.CACHE_TYPE, McpIdentityCache.CACHE_NAME)
			.put(tokenHash, entry);
	}

	private MockHttpServletResponse run(MockHttpServletRequest request, FilterChain chain) throws IOException, ServletException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		new McpAuthFilter().doFilter(request, response, chain);
		return response;
	}

	private McpTokenCreation tokenWithProfile(Identity identity, Integer rateLimitOverride, String... tools) {
		McpProfile profile = mcpService.createProfile("test-" + UUID.randomUUID().toString().substring(0, 12), "Filter test", McpProfileType.user, defaultOrganisations());
		profile.setTools(Set.of(tools));
		profile.setEnabled(true);
		profile.setRateLimitRequestsPerMinute(rateLimitOverride);
		profile = mcpService.updateProfile(profile);
		mcpService.addGrant(profile, identity);
		dbInstance.commitAndCloseSession();
		McpTokenCreation creation = tokenService.generateToken(identity, "filter test");
		dbInstance.commitAndCloseSession();
		return creation;
	}

	private List<ApiAuditLog> auditRowsOf(Identity identity) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		params.setChannels(List.of(ApiAuditChannel.mcp));
		return auditLogService.search(params, 0, 50);
	}

	private static List<Organisation> defaultOrganisations() {
		return List.of(JunitTestHelper.getDefaultOrganisation());
	}

	@Test
	public void moduleDisabledGives404() throws Exception {
		mcpModule.setEnabled(false);
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = run(request("oomcp_whatever"), chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(404);
		verify(chain, never()).doFilter(any(), any());
	}

	@Test
	public void noBearerGives401WithChallenge() throws Exception {
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = run(request(null), chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(401);
		Assertions.assertThat(response.getHeader("WWW-Authenticate")).startsWith("Bearer");
		Assertions.assertThat(response.getContentAsString()).contains("\"code\":401");
		verify(chain, never()).doFilter(any(), any());
	}

	@Test
	public void unknownTokenGives401() throws Exception {
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = run(request("oomcp_" + UUID.randomUUID()), chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(401);
		verify(chain, never()).doFilter(any(), any());

		MockHttpServletResponse basic = run(request("not-a-token"), chain);
		Assertions.assertThat(basic.getStatus()).isEqualTo(401);
	}

	@Test
	public void foreignOriginGives403() throws Exception {
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest request = request("oomcp_x");
		request.addHeader("Origin", "https://evil.example.org");
		MockHttpServletResponse response = run(request, chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(403);
		verify(chain, never()).doFilter(any(), any());
	}

	@Test
	public void tokenWithoutProfileGives403AndAuditRow() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-1");
		// a token needs no profile at service level, the filter refuses the request
		McpTokenCreation creation = tokenService.generateToken(identity, "no profile");
		dbInstance.commitAndCloseSession();

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = run(request(creation.plaintext()), chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(403);
		Assertions.assertThat(response.getContentAsString()).contains("No MCP access");
		verify(chain, never()).doFilter(any(), any());

		List<ApiAuditLog> rows = auditRowsOf(identity);
		Assertions.assertThat(rows).hasSize(1);
		Assertions.assertThat(rows.get(0).getChannel()).isEqualTo(ApiAuditChannel.mcp);
		Assertions.assertThat(rows.get(0).getStatus()).isEqualTo(403);
		Assertions.assertThat(rows.get(0).getAuthProvider()).isEqualTo(McpTokenService.MCP_AUTH_PROVIDER);
	}

	/**
	 * A valid token without an enabled profile: the second request within
	 * the re-validation time is served from the denied entries of the cache,
	 * without a new validation of the token and without grant resolution.
	 */
	@Test
	public void tokenWithoutProfileIsCached() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-1c");
		McpTokenCreation creation = tokenService.generateToken(identity, "no profile cached");
		dbInstance.commitAndCloseSession();

		McpTokenService tokenServiceSpy = mock(McpTokenService.class, delegatesTo(tokenService));
		McpService mcpServiceSpy = mock(McpService.class, delegatesTo(mcpService));
		McpAuthFilter filter = new McpAuthFilter();
		ReflectionTestUtils.setField(filter, "tokenService", tokenServiceSpy);
		ReflectionTestUtils.setField(filter, "mcpService", mcpServiceSpy);
		FilterChain chain = mock(FilterChain.class);

		MockHttpServletResponse first = new MockHttpServletResponse();
		filter.doFilter(request(creation.plaintext()), first, chain);
		Assertions.assertThat(first.getStatus()).isEqualTo(403);
		MockHttpServletResponse second = new MockHttpServletResponse();
		filter.doFilter(request(creation.plaintext()), second, chain);
		Assertions.assertThat(second.getStatus()).isEqualTo(403);

		verify(tokenServiceSpy, times(1)).validateToken(creation.plaintext());
		verify(mcpServiceSpy, times(1)).resolveGrants(any());
		verify(chain, never()).doFilter(any(), any());
		String tokenHash = tokenService.hash(creation.plaintext());
		Assertions.assertThat(identityCache.getDenied(tokenHash)).isNotNull();
		// a denied entry is no MCP session
		Assertions.assertThat(identityCache.get(tokenHash)).isNull();
		Assertions.assertThat(identityCache.entries())
			.noneMatch(entry -> identity.getKey().equals(entry.getIdentityKey()));
	}

	/**
	 * A valid token without an enabled profile counts in the limit of the
	 * client IP for requests without grants: the limit answers 429 before the 403.
	 */
	@Test
	public void tokenWithoutProfileIpLimitBefore403() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-1l");
		McpTokenCreation creation = tokenService.generateToken(identity, "no profile limited");
		dbInstance.commitAndCloseSession();

		int anonymousBefore = mcpModule.getRateLimitAnonymousRequestsPerMinute();
		try {
			mcpModule.setRateLimitAnonymousRequestsPerMinute(2);
			String ip = randomIp();
			FilterChain chain = mock(FilterChain.class);
			for(int i=0; i<2; i++) {
				MockHttpServletRequest request = request(creation.plaintext());
				request.setRemoteAddr(ip);
				Assertions.assertThat(run(request, chain).getStatus()).isEqualTo(403);
			}
			MockHttpServletRequest limited = request(creation.plaintext());
			limited.setRemoteAddr(ip);
			MockHttpServletResponse response = run(limited, chain);
			Assertions.assertThat(response.getStatus()).isEqualTo(429);
			Assertions.assertThat(response.getHeader(McpAuthFilter.HEADER_RETRY_AFTER)).isNotNull();
			verify(chain, never()).doFilter(any(), any());
		} finally {
			mcpModule.setRateLimitAnonymousRequestsPerMinute(anonymousBefore);
		}
	}

	@Test
	public void tokenWithProfilePassesTheChain() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-2");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest request = request(creation.plaintext());
		MockHttpServletResponse response = run(request, chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(200);
		verify(chain, times(1)).doFilter(any(), any());

		Assertions.assertThat(request.getAttribute(McpToolRegistry.CTX_TOOL_CONTEXT)).isInstanceOf(McpToolContext.class);
		McpToolContext toolContext = (McpToolContext)request.getAttribute(McpToolRegistry.CTX_TOOL_CONTEXT);
		Assertions.assertThat(toolContext.identity().getKey()).isEqualTo(identity.getKey());
		// detached in the tools, a Hibernate proxy breaks queries which fetch it again
		Assertions.assertThat(toolContext.identity()).isNotInstanceOf(HibernateProxy.class);
		Assertions.assertThat(toolContext.grants().isGranted(WhoAmIToolProvider.NAME)).isTrue();
		Assertions.assertThat(toolContext.token().tokenId()).isEqualTo(creation.token().tokenId());
		Assertions.assertThat(toolContext.locale()).isNotNull();

		// the identity is never bound to a container session (no JSESSIONID for an MCP client)
		Assertions.assertThat(request.getSession(false)).isNull();
		Assertions.assertThat(request.getAttribute(McpToolRegistry.CTX_USER_SESSION)).isInstanceOf(UserSession.class);
		UserSession userSession = (UserSession)request.getAttribute(McpToolRegistry.CTX_USER_SESSION);
		Assertions.assertThat(userSession.getSessionInfo().getAuthProvider()).isEqualTo(McpTokenService.MCP_AUTH_PROVIDER);
		Assertions.assertThat(userSession.getIdentity().getKey()).isEqualTo(identity.getKey());

		Assertions.assertThat(response.getHeader(McpAuthFilter.HEADER_RATELIMIT_LIMIT))
			.isEqualTo(Integer.toString(mcpModule.getRateLimitRequestsPerMinute()));
		Assertions.assertThat(request.getAttribute(McpRequestTrace.KEY)).isInstanceOf(McpRequestTrace.class);

		// the second request hits the cache and passes too
		MockHttpServletResponse second = run(request(creation.plaintext()), chain);
		Assertions.assertThat(second.getStatus()).isEqualTo(200);
		Assertions.assertThat(identityCache.get(tokenService.hash(creation.plaintext()))).isNotNull();

		// tools/list is a read, not audited by default
		Assertions.assertThat(auditRowsOf(identity)).isEmpty();
	}

	/**
	 * An MCP login counts as a normal login: the creation of the cache entry
	 * writes the last login, a request served from the cache does not.
	 * The entry carries the request data for the session administration.
	 */
	@Test
	public void lastLoginAndSessionActivity() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-login");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);
		// the profile change event clears the identity cache, it must not arrive between the two requests
		dbInstance.commitAndCloseSession();
		waitMessageAreConsumed();
		Date before = new Date(System.currentTimeMillis() - 1000);

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest request = request(creation.plaintext());
		request.addHeader("User-Agent", "claude-code/2.1 " + "x".repeat(200));
		MockHttpServletResponse response = run(request, chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(200);

		Date lastLogin = securityManager.loadIdentityByKey(identity.getKey()).getLastLogin();
		Assertions.assertThat(lastLogin).isNotNull().isAfterOrEqualTo(before)
			.isAfter(new Date(System.currentTimeMillis() - 60_000));

		McpIdentityEntry entry = identityCache.get(tokenService.hash(creation.plaintext()));
		Assertions.assertThat(entry).isNotNull();
		McpSessionActivity activity = entry.activity();
		Assertions.assertThat(activity.getClientIp()).isEqualTo(request.getRemoteAddr());
		Assertions.assertThat(activity.getUserAgent()).startsWith("claude-code/2.1").hasSize(McpSessionActivity.MAX_USER_AGENT_LENGTH);
		Assertions.assertThat(activity.getFirstRequest()).isEqualTo(entry.createdAt());
		Assertions.assertThat(activity.getLastRequest()).isEqualTo(entry.createdAt());

		// the second request within the cache window writes no login
		dbInstance.commitAndCloseSession();
		Thread.sleep(1100);
		MockHttpServletResponse second = run(request(creation.plaintext()), chain);
		Assertions.assertThat(second.getStatus()).isEqualTo(200);
		Date secondLastLogin = securityManager.loadIdentityByKey(identity.getKey()).getLastLogin();
		Assertions.assertThat(secondLastLogin).isEqualTo(lastLogin);
		Assertions.assertThat(identityCache.get(tokenService.hash(creation.plaintext()))).isSameAs(entry);
		Assertions.assertThat(entry.activity().getLastRequest()).isGreaterThan(entry.createdAt());

		// a re-validation within 5 minutes writes no login either
		String hash = tokenService.hash(creation.plaintext());
		McpIdentityEntry aged = new McpIdentityEntry(entry.userSession(), entry.roles(), entry.grants(), entry.token(),
				entry.createdAt() - McpAuthFilter.REVALIDATE_AFTER_MS - 1000, false, entry.activity());
		identityCache.remove(hash);
		putEntry(hash, aged);
		Thread.sleep(1100);
		MockHttpServletResponse third = run(request(creation.plaintext()), chain);
		Assertions.assertThat(third.getStatus()).isEqualTo(200);
		McpIdentityEntry revalidated = identityCache.get(hash);
		Assertions.assertThat(revalidated).isNotSameAs(aged);
		Assertions.assertThat(revalidated.activity().getFirstRequest()).isEqualTo(activity.getFirstRequest());
		Assertions.assertThat(securityManager.loadIdentityByKey(identity.getKey()).getLastLogin()).isEqualTo(lastLogin);

		// after 5 minutes, the re-validation writes the login again
		revalidated.activity().setLastLoginWritten(System.currentTimeMillis() - McpAuthFilter.LAST_LOGIN_INTERVAL_MS - 1000);
		McpIdentityEntry agedAgain = new McpIdentityEntry(revalidated.userSession(), revalidated.roles(), revalidated.grants(),
				revalidated.token(), revalidated.createdAt() - McpAuthFilter.REVALIDATE_AFTER_MS - 1000, false, revalidated.activity());
		identityCache.remove(hash);
		putEntry(hash, agedAgain);
		MockHttpServletResponse fourth = run(request(creation.plaintext()), chain);
		Assertions.assertThat(fourth.getStatus()).isEqualTo(200);
		Assertions.assertThat(securityManager.loadIdentityByKey(identity.getKey()).getLastLogin()).isAfter(lastLogin);

		identityCache.remove(hash);
	}

	@Test
	public void exceptionInTheChainGetsAReference() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-err");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		doThrow(new IllegalStateException("Broken transport")).when(chain).doFilter(any(), any());
		MockHttpServletRequest request = request(creation.plaintext());
		MockHttpServletResponse response = run(request, chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(500);

		McpRequestTrace trace = (McpRequestTrace)request.getAttribute(McpRequestTrace.KEY);
		Assertions.assertThat(trace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.serverError);
		Assertions.assertThat(trace.getErrorReference()).isNotBlank();

		dbInstance.commitAndCloseSession();
		List<ApiAuditLog> rows = auditRowsOf(identity);
		Assertions.assertThat(rows).hasSize(1);
		Assertions.assertThat(rows.get(0).getStatus()).isEqualTo(500);
		Assertions.assertThat(rows.get(0).getRef()).isEqualTo(trace.getErrorReference());
		Assertions.assertThat(rows.get(0).getPathParams()).contains(trace.getErrorReference());
	}

	@Test
	public void profileOverrideLimitsRequestsPerMinute() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-3");
		McpTokenCreation creation = tokenWithProfile(identity, Integer.valueOf(2), WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse first = run(request(creation.plaintext()), chain);
		Assertions.assertThat(first.getStatus()).isEqualTo(200);
		Assertions.assertThat(first.getHeader(McpAuthFilter.HEADER_RATELIMIT_LIMIT)).isEqualTo("2");
		MockHttpServletResponse second = run(request(creation.plaintext()), chain);
		Assertions.assertThat(second.getStatus()).isEqualTo(200);
		MockHttpServletResponse third = run(request(creation.plaintext()), chain);
		Assertions.assertThat(third.getStatus()).isEqualTo(429);
		Assertions.assertThat(third.getHeader(McpAuthFilter.HEADER_RETRY_AFTER)).isNotNull();
		verify(chain, times(2)).doFilter(any(), any());

		List<ApiAuditLog> rows = auditRowsOf(identity);
		Assertions.assertThat(rows).hasSize(1);
		Assertions.assertThat(rows.get(0).getStatus()).isEqualTo(429);
	}

	@Test
	public void wrongTokenIsBlockedAfterMaxAttempts() throws Exception {
		Assume.assumeTrue(loginModule.isAttackPreventionEnabled());
		String wrong = wrongToken();
		String ip = randomIp();
		FilterChain chain = mock(FilterChain.class);

		int max = loginModule.getAttackPreventionMaxAttempts();
		for(int i=0; i<max; i++) {
			MockHttpServletRequest request = request(wrong);
			request.setRemoteAddr(ip);
			Assertions.assertThat(run(request, chain).getStatus()).isEqualTo(401);
		}
		// the same limit as the UI login: the next attempt with this token is blocked without a lookup
		MockHttpServletRequest blocked = request(wrong);
		blocked.setRemoteAddr(ip);
		MockHttpServletResponse response = run(blocked, chain);
		Assertions.assertThat(response.getStatus()).isEqualTo(429);
		Assertions.assertThat(response.getHeader(McpAuthFilter.HEADER_RETRY_AFTER))
			.isEqualTo(Integer.toString(loginModule.getAttackPreventionTimeoutMin().intValue() * 60));
		// the same token from another IP is blocked too, the token counts, not only the IP
		MockHttpServletRequest otherIp = request(wrong);
		otherIp.setRemoteAddr(randomIp());
		Assertions.assertThat(run(otherIp, chain).getStatus()).isEqualTo(429);

		// a valid token from the same IP passes, the IP limit is IP_ATTEMPTS_FACTOR times higher
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-6");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);
		MockHttpServletRequest valid = request(creation.plaintext());
		valid.setRemoteAddr(ip);
		Assertions.assertThat(run(valid, chain).getStatus()).isEqualTo(200);
		verify(chain, times(1)).doFilter(any(), any());
	}

	@Test
	public void ipIsBlockedAfterManyDifferentWrongTokens() throws Exception {
		Assume.assumeTrue(loginModule.isAttackPreventionEnabled());
		// the anonymous rate limit would stop the test before the attack prevention
		mcpModule.setRateLimitEnabled(false);
		String ip = randomIp();
		FilterChain chain = mock(FilterChain.class);

		int max = loginModule.getAttackPreventionMaxAttempts() * McpAuthFilter.IP_ATTEMPTS_FACTOR;
		for(int i=0; i<max; i++) {
			MockHttpServletRequest request = request(wrongToken());
			request.setRemoteAddr(ip);
			Assertions.assertThat(run(request, chain).getStatus()).isEqualTo(401);
		}
		MockHttpServletRequest blocked = request(wrongToken());
		blocked.setRemoteAddr(ip);
		Assertions.assertThat(run(blocked, chain).getStatus()).isEqualTo(429);

		// the IP is blocked for valid tokens too
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-7");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);
		MockHttpServletRequest validBlocked = request(creation.plaintext());
		validBlocked.setRemoteAddr(ip);
		Assertions.assertThat(run(validBlocked, chain).getStatus()).isEqualTo(429);
		// but not from another IP
		MockHttpServletRequest validOther = request(creation.plaintext());
		validOther.setRemoteAddr(randomIp());
		Assertions.assertThat(run(validOther, chain).getStatus()).isEqualTo(200);
		verify(chain, times(1)).doFilter(any(), any());
	}

	@Test
	public void nodeCapLimitsParallelRequestsOfAllIdentities() throws Exception {
		mcpModule.setRateLimitMaxParallelNode(1);
		Identity firstIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-8");
		McpTokenCreation firstCreation = tokenWithProfile(firstIdentity, null, WhoAmIToolProvider.NAME);
		Identity secondIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-9");
		McpTokenCreation secondCreation = tokenWithProfile(secondIdentity, null, WhoAmIToolProvider.NAME);

		// The first request stays in the chain, and so holds the node slot, until the
		// second request is answered. The release comes only from the finally block
		// below, the timeout only guards against a hanging test: a short hold timeout
		// let the first request leave early when the second request was slow under the
		// load of the full suite, and the second request then passed with 200.
		CountDownLatch inChain = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		AtomicBoolean holdTimedOut = new AtomicBoolean(false);
		FilterChain blockingChain = mock(FilterChain.class);
		doAnswer(invocation -> {
			inChain.countDown();
			if(!release.await(120, TimeUnit.SECONDS)) {
				holdTimedOut.set(true);
			}
			return null;
		}).when(blockingChain).doFilter(any(), any());

		AtomicReference<MockHttpServletResponse> firstResponse = new AtomicReference<>();
		AtomicReference<Exception> firstError = new AtomicReference<>();
		Thread first = new Thread(() -> {
			try {
				firstResponse.set(run(request(firstCreation.plaintext()), blockingChain));
			} catch (Exception e) {
				firstError.set(e);
			}
		});
		first.start();
		try {
			Assertions.assertThat(inChain.await(60, TimeUnit.SECONDS)).isTrue();

			// another identity has free slots of its own, but the node is full
			FilterChain chain = mock(FilterChain.class);
			MockHttpServletResponse rejected = run(request(secondCreation.plaintext()), chain);
			Assertions.assertThat(holdTimedOut.get()).as("The first request left the chain before the second one was answered").isFalse();
			Assertions.assertThat(rejected.getStatus()).isEqualTo(429);
			Assertions.assertThat(rejected.getHeader(McpAuthFilter.HEADER_RETRY_AFTER)).isEqualTo("1");
			Assertions.assertThat(rejected.getContentAsString()).contains("\"code\":429");
			verify(chain, never()).doFilter(any(), any());
		} finally {
			release.countDown();
			first.join(30000);
		}

		Assertions.assertThat(firstError.get()).isNull();
		Assertions.assertThat(firstResponse.get()).isNotNull();
		Assertions.assertThat(firstResponse.get().getStatus()).isEqualTo(200);

		// the slot is released, the second identity passes now
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse passed = run(request(secondCreation.plaintext()), chain);
		Assertions.assertThat(passed.getStatus()).isEqualTo(200);
		verify(chain, times(1)).doFilter(any(), any());
	}

	/**
	 * @return A well formed token (prefix and 33 base62 characters) that no row holds
	 */
	private static String wrongToken() {
		String body = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "");
		return McpTokenService.TOKEN_PREFIX + body.substring(0, 33);
	}

	private static String randomIp() {
		return "10." + (int)(Math.random() * 250 + 1) + "." + (int)(Math.random() * 250 + 1) + "." + (int)(Math.random() * 250 + 1);
	}

	@Test
	public void revokedTokenIsRefusedAtOnce() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-4");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(200);

		tokenService.revokeToken(creation.token().tokenId(), identity);
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(401);
		verify(chain, times(1)).doFilter(any(), any());
	}

	@Test
	public void disabledProfileRemovesAccess() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-5");
		McpTokenCreation creation = tokenWithProfile(identity, null, WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(200);

		McpProfile profile = mcpService.getProfilesOf(identity).get(0);
		profile.setEnabled(false);
		mcpService.updateProfile(profile);
		dbInstance.commitAndCloseSession();

		// the change event clears the cache asynchronously, then the next request resolves no grants
		waitForCondition(() -> identityCache.get(tokenService.hash(creation.plaintext())) == null, 5000);
		Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(403);
		verify(chain, times(1)).doFilter(any(), any());
	}

	@Test
	public void blockedLoginRefusesUsersButNotAdministrators() throws Exception {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-block-1");
		McpTokenCreation userCreation = tokenWithProfile(user, null, WhoAmIToolProvider.NAME);
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-filter-block-2");
		McpTokenCreation adminCreation = tokenWithProfile(admin, null, WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		// the cache entry of the user exists before the block, the flag is read on every request
		Assertions.assertThat(run(request(userCreation.plaintext()), chain).getStatus()).isEqualTo(200);

		boolean blockedBefore = AuthHelper.isLoginBlocked();
		AuthHelper.setLoginBlocked(true);
		try {
			MockHttpServletResponse refused = run(request(userCreation.plaintext()), chain);
			Assertions.assertThat(refused.getStatus()).isEqualTo(403);
			Assertions.assertThat(refused.getContentAsString()).contains("Login is blocked");
			// a valid token is no failed attempt
			Assertions.assertThat(refused.getHeader("WWW-Authenticate")).isNull();

			MockHttpServletResponse adminResponse = run(request(adminCreation.plaintext()), chain);
			Assertions.assertThat(adminResponse.getStatus()).isEqualTo(200);
		} finally {
			AuthHelper.setLoginBlocked(blockedBefore);
		}
		verify(chain, times(2)).doFilter(any(), any());

		// the block is lifted, the user passes again
		Assertions.assertThat(run(request(userCreation.plaintext()), chain).getStatus()).isEqualTo(200);
		verify(chain, times(3)).doFilter(any(), any());
	}

	@Test
	public void assessmentModeLocksParticipant() throws Exception {
		Assume.assumeTrue(assessmentModule.isAssessmentModeEnabled());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-exam-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-filter-exam-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		McpTokenCreation creation = tokenWithProfile(participant, null, WhoAmIToolProvider.NAME);

		FilterChain chain = mock(FilterChain.class);
		// before the exam
		Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(200);

		AssessmentMode mode = createRunningAssessmentMode(entry);
		try {
			// the decision of the UI login
			McpAuthFilter filter = new McpAuthFilter();
			Assertions.assertThat(filter.isAssessmentLocked(participant, securityManager.getRoles(participant), new Date())).isTrue();
			Assertions.assertThat(filter.isAssessmentLocked(author, securityManager.getRoles(author), new Date())).isFalse();
			Assertions.assertThat(filter.isAssessmentLocked(participant, securityManager.getRoles(JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-filter-exam-3")), new Date()))
				.as("An administrator is never locked").isFalse();

			// the lock is computed with the token validation, the next validation sees the mode
			identityCache.remove(tokenService.hash(creation.plaintext()));
			MockHttpServletResponse locked = run(request(creation.plaintext()), chain);
			Assertions.assertThat(locked.getStatus()).isEqualTo(403);
			Assertions.assertThat(locked.getContentAsString()).contains("Assessment in progress");
			Assertions.assertThat(identityCache.get(tokenService.hash(creation.plaintext())).assessmentLocked()).isTrue();
			// the cached entry keeps the lock without a new query
			Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(403);
		} finally {
			assessmentModeManager.delete(mode);
			dbInstance.commitAndCloseSession();
		}

		// after the exam the next validation lifts the lock
		identityCache.remove(tokenService.hash(creation.plaintext()));
		Assertions.assertThat(run(request(creation.plaintext()), chain).getStatus()).isEqualTo(200);
		verify(chain, times(2)).doFilter(any(), any());
	}

	/**
	 * An assessment mode for the course members, started one hour ago and
	 * ending in one hour, the pattern of AssessmentModeManagerTest.
	 */
	private AssessmentMode createRunningAssessmentMode(RepositoryEntry entry) {
		AssessmentMode mode = assessmentModeManager.createAssessmentMode(entry);
		mode.setName("MCP lock");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(AssessmentMode.Target.course);
		mode.setManualBeginEnd(false);
		mode = assessmentModeManager.persist(mode);
		dbInstance.commitAndCloseSession();
		return mode;
	}
}
