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
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockReminderImpl;
import org.olat.modules.lecture.model.LectureBlockToTeacher;
import org.olat.repository.RepositoryEntry;
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
	
	
	private LectureBlock createMinimalLectureBlock(int dayInThePast) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
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
