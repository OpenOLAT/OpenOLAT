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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockReminderImpl;
import org.olat.modules.lecture.model.LectureBlockToTeacher;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockReminderDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private LectureBlockReminderDAO lectureBlockReminderDao;

	@Test
	public void createReminder() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-1");
		LectureBlock lectureBlock = createMinimalLectureBlock(2);
		LectureBlockReminderImpl reminder = lectureBlockReminderDao.createReminder(lectureBlock, id, "ok");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reminder);
		Assert.assertNotNull(reminder.getKey());
		Assert.assertNotNull(reminder.getCreationDate());
		Assert.assertEquals(id, reminder.getIdentity());
		Assert.assertEquals(lectureBlock, reminder.getLectureBlock());
	}
	
	@Test
	public void loadLectureBlockToRemind() {
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-2");
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-3");
		LectureBlock lectureBlock = createMinimalLectureBlock(5);
		dbInstance.commit();
		//add the teachers
		lectureService.addTeacher(lectureBlock, teacher1);
		lectureService.addTeacher(lectureBlock, teacher2);
		LectureBlockReminderImpl reminder = lectureBlockReminderDao.createReminder(lectureBlock, teacher1, "ok");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reminder);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		
		List<LectureBlockToTeacher> toRemind = lectureBlockReminderDao.getLectureBlockTeachersToReminder(cal.getTime());
		
		boolean hasTeacher1 = false;
		boolean hasTeacher2 = false;
		for(LectureBlockToTeacher remind:toRemind) {
			if(remind.getLectureBlock().equals(lectureBlock)) {
				if(remind.getTeacher().equals(teacher1)) {
					hasTeacher1 = true;
				} else if(remind.getTeacher().equals(teacher2)) {
					hasTeacher2 = true;
				}
			}
		}
		
		Assert.assertTrue(hasTeacher2);
		Assert.assertFalse(hasTeacher1);
	}
	
	@Test
	public void loadLectureBlockToRemind_status() {
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-8");
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-9");
		LectureBlock lectureBlockAutoClosed = createMinimalLectureBlock(5);
		LectureBlock lectureBlockClosed = createMinimalLectureBlock(5);
		LectureBlock lectureBlockCancelled = createMinimalLectureBlock(5);
		LectureBlock lectureBlockReopen = createMinimalLectureBlock(5);
		LectureBlock lectureBlockOpen = createMinimalLectureBlock(5);
		LectureBlock lectureBlock = createMinimalLectureBlock(5);
		dbInstance.commit();
		//add the teachers
		lectureService.addTeacher(lectureBlockAutoClosed, teacher1);
		lectureService.addTeacher(lectureBlockClosed, teacher1);
		lectureService.addTeacher(lectureBlockCancelled, teacher2);
		lectureService.addTeacher(lectureBlockReopen, teacher1);
		lectureService.addTeacher(lectureBlockReopen, teacher2);
		lectureService.addTeacher(lectureBlockOpen, teacher1);
		lectureService.addTeacher(lectureBlockOpen, teacher2);
		lectureService.addTeacher(lectureBlock, teacher2);
		dbInstance.commit();
		lectureBlockAutoClosed.setRollCallStatus(LectureRollCallStatus.autoclosed);
		lectureBlockAutoClosed = lectureService.save(lectureBlockAutoClosed, null);
		lectureBlockClosed.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlockClosed = lectureService.save(lectureBlockClosed, null);
		lectureBlockCancelled.setStatus(LectureBlockStatus.cancelled);
		lectureBlockCancelled = lectureService.save(lectureBlockCancelled, null);
		lectureBlockReopen.setRollCallStatus(LectureRollCallStatus.reopen);
		lectureBlockReopen = lectureService.save(lectureBlockReopen, null);
		lectureBlockOpen.setRollCallStatus(LectureRollCallStatus.open);
		lectureBlockOpen = lectureService.save(lectureBlockOpen, null);
		dbInstance.commitAndCloseSession();;

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		
		List<LectureBlockToTeacher> toRemind = lectureBlockReminderDao.getLectureBlockTeachersToReminder(cal.getTime());
		
		boolean hasBlock = false;
		boolean hasBlockOpen = false;
		boolean hasOtherBlock = false;
		for(LectureBlockToTeacher remind:toRemind) {
			if(remind.getLectureBlock().equals(lectureBlock)) {
				hasBlock = true;
			} else if(remind.getLectureBlock().equals(lectureBlockOpen)) {
				hasBlockOpen = true;
			} else if(remind.getLectureBlock().equals(lectureBlockAutoClosed)
					|| remind.getLectureBlock().equals(lectureBlockClosed)
					|| remind.getLectureBlock().equals(lectureBlockCancelled)) {
				hasOtherBlock = true;
			}
		}
		
		Assert.assertTrue(hasBlock);
		Assert.assertTrue(hasBlockOpen);
		Assert.assertFalse(hasOtherBlock);
	}
	
	@Test
	public void loadLectureBlockToRemind_entryStatus() {
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-10");
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-11");
		LectureBlock lectureBlock1 = createMinimalLectureBlock(5);
		LectureBlock lectureBlock2 = createMinimalLectureBlock(5);
		
		RepositoryEntry entryBlock1 = lectureBlock1.getEntry();
		RepositoryEntry entryBlock2 = lectureBlock2.getEntry();
		repositoryManager.setStatus(entryBlock1, RepositoryEntryStatusEnum.trash);
		repositoryManager.setStatus(entryBlock2, RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		
		lectureService.addTeacher(lectureBlock1, teacher1);
		lectureService.addTeacher(lectureBlock2, teacher2);
		dbInstance.commitAndCloseSession();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		List<LectureBlockToTeacher> toRemind = lectureBlockReminderDao.getLectureBlockTeachersToReminder(cal.getTime());
		
		boolean hasBlock1 = false;
		boolean hasBlock2 = false;
		for(LectureBlockToTeacher remind:toRemind) {
			if(remind.getLectureBlock().equals(lectureBlock1)) {
				hasBlock1 = true;
			} else if(remind.getLectureBlock().equals(lectureBlock2)) {
				hasBlock2 = true;
			}
		}
		
		Assert.assertFalse(hasBlock1);
		Assert.assertTrue(hasBlock2);
	}
	
	@Test
	public void deleteReminder() {
		//create a reminder
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("reminder-to-delete-1");
		LectureBlock lectureBlock = createMinimalLectureBlock(2);
		LectureBlockReminderImpl reminder = lectureBlockReminderDao.createReminder(lectureBlock, id, "Delete it");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reminder);
		
		//delete the reminders
		int deletedRows = lectureBlockReminderDao.deleteReminders(id);
		Assert.assertEquals(1, deletedRows);
		dbInstance.commitAndCloseSession();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		List<LectureBlockToTeacher> toRemind = lectureBlockReminderDao.getLectureBlockTeachersToReminder(cal.getTime());
		
		boolean hasId = false;
		for(LectureBlockToTeacher remind:toRemind) {
			if(remind.getLectureBlock().equals(lectureBlock)) {
				if(remind.getTeacher().equals(id)) {
					hasId = true;
				}
			}
		}
		
		Assert.assertFalse(hasId);
	}
	
	private LectureBlock createMinimalLectureBlock(int dayInThePast) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);
		dbInstance.commit();
		
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -dayInThePast);
		lectureBlock.setStartDate(cal.getTime());
		lectureBlock.setEndDate(cal.getTime());
		lectureBlock.setTitle("Hello lecturers");
		return lectureBlockDao.update(lectureBlock);
	}
}