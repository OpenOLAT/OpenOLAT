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
package org.olat.mcp;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditChannelSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Configuration of the Model Context Protocol (MCP) server. The endpoint
 * /mcp exposes OpenOlat tools to AI assistants. The module is independent
 * of the REST API switch. Rate limiting uses the shared
 * {@link org.olat.core.util.ratelimit.RequestRateLimiter}, the audit log
 * the shared table o_api_audit_log with the channel {@link ApiAuditChannel#mcp}.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service("mcpModule")
public class McpModule extends AbstractSpringModule implements ConfigOnOff, ApiAuditChannelSettings {

	public static final String MCP_ENDPOINT = "/mcp";

	private static final String ENABLED = "mcp.enabled";
	private static final String USER_TOKEN_GENERATION_ENABLED = "mcp.user.token.generation.enabled";
	private static final String USER_TOKEN_MAX = "mcp.user.token.max";
	private static final String TOKEN_VALIDITY_DAYS = "mcp.token.validity.days";
	private static final String SESSION_TIMEOUT = "mcp.session.timeout";
	private static final String SESSION_CACHE_SIZE = "mcp.session.cache.size";
	private static final String RATELIMIT_ENABLED = "mcp.ratelimit.enabled";
	private static final String RATELIMIT_REQUESTS_PER_MINUTE = "mcp.ratelimit.requests.per.minute";
	private static final String RATELIMIT_MAX_PARALLEL = "mcp.ratelimit.max.parallel";
	private static final String RATELIMIT_MAX_PARALLEL_NODE = "mcp.ratelimit.max.parallel.node";
	private static final String RATELIMIT_ANONYMOUS_REQUESTS_PER_MINUTE = "mcp.ratelimit.anonymous.requests.per.minute";
	private static final String AUDITLOG_ENABLED = "mcp.auditlog.enabled";
	private static final String AUDITLOG_READS = "mcp.auditlog.reads";
	private static final String AUDITLOG_BODY = "mcp.auditlog.body";
	private static final String AUDITLOG_BODY_MAXSIZE = "mcp.auditlog.body.maxsize";
	private static final String REQUEST_MAXSIZE = "mcp.request.maxsize";
	private static final String DEFAULT_PROFILES_CREATED = "mcp.default.profiles.created";

	@Value("${mcp.enabled:false}")
	private String enabled;
	@Value("${mcp.user.token.generation.enabled:true}")
	private String userTokenGenerationEnabled;
	@Value("${mcp.user.token.max:5}")
	private int userTokenMax;
	@Value("${mcp.token.validity.days:365}")
	private int tokenValidityDays;
	@Value("${mcp.session.timeout:300}")
	private int sessionTimeout;
	@Value("${mcp.session.cache.size:20000}")
	private int sessionCacheSize;
	@Value("${mcp.ratelimit.enabled:true}")
	private String rateLimitEnabled;
	@Value("${mcp.ratelimit.requests.per.minute:120}")
	private int rateLimitRequestsPerMinute;
	@Value("${mcp.ratelimit.max.parallel:2}")
	private int rateLimitMaxParallel;
	@Value("${mcp.ratelimit.max.parallel.node:32}")
	private int rateLimitMaxParallelNode;
	@Value("${mcp.ratelimit.anonymous.requests.per.minute:30}")
	private int rateLimitAnonymousRequestsPerMinute;
	@Value("${mcp.auditlog.enabled:true}")
	private String auditLogEnabled;
	@Value("${mcp.auditlog.reads:false}")
	private String auditLogReads;
	@Value("${mcp.auditlog.body:true}")
	private String auditLogBody;
	@Value("${mcp.auditlog.body.maxsize:16384}")
	private int auditLogBodyMaxSize;
	@Value("${mcp.request.maxsize:10485760}")
	private int requestMaxSize;

	private boolean defaultProfilesCreated;

	@Autowired
	public McpModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		enabled = getStringPropertyValue(ENABLED, enabled);
		userTokenGenerationEnabled = getStringPropertyValue(USER_TOKEN_GENERATION_ENABLED, userTokenGenerationEnabled);
		userTokenMax = getIntPropertyValue(USER_TOKEN_MAX, userTokenMax);
		tokenValidityDays = getIntPropertyValue(TOKEN_VALIDITY_DAYS, tokenValidityDays);
		sessionTimeout = getIntPropertyValue(SESSION_TIMEOUT, sessionTimeout);
		sessionCacheSize = getIntPropertyValue(SESSION_CACHE_SIZE, sessionCacheSize);
		rateLimitEnabled = getStringPropertyValue(RATELIMIT_ENABLED, rateLimitEnabled);
		rateLimitRequestsPerMinute = getIntPropertyValue(RATELIMIT_REQUESTS_PER_MINUTE, rateLimitRequestsPerMinute);
		rateLimitMaxParallel = getIntPropertyValue(RATELIMIT_MAX_PARALLEL, rateLimitMaxParallel);
		rateLimitMaxParallelNode = getIntPropertyValue(RATELIMIT_MAX_PARALLEL_NODE, rateLimitMaxParallelNode);
		rateLimitAnonymousRequestsPerMinute = getIntPropertyValue(RATELIMIT_ANONYMOUS_REQUESTS_PER_MINUTE, rateLimitAnonymousRequestsPerMinute);
		auditLogEnabled = getStringPropertyValue(AUDITLOG_ENABLED, auditLogEnabled);
		auditLogReads = getStringPropertyValue(AUDITLOG_READS, auditLogReads);
		auditLogBody = getStringPropertyValue(AUDITLOG_BODY, auditLogBody);
		auditLogBodyMaxSize = getIntPropertyValue(AUDITLOG_BODY_MAXSIZE, auditLogBodyMaxSize);
		requestMaxSize = getIntPropertyValue(REQUEST_MAXSIZE, requestMaxSize);

		String defaultProfilesObj = getStringPropertyValue(DEFAULT_PROFILES_CREATED, "false");
		defaultProfilesCreated = "true".equals(defaultProfilesObj);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return "true".equals(enabled);
	}

	public void setEnabled(boolean enable) {
		enabled = enable ? "true" : "false";
		setStringProperty(ENABLED, enabled, true);
	}

	/**
	 * @return true if users may generate MCP tokens in their personal settings
	 */
	public boolean isUserTokenGenerationEnabled() {
		return "true".equals(userTokenGenerationEnabled);
	}

	public void setUserTokenGenerationEnabled(boolean enable) {
		userTokenGenerationEnabled = enable ? "true" : "false";
		setStringProperty(USER_TOKEN_GENERATION_ENABLED, userTokenGenerationEnabled, true);
	}

	/**
	 * @return The maximum number of tokens per user, 0 for no limit
	 */
	public int getUserTokenMax() {
		return userTokenMax;
	}

	public void setUserTokenMax(int max) {
		userTokenMax = max;
		setIntProperty(USER_TOKEN_MAX, max, true);
	}

	/**
	 * @return The validity of a token in days after its creation, 0 for no expiry
	 */
	public int getTokenValidityDays() {
		return tokenValidityDays;
	}

	public void setTokenValidityDays(int days) {
		tokenValidityDays = days;
		setIntProperty(TOKEN_VALIDITY_DAYS, days, true);
	}

	/**
	 * @return The idle timeout in seconds of the cached identity of a token
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int seconds) {
		sessionTimeout = seconds;
		setIntProperty(SESSION_TIMEOUT, seconds, true);
	}

	/**
	 * @return The maximum number of cached token identities per node, read
	 *         when the cache starts. Above this number the least recently
	 *         used entry is dropped.
	 */
	public int getSessionCacheSize() {
		return sessionCacheSize;
	}

	public boolean isRateLimitEnabled() {
		return "true".equals(rateLimitEnabled);
	}

	public void setRateLimitEnabled(boolean enable) {
		rateLimitEnabled = enable ? "true" : "false";
		setStringProperty(RATELIMIT_ENABLED, rateLimitEnabled, true);
	}

	/**
	 * @return Requests per minute of an authenticated token owner (fixed window of 60 seconds)
	 */
	public int getRateLimitRequestsPerMinute() {
		return rateLimitRequestsPerMinute;
	}

	public void setRateLimitRequestsPerMinute(int requests) {
		rateLimitRequestsPerMinute = requests;
		setIntProperty(RATELIMIT_REQUESTS_PER_MINUTE, requests, true);
	}

	/**
	 * @return Maximum parallel requests of an authenticated token owner
	 */
	public int getRateLimitMaxParallel() {
		return rateLimitMaxParallel;
	}

	public void setRateLimitMaxParallel(int maxParallel) {
		rateLimitMaxParallel = maxParallel;
		setIntProperty(RATELIMIT_MAX_PARALLEL, maxParallel, true);
	}

	/**
	 * @return Maximum parallel MCP requests of the whole node, 0 disables the cap.
	 *         It protects the request threads and the database connections of
	 *         the users of the web interface.
	 */
	public int getRateLimitMaxParallelNode() {
		return rateLimitMaxParallelNode;
	}

	public void setRateLimitMaxParallelNode(int maxParallel) {
		rateLimitMaxParallelNode = maxParallel;
		setIntProperty(RATELIMIT_MAX_PARALLEL_NODE, maxParallel, true);
	}

	/**
	 * @return Requests per minute of a client IP for requests without a valid token
	 */
	public int getRateLimitAnonymousRequestsPerMinute() {
		return rateLimitAnonymousRequestsPerMinute;
	}

	public void setRateLimitAnonymousRequestsPerMinute(int requests) {
		rateLimitAnonymousRequestsPerMinute = requests;
		setIntProperty(RATELIMIT_ANONYMOUS_REQUESTS_PER_MINUTE, requests, true);
	}

	@Override
	public ApiAuditChannel getAuditChannel() {
		return ApiAuditChannel.mcp;
	}

	@Override
	public boolean isAuditLogEnabled() {
		return "true".equals(auditLogEnabled);
	}

	public void setAuditLogEnabled(boolean enable) {
		auditLogEnabled = enable ? "true" : "false";
		setStringProperty(AUDITLOG_ENABLED, auditLogEnabled, true);
	}

	@Override
	public boolean isAuditLogReads() {
		return "true".equals(auditLogReads);
	}

	public void setAuditLogReads(boolean enable) {
		auditLogReads = enable ? "true" : "false";
		setStringProperty(AUDITLOG_READS, auditLogReads, true);
	}

	@Override
	public boolean isAuditLogBody() {
		return "true".equals(auditLogBody);
	}

	public void setAuditLogBody(boolean enable) {
		auditLogBody = enable ? "true" : "false";
		setStringProperty(AUDITLOG_BODY, auditLogBody, true);
	}

	@Override
	public int getAuditLogBodyMaxSize() {
		return auditLogBodyMaxSize;
	}

	public void setAuditLogBodyMaxSize(int maxSize) {
		auditLogBodyMaxSize = maxSize;
		setIntProperty(AUDITLOG_BODY_MAXSIZE, maxSize, true);
	}

	/**
	 * @return The maximum size of a request body in bytes, read at the start
	 *         of the server; it bounds the file content a tool can upload
	 */
	public int getRequestMaxSize() {
		return requestMaxSize;
	}

	/**
	 * @return true once the disabled default profiles were seeded on this instance
	 */
	public boolean isDefaultProfilesCreated() {
		return defaultProfilesCreated;
	}

	public void setDefaultProfilesCreated(boolean created) {
		defaultProfilesCreated = created;
		setStringProperty(DEFAULT_PROFILES_CREATED, created ? "true" : "false", true);
	}
}
