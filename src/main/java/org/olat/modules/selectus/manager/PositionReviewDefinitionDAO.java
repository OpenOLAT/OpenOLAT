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

import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionReviewDefinitionImpl;
import org.olat.modules.selectus.model.review.ReviewFillEnum;
import org.olat.modules.selectus.model.review.ReviewVisibilityEnum;
import org.olat.modules.selectus.model.review.ReviewerNameVisibilityEnum;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PositionReviewDefinitionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PositionReviewDefinition create() {
		PositionReviewDefinitionImpl positionToReview = new PositionReviewDefinitionImpl();
		positionToReview.setCreationDate(new Date());
		positionToReview.setLastModified(positionToReview.getCreationDate());
		positionToReview.setReviewNameVisibilityString(ReviewerNameVisibilityEnum.visible.name());
		positionToReview.setReviewVisibilityString(ReviewVisibilityEnum.always.name());
		positionToReview.setReviewVisibilityHeadString(ReviewVisibilityEnum.always.name());
		positionToReview.setReviewVisibilitySecretaryString(ReviewVisibilityEnum.always.name());
		positionToReview.setReviewVisibilityExofficioString(ReviewVisibilityEnum.always.name());
		positionToReview.setReviewFillString(ReviewFillEnum.fill.name());
		positionToReview.setReviewFillHeadString(ReviewFillEnum.no.name());
		positionToReview.setReviewFillSecretaryString(ReviewFillEnum.no.name());
		positionToReview.setReviewFillExofficioString(ReviewFillEnum.no.name());
		return positionToReview;
	}
	
	public PositionReviewDefinition save(PositionReviewDefinition positionToReview) {
		((PositionReviewDefinitionImpl)positionToReview).setLastModified(new Date());
		if(positionToReview.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(positionToReview);
		} else {
			
			positionToReview.getElements();
			
			
			positionToReview = dbInstance.getCurrentEntityManager().merge(positionToReview);
		}
		return positionToReview;
	}
	
	public void delete(PositionReviewDefinition positionToReview) {
		dbInstance.getCurrentEntityManager().remove(positionToReview);
	}
	
	public PositionReviewDefinition loadByKey(Long reviewDefinitionKey) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select rev from rpositionreviewdefinition as rev where rev.key=:reviewDefinitionKey");
		
		List<PositionReviewDefinition> defs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PositionReviewDefinition.class)
				.setParameter("reviewDefinitionKey", reviewDefinitionKey)
				.getResultList();
		return defs == null || defs.isEmpty() ? null : defs.get(0);
	}
	
	public PositionReviewDefinition loadByAppKey(ApplicationRef app) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select rpos.reviewDefinition from rposition as rpos")
		  .append(" where rpos.key in (select app.position.key from rapplication as app")
		  .append("  where app.key=:appKey")
		  .append(")");
		
		List<PositionReviewDefinition> defs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PositionReviewDefinition.class)
				.setParameter("appKey", app.getKey())
				.getResultList();
		return defs == null || defs.isEmpty() ? null : defs.get(0);
	}
}
