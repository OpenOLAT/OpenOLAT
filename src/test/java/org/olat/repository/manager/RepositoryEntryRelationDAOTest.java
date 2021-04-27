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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRelationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void getDefaultGroup() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		Group group = repositoryEntryRelationDao.getDefaultGroup(re);
		Assert.assertNotNull(group);
	}
	
	@Test
	public void addRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("add-role-2-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();

		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
	}
	
	@Test
	public void hasRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("add-role-3-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
		
		boolean owner = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.owner.name());
		Assert.assertTrue(owner);
		boolean participant = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.participant.name());
		Assert.assertFalse(participant);
	}
	
	@Test
	public void hasRole_admin() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("admin-role-3-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		
		boolean admin = repositoryEntryRelationDao.hasRole(id, re, true, OrganisationRoles.administrator.name());
		Assert.assertTrue(admin);
	}
	
	@Test
	public void getRoles() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("get-roles-1-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
		
		List<String> ownerRoles = repositoryEntryRelationDao.getRoles(id, re);
		Assert.assertNotNull(ownerRoles);
		Assert.assertEquals(1, ownerRoles.size());
		Assert.assertEquals(GroupRoles.owner.name(), ownerRoles.get(0));
	}
	
	@Test
	public void getRoleAndDefaults() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("get-roles-1-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		List<Object[]> participantRoles = repositoryEntryRelationDao.getRoleAndDefaults(id, re);
		Assert.assertNotNull(participantRoles);
		Assert.assertEquals(2, participantRoles.size());
	
		Object[] firstRole = participantRoles.get(0);
		Object[] secondRole = participantRoles.get(1);
		assertThat((String)firstRole[0]).isIn(GroupRoles.participant.name(), OrganisationRoles.user.name());
		assertThat((String)secondRole[0]).isIn(GroupRoles.participant.name(), OrganisationRoles.user.name());
	}
	
	@Test
	public void removeRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("add-role-4-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
		
		//check
		Assert.assertTrue(repositoryEntryRelationDao.hasRole(id, re, GroupRoles.owner.name()));
		
		//remove role
		int removeRoles = repositoryEntryRelationDao.removeRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, removeRoles);
		
		//check
		boolean owner = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.owner.name());
		Assert.assertFalse(owner);
		boolean participant = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.participant.name());
		Assert.assertFalse(participant);
	}
	
	@Test
	public void getMembersAndCountMembers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-2-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		dbInstance.commit();

		//all members
		List<Identity> members = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.defaultGroup,
				GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(2, members.size());
		Assert.assertTrue(members.contains(id1));
		Assert.assertTrue(members.contains(id2));
		
		// owner
		int numOfOwners = repositoryEntryRelationDao.countMembers(re, GroupRoles.owner.name());
		Assert.assertEquals(1, numOfOwners);
		
		//participant
		List<Identity> participants = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		int numOfParticipants = repositoryEntryRelationDao.countMembers(re, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(1, participants.size());
		Assert.assertEquals(1, numOfParticipants);
		Assert.assertTrue(participants.contains(id2));
	}
	
	@Test
	public void getMembers_defaultGroup() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("owner-1-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(owner, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		
		List<Identity> owners = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(1, owners.size());
		Assert.assertTrue(owners.contains(owner));
	}
	
	@Test
	public void getMembers_follow() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-3-");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-4-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re1 = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		RepositoryEntry re2 = repositoryService.create(null, "Rei Ayanami (alt)", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id1, re1, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id3, re2, GroupRoles.participant.name());
		dbInstance.commit();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "group", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, re2);
	    businessGroupRelationDao.addRole(id4, group, GroupRoles.coach.name());
	    dbInstance.commit();
		
		List<RepositoryEntry> res = new ArrayList<>();
		res.add(re1);
		res.add(re2);

		//all members
		List<Identity> coaches = repositoryEntryRelationDao.getMembers(res, RepositoryEntryRelationType.all, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(1, coaches.size());
		Assert.assertTrue(coaches.contains(id4));
		
		//participant
		List<Identity> participants = repositoryEntryRelationDao.getMembers(res, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(id2));
		Assert.assertTrue(participants.contains(id3));
	}
	
	@Test
	public void countMembers_list() {
		//create a repository entry with a business group
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-3-");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-4-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "count relation 1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id2, group, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(id3, group, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(id4, group, GroupRoles.coach.name());
	    dbInstance.commitAndCloseSession();
	    
		//get the number of members
	    int numOfMembers = repositoryService.countMembers(Collections.singletonList(re), null);
		Assert.assertEquals(4, numOfMembers);
		
		//get the number of members without id1
	    int numOfMembersWithExclude = repositoryService.countMembers(Collections.singletonList(re), id1);
		Assert.assertEquals(3, numOfMembersWithExclude);
	}
	
	@Test
	public void getEnrollmentDate() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("enroll-date-1-");
		Identity wid = JunitTestHelper.createAndPersistIdentityAsRndUser("not-enroll-date-1-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		//enrollment date
		Date enrollmentDate = repositoryEntryRelationDao.getEnrollmentDate(re, id);
		Assert.assertNotNull(enrollmentDate);
		
		//this user isn't enrolled
		Date withoutEnrollmentDate = repositoryEntryRelationDao.getEnrollmentDate(re, wid);
		Assert.assertNull(withoutEnrollmentDate);
		
		//as participant
		Date participantEnrollmentDate = repositoryEntryRelationDao.getEnrollmentDate(re, id, GroupRoles.participant.name());
		Assert.assertNotNull(participantEnrollmentDate);
		//as owner
		Date ownerEnrollmentDate = repositoryEntryRelationDao.getEnrollmentDate(re, id, GroupRoles.owner.name());
		Assert.assertNotNull(ownerEnrollmentDate);
		//is not enrolled as coached
		Date coachEnrollmentDate = repositoryEntryRelationDao.getEnrollmentDate(re, id, GroupRoles.coach.name());
		Assert.assertNull(coachEnrollmentDate);
	}
	
	@Test
	public void getEnrollmentDates() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("enroll-date-2-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("enroll-date-3-");
		Identity wid = JunitTestHelper.createAndPersistIdentityAsRndUser("not-enroll-date-2-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		//enrollment date
		Map<Long,Date> enrollmentDates = repositoryEntryRelationDao.getEnrollmentDates(re);
		Assert.assertNotNull(enrollmentDates);
		Assert.assertEquals(2, enrollmentDates.size());
		Assert.assertTrue(enrollmentDates.containsKey(id1.getKey()));
		Assert.assertTrue(enrollmentDates.containsKey(id2.getKey()));
		Assert.assertFalse(enrollmentDates.containsKey(wid.getKey()));
	}

	@Test
	public void getEnrollmentDates_emptyCourse() {
		//enrollment of an empty course
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry notEnrolledRe = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		Map<Long,Date> notEnrollmentDates = repositoryEntryRelationDao.getEnrollmentDates(notEnrolledRe);
		Assert.assertNotNull(notEnrollmentDates);
		Assert.assertEquals(0, notEnrollmentDates.size());
	}
	
	@Test
	public void getAuthorKeys() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("part-2-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		List<Long> authorKeys = repositoryEntryRelationDao.getAuthorKeys(re);
		Assert.assertNotNull(authorKeys);
		Assert.assertEquals(1, authorKeys.size());
		Assert.assertEquals(id1.getKey(), authorKeys.get(0));
	}
	
	@Test
	public void isMember() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "memberg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id1, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		//id1 is member
		boolean member1 = repositoryEntryRelationDao.isMember(id1, re);
		Assert.assertTrue(member1);
		//id2 is not member
		boolean member2 = repositoryEntryRelationDao.isMember(id2, re);
		Assert.assertFalse(member2);
	}
	
	@Test
	public void isMember_v2() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-1-lc-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-2-lc-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-3-lc-");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-4-lc-");
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-5-lc-");
		Identity id6 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-6-lc-");
		Identity idNull = JunitTestHelper.createAndPersistIdentityAsRndUser("re-is-member-null-lc-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "member-1-g", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "member-2-g", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "member-3-g", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, re);
		BusinessGroup groupNull = businessGroupService.createBusinessGroup(null, "member-null-g", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, null);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(id4, group1, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(id5, group2, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(id6, group3, GroupRoles.waiting.name());
	    businessGroupRelationDao.addRole(idNull, groupNull, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//id1 is owner
		boolean member1 = repositoryEntryRelationDao.isMember(id1, re);
		Assert.assertTrue(member1);
		//id2 is tutor
		boolean member2 = repositoryEntryRelationDao.isMember(id2, re);
		Assert.assertTrue(member2);
		//id3 is repo participant
		boolean member3 = repositoryEntryRelationDao.isMember(id3, re);
		Assert.assertTrue(member3);
		//id4 is group coach
		boolean member4= repositoryEntryRelationDao.isMember(id4, re);
		Assert.assertTrue(member4);
		//id5 is group participant
		boolean member5 = repositoryEntryRelationDao.isMember(id5, re);
		Assert.assertTrue(member5);
		//id6 is waiting
		boolean member6 = repositoryEntryRelationDao.isMember(id6, re);
		Assert.assertFalse(member6);
		//idNull is not member
		boolean memberNull = repositoryEntryRelationDao.isMember(idNull, re);
		Assert.assertFalse(memberNull);
	}
	
	@Test
	public void filterMembership() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "memberg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(id, re2, GroupRoles.owner.name());
	    RepositoryEntry re3 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(id, re3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		//id is member
		List<Long> entries = new ArrayList<>();
		entries.add(re1.getKey());
		entries.add(re2.getKey());
		entries.add(re3.getKey());
		entries.add(502l);
		repositoryEntryRelationDao.filterMembership(id, entries);

		Assert.assertTrue(entries.contains(re1.getKey()));
		Assert.assertTrue(entries.contains(re2.getKey()));
		//waiting list
		Assert.assertFalse(entries.contains(re3.getKey()));
		//unkown
		Assert.assertFalse(entries.contains(502l));
		
		//check against empty value
		List<Long> empyEntries = new ArrayList<>();
		repositoryEntryRelationDao.filterMembership(id, empyEntries);
		Assert.assertTrue(empyEntries.isEmpty());
	}
	
	@Test
	public void countRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "count relation 1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    businessGroupService.addResourceTo(group, re2);
	    dbInstance.commitAndCloseSession();

	    int numOfRelations = repositoryEntryRelationDao.countRelations(group.getBaseGroup());
	    Assert.assertEquals(2, numOfRelations);
	}
	
	@Test
	public void getRelations_group() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-" );
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "get relations", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    businessGroupService.addResourceTo(group, re2);
	    dbInstance.commitAndCloseSession();
	    
	    //get the relations from the business group's base group to the 2 repository entries
	    List<Group> groups = Collections.singletonList(group.getBaseGroup());
	    List<RepositoryEntryToGroupRelation> relations = repositoryEntryRelationDao.getRelations(groups);
	    Assert.assertNotNull(relations);
	    Assert.assertEquals(2, relations.size());
		Assert.assertTrue(relations.get(0).getEntry().equals(re1) || relations.get(0).getEntry().equals(re2));
		Assert.assertTrue(relations.get(1).getEntry().equals(re1) || relations.get(1).getEntry().equals(re2));
	}
	
	@Test
	public void getBusinessGroupAndCurriculumRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "get relations", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    dbInstance.commitAndCloseSession();
	    
	    //get the relations from the business group's base group to the repository entry
	    List<RepositoryEntryToGroupRelation> relations = repositoryEntryRelationDao.getBusinessGroupAndCurriculumRelations(re);
	    Assert.assertNotNull(relations);
	    Assert.assertEquals(1, relations.size());
		Assert.assertTrue(relations.get(0).getEntry().equals(re));
	}
	
	@Test
	public void hasRelation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry reMarker = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "get relations", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    dbInstance.commitAndCloseSession();
	    
	    // The repository entry has a relation with the business group
	    boolean hasRelation = repositoryEntryRelationDao.hasRelation(group.getBaseGroup(), re);
		Assert.assertTrue(hasRelation);
	    // The marker repository entry doesn't have a relation with the business group
	    boolean hasNotRelation = repositoryEntryRelationDao.hasRelation(group.getBaseGroup(), reMarker);
		Assert.assertFalse(hasNotRelation);
	}
	
	@Test
	public void removeMembers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-rm-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-rm-2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-rm-3-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.owner.name());
	    dbInstance.commitAndCloseSession();
	    
	    List<Identity> membersToRemove = new ArrayList<>(2);
	    membersToRemove.add(id2);
	    membersToRemove.add(id3);
		boolean removed = repositoryEntryRelationDao.removeMembers(re, membersToRemove);
		Assert.assertTrue(removed);
		dbInstance.commitAndCloseSession();
		
		List<Identity> members = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.defaultGroup,
				GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertNotNull(members);
	    Assert.assertEquals(1, members.size());
	    Assert.assertTrue(members.contains(id1));
	}
	
	@Test
	public void removeRelation_specificOne() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc");
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "remove relation", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    businessGroupService.addResourceTo(group, re2);
	    dbInstance.commitAndCloseSession();
	    
	    int numOfRelations = repositoryEntryRelationDao.removeRelation(group.getBaseGroup(), re2);
	    Assert.assertEquals(1, numOfRelations);
	    dbInstance.commitAndCloseSession();
	    
	    List<Group> groups = Collections.singletonList(group.getBaseGroup());
	    List<RepositoryEntryToGroupRelation> relations = repositoryEntryRelationDao.getRelations(groups);
	    Assert.assertEquals(1, relations.size());
	    RepositoryEntry relationRe1 = relations.get(0).getEntry();
	    Assert.assertNotNull(relationRe1);
	    Assert.assertEquals(re1, relationRe1);
	}
	
	@Test
	public void removeRelations_repositoryEntrySide() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "remove all relations", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    businessGroupService.addResourceTo(group, re2);
	    dbInstance.commitAndCloseSession();
	    
	    int numOfRelations = repositoryEntryRelationDao.removeRelations(re2);
	    Assert.assertEquals(3, numOfRelations);//default relation + relation to group + default organization
	    dbInstance.commitAndCloseSession();
	    
	    List<Group> groups = Collections.singletonList(group.getBaseGroup());
	    List<RepositoryEntryToGroupRelation> relations = repositoryEntryRelationDao.getRelations(groups);
	    Assert.assertEquals(1, relations.size());
	    RepositoryEntry relationRe1 = relations.get(0).getEntry();
	    Assert.assertNotNull(relationRe1);
	    Assert.assertEquals(re1, relationRe1);
	}
	
	@Test
	public void removeRelation_byGroup() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "remove relation by group", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
	    businessGroupService.addResourceTo(group, re2);
	    dbInstance.commitAndCloseSession();
	    
	    int numOfRelations = repositoryEntryRelationDao.removeRelation(group.getBaseGroup());
	    Assert.assertEquals(2, numOfRelations);
	    dbInstance.commitAndCloseSession();
	    
	    List<Group> groups = Collections.singletonList(group.getBaseGroup());
	    List<RepositoryEntryToGroupRelation> relations = repositoryEntryRelationDao.getRelations(groups);
	    Assert.assertEquals(0, relations.size());
	}
	
	@Test
	public void getOrganisations() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Repo-org-1", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, organisation);
		dbInstance.commitAndCloseSession();
		
		List<Organisation> organisations = repositoryEntryRelationDao.getOrganisations(re);
		Assert.assertNotNull(organisations);
		Assert.assertEquals(1, organisations.size());
		Assert.assertEquals(organisation, organisations.get(0));
	}
	
	@Test
	public void getRepositoryEntries() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Repo-org-1", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, organisation);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryEntryRelationDao.getRepositoryEntries(organisation);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(re, entries.get(0));
	}
	
	@Test
	public void getRelatedMembers_CoachedParticipants() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		// Course members
		Identity courseOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-owner-1");
		repositoryService.addRole(courseOwner, entry, GroupRoles.owner.name());
		Identity courseCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-coach-1");
		repositoryService.addRole(courseCoach, entry, GroupRoles.coach.name());
		Identity courseParticipant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-participant-1");
		repositoryService.addRole(courseParticipant1, entry, GroupRoles.participant.name());
		Identity courseParticipant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-participant-2");
		repositoryService.addRole(courseParticipant2, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//Group 1 members
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "group-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, entry);
		Identity groupOwner1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-owner-1-1");
		businessGroupRelationDao.addRole(groupOwner1_1, group1, GroupRoles.owner.name());
		Identity groupCoach1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-coach-1-1");
		businessGroupRelationDao.addRole(groupCoach1_1, group1, GroupRoles.coach.name());
		Identity groupParticipant1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-participant-1-1");
		businessGroupRelationDao.addRole(groupParticipant1_1, group1, GroupRoles.participant.name());
		Identity groupParticipant1_2= JunitTestHelper.createAndPersistIdentityAsRndUser("group-participant-1-1");
		businessGroupRelationDao.addRole(groupParticipant1_2, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//Group 2 members
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "group-2", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, entry);
		Identity groupOwner2_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-owner-2-1");
		businessGroupRelationDao.addRole(groupOwner2_1, group2, GroupRoles.owner.name());
		Identity groupCoach2_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-coach-2-1");
		businessGroupRelationDao.addRole(groupCoach2_1, group2, GroupRoles.coach.name());
		Identity groupParticipant2_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-participant-2-1");
		businessGroupRelationDao.addRole(groupParticipant2_1, group2, GroupRoles.participant.name());
		Identity groupParticipant2_2= JunitTestHelper.createAndPersistIdentityAsRndUser("group-participant-2-1");
		businessGroupRelationDao.addRole(groupParticipant2_2, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Course coach coaches course participant but not group participant
		List<Identity> courseCoachedParticipants = repositoryEntryRelationDao.getRelatedMembers(entry, courseCoach, GroupRoles.coach, GroupRoles.participant);
		assertThat(courseCoachedParticipants)
				.hasSize(2)
				.containsExactlyInAnyOrder(courseParticipant1, courseParticipant2);
		
		// Group coach coaches group participant but not course participant
		List<Identity> groupdParticipants = repositoryEntryRelationDao.getRelatedMembers(entry, groupCoach1_1, GroupRoles.coach, GroupRoles.participant);
		assertThat(groupdParticipants)
				.hasSize(2)
				.containsExactlyInAnyOrder(groupParticipant1_1, groupParticipant1_2);
		
		// Coach of group 1 is now coach of group 2 as well
		businessGroupRelationDao.addRole(groupCoach1_1, group2, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		groupdParticipants = repositoryEntryRelationDao.getRelatedMembers(entry, groupCoach1_1, GroupRoles.coach, GroupRoles.participant);
		assertThat(groupdParticipants)
				.hasSize(4)
				.containsExactlyInAnyOrder(groupParticipant1_1, groupParticipant1_2, groupParticipant2_1, groupParticipant2_2);
		
		// Participant of group 1 is now member of group 2 as well. Get it only once.
		businessGroupRelationDao.addRole(groupParticipant1_1, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		groupdParticipants = repositoryEntryRelationDao.getRelatedMembers(entry, groupCoach1_1, GroupRoles.coach, GroupRoles.participant);
		assertThat(groupdParticipants)
				.hasSize(4)
				.containsExactlyInAnyOrder(groupParticipant1_1, groupParticipant1_2, groupParticipant2_1, groupParticipant2_2);
		
		// Course coach is now participant of group 1. He still coaches only the course participants
		businessGroupRelationDao.addRole(courseCoach, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		courseCoachedParticipants = repositoryEntryRelationDao.getRelatedMembers(entry, courseCoach, GroupRoles.coach, GroupRoles.participant);
		assertThat(courseCoachedParticipants)
				.hasSize(2)
				.containsExactlyInAnyOrder(courseParticipant1, courseParticipant2);
		
		// Coach of course is coach of an other repo. Don't get the participants of the of the repo.
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity course2Participant = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-participant-122");
		repositoryService.addRole(course2Participant, entry2, GroupRoles.participant.name());
		repositoryService.addRole(courseCoach, entry2, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		courseCoachedParticipants = repositoryEntryRelationDao.getRelatedMembers(entry, courseCoach, GroupRoles.coach, GroupRoles.participant);
		assertThat(courseCoachedParticipants)
				.hasSize(2)
				.containsExactlyInAnyOrder(courseParticipant1, courseParticipant2);
	}
	
	@Test
	public void getRelatedMembers_AssignedCoaches() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		// Course members
		Identity courseOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-owner-1");
		repositoryService.addRole(courseOwner, entry, GroupRoles.owner.name());
		Identity courseParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-participant-1");
		repositoryService.addRole(courseParticipant, entry, GroupRoles.participant.name());
		Identity courseCoach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-coach-1");
		repositoryService.addRole(courseCoach1, entry, GroupRoles.coach.name());
		Identity courseCoach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-coach-2");
		repositoryService.addRole(courseCoach2, entry, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//Group 1 members
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "group-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, entry);
		Identity groupOwner1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-owner-1-1");
		businessGroupRelationDao.addRole(groupOwner1_1, group1, GroupRoles.owner.name());
		Identity groupParticipant1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-participant-1-1");
		businessGroupRelationDao.addRole(groupParticipant1_1, group1, GroupRoles.participant.name());
		Identity groupCoach1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-coach-1-1");
		businessGroupRelationDao.addRole(groupCoach1_1, group1, GroupRoles.coach.name());
		Identity groupCoach1_2= JunitTestHelper.createAndPersistIdentityAsRndUser("group-coach-1-1");
		businessGroupRelationDao.addRole(groupCoach1_2, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//Group 2 members
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "group-2", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, true, false, entry);
		Identity groupOwner2_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-owner-2-1");
		businessGroupRelationDao.addRole(groupOwner2_1, group2, GroupRoles.owner.name());
		Identity groupParticipant2_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-participant-2-1");
		businessGroupRelationDao.addRole(groupParticipant2_1, group2, GroupRoles.participant.name());
		Identity groupCoach2_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("group-coach-2-1");
		businessGroupRelationDao.addRole(groupCoach2_1, group2, GroupRoles.coach.name());
		Identity groupCoach2_2= JunitTestHelper.createAndPersistIdentityAsRndUser("group-coach-2-1");
		businessGroupRelationDao.addRole(groupCoach2_2, group2, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// Course participant participantes course coach but not group coach
		List<Identity> courseParticipantedCoachs = repositoryEntryRelationDao.getRelatedMembers(entry,
				courseParticipant, GroupRoles.participant, GroupRoles.coach);
		assertThat(courseParticipantedCoachs)
				.hasSize(2)
				.containsExactlyInAnyOrder(courseCoach1, courseCoach2);
		
		// Group participant participantes group coach but not course coach
		List<Identity> groupdCoachs = repositoryEntryRelationDao.getRelatedMembers(entry, groupParticipant1_1,
				GroupRoles.participant, GroupRoles.coach);
		assertThat(groupdCoachs)
				.hasSize(2)
				.containsExactlyInAnyOrder(groupCoach1_1, groupCoach1_2);
		
		// Participant of group 1 is now participant of group 2 as well
		businessGroupRelationDao.addRole(groupParticipant1_1, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		groupdCoachs = repositoryEntryRelationDao.getRelatedMembers(entry, groupParticipant1_1, GroupRoles.participant, GroupRoles.coach);
		assertThat(groupdCoachs)
				.hasSize(4)
				.containsExactlyInAnyOrder(groupCoach1_1, groupCoach1_2, groupCoach2_1, groupCoach2_2);
		
		// Coach of group 1 is now member of group 2 as well. Get it only once.
		businessGroupRelationDao.addRole(groupCoach1_1, group2, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		groupdCoachs = repositoryEntryRelationDao.getRelatedMembers(entry, groupParticipant1_1, GroupRoles.participant, GroupRoles.coach);
		assertThat(groupdCoachs)
				.hasSize(4)
				.containsExactlyInAnyOrder(groupCoach1_1, groupCoach1_2, groupCoach2_1, groupCoach2_2);
		
		// Course participant is now coach of group 1. He still participantes only the course coachs
		businessGroupRelationDao.addRole(courseParticipant, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		courseParticipantedCoachs = repositoryEntryRelationDao.getRelatedMembers(entry, courseParticipant, GroupRoles.participant, GroupRoles.coach);
		assertThat(courseParticipantedCoachs)
				.hasSize(2)
				.containsExactlyInAnyOrder(courseCoach1, courseCoach2);
		
		// Participant of course is participant of an other repo. Don't get the coachs of the of the repo.
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity course2Coach = JunitTestHelper.createAndPersistIdentityAsRndUser("repo-coach-122");
		repositoryService.addRole(course2Coach, entry2, GroupRoles.coach.name());
		repositoryService.addRole(courseParticipant, entry2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		courseParticipantedCoachs = repositoryEntryRelationDao.getRelatedMembers(entry, courseParticipant, GroupRoles.participant, GroupRoles.coach);
		assertThat(courseParticipantedCoachs)
				.hasSize(2)
				.containsExactlyInAnyOrder(courseCoach1, courseCoach2);
	}
	
	@Test
	public void getGroupDependencies() {
		List<Long> dependencies = repositoryEntryRelationDao.getBrokenGroupDependencies(0, 5000);
		Assert.assertNotNull(dependencies);
		Assert.assertEquals(0, dependencies.size());
	}
	
	@Test
	public void detectBrokenGroupDependencies() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Group group = groupDao.createGroup();
		RepositoryEntryToGroupRelation relation = repositoryEntryRelationDao.createRelation(group, entry);
		dbInstance.commitAndCloseSession();
		
		List<Long> dependencies = repositoryEntryRelationDao.getBrokenGroupDependencies(0, 5000);
		Assert.assertNotNull(dependencies);
		Assert.assertEquals(1, dependencies.size());
		
		RepositoryEntryToGroupRelation reloadedRelation = repositoryEntryRelationDao.loadRelationByKey(relation.getKey());
		repositoryEntryRelationDao.removeRelation(reloadedRelation);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryToGroupRelation deletedRelation = repositoryEntryRelationDao.loadRelationByKey(reloadedRelation.getKey());
		Assert.assertNull(deletedRelation);
		
		List<Long> brokenDependencies = repositoryEntryRelationDao.getBrokenGroupDependencies(0, 5000);
		Assert.assertEquals(0, brokenDependencies.size());
	}
	
}
