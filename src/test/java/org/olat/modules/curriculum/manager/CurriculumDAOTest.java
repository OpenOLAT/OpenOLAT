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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createCurriculum() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-1", "", null, null);
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-1", "Curriculum 1", "Short desc.", false, organisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(curriculum);
		Assert.assertNotNull(curriculum.getKey());
		Assert.assertNotNull(curriculum.getCreationDate());
		Assert.assertNotNull(curriculum.getLastModified());
		Assert.assertEquals("Curriculum 1", curriculum.getDisplayName());
		Assert.assertEquals("CUR-1", curriculum.getIdentifier());
		Assert.assertEquals("Short desc.", curriculum.getDescription());
		Assert.assertEquals(organisation, curriculum.getOrganisation());
	}
	
	@Test
	public void loadByKey() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-2", "", null, null);
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-2", "Curriculum 2", "Short desc.", false, organisation);
		dbInstance.commitAndCloseSession();
		
		Curriculum reloadedCurriculum = curriculumDao.loadByKey(curriculum.getKey());
		Assert.assertNotNull(reloadedCurriculum);
		Assert.assertEquals(curriculum, reloadedCurriculum);
		Assert.assertNotNull(reloadedCurriculum.getCreationDate());
		Assert.assertNotNull(reloadedCurriculum.getLastModified());
		Assert.assertEquals("Curriculum 2", reloadedCurriculum.getDisplayName());
		Assert.assertEquals("CUR-2", reloadedCurriculum.getIdentifier());
		Assert.assertEquals("Short desc.", reloadedCurriculum.getDescription());
		Assert.assertEquals(organisation, reloadedCurriculum.getOrganisation());
	}
	
	@Test
	public void loadByKeys() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-2.1", "", null, null);
		Curriculum curriculum1 = curriculumDao.createAndPersist("CUR-2.1.1", "Curriculum 2.1.1", "Short desc.", false, organisation);
		Curriculum curriculum2 = curriculumDao.createAndPersist("CUR-2.1.2", "Curriculum 2.1.2", "Short desc.", false, organisation);
		Curriculum curriculum3 = curriculumDao.createAndPersist("CUR-2.1.3", "Curriculum 2.1.3", "Short desc.", false, organisation);
		dbInstance.commitAndCloseSession();
		
		List<Curriculum> curriculums = curriculumDao.loadByKeys(Arrays.asList(curriculum1, curriculum2));
		
		Assert.assertTrue(curriculums.contains(curriculum1));
		Assert.assertTrue(curriculums.contains(curriculum2));
		Assert.assertTrue(!curriculums.contains(curriculum3));
	}
	
	@Test
	public void search_all() {
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-3", "Curriculum 3", "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		List<Curriculum> curriculums = curriculumDao.search(params);
		Assert.assertNotNull(curriculums);
		Assert.assertFalse(curriculums.isEmpty());
		Assert.assertTrue(curriculums.contains(curriculum));
	}
	
	@Test
	public void search_org() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-4", "", null, null);
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-4", "Curriculum 4", "Short desc.", false, organisation);
		dbInstance.commitAndCloseSession();
		
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setOrganisations(Collections.singletonList(organisation));
		List<Curriculum> curriculums = curriculumDao.search(params);
		dbInstance.commitAndCloseSession();
		
		//check the result
		Assert.assertNotNull(curriculums);
		Assert.assertEquals(1, curriculums.size());
		Assert.assertTrue(curriculums.contains(curriculum));
		//check the fetch
		Assert.assertEquals(organisation, curriculums.get(0).getOrganisation());
		Assert.assertNotNull(((CurriculumImpl)curriculums.get(0)).getGroup().getKey());
	}
	
	@Test
	public void search_searchString() {
		String identifier = UUID.randomUUID().toString();
		String displayName = UUID.randomUUID().toString();
		Curriculum curriculum = curriculumDao.createAndPersist(identifier, displayName, "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		
		// search something which doesn't exists
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setSearchString("AAAAAAAA");
		List<Curriculum> curriculums = curriculumDao.search(params);
		Assert.assertTrue(curriculums.isEmpty());
		
		// search by identifier
		params.setSearchString(identifier);
		List<Curriculum> curriculumById = curriculumDao.search(params);
		Assert.assertEquals(1, curriculumById.size());
		Assert.assertEquals(curriculum, curriculumById.get(0));
		
		// search by identifier
		params.setSearchString(displayName.substring(0, 25).toUpperCase());
		List<Curriculum> curriculumByName = curriculumDao.search(params);
		Assert.assertTrue(curriculumByName.contains(curriculum));
		
		// search by primary key
		params.setSearchString(curriculum.getKey().toString());
		List<Curriculum> curriculumByKey = curriculumDao.search(params);
		Assert.assertTrue(curriculumByKey.contains(curriculum));
	}
	
	@Test
	public void search_manager() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-user-1");
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumDao.createAndPersist("Curriculum for managers", "Managers", "Short desc.", false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		curriculumService.addMember(curriculum, user, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		// search curriculum for the manager
		CurriculumSearchParameters managerParams = new CurriculumSearchParameters();
		managerParams.setCurriculumAdmin(manager);
		List<Curriculum> managedCurriculums = curriculumDao.search(managerParams);
		Assert.assertEquals(1, managedCurriculums.size());
		Assert.assertEquals(curriculum, managedCurriculums.get(0));
		
		
		// search curriculum for the user
		CurriculumSearchParameters userParams = new CurriculumSearchParameters();
		userParams.setCurriculumAdmin(user);
		List<Curriculum> userCurriculums = curriculumDao.search(userParams);
		Assert.assertTrue(userCurriculums.isEmpty());
	}
	
	@Test
	public void searchWithInfos_manager() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-user-1");
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumDao.createAndPersist("Curriculum for managers", "Managers", "Short desc.", false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		curriculumService.addMember(curriculum, user, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		// search curriculum for the manager
		CurriculumSearchParameters managerParams = new CurriculumSearchParameters();
		managerParams.setCurriculumAdmin(manager);
		List<CurriculumInfos> managedCurriculums = curriculumDao.searchWithInfos(managerParams);
		Assert.assertEquals(1, managedCurriculums.size());
		Assert.assertEquals(curriculum, managedCurriculums.get(0).getCurriculum());
		
		// search curriculum for the user
		CurriculumSearchParameters userParams = new CurriculumSearchParameters();
		userParams.setCurriculumAdmin(user);
		List<CurriculumInfos> userCurriculums = curriculumDao.searchWithInfos(userParams);
		Assert.assertTrue(userCurriculums.isEmpty());
	}
	
	@Test
	public void searchWithInfos_repoOwner() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-user-1");
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-course-owner-1");
		Curriculum curriculum = curriculumDao.createAndPersist("Curriculum for owners", "Owners", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(owner);
		curriculumService.addRepositoryEntry(element, entry, true);
		dbInstance.commit();
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		curriculumService.addMember(curriculum, user, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		// search curriculum for the owner of a repository entry in curriculum
		CurriculumSearchParameters managerParams = new CurriculumSearchParameters();
		managerParams.setElementOwner(owner);
		List<CurriculumInfos> managedCurriculums = curriculumDao.searchWithInfos(managerParams);
		Assert.assertEquals(1, managedCurriculums.size());
		Assert.assertEquals(curriculum, managedCurriculums.get(0).getCurriculum());
		
		// search curriculum for the user
		CurriculumSearchParameters userParams = new CurriculumSearchParameters();
		userParams.setCurriculumAdmin(user);
		List<CurriculumInfos> userCurriculums = curriculumDao.searchWithInfos(userParams);
		Assert.assertTrue(userCurriculums.isEmpty());
	}
	
	@Test
	public void searchWithInfos_elementOwner() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-course-owner-1");
		Curriculum curriculum = curriculumDao.createAndPersist("Curriculum for owners", "Owners", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(owner);
		curriculumService.addRepositoryEntry(element, entry, true);
		dbInstance.commit();
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		curriculumService.addMember(element, owner, CurriculumRoles.curriculumelementowner);
		dbInstance.commitAndCloseSession();
		
		// search curriculum for the owner of a part of the curriculum
		CurriculumSearchParameters managerParams = new CurriculumSearchParameters();
		managerParams.setElementOwner(owner);
		List<CurriculumInfos> managedCurriculums = curriculumDao.searchWithInfos(managerParams);
		Assert.assertEquals(1, managedCurriculums.size());
		Assert.assertEquals(curriculum, managedCurriculums.get(0).getCurriculum());
	}
	
	@Test
	public void getMembersIdentity() {
		// add a curriculum manager
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		// get curriculum manager
		List<Identity> managers = curriculumDao.getMembersIdentity(curriculum, CurriculumRoles.curriculummanager.name());
		Assert.assertNotNull(managers);
		Assert.assertEquals(1, managers.size());
		Assert.assertEquals(manager, managers.get(0));
		
		// get coaches, but there is no coaches
		List<Identity> coaches = curriculumDao.getMembersIdentity(curriculum, CurriculumRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertTrue(coaches.isEmpty());
	}
	
	@Test
	public void getMyCurriculums() {
		// add a curriculum with a coach in an element
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-MY-1", "My Curriculum 1", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		curriculumService.addMember(element, id, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		// get my curriculums
		List<Curriculum> myCurriculums = curriculumDao.getMyCurriculums(id);
		Assert.assertNotNull(myCurriculums);
		Assert.assertEquals(1, myCurriculums.size());
		Assert.assertEquals(curriculum, myCurriculums.get(0));
	}
	
	@Test
	public void hasMyCurriculums() {
		// add a curriculum with a coach in an element
		Identity idWithCurriculum = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-for-fun-1");
		Identity idWithout = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-for-fun-2");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-FUN-1", "My funny curriculum 1", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		curriculumService.addMember(element, idWithCurriculum, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		// has curriculums?
		boolean hasCurriculums = curriculumDao.hasMyCurriculums(idWithCurriculum);
		Assert.assertTrue(hasCurriculums);
		boolean hasNotCurriculums = curriculumDao.hasMyCurriculums(idWithout);
		Assert.assertFalse(hasNotCurriculums);
	}
	
	@Test
	public void hasCurriculumRole() {
		// add a curriculum manager
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		// is curriculum manager
		boolean isCurriculumManager = curriculumDao.hasCurriculumRole(manager, CurriculumRoles.curriculummanager.name());
		Assert.assertTrue(isCurriculumManager);
		boolean isAdministrator = curriculumDao.hasCurriculumRole(manager, OrganisationRoles.administrator.name());
		Assert.assertFalse(isAdministrator);
	}
	
	@Test
	public void hasOwnerRoleInCurriculum() {
		// make a curriculum with an element with a course
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-owner-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(owner);
		curriculumService.addRepositoryEntry(element, entry, true);
		dbInstance.commit();

		// is owner within a curriculum
		boolean isOwner = curriculumDao.hasOwnerRoleInCurriculumElement(owner);
		Assert.assertTrue(isOwner);
	}
	
	@Test
	public void hasRoleExpanded() {
		// add a curriculum manager
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		// is curriculum manager
		boolean isCurriculumManager = curriculumDao.hasRoleExpanded(curriculum, manager, CurriculumRoles.curriculummanager.name());
		Assert.assertTrue(isCurriculumManager);
		boolean isAdministrator = curriculumDao.hasRoleExpanded(curriculum, manager, OrganisationRoles.administrator.name());
		Assert.assertFalse(isAdministrator);
	}
	
	@Test
	public void hasRoleExpanded_organisation() {
		// add a curriculum manager
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndAdmin("admin-cur-manager-1");
		Organisation organisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, organisation);
		dbInstance.commitAndCloseSession();
		
		// is curriculum manager
		boolean isCurriculumManager = curriculumDao.hasRoleExpanded(curriculum, manager, CurriculumRoles.curriculummanager.name());
		Assert.assertFalse(isCurriculumManager);
		boolean isAdministrator = curriculumDao.hasRoleExpanded(curriculum, manager, OrganisationRoles.administrator.name());
		Assert.assertTrue(isAdministrator);
	}
	
}
