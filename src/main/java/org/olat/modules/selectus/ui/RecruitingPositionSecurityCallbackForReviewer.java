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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Initial date: 19.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingPositionSecurityCallbackForReviewer implements RecruitingPositionSecurityCallback {
	
	private final Set<String> visibleDocs;
	private final Set<String> visibleFields;
	private final boolean expertsDocs;
	private final boolean refereesDocs;
	private final boolean expertsComparativeAssesssmentDocs;
	
	private RecruitingPositionSecurityCallbackForReviewer(Set<String> visibleFields, Set<String> visibleDocs,
			boolean expertsDocs, boolean refereesDocs, boolean expertsComparativeAssesssmentDocs) {
		this.visibleFields = visibleFields;
		this.visibleDocs = visibleDocs;
		this.expertsDocs = expertsDocs;
		this.refereesDocs = refereesDocs;
		this.expertsComparativeAssesssmentDocs = expertsComparativeAssesssmentDocs;
	}
	
	public static final RecruitingPositionSecurityCallbackForReviewer withoutDocuments() {
		return new RecruitingPositionSecurityCallbackForReviewer(null, null, false, false, false);
	}
	
	public static final RecruitingPositionSecurityCallbackForReviewer valueOf(Collection<String> visibleFields, Collection<String> visibleDocs) {
		Set<String> fields = visibleFields == null ? new HashSet<>() : new HashSet<>(visibleFields);
		Set<String> docs = visibleDocs == null ? new HashSet<>() : new HashSet<>(visibleDocs);
		return new RecruitingPositionSecurityCallbackForReviewer(fields, docs, false, false, false);
	}
	
	/**
	 * Attention! If documents are all visible, this encompass expert documents, referees documents and comparative assessments
	 * 
	 * @param visibleFields
	 * @param visibleDocs
	 * @param expertsDocs
	 * @param refereesDocs
	 * @param expertsComparativeAssesssmentDocs
	 * @return
	 */
	public static final RecruitingPositionSecurityCallbackForReviewer membersFeedback(Collection<String> visibleFields, Collection<String> visibleDocs,
			boolean expertsDocs, boolean refereesDocs, boolean expertsComparativeAssesssmentDocs) {
		Set<String> fields = visibleFields == null ? new HashSet<>() : new HashSet<>(visibleFields);
		Set<String> docs = visibleDocs == null ? new HashSet<>() : new HashSet<>(visibleDocs);

		boolean seeAllDocuments = (visibleDocs != null && visibleDocs.contains("all"));
		return new RecruitingPositionSecurityCallbackForReviewer(fields, docs,
				expertsDocs || seeAllDocuments,
				refereesDocs || seeAllDocuments,
				expertsComparativeAssesssmentDocs || seeAllDocuments);
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
	public boolean canViewField(String section, String field) {
		if(section != null) {
			if(visibleFields.contains(section + RecruitingModule.ALL)) {
				return true;
			}
			if(visibleFields.contains(section + RecruitingModule.NONE)) {
				return false;
			}
		}
		return visibleFields.contains(field);
	}

	@Override
	public boolean canViewCombinedDocument() {
		return false;
	}

	@Override
	public boolean canViewDocument(DocumentEnum doc) {
		return visibleDocs != null && (visibleDocs.contains("all") || visibleDocs.contains(doc.name()));
	}

	@Override
	public boolean canViewReferences() {
		return expertsDocs || refereesDocs || expertsComparativeAssesssmentDocs;
	}
	
	@Override
	public ReferenceStatus[] canViewReferencesWithStatus() {
		return new ReferenceStatus[] { ReferenceStatus.submitted };
	}

	@Override
	public boolean canViewReferencesOfExperts() {
		return expertsDocs;
	}

	@Override
	public boolean canViewReferencesOfExpertsComparativeAssessment() {
		return expertsComparativeAssesssmentDocs;
	}

	@Override
	public boolean canViewReferencesOfReferees() {
		return refereesDocs;
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
