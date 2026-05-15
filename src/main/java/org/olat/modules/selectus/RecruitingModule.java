/**

 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.selectus.AuditService.NotificationIntervals;
import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.Country;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.MailSettingEnum;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.PersonMaritalStatus;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.ReferenceSendMailType;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.log.NotificationPermission;
import org.olat.modules.selectus.site.PositionContextEntryControllerCreator;
import org.olat.modules.selectus.site.PositionsContextEntryControllerCreator;
import org.olat.modules.selectus.site.PublicFeedbackContextEntryControllerCreator;
import org.olat.modules.selectus.ui.AcademicalDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class RecruitingModule extends AbstractSpringModule implements ConfigOnOff {
	
	public static final String SELECTUS_ENABLED = "selectus.enabled";
	public static final String POSITIONS_LOGIN_ENABLED = "selectus.positions.login";
	
	public static final String NONE = "none";
	public static final String ALL = "all";
	
	public static final String APP_CUSTOM_FIELD_PREFIX = "custom_step_";
	public static final String APP_SECTION_PERSON = "personal";
	public static final String APP_SECTION_ACADEMICAL_BACKGROUND = "academical";
	public static final String APP_SECTION_PROJECT = "project";

	public static final String APP_ID = "application.id";
	public static final String APP_PERSON_TITLE = "application.person.title";
	public static final String APP_PERSON_GENDER = "application.person.gender";
	public static final String APP_PERSON_FIRSTNAME = "application.person.first.name";
	public static final String APP_PERSON_LASTNAME = "application.person.last.name";
	public static final String APP_PERSON_EMAIL = "application.person.email";
	public static final String APP_PERSON_ACADEMIC_TITLE = "application.person.academicTitle";
	public static final String APP_PERSON_BIRTHDAY = "application.person.birthday";
	public static final String APP_PERSON_MOBILE_PHONE = "application.person.mobile.phone";
	public static final String APP_PERSON_PHONE = "application.person.phone";
	public static final String APP_PERSON_NATIONALITY = "application.person.nationality";
	public static final String APP_PERSON_ADD_NATIONALITIES = "application.person.additional.nationalitie";
	public static final String APP_PERSON_MARITAL_STATUS = "application.person.marital.status";
	public static final String APP_PERSON_DISABILITY = "application.person.disability";
	
	public static final String APP_BUSINESS_INFOS_ORGANISATION = "application.businessinformations.organization";
	public static final String APP_BUSINESS_INFOS_UNIT = "application.businessinformations.unit";
	public static final String APP_BUSINESS_INFOS_POSITION = "application.businessinformations.currentPosition";
	
	public static final String APP_ADDRESS_PRIVATE_LINE1 = "application.address.private.line1";
	public static final String APP_ADDRESS_PRIVATE_LINE2 = "application.address.private.line2";
	public static final String APP_ADDRESS_PRIVATE_LINE3 = "application.address.private.line3";
	public static final String APP_ADDRESS_PRIVATE_CODE = "application.address.private.code";
	public static final String APP_ADDRESS_PRIVATE_CITY = "application.address.private.city";
	public static final String APP_ADDRESS_PRIVATE_COUNTRY = "application.address.private.country";
	
	public static final String APP_ADDRESS_BUSINESS_LINE1 = "application.address.business.line1";
	public static final String APP_ADDRESS_BUSINESS_LINE2 = "application.address.business.line2";
	public static final String APP_ADDRESS_BUSINESS_LINE3 = "application.address.business.line3";
	public static final String APP_ADDRESS_BUSINESS_CODE = "application.address.business.code";
	public static final String APP_ADDRESS_BUSINESS_CITY = "application.address.business.city";
	public static final String APP_ADDRESS_BUSINESS_COUNTRY = "application.address.business.country";

	public static final String APP_ADDRESS_BUSINESS_PHONE = "application.business.phone";
	public static final String APP_ADDRESS_BUSINESS_MAIL = "application.business.mail";
	
	public static final String APP_ACADEMIC_NUM_OF_ORIGINAL_PUBLICATIONS = "application.academicalBackground.numberOfOriginalPublications";
	public static final String APP_ACADEMIC_NUM_OF_FIRST_AUTHORSHIPS = "application.academicalBackground.numberOfFirstAuthorships";
	public static final String APP_ACADEMIC_NUM_OF_LAST_AUTHORSHIPS = "application.academicalBackground.numberOfLastAuthorships";
	public static final String APP_ACADEMIC_CITATIONS = "application.academicalBackground.citations";
	public static final String APP_ACADEMIC_IMPACT_FACTOR = "application.academicalBackground.impactFactor";
	public static final String APP_ACADEMIC_HFACTORY = "application.academicalBackground.hFactor";

	public static final String APP_ACADEMIC_HIGHEST_DEGREE = "application.academicalBackground.highestDegree";
	public static final String APP_ACADEMIC_HIGHEST_DEGREE_TYPE = "application.academicalBackground.highestDegree.type";
	public static final String APP_ACADEMIC_HIGHEST_DEGREE_YEAR = "application.academicalBackground.highestDegree.year";
	public static final String APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION = "application.academicalBackground.highestDegree.institution";

	public static final String APP_ACADEMIC_WORKED_IN_ACADEMIA_SINCE = "application.academicalBackground.workedInAcademiaSince";
	public static final String APP_ACADEMIC_WORKED_OUT_ACADEMIA_SINCE = "application.academicalBackground.workedOutAcademiaSince";
	public static final String APP_ACADEMIC_WORKED_OUT_ACADEMIA_CARE_SINCE = "application.academicalBackground.workedOutAcademiaCareSince";
	
	public static final String APP_ACADEMIC_CAREER_DESCRIPTION = "application.academicalBackground.careerDescription";
	public static final String APP_ACADEMIC_DISSERTATION = "application.academicalBackground.dissertation";
	public static final String APP_ACADEMIC_DISSERTATION_DATE = "application.academicalBackground.dissertation.date";
	public static final String APP_ACADEMIC_DISSERTATION_TITLE = "application.academicalBackground.dissertation.title";
	public static final String APP_ACADEMIC_DISSERTATION_INSTITUTION = "application.academicalBackground.dissertation.institution";
	public static final String APP_ACADEMIC_DISSERTATION_KEYWORD1 = "application.academicalBackground.dissertation.keyword1";
	public static final String APP_ACADEMIC_DISSERTATION_KEYWORD2 = "application.academicalBackground.dissertation.keyword2";
	public static final String APP_ACADEMIC_DISSERTATION_KEYWORD3 = "application.academicalBackground.dissertation.keyword3";
	public static final String APP_ACADEMIC_HABILITATION = "application.academicalBackground.habilitation";
	public static final String APP_ACADEMIC_HABILITATION_DATE = "application.academicalBackground.habilitation.date";
	public static final String APP_ACADEMIC_HABILITATION_TITLE = "application.academicalBackground.habilitation.title";
	public static final String APP_ACADEMIC_HABILITATION_INSTITUTION = "application.academicalBackground.habilitation.institution";
	public static final String APP_ACADEMIC_ORCID = "application.academicalBackground.orcid";

	public static final String APP_PROJECT_TITLE = "application.project.title";
	public static final String APP_PROJECT_DESCRIPTION = "application.project.description";
	public static final String APP_PROJECT_DURATION = "application.project.duration";
	public static final String APP_PROJECT_START_DATE = "application.project.start.date";
	
	public static final String APP_PROJECT_FINANCIAL_IMPACT_1 = "application.project.financial.impact.1";
	public static final String APP_PROJECT_FINANCIAL_IMPACT_2 = "application.project.financial.impact.2";
	public static final String APP_PROJECT_FINANCIAL_IMPACT_3 = "application.project.financial.impact.3";
	public static final String APP_PROJECT_FINANCIAL_IMPACT_4 = "application.project.financial.impact.4";
	public static final String APP_PROJECT_FINANCIAL_IMPACT_5 = "application.project.financial.impact.5";
	public static final String APP_PROJECT_ACRONYM = "application.project.acronym";
	public static final String APP_PROJECT_KEYWORDS = "application.project.keywords";
	public static final String APP_PROJECT_DISCIPLINES = "application.project.disciplines";
	
	public static final String NEW_POSITION_EXCLUDE_ATTRIBUTES = "new.position.exclude.attributes";
	
	private static final Logger log = Tracing.createLoggerFor(RecruitingModule.class);

	@Value("${selectus.enabled:false}")
	private boolean enabled;
	@Value("${selectus.module.available:false}")
	private boolean moduleAvailable;
	
	@Value("${selectus.positions.login:false}")
	private boolean positionsLoginEnabled;

	private int maxRating = 3;
	@Value("${recruiting.rating.abstention:disabled}")
	private String ratingAbstention;
	@Value("${recruiting.rejection.all.decision:disabled}")
	private String rejectionAllDecisionsStep;
	
	@Value("${recruiting.officeMail}")
	private String officeMail;
	@Value("${recruiting.staffMail}")
	private String staffMail;
	@Value("${recruiting.bcc.staffMail}")
	private String bccStaffMail;
	
	@Value("${recruiting.send.bcc.confirmation:true}")
	private boolean sendBccForConfirmation;
	@Value("${recruiting.mail.position:disabled}")
	private String mailProPosition;
	@Value("${recruiting.mail.letter:disabled}")
	private String mailLetter;
	
	@Value("${recruiting.position.status:preparation,published,publishedAndInScreening,closedAndInScreening,closedAndNoRating,closed}")
	private String positionStatus;
	
	@Value("${recruiting.decision.tool:disabled}")
	private String decisionTool;
	
	@Value("${recruiting.tagging.tool:disabled}")
	private String taggingTool;
	@Value("${recruiting.tagging.tool.system:disabled}")
	private String systemTag;
	@Value("${recruiting.tagging.tool.position:disabled}")
	private String positionTag;
	@Value("${recruiting.tagging.tool.administrative:disabled}")
	private String administrativeTag;

	@Value("${recruiting.notifications.tool:disabled}")
	private String notificationsTool;

	@Value("${recruiting.notifications.head}")
	private String allowedNotificationsForHead;
	@Value("${recruiting.notifications.secretary}")
	private String allowedNotificationsForSecretary;
	@Value("${recruiting.notifications.committee}")
	private String allowedNotificationsForCommittee;
	@Value("${recruiting.notifications.exofficio}")
	private String allowedNotificationsForExOfficio;

	@Value("${user.person.titles:Dr,Prof,ProfDr}")
	private String userPersonTitles;
	
	@Value("${recruiting.impressum.url}")
	private String impressumUrl;
	@Value("${recruiting.disclaimer.url}")
	private String disclaimerUrl;

	@Value("${recruiting.data.protection:enabled}")
	private String dataProtection;
	
	@Value("${recruiting.apply.term.last.step:disabled}")
	private String applyTermsLastStep;
	
	@Value("${application.memo:disabled}")
	private String applicationMemo;
	@Value("${application.committee.comment:disabled}")
	private String committeeComment;
	
	@Value("${application.duplicate.email:false}")
	private String applicationDuplicateEmail;
	@Value("${application.duplicate.algorithm:email}")
	private String applicationDuplicateAlgorithm;
	
	@Value("${role.exofficio.enable:disabled}")
	private String roleExOfficio;
	
	@Value("${role.rating:member}")
	private String rolesAllowedToRate;
	@Value("${role.see.rating:head,secretary,exofficio}")
	private String rolesAllowedToSeeRating;
	@Value("${role.see.rating.during.rating:head,secretary,exofficio}")
	private String rolesAllowedToSeeRatingDuringRating;
	@Value("${role.notes:member}")
	private String rolesAllowedToNotes;
	@Value("${role.export.applications.excel:null}")
	private String rolesAllowedToExportApplicationListExcel;
	@Value("${role.export.reviews.statistics.excel:null}")
	private String rolesAllowedToExportReviewsStatisticsExcel;
	@Value("${role.export.generated.list:null}")
	private String rolesAllowedToExportGeneratedList;
	@Value("${role.export.applications.pdf:null}")
	private String rolesAllowedToExportApplicationListPdf;
	@Value("${role.export.committee.excel:null}")
	private String rolesAllowedToExportCommitteeListExcel;
	@Value("${role.export.ratings.pdf:null}")
	private String rolesAllowedToExportRatingsPdf;
	
	@Value("${role.mail.center.view:null}")
	private String rolesAllowedToViewMailCenter;
	@Value("${role.mail.center.export.log:null}")
	private String rolesAllowedToExportMailCenterLog;
	@Value("${role.mail.center.view.mail:null}")
	private String rolesAllowedToViewMailCenterEmail;
	@Value("${role.mail.center.resend.mail:null}")
	private String rolesAllowedToResendMailCenterEmail;
	@Value("${role.send.mail.to.application:null}")
	private String rolesAllowedToSendMailToApplicant;
	@Value("${role.send.bulk.application.mails:null}")
	private String rolesAllowedToSendBulkApplicationEmail;
	
	@Value("${role.configure.decision.tool:null}")
	private String rolesAllowedToConfigureDecisionTool;
	@Value("${role.edit.decision.rubrics:null}")
	private String rolesAllowedToEditDecisionRubrics;
	@Value("${role.send.email.to.all.committee:null}")
	private String rolesAllowedToSendEmailAllCommittee;
	@Value("${role.create.application:null}")
	private String rolesAllowedToCreateApplications;
	@Value("${role.edit.application.personal.data:null}")
	private String rolesAllowedToEditApplicationPersonalData;
	@Value("${role.edit.application.academical.background:null}")
	private String rolesAllowedToEditApplicationAcademicalBackground;
	@Value("${role.edit.application.project:null}")
	private String rolesAllowedToEditApplicationProject;
	@Value("${role.edit.application.documents:null}")
	private String rolesAllowedToEditApplicationDocuments;
	@Value("${role.edit.application.status:null}")
	private String rolesAllowedToEditApplicationStatus;
	@Value("${role.edit.application.tags:null}")
	private String rolesAllowedToEditApplicationCategories;
	@Value("${role.delete.public.feedback:null}")
	private String rolesAllowedToDeletePublicFeedback;
	@Value("${role.edit.members.feedback:null}")
	private String rolesAllowedToEditMembersFeedback;
	@Value("${role.see.members.feedback:null}")
	private String rolesAllowedToSeeMembersFeedback;
	@Value("${role.delete.members.feedback:null}")
	private String rolesAllowedToDeleteMembersFeedback;
	
	@Value("${role.edit.application.references:null}")
	private String rolesAllowedToEditApplicationReferences;
	@Value("${role.edit.application.committee.comment:null}")
	private String rolesAllowedToEditApplicationCommitteeComment;
	@Value("${role.delete.application:null}")
	private String rolesAllowedToDeleteApplication;
	@Value("${role.see.parallel.applications:null}")
	private String rolesAllowedToSeeParallelApplications;
	@Value("${role.edit.committee.decision:null}")
	private String rolesAllowedToEditCommitteDecision;
	@Value("${role.see.published.positions:null}")
	private String rolesAllowedToSeePublishedPositions;
	@Value("${role.edit.application.memo:null}")
	private String rolesAllowedToEditApplicationMemo;
	@Value("${role.see.expert.black.list:null}")
	private String rolesAllowedToSeeExpertBlackList;
	@Value("${role.edit.administrative.tags:null}")
	private String rolesAllowedToEditAdministrativeTags;
	@Value("${role.see.administrative.tags:null}")
	private String rolesAllowedToSeeAdministrativeTags;
	
	@Value("${role.edit.position.status:null}")
	private String rolesAllowedToEditPositionStatus;
	@Value("${role.edit.position.profile:null}")
	private String rolesAllowedToEditPositionProfile;
	@Value("${role.edit.position.application.settings:null}")
	private String rolesAllowedToEditPositionApplicationsSettings;
	@Value("${role.edit.position.references.settings:null}")
	private String rolesAllowedToEditPositionReferencesSettings;
	@Value("${role.edit.position.feedbacks.settings:null}")
	private String rolesAllowedToEditPositionFeedbacksSettings;
	
	@Value("${role.edit.position.evaluation.settings:null}")
	private String rolesAllowedToEditPositionEvaluationSettings;
	@Value("${role.edit.position.tags:null}")
	private String rolesAllowedToEditPositionTagsSettings;
	@Value("${role.edit.position.mail.templates:null}")
	private String rolesAllowedToEditPositionMailTemplates;
	
	@Value("${role.share.filters.application.list:null}")
	private String rolesAllowedToShareApplicationListFilters;
	@Value("${role.manage.filters.application.list:null}")
	private String rolesAllowedToManageApplicationListFilters;
	
	@Value("${role.filters.application.list.basic:null}")
	private String rolesAllowedToApplicationBasicFilters;
	@Value("${role.filters.application.list.advanced:null}")
	private String rolesAllowedToApplicationAdvancedFilters;

	@Value("${role.edit.position.committee:null}")
	private String rolesAllowedToEditPositionCommittee;
	@Value("${role.add.position.committee:null}")
	private String rolesAllowedToAddPositionCommittee;
	@Value("${role.remove.position.committee:null}")
	private String rolesAllowedToRemovePositionCommittee;
	
	@Value("${role.search.applications:null}")
	private String rolesAllowedToSearchApplications;
	

	@Value("${role.edit.assignments:null}")
	private String rolesAllowedToEditAssignments;
	
	@Value("${share.filters:enabled}")
	private String shareFilters;
	
	@Value("${application.assignment.method:null}")
	private String applicationAssignmentMethod;
	
	@Value("${feedback.default.options:null}")
	private String feedbackDefaultOptions;
	@Value("${feedback.public:false}")
	private String publicFeedback;
	@Value("${feedback.members:false}")
	private String membersFeedback;
	
	@Value("${table.feedbacks.for.members.due.date:enabled}")
	private String tableFeedbacksForMembersDueDateEnable;
	
	@Value("${feedback.public.form.application.name:enabled}")
	private String publicFeedbackFormApplicationNameEnable;
	@Value("${feedback.public.form.due.date:enabled}")
	private String publicFeedbackFormDueDateEnable;
	
	@Value("${country.prefered}")
	private String countryPrefered;
	
	//table preferences
	@Value("${table.applications.generate.list:disabled}")
	private String tableApplicationsGenerateList;
	
	@Value("${table.applications.sort:id}")
	private String tableApplicationsSortField;
	@Value("${table.applications.sort.direction:desc}")
	private String tableApplicationsSortDirection;
	
	@Value("${table.applications.title:enabled}")
	private String tableApplicationsPersonTitle;
	@Value("${table.applications.firstName:enabled}")
	private String tableApplicationsPersonFirstName;
	@Value("${table.applications.lastName:enabled}")
	private String tableApplicationsPersonLastName;
	@Value("${table.applications.submittedDate:disabled}")
	private String tableApplicationsSubmittedDateEnable;
	@Value("${table.applications.submittedByStaff:optional}")
	private String tableApplicationsSubmittedByStaffEnable;
	@Value("${table.applications.nationality:optional}")
	private String tableApplicationsNationalityEnable;
	@Value("${table.applications.additional.nationalities:optional}")
	private String tableApplicationsAdditionalNationalitiesEnable;
	
	@Value("${table.applications.highestDegree:optional}")
	private String tableApplicationsHighestDegreeEnable;
	@Value("${table.applications.highestDegreeInstitution:optional}")
	private String tableApplicationsHighestDegreeInstitutionEnable;
	@Value("${table.applications.highestDegreeYear:disabled}")
	private String tableApplicationsHighestDegreeYearEnable;
	
	@Value("${table.applications.highestDegreeYear.onlyPhD:enabled}")
	private String tableApplicationsHighestDegreeYearOnlyPhDEnable;

	@Value("${table.applications.highestDegreeYear.workedInAcademiaSince:disabled}")
	private String tableApplicationsWorkedInAcademiaSinceEnable;
	@Value("${table.applications.highestDegreeYear.workedOutAcademiaSince:disabled}")
	private String tableApplicationsWorkedOutAcademiaSinceEnable;
	@Value("${table.applications.highestDegreeYear.workedOutAcademiaCareSince:disabled}")
	private String tableApplicationsWorkedOutAcademiaCareSinceEnable;
	
	@Value("${table.applications.dissertation.title:optional}")
	private String tableApplicationsDissertationTitleEnable;
	@Value("${table.applications.dissertation.date:optional}")
	private String tableApplicationsDissertationDateEnable;
	@Value("${table.applications.dissertation.institution:optional}")
	private String tableApplicationsDissertationInstitutionEnable;
	@Value("${table.applications.dissertation.keyword1:optional}")
	private String tableApplicationsDissertationKeyword1Enable;
	@Value("${table.applications.dissertation.keyword2:optional}")
	private String tableApplicationsDissertationKeyword2Enable;
	@Value("${table.applications.dissertation.keyword3:optional}")
	private String tableApplicationsDissertationKeyword3Enable;

	@Value("${table.applications.organization:enabled}")
	private String tableApplicationsOrganization;
	@Value("${table.applications.organization.unit:optional}")
	private String tableApplicationsOrganizationUnit;
	@Value("${table.applications.organization.currentPosition:optional}")
	private String tableApplicationsOrganizationCurrentPosition;
	
	@Value("${table.applications.email:enabled}")
	private String tableApplicationsEMailEnable;
	@Value("${table.applications.phone:enabled}")
	private String tableApplicationsPhoneEnable;
	@Value("${table.applications.mobile.phone:disabled}")
	private String tableApplicationsMobilePhoneEnable;
	@Value("${table.applications.marital.status:disabled}")
	private String tableApplicationsMaritalStatusEnable;
	@Value("${table.applications.gender:enabled}")
	private String tableApplicationsGenderEnable;
	
	@Value("${table.applications.year.of.birth:enabled}")
	private String tableApplicationsYearOfBirthEnable;
	@Value("${table.applications.birthday:enabled}")
	private String tableApplicationsBirthdayEnable;
	
	@Value("${table.applications.disability:disabled}")
	private String tableApplicationsDisabilityEnable;
	@Value("${table.applications.addressLines:enabled}")
	private String tableApplicationsAddressLinesEnable;
	@Value("${table.applications.businessAddressLines:enabled}")
	private String tableApplicationsBusinessAddressLinesEnable;
	@Value("${table.applications.zipcode:enabled}")
	private String tableApplicationsZipcodeEnable;
	@Value("${table.applications.businessZipcode:enabled}")
	private String tableApplicationsBusinessZipcodeEnable;
	@Value("${table.applications.experts:enabled}")
	private String tableApplicationsExpertsEnable;
	@Value("${table.applications.referees:enabled}")
	private String tableApplicationsRefereesEnable;
	@Value("${table.applications.comparative.experts:enabled}")
	private String tableApplicationsComparativeExpertsEnable;
	@Value("${table.applications.provided.experts.recommendations:enabled}")
	private String tableApplicationsProvidedExpertsRecommendationsEnable;
	@Value("${table.applications.memo:disabled}")
	private String tableApplicationsMemoEnable;
	@Value("${table.applications.committee.comment:disabled}")
	private String tableApplicationsCommitteeCommentEnable;
	@Value("${table.applications.status:optional}")
	private String tableApplicationsStatusEnable;
	@Value("${table.applications.status.date:optional}")
	private String tableApplicationsStatusDateEnable;
	@Value("${table.applications.parallel.applications:optional}")
	private String tableApplicationsParallelApplicationsEnable;
	
	@Value("${table.applications.project:enable}")
	private String tableApplicationsProjectEnable;
	@Value("${table.applications.project.title:disabled}")
	private String tableApplicationsProjectTitleEnable;
	@Value("${table.applications.project.acronym:disabled}")
	private String tableApplicationsProjectAcronymEnable;
	@Value("${table.applications.project.keywords:disabled}")
	private String tableApplicationsProjectKeywordsEnable;
	@Value("${table.applications.project.disciplines:disabled}")
	private String tableApplicationsProjectDisciplinesEnable;
	@Value("${table.applications.project.start.date:disabled}")
	private String tableApplicationsProjectStartDateEnable;
	@Value("${table.applications.project.duration:disabled}")
	private String tableApplicationsProjectDurationEnable;
	@Value("${table.applications.project.financialImpact.1:disabled}")
	private String tableApplicationsProjectFinancialImpact1Enable;
	@Value("${table.applications.project.financialImpact.2:disabled}")
	private String tableApplicationsProjectFinancialImpact2Enable;
	@Value("${table.applications.project.financialImpact.3:disabled}")
	private String tableApplicationsProjectFinancialImpact3Enable;
	@Value("${table.applications.project.financialImpact.4:disabled}")
	private String tableApplicationsProjectFinancialImpact4Enable;
	@Value("${table.applications.project.financialImpact.5:disabled}")
	private String tableApplicationsProjectFinancialImpact5Enable;
	@Value("${table.applications.project.description:enable}")
	private String tableApplicationsProjectDescriptionEnable;

	@Value("${table.applications.committeeRating:context}")
	private String tableApplicationsCommitteeRatingEnable;
	@Value("${table.applications.decision:context}")
	private String tableApplicationsDecisionEnable;

	@Value("${table.applications.reviews:enabled}")
	private String tableApplicationsReviewsEnable;
	@Value("${table.applications.myratings.always.visible:}")
	private String tableApplicationsMyRatingsAlwaysVisibleFor;
	@Value("${table.applications.myreviews.always.visible:}")
	private String tableApplicationsMyReviewsAlwaysVisibleFor;
	
	@Value("${table.applications.default.selected.advanced.filter}")
	private String tableApplicationsDefaultSelectedAdvancedFilter;
	@Value("${table.applications.default.advanced.filters:all,withoutCEmails}")
	private String tableApplicationsDefaultAdvancedFilters;
	@Value("${table.applications.default.filter.basic.fields:id}")
	private String tableApplicationsDefaultBasicFilterFields;
	@Value("${table.applications.default.filter.application.status:active}")
	private String tableApplicationsDefaultBasicFilterApplicationStatus;
	@Value("${table.applications.default.reset.filter.application:false}")
	private String tableApplicationsResetFilterApplication;
	@Value("${table.applications.default.advanced.withoutCEmails.preselected:all}")
	private String tableApplicationsWithoutCEmailsFilterPreSelection;
	

	@Value("${table.committee.userProperties:optional}")
	private String tableCommitteeUserProperties;
	
	@Value("${table.feedbacks.userProperties:optional}")
	private String tableFeedbacksUserProperties;
	@Value("${table.feedbacks.default.advanced.filters:all,feedbackStatus,applicationStatus,applicationDecision,applicationTags}")
	private String tableFeedbacksDefaultAdvancedFilters;

	@Value("${table.reference.application.fullname:enabled}")
	private String tableReferencesApplicationFullNameEnable;
	@Value("${table.reference.application.id:optional}")
	private String tableReferencesApplicationIdEnable;
	@Value("${table.reference.project.title:disabled}")
	private String tableReferencesProjectTitleEnable;
	@Value("${table.reference.project.acronym:disabled}")
	private String tableReferencesProjectAcronymEnable;
	@Value("${table.reference.project.keywords:disabled}")
	private String tableReferencesProjectKeywordsEnable;
	@Value("${table.reference.project.disciplines:disabled}")
	private String tableReferencesProjectDisciplinesEnable;
	@Value("${table.reference.project.start.date:disabled}")
	private String tableReferencesProjectStartDateEnable;
	@Value("${table.reference.project.duration:disabled}")
	private String tableReferencesProjectDurationEnable;
	@Value("${table.reference.project.financialImpact.1:disabled}")
	private String tableReferencesProjectFinancialImpact1Enable;
	@Value("${table.reference.project.financialImpact.2:disabled}")
	private String tableReferencesProjectFinancialImpact2Enable;
	@Value("${table.reference.project.financialImpact.3:disabled}")
	private String tableReferencesProjectFinancialImpact3Enable;
	@Value("${table.reference.project.financialImpact.4:disabled}")
	private String tableReferencesProjectFinancialImpact4Enable;
	@Value("${table.reference.project.financialImpact.5:disabled}")
	private String tableReferencesProjectFinancialImpact5Enable;
	@Value("${table.reference.project.description:disabled}")
	private String tableReferencesProjectDescriptionEnable;
	
	@Value("${table.reference.default.advanced.filters:all,referenceStatus,referenceType,applicationStatus,applicationDecision,applicationTags}")
	private String tableReferencesDefaultAdvancedFilters;
	
	@Value("${table.reference.to.application.title:optional}")
	private String tableReferenceToApplicationTitleEnable;
	@Value("${table.reference.to.application.firstName:enabled}")
	private String tableReferenceToApplicationFirstNameEnable;
	@Value("${table.reference.to.application.lastName:enabled}")
	private String tableReferenceToApplicationLastNameEnable;
	@Value("${table.reference.to.project.title:disabled}")
	private String tableReferenceToProjectTitleEnable;
	
	@Value("${table.applicant.dashboard.duedate:enabled}")
	private String tableApplicantDashboardDueDateEnable;
	
	@Value("${table.decision.application.title:optional}")
	private String tableDecisionApplicationTitleEnable;
	@Value("${table.decision.application.firstName:enabled}")
	private String tableDecisionApplicationFirstNameEnable;
	@Value("${table.decision.application.lastName:enabled}")
	private String tableDecisionApplicationLastNameEnable;
	@Value("${table.decision.project.title:disabled}")
	private String tableDecisionProjectTitleEnable;
	@Value("${table.decision.project.acronym:disabled}")
	private String tableDecisionProjectAcronymEnable;
	@Value("${table.decision.project.keywords:disabled}")
	private String tableDecisionProjectKeywordsEnable;
	@Value("${table.decision.project.disciplines:disabled}")
	private String tableDecisionProjectDisciplinesEnable;
	@Value("${table.decision.project.start.date:disabled}")
	private String tableDecisionProjectStartDateEnable;
	@Value("${table.decision.project.duration:disabled}")
	private String tableDecisionProjectDurationEnable;
	@Value("${table.decision.project.financialImpact.1:disabled}")
	private String tableDecisionProjectFinancialImpact1Enable;
	@Value("${table.decision.project.financialImpact.2:disabled}")
	private String tableDecisionProjectFinancialImpact2Enable;
	@Value("${table.decision.project.financialImpact.3:disabled}")
	private String tableDecisionProjectFinancialImpact3Enable;
	@Value("${table.decision.project.financialImpact.4:disabled}")
	private String tableDecisionProjectFinancialImpact4Enable;
	@Value("${table.decision.project.financialImpact.5:disabled}")
	private String tableDecisionProjectFinancialImpact5Enable;
	
	@Value("${table.mail.application.title:optional}")
	private String tableMailApplicationTitleEnable;
	@Value("${table.mail.application.firstName:enabled}")
	private String tableMailApplicationFirstNameEnable;
	@Value("${table.mail.application.lastName:enabled}")
	private String tableMailApplicationLastNameEnable;
	@Value("${table.mail.project.title:disabled}")
	private String tableMailProjectTitleEnable;
	@Value("${table.mail.project.acronym:disabled}")
	private String tableMailProjectAcronymEnable;
	@Value("${table.mail.project.keywords:disabled}")
	private String tableMailProjectKeywordsEnable;
	@Value("${table.mail.project.disciplines:disabled}")
	private String tableMailProjectDisciplinesEnable;
	@Value("${table.mail.project.start.date:disabled}")
	private String tableMailProjectStartDateEnable;
	@Value("${table.mail.project.duration:disabled}")
	private String tableMailProjectDurationEnable;
	@Value("${table.mail.project.financialImpact.1:disabled}")
	private String tableMailProjectFinancialImpact1Enable;
	@Value("${table.mail.project.financialImpact.2:disabled}")
	private String tableMailProjectFinancialImpact2Enable;
	@Value("${table.mail.project.financialImpact.3:disabled}")
	private String tableMailProjectFinancialImpact3Enable;
	@Value("${table.mail.project.financialImpact.4:disabled}")
	private String tableMailProjectFinancialImpact4Enable;
	@Value("${table.mail.project.financialImpact.5:disabled}")
	private String tableMailProjectFinancialImpact5Enable;
	@Value("${table.mail.project.description:disabled}")
	private String tableMailProjectDescriptionEnable;
	@Value("${table.mail.application.status:enabled}")
	private String tableMailApplicationStatusEnable;
	@Value("${table.mail.email.log.status:enabled}")
	private String tableMailEmailLogStatusEnable;

	@Value("${application.project:disabled}")
	private String applicationProject;
	@Value("${application.project.default:false}")
	private String applicationProjectDefault;
	
	@Value("${application.person.academicTitle:disabled}")
	private String applicationPersonAcademicTitle;
	@Value("${application.person.birthday:enabled}")
	private String applicationPersonBirthday;
	@Value("${application.person.mobile.phone:disabled}")
	private String applicationPersonMobilePhone;
	@Value("${application.person.phone:enabled}")
	private String applicationMobilePhone;
	@Value("${application.person.nationality:enabled}")
	private String applicationPersonNationality;
	@Value("${application.person.nationality.useCountry:true}")
	private String applicationPersonNationalityUseCountry;
	@Value("${application.person.additional.nationalities:disabled}")
	private String applicationPersonAdditionalNationalities;
	@Value("${application.person.additional.nationalities.useCountry:true}")
	private String applicationPersonAdditionalNationalitiesUseCountry;
	
	@Value("${application.academicalBackground.numberOfOriginalPublications:disabled}")
	private String applicationAcademicalBackgroundNumberOfOriginalPublications;
	@Value("${application.academicalBackground.numberOfFirstAuthorships:disabled}")
	private String applicationAcademicalBackgroundNumberOfFirstAuthorships;
	@Value("${application.academicalBackground.numberOfLastAuthorships:disabled}")
	private String applicationAcademicalBackgroundNumberOfLastAuthorships;
	@Value("${application.academicalBackground.citations:disabled}")
	private String applicationAcademicalBackgroundCitations;
	@Value("${application.academicalBackground.impactFactor:disabled}")
	private String applicationAcademicalBackgroundImpactFactor;
	@Value("${application.academicalBackground.hFactor:disabled}")
	private String applicationAcademicalBackgroundHFactor;
	
	@Value("${application.academicalBackground.highestDegree.types:master,md,phd}")
	private String applicationAcademicalBackgroundHighestDegreeTypes;
	
	@Value("${application.academicalBackground.highestDegree:disabled}")
	private String applicationAcademicalBackgroundHighestDegree;
	@Value("${application.academicalBackground.workedInAcademiaSince:disabled}")
	private String applicationAcademicalBackgroundWorkedInAcademiaSince;
	@Value("${application.academicalBackground.workedOutAcademiaSince:disabled}")
	private String applicationAcademicalBackgroundWorkedOutAcademiaSince;
	@Value("${application.academicalBackground.workedOutAcademiaCareSince:disabled}")
	private String applicationAcademicalBackgroundWorkedOutAcademiaCareSince;
	@Value("${application.academicalBackground.careerDescription:disabled}")
	private String applicationAcademicalBackgroundCareerDescription;
	@Value("${application.academicalBackground.dissertation:disabled}")
	private String applicationAcademicalBackgroundDissertation;
	@Value("${application.academicalBackground.dissertation.date:disabled}")
	private String applicationAcademicalBackgroundDissertationDate;
	@Value("${application.academicalBackground.dissertation.date.format:yyyy}")
	private String applicationAcademicalBackgroundDissertationDateFormat;
	@Value("${application.academicalBackground.dissertation.title:disabled}")
	private String applicationAcademicalBackgroundDissertationTitle;
	@Value("${application.academicalBackground.dissertation.institution:disabled}")
	private String applicationAcademicalBackgroundDissertationInstitution;
	@Value("${application.academicalBackground.dissertation.keyword1:disabled}")
	private String applicationAcademicalBackgroundDissertationKeyword1;
	@Value("${application.academicalBackground.dissertation.keyword2:disabled}")
	private String applicationAcademicalBackgroundDissertationKeyword2;
	@Value("${application.academicalBackground.dissertation.keyword3:disabled}")
	private String applicationAcademicalBackgroundDissertationKeyword3;
	@Value("${application.academicalBackground.habilitation:disabled}")
	private String applicationAcademicalBackgroundHabilitation;
	@Value("${application.academicalBackground.habilitation.date:disabled}")
	private String applicationAcademicalBackgroundHabilitationDate;
	@Value("${application.academicalBackground.habilitation.title:disabled}")
	private String applicationAcademicalBackgroundHabilitationTitle;
	@Value("${application.academicalBackground.habilitation.institution:disabled}")
	private String applicationAcademicalBackgroundHabilitationInstitution;
	
	@Value("${application.academicalBackground.orcid:disabled}")
	private String applicationAcademicalBackgroundOrcid;
	
	@Value("${application.person.title:enabled}")
	private String applicationPersonTitle;
	@Value("${application.person.titles:Dr,Prof,ProfDr}")
	private String applicationPersonTitles;
	@Value("${application.person.genders:male,female}")
	private String applicationPersonGenders;
	@Value("${application.person.genders.default}")
	private String applicationPersonGenderDefault;
	@Value("${application.person.gender:enabled}")
	private String applicationPersonGender;
	@Value("${application.person.marital.status:disabled}")
	private String applicationPersonMaritalStatus;
	@Value("${application.person.disability:disabled}")
	private String applicationPersonDisability;
	@Value("${application.person.marital.status.list:single,married,unmarried,divorced,widowed}")
	private String applicationPersonMaritalStatusList;

	@Value("${application.address.private:xor}")
	private String applicationAddressPrivate;
	@Value("${application.address.business:xor}")
	private String applicationAddressBusiness;
	@Value("${application.address.line.3:enabled}")
	private String applicationAddressLine3;
	@Value("${application.business.phone:disabled}")
	private String applicationBusinessPhone;
	@Value("${application.business.mail:disabled}")
	private String applicationBusinessMail;
	
	@Value("${application.address.country:enabled}")
	private String applicationAddressCountry;
	@Value("${application.address.default.country:null}")
	private String applicationDefaultCountry;
	
	@Value("${application.businessinformations.organization:enabled}")
	private String applicationBusinessInformationsOrganization;
	@Value("${application.businessinformations.organization.list}")
	private String applicationBusinessInformationsOrganizationListOfValues;
	@Value("${application.businessinformations.unit:enabled}")
	private String applicationBusinessInformationsUnit;
	@Value("${application.businessinformations.currentPosition:enabled}")
	private String applicationBusinessInformationsCurrentPosition;
	
	@Value("${application.project.financial.impact.1:enabled}")
	private String applicationProjectFinancialImpact1;
	@Value("${application.project.financial.impact.1.type:text}")
	private String applicationProjectFinancialImpact1Type;
	@Value("${application.project.financial.impact.2:disabled}")
	private String applicationProjectFinancialImpact2;
	@Value("${application.project.financial.impact.2.type:text}")
	private String applicationProjectFinancialImpact2Type;
	@Value("${application.project.financial.impact.3:disabled}")
	private String applicationProjectFinancialImpact3;
	@Value("${application.project.financial.impact.3.type:text}")
	private String applicationProjectFinancialImpact3Type;
	@Value("${application.project.financial.impact.4:disabled}")
	private String applicationProjectFinancialImpact4;
	@Value("${application.project.financial.impact.4.type:text}")
	private String applicationProjectFinancialImpact4Type;
	@Value("${application.project.financial.impact.5:disabled}")
	private String applicationProjectFinancialImpact5;
	@Value("${application.project.financial.impact.5.type:text}")
	private String applicationProjectFinancialImpact5Type;
	
	@Value("${application.project.acronym:disabled}")
	private String applicationProjectAcronym;
	@Value("${application.project.keywords:disabled}")
	private String applicationProjectKeywords;
	@Value("${application.project.disciplines:disabled}")
	private String applicationProjectDisciplines;
	@Value("${application.project.start.date:disabled}")
	private String applicationProjectStartDate;
	@Value("${application.project.duration:disabled}")
	private String applicationProjectDuration;
	@Value("${application.project.title:disabled}")
	private String applicationProjectTitle;
	@Value("${application.project.description:disabled}")
	private String applicationProjectDescription;

	@Value("${application.project.description.max.length:7000}")
	private int applicationProjectDescriptionMaxLength;
	
	@Value("${application.status:active,onhold,withdrawn,rejected}")
	private String applicationStatus;

	@Value("${application.pdf.page.separator:enabled}")
	private String applicationPdfPageSeparator;
	
	@Value("${application.reference.visible.status:sentAwaiting,submitted,late}")
	private String applicationReferencesVisibleStatusOption;

	@Value("${application.details.hidden.fields}")
	private String applicationDetailsHiddenFields;
	
	@Value("${parallel.applications.scope:all}")
	private String parallelApplicationsScope;

	@Value("${application.copy:enabled}")
	private String copyApplications;

	//professorship.type
	@Value("${professorship.type:enabled}")
	private String professorshipType;
	
	//mail templates
	@Value("${mail.template.titles}")
	private String mailTemplateTitles;
	@Value("${mail.template.rejection.title:def}")
	private String mailTemplateRejectionTitle;
	@Value("${mail.template.tool.positions:enabled}")
	private String mailTemplateToolPositions;

	@Value("${mail.center.exclusion.filter:alreadySent}")
	private String mailCenterExclusionFilter;
	
	//rating
	@Value("${rating.policy.focus:enabled}")
	private String ratingPolicyFocus;
	@Value("${rating.policy.potential.candidates:enabled}")
	private String ratingPolicyPotentialCandidates;
	@Value("${rating.policy.professorship.type.generic.explanation.candidates:disabled}")
	private String ratingPolicyProfessorshipTypeGenericExplanation;
	
	//reference
	@Value("${reference.enable:disabled}")
	private String reference;
	@Value("${reference.number.of.disclaimers:0}")
	private int referenceDisclaimers;
	@Value("${reference.template.0:null}")
	private String referenceTemplate0;
	@Value("${reference.template.1:null}")
	private String referenceTemplate1;
	@Value("${reference.explanation.edit.application.enable:disabled}")
	private String referenceExplanationInEditApplication;
	@Value("${reference.send.email:auto}")
	private String referenceSendEmail;
	@Value("${reference.privacy.disclaimer:enabled}")
	private String referencePrivacyDisclaimer;
	@Value("${reference.one.time.code:disabled}")
	private String referenceOneTimeCode;
	@Value("${reference.consent:disabled}")
	private String referenceConsent;
	@Value("${reference.experts.blacklist:disabled}")
	private String referenceExpertsBlackList;
	@Value("${reference.titles:Dr,Prof,ProfDr}")
	private String referencePersonTitles;
	@Value("${reference.admin.notes:disabled}")
	private String referenceAdminNotes;
	
	@Value("${reference.referee.consent:disabled}")
	private String referenceRefereeConsent;
	
	@Value("${reference.applicant.management:disabled}")
	private String referenceApplicantManagement;
	
	@Value("${reference.comparative.assessment.experts:disabled}")
	private String comparativeAssessmentExperts;

	//review
	@Value("${review.enable:disabled}")
	private String review;
	@Value("${review.default.slider.steps:5}")
	private Integer reviewDefaultSliderSteps;
	@Value("${review.statistics.zero.based:false}")
	private boolean reviewStatisticsZeroBased;
	@Value("${review.statistics.enable:true}")
	private boolean reviewStatisticsEnabled;
	@Value("${review.statistics.chart:true}")
	private boolean reviewStatisticsChartEnabled;
	@Value("${review.statistics.download:true}")
	private boolean reviewStatisticsDownloadEnabled;
	
	//position languages
	@Value("${position.languages}")
	private String positionLanguages;
	@Value("${position.default.language}")
	private String positionDefaultLanguage;

	@Value("${position.description.rows:8}")
	private int positionDescriptionRows;
	
	@Value("${position.copy:enabled}")
	private String positionCopy;
	
	@Value("${position.jobads.other:enabled}")
	private String positionJobAdsOther;
	@Value("${position.jobads.freetext.only:disabled}")
	private String positionJobAdsFreeTextOnly;

	@Value("${position.academical.background.configurable:disabled}")
	private String positionAcademicalBackgroundConfigurationEnabled;
	
	@Value("${position.custom.steps:disabled}")
	private String positionCustomStepsEnabled;
	
	@Value("${position.max.number.additional.attributes:3}")
	private int positionMaxNumberOfAdditionalAttributes;
	
	//fields
	@Value("${position.planningId:enabled}")
	private String positionPlanningId;
	@Value("${position.department:enabled}")
	private String positionDepartment;
	@Value("${position.department.prefill:enabled}")
	private String positionDepartmentPrefill;
	@Value("${position.homepage:enabled}")
	private String positionHomepage;
	@Value("${position.orgUnit:optional}")
	private String positionOrgUnit;
	
	@Value("${new.position.exclude.attributes:}")
	private String newPositionExcludedFields;
	
	//documents
	@Value("${doc.coveringLetter:5}")
	private String docCoveringLetter;
	@Value("${doc.curriculumVitae:5}")
	private String docCurriculumVitae;
	@Value("${doc.publications:5}")
	private String docPublications;
	@Value("${doc.statements:5}")
	private String docStatements;
	@Value("${doc.proposals:5}")
	private String docProposals;
	@Value("${doc.leadership:0}")
	private String docLeadership;
	@Value("${doc.referees:5}")
	private String docReferees;
	@Value("${doc.projects:5}")
	private String docProjects;
	@Value("${doc.references:5}")
	private String docReferences;
	@Value("${doc.teachingAssessment:0}")
	private String docTeachingAssessment;
	@Value("${doc.certificateOfStudy:0}")
	private String docCertificateOfStudy;
	@Value("${doc.degreeCertificates:0}")
	private String docDegreeCertificates;
	@Value("${doc.dissertation:0}")
	private String docDissertation;
	@Value("${doc.habilitation:0}")
	private String docHabilitation;
	@Value("${doc.clinicalDisciplines:0}")
	private String docClinicalDisciplines;
	@Value("${doc.surgicalDisciplines:0}")
	private String docSurgicalDisciplines;
	@Value("${doc.reprints:0}")
	private String docReprints;
	@Value("${doc.externalFunding:0}")
	private String docExternalFunding;
	@Value("${doc.publication1:0}")
	private String docPublication1;
	@Value("${doc.publication2:0}")
	private String docPublication2;
	@Value("${doc.publication3:0}")
	private String docPublication3;
	@Value("${doc.publication4:0}")
	private String docPublication4;
	@Value("${doc.publication5:0}")
	private String docPublication5;
	@Value("${doc.other:5}")
	private String docOther;
	
	@Value("${doc.combined:5}")
	private String docCombined;

	@Value("${doc.in.combined:all}")
	private String docInCombined;
	@Value("${doc.combined.cover:v2}")
	private String docCombinedCover;
	
	@Value("${doc.types:pdf}")
	private String docTypes;

	@Value("${position.referee.recommendation.docs}")
	private String defaultPositionRefereeRecommendationDocs;
	@Value("${position.expert.recommendation.docs}")
	private String defaultPositionExpertRecommendationDocs;
	@Value("${position.advertisement.default:enabled}")
	private String defaultPositionAdvertisement;
	
	@Value("${attachment.on.file.system:false}")
	private boolean attachmentOnFileSystem;
	
	@Value("${about.url:null}")
	private String aboutUrl;
	
	@Value("${user.property.genders:disabled}")
	private String userPropertyGender;
	@Value("${user.property.typeOf:disabled}")
	private String userPropertyTypeOf;
	
	@Value("${reporting:disabled}")
	private String reporting;
	@Value("${delete.anonymous:disabled}")
	private String deleteAnonymous;
	
	@Value("${reporting.keep.person.gender:true}")
	private String reportingKeepGender;
	@Value("${reporting.keep.person.marital.status:true}")
	private String reportingKeepMaritalStatus;
	@Value("${reporting.keep.person.birthday:true}")
	private String reportingKeepBirthday;
	@Value("${reporting.keep.person.nationality:true}")
	private String reportingKeepNationality;
	@Value("${reporting.keep.person.private.country:true}")
	private String reportingKeepPrivateCountry;
	@Value("${reporting.keep.person.business.country:true}")
	private String reportingKeepBusinessCountry;
	
	@Value("${reporting.keep.academicalBackground.highestDegree.type:true}")
	private String reportingKeepAcademicalBackgroundHighestDegreeType;
	@Value("${reporting.keep.academicalBackground.highestDegree.year:true}")
	private String reportingKeepAcademicalBackgroundHighestDegreeYear;
	@Value("${reporting.keep.academicalBackground.dissertation.date:true}")
	private String reportingKeepAcademicalBackgroundDissertationDate;
	@Value("${reporting.keep.academicalBackground.habilitation.date:true}")
	private String reportingKeepAcademicalBackgroundHabilitationDate;
	
	@Value("${reporting.keep.academicalBackground.numberOfOriginalPublications:true}")
	private String reportingKeepAcademicalBackgroundNumberOfOriginalPublications;
	@Value("${reporting.keep.academicalBackground.numberOfFirstAuthorships:true}")
	private String reportingKeepAcademicalBackgroundNumberOfFirstAuthorships;
	@Value("${reporting.keep.academicalBackground.numberOfLastAuthorships:true}")
	private String reportingKeepAcademicalBackgroundNumberOfLastAuthorships;
	@Value("${reporting.keep.academicalBackground.citations:true}")
	private String reportingKeepAcademicalBackgroundCitations;
	@Value("${reporting.keep.academicalBackground.impactFactor:true}")
	private String reportingKeepAcademicalBackgroundImpactFactor;
	@Value("${reporting.keep.academicalBackground.hFactor:true}")
	private String reportingKeepAcademicalBackgroundHFactor;
	
	@Value("${reporting.keep.project.start.date:true}")
	private String reportingKeepProjectStartDate;
	@Value("${reporting.keep.project.financial.impact.1:true}")
	private String reportingKeepProjectFinancialImpact1;
	@Value("${reporting.keep.project.financial.impact.2:true}")
	private String reportingKeepProjectFinancialImpact2;
	@Value("${reporting.keep.project.financial.impact.3:true}")
	private String reportingKeepProjectFinancialImpact3;
	@Value("${reporting.keep.project.financial.impact.4:true}")
	private String reportingKeepProjectFinancialImpact4;
	@Value("${reporting.keep.project.financial.impact.5:true}")
	private String reportingKeepProjectFinancialImpact5;

	@Value("${reporting.keep.application.status:true}")
	private String reportingKeepApplicationStatus;
	@Value("${reporting.keep.application.decision:true}")
	private String reportingKeepApplicationDecision;
	@Value("${reporting.keep.application.submission.date:true}")
	private String reportingKeepApplicationSubmissionDate;
	
	@Value("${reporting.keep.application.ratings.a:true}")
	private String reportingKeepApplicationRatingsA;
	@Value("${reporting.keep.application.ratings.b:true}")
	private String reportingKeepApplicationRatingsB;
	@Value("${reporting.keep.application.ratings.c:true}")
	private String reportingKeepApplicationRatingsC;
	@Value("${reporting.keep.application.ratings.absention:true}")
	private String reportingKeepApplicationRatingsAbstentions;
	
	@Value("${reporting.keep.application.referees:true}")
	private String reportingKeepApplicationNumReferees;
	@Value("${reporting.keep.application.referees.docs:true}")
	private String reportingKeepApplicationNumRefereesDocuments;
	@Value("${reporting.keep.application.experts:true}")
	private String reportingKeepApplicationNumExperts;
	@Value("${reporting.keep.application.experts.docs:true}")
	private String reportingKeepApplicationNumExpertsDocuments;
	@Value("${reporting.keep.application.comparative.experts:true}")
	private String reportingKeepApplicationNumComparativeExperts;
	@Value("${reporting.keep.application.comparative.experts.docs:true}")
	private String reportingKeepApplicationNumComparativeExpertsDocuments;
	
	@Value("${reporting.keep.application.system.tags:true}")
	private String reportingKeepApplicationSystemTags;
	
	@Value("${reporting.keep.position.title:true}")
	private String reportingKeepPositionTitle;
	@Value("${reporting.keep.position.short.title:true}")
	private String reportingKeepPositionShortTitle;
	@Value("${reporting.keep.position.planningId:true}")
	private String reportingKeepPositionPlanningId;
	@Value("${reporting.keep.position.orgUnit:true}")
	private String reportingKeepPositionOrgUnit;
	@Value("${reporting.keep.position.department:true}")
	private String reportingKeepPositionDepartment;
	@Value("${reporting.keep.position.applicationDeadline:true}")
	private String reportingKeepPositionApplicationDeadline;
	@Value("${reporting.keep.position.ratingDeadline:true}")
	private String reportingKeepPositionRatingDeadline;
	
	@Value("${reporting.keep.committee.role:true}")
	private String reportingKeepCommitteeRole;
	@Value("${reporting.keep.committee.rating.rights:true}")
	private String reportingKeepCommitteeRatingRights;
	@Value("${reporting.keep.committee.gender:true}")
	private String reportingKeepCommitteeGender;
	@Value("${reporting.keep.committee.user.classification:true}")
	private String reportingKeepCommitteeUserClassification;
	@Value("${reporting.keep.committee.number.ratings.a:true}")
	private String reportingKeepCommitteeNumberRatingsA;
	@Value("${reporting.keep.committee.number.ratings.b:true}")
	private String reportingKeepCommitteeNumberRatingsB;
	@Value("${reporting.keep.committee.number.ratings.c:true}")
	private String reportingKeepCommitteeNumberRatingsC;
	@Value("${reporting.keep.committee.number.ratings.abstentions:true}")
	private String reportingKeepCommitteeNumberRatingsAbstentions;
	
	@Autowired
	private List<DecisionRubricSPI> decisionRubricSpies;
	
	private int uploadLimit = 50000;
	
	private PositionStatus[] positionStatusAvailable;
	
	private PositionRole[] positionRolesAllowedToRate;
	private PositionRole[] positionRolesAllowedToSeeRating;
	private PositionRole[] positionRolesAllowedToSeeRatingDuringRating;
	private PositionRole[] positionRolesAllowedToTakeNotes;
	private PositionRole[] positionRolesAllowedToExportApplicationListExcel;
	private PositionRole[] positionRolesAllowedToExportReviewsStatisticsExcel;
	private PositionRole[] positionRolesAllowedToExportGeneratedList;
	private PositionRole[] positionRolesAllowedToExportApplicationListPdf;
	private PositionRole[] positionRolesAllowedToExportCommitteeListExcel;
	private PositionRole[] positionRolesAllowedToExportRatingsPdf;
	
	private PositionRole[] positionRolesAllowedToViewMailCenter;
	private PositionRole[] positionRolesAllowedToExportMailCenterLog;
	private PositionRole[] positionRolesAllowedToViewMailCenterEmail;
	private PositionRole[] positionRolesAllowedToResendMailCenterEmail;
	private PositionRole[] positionRolesAllowedToSendMailToApplicant;
	
	private PositionRole[] positionRolesAllowedToConfigureDecisionTool;
	private PositionRole[] positionRolesAllowedToEditDecisionRubrics;
	private PositionRole[] positionRolesAllowedToSendEmailAllCommittee;
	private PositionRole[] positionRolesAllowedToCreateApplications;
	private PositionRole[] positionRolesAllowedToEditApplicationPersonalData;
	private PositionRole[] positionRolesAllowedToEditApplicationAcademicalBackground;
	private PositionRole[] positionRolesAllowedToEditApplicationProject;
	private PositionRole[] positionRolesAllowedToEditApplicationDocuments;
	private PositionRole[] positionRolesAllowedToEditApplicationStatus;
	private PositionRole[] positionRolesAllowedToEditApplicationCategories;
	private PositionRole[] positionRolesAllowedToDeletePublicFeedback;
	private PositionRole[] positionRolesAllowedToEditMembersFeedback;
	private PositionRole[] positionRolesAllowedToSeeMembersFeedback;
	private PositionRole[] positionRolesAllowedToDeleteMembersFeedback;
	private PositionRole[] positionRolesAllowedToSendBulkApplicationEmails;
	private PositionRole[] positionRolesAllowedToEditApplicationReferences;
	private PositionRole[] positionRolesAllowedToEditApplicationCommitteeComment;
	private PositionRole[] positionRolesAllowedToDeleteApplication;
	private PositionRole[] positionRolesAllowedToSeeParallelApplications;
	private PositionRole[] positionRolesAllowedToEditCommitteDecision;
	private PositionRole[] positionRolesAllowedToSeePublishedPositions;
	private PositionRole[] positionRolesAllowedToEditApplicationsMemo;
	private PositionRole[] positionRolesAllowedToSeeExpertBlackList;
	private PositionRole[] positionRolesAllowedToEditAdministrativeTags;
	private PositionRole[] positionRolesAllowedToSeeAdministrativeTags;
	
	private PositionRole[] positionRolesAllowedToEditPositionStatus;
	private PositionRole[] positionRolesAllowedToEditPositionProfile;
	private PositionRole[] positionRolesAllowedToEditPositionApplicationsSettings;
	private PositionRole[] positionRolesAllowedToEditPositionReferencesSettings;
	private PositionRole[] positionRolesAllowedToEditPositionEvaluationSettings;
	private PositionRole[] positionRolesAllowedToEditPositionTagsSettings;
	private PositionRole[] positionRolesAllowedToEditPositionMailTemplates;
	private PositionRole[] positionRolesAllowedToEditPositionFeedbacksSettings;
	private PositionRole[] positionRolesAllowedToAddPositionCommittee;
	private PositionRole[] positionRolesAllowedToEditPositionCommittee;
	private PositionRole[] positionRolesAllowedToRemovePositionCommittee;
	

	private PositionRole[] positionRolesAllowedToSearchApplications;
	
	private PositionRole[] positionRolesAllowedToEditAssignments;
	
	private FilterPermissions[] positionRolesAllowedToShareApplicationListFilters;
	private FilterPermissions[] positionRolesAllowedToManageApplicationListFilters;
	private FilterPermissions[] positionRolesAllowedToApplicationBasicFilters;
	private FilterPermissions[] positionRolesAllowedToApplicationAdvancedFilters;
	
	private List<String> newPositionExcludedAttributesList;

	private PersonTitle[] userTitles;
	private PersonTitle[] referenceTitles;
	
	private AssignmentMethods[] assignmentMethods;
	
	private String[] feedbackOptions;
	
	private Locale[] positionLocales;
	private Locale positionDefaultLocale;
	
	private Country[] preferedCountries;
	private PersonTitle[] personTitles;
	private PersonGender[] personGenders;
	private PersonGender personDefaultGender;
	private PersonMaritalStatus[] maritalStatusList;
	
	private HighestDegreeType[] highestDegreeTypes;
	
	private AcademicalDateFormat[] dissertationDateFormats;
	
	private AddressOption applicationAddressPrivateOption;
	private AddressOption applicationAddressBusinessOption;
	
	private ReferenceStatus[] applicationReferencesVisibleStatus;
	
	private ApplicationStatus[] applicationAvailableStatus;
	
	private ApplicationStatus[] applicationsDefaultBasicFilterApplicationStatus;

	private ApplicationFieldType applicationProjectFinancialImpact1FieldType;
	private ApplicationFieldType applicationProjectFinancialImpact2FieldType;
	private ApplicationFieldType applicationProjectFinancialImpact3FieldType;
	private ApplicationFieldType applicationProjectFinancialImpact4FieldType;
	private ApplicationFieldType applicationProjectFinancialImpact5FieldType;

	private RecruitingTableOption tableApplicationsPersonTitleOption;
	private RecruitingTableOption tableApplicationsPersonFirstNameOption;
	private RecruitingTableOption tableApplicationsPersonLastNameOption;
	
	private RecruitingTableOption tableApplicationsOrganizationOption;
	private RecruitingTableOption tableApplicationsOrganizationUnitOption;
	private RecruitingTableOption tableApplicationsOrganizationCurrentPositionOption;
	private RecruitingTableOption tableApplicationsSubmittedDateOption;
	private RecruitingTableOption tableApplicationsSubmittedByStaffOption;
	private RecruitingTableOption tableApplicationsNationalityOption;
	private RecruitingTableOption tableApplicationsAdditionalNationalitiesOption;
	private RecruitingTableOption tableApplicationsHighestDegreeOption;
	private RecruitingTableOption tableApplicationsHighestDegreeInstitutionOption;
	private RecruitingTableOption tableApplicationsHighestDegreeYearOption;
	private RecruitingTableOption tableApplicationsWorkedInAcademiaSinceOption;
	private RecruitingTableOption tableApplicationsWorkedOutAcademiaSinceOption;
	private RecruitingTableOption tableApplicationsWorkedOutAcademiaCareSinceOption;
	
	private RecruitingTableOption tableApplicationsDissertationTitleOption;
	private RecruitingTableOption tableApplicationsDissertationDateOption;
	private RecruitingTableOption tableApplicationsDissertationInstitutionOption;
	private RecruitingTableOption tableApplicationsDissertationKeyword1Option;
	private RecruitingTableOption tableApplicationsDissertationKeyword2Option;
	private RecruitingTableOption tableApplicationsDissertationKeyword3Option;
	
	private RecruitingTableOption tableApplicationsEMailOption;
	private RecruitingTableOption tableApplicationsPhoneOption;
	private RecruitingTableOption tableApplicationsMobilePhoneOption;
	private RecruitingTableOption tableApplicationsGenderOption;
	private RecruitingTableOption tableApplicationsMaritalStatusOption;
	private RecruitingTableOption tableApplicationsYearOfBirthOption;
	private RecruitingTableOption tableApplicationsBirthdayOption;
	private RecruitingTableOption tableApplicationsDisabilityOption;
	private RecruitingTableOption tableApplicationsZipcodeOption;
	private RecruitingTableOption tableApplicationsAddressLinesOption;
	private RecruitingTableOption tableApplicationsBusinessZipcodeOption;
	private RecruitingTableOption tableApplicationsBusinessAddressLinesOption;
	private RecruitingTableOption tableApplicationsExpertsOption;
	private RecruitingTableOption tableApplicationsRefereesOption;
	private RecruitingTableOption tableApplicationsComparativeExpertsOption;
	private RecruitingTableOption tableApplicationsProvidedExpertsRecommendationsOption;
	private RecruitingTableOption tableApplicationsStatusOption;
	private RecruitingTableOption tableApplicationsStatusDateOption;
	private RecruitingTableOption tableApplicationsParallelApplicationsOption;
	private RecruitingTableOption tableApplicationsMemoOption;
	private RecruitingTableOption tableApplicationsCommitteeCommentOption;
	
	private RecruitingTableOption tableApplicationsProjectOption;
	private RecruitingTableOption tableApplicationsProjectTitleOption;
	private RecruitingTableOption tableApplicationsProjectAcronymOption;
	private RecruitingTableOption tableApplicationsProjectKeywordsOption;
	private RecruitingTableOption tableApplicationsProjectDisciplinesOption;
	private RecruitingTableOption tableApplicationsProjectStartDateOption;
	private RecruitingTableOption tableApplicationsProjectDurationOption;
	private RecruitingTableOption tableApplicationsProjectFinancialImpact1Option;
	private RecruitingTableOption tableApplicationsProjectFinancialImpact2Option;
	private RecruitingTableOption tableApplicationsProjectFinancialImpact3Option;
	private RecruitingTableOption tableApplicationsProjectFinancialImpact4Option;
	private RecruitingTableOption tableApplicationsProjectFinancialImpact5Option;
	private RecruitingTableOption tableApplicationsProjectDescriptionOption;
	
	private RecruitingTableContextualOption tableApplicationsCommitteeRating;
	private RecruitingTableContextualOption tableApplicationsDecision;

	private RecruitingTableOption tableApplicationsReviewsOption;
	private PositionRole[] tableApplicationsMyRatingsAlwaysVisibleForRoles;
	private PositionRole[] tableApplicationsMyReviewsAlwaysVisibleForRoles;
	
	private RecruitingTableOption tableCommitteeUserPropertiesOption;
	
	private RecruitingTableOption tableFeedbacksUserPropertiesOption;
	
	private RecruitingTableOption tableReferencesApplicationFullNameOption;
	private RecruitingTableOption tableReferencesApplicationIdOption;
	private RecruitingTableOption tableReferencesProjectTitleOption;
	private RecruitingTableOption tableReferencesProjectAcronymOption;
	private RecruitingTableOption tableReferencesProjectKeywordsOption;
	private RecruitingTableOption tableReferencesProjectDisciplinesOption;
	private RecruitingTableOption tableReferencesProjectStartDateOption;
	private RecruitingTableOption tableReferencesProjectDurationOption;
	private RecruitingTableOption tableReferencesProjectFinancialImpact1Option;
	private RecruitingTableOption tableReferencesProjectFinancialImpact2Option;
	private RecruitingTableOption tableReferencesProjectFinancialImpact3Option;
	private RecruitingTableOption tableReferencesProjectFinancialImpact4Option;
	private RecruitingTableOption tableReferencesProjectFinancialImpact5Option;
	private RecruitingTableOption tableReferencesProjectDescriptionOption;
	
	private RecruitingTableOption tableReferenceToApplicationTitleOption;
	private RecruitingTableOption tableReferenceToApplicationFirstNameOption;
	private RecruitingTableOption tableReferenceToApplicationLastNameOption;
	private RecruitingTableOption tableReferenceToProjectTitleOption;
	
	private RecruitingTableOption tableApplicantDashboardDueDateOption;
	
	private RecruitingTableOption tableDecisionApplicationTitleOption;
	private RecruitingTableOption tableDecisionApplicationFirstNameOption;
	private RecruitingTableOption tableDecisionApplicationLastNameOption;
	private RecruitingTableOption tableDecisionProjectTitleOption;
	private RecruitingTableOption tableDecisionProjectAcronymOption;
	private RecruitingTableOption tableDecisionProjectKeywordsOption;
	private RecruitingTableOption tableDecisionProjectDisciplinesOption;
	private RecruitingTableOption tableDecisionProjectStartDateOption;
	private RecruitingTableOption tableDecisionProjectDurationOption;
	private RecruitingTableOption tableDecisionProjectFinancialImpact1Option;
	private RecruitingTableOption tableDecisionProjectFinancialImpact2Option;
	private RecruitingTableOption tableDecisionProjectFinancialImpact3Option;
	private RecruitingTableOption tableDecisionProjectFinancialImpact4Option;
	private RecruitingTableOption tableDecisionProjectFinancialImpact5Option;
	
	private RecruitingTableOption tableMailApplicationTitleOption;
	private RecruitingTableOption tableMailApplicationFirstNameOption;
	private RecruitingTableOption tableMailApplicationLastNameOption;
	private RecruitingTableOption tableMailProjectTitleOption;
	private RecruitingTableOption tableMailProjectAcronymOption;
	private RecruitingTableOption tableMailProjectKeywordsOption;
	private RecruitingTableOption tableMailProjectDisciplinesOption;
	private RecruitingTableOption tableMailProjectStartDateOption;
	private RecruitingTableOption tableMailProjectDurationOption;
	private RecruitingTableOption tableMailProjectFinancialImpact1Option;
	private RecruitingTableOption tableMailProjectFinancialImpact2Option;
	private RecruitingTableOption tableMailProjectFinancialImpact3Option;
	private RecruitingTableOption tableMailProjectFinancialImpact4Option;
	private RecruitingTableOption tableMailProjectFinancialImpact5Option;
	private RecruitingTableOption tableMailProjectDescriptionOption;
	private RecruitingTableOption tableMailApplicationStatusOption;
	private RecruitingTableOption tableMailEmailLogStatusOption;

	private RecruitingTableOption tableFeedbacksForMembersDueDateOption;

	private final List<DocumentOption> docOptions = new ArrayList<>();
	
	private NotificationPermission[] notificationsPermissionsForHead;
	private NotificationPermission[] notificationsPermissionsForSecretary;
	private NotificationPermission[] notificationsPermissionsForCommittee;
	private NotificationPermission[] notificationsPermissionsForExOfficio;
	
	@Autowired
	public RecruitingModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
		initContexts();
		initOptions();
	}
	
	private void initContexts() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator(Position.class.getSimpleName(),
				new PositionContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Apply",
				new PositionContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Positions",
				new PositionsContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("publicfeedback",
				new PublicFeedbackContextEntryControllerCreator());
	}
	
	private void initOptions() {
		positionRolesAllowedToRate = PositionRole.valueOfArray(rolesAllowedToRate);
		positionRolesAllowedToSeeRating = PositionRole.valueOfArray(rolesAllowedToSeeRating);
		positionRolesAllowedToSeeRatingDuringRating = PositionRole.valueOfArray(rolesAllowedToSeeRatingDuringRating);
		positionRolesAllowedToTakeNotes = PositionRole.valueOfArray(rolesAllowedToNotes);
		positionRolesAllowedToExportApplicationListExcel = PositionRole.valueOfArray(rolesAllowedToExportApplicationListExcel);
		positionRolesAllowedToExportReviewsStatisticsExcel = PositionRole.valueOfArray(rolesAllowedToExportReviewsStatisticsExcel);
		positionRolesAllowedToExportGeneratedList = PositionRole.valueOfArray(rolesAllowedToExportGeneratedList);
		positionRolesAllowedToExportApplicationListPdf = PositionRole.valueOfArray(rolesAllowedToExportApplicationListPdf);
		positionRolesAllowedToExportCommitteeListExcel = PositionRole.valueOfArray(rolesAllowedToExportCommitteeListExcel);
		positionRolesAllowedToExportRatingsPdf = PositionRole.valueOfArray(rolesAllowedToExportRatingsPdf);
		
		positionRolesAllowedToViewMailCenter = PositionRole.valueOfArray(rolesAllowedToViewMailCenter);
		positionRolesAllowedToExportMailCenterLog = PositionRole.valueOfArray(rolesAllowedToExportMailCenterLog);
		positionRolesAllowedToViewMailCenterEmail = PositionRole.valueOfArray(rolesAllowedToViewMailCenterEmail);
		positionRolesAllowedToResendMailCenterEmail = PositionRole.valueOfArray(rolesAllowedToResendMailCenterEmail);
		positionRolesAllowedToSendMailToApplicant = PositionRole.valueOfArray(rolesAllowedToSendMailToApplicant);
		positionRolesAllowedToSendBulkApplicationEmails = PositionRole.valueOfArray(rolesAllowedToSendBulkApplicationEmail);
		
		positionRolesAllowedToConfigureDecisionTool = PositionRole.valueOfArray(rolesAllowedToConfigureDecisionTool);
		positionRolesAllowedToEditDecisionRubrics = PositionRole.valueOfArray(rolesAllowedToEditDecisionRubrics);
		positionRolesAllowedToSendEmailAllCommittee = PositionRole.valueOfArray(rolesAllowedToSendEmailAllCommittee);
		positionRolesAllowedToCreateApplications = PositionRole.valueOfArray(rolesAllowedToCreateApplications);
		positionRolesAllowedToEditApplicationPersonalData = PositionRole.valueOfArray(rolesAllowedToEditApplicationPersonalData);
		positionRolesAllowedToEditApplicationAcademicalBackground = PositionRole.valueOfArray(rolesAllowedToEditApplicationAcademicalBackground);
		positionRolesAllowedToEditApplicationProject = PositionRole.valueOfArray(rolesAllowedToEditApplicationProject);
		positionRolesAllowedToEditApplicationDocuments = PositionRole.valueOfArray(rolesAllowedToEditApplicationDocuments);
		positionRolesAllowedToEditApplicationStatus = PositionRole.valueOfArray(rolesAllowedToEditApplicationStatus);
		positionRolesAllowedToEditApplicationCategories = PositionRole.valueOfArray(rolesAllowedToEditApplicationCategories);
		positionRolesAllowedToDeletePublicFeedback = PositionRole.valueOfArray(rolesAllowedToDeletePublicFeedback);
		positionRolesAllowedToEditMembersFeedback = PositionRole.valueOfArray(rolesAllowedToEditMembersFeedback);
		positionRolesAllowedToSeeMembersFeedback = PositionRole.valueOfArray(rolesAllowedToSeeMembersFeedback);
		positionRolesAllowedToDeleteMembersFeedback = PositionRole.valueOfArray(rolesAllowedToDeleteMembersFeedback);
		
		positionRolesAllowedToEditApplicationReferences = PositionRole.valueOfArray(rolesAllowedToEditApplicationReferences);
		positionRolesAllowedToEditApplicationCommitteeComment = PositionRole.valueOfArray(rolesAllowedToEditApplicationCommitteeComment);
		positionRolesAllowedToDeleteApplication = PositionRole.valueOfArray(rolesAllowedToDeleteApplication);
		positionRolesAllowedToSeeParallelApplications = PositionRole.valueOfArray(rolesAllowedToSeeParallelApplications);
		positionRolesAllowedToEditCommitteDecision = PositionRole.valueOfArray(rolesAllowedToEditCommitteDecision);
		positionRolesAllowedToSeePublishedPositions = PositionRole.valueOfArray(rolesAllowedToSeePublishedPositions);
		positionRolesAllowedToEditApplicationsMemo = PositionRole.valueOfArray(rolesAllowedToEditApplicationMemo);
		positionRolesAllowedToSeeExpertBlackList = PositionRole.valueOfArray(rolesAllowedToSeeExpertBlackList);
		positionRolesAllowedToEditAdministrativeTags = PositionRole.valueOfArray(rolesAllowedToEditAdministrativeTags);
		positionRolesAllowedToSeeAdministrativeTags = PositionRole.valueOfArray(rolesAllowedToSeeAdministrativeTags);
		
		positionRolesAllowedToEditPositionStatus = PositionRole.valueOfArray(rolesAllowedToEditPositionStatus);
		positionRolesAllowedToEditPositionProfile = PositionRole.valueOfArray(rolesAllowedToEditPositionProfile);
		positionRolesAllowedToEditPositionApplicationsSettings = PositionRole.valueOfArray(rolesAllowedToEditPositionApplicationsSettings);
		positionRolesAllowedToEditPositionReferencesSettings = PositionRole.valueOfArray(rolesAllowedToEditPositionReferencesSettings);
		positionRolesAllowedToEditPositionEvaluationSettings = PositionRole.valueOfArray(rolesAllowedToEditPositionEvaluationSettings);
		positionRolesAllowedToEditPositionTagsSettings = PositionRole.valueOfArray(rolesAllowedToEditPositionTagsSettings);
		positionRolesAllowedToEditPositionMailTemplates = PositionRole.valueOfArray(rolesAllowedToEditPositionMailTemplates);
		positionRolesAllowedToEditPositionFeedbacksSettings = PositionRole.valueOfArray(rolesAllowedToEditPositionFeedbacksSettings);
		
		positionRolesAllowedToEditPositionCommittee = PositionRole.valueOfArray(rolesAllowedToEditPositionCommittee);
		positionRolesAllowedToAddPositionCommittee = PositionRole.valueOfArray(rolesAllowedToAddPositionCommittee);
		positionRolesAllowedToRemovePositionCommittee = PositionRole.valueOfArray(rolesAllowedToRemovePositionCommittee);
		
		positionRolesAllowedToSearchApplications = PositionRole.valueOfArray(rolesAllowedToSearchApplications);
		
		positionRolesAllowedToEditAssignments = PositionRole.valueOfArray(rolesAllowedToEditAssignments);
		
		assignmentMethods = AssignmentMethods.valueOfArray(applicationAssignmentMethod);
		
		positionRolesAllowedToShareApplicationListFilters = FilterPermissions.valueOfArray(rolesAllowedToShareApplicationListFilters);
		positionRolesAllowedToManageApplicationListFilters = FilterPermissions.valueOfArray(rolesAllowedToManageApplicationListFilters);
		
		positionRolesAllowedToApplicationBasicFilters = FilterPermissions.valueOfArray(rolesAllowedToApplicationBasicFilters);
		positionRolesAllowedToApplicationAdvancedFilters = FilterPermissions.valueOfArray(rolesAllowedToApplicationAdvancedFilters);
		
		String attributesStr = getStringPropertyValue(NEW_POSITION_EXCLUDE_ATTRIBUTES, newPositionExcludedFields);
		if(StringHelper.containsNonWhitespace(attributesStr)) {
			String[] fieldsArr = attributesStr.split(",");
			newPositionExcludedAttributesList = List.of(fieldsArr);
		} else {
			newPositionExcludedAttributesList = List.of();
		}

		//table applications
		tableApplicationsPersonTitleOption = getTableOption(tableApplicationsPersonTitle, RecruitingTableOption.enabled);
		tableApplicationsPersonFirstNameOption = getTableOption(tableApplicationsPersonFirstName, RecruitingTableOption.enabled);
		tableApplicationsPersonLastNameOption = getTableOption(tableApplicationsPersonLastName, RecruitingTableOption.enabled);
		tableApplicationsOrganizationOption = getTableOption(tableApplicationsOrganization, RecruitingTableOption.enabled);
		tableApplicationsOrganizationUnitOption = getTableOption(tableApplicationsOrganizationUnit, RecruitingTableOption.enabled);
		tableApplicationsOrganizationCurrentPositionOption = getTableOption(tableApplicationsOrganizationCurrentPosition, RecruitingTableOption.enabled);
		tableApplicationsSubmittedDateOption = getTableOption(tableApplicationsSubmittedDateEnable, RecruitingTableOption.disabled);
		tableApplicationsSubmittedByStaffOption = getTableOption(tableApplicationsSubmittedByStaffEnable, RecruitingTableOption.optional);
		tableApplicationsNationalityOption = getTableOption(tableApplicationsNationalityEnable, RecruitingTableOption.optional);
		tableApplicationsAdditionalNationalitiesOption = getTableOption(tableApplicationsAdditionalNationalitiesEnable, RecruitingTableOption.optional);
		tableApplicationsWorkedInAcademiaSinceOption = getTableOption(tableApplicationsWorkedInAcademiaSinceEnable, RecruitingTableOption.optional);
		tableApplicationsWorkedOutAcademiaSinceOption = getTableOption(tableApplicationsWorkedOutAcademiaSinceEnable, RecruitingTableOption.optional);
		tableApplicationsWorkedOutAcademiaCareSinceOption = getTableOption(tableApplicationsWorkedOutAcademiaCareSinceEnable, RecruitingTableOption.optional);

		tableApplicationsDissertationTitleOption = getTableOption(tableApplicationsDissertationTitleEnable, RecruitingTableOption.optional);
		tableApplicationsDissertationDateOption = getTableOption(tableApplicationsDissertationDateEnable, RecruitingTableOption.optional);
		tableApplicationsDissertationInstitutionOption = getTableOption(tableApplicationsDissertationInstitutionEnable, RecruitingTableOption.optional);
		tableApplicationsDissertationKeyword1Option = getTableOption(tableApplicationsDissertationKeyword1Enable, RecruitingTableOption.optional);
		tableApplicationsDissertationKeyword2Option = getTableOption(tableApplicationsDissertationKeyword2Enable, RecruitingTableOption.optional);
		tableApplicationsDissertationKeyword3Option = getTableOption(tableApplicationsDissertationKeyword3Enable, RecruitingTableOption.optional);
		tableApplicationsHighestDegreeOption = getTableOption(tableApplicationsHighestDegreeEnable, RecruitingTableOption.optional);
		tableApplicationsHighestDegreeInstitutionOption = getTableOption(tableApplicationsHighestDegreeInstitutionEnable, RecruitingTableOption.optional);
		tableApplicationsHighestDegreeYearOption = getTableOption(tableApplicationsHighestDegreeYearEnable, RecruitingTableOption.disabled);
		tableApplicationsEMailOption = getTableOption(tableApplicationsEMailEnable, RecruitingTableOption.enabled);
		tableApplicationsPhoneOption = getTableOption(tableApplicationsPhoneEnable, RecruitingTableOption.enabled);
		tableApplicationsMobilePhoneOption = getTableOption(tableApplicationsMobilePhoneEnable, RecruitingTableOption.optional);
		tableApplicationsGenderOption = getTableOption(tableApplicationsGenderEnable, RecruitingTableOption.enabled);
		tableApplicationsMaritalStatusOption = getTableOption(tableApplicationsMaritalStatusEnable, RecruitingTableOption.disabled);
		tableApplicationsYearOfBirthOption = getTableOption(tableApplicationsYearOfBirthEnable, RecruitingTableOption.enabled);
		tableApplicationsBirthdayOption = getTableOption(tableApplicationsBirthdayEnable, RecruitingTableOption.enabled);
		tableApplicationsDisabilityOption = getTableOption(tableApplicationsDisabilityEnable, RecruitingTableOption.disabled);
		
		tableApplicationsAddressLinesOption = getTableOption(tableApplicationsAddressLinesEnable, RecruitingTableOption.enabled);
		tableApplicationsBusinessAddressLinesOption = getTableOption(tableApplicationsBusinessAddressLinesEnable, RecruitingTableOption.enabled);
		tableApplicationsZipcodeOption = getTableOption(tableApplicationsZipcodeEnable, RecruitingTableOption.enabled);
		tableApplicationsBusinessZipcodeOption = getTableOption(tableApplicationsBusinessZipcodeEnable, RecruitingTableOption.enabled);
		
		tableApplicationsExpertsOption = getTableOption(tableApplicationsExpertsEnable, RecruitingTableOption.enabled);
		tableApplicationsRefereesOption = getTableOption(tableApplicationsRefereesEnable, RecruitingTableOption.enabled);
		tableApplicationsComparativeExpertsOption = getTableOption(tableApplicationsComparativeExpertsEnable, RecruitingTableOption.enabled);
		tableApplicationsProvidedExpertsRecommendationsOption = getTableOption(tableApplicationsProvidedExpertsRecommendationsEnable, RecruitingTableOption.enabled);
		
		tableApplicationsMemoOption = getTableOption(tableApplicationsMemoEnable, RecruitingTableOption.disabled);
		tableApplicationsCommitteeCommentOption = getTableOption(tableApplicationsCommitteeCommentEnable, RecruitingTableOption.disabled);
		tableApplicationsStatusOption = getTableOption(tableApplicationsStatusEnable, RecruitingTableOption.optional);
		tableApplicationsStatusDateOption = getTableOption(tableApplicationsStatusDateEnable, RecruitingTableOption.optional);
		tableApplicationsParallelApplicationsOption = getTableOption(tableApplicationsParallelApplicationsEnable, RecruitingTableOption.optional);
		tableApplicationsCommitteeRating = getTableContextualOption(tableApplicationsCommitteeRatingEnable, RecruitingTableContextualOption.context);
		tableApplicationsDecision = getTableContextualOption(tableApplicationsDecisionEnable, RecruitingTableContextualOption.context);
		
		tableApplicationsProjectOption = getTableOption(tableApplicationsProjectEnable, RecruitingTableOption.enabled);
		tableApplicationsProjectTitleOption = getTableOption(tableApplicationsProjectTitleEnable, RecruitingTableOption.disabled);
		tableApplicationsProjectAcronymOption = getTableOption(tableApplicationsProjectAcronymEnable, RecruitingTableOption.disabled);
		tableApplicationsProjectKeywordsOption = getTableOption(tableApplicationsProjectKeywordsEnable, RecruitingTableOption.disabled);
		tableApplicationsProjectDisciplinesOption = getTableOption(tableApplicationsProjectDisciplinesEnable, RecruitingTableOption.disabled);
		tableApplicationsProjectStartDateOption = getTableOption(tableApplicationsProjectStartDateEnable, RecruitingTableOption.disabled);
		tableApplicationsProjectDurationOption = getTableOption(tableApplicationsProjectDurationEnable, RecruitingTableOption.disabled);
		tableApplicationsProjectFinancialImpact1Option = getTableOption(tableApplicationsProjectFinancialImpact1Enable, RecruitingTableOption.disabled);
		tableApplicationsProjectFinancialImpact2Option = getTableOption(tableApplicationsProjectFinancialImpact2Enable, RecruitingTableOption.disabled);
		tableApplicationsProjectFinancialImpact3Option = getTableOption(tableApplicationsProjectFinancialImpact3Enable, RecruitingTableOption.disabled);
		tableApplicationsProjectFinancialImpact4Option = getTableOption(tableApplicationsProjectFinancialImpact4Enable, RecruitingTableOption.disabled);
		tableApplicationsProjectFinancialImpact5Option = getTableOption(tableApplicationsProjectFinancialImpact5Enable, RecruitingTableOption.disabled);
		tableApplicationsProjectDescriptionOption = getTableOption(tableApplicationsProjectDescriptionEnable, RecruitingTableOption.disabled);
		
		tableApplicationsReviewsOption = getTableOption(tableApplicationsReviewsEnable, RecruitingTableOption.enabled);
		tableApplicationsMyRatingsAlwaysVisibleForRoles = PositionRole.valueOfArray(tableApplicationsMyRatingsAlwaysVisibleFor);
		tableApplicationsMyReviewsAlwaysVisibleForRoles = PositionRole.valueOfArray(tableApplicationsMyReviewsAlwaysVisibleFor);
		
		tableCommitteeUserPropertiesOption = getTableOption(tableCommitteeUserProperties, RecruitingTableOption.optional);
		
		tableFeedbacksUserPropertiesOption = getTableOption(tableFeedbacksUserProperties, RecruitingTableOption.optional);
		
		tableReferencesApplicationFullNameOption = getTableOption(tableReferencesApplicationFullNameEnable, RecruitingTableOption.enabled);
		tableReferencesApplicationIdOption = getTableOption(tableReferencesApplicationIdEnable, RecruitingTableOption.optional);
		tableReferencesProjectTitleOption = getTableOption(tableReferencesProjectTitleEnable, RecruitingTableOption.disabled);
		tableReferencesProjectAcronymOption = getTableOption(tableReferencesProjectAcronymEnable, RecruitingTableOption.disabled);
		tableReferencesProjectKeywordsOption = getTableOption(tableReferencesProjectKeywordsEnable, RecruitingTableOption.disabled);
		tableReferencesProjectDisciplinesOption = getTableOption(tableReferencesProjectDisciplinesEnable, RecruitingTableOption.disabled);
		tableReferencesProjectStartDateOption = getTableOption(tableReferencesProjectStartDateEnable, RecruitingTableOption.disabled);
		tableReferencesProjectDurationOption = getTableOption(tableReferencesProjectDurationEnable, RecruitingTableOption.disabled);
		tableReferencesProjectFinancialImpact1Option = getTableOption(tableReferencesProjectFinancialImpact1Enable, RecruitingTableOption.disabled);
		tableReferencesProjectFinancialImpact2Option = getTableOption(tableReferencesProjectFinancialImpact2Enable, RecruitingTableOption.disabled);
		tableReferencesProjectFinancialImpact3Option = getTableOption(tableReferencesProjectFinancialImpact3Enable, RecruitingTableOption.disabled);
		tableReferencesProjectFinancialImpact4Option = getTableOption(tableReferencesProjectFinancialImpact4Enable, RecruitingTableOption.disabled);
		tableReferencesProjectFinancialImpact5Option = getTableOption(tableReferencesProjectFinancialImpact5Enable, RecruitingTableOption.disabled);
		tableReferencesProjectDescriptionOption = getTableOption(tableReferencesProjectDescriptionEnable, RecruitingTableOption.disabled);
		
		tableReferenceToApplicationTitleOption = getTableOption(tableReferenceToApplicationTitleEnable, RecruitingTableOption.optional);
		tableReferenceToApplicationFirstNameOption = getTableOption(tableReferenceToApplicationFirstNameEnable, RecruitingTableOption.enabled);
		tableReferenceToApplicationLastNameOption = getTableOption(tableReferenceToApplicationLastNameEnable, RecruitingTableOption.enabled);
		tableReferenceToProjectTitleOption = getTableOption(tableReferenceToProjectTitleEnable, RecruitingTableOption.disabled);
		
		tableApplicantDashboardDueDateOption = getTableOption(tableApplicantDashboardDueDateEnable, RecruitingTableOption.enabled);
		
		tableDecisionApplicationTitleOption = getTableOption(tableDecisionApplicationTitleEnable, RecruitingTableOption.optional);
		tableDecisionApplicationFirstNameOption = getTableOption(tableDecisionApplicationFirstNameEnable, RecruitingTableOption.enabled);
		tableDecisionApplicationLastNameOption = getTableOption(tableDecisionApplicationLastNameEnable, RecruitingTableOption.enabled);
		tableDecisionProjectTitleOption = getTableOption(tableDecisionProjectTitleEnable, RecruitingTableOption.disabled);
		tableDecisionProjectAcronymOption = getTableOption(tableDecisionProjectAcronymEnable, RecruitingTableOption.disabled);
		tableDecisionProjectKeywordsOption = getTableOption(tableDecisionProjectKeywordsEnable, RecruitingTableOption.disabled);
		tableDecisionProjectDisciplinesOption = getTableOption(tableDecisionProjectDisciplinesEnable, RecruitingTableOption.disabled);
		tableDecisionProjectStartDateOption = getTableOption(tableDecisionProjectStartDateEnable, RecruitingTableOption.disabled);
		tableDecisionProjectDurationOption = getTableOption(tableDecisionProjectDurationEnable, RecruitingTableOption.disabled);
		tableDecisionProjectFinancialImpact1Option = getTableOption(tableDecisionProjectFinancialImpact1Enable, RecruitingTableOption.disabled);
		tableDecisionProjectFinancialImpact2Option = getTableOption(tableDecisionProjectFinancialImpact2Enable, RecruitingTableOption.disabled);
		tableDecisionProjectFinancialImpact3Option = getTableOption(tableDecisionProjectFinancialImpact3Enable, RecruitingTableOption.disabled);
		tableDecisionProjectFinancialImpact4Option = getTableOption(tableDecisionProjectFinancialImpact4Enable, RecruitingTableOption.disabled);
		tableDecisionProjectFinancialImpact5Option = getTableOption(tableDecisionProjectFinancialImpact5Enable, RecruitingTableOption.disabled);
		
		tableMailApplicationTitleOption = getTableOption(tableMailApplicationTitleEnable, RecruitingTableOption.optional);
		tableMailApplicationFirstNameOption = getTableOption(tableMailApplicationFirstNameEnable, RecruitingTableOption.enabled);
		tableMailApplicationLastNameOption = getTableOption(tableMailApplicationLastNameEnable, RecruitingTableOption.enabled);
		tableMailProjectTitleOption = getTableOption(tableMailProjectTitleEnable, RecruitingTableOption.disabled);
		tableMailProjectAcronymOption = getTableOption(tableMailProjectAcronymEnable, RecruitingTableOption.disabled);
		tableMailProjectKeywordsOption = getTableOption(tableMailProjectKeywordsEnable, RecruitingTableOption.disabled);
		tableMailProjectDisciplinesOption = getTableOption(tableMailProjectDisciplinesEnable, RecruitingTableOption.disabled);
		tableMailProjectStartDateOption = getTableOption(tableMailProjectStartDateEnable, RecruitingTableOption.disabled);
		tableMailProjectDurationOption = getTableOption(tableMailProjectDurationEnable, RecruitingTableOption.disabled);
		tableMailProjectFinancialImpact1Option = getTableOption(tableMailProjectFinancialImpact1Enable, RecruitingTableOption.disabled);
		tableMailProjectFinancialImpact2Option = getTableOption(tableMailProjectFinancialImpact2Enable, RecruitingTableOption.disabled);
		tableMailProjectFinancialImpact3Option = getTableOption(tableMailProjectFinancialImpact3Enable, RecruitingTableOption.disabled);
		tableMailProjectFinancialImpact4Option = getTableOption(tableMailProjectFinancialImpact4Enable, RecruitingTableOption.disabled);
		tableMailProjectFinancialImpact5Option = getTableOption(tableMailProjectFinancialImpact5Enable, RecruitingTableOption.disabled);
		tableMailProjectDescriptionOption = getTableOption(tableMailProjectDescriptionEnable, RecruitingTableOption.disabled);
		tableMailApplicationStatusOption = getTableOption(tableMailApplicationStatusEnable, RecruitingTableOption.enabled);
		tableMailEmailLogStatusOption = getTableOption(tableMailEmailLogStatusEnable, RecruitingTableOption.enabled);
		
		tableFeedbacksForMembersDueDateOption = getTableOption(tableFeedbacksForMembersDueDateEnable, RecruitingTableOption.enabled);

		personTitles = parsePersonTitles(applicationPersonTitles);
		personGenders = parsePersonGenders(applicationPersonGenders);
		if(StringHelper.containsNonWhitespace(applicationPersonGenderDefault)) {
			personDefaultGender = PersonGender.valueOf(applicationPersonGenderDefault);
		} else {
			personDefaultGender = null;
		}
		maritalStatusList = parsePersonMaritalStatus(applicationPersonMaritalStatusList);

		userTitles = parsePersonTitles(userPersonTitles);
		referenceTitles = parsePersonTitles(referencePersonTitles);
		
		highestDegreeTypes = parseHighestDegreeTypes(applicationAcademicalBackgroundHighestDegreeTypes);
		
		dissertationDateFormats = parseAcademicalDateFormat(applicationAcademicalBackgroundDissertationDateFormat);
		
		applicationAddressPrivateOption = AddressOption.valueOf(applicationAddressPrivate);
		applicationAddressBusinessOption = AddressOption.valueOf(applicationAddressBusiness);

		// application
		applicationReferencesVisibleStatus = ReferenceStatus.valueOfArray(applicationReferencesVisibleStatusOption);
		
		applicationAvailableStatus = ApplicationStatus.valueOfArray(applicationStatus);
		applicationsDefaultBasicFilterApplicationStatus = ApplicationStatus.valueOfArray(tableApplicationsDefaultBasicFilterApplicationStatus);

		applicationProjectFinancialImpact1FieldType = ApplicationFieldType
				.valueOf(applicationProjectFinancialImpact1, applicationProjectFinancialImpact1Type);
		applicationProjectFinancialImpact2FieldType = ApplicationFieldType
				.valueOf(applicationProjectFinancialImpact2, applicationProjectFinancialImpact2Type);
		applicationProjectFinancialImpact3FieldType = ApplicationFieldType
				.valueOf(applicationProjectFinancialImpact3, applicationProjectFinancialImpact3Type);
		applicationProjectFinancialImpact4FieldType = ApplicationFieldType
				.valueOf(applicationProjectFinancialImpact4, applicationProjectFinancialImpact4Type);
		applicationProjectFinancialImpact5FieldType = ApplicationFieldType
				.valueOf(applicationProjectFinancialImpact5, applicationProjectFinancialImpact5Type);

		preferedCountries = parseCountries(countryPrefered);
		
		feedbackOptions = parseFeedbackOptions(feedbackDefaultOptions);

		notificationsPermissionsForHead = parseNotificationPermissisions(allowedNotificationsForHead);
		notificationsPermissionsForSecretary = parseNotificationPermissisions(allowedNotificationsForSecretary);
		notificationsPermissionsForCommittee = parseNotificationPermissisions(allowedNotificationsForCommittee);
		notificationsPermissionsForExOfficio = parseNotificationPermissisions(allowedNotificationsForExOfficio);
		
		parseDocOption(docCoveringLetter, DocumentEnum.coveringLetter);
		parseDocOption(docCurriculumVitae, DocumentEnum.curriculumVitae);
		parseDocOption(docPublications, DocumentEnum.publications);
		parseDocOption(docStatements, DocumentEnum.statements);
		parseDocOption(docProposals, DocumentEnum.proposals);
		parseDocOption(docLeadership, DocumentEnum.leadership);
		parseDocOption(docReferees, DocumentEnum.referees);
		parseDocOption(docProjects, DocumentEnum.projects);
		parseDocOption(docReferences, DocumentEnum.references);
		parseDocOption(docTeachingAssessment, DocumentEnum.teachingAssessment);
		parseDocOption(docCertificateOfStudy, DocumentEnum.certificateOfStudy);
		parseDocOption(docDegreeCertificates, DocumentEnum.degreeCertificates);
		parseDocOption(docDissertation, DocumentEnum.dissertation);
		parseDocOption(docHabilitation, DocumentEnum.habilitation);
		parseDocOption(docClinicalDisciplines, DocumentEnum.clinicalDisciplines);
		parseDocOption(docSurgicalDisciplines, DocumentEnum.surgicalDisciplines);
		parseDocOption(docReprints, DocumentEnum.reprints);
		parseDocOption(docExternalFunding, DocumentEnum.externalFunding);
		parseDocOption(docPublication1, DocumentEnum.publication1);
		parseDocOption(docPublication2, DocumentEnum.publication2);
		parseDocOption(docPublication3, DocumentEnum.publication3);
		parseDocOption(docPublication4, DocumentEnum.publication4);
		parseDocOption(docPublication5, DocumentEnum.publication5);
		parseDocOption(docOther, DocumentEnum.other);
		parseDocOption(docCombined, DocumentEnum.combined);

		positionStatusAvailable = PositionStatus.valueOfArray(positionStatus);
		
		String[] positionLanguageArr = positionLanguages.split(",");
		positionLocales = new Locale[positionLanguageArr.length];
		for(int i=positionLanguageArr.length; i-->0; ) {
			positionLocales[i] = new Locale(positionLanguageArr[i]);
		}
		
		positionDefaultLocale = new Locale(positionDefaultLanguage);
	}
	
	private String[] parseFeedbackOptions(String options) {
		List<String> optionList = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(options)) {
			String[] optionArr = options.split(",");
			for(String option:optionArr) {
				if(StringHelper.containsNonWhitespace(option)) {
					optionList.add(option);
				}
			}
		}
		return optionList.toArray(new String[optionList.size()]);
	}
	
	private HighestDegreeType[] parseHighestDegreeTypes(String types) {
		String[] typeArr = types.split(",");
		HighestDegreeType[] typeEnumArr = new HighestDegreeType[typeArr.length];
		for(int i=typeArr.length; i-->0; ) {
			typeEnumArr[i] = HighestDegreeType.valueOf(typeArr[i]);
		}
		return typeEnumArr;
	}
	
	private AcademicalDateFormat[] parseAcademicalDateFormat(String formats) {
		String[] formatArr = formats.split(",");
		AcademicalDateFormat[] typeEnumArr = new AcademicalDateFormat[formatArr.length];
		for(int i=formatArr.length; i-->0; ) {
			typeEnumArr[i] = AcademicalDateFormat.format(formatArr[i]);
		}
		return typeEnumArr;
	}
	
	private Country[] parseCountries(String countries) {
		Country[] countryEnumArr;
		if(StringHelper.containsNonWhitespace(countries)) {
			String[] countryArr = countries.split(",");
			countryEnumArr = new Country[countryArr.length];
			for(int i=countryArr.length; i-->0; ) {
				countryEnumArr[i] = Country.valueOf(countryArr[i]);
			}
		} else {
			countryEnumArr = new Country[0];
		}
		return countryEnumArr;
	}
	
	private PersonTitle[] parsePersonTitles(String titles) {
		PersonTitle[] titleEnumArr;
		if(StringHelper.containsNonWhitespace(titles)) {
			String[] titleArr = titles.split(",");
			titleEnumArr = new PersonTitle[titleArr.length];
			for(int i=titleArr.length; i-->0; ) {
				titleEnumArr[i] = PersonTitle.valueOf(titleArr[i]);
			}
		} else {
			titleEnumArr = new PersonTitle[0]; 
		}
		return titleEnumArr;
	}
	
	private PersonGender[] parsePersonGenders(String genders) {
		String[] genderArr = genders.split(",");
		PersonGender[] genderEnumArr = new PersonGender[genderArr.length];
		for(int i=genderArr.length; i-->0; ) {
			genderEnumArr[i] = PersonGender.valueOf(genderArr[i]);
		}
		return genderEnumArr;
	}
	
	private PersonMaritalStatus[] parsePersonMaritalStatus(String status) {
		String[] statusArr = status.split(",");
		PersonMaritalStatus[] genderEnumArr = new PersonMaritalStatus[statusArr.length];
		for(int i=statusArr.length; i-->0; ) {
			genderEnumArr[i] = PersonMaritalStatus.valueOf(statusArr[i]);
		}
		return genderEnumArr;
	}
	
	private void parseDocOption(String value, DocumentEnum docEnum) {
		if(StringHelper.containsNonWhitespace(value)) {
			int maxSize = Integer.parseInt(value);
			if(maxSize > 0) {
				docOptions.add(new DocumentOption(docEnum, maxSize));
			}
		}	
	}
	
	private NotificationPermission[] parseNotificationPermissisions(String value) {
		String[] permissions = value.split("[,]");
		List<NotificationPermission> permissionList = new ArrayList<>();
		for(String permission:permissions) {
			String[] details = permission.split("[.]");
			if(details.length == 2) {
				String target = details[0];
				String action = details[1];
				permissionList.add(new NotificationPermission(ActionTarget.valueOf(target), Action.valueOf(action)));
				
			}
		}
		return permissionList.toArray(new NotificationPermission[permissionList.size()]);
	}
	
	private RecruitingTableOption getTableOption(String enabled, RecruitingTableOption defOption) {
		if(StringHelper.containsNonWhitespace(enabled)) {
			return RecruitingTableOption.valueOf(enabled);
		}
		return defOption;
	}
	
	private RecruitingTableContextualOption getTableContextualOption(String enabled, RecruitingTableContextualOption defOption) {
		if(StringHelper.containsNonWhitespace(enabled)) {
			return RecruitingTableContextualOption.valueOf(enabled);
		}
		return defOption;
	}
	
	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(SELECTUS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String epositionsLoginEnabledObj = getStringPropertyValue(POSITIONS_LOGIN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(epositionsLoginEnabledObj)) {
			positionsLoginEnabled = "true".equals(epositionsLoginEnabledObj);
		}
	}
	
	@Override
	public boolean isEnabled() {
		return enabled && moduleAvailable;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(SELECTUS_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isModuleAvailable() {
		return moduleAvailable;
	}
	
	public boolean isAttachmenOnFileSystem() {
		return attachmentOnFileSystem;
	}
	
	public boolean isPositionsLoginEnabled() {
		return positionsLoginEnabled;
	}
	
	public void setPositionsLoginEnabled(boolean enabled) {
		this.positionsLoginEnabled = enabled;
		setStringProperty(POSITIONS_LOGIN_ENABLED, Boolean.toString(enabled), true);
	}
	
	/**
	 * Helper method to get the documents in combined file from the
	 * configuration of the position or the default settings. Document
	 * which can be .docx and .xlsx are excluded.
	 * 
	 * @param position The position (mandatory)
	 * @return A set of documents
	 */
	public Set<DocumentEnum> getDocumentsInCombinedFile(Position position) {
		Set<DocumentEnum> documentsInCombinedFile = position.getDocumentsInCombinedFile();
		if(documentsInCombinedFile.isEmpty()) {
			documentsInCombinedFile.addAll(getDocumentsInCombinedFile());
		}
		
		Set<DocumentEnum> xlsx = position.getXlsxDocuments();
		Set<DocumentEnum> docx = position.getDocxDocuments();
		Set<DocumentEnum> jpg = position.getJpgDocuments();
		if(!xlsx.isEmpty() || !docx.isEmpty() || !jpg.isEmpty()) {
			documentsInCombinedFile.removeAll(xlsx);
			documentsInCombinedFile.removeAll(docx);
			documentsInCombinedFile.removeAll(jpg);
		}
		return documentsInCombinedFile;
	}
	
	public Collection<DocumentEnum> getDocumentsInCombinedFile() {
		if(!StringHelper.containsNonWhitespace(docInCombined) || "all".equals(docInCombined)) {
			return Arrays.asList(DocumentEnum.values());
		}
		String[] docs = docInCombined.split("[,]");
		List<DocumentEnum> documents = new ArrayList<>(docs.length);
		for(String doc:docs) {
			if(StringHelper.containsNonWhitespace(doc)) {
				try {
					documents.add(DocumentEnum.valueOf(doc));
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		
		return documents;
	}
	
	public boolean isAllDocumentsInCombinedFile() {
		return "all".equals(docInCombined);
	}
	
	public String getDocumentCombinedCoverVersion() {
		return StringHelper.containsNonWhitespace(docCombinedCover) ? docCombinedCover : "v1";
	}
	
	public String getOfficeMail() {
		return officeMail;
	}
	
	public MailSettingEnum getMailSetting(Position position, OrganisationUnit organisationSettings) {
		if(position == null) {
			return MailSettingEnum.system;
		}
		
		if(isMailProPositionEnabled() && position.getMailSetting() != null) {
			MailSettingEnum settings = position.getMailSetting();
			if(settings == MailSettingEnum.position || settings == MailSettingEnum.system) {
				return settings;
			}
			if(settings == MailSettingEnum.organisationUnit && organisationSettings != null && organisationSettings.isSystemConfiguration()) {
				return MailSettingEnum.system;
			}
			return MailSettingEnum.organisationUnit;
		}
		
		if(organisationSettings != null && !organisationSettings.isSystemConfiguration()) {
			return MailSettingEnum.organisationUnit;
		}
		return MailSettingEnum.system;
	}
	
	public MailSettingEnum getMailSetting(OrganisationUnit orgUnit) {
		if(orgUnit != null && !orgUnit.isSystemConfiguration()) {
			return MailSettingEnum.organisationUnit;
		}
		return MailSettingEnum.system;
	}

	public String getStaffMail() {
		return staffMail;
	}
	
	public String getStaffMail(Position position, OrganisationUnit organisationSettings) {
		MailSettingEnum setting = getMailSetting(position, organisationSettings);
		
		String mail = null;
		if(setting == MailSettingEnum.position) {
			mail = position.getSenderMail();
		}
		
		if(setting == MailSettingEnum.organisationUnit
				|| (setting == MailSettingEnum.position && position.getOrganisation() != null && !StringHelper.containsNonWhitespace(mail))) {
			mail = organisationSettings.getStaffMail();
		}	
		if(!StringHelper.containsNonWhitespace(mail)) {
			mail = getStaffMail();
		}
		return mail;
	}
	
	public String getStaffMail(OrganisationUnit orgUnit) {
		MailSettingEnum setting = getMailSetting(orgUnit);
		
		String mail = null;
		if(setting == MailSettingEnum.organisationUnit) {
			mail = orgUnit.getStaffMail();
		}	
		if(!StringHelper.containsNonWhitespace(mail)) {
			mail = getStaffMail();
		}
		return mail;
	}
	
	public String getBccStaffMail() {
		return bccStaffMail;
	}
	
	public String getBccStaffMail(Position position, OrganisationUnit organisationSettings) {
		MailSettingEnum setting = getMailSetting(position, organisationSettings);
		
		String mail = null;
		if(setting == MailSettingEnum.position) {
			mail = position.getBccMail();
		} else if(setting == MailSettingEnum.organisationUnit) {
			mail = organisationSettings.getStaffBcc();
		} else {
			mail = getBccStaffMail();
		}
		return StringHelper.containsNonWhitespace(mail) ? mail : null;
	}

	public boolean isMailProPositionEnabled() {
		return "enabled".equals(mailProPosition);
	}
	
	public boolean isMailLetterEnabled() {
		return "enabled".equals(mailLetter);
	}

	public boolean isSendBccForConfirmation() {
		return sendBccForConfirmation;
	}

	public String getImpressumUrl() {
		return impressumUrl;
	}

	public String getDisclaimerUrl() {
		return disclaimerUrl;
	}
	
	public boolean isDataProtectionEnabled() {
		return "enabled".equals(dataProtection);
	}
	
	public boolean isApplayTermsInLastStep() {
		return "enabled".equals(applyTermsLastStep);
	}

	public int getMaxRating() {
		return maxRating;
	}
	
	public boolean isRatingAbstentionEnabled() {
		return "enabled".equals(ratingAbstention);
	}
	
	public boolean isRejectionAllDecisionsStepEnabled() {
		return "enabled".equals(rejectionAllDecisionsStep);
	}
	
	public int getUploadLimit() {
		return uploadLimit;
	}
	
	public boolean isApplicationsMemoEnabled() {
		return "enabled".equals(applicationMemo);
	}
	
	public boolean isApplicationsCommitteeCommentEnabled() {
		return "enabled".equals(committeeComment);
	}
	
	public RecruitingDuplicateApplicationAlgorithm getApplicationDuplicateAlgorithm() {
		if("emailFirstnameLastname".equalsIgnoreCase(applicationDuplicateAlgorithm)) {
			return RecruitingDuplicateApplicationAlgorithm.EMAIL_FIRST_LAST_NAME;
		}
		return RecruitingDuplicateApplicationAlgorithm.EMAIL;
	}
	
	/**
	 * @return true if an applicant with the same email can propose several applications.
	 */
	public RecruitingDuplicateApplicationOption getApplicationDuplicateEmailsAllowed() {
		return RecruitingDuplicateApplicationOption.propertyOf(applicationDuplicateEmail);
	}
	
	public boolean isApplicationDuplicateEmailsAllowed(Position position) {
		RecruitingDuplicateApplicationOption opt = getApplicationDuplicateEmailsAllowed();
		if(opt == RecruitingDuplicateApplicationOption.ALLOWED) {
			return true;
		}
		if(opt == RecruitingDuplicateApplicationOption.NOT_ALLOWED) {
			return false;
		}
		
		RecruitingDuplicateApplicationOption positionOption = position.getDuplicateApplicationAllowedEnum();
		if(positionOption == RecruitingDuplicateApplicationOption.ALLOWED) {
			return true;
		}
		if(positionOption == RecruitingDuplicateApplicationOption.NOT_ALLOWED) {
			return false;
		}
		return false;
	}

	public boolean isRoleExOfficioEnabled() {
		return "enabled".equals(roleExOfficio);
	}
	
	public boolean isDecisionToolEnabled() {
		return "enabled".equals(decisionTool);
	}
	
	public boolean isTaggingToolEnabled() {
		return "enabled".equals(taggingTool);
	}
	
	public boolean isSystemTagsEnabled() {
		return "enabled".equals(systemTag);
	}
	
	public boolean isSystemTagsEnabled(Position position) {
		return isSystemTagsEnabled() && (position == null || position.isSystemTagsEnabled());
	}
	
	public boolean isPositionTagsEnabled() {
		return "enabled".equals(positionTag);
	}
	
	public boolean isPositionTagsEnabled(Position position) {
		return isPositionTagsEnabled() && position.isPositionTagsEnabled();
	}
	
	public boolean isAdministrativeTagsEnabled() {
		return "enabled".equals(administrativeTag);
	}
	
	public boolean isCategoriesEnabledFor(Position position) {
		return isTaggingToolEnabled()
				&& ((isSystemTagsEnabled() && position.isSystemTagsEnabled())
						|| (isPositionTagsEnabled() && position.isPositionTagsEnabled()));
	}
	
	public boolean isNotificationsToolEnabled() {
		return "enabled".equals(notificationsTool);
	}
	
	public NotificationPermission[] getNotificationsPermissionsFor(PositionRole role) {
		switch(role) {
			case head: return getNotificationsPermissionsForHead();
			case secretary: return getNotificationsPermissionsForSecretary();
			case member: return getNotificationsPermissionsForCommittee();
			case exofficio: return getNotificationsPermissionsForExOfficio();
			default: return null;
		}
	}
	
	public NotificationPermission[] getNotificationsPermissionsForHead() {
		return notificationsPermissionsForHead;
	}

	public NotificationPermission[] getNotificationsPermissionsForSecretary() {
		return notificationsPermissionsForSecretary;
	}

	public NotificationPermission[] getNotificationsPermissionsForCommittee() {
		return notificationsPermissionsForCommittee;
	}

	public NotificationPermission[] getNotificationsPermissionsForExOfficio() {
		return notificationsPermissionsForExOfficio;
	}

	public NotificationIntervals[] getNotificationIntervals() {
		return NotificationIntervals.values();
	}
	
	public List<PositionRole> getPositionRolesEnabled() {
		PositionRole[] roles = PositionRole.values();
		List<PositionRole> enabled = new ArrayList<>(roles.length);
		for(PositionRole role:roles) {
			if(role == PositionRole.exofficio && !isRoleExOfficioEnabled()) {
				continue;
			}
			enabled.add(role);
		}
		return enabled;
		
	}
	
	public PositionRole[] getRolesAllowedToRate() {
		return positionRolesAllowedToRate;
	}
	
	public PositionRole[] getRolesAllowedToSeeRating() {
		return positionRolesAllowedToSeeRating;
	}
	
	public PositionRole[] getRolesAllowedToSeeRatingDuringRating() {
		return positionRolesAllowedToSeeRatingDuringRating;
	}
	
	public Set<PositionRole> getRolesAllowedToRateSet() {
		List<PositionRole> list = new ArrayList<>();
		for(PositionRole role:getRolesAllowedToRate()) {
			list.add(role);
		}
		return EnumSet.copyOf(list);
	}
	
	public PositionRole[] getRolesAllowedToTakeNotes() {
		return positionRolesAllowedToTakeNotes;
	}

	public PositionRole[] getRolesAllowedToExportApplicationListExcel() {
		return positionRolesAllowedToExportApplicationListExcel;
	}
	
	public PositionRole[] getRolesAllowedToExportReviewsStatisticsExcel() {
		return positionRolesAllowedToExportReviewsStatisticsExcel;
	}
	
	public PositionRole[] getRolesAllowedToExportGeneratedList() {
		return positionRolesAllowedToExportGeneratedList;
	}
	
	public PositionRole[] getRolesAllowedToExportApplicationListPdf() {
		return positionRolesAllowedToExportApplicationListPdf;
	}
	
	public PositionRole[] getRolesAllowedToExportCommitteeListExcel() {
		return positionRolesAllowedToExportCommitteeListExcel;
	}
	
	public PositionRole[] getRolesAllowedToExportRatingsPdf() {
		return positionRolesAllowedToExportRatingsPdf;
	}
	
	public PositionRole[] getRolesAllowedToViewMailCenter() {
		return positionRolesAllowedToViewMailCenter;
	}
	
	public PositionRole[] getRolesAllowedToExportMailCenterLog() {
		return positionRolesAllowedToExportMailCenterLog;
	}
	
	public PositionRole[] getRolesAllowedToViewMailCenterEmail() {
		return positionRolesAllowedToViewMailCenterEmail;
	}
	
	public PositionRole[] getRolesAllowedToResendMailCenterEmail() {
		return positionRolesAllowedToResendMailCenterEmail;
	}
	
	public PositionRole[] getRolesAllowedToSendMailToApplicant() {
		return positionRolesAllowedToSendMailToApplicant;
	}
	
	public PositionRole[] getRolesAllowedToConfigureDecisionTool() {
		return positionRolesAllowedToConfigureDecisionTool;
	}
	
	public PositionRole[] getRolesAllowedToEditDecisionRubrics() {
		return positionRolesAllowedToEditDecisionRubrics;
	}

	public PositionRole[] getRolesAllowedToSendEmailAllCommittee() {
		return positionRolesAllowedToSendEmailAllCommittee;
	}

	public PositionRole[] getRolesAllowedToCreateApplications() {
		return positionRolesAllowedToCreateApplications;
	}

	public PositionRole[] getRolesAllowedToEditApplicationPersonalData() {
		return positionRolesAllowedToEditApplicationPersonalData;
	}

	public PositionRole[] getRolesAllowedToEditApplicationAcademicalBackground() {
		return positionRolesAllowedToEditApplicationAcademicalBackground;
	}
	
	public PositionRole[] getRolesAllowedToEditApplicationProject() {
		return positionRolesAllowedToEditApplicationProject;
	}

	public PositionRole[] getRolesAllowedToEditApplicationDocuments() {
		return positionRolesAllowedToEditApplicationDocuments;
	}

	public PositionRole[] getRolesAllowedToEditApplicationStatus() {
		return positionRolesAllowedToEditApplicationStatus;
	}
	
	public PositionRole[] getRolesAllowedToEditApplicationCategories() {
		return positionRolesAllowedToEditApplicationCategories;
	}
	
	public PositionRole[] getRolesAllowedToSendBulkApplicationEmails() {
		return positionRolesAllowedToSendBulkApplicationEmails;
	}
	
	public PositionRole[] getRolesAllowedToEditApplicationReferences() {
		return positionRolesAllowedToEditApplicationReferences;
	}
	
	public PositionRole[] getRolesAllowedToDeletePublicFeedback() {
		return positionRolesAllowedToDeletePublicFeedback;
	}
	
	public PositionRole[] getRolesAllowedToEditMembersFeedback() {
		return positionRolesAllowedToEditMembersFeedback;
	}
	
	public PositionRole[] getRolesAllowedToSeeMembersFeedback() {
		return positionRolesAllowedToSeeMembersFeedback;
	}
	
	public PositionRole[] getRolesAllowedToDeleteMembersFeedback() {
		return positionRolesAllowedToDeleteMembersFeedback;
	}
	
	public PositionRole[] getRolesAllowedToEditApplicationCommitteeComment() {
		return positionRolesAllowedToEditApplicationCommitteeComment;
	}

	public PositionRole[] getRolesAllowedToDeleteApplication() {
		return positionRolesAllowedToDeleteApplication;
	}

	public PositionRole[] getRolesAllowedToSeeParallelApplications() {
		return positionRolesAllowedToSeeParallelApplications;
	}

	public PositionRole[] getRolesAllowedToEditCommitteDecision() {
		return positionRolesAllowedToEditCommitteDecision;
	}
	
	public PositionRole[] getRolesAllowedToSeePublishedPositions() {
		return positionRolesAllowedToSeePublishedPositions;
	}
	
	public PositionRole[] getRolesAllowedToSeeExpertBlackList() {
		return positionRolesAllowedToSeeExpertBlackList;
	}
	
	public PositionRole[] getPositionRolesAllowedToEditApplicationsMemo() {
		return positionRolesAllowedToEditApplicationsMemo;
	}

	public void setPositionRolesAllowedToEditApplicationsMemo(PositionRole[] positionRolesAllowedToEditApplicationsMemo) {
		this.positionRolesAllowedToEditApplicationsMemo = positionRolesAllowedToEditApplicationsMemo;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionStatus() {
		return positionRolesAllowedToEditPositionStatus;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionProfile() {
		return positionRolesAllowedToEditPositionProfile;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionApplicationsSettings() {
		return positionRolesAllowedToEditPositionApplicationsSettings;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionReferencesSettings() {
		return positionRolesAllowedToEditPositionReferencesSettings;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionFeedbacksSettings() {
		return positionRolesAllowedToEditPositionFeedbacksSettings;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionEvaluationSettings() {
		return positionRolesAllowedToEditPositionEvaluationSettings;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionTagsSettings() {
		return positionRolesAllowedToEditPositionTagsSettings;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionMailTemplates() {
		return positionRolesAllowedToEditPositionMailTemplates;
	}
	
	public PositionRole[] getRolesAllowedToEditPositionCommittee() {
		return positionRolesAllowedToEditPositionCommittee;
	}
	
	public PositionRole[] getRolesAllowedToAddPositionCommittee() {
		return positionRolesAllowedToAddPositionCommittee;
	}
	
	public PositionRole[] getRolesAllowedToRemovePositionCommittee() {
		return positionRolesAllowedToRemovePositionCommittee;
	}
	
	public PositionRole[] getRolesAllowedToSearchApplications() {
		return positionRolesAllowedToSearchApplications;
	}
	
	public PositionRole[] getRolesAllowedToEditAssignments() {
		return positionRolesAllowedToEditAssignments;
	}
	
	public FilterPermissions[] getRolesAllowedToShareApplicationListFilters() {
		return positionRolesAllowedToShareApplicationListFilters;
	}
	
	public FilterPermissions[] getRolesAllowedToManageApplicationListFilters() {
		return positionRolesAllowedToManageApplicationListFilters;
	}
	
	public FilterPermissions[] getRolesAllowedToApplicationListBasicFilters() {
		return positionRolesAllowedToApplicationBasicFilters;
	}
	
	public FilterPermissions[] getRolesAllowedToApplicationListAdvancedFilters() {
		return positionRolesAllowedToApplicationAdvancedFilters;
	}
	
	public PositionRole[] getRolesAllowedToEditAdministrativeTags() {
		return positionRolesAllowedToEditAdministrativeTags;
	}
	
	public PositionRole[] getRolesAllowedToSeeAdministrativeTags() {
		return positionRolesAllowedToSeeAdministrativeTags;
	}

	public AssignmentMethods[] getAssignmentMethods() {
		return assignmentMethods;
	}
	
	public boolean isApplicationAssignmentsEnabled() {
		return assignmentMethods != null && assignmentMethods.length > 0;
	}
	
	public PersonTitle[] getUserPersonTitles() {
		return userTitles;
	}
	
	public boolean isShareFiltersEnabled() {
		return "enabled".equals(shareFilters);
	}

	public String[] getMailTemplateTitles() {
		String[] titles;
		if(StringHelper.containsNonWhitespace(mailTemplateTitles)) {
			titles = mailTemplateTitles.split("[,]");
		} else {
			titles = new String[0];
		}
		return titles;
	}
	
	public boolean isMailTemplateTitle(String string) {
		String[] templateNames = getMailTemplateTitles();
		for(String templateName:templateNames) {
			if(templateName.equals(string)) {
				return true;
			}
			
		}
		return false;
	}
	
	public String getMailTemplateRejectionTitle() {
		return mailTemplateRejectionTitle;
	}
	
	public MailCenterExcludeOption getMailCenterExclusionOption() {
		if(StringHelper.containsNonWhitespace(mailCenterExclusionFilter)) {
			return MailCenterExcludeOption.valueOf(mailCenterExclusionFilter);
		}
		return MailCenterExcludeOption.alreadySent;
	}
	
	public boolean isMailTemplateToolPositionsEnabled() {
		return "enabled".equals(mailTemplateToolPositions);
	}
	
	public boolean isTableApplicationsGenerateListEnabled() {
		return "enabled".equals(tableApplicationsGenerateList);
	}
	
	public SortKey getTableApplicationSort() {
		return new SortKey(tableApplicationsSortField, "asc".equals(tableApplicationsSortDirection));
	}

	public RecruitingTableOption getTableApplicationsOrganizationOption() {
		if(isApplicationBusinessInformationsOrganizationEnabled()) {
			return tableApplicationsOrganizationOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsOrganizationUnitOption() {
		if(isApplicationBusinessInformationsUnitEnable()) {
			return tableApplicationsOrganizationUnitOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsOrganizationCurrentPositionOption() {
		if(isApplicationBusinessInformationsCurrentPositionEnabled()) {
			return tableApplicationsOrganizationCurrentPositionOption;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableApplicationsSubmittedDateOption() {
		return tableApplicationsSubmittedDateOption;
	}

	public RecruitingTableOption getTableApplicationsSubmittedByStaffOption() {
		return tableApplicationsSubmittedByStaffOption;
	}

	public RecruitingTableOption getTableApplicationsNationalityOption() {
		if(isApplicationPersonNationalityEnabled()) {
			return tableApplicationsNationalityOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsAdditionalNationalitiesOption() {
		if(isApplicationPersonAdditionalNationalitiesEnabled()) {
			return tableApplicationsAdditionalNationalitiesOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsHighestDegreeOption() {
		return tableApplicationsHighestDegreeOption;
	}
	
	public RecruitingTableOption getTableApplicationsHighestDegreeInstitutionOption() {
		return tableApplicationsHighestDegreeInstitutionOption;
	}

	public RecruitingTableOption getTableApplicationsHighestDegreeYearOption() {
		return tableApplicationsHighestDegreeYearOption;
	}
	
	public boolean isTableApplicationsHighestDegreeYearOnlyPhDOption() {
		return "enabled".equals(tableApplicationsHighestDegreeYearOnlyPhDEnable);
	}
	
	public RecruitingTableOption getTableApplicationsWorkedInAcademiaSinceOption() {
		return tableApplicationsWorkedInAcademiaSinceOption;
	}
	
	public RecruitingTableOption getTableApplicationsWorkedOutAcademiaSinceOption() {
		return tableApplicationsWorkedOutAcademiaSinceOption;
	}
	
	public RecruitingTableOption getTableApplicationsWorkedOutAcademiaCareSinceOption() {
		return tableApplicationsWorkedOutAcademiaCareSinceOption;
	}
	
	public RecruitingTableOption getTableApplicationsDissertationTitleOption() {
		return tableApplicationsDissertationTitleOption;
	}
	
	public RecruitingTableOption getTableApplicationsDissertationDateOption() {
		return tableApplicationsDissertationDateOption;
	}
	
	public RecruitingTableOption getTableApplicationsDissertationInstitutionOption() {
		return tableApplicationsDissertationInstitutionOption;
	}

	public RecruitingTableOption getTableApplicationsDissertationKeyword1Option() {
		return tableApplicationsDissertationKeyword1Option;
	}

	public RecruitingTableOption getTableApplicationsDissertationKeyword2Option() {
		return tableApplicationsDissertationKeyword2Option;
	}

	public RecruitingTableOption getTableApplicationsDissertationKeyword3Option() {
		return tableApplicationsDissertationKeyword3Option;
	}

	public RecruitingTableOption getTableApplicationsEMailOption() {
		return tableApplicationsEMailOption;
	}

	public RecruitingTableOption getTableApplicationsPhoneOption() {
		return tableApplicationsPhoneOption;
	}
	
	public RecruitingTableOption getTableApplicationsPersonTitleOption() {
		if(isApplicationPersonTitleEnabled()) {
			return tableApplicationsPersonTitleOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsPersonFirstNameOption() {
		return tableApplicationsPersonFirstNameOption;
	}
	
	public RecruitingTableOption getTableApplicationsPersonLastNameOption() {
		return tableApplicationsPersonLastNameOption;
	}
	
	public RecruitingTableOption getTableApplicationsMobilePhoneOption() {
		return tableApplicationsMobilePhoneOption;
	}
	
	public RecruitingTableOption getTableApplicationsGenderOption() {
		if(isApplicationPersonGenderEnabled()) {
			return tableApplicationsGenderOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsMaritalStatusOption() {
		return tableApplicationsMaritalStatusOption;
	}
	
	public RecruitingTableOption getTableApplicationsYearOfBirthOption() {
		if(isApplicationPersonBirthdayEnabled()) {
			return tableApplicationsYearOfBirthOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsBirthdayOption() {
		if(isApplicationPersonBirthdayEnabled()) {
			return tableApplicationsBirthdayOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationPersonAcademicTitleOption() {
		if(isApplicationPersonAcademicTitleEnabled()) {
			return RecruitingTableOption.optional;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsDisabilityOption() {
		return tableApplicationsDisabilityOption;
	}

	public RecruitingTableOption getTableApplicationsAddressLinesOption() {
		return tableApplicationsAddressLinesOption;
	}

	public RecruitingTableOption getTableApplicationsBusinessAddressLinesOption() {
		return tableApplicationsBusinessAddressLinesOption;
	}

	public RecruitingTableOption getTableApplicationsZipcodeOption() {
		return tableApplicationsZipcodeOption;
	}

	public RecruitingTableOption getTableApplicationsBusinessZipcodeOption() {
		return tableApplicationsBusinessZipcodeOption;
	}
	
	public RecruitingTableOption getTableApplicationsExpertsOption() {
		return tableApplicationsExpertsOption;
	}

	public RecruitingTableOption getTableApplicationsRefereesOption() {
		return tableApplicationsRefereesOption;
	}
	
	public RecruitingTableOption getTableApplicationsComparativeExpertsOption() {
		return tableApplicationsComparativeExpertsOption;
	}

	public RecruitingTableOption getTableApplicationsProvidedExpertsRecommendationsOption() {
		return tableApplicationsProvidedExpertsRecommendationsOption;
	}

	public RecruitingTableOption getTableApplicationsMemoOption() {
		return tableApplicationsMemoOption;
	}
	
	public RecruitingTableOption getTableApplicationsCommitteeCommentOption() {
		return tableApplicationsCommitteeCommentOption;
	}
	
	public RecruitingTableOption getTableApplicationsStatusOption() {
		return tableApplicationsStatusOption;
	}
	
	public RecruitingTableOption getTableApplicationsStatusDateOption() {
		return tableApplicationsStatusDateOption;
	}
	
	public RecruitingTableOption getTableApplicationsParallelApplicationsOption() {
		return tableApplicationsParallelApplicationsOption;
	}

	public RecruitingTableContextualOption getTableApplicationsCommitteeRating() {
		return tableApplicationsCommitteeRating;
	}

	public RecruitingTableContextualOption getTableApplicationsDecision() {
		return tableApplicationsDecision;
	}
	
	public String getTableApplicationsDefaultSelectedAdvancedFilter() {
		return tableApplicationsDefaultSelectedAdvancedFilter;
	}
	
	public String[] getTableApplicationsDefaultAdvancedFilters() {
		if(StringHelper.containsNonWhitespace(tableApplicationsDefaultAdvancedFilters)) {
			return tableApplicationsDefaultAdvancedFilters.split("[,]");
		}
		return new String[0];
	}
	
	public String[] getTableApplicationsDefaultBasicFilterFields() {
		if(StringHelper.containsNonWhitespace(tableApplicationsDefaultBasicFilterFields)) {
			return tableApplicationsDefaultBasicFilterFields.split("[,]");
		}
		return new String[0];
	}
	
	public ApplicationStatus[] getTableApplicationsDefaultBasicFilterApplicationStatus() {
		return applicationsDefaultBasicFilterApplicationStatus;
	}
	
	public RecruitingTableOption getTableApplicationsReviewsOption() {
		if(isReviewEnabled()) {
			return tableApplicationsReviewsOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public PositionRole[] getTableApplicationsMyRatingsAlwaysVisibleForRoles() {
		return tableApplicationsMyRatingsAlwaysVisibleForRoles;
	}
	
	public PositionRole[] getTableApplicationsMyReviewsAlwaysVisibleForRoles() {
		return tableApplicationsMyReviewsAlwaysVisibleForRoles;
	}
	
	public boolean isTableApplicationsResetFilterApplication() {
		return "true".equals(tableApplicationsResetFilterApplication);
	}
	
	public String[] getTableApplicationsWithoutCEmailsFilterPreSelection() {
		if(StringHelper.containsNonWhitespace(tableApplicationsWithoutCEmailsFilterPreSelection)) {
			return tableApplicationsWithoutCEmailsFilterPreSelection.split("[,]");
		}
		return new String[0];
	}

	public RecruitingTableOption getTableApplicationsProject() {
		if(isApplicationProjectEnabled() && (isApplicationProjectTitleEnabled()
				|| isApplicationProjectFinancialImpact1Enabled() || isApplicationProjectDescriptionEnabled())) {
			return tableApplicationsProjectOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectDescription() {
		if(isApplicationProjectEnabled() && isApplicationProjectDescriptionEnabled()) {
			return tableApplicationsProjectDescriptionOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectTitle() {
		if(isApplicationProjectEnabled() && isApplicationProjectTitleEnabled()) {
			return tableApplicationsProjectTitleOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectAcronym() {
		if(isApplicationProjectEnabled() && isApplicationProjectAcronymEnabled()) {
			return tableApplicationsProjectAcronymOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectKeywords() {
		if(isApplicationProjectEnabled() && isApplicationProjectKeywordsEnabled()) {
			return tableApplicationsProjectKeywordsOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectDisciplines() {
		if(isApplicationProjectEnabled() && isApplicationProjectDisciplinesEnabled()) {
			return tableApplicationsProjectDisciplinesOption;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableApplicationsProjectFinancialImpact1() {
		if(isApplicationProjectEnabled() && isApplicationProjectFinancialImpact1Enabled()) {
			return tableApplicationsProjectFinancialImpact1Option;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableApplicationsProjectFinancialImpact2() {
		if(isApplicationProjectEnabled() && isApplicationProjectFinancialImpact2Enabled()) {
			return tableApplicationsProjectFinancialImpact2Option;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableApplicationsProjectFinancialImpact3() {
		if(isApplicationProjectEnabled() && isApplicationProjectFinancialImpact3Enabled()) {
			return tableApplicationsProjectFinancialImpact3Option;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableApplicationsProjectFinancialImpact4() {
		if(isApplicationProjectEnabled() && isApplicationProjectFinancialImpact4Enabled()) {
			return tableApplicationsProjectFinancialImpact4Option;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableApplicationsProjectFinancialImpact5() {
		if(isApplicationProjectEnabled() && isApplicationProjectFinancialImpact5Enabled()) {
			return tableApplicationsProjectFinancialImpact5Option;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectStartDate() {
		if(isApplicationProjectEnabled() && isApplicationProjectStartDateEnabled()) {
			return tableApplicationsProjectStartDateOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableApplicationsProjectDuration() {
		if(isApplicationProjectEnabled() && isApplicationProjectDurationEnabled()) {
			return tableApplicationsProjectDurationOption;
		}
		return RecruitingTableOption.disabled;
	}

	public RecruitingTableOption getTableCommitteeUserPropertiesOption() {
		return tableCommitteeUserPropertiesOption;
	}
	
	public RecruitingTableOption getTableFeedbacksUserPropertiesOption() {
		return tableFeedbacksUserPropertiesOption;
	}
	
	public String[] getTableFeedbacksDefaultAdvancedFilters() {
		if(StringHelper.containsNonWhitespace(tableFeedbacksDefaultAdvancedFilters)) {
			return tableFeedbacksDefaultAdvancedFilters.split("[,]");
		}
		return new String[0];
	}
	
	public String[] getTableReferencesDefaultAdvancedFilters() {
		if(StringHelper.containsNonWhitespace(tableReferencesDefaultAdvancedFilters)) {
			return tableReferencesDefaultAdvancedFilters.split("[,]");
		}
		return new String[0];
	}
	
	public RecruitingTableOption getTableReferencesApplicationFullNameOption() {
		return tableReferencesApplicationFullNameOption;
	}
	
	public RecruitingTableOption getTableReferencesApplicationIdOption() {
		return tableReferencesApplicationIdOption;
	}
	
	public RecruitingTableOption getTableReferencesProjectTitleOption() {
		return tableReferencesProjectTitleOption;
	}
	
	public RecruitingTableOption getTableReferencesProjectAcronymOption() {
		return tableReferencesProjectAcronymOption;
	}
	
	public RecruitingTableOption getTableReferencesProjectKeywordsOption() {
		return tableReferencesProjectKeywordsOption;
	}
	
	public RecruitingTableOption getTableReferencesProjectDisciplinesOption() {
		return tableReferencesProjectDisciplinesOption;
	}
	
	public RecruitingTableOption getTableReferencesProjectFinancialImpact1Option() {
		return tableReferencesProjectFinancialImpact1Option;
	}
	
	public RecruitingTableOption getTableReferencesProjectFinancialImpact2Option() {
		return tableReferencesProjectFinancialImpact2Option;
	}
	
	public RecruitingTableOption getTableReferencesProjectFinancialImpact3Option() {
		return tableReferencesProjectFinancialImpact3Option;
	}
	
	public RecruitingTableOption getTableReferencesProjectFinancialImpact4Option() {
		return tableReferencesProjectFinancialImpact4Option;
	}

	public RecruitingTableOption getTableReferencesProjectFinancialImpact5Option() {
		return tableReferencesProjectFinancialImpact5Option;
	}

	public RecruitingTableOption getTableReferencesProjectStartDateOption() {
		return tableReferencesProjectStartDateOption;
	}

	public RecruitingTableOption getTableReferencesProjectDurationOption() {
		return tableReferencesProjectDurationOption;
	}
	
	public RecruitingTableOption getTableReferencesProjectDescriptionOption() {
		return tableReferencesProjectDescriptionOption;
	}
	
	public RecruitingTableOption getTableReferenceToApplicationTitleOption() {
		return tableReferenceToApplicationTitleOption;
	}
	
	public RecruitingTableOption getTableReferenceToApplicationFirstNameOption() {
		return tableReferenceToApplicationFirstNameOption;
	}
	
	public RecruitingTableOption getTableReferenceToApplicationLastNameOption() {
		return tableReferenceToApplicationLastNameOption;
	}
	
	public RecruitingTableOption getTableReferenceToProjectTitleOption() {
		return tableReferenceToProjectTitleOption;
	}
	
	public RecruitingTableOption getTableApplicantDashboardDueDateOption() {
		return tableApplicantDashboardDueDateOption;
	}

	public RecruitingTableOption getTableDecisionApplicationTitleOption() {
		if (isAdministrativeTagsEnabled()) {
			return tableDecisionApplicationTitleOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableDecisionApplicationFirstNameOption() {
		return tableDecisionApplicationFirstNameOption;
	}
	
	public RecruitingTableOption getTableDecisionApplicationLastNameOption() {
		return tableDecisionApplicationLastNameOption;
	}
	
	public RecruitingTableOption getTableDecisionProjectTitleOption() {
		return tableDecisionProjectTitleOption;
	}
	
	public RecruitingTableOption getTableDecisionProjectAcronymOption() {
		return tableDecisionProjectAcronymOption;
	}
	
	public RecruitingTableOption getTableDecisionProjectKeywordsOption() {
		return tableDecisionProjectKeywordsOption;
	}
	
	public RecruitingTableOption getTableDecisionProjectDisciplinesOption() {
		return tableDecisionProjectDisciplinesOption;
	}
	
	public RecruitingTableOption getTableDecisionProjectFinancialImpact1Option() {
		return tableDecisionProjectFinancialImpact1Option;
	}
	
	public RecruitingTableOption getTableDecisionProjectFinancialImpact2Option() {
		return tableDecisionProjectFinancialImpact2Option;
	}
	
	public RecruitingTableOption getTableDecisionProjectFinancialImpact3Option() {
		return tableDecisionProjectFinancialImpact3Option;
	}
	
	public RecruitingTableOption getTableDecisionProjectFinancialImpact4Option() {
		return tableDecisionProjectFinancialImpact4Option;
	}
	
	public RecruitingTableOption getTableDecisionProjectFinancialImpact5Option() {
		return tableDecisionProjectFinancialImpact5Option;
	}
	
	public RecruitingTableOption getTableDecisionProjectStartDateOption() {
		return tableDecisionProjectStartDateOption;
	}
	
	public RecruitingTableOption getTableDecisionProjectDurationOption() {
		return tableDecisionProjectDurationOption;
	}
	
	public RecruitingTableOption getTableMailApplicationTitleOption() {
		if (isApplicationPersonTitleEnabled()) {
			return tableMailApplicationTitleOption;
		}
		return RecruitingTableOption.disabled;
	}
	
	public RecruitingTableOption getTableMailApplicationFirstNameOption() {
		return tableMailApplicationFirstNameOption;
	}
	
	public RecruitingTableOption getTableMailApplicationLastNameOption() {
		return tableMailApplicationLastNameOption;
	}
	
	public RecruitingTableOption getTableMailProjectTitleOption() {
		return tableMailProjectTitleOption;
	}
	
	public RecruitingTableOption getTableMailProjectAcronymOption() {
		return tableMailProjectAcronymOption;
	}
	
	public RecruitingTableOption getTableMailProjectKeywordsOption() {
		return tableMailProjectKeywordsOption;
	}
	
	public RecruitingTableOption getTableMailProjectDisciplinesOption() {
		return tableMailProjectDisciplinesOption;
	}
	
	public RecruitingTableOption getTableMailProjectFinancialImpact1Option() {
		return tableMailProjectFinancialImpact1Option;
	}
	
	public RecruitingTableOption getTableMailProjectFinancialImpact2Option() {
		return tableMailProjectFinancialImpact2Option;
	}
	
	public RecruitingTableOption getTableMailProjectFinancialImpact3Option() {
		return tableMailProjectFinancialImpact3Option;
	}
	
	public RecruitingTableOption getTableMailProjectFinancialImpact4Option() {
		return tableMailProjectFinancialImpact4Option;
	}

	public RecruitingTableOption getTableMailProjectFinancialImpact5Option() {
		return tableMailProjectFinancialImpact5Option;
	}

	public RecruitingTableOption getTableMailProjectStartDateOption() {
		return tableMailProjectStartDateOption;
	}

	public RecruitingTableOption getTableMailProjectDurationOption() {
		return tableMailProjectDurationOption;
	}
	
	public RecruitingTableOption getTableMailProjectDescriptionOption() {
		return tableMailProjectDescriptionOption;
	}
	
	public RecruitingTableOption getTableMailApplicationStatusOption() {
		return tableMailApplicationStatusOption;
	}
	
	public RecruitingTableOption getTableMailEmailLogStatusOption() {
		return tableMailEmailLogStatusOption;
	}

	/**
	 * The default settings at the instance level.
	 * 
	 * @return
	 */
	public List<DocumentOption> getDocumentOptions() {
		return docOptions;
	}
	
	/**
	 * Helper method to calculate the document options from the
	 * configuration of the position and the default settings.
	 * 
	 * @param position The position (mandatory)
	 * @return The ordered list of options
	 */
	public List<DocumentOption> getDocumentOptions(Position position) {
		List<DocumentOption> defaultOptions = getDocumentOptions();
		Map<DocumentEnum,Integer> documentSizes = position.getDocumentSizes();
		List<DocumentOption> options = new ArrayList<>(defaultOptions.size());
		for(DocumentOption defaultOption:defaultOptions) {
			int maxSize = defaultOption.getMaxSize();
			if(documentSizes.containsKey(defaultOption.getDoc())) {
				maxSize = documentSizes.get(defaultOption.getDoc()).intValue();
			}
			options.add(new DocumentOption(defaultOption.getDoc(), maxSize));
		}
		return options;
	}
	
	public DocumentType[] getDocumentTypes() {
		return DocumentType.types(docTypes);
	}
	
	public String getDefaultPositionRefereeRecommendationDocs() {
		return defaultPositionRefereeRecommendationDocs;
	}
	
	public String getDefaultPositionExpertRecommendationDocs() {
		return defaultPositionExpertRecommendationDocs;
	}
	
	public boolean isAdvertisementDefaultEnabled() {
		return "enabled".equals(defaultPositionAdvertisement);
	}
	
	public boolean isApplicationDetailsFieldVisible(String field) {
		if(StringHelper.containsNonWhitespace(applicationDetailsHiddenFields)
				&& applicationDetailsHiddenFields.contains(field)) {
			String[] hiddenFields = applicationDetailsHiddenFields.split(",");
			for(String hiddenField:hiddenFields) {
				if(hiddenField.equals(field)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public ParallelApplicationScope getParallelApplicationScope() {
		return ParallelApplicationScope.secureValue(parallelApplicationsScope);
	}
	
	public boolean isCopyApplicationEnabled() {
		return "enabled".equals(copyApplications);
	}
	
	public ReferenceStatus[] getApplicationReferencesVisibleStatus() {
		return applicationReferencesVisibleStatus;
	}
	
	public ApplicationStatus[] getApplicationAvailableStatus() {
		return applicationAvailableStatus;
	}
	
	public boolean isApplicationProjectEnabled() {
		return "enabled".equals(applicationProject) || "optional".equals(applicationProject);
	}
	
	public boolean isApplicationProjectEnabledDefault() {
		return "true".equals(applicationProjectDefault);
	}
	
	public boolean isRatingPolicyFocusEnabled() {
		return "enabled".equals(ratingPolicyFocus);
	}
	
	public boolean isRatingPolicyPotentialCandidates() {
		return "enabled".equals(ratingPolicyPotentialCandidates);
	}
	
	public boolean isProfessorshipTypeEnabled() {
		return "enabled".equals(professorshipType);
	}
	
	public boolean isRatingPolicyProfessorshipTypeGenericExplanationEnabled() {
		return "enabled".equals(ratingPolicyProfessorshipTypeGenericExplanation);
	}
	
	public boolean isReferenceEnabled() {
		return "enabled".equals(reference);
	}
	
	public boolean isReferenceConsentEnabled() {
		return "enabled".equals(referenceConsent);
	}
	
	public boolean isReferenceExpertsBlackListEnabled() {
		return "enabled".equals(referenceExpertsBlackList);
	}
	
	public boolean isReferenceRefereeConsentEnabled() {
		return "enabled".equals(referenceRefereeConsent);	
	}
	
	public boolean isReferenceApplicantManagement() {
		return "enabled".equals(referenceApplicantManagement);
	}
	
	public boolean isReferenceAdminNotes() {
		return "enabled".equals(referenceAdminNotes);
	}
	
	public boolean isReferenceOneTimeCodeEnabled() {
		return "enabled".equals(referenceOneTimeCode) || "true".equals(referenceOneTimeCode);
	}
	
	public int getReferenceNumberOfDisclaimers() {
		return referenceDisclaimers;
	}

	public String getReferenceTemplate0() {
		return referenceTemplate0;
	}

	public String getReferenceTemplate1() {
		return referenceTemplate1;
	}
	
	public boolean isReferenceExplanationInEditApplicationEnabled() {
		return "enabled".equals(referenceExplanationInEditApplication);
	}
	
	public ReferenceSendMailType getReferenceSendEmail() {
		if(StringHelper.containsNonWhitespace(referenceSendEmail)) {
			return ReferenceSendMailType.valueOf(referenceSendEmail);
		}
		return ReferenceSendMailType.auto;
	}
	
	public boolean isReferencePrivacyDisclaimer() {
		return "enabled".equals(referencePrivacyDisclaimer);
	}
	
	public PersonTitle[] getReferencePersonTitles() {
		return referenceTitles;
	}
	
	public List<String> getNewPositionExcludedAttributesList() {
		return newPositionExcludedAttributesList;
	}
	
	public boolean isComparativeAssessmentExpertsEnabled() {
		return "enabled".equals(comparativeAssessmentExperts);
	}
	
	public boolean isReviewEnabled() {
		return "enabled".equals(review);
	}

	public Integer getReviewDefaultSliderSteps() {
		return reviewDefaultSliderSteps;
	}
	
	public boolean isReviewStatisticsZeroBased() {
		return reviewStatisticsZeroBased;
	}
	
	public boolean isReviewStatisticsEnabled() {
		return reviewStatisticsEnabled;
	}
	
	public boolean isReviewStatisticsChartEnabled() {
		return reviewStatisticsChartEnabled;
	}
	
	public boolean isReviewStatisticsDownloadEnabled() {
		return reviewStatisticsDownloadEnabled;
	}
	
	public PositionStatus[] getPositionStatus() {
		return positionStatusAvailable;
	}
	
	public Locale getReportingLocale() {
		return I18nModule.getDefaultLocale();
	}
	
	public Locale[] getPositionLocales() {
		return positionLocales;
	}
	
	public List<Locale> getPositionLocales(Position position) {
		Locale[] positionLanguagesArr = getPositionLocales();
		List<Locale> positionLanguageList = new ArrayList<>();
		Collections.addAll(positionLanguageList, positionLanguagesArr);
		
		if(position != null) {
			String availableLanguages = position.getAvailableLanguages();
			if(StringHelper.containsNonWhitespace(availableLanguages) && !"-".equals(availableLanguages)) {
				String[] availableLanguageArr = position.getAvailableLanguagesArray();
				for(Iterator<Locale> localeIt=positionLanguageList.iterator(); localeIt.hasNext(); ) {
					Locale positionLanguage = localeIt.next();
					boolean ok = false;
					for(int j=0; j<availableLanguageArr.length; j++) {
						if(positionLanguage.getLanguage().equals(availableLanguageArr[j])) {
							ok = true;
						}
					}
					if(!ok) {
						localeIt.remove();
					}
				}
			}
		}
		
		return positionLanguageList;
	}

	public Locale getPositionDefaultLocale() {
		return positionDefaultLocale;
	}
	
	public Locale getPositionLocale(String language) {
		Locale locale = getPositionDefaultLocale();
		for(Locale positionLocale:positionLocales) {
			if(positionLocale.getLanguage().equals(language)) {
				locale = positionLocale;
			}
		}
		return locale;
	}
	
	public boolean isPositionAcademicalBackgroundEnabled() {
		return !"hidden".equals(positionAcademicalBackgroundConfigurationEnabled);
	}
	
	public boolean isPositionAcademicalBackgroundConfigurable() {
		return "enabled".equals(positionAcademicalBackgroundConfigurationEnabled);
	}
	
	public boolean isPositionCustomStepsEnabled() {
		return "enabled".equals(positionCustomStepsEnabled);
	}
	
	public int getPositionMaxNumberOfAdditionalAttributes() {
		return positionMaxNumberOfAdditionalAttributes;
	}
	
	public int getPositionMaxNumberOfAdditionalGlobalAttributes() {
		return 50;
	}
	
	public boolean isPositionPlannigIdEnabled() {
		return "enabled".equals(positionPlanningId)
				|| "optional".equals(positionPlanningId);
	}
	
	public boolean isPositionPlannigIdOptional() {
		return "disabled".equals(positionPlanningId)
				|| "optional".equals(positionPlanningId);
	}
	
	public boolean isPositionDepartmentEnabled() {
		return "enabled".equals(positionDepartment)
				|| "optional".equals(positionDepartment);
	}
	
	public boolean isPositionDepartmentOptional() {
		return "disabled".equals(positionDepartment)
				|| "optional".equals(positionDepartment);
	}
	
	public boolean isPositionPrefillDepartment() {
		return "enabled".equals(positionDepartmentPrefill);
	}
	
	public boolean isPositionHomepageEnabled() {
		return "enabled".equals(positionHomepage)
				|| "optional".equals(positionHomepage);
	}
	
	public boolean isPositionHomepageOptional() {
		return "disabled".equals(positionHomepage)
				|| "optional".equals(positionHomepage);
	}
	
	public boolean isPositionOrgUnitOptional() {
		return "disabled".equals(positionOrgUnit)
				|| "optional".equals(positionOrgUnit);
	}
	
	public boolean isPositionOrgUnitEnabled() {
		return "enabled".equals(positionOrgUnit);
	}
	
	public boolean isPositionCopyEnabled() {
		return "enabled".equals(positionCopy);
	}
	
	public int getPositionDescriptionRows() {
		return positionDescriptionRows;
	}
	
	public boolean isPositionJobAdsOtherEnabled() {
		return "enabled".equals(positionJobAdsOther);
	}
	
	public boolean isPositionJobAdsFreeTextOnlyEnabled() {
		return "enabled".equals(positionJobAdsFreeTextOnly);
	}
	
	public boolean isFieldEnabled(String fieldName) {
		switch(fieldName) {
			case APP_ID: return true;
			case APP_PERSON_TITLE: return isApplicationPersonTitleEnabled();
			case APP_PERSON_GENDER: return isApplicationPersonGenderEnabled();
			case APP_PERSON_FIRSTNAME: return true;
			case APP_PERSON_LASTNAME: return true;
			case APP_PERSON_MARITAL_STATUS: return isApplicationPersonMaritalStatusEnabled();
			case APP_PERSON_DISABILITY: return isApplicationPersonDisabilityEnabled();
			case APP_PERSON_ACADEMIC_TITLE: return isApplicationPersonAcademicTitleEnabled();
			case APP_PERSON_BIRTHDAY: return isApplicationPersonBirthdayEnabled();
			case APP_PERSON_MOBILE_PHONE: return isApplicationPersonMobilePhoneEnabled();
			case APP_PERSON_PHONE: return isApplicationPersonPhoneEnabled();
			case APP_PERSON_EMAIL: return true;
			case APP_PERSON_NATIONALITY: return isApplicationPersonNationalityEnabled();
			case APP_PERSON_ADD_NATIONALITIES: return isApplicationPersonAdditionalNationalitiesEnabled();
			case APP_BUSINESS_INFOS_ORGANISATION: return isApplicationBusinessInformationsOrganizationEnabled();
			case APP_BUSINESS_INFOS_UNIT: return isApplicationBusinessInformationsUnitEnable();
			case APP_BUSINESS_INFOS_POSITION: return isApplicationBusinessInformationsCurrentPositionEnabled();
			
			case APP_ADDRESS_PRIVATE_LINE1: return isApplicationAddressPrivateActivated();
			case APP_ADDRESS_PRIVATE_LINE2: return isApplicationAddressPrivateActivated();
			case APP_ADDRESS_PRIVATE_LINE3: return isApplicationAddressPrivateActivated() && isApplicationAddressLine3Enabled();
			case APP_ADDRESS_PRIVATE_CODE: return isApplicationAddressPrivateActivated();
			case APP_ADDRESS_PRIVATE_CITY: return isApplicationAddressPrivateActivated();
			case APP_ADDRESS_PRIVATE_COUNTRY: return isApplicationAddressPrivateActivated();
			
			case APP_ADDRESS_BUSINESS_LINE1: return isApplicationAddressBusinessActivated();
			case APP_ADDRESS_BUSINESS_LINE2: return isApplicationAddressBusinessActivated();
			case APP_ADDRESS_BUSINESS_LINE3: return isApplicationAddressBusinessActivated() && isApplicationAddressLine3Enabled();
			case APP_ADDRESS_BUSINESS_CODE: return isApplicationAddressBusinessActivated();
			case APP_ADDRESS_BUSINESS_CITY: return isApplicationAddressBusinessActivated();
			case APP_ADDRESS_BUSINESS_COUNTRY: return isApplicationAddressBusinessActivated();
			
			case APP_ADDRESS_BUSINESS_PHONE: return isApplicationBusinessPhoneEnabled();
			case APP_ADDRESS_BUSINESS_MAIL: return isApplicationBusinessMailEnabled();
			
			//Academic background
			case APP_ACADEMIC_NUM_OF_ORIGINAL_PUBLICATIONS: return isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled();
			case APP_ACADEMIC_NUM_OF_FIRST_AUTHORSHIPS: return isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled();
			case APP_ACADEMIC_NUM_OF_LAST_AUTHORSHIPS: return isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled();
			case APP_ACADEMIC_CITATIONS: return isApplicationAcademicalBackgroundCitationsEnabled();
			case APP_ACADEMIC_IMPACT_FACTOR: return isApplicationAcademicalBackgroundImpactFactorEnabled();
			case APP_ACADEMIC_HFACTORY: return isApplicationAcademicalBackgroundHFactorEnabled();
			
			case APP_ACADEMIC_HIGHEST_DEGREE:
			case APP_ACADEMIC_HIGHEST_DEGREE_TYPE:
			case APP_ACADEMIC_HIGHEST_DEGREE_YEAR:
			case APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION: return isApplicationAcademicalBackgroundHighestDegreeEnabled();
			
			case APP_ACADEMIC_WORKED_IN_ACADEMIA_SINCE: return isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled();
			case APP_ACADEMIC_WORKED_OUT_ACADEMIA_SINCE: return isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled();
			case APP_ACADEMIC_WORKED_OUT_ACADEMIA_CARE_SINCE: return isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled();
			
			case APP_ACADEMIC_CAREER_DESCRIPTION: return isApplicationAcademicalBackgroundCareerDescriptionEnabled();
			case APP_ACADEMIC_DISSERTATION : return isApplicationAcademicalBackgroundDissertationEnabled();
			case APP_ACADEMIC_DISSERTATION_DATE: return isApplicationAcademicalBackgroundDissertationDateEnabled();
			case APP_ACADEMIC_DISSERTATION_TITLE: return isApplicationAcademicalBackgroundDissertationTitleEnabled();
			case APP_ACADEMIC_DISSERTATION_INSTITUTION: return isApplicationAcademicalBackgroundDissertationInstitutionEnabled();
			case APP_ACADEMIC_DISSERTATION_KEYWORD1: return isApplicationAcademicalBackgroundDissertationKeyword1Enabled();
			case APP_ACADEMIC_DISSERTATION_KEYWORD2: return isApplicationAcademicalBackgroundDissertationKeyword2Enabled();
			case APP_ACADEMIC_DISSERTATION_KEYWORD3: return isApplicationAcademicalBackgroundDissertationKeyword3Enabled();
			case APP_ACADEMIC_HABILITATION: return isApplicationAcademicalBackgroundHabilitationEnabled();
			case APP_ACADEMIC_HABILITATION_DATE: return isApplicationAcademicalBackgroundHabilitationDateEnabled();
			case APP_ACADEMIC_HABILITATION_TITLE: return isApplicationAcademicalBackgroundHabilitationTitleEnabled();
			case APP_ACADEMIC_HABILITATION_INSTITUTION: return isApplicationAcademicalBackgroundHabilitationInstitutionEnabled();
			case APP_ACADEMIC_ORCID: return isApplicationAcademicalBackgroundOrcidEnabled();
			
			//Project
			case APP_PROJECT_TITLE: return isApplicationProjectTitleEnabled();
			case APP_PROJECT_DESCRIPTION: return isApplicationProjectDescriptionEnabled();
			case APP_PROJECT_DURATION: return isApplicationProjectStartDateEnabled();
			case APP_PROJECT_START_DATE: return isApplicationProjectStartDateEnabled();
			
			case APP_PROJECT_FINANCIAL_IMPACT_1: return isApplicationProjectFinancialImpact1Enabled();
			case APP_PROJECT_FINANCIAL_IMPACT_2: return isApplicationProjectFinancialImpact2Enabled();
			case APP_PROJECT_FINANCIAL_IMPACT_3: return isApplicationProjectFinancialImpact3Enabled();
			case APP_PROJECT_FINANCIAL_IMPACT_4: return isApplicationProjectFinancialImpact4Enabled();
			case APP_PROJECT_FINANCIAL_IMPACT_5: return isApplicationProjectFinancialImpact5Enabled();
			case APP_PROJECT_ACRONYM: return isApplicationProjectAcronymEnabled();
			case APP_PROJECT_KEYWORDS: return isApplicationProjectKeywordsEnabled();
			case APP_PROJECT_DISCIPLINES: return isApplicationProjectDisciplinesEnabled();

			default: return false;
		}
	}
	
	private boolean isApplicationAddressPrivateActivated() {
		return applicationAddressPrivateOption == AddressOption.enabled
				|| applicationAddressPrivateOption == AddressOption.optional
				|| applicationAddressPrivateOption == AddressOption.xor;
	}

	private boolean isApplicationAddressBusinessActivated() {
		return applicationAddressBusinessOption == AddressOption.enabled
				|| applicationAddressBusinessOption == AddressOption.optional
				|| applicationAddressBusinessOption == AddressOption.xor;
	}
	
	public boolean isApplicationPersonTitleEnabled() {
		return "enabled".equals(applicationPersonTitle)
				|| "optional".equals(applicationPersonTitle);
	}
	
	public boolean isApplicationPersonTitleOptional() {
		return "disabled".equals(applicationPersonTitle)
				|| "optional".equals(applicationPersonTitle);
	}

	public PersonTitle[] getApplicantPersonTitles() {
		return personTitles;
	}
	
	public PersonGender[] getPersonGenders() {
		return personGenders;
	}
	
	public PersonGender getPersonDefaultGender() {
		return personDefaultGender;
	}
	
	public PersonMaritalStatus[] getMaritalStatusList() {
		return maritalStatusList;
	}
	
	public AddressOption getApplicationAddressPrivateOption() {
		return applicationAddressPrivateOption;
	}

	public AddressOption getApplicationAddressBusinessOption() {
		return applicationAddressBusinessOption;
	}

	public Country[] getPreferedCountries() {
		return preferedCountries;
	}
	
	public String[] getFeedbackOptions() {
		return feedbackOptions;
	}
	
	public boolean isPublicFeedbackEnabled() {
		return "true".equals(publicFeedback);
	}
	
	public boolean isMembersFeedbackEnabled() {
		return "true".equals(membersFeedback);
	}
	
	public RecruitingTableOption getTableFeedbacksForMembersDueDateOption() {
		return tableFeedbacksForMembersDueDateOption;
	}
	
	public boolean isPublicFeedbackFormApplicationNameEnabled() {
		return "enabled".equals(publicFeedbackFormApplicationNameEnable);
	}
	
	public boolean isPublicFeedbackFormDueDateEnabled() {
		return "enabled".equals(publicFeedbackFormDueDateEnable);
	}

	public HighestDegreeType[] getHighestDegreeTypes() {
		return highestDegreeTypes;
	}

	public boolean isApplicationPersonAcademicTitleEnabled() {
		return "enabled".equals(applicationPersonAcademicTitle)
				|| "optional".equals(applicationPersonAcademicTitle);
	}
	
	public boolean isApplicationPersonAcademicTitleOptional() {
		return "disabled".equals(applicationPersonAcademicTitle)
				|| "optional".equals(applicationPersonAcademicTitle);
	}
	
	public boolean isApplicationPersonBirthdayEnabled() {
		return "enabled".equals(applicationPersonBirthday)
				|| "optional".equals(applicationPersonBirthday);
	}
	
	public boolean isApplicationPersonBirthdayOptional() {
		return "disabled".equals(applicationPersonBirthday)
				|| "optional".equals(applicationPersonBirthday);
	}
	
	public boolean isApplicationPersonPhoneEnabled() {
		return "enabled".equals(applicationMobilePhone)
				|| "optional".equals(applicationMobilePhone);
	}
	
	public boolean isApplicationPersonPhoneOptional() {
		return "disabled".equals(applicationMobilePhone)
				|| "optional".equals(applicationMobilePhone);
	}
	
	public boolean isApplicationPersonMobilePhoneEnabled() {
		return "enabled".equals(applicationPersonMobilePhone)
				|| "optional".equals(applicationPersonMobilePhone);
	}
	
	public boolean isApplicationPersonMobilePhoneOptional() {
		return "disabled".equals(applicationPersonMobilePhone)
				|| "optional".equals(applicationPersonMobilePhone);
	}
	
	public boolean isApplicationPersonGenderEnabled() {
		return "enabled".equals(applicationPersonGender)
				|| "optional".equals(applicationPersonGender);
	}
	
	public boolean isApplicationPersonGenderOptional() {
		return "disabled".equals(applicationPersonGender)
				|| "optional".equals(applicationPersonGender);
	}
	
	public boolean isApplicationPersonMaritalStatusEnabled() {
		return "enabled".equals(applicationPersonMaritalStatus)
				|| "optional".equals(applicationPersonMaritalStatus);
	}
	
	public boolean isApplicationPersonMaritalStatusOptional() {
		return "disabled".equals(applicationPersonMaritalStatus)
				|| "optional".equals(applicationPersonMaritalStatus);
	}
	
	public boolean isApplicationPersonDisabilityEnabled() {
		return "enabled".equals(applicationPersonDisability)
				|| "optional".equals(applicationPersonDisability);
	}
	
	public boolean isApplicationPersonDisabilityOptional() {
		return "disabled".equals(applicationPersonDisability)
				|| "optional".equals(applicationPersonDisability);
	}
	
	public boolean isApplicationPersonNationalityEnabled() {
		return "enabled".equals(applicationPersonNationality)
				|| "optional".equals(applicationPersonNationality);
	}
	
	public boolean isApplicationPersonNationalityOptional() {
		return "disabled".equals(applicationPersonNationality)
				|| "optional".equals(applicationPersonNationality);
	}
	
	public boolean isApplicationPersonAdditionalNationalitiesEnabled() {
		return "enabled".equals(applicationPersonAdditionalNationalities)
				|| "optional".equals(applicationPersonAdditionalNationalities);
	}
	
	public boolean isApplicationPersonAdditionalNationalitiesOptional() {
		return "disabled".equals(applicationPersonAdditionalNationalities)
				|| "optional".equals(applicationPersonAdditionalNationalities);
	}
	
	public boolean isApplicationPersonNationalityUseCountry() {
		return "true".equals(applicationPersonNationalityUseCountry);
	}
	
	public boolean isApplicationPersonAdditionalNationalitiesUseCountry() {
		return "true".equals(applicationPersonAdditionalNationalitiesUseCountry);
	}
	
	public boolean isApplicationBusinessInformationsOrganizationEnabled() {
		return "enabled".equals(applicationBusinessInformationsOrganization)
				|| "optional".equals(applicationBusinessInformationsOrganization);
	}
	
	public boolean isApplicationBusinessInformationsOrganizationOptional() {
		return "disabled".equals(applicationBusinessInformationsOrganization)
				|| "optional".equals(applicationBusinessInformationsOrganization);
	}
	
	public List<String> getApplicationBusinessInformationsOrganizationListOfValues() {
		String[] values = applicationBusinessInformationsOrganizationListOfValues.split(",");
		List<String> listOfValues = new ArrayList<>();
		for(String value:values) {
			if(StringHelper.containsNonWhitespace(value)) {
				listOfValues.add(value);
			}
		}
		return listOfValues;
	}
	
	public boolean isApplicationBusinessInformationsUnitEnable() {
		return "enabled".equals(applicationBusinessInformationsUnit)
				|| "optional".equals(applicationBusinessInformationsUnit);
	}
	
	public boolean isApplicationBusinessInformationsUnitOptional() {
		return "disabled".equals(applicationBusinessInformationsUnit)
				|| "optional".equals(applicationBusinessInformationsUnit);
	}
	
	public boolean isApplicationBusinessInformationsCurrentPositionEnabled() {
		return "enabled".equals(applicationBusinessInformationsCurrentPosition)
				|| "optional".equals(applicationBusinessInformationsCurrentPosition);
	}
	
	public boolean isApplicationBusinessInformationsCurrentPositionOptional() {
		return "disabled".equals(applicationBusinessInformationsCurrentPosition)
				|| "optional".equals(applicationBusinessInformationsCurrentPosition);
	}
	
	public boolean isApplicationAddressLine3Enabled() {
		return "enabled".equals(applicationAddressLine3)
				|| "optional".equals(applicationAddressLine3);
	}
	
	public boolean isApplicationAddressLine3Optional() {
		return "disabled".equals(applicationAddressLine3)
				|| "optional".equals(applicationAddressLine3);
	}
	
	public boolean isApplicationBusinessMailEnabled() {
		return "enabled".equals(applicationBusinessMail)
				|| "optional".equals(applicationBusinessMail);
	}
	
	public boolean isApplicationBusinessMailOptional() {
		return "disabled".equals(applicationBusinessPhone)
				|| "optional".equals(applicationBusinessPhone);
	}
	
	public boolean isApplicationBusinessPhoneEnabled() {
		return "enabled".equals(applicationBusinessPhone)
				|| "optional".equals(applicationBusinessPhone);
	}
	
	public boolean isApplicationBusinessPhoneOptional() {
		return "disabled".equals(applicationBusinessPhone)
				|| "optional".equals(applicationBusinessPhone);
	}
	
	public boolean isApplicationAddressCountryEnabled() {
		return "enabled".equals(applicationAddressCountry)
				|| "optional".equals(applicationAddressCountry);
	}
	
	public boolean isApplicationAddressCountryOptional() {
		return "disabled".equals(applicationAddressCountry)
				|| "optional".equals(applicationAddressCountry);
	}
	
	public String getApplicationDefaultCountry() {
		return applicationDefaultCountry;
	}
	
	private boolean isApplicationAcademicalBackgroundEnabled() {
		return isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()
				|| isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()
				|| isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()
				|| isApplicationAcademicalBackgroundCitationsEnabled()
				|| isApplicationAcademicalBackgroundImpactFactorEnabled()
				|| isApplicationAcademicalBackgroundHFactorEnabled()
				|| isApplicationAcademicalBackgroundHighestDegreeEnabled()
				|| isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()
				|| isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()
				|| isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()
				|| isApplicationAcademicalBackgroundCareerDescriptionEnabled()
				|| isApplicationAcademicalBackgroundDissertationEnabled()
				|| isApplicationAcademicalBackgroundDissertationDateEnabled()
				|| isApplicationAcademicalBackgroundDissertationTitleEnabled()
				|| isApplicationAcademicalBackgroundDissertationInstitutionEnabled()
				|| isApplicationAcademicalBackgroundDissertationKeyword1Enabled()
				|| isApplicationAcademicalBackgroundDissertationKeyword2Enabled()
				|| isApplicationAcademicalBackgroundDissertationKeyword3Enabled()
				|| isApplicationAcademicalBackgroundHabilitationEnabled()
				|| isApplicationAcademicalBackgroundOrcidEnabled();
	}
	
	public boolean isApplicationAcademicalBackgroundEnabled(Position position) {
		return isPositionAcademicalBackgroundEnabled() &&
				(isApplicationAcademicalBackgroundEnabled() || getPositionMaxNumberOfAdditionalAttributes() > 0)
				&& (
					position == null
					|| !isPositionAcademicalBackgroundConfigurable()
					|| (isPositionAcademicalBackgroundConfigurable() && position.isApplicationAcademicalBackground())
				);
	}
	
	public boolean isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundNumberOfOriginalPublications)
				|| "optional".equals(applicationAcademicalBackgroundNumberOfOriginalPublications);
	}
	
	public boolean isApplicationAcademicalBackgroundNumberOfOriginalPublicationsOptional() {
		return "disabled".equals(applicationAcademicalBackgroundNumberOfOriginalPublications)
				|| "optional".equals(applicationAcademicalBackgroundNumberOfOriginalPublications);
	}
	
	public boolean isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundNumberOfFirstAuthorships)
				|| "optional".equals(applicationAcademicalBackgroundNumberOfFirstAuthorships);
	}
	
	public boolean isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsOptional() {
		return "disabled".equals(applicationAcademicalBackgroundNumberOfFirstAuthorships)
				|| "optional".equals(applicationAcademicalBackgroundNumberOfFirstAuthorships);
	}

	public boolean isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundNumberOfLastAuthorships)
				|| "optional".equals(applicationAcademicalBackgroundNumberOfLastAuthorships);
	}
	
	public boolean isApplicationAcademicalBackgroundNumberOfLastAuthorshipsOptional() {
		return "disabled".equals(applicationAcademicalBackgroundNumberOfLastAuthorships)
				|| "optional".equals(applicationAcademicalBackgroundNumberOfLastAuthorships);
	}
	
	public boolean isApplicationAcademicalBackgroundCitationsEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundCitations)
				|| "optional".equals(applicationAcademicalBackgroundCitations);
	}
	
	public boolean isApplicationAcademicalBackgroundCitationsOptional() {
		return "disabled".equals(applicationAcademicalBackgroundCitations)
				|| "optional".equals(applicationAcademicalBackgroundCitations);
	}
	
	public boolean isApplicationAcademicalBackgroundImpactFactorEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundImpactFactor)
				|| "optional".equals(applicationAcademicalBackgroundImpactFactor);
	}
	
	public boolean isApplicationAcademicalBackgroundImpactFactorOptional() {
		return "disabled".equals(applicationAcademicalBackgroundImpactFactor)
				|| "optional".equals(applicationAcademicalBackgroundImpactFactor);
	}
	
	public boolean isApplicationAcademicalBackgroundHFactorEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundHFactor)
				|| "optional".equals(applicationAcademicalBackgroundHFactor);
	}
	
	public boolean isApplicationAcademicalBackgroundHFactorOptional() {
		return "disabled".equals(applicationAcademicalBackgroundHFactor)
				|| "optional".equals(applicationAcademicalBackgroundHFactor);
	}

	public boolean isApplicationAcademicalBackgroundHighestDegreeEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundHighestDegree)
				|| "optional".equals(applicationAcademicalBackgroundHighestDegree);
	}
	
	public boolean isApplicationAcademicalBackgroundHighestDegreeOptional() {
		return "disabled".equals(applicationAcademicalBackgroundHighestDegree)
				|| "optional".equals(applicationAcademicalBackgroundHighestDegree);
	}
	
	public boolean isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundWorkedInAcademiaSince)
				|| "optional".equals(applicationAcademicalBackgroundWorkedInAcademiaSince);
	}
	
	public boolean isApplicationAcademicalBackgroundWorkedInAcademiaSinceOptional() {
		return "disabled".equals(applicationAcademicalBackgroundWorkedInAcademiaSince)
				|| "optional".equals(applicationAcademicalBackgroundWorkedInAcademiaSince);
	}
	
	public boolean isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundWorkedOutAcademiaSince)
				|| "optional".equals(applicationAcademicalBackgroundWorkedOutAcademiaSince);
	}
	
	public boolean isApplicationAcademicalBackgroundWorkedOutAcademiaSinceOptional() {
		return "disabled".equals(applicationAcademicalBackgroundWorkedOutAcademiaSince)
				|| "optional".equals(applicationAcademicalBackgroundWorkedOutAcademiaSince);
	}
	
	public boolean isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundWorkedOutAcademiaCareSince)
				|| "optional".equals(applicationAcademicalBackgroundWorkedOutAcademiaCareSince);
	}
	
	public boolean isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceOptional() {
		return "disabled".equals(applicationAcademicalBackgroundWorkedOutAcademiaCareSince)
				|| "optional".equals(applicationAcademicalBackgroundWorkedOutAcademiaCareSince);
	}
	
	public boolean isApplicationAcademicalBackgroundCareerDescriptionEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundCareerDescription)
				|| "optional".equals(applicationAcademicalBackgroundCareerDescription);
	}
	
	public boolean isApplicationAcademicalBackgroundCareerDescriptionOptional() {
		return "disabled".equals(applicationAcademicalBackgroundCareerDescription)
				|| "optional".equals(applicationAcademicalBackgroundCareerDescription);
	}
	
	public AcademicalDateFormat[] getApplicationAcademicalBackgroundDissertationDateFormat() {
		return dissertationDateFormats;
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationDateEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertationDate)
				|| "optional".equals(applicationAcademicalBackgroundDissertationDate);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationDateOptional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertationDate)
				|| "optional".equals(applicationAcademicalBackgroundDissertationDate);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationTitleEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertationTitle)
				|| "optional".equals(applicationAcademicalBackgroundDissertationTitle);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationTitleOptional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertationTitle)
				|| "optional".equals(applicationAcademicalBackgroundDissertationTitle);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationInstitutionEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertationInstitution)
				|| "optional".equals(applicationAcademicalBackgroundDissertationInstitution);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationInstitutionOptional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertationInstitution)
				|| "optional".equals(applicationAcademicalBackgroundDissertationInstitution);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationKeyword1Enabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertationKeyword1)
				|| "optional".equals(applicationAcademicalBackgroundDissertationKeyword1);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationKeyword1Optional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertationKeyword1)
				|| "optional".equals(applicationAcademicalBackgroundDissertationKeyword1);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationKeyword2Enabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertationKeyword2)
				|| "optional".equals(applicationAcademicalBackgroundDissertationKeyword2);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationKeyword2Optional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertationKeyword2)
				|| "optional".equals(applicationAcademicalBackgroundDissertationKeyword2);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationKeyword3Enabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertationKeyword3)
				|| "optional".equals(applicationAcademicalBackgroundDissertationKeyword3);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationKeyword3Optional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertationKeyword3)
				|| "optional".equals(applicationAcademicalBackgroundDissertationKeyword3);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundDissertation)
				|| "optional".equals(applicationAcademicalBackgroundDissertation);
	}
	
	public boolean isApplicationAcademicalBackgroundDissertationOptional() {
		return "disabled".equals(applicationAcademicalBackgroundDissertation)
				|| "optional".equals(applicationAcademicalBackgroundDissertation);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundHabilitation)
				|| "optional".equals(applicationAcademicalBackgroundHabilitation);
	}

	public boolean isApplicationAcademicalBackgroundHabilitationOptional() {
		return "disabled".equals(applicationAcademicalBackgroundHabilitation)
				|| "optional".equals(applicationAcademicalBackgroundHabilitation);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationDateEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundHabilitationDate)
				|| "optional".equals(applicationAcademicalBackgroundHabilitationDate);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationDateOptional() {
		return "disabled".equals(applicationAcademicalBackgroundHabilitationDate)
				|| "optional".equals(applicationAcademicalBackgroundHabilitationDate);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationTitleEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundHabilitationTitle)
				|| "optional".equals(applicationAcademicalBackgroundHabilitationTitle);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationTitleOptional() {
		return "disabled".equals(applicationAcademicalBackgroundHabilitationTitle)
				|| "optional".equals(applicationAcademicalBackgroundHabilitationTitle);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationInstitutionEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundHabilitationInstitution)
				|| "optional".equals(applicationAcademicalBackgroundHabilitationInstitution);
	}
	
	public boolean isApplicationAcademicalBackgroundHabilitationInstitutionOptional() {
		return "disabled".equals(applicationAcademicalBackgroundHabilitationInstitution)
				|| "optional".equals(applicationAcademicalBackgroundHabilitationInstitution);
	}

	public boolean isApplicationAcademicalBackgroundOrcidEnabled() {
		return "enabled".equals(applicationAcademicalBackgroundOrcid)
				|| "optional".equals(applicationAcademicalBackgroundOrcid);
	}

	public boolean isApplicationAcademicalBackgroundOrcidOptional() {
		return "disabled".equals(applicationAcademicalBackgroundOrcid)
				|| "optional".equals(applicationAcademicalBackgroundOrcid);
	}

	public boolean isApplicationProjectFinancialImpact1Enabled() {
		return "enabled".equals(applicationProjectFinancialImpact1)
				|| "optional".equals(applicationProjectFinancialImpact1);
	}

	public boolean isApplicationProjectFinancialImpact1Optional() {
		return "disabled".equals(applicationProjectFinancialImpact1)
				|| "optional".equals(applicationProjectFinancialImpact1);
	}
	
	public ApplicationFieldType getApplicationProjectFinancialImpact1Type() {
		return applicationProjectFinancialImpact1FieldType;
	}
	
	public boolean isApplicationProjectFinancialImpact2Enabled() {
		return "enabled".equals(applicationProjectFinancialImpact2)
				|| "optional".equals(applicationProjectFinancialImpact2);
	}

	public boolean isApplicationProjectFinancialImpact2Optional() {
		return "disabled".equals(applicationProjectFinancialImpact2)
				|| "optional".equals(applicationProjectFinancialImpact2);
	}
	
	public ApplicationFieldType getApplicationProjectFinancialImpact2Type() {
		return applicationProjectFinancialImpact2FieldType;
	}
	
	public boolean isApplicationProjectFinancialImpact3Enabled() {
		return "enabled".equals(applicationProjectFinancialImpact3)
				|| "optional".equals(applicationProjectFinancialImpact3);
	}

	public boolean isApplicationProjectFinancialImpact3Optional() {
		return "disabled".equals(applicationProjectFinancialImpact3)
				|| "optional".equals(applicationProjectFinancialImpact3);
	}
	
	public ApplicationFieldType getApplicationProjectFinancialImpact3Type() {
		return applicationProjectFinancialImpact3FieldType;
	}
	
	public boolean isApplicationProjectFinancialImpact4Enabled() {
		return "enabled".equals(applicationProjectFinancialImpact4)
				|| "optional".equals(applicationProjectFinancialImpact4);
	}

	public boolean isApplicationProjectFinancialImpact4Optional() {
		return "disabled".equals(applicationProjectFinancialImpact4)
				|| "optional".equals(applicationProjectFinancialImpact4);
	}
	
	public ApplicationFieldType getApplicationProjectFinancialImpact4Type() {
		return applicationProjectFinancialImpact4FieldType;
	}
	
	public boolean isApplicationProjectFinancialImpact5Enabled() {
		return "enabled".equals(applicationProjectFinancialImpact5)
				|| "optional".equals(applicationProjectFinancialImpact5);
	}

	public boolean isApplicationProjectFinancialImpact5Optional() {
		return "disabled".equals(applicationProjectFinancialImpact5)
				|| "optional".equals(applicationProjectFinancialImpact5);
	}
	
	public ApplicationFieldType getApplicationProjectFinancialImpact5Type() {
		return applicationProjectFinancialImpact5FieldType;
	}
	
	public boolean isApplicationProjectStartDateEnabled() {
		return "enabled".equals(applicationProjectStartDate)
				|| "optional".equals(applicationProjectStartDate);
	}

	public boolean isApplicationProjectStartDateOptional() {
		return "disabled".equals(applicationProjectStartDate)
				|| "optional".equals(applicationProjectStartDate);
	}
	
	public boolean isApplicationProjectDurationEnabled() {
		return "enabled".equals(applicationProjectDuration)
				|| "optional".equals(applicationProjectDuration);
	}

	public boolean isApplicationProjectDurationOptional() {
		return "disabled".equals(applicationProjectDuration)
				|| "optional".equals(applicationProjectDuration);
	}
	
	public boolean isApplicationProjectAcronymEnabled() {
		return "enabled".equals(applicationProjectAcronym)
				|| "optional".equals(applicationProjectAcronym);
	}

	public boolean isApplicationProjectAcronymOptional() {
		return "disabled".equals(applicationProjectAcronym)
				|| "optional".equals(applicationProjectAcronym);
	}
	
	public boolean isApplicationProjectKeywordsEnabled() {
		return "enabled".equals(applicationProjectKeywords)
				|| "optional".equals(applicationProjectKeywords);
	}

	public boolean isApplicationProjectKeywordsOptional() {
		return "disabled".equals(applicationProjectKeywords)
				|| "optional".equals(applicationProjectKeywords);
	}
	
	public boolean isApplicationProjectDisciplinesEnabled() {
		return "enabled".equals(applicationProjectDisciplines)
				|| "optional".equals(applicationProjectDisciplines);
	}

	public boolean isApplicationProjectDisciplinesOptional() {
		return "disabled".equals(applicationProjectDisciplines)
				|| "optional".equals(applicationProjectDisciplines);
	}
	
	public boolean isApplicationProjectDescriptionOptional() {
		return "disabled".equals(applicationProjectDescription)
				|| "optional".equals(applicationProjectDescription);
	}

	public boolean isApplicationProjectDescriptionEnabled() {
		return "enabled".equals(applicationProjectDescription)
				|| "optional".equals(applicationProjectDescription);
	}
	
	public int getApplicationProjectDescriptionMaxLength() {
		return applicationProjectDescriptionMaxLength;
	}
	
	public boolean isApplicationProjectTitleOptional() {
		return "disabled".equals(applicationProjectTitle)
				|| "optional".equals(applicationProjectTitle);
	}

	public boolean isApplicationProjectTitleEnabled() {
		return "enabled".equals(applicationProjectTitle)
				|| "optional".equals(applicationProjectTitle);
	}
	
	public boolean isApplicationPdfPageSeparatorEnabled() {
		return "enabled".equals(applicationPdfPageSeparator);
	}
	
	public boolean isReportingEnabled() {
		return "enabled".equals(reporting);
	}
	
	public boolean isDeleteAnonymous() {
		return "enabled".equals(deleteAnonymous);
	}
	
	public boolean isReportingKeepGender() {
		return "true".equals(reportingKeepGender);
	}
	
	public boolean isReportingKeepMaritalStatus() {
		return "true".equals(reportingKeepMaritalStatus);
	}
	
	public boolean isReportingKeepBirthday() {
		return "true".equals(reportingKeepBirthday);
	}
	
	public boolean isReportingKeepNationality() {
		return "true".equals(reportingKeepNationality);
	}
	
	public boolean isReportingKeepPrivateCountry() {
		return "true".equals(reportingKeepPrivateCountry);
	}
	
	public boolean isReportingKeepBusinessCountry() {
		return "true".equals(reportingKeepBusinessCountry);
	}
	
	public boolean isReportingKeepAcademicalBackgroundHighestDegreeType() {
		return "true".equals(reportingKeepAcademicalBackgroundHighestDegreeType);
	}
	
	public boolean isReportingKeepAcademicalBackgroundHighestDegreeYear() {
		return "true".equals(reportingKeepAcademicalBackgroundHighestDegreeYear);
	}
	
	public boolean isReportingKeepAcademicalBackgroundDissertationDate() {
		return "true".equals(reportingKeepAcademicalBackgroundDissertationDate);
	}
	
	public boolean isReportingKeepAcademicalBackgroundHabilitationDate() {
		return "true".equals(reportingKeepAcademicalBackgroundHabilitationDate);
	}
	
	public boolean isReportingKeepAcademicalBackgroundNumberOfOriginalPublications() {
		return "true".equals(reportingKeepAcademicalBackgroundNumberOfOriginalPublications);
	}
	
	public boolean isReportingKeepAcademicalBackgroundNumberOfFirstAuthorships() {
		return "true".equals(reportingKeepAcademicalBackgroundNumberOfFirstAuthorships);
	}
	
	public boolean isReportingKeepAcademicalBackgroundNumberOfLastAuthorships() {
		return "true".equals(reportingKeepAcademicalBackgroundNumberOfLastAuthorships);
	}
	
	public boolean isReportingKeepAcademicalBackgroundCitations() {
		return "true".equals(reportingKeepAcademicalBackgroundCitations);
	}
	
	public boolean isReportingKeepAcademicalBackgroundImpactFactor() {
		return "true".equals(reportingKeepAcademicalBackgroundImpactFactor);
	}
	
	public boolean isReportingKeepAcademicalBackgroundHFactor() {
		return "true".equals(reportingKeepAcademicalBackgroundHFactor);
	}
	
	public boolean isReportingKeepProjectStartDate() {
		return "true".equals(reportingKeepProjectStartDate);
	}
	
	public boolean isReportingKeepProjectFinancialImpact1() {
		return "true".equals(reportingKeepProjectFinancialImpact1);
	}
	
	public boolean isReportingKeepProjectFinancialImpact2() {
		return "true".equals(reportingKeepProjectFinancialImpact2);
	}
	
	public boolean isReportingKeepProjectFinancialImpact3() {
		return "true".equals(reportingKeepProjectFinancialImpact3);
	}
	
	public boolean isReportingKeepProjectFinancialImpact4() {
		return "true".equals(reportingKeepProjectFinancialImpact4);
	}
	
	public boolean isReportingKeepProjectFinancialImpact5() {
		return "true".equals(reportingKeepProjectFinancialImpact5);
	}
	
	public boolean isReportingKeepApplicationStatus() {
		return "true".equals(reportingKeepApplicationStatus);
	}

	public boolean isReportingKeepApplicationDecision() {
		return "true".equals(reportingKeepApplicationDecision);
	}
	
	public boolean isReportingKeepApplicationSubmissionDate() {
		return "true".equals(reportingKeepApplicationSubmissionDate);
	}
	
	public boolean isReportingKeepApplicationRatingsA() {
		return "true".equals(reportingKeepApplicationRatingsA);
	}
	
	public boolean isReportingKeepApplicationRatingsB() {
		return "true".equals(reportingKeepApplicationRatingsB);
	}
	
	public boolean isReportingKeepApplicationRatingsC() {
		return "true".equals(reportingKeepApplicationRatingsC);
	}
	
	public boolean isReportingKeepApplicationRatingsAbstentions() {
		return "true".equals(reportingKeepApplicationRatingsAbstentions);
	}
	
	public boolean isReportingKeepApplicationNumReferees() {
		return "true".equals(reportingKeepApplicationNumReferees);
	}
	
	public boolean isReportingKeepApplicationNumRefereesDocuments() {
		return "true".equals(reportingKeepApplicationNumRefereesDocuments);
	}
	
	public boolean isReportingKeepApplicationNumExperts() {
		return "true".equals(reportingKeepApplicationNumExperts);
	}
	
	public boolean isReportingKeepApplicationNumExpertsDocuments() {
		return "true".equals(reportingKeepApplicationNumExpertsDocuments);
	}
	
	public boolean isReportingKeepApplicationNumComparativeExperts() {
		return "true".equals(reportingKeepApplicationNumComparativeExperts);
	}
	
	public boolean isReportingKeepApplicationNumComparativeExpertsDocuments() {
		return "true".equals(reportingKeepApplicationNumComparativeExpertsDocuments);
	}
	
	public boolean isReportingKeepApplicationSystemTags() {
		return "true".equals(reportingKeepApplicationSystemTags);
	}
	
	public boolean isReportingKeepPositionTitle() {
		return "true".equals(reportingKeepPositionTitle);
	}
	
	public boolean isReportingKeepPositionShortTitle() {
		return "true".equals(reportingKeepPositionShortTitle);
	}
	
	public boolean isReportingKeepPositionPlanningId() {
		return "true".equals(reportingKeepPositionPlanningId);
	}
	
	public boolean isReportingKeepPositionOrgUnit() {
		return "true".equals(reportingKeepPositionOrgUnit);
	}
	
	public boolean isReportingKeepPositionDepartment() {
		return "true".equals(reportingKeepPositionDepartment);
	}
	
	public boolean isReportingKeepPositionApplicationDeadline() {
		return "true".equals(reportingKeepPositionApplicationDeadline);
	}
	
	public boolean isReportingKeepPositionRatingDeadline() {
		return "true".equals(reportingKeepPositionRatingDeadline);
	}
	
	public boolean isReportingKeepCommitteeRole() {
		return "true".equals(reportingKeepCommitteeRole);
	}
	
	public boolean isReportingKeepCommitteeRatingRights() {
		return "true".equals(reportingKeepCommitteeRatingRights);
	}
	
	public boolean isReportingKeepCommitteeGender() {
		return "true".equals(reportingKeepCommitteeGender);
	}
	
	public boolean isReportingKeepCommitteeUserClassification() {
		return "true".equals(reportingKeepCommitteeUserClassification);
	}
	
	public boolean isReportingKeepCommitteeNumberRatingsA() {
		return "true".equals(reportingKeepCommitteeNumberRatingsA);
	}
	
	public boolean isReportingKeepCommitteeNumberRatingsB() {
		return "true".equals(reportingKeepCommitteeNumberRatingsB);
	}
	
	public boolean isReportingKeepCommitteeNumberRatingsC() {
		return "true".equals(reportingKeepCommitteeNumberRatingsC);
	}
	
	public boolean isReportingKeepCommitteeNumberRatingsAbstentions() {
		return "true".equals(reportingKeepCommitteeNumberRatingsAbstentions);
	}
	
	public List<DecisionRubricSPI> getDecisionRubricSpies() {
		return new ArrayList<>(decisionRubricSpies);
	}

	public String getAboutUrl() {
		return aboutUrl;
	}

	public void setAboutUrl(String aboutUrl) {
		this.aboutUrl = aboutUrl;
	}
}