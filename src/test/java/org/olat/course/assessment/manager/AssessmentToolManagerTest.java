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
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-2", "assessment-tool-bg-2-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 5.0f, Boolean.TRUE, null, null);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, refEntry, 8.0f, Boolean.TRUE, null, null);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, refEntry, 9.0f, Boolean.TRUE, null, null);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, refEntry);
		dbInstance.commitAndCloseSession();
		
		// coach of group 1 with id 1 and id2
		List<BusinessGroup> coachedGroups = Collections.singletonList(group1);
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(false, false, false, true, coachedGroups);
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
		int numOfInitialLanches = assessmentToolManager.getNumberOfInitialLaunches(coach, params);
		Assert.assertEquals(0, numOfInitialLanches);//not launched, only simulated

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
}
