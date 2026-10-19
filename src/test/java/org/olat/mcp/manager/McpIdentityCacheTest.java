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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.eviction.EvictionStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.core.util.session.UserSessionModule;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfileChangedEvent;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests the identity cache of the MCP tokens, the MCP user session and the
 * session handling of UserSessionManager for MCP, WebDAV and REST sessions.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpIdentityCacheTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of("openolat_whoami"), null, List.of("default-learner"));

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private McpIdentityCache identityCache;
	@Autowired
	private UserSessionModule sessionModule;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private CoordinatorManager coordinatorManager;

	private int currentTokenMax;

	@Before
	public void saveModuleSettings() {
		currentTokenMax = mcpModule.getUserTokenMax();
		mcpModule.setUserTokenMax(5);
	}

	@After
	public void restoreModuleSettings() {
		mcpModule.setUserTokenMax(currentTokenMax);
	}

	@Test
	public void createAndGet() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-1");
		Roles roles = securityManager.getRoles(id);
		McpToken token = token(id);
		String hash = randomHash();

		MockHttpServletRequest request = mcpRequest();
		McpIdentityEntry entry = identityCache.create(hash, request, id, roles, GRANTS, token);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(entry);
		Assert.assertEquals(id, entry.getIdentity());
		Assert.assertEquals(roles, entry.roles());
		Assert.assertEquals(GRANTS, entry.grants());
		Assert.assertEquals(token, entry.token());
		Assert.assertTrue(entry.createdAt() > 0);

		UserSession usess = entry.userSession();
		Assert.assertTrue(usess.isAuthenticated());
		Assert.assertEquals(id, usess.getIdentity());
		Assert.assertEquals(roles, usess.getRoles());
		SessionInfo sinfo = usess.getSessionInfo();
		Assert.assertFalse(sinfo.isWebDAV());
		Assert.assertFalse(sinfo.isREST());
		Assert.assertNull(sinfo.getSession());
		Assert.assertEquals(McpTokenService.MCP_AUTH_PROVIDER, sinfo.getAuthProvider());
		Assert.assertEquals("10.1.2.3", sinfo.getFromIP());
		Assert.assertTrue(sinfo.toString().contains("authProvider: [" + McpTokenService.MCP_AUTH_PROVIDER + "]"));

		McpIdentityEntry cachedEntry = identityCache.get(hash);
		Assert.assertSame(entry, cachedEntry);
		Assert.assertTrue(identityCache.size() >= 1);

		Assert.assertNull(identityCache.get(randomHash()));
		Assert.assertNull(identityCache.get(null));

		identityCache.remove(hash);
	}

	/**
	 * Security (C1): an MCP request must never get an HTTP session, its
	 * cookie would open the GUI, REST and WebDAV outside the grants.
	 */
	@Test
	public void createWithoutHttpSession() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-2");
		int authenticatedSessions = sessionManager.getNumberOfAuthenticatedUserSessions();
		MockHttpServletRequest request = mcpRequest();
		String hash = randomHash();
		McpIdentityEntry entry = identityCache.create(hash, request, id, securityManager.getRoles(id), GRANTS, token(id));
		dbInstance.commitAndCloseSession();

		Assert.assertNull("No HTTP session for an MCP request", request.getSession(false));
		Assert.assertNull(entry.userSession().getSessionInfo().getSession());
		Assert.assertFalse(sessionManager.getAuthenticatedUserSessions().contains(entry.userSession()));
		Assert.assertEquals(authenticatedSessions, sessionManager.getNumberOfAuthenticatedUserSessions());
		Assert.assertFalse(sessionManager.isSignedOnIdentity(id.getKey()));
		Assert.assertFalse(sessionManager.isOnline(id.getKey()));

		identityCache.remove(hash);
	}

	@Test
	public void existingHttpSessionIsUntouched() {
		Identity guiUser = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-3");
		Identity mcpUser = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-4");
		MockHttpServletRequest request = mcpRequest();
		UserSession guiSession = signOnGuiSession(request, guiUser);
		HttpSession httpSession = request.getSession(false);
		List<String> attributeNames = Collections.list(httpSession.getAttributeNames());
		int maxInactiveInterval = httpSession.getMaxInactiveInterval();

		String hash = randomHash();
		McpIdentityEntry entry = identityCache.create(hash, request, mcpUser, securityManager.getRoles(mcpUser), GRANTS, token(mcpUser));
		dbInstance.commitAndCloseSession();

		// The HTTP session and its GUI session are unchanged
		Assert.assertSame(httpSession, request.getSession(false));
		Assert.assertEquals(attributeNames, Collections.list(httpSession.getAttributeNames()));
		Assert.assertEquals(maxInactiveInterval, httpSession.getMaxInactiveInterval());
		Assert.assertTrue(guiSession.isAuthenticated());
		Assert.assertEquals(guiUser, guiSession.getIdentity());
		Assert.assertNotEquals(McpTokenService.MCP_AUTH_PROVIDER, guiSession.getSessionInfo().getAuthProvider());
		// The MCP entry has its own detached session
		UserSession mcpSession = entry.userSession();
		Assert.assertNotSame(guiSession, mcpSession);
		Assert.assertEquals(mcpUser, mcpSession.getIdentity());
		Assert.assertEquals(McpTokenService.MCP_AUTH_PROVIDER, mcpSession.getSessionInfo().getAuthProvider());

		identityCache.remove(hash);
		Assert.assertTrue("Removing the entry keeps the GUI session", guiSession.isAuthenticated());
		sessionManager.signOffAndClear(guiSession);
	}

	@Test
	public void sameIdentityGetsOwnSessionPerToken() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-5");
		Roles roles = securityManager.getRoles(id);
		String hash1 = randomHash();
		String hash2 = randomHash();
		McpIdentityEntry entry1 = identityCache.create(hash1, mcpRequest(), id, roles, GRANTS, token(id));
		McpIdentityEntry entry2 = identityCache.create(hash2, mcpRequest(), id, roles, GRANTS, token(id));
		dbInstance.commitAndCloseSession();

		Assert.assertNotSame(entry1.userSession(), entry2.userSession());
		identityCache.remove(hash1);
		Assert.assertNull(identityCache.get(hash1));
		Assert.assertSame(entry2, identityCache.get(hash2));
		identityCache.remove(hash2);
	}

	@Test
	public void remove() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-6");
		String hash = randomHash();
		identityCache.create(hash, mcpRequest(), id, securityManager.getRoles(id), GRANTS, token(id));
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(identityCache.get(hash));

		identityCache.remove(hash);

		Assert.assertNull(identityCache.get(hash));
		// removing twice or an unknown hash does nothing
		identityCache.remove(hash);
		identityCache.remove(null);
	}

	@Test
	public void clear() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-8");
		String hash1 = randomHash();
		String hash2 = randomHash();
		identityCache.create(hash1, mcpRequest(), id1, securityManager.getRoles(id1), GRANTS, token(id1));
		identityCache.create(hash2, mcpRequest(), id2, securityManager.getRoles(id2), GRANTS, token(id2));
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(identityCache.size() >= 2);

		identityCache.clear();

		Assert.assertEquals(0, identityCache.size());
		Assert.assertNull(identityCache.get(hash1));
		Assert.assertNull(identityCache.get(hash2));
	}

	@Test
	public void profileChangedEventClearsCache() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-9");
		String hash = randomHash();
		identityCache.create(hash, mcpRequest(), id, securityManager.getRoles(id), GRANTS, token(id));
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(identityCache.get(hash));

		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(new McpProfileChangedEvent(Long.valueOf(1)), McpProfileChangedEvent.ORES);
		waitMessageAreConsumed();

		boolean cleared = waitForCondition(() -> identityCache.get(hash) == null, 5000);
		Assert.assertTrue(cleared);
	}

	@Test
	public void guiLoginKeepsMcpSession() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-10");
		String hash = randomHash();
		McpIdentityEntry entry = identityCache.create(hash, mcpRequest(), id, securityManager.getRoles(id), GRANTS, token(id));
		dbInstance.commitAndCloseSession();

		UserSession guiSession = signOnGuiSession(new MockHttpServletRequest(), id);

		Assert.assertTrue(guiSession.isAuthenticated());
		Assert.assertTrue("A GUI login must not end the MCP session", entry.userSession().isAuthenticated());
		Assert.assertSame(entry, identityCache.get(hash));

		sessionManager.signOffAndClear(guiSession);
		Assert.assertTrue("A GUI logout must not end the MCP session", entry.userSession().isAuthenticated());
		identityCache.remove(hash);
	}

	@Test
	public void guiLoginKeepsWebDAVAndRestSessions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-11");
		UserSession webdavSession = signOnNonGuiSession(new MockHttpServletRequest(), id, "webdav");
		UserSession restSession = signOnNonGuiSession(new MockHttpServletRequest(), id, "rest");

		UserSession guiSession = signOnGuiSession(new MockHttpServletRequest(), id);

		Assert.assertTrue(guiSession.isAuthenticated());
		Assert.assertTrue(webdavSession.isAuthenticated());
		Assert.assertTrue(restSession.isAuthenticated());

		sessionManager.signOffAndClear(guiSession);
		sessionManager.signOffAndClear(webdavSession);
		sessionManager.signOffAndClear(restSession);
	}

	@Test
	public void secondGuiLoginEndsFirstGuiSession() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-12");
		UserSession guiSession1 = signOnGuiSession(new MockHttpServletRequest(), id);
		UserSession guiSession2 = signOnGuiSession(new MockHttpServletRequest(), id);

		Assert.assertFalse("Unchanged behaviour: one GUI session per user", guiSession1.isAuthenticated());
		Assert.assertTrue(guiSession2.isAuthenticated());
		sessionManager.signOffAndClear(guiSession2);
	}

	@Test
	public void httpSessionTimeout() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-13");
		for(String type:new String[] { "webdav", "rest" }) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			UserSession usess = signOnNonGuiSession(request, id, type);
			sessionManager.getUserSession(request, request.getSession());
			Assert.assertEquals("Short container session for " + type, 60, request.getSession().getMaxInactiveInterval());
			sessionManager.signOffAndClear(usess);
		}

		MockHttpServletRequest guiRequest = new MockHttpServletRequest();
		UserSession guiSession = signOnGuiSession(guiRequest, id);
		sessionManager.getUserSession(guiRequest, guiRequest.getSession());
		Assert.assertEquals(sessionModule.getSessionTimeoutAuthenticated(), guiRequest.getSession().getMaxInactiveInterval());
		sessionManager.signOffAndClear(guiSession);
	}

	@Test
	public void revokeTokenRemovesEntry() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-14");
		McpTokenCreation creation = tokenService.generateToken(id, "Cached");
		dbInstance.commitAndCloseSession();
		String hash = tokenService.hash(creation.plaintext());
		identityCache.create(hash, mcpRequest(), id, securityManager.getRoles(id), GRANTS, creation.token());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(identityCache.get(hash));

		Assert.assertTrue(tokenService.revokeToken(creation.token().tokenId(), id));

		Assert.assertNull(identityCache.get(hash));
	}

	@Test
	public void entries() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-16");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-17");
		String hash1 = randomHash();
		String hash2 = randomHash();
		String hash3 = randomHash();
		McpIdentityEntry entry1 = identityCache.create(hash1, mcpRequest(), id1, securityManager.getRoles(id1), GRANTS, token(id1));
		McpIdentityEntry entry2 = identityCache.create(hash2, mcpRequest(), id2, securityManager.getRoles(id2), GRANTS, token(id2));
		// one session per user and token: a second token of the same user is a second entry
		McpIdentityEntry entry3 = identityCache.create(hash3, mcpRequest(), id2, securityManager.getRoles(id2), GRANTS, token(id2));
		dbInstance.commitAndCloseSession();

		List<McpIdentityEntry> entries = identityCache.entries();
		Assert.assertTrue(entries.contains(entry1));
		Assert.assertTrue(entries.contains(entry2));
		Assert.assertTrue(entries.contains(entry3));
		Assert.assertEquals(identityCache.size(), entries.size());
		Assert.assertEquals(id1.getKey(), entry1.getIdentityKey());

		identityCache.remove(hash2);
		List<McpIdentityEntry> afterRemove = identityCache.entries();
		Assert.assertTrue(afterRemove.contains(entry1));
		Assert.assertFalse(afterRemove.contains(entry2));
		Assert.assertTrue(afterRemove.contains(entry3));

		identityCache.remove(hash1);
		identityCache.remove(hash3);
	}

	@Test
	public void requestData() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-18");
		Roles roles = securityManager.getRoles(id);
		String hash = randomHash();
		MockHttpServletRequest request = mcpRequest();
		request.removeHeader("User-Agent");
		String longAgent = "Claude-User/1.0 " + "a".repeat(300);
		request.addHeader("User-Agent", longAgent);
		McpIdentityEntry entry = identityCache.create(hash, request, id, roles, GRANTS, token(id));
		dbInstance.commitAndCloseSession();

		McpSessionActivity activity = entry.activity();
		Assert.assertEquals("10.1.2.3", activity.getClientIp());
		Assert.assertEquals(McpSessionActivity.MAX_USER_AGENT_LENGTH, activity.getUserAgent().length());
		Assert.assertEquals(longAgent.substring(0, McpSessionActivity.MAX_USER_AGENT_LENGTH), activity.getUserAgent());
		Assert.assertEquals(entry.createdAt(), activity.getFirstRequest());
		Assert.assertEquals(entry.createdAt(), activity.getLastRequest());
		Assert.assertEquals("Test", entry.token().label());

		// the last request is updated in place on every request
		long first = activity.getFirstRequest();
		activity.touch(first + 1);
		Assert.assertEquals(first + 1, activity.getLastRequest());
		activity.touch(first);
		Assert.assertEquals("Never goes back", first + 1, activity.getLastRequest());
		Assert.assertTrue(activity.isActiveSince(first + 60_001, 60_000));
		Assert.assertFalse(activity.isActiveSince(first + 60_002, 60_000));
		Assert.assertTrue(identityCache.countActive(60) >= 1);

		// the last login write
		Assert.assertTrue(activity.isLastLoginDue(first, McpAuthFilter.LAST_LOGIN_INTERVAL_MS));
		activity.setLastLoginWritten(first);
		Assert.assertFalse(activity.isLastLoginDue(first + 60_000, McpAuthFilter.LAST_LOGIN_INTERVAL_MS));
		Assert.assertTrue(activity.isLastLoginDue(first + McpAuthFilter.LAST_LOGIN_INTERVAL_MS, McpAuthFilter.LAST_LOGIN_INTERVAL_MS));

		// the re-validation keeps the first request and takes IP and client of the current request
		MockHttpServletRequest revalidation = mcpRequest();
		revalidation.setRemoteAddr("10.9.8.7");
		String hash2 = randomHash();
		McpIdentityEntry rebuilt = identityCache.create(hash2, revalidation, id, roles, GRANTS, token(id), false, activity);
		Assert.assertEquals(first, rebuilt.activity().getFirstRequest());
		Assert.assertEquals("10.9.8.7", rebuilt.activity().getClientIp());
		Assert.assertEquals("mcp-test-client/1.0", rebuilt.activity().getUserAgent());
		Assert.assertEquals(rebuilt.createdAt(), rebuilt.activity().getLastRequest());
		Assert.assertEquals("The last login write survives the re-validation", first, rebuilt.activity().getLastLoginWritten());

		// an entry built without request data still has an activity
		McpIdentityEntry plain = new McpIdentityEntry(null, roles, GRANTS, token(id), 1234L);
		Assert.assertNotNull(plain.activity());
		Assert.assertEquals(1234L, plain.activity().getFirstRequest());
		Assert.assertNull(plain.activity().getClientIp());

		identityCache.remove(hash);
		identityCache.remove(hash2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithoutGrants() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cache-15");
		identityCache.create(randomHash(), mcpRequest(), id, securityManager.getRoles(id), null, token(id));
	}

	private UserSession signOnGuiSession(MockHttpServletRequest request, Identity identity) {
		HttpSession httpSession = request.getSession(true);
		UserSession usess = sessionManager.getUserSession(request, httpSession);
		usess.setIdentity(identity);
		usess.setRoles(securityManager.getRoles(identity));
		SessionInfo sinfo = new SessionInfo(identity.getKey(), httpSession);
		sinfo.setAuthProvider("OLAT");
		sinfo.setWebModeFromUreq(null);
		usess.setSessionInfo(sinfo);
		sessionManager.signOn(usess);
		dbInstance.commitAndCloseSession();
		return usess;
	}

	private UserSession signOnNonGuiSession(MockHttpServletRequest request, Identity identity, String type) {
		HttpSession httpSession = request.getSession(true);
		UserSession usess = sessionManager.getUserSession(request, httpSession);
		usess.setIdentity(identity);
		usess.setRoles(securityManager.getRoles(identity));
		SessionInfo sinfo = new SessionInfo(identity.getKey(), httpSession);
		sinfo.setAuthProvider("OLAT");
		sinfo.setWebDAV("webdav".equals(type));
		sinfo.setREST("rest".equals(type));
		sinfo.setWebModeFromUreq(null);
		usess.setSessionInfo(sinfo);
		sessionManager.signOn(usess);
		dbInstance.commitAndCloseSession();
		return usess;
	}

	private MockHttpServletRequest mcpRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
		request.setRemoteAddr("10.1.2.3");
		request.addHeader("User-Agent", "mcp-test-client/1.0");
		return request;
	}

	private McpToken token(Identity identity) {
		return new McpToken(Long.valueOf(-1), "t_test", "Test", identity, new Date(), null);
	}

	private String randomHash() {
		return tokenService.hash(UUID.randomUUID().toString());
	}

	@Test
	public void cacheSizeFollowsTheProperty() {
		Configuration configuration = coordinatorManager.getCoordinator().getCacher().getCacheContainer()
				.getCacheConfiguration(McpIdentityCache.CACHE_TYPE + "-" + McpIdentityCache.CACHE_NAME);
		Assert.assertNotNull(configuration);
		Assert.assertEquals(mcpModule.getSessionCacheSize(), configuration.memory().maxCount());
		Assert.assertEquals(EvictionStrategy.REMOVE, configuration.memory().whenFull());
		Assert.assertTrue(configuration.statistics().enabled());
	}
}
