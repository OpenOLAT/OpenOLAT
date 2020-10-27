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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Encoder;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

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
	private BGAreaManager areaMgr;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
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
		
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		Assert.assertNotNull(savedMode.getKey());
		Assert.assertNotNull(savedMode.getCreationDate());
		Assert.assertNotNull(savedMode.getLastModified());

		//reload and check
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(savedMode.getKey());
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
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		List<AssessmentMode> assessmentModes = assessmentModeMgr.getAssessmentModeFor(entry);
		Assert.assertNotNull(assessmentModes);
		Assert.assertEquals(1, assessmentModes.size());
		Assert.assertEquals(savedMode, assessmentModes.get(0));
	}
	
	@Test
	public void createAssessmentModeToGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "", null, null, null, null, false, false, null);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		AssessmentMode savedMode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		Assert.assertEquals(mode, reloadedMode);
		Assert.assertEquals(savedMode, reloadedMode);
		Assert.assertNotNull(reloadedMode.getGroups());
		Assert.assertEquals(1, reloadedMode.getGroups().size());
		Assert.assertEquals(modeToGroup, reloadedMode.getGroups().iterator().next());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAssessmentModeToArea() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "", null, null, null, null, false, false, null);
		BGArea area = areaMgr.createAndPersistBGArea("little area", "My little secret area", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		AssessmentMode savedMode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		Assert.assertEquals(mode, reloadedMode);
		Assert.assertEquals(savedMode, reloadedMode);
		Assert.assertNotNull(reloadedMode.getAreas());
		Assert.assertEquals(1, reloadedMode.getAreas().size());
		Assert.assertEquals(modeToArea, reloadedMode.getAreas().iterator().next());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAssessmentMode_lectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(lectureBlock, 5, 10, "192.168.1.203", "very-complicated-key");
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		AssessmentMode lecturedMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
		Assert.assertNotNull(lecturedMode);
		Assert.assertEquals(mode, lecturedMode);
		Assert.assertEquals(lectureBlock, lecturedMode.getLectureBlock());
		Assert.assertEquals(5, lecturedMode.getLeadTime());
		Assert.assertEquals(10, lecturedMode.getFollowupTime());
		Assert.assertEquals("192.168.1.203", lecturedMode.getIpList());
		Assert.assertEquals("very-complicated-key", lecturedMode.getSafeExamBrowserKey());
	}
	
	@Test
	public void deleteAssessmentMode() {
		//prepare the setup
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "", null, null, null, null, false, false, null);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		AssessmentMode savedMode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup businessGroupForArea = businessGroupService.createBusinessGroup(author, "as_mode_1", "", null, null, null, null, false, false, null);
		BGArea area = areaMgr.createAndPersistBGArea("little area", "My little secret area", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroupForArea, area);
		dbInstance.commitAndCloseSession();
		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(savedMode, area);
		savedMode.getAreas().add(modeToArea);
		savedMode = assessmentModeMgr.merge(savedMode, true);
		dbInstance.commitAndCloseSession();
		
		//delete
		assessmentModeMgr.delete(savedMode);
		dbInstance.commit();
		//check
		AssessmentMode deletedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		Assert.assertNull(deletedMode);
	}
	
	@Test
	public void loadAssessmentMode_repositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(entry);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void loadAssessmentMode_lectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		((AssessmentModeImpl)mode).setLectureBlock(lectureBlock);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		AssessmentMode lecturedMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
		Assert.assertNotNull(lecturedMode);
		Assert.assertEquals(mode, lecturedMode);
		Assert.assertEquals(lectureBlock, lecturedMode.getLectureBlock());
	}
	
	@Test
	public void loadCurrentAssessmentModes() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModes(now);
		Assert.assertNotNull(currentModes);
		Assert.assertFalse(currentModes.isEmpty());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	/**
	 * Manual without lead time -> not in the current list
	 */
	@Test
	public void loadCurrentAssessmentModes_manualNow() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
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
		mode.setManualBeginEnd(true);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModes(now);
		Assert.assertNotNull(currentModes);
		Assert.assertFalse(currentModes.contains(mode));
	}
	
	/**
	 * Manual with lead time -> in the current list
	 */
	@Test
	public void loadCurrentAssessmentModes_manualNowLeadingTime() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode.setLeadTime(120);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModes(now);
		Assert.assertNotNull(currentModes);
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void getCurrentAssessmentMode() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getCurrentAssessmentMode(entry, now);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	/**
	 * Manual without lead time -> not in the current list
	 */
	@Test
	public void getCurrentAssessmentMode_manualNow() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
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
		mode.setManualBeginEnd(true);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getCurrentAssessmentMode(entry, now);
		Assert.assertNotNull(currentModes);
		Assert.assertTrue(currentModes.isEmpty());
	}
	
	/**
	 * Manual with lead time -> in the current list
	 */
	@Test
	public void getCurrentAssessmentMode_manualNowLeadingTime() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode.setLeadTime(120);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getCurrentAssessmentMode(entry, now);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void isNodeInUse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode node = new IQTESTCourseNode();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setElementList(node.getIdent());
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		boolean inUse = assessmentModeMgr.isNodeInUse(entry, node);
		Assert.assertTrue(inUse);
		
		// other node
		CourseNode otherNode = new IQTESTCourseNode();
		boolean notInUse = assessmentModeMgr.isNodeInUse(entry, otherNode);
		Assert.assertFalse(notInUse);
	}
	
	@Test
	public void isInAssessmentMode() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryReference = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		Date now = new Date();
		boolean entryNow = assessmentModeMgr.isInAssessmentMode(entry, now);
		Assert.assertTrue(entryNow);
		
		//no assessment for this course
		boolean entryReferenceNow = assessmentModeMgr.isInAssessmentMode(entryReference, now);
		Assert.assertFalse(entryReferenceNow);
		
		//out of assessment scope
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.DATE, -1);
		Date aDayBefore = cal.getTime();
		boolean entryReferencePast = assessmentModeMgr.isInAssessmentMode(entryReference, aDayBefore);
		Assert.assertFalse(entryReferencePast);
	}
	
	/**
	 * Manual without leading time -> not in assessment mode
	 */
	@Test
	public void isInAssessmentMode_manual() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setManualBeginEnd(true);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		Date now = new Date();
		boolean entryNow = assessmentModeMgr.isInAssessmentMode(entry, now);
		Assert.assertFalse(entryNow);
	}

	/**
	 * Manual with leading time -> in assessment mode
	 */
	@Test
	public void isInAssessmentMode_manualLeadingTime() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode.setLeadTime(120);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		boolean entryNow = assessmentModeMgr.isInAssessmentMode(entry, now);
		Assert.assertTrue(entryNow);
	}
	
	
	/**
	 * Check an assessment linked to a group with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInBusinessGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-3");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-3");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as-mode-2", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	
	/**
	 * Check an assessment linked to a group with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInBusinessGroup_coach() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-3", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
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
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
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
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	/**
	 * Check an assessment linked to an area with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInArea() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-12");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-13");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-14");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as-mode-3", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		BGArea area = areaMgr.createAndPersistBGArea("area for people", "", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	/**
	 * Check an assessment linked to an area with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInArea_coach() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-12");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-13");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-14");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-3", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		BGArea area = areaMgr.createAndPersistBGArea("area for people", "", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCurriculum() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-30");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-31");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-32");
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-1", "Curriculum for assessment", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for assessment", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, coach, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCurriculumCoach() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-35");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-36");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-37");
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-2", "Curriculum for assessment", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for assessment", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, coach, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.curriculumEls);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void getAssessedIdentities_course_groups() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-15");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-16");
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-17");
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-4", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach1, businessGroup, GroupRoles.coach.name());
		
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-18");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-19");
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(4, assessedIdentityKeys.size());
		Assert.assertFalse(assessedIdentityKeys.contains(author.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach2.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant2.getKey()));
	}
	
	@Test
	public void getPlannedAssessmentMode() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
	
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setBegin(DateUtils.addDays(mode.getBegin(), -4));
		mode.setEnd(DateUtils.addDays(mode.getEnd(), -4));
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		Date from = DateUtils.addDays(new Date(), -5);
		Date to = DateUtils.addDays(new Date(), -2);
		List<AssessmentMode> plannedModes = assessmentModeMgr.getPlannedAssessmentMode(entry, from, to);
		assertThat(plannedModes)
			.isNotNull()
			.containsAnyOf(mode);
	}
	
	@Test
	public void findAssessmentModeSearchParams() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
	
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setBegin(DateUtils.addDays(mode.getBegin(), -25));
		((AssessmentModeImpl)mode).setBeginWithLeadTime(mode.getBegin());
		mode.setEnd(DateUtils.addDays(mode.getEnd(), -23));
		((AssessmentModeImpl)mode).setEndWithFollowupTime(mode.getEnd());
		mode.setName("Mode");
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		SearchAssessmentModeParams params = new SearchAssessmentModeParams();
		params.setDate(DateUtils.addDays(new Date(), -24));
		params.setIdAndRefs(entry.getKey().toString());
		params.setName("Mode");
	
		List<AssessmentMode> plannedModes = assessmentModeMgr.findAssessmentMode(params);
		assertThat(plannedModes)
			.isNotNull()
			.containsAnyOf(mode);
	}
	
	@Test
	public void getAssessedIdentities_course_areas() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-20");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-21");
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-22");
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-5", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach1, businessGroup, GroupRoles.coach.name());
		
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-23");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-24");
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		BGArea area = areaMgr.createAndPersistBGArea("area for people", "", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);

		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(4, assessedIdentityKeys.size());
		Assert.assertFalse(assessedIdentityKeys.contains(author.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach2.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant2.getKey()));
	}
	
	@Test
	public void getAssessedIdentities_course_curriculum() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-37");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-38");
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-39");
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-3", "Curriculum for assessment", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for assessment", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant1, CurriculumRoles.participant);
		curriculumService.addMember(element, coach1, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();
		
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-23");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-24");
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);

		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(4, assessedIdentityKeys.size());
		Assert.assertFalse(assessedIdentityKeys.contains(author.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach2.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant2.getKey()));
	}
	
	@Test
	public void getAssessedIdentities_course_curriculumElements() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-39");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-40");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-41");
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-42");
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-4", "Curriculum for assessment", "Curriculum", null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel-1", "Element for assessment",  CurriculumElementStatus.active,
				null, null, null, null, CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-rel-2", "Element for assessment",  CurriculumElementStatus.active,
				null, null, null, null, CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element1, entry, false);
		curriculumService.addRepositoryEntry(element2, entry, false);
		curriculumService.addMember(element1, participant1, CurriculumRoles.participant);
		curriculumService.addMember(element2, participant2, CurriculumRoles.participant);
		curriculumService.addMember(element1, coach1, CurriculumRoles.coach);
		curriculumService.addMember(element2, coach1, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();
		
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-23");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-24");
		repositoryEntryRelationDao.addRole(participant3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.curriculumEls);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);

		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element1);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(2, assessedIdentityKeys.size());
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
	}
	
	@Test
	public void isIpAllowed_exactMatch() {
		String ipList = "192.168.1.203";

		boolean allowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.203");
		Assert.assertTrue(allowed1);

		//negative test
		boolean notAllowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = assessmentModeMgr.isIpAllowed(ipList, "192.168.100.203");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = assessmentModeMgr.isIpAllowed(ipList, "192.203.203.203");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void isIpAllowed_pseudoRange() {
		String ipList = "192.168.1.1 - 192.168.1.128";

		boolean allowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.64");
		Assert.assertTrue(allowed1);

		//negative test
		boolean notAllowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = assessmentModeMgr.isIpAllowed(ipList, "192.168.100.64");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = assessmentModeMgr.isIpAllowed(ipList, "212.203.203.64");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void isIpAllowed_cidr() {
		String ipList = "192.168.100.1/24";

		boolean allowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.100.64");
		Assert.assertTrue(allowed1);

		//negative test
		boolean notAllowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.99.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = assessmentModeMgr.isIpAllowed(ipList, "192.168.101.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = assessmentModeMgr.isIpAllowed(ipList, "192.167.100.1");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = assessmentModeMgr.isIpAllowed(ipList, "212.203.203.64");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void isIpAllowed_all() {
		String ipList = "192.168.1.203\n192.168.30.1 - 192.168.32.128\n192.168.112.1/24";

		boolean allowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.1.203");
		Assert.assertTrue(allowed1);
		boolean allowed2 = assessmentModeMgr.isIpAllowed(ipList, "192.168.31.203");
		Assert.assertTrue(allowed2);
		boolean allowed3 = assessmentModeMgr.isIpAllowed(ipList, "192.168.112.203");
		Assert.assertTrue(allowed3);

		//negative test
		boolean notAllowed1 = assessmentModeMgr.isIpAllowed(ipList, "192.168.99.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = assessmentModeMgr.isIpAllowed(ipList, "192.168.101.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = assessmentModeMgr.isIpAllowed(ipList, "192.167.100.1");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = assessmentModeMgr.isIpAllowed(ipList, "212.203.203.64");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void removeBusinessGroupFromRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6");
		BusinessGroup businessGroup1 = businessGroupService.createBusinessGroup(author, "as-mode-7", "", null, null, null, null, false, false, entry);
		BusinessGroup businessGroup2 = businessGroupService.createBusinessGroup(author, "as-mode-8", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup2, GroupRoles.participant.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.groups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup1 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup1);
		AssessmentModeToGroup modeToGroup2 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup2);
		mode.getGroups().add(modeToGroup1);
		mode.getGroups().add(modeToGroup2);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant 1
		List<AssessmentMode> currentModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(currentModes1);
		Assert.assertEquals(1, currentModes1.size());
		Assert.assertTrue(currentModes1.contains(mode));
		//check participant 2
		List<AssessmentMode> currentModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(currentModes2);
		Assert.assertEquals(1, currentModes2.size());
		Assert.assertTrue(currentModes2.contains(mode));
		
		//remove business group 1
		businessGroupRelationDao.deleteRelation(businessGroup1, entry);
		dbInstance.commitAndCloseSession();
		
		//check participant 1
		List<AssessmentMode> afterDeleteModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(afterDeleteModes1);
		Assert.assertEquals(0, afterDeleteModes1.size());
		//check participant 2
		List<AssessmentMode> afterDeleteModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(afterDeleteModes2);
		Assert.assertEquals(1, afterDeleteModes2.size());
		Assert.assertTrue(afterDeleteModes2.contains(mode));	
	}
	
	@Test
	public void deleteBusinessGroupFromRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-9");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-10");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-11");
		BusinessGroup businessGroup1 = businessGroupService.createBusinessGroup(author, "as-mode-12", "", null, null, null, null, false, false, entry);
		BusinessGroup businessGroup2 = businessGroupService.createBusinessGroup(author, "as-mode-13", "", null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup2, GroupRoles.participant.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.groups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup1 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup1);
		AssessmentModeToGroup modeToGroup2 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup2);
		mode.getGroups().add(modeToGroup1);
		mode.getGroups().add(modeToGroup2);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant 1
		List<AssessmentMode> currentModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(currentModes1);
		Assert.assertEquals(1, currentModes1.size());
		Assert.assertTrue(currentModes1.contains(mode));
		//check participant 2
		List<AssessmentMode> currentModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(currentModes2);
		Assert.assertEquals(1, currentModes2.size());
		Assert.assertTrue(currentModes2.contains(mode));
		
		//remove business group 1
		businessGroupService.deleteBusinessGroup(businessGroup2);
		dbInstance.commitAndCloseSession();

		//check participant 1
		List<AssessmentMode> afterDeleteModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(afterDeleteModes1);
		Assert.assertEquals(1, afterDeleteModes1.size());
		Assert.assertTrue(afterDeleteModes1.contains(mode));
		//check participant 2
		List<AssessmentMode> afterDeleteModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(afterDeleteModes2);
		Assert.assertEquals(0, afterDeleteModes2.size());
	}
	
	@Test
	public void deleteAreaFromRepositoryEntry() {
		//prepare the setup
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-14");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-15");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.groups);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		BusinessGroup businessGroupForArea = businessGroupService.createBusinessGroup(author, "as_mode_1", "", null, null, null, null, false, false, null);
		businessGroupRelationDao.addRole(participant, businessGroupForArea, GroupRoles.participant.name());
		BGArea area = areaMgr.createAndPersistBGArea("little area", "My little secret area", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroupForArea, area);
		dbInstance.commitAndCloseSession();
		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		mode = assessmentModeMgr.merge(mode, true);
		dbInstance.commitAndCloseSession();
		
		//check the participant modes
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//delete
		areaMgr.deleteBGArea(area);
		dbInstance.commitAndCloseSession();

		//check the participant modes after deleting the area
		List<AssessmentMode> afterDeleteModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(afterDeleteModes);
		Assert.assertEquals(0, afterDeleteModes.size());
	}
	
	@Test
	public void isSafelyAllowed() {
		String safeExamBrowserKey = "gdfkhjsduzezrutuzsf";
		String url = "http://localhost";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-requesthash", hash);
		request.setRequestURI("");
		
		boolean allowed = assessmentModeMgr.isSafelyAllowed(request, safeExamBrowserKey);
		Assert.assertTrue(allowed);
	}
	
	/**
	 * SEB 2.1 and SEB 2.2 use slightly different URLs to calculate
	 * the hash. The first use the raw URL, the second remove the
	 * trailing /.
	 */
	@Test
	public void isSafelyAllowed_seb22() {
		String safeExamBrowserKey = "a3fa755508fa1ed69de26840012fb397bb0a527b55ca35f299fa89cb4da232c6";
		String url = "http://kivik.frentix.com";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("kivik.frentix.com");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-requesthash", hash);
		request.setRequestURI("/");
		
		boolean allowed = assessmentModeMgr.isSafelyAllowed(request, safeExamBrowserKey);
		Assert.assertTrue(allowed);
	}
	
	@Test
	public void isSafelyAllowed_fail() {
		String safeExamBrowserKey = "gdfkhjsduzezrutuzsf";
		String url = "http://localhost";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-requesthash", hash);
		request.setRequestURI("/unauthorized/url");
		
		boolean allowed = assessmentModeMgr.isSafelyAllowed(request, safeExamBrowserKey);
		Assert.assertFalse(allowed);
	}
	
	@Test
	public void isSafelyAllowed_missingHeader() {
		String safeExamBrowserKey = "gdfkhjsduzezrutuzsf";

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.setRequestURI("/unauthorized/url");
		
		boolean allowed = assessmentModeMgr.isSafelyAllowed(request, safeExamBrowserKey);
		Assert.assertFalse(allowed);
	}

	/**
	 * Create a minimal assessment mode which start one hour before now
	 * and stop two hours after now.
	 * 
	 * @param entry
	 * @return
	 */
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
		mode.setManualBeginEnd(false);
		return mode;
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
}
