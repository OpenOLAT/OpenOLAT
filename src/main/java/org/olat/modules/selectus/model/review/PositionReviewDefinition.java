/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
