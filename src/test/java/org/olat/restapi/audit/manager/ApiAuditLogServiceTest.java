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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	@Autowired
	private ApiAuditLogService auditLogService;
	
	@Test
	public void isAuditableMatrix() {
		boolean currentReadsSetting = restModule.isAuditLogReads();
		restModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		
		Assert.assertTrue(auditLogService.isAuditable("POST", 200));
		Assert.assertTrue(auditLogService.isAuditable("PUT", 400));
		Assert.assertTrue(auditLogService.isAuditable("DELETE", 404));
		Assert.assertTrue(auditLogService.isAuditable("PATCH", 200));
		Assert.assertTrue(auditLogService.isAuditable("GET", 401));
		Assert.assertTrue(auditLogService.isAuditable("GET", 403));
		Assert.assertTrue(auditLogService.isAuditable("GET", 429));
		Assert.assertTrue(auditLogService.isAuditable("GET", 500));
		Assert.assertFalse(auditLogService.isAuditable("GET", 200));
		Assert.assertFalse(auditLogService.isAuditable("HEAD", 304));
		Assert.assertFalse(auditLogService.isAuditable("GET", 404));
		Assert.assertFalse(auditLogService.isAuditable("OPTIONS", 200));
		
		restModule.setAuditLogReads(true);
		waitMessageAreConsumed();
		
		Assert.assertTrue(auditLogService.isAuditable("GET", 200));
		Assert.assertTrue(auditLogService.isAuditable("GET", 404));

		restModule.setAuditLogReads(currentReadsSetting);
	}
	
	@Test
	public void logWritesRowInCurrentTransaction() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-1");
		auditLogService.log(entry(id, "POST", 200));
		dbInstance.commitAndCloseSession();
		
		List<ApiAuditLog> rows = rowsOf(id);
		Assertions.assertThat(rows).hasSize(1);
		ApiAuditLog row = rows.get(0);
		Assertions.assertThat(row.getRequestBody()).contains("\"password\":\"***\"").doesNotContain("\"p\"");
		Assertions.assertThat(row.getQuery()).isEqualTo("x=1&secret=***");
		Assertions.assertThat(row.getChannel()).isEqualTo(ApiAuditChannel.rest);
	}
	
	@Test
	public void logGetWithoutReadsWritesNoRow() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-2");
		auditLogService.log(entry(id, "GET", 200));
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(0, countOf(id));
	}
	
	@Test
	public void logInNewTransactionSurvivesWithoutOuterCommit() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-3");
		dbInstance.commitAndCloseSession();
		
		auditLogService.logInNewTransaction(entry(id, "DELETE", 500));
		// no commit here on purpose, the service commits its own transaction
		Assert.assertEquals(1, countOf(id));
	}
	
	@Test
	public void logBodyDisabledStoresNoBody() {
		boolean currentBodySetting = restModule.isAuditLogBody();
		restModule.setAuditLogBody(false);
		waitMessageAreConsumed();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-4");
		auditLogService.log(entry(id, "PUT", 200));
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(rowsOf(id).get(0).getRequestBody());
		
		restModule.setAuditLogBody(currentBodySetting);
		waitMessageAreConsumed();
	}
	
	@Test
	public void logDisabledWritesNoRow() {
		boolean currentEnabled = restModule.isAuditLogEnabled();
		restModule.setAuditLogEnabled(false);
		waitMessageAreConsumed();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-5");
		auditLogService.log(entry(id, "POST", 200));
		auditLogService.logInNewTransaction(entry(id, "DELETE", 500));
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(0, countOf(id));
		
		restModule.setAuditLogEnabled(currentEnabled);
		waitMessageAreConsumed();
	}
	
	@Test
	public void logTruncatesOversizedValues() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-6");
		ApiAuditEntry entry = entry(id, "POST", 200);
		entry.setPath("/restapi/" + "a".repeat(2000));
		entry.setUserAgent("u".repeat(500));
		auditLogService.log(entry);
		dbInstance.commitAndCloseSession();
		
		ApiAuditLog row = rowsOf(id).get(0);
		Assert.assertEquals(1024, row.getPath().length());
		Assert.assertEquals(255, row.getUserAgent().length());
	}
	
	@Test
	public void deleteOlderThanKeepsAllWithoutRetention() {
		Assert.assertEquals(0, auditLogService.deleteOlderThan(0));
		Assert.assertEquals(0, auditLogService.deleteOlderThan(-1));
	}
	
	@Test
	public void accessLineLevelsAndMarker() {
		boolean currentReadSetting = restModule.isAuditLogReads();
		restModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		
		CollectingAppender appender = CollectingAppender.attach("org.olat.restapi.access");
		try {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-7");
			auditLogService.log(entry(id, "GET", 200));
			auditLogService.log(entry(id, "POST", 200));
			auditLogService.log(entry(id, "GET", 401));
			auditLogService.log(entry(id, "GET", 429));
			auditLogService.log(entry(id, "POST", 500));
			dbInstance.commitAndCloseSession();
			
			// a read is debug, a write is info, a denied or failed call is warn
			Assertions.assertThat(appender.levels())
				.containsExactly(Level.DEBUG, Level.INFO, Level.WARN, Level.WARN, Level.WARN);
			Assertions.assertThat(appender.messages().get(1))
				.contains("POST").contains("/restapi/users").contains("200")
				.contains("UserWebService.create").contains("secret=***");
			// the body never reaches the access log
			Assertions.assertThat(appender.messages())
				.allSatisfy(message -> Assertions.assertThat(message).doesNotContain("password"));
			Assertions.assertThat(appender.markers())
				.isNotEmpty().allMatch(marker -> marker != null && "REST".equals(marker.getName()));
		} finally {
			appender.detach();
		}
		
		restModule.setAuditLogReads(currentReadSetting);
		waitMessageAreConsumed();
	}
	
	@Test
	public void accessLineWrittenForNonAuditableRead() {
		boolean currentReadSetting = restModule.isAuditLogReads();
		restModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		
		CollectingAppender appender = CollectingAppender.attach("org.olat.restapi.access");
		try {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-svc-8");
			auditLogService.log(entry(id, "GET", 200));
			dbInstance.commitAndCloseSession();
			
			// no row in the table, but one line in the access log
			Assert.assertEquals(0, countOf(id));
			Assertions.assertThat(appender.levels()).containsExactly(Level.DEBUG);
		} finally {
			appender.detach();
		}
		
		restModule.setAuditLogReads(currentReadSetting);
		waitMessageAreConsumed();
	}
	
	private List<ApiAuditLog> rowsOf(Identity identity) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		return auditLogDao.search(params, 0, -1);
	}
	
	private int countOf(Identity identity) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		return auditLogDao.count(params);
	}
	
	private ApiAuditEntry entry(Identity identity, String method, int status) {
		ApiAuditEntry entry = new ApiAuditEntry();
		entry.setChannel(ApiAuditChannel.rest);
		entry.setIdentity(identity);
		entry.setAuthProvider(ApiAuditLogService.AUTH_API_KEY);
		entry.setIp("127.0.0.1");
		entry.setUserAgent("junit");
		entry.setMethod(method);
		entry.setPath("/restapi/users");
		entry.setQuery("x=1&secret=abc");
		entry.setResourceClass("UserWebService");
		entry.setResourceMethod("create");
		entry.setStatus(status);
		entry.setDurationMs(Long.valueOf(5));
		entry.setRequestBody("{\"login\":\"u\",\"password\":\"p\"}");
		entry.setRef("7");
		entry.setNodeId(Integer.valueOf(1));
		return entry;
	}
	
	/**
	 * Collects the events of a logger, log4j2 has no ListAppender on the test classpath.
	 */
	private static final class CollectingAppender extends AbstractAppender {
		
		private final List<LogEvent> events = new ArrayList<>();
		private final org.apache.logging.log4j.core.Logger logger;
		private final Level previousLevel;
		
		private CollectingAppender(String name, org.apache.logging.log4j.core.Logger logger) {
			super(name, null, null, true, null);
			this.logger = logger;
			this.previousLevel = logger.getLevel();
		}
		
		public static CollectingAppender attach(String loggerName) {
			org.apache.logging.log4j.core.Logger logger =
					(org.apache.logging.log4j.core.Logger)LogManager.getLogger(loggerName);
			CollectingAppender appender = new CollectingAppender("collecting-" + loggerName, logger);
			appender.start();
			logger.addAppender(appender);
			Configurator.setLevel(loggerName, Level.DEBUG);
			return appender;
		}
		
		public void detach() {
			logger.removeAppender(this);
			stop();
			Configurator.setLevel(logger.getName(), previousLevel);
		}
		
		@Override
		public void append(LogEvent event) {
			events.add(event.toImmutable());
		}
		
		public List<Level> levels() {
			return events.stream().map(LogEvent::getLevel).toList();
		}
		
		public List<String> messages() {
			return events.stream().map(event -> event.getMessage().getFormattedMessage()).toList();
		}
		
		public List<Marker> markers() {
			return events.stream().map(LogEvent::getMarker).toList();
		}
	}
}
