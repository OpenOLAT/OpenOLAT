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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 6 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewCurriculumsController extends AbstractImportListController {
	
	public ImportCurriculumsReviewCurriculumsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			ImportCurriculumsContext context, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, "import_review_curriculums", context, runContext);

		initForm(ureq);
		
		// Load data for filters
		loadModel(ureq);
		// Initialize filters
		initFilterTabs();
		initFilters();
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.status,
				new ImportStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.infos));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.ignore));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.displayName,
				new ImportValueCellRenderer(ImportCurriculumsCols.displayName, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.identifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.identifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.organisationIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.organisationIdentifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.absences,
				new ImportValueCellRenderer(ImportCurriculumsCols.absences, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.description,
				new ImportValueCellRenderer(ImportCurriculumsCols.description, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImportCurriculumsCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImportCurriculumsCols.lastModified,
				new DateTimeFlexiCellRenderer(getLocale())));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		// Status
		SelectionValues statusKV = new SelectionValues();
		statusKV.add(SelectionValues.entry(STATUS_NEW, translate("search.status.new")));
		statusKV.add(SelectionValues.entry(STATUS_MODIFIED, translate("search.status.modified")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_ERRORS, translate("search.status.errors")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_WARNINGS, translate("search.status.warnings")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_CHANGES, translate("search.status.changes")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.status"),
				STATUS_KEY, statusKV, true));
		
		// Organisations
		SelectionValues organisationsKV = new SelectionValues();
		List<Organisation> organisations = tableModel.getOrganisations();
		for(Organisation organisation:organisations) {
			StringBuilder sb = new StringBuilder();
			if(StringHelper.containsNonWhitespace(organisation.getDisplayName())) {
				sb.append(organisation.getDisplayName());
			}
			if(StringHelper.containsNonWhitespace(organisation.getIdentifier())) {
				sb.append(" \u00B7 ").append(organisation.getIdentifier());
			}
			organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(), sb.toString()));
		}

		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.organisations"),
				ORGANISATION_KEY, organisationsKV, true));
	}

	private void loadModel(UserRequest ureq) {
		List<ImportedRow> rows = context.getImportedCurriculumsRows();
		if(rows == null) {
			rows = List.of();
		} else {
			final Roles roles = ureq.getUserSession().getRoles();
			final ImportCurriculumsHelper helper = new ImportCurriculumsHelper(getTranslator());
			helper.loadCurrentCurriculums(rows);
			
			for(ImportedRow row:rows) {
				helper.validate(row, roles);
			}
			helper.validateCurriculumsUniqueIdentifiers(rows);
			
			SelectionValues ignorePK = new SelectionValues();
			ignorePK.add(SelectionValues.entry(IGNORE, ""));
			
			for(ImportedRow row:rows) {
				forgeRow(row, ignorePK);
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		loadErrorMessage(rows);
	}
	
	private void loadErrorMessage(List<ImportedRow> rows) {
		long numOfErrors = rows.stream()
			.filter(row -> row.getStatus() == ImportCurriculumsStatus.ERROR || row.getValidationStatistics().errors() > 0)
			.count();

		if(numOfErrors > 0) {
			String i18nKey = numOfErrors > 1 ? "error.link.plural" : "error.link";
			String link = translate(i18nKey, Long.toString(numOfErrors));	
			errorsLink = uifactory.addFormLink("errors.link", link, null, flc, Link.LINK | Link.NONTRANSLATED);
		} else {
			flc.remove("errors.link");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void back() {
		context.setImportedCurriculumsRows(null);
		context.setImportedElementsRows(null);
		super.back();
	}

	@Override
	protected void formNext(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
