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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.restapi.RestConnection;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.manager.ApiAuditLogDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.RolesVO;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks the rows written by the JAX-RS filter for the requests which reach a
 * web service.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditResponseFilterTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	
	@Test
	public void putUserWritesOneRowWithResourceAndMaskedBody()
	throws IOException, URISyntaxException {
		boolean currentBodySetting = restModule.isAuditLogBody();
		restModule.setAuditLogBody(true);
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		int before = rowsOf(admin).size();
		
		UserVO user = new UserVO();
		user.setLogin("audit-put-" + UUID.randomUUID().toString().substring(0, 8));
		user.setFirstName("Audit");
		user.setLastName("Put");
		user.setEmail(user.getLogin() + "@frentix.com");
		user.setPassword("Secret-Audit-1!");
		
		URI uri = conn.getContextURI().path("users").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, user);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		List<ApiAuditLog> rows = rowsOf(admin);
		Assertions.assertThat(rows).hasSize(before + 1);
		ApiAuditLog row = rows.get(0);
		Assert.assertEquals("PUT", row.getMethod());
		Assert.assertEquals(200, row.getStatus());
		Assert.assertEquals("UserWebService", row.getResourceClass());
		Assert.assertEquals("create", row.getResourceMethod());
		Assert.assertEquals(ApiAuditLogService.AUTH_PASSWORD, row.getAuthProvider());
		Assert.assertNotNull(row.getDurationMs());
		Assertions.assertThat(row.getRef()).isNotBlank();
		// the body is stored, the password is masked
		Assertions.assertThat(row.getRequestBody())
			.contains(user.getLogin())
			.contains("\"password\":\"***\"")
			.doesNotContain("Secret-Audit-1!");
		
		restModule.setAuditLogBody(currentBodySetting);
		waitMessageAreConsumed();
	}
	
	@Test
	public void getWritesNoRowByDefault() throws IOException, URISyntaxException {
		boolean currentReadSetting = restModule.isAuditLogReads();
		restModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		int before = rowsOf(admin).size();
		
		URI uri = conn.getContextURI().path("users").path(admin.getKey().toString()).build();
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(before, rowsOf(admin).size());
		
		restModule.setAuditLogReads(currentReadSetting);
		waitMessageAreConsumed();
	}
	
	@Test
	public void getWritesRowWhenReadsEnabled() throws IOException, URISyntaxException {
		boolean currentReadSetting = restModule.isAuditLogReads();
		restModule.setAuditLogReads(true);
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		int before = rowsOf(admin).size();
		
		URI uri = conn.getContextURI().path("users").path(admin.getKey().toString()).build();
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		List<ApiAuditLog> rows = rowsOf(admin);
		Assertions.assertThat(rows.size()).isGreaterThan(before);
		ApiAuditLog row = rows.get(0);
		Assert.assertEquals("GET", row.getMethod());
		Assert.assertEquals("UserWebService", row.getResourceClass());
		Assertions.assertThat(row.getPathParams()).contains("identityKey").contains(admin.getKey().toString());
		// a read has no body to store
		Assert.assertNull(row.getRequestBody());
		
		restModule.setAuditLogReads(currentReadSetting);
		waitMessageAreConsumed();
	}
	
	@Test
	public void forbiddenPostWritesRowWithResource() throws IOException, URISyntaxException {
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("audit-forbidden");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(user);
		RolesVO roles = new RolesVO();
		roles.setAuthor(true);
		
		URI uri = conn.getContextURI().path("users").path(admin.getKey().toString()).path("roles").build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, roles);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		List<ApiAuditLog> rows = rowsOf(user.getIdentity());
		Assertions.assertThat(rows).hasSize(1);
		ApiAuditLog row = rows.get(0);
		Assert.assertEquals(403, row.getStatus());
		Assert.assertEquals("UserWebService", row.getResourceClass());
		Assert.assertEquals("updateRoles", row.getResourceMethod());
	}
	
	@Test
	public void bodyDisabledStoresNoBody() throws IOException, URISyntaxException {
		boolean currentBodySetting = restModule.isAuditLogBody();
		restModule.setAuditLogBody(false);
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		
		UserVO user = new UserVO();
		user.setLogin("audit-nobody-" + UUID.randomUUID().toString().substring(0, 8));
		user.setFirstName("Audit");
		user.setLastName("NoBody");
		user.setEmail(user.getLogin() + "@frentix.com");
		user.setPassword("Secret-Audit-2!");
		
		URI uri = conn.getContextURI().path("users").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, user);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(rowsOf(admin).get(0).getRequestBody());
		
		restModule.setAuditLogBody(currentBodySetting);
		waitMessageAreConsumed();
	}
	
	private List<ApiAuditLog> rowsOf(Identity identity) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		params.setCreatedAfter(DateUtils.addHours(new Date(), -1));
		return auditLogDao.search(params, 0, -1);
	}
}
