/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.repository;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryManagerTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryManagerTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private ACService acService;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;

	/**
	 * Test creation of a repository entry.
	 */
	@Test
	public void testRawRepositoryEntryCreate() {
		try {
			OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("RepoMgrTestCourse", CodeHelper.getForeverUniqueID());
			OLATResourceManager rm = OLATResourceManager.getInstance();
			// create course and persist as OLATResourceImpl
			OLATResource r =  rm.createOLATResourceInstance(resourceable);
			dbInstance.getCurrentEntityManager().persist(r);

			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			RepositoryEntry d = repositoryService.create(null, "Florian Gnägi", "Lernen mit OpenOLAT", "JunitTest_RepositoryEntry", "Beschreibung",
					r, RepositoryEntryStatusEnum.trash, defOrganisation);
			
			dbInstance.commit();
			Assert.assertNotNull(d);
		} catch(Exception ex) {
			ex.printStackTrace();
			fail("No Exception allowed. ex=" + ex.getMessage());
		}
	}
	
	@Test
	public void lookupRepositoryEntryByOLATResourceable() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry loadedRe = repositoryManager.lookupRepositoryEntry(re.getOlatResource(), false);
		
		Assert.assertNotNull(loadedRe);
		Assert.assertEquals(re, loadedRe);
	}
	
	@Test
	public void lookupRepositoryEntryBySoftkey() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry loadedRe = repositoryManager.lookupRepositoryEntryBySoftkey(re.getSoftkey(), false);
		Assert.assertNotNull(loadedRe);
		Assert.assertEquals(re, loadedRe);
	}
	
	@Test
	public void lookupRepositoryEntryKey() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		//check with a return value
		Long repoKey1 = repositoryManager.lookupRepositoryEntryKey(re.getOlatResource(), false);
		Assert.assertNotNull(repoKey1);
		Assert.assertEquals(re.getKey(), repoKey1);
		
		//check with a return value
		Long repoKey2 = repositoryManager.lookupRepositoryEntryKey(re.getOlatResource(), true);
		Assert.assertNotNull(repoKey2);
		Assert.assertEquals(re.getKey(), repoKey2);
		
		//check with a return value
		OLATResourceable dummy = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 0l);
		Long repoKey3 = repositoryManager.lookupRepositoryEntryKey(dummy, false);
		Assert.assertNull(repoKey3);
	}
	
	@Test
	public void lookupRepositoryEntries() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		//check with a return value
		List<Long> keys = Collections.singletonList(re.getKey());
		List<RepositoryEntry> entries = repositoryManager.lookupRepositoryEntries(keys);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
	}
	
	@Test(expected=AssertException.class)
	public void lookupRepositoryEntryKeyStrictFailed() {
		//check with a return value
		OLATResourceable dummy = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 0l);
		Long repoKey3 = repositoryManager.lookupRepositoryEntryKey(dummy, true);
		Assert.assertNull(repoKey3);
	}
	
	@Test
	public void lookupDisplayNameByOLATResourceableId() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(re.getOlatResource().getResourceableId());
		Assert.assertNotNull(displayName);
		Assert.assertEquals(re.getDisplayname(), displayName);
	}
	@Test
	public void lookupResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		OLATResource resource = repositoryManager.lookupRepositoryEntryResource(re.getKey());
		Assert.assertNotNull(resource);
		Assert.assertEquals(re.getOlatResource(), resource);
	}
	
	@Test
	public void lookupExistingExternalIds() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		String externalId = UUID.randomUUID().toString();
		re.setExternalId(externalId);
		repositoryService.update(re);
		dbInstance.commitAndCloseSession();
		
		//load
		String nonExistentExternalId = UUID.randomUUID().toString();
		List<String> externalIds = new ArrayList<>();
		externalIds.add(externalId);
		externalIds.add(nonExistentExternalId);
		List<String> existentExternalIds = repositoryManager.lookupExistingExternalIds(externalIds);
		Assert.assertNotNull(existentExternalIds);
		Assert.assertTrue(existentExternalIds.contains(externalId));
		Assert.assertFalse(existentExternalIds.contains(nonExistentExternalId));
	}
	
	@Test
	public void queryByOwner_replaceQueryByEditor() {
		//create a repository entry with an owner
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("re-owner-la-");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("re-participant-la-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryManager.queryByOwner(owner, true, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
		
		List<RepositoryEntry> partEntries = repositoryManager.queryByOwner(participant, true, null);
		Assert.assertNotNull(partEntries);
		Assert.assertEquals(0, partEntries.size());
	}
	
	@Test
	public void queryByOwner() {
		//create a repository entry with an owner
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-owner-la-");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(null);
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryManager.queryByOwner(id, true, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
		
		List<RepositoryEntry> entriesAlt = repositoryManager.queryByMembership(id, true, false, false, "CourseModule");
		Assert.assertNotNull(entriesAlt);
		Assert.assertEquals(1, entriesAlt.size());
		Assert.assertTrue(entriesAlt.contains(re));
	}
	
	@Test
	public void queryByMembership() {
		//create a 4 repository entries with different memberships
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-participant-la-");
		RepositoryEntry reOwner = JunitTestHelper.deployBasicCourse(null);
		repositoryEntryRelationDao.addRole(id, reOwner, GroupRoles.owner.name());
		RepositoryEntry reCoach = JunitTestHelper.deployBasicCourse(null);
		repositoryEntryRelationDao.addRole(id, reCoach, GroupRoles.coach.name());
		RepositoryEntry reParticipant = JunitTestHelper.deployBasicCourse(null);
		repositoryEntryRelationDao.addRole(id, reParticipant, GroupRoles.participant.name());
		RepositoryEntry reOut = JunitTestHelper.deployBasicCourse(null);
		Assert.assertNotNull(reOut);//add some noise
		dbInstance.commitAndCloseSession();
		
		//check single membership
		List<RepositoryEntry> ownedEntries = repositoryManager.queryByMembership(id, true, false, false, "CourseModule");
		Assert.assertNotNull(ownedEntries);
		Assert.assertEquals(1, ownedEntries.size());
		Assert.assertTrue(ownedEntries.contains(reOwner));
		
		List<RepositoryEntry> coachedEntries = repositoryManager.queryByMembership(id, false, true, false, "CourseModule");
		Assert.assertNotNull(coachedEntries);
		Assert.assertEquals(1, coachedEntries.size());
		Assert.assertTrue(coachedEntries.contains(reCoach));
		
		List<RepositoryEntry> participatingEntries = repositoryManager.queryByMembership(id, false, false, true, "CourseModule");
		Assert.assertNotNull(participatingEntries);
		Assert.assertEquals(1, participatingEntries.size());
		Assert.assertTrue(participatingEntries.contains(reParticipant));
		
		//check 2x membership
		List<RepositoryEntry> doubleEntries = repositoryManager.queryByMembership(id, true, true, false, "CourseModule");
		Assert.assertNotNull(doubleEntries);
		Assert.assertEquals(2, doubleEntries.size());
		Assert.assertTrue(doubleEntries.contains(reOwner));
		Assert.assertTrue(doubleEntries.contains(reCoach));
		
		//check 3x membership
		List<RepositoryEntry> tripleEntries = repositoryManager.queryByMembership(id, true, true, true, "CourseModule");
		Assert.assertNotNull(tripleEntries);
		Assert.assertEquals(3, tripleEntries.size());
		Assert.assertTrue(tripleEntries.contains(reOwner));
		Assert.assertTrue(tripleEntries.contains(reCoach));
		Assert.assertTrue(tripleEntries.contains(reParticipant));
		
		//dummy
		List<RepositoryEntry> noEntries = repositoryManager.queryByMembership(id, false, false, false, "CourseModule");
		Assert.assertNotNull(noEntries);
		Assert.assertTrue(noEntries.isEmpty());
	}
	
	@Test
	public void queryByOwnerLimitAccess() {
		//create a repository entry with an owner
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-owner-la-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryManager.queryByOwnerLimitAccess(id);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
	}
	
	@Test
	public void queryByInitialAuthor() {
		String initialAuthor = UUID.randomUUID().toString();
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(initialAuthor, false);
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> reList = repositoryManager.queryByInitialAuthor(initialAuthor);
		Assert.assertNotNull(reList);
		Assert.assertEquals(1, reList.size());
		Assert.assertEquals(re, reList.get(0));
	}
	
	@Test
	public void getLearningResourcesAsStudent() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-la-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsStudent(id, null, 0, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	@Test
	public void getLearningResourcesAsStudentWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-lb-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "studg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsStudent(id, null, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	@Test
	public void getLearningResourcesAsParticipantAndCoach() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-lb-");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-lb-");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(owner);
		BusinessGroup group = businessGroupService.createBusinessGroup(owner, "studg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsParticipantAndCoach(id, "CourseModule");
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}

	@Test
	public void getLearningResourcesAsBookmark() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-2");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(owner);
		markManager.setMark(course, participant, null, "[RepositoryEntry:" + course.getKey() + "]");
		repositoryEntryRelationDao.addRole(participant, course, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//participant bookmarks
		Roles roles = Roles.userRoles();
		List<RepositoryEntry> courses = repositoryManager.getLearningResourcesAsBookmarkedMember(participant, roles, "CourseModule", 0, -1);
		Assert.assertNotNull(courses);
		Assert.assertEquals(1, courses.size());
	}
	
	/**
	 * Check that the method return only courses within the permissions of the user.
	 */
	@Test
	public void getLearningResourcesAsBookmark_noPermissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-2");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(owner);
		markManager.setMark(course, participant, null, "[RepositoryEntry:" + course.getKey() + "]");
		dbInstance.commitAndCloseSession();
		repositoryManager.setAccess(course, RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		
		//participant bookmarks
		Roles roles = Roles.userRoles();
		List<RepositoryEntry> courses = repositoryManager.getLearningResourcesAsBookmarkedMember(participant, roles, "CourseModule", 0, -1);
		Assert.assertNotNull(courses);
		Assert.assertEquals(0, courses.size());
	}
	
	/**
	 * Check that the method return only courses within the permissions of the user.
	 */
	@Test
	public void getLearningResourcesAsBookmark_notPublished() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-2");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(owner);
		markManager.setMark(course, participant, null, "[RepositoryEntry:" + course.getKey() + "]");
		repositoryEntryRelationDao.addRole(participant, course, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		repositoryManager.setAccess(course, RepositoryEntryStatusEnum.coachpublished, false, false);
		dbInstance.commitAndCloseSession();
		
		//participant bookmarks
		Roles roles = Roles.userRoles();
		List<RepositoryEntry> courses = repositoryManager.getLearningResourcesAsBookmarkedMember(participant, roles, "CourseModule", 0, -1);
		Assert.assertNotNull(courses);
		Assert.assertEquals(0, courses.size());
	}
	
	/**
	 * Check that the method return only courses within the permissions of the user.
	 */
	@Test
	public void getLearningResourcesAsBookmark_noPermissionsBookable() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-courses-2");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(owner);
		markManager.setMark(course, participant, null, "[RepositoryEntry:" + course.getKey() + "]");
		repositoryManager.setAccess(course, false, false, true, RepositoryEntryAllowToLeaveOptions.never, null);
		repositoryManager.setAccess(course, RepositoryEntryStatusEnum.published, false, false);
		
		//create and save an offer
		Offer offer = acService.createOffer(course.getOlatResource(), course.getDisplayname());
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//participant bookmarks
		Roles roles = Roles.userRoles();
		List<RepositoryEntry> courses = repositoryManager.getLearningResourcesAsBookmarkedMember(participant, roles, "CourseModule", 0, -1);
		Assert.assertNotNull(courses);
		Assert.assertEquals(0, courses.size());
		
		// beacause it triggers issues with other tests
		repositoryManager.setAccess(course, RepositoryEntryStatusEnum.preparation, false, false);
		
	}
	
	@Test
	public void getParticipantRepositoryEntry() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-lc-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getParticipantRepositoryEntry(id, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		
		boolean found = false;
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(entry.getKey().equals(re.getKey())) {
				found = true;
			}
		
			if(entry.isAllUsers()) {
				//OK
			} else if(entry.getEntryStatus() == RepositoryEntryStatusEnum.published
					|| entry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
				RepositoryEntry reloadedRe = repositoryManager.lookupRepositoryEntry(entry.getKey());
				boolean member = repositoryEntryRelationDao.hasRole(id, reloadedRe, GroupRoles.participant.name());
				Assert.assertTrue(member);
			} else {
				Assert.fail();
			}
		}
		
		Assert.assertTrue(found);
	}
	
	@Test
	public void getParticipantRepositoryEntry_notPublished() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-le-");
		RepositoryEntry reNotPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryEntryRelationDao.addRole(id, reNotPublished, GroupRoles.participant.name());
		repositoryManager.setAccess(reNotPublished, RepositoryEntryStatusEnum.coachpublished, true, true);
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getParticipantRepositoryEntry(id, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.contains(reNotPublished));
		
		// check access
		for(RepositoryEntry entry:entries) {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(id, Roles.userRoles(), entry);
			Assert.assertTrue(reSecurity.canLaunch());
		}
	}
	
	@Test
	public void getParticipantRepositoryEntry_forAll() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-le-");
		RepositoryEntry reNotPublished = JunitTestHelper.createAndPersistRepositoryEntry(true);
		repositoryManager.setAccess(reNotPublished, RepositoryEntryStatusEnum.published, true, true);
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getParticipantRepositoryEntry(id, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertTrue(entries.contains(reNotPublished));
		log.info("Num. of entries: {}", entries.size());
		
		// check access
		for(RepositoryEntry entry:entries) {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(id, Roles.userRoles(), entry);
			Assert.assertTrue(reSecurity.canLaunch());
		}
	}
	
	@Test
	public void getParticipantRepositoryEntryWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-stud-ld-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "studh", "th", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getParticipantRepositoryEntry(id, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		
		boolean found = false;
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			
			if(entry.getKey().equals(re.getKey())) {
				found = true;
			}
		}
		
		Assert.assertTrue(found);
	}
	
	@Test
	public void getLearningResourcesAsTeacher() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-teac-la-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsTeacher(id, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	@Test
	public void getLearningResourcesAsTeacherWithGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-teac-lb-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "teacherg", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(id, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> entries = repositoryManager.getLearningResourcesAsTeacher(id, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertFalse(entries.isEmpty());
		Assert.assertTrue(entries.contains(re));
		
		Set<Long> duplicates = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			Assert.assertTrue(duplicates.add(entry.getKey()));
			if(!entry.equals(re)) {
				Assert.assertTrue(entry.isAllUsers());
				Assert.assertTrue(entry.getEntryStatus().ordinal() >= RepositoryEntryStatusEnum.published.ordinal());
			}
		}
	}
	
	@Test
	public void getFavoritLearningResourcesAsTeacher() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-fav-1-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		markManager.setMark(re, id, null, "[RepositoryEntry:" + re.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		//check get forbidden favorit
		List<RepositoryEntry> forbiddenEntries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, null, 0, -1);
		Assert.assertNotNull(forbiddenEntries);
		Assert.assertEquals(0, forbiddenEntries.size());
		int countForbiddenEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, null);
		Assert.assertEquals(0, countForbiddenEntries);
		
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check get favorit
		List<RepositoryEntry> entries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, null, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
		
		//check count
		int countEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, null);
		Assert.assertEquals(1, countEntries);
	}
	
	@Test
	public void getFavoritLearningResourcesAsTeacher_restrictedTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-fav-1-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		markManager.setMark(re, id, null, "[RepositoryEntry:" + re.getKey() + "]");
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check get favorite
		List<String> types = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, types, 0, -1, RepositoryEntryOrder.nameAsc);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(re));
		
		//check count
		int countEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, types);
		Assert.assertEquals(1, countEntries);
	}
	
	@Test
	public void getFavoritLearningResourcesAsTeacher_negativeTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-fav-1-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		markManager.setMark(re, id, null, "[RepositoryEntry:" + re.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		//check get favorite
		List<String> types = Collections.singletonList("CourseModule");
		List<RepositoryEntry> entries = repositoryManager.getFavoritLearningResourcesAsTeacher(id, types, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(0, entries.size());
		
		//check count
		int countEntries = repositoryManager.countFavoritLearningResourcesAsTeacher(id, types);
		Assert.assertEquals(0, countEntries);
	}
	
	@Test
	public void queryResourcesLimitType() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("re-member-lc-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		List<String> resourceTypes = Collections.singletonList(re.getOlatResource().getResourceableTypeName());
		List<RepositoryEntry> entries = repositoryManager
				.queryResourcesLimitType(id, Roles.authorRoles(), false, resourceTypes, "re-member", "me", "no", id, true, true);
		Assert.assertNotNull(entries);
	}
	
	@Test
	public void queryReferencableResourcesLimitType() {
		String resourceType = UUID.randomUUID().toString().replace("_", "");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id2");
		Roles id1Roles = securityManager.getRoles(id1);

		// generate 5000 repo entries
		int numbRes = 500;
		long startCreate = System.currentTimeMillis();
		for (int i = 1; i < numbRes; i++) {
			// create course and persist as OLATResourceImpl
			Identity owner = (i % 2 > 0) ? id1 : id2;

			OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(resourceType, Long.valueOf(i));
			OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(resourceable);
			dbInstance.getCurrentEntityManager().persist(r);
			
			// now make a repository entry for this course
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			RepositoryEntry re = repositoryService.create(owner, null, "Lernen mit OLAT " + i, "JunitTest_RepositoryEntry_" + i, "yo man description bla bla + i",
					r, RepositoryEntryStatusEnum.review, defOrganisation);			
			if ((i % 2 > 0)) {
				re.setCanReference(true);
			}
			// save the repository entry
			repositoryService.update(re);

			// flush database and hibernate session cache after 10 records to improve performance
			// without this optimization, the first entries will be fast but then the adding new 
			// entries will slow down due to the fact that hibernate needs to adjust the size of
			// the session cache permanently. flushing or transactions won't help since the problem
			// is in the session cache. 
			if (i%10 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		long endCreate = System.currentTimeMillis();
		log.info("created " + numbRes + " repo entries in " + (endCreate - startCreate) + "ms");
		
		List<String> typelist = Collections.singletonList(resourceType);
		// finally the search query
		long startSearchReferencable = System.currentTimeMillis();
		List<RepositoryEntry> results = repositoryManager.queryResourcesLimitType(id1, id1Roles, false, typelist,
				null, null, null, null, true, false);
		long endSearchReferencable = System.currentTimeMillis();
		log.info("found " + results.size() + " repo entries " + (endSearchReferencable - startSearchReferencable) + "ms");

		// only half of the items should be found
		Assert.assertEquals(numbRes / 2, results.size());
		
		// inserting must take longer than searching, otherwhise most certainly we have a problem somewhere in the query
		Assert.assertTrue((endCreate - startCreate) > (endSearchReferencable - startSearchReferencable));
	}
	
	@Test
	public void countLearningResourcesAsStudent() {
		//create a repository entry with an owner and a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("re-participant-is-");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(owner);
		dbInstance.commitAndCloseSession();
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check
		int count = repositoryManager.countLearningResourcesAsStudent(owner, "CourseModule");
		Assert.assertTrue(1 <= count);
	}
	
	@Test
	public void getRepositoryentryMembership() {
		//create a repository entry with an owner and a participant
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-m-is1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-m-is2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-m-is3-");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-m-is4-");
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-m-is5-");
		Identity id6 = JunitTestHelper.createAndPersistIdentityAsRndUser("re-m-is6-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		if(repositoryEntryRelationDao.hasRole(admin, re, GroupRoles.owner.name())) {
			repositoryEntryRelationDao.removeRole(admin, re, GroupRoles.owner.name());
		}
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id4, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id5, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id6, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.participant.name());
		
		dbInstance.commitAndCloseSession();
		
		Set<Long> identityKeys = new HashSet<>();
		identityKeys.add(id1.getKey());
		identityKeys.add(id2.getKey());
		identityKeys.add(id3.getKey());
		identityKeys.add(id4.getKey());
		identityKeys.add(id5.getKey());
		identityKeys.add(id6.getKey());
		
		//check with all identities
		List<RepositoryEntryMembership> memberships = repositoryManager.getRepositoryEntryMembership(re);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(6, memberships.size());
		
		int countOwner = 0;
		int countTutor = 0;
		int countParticipant = 0;
		for(RepositoryEntryMembership membership:memberships) {
			if(membership.isOwner()) {
				countOwner++;
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			}
			if (membership.isCoach()) {
				countTutor++;
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			}
			if (membership.isParticipant()) {
				countParticipant++;
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			}
			Assert.assertTrue(identityKeys.contains(membership.getIdentityKey()));
		}
		Assert.assertEquals(2, countOwner);
		Assert.assertEquals(2, countTutor);
		Assert.assertEquals(3, countParticipant);
		
		//check with id1
		List<RepositoryEntryMembership> membership1s = repositoryManager.getRepositoryEntryMembership(re, id1);
		Assert.assertNotNull(membership1s);
		Assert.assertEquals(2, membership1s.size());
		for(RepositoryEntryMembership membership:membership1s) {
			if(membership.isOwner()) {
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			} else if (membership.isParticipant()) {
				Assert.assertEquals(re.getKey(), membership.getRepoKey());
			} else {
				Assert.assertTrue(false);
			}
			Assert.assertEquals(id1.getKey(), membership.getIdentityKey());
		}
	}
	
	@Test
	public void getRepositoryentryMembershipAgainstDummy() {
		//no repo, no identities
		List<RepositoryEntryMembership> membership2s = repositoryManager.getRepositoryEntryMembership(null);
		Assert.assertNotNull(membership2s);
		Assert.assertTrue(membership2s.isEmpty());
	}

	@Test
	public void testCountByTypeLimitAccess() {
		String TYPE = UUID.randomUUID().toString().replace("-", "");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("re-gen-1");
		
		int count = repositoryManager.countByType("unkown");
		Assert.assertEquals("Unkown type must return 0 elements", 0,count);
		int countValueBefore = repositoryManager.countByType(TYPE);
		// add 1 entry
		RepositoryEntry re = createRepositoryEntry(TYPE, owner, 999999l);
		// create security group
		repositoryService.update(re);
		count = repositoryManager.countByType(TYPE);
		// check count must be one more element
		Assert.assertEquals("Add one course repository-entry, but countByTypeLimitAccess does NOT return one more element", countValueBefore + 1,count);
	}
	
	@Test
	public void setDescriptionAndName() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		String newName = "Brand new name";
		String newDesc = "Brand new description";
		String newAuthors = "Me and only me";
		String newLocation = "Far away";
		String newExternalId = "Brand - ext";
		String newExternalRef = "Brand - ref";
		String newManagedFlags = RepositoryEntryManagedFlag.access.name();
		
		RepositoryEntryLifecycle newCycle
			= lifecycleDao.create("New cycle 1", "New cycle soft 1", false, new Date(), new Date());
		
		re = repositoryManager.setDescriptionAndName(re, newName, newDesc, newLocation, newAuthors, newExternalId, newExternalRef, newManagedFlags, newCycle);
		Assert.assertNotNull(re);
		
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reloaded = repositoryManager.lookupRepositoryEntry(re.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals("Me and only me", reloaded.getAuthors());
		Assert.assertEquals("Far away", reloaded.getLocation());
		Assert.assertEquals("Brand new name", reloaded.getDisplayname());
		Assert.assertEquals("Brand new description", reloaded.getDescription());
		Assert.assertEquals("Brand - ext", reloaded.getExternalId());
		Assert.assertEquals("Brand - ref", reloaded.getExternalRef());
		Assert.assertEquals(RepositoryEntryManagedFlag.access.name(), reloaded.getManagedFlagsString());
		Assert.assertNotNull(reloaded.getLifecycle());
		Assert.assertEquals(newCycle, reloaded.getLifecycle());
	}
	
	@Test
	public void setDescriptionAndName_lifecycle() {
		RepositoryEntryLifecycle publicCycle
			= lifecycleDao.create("Public 1", "Soft public 1", false, new Date(), new Date());

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		String newName = "Brand new name";
		String newDesc = "Brand new description";
		re = repositoryManager.setDescriptionAndName(re, newName, null, null, newDesc, null, null, null, null, null, null, publicCycle, null, null, null);
		Assert.assertNotNull(re);
		
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reloaded = repositoryManager.lookupRepositoryEntry(re.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals("Brand new name", reloaded.getDisplayname());
		Assert.assertEquals("Brand new description", reloaded.getDescription());
		Assert.assertEquals(publicCycle, reloaded.getLifecycle());
	}
	
	@Test
	public void setDescriptionAndName_organisations() {
		RepositoryEntryLifecycle publicCycle
			= lifecycleDao.create("Public 2", "Soft public 2", false, new Date(), new Date());
		Organisation organisation = organisationService.getDefaultOrganisation();

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		String newName = "Organized name";
		String newDesc = "Organized description";
		re = repositoryManager.setDescriptionAndName(re, newName, null, null, newDesc, null, null, null, null, null, null,
				publicCycle, Collections.singletonList(organisation), null, null);
		Assert.assertNotNull(re);
		Assert.assertEquals(2, re.getGroups().size());// check repository entry to group relations
		
		dbInstance.commitAndCloseSession();
		
		// reload and check twice
		RepositoryEntry reloaded = repositoryManager.lookupRepositoryEntry(re.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals("Organized name", reloaded.getDisplayname());
		Assert.assertEquals("Organized description", reloaded.getDescription());
		Assert.assertEquals(publicCycle, reloaded.getLifecycle());
		Assert.assertEquals(1, reloaded.getOrganisations().size());// check repository entry to organization relations
		Assert.assertEquals(2, reloaded.getGroups().size());// check repository entry to group relations
	}

	@Test
	public void setAllowToLeaveOption() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		RepositoryEntry updatedRe = repositoryManager.setLeaveSetting(re, RepositoryEntryAllowToLeaveOptions.never);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(updatedRe);
		Assert.assertEquals(re, updatedRe);
		Assert.assertEquals(RepositoryEntryAllowToLeaveOptions.never, updatedRe.getAllowToLeaveOption());
	}
	
	@Test
	public void isParticipantAllowedToLeave() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		re = repositoryManager.setLeaveSetting(re, RepositoryEntryAllowToLeaveOptions.never);
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(repositoryService.isParticipantAllowedToLeave(re));
	}
	
	@Test
	public void isAllowed_coach() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("allowed-re-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commit();
		re = repositoryManager.setAccess(re, RepositoryEntryStatusEnum.published, false, false);
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		Roles roles = Roles.userRoles();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(coach, roles, re);
		Assert.assertTrue(reSecurity.canLaunch());
	}
	
	@Test
	public void isAllowed_coachWithAuthorRoles() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("allowed-re-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commit();
		re = repositoryManager.setAccess(re, RepositoryEntryStatusEnum.published, false, false);
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		Roles roles = Roles.userRoles();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(coach, roles, re);
		Assert.assertTrue(reSecurity.canLaunch());
	}
	
	
	/**
	 * Author is not allowed to launch it
	 */
	@Test
	public void isAllowed_authorRoles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("allowed-re-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commit();
		re = repositoryManager.setAccess(re, RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		
		Roles roles = Roles.authorRoles();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(author, roles, re);
		Assert.assertFalse(reSecurity.canLaunch());
	}
	
	@Test
	public void isAllowed_authorRoles_canReference() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("allowed-re-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commit();
		re = repositoryManager.setAccess(re, RepositoryEntryStatusEnum.review, false, false);
		re = repositoryManager.setAccess(re, false, true, false);
		dbInstance.commitAndCloseSession();
		
		Roles roles = Roles.authorRoles();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(author, roles, re);
		Assert.assertTrue(reSecurity.canLaunch());
	}
	
	
	@Test
	public void leave_simpleRepositoryEnty() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-re-1");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-re-2");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-re-3");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(participant, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		//participant leave
		LeavingStatusList status = new LeavingStatusList();
		repositoryManager.leave(participant, re, status, null);
		dbInstance.commit();
		Assert.assertFalse(repositoryService.isMember(participant, re));
		
		//coach and owner can't leave
		repositoryManager.leave(coach, re, status, null);
		dbInstance.commit();
		Assert.assertTrue(repositoryService.isMember(coach, re));
		repositoryManager.leave(owner, re, status, null);
		dbInstance.commit();
		Assert.assertTrue(repositoryService.isMember(owner, re));
	}
	
	@Test
	public void leave_withGroups() {
		//create 2 entries and 2 groups
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-re-4");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-re-5");
		
		//entry 1 is linked to the 2 groups
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(participant, re1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(owner, re1, GroupRoles.owner.name());
		
		//entry 2 is only linked to group 2
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		
		BusinessGroup group1 = businessGroupService.createBusinessGroup(owner, "leaving-group-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
	    businessGroupRelationDao.addRole(participant, group1, GroupRoles.participant.name());
		
	    BusinessGroup group2 = businessGroupService.createBusinessGroup(owner, "leaving-group-2", "tg", BusinessGroup.BUSINESS_TYPE,
	    		null, null, false, false, re1);
	    businessGroupRelationDao.addRole(participant, group2, GroupRoles.participant.name());
	    businessGroupRelationDao.addRelationToResource(group2, re2);
		dbInstance.commitAndCloseSession();
		
		//participant leave
		LeavingStatusList status = new LeavingStatusList();
		repositoryManager.leave(participant, re1, status, null);
		businessGroupService.leave(participant, re1, status, null);
		dbInstance.commit();
		
		//participant is removed from entry 1, group 1 but not group 2 because this group is linked to entry 2 too
		Assert.assertTrue(repositoryService.isMember(participant, re1));
		//but removed from re
		boolean re1Role = repositoryEntryRelationDao.hasRole(participant, re1, GroupRoles.participant.name());
		Assert.assertFalse(re1Role);
		boolean group1Role = businessGroupRelationDao.hasRole(participant, group1, GroupRoles.participant.name());
		Assert.assertFalse(group1Role);
		boolean group2Role = businessGroupRelationDao.hasRole(participant, group2, GroupRoles.participant.name());
		Assert.assertTrue(group2Role);
		
		//owner are never remove (double check)
		Assert.assertTrue(repositoryService.isMember(owner, re1));
		//but removed from re
		boolean re1OwnerRole = repositoryEntryRelationDao.hasRole(owner, re1, GroupRoles.owner.name());
		Assert.assertTrue(re1OwnerRole);
		boolean group1CoachRole = businessGroupRelationDao.hasRole(owner, group1, GroupRoles.coach.name());
		Assert.assertTrue(group1CoachRole);
		boolean group2CoachRole = businessGroupRelationDao.hasRole(owner, group2, GroupRoles.coach.name());
		Assert.assertTrue(group2CoachRole);
	}
	
	/**
	 * This is a simulation of OO-2667 to make sure that the LazyInitializationException don't
	 * set the transaction on rollback.
	 */
	@Test
	public void lazyLoadingCheck() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 5", "",
				null, RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryLifecycle cycle = lifecycleDao.create("New cycle 1", "New cycle soft 1", false, new Date(), new Date());
		re = repositoryManager.setDescriptionAndName(re, "Updated repo entry", null, null, "", null, null, null, null, null, null, cycle, null, null, null);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry lazyRe = repositoryManager.setAccess(re, RepositoryEntryStatusEnum.review, false, false);
		dbInstance.commitAndCloseSession();
		
		try {// produce the exception
			lazyRe.getLifecycle().getValidFrom();
			Assert.fail();
		} catch (LazyInitializationException e) {
			//
		}
		
		//load a fresh entry
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(lazyRe.getKey());
		Date validFrom = entry.getLifecycle().getValidFrom();
		Assert.assertNotNull(validFrom);
		dbInstance.commitAndCloseSession();
	}

	private RepositoryEntry createRepositoryEntry(final String type, Identity owner, long i) {
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(type, Long.valueOf(i));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);
		
		// now make a repository entry for this course
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		return repositoryService.create(owner, null,
				"Lernen mit OLAT " + i, "JunitTest_RepositoryEntry_" + i, "yo man description bla bla + i",
				r, RepositoryEntryStatusEnum.review, defOrganisation);
	}
}
