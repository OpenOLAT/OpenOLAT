/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUnitDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationUnitDAO organisationUnitDao;
	@Autowired
	private OrganisationUnitMembershipDAO organisationUnitMembershipDao;
	
	@Test
	public void createOrganisationUnit() {
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit.setName("Unit");
		unit.setNameDe("Einheit");
		unit.setDescription("Unit description");
		unit.setSystemConfiguration(true);
		unit.setStaffBcc("bcc@frentix.com");
		unit.setStaffMail("mail@frentix.com");
		unit = organisationUnitDao.save(unit);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(unit);
		Assert.assertNotNull(unit.getKey());
		Assert.assertNotNull(unit.getCreationDate());
		Assert.assertEquals("Unit", unit.getName());
		Assert.assertEquals("Einheit", unit.getNameDe());
		Assert.assertEquals("Unit description", unit.getDescription());
		Assert.assertTrue(unit.isSystemConfiguration());
		Assert.assertEquals("bcc@frentix.com", unit.getStaffBcc());
		Assert.assertEquals("mail@frentix.com", unit.getStaffMail());
	}
	
	@Test
	public void loadOrganisationUnit_key() {
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit.setName("Unit alpha");
		unit.setNameDe("Einheit alpha");
		unit.setDescription("Unit description alpha");
		unit.setSystemConfiguration(false);
		unit.setStaffBcc("bcc@frentix.com");
		unit.setStaffMail("mail@frentix.com");
		unit = organisationUnitDao.save(unit);
		dbInstance.commitAndCloseSession();
		
		OrganisationUnit reloadedUnit = organisationUnitDao.loadOrganisationUnitByKey(unit.getKey());
		
		Assert.assertNotNull(reloadedUnit);
		Assert.assertEquals(unit, reloadedUnit);
		Assert.assertNotNull(reloadedUnit.getCreationDate());
		Assert.assertEquals("Unit alpha", reloadedUnit.getName());
		Assert.assertEquals("Einheit alpha", reloadedUnit.getNameDe());
		Assert.assertEquals("Unit description alpha", reloadedUnit.getDescription());
		Assert.assertFalse(reloadedUnit.isSystemConfiguration());
		Assert.assertEquals("bcc@frentix.com", reloadedUnit.getStaffBcc());
		Assert.assertEquals("mail@frentix.com", reloadedUnit.getStaffMail());
	}
	
	@Test
	public void findAllOrganisationunits() {
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit.setName("Unit beta");
		unit.setNameDe("Einheit Beta");
		unit.setDescription("Unit beta description");
		unit = organisationUnitDao.save(unit);
		dbInstance.commitAndCloseSession();
		
		//load all units
		List<OrganisationUnit> allUnits = organisationUnitDao.findAllOrganisationUnits();
		Assert.assertNotNull(allUnits);
		Assert.assertFalse(allUnits.isEmpty());
		Assert.assertTrue(allUnits.contains(unit));
	}
	
	/**
	 * Author / staff can see all organisation units
	 */
	@Test
	@Ignore
	public void findOrganisationUnits() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor("staff-m-2-" + UUID.randomUUID());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit.setName("Unit permission");
		unit = organisationUnitDao.save(unit);
		organisationUnitMembershipDao.createAndPersistMembership(identity, unit);
		
		OrganisationUnit notUnit = organisationUnitDao.createOrganisationUnit();
		notUnit.setName("Unit not permitted");
		notUnit = organisationUnitDao.save(notUnit);
		dbInstance.commitAndCloseSession();
		
		//load all units
		Roles roles = Roles.userRoles();
		List<OrganisationUnit> allUnits = organisationUnitDao.findOrganisationUnits(identity, roles);
		Assert.assertNotNull(allUnits);
		Assert.assertTrue(allUnits.size() >= 2);
		Assert.assertTrue(allUnits.contains(unit));
		Assert.assertTrue(allUnits.contains(notUnit));
	}
	
	/**
	 * Author / staff can see all organisation units
	 */
	@Test
	@Ignore
	public void findOrganisationUnits_author() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor("staff-m-2-" + UUID.randomUUID());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit.setName("Unit permission");
		unit = organisationUnitDao.save(unit);
		organisationUnitMembershipDao.createAndPersistMembership(identity, unit);
		
		OrganisationUnit notUnit = organisationUnitDao.createOrganisationUnit();
		notUnit.setName("Unit not permitted");
		notUnit = organisationUnitDao.save(notUnit);
		dbInstance.commitAndCloseSession();
		
		//load all units
		Roles roles = securityManager.getRoles(identity);
		List<OrganisationUnit> allUnits = organisationUnitDao.findOrganisationUnits(identity, roles);
		Assert.assertNotNull(allUnits);
		Assert.assertTrue(allUnits.size() >= 2);
		Assert.assertTrue(allUnits.contains(unit));
		Assert.assertTrue(allUnits.contains(notUnit));
	}
	
	/**
	 * Organisation unit author / staff can see only their organisation units
	 */
	/*
	@Test
	public void findOrganisationUnits_orgAuthor() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("staff-m-3");
		Identity admin = JunitTestHelper.createAndPersistIdentityAsAdmin("admin-2" + UUID.randomUUID());
		dbInstance.commit();
		Roles roles = Roles.administratorRoles();
		securityManager.updateRoles(admin, identity, roles);
		
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit();
		unit.setName("Unit permission");
		unit = organisationUnitDao.save(unit);
		organisationUnitMembershipDao.createAndPersistMembership(identity, unit);
		
		OrganisationUnit notUnit = organisationUnitDao.createOrganisationUnit();
		notUnit.setName("Unit not permitted");
		notUnit = organisationUnitDao.save(notUnit);
		dbInstance.commitAndCloseSession();
		
		//load all units
		Roles reloadRoles = securityManager.getRoles(identity);
		List<OrganisationUnit> allUnits = organisationUnitDao.findOrganisationUnits(identity, reloadRoles);
		Assert.assertNotNull(allUnits);
		Assert.assertEquals(1, allUnits.size());
		Assert.assertTrue(allUnits.contains(unit));
		Assert.assertFalse(allUnits.contains(notUnit));
	}
	*/
	
}
