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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewElementsController extends AbstractImportListController implements FlexiTableCssDelegate {
	
	@Autowired
	private TaxonomyModule taxonomyModule;
	
	public ImportCurriculumsReviewElementsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			ImportCurriculumsContext context, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, "import_review_elements", context, runContext);
		initForm(ureq);
		
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
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.curriculumIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.curriculumIdentifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.implementationIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.implementationIdentifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.objectType,
				new ImportValueCellRenderer(ImportCurriculumsCols.objectType, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.level));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.displayName,
				new ImportValueCellRenderer(ImportCurriculumsCols.displayName, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.identifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.identifier, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.elementStatus,
				new ImportValueCellRenderer(ImportCurriculumsCols.elementStatus, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.startDate,
				new ImportValueCellRenderer(ImportCurriculumsCols.startDate, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.startTime,
				new ImportValueCellRenderer(ImportCurriculumsCols.startTime, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.endDate,
				new ImportValueCellRenderer(ImportCurriculumsCols.endDate, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.endTime,
				new ImportValueCellRenderer(ImportCurriculumsCols.endTime, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.unit,
				new ImportValueCellRenderer(ImportCurriculumsCols.unit, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.referenceIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.referenceIdentifier, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.location,
				new ImportValueCellRenderer(ImportCurriculumsCols.location, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.elementType,
				new ImportValueCellRenderer(ImportCurriculumsCols.elementType, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.calendar,
				new ImportValueCellRenderer(ImportCurriculumsCols.calendar, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.absences,
				new ImportValueCellRenderer(ImportCurriculumsCols.absences, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.progress,
				new ImportValueCellRenderer(ImportCurriculumsCols.progress, getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.taxonomyLevels,
				new ImportValueCellRenderer(ImportCurriculumsCols.taxonomyLevels, getLocale())));
		
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
		
		// Types
		SelectionValues objectTypeKV = new SelectionValues();
		objectTypeKV.add(SelectionValues.entry(CurriculumExportType.IMPL.name(), translate("curriculum.export.type.impl")));
		objectTypeKV.add(SelectionValues.entry(CurriculumExportType.ELEM.name(), translate("curriculum.export.type.elem")));
		objectTypeKV.add(SelectionValues.entry(CurriculumExportType.COURSE.name(), translate("curriculum.export.type.course")));
		objectTypeKV.add(SelectionValues.entry(CurriculumExportType.TMPL.name(), translate("curriculum.export.type.tmpl")));
		objectTypeKV.add(SelectionValues.entry(CurriculumExportType.EVENT.name(), translate("curriculum.export.type.event")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.object.type"),
				OBJECT_TYPE_KEY, objectTypeKV, true));
	}

	private void loadModel(UserRequest ureq) {
		List<ImportedRow> rows = context.getImportedElementsRows();
		if(rows == null) {
			rows = List.of();
		} else {
			final Roles roles = ureq.getUserSession().getRoles();
			final ImportCurriculumsHelper helper = new ImportCurriculumsHelper(getIdentity(), roles, getTranslator());
			helper.loadCurrentElements(rows, context.getImportedCurriculumsRows());
			if(taxonomyModule.isEnabled()) {
				helper.loadTaxonomy(rows);
			}
			
			for(ImportedRow row:rows) {
				helper.validate(row);
			}
			helper.validateUniqueIdentifiers(rows);
			
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
			String i18nKey = numOfErrors > 1 ? "error.elements.link.plural" : "error.elements.link";
			String link = translate(i18nKey, Long.toString(numOfErrors));	
			errorsLink = uifactory.addFormLink("errors.elements.link", link, null, flc, Link.LINK | Link.NONTRANSLATED);
		} else {
			flc.remove("errors.link");
		}
	}
	
	@Override
	protected void formFinish(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}