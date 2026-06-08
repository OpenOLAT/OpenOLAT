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
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.comparator.AppToCategoryComparator;
import org.olat.modules.selectus.ui.components.CategoriesCellRenderer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DecisionCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceStatusCellRenderer;
import org.olat.modules.selectus.ui.feedback.appsfeedback.PositionFeedbacksTableModel.PositionFeedCols;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.Contact2OverviewStep;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.ContactFeedbacksMemberFinishCallback;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.ContactMembersContext;
import org.olat.modules.selectus.ui.model.AppToCategory;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * The list of feedbacks requests for a position.
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionFeedbacksController extends FormBasicController {

	private static final String PREFS_ID = "recruitingPosFeedbackMembersFlexiList";

	protected static final String FILTER_CATEGORIES = "tags";
	protected static final String FILTER_DECISION = "decision";
	protected static final String FILTER_FEEDBACK_STATUS = "feedbackStatus";
	protected static final String FILTER_APPLICATION_STATUS = "applicationStatus";
	protected static final String FILTER_NULL_KEY = "NULL";
	
	public static final int USER_PROP_OFFSET = 500; //only used in wizard
	public static final String formIdentifyer = "Committee";
	
	private FormLink contactButton;
	private FlexiTableElement tableEl;
	private PositionFeedbacksTableModel tableModel;
	
	private Position position;
	private final boolean canEditFeedbacks;
	private final RecruitingPositionSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private CloseableModalController cmc;
	private ViewFeedbackController viewFeedbackCtrl;
	private SendFeedbackInvitationController sendInvitationCtrl;
	private StepsMainRunController contactMembersFeeadbackWizard;
	private ApplicationFeedbackMemberEditController memberEditCtrl; 
	private ConfirmRemoveFeedbackMemberController confirmRemoveFeedbackMemberCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionFeedbacksController(UserRequest ureq, WindowControl wControl, Position position,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "position_feedbacks", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.position = position;
		this.secCallback = secCallback;
		canEditFeedbacks = secCallback.canEditApplicationMembersFeedback();
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifyer, true);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.application, "app"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.email));
		
		RecruitingTableOption userPropertiesOption = recruitingModule.getTableFeedbacksUserPropertiesOption();
		if(userPropertiesOption != RecruitingTableOption.disabled) {
			boolean visible = userPropertiesOption == RecruitingTableOption.enabled;
			
			int colIndex = USER_PROP_OFFSET;
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex++, true, userPropertyHandler.getName());
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.feedbackStatus, new ReferenceStatusCellRenderer()));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.submissionDeadline, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.dateRequest, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.dateLastReminder, new DateCellRenderer()));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PositionFeedCols.applicationStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PositionFeedCols.categories,
				new CategoriesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PositionFeedCols.decision,
				new DecisionCellRenderer()));
		
		if(canEditFeedbacks) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.sendMail));
		}
		
		StaticFlexiCellRenderer viewCol = new StaticFlexiCellRenderer(translate("apps.feedback.view"), "view");
		viewCol.setIconLeftCSS("o_icon o_icon_preview");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PositionFeedCols.viewFeedback.i18nHeaderKey(), PositionFeedCols.viewFeedback.ordinal(), "view",
				new BooleanCellRenderer(viewCol, null)));
		
		if(canEditFeedbacks) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit-feed", "o_icon_edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete-feed", "o_icon_delete_item"));
		}

		tableModel = new PositionFeedbacksTableModel(columnsModel, userPropertyHandlers, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "feedbacks", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_position_reference_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("apps.feedbacks.emtpy")
				.build());
		tableEl.setPageSize(20);
		tableEl.setSearchEnabled(true);
		initFilters();
		
		if(canEditFeedbacks) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			contactButton = uifactory.addFormLink("contact.apps.feedback.members", formLayout, Link.BUTTON);
		}
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(recruitingModule.isCategoriesEnabledFor(position)) {
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
		
		// Feedback status
		SelectionValues referenceStatusPK = new SelectionValues();
		for(ReferenceStatus status: ReferenceStatus.values()) {
			referenceStatusPK.add(SelectionValues.entry(status.name(), translate("reference.status.".concat(status.name()))));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.feedback.status"),
				FILTER_FEEDBACK_STATUS, referenceStatusPK, true));
		
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
	
	public SelectionValues getApplicationStatusKeyValues() {
		SelectionValues keyValues = new SelectionValues();
		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		for(ApplicationStatus status:availableStatus) {
			keyValues.add(SelectionValues.entry(status.name(), translate("application.status.".concat(status.name()))));	
		}
		return keyValues;
	}
	
	public void reloadModel() {
		loadModel();
	}
	
	private void loadModel() {
		String pathPrefix = "[Positions:0][Position:" + position.getKey() + "][Applications:";
	
		List<PositionFeedbackRow> memberRows = new ArrayList<>();
		List<ApplicationFeedback> feedbacks = feedbackService.getApplicationsFeedbacks(position);
		for(ApplicationFeedback feedback:feedbacks) {
			String cmd = null;
			String i18nLink = null;
			ReferenceStatus status = feedback.getReferenceStatus();
			switch(status) {
				case notSent:
					i18nLink = translate("reference.invite");
					cmd = "invite";
					break;
				case late:
				case sentAwaiting:
					i18nLink = translate("reference.remind");
					cmd = "invite";
					break;
				case submitted:
					i18nLink = translate("reference.reopen");
					cmd = "reopen";
					break;
				default: break;
			}

			FormLink mailLink = uifactory.addFormLink("send-" + feedback.getKey(), cmd, i18nLink, null, flc, Link.LINK | Link.NONTRANSLATED);
			mailLink.setIconLeftCSS("o_icon o_icon_mail");
			mailLink.getComponent().setSuppressDirtyFormWarning(true);
			
			String path = pathPrefix + feedback.getApplication().getKey() + "]";
			String applicationUrl = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			PositionFeedbackRow feedbackRef = new PositionFeedbackRow(feedback.getIdentity(), feedback, applicationUrl, mailLink);
			mailLink.setUserObject(feedbackRef);
			memberRows.add(feedbackRef);
		}
		
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			boolean allowedAdministrative = secCallback.canSeeApplicationAdministrativeCategories();
			List<ApplicationCategoryInfos> tags = taggingService.getApplicationCategories(position, allowedAdministrative);
			
			Map<Long,List<AppToCategory>> appToCategories = new HashMap<>();
			for(ApplicationCategoryInfos tag:tags) {
				List<AppToCategory> categories = appToCategories
						.computeIfAbsent(tag.getApplicationKey(), key -> new ArrayList<>());
				categories.add(AppToCategory.valueOf(tag.getCategory(), tag.isAdministrative()));
			}
			
			for(PositionFeedbackRow row:memberRows) {
				Long appKey = row.getFeedback().getApplication().getKey();
				List<AppToCategory> categories = appToCategories.get(appKey);
				if(categories != null) {
					if(categories.size() > 1) {
						Collections.sort(categories, new AppToCategoryComparator());
					}
					row.setCategorie(categories);
				}
			}
		}
		
		if(tableEl != null) {
			tableModel.setObjects(memberRows);
			tableEl.reset(true, true, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveFeedbackMemberCtrl == source || sendInvitationCtrl == source || memberEditCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(viewFeedbackCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(contactMembersFeeadbackWizard == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				//reload the list
				loadModel();
			}
			cleanUp();
		} 
		
		else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(contactMembersFeeadbackWizard);
		removeAsListenerAndDispose(confirmRemoveFeedbackMemberCtrl);
		removeAsListenerAndDispose(sendInvitationCtrl);
		removeAsListenerAndDispose(viewFeedbackCtrl);
		removeAsListenerAndDispose(memberEditCtrl);
		removeAsListenerAndDispose(cmc);
		contactMembersFeeadbackWizard = null;
		confirmRemoveFeedbackMemberCtrl = null;
		sendInvitationCtrl = null;
		viewFeedbackCtrl = null;
		memberEditCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(contactButton == source) {
			doContactMembers(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("app".equals(se.getCommand())) {
					PositionFeedbackRow row = tableModel.getObject(se.getIndex());
					doOpenApplication(ureq, row);
				} else if("edit-feed".equals(se.getCommand())) {
					PositionFeedbackRow row = tableModel.getObject(se.getIndex());
					doEdit(ureq, row);
				} else if("delete-feed".equals(se.getCommand())) {
					PositionFeedbackRow row = tableModel.getObject(se.getIndex());
					doConfirmRemoveMember(ureq, row);
				} else if("view".equals(se.getCommand())) {
					PositionFeedbackRow row = tableModel.getObject(se.getIndex());
					doViewFeedback(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent ftse) {
				tableModel.filter(ftse.getSearch(), ftse.getFilters());
				tableEl.reset(true, true, false);
			}
		} else if(source instanceof FormLink && source.getUserObject() instanceof PositionFeedbackRow) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("reopen".equals(cmd) || "invite".equals(cmd)) {
				PositionFeedbackRow row = (PositionFeedbackRow)link.getUserObject();
				doSendInvitation(ureq, row);
			} else if("view".equals(cmd)) {
				PositionFeedbackRow row = (PositionFeedbackRow)link.getUserObject();
				doViewFeedback(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenApplication(UserRequest ureq, PositionFeedbackRow row) {
		String businessPath = "[Positions:0][Position:" + position.getKey() + "][Applications:" + row.getFeedback().getApplication().getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doEdit(UserRequest ureq, PositionFeedbackRow row) {
		if(guardModalController(memberEditCtrl)) return;
		
		Identity member = row.getMember();
		Application application = row.getFeedback().getApplication();
		memberEditCtrl = new ApplicationFeedbackMemberEditController(ureq, getWindowControl() ,
				member, row.getFeedback(), application, position);
		listenTo(memberEditCtrl);
		
		String title = translate("edit.feedback.member");
		cmc = new CloseableModalController(getWindowControl(), "c", memberEditCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmRemoveMember(UserRequest ureq, PositionFeedbackRow feedbackRow) {
		if(guardModalController(confirmRemoveFeedbackMemberCtrl)) return;
		
		confirmRemoveFeedbackMemberCtrl = new ConfirmRemoveFeedbackMemberController(ureq, getWindowControl(), position,
				feedbackRow.getFeedback().getApplication(), feedbackRow.getFeedback());
		listenTo(confirmRemoveFeedbackMemberCtrl);

		String title = translate("confirm.remove.feedback.member.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmRemoveFeedbackMemberCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doViewFeedback(UserRequest ureq, PositionFeedbackRow feedbackRow) {
		ApplicationFeedback feedback = feedbackService.getApplicationFeedback(feedbackRow.getFeedback());
		if(feedback == null) {
			showWarning("warning.reference.deleted");
		} else {
			viewFeedbackCtrl = new ViewFeedbackController(ureq, getWindowControl(), feedback);
			listenTo(viewFeedbackCtrl);
		
			String title = translate("apps.feedback.view.title", new String[] {
				RecruitingHelper.formatFullName(feedbackRow.getMember())
			});
			cmc = new CloseableModalController(getWindowControl(), "c", viewFeedbackCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doSendInvitation(UserRequest ureq, PositionFeedbackRow feedbackRow) {
		ApplicationFeedback feedback = feedbackService.getApplicationFeedback(feedbackRow.getFeedback());
		if(feedback == null) {
			showWarning("warning.reference.deleted");
		} else {
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			Identity secretary = recruitingService.getSecretary(position);
			Application app = feedback.getApplication();
			List<Application> apps = Collections.singletonList(app);
			ApplicationMailTemplate template = FeedbackHelper.feedbackTemplate(headOfCommittee, secretary, position, apps,
					feedback.getIdentity(), Collections.singletonList(feedback), feedback.getConfiguration(),
					salutationGenerator, getTranslator());
			
			sendInvitationCtrl = new SendFeedbackInvitationController(ureq, getWindowControl(), position, feedback, template);
			listenTo(sendInvitationCtrl);
			
			String title = translate("apps.feedback.send.title", new String[] {
					RecruitingHelper.formatFullName(feedbackRow.getMember())
			});
			cmc = new CloseableModalController(getWindowControl(), "c", sendInvitationCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doContactMembers(UserRequest ureq) {
		List<ApplicationFeedback> feedbacks = getSelectedFeedbacks();
		ApplicationsFeedbackConfiguration defaultConfig = getConfiguration(feedbacks);
		if(feedbacks.isEmpty() || defaultConfig == null) {
			showWarning("warning.apps.feedbacks.atleastone");
		} else {
			List<Application> apps = getApplicationsOf(feedbacks);
			List<Identity> members = getMembersOf(feedbacks);
			
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
					null, feedbacks, defaultConfig, salutationGenerator, getTranslator());
			
			ContactMembersContext feedbackMembersContext = new ContactMembersContext(position, defaultConfig,
					feedbacks, secretary, headOfCommittee, mailTemplate);
			feedbackMembersContext.setMembers(members);
			feedbackMembersContext.setSelectedMembers(members);
			feedbackMembersContext.setDeadline(defaultConfig.getDeadline());
			Contact2OverviewStep start = new Contact2OverviewStep(ureq, feedbackMembersContext, true);
			ContactFeedbacksMemberFinishCallback finish = new ContactFeedbacksMemberFinishCallback(feedbackMembersContext, getTranslator());
			contactMembersFeeadbackWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(contactMembersFeeadbackWizard);
			getWindowControl().pushAsModalDialog(contactMembersFeeadbackWizard.getInitialComponent());
		}
	}
	
	private ApplicationsFeedbackConfiguration getConfiguration(List<ApplicationFeedback> feedbacks) {
		return feedbacks.isEmpty() ? null : feedbacks.get(0).getConfiguration();
	}
	
	private List<ApplicationFeedback> getSelectedFeedbacks() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.map(PositionFeedbackRow::getFeedback)
				.collect(Collectors.toList());
	}
	
	private List<Identity> getMembersOf(List<ApplicationFeedback> feedbacks) {
		Set<Identity> members = feedbacks.stream()
				.map(ApplicationFeedback::getIdentity)
				.collect(Collectors.toSet());
		return new ArrayList<>(members);
	}
	
	private List<Application> getApplicationsOf(List<ApplicationFeedback> feedbacks) {
		Set<Application> applications = feedbacks.stream()
				.map(ApplicationFeedback::getApplication)
				.collect(Collectors.toSet());
		return new ArrayList<>(applications);
	}
}
