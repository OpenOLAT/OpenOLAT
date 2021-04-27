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
package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EnrollmentManagerSerialTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private EnrollmentManager enrollmentManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void getEnrollmentRows_withWaitingList() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("en-coach-1");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-2");
		Identity waiter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-wait-3");
		Identity out = JunitTestHelper.createAndPersistIdentityAsRndUser("en-out-4");
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(coach, "en-1", "en-1", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, resource);
		businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(waiter1, group, GroupRoles.waiting.name());
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group.getKey());
		
		//check participant 1
		List<EnrollmentRow> enrollments = enrollmentManager.getEnrollments(participant1, groupKeys, null, 128);
		Assert.assertNotNull(enrollments);
		Assert.assertEquals(1, enrollments.size());
		EnrollmentRow enrollment = enrollments.get(0);
		Assert.assertEquals(group.getKey(), enrollment.getKey());
		Assert.assertEquals(group.getName(), enrollment.getName());
		Assert.assertEquals(2, enrollment.getNumOfParticipants());
		Assert.assertEquals(1, enrollment.getNumInWaitingList());
		Assert.assertTrue(enrollment.isParticipant());
		Assert.assertFalse(enrollment.isWaiting());
		
		//check waiter
		List<EnrollmentRow> waitingEnrollments = enrollmentManager.getEnrollments(waiter1, groupKeys, null, 128);
		Assert.assertNotNull(waitingEnrollments);
		Assert.assertEquals(1, waitingEnrollments.size());
		EnrollmentRow waitingEnrollment = waitingEnrollments.get(0);
		Assert.assertEquals(group.getKey(), waitingEnrollment.getKey());
		Assert.assertEquals(group.getName(), waitingEnrollment.getName());
		Assert.assertEquals(2, waitingEnrollment.getNumOfParticipants());
		Assert.assertEquals(1, waitingEnrollment.getNumInWaitingList());
		Assert.assertEquals(1, waitingEnrollment.getPositionInWaitingList());
		Assert.assertFalse(waitingEnrollment.isParticipant());
		Assert.assertTrue(waitingEnrollment.isWaiting());
		
		//check out
		List<EnrollmentRow> outEnrollments = enrollmentManager.getEnrollments(out, groupKeys, null, 128);
		Assert.assertNotNull(outEnrollments);
		Assert.assertEquals(1, outEnrollments.size());
		EnrollmentRow outEnrollment = outEnrollments.get(0);
		Assert.assertEquals(group.getKey(), outEnrollment.getKey());
		Assert.assertEquals(group.getName(), outEnrollment.getName());
		Assert.assertEquals(2, outEnrollment.getNumOfParticipants());
		Assert.assertEquals(1, outEnrollment.getNumInWaitingList());
		Assert.assertFalse(outEnrollment.isParticipant());
		Assert.assertFalse(outEnrollment.isWaiting());
	}
	
	@Test
	public void getEnrollmentRows_withoutWaitingList() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("en-coach-1");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-2");
		Identity waiter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-wait-3");
		Identity out = JunitTestHelper.createAndPersistIdentityAsRndUser("en-out-4");
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(coach, "en-1", "en-1", BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, resource);
		
		businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(waiter1, group, GroupRoles.waiting.name());
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group.getKey());
		
		//check participant 1
		List<EnrollmentRow> enrollments = enrollmentManager.getEnrollments(participant1, groupKeys, null, 128);
		Assert.assertNotNull(enrollments);
		Assert.assertEquals(1, enrollments.size());
		EnrollmentRow enrollment = enrollments.get(0);
		Assert.assertEquals(group.getKey(), enrollment.getKey());
		Assert.assertEquals(group.getName(), enrollment.getName());
		Assert.assertEquals(2, enrollment.getNumOfParticipants());
		Assert.assertEquals(0, enrollment.getNumInWaitingList());
		Assert.assertTrue(enrollment.isParticipant());
		Assert.assertFalse(enrollment.isWaiting());
		
		//check waiter (which not exists in enroll because the flag waiting list is set to false)
		List<EnrollmentRow> waitingEnrollments = enrollmentManager.getEnrollments(waiter1, groupKeys, null, 128);
		Assert.assertNotNull(waitingEnrollments);
		Assert.assertEquals(1, waitingEnrollments.size());
		EnrollmentRow waitingEnrollment = waitingEnrollments.get(0);
		Assert.assertEquals(group.getKey(), waitingEnrollment.getKey());
		Assert.assertEquals(group.getName(), waitingEnrollment.getName());
		Assert.assertEquals(2, waitingEnrollment.getNumOfParticipants());
		Assert.assertEquals(0, waitingEnrollment.getNumInWaitingList());
		Assert.assertEquals(-1, waitingEnrollment.getPositionInWaitingList());
		Assert.assertFalse(waitingEnrollment.isParticipant());
		Assert.assertFalse(waitingEnrollment.isWaiting());
		
		//check out
		List<EnrollmentRow> outEnrollments = enrollmentManager.getEnrollments(out, groupKeys, null, 128);
		Assert.assertNotNull(outEnrollments);
		Assert.assertEquals(1, outEnrollments.size());
		EnrollmentRow outEnrollment = outEnrollments.get(0);
		Assert.assertEquals(group.getKey(), outEnrollment.getKey());
		Assert.assertEquals(group.getName(), outEnrollment.getName());
		Assert.assertEquals(2, outEnrollment.getNumOfParticipants());
		Assert.assertEquals(0, outEnrollment.getNumInWaitingList());
		Assert.assertFalse(outEnrollment.isParticipant());
		Assert.assertFalse(outEnrollment.isWaiting());
	}
	
	@Test
	public void getEnrollmentRows_withAreas() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("en-area-1");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-3");
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-4");
		Identity participant5 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-part-5");
		Identity waiter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-wait-3");
		Identity waiter2 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-wait-4");
		Identity waiter3 = JunitTestHelper.createAndPersistIdentityAsRndUser("en-wait-5");
		
		//create a resource, an area, a group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area = areaManager.createAndPersistBGArea("en-area-" + areaName, "description:" + areaName, resource.getOlatResource());
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "en-area-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "en-group-2", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "en-group-3", "area-group-desc",BusinessGroup.BUSINESS_TYPE, 
				0, 10, true, false, resource);
		
		businessGroupRelationDao.addRole(participant1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant4, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant5, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(waiter1, group2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(waiter2, group2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(waiter3, group2, GroupRoles.waiting.name());
		
		areaManager.addBGToBGArea(group1, area);
		areaManager.addBGToBGArea(group2, area);
		dbInstance.commitAndCloseSession();
		
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());
		List<Long> areaKeys = new ArrayList<>();
		areaKeys.add(area.getKey());
		
		//check id enrollments
		List<EnrollmentRow> idEnrollments = enrollmentManager.getEnrollments(id, groupKeys, areaKeys, 128);
		Assert.assertNotNull(idEnrollments);
		Assert.assertEquals(3, idEnrollments.size());
		
		//check enrollment group 1
		EnrollmentRow enrollment1 = getEnrollmentRowFor(group1, idEnrollments);
		Assert.assertEquals(group1.getKey(), enrollment1.getKey());
		Assert.assertEquals(group1.getName(), enrollment1.getName());
		Assert.assertEquals(1, enrollment1.getNumOfParticipants());
		Assert.assertEquals(0, enrollment1.getNumInWaitingList());
		Assert.assertFalse(enrollment1.isParticipant());
		Assert.assertFalse(enrollment1.isWaiting());

		//check enrollment group 2
		EnrollmentRow enrollment2 = getEnrollmentRowFor(group2, idEnrollments);
		Assert.assertEquals(group2.getKey(), enrollment2.getKey());
		Assert.assertEquals(group2.getName(), enrollment2.getName());
		Assert.assertEquals(4, enrollment2.getNumOfParticipants());
		Assert.assertEquals(3, enrollment2.getNumInWaitingList());
		Assert.assertFalse(enrollment2.isParticipant());
		Assert.assertFalse(enrollment2.isWaiting());
		
		//check enrollment group 3
		EnrollmentRow enrollment3 = getEnrollmentRowFor(group3, idEnrollments);
		Assert.assertEquals(group3.getKey(), enrollment3.getKey());
		Assert.assertEquals(group3.getName(), enrollment3.getName());
		Assert.assertEquals(0, enrollment3.getNumOfParticipants());
		Assert.assertEquals(0, enrollment3.getNumInWaitingList());
		Assert.assertFalse(enrollment3.isParticipant());
		Assert.assertFalse(enrollment3.isWaiting());
		
		
		//check enrollments of participant5
		List<EnrollmentRow> part5Enrollments = enrollmentManager.getEnrollments(participant5, groupKeys, areaKeys, 128);
		Assert.assertNotNull(part5Enrollments);
		Assert.assertEquals(3, part5Enrollments.size());
		
		EnrollmentRow enrollment2_w5 = getEnrollmentRowFor(group2, part5Enrollments);
		Assert.assertEquals(group2.getKey(), enrollment2_w5.getKey());
		Assert.assertEquals(group2.getName(), enrollment2_w5.getName());
		Assert.assertEquals(4, enrollment2_w5.getNumOfParticipants());
		Assert.assertEquals(3, enrollment2_w5.getNumInWaitingList());
		Assert.assertTrue(enrollment2_w5.isParticipant());
		Assert.assertFalse(enrollment2_w5.isWaiting());
		

		//check enrollments of waiter 3
		List<EnrollmentRow> wait3Enrollments = enrollmentManager.getEnrollments(waiter3, groupKeys, areaKeys, 128);
		Assert.assertNotNull(wait3Enrollments);
		Assert.assertEquals(3, wait3Enrollments.size());
		
		EnrollmentRow enrollment2_p3 = getEnrollmentRowFor(group2, wait3Enrollments);
		Assert.assertEquals(group2.getKey(), enrollment2_p3.getKey());
		Assert.assertEquals(group2.getName(), enrollment2_p3.getName());
		Assert.assertEquals(4, enrollment2_p3.getNumOfParticipants());
		Assert.assertEquals(3, enrollment2_p3.getNumInWaitingList());
		Assert.assertFalse(enrollment2_p3.isParticipant());
		Assert.assertTrue(enrollment2_p3.isWaiting());
	}
	
	private EnrollmentRow getEnrollmentRowFor(BusinessGroup group, List<EnrollmentRow> enrollments) {
		if(enrollments == null || enrollments.isEmpty()) return null;
		
		EnrollmentRow row = null;
		for(EnrollmentRow enrollment:enrollments) {
			if(enrollment.getKey().equals(group.getKey())) {
				row = enrollment;
			}
		}

		return row;
	}
	
	/**
	 * Test the bevahior with no data. It's important because some of the value returned
	 * by the database can be null, and this is database dependant.
	 */
	@Test
	public void getEnrollmentRows_null() {
		Identity dummy = JunitTestHelper.createAndPersistIdentityAsRndUser("en-dummy-1");
		dbInstance.commitAndCloseSession();

		//null
		List<EnrollmentRow> nullEnrollments = enrollmentManager.getEnrollments(dummy, null, null, 128);
		Assert.assertNotNull(nullEnrollments);
		Assert.assertEquals(0, nullEnrollments.size());
		
		//wrong keys
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(27l);
		List<Long> areaKeys = new ArrayList<>();
		areaKeys.add(27l);
		
		List<EnrollmentRow> groupEnrollments = enrollmentManager.getEnrollments(dummy, groupKeys, null, 128);
		Assert.assertNotNull(groupEnrollments);
		Assert.assertEquals(0, groupEnrollments.size());
		
		List<EnrollmentRow> areaEnrollments = enrollmentManager.getEnrollments(dummy, null, areaKeys, 128);
		Assert.assertNotNull(areaEnrollments);
		Assert.assertEquals(0, areaEnrollments.size());
		
		List<EnrollmentRow> groupAndAreaEnrollments = enrollmentManager.getEnrollments(dummy, groupKeys, areaKeys, 128);
		Assert.assertNotNull(groupAndAreaEnrollments);
		Assert.assertEquals(0, groupAndAreaEnrollments.size());
	}

}
