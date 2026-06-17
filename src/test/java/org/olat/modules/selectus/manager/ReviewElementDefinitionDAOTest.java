/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewElementDefinitionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ReviewElementDefinitionDAO reviewElementDefinitionDao;
	@Autowired
	private PositionReviewDefinitionDAO positionReviewDefinitionDao;
	
	@Test
	public void createElement() {
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		positionReviewDefinitionDao.save(reviewDefinition);
		dbInstance.commit();
		
		ReviewElementDefinition element = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.slider)
				.reviewElementDefinition();
		dbInstance.commit();
		Assert.assertNotNull(element);
		Assert.assertNotNull(element.getKey());
		Assert.assertNotNull(element.getCreationDate());
		Assert.assertNotNull(element.getLastModified());
		Assert.assertEquals(ReviewElementType.slider, element.getType());
	}
	
	@Test
	public void updateElement() {
		PositionReviewDefinition reviewDefinition = positionReviewDefinitionDao.create();
		positionReviewDefinitionDao.save(reviewDefinition);
		dbInstance.commit();
		
		ReviewElementDefinition element = reviewElementDefinitionDao.create(reviewDefinition, ReviewElementType.text)
				.reviewElementDefinition();
		dbInstance.commit();
		element.setLabel("Try to update");
		reviewElementDefinitionDao.merge(element);
		dbInstance.commitAndCloseSession();
		
		PositionReviewDefinition reloadeReviewDefinition = positionReviewDefinitionDao.loadByKey(reviewDefinition.getKey());
		Assert.assertNotNull(reloadeReviewDefinition);
		List<ReviewElementDefinition> elements = reloadeReviewDefinition.getElements();
		
		Assert.assertNotNull(elements);
		Assert.assertEquals(1, elements.size());
		
		ReviewElementDefinition loadedElement = elements.get(0);
		Assert.assertEquals(ReviewElementType.text, loadedElement.getType());
		Assert.assertEquals("Try to update", loadedElement.getLabel());
	}

}
