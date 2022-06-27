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
package org.olat.modules.dcompensation.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DisadvantageCompensationAuditLogDAOTest extends OlatTestCase {
	
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private DisadvantageCompensationDAO disadvantageCompensationDao;
	@Autowired
	private DisadvantageCompensationAuditLogDAO disadvantageCompensationAuditLogDao;
	
	
	@Test
	public void createAuditLog() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcomp-audit--1");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcomp-audit-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -5);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "By me", approval, creator, entry, subIdent, "Test");
		dbInstance.commitAndCloseSession();
		
		DisadvantageCompensationAuditLog auditLog = disadvantageCompensationAuditLogDao.create("update", "Before", "After", compensation, creator);
		dbInstance.commit();
		
		Assert.assertNotNull(auditLog);
		Assert.assertNotNull(auditLog.getKey());
		Assert.assertNotNull(auditLog.getCreationDate());
		Assert.assertEquals("update", auditLog.getAction());
		Assert.assertEquals("Before", auditLog.getBefore());
		Assert.assertEquals("After", auditLog.getAfter());
		Assert.assertEquals(compensation.getKey(), auditLog.getCompensationKey());
		Assert.assertEquals(entry.getKey(), auditLog.getEntryKey());
		Assert.assertEquals(subIdent, auditLog.getSubIdent());
		Assert.assertEquals(creator.getKey(), auditLog.getAuthorKey());	
	}
	
	@Test
	public void getAuditLogs() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcomp-audit-3");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcomp-audit-4");
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("dcomp-audit-5");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -5);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "By me", approval, creator, entry, subIdent, "Test");
		dbInstance.commitAndCloseSession();
		
		DisadvantageCompensationAuditLog auditLog = disadvantageCompensationAuditLogDao.create("update", "Before changes", "After changes", compensation, doer);
		dbInstance.commit();
		
		List<DisadvantageCompensationAuditLog> auditLogs = disadvantageCompensationAuditLogDao.getAuditLogs(identity, entry, subIdent);
		Assert.assertNotNull(auditLogs);
		Assert.assertEquals(1, auditLogs.size());
		
		DisadvantageCompensationAuditLog reloadedAuditLog = auditLogs.get(0);
		Assert.assertNotNull(reloadedAuditLog);
		Assert.assertEquals(auditLog.getKey(), reloadedAuditLog.getKey());
		Assert.assertEquals("update", auditLog.getAction());
		Assert.assertEquals("Before changes", auditLog.getBefore());
		Assert.assertEquals("After changes", auditLog.getAfter());
		Assert.assertEquals(compensation.getKey(), auditLog.getCompensationKey());
		Assert.assertEquals(entry.getKey(), auditLog.getEntryKey());
		Assert.assertEquals(subIdent, auditLog.getSubIdent());
		Assert.assertEquals(doer.getKey(), auditLog.getAuthorKey());	
	}
	
	@Test
	public void deleteAuditLogs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("dcomp-audit-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -5);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(id, 15, "By me", approval, id, entry, subIdent, "Test");
		dbInstance.commitAndCloseSession();
		
		disadvantageCompensationAuditLogDao.create("update", "Before changes", "After changes", compensation, id);
		disadvantageCompensationAuditLogDao.create("update", "Before changes", "After changes", compensation, id);
		dbInstance.commit();
		
		List<DisadvantageCompensationAuditLog> auditLogs = disadvantageCompensationAuditLogDao.getAuditLogs(id, entry, subIdent);
		Assert.assertNotNull(auditLogs);
		Assert.assertEquals(2, auditLogs.size());
		
		disadvantageCompensationAuditLogDao.deleteDisadvantageCompensationsAuditLogsByEntry(entry);
		dbInstance.commit();
		
		List<DisadvantageCompensationAuditLog> deletedAuditLogs = disadvantageCompensationAuditLogDao.getAuditLogs(id, entry, subIdent);
		Assert.assertNotNull(deletedAuditLogs);
		Assert.assertTrue(deletedAuditLogs.isEmpty());
	}
}
