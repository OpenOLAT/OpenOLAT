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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.IsolationLevel;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfileChangedEvent;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Cache of the identities behind the tokens, keyed by the token hash. The
 * Infinispan local cache McpManager-identity holds one entry per token
 * hash. Every entry is put with the idle timeout of
 * {@link McpModule#getSessionTimeout()}. The cache is defined in the code,
 * not in infinispan-config.xml: its size follows
 * {@link McpModule#getSessionCacheSize()} (mcp.session.cache.size), read
 * when the cache starts. Above this size the least recently used entry is
 * dropped and its next request validates the token again.
 * <p>
 * The user session of an entry is detached: it has no HTTP session and is
 * not signed on in the UserSessionManager. An MCP request never creates,
 * reads or changes an HTTP session, so the MCP response never carries a
 * session cookie that could open the GUI, the REST API or WebDAV outside the
 * grants of the token. The entry and its session end together, when the
 * cache evicts or drops the entry. {@link #size()} is the number of MCP
 * clients of this node for the administration.
 * <p>
 * A valid token whose owner has no enabled profile gets no session. Its
 * entry, without session and with empty grants, lives in the second local
 * cache McpManager-denied for {@link McpAuthFilter#REVALIDATE_AFTER_MS}, so
 * repeated requests of such a token cost no query. These entries never
 * count as MCP sessions. Every node drops the entries of both caches on a
 * McpProfileChangedEvent; a revoked token is removed by its hash.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class McpIdentityCache implements GenericEventListener {

	static final String CACHE_TYPE = "McpManager";
	static final String CACHE_NAME = "identity";
	static final String DENIED_CACHE_NAME = "denied";
	private static final int DEFAULT_TIMEOUT = 300;
	private static final int DEFAULT_SIZE = 20000;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private CoordinatorManager coordinatorManager;

	private CacheWrapper<String,McpIdentityEntry> cache;
	/** The same cache, without wrapper, to iterate the values for the administration */
	private Cache<String,McpIdentityEntry> infinispanCache;
	/** The entries of the valid tokens without enabled profile, without session */
	private CacheWrapper<String,McpIdentityEntry> deniedCache;

	@PostConstruct
	public void initCache() {
		Cacher cacher = coordinatorManager.getCoordinator().getCacher();
		defineCacheConfiguration(cacher.getCacheContainer(), CACHE_NAME, getTimeout());
		defineCacheConfiguration(cacher.getCacheContainer(), DENIED_CACHE_NAME, getDeniedLifespan());
		cache = cacher.getCache(CACHE_TYPE, CACHE_NAME);
		deniedCache = cacher.getCache(CACHE_TYPE, DENIED_CACHE_NAME);
		EmbeddedCacheManager cacheManager = cacher.getCacheContainer();
		if(cacheManager != null) {
			infinispanCache = cacheManager.getCache(CACHE_TYPE + "-" + CACHE_NAME);
		}
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, McpProfileChangedEvent.ORES);
	}

	/**
	 * The settings of a local cache in infinispan-config.xml, with the size
	 * of mcp.session.cache.size and the given idle timeout as default. Every
	 * entry is put with its own expiration anyway.
	 *
	 * @param cacheManager The cache manager
	 * @param name The name of the cache in the type McpManager
	 * @param maxIdleSeconds The default idle timeout in seconds
	 */
	private void defineCacheConfiguration(EmbeddedCacheManager cacheManager, String name, int maxIdleSeconds) {
		String cacheName = CACHE_TYPE + "-" + name;
		if(cacheManager == null || cacheManager.getCacheConfiguration(cacheName) != null) {
			return;
		}

		int size = mcpModule.getSessionCacheSize() > 0 ? mcpModule.getSessionCacheSize() : DEFAULT_SIZE;
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.memory()
			.whenFull(EvictionStrategy.REMOVE)
			.storage(StorageType.HEAP)
			.maxCount(size);
		builder.expiration()
			.maxIdle(maxIdleSeconds * 1000l)
			.wakeUpInterval(5000l);
		builder.transaction()
			.transactionMode(TransactionMode.NON_TRANSACTIONAL);
		builder.locking()
			.concurrencyLevel(1000)
			.useLockStriping(false)
			.lockAcquisitionTimeout(15000)
			.isolationLevel(IsolationLevel.READ_COMMITTED);
		builder.statistics()
			.enable();
		builder.encoding()
			.mediaType("application/x-java-object");
		cacheManager.defineConfiguration(cacheName, builder.build());
	}

	@PreDestroy
	public void destroy() {
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, McpProfileChangedEvent.ORES);
	}

	@Override
	public void event(Event event) {
		if(event instanceof McpProfileChangedEvent) {
			clear();
		}
	}

	/**
	 * @param tokenHash The SHA-256 hex of the plaintext
	 * @return The entry or null
	 */
	public McpIdentityEntry get(String tokenHash) {
		if(tokenHash == null) return null;
		return cache.get(tokenHash);
	}

	/**
	 * @param tokenHash The SHA-256 hex of the plaintext
	 * @return The entry of a valid token without enabled profile, or null
	 */
	public McpIdentityEntry getDenied(String tokenHash) {
		if(tokenHash == null) return null;
		return deniedCache.get(tokenHash);
	}

	/**
	 * Caches the entry of a valid token whose owner has no enabled profile,
	 * for {@link McpAuthFilter#REVALIDATE_AFTER_MS}. The entry has no
	 * session and does not count as MCP session.
	 *
	 * @param tokenHash The SHA-256 hex of the plaintext
	 * @param entry The entry with empty grants
	 */
	public void putDenied(String tokenHash, McpIdentityEntry entry) {
		if(tokenHash == null || entry == null) return;
		deniedCache.put(tokenHash, entry, getDeniedLifespan(), getDeniedLifespan());
	}

	private static int getDeniedLifespan() {
		return (int)(McpAuthFilter.REVALIDATE_AFTER_MS / 1000l);
	}

	/**
	 * Builds the detached non-GUI user session and caches the entry, without
	 * assessment lock and without previous activity, see
	 * {@link #create(String, HttpServletRequest, Identity, Roles, McpGrants, McpToken, boolean, McpSessionActivity)}.
	 *
	 * @param tokenHash The SHA-256 hex of the plaintext
	 * @param request The current request, for the session and the client IP
	 * @param identity The owner of the token
	 * @param roles The roles of the identity
	 * @param grants The resolved grants, not empty
	 * @param token The token metadata
	 * @return The cached entry, without assessment lock
	 */
	public McpIdentityEntry create(String tokenHash, HttpServletRequest request, Identity identity, Roles roles,
			McpGrants grants, McpToken token) {
		return create(tokenHash, request, identity, roles, grants, token, false, null);
	}

	/**
	 * Builds the detached non-GUI user session and caches the entry. The
	 * session is never signed on through the UserSessionManager, it only
	 * carries the identity, the client IP and the auth provider MCP for the
	 * user activity logger. The client IP and the client (User-Agent) come
	 * from the request; the first request time comes from the previous
	 * activity, so it survives the re-validation of the token. A denied
	 * entry of the token is dropped.
	 *
	 * @param tokenHash The SHA-256 hex of the plaintext
	 * @param request The current request, for the session and the client IP
	 * @param identity The owner of the token
	 * @param roles The roles of the identity
	 * @param grants The resolved grants, not empty
	 * @param token The token metadata
	 * @param assessmentLocked true if an assessment mode or an assessment inspection locks the identity
	 * @param previousActivity The activity of the entry before the re-validation, null for a new entry
	 * @return The cached entry
	 */
	public McpIdentityEntry create(String tokenHash, HttpServletRequest request, Identity identity, Roles roles,
			McpGrants grants, McpToken token, boolean assessmentLocked, McpSessionActivity previousActivity) {
		if(tokenHash == null || request == null || identity == null || roles == null || grants == null || token == null) {
			throw new IllegalArgumentException("All arguments are mandatory to cache an MCP identity");
		}

		UserSession usess = createDetachedUserSession(request, identity, roles);
		long now = System.currentTimeMillis();
		McpSessionActivity activity = McpSessionActivity.of(request, previousActivity, now);
		McpIdentityEntry entry = new McpIdentityEntry(usess, roles, grants, token, now, assessmentLocked, activity);
		cache.put(tokenHash, entry, -1, getTimeout());
		deniedCache.remove(tokenHash);
		return entry;
	}

	private int getTimeout() {
		return mcpModule.getSessionTimeout() > 0 ? mcpModule.getSessionTimeout() : DEFAULT_TIMEOUT;
	}

	/**
	 * A user session without HTTP session and without sign on. It never
	 * reaches the servlet container, so no cookie can carry it. The identity
	 * is set on the identity environment directly: UserSession.setIdentity()
	 * registers the session on the global event bus, and only a sign off
	 * removes it again. A detached session is never signed off, so it must
	 * not be registered, else every dropped entry stays in memory.
	 */
	private UserSession createDetachedUserSession(HttpServletRequest request, Identity identity, Roles roles) {
		UserSession usess = new UserSession();
		usess.getIdentityEnvironment().setIdentity(identity);
		usess.setRoles(roles);
		usess.setSessionInfo(createSessionInfo(request, identity));
		usess.setAuthenticated(true);
		usess.reloadPreferences();
		return usess;
	}

	private SessionInfo createSessionInfo(HttpServletRequest request, Identity identity) {
		SessionInfo sinfo = new SessionInfo(identity.getKey());
		User usr = identity.getUser();
		if(usr != null) {
			sinfo.setFirstname(usr.getProperty(UserConstants.FIRSTNAME, null));
			sinfo.setLastname(usr.getProperty(UserConstants.LASTNAME, null));
		}
		sinfo.setFromIP(request.getRemoteAddr());
		sinfo.setAuthProvider(McpTokenService.MCP_AUTH_PROVIDER);
		sinfo.setUserAgent(request.getHeader("User-Agent"));
		sinfo.setSecure(request.isSecure());
		sinfo.setWebModeFromUreq(null);
		return sinfo;
	}

	/**
	 * Drops one entry, for example after a revocation, and the denied entry
	 * of the token. The detached session is not signed on, nothing else
	 * holds it.
	 *
	 * @param tokenHash The SHA-256 hex of the plaintext
	 */
	public void remove(String tokenHash) {
		if(tokenHash == null) return;
		cache.remove(tokenHash);
		deniedCache.remove(tokenHash);
	}

	/**
	 * Drops every entry of this node, the denied entries included.
	 */
	public void clear() {
		cache.clear();
		deniedCache.clear();
	}

	/**
	 * @return The number of entries on this node, for the administration.
	 *         One entry is one MCP session: one per user and token. The
	 *         denied entries do not count.
	 */
	public int size() {
		return cache.size();
	}

	/**
	 * Iterates the values of the local cache. An iteration reads the data
	 * container and, unlike a get, does not count as an access for the
	 * max-idle expiration: viewing the list does not keep an entry alive.
	 * Expired entries are skipped by the iterator. The denied entries are
	 * not listed.
	 *
	 * @return A copy of the entries of this node, never null
	 */
	public List<McpIdentityEntry> entries() {
		if(infinispanCache == null) {
			return List.of();
		}
		List<McpIdentityEntry> entries = new ArrayList<>(infinispanCache.size());
		try(CloseableIterator<McpIdentityEntry> it = infinispanCache.values().iterator()) {
			while(it.hasNext()) {
				McpIdentityEntry entry = it.next();
				if(entry != null) {
					entries.add(entry);
				}
			}
		}
		return entries;
	}

	/**
	 * @param numOfSeconds The window in seconds
	 * @return The number of entries of this node with a request inside the
	 *         window, for the active users of the system information
	 */
	public int countActive(int numOfSeconds) {
		long now = System.currentTimeMillis();
		long window = numOfSeconds * 1000L;
		int count = 0;
		for(McpIdentityEntry entry:entries()) {
			if(entry.activity().isActiveSince(now, window)) {
				count++;
			}
		}
		return count;
	}
}
