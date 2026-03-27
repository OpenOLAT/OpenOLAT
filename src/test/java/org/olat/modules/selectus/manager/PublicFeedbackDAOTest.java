/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.PublicFeedback;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private PublicFeedbackDAO publicFeedbackDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createPublicFeedback() {
		Position position = createRandomPosition(PositionStatus.closedAndInScreening);
		Application application = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		String email = "asuka." + UUID.randomUUID().toString() + "@selectus.com";
		PublicFeedback feedback = publicFeedbackDao.createFeedback("Asuka", "Langley", email, "UUI-123", "WWI-345", application);
		dbInstance.commit();
		
		Assert.assertNotNull(feedback);
		Assert.assertNotNull(feedback.getKey());
		Assert.assertNotNull(feedback.getCreationDate());
		Assert.assertNotNull(feedback.getLastModified());
		Assert.assertEquals("Asuka", feedback.getFirstName());
		Assert.assertEquals("Langley", feedback.getLastName());
		Assert.assertEquals(email, feedback.getEmail());
		Assert.assertEquals("UUI-123", feedback.getExternalId());
		Assert.assertEquals("WWI-345", feedback.getExternalRef());
	}
	
	@Test
	public void loadPublicFeedbacks() {
		Position position = createRandomPosition(PositionStatus.closedAndInScreening);
		Application application = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		String email = UUID.randomUUID().toString() + "@selectus.com";
		PublicFeedback feedback = publicFeedbackDao.createFeedback("John", "Doe", email, "UUI-124", "WWI-346", application);
		dbInstance.commitAndCloseSession();
		
		List<PublicFeedback> feedbacks = publicFeedbackDao.getFeedbacks(application);
		Assert.assertNotNull(feedbacks);
		Assert.assertEquals(1, feedbacks.size());
		Assert.assertEquals(feedback, feedbacks.get(0));
		
		PublicFeedback reloadFeedback = feedbacks.get(0);
		Assert.assertNotNull(reloadFeedback.getKey());
		Assert.assertNotNull(reloadFeedback.getCreationDate());
		Assert.assertNotNull(reloadFeedback.getLastModified());
		Assert.assertEquals("John", reloadFeedback.getFirstName());
		Assert.assertEquals("Doe", reloadFeedback.getLastName());
		Assert.assertEquals(email, reloadFeedback.getEmail());
		Assert.assertEquals("UUI-124", reloadFeedback.getExternalId());
		Assert.assertEquals("WWI-346", reloadFeedback.getExternalRef());
	}
	
	@Test
	public void loadPublicFeedbackBy() {
		Position position = createRandomPosition(PositionStatus.closedAndInScreening);
		Application application = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		String email = UUID.randomUUID().toString() + "@selectus.com";
		String externalId = UUID.randomUUID().toString();
		PublicFeedback feedback = publicFeedbackDao.createFeedback("John", "Doe", email, externalId, "WWI-346", application);
		dbInstance.commitAndCloseSession();
		
		PublicFeedback reloadFeedback = publicFeedbackDao.getFeedbackBy(application, email, null);
		Assert.assertNotNull(reloadFeedback);
		Assert.assertEquals(feedback, reloadFeedback);
		
		PublicFeedback reload2Feedback = publicFeedbackDao.getFeedbackBy(application, null, externalId);
		Assert.assertNotNull(reload2Feedback);
		Assert.assertEquals(feedback, reload2Feedback);
		
		PublicFeedback reload3Feedback = publicFeedbackDao.getFeedbackBy(application, email, externalId);
		Assert.assertNotNull(reload3Feedback);
		Assert.assertEquals(feedback, reload3Feedback);
	}
	
	@Test
	public void deleteByApplication() {
		Position position = createRandomPosition(PositionStatus.closedAndInScreening);
		Application application1 = createRandomApplication(position);
		Application application2 = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		String email1 = UUID.randomUUID().toString() + "@selectus.com";
		PublicFeedback feedback1 = publicFeedbackDao.createFeedback("John", "Doe", email1, "UUI-125", "WWI-347", application1);
		String email2 = UUID.randomUUID().toString() + "@selectus.com";
		PublicFeedback feedback2 = publicFeedbackDao.createFeedback("John", "Doe", email2, "UUI-126", "WWI-348", application2);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(feedback1);
		Assert.assertNotNull(feedback2);
		
		int deletedRows = publicFeedbackDao.deleteApplication(application1);
		Assert.assertEquals(1, deletedRows);
		
		List<PublicFeedback> feedbacksApp1 = publicFeedbackDao.getFeedbacks(application1);
		Assert.assertTrue(feedbacksApp1.isEmpty());
		
		List<PublicFeedback> feedbacksApp2 = publicFeedbackDao.getFeedbacks(application2);
		Assert.assertEquals(1, feedbacksApp2.size());
	}
	
	private Application createRandomApplication(Position position) {
		Application app = applicationDao.createApplication(position);
		
		Person person = app.getPerson();
		person.setFirstName("Rei");
		person.setLastName("Ayanami");
		person.setNationality("JP");
		person.setMail("rei@nerv.co.jp");
		person.setPhone("9435892");
		person.setBirthday(new Date());
		
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		return app;
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-234");
		position.setPositionTitle("Technician in robotic");
		position.setShortTitle("Pilot of robot");
		position.setDepartment("NERV");
		position.setHomepage("http://www.nerv.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young pilot for our semi-living robot.");
		return positionDao.savePosition(position);
	}
}
