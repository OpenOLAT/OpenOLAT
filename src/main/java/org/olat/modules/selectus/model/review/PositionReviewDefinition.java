/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import java.util.List;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionReviewDefinition {
	
	public Long getKey();
	
	public boolean isReviewCommentEnabled();

	public void setReviewCommentEnabled(boolean enable);
	
	public ReviewerNameVisibilityEnum getReviewNameVisibility();

	public void setReviewNameVisibility(ReviewerNameVisibilityEnum reviewNameVisibility);
	
	public ReviewVisibilityEnum getReviewVisibilityCommittee();

	public void setReviewVisibilityCommittee(ReviewVisibilityEnum reviewVisibility);
	
	public PositionRole[] getReviewFillRoles();
	
	public ReviewFillEnum getReviewFillCommittee();

	public void setReviewFillCommittee(ReviewFillEnum reviewFill);
	
	public ReviewVisibilityEnum getReviewVisibilityHead();

	public void setReviewVisibilityHead(ReviewVisibilityEnum reviewVisibility);
	
	public ReviewFillEnum getReviewFillHead();

	public void setReviewFillHead(ReviewFillEnum reviewFill);
	
	public ReviewVisibilityEnum getReviewVisibilitySecretary();

	public void setReviewVisibilitySecretary(ReviewVisibilityEnum reviewVisibility);
	
	public ReviewFillEnum getReviewFillSecretary();

	public void setReviewFillSecretary(ReviewFillEnum reviewFill);
	
	public ReviewVisibilityEnum getReviewVisibilityExofficio();

	public void setReviewVisibilityExofficio(ReviewVisibilityEnum reviewVisibility);
	
	public ReviewFillEnum getReviewFillExofficio();

	public void setReviewFillExofficio(ReviewFillEnum reviewFill);

	public Integer getDefaultSliderSteps();

	public void setDefaultSliderSteps(Integer defaultSliderSteps);

	public String getDefaultSliderLeftLabel();

	public void setDefaultSliderLeftLabel(String defaultSliderLeftLabel);

	public String getDefaultSliderRightLabel();

	public void setDefaultSliderRightLabel(String defaultSliderRightLabel);
	
	public List<ReviewElementDefinition> getElements();
	
	public Boolean getReviewStatisticsEnabled();

	public void setReviewStatisticsEnabled(Boolean reviewStatisticsEnabled);

	public Boolean getReviewRadarChartEnabled();

	public void setReviewRadarChartEnabled(Boolean reviewRadarChartEnabled);

}
