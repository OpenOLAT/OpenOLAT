/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.review.ApplicationStatisticElement;
import org.olat.modules.selectus.model.review.ApplicationStatistics;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionStatistics;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.ReviewFillEnum;
import org.olat.modules.selectus.ui.RecruitingPositionSecurityCallbackImpl;
import org.olat.modules.selectus.ui.RecruitingSecurityCallbackImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private ReviewResponseDAO reviewResponseDao;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private ReviewElementDefinitionDAO reviewElementDefinitionDao;
	@Autowired
	private PositionReviewDefinitionDAO positionReviewDefinitionDao;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private OrganisationService organisationService;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-selectus-service-unit-test", "Org-selectus-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void getReviewStatistics() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("selectus-1");
		organisationService.addMember(defaultUnitTestOrganisation, admin, OrganisationRoles.selectusmanager, admin);
		dbInstance.commitAndCloseSession();
		
		Identity reviewer1 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-service-1");
		Identity reviewer2 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-service-2");
		Identity reviewer3 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-service-3");
		Identity reviewer4 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-service-4");
		Position position = createRandomPosition(PositionStatus.closedAndInScreening);
		
		recruitingService.addToCommittee(position, reviewer1);
		recruitingService.addToCommittee(position, reviewer2);
		recruitingService.addToCommittee(position, reviewer3);
		recruitingService.addToCommittee(position, reviewer4);
		
		Application app1 = createApplication(position, "Eva.1", "T.1");
		Application app2 = createApplication(position, "Eva.2", "T.2");
		Application app3 = createApplication(position, "Eva.3", "T.3");
		
		PositionReviewDefinition reviewDefinition = position.getReviewDefinition();
		ReviewElementDefinition element1 = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		ReviewElementDefinition element2 = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		ReviewElementDefinition element3 = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		dbInstance.commit();
		
		ApplicationLight appLight1 = applicationDao.loadApplicationLightForReference(app1.getKey());
		ApplicationLight appLight2 = applicationDao.loadApplicationLightForReference(app2.getKey());
		ApplicationLight appLight3 = applicationDao.loadApplicationLightForReference(app3.getKey());
		
		List<ApplicationLight> appLightList = Arrays.asList(appLight1, appLight2, appLight3);
		List<ReviewElementDefinition> definitions = Arrays.asList(element1, element2, element3);
		List<Identity> reviewers = Arrays.asList(reviewer1, reviewer2, reviewer3, reviewer4);
		
		for(ApplicationLight appLight:appLightList) {
			for(ReviewElementDefinition definition:definitions) {
				for(int i=0; i<reviewers.size(); i++) {
					reviewResponseDao.create(appLight, definition, reviewers.get(i), null, i+1);
				}
			}	
		}
		dbInstance.commitAndCloseSession();

		Roles adminRoles = securityManager.getRoles(admin);
		PositionRole positionRole = recruitingService.getRole(position, admin);
		RecruitingSecurityCallback secCallback = new RecruitingSecurityCallbackImpl(adminRoles, CommitteeMembershipsStats.empty());
		RecruitingPositionSecurityCallback positionSecCallback
				= new RecruitingPositionSecurityCallbackImpl(secCallback, position, admin, adminRoles, positionRole);
		PositionStatistics stats = reviewService.getReviewStatistics(position, admin, positionSecCallback);
		
		ApplicationStatistics appStats1 = stats.getApplicationStatistics(app1);
		Assert.assertNotNull(appStats1);
		ApplicationStatisticElement appStatsElement1_1 = appStats1.getStatisticsElement(element1);
		Assert.assertNotNull(appStatsElement1_1);
		Assert.assertEquals(4, appStatsElement1_1.getNumOfReviews());
		Assert.assertEquals(1, appStatsElement1_1.getMin().intValue());
		Assert.assertEquals(4, appStatsElement1_1.getMax().intValue());
		Assert.assertEquals(2.5d, appStatsElement1_1.getAverage().doubleValue(), 0.00001);
		Assert.assertEquals(1.118033d, appStatsElement1_1.getStandardDeviation().doubleValue(), 0.00001);
		Assert.assertEquals(1.25d, appStatsElement1_1.getVariance(), 0.00001);
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("RV-123");
		position.setPositionTitle("Professor in review");
		position.setShortTitle("Prof. Reviewer");
		position.setDepartment("REV");
		position.setHomepage("http://www.rev.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young reviewer to review our review process.");
		position.setReviewEnabled(true);
		position = positionDao.savePosition(position);
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		reviewDefinition.setReviewFillCommittee(ReviewFillEnum.fill);
		reviewDefinition = positionReviewDefinitionDao.save(reviewDefinition);
		position.setReviewDefinition(reviewDefinition);
		position = recruitingService.savePosition(position);
		dbInstance.commit();
		return position;
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
