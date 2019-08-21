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

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeToLectureBlockDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private AbsenceNoticeToLectureBlockDAO absenceNoticeToLectureBlockDao;
	
	@Test
	public void createAbsenceNoticeToLectureBlock() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		
		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				start, end, null, null, null, null, null);
		dbInstance.commitAndCloseSession();

		LectureBlock lectureBlock = createLectureBlock();
		
		AbsenceNoticeToLectureBlock relation = absenceNoticeToLectureBlockDao.createRelation(notice, lectureBlock);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(lectureBlock, relation.getLectureBlock());
		Assert.assertEquals(notice, relation.getAbsenceNotice());
	}
	

	public LectureBlock createLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello absences");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		return lectureBlock;
	}
	

}
