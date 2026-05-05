/**
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import static org.olat.modules.selectus.ui.RecruitingHelper.formatFullName;
import static org.olat.modules.selectus.ui.events.SelectPositionLightEvent.SELECT_POSITION;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.ApplicationFieldType;
import org.olat.modules.selectus.ApplicationFieldType.Type;
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AssignmentMethods;
import org.olat.modules.selectus.AssignmentService;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.ParallelApplicationScope;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableContextualOption;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.RatingClosedException;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.EmptyUserRating;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.application.ApplicationRefImpl;
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.model.assignment.AssignmentKey;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.model.mail.PositionMailTemplateRef;
import org.olat.modules.selectus.model.mail.SentEmailTemplates;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionApplicationsDataModel.Fields;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.application.ApplicationEditController;
import org.olat.modules.selectus.ui.application.ApplicationOverviewController;
import org.olat.modules.selectus.ui.application.BatchApplicationCategoriesController;
import org.olat.modules.selectus.ui.application.BatchApplicationStatusController;
import org.olat.modules.selectus.ui.application.GenerateApplicationListController;
import org.olat.modules.selectus.ui.committee.assignment.AddAssignment1CommitteeStep;
import org.olat.modules.selectus.ui.committee.assignment.AddAssignmentStepCallback;
import org.olat.modules.selectus.ui.committee.assignment.AssignmentsData;
import org.olat.modules.selectus.ui.committee.assignment.RemoveAssignment1CommitteeStep;
import org.olat.modules.selectus.ui.committee.assignment.RemoveAssignmentStepCallback;
import org.olat.modules.selectus.ui.comparator.AppToCategoryComparator;
import org.olat.modules.selectus.ui.comparator.IdentityLastnameComparator;
import org.olat.modules.selectus.ui.components.AcademicalDateCellRenderer;
import org.olat.modules.selectus.ui.components.ApplicationURLCellRenderer;
import org.olat.modules.selectus.ui.components.AssignmentsCellRenderer;
import org.olat.modules.selectus.ui.components.BarItem;
import org.olat.modules.selectus.ui.components.CategoriesCellRenderer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DateTimeCellRenderer;
import org.olat.modules.selectus.ui.components.DecisionCellRenderer;
import org.olat.modules.selectus.ui.components.DisabilityCellRenderer;
import org.olat.modules.selectus.ui.components.GenderCellRenderer;
import org.olat.modules.selectus.ui.components.NotesCellRenderer;
import org.olat.modules.selectus.ui.components.ParallelApplicationsCellRenderer;
import org.olat.modules.selectus.ui.components.ProjectCellRenderer;
import org.olat.modules.selectus.ui.components.ReferencesStatsCellRenderer;
import org.olat.modules.selectus.ui.components.SingleRatingCellRenderer;
import org.olat.modules.selectus.ui.components.StackedProgressBarItem;
import org.olat.modules.selectus.ui.components.TooltipCellRenderer;
import org.olat.modules.selectus.ui.copy.CopyApplication1PositionStep;
import org.olat.modules.selectus.ui.copy.CopyApplicationContext;
import org.olat.modules.selectus.ui.copy.CopyApplicationFinishCallback;
import org.olat.modules.selectus.ui.events.ApplicationChangeEvent;
import org.olat.modules.selectus.ui.events.DecisionEvent;
import org.olat.modules.selectus.ui.events.FinalDecisionChangeEvent;
import org.olat.modules.selectus.ui.events.PositionApplicationEvent;
import org.olat.modules.selectus.ui.events.PushControllerEvent;
import org.olat.modules.selectus.ui.events.RatingChangedEvent;
import org.olat.modules.selectus.ui.events.UpdateControllerEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.AddFeedbacksMemberFinishCallback;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.Contact0FilterStep;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.ContactFeedbacksMemberFinishCallback;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.ContactMembersContext;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.Feedback1EmailStep;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.FeedbackMembersContext;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.Remove1SelectStep;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.RemoveFeedbacksMemberFinishCallback;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.RemoveMembersContext;
import org.olat.modules.selectus.ui.model.AppToCategory;
import org.olat.modules.selectus.ui.model.ApplicationAssignmentLightTransient;
import org.olat.modules.selectus.ui.model.ApplicationRow;
import org.olat.modules.selectus.ui.model.ApplicationStatistics;
import org.olat.modules.selectus.ui.rating.CustomRatingFormItem;
import org.olat.modules.selectus.ui.rating.RatingComparator;
import org.olat.modules.selectus.ui.rating.RatingsOverviewFormItem;
import org.olat.modules.selectus.ui.reference.ApplicationReferenceEditController;
import org.olat.modules.selectus.ui.reference.InvitationEmail_0_FilterStep;
import org.olat.modules.selectus.ui.reference.SendReferencesEmailRunnerCallback;
import org.olat.modules.selectus.ui.rejection.CEmail_2_OverviewStep;
import org.olat.modules.selectus.ui.rejection.SendEmailRunnerCallback;
import org.olat.modules.selectus.ui.rejection.TemplateForEmailController;
import org.olat.modules.selectus.ui.review.ReviewEditController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionApplicationsController extends FormBasicController implements FlexiTableCssDelegate, GenericEventListener, Activateable2 {

	protected static final String FILTER_ASSIGNEE = "fassignee";
	protected static final String FILTER_MY_RATING = "myRating";
	protected static final String FILTER_WITH_SENT_EMAILS = "fwithSentEmails";
	protected static final String FILTER_WITHOUT_SENT_EMAILS = "fwithoutSentEmails";

	protected static final String FILTER_NULL_KEY = "NULL";
	protected static final String FILTER_ABSTAIN_KEY = "ABSTAIN";
	
	private final RatingComparator ratingComparator = new RatingComparator();
	
	private boolean listChanged = false;
	private Position position;
	private final OLATResourceable positionOres;
	private List<ApplicationLight> applications;
	private final Translator mailTranslator;
	private final List<Tab> customTabs;

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab myAssignmentsTab;
	private FlexiFiltersTab withoutSentEmailTab;
	private FlexiFiltersTab withoutSentCEmailTab;
	private FlexiFiltersTab applicationActiveTab;
	
	private final ApplicationAttributesDelegate projectAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	private final ApplicationAttributesDelegate personalDataAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	private final ApplicationAttributesDelegate academicalBackgroundAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	private final List<ApplicationAttributesDelegate> customTabsAttributesDelegate
		= new ArrayList<>();
	
	private FormLink generateListButton;
	private FormLink batchDecisionButton;
	private FormLink batchSendReferencesEmailsButton;
	private FormLink batchSendApplicationEmailsButton;
	private FormLink batchCategoriesButton;
	private FormLink batchStatusButton;
	private FormLink addAssignmentButton;
	private FormLink removeAssignmentButton;
	private FormLink toggleAssignmentStatisticsLink;
	private FormLink batchAddFeedbackMembersButton;
	private FormLink batchContactFeedbackMembersButton;
	private FormLink batchRemoveFeedbackMembersButton;
	private FormLink copyApplicationsButton;
	private FormLink addComparativeExpertsButton;
	
	private StackedProgressBarItem myStatisticsItem;
	private StackedProgressBarItem allStatisticsItem;
	private FormLayoutContainer statisticsContainer;
	
	private Link exportAllCombinedPdfLink;
	private final TooledStackedPanel stackPanel;
	private FlexiTableElement tableEl;
	private PositionApplicationsDataModel applicationsDataModel;

	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	
	private CloseableModalController cmc;
	private NotesController notesController;
	private ReviewEditController editReviewCtrl;
	private ApplicationOverviewController appController;
	private DialogBoxController confirmDeleteBox;
	private CloseableModalController notesDialogBox;
	private BatchDecisionController batchDecisionCtrl;
	private ApplicationEditController editApplicationCtrl;
	private CloseableModalController editApplicationDialogBox;
	private GenerateApplicationListController generateListCtrl;
	private StepsMainRunController copyApplicationsWizard;
	private StepsMainRunController addMembersFeeadbackWizard;
	private StepsMainRunController addAssignmentWizardController;
	private StepsMainRunController removeAssignmentWizardController;
	private StepsMainRunController sendReferencesEmailController;
	private StepsMainRunController sendApplicationEmailController;
	private StepsMainRunController contactMembersFeeadbackWizard;
	private StepsMainRunController removeMembersFeeadbackWizard;
	private BatchApplicationStatusController batchApplicationStatusCtrl;
	private BatchApplicationCategoriesController batchAddCategoriesCtrl;
	private ApplicationReferenceEditController addComparativeExpertCtrl;
	
	private final RecruitingPositionSecurityCallback secCallback;
	private final List<String> excludedAttributesList;
	private static final String COLUMN_PREFS = "posAppsListRev16-";
	
	private final AddressOption privateOption;
	private final AddressOption businessOption;
	/**
	 * only loaded on demand
	 */
	private List<Identity> committeeAssignees; 
	private List<PositionMailTemplateRef> mailTemplates;
	
	@Autowired
	private MailService mailService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private AssignmentService assignmentService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionApplicationsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "applications");
		
		this.position = position;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		customTabs = position.getCustomTabsList();
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			for(Tab tab:customTabs) {
				TabConfiguration tabConfiguration = position.getTabConfiguration(tab);
				if(!tabConfiguration.isDisabled()) {
					customTabsAttributesDelegate.add(new ApplicationAttributesDelegate(tab.attributesTab()));
				}
			}
		}
		stackPanel.addListener(this);
		excludedAttributesList = position.getExcludedAttributesList();

		mailTemplates = mailService.getMailTemplates(position, getLocale());
		mailTranslator = Util.createPackageTranslator(TemplateForEmailController.class, getLocale(), getTranslator());
		
		privateOption = recruitingModule.getApplicationAddressPrivateOption();
		businessOption = recruitingModule.getApplicationAddressBusinessOption();
		
		setRatingDeadline();

		initForm(ureq);
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel(position);
		
		positionOres = OresHelper.clone(position);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), positionOres);
		
		addToHistory(ureq, OresHelper.createOLATResourceableType("Applications"), null);
	}

	public List<ApplicationLight> getApplications() {
		return applications;
	}
	
	private void setRatingDeadline() {
		Date ratingDeadline = position.getRatingDeadline();
		if(ratingDeadline != null
				&& !PositionStatus.closed.name().equals(position.getStatus())
				&& !PositionStatus.closedAndNoRating.name().equals(position.getStatus())
				&& !PositionStatus.reporting.name().equals(position.getStatus())) {
			String deadlineStr = DateTimeCellRenderer.format(ratingDeadline);
			deadlineStr += RecruitingHelper.isSummerTime(ratingDeadline) ? " CEST" : " CET";
			String ratingDeadlineWarning = translate("rating.deadline.warning", new String[]{ deadlineStr });
			flc.contextPut("ratingDeadlineWarning", ratingDeadlineWarning);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(ratingDeadline);

			String countdown;
			Date now = new Date();
			if(now.compareTo(ratingDeadline) > 0) {
				countdown = translate("rating.closed");
			} else {
				double daysConst = 24 * 60 * 60 * 1000d;
				long ratingDeadlineTime = ratingDeadline.getTime();
				long countDays = Math.round((ratingDeadlineTime - now.getTime()) / daysConst);
				if(countDays > 1) {
					countdown = translate("rating.countdown.day", new String[]{  Long.toString(countDays) });
				} else {
					double hoursConst = 60 * 60 * 1000d;
					long countHours = Math.round((ratingDeadlineTime - now.getTime()) / hoursConst);
					if(countHours > 1) {
						countdown = translate("rating.countdown.hour", new String[]{  Long.toString(countHours) });
						
					} else {
						double minutesConst = 60 * 1000d;
						long countMinutes = Math.round((ratingDeadlineTime - now.getTime()) / minutesConst);
						countdown = translate("rating.countdown.minute", new String[]{ Long.toString(countMinutes) });
					}
				}
			}

			flc.contextPut("countdown", countdown);
		} else {
			flc.contextPut("ratingDeadlineWarning", null);
			flc.contextPut("countdown", null);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.id, SELECT_POSITION));//EscapeMode.antisamy
		
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		initColumnsModelApplicant(filters);
		initColumnsModelAddress(filters);
		initColumnsModelOrganization(filters);
		initColumnsAdditionalPersonalData(filters);
		initColumnsModelBusinessAddress(filters);
		initColumnsModelPrivateAddress(filters);
		if(recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			initColumnsModelAcademicalBackground(filters);
		}
		if(position.isApplicationProject()) {
			initColumnsModelProject(filters);
		}
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			initColumnsModelCustomTabs(filters);
		}
		initColumnsModelTags();
		initColumnsModelReference(filters);
		initColumnsModelReviews();
		initColumnsModelRatings();
		initColumnsModelStaffInfos(filters);
		initColumnsModelActions();
		
		applicationsDataModel = new PositionApplicationsDataModel(getIdentity(), position, secCallback, getTranslator(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", applicationsDataModel, 25, false, getTranslator(), formLayout);
		initSort();
		tableEl.setCssDelegate(this);
		
		tableEl.setAndLoadPersistedPreferences(ureq, COLUMN_PREFS + position.getKey());
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setShowAllRowsEnabled(true);
		
		initFilterPresets();
		initFilters(filters);
		
		String page = velocity_root + "/assignments_statistics.html";
		statisticsContainer = FormLayoutContainer.createCustomFormLayout("statistics", getTranslator(), page);
		statisticsContainer.contextPut("allStatisticsOnly", Boolean.valueOf(!recruitingModule.isApplicationAssignmentsEnabled()));
		statisticsContainer.setVisible(false);
		formLayout.add(statisticsContainer);
		
		myStatisticsItem = new StackedProgressBarItem("myStatisticsItem");
		statisticsContainer.add("myStatisticsItem", myStatisticsItem);
		allStatisticsItem = new StackedProgressBarItem("allStatisticsItem");
		statisticsContainer.add("allStatisticsItem", allStatisticsItem);
		
		toggleAssignmentStatisticsLink = uifactory.addFormLink("toogle.statistics", "toogle.assignments.statistics.all",
				null, statisticsContainer, Link.LINK);
		toggleAssignmentStatisticsLink.setUserObject(Boolean.TRUE);

		if(secCallback.canEditCommitteeDecision()) {
			tableEl.setMultiSelect(true);
			batchDecisionButton = uifactory.addFormLink("batch.decision", formLayout, Link.BUTTON);
			batchDecisionButton.setElementCssClass("o_sel_batch_decision");
		}
		if(recruitingModule.isCategoriesEnabledFor(position) && secCallback.canEditApplicationCategories()) {
			tableEl.setMultiSelect(true);
			batchCategoriesButton = uifactory.addFormLink("batch.categories", formLayout, Link.BUTTON);
			batchCategoriesButton.setElementCssClass("o_sel_batch_categories");
		}
		if(secCallback.canGenerateApplicationList()) {
			tableEl.setMultiSelect(true);
			generateListButton = uifactory.addFormLink("generate.application.list", formLayout, Link.BUTTON);
			generateListButton.setElementCssClass("o_sel_generate_list");
		}

		initMoreDropdown(formLayout);
		initAssignmentDropdown(formLayout);
	}
	
	private void initMoreDropdown(FormItemContainer formLayout) {
		DropdownItem moreDropdown = new DropdownItem("batch.more", "batch.more", getTranslator());
		moreDropdown.setButton(true);
		moreDropdown.setEmbbeded(true);
		formLayout.add("batch.more", moreDropdown);
		
		boolean canSendApplicationEmails = secCallback.canSendBulkApplicationEmails();
		boolean canReferences = recruitingModule.isReferenceEnabled()
				&& secCallback.canEditApplicationReferences()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled());
		boolean canAppsFeedback = recruitingModule.isMembersFeedbackEnabled()
				&& secCallback.canEditApplicationMembersFeedback()
				&& feedbackService.hasFeedbackConfigurationEnabled(position);
		boolean canEditStatus = secCallback.canEditApplicationStatus();
		
		int buttonOrLink = Link.BUTTON;
		if((canSendApplicationEmails && canReferences) || canAppsFeedback || canEditStatus) {
			buttonOrLink = Link.LINK;
		} else {
			moreDropdown.setVisible(false);
		}
		
		if(canEditStatus) {
			tableEl.setMultiSelect(true);
			batchStatusButton = uifactory.addFormLink("batch.status", formLayout, buttonOrLink);
			batchStatusButton.setElementCssClass("o_sel_batch_status");
			moreDropdown.addElement(batchStatusButton);
		}
		
		if(canSendApplicationEmails) {
			tableEl.setMultiSelect(true);
			batchSendApplicationEmailsButton = uifactory.addFormLink("send.application.mails", formLayout, buttonOrLink);
			batchSendApplicationEmailsButton.setElementCssClass("o_sel_send_app_emails");
			moreDropdown.addElement(batchSendApplicationEmailsButton);
		}
		
		if(canReferences)  {
			tableEl.setMultiSelect(true);
			
			addComparativeExpertsButton = uifactory.addFormLink("add.comparative.expert", formLayout, buttonOrLink);
			addComparativeExpertsButton.setElementCssClass("o_sel_add_comparative_experts");
			addComparativeExpertsButton.setVisible(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled());
			moreDropdown.addElement(addComparativeExpertsButton);
			
			batchSendReferencesEmailsButton = uifactory.addFormLink("send.reference.mails", formLayout, buttonOrLink);
			batchSendReferencesEmailsButton.setElementCssClass("o_sel_send_ref_emails");
			moreDropdown.addElement(batchSendReferencesEmailsButton);
		}
		
		if(canAppsFeedback) {
			batchAddFeedbackMembersButton = uifactory.addFormLink("add.apps.feedback.members", formLayout, buttonOrLink);
			batchAddFeedbackMembersButton.setElementCssClass("o_sel_add_apps_feedback_members");
			moreDropdown.addElement(batchAddFeedbackMembersButton);
			
			batchRemoveFeedbackMembersButton = uifactory.addFormLink("remove.apps.feedback.members", formLayout, buttonOrLink);
			batchRemoveFeedbackMembersButton.setElementCssClass("o_sel_remove_apps_feedback_members");
			moreDropdown.addElement(batchRemoveFeedbackMembersButton);
			
			batchContactFeedbackMembersButton = uifactory.addFormLink("contact.apps.feedback.members", formLayout, buttonOrLink);
			batchContactFeedbackMembersButton.setElementCssClass("o_sel_contact_apps_feedback_members");
			moreDropdown.addElement(batchContactFeedbackMembersButton);
		}
		
		if(recruitingModule.isCopyApplicationEnabled() && secCallback.canCopyApplication()) {
			copyApplicationsButton = uifactory.addFormLink("copy.apps", formLayout, buttonOrLink);
			copyApplicationsButton.setElementCssClass("o_sel_copy_apps");
			moreDropdown.addElement(copyApplicationsButton);
		}
		
	}
	
	private void initAssignmentDropdown(FormItemContainer formLayout) {
		if(recruitingModule.isApplicationAssignmentsEnabled() && secCallback.canEditAssignments()) {
			addAssignmentButton = uifactory.addFormLink("add.assignments", formLayout, Link.LINK);
			addAssignmentButton.setElementCssClass("o_sel_add_assignments");
			removeAssignmentButton = uifactory.addFormLink("remove.assignments", formLayout, Link.LINK);
			removeAssignmentButton.setElementCssClass("o_sel_remove_assignments");

			DropdownItem dropdown = new DropdownItem("batch.assignments", "assignments.mgmt", getTranslator());
			formLayout.add("batch.assignments", dropdown);
			dropdown.addElement(addAssignmentButton);
			dropdown.addElement(removeAssignmentButton);
			dropdown.setButton(true);
			dropdown.setEmbbeded(true);
		}
	}
	
	private void initSort() {
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		SortKey defOrderBy = recruitingModule.getTableApplicationSort();
		sortOptions.setDefaultOrderBy(defOrderBy);
		tableEl.setSortSettings(sortOptions);
	}
	
	private void initColumnsModelApplicant(List<FlexiTableExtendedFilter> filters) {
		initColumnModel(Fields.title, recruitingModule.getTableApplicationsPersonTitleOption(), filters);
		initColumnModel(Fields.firstName, recruitingModule.getTableApplicationsPersonFirstNameOption(), filters);
		initColumnModel(Fields.lastName, recruitingModule.getTableApplicationsPersonLastNameOption(), filters);
		initColumnModel(Fields.gender, recruitingModule.getTableApplicationsGenderOption(), new GenderCellRenderer(), filters);
		initColumnModel(Fields.maritalStatus, recruitingModule.getTableApplicationsMaritalStatusOption(), filters);
		initColumnModel(Fields.yearOfBirth, recruitingModule.getTableApplicationsYearOfBirthOption(), filters);
		initDateColumnModel(Fields.birthday, recruitingModule.getTableApplicationsBirthdayOption(), filters);
		initColumnModel(Fields.academicTitle, recruitingModule.getTableApplicationPersonAcademicTitleOption(), filters);
	}
	
	private void initColumnsModelAddress(List<FlexiTableExtendedFilter> filters) {
		initColumnModel(Fields.nationality, recruitingModule.getTableApplicationsNationalityOption(), filters);
		initColumnModel(Fields.additionalNationalities, recruitingModule.getTableApplicationsAdditionalNationalitiesOption(), filters);
		initColumnModel(Fields.mail, recruitingModule.getTableApplicationsEMailOption(), filters);
		initColumnModel(Fields.phone, recruitingModule.getTableApplicationsPhoneOption(), filters);
		initColumnModel(Fields.mobilePhone, recruitingModule.getTableApplicationsMobilePhoneOption(), filters);
		initColumnModel(Fields.disability, recruitingModule.getTableApplicationsDisabilityOption(), new DisabilityCellRenderer(), filters);
	}
	
	private void initColumnsModelOrganization(List<FlexiTableExtendedFilter> filters) {
		initColumnModel(Fields.organization, recruitingModule.getTableApplicationsOrganizationOption(), filters);
		initColumnModel(Fields.unit, recruitingModule.getTableApplicationsOrganizationUnitOption(), filters);
		initColumnModel(Fields.currentPosition, recruitingModule.getTableApplicationsOrganizationCurrentPositionOption(), filters);
	}
	
	private void initColumnsModelBusinessAddress(List<FlexiTableExtendedFilter> filters) {
		if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
			initColumnModel(Fields.businessAddressLine1, recruitingModule.getTableApplicationsBusinessAddressLinesOption(), filters);
			initColumnModel(Fields.businessAddressLine2, recruitingModule.getTableApplicationsBusinessAddressLinesOption(), filters);
			initColumnModel(Fields.businessAddressLine3, recruitingModule.getTableApplicationsBusinessAddressLinesOption(), filters);
			initColumnModel(Fields.businessZipcode, recruitingModule.getTableApplicationsBusinessZipcodeOption(), filters);
			initColumnModel(Fields.businessCity, filters);
			initColumnModel(Fields.businessCountry, filters);
			if(recruitingModule.isApplicationBusinessPhoneEnabled()) {
				initColumnModel(Fields.businessPhone, filters);
			}
			if(recruitingModule.isApplicationBusinessMailEnabled()) {
				initColumnModel(Fields.businessMail, filters);
			}
		}
	}
	
	private void initColumnsAdditionalPersonalData(List<FlexiTableExtendedFilter> filters) {
		personalDataAttributesDelegate.initColumnsModel(columnsModel, position, SELECT_POSITION, getLocale(), filters);
	}
	
	private void initColumnsModelPrivateAddress(List<FlexiTableExtendedFilter> filters) {
		if(!AddressOption.disabled.equals(privateOption)) {
			initColumnModel(Fields.addressLine1, recruitingModule.getTableApplicationsAddressLinesOption(), filters);
			initColumnModel(Fields.addressLine2, recruitingModule.getTableApplicationsAddressLinesOption(), filters);
			initColumnModel(Fields.addressLine3, recruitingModule.getTableApplicationsAddressLinesOption(), filters);
			initColumnModel(Fields.zipcode, recruitingModule.getTableApplicationsZipcodeOption(), filters);
			initColumnModel(Fields.city, filters);
			if (recruitingModule.isApplicationAddressCountryEnabled()) {
				initColumnModel(Fields.country, filters);
			}
		}
	}
	
	private void initColumnsModelAcademicalBackground(List<FlexiTableExtendedFilter> filters) {
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()) {
			initColumnModel(Fields.numberOfOriginalPublications, filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()) {
			initColumnModel(Fields.numberOfFirstAuthorships, filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()) {
			initColumnModel(Fields.numberOfLastAuthorships, filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()) {
			initColumnModel(Fields.citations, filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()) {
			initColumnModel(Fields.impactFactor, filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()) {
			initColumnModel(Fields.hFactor, filters);
		}
		
		//degree
		if(recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {

			RecruitingTableOption highestDegreeOption = recruitingModule.getTableApplicationsHighestDegreeOption();
			if(!highestDegreeOption.isDisabled() && !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)) {
				initColumnModel(Fields.highestDegreeType, highestDegreeOption, filters);
			}
			
			RecruitingTableOption highestDegreeYearOption = recruitingModule.getTableApplicationsHighestDegreeYearOption();
			if(!highestDegreeYearOption.isDisabled() && !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)) {
				if(recruitingModule.isTableApplicationsHighestDegreeYearOnlyPhDOption()) {
					initDateColumnModel(Fields.highestDegreeYearPhD, highestDegreeYearOption,
							new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale()), filters);
				} else {
					initDateColumnModel(Fields.highestDegreeYear, highestDegreeYearOption,
							new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale()), filters);
				}
			}

			RecruitingTableOption highestDegreeInstitutionOption = recruitingModule.getTableApplicationsHighestDegreeInstitutionOption();
			if(!highestDegreeInstitutionOption.isDisabled() && !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION)) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(highestDegreeInstitutionOption.isVisible(), Fields.highestDegreeInstitution, SELECT_POSITION));	
			}
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()) {
			initColumnModel(Fields.workedInAcademiaSince, recruitingModule.getTableApplicationsWorkedInAcademiaSinceOption(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()) {
			initColumnModel(Fields.workedOutAcademiaSince, recruitingModule.getTableApplicationsWorkedOutAcademiaSinceOption(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()) {
			initColumnModel(Fields.workedOutAcademiaCareSince, recruitingModule.getTableApplicationsWorkedOutAcademiaCareSinceOption(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()) {
			initColumnModel(Fields.careerDescription, filters);
		}

		//dissertation
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			initColumnModel(Fields.dissertationTitle, recruitingModule.getTableApplicationsDissertationTitleOption(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			RecruitingTableOption dissertationDateOption = recruitingModule.getTableApplicationsDissertationDateOption();
			initDateColumnModel(Fields.dissertationDate, dissertationDateOption,
					new AcademicalDateCellRenderer(recruitingModule.getApplicationAcademicalBackgroundDissertationDateFormat(), getLocale()), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			initColumnModel(Fields.dissertationInstitution, recruitingModule.getTableApplicationsDissertationInstitutionOption(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD1)) {
			initColumnModel(Fields.dissertationKeyword1, recruitingModule.getTableApplicationsDissertationKeyword1Option(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD2)) {
			initColumnModel(Fields.dissertationKeyword2, recruitingModule.getTableApplicationsDissertationKeyword2Option(), filters);
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD3)) {
			initColumnModel(Fields.dissertationKeyword3, recruitingModule.getTableApplicationsDissertationKeyword3Option(), filters);
		}
		
		//habilitation
		if(recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled()) {
			initColumnModel(Fields.habilitationTitle, filters);
			initDateColumnModel(Fields.habilitationDate, new AcademicalDateCellRenderer(AcademicalDateFormat.yearsOnly(), getLocale()), filters);
			initColumnModel(Fields.habilitationInstitution, filters);
		}
		
		//orcid
		if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()) {
			initColumnModel(Fields.orcid, filters);
		}
		
		academicalBackgroundAttributesDelegate.initColumnsModel(columnsModel, position, SELECT_POSITION, getLocale(), filters);
	}
	
	private void initColumnsModelProject(List<FlexiTableExtendedFilter> filters) {
		initColumnModel(Fields.projectTitle, recruitingModule.getTableApplicationsProjectTitle(), filters);
		initColumnModel(Fields.projectAcronym, recruitingModule.getTableApplicationsProjectAcronym(), filters);
		initColumnModel(Fields.projectKeywords, recruitingModule.getTableApplicationsProjectKeywords(), filters);
		initColumnModel(Fields.projectDisciplines, recruitingModule.getTableApplicationsProjectDisciplines(), filters);
		initDateColumnModel(Fields.projectStartDate, recruitingModule.getTableApplicationsProjectStartDate(), filters);
		initColumnModel(Fields.projectDuration, recruitingModule.getTableApplicationsProjectDuration(), filters);
		initColumnModel(Fields.projectFinancialImpact1, recruitingModule.getTableApplicationsProjectFinancialImpact1(),
				recruitingModule.getApplicationProjectFinancialImpact1Type(), filters);
		initColumnModel(Fields.projectFinancialImpact2, recruitingModule.getTableApplicationsProjectFinancialImpact2(),
				recruitingModule.getApplicationProjectFinancialImpact2Type(), filters);
		initColumnModel(Fields.projectFinancialImpact3, recruitingModule.getTableApplicationsProjectFinancialImpact3(),
				recruitingModule.getApplicationProjectFinancialImpact3Type(), filters);
		initColumnModel(Fields.projectFinancialImpact4, recruitingModule.getTableApplicationsProjectFinancialImpact4(),
				recruitingModule.getApplicationProjectFinancialImpact4Type(), filters);
		initColumnModel(Fields.projectFinancialImpact5, recruitingModule.getTableApplicationsProjectFinancialImpact5(),
				recruitingModule.getApplicationProjectFinancialImpact5Type(), filters);
		initColumnModel(Fields.project, recruitingModule.getTableApplicationsProject(), new ProjectCellRenderer(), filters);
		initColumnModel(Fields.projectDescription, recruitingModule.getTableApplicationsProjectDescription(),
				new TooltipCellRenderer("o_icon_project_description"), filters);
		projectAttributesDelegate.initColumnsModel(columnsModel, position, SELECT_POSITION, getLocale(), filters);
	}
	
	private void initColumnsModelCustomTabs(List<FlexiTableExtendedFilter> filters) {
		for(ApplicationAttributesDelegate attributesDelegate:customTabsAttributesDelegate) {
			attributesDelegate.initColumnsModel(columnsModel, position, SELECT_POSITION, getLocale(), filters);
		}
	}

	private void initColumnsModelTags() {
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			// Filter is special
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.categories, new CategoriesCellRenderer()));
		}
	}
	
	private void initColumnsModelReference(List<FlexiTableExtendedFilter> filters) {
		if(recruitingModule.isReferenceEnabled()) {
			RecruitingTableOption providedOption = recruitingModule.getTableApplicationsProvidedExpertsRecommendationsOption();
			if((position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()) && !providedOption.isDisabled()) {
				initColumnModel(Fields.providedExpertsRecommendations, providedOption, filters);
			}
			RecruitingTableOption expertsOption = recruitingModule.getTableApplicationsExpertsOption();
			if(position.isExpertRecommendationEnabled() && !expertsOption.isDisabled()) {
				initColumnModel(Fields.experts, expertsOption, new ReferencesStatsCellRenderer(ReferenceType.expert), filters);
			}
			RecruitingTableOption refereesOption = recruitingModule.getTableApplicationsRefereesOption();
			if(position.isRefereeRecommendationEnabled() && !refereesOption.isDisabled()) {
				initColumnModel(Fields.recommendations, refereesOption, new ReferencesStatsCellRenderer(ReferenceType.recommendation), filters);
			}
			RecruitingTableOption comparativeExpertsOption = recruitingModule.getTableApplicationsComparativeExpertsOption();
			if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled() && !comparativeExpertsOption.isDisabled()) {
				initColumnModel(Fields.comparativeExperts, refereesOption, new ReferencesStatsCellRenderer(ReferenceType.comparativeAssessmentExpert), filters);
			}
		}
	}
	
	private void initColumnsModelReviews() {
		if(recruitingModule.isReviewEnabled() && position.isReviewEnabled()) {
			boolean canViewReview = secCallback.canViewReviews(true);
			RecruitingTableOption colEnabled = recruitingModule.getTableApplicationsReviewsOption();
			if ((canViewReview || secCallback.canReview()) && !colEnabled.isDisabled()) {
				// display the number of review whenever allowed to see it
				boolean defVisible = canViewReview && colEnabled.isVisible();
				DefaultFlexiColumnModel reviewsCol = new DefaultFlexiColumnModel(defVisible, Fields.reviews, SELECT_POSITION);
				reviewsCol.setAlwaysVisible(secCallback.canMyReviewsColumnAlwaysVisible());
				columnsModel.addFlexiColumnModel(reviewsCol);				
			}
			if(secCallback.canReview()) {
				// display create/edit button column only when reviewing possible
				DefaultFlexiColumnModel revButtonCol = new DefaultFlexiColumnModel(Fields.reviewButton);
				revButtonCol.setAlwaysVisible(secCallback.canMyReviewsColumnAlwaysVisible());
				columnsModel.addFlexiColumnModel(revButtonCol);
			}
		}
	}
	
	private void initColumnsModelRatings() {
		//ratings
		boolean canRate = secCallback.canRate();
		DefaultFlexiColumnModel myRatingCol = new DefaultFlexiColumnModel(canRate, Fields.myRating, SELECT_POSITION, new SingleRatingCellRenderer());
		myRatingCol.setAlwaysVisible(secCallback.canMyRatingsColumnAlwaysVisible());
		columnsModel.addFlexiColumnModel(myRatingCol);
		
		if(recruitingModule.isApplicationAssignmentsEnabled() && secCallback.canEditAssignments()) {
			DefaultFlexiColumnModel assignmentsCol = new DefaultFlexiColumnModel(Fields.assignments, SELECT_POSITION, new AssignmentsCellRenderer());
			columnsModel.addFlexiColumnModel(assignmentsCol);
		}
	
		RecruitingTableContextualOption committeeRatingOptions = recruitingModule.getTableApplicationsCommitteeRating();
		if(!committeeRatingOptions.isDisabled() && secCallback.canSeeCommitteeRatingsOnce()) {
			boolean ratings = committeeRatingOptions == RecruitingTableContextualOption.always
					|| isCommitteeRatingsColumnDefault();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ratings, Fields.committeeRating, SELECT_POSITION));
		}
		
		RecruitingTableContextualOption decisionOption = recruitingModule.getTableApplicationsDecision();
		if(!decisionOption.isDisabled()) {
			boolean decision = decisionOption == RecruitingTableContextualOption.always || !canRate;
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(decision, Fields.decision, SELECT_POSITION, new DecisionCellRenderer()));
		}
	}
	
	private void initColumnsModelStaffInfos(List<FlexiTableExtendedFilter> filters) {
		if(secCallback.canSeeAd()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.ad, SELECT_POSITION));
		}
		
		initDateColumnModel(Fields.submittedDate, recruitingModule.getTableApplicationsSubmittedDateOption(), filters);
		initColumnModel(Fields.submittedByStaff, recruitingModule.getTableApplicationsSubmittedByStaffOption(), null);//TODO selectus special selection
		initColumnModel(Fields.applicationStatus, recruitingModule.getTableApplicationsStatusOption(), null);// Special configurable list
		initDateColumnModel(Fields.applicationStatusDate, recruitingModule.getTableApplicationsStatusDateOption(), filters);
		
		if(secCallback.canViewParalellApplications()) {
			initColumnModel(Fields.parallelApplications, recruitingModule.getTableApplicationsStatusDateOption(),
					new ParallelApplicationsCellRenderer(getLocale()), filters);
		}
	}
	
	private void initColumnsModelActions() {
		if(recruitingModule.isApplicationsMemoEnabled()) {
			RecruitingTableOption memoOption = recruitingModule.getTableApplicationsMemoOption();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(memoOption.isVisible(), Fields.memo, new TooltipCellRenderer("o_icon_memo")));
		}
		
		if(secCallback.canViewCommitteeComment()) {
			RecruitingTableOption commentOption = recruitingModule.getTableApplicationsCommitteeCommentOption();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(commentOption.isVisible(), Fields.committeeComment, new TooltipCellRenderer("o_icon_comment")));
		}
		
		if(secCallback.canNotes()) {
			DefaultFlexiColumnModel notesColumn = new DefaultFlexiColumnModel(Fields.notes, "notes", new NotesCellRenderer());
			//TODO selectus notesColumn.setActionAriaLabel(translate(Fields.notes.i18nHeaderKey()));
			columnsModel.addFlexiColumnModel(notesColumn);
		} 

		DefaultFlexiColumnModel newWindowColumn = new DefaultFlexiColumnModel("open.new.window", -1, null,
				false,  null, new ApplicationURLCellRenderer(position, getTranslator()));
		newWindowColumn.setIconHeader("o_icon o_icon_external_link");
		columnsModel.addFlexiColumnModel(newWindowColumn);

		if(secCallback.canEditApplication()) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("edit", -1, "edit",
					new StaticFlexiCellRenderer("", "edit", null, "o_icon o_icon_edit", translate("edit")));
			editColumn.setIconHeader("o_icon o_icon_edit");
			editColumn.setHeaderLabel(translate("edit"));
			editColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(editColumn);
		}
		
		if(secCallback.canDeleteApplication()) {
			DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel("delete", -1, "delete",
					new StaticFlexiCellRenderer("", "delete", null, "o_icon o_icon_delete_item", translate("delete")));
			deleteColumn.setIconHeader("o_icon o_icon_delete_item");
			deleteColumn.setHeaderLabel(translate("delete"));
			deleteColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(deleteColumn);
		}
	}
	
	private void initColumnModel(Fields field, RecruitingTableOption option, FlexiCellRenderer render, List<FlexiTableExtendedFilter> filters) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field,
					SELECT_POSITION, render));
			filters.add(new FlexiTableTextFilter(translate(field.i18nHeaderKey()), field.name(), false));
		}
	}
	
	private void initDateColumnModel(Fields field, RecruitingTableOption option, List<FlexiTableExtendedFilter> filters) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field,
					SELECT_POSITION, new DateCellRenderer()));
			filters.add(new FlexiTableDateRangeFilter(translate(field.i18nHeaderKey()), field.name(), false, false,
					getLocale()));
		}
	}
	
	private void initDateColumnModel(Fields field, FlexiCellRenderer render, List<FlexiTableExtendedFilter> filters) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, field,
				SELECT_POSITION, render));
		filters.add(new FlexiTableDateRangeFilter(translate(field.i18nHeaderKey()), field.name(), false, false, getLocale()));
	}
	
	private void initDateColumnModel(Fields field, RecruitingTableOption option, FlexiCellRenderer render, List<FlexiTableExtendedFilter> filters) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field,
					SELECT_POSITION, render));
			filters.add(new FlexiTableDateRangeFilter(translate(field.i18nHeaderKey()), field.name(), false, false, getLocale()));
		}
	}
	
	protected void initColumnModel(Fields field,  List<FlexiTableExtendedFilter> filters) {
		if(field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, field, SELECT_POSITION));
			if(filters != null) {
				filters.add(new FlexiTableTextFilter(translate(field.i18nHeaderKey()), field.name(), false));
			}
		}
	}
	
	private void initColumnModel(Fields field, RecruitingTableOption option, List<FlexiTableExtendedFilter> filters) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field, SELECT_POSITION));
			if(filters != null) {
				filters.add(new FlexiTableTextFilter(translate(field.i18nHeaderKey()), field.name(), false));
			}
		}
	}
	
	private void initColumnModel(Fields field, RecruitingTableOption option, ApplicationFieldType type, List<FlexiTableExtendedFilter> filters) {
		if(!option.isDisabled() && field.visible(excludedAttributesList)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field, SELECT_POSITION));
			if(type.getType() == Type.integer || type.getType() == Type.sum || type.getType() == Type.number) {
				filters.add(new FlexiTableNumericalRangeFilter(translate(field.i18nHeaderKey()), field.name(), false,
						translate("from"), translate("to")));
			} else {
				filters.add(new FlexiTableTextFilter(translate(field.i18nHeaderKey()), field.name(), false));
			}
		}
	}
	
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		// applicationStatus,withoutCEmails,,female,applicationADecision,
		String[] defaultFilters = recruitingModule.getTableApplicationsDefaultAdvancedFilters();
		
		// Application active
		if(isEnabled(defaultFilters, "applicationActive")) {
			applicationActiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters("applicationActive", translate("filter.application.active"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(Fields.applicationStatus.name(), ApplicationStatus.active.name())));
			tabs.add(applicationActiveTab);
		}

		// Decision
		if(isEnabled(defaultFilters, "applicationADecision")) {
			FlexiFiltersTab decisionTab = FlexiFiltersTabFactory.tabWithImplicitFilters("applicationADecision", translate("decision.3.filter"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(Fields.decision.name(), "3")));
			tabs.add(decisionTab);
		}
		if(isEnabled(defaultFilters, "applicationBDecision")) {
			FlexiFiltersTab decisionTab = FlexiFiltersTabFactory.tabWithImplicitFilters("applicationBDecision", translate("decision.2.filter"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(Fields.decision.name(), "2")));
			tabs.add(decisionTab);
		}
		if(isEnabled(defaultFilters, "applicationCDecision")) {
			FlexiFiltersTab decisionTab = FlexiFiltersTabFactory.tabWithImplicitFilters("applicationCDecision", translate("decision.1.filter"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(Fields.decision.name(), "1")));
			tabs.add(decisionTab);
		}
		
		// Without sent emails
		if(isEnabled(defaultFilters, "withoutSentEmails")) {
			List<String> templates = mailTemplates.stream()
					.map(PositionMailTemplateRef::getName)
					.collect(Collectors.toList());
			templates.add("filter.no.template");
			withoutSentEmailTab = FlexiFiltersTabFactory.tabWithImplicitFilters("withoutEmail", translate("filter.without.sent.email"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_WITHOUT_SENT_EMAILS, templates)));
			tabs.add(withoutSentEmailTab);
		}
		if(isEnabled(defaultFilters, "withoutCEmails") && StringHelper.containsNonWhitespace(recruitingModule.getMailTemplateRejectionTitle())) {
			withoutSentCEmailTab = FlexiFiltersTabFactory.tabWithImplicitFilters("withoutCEmails", translate("filter.c.email"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_WITHOUT_SENT_EMAILS, recruitingModule.getMailTemplateRejectionTitle())));
			tabs.add(withoutSentCEmailTab);
		}

		// My assignment
		if(isEnabled(defaultFilters, "assignments")) {
			myAssignmentsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("myAssignments", translate("filter.my.assignments"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_ASSIGNEE, getIdentity().getKey().toString())));
			tabs.add(myAssignmentsTab);
		}
		
		// Not rated
		if(secCallback.canRate() && isEnabled(defaultFilters, "notRated")) {
			FlexiFiltersTab notRatedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("myRatings", translate("filter.application.notRated"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_MY_RATING, FILTER_NULL_KEY)));
			tabs.add(notRatedTab);
		}
		
		// Female
		if(isEnabled(defaultFilters, "female")) {
			FlexiFiltersTab femaleTab = FlexiFiltersTabFactory.tabWithImplicitFilters("female", translate(PersonGender.female.i18nKey()),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(PositionApplicationsDataModel.Fields.gender.name(), translate(PersonGender.female.i18nKey()))));
			tabs.add(femaleTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	private boolean isEnabled(String[] list, String value) {
		for(String element:list) {
			if(value.equals(element)) {
				return true;
			}
		}
		return false;
	}
	
	private void initFilters(List<FlexiTableExtendedFilter> filedsFilters) {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// Application status
		SelectionValues applicationStatusPK = new SelectionValues();
		ApplicationStatus[] applicationStatus = recruitingModule.getTableApplicationsDefaultBasicFilterApplicationStatus();
		for(ApplicationStatus status:applicationStatus) {
			applicationStatusPK.add(SelectionValues.entry(status.name(), translate("application.status.".concat(status.name()))));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.feedback.application.status"),
				Fields.applicationStatus.name(), applicationStatusPK, true));
		
		// Decisions
		SelectionValues decisionKV = new SelectionValues();
		decisionKV.add(SelectionValues.entry(FILTER_NULL_KEY, translate("decision.0.filter")));
		decisionKV.add(SelectionValues.entry("3", translate("decision.3.filter")));
		decisionKV.add(SelectionValues.entry("2", translate("decision.2.filter")));
		decisionKV.add(SelectionValues.entry("1", translate("decision.1.filter")));
		decisionKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.feedback.application.decision"),
				Fields.decision.name(), decisionKV, true));
		
		// Without sent emails
		SelectionValues templatesPK = new SelectionValues();
		if(mailTemplates != null && !mailTemplates.isEmpty()) {
			for(PositionMailTemplateRef mailTemplate:mailTemplates) {
				templatesPK.add(SelectionValues.entry(mailTemplate.getName(), mailTemplate.getName()));
			}
		}
		templatesPK.add(SelectionValues.entry(translate("filter.no.template"), translate("filter.no.template")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.not.sent.email"),
				FILTER_WITHOUT_SENT_EMAILS, templatesPK, true));
		// With sent emails
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.sent.email"),
				FILTER_WITH_SENT_EMAILS, templatesPK, true));
		
		// Assignees
		if(secCallback.canEditAssignments()) {
			committeeAssignees = recruitingService.getCommittee(position, PositionRole.values());
			if(committeeAssignees.size() > 1) {
				Collections.sort(committeeAssignees, new IdentityLastnameComparator());
			}
			SelectionValues assigneesPK = new SelectionValues();
			for(Identity member:committeeAssignees) {
				String assignee = userManager.getUserDisplayName(member);
				String label = translate("filter.assignment.to", new String[] { assignee } );
				assigneesPK.add(SelectionValues.entry(member.getKey().toString(), label));
			}
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.assignments"),
					FILTER_ASSIGNEE, assigneesPK, true));
		} else {
			SelectionValues recertificationValues = new SelectionValues();
			recertificationValues.add(SelectionValues.entry(getIdentity().getKey().toString(), translate("filter.my.assignments")));
			filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.my.assignments"),
					FILTER_ASSIGNEE, recertificationValues, true));
		}
		
		// My ratings
		if(secCallback.canRate()) {
			SelectionValues ratingPK = new SelectionValues();
			ratingPK.add(SelectionValues.entry("A", translate("rating.2.filter")));
			ratingPK.add(SelectionValues.entry("B", translate("rating.1.filter")));
			ratingPK.add(SelectionValues.entry("C", translate("rating.0.filter")));
			ratingPK.add(SelectionValues.entry(FILTER_ABSTAIN_KEY, translate("abstain.title")));
			ratingPK.add(SelectionValues.entry(FILTER_NULL_KEY, translate("rating.not.rated")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("edit.application.my_rating"),
					FILTER_MY_RATING, ratingPK, true));
		}
		
		// Gender
		SelectionValues genderPK = new SelectionValues();
		genderPK.add(SelectionValues.entry(FILTER_NULL_KEY, "-"));
		PersonGender[] personGenders = recruitingModule.getPersonGenders();
		for(PersonGender personGender:personGenders) {
			genderPK.add(SelectionValues.entry(translate(personGender.i18nKey()), translate(personGender.i18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate(Fields.gender.i18nHeaderKey()),
				Fields.gender.name(), genderPK, true));
		
		filters.addAll(filedsFilters);
		
		tableEl.setFilters(true, filters, true, false);
	}
	
	public SelectionValues getAssigneesKeyValues() {
		if(committeeAssignees == null) {
			committeeAssignees = recruitingService.getCommittee(position, PositionRole.values());
		}

		SelectionValues keyValues = new SelectionValues();
		for(Identity assignee:committeeAssignees) {
			String fullName = userManager.getUserDisplayName(assignee);
			keyValues.add(SelectionValues.entry(fullName, fullName));
		}
		return keyValues;
	}
	
	public SelectionValues getApplicationStatusKeyValues() {
		SelectionValues keyValues = new SelectionValues();
		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		for(ApplicationStatus status:availableStatus) {
			keyValues.add(SelectionValues.entry(status.name(), translate("application.status.".concat(status.name()))));	
		}
		return keyValues;
	}
	
	public SelectionValues getParallelPositionsKeyValues() {
		SelectionValues keyValues = new SelectionValues();
		
		ParallelApplicationScope scope = recruitingModule.getParallelApplicationScope();
		List<ParallelApplication> apps = recruitingService.getParallelApplications(position, scope);
		Set<PositionLight> positions = new HashSet<>();
		for(ParallelApplication app:apps) {
			positions.add(app.getPosition());
		}
		
		for(PositionLight position:positions) {
			keyValues.add(SelectionValues.entry(position.getKey().toString(), position.getMLTitle(getLocale())));
		}
		keyValues.add(SelectionValues.entry("none", "None"));
		return keyValues;
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, positionOres);
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		ApplicationRow row = applicationsDataModel.getObject(pos);

		StringBuilder sb = new StringBuilder(32);
		ApplicationLight app = row.getApplication();
		if(row.getDecision() != null && row.getDecision() > 0) {
			int decision = row.getDecision().intValue();
			switch(decision) {
				case 1: sb.append("fx_r_c_decision"); break;
				case 2: sb.append("fx_r_b_decision"); break;
				case 3: sb.append("fx_r_a_decision"); break;
				default: break;
			}
		}
		sb.append(" fx_r_".concat(app.getApplicationStatus().name()));
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public void loadMailTemplatesAndCo() {
		mailTemplates = mailService.getMailTemplates(position, getLocale());
	}

	public void loadModel(Position updatedPosition) {
		this.position = updatedPosition;
		applications = recruitingService.getApplications(position);

		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		boolean allowToRate = isAllowedToRate(position);

		List<? extends IdentityRef> committee = recruitingService.getCommitteeRefs(position, ratingRoles);
		List<UserRating> ratings = recruitingService.getRatings(position, committee);

		// notes
		List<Notes> notes = recruitingService.getNotes(position, getIdentity());
		Map<Long,Notes> notesMap = notes.stream()
				.collect(Collectors.toMap(Notes::getApplicationKey, n -> n, (u, v) -> u));
		
		List<ApplicationAssignmentLight> applicationAssignments = assignmentService.getAssignments(position);
		Map<Long, Long> assignmentsPerApplications = applicationAssignments.stream()
		           .collect(Collectors.groupingBy(ApplicationAssignmentLight::getApplicationKey, Collectors.counting()));
		Map<Long, List<ApplicationAssignmentLight>> applicationToAssignments = applicationAssignments.stream()
		           .collect(Collectors.groupingBy(ApplicationAssignmentLight::getApplicationKey, Collectors.toList()));
		Map<AssignmentKey, AssignmentKey> assignmentKeys = applicationAssignments.stream()
				.map(assignment -> new AssignmentKey(assignment.getAssigneeKey(), assignment.getApplicationKey()))
				.collect(Collectors.toMap(key -> key, key -> key, (u, v) -> v));

		List<SentEmailTemplates> emailTemplatesLogs = recruitingService.getApplicationSentEmails(position);
		Map<Long, SentEmailTemplates> emailTemplatesMap = emailTemplatesLogs.stream()
				.collect(Collectors.toMap(SentEmailTemplates::getApplicationKey, template -> template, (u, v) -> u));

		Map<Long,ApplicationRefereeStats> appKeyToReviewerStats = new HashMap<>();
		if(recruitingModule.isReferenceEnabled()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled() || position.isComparativeAssessmentExpertEnabled())) {
			List<ApplicationRefereeStats> reviewerStats = recruitingService.getApplicationReviewerStats(position);
			for(ApplicationRefereeStats stats:reviewerStats) {
				appKeyToReviewerStats.put(stats.getKey(), stats);
			}
		}

		boolean canReview  = secCallback.canReview();
		Set<Long> reviewedApps = Collections.emptySet();
		Map<Long, AtomicInteger> reviewsNumber = Collections.emptyMap();
		if(recruitingModule.isReviewEnabled() && position.isReviewEnabled()) {
			List<IdentityRef> reviewers = reviewService.getReviewerRefs(position);
			reviewsNumber = reviewService.getNumberOfReviews(position, reviewers);
			if(canReview) {
				reviewedApps = reviewService.getApplicationReviewed(position, getIdentity());
			}
		}
		
		Map<String,List<ParallelApplication>> paralellApps = loadParalellApplications();
		
		String pathPrefix = "[Positions:0][Position:" + position.getKey() + "][Applications:";

		List<ApplicationRow> rows = new ArrayList<>(applications.size());
		for(ApplicationLight application:applications) {
			Notes appNotes = notesMap.get(application.getKey());
			ApplicationRefereeStats refereesStats = appKeyToReviewerStats.get(application.getKey());
			
			String path = pathPrefix + application.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			Object[] additionalValues = localizedAdditionalValues(application.getAdditionalValues(), application.getAdditionalTypes());
			ApplicationRow row = new ApplicationRow(application, appNotes, refereesStats,
					additionalValues, url);
			
			String mail = row.getMail();
			if(StringHelper.containsNonWhitespace(mail)) {
				row.setParallelApplications(paralellApps.get(mail.toLowerCase()));
			}
			
			UserRatingMapper userRatingMapper = new UserRatingMapper(application);
			row.setUserRatingMapper(userRatingMapper);
			if(reviewsNumber.containsKey(application.getKey())) {
				row.setNumOfReviews(reviewsNumber.get(application.getKey()).intValue());
			}
			if(secCallback.canReview(application)) {
				boolean reviewed = reviewedApps.contains(application.getKey());
				forgeReviewButton(row, reviewed);
				row.setReviewed(reviewed);
			}
			
			UserRating currentRating = null;
			int numOfAssignedRatings = 0;
			String resSubPath = application.getKey().toString();
			for(UserRating rating:ratings) {
				if(resSubPath.equals(rating.getResSubPath())) {
					Long raterKey = rating.getCreator().getKey();
					if(getIdentity().getKey().equals(raterKey)) {
						currentRating = rating;
					}
					
					if(assignmentKeys.containsKey(new AssignmentKey(raterKey, application.getKey()))) {
						numOfAssignedRatings++;
					}
				} 
			}
			row.setCurrentRating(currentRating);
			row.setNumOfAssignedRatings(numOfAssignedRatings);
			
			Long numOfAssignments = assignmentsPerApplications.get(application.getKey());
			if(numOfAssignments != null && numOfAssignments.longValue() > 0l) {
				List<ApplicationAssignmentLight> assignments = applicationToAssignments.get(application.getKey());
				String[] assigneeArray = new String[assignments.size()];
				String[] assigneeKeyArray = new String[assignments.size()];
				for(int i=assignments.size(); i-->0; ) {
					assigneeArray[i] = userManager.getUserDisplayName(assignments.get(i).getAssigneeKey());
					assigneeKeyArray[i] = assignments.get(i).getAssigneeKey().toString();
				}
				row.setAssigneeArray(assigneeArray);
				row.setAssigneeKeysArray(assigneeKeyArray);
				row.setNumOfAssignments(numOfAssignments.intValue());
			} else {
				row.setNumOfAssignments(0);
			}

			userRatingMapper.setRating(currentRating);
			
			if(allowToRate && row.isAllowed()) {
				forgeRatingItem(row, currentRating);
			}
			
			SentEmailTemplates sentMailTemplates = emailTemplatesMap.get(application.getKey());
			String[] mailTemplatesNames = calculateMailTemplates(sentMailTemplates);
			row.setSentEmailTemplates(mailTemplatesNames);
	
			if(secCallback.canSeeCommitteeRatings()) {
				forgeRatingsOverviewItem(row, committee, ratings, userRatingMapper);
			}
			rows.add(row);
		}
		
		applicationsDataModel.setData(rows, ratings);

		if(exportAllCombinedPdfLink != null) {
			exportAllCombinedPdfLink.setEnabled(!applications.isEmpty());
		}
		
		setRatingDeadline();
		
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			Map<Long,ApplicationRow> rowMap = rows.stream()
					.collect(Collectors.toMap(ApplicationRow::getKey, r -> r, (u,v) -> v));
			boolean allowedAdministrative = secCallback.canSeeApplicationAdministrativeCategories();
			List<ApplicationCategoryInfos> tags = taggingService.getApplicationCategories(position, allowedAdministrative);
			for(ApplicationCategoryInfos tag:tags) {
				ApplicationRow row = rowMap.get(tag.getApplicationKey());
				if(row != null) {
					row.addCategorie(tag.getCategory(), tag.isAdministrative());
				}
			}
			
			for(ApplicationRow row:rows) {
				List<AppToCategory> categories = row.getCategories();
				if(categories != null && categories.size() > 1) {
					Collections.sort(categories, new AppToCategoryComparator());
				}
			}
		}
		
		if(secCallback.canRate() && !recruitingModule.isApplicationAssignmentsEnabled()) {
			applicationAssignments = generateTransientAssignments();
		}
		loadStatisticsModel(applicationAssignments, ratings);
		
		applicationsDataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, false);
	}
	
	private Map<String,List<ParallelApplication>> loadParalellApplications() {
		Map<String,List<ParallelApplication>> apps = new HashMap<>();
		if(secCallback.canViewParalellApplications()) {
			ParallelApplicationScope scope = recruitingModule.getParallelApplicationScope();
			List<ParallelApplication> parallelApps =  recruitingService.getParallelApplications(position, scope);
			for(ParallelApplication parallelApp:parallelApps) {
				String email = parallelApp.getApplicationEmail();
				email = email.toLowerCase();
				apps.computeIfAbsent(email, m -> new ArrayList<>(1))
					.add(parallelApp);
			}	
		}
		return apps;
	}
	
	private Object[] localizedAdditionalValues(String[] rawValues, PositionAttributeDefinitionConfiguration[] selectConfigurations) {
		if(rawValues == null) return null;
		
		Object[] values = new Object[rawValues.length];
		for(int i=0; i<rawValues.length; i++) {
			if(i < selectConfigurations.length && selectConfigurations[i] != null) {	
				values[i] = ApplicationAttributesDelegate.getLocalizedValuesWithOthers(selectConfigurations[i], rawValues[i], getLocale());
			} else {
				values[i] = rawValues[i];
			}
		}
		return values;
	}
	
	private String[] calculateMailTemplates(SentEmailTemplates sentMailTemplates) {
		if(sentMailTemplates != null && sentMailTemplates.getTemplates() != null) {
			String[] templates = sentMailTemplates.getTemplates();
			List<String> templateNames = new ArrayList<>(templates.length * 2);
			for(int i=templates.length; i-->0; ) {
				String template = templates[i];
				if(StringHelper.containsNonWhitespace(template)) {
					if("-".equals(template)) {
						templateNames.add(translate("filter.no.template"));
					} else if(recruitingModule.isMailTemplateTitle(template)) {
						String label = mailTranslator.translate("rejection.template.label.".concat(template.toLowerCase()));
						if(label.length() < 32) {
							templateNames.add(label);
						} else {
							templateNames.add(translate("filter.no.template"));
						}
					} else {
						boolean matched = false;
						for(PositionMailTemplateRef mailTemplateRef:mailTemplates) {
							if(mailTemplateRef.match(template)) {
								templateNames.add(mailTemplateRef.getName());
								templateNames.add(template);
								matched = true;
							}
						}
						
						if(!matched) {
							templateNames.add(translate("filter.no.template"));
						}
					}
				} else {
					templateNames.add(translate("filter.no.template"));
				}
			}
			return templateNames.toArray(new String[templateNames.size()]);
		}
		return new String[0];	
	}

	private void loadStatisticsModel() {
		if(!secCallback.canRate()) return;
		
		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();

		List<? extends IdentityRef> committee = recruitingService.getCommitteeRefs(position, ratingRoles);
		List<UserRating> ratings = recruitingService.getRatings(position, committee);
		
		List<ApplicationAssignmentLight> applicationAssignments;
		if(recruitingModule.isApplicationAssignmentsEnabled()) {
			applicationAssignments = assignmentService.getAssignments(position);
		} else {
			applicationAssignments = generateTransientAssignments();
		}
		loadStatisticsModel(applicationAssignments, ratings);
	}
	
	private List<ApplicationAssignmentLight> generateTransientAssignments() {
		final Long assigneeKey = getIdentity().getKey();
		final List<ApplicationAssignmentLight> applicationAssignments = new ArrayList<>(applications.size());
		for(ApplicationLight application:applications) {
			applicationAssignments.add(new ApplicationAssignmentLightTransient(assigneeKey, application.getKey()));
		}
		return applicationAssignments;
	}

	private void loadStatisticsModel(List<ApplicationAssignmentLight> applicationAssignments, List<UserRating> ratings) {
		if(!secCallback.canRate()) return;
		
		Long myIdentityKey = getIdentity().getKey();
		ApplicationStatistics myStatistics = new ApplicationStatistics();
		ApplicationStatistics allStatistics = new ApplicationStatistics();
		
		Map<AssignmentKey, AssignmentKey> assignmentKeys = applicationAssignments.stream()
			.map(assignment -> new AssignmentKey(assignment.getAssigneeKey(), assignment.getApplicationKey()))
			.collect(Collectors.toMap(key -> key, key -> key, (u, v) -> v));
		
		Map<AssignmentKey, UserRating> userRatingKeys = ratings.stream()
				.collect(Collectors.toMap(rating -> new AssignmentKey(rating.getCreator().getKey(), Long.valueOf(rating.getResSubPath())),
						rating -> rating, (u, v) -> v));

		for(ApplicationLight application:applications) {
			if(application.getApplicationStatus() != ApplicationStatus.active) {
				continue;
			}
			
			AssignmentKey myKey = new AssignmentKey(myIdentityKey, application.getKey());
			boolean assigned = assignmentKeys.containsKey(myKey);
			
			if(assigned) {
				myStatistics.incrementNumOfAssignments();
			}
			allStatistics.incrementNumOfApplications();
			
			UserRating userRating = userRatingKeys.get(myKey);
			if(userRating == null || userRating.getRating() == null) {
				if(application.getDecision() != null && application.getDecision().intValue() > 0) {
					allStatistics.incrementNotActive();
					if(assigned) {
						myStatistics.incrementNotActive();
					}
				} else {
					allStatistics.incrementNotRated();
					if(assigned) {
						myStatistics.incrementNotRated();
					}
				}
			} else {
				int rating = userRating.getRating().intValue();
				if(RecruitingService.ABSTENTION == rating) {
					allStatistics.incrementAbstention();
					if(assigned) {
						myStatistics.incrementAbstention();
					}
				} else if(rating > 0) {
					allStatistics.incrementRating(rating - 1);
					if(assigned) {
						myStatistics.incrementRating(rating - 1);
					}
				}
			}	
		}
		
		String myMessage = translate("assignment.my.statistics", new String[] {
				Integer.toString(myStatistics.getAssignmentsDone()), Integer.toString(myStatistics.getNumOfAssignments())
			});
		
		String allMessage = translate("assignment.all.statistics", new String[] {
				Integer.toString(allStatistics.getApplicationsDone()), Integer.toString(allStatistics.getNumOfApplications())
			});
		
		myStatisticsItem.setWidth(myStatistics.getNumOfAssignments());
		List<BarItem> myItems = new ArrayList<>();
		myItems.add(new BarItem(translate("rating.2"), "progress-bar-success", myStatistics.getRating(2)));
		myItems.add(new BarItem(translate("rating.1"), "o_bar_rating_on", myStatistics.getRating(1)));
		myItems.add(new BarItem(translate("rating.0"), "o_bar_rating_on", myStatistics.getRating(0)));
		myItems.add(new BarItem(translate("abstain.title"), "o_bar_rating_on o_bar_rating_on o_bar_rating_abstain", myStatistics.getAbstention()));
		myItems.add(new BarItem("", "progress-bar-info", myStatistics.getNotActive()));
		myStatisticsItem.setBarItems(myItems);
		
		allStatisticsItem.setWidth(allStatistics.getNumOfApplications());
		List<BarItem> allItems = new ArrayList<>();
		allItems.add(new BarItem(translate("rating.2"), "progress-bar-success", allStatistics.getRating(2)));
		allItems.add(new BarItem(translate("rating.1"), "o_bar_rating_on", allStatistics.getRating(1)));
		allItems.add(new BarItem(translate("rating.0"), "o_bar_rating_on", allStatistics.getRating(0)));
		allItems.add(new BarItem(translate("abstain.title"), "o_bar_rating_on o_bar_rating_on o_bar_rating_abstain", allStatistics.getAbstention()));
		allItems.add(new BarItem("", "progress-bar-info", allStatistics.getNotActive()));
		allStatisticsItem.setBarItems(allItems);
		
		statisticsContainer.contextPut("allStatisticsMsg", allMessage);
		statisticsContainer.contextPut("allStatistics", allStatistics);

		statisticsContainer.contextPut("myStatisticsMsg", myMessage);
		statisticsContainer.contextPut("myStatistics", myStatistics);
		statisticsContainer.setVisible(true);

		if(myStatistics.getNumOfAssignments() == 0 || !recruitingModule.isApplicationAssignmentsEnabled()) {
			myStatisticsItem.setVisible(false);
			allStatisticsItem.setVisible(true);
		} else {
			boolean my = ((Boolean)toggleAssignmentStatisticsLink.getUserObject()).booleanValue();
			myStatisticsItem.setVisible(my);
			allStatisticsItem.setVisible(!my);
		}
	}
	
	private void doToogleAssignmentsStatistics() {
		boolean my = !((Boolean)toggleAssignmentStatisticsLink.getUserObject()).booleanValue();

		String label = my ? translate("toogle.assignments.statistics.all") : translate("toogle.assignments.statistics.my");
		
		toggleAssignmentStatisticsLink.getComponent().setCustomDisplayText(label);
		toggleAssignmentStatisticsLink.setUserObject(Boolean.valueOf(my));
		myStatisticsItem.setVisible(my);
		allStatisticsItem.setVisible(!my);
	}
	
	private RatingsOverviewFormItem forgeRatingsOverviewItem(ApplicationRow row, List<? extends IdentityRef> committee, List<UserRating> ratings,
			UserRatingMapper userRatingMapper) {
		
		String ratingsKey = "rating-o-" + row.getKey();
		RatingsOverviewFormItem overviewItem = (RatingsOverviewFormItem)tableEl.getFormComponent(ratingsKey);
		if(overviewItem == null) {
			overviewItem = new RatingsOverviewFormItem(ratingsKey);
			overviewItem.setDomReplacementWrapperRequired(false);
		} else {
			overviewItem.getComponent().setDirty(true);
		}

		String resSubPath = row.getApplication().getKey().toString();
		List<UserRating> appRatings = new ArrayList<>();
		for(IdentityRef member:committee) {
			UserRating memberRating = null;
			for(UserRating rating:ratings) {
				if(resSubPath.equals(rating.getResSubPath()) && member.getKey().equals(rating.getCreator().getKey())) {
					memberRating = rating;
					break;
				}
			}
			if(memberRating == null) {
				memberRating = new EmptyUserRating(member);
			}
			appRatings.add(memberRating);
		}

		Collections.sort(appRatings, ratingComparator);
		overviewItem.setRatings(appRatings);
		userRatingMapper.setRatings(appRatings);
		row.setRatingOverviewItem(overviewItem);
		return overviewItem;
	}
	
	private CustomRatingFormItem forgeRatingItem(ApplicationRow row, UserRating currentRating) {
		String ratingKey = "rating-" + row.getKey();
		float currentRatingVal = currentRating == null || currentRating.getRating() == null ? 0.0f : currentRating.getRating().floatValue();

		CustomRatingFormItem ratingItem = (CustomRatingFormItem)tableEl.getFormComponent(ratingKey);
		if(ratingItem == null) {
			ratingItem = new CustomRatingFormItem(ratingKey, currentRatingVal,
					recruitingModule.getMaxRating(), true, recruitingModule.isRatingAbstentionEnabled(), getTranslator());
			ratingItem.setDomReplacementWrapperRequired(false);
			ratingItem.setLevelLabel(0, "rating.0");
			ratingItem.setLevelLabel(1, "rating.1");
			ratingItem.setLevelLabel(2, "rating.2");
		} else {
			ratingItem.setCurrentRating(currentRatingVal);
		}
		ratingItem.setUserObject(row);
		row.setRatingItem(ratingItem);
		return ratingItem;
	}
	
	private FormLink forgeReviewButton(ApplicationRow row, boolean reviewed) {
		String reviewKey = "review_" + row.getKey();
		String i18nLink = reviewed ? "edit.review.table" : "add.review.table";
		FormLink reviewButton = (FormLink)tableEl.getFormComponent(reviewKey);
		if(reviewButton == null) {
			reviewButton = uifactory.addFormLink(reviewKey, "review", i18nLink, null,
				flc, Link.BUTTON_XSMALL);
			reviewButton.setDomReplacementWrapperRequired(false);
		} else if(!i18nLink.equals(reviewButton.getI18nKey()))  {
			reviewButton.setI18nKey(i18nLink);
			reviewButton.getComponent().setDirty(true);
		}
		reviewButton.setUserObject(row);
		row.setReviewButton(reviewButton);
		return reviewButton;
	}
	
	private boolean isCommitteeRatingsColumnDefault() {
		return (!secCallback.canRate() || 
			(secCallback.canRate() && position.getStatus() != null
				&& PositionStatus.closedAndNoRating.name().equals(position.getStatus())));
	}
	
	private boolean isAllowedToRate(Position position) {
		String statusStr = position.getStatus();
		//no status -> can't rate
		if(!StringHelper.containsNonWhitespace(statusStr)) {
			return false;
		}
		PositionStatus status = PositionStatus.valueOf(statusStr);
		//not in screening -> can't rate
		if(!PositionStatus.publishedAndInScreening.equals(status) && !PositionStatus.closedAndInScreening.equals(status)) {
			return false;
		}
		//not in committee -> can't rate
		return secCallback.canRate();
	}

	@Override
	public void event(Event event) {
		if(FinalDecisionChangeEvent.FINAL_DECISION.equals(event.getCommand())
				&& event instanceof FinalDecisionChangeEvent) {
			doProcess((FinalDecisionChangeEvent)event);
		} else if(event instanceof RatingChangedEvent) {
			doProcess((RatingChangedEvent)event);
		}
	}
	
	private void doProcess(RatingChangedEvent changeEvent) {
		ApplicationRef app = changeEvent.getApplication();
		if(changeEvent.getDoerIdentityKey() != null
				&& changeEvent.getDoerIdentityKey().equals(getIdentity().getKey())
				&& !"pos".equals(changeEvent.getEmitter())) {
			ApplicationRow row = applicationsDataModel.getApplicationRow(app);
			if(row != null && row.getRatingItem() != null) {
				UserRating rating = recruitingService.getRating(position, row.getApplication(), getIdentity());
				float currentRatingVal = rating == null || rating.getRating() == null ? 0.0f : rating.getRating().floatValue();
				row.getRatingItem().setCurrentRating(currentRatingVal);
				row.getRatingItem().getComponent().setDirty(true);
			}
			return;
		} else if(!secCallback.canSeeCommitteeRatings()) {
			return;
		}
			
		ApplicationRow row = applicationsDataModel.getApplicationRow(app);
		if(row != null) {
			PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
			List<IdentityRef> committee = recruitingService.getCommitteeRefs(position, ratingRoles);
			List<UserRating> ratings = recruitingService.getRatings(position, app, committee);
			
			forgeRatingsOverviewItem(row, committee, ratings, row.getUserRatingMapper());
		}
	}
	
	private void doProcess(FinalDecisionChangeEvent changeEvent) {
		synchronized(applicationsDataModel.decisionLock) {
			applicationsDataModel.setDecision(changeEvent.getApplicationKey(), changeEvent.getDecision());
		}
		if(appController != null) {
			listChanged = true;
		} else {
			tableEl.getComponent().setDirty(true);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(appController != null) {
				stackPanel.popController(appController);
				removeAsListenerAndDispose(appController);
				appController = null;
			}
			if(tableEl.getSelectedFilterTab() == null) {
				tableEl.setSelectedFilterTab(ureq, allTab);
			}
			
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Assignments".equalsIgnoreCase(type)) {
				tableEl.setSelectedFilterTab(ureq, myAssignmentsTab);
				//TODO selectus filter
			} else if(tableEl.getSelectedFilterTab() == null) {
				tableEl.setSelectedFilterTab(ureq, allTab);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(batchDecisionButton == source) {
			doSetDecision(ureq);
		} else if(batchCategoriesButton == source) {
			doCategories(ureq);
		} else if(batchStatusButton == source) {
			doChangeStatus(ureq);
		} else if(generateListButton == source) {
			doGenerateList(ureq);
		} else if(addAssignmentButton == source) {
			doAddAssignments(ureq);
		} else if(removeAssignmentButton == source) {
			doRemoveAssignments(ureq);
		} else if(toggleAssignmentStatisticsLink == source) {
			doToogleAssignmentsStatistics();
		} else if(batchSendApplicationEmailsButton == source) {
			doSendApplicationEmails(ureq);
		} else if(addComparativeExpertsButton == source) {
			doAddComparativeExpert(ureq);
		} else if(batchSendReferencesEmailsButton == source) {
			doSendReferenceEmail(ureq);
		} else if(batchAddFeedbackMembersButton == source) {
			doAddMembersToApplicationsFeedback(ureq);
		} else if(batchContactFeedbackMembersButton == source) {
			doContactMembersOfApplicationsFeedback(ureq);
		} else if(batchRemoveFeedbackMembersButton == source) {
			doRemoveMembersOfApplicationsFeedback(ureq);
		} else if(copyApplicationsButton == source) {
			doCopyApplications(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("delete".equals(se.getCommand())) {
					ApplicationRow row = applicationsDataModel.getObject(se.getIndex());
					doConfirmDelete(ureq, row.getApplication());
				} else if("edit".equals(se.getCommand())) {
					ApplicationRow row = applicationsDataModel.getObject(se.getIndex());
					editApplication(ureq, row.getApplication());
				} else if(SELECT_POSITION.equals(se.getCommand())) {
					ApplicationRow row = applicationsDataModel.getObject(se.getIndex());
					selectApplication(ureq, row.getApplication(), false, null);
				} else if ("notes".equals(se.getCommand())) {
					ApplicationRow row = applicationsDataModel.getObject(se.getIndex());
					editNotes(ureq, row.getApplication());
				}
			} else if(event instanceof FlexiTableSearchEvent ftse) {
				applicationsDataModel.filter(ftse.getSearch(), ftse.getFilters());
				tableEl.reset(true, true, false);
			} else if(event instanceof FlexiTableFilterTabEvent) {
				applicationsDataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, false);
			}
		} else if(source instanceof CustomRatingFormItem) {
			if(event instanceof RatingFormEvent) {
				RatingFormEvent e = (RatingFormEvent)event;
				doRating(e, (CustomRatingFormItem)source);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("review".equals(link.getCmd())) {
				doEditReview(ureq, (ApplicationRow)link.getUserObject());
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == appController) {
					removeAsListenerAndDispose(appController);
					appController = null;
					if(listChanged) {
						tableEl.reloadData();
						listChanged = false;
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editApplicationCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				//do nothing
			} else if (event == Event.CHANGED_EVENT) {
				loadModel(position);
			}	else if (event == Event.DONE_EVENT){
				loadModel(position);
			}
			editApplicationDialogBox.deactivate();
			doCleanUp();
		} else if (source == editApplicationDialogBox) {
			doCleanUp();
		} else if(source == notesDialogBox) {
			doCleanUp();
		} else if (source == notesController) {
			//reload
			if(event == Event.DONE_EVENT) {
				doUpdateNotes(notesController.getNotes());
			}
			notesDialogBox.deactivate();
			doCleanUp();
		} else if(addComparativeExpertCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event == Event.CANCELLED_EVENT) {
				loadModel(position);
			}
			cmc.deactivate();
			doCleanUp();
		} else if(source == confirmDeleteBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				ApplicationLight app = (ApplicationLight)confirmDeleteBox.getUserObject();
				doDelete(app);
				loadModel(position);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == appController) {
			if(Event.CHANGED_EVENT == event) {
				loadModel(position);
			} else if(event instanceof DecisionEvent) {
				loadModel(position);
			}  else if(event instanceof ApplicationChangeEvent) {
				Application currentApp = appController.getApplication();
				//update the GUI
				loadModel(position);
				for(ApplicationLight application:applications) {
					if(application.getKey().equals(currentApp.getKey())) {
						selectApplication(ureq, application, true, null);
						break;
					}
				}
			} else if (event instanceof PositionApplicationEvent && (PositionApplicationEvent.NEXT.equals(event.getCommand()) || 
					PositionApplicationEvent.PREVIOUS.equals(event.getCommand()))) {
				PositionApplicationEvent posEvent = (PositionApplicationEvent)event;
				Long appKey = posEvent.getAppKey();
				List<Long> sortedKeys = posEvent.getSortedAppKeys();
				for(ApplicationLight application:applications) {
					if(application.getKey().equals(appKey)) {
						nextPreviousApplication(ureq, application, sortedKeys);
						break;
					}
				}
			} else if (PositionApplicationEvent.ALL.equals(event)) {
				stackPanel.popController(appController);
			}
		} else if(batchDecisionCtrl == source || editReviewCtrl == source
				|| batchAddCategoriesCtrl == source || batchApplicationStatusCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(position);
			}
			cmc.deactivate();
			doCleanUp();
		} else if(generateListCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				MediaResource rsrc = generateListCtrl.getMediaResource();
				if(rsrc == null) {
					showError("generate.application.list.error");
				} else {
					ureq.getDispatchResult().setResultingMediaResource(rsrc);
				}
			}
			doCleanUp();
		} else if(addAssignmentWizardController == source
				|| removeAssignmentWizardController == source
				|| sendApplicationEmailController == source
				|| sendReferencesEmailController == source
				|| addMembersFeeadbackWizard == source
				|| removeMembersFeeadbackWizard == source
				|| contactMembersFeeadbackWizard == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				//reload the list
				loadModel(position);
			}
			doCleanUp();
		} else if(copyApplicationsWizard == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
			}
			doCleanUp();
		} else if(cmc == source) {
			doCleanUp();
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void doCleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(generateListCtrl);
		removeAsListenerAndDispose(notesDialogBox);
		removeAsListenerAndDispose(notesController);
		removeAsListenerAndDispose(editApplicationCtrl);
		removeAsListenerAndDispose(editApplicationDialogBox);
		removeAsListenerAndDispose(batchDecisionCtrl);
		removeAsListenerAndDispose(batchAddCategoriesCtrl);
		removeAsListenerAndDispose(editReviewCtrl);
		removeAsListenerAndDispose(copyApplicationsWizard);
		removeAsListenerAndDispose(addComparativeExpertCtrl);
		removeAsListenerAndDispose(addMembersFeeadbackWizard);
		removeAsListenerAndDispose(removeMembersFeeadbackWizard);
		removeAsListenerAndDispose(contactMembersFeeadbackWizard);
		removeAsListenerAndDispose(sendReferencesEmailController);
		removeAsListenerAndDispose(sendApplicationEmailController);
		removeAsListenerAndDispose(addAssignmentWizardController);
		removeAsListenerAndDispose(removeAssignmentWizardController);
		cmc = null;
		editReviewCtrl = null;
		notesDialogBox = null;
		notesController = null;
		generateListCtrl = null;
		batchDecisionCtrl = null;
		editApplicationCtrl = null;
		copyApplicationsWizard = null;
		batchAddCategoriesCtrl = null;
		editApplicationDialogBox = null;
		addComparativeExpertCtrl = null;
		addMembersFeeadbackWizard = null;
		removeMembersFeeadbackWizard = null;
		addAssignmentWizardController = null;
		contactMembersFeeadbackWizard = null;
		sendReferencesEmailController = null;
		sendApplicationEmailController = null;
		removeAssignmentWizardController = null;
	}
	
	//TODO selectus
	/*
	private void doFilter(FlexiTableAdvancedFilter filter) {
		String query = filter.getAsQuery();
		applicationsDataModel.flexiSearch(tableEl.getQuickSearchString(), query);
		if(filter.equals(myAssignmentsFilter)) {
			tableEl.sort(Fields.myAssignment.name(), true);
		}
	}
	*/
	
	private void doRating(RatingFormEvent e, CustomRatingFormItem source) {
		ApplicationRow row = (ApplicationRow)source.getUserObject();
		ApplicationLight application = row.getApplication();
				
		try {
			boolean remove = false;
			Integer currentRating = null;
			UserRating rating = row.getCurrentRating();
			if(rating != null && rating.getRating() != null) {
				currentRating = rating.getRating();
				float selectedRating = e.getRating();
				float diff = currentRating.floatValue() - selectedRating;
				if(Math.abs(diff) <= 0.001) {
					remove = true;
				}
			}
			
			if(remove) {
				recruitingService.removeRating(position, application, getIdentity());
				source.reset();
				row.setCurrentRating(null);
				
				//log
				String messageI18n = "audit.log.rating.remove";
				String beforeRating = currentRating == null ? "-" : currentRating.toString();
				String[] messageArgs = new String[] { translateRating(currentRating), null,
						salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
				auditService.auditRatingLog(Action.remove, ActionTarget.rating, beforeRating, null,
						messageI18n, messageArgs, getTranslator(), position, application, null, getIdentity());
			} else {
				int newRating = Float.valueOf(e.getRating()).intValue();
				rating = recruitingService.setRating(position, application, getIdentity(), newRating);
				row.setCurrentRating(rating);
				
				//log
				if(currentRating == null) {
					String messageI18n = "audit.log.rating.add";
					String[] messageArgs = new String[] { "", translateRating(newRating),
							salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
					auditService.auditRatingLog(Action.add, ActionTarget.rating, null, translateRating(newRating),
							messageI18n, messageArgs, getTranslator(), position, application, rating, getIdentity());
				} else {
					String messageI18n = "audit.log.rating.update";
					String beforeRating = currentRating == null ? "-" : currentRating.toString();
					String[] messageArgs = new String[] { translateRating(currentRating), translateRating(newRating),
							salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
					auditService.auditRatingLog(Action.update, ActionTarget.rating, beforeRating, Integer.toString(newRating),
							messageI18n, messageArgs, getTranslator(), position, application, rating, getIdentity());
				}
			}
			
			RatingChangedEvent changedEvent = new RatingChangedEvent(application, getIdentity().getKey(), "pos");
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(changedEvent, positionOres);
			
			loadStatisticsModel();
		} catch (RatingClosedException rce) {
			showError("rating.closed.error");
		}
	}
	
	private String translateRating(Integer rating) {
		if(rating == null) {
			return "-";
		}
		if(rating.intValue() > 0 && rating.intValue() < 3) {
			return translate("rating." + (rating - 1));
		}
		if(rating.intValue() == RecruitingService.ABSTENTION) {
			return translate("abstain.title");
		}
		return "";
	}
	
	private void doAddAssignments(UserRequest ureq) {
		removeAsListenerAndDispose(addAssignmentWizardController);
		
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			List<ApplicationLight> apps = selectedApplications.stream()
					.map(ApplicationRow::getApplication)
					.collect(Collectors.toList());

			AssignmentsData data = new AssignmentsData(position, apps, AssignmentMethods.manual);
			Step start = new AddAssignment1CommitteeStep(ureq, data);
			AddAssignmentStepCallback finish = new AddAssignmentStepCallback(apps, data, getTranslator());
		
			String title;
			if(selectedApplications.size() == 1) {
				title = translate("add.assignment.wizard.title");
			} else {
				title = translate("add.assignment.wizard.title.plural", new String[] { Integer.toString(selectedApplications.size()) });
			}		
			addAssignmentWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(addAssignmentWizardController);
			
			getWindowControl().pushAsModalDialog(addAssignmentWizardController.getInitialComponent());
		}
	}
	
	private void doRemoveAssignments(UserRequest ureq) {
		removeAsListenerAndDispose(removeAssignmentWizardController);
		
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			List<ApplicationLight> apps = selectedApplications.stream()
					.map(ApplicationRow::getApplication)
					.collect(Collectors.toList());
			AssignmentsData data = new AssignmentsData(position, apps, AssignmentMethods.manual);
			Step start = new RemoveAssignment1CommitteeStep(ureq, data);
			RemoveAssignmentStepCallback finish = new RemoveAssignmentStepCallback(apps, data, getTranslator());
		
			String title;
			if(selectedApplications.size() == 1) {
				title = translate("remove.assignment.wizard.title");
			} else {
				title = translate("remove.assignment.wizard.title.plural", new String[] { Integer.toString(selectedApplications.size()) });
			}		
			removeAssignmentWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(removeAssignmentWizardController);
			
			getWindowControl().pushAsModalDialog(removeAssignmentWizardController.getInitialComponent());
		}
	}
	
	private void doAddComparativeExpert(UserRequest ureq) {
		removeAsListenerAndDispose(addComparativeExpertCtrl);
		
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			List<Application> applicationsToCompare = new ArrayList<>(selectedApplications.size());
			for(ApplicationRow selectedApplication:selectedApplications) {
				Application app = recruitingService.getApplicationByKey(selectedApplication.getKey());
				if(app != null) {
					applicationsToCompare.add(app);
				}
			}
			addComparativeExpertCtrl = new ApplicationReferenceEditController(ureq, getWindowControl(), position,
					null, applicationsToCompare, ReferenceType.comparativeAssessmentExpert, secCallback);
			listenTo(addComparativeExpertCtrl);
			
			String title = translate("add.comparative.expert.title");
			cmc = new CloseableModalController(getWindowControl(), "c", addComparativeExpertCtrl.getInitialComponent(), title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doSendApplicationEmails(UserRequest ureq) {
		removeAsListenerAndDispose(sendApplicationEmailController);
		
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			final EmailVariables emailVar = mailService.getEmailVariables(position, getLocale());
			emailVar.setShowAttachmentWarning(true);
			
			List<ApplicationLight> apps = selectedApplications.stream()
					.map(ApplicationRow::getApplication).collect(Collectors.toList());
			emailVar.setRows(apps);
			emailVar.setSelectedApps(apps);
			CEmail_2_OverviewStep start = new CEmail_2_OverviewStep(ureq, emailVar, secCallback);
			StepRunnerCallback finish = new SendEmailRunnerCallback(getIdentity(), emailVar, secCallback, getTranslator());
					
			String title = translate("rejection.wizard.title");
			sendApplicationEmailController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(sendApplicationEmailController);
			
			getWindowControl().pushAsModalDialog(sendApplicationEmailController.getInitialComponent());
		}
	}
	
	private void doSendReferenceEmail(UserRequest ureq) {
		removeAsListenerAndDispose(sendReferencesEmailController);
		
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			final InvitationVariables invitationVar = mailService.getInvitationVariables(position, getLocale());
			List<ApplicationLight> apps = selectedApplications.stream()
					.map(ApplicationRow::getApplication).collect(Collectors.toList());
			invitationVar.setApplications(apps);
			Step start = new InvitationEmail_0_FilterStep(ureq, invitationVar);
			StepRunnerCallback finish = new SendReferencesEmailRunnerCallback(invitationVar);
	
			removeAsListenerAndDispose(sendReferencesEmailController);
			sendReferencesEmailController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("reference.wizard.title"), null);
			listenTo(sendReferencesEmailController);
			
			getWindowControl().pushAsModalDialog(sendReferencesEmailController.getInitialComponent());
		}
	}
	
	private void doUpdateNotes(Notes notes) {
		ApplicationRef appRef = new ApplicationRefImpl(notes.getApplicationKey());
		ApplicationRow appRow = applicationsDataModel.getApplicationRow(appRef);
		if(appRow != null) {
			appRow.setNotes(notes);
		}
		tableEl.reset(false, false, true);
	}
	
	private void doEditReview(UserRequest ureq, ApplicationRow row) {
		if(guardModalController(editReviewCtrl)) return;
		
		Application application = recruitingService.getApplicationByKey(row.getApplication().getKey());
		editReviewCtrl = new ReviewEditController(ureq, getWindowControl(), position, application);
		listenTo(editReviewCtrl);
		
		String title = translate("edit.review");
		cmc = new CloseableModalController(getWindowControl(), "c", editReviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, ApplicationLight application) {
		String title = translate("confirm.delete.application.title");
		String name = RecruitingHelper.formatFullName(application, getTranslator());
		String text = translate("confirm.delete.application", new String[]{name});
		confirmDeleteBox = activateYesNoDialog(ureq, title, text, confirmDeleteBox);
		confirmDeleteBox.setUserObject(application);
	}
	
	private void doDelete(ApplicationLight app) {
		Application reloadedApp = recruitingService.getApplicationByKey(app.getKey());
		
		String before = auditService.toAuditXml(reloadedApp);
		String messageI18n = "audit.log.application.deleted";
		String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(reloadedApp, getLocale()), reloadedApp.getId().toString() };
		
		recruitingService.deleteApplication(reloadedApp);

		auditService.auditApplicationLog(Action.delete, ActionTarget.application, before, null, messageI18n, messageArgs, getTranslator(), position, reloadedApp, getIdentity());
		logAudit("Application deleted: " + reloadedApp.toString(), null);
	}
	
	private void editNotes(UserRequest ureq, ApplicationLight application) {
		removeAsListenerAndDispose(notesController);
		removeAsListenerAndDispose(notesDialogBox);

		notesController = new NotesController(ureq, getWindowControl(), application);
		listenTo(notesController);
		
		notesDialogBox = new CloseableModalController(getWindowControl(), "c", notesController.getInitialComponent(), translate("edit.notes"));
		notesDialogBox.activate();
		listenTo(notesDialogBox);		
	}
	
	protected void selectApplication(UserRequest ureq, ApplicationRef appRef, boolean update, List<ContextEntry> entries) {
		Application app = recruitingService.getApplicationByKey(appRef.getKey());
		if(app == null) {
			showWarning("warning.application.deleted");
			loadModel(position);
			return;
		}

		String crumbText = RecruitingHelper.formatFullName(app, getTranslator());
		stackPanel.popController(appController);
		removeAsListenerAndDispose(appController);
		
		int numOfApplications = applicationsDataModel.getRowCount();
		int currentPosition = applicationsDataModel.getApplicationIndex(app);
		
		List<Long> sortedKeys = new ArrayList<>(numOfApplications);
		for(int i=0; i<numOfApplications; i++) {
			ApplicationRow sortedApp = applicationsDataModel.getObject(i);
			if(sortedApp != null && sortedApp.getApplication() != null) {
				sortedKeys.add(sortedApp.getApplication().getKey());
			}
		}
		
		appController = new ApplicationOverviewController(ureq, getWindowControl(), stackPanel, position, app, currentPosition,
				sortedKeys, false, secCallback);
		appController.activate(ureq, entries, null);
		stackPanel.pushController(crumbText, appController);
		listenTo(appController);
		if(update) {
			fireEvent(ureq, new UpdateControllerEvent(crumbText, appController));
		} else {
			fireEvent(ureq, new PushControllerEvent(crumbText, appController));
		}
		addToHistory(ureq, OresHelper.createOLATResourceableInstance("Applications", app.getKey()), null);
	}
	
	private void nextPreviousApplication(UserRequest ureq, ApplicationLight app, List<Long> sortedKeys) {
		String crumbText = RecruitingHelper.formatFullName(app, getTranslator());
		stackPanel.popController(appController);
		removeAsListenerAndDispose(appController);

		int currentPosition = 0;
		for(Long appKey:sortedKeys) {
			if(appKey.equals(app.getKey())) {
				break;
			}
			currentPosition++;
		}

		Application reloadedApp = recruitingService.getApplicationByKey(app.getKey());
		appController = new ApplicationOverviewController(ureq, getWindowControl(), stackPanel, position, reloadedApp,
				currentPosition, sortedKeys, false, secCallback);
		stackPanel.pushController(crumbText, appController);
		listenTo(appController);
		fireEvent(ureq, new UpdateControllerEvent(crumbText, appController));
		addToHistory(ureq, OresHelper.createOLATResourceableInstance("Applications", app.getKey()), null);
	}
	
	private void editApplication(UserRequest ureq, ApplicationLight app) {
		removeAsListenerAndDispose(editApplicationCtrl);
		removeAsListenerAndDispose(editApplicationDialogBox);

		Application reloadedApp = recruitingService.getApplicationToEdit(app);
		editApplicationCtrl = new ApplicationEditController(ureq, getWindowControl(), reloadedApp, position, secCallback);
		listenTo(editApplicationCtrl);
		
		String title = translate("edit_application");
		editApplicationDialogBox = new CloseableModalController(getWindowControl(), "c", editApplicationCtrl.getInitialComponent(), title);
		editApplicationDialogBox.activate();
		listenTo(editApplicationDialogBox);
	}

	private void doSetDecision(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes == null || selectedIndexes.isEmpty()) {
			showWarning("batch.decision.atleastone");
			return;
		}
		
		List<ApplicationRow> selectedApplications = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			ApplicationRow row = applicationsDataModel.getObject(selectedIndex.intValue());
			selectedApplications.add(row);
		}

		batchDecisionCtrl = new BatchDecisionController(ureq, getWindowControl(), selectedApplications, position);
		listenTo(batchDecisionCtrl);
		
		String title = translate("batch.decision.title", new String[]{ Integer.toString(selectedApplications.size())} );
		cmc = new CloseableModalController(getWindowControl(), "c", batchDecisionCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doCategories(UserRequest ureq) {
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			batchAddCategoriesCtrl = new BatchApplicationCategoriesController(ureq, getWindowControl(), selectedApplications, position, secCallback);
			listenTo(batchAddCategoriesCtrl);
			
			String title = translate("batch.categories.title", new String[]{ Integer.toString(selectedApplications.size()) });
			cmc = new CloseableModalController(getWindowControl(), "c", batchAddCategoriesCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doChangeStatus(UserRequest ureq) {
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			batchApplicationStatusCtrl = new BatchApplicationStatusController(ureq, getWindowControl(), selectedApplications);
			listenTo(batchApplicationStatusCtrl);
			
			String i18n = selectedApplications.size() > 1 ? "batch.status.title.plural" : "batch.status.title.singular";
			String title = translate(i18n, new String[]{ Integer.toString(selectedApplications.size()) });
			cmc = new CloseableModalController(getWindowControl(), "c", batchApplicationStatusCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private List<ApplicationRow> getSelectedApplications() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<ApplicationRow> selectedApplications = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			ApplicationRow row = applicationsDataModel.getObject(selectedIndex.intValue());
			selectedApplications.add(row);
		}
		return selectedApplications;
	}
	
	private void doGenerateList(UserRequest ureq) {
		List<ApplicationRow> selectedApplications = getOrderedSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			generateListCtrl = new GenerateApplicationListController(ureq, getWindowControl(),
					position, selectedApplications, tableEl, applicationsDataModel, secCallback);
			listenTo(generateListCtrl);
			
			String title = translate("generate.application.list.title", new String[]{ Integer.toString(selectedApplications.size())} );
			cmc = new CloseableModalController(getWindowControl(), "c", generateListCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private List<ApplicationRow> getOrderedSelectedApplications() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<ApplicationRow> selectedApplications = new ArrayList<>(selectedIndexes.size());
		List<ApplicationRow> sortedApplications = applicationsDataModel.getObjects();
		int numOfApplications = sortedApplications.size();
		for(int i=0; i<numOfApplications; i++) {
			if(selectedIndexes.contains(Integer.valueOf(i))) {
				selectedApplications.add(sortedApplications.get(i));
			}
		}
		return selectedApplications;
	}
	
	private void doAddMembersToApplicationsFeedback(UserRequest ureq) {
		List<ApplicationRow> selectedApplications = getSelectedApplications();
		if(selectedApplications.isEmpty()) {
			showWarning("batch.decision.atleastone");
		} else {
			List<ApplicationsFeedbackConfiguration> configs = feedbackService.getApplicationsFeedbackConfigurations(position);
			if(configs.isEmpty()) {
				return;
			}
			ApplicationsFeedbackConfiguration defaultConfig = configs.get(0);
			
			List<ApplicationLight> apps = selectedApplications.stream()
					.map(ApplicationRow::getApplication).collect(Collectors.toList());
			
			String title;
			if(apps.size() == 1) {
				String applicationFullname = formatFullName(apps.get(0), getTranslator());
				title = translate("add.apps.feedbacks.member.title.singular", new String[] { applicationFullname });
			} else {
				title = translate("add.apps.feedbacks.member.title.plural", new String[] { Integer.toString(apps.size()) });		
			}

			Identity secretary = recruitingService.getSecretary(position);
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			RecruitingMailTemplate mailTemplate = FeedbackHelper.feedbackTemplate(headOfCommittee, secretary, position, apps,
					null, null, defaultConfig, salutationGenerator, getTranslator());
			
			FeedbackMembersContext feedbackMembersContext = new FeedbackMembersContext(position, defaultConfig,
					apps, secretary, headOfCommittee, mailTemplate);
			feedbackMembersContext.setDeadline(defaultConfig.getDeadline());
			Feedback1EmailStep start = new Feedback1EmailStep(ureq, feedbackMembersContext);
			AddFeedbacksMemberFinishCallback finish = new AddFeedbacksMemberFinishCallback(feedbackMembersContext, getTranslator());
			addMembersFeeadbackWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(addMembersFeeadbackWizard);
			getWindowControl().pushAsModalDialog(addMembersFeeadbackWizard.getInitialComponent());
		}
	}
	
	private void doContactMembersOfApplicationsFeedback(UserRequest ureq) {
		List<ApplicationRow> applicationRows = getSelectedApplications();
		List<ApplicationFeedback> feedbacks = feedbackService.getApplicationsFeedbacks(applicationRows);
		if(feedbacks.isEmpty()) {
			showWarning("warning.apps.feedbacks.atleastone");
		} else {
			ApplicationsFeedbackConfiguration defaultConfig = feedbacks.get(0).getConfiguration();
			List<Identity> members = feedbacks.stream()
					.map(ApplicationFeedback::getIdentity)
					.distinct()
					.collect(Collectors.toList());
			List<Application> apps = feedbacks.stream()
					.map(ApplicationFeedback::getApplication)
					.distinct()
					.collect(Collectors.toList());
			
			String title;
			if(members.size() == 1) {
				String memberFullname = RecruitingHelper.formatFullName(members.get(0));
				title = translate("contact.apps.feedbacks.member.title.singular", new String[] { memberFullname });
			} else {
				title = translate("contact.apps.feedbacks.member.title.plural", new String[] { Integer.toString(members.size()) });		
			}

			Identity secretary = recruitingService.getSecretary(position);
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			RecruitingMailTemplate mailTemplate = FeedbackHelper.feedbackTemplate(headOfCommittee, secretary, position, apps,
					null, null, defaultConfig, salutationGenerator, getTranslator());
			
			ContactMembersContext feedbackMembersContext = new ContactMembersContext(position, defaultConfig,
					feedbacks, secretary, headOfCommittee, mailTemplate);
			feedbackMembersContext.setDeadline(defaultConfig.getDeadline());
			Contact0FilterStep start = new Contact0FilterStep(ureq, feedbackMembersContext);
			ContactFeedbacksMemberFinishCallback finish = new ContactFeedbacksMemberFinishCallback(feedbackMembersContext, getTranslator());
			contactMembersFeeadbackWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(contactMembersFeeadbackWizard);
			getWindowControl().pushAsModalDialog(contactMembersFeeadbackWizard.getInitialComponent());
		}
	}
	
	private void doRemoveMembersOfApplicationsFeedback(UserRequest ureq) {
		List<ApplicationRow> applicationRows = getSelectedApplications();
		List<ApplicationFeedback> feedbacks = feedbackService.getApplicationsFeedbacks(applicationRows);
		if(feedbacks.isEmpty()) {
			showWarning("warning.apps.feedbacks.atleastone");
		} else {
			List<Identity> members = feedbacks.stream()
					.map(ApplicationFeedback::getIdentity)
					.distinct()
					.collect(Collectors.toList());

			String title;
			if(members.size() == 1) {
				String memberFullname = RecruitingHelper.formatFullName(members.get(0));
				title = translate("remove.apps.feedbacks.member.title.singular", new String[] { memberFullname });
			} else {
				title = translate("remove.apps.feedbacks.member.title.plural", new String[] { Integer.toString(members.size()) });		
			}
			
			RemoveMembersContext feedbackMembersContext = new RemoveMembersContext(position, feedbacks, members);
			Remove1SelectStep start = new Remove1SelectStep(ureq, feedbackMembersContext);
			RemoveFeedbacksMemberFinishCallback finish = new RemoveFeedbacksMemberFinishCallback(feedbackMembersContext, getTranslator());
			removeMembersFeeadbackWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(removeMembersFeeadbackWizard);
			getWindowControl().pushAsModalDialog(removeMembersFeeadbackWizard.getInitialComponent());
		}
	}
	
	private void doCopyApplications(UserRequest ureq) {
		List<ApplicationRow> applicationRows = getSelectedApplications();
		if(applicationRows.isEmpty()) {
			showWarning("warning.apps.atleastone");
		} else {
			List<ApplicationLight> apps = applicationRows.stream()
					.map(ApplicationRow::getApplication)
					.collect(Collectors.toList());
			CopyApplicationContext copyContext = new CopyApplicationContext(position, apps);
			CopyApplication1PositionStep start = new CopyApplication1PositionStep(ureq, copyContext, getTranslator());
			CopyApplicationFinishCallback finish = new CopyApplicationFinishCallback(copyContext);
			
			String title;
			if(applicationRows.size() == 1) {
				title = translate("copy.application.title");
			} else {
				title = translate("copy.applications.title", Integer.toString(applicationRows.size()));
			}
			copyApplicationsWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(copyApplicationsWizard);
			getWindowControl().pushAsModalDialog(copyApplicationsWizard.getInitialComponent());
		}
	}
	
	public final class RatingFilter extends FlexiTableFilter implements ShortName {
		
		private final float rating;
		private final RatingFilterType type;
		
		public RatingFilter(float rating, String label) {
			super(label, Float.toString(rating));
			this.rating = rating;
			type = RatingFilterType.rating;
		}
		
		public RatingFilter(String label, RatingFilterType type) {
			super(label, type.name());
			rating = -1.0f;
			this.type = type;
		}
		
		public RatingFilter(String label, String filter, RatingFilterType type) {
			super(label, filter);
			rating = -1.0f;
			this.type = type;
		}

		@Override
		public String getShortName() {
			return getLabel();
		}
		
		public float getRating() {
			return rating;
		}
		
		public RatingFilterType type() {
			return type;
		}
	}
	
	public enum RatingFilterType {
		rating,
		cMail,
		category,
		showAll
	}
}