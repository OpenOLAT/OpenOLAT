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
package org.olat.course.assessment.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createAssessmentMode() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		
		mode.setName("Assessment in sight");
		mode.setDescription("Assessment description");
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, 2);
		Date begin = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 2);
		Date end = cal.getTime();
		mode.setBegin(begin);
		mode.setEnd(end);
		mode.setLeadTime(15);
		
		mode.setTargetAudience(Target.course);
		
		mode.setRestrictAccessElements(true);
		mode.setElementList("173819739,239472389");
		
		mode.setRestrictAccessIps(true);
		mode.setIpList("192.168.1.123");
		
		mode.setSafeExamBrowser(true);
		mode.setSafeExamBrowserKey("785rhqg47368ahfahl");
		mode.setSafeExamBrowserHint("Use the SafeExamBrowser");
		
		mode.setApplySettingsForCoach(true);
		
		AssessmentMode savedMode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		Assert.assertNotNull(savedMode.getKey());
		Assert.assertNotNull(savedMode.getCreationDate());
		Assert.assertNotNull(savedMode.getLastModified());

		//reload and check
		AssessmentMode reloadedMode = assessmentModeMgr.loadById(savedMode.getKey());
		Assert.assertNotNull(reloadedMode);
		Assert.assertEquals(savedMode.getKey(), reloadedMode.getKey());
		Assert.assertNotNull(reloadedMode.getCreationDate());
		Assert.assertNotNull(reloadedMode.getLastModified());
		Assert.assertEquals(savedMode, reloadedMode);
		
		Assert.assertEquals("Assessment in sight", reloadedMode.getName());
		Assert.assertEquals("Assessment description", reloadedMode.getDescription());
		
		Assert.assertEquals(begin, reloadedMode.getBegin());
		Assert.assertEquals(end, reloadedMode.getEnd());
		Assert.assertEquals(15, reloadedMode.getLeadTime());
		
		Assert.assertEquals(Target.course, reloadedMode.getTargetAudience());
		
		Assert.assertTrue(reloadedMode.isRestrictAccessElements());
		Assert.assertEquals("173819739,239472389", reloadedMode.getElementList());
		
		Assert.assertTrue(reloadedMode.isRestrictAccessIps());
		Assert.assertEquals("192.168.1.123", reloadedMode.getIpList());
		
		Assert.assertTrue(reloadedMode.isApplySettingsForCoach());
		
		Assert.assertTrue(reloadedMode.isSafeExamBrowser());
		Assert.assertEquals("785rhqg47368ahfahl", reloadedMode.getSafeExamBrowserKey());
		Assert.assertEquals("Use the SafeExamBrowser", reloadedMode.getSafeExamBrowserHint());
	}
	
	@Test
	public void loadAssessmentModes() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		List<AssessmentMode> assessmentModes = assessmentModeMgr.loadAssessmentMode(entry);
		Assert.assertNotNull(assessmentModes);
		Assert.assertEquals(1, assessmentModes.size());
		Assert.assertEquals(savedMode, assessmentModes.get(0));
	}
	
	@Test
	public void createAssessmentModeToGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "", null, null, null, null, false, false, null);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		AssessmentMode savedMode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode reloadedMode = assessmentModeMgr.loadById(mode.getKey());
		Assert.assertEquals(mode, reloadedMode);
		Assert.assertEquals(savedMode, reloadedMode);
		Assert.assertNotNull(reloadedMode.getGroups());
		Assert.assertEquals(1, reloadedMode.getGroups().size());
		Assert.assertEquals(modeToGroup, reloadedMode.getGroups().iterator().next());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void loadAssessmentMode_repositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		List<AssessmentMode> currentModes = assessmentModeMgr.loadAssessmentMode(entry);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void loadCurrentAssessmentModes() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		List<AssessmentMode> currentModes = assessmentModeMgr.loadCurrentAssessmentModes();
		Assert.assertNotNull(currentModes);
		Assert.assertFalse(currentModes.isEmpty());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	/**
	 * Check an assessment linked to a group with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInBusinessGroup() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-3");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-3");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as-mode-2", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.save(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.loadAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.loadAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.loadAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	
	/**
	 * Check an assessment linked to a group with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInBusinessGroup_coach() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-3", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.save(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.loadAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.loadAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.loadAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCourse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6");
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.loadAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.loadAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.loadAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCourse_coach() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-7");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-8");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-9");
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.save(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.loadAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.loadAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.loadAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	
	private AssessmentMode createMinimalAssessmentmode(RepositoryEntry entry) {
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		return mode;
	}
	
}
