/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * Initial date: 21 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationsFeedbackConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ApplicationsFeedbackConfigurationDAO applicationsFeedbackConfigurationDao;

	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createConfiguration() {
		Position position = createRandomPosition();
		String configurationName = "My first config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(config);
		Assert.assertNotNull(config.getKey());
	}
	
	@Test
	public void getFeedbackConfigurations() {
		Position position = createRandomPosition();
		String configurationName = "My config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationsFeedbackConfiguration> configurations = applicationsFeedbackConfigurationDao.getFeedbackConfigurations(position);
		
		Assert.assertNotNull(configurations);
		Assert.assertTrue(configurations.contains(config));
	}
	
	
	private Position createRandomPosition() {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AConfig-1");
		position.setPositionTitle("Technician in Apps configuration");
		position.setShortTitle("Pilot of configuration");
		position.setDepartment("NERVig");
		position.setHomepage("http://www.nerv.ig.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(PositionStatus.published.name());
		position.setDescription("We search a specialist for configuration.");
		return positionDao.savePosition(position);
	}

}
