/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewFillEnum;
import org.olat.modules.selectus.model.review.ReviewVisibilityEnum;
import org.olat.modules.selectus.model.review.ReviewerNameVisibilityEnum;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionReviewDefinitionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private PositionReviewDefinitionDAO positionReviewDefinitionDao;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createPositionToReviewDefinition() {
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos.setPlaningsNumber("MZ-910");
		pos.setStatus(PositionStatus.publishedAndInScreening.name());
		pos = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();
		
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		pos.setReviewDefinition(reviewDefinition);
		pos.setReviewEnabled(true);
		
		positionReviewDefinitionDao.save(reviewDefinition);
		positionDao.savePosition(pos);
		
		dbInstance.commit();
	}
	
	@Test
	public void createAndLoad() {
		Position pos = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		pos.setPlaningsNumber("MZ-911");
		pos.setStatus(PositionStatus.publishedAndInScreening.name());
		
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		reviewDefinition.setDefaultSliderLeftLabel("Left");
		reviewDefinition.setDefaultSliderRightLabel("Other left");
		reviewDefinition.setDefaultSliderSteps(Integer.valueOf(5));
		reviewDefinition.setReviewNameVisibility(ReviewerNameVisibilityEnum.anonymous);
		reviewDefinition.setReviewVisibilityCommittee(ReviewVisibilityEnum.afterRating);
		reviewDefinition.setReviewVisibilityHead(ReviewVisibilityEnum.always);
		reviewDefinition.setReviewVisibilitySecretary(ReviewVisibilityEnum.afterSubmission);
		reviewDefinition.setReviewVisibilityExofficio(ReviewVisibilityEnum.staffOnly);
		reviewDefinition.setReviewFillCommittee(ReviewFillEnum.fill);
		reviewDefinition.setReviewFillHead(ReviewFillEnum.no);
		reviewDefinition.setReviewFillSecretary(ReviewFillEnum.fill);
		reviewDefinition.setReviewFillExofficio(ReviewFillEnum.no);
		
		pos.setReviewDefinition(reviewDefinition);
		pos.setReviewEnabled(true);

		positionReviewDefinitionDao.save(reviewDefinition);
		pos = positionDao.savePosition(pos);
		dbInstance.commitAndCloseSession();

		Position reloadedPosition = positionDao.loadPositionByKey(pos.getKey());
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(reloadedPosition.isReviewEnabled());
		Assert.assertNotNull(reloadedPosition.getReviewDefinition());
		
		PositionReviewDefinition reloadReviewDefinition = reloadedPosition.getReviewDefinition();
		Assert.assertEquals(reviewDefinition, reloadReviewDefinition);
		Assert.assertEquals("Left", reloadReviewDefinition.getDefaultSliderLeftLabel());
		Assert.assertEquals("Other left", reloadReviewDefinition.getDefaultSliderRightLabel());
		Assert.assertEquals(Integer.valueOf(5), reloadReviewDefinition.getDefaultSliderSteps());
		Assert.assertEquals(ReviewerNameVisibilityEnum.anonymous, reloadReviewDefinition.getReviewNameVisibility());
		Assert.assertEquals(ReviewVisibilityEnum.afterRating, reloadReviewDefinition.getReviewVisibilityCommittee());
		Assert.assertEquals(ReviewVisibilityEnum.always, reloadReviewDefinition.getReviewVisibilityHead());
		Assert.assertEquals(ReviewVisibilityEnum.afterSubmission, reloadReviewDefinition.getReviewVisibilitySecretary());
		Assert.assertEquals(ReviewVisibilityEnum.staffOnly, reloadReviewDefinition.getReviewVisibilityExofficio());
		Assert.assertEquals(ReviewFillEnum.fill, reloadReviewDefinition.getReviewFillCommittee());
		Assert.assertEquals(ReviewFillEnum.no, reloadReviewDefinition.getReviewFillHead());
		Assert.assertEquals(ReviewFillEnum.fill, reloadReviewDefinition.getReviewFillSecretary());
		Assert.assertEquals(ReviewFillEnum.no, reloadReviewDefinition.getReviewFillExofficio());
	}

}
