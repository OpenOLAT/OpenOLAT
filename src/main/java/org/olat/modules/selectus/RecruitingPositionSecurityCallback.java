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
package org.olat.modules.selectus;

import java.util.List;

import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Initial date: 11.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RecruitingPositionSecurityCallback extends RecruitingSecurityCallback {
	
	public void updatePosition(Position position);
	
	public boolean isReadOnly();
	
	/**
	 * 
	 * @return true if the user can use the notes fonction
	 */
	public boolean canNotes();
	
	/**
	 * 
	 * @return true if the user can see the job ads information
	 */
	public boolean canSeeAd();
	
	/**
	 * @return true if the user can see the expert black list
	 */
	public boolean canSeeExpertBlackList();
	
	/**
	 * 
	 * @return true if the user can see the URL of the position (typically in profile of the position)
	 */
	@Override
	public boolean canSeePositionURL();
	
	/**
	 * 
	 * @return true if the user can see the 3 documents 
	 */
	public boolean canSeePositionDocuments();
	
	/**
	 * @return true if the user has edit rights to the "Status" tab
	 */
	public boolean canEditPositionStatus();
	
	/**
	 * @return true if the user has edit rights to the "Profile" tab
	 */
	public boolean canEditPositionProfile();
	
	/**
	 * @return true if the user has edit rights to the "Application" tab
	 */
	public boolean canEditPositionApplicationsSettings();
	
	/**
	 * @return true if the user has edit rights to the "Experts/Referees" tab
	 */
	public boolean canEditPositionReferencesSettings();
	
	/**
	 * @return true if the user has edit rights to the "Feedback" tab
	 */
	public boolean canEditPositionFeedbackSettings();
	
	/**
	 * @return true if the user has edit rights to the "Evaluation" tab
	 */
	public boolean canEditPositionEvaluationSettings();
	
	/**
	 * @return true if the user has edit rights to the "Tag management" tab
	 */
	public boolean canEditPositionCategoriesSettings();
	
	/**
	 * @return true if the user can add new tags for a specific position
	 */
	public boolean canEditPositionCategories();
	
	/**
	 * @return true if the user can add / remove / edit mail templates for a specific position
	 */
	public boolean canEditPositionMailTemplates();
	
	/**
	 * @return true if the user can edit the attributes of the position for reporting
	 */
	public boolean canEditReportAttributes();
	
	/**
	 * @return true if the user can vote (independently of the status of the application)
	 */
	public boolean canRate();
	
	/**
	 * @return true if the user can show/hide the column, false if the column is always visible
	 */
	public boolean canMyRatingsColumnAlwaysVisible();

	/**
	 * 
	 * @return true if the user can do a review for the position
	 */
	public boolean canReview();
	
	/**
	 * @return true if the user can show/hide the column, false if the column is always visible
	 */
	public boolean canMyReviewsColumnAlwaysVisible();
	
	/**
	 * @param app The application to review
	 * @return true if the user can review a specific
	 */
	public boolean canReview(ApplicationShort app);
	
	/**
	 * 
	 * @param app The application to comment
	 * @return true if the user can comment a specific application
	 */
	public boolean canCommentReview(ApplicationShort app);
	
	/**
	 * 
	 * @return true if the user can delete a review
	 */
	public boolean canDeleteReviews();
	
	/**
	 * List / My rating (PDF) -> for all which are not staff
	 * @return
	 */
	public boolean canDownloadMyRatings();
	
	/**
	 * 
	 * @return true if the user can see the ratings of the committee before the voting
	 * 			    part of the committee
	 */
	public boolean canSeeCommitteeRatings();
	
	/**
	 * @return true If the user can see at a moment the committee ratings
	 */
	public boolean canSeeCommitteeRatingsOnce();

	public boolean canAddApplication();
	
	public boolean canCopyApplication();
	
	public boolean canEditApplication();
	
	public boolean canEditApplicationPersonalData();
	
	public boolean canEditApplicationAcademicalBackground();
	
	public boolean canEditApplicationDocuments();

	public boolean canEditApplicationMemo();
	
	public boolean canEditApplicationCommitteeComment();
	
	public boolean canEditApplicationStatus();
	
	public boolean canEditApplicationCategories();
	
	public boolean canEditApplicationAdministrativeCategories();
	
	public boolean canSeeApplicationAdministrativeCategories();
	
	public boolean canEditApplicationReferences();
	
	public boolean canEditApplicationMembersFeedback();
	
	public boolean canEditApplicationProject();
	
	public boolean canSendBulkApplicationEmails();
	
	public boolean canDeleteApplication();
	
	public String shareFiltersAs();
	
	/**
	 * @return List of permissions to read filters
	 */
	public List<String> canSharedFiltersBy();
	
	//TODO selectus public List<FlexiTableSharePermission> mustShareFiltersWith(Translator translator);
	
	public boolean canManageFilters();
	
	public boolean canBasicFilters();
	
	public boolean canAdvancedFilters();
	
	/**
	 * 
	 * @return true if the user can see the applications which a
	 * 				applicant have made parallel to the current one.
	 */
	public boolean canViewParalellApplications();
	
	public boolean canViewField(String section, String field);
	
	public boolean canViewDocument(DocumentEnum doc);
	
	public boolean canViewCombinedDocument();
	
	public boolean canViewReferences();
	
	public boolean canViewReferencesOfExperts();
	
	public boolean canViewReferencesOfExpertsComparativeAssessment();
	
	public boolean canViewReferencesOfReferees();
	
	public ReferenceStatus[] canViewReferencesWithStatus();
	
	public boolean canViewReviews(boolean hasReviewed);
	
	public boolean canViewReviewAfterSubmission();
	
	public boolean canViewReviewAfterRating();
	
	public boolean canViewCommitteeComment();

	public boolean canViewPositionLog();
	
	public boolean canViewApplicationLog();
	
	
	/**
	 * 
	 * @return true if the user is allowed to edit the final committee
	 * 				decision (final rating)
	 */
	public boolean canEditCommitteeDecision();
	
	public boolean canEditAssignments();
	
	public boolean canDeletePublicFeedbacks();
	
	/**
	 * @return true if the user can compose and generate an Excel list of
	 * 		applications.
	 */
	public boolean canGenerateApplicationList();

}
