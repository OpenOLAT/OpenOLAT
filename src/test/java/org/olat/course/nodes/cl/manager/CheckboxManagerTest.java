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
package org.olat.course.nodes.cl.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.cl.model.AssessmentData;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.model.DBCheckbox;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private CheckboxManagerImpl checkboxManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createCheckBox() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-1", 2345l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		dbInstance.commit();
		
		Assert.assertNotNull(checkbox);
		Assert.assertNotNull(checkbox.getKey());
		Assert.assertNotNull(checkbox.getCreationDate());
		Assert.assertEquals(checkboxId, checkbox.getCheckboxId());
		Assert.assertEquals("checkbox-1", checkbox.getResName());
		Assert.assertEquals(Long.valueOf(2345l), checkbox.getResId());
		Assert.assertEquals(resSubPath, checkbox.getResSubPath());
	}
	
	@Test
	public void loadCheckBox() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-2", 2346l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		dbInstance.commit();
		
		//load it
		List<DBCheckbox> checkboxList = checkboxManager.loadCheckbox(ores, resSubPath);
		Assert.assertNotNull(checkboxList);
		Assert.assertEquals(1, checkboxList.size());
		DBCheckbox dbCheckbox = checkboxList.get(0);
		Assert.assertEquals(checkbox, dbCheckbox);
		//paranoia check
		Assert.assertNotNull(dbCheckbox.getCreationDate());
		Assert.assertEquals(checkboxId, dbCheckbox.getCheckboxId());
		Assert.assertEquals(resSubPath, dbCheckbox.getResSubPath());
		Assert.assertEquals("checkbox-2", dbCheckbox.getResName());
		Assert.assertEquals(Long.valueOf(2346l), dbCheckbox.getResId());
		Assert.assertEquals(resSubPath, dbCheckbox.getResSubPath());
	}
	
	@Test
	public void loadCheckBox_withcheckboxId() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-7", 2351l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		dbInstance.commit();
		
		//load it
		DBCheckbox loadedBox = checkboxManager.loadCheckbox(ores, resSubPath, checkboxId);
		Assert.assertNotNull(loadedBox);
		Assert.assertEquals(checkbox, loadedBox);
	}
	
	@Test
	public void removeCheckBox() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-3", 2347l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		dbInstance.commit();
		Assert.assertNotNull(checkbox);
		
		//load it
		List<DBCheckbox> checkboxList = checkboxManager.loadCheckbox(ores, resSubPath);
		Assert.assertNotNull(checkboxList);
		Assert.assertEquals(1, checkboxList.size());
		dbInstance.commitAndCloseSession();
		
		//remove
		checkboxManager.removeCheckbox(checkboxList.get(0));
		dbInstance.commitAndCloseSession();

		//reload
		List<DBCheckbox> deletedCheckboxList = checkboxManager.loadCheckbox(ores, resSubPath);
		Assert.assertNotNull(checkboxList);
		Assert.assertEquals(0, deletedCheckboxList.size());
	}
	
	@Test
	public void syncCheckBox() {
		// build a list of checkbox to sync
		CheckboxList list = new CheckboxList();
		Checkbox checkbox1 = new Checkbox();
		checkbox1.setCheckboxId(UUID.randomUUID().toString());
		checkbox1.setTitle("Sync me");
		list.add(checkbox1);
		
		Checkbox checkbox2 = new Checkbox();
		checkbox2.setCheckboxId(UUID.randomUUID().toString());
		checkbox2.setTitle("Sync me too");
		list.add(checkbox2);
		
		//sync them to the database
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-4", 2348l);
		String resSubPath = UUID.randomUUID().toString();
		checkboxManager.syncCheckbox(list, ores, resSubPath);
		dbInstance.commitAndCloseSession();
		
		//load them
		List<DBCheckbox> dbCheckboxList = checkboxManager.loadCheckbox(ores, resSubPath);
		Assert.assertNotNull(dbCheckboxList);
		Assert.assertEquals(2, dbCheckboxList.size());
		
		for(DBCheckbox dbCheckbox:dbCheckboxList) {
			Assert.assertNotNull(dbCheckbox);
			Assert.assertNotNull(dbCheckbox.getKey());
			Assert.assertNotNull(dbCheckbox.getCheckboxId());
			Assert.assertTrue(dbCheckbox.getCheckboxId().equals(checkbox1.getCheckboxId())
					|| dbCheckbox.getCheckboxId().equals(checkbox2.getCheckboxId()));
		}
	}
	
	@Test
	public void createCheck() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("check-1");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-5", 2349l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		
		//create a check
		DBCheck check = checkboxManager.createCheck(checkbox, id, Float.valueOf(1.0f), Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		//paranoia check
		Assert.assertNotNull(check);
		Assert.assertNotNull(check.getKey());
		Assert.assertNotNull(check.getCreationDate());
		Assert.assertNotNull(check.getLastModified());
		Assert.assertEquals(id, check.getIdentity());
		Assert.assertEquals(checkbox, check.getCheckbox());
		Assert.assertEquals(Boolean.TRUE, check.getChecked());
		Assert.assertEquals(1.0f, check.getScore().floatValue(), 0.00001);
	}
	
	@Test
	public void loadCheck() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("check-2");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-6", 2350l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		
		//create a check
		DBCheck check = checkboxManager.createCheck(checkbox, id, Float.valueOf(1.0f), Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		
		//load the check
		DBCheck loadedCheck = checkboxManager.loadCheck(checkbox, id);
		//paranoia check
		Assert.assertNotNull(loadedCheck);
		Assert.assertEquals(check, loadedCheck);
		Assert.assertEquals(id, loadedCheck.getIdentity());
		Assert.assertEquals(checkbox, loadedCheck.getCheckbox());
		Assert.assertEquals(Boolean.TRUE, loadedCheck.getChecked());
		Assert.assertEquals(1.0f, loadedCheck.getScore().floatValue(), 0.00001);
	}
	
	@Test
	public void loadChecks_byOres() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("check-3");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-8", 2352l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		//create a check
		DBCheck check = checkboxManager.createCheck(checkbox, id, null, Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		
		//load the check
		List<DBCheck> loadedChecks = checkboxManager.loadCheck(id, ores, resSubPath);
		Assert.assertNotNull(loadedChecks);
		Assert.assertEquals(1, loadedChecks.size());
		Assert.assertEquals(check, loadedChecks.get(0));
	}
	
	@Test
	public void testCheck() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("check-2");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-6", 2350l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, ores, resSubPath);
		dbInstance.commitAndCloseSession();
		
		//check
		checkboxManager.check(checkbox, id, Float.valueOf(1.515f), Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		//load the check
		DBCheck loadedCheck = checkboxManager.loadCheck(checkbox, id);
		//paranoia check
		Assert.assertNotNull(loadedCheck);
		Assert.assertEquals(id, loadedCheck.getIdentity());
		Assert.assertEquals(checkbox, loadedCheck.getCheckbox());
		Assert.assertEquals(Boolean.FALSE, loadedCheck.getChecked());
		Assert.assertEquals(1.515f, loadedCheck.getScore().floatValue(), 0.00001);
	}
	
	@Test
	public void loadAssessmentDatas() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-4");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-5");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-6");
		repositoryEntryRelationDao.addRole(id1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id3, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		OLATResourceable ores = entry.getOlatResource();
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId1 = UUID.randomUUID().toString();
		DBCheckbox checkbox1 = checkboxManager.createDBCheckbox(checkboxId1, ores, resSubPath);
		String checkboxId2 = UUID.randomUUID().toString();
		DBCheckbox checkbox2 = checkboxManager.createDBCheckbox(checkboxId2, ores, resSubPath);
		//create a check
		DBCheck check1_1 = checkboxManager.createCheck(checkbox1, id1, null, Boolean.TRUE);
		DBCheck check1_2 = checkboxManager.createCheck(checkbox2, id1, null, Boolean.TRUE);
		DBCheck check2_1 = checkboxManager.createCheck(checkbox1, id2, null, Boolean.TRUE);
		DBCheck check3_1 = checkboxManager.createCheck(checkbox1, id3, null, Boolean.TRUE);
		DBCheck check3_2 = checkboxManager.createCheck(checkbox2, id3, null, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		//load the check
		List<AssessmentData> loadedChecks = checkboxManager.getAssessmentDatas(ores, resSubPath, entry, null, true, null);
		Assert.assertNotNull(loadedChecks);
		Assert.assertEquals(3, loadedChecks.size());
		
		List<DBCheck> collectedChecks = new ArrayList<>();
		for(AssessmentData loadedCheck:loadedChecks) {
			for(DBCheck loaded:loadedCheck.getChecks()) {
				collectedChecks.add(loaded);
			}
		}

		Assert.assertEquals(5, collectedChecks.size());
		Assert.assertTrue(collectedChecks.contains(check1_1));
		Assert.assertTrue(collectedChecks.contains(check1_2));
		Assert.assertTrue(collectedChecks.contains(check2_1));
		Assert.assertTrue(collectedChecks.contains(check3_1));
		Assert.assertTrue(collectedChecks.contains(check3_2));
	}

	@Test
	public void loadAssessmentDatas_inCourse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("check-18");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-check-18");
		repositoryEntryRelationDao.addRole(id, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		String checkboxId = UUID.randomUUID().toString();
		String resSubPath = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, entry.getOlatResource(), resSubPath);
		DBCheck check = checkboxManager.createCheck(checkbox, id, null, Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		
		//load and check the check
		List<AssessmentData> loadedChecks = checkboxManager.getAssessmentDatas(entry.getOlatResource(), resSubPath, entry, coach, false, null);
		Assert.assertNotNull(loadedChecks);
		Assert.assertEquals(1, loadedChecks.size());
		AssessmentData data = loadedChecks.get(0);
		Assert.assertNotNull(data);
		Assert.assertNotNull(data.getChecks());
		Assert.assertEquals(1, data.getChecks().size());
		Assert.assertEquals(check, data.getChecks().get(0));
		Assert.assertEquals(id, data.getIdentity());
	}
	
	@Test
	public void loadAssessmentDatas_inGroup() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("check-19");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-check-19");
		BusinessGroup group = businessGroupDao.createAndPersist(coach, "gcheck", "gcheck-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRelationToResource(group, entry);
		dbInstance.commitAndCloseSession();
		
		String checkboxId = UUID.randomUUID().toString();
		String resSubPath = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, entry.getOlatResource(), resSubPath);
		DBCheck check = checkboxManager.createCheck(checkbox, id, null, Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentData> loadedChecks = checkboxManager.getAssessmentDatas(entry.getOlatResource(), resSubPath, entry, coach, true, null);
		Assert.assertNotNull(loadedChecks);
		Assert.assertEquals(1, loadedChecks.size());
		AssessmentData data = loadedChecks.get(0);
		Assert.assertNotNull(data);
		Assert.assertNotNull(data.getChecks());
		Assert.assertEquals(1, data.getChecks().size());
		Assert.assertEquals(check, data.getChecks().get(0));
		Assert.assertEquals(id, data.getIdentity());
	}
	
	@Test
	public void loadAssessmentDatas_admin() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity groupParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("check-20");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gcheck", "gcheck-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		businessGroupRelationDao.addRole(groupParticipant, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRelationToResource(group, entry);

		Identity courseParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("check-21");
		repositoryEntryRelationDao.addRole(courseParticipant, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//add some noise
		Identity courseOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("check-22");
		repositoryEntryRelationDao.addRole(courseOwner, entry, GroupRoles.owner.name());
		Identity groupWaiting = JunitTestHelper.createAndPersistIdentityAsRndUser("check-23");
		businessGroupRelationDao.addRole(groupWaiting, group, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();
		
		
		String checkboxId = UUID.randomUUID().toString();
		String resSubPath = UUID.randomUUID().toString();
		DBCheckbox checkbox = checkboxManager.createDBCheckbox(checkboxId, entry.getOlatResource(), resSubPath);
		DBCheck checkGroup = checkboxManager.createCheck(checkbox, groupParticipant, null, Boolean.TRUE);
		DBCheck checkCourse = checkboxManager.createCheck(checkbox, courseParticipant, null, Boolean.TRUE);
		DBCheck checkNotVisible1 = checkboxManager.createCheck(checkbox, groupWaiting, null, Boolean.FALSE);
		DBCheck checkNotVisible2 = checkboxManager.createCheck(checkbox, courseOwner, null, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentData> loadedChecks = checkboxManager.getAssessmentDatas(entry.getOlatResource(), resSubPath, entry, null, true, null);
		Assert.assertNotNull(loadedChecks);
		Assert.assertEquals(2, loadedChecks.size());
		
		List<DBCheck> collectedChecks = new ArrayList<>();
		for(AssessmentData loadedCheck:loadedChecks) {
			for(DBCheck loaded:loadedCheck.getChecks()) {
				collectedChecks.add(loaded);
			}
		}
		
		Assert.assertEquals(2, collectedChecks.size());
		Assert.assertTrue(collectedChecks.contains(checkGroup));
		Assert.assertTrue(collectedChecks.contains(checkCourse));
		Assert.assertFalse(collectedChecks.contains(checkNotVisible1));
		Assert.assertFalse(collectedChecks.contains(checkNotVisible2));
	}
	
	@Test
	public void countChecks_resource() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-16");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-17");
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-12", 2354l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId1 = UUID.randomUUID().toString();
		DBCheckbox checkbox1 = checkboxManager.createDBCheckbox(checkboxId1, ores, resSubPath);
		String checkboxId2 = UUID.randomUUID().toString();
		DBCheckbox checkbox2 = checkboxManager.createDBCheckbox(checkboxId2, ores, resSubPath);
		String checkboxId3 = UUID.randomUUID().toString();
		DBCheckbox checkbox3 = checkboxManager.createDBCheckbox(checkboxId3, ores, resSubPath);
		String checkboxId4 = UUID.randomUUID().toString();
		checkboxManager.createDBCheckbox(checkboxId4, ores, resSubPath);
		//create a check
		checkboxManager.createCheck(checkbox1, id1, null, Boolean.TRUE);
		checkboxManager.createCheck(checkbox2, id1, null, Boolean.TRUE);
		checkboxManager.createCheck(checkbox1, id2, null, Boolean.TRUE);
		checkboxManager.createCheck(checkbox3, id2, null, Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		
		//count the checks
		int checked = checkboxManager.countChecks(ores, resSubPath);
		Assert.assertEquals(4, checked);
	}
	
	@Test
	public void countChecked_withIdentity() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-8");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-12");
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-10", 2354l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId1 = UUID.randomUUID().toString();
		DBCheckbox checkbox1 = checkboxManager.createDBCheckbox(checkboxId1, ores, resSubPath);
		String checkboxId2 = UUID.randomUUID().toString();
		DBCheckbox checkbox2 = checkboxManager.createDBCheckbox(checkboxId2, ores, resSubPath);
		String checkboxId3 = UUID.randomUUID().toString();
		DBCheckbox checkbox3 = checkboxManager.createDBCheckbox(checkboxId3, ores, resSubPath);
		String checkboxId4 = UUID.randomUUID().toString();
		DBCheckbox checkbox4 = checkboxManager.createDBCheckbox(checkboxId4, ores, resSubPath);
		//create a check
		checkboxManager.createCheck(checkbox1, id1, null, Boolean.TRUE);
		checkboxManager.createCheck(checkbox2, id1, null, Boolean.TRUE);
		checkboxManager.createCheck(checkbox1, id2, null, Boolean.TRUE);
		checkboxManager.createCheck(checkbox3, id2, null, Boolean.FALSE);
		checkboxManager.createCheck(checkbox4, id2, null, Boolean.TRUE);
		dbInstance.commitAndCloseSession();
		
		//count the checks
		int id1Checked = checkboxManager.countChecked(id1, ores, resSubPath);
		Assert.assertEquals(2, id1Checked);

		int id2Checked = checkboxManager.countChecked(id2, ores, resSubPath);
		Assert.assertEquals(2, id2Checked);
		
		int id3Checked = checkboxManager.countChecked(id3, ores, resSubPath);
		Assert.assertEquals(0, id3Checked);
	}
	
	@Test
	public void calculateScore() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-9");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-10");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-11");
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-10", 2355l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId1 = UUID.randomUUID().toString();
		DBCheckbox checkbox1 = checkboxManager.createDBCheckbox(checkboxId1, ores, resSubPath);
		String checkboxId2 = UUID.randomUUID().toString();
		DBCheckbox checkbox2 = checkboxManager.createDBCheckbox(checkboxId2, ores, resSubPath);
		String checkboxId3 = UUID.randomUUID().toString();
		DBCheckbox checkbox3 = checkboxManager.createDBCheckbox(checkboxId3, ores, resSubPath);
		String checkboxId4 = UUID.randomUUID().toString();
		DBCheckbox checkbox4 = checkboxManager.createDBCheckbox(checkboxId4, ores, resSubPath);
		//create a check
		checkboxManager.createCheck(checkbox1, id1, 3.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox2, id1, 2.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox1, id2, 4.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox3, id2, 5.5f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox4, id2, 1.0f, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		//count the checks
		float score1 = checkboxManager.calculateScore(id1, ores, resSubPath);
		Assert.assertEquals(5.0f, score1, 0.0001);

		float score2 = checkboxManager.calculateScore(id2, ores, resSubPath);
		Assert.assertEquals(9.5f, score2, 0.0001);
		
		float score3 = checkboxManager.calculateScore(id3, ores, resSubPath);
		Assert.assertEquals(0.0f, score3, 0.0001);
	}
	
	@Test
	public void calculateResource() {
		//create checkbox to delete
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-13");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-14");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-15");
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("checkbox-11", 2356l);
		String resSubPath = UUID.randomUUID().toString();
		String checkboxId1 = UUID.randomUUID().toString();
		DBCheckbox checkbox1 = checkboxManager.createDBCheckbox(checkboxId1, ores, resSubPath);
		String checkboxId2 = UUID.randomUUID().toString();
		DBCheckbox checkbox2 = checkboxManager.createDBCheckbox(checkboxId2, ores, resSubPath);
		String checkboxId3 = UUID.randomUUID().toString();
		DBCheckbox checkbox3 = checkboxManager.createDBCheckbox(checkboxId3, ores, resSubPath);
		//create a check
		checkboxManager.createCheck(checkbox1, id1, 3.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox2, id1, 2.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox1, id2, 4.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox3, id2, 5.5f, Boolean.TRUE);
		checkboxManager.createCheck(checkbox3, id3, 1.0f, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
	
		//create a reference which must stay
		OLATResourceable oresRef = OresHelper.createOLATResourceableInstance("checkbox-12", 2357l);
		String resSubPathRef = UUID.randomUUID().toString();
		String checkboxIdRef = UUID.randomUUID().toString();
		DBCheckbox checkboxRef = checkboxManager.createDBCheckbox(checkboxIdRef, oresRef, resSubPathRef);
		checkboxManager.createCheck(checkboxRef, id1, 3.0f, Boolean.TRUE);
		checkboxManager.createCheck(checkboxRef, id3, 2.0f, Boolean.TRUE);
		dbInstance.commitAndCloseSession();

		//delete all checks and checklist of the resource
		checkboxManager.deleteCheckbox(ores, resSubPath);
		dbInstance.commitAndCloseSession();
		
		//verify id1
		List<DBCheck> checksId1 = checkboxManager.loadCheck(id1, ores, resSubPath);
		Assert.assertNotNull(checksId1);
		Assert.assertEquals(0, checksId1.size());
		List<DBCheck> checksId1Ref = checkboxManager.loadCheck(id1, oresRef, resSubPathRef);
		Assert.assertNotNull(checksId1Ref);
		Assert.assertEquals(1, checksId1Ref.size());
		//verify id2
		List<DBCheck> checksId2 = checkboxManager.loadCheck(id2, ores, resSubPath);
		Assert.assertNotNull(checksId2);
		Assert.assertEquals(0, checksId2.size());
		List<DBCheck> checksId2Ref = checkboxManager.loadCheck(id2, oresRef, resSubPathRef);
		Assert.assertNotNull(checksId2Ref);
		Assert.assertEquals(0, checksId2Ref.size());
		//verify id3
		List<DBCheck> checksId3 = checkboxManager.loadCheck(id3, ores, resSubPath);
		Assert.assertNotNull(checksId3);
		Assert.assertEquals(0, checksId3.size());
		List<DBCheck> checksId3Ref = checkboxManager.loadCheck(id3, oresRef, resSubPathRef);
		Assert.assertNotNull(checksId3Ref);
		Assert.assertEquals(1, checksId3Ref.size());
	}
}
