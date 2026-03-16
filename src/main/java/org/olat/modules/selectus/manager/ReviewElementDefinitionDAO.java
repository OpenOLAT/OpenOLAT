/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionReviewDefinitionImpl;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinitionImpl;
import org.olat.modules.selectus.model.review.ReviewElementType;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReviewElementDefinitionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ReviewPair create(PositionReviewDefinition positionToReviewDefinition, ReviewElementType type) {
		ReviewElementDefinitionImpl element = new ReviewElementDefinitionImpl();
		element.setCreationDate(new Date());
		element.setLastModified(element.getCreationDate());
		element.setElementType(type.name());
		
		//force load of the list
		List<ReviewElementDefinition> definitions = ((PositionReviewDefinitionImpl)positionToReviewDefinition).getElements();
		definitions.add(element);
		element.setPositionReviewDefinition(positionToReviewDefinition);
		dbInstance.getCurrentEntityManager().persist(element);
		positionToReviewDefinition = dbInstance.getCurrentEntityManager().merge(positionToReviewDefinition);
		return new ReviewPair(element, positionToReviewDefinition);
	}

	public ReviewElementDefinition merge(ReviewElementDefinition element) {
		element.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(element);
	}
	
	public void delete(ReviewElementDefinition element) {
		ReviewElementDefinition reloadedElement = dbInstance.getCurrentEntityManager()
				.getReference(ReviewElementDefinitionImpl.class, element.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedElement);
	}
	
	public static class ReviewPair {
		
		private final ReviewElementDefinition reviewElementDefinition;
		private final PositionReviewDefinition positionToReviewDefinition;
		
		public ReviewPair(ReviewElementDefinition reviewElementDefinition, PositionReviewDefinition positionToReviewDefinition) {
			this.reviewElementDefinition = reviewElementDefinition;
			this.positionToReviewDefinition = positionToReviewDefinition;
		}
		
		public ReviewElementDefinition reviewElementDefinition() {
			return reviewElementDefinition;
		}
		
		public PositionReviewDefinition positionReviewDefinition() {
			return positionToReviewDefinition;
		}
	}
}
