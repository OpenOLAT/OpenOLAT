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
package org.olat.repository;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  18 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryManagerQueryTest extends OlatTestCase {
	
	private static final String TEST_RES_NAME = "TestManagerQuery";
	
	@Autowired
	private RepositoryManager rm;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserManager userManager;
	
	private static Identity admin;
	
	private static final String author = "RepositoryManagerQueryAuthor";
	private static boolean initialized = false;
	
	public void testManagers() {
		assertNotNull(rm);
	}
	
	@Before
	public void setup() {
		if(initialized) return;
		
		DB db = DBFactory.getInstance();
		
		admin = JunitTestHelper.createAndPersistIdentityAsAdmin("administrator");
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor(author + "1");
		id1.getUser().setProperty(UserConstants.FIRSTNAME, author + "1");
		id1.getUser().setProperty(UserConstants.LASTNAME, author + "1");
		userManager.updateUserFromIdentity(id1);

		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor(author + "2");
		id2.getUser().setProperty(UserConstants.FIRSTNAME, author + "2");
		id2.getUser().setProperty(UserConstants.LASTNAME, author + "2");
		userManager.updateUserFromIdentity(id2);

		Identity institut1 = JunitTestHelper.createAndPersistIdentityAsAuthor("kanu");
		institut1.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Volks");
		institut1.getUser().setProperty(UserConstants.FIRSTNAME, "Kanu");
		institut1.getUser().setProperty(UserConstants.LASTNAME, "Unchou");
		userManager.updateUserFromIdentity(institut1);

		Identity institut2 = JunitTestHelper.createAndPersistIdentityAsAuthor("rei");
		institut2.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Nerv");
		institut2.getUser().setProperty(UserConstants.FIRSTNAME, "Rei");
		institut2.getUser().setProperty(UserConstants.LASTNAME, "Ayanami");
		userManager.updateUserFromIdentity(institut2);

		db.commitAndCloseSession();

		// generate some repo entries
		int numbRes = 500;
		for (int i = 0; i < numbRes; i++) {
			// create course and persist as OLATResourceImpl
			RepositoryEntry re = createCourseRepositoryEntry(db, i);				

			SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
			securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, ownerGroup);
			securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
			
			int rest = i%4;
			switch(rest) {
				case 0: {
					re.setAccess(RepositoryEntry.ACC_USERS_GUESTS);
					securityManager.addIdentityToSecurityGroup(id1, ownerGroup); break;
				}
				case 1: {
					re.setAccess(RepositoryEntry.ACC_USERS);
					securityManager.addIdentityToSecurityGroup(id2, ownerGroup); break;
				}
				case 2: {
					re.setAccess(RepositoryEntry.ACC_OWNERS);
					securityManager.addIdentityToSecurityGroup(institut1, ownerGroup); break;
				}
				case 3: {
					re.setAccess(RepositoryEntry.ACC_OWNERS);
					securityManager.addIdentityToSecurityGroup(institut2, ownerGroup); break;
				}
			}
			re.setOwnerGroup(ownerGroup);
			rm.saveRepositoryEntry(re);
			securityManager.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());	
			if (i % 20 == 0) {
				db.commitAndCloseSession();
			}
		}

		db.commitAndCloseSession();
		
		initialized = true;
	}
	
	@Test
	public void testTwoShootQuery() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		Roles role1 = new Roles(false, false, false, true, false, false, false);
		List<RepositoryEntry> resultTwoShoot = rm.genericANDQueryWithRolesRestriction(null, null, null, types, null, role1, null);
		assertNotNull(resultTwoShoot);
		assertFalse(resultTwoShoot.isEmpty());
	}
	
	@Test
	public void testOneShootQueryWithRoles() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: author + institution manager
		Roles role2 = new Roles(false, false, false, true, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut = rm.genericANDQueryWithRolesRestriction(null, null, null, types, null, role2, "Volks");
		Set<RepositoryEntry> resultTwoShootInstitutSet = new HashSet<RepositoryEntry>(resultTwoShootInstitut);
		assertNotNull(resultTwoShootInstitut);
		assertFalse(resultTwoShootInstitut.isEmpty());
		assertEquals(resultTwoShootInstitutSet.size(), resultTwoShootInstitut.size());
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types, admin, role2, "Volks");
		List<RepositoryEntry> resultOneShootInstitut = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut);
		assertFalse(resultOneShootInstitut.isEmpty());

		assertEquals(resultTwoShootInstitutSet.size(), resultOneShootInstitut.size());
	}
	
	@Test
	public void testOneShootQueryWithAuthorRole() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: author + institution manager
		Roles role2 = new Roles(false, false, false, true, false, false, false);
		List<RepositoryEntry> resultTwoShoot = rm.genericANDQueryWithRolesRestriction(null, null, null, types, null, role2, null);
		assertNotNull(resultTwoShoot);
		assertFalse(resultTwoShoot.isEmpty());
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types, null, role2, null);
		List<RepositoryEntry> resultOneShoot = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShoot);
		assertFalse(resultOneShoot.isEmpty());

		assertEquals(resultTwoShoot.size(), resultOneShoot.size());
	}
	
	@Test
	public void testOneShootWithInstitution() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager
		Roles role3 = new Roles(false, false, false, true, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut3 = rm.genericANDQueryWithRolesRestriction(null, null, null, types, null, role3, "Volks");
		assertNotNull(resultTwoShootInstitut3);
		assertFalse(resultTwoShootInstitut3.isEmpty());
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types, null, role3, "Volks");
		List<RepositoryEntry> resultOneShootInstitut3 = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut3);
		assertFalse(resultOneShootInstitut3.isEmpty());
		//check
		assertEquals(resultTwoShootInstitut3.size(), resultOneShootInstitut3.size());
	}
	
	@Test
	public void testOneShootWithInstitutionAndSearchByAuthorName() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager search: authorname
		Roles role4 = new Roles(false, false, false, false, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut4 = rm.genericANDQueryWithRolesRestriction(null, "kan", null, types, null, role4, "Volks");
		assertNotNull(resultTwoShootInstitut4);
		assertFalse(resultTwoShootInstitut4.isEmpty());
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, "kan", null, types, null, role4, "Volks");
		List<RepositoryEntry> resultOneShootInstitut4 = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut4);
		assertFalse(resultOneShootInstitut4.isEmpty());
		//check
		assertEquals(resultTwoShootInstitut4.size(), resultOneShootInstitut4.size());
	}
	
	@Test
	public void testOneShootQueryPaging() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager search: authorname
		Roles role4 = new Roles(false, false, false, false, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut4 = rm.genericANDQueryWithRolesRestriction(null, "kan", null, types, null, role4, "Volks");
		assertNotNull(resultTwoShootInstitut4);
		assertFalse(resultTwoShootInstitut4.isEmpty());
		
		//test paging
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, "kan", null, types, null, role4, "Volks");
		List<RepositoryEntry> resultOneShootInstitut6 = rm.genericANDQueryWithRolesRestriction(params, 0, 50, true);
		int resultOneShootInstitutTotal6 = rm.countGenericANDQueryWithRolesRestriction(params);
		assertNotNull(resultOneShootInstitut6);
		assertEquals(50, resultOneShootInstitut6.size());
		//check
		assertEquals(resultTwoShootInstitut4.size(), resultOneShootInstitutTotal6);
	}
	
	private RepositoryEntry createCourseRepositoryEntry(DB db, final int i) {
		OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(TEST_RES_NAME);
		db.saveObject(r);
		// now make a repository entry for this course
		RepositoryEntry re = RepositoryManager.getInstance().createRepositoryEntryInstance("Rei Ayanami", "Lernen mit OLAT " + i, "Description of learning by OLAT " + i);
		re.setDisplayname("JunitTest_RepositoryEntry_" + i);
		re.setOlatResource(r);
		re.setAccess(RepositoryEntry.ACC_OWNERS);
		//db.commit();
		return re;
	}
}