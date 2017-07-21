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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	
	@Test
	public void createAndPersistRollCall() {
		LectureBlock lectureBlock = createMinimalLectureBlock(2);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(rollCall);
		Assert.assertNotNull(rollCall.getKey());
		Assert.assertNotNull(rollCall.getCreationDate());
		Assert.assertNotNull(rollCall.getLastModified());
		Assert.assertEquals(lectureBlock, rollCall.getLectureBlock());
		Assert.assertEquals(id, rollCall.getIdentity());	
	}
	
	@Test
	public void createAndLoadRollCall() {
		LectureBlock lectureBlock = createMinimalLectureBlock(2);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		
		Assert.assertNotNull(reloadRollCall);
		Assert.assertNotNull(reloadRollCall.getKey());
		Assert.assertNotNull(reloadRollCall.getCreationDate());
		Assert.assertNotNull(reloadRollCall.getLastModified());
		Assert.assertEquals(rollCall, reloadRollCall);
		Assert.assertEquals(lectureBlock, reloadRollCall.getLectureBlock());
		Assert.assertEquals(id, reloadRollCall.getIdentity());	
	}
	
	@Test
	public void createRollCall_absences() {
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Collections.singletonList(2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(rollCall, reloadRollCall);
		
		//check absence
		Assert.assertEquals(1, reloadRollCall.getLecturesAbsentNumber());
		List<Integer> absenceList = reloadRollCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(1, absenceList.size());
		Assert.assertEquals(2, absenceList.get(0).intValue());
		
		//check attendee
		Assert.assertEquals(2, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(2, attendeeList.size());
		Assert.assertEquals(0, attendeeList.get(0).intValue());
		Assert.assertEquals(1, attendeeList.get(1).intValue());
	}
	
	@Test
	public void addLectures() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(0);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(1, reloadRollCall.getLecturesAbsentNumber());
		
		List<Integer> additionalAbsences = Arrays.asList(1, 2);
		lectureBlockRollCallDao.addLecture(lectureBlock, reloadRollCall, additionalAbsences);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(3, reloadRollCall.getLecturesAbsentNumber());
		List<Integer> absenceList = reloadRollCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(3, absenceList.size());
		Assert.assertEquals(0, absenceList.get(0).intValue());
		Assert.assertEquals(1, absenceList.get(1).intValue());
		Assert.assertEquals(2, absenceList.get(2).intValue());
		
		//check attendee
		Assert.assertEquals(1, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(1, attendeeList.size());
		Assert.assertEquals(3, attendeeList.get(0).intValue());
	}
	
	@Test
	public void removeLectures() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(0, 1, 2, 3);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(4, reloadRollCall.getLecturesAbsentNumber());
		
		List<Integer> removedAbsences = Arrays.asList(1, 2);
		lectureBlockRollCallDao.removeLecture(lectureBlock, reloadRollCall, removedAbsences);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(2, reloadRollCall.getLecturesAbsentNumber());
		List<Integer> absenceList = reloadRollCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(2, absenceList.size());
		Assert.assertEquals(0, absenceList.get(0).intValue());
		Assert.assertEquals(3, absenceList.get(1).intValue());
		
		//check attendee
		Assert.assertEquals(2, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(2, attendeeList.size());
		Assert.assertEquals(1, attendeeList.get(0).intValue());
		Assert.assertEquals(2, attendeeList.get(1).intValue());
	}
	
	@Test
	public void adaptLectures_removeAbsences() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(0, 1, 2, 3);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(4, reloadRollCall.getLecturesAbsentNumber());
		
		//adapt the number of lectures
		LectureBlockRollCall adaptedCall = lectureBlockRollCallDao.adaptLecture(lectureBlock, reloadRollCall, 2, id);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(2, adaptedCall.getLecturesAbsentNumber());
		List<Integer> absenceList = adaptedCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(2, absenceList.size());
		Assert.assertEquals(0, absenceList.get(0).intValue());
		Assert.assertEquals(1, absenceList.get(1).intValue());
		
		//check attendee
		Assert.assertEquals(0, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(0, attendeeList.size());
	}
	
	@Test
	public void adaptLectures_removeMixed() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(2, reloadRollCall.getLecturesAbsentNumber());
		
		//adapt the number of lectures
		LectureBlockRollCall adaptedCall = lectureBlockRollCallDao.adaptLecture(lectureBlock, reloadRollCall, 2, id);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(1, adaptedCall.getLecturesAbsentNumber());
		List<Integer> absenceList = adaptedCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(1, absenceList.size());
		Assert.assertEquals(1, absenceList.get(0).intValue());
		
		//check attendee
		Assert.assertEquals(1, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(1, attendeeList.size());
		Assert.assertEquals(0, attendeeList.get(0).intValue());
	}
	
	@Test
	public void adaptLectures_addMixed() {
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(2, reloadRollCall.getLecturesAbsentNumber());
		
		//adapt the number of lectures
		LectureBlockRollCall adaptedCall = lectureBlockRollCallDao.adaptLecture(lectureBlock, reloadRollCall, 4, id);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(2, adaptedCall.getLecturesAbsentNumber());
		List<Integer> absenceList = adaptedCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(2, absenceList.size());
		Assert.assertEquals(1, absenceList.get(0).intValue());
		Assert.assertEquals(2, absenceList.get(1).intValue());
		
		//check attendee
		Assert.assertEquals(2, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(2, attendeeList.size());
		Assert.assertEquals(0, attendeeList.get(0).intValue());
		Assert.assertEquals(3, attendeeList.get(1).intValue());
	}

	private LectureBlock createMinimalLectureBlock(int numOfLectures) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(numOfLectures);
		lectureBlock.setEffectiveLecturesNumber(numOfLectures);
		return lectureBlockDao.update(lectureBlock);
	}
}
