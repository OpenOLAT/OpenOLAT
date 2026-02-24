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
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewUsersTableModel extends DefaultFlexiTableDataModel<ImportedUserRow>
implements FilterableFlexiTableModel, SortableFlexiTableDataModel<ImportedUserRow>  {
	
	private static final ImportCurriculumsCols[] COLS = ImportCurriculumsCols.values();

	private final Locale locale;
	private List<ImportedUserRow> backupList;
	
	public ImportCurriculumsReviewUsersTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public List<String> getUsernames() {
		if(backupList == null) List.of();

		Set<String> usernames = backupList.stream()
			.map(ImportedUserRow::getUsername)
			.filter(username -> StringHelper.containsNonWhitespace(username))
			.collect(Collectors.toSet());
		return List.copyOf(usernames);
	}
	
	public List<Organisation> getOrganisations() {
		if(backupList == null) List.of();

		Set<Organisation> organisations = backupList.stream()
			.map(ImportedUserRow::getOrganisation)
			.filter(org -> org != null)
			.collect(Collectors.toSet());
		return List.copyOf(organisations);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ImportedUserRow> rows = new ImportCurriculumsReviewUsersTableSortDelegate(orderBy, this, locale).sort();
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
			final Set<Long> organisations = getFilteredLongList(filters, ImportCurriculumsReviewCurriculumsController.ORGANISATION_KEY);
			
			List<ImportedUserRow> filteredRows = new ArrayList<>(backupList.size());
			for(ImportedUserRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptOrganisations(organisations, row)
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
	
	private Set<Long> getFilteredLongList(List<FlexiTableFilter> filters, String filterName) {
		Set<String> set = getFilteredList(filters, filterName);
		return set.stream()
				.filter(s -> StringHelper.isLong(s))
				.map(s -> Long.valueOf(s))
				.collect(Collectors.toSet());
	}
	
	private boolean isFilterSelected(List<FlexiTableFilter> filters, String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}

	private boolean acceptOrganisations(Set<Long> organisations, ImportedUserRow row) {
		if(organisations == null || organisations.isEmpty()) return true;
		return row.getOrganisation() != null && organisations.contains(row.getOrganisation().getKey());
	}
	
	private boolean acceptStatus(Set<String> status, ImportedUserRow row) {
		if(status == null || status.isEmpty()) return true;
		
		if((row.getStatus() == ImportCurriculumsStatus.ERROR && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_WITH_ERRORS))
				|| (row.getStatus() == ImportCurriculumsStatus.MODIFIED && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_MODIFIED))) {
			return true;
		}
		
		if(row.getIdentity() == null && status.contains(ImportCurriculumsReviewCurriculumsController.STATUS_NEW)) {
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
	
	private boolean acceptUsername(Set<String> usernames, ImportedUserRow row) {
		if(usernames == null || usernames.isEmpty()) return true;
		
		String username = row.getUsername();
		return StringHelper.containsNonWhitespace(username) && usernames.contains(username);
	}
	
	private boolean acceptIgnored(boolean ignored, ImportedUserRow row) {
		if(!ignored) return true;
		return row.isIgnored();
	}
	
	private boolean accept(String searchValue, ImportedUserRow row) {
		if(searchValue == null) return true;
		
		String[] identityProps = row.getIdentityProps();
		if(identityProps != null && identityProps.length > 0) {
			for(int i=identityProps.length; i-->0; ) {
				String identityProp = identityProps[i];
				if(accept(searchValue, identityProp)) {
					return true;
				}
			}
		}
		return accept(searchValue, row.getUsername())
				|| accept(searchValue, row.getOrganisationIdentifier());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ImportedUserRow userRow = getObject(row);
		return getValueAt(userRow, col);
	}
	
	@Override
	public Object getValueAt(ImportedUserRow userRow, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case rowNum -> Integer.valueOf(userRow.getRowNum());
				case status -> userRow.getStatus();
				case infos, infosWarnings, infosErrors -> userRow.getValidationResultsLink();
				case ignore -> userRow.getIgnoreEl();
				case tools -> Boolean.valueOf(userRow.getIgnoreEl() != null && userRow.getIgnoreEl().isEnabled());
				case organisationIdentifier -> userRow.getOrganisationIdentifier();
				case password -> userRow.getPassword();
				default -> "ERROR";
			};
		}
		
		if(col >= ImportCurriculumsReviewUsersController.USER_PROPS_OFFSET) {
			int propPos = col - ImportCurriculumsReviewUsersController.USER_PROPS_OFFSET;
			return userRow.getIdentityProp(propPos);
		}
		return "ERROR";
	}

	@Override
	public void setObjects(List<ImportedUserRow> objects) {
		backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}
}
