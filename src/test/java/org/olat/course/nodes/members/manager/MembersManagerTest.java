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
package org.olat.course.nodes.members.manager;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.nodes.members.MembersManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 24 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private MembersManager membersManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void getOwners() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		List<Identity> owners = membersManager.getOwners(entry);
		Assert.assertEquals(1, owners.size());
		Assert.assertEquals(author, owners.get(0));
	}
	
	@Test
	public void getOwnersKeys() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
	    // add curriculum
	    Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("Curriculum-elements", "Members Curriculum", "A curriculum for members", defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);

		Identity curriculumElementOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-2b");
		curriculumService.addMember(element, curriculumElementOwner, CurriculumRoles.owner);
		
		List<Long> ownersKeys = membersManager.getOwnersKeys(entry);
		Assert.assertEquals(2, ownersKeys.size());
		Assert.assertTrue(ownersKeys.contains(author.getKey()));
		Assert.assertTrue(ownersKeys.contains(curriculumElementOwner.getKey()));
	}
	
	@Test
	public void getParticipants() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		// add course participants
		Identity part1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-1");
		repositoryEntryRelationDao.addRole(part1, entry, GroupRoles.participant.name());
		Identity part2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-2");
		repositoryEntryRelationDao.addRole(part2, entry, GroupRoles.participant.name());
		
		// add groups
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "memberg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);
		Identity coach3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-3");
	    businessGroupRelationDao.addRole(coach3, group, GroupRoles.coach.name());
	    Identity part4 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-4");
	    businessGroupRelationDao.addRole(part4, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(part2, group, GroupRoles.participant.name());
		
	    dbInstance.commitAndCloseSession();
		
		ModuleConfiguration config = new ModuleConfiguration();
		config.setBooleanEntry(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL, Boolean.TRUE);
		
		List<Identity> participants = membersManager.getParticipants(entry, config);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(part1));
		Assert.assertTrue(participants.contains(part2));
		Assert.assertTrue(participants.contains(part4));
	}
	
	@Test
	public void getParticipants_groups() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-5");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		// add course participants
		Identity part1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-6");
		repositoryEntryRelationDao.addRole(part1, entry, GroupRoles.participant.name());
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-7");
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		
		// add groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "memberg-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);
		Identity coach3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-8");
	    businessGroupRelationDao.addRole(coach3, group1, GroupRoles.coach.name());
	    Identity part4 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-9");
	    businessGroupRelationDao.addRole(part4, group1, GroupRoles.participant.name());
	    Identity part5 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-10");
	    businessGroupRelationDao.addRole(part5, group1, GroupRoles.participant.name());
	    
	    Identity coach6 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-11");
	    BusinessGroup group2 = businessGroupService.createBusinessGroup(coach6, "memberg-2", "tg", BusinessGroup.BUSINESS_TYPE,
	    		null, null, false, false, entry);
	    Identity part7 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-12");
	    businessGroupRelationDao.addRole(part7, group2, GroupRoles.participant.name());
		
	    dbInstance.commitAndCloseSession();
		
		ModuleConfiguration config = new ModuleConfiguration();
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		config.set(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID, groupKeys);
		
		List<Identity> participants = membersManager.getParticipants(entry, config);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(part4));
		Assert.assertTrue(participants.contains(part5));
		Assert.assertTrue(participants.contains(part7));
	}
	
	@Test
	public void getParticipants_groups_emptyList() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-5");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		// add course participants
		Identity part1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-6");
		repositoryEntryRelationDao.addRole(part1, entry, GroupRoles.participant.name());
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-7");
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		
		
	    dbInstance.commitAndCloseSession();
		
		ModuleConfiguration config = new ModuleConfiguration();
		List<Long> groupKeys = new ArrayList<>();

		config.set(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID, groupKeys);
		
		List<Identity> participants = membersManager.getParticipants(entry, config);
		Assert.assertEquals(0, participants.size());
	}
	
	@Test
	public void getParticipants_groups_curriculum() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-14");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		// add group
	    Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-15");
	    BusinessGroup group = businessGroupService.createBusinessGroup(coach1, "memberg-2", "tg", BusinessGroup.BUSINESS_TYPE,
	    		null, null, false, false, entry);
	    Identity part2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-16");
		businessGroupRelationDao.addRole(part2, group, CurriculumRoles.participant.name());
	    Identity part3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-17");
		businessGroupRelationDao.addRole(part3, group, CurriculumRoles.participant.name());
		
	    // add curriculum
	    Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("Curriculum-elements", "Members Curriculum", "A curriculum for members", defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);

	    Identity part4 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-17");
		curriculumService.addMember(element, part4, CurriculumRoles.participant);
	    Identity part5 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-18");
		curriculumService.addMember(element, part5, CurriculumRoles.participant);
		
	    dbInstance.commitAndCloseSession();
		
		ModuleConfiguration config = new ModuleConfiguration();
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group.getKey());
		config.set(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID, groupKeys);
		List<Long> curriculumKeys = new ArrayList<>();
		curriculumKeys.add(element.getKey());
		config.set(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT_ID, curriculumKeys);
		
		List<Identity> participants = membersManager.getParticipants(entry, config);
		Assert.assertEquals(4, participants.size());
		Assert.assertTrue(participants.contains(part2));
		Assert.assertTrue(participants.contains(part3));
		Assert.assertTrue(participants.contains(part4));
		Assert.assertTrue(participants.contains(part5));
	}

	@Test
	public void getParticipants_areas() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-14");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		BGArea area = areaManager.createAndPersistBGArea("area-mem-1", "description:", entry.getOlatResource());
		
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-19");
		BusinessGroup group = businessGroupService.createBusinessGroup(coach1, "area-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, entry);
		areaManager.addBGToBGArea(group, area);
		
		Identity part2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-20");
		businessGroupRelationDao.addRole(part2, group, CurriculumRoles.participant.name());
		Identity part3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-p-21");
		businessGroupRelationDao.addRole(part3, group, CurriculumRoles.participant.name());
		
		dbInstance.commitAndCloseSession();
		
		ModuleConfiguration config = new ModuleConfiguration();
		List<Long> areaIds = new ArrayList<>();
		areaIds.add(area.getKey());
		config.set(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA_ID, areaIds);
		
		List<Identity> participants = membersManager.getParticipants(entry, config);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(part2));
		Assert.assertTrue(participants.contains(part3));
	}

}
