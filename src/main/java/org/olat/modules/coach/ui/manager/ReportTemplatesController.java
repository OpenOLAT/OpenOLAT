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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.coach.reports.ReportConfiguration;
import org.olat.modules.coach.ui.manager.ReportTemplatesDataModel.ReportTemplateCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-01-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ReportTemplatesController extends FormBasicController {
	private static final String PROPS_IDENTIFIER = ReportTemplatesController.class.getName();
	private static final String PLAY_CMD = "play";
	private static final String FILTER_CATEGORY = "filter.category";

	private FlexiTableElement tableEl;
	private ReportTemplatesDataModel tableModel;
	private List<UserPropertyHandler> userPropertyHandlers;
	private int count = 0;

	@Autowired
	private UserManager userManager;
	@Autowired
	private List<ReportConfiguration> reportConfigurations;

	public ReportTemplatesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "report_templates");

		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);

		initForm(ureq);
		loadModel();
		
		initFilters();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.name));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.category));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.description));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.type));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportTemplateCols.run));

		tableModel = new ReportTemplatesDataModel(columnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list",
				tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void loadModel() {
		List<ReportTemplatesRow> rows = new ArrayList<>();

		for (ReportConfiguration reportConfiguration : reportConfigurations) {
			ReportTemplatesRow row = new ReportTemplatesRow();
			row.setReportConfiguration(reportConfiguration);
			row.setName(reportConfiguration.getName(getLocale()));
			row.setCategory(reportConfiguration.getCategory(getLocale()));
			row.setDescription(reportConfiguration.getDescription(getLocale()));
			row.setType(translate("type." + (reportConfiguration.isDynamic() ? "dynamic" : "static")));
			rows.add(row);
		}

		applyFilters(rows);

		for (ReportTemplatesRow row : rows) {
			forgeRow(row);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void applyFilters(List<ReportTemplatesRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) {
			return;
		}
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_CATEGORY.equals(filter.getFilter())) {
				List<String> categories = ((FlexiTableMultiSelectionFilter) filter).getValues();
				if (categories != null && !categories.isEmpty()) {
					rows.removeIf(row -> row.getCategory() == null || !categories.contains(row.getCategory()));
				}
			}
		}
	}

	private void forgeRow(ReportTemplatesRow row) {
		String playId = "play-" + count++;
		FormLink playLink = uifactory.addFormLink(playId, PLAY_CMD, "", null, flc, Link.NONTRANSLATED);
		playLink.setIconLeftCSS("o_icon o_icon-lg o_icon_play");
		playLink.setUserObject(row);
		row.setPlayLink(playLink);
		flc.add(playLink);
		flc.add(playId, playLink);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink formLink) {
			if (PLAY_CMD.equals(formLink.getCmd())) {
				if (formLink.getUserObject() instanceof ReportTemplatesRow row) {
					doRunReport(ureq, row);
				}
			}
		} else if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doRunReport(UserRequest ureq, ReportTemplatesRow row) {
		row.getReportConfiguration().generateReport(getIdentity(), ureq.getLocale(), userPropertyHandlers);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public void reload() {
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues categoryKV = new SelectionValues();
		tableModel.getObjects().stream().map(ReportTemplatesRow::getCategory)
				.filter(StringHelper::containsNonWhitespace).distinct()
				.forEach(category -> categoryKV.add(SelectionValues.entry(category, category)));
		categoryKV.sort(SelectionValues.VALUE_ASC);
		if (!categoryKV.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.category"), FILTER_CATEGORY, 
					categoryKV, true));
		}
		
		tableEl.setFilters(true, filters, false, true);
	}
}
