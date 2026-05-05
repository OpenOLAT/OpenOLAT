/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import static org.olat.modules.selectus.ui.RecruitingHelper.getPositionDerivedFilename;
import static org.olat.modules.selectus.ui.RecruitingHelper.normalizeFilename;
import static org.olat.modules.selectus.ui.events.SelectPositionLightEvent.SELECT_POSITION;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.RejectionEmailLogFull;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.AcademicalDateFormat;
import org.olat.modules.selectus.ui.FOPTableExport;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.AcademicalDateCellRenderer;
import org.olat.modules.selectus.ui.components.CategoriesCellRenderer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DecisionCellRenderer;
import org.olat.modules.selectus.ui.components.DisabilityCellRenderer;
import org.olat.modules.selectus.ui.components.GenderCellRenderer;
import org.olat.modules.selectus.ui.components.LanguageCellRenderer;
import org.olat.modules.selectus.ui.components.LogStatusCellRenderer;
import org.olat.modules.selectus.ui.components.MultiRatingCellRenderer;
import org.olat.modules.selectus.ui.components.TooltipCellRenderer;
import org.olat.modules.selectus.ui.events.DecisionEvent;
import org.olat.modules.selectus.ui.rejection.PositionMailCenterDataModel.Fields;
import org.olat.modules.selectus.ui.resources.FOPMediaResource;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailCenterController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private PositionMailCenterDataModel tableModel;
	private FormLink sendMailLink;
	private FormLink exportLink;
	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	
	private final ApplicationAttributesDelegate projectAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	private final ApplicationAttributesDelegate personalDataAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	private final ApplicationAttributesDelegate academicalBackgroundAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	
	private CloseableModalController cmc;
	private PositionViewEmailLogController viewEmailLogCtrl;
	private PositionRejectionResendController resendController;
	private StepsMainRunController decisionWizardController;
	private StepsMainRunController sendMailWizardController;
	private AttachmentWarningController attachmentWarningCtrl;
	
	private static final String COLUMN_PREFS = "rejAppsListRev5";

	private Position position;
	private final AddressOption privateOption;
	private final AddressOption businessOption;
	private List<PositionMailTemplate> mailTemplates;
	private final RecruitingPositionSecurityCallback secCallback;
	private final List<String> excludedAttributesList;
	
	@Autowired
	private MailService mailService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionMailCenterController(UserRequest ureq, WindowControl wControl, Position position,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "rejection_main");
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		
		this.position = position;
		this.secCallback = secCallback;
		mailTemplates = mailService.getTemplates(position);
		privateOption = recruitingModule.getApplicationAddressPrivateOption();
		businessOption = recruitingModule.getApplicationAddressBusinessOption();
		excludedAttributesList = position.getExcludedAttributesList();
		
		initForm(ureq);
		loadModel(position);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;
		mailTemplates = mailService.getTemplates(position);
		tableModel.setMailTemplates(mailTemplates);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		sendMailLink = uifactory.addFormLink("rejection.sendmails", formLayout, Link.BUTTON);
		sendMailLink.setElementCssClass("o_sel_rejection_sendmails");
		exportLink = uifactory.addFormLink("rejection.export.log", formLayout, Link.BUTTON);
		exportLink.setVisible(secCallback.canMailCenterExportLog());

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.id, SELECT_POSITION));//EscapeMode.antisamy
		initColumnsModelApplicant();
		initColumnsAdditionalPersonalData();
		if(recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			initColumnsModelAcademicalBackground();
		}
		if(position.isApplicationProject()) {
			initColumnsModelProject();
		}
		initColumnsModelCategories();

		//ratings
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.committeeRating, new MultiRatingCellRenderer(getTranslator(), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.template));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.decision, new DecisionCellRenderer()));

		initColumnsModelAddress();
		initColumnsModelOrganization();
		initColumnsModelBusinessAddress();
		initColumnsModelPrivateAddress();
		initColumnsModelStaffInfos();
		
		if(secCallback.canMailCenterViewEmail()) {
			StaticFlexiCellRenderer viewRenderer = new StaticFlexiCellRenderer(translate("rejection.quick.view"), "view");
			viewRenderer.setIconLeftCSS("o_icon o_icon_quickview");
			DefaultFlexiColumnModel viewColDesc = new DefaultFlexiColumnModel("table.header.action", Fields.quickView.ordinal(), "view",
					new BooleanCellRenderer(viewRenderer, null));
			viewColDesc.setAlwaysVisible(true);
			viewColDesc.setExportable(false);
			columnsModel.addFlexiColumnModel(viewColDesc);
		}
		
		if(secCallback.canMailCenterResendEmail()) {
			StaticFlexiCellRenderer resendRenderer = new StaticFlexiCellRenderer(translate("rejection.resend"), "resend");
			resendRenderer.setIconLeftCSS("o_icon o_icon_mail");
			DefaultFlexiColumnModel resendColDesc = new DefaultFlexiColumnModel("table.header.action", Fields.resendMail.ordinal(), "resend",
					new BooleanCellRenderer(resendRenderer, null));
			resendColDesc.setAlwaysVisible(true);
			resendColDesc.setExportable(false);
			columnsModel.addFlexiColumnModel(resendColDesc);
		}
		
		tableModel = new PositionMailCenterDataModel(getTranslator(), columnsModel, mailTemplates);
		tableEl = uifactory.addTableElement(getWindowControl(), "log", tableModel, 40, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("rejection.table.log.empty")
				.build());
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(Fields.logCreationDate.name(), false));
		tableEl.setAndLoadPersistedPreferences(ureq, COLUMN_PREFS);
	}
	
	private void initColumnsModelApplicant() {
		initColumnModel(Fields.title, recruitingModule.getTableMailApplicationTitleOption());
		initColumnModel(Fields.firstName, recruitingModule.getTableMailApplicationFirstNameOption());
		initColumnModel(Fields.lastName, recruitingModule.getTableMailApplicationLastNameOption());
		initColumnModel(Fields.gender, recruitingModule.getTableApplicationsGenderOption(), new GenderCellRenderer());
		
		if(recruitingModule.isApplicationPersonMaritalStatusEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.maritalStatus, SELECT_POSITION));
		}
		
		if(recruitingModule.getPositionLocales().length > 1) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.language, new LanguageCellRenderer()));
		}
		initColumnModel(Fields.yearOfBirth, recruitingModule.getTableApplicationsYearOfBirthOption());
		initColumnModel(Fields.birthday, recruitingModule.getTableApplicationsBirthdayOption(), new DateCellRenderer());
		
		if(recruitingModule.isApplicationPersonAcademicTitleEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.academicTitle));
		}
	}
	
	private void initColumnsAdditionalPersonalData() {
		personalDataAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale(), null);
	}
	
	private void initColumnsModelAcademicalBackground() {
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.numberOfOriginalPublications));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.numberOfFirstAuthorships));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.numberOfLastAuthorships));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.citations));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.impactFactor));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.hFactor));
		}

		//degree
		if(recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
			
			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.highestDegreeType));
			}

			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)) {
				RecruitingTableOption highestDegreeYearOption = recruitingModule.getTableApplicationsHighestDegreeYearOption();
				if(!highestDegreeYearOption.isDisabled()) {
					if(recruitingModule.isTableApplicationsHighestDegreeYearOnlyPhDOption()) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.highestDegreeYearPhD,
								new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
					} else {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.highestDegreeYear,
								new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
					}
				}
			}

			if(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.highestDegreeInstitution));
			}
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.workedInAcademiaSince));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.workedOutAcademiaSince));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.workedOutAcademiaCareSince));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.careerDescription));
		}

		//dissertation
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.dissertationTitle));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {	
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.dissertationDate,
					new AcademicalDateCellRenderer(recruitingModule.getApplicationAcademicalBackgroundDissertationDateFormat(), getLocale())));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.dissertationInstitution));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.dissertationKeyword1));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.dissertationKeyword2));
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.dissertationKeyword3));
		}
			
		//habilitation
		if(recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.habilitationTitle));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.habilitationDate,
					new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.habilitationInstitution));
		}
		//orcid
		if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.orcid));
		}
		
		academicalBackgroundAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale(), null);
	}
	
	private void initColumnsModelAddress() {
		initOptionalColumnModel(Fields.nationality, recruitingModule.getTableApplicationsNationalityOption());
		initOptionalColumnModel(Fields.additionalNationalities, recruitingModule.getTableApplicationsAdditionalNationalitiesOption());
		initOptionalColumnModel(Fields.mail, recruitingModule.getTableApplicationsPhoneOption());
		initOptionalColumnModel(Fields.phone, recruitingModule.getTableApplicationsPhoneOption());
		initOptionalColumnModel(Fields.mobilePhone, recruitingModule.getTableApplicationsMobilePhoneOption());
		initOptionalColumnModel(Fields.disability, recruitingModule.getTableApplicationsDisabilityOption(), new DisabilityCellRenderer());
	}
	
	private void initColumnsModelOrganization() {
		initOptionalColumnModel(Fields.organization, recruitingModule.getTableApplicationsOrganizationOption());
		initOptionalColumnModel(Fields.unit, recruitingModule.getTableApplicationsOrganizationUnitOption());
		initOptionalColumnModel(Fields.currentPosition, recruitingModule.getTableApplicationsOrganizationCurrentPositionOption());
	}
	
	private void initColumnsModelBusinessAddress() {
		if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
			initOptionalColumnModel(Fields.businessAddressLine1, recruitingModule.getTableApplicationsBusinessAddressLinesOption());
			initOptionalColumnModel(Fields.businessAddressLine2, recruitingModule.getTableApplicationsBusinessAddressLinesOption());
			initOptionalColumnModel(Fields.businessAddressLine3, recruitingModule.getTableApplicationsBusinessAddressLinesOption());
			initOptionalColumnModel(Fields.businessZipcode, recruitingModule.getTableApplicationsBusinessZipcodeOption());
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.businessCity));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.businessCountry));
			if(recruitingModule.isApplicationBusinessPhoneEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.businessPhone));
			}
			if(recruitingModule.isApplicationBusinessMailEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.businessMail));
			}
		}
	}
	
	private void initColumnsModelPrivateAddress() {
		if(!AddressOption.disabled.equals(privateOption)) {
			initOptionalColumnModel(Fields.addressLine1, recruitingModule.getTableApplicationsAddressLinesOption());
			initOptionalColumnModel(Fields.addressLine2, recruitingModule.getTableApplicationsAddressLinesOption());
			initOptionalColumnModel(Fields.addressLine3, recruitingModule.getTableApplicationsAddressLinesOption());
			initOptionalColumnModel(Fields.zipcode, recruitingModule.getTableApplicationsZipcodeOption());
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.city));
			if (recruitingModule.isApplicationAddressCountryEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.country));
			}
		}
	}
	
	private void initColumnsModelStaffInfos() {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.ad));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.submittedByStaff));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.applicationStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.applicationStatusDate, new DateCellRenderer()));
		initColumnModel(Fields.logStatus, recruitingModule.getTableMailEmailLogStatusOption(), new LogStatusCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.logCreationDate, new DateCellRenderer()));
	}
	
	private void initColumnsModelProject() {
		initColumnModel(Fields.projectTitle, recruitingModule.getTableMailProjectTitleOption());
		initColumnModel(Fields.projectAcronym, recruitingModule.getTableMailProjectAcronymOption());
		initColumnModel(Fields.projectKeywords, recruitingModule.getTableMailProjectKeywordsOption());
		initColumnModel(Fields.projectDisciplines, recruitingModule.getTableMailProjectDisciplinesOption());
		initColumnModel(Fields.projectStartDate, recruitingModule.getTableMailProjectStartDateOption(), new DateCellRenderer());
		initColumnModel(Fields.projectDuration, recruitingModule.getTableMailProjectDurationOption());
		initColumnModel(Fields.projectFinancialImpact1, recruitingModule.getTableMailProjectFinancialImpact1Option());
		initColumnModel(Fields.projectFinancialImpact2, recruitingModule.getTableMailProjectFinancialImpact2Option());
		initColumnModel(Fields.projectFinancialImpact3, recruitingModule.getTableMailProjectFinancialImpact3Option());
		initColumnModel(Fields.projectFinancialImpact4, recruitingModule.getTableMailProjectFinancialImpact4Option());
		initColumnModel(Fields.projectFinancialImpact5, recruitingModule.getTableMailProjectFinancialImpact5Option());
		initColumnModel(Fields.projectDescription, recruitingModule.getTableMailProjectDescriptionOption(), new TooltipCellRenderer("o_icon_project_description"));
		
		projectAttributesDelegate.initColumnsModel(columnsModel, position, null, getLocale(), null);
	}

	private void initColumnsModelCategories() {
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.categories, new CategoriesCellRenderer()));
		}
	}
	
	private void initColumnModel(Fields field, RecruitingTableOption option) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field));
		}
	}
	
	private void initColumnModel(Fields field, RecruitingTableOption option, FlexiCellRenderer renderer) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field, renderer));
		}
	}
	
	private void initOptionalColumnModel(Fields field, RecruitingTableOption option) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, field));
		}
	}
	
	private void initOptionalColumnModel(Fields field, RecruitingTableOption option, FlexiCellRenderer renderer) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, field, renderer));
		}
	}
	
	public void loadModel(Position updatePosition) {
		this.position = updatePosition;
		
		List<MailLogInfos> log = recruitingService.getMailLog(updatePosition);
		//infos and buttons
		String message;
		if(!log.isEmpty()) {
			message = translate("rejection.info.send");
		} else {
			message = translate("rejection.info.nosend");
		}
		flc.contextPut("message", message);


		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<IdentityRef> committee = recruitingService.getCommitteeRefs(updatePosition, ratingRoles);
		List<UserRating> ratings = recruitingService.getRatings(updatePosition, committee);
		Map<Long, List<ApplicationCategoryInfos>> appToCategories = taggingService.getApplicationToCategories(updatePosition,
				secCallback.canSeeApplicationAdministrativeCategories());
		tableModel.setObjects(log, ratings, committee, appToCategories);
		tableEl.reset(true, true, true);
		exportLink.setEnabled(!log.isEmpty());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sendMailLink == source) {
			startSendEmail(ureq);
		} else if(exportLink == source) {
			exportLog(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("resend".equals(se.getCommand())) {
					doResend(ureq, tableModel.getObject(se.getIndex()));
				} else if("view".equals(se.getCommand())) {
					doView(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(decisionWizardController == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				fireEvent(ureq, new DecisionEvent());
			}
			removeAsListenerAndDispose(decisionWizardController);
			decisionWizardController = null;
		} else if(sendMailWizardController == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				//reload the list
				loadModel(position);
				
				@SuppressWarnings("unchecked")
				Map<ApplicationLight, MailerResult> mailerResults = (Map<ApplicationLight, MailerResult>)sendMailWizardController.getRunContext().get("mailerResults");
				Boolean asyncMailer = (Boolean)sendMailWizardController.getRunContext().get("asyncMailer");
				analyseResults(asyncMailer, mailerResults);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			removeAsListenerAndDispose(sendMailWizardController);
			sendMailWizardController = null;
		} else if(attachmentWarningCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				loadModel(position);
				cmc.deactivate();
				cleanUp();
			}
		} else if(resendController == source) {
			if(event == Event.DONE_EVENT) {
				RejectionEmailLogFull log = resendController.getLog();
				doSend(log, resendController.getTemplate(), resendController.isRefreshLetter());
			}
			cmc.deactivate();
			cleanUp();
		} else if(viewEmailLogCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} 

		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(attachmentWarningCtrl);
		removeAsListenerAndDispose(viewEmailLogCtrl);
		removeAsListenerAndDispose(resendController);
		removeAsListenerAndDispose(cmc);
		attachmentWarningCtrl = null;
		viewEmailLogCtrl = null;
		resendController = null;
		cmc = null;
	}
	
	private void startSendEmail(UserRequest ureq) {
		final EmailVariables emailVar = mailService.getEmailVariables(position, getLocale());
		emailVar.setShowAttachmentWarning(true);

		if(recruitingModule.isRejectionAllDecisionsStepEnabled()) {
			Step start = new CEmail_0_FilterStep(ureq, emailVar, secCallback);
			startSendEmail(ureq, emailVar, start);
		} else {
			List<ApplicationLight> apps = recruitingService.getApplicationsWithCDecision(position);
			if(apps.isEmpty()) {
				showWarning("rejection.no.application.tosendemail");
			} else {
				emailVar.setRows(apps);
			}
			Step start = new CEmail_1_SelectStep(ureq, emailVar, secCallback, true);
			startSendEmail(ureq, emailVar, start);
		}
	}

	private void startSendEmail(UserRequest ureq, final EmailVariables emailVar, Step start) {
		removeAsListenerAndDispose(sendMailWizardController);
		StepRunnerCallback finish =  new SendEmailRunnerCallback(getIdentity(), emailVar, secCallback, getTranslator());

		sendMailWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("rejection.wizard.title"), null);
		listenTo(sendMailWizardController);
		
		getWindowControl().pushAsModalDialog(sendMailWizardController.getInitialComponent());
	}
	
	private void analyseResults(Boolean asyncMailer, Map<ApplicationLight, MailerResult> mailerResults) {
		if(asyncMailer != null && asyncMailer.booleanValue()) {
			showInfo("rejection.mail.sent");
			return;
		}

		int countError = 0;
		for(MailerResult result:mailerResults.values()) {
			if(result.getReturnCode() != MailerResult.OK) {
				countError++;
			}
		}
		
		if(countError == 0) {
			showInfo("rejection.mail.send.success");
		} else {
			StringBuilder sb = new StringBuilder();
			MailerResult aggregated = new MailerResult();
			for(Map.Entry<ApplicationLight, MailerResult> entry:mailerResults.entrySet()) {
				MailerResult result = entry.getValue();
				if(result.getReturnCode() != MailerResult.OK) {
					ApplicationLight app = entry.getKey();
					String mail = app.getPerson().getMail();
					if(sb.length() > 0) sb.append(", ");
					sb.append(mail);
					aggregated.append(result);
				}
			}
			if(aggregated.getFailedAddresses().isEmpty()) {
				showError("rejection.mail.send.error", sb.toString());
			} else {
				String error = aggregated.getFailedAddresses().size() == 1 ?"rejection.mail.send.invalid.address" : "rejection.mail.send.invalid.addresses";
				showError(error, new String[] { sb.toString(), aggregated.failedAddressesToString() });
			}
		}
	}
	
	private void doView(UserRequest ureq, MailLogInfos log) {
		if(guardModalController(viewEmailLogCtrl)) return;
		
		viewEmailLogCtrl = new PositionViewEmailLogController(ureq, getWindowControl(), log.getMailLog());
		listenTo(viewEmailLogCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "c", viewEmailLogCtrl.getInitialComponent(), translate("rejection.quick.view"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doResend(UserRequest ureq, MailLogInfos log) {
		if(resendController != null) return;
		
		String language = log.getApplication().getLanguage();
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		Identity secretary = recruitingService.getSecretary(position);
		Locale templateLocale = recruitingModule.getPositionLocale(language);
		Translator translator = Util.createPackageTranslator(PositionMailCenterController.class, templateLocale,
				Util.createPackageTranslator(PositionController.class, templateLocale));
		
		String templateName = log.getMailLog().getMailTemplate();
		if(!StringHelper.containsNonWhitespace(templateName)) {
			templateName = ApplicationMailTemplate.DEFAULT_TEMPLATE;
		}	
		SubjectAndBody subjectAndBody = mailService.rejectionTemplate(position, templateName, headOfCommittee, templateLocale);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, templateName, templateName,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = recruitingService.createMailSender()
				.createWithContext(log.getApplication(), null, null, null, null, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, templateName, templateName,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), subjectAndBody2.getLetter(),
					headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		}

		resendController = new PositionRejectionResendController(ureq, getWindowControl(), log.getMailLog(), template);
		listenTo(resendController);
		cmc = new CloseableModalController(getWindowControl(), "c", resendController.getInitialComponent(), translate("rejection.resend.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doSend(RejectionEmailLogFull log, ApplicationMailTemplate template, boolean refreshLetter) {
		ApplicationLight cEmailApp = log.getApplication();
		MailerResult result = new MailerResult();
		if(!refreshLetter && template.getLetterTemplate() != null && log.getLetter() != null) {
			Attachment attachment = log.getLetter();
			template.setLetterTemplate(MailAttachment.valueOf(attachment));
		}
		
		recruitingService.sendRejectionMail(position, cEmailApp, template, result);
		if(result.getReturnCode() == MailerResult.OK) {
			showInfo("rejection.mail.send.success");
		} else if(result.getReturnCode() == MailerResult.RECIPIENT_ADDRESS_ERROR) {
			String error = result.getFailedAddresses().size() == 1 ?"rejection.mail.send.invalid.address" : "rejection.mail.send.invalid.addresses";
			showError(error, new String[] { cEmailApp.getPerson().getMail(), result.failedAddressesToString() });
		} else {
			showError("rejection.mail.send.error", cEmailApp.getPerson().getMail());
		}
	}
	
	private void exportLog(UserRequest ureq) {
		List<MailLogInfos> rejectionLog = recruitingService.getMailLog(position);
		PositionRejectionEmailPdfDataModel model = new PositionRejectionEmailPdfDataModel(rejectionLog, getTranslator());
		FOPMediaResource resource =  new FOPTableExport().exportRejectionLog(getIdentity(), position, model, getLocale());
		
		String derivedFilename = getPositionDerivedFilename(position, getLocale());
		String date = Formatter.formatShortDateFilesystem(new Date());
		resource.setFilename(normalizeFilename(derivedFilename) + "_crejection_" + date);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}