/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemList;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceComment;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.comparator.AppToCategoryComparator;
import org.olat.modules.selectus.ui.comparator.LastnameComparator;
import org.olat.modules.selectus.ui.components.CategoriesCellRenderer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DecisionCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceAdminNoteCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceStatusCellRenderer;
import org.olat.modules.selectus.ui.components.TooltipCellRenderer;
import org.olat.modules.selectus.ui.model.AppToCategory;
import org.olat.modules.selectus.ui.reference.PositionReferenceDataModel.ReferenceCols;
import org.olat.modules.selectus.ui.resources.AttachmentMediaResource;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionReferenceListController extends FormBasicController {

	private static final String PREFS_ID = "recruitingPosRecommendationFlexiList";

	protected static final String FILTER_CATEGORIES = "tags";
	protected static final String FILTER_DECISION = "decision";
	protected static final String FILTER_REFERENCE_TYPE = "referenceType";
	protected static final String FILTER_REFERENCE_STATUS = "referenceStatus";
	protected static final String FILTER_APPLICATION_STATUS = "applicationStatus";
	protected static final String FILTER_NULL_KEY = "NULL";
	
	private FormLink contactButton;
	private FormLink sendMailButton;
	private FlexiTableElement tableEl;
	private PositionReferenceDataModel dataModel;
	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private SendInvitationEmailController sendReferenceCtrl;
	private StepsMainRunController sendMailWizardController;
	private ReferenceCommentsController referenceCommentsCtrl;
	private ApplicationReferenceEditController editRecommendationCtrl;
	
	private int counter = 0;
	private Position position;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionReferenceListController(UserRequest ureq, WindowControl wControl,
			Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "reference_main", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.secCallback = secCallback;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		sendMailButton = uifactory.addFormLink("recommendation.sendmails", formLayout, Link.BUTTON);
		contactButton = uifactory.addFormLink("recommendation.contact", "recommendation.sendmails", null, formLayout, Link.BUTTON);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.type));
		
		// applicant name
		RecruitingTableOption fullNameOption = recruitingModule.getTableReferencesApplicationFullNameOption();
		if(!fullNameOption.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(fullNameOption.isVisible(), ReferenceCols.application));
		}
		
		initColumnModel(ReferenceCols.applicationId, recruitingModule.getTableReferencesApplicationIdOption());
		// project
		initColumnModel(ReferenceCols.projectTitle, recruitingModule.getTableReferencesProjectTitleOption());
		initColumnModel(ReferenceCols.projectAcronym, recruitingModule.getTableReferencesProjectAcronymOption());
		initColumnModel(ReferenceCols.projectKeywords, recruitingModule.getTableReferencesProjectKeywordsOption());
		initColumnModel(ReferenceCols.projectDisciplines, recruitingModule.getTableReferencesProjectDisciplinesOption());
		initColumnModel(ReferenceCols.projectStartDate, recruitingModule.getTableReferencesProjectStartDateOption(),
				new DateCellRenderer());
		initColumnModel(ReferenceCols.projectDuration, recruitingModule.getTableReferencesProjectDurationOption());
		initColumnModel(ReferenceCols.projectFinancialImpact1, recruitingModule.getTableReferencesProjectFinancialImpact1Option());
		initColumnModel(ReferenceCols.projectFinancialImpact2, recruitingModule.getTableReferencesProjectFinancialImpact2Option());
		initColumnModel(ReferenceCols.projectFinancialImpact3, recruitingModule.getTableReferencesProjectFinancialImpact3Option());
		initColumnModel(ReferenceCols.projectFinancialImpact4, recruitingModule.getTableReferencesProjectFinancialImpact4Option());
		initColumnModel(ReferenceCols.projectFinancialImpact5, recruitingModule.getTableReferencesProjectFinancialImpact5Option());
		initColumnModel(ReferenceCols.projectDescription, recruitingModule.getTableReferencesProjectDescriptionOption(),
				new TooltipCellRenderer("o_icon_project_description"));

		// status
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.referenceStatus, new ReferenceStatusCellRenderer()));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.submissionDeadline, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReferenceCols.dateInvitation, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReferenceCols.dateLastReminder, new DateCellRenderer()));
		// application
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReferenceCols.applicationStatus));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReferenceCols.categories, new CategoriesCellRenderer()));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReferenceCols.decision, new DecisionCellRenderer()));
		
		if(recruitingModule.isReferenceAdminNotes()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.adminNote,
					new ReferenceAdminNoteCellRenderer()));
		}
		
		// actions
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.sendMail));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferenceCols.viewLetter));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit", "o_icon_edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete", "o_icon_delete_item"));
		
		dataModel = new PositionReferenceDataModel(columnsModel, position, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_position_recommendation_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("recommendation.list.empty")
				.build());
		tableEl.setPageSize(40);
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		initFilters();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(this.recruitingModule.isCategoriesEnabledFor(position)) {
			// Categories
			boolean seeAdministrativeCategories = secCallback.canSeeApplicationAdministrativeCategories();
			List<Category> categories = taggingService.getAvailableCategoriesFor(position);
			SelectionValues categoriesPK = new SelectionValues();
			for(Category category:categories) {
				String label = RecruitingHelper.getLabel(category);
				categoriesPK.add(SelectionValues.entry(category.getName(), label));
				if(seeAdministrativeCategories) {
					String tagName = "a:".concat(category.getName());
					String adminLabel = RecruitingHelper.getLabel(tagName, category.getColor(), true);
					categoriesPK.add(SelectionValues.entry(tagName, adminLabel));
				}
			}
			categoriesPK.add(SelectionValues.entry(FILTER_NULL_KEY, translate("filter.no.categories")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("table.header.categories"),
					FILTER_CATEGORIES, categoriesPK, true));
		}
		
		// Reference status
		SelectionValues referenceStatusPK = new SelectionValues();
		for(ReferenceStatus status: ReferenceStatus.values()) {
			referenceStatusPK.add(SelectionValues.entry(status.name(), translate("reference.status.".concat(status.name()))));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.reference.status"),
				FILTER_REFERENCE_STATUS, referenceStatusPK, true));
		
		// Reference type
		SelectionValues referenceTypePK = new SelectionValues();
		referenceTypePK.add(SelectionValues.entry(ReferenceType.expert.name(),
				translate("table.header.reference.type.expert")));
		referenceTypePK.add(SelectionValues.entry(ReferenceType.recommendation.name(),
				translate("table.header.reference.type.recommendation")));
		referenceTypePK.add(SelectionValues.entry(ReferenceType.comparativeAssessmentExpert.name(),
				translate("table.header.reference.type.comparativeAssessmentExpert")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.reference.type"),
				FILTER_REFERENCE_TYPE, referenceTypePK, true));
		
		// Application status
		SelectionValues applicationStatusPK = new SelectionValues();
		ApplicationStatus[] applicationStatus = recruitingModule.getTableApplicationsDefaultBasicFilterApplicationStatus();
		for(ApplicationStatus status:applicationStatus) {
			applicationStatusPK.add(SelectionValues.entry(status.name(), translate("application.status.".concat(status.name()))));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.feedback.application.status"),
				FILTER_APPLICATION_STATUS, applicationStatusPK, true));
		
		// Decisions
		SelectionValues decisionKV = new SelectionValues();
		decisionKV.add(SelectionValues.entry(FILTER_NULL_KEY, translate("decision.0.filter")));
		decisionKV.add(SelectionValues.entry("3", translate("decision.3.filter")));
		decisionKV.add(SelectionValues.entry("2", translate("decision.2.filter")));
		decisionKV.add(SelectionValues.entry("1", translate("decision.1.filter")));
		decisionKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.feedback.application.decision"),
				FILTER_DECISION, decisionKV, true));
		
		tableEl.setFilters(true, filters, true, true);
	}
	
	private final void initColumnModel(ReferenceCols col, RecruitingTableOption option, FlexiCellRenderer renderer) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), col, renderer));
		}
	}
	
	private final void initColumnModel(ReferenceCols col, RecruitingTableOption option) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), col));
		}
	}
	
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		dataModel.setPosition(updatedPosition);
	}
	
	public void reloadModel() {
		loadModel();
	}
	
	private void loadModel() {
		String pathPrefix = "[Positions:0][Position:" + position.getKey() + "][Applications:";
		
		List<Reference> refs = recruitingService.getPositionReferences(position, null, false);
		Map<Reference,List<Application>> referenceToApplicationsMap = loadApplications();
		Set<Long> commentsRefs = new HashSet<>(recruitingService.getReferencesWithComments(position));
		List<PositionReferenceRow> positionRefs = new ArrayList<>(refs.size());
		for(Reference ref:refs) {
			ReferenceStatus status = ref.getReferenceStatus();
			ReferenceRequestStatus requestStatus = ref.getRequestStatus();
			
			String cmd = null;
			String i18nLink = null;
			if(status == ReferenceStatus.deactivated) {
				i18nLink = translate("reference.reactivate");
				cmd = "ref-reactivate";
			} else if(requestStatus == ReferenceRequestStatus.declined) {
				i18nLink = translate("reference.reopen");
				cmd = "ref-reopen";
			} else {
				switch(status) {
					case notSent:
						i18nLink = translate("reference.invite");
						cmd = "ref-invite";
						break;
					case late:
					case sentAwaiting:
						i18nLink = translate("reference.remind");
						cmd = "ref-invite";
						break;
					case submitted:
						i18nLink = translate("reference.reopen");
						cmd = "ref-reopen";
						break;
					case deactivated:
						i18nLink = translate("reference.reactivate");
						cmd = "ref-reactivate";
						break;	
				}
			}

			FormLink openLink = uifactory.addFormLink("ref-send-" + (++counter), cmd, i18nLink, null, flc, Link.LINK | Link.NONTRANSLATED);
			openLink.getComponent().setSuppressDirtyFormWarning(true);
			openLink.setUserObject(ref);
			openLink.setIconLeftCSS("o_icon o_icon_mail");
			
			DownloadLink documentLink = null;
			if(ref.getLetter() != null) {
				MediaResource letter = new AttachmentMediaResource(ref, ref.getLetter());
				documentLink = uifactory.addDownloadLink("doc_" + ref.getKey(), translate("view.application.recommendation"), null, letter, tableEl);
				documentLink.setIconLeftCSS("o_icon o_icon_preview");
			}
			String applicationUrl = null;
			if(ref.getApplication() != null) {
				String path = pathPrefix + ref.getApplication().getKey() + "]";
				applicationUrl = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			}
			List<Application> applications = referenceToApplicationsMap.get(ref);
			FormItemCollection applicationsLinks = forgeApplicationLinks(ref.getApplication(), applications, pathPrefix);
			positionRefs.add(new PositionReferenceRow(ref, applicationUrl, applications,
					commentsRefs.contains(ref.getKey()), openLink, documentLink, applicationsLinks));
		}
		
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			loadCategories(positionRefs);
		}
		
		dataModel.setObjects(positionRefs);
		tableEl.reloadData();
	}
	
	private FormItemCollection forgeApplicationLinks(Application application, List<Application> applications, String pathPrefix) {
		FormItemList applicationLinks = new FormItemList(10);
		if(application != null) {
			applicationLinks.add(forgeApplicationLink(application, pathPrefix));
		} else if(applications != null) {
			if(applications.size() > 1) {
				Collections.sort(applications, new LastnameComparator());
			}
			for(Application app:applications) {
				applicationLinks.add(forgeApplicationLink(app, pathPrefix));
			}
		}
		return applicationLinks;
	}
	
	private FormLink forgeApplicationLink(Application application, String pathPrefix) {
		String app = this.salutationGenerator.getFullname(application, getLocale());
		FormLink link = uifactory.addFormLink("app_" + (++counter), "app", app, null, flc, Link.LINK | Link.NONTRANSLATED);
		link.setElementCssClass("o_reference_application_link");
		link.setUserObject(application);
		String path = pathPrefix + application.getKey() + "]";
		String applicationUrl = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
		link.getComponent().setUrl(applicationUrl);
		return link;
	}
	
	private Map<Reference,List<Application>> loadApplications() {
		List<ReferenceToApplication> refToApps = recruitingService.getReferenceToApplications(position);
		Map<Reference,List<Application>> refToAppsMap = new HashMap<>();
		for(ReferenceToApplication refToApp:refToApps) {
			refToAppsMap
				.computeIfAbsent(refToApp.getReference(), ref -> new ArrayList<>())
				.add(refToApp.getApplication());
		}
		return refToAppsMap;
	}
	
	private void loadCategories(List<PositionReferenceRow> positionRefs) {
		
		boolean allowedAdministrative = secCallback.canSeeApplicationAdministrativeCategories();
		List<ApplicationCategoryInfos> tags = taggingService.getApplicationCategories(position, allowedAdministrative);
		
		Map<Long,List<AppToCategory>> appToCategories = new HashMap<>();
		for(ApplicationCategoryInfos tag:tags) {
			List<AppToCategory> categories = appToCategories
					.computeIfAbsent(tag.getApplicationKey(), key -> new ArrayList<>());
			categories.add(AppToCategory.valueOf(tag.getCategory(), tag.isAdministrative()));
		}
		
		for(PositionReferenceRow row:positionRefs) {
			List<AppToCategory> categories = null;
			if(row.getApplication() != null) {
				Long appKey = row.getApplication().getKey();
				categories = appToCategories.get(appKey);
			} else if(row.getApplications() != null && !row.getApplications().isEmpty()) {
				categories = new ArrayList<>();
				for(Application app:row.getApplications()) {
					List<AppToCategory> appCategories = appToCategories.get(app.getKey());
					if(appCategories != null) {
						categories.addAll(appCategories);
					}
				}
			}
			
			if(categories != null) {
				if(categories.size() > 1) {
					Collections.sort(categories, new AppToCategoryComparator());
				}
				row.setCategorie(categories);
			}
		}
	}
	
	public SelectionValues getApplicationStatusKeyValues() {
		SelectionValues keyValues = new SelectionValues();
		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		for(ApplicationStatus status:availableStatus) {
			keyValues.add(SelectionValues.entry(status.name(), translate("application.status.".concat(status.name()))));	
		}
		return keyValues;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(sendReferenceCtrl == source || editRecommendationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(referenceCommentsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(sendMailWizardController == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				loadModel();
				
				@SuppressWarnings("unchecked")
				Map<Reference, MailerResult> mailerResults = (Map<Reference, MailerResult>)sendMailWizardController.getRunContext().get("mailerResults");
				analyseResults(mailerResults);
			}
			removeAsListenerAndDispose(sendMailWizardController);
			sendMailWizardController = null;
		} else if(confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				PositionReferenceRow posReference = (PositionReferenceRow)confirmDeleteCtrl.getUserObject();
				doDeleteReference(posReference);
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editRecommendationCtrl);
		removeAsListenerAndDispose(sendReferenceCtrl);
		removeAsListenerAndDispose(cmc);
		editRecommendationCtrl = null;
		sendReferenceCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sendMailButton == source) {
			doSendInvitationEmail(ureq);
		} else if(contactButton == source) {
			doContact(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				PositionReferenceRow posReference = dataModel.getObject(se.getIndex());
				if("app".equals(se.getCommand())) {
					PositionReferenceRow row = dataModel.getObject(se.getIndex());
					doOpenApplication(ureq, row);
				} else if("edit".equals(cmd)) {
					doEdit(ureq, posReference) ;
				} else if("delete".equals(cmd)) {
					doConfirmDelete(ureq, posReference) ;
				} else if("rcomments".equals(cmd)) {
					doOpenRefereeComments(ureq, posReference);
				}
			} else if(event instanceof FlexiTableSearchEvent ftse) {
				dataModel.filter(ftse.getSearch(), ftse.getFilters());
				tableEl.reset(true, true, false);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(("ref-reopen".equals(cmd) || "ref-invite".equals(cmd) || "ref-reactivate".equals(cmd))
					&& link.getUserObject() instanceof Reference) {
				doSendInvitation(ureq, (Reference)link.getUserObject());
			} else if("app".equals(cmd) && link.getUserObject() instanceof Application) {
				doOpenApplication(ureq, (Application)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenApplication(UserRequest ureq, PositionReferenceRow row) {
		Application app = row.getApplication();
		if(app == null && row.getApplications() != null && !row.getApplications().isEmpty()) {
			app = row.getApplications().get(0);
		}
		doOpenApplication(ureq, app);
	}

	private void doOpenApplication(UserRequest ureq, Application app) {
		if(app == null) return;
		
		String businessPath = "[Positions:0][Position:" + position.getKey() + "][Applications:" + app.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doSendInvitation(UserRequest ureq, Reference reference) {
		Reference reloadedReference = recruitingService.getReferenceById(reference.getKey());
		if(reloadedReference == null) {
			showWarning("warning.reference.deleted");
		} else {
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			Identity secretary = recruitingService.getSecretary(position);
			Application app = reference.getApplication();
			List<Application> appsList = null;
			if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				appsList = recruitingService.getReferenceToApplications(reference)
						.stream().map(ReferenceToApplication::getApplication)
						.collect(Collectors.toList());
			}
			
			ApplicationMailTemplate template = ReferenceHelper.referenceTemplate(headOfCommittee, secretary, position, app, appsList,
					reference, salutationGenerator, getTranslator());
	
			sendReferenceCtrl = new SendInvitationEmailController(ureq, getWindowControl(), position, reference, template);
			listenTo(sendReferenceCtrl);
			cmc = new CloseableModalController(getWindowControl(), "c", sendReferenceCtrl.getInitialComponent(), translate("reference.send.title"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doSendInvitationEmail(UserRequest ureq) {
		List<Reference> references = recruitingService.getPositionReferences(position, null, false);
		if(references.isEmpty()) {
			showWarning("reference.no.tosendemail");
		} else {
			final InvitationVariables invitationVar = mailService.getInvitationVariables(position, getLocale());
			invitationVar.setRows(references);
			
			SortKey[] sorts = tableEl.getOrderBy();
			if(sorts != null && sorts.length > 0 && sorts[0] != null) {
				invitationVar.setSortKey(sorts[0]);
			}
			
			Step start = new InvitationEmail_0_FilterStep(ureq, invitationVar);
			StepRunnerCallback finish = new SendReferencesEmailRunnerCallback(invitationVar);
	
			removeAsListenerAndDispose(sendMailWizardController);
			sendMailWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("reference.wizard.title"), null);
			listenTo(sendMailWizardController);
			
			getWindowControl().pushAsModalDialog(sendMailWizardController.getInitialComponent());
		}
	}
	
	private void doContact(UserRequest ureq) {
		List<Reference> references = getSelectedReferences();
		if(references.isEmpty()) {
			showWarning("reference.no.tosendemail");
		} else {
			final InvitationVariables invitationVar = mailService.getInvitationVariables(position, getLocale());
			invitationVar.setRows(references);
			invitationVar.setSelectedReferences(references);
			
			SortKey[] sorts = tableEl.getOrderBy();
			if(sorts != null && sorts.length > 0 && sorts[0] != null) {
				invitationVar.setSortKey(sorts[0]);
			}
			
			Step start = new InvitationEmail_2_OverviewStep(ureq, invitationVar);
			StepRunnerCallback finish = new SendReferencesEmailRunnerCallback(invitationVar);
	
			removeAsListenerAndDispose(sendMailWizardController);
			sendMailWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("reference.wizard.title"), null);
			listenTo(sendMailWizardController);
			
			getWindowControl().pushAsModalDialog(sendMailWizardController.getInitialComponent());
		}
	}
	
	private void doOpenRefereeComments(UserRequest ureq, PositionReferenceRow posReference) {
		Reference ref = posReference.getReference();
		Application app = posReference.getApplication();
		
		List<ReferenceComment> comments = recruitingService.getComments(ref);
		
		String[] args = new String[] {
			salutationGenerator.getFullname(ref, getLocale()),
			app == null ? null : salutationGenerator.getFullname(app, getLocale()),
		};
		
		String i18nTitle = "reference.comments.referee.title";
		if(ref.getReferenceType() == ReferenceType.expert) {
			i18nTitle = "reference.comments.expert.title";
		}
		String title = translate(i18nTitle, args);
		referenceCommentsCtrl = new ReferenceCommentsController(ureq, getWindowControl(), comments);
		listenTo(referenceCommentsCtrl);
		cmc = new CloseableModalController(getWindowControl(), "c", referenceCommentsCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private List<Reference> getSelectedReferences() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
			.map(index -> dataModel.getObject(index.intValue()))
			.filter(Objects::nonNull)
			.map(PositionReferenceRow::getReference)
			.collect(Collectors.toList());
	}
	
	private void analyseResults(Map<Reference, MailerResult> mailerResults) {
		int countError = 0;
		for(MailerResult result:mailerResults.values()) {
			if(result.getReturnCode() != MailerResult.OK) {
				countError++;
			}
		}
		
		if(countError == 0) {
			showInfo("reference.mail.send.success");
		} else {
			StringBuilder sb = new StringBuilder();
			MailerResult aggregated = new MailerResult();
			for(Map.Entry<Reference, MailerResult> entry:mailerResults.entrySet()) {
				MailerResult result = entry.getValue();
				if(result.getReturnCode() != MailerResult.OK) {
					Reference ref = entry.getKey();
					String mail =ref.getEmail();
					if(sb.length() > 0) sb.append(", ");
					sb.append(mail);
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
	
	private void doEdit(UserRequest ureq, PositionReferenceRow posReference) {
		if(guardModalController(editRecommendationCtrl)) return;
		
		Reference reference = posReference.getReference();
		editRecommendationCtrl = new ApplicationReferenceEditController(ureq, getWindowControl(), position, reference, secCallback);
		listenTo(editRecommendationCtrl);
		
		String title = translate("edit.recommendation");
		cmc = new CloseableModalController(getWindowControl(), "c", editRecommendationCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, PositionReferenceRow posReference) {
		String title;
		String text;

		Reference reference = posReference.getReference();
		String fullName = salutationGenerator.getFullname(reference, getLocale());
		String[] textArgs = new String[] { fullName, reference.getInstitution() };
		if(reference.getReferenceType() == ReferenceType.expert) {
			title = translate("reference.confirm.delete.expert.title");
			text = translate("reference.confirm.delete.expert.text", textArgs);
		} else {
			title = translate("reference.confirm.delete.recommendation.title");
			text = translate("reference.confirm.delete.recommendation.text", textArgs);
		}
		
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(posReference);
	}
	
	private void doDeleteReference(PositionReferenceRow posReference) {
		Reference reference = posReference.getReference();
		recruitingService.deleteReference(reference);
		
		// log
		Application application = reference.getApplication();
		List<Application> applicationsList = recruitingService.getReferenceToApplicationsList(reference);
		
		ActionTarget target = null;
		String messageI18n = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.expert.delete";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.referee.delete";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeExpert;
			messageI18n = "audit.log.comparative.expert.delete";
		}
		
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(reference, getLocale()),
			salutationGenerator.getTitleFullname(application, applicationsList, getLocale()),
			RecruitingHelper.formatIDs(application, applicationsList)
		};
		auditService.auditRefereeLog(Action.delete, target, null, null, messageI18n, messageArgs, getTranslator(),
				position, application, reference, getIdentity());
	}
}
