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
package org.olat.repository.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryQueriesTest extends OlatTestCase {
	
	private static final String TEST_RES_NAME = "TestManagerQuery";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager rm;
	@Autowired
	private RepositoryEntryQueries repositoryEntryQueries;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	private static Identity admin;
	private static Identity user1;
	private static Identity learnResourceManager1;
	private static Identity learnResourceManager2;
	
	private static final String author = "RepositoryManagerQueryAuthor";
	private static boolean initialized = false;
	
	public void testManagers() {
		assertNotNull(rm);
	}
	
	@Before
	public void setup() {
		if(initialized) return;

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Test repo query", "test-repo-query", null, defOrganisation, null);
		dbInstance.commit();
		
		admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("queryAdministrator");
		
		user1 = JunitTestHelper.createAndPersistIdentityAsAuthor(author + "1");
		user1.getUser().setProperty(UserConstants.FIRSTNAME, author + "1");
		user1.getUser().setProperty(UserConstants.LASTNAME, author + "1");
		userManager.updateUserFromIdentity(user1);

		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor(author + "2");
		id2.getUser().setProperty(UserConstants.FIRSTNAME, author + "2");
		id2.getUser().setProperty(UserConstants.LASTNAME, author + "2");
		userManager.updateUserFromIdentity(id2);

		learnResourceManager1 = JunitTestHelper.createAndPersistIdentityAsAuthor("kanu");
		learnResourceManager1.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Volks");
		learnResourceManager1.getUser().setProperty(UserConstants.FIRSTNAME, "Kanu");
		learnResourceManager1.getUser().setProperty(UserConstants.LASTNAME, "Unchou");
		userManager.updateUserFromIdentity(learnResourceManager1);

		learnResourceManager2 = JunitTestHelper.createAndPersistIdentityAsAuthor("rei");
		learnResourceManager2.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Nerv");
		learnResourceManager2.getUser().setProperty(UserConstants.FIRSTNAME, "Rei");
		learnResourceManager2.getUser().setProperty(UserConstants.LASTNAME, "Ayanami");
		userManager.updateUserFromIdentity(learnResourceManager2);

		dbInstance.commit();
		
		organisationService.addMember(organisation, learnResourceManager1, OrganisationRoles.learnresourcemanager);
		organisationService.addMember(organisation, learnResourceManager2, OrganisationRoles.learnresourcemanager);
		organisationService.addMember(organisation, user1, OrganisationRoles.user);
		organisationService.addMember(organisation, user1, OrganisationRoles.user);
		dbInstance.commitAndCloseSession();

		// generate some repo entries
		int numbRes = 500;
		for (int i = 0; i < numbRes; i++) {

			// create course and persist as OLATResourceImpl
			
			int rest = i%4;
			boolean guests = false;
			boolean allUsers = false;
			Identity owner = null;
			switch(rest) {
				case 0: {
					guests = true;
					allUsers = true;
					owner = user1;
					break;
				}
				case 1: {
					allUsers = true;
					owner = id2;
					break;
				}
				case 2: {
					owner = learnResourceManager1;
					break;
				}
				case 3: {
					owner = learnResourceManager2;
					break;
				}
			}

			RepositoryEntry re = createCourseRepositoryEntry(owner, i, organisation);
			re.setEntryStatus(RepositoryEntryStatusEnum.published);
			re.setAllUsers(allUsers);
			re.setGuests(guests);
			repositoryService.update(re);
			if (i % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}

		dbInstance.commitAndCloseSession();
		
		initialized = true;
	}
	
	@Test
	public void testOneShootQueryWithRoles() {
		//roles: admin
		Roles adminRoles = securityManager.getRoles(admin);
		Assert.assertTrue(adminRoles.isAdministrator());
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(admin, adminRoles, TEST_RES_NAME);
		List<RepositoryEntry> resultOneShootInstitut = repositoryEntryQueries.searchEntries(params, 0, -1, true);
		Assert.assertNotNull(resultOneShootInstitut);
		Assert.assertFalse(resultOneShootInstitut.isEmpty());
	}
	
	@Test
	public void testOneShootQueryWithAuthorRole() {
		//roles: author
		Roles authorRoles =  Roles.authorRoles();
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(user1, authorRoles, TEST_RES_NAME);
		List<RepositoryEntry> resultOneShoot = repositoryEntryQueries.searchEntries(params, 0, -1, true);
		Assert.assertNotNull(resultOneShoot);
		Assert.assertFalse(resultOneShoot.isEmpty());
	}
	
	@Test
	public void testOneShootWithInstitution() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager
		Roles learnResourceManager1Roles = securityManager.getRoles(learnResourceManager1);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types,
				learnResourceManager1, learnResourceManager1Roles);
		List<RepositoryEntry> resultOneShootInstitut3 = repositoryEntryQueries.searchEntries(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut3);
		assertFalse(resultOneShootInstitut3.isEmpty());
	}
	
	@Test
	public void testOneShootWithInstitutionAndSearchByAuthorName() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager search: authorname
		Roles learnResourceManager2Roles = securityManager.getRoles(learnResourceManager2);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, "kan", null, types,
				learnResourceManager2, learnResourceManager2Roles);
		List<RepositoryEntry> resultOneShootInstitut4 = repositoryEntryQueries.searchEntries(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut4);
		assertFalse(resultOneShootInstitut4.isEmpty());
	}
	
	@Test
	public void testOneShootQueryPaging() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager search: authorname
		Roles learnResourceManager1Role = securityManager.getRoles(learnResourceManager1);

		//test paging
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, "kan", null, types,
				learnResourceManager1, learnResourceManager1Role);
		List<RepositoryEntry> resultOneShootInstitut6 = repositoryEntryQueries.searchEntries(params, 0, 50, true);
		rm.countGenericANDQueryWithRolesRestriction(params);
		assertNotNull(resultOneShootInstitut6);
		assertEquals(50, resultOneShootInstitut6.size());
	}
	
	@Test
	public void search_byAuthor() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("author-re-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(Roles.administratorRoles());
		params.setIdentity(owner);
		params.setAuthor(owner.getName());
		List<RepositoryEntry> myEntries = repositoryEntryQueries.searchEntries(params, 0, -1, true);
		Assert.assertNotNull(myEntries);
		Assert.assertEquals(1, myEntries.size());
		Assert.assertTrue(myEntries.contains(re));
	}
	
	@Test
	public void genericANDQueryWithRoles_managed() {
		RepositoryEntry managedRe = JunitTestHelper.createAndPersistRepositoryEntry();
		managedRe.setManagedFlagsString("all");
		managedRe = dbInstance.getCurrentEntityManager().merge(managedRe);
		Identity learnResourceManager = JunitTestHelper.createAndPersistIdentityAsRndLearnResourceManager("repo-admin");
		RepositoryEntry freeRe = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		//search managed
		SearchRepositoryEntryParameters paramsManaged = new SearchRepositoryEntryParameters();
		paramsManaged.setRoles(Roles.administratorRoles());
		paramsManaged.setManaged(Boolean.TRUE);
		paramsManaged.setIdentity(learnResourceManager);
		List<RepositoryEntry> managedEntries = repositoryEntryQueries.searchEntries(paramsManaged, 0, -1, true);
		Assert.assertNotNull(managedEntries);
		Assert.assertTrue(managedEntries.size() > 0);
		Assert.assertTrue(managedEntries.contains(managedRe));
		Assert.assertFalse(managedEntries.contains(freeRe));

		//search unmanaged
		SearchRepositoryEntryParameters paramsFree = new SearchRepositoryEntryParameters();
		paramsFree.setRoles(Roles.administratorRoles());
		paramsFree.setManaged(Boolean.FALSE);
		paramsFree.setIdentity(learnResourceManager);
		List<RepositoryEntry> freeEntries = repositoryEntryQueries.searchEntries(paramsFree, 0, -1, true);
		Assert.assertNotNull(freeEntries);
		Assert.assertTrue(freeEntries.size() > 0);
		Assert.assertFalse(freeEntries.contains(managedRe));
		Assert.assertTrue(freeEntries.contains(freeRe));
	}
	
	@Test
	public void genericANDQueryWithRolesWithStandardUser() {
		//create 2 identities (repo owner and tutor)
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-gen-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-gen-2");
		
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id1, re2, GroupRoles.participant.name());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "teacherg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		
		//check for guest (negative test)
		SearchRepositoryEntryParameters params1 = new SearchRepositoryEntryParameters();
		params1.setRoles(Roles.guestRoles());
		List<RepositoryEntry> entries1 = repositoryEntryQueries.searchEntries(params1, 0, -1, true);
		Assert.assertNotNull(entries1);
		Assert.assertFalse(entries1.contains(re1));
		Assert.assertFalse(entries1.contains(re2));
		for(RepositoryEntry entry:entries1) {
			Assert.assertTrue(entry.isAllUsers());
		}
		
		//check for identity 1 (participant re2 + re1 accessible to all users)
		SearchRepositoryEntryParameters params2 = new SearchRepositoryEntryParameters();
		params2.setIdentity(id1);
		params2.setRoles(Roles.userRoles());
		List<RepositoryEntry> entries2 = repositoryEntryQueries.searchEntries(params2, 0, -1, true);
		Assert.assertNotNull(entries2);
		Assert.assertFalse(entries2.isEmpty());
		Assert.assertTrue(entries2.contains(re1));
		Assert.assertTrue(entries2.contains(re2));
		for(RepositoryEntry entry:entries2) {
			if(!entry.equals(re2)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
		
		//check for identity 1 (re1 accessible to all users)
		SearchRepositoryEntryParameters params3 = new SearchRepositoryEntryParameters();
		params3.setIdentity(id2);
		params3.setRoles(Roles.userRoles());
		List<RepositoryEntry> entries3 = repositoryEntryQueries.searchEntries(params3, 0, -1, true);
		Assert.assertNotNull(entries3);
		Assert.assertFalse(entries3.isEmpty());
		Assert.assertTrue(entries3.contains(re1));
		Assert.assertFalse(entries3.contains(re2));
		for(RepositoryEntry entry:entries3) {
			Assert.assertTrue(entry.isAllUsers());
			Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
		}
	}
	
	@Test
	public void genericANDQueryWithRolesRestrictionMembersOnly() {
		//create 2 identities (repo owner and tutor)
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-gen-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-gen-2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-gen-3-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "teacherg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id2, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		
		//check for id 1 (owner of the repository entry)
		SearchRepositoryEntryParameters params1 = new SearchRepositoryEntryParameters();
		params1.setIdentity(id1);
		params1.setRoles(Roles.userRoles());
		params1.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries1 = repositoryEntryQueries.searchEntries(params1, 0, -1, true);
		Assert.assertNotNull(entries1);
		Assert.assertFalse(entries1.isEmpty());
		Assert.assertTrue(entries1.contains(re));
		for(RepositoryEntry entry:entries1) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
		
		//check for id2 (tutor)
		SearchRepositoryEntryParameters params2 = new SearchRepositoryEntryParameters();
		params2.setIdentity(id2);
		params2.setRoles(Roles.userRoles());
		params2.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries2 = repositoryEntryQueries.searchEntries(params2, 0, -1, true);
		Assert.assertNotNull(entries2);
		Assert.assertFalse(entries2.isEmpty());
		Assert.assertTrue(entries2.contains(re));
		for(RepositoryEntry entry:entries2) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
		
		//check for id3 (negative test)
		SearchRepositoryEntryParameters params3 = new SearchRepositoryEntryParameters();
		params3.setIdentity(id3);
		params3.setRoles(Roles.userRoles());
		params3.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries3 = repositoryEntryQueries.searchEntries(params3, 0, -1, true);
		Assert.assertNotNull(entries3);
		Assert.assertFalse(entries3.contains(re));
		for(RepositoryEntry entry:entries3) {
			Assert.assertTrue(entry.isAllUsers());
			Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
		}
	}
	
	@Test
	public void queryByTypeLimitAccess() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("qbtla-1-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "qbtla-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(id, Roles.userRoles(), re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryEntryQueries.searchEntries(params, 0, -1, false);
		
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		for(RepositoryEntry entry:entries) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	@Test
	public void queryByTypeLimitAccess_withoutInstitution() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("qbtla-2-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "qbtla-2", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(id, Roles.userRoles(), re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryEntryQueries.searchEntries(params, 0, -1, false);
		
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		for(RepositoryEntry entry:entries) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	@Test
	public void queryByTypeLimitAccess_withInstitution() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("qbtla-3-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "qbtla-3", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		//promote id to institution resource manager
		id.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "openolat.org");
		userManager.updateUserFromIdentity(id);
		organisationService.addMember(id, OrganisationRoles.learnresourcemanager);
		dbInstance.commitAndCloseSession();
		
		//check
		Roles learnResourceManagerRoles = Roles.learnResourceManagerRoles();
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(id, learnResourceManagerRoles, re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryEntryQueries.searchEntries(params, 0, -1, false);
		
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		for(RepositoryEntry entry:entries) {
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	private RepositoryEntry createCourseRepositoryEntry(Identity owner, final int i, Organisation organisation) {
		OLATResource r =  resourceManager.createOLATResourceInstance(TEST_RES_NAME);
		resourceManager.saveOLATResource(r);
		// now make a repository entry for this course
		RepositoryEntry re = repositoryService.create(owner, null, "Lernen mit OLAT " + i, "JunitTest_RepositoryEntry_" + i,
				"Description of learning by OLAT " + i, r, RepositoryEntryStatusEnum.preparation, organisation);
		return re;
	}
}
