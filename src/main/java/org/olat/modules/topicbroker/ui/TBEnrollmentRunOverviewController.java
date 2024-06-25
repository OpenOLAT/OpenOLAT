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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.ui.TBEnrollmentRunDataModel.EnrollmentRunCols;

/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentRunOverviewController extends FormBasicController {

	private FigureWidget participantsWidget;
	private FigureWidget enrollmentsWidget;
	private ProgressBar enrollmentsProgress;
	private TBEnrollmentRunDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private TBBroker broker;

	protected TBEnrollmentRunOverviewController(UserRequest ureq, WindowControl wControl, Form mainForm, TBBroker broker) {
		super(ureq, wControl, LAYOUT_CUSTOM, "enrollments_run_overview", mainForm);
		this.broker = broker;
		
		initForm(ureq);
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
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EnrollmentRunCols.priority));
		
		DefaultFlexiColumnModel enrollmentsColumn = new DefaultFlexiColumnModel(EnrollmentRunCols.enrollments,
				new TextFlexiCellRenderer(EscapeMode.none));
		enrollmentsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		enrollmentsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(enrollmentsColumn);
		
		dataModel = new TBEnrollmentRunDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public void updateModel(TBEnrollmentStats enrollmentStats) {
		participantsWidget.setValue(String.valueOf(enrollmentStats.getNumIdentities()));
		enrollmentsWidget.setValue(String.valueOf(enrollmentStats.getNumEnrollments()));
		enrollmentsWidget.setDesc(translate("widget.enrollments.desc", String.valueOf(enrollmentStats.getNumRequiredEnrollments())));
		enrollmentsProgress.setActual(enrollmentStats.getNumEnrollments());
		enrollmentsProgress.setMax(enrollmentStats.getNumRequiredEnrollments());
		
		List<TBEnrollmentRunRow> rows = new ArrayList<>();
		for (int i = 1; i <= broker.getMaxSelections(); i++) {
			TBEnrollmentRunRow row = new TBEnrollmentRunRow();
			row.setPriority(translate("selection.priority.no", String.valueOf(i)));
			row.setEnrollments(String.valueOf(enrollmentStats.getNumEnrollments(i)));
			rows.add(row);
		}
		
		TBEnrollmentRunRow rowWaitingList = new TBEnrollmentRunRow();
		rowWaitingList.setPriority(translate("selection.status.waiting.list"));
		String waitingListString = "<span><i class=\"o_icon o_icon_warn\"></i> " + enrollmentStats.getNumWaitingList() + "</span>";
		rowWaitingList.setEnrollments(waitingListString);
		rows.add(rowWaitingList);
		
		TBEnrollmentRunRow rowMissing = new TBEnrollmentRunRow();
		rowMissing.setPriority(translate("selection.missing"));
		String missingString = "<span><i class=\"o_icon o_icon_error\"></i> " + enrollmentStats.getNumMissing() + "</span>";
		rowMissing.setEnrollments(missingString);
		rows.add(rowMissing);
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

}
