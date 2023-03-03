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
package org.olat.modules.invitation.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.modules.invitation.model.InvitationWithBusinessGroup;
import org.olat.modules.invitation.model.InvitationWithProject;
import org.olat.modules.invitation.model.InvitationWithRepositoryEntry;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ProjectService projectService;
	
	@Test
	public void createAndPersistInvitation() {
		Invitation invitation = createDummyInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commit();
		
		Assert.assertNotNull(invitation);
		Assert.assertNotNull(invitation.getKey());
		Assert.assertNotNull(invitation.getBaseGroup());
		Assert.assertNotNull(invitation.getToken());
	}
	
	@Test
	public void findInvitationByToken() {
		Invitation invitation = createDummyInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commitAndCloseSession();
		
		Invitation reloadedInvitation = invitationDao.findInvitationByToken(invitation.getToken());
		Assert.assertNotNull(reloadedInvitation);
		Assert.assertNotNull(reloadedInvitation.getKey());
		Assert.assertNotNull(reloadedInvitation.getBaseGroup());
		Assert.assertEquals(invitation, reloadedInvitation);
		Assert.assertEquals(invitation.getToken(), reloadedInvitation.getToken());
	}
	
	@Test
	public void findInvitationByKey() {
		Invitation invitation = createDummyInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commitAndCloseSession();
		
		Invitation reloadedInvitation = invitationDao.findInvitationByKey(invitation.getKey());
		Assert.assertNotNull(reloadedInvitation);
		Assert.assertNotNull(reloadedInvitation.getKey());
		Assert.assertNotNull(reloadedInvitation.getBaseGroup());
		Assert.assertEquals(invitation, reloadedInvitation);
		Assert.assertEquals(invitation.getToken(), reloadedInvitation.getToken());
	}
	
	@Test
	public void findInvitation_group() {
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-1");
		Invitation invitation = createDummyInvitation();
		// Need the email to match user <-> invitation
		((InvitationImpl)invitation).setMail(invitee.getUser().getEmail());
		invitation = invitationDao.update(invitation);
		groupDao.addMembershipTwoWay(invitation.getBaseGroup(), invitee, GroupRoles.invitee.name());
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		Invitation foundInvitation = invitationDao.findInvitation(invitation.getBaseGroup(), invitee);
		Assert.assertNotNull(foundInvitation);
		Assert.assertEquals(invitation, foundInvitation);
	}
	
	/**
	 * By invitation foreign key to identity
	 */
	@Test
	public void findInvitationsByIdentity() {
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-2");
		Invitation invitation = createDummyInvitation();
		((InvitationImpl)invitation).setIdentity(invitee);
		invitation = invitationDao.update(invitation);
		groupDao.addMembershipTwoWay(invitation.getBaseGroup(), invitee, GroupRoles.invitee.name());
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		List<Invitation> foundInvitations = invitationDao.findInvitations(invitee);
		assertThat(foundInvitations)
			.isNotNull()
			.containsExactlyInAnyOrder(invitation);
	}
	
	/**
	 * By email
	 */
	@Test
	public void findInvitationsByIdentityEmail() {
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-3");
		Invitation invitation = createDummyInvitation();
		((InvitationImpl)invitation).setMail(invitee.getUser().getEmail());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		List<Invitation> foundInvitations = invitationDao.findInvitations(invitee);
		assertThat(foundInvitations)
			.isNotNull()
			.containsExactlyInAnyOrder(invitation);
	}
	
	@Test
	public void findSimilarInvitations() {
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-3");
		Invitation invitation = createDummyInvitation();
		((InvitationImpl)invitation).setMail(invitee.getUser().getEmail());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		List<Invitation> foundInvitations = invitationDao.findInvitations(invitation.getType(),
				invitee.getUser().getEmail().toUpperCase(), invitation.getBaseGroup());
		assertThat(foundInvitations)
			.isNotNull()
			.containsExactlyInAnyOrder(invitation);
	}
	
	@Test
	public void findInvitations_businessGroup() {
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Invitations", "Group for invitations",
				BusinessGroup.BUSINESS_TYPE, null, null, 0, 10, false, false, null);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-4");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.businessGroup);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(businessGroup.getBaseGroup());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		List<Invitation> foundInvitations = invitationDao.findInvitations(businessGroup, searchParams);
		assertThat(foundInvitations)
			.isNotNull()
			.containsExactlyInAnyOrder(invitation);
	}
	
	@Test
	public void findInvitations_project() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.project);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(project.getBaseGroup());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		List<Invitation> foundInvitations = invitationDao.findInvitations(project, searchParams);
		assertThat(foundInvitations)
			.isNotNull()
			.containsExactlyInAnyOrder(invitation);
	}
	
	@Test
	public void findInvitations_course() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-auth-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-5");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.repositoryEntry);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(defGroup);
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);

		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		List<Invitation> foundInvitations = invitationDao.findInvitations(entry, searchParams);
		assertThat(foundInvitations)
			.isNotNull()
			.containsExactlyInAnyOrder(invitation);
	}
	
	@Test
	public void findInvitationsWithBusinessGroup() {
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Invitations", "Group for invitations",
				BusinessGroup.BUSINESS_TYPE, null, null, 0, 10, false, false, null);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-4");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.businessGroup);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(businessGroup.getBaseGroup());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		searchParams.setIdentityKey(invitee.getKey());
		searchParams.setSearchString("invitations");
		searchParams.setUserPropertyHandlers(List.of());
		List<InvitationWithBusinessGroup> foundInvitations = invitationDao.findInvitationsWitBusinessGroups(searchParams);
		Assert.assertEquals(1, foundInvitations.size());
		Assert.assertEquals(invitation, foundInvitations.get(0).getInvitation());
		Assert.assertEquals(businessGroup, foundInvitations.get(0).getBusinessGroup());
	}
	
	@Test
	public void findInvitationsWithProject() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		project.setTitle("Project with invitations.");
		project = projectService.updateProject(project.getCreator(), project);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.project);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(project.getBaseGroup());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		searchParams.setIdentityKey(invitee.getKey());
		searchParams.setSearchString("invitations");
		searchParams.setUserPropertyHandlers(List.of());
		List<InvitationWithProject> foundInvitations = invitationDao.findInvitationsWitProjects(searchParams);
		Assert.assertEquals(1, foundInvitations.size());
		Assert.assertEquals(invitation, foundInvitations.get(0).getInvitation());
		Assert.assertEquals(project, foundInvitations.get(0).getProject());
	}
	
	@Test
	public void findInvitationsWithRepositoryEntries() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-auth-6");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-6");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.repositoryEntry);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(defGroup);
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);

		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		searchParams.setIdentityKey(invitee.getKey());
		searchParams.setSearchString(entry.getDisplayname());
		searchParams.setUserPropertyHandlers(List.of());
		List<InvitationWithRepositoryEntry> foundInvitations = invitationDao.findInvitationsWithRepositoryEntries(searchParams, false);
		Assert.assertEquals(1, foundInvitations.size());
		Assert.assertEquals(invitation, foundInvitations.get(0).getInvitation());
		Assert.assertEquals(entry, foundInvitations.get(0).getEntry());
	}
	
	@Test
	public void findInvitationsWithRepositoryEntries_followBusinesGroups() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-auth-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Invitations in course", "Group for invitations",
				BusinessGroup.BUSINESS_TYPE, null, null, 0, 10, false, false, entry);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-7");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.businessGroup);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(businessGroup.getBaseGroup());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);

		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		searchParams.setIdentityKey(invitee.getKey());
		searchParams.setSearchString(entry.getDisplayname());
		searchParams.setUserPropertyHandlers(List.of());
		
		// Search following groups
		List<InvitationWithRepositoryEntry> foundInvitations = invitationDao.findInvitationsWithRepositoryEntries(searchParams, true);
		Assert.assertEquals(1, foundInvitations.size());
		Assert.assertEquals(invitation, foundInvitations.get(0).getInvitation());
		Assert.assertEquals(entry, foundInvitations.get(0).getEntry());
		
		// Search strictly in repository entry
		List<InvitationWithRepositoryEntry> foundInvitationsInRepo = invitationDao.findInvitationsWithRepositoryEntries(searchParams, false);
		Assert.assertTrue(foundInvitationsInRepo.isEmpty());
	}
	
	@Test
	public void loadByKey() {
		Invitation invitation = createDummyInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commitAndCloseSession();
		
		Invitation reloadedInvitation = invitationDao.loadByKey(invitation.getKey());
		Assert.assertNotNull(reloadedInvitation);
		Assert.assertEquals(invitation, reloadedInvitation);
	}
	
	@Test
	public void hasInvitationTestHQL() {
		String token = UUID.randomUUID().toString();
		boolean hasInvitation = invitationDao.hasInvitations(token);
		Assert.assertFalse(hasInvitation);
	}
	
	@Test
	public void hasInvitation() {
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.repositoryEntry);
		dbInstance.commitAndCloseSession();
		
		// Not linked to a resource, group, binder or repository entry
		boolean hasInvitation = invitationDao.hasInvitations(invitation.getToken());
		Assert.assertFalse(hasInvitation);
		
		// Link to a repository entry
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-auth-12");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-12");
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(defGroup);
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		
		boolean hasValidInvitation = invitationDao.hasInvitations(invitation.getToken());
		Assert.assertTrue(hasValidInvitation);
	}
	
	@Test
	public void hasInvitationsBusinessGroup() {
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Invitations", "Group To test has invitations",
				BusinessGroup.BUSINESS_TYPE, null, null, 0, 10, false, false, null);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-9");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.businessGroup);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(businessGroup.getBaseGroup());
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);

		boolean foundInvitations = invitationDao.hasInvitations(businessGroup);
		Assert.assertTrue(foundInvitations);
	}
	
	@Test
	public void hasInvitationsRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-auth-10");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-10");
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.repositoryEntry);
		((InvitationImpl)invitation).setIdentity(invitee);
		((InvitationImpl)invitation).setBaseGroup(defGroup);
		invitation = invitationDao.update(invitation);
		dbInstance.commit();
		Assert.assertNotNull(invitation);

		boolean foundInvitations = invitationDao.hasInvitations(entry);
		Assert.assertTrue(foundInvitations);
	}
	
	@Test
	public void isInvitee() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("invitation-admin-1");
		
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.binder);
		String uuid = UUID.randomUUID().toString().replace("-", "");
		invitation.setFirstName("Fiona");
		invitation.setLastName("Laurence".concat(uuid));
		invitation.setMail(uuid.concat("@frentix.com"));

		Group group = groupDao.createGroup();
		Identity id2 = invitationService.getOrCreateIdentityAndPersistInvitation(invitation, group, Locale.ENGLISH, admin);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Invitee-2");
		dbInstance.commitAndCloseSession();
		
		boolean invitee = invitationDao.isInvitee(id2);
		Assert.assertTrue(invitee);
		boolean notInvitee = invitationDao.isInvitee(id1);
		Assert.assertFalse(notInvitee);
	}
	
	@Test
	public void countInvitations() {
		Invitation invitation = createDummyInvitation();
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		long numOfInvitations = invitationDao.countInvitations();
		Assert.assertTrue(numOfInvitations > 0l);
	}
	
	@Test
	public void deleteInvitationByGroup() {
		Invitation invitation = createDummyInvitation();
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		invitationDao.deleteInvitation(invitation.getBaseGroup());
		dbInstance.commit();
		
		Invitation deletedInvitation = invitationDao.findInvitationByToken(invitation.getToken());
		Assert.assertNull(deletedInvitation);
	}
	
	@Test
	public void deleteInvitationByIdentity() {
		Invitation invitation = createDummyInvitation();
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-5");
		((InvitationImpl)invitation).setIdentity(invitee);
		invitation = invitationDao.update(invitation);
		dbInstance.commitAndCloseSession();
		
		int deletedRows = invitationDao.deleteInvitation(invitee);
		dbInstance.commit();
		Assert.assertEquals(1, deletedRows);
		
		Invitation deletedInvitation = invitationDao.findInvitationByKey(invitation.getKey());
		Assert.assertNull(deletedInvitation);
	}
	

	private Invitation createDummyInvitation() {
		Group group = groupDao.createGroup();
		InvitationImpl invitation = (InvitationImpl)invitationDao.createInvitation(InvitationTypeEnum.binder);
		invitation.setBaseGroup(group);
		dbInstance.getCurrentEntityManager().persist(invitation);
		return invitation;
	}
}
