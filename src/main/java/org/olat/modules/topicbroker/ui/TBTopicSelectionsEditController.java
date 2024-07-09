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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBParticipantDataModel.TBParticipantCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicSelectionsEditController extends FormBasicController {
	
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_BOOSTED = "Boosted";
	private static final String TAB_ID_PRIORITY = "Priority";
	
	private InfoPanel configPanel;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabBoosted;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private TBParticipantDataModel enrollmentsDataModel;
	private FlexiTableElement enrollmentsTableEl;
	private TBParticipantDataModel waitingListDataModel;
	private FlexiTableElement waitingListTableEl;
	private Comparator<TBParticipantRow> rowComporator = Comparator
			.comparingInt(TBParticipantRow::getPrioritySortOrder)
			.thenComparing(Comparator.comparing(TBParticipantRow::getBoost, Comparator.nullsLast(Comparator.naturalOrder())));
	
	private final TBBroker broker;
	private final TBTopic topic;
	private final Long topicKey;
	private final TBParticipantCandidates participantCandidates;
	private Set<Long> allIdentityKeys;
	private Set<Long> visibleIdentityKeys;
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurityManager securityManager;

	protected TBTopicSelectionsEditController(UserRequest ureq, WindowControl wControl, TBBroker broker,
			TBTopic topic, TBParticipantCandidates participantCandidates) {
		super(ureq, wControl, "topic_selections");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.broker = broker;
		this.topic = topic;
		this.topicKey = topic.getKey();
		this.participantCandidates = participantCandidates;
		allIdentityKeys = participantCandidates.getAllIdentities().stream()
				.map(Identity::getKey)
				.collect(Collectors.toSet());
		if (!participantCandidates.isAllIdentitiesVisible()) {
			visibleIdentityKeys = participantCandidates.getVisibleIdentities().stream()
					.map(Identity::getKey)
					.collect(Collectors.toSet());
		}
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TBParticipantDataModel.USAGE_IDENTIFIER,
				isAdministrativeUser);
		
		initForm(ureq);
		initFilterTabs(ureq);
		loadModel();
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
		
		for (int i = 1; i <= broker.getMaxSelections().intValue(); i++) {
			FlexiFiltersTab tabPriority = FlexiFiltersTabFactory.tab(
					TAB_ID_PRIORITY + i,
					translate("tab.priority", String.valueOf(i)),
					TabSelectionBehavior.reloadData);
			tabs.add(tabPriority);
		}
		
		enrollmentsTableEl.setFilterTabs(true, tabs);
		enrollmentsTableEl.setSelectedFilterTab(ureq, tabAll);
		waitingListTableEl.setFilterTabs(true, tabs);
		waitingListTableEl.setSelectedFilterTab(ureq, tabAll);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configPanel = new InfoPanel("configs");
		configPanel.setTitle(translate("configuration"));
		configPanel.setPersistedStatusId(ureq, "tb-topic-selections-edit" + broker.getKey());
		formLayout.add("config", new ComponentWrapperElement(configPanel));
		updateTopicConfigUI();
		updateBrokerStatusUI();
		
		initEnrollmentsTable(formLayout, ureq);
		initWaitingListTable(formLayout, ureq);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setElementCssClass("o_button_group o_button_group_bottom o_button_group_right");
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("close", buttonLayout);
	}
	
	private void initEnrollmentsTable(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = TBParticipantDataModel.USER_PROPS_OFFSET;
		int colFirstname = -1;
		int colLastname = -1;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(TBParticipantDataModel.USAGE_IDENTIFIER, userPropertyHandler);
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, true, "userProp-" + colIndex);
			if (UserConstants.FIRSTNAME.equals(userPropertyHandler.getName())) {
				columnModel.setCellRenderer(new AnonymUserPropRenderer(i));
				colFirstname = colIndex-1;
			}
			if (UserConstants.LASTNAME.equals(userPropertyHandler.getName())) {
				columnModel.setCellRenderer(new AnonymUserPropRenderer(i));
				colLastname = colIndex-1;
			}
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		DefaultFlexiColumnModel boostColumn = new DefaultFlexiColumnModel(TBParticipantCols.boost);
		boostColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		boostColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(boostColumn);
		
		DefaultFlexiColumnModel priorityColumn = new DefaultFlexiColumnModel(TBParticipantCols.prioritySortOrder);
		priorityColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		priorityColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(priorityColumn);
		
		DefaultFlexiColumnModel enrolledColumn = new DefaultFlexiColumnModel(TBParticipantCols.enrolled, new TextFlexiCellRenderer(EscapeMode.none));
		enrolledColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrolledColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(enrolledColumn);
		
		DefaultFlexiColumnModel withdrawColumn = new DefaultFlexiColumnModel(TBParticipantCols.withdraw.i18nHeaderKey(),
				TBParticipantCols.withdraw.ordinal(), "withdraw",
				new BooleanCellRenderer(null, new StaticFlexiCellRenderer(translate("withdraw"), "withdraw")));
		withdrawColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(withdrawColumn);
		
		enrollmentsDataModel = new TBParticipantDataModel(columnsModel, getLocale(), colFirstname, colLastname);
		enrollmentsTableEl = uifactory.addTableElement(getWindowControl(), "enrollmentsTable", enrollmentsDataModel, 20, false, getTranslator(), formLayout);
		enrollmentsTableEl.setAndLoadPersistedPreferences(ureq, "topic-broker-topic-enrollments" + broker.getKey());
	}
	
	private void initWaitingListTable(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = TBParticipantDataModel.USER_PROPS_OFFSET;
		int colFirstname = -1;
		int colLastname = -1;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(TBParticipantDataModel.USAGE_IDENTIFIER, userPropertyHandler);
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, true, "userProp-" + colIndex);
			if (UserConstants.FIRSTNAME.equals(userPropertyHandler.getName())) {
				columnModel.setCellRenderer(new AnonymUserPropRenderer(i));
				colFirstname = colIndex-1;
			}
			if (UserConstants.LASTNAME.equals(userPropertyHandler.getName())) {
				columnModel.setCellRenderer(new AnonymUserPropRenderer(i));
				colLastname = colIndex-1;
			}
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		DefaultFlexiColumnModel boostColumn = new DefaultFlexiColumnModel(TBParticipantCols.boost);
		boostColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		boostColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(boostColumn);
		
		DefaultFlexiColumnModel priorityColumn = new DefaultFlexiColumnModel(TBParticipantCols.prioritySortOrder);
		priorityColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		priorityColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(priorityColumn);
		
		DefaultFlexiColumnModel enrolledColumn = new DefaultFlexiColumnModel(TBParticipantCols.enrolled, new TextFlexiCellRenderer(EscapeMode.none));
		enrolledColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrolledColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(enrolledColumn);
		
		DefaultFlexiColumnModel enrollColumn = new DefaultFlexiColumnModel(TBParticipantCols.enroll.i18nHeaderKey(),
				TBParticipantCols.enroll.ordinal(), "enroll",
				new BooleanCellRenderer(null, new StaticFlexiCellRenderer(translate("enroll"), "enroll")));
		enrollColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(enrollColumn);
		
		waitingListDataModel = new TBParticipantDataModel(columnsModel, getLocale(), colFirstname, colLastname);
		waitingListTableEl = uifactory.addTableElement(getWindowControl(), "waitingListTable", waitingListDataModel, 20, false, getTranslator(), formLayout);
		waitingListTableEl.setAndLoadPersistedPreferences(ureq, "topic-broker-topic-waiting-list" + broker.getKey());
	}
	
	private void loadModel() {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setEnrolledOrIdentities(participantCandidates.getAllIdentities());
		searchParams.setEnrolledOrMaxSortOrder(broker.getMaxSelections());
		searchParams.setFetchIdentity(true);
		List<TBSelection> selections = topicBrokerService.getSelections(searchParams);
		List<TBSelection> topicSelections = selections.stream()
				.filter(selection -> topicKey.equals(selection.getTopic().getKey()))
				.toList();
		Map<Long, List<TBSelection>> participantKeyToSelections = selections.stream()
				.sorted((s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()))
				.collect(Collectors.groupingBy(selection -> selection.getParticipant().getKey()));
		
		List<TBParticipantRow> enrollmentsRows = new ArrayList<>();
		List<TBParticipantRow> waitingListRows = new ArrayList<>();
		for (TBSelection topicSelection : topicSelections) {
			TBParticipant participant = topicSelection.getParticipant();
			
			Identity identity = participant.getIdentity();
			boolean anonym = false;
			if (visibleIdentityKeys != null && !visibleIdentityKeys.contains(identity.getKey())) {
				identity = new TransientIdentity();
				anonym = true;
			}
			TBParticipantRow row = new TBParticipantRow(identity, userPropertyHandlers, getLocale());
			row.setAnonym(anonym);
			row.setBroker(broker);
			row.setMaxSelections(broker.getMaxSelections());
			row.setBoost(participant.getBoost());
			
			row.setPrioritySortOrder(topicSelection.getSortOrder());
			
			row.setSelections(participantKeyToSelections.get(participant.getKey()));
			forgeEnrolled(row, participant);
			
			if (topicSelection.isEnrolled()) {
				enrollmentsRows.add(row);
			} else {
				waitingListRows.add(row);
			}
		}
		
		applyFilters(enrollmentsTableEl, enrollmentsRows);
		enrollmentsRows.sort(rowComporator);
		enrollmentsDataModel.setObjects(enrollmentsRows);
		enrollmentsTableEl.reset(false, false, true);
		
		applyFilters(waitingListTableEl, waitingListRows);
		waitingListRows.sort(rowComporator);
		waitingListDataModel.setObjects(waitingListRows);
		waitingListTableEl.reset(false, false, true);
	}
	
	private void applyFilters(FlexiTableElement tableEl, List<TBParticipantRow> rows) {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabAll) {
			return;
		}
		
		if (tableEl.getSelectedFilterTab() == tabBoosted) {
			rows.removeIf(row -> row.getBoost() == null);
		} else if (tableEl.getSelectedFilterTab().getId().startsWith(TAB_ID_PRIORITY)) {
			int sortOrder = Integer.valueOf(tableEl.getSelectedFilterTab().getId().substring(TAB_ID_PRIORITY.length()));
			rows.removeIf(row -> row.getPrioritySortOrder() != sortOrder);
		}
	}

	private void forgeEnrolled(TBParticipantRow row, TBParticipant participant) {
		long numEnrollments = row.getSelections().stream().filter(TBSelection::isEnrolled).count();
		row.setNumEnrollments((int)numEnrollments);
		
		row.setRequiredEnrollments(TBUIFactory.getRequiredEnrollments(broker, participant));
		
		row.setEnrolledString(translate("enrollments.of", 
				String.valueOf(row.getNumEnrollments()), 
				String.valueOf(row.getRequiredEnrollments())));
		
		if (broker.getEnrollmentStartDate() == null) {
			if (row.getNumEnrollments() < row.getRequiredEnrollments()) {
				String enrolledString = "<span title=\"" + translate("topic.selections.message.too.less.enrollments") + "\"><i class=\"o_icon o_icon_warn\"></i> ";
				enrolledString += row.getEnrolledString();
				enrolledString += "</span>";
				row.setEnrolledString(enrolledString);
			}
		}
		
		if (broker.getEnrollmentStartDate() != null) {
			if (!allIdentityKeys.contains(row.getIdentityKey())) {
				String enrolledString = "<span title=\"" + translate("topic.selections.message.no.participant") + "\"><i class=\"o_icon o_icon_warn\"></i> </span>";
				enrolledString += row.getEnrolledString();
				row.setEnrolledString(enrolledString);
			}
		}
	}
	
	private void updateBrokerStatusUI() {
		flc.contextPut("statusLabel", TBUIFactory.getLabel(getTranslator(), broker));
	}
	
	private void updateTopicConfigUI() {
		String infos = "<ul class=\"list-unstyled\">";
		
		infos += TBUIFactory.createInfo("o_icon_tb_participants", translate("topic.participants.label.min.max",
				String.valueOf(topic.getMinParticipants()),
				String.valueOf(topic.getMaxParticipants())));
		
		infos += "</ul>";
		configPanel.setInformations(infos);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (enrollmentsTableEl == source) {
			if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} else if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				TBParticipantRow row = enrollmentsDataModel.getObject(se.getIndex());
				if("withdraw".equals(cmd)) {
					doWithdraw(row);
				}
			}
		} else if (waitingListTableEl == source) {
			if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				TBParticipantRow row = waitingListDataModel.getObject(se.getIndex());
				if("enroll".equals(cmd)) {
					doEnroll(row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	private void doWithdraw(TBParticipantRow row) {
		Identity participantIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		if (participantIdentity != null) {
			topicBrokerService.withdraw(getIdentity(), participantIdentity, topic, false);
		}
		
		loadModel();
	}

	private void doEnroll(TBParticipantRow row) {
		Identity participantIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		if (participantIdentity != null) {
			topicBrokerService.enroll(getIdentity(), participantIdentity, topic, false);
		}
		
		loadModel();
	}

}
