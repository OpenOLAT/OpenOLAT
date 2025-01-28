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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.coach.reports.ReportConfiguration;
import org.olat.modules.coach.ui.manager.ReportTemplatesDataModel.ReportTemplateCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-01-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ManagerReportsController extends FormBasicController implements Activateable2 {

	private final TooledStackedPanel stackedPanel;
	private FlexiTableElement reportTemplatesTableEl;
	private ReportTemplatesDataModel reportTemplatesDataModel;
	
	private GeneratedReportsController generatedReportsController;

	private int count = 0;

	@Autowired
	private List<ReportConfiguration> reportConfigurations;
	
	public ManagerReportsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl, "manager_reports");
		this.stackedPanel = stackedPanel;
		
		initForm(ureq);

		generatedReportsController = new GeneratedReportsController(ureq, wControl);
		listenTo(generatedReportsController);
		flc.put("generated.reports", generatedReportsController.getInitialComponent());

		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initReportTemplatesTable(formLayout);
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

	private void loadModel() {
		List<ReportTemplatesRow> rows = new ArrayList<>();

		for (ReportConfiguration reportConfiguration : reportConfigurations) {
			ReportTemplatesRow row = new ReportTemplatesRow();
			row.setName(reportConfiguration.getName(getLocale()));
			row.setCategory(reportConfiguration.getCategory(getLocale()));
			row.setDescription(reportConfiguration.getDescription(getLocale()));
			row.setType(translate("type." + (reportConfiguration.isDynamic() ? "dynamic" : "static")));
			row.setRun("Configured");
			forgeRow(row);
			rows.add(row);
		}
		
		reportTemplatesDataModel.setObjects(rows);
		reportTemplatesTableEl.reset();
		generatedReportsController.reload();
	}

	private void forgeRow(ReportTemplatesRow row) {
		String playId = "play-" + count++;
		FormLink playLink = uifactory.addFormLink(playId, "play", "", null, flc, Link.NONTRANSLATED);
		playLink.setIconLeftCSS("o_icon o_icon-lg o_icon_play");
		playLink.setUserObject(row);
		row.setPlayLink(playLink);
		flc.add(playLink);
		flc.add(playId, playLink);
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		
	}

	public void reload() {
		loadModel();
	}
}
