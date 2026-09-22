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
package org.olat.restapi.audit.manager;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogSearchParams.OrderBy;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.ApiAuditStatusClass;
import org.olat.restapi.audit.model.ApiAuditLogImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	
	@Test
	public void createAndLoad() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-1");
		ApiAuditLog row = createRow(id, ApiAuditChannel.rest, "POST", 200, "UserWebService");
		dbInstance.commitAndCloseSession();
		
		ApiAuditLog loaded = auditLogDao.loadByKey(row.getKey());
		Assert.assertNotNull(loaded);
		Assert.assertNotNull(loaded.getCreationDate());
		Assert.assertEquals(ApiAuditChannel.rest, loaded.getChannel());
		Assert.assertEquals(id.getKey(), loaded.getIdentity().getKey());
		Assert.assertEquals("POST", loaded.getMethod());
		Assert.assertEquals(200, loaded.getStatus());
		Assert.assertEquals("{\"login\":\"a\"}", loaded.getRequestBody());
	}
	
	/**
	 * Persist and check all columns
	 */
	@Test
	public void createEntryAndLoad() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-entity");
		dbInstance.commitAndCloseSession();
		
		ApiAuditEntry entry = new ApiAuditEntry();
		entry.setChannel(ApiAuditChannel.rest);
		entry.setIdentity(actor);
		entry.setAuthProvider("API-Key");
		entry.setIp("203.0.113.9");
		entry.setUserAgent("Apache-HttpClient/5.3");
		entry.setMethod("PUT");
		entry.setPath("/restapi/curriculum/12/elements/345/participants/678");
		entry.setQuery("status=published");
		entry.setResourceClass("CurriculumElementsWebService");
		entry.setResourceMethod("putParticipant");
		entry.setPathParams("{\"curriculumKey\":\"12\",\"identityKey\":\"678\"}");
		entry.setStatus(200);
		entry.setDurationMs(Long.valueOf(84));
		entry.setRequestBody("{\"role\":\"participant\",\"password\":\"***\"}");
		entry.setRef("I8f3c2a-J42");
		entry.setNodeId(Integer.valueOf(1));
		
		ApiAuditLog auditLog = auditLogDao.create(entry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auditLog.getKey());
	
		ApiAuditLog reloaded = auditLogDao.loadByKey(auditLog.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(auditLog, reloaded);
		Assert.assertNotNull(reloaded.getCreationDate());
		Assert.assertEquals(ApiAuditChannel.rest, reloaded.getChannel());
		Assert.assertEquals(actor, reloaded.getIdentity());
		Assert.assertNull(reloaded.getLoginAttempt());
		Assert.assertEquals("API-Key", reloaded.getAuthProvider());
		Assert.assertEquals("203.0.113.9", reloaded.getIp());
		Assert.assertEquals("Apache-HttpClient/5.3", reloaded.getUserAgent());
		Assert.assertEquals("PUT", reloaded.getMethod());
		Assert.assertEquals("/restapi/curriculum/12/elements/345/participants/678", reloaded.getPath());
		Assert.assertEquals("status=published", reloaded.getQuery());
		Assert.assertEquals("CurriculumElementsWebService", reloaded.getResourceClass());
		Assert.assertEquals("putParticipant", reloaded.getResourceMethod());
		Assert.assertEquals("{\"curriculumKey\":\"12\",\"identityKey\":\"678\"}", reloaded.getPathParams());
		Assert.assertEquals(200, reloaded.getStatus());
		Assert.assertEquals(Long.valueOf(84), reloaded.getDurationMs());
		Assert.assertEquals("{\"role\":\"participant\",\"password\":\"***\"}", reloaded.getRequestBody());
		Assert.assertEquals("I8f3c2a-J42", reloaded.getRef());
		Assert.assertEquals(Integer.valueOf(1), reloaded.getNodeId());
	}
	
	@Test
	public void createAndPersistDeniedRequestWithoutIdentity() {
		ApiAuditLogImpl row = new ApiAuditLogImpl();
		row.setCreationDate(new Date());
		row.setChannel(ApiAuditChannel.rest);
		row.setLoginAttempt("hr-sync");
		row.setAuthProvider("none");
		row.setIp("192.0.2.77");
		row.setMethod("GET");
		row.setPath("/restapi/users");
		row.setStatus(401);
		
		ApiAuditLog auditLog = auditLogDao.create(row);
		dbInstance.commitAndCloseSession();
		
		ApiAuditLog reloaded = auditLogDao.loadByKey(auditLog.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertNull(reloaded.getIdentity());
		Assert.assertEquals("hr-sync", reloaded.getLoginAttempt());
		Assert.assertEquals(401, reloaded.getStatus());
		Assert.assertNull(reloaded.getRequestBody());
		Assert.assertNull(reloaded.getDurationMs());
	}
	
	@Test
	public void loadByUnknownKey() {
		Assert.assertNull(auditLogDao.loadByKey(Long.valueOf(-1)));
	}
	
	@Test
	public void searchByIdentityAndChannel() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-2");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-3");
		createRow(id1, ApiAuditChannel.rest, "PUT", 200, "UserWebService");
		createRow(id1, ApiAuditChannel.mcp, "POST", 200, "McpTool");
		createRow(id2, ApiAuditChannel.rest, "DELETE", 404, "UserWebService");
		dbInstance.commitAndCloseSession();
		
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(id1.getKey());
		params.setChannels(List.of(ApiAuditChannel.mcp));
		
		List<ApiAuditLog> rows = auditLogDao.search(params, 0, -1);
		Assertions.assertThat(rows).hasSize(1);
		Assertions.assertThat(rows.get(0).getResourceClass()).isEqualTo("McpTool");
		Assert.assertEquals(1, auditLogDao.count(params));
	}
	
	@Test
	public void searchByUserText() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-4");
		createRow(id, ApiAuditChannel.rest, "PUT", 200, "UserWebService");
		createRow(null, id.getUser().getNickName() + "-attacker", ApiAuditChannel.rest, "GET", 401, null);
		dbInstance.commitAndCloseSession();
		
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setUserSearch(id.getUser().getNickName());
		// the row of the identity and the row of the login attempt which contains the name
		Assertions.assertThat(auditLogDao.search(params, 0, -1))
			.hasSize(2);
		
		params.setUserSearch(id.getUser().getNickName() + "-attacker");
		List<ApiAuditLog> attempts = auditLogDao.search(params, 0, -1);
		Assertions.assertThat(attempts).hasSize(1);
		Assertions.assertThat(attempts.get(0).getIdentity()).isNull();
		Assertions.assertThat(attempts.get(0).getStatus()).isEqualTo(401);
	}
	
	@Test
	public void searchByStatusClassMethodAndResource() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-5");
		createRow(id, ApiAuditChannel.rest, "GET", 401, null);
		createRow(id, ApiAuditChannel.rest, "GET", 403, "CourseWebService");
		createRow(id, ApiAuditChannel.rest, "GET", 429, "CourseWebService");
		createRow(id, ApiAuditChannel.rest, "POST", 500, "CourseWebService");
		createRow(id, ApiAuditChannel.rest, "POST", 409, "CourseWebService");
		createRow(id, ApiAuditChannel.rest, "PUT", 201, "CourseWebService");
		dbInstance.commitAndCloseSession();
		
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(id.getKey());
		params.setStatusClasses(List.of(ApiAuditStatusClass.denied));
		Assert.assertEquals(2, auditLogDao.count(params));
		
		params.setStatusClasses(List.of(ApiAuditStatusClass.rateLimited, ApiAuditStatusClass.serverError));
		Assert.assertEquals(2, auditLogDao.count(params));
		
		// 409 only, 401, 403 and 429 have their own classes
		params.setStatusClasses(List.of(ApiAuditStatusClass.clientError));
		Assert.assertEquals(1, auditLogDao.count(params));
		
		params.setStatusClasses(List.of(ApiAuditStatusClass.success));
		Assert.assertEquals(1, auditLogDao.count(params));
		
		params.setStatusClasses(null);
		params.setMethods(List.of("POST", "PUT"));
		params.setResourceClass("course");
		Assert.assertEquals(3, auditLogDao.count(params));
	}
	
	@Test
	public void searchByDateRangeAndOrder() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-6");
		ApiAuditLog first = createRow(id, ApiAuditChannel.rest, "PUT", 200, "AaaWebService");
		ApiAuditLog second = createRow(id, ApiAuditChannel.rest, "PUT", 200, "BbbWebService");
		dbInstance.commitAndCloseSession();
		
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(id.getKey());
		params.setCreatedAfter(DateUtils.addDays(new Date(), -1));
		params.setCreatedBefore(DateUtils.addDays(new Date(), 1));
		params.setOrder(OrderBy.resourceClass);
		params.setOrderAsc(false);
		
		List<ApiAuditLog> rows = auditLogDao.search(params, 0, 10);
		Assertions.assertThat(rows).extracting(ApiAuditLog::getKey)
			.containsExactly(second.getKey(), first.getKey());
		
		params.setOrderAsc(true);
		Assertions.assertThat(auditLogDao.search(params, 0, 10)).extracting(ApiAuditLog::getKey)
			.containsExactly(first.getKey(), second.getKey());
		
		params.setCreatedAfter(DateUtils.addDays(new Date(), 1));
		Assertions.assertThat(auditLogDao.search(params, 0, 10)).isEmpty();
	}
	
	@Test
	public void searchPaging() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-8");
		for(int i=0; i<5; i++) {
			createRow(id, ApiAuditChannel.rest, "PUT", 200, "PagingWebService");
		}
		dbInstance.commitAndCloseSession();
		
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(id.getKey());
		Assert.assertEquals(5, auditLogDao.count(params));
		Assertions.assertThat(auditLogDao.search(params, 0, 2)).hasSize(2);
		Assertions.assertThat(auditLogDao.search(params, 4, 10)).hasSize(1);
	}
	
	@Test
	public void deleteOlderThan() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-7");
		ApiAuditLog old = createRow(id, ApiAuditChannel.rest, "PUT", 200, "OldWebService");
		ApiAuditLog fresh = createRow(id, ApiAuditChannel.rest, "PUT", 200, "FreshWebService");
		dbInstance.commitAndCloseSession();
		
		// creationdate is not updatable through JPA, back-date the row with a native query
		dbInstance.getCurrentEntityManager()
			.createQuery("update apiauditlog set creationDate=:creationDate where key=:key")
			.setParameter("creationDate", DateUtils.addDays(new Date(), -400))
			.setParameter("key", old.getKey())
			.executeUpdate();
		dbInstance.commitAndCloseSession();
		
		int deleted = auditLogDao.deleteOlderThan(DateUtils.addDays(new Date(), -365));
		dbInstance.commitAndCloseSession();
		
		Assertions.assertThat(deleted).isGreaterThanOrEqualTo(1);
		Assert.assertNull(auditLogDao.loadByKey(old.getKey()));
		Assert.assertNotNull(auditLogDao.loadByKey(fresh.getKey()));
	}
	
	private ApiAuditLog createRow(Identity identity, ApiAuditChannel channel, String method, int status, String resourceClass) {
		return createRow(identity, null, channel, method, status, resourceClass);
	}
	
	private ApiAuditLog createRow(Identity identity, String loginAttempt, ApiAuditChannel channel, String method, int status, String resourceClass) {
		ApiAuditLogImpl row = new ApiAuditLogImpl();
		row.setChannel(channel);
		row.setIdentity(identity);
		row.setLoginAttempt(loginAttempt);
		row.setAuthProvider(ApiAuditLogService.AUTH_API_KEY);
		row.setIp("127.0.0.1");
		row.setUserAgent("junit");
		row.setMethod(method);
		row.setPath("/restapi/users/123");
		row.setResourceClass(resourceClass);
		row.setResourceMethod("update");
		row.setPathParams("{\"identityKey\":\"123\"}");
		row.setStatus(status);
		row.setDurationMs(Long.valueOf(12));
		row.setRequestBody("{\"login\":\"a\"}");
		row.setRef("42");
		row.setNodeId(Integer.valueOf(1));
		return auditLogDao.create(row);
	}
}
