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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 23.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	@Test
	public void assessmentTool_coach() {
		//course
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-1");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-2");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-3");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-4");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-1");

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-2", "assessment-tool-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		ae1.setScore(BigDecimal.valueOf(3.0));
		ae1.setPassed(Boolean.FALSE);
		assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		ae2.setScore(BigDecimal.valueOf(5.0));
		ae2.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, refEntry);
		ae3.setScore(BigDecimal.valueOf(8.0));
		ae3.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, refEntry);
		ae4.setScore(BigDecimal.valueOf(9.0));
		ae4.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae4);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// coach of group 1 with id 1 and id2
		List<BusinessGroup> coachedGroups = Collections.singletonList(group1);
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(false, false, false, true, false, coachedGroups);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);

		// statistics
		AssessmentStatistics statistics = assessmentToolManager.getStatistics(coach, params);
		Assert.assertEquals(4.0d, statistics.getAverageScore().doubleValue(), 0.0001);
		Assert.assertEquals(1, statistics.getCountFailed());
		Assert.assertEquals(1, statistics.getCountPassed());

		//check assessed identities list
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(coach, params);
		Assert.assertNotNull(assessedIdentities);
		Assert.assertEquals(2, assessedIdentities.size());
		
		//number of assessed identities
		int numOfAssessedIdentities = assessmentToolManager.getNumberOfAssessedIdentities(coach, params);
		Assert.assertEquals(2, numOfAssessedIdentities);
		
		//check only the queries
		AssessmentMembersStatistics participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params);
		Assert.assertNotNull(participantStatistics);

		List<IdentityShort> assessedShortIdentities = assessmentToolManager.getShortAssessedIdentities(coach, params, 120);
		Assert.assertNotNull(assessedShortIdentities);
		Assert.assertEquals(2, assessedShortIdentities.size());
		
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, AssessmentEntryStatus.notStarted);
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(0, assessmentEntries.size());
		
		// separate check with more options in the search parameters
		// add by group key 
		params.setBusinessGroupKeys(Collections.singletonList(group1.getKey()));
		
		// assessed groups
		List<AssessedBusinessGroup> assessedGroups = assessmentToolManager.getBusinessGroupStatistics(coach, params);
		Assert.assertNotNull(assessedGroups);
		Assert.assertEquals(1, assessedGroups.size());

		//check assessed identities list
		List<Identity> assessedIdentitiesAlt = assessmentToolManager.getAssessedIdentities(coach, params);
		Assert.assertNotNull(assessedIdentitiesAlt);
		Assert.assertEquals(2, assessedIdentitiesAlt.size());
		
		//number of assessed identities
		int numOfAssessedIdentitiesAlt = assessmentToolManager.getNumberOfAssessedIdentities(coach, params);
		Assert.assertEquals(2, numOfAssessedIdentitiesAlt);

		List<IdentityShort> assessedShortIdentitiesAlt = assessmentToolManager.getShortAssessedIdentities(coach, params, 120);
		Assert.assertNotNull(assessedShortIdentitiesAlt);
		Assert.assertEquals(2, assessedShortIdentitiesAlt.size());
		
		List<AssessmentEntry> assessmentEntriesAlt = assessmentToolManager.getAssessmentEntries(coach, params, AssessmentEntryStatus.notStarted);
		Assert.assertNotNull(assessmentEntriesAlt);
		Assert.assertEquals(0, assessmentEntriesAlt.size());
	}
	
	@Test
	public void assessmentTool_admin() {
		//course
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("ast-admin-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-5");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-6");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-7");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-8");
		Identity assessedExtIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-9");
		Identity assessedExtIdentity6 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-10");
		Identity assessedExtIdentity7 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-11");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-9");

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-2", "assessment-tool-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		ae1.setScore(BigDecimal.valueOf(3.0));
		ae1.setPassed(Boolean.FALSE);
		assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		ae2.setScore(BigDecimal.valueOf(5.0));
		ae2.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, refEntry);
		ae3.setScore(BigDecimal.valueOf(8.0));
		ae3.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, refEntry);
		ae4.setScore(BigDecimal.valueOf(9.0));
		ae4.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae4);
		AssessmentEntry ae5 = assessmentEntryDao.createAssessmentEntry(assessedExtIdentity5, null, entry, subIdent, null, refEntry);
		ae5.setScore(BigDecimal.valueOf(3.0));
		ae5.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae5);
		AssessmentEntry ae6 = assessmentEntryDao.createAssessmentEntry(assessedExtIdentity6, null, entry, subIdent, null, refEntry);
		ae6.setScore(BigDecimal.valueOf(4.0));
		ae6.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae6);
		AssessmentEntry ae7 = assessmentEntryDao.createAssessmentEntry(assessedExtIdentity7, null, entry, subIdent, null, refEntry);
		ae7.setScore(BigDecimal.valueOf(5.0));
		ae7.setPassed(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(ae7);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// administrator with full access
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, true, true, true, true, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);

		//check assessed identities list
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertNotNull(assessedIdentities);
		Assert.assertEquals(7, assessedIdentities.size());
		
		List<IdentityShort> assessedShortIdentities = assessmentToolManager.getShortAssessedIdentities(admin, params, 120);
		Assert.assertNotNull(assessedShortIdentities);
		Assert.assertEquals(7, assessedShortIdentities.size());
		
		//number of assessed identities
		int numOfAssessedIdentities = assessmentToolManager.getNumberOfAssessedIdentities(admin, params);
		Assert.assertEquals(7, numOfAssessedIdentities);
		
		// statistics
		AssessmentStatistics statistics = assessmentToolManager.getStatistics(admin, params);
		Assert.assertEquals(5.28571d, statistics.getAverageScore().doubleValue(), 0.0001);
		Assert.assertEquals(1, statistics.getCountFailed());
		Assert.assertEquals(6, statistics.getCountPassed());
		
		//check only the queries as the statistics need the course infos
		AssessmentMembersStatistics participantStatistics = assessmentToolManager.getNumberOfParticipants(admin, params);
		Assert.assertNotNull(participantStatistics);
	}
	
	@Test
	public void getNumberOfParticipants() {
		//course
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("ast-admin-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-5");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-6");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-7");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-8");
		Identity assessedExtIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-9");
		Identity assessedExtIdentity6 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-10");
		Identity assessedExtIdentity7 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-11");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-9");

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry, GroupRoles.participant.name());
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedExtIdentity5, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedExtIdentity6, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedExtIdentity7, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// the course infos need to calculate the number of participants
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity1);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity2);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity3);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity4);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity5);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity6);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity7);
		dbInstance.commitAndCloseSession();
		
		// statistics as admin
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, true, true, true, true, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);

		AssessmentMembersStatistics statisticsAsAdmin = assessmentToolManager.getNumberOfParticipants(admin, params);
		Assert.assertNotNull(statisticsAsAdmin);
		Assert.assertEquals(3, statisticsAsAdmin.getNumOfOtherUsers());
		Assert.assertEquals(4, statisticsAsAdmin.getNumOfParticipants());
		Assert.assertEquals(3, statisticsAsAdmin.getLoggedIn());
		Assert.assertEquals(4, statisticsAsAdmin.getNumOfParticipantsLoggedIn());
	}


}
