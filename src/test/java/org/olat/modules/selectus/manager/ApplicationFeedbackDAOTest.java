/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.feedback.ApplicationFeedbackImpl;

/**
 * 
 * Initial date: 27 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFeedbackDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ApplicationFeedbackDAO applicationFeedbackDao;
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
	public void createApplicationFeedback() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-1");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "My first config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		Assert.assertNotNull(feedback.getKey());
		Assert.assertNotNull(feedback.getCreationDate());
		Assert.assertNotNull(feedback.getLastModified());
		Assert.assertEquals(identity, feedback.getIdentity());
		Assert.assertEquals(app, feedback.getApplication());
		Assert.assertEquals(config, ((ApplicationFeedbackImpl)feedback).getConfiguration());
	}
	
	@Test
	public void updateApplicationFeedback() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-1");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "My first config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, new Date(), config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		feedback.setComment("This a comment");
		feedback.setCommentDate(new Date());
		feedback.setDeadline(new Date());
		
		feedback = applicationFeedbackDao.updateFeedback(feedback);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback reloadedFeedback = applicationFeedbackDao.loadByKey(feedback);
		Assert.assertEquals(feedback, reloadedFeedback);
		Assert.assertEquals(app, reloadedFeedback.getApplication());
		Assert.assertEquals(identity, reloadedFeedback.getIdentity());
		Assert.assertEquals(config, ((ApplicationFeedbackImpl)reloadedFeedback).getConfiguration());
		Assert.assertEquals("This a comment", reloadedFeedback.getComment());
		Assert.assertNotNull(reloadedFeedback.getCommentDate());
		Assert.assertNotNull(reloadedFeedback.getDeadline());
	}
	
	@Test
	public void loadByApplication() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-7");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "A config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationFeedback> feedbacks = applicationFeedbackDao.loadByApplication(app);
		Assert.assertNotNull(feedbacks);
		Assert.assertEquals(1, feedbacks.size());
		Assert.assertEquals(feedback, feedbacks.get(0));
	}
	
	@Test
	public void searchApplicationFeedbackByStatus() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-7");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "A config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		
		ApplicationFeedback awaitingFeedback = applicationFeedbackDao.createFeedback(identity, app, cal.getTime(), config);
		Assert.assertNotNull(awaitingFeedback);
		awaitingFeedback.setReferenceStatus(ReferenceStatus.sentAwaiting);
		awaitingFeedback = applicationFeedbackDao.updateFeedback(awaitingFeedback);
		dbInstance.commitAndCloseSession();
		
		cal.add(Calendar.DATE, -2);
		ApplicationFeedback awaitingAndLateFeedback = applicationFeedbackDao.createFeedback(identity, app, cal.getTime(), config);
		Assert.assertNotNull(awaitingAndLateFeedback);
		awaitingAndLateFeedback.setReferenceStatus(ReferenceStatus.sentAwaiting);
		awaitingAndLateFeedback = applicationFeedbackDao.updateFeedback(awaitingAndLateFeedback);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback lateFeedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(lateFeedback);
		lateFeedback.setReferenceStatus(ReferenceStatus.late);
		lateFeedback = applicationFeedbackDao.updateFeedback(lateFeedback);
		dbInstance.commitAndCloseSession();

		List<ApplicationFeedback> feedbacks = applicationFeedbackDao.searchApplicationFeedback(ReferenceStatus.sentAwaiting, new Date());
		Assert.assertNotNull(feedbacks);
		Assert.assertTrue(feedbacks.contains(awaitingAndLateFeedback));
		Assert.assertFalse(feedbacks.contains(awaitingFeedback));
		Assert.assertFalse(feedbacks.contains(lateFeedback));
	}
	
	@Test
	public void loadByMember() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-8");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "A member config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationFeedback> feedbacks = applicationFeedbackDao.loadByMember(identity);
		Assert.assertNotNull(feedbacks);
		Assert.assertEquals(1, feedbacks.size());
		Assert.assertEquals(feedback, feedbacks.get(0));
	}
	
	@Test
	public void loadByPosition() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-8 d");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "Another config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationFeedback> feedbacks = applicationFeedbackDao.loadByPosition(position);
		Assert.assertNotNull(feedbacks);
		Assert.assertEquals(1, feedbacks.size());
		Assert.assertEquals(feedback, feedbacks.get(0));
	}
	
	
	@Test
	public void hasFeedback() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-5");
		Identity notIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-6");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "My first config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		boolean hasFeedback = applicationFeedbackDao.hasFeedback(identity, app, config);
		Assert.assertTrue(hasFeedback);
		boolean hasNotFeedback = applicationFeedbackDao.hasFeedback(notIdentity, app, config);
		Assert.assertFalse(hasNotFeedback);
	}
	
	@Test
	public void hasFeedbackOpen() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-8");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "My first config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		// first check
		boolean hasNotFeedback = applicationFeedbackDao.hasFeedbackOpen(identity);
		Assert.assertFalse(hasNotFeedback);
		
		// save feedback
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		boolean hasFeedback = applicationFeedbackDao.hasFeedbackOpen(identity);
		Assert.assertTrue(hasFeedback);
	}
	
	@Test
	public void getApplicationFeedbacks() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("feedback-1");
		Position position = createRandomPosition();
		Application app = createRandomApplication(position);
		String configurationName = "My first config";
		ApplicationsFeedbackConfiguration config = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(configurationName, position);
		dbInstance.commitAndCloseSession();
		
		ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(identity, app, null, config);
		Assert.assertNotNull(feedback);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationFeedback> feedbacks = applicationFeedbackDao.getFeedbacks(app);
		Assert.assertNotNull(feedbacks);
		Assert.assertEquals(1, feedbacks.size());
		Assert.assertTrue(feedbacks.contains(feedback));
	}
	
	
	
	private Application createRandomApplication(Position pos) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Valeria " + UUID.randomUUID());
		person.setLastName("Sishi");
		person.setNationality("IT");
		person.setMail("valeria@azura.it");
		person.setPhone("9435898");
		person.setBirthday(new Date());
		return applicationDao.saveTempApplication(app, true);
	}
	
	private Position createRandomPosition() {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AConfiguration-1");
		position.setPositionTitle("Technician in applicatio's configuration");
		position.setShortTitle("Pilot of configuration");
		position.setDepartment("NERVig");
		position.setHomepage("http://www.nerv.ig.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(PositionStatus.published.name());
		position.setDescription("We search a specialist to manage a lot of configuration.");
		return positionDao.savePosition(position);
	}

}
