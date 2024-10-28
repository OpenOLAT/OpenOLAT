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
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.ui.TBEnrollmentRunDataModel.EnrollmentRunCols;
import org.olat.modules.topicbroker.ui.TBTopicDataModel.TopicCols;

/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentRunOverviewController extends FormBasicController {

	private static final String SCOPE_ENROLLMENTS = "participants";
	private static final String SCOPE_TOPICS = "topics";
	
	private FigureWidget participantsWidget;
	private FigureWidget enrollmentsWidget;
	private ProgressBar enrollmentsProgress;
	private FigureWidget topicsWidget;
	private ProgressBar topicsProgress;
	private FormScopeSelection scopeEl;
	private TBEnrollmentRunDataModel enrollmentsDataModel;
	private FlexiTableElement enrollmentsTableEl;
	private TBTopicDataModel topicsDataModel;
	private FlexiTableElement topicsTableEl;
	
	private final TBBroker broker;
	private final List<TBTopic> topics;

	protected TBEnrollmentRunOverviewController(UserRequest ureq, WindowControl wControl, Form mainForm,
			TBBroker broker, List<TBTopic> topics) {
		super(ureq, wControl, LAYOUT_CUSTOM, "enrollments_run_overview", mainForm);
		this.broker = broker;
		this.topics = topics;
		this.topics.sort((r1, r2) -> Integer.compare(r1.getSortOrder(), r2.getSortOrder()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		WidgetGroup widgetGroup = WidgetFactory.createWidgetGroup("widgets", flc.getFormItemComponent());
		
		participantsWidget = WidgetFactory.createFigureWidget("participantsWidget", flc.getFormItemComponent(),
				translate("widget.participants.title"), "o_icon_tb_participants");
		widgetGroup.add(participantsWidget);
		
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
		
		List<Scope> scopes = List.of(
				ScopeFactory.createScope(SCOPE_ENROLLMENTS, translate("scope.enrollments"), null),
				ScopeFactory.createScope(SCOPE_TOPICS, translate("scope.topics"), null)
				);
		scopeEl = uifactory.addScopeSelection("scope", null, formLayout, scopes);
		scopeEl.addActionListener(FormEvent.ONCHANGE);
		
		
		// Enrollments
		FlexiTableColumnModel enrollmentsColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		enrollmentsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EnrollmentRunCols.priority));
		
		DefaultFlexiColumnModel enrollmentsColumn = new DefaultFlexiColumnModel(EnrollmentRunCols.enrollments,
				new TextFlexiCellRenderer(EscapeMode.none));
		enrollmentsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrollmentsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrollmentsColumnsModel.addFlexiColumnModel(enrollmentsColumn);
		
		enrollmentsDataModel = new TBEnrollmentRunDataModel(enrollmentsColumnsModel);
		enrollmentsTableEl = uifactory.addTableElement(getWindowControl(), "enrollmenttable", enrollmentsDataModel, 20, false, getTranslator(), formLayout);
		enrollmentsTableEl.setCustomizeColumns(false);
		enrollmentsTableEl.setNumOfRowsEnabled(false);
		
		
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
		
		DefaultFlexiColumnModel enrolledColumn = new DefaultFlexiColumnModel(TopicCols.enrolled, new TextFlexiCellRenderer(EscapeMode.none));
		enrolledColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrolledColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		topicsColumnsModel.addFlexiColumnModel(enrolledColumn);
		
		topicsDataModel = new TBTopicDataModel(topicsColumnsModel);
		topicsTableEl = uifactory.addTableElement(getWindowControl(), "topictable", topicsDataModel, 20, false, getTranslator(), formLayout);
		topicsTableEl.setCustomizeColumns(false);
		topicsTableEl.setNumOfRowsEnabled(false);
	}
	
	private void updateUI() {
		boolean enrollments = SCOPE_ENROLLMENTS.equals(scopeEl.getSelectedKey());
		enrollmentsTableEl.setVisible(enrollments);
		topicsTableEl.setVisible(!enrollments);
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
		participantsWidget.setValue(String.valueOf(enrollmentStats.getNumIdentities()));
		
		enrollmentsWidget.setValue(String.valueOf(enrollmentStats.getNumEnrollments()));
		enrollmentsWidget.setDesc(translate("widget.enrollments.desc", String.valueOf(enrollmentStats.getNumRequiredEnrollments())));
		enrollmentsProgress.setActual(enrollmentStats.getNumEnrollments());
		enrollmentsProgress.setMax(enrollmentStats.getNumRequiredEnrollments());
		
		topicsWidget.setValue(String.valueOf(enrollmentStats.getNumTopicsMinReached()));
		topicsWidget.setDesc(translate("widget.topics.desc", String.valueOf(enrollmentStats.getNumTopicsTotal())));
		topicsProgress.setActual(enrollmentStats.getNumTopicsMinReached());
		topicsProgress.setMax(enrollmentStats.getNumTopicsTotal());
		
		
		// Enrollments
		List<TBEnrollmentRunRow> enrollmentRows = new ArrayList<>();
		for (int i = 1; i <= broker.getMaxSelections(); i++) {
			TBEnrollmentRunRow row = new TBEnrollmentRunRow();
			row.setPriority(translate("selection.priority.no", String.valueOf(i)));
			row.setEnrollments(String.valueOf(enrollmentStats.getNumEnrollments(i)));
			enrollmentRows.add(row);
		}
		
		TBEnrollmentRunRow rowWaitingList = new TBEnrollmentRunRow();
		rowWaitingList.setPriority(translate("selection.status.waiting.list"));
		String waitingListString = "<span><i class=\"o_icon o_icon_warn\"></i> " + enrollmentStats.getNumWaitingList() + "</span>";
		rowWaitingList.setEnrollments(waitingListString);
		enrollmentRows.add(rowWaitingList);
		
		TBEnrollmentRunRow rowMissing = new TBEnrollmentRunRow();
		rowMissing.setPriority(translate("selection.missing"));
		String missingString = "<span><i class=\"o_icon o_icon_error\"></i> " + enrollmentStats.getNumMissing() + "</span>";
		rowMissing.setEnrollments(missingString);
		enrollmentRows.add(rowMissing);
		
		enrollmentsDataModel.setObjects(enrollmentRows);
		enrollmentsTableEl.reset();
		
		
		// Topics
		List<TBTopicRow> topicRows = new ArrayList<>(topics.size());
		for (TBTopic topic : topics) {
			TBTopicRow row = new TBTopicRow(topic);
			row.setNumEnrollments(enrollmentStats.getNumEnrollments(topic));
			row.setEnrolledString(String.valueOf(row.getNumEnrollments()));
			
			if (topic.getMinParticipants() != null) {
				row.setMinParticipantsString(topic.getMinParticipants().toString());
				if (row.getNumEnrollments() == 0) {
					String minParticipantsString = "<span title=\"" + translate("topic.selections.message.selections.less.min") + "\"><i class=\"o_icon o_icon_error\"></i> ";
					minParticipantsString += row.getMinParticipantsString();
					minParticipantsString += "</span>";
					row.setMinParticipantsString(minParticipantsString);
				}
			}
			
			topicRows.add(row);
		}
		
		topicsDataModel.setObjects(topicRows);
		topicsTableEl.reset();
	}

}
