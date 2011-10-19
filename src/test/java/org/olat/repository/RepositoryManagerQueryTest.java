package org.olat.repository;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.course.CourseModule;
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
	
	@Autowired
	private RepositoryManager rm;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserManager userManager;
	
	
	public void testManagers() {
		assertNotNull(rm);
	}
	
	@Test
	public void testOneShootQuery() {
		DB db = DBFactory.getInstance();
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id2");
		Identity institut1 = JunitTestHelper.createAndPersistIdentityAsAuthor("kanu");
		institut1.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Volks");
		Identity institut2 = JunitTestHelper.createAndPersistIdentityAsAuthor("rei");
		institut2.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Nerv");
		userManager.updateUserFromIdentity(institut1);
		userManager.updateUserFromIdentity(institut2);

		// generate 500 repo entries
		int numbRes = 400;
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

		List<String> types = Collections.singletonList(CourseModule.getCourseTypeName());
		
		// finally the search query
		Roles role1 = new Roles(false, false, false, true, false, false, false);
		List<RepositoryEntry> resultTwoShoot = rm.genericANDQueryWithRolesRestriction(null, null, null, types, role1, null);
		assertNotNull(resultTwoShoot);
		assertFalse(resultTwoShoot.isEmpty());
		
		//roles: author + institution manager
		long startSearchReferencable2 = System.currentTimeMillis();
		Roles role2 = new Roles(false, false, false, true, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut = rm.genericANDQueryWithRolesRestriction(null, null, null, types, role2, "Volks");
		Set<RepositoryEntry> resultTwoShootInstitutSet = new HashSet<RepositoryEntry>(resultTwoShootInstitut);
		assertNotNull(resultTwoShootInstitut);
		assertFalse(resultTwoShootInstitut.isEmpty());
		assertEquals(resultTwoShootInstitutSet.size(), resultTwoShootInstitut.size());
		long endSearchReferencable2 = System.currentTimeMillis();
		
		long startSearchReferencable3 = System.currentTimeMillis();
		List<RepositoryEntry> resultOneShootInstitut = rm.genericANDQueryWithRolesRestriction(null, null, null, types, role2, "Volks", 0, -1, true);
		assertNotNull(resultOneShootInstitut);
		assertFalse(resultOneShootInstitut.isEmpty());
		long endSearchReferencable3 = System.currentTimeMillis();
		//check
		assertEquals(resultTwoShootInstitutSet.size(), resultOneShootInstitut.size());
		
		
		//roles: institution manager
		Roles role3 = new Roles(false, false, false, true, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut3 = rm.genericANDQueryWithRolesRestriction(null, null, null, types, role3, "Volks");
		assertNotNull(resultTwoShootInstitut3);
		assertFalse(resultTwoShootInstitut3.isEmpty());
		
		List<RepositoryEntry> resultOneShootInstitut3 = rm.genericANDQueryWithRolesRestriction(null, null, null, types, role3, "Volks", 0, -1, true);
		assertNotNull(resultOneShootInstitut3);
		assertFalse(resultOneShootInstitut3.isEmpty());
		//check
		assertEquals(resultTwoShootInstitut3.size(), resultOneShootInstitut3.size());
		
		
		//roles: institution manager search: authorname
		long startSearchReferencable4 = System.currentTimeMillis();
		Roles role4 = new Roles(false, false, false, false, false, true, false);
		List<RepositoryEntry> resultTwoShootInstitut4 = rm.genericANDQueryWithRolesRestriction(null, "kan", null, types, role4, "Volks");
		assertNotNull(resultTwoShootInstitut4);
		assertFalse(resultTwoShootInstitut4.isEmpty());
		long endSearchReferencable4 = System.currentTimeMillis();
		

		long startSearchReferencable5 = System.currentTimeMillis();
		List<RepositoryEntry> resultOneShootInstitut4 = rm.genericANDQueryWithRolesRestriction(null, "kan", null, types, role4, "Volks", 0, -1, true);
		assertNotNull(resultOneShootInstitut4);
		assertFalse(resultOneShootInstitut4.isEmpty());
		long endSearchReferencable5 = System.currentTimeMillis();
		//check
		assertEquals(resultTwoShootInstitut4.size(), resultOneShootInstitut4.size());

		System.out.println((endSearchReferencable2 - startSearchReferencable2) + " :: " + (endSearchReferencable3 - startSearchReferencable3) + " ms");
		System.out.println((endSearchReferencable4 - startSearchReferencable4) + " :: " + (endSearchReferencable5 - startSearchReferencable5) + " ms");
		
		
		//test paging
		List<RepositoryEntry> resultOneShootInstitut6 = rm.genericANDQueryWithRolesRestriction(null, "kan", null, types, role4, "Volks", 0, 50, true);
		int resultOneShootInstitutTotal6 = rm.countGenericANDQueryWithRolesRestriction(null, "kan", null, types, role4, "Volks", true);
		assertNotNull(resultOneShootInstitut6);
		assertEquals(50, resultOneShootInstitut6.size());
		//check
		assertEquals(resultOneShootInstitut4.size(), resultOneShootInstitutTotal6);
	}
	
	private RepositoryEntry createCourseRepositoryEntry(DB db, final int i) {
		OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.getCourseTypeName());
		db.saveObject(r);
		// now make a repository entry for this course
		RepositoryEntry re = RepositoryManager.getInstance().createRepositoryEntryInstance("Florian Gnägi", "Lernen mit OLAT " + i, "yo man description bla bla + i");
		re.setDisplayname("JunitTest_RepositoryEntry_" + i);
		re.setOlatResource(r);
		re.setAccess(RepositoryEntry.ACC_OWNERS);
		//db.commit();
		return re;
	}
}