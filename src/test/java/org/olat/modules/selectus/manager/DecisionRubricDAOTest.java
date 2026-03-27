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

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * Initial date: 17 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionRubricDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private DecisionRubricDAO decisionRubricDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createAndSaveDefinition() {
		DecisionRubricDefinition def = decisionRubricDao.createDecisionRubricDefinition();
		Assert.assertNotNull(def);
		Assert.assertNotNull(def.getCreationDate());
		Assert.assertNotNull(def.getLastModified());
		
		Position position = createRandomPosition(PositionStatus.closed);
		dbInstance.commitAndCloseSession();
		
		def.setRubric("My first rubric");
		def.setType("Unkown");
		def.setSum(true);
		def.setWeight(10);
		def.setPos(0);
		DecisionRubricDefinition savedDef = decisionRubricDao.saveDefinition(def, position);
		dbInstance.commit();
		
		Assert.assertNotNull(savedDef);
		Assert.assertNotNull(savedDef.getCreationDate());
		Assert.assertNotNull(savedDef.getLastModified());
		Assert.assertEquals("My first rubric", savedDef.getRubric());
		Assert.assertEquals("Unkown", savedDef.getType());
		Assert.assertEquals(true, savedDef.isSum());
		Assert.assertEquals(10, savedDef.getWeight());
	}
	
	@Test
	public void loadDefinition() {
		DecisionRubricDefinition def = decisionRubricDao.createDecisionRubricDefinition();
		Assert.assertNotNull(def);
		Assert.assertNotNull(def.getCreationDate());
		Assert.assertNotNull(def.getLastModified());
		
		Position position = createRandomPosition(PositionStatus.closed);
		dbInstance.commitAndCloseSession();
		
		def.setRubric("A rubric");
		def.setType("A B C");
		def.setSum(true);
		def.setWeight(8);
		def.setPos(0);
		DecisionRubricDefinition savedDef = decisionRubricDao.saveDefinition(def, position);
		dbInstance.commit();
		
		List<DecisionRubricDefinition> defs = decisionRubricDao.getDecisionRubricDefinition(position);
		
		Assert.assertNotNull(defs);
		Assert.assertEquals(1, defs.size());
		Assert.assertEquals(savedDef, defs.get(0));
		DecisionRubricDefinition loadedDef = defs.get(0);
		
		Assert.assertNotNull(loadedDef.getCreationDate());
		Assert.assertNotNull(loadedDef.getLastModified());
		Assert.assertEquals("A rubric", loadedDef.getRubric());
		Assert.assertEquals("A B C", loadedDef.getType());
		Assert.assertEquals(true, loadedDef.isSum());
		Assert.assertEquals(8, loadedDef.getWeight());
	}
	
	@Test
	public void createRubric() {
		// create the position and the definition
		Position position = createRandomPosition(PositionStatus.closed);
		DecisionRubricDefinition def = decisionRubricDao.createDecisionRubricDefinition();
		def.setRubric("A rubric");
		def.setType("A B C");
		def.setSum(true);
		def.setWeight(8);
		def.setPos(0);
		def = decisionRubricDao.saveDefinition(def, position);
		dbInstance.commit();
		
		//create an application and a rubric
		Application app = applicationDao.createApplication(position);
		app.getPerson().setFirstName("Rei");
		app.getPerson().setLastName("Ayanami");
		app.getPerson().setNationality("JP");
		app.getPerson().setMail("rei@nerv.co.jp");
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		
		//
		List<ApplicationLight> applications = recruitingFrontendManager.getApplications(position);
		ApplicationLight appLight = applications.get(0);
		DecisionRubric rubric = decisionRubricDao.createDecisionRubric(def, appLight);
		decisionRubricDao.saveDecisionRubric(rubric);
		dbInstance.commit();
		Assert.assertNotNull(rubric);
		Assert.assertNotNull(rubric.getKey());
		Assert.assertEquals(appLight, rubric.getApplication());
		Assert.assertEquals(def, rubric.getDefinition());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Lonely rubric");
		position.setPositionTitle("Rubricer");
		position.setShortTitle("Pilot of rubric");
		position.setDepartment("RUBI");
		position.setHomepage("http://www.rubi.ch");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a expert in making of rubrics.");
		return positionDao.savePosition(position);
	}
}