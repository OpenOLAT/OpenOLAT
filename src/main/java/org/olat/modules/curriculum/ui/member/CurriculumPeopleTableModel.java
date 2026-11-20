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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 17 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumPeopleTableModel extends DefaultFlexiTableDataModel<CurriculumPeopleRow>
implements SortableFlexiTableDataModel<CurriculumPeopleRow>, FilterableFlexiTableModel {
	
	private PeopleCols[] COLS = PeopleCols.values();
	
	private final Locale locale;
	private List<CurriculumPeopleRow> backupRows;
	
	public CurriculumPeopleTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<CurriculumPeopleRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			String searchString = quickSearch == null ? null : quickSearch.toLowerCase();
			boolean pendingMemberships = getFilterPending(filters);
			List<CurriculumRoles> roles = getFilterByRoles(filters);
			Set<Long> organisationKeys = getFilteredOrganisations(filters);
			List<CurriculumPeopleRow> filteredRows = backupRows.stream()
					.filter(row -> acceptSearch(row, searchString)
							&& acceptPendingMemberships(row, pendingMemberships)
							&& acceptOrganisations(row, organisationKeys)
							&& acceptRoles(row, roles))
					.toList();
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private Set<Long> getFilteredOrganisations(List<FlexiTableFilter> filters) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, CurriculumPeopleController.FILTER_ORGANISATIONS);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(Long::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return null;
    }
	
	private List<CurriculumRoles> getFilterByRoles(List<FlexiTableFilter> filters) {
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, CurriculumPeopleController.FILTER_ROLES);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(CurriculumRoles::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	private boolean getFilterPending(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, CurriculumPeopleController.FILTER_PENDING);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && filterValues.contains(CurriculumPeopleController.FILTER_PENDING);
		}
		return false;
	}

	private boolean acceptPendingMemberships(CurriculumPeopleRow row, boolean pendingMemberships) {
		if(!pendingMemberships) return true;
		return row.getNumOfReservations() > 0;
	}
	
	private boolean acceptSearch(CurriculumPeopleRow row, String searchString) {
		if(searchString == null) return true;
		
		String[] userProps = row.getIdentityProps();
		for(String userProp:userProps) {
			if(userProp != null && userProp.toLowerCase().contains(searchString)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean acceptRoles(CurriculumPeopleRow row, List<CurriculumRoles> roles) {
		if(roles == null || roles.isEmpty()) return true;
		
		for(CurriculumRoles role:roles) {
			if(row.getRoles().contains(role)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean acceptOrganisations(CurriculumPeopleRow row, Set<Long> organisationsKeys) {
		if(organisationsKeys == null || organisationsKeys.isEmpty()) return true;
		
		List<OrganisationWithParents> organisations = row.getOrganisations();
		return organisations == null || organisations.isEmpty()
			? false
			: organisations.stream().anyMatch(org -> organisationsKeys.contains(org.getKey()));
	}
	
	public CurriculumPeopleRow getNext(CurriculumPeopleRow row) {
		int nextIndex = getObjects().indexOf(row) + 1;
		if(nextIndex < 0 || nextIndex >= getRowCount()) {
			nextIndex = 0;
		}
		return getObject(nextIndex);
	}
	
	public CurriculumPeopleRow getPrevious(CurriculumPeopleRow row) {
		int previousIndex = getObjects().indexOf(row) - 1;
		if(previousIndex < 0 || previousIndex >= getRowCount()) {
			previousIndex = getRowCount() - 1;
		}
		return getObject(previousIndex);
	}
	
	public CurriculumPeopleRow getObjectByKey(Long key) {
		if(backupRows == null || backupRows.isEmpty() || key == null) return null;
		return backupRows.stream()
				.filter(row -> key.equals(row.getIdentityKey()))
				.findFirst().orElse(null);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumPeopleRow peopleRow = this.getObject(row);
		return getValueAt(peopleRow, col);
	}

	@Override
	public Object getValueAt(CurriculumPeopleRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case numOfImplementations -> row.getNumOfImplementations();
				case role -> row.getRoles();
				case numOfPendingReservations -> row.getNumOfReservations();
				case tools -> row.getToolsLink();
				default -> "ERROR";
			};
		}
		int propPos = col - CurriculumPeopleController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<CurriculumPeopleRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum PeopleCols implements FlexiSortableColumnDef {

		numOfImplementations("table.header.num.of.implementations"),
		role("table.header.role"),
		numOfPendingReservations("table.header.num.of.pending"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private PeopleCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
