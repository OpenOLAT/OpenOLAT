/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui.manager;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.coach.ui.manager.GeneratedReportsDataModel.GeneratedReportsCols;
import org.olat.modules.coach.ui.manager.ReportTemplatesDataModel.ReportTemplateCols;

/**
 * Initial date: 2025-01-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ManagerReportsController extends FormBasicController implements Activateable2 {

	private final TooledStackedPanel stackedPanel;
	private FlexiTableElement reportTemplatesTableEl;
	private ReportTemplatesDataModel reportTemplatesDataModel;
	private FlexiTableElement generatedReportsTableEl;
	private GeneratedReportsDataModel generatedReportsDataModel;
	
	public ManagerReportsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl, "manager_reports");
		this.stackedPanel = stackedPanel;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initReportTemplatesTable(formLayout);
		initGeneratedReportsTable(formLayout);
	}

	private void initReportTemplatesTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.name));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.category));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.description));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.type));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.run));
		
		reportTemplatesDataModel = new ReportTemplatesDataModel(columnModel, getLocale());
		reportTemplatesTableEl = uifactory.addTableElement(getWindowControl(), "report.templates", 
				reportTemplatesDataModel, 25, false, getTranslator(), formLayout);
		reportTemplatesTableEl.setNumOfRowsEnabled(false);
	}

	private void initGeneratedReportsTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GeneratedReportsCols.name));
		
		generatedReportsDataModel = new GeneratedReportsDataModel(columnModel, getLocale());
		generatedReportsTableEl = uifactory.addTableElement(getWindowControl(), "generated.reports",
				generatedReportsDataModel, 25, false, getTranslator(), formLayout);
		generatedReportsTableEl.setNumOfRowsEnabled(false);
	}

	private void loadModel() {
		ReportTemplatesRow reportTemplateRow = new ReportTemplatesRow();
		reportTemplateRow.setName("Absences - Last 4 weeks");
		reportTemplateRow.setCategory("Absences");
		reportTemplateRow.setDescription("This report contains all users' absences for the past 4 weeks.");
		reportTemplateRow.setType("Static");
		reportTemplateRow.setRun("Run");
		reportTemplatesDataModel.setObjects(List.of(reportTemplateRow));
		reportTemplatesTableEl.reset();

		GeneratedReportsRow generatedReportRow = new GeneratedReportsRow();
		generatedReportRow.setName("Absences - Last 4 weeks");
		generatedReportsDataModel.setObjects(List.of(generatedReportRow));
		generatedReportsTableEl.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		
	}
}
