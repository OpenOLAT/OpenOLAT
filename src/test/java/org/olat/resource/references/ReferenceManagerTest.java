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

package org.olat.resource.references;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 */
public class ReferenceManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void testAddReference() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget = JunitTestHelper.createRandomResource();
		String udata = "üserdätä";
		
		// add a reference
		referenceManager.addReference(oressource, orestarget, udata);
		dbInstance.commitAndCloseSession();
		
		OLATResourceable targetOres = OresHelper.createOLATResourceableInstance(orestarget.getResourceableTypeName(), orestarget.getResourceableId());
		OLATResource orestarget2 = resourceManager.findOrPersistResourceable(targetOres);
		List<Reference> refs = referenceManager.getReferencesTo(orestarget2);
		Assert.assertNotNull(refs);
		Assert.assertEquals("only one reference may exist", 1, refs.size());
		Assert.assertEquals(oressource, refs.get(0).getSource());
	}
	
	@Test
	public void testReferencesToAndFrom() {
		// same resouceable id on purpose
		OLATResource s1 = JunitTestHelper.createRandomResource();
		OLATResource s2 = JunitTestHelper.createRandomResource();
		OLATResource s3 = JunitTestHelper.createRandomResource();
		OLATResource t1 = JunitTestHelper.createRandomResource();
		OLATResource t2 = JunitTestHelper.createRandomResource();
		
		// add references
		referenceManager.addReference(s1,t1,"r11");
		referenceManager.addReference(s2,t1,"r21");
		referenceManager.addReference(s2,t2,"r22");
		referenceManager.addReference(s3,t2,"r32");
		
		dbInstance.commitAndCloseSession();

		// find the refs again with DB resource
		List<Reference> s1R =referenceManager.getReferences(s1);
		Assert.assertEquals("s1 only has one reference", 1, s1R.size());
		Reference ref = s1R.get(0);
		Assert.assertEquals("source and s1 the same", s1, ref.getSource());
		Assert.assertEquals("target and t1 the same", t1, ref.getTarget());
		
		// find the same refs again with  resourceable
		OLATResourceable s1Ores = OresHelper.createOLATResourceableInstance(s1.getResourceableTypeName(), s1.getResourceableId());
		List<Reference> s1Rb =referenceManager.getReferences(s1Ores);
		Assert.assertEquals("s1 only has one reference", 1, s1Rb.size());
		Reference refb = s1R.get(0);
		Assert.assertEquals("source and s1 the same", s1, refb.getSource());
		Assert.assertEquals("target and t1 the same", t1, refb.getTarget());
		
		// two refs from s2
		List<Reference> s2refs = referenceManager.getReferences(s2);
		Assert.assertEquals("s2 holds two refs (to t1 and t2)", 2, s2refs.size());
		
		// two refs to t2
		List<Reference> t2refs = referenceManager.getReferencesTo(t2);
		Assert.assertEquals("t2 holds two source refs (to s2 and s3)", 2, t2refs.size());		
	}
	
	@Test
	public void testAddAndDeleteReference() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget = JunitTestHelper.createRandomResource();
		String udata = "üserdätä";
		// add a reference
		referenceManager.addReference(oressource, orestarget, udata);
		dbInstance.commitAndCloseSession();
				
		OLATResourceable orestarget2 = OresHelper.createOLATResourceableInstance(orestarget.getResourceableTypeName(), orestarget.getResourceableId());
		List<Reference> refs = referenceManager.getReferencesTo(orestarget2);
		Assert.assertEquals("only one reference may exist", 1, refs.size());
		dbInstance.commitAndCloseSession();
		
		for (Reference ref : refs) {
			referenceManager.delete(ref);
		}
		dbInstance.commitAndCloseSession();

		// now make sure the reference was deleted
		OLATResourceable orestarget3 = OresHelper.createOLATResourceableInstance(orestarget.getResourceableTypeName(), orestarget.getResourceableId());
		List<Reference> norefs = referenceManager.getReferencesTo(orestarget3);
		assertTrue("reference should now be deleted", norefs.isEmpty());
	}
	
	@Test
	public void testAddAndDeleteAllReferences() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget1 = JunitTestHelper.createRandomResource();
		OLATResource orestarget2 = JunitTestHelper.createRandomResource();
		// add the references
		referenceManager.addReference(oressource, orestarget1, "üserdätä");
		referenceManager.addReference(oressource, orestarget2, "éserdàtà");
		dbInstance.commitAndCloseSession();
				
		List<Reference> refs = referenceManager.getReferences(oressource);
		Assert.assertEquals("2 references exist", 2, refs.size());
		dbInstance.commitAndCloseSession();
		
		referenceManager.deleteAllReferencesOf(oressource);
		dbInstance.commitAndCloseSession();

		// now make sure the reference was deleted

		List<Reference> norefs = referenceManager.getReferences(oressource);
		assertTrue("reference should now be deleted", norefs.isEmpty());
	}
	
	@Test
	public void testAddAndDeleteAllReferences_parano() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget1 = JunitTestHelper.createRandomResource();
		OLATResource orestarget2 = JunitTestHelper.createRandomResource();
		OLATResource orestarget3 = JunitTestHelper.createRandomResource();
		// add the references
		referenceManager.addReference(oressource, orestarget1, "üserdätä");
		referenceManager.addReference(orestarget2, orestarget1, "üserdätä");
		referenceManager.addReference(oressource, orestarget2, "éserdàtà");
		referenceManager.addReference(orestarget2, oressource, "éserdàtà");
		referenceManager.addReference(orestarget3, oressource, "éserdàtà");
		referenceManager.addReference(orestarget3, orestarget1, "éserdàtà");
		dbInstance.commitAndCloseSession();
				
		List<Reference> refs = referenceManager.getReferences(oressource);
		Assert.assertEquals("2 references exist", 2, refs.size());
		List<Reference> reverseRefs = referenceManager.getReferencesTo(oressource);
		Assert.assertEquals("2 references exist", 2, reverseRefs.size());

		referenceManager.deleteAllReferencesOf(oressource);
		dbInstance.commitAndCloseSession();

		// now make sure the reference was deleted
		List<Reference> norefs = referenceManager.getReferences(oressource);
		Assert.assertTrue("reference should now be deleted", norefs.isEmpty());
		
		//check target 3 has 1
		List<Reference> refTarget3s = referenceManager.getReferences(orestarget3);
		Assert.assertEquals(1, refTarget3s.size());
		Assert.assertEquals(orestarget1, refTarget3s.get(0).getTarget());
		
		//check target 2 has 1
		List<Reference> refTarget2s = referenceManager.getReferences(orestarget2);
		Assert.assertEquals(1, refTarget2s.size());
		Assert.assertEquals(orestarget1, refTarget2s.get(0).getTarget());
	}
	
	@Test
	public void getReferencesInfos_simpleCase() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Asuka");
		Roles adminRoles = new Roles(true, false, false, false, false, false, false);

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry course1 = repositoryService.create(null,"Asuka Langley", "-", "Reference Manager course 1", "", null, 0, defOrganisation);
		RepositoryEntry course2 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 2", "", null, 0, defOrganisation);
		RepositoryEntry test = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test ", "", null, 0, defOrganisation);
		// add the references
		referenceManager.addReference(course1.getOlatResource(), test.getOlatResource(), "86234");
		referenceManager.addReference(course2.getOlatResource(), test.getOlatResource(), "78437590");
		dbInstance.commitAndCloseSession();
		
		//ref of course 1
		List<ReferenceInfos> refCourse1s = referenceManager.getReferencesInfos(Collections.singletonList(course1), id, adminRoles);
		Assert.assertNotNull(refCourse1s);
		Assert.assertEquals(1, refCourse1s.size());
		ReferenceInfos ref = refCourse1s.get(0);
		Assert.assertEquals(test, ref.getEntry());
		Assert.assertFalse(ref.isOrphan());
		Assert.assertTrue(ref.isOwner());
		
		//ref of course 1 and 2
		List<RepositoryEntry> courses = new ArrayList<>(2);
		courses.add(course1);
		courses.add(course2);
		List<ReferenceInfos> refCourse1and2s = referenceManager.getReferencesInfos(courses, id, adminRoles);
		Assert.assertNotNull(refCourse1and2s);
		Assert.assertEquals(1, refCourse1and2s.size());
		ReferenceInfos ref1nd2 = refCourse1and2s.get(0);
		Assert.assertEquals(test, ref1nd2.getEntry());
		Assert.assertTrue(ref1nd2.isOrphan());
		Assert.assertTrue(ref1nd2.isOwner());
		
		//ref empty
		List<RepositoryEntry> emptyList = new ArrayList<>(2);
		List<ReferenceInfos> emptyRefList = referenceManager.getReferencesInfos(emptyList, id, adminRoles);
		Assert.assertNotNull(emptyRefList);
		Assert.assertEquals(0, emptyRefList.size());
	}
	
	@Test
	public void getReferencesInfos_difficultCase() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Asuka");
		Roles adminRoles = new Roles(true, false, false, false, false, false, false);

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry course1 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 1", "", null, 0, defOrganisation);
		RepositoryEntry course2 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 2", "", null, 0, defOrganisation);
		RepositoryEntry course3 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 3", "", null, 0, defOrganisation);
		RepositoryEntry course4 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 4", "", null, 0, defOrganisation);
		RepositoryEntry test12 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test 12", "", null, 0, defOrganisation);
		RepositoryEntry test2 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test 2", "", null, 0, defOrganisation);
		RepositoryEntry test234 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test 234", "", null, 0, defOrganisation);
		// add the references
		referenceManager.addReference(course1.getOlatResource(), test12.getOlatResource(), "45345");
		referenceManager.addReference(course2.getOlatResource(), test12.getOlatResource(), "453421");
		referenceManager.addReference(course2.getOlatResource(), test2.getOlatResource(), "678678");
		referenceManager.addReference(course2.getOlatResource(), test234.getOlatResource(), "178942");
		referenceManager.addReference(course3.getOlatResource(), test234.getOlatResource(), "479532");
		referenceManager.addReference(course4.getOlatResource(), test234.getOlatResource(), "57892386");
		dbInstance.commitAndCloseSession();
		
		//course 1,2
		List<RepositoryEntry> courses12 = new ArrayList<>(2);
		courses12.add(course1);
		courses12.add(course2);
		List<ReferenceInfos> refCourses12 = referenceManager.getReferencesInfos(courses12, id, adminRoles);
		Assert.assertNotNull(refCourses12);
		Assert.assertEquals(3, refCourses12.size());
		//test12
		ReferenceInfos refCourses12_test12 = getReferenceInfos(refCourses12, test12);
		Assert.assertNotNull(refCourses12_test12);
		Assert.assertTrue(refCourses12_test12.isOrphan());
		Assert.assertTrue(refCourses12_test12.isOwner());
		//test 2
		ReferenceInfos refCourses12_test2 = getReferenceInfos(refCourses12, test2);
		Assert.assertNotNull(refCourses12_test2);
		Assert.assertTrue(refCourses12_test2.isOrphan());
		Assert.assertTrue(refCourses12_test2.isOwner());
		//test234
		ReferenceInfos refCourses12_test234 = getReferenceInfos(refCourses12, test234);
		Assert.assertNotNull(refCourses12_test234);
		Assert.assertFalse(refCourses12_test234.isOrphan());
		Assert.assertTrue(refCourses12_test234.isOwner());
		
		//course 2
		List<RepositoryEntry> courses2 = new ArrayList<>(2);
		courses2.add(course2);
		List<ReferenceInfos> refCourses2 = referenceManager.getReferencesInfos(courses2, id, adminRoles);
		Assert.assertNotNull(refCourses2);
		Assert.assertEquals(3, refCourses2.size());
		//test12
		ReferenceInfos refCourses2_test12 = getReferenceInfos(refCourses2, test12);
		Assert.assertNotNull(refCourses2_test12);
		Assert.assertFalse(refCourses2_test12.isOrphan());
		Assert.assertTrue(refCourses2_test12.isOwner());
		//test 2
		ReferenceInfos refCourses2_test2 = getReferenceInfos(refCourses2, test2);
		Assert.assertNotNull(refCourses2_test2);
		Assert.assertTrue(refCourses2_test2.isOrphan());
		Assert.assertTrue(refCourses2_test2.isOwner());
		//test234
		ReferenceInfos refCourses2_test234 = getReferenceInfos(refCourses2, test234);
		Assert.assertNotNull(refCourses2_test234);
		Assert.assertFalse(refCourses2_test234.isOrphan());
		Assert.assertTrue(refCourses2_test234.isOwner());
		
		//course 4
		List<RepositoryEntry> courses4 = new ArrayList<>(2);
		courses4.add(course4);
		List<ReferenceInfos> refCourses4 = referenceManager.getReferencesInfos(courses4, id, adminRoles);
		Assert.assertNotNull(refCourses4);
		Assert.assertEquals(1, refCourses4.size());
		//test234
		ReferenceInfos refCourses4_test234 = getReferenceInfos(refCourses4, test234);
		Assert.assertNotNull(refCourses4_test234);
		Assert.assertFalse(refCourses4_test234.isOrphan());
		Assert.assertTrue(refCourses4_test234.isOwner());
	}
	
	@Test
	public void getReferencesInfos_permission() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Asuka");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Rei");
		Roles roles = new Roles(false, false, false, false, false, false, false);

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry course1 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 1 permission", "", null, 0, defOrganisation);
		RepositoryEntry course2 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 2 permission", "", null, 0, defOrganisation);
		RepositoryEntry course3 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 3 permission", "", null, 0, defOrganisation);
		RepositoryEntry course4 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager course 4 permission", "", null, 0, defOrganisation);
		RepositoryEntry test12 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test 12 permission", "", null, 0, defOrganisation);
		repositoryEntryRelationDao.addRole(id1, test12, GroupRoles.owner.name());
		RepositoryEntry test2 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test 2 permission", "", null, 0, defOrganisation);
		repositoryEntryRelationDao.addRole(id2, test2, GroupRoles.owner.name());
		RepositoryEntry test234 = repositoryService.create(null, "Asuka Langley", "-", "Reference Manager test 234 permission", "", null, 0, defOrganisation);
		repositoryEntryRelationDao.addRole(id2, test234, GroupRoles.owner.name());
		
		// add the references
		referenceManager.addReference(course1.getOlatResource(), test12.getOlatResource(), "4534565");
		referenceManager.addReference(course2.getOlatResource(), test12.getOlatResource(), "4532421");
		referenceManager.addReference(course2.getOlatResource(), test2.getOlatResource(), "6786878");
		referenceManager.addReference(course2.getOlatResource(), test234.getOlatResource(), "17988942");
		referenceManager.addReference(course3.getOlatResource(), test234.getOlatResource(), "47945532");
		referenceManager.addReference(course4.getOlatResource(), test234.getOlatResource(), "578912386");
		dbInstance.commitAndCloseSession();
		
		//course 1,2
		List<RepositoryEntry> courses12 = new ArrayList<>(2);
		courses12.add(course1);
		courses12.add(course2);
		List<ReferenceInfos> refCourses12 = referenceManager.getReferencesInfos(courses12, id1, roles);
		Assert.assertNotNull(refCourses12);
		Assert.assertEquals(3, refCourses12.size());
		//test12
		ReferenceInfos refCourses12_test12 = getReferenceInfos(refCourses12, test12);
		Assert.assertNotNull(refCourses12_test12);
		Assert.assertTrue(refCourses12_test12.isOrphan());
		Assert.assertTrue(refCourses12_test12.isOwner());
		//test 2
		ReferenceInfos refCourses12_test2 = getReferenceInfos(refCourses12, test2);
		Assert.assertNotNull(refCourses12_test2);
		Assert.assertTrue(refCourses12_test2.isOrphan());
		Assert.assertFalse(refCourses12_test2.isOwner());
		//test234
		ReferenceInfos refCourses12_test234 = getReferenceInfos(refCourses12, test234);
		Assert.assertNotNull(refCourses12_test234);
		Assert.assertFalse(refCourses12_test234.isOrphan());
		Assert.assertFalse(refCourses12_test234.isOwner());
		
		//course 2
		List<RepositoryEntry> courses2 = new ArrayList<>(2);
		courses2.add(course2);
		List<ReferenceInfos> refCourses2 = referenceManager.getReferencesInfos(courses2, id2, roles);
		Assert.assertNotNull(refCourses2);
		Assert.assertEquals(3, refCourses2.size());
		//test12
		ReferenceInfos refCourses2_test12 = getReferenceInfos(refCourses2, test12);
		Assert.assertNotNull(refCourses2_test12);
		Assert.assertFalse(refCourses2_test12.isOrphan());
		Assert.assertFalse(refCourses2_test12.isOwner());
		//test 2
		ReferenceInfos refCourses2_test2 = getReferenceInfos(refCourses2, test2);
		Assert.assertNotNull(refCourses2_test2);
		Assert.assertTrue(refCourses2_test2.isOrphan());
		Assert.assertTrue(refCourses2_test2.isOwner());
		//test234
		ReferenceInfos refCourses2_test234 = getReferenceInfos(refCourses2, test234);
		Assert.assertNotNull(refCourses2_test234);
		Assert.assertFalse(refCourses2_test234.isOrphan());
		Assert.assertTrue(refCourses2_test234.isOwner());
	}
	
	private ReferenceInfos getReferenceInfos(List<ReferenceInfos> refInfoList, RepositoryEntry entry) {
		ReferenceInfos infos = null;
		if(refInfoList != null && refInfoList.size() > 0) {
			for(ReferenceInfos refInfos:refInfoList) {
				if(refInfos.getEntry().equals(entry)) {
					infos = refInfos;
				}
			}
		}
		return infos;
	}
}