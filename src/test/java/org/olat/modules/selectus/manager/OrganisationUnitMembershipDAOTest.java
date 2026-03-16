/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.OrganisationUnitMembership;


/**
 * 
 * Initial date: 16 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUnitMembershipDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationUnitDAO organisationUnitDao;
	@Autowired
	private OrganisationUnitMembershipDAO organisationUnitMembershipDao;
	
	@Test
	public void createOrganisationUnitMembership() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-m-1");
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit = organisationUnitDao.save(unit);
		dbInstance.commit();
		
		// create the membership
		OrganisationUnitMembership membership = organisationUnitMembershipDao.createAndPersistMembership(member, unit);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(membership);
		Assert.assertNotNull(membership.getKey());
		Assert.assertNotNull(membership.getCreationDate());
		Assert.assertEquals(member, membership.getIdentity());
		Assert.assertEquals(unit, membership.getOrganisationUnit());
	}
	
	@Test
	public void loadOrganisationUnitMembership_key() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-m-2");
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit = organisationUnitDao.save(unit);
		// create the membership
		OrganisationUnitMembership membership = organisationUnitMembershipDao.createAndPersistMembership(member, unit);
		dbInstance.commitAndCloseSession();
		
		OrganisationUnitMembership reloadedMembership = organisationUnitMembershipDao.loadByKey(membership.getKey());
		Assert.assertNotNull(reloadedMembership);
		Assert.assertNotNull(reloadedMembership.getKey());
		Assert.assertEquals(membership, reloadedMembership);
		Assert.assertNotNull(reloadedMembership.getCreationDate());
		Assert.assertEquals(member, reloadedMembership.getIdentity());
		Assert.assertEquals(unit, reloadedMembership.getOrganisationUnit());
	}

	@Test
	public void loadOrganisationUnitMembership_identity() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-m-3");
		
		// create membership 1
		OrganisationUnit unit1 = organisationUnitDao.createOrganisationUnit();
		unit1 = organisationUnitDao.save(unit1);
		OrganisationUnitMembership membership1 = organisationUnitMembershipDao.createAndPersistMembership(member, unit1);
		// create membership 2
		OrganisationUnit unit2 = organisationUnitDao.createOrganisationUnit();
		unit2 = organisationUnitDao.save(unit2);
		OrganisationUnitMembership membership2 = organisationUnitMembershipDao.createAndPersistMembership(member, unit2);

		dbInstance.commitAndCloseSession();
		
		List<OrganisationUnitMembership> membershipList = organisationUnitMembershipDao.findMemberships(member);
		Assert.assertNotNull(membershipList);
		Assert.assertEquals(2, membershipList.size());
		Assert.assertTrue(membershipList.contains(membership1));
		Assert.assertTrue(membershipList.contains(membership2));
	}

	@Test
	public void updateOrganisationUnitMembership() {
		Identity member1 = JunitTestHelper.createAndPersistIdentityAsRndUser("org-m-2");
		Identity member2 = JunitTestHelper.createAndPersistIdentityAsRndUser("org-m-3");
		Identity member3 = JunitTestHelper.createAndPersistIdentityAsRndUser("org-m-4");
		OrganisationUnit targetUnit = organisationUnitDao.createOrganisationUnit();
		targetUnit = organisationUnitDao.save(targetUnit);
		// create the membership
		organisationUnitMembershipDao.createAndPersistMembership(member1, targetUnit);
		
		OrganisationUnit unit1 = organisationUnitDao.createOrganisationUnit();
		unit1 = organisationUnitDao.save(unit1);
		OrganisationUnit unit2 = organisationUnitDao.createOrganisationUnit();
		unit2 = organisationUnitDao.save(unit2);
		organisationUnitMembershipDao.createAndPersistMembership(member1, unit1);
		organisationUnitMembershipDao.createAndPersistMembership(member1, unit2);
		organisationUnitMembershipDao.createAndPersistMembership(member2, unit2);
		organisationUnitMembershipDao.createAndPersistMembership(member3, unit2);
		dbInstance.commitAndCloseSession();
		
		List<OrganisationUnit> oldUnits = new ArrayList<>();
		oldUnits.add(unit1);
		oldUnits.add(unit2);
		int rows = organisationUnitMembershipDao.updateMembership(oldUnits, targetUnit);
		Assert.assertEquals(2, rows);
		
		// new memberships
		boolean isMember_t_1 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member1, targetUnit);
		Assert.assertTrue(isMember_t_1);
		boolean isMember_t_2 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member2, targetUnit);
		Assert.assertTrue(isMember_t_2);
		boolean isMember_t_3 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member3, targetUnit);
		Assert.assertTrue(isMember_t_3);
		
		// old memberships (unit 1 and 2 to member 1 are not updated because they already exist)
		boolean isMember_1_1 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member1, unit1);
		Assert.assertTrue(isMember_1_1);
		boolean isMember_1_2 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member1, unit2);
		Assert.assertTrue(isMember_1_2);
		boolean isMember_2_2 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member2, unit2);
		Assert.assertFalse(isMember_2_2);
		boolean isMember_2_3 = organisationUnitMembershipDao.isMemberOfOrganisationUnit(member3, unit2);
		Assert.assertFalse(isMember_2_3);
	}
}
