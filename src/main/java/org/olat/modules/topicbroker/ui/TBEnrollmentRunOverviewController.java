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
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.scope.FormScopeSelection;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.ui.TBEnrollmentRunDataModel.EnrollmentRunCols;
import org.olat.modules.topicbroker.ui.TBTopicDataModel.TopicCols;
import org.olat.modules.topicbroker.ui.components.TBTopicEnrollmentStatusRenderer;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentRunOverviewController extends FormBasicController {

	private static final String SCOPE_TOPICS = "topics";
	private static final String SCOPE_PRIORIY = "priority";
	private static final String SCOPE_WAITING_LIST = "waiting";
	private static final String SCOPE_NO_SELECTION = "no.selection";
	
	private FigureWidget enrollmentsWidget;
	private ProgressBar enrollmentsProgress;
	private FigureWidget topicsWidget;
	private ProgressBar topicsProgress;
	private FigureWidget participantsWidget;
	private FormScopeSelection scopeEl;
	private TBEnrollmentRunDataModel prioriryDataModel;
	private FlexiTableElement priorityTableEl;
	private TBTopicDataModel topicsDataModel;
	private FlexiTableElement topicsTableEl;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private TBParticipantDataModel waitingListDataModel;
	private FlexiTableElement waitingListTableEl;
	private TBParticipantDataModel noSelectionDataModel;
	private FlexiTableElement noSelectionTableEl;
	
	private final TBBroker broker;
	private final List<TBTopic> topics;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;

	protected TBEnrollmentRunOverviewController(UserRequest ureq, WindowControl wControl, Form mainForm,
			TBBroker broker, List<TBTopic> topics) {
		super(ureq, wControl, LAYOUT_CUSTOM, "enrollments_run_overview", mainForm);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.broker = broker;
		this.topics = topics;
		this.topics.sort((r1, r2) -> Integer.compare(r1.getSortOrder(), r2.getSortOrder()));
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TBParticipantDataModel.USAGE_IDENTIFIER,
				isAdministrativeUser);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		WidgetGroup widgetGroup = WidgetFactory.createWidgetGroup("widgets", flc.getFormItemComponent());
		
		enrollmentsWidget = WidgetFactory.createFigureWidget("enrollmentsWidget", flc.getFormItemComponent(),
				translate("widget.enrollments.title"), "o_icon_tb_enrollments");
		enrollmentsProgress = new ProgressBar("scoreProgress", 100, 0, 0, null);
		enrollmentsProgress.setWidthInPercent(true);
		enrollmentsProgress.setLabelAlignment(LabelAlignment.none);
		enrollmentsProgress.setRenderSize(RenderSize.small);
		enrollmentsProgress.setLabelMaxEnabled(false);
		enrollmentsWidget.setAdditionalComp(enrollmentsProgress);
		enrollmentsWidget.setAdditionalCssClass("o_widget_progress");
		widgetGroup.add(enrollmentsWidget);
		
		topicsWidget = WidgetFactory.createFigureWidget("topicsWidget", flc.getFormItemComponent(),
				translate("widget.topics.title"), "o_icon_tb_topics");
		topicsProgress = new ProgressBar("scoreProgress", 100, 0, 0, null);
		topicsProgress.setWidthInPercent(true);
		topicsProgress.setLabelAlignment(LabelAlignment.none);
		topicsProgress.setRenderSize(RenderSize.small);
		topicsProgress.setLabelMaxEnabled(false);
		topicsWidget.setAdditionalComp(topicsProgress);
		topicsWidget.setAdditionalCssClass("o_widget_progress");
		widgetGroup.add(topicsWidget);
		
		participantsWidget = WidgetFactory.createFigureWidget("participantsWidget", flc.getFormItemComponent(),
				translate("widget.participants.title"), "o_icon_num_participants");
		widgetGroup.add(participantsWidget);
		
		List<Scope> scopes = List.of(
				ScopeFactory.createScope(SCOPE_TOPICS, translate("scope.by.topics"), null, "o_icon o_icon_tb_topics"),
				ScopeFactory.createScope(SCOPE_PRIORIY, translate("scope.by.priorities"), null, "o_icon o_icon_tb_priority"),
				ScopeFactory.createScope(SCOPE_WAITING_LIST, translate("scope.waiting.list"), null, "o_icon o_icon_important"),
				ScopeFactory.createScope(SCOPE_NO_SELECTION, translate("scope.no.selection"), null, "o_icon o_icon_error")
				);
		scopeEl = uifactory.addScopeSelection("scope", null, formLayout, scopes);
		scopeEl.addActionListener(FormEvent.ONCHANGE);
		
		
		// Topics
		FlexiTableColumnModel topicsColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		topicsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.identifier));
		topicsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.title));
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(TopicCols.minParticipants, new TextFlexiCellRenderer(EscapeMode.none));
		minParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		minParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		topicsColumnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(TopicCols.maxParticipants);
		maxParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		maxParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		topicsColumnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		topicsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.enrollmentStatus, new TBTopicEnrollmentStatusRenderer()));
		topicsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TopicCols.enrollmentAvailability));
		
		DefaultFlexiColumnModel enrolledColumn = new DefaultFlexiColumnModel(TopicCols.enrolled, new TextFlexiCellRenderer(EscapeMode.none));
		enrolledColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrolledColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		topicsColumnsModel.addFlexiColumnModel(enrolledColumn);
		
		DefaultFlexiColumnModel waitingListColumn = new DefaultFlexiColumnModel(TopicCols.waitingList);
		waitingListColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		waitingListColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		topicsColumnsModel.addFlexiColumnModel(waitingListColumn);
		
		topicsDataModel = new TBTopicDataModel(topicsColumnsModel);
		topicsTableEl = uifactory.addTableElement(getWindowControl(), "topictable", topicsDataModel, 20, false, getTranslator(), formLayout);
		topicsTableEl.setCustomizeColumns(false);
		topicsTableEl.setNumOfRowsEnabled(false);
		
		
		// Priorities
		FlexiTableColumnModel priorityColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		priorityColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EnrollmentRunCols.priority));
		
		DefaultFlexiColumnModel priorityColumn = new DefaultFlexiColumnModel(EnrollmentRunCols.enrollments,
				new TextFlexiCellRenderer(EscapeMode.none));
		priorityColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		priorityColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		priorityColumnsModel.addFlexiColumnModel(priorityColumn);
		
		prioriryDataModel = new TBEnrollmentRunDataModel(priorityColumnsModel);
		priorityTableEl = uifactory.addTableElement(getWindowControl(), "prioritytable", prioriryDataModel, 20, false, getTranslator(), formLayout);
		priorityTableEl.setCustomizeColumns(false);
		priorityTableEl.setNumOfRowsEnabled(false);
		
		
		// Waiting list
		FlexiTableColumnModel waitingListColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = TBParticipantDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(TBParticipantDataModel.USAGE_IDENTIFIER, userPropertyHandler);
			waitingListColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, null, true, propName));
		}
		
		waitingListDataModel = new TBParticipantDataModel(waitingListColumnsModel, getLocale());
		waitingListTableEl = uifactory.addTableElement(getWindowControl(), "waitinglisttable", waitingListDataModel, 20, false, getTranslator(), formLayout);
		waitingListTableEl.setEmptyTableMessageKey("participants.empty.waiting.list");
		waitingListTableEl.setCustomizeColumns(false);
		waitingListTableEl.setNumOfRowsEnabled(false);
		
		
		// No selection
		FlexiTableColumnModel noSelectionColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		colIndex = TBParticipantDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(TBParticipantDataModel.USAGE_IDENTIFIER, userPropertyHandler);
			noSelectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, null, true, propName));
		}
		
		noSelectionDataModel = new TBParticipantDataModel(noSelectionColumnsModel, getLocale());
		noSelectionTableEl = uifactory.addTableElement(getWindowControl(), "noselectiontable", noSelectionDataModel, 20, false, getTranslator(), formLayout);
		noSelectionTableEl.setEmptyTableMessageKey("participants.empty.no.selection");
		noSelectionTableEl.setCustomizeColumns(false);
		noSelectionTableEl.setNumOfRowsEnabled(false);
	}
	
	private void updateUI() {
		topicsTableEl.setVisible(SCOPE_TOPICS.equals(scopeEl.getSelectedKey()));
		priorityTableEl.setVisible(SCOPE_PRIORIY.equals(scopeEl.getSelectedKey()));
		waitingListTableEl.setVisible(SCOPE_WAITING_LIST.equals(scopeEl.getSelectedKey()));
		noSelectionTableEl.setVisible(SCOPE_NO_SELECTION.equals(scopeEl.getSelectedKey()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == scopeEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public void updateModel(TBEnrollmentStats enrollmentStats) {
		// Widgets
		enrollmentsWidget.setValue(String.valueOf(enrollmentStats.getNumEnrollments()));
		enrollmentsWidget.setDesc(translate("widget.enrollments.description", String.valueOf(enrollmentStats.getNumRequiredEnrollments())));
		enrollmentsProgress.setActual(enrollmentStats.getNumEnrollments());
		enrollmentsProgress.setMax(enrollmentStats.getNumRequiredEnrollments());
		
		topicsWidget.setValue(String.valueOf(enrollmentStats.getNumTopicsMinReached()));
		topicsWidget.setDesc(translate("widget.topics.description", String.valueOf(enrollmentStats.getNumTopicsTotal())));
		topicsProgress.setActual(enrollmentStats.getNumTopicsMinReached());
		topicsProgress.setMax(enrollmentStats.getNumTopicsTotal());
		
		participantsWidget.setValue(String.valueOf(enrollmentStats.getNumIdentities()));
		
		
		// Topics
		List<TBTopicRow> topicRows = new ArrayList<>(topics.size());
		for (TBTopic topic : topics) {
			TBTopicRow row = new TBTopicRow(topic);
			row.setNumEnrollments(enrollmentStats.getNumEnrollments(topic));
			row.setWaitingList(enrollmentStats.getNumWaitingList(topic));
			row.setWaitingListString(String.valueOf(row.getWaitingList()));
			row.setEnrolledString(String.valueOf(row.getNumEnrollments()));
			row.setEnrollmentStatus(TBUIFactory.getEnrollmentStatus(topic.getMinParticipants(), row.getNumEnrollments()));
			row.setTranslatedEnrollmentStatus(TBUIFactory.getTranslatedStatus(getTranslator(), row.getEnrollmentStatus()));
			row.setAvailability(TBUIFactory.getAvailability(getTranslator(), row.getEnrollmentStatus(),
					row.getMaxParticipants(), row.getNumEnrollments(), row.getWaitingList()));
			
			if (topic.getMinParticipants() != null) {
				row.setMinParticipantsString(topic.getMinParticipants().toString());
			}
			
			topicRows.add(row);
		}
		
		topicsDataModel.setObjects(topicRows);
		topicsTableEl.reset();
		
		
		// Priorities
		List<TBEnrollmentRunRow> enrollmentRows = new ArrayList<>();
		for (int i = 1; i <= broker.getMaxSelections(); i++) {
			TBEnrollmentRunRow row = new TBEnrollmentRunRow();
			row.setPriority(translate("selection.priority.no", String.valueOf(i)));
			row.setEnrollments(String.valueOf(enrollmentStats.getNumEnrollments(i)));
			enrollmentRows.add(row);
		}
		
		prioriryDataModel.setObjects(enrollmentRows);
		priorityTableEl.reset();
		
		
		// Waiting list
		List<TBParticipantRow> waitingListRows = new ArrayList<>(enrollmentStats.getIdentitiesWaitingList().size());
		for (Identity identity : enrollmentStats.getIdentitiesWaitingList()) {
			TBParticipantRow row = new TBParticipantRow(identity, userPropertyHandlers, getLocale());
			waitingListRows.add(row);
		}
		
		waitingListDataModel.setObjects(waitingListRows);
		waitingListTableEl.reset();
		
		
		// No selection
		List<TBParticipantRow> noSelectionRows = new ArrayList<>(enrollmentStats.getIdentitiesNoSelection().size());
		for (Identity identity : enrollmentStats.getIdentitiesNoSelection()) {
			TBParticipantRow row = new TBParticipantRow(identity, userPropertyHandlers, getLocale());
			noSelectionRows.add(row);
		}
		
		noSelectionDataModel.setObjects(noSelectionRows);
		noSelectionTableEl.reset();
	}

}
