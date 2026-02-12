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
import java.util.Set;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewCurriculumsTableModel extends DefaultFlexiTableDataModel<CurriculumImportedRow>
implements FilterableFlexiTableModel {
	
	private static final ImportCurriculumsCols[] COLS = ImportCurriculumsCols.values();

	private List<CurriculumImportedRow> backupList;
	
	public ImportCurriculumsReviewCurriculumsTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			final boolean ignored = isFilterSelected(filters, ImportCurriculumsReviewCurriculumsController.IGNORED_KEY);
			final Set<String> status = getFilteredList(filters, ImportCurriculumsReviewCurriculumsController.STATUS_KEY);
			
			List<CurriculumImportedRow> filteredRows = new ArrayList<>(backupList.size());
			for(CurriculumImportedRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptIgnored(ignored, row)
						&& acceptStatus(status, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private Set<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? Set.copyOf(filterValues) : Set.of();
		}
		return Set.of();
	}
	
	private boolean isFilterSelected(List<FlexiTableFilter> filters, String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}
	
	private boolean acceptStatus(Set<String> status, CurriculumImportedRow row) {
		if(status == null || status.isEmpty()) return true;
		
		if((row.getStatus() == ImportCurriculumsStatus.ERROR && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_WITH_ERRORS))
				|| (row.getStatus() == ImportCurriculumsStatus.MODIFIED && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_MODIFIED))) {
			return true;
		}
		
		CurriculumImportedStatistics statistics = row.getValidationStatistics();
		if((statistics.errors() > 0 && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_WITH_ERRORS))
				|| (statistics.warnings() > 0 && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_WITH_WARNINGS))
				|| (statistics.changes() > 0 && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_WITH_CHANGES))) {
			return true;
		}
		return false;
	}
	
	private boolean acceptIgnored(boolean ignored, CurriculumImportedRow row) {
		if(!ignored) return true;
		return row.isIgnored();
	}
	
	private boolean accept(String searchValue, CurriculumImportedRow row) {
		if(searchValue == null) return true;
		return accept(searchValue, row.getDisplayName())
				|| accept(searchValue, row.getIdentifier())
				|| accept(searchValue, row.getDescription());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumImportedRow importedRow = getObject(row);
		return switch(COLS[col]) {
			case rowNum -> Integer.valueOf(importedRow.getRowNum());
			case status -> importedRow.getStatus();
			case infos -> importedRow.getValidationResultsLink();
			case ignore -> importedRow.getIgnoreEl();
			case displayName -> importedRow.getDisplayName();
			case identifier -> importedRow.getIdentifier();
			case organisationIdentifier -> importedRow.getOrganisationIdentifier();
			case absences -> importedRow.getAbsences();
			case description -> importedRow.getDescription();
			case creationDate -> importedRow.getCreationDate();
			case lastModified -> importedRow.getLastModified();
			case tools -> Boolean.valueOf(importedRow.getIgnoreEl() != null);
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CurriculumImportedRow> objects) {
		this.backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum ImportCurriculumsCols implements FlexiSortableColumnDef {
		rowNum("table.header.row.num"),
		status("table.header.import.status"),
		infos("table.header.import.infos"),
		ignore("table.header.ignore"),
		displayName("table.header.title"),
		identifier("table.header.identifier"),
		organisationIdentifier("table.header.organisation.identifier"),
		absences("table.header.absences"),
		description("table.header.description"),
		creationDate("table.header.creation.date"),
		lastModified("table.header.last.modified"),
		tools("action.more");
		
		private final String i18nHeaderKey;
		
		private ImportCurriculumsCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
