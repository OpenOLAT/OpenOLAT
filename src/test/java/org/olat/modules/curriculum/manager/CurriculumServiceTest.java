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
package org.olat.modules.curriculum.manager;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void addCurriculumManagers() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", null);
		dbInstance.commitAndCloseSession();
		
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		// check if we can retrieve the managers
		List<Identity> managers = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.curriculummanager);
		Assert.assertNotNull(managers);
		Assert.assertEquals(1, managers.size());
		Assert.assertEquals(manager, managers.get(0));
		
		// check that there is not an other member with an other role
		List<Identity> owners = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.owner);
		Assert.assertTrue(owners.isEmpty());
	}
	
	@Test
	public void getCurriculumElements() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-2", "Curriculum 2", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry publishedEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry reviewedEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		
		publishedEntry = repositoryManager.setAccess(publishedEntry, RepositoryEntryStatusEnum.published, false, false);
		reviewedEntry = repositoryManager.setAccess(reviewedEntry, RepositoryEntryStatusEnum.review, false, false);
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element, publishedEntry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		List<CurriculumRef> curriculumList = Collections.singletonList(curriculum);
		List<CurriculumElementRepositoryEntryViews> myElements = curriculumService.getCurriculumElements(participant, Roles.userRoles(), curriculumList);
		Assert.assertNotNull(myElements);
		Assert.assertEquals(1, myElements.size());
		
		CurriculumElementRepositoryEntryViews myElement = myElements.get(0);
		Assert.assertEquals(element, myElement.getCurriculumElement());
		Assert.assertEquals(1, myElement.getEntries().size());
		Assert.assertEquals(publishedEntry.getKey(), myElement.getEntries().get(0).getKey());
		
		CurriculumElementMembership membership = myElement.getCurriculumMembership();
		Assert.assertTrue(membership.isParticipant());
		Assert.assertFalse(membership.isCoach());
		Assert.assertFalse(membership.isRepositoryEntryOwner());
		Assert.assertFalse(membership.isCurriculumElementOwner());
	}
	
	@Test
	public void deleteCurriculum() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-3", "Curriculum 3", "Curriculum", null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-del", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element2, entry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element1, participant, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		List<CurriculumRef> curriculumList = Collections.singletonList(curriculum);
		List<CurriculumElementRepositoryEntryViews> myElements = curriculumService.getCurriculumElements(participant, Roles.userRoles(), curriculumList);
		Assert.assertNotNull(myElements);
		Assert.assertEquals(2, myElements.size());
		
		curriculumService.deleteCurriculum(curriculum);
		dbInstance.commitAndCloseSession();
		
		// check
		Curriculum deletedCurriculum = curriculumService.getCurriculum(curriculum);
		Assert.assertNull(deletedCurriculum);
	}
	
	@Test
	public void deleteCurriculumInQuality() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-3", "Curriculum 3", "Curriculum", null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-del", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Organisation organisation = organisationService.getDefaultOrganisation();
		QualityDataCollection dataCollection = qualityService.createDataCollection(Collections.singletonList(organisation), entry);
		dataCollection.setTopicCurriculumElement(element1);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element2, entry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element1, participant, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		List<CurriculumRef> curriculumList = Collections.singletonList(curriculum);
		List<CurriculumElementRepositoryEntryViews> myElements = curriculumService.getCurriculumElements(participant, Roles.userRoles(), curriculumList);
		Assert.assertNotNull(myElements);
		Assert.assertEquals(2, myElements.size());
		
		curriculumService.deleteCurriculum(curriculum);
		dbInstance.commitAndCloseSession();
		
		// check
		Curriculum deletedCurriculum = curriculumService.getCurriculum(curriculum);
		Assert.assertNotNull(deletedCurriculum);
		Assert.assertEquals(CurriculumStatus.deleted.name(), deletedCurriculum.getStatus());
	}
}
