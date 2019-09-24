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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private AbsenceCategoryDAO absenceCategoryDao;
	@Autowired
	private LectureBlockAuditLogDAO lectureBlockAuditLogDao;
	
	@Test
	public void createAuditLog() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-1-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-1-");
		lectureBlockAuditLogDao.auditLog(LectureBlockAuditLog.Action.createLectureBlock, "3", "4", "Update absence", null, null,entry, identity, author);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getAuditLog_byIdentity() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-3-");
		lectureBlockAuditLogDao.auditLog(LectureBlockAuditLog.Action.createLectureBlock, "3", "4", "Update absence", null, null,entry, identity, author);
		dbInstance.commitAndCloseSession();
		
		//load the audit log
		List<LectureBlockAuditLog> auditLog = lectureBlockAuditLogDao.getAuditLog(identity);
		Assert.assertNotNull(auditLog);
		Assert.assertEquals(1, auditLog.size());
		//check the entry
		LectureBlockAuditLog logEntry = auditLog.get(0);
		Assert.assertEquals(identity.getKey(), logEntry.getIdentityKey());
		Assert.assertEquals(author.getKey(), logEntry.getAuthorKey());
		Assert.assertEquals(entry.getKey(), logEntry.getEntryKey());
		Assert.assertEquals("3", logEntry.getBefore());
		Assert.assertEquals("4", logEntry.getAfter());
		Assert.assertEquals(LectureBlockAuditLog.Action.createLectureBlock.name(), logEntry.getAction());
	}
	
	@Test
	public void getAuditLog_byRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-4-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-5-");
		lectureBlockAuditLogDao.auditLog(LectureBlockAuditLog.Action.updateAuthorizedAbsence, "Before", "After", "Update absence of course", null, null,entry, identity, author);
		dbInstance.commitAndCloseSession();
		
		//load the audit log
		List<LectureBlockAuditLog> auditLog = lectureBlockAuditLogDao.getAuditLog(entry);
		Assert.assertNotNull(auditLog);
		Assert.assertEquals(1, auditLog.size());
		//check the entry
		LectureBlockAuditLog logEntry = auditLog.get(0);
		Assert.assertEquals(identity.getKey(), logEntry.getIdentityKey());
		Assert.assertEquals(author.getKey(), logEntry.getAuthorKey());
		Assert.assertEquals(entry.getKey(), logEntry.getEntryKey());
		Assert.assertEquals("Before", logEntry.getBefore());
		Assert.assertEquals("After", logEntry.getAfter());
		Assert.assertEquals("Update absence of course", logEntry.getMessage());
		Assert.assertEquals(LectureBlockAuditLog.Action.updateAuthorizedAbsence.name(), logEntry.getAction());
	}
	
	@Test
	public void getAuditLog_byLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("I will be loged");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		lectureBlockAuditLogDao.auditLog(LectureBlockAuditLog.Action.autoclose, "Before", "After", "Close the absence of course", lectureBlock, null,entry, null, null);
		dbInstance.commitAndCloseSession();
		
		//load the audit log
		List<LectureBlockAuditLog> auditLog = lectureBlockAuditLogDao.getAuditLog(lectureBlock);
		Assert.assertNotNull(auditLog);
		Assert.assertEquals(1, auditLog.size());
		//check the entry
		LectureBlockAuditLog logEntry = auditLog.get(0);
		Assert.assertEquals(entry.getKey(), logEntry.getEntryKey());
		Assert.assertEquals("Before", logEntry.getBefore());
		Assert.assertEquals("After", logEntry.getAfter());
		Assert.assertEquals("Close the absence of course", logEntry.getMessage());
		Assert.assertEquals(LectureBlockAuditLog.Action.autoclose.name(), logEntry.getAction());
	}
	
	@Test
	public void xmlAuditLog() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		String xml = lectureBlockAuditLogDao.toXml(lectureBlock);
		Assert.assertNotNull(xml);
	}
	
	@Test
	public void xmlAuditLog_absenceNotice() {
		String title = UUID.randomUUID().toString();
		String description = "Long absence";
		AbsenceCategory absenceCategory = absenceCategoryDao.createAbsenceCategory(title, description);
		dbInstance.commitAndCloseSession();
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity notifier = JunitTestHelper.createAndPersistIdentityAsRndUser("notifier-1");
		Identity authorizer = JunitTestHelper.createAndPersistIdentityAsRndUser("authorizer-1");
		
		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());

		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				start, end, absenceCategory, "A very good reason", Boolean.TRUE, authorizer, notifier);
		dbInstance.commitAndCloseSession();

		String xml = lectureBlockAuditLogDao.toXml(notice);
		System.out.println(xml);
		Assert.assertNotNull(xml);
	}
}
