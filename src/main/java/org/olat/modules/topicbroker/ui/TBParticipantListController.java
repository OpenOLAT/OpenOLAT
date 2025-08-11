/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerStatus;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBParticipantCandidates.FilterGroup;
import org.olat.modules.topicbroker.TBParticipantSearchParams;
import org.olat.modules.topicbroker.TBSecurityCallback;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBParticipantDataModel.TBParticipantCols;
import org.olat.modules.topicbroker.ui.components.TBSelectionsRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private static final String FILTER_GROUPS = "groups";
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_BOOSTED = "Boosted";
	private static final String TAB_ID_OPEN_SELECTION = "OpenSelection";
	private static final String TAB_ID_WAITING_LIST = "WaitingList";
	private static final String TAB_ID_ENROLLED_PARTIALLY = "PartiallyEnrolled";
	private static final String TAB_ID_ENROLLED_FULLY = "FullyEnrolled";
	private static final String CMD_DETAILS = "details";
	private static final String CMD_ACTIVITY_LOG = "activity.log";
	
	private InfoPanel configPanel;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabBoosted;
	private FlexiFiltersTab tabOpenSelection;
	private FlexiFiltersTab tabWaitingList;
	private FlexiFiltersTab tabEnrolledPartially;
	private FlexiFiltersTab tabEnrolledFully;
	private FormLink bulkEmailButton;
	private FormLink bulkNotificationButton;
	private FormLink bulkWithdrawEnrollmentsButton;
	private FormLink bulkResetSelectionsButton;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private FormLink enrollmentManualStartLink;
	private FormLink notificationsLink;
	private TBParticipantDataModel dataModel;
	private FlexiTableElement tableEl;
	private VelocityContainer detailsVC;
	
	private CloseableModalController cmc;
	private TBEnrollmentManualProcessController enrollmentManualCtrl;
	private LayoutMain3ColsBackController enrollmentLayoutCtr;
	private ContactFormController contactCtrl;
	private ConfirmationController notificationConfirmationCtrl;
	private ConfirmationController withdrawEnrollmentsConfirmationCtrl;
	private ConfirmationController resetSelectionConfirmationCtrl;	private TBActivityLogController activityLogCtrl;
	private LayoutMain3ColsBackController activityLogLayoutCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private TBBroker broker;
	private final TBSecurityCallback secCallback;
	private final TBParticipantCandidates participantCandidates;
	private final List<Identity> identities;
	private final UserInfoProfileConfig profileConfig;
	private Set<Long> detailsOpenIdentityKeys;
	private List<TBParticipantSelectionsController> detailCtrls = new ArrayList<>(1);
	private int counter = 0;
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private RepositoryService repositoryService;

	public TBParticipantListController(UserRequest ureq, WindowControl wControl, TBBroker broker,
			TBSecurityCallback secCallback, TBParticipantCandidates participantCandidates) {
		super(ureq, wControl, "participant_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.broker = broker;
		this.secCallback = secCallback;
		this.participantCandidates = participantCandidates;
		this.identities = participantCandidates.getVisibleIdentities();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TBParticipantDataModel.USAGE_IDENTIFIER,
				isAdministrativeUser);
		
		profileConfig = userPortraitService.createProfileConfig();
		
		initForm(ureq);
		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		updateCommandUI();
		loadModel(ureq);
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(6);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		tabBoosted = FlexiFiltersTabFactory.tab(
				TAB_ID_BOOSTED,
				translate("tab.boosted"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabBoosted);
		
		if (broker.getEnrollmentStartDate() == null && broker.getEnrollmentStartDate() == null) {
			tabOpenSelection = FlexiFiltersTabFactory.tab(
					TAB_ID_OPEN_SELECTION,
					translate("tab.open.selection"),
					TabSelectionBehavior.reloadData);
			tabs.add(tabOpenSelection);
		}
		
		tabWaitingList = FlexiFiltersTabFactory.tab(
				TAB_ID_WAITING_LIST,
				translate("tab.waiting.list.on"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabWaitingList);
		
		tabEnrolledPartially = FlexiFiltersTabFactory.tab(
				TAB_ID_ENROLLED_PARTIALLY,
				translate("tab.enrolled.partially"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabEnrolledPartially);
		
		tabEnrolledFully = FlexiFiltersTabFactory.tab(
				TAB_ID_ENROLLED_FULLY,
				translate("tab.enrolled.fully"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabEnrolledFully);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	private void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		detailsOpenIdentityKeys = null;
		loadModel(ureq);
	}
	
	private void initFilters() {
		List<FilterGroup> filterGroups = participantCandidates.getFilterGroups();
		if (filterGroups == null || filterGroups.isEmpty()) {
			return;
		}
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(1);
		
		SelectionValues groupValues = new SelectionValues();
		filterGroups.forEach(group -> groupValues.add(new SelectionValue(
				group.key(), StringHelper.escapeHtml(group.name()), null,
				"o_icon o_icon-fw " + group.iconCss(), null, true)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.groups"), FILTER_GROUPS, groupValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configPanel = new InfoPanel("configs");
		configPanel.setTitle(translate("configuration"));
		configPanel.setPersistedStatusId(ureq, "tb-config-" + broker.getKey());
		formLayout.add("config", new ComponentWrapperElement(configPanel));
		updateBrokerConfigUI();
		updateBrokerStatusUI();
		
		if (!broker.isAutoEnrollment() && secCallback.canStartManualEnrollment()) {
			enrollmentManualStartLink = uifactory.addFormLink("enrollment.manual.initiate", formLayout, Link.BUTTON);
			enrollmentManualStartLink.setElementCssClass("o_sel_tb_enrollment_start");
			updateEnrollmentManualUI();
		}
		
		notificationsLink = uifactory.addFormLink("participants.notifications", formLayout, Link.BUTTON);
		notificationsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_notification");
		notificationsLink.setVisible(false);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		FlexiTableSortOptions sortOptions = null;
		int colIndex = TBParticipantDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(TBParticipantDataModel.USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, CMD_DETAILS,true, propName));
			if (UserConstants.LASTNAME.equals(userPropertyHandler.getName())) {
				SortKey sortKey = new SortKey(propName, true);
				sortOptions = new FlexiTableSortOptions();
				sortOptions.setDefaultOrderBy(sortKey);
			}
		}
		
		DefaultFlexiColumnModel boostColumn = new DefaultFlexiColumnModel(TBParticipantCols.boost);
		boostColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		boostColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(boostColumn);
		
		DefaultFlexiColumnModel enrolledColumn = new DefaultFlexiColumnModel(TBParticipantCols.enrolled, new TextFlexiCellRenderer(EscapeMode.none));
		enrolledColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrolledColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(enrolledColumn);
		
		DefaultFlexiColumnModel waitingListColumn = new DefaultFlexiColumnModel(TBParticipantCols.waitingList);
		waitingListColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		waitingListColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(waitingListColumn);
		
		DefaultFlexiColumnModel selectedColumn = new DefaultFlexiColumnModel(TBParticipantCols.selected);
		selectedColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		selectedColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(selectedColumn);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBParticipantCols.priority, new TBSelectionsRenderer()));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(TBParticipantCols.tools));
		
		dataModel = new TBParticipantDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "topic-broker-participants" + broker.getKey());
		tableEl.setSortSettings(sortOptions);
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		String page = velocity_root + "/details.html";
		detailsVC = new VelocityContainer("details_" + counter++, "vc_details", page, getTranslator(), this);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void initBulkLinks() {
		bulkEmailButton = uifactory.addFormLink("participants.bulk.email", flc, Link.BUTTON);
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		tableEl.addBatchButton(bulkEmailButton);
		
		bulkNotificationButton = uifactory.addFormLink("participants.bulk.notification", flc, Link.BUTTON);
		bulkNotificationButton.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_notification");
		bulkNotificationButton.setVisible(false);
		tableEl.addBatchButton(bulkNotificationButton);
		
		bulkWithdrawEnrollmentsButton = uifactory.addFormLink("participants.bulk.withdraw.enrollments", flc, Link.BUTTON);
		bulkWithdrawEnrollmentsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_withdraw");
		tableEl.addBatchButton(bulkWithdrawEnrollmentsButton);
		
		bulkResetSelectionsButton = uifactory.addFormLink("participants.bulk.reset.selections", flc, Link.BUTTON);
		bulkResetSelectionsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_unselect");
		tableEl.addBatchButton(bulkResetSelectionsButton);
	}
	
	private void updateCommandUI() {
		if (TBBrokerStatus.enrollmentDone == TBUIFactory.getBrokerStatus(broker) && secCallback.canSendNotification()) {
			notificationsLink.setVisible(true);
			bulkNotificationButton.setVisible(true);
		}
	}
	
	public void reload(UserRequest ureq) {
		broker = topicBrokerService.getBroker(broker);
		updateCommandUI();
		updateBrokerStatusUI();
		updateBrokerConfigUI();
		updateEnrollmentManualUI();
		loadConfigInfos(ureq);
		loadModel(ureq);
	}

	private void loadModel(UserRequest ureq) {
		List<Identity> filteredIdentities = getFilterIdentitities();
		
		TBParticipantSearchParams participantSearchParams = new TBParticipantSearchParams();
		participantSearchParams.setBroker(broker);
		participantSearchParams.setIdentities(filteredIdentities);
		participantSearchParams.setFetchIdentity(true);
		Map<Long, TBParticipant> identityKeyToParticipant = topicBrokerService.getParticipants(participantSearchParams).stream()
				.collect(Collectors.toMap(particioant -> particioant.getIdentity().getKey(), Function.identity()));
		
		TBSelectionSearchParams selectionSearchParams = new TBSelectionSearchParams();
		selectionSearchParams.setBroker(broker);
		selectionSearchParams.setIdentities(filteredIdentities);
		selectionSearchParams.setFetchIdentity(true);
		selectionSearchParams.setFetchTopic(true);
		Map<Long, List<TBSelection>> identityKeyToSelections = topicBrokerService.getSelections(selectionSearchParams).stream()
				.sorted((s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()))
				.collect(Collectors.groupingBy(selection -> selection.getParticipant().getIdentity().getKey()));
		
		List<TBParticipantRow> rows = new ArrayList<>(filteredIdentities.size());
		for (Identity identity : filteredIdentities) {
			TBParticipantRow row = new TBParticipantRow(identity, userPropertyHandlers, getLocale());
			row.setBroker(broker);
			row.setMaxSelections(broker.getMaxSelections());
			
			TBParticipant participant = identityKeyToParticipant.get(identity.getKey());
			row.setParticipant(participant);
			List<TBSelection> identitySelections = identityKeyToSelections.getOrDefault(identity.getKey(), List.of());
			row.setSelections(identitySelections);
			if (participant != null) {
				row.setBoost(participant.getBoost());
			}
			forgeEnrolled(row, participant);
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		applySearch(rows);
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
		
		tableEl.collapseAllDetails();
		detailCtrls.forEach(ctrl -> {
			flc.remove(ctrl.getInitialFormItem());
			removeAsListenerAndDispose(ctrl);
		});
		detailCtrls.clear();
		if (detailsOpenIdentityKeys != null && !detailsOpenIdentityKeys.isEmpty()) {
			dataModel.getObjects().stream()
				.filter(row -> detailsOpenIdentityKeys.contains(row.getIdentityKey()))
				.forEach(row -> {
					int index = dataModel.getObjects().indexOf(row);
					doShowDetails(ureq, row);
					tableEl.expandDetails(index);
				});
		}
	}

	private List<Identity> getFilterIdentitities() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters != null && !filters.isEmpty()) {
			for (FlexiTableFilter filter : filters) {
				if (FILTER_GROUPS.equals(filter.getFilter())) {
					List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
					if (values != null && !values.isEmpty()) {
						Set<Long> identityKeys = participantCandidates.getFilterGroups().stream()
								.filter(group -> values.contains(group.key()))
								.flatMap(group -> group.identityKeys().stream())
								.collect(Collectors.toSet());
						ArrayList<Identity> filteredIdentities = new ArrayList<>(identities);
						filteredIdentities.removeIf(identity -> !identityKeys.contains(identity.getKey()));
						return filteredIdentities;
					}
				}
			}
		}
		return identities;
	}

	private void applyFilters(List<TBParticipantRow> rows) {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabAll) {
			return;
		}
		
		if (tableEl.getSelectedFilterTab() == tabBoosted) {
			rows.removeIf(row -> row.getBoost() == null);
		} else if (tableEl.getSelectedFilterTab() == tabOpenSelection) {
			rows.removeIf(row -> row.getNumSelections() >= row.getMaxSelections());
		} else if (tableEl.getSelectedFilterTab() == tabWaitingList) {
			rows.removeIf(row -> row.getWaitingList() == 0);
		} else if (tableEl.getSelectedFilterTab() == tabEnrolledPartially) {
			rows.removeIf(row -> row.getNumEnrollments()== 0 || row.getRequiredEnrollments() <= row.getNumEnrollments());
		} else if (tableEl.getSelectedFilterTab() == tabEnrolledFully) {
			rows.removeIf(row -> row.getRequiredEnrollments() > row.getNumEnrollments());
		}
	}
	
	private void applySearch(List<TBParticipantRow> rows) {
		String searchValue = tableEl.getQuickSearchString();
		if (StringHelper.containsNonWhitespace(searchValue)) {
			List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
			rows.removeIf(row -> 
					containsNot(searchValues, row.getIdentityProp(UserConstants.LASTNAME, userPropertyHandlers))
					&& containsNot(searchValues, row.getIdentityProp(UserConstants.FIRSTNAME, userPropertyHandlers))
				);
		}
	}
	
	private boolean containsNot(List<String> searchValues, String candidate) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			String candidateLowerCase = candidate.toLowerCase();
			return searchValues.stream().noneMatch(searchValue -> candidateLowerCase.indexOf(searchValue) >= 0);
		}
		return true;
	}

	private void forgeEnrolled(TBParticipantRow row, TBParticipant participant) {
		long numEnrollments = row.getSelections().stream().filter(TBSelection::isEnrolled).count();
		row.setNumEnrollments((int)numEnrollments);
		
		row.setRequiredEnrollments(TBUIFactory.getRequiredEnrollments(broker, participant));
		
		row.setEnrolledString(translate("enrollments.of", 
				String.valueOf(row.getNumEnrollments()), 
				String.valueOf(row.getRequiredEnrollments())));
		if (broker.getEnrollmentDoneDate() != null && numEnrollments < row.getRequiredEnrollments()) {
			String enrolledString = "<span title=\"" + translate("participant.msg.too.less.enrollments",
					String.valueOf(row.getNumEnrollments()), String.valueOf(row.getRequiredEnrollments()))
					+ "\"><i class=\"o_icon o_icon_error\"></i> ";
			enrolledString += row.getEnrolledString();
			enrolledString += "</span>";
			row.setEnrolledString(enrolledString);
		}
		
		if (broker.getEnrollmentStartDate() != null) {
			if (row.getNumEnrollments() >= row.getRequiredEnrollments()) {
				row.setWaitingList(0);
			} else {
				row.setWaitingList(row.getSelections().size() - row.getNumEnrollments());
			}
		} else {
			row.setWaitingList(row.getSelections().size() - row.getNumEnrollments());
		}
	}
	
	private void forgeToolsLink(TBParticipantRow row) {
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	private void doShowDetails(UserRequest ureq, TBParticipantRow row) {
		TBParticipant participant = row.getParticipant();
		if (participant == null) {
			Identity participantIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
			participant = topicBrokerService.getOrCreateParticipant(getIdentity(), broker, participantIdentity);
		}
		
		TBParticipantSelectionsController detailsCtrl = new TBParticipantSelectionsController(ureq, getWindowControl(),
				mainForm, broker, participant, profileConfig, userPortraitService.createPortraitUser(getLocale(), participant.getIdentity()),
				row.getSelections(), secCallback.canEditSelections());
		listenTo(detailsCtrl);
		detailCtrls.add(detailsCtrl);
		// Add as form item to catch the events...
		flc.add("detailsform_" + counter++, detailsCtrl.getInitialFormItem());
		
		// ... and add the component to the details container.
		String detailsComponentName = "details_" + counter++;
		row.setDetailsComponentName(detailsComponentName);
		detailsVC.put(detailsComponentName, detailsCtrl.getInitialComponent());
	}
	
	private void setDetailsOpenIdentities() {
		detailsOpenIdentityKeys = tableEl.getDetailsIndex().stream()
				.map(i -> dataModel.getObject(i))
				.filter(Objects::nonNull)
				.map(TBParticipantRow::getIdentityKey)
				.collect(Collectors.toSet());
	}
	
	private void updateBrokerStatusUI() {
		TBBrokerStatus brokerStatus = TBUIFactory.getBrokerStatus(broker);
		if (broker.isAutoEnrollment() && TBBrokerStatus.enrollmentInProgess == brokerStatus) {
			flc.contextPut("messageInfo", translate("selection.msg.auto.enrollment.pending"));
		}
		
		flc.contextPut("statusLabel", TBUIFactory.getLabel(getTranslator(), brokerStatus));
	}

	private void updateEnrollmentManualUI() {
		if (enrollmentManualStartLink == null) {
			return;
		}
		
		boolean enrollmentManual = broker.getEnrollmentStartDate() == null
				&& broker.getSelectionEndDate() != null && broker.getSelectionEndDate().before(new Date());
		enrollmentManualStartLink.setEnabled(enrollmentManual);
		enrollmentManualStartLink.setPrimary(enrollmentManual);
		enrollmentManualStartLink.setVisible(broker.getEnrollmentStartDate() == null && broker.getEnrollmentDoneDate() == null);
	}
	
	private void updateBrokerConfigUI() {
		String infos = TBUIFactory.getConfigInfos(getTranslator(), broker, true, null);
		configPanel.setInformations(infos);
	}
	
	private void loadConfigInfos(UserRequest ureq) {
		configPanel.reloadStatus(ureq);
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			FlexiFiltersTab tab = tableEl.getFilterTabById(type);
			if (tab != null) {
				selectFilterTab(ureq, tab);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (enrollmentManualCtrl == source) {
			enrollmentLayoutCtr.deactivate();
			broker = topicBrokerService.getBroker(broker);
			updateBrokerStatusUI();
			updateBrokerConfigUI();
			updateEnrollmentManualUI();
			loadModel(ureq);
			cleanUp();
		} else if (source == contactCtrl) {
			if (cmc != null) {
				cmc.deactivate();
			}
			cleanUp();
		} else if (source == notificationConfirmationCtrl) {
			if (event == Event.DONE_EVENT) {
				doSendNotification((List<Identity>)notificationConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (withdrawEnrollmentsConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doWithdrawEnrollments(ureq, (List<Identity>)withdrawEnrollmentsConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (resetSelectionConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doResetSelections(ureq, (List<Identity>)resetSelectionConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source instanceof TBParticipantSelectionsController) {
			if (event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			}
		} else if (source == enrollmentLayoutCtr && event == Event.BACK_EVENT) {
			cleanUp();
		} else if (source == activityLogLayoutCtrl && event == Event.BACK_EVENT) {
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(enrollmentManualCtrl);
		removeAsListenerAndDispose(enrollmentLayoutCtr);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(notificationConfirmationCtrl);
		removeAsListenerAndDispose(withdrawEnrollmentsConfirmationCtrl);
		removeAsListenerAndDispose(resetSelectionConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(activityLogLayoutCtrl);
		removeAsListenerAndDispose(activityLogCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		enrollmentManualCtrl = null;
		enrollmentLayoutCtr = null;
		contactCtrl = null;
		notificationConfirmationCtrl = null;
		withdrawEnrollmentsConfirmationCtrl = null;
		resetSelectionConfirmationCtrl = null;
		cmc = null;
		activityLogLayoutCtrl = null;
		activityLogCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enrollmentManualStartLink) {
			doEnrollmentManual(ureq);
		} else if (source == notificationsLink) {
			doConfirmNotification(ureq);
		} else if (source == bulkEmailButton) {
			doBulkEmail(ureq);
		} else if (source == bulkNotificationButton) {
			doBulkNotification(ureq);
		} else if (source == bulkWithdrawEnrollmentsButton) {
			doBulkWithdrawEnrollmentsConfirmation(ureq);
		} else if (source == bulkResetSelectionsButton) {
			doBulkResetSelectionsConfirmation(ureq);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				TBParticipantRow row = dataModel.getObject(se.getIndex());
				if (CMD_DETAILS.equals(cmd)) {
					if (detailsOpenIdentityKeys == null) {
						detailsOpenIdentityKeys = new HashSet<>(1);
					}
					if (detailsOpenIdentityKeys.contains(row.getIdentityKey())) {
						detailsOpenIdentityKeys.remove(row.getIdentityKey());
					} else {
						detailsOpenIdentityKeys.add(row.getIdentityKey());
					}
					loadModel(ureq);
				}
			} else if (event instanceof DetailsToggleEvent) {
				DetailsToggleEvent dte = (DetailsToggleEvent)event;
				TBParticipantRow row = dataModel.getObject(dte.getRowIndex());
				if (dte.isVisible()) {
					doShowDetails(ureq, row);
					setDetailsOpenIdentities();
				} else if (detailsOpenIdentityKeys.contains(row.getIdentityKey())) {
					detailsOpenIdentityKeys.remove(row.getIdentityKey());
					loadModel(ureq);
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				detailsOpenIdentityKeys = null;
				loadModel(ureq);
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq);
			}
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof TBParticipantRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEnrollmentManual(UserRequest ureq) {
		enrollmentManualCtrl = new TBEnrollmentManualProcessController(ureq, getWindowControl(), broker, participantCandidates);
		listenTo(enrollmentManualCtrl);
		
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(broker.getRepositoryEntry().getKey());
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(getIdentity());
		identityEnv.setRoles(ureq.getUserSession().getRoles());
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		
		CourseNode courseNode = course.getRunStructure().getNode(broker.getSubIdent());
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), enrollmentManualCtrl, userCourseEnv, courseNode, "o_icon_topicbroker");
		
		enrollmentLayoutCtr = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, ctrl.getInitialComponent(), null);
		enrollmentLayoutCtr.addDisposableChildController(enrollmentManualCtrl);
		enrollmentLayoutCtr.activate();
		listenTo(enrollmentLayoutCtr);
	}
	
	private void doBulkEmail(UserRequest ureq) {
		if (guardModalController(contactCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("participants.bulk.email.empty.selection");
			return;
		}
		
		List<Long> selectedIdentityKeys = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(TBParticipantRow::getIdentityKey)
				.filter(Objects::nonNull)
				.toList();
		List<Identity> selectedIdentities = securityManager.loadIdentityByKeys(selectedIdentityKeys);
		if (selectedIdentities.isEmpty()) {
			showWarning("participants.bulk.email.empty.selection");
			return;
		}
		
		ContactMessage contactMessage = new ContactMessage(getIdentity());
		ContactList contactList = new ContactList(translate("participants.bulk.email.contacts"));
		contactList.addAllIdentites(selectedIdentities);
		contactMessage.addEmailTo(contactList);
		
		removeAsListenerAndDispose(contactCtrl);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		listenTo(contactCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent(),
				true, translate("participants.bulk.email"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doBulkNotification(UserRequest ureq) {
		if (guardModalController(contactCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("participants.bulk.notification.empty.selection");
			return;
		}
		
		List<Long> selectedIdentityKeys = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(TBParticipantRow::getIdentityKey)
				.filter(Objects::nonNull)
				.toList();
		List<Identity> selectedIdentities = securityManager.loadIdentityByKeys(selectedIdentityKeys);
		
		int identitiesSize = selectedIdentities.size();
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentities(selectedIdentities);
		Set<Long> identityKeysWithSelection = topicBrokerService.getSelections(searchParams).stream()
				.map(selection -> selection.getParticipant().getIdentity().getKey())
				.collect(Collectors.toSet());
		
		selectedIdentities.removeIf(identity -> !identityKeysWithSelection.contains(identity.getKey()));
		int numIdentitiesWithoutSelection = identitiesSize - identityKeysWithSelection.size();
		
		if (selectedIdentities.size() == 0) {
			showWarning("participants.bulk.notification.empty.selection");
			return;
		}
		
		doConfirmNotification(ureq, selectedIdentities, numIdentitiesWithoutSelection);
	}
	
	private void doConfirmNotification(UserRequest ureq) {
		ArrayList<Identity> visibleIdentities = new ArrayList<>(participantCandidates.getVisibleIdentities());
		
		int identitiesSize = visibleIdentities.size();
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentities(visibleIdentities);
		Set<Long> identityKeysWithSelection = topicBrokerService.getSelections(searchParams).stream()
				.map(selection -> selection.getParticipant().getIdentity().getKey())
				.collect(Collectors.toSet());
		
		visibleIdentities.removeIf(identity -> !identityKeysWithSelection.contains(identity.getKey()));
		int numIdentitiesWithoutSelection = identitiesSize - identityKeysWithSelection.size();
		
		doConfirmNotification(ureq, visibleIdentities, numIdentitiesWithoutSelection);
	}
	
	private void doConfirmNotification(UserRequest ureq, List<Identity> identities, int numIdentitiesWithoutSelection) {
		if (guardModalController(notificationConfirmationCtrl)) return;
		
		String message = translate("participants.notifications.mgs", String.valueOf(identities.size()));
		if (numIdentitiesWithoutSelection > 0) {
			message += " " + translate("participants.notifications.mgs.without.selection");
		}
		notificationConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), message, null,
				translate("participants.notifications.button"), ButtonType.submitPrimary);
		notificationConfirmationCtrl.setUserObject(identities);
		listenTo(notificationConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				notificationConfirmationCtrl.getInitialComponent(), true, translate("participants.notifications"),
				true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSendNotification(List<Identity> identities) {
		topicBrokerService.sendEnrollmentEmails(broker, identities);
	}
	
	private void doBulkWithdrawEnrollmentsConfirmation(UserRequest ureq) {
		if (guardModalController(withdrawEnrollmentsConfirmationCtrl)) return;
		
		List<Long> selectedIdentityKeys = new ArrayList<>();
		int numEnrollments = 0;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex != null && !selectedIndex.isEmpty()) {
			for (Integer index : selectedIndex) {
				TBParticipantRow row = dataModel.getObject(index.intValue());
				if (row != null && row.getNumEnrollments() > 0) {
					selectedIdentityKeys.add(row.getIdentityKey());
					numEnrollments += row.getNumEnrollments();
				}
			}
		}
		
		List<Identity> selectedIdentities = securityManager.loadIdentityByKeys(selectedIdentityKeys);
		if (selectedIdentities.isEmpty()) {
			showWarning("participants.bulk.withdraw.enrollments.empty");
			return;
		}
		
		withdrawEnrollmentsConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("participants.bulk.withdraw.enrollments.message", String.valueOf(numEnrollments)),
				translate("participants.bulk.withdraw.enrollments.confirmation"),
				translate("participants.bulk.withdraw.enrollments.button"));
		withdrawEnrollmentsConfirmationCtrl.setUserObject(selectedIdentities);
		listenTo(withdrawEnrollmentsConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				withdrawEnrollmentsConfirmationCtrl.getInitialComponent(), true,
				translate("participants.bulk.withdraw.enrollments"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doWithdrawEnrollments(UserRequest ureq, List<Identity> identities) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setIdentities(identities);
		searchParams.setFetchTopic(true);
		searchParams.setFetchIdentity(true);
		List<TBSelection> selections = topicBrokerService.getSelections(searchParams);
		for (TBSelection selection : selections) {
			if (selection.isEnrolled()) {
				topicBrokerService.withdraw(getIdentity(), selection.getParticipant().getIdentity(), selection.getTopic(), false);
			}
		}
		
		loadModel(ureq);
	}
	
	private void doBulkResetSelectionsConfirmation(UserRequest ureq) {
		if (guardModalController(resetSelectionConfirmationCtrl)) return;
		
		List<Long> selectedIdentityKeys = new ArrayList<>();
		int numSelectionWithoutEnrollments = 0;
		int numSelectionWithEnrollments = 0;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex != null && !selectedIndex.isEmpty()) {
			for (Integer index : selectedIndex) {
				TBParticipantRow row = dataModel.getObject(index.intValue());
				if (row != null && row.getSelections() != null && !row.getSelections().isEmpty()) {
					selectedIdentityKeys.add(row.getIdentityKey());
					numSelectionWithoutEnrollments += row.getSelections().size();
					numSelectionWithoutEnrollments -= row.getNumEnrollments();
					numSelectionWithEnrollments += row.getNumEnrollments();
				}
			}
		}
		
		List<Identity> selectedIdentities = securityManager.loadIdentityByKeys(selectedIdentityKeys);
		if (selectedIdentities.isEmpty()) {
			showWarning("participants.bulk.reset.selections.empty");
			return;
		}
		
		resetSelectionConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("participants.bulk.reset.selections.message", String.valueOf(numSelectionWithoutEnrollments), String.valueOf(numSelectionWithEnrollments)),
				translate("participants.bulk.reset.selections.confirmation"),
				translate("reset"));
		resetSelectionConfirmationCtrl.setUserObject(selectedIdentities);
		listenTo(resetSelectionConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				resetSelectionConfirmationCtrl.getInitialComponent(), true,
				translate("participants.bulk.reset.selections"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doResetSelections(UserRequest ureq, List<Identity> identities) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setIdentities(identities);
		searchParams.setFetchTopic(true);
		searchParams.setFetchIdentity(true);
		List<TBSelection> selections = topicBrokerService.getSelections(searchParams);
		for (TBSelection selection : selections) {
			topicBrokerService.unselect(getIdentity(), selection.getParticipant().getIdentity(), selection.getTopic());
		}
		
		loadModel(ureq);
	}
	
	private void doOpenActivityLog(UserRequest ureq, TBParticipant participant) {
		activityLogCtrl = new TBActivityLogController(ureq, getWindowControl(), broker, participantCandidates, null, participant);
		listenTo(activityLogCtrl);
		
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(broker.getRepositoryEntry().getKey());
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(getIdentity());
		identityEnv.setRoles(ureq.getUserSession().getRoles());
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		
		CourseNode courseNode = course.getRunStructure().getNode(broker.getSubIdent());
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), activityLogCtrl, userCourseEnv, courseNode, "o_icon_topicbroker");
		
		activityLogLayoutCtrl = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, ctrl.getInitialComponent(), null);
		activityLogLayoutCtrl.addDisposableChildController(activityLogCtrl);
		activityLogLayoutCtrl.activate();
		listenTo(activityLogLayoutCtrl);
	}
	
	private void doOpenTools(UserRequest ureq, TBParticipantRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final TBParticipant participant;
		private final List<String> names = new ArrayList<>(5);

		
		public ToolsController(UserRequest ureq, WindowControl wControl, TBParticipantRow row) {
			super(ureq, wControl);
			
			participant = row.getParticipant();
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC);
			
			addLink("activity.log.title", CMD_ACTIVITY_LOG, "o_icon o_icon-fw o_icon_log");
			mainVC.contextPut("names", names);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if (iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			names.add(name);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_ACTIVITY_LOG.equals(cmd)) {
					doOpenActivityLog(ureq, participant);
				}
			}
		}
	}

}
