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

import jakarta.servlet.http.HttpServletRequest;

/**
 * The request data of one MCP session (one entry of the identity cache) for
 * the session administration: the client IP, the client (user agent), the
 * time of the first and of the last request, and the time the last login of
 * the identity was written.
 * <p>
 * The last request time is written in place on every request: the identity
 * cache is a local Infinispan cache with heap object storage, the cache
 * holds this object by reference, so a request never puts the entry again.
 * The write is one volatile field write, cheaper than a cache put, and keeps
 * the time exact for the "active users" of the last minute in the system
 * information. The re-validation of the token after
 * {@link McpAuthFilter#REVALIDATE_AFTER_MS} creates a new activity with the
 * current request and keeps the first request time and the time of the last
 * login write.
 *
 * Initial date: 14 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public final class McpSessionActivity {

	/** The length of the user agent kept for the administration */
	public static final int MAX_USER_AGENT_LENGTH = 120;

	private final String clientIp;
	private final String userAgent;
	private final long firstRequest;
	private volatile long lastRequest;
	private volatile long lastLoginWritten;

	public McpSessionActivity(String clientIp, String userAgent, long firstRequest, long lastRequest) {
		this(clientIp, userAgent, firstRequest, lastRequest, 0L);
	}

	public McpSessionActivity(String clientIp, String userAgent, long firstRequest, long lastRequest, long lastLoginWritten) {
		this.clientIp = clientIp;
		this.userAgent = truncate(userAgent);
		this.firstRequest = firstRequest;
		this.lastRequest = lastRequest;
		this.lastLoginWritten = lastLoginWritten;
	}

	/**
	 * @param request The current request, may be null
	 * @param previous The activity of the entry before the re-validation, may be null
	 * @param now The current time in milliseconds
	 * @return The activity with the IP and the client of the request; the
	 *         first request and the time of the last login write of the
	 *         previous activity survive the re-validation
	 */
	public static McpSessionActivity of(HttpServletRequest request, McpSessionActivity previous, long now) {
		String ip = request == null ? null : request.getRemoteAddr();
		String agent = request == null ? null : request.getHeader("User-Agent");
		long first = previous == null || previous.firstRequest <= 0 ? now : previous.firstRequest;
		long loginWritten = previous == null ? 0L : previous.lastLoginWritten;
		return new McpSessionActivity(ip, agent, first, now, loginWritten);
	}

	static String truncate(String value) {
		if(value == null) return null;
		String trimmed = value.trim();
		if(trimmed.length() > MAX_USER_AGENT_LENGTH) {
			trimmed = trimmed.substring(0, MAX_USER_AGENT_LENGTH);
		}
		return trimmed.isEmpty() ? null : trimmed;
	}

	/**
	 * Sets the last request time, in place, on every request.
	 *
	 * @param now The current time in milliseconds
	 */
	public void touch(long now) {
		if(now > lastRequest) {
			lastRequest = now;
		}
	}

	/**
	 * @param now The current time in milliseconds
	 * @param intervalMs The minimal time between two writes of the last login
	 * @return true if the last login was never written for this session or
	 *         the last write is at least the interval ago
	 */
	public boolean isLastLoginDue(long now, long intervalMs) {
		return lastLoginWritten <= 0 || now - lastLoginWritten >= intervalMs;
	}

	public long getLastLoginWritten() {
		return lastLoginWritten;
	}

	public void setLastLoginWritten(long lastLoginWritten) {
		this.lastLoginWritten = lastLoginWritten;
	}

	/**
	 * @param now The current time in milliseconds
	 * @param windowMs The window
	 * @return true if the last request lies inside the window
	 */
	public boolean isActiveSince(long now, long windowMs) {
		return now - lastRequest <= windowMs;
	}

	public String getClientIp() {
		return clientIp;
	}

	/**
	 * @return The User-Agent header, at most 120 characters
	 */
	public String getUserAgent() {
		return userAgent;
	}

	public long getFirstRequest() {
		return firstRequest;
	}

	public long getLastRequest() {
		return lastRequest;
	}
}
