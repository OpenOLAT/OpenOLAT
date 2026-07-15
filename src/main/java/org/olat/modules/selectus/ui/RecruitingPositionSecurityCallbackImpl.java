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

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.FilterPermissions;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewFillEnum;
import org.olat.modules.selectus.model.review.ReviewVisibilityEnum;

/**
 * 
 * Initial date: 10.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingPositionSecurityCallbackImpl implements RecruitingPositionSecurityCallback {

	private Position position;
	private PositionReviewDefinition reviewDefinition;
	
	private final boolean selectusManager;
	private final PositionRole positionRole;
	private final RecruitingSecurityCallback recruitingSecCallback;
	
	private final RecruitingModule recruitingModule;
	
	public RecruitingPositionSecurityCallbackImpl(RecruitingSecurityCallback recruitingSecCallback,
			Position position, IdentityRef identity, Roles roles, PositionRole positionRole) {
		this.recruitingSecCallback = recruitingSecCallback;
		this.positionRole = positionRole;
		this.position = position;
		reviewDefinition = position.getReviewDefinition();
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);

		selectusManager = (position.getOrganisation() != null && roles.hasRole(position.getOrganisation(), OrganisationRoles.selectusmanager))
				|| position.getKey() == null;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		reviewDefinition = updatedPosition.getReviewDefinition();
	}

	@Override
	public boolean isReadOnly() {
		return PositionStatus.reporting.name().equals(position.getStatus());
	}

	@Override
	public boolean canNotes() {
		if(isReadOnly()) return false;

		return isAllowedByPositionRole(recruitingModule.getRolesAllowedToTakeNotes());
	}

	@Override
	public boolean canSeeAd() {
		return selectusManager || !canRate();
	}
	
	@Override
	public boolean canSeeExpertBlackList() {
		return selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToSeeExpertBlackList());
	}

	@Override
	public boolean canSeePositionURL() {
		return selectusManager || recruitingSecCallback.canSeePositionURL();
	}

	@Override
	public boolean canRate() {
		if(isReadOnly()) return false;
		
		return isAllowedByPositionRole(recruitingModule.getRolesAllowedToRate());
	}

	@Override
	public boolean canMyRatingsColumnAlwaysVisible() {
		PositionRole[] roles = recruitingModule.getTableApplicationsMyRatingsAlwaysVisibleForRoles();
		if(roles == null || roles.length == 0) {
			return canRate();
		}
		return isAllowedByPositionRole(roles);
	}
	
	@Override
	public boolean canReview() {
		String statusStr = position.getStatus();
		//no status -> can't review
		if(!StringHelper.containsNonWhitespace(statusStr) || isReadOnly()) {
			return false;
		}
		PositionStatus status = PositionStatus.valueOf(statusStr);
		//not in screening -> can't review
		if(!PositionStatus.publishedAndInScreening.equals(status) && !PositionStatus.closedAndInScreening.equals(status)) {
			return false;
		}
		
		PositionReviewDefinition reviewDef = position.getReviewDefinition();
		if(reviewDef == null) {
			return false;
		}
		
		if((reviewDef.getReviewFillCommittee() == ReviewFillEnum.fill && positionRole == PositionRole.member)
				|| (reviewDef.getReviewFillHead() == ReviewFillEnum.fill && positionRole == PositionRole.head)
				|| (reviewDef.getReviewFillSecretary() == ReviewFillEnum.fill && positionRole == PositionRole.secretary)
				|| (reviewDef.getReviewFillExofficio() == ReviewFillEnum.fill && positionRole == PositionRole.exofficio)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canMyReviewsColumnAlwaysVisible() {
		PositionRole[] roles = recruitingModule.getTableApplicationsMyReviewsAlwaysVisibleForRoles();
		if(roles == null || roles.length == 0) {
			return canReview();
		}
		return isAllowedByPositionRole(roles);
	}

	@Override
	public boolean canReview(ApplicationShort app) {
		if(app == null || app.getApplicationStatus() != ApplicationStatus.active
				|| (app.getDecision() != null && app.getDecision().intValue() > 0)) {
			return false;
		}
		return canReview();
	}

	@Override
	public boolean canCommentReview(ApplicationShort app) {
		return canReview(app);
	}

	@Override
	public boolean canDownloadMyRatings() {
		return canRate();
	}

	@Override
	public boolean canSeeCommitteeRatings() {
		String statusStr = position.getStatus();
		if(!StringHelper.containsNonWhitespace(statusStr)) {
			return false;
		}
		PositionStatus status = PositionStatus.valueOf(statusStr);
		
		boolean canSee = positionRole != PositionRole.member;
		if(canSee) {
			//not a committee member, can see ratings
			if(selectusManager) {
				return true;
			}
			if(isAllowedByPositionRole(recruitingModule.getRolesAllowedToSeeRating())) {
				if(PositionStatus.publishedAndInScreening.equals(status) || PositionStatus.closedAndInScreening.equals(status)) {
					return isAllowedByPositionRole(recruitingModule.getRolesAllowedToSeeRatingDuringRating());
				}
				return true;
			}
			return false;
		}

		//not in screening -> can't rate
		return PositionStatus.closedAndNoRating.equals(status);
	}
	
	@Override
	public boolean canSeeCommitteeRatingsOnce() {
		return selectusManager 
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSeeRating());
	}

	@Override
	public boolean canSeePositionDocuments() {
		return true;
	}

	@Override
	public boolean canViewParalellApplications() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSeeParallelApplications());
	}

	@Override
	public boolean canViewCombinedDocument() {
		return true;
	}
	
	@Override
	public boolean canViewField(String section, String field) {
		return true;
	}

	@Override
	public boolean canViewDocument(DocumentEnum doc) {
		return true;
	}

	@Override
	public boolean canViewReferences() {
		return true;
	}
	
	@Override
	public ReferenceStatus[] canViewReferencesWithStatus() {
		return recruitingModule.getApplicationReferencesVisibleStatus();
	}

	@Override
	public boolean canViewReferencesOfExperts() {
		return canViewReferences();
	}

	@Override
	public boolean canViewReferencesOfExpertsComparativeAssessment() {
		return canViewReferences();
	}

	@Override
	public boolean canViewReferencesOfReferees() {
		return canViewReferences();
	}

	@Override
	public boolean canExcelReviewStatistics() {
		return recruitingModule.isReviewEnabled() && recruitingModule.isReviewStatisticsDownloadEnabled() && position.isReviewEnabled()
				&& (selectusManager || recruitingSecCallback.canExcelReviewStatistics()
						|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportReviewsStatisticsExcel()));
	}

	@Override
	public boolean canViewReviews(boolean hasReviewed) {
		boolean visible = false;
		// the configured "always allowed" user roles override the config in UI. 
		// e.g. head can always see the reviews, regardless of whether her can review or not 
		boolean isAlwaysAllowed = selectusManager;
		
		if(reviewDefinition != null) {
			ReviewVisibilityEnum visibility = null;
			if(positionRole == PositionRole.member) {	
				visibility = reviewDefinition.getReviewVisibilityCommittee();
			} else if(positionRole == PositionRole.head) {	
				visibility = reviewDefinition.getReviewVisibilityHead();
			} else if(positionRole == PositionRole.secretary) {	
				visibility = reviewDefinition.getReviewVisibilitySecretary();
			} else if(positionRole == PositionRole.exofficio) {	
				visibility = reviewDefinition.getReviewVisibilityExofficio();
			}

			if(visibility == null) { 
				visible = isAlwaysAllowed;
			} else {
				switch(visibility) {
					case always:
						visible = true;
						break;
					case afterSubmission:
						visible = hasReviewed || isAlwaysAllowed;
						break;
					case afterRating:
						PositionStatus status = PositionStatus.valueOf(position.getStatus());
						visible = status == PositionStatus.closedAndNoRating || status == PositionStatus.closed || isAlwaysAllowed;
						break;
					case staffOnly:
						visible = isAlwaysAllowed;
						break;
				}
			}
		}
		return visible;
	}

	@Override
	public boolean canViewReviewAfterSubmission() {
		return matchReviewVisibility(ReviewVisibilityEnum.afterSubmission);
	}

	@Override
	public boolean canViewReviewAfterRating() {
		return matchReviewVisibility(ReviewVisibilityEnum.afterRating);
	}
	
	private boolean matchReviewVisibility(ReviewVisibilityEnum visibility) {
		if(reviewDefinition != null) {
			if(positionRole == PositionRole.member) {	
				return reviewDefinition.getReviewVisibilityCommittee() == visibility;
			} else if(positionRole == PositionRole.head) {	
				return reviewDefinition.getReviewVisibilityHead() == visibility;
			} else if(positionRole == PositionRole.secretary) {	
				return reviewDefinition.getReviewVisibilitySecretary() == visibility;
			} else if(positionRole == PositionRole.exofficio) {	
				return reviewDefinition.getReviewVisibilityExofficio() == visibility;
			}
		}
		return false;
	}

	@Override
	public boolean canViewCommitteeComment() {
		return recruitingModule.isApplicationsCommitteeCommentEnabled() && position.isCommitteeCommentEnabled()
				&& (selectusManager
						|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationCommitteeComment())
						|| isAllowedByPositionRole(position.getCommitteeCommentVisiblity()));
	}

	@Override
	public boolean canViewPositionListLog() {
		return recruitingModule.isNotificationsToolEnabled();
	}

	@Override
	public boolean canViewPositionLog() {
		return recruitingModule.isNotificationsToolEnabled();
	}

	@Override
	public boolean canViewApplicationLog() {
		return recruitingModule.isNotificationsToolEnabled();
	}

	@Override
	public boolean canDeleteReviews() {
		return selectusManager;
	}

	@Override
	public boolean canAddPosition() {
		return recruitingSecCallback.canAddPosition();
	}

	@Override
	public boolean canEditPosition() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionStatus())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionProfile())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionApplicationsSettings())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionReferencesSettings())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionEvaluationSettings())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionTagsSettings())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionMailTemplates());
	}

	@Override
	public boolean canDeletePosition() {
		return selectusManager;
	}

	@Override
	public boolean canArchivePosition() {
		return selectusManager || recruitingSecCallback.canEditPosition();
	}

	@Override
	public boolean canCopyPosition() {
		if(isReadOnly()) return false;
		return recruitingSecCallback.canCopyPosition();
	}
	
	@Override
	public boolean canReportingPosition() {
		return recruitingSecCallback.canReportingPosition();
	}
	
	@Override
	public boolean canSearchPositionByOrgUnits() {
		return recruitingSecCallback.canSearchPositionByOrgUnits();
	}

	@Override
	public boolean canSearchPositionByGlobalAttributes() {
		return recruitingSecCallback.canSearchPositionByGlobalAttributes();
	}

	@Override
	public boolean canEditPositionStatus() {
		return selectusManager || recruitingSecCallback.canEditPosition()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionStatus());
	}

	@Override
	public boolean canEditPositionCategoriesSettings() {
		return recruitingModule.isTaggingToolEnabled()
				&& (recruitingModule.isSystemTagsEnabled() || recruitingModule.isPositionTagsEnabled())
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionTagsSettings()));
	}

	@Override
	public boolean canEditPositionCategories() {
		return recruitingModule.isTaggingToolEnabled()
				&& (recruitingModule.isPositionTagsEnabled() && position.isPositionTagsEnabled())
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionTagsSettings()));
	}
	
	@Override
	public boolean canEditPositionMailTemplates() {
		return recruitingModule.isMailTemplateToolPositionsEnabled()
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionMailTemplates()));
	}

	@Override
	public boolean canEditApplicationCategories() {
		return recruitingModule.isCategoriesEnabledFor(position)
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationCategories())
					|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditAdministrativeTags()));
	}

	@Override
	public boolean canEditApplicationAdministrativeCategories() {
		return recruitingModule.isCategoriesEnabledFor(position) && recruitingModule.isAdministrativeTagsEnabled()
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditAdministrativeTags()));
	}

	@Override
	public boolean canSeeApplicationAdministrativeCategories() {
		return recruitingModule.isCategoriesEnabledFor(position) && recruitingModule.isAdministrativeTagsEnabled()
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditAdministrativeTags())
						|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSeeAdministrativeTags()));
	}

	@Override
	public boolean canEditPositionProfile() {
		return selectusManager || recruitingSecCallback.canEditPosition()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionProfile());
	}

	@Override
	public boolean canEditPositionApplicationsSettings() {
		return selectusManager || recruitingSecCallback.canEditPosition()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionApplicationsSettings());
	}

	@Override
	public boolean canEditPositionReferencesSettings() {
		return selectusManager || recruitingSecCallback.canEditPosition()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionReferencesSettings());
	}
	
	@Override
	public boolean canEditPositionFeedbackSettings() {
		return selectusManager || recruitingSecCallback.canEditPosition()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionFeedbacksSettings());
	}

	@Override
	public boolean canEditPositionEvaluationSettings() {
		return selectusManager || recruitingSecCallback.canEditPosition()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionEvaluationSettings());
	}
	
	@Override
	public boolean canEditReportAttributes() {
		return selectusManager;
	}

	@Override
	public boolean canSearchApplications() {
		return recruitingSecCallback.canSearchApplications()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSearchApplications());
	}

	@Override
	public boolean canAddApplication() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToCreateApplications());
	}
	
	@Override
	public boolean canCopyApplication() {
		return selectusManager;
	}

	@Override
	public boolean canEditApplication() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationAcademicalBackground())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationDocuments())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationPersonalData())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationStatus())
				|| isAllowedByPositionRole(recruitingModule.getPositionRolesAllowedToEditApplicationsMemo())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationReferences())
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationCommitteeComment());
	}

	@Override
	public boolean canEditApplicationPersonalData() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationPersonalData());
	}

	@Override
	public boolean canEditApplicationAcademicalBackground() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationAcademicalBackground());
	}
	
	@Override
	public boolean canEditApplicationProject() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationProject());
	}

	@Override
	public boolean canEditApplicationDocuments() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationDocuments());
	}

	@Override
	public boolean canEditApplicationMemo() {
		return recruitingModule.isApplicationsMemoEnabled()
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getPositionRolesAllowedToEditApplicationsMemo()));
	}
	
	@Override
	public boolean canEditApplicationCommitteeComment() {
		return recruitingModule.isApplicationsCommitteeCommentEnabled() && position.isCommitteeCommentEnabled()
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationCommitteeComment()));
	}

	@Override
	public boolean canEditApplicationStatus() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationStatus());
	}
	
	@Override
	public boolean canEditApplicationReferences() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditApplicationReferences());
	}

	@Override
	public boolean canEditApplicationMembersFeedback() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditMembersFeedback());
	}

	@Override
	public boolean canEditCommitteeDecision() {
		return selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditCommitteDecision());
	}

	@Override
	public boolean canManageFilters() {
		return selectusManager || isAllowedByFilterPermission(recruitingModule.getRolesAllowedToManageApplicationListFilters());
	}
	
	@Override
	public List<String> canSharedFiltersBy() {
		if(selectusManager) {
			return Collections.singletonList(FilterPermissions.author.name());
		}
		if(positionRole != null) {
			return Collections.singletonList(positionRole.name());
		}
		return Collections.emptyList();
	}
	
	private boolean isFilterPermissionAvailable(FilterPermissions permission) {
		if(permission == FilterPermissions.exofficio) {
			return recruitingModule.isRoleExOfficioEnabled();
		}
		return true;
	}

	//TODO selectus
	/*
	@Override
	public List<FlexiTableSharePermission> mustShareFiltersWith(Translator translator) {
		List<FlexiTableSharePermission> permissions = new ArrayList<>();
		if(!recruitingModule.isShareFiltersEnabled()) {
			//
		} else if(canManageFilters()) {
			FilterPermissions[] managerPermissions = recruitingModule.getRolesAllowedToManageApplicationListFilters();
			
			for(FilterPermissions permission:FilterPermissions.values()) {
				if(!isFilterPermissionAvailable(permission)) {
					continue;
				}
				
				if(permission == FilterPermissions.author) {
					String i18nString = translator.translate(permission.i18nKey());
					FlexiTableSharePermission p = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(i18nString, permission.name(),
									permission.ordinal(), true, Collections.emptyList());
					permissions.add(p);
					continue;
				}
				
				if(FilterPermissions.isInArray(managerPermissions, permission)) {
					String authorI18nString = translator.translate(FilterPermissions.author.i18nKey());
					FlexiTableSharePermission ap = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(authorI18nString, FilterPermissions.author.name(),
									FilterPermissions.author.ordinal(), true, Collections.emptyList());

					String i18nString = translator.translate(permission.i18nKey());
					FlexiTableSharePermission p = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(i18nString, permission.name(),
									permission.ordinal(), true, Collections.singletonList(ap));
					permissions.add(p);
					
				} else {
					List<FlexiTableSharePermission> subPermissions = new ArrayList<>();
					FlexiTableSharePermission ap = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(translator.translate(FilterPermissions.author.i18nKey()),
									FilterPermissions.author.name(), FilterPermissions.author.ordinal(),
									true, Collections.emptyList());
					subPermissions.add(ap);
					
					for(FilterPermissions managerPermission: managerPermissions) {
						String i18nString = translator.translate(managerPermission.i18nKey());
						FlexiTableSharePermission mp = FlexiTableAdvancedFilterFactory
								.createFlexiTableSharePermission(i18nString, managerPermission.name(),
										managerPermission.ordinal(), true, Collections.emptyList());
						subPermissions.add(mp);
					}
					
					String i18n = translator.translate(permission.i18nKey());
					FlexiTableSharePermission p = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(i18n, permission.name(), permission.ordinal(), true, subPermissions);
					permissions.add(p);
				}
			}
		} else if(canShareFilters()) {
			FilterPermissions[] managerPermissions = recruitingModule.getRolesAllowedToManageApplicationListFilters();
			for(FilterPermissions permission:FilterPermissions.values()) {
				if(!isFilterPermissionAvailable(permission)) {
					continue;
				}
				
				if(!FilterPermissions.isInArray(managerPermissions, permission)) {
					List<FlexiTableSharePermission> subPermissions = new ArrayList<>();
					for(FilterPermissions managerPermission: managerPermissions) {
						String i18nString = translator.translate(managerPermission.i18nKey());
						FlexiTableSharePermission ap = FlexiTableAdvancedFilterFactory
								.createFlexiTableSharePermission(i18nString, managerPermission.name(),
										managerPermission.ordinal(), true, Collections.emptyList());
						subPermissions.add(ap);
					}
					
					FlexiTableSharePermission ap = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(translator.translate(FilterPermissions.author.i18nKey()),
									FilterPermissions.author.name(), FilterPermissions.author.ordinal(),
									true, Collections.emptyList());
					subPermissions.add(ap);
					
					String i18n = translator.translate(permission.i18nKey());
					FlexiTableSharePermission p = FlexiTableAdvancedFilterFactory
							.createFlexiTableSharePermission(i18n, permission.name(), permission.ordinal(), true, subPermissions);
					permissions.add(p);
				}
			}
		}
		return permissions;
	}
	*/
	
	private boolean canShareFilters() {
		return selectusManager || isAllowedByFilterPermission(recruitingModule.getRolesAllowedToShareApplicationListFilters());
	}

	private boolean isAllowedByFilterPermission(FilterPermissions[] permissions) {
		boolean hasPermission = false;
		if(selectusManager) {
			for(int i=permissions.length; i-->0; ) {
				if(FilterPermissions.author.equals(permissions[i])) {
					hasPermission = true;
				}
			}
		} else if(positionRole != null) {
			for(int i=permissions.length; i-->0; ) {
				if(permissions[i] != null && permissions[i].name().endsWith(positionRole.name())) {
					hasPermission = true;
				}
			}
		}
		return hasPermission;
	}

	@Override
	public String shareFiltersAs() {
		if(selectusManager) {
			return FilterPermissions.author.name();
		}
		return positionRole == null ? "nobody" : positionRole.name();
	}

	@Override
	public boolean canBasicFilters() {
		return selectusManager || isAllowedByFilterPermission(recruitingModule.getRolesAllowedToApplicationListBasicFilters());
	}

	@Override
	public boolean canAdvancedFilters() {
		return selectusManager || isAllowedByFilterPermission(recruitingModule.getRolesAllowedToApplicationListAdvancedFilters());
	}

	@Override
	public boolean canGenerateApplicationList() {
		return recruitingModule.isTableApplicationsGenerateListEnabled()
				&& (selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportGeneratedList()));
	}

	@Override
	public boolean canDeleteApplication() {
		return selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToDeleteApplication());
	}

	@Override
	public boolean canAddCommitteeMember() {
		return selectusManager || recruitingSecCallback.canAddCommitteeMember()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToAddPositionCommittee());
	}

	@Override
	public boolean canImportCommitteeMembers() {
		return	recruitingSecCallback.canImportCommitteeMembers();
	}
	
	@Override
	public boolean canEditCommitteeMember() {
		return selectusManager || recruitingSecCallback.canEditCommitteeMember()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditPositionCommittee());
	}

	@Override
	public boolean canRemoveCommitteeMember() {
		return selectusManager || recruitingSecCallback.canRemoveCommitteeMember()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToRemovePositionCommittee());
	}

	@Override
	public boolean canSendMailToCommittee() {
		return selectusManager || recruitingSecCallback.canSendMailToCommittee()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSendEmailAllCommittee());
	}

	@Override
	public boolean canEditAssignments() {
		return selectusManager || isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditAssignments());
	}

	@Override
	public boolean canExcelListCommittee() {
		return selectusManager || recruitingSecCallback.canExcelListCommittee()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportCommitteeListExcel());
	}

	@Override
	public boolean canPDFApplicationList() {
		return selectusManager || recruitingSecCallback.canPDFApplicationList()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportApplicationListPdf());
	}
	
	@Override
	public boolean canExcelApplicationList() {
		return selectusManager || recruitingSecCallback.canExcelApplicationList()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportApplicationListExcel());
	}

	@Override
	public boolean canPDFRatings() {
		boolean allowed = selectusManager || recruitingSecCallback.canPDFRatings()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportRatingsPdf());
		if(allowed && positionRole == PositionRole.member) {
			String statusStr = position.getStatus();
			if(!StringHelper.containsNonWhitespace(statusStr)) {
				allowed = false;
			} else {
				PositionStatus status = PositionStatus.valueOf(statusStr);
				allowed &= (status == PositionStatus.closed || status == PositionStatus.closedAndNoRating);
			}
		}
		return allowed;
	}

	@Override
	public boolean canMailCenter() {
		return selectusManager || recruitingSecCallback.canMailCenter()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToViewMailCenter());
	}

	@Override
	public boolean canMailCenterExportLog() {
		return selectusManager || recruitingSecCallback.canMailCenterExportLog()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToExportMailCenterLog());
	}

	@Override
	public boolean canMailCenterViewEmail() {
		return selectusManager || recruitingSecCallback.canMailCenterViewEmail()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToViewMailCenterEmail());
	}

	@Override
	public boolean canMailCenterResendEmail() {
		return selectusManager || recruitingSecCallback.canMailCenterResendEmail()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToResendMailCenterEmail());
	}

	@Override
	public boolean canSendMailToApplicant() {
		return selectusManager || recruitingSecCallback.canSendMailToApplicant()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSendMailToApplicant());
	}
	
	@Override
	public boolean canSendBulkApplicationEmails() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToSendBulkApplicationEmails());
	}

	@Override
	public boolean canDecisionTool() {
		if(!position.isDecisionTool()) return false;
		if(canConfigureDecisionTool()) return true;
		
		String statusStr = position.getStatus();
		if(StringHelper.containsNonWhitespace(statusStr)) {
			PositionStatus status = PositionStatus.valueOf(statusStr);
			return (status == PositionStatus.closed || status == PositionStatus.closedAndNoRating);
		}
		return false;
	}

	@Override
	public boolean canConfigureDecisionTool() {
		return position.isDecisionTool() && (selectusManager || recruitingSecCallback.canConfigureDecisionTool()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToConfigureDecisionTool()));
	}
	
	@Override
	public boolean canEditDecisionRubrics() {
		return position.isDecisionTool() && (selectusManager || recruitingSecCallback.canEditDecisionRubrics()
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToEditDecisionRubrics()));
	}

	@Override
	public boolean canDeleteCache() {
		return recruitingSecCallback.canDeleteCache();
	}
	
	@Override
	public boolean canDeletePublicFeedbacks() {
		return selectusManager
				|| isAllowedByPositionRole(recruitingModule.getRolesAllowedToDeletePublicFeedback());
	}

	private boolean isAllowedByPositionRole(PositionRole[] allowedRoles) {
		boolean allowed = false;
		if(positionRole != null) {
			PositionRole[] roles = allowedRoles;
			for(PositionRole role:roles) {
				if(positionRole == role) {
					allowed = true;
				}
			}
		}
		return allowed;
	}
}
