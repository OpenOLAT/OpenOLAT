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
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.model.review.ReviewResponseImpl;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewResponseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private ReviewResponseDAO reviewResponseDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ReviewElementDefinitionDAO reviewElementDefinitionDao;
	@Autowired
	private PositionReviewDefinitionDAO positionReviewDefinitionDao;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-selectus-service-unit-test", "Org-selectus-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createResponse() {
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-1");
		
		Position pos = createRandomPosition(PositionStatus.closed);
		Application app = createApplication(pos, "John", "Deleted");
		Assert.assertNotNull(app);
		
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		reviewDefinition = positionReviewDefinitionDao.save(reviewDefinition);
		ReviewElementDefinition element = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		dbInstance.commit();
		
		List<ApplicationLight> applications = applicationDao.findApplicationsLight(pos, true);
		ApplicationLight appLight = applications.get(0);
		ReviewResponse response = reviewResponseDao.create(appLight, element, reviewer, "Test", null);
		
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getKey());
		Assert.assertNotNull(response.getCreationDate());
		Assert.assertNotNull(response.getLastModified());
		Assert.assertEquals(reviewer, response.getReviewer());
		Assert.assertEquals(element, response.getElement());
		Assert.assertEquals(appLight, ((ReviewResponseImpl)response).getApplication());
	}
	
	@Test
	public void getResponses_byApp() {
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-2");
		
		Position pos = createRandomPosition(PositionStatus.closed);
		Application app = createApplication(pos, "John", "Forget-1");
		Assert.assertNotNull(app);
		
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		reviewDefinition = positionReviewDefinitionDao.save(reviewDefinition);
		ReviewElementDefinition element = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		dbInstance.commit();
		
		List<ApplicationLight> applications = applicationDao.findApplicationsLight(pos, true);
		ApplicationLight appLight = applications.get(0);
		ReviewResponse response = reviewResponseDao.create(appLight, element, reviewer, null, 3);
		dbInstance.commitAndCloseSession();
		
		List<ReviewResponse> responses = reviewResponseDao.getResponses(app);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(responses);
		Assert.assertEquals(1, responses.size());
		
		ReviewResponse reloadedResponse = responses.get(0);
		Assert.assertEquals(response, reloadedResponse);
		Assert.assertEquals(reviewer, reloadedResponse.getReviewer());
		Assert.assertEquals(element, reloadedResponse.getElement());
		Assert.assertEquals(appLight, ((ReviewResponseImpl)reloadedResponse).getApplication());
	}
	
	@Test
	public void getResponses_byPosition() {
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-3");
		
		Position pos = createRandomPosition(PositionStatus.closed);
		Application app = createApplication(pos, "John", "Disappear");
		Assert.assertNotNull(app);
		
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		reviewDefinition = positionReviewDefinitionDao.save(reviewDefinition);
		ReviewElementDefinition element = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		dbInstance.commit();
		
		List<ApplicationLight> applications = applicationDao.findApplicationsLight(pos, true);
		ApplicationLight appLight = applications.get(0);
		ReviewResponse response = reviewResponseDao.create(appLight, element, reviewer, null, 3);
		dbInstance.commitAndCloseSession();
		
		List<ReviewResponse> responses = reviewResponseDao.getResponses(pos, 0, 64);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(responses);
		Assert.assertEquals(1, responses.size());
		
		ReviewResponse reloadedResponse = responses.get(0);
		Assert.assertEquals(response, reloadedResponse);
		Assert.assertEquals(reviewer, reloadedResponse.getReviewer());
		Assert.assertEquals(element, reloadedResponse.getElement());
		Assert.assertEquals(appLight, ((ReviewResponseImpl)reloadedResponse).getApplication());
	}
	
	@Test
	public void getReviewers() {
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-3");
		
		Position pos = createRandomPosition(PositionStatus.closed);
		Application app = createApplication(pos, "John", "Disappear");
		Assert.assertNotNull(app);
		
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		reviewDefinition = positionReviewDefinitionDao.save(reviewDefinition);
		ReviewElementDefinition element = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		dbInstance.commit();
		
		List<ApplicationLight> applications = applicationDao.findApplicationsLight(pos, true);
		ApplicationLight appLight = applications.get(0);
		ReviewResponse response = reviewResponseDao.create(appLight, element, reviewer, null, 3);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		
		List<Identity> reviewers = reviewResponseDao.getReviewers(pos);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reviewers);
		Assert.assertEquals(1, reviewers.size());
		Assert.assertEquals(reviewer, reviewers.get(0));
	}
	
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("RV-121");
		position.setPositionTitle("Technician in review");
		position.setShortTitle("Reviewer");
		position.setDepartment("REV");
		position.setHomepage("http://www.rev.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young reviewer to review our review process.");
		return positionDao.savePosition(position);
	}
	
	private Application createApplication(Position pos, String firstName, String lastName) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		return app;
	}

}
