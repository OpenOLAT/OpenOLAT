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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.model.ApiAuditLogImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogRetentionJobTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	@Autowired
	private ApiAuditLogService auditLogService;
	
	@Test
	public void jobDeletesRowsBeyondRetention() {
		int currentRetetion = restModule.getAuditLogRetentionDays();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-job-1");
		ApiAuditLogImpl old = createRow(id, 400);
		ApiAuditLogImpl fresh = createRow(id, 0);
		
		restModule.setAuditLogRetentionDays(365);
		new ApiAuditLogRetentionJob().executeWithDB(null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(auditLogDao.loadByKey(old.getKey()));
		Assert.assertNotNull(auditLogDao.loadByKey(fresh.getKey()));
		
		restModule.setAuditLogRetentionDays(currentRetetion);
		waitMessageAreConsumed();
	}
	
	@Test
	public void jobKeepsEverythingWhenRetentionDisabled() {
		int currentRetetion = restModule.getAuditLogRetentionDays();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-job-2");
		ApiAuditLogImpl old = createRow(id, 800);
		
		restModule.setAuditLogRetentionDays(0);
		new ApiAuditLogRetentionJob().executeWithDB(null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(auditLogDao.loadByKey(old.getKey()));
		
		restModule.setAuditLogRetentionDays(currentRetetion);
		waitMessageAreConsumed();
	}
	
	@Test
	public void serviceRetentionIsRelativeToToday() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-job-3");
		ApiAuditLogImpl old = createRow(id, 10);
		ApiAuditLogImpl fresh = createRow(id, 2);
		
		int deleted = auditLogService.deleteOlderThan(5);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(deleted >= 1);
		Assert.assertNull(auditLogDao.loadByKey(old.getKey()));
		Assert.assertNotNull(auditLogDao.loadByKey(fresh.getKey()));
	}
	
	private ApiAuditLogImpl createRow(Identity identity, int ageInDays) {
		ApiAuditLogImpl row = new ApiAuditLogImpl();
		row.setChannel(ApiAuditChannel.rest);
		row.setIdentity(identity);
		row.setMethod("PUT");
		row.setPath("/restapi/users/1");
		row.setStatus(200);
		auditLogDao.create(row);
		dbInstance.commitAndCloseSession();
		
		if(ageInDays > 0) {
			// Update statement for creationDate
			dbInstance.getCurrentEntityManager()
				.createQuery("update apiauditlog set creationDate=:creationDate where key=:key")
				.setParameter("creationDate", DateUtils.addDays(new Date(), -ageInDays))
				.setParameter("key", row.getKey())
				.executeUpdate();
			dbInstance.commitAndCloseSession();
		}
		return row;
	}
}
