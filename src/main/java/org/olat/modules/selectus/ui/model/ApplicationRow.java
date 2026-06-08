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
package org.olat.modules.selectus.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.form.flexible.elements.FormLink;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.ui.UserRatingMapper;
import org.olat.modules.selectus.ui.rating.CustomRatingFormItem;
import org.olat.modules.selectus.ui.rating.RatingsOverviewFormItem;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationRow implements ApplicationLightRow, ApplicationRef {
	
	private int numOfReviews;
	private int numOfAssignedRatings;
	private int numOfAssignments;
	private String[] assigneeArray;
	private String[] assigneeKeysArray;
	private String[] sentEmailTemplates;
	
	private boolean reviewed;
	
	private Integer decision;
	private UserRating currentRating;
	private UserRatingMapper userRatingMapper;
	private List<AppToCategory> tags;
	private List<ParallelApplication> parallelApplications;
	
	private Notes notes;
	private ApplicationLight application;
	private ApplicationRefereeStats refereesStats;
	
	private Object[] additionalAttributesValues;
	
	private final String url;
	
	private FormLink reviewButton;
	private CustomRatingFormItem ratingItem;
	private RatingsOverviewFormItem ratingOverviewItem;
	
	public ApplicationRow(ApplicationLight application, Notes notes, ApplicationRefereeStats refereesStats,
			Object[] additionalAttributesValues, String url) {
		this.url = url;
		this.notes = notes;
		this.application = application;
		this.refereesStats = refereesStats;
		decision = application.getDecision();
		this.additionalAttributesValues = additionalAttributesValues;
	}

	@Override
	public Long getKey() {
		return application.getKey();
	}

	@Override
	public ApplicationLight getApplication() {
		return application;
	}

	public void setApplication(ApplicationLight application) {
		this.application = application;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Notes getNotes() {
		return notes;
	}

	public void setNotes(Notes notes) {
		this.notes = notes;
	}
	
	public String getMail() {
		if(application.getPerson() != null) {
			return application.getPerson().getMail();
		}
		return null;
	}
	
	public ApplicationRefereeStats getRefereesStats() {
		return refereesStats;
	}
	
	public int getNumOfSubmittedExperts() {
		return refereesStats == null ? 0 : refereesStats.getNumOfSubmittedExperts();
	}
	
	public int getNumOfSubmittedRecommendations() {
		return refereesStats == null ? 0 : refereesStats.getNumOfSubmittedRecommendations();
	}
	
	public int getNumOfSubmittedComparativeExperts() {
		return refereesStats == null ? 0 : refereesStats.getNumOfSubmittedComparativeExperts();
	}
	
	public int getTotalSubmitted() {
		return refereesStats == null ? 0 : refereesStats.getTotalSubmitted();
	}

	public Object getAdditionalValue(int index) {
		Object val = null;
		if(additionalAttributesValues != null && index >= 0 && index < additionalAttributesValues.length) {
			val = additionalAttributesValues[index];
		}
		return val;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public Integer getDecision() {
		return decision;
	}

	public void setDecision(Integer decision) {
		this.decision = decision;
	}

	public List<AppToCategory> getCategories() {
		return tags;
	}

	public void addCategorie(Category tag, boolean administrative) {
		if(tags == null) {
			tags = new ArrayList<>();
		}
		tags.add(AppToCategory.valueOf(tag, administrative));
	}

	public UserRating getCurrentRating() {
		return currentRating;
	}

	public void setCurrentRating(UserRating currentRating) {
		this.currentRating = currentRating;
	}


	
	public UserRatingMapper getUserRatingMapper() {
		return userRatingMapper;
	}
	
	public void setUserRatingMapper(UserRatingMapper userRatingMapper) {
		this.userRatingMapper = userRatingMapper;
	}

	public boolean isAllowed() {
		boolean ok = true;
		if(application != null && application.getDecision() != null && application.getDecision() > 0) {
			ok &= false;
		}
		if(decision != null && decision > 0) {
			ok &= false;
		}
		if(application != null && application.getApplicationStatus() != ApplicationStatus.active) {
			ok &= false;
		}
		return ok;
	}

	public List<ParallelApplication> getParallelApplications() {
		return parallelApplications;
	}

	public void setParallelApplications(List<ParallelApplication> parallelApplications) {
		this.parallelApplications = parallelApplications;
	}

	public int getNumOfReviews() {
		return numOfReviews;
	}

	public void setNumOfReviews(int numOfReviews) {
		this.numOfReviews = numOfReviews;
	}

	public int getNumOfAssignedRatings() {
		return numOfAssignedRatings;
	}

	public void setNumOfAssignedRatings(int numOfRatings) {
		this.numOfAssignedRatings = numOfRatings;
	}

	public int getNumOfAssignments() {
		return numOfAssignments;
	}

	public void setNumOfAssignments(int numOfAssignments) {
		this.numOfAssignments = numOfAssignments;
	}

	public String[] getAssigneeArray() {
		return assigneeArray;
	}

	public void setAssigneeArray(String[] assigneeArray) {
		this.assigneeArray = assigneeArray;
	}

	public String[] getAssigneeKeysArray() {
		return assigneeKeysArray;
	}

	public void setAssigneeKeysArray(String[] assigneeKeysArray) {
		this.assigneeKeysArray = assigneeKeysArray;
	}

	public String[] getSentEmailTemplates() {
		return sentEmailTemplates;
	}

	public void setSentEmailTemplates(String[] sentEmailTemplates) {
		this.sentEmailTemplates = sentEmailTemplates;
	}
	
	public FormLink getReviewButton() {
		return reviewButton;
	}

	public void setReviewButton(FormLink reviewButton) {
		this.reviewButton = reviewButton;
	}
	
	public CustomRatingFormItem getRatingItem() {
		return ratingItem;
	}

	public void setRatingItem(CustomRatingFormItem ratingItem) {
		this.ratingItem = ratingItem;
	}

	public RatingsOverviewFormItem getRatingOverviewItem() {
		return ratingOverviewItem;
	}

	public void setRatingOverviewItem(RatingsOverviewFormItem ratingOverviewItem) {
		this.ratingOverviewItem = ratingOverviewItem;
	}
}
