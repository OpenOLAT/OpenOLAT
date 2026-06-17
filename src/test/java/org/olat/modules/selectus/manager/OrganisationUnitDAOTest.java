/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
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
	private PositionDAO positionDao;
	@Autowired
	private OrganisationUnitDAO organisationUnitDao;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createOrganisationUnit() {
		Organisation organisation = organisationService
				.createOrganisation("Org-unit-1-test", "Org-unit-1-test", "", null, null, JunitTestHelper.getDefaultActor());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit(organisation);
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
	public void loadOrganisationUnitByKey() {
		Organisation organisation = organisationService
				.createOrganisation("Org-unit-2-test", "Org-unit-2-test", "", null, null, JunitTestHelper.getDefaultActor());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit(organisation);
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
	public void loadOrganisationUnitByOrganisation() {
		Organisation organisation = organisationService
				.createOrganisation("Org-unit-4-test", "Org-unit-4-test", "", null, null, JunitTestHelper.getDefaultActor());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit(organisation);
		unit.setName("Unit gamma");
		unit.setNameDe("Einheit gamma");
		unit.setDescription("Unit description gamma");
		unit.setSystemConfiguration(false);
		unit.setStaffBcc("bcc@frentix.com");
		unit.setStaffMail("mail@frentix.com");
		unit = organisationUnitDao.save(unit);
		dbInstance.commitAndCloseSession();
		
		OrganisationUnit reloadedUnit = organisationUnitDao.loadOrganisationUnitByOrganisation(organisation);
		Assert.assertNotNull(reloadedUnit);
		Assert.assertEquals(unit, reloadedUnit);
		Assert.assertNotNull(reloadedUnit.getOrganisation());
		Assert.assertEquals(organisation, reloadedUnit.getOrganisation());
	}
	
	@Test
	public void loadOrganisationUnitByPosition() {
		Organisation organisation = organisationService
				.createOrganisation("Org-unit-5-test", "Org-unit-5-test", "", null, null, JunitTestHelper.getDefaultActor());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit(organisation);
		unit.setName("Unit theta");
		unit.setNameDe("Einheit theta");
		unit.setDescription("Unit description theta");
		unit.setSystemConfiguration(false);
		unit.setStaffBcc("bcc@frentix.com");
		unit.setStaffMail("mail@frentix.com");
		unit = organisationUnitDao.save(unit);
		dbInstance.commitAndCloseSession();
		
		//create a position and save it
		Position position = positionDao.createPosition("none", "none", organisation);
		position.setPlaningsNumber("ORG-808");
		position.setPositionTitle("Prof.");
		position.setShortTitle("Short title");
		position.setDepartment("Organisation for psychology");
		position.setHomepage("http://www.asylum.com");
		position.setApplicationDeadline(new Date());
		position.setStatus("in preparation");
		position.setDescription("The department of psychology ...");
		positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		OrganisationUnit reloadedUnit = organisationUnitDao.loadOrganisationUnitByPosition(position);
		Assert.assertNotNull(reloadedUnit);
		Assert.assertEquals(unit, reloadedUnit);
		Assert.assertNotNull(reloadedUnit.getOrganisation());
		Assert.assertEquals(organisation, reloadedUnit.getOrganisation());
	}
	
	@Test
	public void findAllOrganisationunits() {
		Organisation organisation = organisationService
				.createOrganisation("Org-unit-3-test", "Org-unit-3-test", "", null, null, JunitTestHelper.getDefaultActor());
		OrganisationUnit unit = organisationUnitDao.createOrganisationUnit(organisation);
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
}
