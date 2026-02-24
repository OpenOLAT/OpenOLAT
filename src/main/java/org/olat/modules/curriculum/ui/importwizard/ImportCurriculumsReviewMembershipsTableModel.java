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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewMembershipsTableModel extends DefaultFlexiTableDataModel<ImportedMembershipRow>
implements FilterableFlexiTableModel, SortableFlexiTableDataModel<ImportedMembershipRow> {
	
	private static final ImportCurriculumsCols[] COLS = ImportCurriculumsCols.values();

	private final Locale locale;
	private List<ImportedMembershipRow> backupList;
	
	public ImportCurriculumsReviewMembershipsTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public List<String> getUsernames() {
		if(backupList == null) List.of();

		Set<String> usernames = backupList.stream()
			.map(ImportedMembershipRow::getUsername)
			.filter(username -> StringHelper.containsNonWhitespace(username))
			.collect(Collectors.toSet());
		return List.copyOf(usernames);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ImportedMembershipRow> rows = new ImportCurriculumsReviewMembershipsTableSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			final boolean ignored = isFilterSelected(filters, ImportCurriculumsReviewCurriculumsController.IGNORED_KEY);
			final Set<String> status = getFilteredList(filters, ImportCurriculumsReviewCurriculumsController.STATUS_KEY);
			final Set<String> usernames = getFilteredList(filters, ImportCurriculumsReviewCurriculumsController.USERNAME_KEY);
			final Set<String> curriculums = getFilteredList(filters, ImportCurriculumsReviewCurriculumsController.CURRICULUM_KEY);
			final Set<String> implementations = getFilteredList(filters, ImportCurriculumsReviewCurriculumsController.IMPLEMENTATION_KEY);
			
			List<ImportedMembershipRow> filteredRows = new ArrayList<>(backupList.size());
			for(ImportedMembershipRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptImplementations(implementations, row)
						&& acceptCurriculums(curriculums, row)
						&& acceptUsername(usernames, row)
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
	
	private boolean acceptStatus(Set<String> status, ImportedMembershipRow row) {
		if(status == null || status.isEmpty()) return true;
		
		if((row.getStatus() == ImportCurriculumsStatus.ERROR && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_WITH_ERRORS))
				|| (row.getStatus() == ImportCurriculumsStatus.MODIFIED && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_MODIFIED))) {
			return true;
		}
		
		if(status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_NEW)) {
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
	
	private boolean acceptCurriculums(Set<String> identifiers, ImportedMembershipRow row) {
		if(identifiers == null || identifiers.isEmpty()) return true;
		
		String rowIdentifier = row.getCurriculumIdentifier();
		return rowIdentifier != null && identifiers.contains(rowIdentifier);
	}
	
	private boolean acceptImplementations(Set<String> identifiers, ImportedMembershipRow row) {
		if(identifiers == null || identifiers.isEmpty()) return true;
		
		String rowIdentifier = row.getImplementationIdentifier();
		return rowIdentifier != null && identifiers.contains(rowIdentifier);
	}
	
	private boolean acceptUsername(Set<String> usernames, ImportedMembershipRow row) {
		if(usernames == null || usernames.isEmpty()) return true;
		
		String username = row.getUsername();
		return StringHelper.containsNonWhitespace(username) && usernames.contains(username);
	}
	
	private boolean acceptIgnored(boolean ignored, ImportedMembershipRow row) {
		if(!ignored) return true;
		return row.isIgnored();
	}
	
	private boolean accept(String searchValue, ImportedMembershipRow row) {
		if(searchValue == null) return true;
		
		return accept(searchValue, row.getUsername())
				|| accept(searchValue, row.getCurriculumIdentifier())
				|| accept(searchValue, row.getImplementationIdentifier())
				|| accept(searchValue, row.getIdentifier());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ImportedMembershipRow importedRow = getObject(row);
		return getValueAt(importedRow, col);
	}
	
	@Override
	public Object getValueAt(ImportedMembershipRow importedRow, int col) {
		return switch(COLS[col]) {
			case rowNum -> Integer.valueOf(importedRow.getRowNum());
			case status -> importedRow.getStatus();
			case infos, infosWarnings, infosErrors -> importedRow.getValidationResultsLink();
			case ignore -> importedRow.getIgnoreEl();
			case curriculumIdentifier -> importedRow.getCurriculumIdentifier();
			case implementationIdentifier -> importedRow.getImplementationIdentifier();
			case identifier -> importedRow.getIdentifier();
			case role -> importedRow.getRole();
			case username -> importedRow.getUsername();
			case tools -> Boolean.valueOf(importedRow.getIgnoreEl() != null && importedRow.getIgnoreEl().isEnabled());
			default -> "ERROR";
		};
	}

	@Override
	public void setObjects(List<ImportedMembershipRow> objects) {
		this.backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}
}
