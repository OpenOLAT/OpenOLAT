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
package org.olat.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.core.util.ratelimit.manager.RequestRateLimiterImpl;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.manager.ApiAuditLogDAO;
import org.olat.restapi.security.RestApiLoginFilter;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Sends more requests than allowed through the HTTP stack. The first
 * request authenticates with Basic authentication, the next ones use the
 * session cookie, all are limited by the identity.
 *
 * Initial date: 23 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class RestApiRateLimitTest extends OlatRestTestCase {

	private static final int SC_TOO_MANY_REQUESTS = 429;

	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;

	@Test
	public void burstOverLimit() throws IOException, URISyntaxException {
		int currentRateLimit = restModule.getRateLimitRequestsPerMinute();
		final int LIMIT = 5;
		final int REJECTED = 65;
		restModule.setRateLimitRequestsPerMinute(LIMIT);
		waitMessageAreConsumed();
		waitForFreshWindow();
		
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rate-limit-1");
		RestConnection conn = new RestConnection(id);
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path("me").build();

		for(int i=1; i<=LIMIT; i++) {
			HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
			Assert.assertEquals("Request " + i, 200, response.getStatusLine().getStatusCode());
			Assert.assertEquals(Integer.toString(LIMIT), response.getFirstHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT).getValue());
			Assert.assertEquals(Integer.toString(LIMIT - i), response.getFirstHeader(RestApiLoginFilter.HEADER_RATELIMIT_REMAINING).getValue());
			EntityUtils.consume(response.getEntity());
		}

		for(int i=0; i<REJECTED; i++) {
			HttpResponse rejected = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
			Assert.assertEquals(SC_TOO_MANY_REQUESTS, rejected.getStatusLine().getStatusCode());
			Assert.assertNotNull(rejected.getFirstHeader(RestApiLoginFilter.HEADER_RETRY_AFTER));
			Assert.assertEquals("0", rejected.getFirstHeader(RestApiLoginFilter.HEADER_RATELIMIT_REMAINING).getValue());
			Assertions.assertThat(rejected.getFirstHeader("Content-Type").getValue()).startsWith("application/json");
			Assertions.assertThat(EntityUtils.toString(rejected.getEntity())).contains("\"code\":429");
		}
		conn.shutdown();
		dbInstance.commitAndCloseSession();

		// only the first rejected request of the window is audited
		List<ApiAuditLog> rows = rowsOf(id);
		Assertions.assertThat(rows)
			.hasSize(1)
			.allSatisfy(row -> {
				Assert.assertEquals(ApiAuditChannel.rest, row.getChannel());
				Assert.assertEquals("GET", row.getMethod());
				Assertions.assertThat(row.getPath()).endsWith("/users/me");
				Assert.assertNotNull(row.getIp());
			});
		
		restModule.setRateLimitRequestsPerMinute(currentRateLimit);
		waitMessageAreConsumed();
	}

	@Test
	public void otherIdentityNotLimited() throws IOException, URISyntaxException {
		int currentRateLimit = restModule.getRateLimitRequestsPerMinute();
		final int LIMIT = 5;
		restModule.setRateLimitRequestsPerMinute(LIMIT);
		waitMessageAreConsumed();
		waitForFreshWindow();
		
		IdentityWithLogin id1 = JunitTestHelper.createAndPersistRndUser("rate-limit-2");
		IdentityWithLogin id2 = JunitTestHelper.createAndPersistRndUser("rate-limit-3");
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path("me").build();

		RestConnection conn1 = new RestConnection(id1);
		int lastStatus = -1;
		for(int i=0; i<=LIMIT; i++) {
			HttpResponse response = conn1.execute(conn1.createGet(uri, MediaType.APPLICATION_JSON, true));
			lastStatus = response.getStatusLine().getStatusCode();
			EntityUtils.consume(response.getEntity());
		}
		Assert.assertEquals(SC_TOO_MANY_REQUESTS, lastStatus);
		conn1.shutdown();

		RestConnection conn2 = new RestConnection(id2);
		HttpResponse response = conn2.execute(conn2.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals(Integer.toString(LIMIT - 1), response.getFirstHeader(RestApiLoginFilter.HEADER_RATELIMIT_REMAINING).getValue());
		EntityUtils.consume(response.getEntity());
		conn2.shutdown();
		
		restModule.setRateLimitRequestsPerMinute(currentRateLimit);
		waitMessageAreConsumed();
	}

	@Test
	public void pingIsExempt() throws IOException, URISyntaxException {
		int currentAnonymousRateLimit = restModule.getRateLimitAnonymousRequestsPerMinute();
		restModule.setRateLimitAnonymousRequestsPerMinute(2);
		waitMessageAreConsumed();
		waitForFreshWindow();

		RestConnection conn = new RestConnection();
		URI uri = UriBuilder.fromUri(getContextURI()).path("ping").build();
		for(int i=0; i<5; i++) {
			HttpResponse response = conn.execute(conn.createGet(uri, MediaType.TEXT_PLAIN, false));
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNull(response.getFirstHeader(RestApiLoginFilter.HEADER_RATELIMIT_LIMIT));
			EntityUtils.consume(response.getEntity());
		}
		conn.shutdown();
		
		restModule.setRateLimitAnonymousRequestsPerMinute(currentAnonymousRateLimit);
		waitMessageAreConsumed();
	}

	private List<ApiAuditLog> rowsOf(IdentityWithLogin id) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setCreatedAfter(DateUtils.addHours(new Date(), -1));
		params.setIdentityKey(id.getIdentity().getKey());
		return auditLogDao.search(params, 0, -1).stream()
				.filter(row -> row.getStatus() == SC_TOO_MANY_REQUESTS)
				.toList();
	}

	/**
	 * Prevents a burst to be split by the start of a new window.
	 */
	private void waitForFreshWindow() {
		long secondInWindow = Instant.now().getEpochSecond() % RequestRateLimiterImpl.WINDOW_SECONDS;
		if(secondInWindow >= RequestRateLimiterImpl.WINDOW_SECONDS - 10) {
			sleep((int)(RequestRateLimiterImpl.WINDOW_SECONDS - secondInWindow + 1) * 1000);
		}
	}
}
