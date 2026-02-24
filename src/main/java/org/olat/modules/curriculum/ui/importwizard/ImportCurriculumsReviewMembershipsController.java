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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewMembershipsController extends AbstractImportListController {

	protected static final String SORT_RELEVANCE_KEY = "mrelevance";
	
	private ImportCurriculumsReviewMembershipsTableModel tableModel;
	
	public ImportCurriculumsReviewMembershipsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			ImportCurriculumsContext context, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, "import_review_memberships", context, runContext, "Memberships", false, false);
		initForm(ureq);
		
		// Load data for filters
		loadModel();
		// Initialize filters
		initFilterTabs();
		initFilters();
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.status,
				new ImportStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.infosErrors));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.ignore));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.curriculumIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.curriculumIdentifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.implementationIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.implementationIdentifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.identifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.identifier, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.role,
				new ImportValueCellRenderer(ImportCurriculumsCols.role, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.username,
				new ImportValueCellRenderer(ImportCurriculumsCols.username, getLocale())));
	}
	
	@Override
	protected DefaultFlexiTableDataModel<? extends AbstractImportRow> initTableModel(FlexiTableColumnModel columnsModel) {
		tableModel = new ImportCurriculumsReviewMembershipsTableModel(columnsModel, getLocale());
		return tableModel;
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		// Status
		SelectionValues statusKV = new SelectionValues();
		statusKV.add(SelectionValues.entry(STATUS_NEW, translate("search.status.new")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_ERRORS, translate("search.status.errors")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_CHANGES, translate("search.status.changes")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.object.status"),
				STATUS_KEY, statusKV, true));
		
		// Products / curriculums
		SelectionValues curriculumsKV = new SelectionValues();
		List<ImportedRow> curriculumRows = context.getImportedCurriculumsRows();
		for(ImportedRow curriculumRow:curriculumRows) {
			String curriculumIdentifier = curriculumRow.getIdentifier();
			if(StringHelper.containsNonWhitespace(curriculumIdentifier)) {
				StringBuilder sb = new StringBuilder();
				if(StringHelper.containsNonWhitespace(curriculumRow.getDisplayName())) {
					sb.append(curriculumRow.getDisplayName());
				}
				sb.append(" \u00B7 ").append(curriculumIdentifier);
				curriculumsKV.add(SelectionValues.entry(curriculumIdentifier, sb.toString()));
			}
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.curriculum"),
				CURRICULUM_KEY, curriculumsKV, true));
		
		// Implementations
		SelectionValues implementationsKV = new SelectionValues();
		List<ImportedRow> implementationsRows = context.getImportedElementsRows();
		for(ImportedRow implementationRow:implementationsRows) {
			if(implementationRow.type() == CurriculumExportType.IMPL) {
				String identifier = implementationRow.getIdentifier();
				if(StringHelper.containsNonWhitespace(identifier)) {
					StringBuilder sb = new StringBuilder();
					if(StringHelper.containsNonWhitespace(implementationRow.getDisplayName())) {
						sb.append(implementationRow.getDisplayName());
					}
					sb.append(" \u00B7 ").append(identifier);
					implementationsKV.add(SelectionValues.entry(identifier, sb.toString()));
				}
			}
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.implementation"),
				IMPLEMENTATION_KEY, implementationsKV, true));
		
		// User names
		SelectionValues usernamesKV = new SelectionValues();
		List<String> usernames = tableModel.getUsernames();
		for(String username:usernames) {
			usernamesKV.add(SelectionValues.entry(username, username));
		}

		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.usernames"),
				USERNAME_KEY, usernamesKV, true));
		
	}
	
	@Override
	protected void sortOptions() {
		List<FlexiTableSort> sorters = new ArrayList<>();
		sorters.add(new FlexiTableSort(translate("sort.relevance"), SORT_RELEVANCE_KEY));
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		tableEl.setSortSettings(options);
	}

	private void loadModel() {
		List<ImportedMembershipRow> rows = context.getImportedMembershipsRows();
		if(rows == null) {
			rows = List.of();
		} else {
			ImportCurriculumsObjectsLoader loader = context.getLoader();
			loader.loadMemberships(rows, context.getImportedCurriculumsRows(),
					context.getImportedElementsRows(), context.getImportedUsersRows());
			
			ImportCurriculumsValidator validator = context.getValidator();
			for(ImportedMembershipRow row:rows) {
				validator.validate(row);
			}
			
			SelectionValues ignorePK = new SelectionValues();
			ignorePK.add(SelectionValues.entry(IGNORE, ""));
			
			for(ImportedMembershipRow row:rows) {
				forgeRow(row, ignorePK);
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		loadErrorMessage(rows, "error.members");
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
