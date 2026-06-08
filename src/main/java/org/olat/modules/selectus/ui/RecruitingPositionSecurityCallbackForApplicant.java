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
package org.olat.modules.selectus.ui;

import java.util.Collections;
import java.util.List;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Initial date: 23 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingPositionSecurityCallbackForApplicant implements RecruitingPositionSecurityCallback {

	public RecruitingPositionSecurityCallbackForApplicant() {
		//
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void updatePosition(Position position) {
		//
	}

	@Override
	public boolean canNotes() {
		return false;
	}

	@Override
	public boolean canSeeAd() {
		return false;
	}
	
	@Override
	public boolean canSeeExpertBlackList() {
		return false;
	}

	@Override
	public boolean canSeePositionURL() {
		return false;
	}

	@Override
	public boolean canRate() {
		return false;
	}

	@Override
	public boolean canMyRatingsColumnAlwaysVisible() {
		return false;
	}

	@Override
	public boolean canReview() {
		return false;
	}

	@Override
	public boolean canMyReviewsColumnAlwaysVisible() {
		return false;
	}

	@Override
	public boolean canCommentReview(ApplicationShort app) {
		return false;
	}

	@Override
	public boolean canReview(ApplicationShort app) {
		return false;
	}

	@Override
	public boolean canDeleteReviews() {
		return false;
	}

	@Override
	public boolean canDownloadMyRatings() {
		return false;
	}

	@Override
	public boolean canSeeCommitteeRatings() {
		return false;
	}

	@Override
	public boolean canSeeCommitteeRatingsOnce() {
		return false;
	}

	@Override
	public boolean canSeePositionDocuments() {
		return false;
	}

	@Override
	public boolean canViewParalellApplications() {
		return false;
	}

	@Override
	public boolean canViewCombinedDocument() {
		return false;
	}
	
	@Override
	public boolean canViewField(String section, String field) {
		return true;
	}

	@Override
	public boolean canViewDocument(DocumentEnum doc) {
		return false;
	}

	@Override
	public boolean canViewReferences() {
		return false;
	}
	
	@Override
	public ReferenceStatus[] canViewReferencesWithStatus() {
		return new ReferenceStatus[] { ReferenceStatus.submitted };
	}

	@Override
	public boolean canViewReferencesOfExperts() {
		return false;
	}

	@Override
	public boolean canViewReferencesOfExpertsComparativeAssessment() {
		return false;
	}

	@Override
	public boolean canViewReferencesOfReferees() {
		return false;
	}

	@Override
	public boolean canViewReviews(boolean hasReviewed) {
		return false;
	}

	@Override
	public boolean canViewReviewAfterSubmission() {
		return false;
	}

	@Override
	public boolean canViewReviewAfterRating() {
		return false;
	}

	@Override
	public boolean canViewCommitteeComment() {
		return false;
	}

	@Override
	public boolean canViewPositionListLog() {
		return false;
	}

	@Override
	public boolean canViewPositionLog() {
		return false;
	}

	@Override
	public boolean canViewApplicationLog() {
		return false;
	}

	@Override
	public boolean canAddPosition() {
		return false;
	}

	@Override
	public boolean canEditPosition() {
		return false;
	}

	@Override
	public boolean canDeletePosition() {
		return false;
	}

	@Override
	public boolean canArchivePosition() {
		return false;
	}

	@Override
	public boolean canCopyPosition() {
		return false;
	}
	
	@Override
	public boolean canReportingPosition() {
		return false;
	}

	@Override
	public boolean canSearchPositionByOrgUnits() {
		return false;
	}

	@Override
	public boolean canSearchPositionByGlobalAttributes() {
		return false;
	}

	@Override
	public boolean canEditPositionStatus() {
		return false;
	}

	@Override
	public boolean canEditApplicationCategories() {
		return false;
	}

	@Override
	public boolean canEditApplicationAdministrativeCategories() {
		return false;
	}

	@Override
	public boolean canSeeApplicationAdministrativeCategories() {
		return false;
	}

	@Override
	public boolean canEditPositionCategoriesSettings() {
		return false;
	}

	@Override
	public boolean canEditPositionCategories() {
		return false;
	}

	@Override
	public boolean canEditReportAttributes() {
		return false;
	}

	@Override
	public boolean canSendBulkApplicationEmails() {
		return false;
	}

	@Override
	public boolean canEditPositionProfile() {
		return false;
	}

	@Override
	public boolean canEditPositionApplicationsSettings() {
		return false;
	}
	
	@Override
	public boolean canEditPositionMailTemplates() {
		return false;
	}

	@Override
	public boolean canEditPositionReferencesSettings() {
		return false;
	}

	@Override
	public boolean canEditPositionFeedbackSettings() {
		return false;
	}

	@Override
	public boolean canEditPositionEvaluationSettings() {
		return false;
	}

	@Override
	public boolean canSearchApplications() {
		return false;
	}

	@Override
	public boolean canAddApplication() {
		return false;
	}
	
	@Override
	public boolean canCopyApplication() {
		return false;
	}

	@Override
	public boolean canEditApplication() {
		return false;
	}

	@Override
	public boolean canEditApplicationPersonalData() {
		return false;
	}

	@Override
	public boolean canEditApplicationAcademicalBackground() {
		return false;
	}

	@Override
	public boolean canEditApplicationProject() {
		return false;
	}

	@Override
	public boolean canEditApplicationDocuments() {
		return false;
	}

	@Override
	public boolean canEditApplicationMemo() {
		return false;
	}
	
	@Override
	public boolean canEditApplicationCommitteeComment() {
		return false;
	}

	@Override
	public boolean canEditApplicationStatus() {
		return false;
	}
	
	@Override
	public boolean canEditApplicationReferences() {
		return false;
	}

	@Override
	public boolean canEditApplicationMembersFeedback() {
		return false;
	}

	@Override
	public boolean canEditCommitteeDecision() {
		return false;
	}

	@Override
	public boolean canDeleteApplication() {
		return false;
	}
	
	@Override
	public boolean canManageFilters() {
		return false;
	}

	@Override
	public String shareFiltersAs() {
		return null;
	}

	@Override
	public List<String> canSharedFiltersBy() {
		return Collections.emptyList();
	}

	//TODO selectus
	/*
	@Override
	public List<FlexiTableSharePermission> mustShareFiltersWith(Translator translator) {
		return Collections.emptyList();
	}
	*/

	@Override
	public boolean canBasicFilters() {
		return false;
	}

	@Override
	public boolean canAdvancedFilters() {
		return false;
	}

	@Override
	public boolean canAddCommitteeMember() {
		return false;
	}

	@Override
	public boolean canImportCommitteeMembers() {
		return false;
	}
	
	@Override
	public boolean canEditCommitteeMember() {
		return false;
	}

	@Override
	public boolean canRemoveCommitteeMember() {
		return false;
	}

	@Override
	public boolean canSendMailToCommittee() {
		return false;
	}

	@Override
	public boolean canExcelListCommittee() {
		return false;
	}

	@Override
	public boolean canPDFApplicationList() {
		return false;
	}
	
	@Override
	public boolean canExcelApplicationList() {
		return false;
	}

	@Override
	public boolean canExcelReviewStatistics() {
		return false;
	}

	@Override
	public boolean canPDFRatings() {
		return false;
	}

	@Override
	public boolean canMailCenter() {
		return false;
	}
	
	@Override
	public boolean canMailCenterExportLog() {
		return false;
	}

	@Override
	public boolean canMailCenterViewEmail() {
		return false;
	}

	@Override
	public boolean canMailCenterResendEmail() {
		return false;
	}

	@Override
	public boolean canSendMailToApplicant() {
		return false;
	}

	@Override
	public boolean canDecisionTool() {
		return false;
	}

	@Override
	public boolean canConfigureDecisionTool() {
		return false;
	}

	@Override
	public boolean canEditDecisionRubrics() {
		return false;
	}

	@Override
	public boolean canDeleteCache() {
		return false;
	}

	@Override
	public boolean canEditAssignments() {
		return false;
	}

	@Override
	public boolean canDeletePublicFeedbacks() {
		return false;
	}

	@Override
	public boolean canGenerateApplicationList() {
		return false;
	}
}
