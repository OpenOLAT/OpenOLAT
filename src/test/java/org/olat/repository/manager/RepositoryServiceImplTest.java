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

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
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
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.CatalogEntry;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryServiceImplTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryServiceImpl repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createRepositoryEntry() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-1");
		
		String displayName = "ServiceTest";
		String resourceName = "ServiceTest";
		String description = "Test the brand new service";
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, resourceName, displayName, description, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getCreationDate());
		Assert.assertNotNull(re.getLastModified());
		Assert.assertNotNull(re.getOlatResource());
	}
	
	@Test
	public void createAndLoadRepositoryEntry() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-1");
		
		String displayName = "Service test 2";
		String resourceName = "ServiceTest";
		String description = "Test the brand new service";
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, resourceName, displayName, description, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		RepositoryEntry loadedEntry = repositoryService.loadByKey(re.getKey());
		Assert.assertNotNull(loadedEntry);
		Assert.assertNotNull(re.getCreationDate());
		Assert.assertNotNull(re.getLastModified());
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertNotNull(loadedEntry.getGroups());
		Assert.assertEquals(2, loadedEntry.getGroups().size());// default group + default organization group
		//saved?
		Assert.assertEquals(displayName, re.getDisplayname());
		Assert.assertEquals(resourceName, re.getResourcename());
		Assert.assertEquals(description, re.getDescription());
		//default value
		Assert.assertFalse(re.getCanCopy());
		Assert.assertFalse(re.getCanDownload());
		Assert.assertFalse(re.getCanReference());
		Assert.assertEquals(RepositoryEntryStatusEnum.trash, re.getEntryStatus());
	}
	
	@Test
	public void deleteCourseSoftly() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("re-soft-");
		Identity coachGroup = JunitTestHelper.createAndPersistIdentityAsRndUser("re-soft-");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("re-soft-");
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-del-1");
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(initialAuthor);
		dbInstance.commitAndCloseSession();
		
		//add business group
		BusinessGroup group = businessGroupService.createBusinessGroup(coachGroup, "Relation 1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupService.addResourceTo(group, re);
	    dbInstance.commit();
	    
		// catalog
	    List<CatalogEntry> rootEntries = catalogManager.getRootCatalogEntries();
	    CatalogEntry catEntry = catalogManager.createCatalogEntry();
	    catEntry.setName("Soft");
	    catEntry.setRepositoryEntry(re);
	    catEntry.setParent(rootEntries.get(0));
	    catalogManager.addCatalogEntry(rootEntries.get(0), catEntry);
	    dbInstance.commit();
	    
	    //check the catalog
	    List<CatalogEntry> catEntries = catalogManager.getCatalogCategoriesFor(re);
	    Assert.assertNotNull(catEntries);
		Assert.assertEquals(1, catEntries.size());
	    
		// add owner, coach...
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, re, GroupRoles.participant.name());
	    dbInstance.commit();
		
		// kill it softly like A. Keys
		repositoryService.deleteSoftly(re, initialAuthor, false, false);
		dbInstance.commit();

		//check that the members are removed
		List<Identity> coachAndParticipants = repositoryEntryRelationDao
				.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertNotNull(coachAndParticipants);
		Assert.assertEquals(0, coachAndParticipants.size());
		
		//check the relations between course and business groups
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, re, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(0, groups.size());
		
		//check the catalog
	    List<CatalogEntry> removedCatEntries = catalogManager.getCatalogCategoriesFor(re);
	    Assert.assertNotNull(removedCatEntries);
		Assert.assertEquals(0, removedCatEntries.size());
		
		RepositoryEntry reloadEntry = repositoryService.loadByKey(re.getKey());
		Assert.assertNotNull(reloadEntry);
		Assert.assertEquals(RepositoryEntryStatusEnum.trash, reloadEntry.getEntryStatus());
		Assert.assertNotNull(reloadEntry.getDeletionDate());
		Assert.assertEquals(initialAuthor, reloadEntry.getDeletedBy());
	}

	@Test
	public void deleteCoursePermanently() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-del-1");
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(initialAuthor);
		dbInstance.commitAndCloseSession();
		
		Roles roles = Roles.authorRoles();
		ErrorList errors = repositoryService.deletePermanently(re, initialAuthor, roles, Locale.ENGLISH);
		Assert.assertNotNull(errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * How can be a resource manager if Constants.ORESOURCE_USERMANAGER is never used?
	 */
	@Test
	public void isInstitutionalRessourceManagerFor() {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsRndUser("instit-1");
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsRndUser("instit-2");
		Identity part3 = JunitTestHelper.createAndPersistIdentityAsRndUser("instit-3");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(owner1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(owner2, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(part3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		//set the institutions
		owner1.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "volks");
		owner2.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "volks");
		part3.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "volks");
		userManager.updateUserFromIdentity(owner1);
		userManager.updateUserFromIdentity(owner2);
		userManager.updateUserFromIdentity(part3);
		dbInstance.commit();
		
		//promote owner1 to institution resource manager
		organisationService.addMember(owner1, OrganisationRoles.learnresourcemanager);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean institutionMgr1 = repositoryService.hasRoleExpanded(owner1, re, OrganisationRoles.learnresourcemanager.name());
		boolean institutionMgr2 = repositoryService.hasRoleExpanded(owner2, re, OrganisationRoles.learnresourcemanager.name());
		boolean institutionMgr3 = repositoryService.hasRoleExpanded(part3, re, OrganisationRoles.learnresourcemanager.name());
	
		Assert.assertTrue(institutionMgr1);
		Assert.assertFalse(institutionMgr2);
		Assert.assertFalse(institutionMgr3);
	}
	
	@Test
	public void isOwnerOfRepositoryEntry() {
		//create a repository entry with an owner and a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("re-owner-1-is");
		Identity part = JunitTestHelper.createAndPersistIdentityAsRndUser("re-owner-2-is");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(part, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check
		boolean isOwnerOwner = repositoryService.hasRoleExpanded(owner, re, GroupRoles.owner.name());
		Assert.assertTrue(isOwnerOwner);
		boolean isPartOwner = repositoryService.hasRoleExpanded(part, re, GroupRoles.owner.name());
		Assert.assertFalse(isPartOwner);
	}
	
	

}
