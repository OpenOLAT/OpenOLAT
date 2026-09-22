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
package org.olat.restapi.audit;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.restapi.RestConnection;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.manager.ApiAuditLogDAO;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks the rows written by the servlet filter for the requests which never
 * reach a web service.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class RestApiAuditFilterTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	
	@Test
	public void unauthorizedGetWritesRowWithoutIdentity() throws IOException, URISyntaxException {
		String marker = "audit-no-auth-" + UUID.randomUUID();
		RestConnection conn = new RestConnection();
		URI uri = conn.getContextURI().path("users").path(marker).build();
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, false));
		Assert.assertEquals(401, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		List<ApiAuditLog> rows = rowsFor(marker);
		Assertions.assertThat(rows).hasSize(1);
		ApiAuditLog row = rows.get(0);
		Assert.assertEquals(401, row.getStatus());
		Assert.assertNull(row.getIdentity());
		Assert.assertEquals(ApiAuditLogService.AUTH_NONE, row.getAuthProvider());
		Assert.assertEquals(ApiAuditChannel.rest, row.getChannel());
		Assert.assertEquals("GET", row.getMethod());
		Assert.assertNotNull(row.getIp());
		Assert.assertNotNull(row.getDurationMs());
	}
	
	@Test
	public void failedBasicAuthStoresLoginAttemptNotPassword() throws IOException, URISyntaxException {
		String login = "audit-wrong-" + UUID.randomUUID().toString().substring(0, 8);
		String marker = "audit-basic-fail-" + UUID.randomUUID();
		RestConnection conn = new RestConnection(login, "wrong-password-xyz");
		URI uri = conn.getContextURI().path("users").path(marker).build();
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(401, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		// the client sends the request twice: the unauthenticated challenge and the
		// retry with the credentials, only the retry carries the login attempt
		List<ApiAuditLog> rows = rowsFor(marker);
		Assertions.assertThat(rows).isNotEmpty();
		Assertions.assertThat(rows).allMatch(row -> row.getStatus() == 401);
		Assertions.assertThat(rows).extracting(ApiAuditLog::getLoginAttempt).contains(login);
		// the password must never reach the audit log
		Assertions.assertThat(rows).allSatisfy(row -> {
			Assertions.assertThat(row.getRequestBody()).isNull();
			Assertions.assertThat(row.getIdentity()).isNull();
			if(row.getLoginAttempt() != null) {
				Assertions.assertThat(row.getLoginAttempt()).doesNotContain("wrong-password-xyz");
			}
		});
	}
	
	@Test
	public void moduleDisabledWritesForbiddenRow() throws IOException, URISyntaxException {
		String marker = "audit-disabled-" + UUID.randomUUID();
		restModule.setEnabled(false);
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		URI uri = conn.getContextURI().path("users").path(marker).build();
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();

		List<ApiAuditLog> rows = rowsFor(marker);
		Assertions.assertThat(rows).hasSize(1);
		Assert.assertEquals(403, rows.get(0).getStatus());
		
		restModule.setEnabled(true);
		waitMessageAreConsumed();
	}
	
	@Test
	public void auditDisabledWritesNoRow() throws IOException, URISyntaxException {
		String marker = "audit-off-" + UUID.randomUUID();
		restModule.setAuditLogEnabled(false);
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection();
		URI uri = conn.getContextURI().path("users").path(marker).build();
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, false));
		Assert.assertEquals(401, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		List<ApiAuditLog> rows = rowsFor(marker);
		Assertions.assertThat(rows).isEmpty();

		restModule.setAuditLogEnabled(true);
		waitMessageAreConsumed();
	}
	
	private List<ApiAuditLog> rowsFor(String pathContains) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setCreatedAfter(DateUtils.addHours(new Date(), -1));
		return auditLogDao.search(params, 0, -1).stream()
				.filter(row -> row.getPath() != null && row.getPath().contains(pathContains))
				.toList();
	}
}
